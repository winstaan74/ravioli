<html>
    <head>
        <title><g:layoutTitle default="Ravioli" /></title>
        <link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
        <g:layoutHead />
        <g:javascript library="application" />	
        <nav:resources />			
    </head>
    <body>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>	
        <!--  logo created using http://creatr.cc/creatr/ -->
        <div class="logo"><img src="${resource(dir:'images',file:'ravioli.png')}" alt="Ravioli" /></div>
        <div id="menu">
        	<nav:render group="tabs" />
        	<nav:renderSubItems group="tabs" />
        </div>
        <!-- we already have a flash, elsewhere - leave this off for now
        <g:if test="${flash.message }">
  			<div id="flash">
  				${flash.message }
  			</div>
  		</g:if>
  		-->
        <g:layoutBody />
        <div id="footer">
        	Version <g:meta name="app.version" /> on Grails <g:meta name="app.grails.version" />
        </div>		
    </body>	
</html>