package org.ravioli

import grails.test.*

class SmartListUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testValidation() {
		mockDomain(ListContainer)
    	mockForConstraintsTests(SmartList)
		SmartList l =new SmartList()
		assertFalse l.validate()

		l.container = new ListContainer()
		
		assertFalse l.validate()

		l.title = 'a title'
		
		assertFalse l.validate()
		
		l.query = 'a query'
		
		if (! l.validate()) {
			l.errors.allErrors.each {
				println it
			}
		}
		assertTrue l.validate()
    }
}
