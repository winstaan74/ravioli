package org.ravioli

import grails.test.*

class RegistryUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConstraints() {
        def already = new Registry(ivorn:'ivo://foo.bar'
                    )
        mockForConstraintsTests(Registry,[already])

        def testReg = new Registry()
        assertFalse testReg.validate()
        assertEquals "nullable", testReg.errors["ivorn"]

        testReg.ivorn="foo.bar.choo"
        assertFalse testReg.validate()
        assertEquals "matches", testReg.errors["ivorn"]

        testReg.ivorn=already.ivorn
        assertFalse testReg.validate()
        assertEquals "unique", testReg.errors['ivorn']

        testReg.ivorn="ivo://a.new.registry/something"
        assertFalse testReg.validate()
        assertNull testReg.errors["ivorn"], testReg.errors['ivorn']


        testReg.endpoint = "fink"
        assertFalse testReg.validate()
        assertEquals "url", testReg.errors['endpoint']

        testReg.endpoint = 'http://www.foo.com/bar'
        assertFalse testReg.validate()
        assertNull testReg.errors['endpoint'], testReg.errors['endpoint']


        assertEquals "nullable", testReg.errors['name']
        testReg.name = "a name"
        assertFalse testReg.validate()
        assertNull testReg.errors['name']

        assertEquals "nullable", testReg.errors['manages']
        testReg.manages = []
        boolean result = testReg.validate()
        assertTrue testReg.errors.toString(),result


    }

    void testEndpointMangling() {
        Registry r= new Registry()
        String u = 'http://www.slashdot.org/foo/path.php'
        r.endpoint = (u + '?')
        assertEquals u, r.endpoint

        // same occurs with ctr too?
        Registry s = new Registry(endpoint: u + '?')
        assertEquals u, s.endpoint
		
		// unmangled pass through unchanged
		Registry t = new Registry()
		t.endpoint = u;
		assertEquals(u,t.endpoint)
    }
	
	void testConstructionFromMap() {
		// the following map items need to be provided for the map to be correct.
		def map = [name:'fred',
		           endpoint:'http://www.foo.com',
		           ivorn:'ivo://foo.bar.choo',
		           manages:[]
		]
		mockForConstraintsTests(Registry)		
		Registry r = new Registry(map)
		assertTrue r.validate()
	}


}
