package org.ravioli

//controls the list of known registries.
//use standard scaffolding to list / view / edit registries.
class RegistryController {
    static scaffold = true

    HarvestService harvestService // will be autowired

    def index = {
      //  redirect(action:'list')
		// got an index file now.
    }

    /** update list of registries from the rofr */
    def updateRofr = {
		//@todo do something with the results.
        def results = harvestService.readRofr()
        redirect(action:'index')
    }

    /** reload list of registries from rofr - total reload */
    def reloadRofr = {
        def results = harvestService.readRofr(false)
        redirect(action:'index')
    }

    def sample = {
		harvestService.sample();
		redirect(action:'index')
	}
		

    /** harvest the named registry */
    def harvest = {

    }


}
