package org.ravioli

import grails.test.*

class ResourceControllerIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		controller = new ResourceController()
    }

    protected void tearDown() {
        super.tearDown()
    }

	
	def controller
	
	void testSearchNoParams() {
		controller.params.q = ""
		def model =controller.search()
		assertTrue model.isEmpty()
	}
	
	final static String GLIM = "ivo://wfau.roe.ac.uk/glimpse-dsa/wsa"
	void testSearch() {
		controller.params.q = "title:glimpse"
		def model = controller.search()
		assertFalse model.isEmpty()
		assertTrue model.containsKey('searchResult')
		assertEquals(1, model.searchResult.size())
		def r = model.searchResult[0]
		assertTrue(r instanceof Resource)
		assertEquals(GLIM,r.ivorn)
		assertNull model.searchError
	}
}
