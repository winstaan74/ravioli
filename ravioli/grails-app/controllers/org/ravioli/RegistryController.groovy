package org.ravioli

//controls the list of known registries.
//use standard scaffolding to list / view / edit registries.
class RegistryController {
	static layout = 'explore'
	static scaffold = true
	
	static navigation = [
	[title:'Registries'
	, group:'admin'
	, order: 10
	, subItems:[
	[action:'list']
	, [action:'harvest', title:'Harvest a registry']					
	, [action:'updateRofr',title:'Update list of registries']
	, [action:'reloadRofr',title:'Reload list of registries']
	//, [action:'sample',title:'Grab a sample of resources']
	]
	]
	]
	
	
	HarvestService harvestService // will be autowired
	def backgroundService; 
	
	def index = {
		redirect(action:'list')
	}
	
	/** update list of registries from the rofr */
	def updateRofr = {
		def hr = harvestService.readRofr(true)
		flash.message = "Harvested ${hr.created} new, ${hr.modified} modified registries from RofR"
		redirect(action:'list')
	}
	
	/** reload list of registries from rofr - total reload */
	def reloadRofr = {
		def results = harvestService.readRofr(false)
		flash.message = "Harvested ${results.created} new, ${results.modified} modified registries from RofR"
		redirect(action:'list')
	}
	
//	def sample = {
//		harvestService.sample();
//		redirect(action:'index')
//	}
	
	
	/** harvest the named registry */
	def harvest = {
		def harvestId = params.harvestId
		if (! harvestId) {
			return [:]
		}
		// incremental is a checkbox value - is either there (checked), or is not.
		boolean incremental = params.incremental ? true : false; // converts from groovy truth to flag.
		log.debug(params.dump())
		Registry r = Registry.findByIvorn(harvestId)
		if (r) {
			def hr = harvestService.harvest(r,incremental)
			flash.message = "Started harvesting from ${r.name} - deleted ${hr.deleted} resources, fetching ${hr.created} new resources, ${hr.modified} modified resources"
			redirect(action:'list') 
		} else {
			flash.message = "Unknown registry ${harvestId}"
			return [:]
		}
	}
	
	def harvestAll = {
		boolean incremental = params.incremental ? true : false; // converts from groovy truth to flag.
		
		Registry.list().each { r->
			backgroundService.execute("Listing updates from ${r.ivorn}") {
				log.info "Listing updates from ${r.ivorn}"
				harvestService.harvest(r,incremental) 
			}
		}
		
		flash.message = 'your office lights may dim while performing this monster re-harvest'		
		redirect(action:'list')
	}
	
	
}
