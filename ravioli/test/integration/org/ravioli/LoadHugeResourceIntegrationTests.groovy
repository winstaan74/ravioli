package org.ravioli

import grails.test.*
/** tests for what happens when huge resources are harvested */
class LoadHugeResourceIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }
	
	def harvestService
	
    void testLoadLargeWAFAU() {
		def expected = 'ivo://wfau.roe.ac.uk/sdssdr3-dsa/dsa'
		def u = this.class.getResource('largeResource.xml')
		assertNotNull u
		Resource r= Resource.buildResource(u.text)
		assertNotNull r
		//assertTrue (r.rxml instanceof ResourceCompressedXml)
		assertEquals expected, r.ivorn
		if (! r.validate()) {
			r.errors.each {
				println it
			}
		}
		assertTrue r.validate()
		r.save(flush:true)
		
		// maybe it's retrieving it that's the problem??
		Resource r1 = Resource.findByIvorn(expected)
		assertNotNull r1
		assertEquals expected, r1.ivorn
		def slurp = r1.rxml.createSlurper()
		assertNotNull r1.rxml.description
		assertNotNull slurp
		
    }
	
	
//	void testLoadLargeWAFAUXmlOnly() {
//		def expected = 'ivo://wfau.roe.ac.uk/sdssdr3-dsa/dsa'
//		def u = this.class.getResource('largeResource.xml')
//		assertNotNull u
//		Resource r= Resource.buildResource(u.text)
//		assertNotNull r
//		assertTrue (r.rxml instanceof ResourceCompressedXml)
//		if (! r.rxml.validate()) {
//			r.rxml.errors.each {
//				println it
//			}
//		}
//		assertTrue r.rxml.validate()
//		r.rxml.save(flush:true)
//		
//	}
}
