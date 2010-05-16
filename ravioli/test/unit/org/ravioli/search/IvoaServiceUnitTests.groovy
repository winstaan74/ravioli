package org.ravioli.search
import groovy.util.XmlSlurper;

import org.ravioli.Resource;
import org.ravioli.ResourceXml;
import grails.test.*

class IvoaServiceUnitTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
        serv = new IvoaService()
    }
    
    def serv

    protected void tearDown() {
        super.tearDown()
        serv = null
    }

    void testResourceKnown() {
    	def identifier = 'ivo://test.resource'
    	def pi= "<?xml version='1.0'?>"
    	def body = "<resource><identifier>${identifier}</identifier></resource>"
    	
    	Resource r= new Resource(ivorn:identifier, rxml:new ResourceXml(xml:pi+body))
    	mockDomain(Resource,[r])
    	
    	def res = serv.getResource(identifier)
    	assertNotNull(res)
    	assertEquals(body, res) // processing instr should be stripped.
    }
    
    void testResourceUnknown() {
    	mockDomain(Resource)
    	assertNull serv.getResource('ivo://unknown')
    }
    
    
    void testTranslateWhere() {
    	def q = """
    		<rs:Where 
    			xmlns:rs="http://www.ivoa.net/wsdl/RegistrySearch/v1.0"
    			xmlns:adql="http://www.ivoa.net/xml/ADQL/v1.0"
    			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    		>
    	<adql:Condition xsi:type="adql:likePredType">
    	<adql:Arg Table="" xsi:type="adql:columnReferenceType"
    	name="description"
    	xpathName="content/description"/>
    	<adql:Pattern xsi:type="adql:atomType">
    	<adql:Literal Value="%quasar%" xsi:type="adql:stringType"/>
    	</adql:Pattern>
    	</adql:Condition>
    	</rs:Where>
    			"""
    	def slurp = new XmlSlurper().parseText(q)
    	def op = serv.translateWhere(slurp)
    	assertEquals "description:*quasar*",op
    }
}
