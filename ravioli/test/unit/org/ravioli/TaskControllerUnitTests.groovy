package org.ravioli

import grails.test.*
@Mixin(RavioliAssert)
class TaskControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }


	public void testCleanup() {
		def l = [
		         new Task(completed:true)
		         , new Task(completed:false)
				,new Task(completed:true)
		]
		mockDomain(Task,l)
		assertTrue Task.list().any{ it.completed}
		this.controller.cleanup()
		
		assertTrue Task.list().every {! it.completed}
	}
}
