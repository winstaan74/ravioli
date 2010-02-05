import java.net.URLEncoder;

class StubRegistryControllerFunctionalTests extends functionaltestplugin.FunctionalTestCase {
	
	def regId = 'ivo://uk.ac.le.star/org.astrogrid.registry.RegistryService'
	def encoded = regId.encodeAsMD5()
	
	
	void testIdentify() {
		get("/stub-registry/${encoded}/?verb=Identify")
		assertStatus 200
		assertContentType('text/xml')
		assertContentContains "<identifier>${regId}</identifier"
    }
	
	
	void testUnknownIdentify() {
		
		get("/stub-registry/${encoded}unknown/?verb=Identify")
		assertStatus 404
	}
	
	void testList() {
		
		get("/stub-registry/${encoded}/?verb=ListIdentifiers")
		assertStatus 200
		assertContentType('text/xml')
		def xml = new XmlSlurper().parseText(page.content)
		def ids = xml.ListIdentifiers.header.identifier*.text()
		assertNotNull(ids)
		assertTrue(ids.size() > 0)
		ids.each {
			it.startsWith('ivo://')
		}
	}
	
	void tesUnknowntList() {
		
		get("/stub-registry/${encoded}unknown/?verb=ListIdentifiers")
		assertStatus 404
	}
	
	
	void testResumptionList() {
		
		def token = '1265068515435:200:203: : :ivo_managed:ivo_vor'
		get("/stub-registry/${encoded}/?verb=ListIdentifiers&resumptionToken=${URLEncoder.encode(token)}")

		println page.content
		assertStatus 200
		assertContentType('text/xml')
		def xml = new XmlSlurper().parseText(page.content)
		def ids = xml.ListIdentifiers.header.identifier*.text()
		assertNotNull(ids)
		assertTrue(ids.size() > 0)
		ids.each {
			it.startsWith('ivo://')
		}
	}
	
	void testUnknownResumptionList() {
		
		def token = 'something else'
		get("/stub-registry/${encoded}/?verb=ListIdentifiers&resumptionToken=${URLEncoder.encode(token)}")
		assertStatus 404
	}
	
	
	void testGetRecord() {
		
		def ivorn = 'ivo://uk.ac.le.star.tmpledas/ledas/ledas/ic348cxo'
		get("/stub-registry/${encoded}/?verb=GetRecord&identifier=${URLEncoder.encode(ivorn)}") 
		// real web interface doesn't url encode this param,
		// however, if we don't, it's not possible to pass it - the web tester barfs.
		// maybe some url encoding of params is going on automatically, without me seeing?
		
		assertStatus 200
		assertContentType('text/xml')
		def xml = new XmlSlurper().parseText(page.content)
		
		assertEquals ivorn, xml.GetRecord.record.metadata.Resource.identifier.text()
	}

	
	void testUnknownGetRecord() {
		
		def ivorn = 'ivo://uk.ac.le.star.tmpledas/ledas/ledas/ic348cxo-unknown'
		get("/stub-registry/${encoded}/?verb=GetRecord&identifier=${URLEncoder.encode(ivorn)}")
		assertStatus 404
	}
	
}
