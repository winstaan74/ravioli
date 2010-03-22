<html>
<head>
	<title>Registry Resources</title>
 	<gui:resources components="dataTable, dialog, tooltip, expandablePanel, tabView, datePicker" />
	<yui:javascript dir="cookie" file="cookie-min.js" />
	<yui:javascript dir="json" file="json-min.js" />

<style type='text/css'>
<%-- putting an inline style here seems to be the only way to override the
default styling. --%>

<%-- paginator --%>
.yui-skin-sam a.yui-pg-first:link, 
.yui-skin-sam a.yui-pg-first:visited, 
.yui-skin-sam a.yui-pg-first:active,  
.yui-skin-sam a.yui-pg-previous:link, 
.yui-skin-sam a.yui-pg-previous:visited, 
.yui-skin-sam a.yui-pg-previous:active, 
.yui-skin-sam a.yui-pg-next:link, 
.yui-skin-sam a.yui-pg-next:visited, 
.yui-skin-sam a.yui-pg-next:active, 
.yui-skin-sam a.yui-pg-last:link, 
.yui-skin-sam a.yui-pg-last:visited, 
.yui-skin-sam a.yui-pg-last:active, 
.yui-skin-sam a.yui-pg-page:link, 
.yui-skin-sam a.yui-pg-page:visited, 
.yui-skin-sam a.yui-pg-page:active {
color:#054345;
}
.yui-skin-sam a.yui-pg-first:hover, 
.yui-skin-sam a.yui-pg-page:hover, 
.yui-skin-sam a.yui-pg-previous:hover, 
.yui-skin-sam a.yui-pg-next:hover,
 .yui-skin-sam a.yui-pg-last:hover {
    color: #660000;
}

<%-- data table rows --%>
.yui-skin-sam tr.yui-dt-odd, 
.yui-skin-sam tr.yui-dt-odd td.yui-dt-asc, 
.yui-skin-sam tr.yui-dt-odd td.yui-dt-desc {
background-color:#E6EEEF;
}
.yui-skin-sam tr.yui-dt-even,
.yui-skin-sam tr.yui-dt-even td.yui-dt-asc, 
.yui-skin-sam tr.yui-dt-even td.yui-dt-desc {
background-color:#FFFFFF;
}
.yui-skin-sam tr.yui-dt-selected td, 
.yui-skin-sam tr.yui-dt-selected td.yui-dt-asc, 
.yui-skin-sam tr.yui-dt-selected td.yui-dt-desc {
background-color:#347879;
}

.yui-skin-sam .yui-dt-liner {
cursor:pointer;
}

.yui-skin-sam tr.ymod-expandedData td, .yui-skin-sam tr.ymod-expandedData, .yui-skin-sam tr.ymod-expandedData div.ymod-expandedDataContent {
	background-color: #FFFFFF;
		color: #000;
	}
.yui-skin-sam tr.ymod-expandedData .columncontainer {
	padding-bottom:20px;
	border-bottom: 1px black solid;
	}
<%-- style for editable columns example - much may not be needed --%>
#dt-dlg {visibility:hidden;border:1px solid #808080;background-color:#E3E3E3;}
#dt-dlg .hd {font-weight:bold;padding:1em;background:none;background-color:#E3E3E3;border-bottom:0;}
#dt-dlg .ft {text-align:right;padding:.5em;background-color:#E3E3E3;}
#dt-dlg .bd {height:20em;margin:0 1em;overflow:auto;border:1px solid black;background-color:white;}
#dt-dlg .dt-dlg-pickercol {clear:both;padding:.5em 1em 3em;border-bottom:1px solid gray;}
#dt-dlg .dt-dlg-pickerkey {float:left;}
#dt-dlg .dt-dlg-pickerbtns {float:right;}

<%-- Container workarounds for Mac Gecko scrollbar issues --%>

.yui-panel-container.hide-scrollbars #dt-dlg .bd {
    <%-- Hide scrollbars by default for Gecko on OS X --%>
    overflow: hidden;
}
.yui-panel-container.show-scrollbars #dt-dlg .bd {
    <%-- Show scrollbars for Gecko on OS X when the Panel is visible --%>
    overflow: auto;
}
#dt-dlg_c .underlay {overflow:hidden;}

.inprogress {position:absolute;} <%-- transitional progressive enhancement state --%>
<%-- control width of table --%>
#dt_div_resources {
	width:800px;
}
#dt_div_resources > table {
	width:800px;
	}
----</style>
</head>
<body>
	<%-- search form --%>
	<input type="text" id="sb" value="${params.query}"/>
	<button type='button' id="filterButton">Search</button>
	<%-- if we've already got a query, trigger it once the page has loaded --%>

<div id='resourcesParent'>
	<r:resourceTable />
</div>
<%--popup dialogue for selecting columns. --%>
<div id="dt-dlg">
    <span class="corner_tr"></span>
    <span class="corner_tl"></span>
    <span class="corner_br"></span>
    <span class="corner_bl"></span>
    <div class="hd">
        Choose which columns you would like to see:
    </div>
    <div id="dt-dlg-picker" class="bd">
    </div>
</div>
<%--last of all, load the javascript for the table.
loading last means faster page display.
 --%>
 <g:javascript>
 <%--using grails to compute the link back - so this bit of javascript is inline --%>
 function mkOpenResourceUrl(id) {
 	base = '<g:createLink controller="explore" action="resource"/>'
 	return base + "/" + id
 }
</g:javascript>
	<g:javascript src='resourceTable.js'/>
	<g:javascript src='resource.js' />
</body>
</html>