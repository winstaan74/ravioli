import org.ravioli.Resource

import com.gargoylesoftware.htmlunit.WebAssert;

class ExploreFunctionalTests extends FunctionalTestUtils {

	
    void testMainPage() {
		get('/')
		assertStatus 200
		assertTitleContains "Registry Resources"
		
		// check we're not logged in, and there's a login link..
		assertContentDoesNotContain "Logged in"
		
		assertContentContains 'Login'
		assertContentContains 'Register'	
    }
	
	/** grab the resource detail url directly, to see how it's working.
	 * underlying taglibs have already been integration tested - just want to check there's no faults here, and that we're
	 * seeing some of the expected data 
	 */
	void testAscao() {
		def r= displayResource( 'ivo://nasa.heasarc/ascao')
		acc r.shortname
		acc 'CatalogService'
		acc 'Archive'
		acc 'Proposed Target'
		acc 'Research'
		furtherInformation()
		acc 'service-for'
		acc 'Relationships'
		hasLink('NASA/GSFC Exploration of the Universe Division')
		hasLink 'HEASARC'
		hasLink 'NASA/GSFC HEASARC'
		acc 'Michael Preciado'
		acc 'X-ray'
	}
	
	void testCDS() {
		def r = displayResource ('ivo://CDS.VizieR/II/85')
		acc r.shortname
		acc 'CatalogService'
		acc 'Catalog'
		acc 'Spectrophotometry'
		acc 'Research'
		furtherInformation()
		hasLink '1980RMxAA...5...25J'
		acc 'Footprint Service'
		acc 'Optical'
		acc 'public'
		hasLink 'CDS'
		acc 'Johnson H.L.'
		acc 'Francois Ochsenbein [CDS]'
		acc 'Date'
		acc 'Version'
		acc '29-Apr-1997'
		acc '''CDS, Observatoire de Strasbourg, 11 rue de l'Universite, F-67000 Strasbourg, France'''
		hasLink 'question@simbad.u-strasbg.fr'
	}
	
	void testCarnivore() {
		def r = displayResource ('ivo://nvo.caltech/registry')
		acc r.shortname
		acc 'Registry'
		acc 'data repositories'
		acc 'digital libraries'
		furtherInformation()
		// crappy registry - not my problem.
		//lacks 'mirror-of' // as there's no resource info..
		acc 'Manages Authorities'
		acc 'nvo.caltech'
		acc 'Dr Matthew J. Graham'
		hasLink 'Caltech Center for'
		acc '626 395 8030'
		acc 'CACR, Mail Code 158-79, Pasadena, CA 91125'
		hasLink 'mjg@cacr.caltech.edu'
		acc 'Research'
		acc 'Full'
		hasLogo()
	}
	
	def displayResource(String ivorn) {
		Resource r = Resource.findByIvorn(ivorn)
		assertNotNull "Not Found ${ivorn}",r
		def id = r.id
		get("/explore/inlineResource/${id}")
		assertStatus 200
		// things which will always be here
		acc ivorn
		acc r.description
		acc 'IVOA-ID'
		acc r.title
		acc 'Resource Type'
		return r
	}
	
	def acc(String term) {
		assertContentContains term
	}
	
	def lacks(String term) {
		assertContentDoesNotContain(term)
	}
	
	def furtherInformation(present=true) {
		WebAssert.assertLinkPresentWithText(page, 'Further Information...')
	}
	
	def hasLink(String name) {
		WebAssert.assertLinkPresentWithText(page, name)
	}
	
	def hasLogo() {
		WebAssert.assertElementPresentByXPath(page, '''//img[@class='creator-logo')''')
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
