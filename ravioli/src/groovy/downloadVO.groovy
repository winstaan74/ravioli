// enable when we're online.
//@Grab(group='org.grails', module='grails-core', version='1.2.1')
//def fn() {
//}

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;
import org.codehaus.groovy.grails.plugins.codecs.MD5Codec
// add grails-standard encoding magic..
String.metaClass.encodeAsMD5 = {org.codehaus.groovy.grails.plugins.codecs.MD5Codec.encode(delegate)}


// script to download the whole vo.

def basedir = new File('/Users/noel/tinkering/mock-registry')
def reglist = new File(basedir,'/rofr/list.xml')

assert basedir.exists(), 'cant find basedir'
assert reglist.exists(), 'cant find reglist'

def xml = new XmlSlurper().parse(reglist)
doParseRofr(xml) { ivo, url ->
	println "Processing Registry ${ivo}"
	try {
		def regDir = new File(basedir,ivo.encodeAsMD5())
		regDir.mkdir()
		def wgetFile = new File(regDir,"wget.sh")
		wgetFile.withPrintWriter{ writer ->
			println "Identifying.."
			def idUrl= url + "?verb=Identify"
			def idFile = new File(regDir,'identify.xml')
			//writer.println("wget ${idUrl} -O ${idFile} -nc")
			wget(idUrl, idFile)
			println "Listing..."
			def listFile = new File(regDir,'list.xml')
			wget(url + "?verb=ListIdentifiers&metadataPrefix=ivo_vor&set=ivo_managed",listFile)
			// now process the list file..
			while(true) {
				def listXML = new XmlSlurper().parse(listFile)
				listXML.ListIdentifiers.header.identifier*.text().each { resourceId ->
					try {
						// grab the resource
						def resourceUrl = url + "?verb=GetRecord&metadataPrefix=ivo_vor&identifier=" + resourceId
						def resourceFile = new File(regDir,URLEncoder.encode(resourceId))
						//save(resourceUrl,resourceFile)
						writer.println "wget '${resourceUrl}' -O '${resourceFile}' -nc"
					} catch (e) {
						println "Failed to download resource ${resourceId} from ${ivo}"
						e.printStackTrace()
					}
				}
				// check for resumption token..
				def tok = listXML.ListIdentifiers.resumptionToken?.text().trim() ?: null
				if (! tok) {return;} // we're finished with this registry.	
				def encodedTok = URLEncoder.encode(tok)
				listFile = new File(regDir,"list-${encodedTok}.xml")
				println "Listing continuation..."
				wget(url + "?verb=ListIdentifiers&resumptionToken=" + encodedTok,listFile)
			}
		}
	} catch (e) {
		println "Failed to process registry ${ivo}"
		e.printStackTrace()
	}
}

// supporting fns.
public void save(String u, File f) {
	f .withWriter { writer ->
		new URL(u).withReader { reader ->
			writer << reader  // feck, that's nice, compared to java..
		}
	}
}

public void wget(String u, File f) {
	def sout = new StringBuffer()
	def serr = new StringBuffer()
	Process proc = "wget ${u} -O ${f} -nc".execute()
	proc.consumeProcessOutput(sout,serr)
	proc.waitForOrKill(5 * 60 * 1000)
	println "out> $sout err $serr"
}

// copied from RegParserService - not on classpath.
public void doParseRofr(def xml, Closure processor) {
	xml.ListRecords.record.metadata.
	Resource.findAll{it.'@xsi:type' =~ 'Registry'}.
	each { r ->
		def ivorn =  r?.identifier?.text()
		def endpoint
		r.capability.findAll{ cap ->
			cap.'@standardID' =~"ivo://ivoa.net/std/Registry" &&
			cap.'@xsi:type' =~ 'Harvest'
		}.each{ cap ->
			cap.interface.findAll {face ->
				face.'@xsi:type' =~ 'OAIHTTP' &&
				( face.'@version' =~ '1.0' || face.'@version' =~ "") &&
				( face.'@role' =~ 'std' || face.'@role' =~ "")
			}.each { face ->
				endpoint = face.accessURL?.toString()
			}
		}
		processor.call(ivorn,endpoint)
	}
}