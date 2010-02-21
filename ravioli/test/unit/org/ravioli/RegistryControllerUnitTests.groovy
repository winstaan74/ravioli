package org.ravioli

import grails.test.*

@Mixin(RavioliAssert)
class RegistryControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
		harvestControl = mockFor(HarvestService)
    }

    protected void tearDown() {
        super.tearDown()
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
		mockDomain(RegistryHarvestTask,[])

		this.controller.params.harvestId = r.ivorn
		this.controller.params.incremental = false
		
		this.controller.harvest()

		def l = RegistryHarvestTask.list()
		assertEquals 1, l.size()

		def task = l[0]
		assertEquals false, task.incremental
		assertEquals r, task.reg
		
		flashContains(r.name)
		redirectsTo('list')
	}
	
	void testHarvestWithMissingIncremental() {
		def r = new Registry(ivorn:'ivo://foo.bar.choo',name:'a reg')
		mockDomain(Registry,[r])
		mockDomain(RegistryHarvestTask,[])
		
		this.controller.params.harvestId = r.ivorn
		
		this.controller.harvest()
		
		def l = RegistryHarvestTask.list()
		assertEquals 1, l.size()
		
		def task = l[0]
		assertEquals false, task.incremental
		assertEquals r, task.reg
		
		flashContains(r.name)
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
	
	void testHarvesAll() {
		def r = new Registry(ivorn:'ivo://foo.bar.choo',name:'a reg')
		def r1 = new Registry(ivorn:'ivo://wibble.nibble', name:'another reg')
		def rs = [r,r1]
		mockDomain(Registry,rs)
		mockDomain(RegistryHarvestTask,[])

		this.controller.params.incremental = false
		this.controller.harvestAll()
		
		def l = RegistryHarvestTask.list()
		assertEquals rs.size(), l.size()
		
		assertEquals rs, l*.reg
		assertTrue l.every{! it.incremental}
		
	}
	
	

}
