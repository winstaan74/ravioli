<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
  "http://www.w3.org/TR/html4/strict.dtd">

<html>
  <head>
   <link rel="stylesheet" href="${resource(dir:'css',file:'reset-fonts-grids.css')}" />
  
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    <title><g:message code="nimble.layout.admin.title" /> <g:layoutTitle default="Grails"/></title>

  	<nh:nimblecore/>
    <nh:nimbleui/>
    <nh:admin/>

	<nh:growl/>
    <script type="text/javascript">
      <njs:flashgrowl/>
    </script>

    <g:layoutHead/>
        <link rel="stylesheet" href="${resource(dir:'css',file:'ravioli.css')}" />
        
</head>

<body class="yui-skin-sam">

  <div id="doc3" class="yui-t2">

    <div id="hd">
      <g:render template='/templates/nimble/nimbleheader' model="['navigation':true]"/>
    </div>

    <div id="bd">
    	<div id="yui-main">
    	<div class='yui-b'>
	      		<g:layoutBody/>
	     </div>
	     </div>
	     <div class="yui-b">
	        <g:render template="/templates/nimble/navigation/sidenavigation" />
		</div>
    </div>

    <div id="ft">
    	<g:render template='/bottom' />
    </div>

  </div>

<n:sessionterminated/>

</body>

</html>