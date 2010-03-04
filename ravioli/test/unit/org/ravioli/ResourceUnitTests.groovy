package org.ravioli;
import groovy.util.XmlSlurper;
import groovy.xml.StreamingMarkupBuilder;

import grails.test.*;

class ResourceUnitTests extends GrailsUnitTestCase {
	
	protected void setUp() throws Exception {
		super.setUp();
		def u = this.class.getResource("trimmedRegResource.xml")
		xml = u.text
	}
	
	def xml
	
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
		def ivorn = "ivo://ivoa.net/rofr"
	//	gp.namespace().each{println it}
		Resource r = Resource.buildResource(xml, ivorn)
		r.rxml.xmlService = new XmlService()
		assertNotNull r
		
		assertEquals(ivorn, r.ivorn)
		
		assertEquals('IVOA Registry of Registries',r.titleField)
		
		assertNotNull(r.created)
		
		assertNotNull(r.modified)

		assertEquals 'active', r.status
		
		// new fields, as used in table..
		assertEquals '', r.sourceField
		assertEquals 'RofR', r.shortnameField
		
		assertEquals '', r.wavebands
		assertEquals 'another subject, virtual observatory',r.subjects
		assertEquals 'International Virtual Observatory Alliance', r.publishers
		assertEquals 'Raymond Plante', r.creators
		assertTrue r.validate()
		
		// test some fields of the created rxml.
		assertNotNull(r.rxml)
		assertNotNull(r.rxml.createSlurper())
		assertNotNull r.rxml.description
	}

	void testWhenParsedIvornDoesnMatchExpectedIvorn() {
		mockForConstraintsTests(Resource)
		def ivorn = "ivo://another.ivorn"
		shouldFailWithCause(IdentifyException) {
			Resource.buildResource(xml, ivorn)
		}
	}
	
	void testWhenNoIvornProvided() {
		mockForConstraintsTests(Resource)
		
		Resource r = Resource.buildResource(xml)
		assertTrue r.validate();
		assertEquals('ivo://ivoa.net/rofr',r.ivorn);
	}

	
	/** test that TABLE_COLUMNS and tableRow gel */
	void testItemForEachColumn() {
		Resource r = Resource.buildResource(xml)
		r.id = 42 // necessary. so that we've mocked all the data correctly.
		def row = r.tableRow()
		assertEquals Resource.TABLE_COLUMNS.size(), row.size()
		//check each column has a key, label, and a corresponding value in the row.
		// and that any other column key conforms to a small set of known values.
		Resource.TABLE_COLUMNS.each {col ->
			assertTrue col.containsKey('key')
			assertTrue col.containsKey('label')
			col.each{ field ->
				assertTrue field.getKey() in ['key','label','width','hidden','sortable','formatter']
			}
			assertTrue row.containsKey(col.key)
			assertNotNull "row value of ${col.key} is null", row.get(col.key) // never null, though may be blank.
		}
	}
	
}
