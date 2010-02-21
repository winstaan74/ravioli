

package org.ravioli

import java.io.ByteArrayInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;


/** xml that defines a resource
 * in a separate domain class, so that it can be lazily loaded
 * separate to the resource it defines
 * 
 * which means for quick lookups in the resource, this large CLOB (sometimes some megs)
 * doesn't need to be loaded from db.
 * 
 * @todo put binary compressed alternative in a subclass? with a factory method?
 * @author noel
 *  */
class ResourceXml {
	
	public static final int MAX_PACKET_SIZE = 1084000; //1048576 is max packet size for myslq driver.

	static constraints = {
		xml(nullable:true,blank:true,maxSize:MAX_PACKET_SIZE) 
		binXml(nullable:true, maxSize:MAX_PACKET_SIZE)
	}


	
	static mapping = {
		xml column: 'xml', type:'text', length:1084000
		binXml column: 'binXml', type:'binary', length:1084000
	}


	
	public String getXml() {
		if (this.binXml != null) {
			ByteArrayInputStream bis = new ByteArrayInputStream(this.binXml)
			bis.withStream { s ->
				new GZIPInputStream(s).withStream {zis ->
					return zis.text
				}
			}
		} else {
			return this.xml
		}	
	}
	
	public void setXml(String xml) {
		if (xml.size() > MAX_PACKET_SIZE) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream()
			bos.withStream { s ->
				new GZIPOutputStream(s).withStream { zos ->
					zos << xml
				}
			}
			this.binXml = bos.toByteArray()
		} else {
		this.xml = xml
		}
	}
	
	/** the xml resource description */
	String xml 
	byte[] binXml
	
	def transient xmlService  // reference to xml service.
	
	static transients = ['xmlService']
	static belongsTo = [resource:Resource] // means deletes and updates to resource will be cascaded here
	
	
	/** access the stripped version of xml - no tags, just body content 
	 * and attr values
	 */
	public String stripXML() {
		return xpathList("//@*|//text()").join(' ')
	}
	
	/** evaluate an xpath over the xml body of this resource
	 * and return a single value, or null if no match.
	 * @param path
	 * @return
	 */
	public  String xpath(String path) {
		return xmlService.xpath(this.getXml(),path)
	}
	
	/** evaluate an xpath over the xml body of this resource
	 * and return a list of values
	 * @param path
	 * @return
	 */
	public  List xpathList(String path) {
		return xmlService.xpathList(this.getXml(),path)
	}
	
	
	// some of these are currently unused, but do no harm being there. 
	private final static Map DYNAMIC_PROPERTIES = [
	titleField: '/node()/title'
	, shortnameField:'/node()/shortName'
	, description:'/node()/content/description'
	, sourceField:'/node()/content/source'
	, sourceFormat:'/node()/content/source/@format'
	, resourcetype: '/node()/@*[local-name() = "type"]'
	, referenceUrl: '/node()/content/referenceURL'
	, version: '/node()/curation/version'
	, date: '/node()/curation/date' // strictly speaking, this can be a list, but really never is.
	]
	
	private final static Map DYNAMIC_LISTS = [
	subject: '/node()/content/subject/text()'
	,waveband: '/node()/coverage/waveband/text()'
	,capability: '/node()/capability/@standardID|/node()/capability/@*[local-name() = "type"]'
	,creator: '/node()/curation/creator/name/text() | /node()/curation/creator/@ivo-id'
	, curation: '/node()/curation//text() | /node()/curation//@*'
	, publisher: '/node()/curation/publisher/text() | /node()/curation/publisher/@ivo-id'
	, name: '/node()/shortName/text() | /node()/title/text()' 
	, col: '/node()/catalog/table/column/name/text() | /node()/table/column/name/text()'
	, ucd: '/node()/catalog/table/column/ucd/text() | /node()/table/column/ucd/text()'
	, level: '/node()/content/contentLevel/text()' // don't have an index for this
	, contenttype: '/node()/content/type/text()' // text() necessary, as used as part of a dynamic list defn below.
	
	//future: validationLevel?
	]
	
	static { // this is defined in terms of other ones - so do it after the map creation
		// so we can refer to the map.
		DYNAMIC_LISTS.type = DYNAMIC_LISTS.capability + 
		" | " + DYNAMIC_LISTS.contenttype +
		" | " + DYNAMIC_PROPERTIES.resourcetype
	}
	
	/** utility to provide convenient access to xpath-defined fields
	 * means we can adjust the implementation later, without letting the details leak out.
	 * @param name
	 * @return
	 */
	def propertyMissing(String name) {
		if (name == 'xml') {
			return getXml()
		}
		def xp = DYNAMIC_PROPERTIES[name]
		if(xp) {
			// cache the method impleemntaiton, so it will be called more efficiently next time.
			Resource.metaClass."$name" = { ->  xpath(xp)?.trim() }
			return xpath(xp)?.trim()
		} else {
			xp = DYNAMIC_LISTS[name]
			if (xp) {
				Resource.metaClass."$name" = {-> xpathList(xp) }
				return xpathList(xp) 
			}
		}
		throw new MissingPropertyException(name)
	}
}
