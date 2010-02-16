<html>
<head>
	<title>Registry Resources</title>
 	<gui:resources components="dataTable, dialog, menu"/>
	<yui:javascript dir="cookie" file="cookie-min.js" />
	<yui:javascript dir="json" file="json-min.js" />

<style type='text/css'>
<%-- putting an inline style here seems to be the only way to override the
default styling. --%>

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
	width:100%;
}
.yui_dt_hidden {
	display:none;
}
</style>
</head>
<body>
	<%-- search form --%>
	<input type="text" id="sb" value="">
	<button id="filterButton">Search</button>

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
seems to work best here.
 --%>
 <g:javascript>
 <%--using grails to compute the link back - so this bit of javascript is inline --%>
 function mkOpenResourceUrl(id) {
 	base = '<g:createLink controller="explore" action="resource"/>'
 	return base + "/" + id
 }
</g:javascript>
	<g:javascript src='resourceTable.js'/>
</body>
</html>