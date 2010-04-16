<%--
Format a resource in a separate HTML page.
 --%>
<html>
<head>
<title>
${r.titleField }
</title>
 	<gui:resources components="tooltip, tabView, expandablePanel, datePicker"/>
 		<yui:javascript dir="json" file="json-min.js" />
</head>
<body class="yui-skin-sam">
	<g:render template="/resourceDetail" />
	<%--load js last of all. --%>
	<g:javascript src='resource.js' />
</body>
</html>