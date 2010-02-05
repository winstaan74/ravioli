<%-- common login menu --%>
<!-- TODO: work on this -->
 <div id="login-div">
        	<n:isNotLoggedIn>
        	<g:link controller="auth" action="login">Login</g:link>
        	| <g:link controller="account" action="createuser">Register</g:link>
        	</n:isNotLoggedIn>
        	<n:isLoggedIn>
        	Logged in as <g:link controller="profile"><n:principal/></g:link>
        	| <g:link controller="auth" action="logout">logout</g:link>
        	</n:isLoggedIn>
        	<n:isAdministrator>
        	<nav:render group="admin"/><nav:renderSubItems group="admin" />
        	</n:isAdministrator>
        </div>	
      
     