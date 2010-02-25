import org.ravioli.Resource;

import com.gargoylesoftware.htmlunit.WebAssert;

/** test the formatting of resources */
@Mixin(RavioliFunctionalAssert)
class ResourceDisplayFunctionalTests extends functionaltestplugin.FunctionalTestCase{
	
	
	
	def displayResource(String ivorn) {
		Resource r = Resource.findByIvorn(ivorn)
		assertNotNull "Not Found ${ivorn}",r
		def id = r.id
		get("/explore/resource/${id}")
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
	
	/** resource 15 */
	void testCDS() {
		Resource r = displayResource ('ivo://CDS.VizieR/II/85')
		fieldContains('shortname',r.shortnameField)
		fieldContains 'resourcetype', 'CatalogService'
		fieldContains 'contenttype', 'Catalog'
		fieldContains 'subject', 'Spectrophotometry'
		fieldContains 'level', 'Research'
		fieldPresent 'created'
		fieldNotPresent 'modified'
		furtherInformation()
		// source should be present, and be a linked
		fieldContains 'sourcereference', '1980RMxAA...5...25J'
		hasLink '1980RMxAA...5...25J'
		
		fieldContains 'footprintservice','http://cdsarc.u-strasbg.fr'
		
		fieldContains 'wavebands', 'Optical'
		fieldContains 'accessrights', 'public'
		hasLink 'CDS'
		fieldContains 'creator', 'Johnson H.L.'
		fieldContains 'contributors', 'Francois Ochsenbein'
		fieldContains 'date', '1997-12-09T17:12:30'
		fieldContains 'version', '29-Apr-1997'
		fieldContains 'contact', '''CDS, Observatoire de Strasbourg, 11 rue de l'Universite, F-67000 Strasbourg, France'''
		hasLink 'question@simbad.u-strasbg.fr'
		
		// interfaces.
		hasLink 'Web Form'
		
		hasLink 'HTTP Web Service - Base URL'
		fieldContains 'querytype','GET'
		fieldContains 'resulttype','text/xml+votable'
		
		//@todo add table metadata diusplay, somehow.
	}
	
	/** test the SIAP capability formatting.
	 * resource number 65
	 */
	void testIRAS() {
		def r = displayResource( 'ivo://irsa.ipac/ISSA')
		furtherInformation()
		acc 'Image access service (SIAP)'
		fieldPresent 'created'
		fieldPresent 'modified'
		fieldContains 'servicetype', 'Atlas'
		fieldContains 'maximumfilesize','1008000'
		fieldContains 'maximumresultsreturned', '50'
		fieldContains 'maximumimageextent', '12.5,12.5'
		fieldContains 'maximumimagesize','500,500'
		fieldContains 'maximumquerysize','12.5,12.5'
		fieldContains 'publisher','NASA/IPAC'
		//@todo test fr the test query too.
		hasLink 'NASA/IPAC Infrared Science Archive'
		
		fieldContains 'wavebands', 'Infrared'
		
		// test interface - will expect this to change later.
		hasLink 'HTTP Web Service - Base URL'
		fieldContains 'querytype','GET'
		fieldContains 'resulttype','application/xml+votable'
		
		lacks 'Other Capabilities'
	}

	
	/** test a cone search service, and multiple capabilities, and cea service, and vosi.
	 * resource number 45
*/
	void testGlimpse() {
		def r = displayResource( 'ivo://wfau.roe.ac.uk/glimpse-dsa/wsa')
		furtherInformation()
		acc 'Catalog cone search service'
		
		// check we're seeing descriptions from two different capabilities..
		acc 'wsa, glimpse_hrc_inter: cone search'
		acc 'wsa, glimpse_mca_inter: cone search'
		fieldContains 'maximumsearchradius','1.5'
		fieldContains 'maximumresultsreturned','2000000'
		
		// we've got multiple cone capabilities - problem?
		
		
		acc 'Check service availability'
		
		acc 'Catalog cone search service'
		
		
		acc 'relationships'
		fieldContains 'service-for', 'ivo://wfau.roe.ac.uk' 
		
		
		acc 'Other Capabilities' // the thing that hides the unintesting bot
		
		// however, the uninteresting bit still seewms to be visible to the test
		// to continue test below.
//		// test for cea
		acc 'Remote application (CEA) service'
		acc 'Access to two applications' /// useless descrption
		def ivo = 'ivo://wfau.roe.ac.uk/glimpse-dsa/wsa/ceaApplication' 
		fieldContains 'providestasks',ivo
//			//want to check for presence of link here, but not easy too - as already have identical link

//		// try this - hack into the page, and check we've got 2 links with the above url.
		assertEquals 2, page.anchors.findAll{ it.asText() == ivo}.size()
//		
//		// test interface - will expect this to change later.
		hasLink 'HTTP Web Service - Full URL'
		fieldContains 'querytype','GET'
		fieldContains 'resulttype','application/xml'
		hasLink 'SOAP Web Service' // cea
		
		
		

	}
	
	/** rtesource number 5 */
	void testAscao() {
		def r= displayResource( 'ivo://nasa.heasarc/ascao')
		fieldContains 'shortname',r.shortnameField
		fieldContains 'resourcetype', 'CatalogService'
		fieldPresent 'created'
		fieldNotPresent 'modified'
		fieldContains 'contenttype', 'Archive'
		fieldContains 'subject', 'Proposed Target'
		fieldContains 'level', 'Research'
		furtherInformation()
		
		fieldContains 'creator', 'HEASARC'
		fieldContains 'publisher', 'NASA/GSFC'
		hasLink 'NASA/GSFC HEASARC'
		fieldContains 'contact', 'Michael Preciado'
		fieldContains 'wavebands', 'X-ray'

		acc 'Relationships'
		fieldContains 'service-for','NASA'
		hasLink 'NASA/GSFC Exploration of the Universe Division'
	}
	
	/** a registry, resource no 25 */
	void testCarnivore() {
		def r = displayResource ('ivo://nvo.caltech/registry')
		fieldContains 'shortname',r.shortnameField
		fieldContains 'resourcetype', 'Registry'
		fieldContains 'date', '2005-01-07'
		
		fieldContains 'creator', 'Dr Matthew J. Graham'
		hasLogo()
		fieldContains 'publisher', 'Caltech Center for'
		hasLink 'Caltech Center for'
		
		fieldContains 'contact', 'Dr Matthew J. Graham'
		fieldContains 'contact','CACR, Mail Code 158-79, Pasadena, CA 91125'
		hasLink 'mjg@cacr.caltech.edu'
		fieldContains 'contact', '626 395 8030'
		
		fieldContains 'subject', 'data repositories'
		fieldContains 'subject', 'digital libraries'
		
		furtherInformation()
		fieldContains 'level','Research'
		// crappy registry - not my problem.
		//lacks 'mirror-of' // as there's no resource info..

		fieldContains 'managesauthorities', 'nvo.caltech'
		
		acc 'Registry Harvest'
		hasLink 'SOAP Web Service'
		hasLink 'HTTP Web Service - URL'
		
		acc 'Registry Search'

		fieldContains 'extensionsearchsupport', 'full'
		fieldContains 'additionalprotocols', 'XQuery'

	}
	
	/** a publisching authority, 
	 * resource 85 
	 */
	void testCasu() {
		def r = displayResource ('ivo://uk.ac.cam.ast')
		fieldContains 'subject','Authority'
		fieldContains 'managesorganization','ivo://uk.ac.cam.ast/organisation' 
	}
	
	/** a cea application
	 * resource 35
	 */
	void testPleinpot() {
		def r = displayResource ('ivo://obspm.fr/PLEINPOT_pipeline')
		fieldContains 'publisher' , 'VO Paris'
		fieldContains 'creator', 'VO Paris'
		fieldContains 'contact', 'Pierre'
		fieldContains 'contact', 'vo.paris@obspm.fr'
		furtherInformation()
		fieldContains 'contenttype', 'Other'
		fieldContains 'level', 'Research'
		
		WebAssert.assertTextPresentInElement page, 'Remote Application', 'cea-application'
		fieldContains 'interfaces','pipeline'
		}
	 
}