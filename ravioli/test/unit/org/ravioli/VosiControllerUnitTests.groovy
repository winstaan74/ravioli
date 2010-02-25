
package org.ravioli

import grails.test.*
/** was an integration test, but the render() method in controller doesn't 
 * seem to work correctly at unit stage.
 */

@Mixin(RavioliAssert)
class VosiControllerUnitTests  extends ControllerUnitTestCase  {
    protected void setUp() {
        super.setUp()
    }
	

    protected void tearDown() {
        super.tearDown()
    }
	
	void testNoParam() {
		controller.index()
		status 400
	}

    void testAvailable() {
		def u = this.class.getResource('vosi-ok.xml').toString()
		assertNotNull u
		controller.params.u = u
		controller.index()
		contains( 'Available')
		contains('uptime')
		notContains ('Unavailable')

    }
	
	void testAvailableUpSince() {
		def u = this.class.getResource('vosi-upsince.xml').toString()
		assertNotNull u
		controller.params.u = u
		controller.index()
		contains( 'Available')
		contains('since')
		notContains ('Unavailable')
		
	}
	
	void testUnavailable() {
		def u = this.class.getResource('vosi-fail.xml').toString()
		assertNotNull u
		controller.params.u = u
		controller.index()
		notContains( 'Available')
		contains ('Unavailable')
	}
	
	void testError() {
	
		controller.params.u = 'file://pretty/sure/this/doesnt/exist.txt'
		controller.index()
		contains('Unable to contact service')
	}
	
	void test404HTML() {
		def u = this.class.getResource('404.html').toString()
		assertNotNull u
		controller.params.u = u
		controller.index()
		contains('Unable to contact service')
	}
	
	void test505Text() { // test what happens when a '505 not found' html message is returned, in stead of the XML doc
		def u = this.class.getResource('500.txt').toString()
		assertNotNull u
		controller.params.u = u
		controller.index()
		contains('Unable to contact service')
		}

}
