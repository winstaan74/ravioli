class RegistryFunctionalTests extends FunctionalTestUtils {
	
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

		assertContentContains 'Registries'
		click 'Registries'
		
		assertStatus 200
		
		assertContentContains 'List'
		assertContentContains 'harvest'
		assertContentContains 'update'
		assertContentContains 'reload'
		assertContentContains 'grab'
		
		// can't trigger any of these, as they go to a back end server.
		
		assertContentContains 'Next'
		click 'Next'
		assertStatus 200
		
    }
	
	void testHarvestPage() {

		click 'Registries'
		click 'Harvest a registry'
		assertStatus 200
		
		assertContentContains 'Harvest from'
		
	}
}
