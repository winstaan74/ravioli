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
	
	void testIndex() {
		this.controller.index()
		redirectsTo('list')
	}
    void testUpdateRofr() {
		
		 harvestControl.demand.readRofr() { incremental -> // number of args has to match the caller, else fails.
			assertTrue(incremental)
			return new HarvestResults(created:3,modified:42)
		}
				
		this.controller.harvestService = harvestControl.createMock()
    	def model = this.controller.updateRofr()
		harvestControl.verify()
		flashContains("42 modified")
		flashContains('3 new')
		redirectsTo('list')

    }
	
	void testReloadRofr() {
		
		harvestControl.demand.readRofr() { incremental -> // number of args has to match the caller, else fails.
			assertFalse(incremental)
			return new HarvestResults(created:3,modified:42)
		}
		
		this.controller.harvestService = harvestControl.createMock()
		def model = this.controller.reloadRofr()
		harvestControl.verify()
		flashContains("42 modified")
		flashContains('3 new')
		redirectsTo('list')
		
	}	
	
	// sample - not testing.
	
	void testHarvestNoParams() {
		def model = this.controller.harvest()
		assertEquals 0, model.size()
		
	}
	
	void testHarvestWithParams() {
		def r = new Registry(ivorn:'ivo://foo.bar.choo',name:'a reg')
		mockDomain(Registry,[r])

		this.controller.params.harvestId = r.ivorn
		this.controller.params.incremental = false

		harvestControl.demand.harvest() {reg, inc ->
			assertEquals(r,reg)
			assertFalse("incremental flag expected to be false",inc)
			return new HarvestResults(created:3,modified:42)
		}
		this.controller.harvestService = harvestControl.createMock()
		
		this.controller.harvest()

		harvestControl.verify()
		
		flashContains(r.name)
		flashContains("42 modified")
		flashContains('3 new')
		redirectsTo('list')
	}
	
	void testHarvestWithMissingIncremental() {// shold provide default value for incremental.
		def r = new Registry(ivorn:'ivo://foo.bar.choo',name:'a reg')
		mockDomain(Registry,[r])
		
		this.controller.params.harvestId = r.ivorn
		
		harvestControl.demand.harvest() {reg, inc ->
			assertEquals(r,reg)
			assertTrue('incremental flag expected to be true',inc)
			return new HarvestResults(created:3,modified:42)
		}
		this.controller.harvestService = harvestControl.createMock()
		
		this.controller.harvest()
		
		harvestControl.verify()
		
		flashContains(r.name)
		flashContains("42 modified")
		flashContains('3 new')
		redirectsTo('list')
	}
	
	void testHarvestWithUnknownRegistry() {
		def r = new Registry(ivorn:'ivo://foo.bar.choo',name:'a reg')
		mockDomain(Registry,[r])
		
		this.controller.params.harvestId = 'ivo://an.unknown.reg'
		this.controller.params.incremental = false
	
		def model = this.controller.harvest()
		
		flashContains(this.controller.params.harvestId)
		assertEquals(0,model.size())
	}
	
	
	
    private void redirectsTo(String action) {
		assertEquals action, this.controller.redirectArgs['action']
		assertNull  this.controller.redirectArgs['controller']
    }
	
	private void flashContains(String s) {
		assertNotNull this.controller.flash.message
		assertTrue this.controller.flash.message + "/=/" + s,this.controller.flash.message.contains(s)
	}
}
