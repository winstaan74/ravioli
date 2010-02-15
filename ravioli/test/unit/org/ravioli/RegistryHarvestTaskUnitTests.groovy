package org.ravioli;

import grails.test.GrailsUnitTestCase;

class RegistryHarvestTaskUnitTests extends GrailsUnitTestCase {
	protected void setUp() throws Exception {
		super.setUp();
		mockLogging(RegParserService)
		mockForConstraintsTests Registry
		parserControl = mockFor(RegParserService)
	}
	
	def parserControl
	
	void testHarvest() {
		Resource existing = new Resource(ivorn:'ivo://foo.bar.choo')
		Resource deleteable = new Resource(ivorn:'ivo://delete.me')
		mockDomain(Resource, [existing,deleteable])
		mockDomain(Task)
		Date now = new Date()
		Registry r = new Registry()
		parserControl.demand.identify() {reg ->
			assertSame(r,reg)
		}
		def ids = [[ivorn:existing.ivorn, deleted:false]
		,[ivorn:'ivo://ibble.wibble/woo', deleted:false]
		,[ivorn:deleteable.ivorn, deleted:true]
		]
		parserControl.demand.listIdentifiers {reg, inc ->
			assertSame r,reg
			return [
			resumptionToken: "foo"
			,totalSize: ids.size()
			,ids:ids
			]
		}
		
		def task = new RegistryHarvestTask(reg:r)
		
		task.regParserService = parserControl.createMock()
		def outcome = task.run(System.out)
		assertEquals Outcome.COMPLETED, outcome
		parserControl.verify()
		assertTrue now <= r.lastHarvest
		// check we've seen a deletion..
		assertNull Resource.findByIvorn(deleteable.ivorn)

		assertEquals 3, Task.list().size()
		// check we've got what we expect.
	
		
	}
}
