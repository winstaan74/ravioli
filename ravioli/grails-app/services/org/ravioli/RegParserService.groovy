



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
	 * @return list of uri-string.
	 */
	def listIdentifiers(Registry reg, incremental=true) {
		log.info("Listing Identifier for ${reg.ivorn}, ${incremental}")
		def url = constructListQuery(reg,incremental);
		debugUrl(url)
		def xml = new XmlSlurper().parse(url)
		def result =  xml.ListIdentifiers.header.identifier*.text()
		if (log.debugEnabled) {
			log.debug (result.dump())
		}
		return result
	}
	/** harvest a record from a registry 
	 * 
	 * @param reg registry to harvest from
	 * @param ivorn record id to load
	 * @preturn the GPath of the Resource document, for further processing. - i.e result will have name() == 'Resource'
	 */
	String harvest(Registry reg, String ivorn) {
		log.info "Harvesting ${ivorn} from ${reg.ivorn}"
		def url = reg.endpoint + "?verb=GetRecord&metadataPrefix=ivo_vor&identifier=" + ivorn
		debugUrl(url)
		def xslt = '''
		<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
			<xsl:template match="/">
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

