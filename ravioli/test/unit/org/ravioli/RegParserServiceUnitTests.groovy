

package org.ravioli
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import javax.xml.transform.TransformerException;
import grails.test.*

class RegParserServiceUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		mockLogging(RegParserService)
		mockLogging XmlService
		parser = new RegParserService()
		parser.xmlService = new XmlService()
        
        String.metaClass.encodeAsURL = {org.codehaus.groovy.grails.plugins.codecs.URLCodec.encode(delegate)}
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
		def r = parser.listIdentifiers(ro)
		assertNotNull r
		assertEquals 16,r.totalSize
		assertNull r.resumptionToken
		def l = r.ids
		l.each {
			assertTrue (it instanceof Map)
			assertTrue(it.ivorn.startsWith("ivo://"))
			assertTrue(it.deleted instanceof Boolean)
			}
		def f = l.findAll{it.deleted}
		assertEquals 1, f.size()
		assertEquals 'ivo://org.gavo.dc/static/registryrecs/registry.rr',f[0].ivorn
	}
	
	void testConstructResumeListQuery() {
		// bit of magic to mock-out a url encoder - that's provided by the grails runtime.
		String.metaClass.encodeAsURL = {org.codehaus.groovy.grails.plugins.codecs.URLCodec.encode(delegate)}
		String url = 'http://www.slashdot.org/foo'
		Registry ro = new Registry(endpoint:url)
		// kinds of token seen in the wild.
		// does this work, considering the spaces?? seems to.
		def tokens = ['1264903361556:200:203: : :ivo_managed:ivo_vor','ivo_managed!!!ivo_vor!1']
		tokens.each {
			def u = parser.constructResumeListQuery(ro,it)
			// just verify it's created a valid URL
			URL u1 = new URL(u);
			assertEquals u, u1.toString()
		}
	}
	
	/** test for the different variants of resumption token */
	void testParseResumptionToken() {
		// test for invalid input
		def xml = new XmlSlurper().parseText( "<foo><bar stuff=''/></foo>")
		shouldFail(AssertionError) {
			parser.parseResumptionToken(xml.bar)
		}
		// test for document not containing a r.t.
		xml = new XmlSlurper().parseText( "<foo><bar stuff=''/></foo>")
		def r = parser.parseResumptionToken(xml.resumptionToken)
		assertNotNull(r)
		assertNull r.totalSize
		assertNull r.resumptionToken

		// test for size, and token
		xml = new XmlSlurper().parseText (
				"<foo><resumptionToken completeListSize='22'>atoken</resumptionToken></foo>"
		)
		r = parser.parseResumptionToken(xml.resumptionToken)
		assertEquals "atoken", r.resumptionToken
		assertEquals 22, r.totalSize
		
		// test for token with no size
		xml = new XmlSlurper().parseText (
				"<foo><resumptionToken >atoken</resumptionToken></foo>"
				)
		r = parser.parseResumptionToken(xml.resumptionToken)
		assertEquals "atoken", r.resumptionToken
		assertNull r.totalSize
		
		// test for size, with no token - 2 variants.
		xml = new XmlSlurper().parseText (
				"<foo><resumptionToken completeListSize='22'></resumptionToken></foo>"
				)
		r = parser.parseResumptionToken(xml.resumptionToken)
		assertNull r.resumptionToken
		assertEquals 22, r.totalSize
		
		xml = new XmlSlurper().parseText (
				"<foo><resumptionToken completeListSize='22'/></foo>"
				)
		r = parser.parseResumptionToken(xml.resumptionToken)
		assertNull r.resumptionToken
		assertEquals 22, r.totalSize
	}
	
	
	void testHarvest() {
		String url = this.class.getResource("registryResource.xml").toString()
		Registry ro = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		String xml = parser.harvest(ro,"ivo://ivoa.net.rofr")
		assertNotNull(xml)
		def gp = new XmlSlurper().parseText(xml)
		assertEquals('Resource',gp.name())
	}
	
	void testHarvestUnknown() {
		String url = this.class.getResource("unknownResource.xml").toString()
		Registry ro = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
		try {
			parser.harvest(ro,"ivo://ivoa.net.rofr")
			fail ("should have failed")
		} catch (TransformerException t) {
			def cause = t.getCause();
			assertNotNull(cause)
			assertEquals HarvestServiceException, cause.getClass()
			assertEquals 'The value of the identifier argument is unknown or illegal in this repository',cause.message
		}
	}
	
//	void testHarvestSlurp() {
//		String url = this.class.getResource("registryResource.xml").toString()
//		Registry ro = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
//		def gp = parser.harvestSlurp(ro,"ivo://ivoa.net.rofr")
//		assertEquals('Resource',gp.name())
//	}
//	
//	void testHarvestSlurpUnknown() {
//		String url = this.class.getResource("unknownResource.xml").toString()
//		Registry ro = new Registry(endpoint:url,ivorn:'ivo://ivoa.net/rofr')
//		try {
//			parser.harvestSlurp(ro,"ivo://ivoa.net.rofr")
//			fail ("should have failed")
//		} catch (HarvestServiceException t) {
//			assertEquals 'The value of the identifier argument is unknown or illegal in this repository',t.message
//		}
//	}
	
}
