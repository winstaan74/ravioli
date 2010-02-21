



/*
 * Handles the parsing of harvesting responses.
 * 
 * handles the fetching and parsing side of the problem
 * HarvestSerrvice handles the object creation  / persistence part of the logic
 */

package org.ravioli
import groovy.util.slurpersupport.GPathResult

/**
 *
 * @author noel 
 */
class RegParserService  {
	
	def xmlService // link to xml parsing support.
	boolean transactional = false
	
	/** log an url and it's content, if log.debug level is enabled.. */
	private void debugUrl(def url) {
		if (log.debugEnabled) {
			log.debug url
			log.debug new URL(url).text
		}
	}
	
	/** identify the registry - ie.
	 harvest it's id record, and verify it matches
	 what we've got */
	String identify(Registry r) {
		log.info("Checking Identity of ${r.ivorn}")
		def url = r.endpoint + "?verb=Identify"
		debugUrl(url)
		def xml = new XmlSlurper().parse(url)
		def reportedIvorn = parseIdentify(xml)
		log.info("Reports itself to be '${reportedIvorn}'" )
		if (reportedIvorn == null || reportedIvorn.trim() != r.ivorn) {
			throw new IdentifyException(reported:reportedIvorn,expected:r.ivorn)
		}
		return reportedIvorn
		
	}
	
	/** parse the list of registries from rofr
	 * @param rofr - must be rofr.
	 */
	void parseRofr(Registry rofr,incremental=true,Closure processor) {
		log.info("Parsing Rofr")
		if (rofr.ivorn !='ivo://ivoa.net/rofr') {
			throw new IllegalArgumentException(rofr?.ivorn)
		}
		
		def url= constructRofrQuery(rofr,incremental);
		debugUrl(url)
		def xml = new XmlSlurper().parse(url)	
		log.debug "Passing to processor"
		doParseRofr(xml,processor);
		
	}
	
	/** list identifiers of resoruces owned by this registry
	 * 
	 * @param reg the owning registry.
	 * @return Map structure, with the fields 
	 * resumptionToken - null, or string to use to resume
	 * result - list of maps of identifier and deleted flag.
	 */
	def listIdentifiers(Registry reg, incremental=true) {
		log.info("Listing Identifier for ${reg.ivorn}, ${incremental}")
		def url = constructListQuery(reg,incremental);
		return parseListIdentifiers(url)
	}
	
	def listResumedIdentifiers(Registry reg, String token) {
		log.info("Resuming List Identifier for ${reg.ivorn}")
		def url = constructResumeListQuery(reg,token)
		return parseListIdentifiers(url)
	}
	
	private String constructResumeListQuery(Registry reg, String token) {
		return reg.endpoint + "?verb=ListIdentifiers&resumptionToken=" + token.encodeAsURL()
	}
	
	private parseListIdentifiers(String url) {
		debugUrl(url)
		def xml = new XmlSlurper().parse(url)
		def ids = xml.ListIdentifiers.header.collect { h->
			[
			ivorn: h.identifier.text()
			, deleted: h.'@status' == 'deleted' 
			]
		}
		def result = parseResumptionToken(xml.ListIdentifiers.resumptionToken)
		result.ids = ids
		if (log.debugEnabled) {
			log.debug (result.dump())
		}
		return result
	}
	
	/** helper method - expects to be passed the xml of the resumption token element, 
	 * and returns a map of the parsed bits.
	 * @param xml
	 * @return
	 */
	private Map parseResumptionToken(def resumptionToken) {
		assert resumptionToken.name() == 'resumptionToken',"input must be the resumption token, but was ${resumptionToken.dump()}"
		def sz
		try {
			sz = resumptionToken?.'@completeListSize'?.toInteger() 
		} catch (NumberFormatException e) {
			// no matter.
		}
		def rt = resumptionToken?.text().trim()
		return [
		        resumptionToken: rt ?: null // takes empty string to null
		        ,totalSize: sz
		]
	}
	
