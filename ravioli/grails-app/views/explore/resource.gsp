<html>
<head>
<title>
${r.titleField }
</title>
 	<gui:resources components="tooltip, expandablePanel, tabView"/>
 		<yui:javascript dir="json" file="json-min.js" />
</head>
<body class="yui-skin-sam">
	<g:render template="resourceDetail" />
	<g:javascript src='resource.js' />
</body>
</html>