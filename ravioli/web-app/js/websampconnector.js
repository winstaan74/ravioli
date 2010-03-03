/**
 * WebSampConnector Toolkit.
 * 
 * This file contains the Javascript object associated to the WebSampConnector
 * Java applet. It defines the WebSampConnector namespace and its properties.
 * @see http://vo.imcce.fr/webservices/samp/?manual 
 * @author J. Berthier <berthier@imcce.fr> (IMCCE/OBSPM/VOParis Data Centre)
 * @author A. Tregoubenko <http://arty.name>
 * @version 1.5, 2010-02-21
 * @copyright 2010, VO-Paris Data Centre <http://vo.obspm.fr/>
 *
 * @usage: You can override the default configuration values by passing new 
 *   values through the configure property of the WebSampConnector object, e.g.:
 *       <script type="text/javascript">
 *           WebSampConnector.configure({
 *               jAppletCodeBase: '/mypath/', 
 *               hubIconId: 'myStatusIcon'
 *           });
 *       </script>
 * 
 * Note: you are encouraged to override or provide your own callbackFunction and
 * handler methods to fulfill the requirements of your Web application. By default,
 * the callbackFunction is the setStatusIcon method, and the handlers just print
 * logs in a textarea. Cf. websampconnector.Handlers.js file
 */
var WebSampConnector = {

   // Id of the Japplet tag
   jAppletId: 'WebSampConnectorApplet',
   // Absolute path to the JApplet directory
   jAppletCodeBase: './',
   // Version of the WebSampConnector JApplet
   jAppletVersion: '1.5',
   // Prefix of the hub status icon names (hub-connected.png, hub-disconnected.png, ...)
   hubIconPrefix: 'icons/hub-',
   // Extension of the hub status icon names
   hubIconExt: '.png',
   // Id of the html img element if the status icon
   hubIconId: 'hubStatusIcon',
   // Id of the html textarea element used to write the received messages
   hubTextArea: 'HubMsg',
   // Name of the callback function immediately called after the connection has been established
   callbackFunction: "setStatusIcon",
   
   /**
    * Client configuration method to override default configuration values
    */
   configure: function(options) {
      this.jAppletId        = options.jAppletId        || this.jAppletId;
      this.jAppletCodeBase  = options.jAppletCodeBase  || this.jAppletCodeBase;
      this.jAppletVersion   = options.jAppletVersion   || this.jAppletVersion;
      this.hubIconPrefix    = options.hubIconPrefix    || this.hubIconPrefix;
      this.hubIconExt       = options.hubIconExt       || this.hubIconExt;
      this.hubIconId        = options.hubIconId        || this.hubIconId;
      this.hubTextArea      = options.hubTextArea      || this.hubTextArea;
      this.callbackFunction = options.callbackFunction || this.callbackFunction;
   },

   /**
    * Returns a log message
    */
   log: function(message) {
     alert(message);
   },

   /**
    * Formats a number in a sexagesimal representation (i.e. deg:min:sec)
    * @param num a decimal number
    * @return a string containing the sexagesimal representation of the number 
    */
   decToSexa: function(num) {
      var deg = parseInt(num);
      num -= deg;
      num *= 60;
      var min = parseInt(num);
      num -= min;
      num *= 60;
      var sec = parseInt(num);
      return '' + deg + ':' + min + ':' + sec;
   },

   /**
    * Returns the Japplet HTML element Id
    */
   getApplet: function() {
      return document.getElementById(WebSampConnector.jAppletId);
   },

   /**
    * Generic method to broadcast messages to the hub
    */
   proxyMethod: function(methodName) {
      return function() {
         try {
            var applet = this.getApplet();
            return applet[methodName].apply(applet, arguments);
         } catch (e) {
            this.log('Error: WebSampConnector:\n' + e);
            return false;
         }
      }
   },

   /**
    * This method builds the Japplet, and connects and registers the Web browser 
    * to a running Samp hub
    */
   start: function() {
      // Get the name of the browser
      var nav = navigator.appName;
      // Destroy the old applet if it exists
      var el = this.getApplet();
      if (el) {
         el.parentNode.removeChild(el);
      }
      // Define the applet tag
      if (nav.indexOf("Microsoft Internet Explorer") != -1) {
         var applet = "<object id='" + this.jAppletId + "' name='" + this.jAppletId + "'"
            + " style='visibility: hidden; position: absolute; top:0; left:0;' width='0' height='0'"
            + " codebase='" + this.jAppletCodeBase + "'"
            + " archive='sWebSampConnector-" + this.jAppletVersion + ".jar'"
            + " code='org.voparis.WebSampConnector'"
            + " MAYSCRIPT>"
            + " WebSampConnector: you need a Java-enabled browser to launch it.<br />"
            + " <a href='http://java.sun.com/products/plugin/downloads/'>Get the latest Java Plug-in here.</a>"
            + "</object>";
      } else {
         var applet = "<object type='application/x-java-applet;version=1.6'"
                + " id='" + this.jAppletId + "' name='" + this.jAppletId + "'"
                + " archive='sWebSampConnector-" + this.jAppletVersion + ".jar'"
                + " style='visibility: hidden; position: absolute; top:0; left:0;' width='0' height='0'>"
                + " <param name='code' value='org.voparis.WebSampConnector' />"
                + " <param name='codebase' value='" + this.jAppletCodeBase + "' />"
                + " <param name='archive' value='sWebSampConnector-" + this.jAppletVersion + ".jar' />"
                + " <param name='mayscript' value='true' />"
               + " WebSampConnector: you need a Java-enabled browser to launch it.<br />"
               + " <a href='http://java.sun.com/products/plugin/downloads/'>Get the latest Java Plug-in here.</a>"
                + "</object>";
      }
      // Append the applet to the document
      var body = document.body;
      var div = document.createElement("div");
      div.innerHTML = applet;
      body.appendChild(div);
   },

   /**
    * This method unregisters the client and terminates the connection
    * @return true if the client is connected, if not false
    */
   stop: function() {
      var status = false;
      try {
         status = this.getApplet().disconnect();
         setStatusIcon(["disconnected"]);
      } catch (e) {
         setStatusIcon(["unknown"]);
         this.log('Error: WebSampConnector:\n' + e);
      }
      return status;
   },
    
   /**
    * This method allows the client to know if he is connected to a running Samp hub.
    * @param dmsg boolean Sets to true to display messages
    * @return true if the client is connected, if not false
    */
   isConnected: function(dmsg) {
      var status = false;
      try {
         if (this.getApplet().isConnectedToHub()) {
            status = true;
            this.setStatusIcon(["connected"]);
            if (dmsg) {
               this.log('Connected to the Samp hub');
            }
         } else {
            this.setStatusIcon(["disconnected"]);
            if (dmsg) {
               this.log('Not connected to the Samp hub');
            }
         }
      } catch (e) {
         this.setStatusIcon(["disconnected"]);
         if (dmsg) {
            this.log('Not connected to the Samp hub');
         }
      }
      return status;
   }

};

