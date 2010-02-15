package org.ravioli

import grails.test.*

class TaskServiceIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		s = new TaskService()
    }
	
	TaskService s
	
    protected void tearDown() {
        super.tearDown()
		s = null;
    }

    void testRunNextTaskEmpty() {
    	assertTrue Task.list().isEmpty()
		assertFalse s.runNextTask()
    }
	
	void testRunNextTask() {
		Task t = new Task()
		assertNotNull t.save()
		assertTrue s.runNextTask()
		
		// reload t from db. - probably unnecessary
		t = Task.get(t.id)
		assertTrue t.completed
		assertEquals 1,t.retries
		assertNotNull t.creation
		
		// check executions
		assertEquals 1, t.executions.size()
		TaskExecution te = t.executions.iterator().next()
		assertNotNull te.start
		assertNotNull te.finish
		assertTrue te.finish >= te.start
		assertTrue te.messages.contains('warning')
		assertEquals Outcome.COMPLETED, te.outcome
		
	}
	
	
}
