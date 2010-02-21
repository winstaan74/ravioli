
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
//search indexes that don't occur in the table - never need to be sorted on, or stored.			
@SearchableDynamicMetaData(name='description', converter='groovy'
			, expression="data.description" , store=Store.NO)

,@SearchableDynamicMetaData(name="all", converter="groovy"
		, excludeFromAll=ExcludeFromAll.YES
		,expression="data.rxml.stripXML()", store=Store.NO )

,@SearchableDynamicMetaData(name='resourcetype', converter='groovy'
		,excludeFromAll=ExcludeFromAll.YES // as part of 'type'
		, expression="data.resourcetype", store=Store.NO )

,@SearchableDynamicMetaData(name="capability", converter="groovy"
		, excludeFromAll=ExcludeFromAll.YES // as part of 'type'
		,expression="data.capability",  store=Store.NO )
	
,@SearchableDynamicMetaData(name="curation", converter="groovy"
	//, excludeFromAll=ExcludeFromAll.YES
	,expression="data.curation", store=Store.NO )

,@SearchableDynamicMetaData(name="name", converter="groovy"
	, excludeFromAll=ExcludeFromAll.YES // as we already have title ad shortname
	,expression="data.name", store=Store.NO )

,@SearchableDynamicMetaData(name="ucd", converter="groovy"
	, excludeFromAll=ExcludeFromAll.YES
	,expression="data.ucd", store=Store.NO )
	
,@SearchableDynamicMetaData(name="col", converter="groovy"
		,excludeFromAll=ExcludeFromAll.YES
		,expression="data.col" , store=Store.NO)

,@SearchableDynamicMetaData(name="type", converter="groovy"
	//, excludeFromAll=ExcludeFromAll.YES 
	,expression="data.type" , store=Store.NO)

// search indexes that do occur in the table - not stored, or sortable.
// however, there needds to be another index for each table column (named with a leading '_') which is sotred and sortable.

,@SearchableDynamicMetaData(name="subject", converter="groovy"
			,expression="data.subject",store=Store.NO )
			
,@SearchableDynamicMetaData(name="waveband", converter="groovy"
	//, excludeFromAll=ExcludeFromAll.YES
		,expression="data.waveband",store=Store.NO)
		
,@SearchableDynamicMetaData(name="creator", converter="groovy"
	, excludeFromAll=ExcludeFromAll.YES // already have curation
		,expression="data.creator"
		,store=Store.NO )
	
,@SearchableDynamicMetaData(name="publisher", converter="groovy"
	, excludeFromAll=ExcludeFromAll.YES // already have curation
		,expression="data.publisher",store=Store.NO )
		
,@SearchableDynamicMetaData(name="title", converter="groovy"
	, expression="data.titleField"
	, excludeFromAll=ExcludeFromAll.NO
	,boost=2.0f
	, store=Store.NO)
		
,@SearchableDynamicMetaData(name="shortname",converter="groovy"
	, expression="data.shortnameField"
	, boost=2.0f
	, excludeFromAll=ExcludeFromAll.NO
	,store=Store.NO)

,@SearchableDynamicMetaData(name="identifier", converter="groovy"
	, excludeFromAll=ExcludeFromAll.NO 
	, boost=2.0f
	,expression="data.ivorn"
	, store=Store.NO )
	
,@SearchableDynamicMetaData(name="source",converter="groovy"
		, expression="data.sourceField" ,store=Store.NO)
		
]) 
class Resource {
	static constraints = {
		ivorn(unique:true, matches:/ivo:\/\/\S+/,maxsize:1000) // must have prefix ivo://
		titleField(nullable:true,maxsize:1000)
		shortnameField(nullable:true, maxsize:100)
		sourceField(nullable:true, maxsize:200)
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
		ivorn column: 'ivorn', sqlType:'varchar(1000)', unique:true
		titleField column: 'title', sqlType:'varchar(1000)'
		wavebands column:'wavebands',sqlType:'varchar(1000)'
		subjects column:'subjects', sqlType:'varchar(1000)'
		publishers column:'publishers',sqlType:'varchar(1000)'
		creators column:'creators',sqlType:'varchar(1000)'
	}

