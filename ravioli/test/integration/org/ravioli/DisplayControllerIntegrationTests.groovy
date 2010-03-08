package org.ravioli

import grails.test.*
import grails.converters.*
import org.codehaus.groovy.grails.web.json.*;

class DisplayControllerIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
        controller = new DisplayController()
    }
    def controller
    protected void tearDown() {
        super.tearDown()
		controller = null
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
