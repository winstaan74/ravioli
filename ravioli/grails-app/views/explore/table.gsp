<html>
<head>
	<title>Registry Resources</title>
	<gui:resources components="dataTable"/>
<%-- putting an inline style here seems to be the only way to override the
default styling. --%>
<style text='text/css'>

.yui-skin-sam tr.ymod-expandedData td, .yui-skin-sam tr.ymod-expandedData, .yui-skin-sam tr.ymod-expandedData div.ymod-expandedDataContent {
	background-color: #FFFFFF;
		color: #000;
	}

.yui-skin-sam tr.ymod-expandedData .columncontainer {
	padding-bottom:20px;
	border-bottom: 1px black solid;
	}
</style>
</head>
<body>
	<input type="text" id="sb" value="">
	<button id="filterButton">Search</button><br/> 
<gui:dataTable
	id="resources"
	sortedBy="modified"
	draggableColumns="false"
	rowsPerPage="30"
	columnDefs="[
		[key:'ivorn', label:'IVO-ID', width:'250', resizable:true]
		,[key:'title', label:'Title',width:'400', resizable:true]
		,[key:'created',sortable:true, label:'Created', width:'80']
		,[key:'modified',sortable:true, label:'Modified', width:'80']
	]"
	rowExpansion="true"
	controller="explore" action="tableDataAsJSON"
	paginatorConfig="[
	template:'{PreviousPageLink} {PageLinks} {NextPageLink} {CurrentPageReport}',
	pageReportTemplate:'{totalRecords} resources'
	]"
/>
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