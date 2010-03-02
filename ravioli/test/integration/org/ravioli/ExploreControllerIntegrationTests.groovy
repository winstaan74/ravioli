package org.ravioli

import grails.test.*
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*;

class ExploreControllerIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
        controller = new ExploreController()
    }
    def controller
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
		
		checkResponse(o)

    }
	
def checkResponse(def o) {
		def keys = Resource.TABLE_COLUMNS.collect {it.key} + ['dataUrl']
		def r = o.results
		assertNotNull r
		assertEquals o.totalRecords, r.length()
		
		r.each { m ->
			keys.each { assertNotNull "key ${it} missing",m."${it}"}
			assertEquals keys.size(), m.size()
		}
	}
	
	void testTableDataAsJSONOddParams() {
		controller.params.max = 200
		controller.params.sort='titleField'
		def model = controller.tableDataAsJSON()
		def js =  controller.response.contentAsString
		//println js
		assertNotNull(js)
		// ok. now parse json back into groovy, and check it's structure.
		Map o = JSON.parse(js)
		//println o.dump()
		assertEquals(11,o.totalRecords)
		
		checkResponse(o)
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
		
		checkResponse(o)
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
		
		checkResponse(o)
		
	}
	// testing this as an integration test, else it's a pain to mock a resource with 
	// table metadata
	void testTableMetadata() {
		def ivo = 'ivo://wfau.roe.ac.uk/glimpse-dsa/wsa'
		Resource r = Resource.findByIvorn(ivo)
		assertNotNull(r)
		//ok. foud the correct resource.
		controller.params.id = r.id
		def model = controller.tableMetadata()
		assertNotNull model
		assertEquals r, model.r
		assertNotNull model.tableNames
		println model.tableNames
		assertEquals 2, model.tableNames.size()
		
	}
	
	void testTableMetadataAsJSON() {
		def ivo = 'ivo://wfau.roe.ac.uk/glimpse-dsa/wsa'
		Resource r = Resource.findByIvorn(ivo)
		assertNotNull(r)
		//ok. foud the correct resource.
		controller.params.id = r.id
		controller.params.table = 'glimpse_mca_inter'
		def model = controller.tableMetadataAsJSON()
		def js =  controller.response.contentAsString
		//println js
		assertNotNull(js)
		// ok. now parse json back into groovy, and check it's structure.
		Map o = JSON.parse(js)
		//println o.dump()
		
		assertNotNull o.totalRecords
		assertNotNull o.name
		assertNotNull o.description
		assertNotNull o.role
		
		def results = o.results
		assertNotNull(results)
		assertEquals o.totalRecords, results.size()
		assertEquals 63,o.totalRecords
		assertTrue (results instanceof List)
		results.each { row->
			assertNotNull row.col
			assertNotNull row.desc
			assertNotNull row.type
			assertNotNull row.unit
			assertNotNull row.ucd
		}
		
	}

}
