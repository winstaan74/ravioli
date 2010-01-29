class ResourceFunctionalTests extends FunctionalTestUtils {
	
	protected void setUp() {
		super.setUp()
		logout()
		loginAdmin();
		get('/')
	}
	
	protected void tearDown() {
		logout()
		super.tearDown()
	}
	
    void testResourcePage() {
		assertContentContains 'Resources'
		click 'Resources'
		assertContentContains 'List'
		assertContentContains 'Search'
		
		assertStatus 200
		
		// ok. in testing mode - only have 2 pages - 11 resources or so..
		click 'Next'
		assertStatus 200
    }
	
	
	void testResourceSearchPage() {
		assertContentContains 'Resources'
		click 'Resources'
		click 'Search'
		assertStatus 200
		
		assertContentDoesNotContain('No resources found')
		form {
			q = 'asca'
			search.click()
		}
		assertStatus 200
		// one of the expected search results.
		assertContentContains 'ASCA Proposals'
		click 'ASCA Proposals'
		assertStatus 200
		assertContentContains 'stc:STCResourceProfile'
		
		back()
		form {
			q = 'nothing'
			search.click()
		}
		assertStatus 200
		assertContentContains 'No resources found'
	}
}
