package org.ravioli

import grails.test.*

class ResourceListBlockUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testConstraints() {
    	mockForConstraintsTests(ResourceListBlock)
		ResourceListBlock c = new ResourceListBlock()
		assertFalse c.validate()
		
		c.title ='a title'

	
		
		if (! c.validate()) {
			c.errors.allErrors.each {
				println it
			}
		}
		assertTrue c.validate()
		
		// only title required. - icon, lists are both optional.
    }
}
