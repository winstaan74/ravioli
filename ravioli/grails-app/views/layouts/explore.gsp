<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
 "http://www.w3.org/TR/html4/strict.dtd">
 <%--page template for all explore functionality. --%>
<html>
<head>
   <title>Ravioli &raquo; <g:layoutTitle /></title>
  <link rel="stylesheet" href="${resource(dir:'css',file:'reset-fonts-grids.css')}" />
  <link rel="stylesheet" href="${resource(dir:'css',file:'famfamfam.css',plugin:'nimble')}" />  
  <g:if test="${['resource','registry','task','taskExecution'].contains(params.controller)  }">
    <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" /><%--default grails styling.. --%>
  </g:if>
  <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
  <g:javascript library="yui" />	
  <nav:resources />	
  <g:layoutHead />
  <%--add our own custom stylesheet last of all - can only help. --%>
  <link rel="stylesheet" href="${resource(dir:'css',file:'ravioli.css')}" />
</head>
<body class="yui-skin-sam">
<div id="doc3" class="yui-t2">
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>	
   <div id="hd">
    <g:link controller='explore'><img align='left' src="${resource(dir:'images',file:'ravioli.png')}" alt="Ravioli" /></g:link>
   	<g:render template="/topMenu" />
   	<g:if test="${flash.message }">
  		<div id="flash">
  				${flash.message }
  			</div>
  	</g:if>
   </div>
   <div id="bd">
	<div id="yui-main">
	<div class="yui-b">
<!--  	<div class="yui-ge">-->
        <g:layoutBody />
<!-- </div>-->
</div>
	</div>
	<div class="yui-b"><g:render template="/leftMenu"/></div>
	
	</div>
   <div id="ft"><p style='text-align: center; margin-top:50px;'>Ravioli v<g:meta name="app.version" /> 
      | <a href='hhttp://code.google.com/p/ravioli/wiki/Changes' target='_blank'>Changes</a>
      | <a href='http://code.google.com/p/ravioli/issues/entry' target='_blank'>Report an issue</a>
      | <a href="http://code.google.com/p/ravioli/" target='_blank'>Project page</a>
   </p></div>
</div>
</body>
</html>