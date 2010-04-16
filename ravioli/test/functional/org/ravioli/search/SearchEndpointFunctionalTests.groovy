

package org.ravioli.search
import org.springframework.ws.soap.client.SoapFaultClientException;
import static IvoaEndpointConstants.*

import org.codehaus.groovy.grails.plugins.spring.ws.EndpointFunctionalTestCase

class SearchEndpointFunctionalTests extends EndpointFunctionalTestCase {
	
	void setUp(){
		super.setUp()
		webServiceTemplate.setDefaultUri(serviceURL)
	}
	
	/** all the implementation of 'from', 'max' parameters, and the results
	 * of such are shared with KeywordSearch, and tested there.
	 * 
	 * Here, just test that the translation from xqdql to lucen string is working
	 * and that we get some results out.
	 */
	void testSearchLike() {
		def resp = withEndpointRequest(serviceURL) {
			Search(xmlns:namespace,'xmlns:xsi':"http://www.w3.org/2001/XMLSchema-instance") {
				Where (xmlns:'http://www.ivoa.net/xml/ADQL/v1.0','xmlns:adql':'http://www.ivoa.net/xml/ADQL/v1.0') {
					Condition('xsi:type':'adql:likePredType') {
						Arg('xsi:type':'adql:columnReferenceType', xpathName:'title')
						Pattern('xsi:type':'adql:atomType'){
							Literal(Value:'%glim%','xsi:type':'adql:stringType') 
						}
					}
				}
			}
		}
		assertEquals 'SearchResponse', resp.name()
		
		def vor = resp.VOResources
		assertFalse vor.isEmpty()
		assertEquals 1, vor.'@from'?.toInteger()
		assertEquals 1, vor.'@numberReturned'?.toInteger()
		assertFalse vor.'@more'.toBoolean()	
		
		// check the resources..
		
		def ids = vor.Resource.identifier.list()*.text()
		assertEquals( [GLIM],ids)
	}
	
	final static String GLIM = "ivo://wfau.roe.ac.uk/glimpse-dsa/wsa"
	
	void testSearchEquals() {
		def resp = withEndpointRequest(serviceURL) {
			Search(xmlns:namespace,'xmlns:xsi':"http://www.w3.org/2001/XMLSchema-instance") {
				Where (xmlns:'http://www.ivoa.net/xml/ADQL/v1.0','xmlns:adql':'http://www.ivoa.net/xml/ADQL/v1.0') {
					Condition('xsi:type':'adql:comparisonPredType', Comparison:'=') {
						Arg('xsi:type':'adql:columnReferenceType', xpathName:'coverage/waveband')
						Arg('xsi:type':'adql:atomType') {
							Literal(Value:'x-ray','xsi:type':'adql:stringType') 
						}
					}
				}
			}
		}
		assertEquals 'SearchResponse', resp.name()
		
		def vor = resp.VOResources
		assertFalse vor.isEmpty()
		assertEquals 1, vor.'@from'?.toInteger()
		assertEquals 1, vor.'@numberReturned'?.toInteger()
		assertFalse vor.'@more'.toBoolean()	
		
		// check the resources..
		
		def ids = vor.Resource.identifier.list()*.text()
		assertEquals( ['ivo://nasa.heasarc/ascao'],ids)
	}
	
	void testSearchInvaldAdql() {
		shouldFail(SoapFaultClientException) {
		def resp = withEndpointRequest(serviceURL) {
			Search(xmlns:namespace,'xmlns:xsi':"http://www.w3.org/2001/XMLSchema-instance") {
				Where (xmlns:'http://www.ivoa.net/xml/ADQL/v1.0','xmlns:adql':'http://www.ivoa.net/xml/ADQL/v1.0') {
					Condition('xsi:type':'adql:betweenPredType', Comparison:'=') {
						// just bobbins here. main point is that the Condition/@xsi:type is unrecognized.
						Arg('xsi:type':'adql:columnReferenceType', xpathName:'coverage/waveband')
						Arg('xsi:type':'adql:atomType') {
							Literal(Value:'x-ray','xsi:type':'adql:stringType') 
						}
					}
				}
			}
		}
		}
	}
	
}