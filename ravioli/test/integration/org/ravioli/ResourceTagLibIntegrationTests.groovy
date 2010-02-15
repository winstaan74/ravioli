package org.ravioli
import groovy.util.XmlSlurper;

import grails.test.*

class ResourceTagLibIntegrationTests extends GroovyPagesTestCase {


    void testResourcetype() {
		// first part of template just injects value into page context.
		def template = '''<g:set var='r' value="${r}" scope='page'/><r:resourcetype/>'''
		// use a map to stub for a resource.
		assertOutputEquals 'ConeSearch', template, [r:[resourcetype:'cs:ConeSearch']]
		assertOutputEquals 'ConeSearch', template, [r:[resourcetype:'ConeSearch']]
		assertOutputEquals 'unspecified', template, [r:[resourcetype:null]]
		assertOutputEquals 'Remote Application (CEA)', template, [r:[resourcetype:'CeaApplication']]
		assertOutputEquals 'Remote Application (CEA)', template, [r:[resourcetype:'CeaHttpApplication']]
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
		def template = '''<g:set var='r' value="${r}" scope='page'/><r:source/>'''
		
		assertOutputEquals '', template, [r:[source:null]] // no source.
		assertOutputEquals 'anyold', template, [r:[source:'anyold']] // arbitrary kind of source
		
		def urlOutput = """<a href="${ResourceTagLib.BIBCODE_URL}anyold">anyold</a>"""
		assertOutputEquals urlOutput, template, [r:[source:'anyold',sourceFormat:'bibcode']] // bibcode marked as such, even tho it's not looking like one
		
		urlOutput = '''<a href="http://www.slashdot.org">http://www.slashdot.org</a>'''
		assertOutputEquals urlOutput, template, [r:[source:'http://www.slashdot.org']] // url instead of bibcode
		assertOutputEquals urlOutput, template, [r:[source:'http://www.slashdot.org', sourceFormat:'bibcode']] // url instead of bibcode - sourceFormat is ignored		
		
		def bc = '1974AJ.....79..819H'
		assertTrue new ResourceTagLib().looksLikeBibcode(bc)
		
		urlOutput = """<a href="${ResourceTagLib.BIBCODE_URL}${bc}">${bc}</a>"""
		assertOutputEquals urlOutput, template, [r:[source:bc]] // bibcode not marked as such - should recognize by it's format.
	}
	
	void testXpath() {
		Resource r = Resource.findByIvorn('ivo://nvo.caltech/registry');
		def template = '''<g:set var='r' value="${r}" scope='page'/><r:xpath path='/node()/shortName'/>'''
		assertOutputEquals 'Carnivore', template, [r:r]
		
		template = '''<g:set var='r' value="${r}" scope='page'/><r:xpath path='/node()/rubbish'/>'''
		assertOutputEquals '', template, [r:r]
		
	}
	
	void textXpathList() {
		Resource r = Resource.findByIvorn('ivo://jvo/vizier/J/ApJS/120/265');
		// singleton
		def template = '''<g:set var='r' value="${r}" scope='page'/><r:xpathList path='/node()/content/type'/>'''
		assertOutputEquals 'Catalog', template, [r:r]
		
		//many
		template = '''<g:set var='r' value="${r}" scope='page'/><r:xpathList path='/node()/content/subject'/>'''
		assertOutputEquals 'Globular Clusters, Photometry', template, [r:r]		
		
		//none
		template = '''<g:set var='r' value="${r}" scope='page'/><r:xpathList path='/node()/rubbish'/>'''
		assertOutputEquals '', template, [r:r]		
	}
	
	void testResourceName() {
		def template = '''<r:resourceName uri="${uri}">${body}</r:resourceName>'''
		assertOutputEquals 'foo', template, [body:'foo'] // no link provided
		
		assertOutputEquals '<a href="http://www.slashdot.org">http://www.slashdot.org</a>', template, [body:' http://www.slashdot.org '] // no link attribute, but it looks like a url.		
		assertOutputEquals '<a class="res" target="_blank" href="/explore/resource?ivorn=ivo%3A%2F%2Ffoo.bar%2Fchoo">ivo://foo.bar/choo</a>', template, [body:' ivo://foo.bar/choo '] // no link attribute, but it looks like a url.		
		assertOutputEquals '<a class="res" target="_blank" href="/explore/resource?ivorn=ivo%3A%2F%2Ffoo.bar%2Fchoo">fred</a>', template, [uri:'ivo://foo.bar/choo', body:'fred'] // no link attribute, but it looks like a url.		
			
		
	}
	
	void testResourceNameXmlSlurper() {
		
		Resource r= Resource.findByIvorn('ivo://CDS.VizieR/II/85')
		// the common use of the <resourceName tag is to pass a xmlslurper result into it - so we'll test that..
		def xml = new XmlSlurper().parseText(r.xml)
		
		// ok, now pass the xml in.. want the tag to detect when it's getting a complex type, and extract the attribute ivo-id itself.
		def template = '''<r:resourceName xml="${xml}" />'''
		assertOutputEquals '<a class="res" target="_blank" href="/explore/resource?ivorn=ivo%3A%2F%2FCDS">CDS</a>', template, [xml: xml.curation.publisher] // resource name with ivo-id
		
		assertOutputEquals 'Catalog', template, [xml:xml.content.type] // something with no ivo-id - so not linked.
		
		assertOutputEquals '' , template, [xml: xml.foo] // a non-existent path.
		
	}
}
