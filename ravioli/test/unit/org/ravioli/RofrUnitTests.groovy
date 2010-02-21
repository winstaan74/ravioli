package org.ravioli

import grails.test.*

class RofrUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGetInstance() {
    	mockDomain(Rofr)
		def r = Rofr.getInstance()
		assertNotNull(r)
		
		// try getting it again
		def r1 = Rofr.getInstance()
		assertSame r, r1
		
		// verify that it's stored in db.
		assertEquals 1, Rofr.list().size()
		assertEquals r, Rofr.list()[0]
    }
}
