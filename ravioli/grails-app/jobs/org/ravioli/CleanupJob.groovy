package org.ravioli

/** runs daily to clean up completed jobs more than a day old.
 * failed jobs are left, to be deleted manually
 * @author noel
 *
 */
class CleanupJob {
	
	static triggers = {
		cron name: 'daily cleanup', cronExpression: '0 1 4 ? * *' // 4:01 every day.
	}	
	
    def execute() {
		Date yesterday = new Date() - 1 // why can't Java be this good?
		def l = Task.findAllByCompletedAndCreationLessThan(true,yesterday)
		l*.delete()
    }
	
	def concurrent = false;
	def group = 'tasks'
}
