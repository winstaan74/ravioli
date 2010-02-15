
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
			
@SearchableDynamicMetaData(name='description', converter='groovy'
			, expression="data.description" )
			
,@SearchableDynamicMetaData(name='resourcetype', converter='groovy', excludeFromAll=ExcludeFromAll.YES
		, expression="data.resourcetype" )

,@SearchableDynamicMetaData(name="all", converter="groovy", excludeFromAll=ExcludeFromAll.YES
			,expression="data.rxml.stripXML()" )

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
,@SearchableDynamicMetaData(name="identifier", converter="groovy", excludeFromAll=ExcludeFromAll.YES
,expression="data.ivorn" )
,@SearchableDynamicMetaData(name="date", converter="groovy", excludeFromAll=ExcludeFromAll.YES
//,format="yyyy-MM-dd" @todo work out how to express format here..
//@todo find out how to make this field non-tokenized.
,expression="[] << data.created << data.modified " )
]) 
class Resource {
	static constraints = {
		ivorn(unique:true, matches:/ivo:\/\/\S+/,maxsize:1000) // must have prefix ivo://
		title(nullable:true,maxsize:1000)
		shortname(nullable:true, maxsize:100)
		source(nullable:true, maxsize:200)
		created(nullable:false)
		modified(nullable:true)	
		date(nullable:false) // display version of date.
		status(matches:'active') // enforce a constant.
		rxml(unique:true)
		wavebands(nullable:true, maxsize:1000)
		subjects(nullable:true, maxsize:1000)
		publishers(nullable:true, maxsize:1000)
		creators(nullable:true, maxsize:1000)
	}

	static mapping = {
		// need to define column types explicitly, as otherwise we're given a varchar(255), which isn't large enough for some of the fields above
		ivorn column: 'ivorn', sqlType:'VARCHAR(1000)'
		title column: 'title', sqlType:'VARCHAR(1000)'
		wavebands column:'wavebands', sqlType:'VARCHAR(1000)'
		subjects column:'subjects', sqlType:'VARCHAR(1000)'
		publishers column:'publishers', sqlType:'VARCHAR(1000)'
		creators column:'creators', sqlType:'VARCHAR(1000)'
	}

		/** the resource xml - modelled in a separate object
		 *   */	
		ResourceXml rxml= new ResourceXml()
		
		@SearchableId(excludeFromAll=ExcludeFromAll.YES)
		Long id
	//vodesktop default becomes lucene's 'all'
	// need to omit bits we don't want from 'all'
		
		@SearchableProperty(name='ivorn',boost=2.0f)
		String ivorn // $r/identifier 
		
		String status // not searchable.
		
		@SearchableProperty(boost=2.0f)
		String shortname
		
		@SearchableProperty() // decided to add to default search index.
		String source
		
		@SearchableProperty(boost=2.0f)
		String title 
		
		@SearchableProperty(format="yyyy-MM-dd")
		Date created
		@SearchableProperty(format="yyyy-MM-dd")
		Date modified
		
	// pre-formatted fields used in resource table.
	// not indexed: lucene has indexed the raw text elsewhere.
	// using the dynamic property in the singular - e.g. r.subject, 
	// while r.subjects gives the formatted version.
		String subjects
		String wavebands
		String publishers
		String creators
		
		// not searchable - we define the 'date' search index above, to include both modified and created dates
		Date date // combination of modified and created
		
		
		/** parse a url containing XML into a resource 
		 * 
		 * @param xml xml description of the resource - outer tag is expeected to be <ri:Resource
		 * @param ivorn identifier of this resource (optional - else will be determined from the source).
		 * @return
		 */
	static Resource buildResource(String xml, String ivorn = null) {
		Resource r = new Resource(ivorn:ivorn)
		// parse up the rest
		r.updateFields(xml)
		return r;
		
	}

	/** parse xml and update most of the fields of this resource from it */
	void updateFields(String xml) {
		this.rxml.xml = xml;
		def gp = new XmlSlurper().parseText(xml)

		def ivo = gp.identifier?.text()?.trim()
		if (this.ivorn == null) { // never overwrite the ivorn
			this.ivorn = ivo
		} else if (ivorn != ivo) { // check the ivorn is correct
			throw new IdentifyException(reported:ivo, expected:ivorn)
		}
		
		this.title = gp.title?.text()?.trim()
		this.shortname = gp.shortName?.text()?.trim()
		this.status = gp.'@status'.text()
		this.source = gp.content.source?.text()?.trim()
		// using joda time here, as it has the correct parsing built in.
		this.created = new DateTime(gp.'@created'.text()).toDate();
		try {
			DateTime dt = new DateTime(gp.'@updated'.text())
			this.modified = dt.toDate();
		} catch (IllegalArgumentException e) {
			// no matter..
		}	
		this.date = modified ?: created
		
		def fuse = {it.collect({e -> e.text()?.trim()}).sort().join(', ')}
		// variant that doesn't sort it's items
		def fuseNS = {it.collect({e -> e.text()?.trim()}).join(', ')}
		this.subjects = fuse(gp.content.subject)
		this.wavebands= fuse(gp.coverage.waveband)
		this.publishers = fuseNS(gp.curation.publisher)
		this.creators = fuse(gp.curation.creator.name)
	}
	
	/** search functionality*/
	/** rewrite query to remove all references to 'ivo://' */
	public static String rewriteQuery(String s) {
		return s?.replaceAll('ivo:', '')
	}
	
	// delegate methods
	public String xpath(String path) {
		return rxml.xpath(path)
	}
	
	public List xpathList(String path) {
		return rxml.xpathList(path)
	}
	
	def propertyMissing(String name) {
		return rxml.propertyMissing(name)
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
