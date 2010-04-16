package org.ravioli


/** Typical actions for manipulating {@link ResourceListBlocks}.
 * There's also dynamic (AJAX) functionality for resourcelistblocks in {@link AjaxListController}
 * 
 * @todo protect most of the methods in the controller to 'admin' level.
 * */
class ResourceListBlockController  {
	static layout = 'explore'
	static scaffold = true;
	static navigation = [
	title:"Blocks"
	,group:'admin'
	,order:40
	,subItems: [
	[action:'list']
	]
	]
	
	static defaultAction = 'list'
		
		/** DIsplay an edit form for the user
		 * @todo protect this in security mappings - require login.
		 */
	def edit = {
		def lc = ResourceListBlock.get(params.id)
		if (! lc) {
			render status:404, text:"Block ${params.id} not found"
		} else if (! lc.iCanEdit()) {
			render status: 530, text:'Not permitted to edit this block'
		} else {
			[container:lc]
		}
	}
	
	/** persist changes made in the 'edit' view */
	def update = {
		def lc = ResourceListBlock.get(params.id)
		if (! lc) {
			render status:404, text:"Block ${params.id} not found"
		} else if (! lc.iCanEdit()) {
			render status: 530, text:'Not permitted to edit this block'
		} else {
			//render text:params.inspect()
			if (params.title) {
				lc.title = params.title?.trim()
			}
			// now re-order the list items. first parse a proper list of indexes
			def newIxs = params.rlists_input.split()
				.collect{it.split('_')[1] as Long}
				.unique()
			
			// check whether any changes have been made
			def ixs = lc.lists.collect{it.id}
			if (ixs != newIxs &&
					//rest is sanity checks.. - no repeats / adds / removes
					ixs.size() == newIxs.size()  &&
					newIxs.every{ixs.contains(it)}
				) { 
				// ok. what do I do now?
				// seems I can just manipulate the list.
				// and my changes get persisted - good.
				// ok, lets build my new list
				def newList = newIxs.collect { i-> 
					lc.lists.find{o-> o.id == i}
					}
				// and now clear the persistant list, and populate it
				// won't just assign, incase there's some kind of special sauce
				// inthe persistent list.
				lc.lists.clear()
				lc.lists.addAll(newList)
				
			} 
			
			lc.save()
			redirect(controller:'explore')
		}
	}


}
