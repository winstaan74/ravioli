/**
 * test that specific pages are protected at each permission level.
 */
class ProtectedPagesFunctionalTests extends FunctionalTestUtils {
	
	def adminOnly = ['/registry/list', '/resource/list','/buildInfo','/task/list']
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
		// go back to root- check we see the admin menu.
		get('/')
		assertContentContains 'logout'
		assertContentContains 'user'		
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