/*
 * This method broadcasts a message from the Web page to VO applications via the Samp hub 
 * @param type the MType of the resource 
 * @param id the Id of the resource 
 * @param name the name of the resource 
 * @param url the URL where is located the resource
 * @return true if no error occurs, false if not
 * @throws exception if an error occurs
 */
WebSampConnector.sendSampMsg = WebSampConnector.proxyMethod('sendSampMsg');

/*
 * This method allows the client to point a given celestial coordinate 
 * in a VO application (e.g. Aladin) by selecting it in the Web page.
 * @param ra the right ascension to point (in degrees)
 * @param dec the declination to point (in degrees)
 * @return true if no error occurs, false if not (if an exception is rises then a message is printed)
 */
WebSampConnector.pointAtSky = WebSampConnector.proxyMethod('pointAtSky');
 
/*
 * This method allows the client to highlight a given row of a table
 * in a VO application (e.g. Aladin) by selecting it in the Web page.
 * @param tableId the Id of the resource (table)
 * @param url the URL where is located the resource
 * @param row the number of the row to highlight (from 0 to n-1)
 * @return true if no error occurs, if not false (if an exception is rises then a message is printed)
 * @throws PrivilegedActionException 
 */
WebSampConnector.tableHighlightRow = WebSampConnector.proxyMethod('tableHighlightRow');
 
/*
 * This method allows the client to highlight a set of rows of the table
 * in a VO application (e.g. Aladin) by selecting them in the Web page.
 * @param tableId the Id of the resource (table)
 * @param url the URL where is located the resource
 * @param rowList an array of integers providing the list of the rows to highlight (from 0 to n-1)
 * @return true if no error occurs, if not false (if an exception is rises then a message is printed)
 */
WebSampConnector.tableSelectRowList = WebSampConnector.proxyMethod('tableSelectRowList');
 
/*
 * This method allows the client to send a script to be executed by Aladin
 * @param ascript a string containing the Aladin script to be executed
 * @return true if no error occurs, if not false (if an exception is rises then a message is printed)
 */
WebSampConnector.sendAladinScript = WebSampConnector.proxyMethod('sendAladinScript');

/*
 * Callback function binding needed by the JApplet. The default callback function 
 * is defined in the websampconnector.Handlers.js file 
 */
callbackFunction = WebSampConnector.callbackFunction;
