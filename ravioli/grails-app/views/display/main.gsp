<html>
<head>
	<title>Registry Resources</title>
	<meta name="layout" content="main" />
	<gui:resources components="dataTable"/>
</head>
<body>
<style>
<!--
not working at present -->tr.ymod-expandedData div.ymod-expandedDataContent td
	{
	background: #FFF;
	color: #000;
}
</style>

<div class="yui-skin-sam">
	<input type="text" id="sb" value="">
	<button id="filterButton">Search</button><br/> 
<gui:dataTable
	id="resources"
	sortedBy="modified"
	draggableColumns="false"
	rowsPerPage="40"
	columnDefs="[
		[key:'ivorn', label:'IVO-ID', width:'300']
		,[key:'title', label:'Title',width:'600', resizable:true]
		,[key:'created',sortable:true, label:'Created', width:'80']
		,[key:'modified',sortable:true, label:'Modified', width:'80']
	]"
	rowExpansion="true"
	controller="display" action="tableDataAsJSON"
	paginatorConfig="[
	template:'{PreviousPageLink} {PageLinks} {NextPageLink} {CurrentPageReport}',
	pageReportTemplate:'{totalRecords} resources'
	]"
/>
</div>
 <script> 
   YAHOO.util.Event.onDOMReady(function () { 
    updateFilter = function () { 
     GRAILSUI.resources.customQueryString = 
      "q="+YAHOO.util.Dom.get("sb").value
 GRAILSUI.resources.loadingDialog.show();
     var state = GRAILSUI.resources.getState(); 
     // gui:dataTable uses the "sorting" property of "state" object, 
     // but yui's "state" has a "sortedBy", not "sorting" (bug maybe?) 
     // so I copy it's value 
     state.sorting = state.sortedBy; 
     // reset the pager, in case the new result has less pages then the current one 
     state.pagination.recordOffset = 0; 
     query = GRAILSUI.resources.buildQueryString(state); 
 
     GRAILSUI.resources.getDataSource().sendRequest(query,{ 
      success : GRAILSUI.resources.onDataReturnReplaceRows, 
      failure : GRAILSUI.resources.onDataReturnReplaceRows, 
      scope  : GRAILSUI.resources, 
      argument: state 
     }); 
GRAILSUI.resources.loadingDialog.hide();
    }; 
 
    YAHOO.util.Event.on('filterButton','click',function (e) { 
     updateFilter(); 
    }); 
   }); 
  </script> 
</body>
</html>