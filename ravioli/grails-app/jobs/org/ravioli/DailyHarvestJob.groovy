package org.ravioli

/** scheduled job that triggers an incremental harvest of the entire VO
 *  refreshes registry list from rofr
 *  then performs incremntal harvest on each registry
 *  
 *  operates by creating new tasks.
 * @author noel
 *
 */
class DailyHarvestJob {
	
	def harvestService
	
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
			def task = new RegistryHarvestTask(reg:r)
			assert task.save()
		}
	}
}
