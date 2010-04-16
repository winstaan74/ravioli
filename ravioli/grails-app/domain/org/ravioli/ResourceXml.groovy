

package org.ravioli

import groovy.util.slurpersupport.GPathResult;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

import org.hibernate.lob.ClobImpl;
import java.io.ByteArrayOutputStream;

import org.hibernate.Hibernate;




/** The XML that defines a resource.
 * 
 * Stored in a separate domain class from {@link Resource}, so that it can be lazily loaded
 * separate to the resource it defines
 * properties in this object are only required for search indexing, or resource details display.
 * 
 * which means for quick lookups in the resource, this large CLOB (sometimes some megs)
 * doesn't need to be loaded from db.
 * 
 * @author noel
 *  */
class ResourceXml {
	
/** make a lot of space for the xml */
	static constraints = {
		xml(nullable:true,blank:true,maxSize: 2147483647) // max size for a long text in mysql.

	}
/** unused - remove */
	static mapping ={
		clob type: 'clob'
	}
	
	static transients = ['xmlService','vals']
	 /** belongs to a reosurce - deletes and updates cascade */
	static belongsTo = Resource 
	
	
	String xml

	def transient xmlService  // reference to xml service.

	
	/** access the stripped version of xml - no tags, just body content 
	 * and attr values
	 *
	 */
	public String stripXML() {
		return xmlService.xpathList(getXml(),"//@*|//text()").join(' ')
	}

	/**
	 * properties that are defined using XPATH.
	 * 
	 *  some of these are currently unused, but do no harm being there. 
	 */
	private final static Map DYNAMIC_PROPERTIES = [
//	titleField: '/node()/title'
//	, shortnameField:'/node()/shortName'
	 description:'/node()/content/description' 
//	, sourceField:'/node()/content/source'
//	, sourceFormat:'/node()/content/source/@format'
	, resourcetype: '/node()/@*[local-name() = "type"]'
//	, referenceUrl: '/node()/content/referenceURL'
//	, version: '/node()/curation/version'
//	, date: '/node()/curation/date' // strictly speaking, this can be a list, but really never is.
	]

	/** 
	 * List-valued properties, defined using XPATH.
	 * 
	 * these are used during indexing.. */
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
	
	/** Implements Dynamic properties, by applying XPaths to the XML.
	 * 
	 * This is a tility to provide convenient access to xpath-defined fields
	 * means we can adjust the implementation later, without letting the details leak out.
	 * 
	 * first time one dynamic property is requested, all are computed and loaded. This is sensible,
	 * as the dynamic properties are onlyl accessed during search indexing - and if one is seen, then all the rest 
	 * are going to be seen very soon. By computing all these properties at once, the XML only needs to be fetched from database, and parsed, once.
	 * 
	 * @param name - one of the keys in {@link DYNAMIC_LISTS} or {@lnk DYNAMIC_PROPERTIES}
	 * @return
	 */
	def propertyMissing(String name) {
		if (vals.containsKey(name)) {
			return vals.get(name)
		}
		if (DYNAMIC_PROPERTIES.containsKey(name) || DYNAMIC_LISTS.containsKey(name)) {
			// load them all up.
			def _xml = getXml() // optmization, in case of subclass - only need to decompress data once.
			DYNAMIC_PROPERTIES.each {k, path -> 
				def val = xmlService.xpath(_xml,path)?.trim()
				vals."${k}" = val
				} 
			DYNAMIC_LISTS.each {k,path -> 
				def val = xmlService.xpathList(_xml,path)
				vals."${k}" = val
			} 
			return vals.get(name)
		}
		throw new MissingPropertyException(name)
	}
	
	/** temporary storage space for the dyanmic values */
	def vals = [:]

	public GPathResult createSlurper() {
		return new XmlSlurper().parseText(getXml())
	}
}
