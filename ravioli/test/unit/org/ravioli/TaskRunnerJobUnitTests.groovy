package org.ravioli

import grails.test.*

class TaskRunnerJobUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testExecute() {
		mockLogging(TaskRunnerJob)
		def job = new TaskRunnerJob()
		def sControl = mockFor(TaskService)
		int i = 0;
		sControl.demand.runNextTask(4..4) {->
			return ++i < 4}
		job.taskService = sControl.createMock()


		// need to mock the withSession method too.
		def tControl = mockFor(Task)
		tControl.demand.static.withNewSession(4..4) { clos ->
			clos.call()
		}

		job.execute()
		
		sControl.verify()
		

    }
}
