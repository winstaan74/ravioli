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
		
		assertEquals 'nullable', r.errors['resourcetype']
		r.resourcetype = 'Resource'
		assertFalse r.validate()
		assertNull r.errors['resourcetype']
		
		// the list-valued params must be non-null too.
		
		assertEquals 'nullable', r.errors['subject']
		r.subject = []
		assertFalse r.validate()
		assertNull r.errors['subject']
		
		assertEquals 'nullable', r.errors['waveband']
		r.waveband = []
		assertFalse r.validate()
		assertNull r.errors['waveband']
		
		assertEquals 'nullable', r.errors['capability']
		r.capability = []
		assertFalse r.validate()
		assertNull r.errors['capability']
		
		assertEquals 'nullable', r.errors['creator']
		r.creator = []
		assertFalse r.validate()
		assertNull r.errors['creator']
		
		assertEquals 'nullable', r.errors['publisher']
		r.publisher = []
		
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
		
		assertEquals('Registry',r.resourcetype)
		
		assertEquals('IVOA Registry of Registries',r.title)

		assertNull r.source
		
		assertNotNull(r.description)
		assertTrue(r.description.startsWith("This is a spec"))
		
		assertNotNull r.subject
		assertEquals 2,r.subject.size()
		assertTrue r.subject.contains('virtual observatory')
		assertTrue r.subject.contains('another subject')
		r.subject.each {
			assertTrue (it instanceof String )
		}
		
		assertNotNull r.waveband
		assertEquals 0, r.waveband.size()
		
		assertNotNull r.capability
		assertEquals 2, r.capability.size()
		assertTrue(r.capability.contains('Harvest'))
		assertTrue(r.capability.contains('ivo://ivoa.net/std/Registry'))
		r.capability.each {
			assertTrue (it instanceof String)
		}
		
		assertNotNull r.creator
		assertEquals 1, r.creator.size()
		assertTrue r.creator.contains('Raymond Plante')
		r.creator.each {
			assertTrue (it instanceof String)
			
			}
		
		assertNotNull r.publisher
		assertEquals 2, r.publisher.size()
		assertTrue r.publisher.contains("ivo://ivoa.net/IVOA")
		assertTrue r.publisher.contains("International Virtual Observatory Alliance")
		r.publisher.each {
			assertTrue(it instanceof String) 
		}
		
		
		assertTrue r.validate()
		
	}
	
	void testWhenParsedIvornDoesnMatchExpectedIvorn() {
	}
}
