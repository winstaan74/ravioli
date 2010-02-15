package org.ravioli

import grails.test.*

class DailyHarvestJobUnitTests extends GrailsUnitTestCase {
	protected void setUp() {
		super.setUp()
		harvestControl = mockFor(HarvestService)
	}
	
	protected void tearDown() {
		super.tearDown()
	}
	
	def harvestControl
	
	void testExecute() {
		harvestControl.demand.readRofr() { inc ->
			assertTrue("incremental should be true",inc)
			return new HarvestResults()
		}
		def r = new Registry(ivorn:'ivo://foo.bar.choo',name:'a reg')
		def r1 = new Registry(ivorn:'ivo://wibble.nibble', name:'another reg')
		def rs = [r,r1]
		mockDomain(Registry,rs)
		mockDomain(RegistryHarvestTask,[])
		mockLogging(DailyHarvestJob)
		
		def job = new DailyHarvestJob();
		job.harvestService = harvestControl.createMock()
		
		job.execute()
		
		harvestControl.verify()
		
		def l = RegistryHarvestTask.list()
		assertEquals rs.size(), l.size()
		
		assertEquals rs, l*.reg
		assertTrue l.every{ it.incremental}
		
	}
}
