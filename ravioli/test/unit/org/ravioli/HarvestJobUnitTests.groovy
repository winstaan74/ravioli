package org.ravioli

import grails.test.*

class HarvestJobUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		harvestControl = mockFor(HarvestService)
		backgroundControl = mockFor(BackgroundServiceStub)
    }

    protected void tearDown() {
        super.tearDown()
    }
	
	def harvestControl
	def backgroundControl
	
    void testExecute() {
		harvestControl.demand.readRofr() { inc ->
			assertTrue("incremental should be true",inc)
			return new HarvestResults()
		}
		def r1 = new Registry(ivorn:'ivo://a.b.c')
		def r2 = new Registry(ivorn:'ivo://c.d.e')
		mockDomain(Registry,[r1,r2])
		mockLogging(HarvestJob)
		backgroundControl.demand.execute(2..2) { name, clos ->
			clos.call()
		}
		
		harvestControl.demand.harvest(2..2) {reg, inc ->
			assertTrue('incremental should be true',inc)
		}
		
		def job = new HarvestJob();
		job.harvestService = harvestControl.createMock()
		job.backgroundService = backgroundControl.createMock()
		
		job.execute()
		
		harvestControl.verify()
		backgroundControl.verify()
		
		
    }
}
