package org.ravioli

/** base class for a list of resources 
 * 
 * @see BookmarkList
 * @see SmartList
 * */

class ResourceList {
 
    static constraints = {
		icon(nullable:true)
    }
	
    static permissionName = 'list'
    
	static belongsTo = [container:ResourceListBlock]
	/** display title for ths list*/
	String title
	/** classname of an icon (optional) */
	String icon
	
	/** flag indicating whether this list container is publically visible */
	boolean everyone
}
