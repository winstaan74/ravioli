package org.ravioli

import org.apache.shiro.SecurityUtils;

import grails.plugins.nimble.core.Group;
import grails.plugins.nimble.core.Permission;
import grails.plugins.nimble.core.Role;

/** a load of helper methods for  an object that provides permissioning support
 was a mixin, but these don't seem to work well with domain objects...
 
 * @author noel
 *
 */
class Permissionable {
	
	/** augment our domain classes with some methods about permissions 
	 * 
	 * each class is expected to have a static variable called 'permissionName'
	 * */
	static def mixinMethods(domainClasses) {
		
		for (d in domainClasses) {
			/* the name of the mermission that the actor must have to edit/delete this resource */
			d.metaClass.editPermission << { ->
				"${delegate.permissionName}:edit:${delegate.id}"		
			}
			
			/** the name of the permission that the actor must have to view this resource */
			d.metaClass.viewPermission << { ->
				"${delegate.permissionName}:view:${delegate.id}"
			}
			
			d.metaClass.viewEditPermission << { ->
				"${delegate.permissionName}:view,edit:${delegate.id}"
			}
			
			d.metaClass.'static'.listEditableBy << { rug ->
				delegate.getAll (indexesBy (rug, delegate, "edit"))
			}
			
			d.metaClass.'static'.listViewableBy << { rug ->
				delegate.getAll (indexesBy (rug, delegate, "view"))
			}
			
			d.metaClass.'static'.indexesViewableBy << { rug ->
				indexesBy(rug, delegate, 'view')
			}
			
			d.metaClass.'static'.indexesEditableBy << { rug ->
				indexesBy(rug,delegate,'edit')
			}
			
			// check whether currently logged in user can view this object
			// if th object has a 'isPublic' flag, this is first checked
			// and failing, permissions are then checked.
			d.metaClass.iCanView << { ->
				( delegate.hasProperty('everyone') && delegate.everyone ) ||
				SecurityUtils.subject.isPermitted(viewPermission())			
			}
			
			// check whether the currently logged inuser has permission to edit this object.
			d.metaClass.iCanEdit << { ->
				SecurityUtils.subject.isPermitted(editPermission())
			}
			
			
		} // end for loop.
		
	} // end addMixin
	
	/** implementation */
	private static def indexesBy(rug, del, action) {
		def l = Permission.withCriteria {
			like('target',"${del.permissionName}:%${action}%:%")
			switch( rug) {
				case User:
					or {
						eq('user',rug) // it's a permission for this user.
						if (rug.groups) {
							'in'('group',rug.groups) // a permission for one of this user's groups.
						}
						def rs = rug.roles + rug.groups*.roles
						if (rs) {
							'in'('role', rs) 
						}
					}
					break;
				case Group:
					or {
						eq('group',rug)
						if (rug.roles) {
							'in' ('role',rug.roles)
						}
					}
					break;
				case Role:
					eq('role',rug)
					break;
				default:
					throw IllegalArgumentException ("must pass in a User, Group or Role")
				
			}
			projections {
				property 'target'
			}
		}
		return l.collect{  
			it.substring(1+it.lastIndexOf(':')).tokenize(',')*.toInteger()
		}.flatten().unique()
	}
	
	
}
