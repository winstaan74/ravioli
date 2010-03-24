package org.ravioli;

import groovy.util.GroovyTestCase;

class DescriptionFormatterUnitTests extends GroovyTestCase {

	protected void setUp() {
		super.setUp()
		f = new DescriptionFormatter()
	}
	
	protected void tearDown() {
		super.tearDown()
	}
	
	def f
	
	void testTagify() {
		assertEquals "<p></p>", f.tagify(null)
		
		assertEquals "<p></p>", f.tagify('')
		
		assertEquals """<p><a href="http://www.foo.com">http://www.foo.com</a></p>""", f.tagify('http://www.foo.com')
	
		assertEquals """<p></p><p></p>""", f.tagify('\n\n')
		
		assertEquals """<p></p><p></p>""", f.tagify('\n  \n')
		
		assertEquals """<p>a</p><p>b</p>""", f.tagify('a\n  \nb')
		
		
		assertEquals """<p>Hi there <a href="http://www.foo.com">http://www.foo.com</a>\nmore</p><p>stuff</p>""",
			f.tagify('Hi there http://www.foo.com\nmore\n  \nstuff')
	
	}
	
	void testFormat() {

		assertEquals '<p></p>', f.format(null)
		
		assertEquals '<p></p>', f.format('')
		
		assertEquals '<p>foo</p>', f.format('foo')
		
		def s1 = 'Mary had a little (>2.0nm) lamb.'
		def s2 = 'Whose fleece was white as snow.'
		def input = (s1 + ' ' + s2 + ' ') * 10 // repeat 10 times.
		assertTrue input.length() > f.HEADING_SIZE
		def result = f.format(input)
		assertTrue result instanceof List
		assertEquals '<p>'+s1+'</p>', result[0]  // first item of lislt will be first sentance
		     
		// remainder will be second item of list
		                                     
		assertEquals '<p>' + s2 + (' ' + s1 + ' ' + s2 )*9 + '</p>', result[1]                               
		                                     
		                                     
	}
	
}
