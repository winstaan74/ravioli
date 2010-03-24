import org.ravioli.Resource;

import com.gargoylesoftware.htmlunit.*;
/** bunch of ravioli-specific utilities */

/** mixin of additional abilities */
class RavioliFunctionalAssert {
	
	def login(user, pass) {
		setRedirectEnabled (true);
		// ok. now login as user, and see if we do any better 
		get('/login')
		def foo = form('signin') {
			username = user
			password = pass
			// can't find login button. - do it manually. ugh
		}
		def form = page.getFormByName('signin')
		def submit = form.getElementsByAttribute('button','type','submit')
	//	println submit.dump()
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
		setJavaScriptEnabled(true)
		client.ajaxController = new NicelyResynchronizingAjaxController()
	}
	
	/** assert content contains */
	def acc(String term) {
		assertContentContains term
	}
	
	/** assert a field with fieldId exists, and contains 
	 * a substring matching 'match'
	 * @param fieldId
	 * @param match
	 * @return
	 */
	def fieldContains(String fieldId, String match ) {
//		def el = fieldPresent(fieldId)
//		def txt = el.asText()
//		def matchText = txt.toLowerCase()
//		assertTrue ("Element content for ${el.nodeName} is '${txt}': does not contain '${match}'",
//			matchText.contains(match.toLowerCase()	))	
		def id = fieldId.startsWith ('field' )? fieldId : 'field_' + fieldId
		WebAssert.assertTextPresentInElement(page, match, id)
		
	}
	
	/** assert a field with the given id is present */
	def fieldPresent (String fieldId) {
		def id = fieldId.startsWith ('field' )? fieldId : 'field_' + fieldId
		WebAssert.assertElementPresent(page, id)
	}
	
	def fieldNotPresent(String fieldId) {
		def id = fieldId.startsWith ('field' )? fieldId : 'field_' + fieldId
		WebAssert.assertElementNotPresent(page, id)
	}
	
	
	/** assert content does not contain */
	def lacks(String term) {
		assertContentDoesNotContain(term)
	}
	
	def furtherInformation() {
		hasLink('Further Information')
	}
	
	def tableMetadata() {
		hasLink('Show Table Metadata')
	}
	
	def hasLink(String name) {
		WebAssert.assertLinkPresentWithText(page, name)
	}
	
	def hasLogo() {
		WebAssert.assertElementPresentByXPath(page, '''//img[@class='resource-logo']''')
	}	
	
	def displayResource(String ivorn) {
		Resource r = Resource.findByIvorn(ivorn)
		assertNotNull "Not Found ${ivorn}",r
		def id = r.id
		get("/display/resource/${id}")
		assertStatus 200
		// things which will always be here
		assertTitleContains(r.titleField)
		fieldContains('ivoa-id',ivorn)
		def description = r.rxml.createSlurper().content.description.text()
		if (description) {
			acc description[0..Math.min(description.size()-1,30)]
		}
		acc r.titleField
		return r
	}
	
}
