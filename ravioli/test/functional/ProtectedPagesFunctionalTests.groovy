/**
 * test that specific pages are protected at each permission level.
 */
@Mixin(RavioliFunctionalAssert)
class ProtectedPagesFunctionalTests extends functionaltestplugin.FunctionalTestCase {
	
	def adminOnly = ['/registry/list', '/resource/list','/buildInfo','/task/list'
	                 ,'/taskExecution/list'
	                 ,'/resourceList/list'
	                 ,'/listContainer/list'
	                 ,'/tableViewer/list'
	                 ]
	def userOnly = []
	
	
	protected void setUp() {
		super.setUp();
		logout()
	}
	
	protected void tearDown() {
		logout()
		super.tearDown()
	}
	void testNotLoggedIn() {
		redirectEnabled = false
		
		(adminOnly + userOnly).each {
			println "getting ${it}"
			get it
			assertRedirectUrlContains '/login'
		}
	}
	
	void testUserLogin() {
		loginUser()
		
		adminOnly.each { // each should be inacessible.
			println "getting ${it}"
			get it
			assertStatus 403
		}
		userOnly.each { // thse should now be fine.
			get it
			assertStatus 200
		}
	}
	
	void testAdminLogin() {
		loginAdmin()

		(adminOnly + userOnly).each {
			get it
			assertStatus 200
		}
	}
	

}
