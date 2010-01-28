
package org.ravioli

import org.codehaus.groovy.grails.commons.*

public class HarvestResults {
	int created;
	int modified;
	def errors = []
	
}


/** service that takes care of the harvesting
 side of things
 */
class HarvestService {
	
	
	def regParserService; 
	def backgroundService; // COOL!!
	
	boolean transactional = true
	
	/** re-queries the registry of registries,
	 *if incremental, then harvest resources that have been modified since last harvest
	 *otherwise,  will harvest all.
	 */
	HarvestResults readRofr(boolean incremental= true) {
		Registry rofr = Registry.findByIvorn("ivo://ivoa.net/rofr")
		// lets check that we can identify the rofr
		regParserService.identify(rofr) // will throw if not matching.
		Date now = new Date(); 
		HarvestResults hr = new HarvestResults()
		regParserService.parseRofr(rofr) { map ->
			//first create a Registry object form the map, to see if 
			// it\s valid...
			Registry trial = new Registry(map)
			if (!trial.validate()) {
				def err = trial.errors
				// harrumph - doesn't work - id may not be unique.. work around this..
				if (! (err.errorCount == 1 
				&& err.fieldError.field == 'ivorn' 
				&& err.fieldError.code == 'unique'
				)) { 	
					hr.errors.add(trial.errors)
					log.warn( "Failed to create registry: " + trial.errors )
					return
				}
			}
			// ok, input is valid - let's create or modify..
			Registry r = Registry.findByIvorn(map.ivorn)
			if (r) { // update
				hr.modified++
				r.name = map.name
				r.manages = map.manages 
				r.endpoint = map.endpoint
			} else { // create
				hr.created++
				trial.save()
			}
			
		}
		
		rofr.lastHarvest = now;
		return hr;
	}
	
	/** harvests resources from the specified registry, 
	 * populating the resources table
	 * @param r
	 */
	HarvestResults harvest(Registry reg, incremental = true) {
		// first check that we can identify the registry correctly.
		regParserService.identify(reg)
		Date now = new Date()
		HarvestResults hr = new HarvestResults()
		def ids = regParserService.listIdentifiers(reg,incremental)
		log.info "Harvesting from ${reg.ivorn}, ${ids?.size()} changes found"
		// compute how many of these id's we've already seen.
		// and dispatch a background service to harvest that id.
		ids.each { ivorn ->
			def c = Resource.findByIvorn(ivorn)
			if (c) { // already exists
				hr.modified++
			} else {
				hr.created++
			}
			backgroundService.execute("Harvesting " + ivorn) {
				harvestResource(reg,ivorn)
			}
		}
		// now update the date of last harvest. (even though the harvest is still progressing now..
		reg.lastHarvest = now;
		return hr 
		
	}
	
	/** harvest a resource and ingest it into our database 
	 * 
	 */
	def harvestResource(Registry reg, String ivorn) {
		try {
			log.info "Fetching ${ivorn} from ${reg.ivorn}"
			String xml = regParserService.harvest(reg,ivorn)
			// see if it exists first..
			Resource r = Resource.findByIvorn(ivorn)
			if (r == null) { // it's a new one.
				r = Resource.buildResource(xml,ivorn)
				log.info("Created resource for ${ivorn}")
			} else { // update an existing one.
				r.updateFields(xml)
				log.info "Updated resource for ${ivorn}"
			}
			
			if (r.validate()) {
				r.save()
			} else {
				log.error "Failed to save ${ivorn}: ${r?.errors}"
			}
		} catch (e) {
			log.error "Exception when harvesting ${ivorn} from ${reg.ivorn}",e
		}
	}
	
	/** method to harvest a sample from all known registries, for testing. */
	def sample() {
		// first update the list of registreis from rofr
		readRofr()
		File root = new File("/tmp/sample")
		root.mkdirs();
		// now for each registry
		int i = 0;
		Registry.list().each { r ->
			def ids = regParserService.listIdentifiers(r,false); // not incremental.
			// shuffle, and take the first 10 ids.
			Collections.shuffle(ids)
			int end = Math.min(ids.size(), 10)
			ids[0..<end].each{ ivo ->
				//String filename = 
				i++
				File out = new File(root,"${i}.xml")
				out.text = regParserService.harvest(r,ivo)
			}
		}
	}
	
	
	
	
	
	
}
