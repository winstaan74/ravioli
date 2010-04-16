package org.ravioli

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import grails.plugins.nimble.core.Permission;
import grails.plugins.nimble.core.PermissionService;
/**
 *  contains the ajax / remote calls on {@link ResourceLists} and {@link ResourceListBlocks}
 *  separated out into a new controller, as they don't want layouts, while the scaffolded
 *  default CRUD operations do.
 */
class AjaxListController {

	static defaultAction = 'displayBlock'
		
	PermissionService permissionService
	def authenticatedUser
	
	/** display a block, by name
	 * must be public, or current user has view permissions 
	 */
	def displayBlock = { 
		def lc 
		if (params.name) {
			lc = ResourceListBlock.findByName(params.name)
		} else if (params.id) {
			lc = ResourceListBlock.get(params.id)
		} else {
			render status:500, text:"You must provide a 'id' or 'name' parameter"
			return
		}
		if (! lc) {
			render status:404, text:"container '${params.name}/${params.id}' not found"
		} else if (! lc.iCanView()) {
			render status:530, text:"You're not permitted to view this set of lists"
		} else {
			render template:'/resourceListBlock/editable', model:[container:lc]                                                                                       
		}
	}
	
	
	/** access the block for the currently logged in user */
	def displayUserBlock = {
		if (! authenticatedUser) {
			render status:500, text:'Not logged in'
		} else {
			if (! authenticatedUser.profile.userBlock) { // better initialize them
				initializeUserBlock(authenticatedUser)
			}
			ResourceListBlock cont = authenticatedUser.profile.userBlock
			if ( !( cont.iCanView())) { // belt and braces.
				render status:530, text:"You're not permitted to view this set of lists"
				return
			}
			
			render template:'/resourceListBlock/editable', model:[container:cont]
		}
	}
	/**
	 * Create example lists for a newly registered user.
	 * 
	 * seems like authenticatedUser is null in this method - need to pass it through explicitly.
	 * @param user
	 * @return
	 */
	private def initializeUserBlock(user) {
		ResourceListBlock lc = new ResourceListBlock(title:'My Lists')
		.addToLists(new SmartList(query:'title:SDSS', title:'An Example'))
		.addToLists(new SmartList(query:'title:UKIDSS', title:'UKIDSS'))
		.save()
		user.profile.userBlock = lc
		user.profile.save()
		
		def permission = new Permission(target:lc.viewEditPermission(),type:Permission.defaultPerm)
		permissionService.createPermission(permission,user) 
		lc.lists.each {
			permission = new Permission(target:it.viewEditPermission(),type:Permission.defaultPerm)
			permissionService.createPermission(permission,authenticatedUser) 
		}
	}
	
	/** display other lists that the current user can view. */
	def displayOtherBlocks = {
		if (! authenticatedUser) {
			render status:500, text:'No user logged in'
		} else {
			def ids = ResourceListBlock.indexesViewableBy(authenticatedUser)
			ids -= authenticatedUser.profile.userBlock?.id
			if (! ids) { 
				render '' // necessary to render nothing.
				return
			} else {
				def lists = ResourceListBlock.getAll(ids).findAll{ it != null}
				render template:'/resourceListBlock/editable', var:'collection',collection:lists
			}
		}
	}
		
	/** update a list - by ID.*/
	def updateAJAX = {
			if (! authenticatedUser) {
				render status:500, text:'No user logged in'
			} else {
				//@todo
			}
	}
	

}
