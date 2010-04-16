
package org.ravioli.search

import org.springframework.ws.soap.client.SoapFaultClientException;

import static IvoaEndpointConstants.*
import org.codehaus.groovy.grails.plugins.spring.ws.EndpointFunctionalTestCase

class GetResourceEndpointFunctionalTests extends EndpointFunctionalTestCase {
	
	
	void setUp(){
		super.setUp()
		webServiceTemplate.setDefaultUri(serviceURL)
	}
	
	void testGetResourceUnknown() {
		def ivo = 'ivo://notKnown'
		shouldFail(SoapFaultClientException) {
			def resp = withEndpointRequest(serviceURL) {
				GetResource(xmlns:namespace) {
					identifier(ivo)
				}
			}
		}
	}
	
	void testGetResourceKnown() {
		def ivo = 'ivo://wfau.roe.ac.uk/glimpse-dsa/wsa'
		def resp = withEndpointRequest(serviceURL) {
			GetResource(xmlns:namespace) {
				identifier(ivo)
			}
		}
		
		// this is correct, accoring to specs, but AG registry returns a 'ResolveResource' element instead. whu?
		
		assertEquals 'ResolveResponse', resp.name()
		assertFalse resp.Resource.isEmpty()
		assertEquals ivo, resp.Resource.identifier.text()
	}
}