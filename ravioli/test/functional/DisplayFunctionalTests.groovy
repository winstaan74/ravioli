

class DisplayFunctionalTests extends FunctionalTestUtils {

	
    void testMainPage() {
        // Here call get(uri) or post(uri) to start the session
        // and then use the custom assertXXXX calls etc to check the response
        //
        // get('/something')
        // assertStatus 200
        // assertContentContains 'the expected text'
		get('/')
		assertStatus 200
		assertTitleContains "Registry Resources"
		
		// check we're not logged in, and there's a login link..
		assertContentDoesNotContain "Logged in"
		
		assertContentContains 'Login'
		assertContentContains 'Register'
		
		
    }
	
	// just can't seem to test ajax - give up. wah.
//	void testTableFunctionality() {
//
//		get('/')
//		prepareForAjax();
//		get('/')
//		synchronized(page) {
//			page.wait(2000)
//		}
//		page.webClient.waitForBackgroundJavaScript(10000)
//		assertStatus 200
//		
//		assertContentContains 'prev' // can't click these - dataset isn't big enough.
//		assertContentContains 'next'
//
//	//	assertContentContains '11 resources' // running with reduced query set.
//		assertContentContains 'Caltech NVO Registry'
//	//	assertContentContains 'ASCA Proposals'
//	//	click 'ASCA Proposals' // expand a row.
//	//	assertContentContains 'to obtain more detailed information'
//		
//		// todo search for an item	
//	}

}
