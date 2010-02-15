package org.ravioli;

import org.xml.sax.SAXParseException;

import grails.test.GrailsUnitTestCase;

class ResourceHarvestTaskUnitTests extends GrailsUnitTestCase {
	protected void setUp() throws Exception {
		super.setUp();
		mockLogging(RegParserService)
		mockForConstraintsTests Registry
		parserControl = mockFor(RegParserService)
	}
	
	
	def parserControl
	
	
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
		
		ResourceHarvestTask task = new ResourceHarvestTask(reg:rofr, ivorn:ivorn)
		task.regParserService = parserControl.createMock()
		assertEquals Outcome.COMPLETED, task.run(System.out)

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
		def task = new ResourceHarvestTask(reg:rofr, ivorn:ivorn)
		task.regParserService = parserControl.createMock()
		assertEquals Outcome.COMPLETED, task.run(System.out)
		
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
		def task = new ResourceHarvestTask(reg:rofr, ivorn:ivorn)
		task.regParserService = parserControl.createMock()
		assertEquals Outcome.COMPLETED, task.run(System.out)
		
		// validate results
		assertEquals(0,Resource.list().size())
		Resource r = Resource.findByIvorn(ivorn)
		assertNull(r)
		
	}
	
	void testHarvestUnrecognizedResource() { // get an unrecognized response from server
		String url = this.class.getResource("unknownResource.xml").toString()
		Registry rofr = new Registry(ivorn:'ivo://ivoa.net/rofr',endpoint:url) 
		String ivorn = 'ivo://jvo/vizier/J/A AS/102/397' // note the problematic space
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
		def task = new ResourceHarvestTask(reg:rofr, ivorn:ivorn)
		task.regParserService = parserControl.createMock()
		assertEquals Outcome.FAILED, task.run(System.out)
		
		// validate results
		assertEquals(0,Resource.list().size())
		Resource r = Resource.findByIvorn(ivorn)
		assertNull(r)
	}
	
	void testHarvestResourceServiceDown() { // get a 404, or similar from server
		// simulate by having a url that points to no-where
		String url = this.class.getResource("unknownResource.xml").toString() + "non-existent"
		Registry rofr = new Registry(ivorn:'ivo://ivoa.net/rofr',endpoint:url) 
		String ivorn = 'ivo://whatever'
		mockDomain(Resource)
		parserControl.demand.harvest() {r, ivo ->
			assertEquals(ivorn,ivo)
			assertEquals(rofr,r)
			def real = new RegParserService()
			real.xmlService = new XmlService()
			return real.harvest(r,ivo)
		}

		def task = new ResourceHarvestTask(reg:rofr, ivorn:ivorn)
		task.regParserService = parserControl.createMock()
		shouldFail(SAXParseException) {
			task.run(System.out)
		}
		// validate results - shold be no changes.
		assertEquals(0,Resource.list().size())
	}
	
	void testHarvestMisidentification() { // mis-identified resource
		String url = this.class.getResource("misidentifiedResource.xml").toString()
		Registry rofr = new Registry(ivorn:'ivo://ivoa.net/rofr',endpoint:url) 
		String ivorn = 'ivo://org.gavo.dc/static/registryrecs/dc.rr' 
		mockDomain(Resource)
		parserControl.demand.harvest() {r, ivo ->
			assertEquals(ivorn,ivo)
			assertEquals(rofr,r)
			def real = new RegParserService()
			real.xmlService = new XmlService()
			return real.harvest(r,ivo)
		}
		def task = new ResourceHarvestTask(reg:rofr, ivorn:ivorn)
		task.regParserService = parserControl.createMock()
		assertEquals Outcome.FAILED, task.run(System.out)
		
		// validate results
		assertEquals(0,Resource.list().size())
	}
}
