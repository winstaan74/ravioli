

package org.ravioli

import org.joda.time.DateTime;
import javax.xml.xpath.*;
import org.xml.sax.InputSource;
import org.compass.annotations.*
import groovy.util.XmlSlurper;

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
			, expression="data.xpath(data.SHORTNAME_XPATH)" )
			
,@SearchableDynamicMetaData(name='description', converter='groovy'
			, expression="data.xpath(data.DESCRIPTION_XPATH)" )
			
,@SearchableDynamicMetaData(name='source', converter='groovy', excludeFromAll=ExcludeFromAll.YES
			, expression="data.xpath(data.SOURCE_XPATH)" )
			
,@SearchableDynamicMetaData(name='resourcetype', converter='groovy', excludeFromAll=ExcludeFromAll.YES
		, expression="data.xpath(data.RESOURCETYPE_XPATH)" )

,@SearchableDynamicMetaData(name="all", converter="groovy", excludeFromAll=ExcludeFromAll.YES
			,expression="data.stripXML()" )

,@SearchableDynamicMetaData(name="subject", converter="groovy"
			,expression="data.xpathList(data.SUBJECT_XPATH)" )

,@SearchableDynamicMetaData(name="waveband", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.xpathList(data.WAVEBAND_XPATH)" )
		
,@SearchableDynamicMetaData(name="capability", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.xpathList(data.CAPABILITY_XPATH)" )
		
,@SearchableDynamicMetaData(name="creator", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.xpathList(data.CREATOR_XPATH)" )
		
,@SearchableDynamicMetaData(name="curation", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.xpathList(data.CURATION_XPATH)" )
		
,@SearchableDynamicMetaData(name="publisher", converter="groovy", excludeFromAll=ExcludeFromAll.YES
		,expression="data.xpathList(data.PUBLISHER_XPATH)" )

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

	public final static String SHORTNAME_XPATH = '/node()/shortName'
	public final static String SOURCE_XPATH = '/node()/content/source'
	public final static String DESCRIPTION_XPATH = '/node()/content/description'
	public final static String RESOURCETYPE_XPATH = '/node()/@*[local-name() = "type"]' // work around for namespace - trying to get to xsi:type
		
	public final static String SUBJECT_XPATH = '/node()/content/subject/text()'
	public final static String WAVEBAND_XPATH = '/node()/coverage/waveband/text()'
	public final static String CAPABILITY_XPATH= '/node()/capability/@standardID|/node()/capability/@*[local-name() = "type"]'
	public final static String CREATOR_XPATH = '/node()/curation/creator/name/text() | /node()/curation/creator/@ivo-id'
	public final static String CURATION_XPATH = '/node()/curation//text() | /node()/curation//@*'
	public final static String PUBLISHER_XPATH = '/node()/curation/publisher/text() | /node()/curation/publisher/@ivo-id'
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
	public String stripXML() {
		return xpathList("//@*|//text()").join(' ')
	}

	/** evaluate an xpath over the xml body of this resource
	 * and return a single value, or null if no match.
	 * @param path
	 * @return
	 */
	public  String xpath(String path) {
		return xmlService.xpath(this.xml,path)
	}
	
	/** evaluate an xpath over the xml body of this resource
	 * and return a list of values
	 * @param path
	 * @return
	 */
	public  List xpathList(String path) {
		return xmlService.xpathList(this.xml,path)
	}
	

	
		
/*
 *Other Search positions used by voexplorer
 * '*' - present in smartlist building ui
 * '+' - present in filterwheels
 * @ - incorporated into this code.
 
		targets.put("name",new String[] {"$r/shortName","$r/title"});
		//deprecated - prefer resourcetype or capability.
		targets.put("type", new String[] {
		        "$r/@xsi:type"
		        ,"$r/content/type"
		        ,"$r/capability/@xsi:type"
		 //drags in too much cruft.       ,"$r/capability/@standardID"
		        });
		targets.put("level", new String[] {"$r/content/contentLevel"});
		targets.put("col",     new String[]{
                "$r/catalog/table/column/name",
                "$r/table/column/name"});
	+	targets.put("ucd",		 new String[]{
		        "$r/catalog/table/column/ucd",
		        "$r/table/column/ucd"});
        ALSO
        + serviceType - specific to SIAP
        + tag - anns, retitles, flags.
	}

    incremental search uses:
        - id, title, shortName, subject content/type, description
        - creator/name creator/id publisher/name publisher/id
        - contrubutor, contact/name
        - annotations.
        - waveband

    System Filter - xsiType, capability.
        - might be a bit tricky to implement. we'll see.

    Program Code
        - might require access to a few other fields
            - access url, for example.
            - could always use XPATH / XQuery parsing to get to these.
            - they're projections, not queries.
        - presentation
            - use xslt over the whole lot.
            
    Q - how to represent resources? As an inheritance tree again?
            - naah. it wasn't that nice.
            -

*/

}
