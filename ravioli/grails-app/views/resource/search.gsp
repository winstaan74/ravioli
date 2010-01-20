<html>
<head>
	<title>Search Resources</title>
	<meta name="layout" content="main" />
</head>
<body>
	<h1>Search</h1>
	
	<g:form action='search'>
		<g:textField name="q" value="${params.q}" />
		<g:submitButton name="search" value='Search'/>
	</g:form>
	<br />
	<g:if test='${searchResult?.empty}'>
		<div> No resources found</div>
	</g:if>
	<g:elseif test="${ searchResult?.size() > 0}" >
		<g:each var='r' in='${searchResult }'>
			<div class="searchResource">
				<div class='resourceLink'>
				<g:link action='show' id='${r.id}'>${r.title}</g:link>
				</div>
			</div>
		</g:each>
	</g:elseif>
	<g:if test='${searchError }'>
		${searchError }
	</g:if>
</body>
</html>