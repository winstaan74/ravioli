import grails.converters.JSON;

/** we've already unit tests the controller and it's error handling
 * capability. Here we're testing whether it really can return results from ads.
 * @author noel
 * 
 * this test requires network access, and thart ADS is up and runninf.
 *
 */
@Mixin(RavioliFunctionalAssert)
class AdsFunctionalTests extends functionaltestplugin.FunctionalTestCase {
    void testGetAdsBiblioAsJSON() {
		def bibcode = '2003ASPC..295..465P'
		get("/ads/${bibcode}?format=json")
		assertStatus 200
		assertContentType('application/json')
		// check the content -- verify some if the fields are there.
		def o = JSON.parse(page.getInputStream(), 'UTF-8')
		assertNotNull o
		assertTrue o.title?.startsWith('XAssist')	
    }
	
	void testGetUnknownAdsBiblioAsJSON() {
		def bibcode = '1800ASPC..295..465P'
		get("/ads/${bibcode}?format=json")
		assertStatus 404
	}
	
	void testGetAdsBiblioAsHTML() {
		def bibcode = '2003ASPC..295..465P'
		get("/ads/${bibcode}")
		assertStatus 200
		assertContentType('text/html')
		// check the content -- verify some if the fields are there.
		acc 'XAssist'
		
	}
	
	// in case of html, it returns a valid 200 response, still containing an err code.
	void testGetUnknownAdsBiblioAsHTML() {
		def bibcode = '1800ASPC..295..465P'
		get("/ads/${bibcode}")
		assertStatus 404
	}
// no workee - can never get javascript to work. rats.	
//	void testIntegrationWithResourceDisplay() {
//		prepareForAjax()
//		displayResource('ivo://CDS.VizieR/II/85')
//		def bibcode = '1980RMxAA...5...25J'
//		hasLink bibcode
//		click bibcode
//		client.waitForBackgroundJavaScript(5000)
//		Thread.sleep(5000)
//		// this one doesn't actially work, but we've got no other resource with a bibcode in the test set - oh well.
//		acc 'not found'
//		
//	}
}
