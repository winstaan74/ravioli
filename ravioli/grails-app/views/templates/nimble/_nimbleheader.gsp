<div id="top-menu" ><%--defanged - id="banner" --%>
	   <g:link controller='explore'><img style='float:left;' src="${resource(dir:'images',file:'ravioli.png')}" alt="Ravioli" /></g:link>

	<g:if test="${navigation}">
			<g:render template="/loginMenu"/>
		<%--<n:isLoggedIn>
			<div id="userops">
				<g:message code="nimble.label.usergreeting" /> <n:principalName /> | <g:link controller="auth" action="logout" class=""><g:message code="nimble.link.logout.basic" /></g:link>
			</div>
		</n:isLoggedIn>--%>
	</g:if>
</div>