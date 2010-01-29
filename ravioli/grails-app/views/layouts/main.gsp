<html>
    <head>
        <title>Ravioli &raquo; <g:layoutTitle /></title>
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:layoutHead />
        <g:javascript library="application" />	
        <nav:resources />			
    </head>
    <body>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>	
        <!--  logo created using http://creatr.cc/creatr/ -->
        <div class="logo"><img src="${resource(dir:'images',file:'ravioli.png')}" alt="Ravioli" /></div>
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
        <!--  unused
        <div id="menu-div">
        	<nav:render group="tabs" /><nav:renderSubItems group="tabs" />
        </div>
        -->
        <!-- we already have a flash, elsewhere - leave this off for now
        <g:if test="${flash.message }">
  			<div id="flash">
  				${flash.message }
  			</div>
  		</g:if>
  		-->
        <g:layoutBody />
        <div id="footer-div">
        	Ravioli v<g:meta name="app.version" /> 
        </div>		
    </body>	
</html>