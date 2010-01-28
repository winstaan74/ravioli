
package org.ravioli

import org.joda.time.DateTime;
import javax.xml.xpath.*;
import org.compass.annotations.*

/**
 * 
 * Representation of a voresource.
 * original XML is maintained in xml field.
 * on construction, we parse a few fields that either we're going to need
 * programattically, or which need some mangling that's easier to do by hand
 * than in xpath.
 * Other fields that we want to enable search on are expressed as 'searchableDynamics' - 
 * there's an xpath that's applied to compute the value from the xml field
 * This is more inefficient timewise, but only happens when the search enging is indexing
 * a resource - when it's just been harvested, once.
 * Because of the dynamic approach, there's less data replicated in the db,
 * which needs to be loaded when accessing the resource - so user experience should be
 * improved.
 * 
 * Furthermore, some of the query positions are easiest to express in XPATH.
 * Note that xpath fields are extracted untrimmed - but this doesn't matter, as they\re only
 * going for trimming anyhow.
 * 
 */
@Searchable
@SearchableDynamicMetaDatas(value=[
@SearchableDynamicMetaData(name="shortname",converter="groovy"
			, expression="data.shortname" )
			
,@SearchableDynamicMetaData(name='description', converter='groovy'
			, expression="data.description" )
			
,@SearchableDynamicMetaData(name='source', converter='groovy', excludeFromAll=ExcludeFromAll.YES
			, expression="data.source" )
			
,@SearchableDynamicMetaData(name='resourcetype', converter='groovy', excludeFromAll=ExcludeFromAll.YES
		, expression="data.resourcetype" )

,@SearchableDynamicMetaData(name="all", converter="groovy", excludeFromAll=ExcludeFromAll.YES
			,expression="data.stripXML()" )

,@SearchableDynamicMetaData(name="subject", converter="groovy"
			,expression="data.subject" )

,@SearchableDynamicMetaData(name="waveband", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.waveband" )
		
,@SearchableDynamicMetaData(name="capability", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.capability" )
		
,@SearchableDynamicMetaData(name="creator", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.creator" )
		
,@SearchableDynamicMetaData(name="curation", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.curation" )
		
,@SearchableDynamicMetaData(name="publisher", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.publisher" )

,@SearchableDynamicMetaData(name="name", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.name" )

,@SearchableDynamicMetaData(name="ucd", converter="groovy", excludeFromAll=ExcludeFromAll.YES
,expression="data.ucd" )
,@SearchableDynamicMetaData(name="col", converter="groovy", excludeFromAll=ExcludeFromAll.YES
,expression="data.col" )
,@SearchableDynamicMetaData(name="type", converter="groovy", excludeFromAll=ExcludeFromAll.YES
,expression="data.type" )
])
class Resource {
	
	def transient xmlService  // reference to xml service.
	static constraints = {
		ivorn(unique:true, matches:/ivo:\/\/\S+/,maxsize:200) // must have prefix ivo://
		title(nullable:true,maxsize:500)
		created(nullable:false)
		modified(nullable:true)
		xml(nullable:false,blank:false)		
	}
	static transients = ['xmlService']

	private final static Map DYNAMIC_PROPERTIES = [
	       shortname:'/node()/shortName'
	       ,description:'/node()/content/description'
		   , source:'/node()/content/source'
		   , resourcetype: '/node()/@*[local-name() = "type"]'
		   , contenttype: '/node()/content/type/text()' // text() necessary, as used as part of a dynamic list defn below.
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
			//future: validationLevel?
		]

	static { // this is defined in terms of other ones - so do it after the map creation
		// so we can refer to the map.
		DYNAMIC_LISTS.type = DYNAMIC_LISTS.capability + 
		" | " + DYNAMIC_PROPERTIES.contenttype +
		" | " + DYNAMIC_PROPERTIES.resourcetype
	}

		@SearchableId(excludeFromAll=ExcludeFromAll.YES)
		Long id
	//vodesktop default becomes lucene's 'all'
	// need to omit bits we don't want from 'all'
		
		@SearchableProperty(name='identifier',boost=2.0f)
		String ivorn // $r/identifier 
		
		// not searchable - accesses the content through 'all" defined using stripXML
		String xml 

		@SearchableProperty(boost=2.0f)
		String title //$r/title

		@SearchableProperty(format="yyyy-MM-dd")
		Date created
		@SearchableProperty(format="yyyy-MM-dd")
		Date modified

		/** parse a url containing XML into a resource 
		 * 
		 * @param xml xml description of the resource - outer tag is expeected to be <ri:Resource
		 * @param ivorn identifier of this resource (optional - else will be determined from the source).
		 * @return
		 */
	static Resource buildResource(String xml, String ivorn = null) {
		Resource r = new Resource(xml:xml, ivorn:ivorn)
		// parse up the rest
		r.updateFields(xml)
		return r;
		
	}

	/** parse xml and update most of the fields of this resource from it */
	void updateFields(String xml) {
		
		this.xml = xml;
		def gp = new XmlSlurper().parseText(xml)

		def ivo = gp.identifier?.text()?.trim()
		if (this.ivorn == null) { // never overwrite the ivorn
			this.ivorn = ivo
		} else if (ivorn != ivo) { // check the ivorn is correct
			throw new IllegalArgumentException("Expected xml for resource '${ivorn}', but got identifier '${ivo}'");
		}
		
		this.title = gp.title?.text()?.trim()
		
		// using joda time here, as it has the correct parsing built in.
		this.created = new DateTime(gp.'@created'.text()).toDate();
		try {
			DateTime dt = new DateTime(gp.'@updated'.text())
			this.modified = dt.toDate();
		} catch (IllegalArgumentException e) {
			// no matter..
		}		
	}
	
	/** access the stripped version of xml - no tags, just body content 
	 * and id values
	 */
	private String stripXML() {
		return xpathList("//@*|//text()").join(' ')
	}

	/** evaluate an xpath over the xml body of this resource
	 * and return a single value, or null if no match.
	 * @param path
	 * @return
	 */
	private  String xpath(String path) {
		return xmlService.xpath(this.xml,path)
	}
	
	/** evaluate an xpath over the xml body of this resource
	 * and return a list of values
	 * @param path
	 * @return
	 */
	private  List xpathList(String path) {
		return xmlService.xpathList(this.xml,path)
	}
	
	/** utility to provide convenient access to xpath-defined fields
	 * means we can adjust the implementation later, without letting the details leak out.
	 * @param name
	 * @return
	 */
	def propertyMissing(String name) {
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
	
	/** search functionality*/
	/** rewrite query to remove all references to 'ivo://' */
	public static String rewriteQuery(String s) {
		return s?.replaceAll('ivo:', '')
	}
	
		
/*
 *Other Search positions used by voexplorer

        + serviceType - specific to SIAP
        + tag - anns, retitles, flags.
	}

    incremental search uses:
        - id, title, shortName, subject content/type, description
        - creator/name creator/id publisher/name publisher/id
        - contrubutor, contact/name
        - annotations.
        - waveband
    - maybe this should be our definition of the main index??

    System Filter - xsiType, capability.
        - might be a bit tricky to implement. we'll see.

*/

}
