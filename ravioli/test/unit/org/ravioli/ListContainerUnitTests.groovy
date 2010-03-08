package org.ravioli

import grails.test.*

class ListContainerUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConstraints() {
    	mockForConstraintsTests(ListContainer)
		ListContainer c = new ListContainer()
		assertFalse c.validate()
		
		c.title ='a title'
		
		assertFalse c.validate()

		c.name = 'myname'
	
		
		if (! c.validate()) {
			c.errors.allErrors.each {
				println it
			}
		}
		assertTrue c.validate()
		
		// only title required. - icon, lists are both optional.
    }
}
