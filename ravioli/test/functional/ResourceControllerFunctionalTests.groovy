/** test for the resource controller */
@Mixin(RavioliFunctionalAssert)
class ResourceControllerFunctionalTests  extends functionaltestplugin.FunctionalTestCase{
	
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
		acc 'Resources'
		click 'Resources'
		acc 'List'
		acc 'Search'
		
		assertStatus 200
		
		// ok. in testing mode - only have 2 pages - 11 resources or so..
		click 'Next'
		assertStatus 200
    }
	
	//fails.	
	void testResourceSearchPage() {
		acc 'Resources'
		click 'Resources'
		click 'Search'
		assertStatus 200
		
		lacks('No resources found')
		form {
			q = 'asca'
			search.click()
		}
		assertStatus 200
		// one of the expected search results.
		acc 'ASCA Proposals'
		click 'ASCA Proposals'
		assertStatus 200
		acc 'X-ray'
		
		back()
		form {
			q = 'nothing'
			search.click()
		}
		assertStatus 200
		acc 'No resources found'
	}
}
