package org.ravioli
import groovy.util.XmlSlurper;

import grails.test.*

class ResourceTagLibIntegrationTests extends GroovyPagesTestCase {


    void testResourcetype() {
		// first part of template just injects value into page context.
		def template = '''<g:set var='xml' value="${xml}" scope='page'/><r:resourcetype/>'''
		// use a map to stub for a resource.
		def xml = new XmlSlurper().parseText('<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="cs:ConeSearch"/>')
		assertOutputEquals 'ConeSearch', template, [xml:xml]
		
		xml = new XmlSlurper().parseText('<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ConeSearch"/>')
		assertOutputEquals 'ConeSearch', template, [xml:xml]
		
		xml = new XmlSlurper().parseText('<resource />')
		assertOutputEquals 'unspecified', template, [xml:xml]
		
		xml = new XmlSlurper().parseText('<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="CeaApplication"/>')
		assertOutputEquals 'Remote Application (CEA)', template, [xml:xml]
		
		xml = new XmlSlurper().parseText('<resource xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="CeaHttpApplication"/>')
		assertOutputEquals 'Remote Application (CEA)', template,[xml:xml]
    }
	
	void testCreated() {
		def template = '''<g:set var='r' value="${r}" scope='page'/><r:created/>'''
		def date = new Date()
		assertOutputEquals date.format('yyyy-MM-dd'), template, [r:[created:date]]
		assertOutputEquals '', template, [r:[created:null]]
	}
	
	void testModifed() {
		def template = '''<g:set var='r' value="${r}" scope='page'/><r:modified/>'''
		def date = new Date()
		assertOutputEquals date.format('yyyy-MM-dd'), template, [r:[modified:date]]
		assertOutputEquals '', template, [r:[modified:null]]
	}
	
	void testSource() {
		def template = '''<g:set var='r' value="${r}" scope='page'/><g:set var='xml' value="${xml}" scope='page'/><r:source/>'''
		
		def empty = new XmlSlurper().parseText('<resource />')
		assertOutputEquals '', template, [r:[sourceField:null],xml:empty] // no source.
		assertOutputEquals 'anyold', template, [r:[sourceField:'anyold'],xml:empty] // arbitrary kind of source
		
		def bibcode = new XmlSlurper().parseText('<resource><content><source format="bibcode"/></content></resource>')
		def result = applyTemplate(template, [r:[sourceField:'anyold'],xml:bibcode] )// bibcode marked as such, even tho it's not looking like one
		// just check that we've taken the correct branch of the source taglib
		// not that all the fromatting is correct.
		assertTrue result.contains('spinner')


		def urlOutput = '''<a href="http://www.slashdot.org">http://www.slashdot.org</a>'''
		assertOutputEquals urlOutput, template, [r:[sourceField:'http://www.slashdot.org'],xml:empty] // url instead of bibcode
		assertOutputEquals urlOutput, template, [r:[sourceField:'http://www.slashdot.org'],xml:bibcode] // url instead of bibcode - sourceFormat is ignored		
		
		def bc = '1974AJ.....79..819H'
		assertTrue new ResourceTagLib().looksLikeBibcode(bc)
		
		result = applyTemplate (template, [r:[sourceField:bc],xml:empty]) // bibcode not marked as such - should recognize by it's format.
		assertTrue result.contains('spinner')
		}

	void testResourceName() {
		def template = '''<r:resourceName uri="${uri}">${body}</r:resourceName>'''
		assertOutputEquals 'foo', template, [body:'foo'] // no link provided
		
		assertOutputEquals '<a href="http://www.slashdot.org">http://www.slashdot.org</a>', template, [body:' http://www.slashdot.org '] // no link attribute, but it looks like a url.		
		assertOutputEquals '<a class="res" target="_blank" href="/explore/resource?ivorn=ivo%3A%2F%2Ffoo.bar%2Fchoo">ivo://foo.bar/choo</a>', template, [body:' ivo://foo.bar/choo '] // no link attribute, but it looks like a url.		
		assertOutputEquals '<a class="res" target="_blank" href="/explore/resource?ivorn=ivo%3A%2F%2Ffoo.bar%2Fchoo">fred</a>', template, [uri:'ivo://foo.bar/choo', body:'fred'] // no link attribute, but it looks like a url.	
		
	
		assertOutputEquals '<a class="res" target="_blank" href="/explore/resource?ivorn=ivo%3A%2F%2Ffoo.bar%2Fchoo">ivo://foo.bar/choo</a>', template, [uri:'ivo://foo.bar/choo', body:''] // link attribute, no body.
				
		
	}
	
	void testResourceNameXmlSlurper() {
		
		Resource r= Resource.findByIvorn('ivo://CDS.VizieR/II/85')
		// the common use of the <resourceName tag is to pass a xmlslurper result into it - so we'll test that..
		def xml = r.rxml.createSlurper()
		
		// ok, now pass the xml in.. want the tag to detect when it's getting a complex type, and extract the attribute ivo-id itself.
		def template = '''<r:resourceName xml="${xml}" />'''
		assertOutputEquals '<a class="res" target="_blank" href="/explore/resource?ivorn=ivo%3A%2F%2FCDS">CDS</a>', template, [xml: xml.curation.publisher] // resource name with ivo-id
		
		assertOutputEquals 'Catalog', template, [xml:xml.content.type] // something with no ivo-id - so not linked.
		
		assertOutputEquals '' , template, [xml: xml.foo] // a non-existent path.
		
	}
}
