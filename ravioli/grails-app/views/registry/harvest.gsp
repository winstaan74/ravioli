<html>
<head>
	<title>Harvest a registry</title>
	<meta name="layout" content="main" />
</head>
<body>
	<h1>Harvest from a registry</h1>
	
	<g:form action='harvest'>
		Harvest from :<g:select name="harvestId" 
			from='${org.ravioli.Registry.list() }'
			optionKey='ivorn'
			optionValue='name'
			noSelection='${['null':'Choose a registry'] }'
			/>
			<g:submitButton name='harvest' value='Harvest' />
			<br />
		only resources changes since last harvest: <g:checkBox name="incremental" value="true" />
	</g:form>
	<br />
	<g:if test='${harvestError }'>
		${harvestError }
	</g:if>
</body>
</html>