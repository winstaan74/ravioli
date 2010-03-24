package org.ravioli

import grails.test.*

@Mixin(RavioliAssert)
class ResourceControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
		
    }

    protected void tearDown() {
        super.tearDown()
    }
	

    void testSearchNoParams() {
    	this.controller.params.q = ""
		def model = this.controller.search()
		assertTrue model.isEmpty()
    }
	
	void testSearch() {
		Resource r = new Resource()
		this.controller.params.q = "observatory"
		
		def resourceControl = mockFor(Resource)
		resourceControl.demand.static.searchEvery() {s,p -> // it's a static - how do I do that?
			assertEquals(controller.params.q,s)
			return [r]
		}
		
		def model = this.controller.search()

		assertFalse model.isEmpty()
		assertTrue model.containsKey('searchResult')
		def sr = model.searchResult
		assertEquals(1,sr.size())
		assertSame(r,sr[0])
	}
	
	void testSearchWithTransformedQuery() {
		Resource r = new Resource()
		this.controller.params.q = "fred AND ivo://foo.bar.choo"
		
		def resourceControl = mockFor(Resource)
		resourceControl.demand.static.searchEvery() {s,p -> // it's a static - how do I do that?
			assertEquals('fred AND ivo\\://foo.bar.choo',s)
			return [r]
		}
		
		def model = this.controller.search()
		
		assertFalse model.isEmpty()
		assertTrue model.containsKey('searchResult')
		def sr = model.searchResult
		assertEquals(1,sr.size())
		assertSame(r,sr[0])
	}
	
	
	void testReindex() {
		mockDomain(ReindexTask)
		this.controller.reindex()
		def l = ReindexTask.list()
		assertNotNull l
		assertEquals 1, l.size()
		assertNotNull l[0]
		flashContains 'started'
		redirectsTo('list')
	}
	
}
