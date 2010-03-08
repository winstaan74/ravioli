package org.ravioli

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
	
    def index = {
		redirect(action:'list')
		}
}
