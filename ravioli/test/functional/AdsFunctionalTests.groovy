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
	
	void testGetUnknownAdsBiblio() {
		def bibcode = '1800ASPC..295..465P'
		get("/ads/${bibcode}")
		assertStatus 404
	}
	
	void testGetAdsBiblioAsHTML() {
		def bibcode = '2003ASPC..295..465P'
		get("/ads/${bibcode}")
		assertStatus 200
		assertContentType('text/html')
		// check the content -- verify some if the fields are there.
		acc 'XAssist'
		hasLink '(show abstract)'
		hasLink '(show authors)'
		
	}
}
