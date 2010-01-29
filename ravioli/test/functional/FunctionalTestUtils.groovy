import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController
/** bunch of ravioli-specific utilities */

class FunctionalTestUtils extends functionaltestplugin.FunctionalTestCase{
	
	def login(user, pass) {
		redirectEnabled = true;
		// ok. now login as user, and see if we do any better 
		get('/login')
		def foo = form('signin') {
			username = user
			password = pass
			// can't find login button. - do it manually. ugh
		}
		def form = page.getFormByName('signin')
		def submit = form.getElementsByAttribute('button','type','submit')
		println submit.dump()
		submit[0].click()
		assertStatus 302
	}
	
	def loginUser() {
		login('user','useR123!')
	}
	
	def loginAdmin() {
		login('admin','admiN123!')
	}
	
	def logout() {
		get('/logout')
		
	}
	
	def prepareForAjax() {
		page.webClient.ajaxController = new NicelyResynchronizingAjaxController()
	}
}
