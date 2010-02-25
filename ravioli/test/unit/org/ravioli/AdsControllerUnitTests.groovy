
package org.ravioli
import grails.converters.JSON;
import grails.test.*
import org.codehaus.groovy.grails.plugins.codecs.URLCodec;

@Mixin(RavioliAssert)
class AdsControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
                                                                 
        loadCodec(URLCodec)
    }

    protected void tearDown() {
        super.tearDown()
    }
	
	
	
	def setEndpoint(val) {
		controller.metaClass.grailsApplication = [config:[ravioli:[ads:[endpoint:(val+'?')] ]]]
		
	}
	
	
	void testNoBibcode() {
		controller.index()
		status 400
		
		
	}
	
	void testUnknownBibcode() {
		controller.params.bib = 'not important'
		endpoint = this.class.getResource('ads-fail.xml').toString()
		
		controller.index()
		status 404
	}
	
	void testBibcodeJSON() {
		controller.params.bib = '1989ApJ...342L..71R'
		controller.request.format = 'json'
		endpoint = this.class.getResource('ads-ok.xml').toString()
		
		controller.index()
		// inspect the content.
		def o = JSON.parse(resp())
		// test the expected structure is there..
		assertEquals 'Gamma-ray observations of SN 1987A from Antarctica', o.title
		def l = o.authors
		assertTrue (l instanceof List)
		assertTrue l.contains('Eichhorn, G.')
		
		def links = o.links
		assertEquals 8,links.size()
		assertTrue (links instanceof Map)
		println links.inspect()
		println links.keySet()
		assertTrue 'references' in links.keySet()
//groovy bug - this fails		assertTrue links.containsKey('references')
		links.each { k,v ->
			assertNotNull k
			assertNotNull v
			assertTrue (v instanceof Map)
			assertNotNull v.name
			assertNotNull v.url
		}
		
		assertEquals 26,o.citations
		assertTrue o.abstract.startsWith ('Gamma-ray lines from the directio')
	}
	
	void testBibcodeHTML() {
		controller.params.bib = '1989ApJ...342L..71R'
		controller.request.format = 'html'
		endpoint = this.class.getResource('ads-ok.xml').toString()
		
		def model = controller.index()
		assertNotNull model
		assertNotNull model.rec
		assertEquals 'Gamma-ray observations of SN 1987A from Antarctica',model.rec.title.text()
		
		// check we're goong to the correct template.
		assertEquals 'ads', controller.renderArgs.template
		}
	
	
	void testServiceDown() { // simulate the service not being avaliable
		controller.params.bib = 'not important'
		endpoint = 'file://pretty/sure/this/doesnt/exist.txt'
		controller.index()
		status 500
	}
	
	void testService404() { // simulate the service returning malformed HTML in place of the reponse
		controller.params.bib = 'not important'
		endpoint = this.class.getResource('404.html').toString()
		controller.index()
		status 500
	}
	
	void testService500() { // simulate the service a text file in place of the xml
		controller.params.bib = 'not important'
		endpoint = this.class.getResource('500.txt').toString()
		controller.index()
		status 500
	}
}
