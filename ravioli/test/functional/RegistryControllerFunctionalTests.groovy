@Mixin(RavioliFunctionalAssert)
class RegistryControllerFunctionalTests extends functionaltestplugin.FunctionalTestCase {
	
	protected void setUp() {
		super.setUp()
		logout();
		loginAdmin();
		get('/')
	}
	
	protected void tearDown() {
		logout()
		super.tearDown();
	}
	
	void testRegistryPage() {

		acc 'Registries'
		click 'Registries'
		
		assertStatus 200
		
		acc 'List'
		acc 'harvest'
		acc 'update'
		acc 'reload'
		
		// can't trigger any of these, as they go to a back end server.
		
		acc 'Next'
		click 'Next'
		assertStatus 200
		
    }
	
	void testHarvestPage() {

		click 'Registries'
		click 'Harvest a registry'
		assertStatus 200
		
		acc 'Harvest from'
		
	}
}
