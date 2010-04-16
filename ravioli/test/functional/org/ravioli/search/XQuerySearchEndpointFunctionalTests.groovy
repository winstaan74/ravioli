package org.ravioli.search

import static IvoaEndpointConstants.*

import org.codehaus.groovy.grails.plugins.spring.ws.EndpointFunctionalTestCase
import org.springframework.ws.soap.client.SoapFaultClientException;

class XQuerySearchEndpointFunctionalTests extends EndpointFunctionalTestCase {
	
	void setUp(){
		super.setUp()
		webServiceTemplate.setDefaultUri(serviceURL)
	}
	
	void testXQueryIsUnsupported() {
		shouldFail(SoapFaultClientException) {
			def resp = withEndpointRequest(serviceURL) {
				XQuerySearch(xmlns:namespace) {
					xquery('ignored')
				}
			}
		}
	}
}