		/** the resource xml - modelled in a separate object
		 *   */	
		ResourceXml rxml= new ResourceXml()

		// used in table row, so must be stored.
		@SearchableId(store=Store.YES)
		Long id
		
		// used in table row.
		@SearchableProperty(name="_ivorn",index=Index.NOT_ANALYZED,store=Store.YES)
		String ivorn // $r/identifier 
		
		String status // not searchable.
		
		// used in table row.
		@SearchableProperty(name="_shortnameField",index=Index.NOT_ANALYZED,store=Store.YES)
		String shortnameField
		
		@SearchableProperty(name="_sourceField",index=Index.NOT_ANALYZED, store=Store.YES) 
		String sourceField
		

		@SearchableProperty(name="_titleField",index=Index.NOT_ANALYZED, store=Store.YES)
		String titleField 
		
		@SearchableProperty(format="yyyy-MM-dd", store=Store.NO)
		Date created
		@SearchableProperty(format="yyyy-MM-dd", store=Store.NO)
		Date modified
		
		// date used in table.
		@SearchableProperty(format="yyyy-MM-dd", store=Store.YES)		
		Date date // combination of modified and created
		
	// pre-formatted fields used in resource table.
	// not indexed: lucene has indexed the raw text elsewhere.
	// but are stored: so that they can be used to populate table from search results.
	// using the dynamic property in the singular - e.g. r.subject, 
	// while r.subjects gives the formatted version.
		@SearchableProperty(name='_subjects',index=Index.NOT_ANALYZED, store=Store.YES)
		String subjects
		@SearchableProperty(name='_wavebands',index=Index.NOT_ANALYZED, store=Store.YES)
		String wavebands
		@SearchableProperty(name='_publishers',index=Index.NOT_ANALYZED, store=Store.YES)
		String publishers
		@SearchableProperty(name='_creators',index=Index.NOT_ANALYZED, store=Store.YES)
		String creators
		


		
		
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
		
		this.titleField = gp.title?.text()?.trim()
		this.shortnameField = gp.shortName?.text()?.trim()
		this.status = gp.'@status'.text()
		this.sourceField = gp.content.source?.text()?.trim()
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
	
	/* return the row of data used in the resource table
	 * returns a map, with a key for each of TABLE_COLUMNS.
	 * used in ExploreController to generate json.
	 *
	 * 
	 */
	Map tableRow() {
		return [
		        _ivorn : ivorn
				,_titleField : titleField
				,_shortnameField: shortnameField ?:""
				,_sourceField: sourceField ?:""
				,_subjects: subjects ?: ""
				,_wavebands: wavebands?: ""
				,_publishers: publishers?: ""
				,_creators: creators?: ""
				,date: date?.format("yyyy-MM-dd")
				,id:id
		]
	}
	
	/** describes the columns that will be available in the client-side YUI table 
	 * 
	 *column keys are chosen to correspond to lucene indexes that allow sorting - i.e. indexes
	 *that are untokenized. The actual db column is sortable on by removing the '_' prefix.
	 * 
	 * some presentation leak in here,(hidden, width, label) sadly, but can't be helped.
	 * */
	static def TABLE_COLUMNS =  [
	                     		//@todo implement mass-selection using checkbox		[key:'check', formatter:'checkbox']
	                    		[key:'_ivorn', label:'IVOA-ID', width:250]
	                    		,[key:'_titleField', label:'Title',  width:250]
	                    		,[key:'_shortnameField',label:'Short Name', hidden:true, width:80]
								,[key:'_sourceField', label:'Source Reference',hidden:true, width:100]
	                    		,[key:'_subjects', label:'Subject', hidden:true,width:250]
	                    		,[key:'_wavebands',label:'Waveband',hidden:true, width:100]
	                    		,[key:'_publishers',label:'Publisher',hidden:true,width:250]
	                    		,[key:'_creators',label:'Creator',hidden:true,width:250]
	                    		,[key:'date', label:'Date', width:80]	
	                    		,[key:'id', label:'ID',hidden:true] // never displayed, used to create links to resources.
	                    		]
	
	
	
		
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
