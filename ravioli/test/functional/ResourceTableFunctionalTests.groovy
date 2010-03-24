import org.ravioli.Resource

import com.gargoylesoftware.htmlunit.WebAssert;
@Mixin(RavioliFunctionalAssert)
class ResourceTableFunctionalTests extends functionaltestplugin.FunctionalTestCase{

	
    void testMainPage() {
		get('/')
		assertStatus 200
		assertTitleContains "Registry Resources"
		
		// check we're not logged in, and there's a login link..
		lacks "Logged in"
		
		acc 'Login'
		acc 'Register'	
		
		
		// check that we've got the footer
		
		hasLink 'Changes'
		acc 'Samp'
    }
	
	//@write a test that requiests the JSON table update code.
	
//@todo test the ajax functionality. - columns dialogue, sorting, pagination, resource expansion
//
//	
//	// just can't seem to test ajax - give up. wah.
//	void testTableFunctionality() {
//
//	//	get('/')
//		prepareForAjax();
//		get('/')
////		synchronized(page) {
////			page.wait(2000)
////		}
//		client.waitForBackgroundJavaScript(10000)
//		assertStatus 200
//		
//		assertContentContains 'prev' // can't click these - dataset isn't big enough.
//		assertContentContains 'next'
//
//		assertContentContains '11 resources' // running with reduced query set.
//		assertContentContains 'Caltech NVO Registry'
//	//	assertContentContains 'ASCA Proposals'
//	//	click 'ASCA Proposals' // expand a row.
//	//	assertContentContains 'to obtain more detailed information'
//		
//		// todo search for an item	
//	}
//	
}
