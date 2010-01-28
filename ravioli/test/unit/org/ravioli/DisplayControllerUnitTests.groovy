package org.ravioli

import grails.test.*

class DisplayControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }


	void testTableData() {
		Resource r1 = new Resource(ivorn:'ivo://foo.bar',title:'title a')
		Resource r2 = new Resource(ivorn:'ivo://bee.boo',title:'second title')
		mockDomain(Resource, [r1,r2])
		def resourceControl = mockFor(Resource)
		resourceControl.demand.static.list() {m ->
			assertFalse m.max > controller.MAX_QUERY
			return [r1,r2]
		}
		controller.params.max=200 // spurious input.
		def model = controller.tableDataAsJSON()

		assertEquals 'text/json', controller.renderArgs.contentType
		
		// bug on mailing list about this - can't inspect json output in unit test - repeat as an integration test
		// but leave this unit test in place - at least it's exercising the code.
		def resp1 =  controller.response.contentAsString
	
	}
	
	void testTableDataSearch() {
		Resource r1 = new Resource(ivorn:'ivo://foo.bar',title:'title a')
		Resource r2 = new Resource(ivorn:'ivo://bee.boo',title:'second title')
		mockDomain(Resource, [r1,r2])
		controller.params.q = 'foo AND ivo://foo.bar.choo'
		controller.params.max = 200
		
		def resourceControl = mockFor(Resource)
		resourceControl.demand.static.search() { q, map ->
			assertEquals('foo AND //foo.bar.choo',q)
			assertNotNull(map)
			assertFalse map.max > controller.MAX_QUERY
			return [total:1,results:[r2]]
		}
		def model = controller.tableDataAsJSON()
		
		assertEquals 'text/json', controller.renderArgs.contentType
		
		// bug on mailing list about this - can't inspect json output in unit test - repeat as an integration test
		// but leave this unit test in place - at least it's exercising the code.
		def resp1 =  controller.response.contentAsString
		
	}
	
	void testResourceDetail() {
		Resource r1 = new Resource(ivorn:'ivo://foo.bar',title:'title a', xml:'<foo />')
		Resource r2 = new Resource(ivorn:'ivo://bee.boo',title:'second title')
		mockDomain(Resource, [r1,r2])

		
		controller.params.id= r1.getId()
		
		def model = controller.resourceDetail()
		assertEquals r1, model.r
		assertEquals 'resourceDetail', controller.renderArgs['template']
	}
	
	void testUnknownResourceDetail() {
		Resource r1 = new Resource(ivorn:'ivo://foo.bar',title:'title a', xml:'<foo />')
		Resource r2 = new Resource(ivorn:'ivo://bee.boo',title:'second title')
		mockDomain(Resource, [r1,r2])
		
		
		controller.params.id= 42
		
		def model = controller.resourceDetail()
		assertEquals 404, controller.renderArgs['status']
	}
	
}
