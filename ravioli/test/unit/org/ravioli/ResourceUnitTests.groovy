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
		


		assertEquals('nullable',r.errors['created'])
		r.created = new Date()
		
		assertEquals('nullable',r.errors['date'])
		r.date = r.created
		
		assertEquals('nullable',r.errors['status'])
		
		r.status = 'deleted' // still invalid..
		assertFalse r.validate()
		assertEquals('matches',r.errors['status'])

		r.status = 'active' //now valid.

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

		assertEquals 'active', r.status
		
		// new fields, as used in table..
		assertEquals '', r.source
		assertEquals 'RofR', r.shortname
		
		assertEquals '', r.wavebands
		assertEquals 'another subject, virtual observatory',r.subjects
		assertEquals 'International Virtual Observatory Alliance', r.publishers
		assertEquals 'Raymond Plante', r.creators
		assertTrue r.validate()
		
	}
	
	void testWhenParsedIvornDoesnMatchExpectedIvorn() {
		mockForConstraintsTests(Resource)
		URL u = this.class.getResource("trimmedRegResource.xml")
		def ivorn = "ivo://another.ivorn"
		shouldFailWithCause(IdentifyException) {
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

	void testDelegates() { // check the delegate functions are working.
		URL u = this.class.getResource("trimmedRegResource.xml")
		Resource r = Resource.buildResource(u.text)
		r.rxml.xmlService = new XmlService()
		// check propertyMissing is delegateing
		assertEquals r.rxml.description, r.description
		
		// check xpath
		def xp = '/node()/title'
		assertEquals r.rxml.xpath(xp),r.xpath(xp)
		
		// xpathList
		xp = '/node()/content/subject/text()'
		def a  = r.rxml.xpathList(xp)
		def b = r.xpathList(xp)
		assertEquals a,b
	}
	
	
	//we could add unit tests for some other resources that exercise different parts of the data model
	// eg waveband..
	// exercised in resource integration test, so maybe not so much of a problem.
	

	
}
