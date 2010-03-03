/**
 * WebSampConnector Toolkit.
 * 
 * This file contains useful methods to interface the WebSampConnector Java
 * applet into your Web application. You are encouraged to provide your own 
 * methods to fulfill the requirements of your Web application.
 * @requires websampconnector.js
 * @requires websampconnector.Handlers.js
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
    * Connect and disconnet to the Samp hub and display a message
    */
   WebSampConnector.manageConnection = function() {
      if (WebSampConnector.isConnected(false)) {
         WebSampConnector.stop();
         WebSampConnector.changeMsgStatus("Disconnected", "red");
      } else {
         WebSampConnector.start();
         WebSampConnector.changeMsgStatus("Connected", "green");
      }
   };
    
   /*
    * Broadcast a resource to the hub and set icon and status
    * @param type the MType of the resource 
    * @param id the Id of the resource 
    * @param name the name of the resource 
    * @param url the URL where is located the resource
    */
   WebSampConnector.sendSampMsgInterface = function(type, id, name, url) {
      try {
         if (WebSampConnector.sendSampMsg(type, id, name, url)) {
            WebSampConnector.changeMsgStatus("ok, sent", "blue");
            WebSampConnector.setStatusIcon(["connected"]);
         } else {
            WebSampConnector.changeMsgStatus("Connect before!", "red");
            WebSampConnector.setStatusIcon(["disconnected"]);
         }
      } catch (e) {
         WebSampConnector.changeMsgStatus("Please launch a hub!", "red");
         WebSampConnector.setStatusIcon(["disconnected"]);
      }
   };

   /*
    * Change the message in the textarea
    * @param msg the message to display
    * @param color the color of the text
    */
   WebSampConnector.changeMsgStatus = function(msg, color) {
      var hub = document.getElementById(WebSampConnector.hubId);
      if (hub) {
         hub.style.color = color;
         hub.value = msg;
      }
   };
    
   /*
    * Open and close the interface container
    * @param id the name of the main div which contains the interface
    */
   WebSampConnector.toggleDiv = function(id) {
         var divStyle = document.getElementById(id).style;
         var disp = divStyle.display;  
         divStyle.display = (disp == 'none') ? 'block' : 'none'; 
   };
    
   /*
    * Open a given div
    * @param id the name of the div to open
    */
   WebSampConnector.openDiv = function(id) {
      var divStyle = document.getElementById(id).style;
      divStyle.display = 'block'; 
   };
    
   /*
    * Close a given div
    * @param id the name of the div to open
    */
   WebSampConnector.closeDiv = function(id) {
      var divStyle = document.getElementById(id).style; 
      divStyle.display = 'none'; 
   };

})();
