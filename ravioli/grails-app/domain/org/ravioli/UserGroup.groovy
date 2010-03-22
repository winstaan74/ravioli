package org.ravioli

import grails.plugins.nimble.core.Group;

/** wraps a nimble gropu with some additional fields.
 * can't seem to make it depend on the nimble group - so will just have to manage 
 * this by hand.
 * */
class UserGroup {

    static constraints = {
		title(blank:false)
		description(maxSize:100000) 
		group unique:true
    }

    static mapping = {
		cache: true
    }
	
	String title
	String description
	Group group

	
}
