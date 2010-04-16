
package org.ravioli

import org.codehaus.groovy.grails.commons.*

public class HarvestResults {
	int created;
	int modified;
	int deleted;
	def errors = []
	
}


/** Harvests Resources from Registries
 */
class HarvestService {
	
	
	def regParserService; 
	def backgroundService; 
	
	boolean transactional = true
	
	/** re-queries the registry of registries,
	 *if incremental, then harvest resources that have been modified since last harvest
	 *otherwise,  will harvest all.
	 */
	HarvestResults readRofr(boolean incremental= true) {
		Registry reg = Registry.findByIvorn("ivo://ivoa.net/rofr")
		// lets check that we can identify the rofr
		regParserService.identify(reg) // will throw if not matching.
		Date now = new Date(); 
		HarvestResults hr = new HarvestResults()
		regParserService.parseRofr(reg) { map ->
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
		Rofr rofr = Rofr.getInstance()
		rofr.lastHarvest = now;
		return hr;
	}

		
}
