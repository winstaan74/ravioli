<html>
<head>
	<title>Edit List Set</title>
	<gui:resources components="draggableList"/>
</head>
<body>
	<h1>Edit List Set</h1>

	<g:form action='update'>
		<label>Title</label>
		<g:hiddenField name="id" value="${container.id }" />
		<g:textField name="title" value="${container.title }" />
		<gui:draggableListWorkArea formReady="true">
			<gui:draggableList id="rlists" prepend="list_">
				<g:each in="${container.lists }">
					<li id="${it.id }">${it.title }</li>
				</g:each>
			</gui:draggableList>
		</gui:draggableListWorkArea>
		<g:submitButton name="Update" value="update" />
	</g:form>
</body>
</html>