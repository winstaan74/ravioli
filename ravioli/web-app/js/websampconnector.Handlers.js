/**
 * WebSampConnector Toolkit.
 * 
 * This file contains the WebSampConnector handlers which allow your Web application
 * to receive messages coming from VO-softwares. You are encouraged to provide your 
 * own methods to fulfill the requirements of your Web application.
 * @requires websampconnector.js
 * @see http://vo.imcce.fr/webservices/samp/?manual 
 * @author J. Berthier <berthier@imcce.fr> (IMCCE/OBSPM/VOParis Data Centre)
 * @author A. Tregoubenko <http://arty.name>
 * @version 1.5, 2010-02-21
 * @copyright 2010, VO-Paris Data Centre <http://vo.obspm.fr/>
 */

(function()
{
   // This function checks if the namespace is defined
   var f = function() {
      if (typeof WebSampConnector == "undefined") {
         alert("The WebSampConnector namespace is undefined.\nLoad websampconnector.js before this script");
         return false;
      }
      return true;
   }
   if (!f()) { return; }

   /*
    * This method changes the icon which indicates the status of the connection to the hub.
    * It is the default callback function. If you provide your own callback function, do
    * not forget to define the corresponding binding (see the end of this file).
    * @param args a string array containing a single element with the hub status: 
    *   connected, disconnected, unknown
    */
   WebSampConnector.setStatusIcon = function(args) {
	//   alert('here')
      if (args != null) {
         var status = args[0];
         // show all the samp buttons.
         var samp = getCSSRule('.samp')
         var connected = getCSSRule('#CONNECTED')
         var notconnected= getCSSRule('#NOTCONNECTED')
     //    var icon = document.getElementById(WebSampConnector.hubIconId);
         if (status == 'connected'){
        	 css.style.visibility='visible'
        	 notconnected.style.display='none'
             connected.style.display='block'
         } else if (status == 'disconnected') {
        	 css.style.visibility='hidden'
             connected.style.display='none'
             notconnected.style.display='block'
         }

   /*      if (icon) {
            icon.src = WebSampConnector.hubIconPrefix + status + WebSampConnector.hubIconExt;
         }*/
      } else {
         WebSampConnector.log("Hub status icon: arg undefined!");
      }
   };

   /*
    * This method allows a VO application to point a given celestial coordinate in the Web page
    * @param args an array which contains the celestial coordinates: RA=args[0], DEC=args[1]
    */
   WebSampConnector.pointAtSkyHandler = function(args) {
      var textn = "undefined!";
      if (args != null) {
         var ra = args[0];
         var dec = args[1];
         textn = "RA=" + WebSampConnector.decToSexa(ra) + " ; DEC=" + WebSampConnector.decToSexa(dec);
      }
      var hub = document.getElementById(WebSampConnector.hubId);
      if (hub) {
         hub.value = textn;
      }
   };
    
   /*
    * This method allows a VO application to highlight a given row in the Web page
    * @param args an array which contains the celestial coordinates: table-id=args[0], url=args[1], row=args[2]
    */
   WebSampConnector.highlightRowHandler = function(args) {
      var textn = "undefined!";
      if (args != null) {
         var tId = args[0];
         var url = args[1];
         var row = args[2];
         textn = "Highlighted row: #" + row;
      }
      var hub = document.getElementById(WebSampConnector.hubId);
      if (hub) {
         hub.value = textn;
      }
   };
    
   /*
    * This method allows a VO application to highlight a selection of rows in the Web page
    * @param args an array which contains the celestial coordinates: table-id=args[0], url=args[1], rows[]=args[2]
    */
   WebSampConnector.selectRowListHandler = function(args) {
      var textn = "undefined!";
      if (args != null) {
         var tId = args[0];
         var url = args[1];
         textn = "Selected rows: ";
         for (i=2; i<args.length; i++) {
            textn += '#' + args[i] + ', ';
         }
      }
      var hub = document.getElementById(WebSampConnector.hubId);
      if (hub) {
         hub.value = textn;
      }
   };

   /*
    * This method retrieves the list of client public IDs for those clients currently registered.
    * The result is returned as an array of string containing the list of registered clients, 
    * formatted as: publicId | Name | DescriptionText | DocumentationURL
    */
   WebSampConnector.getRegisteredClients = function() {
      try {
         var textn = "Registered clients:\n";
         var applet = WebSampConnector.getApplet();
         var clients = applet['getRegisteredClients'].apply(applet, null);
         for (i=0; i<clients.length; i++) {
            textn += ' * ' + clients[i] + '\n';
         }
         WebSampConnector.log(textn);
      } catch (e) {
         WebSampConnector.log('Error: WebSampConnector:\n' + e);
      }
   };

   /*
    * This method retrieves the list of clients which subscribed a given MType.
    * The result is returned as an array of string containing the list of subscribed clients,
    * formatted as: publicId | Name
    * @param array of string providing the given MType (e.g. table.load.votable)
    */
   WebSampConnector.getSubscribedClients = function(args) {
      try {
         var textn = "Subscribed clients of MType: " + args[0] + "\n";
         var applet = WebSampConnector.getApplet();
         var clients = applet['getSubscribedClients'].apply(applet, args);
         for (i=0; i<clients.length; i++) {
            textn += ' * ' + clients[i] + '\n';
         }
         WebSampConnector.log(textn);
      } catch (e) {
         WebSampConnector.log('Error: WebSampConnector:\n' + e);
      }
   };

   /*
    * Java to javascript communication bindings (it appears that it is not possible
    * to call a JS method a.b() from a Java applet using the JSObject call method).
    */
   pointAtSkyHandler = WebSampConnector.pointAtSkyHandler;
   highlightRowHandler = WebSampConnector.highlightRowHandler;
   selectRowListHandler = WebSampConnector.selectRowListHandler;

   /*
    * Callback function binding needed by the JApplet: provide here the binding
    * to your own application if you override the default callback function.
    */
   setStatusIcon = WebSampConnector.setStatusIcon;

})();
