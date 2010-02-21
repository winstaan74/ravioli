package org.ravioli;


/** a pending task to harvest a registry */
class RegistryHarvestTask extends Task {
	static constraints = {
		resumptionToken(nullable:true, maxSize:1000)
	}

	
	// the registry to harvest from.
	Registry reg
	// flag whether to perform an incremental harvest
	boolean incremental = true
	// a resumptionToken, if we'rte not harvesting from startpoint.
	String resumptionToken

	def regParserService
	
	Outcome run(PrintStream out) {
		def result;
		if (!resumptionToken) {
			// first check that we can identify the registry correctly.
			try {
				regParserService.identify(reg)
			} catch (IdentifyException e) {
				out.println ("Registry ${e.expected} reports itself to be ${e.reported} - proceeding")
			}
			Date now = new Date()
			result = regParserService.listIdentifiers(reg,incremental)
			// now update the date of last harvest. (even though the harvest is still progressing now..
			reg.lastHarvest = now;
		} else {
			result = regParserService.listResumedIdentifiers(reg,resumptionToken)
		}
		if (result.totalSize) {
			out.println  "Harvesting from ${reg.ivorn}, ${result.totalSize} changes found"
		} else if (result.resumptionToken) {
			out.println "Harvesting from ${reg.ivorn}, over ${result.ids?.size()} changes found"
		} else {
			out.println "Harvesting from ${reg.ivorn}, ${result.ids?.size()} changes found"
		}
		// compute how many of these id's we've already seen.
		// and dispatch a background service to harvest that id.
		result.ids?.each { 
			String ivorn = it.ivorn
			def c = Resource.findByIvorn(ivorn)
			if (it.deleted) {
				if (c) {
					c.delete()
				}
			} else {
				def rh = new ResourceHarvestTask(reg:reg, ivorn:ivorn)
				if (!rh.save()) {
					out.println("failed to save resource harvest task")
					rh?.errors?.allErrors.each{
						out.println it
					}
				} 
			}
		}
		// now need to check for a resumption token..
		if (result.resumptionToken) {
			def rh = new RegistryHarvestTask(reg:reg, resumptionToken:result.resumptionToken)
			assert rh.save()
		}
		return Outcome.COMPLETED
	}
	
}
