package org.ravioli

import grails.test.*

class TaskExecutionUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

	
	void testConstraints() {
		mockForConstraintsTests TaskExecution
		
		TaskExecution te = new TaskExecution()
		
		if (! te.validate()) {
			te.errors.allErrors.each {println it}
		}
		assertTrue te.validate() // t created in valid state.
	}
}
