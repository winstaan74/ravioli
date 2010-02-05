import grails.test.*

class LabelTagLibIntegrationTests extends GroovyPagesTestCase {

	
	void testField() {
		def template = '''<l:field name='title' value="${val}" />''' // single, not double, so GString isn't evaluated now..
		// correct case
		def ls = [1,2,3]
		def result = applyTemplate(template, [val:ls])
		assertTrue result.contains('title')
		assertTrue result.contains("${ls}")
		// non-appearing cases.
		assertOutputEquals('',template, [val:null]) // null value provided
		assertOutputEquals('',template, [:]) // no value provided
		assertOutputEquals('',template, [val:'']) // empty provided
		assertOutputEquals('',template, [val:[]]) // empty list provided	
		assertOutputEquals('',template, [val:[:]]) // empty map provided			
		assertOutputEquals('',template, [val:'  ']) // blank provided

	}
	
	void testFieldBody() {
		def template = '''<l:field name='title'>${val}</l:field>'''
		// correct case
		def result = applyTemplate(template, [val:"foo"])
		assertTrue result.contains('title')
		assertTrue result.contains("foo")
		// non-appearing cases.
		assertOutputEquals('',template, [val:null]) // null value provided
		assertOutputEquals('',template, [:]) // no value provided
		assertOutputEquals('',template, [val:'']) // empty provided
		// can't expect these two to work in body - as we'll have the representation of the datastrucrure itself.
		//assertOutputEquals('',template, [val:[]]) // empty list provided	
		//assertOutputEquals('',template, [val:[:]]) // empty map provided			
		assertOutputEquals('',template, [val:'  ']) // blank provided
	}
	
	void testLabel() {
		def template = '''<l:label name='title'/>'''
		assertOutputEquals '''<span class="label">title</span>&nbsp;''', template
	}
	
	void testSeq() {
		def template = '''<l:seq name='title' values="${val}"/>'''
		def l = [1,2,3]
		def result = applyTemplate(template, [val:l])
		assertTrue result.contains('title')
		assertTrue result.contains('1, 2, 3')
		
		// test for non-appearing cases
		assertOutputEquals('',template, [val:null]) // null value provided
		assertOutputEquals('',template, [:]) // no value provided
		assertOutputEquals('',template, [val:'']) // empty provided	
		assertOutputEquals('',template, [val:[]]) // empty list provided	
		
	}
	
	void testCondLink() {
		def template = '''<l:condLink name='title' url="${val}"/>'''
		def url = 'http://www.slashdot.org'
		assertOutputEquals "<a href='${url}'>title</a>" ,template, [val:url]
		
		// test for non-appearing cases
		assertOutputEquals('',template, [val:null]) // null value provided
		assertOutputEquals('',template, [:]) // no value provided
		assertOutputEquals('',template, [val:'']) // empty provided	
		
	}

	void testCondLinkBody() {
		def template = '''<l:condLink name='title'> ${val} </l:condLink>''' // note additional space..
		def url = 'http://www.slashdot.org'
		assertOutputEquals "<a href='${url}'>title</a>", template, [val:url]
		
		// test for non-appearing cases
		assertOutputEquals('',template, [val:null]) // null value provided
		assertOutputEquals('',template, [:]) // no value provided
		assertOutputEquals('',template, [val:'']) // empty provided	
		
	}
}
