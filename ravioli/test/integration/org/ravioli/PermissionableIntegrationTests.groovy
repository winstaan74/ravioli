package org.ravioli
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;



import grails.plugins.nimble.core.Group;
import grails.plugins.nimble.core.LevelPermission;
import grails.plugins.nimble.core.Permission;
import grails.plugins.nimble.core.PermissionService;
import grails.test.*
/** tests of creating permissions, adding permissions, and then using permissionable
 * support to query for them..
 * 
 * */
class PermissionableIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
        user = User.findByUsername('user')
        admin = User.findByUsername('admin')

    }

    PermissionService permissionService
    
    protected void tearDown() {
        super.tearDown()
    }
    
    User user
    User admin;

    void testWeHaveAUser() {
    	assertNotNull admin
    	assertNotNull user
    	assertNotNull permissionService
    }

/** test that permission names are working for the permissioned classes */    
    void testPermissionNames() {
    	Resource r = new Resource()
    	r.id = 42
    	def p = r.editPermission()
    	assertNotNull p
    	assertEquals 'resource:edit:42',p
    	
    	ResourceListBlock lc = new ResourceListBlock()
    	lc.id = 34
    	p = lc.viewPermission()
    	assertNotNull p 
    	assertEquals 'block:view:34',p
    	
    	// issue here - bookmarklist needs to have a permission 
    	// named 'ResourceList' - i.e. need the classname of the common superclass.
    	ResourceList rl = new BookmarkList()
    	rl.id = 57
    	p = rl.viewEditPermission()
    	assertNotNull p
    	assertEquals 'list:view,edit:57',p
    }
    
/** exercises creating permissions, and then querying permissions table for 
   resources 'viewable' by a user.
   
 */
    void testSearchingForPermissions() {
   
    	// reuse this instance, to create a range of permissions strings.
    	Resource r = new Resource()
    	r.id = 42

   //view, edit for user
    	Permission p = new Permission(target:r.viewEditPermission(),type:Permission.defaultPerm)
    	assertNotNull permissionService.createPermission(p, user)
    	
    // view for admin
    	r.id = 89
    	p = new Permission(target:r.viewPermission(),type:Permission.defaultPerm)
    	assertNotNull permissionService.createPermission(p,admin)
    //view for user's group
    	def grp = new Group(name:'a new group').save(flush:true)
    	assertNotNull grp
    	user.addToGroups grp
    	user.save(flush:true)
    	
    	r.id = 98
    	p = new Permission(target:r.viewPermission(),type:Permission.defaultPerm)
    	assertNotNull permissionService.createPermission(p,grp)
    	
    // view a different kind of object
      	ResourceListBlock lc = new ResourceListBlock()
    	lc.id = 91
    	p = new Permission(target:lc.viewPermission(),type:Permission.defaultPerm)
    	assertNotNull permissionService.createPermission(p,user)
    	
    // view for a role
    	assertTrue user.roles.size() > 0 // bit fiddly getting the first - as it's a set.
    	def role = user.roles.iterator().next()
    	
    	p= new Permission(target:'resource:view:28,34,42',type:Permission.defaultPerm)
    	assertNotNull permissionService.createPermission(p,role)
    	
    	// ok, now see if I can query for resources viewable by user 
    	// problemo - we're goiong to get nulls back from the db...
    	// so can't do full query with meaningful results...
    	// can get halfway though.
    	
    	def ixs = Resource.indexesViewableBy(user)
    	assertEquals ( [28,34,42,98] ,ixs.sort())
    	
    	ixs = ResourceListBlock.indexesViewableBy(user)
    	assertEquals ([91],ixs)
    	
    	ixs = ResourceListBlock.indexesViewableBy(admin)
    	assertEquals ([], ixs)

    	
       	ixs = Resource.indexesViewableBy(grp)
    	assertEquals ([98], ixs)
    	
    	ixs = Resource.indexesViewableBy(role)
    	assertEquals([28,34,42],ixs)
    	
    	ixs = Resource.indexesEditableBy (user)
    	assertEquals ([42],ixs)
    	
    }
}
