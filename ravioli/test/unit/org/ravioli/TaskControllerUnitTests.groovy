package org.ravioli

import grails.test.*

class TaskControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }
	void testIndex() {
		this.controller.index()
		redirectsTo('list')
	}
	
	private void redirectsTo(String action) {
		assertEquals action, this.controller.redirectArgs['action']
		assertNull  this.controller.redirectArgs['controller']
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
