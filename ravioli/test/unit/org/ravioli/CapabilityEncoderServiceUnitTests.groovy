package org.ravioli
import groovy.util.XmlSlurper;

import grails.test.*

class CapabilityEncoderServiceUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		service = new CapabilityEncoderService();
    }
	
	def service

    protected void tearDown() {
        super.tearDown()
		service = null
    }

    void testRules() {
		def r = service.rules
		assertNotNull(r)
		assertTrue r.size() > 0

    }
	
	void testEncode() {
		def u = this.class.getResource("14.xml")
		def xml = new XmlSlurper().parse(u.toString())

		assertEquals 'Resource', xml.name()
		int code = service.encode(xml)
		assertTrue code > 0
		
		def l = service.decode(code)
		assertNotNull(l)
		
		def expected = [
		                'Web interface'
		                ,'Catalog cone search service'
		                ,'Table metadata'
		                ,'Downloadable tables'
						]
		assertEquals expected.sort(), l.sort()
	}
	
	void testJavascriptDecoder() {
		// check we can generate it, for starters
		def str = service.javascriptDecoder()
		// todo - would be nice to be able to parse the JS
	}
}
