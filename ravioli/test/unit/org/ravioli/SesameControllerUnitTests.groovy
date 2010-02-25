


package org.ravioli

import org.codehaus.groovy.grails.plugins.codecs.URLCodec;
import grails.converters.JSON;
import grails.test.*
@Mixin(RavioliAssert)
class SesameControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
                                                                 
		loadCodec(URLCodec)

    }
	
	def setEndpoint(val) {
		controller.metaClass.grailsApplication = [config:[ravioli:[sesame:[endpoint:(val+'?')] ]]]
		
	}
	

    protected void tearDown() {
        super.tearDown()
    }

    void testNoObject() {
		controller.index()
		status 400
		

    }
	
	void testUnknownObeject() {
		controller.params.obj = 'not important'
		endpoint = this.class.getResource('sesame-notfound.xml').toString()

		controller.index()
		status 404
		
	}
	
	void testKnownObject() {
		controller.params.obj = 'not important'
		endpoint = this.class.getResource('sesame-ok.xml').toString()

		controller.index()
		// parse the JSO N back again, chekc it's what we expect
		def o =JSON.parse(resp())
		assertNotNull o 
		assertEquals '010.67425',o.ra
		assertEquals '+40.8651667',o.dec
	}
	
	void testServiceDown() { // simulate the service not being avaliable
		controller.params.obj = 'not important'
		endpoint = 'file://pretty/sure/this/doesnt/exist.txt'
		controller.index()
		status 500
	}
	
	void testServiceError() { // simulate the service returning an XML with 'ERROR'
		controller.params.obj = 'not important'
		endpoint = this.class.getResource('sesame-error.xml').toString()
		controller.index()
		status 500
	}
	
	void testService404() { // simulate the service returning malformed HTML in place of the reponse
		controller.params.obj = 'not important'
		endpoint = this.class.getResource('404.html').toString()
		controller.index()
		status 500
	}
	
	void testService500() { // simulate the service a text file in place of the xml
		controller.params.obj = 'not important'
		endpoint = this.class.getResource('500.txt').toString()
		controller.index()
		status 500
	}
}
