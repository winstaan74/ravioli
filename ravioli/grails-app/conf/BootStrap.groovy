
import org.springframework.context.ApplicationContext;
import org.codehaus.groovy.grails.commons.*
import org.ravioli.*
import grails.util.Environment;
import groovy.util.XmlParser;

import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;


class BootStrap {
	
	def regParserService 
	def searchableService
	ApplicationContext ctx
	def init = { servletContext ->
		ctx = servletContext.getAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT)
		
		switch (Environment.current) {
			case Environment.DEVELOPMENT:
				populateRegistries()
				populateResources(1..<140)
			break;
			
			case Environment.TEST: // integration tests.
				populateRegistries()
				populateResources([5,15,25,35,45,55,65,75,85,95,92]) // smaller set of resources.
				break;
			default:
				populateRegistries()
		}
	}
	/** load resources from disk
	 * 
	 * @param range rang between 1 and 143, which lists xml files to populate db with.
	 * @param log
	 * @return
	 */
	private populateResources(range) {
		searchableService.stopMirroring()
		range.each { ix -> // we have 143, but want to reduce startup time.
			try { // don't want a failure in one of these to halt entire load.
				ctx.getResource("classpath:/exampledata/sample/${ix}.xml").getInputStream().with { is ->
					Resource r = Resource.buildResource(is.text);
					if (r.validate()) {
						r.save();
					} else {
						log.error(r.errors)
					}
				}
			} catch (Throwable t) {
				log.error("Failed to load ${ix}.xml, ${t.getMessage()}");
			}
		}
		searchableService.startMirroring()
		searchableService.index()
	}
	
	/**
	 * load registry information from disk.
	 * @return
	 */
	private populateRegistries() {
		ctx.getResource("classpath:/exampledata/rofrRegistries.xml").getInputStream().with { is ->
			def xml = new XmlSlurper().parse(is)
			regParserService.doParseRofr(xml) { m ->
				//if (m.ivorn != rofr.ivorn) {
				new Registry(m).save()
				//}
			}
		}
	}
	def destroy = {
	}
} 