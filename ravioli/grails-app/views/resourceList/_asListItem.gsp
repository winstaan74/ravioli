<%--

render a resourcelist as an item in a resourcelistblock

 --%>
<%@page import="org.ravioli.SmartList"%>
<%@page import="org.ravioli.BookmarkList"%>

<g:if test="${l.iCanView() }"> <%-- if not viewable, skip it. --%>
<%
def clazz = l.icon ? "icon ${l.icon}" : null
def q
switch (l) {
case SmartList:
	q = l.query
	break;
case BookmarkList:
	q = l.ivorns.collect {
		'_ivorn:' + it
	}.join(" OR ")
}
%>
<li id="${l.id }">
<g:if test="${request.forwardURI == request.contextPath + '/' }">
	<a class="${clazz }" href='#' onclick="applyFilter('${q }')">${l.title }</a>
</g:if>
<g:else>
	<g:link class="${clazz }" controller='explore' params="${[query:q ]}">${l.title }</g:link>
</g:else>

<%-- 
<g:if test="${l.iCanEdit() }">
(editable)
</g:if>
--%>
</li>

</g:if>