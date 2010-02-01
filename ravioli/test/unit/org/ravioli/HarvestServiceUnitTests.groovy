package org.ravioli;

import grails.test.*

class BackgroundServiceStub {
	//just used for testing
	
	def execute(String name, Closure toRun) {
		
	}
}

class HarvestServiceUnitTests extends GrailsUnitTestCase {

	protected void setUp() throws Exception {
		super.setUp();
		mockLogging(HarvestService)
		mockLogging(RegParserService)
		mockForConstraintsTests Registry
		parserControl = mockFor(RegParserService)
		hs = new HarvestService()
		
		// irritating bug - can't import a class in the default namespace.
		// so I'll define my own identical class..
		backgroundControl = mockFor(BackgroundServiceStub)
		
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		hs = null
	}
	def backgroundControl
	def parserControl
	HarvestService hs;
	
	
	void testHarvest() {
		Resource existing = new Resource(ivorn:'ivo://foo.bar.choo')
		Resource deleteable = new Resource(ivorn:'ivo://delete.me')
		mockDomain(Resource, [existing,deleteable])
		mockLogging(HarvestService)
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
		
		// we only see harvest demands for the undeleted resources..
		// plus the recursive call to harvest the remainder.
		backgroundControl.demand.execute(3..3) { msg, clos ->
			}
		hs.regParserService = parserControl.createMock()
		hs.backgroundService = backgroundControl.createMock()
		HarvestResults result = hs.harvest( r)
		parserControl.verify()
		backgroundControl.verify()
		assertTrue now <= r.lastHarvest
		assertEquals 1, result.modified
		assertEquals 1, result.created
		assertEquals 1, result.deleted
		// check we've seen a deletion..
		assertNull Resource.findByIvorn(deleteable.ivorn)
		
	}
	
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
	
	void testHarvestNewResource() {
		String url = this.class.getResource("registryResource.xml").toString()
		Registry rofr = new Registry(ivorn:'ivo://ivoa.net/rofr',endpoint:url) 
		String ivorn = rofr.ivorn
		mockDomain(Resource)
		parserControl.demand.harvest() {r, ivo ->
			assertEquals(ivorn,ivo)
			assertEquals(rofr,r)
			def real = new RegParserService()
			real.xmlService = new XmlService()
			def res = real.harvest(r,ivo)
			
		}
		hs.regParserService = parserControl.createMock()
		def results= hs.harvestResource( rofr, ivorn)
		parserControl.verify()
		// validate results
		assertEquals(1,Resource.list().size())
		Resource r = Resource.findByIvorn(ivorn)
		assertNotNull(r)
		assertEquals(ivorn,r.ivorn)
		assertNotNull(r.xml)
		// of course, rest of fields are populate too - this is covered in Resource unit tests.
	
	}
	
	void testHarvestExistingResource() {
		String url = this.class.getResource("registryResource.xml").toString()
		Registry rofr = new Registry(ivorn:'ivo://ivoa.net/rofr',endpoint:url) 
		String ivorn = rofr.ivorn
		mockDomain(Resource,[
		  new Resource(ivorn:ivorn,title:"don't want to see this")
		])
		parserControl.demand.harvest() {r, ivo ->
			assertEquals(ivorn,ivo)
			assertEquals(rofr,r)
			def real = new RegParserService()
			real.xmlService = new XmlService()
			return real.harvest(r,ivo)
			
		}
		hs.regParserService = parserControl.createMock()
		def results= hs.harvestResource( rofr, ivorn)
		parserControl.verify()
		// validate results
		assertEquals(1,Resource.list().size())
		Resource r = Resource.findByIvorn(ivorn)
		assertNotNull(r)
		assertEquals(ivorn,r.ivorn)
		assertNotNull(r.xml)
		assertEquals("IVOA Registry of Registries",r.title) // verify this field has been overridden
		// of course, rest of fields are populate too - this is covered in Resource unit tests.
		
	}
	
	void testHarvestDeletedResource() {
		String url = this.class.getResource("registryResource1.xml").toString()
		Registry rofr = new Registry(ivorn:'ivo://ivoa.net/rofr',endpoint:url) 
		String ivorn = rofr.ivorn
		mockDomain(Resource,[
				new Resource(ivorn:ivorn,title:"don't want to see this")
				])
		parserControl.demand.harvest() {r, ivo ->
			assertEquals(ivorn,ivo)
			assertEquals(rofr,r)
			def real = new RegParserService()
			real.xmlService = new XmlService()
			return real.harvest(r,ivo)
			
		}
		hs.regParserService = parserControl.createMock()
		def results= hs.harvestResource( rofr, ivorn)
		parserControl.verify()
		// validate results
		assertEquals(0,Resource.list().size())
		Resource r = Resource.findByIvorn(ivorn)
		assertNull(r)
	
	}
	
}
