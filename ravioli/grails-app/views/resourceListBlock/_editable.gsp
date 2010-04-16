<%--

render a block, with some editable functionality

 --%>
<%
	def containerId = "listContainer${container.id }"
	def spinnerId = containerId + "spinner"
	def clazz
	if (container.icon) {
		clazz = 'icon ' + container.icon
	} 
%>
<div class='leftBlock' id="${containerId }">
<h3 class="${clazz }">${container.title }
<g:if test="${container.iCanEdit() }">
	<g:link controller='resourceListBlock' action='edit' id="${container.id }" class='icon icon_pencil'></g:link>
</g:if>
</h3>
<%--
<g:remoteLink controller="ajaxList" action="display" id="${container.id }" method="get"
	update="${containerId }"
	onLoading="${sf.spinnerStart(id:spinnerId) }"
	onComplete="${sf.spinnerStop(id:spinnerId) }"
	>(reload)</g:remoteLink>
<sf:spinner id="${spinnerId }" />
 --%>


 	<ul>
			<g:render template="/resourceList/asListItem" var="l"
				collection="${container.lists}" />
	</ul>	
</div>