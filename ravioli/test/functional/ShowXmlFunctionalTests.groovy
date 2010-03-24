
@Mixin(RavioliFunctionalAssert)
class ShowXmlFunctionalTests extends functionaltestplugin.FunctionalTestCase {
	
	void testLinkToXml() {
		def r = displayResource ('ivo://obspm.fr/PLEINPOT_pipeline')
		hasLink 'Show XML'
		//click 'Show XML' // don't work - as opens in new tab. crap.


		// open it explicitly
		get "/display/resource/${r.id}?format=xml"
		
		assertContentType('application/xml')
		// dunno if we can get any further..
	}
}
