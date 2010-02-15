package org.ravioli;

import grails.test.*



class HarvestServiceUnitTests extends GrailsUnitTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		mockLogging(HarvestService)
		mockLogging(RegParserService)
		mockForConstraintsTests Registry
		parserControl = mockFor(RegParserService)
		hs = new HarvestService()
		
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		hs = null
	}
	def parserControl
	HarvestService hs;
	
	

	
	void testReadRofr() {
		Registry rofr = new Registry(ivorn:'ivo://ivoa.net/rofr') 
		Rofr rofrDate = new Rofr()
		Date now = new Date()
		mockDomain(Rofr,[rofrDate])
		mockForConstraintsTests(Registry)
		mockDomain(Registry,[
		                     rofr
		                     ,new Registry(ivorn:'ivo://ibble.neep'
							, name:'do not want to see this'
							, lastHarvest: now)
		])

		def m1 = [name:"Foo",ivorn:"ivo://foo.bar",endpoint:"http://www.something.com",manages:[]]
		def m2 = [name:"Bar",ivorn:"ivo://ibble.neep",endpoint:"http://www.something.com",manages:['fink','funk']]
		def invalid = [name:'Missing stuff']   
		def invalidUpdate = [ivorn:"ivo://ibble.neep",name:"Dont want to see this"]
		// set expectations on parser here.
		parserControl.demand.identify() {r ->
			assertSame(rofr,r)
		}
		parserControl.demand.parseRofr() {r, closure ->
			assertSame(rofr,r)
			closure.call(m1)
			closure.call(invalid)
			closure.call(m2)
			closure.call(invalidUpdate)
		}
		hs.regParserService = parserControl.createMock()
		def results = hs.readRofr()
		parserControl.verify()
		// validate results
		assertNotNull(results)
		assertEquals(1,results.modified)
		assertEquals 1,results.created
		assertEquals 2,results.errors.size()
		// check we've got a new one created
		Registry foo = Registry.findByName("Foo")
		assertNotNull(foo)
		assertEquals(m1.ivorn,foo.ivorn)
		assertEquals(m1.endpoint,foo.endpoint)
		assertEquals(0,foo.manages.size())
		
		// check we've seen am update, nut not the erroneous update
		Registry bar = Registry.findByName("Bar")
		assertNotNull(bar)
		assertEquals(m2.ivorn,bar.ivorn)
		assertEquals(m2.endpoint,bar.endpoint)
		assertEquals(2,bar.manages.size())		
		assertEquals(now,bar.lastHarvest) // verify that this field has _not_ been updated
		
		// check rofr is still htere, and harvest date on it is unchanged
		// instead, harvest data should be recorded on rofrDate
		Registry rofr1 = Registry.findByIvorn('ivo://ivoa.net/rofr')
		assertNotNull(rofr1)
		assertNull(rofr1.lastHarvest)

		//
		assertNotNull rofrDate.lastHarvest
		assertTrue(rofrDate.lastHarvest > now)
	}
	
	
}
