package org.ravioli

/** controller for all user-group related functions */
class UserGroupController {
	def scaffold = true
	def index = {
		redirect action:'list'
	}
	

}
