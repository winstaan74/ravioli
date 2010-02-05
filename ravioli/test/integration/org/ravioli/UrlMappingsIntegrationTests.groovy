package org.ravioli

import grails.test.*

/** specialized test for url mappings */
class UrlMappingsIntegrationTests extends GrailsUrlMappingsTestCase {
	
	public void testMappings() {
		
		// test the default mapping behaviour - check my understanding of all this.
		assertUrlMapping('/registry/harvest/3',controller:'registry', action:'harvest') {
			id = 3
		}
		
		assertForwardUrlMapping(500, uri:'/error')
		
		assertUrlMapping('/',controller:'explore', action:'index')
		
	}
	
	public void testStubRegistryMappings() {
		def regId = 'ivo://uk.ac.le.star/org.astrogrid.registry.RegistryService'
		def encoded = URLEncoder.encode(regId)
		assertForwardUrlMapping("/stub-registry/${encoded}/?verb=Identify"
				, controller:"stubRegistry", action:'identify') {
				registryIvorn = encoded
				}
		
		assertForwardUrlMapping("/stub-registry/${encoded}/?verb=ListIdentifiers"
				, controller:"stubRegistry", action:'listidentifiers') {
					registryIvorn = encoded
				}
		
		def tok = 'resumptionThang'
		assertForwardUrlMapping("/stub-registry/${encoded}/?verb=ListIdentifiers&resumptionToken=${tok}"
				, controller:"stubRegistry", action:'listidentifiers') {
					registryIvorn = encoded
					resumptionToken = tok
				}
		
		def ivorn = 'ivo://uk.ac.le.star.tmpledas/ledas/ledas/ic348cxo'
		def ivo = URLEncoder.encode(ivorn) // needs to be encoded, else doesn't work.. arse.
		assertForwardUrlMapping("/stub-registry/${encoded}/?verb=GetRecord&identifier=${ivo}"
				, controller:"stubRegistry", action:'getrecord') {
					registryIvorn = encoded
					identifier = ivo
				}
	}
}
