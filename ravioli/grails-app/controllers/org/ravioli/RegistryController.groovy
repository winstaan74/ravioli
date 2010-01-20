package org.ravioli

//controls the list of known registries.
//use standard scaffolding to list / view / edit registries.
class RegistryController {
    static scaffold = true

    static navigation = [
         [title:'Registries'
		 , group:'tabs'
		 , order: 10
         , subItems:[
                     [action:'list']
					, [action:'harvest', title:'Harvest a registry']					
					, [action:'updateRofr',title:'Update list of registries']
                     , [action:'reloadRofr',title:'Reload list of registries']
                        , [action:'sample',title:'Grab a sample of resources']
                    ]
		]
    ]
    
    HarvestService harvestService // will be autowired

    def index = {
        redirect(action:'list')
    }

    /** update list of registries from the rofr */
    def updateRofr = {
        def hr = harvestService.readRofr()
		flash.message = "Harvested ${hr.created} new, ${hr.modified} modified registries from RofR"
        redirect(action:'list')
    }

    /** reload list of registries from rofr - total reload */
    def reloadRofr = {
        def results = harvestService.readRofr(false)
        flash.message = "Harvested ${results.created} new, ${results.modified} modified registries from RofR"
        redirect(action:'list')
    }

    def sample = {
		harvestService.sample();
		redirect(action:'index')
	}
		

    /** harvest the named registry */
    def harvest = {
    	def harvestId = params.harvestId
		if (! harvestId) {
			return [:]
		}
		def incremental = params.incremental
		try {
			Registry r = Registry.findById(harvestId)
			harvestService.harvest(r,incremental)
			flash.message = 'Started harvesting from '+  r.name + ' - results being fetched now'
			redirect(action:'list') // @todo report the progress of the harvest
		} catch(e) {
			return [harvestError: e]
		}
    }


}
