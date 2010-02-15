// this is a standalone script. mangle the classpath, so we get to the libs we need.
//would be better to use grape for this, but not got network at the moment.
def loader = this.class.classLoader.rootLoader
loader.addURL(new File('/Users/noel/.ivy2/cache/org.grails/grails-core/jars/grails-core-1.2.1.jar').toURL())

// offline - will work once we're online
//@Grab(group='org.grails', module='grails-core', version='1.2.1')
//def fn() {
//}

import groovy.xml.StreamingMarkupBuilder

// can't import - as class not loaded yet./
//import org.codehaus.groovy.grails.plugins.codecs.MD5Codec

//add grails-standard encoding magic..
// classname looked up by string, to work around loading-time issues. wouldn'\t be a problem with grape..
String.metaClass.encodeAsMD5 = {Class.forName('org.codehaus.groovy.grails.plugins.codecs.MD5Codec').encode(delegate)}


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
				  face.accessURL[0]  = "http://127.0.0.1:8080/ravioli/stub-registry/${ivorn.encodeAsMD5()}/"
				}
			}
		}
def outputBuilder = new StreamingMarkupBuilder()
def outFile = new File(basedir,'/rofr/stubRegistries.xml')
outFile.text = outputBuilder.bind{ mkp.yield xml }

