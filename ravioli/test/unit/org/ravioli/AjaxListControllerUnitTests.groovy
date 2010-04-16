package org.ravioli
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;

import grails.plugins.nimble.core.Group;
import grails.plugins.nimble.core.Permission;
import grails.plugins.nimble.core.Role;
import grails.test.*
@Mixin(RavioliAssert)
class AjaxListControllerUnitTests extends ControllerUnitTestCase {
    protected void setUp() {
        super.setUp()
        Permissionable.mixinMethods(ResourceListBlock)
		SecurityUtils.setSecurityManager(new DefaultSecurityManager())
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testDisplayBlockUnknown() {
    	mockDomain(ResourceListBlock)
    	controller.params.name = 'noexist'
    	def m = controller.displayBlock()
    	status 404
    	contains 'not found'
    }
    
    void testDisplayBlockNotPermitted() {
    	ResourceListBlock lc = new ResourceListBlock(name:'test',everyone:false)
    	mockDomain(ResourceListBlock,[lc])
    	controller.params.name = lc.name
    	def m = controller.displayBlock()
    	status 530
    	contains 'not permitted'
    	
    }
    
    void testDisplayBlockPermitted() {
    	ResourceListBlock lc = new ResourceListBlock(name:'test',everyone:true)
    	mockDomain(ResourceListBlock,[lc])
    	controller.params.name = lc.name
    	def m = controller.displayBlock()
    	editableContainerOn lc

    }
    
    // check that we're getting the editable container template, on the specified list
    def editableContainerOn(l) {
    	template '/resourceListBlock/editable'
    	assertTrue controller.renderArgs.model.containsKey( 'container')
    	assertEquals l, controller.renderArgs.model.container
    }
    
    void testDisplayUseBlockrNotLoggedIn() {
    	def m = controller.displayUserBlock()
    	status 500
    	contains 'Not logged in'
    	
    }
    
    void testDisplayUserBlockUnAuthenticated() {
    	User u = new User()
    	u.profile = new Profile()
    	ResourceListBlock lc = new ResourceListBlock()
    	u.profile.userBlock = lc
    	controller.authenticatedUser = u
    	def m = controller.displayUserBlock()
    	status 530
    	contains 'not permitted'
    	
    	
    }
    
    void testDisplayUserBlockAuthenticated() {
    	User u = new User()
    	u.profile = new Profile()
    	ResourceListBlock lc = new ResourceListBlock(everyone:true)
    	u.profile.userBlock = lc
    	controller.authenticatedUser = u
    	def m = controller.displayUserBlock()
    	editableContainerOn u.profile.userBlock
    	
    }
    
}
