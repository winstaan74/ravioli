package org.ravioli

import grails.test.*

class RegistryControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
		harvestControl = mockFor(HarvestService)
    }

    protected void tearDown() {
        super.tearDown()
    }
	
	void testNothing() {
	}
	
	def harvestControl
//	doesn't seem to work - wrong things get in scope.
//    void testUpdateRofr() {
//		
//		 harvestControl.demand.readRofr() { 
//			return new HarvestResults()
//		}
//
//		this.controller.harvestService = harvestControl.createMock()
//		
//    	def model = this.controller.updateRofr()
//		//controller.harvestService.readRofr()
//		harvestControl.verify()
//		assertNotNull this.controller.flash.message
//		assertEquals "list",this.controller.redirectArgs['action']
//		assertEquals 'resource',this.controller.redirectArgsp['controller']
//    }
}
