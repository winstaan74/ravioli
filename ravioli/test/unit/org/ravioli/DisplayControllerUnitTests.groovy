package org.ravioli

import grails.test.*
@Mixin(RavioliAssert)
class DisplayControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

	void testIndex() {
		this.controller.index()
		redirectsTo('resource')
	}
	void testResource() {
		Resource r1 = new Resource(ivorn:'ivo://foo.bar',titleField:'title a')
		r1.rxml.xml ='<foo />'
		Resource r2 = new Resource(ivorn:'ivo://bee.boo',titleField:'second title')
		mockDomain(Resource, [r1,r2])
		
		
		controller.params.id= r1.getId()
		
		def model = controller.resource()
		assertEquals r1, model.r
	}
	
	void testresourceUnknown() {
		Resource r1 = new Resource(ivorn:'ivo://foo.bar',titleField:'title a')
		r1.rxml.xml ='<foo />'
		Resource r2 = new Resource(ivorn:'ivo://bee.boo',titleField:'second title')
		mockDomain(Resource, [r1,r2])
		
		
		controller.params.id= 42
		
		def model = controller.resource()
		status 404
	}
	
	void testResourceIvorn() {
		Resource r1 = new Resource(ivorn:'ivo://foo.bar',titleField:'title a')
		r1.rxml.xml ='<foo />'
		Resource r2 = new Resource(ivorn:'ivo://bee.boo',titleField:'second title')
		mockDomain(Resource, [r1,r2])
		
		
		controller.params.ivorn= r1.ivorn
		
		def model = controller.resource()
		assertEquals r1, model.r
	}
	
	void testresourceUnknownIvorn() {
		Resource r1 = new Resource(ivorn:'ivo://foo.bar',titleField:'title a')
		r1.rxml.xml ='<foo />'
		Resource r2 = new Resource(ivorn:'ivo://bee.boo',titleField:'second title')
		mockDomain(Resource, [r1,r2])
		
		
		controller.params.ivorn= 'ivo://missing'
		
		def model = controller.resource()
		status 404
	}
	
	// test for tableMetadata of an existing resource is donew in exploreControllerIntegrationTest.
	
	// test for an unknown resource
	void testTableMetadataUnknown() {
		mockDomain(Resource) // no entries
		controller.params.id = 3
		def resp = controller.tableMetadata()
		status 404
	}
	// test for a resource that lacks table metadata
	void testTableMetadataNoTable() {
		Resource r = new Resource()
		r.rxml.xml = '<foo />'
		mockDomain(Resource,[r]) // no entries
		controller.params.id = r.id
		def resp = controller.tableMetadata()
		status 400
	}
}
