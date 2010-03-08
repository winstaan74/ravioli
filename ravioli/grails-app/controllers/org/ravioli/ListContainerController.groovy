package org.ravioli

class ListContainerController {
	static layout = 'explore'
	static scaffold = true;
	static navigation = [
	                     title:"Containers"
						,group:'admin'
						,order:40
						,subItems: [
						            [action:'list']
						]
						]
    def index = { redirect(action:'list') }
}
