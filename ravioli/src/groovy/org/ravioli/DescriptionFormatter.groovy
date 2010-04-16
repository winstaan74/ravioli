package org.ravioli;

/** formats a block of text, 
 * splits into lead-in and further
 * rep[laces blank lines with br tags.
 * recognized embedded urls, wraps in a tags.
 * 
 * might be a candidate to make into a service some time in the far future.
 * @author noel
 *
 */
class DescriptionFormatter {
	/** 'constant' to be set during initialization. - will be the minimum size of the heading.*/
	 int HEADING_SIZE = 200
	
	// static patterns, safe to share between threads.
	
	/** find a blank line */
	static final def blankLine =  ~/\n\s*\n/
	
	/** find a fullstop, preceded and followed by a not-digit */
	static final def fullstop = ~/\D\.\D/ 

	/** detect a url - http  */
	static final def url = ~/(http|ftp):\/\/\S+/

	/** 
	 * formats a description
	 * @return either a single string, or a 2-elememtn array - first item will
	 * be the heading, the second item will be the following text.
	 */
	def format(String d) {
		String descr = d?.trim()
		def matches = descr =~ fullstop 
		if (!matches || matches.start() + HEADING_SIZE > descr.size()) {
			return tagify(descr)
		} else {
			int ix = matches.start() + 2 // cut at first full stop - +2 from start of match.
			def head = descr.substring(0,ix)
			def rest = descr.substring(ix).trim()
			return [tagify(head),tagify(rest)]
		}
	}

	/** splits into &lt;p&gt; tags, and finds any a tags. */
	public String tagify(String s) {
		return "<p>" +
			( s?.replaceAll(url,'<a href="$0">$0</a>')?.replaceAll(blankLine, "</p><p>") ?: "") +
			 "</p>"
	}

}
