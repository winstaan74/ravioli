package org.ravioli
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;

import grails.test.*

class RegParserServiceUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		mockLogging(RegParserService)
		parser = new RegParserService()
		parser.xmlService = new XmlService()
    }

    protected void tearDown() {
        super.tearDown()
		parser = null;
    }
	
	RegParserService parser

    void testIdentify() {
        String url = this.class.getResource("identify.xml").toString();
		Registry r = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		parser.identify(r) // will throw if fails.
    }

    void testIdentifyFaulty() {
		String url = this.class.getResource("faultyIdentify.xml").toString();
		Registry r = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		shouldFail(IdentifyException) {
			parser.identify(r) // will throw if fails.
		}
    }
	
	
	void testConstructListQuery() {
		def url = "http://rofr.ivoa.net/cgi-bin/oai.pl" 
		Registry r = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		// not incremental
		def url1 = parser.constructListQuery(r,false)
		assertNotNull(url1)
		assertTrue(url != url1)
		assertTrue(url1.startsWith(url))
		
		// incremental, no date
		def url2 = parser.constructListQuery(r)
		assertEquals(url1,url2)
		
		// increental, date provided.
		Date now = new Date()
		r.lastHarvest = now
		def url3 = parser.constructListQuery(r)
		assertTrue(url1 != url3)
		assertTrue(url3.startsWith(url1))
	}
	
	void testConstructRofrQuery() {
		Rofr rofr = new Rofr()
		mockDomain(Rofr,[rofr])
		def url = "http://rofr.ivoa.net/cgi-bin/oai.pl" 
		Registry r = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		// not incremental
		def url1 = parser.constructRofrQuery(r,false)
		assertNotNull(url1)
		assertTrue(url != url1)
		assertTrue(url1.startsWith(url))
		
		// incremental, no date
		def url2 = parser.constructRofrQuery(r)
		assertEquals(url1,url2)
		
		// increental, date provided.
		Date now = new Date()
		rofr.lastHarvest = now
		def url3 = parser.constructRofrQuery(r)
		assertTrue(url1 != url3)
		assertTrue(url3.startsWith(url1))
	}
	
	
	
	final int REG_COUNT = 16;
	
    void testParseRofr() {
		String url = this.class.getResource("rofrRegistries.xml").toString()
		Registry r = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		Rofr rofr = new Rofr()
		mockDomain(Rofr,[rofr])
        def callCount = 0
        String id = parser.parseRofr(r) {
            callCount ++
            assertNotNull it.name
			assertTrue (it.name instanceof String)
            assertNotNull it.ivorn
			assertTrue (it.ivorn instanceof String)
            assertNotNull it.manages
            it.manages.each {
                assertTrue it.getClass().getName(), (it instanceof String)
            }
            assertNotNull it.endpoint
			assertTrue (it.endpoint instanceof String)
        }
        assertTrue (callCount > 0)
		assertEquals(REG_COUNT,callCount)
    }
	
	void testPassWrongRegIntoParseRofr() {
		shouldFail (IllegalArgumentException){
			Registry r = new Registry(ivorn: 'ivo://something.else')
			parser.parseRofr r, {
			
			}
		}
	}
	
	
	/** test we can create a registry object from each one*/
	void testCreateRegistryObjectsFromRofr() {
		mockForConstraintsTests Registry;
		mockDomain Rofr, [new Rofr()]
		String url = this.class.getResource("rofrRegistries.xml").toString()
		Registry ro = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		def callCount = 0
		String id = parser.parseRofr(ro) {
			callCount ++
			Registry r= new Registry(it)
			assertTrue(r.validate())
		}
		assertTrue (callCount > 0)
		assertEquals(REG_COUNT,callCount)
	}
	
	void testListIdentifiers() {
		String url = this.class.getResource("listIdentifiers.xml").toString()
		Registry ro = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		def l = parser.listIdentifiers(ro)
		assertNotNull l
		assertTrue l.size > 0
		l.each {
			assertTrue (it instanceof String)
			assertTrue(it.startsWith("ivo://"))			
			}
	}
	
	void testHarvest() {
		String url = this.class.getResource("registryResource.xml").toString()
		Registry ro = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		String xml = parser.harvest(ro,"ivo://ivoa.net.rofr")
		assertNotNull(xml)
		def gp = new XmlSlurper().parseText(xml)
		assertEquals('Resource',gp.name())
	}

}
