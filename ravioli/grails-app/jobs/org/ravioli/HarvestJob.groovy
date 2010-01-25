package org.ravioli


class HarvestJob {
	
	def harvestService
	def backgroundService
	
	def concurrent = false;
	def group = 'harvest'
	
	static triggers = {
		cron name: 'daily harvest', cronExpression: '0 15 4 ? * *' // 4:14 every day.
	}	
	
	def execute() {
		// execute task
		HarvestResults ro =harvestService.readRofr(true)
		log.info "Checked Rofr for updates: ${ro.created} new, ${ro.modified} modified registries"
		// now list registries
		Registry.list().each { r->
			backgroundService.execute("Listing updates from ${r.ivorn}") {
				log.info "Listing updates from ${r.ivorn}"
				harvestService.harvest(r,true) // which in turn will trigger more background threads..
			}
		}
	}
}
