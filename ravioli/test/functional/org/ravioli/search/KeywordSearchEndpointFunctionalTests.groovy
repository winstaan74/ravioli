package org.ravioli.search


import static IvoaEndpointConstants.*
import org.codehaus.groovy.grails.plugins.spring.ws.EndpointFunctionalTestCase

class KeywordSearchEndpointFunctionalTests extends EndpointFunctionalTestCase {
	

    void setUp(){
      super.setUp()
      webServiceTemplate.setDefaultUri(serviceURL)
    }
    
    final static FUSE = "ivo://sdss.jhu/openskynode/fuse"
    final static GLIMPSE = 'ivo://wfau.roe.ac.uk/glimpse-dsa/wsa'
    final static CARNIVORE = 'ivo://nvo.caltech/registry'
    
    void testSingleTermSearchIds() {
    	def resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'Ultraviolet'
    			identifiersOnly true
    			
    		}
    	}

    	assertEquals 'SearchResponse', resp.name()
    	
    	def vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 1, vor.'@from'?.toInteger()
    	assertEquals 1, vor.'@numberReturned'?.toInteger()
    	assertFalse vor.'@more'.toBoolean()
    	
    	def ids = vor.identifier
    	
    	assertFalse ids.isEmpty()
    	assertEquals 1, ids.size()
    	assertEquals FUSE, ids.text()
    	
    }
    
    void testSingleTermSearchResource() {
    	def resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'Ultraviolet'
    		}
    	}

    	assertEquals 'SearchResponse', resp.name()
    	
    	def vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 1, vor.'@from'?.toInteger()
    	assertEquals 1, vor.'@numberReturned'?.toInteger()
    	assertFalse vor.'@more'.toBoolean()
    	
    	def res = vor.Resource
    	
    	assertFalse res.isEmpty()
    	assertEquals 1, res.size()
    	assertEquals FUSE, res.identifier.text()
    	
    }
    
    void testSearchLuceneMultipleIds() {
    	def resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'resourcetype:registry'
    			identifiersOnly true
    		}
    	}
    	assertEquals 'SearchResponse', resp.name()
    	
    	def vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 1, vor.'@from'?.toInteger()
    	assertEquals 2, vor.'@numberReturned'?.toInteger()
    	assertFalse vor.'@more'.toBoolean()
    	
    	def ivos = vor.identifier.list()*.text().sort()
    	assertEquals ([CARNIVORE,'ivo://org.ravioli/registry'].sort(),ivos)
    }
    
    
    void testSearchLuceneMultipleResources() {
    	def resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'created:[2008-1-1 TO 2008-12-30]'
    		}
    	}
    	assertEquals 'SearchResponse', resp.name()
    	
    	def vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 1, vor.'@from'?.toInteger()
    	assertEquals 6, vor.'@numberReturned'?.toInteger()
    	assertFalse vor.'@more'.toBoolean()
    	
    	def res = vor.Resource
    	
    	assertFalse res.isEmpty()
    	def ivos =  res.identifier.list()*.text()
    	assertTrue ivos.contains(GLIMPSE)
    	assertEquals 6, ivos.size()
    }
    
    void testSearchOR() {
    	def resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'glimpse carnivore'
    			identifiersOnly true
    			orValues true
    		}
    	}
    	assertEquals 'SearchResponse', resp.name()
    	
    	def vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 1, vor.'@from'?.toInteger()
    	assertEquals 2, vor.'@numberReturned'?.toInteger()
    	assertFalse vor.'@more'.toBoolean()
    	
    	def ids = vor.identifier.list()*.text().sort()
    	assertEquals ([GLIMPSE, CARNIVORE].sort(), ids)
    }
    
    void testSearchAND() {
    	def resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'glimpse carnivore'
    			identifiersOnly true
    		}
    	}
    	assertEquals 'SearchResponse', resp.name()
    	
    	def vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 1, vor.'@from'?.toInteger()
    	assertEquals 0, vor.'@numberReturned'?.toInteger()
    	assertFalse vor.'@more'.toBoolean()
    	
    	def ids = vor.identifier
    	
    	assertTrue ids.isEmpty()
    }
    
    
    void testFromTo() {
    	def resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'publisher:*'
    			identifiersOnly true
    		}
    	}
    	
    	def vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 1, vor.'@from'?.toInteger()
    	assertEquals 12, vor.'@numberReturned'?.toInteger()
    	assertFalse vor.'@more'.toBoolean()
    	
    	def ixs = vor.identifier.list()*.text()
    	
    	// ok. now see if we can get these values 'incrementally'
    	resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'publisher:*'
    			identifiersOnly true
    			from 1
    			max 5
    		}
    	}
    	
    	vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 1, vor.'@from'?.toInteger()
    	assertEquals 5, vor.'@numberReturned'?.toInteger()
    	assertTrue vor.'@more'.toBoolean()
    	
    	def partials = vor.identifier.list()*.text()
    	
    	// get the next segment - tests a different 'from' value
    	resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'publisher:*'
    			identifiersOnly true
    			from 6
    			max 5
    		}
    	}
    	
    	vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 6, vor.'@from'?.toInteger()
    	assertEquals 5, vor.'@numberReturned'?.toInteger()
    	assertTrue vor.'@more'.toBoolean()
    	
    	partials += vor.identifier.list()*.text()
    	
    	// now get the last segment - tests when ouor 'to' overruns the available.
    	resp = withEndpointRequest(serviceURL) {
    		KeywordSearch(xmlns:namespace) {
    			keywords 'publisher:*'
    			identifiersOnly true
    			from 11
    			max 5
    		}
    	}
    	
    	vor = resp.VOResources
    	assertFalse vor.isEmpty()
    	assertEquals 11, vor.'@from'?.toInteger()
    	assertEquals 2, vor.'@numberReturned'?.toInteger()
    	assertFalse vor.'@more'.toBoolean()
    	
    	partials += vor.identifier.list()*.text()
    	
    	//now verify we've seen the same ivorns, in the same order, in both cases
    	assertEquals ixs, partials
    }
    
    
    

    
}