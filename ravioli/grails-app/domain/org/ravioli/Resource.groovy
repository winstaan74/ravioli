package org.ravioli

import groovy.util.XmlSlurper;

class Resource {
	static searchable = true
	static constraints = {
		ivorn(unique:true, matches:/ivo:\/\/\S+/) // must have prefix ivo://
		xml(nullable:false)
		title(nullable:true)
		source(nullable:true)
		description(nullable:true)
		resourcetype(nullable:false)
		subject(nullable:false)
		waveband(nullable:false)
		capability(nullable:false)
		creator(nullable:false)
		publisher(nullable:false)
	}
		String ivorn // $r/identifier - need to map to 'id'
		String xml // the text itself. would maybe want to load this lazily.
		String resourcetype //$r/@xsi:type
		String title //$r/title
		String source // $r/content/source
		String description // $r/content/description
		
		static hasMany = [
		      subject:String  // $r/content/subject
		      , waveband: String //$r/coverage/waveband
		      , capability: String //$r/capability/@xsi:type, $r/capability/@standardID
				// maybe represent capability as an object, containing access urls too.
		      , creator: String //$r/curation/creator/name $r/curation/creator/name/@ivo-id
			  , publisher: String // $r/curation./publisher $r/curation/publisher/@ivo-id
		]

		// @todo
		// default - "$r/title","$r/identifier","$r/shortName","$r/content/subject","$r/content/description"
			// have all these apart from shortName - maybe some kind of synthesized Q.
		// any, all $r//*
		// curation - $r/curation//* - represent as a separate object? - or as  synthesized Q of creator, publisher and contributor (currently missing)

	
		
		/** parse a url containing XML into a resource */
	static Resource buildResource(String xml, String ivorn) {
		Resource r = new Resource(xml:xml, ivorn:ivorn)
		// parse up the rest
		r.updateFields(xml)
		return r;
		
	}

	/** parse xml and update most of the fields of this resource from it */
	void updateFields(String xml) {
		this.xml = xml;
		def gp = new XmlSlurper().parseText(xml)
		
		this.resourcetype = removePrefix(gp.'@xsi:type'?.text()?.trim())
		
		this.title = gp.title?.text()?.trim()
		
		String source =  gp.content.source?.text()?.trim()
		this.source = source?.size() > 0 ? source : null
		
		this.description = gp.content.description?.text()?.trim()
		
		this.subject = gp.content.subject?.list()*.text()*.trim()
		
		this.waveband = gp.coverage.waveband?.list()*.text()*.trim()*.toLowerCase()
		
		this.capability = gp.capability.'@xsi:type'.list().collect {removePrefix(it.text()?.trim())} 
		this.capability.addAll(gp.capability.'@standardID'.list()*.text()*.trim())
		
		this.creator = gp.curation.creator.name?.list()*.text()
		this.creator.addAll(gp.curation.creator.'@ivo-id'?.list()*.text())
		
		this.publisher = gp.curation.publisher.'@ivo-id'?.list()*.text()
		this.publisher.addAll(gp.curation.publisher?.list()*.text()*.trim())
	}
	
	private String removePrefix(String rt) {
		int ix =rt.indexOf(':')
		return ix != -1 ? rt.substring(ix+1) : rt
	}
	
		
/*
 *Other Search positions used by voexplorer
 * '*' - present in smartlist building ui
 * '+' - present in filterwheels
 * @ - incorporated into this code.
 
		targets.put("shortname", new String[] {"$r/shortName"});
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
