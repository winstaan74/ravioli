
package org.ravioli;

import java.io.IOError;
import java.io.IOException;

import groovy.util.slurpersupport.GPathResult;

import javax.xml.transform.TransformerException;

/** a pending task to harvest a resource */
class ResourceHarvestTask extends Task {
	
	static constraints = {
		ivorn( matches:/ivo:\/\/\S+/,maxSize:1000) // must have prefix ivo://
		
	}

	
	/** the registry to harves the resource from */
	Registry reg
	/** the resource to harvest*/
	String ivorn // represent this by ivorn, not resource object, as we may not have it yet.
	
	def regParserService
	
	Outcome run(PrintStream out) {
		try {
			out.println "Fetching ${ivorn} from ${reg.ivorn}"
			String xml = regParserService.harvest(reg,ivorn)
			// see if it exists first..
			Resource r = Resource.findByIvorn(ivorn)
			if (r == null) { // it's a new one.
				r = Resource.buildResource(xml,ivorn)
				// check it's an active resource
				if (r.status != 'active') {
					out.println("Resource ${ivorn} is not active : ${r.status} - discarding");
					return Outcome.COMPLETED; // not been saved yet - just bail out.
				}
				out.println ("Created resource for ${ivorn}")
			} else { // update an existing one.
				r.updateFields(xml)
				if (r.status != 'active') {
					out.println("Previously active resource ${ivorn} is now ${r.status} - deleting");
					r.delete()
					return Outcome.COMPLETED
				} else {
					out.println "Updated resource for ${ivorn}"
				}
			}
			if (r.validate() ) {
				r.save(flush:true)
				return Outcome.COMPLETED
			} else {
				out.println "Failed to save ${ivorn}"
				r?.errors?.allErrors.each {
					out.println it
				}
				return Outcome.FAILED
			}
		} catch (IdentifyException e) {
			out.println(e.message)
			return Outcome.FAILED
		} catch (UnknownResourceException e) {
			out.println(e.message)
			return Outcome.FAILED // no point retrying
		} catch (HarvestServiceException e) {
			out.println(e.message)
			return Outcome.FAILED 
		
		} catch (IOException e) {
			out.println(e.message)
			return Outcome.ERROR // may be worth retrying
		
		} catch (TransformerException e) {
			def cause = e.getCause()
			switch(cause) {
				case HarvestServiceException:
					out.println(cause.message)
					return Outcome.FAILED
				case null:
					out.println(e.messsage)
					return Outcome.ERROR
				default:
					out.println(cause.message)
					return Outcome.ERROR
			}
		}
		
	}
}
