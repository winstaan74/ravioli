package org.ravioli

import grails.test.*
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*;

class ExploreControllerIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }
    def controller = new ExploreController()
    protected void tearDown() {
        super.tearDown()
		controller = null
    }

    void testTableDataAsJSON() {
    	def model = controller.tableDataAsJSON()
		def js =  controller.response.contentAsString
		//println js
		assertNotNull(js)
		// ok. now parse json back into groovy, and check it's structure.
		Map o = JSON.parse(js)
		//println o.dump()
		assertEquals(11,o.totalRecords)
		
		def r = o.results
		assertNotNull r
		assertEquals o.totalRecords, r.length()
		
		r.each { m ->
			assertNotNull m.ivorn
			assertNotNull m.title
			assertNotNull m.created
			assertNotNull m.modified
			assertNotNull m.dataUrl
		}
		
    }
	
	void testTableDataAsJSONOddParams() {
		controller.params.max = 200
		controller.params.sort='title'
		def model = controller.tableDataAsJSON()
		def js =  controller.response.contentAsString
		//println js
		assertNotNull(js)
		// ok. now parse json back into groovy, and check it's structure.
		Map o = JSON.parse(js)
		//println o.dump()
		assertEquals(11,o.totalRecords)
		
		def r = o.results
		assertNotNull r
		assertEquals o.totalRecords, r.length()
		
		r.each { m ->
			assertNotNull m.ivorn
			assertNotNull m.title
			assertNotNull m.created
			assertNotNull m.modified
			assertNotNull m.dataUrl
		}
		
	}
	
	void testTableDataAsJSONSearch() {
		controller.params.q = 'glimpse'
		def model = controller.tableDataAsJSON()
		def js =  controller.response.contentAsString
		//println js
		assertNotNull(js)
		// ok. now parse json back into groovy, and check it's structure.
		Map o = JSON.parse(js)
		//println o.dump()
		assertEquals(1,o.totalRecords)
		
		def r = o.results
		assertNotNull r
		assertEquals o.totalRecords, r.length()
		
		r.each { m ->
			assertNotNull m.ivorn
			assertNotNull m.title
			assertNotNull m.created
			assertNotNull m.modified
			assertNotNull m.dataUrl
		}
		
	}
	
	void testTableDataAsJSONSearchParams() {
		controller.params.q = 'glimpse'
		controller.params.max=200
		controller.params.sort='modified'
		def model = controller.tableDataAsJSON()
		def js =  controller.response.contentAsString
		//println js
		assertNotNull(js)
		// ok. now parse json back into groovy, and check it's structure.
		Map o = JSON.parse(js)
		//println o.dump()
		assertEquals(1,o.totalRecords)
		
		def r = o.results
		assertNotNull r
		assertEquals o.totalRecords, r.length()
		
		r.each { m ->
			assertNotNull m.ivorn
			assertNotNull m.title
			assertNotNull m.created
			assertNotNull m.modified
			assertNotNull m.dataUrl
		}
		
	}
	
	void testInlineResource() {
		//@
	}
}
