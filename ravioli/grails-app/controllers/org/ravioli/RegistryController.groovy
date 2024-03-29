package org.ravioli

/**
 * Manages registry objects.
 * All these functions are protected by authentication - only accessible to administrator.
 * @author noel
 *
 */
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
	static defaultAction = 'list'
	
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
			RegistryHarvestTask task = new RegistryHarvestTask(reg:r, incremental:incremental)
			if( task.save() ) { 
				flash.message = "Started harvesting from ${r.name}"
			} else {
				flash.message = 'Unexpected fault when initializing harvest task'
			}
			redirect(action:'list') 
		} else {
			flash.message = "Unknown registry ${harvestId}"
			return [:]
		}
	}
	/** trigger a harvest of all registries */
	def harvestAll = {
		boolean incremental = params.incremental ? true : false; // converts from groovy truth to flag.
		
		Registry.list().each { r->
			RegistryHarvestTask task = new RegistryHarvestTask(reg:r, incremental:incremental)
			assert task.save()
			}
		
		flash.message = 'your office lights may dim while performing this monster re-harvest'		
		redirect(action:'list')
	}
	
	
}
