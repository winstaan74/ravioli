
package org.ravioli

import grails.plugins.nimble.core.Permission;
import grails.plugins.nimble.core.PermissionService;
/** More information about a user */

class Profile extends grails.plugins.nimble.social.SocialProfileBase {

	static constraints = {
		userBlock nullable:true
	}
	/** a block containing the user's resourceLists */
	ResourceListBlock userBlock 
	
	/** clean up this deleted user's resourceLists */
	def afterDelete() {
		ResourceListBlock.withNewSession {
			userBlock.delete() // cascades to child lists.
			//@todo should remove all permissions for this deleted user.
		}
	}

}