	/** harvest a record from a registry 
	 * 
	 * @param reg registry to harvest from
	 * @param ivorn record id to load
	 * @preturn the GPath of the Resource document, for further processing. - i.e result will have name() == 'Resource'
 	* @todo spend some time debugging behaviour of url encoding of ivorn parameter.
	 */
	String harvest(Registry reg, String ivorn) {
		log.info "Harvesting ${ivorn} from ${reg.ivorn}"
		def url = reg.endpoint + "?verb=GetRecord&metadataPrefix=ivo_vor&identifier=" + ivorn
		debugUrl(url)
		// test for occurrence of error, and if see it, halt with a transform exception
		// else just cut out the resource part of the response.
		def xslt = '''
		<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
			xmlns:java="http://xml.apache.org/xalan/java"
				exclude-result-prefixes="java"
			version="1.0">
			<xsl:template match='/'>
			<xsl:if test="//node()[local-name() = 'error']">
				<xsl:value-of 
				select='java:org.ravioli.HarvestServiceException.throwException(//node()[local-name() = "error"])' />
			</xsl:if>
			<xsl:copy-of select="//node()[local-name() = 'Resource']"/>
			</xsl:template>
		</xsl:stylesheet>
			'''.trim()
		Writer output = new StringWriter();
		output.withWriter {
			xmlService.transform(xslt,new URL(url),output)
		}
		return output.toString();
	}
	
	/** construct the reg of reg list records query. - either incrmental, or no */
	private String constructRofrQuery(Registry r, incremental=true,query= "?verb=ListRecords&metadataPrefix=ivo_vor&set=ivoa_publishers") {
		String url = r.endpoint + query
		def rofr = Rofr.getInstance()
		if(incremental && rofr.lastHarvest) { // can onlly incremntal harvest if asked, and we've got a date
			
			url += "&from=" + rofr.lastHarvest.format("yyyy-MM-dd'T'HH:MM:ss'Z'")
		}
		return url;
	}
	
	/** consturct a listId query - either incrmental, or no */
	private String constructListQuery(Registry r, incremental=true,query= "?verb=ListIdentifiers&metadataPrefix=ivo_vor&set=ivo_managed") {
		String url = r.endpoint + query
		if(incremental && r.lastHarvest) { // can onlly incremntal harvest if asked, and we've got a date
			
			url += "&from=" + r.lastHarvest.format("yyyy-MM-dd'T'HH:MM:ss'Z'")
		}
		return url;
	}
	
	
	
	
	/** parse the response of the 'identify' verb,
	 returns the reported identifier */
	private String parseIdentify(GPathResult xml) {
		return  xml?.Identify?.description?.Resource?.identifier?.text()
	}
	
	/** parse the 'list registries' response from
	 the rofr
	 @param processor - a closure to process for each
	 item - will be passed a map of parsed items
	 */
	public void doParseRofr(GPathResult xml, Closure processor) {
		xml.ListRecords.record.metadata.
		Resource.findAll{it.'@xsi:type' =~ 'Registry'}.
		each { r ->
			def map = [
			ivorn : r?.identifier?.text()
			,name: r?.title?.text()
			,manages: r?.managedAuthority?.list()*.text()
			]
			r.capability.findAll{ cap ->
				cap.'@standardID' =~"ivo://ivoa.net/std/Registry" &&
				cap.'@xsi:type' =~ 'Harvest'
			}.each{ cap ->
				cap.interface.findAll {face ->
					face.'@xsi:type' =~ 'OAIHTTP' &&
					( face.'@version' =~ '1.0' || face.'@version' =~ "") &&
					( face.'@role' =~ 'std' || face.'@role' =~ "")
				}.each { face ->
					map['endpoint'] = face.accessURL?.toString()
				}
			}
			processor.call(map)
		}
	}
}

