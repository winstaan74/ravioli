 <div id="top-menu">

<%--SAMP integration --%>

<div id="SAMP">
<gui:toolTip text="
Samp is a way to exchange data between astronomy tools. 
Launch Topcat, Aladin, VODesktop, VOSpec, or another tool, and then click Connect.
Once connected, additional buttons will appear in the resource display to 
send data to other tools.
">
	<div class='head'>Samp</div>
	 <div id="CONNECTED" class="icon icon_connect">
	 	Connected.
	 	<a href="#" onclick="sampDisconnect();">(disconnect)</a>
	 	
	 </div>
	 <div id="CONNECTING">
	 	<img src="${g.resource(dir:'/images',file:'spinner.gif') }"/>
	 	working..
	 </div>
	 
	 <div id="NOTCONNECTED" class="icon icon_disconnect">
	 	Not connected.
	 	<a  href="#" class='main' onclick="sampConnect('${grailsApplication.config.grails.serverURL }/applet');">Connect</a>
	 </div>

</gui:toolTip>
</div><%-- end samp --%>
     
     	

      <%-- common login menu --%>

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
            
</div><%-- end top menu --%> 	
        
     
