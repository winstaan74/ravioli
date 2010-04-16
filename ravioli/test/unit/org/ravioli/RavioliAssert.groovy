package org.ravioli;

import junit.framework.Assert;

/** class of helpful assertion methods
 * use by saying 
 * 
 * @Mixin(RavioliAssert)
 *  */

class RavioliAssert extends Assert {
	
	
	public void redirectsTo(String action) {
		assertEquals action, this.controller.redirectArgs['action']
		assertNull  this.controller.redirectArgs['controller']
	}
	
	public void flashContains(String s) {
		assertNotNull this.controller.flash.message
		assertTrue this.controller.flash.message + "/=/" + s,this.controller.flash.message.contains(s)
	}
	
	public void contentType(String s) {
		assertEquals s, controller.renderArgs.contentType
	}
	
	
	public String resp() {
		return controller.response.contentAsString
	}
	
	public void contains(String s) {
		assertTrue resp().contains(s)
	}
	public void notContains(String s) {
		assertFalse resp().contains(s)
	}	
	public void template(String t) {
		assertEquals t,controller.renderArgs.template
	}
	
	public void status(int code) {
		assertEquals code, controller.renderArgs.status
	}
	
}
