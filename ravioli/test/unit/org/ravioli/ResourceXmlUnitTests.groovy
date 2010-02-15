package org.ravioli

import grails.test.*

class ResourceXmlUnitTests extends GrailsUnitTestCase {
	
	// no significant constraints now.
//	void testConstraints() {
//		mockForConstraintsTests(ResourceXml)
//		def r = new ResourceXml()
//		assertFalse r.validate()
//		
//		assertEquals "nullable",r.errors['xml']
//		r.xml = "some xml. oh rly?"
//		if (! r.validate()) {
//			r.errors.each {println it}
//		}
//		// depending how this test is run, we get vaiable results for vaidate()
//		// seems as if in some cases the 'resource' link has been synthesized, and in other
//		// cases has been not. work aroud this by just asserting that the r.xml field is now valid
//		r.validate()
//		assertNull r.errors['xml']
//	}
	
	void testStrippedXML() {
		def inputXML='''
			<element attr="attr-value">
			  Some content
				<p ix="another value">
				more content
			</p>
			<i>stuff</i>more words
			</element>
			'''
		def r = new ResourceXml(xml:inputXML)
		r.xmlService = new XmlService();
		String output = r.stripXML()
		assertNotNull output
		[
		"attr-value " // all data should be space delimited
		, " another value "
		, "more content"
		, " stuff "
		, " more words"
		].each { str ->
			assertTrue("Missing ${str}",output.contains(str))
		}
	}
	
	/** test the xpath-defined dynamic fields */
	void testPropertyMissing() {
		mockForConstraintsTests(ResourceXml)
		URL u = this.class.getResource("trimmedRegResource.xml")
		
		def r = new ResourceXml(xml:u.text)
		r.xmlService = new XmlService()
		
		assertEquals('RofR',r.shortname)
		
		assertNull(r.source)
		
		// 
		assertTrue(r.description.startsWith('This is a spec'))
		
		assertEquals("vg:Registry",r.resourcetype)
		
		
		def subj = r.subject
		assertNotNull subj
		assertEquals 2,subj.size()
		assertTrue subj.contains('virtual observatory')
		assertTrue subj.contains('another subject')
		subj.each {
			assertTrue (it instanceof String )
		}
		
		assertEquals(0,r.waveband.size())
		
		def cap = r.capability
		assertEquals 2, cap.size()
		assertTrue(cap.contains('vg:Harvest'))
		assertTrue(cap.contains('ivo://ivoa.net/std/Registry'))
		cap.each {
			assertTrue (it instanceof String)
		}
		
		def curation = r.curation 
		curation.each {
			assertTrue (it instanceof String)
		}
		
		def creator = r.creator
		assertEquals 1, creator.size()
		assertTrue creator.contains('Raymond Plante')
		assertTrue curation.contains('Raymond Plante')
		creator.each {
			assertTrue (it instanceof String) 
			
		}
		
		def publisher = r.publisher
		assertEquals 2, publisher.size()
		assertTrue publisher.contains("ivo://ivoa.net/IVOA")
		assertTrue curation.contains("ivo://ivoa.net/IVOA")		
		
		assertTrue publisher*.trim().contains("International Virtual Observatory Alliance")
		assertTrue curation*.trim().contains("International Virtual Observatory Alliance")
		
		publisher.each {
			assertTrue(it instanceof String) 
		}
		
		assertNotNull r.ucd
		assertTrue r.ucd.isEmpty()
		
		assertNotNull r.col
		assertTrue r.col.isEmpty()
		
		assertTrue r.name.contains(r.title)
		assertTrue r.name.contains(r.shortname)
		
		assertTrue r.level.isEmpty()
		
		assertEquals(["Registry"], r.contenttype)
		
	}
	
	void testUnknownPropertyMissing() {
		mockForConstraintsTests(ResourceXml)
		URL u = this.class.getResource("trimmedRegResource.xml")
		
		def r = new ResourceXml(xml:u.text)
		r.xmlService = new XmlService()
		
		shouldFail(MissingPropertyException){
			r.unknown
		}
		
	}
	
}
