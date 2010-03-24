import grails.converters.JSON;

/** we've already unit tested the resolver service. here we're just testing that
 * we can acutally get some data back from sesame.
 * 
 * this test requires internet acces, and for the CDS server to be up.
 */
@Mixin(RavioliFunctionalAssert)
class SesameFunctionalTests extends functionaltestplugin.FunctionalTestCase {
	void testResolve() {
		get('/sesame/?obj=m42')
		assertStatus 200
		assertContentType('application/json')
		// check the content - its' JSON.
		def o = JSON.parse(page.getInputStream(), 'UTF-8')
		assertNotNull o
	//doesn't work	assertTrue o.containsKey('ra')	
		assertNotNull o.ra
		assertEquals 83.8186621, o.ra
		assertNotNull o.dec
	}
	
	void testResolveUnknown() {
		get('/sesame/?obj=resolve-unknown')
		assertStatus 404
	}
}
