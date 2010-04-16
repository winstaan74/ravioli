package org.ravioli.search
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import static IvoaEndpointConstants.*



import org.codehaus.groovy.grails.plugins.spring.ws.EndpointFunctionalTestCase

class GetIdentityEndpointFunctionalTests extends EndpointFunctionalTestCase {
	


    void setUp(){
      super.setUp()
      webServiceTemplate.setDefaultUri(serviceURL)
    }
    
    void testGetIdentity() {
    	def myIvo = ConfigurationHolder.config.ravioli.registration.ivorn
       def resp = withEndpointRequest(serviceURL) {
    	   GetIdentity(xmlns:namespace)
       }
       // this is correct, accoring to specs, but AG registry returns a 'ResolveResource' element instead. whu?
       assertEquals "ResolveResponse" ,resp.name()
       assertFalse resp.Resource.isEmpty()
       assertEquals myIvo, resp.Resource.identifier.text()?.trim()
       
    }
}