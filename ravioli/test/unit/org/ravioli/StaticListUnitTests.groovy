package org.ravioli

import grails.test.*

class StaticListUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConstraints() {
    	mockForConstraintsTests(StaticList)
		StaticList l = new StaticList()
		
		assertFalse l.validate()

		l.container = new ListContainer()

    	assertFalse l.validate()
		
		l.title = 'a title'
		
		if (! l.validate()) {
			l.errors.allErrors.each {
				println it
			}
		}
		assertTrue l.validate()
    }
}
