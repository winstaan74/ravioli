<html>
<head>
	<title>Table Metadata for ${r.titleField }</title>
	<gui:resources components="dataTable"/>
</head>

<body class='yui-skin-sam'>
<div class="tableMetadata">
	<h1>Table Metadata for ${r.titleField }</h1>

	<g:form name="showtable">
		<fieldset><legend>Choose a table</legend>
		<label for="table">Table: </label>
		<g:select name="table" from="${tableNames }" />
		<button type='button' onClick="loadTable();">Show Columns</button>
		</fieldset>
	</g:form>
	<div class="description" id="description"></div>
	<div id="coltable"></div>
<g:javascript>
	function loadTable() {
		var tableName =  YAHOO.util.Dom.get('table').value
		var customQueryString = "?table=" + tableName + "&id=${r.id }"
		var state = dt.getState()
		ds.sendRequest(customQueryString,{
			success: updateTableCallback,
			failure: dt.onDataReturnReplaceRows,
			scope : dt,
			argument: state
		});
	}
	
	function updateTableCallback(req,resp,payload) {
		YAHOO.util.Dom.get('description').innerHTML = 
			"<b>" + resp.meta.name + ":</b> "
			+ (resp.meta.role ? '(' + resp.meta.role + ')' : '') 
			+ resp.meta.description;
		dt.onDataReturnReplaceRows(req,resp,payload);
	}
	var cols = [
	        {key:"ix",label:"#", sortable:true,width:20},
            {key:"col",label:"Column Name",sortable:true,resizeable:true,width:90},
            {key:"desc",label:"Description",sortable:true,resizeable:true,width:310},
            {key:"type",label:"Type",sortable:true,resizeable:true,width:60},
            {key:"unit",label:"Unit",sortable:true,resizeable:true,width:60},
            {key:"ucd",label:"UCD",sortable:true,resizeable:true,width:130}    
      ];
      var fields = [{key:"ix"},{key:"col"}, {key:"desc"},{key:"type"},{key:"unit"},{key:"ucd"}];
      var ds = new YAHOO.util.DataSource("${g.createLink(action:'tableMetadataAsJSON') }");
      ds.responseType = YAHOO.util.DataSource.TYPE_JSON;
      ds.responseSchema = {
      	resultsList:'results'
      	, fields:fields
      	, metaFields: {description: 'description', role:'role', name:'name'}
      	};

      var dt = new YAHOO.widget.ScrollingDataTable("coltable", cols, ds , {
                sortedBy:{key:"ix",dir:"asc"}
                , width:"800px"
                , height:"500px"});
      // load the table data..
      loadTable()
	 </g:javascript>
	 </div>
</body>
</html>