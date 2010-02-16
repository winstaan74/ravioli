
package org.ravioli;

import org.apache.commons.collections.map.ReferenceMap;
import org.w3c.dom.Node;
import javax.xml.transform.*;
import javax.xml.xpath.*;
import org.xml.sax.InputSource;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/** xml processing assistance - contains parsing, etc */
class XmlService  {
	
	
	boolean transactional = false;
	
	/** evaluate an xpath of an xml document, and return a list of results
	 * 
	 */
	public List xpathList(def xml, String xstr) {
		synchronized (xpath) {
			XPathExpression e = findExpression(xstr)
			withInputSource(xml) { is -> 
				
				def nodes =  e.evaluate(is, XPathConstants.NODESET)
				//if it's a text-containing node, get the text value, else keep as-is.
				return nodes.collect { n->
					switch (n.nodeType) {
						case Node.ATTRIBUTE_NODE:
						case Node.TEXT_NODE:
						return n.nodeValue
						// bah, YAGNI
						//	case Node.ELEMENT_NODE && n.get: // check number of children, coerce to text if no child elements?
						//		return n;
						default:
						return n
					}
				}
			}
		}
	}
	
	
	/** evaluate an xpath of an xml docvument, and return a string
	 * 
	 */
	public String xpath(def xml, String xstr) {
		synchronized (xpath) {
			XPathExpression e = findExpression(xstr)
			withInputSource(xml) { is ->
				String r = e.evaluate(is)
				return r ?: null
			}
		}
	}
	
	/** find the expression for an xpath - either the 
	 * xpath is compiled, or a previously cached one is found.
	 * @param xstr
	 * @return
	 */
	private XPathExpression findExpression(String xstr) {
		if (xpathCache.containsKey(xstr)) {
			XPathExpression o =  xpathCache.get(xstr) // think it's still possible to get a null here.
			if (o != null) { return o; }
		}
		// not in the cache - we need to create it.
		// no need to synchronize -caller has already done that.
		XPathExpression xp = xpath.compile(xstr)
		xpathCache.put(xstr,xp)
		return xp;
		
	}
	
	/**create an input source from the xml parameter, and pass it to the closure.
	 * @param xml - may be a stream, reader, or xml-containing string
	 */
	private def withInputSource(xml , Closure closure) {
		switch(xml) {
			case Reader:
			case InputStream:	
			return closure.call(new InputSource(xml))

			case URL:
				return closure.call(new InputSource(xml.toString()))
			
			case String:
			return new StringReader(xml).withReader { r ->
				closure.call(new InputSource(r))
			}
			default:
			throw new IllegalArgumentException("Passed in a " + xml?.class?.name);			
		}
	}
	
	
	private final Map<String, XPathExpression> xpathCache = new ReferenceMap<String,XPathExpression>()
	
	private final XPath xpath = XPathFactory.newInstance().newXPath();
	
	
	// api to XSLT transformations
	/**
	 * @param stylesheet the stylesheet to use
	 * @param the input - may be String xml content, or URL, or InputStream, or Reader
	 * @param output - may be writer, outputstream, or URL
	 */
	public void transform(String stylesheet, def input, def output) {
		Transformer trans = findTransformer(stylesheet)
		withStreamSource(input) { is ->
			withStreamResult(output) { os ->
				trans.transform(is, os)
			}
		}
		
	}
	
	private Transformer findTransformer(String stylesheet) {
		Transformer trans
		synchronized (xsltCache) {
			if (xsltCache.containsKey(stylesheet)) {
				Templates t = xsltCache.get(stylesheet);
				if (t != null) { trans =  t.newTransformer() }
			}
			// not in the cache - need to create one.
			withStreamSource(stylesheet) { ss ->
				Templates t = tFactory.newTemplates(ss)
				xsltCache.put(stylesheet, t)
				trans = t.newTransformer();
			}
		}
		trans.setErrorListener(new MyErrorListener(log:log))
		return trans
	}
	
	private def withStreamResult(def result, Closure closure) {
		switch(result) {
			case Writer:
			case OutputStream:
			case String:
			return closure.call(new StreamResult(result));
			case URL:
			return closure.call(new StreamResult(result.toString()));
			default:
			throw new IllegalArgumentException("Passed in a " + result?.class?.name);	
		}
	}
	
	private def withStreamSource(xml , Closure closure) {
		switch(xml) {
			case Reader:
			case InputStream:	
			return closure.call(new StreamSource(xml))
			case URL:
			return closure.call(new StreamSource(xml.toString()))
			case String:
			return new StringReader(xml).withReader { r ->
				closure.call(new StreamSource(r))
			}
			default:
			throw new IllegalArgumentException("Passed in a " + xml?.class?.name);
		}
	}
	
	private final TransformerFactory tFactory = TransformerFactory.newInstance()
	private final Map<String,Templates> xsltCache = new ReferenceMap<String,Templates>()
}
	// error listener interface.
	class MyErrorListener implements ErrorListener {
	public void error(TransformerException exception)
			throws TransformerException {
		this.ex = exception;
		
	}
	def log;
	
	def ex;
	
	public void fatalError(TransformerException exception)
			throws TransformerException {
		throw ex ?: exception // if we've seen an exception preivously, it'll be more informative than thos one.
		
	}
	
	public void warning(TransformerException exception) throws TransformerException {
		this.ex = exception
		}
	}

