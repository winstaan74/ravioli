package org.ravioli

import grails.test.*

class IdGenServiceUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
		service = new IdGenService()
    }

    protected void tearDown() {
        super.tearDown()
		service = null;
    }
	
	def service
    void testNext() {
		def x = service.next()
		def y = service.next()
		def z = service.next()
		
		assertNotNull x
		assertNotNull y
		assertNotNull z
		
		assertFalse (x == y)
		assertFalse (y == z)
    }
}
