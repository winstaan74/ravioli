
@Mixin(RavioliFunctionalAssert)
class ShowTableMetadataFunctionalTests extends functionaltestplugin.FunctionalTestCase {

	// can't follow link, as opens in new tab :(
	// 
	
		void testLinkToTableMetadata() {
			def r = displayResource( 'ivo://wfau.roe.ac.uk/glimpse-dsa/wsa')
			tableMetadata()

			get "/display/tableMetadata/${r.id}"
			assertStatus 200
			
			acc 'Table Metadata'
			acc 'GLIMPSE'
			
			assertTitleContains('Table Metadata')
			
			acc 'glimpse_hrc_inter'
		}
}
