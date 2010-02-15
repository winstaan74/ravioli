package org.ravioli;

import grails.test.GrailsUnitTestCase;

class TaskServiceUnitTests extends GrailsUnitTestCase {
	protected void setUp() {
		super.setUp();
		mockLogging(TaskService)
		service = new TaskService()
		// mock the static method withNewSession on Task
		def taskControl = mockFor(Task)
		taskControl.demand.static.withNewSession() { it.call()}
	}
	
	protected void tearDown() {
		super.tearDown()
		service = null;
	}
	
	TaskService service
	
	void testRunNextTaskNoTasks() {
		def ts = []
		mockDomain(Task,ts)
		assertFalse service.runNextTask()
		// nothing should happen.
		assertEquals ([], ts)
	}
	
	void testRunNextTaskFailing() {
		Task t = new FailTask(retries:2)
		def ts = [t]
		mockDomain(Task,ts)
		mockDomain(TaskExecution)
		assertTrue service.runNextTask()
		// nothing should happen.
		assertEquals ([t],ts)
		assertEquals 1, TaskExecution.list().size()
		assertEquals 1, t.executions.size()
		assertEquals 3, t.retries
		assertFalse t.completed
		
		// then inspect the execution message..
		def e = t.executions.iterator().next()
		assertNotNull e.finish
		assertEquals Outcome.FAILED, e.outcome
		assertTrue e.messages.contains("cheese")
		assertEquals 1,e.messages.readLines().size()
	}
	
	void testRunNextTaskError() {
		Task t = new ErrorTask(retries:2)
		def ts = [t]
		mockDomain(Task,ts)
		mockDomain(TaskExecution)
		
		assertTrue service.runNextTask()
		// nothing should happen.
		assertEquals ([t],ts)
		assertEquals 1, t.executions.size()
		assertEquals 3, t.retries
		assertFalse t.completed
		
		// then inspect the execution message..
		def e = t.executions.iterator().next()
		assertNotNull e.finish
		assertEquals Outcome.ERROR, e.outcome
	}
	
	void testRunNextTaskSuccess() {
		Task t = new SuccessTask(retries:2)
		def ts = [t]
		mockDomain(Task,ts)
		mockDomain(TaskExecution)
		assertTrue service.runNextTask()
		// nothing should happen.
		assertEquals ([t],ts)
		assertEquals 1, t.executions.size()
		assertEquals 3, t.retries
		assertTrue t.completed
		
		// then inspect the execution message..
		def e = t.executions.iterator().next()
		assertNotNull e.finish
		assertEquals Outcome.COMPLETED, e.outcome
	}
	
	void testRunNextTaskThrowsError() {
		Task t = new ErrorTask(retries:2,throwE:true)
		def ts = [t]
		mockDomain(Task,ts)
		mockDomain(TaskExecution)
		assertTrue service.runNextTask()
		// nothing should happen.
		assertEquals ([t],ts)
		assertEquals 1, t.executions.size()
		assertEquals 3, t.retries
		assertFalse t.completed
		
		// then inspect the execution message..
		def e = t.executions.iterator().next()
		assertNotNull e.finish
		assertEquals Outcome.ERROR, e.outcome
		// check we've got something looking like a stacktrace
		assertTrue e.messages.readLines().size() > 5
	}
	
	void testFindNextTask() {
		// empty db.
		def ts = []
		mockDomain(Task,ts)
		assertNull service.findNextTask()

		// completed task
		Task t = new Task(completed:true)
		ts << t
		t.save()
		assertNull service.findNextTask()
		
		// incomplete, but over limit of retries
		t.completed = false;
		t.retries = TaskService.MAX_RETRIES;
		t.save()
		assertNull service.findNextTask()
		
		// suitable candidate
		t.retries = 0
		t.save()
		assertEquals t, service.findNextTask()
		
		// another suitable candidate - each has same number of retries, so 
		// we should get the oldest one first.
		Task t1 = new Task()
		// verify that t1 is 'younger' than (i.e. has a greater date)
		assertTrue t1.creation > t.creation
		// make sure theuy've got the same number of retires
		t1.retries = t.retries
		ts << t1
		t1.save()
		assertEquals t, service.findNextTask()
		// 'remove' this one from the queue, and then we should see the next
		t.completed = true;
		t.save()
		assertEquals t1, service.findNextTask()
		// remove this one too, and we're back to empty.
		t1.retries = TaskService.MAX_RETRIES;
		t1.save();
		assertNull service.findNextTask()
		
		// reset, try another sequence.
		t1.retries = t.retries = 0
		t1.completed = t.completed = false
		t.save(); t1.save()
		// we expect to get oldest
		assertEquals t, service.findNextTask()
		// but if we increment it's retries, then we expect to find the other one.
		t.retries++
		t.save()
		assertTrue t.retries > t1.retries
		assertEquals t1, service.findNextTask()
	}
} // end task
		
class SuccessTask extends Task {
	Outcome run(PrintStream out) {
		return Outcome.COMPLETED;
	}
}

class FailTask extends Task {
	Outcome run(PrintStream out) {
		out << "wrong kind of cheese!!"
		return Outcome.FAILED;
	}
}

class ErrorTask extends Task {
	def throwE = false;
	Outcome run(PrintStream out) {
		if (throwE) {
			throw new Exception();
		}
		return Outcome.ERROR
	}
}
