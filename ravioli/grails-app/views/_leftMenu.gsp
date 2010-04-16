<%-- the LHS navigation bar --%>

<n:isAdministrator>
	<g:render template="/adminMenu" />
</n:isAdministrator>
<g:include controller="ajaxList" action="displayBlock" params="[name:'alls']"/>

<g:include controller="ajaxList" action="displayBlock"  params="[name:'canned']"/>

<n:isLoggedIn>
	<g:include controller="ajaxList" action="displayUserBlock"/>
	
	<%-- where we'll add containers for groups, etc for this user, later.. --%>
	<g:include controller="ajaxList" action="displayOtherBlocks" />
</n:isLoggedIn>




