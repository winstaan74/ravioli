

/*
 * Handles the parsing of harvesting responses.
 * 
 * handles the fetching and parsing side of the problem
 * HarvestSerrvice handles the object creation  / persistence part of the logic
 */

package org.ravioli
import groovy.util.slurpersupport.GPathResult
import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.stax.*;
import javax.xml.transform.stream.*;

/**
 *
 * @author noel 
 */
class RegParserService  {
	
	boolean transactional = false
	
	
	
	/** identify the registry - ie.
	 harvest it's id record, and verify it matches
	 what we've got */
	String identify(Registry r) {
		log.info("Checking Identity of ${r.ivorn}")
		def xml = new XmlSlurper().parse(r.endpoint + "?verb=Identify")
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
		if (rofr.ivorn !='ivo://ivoa.net/rofr') {
			throw new IllegalArgumentException(rofr?.ivorn)
		}
		
		def url= constructQuery(rofr,incremental);
		
		def xml = new XmlSlurper().parse(url)	
		doParseRofr(xml,processor);
		
	}
	
	/** list identifiers of resoruces owned by this registry
	 * 
	 * @param reg the owning registry.
	 * @return list of uri-string.
	 */
	def listIdentifiers(Registry reg, incremental=true) {
		def url = constructQuery(reg,incremental,"?verb=ListIdentifiers&metadataPrefix=ivo_vor&set=ivo_managed");
		def xml = new XmlSlurper().parse(url)
		return xml.ListIdentifiers.header.identifier*.text()
	}
	/** harvest a record from a registry 
	 * 
	 * @param reg registry to harvest from
	 * @param ivorn record id to load
	 * @preturn the GPath of the Resource document, for further processing. - i.e result will have name() == 'Resource'
	 */
	String harvest(Registry reg, String ivorn) {
		URL url = new URL(reg.endpoint + "?verb=GetRecord&metadataPrefix=ivo_vor&identifier=" + ivorn)

		def xslt = '''
		<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
			<xsl:template match="/">
				<xsl:copy-of select="//node()[local-name() = 'Resource']"/>
			</xsl:template>
			<!--
			<xsl:template match="@*|node()">  
				<xsl:copy>    
					<xsl:apply-templates select="@*|node()"/>  
				</xsl:copy>
			</xsl:template>
			-->
		</xsl:stylesheet>
			'''.trim()
		Writer output
		Reader style
		try {
			url.withInputStream{ is ->
				output = new StringWriter();
				style = new StringReader(xslt)
				def transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(style))
				transformer.transform(new StreamSource(is),new StreamResult(output))
				
			}
		} finally {
			output?.close()
			style?.close()
		}
		return output?.toString()
	}
	
	/** take care of constructing the query - either incrmental, or no */
	String constructQuery(Registry rofr, incremental=true,query= "?verb=ListRecords&metadataPrefix=ivo_vor&set=ivoa_publishers") {
		String url = rofr.endpoint + query
		if(incremental && rofr.lastHarvest) { // can onlly incremntal harvest if asked, and we've got a date
			
			url += "&from=" + rofr.lastHarvest.format("yyyy-MM-dd'T'HH:MM:ss'Z'")
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

