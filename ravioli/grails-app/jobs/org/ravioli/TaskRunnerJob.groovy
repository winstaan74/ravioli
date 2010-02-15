package org.ravioli

/** job that triggers processing of the task queue */
class TaskRunnerJob {
	
	static triggers = {
		simple name:'every minute', 
			startDelay:1000, 
			repeatInterval:60000
	}
	def group = 'tasks'
	def concurrent = false // for concurrency, would have to add task locking in db.
	def taskService
	
	def execute() {
		log.info("Processing Task Queue")
		try {
			// execute task
			boolean flag= true
			while(flag) {
				Task.withNewSession { x ->
					flag = taskService.runNextTask()
				}
			}
			log.info("Drained Task Queue")
		} catch (e) {
			e.printStackTrace()
			log.error("Unexpected Exception from taskService",e)
		}
	}
}
