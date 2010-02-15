package org.ravioli

import grails.test.*

class CleanupJobUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testCleanupJob() {
		Date now = new Date()
		def remove = [
		    new Task(creation:now - 2 , completed:true)// old and complete
		]
		def remain = [
		     new Task() // crration is now, not complete 
		     , new Task(creation:now - 2) // old and incomplete
			, new Task(completed:true) // completed, but fresh.
		]
		def l = remove + remain
		Collections.shuffle(l)
		mockDomain(Task,l)
    	def job = new CleanupJob()
		job.execute()
		
		def result = Task.list()
		remove.each {
			assertFalse (it in result)
		}
		remain.each {
			assertTrue (it in result)
		}
    }
}
