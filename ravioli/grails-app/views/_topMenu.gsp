<%-- common login menu --%>	
 <div id="top-menu">
 	<div class="login">
        	<n:isNotLoggedIn>
        	<g:link class='main' controller="auth" action="login">Login</g:link>
        	| <g:link class='main' controller="account" action="createuser">Register</g:link>
        	</n:isNotLoggedIn>
        	<n:isLoggedIn>
        	Logged in as <g:link class='main' controller="profile"><n:principal/></g:link>
        	| <g:link class='main' controller="auth" action="logout">logout</g:link>
        	</n:isLoggedIn>
     </div>
        <n:isAdministrator>
        <div class='admin'>
        	<nav:render group="admin"/><nav:renderSubItems group="admin" />
        </div>
        </n:isAdministrator>
        </div>	
      
     