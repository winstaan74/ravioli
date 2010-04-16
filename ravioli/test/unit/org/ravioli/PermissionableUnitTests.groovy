package org.ravioli;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import grails.test.GrailsUnitTestCase;

class PermissionableUnitTests extends GrailsUnitTestCase {
	protected void setUp() {
		super.setUp();
	}
	
	protected void tearDown() {
		super.tearDown()
	}
	
	void testListContainerPermissions() {
		Permissionable.mixinMethods(ResourceListBlock)
		
		ResourceListBlock lc = new ResourceListBlock(id:42)
		
		assertEquals 'block:view:42',lc.viewPermission()
		assertEquals 'block:edit:42',lc.editPermission()
		assertEquals 'block:view,edit:42',lc.viewEditPermission()
	}
	void testEveryoneCanView() {
		Permissionable.mixinMethods(ResourceListBlock)
		
		ResourceListBlock lc = new ResourceListBlock(id:42)
		lc.everyone = true
		assertTrue lc.iCanView()

		// this chekc will refer to the security manager - bettert set one up..
		SecurityUtils.setSecurityManager(new DefaultSecurityManager())
		assertFalse lc.iCanEdit()
		
	}
	
	void testPermittedCantView() {
		Permissionable.mixinMethods(ResourceListBlock)
		SecurityUtils.setSecurityManager(new DefaultSecurityManager())
				
		ResourceListBlock lc = new ResourceListBlock(id:42)
		lc.everyone = false
		assertFalse lc.iCanView()
		assertFalse lc.iCanEdit()
	}
	

}
