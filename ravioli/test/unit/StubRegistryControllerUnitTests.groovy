import java.net.URLEncoder;


import groovy.util.XmlSlurper;

import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import grails.test.*
import org.ravioli.*;

class StubRegistryControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
		regParser = new RegParserService() // just used in validation.
		regId = 'ivo://uk.ac.le.star/org.astrogrid.registry.RegistryService'
		String.metaClass.encodeAsMD5 = {org.codehaus.groovy.grails.plugins.codecs.MD5Codec.encode(delegate)}
		controller.params.registryIvorn = regId.encodeAsMD5()
		// mock out the bit of the config object we need.
		controller.grailsApplication = [
		config: [
		   ravioli: [
		    stubRegistry: [
			  basedir: "/Users/noel/tinkering/mock-registry"
			]
		  ]
		]
	]
    }
	
	def regId
	def regParser

    protected void tearDown() {
        super.tearDown()
    }


	void testIdentify() {
		controller.params.verb = 'Identify'
		
		controller.identify()
		assertFalse (404 == renderArgs.status)
		
		assertEquals 'text/xml', controller.response.contentType 
		def xml = new XmlSlurper().parseText(controller.response.contentAsString)
		assertEquals regId, regParser.parseIdentify(xml)
		
	}
	
	void testIdentifyUnknown() {
		controller.params.verb = 'Identify'
		controller.params.registryIvorn = controller.params.registryIvorn + "unknown"
		controller.identify()
		assertEquals 404, renderArgs.status
		assertTrue controller.response.contentAsString.contains("not found")
		
	}
	
	void testListIdentifiers() {
		controller.params.verb = 'ListIdentifiers'
		controller.params.metadataPrefix = 'ivo_vor'
		controller.params.set = 'ivo_managed'
		controller.listidentifiers()
		
		assertFalse (404 == renderArgs.status)
		assertEquals 'text/xml', controller.response.contentType
		def xml = new XmlSlurper().parseText(controller.response.contentAsString)
		def ids = xml.ListIdentifiers.header.identifier*.text()
		assertNotNull(ids)
		assertTrue(ids.size() > 0)
		ids.each {
			it.startsWith('ivo://')
		}
	}
	
	void testListIdentifiersUnknownRegistry() {
		controller.params.verb = 'ListIdentifiers'
		controller.params.metadataPrefix = 'ivo_vor'
		controller.params.set = 'ivo_managed'
		controller.params.registryIvorn = controller.params.registryIvorn + "unknown"
		
		controller.listidentifiers()
		
		assertEquals 404, renderArgs.status
		assertTrue controller.response.contentAsString.contains("not found")
	}
	
	void testListIdentifiersResumption() {
		controller.params.verb = 'ListIdentifiers'
		def token = '1265068515435:200:203: : :ivo_managed:ivo_vor'
		controller.params.resumptionToken = token // no url encoding happening - at this point, params have been decoded back agaoun.
		
		controller.listidentifiers()
		
		assertFalse (404 == renderArgs.status)
		assertEquals 'text/xml', controller.response.contentType
		def xml = new XmlSlurper().parseText(controller.response.contentAsString)
		def ids = xml.ListIdentifiers.header.identifier*.text()
		assertNotNull(ids)
		assertTrue(ids.size() > 0)
		ids.each {
			it.startsWith('ivo://')
		}
	}
	
	void testListIdentifiersUnknownResumption() {
		controller.params.verb = 'ListIdentifiers'
		def token = '1265068515435:200:203: : :ivo_managed:ivo_vor unknown'
		controller.params.resumptionToken = token
		
		controller.listidentifiers()
		
		assertEquals 404, renderArgs.status
		assertTrue controller.response.contentAsString.contains("not found")	
	}
	
	void testGetRecord() {
		controller.params.verb = 'GetRecord'
		def ivorn = 'ivo://uk.ac.le.star.tmpledas/ledas/ledas/ic348cxo'
		controller.params.metadataPrefix = 'ivo_vor'
		controller.params.identifier = ivorn 
		
		controller.getrecord()

		assertFalse (404 == renderArgs.status)
		assertEquals 'text/xml', controller.response.contentType 
		def xml = new XmlSlurper().parseText(controller.response.contentAsString)

		assertEquals ivorn, xml.GetRecord.record.metadata.Resource.identifier.text()
		
	}
	
	void testGetUnknownRecord() {
		controller.params.verb = 'GetRecord'
		controller.params.metadataPrefix = 'ivo_vor'
		def ivorn = 'ivo://uk.ac.le.star.tmpledas/ledas/ledas/ic348cxo-unknown'
		controller.params.identifier = ivorn 
		
		controller.getrecord()
		
		assertEquals 404, renderArgs.status
		assertTrue controller.response.contentAsString.contains("not found")
		
		
	}
//	
//	void testGetResourceWithPlusInIvorn() {
//		controller.params.verb = 'GetRecord'
//		controller.params.metadataPrefix = 'ivo_vor'
//		def ivorn = 'ivo://CDS.VizieR/J/A+A/265/32'
//		controller.params.identifier = ivorn 
//		
//		controller.getrecord()
//		
//		assertFalse (404 == renderArgs.status)
//		assertEquals 'text/xml', controller.response.contentType 
//		def xml = new XmlSlurper().parseText(controller.response.contentAsString)
//		
//		assertTrue xml.error.text().contains('unknown')
//		
//		
//	}
}
