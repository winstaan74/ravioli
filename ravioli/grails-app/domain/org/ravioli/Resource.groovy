


package org.ravioli

import org.joda.time.DateTime;
import javax.xml.xpath.*;
import org.compass.annotations.*
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.StreamingMarkupBuilder;

/**
 * 
 * Representation of a voresource.
 * 
 * 3 kinds of property / data element
 * <ul>
 * <li>table row elements - used to display in table row,  precomputed, stored in this class</li>
 * <li>search elements - used once, computed on demand, when indexing with lucene</li>
 * <li>resource detail elements - used to display the resource details. stored within ResourceXml object. </li>
 * </ul>
 * 
 */
@Searchable
@SearchableDynamicMetaDatas(value=[
//search indexes that don't occur in the table - never need to be sorted on, or stored.			
@SearchableDynamicMetaData(name='description', converter='groovy'
			, expression="data.rxml.description" , store=Store.NO)

,@SearchableDynamicMetaData(name="all", converter="groovy"
		, excludeFromAll=ExcludeFromAll.YES
		,expression="data.rxml.stripXML()", store=Store.NO )

,@SearchableDynamicMetaData(name='resourcetype', converter='groovy'
		,excludeFromAll=ExcludeFromAll.YES // as part of 'type'
		, expression="data.rxml.resourcetype", store=Store.NO )

,@SearchableDynamicMetaData(name="capability", converter="groovy"
		, excludeFromAll=ExcludeFromAll.YES // as part of 'type'
		,expression="data.rxml.capability",  store=Store.NO )
	
,@SearchableDynamicMetaData(name="curation", converter="groovy"
	//, excludeFromAll=ExcludeFromAll.YES
	,expression="data.rxml.curation", store=Store.NO )

,@SearchableDynamicMetaData(name="name", converter="groovy"
	, excludeFromAll=ExcludeFromAll.YES // as we already have title ad shortname
	,expression="data.rxml.name", store=Store.NO )

,@SearchableDynamicMetaData(name="ucd", converter="groovy"
	, excludeFromAll=ExcludeFromAll.YES
	,expression="data.rxml.ucd", store=Store.NO )
	
,@SearchableDynamicMetaData(name="col", converter="groovy"
		,excludeFromAll=ExcludeFromAll.YES
		,expression="data.rxml.col" , store=Store.NO)

,@SearchableDynamicMetaData(name="type", converter="groovy"
	//, excludeFromAll=ExcludeFromAll.YES 
	,expression="data.rxml.type" , store=Store.NO)

// search indexes that do occur in the table - not stored, or sortable.
// however, there needds to be another index for each table column (named with a leading '_') which is sotred and sortable.

,@SearchableDynamicMetaData(name="subject", converter="groovy"
			,expression="data.rxml.subject",store=Store.NO )
			
,@SearchableDynamicMetaData(name="waveband", converter="groovy"
	//, excludeFromAll=ExcludeFromAll.YES
		,expression="data.rxml.waveband",store=Store.NO)
		
,@SearchableDynamicMetaData(name="creator", converter="groovy"
	, excludeFromAll=ExcludeFromAll.YES // already have curation
		,expression="data.rxml.creator"
		,store=Store.NO )
	
,@SearchableDynamicMetaData(name="publisher", converter="groovy"
	, excludeFromAll=ExcludeFromAll.YES // already have curation
		,expression="data.rxml.publisher",store=Store.NO )
		
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

// human-readable version of 'capability' - uses same terminology as in the formatted display
,@SearchableDynamicMetaData(name="ability",converter="groovy" //called abilities, as already have 'capability'
		, expression="data.capabilityEncoderService.decode(data.capabilityCode)" 
		, excludeFromAll=ExcludeFromAll.NO
		,store=Store.NO)	
]) 
class Resource {
	
	def capabilityEncoderService
	
	static constraints = {
		ivorn(unique:true, matches:/ivo:\/\/\S+/,maxSize:1000) // must have prefix ivo://
		titleField(nullable:true,maxSize:1000)
		shortnameField(nullable:true, maxSize:100)
		sourceField(nullable:true, maxSize:200)
		created(nullable:false)
		modified(nullable:true)	
		date(nullable:false) // display version of date.
		status(matches:'active') // enforce a constant.
		rxml(unique:true)
		wavebands(nullable:true, maxSize:1000)
		subjects(nullable:true, maxSize:1000)
		publishers(nullable:true, maxSize:1000)
		creators(nullable:true, maxSize:1000)
	}

	static mapping = {
		ivorn column: 'ivorn', sqlType:'varchar(1000)', unique:true
	}

		/** the resource xml - modelled in a separate object
		 *   */	
		ResourceXml rxml= new ResourceXml()

		// used in table row, so must be stored.
		@SearchableId(store=Store.YES)
		Long id
		
		// used in table row.
		// made this un-tokenoized, rather than not-analyzed, 
		// so I can query against exact ivorn matches - used when retrieving static lists.
		@SearchableProperty(name="_ivorn",index=Index.UN_TOKENIZED,store=Store.YES)
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
		
		
		// different requirements here
		// encoding of the 'capabilities' of this resource
		// want it to be available when revived from search indexes
		@SearchableProperty(name='capabilityCode',index=Index.NOT_ANALYZED, store=Store.YES)
		int capabilityCode;

		
		
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
		this.rxml.xml = xml
		def gp = rxml.createSlurper()
		
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

		// initializing this with ?., so it doesn't mess up current unit tests.
		// the capability encoder has it's own unit tests..
		this.capabilityCode = capabilityEncoderService?.encode(gp) ?: 0

	}

	
	/** search functionality*/
	/** rewrite query to escape a few common symbols.
	 * : prefixed by ivo
	 *  - , + where there's no whitespace either side..
	 *  */
	public static String rewriteQuery(String s) {
		return s?.replaceAll(ivoPrefix, /ivo\\:/).replaceAll(nestedOp, /$1\\$2$3/)
	}
	/** patterns used to rewrite queries */
	static final def ivoPrefix = ~/ivo:/
	/** a + or - nested within text - probably in an ivorn, */
	static final def nestedOp = /(\S)([-\+])(\S)/
	
//	
//	// delegate methods
//	public String xpath(String path) {
//		return rxml.xpath(path)
//	}
//	
//	public List xpathList(String path) {
//		return rxml.xpathList(path)
//	}
//	
//	def propertyMissing(String name) {
//		return rxml.propertyMissing(name)
//	}
//	
	
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
				,capabilityCode: capabilityCode
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
	                    		[key:'_titleField', label:'Title',  width:250]
		                    	,[key:'_shortnameField',label:'Short Name', hidden:true, width:80]  
								,[key:'capabilityCode', label:'Capability',width:80,formatter:'@formatCapabilities']
		                    	,[key:'_ivorn', label:'IVOA-ID', width:250]
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
