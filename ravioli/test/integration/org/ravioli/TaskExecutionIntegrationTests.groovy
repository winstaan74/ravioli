package org.ravioli

import grails.test.*

class TaskExecutionIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testCRUD() {
    	assertEquals 0, TaskExecution.list().size()
		
		TaskExecution te = new TaskExecution()
		assertTrue te.validate()
		te.save()
		
		assertEquals 1, TaskExecution.list().size()
		assertEquals te, TaskExecution.list()[0]
    }
}
