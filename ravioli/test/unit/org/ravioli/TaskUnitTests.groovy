package org.ravioli

import grails.test.*

class TaskUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testStartingCondition() {
		//mockDomain(TaskExecution)
		mockDomain(Task)
    	Task t = new Task()
		
		assertNotNull(t.creation)
		//assertNull t.execution
		//		assertNotNull(t.execution)
		//		assertEquals 0, t.execution.size()
		
		assertEquals 0, t.retries
		
		assertFalse t.completed
    }
	
	// no constraints on task - should there be.
	void testConstraints() {
		mockForConstraintsTests(Task)
		
		Task t = new Task()
		
		assertTrue t.validate() // t created in valid state.
	}
}
