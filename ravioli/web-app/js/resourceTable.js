/** additional javascript based functionality for the resourceTable */
 YAHOO.util.Event.onDOMReady(function () { 
	   /*
	   *search functionality
	   */
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
  
  var RAVIOLI_COOKIE = 'ravioli_resource_table';
  
  /** outputs display information to a cookie */
  
  var loadingState = false;
  function persistTableDisplayState() {
	  if (loadingState) {
		  return;
	  }
	  var allColumns = GRAILSUI.resources.getColumnSet().keys;
	  var cookieData = [];
	  var l = allColumns.length;
	  for (var i =0; i < l; i++) {
		  var col = allColumns[i];
		  cookieData.push( {
			  'key': col.getKey()
			  ,'hidden':col.hidden
			  ,'width': col.width
		  });
	  }
	  var str = YAHOO.lang.JSON.stringify(cookieData);
	  YAHOO.util.Cookie.set(RAVIOLI_COOKIE,str);
  }
  
  
  /** loads cookie of table display info, if available */
  function loadTableDisplayState() {
	  var loadingState = true;
	  var str = YAHOO.util.Cookie.get(RAVIOLI_COOKIE);
	  if (str) {
		  var l = YAHOO.lang.JSON.parse(str);
		  for (var i = 0; i < l.length; i++) {
			  var o = l[i];
			  var col = GRAILSUI.resources.getColumn(o.key)
			  if (col) {// defensive, in case it doesn't exist.
				 if (o.hidden) {
					 GRAILSUI.resources.hideColumn(col);
				 } else {
					 GRAILSUI.resources.showColumn(col);
				 }
				  GRAILSUI.resources.setColumnWidth(col,o.width);
				  GRAILSUI.resources.reorderColumn(col,i);
			  }
			           
		  }
	  }
	  var loadingState = false;
  }
  // call the above function to load state.
  loadTableDisplayState();
  
  /** listen to changes in column width, column order, and visibility */
  GRAILSUI.resources.subscribe("columnResizeEvent", persistTableDisplayState);
  GRAILSUI.resources.subscribe('columnReorderEvent', persistTableDisplayState);
  GRAILSUI.resources.subscribe("columnShowEvent", persistTableDisplayState);
  GRAILSUI.resources.subscribe("columnHideEvent", persistTableDisplayState);

	 /*
	 *adjustable columns.
	 */
    newCols = true;
    showDlg = function(e) {
        YAHOO.util.Event.stopEvent(e);

        if(newCols) {
            // Populate Dialog
            // Using a template to create elements for the SimpleDialog
            var allColumns = GRAILSUI.resources.getColumnSet().keys;
            var elPicker = YAHOO.util.Dom.get("dt-dlg-picker");
            var elTemplateCol = document.createElement("div");
            YAHOO.util.Dom.addClass(elTemplateCol, "dt-dlg-pickercol");
            var elTemplateKey = elTemplateCol.appendChild(document.createElement("span"));
            YAHOO.util.Dom.addClass(elTemplateKey, "dt-dlg-pickerkey");
            var elTemplateBtns = elTemplateCol.appendChild(document.createElement("span"));
            YAHOO.util.Dom.addClass(elTemplateBtns, "dt-dlg-pickerbtns");
            var onclickObj = {fn:handleButtonClick, obj:this, scope:false };
            
            // Create one section in the SimpleDialog for each Column
            var elColumn, elKey, elButton, oButtonGrp;
            for(var i=0,l=allColumns.length;i<l;i++) {
                var oColumn = allColumns[i];
                if (oColumn.getKey() == 'dataUrl' || oColumn.getKey() == 'id') {continue;}
                // Use the template
                elColumn = elTemplateCol.cloneNode(true);
                
                // Write the Column key
                elKey = elColumn.firstChild;
                elKey.innerHTML = oColumn.label;
                
                // Create a ButtonGroup
                oButtonGrp = new YAHOO.widget.ButtonGroup({ 
                                id: "buttongrp"+i, 
                                name: oColumn.getKey(), 
                                container: elKey.nextSibling
                });
                oButtonGrp.addButtons([
                    { label: "Show", value: "Show", checked: ((!oColumn.hidden)), onclick: onclickObj},
                    { label: "Hide", value: "Hide", checked: ((oColumn.hidden)), onclick: onclickObj}
                ]);
                                
                elPicker.appendChild(elColumn);
            }
            newCols = false;
    	}
        myDlg.show();
    };
    hideDlg = function(e) {
        this.hide();
    };
    handleButtonClick = function(e, oSelf) {
        var sKey = this.get("name");
        if(this.get("value") === "Hide") {
            // Hides a Column
            GRAILSUI.resources.hideColumn(sKey);
        }
        else {
            // Shows a Column
            GRAILSUI.resources.showColumn(sKey);
        }
    };
    
    // Create the SimpleDialog
    YAHOO.util.Dom.removeClass("dt-dlg", "inprogress");
    myDlg = new YAHOO.widget.SimpleDialog("dt-dlg", {
            width: "30em",
		    visible: false,
		    modal: true,
		    buttons: [ 
				{ text:"Close",  handler:hideDlg }
            ],
            fixedcenter: true,
            constrainToViewport: true
	});
	myDlg.render();

    // Nulls out myDlg to force a new one to be created
    GRAILSUI.resources.subscribe("columnReorderEvent", function(){
        newCols = true;
        YAHOO.util.Event.purgeElement("dt-dlg-picker", true);
        YAHOO.util.Dom.get("dt-dlg-picker").innerHTML = "";
    }, this, true);
//	NOt wanted - as interferes with right-clicking on url links.
//	/** context menu */
//        var onContextMenuClick = function(p_sType, p_aArgs, p_myDataTable) {
//            var task = p_aArgs[1];
//            if(task) {
//                // Extract which TR element triggered the context menu
//                var elRow = this.contextEventTarget;
//                elRow = p_myDataTable.getTrEl(elRow);
//
//                if(elRow) {
//                    switch(task.index) {
//                        case 0:     //open in new window
//                        	var oRecord = p_myDataTable.getRecord(elRow)
//                        	var url = mkOpenResourceUrl(oRecord.getData('id'))
//                        	window.open(url)
//                    }
//                }
//            }
//        };
//
//        var myContextMenu = new YAHOO.widget.ContextMenu("mycontextmenu",
//                {trigger:GRAILSUI.resources.getTbodyEl()});
//        myContextMenu.addItem("Show resource in new window");
//        // Render the ContextMenu instance to the parent container of the DataTable
//        myContextMenu.render("resourcesParent");
//        myContextMenu.clickEvent.subscribe(onContextMenuClick, GRAILSUI.resources);

   }); // end container.

// Hook up the SimpleDialog to the link(s) - wait until they become available
 YAHOO.util.Event.onAvailable('dt-options-link', function() {
     linkArr = YAHOO.util.Dom.getElementsByClassName("dt-options-link",'a',GRAILSUI.resources)
 	YAHOO.util.Event.addListener(linkArr, "click", showDlg, this, true);
	}); 