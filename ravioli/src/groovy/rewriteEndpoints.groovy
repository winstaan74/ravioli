import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.XmlUtil;
import groovy.xml.StreamingMarkupBuilder

// script to rewrite endpoints in rofr file, so it can be used for testing.
// used to create stub registries file, that can then be copied into sampledata folder.

def basedir = new File('/Users/noel/tinkering/mock-registry')
def reglist = new File(basedir,'/rofr/list.xml')

assert basedir.exists(), 'cant find basedir'
assert reglist.exists(), 'cant find reglist'

def xml = new XmlSlurper().parse(reglist)

xml.ListRecords.record.metadata.Resource. // work around - namespace sensitive.
		findAll{it.'@xsi:type' =~ 'Registry'}.
		each { r ->
			def ivorn =  r?.identifier?.text()
			r.capability.findAll{ cap ->
				cap.'@standardID' =~"ivo://ivoa.net/std/Registry" &&
				cap.'@xsi:type' =~ 'Harvest'
			}.each{ cap ->
				cap.interface.findAll {face ->
					face.'@xsi:type' =~ 'OAIHTTP' &&
					( face.'@version' =~ '1.0' || face.'@version' =~ "") &&
					( face.'@role' =~ 'std' || face.'@role' =~ "")
				}.each { face ->
					def encoded = URLEncoder.encode(ivorn)
					def newURL = "http://127.0.0.1:8080/ravioli/stub-registry/${encoded}/"
					face.accessURL[0] = newURL
				}
			}
		}
def outputBuilder = new StreamingMarkupBuilder()
def outFile = new File(basedir,'/rofr/stubRegistries.xml')
outFile.text = outputBuilder.bind{ mkp.yield xml }

