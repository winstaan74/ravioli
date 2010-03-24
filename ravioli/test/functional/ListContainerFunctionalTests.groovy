

@Mixin(RavioliFunctionalAssert)
class ListContainerFunctionalTests extends functionaltestplugin.FunctionalTestCase {
    void testSomeWebsiteFeature() {
    	get '/'
    	assertStatus 200
    	
    	acc 'Browse'
    	
    	hasLink 'All VizieR'
    	
    	acc 'Sample Queries'
    	
    	hasLink 'VO taster list'
    	
    	
    	// try clicking a few.
    	click 'All VizieR'
    	
    	// but, seeing we can't inspect the table, we can't check much..
    	
    	// maybe we can check that the search field is being changed, at least.
    	def val = byId('sb').value
    	assertNotNull val
    	assertEquals 'identifier:CDS.VizieR', val
    }
}
