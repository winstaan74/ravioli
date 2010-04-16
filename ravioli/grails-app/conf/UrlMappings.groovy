class UrlMappings {
	static mappings = {
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}
		
		"/"(controller:"explore")
		"500"(view:'/error')
		
		//fiddly beasts these - because there's going to be params following, it _needs_ **
		"/stub-registry/$registryIvorn/**"{
			controller = "stubRegistry"
			// map verb parameter to action  - convert to lowercase, as actions can't start with a capital.
			action = {params.verb?.toLowerCase()}
		}
		
		// nice mappings for ads and sesame.
		
		"/ads/${bib}"(controller:'ads')
		//seems to foul up the ajax call, as it comes from a form.
		//"/sesame/${obj}"(controller:'sesame')
		
		
	}
}
