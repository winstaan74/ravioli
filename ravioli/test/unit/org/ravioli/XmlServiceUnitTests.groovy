

package org.ravioli;
import javax.xml.transform.stream.*;
import javax.xml.transform.TransformerException;
import org.xml.sax.InputSource;

import grails.test.GrailsUnitTestCase;


class XmlServiceUnitTests extends GrailsUnitTestCase {
	protected void setUp() {
		super.setUp()
		mockLogging(XmlService)
		xml = new XmlService()
	}
	protected void tearDown() {
		super.tearDown()
		xml = null;
	}
	
	XmlService xml
	
	public void testXPath() {
		def doc = """<foo><bar>txt</bar></foo>"""
		assertEquals("txt",xml.xpath(doc, "/foo/bar"))
	}
	
	
	public void testXPathList() {
		def doc = """<foo><bar>bar</bar><bar>beep</bar></foo>"""
		assertEquals(['bar','beep'],xml.xpathList(doc, "/foo/bar/text()"))
	}
	
	void testXPathListNotEndingInText() {
		def doc = """<foo><bar>bar</bar><bar>beep</bar></foo>"""
		def l = xml.xpathList(doc, "/foo/bar")
		assertNotNull(l) 
		assertEquals (2, l.size())
		l.each {
			assertNotNull(it)
		}
	}
	
	void testEmptyXPath() {
		def doc = """<foo><bar>txt</bar></foo>"""
		assertNull xml.xpath(doc, "/none")
	}
	
	void testEmptyXPathList() {
		def doc = """<foo><bar>bar</bar><bar>beep</bar></foo>"""
		def l = xml.xpathList(doc, "/none")
		assertNotNull l
		assertEquals 0, l.size()
	}
	
	void testXPathPutInCache() { // wow - seems like I can access private variables. funky!
		assertEquals 0, xml.xpathCache.size()
		def doc = """<foo><bar>txt</bar></foo>"""
		assertEquals("txt",xml.xpath(doc, "/foo/bar"))
		assertEquals 1, xml.xpathCache.size()
	}
	
	
	private static final String style = """
		<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
		""";
	
	void testTransformerPutInCache() {
		assertEquals 0, xml.xsltCache.size()
		def t = xml.findTransformer(style)
		assertNotNull(t)
		assertEquals 1, xml.xsltCache.size()
		// now exercize the other branch..
		t = xml.findTransformer(style)
		assertNotNull(t)
		assertEquals 1, xml.xsltCache.size()
	}
	
	void testTransform() {
		def doc = """<?xml version="1.0" encoding="UTF-8"?><foo><bar/></foo>"""
		StringWriter result = new StringWriter()
		xml.transform(style,doc,result)
		assertEquals(doc,result.toString())
	}
	
	void testWithInputSource() {
		StringReader s = new StringReader("")
		xml.withInputSource(s) { 
			assertNotNull(it)
			assertTrue (it instanceof InputSource)
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0])
		xml.withInputSource(bis) { 
			assertNotNull(it)
			assertTrue (it instanceof InputSource)
		}
		xml.withInputSource("hi") { 
			assertNotNull(it)
			assertTrue (it instanceof InputSource)
		}
		xml.withInputSource(new URL("http://www.slashdot.org")) { 
			assertNotNull(it)
			assertTrue (it instanceof InputSource)
		}
		shouldFail(IllegalArgumentException) {
			xml.withInputSource(new Object()) { 
				fail("shouldn't get here")
			}			
		}
	}
	
	void testWithStreamSource() {
		StringReader s = new StringReader("")
		xml.withStreamSource(s) { 
			assertNotNull(it)
			assertTrue (it instanceof StreamSource)
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0])
		xml.withStreamSource(bis) { 
			assertNotNull(it)
			assertTrue (it instanceof StreamSource)
		}
		xml.withStreamSource("hi") { 
			assertNotNull(it)
			assertTrue (it instanceof StreamSource)
		}
		xml.withStreamSource(new URL("http://www.slashdot.org")) { 
			assertNotNull(it)
			assertTrue (it instanceof StreamSource)
		}
		shouldFail(IllegalArgumentException) {
			xml.withStreamSource(new Object()) { 
				fail("shouldn't get here")
			}			
		}
	}
	
	void testWithStreamResult() {
		StringWriter s = new StringWriter();
		xml.withStreamResult(s) {
			assertNotNull(it)
			assertTrue(it instanceof StreamResult)
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		xml.withStreamResult(bos) {
			assertNotNull(it)
			assertTrue(it instanceof StreamResult)
		}
		
		xml.withStreamResult(new URL("http://www.slashdot.org")) {
			assertNotNull(it)
			assertTrue(it instanceof StreamResult)
		}
		
		xml.withStreamResult("foo") {
			assertNotNull(it)
			assertTrue(it instanceof StreamResult)
		}
		
		shouldFail(IllegalArgumentException) {
			xml.withStreamResult(new Object()) { 
				fail("shouldn't get here")
			}			
		}
	}
	
	private static final String errorStyle = """
		<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
		<xsl:template match='/'>
			<xsl:message terminate='yes'>
			fooble
		    </xsl:message>
		</xsl:template>
</xsl:stylesheet>
		""";
	void testThrowingException() {
		def doc = """<?xml version="1.0" encoding="UTF-8"?><foo><bar/></foo>"""
		StringWriter result = new StringWriter()
		try {
			xml.transform(errorStyle,doc,result)
			fail("expected to throw")
		} catch (TransformerException e) {
			assertTrue e.getMessage().contains('fooble')
		}
	}
	
	private static final String extensionStyle = """
		<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
			xmlns:java="http://xml.apache.org/xalan/java"
				exclude-result-prefixes="java"
		version="1.0">
		<xsl:template match='/'>
			<xsl:value-of select='java:java.lang.System.getProperty("user.home")' />
		</xsl:template>
</xsl:stylesheet>
		""";
	
void testTransformExtension() {
	def doc = """<?xml version="1.0" encoding="UTF-8"?><foo><bar/></foo>"""
	StringWriter result = new StringWriter()
	xml.transform(extensionStyle,doc,result)
	assertTrue (result.toString().contains('/Users/noel'))
}
}
