package org.ravioli

import grails.test.*

class BookmarkListUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConstraints() {
    	mockForConstraintsTests(BookmarkList)
		BookmarkList l = new BookmarkList()
		
		assertFalse l.validate()

		l.container = new ResourceListBlock()

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
