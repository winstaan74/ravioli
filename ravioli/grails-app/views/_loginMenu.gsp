<%--fragment used as login menu --%>
 	<div class="login">
        	<n:isNotLoggedIn>
        	<g:link class='main' controller="auth" action="login">Login</g:link>
        	| <g:link class='main' controller="account" action="createuser">Register</g:link>
        	</n:isNotLoggedIn>
        	<n:isLoggedIn>
        	Logged in as <g:link class='main' controller="profile"><n:principalName/></g:link>
        	| <g:link class='main' controller="auth" action="logout">Logout</g:link>
        	</n:isLoggedIn>
     </div>