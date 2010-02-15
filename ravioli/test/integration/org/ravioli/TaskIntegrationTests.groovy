package org.ravioli

import grails.test.*

/** at the moment I'm seeing errors from hibernate 
 * when creating task and taskExecution objects.
 * these don't appear in unit tests, so will write some 
 * integration tests to tease them out.
 * 
 * ok. dunno what I've done, but things are working ok again now.
 */
class TaskIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }
	

    void testTaskCRUD() {
		
		assertEquals 0, Task.list().size()
		
		Task t = new Task()
		assertTrue t.validate()
		t.save()
		
		assertEquals 1, Task.list().size()
		assertEquals t, Task.list()[0]
	}
	
	void testAssociationWithTaskExecution() {
		Task t = new Task()
		t.save(flush:true)
		assertNull t.executions

		def te = new TaskExecution()
		t.addToExecutions(te)
		t.save(flush:true)

		assertEquals 1, t.executions.size()
		assertEquals te, t.executions.iterator().next()
		
		// save has cascaded.
		assertEquals 1, TaskExecution.list().size()
		assertEquals te, TaskExecution.list()[0]
		
		// check we've can add another.
		def te1 = new TaskExecution()
		t.addToExecutions(te1)
		t.save(flush:true)
		
		assertEquals 2, t.executions.size()
		assertEquals 2, TaskExecution.list().size()
		
//		// does the set method add have the same affect??
//		kinda - but the objects are created in an unstable state - therefore shodln't use.
//		def te2 = new TaskExecution()
//		t.executions.add(te2)
//		t.save()
//		
//		assertEquals 3, t.executions.size()
//		assertEquals 3, TaskExecution.list().size()
		// check we can delete one, and it cascades
		
		t.removeFromExecutions(te)
		t.save(flush:true)
		
		assertEquals 1, t.executions.size()
		// this doesn't actually delete the task execution..
		assertEquals 2, TaskExecution.list().size()
		te.delete()
		assertEquals 1, TaskExecution.list().size()


//		// check we can delete using the collections method.
//		don't trust this, as the collections.add() doesn't work;
//		t.removeFromExecutions(te2)
//		t.save()
//
//		assertEquals 1, t.executions.size()
//		assertEquals 1, TaskExecution.list().size()
//		
		// check we can delete task, and executions are removed too
		t.delete()
		assertTrue Task.list().isEmpty()
		assertTrue TaskExecution.list().isEmpty()

	}
	
	void testSubclasses() {
		// that we can create subclass obects, but find them
		// by parent-class querying methods
		Registry r = Registry.list()[0]
		
		RegistryHarvestTask a = new RegistryHarvestTask(reg:r)
		ResourceHarvestTask b = new ResourceHarvestTask(reg:r, ivorn:'ivo://foo.bar.choo')
		assertTrue a.validate()
		assertTrue b.validate()
		a.save()
		b.save()
		
		def l = Task.list()
		assertEquals 2, l.size()
		assertTrue l.contains(a)
		assertTrue l.contains(b)
	}
}
