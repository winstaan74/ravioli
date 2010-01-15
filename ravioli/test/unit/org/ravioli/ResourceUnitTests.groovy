package org.ravioli;

import grails.test.*;

class ResourceUnitTests extends GrailsUnitTestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	void testConstraints() {
		
		def already = new Resource(ivorn:'ivo://foo.bar')
		mockForConstraintsTests(Resource,[already])
		def r = new Resource()
		assertFalse r.validate()
		
		assertEquals "nullable", r.errors["ivorn"]
		
		r.ivorn="foo.bar.choo"
		assertFalse r.validate()
		assertEquals "matches", r.errors["ivorn"]
		
		r.ivorn=already.ivorn
		assertFalse r.validate()
		assertEquals "unique", r.errors['ivorn']
		
		r.ivorn="ivo://a.new.registry/something"
		assertFalse r.validate()
		assertNull r.errors["ivorn"], r.errors['ivorn']	
		
		assertEquals "nullable",r.errors['xml']
		r.xml = "some xml. oh rly?"
		assertFalse r.validate()
		assertNull r.errors['xml']

		assertEquals('nullable',r.errors['created'])
		r.created = new Date()
		
		if (! r.validate()) {
			r.errors.each {println it}
		}
		assertTrue r.validate()
	}
	
	void testBuild() {
		mockForConstraintsTests(Resource)
		URL u = this.class.getResource("trimmedRegResource.xml")
		def ivorn = "ivo://ivoa.net/rofr"
		Resource r = Resource.buildResource(u.text, ivorn)
		assertNotNull r
		
		assertEquals(ivorn, r.ivorn)
		
		assertNotNull(r.xml)
		
		assertEquals('IVOA Registry of Registries',r.title)
		
		assertNotNull(r.created)
		
		assertNotNull(r.modified)
		assertTrue r.validate()
		
	}
	
	void testWhenParsedIvornDoesnMatchExpectedIvorn() {
		mockForConstraintsTests(Resource)
		URL u = this.class.getResource("trimmedRegResource.xml")
		def ivorn = "ivo://another.ivorn"
		shouldFailWithCause(IllegalArgumentException) {
			Resource.buildResource(u.text, ivorn)
		}
	}
	
	void testWhenNoIvornProvided() {
		mockForConstraintsTests(Resource)
		URL u = this.class.getResource("trimmedRegResource.xml")
		
		Resource r = Resource.buildResource(u.text)
		assertTrue r.validate();
		assertEquals('ivo://ivoa.net/rofr',r.ivorn);
	}
	
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
		Resource r = new Resource(xml:inputXML)
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
	
	//@todo add unit tests for some other resources that exercise different parts of the data model
	// eg waveband..
	// exercised in resource integration test, so maybe not so much of a problem.
	
	/** test the xpath-defined dynamic fields */
	void testXpath() {
		mockForConstraintsTests(Resource)
		URL u = this.class.getResource("trimmedRegResource.xml")
		
		Resource r = Resource.buildResource(u.text)
		assertTrue r.validate();
		
		assertEquals('RofR',r.xpath(Resource.SHORTNAME_XPATH))
		
		assertNull(r.xpath(Resource.SOURCE_XPATH))
		
		// 
		def descr = r.xpath(Resource.DESCRIPTION_XPATH)
		assertTrue(descr.trim().startsWith('This is a spec'))

		assertEquals("vg:Registry",r.xpath(Resource.RESOURCETYPE_XPATH))


		def subj = r.xpathList(r.SUBJECT_XPATH)
		assertNotNull subj
		assertEquals 2,subj.size()
		assertTrue subj.contains('virtual observatory')
		assertTrue subj.contains('another subject')
		subj.each {
			assertTrue (it instanceof String )
		}
		
		assertEquals(0,r.xpathList(r.WAVEBAND_XPATH).size())

		def cap = r.xpathList(r.CAPABILITY_XPATH)
		assertEquals 2, cap.size()
		assertTrue(cap.contains('vg:Harvest'))
		assertTrue(cap.contains('ivo://ivoa.net/std/Registry'))
		cap.each {
			assertTrue (it instanceof String)
		}
		
		def curation = r.xpathList(r.CURATION_XPATH) // check this along with subsets below.
		curation.each {
				assertTrue (it instanceof String)
		}
		
		def creator = r.xpathList(r.CREATOR_XPATH)
		assertEquals 1, creator.size()
		assertTrue creator.contains('Raymond Plante')
		assertTrue curation.contains('Raymond Plante')
		creator.each {
			assertTrue (it instanceof String) 
			
		}
		
		def publisher = r.xpathList(r.PUBLISHER_XPATH)
		assertEquals 2, publisher.size()
		assertTrue publisher.contains("ivo://ivoa.net/IVOA")
		assertTrue curation.contains("ivo://ivoa.net/IVOA")		
		
		assertTrue publisher*.trim().contains("International Virtual Observatory Alliance")
		assertTrue curation*.trim().contains("International Virtual Observatory Alliance")

		publisher.each {
			assertTrue(it instanceof String) 
		}
		
		
	}
	
}
