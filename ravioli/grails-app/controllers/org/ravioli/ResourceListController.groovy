package org.ravioli
/** controller that manages {@link ResourceLists}.
 * 
 */
class ResourceListController {
	static layout = 'explore'
	static scaffold = true;
	static navigation = [
	                     title:'Lists'
	                    ,group:'admin'
	                    ,order:50
	                    ,subItems:[
	                               [action:'list']
						]
	]
	
	
	static defaultAction = 'list'
	
}
