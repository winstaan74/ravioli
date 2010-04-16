package org.ravioli
/** an ordered set of {@link ResourceList}, grouped in a block */
class ResourceListBlock {

	
	static constraints = {
		icon(nullable:true)
		name(nullable:true)
	}
	
	static permissionName = 'block'
	
	static mapping = {
		//		lists fetch:'join' // make the lists association eager., but breaks scaffolding list.
		cache 'nonstrict-read-write'
	}
	
	/** title to display in ui */
	String title
	
	/** a name for the list - used within views to reference to it */
	String name
	/** classname of an icon (optional)
	 */
	String icon
	
	/** flag indicating whether this list container is publically visible */
	boolean everyone
	
	/** the resource lists this block contains .
	 * not actually necessary to define this variable, but I've typed it as 'List' so 
	 * that the ordering of lists is maintained (else would defauylt to set)
	 */
	List lists 
	static hasMany = [ lists: ResourceList]
	
}
