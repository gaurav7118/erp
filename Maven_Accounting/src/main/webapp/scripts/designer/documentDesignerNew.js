var selectedElement = null;
var headerId = null;
var footerId = null;
var headerJson = null;
var footerJson = null;
var headerHtml = null;
var footerHtml = null;
var globalHeader = null;
var innerHtmlArray = [];
var space = '&nbsp;';
var doubleSpace = '&nbsp;&nbsp;';
var defaultFieldGlobalStore;
var defaultFieldGlobalStoreForSearchRecord;
var defaultDimensionAndCustomStore;
var defaultDimensionAndCustomArray= [];
var summaryTableJson = null;
var isSummaryTableApplied = false;
var elementJson;
var isGroupingApplied = false;
var isFormattingApplied = false;
var isFormattingRowPresent = false;
var isSequnceChanged = false;
var isRowDeleted = false;
var isExtendedGlobalTable= false;
var pageSizeForExtGT= 'a4';
var pageOrientationForEXTGT= 'portrait';
var adjustPageHeight= '0';

// common functions

function checkSubStr(str,substr) {
    if (str.indexOf(substr) >=0 ) {
        return true;
    }
    return false
}

function insertAtCursorForTextArea(field,val) {
    
    var input = field.getEl().dom.children[0].children[0].children[1].children[0]
    var startPos = input.selectionStart;
    var endPos = input.selectionEnd;
    input.value = input.value.substring(0, startPos)
        + val
        + input.value.substring(endPos, input.value.length);
}

// Global Stores

var pageSizeStore=Ext.create("Ext.data.Store", {     //Store for Page Size type 
    fields: ["id", "size"],
    data: [
    {
        id: "a4",
        size: "A4"
    },
    {
        id: "letter",
        size: "US Letter"
    },
    {
        id: "a3",
        size: "A3"
    }
    ]
});

var pageOrientationStore=Ext.create("Ext.data.Store", {
    fields: ["id", "orientation"],
    data: [
     {
        id: "portrait",
        orientation: "Portrait"
    },
    {
        id: "landscape",
        orientation: "Landscape"
    }
    ]
});

var bulletStore = Ext.create("Ext.data.Store", {  // Bullet Store
        fields: ["id", "value"],
        data: [
        {
            id: "none",
            value: "None"
        }, {
            id: "disc",
            value: "Disc"
        }, {
            id: "circle",
            value: "Circle"
        }, {
        }, {
            id: "bigCircle",
            value: "Big Circle"
        }, {
            id: "square",
            value: "Square"
        }, {
            id: "decimal",
            value: "Decimal"
        }, {
            id: "decimal-leading-zero",
            value: "Decimal Leading Zero "
        }, {
            id: "lower-alpha",
            value: "Small Alphabets "
        }, {
            id: "lower-roman",
            value: "Small Roman"
        }, {
            id: "upper-alpha",
            value: "Big Alphabets"
        }, {
            id: "upper-roman",
            value: "Big Roman"
        }
        ]
});

 var lineStore =new Ext.data.Store({ //Store for border type 
        fields : ['id','type'],
        data : [
        {
            id: 'none', 
            type: 'None'
        },
        {
            id: "solid", 
            type: 'Solid'
        },
        
        {
            id: "dashed",    
            type: 'Dashed'
        },

        {
            id: "dotted", 
            type: 'Dotted'
        },
        {
            id: "double",
            type: 'Double'
        }
        
        ]
    });

 var pagefontstore= Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
        {
            "val":"", 
            "name":"None"
        },
        
        {
            "val":"sans-serif", 
            "name":"Sans-Serif"
        },

        {
            "val":"arial", 
            "name":"Arial"
        },

        {
            "val":"verdana", 
            "name":"Verdana"
        },

        {
            "val":"times new roman", 
            "name":"Times New Roman"
        },

        {
            "val":"tahoma", 
            "name":"Tahoma"
        },

        {
            "val":"calibri", 
            "name":"Calibri"
        },
        {
            "val":"courier new", 
            "name":"Courier New"
        },
        {
            "val":"MICR Encoding", 
            "name":"MICR"
        }
        ]
    });
    
    var alignStore=new Ext.data.Store({
        fields : ['id','align'],
        data : [
        {
            id: 'left', 
            align: 'Left'
        },

        {
            id: 'center',    
            align: 'Center'
        },

        {
            id: 'right', 
            align: 'Right'
        }

        ]
    });
    
    this.url = "ACCCurrency/getCurrency.do";
     this.fields = [{
                        name:'typeid',
                        type:'int'
                    }, {
                        name:'currency',
                        type:'string'
                    }];
        
        this.store = Ext.create('Ext.data.Store', {
        autoLoad: false,
        fields: this.fields,
        proxy: {
            type: 'ajax',
            url: this.url,
            reader: {
                type: 'json',
                rootProperty: "data[\"data\"]",
                keepRawData: true,
                totalProperty: "data[\"count\"]"
            }
        }
    });
    
    this.data = "";
    this.store.load();
        
    this.store.on("load", function() {
            this.data = this.store.getProxy().getReader().rawData.data.data;
        }, this);
        
        
    
/************************ Function to ToolTip creation ******************/
function createToolTip(target,ttip,title,anchor,trackMouse){
    Ext.create('Ext.tip.ToolTip',{
        target: target,       //Target to apply ToolTip (id of the component in which ttip has to be applied.)
        html: ttip,           //ToolTip message
        title:title,          //Title of ToolTip
        anchor: anchor,       //Position of ToolTip
        trackMouse: trackMouse//Move with mouse or not
    });
}

/******************** Function to convert RGB color code to Hex color code *************/
function rgbToHex(rgb,hashCheck){
 var hex = rgb;
 rgb = rgb.match(/^rgba?[\s+]?\([\s+]?(\d+)[\s+]?,[\s+]?(\d+)[\s+]?,[\s+]?(\d+)[\s+]?/i);
 if(hex.indexOf("rgb(") > -1){
        if(rgb && rgb.length === 4){
            if(hashCheck) {
                hex = "#" + ("0" + parseInt(rgb[1],10).toString(16)).slice(-2) + ("0" + parseInt(rgb[2],10).toString(16)).slice(-2) + ("0" + parseInt(rgb[3],10).toString(16)).slice(-2);
            } else{   
                hex = ("0" + parseInt(rgb[1],10).toString(16)).slice(-2) + ("0" + parseInt(rgb[2],10).toString(16)).slice(-2) + ("0" + parseInt(rgb[3],10).toString(16)).slice(-2);
            }
        }
    }
 return hex.toUpperCase();
}

/* Function to convert cm to px */
function cm2px(cm) {
//    1 cm = 37.795276 px;
//    var d = $("<div/>").css({
//        position: 'absolute', 
//        top : '-1000cm', 
//        left : '-1000cm', 
//        height : '1000cm', 
//        width : '1000cm'
//    }).appendTo('body');
//    var px_per_cm = d.height() / 1000;
//    d.remove();
//    return cm * px_per_cm;
    return parseFloat((cm * 37.80).toFixed(1));
}

function px2cm(px) {
//    1 px = 0.026458 cm
//    var d = $("<div/>").css({
//        position: 'absolute', 
//        top : '-1000cm', 
//        left : '-1000cm', 
//        height : '1000cm', 
//        width : '1000cm'
//    }).appendTo('body');
//    var px_per_cm = d.height() / 1000;
//    d.remove();
//    return px / px_per_cm;
    return parseFloat((px * 0.026458).toFixed(1));
}

/* Function to get DPI */
function getDPI(){
    // create an empty element
    var div = document.createElement("div");
    // give it an absolute size of one inch
    div.style.height="1in";
    div.style.width="1in";
    // append it to the body
    var body = document.getElementsByTagName("body")[0];
    body.appendChild(div);
    // read the computed height
    var dpi = document.defaultView.getComputedStyle(div, null).getPropertyValue('height');
    // remove it again
    body.removeChild(div);
    // and return the value
    return parseInt(dpi, 10);
}
/* Function to calculate page height */
function getPageHeight(ps,po,ptf){
    var ph = 1120.00;
    if ( ps === "a4" ) {
        if ( po === 'portrait' ) {
            ph = 11.49 * getDPI();
        } else {
            ph = 8.07 * getDPI();
        }
    } else if ( ps === "letter" ) {
        if ( po === 'portrait' ) {
            ph = 10.8 * getDPI();
        } else {
            ph = 8.3 * getDPI();
        }
    } else if ( ps === "a3" ) {
        if ( po === 'portrait' ) {
            ph = 16.34 * getDPI();
        } else {
            ph = 11.49 * getDPI();
        }
    }
    return ph;
}
/* Function to calculate page height for preprinted template */
function calculatepageheight(){
    var platform =  navigator.platform;                                               // checking platform  
    var isSpace = true;
    var mainPanel = getEXTComponent('idMainPanel');
    var rowcount = mainPanel.items.length;
    var pageHeight = 1120.00;
    pageHeight = getPageHeight("a4", "portrait",platform);                                                // getting page height depending upon page size and orientation.
//    pageHeight = Math.round(pageHeight);// in PX
//    pageHeight=parseFloat(pageHeight.toFixed(1));// in CM
    pageHeight = parseFloat(px2cm(pageHeight).toFixed(1));// in CM
//    var rowheightaddition = 25; // in PX
    var rowheightaddition = 1; //in CM
    var pageSettings = pagelayoutproperty[0].pagelayoutsettings;
    var tMargin = pageSettings == null || pageSettings == undefined ? 0 : pageSettings.pagetop;          // Fetching Top MArgin and bottom margin
    var bMargin = pageSettings == null || pageSettings == undefined ? 0 : pageSettings.pagebottom;
    
    var topMargin = 0;
    var bottomMargin = 0;
    if(tMargin.indexOf("px") > -1 && bMargin.indexOf("px") > -1){
        topMargin = parseInt(tMargin.replace("px",""));
        bottomMargin = parseInt(bMargin.replace("px",""));
    }else if(tMargin.indexOf("cm") > -1 && bMargin.indexOf("cm") > -1){             //Convert Margins from cm to pixels.
        topMargin = cm2px(parseFloat(tMargin.replace("cm","")));
        bottomMargin = cm2px(parseFloat(bMargin.replace("cm","")));
    }else if(tMargin.indexOf("mm") > -1 && bMargin.indexOf("mm") > -1){            //Convert Margins from mm to pixels. 
        topMargin = mm2px(parseFloat(tMargin.replace("mm","")));
        bottomMargin = mm2px(parseFloat(bMargin.replace("mm","")));
    }else if(tMargin.indexOf("in") > -1 && bMargin.indexOf("in") > -1){            //Convert Margins from inch to pixels.
        topMargin = in2px(parseFloat(tMargin.replace("in","")));
        bottomMargin = in2px(parseFloat(bMargin.replace("in","")));
    }
    
    topMargin = parseFloat(tMargin.replace("cm",""));
    bottomMargin = parseFloat(bMargin.replace("cm",""));
    
//    topMargin = Math.round(topMargin);
//    bottomMargin = Math.round(bottomMargin);
    
    rowheightaddition += topMargin;
    rowheightaddition += bottomMargin;
    
//    rowheightaddition += parseInt(tMargin.replace("px",""), 10);
//    rowheightaddition += parseInt(bMargin.replace("px",""), 10);
    for(var row = 0; row < rowcount; row++){
        rowheightaddition += mainPanel.items.items[row].rowHeight;
    }

    if(rowheightaddition > pageHeight){
        isSpace = false;
    }

    return isSpace;
}

/* Function to calculate page height when row height change for preprinted template*/
function calculatePageHeightOnHeightChange(newRowHeight){
    var platform =  navigator.platform;                                               // checking platform  
    var isSpace = true;
    var mainPanel = getEXTComponent('idMainPanel');
    var rowcount = mainPanel.items.length;
    var pageHeight = 1120.00;
    pageHeight = getPageHeight("a4", "portrait",platform);                                                // getting page height depending upon page size and orientation.
//    pageHeight=Math.round(pageHeight);// in PX
//    pageHeight=parseFloat(pageHeight.toFixed(1));// in CM
    pageHeight = parseFloat(px2cm(pageHeight).toFixed(1));// in CM
    var rowheightaddition = 0;
    var pageSettings = pagelayoutproperty[0].pagelayoutsettings;
    var tMargin = pageSettings == null || pageSettings == undefined ? 0 : pageSettings.pagetop;          // Fetching Top MArgin and bottom margin
    var bMargin = pageSettings == null || pageSettings == undefined ? 0 : pageSettings.pagebottom;

    var topMargin = 0;
    var bottomMargin = 0;
    if(tMargin.indexOf("px") > -1 && bMargin.indexOf("px") > -1){
        topMargin = parseInt(tMargin.replace("px",""));
        bottomMargin = parseInt(bMargin.replace("px",""));
    }else if(tMargin.indexOf("cm") > -1 && bMargin.indexOf("cm") > -1){             //Convert Margins from cm to pixels.
        topMargin = cm2px(parseFloat(tMargin.replace("cm","")));
        bottomMargin = cm2px(parseFloat(bMargin.replace("cm","")));
    }else if(tMargin.indexOf("mm") > -1 && bMargin.indexOf("mm") > -1){            //Convert Margins from mm to pixels. 
        topMargin = mm2px(parseFloat(tMargin.replace("mm","")));
        bottomMargin = mm2px(parseFloat(bMargin.replace("mm","")));
    }else if(tMargin.indexOf("in") > -1 && bMargin.indexOf("in") > -1){            //Convert Margins from inch to pixels.
        topMargin = in2px(parseFloat(tMargin.replace("in","")));
        bottomMargin = in2px(parseFloat(bMargin.replace("in","")));
    }
    
    topMargin = parseFloat(tMargin.replace("cm",""));
    bottomMargin = parseFloat(bMargin.replace("cm",""));
    
//    topMargin = Math.round(topMargin);
//    bottomMargin = Math.round(bottomMargin);
    
    rowheightaddition += topMargin;
    rowheightaddition += bottomMargin;
    
//    rowheightaddition += parseInt(tMargin.replace("px",""), 10);
//    rowheightaddition += parseInt(bMargin.replace("px",""), 10);
    for(var row = 0; row < rowcount; row++){
        rowheightaddition += parseFloat((mainPanel.items.items[row].rowHeight).toFixed(1));
    }

//    rowheightaddition += newRowHeight; // in PX
//    rowheightaddition += parseFloat((newRowHeight * 37.80).toFixed(1)) ; // in CM
    rowheightaddition += parseFloat((newRowHeight).toFixed(1)) ; // in CM

    if(rowheightaddition > pageHeight){
        isSpace = false;
    }

    return isSpace;
}

function getEXTComponent(id) {  // function to get extjs component.
    return Ext.getCmp(id);      // written, as if in future EXTJS changes its function for fetching component. We have to change only here.
}
function getEXTComponentStyle(cmp,prop) {     //  function to get component style.
    if( cmp ) {
        return cmp.getEl().dom.style;
    }
    return false;
}
function setEXTComponentStyle(cmp,prop,value) {   // function to set component Style.
    if ( cmp ) {
        if ( prop ) {
            cmp.getEl().dom.style[prop] = value;
        }
    }
}
function getEXTComponentInnerHTML(cmp) {  // function to get component innerHTML.
    if ( cmp ) {
        return cmp.getEl().dom.innerHTML; 
    }
    return false;
}
function setEXTComponentInnerHTML(cmp,html) {  //  function to set component innerHTML.
    if ( cmp ) {
        if ( html ) {
            cmp.getEl().dom.innerHTML = html;
        }
    }
}
function addGroupingRow(type) {
    var rowHtml = "";
    var lineTable = Ext.get("itemlistconfigsectionPanelGrid");
    if ( lineTable ) {
        var headerCells = lineTable.dom.childNodes[1].rows[0].cells;
        var firstRowCells  = lineTable.dom.childNodes[1].rows[1].cells;
        var lineItems = lineTable.dom.childNodes[1].rows[2].cells;
        if ( firstRowCells &&   firstRowCells[0] && firstRowCells[0].getAttribute('isLineItemWithPrefix')==null ) {
            //Show message for old table.
//            alert("a");
        } else {
            var totalCells = firstRowCells.length;
            var productNameCheck = 1;
            var thirdRow = lineTable.dom.childNodes[1].rows[2];
            var isGroupingRow = (thirdRow.attributes !=null && thirdRow.attributes.getNamedItem("isGroupingRow")) ?  thirdRow.attributes.getNamedItem("isGroupingRow").value : "no";
            var indexToAdd = 0;
            if ( isGroupingRow === "yes") {
                indexToAdd++;
            }
            if ( productNameCheck === 1 ) {
                var row;
                if ( type === 0) {
                     row = lineTable.dom.insertRow(3);
                } else {
                    row = lineTable.dom.insertRow(4+indexToAdd);
                }
                row.setAttribute("isGroupingRow","yes");
                for ( var cellIndex1 = 0 ; cellIndex1 < totalCells ; cellIndex1++ ) {
                    var cell = row.insertCell(cellIndex1);
                    var fieldid = headerCells[cellIndex1].attributes.getNamedItem("fieldid")!=null ? headerCells[cellIndex1].attributes.getNamedItem("fieldid").value : ""; 
                    var xtype = headerCells[cellIndex1].attributes.getNamedItem("xtype")!=null ? headerCells[cellIndex1].attributes.getNamedItem("xtype").value : ""; 
                    cell.setAttribute("style","padding:5px;");
                    var div = document.createElement('div');
                    div.setAttribute("class", "fieldItemDivBorder");
                    div.setAttribute("putIn",fieldid);
                    div.setAttribute("xtype",xtype);
                    if ( type===0) {
                        div.innerHTML = "Grouping Field";
                    } else {
                        div.innerHTML = "Formatting Field";
                        isFormattingRowPresent = true;
                    }
                    div.setAttribute("onClick", "getGroupingFieldPropertyPanel(event,this,"+type+")");

                    cell.appendChild(div);
                }
                
                rowHtml = row.outerHTML;
            } else {
                //show message for product name not found
//                alert("b");
            }
            
        }
    }
    return rowHtml;
}
 
function  removeGroupingRow(type) {
    var lineTable = Ext.get("itemlistconfigsectionPanelGrid");
    if ( lineTable ) {
        var groupingRow  = lineTable.dom.childNodes[1].rows[2];
        var isGroupingRow = (groupingRow.attributes !=null && groupingRow.attributes.getNamedItem("isGroupingRow")) ?  groupingRow.attributes.getNamedItem("isGroupingRow").value : "no";
        var indexToAdd=0;
        if ( isGroupingRow === "yes" ) {
            if ( type === 0 ) {
                lineTable.dom.deleteRow(3);
            }
            indexToAdd++;
        } 
        
        if ( type === 1) {
            var afterGroupingRow = lineTable.dom.childNodes[1].rows[3+indexToAdd];
            var isAfterGrooupingRow = (afterGroupingRow.attributes !=null && afterGroupingRow.attributes.getNamedItem("isGroupingRow")) ?  afterGroupingRow.attributes.getNamedItem("isGroupingRow").value : "no";
            if ( isAfterGrooupingRow === "yes") {
                lineTable.dom.deleteRow(4+indexToAdd);
            }
            isFormattingRowPresent = false;
        }
    }
}

function changeValOnUnitChange( newUnit,oldUnit,value) {
    var pxTocm = 0.0265;
    var pxTomm = 0.265;
    var pxToin = 0.0104;
    var cmTomm = 10;
    var cmToin = 0.394;
    var mmToin = 0.0394;
    switch ( oldUnit ) {
        case "px":
                switch ( newUnit ) {
                    case "cm":
                            return (value * pxTocm).toFixed(1);
                            break;
                    case "mm":
                            return (value * pxTomm).toFixed(0);
                            break;
                    case "in":
                            return ( value * pxToin).toFixed(2);
                            break;
                }
                break;
        case "cm":
                switch ( newUnit ) {
                    case "px":
                            return ( value * (1/pxTocm)).toFixed(0);
                            break;
                    case "mm":
                            return (value * cmTomm).toFixed(0);
                            break;
                    case "in":
                            return (value * cmToin ).toFixed(2);
                            break;
                }
                break;
        case "mm":
                switch ( newUnit ) {
                    case "px":
                            return ( value * (1/pxTomm)).toFixed(0);
                            break;
                    case "cm":
                            return (value * (1/cmTomm)).toFixed(1);
                            break;
                    case "in":
                            return (value * mmToin ).toFixed(2);
                            break;
                }
                break;
        case "in":
            switch ( newUnit ) {
                    case "px":
                            return ( value * (1/pxToin)).toFixed(0);
                            break;
                    case "mm":
                            return (value * (1/mmToin)).toFixed(0);
                            break;
                    case "cm":
                            return (value * (1/cmToin) ).toFixed(1);
                            break;
                }
                break;
    }
    return value;
}
    
function remove(ele) {
    var parent ;  
    try { 
        if ( ele ) {
            parent = ele.parentNode;
            if ( parent ) {
                parent.removeChild(ele);
                return; 
            } else {
                if ( Ext.isIE) {
                    // Code for IE
                    return;
                } else {
                    ele.remove();
                    return;
                }
            }
        }
//        showMessage("");// show message if element or parent does not exists
    } catch (e) {
//        showMessage("");//show Message if exception occurrs
    }
}
    
function showMessage(title,msg) {
    //A Common Function to show all the messages in Document designer.
    WtfComMsgBox([title, msg], 4);   
}
function addColumntoLineItemRows(cellIds,field,isFirstRow,cells) {
    
    var columns = [];
    var column;
    
    for (var i = 0 ; i < cellIds.length; i++) {
        if(cells && cells[i]){
            Ext.getElementById(cellIds[i]).appendChild(cells[i].getEl().dom);    
            cells[i].doLayout();
            columns.push(cells[i]);
        } else{
            column = createColumn(cellIds[i]);
            columns.push(column);
        }
    }
//    if(cells.length > 0){
//        columns = cells;
//    }
    if(isFirstRow){
        field.firstRowCells = columns;
    } else{
        field.lastRowCells = columns;
    }
}


function createColumn(parentid, obj ) {
    var marginTop = (obj && obj.marginTop) || (obj && obj.marginTop === 0)  ? obj.marginTop  : '0px';
    var marginBottom = (obj && obj.marginBottom) || (obj && obj.marginBottom === 0) ? obj.marginBottom : '0px';
    var marginLeft = (obj && obj.marginLeft)|| (obj && obj.marginLeft === 0) ? obj.marginLeft : '0px';
    var marginRight = (obj && obj.marginRight) || (obj && obj.marginRight === 0) ? obj.marginRight : '0px';
    var borderTop = (obj && obj.borderTop)  ? obj.borderTop : 'solid';
    var borderLeft = (obj && obj.borderLeft)  ? obj.borderLeft : 'solid';
    var borderBottom = (obj && obj.borderBottom)  ? obj.borderBottom : 'solid';
    var borderRight = (obj && obj.borderRight)  ? obj.borderRight : 'solid';
    var innerPanel = Ext.create("Ext.Panel", {
                fieldType: Ext.fieldID.insertColumnPanel,
                columnwidth: 100,
                columnWidth: 1,
                //resizable: true,
                autoHeight: true,
                marginTop : marginTop,
                marginLeft : marginLeft,
                marginBottom : marginBottom,
                marginRight : marginRight,
                borderTop : borderTop,
                borderLeft : borderLeft,
                borderBottom : borderBottom,
                borderRight : borderRight,
                cls : "sectionclass_lineitem_Field_Container",
                flex: 1,
                renderTo: parentid,
                listeners:
                {
                    render: function (c) {
                        c.el.dom.style.marginTop = marginTop+"px";
                        c.el.dom.style.marginBottom = marginBottom+"px";
                        c.el.dom.style.marginLeft = marginLeft+"px"; 
                        c.el.dom.style.marginRight = marginRight+"px";
                        c.getEl().on('click', function (e) {

                            if (selectedElement != null || selectedElement != undefined)
                            {
                                Ext.get(selectedElement).removeCls("selected");
                            }
                            selectedElement = this.id;
                            Ext.getCmp(selectedElement).addClass('selected');
                            e.stopPropagation( );
                            createPropertyPanelForGlobalRowColumn(selectedElement);
                            setPropertyForGlobalRowColumn(selectedElement);
                            showElements(selectedElement);

                        });
//                        c.getEl().on("contextmenu", function (event, ele) {
//                            if (selectedElement != null || selectedElement != undefined)
//                            {
//                                if(Ext.get(selectedElement)!=null){
//                                    Ext.get(selectedElement).removeCls("selected");
//                                }
//                            }
//                            selectedElement = this.id;
//                            Ext.getCmp(selectedElement).addClass('selected');
//                            event.stopEvent();
////                            selectedElement = ele.parentNode.parentNode.parentNode.id;
//                            contextMenu.showAt(event.getXY());
//                            return false;
//                        });


                    },
                    'resize': function () {
                    // var panel = this;
                    // panel.doLayout();
                    }
                }
            });
            
            return innerPanel;
}

function createPropertyPanelForGlobalRowColumn(id) {
    
    
    var borderComboTop = new Ext.form.field.ComboBox({
        columnWidth:.50,
        fieldLabel: 'Top',
        store: lineStore,
        id: 'borderComboTop',
        displayField: 'type',
        valueField: 'id',
        queryMode: 'local',
        width: 210,
        emptyText: 'Select a type',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        hidden : false,
        value :'none',
        listeners:{
            scope:this,
            change: function () {
                updatePropertyForGlobalRowColumn(id);
            }
        }
    });
    var borderComboBottom = new Ext.form.field.ComboBox({
        columnWidth:.50,
        fieldLabel: 'Bottom',
        store: lineStore,
        id: 'borderComboBottom',
        displayField: 'type',
        valueField: 'id',
        queryMode: 'local',
        width: 210,
        emptyText: 'Select a type',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        hidden : false,
        value :'none',
        listeners:{
            scope:this,
            change: function () {
                updatePropertyForGlobalRowColumn(id);
            }
        }
    });
    var borderComboLeft = new Ext.form.field.ComboBox({
        columnWidth:.50,
        fieldLabel: 'Left',
        store: lineStore,
        id: 'borderComboLeft',
        displayField: 'type',
        valueField: 'id',
        queryMode: 'local',
        width: 210,
        emptyText: 'Select a type',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        hidden : false,
        value :'none',
        listeners:{
            scope:this,
            change: function () {
                updatePropertyForGlobalRowColumn(id);
            }
        }
    });
    var borderComboRight = new Ext.form.field.ComboBox({
        columnWidth:.50,
        fieldLabel: 'Right',
        store: lineStore,
        id: 'borderComboRight',
        displayField: 'type',
        valueField: 'id',
        queryMode: 'local',
        width: 210,
        emptyText: 'Select a type',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        hidden : false,
        value :'none',
        listeners:{
            scope:this,
            change: function () {
                updatePropertyForGlobalRowColumn(id);
            }
        }
    });
    
    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updatePropertyForGlobalRowColumn(id);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updatePropertyForGlobalRowColumn(id);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updatePropertyForGlobalRowColumn(id);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updatePropertyForGlobalRowColumn(id);
            }
        }
    }
    var bordersettings = {
        xtype: 'fieldset',
        title: 'Border Settings',
        id: 'idbordersettings',
        width:270,
        items:[
        borderComboTop, borderComboBottom, borderComboLeft, borderComboRight 
        ]
    };
    var marginsettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:270,
        items:[
        topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };
    
    var fieldPropertyPanel =  Ext.create("Ext.Panel", {
        width: 300,
//        div:div,
        autoHeight:true,
        border: false,
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [
            marginsettings ,bordersettings//valueSettings,spaceSettings,fontSettings,,bordersettings,preTextSettings, postTextSettings,sequence
        ]
    });  
    
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
    if (propertyPanel){
        propertyPanelRoot.remove(propertyPanel.id);
    }
    propertyPanel = fieldPropertyPanel; 
    propertyPanelRoot.add(propertyPanel);
    Ext.getCmp("idEastPanel").setTitle("Global Row Column Property Panel");
    Ext.getCmp("idEastPanel").add(propertyPanelRoot);
    Ext.getCmp("idEastPanel").doLayout();
}

function setPropertyForGlobalRowColumn(id) {
    var eleCol = getEXTComponent(id);
    if ( eleCol ) {
        var topMargin = "0";  
        var leftMargin = "0"; 
        var rightMargin = "0";
        var bottomMargin = "0";
        var borderTop;
        var borderLeft;
        var borderBottom;
        var borderRight;
        
        topMargin = eleCol.marginTop;  
        leftMargin = eleCol.marginLeft;
        rightMargin = eleCol.marginRight;
        bottomMargin = eleCol.marginBottom;
        borderTop = eleCol.borderTop;
        borderLeft = eleCol.borderLeft;
        borderBottom = eleCol.borderBottom;
        borderRight = eleCol.borderRight;
        
        var topMarginNumberField = getEXTComponent("idtopmargin");      
        var leftMarginNumberField = getEXTComponent("idleftmargin");      
        var rightMarginNumberField = getEXTComponent("idrightmargin");      
        var bottomMarginNumberField = getEXTComponent("idbottommargin");  
        var bordertopCombo =  getEXTComponent("borderComboTop");
        var borderLeftCombo =  getEXTComponent("borderComboLeft");
        var borderBottomCombo =  getEXTComponent("borderComboBottom");
        var borderRightCombo =  getEXTComponent("borderComboRight");
        
        if ( topMarginNumberField ) {
            topMarginNumberField.setValue(topMargin);
        }
        if ( leftMarginNumberField ) {
            leftMarginNumberField.setValue(leftMargin);
        }
        if ( rightMarginNumberField ) {
            rightMarginNumberField.setValue(rightMargin);
        }
        if ( bottomMarginNumberField ) {
            bottomMarginNumberField.setValue(bottomMargin);
        }
        if ( bordertopCombo ) {
            bordertopCombo.setValue(borderTop);
        }
        if ( borderLeftCombo ) {
            borderLeftCombo.setValue(borderLeft);
        }
        if ( borderBottomCombo ) {
            borderBottomCombo.setValue(borderBottom);
        }
        if ( borderRightCombo ) {
            borderRightCombo.setValue(borderRight);
        }
        
        
    }
}

function updatePropertyForGlobalRowColumn(id) {
    var eleCol = getEXTComponent(id);
    if ( eleCol ) {
        var topMarginNumberField = getEXTComponent("idtopmargin");      
        var leftMarginNumberField = getEXTComponent("idleftmargin");      
        var rightMarginNumberField = getEXTComponent("idrightmargin");      
        var bottomMarginNumberField = getEXTComponent("idbottommargin");  
        var bordertopCombo =  getEXTComponent("borderComboTop");
        var borderLeftCombo =  getEXTComponent("borderComboLeft");
        var borderBottomCombo =  getEXTComponent("borderComboBottom");
        var borderRightCombo =  getEXTComponent("borderComboRight");
        
        var topMargin = "0";  
        var leftMargin = "0"; 
        var rightMargin = "0";
        var bottomMargin = "0";
        var borderTop;
        var borderLeft;
        var borderBottom;
        var borderRight;
        
        if ( topMarginNumberField ) {      
            topMargin = topMarginNumberField.getValue();     // fetching the value of top margin.
        }
        if ( leftMarginNumberField ) {     
            leftMargin = leftMarginNumberField.getValue();   // fetching the value of Left Margin.
        }
        if ( rightMarginNumberField ) {     
            rightMargin = rightMarginNumberField.getValue(); // fetching the value of right Margin. 
        }
        if ( bottomMarginNumberField) {    
            bottomMargin = bottomMarginNumberField.getValue(); // fetching the value of bottom margin.
        }
        if ( bordertopCombo ) {
            borderTop = bordertopCombo.getValue(); 
        }
        if ( borderLeftCombo ) {
            borderLeft = borderLeftCombo.getValue(); 
        }
        if ( borderBottomCombo ) {
            borderBottom = borderBottomCombo.getValue(); 
        }
        if ( borderRightCombo ) {
            borderRight = borderRightCombo.getValue(); 
        }
        setEXTComponentStyle(eleCol, "marginTop", topMargin + "px");       // updating top margin
        setEXTComponentStyle(eleCol, "marginLeft", leftMargin + "px");     // updating left margin
        setEXTComponentStyle(eleCol, "marginRight", rightMargin + "px");   // updating right margin
        setEXTComponentStyle(eleCol, "marginBottom", bottomMargin + "px")  // updating bottom margin
        eleCol.marginTop = topMargin;          // updating margin in text field obj . Done for creating json later 
        eleCol.marginBottom = bottomMargin;    //---------------------------,,------------------------------
        eleCol.marginRight = rightMargin;      //---------------------------,,------------------------------
        eleCol.marginLeft = leftMargin;        //---------------------------,,------------------------------
        
        // Get table border color - ERP-21864
        var tableBorderColor = Ext.getElementById("itemlistconfigsectionPanelGrid").style.borderColor;
        tableBorderColor = tableBorderColor.replace(/-moz-use-text-color/g, ""); // replaced -moz-use-text-color as it is getting appended automaticaly
        tableBorderColor = tableBorderColor.trim();
        var parentCell = eleCol.getEl().dom.parentNode;
        if( parentCell && parentCell.nodeName.toLowerCase() === "td" ) {
            
            if(bordertopCombo){
                if(borderTop!="none"){
                    parentCell.style.setProperty("border-top","1px "+ borderTop ); 
                    if ( borderTop === "double") {
                        parentCell.style.setProperty("border-top-width", "4px" );
                    } else {
                        parentCell.style.setProperty("border-top-width", "1px thin");
                    }
                }else{
                    parentCell.style.setProperty("border-top","none");
                }
            }  
            if(borderLeftCombo){
                if(borderLeft!="none"){
                    parentCell.style.setProperty("border-left","1px "+ borderLeft ); 
                    if ( borderLeft === "double") {
                        parentCell.style.setProperty("border-left-width", "4px" );
                    } else {
                        parentCell.style.setProperty("border-left-width", "1px thin");
                    }
                }else{
                    parentCell.style.setProperty("border-left","none");
                }
            }  
            if(borderBottomCombo){
                if(borderBottom!="none"){
                    parentCell.style.setProperty("border-bottom","1px "+ borderBottom ); 
                    if ( borderBottom === "double") {
                        parentCell.style.setProperty("border-bottom-width", "4px" );
                    } else {
                        parentCell.style.setProperty("border-bottom-width", "1px thin");
                    }
                }else{
                    parentCell.style.setProperty("border-bottom","none");
                }
            }  
            if(borderRightCombo){
                if(borderRight!="none"){
                    parentCell.style.setProperty("border-right","1px "+ borderRight ); 
                    if ( borderRight === "double") {
                        parentCell.style.setProperty("border-right-width", "4px" );
                    } else {
                        parentCell.style.setProperty("border-right-width", "1px thin");
                    }
                }else{
                    parentCell.style.setProperty("border-right","none");
                }
            }  
            parentCell.style.borderTopColor = tableBorderColor; //ERP-21864
            parentCell.style.borderLeftColor = tableBorderColor; //ERP-21864
            parentCell.style.borderBottomColor = tableBorderColor; //ERP-21864
            parentCell.style.borderRightColor = tableBorderColor; //ERP-21864
        }
        eleCol.borderTop = borderTop;
        eleCol.borderLeft = borderLeft;
        eleCol.borderBottom = borderBottom;
        eleCol.borderRight = borderRight;
        
        
    }
}

function addBullets(str,bulletType) {   // function to add bullets
    if ( str ) {
        var tempStr  = str;
        if ( bulletType === "bigCircle" ) {
            tempStr = "<ul class='ulbigcircle'><li>" + tempStr + "</li></ul>";  //  adding "ul" and "li" wherever line break or \n is present.
        } else {
            tempStr = "<ul style='list-style-type:" + bulletType + "'><li>" + tempStr + "</li></ul>";  //  adding "ul" and "li" wherever line break or \n is present.
        }
        tempStr = tempStr.replace(/\n|<br>|<\/br>|<br\/>/g, "<br></li><li>")
        return tempStr;
    }
    return str;
}

function remBullets(str) {    // function to remove bullets.
    if ( str ) {
        var tempStr = str;
        tempStr =  tempStr.replace(/<br><\/li><li>/g,"<br>");   // bullets have been removed from innerHTML here.
        tempStr = tempStr.replace("<ul><li>", "");
        tempStr = tempStr.replace("</li></ul>", "");
        return tempStr;
    }
    return str;
}

function validateJson(jsonStr) { 
    var json = {};
    try {                                 // If String passed in parameter is not a perfect json then this function will return an error message.
        json = JSON.parse(jsonStr);
    } catch ( e ) {
        return "Invalid data. Please enter valid data and try again.";
    }
    
    if (json[0].data  && json[0].data.length > 0) {                    // if a lineitem table is already added in the design then this block doesnot allow another line item table to add in the design
        if ( json[0].data[0].data && json[0].data[0].data.length > 0) { // Provided a length check 
            if (json[0].data[0].data[0].fieldType == 11) {
                var targetPanel = Ext.getCmp("idMainPanel");
                var component = targetPanel.queryById("itemlistcontainer");
                if (component) {
                    return 'Sorry only one line item is applicable at a time';
                } 
            }
        }
    }
    return true;
}

function createJson(obj) {

    var jsonData = {};
        var dataItems = [];

        jsonData["id"] = obj.id
        jsonData["column"] = obj.column;
        jsonData["fieldType"] = obj.fieldType;
        jsonData["layout"] = (obj.fieldType == Ext.fieldID.insertRowPanel) ? "column" : '';
        jsonData["draggable"] = obj.draggable;
        jsonData["unit"] = obj.unit;
        jsonData["width"] = obj.width;
        jsonData["height"] = obj.height;
        jsonData["columnWidth"] = obj.columnWidth;
        jsonData["columnwidth"] = obj.columnwidth;
        jsonData["x"] = obj.x;
        jsonData["y"] = obj.y;

        if (obj.fieldType == Ext.fieldID.insertText)
        {
            jsonData["elementwidth"] = obj.elementwidth;
            jsonData["labelhtml"] = obj.labelhtml;
            jsonData["label"] = obj.label;
            jsonData["textalignclass"] = obj.textalignclass;
            jsonData["textcolor"] = obj.textcolor;
            jsonData["textline"] = obj.textline;
            jsonData["bold"] = obj.bold;
            jsonData["italic"] = obj.italic;
            jsonData["fontsize"] = obj.fontsize;
            jsonData["fieldAlignment"] = obj.fieldAlignment;
            jsonData["elementheight"]=obj.elementheight;
            jsonData["iselementlevel"] = obj.iselementlevel;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom

        }

        if (obj.fieldType == Ext.fieldID.insertField )
        {
            jsonData["elementwidth"] = obj.elementwidth;
            jsonData["elementheight"]=obj.elementheight;
            jsonData["labelhtml"] = obj.labelhtml;
            jsonData["label"] = obj.label;
            jsonData["textalignclass"] = obj.textalignclass;
            jsonData["fontsize"] = obj.fontsize;
            jsonData["fieldAlignment"] = obj.fieldAlignment;
            jsonData["iselementlevel"] = obj.iselementlevel;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom;
            jsonData["bold"] = obj.bold;
            jsonData["italic"] = obj.italic;
            jsonData["textcolor"] = obj.textcolor;
            jsonData["isPreText"] = obj.isPreText;
            jsonData["isPostText"] = obj.isPostText;
            jsonData["preTextValue"] = obj.preTextValue;
            jsonData["preTextWordSpacing"] = obj.preTextWordSpacing;
            jsonData["postTextValue"] = obj.postTextValue;
            jsonData["postTextWordSpacing"] = obj.postTextWordSpacing;
            jsonData["preTextbold"] = obj.preTextbold;
            jsonData["postTextbold"] = obj.postTextbold;
            jsonData["preTextItalic"] = obj.preTextItalic;
            jsonData["postTextItalic"] = obj.postTextItalic;
            jsonData["valueSeparator"] = obj.valueSeparator;
            jsonData["dimensionValue"] = obj.dimensionValue;
            jsonData["valueWithComma"] = obj.valueWithComma;
            jsonData["decimalPrecision"] = obj.decimalPrecision?obj.decimalPrecision:_amountDecimalPrecision;
            jsonData["xType"] = obj.xType?obj.xType:"0";
            var arr = obj.labelhtml.match(/\{PLACEHOLDER:(.*?)}/g);
            if (arr && arr[0]) {
                var matches = arr[0].replace(/\{|\}/gi, '').split(":");
                jsonData['placeholder'] = matches[1];
                var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                if (obj.fieldType < "25") {
                    if (rec) {
                        var recdata = rec.data;
                        jsonData['reftablename'] = recdata.reftablename;
                        jsonData['reftablefk'] = recdata.reftablefk;
                        jsonData['reftabledatacolumn'] = recdata.reftabledatacolumn;
                        jsonData['dbcolumnname'] = recdata.dbcolumnname;
                        jsonData['fieldid'] = recdata.id;
                        jsonData['label'] = recdata.label;
                        jsonData['xtype'] = recdata.xtype;
                        jsonData['customfield'] = recdata.customfield;
                    }
                } else {
                    jsonData['fieldid'] = jsonData.id;
                    jsonData['label'] = jsonData.label;
                    jsonData['xtype'] = jsonData.xtype;
                }
            }
        }
        if (obj.fieldType == Ext.fieldID.insertImage)
        {
            jsonData["src"] = obj.src;
            jsonData["elementwidth"]=obj.elementwidth;
            jsonData["elementheight"]=obj.elementheight;
            jsonData["textalignclass"] = obj.textalignclass;
            jsonData["fieldAlignment"] = obj.fieldAlignment;
            jsonData["iselementlevel"] = obj.iselementlevel;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom
        }
        if (obj.fieldType == Ext.fieldID.insertTable)
        {

            /*
             var tmpwidth=parseFloat(Ext.getCmp('idWidth').getValue());
             jsonData["width"]=tmpwidth;
             */
            jsonData["labelhtml"] = obj.el.el.dom.childNodes[0].outerHTML;
            jsonData["bordercolor"] = obj.bordercolor;
            jsonData["tablewidth"] = obj.tablewidth;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom
            var dataItems = [];
            var columndata = [];
            columndata = documentLineColumns;
            
            var listItems = Ext.get('itemlistconfigsectionPanelGrid');

            if (listItems && listItems.dom.childNodes[1].rows[1].cells.length > 0) {

                var headeritems = [];
                var childItems = listItems.dom.childNodes[0].nextElementSibling.rows[0].cells;
                for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
                    var child = childItems[itemcnt];
                    var colSetting = child.attributes;
                    var childConfig = {};
                    for (var attrCnt = 0; attrCnt < colSetting.length; attrCnt++) {
                        if (colSetting[attrCnt].name != 'class' && colSetting[attrCnt].name != 'style')
                            childConfig[colSetting[attrCnt].name] = colSetting[attrCnt].value
                    }
                    headeritems.push(childConfig);
                }
                var lineitems = [];
                var oldtemplate=false;
                var childItems = listItems.dom.childNodes[1].rows[1].cells;
                if ( listItems.dom.childNodes[1].rows[1].cells.length > 0 ) {
                    for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
                        var divs = childItems[itemcnt].childNodes;
                        if ( divs[0].nodeName != "DIV" ) {
                            oldtemplate = true;
                            break;
                        }
                        for (var divcnt = 0; divcnt < divs.length; divcnt++) {
                            var child = divs[divcnt];
                            var colSetting = child.attributes;
                            var childConfig = {};
                            for (var attrCnt = 0; attrCnt < colSetting.length; attrCnt++) {
                                if (colSetting[attrCnt].name != 'class')
                                    childConfig[colSetting[attrCnt].name] = colSetting[attrCnt].value
                            }
                            lineitems.push(childConfig);
                        }
                    }
               }
               if (oldtemplate) {
                   lineitems = headeritems;
               }
//                var panelId = listItems.dom.attributes["panelid"].value;
//                var parentPanel = panelId?Ext.getCmp(panelId):"";
                var summaryJson = obj.ownerCt.items.items[0].summaryTableJson;
                var isSummaryTable =  obj.ownerCt.items.items[0].isSummaryTableApplied;
                var summarycellplaceholder = [];
                if(isSummaryTable) {
                    var summarytable = document.getElementById("summaryTableID").innerHTML;
                    var arr = summarytable.match(/\{PLACEHOLDER:(.*?)}/g);
                    if (arr && arr.length>0) {
                        for(var gCnt=0; gCnt<arr.length;gCnt++) {
                            var childConfig = {};
                            var matches = arr[gCnt].replace(/\{|\}/gi, '').split(":");
                            childConfig['placeholder'] = matches[1];
                            var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                            if (rec) {
                                var recdata = rec.data;
                                childConfig['reftablename'] = recdata.reftablename;
                                childConfig['reftablefk'] = recdata.reftablefk;
                                childConfig['reftabledatacolumn'] = recdata.reftabledatacolumn;
                                childConfig['dbcolumnname'] = recdata.dbcolumnname;
                                childConfig['fieldid'] = recdata.id;
                                childConfig['label'] = recdata.label;
                                childConfig['xtype'] = recdata.xtype;
                                childConfig['customfield'] = recdata.customfield;
                            }
                            summarycellplaceholder.push(childConfig);
                        }
                    }
                }
                var parentRowID = obj.ownerCt.ownerCt.id;
                var fontsize=obj.fontsize?obj.fontsize:""; 
                var tempallign=(obj.align!=null)?obj.align:"center";
                var align=(tempallign===0 || tempallign=="left")?"left":(tempallign==1 || tempallign=="center")?"center":"right"; 
                var bold=obj.bold?((obj.bold=="true")?true:false):false; 
                var italic=obj.italic?((obj.italic=="true")?true:false):false; 
                var underline=obj.underline?((obj.underline=="true")?true:false):false; 
                var bordercolor=obj.bordercolor?obj.bordercolor:"#FFFFFF"; 
                var lineConfig = {
                    'lineitems': lineitems,
                    'headeritems':headeritems,
                    id: obj.id,
                    x: obj.x,
                    y: obj.y,
                    height: Ext.get(obj.id).getBox().height,
                    width: Ext.get(obj.id).getBox().width,
                    fieldTypeId: obj.fieldTypeId,
                    //labelhtml: content,
                    tablecolor: this.tcolor,
                    tablebordermode: this.bmode,
                    summaryInfo: isSummaryTable?summaryJson:"",
                    cellplaceholder:isSummaryTable?summarycellplaceholder:"",
                    isSummaryTable:isSummaryTable,
                    parentrowid:parentRowID,
                    fontsize:fontsize,
                    bold:bold,
                    align:align,
                    italic:italic,
                    underline:underline,
                    bordercolor:bordercolor,
                    'columndata': JSON.stringify(columndata)
                //                    cellplaceholder:lineitems
                };
                dataItems.push(lineConfig);
            }

        }
        if (obj.fieldType == Ext.fieldID.insertGlobalTable)
        {
            jsonData["labelhtml"] = obj.el.el.dom.childNodes[0].outerHTML;
            jsonData["borderedgetype"]=obj.borderedgetype;
            jsonData["rowspacing"]=obj.rowspacing;
            jsonData["columnspacing"]=obj.columnspacing;
            jsonData["fieldAlignment"]=obj.fieldAlignment;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom
            jsonData["tableWidth"] = obj.tableWidth;
            jsonData["borderColor"] = obj.borderColor;
            jsonData["backgroundColor"] = obj.backgroundColor;
            jsonData["headerColor"] = obj.headerColor;
            jsonData["tableAlign"] = obj.tableAlign;
            var content = obj.el.getHTML();
            var arr = content.match(/\{PLACEHOLDER:(.*?)}/g);
            if (arr && arr.length>0) {
                var lineitems = [];
                for(var gCnt=0; gCnt<arr.length;gCnt++) {
                    var childConfig = {};
                    var matches = arr[gCnt].replace(/\{|\}/gi, '').split(":");
                    childConfig['placeholder'] = matches[1];
                    var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                    if (rec) {
                        var recdata = rec.data;
                        childConfig['reftablename'] = recdata.reftablename;
                        childConfig['reftablefk'] = recdata.reftablefk;
                        childConfig['reftabledatacolumn'] = recdata.reftabledatacolumn;
                        childConfig['dbcolumnname'] = recdata.dbcolumnname;
                        childConfig['fieldid'] = recdata.id;
                        childConfig['label'] = recdata.label;
                        childConfig['xtype'] = recdata.xtype;
                        childConfig['customfield'] = recdata.customfield;
                    }
                    lineitems.push(childConfig);
                }
            }
            jsonData["cellplaceholder"] = lineitems;
            jsonData["fixedrowvalue"] =obj.fixedrowvalue;
            
        }

        if (obj.fieldType == Ext.fieldID.insertRowPanel)
        {
            jsonData["isheader"] = obj.isheader;
            jsonData["isfooter"] = obj.isfooter;

        }
        if (obj.fieldType == Ext.fieldID.insertColumnPanel ){
            jsonData["fieldalignment"] = obj.fieldalignment;
            jsonData["type"] = obj.type;
            jsonData["borderedgetype"]=obj.borderedgetype;
            jsonData["allowborder"] = obj.allowborder;
            jsonData["backgroundColor"] = obj.backgroundColor;
            jsonData["linespacing"] = obj.linespacing;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom
        }
        
        jsonData["data"] = dataItems;

        return jsonData;


}

function getGlobalHeaderFooterJson(designerPanel, globalheaderid)
{
    var panelItems = designerPanel.items.items;
    var items = [];
    var jsonObj = [];

    for (var cnt = 0; cnt < panelItems.length; cnt++) {

        var obj_1 = panelItems[cnt];

        if (obj_1.id != globalheaderid)
            continue;

        jsonObj = createJson(obj_1);

        for (var i = 0; i < obj_1.items.items.length; i++)
        {
            var obj_2 = obj_1.items.items[i];
            var innerJsonObj_1 = createJson(obj_2);

            if (obj_2.items.items.length > 0)
            {
                for (var j = 0; j < obj_2.items.items.length; j++)
                {
                    var obj_3 = obj_2.items.items[j];
                    var innerJsonObj_2 = createJson(obj_3);
                    innerJsonObj_1.data.push(innerJsonObj_2);
                }

            }

            jsonObj.data.push(innerJsonObj_1);

        }

        if (jsonObj.isheader == true)
        {
            headerJson = jsonObj;
            headerHtml = document.getElementById(jsonObj.id).outerHTML;
        }

        if (jsonObj.isfooter == true)
        {
            footerJson = jsonObj;
            footerHtml = document.getElementById(jsonObj.id).outerHTML;

        }

        items.push(jsonObj);

    }
    return items;


}

function createPropertyPanel(obj)
{
    if (!obj)
        return;

    var ele = null;
    var fieldType = null;
    var propertyPanel = null;

    /* Page Property*/
    if (obj == "idMainPanel")
    {
        propertyPanel = createPagePropertyPanel();
    }
    else
    {
        ele = Ext.getCmp("idMainPanel").queryById(obj);
        if (ele === null) {
            ele = Ext.getCmp(obj);
        }
        fieldType = ele.fieldType;
    }

    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);

    /* Static text */
    if (fieldType == Ext.fieldID.insertText )
    {
        propertyPanel = createStaticTextPropertyPanel(ele);

    }

    /* Select Field */
    if (fieldType == Ext.fieldID.insertField)
    {
        propertyPanel = createSelectFieldPropertyPanel(ele);

    }
    /* Image Field */
    if (fieldType == Ext.fieldID.insertImage)
    {
        propertyPanel = createImagePropertyPanel(ele);

    }

    /* Row Panel*/
    if (fieldType == Ext.fieldID.insertRowPanel )
    {
        propertyPanel = createRowPropertyPanel();
    }
    /* Column Panel*/
    if (fieldType == Ext.fieldID.insertColumnPanel )
    {
        propertyPanel = createColumnPropertyPanel(ele);
    }
    /* Inline Table*/
    if (fieldType == Ext.fieldID.insertTable)
    {
        propertyPanel = createInlineTablePropertyPanel(ele);
    }
    if (fieldType == Ext.fieldID.insertGlobalTable)
    {
        propertyPanel = createGlobalTablePropertyPanel(ele);
    }
    if (fieldType == Ext.fieldID.insertDataElement)
    {
        propertyPanel = createDataElementPropertyPanel(ele);
    }
    if (fieldType == Ext.fieldID.insertAgeingTable)
    {
        propertyPanel = createAgeingTablePropertyPanel(ele);
    }
    if (fieldType == Ext.fieldID.insertGroupingSummaryTable)
    {
        propertyPanel = createGroupingSummaryTablePropertyPanel(ele);
    }
    //create property panel for Details Table
    if (fieldType == Ext.fieldID.insertDetailsTable)
    {
        propertyPanel = createDetailsTablePropertyPanel(ele);
    }
    propertyPanelRoot.add(propertyPanel);
    if (Ext.getCmp("idEasttPanel"))
        Ext.getCmp("idEastPanel").add(propertyPanelRoot);

    return propertyPanelRoot;
}
  

/* Used for InnerHtmlArray For Tree Panel-Neeraj*/
function TreeHtmlArray(targetpanelid,overModel,innerHtmlArray,dropPosition,recordindex,node)
{
    var columnlength=1;
    var targetpanel= Ext.getCmp(targetpanelid);
    if(targetpanel.items.items.length>0){
        columnlength=targetpanel.items.items.length;
        for(var i=0 ; i < targetpanel.items.items.length ; i++){/*Fields*/   
                if(overModel.data.id==targetpanel.items.items[i].id){
                    recordindex=i;
                }
                innerHtmlArray.push(targetpanel.items.items[i]);
        }
    }
    if(dropPosition=="after"&&recordindex=="0"){//recordindex is 0 and inserting node after 0;
        recordindex=recordindex+1;
    }else if(dropPosition=="before"&&recordindex==columnlength-1){//recordindex is last and inserting node before last record;
        recordindex=recordindex-1;
    }
   
    innerHtmlArray.splice(recordindex, 0, node);//inserting at particular node
    targetpanel.doLayout();
    return innerHtmlArray;
}

function removeObject(removedElement){
    summaryTableJson = null;
    isSummaryTableApplied = false;
    if (removedElement != "idMainPanel")
    {
        var targetPanel = Ext.getCmp(removedElement).ownerCt;
        if (Ext.getCmp(removedElement).fieldType == Ext.fieldID.insertRowPanel) {//if row is deleted then the global Header flag should be reset-ERP-12572
            if (Ext.getCmp(removedElement).isheader == true) {
                headerId = null;
            }
            if (Ext.getCmp(removedElement).isfooter == true) { //if row is deleted then the globalFooter flag should be
                footerId = null;
            }
            var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);//Resetting the property panel
            var propertyPanel = Ext.getCmp("idPropertyPanel");
            if (propertyPanel)
                propertyPanelRoot.remove(propertyPanel.id);
            propertyPanelRoot.doLayout();
            var mainPanel = Ext.getCmp("idMainPanel");
            mainPanel.remove(Ext.getCmp(removedElement));
        } else if (targetPanel.column == '1' && Ext.getCmp(removedElement).fieldType == "14") {//if row has one column and selected element is column
            var lineitems = Ext.getCmp(removedElement).items.items;

            for (var i = 0; i < lineitems.length; i++) {
                if (lineitems[i].fieldType == Ext.fieldID.insertTable) {//selected one column has line item reseting pagelayoutproperty 
                    pagelayoutproperty[1] = saveTableProperty("#000000", "borderstylemode1", "#FFFFFF");
                }
            }
            var mainPanel = Ext.getCmp("idMainPanel");
            mainPanel.remove(targetPanel);
            showElements("idMainPanel");
            removedElement = "idMainPanel";
            createPropertyPanel("idMainPanel");
            setProperty("idMainPanel");
        } else if (targetPanel.column != '1' && Ext.getCmp(removedElement).fieldType == "14") {//if row has more than one column then resize column
            targetPanel.remove(removedElement);
            targetPanel.column = (targetPanel.column - 1);
            setColumns(targetPanel, targetPanel.items.length, null, true);
            showElements(targetPanel.id);
            removedElement = targetPanel.id;
            createPropertyPanel(targetPanel.id)
            targetPanel.addClass("selected");
            setProperty(targetPanel.id);
        } else {
            if (Ext.getCmp(removedElement).fieldType == Ext.fieldID.insertTable) {
                // resetting pagelayoutproperty 
                pagelayoutproperty[1] = saveTableProperty("#000000", "borderstylemode1", "#FFFFFF");
                isGroupingApplied = false;
                isFormattingApplied = false;
                isSequnceChanged = false;
                isRowDeleted = false;
                isFormattingRowPresent = false;
            }
            targetPanel.remove(removedElement);
            if(targetPanel.items.items.length === 0){
                targetPanel.update(space);
            }
        }
        initTreePanel();
    }
}

function removeAllObjects(){
    var targetPanel = Ext.getCmp("idMainPanel");
    targetPanel.removeAll();
    initTreePanel();
    summaryTableJson = null;
    isSummaryTableApplied = false;
    headerId = null;
    footerId = null;
}

/*Tree Panel*/
var treeStore = Ext.create('Ext.data.TreeStore', {
    root: {
        expanded: true,
        children: [
        {
            id: 'idPageTree',
            text: 'Page'
        }
        ]
    }
});

var treePanel = Ext.create('Ext.tree.Panel', {
    width: 200,
    autoHeight:true,
    store: treeStore,
    rootVisible: false,
    folderSort: true,
    enableDD: true,
    border:false,
    animate:true,
    viewConfig: {//Config to show drag property 
        plugins: {
            ptype: 'treeviewdragdrop'
        },
        listeners: {
           
            /*Called before dropping the node*/
            beforedrop: function ( node, data, overModel, dropPosition, dropHandler, eOpts ){
                var treenodepanelid=overModel.data.id;
                var recordindex=0;
                var source = data.records[0].data;
                var target=overModel.data;
                if(source.leaf==true){//Allowing only the leaf node only to get dragged
                    /*Restricting to drag if item is dragged on rowPanel else delete from Panel*/
                    if((Ext.getCmp(treenodepanelid).fieldType==Ext.fieldID.insertRowPanel)||(Ext.getCmp(treenodepanelid).fieldType==Ext.fieldID.insertColumnPanel && dropPosition=="before")
                                ||Ext.getCmp(source.id).fieldType==Ext.fieldID.insertTable){
                        dropHandler.cancelDrop();  //Cancelling the drop
                        if(Ext.getCmp(source.id).fieldType==Ext.fieldID.insertTable){
                            WtfComMsgBox(["Warning", "Sorry you cannot move Line Items."], 0);
                        }
                    }else if(Ext.getCmp(treenodepanelid).fieldType==Ext.fieldID.insertTable){
                        dropHandler.cancelDrop(); 
                        WtfComMsgBox(["Warning", "Sorry you can not add any element in the row containing Line item."], 0);
                    }else{
                        this.innerHtmlArray = new Array();
                        /*If targetPanel and source panel are different checked on their leaf structure*/
                        if(source.leaf!=target.leaf && source.parentId!=target.parentId){
                            var previouspanelid=data.records[0].data.parentId;//Parent Id
                            var itemnodeid=source.id;//Item Id
                            overModel.insertChild(overModel.childNodes.length,data.records[0]);
                            if(Ext.getCmp(previouspanelid).fieldType==Ext.fieldID.insertColumnPanel){
                                var previouspanel= Ext.getCmp(previouspanelid);
                                var node= Ext.getCmp(itemnodeid);
                                this.innerHtmlArray.push(node);
                                previouspanel.remove(itemnodeid);//Pushing into array and deleting from oldpanel
                                previouspanel.doLayout();
                            }
                        }else if(source.parentId==target.parentId){//Same parent node
                             var previouspanelid= source.parentId;//Parent Id
                             var itemnodeid=source.id;//Item Id
                             var columnlength=1;
                            if(Ext.getCmp(previouspanelid).fieldType==Ext.fieldID.insertColumnPanel){
                                var previouspanel= Ext.getCmp(previouspanelid);
                                    if(previouspanel.items.items.length>0){
                                        columnlength=previouspanel.items.items.length;
                                        for(var i=0 ; i < previouspanel.items.items.length ; i++){/*Fields*/   
                                            if(itemnodeid!=previouspanel.items.items[i].id){//Not adding items in array when it is equal to itemnode
                                                if(target.id==previouspanel.items.items[i].id){
                                                    recordindex=i;
                                                }
                                            this.innerHtmlArray.push(previouspanel.items.items[i]);
                                        }
                                    }
                                }
                                if(dropPosition=="after"&&recordindex=="0"){//recordindex is 0 and inserting node after 0;
                                    recordindex=recordindex+1;
                                }else if(dropPosition=="before"&&recordindex==columnlength-1){//recordindex is last and inserting node before last record;
                                    recordindex=recordindex-1;
                                }
                                var node= Ext.getCmp(itemnodeid);//dragged item
                                this.innerHtmlArray.splice(recordindex, 0, node);//inserting at particular node in array
                                previouspanel.doLayout();
                            }
                        }else if(source.leaf==target.leaf && source.parentId!=target.parentId){//if one leaf of one parent when inserted between the leaf of another parent 
                            var previouspanelid=source.parentId;//Parent Id
                            var itemnodeid=source.id;//Item Id
                            var node= Ext.getCmp(itemnodeid);
                            /*Removing from previous panel id*/
                            if(Ext.getCmp(previouspanelid).fieldType==Ext.fieldID.insertColumnPanel){
                                var previouspanel= Ext.getCmp(previouspanelid);
                                previouspanel.remove(itemnodeid);//Pushing into array and deleting from oldpanel
                                previouspanel.doLayout();
                            }
                            var targetpanelid=target.parentId;//Parent Id
                            this.innerHtmlArray=TreeHtmlArray(targetpanelid,overModel,this.innerHtmlArray,dropPosition,recordindex,node);
                        }
                    }
                } else if((Ext.getCmp(treenodepanelid).fieldType==Ext.fieldID.insertRowPanel) || Ext.getCmp(source.id).fieldType==Ext.fieldID.insertRowPanel){
                    if((Ext.getCmp(treenodepanelid).fieldType==Ext.fieldID.insertColumnPanel) || overModel.data.leaf == true){
                        dropHandler.cancelDrop();  //Cancelling the drop
                    }
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        if(Ext.get(selectedElement)!=null){
                            Ext.get(selectedElement).removeCls("selected");
                        }
                    }
                    selectedElement = source.id;
                } else{
                     dropHandler.cancelDrop(); 
                }
            },
            /*Called after dropping the node*/
            drop: function (node, data, overModel, dropPosition) {   
                /*Creating fields and adding in respective columns*/
                    var innerPanel=null;
                    var fieldObj=null;
                    var field = null;
                     var columnpanelid=overModel.data.id;
                     var isleaf=overModel.data.leaf;
                    var fieldType = Ext.getCmp(columnpanelid).fieldType;
                     
                    if(!isleaf && (Ext.getCmp(columnpanelid).fieldType==Ext.fieldID.insertColumnPanel || Ext.getCmp(columnpanelid).fieldType==Ext.fieldID.insertRowPanel)){//if dropping on column
                        innerPanel=Ext.getCmp(columnpanelid);
                    } else{//if dropping other than column
                        innerPanel=Ext.getCmp(columnpanelid).ownerCt;
                        innerPanel.removeAll();//Pushing into array and removingall from oldpanel
                    }
                    if(fieldType!=Ext.fieldID.insertRowPanel){
                    if(this.innerHtmlArray.length>0){
                        for(var cnt=0;cnt<this.innerHtmlArray.length;cnt++){
                            fieldObj=this.innerHtmlArray[cnt];                  
                    
                            if (fieldObj.fieldType == Ext.fieldID.insertText)
                            {
                                field = createTextField(innerPanel,fieldObj.label, fieldObj);
                        
                            }
                            else if (fieldObj.fieldType == Ext.fieldID.insertField)
                            {
                                var xType = fieldObj.xType?fieldObj.xType:"0";
                                var isFormula = fieldObj.isFormula?fieldObj.isFormula:false;
                                field = createExtComponent_2(innerPanel, undefined, fieldObj.fieldType, fieldObj.labelhtml, fieldObj.x, fieldObj.y,null, fieldObj, fieldObj.label,xType, isFormula);
                            }
                            else if (fieldObj.fieldType == Ext.fieldID.insertImage)
                            {
                                field = createExtImgComponent(innerPanel, innerPanel, fieldObj.fieldType, fieldObj.src, fieldObj.x, fieldObj.y, fieldObj);
                            }
                            else if (fieldObj.fieldType == Ext.fieldID.insertDrawBox)
                            {
                                field = createBox(fieldObj);
                            }
//                            else if (fieldObj.fieldType == Ext.fieldID.insertTable)
//                            {
//                                var isSummaryTable=false;
//                                var summaryJson="";
//                                if(fieldObj.data){
//                                    if(fieldObj.data[0]){
//                                        if(fieldObj.data[0].isSummaryTable){
//                                            isSummaryTable = true;
//                                            summaryTableJson= fieldObj.data[0].summaryInfo;
//                                            summaryJson= fieldObj.data[0].summaryInfo;
//                                        }    
//                                    }    
//                                }
//                                field = getlineitemstable(fieldObj.x, fieldObj.y, fieldObj.labelhtml, innerPanel, fieldObj, isSummaryTable, summaryJson);
//                            }
                            else if (fieldObj.fieldType == Ext.fieldID.insertGlobalTable)
                            {
                                var borderedgetype = fieldObj.borderedgetype!=null?fieldObj.borderedgetype:"1";
                                var rowspacing = fieldObj.rowspacing!=null?fieldObj.rowspacing:"5";
                                var columnspacing = fieldObj.columnspacing!=null?fieldObj.columnspacing:"5";
                                var fieldAlignment = fieldObj.fieldAlignment!=null?fieldObj.fieldAlignment:"1"; 
                                var fixedrowvalue=fieldObj.fixedrowvalue!=null?fieldObj.fixedrowvalue:"";
                                var tableHeader=fieldObj.tableHeader!=null?fieldObj.tableHeader:false;
                                field = createGlobalTable(fieldObj.x, fieldObj.y, fieldObj.labelhtml, innerPanel, fieldObj.tableWidth, fieldObj.height,fixedrowvalue, Ext.fieldID.insertGlobalTable,borderedgetype,rowspacing,columnspacing,fieldAlignment,fieldObj,tableHeader);
                        
                            }else if (fieldObj.fieldType == Ext.fieldID.insertDataElement)
                            {
                                field = createDataElementComponent(innerPanel, fieldObj.fieldType, fieldObj.dataelementhtml, fieldObj.x, fieldObj.y,fieldObj);
                            }
                            innerPanel.add(field);
                            innerPanel.doLayout();
                        }
                         initTreePanel();//To refresh the tree after adding else while dragging into node it creates problem
                    }
                    }else{
                        var totalLength = Ext.getCmp("idMainPanel").items.items.length;
                        var indexOfDraggedRow;
                        var indexOfPreviousRow;
                        for (var i = 0; i < totalLength; i++) {
                            var item = Ext.getCmp("idMainPanel").items.items[i];
                            if (item.id === selectedElement) {
                                indexOfDraggedRow = i;
                            }
                            if(item.id === columnpanelid){
                                indexOfPreviousRow = i;
                            }
                        }
                        var dragUpOrDown =  indexOfDraggedRow - indexOfPreviousRow;
                        var json = getElementJson(Ext.getCmp(selectedElement), 1);
                        Ext.getCmp("idMainPanel").remove(Ext.getCmp(selectedElement));
                        if(dragUpOrDown > 0 || indexOfPreviousRow ==(totalLength-1)){
                            createExtElements(JSON.stringify(json), indexOfPreviousRow);
                        } else{
                            createExtElements(JSON.stringify(json), indexOfPreviousRow-1);
                        }
                        initTreePanel();
                    }
            this.innerHtmlArray= new Array();
            },                 
            notifyDrop: function (dragSource, event, data) {         
                var nodeId = data.node.id;         
                alert(nodeId);       
            },       
            notifyOver: function (dragSource, event, data) {         
                alert('over');
            }           
        }   
    },
    useArrows: true,//Showing arrows where moving
    listeners: {
        itemclick: function (view, rec, item, index, eventObject) {
            /*Other than Root Node*/
            if(rec.data.id!="idPageTree"){
                if (selectedElement != null || selectedElement != undefined)
                {
                    if(Ext.get(selectedElement)){
                        Ext.get(selectedElement).removeCls("selected");
                    }else{
                        Ext.get(rec.getId()).removeCls("selected");
                    
                    }
                }
                selectedElement = rec.getId();
                Ext.getCmp(selectedElement).addClass('selected');  
            }else{
                Ext.get("idMainPanel").removeCls("selected");
                if (selectedElement != null || selectedElement != undefined){
                    if(Ext.get(selectedElement)){
                        Ext.get(selectedElement).removeCls("selected");
                    }
                }
                selectedElement = "idMainPanel";
                eventObject.stopPropagation( );
                Ext.getCmp(selectedElement).addClass('selected');
            }
            createPropertyPanel(selectedElement);
            setProperty(selectedElement);
            showElements(selectedElement);
        }
    }

});

/*Tree Panel*/
function initTreePanel()
{

    var treeNode = treePanel.getRootNode();
    treeNode.getChildAt(0).removeAll();
    for (var i = 0; i < Ext.getCmp("idMainPanel").items.items.length; i++)
    {
        var tmpRowNode = Ext.getCmp("idMainPanel").items.items[i];
        var str = '';
        if (tmpRowNode.isheader == true)
            str = "Header";
        else if (tmpRowNode.isfooter == true)
            str = "Footer";
        else
            str = "Row - "+(i+1);



        treeNode.getChildAt(0).appendChild({
            id: tmpRowNode.id,
            text: str
        });

        for (var j = 0; j < Ext.getCmp(tmpRowNode.id).items.items.length; j++)
        {
            var tmpColumnNode = Ext.getCmp(tmpRowNode.id).items.items[j];
            treeNode.getChildAt(0).getChildAt(i).appendChild({
                id: tmpColumnNode.id,
                text: 'Column - '+(j+1)
            });

            for (var k = 0; k < Ext.getCmp(tmpColumnNode.id).items.items.length; k++)
            {
                var tmpInnerNode = Ext.getCmp(tmpColumnNode.id).items.items[k];
                var nodeId = tmpInnerNode.id;
                var tmpText = '';

                if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertText) {
                    if(Ext.getElementById(nodeId).children[0] && Ext.getElementById(nodeId).children[0].nodeName=="UL"){
                        tmpText = Ext.getElementById(nodeId).children[0].children[0].innerHTML;
                    }else{
                        tmpText = Ext.util.Format.ellipsis(Ext.getCmp(nodeId).label, 15);
                    }
                    if(tmpText.indexOf("<br>")!= -1){
                        tmpText = tmpText.substr(0, tmpText.indexOf("<br>"));
                    }
                        
                }else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertField) {
                    tmpText = Ext.getCmp(nodeId).label;
                } else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertImage) {
                    tmpText = "Image";
                } else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertDrawBox) {
                    tmpText = "Box";
                } else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertTable) {
                    tmpText = "Inline Table";
                } else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertGlobalTable) {
                    tmpText = "Global Table";
                } else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertDataElement) {
                    tmpText = "Data Element";
                } else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertAgeingTable && _CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID) {
                    tmpText = "Checklist Table";
                }  else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertAgeingTable) {
                    tmpText = "Ageing Table";
                } else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertGroupingSummaryTable) {
                    tmpText = "Grouping Summary Table";
                } else if (Ext.getCmp(nodeId).fieldType == Ext.fieldID.insertDetailsTable) {
                    tmpText = "Details Table";
                }
                var originalText = tmpText;
                if (tmpText.length > 15)
                {
                    tmpText = tmpText.substring(0, 15);
                    tmpText = tmpText + "...";
                }

                treeNode.getChildAt(0).getChildAt(i).getChildAt(j).appendChild({
                    id: nodeId,
                    text: tmpText,
                    qtip : originalText,
                    leaf: true
                                });

            }


        }

    }
    treeNode.expandChildren(true);

}
    
/*Page Property Panel*/
function createPagePropertyPanel() {

    var propertyPanelRoot = getEXTComponent(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = getEXTComponent("idPropertyPanel");
    var fontsyleflag=false;
    var pagestyle,pagefont="Sans-Serif",pageheightvalue="11.00",pagewidthvalue="8.5",pagesizevalue="A4", pagefontsize="12";
    var pagetopvalue="72px",pagebottomvalue="140px",pageleftvalue="72px",pagerightvalue="49px",pageorientationvalue="Portrait";
    var ispreprinted = false;
    var topPageBorder = false;
    var bottomPageBorder = false;
    var leftPageBorder = false;
    var rightPageBorder= false;
    var allowpageBorder = false;
    var ismultipletransaction = false;
//    var pageborderType = "solid"; // ERP-18286 : Dotted page border is applying for only first page in print
    var pagemarginunit = "px";
    var defaultMargin = "";
    if(pagelayoutproperty[0].pagelayoutsettings){
        pagestyle=pagelayoutproperty[0].pagelayoutsettings;
        pagefontsize=pagestyle.pagefontsize?pagestyle.pagefontsize:"12";
        pagefont=pagestyle.pagefont?pagestyle.pagefont:"Sans-Serif";
        pageheightvalue=pagestyle.pageheight?pagestyle.pageheight:"11.00";
        pagewidthvalue=pagestyle.pagewidth?pagestyle.pagewidth:"8.5";
        pageorientationvalue=pagestyle.pageorientation?pagestyle.pageorientation:"Portrait";
        pagesizevalue=pagestyle.pagesize?pagestyle.pagesize:"A4";
        ispreprinted=pagestyle.ispreprinted?pagestyle.ispreprinted:false;
        allowpageBorder = pagestyle.pageBorderIncluded?pagestyle.pageBorderIncluded:false;
        topPageBorder = pagestyle.topPageBorder?pagestyle.topPageBorder:false;
        leftPageBorder = pagestyle.leftPageBorder?pagestyle.leftPageBorder:false;
        rightPageBorder = pagestyle.rightPageBorder?pagestyle.rightPageBorder:false;
        bottomPageBorder = pagestyle.bottomPageBorder?pagestyle.bottomPageBorder:false;
        pagemarginunit = pagestyle.pagemarginunit?pagestyle.pagemarginunit:"px";
        ismultipletransaction = pagestyle.ismultipletransaction?pagestyle.ismultipletransaction:false;
//        pageborderType = pagestyle.pageBorderType?pagestyle.pageBorderType:"solid";   // ERP-18286 : Dotted page border is applying for only first page in print
        if ( pageMarginUnit == "px" ) {
            defaultMargin = "25";
        } else if ( pageMarginUnit == "mm" ) {
            defaultMargin = "6.4";
        } else if ( pageMarginUnit == "cm") {
            defaultMargin = "0.64";
        } else {
            defaultMargin = "0.25";
        }
                
        pagetopvalue=pagestyle.pagetop?pagestyle.pagetop:defaultMargin;
        pagebottomvalue=pagestyle.pagebottom?pagestyle.pagebottom:defaultMargin;
        pageleftvalue=pagestyle.pageleft?pagestyle.pageleft:defaultMargin;
        pagerightvalue=pagestyle.pageright?pagestyle.pageright:defaultMargin;
    }

if (propertyPanel)
        propertyPanelRoot.remove(propertyPanel.id);

    var elementId = {
        xtype: 'textfield',
        fieldLabel: 'Page ID',
        id: 'idElementId',
        hidden:true,         //Not need to show id of element
        //disabled:true,
        readOnly: true
    };
    
    var pageSize = {
        xtype: 'combo',
        fieldLabel: 'Page size',
        id: 'idPageSize',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "size"],
            data: [
            {
                id: "1",
                size: "A4"
            }, {
                id: "2",
                size: "US Letter"
            },{
                id: "3",
                size: "A5"
            }
            ]
        }),
        value:pagesizevalue,
        displayField: 'size',
        valueField: 'id',
        width: 200,
        padding:'2 2 2 2',
        labelWidth: 100,
        listeners: {
            'change': function ()
            {
                updateProperty(selectedElement);

            }
        }
    };
    var insertHTML = {
        xtype: 'button',
        id: 'idinserttHTMLBtn',
        width: 60,
        height: 75,
        cls:'insertRowBtn',
        padding:'2 2 2 2',
        margin :'5 5 5 5',
        listeners: {
            afterrender: function() {
                createToolTip('idinserttHTMLBtn', 'Insert Row', '', 'top', true);
            },
//            scope:this,
            'click': function()
            {
                var InsertHTMLWin = Ext.create('Ext.window.Window', {
                    title: 'Add Data',
                    height: 500,
                    width: 400,
                    id:"idInsertHTMLWin",
                    layout: 'fit',
                    modal:true,              //ERP-19208
                    items: {
                        xtype: 'textarea',
                        border: false,
                        height: 500,
                        id:"svj",
                        width: 400,
                        autoScroll:true,
//                        value: JSON.stringify(getElementJson(Ext.getCmp(selectedElement)),null,4)
                        listeners: {
                            change: function (){
                                var textContent=getEXTComponent('svj').getValue().trim();
                                if(textContent === ''){
                                    getEXTComponent('idaddrow').setDisabled(true);
                                }else{
                                    getEXTComponent('idaddrow').setDisabled(false);
                                }
                            }
                        } 
                    },
                    buttons:[
                       {
                            text: 'Add',
                            scope:this,
                            id:'idaddrow',
                            disabled:true,
                            handler: function() {
                                var str = getEXTComponent("svj").getValue();
                                var retValue = validateJson(str);
                                if (retValue === true) {
                                    createExtElements(str);
                                    initTreePanel();
                                    if (getEXTComponent("idInsertHTMLWin")) {
                                        getEXTComponent("idInsertHTMLWin").close();
                                    }
                                } else {
                                   WtfComMsgBox(["Invalid", retValue], 0); 
                                }
                            }
                        },
                        {
                            text:"Close",
                            scope:this,
                            handler: function() {
                                if ( getEXTComponent("idInsertHTMLWin")) {
                                    getEXTComponent("idInsertHTMLWin").close();
                                }
                            }
                        }
                    ]
                }).show();
            }
        }
    };
    var removeAllObjectsBtn = {
        xtype: 'button',
        id: 'idremoveAllObjectsBtn',
        width: 60,
        height: 75,
        cls:'removeAllObjBtn',
        padding:'2 2 2 2',
        margin :'5 5 5 5',
        handler:function(){
            removeAllObjects();
        },
        listeners: {
            afterrender: function() {
                createToolTip('idremoveAllObjectsBtn', 'Remove All Object(s)', '', 'top', true);
            }
        }
    };
    
    var addWaterMarkButton = {
        xtype: 'button',
        id: 'addwatermarkbutton',
        width: 65,
        height: 75,
        cls: 'addWatermarkBtnIcon',
        padding:'2 2 2 2',
        margin :'5 5 5 5',
        scope: this,
        listeners: {
            afterrender: function() {
                createToolTip('addwatermarkbutton', 'Create Watermark', '', 'top', true);
            }
        },
        handler:function(){
            var config = {
                pageMarginUnit: Ext.getCmp('idPageMarginUnit').getValue(),
                pageMargin: {
                    top: Ext.getCmp("idpagetop").getValue(),
                    left: Ext.getCmp("idpageleft").getValue(),
                    bottom: Ext.getCmp("idpagebottom").getValue(),
                    right: Ext.getCmp("idpageright").getValue()
                }
            }
            getExtWatermarkWindow(config);
        }
    }
    
    var pageOrientation = {
        xtype: 'combo',
        fieldLabel: 'Orientation',
        id: 'idOrientation',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "orientation"],
            data: [
            {
                id: "1",
                orientation: "Landscape"
            }, {
                id: "2",
                orientation: "Portrait"
            }
            ]
        }),
        value:pageorientationvalue,
        displayField: 'orientation',
        valueField: 'id',
        width: 200,
        padding:'2 2 2 2',
        labelWidth: 100,
        listeners: {
            'change': function ()
            {
                updateProperty(selectedElement);

            }
        }
    };

    var updateBtn = {
        xtype: 'button',
        text: 'Update',
        id: 'idUpdateBtn',
        width: 55,
        style: {
            'float': 'right',
            'margin-right': '25px'
        },
        listeners: {
            'click': function ()
            {
                updateProperty(selectedElement);

            }
        }
    };
    var pagelayoutsettings = {
      xtype: 'fieldset',
      title: 'Page Layout Settings',
      id:'idpagelayoutsettingfieldset',
      hidden: !ispreprinted,
      width:270,
      items:[
        elementId, pageSize, pageOrientation  
      ]
    };
    /*Page Font Settings*/
           var pagefontstore= Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
            {"val":"sans-serif", "name":"Sans-Serif"},
            {"val":"arial", "name":"Arial"},
            {"val":"verdana", "name":"Verdana"},
            {"val":"times new roman", "name":"Times New Roman"},
            {"val":"tahoma", "name":"Tahoma"},
            {"val":"calibri", "name":"Calibri"},
            {"val":"courier new", "name":"Courier New"},
            {"val":"MICR Encoding", "name":"MICR"}
        ]
    });
    
    
    var pagefontfields = {
        xtype: 'combo',
        fieldLabel: 'Font Family',
        id:'pagefontid',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        value : pagefont,
        padding:'2 2 2 2',
        emptytext:'sans-serif',
        labelWidth:100,
        width:200,
        listeners: {
            'select': function (e) {
                    updateProperty(selectedElement);
            }
        }
    };
   
     var fontSize = {
        xtype: 'numberfield',
        fieldLabel: 'Font Size',
        id: 'idFontSize',
        value: pagefontsize,
        width: 200,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        listeners: {
            'change': function () {
                updateProperty(selectedElement);
            }
        }
    };
    var pageFonts = {
        xtype: 'fieldset',
        title: 'Font Settings',
        id:'idpagefontsettingfieldset',
        width:270,
        items:[
            fontSize,pagefontfields  
        ]
    };
    
    /*Page Margins*/
    
    var pageleft = {
        xtype: 'numberfield',
        fieldLabel: 'Left',
        id: 'idpageleft',
        value:pageleftvalue ,
        width: 200,
        minLength: 1,
        padding:'2 2 2 2',
        listeners: {
            'change': function(){
                updateProperty(selectedElement);
            }
        }
    };

        var pageright = {
        xtype: 'numberfield',
        fieldLabel: 'Right',
        id: 'idpageright',
        value:pagerightvalue ,
        width: 200,
        minLength: 1,
        padding:'2 2 2 2',
        listeners: {
            'change': function(){
                 updateProperty(selectedElement);
            }
        }
    };
    
        var pagebottom = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom',
        id: 'idpagebottom',
        value:pagebottomvalue ,
        width: 200,
        minLength: 1,
        padding:'2 2 2 2',
        listeners: {
            'change': function(){
               updateProperty(selectedElement);
            }
        }
    };
    
   var pagetop= {
        xtype: 'numberfield',
        fieldLabel: 'Top',
        id: 'idpagetop',
        value:pagetopvalue ,
        width: 200,
        minLength: 1,
        padding:'2 2 2 2',
        listeners: {
            'change': function(){
                updateProperty(selectedElement);
            }
        }
    };
    
    var lineStore=Ext.create('Ext.data.Store', {
        fields : ['id','type'],
        data : [
        {
            id: "none", 
            type: 'None'
        },
        {
            id: "solid", 
            type: 'Solid'
        },
        
        {
            id: "dashed",    
            type: 'Dashed'
        },

        {
            id: "dotted", 
            type: 'Dotted'
        },
        {
            id: "double",
            type: 'Double'
        }
        
        ]
    });
     
    var pageBorderType = {
        xtype: 'combo',
        fieldLabel: 'Page Border Type',
        id:'pageBorderTypeid',
        displayField: 'type',
        valueField: 'id',
        store: lineStore,
//        value :pageborderType,
        padding:'2 2 2 2',
        emptytext:'sans-serif',
        hidden: true,//!allowpageBorder,  // ERP-18286 : Dotted page border is applying for only first page in print
        labelWidth:100,
        width:210,
        listeners: {
            'select': function (e) {
                    updateProperty(selectedElement);
            }
        }
    }; 
     
    var allowPageBorder = {
        xtype: 'checkbox',
        fieldLabel: 'Allow Border',
        id:'allowPageBorderid',
        value: allowpageBorder,
        padding:'2 2 2 2',
        checked:allowpageBorder,
        listeners : {
            'change' : function(combo,newVal,oldVal) {
                if ( newVal ) {
                    getEXTComponent("allowTopPageBorderid").setDisabled(false);
                    getEXTComponent("allowBottomPageBorderid").setDisabled(false);
                    getEXTComponent("allowLeftPageBorderid").setDisabled(false);
                    getEXTComponent("allowRightPageBorderid").setDisabled(false);
//                    Ext.getCmp("pageBorderTypeid").show();   // ERP-18286 : Dotted page border is applying for only first page in print
                    getEXTComponent("allowTopPageBorderid").setValue(true);
                    getEXTComponent("allowBottomPageBorderid").setValue(true);
                    getEXTComponent("allowLeftPageBorderid").setValue(true);
                    getEXTComponent("allowRightPageBorderid").setValue(true);
//                    Ext.getCmp("pageBorderTypeid").setValue("solid");   // ERP-18286 : Dotted page border is applying for only first page in print
                } else {
                    getEXTComponent("allowTopPageBorderid").setDisabled(true);
                    getEXTComponent("allowBottomPageBorderid").setDisabled(true);
                    getEXTComponent("allowLeftPageBorderid").setDisabled(true);
                    getEXTComponent("allowRightPageBorderid").setDisabled(true);
//                    Ext.getCmp("pageBorderTypeid").hide();    // ERP-18286 : Dotted page border is applying for only first page in print
                    getEXTComponent("allowTopPageBorderid").setValue(false);
                    getEXTComponent("allowBottomPageBorderid").setValue(false);
                    getEXTComponent("allowLeftPageBorderid").setValue(false);
                    getEXTComponent("allowRightPageBorderid").setValue(false);
//                    Ext.getCmp("pageBorderTypeid").setValue("none");  // ERP-18286 : Dotted page border is applying for only first page in print
                }
                updateProperty(selectedElement);
                
            }
        }
    }
    var allowTopPageBorder = {
        xtype: 'checkbox',
        fieldLabel: 'Top',
        id:'allowTopPageBorderid',
        value: topPageBorder,
        padding:'2 2 2 2',
        checked:topPageBorder,
        disabled:!allowpageBorder,
        listeners : {
            'change' : function() {
                updateProperty(selectedElement);
            }
        }
    }
    var allowBottomPageBorder = {
        xtype: 'checkbox',
        fieldLabel: 'Bottom',
        id:'allowBottomPageBorderid',
        value: bottomPageBorder,
        padding:'2 2 2 2',
        checked:bottomPageBorder,
        disabled:!allowpageBorder,
        listeners : {
            'change' : function() {
                updateProperty(selectedElement);
            }
        }
    }
    var allowLeftPageBorder = {
        xtype: 'checkbox',
        fieldLabel: 'Left',
        id:'allowLeftPageBorderid',
        value: leftPageBorder,
        padding:'2 2 2 2',
        checked:leftPageBorder,
        disabled:!allowpageBorder,
        listeners : {
            'change' : function() {
                updateProperty(selectedElement);
            }
        }
    }
    var allowRightPageBorder = {
        xtype: 'checkbox',
        fieldLabel: 'Right',
        id:'allowRightPageBorderid',
        value: rightPageBorder,
        padding:'2 2 2 2',
        checked:rightPageBorder,
        disabled:!allowpageBorder,
        listeners : {
            'change' : function() {
                updateProperty(selectedElement);
            }
        }
    }
//    
    var pageMarginUnit = {
        xtype: 'combo',
        fieldLabel: 'Unit',
        id: 'idPageMarginUnit',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "size"],
            data: [
            {
                id: "px",
                size: "Pixels"
            }, {
                id: "mm",
                size: "Milli Meters"
            },{
                id: "cm",
                size: "Centi Meters"
            },{
                id: "in",
                size: "Inches"
            }
            ]
        }),
        value:pagemarginunit,
        displayField: 'size',
        valueField: 'id',
        width: 200,
        padding:'2 2 2 2',
        labelWidth: 100,
        listeners: {
            'change': function (combo, newVal,oldVal)
            {   
                getEXTComponent("idpagetop").setValue(changeValOnUnitChange(newVal,oldVal,getEXTComponent("idpagetop").getValue()));
                getEXTComponent("idpagebottom").setValue(changeValOnUnitChange(newVal,oldVal,getEXTComponent("idpagebottom").getValue()));
                getEXTComponent("idpageright").setValue(changeValOnUnitChange(newVal,oldVal,getEXTComponent("idpageright").getValue()));
                getEXTComponent("idpageleft").setValue(changeValOnUnitChange(newVal,oldVal,getEXTComponent("idpageleft").getValue()));
                updateProperty(selectedElement);

            }
        }
    };

    var pageMargins = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id:'idpagemarginsettingfieldset',
        width:270,
        items:[
             pageMarginUnit,pagetop, pagebottom,pageleft,pageright
        ]
    };
    
    var pageBorders = {
        xtype: 'fieldset',
        title: 'Border Settings',
        id:'idpagebordersettingfieldset',
        width:270,
        items:[
             allowPageBorder,allowTopPageBorder,allowBottomPageBorder,allowRightPageBorder,allowLeftPageBorder,pageBorderType
        ]
    };
    
    /*Page Sizes*/
       var pagewidth= {
        xtype: 'numberfield',
        fieldLabel: 'Width',
        id: 'idpagewidth',
        value:pagewidthvalue ,
        width: 200,
        minLength: 1,
        padding:'2 2 2 2',
        maxLength: 3,
        allowDecimals: true,
        decimalPrecision:2,
        listeners: {
            'change': function(){
                updateProperty(selectedElement);
            }
        }
    };
    
       var pageheight= {
        xtype: 'numberfield',
        fieldLabel: 'Height',
        id: 'idpageheight',
        value:pageheightvalue ,
        width: 200,
        minLength: 1,
        padding:'2 2 2 2',
        maxLength: 3,
        allowDecimals: true,
        decimalPrecision:2,
        listeners: {
            'change': function(){
               updateProperty(selectedElement);
            }
        }
    };
    
    var pageSizesParams = {
        xtype: 'fieldset',
        title: 'Page Size',
        id:'idpagesizesparamssettingfieldset',
        width:270,
        hidden:true,
        items:[
        pagewidth,pageheight  
        ]
    };
    
    var prePrintedCheck = {
        xtype: 'checkbox',
        id: 'idprePrintedCheck',
        fieldLabel: 'PrePrinted',
        pading: '2 2 2 2',
        checked: ispreprinted,
        listeners:{
            change:function(check, newVal, oldVal){
                if(newVal){
                    getEXTComponent('idpagelayoutsettingfieldset').show();
                    getEXTComponent("idPageMarginUnit").setValue("cm");
                } else{
                    getEXTComponent('idpagelayoutsettingfieldset').hide();
                }
                updateProperty(selectedElement);
            }
            
        }
    }
     var allowmultipletransactionpage = {
        xtype: 'checkbox',
        id: 'idallowmultipletransactionCheck',
        fieldLabel: 'Allow Multiple Transaction On Same Page',
        pading: '2 2 2 2',
        checked: ismultipletransaction,
        listeners: {
            'change': function ()
            {
                updateProperty(selectedElement);

            }
        }
    }
    /**
     * Store for negative value combo
     */
    var nagativeValueStore=Ext.create('Ext.data.Store', {
        fields : ['id','type'],
        data : [
        {
            id: 0,
            type: 'None'
        },
        {
            id: 1,
            type: 'Minus Symbol'
        },
        {
            id: 2,
            type: 'Brackets'
        }
        ]
    });
    // get value from page properties for negative value combo
    var negativeValueIn = 0;
    if(pagelayoutproperty[0].pagelayoutsettings){
        negativeValueIn = pagelayoutproperty[0].pagelayoutsettings.negativeValueIn;
    }
    // Combo for selecting negative value format
    var negativeValueCombo = {
        xtype: 'combo',
        fieldLabel: 'Negative Value In',
        id: 'negativeValueComboid',
        displayField: 'type',
        valueField: 'id',
        store: nagativeValueStore,
        padding: '2 2 2 2',
        labelWidth: 100,
        value: negativeValueIn,
        width: 210,
        listeners: {
            'select': function (e) {
                    updateProperty(selectedElement);
            }
        }
    }
    
    var prePrintedSettings = {
      xtype: 'fieldset',
      title: 'PrePrinted Layout Settings',
      id:'idpreprintedlayoutsettings',
      width:270,
      items:[
        prePrintedCheck
      ]
    };
    var miscellaneous = {
      xtype: 'fieldset',
      title: 'Miscellaneous Setting',
      id:'idmiscellaneoussetting',
      width:270,
      items:[
        allowmultipletransactionpage
      ]
    };
    propertyPanel = Ext.create("Ext.Panel", {
        autoHeight:true,
        autoScroll:true,
        width: 300,
        border: false,
        //layout:'column',
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [
            insertHTML, removeAllObjectsBtn, addWaterMarkButton, pageSizesParams, pageFonts, pageMargins, pageBorders, prePrintedSettings, pagelayoutsettings, miscellaneous, negativeValueCombo
        ]
    });

//    propertyPanel.add(updateBtn);

    return propertyPanel;
}
    
/*Text Property Panel*/
function createStaticTextPropertyPanel(ele) {
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");

    if (propertyPanel)
        propertyPanelRoot.remove(propertyPanel.id);

    var content = '';
    if (ele)
        content = ele.el.dom.textContent;

    var unitvalue="1"
    if(ele.unit && ele.unit=="px"){
        unitvalue="1";
    }else{
        unitvalue="2";
    } 

    var textAlignmentStore = Ext.create("Ext.data.Store", {
        fields: ["id", "textalign"],
        data: [
        {
            id: "1",
            textalign: "Left"
        }, {
            id: "2",
            textalign: "Center"

        }, {
            id: "3",
            textalign: "Right"
        }
        ]
    });


    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    }
    
    var marginsettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:270,
        items:[
            topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };
    
    var colonAlignmentStore = Ext.create("Ext.data.Store", {
        fields: ["id", "colonalign"],
        data: [
        {
            id: "1",
            colonalign: "Left"
        }, {
            id: "2",
            colonalign: "Center"

        }, {
            id: "3",
            colonalign: "Right"
        }
        ]
    });

    var textLineStore = Ext.create("Ext.data.Store", {
        fields: ["id", "textline"],
        data: [
        {
            id: "0",
            textline: "None"
        },
        {
            id: "1",
            textline: "Overline"
        }, {
            id: "2",
            textline: "Line-through"

        }, {
            id: "3",
            textline: "Underline"
        }
        ]
    });

    var elementId = {
        xtype: 'textfield',
        fieldLabel: 'Element ID',
        id: 'idElementId',
        padding:'5 5 5 5',
        hidden:true,
        //disabled:true,
        readOnly: true
    };

    var selectedLabelTitle = {
        id: 'selectedValue',
        xtype: 'label',
        fieldLabel: 'Selected',
        text: 'Selected Text:'
    };
    
    var selectedLabel = {
        xtype: 'textarea',
//        fieldLabel: 'Selected',
        id: 'idSelectedLabel',
        padding:'5 5 5 5',
        value: content,
        width:210,
        listeners: {
            change: function(field) {
                updateProperty(selectedElement);
                var record = treePanel.getStore().getNodeById(selectedElement);
                if(Ext.getElementById(selectedElement).children[0] && Ext.getElementById(selectedElement).children[0].nodeName=="UL"){
                    tmpText = Ext.getElementById(selectedElement).children[0].children[0].innerHTML;
                    
                }else{
                    tmpText = Ext.util.Format.ellipsis(Ext.getCmp(selectedElement).label, 15);
                }
                if(tmpText.indexOf("<br>")!= -1){
                    tmpText = tmpText.substr(0, tmpText.indexOf("<br>"));
                }
                record.data.text=Ext.util.Format.ellipsis(tmpText,15);
                treePanel.getView().refresh();

            //                initTreePanel();
            }    
        }
    };
    var height = {
        xtype: 'numberfield',
        fieldLabel: 'Height',
        id: 'idHeight',
        value:  (ele != null || ele != undefined) ? ele.elementheight : 0,
        width: 210,
        minLength: 1,
        padding:'2 2 2 2',
        maxLength: 3,
        hidden:true,
        listeners: {
            'change': function(){
                if(Ext.getCmp('idUnit').getValue()=="2" && Ext.getCmp('idHeight').getValue()>100)//percentage check 
                {
                    WtfComMsgBox(["Config Error", "Height cannot be greater than 100."], 0);  
                    Ext.getCmp('idHeight').setValue(100);
                }
                updateProperty(selectedElement);
            }
        }
    };
    var width = {
        xtype: 'numberfield',
        fieldLabel: 'Width',
        id: 'idWidth',
        padding:'2 2 2 2',
        value: (ele != null || ele != undefined) ? ele.width: 0,
        width: 210,
        maxValue:100,
        minLength: 1,
        //            maxLength: 3,
        listeners: {
            'change': function (e) {
                if ( Ext.getCmp("idUnit") && Ext.getCmp("idUnit").getValue() == "2") {
                    if ( e.getValue() > 100 ) {
                        e.setValue(100);
                        WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                    }
                }
                updateProperty(selectedElement);
            }
        }
    };
    var unit = {
        xtype: 'combo',
        fieldLabel: '  Unit',
        id: 'idUnit',
        padding:'2 2 2 2',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "unit"],
            data: [
            {
                id: "1",
                unit: "px"
            }, {
                id: "2",
                unit: "%"

            }
            ]
        }),
        value: unitvalue,
        displayField: 'unit',
        valueField: 'id',
        width: 210,
        //            labelWidth: 30,
        listeners: {
            'select': function (e,newval, oldval) {
                if ( e.getValue() == "2" ) {
                    if (Ext.getCmp("idWidth")) {
                        if ( Ext.getCmp("idWidth").getValue() > 100 ) {
                            Ext.getCmp("idWidth").setValue(100);
                        }
                        Ext.getCmp("idWidth").setMaxValue(100);
                    }
                } else if ( e.getValue() == "1" ) {
                    if (Ext.getCmp("idWidth")) {
                        Ext.getCmp("idWidth").setMaxValue(1000);
                    }
                } 
                updateProperty(selectedElement);
            }
        }
    };
    var alignvalue = "";
    var customvalue = (ele != null || ele != undefined) ? ele.textalignclass : "Left";
    if (customvalue == 'classTextAligment_center') {
        alignvalue = 'Center';
    } else if (customvalue == 'classTextAligment_right') {
        alignvalue = 'Right';
    } else if (customvalue == 'classTextAligment_left') {
        alignvalue = 'Left';
    }
    var textalign = {
        xtype: 'combo',
        fieldLabel: 'Text Alignment',
        id: 'idTextAlign',
        store: textAlignmentStore,
        value: alignvalue,
        padding:'2 2 2 2',
        displayField: 'textalign',
        valueField: 'id',
        width: 210,
        listeners: {
            'select': function (e) {
                if (e.getRawValue() == "Left") {
                    var data = [["1", "Left"], ["2", "Center"], ["3", "Right"]];
                    colonAlignmentStore.loadData(data, false);

                }
                else if (e.getRawValue() == "Center") {
                    var data = [["1", "Left"], ["3", "Right"]];
                    colonAlignmentStore.loadData(data, false);

                }
                else if (e.getRawValue() == "Right") {
                    var data = [["1", "Left"]];
                    colonAlignmentStore.loadData(data, false);
                }
                updateProperty(selectedElement);
            }
        }
    };
    var usecolon = {
        xtype: 'checkbox',
        id: 'idUseColon',
        padding:'2 2 2 2',
        fieldLabel: 'Use Colon',
        hidden: true,
        listeners: {
            change: function (e) {
                var flg = this.getValue();
                if (flg == true) {
                    Ext.getCmp("idColonAlign").setDisabled(false);
                } else {
                    Ext.getCmp("idColonAlign").setDisabled(true);
                }
                updateProperty(selectedElement);
            }

        }
    };
    var colonalign = {
        xtype: 'combo',
        fieldLabel: 'Colon Alignment',
        id: 'idColonAlign',
        store: colonAlignmentStore,
        value: 'Left',
        padding:'2 2 2 2',
        displayField: 'colonalign',
        valueField: 'id',
        width: 170,
        hidden: true
    };
    
    
    var applyBullets = {    //  apply bullets checkbox
        xtype: 'checkbox',
        id: 'idApplyBullets',
        padding:'2 2 2 2',
        fieldLabel: 'Apply Bullets',
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }

        }
    };
    var bulletType = {   //  bullet type combo, to select bullet type
        xtype: 'combo',
        fieldLabel: 'Bullet Type',
        id: 'idbulletType',
        store: bulletStore,
        padding:'2 2 2 2',
        displayField: 'value',
        valueField: 'id',
        width: 210,
        listeners : {
            'select' : function(e) {
                updateProperty(selectedElement); 
            }
        }
    };
    
    var textColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color",
        width: 105
           
    });
    
    var colorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: (ele != null || ele != undefined) ? ele.textcolor : '#000000',
        id: 'colorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', newVal);
                updateProperty(selectedElement);
            }

        }
    });

//    var textColorBtn = Ext.create('Ext.Button', {
//        id: 'idTextColorBtn',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            //                            xtype: 'colormenu',
//            value: (ele != null || ele != undefined) ? ele.textcolor : '#000000',
//            id: 'colorpicker',
//            handler: function (obj, rgb) {
//
//                //this.selectedcolor='FFFFFF';
//                Ext.colorpicker = Ext.getCmp('colorpicker');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', selectednewcolor);
//                updateProperty(selectedElement);
//
//            }// handler
//        }), // menu
//        text: ''
//    });
    var selectedTextColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedTextPanel',
        bodyStyle :"margin-top:1px;background-color:"+((ele != null || ele != undefined) ? ele.textcolordata : '#000000'),
        height: 20,
        width: 25,        
        border: false
    });

    var textColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        padding:'2 2 2 2',
        border: false,
        items: [textColorLabel,selectedTextColorPanel,colorPicker]
    });

    var boldText = {
        xtype: 'checkbox',
        id: 'idBoldText',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        checked: (ele.bold == 'true') ? true : false,
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    var italicText = {
        xtype: 'checkbox',
        id: 'idItalicText',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        checked: (ele.italic == 'true') ? true : false,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                //                                  if (newVal == '1' && oldVal == '0') {
                //                                      var allCheckBoxes = checkbox.up('checkboxgroup').items.items;
                //                                      for (var i = 0; i < allCheckBoxes.length; i++) {
                //                                          allCheckBoxes[i].setValue('1');
                //                                      }
                //                                  }
                updateProperty(selectedElement);

            }
        }
    };

    var textLine = {
        xtype: 'combo',
        fieldLabel: 'Text Line',
        id: 'idTextLine',
        store: textLineStore,
        value: (ele != null || ele != undefined) ? ele.textline : 'None',
        displayField: 'textline',
        valueField: 'id',
        padding:'2 2 2 2',
        listWidth: 200,
        width: 210,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }

    };
        var pagefontstore= Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
        {
            "val":"", 
            "name":"None"
        },
        {
            "val":"sans-serif", 
            "name":"Sans-Serif"
        },

        {
            "val":"arial", 
            "name":"Arial"
        },

        {
            "val":"verdana", 
            "name":"Verdana"
        },

        {
            "val":"times new roman", 
            "name":"Times New Roman"
        },

        {
            "val":"tahoma", 
            "name":"Tahoma"
        },

        {
            "val":"calibri", 
            "name":"Calibri"
        },
        {
            "val":"courier new", 
            "name":"Courier New"
        },
        {
            "val":"MICR Encoding", 
            "name":"MICR"
        }
        
        ]
    });
    
    
    var pagefontfields = {
        xtype: 'combo',
        fieldLabel: 'Font',
        id:'pagefontid',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        padding:'2 2 2 2',
        labelWidth:100,
        width:210,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    var fontSize = {
        xtype: 'numberfield',
        fieldLabel: 'Font Size',
        id: 'idFontSize',
        value: (ele != null || ele != undefined) ? ele.fontsize : '', //parseInt to remove the px e.g 250px to 250
        width: 210,
        padding:'2 2 2 2',
        minLength: 1,
        minValue:0,
        maxLength: 3,
        listeners: {
            'change': function () {
                updateProperty(selectedElement);
            }
        }
    };
        
    var fieldAlignment = {
        xtype: 'combo',
        fieldLabel: 'Field Alignment',
        id: 'fields_Alignment',
        padding:'2 2 2 2',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "type"],
            data: [
            {
                id: "1",
                type: "Block"
            }, {
                id: "2",
                type: "Inline"

            }
            ]
        }),
        value:ele.fieldAlignment?ele.fieldAlignment:'1',
        displayField: 'type',
        valueField: 'id',
        width: 210,
        labelWidth: 100,
        listeners: {
            select: function (field) {
                updateProperty(selectedElement);
            }
        }
    };


    var updateBtn = {
        xtype: 'button',
        text: 'Update  ',
        id: 'idUpdateBtn',
        width: 55,
        style: {
            'float': 'right',
            'margin-right': '25px'

        },
        listeners: {
            'click': function ()
            {
                updateProperty(selectedElement);

            }
        }
    };

    var resetBtn = {//Reset Button
        xtype: 'button',
        text: 'Reset All ',
        id: 'idresetBtn',
        width: 55,
        //            hidden: true,
        style: {
            'float': 'right',
            'margin-right': '25px'

        },
        listeners: {
            'click': function ()
            {
                resetProperty(selectedElement);
            }
        }
    };
        
    var borderColor = {
        xtype: 'checkbox',
        id: 'idbordercolor',
        padding:'5 5 5 5',
        fieldLabel: 'Allow Border',
        checked:false,
        listeners: {
    //                change: function (checkbox, newVal, oldVal) {
    //
    //                }
    }
    };
    var valuesettings = {
        xtype: 'fieldset',
        width:270,
        title:'Value Settings',
        id:'idvaluesettingsfieldset',
        items: [
            elementId,selectedLabelTitle,selectedLabel,usecolon,colonalign
        ]
    };
    
    var fontsettings = {
        xtype: 'fieldset',
        width:270,
        title:'Font Settings',
        id:'idfontsettingsfieldset',
        items: [
            boldText,italicText,applyBullets,bulletType,fontSize,pagefontfields,textLine,textColorPanel
        ]
    };
    var spacesettings = {
        xtype: 'fieldset',
        width:270,
        title:'Alignment Settings',
        id:'idspacesettingsfieldset',
        items: [
            unit,width,height,textalign,fieldAlignment
        ]
    };
    var bordersettings = {
        xtype: 'fieldset',
        width:270,
        title:'Border Settings',
        id:'idbordersettingsfieldset',
        hidden:true,
        items: [
            borderColor
        ]
    };
    propertyPanel = Ext.create("Ext.Panel", {
//        height: 500,
        width: 300,
        border: false,
        //layout:'column',
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [
            valuesettings,fontsettings,spacesettings,bordersettings,marginsettings
        ]
    });
//    propertyPanel.add(updateBtn);
//    propertyPanel.add(resetBtn);
        
    return propertyPanel;
        
}
    
/*Creates property panel for global table*/
function createGlobalTablePropertyPanel(ele) {
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");

    if (propertyPanel){
        propertyPanelRoot.remove(propertyPanel.id);
    }
    
    var isPrePrinted = false;
    var pageSettings = pagelayoutproperty[0].pagelayoutsettings;
    if(pageSettings){
        isPrePrinted = pageSettings.ispreprinted ? pageSettings.ispreprinted : false;
    }
    
        
    var width = {
        xtype: 'numberfield',
        fieldLabel: 'Width (%)',
        labelWidth: 100,
        id: 'globalTableWidthid',
        value: 0,
        width: 210,
        minValue: 0,
        maxValue:100,
        minLength: 1,
        maxLength: 3,
        padding: '5 5 5 5',
        listeners: {
            'change': function (field)
            {   
                if ( field.getValue() > 100 ) {
                    field.setValue(100);
                    WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                }
                updateProperty(selectedElement);
            }
        }
    };
    
    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    }
    
    var marginsettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:270,
        items:[
            topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };
    
    var summaryTableCheck = {
        xtype: 'checkbox',
        id: 'idsummarytablecheck',
        fieldLabel: 'Make As Summary Table',
        labelWidth:105,
        padding:'2 2 2 2',
        checked: (ele.isSummaryTable) ? true : false,
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var summaryTableHeightComboStore = new Ext.data.Store({
        fields : ['id','type'],
        data : [{
            id: "leaveOnPage",
            type: "Leave on page"

        }, {
            id: "mergeToLine",
            type: "Merge to Line Table"
        }]
    });

    var summaryTableHeightCombo = {
        xtype: 'combo',
        id: 'idsummaryTableHeightCombo',
        fieldLabel: 'Table Height',
        labelWidth: 100,
        width: 210,
        padding: '2 2 2 2',
        hidden: true,
        disabled: true,
        store: summaryTableHeightComboStore,
        displayField: 'type',
        valueField: 'id',
        value: (ele.summaryTableHeight != undefined && ele.summaryTableHeight != "") ? ele.summaryTableHeight : 'leaveOnPage',
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }
        }
    }
    
    var summaryTableSettings = {
        xtype: 'fieldset',
        title: 'Summary Table Settings',
        id: 'idsummarytablesettings',
        hidden: !isPrePrinted,
        width:270,
        items:[
            summaryTableCheck, summaryTableHeightCombo
        ]
    };
    
    var unit = {
        xtype: 'combo',
        fieldLabel: '  Unit',
        id: 'globalTableUnitid',
        labelWidth: 100,
        hidden:true,
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "unit"],
            data: [
            {
                id: "1",
                unit: "px"
            }, {
                id: "2",
                unit: "%"

            }
            ]
        }),
        value: 'px',
        displayField: 'unit',
        valueField: 'id',
        width: 210,
        labelWidth: 100,
        padding: '5 5 5 5'

    };
    var alignStore;
    alignStore=new Ext.data.Store({
        fields : ['id','align'],
        data : [
        {
            id: 0, 
            align: 'Left'
        },

        {
            id: 1,    
            align: 'Center'
        },

        {
            id: 2, 
            align: 'Right'
        }

        ]
    });
    var alignCombo = new Ext.form.field.ComboBox({
        fieldLabel: 'Align Table',
        store: alignStore,
        id: 'allignTableCombo',
        displayField: 'align',
        valueField: 'id',
        labelWidth:100,
        queryMode: 'local',
        width: 210,
        emptyText: 'Select alignment',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        padding: '5 5 5 5',
        value :1,
        listeners: {
            'change': function ()
            {
                updateProperty(selectedElement);
            }
        }
    });
    
    var fieldAlignment = {
        xtype: 'combo',
        fieldLabel: 'Field Alignment',
        id: 'idfieldsAlignment',
        padding:'5 5 5 5',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "type"],
            data: [
            {
                id: "1",
                type: "Block"
            }, {
                id: "2",
                type: "Inline"

            }
            ]
        }),
        value:ele.fieldAlignment?ele.fieldAlignment:'1',
        displayField: 'type',
        valueField: 'id',
        width: 210,
        labelWidth: 100,
        listeners: {
            select: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var borderColorLabel = Ext.create("Ext.form.Label", {
        text: "Border Color:",
        width: 105,
        labelWidth:100

    });
//    var borderColorBtn = Ext.create('Ext.Button', {
//        id: 'newmenubtn',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            value: ele.borderColor ? rgbToHex(ele.borderColor,false) : '000000',
//            id: 'colorpicker',
//            handler: function (obj, rgb) {
//                Ext.colorpicker = Ext.getCmp('colorpicker');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', selectednewcolor);
//                updateProperty(selectedElement);
//            } // handler
//        }), // menu
//        text: ''
//    });
    var bordercolorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: ele.borderColor ? rgbToHex(ele.borderColor,false) : '#000000',
        id: 'colorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', newVal);
                updateProperty(selectedElement);
            }

        }
    });
    var selectedBorderColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedBorderPanel',
        height: 20,
        width: 25,
        border: false,
        bodyStyle: 'margin-top:1px'

    });

    var borderColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 220,
        border: false,
        padding: '5 5 5 5',
        items: [borderColorLabel, selectedBorderColorPanel, bordercolorPicker]
    });
    var backgroundColorLabel = Ext.create("Ext.form.Label", {
        text: "Background Color:",
        width: 105,
        labelWidth:100

    });
//    var backgroundColorBtn = Ext.create('Ext.Button', {
//        id: 'idbackgroundcolor',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            value: ele.backgroundColor ? rgbToHex(ele.backgroundColor,false) : 'FFFFFF',
//            id: 'idbackgroundcolorpicker',
//            handler: function (obj, rgb) {
//                Ext.colorpicker = Ext.getCmp('idbackgroundcolorpicker');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedbackgroundPanel').body.setStyle('background-color', selectednewcolor);
//                updateProperty(selectedElement);
//            } // handler
//        }), // menu
//        text: ''
//    });
    var backgroundcolorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: ele.backgroundColor ? rgbToHex(ele.backgroundColor,false) : '#FFFFFF',
        id: 'idbackgroundcolorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedbackgroundPanel').body.setStyle('background-color', newVal);
                updateProperty(selectedElement);
            }

        }
    });
    var selectedbackgroundColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedbackgroundPanel',
        height: 20,
        width: 25,
        border: false,
        bodyStyle: 'margin-top:2px'

    });

    var backgroundColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 220,
        border: false,
        padding: '5 5 5 5',
        items: [backgroundColorLabel, selectedbackgroundColorPanel, backgroundcolorPicker]
    });
    var headerbackgroundColorLabel = Ext.create("Ext.form.Label", {
        text: "Header Background Color:",
        width: 105,
        labelWidth:100

    });
//    var headerbackgroundColorBtn = Ext.create('Ext.Button', {
//        id: 'idheaderbackgroundcolor',
//        margin: '5 0 0 0',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            value: ele.headerColor ? rgbToHex(ele.headerColor,false) : 'FFFFFF',
//            id: 'idheaderbackgroundcolorpicker',
//            handler: function (obj, rgb) {
//                Ext.colorpicker = Ext.getCmp('idheaderbackgroundcolorpicker');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedheaderbackgroundPanel').body.setStyle('background-color', selectednewcolor);
//                updateProperty(selectedElement);
//            } // handler
//        }), // menu
//        text: ''
//    });
        var headerbackgroundcolorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: ele.headerColor ? rgbToHex(ele.headerColor,false) : '#FFFFFF',
        id: 'idheaderbackgroundcolorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedheaderbackgroundPanel').body.setStyle('background-color', newVal);
                updateProperty(selectedElement);
            }

        },
        margin: '5 0 0 5'
    });
    var selectedheaderbackgroundColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedheaderbackgroundPanel',
        height: 20,
        width: 25,
        margin: '5 0 0 0',
        border: false,
        bodyStyle: 'margin-top:1px'

    });

    var headerbackgroundColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 220,
        border: false,
        padding: '5 5 5 5',
        items: [headerbackgroundColorLabel, selectedheaderbackgroundColorPanel, headerbackgroundcolorPicker]
    });
//    var updateBtn = {
//        xtype: 'button',
//        text: 'Update',
//        id: 'idUpdateBtn',
//        padding: '5 5 5 5',
//        width: 55,
//        style: {
//            'float': 'right',
//            'margin-right': '25px'
//        },
//        listeners: {
//            'click': function ()
//            {
//                updateProperty(selectedElement);
//            }
//        }
//    };
    var borderStyle={
        xtype: 'fieldset',
        columns: 2,
        title: 'BorderStyle',
        anchor: '100%',
        height: 100,
        hidden:true,
        id : 'borderStyleFieldset',
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
        items: [
        {
            xtype: 'button',
            enableToggle: true,
            scale: 'large',
            scope: this,
            rowspan: 3,
            height: 63,
            width: 74,
            //                    id:'border1Img',
            itemId: 'border1',
            iconCls: 'border1Img ',
            cls: 'table,td',
            enableToggle: true,
            autoHeight: true,
            margin: '10 20 30 10',
            toggleGroup: 'ratings',
            pressed: true
        },
        {
            xtype: 'button',
            enableToggle: true,
            scale: 'large',
            rowspan: 3,
            height: 63,
            width: 74,
            itemId: 'border3',
            //                    id:'border3Img',
            iconCls: 'border2Img',
            scope: this,
            margin: '10 20 30 10',
            enableToggle: true,
            autoHeight: true,
            toggleGroup: 'ratings'
        }
        ]

        };
        var bordercornerstyle = {
            xtype: 'combo',
            fieldLabel: 'Border Corner',
            id: 'idbordercornerstyle',
            padding:'5 5 5 5',
            store: Ext.create("Ext.data.Store", {
                fields: ["id", "type"],
                data: [
                {
                    id: "1",
                    type: "Normal"
                }, {
                    id: "2",
                    type: "Round"

                }
                ]
            }),
            value: (ele.borderedgetype!=null||ele.borderedgetype!=undefined)?ele.borderedgetype:'1',
            displayField: 'type',
            valueField: 'id',
//            disabled: !borderColor.checked,
            hidden:(ele.getEl().dom.children[0].frame && ele.getEl().dom.children[0].frame=="box")?false:true,
            width: 210,
            labelWidth: 100,
            listeners: {
                select: function (field) {
                     updateProperty(selectedElement);
                }
            }
        };
        var rowspacing = {
            xtype: 'numberfield',
            fieldLabel: 'Row Spacing',
            id: 'idrowspacing',
            labelWidth: 100,
            padding:'5 5 5 5',
            value: ele.rowspacing!=null?ele.rowspacing:5,
            width: 210,
            minValue:0,
            minLength: 1,
            maxLength: 3,
            listeners: {
                change: function (obj,newVal,oldVal) {
                     updateProperty(selectedElement);
                }
            }
            
        };
        var columnspacing = {
            xtype: 'numberfield',
            fieldLabel: 'Column Spacing',
            id: 'idcolumnspacing',
            labelWidth: 100,
            padding:'5 5 5 5',
            value: ele.columnspacing!=null?ele.columnspacing:5,
            width: 210,
            minValue:0,
            minLength: 1,
            maxLength: 3,
            listeners: {
                change: function (obj,newVal,oldVal) {
                     updateProperty(selectedElement);
                }
            }
            
        };
        
    var classname=ele.getEl().dom.children[0].className;
    var rows=ele.getEl().dom.children[0].rows;
    var cells=ele.getEl().dom.children[0].rows[0].cells;
    var balanceOutstanding = {
        xtype: 'checkbox',
        id: 'idBalanceOutstanding',
        fieldLabel: 'Balance Outstanding',
        labelWidth:105,
        disabled: classname.indexOf("globaltablerepeat")>=0 && rows.length==1 && cells.length==2 ? false:true,
        hidden: (parseInt(_CustomDesign_moduleId) === Ext.moduleID.Acc_Customer_AccStatement_moduleid ||parseInt(_CustomDesign_moduleId)=== Ext.moduleID.Acc_Vendor_AccStatement_moduleid)?false:true,
        padding:'2 2 2 2',
        checked: ele.isbalanceoutstanding  ? ele.isbalanceoutstanding : false,
        listeners: {
            change: function (e,newval,oldVal) {
                if ( newval ) {
                    Ext.getCmp(selectedElement).getEl().dom.children[0].rows[0].cells[0].innerHTML= "#Outstanding Currency#";
                    Ext.getCmp(selectedElement).getEl().dom.children[0].rows[0].cells[1].innerHTML= "#Amount#";
                    Ext.getCmp("multiCurrencyId").enable();
                } else {
                    Ext.getCmp(selectedElement).getEl().dom.children[0].rows[0].cells[0].innerHTML= "&nbsp;";
                    Ext.getCmp(selectedElement).getEl().dom.children[0].rows[0].cells[1].innerHTML= "&nbsp;";
                    Ext.getCmp("multiCurrencyId").disable();
                }
                Ext.getCmp(selectedElement).isbalanceoutstanding=newval;
                Ext.getCmp(selectedElement).getEl().dom.children[0].setAttribute("isbalanceoutstanding", newval)
            }
        }
    };
    var extendedBorderBtnDisabled = false;
    if (classname.indexOf("globaltablerepeat")>=0) {
        if (!ele.isExtendLineItem && isExtendedGlobalTable) {
            extendedBorderBtnDisabled = true;
        }
    } else {
        extendedBorderBtnDisabled = true;
    }
    var extendedBorderBtn = {
        xtype: 'button',
        id: 'idExtendedBorderBtn',
//        cls:'extlineitem',
        width: 150,
        disabled: extendedBorderBtnDisabled,
        hidden: (_CustomDesign_moduleId ==Ext.moduleID.Acc_Debit_Note_ModuleId && !(_CustomDesign_templateSubtype==Ext.Subtype.PurchaseReturn)|| (_CustomDesign_moduleId==Ext.moduleID.Acc_Credit_Note_ModuleId && !(_CustomDesign_templateSubtype==Ext.Subtype.SalesReturn))|| _CustomDesign_moduleId == Ext.moduleID.Acc_Make_Payment_ModuleId || _CustomDesign_moduleId ==Ext.moduleID.Acc_Receive_Payment_ModuleId) ? false : true,
//        height:75,
        text:"Extended Global Table",
        listeners: {
            afterrender: function() {
                createToolTip('idExtendedBorderBtn', 'Extended Line Item Table', '', 'top', true);
            },
            'click': function ()
            {
                var form = new Ext.form.FormPanel({
                    frame: true,
                    labelWidth: 175,
                    autoHeight: true,
                    bodyStyle: 'padding:5px 5px 0;position:relative;background-color:#f1f1f1;',
                    autoWidth: true,
                    defaults: {
                        width: 300
                    },
                    items: [
                    {
                        xtype:'checkbox',
                        //            fieldLabel: 'Allow Pagesize & Orientation',
                        boxLabel: 'Enable Extended Border',
                        id: 'idallowpageorientation',
                        checked:ele.isExtendLineItem,
                        width: 300,
                        listeners: {
                            change: function(field, nval, oval) {
                                if(Ext.getCmp('idallowpageorientation')){
                                    if(nval==true){
                                        Ext.getCmp('idPageSizeCombo').setDisabled(false);
                                        Ext.getCmp('idPageOrientationCombo').setDisabled(false);
                                        Ext.getCmp('idAdjustPageHeightBy').setDisabled(false);
                                    } else{
                                        Ext.getCmp('idPageSizeCombo').setDisabled(true);
                                        Ext.getCmp('idPageOrientationCombo').setDisabled(true);
                                        Ext.getCmp('idAdjustPageHeightBy').setDisabled(true);
                                    }
                                }
                            }
                        }
                    },{
                        xtype: 'combo',
                        fieldLabel: 'Select Size',
                        store: pageSizeStore,
                        id: 'idPageSizeCombo',
                        displayField: 'size',
                        valueField: 'id',
                        queryMode: 'local',
                        width: 300,
                        value:ele.pageSize ? ele.pageSize : "a4",
                        emptyText: 'Select Size',
                        disabled:!ele.isExtendLineItem
                    },{
                        xtype: 'combo',
                        fieldLabel: 'Select Orientation',
                        store: pageOrientationStore,
                        id: 'idPageOrientationCombo',
                        displayField: 'orientation',
                        valueField: 'id',
                        value:ele.pageOrientation ? ele.pageOrientation : "portrait",
                        queryMode: 'local',
                        width: 300,
                        emptyText: 'Select Orientation',
                        disabled:!ele.isExtendLineItem
                    },{
                        xtype: 'textfield',
                        fieldLabel: 'Adjust Page Height By (In Pixels)',
                        id: 'idAdjustPageHeightBy',
                        value: ele.adjustPageHeight ? ele.adjustPageHeight : "0",
                        queryMode: 'local',
                        width: 300,
                        labelWidth: 190,
                        emptyText: '0',
                        disabled: !ele.isExtendLineItem
                    }]
                });
                        
                var extendedBorderWin = new Ext.Window({
                    title:'Extended Border Settings',
                    width: 400,
                    autoHeight: true,
                    
                        bodyStyle :  "background-color:#f1f1f1;",
                    plain: true,
                    modal: true,
                    items: form,
                    buttons:[
                    {
                        text : 'Save',
                        handler : function(){
                            var isExtendLineItem = false;
                            var pageSize = "a4";
                            var pageOrientation = "portrait";
                            var adjustPageHeight = "0";
                            var extendedLineItemCheckBox = Ext.getCmp("idallowpageorientation");
                            var pageSizeCombo = Ext.getCmp("idPageSizeCombo");
                            var pageOrientationCombo = Ext.getCmp("idPageOrientationCombo");
                            var adjustPageHeightBy = Ext.getCmp("idAdjustPageHeightBy");
                            if ( extendedLineItemCheckBox ) {
                                isExtendLineItem = extendedLineItemCheckBox.getValue();
                            }

                            if ( isExtendLineItem ) {
                                if ( pageSizeCombo ) {
                                    pageSize = pageSizeCombo.getValue();
                                }
                                if ( pageOrientationCombo ) {
                                    pageOrientation = pageOrientationCombo.getValue();
                                }
                                if ( adjustPageHeightBy ) {
                                    adjustPageHeight = adjustPageHeightBy.getValue();
                                }
                                ele.isExtendLineItem = isExtendLineItem;
                                ele.pageSize = pageSize;
                                ele.pageOrientation = pageOrientation;
                                ele.adjustPageHeight = adjustPageHeight;
                                isExtendedGlobalTable = isExtendLineItem;
                                pageSizeForExtGT = pageSize;
                                pageOrientationForEXTGT = pageOrientation;
                                if (pagelayoutproperty[0].pagelayoutsettings) {
                                    pagelayoutproperty[0].pagelayoutsettings.isExtendedGlobalTable = isExtendLineItem;
                                    pagelayoutproperty[0].pagelayoutsettings.adjustPageHeight = adjustPageHeight;
                                    pagelayoutproperty[0].pagelayoutsettings.pageSizeForExtGT = pageSizeForExtGT;
                                    pagelayoutproperty[0].pagelayoutsettings.pageOrientationForEXTGT = pageOrientationForEXTGT;
                                } else {
                                    pagelayoutproperty[0].pagelayoutsettings = {};
                                    pagelayoutproperty[0].pagelayoutsettings["isExtendedGlobalTable"] = isExtendLineItem;
                                    pagelayoutproperty[0].pagelayoutsettings["adjustPageHeight"] = adjustPageHeight;
                                    pagelayoutproperty[0].pagelayoutsettings["pageSizeForExtGT"] = pageSizeForExtGT;
                                    pagelayoutproperty[0].pagelayoutsettings["pageOrientationForEXTGT"] = pageOrientationForEXTGT;
                                }
                                var classes = Ext.getCmp(selectedElement).getEl().dom.children[0].attributes["class"].value;
                                Ext.getCmp(selectedElement).getEl().dom.children[0].setAttribute("class", classes + "  lineitemtablewrap");
                                
                            } else {
                                ele.isExtendLineItem = isExtendLineItem;
                                ele.pageSize = pageSize;
                                ele.pageOrientation = pageOrientation;
                                ele.adjustPageHeight = adjustPageHeight;
                                isExtendedGlobalTable = isExtendLineItem;
                                pageSizeForExtGT = pageSize;
                                pageOrientationForEXTGT = pageOrientation;
                                if (pagelayoutproperty[0].pagelayoutsettings) {
                                    pagelayoutproperty[0].pagelayoutsettings.isExtendedGlobalTable = isExtendLineItem;
                                    pagelayoutproperty[0].pagelayoutsettings.adjustPageHeight = adjustPageHeight;
                                    pagelayoutproperty[0].pagelayoutsettings.pageSizeForExtGT = pageSizeForExtGT;
                                    pagelayoutproperty[0].pagelayoutsettings.pageOrientationForEXTGT = pageOrientationForEXTGT;
                                } else {
                                    pagelayoutproperty[0].pagelayoutsettings = {};
                                    pagelayoutproperty[0].pagelayoutsettings["isExtendedGlobalTable"] = isExtendLineItem;
                                    pagelayoutproperty[0].pagelayoutsettings["adjustPageHeight"] = adjustPageHeight;
                                    pagelayoutproperty[0].pagelayoutsettings["pageSizeForExtGT"] = pageSizeForExtGT;
                                    pagelayoutproperty[0].pagelayoutsettings["pageOrientationForEXTGT"] = pageOrientationForEXTGT;
                                }
                                var classes = Ext.getCmp(selectedElement).getEl().dom.children[0].attributes["class"].value;
                                classes = classes.replace("lineitemtablewrap","");
                                Ext.getCmp(selectedElement).getEl().dom.children[0].setAttribute("class", classes);
                            }
                            extendedBorderWin.close();
                        }
                    },
                    {
                        text : 'Cancel',
                        handler : function(){
                            extendedBorderWin.close();
                        }
                    }
                    ]
                });
                extendedBorderWin.show();
            }
        }
    };
    var multiCurrency =new Ext.form.field.Checkbox({
        fieldLabel: 'Multiple Currency',
        id: 'multiCurrencyId',
        checked: ele.isOutstandingMultipleCurrency ? ele.isOutstandingMultipleCurrency : false, 
        disabled:!ele.isbalanceoutstanding,
        width: 210,
        hidden: (parseInt(_CustomDesign_moduleId) === Ext.moduleID.Acc_Customer_AccStatement_moduleid ||parseInt(_CustomDesign_moduleId)=== Ext.moduleID.Acc_Vendor_AccStatement_moduleid)?false:true,
        padding:'2 2 2 2',
        listeners: {
            'change': function (e) {
                var selected = getEXTComponent(selectedElement);
                selected.isOutstandingMultipleCurrency = e.getValue();
            }
        }
    });
    var spacesettingfieldset ={
        xtype: 'fieldset',
            autoHeight:true,
            width:270,
            id:'idspacesettingfieldset',
            title:'Alignment Settings',
            items:[
                width,alignCombo,rowspacing,columnspacing,fieldAlignment
            ]
        };
        var colorsettingfieldset ={
            xtype: 'fieldset',
            height:140,
            width:270,
            id:'idcolorsettingfieldset',
            title:'Color Settings',
            items:[
                borderColorPanel,backgroundColorPanel,headerbackgroundColorPanel
            ]
        };
        var bordersettingfieldset ={
            xtype: 'fieldset',
            height:100,
            width:270,
            id:'idbordersettingfieldset',
            title:'Border Settings',
            hidden:true/*(ele.getEl().dom.children[0].frame && ele.getEl().dom.children[0].frame=="box")?false:true*/,
            items:[
                bordercornerstyle
            ]
        };
        
        var jedetails = {
        xtype: 'checkbox',
        id: 'idjedetails',
        fieldLabel: 'JE Details Table',
        labelWidth:105,
        disabled: parseInt(_CustomDesign_moduleId) === Ext.moduleID.Acc_Make_Payment_ModuleId ? (checkSubStr(classname.replace(/\s/g,","), "globaltablerepeat") ? false:true) : true,
        padding:'2 2 2 2',
        checked: ele.isjedetailstable  ? ele.isjedetailstable : false,
        listeners: {
            change: function (e,newval,oldVal) {
                if(newval){
                    getEXTComponent(selectedElement).el.dom.children[0].classList.add("jetable");
                    getEXTComponent(selectedElement).isjedetailstable = true;
                } else{
                    getEXTComponent(selectedElement).el.dom.children[0].classList.remove("jetable");
                    getEXTComponent(selectedElement).isjedetailstable = false;
                }
            }
        }
    };
        
        var miscellaneoussetting ={
            xtype: 'fieldset',
            autoHeight:true,
            width:270,
            id:'idmiscellaneoussettingfieldset',
            title:'Miscellaneous Settings',
            items:[
                jedetails,balanceOutstanding,multiCurrency
            ]
            
        };
        
        propertyPanel = Ext.create("Ext.Panel", {
            autoHeight:true,
            width: 300,
            layout: 'column',
            border: false,
            id: 'idPropertyPanel',
            padding: '5 5 5 5',
            items: [
                {
                    xtype: 'panel',
                    width: 300,
                    layout: 'column',
                    border: false,
                    items: [
                        extendedBorderBtn,
                        spacesettingfieldset,colorsettingfieldset,bordersettingfieldset,marginsettings,miscellaneoussetting,summaryTableSettings
                    ]
                },
            ]
    });
//    propertyPanel.add(updateBtn);

    return propertyPanel;
}
    
/*Select Field Property Panel*/
function createSelectFieldPropertyPanel(ele) {
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");

    if (propertyPanel)
        propertyPanelRoot.remove(propertyPanel.id);

    var content = '';
    if (ele)
        content = ele.el.dom.textContent;
        
    var assignvalue="1"
    if(ele.unit && ele.unit=="px"){
        assignvalue="1";
    }else{
        assignvalue="2";
    }    
    
     var boldText = {
        xtype: 'checkbox',
        id: 'idBoldText',
        fieldLabel: 'Bold',
        labelWidth:105,
        padding:'2 2 2 2',
        checked: (ele.bold == 'true') ? true : false,
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    var italicText = {
        xtype: 'checkbox',
        id: 'idItalicText',
        fieldLabel: 'Italic',
        labelWidth:105,
        padding:'2 2 2 2',
        checked: (ele.italic == 'true') ? true : false,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                updateProperty(selectedElement);
            }
        }
    };
    var textColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color",
        width: 110

    });
    
    var defaultfield = {
        xtype: 'textfield',
        fieldLabel: 'Default Value',
        id: 'iddefaultfield',
        padding:'2 2 2 2',
        width:230,
        disabled:ele.isFormula?true:false,
        value: ele.defaultValue?ele.defaultValue.replace(/&nbsp;/g,' '):(ele.isFormula?"0":""),
        listeners: {
            change: function(field) {
                updateProperty(selectedElement);
            }    
        }
    };
    
    // for formula builder - to show formula
    var formula = "";
    if(ele.isFormula){
        var labelHtml = ele.labelhtml;
        labelHtml = labelHtml.split(/\{PLACEHOLDER\:|}/)[1];
        formula = labelHtml;
    }
    
    var formulaTitle = {
        id: 'selectedValue',
        xtype: 'label',
        fieldLabel: 'Selected',
        hidden: ele.isFormula?false:true,
        text: 'Formula:'
    };
    
    var formulaText = {
        xtype: 'textarea',
        id: 'idFormula',
        padding:'5 5 5 5',
        value: formula,
        readOnly: true,
        hidden: ele.isFormula?false:true,
        width:230
    };
    var colorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: (ele != null || ele != undefined) ? ele.textcolor : '#000000',
        id: 'colorpicker',
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', newVal);
                updateProperty(selectedElement);
            }
        }
    });
    
//    var textColorBtn = Ext.create('Ext.Button', {
//        id: 'idTextColorBtn',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            //                            xtype: 'colormenu',
//            value: (ele != null || ele != undefined) ? ele.textcolor : '#000000',
//            id: 'colorpicker',
//            handler: function (obj, rgb) {
//
//                //this.selectedcolor='FFFFFF';
//                Ext.colorpicker = Ext.getCmp('colorpicker');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', selectednewcolor);
//                updateProperty(selectedElement);
//
//            }// handler
//        }), // menu
//        text: ''
//    });
    var selectedTextColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedTextPanel',
        bodyStyle :"margin-top:2px; background-color:"+((ele != null || ele != undefined) ? ele.textcolordata : '#000000'),
        height: 20,
        width: 25,
        border: false
    });

    var textColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 220,
        padding: '2 2 2 2',
        border: false,
        items: [textColorLabel, selectedTextColorPanel, colorPicker]
    });

    this.borderColorLabel = Ext.create("Ext.form.Label", {
        text: "Border Color",
        width: 105
    });
                
    this.selectedBorderColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedBorderPanel',
        height: 20,
        width: 30,
        border: false
    });
//    var bordercolorPicker = Ext.create('Ext.ux.ColorPicker', {
//        luminanceImg: '../../images/luminance.png',
//        spectrumImg: '../../images/spectrum.png',
//        value: (obj != null || obj != undefined) ? obj.textcolordata : '#000000',
//      
//        id: 'selectbordercolorpicker',
//        listeners: {
//             scope:this,
//            change: function (obj, newVal) {
//                Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', newVal);
//                updateProperty(selectedElement);
//            }
//
//        }
//    });
                
    this.borderColorBtn = Ext.create('Ext.Button', {
        id: 'idBorderColorBtn',
        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
            //                        value: '#ffffff',
            id: 'selectbordercolorpicker',
            scope:this,
            handler: function (obj, rgb) {
                Ext.colorpicker = Ext.getCmp('selectbordercolorpicker');
                var selectednewcolor = "#" + rgb.toString();
                Ext.colorpicker.value = selectednewcolor;
                Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', selectednewcolor);
                updateProperty(selectedElement);
            }
        }), 
        text: ''
    });
    
    this.borderColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        padding:'2 2 2 0',
        border: false,
        items: [this.borderColorLabel,this.selectedBorderColorPanel,this.borderColorBtn]
    });
    
    var textLineStore = Ext.create("Ext.data.Store", {
        fields: ["id", "textline"],
        data: [
            {
                id: "none",
                textline: "None"
            },
            {
                id: "overline",
                textline: "Overline"
            }, {
                id: "line-through",
                textline: "Line-through"

            }, {
                id: "underline",
                textline: "Underline"
            }
        ]
    });

    var textLine = {
        xtype: 'combo',
        fieldLabel: 'Text Line',
        id: 'idTextLine',
        store: textLineStore,
        value: ele ? ele.textDecoration : 'none',
        displayField: 'textline',
        valueField: 'id',
        padding: '2 2 2 2',
        listWidth: 200,
        labelWidth: 105,
        width: 170,
        listeners: {
            'select': function(e) {
                updateProperty(selectedElement);
            }
        }

    };

    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 105,
        value: 0,
        padding:'2 2 2 2',
        width: 170,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 105,
        value: 0,
        padding:'2 2 2 2',
        width: 170,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 105,
        value: 0,
        padding:'2 2 2 2',
        width: 170,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 105,
        value: 0,
        padding:'2 2 2 2',
        width: 170,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    }
    
    var marginsettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:260,
        items:[
            topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };
    var textAlignmentStore = Ext.create("Ext.data.Store", {
        fields: ["id", "textalign"],
        data: [
        {
            id: "1",
            textalign: "Left"
        }, {
            id: "2",
            textalign: "Center"

        }, {
            id: "3",
            textalign: "Right"
        }
        ]
    });

       
    var colonAlignmentStore = Ext.create("Ext.data.Store", {
        fields: ["id", "colonalign"],
        data: [
        {
            id: "1",
            colonalign: "Left"
        }, {
            id: "2",
            colonalign: "Center"

        }, {
            id: "3",
            colonalign: "Right"
        }
        ]
    });

    var elementId = {
        xtype: 'textfield',
        fieldLabel: 'Element ID',
        id: 'idElementId',
        width:230,
        labelWidth:105,
        padding:'5 5 5 5',
        hidden:true,
        //disabled:true,
        readOnly: true
    };
    
    var selectedfield ={
        xtype: 'combo',
        fieldLabel: 'Select Field',
        store: defaultFieldGlobalStore,
        id: 'idcustomdesignerfieldselectcombo',
        displayField: 'label',
        valueField: 'id',
        queryMode: 'local',
        width:230,
        disabled: ele.isFormula,
        padding:'2 2 2 2',
        blankText: 'Select a field',
        listeners: {
            scope: this,
            'select': function (combo, selection) {
                if(selection[0].data.xtype!="NAN"){
                    updateProperty(selectedElement);
                    var record = treePanel.getStore().getNodeById(selectedElement);
                    record.data.text=Ext.getCmp(selectedElement).label;
                    treePanel.getView().refresh();
                //               initTreePanel();
                }
            }
        }
    };
    var selectedLabel = {
        xtype: 'textfield',
        fieldLabel: 'Selected',
        id: 'idSelectedLabel',
        width:230,
        labelWidth:105,
        padding:'5 5 5 5',
        //disabled:true,
        readOnly: true,
        hidden:true,
        value: content
    };
    var width = {
        xtype: 'numberfield',
        fieldLabel: 'Width',
        id: 'idWidth',
        width:170,
        labelWidth:105,
        padding:'2 2 2 2',
        value: (ele != null || ele != undefined) ? ele.width : 0,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        listeners: {
            'change': function (e) {
                if ( Ext.getCmp("idUnit") && Ext.getCmp("idUnit").getValue() == "2") {
                    if ( e.getValue() > 100 ) {
                        e.setValue(100);
                        WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                    }
                }
                updateProperty(selectedElement);
            }
        }
    };
        
    var height = {
        xtype: 'numberfield',
        fieldLabel: 'Height',
        id: 'idHeight',
        value:  (ele != null || ele != undefined) ? ele.elementheight : 0,
        width:170,
        labelWidth:105,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        hidden:true,
        listeners: {
            'change': function(){
                if(Ext.getCmp('idUnit').getValue()=="2" && Ext.getCmp('idHeight').getValue()>100)//percentage check 
                {
                    WtfComMsgBox(["Config Error", "Height cannot be greater than 100."], 0);  
                    Ext.getCmp('idHeight').setValue(100);
                }
                updateProperty(selectedElement);
            }
        }
    };
        
    var unit = {
        xtype: 'combo',
        fieldLabel: '  Unit',
        id: 'idUnit',
        width:170,
        labelWidth:105,
        padding:'2 2 2 2',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "unit"],
            data: [
            {
                id: "1",
                unit: "px"
            }, {
                id: "2",
                unit: "%"

            }
            ]
        }),
        value: assignvalue,
        displayField: 'unit',
        valueField: 'id',
        //            labelWidth: 30,
        listeners: {
            'select': function (e) {
                if ( e.getValue() == "2" ) {
                    if (Ext.getCmp("idWidth")) {
                        if ( Ext.getCmp("idWidth").getValue() > 100 ) {
                            Ext.getCmp("idWidth").setValue(100);
                            WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                        }
                        Ext.getCmp("idWidth").setMaxValue(100);
                    }
                } else if ( e.getValue() == "1" ) {
                    if (Ext.getCmp("idWidth")) {
                        Ext.getCmp("idWidth").setMaxValue(1000);
                    }
                } 
                updateProperty(selectedElement);
            }
        }
    };
    var alignvalue = "";
    var customvalue = (ele != null || ele != undefined) ? ele.textalignclass : "Left";
    if (customvalue == 'classTextAligment_center') {
        alignvalue = 'Center';
    } else if (customvalue == 'classTextAligment_right') {
        alignvalue = 'Right';
    } else if (customvalue == 'classTextAligment_left') {
        alignvalue = 'Left';
    }
    var textalign = {
        xtype: 'combo',
        fieldLabel: 'Text Alignment',
        id: 'idTextAlign',
        width:170,
        labelWidth:105,
        padding:'2 2 2 2',
        store: textAlignmentStore,
        value: alignvalue,
        displayField: 'textalign',
        valueField: 'id',
        listeners: {
            'select': function (e) {
                if (e.getRawValue() == "Left") {
                    var data = [["1", "Left"], ["2", "Center"], ["3", "Right"]];
                    colonAlignmentStore.loadData(data, false);
                }
                else if (e.getRawValue() == "Center") {
                    var data = [["1", "Left"], ["3", "Right"]];
                    colonAlignmentStore.loadData(data, false);
                }
                else if (e.getRawValue() == "Right") {
                    var data = [["1", "Left"]];
                    colonAlignmentStore.loadData(data, false);
                }
                updateProperty(selectedElement);
            }
        }
    };
    var decimalprecision = {
        xtype: 'numberfield',
        fieldLabel: 'Decimals',
        id: 'decimalprecisionid',
        value:(ele.decimalPrecision || (ele.decimalPrecision === 0)) ? ele.decimalPrecision:_amountDecimalPrecision,  // Done as decimal precision is not working for zero value.
        width:230,
        padding:'2 2 2 2',
        minValue: 0,
        maxValue: 6,
        disabled: (ele.xType && ele.xType=="2" || ele.isNumeric) ?false:true,
        listeners: {
            'change': function(){
                updateProperty(selectedElement);
            }
        }
    };
    
    var showAmountInWords = {
        xtype: 'combo',
        fieldLabel: 'Amount In Words',
        id: 'showAmountInWordsId',
        padding: '2 2 2 2',
        hidden: ele.isFormula ? false : true,
        displayField: 'name',
        valueField: 'id',
        width: 230,
        triggerAction: 'all',
        store: Ext.create('Ext.data.Store', {
            fields: ['id', 'name'],
            data: [
                {'id': 0, 'name': 'NA'},
                {'id': 1, 'name': 'Document Currency'},
                {'id': 2, 'name': 'Base Currency'}
            ]
        }),
        value: Ext.isNumber(ele.showAmountInWords) ? ele.showAmountInWords : 0,
        listeners: {
            change: function () {
                if (ele.isFormula && Ext.getCmp('showAmountInWordsId')) {
                    ele.showAmountInWords = Ext.getCmp('showAmountInWordsId').getValue();
                }
            }
        }
    };

    var colonalign = {
        xtype: 'combo',
        fieldLabel: 'Colon Alignment',
        id: 'idColonAlign',
        width:170,
        labelWidth:105,
        padding:'2 2 2 2',
        store: colonAlignmentStore,
        value: 'Left',
        displayField: 'colonalign',
        valueField: 'id',
        hidden: true
    };

 var pagefontstore= Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
        {
            "val":"", 
            "name":"None"
        },
        
        {
            "val":"sans-serif", 
            "name":"Sans-Serif"
        },

        {
            "val":"arial", 
            "name":"Arial"
        },

        {
            "val":"verdana", 
            "name":"Verdana"
        },

        {
            "val":"times new roman", 
            "name":"Times New Roman"
        },

        {
            "val":"tahoma", 
            "name":"Tahoma"
        },

        {
            "val":"calibri", 
            "name":"Calibri"
        },
        {
            "val":"courier new", 
            "name":"Courier New"
        },
        {
            "val":"MICR Encoding", 
            "name":"MICR"
        }
        ]
    });
    
    
    var pagefontfields = {
        xtype: 'combo',
        fieldLabel: 'Font',
        id:'pagefontid',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        padding:'2 2 2 2',
        labelWidth:105,
        width:170,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    var fontSize = {
        xtype: 'numberfield',
        fieldLabel: 'Font Size',
        id: 'idFontSize',
        width:170,
        labelWidth:105,
        padding:'2 2 2 2',
        value: (ele != null || ele != undefined) ? ele.fontsize : '', //parseInt to remove the px e.g 250px to 250
        minValue:0,
        minLength: 1,
        maxLength: 3,
        listeners: {
            'change': function () {
                updateProperty(selectedElement);
            }
        }
    };
        
        
    var fieldAlignment = {
        xtype: 'combo',
        fieldLabel: 'Field Alignment',
        id: 'fields_Alignment',
        width:170,
        labelWidth:105,
        padding:'2 2 2 2',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "type"],
            data: [
            {
                id: "1",
                type: "Block"
            }, {
                id: "2",
                type: "Inline"

            }
            ]
        }),
        value:ele.fieldAlignment?ele.fieldAlignment:'1',
        displayField: 'type',
        valueField: 'id',
        listeners: {
            select: function (field) {
                updateProperty(selectedElement);
            //                
            }
        }
    };

    var updateBtn = {
        xtype: 'button',
        text: 'Update',
        id: 'idUpdateBtn',
        width: 55,
        style: {
            'float': 'right',
            'margin-right': '25px'

        },
        listeners: {
            'click': function ()
            {
                updateProperty(selectedElement);
            }
        }
    };
    var resetBtn={//Reset Button
        xtype:'button',
        text:'Reset All',
        id:'idresetBtn',
        width:55,
        hidden:true,
        style:{
            'float':'right',
            'margin-right':'25px'
                
        },
        listeners:{
            'click':function()
            {
                resetProperty(selectedElement);
                    
            }
        }
    }; 
    var borderColor = {
        xtype: 'checkbox',
        id: 'idbordercolor',
        width:170,
        labelWidth:105,
        padding:'2 2 2 2',
        fieldLabel: 'Allow Border',
        checked:false,
        listeners: {
    //                change: function (checkbox, newVal, oldVal) {
    //
    //                }
    }
    };
    var preTextBold = {
        xtype: 'checkbox',
        id: 'idpreTextBoldText',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        labelWidth:105,
        checked: (ele.preTextbold) ? ele.preTextbold : false,
        hidden:!(ele.isPreText?ele.isPreText:false),
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    var pretextitalic = {
        xtype: 'checkbox',
        id: 'idPreTextItalicText',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        labelWidth:105,
        checked: (ele.preTextItalic) ? ele.preTextItalic : false,
        hidden:!(ele.isPreText?ele.isPreText:false),
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                updateProperty(selectedElement);
            }
        }
    };
    var preTextValueLabel = {        // creating label for pretext area
        id: 'idPreTextFieldLabel',
        xtype: 'label',
        text: 'Pre-Text Value:',
        hidden:!(ele.isPreText?ele.isPreText:false)
    };
    var preTextValue = {
        xtype: 'textarea',
        id: 'idpreTextFieldValue',
        padding:'2 2 2 2',
        width:210,
        value:ele.preTextValue?((ele.preTextValue).replace(/&nbsp;/g,' ')).replace(/<br>/g,'\n'):"", //replace &nbsp; to spaces and <br> tags to \n
        hidden:!(ele.isPreText?ele.isPreText:false),
        listeners: {
            'change': function (e) {
                 updateProperty(selectedElement);
            }
        }
    }
    var preTextWordSpacing = {
        xtype: 'numberfield',
        fieldLabel: 'Pre-Text Spacing',
        id: 'idpreTextwordSpacing',
        padding:'2 2 2 2',
        width:210,
        labelWidth:105,
        value:ele.preTextWordSpacing?ele.preTextWordSpacing:0,
        hidden:!(ele.isPreText?ele.isPreText:false),
        listeners: {
            'change': function (e) {
                 updateProperty(selectedElement);
            }
        }
    }
    var preText = {
        xtype: 'checkbox',
        id: 'idpreText',
        fieldLabel: 'Apply Pre-text',
        padding:'2 2 2 2',
        labelWidth:105,
        checked:ele.isPreText?ele.isPreText:false,
        listeners: {
            change: function (e) {
                if (e.getValue()) {
                    addPrePostTextInSelectField(ele,1);
                    Ext.getCmp("idPreTextFieldLabel").show();
                    Ext.getCmp("idpreTextFieldValue").show();
                    Ext.getCmp("idpreTextFieldValue").setValue("PreText");
                    Ext.getCmp("idpreTextwordSpacing").show();
                    Ext.getCmp("idpreTextwordSpacing").setValue(0);
                    Ext.getCmp("idpreTextBoldText").show();
                    Ext.getCmp("idPreTextItalicText").show();
                } else {
                    removePrePostTextInSelectField(ele,1);
                    Ext.getCmp("idPreTextFieldLabel").hide();
                    Ext.getCmp("idpreTextFieldValue").hide();
                    Ext.getCmp("idpreTextwordSpacing").hide();
                    Ext.getCmp("idpreTextBoldText").hide();
                    Ext.getCmp("idPreTextItalicText").hide();
                }
            }
        }
    };
    
    var postTextBold = {
        xtype: 'checkbox',
        id: 'idpostTextBoldText',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        labelWidth:105,
        checked: (ele.postTextbold) ? ele.postTextbold : false,
        hidden:!(ele.isPostText?ele.isPostText:false),
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    var posttextitalic = {
        xtype: 'checkbox',
        id: 'idPostTextItalicText',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        labelWidth:105,
        checked: (ele.postTextItalic) ? ele.postTextItalic : false,
        hidden:!(ele.isPostText?ele.isPostText:false),
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                updateProperty(selectedElement);
            }
        }
    };
    var postTextValueLabel = {      // creating label for posttext area
        id: 'idPostTextFieldLabel',
        xtype: 'label',
        text: 'Post-Text Value:',
        hidden:!(ele.isPostText?ele.isPostText:false)
    };
    var postTextValue = {
        xtype: 'textarea',
        id: 'idpostTextFieldValue',
        padding:'2 2 2 2',
        width:210,
        value:ele.postTextValue?((ele.postTextValue).replace(/&nbsp;/g,' ')).replace(/<br>/g,'\n'):"", // replace &nbsp; to spaces and <br> tags to \n
        hidden:!(ele.isPostText?ele.isPostText:false),
        listeners: {
            'change': function (e) {
                 updateProperty(selectedElement);
            }
        }
    }
    var postTextWordSpacing = {
        xtype: 'numberfield',
        fieldLabel: 'Post-Text Spacing',
        id: 'idpostTextwordSpacing',
        padding:'2 2 2 2',
        width:210,
        labelWidth:105,
        value:ele.postTextWordSpacing?ele.postTextWordSpacing:0,
        hidden:!(ele.isPostText?ele.isPostText:false),
        listeners: {
            'change': function (e) {
                 updateProperty(selectedElement);
            }
        }
    }
   var postText = {
        xtype: 'checkbox',
        id: 'idpostText',
        fieldLabel: 'Apply Post-text',
        padding:'2 2 2 2',
        labelWidth:105,
        checked:ele.isPostText?ele.isPostText:false,
        listeners: {
            change: function (e) {
                if (e.getValue()) {
                    addPrePostTextInSelectField(ele,2);
                    Ext.getCmp("idPostTextFieldLabel").show();
                    Ext.getCmp("idpostTextFieldValue").show();
                    Ext.getCmp("idpostTextFieldValue").setValue("PostText");
                    Ext.getCmp("idpostTextwordSpacing").show();
                    Ext.getCmp("idpostTextwordSpacing").setValue(0);
                    Ext.getCmp("idpostTextBoldText").show();
                    Ext.getCmp("idPostTextItalicText").show();
                } else {
                    removePrePostTextInSelectField(ele,2);
                    Ext.getCmp("idPostTextFieldLabel").hide();
                    Ext.getCmp("idpostTextFieldValue").hide();
                    Ext.getCmp("idpostTextFieldValue").setValue("");
                    Ext.getCmp("idpostTextwordSpacing").hide();
                    Ext.getCmp("idpostTextBoldText").hide();
                    Ext.getCmp("idPostTextItalicText").hide();
                }
            }
        }
    };
     var preTextSettings = {
        xtype:'fieldset',
        title:'Pre-text Settings',
        autoHeight:true,
        id:'idPretextSettingsfieldset',
        width:270,
        collapsible:true,
        collapsed:true,
        items:[
            preText,preTextBold,pretextitalic,preTextValueLabel,preTextValue,preTextWordSpacing
        ]
    };
    var postTextSettings = {
        xtype:'fieldset',
        title:'Post-text Settings',
        autoHeight:true,
        id:'idPosttextSettingsfieldset',
        width:270,
        collapsible:true,
        collapsed:true,
        items:[
            postText,postTextBold,posttextitalic,postTextValueLabel,postTextValue,postTextWordSpacing
        ]
    };
    var  separatorStore= Ext.create("Ext.data.Store", {
        fields: ["id", "textline"],
        data: [
        {
            id: "linebreak",
            textline: "Line Break"
        },
        {
            id: "comma",
            textline: "Comma"
        }, {
            id: "colon",
            textline: "Colon"

        }, {
            id: "semicolon",
            textline: "Semi Colon"
        },{
            id: "space",
            textline: "Space"
        }
        ]
    });
    var separatorCombo = {
        xtype: 'combo',
        fieldLabel: 'Value Separator',
        id: 'idvalSeparatorSelectField',
        store: separatorStore,
        displayField: 'textline',
        valueField: 'id',
        listWidth: 200,
        padding:'2 2 2 2',
        width: 230,
        value:ele.valueSeparator?ele.valueSeparator:"linebreak",
        listeners: {
            'select': function(e) {
                updateProperty(selectedElement);
            }
        }
    };
     var  dimensionStore= Ext.create("Ext.data.Store", {
        fields: ["id", "text"],
        data: [
        {
            id: "0",
            text: "Title"
        },
        {
            id: "1",
            text: "Description"
        },
        {
            id: "2",
            text: "Both"
        }
        ]
    });
     var dimensionsettingCombo = {
        xtype: 'combo',
        fieldLabel: 'Dimension Value',
        id: 'iddimensionSelectField',
        store: dimensionStore,
        displayField: 'text',
        valueField: 'id',
        listWidth: 200,
        padding:'2 2 2 2',
        width: 230,
        value:ele.dimensionValue?ele.dimensionValue:"2",
        disabled: ele.xType?ele.xType=="4"?false:true:true,
        listeners: {
            'select': function(e) {
                updateProperty(selectedElement);
            }
        }
    };
    
    
    this.fields[0].mapping="currencyid";
    this.fields[1].mapping="name";

    var recordCurrencyStore = new Ext.data.SimpleStore({
        fields: this.fields,
        data : this.data
    });

    var recordCurrencyCombo = new Ext.form.field.ComboBox({
        store: recordCurrencyStore,
        name:'recordcurrencytypeid',
        id: 'recordCurrencyId',
        displayField:'currency',
        valueField:'typeid',
        triggerAction: 'all',
        selectOnFocus:true,
        fieldLabel:  'Currency',
        queryMode: 'local',
        value:ele.specificreccurrency?ele.specificreccurrency:"none",
        
        //value:obj.specificreccurrency?obj.specificreccurrency:"none",
        disabled: (ele.label == 'Specific Currency Amount' || ele.label == 'Specific Currency Discount' || ele.label == 'Specific Currency Exchange Rate' || ele.label == 'Specific Currency SubTotal' || ele.label == 'Specific Currency SubTotal-Discount' || ele.label == 'Specific Currency Tax Amount' || ele.label == 'Specific Currency Unit Price' || ele.label == 'Specific Currency Term Amount') ? false : true,
        hidden: (ele.label == 'Specific Currency Amount' || ele.label == 'Specific Currency Discount' || ele.label == 'Specific Currency Exchange Rate' || ele.label == 'Specific Currency SubTotal' || ele.label == 'Specific Currency SubTotal-Discount' || ele.label == 'Specific Currency Tax Amount' || ele.label == 'Specific Currency Unit Price' || ele.label == 'Specific Currency Term Amount') ? false : true,
        listeners:{
            scope:this,
            change: function (e) {
                updateProperty(selectedElement);
            }
        }

    });
    /**
     * Checkbox for Show zero(0) value as blank or not
     */
    var showZeroValueAsBlank = {
        xtype: 'checkbox',
        id: 'showZeroValueAsBlankId',
        fieldLabel: 'Show Zero(0) Value as Blank',
        padding:'2 2 2 2',
        hidden: ele.xType != '2',
        checked: (ele.showzerovalueasblank != undefined) ? ele.showzerovalueasblank : false,
        listeners: {
            'change': function (e) {
                var showzerovalueasblank = Ext.getCmp("showZeroValueAsBlankId").getValue();
                ele.showzerovalueasblank = showzerovalueasblank;
            }
        }
    };
    
    var valuewithcomma = {
        xtype: 'checkbox',
        id: 'valuewithcommaid',
        fieldLabel: 'Value With Comma',
        padding:'2 2 2 2',
        checked: (ele.valueWithComma == true) ? true : false,
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var isNoWrapValue = {
        xtype: 'checkbox',
        id: 'isNoWrapValueId',
        fieldLabel: 'Don\'t Wrap Text' + "<span data-qtip='This property works for</br>comma separated multiple values</br>like PO Ref No., CQ Ref No.' class=\"formHelpButton\">&nbsp;&nbsp;&nbsp;&nbsp;</span>",
        padding:'2 2 2 2',
        checked: (ele.isNoWrapValue == true) ? true : false,
        listeners: {
            'change': function (e) {
                ele.isNoWrapValue = Ext.getCmp("isNoWrapValueId").getValue();
            }
        }
    };
    
    var valuesettings = {
      xtype:'fieldset',
      id:'idvaluesettingfieldset',
      title:'Value Settings',
      width:260,
      items:[
            elementId, selectedfield, selectedLabel, formulaTitle, formulaText, colonalign, decimalprecision, showAmountInWords, defaultfield, separatorCombo, valuewithcomma, isNoWrapValue, dimensionsettingCombo, recordCurrencyCombo, showZeroValueAsBlank
      ]
    };
    
    var fontsettings = {
      xtype:'fieldset',
      id:'idfontsettingfieldset',
      title:'Font Settings',
      width:260,
      items:[
          boldText, italicText , fontSize ,pagefontfields, textalign, textColorPanel,textLine
      ]
    };
    var alignsettings = {
      xtype:'fieldset',
      id:'idalignsettingfieldset',
      title:'Alignment Settings',
      width:260,
      items:[
          unit,width,height,fieldAlignment
      ]
    };
    var bordersettings = {
      xtype:'fieldset',
      id:'idbordersettingfieldset',
      title:'Border Settings',
      width:260,
      hidden:true,
      items:[
          this.borderColorPanel
      ]
    };
    var label = ele.label.toLowerCase().replace(/#/g,"");
    var sequence={
        xtype:'button',
        text:(label==Ext.FieldType.allgloballevelcustomfields || label==Ext.FieldType.alllinelevelcustomfields)?'Set Custom Field Sequence':'Set Dimensions Sequence',
        id:'iddimensionsequence',
        width:150,
        hidden:(label==Ext.FieldType.allgloballeveldimensions || label==Ext.FieldType.alldimensions || label==Ext.FieldType.alllineleveldimensions || label==Ext.FieldType.allgloballevelcustomfields || label==Ext.FieldType.alllinelevelcustomfields)?false:true,
        style:{
            'float':'left'
        },
        listeners:{
            'click':function()
            {
                var label = ele.label.toLowerCase().replace(/#/g,"");
                if(label==Ext.FieldType.allgloballeveldimensions || label==Ext.FieldType.alldimensions){
                    openDimensionAndCustomColumnWindow(false , false, false);
                } else if(label==Ext.FieldType.alllineleveldimensions){
                    openDimensionAndCustomColumnWindow(false , true, false);
                } else if(label==Ext.FieldType.allgloballevelcustomfields){
                    openDimensionAndCustomColumnWindow(true , false, true);
                } else if(label==Ext.FieldType.alllinelevelcustomfields){
                    openDimensionAndCustomColumnWindow(true , true, true);
                }
            }
        }
    };
    propertyPanel = Ext.create("Ext.Panel", {
        autoHeight:true,
        width: 300,
        border: false,
        layout:'column',
        id: 'idPropertyPanel',
        defaults: {
            margin: '7 0 0 10'
        },
        items: [
            valuesettings,fontsettings,alignsettings,bordersettings,marginsettings,preTextSettings,postTextSettings,sequence
        ],          
        buttons: [resetBtn]
    });
    return propertyPanel;
}

function createDataElementPropertyPanel(obj){
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
    if (propertyPanel){
        propertyPanelRoot.remove(propertyPanel.id);
    }
    var  fieldValue = "";
    if(obj && obj.labelval){
        fieldValue = obj.labelval;
        fieldValue = fieldValue.replace(/<span[\s\w\d=;:"'().\/,\-]*>(.|\n)*?<\/span>/gi,"");
    }
    var fieldValue = {
        xtype: 'textfield',
        fieldLabel: 'Label',
        id: 'idFieldValue',
        padding:'2 2 2 2',
        width:210,
        labelWidth:100,
        value:fieldValue,
        disabled:obj?!obj.customlabel:true,
        listeners: {
            'change': function (e) {
                 updateProperty(selectedElement);
            }
        }
    };
    var selectField = new Ext.form.field.ComboBox({
            fieldLabel: 'Select Field',
            store: defaultFieldGlobalStore,
            id: 'customdesignerfieldselectcombo',
            displayField: 'label',
            valueField: 'id',
            queryMode: 'local',
            width: 245,
            obj:obj,
            padding:'2 2 2 2',
            blankText: 'Select a field',
            value:obj?obj.selectfield:"",
            listeners: {
            scope: this,
            'select': function(combo, selection) {
                if( selection[0].data.xtype !="NAN"){
                    var htmllabel = selection[0].data.label;
                    obj.selectfield = selection[0].data.id;
                    var allowlabel=obj.allowlabel?obj.allowlabel:false;
                    var span = "";
                    if(allowlabel){
                        if(getEXTComponent('idcustomlabel').getValue()==false){
                            getEXTComponent('idFieldValue').setValue(htmllabel);
                        }
                        span = obj.getEl().dom.children[0].children[1];
                    }else{
                        span = obj.getEl().dom.children[0].children[0];
                                
                    }
                    span.innerHTML = "#" + htmllabel + "#";
                    span.setAttribute("attribute","{PLACEHOLDER:"+ Ext.getCmp('customdesignerfieldselectcombo').getValue() +"}");
                            
                    var div = obj.getEl().dom.children[0];
                    obj.dataelementhtml= div.outerHTML;
                }
            }
        }
    });
    var defaultfield = {
        xtype: 'textfield',
        fieldLabel: 'Default Value',
        id: 'iddefaultdata',
        padding:'2 2 2 2',
        width:210,
        value: obj.defaultValue?obj.defaultValue.replace(/&nbsp;/g,' '):"",
        listeners: {
            change: function(field) {
                updateProperty(selectedElement);
            }    
        }
    };
     
    var labelwidth = {
        xtype: 'numberfield',
        fieldLabel: 'Width (%)',
        id: 'idLabelWidth',
        padding:'2 2 2 2',
        width: 210,
        maxValue:100,
        minLength: 1,
        obj:obj,
        scope:this,
        value:obj?obj.labelwidth:40,
        listeners: {
            'change': function (field) {
                    if ( field.getValue() > 100 ) {
                        field.setValue(100);
                        WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                    }
                    if ( field.getValue() > 100 ) {
                        field.setValue(100);
                        WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                    }
                    var allowlabel=obj.allowlabel?obj.allowlabel:false;;
                    if(allowlabel){
                        var labelspan = obj.getEl().dom.children[0].children[0];
                        var selectField = obj.getEl().dom.children[0].children[1];
                        var labelWidth = field.getValue();
                        var dataWidth = 100 - labelWidth;
                        labelspan.style.width= labelWidth+"%";
                        selectField.style.width= dataWidth+"%";
                        obj.labelwidth = labelWidth;
                        obj.datawidth = dataWidth;
                        if(Ext.getCmp("idDataWidth")){
                            Ext.getCmp("idDataWidth").setValue(dataWidth);
                        }
                    }
                    
                    var div = obj.getEl().dom.children[0];
                    obj.dataelementhtml= div.outerHTML;
            }
        }
    };
    var datawidth = {
        xtype: 'numberfield',
        fieldLabel: 'Width (%)',
        id: 'idDataWidth',
        padding:'2 2 2 2',
        width: 210,
        maxValue:100,
        minLength: 1,
        obj:obj,
        scope:this,
        value:obj?obj.datawidth:60,
        listeners: {
            'change': function (field) {
                if ( field.getValue() > 100 ) {
                    field.setValue(100);
                    WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                }
                var allowlabel=obj.allowlabel?obj.allowlabel:false;;
                if(allowlabel){
                    var labelspan = obj.getEl().dom.children[0].children[0];
                    var selectField = obj.getEl().dom.children[0].children[1];
                    var dataWidth = Ext.getCmp("idDataWidth").getValue();
                    var labelWidth = 100-dataWidth;
                    selectField.style.width= dataWidth+"%";
                    labelspan.style.width= labelWidth+"%";
                    obj.labelwidth = labelWidth;
                    obj.datawidth = dataWidth;
                    if(Ext.getCmp("idLabelWidth")){
                        Ext.getCmp("idLabelWidth").setValue(labelWidth);
                    }
                } else{
                    var selectField = obj.getEl().dom.children[0].children[0];
                    var dataWidth = Ext.getCmp("idDataWidth").getValue();
                    selectField.style.width= dataWidth+"%";
                    obj.labelwidth = 0;
                    obj.datawidth = dataWidth;
                    if(Ext.getCmp("idLabelWidth")){
                        Ext.getCmp("idLabelWidth").setValue(labelWidth);
                    }
                }
                var div = obj.getEl().dom.children[0];
                obj.dataelementhtml= div.outerHTML;
            }
        }
    }; 
    var componentwidth = {
        xtype: 'numberfield',
        fieldLabel: 'Width (%)',
        id: 'idComponentWidth',
        padding:'2 2 2 2',
        width: 210,
        maxValue:100,
        minLength: 1,
        scope:this,
        value:obj?obj.componentwidth:60,
        listeners: {
            'change': function (field) {
                if ( field.getValue() > 100 ) {
                    field.setValue(100);
                    WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                }
                updateProperty(selectedElement);
            }
        }
    }; 
    var boldlabel = new Ext.form.field.Checkbox({
        id: 'idBoldLabel',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        checked:obj?obj.labelbold:false,
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }
    });
    var bolddata = new Ext.form.field.Checkbox({
        id: 'idBoldData',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        checked:obj?obj.databold:false,
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }
    });
    var italiclabel = new Ext.form.field.Checkbox({
        id: 'idItalicLabel',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        checked:obj?obj.labelitalic:false,
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }
    });
    var italicdata = new Ext.form.field.Checkbox({
        id: 'idItalicData',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        checked:obj?obj.dataitalic:false,
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }
    });
    
    var labelfontsize = new Ext.form.field.Number({
        xtype: 'numberfield',
        fieldLabel: 'Font Size:',
        id:'idLabelFontSize',
        padding:'2 2 2 2',
        value:obj?obj.labelfontsize:"",
        width: 210,
        minLength: 1,
        minValue:0,
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }
    });
    var datafontsize = new Ext.form.field.Number({
        xtype: 'numberfield',
        fieldLabel: 'Font Size:',
        id:'idDataFontSize',
        padding:'2 2 2 2',
        value:obj?obj.datafontsize:"",
        width: 210,
        minLength: 1,
        minValue:0,
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }
    });
     var pagefontstore= Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
        {
            "val":"", 
            "name":"None"
        },
        
        {
            "val":"sans-serif", 
            "name":"Sans-Serif"
        },

        {
            "val":"arial", 
            "name":"Arial"
        },

        {
            "val":"verdana", 
            "name":"Verdana"
        },

        {
            "val":"times new roman", 
            "name":"Times New Roman"
        },

        {
            "val":"tahoma", 
            "name":"Tahoma"
        },

        {
            "val":"calibri", 
            "name":"Calibri"
        },
        {
            "val":"courier new", 
            "name":"Courier New"
        },
        {
            "val":"MICR Encoding", 
            "name":"MICR"
        }
        ]
    });
    
    
    var labelfontfields = {
        xtype: 'combo',
        fieldLabel: 'Font',
        id:'idLabelFontFamily',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        padding:'2 2 2 2',
        labelWidth:100,
        width:210,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var datafontfields = {
        xtype: 'combo',
        fieldLabel: 'Font',
        id:'idDataFontFamily',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        padding:'2 2 2 2',
        labelWidth:100,
        width:210,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    var labelalign = new Ext.form.field.ComboBox({
        fieldLabel: 'Alignments',
        store: Ext.alignStore,
        id: 'idLabelAlignSelectCombo',
        padding:'2 2 2 2',
        displayField: "name",
        valueField: 'position',
        queryMode: 'local',
        width:210,
        value:obj?obj.labelalign:"left",
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }
    });
    var dataalign = new Ext.form.field.ComboBox({
        fieldLabel: 'Alignments',
        store: Ext.alignStore,
        id: 'idDataAlignSelectCombo',
        padding:'2 2 2 2',
        displayField: "name",
        valueField: 'position',
        queryMode: 'local',
        width:210,
        value:obj?obj.dataalign:"left",
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }
    });
    
    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        minValue:0,
        value:obj?obj.topmargin:0,
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        minValue:0,
        value:obj?obj.bottommargin:5,
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        minValue:0,
        value:obj?obj.leftmargin:0,
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        minValue:0,
        value:obj?obj.rightmargin:0,
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    }
    var colonStore=new Ext.data.Store({
        fields : ['id','position'],
        data : [
        {
            id: -1, 
            position: 'None'
        },
        
        {
            id: 0,    
            position: 'With Text'
        },

        {
            id: 1, 
            position: 'Right Aligned'
        }
        
        ]
    });
    var colonCombo = new Ext.form.field.ComboBox({
        fieldLabel: 'Colon Position',
        store: colonStore,
        id: 'colonPositionCombo',
        padding:'2 2 2 2',
        displayField: 'position',
        valueField: 'id',
        queryMode: 'local',
        width: 210,
        emptyText: 'Select a Position',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        hidden : false,
        value :obj?obj.colontype:-1,
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }
    });
    
    var textLineStore = Ext.create("Ext.data.Store", {
        fields: ["id", "textline"],
        data: [
        {
            id: "0",
            textline: "None"
        },
        {
            id: "1",
            textline: "Overline"
        }, {
            id: "2",
            textline: "Line-through"

        }, {
            id: "3",
            textline: "Underline"
        }
        ]
    });
    var textLineLabel = {
        xtype: 'combo',
        fieldLabel: 'Text Line',
        id: 'idTextLineLabel',
        store: textLineStore,
        value: (obj != null || obj != undefined) ? obj.textlinelabel : 'None',
        displayField: 'textline',
        valueField: 'id',
        padding:'2 2 2 2',
        listWidth: 200,
        width: 210,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }

    };
    var textLineData = {
        xtype: 'combo',
        fieldLabel: 'Text Line',
        id: 'idTextLineData',
        store: textLineStore,
        value: (obj != null || obj != undefined) ? obj.textlinedata : 'None',
        displayField: 'textline',
        valueField: 'id',
        padding:'2 2 2 2',
        listWidth: 200,
        width: 210,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }

    };
    
    var textColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color",
        width: 105

    });

//    var textColorLabelBtn = Ext.create('Ext.Button', {
//        id: 'idTextColorBtnLabel',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            //                            xtype: 'colormenu',
//            value: (obj != null || obj != undefined) ? obj.textcolorlabel : '#000000',
//            id: 'colorpickerlabel',
//            handler: function (obj, rgb) {
//
//                //this.selectedcolor='FFFFFF';
//                Ext.colorpicker = Ext.getCmp('colorpickerlabel');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedTextPanelLabel').body.setStyle('background-color', selectednewcolor);
//                updateProperty(selectedElement);
//
//            }// handler
//        }), // menu
//        text: ''
//    });
    var textColorpicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: (obj != null || obj != undefined) ? obj.textcolorlabel : '#000000',
        id: 'colorpickerlabel',
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('idSelectedTextPanelLabel').body.setStyle('background-color', newVal);
                updateProperty(selectedElement);
            }
        }
    });
    var selectedTextColorLabelPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedTextPanelLabel',
        height: 20,
        bodyStyle :"margin-top:2px;background-color:"+((obj != null || obj != undefined) ? obj.textcolorlabel : '#000000'), 
        width: 25,
        border: false
    });

    var labelTextColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 220,
        padding:'2 2 2 2',
        border: false,
        items: [textColorLabel, selectedTextColorLabelPanel, textColorpicker]
    });
    
    var textColorData = Ext.create("Ext.form.Label", {
        text: "Text Color",
        width: 105

    });
    var colorpicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: (obj != null || obj != undefined) ? obj.textcolordata : '#000000',        
        id: 'colorpickerdata',
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('idSelectedTextPanelData').body.setStyle('background-color', newVal);
                updateProperty(selectedElement);
            }
        }
    });
//    var textColorDataBtn = Ext.create('Ext.Button', {
//        id: 'idTextColorBtnData',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            //                            xtype: 'colormenu',
//        value: (obj != null || obj != undefined) ? obj.textcolordata : '#000000', 
//            id: 'colorpickerdata',
//            handler: function (obj, rgb) {
//
//                //this.selectedcolor='FFFFFF';
//                Ext.colorpicker = Ext.getCmp('colorpickerdata');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedTextPanelData').body.setStyle('background-color', selectednewcolor);
//                updateProperty(selectedElement);
//
//            }// handler
//        }), // menu
//        text: ''
//    });
    var selectedTextColorDataPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedTextPanelData',
        bodyStyle :"margin-top:2px; background-color:"+((obj != null || obj != undefined) ? obj.textcolordata : '#000000'), 
        height: 20,
        width: 25,
        border: false
    });

    var dataTextColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 220,
        padding:'2 2 2 2',
        border: false,
        items: [textColorData,selectedTextColorDataPanel,colorpicker]
    });
    
     var fieldAlignment = {
        xtype: 'combo',
        fieldLabel: 'Field Alignment',
        id: 'idfields_Alignment',
        padding:'2 2 2 2',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "type"],
            data: [
            {
                id: "1",
                type: "Block"
            }, {
                id: "2",
                type: "Inline"

            }
            ]
        }),
        value: obj.fieldalignment?obj.fieldalignment:'1',
        displayField: 'type',
        valueField: 'id',
        width: 210,
        labelWidth: 100,
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var customizeLabel = {
        xtype:'checkbox',
        fieldLabel: 'Custom Label',
        id: 'idcustomlabel',
        padding:'2 2 2 2',
        name: 'customlabel',
        checked:obj?obj.customlabel:false,
        width: 300,
        listeners: {
            scope: this,
            change: function(field, nval, oval) {
                if(Ext.getCmp('idFieldValue')){
                    if(nval==true){
                        Ext.getCmp('idFieldValue').setDisabled(false);
                    } else{
                        var rec = searchRecord(defaultFieldGlobalStore, Ext.getCmp('customdesignerfieldselectcombo').getValue(), 'id');
                        if(rec && rec.data){
                            Ext.getCmp('idFieldValue').setValue(rec.data.label);
                        }
                        Ext.getCmp('idFieldValue').setDisabled(true);

                    }
                }
                obj.customlabel=nval;
            }
        }
    };
    
    var decimalprecision = {
        xtype: 'numberfield',
        fieldLabel: 'Decimals',
        id: 'decimalprecisiondataid',
        value:(obj.decimalPrecision!=undefined)? obj.decimalPrecision:_amountDecimalPrecision,
        width:210,
        labelWidth:100,
        padding:'2 2 2 2',
        minValue: 0,
        maxValue: 6,
        disabled: (obj.xType && obj.xType=="2" || obj.isNumeric) ?true:false,
        listeners: {
            'change': function(){
                updateProperty(selectedElement);
            }
        }
    };
    var  separatorStore= Ext.create("Ext.data.Store", {
        fields: ["id", "textline"],
        data: [
        {
            id: "linebreak",
            textline: "Line Break"
        },
        {
            id: "comma",
            textline: "Comma"
        }, {
            id: "colon",
            textline: "Colon"

        }, {
            id: "semicolon",
            textline: "Semi Colon"
        }, {
            id: "space",
            textline: "Space"
        }
        ]
    });
    var separatorCombo = {
        xtype: 'combo',
        fieldLabel: 'Value Separator',
        id: 'idvalSeparatorDataField',
        store: separatorStore,
        displayField: 'textline',
        valueField: 'id',
        listWidth: 200,
        padding:'2 2 2 2',
        width: 230,
        value:obj.valueSeparator?obj.valueSeparator:"linebreak",
        listeners: {
            'select': function(e) {
                updateProperty(selectedElement);
            }
        }
    };
     var  dimensionStore= Ext.create("Ext.data.Store", {
        fields: ["id", "text"],
        data: [
        {
            id: "0",
            text: "Title"
        },
        {
            id: "1",
            text: "Description"
        },
        {
            id: "2",
            text: "Both"
        }
        ]
    });
    
    this.fields[0].mapping="currencyid";
    this.fields[1].mapping="name";

    var recordCurrencyStore = new Ext.data.SimpleStore({
        fields: this.fields,
        data : this.data
    });
    var recordCurrencyCombo = new Ext.form.field.ComboBox({
        store: recordCurrencyStore,
        name:'recordcurrencytypeid',
        id: 'recordCurrencyId',
        displayField:'currency',
        valueField:'typeid',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true,
        queryMode: 'local',
        fieldLabel:  'Currency',//this is currency combo
        value:obj.specificreccurrency?obj.specificreccurrency:"none",
        disabled: (obj.selectfield == 'GlobalSpecificCurrencyExchangeRate' || obj.selectfield == 'GlobalSpecificCurrencyAmount' ||  obj.selectfield == 'GlobalSpecificCurrencySubTotal' || obj.selectfield == 'GlobalSpecificCurrencySubTotalWithDicount' || obj.selectfield == 'GlobalSpecificCurrencyTaxAmount' || obj.selectfield == 'GlobalSpecificCurrencyTermAmount') ? false : true,
        listeners:{
            scope:this,
            change: function () {
                updateProperty(selectedElement);
            }
        }

    });
    /**
     * Checkbox for Show zero(0) value as blank or not
     */
    var showZeroValueAsBlank = {
        xtype: 'checkbox',
        id: 'showZeroValueAsBlankId',
        fieldLabel: 'Show Zero(0) Value as Blank',
        padding:'2 2 2 2',
        hidden: obj.XType != '2',
        checked: (obj.showzerovalueasblank != undefined) ? obj.showzerovalueasblank : false,
        listeners: {
            'change': function (e) {
                var showzerovalueasblank = Ext.getCmp("showZeroValueAsBlankId").getValue();
                obj.showzerovalueasblank = showzerovalueasblank;
            }
        }
    };
    
     var dimensionsettingCombo = {
        xtype: 'combo',
        fieldLabel: 'Dimension Value',
        id: 'iddimensionDataField',
        store: dimensionStore,
        displayField: 'text',
        valueField: 'id',
        listWidth: 200,
        padding:'2 2 2 2',
        width: 230,
        value:obj.dimensionValue?obj.dimensionValue:"2",
        disabled: obj.XType?obj.XType=="4"?false:true:true,
        listeners: {
            'select': function(e) {
                updateProperty(selectedElement);
            }
        }
    };
    var dataComponentSettings = {
        xtype:'fieldset',
        title:'Component Settings',
        autoHeight:true,
        id:'idcomponentSettingsfieldset',
        width:270,
        items:[
            componentwidth, fieldAlignment
        ]
    };
    var valuewithcomma = {
        xtype: 'checkbox',
        id: 'valuewithcommaDataid',
        fieldLabel: 'Value With Comma',
        padding:'2 2 2 2',
        checked: (obj.valueWithComma == true) ? true : false,
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    var labelSettings = {
        xtype:'fieldset',
        title:'Label Settings',
        autoHeight:true,
        hidden:!obj.allowlabel,
        id:'idlabelSettingsfieldset',
        width:270,
        items:[
           boldlabel, italiclabel,customizeLabel,fieldValue, labelwidth,labelTextColorPanel,  labelfontsize,labelfontfields,labelalign,textLineLabel,colonCombo  
        ]
    };
    
    var isNoWrapValue = {
        xtype: 'checkbox',
        id: 'isNoWrapValueId',
        fieldLabel: 'Don\'t Wrap Text' + "<span data-qtip='This property works for</br>comma separated multiple values</br>like PO Ref No., CQ Ref No.' class=\"formHelpButton\">&nbsp;&nbsp;&nbsp;&nbsp;</span>",
        padding:'2 2 2 2',
        checked: (obj.isNoWrapValue == true) ? true : false,
        listeners: {
            'change': function (e) {
                obj.isNoWrapValue = Ext.getCmp("isNoWrapValueId").getValue();
            }
        }
    };
    
    var dataSettings = {
        xtype:'fieldset',
        title:'Data Settings',
        autoHeight:true,
        id:'idDataSettingsfieldset',
        width:270,
        items:[
            bolddata, italicdata, selectField, defaultfield, decimalprecision,valuewithcomma,isNoWrapValue,separatorCombo, datawidth,dataTextColorPanel, datafontsize,datafontfields,dataalign,textLineData,dimensionsettingCombo, recordCurrencyCombo,showZeroValueAsBlank
        ]
    };
    var marginSettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:270,
        items:[
            topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };
    
    var fieldPropertyPanel =  Ext.create("Ext.Panel", {
        width: 300,
        autoHeight:true,
        border: false,
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [
            dataComponentSettings,labelSettings,dataSettings,marginSettings
        ]
    });

    return fieldPropertyPanel;
}

function addPrePostTextInSelectField(ele, type) {
    
    var id =  ele.id+type;
    if (!document.getElementById(id)) {
        var initialspan = ele.getEl().dom.children[0];
        var span = document.createElement('span');
        span.innerHTML = type == 1 ? "PreText" : "PostText";
        span.setAttribute("id", id);
        span.setAttribute("label", (type == 1 ? "PreText" : "PostText"));
        span.setAttribute("type", type);
        if (type == 1) {
            ele.getEl().dom.insertBefore(span, initialspan);
            ele.isPreText = true;
        } else {
            ele.isPostText = true;
            ele.getEl().dom.appendChild(span);
        }
    }
}
function removePrePostTextInSelectField(ele, type) {
    var id = ele.id+type;
    if (document.getElementById(id)) {
        document.getElementById(id).remove();
    }
    if ( type == 1 ) {
        ele.isPreText = false;
    } else  {
        ele.isPostText = false;
    }
}

/*Image Property Panel*/
function createImagePropertyPanel(ele) {
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");

    if (propertyPanel)
        propertyPanelRoot.remove(propertyPanel.id);

    var content = '';
    if (ele)
        content = ele.el.dom.textContent;
        
    var assignvalue="px"
    if(ele.unit && ele.unit=="px"){
        assignvalue="px";
    }else{
        assignvalue="%";
    }    
        
    var elementId = {
        xtype: 'textfield',
        fieldLabel: 'Element ID',
        id: 'idElementId',
        hidden:true,
        //disabled:true,
        readOnly: true
    };

    
    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 170,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 170,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 170,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:ele.fieldAlign=='1',//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 170,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:ele.fieldAlign=='1',//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    }
    
    var marginsettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:260,
        items:[
            topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };


    var width = {
        xtype: 'textfield',
        fieldLabel: 'Width',
        id: 'idWidth',
        width: 210,
        minLength: 1,
        maxLength: 4,
        validator : function(value){
            var patt = /^[0-9]*$/;
            if(value.toLowerCase() == "auto"){
               return true; 
            } else if(patt.test(value)){
               return true; 
            }
            return "Only  numeric and auto value is acceptible for width";
        },
        minValue: 0,
        listeners: {
            'change': function(){
                if(Ext.getCmp('idWidth').getValue()>100 && Ext.getCmp('idUnit').getValue()=="2")//percentage check 
                {
                    WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);   
                    Ext.getCmp('idWidth').setValue(100);
                }
                updateProperty(selectedElement);
            }
        }
    };
        
    var height = {
        xtype: 'textfield',
        fieldLabel: 'Height (px)',
        id: 'idHeight',
        width: 210,
        minValue: 0,
        minLength: 1,
        maxLength: 4,
        validator : function(value){
            var patt = /^[0-9]*$/;
            if(value.toLowerCase() == "auto"){
               return true; 
            } else if(patt.test(value)){
               return true; 
            }
            return "Only  numeric and auto value is acceptible for height";
        },
        listeners: {
            'change': function(){
//                if(Ext.getCmp('idUnit').getValue()=="2" && Ext.getCmp('idHeight').getValue()>100)//percentage check 
//                {
//                    WtfComMsgBox(["Config Error", "Height cannot be greater than 100."], 0);  
//                    Ext.getCmp('idHeight').setValue(100);
//                }
                updateProperty(selectedElement);
            }
        }
    };
        
    var unit = {
        xtype: 'combo',
        fieldLabel: '  Unit',
        id: 'idUnit',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "unit"],
            data: [
            {
                id: "px",
                unit: "px"
            }, {
                id: "%",
                unit: "%"

            }
            ]
        }),
        value:assignvalue,
        displayField: 'unit',
        valueField: 'id',
        width: 210,
        //            labelWidth: 30,
        listeners: {
            'select': function (combo, record, index) {
                if((Ext.getCmp('idWidth').getValue()>100)&& combo.getValue()=='2')
                { //if combo is selected from px to % and value of width & height is greater than 100 then it converts the px to %.  
                    if (Ext.getCmp('idWidth').getValue() > 0) {
                        Ext.getCmp('idWidth').setValue(100);
                    }
//                    if (Ext.getCmp('idHeight').getValue() > 0) {
//                        Ext.getCmp('idHeight').setValue(100);
//                    }
                }
                updateProperty(selectedElement);
            }
        }
    };

    var updateBtn = {
        xtype: 'button',
        text: 'Update',
        id: 'idUpdateBtn',
        width: 55,
        hidden:true,
        style: {
            'float': 'right',
            'margin-right': '25px'

        },
        listeners: {
            'click': function ()
            {
                updateProperty(selectedElement);

            }
        }
    };
       

    var textAlignmentStore = Ext.create("Ext.data.Store", {
        fields: ["id", "textalign"],
        data: [
        {
            id: "1",
            textalign: "Left"
        }, {
            id: "2",
            textalign: "Center"

        }, {
            id: "3",
            textalign: "Right"
        }
        ]
    });

    var colonAlignmentStore = Ext.create("Ext.data.Store", {
        fields: ["id", "colonalign"],
        data: [
        {
            id: "1",
            colonalign: "Left"
        }, {
            id: "2",
            colonalign: "Center"

        }, {
            id: "3",
            colonalign: "Right"
        }
        ]
    });
        
    var alignvalue = "";
    var customvalue = (ele != null || ele != undefined) ? ele.textalignclass : "Left";
    if (customvalue == 'imageshiftcenter') {
        alignvalue = 'Center';
    } else if (customvalue == 'imageshiftright') {
        alignvalue = 'Right';
    } else if (customvalue == 'imageshiftleft') {
        alignvalue = 'Left';
    }
        
    var textalign = {
        xtype: 'combo',
        fieldLabel: 'Text Alignment',
        id: 'idTextAlign',
        store: textAlignmentStore,
        value:alignvalue!=""?alignvalue:'Left',
        displayField: 'textalign',
        valueField: 'id',
        width: 210,
        hidden:ele.fieldAlign=='2',
        listeners: {
            'select': function (e) {
                if (e.getRawValue() == "Left") {
                    var data = [["1", "Left"], ["2", "Center"], ["3", "Right"]];
                    colonAlignmentStore.loadData(data, false);

                }
                else if (e.getRawValue() == "Center") {
                    var data = [["1", "Left"], ["3", "Right"]];
                    colonAlignmentStore.loadData(data, false);

                }
                else if (e.getRawValue() == "Right") {
                    var data = [["1", "Left"]];
                    colonAlignmentStore.loadData(data, false);
                }
                updateProperty(selectedElement);
            }
        }
    };
        
    var fieldAlignment = {
        xtype: 'combo',
        fieldLabel: 'Field Alignment',
        id: 'fields_Alignment',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "type"],
            data: [
            {
                id: "1",
                type: "Block"
            }, {
                id: "2",
                type: "Inline"

            }
            ]
        }),
        value: ele.fieldAlign?ele.fieldAlign:'1',
        displayField: 'type',
        valueField: 'id',
        width: 210,
        labelWidth: 100,
        listeners: {
            change: function (field) {
                if ( field.getValue() == '1' ) {
                    Ext.getCmp('idTextAlign').show();
                    Ext.getCmp('idleftmargin').hide();
                    Ext.getCmp('idrightmargin').hide();
                } else if ( field.getValue() == '2' ) {
                    Ext.getCmp('idTextAlign').hide();
                    Ext.getCmp('idleftmargin').show();
                    Ext.getCmp('idrightmargin').show();
                }
                updateProperty(selectedElement);
            }
        }
    };
    var borderColor = {
        xtype: 'checkbox',
        id: 'idbordercolor',
        fieldLabel: 'Border Color',
        checked:false,
        listeners: {
    //                change: function (checkbox, newVal, oldVal) {
    //
    //                }
    }
    };
    var valuesetting = {
        xtype:'fieldset',
        title:'Value settings',
        id:'idvaluesettingfieldset',
        width:260,
        hidden:true,
        items:[
            elementId
        ]
    };
    var alignsetting = {
        xtype:'fieldset',
        title:'Alignment settings',
        id:'idalignsettingfieldset',
        width:260,
        items:[
            unit,width,height,fieldAlignment,textalign
        ]
    };
    var bordersetting = {
        xtype:'fieldset',
        title:'Border settings',
        id:'idbordersettingfieldset',
        width:260,
        hidden:true,
        items:[
            borderColor
        ]
    };
    propertyPanel = Ext.create("Ext.Panel", {
        autoHeight:true,
        width: 300,
        border: false,
        layout:'column',
        id: 'idPropertyPanel',
        //                padding: '5 5 5 5',
        defaults: {
            margin: '7 0 0 10'
        },
        items: [
            valuesetting,alignsetting,bordersetting,marginsettings
        ]
    });
    propertyPanel.add(updateBtn);

    return propertyPanel;

}
    
/*Row Property Panel*/
function createRowPropertyPanel() {
        
    var propertyPanelRoot = getEXTComponent(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = getEXTComponent("idPropertyPanel");
    if (propertyPanel) {
        propertyPanelRoot.remove(propertyPanel.id);
    }
    
    var isPrePrinted = pagelayoutproperty[0].pagelayoutsettings == null || pagelayoutproperty[0].pagelayoutsettings== undefined ? false : pagelayoutproperty[0].pagelayoutsettings.ispreprinted;
    
//    var elementId = {
//        xtype: 'textfield',
//        fieldLabel: 'Element ID',
//        id: 'idElementId',
//        hidden:true,
//        readOnly: true
//    };

    /******************* Copy Row Button ******************/
    var copyHtml = {
        xtype: 'button',
        id: 'idcopyHTMLBtn',
        width: 50,
        height:50,
        margin:'5 5 5 5',
        iconCls:"copyrowbtn",
        listeners: {
            'click': function()  // on clicking the button a window will open, containing the json of selected row.
            {
                var copyHTMLWin = Ext.create('Ext.window.Window', {  // window created for copy Json 
                    title: 'Data',
                    height: 500,
                    width: 400,
                    id:"idcopyHTMLWin",
                    layout: 'fit',
                    modal:true,              //ERP-19208
                    items: {
                        xtype: 'textarea',
                        border: false,
                        editable:false,
                        height: 500,
                        width: 400,
                        id:"idCopyRowTextArea",
                        autoScroll:true,
                        value: JSON.stringify(getElementJson(getEXTComponent(selectedElement),1),null,4) 
                    },
                    buttons:[
                        {
                            text:"Select All",
                            handler: function() {
                                var copyRowTextArea = getEXTComponent("idCopyRowTextArea");
                                if ( copyRowTextArea ) {
                                    copyRowTextArea.focus();// for chrome browser before selectin text focus is compulsory
                                    copyRowTextArea.selectText();
                                }
                            }
                        },
                        {
                            text:"Close",
                            handler: function() {
                                var copyRowWindow = getEXTComponent("idcopyHTMLWin");
                                if ( copyRowWindow ) {
                                    copyRowWindow.close();
                                }
                            }
                        }
                    ]
                }).show();

            },
            afterrender: function() {
                createToolTip('idcopyHTMLBtn', 'Copy Row/ Section', '', 'top', true);
            }
        }
        
    };
    /********************* Merge & Split settings ********************/
        /***************** No. of columns in row  ********************/
    var column = {   // Component for columns in  row panel
        xtype: 'numberfield',
        fieldLabel: 'Column(s)',
        id: 'idColumns',
        value: 0,
        padding:'2 2 2 2',
        width: 170,
        minValue: 1,
        maxValue: 5,
        listeners:{
            'change':function(field,newValue,oldValue){
                var ele = Ext.getCmp(selectedElement).items.items[0].items.items; // ERP-19333 : [Document Designer]-When line table is inserted and we use row propety, Columns and increase columns,line table is not rendered.
                if(ele.length > 0?ele[0].fieldType==Ext.fieldID.insertTable:false){
                    Ext.Msg.show({
                        title      : 'Row Split/Merge',
                        msg        : 'Can not Split or Merge row having Line Item Table.',
                        width      : 370,
                        buttons    : Ext.MessageBox.OK,
                        icon       : Ext.Msg.WARNING
                    });
                    return false;
                }
                if(newValue > oldValue){
                    // Splitting
                    updateProperty(selectedElement,false);
                    initTreePanel();
                }else{
                    // Merging
                    updateProperty(selectedElement,true);
                    initTreePanel();
                }
            }
        } 
    };
        /************** Joining two rows ***********************/
    var joinNextRow = {  // check box for joining two rows.
        xtype: 'checkbox',
        id: 'idjoinNextRow',
        fieldLabel: 'Join with next',
        padding:'2 2 2 2',
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                var rowEle = getEXTComponent(selectedElement);
                if ( newVal ) {
                    rowEle.addCls("joinnextrow");  // adding join next row css class
                } else {
                    rowEle.removeCls("joinnextrow"); // removing join next row css class
                }
                rowEle.isJoinNextRow = newVal;
            }
        }
    };
        /******************* Fieldset for Merge and Split Settings ***********************/
    var mergesplitsetting = {
        xtype:'fieldset',
        title:'Merge & Split settings',
        id:'idmergersplitsettingfieldset',
        width:270,
        items:[
            joinNextRow,column
        ]
    };
    
    /********************** Header And Footer Settings ***********************************/
        /****************** checkbox for setting row as header ***************************/
    var headerProperty = {
        xtype: "checkbox",
        fieldLabel: "Header",
        id: "idHeader",
        padding:'2 2 2 2',
        disabled: (headerId != null && headerId != selectedElement)||footerId == selectedElement ? true : false,  //If footer is checked for row then header should be set disabled.
        checked: (headerId == selectedElement) ? true : false,
        listeners: {
            change: function (e) {
                var flg = this.getValue();
                if (flg == true) {
                    headerId = selectedElement;
                    Ext.getCmp("idFooter").setDisabled(true);
                    Ext.getCmp("idsetAsGlobalHeaderBtn").setDisabled(false);

                    if (Ext.getCmp("idHeader").getValue() == true)
                    {
                        Ext.getCmp(selectedElement).isheader = true;
                    }
                    else
                    {
                        Ext.getCmp(selectedElement).isheader = false;
                    }
                }
                else
                {
                    headerId = null;
                    headerHtml = null;
                    headerJson = null;
                    Ext.getCmp(selectedElement).isheader = false;
                    if(footerId==null){                             //If footer is not present in template then set footer checkbox enable.
                        Ext.getCmp("idFooter").setDisabled(false);
                        Ext.getCmp("idsetAsGlobalHeaderBtn").setDisabled(true);
                    }
                }
                initTreePanel();                         //Initiate tree panel to show header node in tree view.
            }
        }
    };
        /**************** Checkbox for setting row as footer **********************/
    var footerProperty = {
        xtype: "checkbox",
        fieldLabel: "Footer",
        id: "idFooter",
        padding:'2 2 2 2',
        disabled: ((footerId != null && footerId != selectedElement))||headerId == selectedElement ? true : false,       //If header is checked for row then footer should be set disabled.
        checked: (footerId == selectedElement) ? true : false,
        listeners: {
            change: function (e) {
                var flg = this.getValue();
                if (flg == true) {
                    footerId = selectedElement;
                    Ext.getCmp("idHeader").setDisabled(true);
                    Ext.getCmp("idsetAsGlobalFooterBtn").setDisabled(false);
                    if (Ext.getCmp("idFooter").getValue() == true)
                    {
                        Ext.getCmp(selectedElement).isfooter = true;
                    }
                    else
                    {
                        Ext.getCmp(selectedElement).isfooter = false;
                    }
                }
                else
                {
                    footerId = null;
                    footerHtml = null;
                    footerJson = null;
                    Ext.getCmp(selectedElement).isfooter = false;
                    if(headerId==null){                                      //If header is not present in template then set header checkbox enable.
                        Ext.getCmp("idHeader").setDisabled(false);
                        Ext.getCmp("idsetAsGlobalFooterBtn").setDisabled(true);
                    }
                }
                initTreePanel();                                    //Initiate tree panel to show footer node in tree view.
            }
        }
    };
        /********************* Button for making that row as global header *********************/
    var setAsGlobalHeaderBtn = {
         xtype: 'button',
        text: 'Set as Global Header',
        id: 'idsetAsGlobalHeaderBtn',
        width: 115,
        padding:'2 2 2 2',
        margin:'5 2 2 2',
        disabled:Ext.getCmp("idHeader")?(Ext.getCmp("idHeader").checked?false:true):true,
        handler:function(){
            Ext.MessageBox.show({
                title: 'Set as Global Header',
                msg: 'Are you sure you want to save Global Header?',
                buttons: Ext.MessageBox.YESNO,
                icon: Ext.MessageBox.QUESTION,
                fn: function(btn) {
                    if (btn == 'yes')
                    {
                        globalHeader = selectedElement.id;
                        var headerjson = getGlobalHeaderFooterJson(Ext.getCmp("idMainPanel"), selectedElement);
                        headerjson = Ext.JSON.encode(headerjson)
                        var headerhtml = document.getElementById(selectedElement).outerHTML;

                        Ext.Ajax.request({
                            url: "DocumentDesignController/saveGlobalHeaderFooter.do",
                            method: 'POST',
                            params: {
                                json: headerjson,
                                html: headerhtml,
                                isheader: 1
                            },
                            success: function(response, req) {
                                var result = Ext.decode(response.responseText);

                            }
                        }) // End of Ajax Request
                    } else {
                        globalHeader = null;
                    }
                }
            });
        }
    };
        /************************ Button for making that row as footer ***********************/
    var setAsGlobalFooterBtn = {
         xtype: 'button',
        text: 'Set as Global Footer',
        id: 'idsetAsGlobalFooterBtn',
        width: 115,
        padding:'2 2 2 2',
        margin:'5 2 2 2',
        disabled:Ext.getCmp("idFooter")?(Ext.getCmp("idFooter").checked?false:true):true,
        handler:function(){
            Ext.MessageBox.show({
                title: 'Set as Global Footer',
                msg: 'Are you sure you want to save Global Footer?',
                buttons: Ext.MessageBox.YESNO,
                icon: Ext.MessageBox.QUESTION,
                fn: function (btn) {
                    if (btn == 'yes')
                    {
                        globalFooter = selectedElement.id;
                        var footerjson = getGlobalHeaderFooterJson(Ext.getCmp("idMainPanel"), selectedElement);
                        var footerjson = Ext.JSON.encode(footerjson)
                        var footerhtml = document.getElementById(selectedElement).outerHTML;

                        Ext.Ajax.request({
                            url: "DocumentDesignController/saveGlobalHeaderFooter.do",
                            method: 'POST',
                            params: {
                                json: footerjson,
                                html: footerhtml,
                                isheader: 0
                            },
                            success: function (response, req) {
                                var result = Ext.decode(response.responseText);

                            }
                        }) // End of Ajax Request

                    } else {
                        globalFooter = null;
                    }
                }
            });
        }
    };
    
        /************************* Column Panel for header property ******************************/
    var headerColumnPanel = {
        xtype:'panel',
        autoHeight:true,
        border: false,
        layout:'column',
        items:[
            {
                columnWidth:0.5,
                border:false,
                items:[headerProperty]
            },{
                columnWidth:0.5,
                border:false,
                items:[setAsGlobalHeaderBtn]
            }
        ]
    };
        /*********************** Column Panel for Footer Property ******************************/
    var footerColumnPanel = {
        xtype:'panel',
        autoHeight:true,
        border: false,
        layout:'column',
        items:[
            {
                columnWidth:0.5,
                border:false,
                items:[footerProperty]
            },{
                columnWidth:0.5,
                border:false,
                items:[setAsGlobalFooterBtn]
            }
        ]
    };
    
        /********************** Fieldset for header Footer settings *************************/
    var headerfootersetting = {
        xtype:'fieldset',
        title:'Header & Footer settings',
        id:'idheaderfootersettingfieldset',
        width:270,
        hidden: isPrePrinted,
        items:[
            headerColumnPanel,footerColumnPanel
        ]
    };
    
    /*********************** sequnce settings has been removed ***************************
    var moveupBtn = {
        xtype: 'button',
        text: 'Move up',
        id: 'idmoveUpBtn',
        width: 100,
        style: {
            'float': 'left',
            'margin-right': '25px',
            'margin-left':'5px',
            'margin-bottom':"5px"

        },
        listeners: {
            'click': function ()
            {
                var totalLength = Ext.getCmp("idMainPanel").items.items.length;
                var index;
                for (var i = 0; i < totalLength; i++) {
                    var item = Ext.getCmp("idMainPanel").items.items[i];
                    if (item.id === selectedElement) {
                        index = i;
                        break;
                    }
                }
                if (index == 0) {
                    WtfComMsgBox(["Warning", "Row is already at first position."], 0);
                } else {
                    var json = getElementJson(Ext.getCmp(selectedElement), 1);
                    Ext.getCmp("idMainPanel").remove(Ext.getCmp(selectedElement));
                    createExtElements(JSON.stringify(json), index - 1);
                    initTreePanel();
                }
                   
            }
        }
    };
    var movedownBtn = {
        xtype: 'button',
        text: 'Move Down',
        id: 'idmovedownBtn',
        width: 100,
        style: {
            'float': 'right',
            'margin-bottom':"5px"

        },
        listeners: {
            'click': function ()
            {
                var totalLength = Ext.getCmp("idMainPanel").items.items.length;
                var index;
                for (var i = 0; i < totalLength; i++) {
                    var item = Ext.getCmp("idMainPanel").items.items[i];
                    if (item.id === selectedElement) {
                        index = i;
                        break;
                    }
                }
                if (index == totalLength - 1) {
                    WtfComMsgBox(["Warning", "Row is at last position, cannot move down further."], 0);
                } else {
                    var json = getElementJson(Ext.getCmp(selectedElement), 1);
                    Ext.getCmp("idMainPanel").remove(Ext.getCmp(selectedElement));
                    createExtElements(JSON.stringify(json), index + 1);
                    initTreePanel();
                }
            }
        }
    };
  

    var sequenceSettings = {
        xtype : 'fieldset',
        title : 'Sequence Settings',
        width : 270,
        id:'idsequenceSettingsfieldset',
        hidden:true,
        items : [
            moveupBtn,movedownBtn
        ]
        
    } 
    ***********************************************************************************/
   
   /************************* update button have been removed **************************
    
    var updateBtn = {
        xtype: 'button',
        text: 'Update',
        id: 'idUpdateBtn',
        width: 55,
        style: {
            'float': 'right',
            'margin-right': '25px'

        },
        listeners: {
            'click': function ()
            {
                updateProperty(selectedElement);

            }
        }
    };
    **********************************************************************************/
   
   /************************ border color settings have been removed *******************
    var borderColor = {
        xtype: 'checkbox',
        id: 'idbordercolor',
        fieldLabel: 'Border Color',
        checked:false,
        padding:'2 2 2 2',
        listeners: {
    //                change: function (checkbox, newVal, oldVal) {
    //
    //                }
    }
    };
    
    
    
    var bordersetting = {
        xtype:'fieldset',
        title:'Border settings',
        id:'idbordersettingfieldset',
        hidden:true,
        width:270,
        items:[
            borderColor
        ]
    };
    ***********************************************************************/
   
   var rowHeight = {
       xtype: 'numberfield',
       fieldLabel : 'Height (cm)',
       id: 'idrowHeight',
       padding: '2 2 2 2',
       width: 170,
//       value : getEXTComponent(selectedElement).rowHeight, // in PX
//       minValue: 25, // in PX
//       value : px2cm(getEXTComponent(selectedElement).rowHeight), // in CM
       value : getEXTComponent(selectedElement).rowHeight, // in CM
       minValue: 1, // in CM
       listeners:{
           change : function(field, newVal, oldVal){
               var isSpace = calculatePageHeightOnHeightChange(newVal - oldVal);
               if(isSpace){
                   updateProperty(selectedElement);
               } else{
                   WtfComMsgBox(["Page size exceeds", "No space available on page for increase row height."], 0);
                   getEXTComponent("idrowHeight").suspendEvents();
                   getEXTComponent("idrowHeight").setValue(oldVal);
                   getEXTComponent("idrowHeight").resumeEvents(true);
               }               
           }
       }
   }
   
   var heightsetting = {
        xtype:'fieldset',
        title:'Height settings',
        id:'idheightsettingfieldset',
        hidden: !isPrePrinted,
        width:270,
        items:[
            rowHeight
        ]
    };
    
    propertyPanel = Ext.create("Ext.Panel", {   // property panel created here 
        autoHeight:true,
        width: 300,
        border: false,
        //layout:'column',
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [
                copyHtml,
                mergesplitsetting,
                headerfootersetting,
                heightsetting
        ]
    });
    return propertyPanel;
}
    
/*Column Property Panel*/
function createColumnPropertyPanel(ele) {
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");

    if (propertyPanel)
        propertyPanelRoot.remove(propertyPanel.id);

    var elementId = {
        xtype: 'textfield',
        fieldLabel: 'Element ID',
        id: 'idElementId',
        padding:'2 2 2 2',
        hidden:true,
        //disabled:true,
        readOnly: true
    };
    
    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    }
    
    var marginsettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:270,
        items:[
            topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };

    var type = {
        xtype: 'combo',
        fieldLabel: 'Type',
        id: 'idType',
        padding:'2 2 2 2',
        hidden:true,                         // to be provide Auto/custom facility later
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "type"],
            data: [
            {
                id: "1",
                type: "Auto"
            }, {
                id: "2",
                type: "Custom"

            }
            ]
        }),
        value: (ele.type!=null||ele.type!=undefined)?ele.type:'Auto',
        displayField: 'type',
        valueField: 'id',
        width: 210,
        labelWidth: 100,
        listeners: {
            select: function (e) {
                if (this.getRawValue() == "Custom")
                {
                    Ext.getCmp("idWidth").show();
                    Ext.getCmp("idUnit").show();
                }
                else
                {
                    Ext.getCmp("idWidth").hide();
                    Ext.getCmp("idUnit").hide();
                }
            }

        }

    };
            
    var width = {
        xtype: 'numberfield',
        fieldLabel: 'Width (%)',
        id: 'idWidth',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        step:10,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field,newval, oldval) {
                if ( field.getValue() > 100 ) {
                    field.setValue(100);
                    WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                }
                Ext.getCmp(selectedElement).removeCls('sectionclass_element_'+oldval); 
                Ext.getCmp(selectedElement).addCls('sectionclass_element_'+newval); 
                updateProperty(selectedElement);
            }
        }


    };
    var unit = {
        xtype: 'combo',
        fieldLabel: '    Unit',
        id: 'idUnit',
        padding:'2 2 2 2',
        labelWidth: 100,
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "unit"],
            data: [
            {
                id: "1",
                unit: "%"
            }
            ]
        }),
        value: '%',
        displayField: 'unit',
        valueField: 'id',
        width: 210,
        hidden:true//type.value == "Auto"
    };
        
    var fieldAlignment = {
        xtype: 'combo',
        fieldLabel: 'Field(s) Alignment',
        id: 'fields_Alignment',
        padding:'2 2 2 2',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "type"],
            data: [
            {
                id: "1",
                type: "Block"
            }, {
                id: "2",
                type: "Inline"

            }
            ]
        }),
        value: (ele.fieldalignment!=null||ele.fieldalignment!=undefined)?ele.fieldalignment:'1', //on render & selection
        displayField: 'type',
        valueField: 'id',
        width: 210,
        labelWidth: 100,
        listeners: {
            select: function (field) {
                updateProperty(selectedElement);
            }
        }
    };

    var updateBtn = {
        xtype: 'button',
        text: 'Update',
        id: 'idUpdateBtn',
        width: 55,
        style: {
            'float': 'right',
            'margin-right': '25px'

        },
        listeners: {
            'click': function ()
            {
                updateProperty(selectedElement);

            }
        }

    };
       
    var borderColor = {
        xtype: 'checkbox',
        id: 'idbordercolor',
        fieldLabel: 'Allow Border',
        padding:'2 2 2 2',
        checked:ele.allowborder,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                      if ( newVal ) {
                          Ext.getCmp('idbordercornerstyle').show();
                          Ext.getCmp('idborderColorPanel').show();
                      } else {
                          Ext.getCmp('idbordercornerstyle').hide();
                          Ext.getCmp('idbordercornerstyle').setValue("1");
                          Ext.getCmp('idborderColorPanel').hide();
                      }
                updateProperty(selectedElement);
            }
        }
    };
    var bordercornerstyle = {
            xtype: 'combo',
            fieldLabel: 'Border Corner',
            id: 'idbordercornerstyle',
            padding:'2 2 2 2',
            store: Ext.create("Ext.data.Store", {
                fields: ["id", "type"],
                data: [
                {
                    id: "1",
                    type: "Normal"
                }, {
                    id: "2",
                    type: "Round"

                }
                ]
            }),
            value: (ele.borderedgetype!=null||ele.borderedgetype!=undefined)?ele.borderedgetype:'1',
            displayField: 'type',
            valueField: 'id',
            hidden: !borderColor.checked,
            width: 210,
            labelWidth: 100,
            listeners: {
                select: function (field) {
                     updateProperty(selectedElement);
                }
            }
        };
        var backgroundColorLabel = Ext.create("Ext.form.Label", {
            text: "Background Color",
            width: 100
                        

        });
//        var backgroundColorBtn = Ext.create('Ext.Button', {
//            id: 'idbackgroundcolor',
//            menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//                value: ele.backgroundColor ? rgbToHex( ele.backgroundColor,true) : 'FFFFFF',
//                id: 'colorpicker',
//                handler: function (obj, rgb) {
//                    Ext.colorpicker = Ext.getCmp('colorpicker');
//                    var selectednewcolor = "#" + rgb.toString();
//                    Ext.colorpicker.value = selectednewcolor;
//                    Ext.getCmp('idSelectedBackgroundPanel').body.setStyle('background-color', selectednewcolor);
//                    updateProperty(selectedElement);
//                } // handler
//            }), // menu
//            text: ''
//        });
        var backgroundcolorpicker = Ext.create('Ext.ux.ColorPicker', {
            luminanceImg: '../../images/luminance.png',
            spectrumImg: '../../images/spectrum.png',
            value: ele.backgroundColor ? rgbToHex( ele.backgroundColor,true) : '#FFFFFF',
            id: 'colorpicker',
            listeners: {
                change: function (thiz, newVal) {
                    Ext.getCmp('idSelectedBackgroundPanel').body.setStyle('background-color', newVal);
                    updateProperty(selectedElement);
                }

            }            
        });
        var selectedBackgroundColorPanel = Ext.create("Ext.Panel", {
            id: 'idSelectedBackgroundPanel',
            height: 20,
            width: 30,
            padding:"0 0 0 5",
            border: false,
            bodyStyle: 'margin-top:1px'

        });

        var backgroundColorPanel = Ext.create("Ext.Panel", {
            layout: 'column',
            width: 220,
            padding:'2 2 2 2',
            border: false,
            items: [backgroundColorLabel, selectedBackgroundColorPanel, backgroundcolorpicker]
        });
        
        var borderColorLabel = Ext.create("Ext.form.Label", {
            text: "Border Color",
            width: 100

        });
        var bordercolorpicker = Ext.create('Ext.ux.ColorPicker', {
            luminanceImg: '../../images/luminance.png',
            spectrumImg: '../../images/spectrum.png',
            value: ele.borderColor ? ele.borderColor : '#000000',
            id: 'bordercolorpicker',
            listeners: {
                change: function (thiz, newVal) {
                    Ext.getCmp('idSelectedborderPanel').body.setStyle('background-color', newVal);
                    updateProperty(selectedElement);
                }

            }            
        });
//        var borderColorBtn = Ext.create('Ext.Button', {
//            id: 'idBordercolor',
//            menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//                value: ele.borderColor ? ele.borderColor : '#000000',
//                id: 'bordercolorpicker',
//                handler: function (obj, rgb) {
//                    Ext.colorpicker = Ext.getCmp('bordercolorpicker');
//                    var selectednewcolor = "#" + rgb.toString();
//                    Ext.colorpicker.value = selectednewcolor;
//                    Ext.getCmp('idSelectedborderPanel').body.setStyle('background-color', selectednewcolor);
//                    updateProperty(selectedElement);
//                } // handler
//            }), // menu
//            text: ''
//        });
        var selectedborderColorPanel = Ext.create("Ext.Panel", {
            id: 'idSelectedborderPanel',
            height: 20,
            width: 30,
            padding:"0 0 0 5",
            border: false,
            bodyStyle: 'margin-top:1px'             

        });

        var borderColorPanel = Ext.create("Ext.Panel", {
            layout: 'column',
            width: 220,
            id:'idborderColorPanel',
            padding:'2 2 2 2',
            border: false,
            hidden:!borderColor.checked,
            items: [borderColorLabel, selectedborderColorPanel, bordercolorpicker]
        });
        
        var linespacing = {
            xtype: 'numberfield',
            fieldLabel: 'Line Spacing',
            id: 'idlinespacing',
            padding:'2 2 2 2',
            labelWidth: 100,
            value: ele.linespacing!=null?ele.linespacing:5,
            width: 210,
            minValue:0,
            minLength: 1,
            maxLength: 3,
            listeners: {
                change: function (obj,newVal,oldVal) {
                     updateProperty(selectedElement);
                }
            }
            
        };
        var disabled = false;
        var component = Ext.getCmp(selectedElement);
        if( component!= null &&  component.ownerCt && component.ownerCt.items && component.ownerCt.items.items ){
            var numberOfColumns = component.ownerCt.items.items.length;
            var columnId = component.ownerCt.items.items[numberOfColumns-1].id;
            if(selectedElement == columnId){
                disabled = true;
            }
        }
        var joinPreviousColumn = {  // check box for joining two Columns.
            xtype: 'checkbox',
            id: 'idjoinPreviousColumn',
            fieldLabel: 'Join with next',
            padding:'2 2 2 2',
            disabled:disabled,
            listeners: {
                change: function (checkbox, newVal, oldVal) {
                    var colEle = getEXTComponent(selectedElement);
                    if ( newVal ) {
                        colEle.addCls("joinpreviouscolumn");  // adding join next column css class
                    } else {
                        colEle.removeCls("joinpreviouscolumn"); // removing join next column css class
                    }
                    colEle.isJoinPreviousColumn = newVal;
                }
            }
        };
        
        var bordersettings = {
            xtype:'fieldset',
            title:'Border Settings',
            id:'idbordersettingfieldset',
            width:270,
            items:[
                borderColor,bordercornerstyle
            ]
        };
        var alignsettings = {
            xtype:'fieldset',
            title:'Alignment Settings',
            id:'idalignsettingfieldset',
            width:270,
            items:[
                elementId,type,width,unit,fieldAlignment,linespacing
            ]
        };
        var colorsettings = {
            xtype:'fieldset',
            title:'Color Settings',
            id:'idcolorsettingfieldset',
            width:270,
            items:[
                backgroundColorPanel,borderColorPanel
            ]
        };
        var mergesplitsetting = {
            xtype:'fieldset',
            title:'Merge & Split settings',
            id:'idmergersplitColumnsettingfieldset',
            width:270,
            items:[
                joinPreviousColumn
            ]
        };
    
    propertyPanel = Ext.create("Ext.Panel", {
        autoHeight:true,
        autoScroll:true,
        width: 300,
        border: false,
        //layout:'column',
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [
            alignsettings,colorsettings,bordersettings,marginsettings,mergesplitsetting
        ]
    });
//    propertyPanel.add(updateBtn);
    return propertyPanel;
}
    
/**Line Item Table**/

function createInlineTablePropertyPanel(ele) {
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
    var backgroundcolor="#FFFFFF";
    if (pagelayoutproperty[1].tableproperties) {
        if (pagelayoutproperty[1].tableproperties != undefined) {
             backgroundcolor= pagelayoutproperty[1].tableproperties.backgroundcolor;
        }
    }
    
    if (propertyPanel)
        propertyPanelRoot.remove(propertyPanel.id);

    var elementId = {
        xtype: 'textfield',
        fieldLabel: 'Element ID',
        id: 'idElementId',
        hidden:true,
        //disabled:true,
        hidden:true,
        readOnly: true
    };

    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    }
    
    var marginsettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:260,
        items:[
            topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };
    var consolidatedLineTable = {  // consolidated Line Item
        xtype: 'checkbox',
        id: 'consolidatedLineTableId',
        fieldLabel: 'Consolidated Table',
        padding:'2 2 2 2',
        checked: (ele.isconsolidated == 'true') ? true : false,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                if (!((ele.isconsolidated == true && newVal === true) ||(ele.isconsolidated == false && newVal === false) )) {
                    if (newVal ) {
                        Ext.MessageBox.confirm("Warning","Enabling consolidated flag, will print custom fields data for first line item for a product. Do you want to Continue?", function(btn){
                            if(btn == 'yes') {  
                                ele.isconsolidated = true;
                            }else if(btn == 'no') {
                                ele.isconsolidated = false;           
                            }
                        },this);

                    } else {
                        ele.isconsolidated = false;
                    }
                }
                
            }
        }
    };
    
    var miscsettings = {
        xtype: 'fieldset',
        title: 'Miscellaneous Settings',
        id: 'idmiscsettings',
        width:260,
        items:[
            consolidatedLineTable
        ]
    };
    
//    var isExtendedBorderBtnDisabled = true;
//    if ( ele.getEl().dom.children[0] ) {        
//        if ( ele.getEl().dom.children[0].attributes.getNamedItem("borderstylemode"))  {
//            if ( ele.getEl().dom.children[0].attributes.getNamedItem("borderstylemode").value !== "borderstylemode1" && ele.getEl().dom.children[0].attributes.getNamedItem("borderstylemode").value !== "borderstylemode2" && ele.getEl().dom.children[0].attributes.getNamedItem("borderstylemode").value !== "borderstylemode3" )  {
//                isExtendedBorderBtnDisabled = false;
//            }
//        }
//    }
//    if ( isExtendedBorderBtnDisabled ) {
//        ele.isExtendLineItem = false;
//    }
    var extendedBorderBtn = {
        xtype: 'button',
        id: 'idExtendedBorderBtn',
        cls:'extlineitem',
        width: 60,
        height:75,
        listeners: {
            afterrender: function() {
                createToolTip('idExtendedBorderBtn', 'Extended Line Item Table', '', 'top', true);
            },
            'click': function ()
            {
                var form = new Ext.form.FormPanel({
                    frame: true,
                    labelWidth: 175,
                    autoHeight: true,
                    bodyStyle: 'padding:5px 5px 0;position:relative;background-color:#f1f1f1;',
                    autoWidth: true,
                    defaults: {
                        width: 300
                    },
                    items: [
                    {
                        xtype:'checkbox',
                        //            fieldLabel: 'Allow Pagesize & Orientation',
                        boxLabel: 'Enable Extended Border',
                        id: 'idallowpageorientation',
                        checked:ele.isExtendLineItem,
                        width: 300,
                        listeners: {
                            change: function(field, nval, oval) {
                                if(Ext.getCmp('idallowpageorientation')){
                                    if(nval==true){
                                        Ext.getCmp('idPageSizeCombo').setDisabled(false);
                                        Ext.getCmp('idPageOrientationCombo').setDisabled(false);
                                        Ext.getCmp('idAdjustPageHeightBy').setDisabled(false);
                                    } else{
                                        Ext.getCmp('idPageSizeCombo').setDisabled(true);
                                        Ext.getCmp('idPageOrientationCombo').setDisabled(true);
                                        Ext.getCmp('idAdjustPageHeightBy').setDisabled(true);
                                    }
                                }
                            }
                        }
                    },{
                        xtype: 'combo',
                        fieldLabel: 'Select Size',
                        store: pageSizeStore,
                        id: 'idPageSizeCombo',
                        displayField: 'size',
                        valueField: 'id',
                        queryMode: 'local',
                        width: 300,
                        value:ele.pageSize ? ele.pageSize : "a4",
                        emptyText: 'Select Size',
                        disabled:!ele.isExtendLineItem
                    },{
                        xtype: 'combo',
                        fieldLabel: 'Select Orientation',
                        store: pageOrientationStore,
                        id: 'idPageOrientationCombo',
                        displayField: 'orientation',
                        valueField: 'id',
                        value:ele.pageOrientation ? ele.pageOrientation : "portrait",
                        queryMode: 'local',
                        width: 300,
                        emptyText: 'Select Orientation',
                        disabled:!ele.isExtendLineItem
                    },{
                        xtype: 'textfield',
                        fieldLabel: 'Adjust Page Height By (In Pixels)',
                        id: 'idAdjustPageHeightBy',
                        value: ele.adjustPageHeight ? ele.adjustPageHeight : "0",
                        queryMode: 'local',
                        width: 300,
                        labelWidth: 190,
                        emptyText: '0',
                        disabled: !ele.isExtendLineItem
                    }]
                });
                        
                var extendedBorderWin = new Ext.Window({
                    title:'Extended Border Settings',
                    width: 400,
//                    closeAction:'hide',
                    //        id: 'idExtendedBorderWindow',
                    autoHeight: true,
                    
                        bodyStyle :  "background-color:#f1f1f1;",
                    plain: true,
                    modal: true,
                    items: form,
                    buttons:[
                    {
                        text : 'Save',
                        handler : function(){
                            var isExtendLineItem = false;
                            var pageSize = "a4";
                            var pageOrientation = "portrait";
                            var adjustPageHeight = "0";
                            var extendedLineItemCheckBox = Ext.getCmp("idallowpageorientation");
                            var pageSizeCombo = Ext.getCmp("idPageSizeCombo");
                            var pageOrientationCombo = Ext.getCmp("idPageOrientationCombo");
                            var adjustPageHeightBy = Ext.getCmp("idAdjustPageHeightBy");
                            if ( extendedLineItemCheckBox ) {
                                isExtendLineItem = extendedLineItemCheckBox.getValue();
                            }
                
                            if ( isExtendLineItem ) {
                                if ( pageSizeCombo ) {
                                    pageSize = pageSizeCombo.getValue();
                                }
                                if ( pageOrientationCombo ) {
                                    pageOrientation = pageOrientationCombo.getValue();
                                }
                                if ( adjustPageHeightBy ) {
                                    adjustPageHeight = adjustPageHeightBy.getValue();
                                }
                    
                                ele.isExtendLineItem = isExtendLineItem;
                                ele.pageSize = pageSize;
                                ele.pageOrientation = pageOrientation;
                                ele.adjustPageHeight = adjustPageHeight;
                            } else {
                                ele.isExtendLineItem = isExtendLineItem;
                                ele.pageSize = pageSize;
                                ele.pageOrientation = pageOrientation;
                                ele.adjustPageHeight = adjustPageHeight;
                            }
                            extendedBorderWin.close();
                        }
                    },
                    {
                        text : 'Cancel',
                        handler : function(){
                            extendedBorderWin.close();
                        }
                    }
                    ]
                });
                extendedBorderWin.show();
            }
        }
    };
    var width = {
        xtype: 'numberfield',
        fieldLabel: 'Width (%)',
        id: 'idWidth',
        width: 210,
        minValue: 1,
        maxValue: 100,
        value:ele.tablewidth?ele.tablewidth:100,
        listeners: {
            change: function (e) {
                if ( e.getValue() > 100 ) {
                    e.setValue(100);
                    WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                }
                updateProperty(selectedElement);
            }
        }
    };
    var unit = {
        xtype: 'combo',
        fieldLabel: '  Unit',
        id: 'idUnit',
        hidden:true,
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "unit"],
            data: [
            {
                id: "1",
                unit: "px"
            }, {
                id: "2",
                unit: "%"

            }
            ]
        }),
        value: 'px',
        displayField: 'unit',
        valueField: 'id',
        width: 170
        //            labelWidth: 30,
//        disabled: true
    };

    var backgroundColorLabel = Ext.create("Ext.form.Label", {
        text: "Background Color:",
        padding: '5 0 5 0',
        width: 105

    });
        var backgroundcolorpicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: (ele != null || ele != undefined) ? ele.bordercolor : '#000000',
        id: 'idbackgroundcolorpicker',
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('idSelectedbackgroundPanel').body.setStyle('background-color', newVal);
                updateProperty(selectedElement);
            }

        }
    });
//    var backgroundColorBtn = Ext.create('Ext.Button', {
//        id: 'idbackgroundcolor',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            id: 'idbackgroundcolorpicker',
//            handler: function (obj, rgb) {
//                Ext.colorpicker = Ext.getCmp('idbackgroundcolorpicker');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedbackgroundPanel').body.setStyle('background-color', selectednewcolor);
////                pagelayoutproperty[1].tableproperties.backgroundcolor=selectednewcolor; //giving problems while setting header background color of line item
//                updateProperty(selectedElement);
//
//            } // handler
//        }), // menu
//        text: ''
//    });
    var selectedbackgroundColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedbackgroundPanel',
        height: 20,
        width: 25,
        border: false,
        margin:'1 0 0 0 '
    });

    var backgroundColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 220,
        border: false,
        items: [backgroundColorLabel, selectedbackgroundColorPanel, backgroundcolorpicker]
    });

    var updateBtn = {
        xtype: 'button',
        cls:'editlineitem',
        id: 'idUpdateBtn',
        height:75,
        width: 60,//ERP-18300 [Document Designer]- Label for "Edit Line Item" is not visible.
        style: {
            'margin-top': '5px'
        },
        listeners: {
            afterrender: function() {
                createToolTip('idUpdateBtn', 'Edit Line Item', '', 'top', true);
            },
            'click': function ()
            {
                var selectedfield = Ext.getCmp(selectedElement);
                var lineitemparentpanel = Ext.getCmp(selectedElement).ownerCt;
                
                var Posx=selectedfield.x;
                var Posy=selectedfield.y;
//                documentLineColumns=
                openProdWindowAndSetConfig(selectedfield,lineitemparentpanel.id,true,Posx,Posy,documentLineColumns,lineitemparentpanel);
            }
        }
    };
    var removeSummaryTableBtn = {
        xtype: 'button',
        cls:'remsummtable',
        id: 'removesummarytableid',
        hidden:(Ext.getCmp(selectedElement).ownerCt.items.items[0].isSummaryTableApplied)?!(Ext.getCmp(selectedElement).ownerCt.items.items[0].isSummaryTableApplied):true,
        width: 60,
        height:75,
        style: {
            'margin-top': '5px'
        },
        listeners: {
            afterrender: function() {
                createToolTip('removesummarytableid', 'Remove Summary Table', '', 'top', true);
            },
            'click': function ()
            {
                var lineitemparentpanel = Ext.getCmp(selectedElement).ownerCt;
                if(lineitemparentpanel!=null){
                    if(lineitemparentpanel.items){
                        if(lineitemparentpanel.items.items){
                            if(lineitemparentpanel.items.items[0]){
                                    lineitemparentpanel.items.items[0].summaryTableJson=null;
                                    lineitemparentpanel.items.items[0].isSummaryTableApplied= false;
                            }
                        }
                    }
                }
                summaryTableJson = null;
                isSummaryTableApplied = false;
                if( document.getElementById('summaryTableID')!=null ){
                    document.getElementById('summaryTableID').remove();
                }
            }
        }
    };
    var boldText = {
        xtype: 'checkbox',
        id: 'boldTextLineItemId',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        checked: (ele.bold == 'true') ? true : false,
        listeners: {
            change: function (e) {
//                updateProperty(selectedElement);
                var table = Ext.get('itemlistconfigsectionPanelGrid').dom;
                var cells = table.children[1].children[0].cells;
                if (Ext.getCmp("boldTextLineItemId").getValue() == true) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontWeight="bold";
                    }
                    Ext.getCmp(selectedElement).bold = "true";
                } else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontWeight="normal";
                    }
                    Ext.getCmp(selectedElement).bold = "false";
                }
            }
        }
    };
    var italicText = {
        xtype: 'checkbox',
        id: 'italicTextLineItemId',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        checked: (ele.italic == 'true') ? true : false,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
//                updateProperty(selectedElement);
                var table = Ext.get('itemlistconfigsectionPanelGrid').dom;
                var cells = table.children[1].children[0].cells;
                if (newVal) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontStyle="italic";
                    }
                    Ext.getCmp(selectedElement).italic = "true";
                } else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontStyle="normal";
                    }
                    Ext.getCmp(selectedElement).italic = "false";
                }
            }
        }
    };
    
    var underLine = {
        xtype: 'checkbox',
        id: 'underLineTextLineItemId',
        fieldLabel: 'Underline',
        padding:'2 2 2 2',
        checked: (ele.underline == 'true') ? true : false,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
//                updateProperty(selectedElement);
                var table = Ext.get('itemlistconfigsectionPanelGrid').dom;
                var cells = table.children[1].children[0].cells;
                if (Ext.getCmp("underLineTextLineItemId").getValue() == true) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.textDecoration="underline";
                    }
                    Ext.getCmp(selectedElement).underline = "true";
                } else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.textDecoration="";
                    }
                    Ext.getCmp(selectedElement).underline = "false";
                }
            }
        }
    };
    
    var repeatLineTable = {  // repeat line Item table checkbox
        xtype: 'checkbox',
        id: 'repeatLineTableId',
        fieldLabel: 'Repeat for every product',
        padding:'2 2 2 2',
        checked: (ele.islineitemrepeat == 'true') ? true : false,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                ele.islineitemrepeat = newVal;
            }
        }
    };
    var includeProductCategory = {
        xtype: 'checkbox',
        id: 'includeProductCategoryId',
        fieldLabel: 'Include Grouping Row Before',
        padding:'2 2 2 2',
        checked: ele.includeProductCategory,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                if ( newVal ) {
                    var rowHTML = "";
                    rowHTML = addGroupingRow(0);
                    ele.groupingRowHTMl = rowHTML;
                } else {
                    removeGroupingRow(0);
                    ele.groupingRowHTMl = "";
                    isGroupingApplied = false;
                }
                updateProperty(selectedElement);
            }
        }
    };
    
    var includegroupingRowAfter = {
        xtype: 'checkbox',
        id: 'includegroupingRowAfterId',
        fieldLabel: 'Include Amount Formatting Row',
        padding:'2 2 2 2',
        checked: ele.includegroupingRowAfterId,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                if ( newVal ) {
                    var rowHTML = "";
                    rowHTML = addGroupingRow(1);
                    ele.groupingRowAfterHTML = rowHTML;
                } else {
                    removeGroupingRow(1);
                    ele.groupingRowAfterHTML = "";
                    isFormattingApplied = false;
                }
                updateProperty(selectedElement);
            }
        }
    };
    
    var pagefontstore= Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
        {
            "val":"", 
            "name":"None"
        },
        
        {
            "val":"sans-serif", 
            "name":"Sans-Serif"
        },

        {
            "val":"arial", 
            "name":"Arial"
        },

        {
            "val":"verdana", 
            "name":"Verdana"
        },

        {
            "val":"times new roman", 
            "name":"Times New Roman"
        },

        {
            "val":"tahoma", 
            "name":"Tahoma"
        },

        {
            "val":"calibri", 
            "name":"Calibri"
        },
        {
            "val":"courier new", 
            "name":"Courier New"
        },
        {
            "val":"MICR Encoding", 
            "name":"MICR"
        }
        ]
    });
    
    
    var pagefontfields = {
        xtype: 'combo',
        fieldLabel: 'Font',
        id:'pageFontLineItemId',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        padding:'2 2 2 2',
        labelWidth:100,
        width:210,
        value:(ele != null || ele != undefined) ? ele.fontfamily : '',
        listeners: {
            'select': function (e) {
//                updateProperty(selectedElement);
                var table = Ext.get('itemlistconfigsectionPanelGrid').dom;
                var cells = table.children[1].children[0].cells;
                var tmpFontFamily = Ext.getCmp('pageFontLineItemId').getValue();
                for( var i=0; i< cells.length; i++ ){
                    cells[i].style.fontFamily = tmpFontFamily;
                }
                Ext.getCmp(selectedElement).fontfamily = Ext.getCmp('pageFontLineItemId').getValue();
            }
        }
    };
    
    var fontSize = {
        xtype: 'numberfield',
        fieldLabel: 'Font Size',
        id: 'fontSizeLineItemId',
        value: (ele != null || ele != undefined) ? ele.fontsize : 'None', //parseInt to remove the px e.g 250px to 250
        width: 210,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        minValue : 0,
        listeners: {
            'change': function () {
//                updateProperty(selectedElement);
                var table = Ext.get('itemlistconfigsectionPanelGrid').dom;
                var cells = table.children[1].children[0].cells;
                if (Ext.getCmp('fontSizeLineItemId').getValue() > 0) {
                    var tmpFontSize = Ext.getCmp('fontSizeLineItemId').getValue() + 'px';
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontSize=tmpFontSize;
                    }
                    Ext.getCmp(selectedElement).fontsize = Ext.getCmp('fontSizeLineItemId').getValue();
                } else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontSize="";
                    }
                    Ext.getCmp(selectedElement).fontsize = "";
                }
            }
        }
    };
    var alignStore;
    alignStore=new Ext.data.Store({
        fields : ['id','align'],
        data : [
        {
            id: 0, 
            align: 'Left'
        },

        {
            id: 1,    
            align: 'Center'
        },

        {
            id: 2, 
            align: 'Right'
        }

        ]
    });
    var alignCombo = new Ext.form.field.ComboBox({
        fieldLabel: 'Align Headers',
        store: alignStore,
        id: 'allignLineTableCombo',
        displayField: 'align',
        valueField: 'id',
        labelWidth:100,
        queryMode: 'local',
        width: 210,
        emptyText: 'Select alignment',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        value :ele.align,
        padding:'2 2 2 2',
        listeners: {
            'change': function (ele, val){
//                updateProperty(selectedElement);
                var table = Ext.get('itemlistconfigsectionPanelGrid').dom;
                var cells = table.children[1].children[0].cells;
                var align = val;
                if(align== 'left'  || align === 0){
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].align="left";
                            cells[i].style.textAlign="left";
                        cells[i].style.paddingLeft="4px";
                    }
                    Ext.getCmp(selectedElement).align = 0;
                } else if(align=='center' || align == 1){
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].align="center";
                        cells[i].style.textAlign="center";
                    }
                    Ext.getCmp(selectedElement).align = 1;
                } else{
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].align="right";
                        cells[i].style.paddingRight="4px";
                        cells[i].style.textAlign="right";
                    }
                    Ext.getCmp(selectedElement).align = 2;
                }
            }
        }
    });
    
    var spacesettings = {
        xtype: 'fieldset',
        width:260,
        title:'Space Settings',
        id:'idspacesettingsfieldset',
        items: [
            repeatLineTable,width
        ]
    };
    
    var fontsettings = {
        xtype: 'fieldset',
        width:260,
        title:'Font Settings',
        id:'idfontsettingsfieldset',
        items: [
           boldText,italicText, underLine, fontSize, pagefontfields
        ]
    };
    
    var headersettings = {
        xtype: 'fieldset',
        width:260,
        title:'Header Settings',
        id:'idheadersettingsfieldset',
        items: [
            alignCombo,backgroundColorPanel
        ]
    };
    /*
     * Array for sorting field combo
     */
    var records = [];
    records.push({
        id: "",
        name: "None"
    });
    var columnslength = ele.initialConfig.columns.length;
    // Get line item table selected fields for sorting combo
    for(var cnt = 0; cnt < columnslength; cnt++){
        if(ele.initialConfig.columns[cnt].fieldid !== "0"){
            records.push({
                id: ele.initialConfig.columns[cnt].fieldid,
                name: ele.initialConfig.columns[cnt].columnname,
                xtype: ele.initialConfig.columns[cnt].xtype
            });
        }
    }
    // Store for sorting fields
    var sortfieldstore= Ext.create('Ext.data.Store', {
        fields: ['id', 'name', 'xtype'],
        data : records
    });
    // Combo for sorting field
    var sortfieldcombo = {
        xtype: 'combo',
        fieldLabel: 'Select Field',
        id:'sortfieldid',
        displayField: 'name',
        valueField: 'id',
        store: sortfieldstore,
        value: ele != null ? (ele.sortfield != undefined ? ele.sortfield : '') : '',
        padding:'2 2 2 2',
        labelWidth:100,
        width:210,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    // Store for sorting order
    var sortorderstore= Ext.create('Ext.data.Store', {
        fields: ['id', 'name'],
        data : [
        {
            "id":"", 
            "name":"None"
        },
        {
            "id":"asc", 
            "name":"Ascending"
        },
        {
            "id":"desc", 
            "name":"Descending"
        }
        ]
    });
    //Combo for sorting order
    var sortorder = {
        xtype: 'combo',
        fieldLabel: 'Sorting Order',
        id:'sortorderid',
        displayField: 'name',
        valueField: 'id',
        store: sortorderstore,
        value: ele != null ? (ele.sortorder != undefined ? ele.sortorder : '') : '',
        padding:'2 2 2 2',
        labelWidth:100,
        width:210,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    // Fieldset for sorting components
    var sortingsettings = {
        xtype: 'fieldset',
        width:260,
        title:'Sorting Settings',
        id:'idsortingsettingsfieldset',
        items: [
            sortfieldcombo, sortorder
        ]
    };
    
    var includeProductCategorysettings = {
        xtype: 'fieldset',
        width:260,
        title:'Grouping settings',
        id:'idincludeProductCategoryfieldset',
        items: [
            includeProductCategory,includegroupingRowAfter
        ]
    };
    
       
    propertyPanel = Ext.create("Ext.Panel", {
        autoHeight: true,
        width: 295,//ERP-18300 [Document Designer]- Label for "Edit Line Item" is not visible.
        border: false,
        layout:'column',
        id: 'idPropertyPanel',
        defaults: {
            margin: '7 0 0 10'
        },
        items: [updateBtn,extendedBorderBtn,removeSummaryTableBtn,
        elementId,unit,spacesettings,fontsettings,headersettings,marginsettings,sortingsettings,includeProductCategorysettings,miscsettings
        ]          
    });

    return propertyPanel;
}
    
/*Setting property on fields of  Property Panel*/
function setProperty(selectedElement)
{
    if (selectedElement == "idMainPanel")
    {
        (Ext.getCmp("idEastPanel")).setTitle("Page Property Panel");
        var record = treePanel.getStore().getNodeById("idPageTree");
        Ext.getCmp("idElementId").setValue("idMainPanel");
        treePanel.getSelectionModel().select(record)
        return;
    }
    var ele = Ext.getCmp("idMainPanel").queryById(selectedElement);
    if (ele === null) {
        ele = Ext.getCmp(selectedElement);
    }
    var fieldType = ele.fieldType;

    var record = treePanel.getStore().getNodeById(selectedElement);
    treePanel.getSelectionModel().select(record)

    /*Static text*/
    if (fieldType == Ext.fieldID.insertText) {
        (Ext.getCmp("idEastPanel")).setTitle("Text Property Panel");
        var obj = ele;
        
        var margintop =  Ext.getCmp(selectedElement).marginTop;
        var marginleft =  Ext.getCmp(selectedElement).marginLeft;
        var marginright =  Ext.getCmp(selectedElement).marginRight;
        var marginbottom =  Ext.getCmp(selectedElement).marginBottom;
        var fontfamily = Ext.getCmp(selectedElement).fontfamily;
        var isBulletsApplied = Ext.getCmp(selectedElement).isBulletsApplied;
        var bulletType = Ext.getCmp(selectedElement).bulletType?Ext.getCmp(selectedElement).bulletType:"none";
        
        var elementHtml = document.getElementById(selectedElement);
        var elementWidth = elementHtml.style.width;
        var elementWeight = elementHtml.style.fontWeight;
        var elementStyle = elementHtml.style.fontStyle;
        Ext.getCmp("idElementId").setFieldLabel("Text ID");
        Ext.getCmp("idElementId").setValue(ele.id);
        var label = obj.el.dom.innerHTML;
        if ( label==="&nbsp;" ) {
            label = "";
        }
        
        label = label.replace(/<br>/g,'\n');
        label = label.replace(/&nbsp;/g,' ');
        label = label.replace(/<[^<>]*>/g,'');
        Ext.getCmp("idApplyBullets").setValue(isBulletsApplied);
        Ext.getCmp("idbulletType").setValue(bulletType)
        Ext.getCmp('pagefontid').setValue(fontfamily);
        Ext.getCmp("idtopmargin").setValue(margintop);
        Ext.getCmp("idbottommargin").setValue(marginbottom);
        Ext.getCmp("idrightmargin").setValue(marginright);
        Ext.getCmp("idleftmargin").setValue(marginleft);
        Ext.getCmp("idSelectedLabel").setValue(label);
        Ext.getCmp('idTextAlign').setDisabled(false);
        Ext.getCmp('idUnit').setDisabled(false);
        Ext.getCmp('idWidth').setDisabled(false);
        Ext.getCmp('idUseColon').setDisabled(false);
        Ext.getCmp('idColonAlign').setDisabled(false);
        Ext.getCmp('idWidth').setValue(elementWidth);
        Ext.getCmp('idUnit').setRawValue(ele.unit);

//        var classValue = Ext.getCmp("textColorText").getEl().dom.children[0].children[0].children[1].children[0].attributes.class.value;
//        classValue += " jscolor";
//        Ext.getCmp("textColorText").getEl().dom.children[0].children[0].children[1].children[0].attributes.class.value = classValue;
//        Ext.getCmp("textColorText").getEl().dom.children[0].children[0].children[1].children[0].attributes.class.value = classValue;
        
        var flg = Ext.getCmp('idUseColon').getValue();
        if (flg == true) {
            Ext.getCmp("idColonAlign").setDisabled(false);
        } else {
            Ext.getCmp("idColonAlign").setDisabled(true);
        }

        //            if (Ext.getCmp('idWidth').getValue() > 0) {
        //                obj.setWidth(Ext.getCmp('idWidth').getValue());
        //            }

        if (ele.textalignclass == "classTextAligment_left") {
            Ext.getCmp('idTextAlign').setValue("Left");
        }
        else if (ele.textalignclass == "classTextAligment_center") {
            Ext.getCmp('idTextAlign').setValue("Center");

        }
        else if (ele.textalignclass == "classTextAligment_right") {
            Ext.getCmp('idTextAlign').setValue("Right");

        }

        if (elementWeight == "bold") {
            Ext.getCmp('idBoldText').setValue(true);
        } else {
            Ext.getCmp('idBoldText').setValue(false);
        }

        if (elementStyle == "italic") {
            Ext.getCmp('idItalicText').setValue(true);
        } else {
            Ext.getCmp('idItalicText').setValue(false);
        }
        /*
             var style = document.createElement('style');
             style.type = 'text/css';
             var colortxt = 'red !important';
             style.innerHTML = '.cssClass { color :'+colortxt+'}';
             document.getElementsByTagName('head')[0].appendChild(style);
             document.getElementById(ele.items.items[0].id).className = 'cssClass';
             
             //ele.addClass('cssClass');
             */

        Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', ele.textcolor);
        Ext.getCmp('idFontSize').setValue(ele.fontsize);
        Ext.getCmp('fields_Alignment').setValue(ele.fieldAlignment);    
    } else if (fieldType == Ext.fieldID.insertField) {
        (Ext.getCmp("idEastPanel")).setTitle("Field Property Panel");
        var obj = ele;
        var elementWidth = ele.getEl().dom.style.width;
        var isbold = ele.bold;
        var isitalic = ele.italic
        var margintop =  Ext.getCmp(selectedElement).marginTop;
        var marginleft =  Ext.getCmp(selectedElement).marginLeft;
        var marginright =  Ext.getCmp(selectedElement).marginRight;
        var marginbottom =  Ext.getCmp(selectedElement).marginBottom;
        var bordercolor = Ext.getCmp(selectedElement).borderColor;
        
        Ext.getCmp('pagefontid').setValue(ele.fontfamily);
        Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', ele.textcolor);
        Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', ele.borderColor);
        Ext.getCmp('idBoldText').setValue(isbold);
        Ext.getCmp('idItalicText').setValue(isitalic);
        Ext.getCmp("idElementId").setFieldLabel("Field ID");
        Ext.getCmp("idElementId").setValue(ele.id);
        //  Ext.getCmp("idSelectedLabel").setValue(obj.el.dom.innerHTML);
        Ext.getCmp('idTextAlign').setDisabled(false);
        Ext.getCmp('idColonAlign').setDisabled(false);
        Ext.getCmp('idUnit').setDisabled(false);
        Ext.getCmp('idWidth').setDisabled(false);
        Ext.getCmp('idHeight').setDisabled(false);

        Ext.getCmp('idcustomdesignerfieldselectcombo').setValue(obj.label);
        
        Ext.getCmp("idtopmargin").setValue(margintop);
        Ext.getCmp("idbottommargin").setValue(marginbottom);
        Ext.getCmp("idrightmargin").setValue(marginright);
        Ext.getCmp("idleftmargin").setValue(marginleft);
        
        if(ele.unit=='%'){//elements unit
            if (obj.elementwidth > 0) {  //width in percentage assigned tto elementwidth variable in obj
                Ext.getCmp('idWidth').setValue(parseInt(ele.elementwidth));
                Ext.getCmp(selectedElement).elementwidth=ele.elementwidth;
            }
                
        //                  if (obj.height > 0) {  //width in percentage
        //                    Ext.getCmp('idHeight').setValue(ele.elementheight);
        //                    Ext.getCmp(selectedElement).elementheight=ele.elementheight;
        //                }
        }else{
            Ext.getCmp('idWidth').setValue(obj.width);
            Ext.getCmp(selectedElement).elementwidth=obj.width;
            //                Ext.getCmp('idHeight').setDisabled(false);
            //                Ext.getCmp('idHeight').setValue(obj.height);
            Ext.getCmp(selectedElement).elementheight=obj.height;
        }
         Ext.getCmp('idUnit').setRawValue(ele.unit);
         Ext.getCmp('idWidth').setValue(elementWidth);
        if (ele.textalignclass == "classTextAligment_left") {
            Ext.getCmp('idTextAlign').setValue("Left");
        }
        else if (ele.textalignclass == "classTextAligment_center") {
            Ext.getCmp('idTextAlign').setValue("Center");

        }
        else if (ele.textalignclass == "classTextAligment_right") {
            Ext.getCmp('idTextAlign').setValue("Right");

        }
        Ext.getCmp('idFontSize').setValue(ele.fontsize);
        Ext.getCmp('fields_Alignment').setValue(ele.fieldAlignment); 

    } else if (fieldType == Ext.fieldID.insertImage) {
        (Ext.getCmp("idEastPanel")).setTitle("Image Property Panel");
        var obj = ele.el.dom;
        var margintop =  Ext.getCmp(selectedElement).marginTop;
        var marginleft =  Ext.getCmp(selectedElement).marginLeft;
        var marginright =  Ext.getCmp(selectedElement).marginRight;
        var marginbottom =  Ext.getCmp(selectedElement).marginBottom;
        var finalheight ;
        var finalwidth;
        Ext.getCmp("idElementId").setFieldLabel("Image ID");
        Ext.getCmp("idElementId").setValue(ele.id);
        Ext.getCmp('idTextAlign').setDisabled(false);
        Ext.getCmp('idUnit').setDisabled(false);
        Ext.getCmp('idWidth').setDisabled(false);
        Ext.getCmp('idHeight').setDisabled(false);
            
        if(ele.unit=='%'){//elements unit
            if (ele.width > 0) {  //width in percentage
                var tmpWidth = 0;
                //                    tmpWidth =(obj.width/ Ext.getCmp(selectedElement).ownerCt.getWidth())*100;
//                Ext.getCmp('idWidth').setValue(ele.elementwidth);
//                Ext.getCmp(selectedElement).elementwidth=ele.elementwidth;
                finalwidth = ele.elementwidth;
            }
                
        }else{
            if(ele.width==null||ele.width==undefined){//first time rendering problem
                finalwidth=obj.offsetWidth;
            }else if (ele.width=="auto" || ele.width==""  ){
                finalwidth = "auto";
            }else{
                finalwidth=ele.width;                  
            }
        } 
                
            if(ele.elementheight==null||ele.elementheight==undefined){
                finalheight=obj.offsetHeight;
            }else if (ele.elementheight=="auto" || ele.elementheight==""  ){
                finalheight = "auto";
            }else{
                finalheight=ele.elementheight;                  
            }
                
            Ext.getCmp('idWidth').setValue(finalwidth);
            Ext.getCmp(selectedElement).elementwidth=finalwidth;
            Ext.getCmp('idHeight').setValue(finalheight);
            Ext.getCmp(selectedElement).elementheight=finalheight;
            
        Ext.getCmp("idtopmargin").setValue(margintop);
        Ext.getCmp("idbottommargin").setValue(marginbottom);
        Ext.getCmp("idrightmargin").setValue(marginright);
        Ext.getCmp("idleftmargin").setValue(marginleft);   
        Ext.getCmp('idUnit').setRawValue(ele.unit);
        if (ele.textalignclass == "imageshiftleft") {
            Ext.getCmp('idTextAlign').setValue("Left");
        }
        if (ele.textalignclass == "imageshiftcenter") {
            Ext.getCmp('idTextAlign').setValue("Center");

        }
        else if (ele.textalignclass == "imageshiftright") {
            Ext.getCmp('idTextAlign').setValue("Right");

        }
        Ext.getCmp('fields_Alignment').setValue(ele.fieldAlign); 
    } else if (fieldType == Ext.fieldID.insertTable) {
        (Ext.getCmp("idEastPanel")).setTitle("Inline Table Property Panel");
        var obj = ele;
        var table = Ext.get('itemlistconfigsectionPanelGrid').dom;
        var tableheader = Ext.get('itemlistconfigsectionPanelGrid').dom.children[1].rows[0];
        var margintop =  Ext.getCmp(selectedElement).marginTop;
        var marginleft =  Ext.getCmp(selectedElement).marginLeft;
        var marginright =  Ext.getCmp(selectedElement).marginRight;
        var marginbottom =  Ext.getCmp(selectedElement).marginBottom;
        var isconsolidated =  Ext.getCmp(selectedElement).isconsolidated;
        var color1 = Ext.getCmp(selectedElement).bordercolor
        
        Ext.getCmp('idSelectedbackgroundPanel').body.setStyle('background', color1);
        Ext.getCmp('idbackgroundcolorpicker').value = color1;
        Ext.getCmp("idElementId").setFieldLabel("Table ID");
        Ext.getCmp("idElementId").setValue(ele.id);
        Ext.getCmp('idUnit').setDisabled(true);
        Ext.getCmp('idWidth').setDisabled(false);
        Ext.getCmp("idtopmargin").setValue(margintop);
        Ext.getCmp("idbottommargin").setValue(marginbottom);
        Ext.getCmp("idrightmargin").setValue(marginright);
        Ext.getCmp("idleftmargin").setValue(marginleft);  
        Ext.getCmp("consolidatedLineTableId").setValue(isconsolidated);  
        var color = tableheader.bgColor;
    //            var tmpWidth = '' + ele.style.width;
    //            Ext.getCmp('idWidth').setValue(ele.style.width);
    //
    //            if (tmpWidth.indexOf("%") != -1)
    //                Ext.getCmp('idUnit').setRawValue("%");
    //            else
    //                Ext.getCmp('idUnit').setRawValue("px");

    //            Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', selectednewcolor);
    } else if ( fieldType == Ext.fieldID.insertGlobalTable ) {
        (Ext.getCmp("idEastPanel")).setTitle("Global Table Property Panel");
        var obj = ele;
        var margintop =  Ext.getCmp(selectedElement).marginTop;
        var marginleft =  Ext.getCmp(selectedElement).marginLeft;
        var marginright =  Ext.getCmp(selectedElement).marginRight;
        var marginbottom =  Ext.getCmp(selectedElement).marginBottom;
        var borderColor = Ext.getCmp(selectedElement).borderColor;
        var backgroundColor = Ext.getCmp(selectedElement).backgroundColor;
        var headerColor = Ext.getCmp(selectedElement).headerColor;
        var tableAlign  = Ext.getCmp(selectedElement).tableAlign;
        var pageSize  = Ext.getCmp(selectedElement).pageSize;
        var pageOrientation  = Ext.getCmp(selectedElement).pageOrientation;
        var table = obj.getEl().dom.children[0];
        var tableheader = table.children[0].rows[0];
        var isSummaryTable = Ext.getCmp(selectedElement).isSummaryTable;
        var summaryTableHeight = Ext.getCmp(selectedElement).summaryTableHeight;
        var ownerwidth = ele.ownerCt.getWidth();
        var unit = obj.unit;
        var fieldAlignment = obj.fieldAlignment;
        var width;
        if ( fieldAlignment === "2" ) {
            width = obj.getEl().dom.style.width;
        } else {
            width = table.width;
        }
//        if ( unit == "px" ) {
//            Ext.getCmp("globalTableUnitid").setValue("px");
//            Ext.getCmp("globalTableWidthid").setValue(width);
//        } else {
//            Ext.getCmp("globalTableUnitid").setValue("%");
//            var percentwidth = Math.round((width/ownerwidth)*100);
//            Ext.getCmp("globalTableWidthid").setValue(percentwidth);
//        } 
        Ext.getCmp('idSelectedBorderPanel').body.setStyle('background', borderColor);
        Ext.getCmp('colorpicker').value = borderColor;
        Ext.getCmp('idSelectedbackgroundPanel').body.setStyle('background', backgroundColor);
        Ext.getCmp('idbackgroundcolorpicker').value = backgroundColor;
        Ext.getCmp('idSelectedheaderbackgroundPanel').body.setStyle('background', headerColor);
        Ext.getCmp('idheaderbackgroundcolorpicker').value = headerColor;
        Ext.getCmp("idtopmargin").setValue(margintop);
        Ext.getCmp("idbottommargin").setValue(marginbottom);
        Ext.getCmp("idrightmargin").setValue(marginright);
        Ext.getCmp("idleftmargin").setValue(marginleft);
                
        Ext.getCmp('idfieldsAlignment').setValue(fieldAlignment);
        Ext.getCmp("globalTableWidthid").setValue(width);
        Ext.getCmp("allignTableCombo").setValue(tableAlign);
        Ext.getCmp("idsummarytablecheck").setValue(isSummaryTable);
        Ext.getCmp("idsummaryTableHeightCombo").setValue(summaryTableHeight);
        
    } else if (fieldType == Ext.fieldID.insertRowPanel) {
        getEXTComponent("idEastPanel").setTitle("Row Property Panel");
//        Ext.getCmp("idElementId").setFieldLabel("Row ID");
//        Ext.getCmp("idElementId").setValue(ele.id);
        /* Need to suspend "Column(s)" property pane
             * l field's change event 
             * because it fires updateProperty() where setColumns() recreates columns */
        getEXTComponent("idColumns").suspendEvent('change'); 
        getEXTComponent("idColumns").setValue(ele.column);
        getEXTComponent("idColumns").resumeEvent('change');
            
        getEXTComponent("idHeader").setValue(ele.isheader);
        getEXTComponent("idFooter").setValue(ele.isfooter);
        getEXTComponent("idsetAsGlobalHeaderBtn").setDisabled(true);
        getEXTComponent("idsetAsGlobalFooterBtn").setDisabled(true);
        if(ele.isheader){
            getEXTComponent("idsetAsGlobalHeaderBtn").setDisabled(false);
            getEXTComponent("idHeader").setDisabled(false);
        }else if(ele.isfooter){
            getEXTComponent("idsetAsGlobalFooterBtn").setDisabled(false);
            getEXTComponent("idFooter").setDisabled(false);
        }
        
        if (ele.isJoinNextRow) {
            getEXTComponent("idjoinNextRow").setValue(true);
        } else {
            getEXTComponent("idjoinNextRow").setValue(false);
        }
        
        if(ele.isPrePrinted){
//            getEXTComponent("idrowHeight").setValue(parseFloat(px2cm(ele.rowHeight).toFixed(1)));
            getEXTComponent("idrowHeight").setValue(parseFloat(ele.rowHeight).toFixed(1));
        }
        
    } else if (fieldType == Ext.fieldID.insertColumnPanel) {   
        var margintop =  Ext.getCmp(selectedElement).marginTop;
        var marginleft =  Ext.getCmp(selectedElement).marginLeft;
        var marginright =  Ext.getCmp(selectedElement).marginRight;
        var marginbottom =  Ext.getCmp(selectedElement).marginBottom;
        var width = Ext.getCmp(selectedElement).columnwidth;
        var ele1 = Ext.get(selectedElement + "-innerCt");
        var color = ele1.dom.style.backgroundColor;
        var borderColor = ele1.dom.style.borderColor;
        
        Ext.getCmp("idtopmargin").setValue(margintop);
        Ext.getCmp("idbottommargin").setValue(marginbottom);
        Ext.getCmp("idrightmargin").setValue(marginright);
        Ext.getCmp("idleftmargin").setValue(marginleft);
        
        Ext.getCmp("idElementId").setFieldLabel("Column ID");
        (Ext.getCmp("idEastPanel")).setTitle("Column Property Panel");
        Ext.getCmp("idElementId").setValue(ele.id);
        Ext.getCmp("idWidth").setValue(width);
        Ext.getCmp('idSelectedBackgroundPanel').body.setStyle('background', color);
        Ext.getCmp('colorpicker').value = color;
        Ext.getCmp('idSelectedborderPanel').body.setStyle('background', borderColor);
        Ext.getCmp('bordercolorpicker').value = borderColor;
        
        if (ele.isJoinPreviousColumn) {
            getEXTComponent("idjoinPreviousColumn").setValue(true);
        } else {
            getEXTComponent("idjoinPreviousColumn").setValue(false);
        }
    
    } else if (fieldType == Ext.fieldID.insertDataElement){
        (Ext.getCmp("idEastPanel")).setTitle("Data Element Property Panel");
    } else if (fieldType == Ext.fieldID.insertDetailsTable) {
        //set properties of Details Table
        (Ext.getCmp("idEastPanel")).setTitle("Details Table Property Panel");
        var obj = ele;
        //get margin from element
        var margintop =  Ext.getCmp(selectedElement).marginTop;
        var marginleft =  Ext.getCmp(selectedElement).marginLeft;
        var marginright =  Ext.getCmp(selectedElement).marginRight;
        var marginbottom =  Ext.getCmp(selectedElement).marginBottom;
        //set margin in property panel margin components
        Ext.getCmp("idtopmargin").setValue(margintop);
        Ext.getCmp("idbottommargin").setValue(marginbottom);
        Ext.getCmp("idrightmargin").setValue(marginright);
        Ext.getCmp("idleftmargin").setValue(marginleft);  
        Ext.getCmp("idElementId").setFieldLabel("Table ID");
        Ext.getCmp("idElementId").setValue(ele.id);
        Ext.getCmp('idWidth').setDisabled(false);
        //set font family
        Ext.getCmp("fontDetailsTableId").setValue(Ext.getCmp(selectedElement).fontfamily);  
    }
    Ext.getCmp("idEastPanel").doLayout();

}

/*Updating property on elements of Column Panel on change of Property Panel fields*/
function updateProperty(selectedElement,isMerging)
{
    var ele = Ext.getCmp("idMainPanel").queryById(selectedElement);
     var fieldType="";
    if(ele){
        fieldType = ele.fieldType;
    }else{
        ele = Ext.getCmp(selectedElement);
        fieldType = Ext.getCmp(selectedElement).fieldType;//Main Panel
    }
   
     
    if ( fieldType === Ext.fieldID.insertText ) {  /****** Update property for Insert Text *************/
        /*********** Fetched all the components present in Insert text Property Panel ***********/     
        var textEle = getEXTComponent(selectedElement);
        var selectedTextArea = getEXTComponent("idSelectedLabel");  
        var applyBulletCheckBox = getEXTComponent("idApplyBullets"); 
        var bulletTypeCombo = getEXTComponent("idbulletType");      
        var textColorPicker = getEXTComponent("colorpicker");      
        var topMarginNumberField = getEXTComponent("idtopmargin");      
        var leftMarginNumberField = getEXTComponent("idleftmargin");      
        var rightMarginNumberField = getEXTComponent("idrightmargin");      
        var bottomMarginNumberField = getEXTComponent("idbottommargin");      
        var textAlignCombo = getEXTComponent("idTextAlign");      
        var widthNumberField = getEXTComponent("idWidth");      
        var unitCombo = getEXTComponent("idUnit");      
        var boldCheckBox = getEXTComponent("idBoldText");      
        var italicCheckBox = getEXTComponent("idItalicText");      
        var textLineCombo = getEXTComponent("idTextLine");      
        var fontSizeNumberField = getEXTComponent("idFontSize");      
        var fontCombo = getEXTComponent("pagefontid");      
        var fieldAlignmentCombo = getEXTComponent("fields_Alignment");      
        
        /**************** declared all the variables used *******************/
        var isBulletsApplied = false;  
        var selectedText = "";         
        var bulletType = "none";       
        var selectedTextColor = "#000000";     
        var topMargin = "0px";  
        var leftMargin = "0px"; 
        var rightMargin = "0px";
        var bottomMargin = "5px";   
        var textAlignment = "Left"; 
        var width;   
        var unit;    
        var isBold = false;   
        var isItalic = false; 
        var textDecoration;   
        var fontSize;       
        var fontFamily;     
        var fieldAlignment; 
        
        /************* updating InnerHTML/ text of text field **************/
        if ( selectedTextArea ) {      
            selectedText = selectedTextArea.getValue();      // fetching selected text.
        }
        if (selectedText === "" || selectedText === " ") {
            selectedText = space;                         //  if selected text is empty or a space is added, replacing that with a "&nbsp;". So that it renders properly in HTML.
        }
        selectedText = selectedText.replace(/\n/g,'<br>');      // replacing a return/"\n" with a line break <br>.
        selectedText = selectedText.replace(/\s\s/g,' '+space); // replacing a white space with a non breaking space (&nbsp;).
        if ( applyBulletCheckBox ) {   // checking for component  
            isBulletsApplied = applyBulletCheckBox.getValue()?applyBulletCheckBox.getValue():false;   // checking if bullets are appllied or not.
        }
        if ( bulletTypeCombo ) { // checking for component
            bulletType = bulletTypeCombo.getValue()?bulletTypeCombo.getValue():"none";  // fetching bullet type, if not present setting it to "none".
        }
        if ( isBulletsApplied ) {                        
            selectedText = addBullets(selectedText,bulletType);
        } else {
            selectedText =  remBullets(selectedText,bulletType);
        }
        textEle.isBulletsApplied = isBulletsApplied;  // adding bullet configuration to static text's object. Done, for creating json later.
        textEle.bulletType = bulletType;              // adding bullet type to static text's object. Done, for creating json later.
        setEXTComponentInnerHTML(textEle, selectedText) // replacing innerhtml with formatted text
        textEle.labelhtml = selectedText;             //  adding that text to obj of static text. Done, for creating json later.
        textEle.label = selectedText;                 //  -------------------------,,-------------------
        
        /******** updating text Color ********/
        if ( textColorPicker ) {       
            selectedTextColor = textColorPicker.value;         // fetching color 
        }
        setEXTComponentStyle(textEle, "color", selectedTextColor);   // setting color to text
        textEle.textcolor = selectedTextColor;                       //  adding color to text field object. Done for creating json later
        
        /************* updating Margins ***************************/
        if ( topMarginNumberField ) {      
            topMargin = topMarginNumberField.getValue();     // fetching the value of top margin.
        }
        if ( leftMarginNumberField ) {     
            leftMargin = leftMarginNumberField.getValue();   // fetching the value of Left Margin.
        }
        if ( rightMarginNumberField ) {     
            rightMargin = rightMarginNumberField.getValue(); // fetching the value of right Margin. 
        }
        if ( bottomMarginNumberField) {    
            bottomMargin = bottomMarginNumberField.getValue(); // fetching the value of bottom margin.
        }
        setEXTComponentStyle(textEle, "marginTop", topMargin + "px");       // updating top margin
        setEXTComponentStyle(textEle, "marginLeft", leftMargin + "px");     // updating left margin
        setEXTComponentStyle(textEle, "marginRight", rightMargin + "px");   // updating right margin
        setEXTComponentStyle(textEle, "marginBottom", bottomMargin + "px")  // updating bottom margin
        textEle.marginTop = topMargin;          // updating margin in text field obj . Done for creating json later 
        textEle.marginBottom = bottomMargin;    //---------------------------,,------------------------------
        textEle.marginRight = rightMargin;      //---------------------------,,------------------------------
        textEle.marginLeft = leftMargin;        //---------------------------,,------------------------------
        
        /********************* updating text Alignment *****************************/
        if ( textAlignCombo ) {     
            textAlignment = textAlignCombo.getRawValue();   // fetching the value of text Alignment.
        }    
        if (textAlignment == 'Left') {                     // if value is left add css class for left and removing for right and middle.
            textEle.removeCls('classTextAligment_center');
            textEle.removeCls('classTextAligment_right');
            textEle.addClass('classTextAligment_left');
            textEle.textalignclass = "classTextAligment_left";
        } else if (textAlignment == 'Center') {            // if value is center add css class for center and removing for right and left.
            textEle.removeCls('classTextAligment_left');
            textEle.removeCls('classTextAligment_right');
            textEle.addClass('classTextAligment_center');
            textEle.textalignclass = "classTextAligment_center";
        } else if (textAlignment == 'Right') {             // if value is right add css class for right and removing for left and middle.
            textEle.removeCls('classTextAligment_left');
            textEle.removeCls('classTextAligment_center');
            textEle.addClass('classTextAligment_right');
            textEle.textalignclass = "classTextAligment_right";
        }

       /**************** updating width ********************************************/
        if ( widthNumberField ) {     
            width = widthNumberField.getValue();      // fetching width
        }
        if ( unitCombo ) {     
            unit =  unitCombo.getRawValue();          // fetching unit
        }
        if ( width > 0) {                     // checking if width is greater than zero.
            if ( unit == "px") {                      // if unit is px then directly setting the width with unit as pixels 
                textEle.elementwidth = width;
                textEle.unit = "px";
                setEXTComponentStyle( textEle, "width", width+unit );
            } else if ( unit == "%" ) {              // if unit is % then width is only set when it is less than 100 with unit as %.     
                textEle.elementwidth = width;
                textEle.unit = "%";
                if ( width <= 100 ) {
                    setEXTComponentStyle( textEle, "width", width+unit );
                }
            }
        }
        
        /****************************** Updating  font weight ******************/    
        if ( boldCheckBox ) {     // Checking for Component
            isBold = boldCheckBox.getValue();
        }    
        if (isBold == true) {     
            setEXTComponentStyle(textEle, "fontWeight", "bold");    // Setting weight as bold
            textEle.bold = "true";
        } else {
            setEXTComponentStyle(textEle, "fontWeight", "normal");  //  setting weight as normal
            textEle.bold = "false";
        }
        
        /******************* updating font style **************************/
        if ( italicCheckBox ) {     // Checking for Component
            isItalic = italicCheckBox.getValue();
        }
        if (isItalic == true) {
            setEXTComponentStyle(textEle, "fontStyle", "italic");
            textEle.italic = "true";
        } else {
            setEXTComponentStyle(textEle, "fontStyle", "normal");
            textEle.italic = "false";
        }
        
        /********************** updating Text Decoration *************************/ 
        if( textLineCombo ) {    // Checking for Component
           textLine =  textLineCombo.getRawValue();
        }
        if (textLine == "Overline") {
            setEXTComponentStyle(textEle, "textDecoration", "overline");
            textEle.textline = "Overline";
        } else if (textLine == "Line-through") {
            setEXTComponentStyle(textEle, "textDecoration", "line-through");
            textEle.textline = "Line-through";
        } else if (textLine == "Underline") {
            setEXTComponentStyle(textEle, "textDecoration", "underline");
            textEle.textline = "Underline";
        } else {
            setEXTComponentStyle(textEle, "textDecoration", "");
            textEle.textline = "None";
        }

        /************************ updating font size *************************/
        if( fontSizeNumberField ) {     // Checking for Component
            fontSize = fontSizeNumberField.getValue();
        }
        if (fontSize > 0)
        {
            setEXTComponentStyle(textEle, "fontSize", fontSize + "px");  // setting font size
            textEle.fontsize = fontSize + "px";
        } else {
            setEXTComponentStyle(textEle, "fontSize", "");
            textEle.fontsize = "";
        }
        
        /*************************** updating font family ***************************/
        if ( fontCombo ) {      // Checking for Component
            fontFamily = fontCombo.getValue();
        }
        setEXTComponentStyle(textEle, "fontFamily", fontFamily);    // setting font family
        textEle.fontfamily = fontFamily;
        
        /************************* updating field alignment ***********************/
        if ( fieldAlignmentCombo ) {       // Checking for Component
            fieldAlignment = fieldAlignmentCombo.getValue(); // fetching feild alignment value
        }
        if(fieldAlignment ===  "2"){     // adding inline css class and removing block class
            textEle.addCls("sectionclass_field_inline"); 
            textEle.removeCls("sectionclass_field_block");
        } else {                          // // adding block css class and removing inline class
            textEle.removeCls("sectionclass_field_inline");
            textEle.addCls("sectionclass_field_block");
        }
        textEle.fieldAlignment=fieldAlignment;
        textEle.iselementlevel=true;
        
        getEXTComponent("idMainPanel").doLayout();        
        
    } else if ( fieldType === Ext.fieldID.insertField ) {
        
        var obj = ele;
            
        Ext.getCmp('idTextAlign').setDisabled(false);
        Ext.getCmp('idColonAlign').setDisabled(false);
             
        //Width Component
        Ext.getCmp('idUnit').setDisabled(false);
        Ext.getCmp('idWidth').setDisabled(false);
        var width =  Ext.getCmp('idWidth').getValue();
        if (Ext.getCmp('idWidth').getValue() > 0 && Ext.getCmp('idUnit').getRawValue() == "px") {

            Ext.getCmp(selectedElement).elementwidth = width;
            Ext.getCmp(selectedElement).unit = "px";
            document.getElementById(selectedElement).style.width = Ext.getCmp('idWidth').getValue() + Ext.getCmp('idUnit').getRawValue();
        }
        if (Ext.getCmp('idWidth').getValue() > 0 && Ext.getCmp('idUnit').getRawValue() == "%") {
            
            Ext.getCmp(selectedElement).elementwidth = width;
            Ext.getCmp(selectedElement).unit = "%";
            if ( width <= 100 ) {
                document.getElementById(selectedElement).style.width = Ext.getCmp("idWidth").getValue() + Ext.getCmp('idUnit').getRawValue();
            }
        }
        if (Ext.getCmp("iddefaultfield")) {
            Ext.getCmp(selectedElement).defaultValue = Ext.getCmp("iddefaultfield").getValue().replace(/\s/g,space);
        }
        
        var selectednewcolor = Ext.getCmp('colorpicker').value;
        Ext.getCmp(selectedElement).getEl().setStyle('color', selectednewcolor);
        Ext.getCmp(selectedElement).textcolor = selectednewcolor;
        var borderColor = Ext.getCmp('selectbordercolorpicker').value;
        Ext.getCmp(selectedElement).getEl().setStyle('border-color', borderColor);
        Ext.getCmp(selectedElement).borderColor = borderColor;
//        document.getElementById(selectedElement).style.width = Ext.getCmp("idWidth").getValue() + Ext.getCmp('idUnit').getRawValue();
//        ele.unit = Ext.getCmp('idUnit').getRawValue();
//        ele.width=Ext.getCmp("idWidth").getValue() + Ext.getCmp('idUnit').getRawValue();
//        ele.elementwidth = Ext.getCmp("idWidth").getValue();
        //}
            
        //            //Height component
        //            Ext.getCmp('idHeight').setDisabled(false);
        //            if (Ext.getCmp('idHeight').getValue() > 0 && Ext.getCmp('idUnit').getRawValue() == "%") {
        //                var tmpHeight = 0;
        //                if (Ext.getCmp('idHeight').getValue() == "100")
        //                    tmpHeight = parseFloat("1");
        //                else
        //                    tmpHeight = parseFloat("0." + Ext.getCmp('idHeight').getValue());
//            
        //                tmpHeight = Ext.getCmp(selectedElement).ownerCt.getHeight() * tmpHeight;
        //                Ext.getCmp(selectedElement).setHeight(tmpHeight);
        //                Ext.getCmp(selectedElement).elementheight =Ext.getCmp("idHeight").getValue();
        //                Ext.getCmp(selectedElement).unit = "%";
        //            }else{
        //                Ext.getCmp(selectedElement).setHeight(Ext.getCmp("idHeight").getValue());
        //                Ext.getCmp(selectedElement).unit = "px";
//            }

        /****
             *Text Align
             *****/
            
        var textLine;
        if (Ext.getCmp("idTextLine")) {
            textLine = Ext.getCmp("idTextLine").getValue();
            Ext.getCmp(selectedElement).getEl().dom.style.textDecoration = textLine;
            Ext.getCmp(selectedElement).textDecoration = textLine;
        }
        if (Ext.getCmp("idBoldText").getValue() == true) {
            Ext.getCmp(selectedElement).getEl().setStyle('font-weight', 'bold');
            Ext.getCmp(selectedElement).bold = true;
        }
        else {
            Ext.getCmp(selectedElement).getEl().setStyle('font-weight', 'normal');
            Ext.getCmp(selectedElement).bold = false;
        }
        
        if (Ext.getCmp("idItalicText").getValue() == true) {
            Ext.getCmp(selectedElement).getEl().setStyle('font-style', 'italic');
            Ext.getCmp(selectedElement).italic = true;
        } else {
            Ext.getCmp(selectedElement).getEl().setStyle('font-style', 'normal');
            Ext.getCmp(selectedElement).italic = false;
        }
        var margintop =  Ext.getCmp("idtopmargin").getValue();
        var marginleft =  Ext.getCmp("idleftmargin").getValue();
        var marginright =  Ext.getCmp("idrightmargin").getValue();
        var marginbottom =  Ext.getCmp("idbottommargin").getValue();
        Ext.getCmp(selectedElement).getEl().dom.style.marginTop = margintop + "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginBottom = marginbottom+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginRight = marginright+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginLeft = marginleft + "px";
        Ext.getCmp(selectedElement).marginTop = margintop;
        Ext.getCmp(selectedElement).marginBottom = marginbottom;
        Ext.getCmp(selectedElement).marginRight = marginright;
        Ext.getCmp(selectedElement).marginLeft = marginleft;
        
        var rec = searchRecord(defaultFieldGlobalStore, Ext.getCmp('idcustomdesignerfieldselectcombo').getValue(), 'id');
        if (rec) {
            var label = rec.data.label;
            var checklabel = Ext.getCmp('idcustomdesignerfieldselectcombo').getValue().toLowerCase();
            
             var labelHTML = "";
            if (Ext.getCmp('idcustomdesignerfieldselectcombo').getValue() == "PageNumberField") {
                labelHTML = "<span id=\"pagenumberspan\" attribute='{PLACEHOLDER:" + Ext.getCmp('idcustomdesignerfieldselectcombo').getValue() + "}'>#" + label + "#</span>";
            } else if(checklabel==Ext.FieldType.allgloballeveldimensionslabel || checklabel==Ext.FieldType.alllineleveldimensionslabel || checklabel==Ext.FieldType.allgloballevelcustomfieldslabel || checklabel==Ext.FieldType.alllinelevelcustomfieldslabel || checklabel==Ext.FieldType.alldimensionslabel){
                labelHTML = "<span style='display:table;width:100%;' attribute='{PLACEHOLDER:" + Ext.getCmp('idcustomdesignerfieldselectcombo').getValue() + "}'>#" + label + "#</span>";
            } else {
                labelHTML = "<span attribute='{PLACEHOLDER:" + Ext.getCmp('idcustomdesignerfieldselectcombo').getValue() + "}'>#" + label + "#</span>";
            }

            if(ele.isPreText){ //Check for is their PreText applied - (ERP-18751)
                ele.getEl().dom.children[1].outerHTML = labelHTML; //if PreText is applied then position of labelHTML (placeholder span) children is [1] - (ERP-18751)
            } else{
                ele.getEl().dom.children[0].outerHTML = labelHTML; //if PreText is not applied then position of labelHTML (placeholder span) children is [0] - (ERP-18751)
            }
            Ext.getCmp(selectedElement).label = "#" + label + "#";
            Ext.getCmp(selectedElement).labelhtml=labelHTML;
        }
        var textAlign = Ext.getCmp('idTextAlign').getRawValue();
        if (textAlign == 'Left') {
            ele.removeCls('classTextAligment_center');
            ele.removeCls('classTextAligment_right');
            ele.addClass('classTextAligment_left');
            Ext.getCmp(selectedElement).textalignclass = "classTextAligment_left";
        }
        else if (textAlign == 'Center') {
            ele.removeCls('classTextAligment_left');
            ele.removeCls('classTextAligment_right');
            ele.addClass('classTextAligment_center');
            Ext.getCmp(selectedElement).textalignclass = "classTextAligment_center";
        }
        else if (textAlign == 'Right') {
            ele.removeCls('classTextAligment_left');
            ele.removeCls('classTextAligment_center');
            ele.addClass('classTextAligment_right');
            Ext.getCmp(selectedElement).textalignclass = "classTextAligment_right";
        }

        if (Ext.getCmp('idFontSize').getValue() > 0)
        {
            var tmpFontSize = Ext.getCmp('idFontSize').getValue() + 'px';
            Ext.getCmp(selectedElement).getEl().el.setStyle('font-size', tmpFontSize);
            // Loop for applying font size to PreText, Select Field and PostText  // ERP-19417
            for(var cnt = 0; cnt < Ext.getCmp(selectedElement).getEl().dom.children.length; cnt++){
                Ext.getCmp(selectedElement).getEl().dom.children[cnt].style.fontSize=tmpFontSize;
            }            
            Ext.getCmp(selectedElement).fontsize = tmpFontSize;
        } else {
            Ext.getCmp(selectedElement).getEl().el.setStyle('font-size', "");
            // Loop for applying font size to PreText, Select Field and PostText  // ERP-19417
            for(var cnt = 0; cnt < Ext.getCmp(selectedElement).getEl().dom.children.length; cnt++){
                Ext.getCmp(selectedElement).getEl().dom.children[cnt].style.fontSize="";
            } 
            Ext.getCmp(selectedElement).fontsize = "";
        }
        if (Ext.getCmp("pagefontid")) {
             var tmpFontFamily = Ext.getCmp('pagefontid').getValue();
            Ext.getCmp(selectedElement).getEl().setStyle('font-family', tmpFontFamily);
            Ext.getCmp(selectedElement).fontfamily = tmpFontFamily;
        }
        var alignment_type = Ext.getCmp("fields_Alignment").getValue();
        if(alignment_type== "2"){
            ele.addCls("sectionclass_field_inline");
            ele.removeCls("sectionclass_field_block");
        } else {
            ele.removeCls("sectionclass_field_inline");
            ele.addCls("sectionclass_field_block");
        }
        if(Ext.getCmp('decimalprecisionid')){
            ele.decimalPrecision = Ext.getCmp('decimalprecisionid').getValue();
            if(ele.isFormula){
                if(Ext.getCmp("idpreText").checked && !Ext.getCmp("idpostText").checked){
                    ele.getEl().dom.children[1].attributes.decimalprecision.value = ele.decimalPrecision;
                } else if(!Ext.getCmp("idpreText").checked && Ext.getCmp("idpostText").checked){
                    ele.getEl().dom.children[0].attributes.decimalprecision.value = ele.decimalPrecision;
                } else if(Ext.getCmp("idpreText").checked && Ext.getCmp("idpostText").checked){
                    ele.getEl().dom.children[1].attributes.decimalprecision.value = ele.decimalPrecision;
                } else{
                    ele.getEl().dom.children[0].attributes.decimalprecision.value = ele.decimalPrecision;
                }
            }
        }
        if(Ext.getCmp('idvalSeparatorSelectField')){
            ele.valueSeparator = Ext.getCmp('idvalSeparatorSelectField').getValue();
        }
        if (Ext.getCmp("recordCurrencyId")) {
            ele.specificreccurrency = Ext.getCmp('recordCurrencyId').getValue();
        }
        if(Ext.getCmp('iddimensionSelectField')){
            ele.dimensionValue = Ext.getCmp('iddimensionSelectField').getValue();
        }
        if(Ext.getCmp('valuewithcommaid')){
            ele.valueWithComma = Ext.getCmp('valuewithcommaid').getValue();
        }
        
        
        ele.fieldAlignment=alignment_type;
        ele.iselementlevel=true;
                
        if ( Ext.getCmp("idpreTextFieldValue") ) {
            var pretextVal = Ext.getCmp("idpreTextFieldValue").getValue();
            pretextVal = pretextVal.replace(/\n/g,'<br>');  //replace \n to <br> tags
            pretextVal = pretextVal.replace(/\s\s/g, doubleSpace); //replace spaces to &nbsp;
            if ( ele.isPreText ) {
                ele.getEl().dom.children[0].innerHTML = pretextVal;
                ele.preTextValue = pretextVal;
            } else {
                ele.preTextValue = "";
            }
        }
        if ( Ext.getCmp("idpreTextwordSpacing") ) {
            var pretextspacing = Ext.getCmp("idpreTextwordSpacing").getValue();
            if ( ele.isPreText ) {
                ele.getEl().dom.children[0].style.marginRight = pretextspacing  + "px";
                ele.preTextWordSpacing = pretextspacing;
            } else {
                ele.preTextWordSpacing = 5;
            }
        }
        if ( Ext.getCmp("idpostTextFieldValue") ) {
            var posttextVal = Ext.getCmp("idpostTextFieldValue").getValue();
            posttextVal = posttextVal.replace(/\n/g,'<br>');  //replace \n to <br> tags
            posttextVal = posttextVal.replace(/\s\s/g, doubleSpace); //replace spaces to &nbsp;
            if ( ele.isPostText ) {
                if ( ele.isPreText ) {
                    ele.getEl().dom.children[2].innerHTML = posttextVal;
                    ele.postTextValue = posttextVal;
                } else  {
                    ele.getEl().dom.children[1].innerHTML = posttextVal;
                    ele.postTextValue = posttextVal;
                }
            } else {
                ele.postTextValue = "";
            }
        }
        if ( Ext.getCmp("idpostTextwordSpacing") ) {
            var posttextspacing = Ext.getCmp("idpostTextwordSpacing").getValue();
            if ( ele.isPostText ) {
                if ( ele.isPreText ) {
                    ele.getEl().dom.children[2].style.marginLeft = posttextspacing  + "px";
                    ele.postTextWordSpacing = posttextspacing;
                } else {
                    ele.getEl().dom.children[1].style.marginLeft = posttextspacing  + "px";
                    ele.postTextWordSpacing = posttextspacing;
                }
            } else {
                ele.postTextWordSpacing = 5;
            }
        }
        
        if ( Ext.getCmp("idpreTextBoldText") ) {
            var pretextbold = Ext.getCmp("idpreTextBoldText").getValue();
            if ( ele.isPreText ) {
                if (pretextbold) {
                    ele.getEl().dom.children[0].style.fontWeight = "bold";
                    ele.preTextbold = pretextbold;
                } else {
                    ele.getEl().dom.children[0].style.fontWeight = "normal";
                    ele.preTextbold= pretextbold;
                }
            } else {
                ele.preTextbold = "";
            }
        }
        if ( Ext.getCmp("idpostTextBoldText") ) {
            var posttextbold = Ext.getCmp("idpostTextBoldText").getValue();
            if ( ele.isPostText ) {
                if (ele.isPreText) {
                    if (posttextbold) {
                        ele.getEl().dom.children[2].style.fontWeight = "bold";
                        ele.postTextbold = posttextbold;
                    } else {
                        ele.getEl().dom.children[2].style.fontWeight = "normal";
                        ele.postTextbold = posttextbold;
                    }
                } else {
                    if (posttextbold) {
                        ele.getEl().dom.children[1].style.fontWeight = "bold";
                        ele.postTextbold = posttextbold;
                    } else {
                        ele.getEl().dom.children[1].style.fontWeight = "normal";
                        ele.postTextbold = posttextbold;
                    }
                }
            } else {
                ele.postTextbold = "";
            }
        }
        
        if ( Ext.getCmp("idPreTextItalicText") ) {
            var pretextitalic = Ext.getCmp("idPreTextItalicText").getValue();
            if ( ele.isPreText ) {
                if (pretextitalic) {
                    ele.getEl().dom.children[0].style.fontStyle = "italic";
                    ele.preTextItalic = pretextitalic;
                } else {
                    ele.getEl().dom.children[0].style.fontStyle = "normal";
                    ele.preTextItalic= pretextitalic;
                }
            } else {
                ele.preTextItalic = "";
            }
        }
        if ( Ext.getCmp("idPostTextItalicText") ) {
            var posttextitalic = Ext.getCmp("idPostTextItalicText").getValue();
            if ( ele.isPostText ) {
                if (ele.isPreText) {
                    if (posttextitalic) {
                        ele.getEl().dom.children[2].style.fontStyle = "italic";
                        ele.postTextItalic = posttextitalic;
                    } else {
                        ele.getEl().dom.children[2].style.fontStyle = "normal";
                        ele.postTextItalic = posttextitalic;
                    }
                } else {
                    if (posttextitalic) {
                        ele.getEl().dom.children[1].style.fontStyle = "italic";
                        ele.postTextItalic = posttextitalic;
                    } else {
                        ele.getEl().dom.children[1].style.fontStyle = "normal";
                        ele.postTextItalic = posttextitalic;
                    }
                }
            } else {
                ele.postTextItalic = "";
            }
        }
        
        Ext.getCmp("idMainPanel").doLayout();
            
            
    } else if (fieldType == Ext.fieldID.insertImage) {
        var imgEle = Ext.getCmp(selectedElement);
        // Field Alignment (inline, block)
        var alignment_type = Ext.getCmp("fields_Alignment").getValue();
        // Margins
        var margintop =  Ext.getCmp("idtopmargin").getValue();
        var marginleft =  Ext.getCmp("idleftmargin").getValue();
        var marginright =  Ext.getCmp("idrightmargin").getValue();
        var marginbottom =  Ext.getCmp("idbottommargin").getValue();
        // Text/Image Alignment (center, left , rigth)
        var textAlign = Ext.getCmp('idTextAlign').getRawValue();
        // Height, width, unit of width
        var height;
        var width;
        var unit;
        if ( Ext.getCmp('idHeight') ) {
            height = Ext.getCmp('idHeight').getValue();
        }
        if ( Ext.getCmp('idWidth') ) {
            width = Ext.getCmp('idWidth').getValue();
        }
        if ( Ext.getCmp('idUnit') ) {
            unit = Ext.getCmp('idUnit').getValue();
        }
        var element = imgEle.getEl().dom.children[0]; // image
        
        
        if ( alignment_type === "2" ) {  // Inline 
            element = imgEle.getEl().dom; // component
            
            element.style.marginLeft = marginleft + "px";
            element.style.marginRight = marginright + "px"; 
            element.children[0].style.margin = "0px";
            
            imgEle.addCls("image_auto_width_new_inlinecls");
            imgEle.removeCls("image_auto_width_new_blockcls");
            
            
            
        } else {
            if ( textAlign === "Left" ) {
                element.style.marginRight = "auto";
                element.style.marginLeft = "0px";
                imgEle.textalignclass = "imageshiftleft";
            } else if ( textAlign === "Center" ) {
                element.style.marginRight = "auto";
                element.style.marginLeft = "auto";
                imgEle.textalignclass = "imageshiftcenter";
            } else if ( textAlign === "Right" ) {
                element.style.marginRight = "0px";
                element.style.marginLeft = "auto";
                imgEle.textalignclass = "imageshiftright";
            }       
            imgEle.addCls("image_auto_width_new_blockcls");
            imgEle.removeCls("image_auto_width_new_inlinecls");
//            element.parentNode.style.margin = "0px";
        }
        element.style.marginTop = margintop + "px";
        element.style.marginBottom = marginbottom + "px";
        
        if ( width ) {
            if ( width.toLowerCase()  === "auto") {
                width = width.toLowerCase();
                element.style.width = width;
            } else {
                element.style.width = width + unit;
            }
            imgEle.width = width;
            imgEle.elementwidth = width;
        } else {
            element.style.width = "auto";
            imgEle.width = "auto";
            imgEle.elementwidth = "auto";
        }
        imgEle.unit = unit;
        
        if ( height ) {
            if (height.toLowerCase() === "auto" ) {
                height = height.toLowerCase();
                if (element.nodeName === "IMG" ) {
                    element.style.height = height;
                } else {
                    element.children[0].style.height = height;
                }
            } else {
                if (element.nodeName === "IMG" ) {
                    element.style.height = height + "px";
                } else {
                    element.children[0].style.height = height + "px";
                }
            }
            imgEle.elementheight = height;
            imgEle.height = height;
        } else {
            element.style.height = "auto";
            if (element.nodeName === "IMG" ) {
                element.style.height = "auto";
            } else {
                element.children[0].style.height = "auto";
            }
            imgEle.height = "auto";
            imgEle.elementheight= "auto";
        }
        imgEle.marginTop = margintop;
        imgEle.marginBottom = marginbottom;
        imgEle.marginRight = marginright;
        imgEle.marginLeft = marginleft;
        imgEle.fieldAlign=alignment_type;
        
        
//        var obj = ele;
//            
//        var textAlign = Ext.getCmp('idTextAlign').getRawValue();
//        //Width Component
//        Ext.getCmp('idUnit').setDisabled(false);
//        Ext.getCmp('idWidth').setDisabled(false);
//        if (Ext.getCmp('idWidth').getValue() > 0 && Ext.getCmp('idUnit').getRawValue() == "%") {
////            var tmpWidth = 0;
////            if (Ext.getCmp('idWidth').getValue() == "100")
////                tmpWidth = parseFloat("1");
////            else
////                tmpWidth = parseFloat("0." + Ext.getCmp('idWidth').getValue());
////            tmpWidth = Ext.getCmp(selectedElement).ownerCt.getWidth() * tmpWidth;
//            Ext.getCmp(selectedElement).setWidth(Ext.getCmp('idWidth').getValue() + "%");
//            if(alignment_type== "2"){
//                Ext.getCmp(selectedElement).getEl().dom.style.width = Ext.getCmp('idWidth').getValue() + "%";
//                Ext.getCmp(selectedElement).getEl().dom.children[0].style.width = "100% !important";
//            }else{
//                Ext.getCmp(selectedElement).getEl().dom.children[0].style.width = Ext.getCmp('idWidth').getValue() + "%";
//            }
//            Ext.getCmp(selectedElement).elementwidth =Ext.getCmp('idWidth').getValue();
//            Ext.getCmp(selectedElement).unit = "%";
//        }else{
//            if(Ext.getCmp('idWidth') && Ext.getCmp('idWidth').getValue()!=null){
//                var width = Ext.getCmp('idWidth').getValue().toLowerCase();
//                
//                Ext.getCmp(selectedElement).setWidth(width?width:"auto");
//                if(alignment_type== "2"){
//                    Ext.getCmp(selectedElement).getEl().dom.style.width = width?(width!="auto")?width+"px":"auto":"auto";
//                    Ext.getCmp(selectedElement).getEl().dom.children[0].style.width = "100% !important";
//                }else{
//                    Ext.getCmp(selectedElement).getEl().dom.children[0].style.width = width?(width!="auto")?width+"px":"auto":"auto";
//                }
//                Ext.getCmp(selectedElement).width = width?width:"auto";
//                Ext.getCmp(selectedElement).elementwidth = width?width:"auto";
//                Ext.getCmp(selectedElement).unit = "px";
//            }
//        }
//        if(Ext.getCmp('idWidth').getValue().toLowerCase()== "auto" || Ext.getCmp('idWidth').getValue()== ""){
//            if(alignment_type== "2"){
//                ele.removeCls("imagesectionclass_field_inline");
//                ele.addCls("image_auto_width_inlinecls");
//            } else {
//                ele.removeCls("imagesectionclass_field_block");
//                ele.addCls("image_auto_width_blockcls");
//            }
//        } else{
//            if(alignment_type== "2"){
//                ele.removeCls("image_auto_width_inlinecls");
//                ele.addCls("imagesectionclass_field_inline");
//            } else {
//                ele.removeCls("image_auto_width_blockcls");
//                ele.addCls("imagesectionclass_field_block");
//            }
//        }
//        var margintop =  Ext.getCmp("idtopmargin").getValue();
//        var marginleft =  Ext.getCmp("idleftmargin").getValue();
//        var marginright =  Ext.getCmp("idrightmargin").getValue();
//        var marginbottom =  Ext.getCmp("idbottommargin").getValue();
//        Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginTop = margintop + "px";
//        Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginBottom = marginbottom+ "px";
//        Ext.getCmp(selectedElement).getEl().dom.style.marginRight ="0px";
//        Ext.getCmp(selectedElement).getEl().dom.style.marginLeft ="0px";
//        if ( alignment_type == "2") {
//            Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginTop = "0px";
//            Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginBottom ="0px";
//            Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginRight = "0px";
//            Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginLeft ="0px";
//            Ext.getCmp(selectedElement).getEl().dom.style.marginRight = marginright+ "px";
//            Ext.getCmp(selectedElement).getEl().dom.style.marginLeft = marginleft + "px";
//            Ext.getCmp(selectedElement).getEl().dom.style.marginTop = margintop+ "px";
//            Ext.getCmp(selectedElement).getEl().dom.style.marginBottom = marginbottom + "px";
//        }
//        Ext.getCmp(selectedElement).marginTop = margintop;
//        Ext.getCmp(selectedElement).marginBottom = marginbottom;
//        Ext.getCmp(selectedElement).marginRight = marginright;
//        Ext.getCmp(selectedElement).marginLeft = marginleft;
//        //Height component
//        Ext.getCmp('idHeight').setDisabled(false);
////        if (Ext.getCmp('idHeight').getValue() > 0 && Ext.getCmp('idUnit').getRawValue() == "%") {
////            var tmpHeight = 0;
////            if (Ext.getCmp('idHeight').getValue() == "100")
////                tmpHeight = parseFloat("1");
////            else
////                tmpHeight = parseFloat("0." + Ext.getCmp('idHeight').getValue());
////            
////            tmpHeight = Ext.getCmp(selectedElement).ownerCt.getHeight() * tmpHeight;
////            Ext.getCmp(selectedElement).setHeight(tmpHeight);
////            Ext.getCmp(selectedElement).elementheight =Ext.getCmp("idHeight").getValue();
////            Ext.getCmp(selectedElement).unit = "%";
////        }else{
//            if(Ext.getCmp('idHeight') && Ext.getCmp('idHeight').getValue()!=null){
//                var height = Ext.getCmp('idHeight').getValue().toLowerCase();
//            
//                Ext.getCmp(selectedElement).setHeight(height?height:"auto");
//                Ext.getCmp(selectedElement).getEl().dom.children[0].style.height = height?(height !="auto")?height+"px":"auto":"auto";
//                Ext.getCmp(selectedElement).elementheight = height?height:"auto";
//                Ext.getCmp(selectedElement).height = height?height:"auto";
//            }
////        }
//            
//        /****
//             *Text Align
//             *****/
//        //            var parent = Ext.getCmp(selectedElement).ownerCt;
//        //            var colCount = parent.items.length;
//            
//        if ( alignment_type == "1" ) {
//            if (textAlign == 'Left') {
//                //            ele.removeCls('imageshiftright ');
//                //            ele.removeCls('imageshiftcenter');
//                //            ele.addClass('imageshiftleft');
//                Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginRight = "auto";
//                Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginLeft = "0px";
//                Ext.getCmp(selectedElement).textalignclass = "imageshiftleft";
//            }
//            else if (textAlign == 'Center') {
//                //            ele.removeCls('imageshiftright ');
//                //            ele.removeCls('imageshiftleft');
//                //            ele.addClass('imageshiftcenter');
//                Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginRight = "auto";
//                Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginLeft = "auto";
//                Ext.getCmp(selectedElement).textalignclass = "imageshiftcenter";
//            }
//            else if (textAlign == 'Right') {
//                //            ele.removeCls('imageshiftleft ');
//                //            ele.removeCls('imageshiftcenter');
//                //            ele.addClass('imageshiftright');
//                Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginRight = "0px";
//                Ext.getCmp(selectedElement).getEl().dom.children[0].style.marginLeft = "auto";
//                Ext.getCmp(selectedElement).textalignclass = "imageshiftright";
//            }
//        }
//        if(alignment_type== "2"){
//            ele.addCls("imagesectionclass_field_inline");
//            ele.removeCls("imagesectionclass_field_block");
//        } else {
//            ele.removeCls("imagesectionclass_field_inline");
//            ele.addCls("imagesectionclass_field_block");
//        }
//        ele.fieldAlignment=alignment_type;
//        ele.iselementlevel=true,
        Ext.getCmp("idMainPanel").doLayout();
    //            parent.doLayout();
    } else if (fieldType == Ext.fieldID.insertTable) {
        var obj = ele;
        var selectednewcolor = Ext.getCmp('idbackgroundcolorpicker').value;
        Ext.get('itemlistconfigsectionPanelGrid').dom.children[1].rows[0].bgColor = selectednewcolor;
        obj.bordercolor = selectednewcolor;
        var table=  obj.el.dom.children[0];
        var align=  Ext.getCmp("allignLineTableCombo").getValue();
        var cells=table.children[1].children[0].cells;
        if (Ext.getCmp('idWidth').getValue() > 0) {
           var width = Ext.getCmp('idWidth').getValue();
           table.style.width = width+"%";
           obj.tablewidth = width;
        }
//        if(align== 'left'  || align === 0){
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].align="left";
//                    cells[i].style.textAlign="left";
//                cells[i].style.paddingLeft="4px";
//            }
//                obj.align = 0;
//        } else if(align=='center' || align == 1){
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].align="center";
//                    cells[i].style.textAlign="center";
//                }
//                obj.align = 1;
//        } else{
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].align="right";
//                cells[i].style.paddingRight="4px";
//                    cells[i].style.textAlign="right";
//                }
//                obj.align = 2;
//        }
//        if (Ext.getCmp("boldTextLineItemId").getValue() == true) {
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].style.fontWeight="bold";
//            }
//            Ext.getCmp(selectedElement).bold = "true";
//        }
//        else {
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].style.fontWeight="normal";
//            }
//            Ext.getCmp(selectedElement).bold = "false";
//        }
//
//        if (Ext.getCmp("italicTextLineItemId").getValue() == true) {
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].style.fontStyle="italic";
//            }
//            Ext.getCmp(selectedElement).italic = "true";
//        } else {
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].style.fontStyle="normal";
//            }
//            Ext.getCmp(selectedElement).italic = "false";
//        }
//        
//        if (Ext.getCmp("underLineTextLineItemId").getValue() == true) {
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].style.textDecoration="underline";
//            }
//            Ext.getCmp(selectedElement).underline = "true";
//        }
//        else {
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].style.textDecoration="";
//            }
//            Ext.getCmp(selectedElement).underline = "false";
//        }
//        
//        if (Ext.getCmp('fontSizeLineItemId').getValue() > 0)
//        {
//            var tmpFontSize = Ext.getCmp('fontSizeLineItemId').getValue() + 'px';
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].style.fontSize=tmpFontSize;
//            }
//            Ext.getCmp(selectedElement).fontsize = Ext.getCmp('fontSizeLineItemId').getValue();
//        } else {
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].style.fontSize="";
//            }
//            Ext.getCmp(selectedElement).fontsize = "";
//        }
//       
//        var tmpFontFamily = Ext.getCmp('pageFontLineItemId').getValue();
//            for( var i=0; i< cells.length; i++ ){
//                cells[i].style.fontFamily=tmpFontFamily;
//            }
//            Ext.getCmp(selectedElement).fontfamily = Ext.getCmp('pageFontLineItemId').getValue();
        
        var margintop =  Ext.getCmp("idtopmargin").getValue();
        var marginleft =  Ext.getCmp("idleftmargin").getValue();
        var marginright =  Ext.getCmp("idrightmargin").getValue();
        var marginbottom =  Ext.getCmp("idbottommargin").getValue();
        Ext.getCmp(selectedElement).getEl().dom.style.marginTop = margintop + "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginBottom = marginbottom+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginRight = marginright+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginLeft = marginleft + "px";
        Ext.getCmp(selectedElement).marginTop = margintop;
        Ext.getCmp(selectedElement).marginBottom = marginbottom;
        Ext.getCmp(selectedElement).marginRight = marginright;
        Ext.getCmp(selectedElement).marginLeft = marginleft;
        
        var sortfield = getEXTComponent("sortfieldid").getValue();
        var sortfieldxtype = "";
        if(sortfield !== "")
             sortfieldxtype = getEXTComponent("sortfieldid").getStore().findRecord("id",sortfield).data.xtype;
        getEXTComponent(selectedElement).sortfield = sortfield;
        getEXTComponent(selectedElement).sortfieldxtype = sortfieldxtype;
        
        var sortorder = getEXTComponent("sortorderid").getValue();
        getEXTComponent(selectedElement).sortorder = sortorder;
        
        var includeProductCategory = Ext.getCmp("includeProductCategoryId").getValue();
        Ext.getCmp(selectedElement).includeProductCategory = includeProductCategory;
        
        var includegroupingRowAfterId = Ext.getCmp("includegroupingRowAfterId").getValue();
        Ext.getCmp(selectedElement).includegroupingRowAfterId = includegroupingRowAfterId;
        

    } else if (fieldType == Ext.fieldID.insertRowPanel ) {
        setColumns(null,null,null,isMerging);
        
        var isPrePrinted = false;
        if(pagelayoutproperty[0].pagelayoutsettings){
            isPrePrinted = pagelayoutproperty[0].pagelayoutsettings.ispreprinted;
        }
        if(isPrePrinted){
            var rowHeight = getEXTComponent("idrowHeight").getValue();
//            var rowHeightPX = rowHeight;
//            var rowHeightPX = cm2px(rowHeight); // in CM
            var rowHeightCM = rowHeight; // in CM
            var colLength = getEXTComponent(selectedElement).items.items.length;
//            getEXTComponent(selectedElement).rowHeight = rowHeight;
            getEXTComponent(selectedElement).rowHeight = rowHeightCM;
            for(var len = 0 ; len < colLength; len++){
//                getEXTComponent(selectedElement).items.items[len].el.dom.children[0].children[0].children[0].style.setProperty("height",rowHeightPX + "px","important");
                getEXTComponent(selectedElement).items.items[len].el.dom.children[0].children[0].children[0].style.setProperty("height",rowHeightCM + "cm","important");
                getEXTComponent(selectedElement).items.items[len].el.dom.children[0].children[0].children[0].style.setProperty("overflow","hidden","");
            }
        }
        
    } else if (fieldType == Ext.fieldID.insertColumnPanel) {
        var type = Ext.getCmp("idType").getRawValue();
//        if (type == "Custom")
//        {
        Ext.getCmp(selectedElement).type = type;
        var width = Ext.getCmp("idWidth").getValue();
        var unit = Ext.getCmp("idUnit").getValue();
        var parent = Ext.getCmp(selectedElement).ownerCt;
        var colCount = parent.items.length;
        if (width < 10) {
            tmpwidth = parseFloat("0.0" + width);
        }
        else {
            tmpwidth = parseFloat("0." + width);
        }
                    
        if ( width == 100 ) {
            tmpwidth = 1.0;
        }
        Ext.getCmp(selectedElement).columnWidth = tmpwidth;
//        Ext.getCmp(selectedElement).addCls('sectionclass_element_'+width); 
        Ext.getCmp(selectedElement).columnwidth = width;
        var margintop =  Ext.getCmp("idtopmargin").getValue();
        var marginleft =  Ext.getCmp("idleftmargin").getValue();
        var marginright =  Ext.getCmp("idrightmargin").getValue();
        var marginbottom =  Ext.getCmp("idbottommargin").getValue();
        Ext.getCmp(selectedElement).getEl().dom.style.marginTop = margintop + "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginBottom = marginbottom+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginRight = marginright+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginLeft = marginleft + "px";
        Ext.getCmp(selectedElement).marginTop = margintop;
        Ext.getCmp(selectedElement).marginBottom = marginbottom;
        Ext.getCmp(selectedElement).marginRight = marginright;
        Ext.getCmp(selectedElement).marginLeft = marginleft;
        
    
        var alignment_type = Ext.getCmp("fields_Alignment").getValue();
        var ele1 = Ext.get(selectedElement+"-innerCt");
        if(alignment_type== "2"){
            ele1.removeCls('sectionclass_field_container');
            ele1.addCls('sectionclass_field_container_inline');
        } else {
            ele1.removeCls('sectionclass_field_container_inline');
            ele1.addCls('sectionclass_field_container');
        }
        /*If Block value is given to Column then the panel should also get updated*/
        var items=Ext.getCmp(selectedElement).items.items;
        for(var i=0;i<=items.length-1;i++){
            if(items[i].iselementlevel==false){//if the component is having fieldalignment
                items[i].fieldAlignment=alignment_type;
            }
        }
        Ext.getCmp(selectedElement).fieldalignment=Ext.getCmp("fields_Alignment").getValue();
            Ext.getCmp(selectedElement).borderedgetype=Ext.getCmp("idbordercornerstyle").getValue();
            
            
            
            var bordercorner = Ext.getCmp('idbordercornerstyle').getValue();
            if (bordercorner == 1 ) {
                ele1.removeCls('elementBorderRound');
            } else if ( bordercorner == 2 ) {
                ele1.addCls('elementBorderRound');
            }
        /*Border for section*/
            
        var borderChecked = Ext.getCmp("idbordercolor").getValue();
             Ext.getCmp(selectedElement).allowborder=borderChecked;
        if(borderChecked){
            // ele1.removeCls('sectionclass_field_container');
            ele1.addCls('elementBorder');
        } else {
            //ele1.removeCls('sectionclass_field_container_inline');
            ele1.removeCls('elementBorderRound');
            ele1.removeCls('elementBorder');
        }
            var selectednewcolor = Ext.getCmp('colorpicker').value;
             Ext.getCmp(selectedElement).backgroundColor=selectednewcolor;
            //selectednewcolor = "#"+selectednewcolor;
            if(selectednewcolor){
                ele1.setStyle('background',selectednewcolor);
            };
            selectednewcolor = Ext.getCmp('bordercolorpicker').value;
             Ext.getCmp(selectedElement).borderColor=selectednewcolor;
            //selectednewcolor = "#"+selectednewcolor;
            if(selectednewcolor){
                ele1.setStyle('border-color',selectednewcolor);
            };
            var linespacing = Ext.getCmp('idlinespacing').getValue();
            Ext.getCmp(selectedElement).linespacing=linespacing;
//            ele1.dom.style.marginTop = linespacing + "px";
//            ele1.dom.style.marginBottom = linespacing + "px";
            var children = ele1.dom.children.length;
            for ( var i =0; i<children; i++ ) {
//                ele1.dom.children[i].style.marginTop =linespacing + "px";
                if(i!=(children-1)) {
                    ele1.dom.children[i].style.marginBottom = linespacing + "px";
                    ele.items.items[i].marginBottom = linespacing;
                }
            }
            
//            Ext.getCmp(selectedElement).doLayout();
            
        } else if (fieldType == Ext.fieldID.insertGlobalTable) {
           
        var obj = ele;
        var table=  obj.getEl().dom.children[0];
        var tableheader = table.rows[0];
//        var unit = Ext.getCmp('globalTableUnitid').getRawValue();
        var width = Ext.getCmp('globalTableWidthid').getValue();
//        obj.unit = unit;
//        if (width > 0) {
////            if (unit == "px"){
////                table.width = width;
//                    
////            }
////            else
////            {
////                var tmpwidth = parseFloat('.' + width);
//                if ( width == 100) {
//                    table.width=obj.ownerCt.getWidth()
//                } else {
//                    table.width=obj.ownerCt.getWidth() * tmpwidth;
//                }
//            }
        if ( obj.fieldAlignment == "2" ) {
            table.width = "100%";
            obj.getEl().dom.style.width = width + "%";
        } else {
            obj.getEl().dom.style.width = "100%";
            table.width = width + "%";
        }
            Ext.getCmp(selectedElement).tableWidth = width;
            var bordercorner = Ext.getCmp('idbordercornerstyle').getValue();
            Ext.getCmp(selectedElement).borderedgetype=bordercorner;
            var rows = table.rows.length;
            var cellsfirstrow = table.rows[0].cells.length;
            var cellslastrow = table.rows[rows-1].cells.length;
            if (bordercorner == 1 ) {
                table.style.borderRadius="0px"
                table.rows[0].cells[0].style.borderTopLeftRadius="0px";
                table.rows[0].cells[cellsfirstrow-1].style.borderTopRightRadius="0px";
                table.rows[rows-1].cells[0].style.borderBottomLeftRadius="0px";
                table.rows[rows-1].cells[cellslastrow-1].style.borderBottomRightRadius="0px";
            } else if ( bordercorner == 2 ) {
                table.style.borderRadius="10px"
                table.rows[0].cells[0].style.borderTopLeftRadius="10px";
                table.rows[0].cells[cellsfirstrow-1].style.borderTopRightRadius="10px";
                table.rows[rows-1].cells[0].style.borderBottomLeftRadius="10px";
                table.rows[rows-1].cells[cellslastrow-1].style.borderBottomRightRadius="10px";
            }
            var rowspacing = Ext.getCmp('idrowspacing').getValue();
            Ext.getCmp(selectedElement).rowspacing=rowspacing;
            for (var i=0; i<rows; i++) {
                var cells = table.rows[i].cells.length;
                for (var j=0; j<cells;j++) {
                    table.rows[i].cells[j].style.paddingTop=rowspacing+"px";
                    table.rows[i].cells[j].style.paddingBottom=rowspacing+"px";
                }
            }
            var columnspacing = Ext.getCmp('idcolumnspacing').getValue();
            Ext.getCmp(selectedElement).columnspacing=columnspacing;
            for (var i=0; i<rows; i++) {
                var cells = table.rows[i].cells.length;
                for (var j=0; j<cells;j++) {
                    table.rows[i].cells[j].style.paddingLeft=columnspacing+"px";
                    table.rows[i].cells[j].style.paddingRight=columnspacing+"px";
                }
            }
            
        var margintop =  Ext.getCmp("idtopmargin").getValue();
        var marginleft =  Ext.getCmp("idleftmargin").getValue();
        var marginright =  Ext.getCmp("idrightmargin").getValue();
        var marginbottom =  Ext.getCmp("idbottommargin").getValue();
        Ext.getCmp(selectedElement).getEl().dom.style.marginTop = margintop + "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginBottom = marginbottom+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginRight = marginright+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginLeft = marginleft + "px";
        Ext.getCmp(selectedElement).marginTop = margintop;
        Ext.getCmp(selectedElement).marginBottom = marginbottom;
        Ext.getCmp(selectedElement).marginRight = marginright;
        Ext.getCmp(selectedElement).marginLeft = marginleft;
            
        
        
        var selectednewcolor = Ext.getCmp('colorpicker').value;
        Ext.getCmp(selectedElement).borderColor = selectednewcolor;
        if(selectednewcolor){
            table.style.borderColor=selectednewcolor;
        }
        selectednewcolor = Ext.getCmp('idbackgroundcolorpicker').value;
        Ext.getCmp(selectedElement).backgroundColor = selectednewcolor;
        if ( selectednewcolor ) {
            table.bgColor = selectednewcolor;
        }
        selectednewcolor = Ext.getCmp('idheaderbackgroundcolorpicker').value;
        Ext.getCmp(selectedElement).headerColor = selectednewcolor;
        if ( selectednewcolor ) {
            tableheader.bgColor = selectednewcolor;
        }
        var align=  Ext.getCmp("allignTableCombo").getValue();
        Ext.getCmp(selectedElement).tableAlign = align;
        if(align== 'left'  || align == 0){
            table.style.marginRight="auto";
            table.style.marginLeft="0px";
        } else if(align=='center' || align == 1){
            table.style.marginRight="auto";
            table.style.marginLeft="auto";
        } else{
            table.style.marginRight="0px";
            table.style.marginLeft="auto";
        }
            
        var fieldAlignment = Ext.getCmp('idfieldsAlignment').getValue();
        if ( fieldAlignment == "1" ) {
            obj.getEl().dom.style.display = "inline-block";
            obj.fieldAlignment = "1"
        } else if ( fieldAlignment == "2" ) {
            obj.getEl().dom.style.display = "inline-table";
            obj.fieldAlignment = "2"
        }
        if(obj.tableHeader){ //If Repeate row table with header then set display as 'block' for page break
            obj.getEl().dom.style.display = "block";
            obj.fieldAlignment = "1"
        }
        
        var isSummaryTable = Ext.getCmp("idsummarytablecheck").getValue();
        var summaryTableHeight = Ext.getCmp("idsummaryTableHeightCombo").getValue();
        if(isSummaryTable){
            table.id = "summaryTable";
            table.setAttribute("tableHeightType", summaryTableHeight);
            Ext.getCmp("idsummaryTableHeightCombo").show();
            Ext.getCmp("idsummaryTableHeightCombo").enable();
            obj.summaryTableHeight = summaryTableHeight;
        } else{
            table.id = "";
            table.removeAttribute("tableHeightType");
            Ext.getCmp("idsummaryTableHeightCombo").hide();
            Ext.getCmp("idsummaryTableHeightCombo").disable();
            obj.summaryTableHeight = "";
        }
        obj.isSummaryTable = isSummaryTable;
        
    } else if (fieldType == Ext.fieldID.pagePanel) {
            var ispreprinted = getEXTComponent('idprePrintedCheck').getValue();
            var ismultipletransaction = getEXTComponent('idallowmultipletransactionCheck').getValue();
            var pagesize = getEXTComponent('idPageSize').getValue();
            var pageorientation = getEXTComponent('idOrientation').getValue();
            var pagemarginunit = getEXTComponent('idPageMarginUnit').getValue();
            var pageleft = getEXTComponent('idpageleft').getValue()+pagemarginunit;
            var pageright = getEXTComponent('idpageright').getValue()+pagemarginunit;
            var pagebottom = getEXTComponent('idpagebottom').getValue()+pagemarginunit;
            var pagetop = getEXTComponent('idpagetop').getValue()+pagemarginunit;
            var topPageBorder = getEXTComponent("allowTopPageBorderid").getValue();
            var bottomPageBorder = getEXTComponent("allowBottomPageBorderid").getValue();
            var leftPageBorder = getEXTComponent("allowLeftPageBorderid").getValue();
            var rightPageBorder = getEXTComponent("allowRightPageBorderid").getValue();
//            var pageBorderType = Ext.getCmp("pageBorderTypeid").getValue();   // ERP-18286 : Dotted page border is applying for only first page in print
            var pagewidth = getEXTComponent('idpagewidth').getValue();
            var pageheight = getEXTComponent('idpageheight').getValue();
            var pagefont = getEXTComponent('pagefontid').getValue();
            var pagefontsize = getEXTComponent('idFontSize').getValue();
            var pageBorderIncluded = getEXTComponent('allowPageBorderid').getValue();
            var neagtiveValueIn = getEXTComponent('negativeValueComboid').getValue();
            Ext.getCmp("idMainPanel").body.setStyle('font-family', '' + pagefont + '');
            pagelayoutproperty[0] = savePageProperty(ispreprinted,pagesize,pageorientation,pageleft,pageright,pagebottom,pagetop,pagewidth,pageheight,pagefont,pagefontsize,pageBorderIncluded,pagemarginunit,topPageBorder,bottomPageBorder,leftPageBorder,rightPageBorder,ismultipletransaction,neagtiveValueIn);//,pageBorderType); // ERP-18286 : Dotted page border is applying for only first page in print
    } else if (fieldType == Ext.fieldID.insertDataElement) {
            var dataspan = "";
            var allowlabel = ele.allowlabel?ele.allowlabel:false;
            if(allowlabel){
                var label = ele.getEl().dom.children[0].children[0].children[0];
                var labelspan = ele.getEl().dom.children[0].children[0];
                dataspan = ele.getEl().dom.children[0].children[1];
                if(Ext.getCmp("idFieldValue")){
                    var newlabel = Ext.getCmp("idFieldValue").getValue();
                    label.innerHTML= newlabel;
                    ele.labelval = newlabel;
                }
                if(Ext.getCmp("idBoldLabel")){
                    var value = Ext.getCmp("idBoldLabel").getValue();
                    if(value==true){
                        labelspan.style.fontWeight = "bold";
                    } else {
                        labelspan.style.fontWeight = "normal";
                    }
                    ele.labelbold = value;
                }
                if(Ext.getCmp("idItalicLabel")){
                    var value = Ext.getCmp("idItalicLabel").getValue();
                    if(value==true){
                        labelspan.style.fontStyle = "Italic";
                    } else {
                        labelspan.style.fontStyle = "normal";
                    }
                    ele.labelitalic = value;
                }
                if (Ext.getCmp("idLabelFontSize")) {
                    var fontsize = Ext.getCmp("idLabelFontSize").getValue();
                    labelspan.style.fontSize = fontsize + "px";
                    ele.labelfontsize = fontsize;
                }
                if (Ext.getCmp("idLabelFontFamily")) {
                    var tmpFontFamily = Ext.getCmp('idLabelFontFamily').getValue();
                    labelspan.style.fontFamily = tmpFontFamily;
                    ele.labelfontfamily = tmpFontFamily;
                }
                if(Ext.getCmp("idLabelAlignSelectCombo")){
                    var align = Ext.getCmp("idLabelAlignSelectCombo").getValue();
                    if(align == "center"){
                        labelspan.style.textAlign = "center";
                    } else if(align == "left"){
                        labelspan.style.textAlign = "left";
                    } else if(align == "right"){
                        labelspan.style.textAlign = "right";
                    }
                    ele.labelalign = align;
                }
                if(Ext.getCmp("colonPositionCombo")){
                    var editvalue = Ext.getCmp("idFieldValue").getValue();
                    var colonType =Ext.getCmp("colonPositionCombo").getValue();
                    if(colonType!=null && colonType===0){//With the text
                        editvalue = editvalue+"<span class='colonwiththetextwithoutmargin'>:</span>";
                        colonType=0;
                    } else if(colonType==1){ // Right Aligned
                        editvalue = editvalue+"<span class='rightalignedcolonwithoutmargin'>:</span>";
                        colonType=1;
                    }
                    label.innerHTML = editvalue;
                    ele.labelval = editvalue;
                    ele.colontype = colonType;
                }
                if(Ext.getCmp("idTextLineLabel")){
                    var textLine = Ext.getCmp("idTextLineLabel").getRawValue();
                    if (textLine == "Overline") {
                        labelspan.style.textDecoration = 'overline';
                        ele.textlinelabel = "Overline";
                    } else if (textLine == "Line-through") {
                        labelspan.style.textDecoration = 'line-through';
                        ele.textlinelabel = "Line-through";
                    } else if (textLine == "Underline") {
                        labelspan.style.textDecoration = 'underline';
                        ele.textlinelabel = "Underline";
                    } else {
                        labelspan.style.textDecoration = '';
                        ele.textlinelabel = "None";
                    }
                
                }
                if(Ext.getCmp('colorpickerlabel')){
                    var selectednewcolor = Ext.getCmp('colorpickerlabel').value;
                    labelspan.style.color = selectednewcolor;
                    ele.textcolorlabel = selectednewcolor;
                }
                
            } else{
                dataspan = ele.getEl().dom.children[0].children[0];
                dataspan.style.width = "100%";
            }
            
            if(Ext.getCmp("idBoldData")){
                var value = Ext.getCmp("idBoldData").getValue();
                if(value==true){
                    dataspan.style.fontWeight = "bold";
                } else {
                    dataspan.style.fontWeight = "normal";
                }
                ele.databold = value;
            }
            if(Ext.getCmp("idItalicData")){
                var value = Ext.getCmp("idItalicData").getValue();
                if(value==true){
                    dataspan.style.fontStyle = "Italic";
                } else {
                    dataspan.style.fontStyle = "normal";
                }
                ele.dataitalic = value;
            }
            
            if(Ext.getCmp("idDataFontSize")){
                var fontsize = Ext.getCmp("idDataFontSize").getValue();
                dataspan.style.fontSize = fontsize + "px";
                ele.datafontsize = fontsize;
            }
            if(Ext.getCmp("idDataFontFamily")){
                var fontfamily = Ext.getCmp("idDataFontFamily").getValue();
                dataspan.style.fontFamily = fontfamily;
                ele.datafontfamily = fontfamily;
            }
            
            if(Ext.getCmp("idDataAlignSelectCombo")){
                var align = Ext.getCmp("idDataAlignSelectCombo").getValue();
                if(align == "center"){
                    dataspan.style.textAlign = "center";
                } else if(align == "left"){
                    dataspan.style.textAlign = "left";
                } else if(align == "right"){
                    dataspan.style.textAlign = "right";
                }
                ele.dataalign = align;
            }
            // common settings
            var margin= 0;
            if(Ext.getCmp("idtopmargin")){
                margin = Ext.getCmp("idtopmargin").getValue();
                ele.getEl().dom.style.marginTop= margin+"px";
                ele.topmargin = margin;
            }
            if(Ext.getCmp("idbottommargin")){
                margin = Ext.getCmp("idbottommargin").getValue();
                ele.getEl().dom.style.marginBottom= margin+"px";
                ele.bottommargin = margin;
            }
            if(Ext.getCmp("idleftmargin")){
                margin = Ext.getCmp("idleftmargin").getValue();
                ele.getEl().dom.style.marginLeft= margin+"px";
                ele.leftmargin = margin;
            }
            if(Ext.getCmp("idrightmargin")){
                margin = Ext.getCmp("idrightmargin").getValue();
                ele.getEl().dom.style.marginRight= margin+"px";
                ele.rightmargin = margin;
            }
            
            if(Ext.getCmp("idTextLineData")){
                var textLine = Ext.getCmp("idTextLineData").getRawValue();
                if (textLine == "Overline") {
                    dataspan.style.textDecoration = 'overline';
                    ele.textlinedata = "Overline";
                } else if (textLine == "Line-through") {
                    dataspan.style.textDecoration = 'line-through';
                    ele.textlinedata = "Line-through";
                } else if (textLine == "Underline") {
                    dataspan.style.textDecoration = 'underline';
                    ele.textlinedata = "Underline";
                }else {
                    dataspan.style.textDecoration = '';
                    ele.textlinedata = "None";
                }
                
            }
            
            if(Ext.getCmp('colorpickerdata')){
                var selectednewcolor = Ext.getCmp('colorpickerdata').value;
                dataspan.style.color = selectednewcolor;
                ele.textcolordata = selectednewcolor;
            }
            
            if(Ext.getCmp('decimalprecisiondataid')){
                ele.decimalPrecision = Ext.getCmp('decimalprecisiondataid').getValue();
            }
            if (Ext.getCmp("iddefaultdata")) {
                Ext.getCmp(selectedElement).defaultValue = Ext.getCmp("iddefaultdata").getValue().replace(/\s/g,space);
            } 
            if (Ext.getCmp("idvalSeparatorDataField")) {
                Ext.getCmp(selectedElement).valueSeparator = Ext.getCmp("idvalSeparatorDataField").getValue();
            } 
            if (Ext.getCmp("recordCurrencyId")) {
                Ext.getCmp(selectedElement).specificreccurrency = Ext.getCmp("recordCurrencyId").getValue();
            }
            if (Ext.getCmp("iddimensionDataField")) {
                Ext.getCmp(selectedElement).dimensionValue = Ext.getCmp("iddimensionDataField").getValue();
            } 
            if(Ext.getCmp('valuewithcommaDataid')){
                Ext.getCmp(selectedElement).valueWithComma = Ext.getCmp("valuewithcommaDataid").getValue();
            }
            var dataelement = ele.getEl().dom.children[0];
            
            if(Ext.getCmp('idComponentWidth')){
                ele.getEl().dom.style.width=Ext.getCmp('idComponentWidth').getValue()+"%";
                ele.width = Ext.getCmp('idComponentWidth').getValue()+"%";
                ele.componentwidth = Ext.getCmp('idComponentWidth').getValue();
            }
            if(Ext.getCmp('idfields_Alignment')){
                var fieldAlignment = Ext.getCmp('idfields_Alignment').getValue();
                if ( fieldAlignment == "1" ) {
                    ele.removeCls('sectionclass_data_element_inline');
                    ele.addCls('sectionclass_data_element');
                    ele.fieldalignment = "1"
                } else if ( fieldAlignment == "2" ) {
                    ele.removeCls('sectionclass_data_element');
                    ele.addCls('sectionclass_data_element_inline');
                    ele.fieldalignment = "2"
                }
            }
            
            ele.dataelementhtml = dataelement.outerHTML;
            
    } else if (fieldType == Ext.fieldID.insertDetailsTable) {
        //update property of Details Table
        var obj = ele;
        var table = obj.el.dom.children[0];
        var align = Ext.getCmp("allignDetailsTableCombo").getValue();
        var cells = table.children[1].children[0].cells;
        //set width
        if (Ext.getCmp('idWidth').getValue() > 0) {
            var width = Ext.getCmp('idWidth').getValue();
            table.style.width = width+"%";
            obj.tablewidth = width;
        }
        //set alignments
        if(align== 'left'  || align === 0){
            for( var i=0; i< cells.length; i++ ){
                cells[i].align="left";
                cells[i].style.textAlign="left";
                cells[i].style.paddingLeft="4px";
            }
            obj.align = 0;
        } else if(align=='center' || align == 1){
            for( var i=0; i< cells.length; i++ ){
                cells[i].align="center";
                cells[i].style.textAlign="center";
            }
            obj.align = 1;
        } else{
            for( var i=0; i< cells.length; i++ ){
                cells[i].align="right";
                cells[i].style.paddingRight="4px";
                cells[i].style.textAlign="right";
            }
            obj.align = 2;
        }
        //set bold header text
        if (Ext.getCmp("boldTextLineItemId").getValue() == true) {
            for( var i=0; i< cells.length; i++ ){
                cells[i].style.fontWeight="bold";
            }
            Ext.getCmp(selectedElement).bold = "true";
        } else {
            for( var i=0; i< cells.length; i++ ){
                cells[i].style.fontWeight="normal";
            }
            Ext.getCmp(selectedElement).bold = "false";
        }
        //set italic header text
        if (Ext.getCmp("italicTextLineItemId").getValue() == true) {
            for( var i=0; i< cells.length; i++ ){
                cells[i].style.fontStyle="italic";
            }
            Ext.getCmp(selectedElement).italic = "true";
        } else {
            for( var i=0; i< cells.length; i++ ){
                cells[i].style.fontStyle="normal";
            }
            Ext.getCmp(selectedElement).italic = "false";
        }
        //set underline to header text
        if (Ext.getCmp("underLineTextLineItemId").getValue() == true) {
            for( var i=0; i< cells.length; i++ ){
                cells[i].style.textDecoration="underline";
            }
            Ext.getCmp(selectedElement).underline = "true";
        } else {
            for( var i=0; i< cells.length; i++ ){
                cells[i].style.textDecoration="";
            }
            Ext.getCmp(selectedElement).underline = "false";
        }
        //set font size of header text
        if (Ext.getCmp('fontSizeLineItemId').getValue() > 0){
            var tmpFontSize = Ext.getCmp('fontSizeLineItemId').getValue() + 'px';
            for( var i=0; i< cells.length; i++ ){
                cells[i].style.fontSize=tmpFontSize;
            }
            Ext.getCmp(selectedElement).fontsize = Ext.getCmp('fontSizeLineItemId').getValue();
        } else {
            for( var i=0; i< cells.length; i++ ){
                cells[i].style.fontSize="";
            }
            Ext.getCmp(selectedElement).fontsize = "";
        }
        //set font family of header text
        var tmpFontFamily = Ext.getCmp('fontDetailsTableId').getValue();
        for( var i=0; i< cells.length; i++ ){
            cells[i].style.fontFamily=tmpFontFamily;
        }
        Ext.getCmp(selectedElement).fontfamily = Ext.getCmp('fontDetailsTableId').getValue();
        //set margins
        var margintop =  Ext.getCmp("idtopmargin").getValue();
        var marginleft =  Ext.getCmp("idleftmargin").getValue();
        var marginright =  Ext.getCmp("idrightmargin").getValue();
        var marginbottom =  Ext.getCmp("idbottommargin").getValue();
        Ext.getCmp(selectedElement).getEl().dom.style.marginTop = margintop + "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginBottom = marginbottom+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginRight = marginright+ "px";
        Ext.getCmp(selectedElement).getEl().dom.style.marginLeft = marginleft + "px";
        Ext.getCmp(selectedElement).marginTop = margintop;
        Ext.getCmp(selectedElement).marginBottom = marginbottom;
        Ext.getCmp(selectedElement).marginRight = marginright;
        Ext.getCmp(selectedElement).marginLeft = marginleft;
        Ext.getCmp(selectedElement).consolidatedfield = Ext.getCmp("consolidationfieldid").getValue();
        Ext.getCmp(selectedElement).summationfields = Ext.getCmp("summationfieldsid").getValue();
    }
}

/*Page Property*/
function savePageProperty(ispreprinted,pagesize,pageorientation,pageleft,pageright,pagebottom,pagetop,pagewidth,pageheight,pagefont,pagefontsize,pageBorderIncluded,pagemarginunit,topPageBorder,bottomPageBorder,leftPageBorder,rightPageBorder,ismultipletransaction,neagtiveValueIn) {//,pageBorderType) {// ERP-18286 : Dotted page border is applying for only first page in print
    var returnConfig  = {
        "pagelayoutsettings": {
            "ispreprinted":ispreprinted,
            "pagesize": pagesize, 
            "pageorientation": pageorientation,
            "pagewidth": pagewidth, 
            "pageheight": pageheight,
            "pagetop": pagetop, 
            "pagebottom": pagebottom,
            "pageleft": pageleft,
            "pageright":pageright,
            "pagefont": pagefont, 
            "pagefontsize":pagefontsize,
            "pageBorderIncluded":pageBorderIncluded,
            "pagemarginunit":pagemarginunit,
            "topPageBorder":topPageBorder,
            "bottomPageBorder":bottomPageBorder,
            "leftPageBorder":leftPageBorder,
            "isExtendedGlobalTable":isExtendedGlobalTable,
            "adjustPageHeight":adjustPageHeight,
            "pageSizeForExtGT":pageSizeForExtGT,
            "pageOrientationForEXTGT":pageOrientationForEXTGT,
            "rightPageBorder":rightPageBorder,
//            "pageBorderType":pageBorderType   // ERP-18286 : Dotted page border is applying for only first page in print
            "ismultipletransaction":ismultipletransaction,
            "negativeValueIn":neagtiveValueIn
        }
    };
    return returnConfig;
}
    
function showElements(selectedElement)
{

    var obj = Ext.getCmp(selectedElement);
    var fieldType = obj.fieldType;

    /*Row Panel*/
    if (fieldType == Ext.fieldID.insertRowPanel)
    {
        Ext.getCmp("idSelectField").setDisabled(true);
        Ext.getCmp("idBoxtBtn").setDisabled(true);
        Ext.getCmp("idStaticTextBtn").setDisabled(true);
        Ext.getCmp("idImageBtn").setDisabled(true);
        Ext.getCmp("idPanel").setDisabled(true);
        Ext.getCmp("idInlineTableBtn").setDisabled(true);
        Ext.getCmp("idInsertDataElementBtn").setDisabled(true);

    }

    /*Column Panel*/
    else if (fieldType == Ext.fieldID.insertColumnPanel )
    {
        Ext.getCmp("idSelectField").setDisabled(false);
        Ext.getCmp("idBoxtBtn").setDisabled(false);
        Ext.getCmp("idStaticTextBtn").setDisabled(false);
        Ext.getCmp("idImageBtn").setDisabled(false);
        Ext.getCmp("idPanel").setDisabled(true);
        Ext.getCmp("idSelectField").setDisabled(false);
        Ext.getCmp("idInlineTableBtn").setDisabled(false);
        Ext.getCmp("idInsertDataElementBtn").setDisabled(false);
    }
    /*Page Panel*/
    else if (fieldType == Ext.fieldID.pagePanel)
    {
        Ext.getCmp("idSelectField").setDisabled(true);
        Ext.getCmp("idBoxtBtn").setDisabled(true);
        Ext.getCmp("idStaticTextBtn").setDisabled(true);
        Ext.getCmp("idImageBtn").setDisabled(true);
        Ext.getCmp("idPanel").setDisabled(false);
        Ext.getCmp("idInlineTableBtn").setDisabled(true);
        Ext.getCmp("idInsertDataElementBtn").setDisabled(true);
    }
    else
    {
        Ext.getCmp("idSelectField").setDisabled(true);
        Ext.getCmp("idBoxtBtn").setDisabled(true);
        Ext.getCmp("idStaticTextBtn").setDisabled(true);
        Ext.getCmp("idImageBtn").setDisabled(true);
        Ext.getCmp("idPanel").setDisabled(true);
        Ext.getCmp("idInlineTableBtn").setDisabled(true);
        Ext.getCmp("idInsertDataElementBtn").setDisabled(true);
    }
}
    
    
    
function resetProperty(selectedElement)
{
    var ele = Ext.getCmp("idMainPanel").queryById(selectedElement);
    if (!ele) {
        ele = Ext.getCmp(selectedElement);
    }
    var fieldType = ele.fieldType;
    if (fieldType == Ext.fieldID.insertText)
    {
        var obj = ele.items.items[0];
        Ext.getCmp('idTextAlign').setDisabled(false);
        Ext.getCmp('idUnit').setDisabled(false);
        Ext.getCmp('idWidth').setDisabled(false);
        Ext.getCmp('idUseColon').setDisabled(false);
        Ext.getCmp('idColonAlign').setDisabled(false);

        /****
             *Text Align
             *****/
        var textAlign = Ext.getCmp('idTextAlign').originalValue;
        if (textAlign == 'Left') {
            ele.removeCls('classTextAligment_center');
            ele.removeCls('classTextAligment_right');
            ele.addClass('classTextAligment_left');
            Ext.getCmp(selectedElement).textalignclass = "classTextAligment_left";
        }
        else if (textAlign == 'Center') {
            ele.removeCls('classTextAligment_left');
            ele.removeCls('classTextAligment_right');
            ele.addClass('classTextAligment_center');
            Ext.getCmp(selectedElement).textalignclass = "classTextAligment_center";
        }
        else if (textAlign == 'Right') {
            ele.removeCls('classTextAligment_left');
            ele.removeCls('classTextAligment_center');
            ele.addClass('classTextAligment_right');
            Ext.getCmp(selectedElement).textalignclass = "classTextAligment_right";
        }

        var selectednewcolor = Ext.getCmp('colorpicker').value;
        Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('color', selectednewcolor);
        Ext.getCmp(selectedElement).textcolor = selectednewcolor;

        var flg = Ext.getCmp('idUseColon').originalValue;
        if (flg == true) {
            Ext.getCmp("idColonAlign").setDisabled(false);

            var tmpStr = "";
            tmpStr = Ext.getCmp("idSelectedLabel").getValue();
            var colonAlign = Ext.getCmp("idColonAlign").getRawValue();
            var strColon = "";
            if (colonAlign == "Left") {
                strColon = tmpStr + " :";
            }
            if (colonAlign == "Center")
                strColon = tmpStr + "<span class='colonAlignment_center'> :</span>";
            if (colonAlign == "Right")
                strColon = tmpStr + "<span class='colonAlignment_right'> :</span>";


            var tmpcolon = {
                xtype: 'label',
                forId: selectedElement,
                cls: 'colonAlignment_center',
                text: ":"
            };

            ele.add(tmpcolon);

        } else {
            Ext.getCmp("idColonAlign").setDisabled(true);
        }

        if (Ext.getCmp('idWidth').originalValue > 0 && Ext.getCmp('idUnit').originalValue == "px") {

            Ext.getCmp(selectedElement).setWidth(Ext.getCmp("idWidth").originalValue);
            //Ext.getCmp(selectedElement).width=Ext.getCmp("idWidth").getValue();
            Ext.getCmp(selectedElement).elementwidth = Ext.getCmp('idWidth').originalValue;
            Ext.getCmp(selectedElement).unit = "px";

        }
        if (Ext.getCmp('idWidth').originalValue > 0 && Ext.getCmp('idUnit').originalValue == "%") {
            var tmpWidth = 0;
            if (Ext.getCmp('idWidth').originalValue == "100")
                tmpWidth = parseFloat("1");
            else
                tmpWidth = parseFloat("0." + Ext.getCmp('idWidth').originalValue);

            tmpWidth = Ext.getCmp(selectedElement).ownerCt.getWidth() * tmpWidth;
            Ext.getCmp(selectedElement).setWidth(tmpWidth);
            //Ext.getCmp(selectedElement).width=Ext.getCmp('idWidth').getValue();
            Ext.getCmp(selectedElement).elementwidth = Ext.getCmp('idWidth').originalValue;
            Ext.getCmp(selectedElement).unit = "%";
        }

        if (Ext.getCmp("idBoldText").originalValue == true) {
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('font-weight', 'bold');
            Ext.getCmp(selectedElement).bold = "true";
        }
        else {
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('font-weight', 'normal');
            Ext.getCmp(selectedElement).bold = "false";
        }

        if (Ext.getCmp("idItalicText").originalValue == true) {

            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('font-style', 'italic');
            Ext.getCmp(selectedElement).italic = "true";
        } else {
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('font-style', 'normal');
            Ext.getCmp(selectedElement).italic = "false";
        }

        if (Ext.getCmp("idTextLine").originalValue == "Overline") {
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('text-decoration', 'overline');
            Ext.getCmp(selectedElement).textline = "Overline";
        } else if (Ext.getCmp("idTextLine").originalValue == "Line-through") {
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('text-decoration', 'line-through');
            Ext.getCmp(selectedElement).textline = "Line-through";
        } else if (Ext.getCmp("idTextLine").originalValue == "Underline") {
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('text-decoration', 'underline');
            Ext.getCmp(selectedElement).textline = "Underline";

        } else {
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('text-decoration', '');
            Ext.getCmp(selectedElement).textline = "None";
        }

        if (Ext.getCmp('idFontSize').originalValue > 0)
        {
            var tmpFontSize = Ext.getCmp('idFontSize').getValue() + 'px';
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('font-size', tmpFontSize);
            Ext.getCmp(selectedElement).fontsize = tmpFontSize;
        }
         if (Ext.getCmp('pagefontid'))
        {
            var tmpFontFamily = Ext.getCmp('pagefontid').getValue();
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('font-family', tmpFontFamily);
            Ext.getCmp(selectedElement).fontfamily = tmpFontFamily;
        }
        Ext.getCmp("idTextAlign").setValue(Ext.getCmp("idTextAlign").originalValue);
        Ext.getCmp("idUnit").setValue(Ext.getCmp("idUnit").originalValue);
        Ext.getCmp("idWidth").setValue(Ext.getCmp("idWidth").originalValue);
        Ext.getCmp("idHeight").setValue(Ext.getCmp("idHeight").originalValue);
        Ext.getCmp("idTextLine").setValue(Ext.getCmp("idTextLine").originalValue);
        Ext.getCmp("idItalicText").setValue(Ext.getCmp('idItalicText').originalValue);
        Ext.getCmp('idFontSize').setValue(Ext.getCmp('idFontSize').originalValue);
        Ext.getCmp('pagefontid').setValue(Ext.getCmp('pagefontid').originalValue);
        Ext.getCmp('idWidth').setValue(Ext.getCmp('idWidth').originalValue);
        Ext.getCmp("idBoldText").setValue(Ext.getCmp("idBoldText").originalValue);

    }

    /*Select Field*/
    if (fieldType == Ext.fieldID.insertField)
    {
        var obj = ele;
        Ext.getCmp('idTextAlign').setDisabled(false);
        Ext.getCmp('idUnit').setDisabled(false);
        Ext.getCmp('idWidth').setDisabled(false);
        Ext.getCmp('idColonAlign').setDisabled(false);
        Ext.getCmp(selectedElement).setWidth(Ext.getCmp("idWidth").originalValue);

        if (Ext.getCmp('idWidth').originalValue > 0 && Ext.getCmp('idUnit').originalValue == "px") {
            Ext.getCmp(selectedElement).setWidth(Ext.getCmp("idWidth").originalValue);
            //Ext.getCmp(selectedElement).width=Ext.getCmp("idWidth").getValue();
            Ext.getCmp(selectedElement).elementwidth = Ext.getCmp('idWidth').originalValue;
            Ext.getCmp(selectedElement).unit = "px";
        }
        if (Ext.getCmp('idWidth').originalValue > 0 && Ext.getCmp('idUnit').originalValue == "%") {
            var tmpWidth = 0;
            if (Ext.getCmp('idWidth').originalValue == "100")
                tmpWidth = parseFloat("1");
            else
                tmpWidth = parseFloat("0." + Ext.getCmp('idWidth').originalValue);

            tmpWidth = Ext.getCmp(selectedElement).ownerCt.getWidth() * tmpWidth;
            Ext.getCmp(selectedElement).setWidth(tmpWidth);

            //Ext.getCmp(selectedElement).width=Ext.getCmp('idWidth').getValue();
            Ext.getCmp(selectedElement).elementwidth = Ext.getCmp('idWidth').originalValue;
            Ext.getCmp(selectedElement).unit = "%";
        }

        /****
             *Text Align
             *****/
        var textAlign = Ext.getCmp('idTextAlign').originalValue;
        if (textAlign == 'Left') {
            ele.removeCls('classTextAligment_center');
            ele.removeCls('classTextAligment_right');
            ele.addClass('classTextAligment_left');
            Ext.getCmp(selectedElement).textalignclass = "classTextAligment_left";
        }
        else if (textAlign == 'Center') {
            ele.removeCls('classTextAligment_left');
            ele.removeCls('classTextAligment_right');
            ele.addClass('classTextAligment_center');
            Ext.getCmp(selectedElement).textalignclass = "classTextAligment_center";
        }
        else if (textAlign == 'Right') {
            ele.removeCls('classTextAligment_left');
            ele.removeCls('classTextAligment_center');
            ele.addClass('classTextAligment_right');
            Ext.getCmp(selectedElement).textalignclass = "classTextAligment_right";
        }

        if (Ext.getCmp('idFontSize').originalValue > 0)
        {
            var tmpFontSize = Ext.getCmp('idFontSize').getValue() + 'px';
            Ext.getCmp(selectedElement).getEl().el.setStyle('font-size', tmpFontSize);
            Ext.getCmp(selectedElement).fontsize = tmpFontSize;
        }
        if (Ext.getCmp('pagefontid'))
        {
            var tmpFontFamily = Ext.getCmp('pagefontid').getValue();
            Ext.getCmp(selectedElement).items.items[0].getEl().setStyle('font-family', tmpFontFamily);
            Ext.getCmp(selectedElement).fontfamily = tmpFontFamily;
        }
        Ext.getCmp("idTextAlign").setValue(Ext.getCmp("idTextAlign").originalValue);
        Ext.getCmp("idUnit").setValue(Ext.getCmp("idUnit").originalValue);
        Ext.getCmp("idWidth").setValue(Ext.getCmp("idWidth").originalValue);
        Ext.getCmp('idFontSize').setValue(Ext.getCmp('idFontSize').originalValue);
        Ext.getCmp('pagefontid').setValue(Ext.getCmp('pagefontid').originalValue);
    }

    /*Image*/
    if (fieldType == Ext.fieldID.insertImage)
    {
        var obj = ele;
        Ext.getCmp('idUnit').setDisabled(false);
        Ext.getCmp('idWidth').setDisabled(false);
        Ext.getCmp(selectedElement).setWidth(Ext.getCmp("idWidth").getValue());
        if (Ext.getCmp('idWidth').getValue() > 0) {
            obj.setWidth(Ext.getCmp('idWidth').getValue());
        }

    }

    /*Inline Table*/
    if (fieldType == Ext.fieldID.insertTable)
    {
        var obj = ele;
        Ext.getCmp('idUnit').setDisabled(false);
        Ext.getCmp('idWidth').setDisabled(false);

        var unit = Ext.getCmp('idUnit').getRawValue();

        if (Ext.getCmp('idWidth').getValue() > 0) {
            if (unit == "px")
                obj.setWidth(Ext.getCmp('idWidth').getValue());
            else
            {

                var tmpwidth = parseFloat('.' + Ext.getCmp('idWidth').getValue());
                obj.setWidth(obj.ownerCt.getWidth() * tmpwidth);
            //obj.style.width=Ext.getCmp('idWidth').getValue()+"%";
            }
        }
        var selectednewcolor = Ext.getCmp('colorpicker').value;
        Ext.get('itemlistconfigsectionPanelGrid').dom.style.borderColor = selectednewcolor;
        obj.bordercolor = selectednewcolor;
        
        var table=  obj.el.dom.children[0];
        var align=  Ext.getCmp("allignLineTableCombo").getValue();
        var cells=table.children[1].children[0].cells;
        if(align== 'left'  || align == 0){
            for( var i=0; i< cells.length; i++ ){
                cells[i].align="left";
                cells[i].style.paddingLeft="2px";
            }
        } else if(align=='center' || align == 1){
            for( var i=0; i< cells.length; i++ ){
                cells[i].align="center";
            }
        } else{
            for( var i=0; i< cells.length; i++ ){
                cells[i].align="right";
                cells[i].style.paddingRight="2px";
            }
        }

    }


    /*Row Panel / Section*/
    if (fieldType == Ext.fieldID.insertRowPanel )
    {
        setColumns();
    }

    /*Column*/
    if (fieldType == Ext.fieldID.insertColumnPanel )
    {
        var type = Ext.getCmp("idType").getRawValue();
        if (type == "Custom")
        {
            var width = Ext.getCmp("idWidth").getValue();
            var unit = Ext.getCmp("idUnit").getValue();
            var parent = Ext.getCmp(selectedElement).ownerCt;
            var colCount = parent.items.length;

            for (var i = 0; i < colCount; i++)
            {
                var tmpid = parent.items.items[i].id;
                if (tmpid == selectedElement)
                {
                    var tmpwidth = 100 - (100 - width);
                    if (width < 10)
                        tmpwidth = parseFloat("0.0" + tmpwidth);
                    else
                        tmpwidth = parseFloat("0." + tmpwidth);

                    Ext.getCmp(selectedElement).columnWidth = tmpwidth;
                }
                else
                {
                    var tmpwidth = (100 - width) / (colCount - 1);
                    tmpwidth = parseFloat("0." + tmpwidth);
                    Ext.getCmp(tmpid).columnWidth = tmpwidth;
                }

            }
            parent.doLayout();

        }


    }

//        initTreePanel();

}
/* Used for setting innerHTML while merging or splitting*/
function getOrSetInnerhtml(obj,innerHtmlArray, get, isMerging)
{
    if(obj.items){
        if(obj.items.items){
            for(var i=0 ; i < obj.items.items.length ; i++){
                var element = document.getElementById(obj.items.items[i].id+"-innerCt");
                if(element != undefined){
                    if(get){
                        if(element.innerHTML){
                            innerHtmlArray[i] = element.innerHTML;
                        }
                    } else{
                        if(innerHtmlArray[i]){
                            element.innerHTML = innerHtmlArray[i];
                        }
                        if(isMerging){
                            // if merging add last component's element to second last element
                            if(i==obj.items.items.length-1){
                                if(innerHtmlArray[i+1]){
                                    element.innerHTML += innerHtmlArray[i+1];
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
    
/* Used for merging or splitting-Neeraj*/
function MergingSplittingItems(obj,isMerging,col)
{
    var innerHtmlArray = new Array();
    /*Splitting & Merging(obj=row)*/
    if(obj.items){
        if(obj.items.items){
            for(var i=0 ; i < obj.items.items.length ; i++){/*Columns*/
                
                if(obj.items.items[i].items.items.length>0){
                    var columnHtmlArray = new Array();
                    for(var j=0 ; j <obj.items.items[i].items.items.length ; j++){ /*Fields*/   
                        columnHtmlArray.push(obj.items.items[i].items.items[j]);
                    }
                    innerHtmlArray.push(columnHtmlArray);
                }
            }
        }
    }
    /*Merging Case--Putting all array after col in last column of innerHtmlArray */
    if(isMerging){
        for(var m=col+1;m<innerHtmlArray.length;m++){
            var VariableArray=innerHtmlArray[m];
            for(var n=0;n<VariableArray.length;n++){
                innerHtmlArray[col].push(VariableArray[n]);
            }
        }
    }
    return innerHtmlArray;
}  
    
//   /*Setting Columns*/
function setColumns(parent, colCount, colObj, isMerging)
{
    var obj = null;
    var col = null;

    if (parent == null || parent == undefined) {
        /* For update properties*/
        obj = Ext.getCmp(selectedElement);
        col = Ext.getCmp('idColumns').getValue();
    }
    else
    {  /* from loadDocument() */
        obj = parent;
        col = colCount;
    }
    
    var colArray = obj.items.items;
    var backgroundColor = [];
    for (var i = 0; i < col; i++){
        if(obj.isPrePrinted){ //get background colors applied to each column
            backgroundColor[i] = colArray[i]==null || colArray[i]==undefined ? 'rgb(255, 255, 255)' : colArray[i].el.dom.children[0].children[0].children[0].style.getPropertyValue("background-color");
        }
    }
    
    var ColumnItemsArray= MergingSplittingItems(obj,isMerging,col-1);
//    getOrSetInnerhtml(obj,innerHtmlArray,true,isMerging);
    obj.removeAll();
    var colWidth = '0.' + (100 / col);
    var tmpWidth = parseFloat(colWidth);
    if (col == 1) {
        tmpWidth = 1;
    }
            
    var widthClass = "sectionclass_element_100"
        
    switch(col){
        case 1 :
            widthClass = "sectionclass_element_100";
            break;
        case 2 :
            widthClass = "sectionclass_element_50";
            break;
        case 3 :
            widthClass = "sectionclass_element_33";
            break;
        case 4 :
            widthClass = "sectionclass_element_25";
            break;
        case 5 :
            widthClass = "sectionclass_element_20";
            break;
    }

    for (var i = 0; i < col; i++)
    {        
        var innerPanel = Ext.create("Ext.Panel", {
            id: (colObj != undefined) ? colObj.id : '',
            fieldType: Ext.fieldID.insertColumnPanel,
            columnwidth: tmpWidth * 100,
            columnWidth: tmpWidth,
            //resizable:true,
            autoHeight:true,
            marginTop : 0,
            marginLeft : 0,
            marginBottom : 0,
            marginRight : 0,
            cls : "sectionclass_element " + widthClass + ( (i==0) ? "  " : " section_left_margin "),
            scope:this,
            html:space,
            flex: 1,
            listeners:
            {
                render: function (c) {
                     c.el.dom.style.marginTop = "0px";
                    c.el.dom.style.marginBottom = "0px";
                    c.el.dom.style.marginLeft = "0px"; 
                    c.el.dom.style.marginRight = "0px";
                    c.el.dom.children[0].children[0].children[0].style.setProperty("background-color",backgroundColor[i],""); //set background color
                    c.getEl().on('click', function (e) {
                        if (selectedElement != null || selectedElement != undefined)
                        {
                            Ext.get(selectedElement).removeCls("selected");
                        }
                        selectedElement = this.id;
                        Ext.getCmp(selectedElement).addClass("selected");
                        e.stopPropagation( );
                        createPropertyPanel(selectedElement);
                        setProperty(selectedElement);
                        showElements(selectedElement);

                    });
                    c.getEl().on("contextmenu", function (event, element) {
                        if (selectedElement != null || selectedElement != undefined)
                        {
                            if(Ext.get(selectedElement)!=null){
                                Ext.get(selectedElement).removeCls("selected");
                            }
                        }
                        selectedElement = this.id;
                        Ext.getCmp(selectedElement).addClass('selected');
                        event.stopEvent();
//                        selectedElement = element.parentNode.parentNode.parentNode.id;
                        contextMenu.showAt(event.getXY());
                        return false;
                    });

                },
                'resize': function () {
                // var panel = this;
                // panel.doLayout();
                }
            }
        });
               /*Splitting & Merging*/
           if(ColumnItemsArray[i]){
            var columnarray=ColumnItemsArray[i];
            for (var l = 0; l < columnarray.length; l++)
            {
                var fieldObj = columnarray[l];
                var field = null;
                if (fieldObj.fieldType == Ext.fieldID.insertText)
                {
                    field = createTextField(innerPanel,fieldObj.label, fieldObj);
                    
                }
                else if (fieldObj.fieldType == Ext.fieldID.insertField)
                {
                    var xType = fieldObj.xType?fieldObj.xType:"0";
                    var isFormula = fieldObj.isFormula?fieldObj.isFormula:false;
                    field = createExtComponent_2(innerPanel, undefined, fieldObj.fieldType, fieldObj.labelhtml, fieldObj.x, fieldObj.y,null, fieldObj, fieldObj.label,xType, isFormula);
                }
                else if (fieldObj.fieldType == Ext.fieldID.insertImage)
                {
                    field = createExtImgComponent(innerPanel, innerPanel, fieldObj.fieldType, fieldObj.src, fieldObj.x, fieldObj.y, fieldObj);
                }
                else if (fieldObj.fieldType == Ext.fieldID.insertDrawBox)
                {
                    field = createBox(fieldObj);
                }
                else if (fieldObj.fieldType == Ext.fieldID.insertTable)
                {
                    var isSummaryTable=false;
                    var summaryJson=summaryTableJson;
                    if(fieldObj.data){
                        if(fieldObj.data[0]){
                            if(fieldObj.data[0].isSummaryTable){
                                isSummaryTable = true;
//                                summaryTableJson= tempjson;
//                                summaryJson= tempjson;
                            }    
                        }    
                    }
                    field = getlineitemstable(fieldObj.x, fieldObj.y, fieldObj.labelhtml, innerPanel, fieldObj, isSummaryTable, summaryJson);
                }
                else if (fieldObj.fieldType == Ext.fieldID.insertGlobalTable)
                {
                    var borderedgetype = fieldObj.borderedgetype!=null?fieldObj.borderedgetype:"1";
                    var rowspacing = fieldObj.rowspacing!=null?fieldObj.rowspacing:"5";
                    var columnspacing = fieldObj.columnspacing!=null?fieldObj.columnspacing:"5";
                    var fieldAlignment = fieldObj.fieldAlignment!=null?fieldObj.fieldAlignment:"1"; 
                    var fixedrowvalue=fieldObj.fixedrowvalue!=null?fieldObj.fixedrowvalue:"";
                    var tableHeader=fieldObj.tableHeader!=null?fieldObj.tableHeader:false;
                    field = createGlobalTable(fieldObj.x, fieldObj.y, fieldObj.labelhtml, innerPanel, fieldObj.tableWidth, fieldObj.height,fixedrowvalue, Ext.fieldID.insertGlobalTable,borderedgetype,rowspacing,columnspacing,fieldAlignment,fieldObj,tableHeader);
                    
                }else if (fieldObj.fieldType == Ext.fieldID.insertDataElement)
                {
                    field = createDataElementComponent(innerPanel,fieldObj.fieldType, fieldObj.dataelementhtml, fieldObj.x, fieldObj.y,fieldObj);
                }
                
                innerPanel.add(field);
                innerPanel.doLayout();
            }
        } 
       obj.add(innerPanel);
    }
    obj.column = col;
    obj.doLayout();
}
   
   /*Create Text Field*/
function createTextField(designerPanel,text, obj)
{
    var tmpTextAlignClass = (obj != null || obj != undefined) ? obj.textalignclass : "classTextAligment_left";
    var fieldAlignment = (obj != null || obj != undefined) ? obj.fieldAlignment : "1";
    var tmpTextcolor = (obj != null || obj != undefined) ? obj.textcolor : "#000000";
    var tmpTextLine = (obj != null || obj != undefined) ? obj.textline : "None";
    var tmpBold = (obj != null || obj != undefined) ? obj.bold : false;
    var tmpItalic = (obj != null || obj != undefined) ? obj.italic : false;
    var tmpWidth = (obj != null || obj != undefined) ? obj.elementwidth : 100;
    //var tmpHeight = (obj != null || obj != undefined) ? obj.elementheight :40;
    var tmpUnit = (obj != null || obj != undefined) ? obj.unit : '%';
    var tmpFontSize = (obj != null || obj != undefined) ? obj.fontsize : '';
    var marginTop = (obj && obj.marginTop) || (obj && obj.marginTop === 0)  ? obj.marginTop  : '0px';
    var marginBottom = (obj && obj.marginBottom) || (obj && obj.marginBottom === 0) ? obj.marginBottom : '5px';
    var marginLeft = (obj && obj.marginLeft)|| (obj && obj.marginLeft === 0) ? obj.marginLeft : '0px';
    var marginRight = (obj && obj.marginRight) || (obj && obj.marginRight === 0) ? obj.marginRight : '0px';
    var fontfamily = (obj && obj.fontfamily) ? obj.fontfamily : '';
    var tmpheightSize = (obj != null || obj != undefined) ? obj.height : '50';
    var iselementlevel=(obj != null || obj != undefined) ? obj.iselementlevel :false;
    var isBulletsApplied=(obj && obj.isBulletsApplied)? obj.isBulletsApplied :false;
    var bulletType=(obj && obj.bulletType)? obj.bulletType :"none";
        
    /* If container class value is given as inline change the component to inline*/
    var containerclassfieldalignvalue=(designerPanel.fieldalignment!=null||designerPanel.fieldalignment!=undefined)?designerPanel.fieldalignment:"1";
    var containerclass="sectionclass_field_container";
    if(containerclassfieldalignvalue== "2"){
        containerclass='sectionclass_field_container_inline';
    }
        
    var sectionclass="sectionclass_field";
    if(fieldAlignment== "2"){
        sectionclass='sectionclass_field sectionclass_field_container_inline';
    } 
        
    var tmpElement = Ext.create("Ext.Component", {
        border: true,
        fieldType: Ext.fieldID.insertText,
        //draggable: true,
        x: (obj != null || obj != undefined) ? obj.x : '',
        y: (obj != null || obj != undefined) ? obj.y : '',
        //resizable: true,
        // width:(obj!=null || obj!=undefined)?obj.width:50,
        width:tmpWidth + tmpUnit,
        elementwidth:tmpWidth,
        //elementheight:tmpHeight,
        unit: tmpUnit,
        cls : sectionclass,
        ctCls :containerclass,
            
        label : text===""?"&nbsp;":text,
        labelhtml:text===""?"&nbsp;":text,
        html: text===""?"&nbsp;":text,
        marginTop : marginTop,
        marginBottom:marginBottom,
        marginLeft:marginLeft,
        marginRight:marginRight,
        fontfamily:fontfamily,
        textalignclass: tmpTextAlignClass,
        fieldAlignment: fieldAlignment,
        textcolor: tmpTextcolor,
        textline: tmpTextLine,
        isBulletsApplied: isBulletsApplied,
        bulletType: bulletType,
        bold: tmpBold,
        iselementlevel:true,
        //height: (obj != null || obj != undefined) ? obj.height : 50,
        italic: tmpItalic,
        fontsize: tmpFontSize,
        style: {
            color: tmpTextcolor,
            'text-decoration': tmpTextLine,
            'font-weight': (tmpBold == "true") ? "bold" : '',
            'font-style': (tmpItalic == "true") ? "italic" : '',
            'font-size': tmpFontSize
        },
          
        listeners: {
            render: function (c) {
                c.el.dom.style.marginTop = marginTop.toString().replace('px','') + "px";        //ERP-19449
                c.el.dom.style.marginBottom = marginBottom.toString().replace('px','') + "px";  //ERP-19449
                c.el.dom.style.marginLeft = marginLeft.toString().replace('px','') + "px";      //ERP-19449
                c.el.dom.style.marginRight = marginRight.toString().replace('px','') + "px";    //ERP-19449
                c.el.dom.style.fontFamily = fontfamily;
                c.el.on('click', function (e) {
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    selectedElement = this.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    e.stopPropagation( );
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);
                });

                c.getEl().on("contextmenu", function (event, ele) {
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        if(Ext.get(selectedElement)!=null){
                            Ext.get(selectedElement).removeCls("selected");
                        }
                    }
                    selectedElement = this.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    event.stopEvent();
                    createContextMenu(Ext.fieldID.insertText,selectedElement);
                    contextMenuNew.showAt(event.getXY());
                    return false;
                });

            }
        }
    });
    tmpElement.addCls(tmpTextAlignClass);
    return tmpElement;
}
    
    /*Create Select Field*/
function createExtComponent_2(designerPanel, propertyPanel, fieldType, labelhtml,X, Y,selectfieldbordercolor, obj, label, xtype, isFormula,isNumeric) {

    var tmpTextAlignClass = (obj != null || obj != undefined) ? obj.textalignclass : "classTextAligment_left";
    var fieldAlignment = (obj != null || obj != undefined) ? obj.fieldAlignment : "1";
    var tmpTextColor = (obj != null || obj != undefined) ? obj.textcolor : '#000000';
    var tmpWidth = (obj != null || obj != undefined) ? obj.elementwidth : 100;
    var tmpUnit = (obj != null || obj != undefined) ? obj.unit : '%';
    var tmpFontSize = ((obj != null || obj != undefined) && obj.fontsize != undefined) ? obj.fontsize : '';
    var tmpHeight = (obj != null || obj != undefined) ? obj.elementheight : 20;
    var iselementlevel=(obj != null || obj != undefined) ? obj.iselementlevel :false; //flag to check whether component has inline or block.
    var marginTop = (obj && obj.marginTop) || (obj && obj.marginTop === 0)  ? obj.marginTop  : '0px';
    var marginBottom = (obj && obj.marginBottom) || (obj && obj.marginBottom === 0) ? obj.marginBottom : '5px';
    var marginLeft = (obj && obj.marginLeft)|| (obj && obj.marginLeft === 0) ? obj.marginLeft : '0px';
    var marginRight = (obj && obj.marginRight) || (obj && obj.marginRight === 0) ? obj.marginRight : '0px';
    var bold = (obj && obj.bold)?obj.bold:false;
    var italic = (obj && obj.italic)?obj.italic:false;
    var decimalPrecision = (obj && (obj.decimalPrecision || obj.decimalPrecision === 0))?obj.decimalPrecision:_amountDecimalPrecision;     // Done as decimal precision is not working for zero value.
    var containerclassfieldalignvalue=(designerPanel.fieldalignment!=null||designerPanel.fieldalignment!=undefined)?designerPanel.fieldalignment:"1";
    var ispretext = (obj && obj.isPreText)?obj.isPreText:false;
    var isposttext = (obj && obj.isPostText)?obj.isPostText:false;
    var preTextValue = (obj && obj.preTextValue)?obj.preTextValue:"preText";
    var postTextValue = (obj && obj.postTextValue)?obj.postTextValue:"postText";
    var preTextWordSpacing = (obj && obj.preTextWordSpacing)?obj.preTextWordSpacing:0;
    var postTextWordSpacing = (obj && obj.postTextWordSpacing)?obj.postTextWordSpacing:0;
    var postTextItalic = (obj && obj.postTextItalic)?obj.postTextItalic:false;
    var preTextItalic = (obj && obj.preTextItalic)?obj.preTextItalic:false;
    var postTextbold = (obj && obj.postTextbold)?obj.postTextbold:false;
    var preTextbold = (obj && obj.preTextbold)?obj.preTextbold:false;
    var fontfamily = (obj && obj.fontfamily) ? obj.fontfamily : '';
    var textDecoration = (obj && obj.textDecoration)?obj.textDecoration:"none";
    var defaultValue = (obj && obj.defaultValue)?obj.defaultValue:"";
    var dimensionorder = (obj && obj.dimensionorder)?obj.dimensionorder: undefined;
    var customfieldorder = (obj && obj.customfieldorder)?obj.customfieldorder: undefined;
    var valueSeparator = (obj && obj.valueSeparator)?obj.valueSeparator: "linebreak";
    var specificreccurrency = (obj && obj.specificreccurrency)?obj.specificreccurrency: "none";
    var dimensionValue = (obj && obj.dimensionValue)?obj.dimensionValue: "2";
    var valueWithComma = (obj && obj.valueWithComma)?obj.valueWithComma: false;
    var showzerovalueasblank = (obj && obj.showzerovalueasblank)?obj.showzerovalueasblank: false;
    var isNoWrapValue = (obj && obj.isnowrapvalue) ? obj.isnowrapvalue : false;
    var containerclass="sectionclass_field_container";
    var xType=xtype?xtype:"0";
    var isNumeric=isNumeric?isNumeric:false;
    var showAmountInWords = (obj && obj.showAmountInWords) ? obj.showAmountInWords : false;
    if(containerclassfieldalignvalue== "2"){
        containerclass='sectionclass_field_container_inline';
    } 
        
    var sectionclass="selectfield sectionclass_field"; //ERP-19850
    if(fieldAlignment== "2"){
        sectionclass='sectionclass_field sectionclass_field_container_inline selectfield'; //ERP-19850
    }
    var field = Ext.create('Ext.Component', {
        x:X,
        y:Y,
        width :tmpWidth + tmpUnit,
        //            height:'auto',
        labelhtml:labelhtml,
        isFormula:isFormula,
        label : label,
        borderColor:selectfieldbordercolor,
        textalignclass:tmpTextAlignClass,
        cls : sectionclass,
        ctCls : containerclass,
        textcolor:tmpTextColor,
        marginTop : marginTop,
        marginBottom:marginBottom,
        marginLeft:marginLeft,
        marginRight:marginRight,
        bold:bold,
        fontfamily:fontfamily,
        italic:italic,
        defaultValue:defaultValue,
        ispretext:ispretext,
        isposttext:isposttext,
        preTextValue:preTextValue,
        postTextValue:postTextValue,
        preTextWordSpacing:preTextWordSpacing,
        postTextWordSpacing:postTextWordSpacing,
        preTextbold:preTextbold,
        postTextbold:postTextbold,
        preTextItalic:preTextItalic,
        postTextItalic:postTextItalic,
        elementwidth:tmpWidth,
        elementheight:tmpHeight,
        unit:tmpUnit,
        fieldAlignment:fieldAlignment,
        textDecoration:textDecoration,
        fontsize:tmpFontSize,
        iselementlevel:iselementlevel,
        decimalPrecision:decimalPrecision,
        dimensionorder:dimensionorder,
        customfieldorder:customfieldorder,
        valueSeparator:valueSeparator,
        specificreccurrency:specificreccurrency,
        dimensionValue:dimensionValue,
        valueWithComma:valueWithComma,
        showzerovalueasblank:showzerovalueasblank,
        isNoWrapValue:isNoWrapValue,
        xType:xType,
        isNumeric:isNumeric,
        showAmountInWords: showAmountInWords,
        style : {
            /*borderColor:(selectfieldbordercolor!=null&&selectfieldbordercolor!='#B5B8C8')?selectfieldbordercolor:'#B5B8C8',*/
            color:(obj==null || obj==undefined)?'black':obj.textcolor,
            'font-size':tmpFontSize
        },
        //            draggable: true,
        initDraggable: function () {
            var me = this,
            ddConfig;
            //                             if (!me.header)
            //                             {
            //                                 me.updateHeader(true);
            //                             }
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
            //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        }, //initDraggable
        //resizable: true,
        autoDestroy: true,
        fieldType: fieldType,
        html: labelhtml,
        onRender: function () {
            this.superclass.onRender.call(this);
            //addPositionObjectInCollection(this);
            this.el.dom.style.marginTop = marginTop + "px";
            this.el.dom.style.marginBottom = marginBottom  + "px";
            this.el.dom.style.marginLeft = marginLeft  + "px"; 
            this.el.dom.style.marginRight = marginRight  + "px";
            this.el.dom.style.fontFamily = fontfamily;
            this.el.dom.style.textDecoration = textDecoration;
            if ( bold ) {
                this.el.dom.style.fontWeight = "bold" ;
            } else {
                this.el.dom.style.fontWeight = "normal" ;
            }
            if ( italic ) {
                this.el.dom.style.fontStyle = "italic" ;
            } else {
                this.el.dom.style.fontStyle = "normal" ;
            }
            if ( ispretext ) {
                addPrePostTextInSelectField(this,1);
                this.getEl().dom.children[0].innerHTML = preTextValue;
                this.getEl().dom.children[0].style.marginRight = preTextWordSpacing  + "px";
                if (preTextbold ) {
                    this.getEl().dom.children[0].style.fontWeight = "bold";
                }
                if ( preTextItalic ) {
                    this.getEl().dom.children[0].style.fontStyle = "italic";
                }
                if(this.fontsize != ""){ // ERP-19417
                    this.getEl().dom.children[0].style.fontSize = this.fontsize; // PreText
                    this.getEl().dom.children[1].style.fontSize = this.fontsize; // SelectField
                }
            }
            if ( isposttext ) {
                addPrePostTextInSelectField(this, 2);
                if (ispretext) {
                    this.getEl().dom.children[2].innerHTML = postTextValue;
                    this.getEl().dom.children[2].style.marginLeft = postTextWordSpacing + "px";
                    if (postTextbold) {
                        this.getEl().dom.children[2].style.fontWeight = "bold";
                    }
                    if (postTextItalic) {
                        this.getEl().dom.children[2].style.fontStyle = "italic";
                    }
                    if(this.fontsize != ""){ // ERP-19417
                        this.getEl().dom.children[2].style.fontSize = this.fontsize; // PostText
                    }
                } else {
                    this.getEl().dom.children[1].innerHTML = postTextValue;
                    this.getEl().dom.children[1].style.marginLeft = postTextWordSpacing + "px";
                    if (postTextbold) {
                        this.getEl().dom.children[1].style.fontWeight = "bold";
                    }
                    if (postTextItalic) {
                        this.getEl().dom.children[1].style.fontStyle = "italic";
                    }
                    if(this.fontsize != ""){ // ERP-19417
                        this.getEl().dom.children[0].style.fontSize = this.fontsize; // SelectField
                        this.getEl().dom.children[1].style.fontSize = this.fontsize; // PostText
                    }
                }
            }
            this.el.on('click', function (eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if (component) {
                    if (Ext.getCmp('contentImage'))
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    selectedElement = this.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    eventObject.stopPropagation( );
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);
                    // getPropertyPanel(component, designerPanel,propertyPanel);
                }

            });

            this.el.on("contextmenu", function(event, ele) {
                if (selectedElement != null || selectedElement != undefined)
                {
                    if(Ext.get(selectedElement)!=null){
                        Ext.get(selectedElement).removeCls("selected");
                    }
                }
                selectedElement = this.id;
                Ext.getCmp(selectedElement).addClass('selected');
                event.stopPropagation();
                event.stopEvent();
                createContextMenu(Ext.fieldID.insertField,this.id);
                contextMenuNew.showAt(event.getXY());
                return false;
            });



        },
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            }
            ,
            removed: function () {
            /*onDeleteUpdatePropertyPanel();*/
            //                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            },
            resize: function () {
            /*var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                     if(selectedfield != null){
                     Ext.getCmp('idWidth').setValue(selectedfield.getWidth());
        }
                     */
            }

        }
    });
    //field.on('drag',showAlignedComponent, field);
    //field.on('dragend',removeAlignedLine, field);
    field.addCls(tmpTextAlignClass);
    //        field.on('drag',showAlignedComponent, field);
    //        field.on('dragend',removeAlignedLine, field);    
    return field;
}//createExtComponent()

function createDataElementComponent(designerPanel, fieldType, dataelementhtml,X,Y,obj, label, selectfield,allowLabel,xtype, isNumeric) {
    var displaylabel = (obj && obj.labelval)?obj.labelval:(label?label:"");
    var select = (obj && obj.selectfield)?obj.selectfield:(selectfield?selectfield:"");
    var labelwidth = (obj && obj.labelwidth != undefined)?obj.labelwidth:40;
    var datawidth = (obj && obj.datawidth != undefined)?obj.datawidth:60;
    var componentwidth = (obj && obj.componentwidth != undefined)?obj.componentwidth:100;
    var labelbold = (obj && obj.labelbold)?obj.labelbold:false;
    var databold = (obj && obj.databold)?obj.databold:false;
    var labelitalic = (obj && obj.labelitalic)?obj.labelitalic:false;
    var dataitalic = (obj && obj.dataitalic)?obj.dataitalic:false;
    var labelfontsize = (obj && obj.labelfontsize!= undefined)?obj.labelfontsize:"";
    var datafontsize = (obj && obj.datafontsize!= undefined)?obj.datafontsize:"";
    var labelalign = (obj && obj.labelalign)?obj.labelalign:"left";
    var dataalign = (obj && obj.dataalign)?obj.dataalign:"left";
    var topmargin = (obj && obj.topmargin != undefined)?obj.topmargin:0;
    var bottommargin = (obj && obj.bottommargin != undefined)?obj.bottommargin:5;
    var leftmargin = (obj && obj.leftmargin != undefined)?obj.leftmargin:0;
    var rightmargin = (obj && obj.rightmargin != undefined)?obj.rightmargin:0;
    var labelfontfamily = (obj && obj.labelfontfamily) ? obj.labelfontfamily : '';
    var datafontfamily = (obj && obj.datafontfamily) ? obj.datafontfamily : '';
    var colontype = (obj && obj.colontype != undefined)?obj.colontype:-1;
    var allowlabel = allowLabel?allowLabel:((obj && obj.allowlabel)?obj.allowlabel:false);
    var customlabel = ((obj && obj.customlabel)?obj.customlabel:false);
    var textlinelabel = (obj != null || obj != undefined) ? obj.textlinelabel : "None";
    var textlinedata = (obj != null || obj != undefined) ? obj.textlinedata : "None";
    var textcolorlabel = (obj != null || obj != undefined) ? obj.textcolorlabel : '#000000';
    var textcolordata = (obj != null || obj != undefined) ? obj.textcolordata : '#000000';
    var fieldalignment = (obj != null || obj != undefined) ? obj.fieldalignment : "1";
    var decimalPrecision = (obj && obj.decimalPrecision!=undefined)?obj.decimalPrecision:_amountDecimalPrecision;
    var defaultValue = (obj && obj.defaultValue)?obj.defaultValue.replace(/&nbsp;/g,' '):"";
    var XType = xtype?xtype:(obj && obj.XType)?obj.XType:"";
    var valueSeparator = (obj && obj.valueSeparator)?obj.valueSeparator: "linebreak";
    var specificreccurrency = (obj && obj.specificreccurrency)?obj.specificreccurrency: "none";
    var dimensionValue = (obj && obj.dimensionValue)?obj.dimensionValue: "2";
    var valueWithComma = (obj && obj.valueWithComma)?obj.valueWithComma: false;
    var showzerovalueasblank = (obj && obj.showzerovalueasblank)?obj.showzerovalueasblank: false;
    var isNoWrapValue = (obj && obj.isnowrapvalue) ? obj.isnowrapvalue : false;
    var isNumeric = isNumeric ? isNumeric : false;
    
    var containerclassfieldalignvalue=(designerPanel.fieldalignment!=null||designerPanel.fieldalignment!=undefined)?designerPanel.fieldalignment:"1";
    var containerclass="sectionclass_field_container";
    if(containerclassfieldalignvalue== "2"){
        containerclass='sectionclass_field_container_inline';
    } 
    var sectionclass="sectionclass_data_element";
    if(fieldalignment== "2"){
        sectionclass='sectionclass_data_element_inline';
    } 
    var field = Ext.create('Ext.Component', {
        border: true,
        x:X,
        y:Y,
        width :componentwidth+"%",
        cls : sectionclass,
        ctCls:containerclass,
        labelval : displaylabel,
        selectfield:select,
        labelwidth:labelwidth,
        datawidth:datawidth,
        labelbold:labelbold,
        databold:databold,
        labelitalic:labelitalic,
        dataitalic:dataitalic,
        labelfontsize:labelfontsize,
        datafontsize:datafontsize,
        labelalign:labelalign,
        dataalign:dataalign,
        topmargin:topmargin,
        bottommargin:bottommargin,
        leftmargin:leftmargin,
        labelfontfamily:labelfontfamily,
        datafontfamily:datafontfamily,
        rightmargin:rightmargin,
        colontype:colontype,
        allowlabel:allowlabel,
        componentwidth:componentwidth,
        textlinelabel:textlinelabel,
        textlinedata:textlinedata,
        textcolorlabel:textcolorlabel,
        textcolordata:textcolordata,
        fieldalignment:fieldalignment,
        customlabel:customlabel,
        decimalPrecision:decimalPrecision,
        defaultValue:defaultValue,
        XType:XType,
        dataelementhtml:dataelementhtml,
        valueSeparator:valueSeparator,
        specificreccurrency:specificreccurrency,
        dimensionValue:dimensionValue,
        valueWithComma:valueWithComma,
        showzerovalueasblank:showzerovalueasblank,
        isNoWrapValue:isNoWrapValue,
        isNumeric:isNumeric,
        style: {
            'overflow':"hidden",
            'margin-bottom':"5px"
        },
        
        initDraggable: function () {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        },
        autoDestroy: true,
        fieldType: fieldType,
        html: dataelementhtml,
        onRender: function () {
            this.superclass.onRender.call(this);
            if(this.topmargin){
                this.getEl().dom.style.marginTop = this.topmargin+"px";
            }
            if(this.bottommargin){
                this.getEl().dom.style.marginBottom = this.bottommargin+"px";
            }
            if(this.leftmargin){
                this.getEl().dom.style.marginLeft = this.leftmargin+"px";
            }
            if(this.rightmargin){
                this.getEl().dom.style.marginRight = this.rightmargin+"px";
            }
            this.addClass("fieldItemDivBorder");
            this.el.on('click', function (eventObject, target, arg) {
                if (selectedElement != null || selectedElement != undefined)
                    {
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    selectedElement = this.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    eventObject.stopPropagation( );
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);
            });

            this.el.on("contextmenu", function(event, ele) {
                if (selectedElement != null || selectedElement != undefined)
                {
                    Ext.get(selectedElement).removeCls("selected");
                }
                selectedElement = this.id;
                Ext.getCmp(selectedElement).addClass('selected');
                event.stopEvent();
                createContextMenu(Ext.fieldID.insertDataElement,ele.parentNode.parentNode.id);
                contextMenuNew.showAt(event.getXY());
                return false;
            });



        },
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            }
        }
    });
    return field;
}

/* Create Image*/
function createExtImgComponent(designerPanel, propertyPanel, fieldTypeId, src, X, Y, obj) {
    var textalignclass=(obj.textalignclass!=null || obj.textalignclass!=undefined)?obj.textalignclass:"imageshiftleft";
    var iselementlevel=(obj != null || obj != undefined) ? obj.iselementlevel :false;
    var fieldAlignment = (obj != null || obj != undefined) ? obj.fieldAlignment : "1";
    var marginTop = (obj && obj.marginTop) || (obj && obj.marginTop === 0)  ? obj.marginTop  : '0px';
    var marginBottom = (obj && obj.marginBottom) || (obj && obj.marginBottom === 0) ? obj.marginBottom : '0px';
    var marginLeft = (obj && obj.marginLeft)|| (obj && obj.marginLeft === 0) ? obj.marginLeft : '0px';
    var marginRight = (obj && obj.marginRight) || (obj && obj.marginRight === 0) ? obj.marginRight : '0px';
        
    /* If container class value is given as inline change the component to inline*/
    var containerclassfieldalignvalue=(designerPanel.fieldalignment!=null||designerPanel.fieldalignment!=undefined)?designerPanel.fieldalignment:"1";
    var containerclass="sectionclass_field_container";
    if(containerclassfieldalignvalue== "2"){
        containerclass='sectionclass_field_container_inline';
    } 
    
    var sectionclass="sectionclass_field_without_border imagesectionclass_field_block";
            if(fieldAlignment== "2"){
                sectionclass='sectionclass_field_without_border imagesectionclass_field_inline';
            } 
        
    var field = Ext.create('Ext.Img', {
        width:  (obj.width != null || obj.width != undefined) ? obj.width : 'auto',
        height:  (obj.height != null || obj.height != undefined)? obj.height: 'auto',
        x: (obj != null || obj != undefined) ? obj.x : '',
        y: (obj != null || obj != undefined) ? obj.y : '',
        draggable: true,
        elementwidth:(obj.elementwidth!=null || obj.elementwidth!=undefined)?obj.elementwidth:'auto',
        elementheight:(obj!=null || obj!=undefined)?obj.elementheight:'auto',
        iselementlevel:iselementlevel,
        unit:(obj.unit!=null || obj.unit!=undefined)?obj.unit:'px',
        textalignclass: textalignclass,
        fieldAlignment:fieldAlignment,
        fieldType: fieldTypeId,
        marginTop : marginTop,
        marginBottom:marginBottom,
        marginLeft:marginLeft,
        marginRight:marginRight,
        cls : sectionclass,
        ctCls : containerclass,
        src: src,
        draggable:false,
        onRender: function () {
            this.superclass.onRender.call(this);
            // addPositionObjectInCollection(this);
            this.el.dom.style.marginTop = marginTop + "px";
            this.el.dom.style.marginBottom = marginBottom  + "px";
            this.el.dom.style.marginLeft = marginLeft  + "px"; 
            this.el.dom.style.marginRight = marginRight  + "px";
            this.el.on('click', function (eventObject, target, arg) {

                var component = designerPanel.queryById(this.id)
                if (component) {
                    if (Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    }
                    eventObject.stopPropagation();
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    selectedElement = this.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);
                }
            });
        },
        listeners: {
            //                move: function ( obj, x, y, eOpts ) {
            //                      var component = designerPanel.queryById(this.id)
            //                      selectedElement = this.id;
            //                      Ext.getCmp(selectedElement).x=x;
            //                      Ext.getCmp(selectedElement).y=y; 
            //                },
            onMouseUp: function (field) {
                field.focus();
            }
        }
    })
    field.addCls(textalignclass);
    return field;
}
   
   /*Create Box*/
    function createBox(obj)
    {
        var tmpWidth = (obj != null || obj != undefined) ? obj.width : 50;
        var tmpHeight = (obj != null || obj != undefined) ? obj.height : 50;
        var tmpX = (obj != null || obj != undefined) ? obj.x : 10;
        var tmpY = (obj != null || obj != undefined) ? obj.y : 10;

        var tmpElement = Ext.create("Ext.Panel", {
            //            height: tmpHeight,
            width: tmpWidth,
            x: tmpX,
            y: tmpY,
            border: true,
            draggable: true,
            resizable: true,
            fieldType: Ext.fieldID.insertDrawBox,
            style:
            {
                position: 'relative',
                border: '1px solid'
            },
            listeners: {
                render: function (c) {
                    c.el.on('click', function (e) {
                        if (selectedElement != null || selectedElement != undefined)
                        {
                            Ext.get(selectedElement).removeCls("selected");
                        }
                        selectedElement = this.id;
                        Ext.getCmp(selectedElement).addClass('selected');
                        e.stopPropagation( );
                        createPropertyPanel(selectedElement);

                    });
//                    c.getEl().on("contextmenu", function (event, ele) {
//                        event.stopEvent();
//                        contextMenu.showAt(event.getXY());
//                        return false;
//                    });
                    c.getEl().on("contextmenu", function(event, ele) {
                        if (selectedElement != null || selectedElement != undefined)
                        {
                            if(Ext.get(selectedElement)!=null){
                                Ext.get(selectedElement).removeCls("selected");
                            }
                        }
                        selectedElement = this.id;
                        Ext.getCmp(selectedElement).addClass('selected');
                        event.stopEvent();
                        createContextMenu(Ext.fieldID.insertImage,ele.id);
                        contextMenuNew.showAt(event.getXY());
                        return false;
                    });
                }
            }
        });
        return tmpElement;
    }
    
/*Create Line Item Table*/
    function getlineitemstable(PosX, PosY, html, designerPanel, obj, isSummaryTableApplied, summaryTable ,headerProperties) {
        /*When we import template and save it then template was not soving without edit line item table. 
         *Prbolem - jousap format HTML,thats why some extra space and line break add in HTML
         *For Remove that Extra space and line break below Regex use. 
         */
        html = html.replace(/\n*>\s*\n*\s*</g,"><");
        var fontsize="";
        var bold="false";
        var italic="false";
        var underline="false";
        var align="center";
        var bordercolor="";
        var width = 100;
        var marginTop = (obj && obj.marginTop) || (obj && obj.marginTop === 0) ? obj.marginTop : '0px';
        var marginBottom = (obj && obj.marginBottom) || (obj && obj.marginBottom === 0) ? obj.marginBottom : '5px';
        var marginLeft = (obj && obj.marginLeft) || (obj && obj.marginLeft === 0) ? obj.marginLeft : '0px';
        var marginRight = (obj && obj.marginRight) || (obj && obj.marginRight === 0) ? obj.marginRight : '0px';
        var islineitemrepeat = (obj && obj.islineitemrepeat) ? obj.islineitemrepeat : false;;
        var isconsolidated = (obj && obj.isconsolidated) ? obj.isconsolidated : false;;
        var fontfamily = (obj && obj.fontfamily) ? obj.fontfamily : '';
        if ( obj.includegroupingRowAfterId  === true ) {
            isFormattingRowPresent = true;
        }
        if ( obj.isFormattingApplied  === true ) {
            isFormattingApplied = true;
        }
        if ( obj.isGroupingApplied  === true ) {
            isGroupingApplied = true;
        }
        
        width = (obj && obj.tablewidth) ? obj.tablewidth : 100;
        align = (obj && obj.align) ? obj.align : 1;
        fontsize = (obj && obj.fontsize) ? obj.fontsize : 0;
        bordercolor =  (obj && obj.bordercolor) ? obj.bordercolor : "#FFFFFF";
//        var dimensionorder;
//        var customfieldorder;
        if (obj && obj.data && obj.data[0]) {
          fontsize = obj.data[0].fontsize ? obj.data[0].fontsize : "";
          bold = obj.data[0].bold ? "true" : "false";
          italic = obj.data[0].italic ? "true" : "false";
          underline = obj.data[0].underline ? "true" : "false";
          bordercolor = obj.data[0].bordercolor ? obj.data[0].bordercolor : "";
          var tempalign=obj.data[0].align?obj.data[0].align:"center";
            if(tempalign=="left" || tempalign===0){
                align=0;
            } else if(tempalign=="center" || tempalign===1 ){
                align=1;
            } else{
                align=2;
           }
           var lineitems = obj.data[0].lineitems;
//           for(var items = 0 ; items < lineitems.length; items++){
//               if(lineitems[items].columnname.toLowerCase() == "all line level dimensions" ){
//                   var alldimensioncolmn = lineitems[items];
//                   if(alldimensioncolmn.dimensionorder){
//                       dimensionorder = alldimensioncolmn.dimensionorder;
//                   }
//                   if(alldimensioncolmn.customfieldorder){
//                       customfieldorder = alldimensioncolmn.customfieldorder;
//                   }
//               }
//           }
        }
//        if(obj.dimensionorder){
//            dimensionorder = obj.dimensionorder;
//        }
        if(headerProperties){
            if(headerProperties.isbold){
                bold=headerProperties.isbold;
            }
            if(headerProperties.isitalic){
                italic=headerProperties.isitalic;
            }
            if(headerProperties.isunderline){
                underline=headerProperties.isunderline;
            }
            if(headerProperties.fontsize != undefined){
                fontsize=headerProperties.fontsize;
            }
            if(headerProperties.tablewidth != undefined){
                width=headerProperties.tablewidth;
            }
            if(headerProperties.marginBottom != undefined){
                marginBottom=headerProperties.marginBottom;
            }
            if(headerProperties.marginLeft != undefined){
                marginLeft=headerProperties.marginLeft;
            }
            if(headerProperties.marginRight != undefined){
                marginRight=headerProperties.marginRight;
            }
            if(headerProperties.marginTop != undefined){
                marginTop=headerProperties.marginTop;
            }
            if(headerProperties.bordercolor){
                bordercolor=headerProperties.bordercolor;
            }
            if(headerProperties.align != undefined){
                align=headerProperties.align;
            }
        }
        // Columns present in old line items
        var columns = [];
        if(obj && !obj.columns && obj.data){
            columns = obj.data[0].lineitems;
            for(var i= 0 ; i < columns.length ; i++){
                columns[i].seq = i+1;
                if(!columns[i].columnname){
                    columns[i].columnname = columns[i].label;
                }
                if(!columns[i].displayfield){
                    if(obj.data[0].columndata){
                        var columnData = eval("(" + obj.data[0].columndata + ")");
                        for(var j = 0 ; j< columnData.length ; j++){
                            if(columnData[j].columnname == columns[i].columnname){
                                if(columnData[j].displayfield){
                                    columns[i].displayfield = columnData[j].displayfield;
                                }
                                break;
                            }
                        }
                    }
                    if(!columns[i].displayfield){
                        columns[i].displayfield = columns[i].label;
                    }
                }
            }
        } else{
            columns = documentLineColumns.length?documentLineColumns:(obj && obj.columns)?obj.columns:[];
        }
        documentLineColumns = columns;
        var field = Ext.create(Ext.Component, {
            x: PosX,
            y: PosY,
            id: 'itemlistcontainer',
            isSummaryTableApplied : isSummaryTableApplied,
            summaryTableJson : summaryTable,
            draggable: false,
            fontsize:fontsize,
            fontfamily:fontfamily,
            bold:bold,
            marginTop : marginTop,
            marginBottom:marginBottom,
            marginLeft:marginLeft,
            marginRight:marginRight,
            islineitemrepeat:islineitemrepeat,
            isconsolidated:isconsolidated,
            italic:italic,
            underline:underline,
            tablewidth:width,
            align:align,
            bordercolor:bordercolor,
            includeProductCategory:obj.includeProductCategory,
            includegroupingRowAfterId:obj.includegroupingRowAfterId,
            sortfield:obj.sortfield,
            sortfieldxtype: obj.sortfieldxtype,
            sortorder:obj.sortorder,
            groupingRowHTMl:obj.groupingRowHTMl?obj.groupingRowHTMl:"",
            groupingRowAfterHTML:obj.groupingRowAfterHTML?obj.groupingRowAfterHTML:"",
            isExtendLineItem:obj.isExtendLineItem?obj.isExtendLineItem:false,
            pageSize:obj.pageSize?obj.pageSize:"a4",
            pageOrientation:obj.pageOrientation?obj.pageOrientation:"portrait",
            adjustPageHeight:obj.adjustPageHeight?obj.adjustPageHeight:"0",
            columns:columns,
//            dimensionorder:dimensionorder,
            initDraggable: function () {
                var me = this,
                ddConfig;
                ddConfig = Ext.applyIf({
                    el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                //delegate: '#' + Ext.escapeId(me.header.id)
                }, me.draggable);
                me.dd = new Ext.util.ComponentDragger(this, ddConfig);
                me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
            }, //initDraggable
            containerId: designerPanel.id,
            fieldType: Ext.fieldID.insertTable,
            resizable: false,
            cls : 'sectionclass_field_without_border sectionclass_element_100',
            ctCls : 'sectionclass_field_container',
            unit: (obj != null || obj != undefined) ? obj.unit : '',
            //            style: {
            ////                width: (obj != null || obj != undefined) ? obj.width + '' + obj.unit : '',
            //                height: 'auto !important',
            //                borderColor: '#B5B8C8',
            //                borderStyle: 'solid',
            //                borderWidth: '1px',
            //                position: 'relative'
            //            },
            fieldDefaults: {
                anchor: '100%'
            },
            layout: {
                type: 'vbox',
                align: 'stretch'  // Child items are stretched to full width
            },
            //            bordercolor: 'black',
            html: html, // this string is compared. Search with 'customize line items
            onRender: function () {
                this.superclass.onRender.call(this);
                addPositionObjectInCollection(this);
                this.el.dom.style.marginTop = marginTop + "px";
                this.el.dom.style.marginBottom = marginBottom  + "px";
                this.el.dom.style.marginLeft = marginLeft  + "px"; 
                this.el.dom.style.marginRight = marginRight  + "px";
                this.el.dom.style.width = width  + "%";
                var table = document.getElementById('itemlistconfigsectionPanelGrid');
                if(table){
                    table.setAttribute("panelid", designerPanel.id);
                }
                if(obj && obj.data){
                    if(obj.data[1]){
                        createGlobalFieldsForLineTable(JSON.stringify(obj.data[1]),field,true);
                    }
                    if(obj.data[2]){
                        createGlobalFieldsForLineTable(JSON.stringify(obj.data[2]),field,false);
                    }
                }
                this.el.on('click', function (eventObject, target, arg) {
                    var component = designerPanel.queryById(this.id)
                    if (component) {
                        //                        if (Ext.getCmp('contentImage')) {
                        //                            Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                        //                        }
                        eventObject.stopPropagation();
                        if (selectedElement != null || selectedElement != undefined)
                        {
                            if(Ext.get(selectedElement)){
                            Ext.get(selectedElement).removeCls("selected");
                        }
                        }
                        selectedElement = this.id;
                        Ext.getCmp(selectedElement).addClass('selected');
                        createPropertyPanel(selectedElement);
                        setProperty(selectedElement);
                        showElements(selectedElement);
                    }
                });
                this.el.on("contextmenu", function (event, ele) {
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        if(Ext.get(selectedElement)!=null){
                            Ext.get(selectedElement).removeCls("selected");
                        }
                    }
                    event.stopEvent();
                    var id = ele.parentNode.parentNode.parentNode.attributes["panelid"].value;
                    selectedElement = id;
                    createContextMenu(Ext.fieldID.insertTable,id);
                    contextMenuNew.showAt(event.getXY());
                    return false;
                });
            }, //onRender
            listeners: {
                onMouseUp: function (field) {
                    field.focus();
                },
                removed: function () {

                },
                afterrender: function () {
                    this.el.on('click', function (eventObject, target, arg) {
                        var component = designerPanel.queryById(this.id)
                        if (component && Ext.getCmp('contentImage')) {
                            Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                            eventObject.stopPropagation();

                        }
                    });
                }
            }
        });
        return field;
    }

Ext.onReady(function () {
    Ext.pagenumberlabel="Page Number"
    Ext.pagenumberid="pagenumberspan"

    //    var defaultFieldGlobalStore;
    Ext.fieldID = {
        insertText: 1,
        insertField: 2,
        insertImage: 3,
        insertHLine: 4,
        insertVLine: 5,
        insertDrawBox: 6,
        insertTable: 11,
        insertGlobalTable: 12,
        insertRowPanel: 13,
        insertColumnPanel: 14,
        insertTabSpacer: 15,
        pagePanel: 16,
        insertDataElement: 17,
        insertAgeingTable: 18,
        insertGroupingSummaryTable: 19,
        insertDetailsTable: 20

    }
    /*Moduleid Subtypes*/
    Ext.moduleID = {
        Acc_Invoice_ModuleId: 2,
        Acc_Vendor_Invoice_ModuleId: 6,
        Acc_Cash_Sales_ModuleId: 4,
        Acc_Debit_Note_ModuleId: 10,
        Acc_Credit_Note_ModuleId: 12,
        Acc_Make_Payment_ModuleId : 14,
        Acc_Receive_Payment_ModuleId: 16,
        Acc_Purchase_Order_ModuleId: 18,
        Acc_Sales_Order_ModuleId: 20,
        Acc_Customer_Quotation_ModuleI: 22,
        Acc_Vendor_Quotation_ModuleId: 23,
        Acc_Delivery_Order_ModuleId :27,
        Acc_Goods_Receipt_ModuleId : 28,
        Acc_Sales_Return_ModuleId : 29,
        Acc_Purchase_Return_ModuleId : 31,
        Acc_Purchase_Requisition_ModuleId : 32,
        Acc_RFQ_ModuleId : 33,
        Acc_Customer_AccStatement_moduleid : 60,
        Acc_Vendor_AccStatement_moduleid : 61,
        Acc_MRP_Sales_Order_ModuleId: 62,
        Acc_FixedAssets_PurchaseRequisition_ModuleId : 87,
        Acc_FixedAssets_Vendor_Quotation_ModuleId : 89,
        Acc_Stock_Repair_Report_ModuleId : 247,   
        Acc_QA_Approval_ModuleId :132,
        Build_Assembly_Report_ModuleId :133,
        Bank_Reconciliation_ModuleId : 124,
        MRP_WORK_ORDER_MODULEID : 1105
    } 
    Ext.countryid = {
        USA: "244",
        INDIA: "105"
    }
 /*modules subtypes*/
    Ext.Subtype = {
        Sales:0,
        Purchase: 0,
        Consignment:1,
        Default: 0,
        SalesReturn : 1,
        Undercharge : 7,
        Overcharge : 8,
        PurchaseReturn: 1,
        JobOrder: 3,
        JobOrderLabel: 4,
        OpeningInvoice : 5
    }

    Ext.FieldType = {
        allgloballeveldimensions:'all global level dimensions',
        alllineleveldimensions: 'all line level dimensions',
        allgloballevelcustomfields: 'all global level customfields',
        alllinelevelcustomfields: 'all line level custom fields',
        alldimensions: 'all dimensions',
        allgloballeveldimensionslabel:'allgloballeveldimensions',
        alllineleveldimensionslabel: 'alllineleveldimensions',
        allgloballevelcustomfieldslabel: 'allgloballevelcustomfields',
        alllinelevelcustomfieldslabel: 'alllinelevelcustomfields',
        alldimensionslabel: 'alldimensions'
    }

Ext.ComponenetId = {
        idPropertyPanelRoot : 'idPropertyPanelRoot'
    }
    // Define dfault header
    Ext.define('HeaderData', {
        extend: 'Ext.data.Model',
        fields: ['id', 'label', 'dbcolumnname', 'reftablename', 'reftablefk', 'reftabledatacolumn', 'dummyvalue', 'xtype', 'customfield', 'isNumeric']
    });
    Ext.define('DimensionAndCustomData', {
        extend: 'Ext.data.Model',
        fields: ['fieldname', 'fieldlabel', 'iscustom', 'isline', 'customlabel',{name: 'sequence', type: 'int'}]
    });
 

    var westPanel = {
        xtype: "panel",
        title: 'Tree View',
        id: 'idWestPanel',
        region: "west",
        split: true,
        width: 200,
        border:false,
        collapsible: true,
        resizable: false,
        autoHeight: true,
        autoScroll:true,
        defaults: {
//            height: 70,
            width: 80
        },
        items: [treePanel]
    };

    var topPanel = {
        xtype: "panel",
        region: "north",
        split: true,
        resizable: false,
        autoHeight: true,
        defaults: {
            height: 70,
            width: 80
        },
        items: [
            Ext.create("Ext.Button", {
            id: "idPanel",
            hidden : _isdefaulttemplate === 'true' ? true : false,
            iconCls: 'insertSection',            
            listeners: {
                click: function (c) {
                    var tmpElement = createPanel();

                    var targetPanel;
                    if (selectedElement == null || selectedElement == undefined)
                        targetPanel = Ext.getCmp("idMainPanel");
                    else
                        targetPanel = Ext.getCmp(selectedElement);

                    if(tmpElement){
                        targetPanel.add(tmpElement);
                        initTreePanel();
                    }
                //targetPanel.insert(0,tmpElement);
                }
            }
        }),
        Ext.create("Ext.Button", {
            id: "idSelectField",
            iconCls: 'selectFieldImg',
            autoHeight: true,
            scale: 'large',
            hidden : _isdefaulttemplate === 'true' ? true : false,
            scope: this,
            handler: function () {
                var containerpanel = Ext.getCmp(selectedElement); 
                if ( containerpanel ) {
                    var itemlength =  containerpanel.items.items.length;
                    for ( var index = 0 ; index < itemlength ; index++ ) {
                        if ( containerpanel.items.items[index].fieldType == 11 ) {
                            WtfComMsgBox(["Warning", "Cannot add any element in the row containing Line item."], 0);
                            return;
                        }
                    }
                }
                createField(Ext.fieldID.insertField);
            }
        }),
        Ext.create("Ext.Button", {
            id: "idStaticTextBtn",
            iconCls: 'insertTextImg',
            autoHeight: true,
            hidden : _isdefaulttemplate === 'true' ? true : false,
            scale: 'large',
            scope: this,
            listeners: {
                click: function (c) {
                    var containerpanel = Ext.getCmp(selectedElement); 
                    if ( containerpanel ) {
                        var itemlength =  containerpanel.items.items.length;
                        for ( var index = 0 ; index < itemlength ; index++ ) {
                            if ( containerpanel.items.items[index].fieldType == 11 ) {
                                WtfComMsgBox(["Warning", "Cannot add any element in the row containing Line item."], 0);
                                return;
                            }
                        }
                    }
                    var dlg = Ext.MessageBox.prompt('Name', 'Please enter value:', function (btn, text) {
                        if (btn == 'ok')
                        {
                            if(text === '' || (text === ' ' && text.length === 1)){
                                text = '&nbsp;';
                            }
                            text = text.replace(/\s\s/g," "+space);                            
                            var tmpElement = createTextField(Ext.getCmp("idMainPanel"),text, null);
                            var targetPanel;
                            if (selectedElement == null || selectedElement == undefined)
                                targetPanel = Ext.getCmp("idMainPanel");
                            else {
                                if(Ext.getCmp(selectedElement).items.items.length === 0){
                                    Ext.getCmp(selectedElement).update("");
                                }
                                targetPanel = Ext.getCmp(selectedElement);
                            }
                            targetPanel.add(tmpElement);
                            initTreePanel();
                        }
                    });
                }
            }
        }),
        Ext.create("Ext.Button", {
            id: "idInsertDataElementBtn",
            iconCls: 'insertDataElementImg',
            autoHeight: true,
            hidden : _isdefaulttemplate === 'true' ? true : false,
            scale: 'large',
            scope: this,
            listeners: {
                click: function (c) {
                    var containerpanel = Ext.getCmp(selectedElement); 
                    if ( containerpanel ) {
                        var itemlength =  containerpanel.items.items.length;
                        for ( var index = 0 ; index < itemlength ; index++ ) {
                            if ( containerpanel.items.items[index].fieldType == 11 ) {
                                WtfComMsgBox(["Warning", "Cannot add any element in the row containing Line item."], 0);
                                return;
                            }
                        }
                    }
                        var form = new Ext.form.FormPanel({
                            frame: true,
                            labelWidth: 175,
                            autoHeight: true,
                            bodyStyle: 'padding:5px 5px 0;position:relative;',
                            autoWidth: true,
                            defaults: {
                                width: 300
                            },
//                            defaultType: 'textfield',
                            items: [
                                {
                                    xtype: 'combo',
                                    fieldLabel: 'Select Field',
                                    store: defaultFieldGlobalStore,
                                    id: 'customdesignerfieldselectcombo',
                                    displayField: 'label',
                                    valueField: 'id',
                                    queryMode: 'local',
                                    width: 300,
                                    blankText: 'Select a field',
                                    listeners: {
                                        scope: this,
                                        'select': function(combo, selection) {
                                            if(selection[0].data.xtype != "NAN"){
                                                Ext.getCmp('idCreateBtn').setDisabled(false);
                                                var htmllabel = selection[0].data.label
                                                this.label = htmllabel;
                                                this.xType = selection[0].data.xtype;
                                                this.isNumeric = selection[0].data.isNumeric;
                                                if(Ext.getCmp('idcreatelabel')){
                                                    Ext.getCmp('idcreatelabel').setValue(htmllabel);
                                                }
                                            }
                                        }
                                    }
                                },{
                                    xtype:'checkbox',
                                    fieldLabel: 'Allow Label',
                                    id: 'idallowlabel',
                                            name: 'allowlabel',
                                            checked:false,
                                            width: 300,
                                            listeners: {
                                                scope: this,
                                                change: function(field, nval, oval) {
                                                    if(Ext.getCmp('idcreatelabel')){
                                                        if(nval==true){
                                                            Ext.getCmp('idcreatelabel').setDisabled(false);
                                                        } else{
                                                            Ext.getCmp('idcreatelabel').setDisabled(true);

                                                        }
                                                    }
                                                }
                                            }
                                },{
                                    xtype:'textfield',
                                    fieldLabel: 'Label',
                                    id: 'idcreatelabel',
                                    name: 'createlabel',
                                    value: '',
                                    readOnly:true,
                                    disabled:true,
                                    width: 300,
                                    allowBlank: false
                                }]
                        });
                    var win = new Ext.Window({
                        title:'Add Data Element',
                        closable: true,
                        width: 400,
                        id: 'idInsertDataElementWindow',
                        autoHeight: true,
                        plain: true,
                        modal: true,
                        items: form,
                        buttons:[
                            {
                                text : 'Create',
                                scope:this,
                                id:'idCreateBtn',
                                disabled:true,
                                handler : function(){
//                                    var label = Ext.getCmp('idcreatelabel').getValue();
                                    
                                    var label = Ext.getCmp('idcreatelabel').getValue();
                                    var allowlabel = Ext.getCmp('idallowlabel').getValue();
                                    var labelElement = document.createElement("label");
                                    labelElement.innerHTML = label;
                                    
                                    var span1 = document.createElement("span");
                                    span1.innerHTML = labelElement.outerHTML;
                                    span1.setAttribute("class", "fieldItemDivBorder");
                                    span1.setAttribute("style", "width:40%;float:left;position:relative;");
                                    
                                    var span2 = document.createElement("span");
                                    span2.innerHTML = "#" + this.label + "#";
                                    span2.setAttribute("class", "fieldItemDivBorder");
                                    if(allowlabel){
                                        span2.setAttribute("style", "width:60%;float:left;");
                                    }else{
                                        span2.setAttribute("style", "width:100%;float:left;");
                                    }
                                    span2.setAttribute("attribute","{PLACEHOLDER:"+ Ext.getCmp('customdesignerfieldselectcombo').getValue() +"}");
                                    
                                    if (Ext.getCmp('customdesignerfieldselectcombo').getValue().toLowerCase() == "pagenumberfield") {
                                        span2.setAttribute("id", "pagenumberspan");
                                    }
                                    var fieldTypeId = Ext.fieldID.insertDataElement;
                                    var X = Ext.getCmp(selectedElement).cursorCustomX;
                                    var Y = Ext.getCmp(selectedElement).cursorCustomY;
                                    
                                    var finalElement = document.createElement("div");
                                    finalElement.setAttribute("type", "dataElement");
                                    if(allowlabel){
                                        finalElement.innerHTML += span1.outerHTML;
                                    }
                                    finalElement.innerHTML += span2.outerHTML;
                                    
                                    var rec = searchRecord(defaultFieldGlobalStore, Ext.getCmp('customdesignerfieldselectcombo').getValue(), 'id');
                                    var xtype = "";
                                    if(rec && rec.data && rec.data.xtype){
                                        xtype = rec.data.xtype;
                                    }
                                    
                                    var fieldElement = createDataElementComponent(Ext.getCmp(selectedElement), fieldTypeId, finalElement.outerHTML, X, Y, null, label,Ext.getCmp('customdesignerfieldselectcombo').getValue(),allowlabel, xtype, this.isNumeric);
                                    
                                    if(Ext.getCmp(selectedElement).items.items.length === 0){
                                       Ext.getCmp(selectedElement).update("");
                                    }
                                    Ext.getCmp(selectedElement).add(fieldElement);//ERP-12570
                                    this.label = undefined;
                                    //  this.resetCursorPosition();
                                    Ext.getCmp('idInsertDataElementWindow').close();
                                    Ext.getCmp(selectedElement).doLayout();
                                    win.destroy();
                                    initTreePanel();
                                }
                            },
                            {
                                text : 'Cancel',
                                scope:this,
                                handler : function(){
                                    Ext.getCmp('idInsertDataElementWindow').close();
                                }
                            }
                        ]
                    });
                    win.show();
                    
                }
            }
        }),
        Ext.create("Ext.Button", {
            id: "idBoxtBtn",
            iconCls: 'drawBoxImg',
            autoHeight: true,
            scope: this,
            hidden:true,
            scale: 'large',
            listeners: {
                click: function (c) {
                    var containerpanel = Ext.getCmp(selectedElement); 
                    if ( containerpanel ) {
                        var itemlength =  containerpanel.items.items.length;
                        for ( var index = 0 ; index < itemlength ; index++ ) {
                            if ( containerpanel.items.items[index].fieldType == 11 ) {
                                WtfComMsgBox(["Warning", "Cannot add any element in the row containing Line item."], 0);
                                return;
                            }
                        }
                    }
                    var tmpElement = createBox(null);
                    var targetPanel;
                    targetPanel = Ext.getCmp(selectedElement);
                    targetPanel.add(tmpElement);
                    targetPanel.doLayout();
                    initTreePanel();
                }
            }
        }), {
            xtype: 'button',
            id: "idImageBtn",
            iconCls: 'insertImg',
            autoHeight: true,
            hidden : _isdefaulttemplate === 'true' ? true : false,
            scale: 'large',
            scope: this,
            handler: function () {
                var containerpanel = Ext.getCmp(selectedElement); 
                if ( containerpanel ) {
                    var itemlength =  containerpanel.items.items.length;
                    for ( var index = 0 ; index < itemlength ; index++ ) {
                        if ( containerpanel.items.items[index].fieldType == 11 ) {
                            WtfComMsgBox(["Warning", "Cannot add any element in the row containing Line item."], 0);
                            return;
                        }
                    }
                }
                new Ext.imageUploadWin({
                    containerPanel: Ext.getCmp(selectedElement),
                    fieldType: Ext.fieldID.insertImage,
                    XPos: 10,
                    YPos: 10,
                    parentScope: this,
                    listeners : {
                        beforeclose: function () {
                            initTreePanel();
                        }
                    }
                }).show();
                    
            }
        }, {
            xtype: 'button',
            id: "idInlineTableBtn",
            iconCls: 'inserttable',
            hidden : _isdefaulttemplate === 'true' ? true : false,
            autoHeight: true,
            scale: 'large',
            scope: this,
            handler: function () {
                var containerpanel = Ext.getCmp(selectedElement); 
                if ( containerpanel ) {
                    var itemlength =  containerpanel.items.items.length;
                    for ( var index = 0 ; index < itemlength ; index++ ) {
                        if ( containerpanel.items.items[index].fieldType == 11 ) {
                            WtfComMsgBox(["Warning", "Cannot add any element in the row containing Line item."], 0);
                            return;
                        }
                    }
                }
                var dtable = new Ext.designtable({
                    parentScope: this,
                    listeners: {
                        beforeclose: function () {
                            initTreePanel();
                        }
                    }
                });
                dtable.show();
            }
        }, Ext.create("Ext.Button", {
            iconCls: 'saveImg',
            style: 'margin:2px 0px 2px 2px',
            autoHeight: true,
            hidden : _isdefaulttemplate === 'true' ? true : false,
            scale: 'large',
            handler: function () {
                saveDocument(false);
            }
        }), Ext.create("Ext.Button", {
            iconCls: 'previewImg',
            style: 'margin:2px 0px 2px 2px',
            autoHeight: true,
            hidden : _isdefaulttemplate === 'true' ? true : false,
            scale: 'large',
            handler: function () {
                saveDocument(true);
            }
        }),Ext.create("Ext.Button", {
            iconCls: 'samplePreviewImg',
            style: 'margin:2px 0px 2px 2px',
            autoHeight: true,
            scale: 'large',
            handler: function () {
                previewDocument();
            }
        }),{
            xtype: 'button',
            iconCls: 'pageLayoutImg',
            style : 'margin:2px 0px 2px 2px',
            autoHeight: true,
            hidden:true,
            scale: 'large',
            handler : function() {
                var winObj = new Ext.PageLayoutPropWin({
                    isnewdocumentdesigner: true
                });
                winObj.show();
            }
        }, Ext.create("Ext.Button", {
            id: "idDelete",
            iconCls: 'removeObjImg',
            autoHeight: true,
//            hidden : _isdefaulttemplate === 'true' ? true : false,
            hidden:true,
            scope: this,
            scale: 'large',
            listeners: {
                click: function (c) {
                    //var targetPanel =Ext.getCmp("idMainPanel");
                    summaryTableJson = null;
                    isSummaryTableApplied = false;
                    if (selectedElement != "idMainPanel")
                    {
                        var targetPanel = Ext.getCmp(selectedElement).ownerCt;
                        if(Ext.getCmp(selectedElement).fieldType == Ext.fieldID.insertRowPanel){//if row is deleted then the global Header flag should be reset-ERP-12572
                            if(Ext.getCmp(selectedElement).isheader==true){
                                headerId=null;
                            }
                              if(Ext.getCmp(selectedElement).isfooter==true){ //if row is deleted then the globalFooter flag should be
                                footerId=null;
                            }
                            var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);//Resetting the property panel
                            var propertyPanel = Ext.getCmp("idPropertyPanel");
                            if (propertyPanel)
                                propertyPanelRoot.remove(propertyPanel.id);
                            propertyPanelRoot.doLayout();
                            var mainPanel = Ext.getCmp("idMainPanel");
                            mainPanel.remove(Ext.getCmp(selectedElement));
                        }else if(targetPanel.column=='1' && Ext.getCmp(selectedElement).fieldType=="14"){//if row has one column and selected element is column
                            var lineitems=Ext.getCmp(selectedElement).items.items;
                            
                            for(var i=0;i<lineitems.length;i++){
                                if(lineitems[i].fieldType==Ext.fieldID.insertTable){//selected one column has line item reseting pagelayoutproperty 
                                     pagelayoutproperty[1] = saveTableProperty("#000000","borderstylemode1","#FFFFFF");
                                }
                            }
                            var mainPanel = Ext.getCmp("idMainPanel");
                            mainPanel.remove(targetPanel);
                            showElements("idMainPanel");
                            selectedElement = "idMainPanel";
                            createPropertyPanel("idMainPanel");
                            setProperty("idMainPanel");
                        }else if(targetPanel.column!='1' && Ext.getCmp(selectedElement).fieldType=="14"){//if row has more than one column then resize column
                            targetPanel.remove(selectedElement);
                            targetPanel.column=(targetPanel.column-1);
                            setColumns(targetPanel,targetPanel.items.length,null,true);
                            showElements(targetPanel.id);
                            selectedElement = targetPanel.id;
                            createPropertyPanel(targetPanel.id)
                            targetPanel.addClass("selected");
                            setProperty(targetPanel.id);
                        }else{
                            if(Ext.getCmp(selectedElement).fieldType==Ext.fieldID.insertTable){
                                // resetting pagelayoutproperty 
                                pagelayoutproperty[1] = saveTableProperty("#000000","borderstylemode1","#FFFFFF");
                            }
                            targetPanel.remove(selectedElement);
                        }
                        initTreePanel();
                    }
                }
            }
        }), Ext.create("Ext.Button", {
            iconCls: 'removeAllObjects',
            id: "idRemoveAllElements",
            autoHeight: true,
//            hidden : _isdefaulttemplate === 'true' ? true : false,
            hidden:true,
            scope: this,
            scale: 'large',
            listeners: {
                click: function (c) {
                    var targetPanel = Ext.getCmp("idMainPanel");
                    targetPanel.removeAll();
                    initTreePanel();
                    summaryTableJson = null;
                    isSummaryTableApplied = false;
                    headerId=null;
                    footerId=null;
                }
            }
        }), {
            xtype: 'button',
            id: "idImportGlobalHeaderBtn",
            iconCls: 'importGlobalHeader',
            autoHeight: true,
            hidden : _isdefaulttemplate === 'true' ? true : false,
            scale: 'large',
            scope: this,
            handler: function () {
                importGlobalHeaderFooter(1);
            }
        }, {
            xtype: 'button',
            id: "idImportGlobalFooterBtn",
            iconCls: 'importGlobalFooter',
            autoHeight: true,
            hidden : _isdefaulttemplate === 'true' ? true : false,
            scale: 'large',
            scope: this,
            handler: function () {
                importGlobalHeaderFooter(0);
            }
        }
        ]

    };
    
    var mainPanel = {
        xtype: "panel",
        id: "idMainPanel",
        baseCls: 'designerPanel',
        region: "center",
        split: true,
        autoScroll: true,
        fieldType: Ext.fieldID.pagePanel,
        size: 'a4',
        orientation: 'landscape',
        params: {
            moduleid: "1",
            templateid: "1"
        },
        /*style: {
            height: 842,
            width: 595
        },*/
        listeners: {
            render: function (c) {
                var url = "CustomDesign/getDesignTemplate.do";
                if((parseInt(_CustomDesign_moduleId) === Ext.moduleID.Acc_Invoice_ModuleId || parseInt(_CustomDesign_moduleId) === Ext.moduleID.Acc_Sales_Order_ModuleId)
                    && (parseInt(_CustomDesign_templateSubtype) === 3 || parseInt(_CustomDesign_templateSubtype) === 4)){
                    url = "CustomDesign/getJobOrderDesignTemplate.do";
                } else if(parseInt(_CustomDesign_moduleId) === Ext.moduleID.Acc_QA_Approval_ModuleId){
                    url = "CustomDesign/getQAApprovalDesignTemplate.do";
                }
                Ext.Ajax.request({
                    url: url,
                    // url: "DocumentDesignController/loadDocument.do",
                    method: 'POST',
                    params: {
                        bandID: "1",
                        moduleid: _CustomDesign_moduleId,
                        templateid: _CustomDesign_templateId
                    },
                    success: function (response, req) {
                        var result = Ext.decode(response.responseText);
                        if (result.success && isValidSession(result)) {
                            var resData = result.data.data[0];
//                            var arr2 = [];
//                            for (var j = 0; j < resData.defaultfield.length; j++) {
//                            var data = resData.defaultfield[j];
//                            var header="";
//                            if(data.xtype=='NAN'){
//                                var header = headerCheck(HTMLStripper1(data.label));
//                                header = header.replace("*", "");
//                                header = header.trim();
//                            }else{
//                                header=data.label;
//                            }
//                            var arr1 = [data.id,header,data.dbcolumnname,data.reftablename,data.reftablefk,data.reftabledatacolumn,data.dummyvalue,data.xtype,data.customfield]
//                            arr2.push(arr1);
//                        }
                            var json = resData.jsonBody
                            defaultFieldGlobalStore = new Ext.create('Ext.data.Store', {
                                model: 'HeaderData',
                                data: resData.defaultfield
                            });
                            defaultFieldGlobalStoreForSearchRecord = new Ext.create('Ext.data.Store', {
                                model: 'HeaderData',
                                data: resData.defaultfield
                            });
                            defaultDimensionAndCustomStore = new Ext.create('Ext.data.Store', {
                                model: 'DimensionAndCustomData',
                                data: resData.dimensionandcustom
                            });
                            defaultDimensionAndCustomArray = resData.dimensionandcustom;
                            
                            if(resData.summaryTable!=undefined && resData.summaryTable!=""){
                               summaryTableJson =  resData.summaryTable;
                               isSummaryTableApplied = true;
                            }
                            /*Setting Font-Family for Page(ERP-12465)*/
                            if(resData.pagelayoutproperty!=undefined && resData.pagelayoutproperty!=""){
                                var pagelay = eval(resData.pagelayoutproperty);
                                if(pagelay[0]==null||pagelay[0]==undefined) {
                                    pagelayoutproperty[0]={};
                                } else {
                                    pagelayoutproperty[0]=pagelay[0];
                                    if( pagelayoutproperty[0].pagelayoutsettings){
                                       var pagefont = pagelayoutproperty[0].pagelayoutsettings.pagefont;
                                        Ext.getCmp("idMainPanel").body.setStyle('font-family', '' + pagefont + '');
                                    }
                                }
                                
                                if(pagelay[1]==null||pagelay[1]==undefined) {
                                    pagelayoutproperty[1]={};
                                } else {
                                    pagelayoutproperty[1]=pagelay[1];
                                }
                                
                                pagelayoutproperty[2] = pagelay[2];
                            }
                            documentLineColumnsArray = resData.linecolumns;
                            ageingColumnsArray=resData.ageingcolumns;
                            loadDocument();
                        }
                    }
                }) // End of Ajax Request

                c.el.on('click', function (e) {
                    e.stopPropagation();
                    if (selectedElement != null || selectedElement != undefined){
                        if(Ext.get(selectedElement)){
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    }
                    selectedElement = this.id;
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);
                });
            }
        }
    };

    var rootCenter = {
        xtype: "panel",
        id: "rootCenter",
        region: "center",
        split: true,
        autoScroll: true,
        layout: "border",
        items: [
        /*{
                xtype: 'panel',
                height: 25,
                border: false,
                style: 'background-color: #FFFFFF;',
                region: 'north',
                id: 'sectionhorizontalRuler'
            },*/ mainPanel/*,
            {
                region: 'west',
                width: 25,
                height: 1200,
                border: false,
                id: 'sectionverticalRuler'
            }*/

        ],
        listeners: {
            render: function (c) {
                c.el.on('click', function (e) {
                    selectedElement = this.id;
                    e.stopPropagation( );
                    showElements(selectedElement);

                });
            }
        }
    };

    var eastPanel = {
        xtype: "panel",
        title: "Property Panel",
        collapsible: true,
        split: true,
        region: "east",
        autoScroll:true,
        id: 'idEastPanel',
        width: 280,
        items: [
        Ext.create("Ext.Panel", {
            //height: 400,
            border: false,
            autoScroll:true,
            id: Ext.ComponenetId.idPropertyPanelRoot

        })]
    };

    var viewport = new Ext.Viewport({
        layout: "border",
        resizable: false,
        items: [topPanel, rootCenter, westPanel, eastPanel],
        renderTo: Ext.getBody()
    });
 });
 var contextMenu = Ext.create('Ext.menu.Menu', {
        items: [
        {
            text: 'Column',
            handler: function () {
                var tmpElement = null;
                tmpElement = Ext.getCmp(selectedElement);
                if (tmpElement.fieldType == Ext.fieldID.insertColumnPanel)
                {
                    return;
                }
                else if (tmpElement.fieldType != Ext.fieldID.insertRowPanel && tmpElement.fieldType != Ext.fieldID.insertColumnPanel)
                {
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    tmpElement = Ext.getCmp(selectedElement).ownerCt;
                    selectedElement = tmpElement.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);

                }
                return true;

            }
        },
        {
            text: 'Row',
            handler: function () {
                var tmpElement = null;
                tmpElement = Ext.getCmp(selectedElement);
                if (tmpElement.fieldType == Ext.fieldID.insertRowPanel)
                {
                    return;
                }

                else if (tmpElement.fieldType == Ext.fieldID.insertColumnPanel)
                {
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    selectedElement = Ext.getCmp(tmpElement.id).ownerCt;
                    selectedElement = selectedElement.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);

                }
                else if (tmpElement.fieldType != Ext.fieldID.insertRowPanel && tmpElement.fieldType != Ext.fieldID.insertColumnPanel)
                {
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    tmpElement = Ext.getCmp(selectedElement).ownerCt;
                    selectedElement = Ext.getCmp(tmpElement.id).ownerCt;
                    selectedElement = selectedElement.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);
                }
                return true;
            }
        },
        {
            text: 'Page',
            handler: function () {
                var tmpElement = null;
                tmpElement = Ext.getCmp(selectedElement);
                if (tmpElement.fieldType == Ext.fieldID.pagePanel)
                {
                    return;

                }
                else if (tmpElement.fieldType == Ext.fieldID.insertColumnPanel)
                {
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    selectedElement = Ext.getCmp(tmpElement.id).ownerCt;
                    selectedElement = selectedElement.ownerCt.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);

                }
                else if (tmpElement.fieldType != Ext.fieldID.insertRowPanel && tmpElement.fieldType != Ext.fieldID.insertColumnPanel)
                {
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    tmpElement = Ext.getCmp(selectedElement).ownerCt;
                    selectedElement = Ext.getCmp(tmpElement.id).ownerCt;
                    selectedElement = selectedElement.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);
                }
                return true;
            }
        }, 
        {
            text:"Paste",
            handler: function() {
                if (Ext.getCmp(selectedElement).fieldType == Ext.fieldID.insertColumnPanel) {

                    var Json = elementJson;
                    var field;
                    var columnPanel = Ext.getCmp(selectedElement);
                    if (Json.fieldType == Ext.fieldID.insertText)
                    {
                        field = createTextField(columnPanel, Json.labelhtml, Json);

                    }
                    else if (Json.fieldType == Ext.fieldID.insertField)
                    {
                        var xType = Json.xType ? Json.xType : "0";
                        field = createExtComponent_2(columnPanel, undefined, Json.fieldType, Json.labelhtml, Json.x, Json.y, null, Json, Json.label, xType, false);
                    } else if (Json.fieldType == Ext.fieldID.insertGlobalTable)
                    {
                        var borderedgetype = Json.borderedgetype!=null?Json.borderedgetype:"1";
                        var rowspacing = Json.rowspacing!=null?Json.rowspacing:"5";
                        var columnspacing = Json.columnspacing!=null?Json.columnspacing:"5"; 
                        var fieldAlignment = Json.fieldAlignment!=null?Json.fieldAlignment:"1"; 
                        var fixedrowvalue=Json.fixedrowvalue!=null?Json.fixedrowvalue:"";
                        var tableHeader=Json.tableHeader!=null?Json.tableHeader:false;
                        field = createGlobalTable(Json.x, Json.y, Json.labelhtml, columnPanel, Json.tableWidth, Json.height,fixedrowvalue, Ext.fieldID.insertGlobalTable,borderedgetype,rowspacing,columnspacing,fieldAlignment,Json,tableHeader);

                    }
                    columnPanel.add(field);
                    columnPanel.doLayout();
                    initTreePanel();
                }
            }
        },
        {
            text:"Remove",
            handler: function() {
                if(getEXTComponent(selectedElement).items.items.length > 0 && getEXTComponent(selectedElement).items.items[0].fieldType == Ext.fieldID.insertTable){ //ERP-19380 : When we remove line table and again insert it,its data is not refreshed.
                    documentLineColumns=[];
                }
                if(getEXTComponent(selectedElement).items.items.length > 0 && getEXTComponent(selectedElement).items.items[0].fieldType == Ext.fieldID.insertAgeingTable){ //ERP-19380 : When we remove line table and again insert it,its data is not refreshed.
                    ageingColumns=[];
                }
                if(getEXTComponent(selectedElement).items.items.length > 0 && getEXTComponent(selectedElement).items.items[0].fieldType == Ext.fieldID.insertGroupingSummaryTable){
                    groupingSummaryColumns=[];
                }
                //remove columns info of deleting details table
                if(getEXTComponent(selectedElement).items.items.length > 0 && getEXTComponent(selectedElement).items.items[0].fieldType == Ext.fieldID.insertDetailsTable){
                    detailsTableColumnsObj[Ext.getCmp(selectedElement).detailsTableId] = [];
                }
               removeObject(selectedElement); 
            }
        }
        ]
    });
    
    var contextMenuNew = "";
    function createContextMenu(fieldType,elementId) {
            contextMenuNew = Ext.create('Ext.menu.Menu', {
               items:[
                {
                   text:(fieldType == Ext.fieldID.insertField)?"Copy Field":"Copy Label",
                   hidden:(fieldType == Ext.fieldID.insertField || fieldType == Ext.fieldID.insertText)?false:true,
                   handler: function() {
                       elementJson = createJson(Ext.getCmp(elementId));
                   }
                },
                {
                   text:"Remove",
                   handler: function() {
                       if(fieldType == Ext.fieldID.insertTable){
                           documentLineColumns=[];
                       }
                       if(fieldType == Ext.fieldID.insertAgeingTable){
                           ageingColumns=[];
                       }
                       if(fieldType == Ext.fieldID.insertGroupingSummaryTable){
                           groupingSummaryColumns=[];
                       }
                       //remove columns info of deleting details table
                       if(fieldType == Ext.fieldID.insertDetailsTable){
                           detailsTableColumnsObj[Ext.getCmp(selectedElement).detailsTableId] = [];
                       }
                       removeObject(elementId);
                   }
                }
               ] 
            });
    }
    
 
    function createField(fieldTypeId) {
        var isFormulaFunctionHidden = false;
        if((_CustomDesign_moduleId == Ext.moduleID.Acc_Sales_Order_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Invoice_ModuleId) && (_CustomDesign_templateSubtype == Ext.Subtype.JobOrder || _CustomDesign_templateSubtype == Ext.Subtype.JobOrderLabel)){
            isFormulaFunctionHidden = true;
        }
            
        var form = new Ext.form.FormPanel({
            frame: true,
            labelWidth: 175,
            autoHeight: true,
            bodyStyle: 'padding:5px 5px 0;position:relative;',
            autoWidth: true,
            defaults: {
                width: 300
            },
            defaultType: 'textfield',
            items: [{
                xtype: 'radiogroup',
                fieldLabel: 'Field Type',
                id: 'fieldtypeid',
                width: 370,
                labelWidth:70,
                columns: 2,
                vertical: true,
                items: [{
                    boxLabel: 'Select Field',
                    name: 'fieldtype' , 
                    inputValue: '1' ,
                    checked: true ,
                    listeners: {
                        change: function (cb, nv, ov) {
                            if(nv){
                                getEXTComponent("customdesignerfieldselectcombo").show();
                            } else{
                                getEXTComponent("customdesignerfieldselectcombo").hide();
                            }
                        }
                    }
                } , {
                    boxLabel: 'Create Formula', 
                    name: 'fieldtype', 
                    inputValue:'2',
                    hidden: isFormulaFunctionHidden,
                    listeners: {
                        change: function (cb, nv, ov) {
                            if(nv){
                                this.createFormulaWindow = openFormulaBuilderWindow(fieldTypeId, true, false, false);
                                this.createFormulaWindow.show();
//                                getEXTComponent("customdesignerfieldselectcombo").hide();
                            } else{
                                getEXTComponent("customdesignerfieldselectcombo").show();
                            }
                        }
                    }
                }]
            },{
                fieldLabel: 'Field Label',
                id: 'customdesignerfieldlabel',
                name: 'fieldlabel',
                value: '',
                width: 300,
                //                    maskRe: /[A-Za-z0-9_: ]+/,
                hidden: fieldTypeId != Ext.fieldID.insertText,
                hideLabel: fieldTypeId != Ext.fieldID.insertText,
                allowBlank: false
            }, {
                xtype: 'combo',
                fieldLabel: 'Select Field',
                store: defaultFieldGlobalStore,
                id: 'customdesignerfieldselectcombo',
                displayField: 'label',
                valueField: 'id',
                queryMode: 'local',
                hidden: fieldTypeId != Ext.fieldID.insertField ,
                hideLabel: fieldTypeId != Ext.fieldID.insertField ,
                width: 370,
                labelWidth:70,
                blankText: 'Select a field',
                allowBlank: fieldTypeId != Ext.fieldID.insertField ,
                listeners: {
                    scope: this,
                    'select': function (combo, selection) {
                    if(selection[0].data.xtype!="NAN"){
                        var htmllabel = selection[0].data.label
                        this.label = htmllabel;
                        this.xType = selection[0].data.xtype;
                        this.isNumeric = selection[0].data.isNumeric;
                        var applycolorcomponent = Ext.getCmp('selectfieldapplycolor');
                        applycolorcomponent.enable();
                        getEXTComponent('idcreate').enable();
                    }
                }
            }
        }, {
                xtype: 'fieldset',
                checkboxToggle: true,
                title: 'Apply Border Color',
                autoHeight: true,
                id: 'selectfieldapplycolor',
                disabled: fieldTypeId == Ext.fieldID.insertText ? false : true,
                collapsed: true,
                hidden:true,
                width: 150,
                autoHeight:true,
                fieldDefaults: {
                    labelAlign: 'left',
                    labelWidth: 105
                },
                items: [
                this.selectColorPalette = new Ext.ColorPalette({
                    cls: 'palette',
                    value: "",
                    id: 'fieldbordercolor',
                    scope: this,
                    height: 70,
                    colors: ["FFFFFF", "CC3333", "DD4477", "994499", "6633CC", "336699", "3366CC", "22AA99", "329262", "109618", "66AA00", "AAAA11", "D6AE00", "EE8800", "DD5511", "A87070", "8C6D8C", "627487", "7083A8", "5C8D87", "898951", "B08B59", "000000", "008B8B", "00FFFF", "4B0082", "6B8E23", "808080", "87CEEB", "87CEFA", "ADFF2F"]
                })]
            }],
            buttons: [{
                text: 'Create',
                disabled:true,
                id:'idcreate',
                handler: function () {
                    var label = this.label;
                    var checklabel = Ext.getCmp('customdesignerfieldselectcombo').getValue().toLowerCase();
                    createSelectField(label, checklabel, fieldTypeId, false); // isFormula is always false from here
                    //  this.resetCursorPosition();
                    Ext.getCmp('enterfieldlabelwin').close();
                    Ext.getCmp(selectedElement).doLayout();
                    win.destroy();
                    initTreePanel();
                },
                scope: this
            }, {
                text: 'Close',
                handler: function () {
                    Ext.getCmp('enterfieldlabelwin').close();
                },
                scope: this
            }]
        });
        var win = new Ext.Window({
            title: fieldTypeId == Ext.fieldID.insertText ? 'Add Text' : 'Select Field',
            closable: true,
            width: 400,
            id: 'enterfieldlabelwin',
            autoHeight: true,
            plain: true,
            modal: true,
            items: form
        });
        win.show();
    }

function createSelectField(label, checklabel, fieldTypeId, isFormula){
    var selectedapplyfieldcolor = Ext.getCmp('fieldbordercolor') == undefined ? undefined : Ext.getCmp('fieldbordercolor').getValue();
    if (selectedapplyfieldcolor) {
        var res = selectedapplyfieldcolor.match(/#/g);
        if (res == null) {
            selectedapplyfieldcolor = "#" + selectedapplyfieldcolor;
        }
    }
    var labelHTML =""; 
    if ( checklabel.toLowerCase() == "pagenumberfield" ) {
        labelHTML=(fieldTypeId != Ext.fieldID.insertField ) ? label : "<span id=\"pagenumberspan\" attribute='{PLACEHOLDER:" + checklabel + "}'>#" + label + "#</span>";
    } else if(checklabel==Ext.FieldType.allgloballeveldimensionslabel || checklabel==Ext.FieldType.alllineleveldimensionslabel || checklabel==Ext.FieldType.allgloballevelcustomfieldslabel || checklabel==Ext.FieldType.alllinelevelcustomfieldslabel || checklabel==Ext.FieldType.alldimensionslabel){
        labelHTML = "<span style='display:table;width:100%;' attribute='{PLACEHOLDER:" + checklabel + "}'>#" + label + "#</span>";
    } else {
        if(isFormula){
            labelHTML=(fieldTypeId != Ext.fieldID.insertField ) ? label : "<span decimalprecision=2 isformula='true' attribute='{PLACEHOLDER:" + checklabel + "}'>#" + label + "#</span>";
        } else{
            labelHTML=(fieldTypeId != Ext.fieldID.insertField ) ? label : "<span attribute='{PLACEHOLDER:" + checklabel + "}'>#" + label + "#</span>";
        }
    }
    var newLabel = (fieldTypeId == Ext.fieldID.insertField ) ? label : "#" + label + "#";
    var xtype = this.xType;
    var isNumeric = this.isNumeric;
    if(isFormula){
        xtype = "2";
    }
    fieldTypeId = Ext.fieldID.insertField ;
    var field = createExtComponent_2(Ext.getCmp(selectedElement), undefined, fieldTypeId, labelHTML, Ext.getCmp(selectedElement).cursorCustomX, Ext.getCmp(selectedElement).cursorCustomY,selectedapplyfieldcolor,null, newLabel, xtype, isFormula,isNumeric);
    this.label = undefined;
    field.isFormula = isFormula;
    if(Ext.getCmp(selectedElement).items.items.length === 0){
        Ext.getCmp(selectedElement).update("");
    }
    Ext.getCmp(selectedElement).add(field);//ERP-12570
}

    function createPanel(obj)
    {
        /*Row*/
        var isPrePrinted = false;
        if(pagelayoutproperty[0].pagelayoutsettings){
            isPrePrinted = pagelayoutproperty[0].pagelayoutsettings.ispreprinted;
            isExtendedGlobalTable = pagelayoutproperty[0].pagelayoutsettings.isExtendedGlobalTable;
            adjustPageHeight = pagelayoutproperty[0].pagelayoutsettings.adjustPageHeight;
            pageSizeForExtGT = pagelayoutproperty[0].pagelayoutsettings.pageSizeForExtGT;
            pageOrientationForEXTGT = pagelayoutproperty[0].pagelayoutsettings.pageOrientationForEXTGT;
        }
        
        if(isPrePrinted && (obj==null || obj==undefined)){
            var isSpace = calculatepageheight();
            if(!isSpace){
                WtfComMsgBox(["Page size exceeds", "No space available on page."], 0);
                return false;
            }
        }
        var tmpElement = Ext.create("Ext.Panel", {
            fieldType: Ext.fieldID.insertRowPanel,
            column: (obj == null || obj == undefined) ? 1 : obj.column,
            autoHeight: !isPrePrinted,
//            rowHeight: (obj && obj.rowHeight)?obj.rowHeight:1, // in PX
            rowHeight: (obj && obj.rowHeight)?obj.rowHeight:1, // in CM
            flex: 1,
            //dragGroup: 'myPanelDropTarget',
            layout:'column',
            cls : "sectionclass_parent",
            autoScroll:true,
            isJoinNextRow:(obj && obj.isJoinNextRow)?obj.isJoinNextRow:false,
            isheader:(obj==null || obj==undefined)?false:obj.isheader,
            isfooter:(obj==null || obj==undefined)?false:obj.isfooter,
            isPrePrinted: isPrePrinted,
            listeners: {
                render: function (c) {

                    if ((obj != null || obj != undefined) && obj.isheader == true)
                        headerId = obj.id;

                    if ((obj != null || obj != undefined) && obj.isfooter == true)
                        footerId = obj.id;
                    
                    if ( obj && obj.isJoinNextRow ) {
                        c.addCls("joinnextrow");
                    } else {
                        c.removeCls("joinnextrow");
                    }
                    c.getEl().on('click', function (e) {
                        if (selectedElement != null || selectedElement != undefined)
                        {
                            Ext.get(selectedElement).removeCls("selected");
                        }
                        selectedElement = this.id;
                        Ext.getCmp(selectedElement).addClass('selected');
                        e.stopPropagation( );
                        createPropertyPanel(selectedElement);
                        setProperty(selectedElement);
                        showElements(selectedElement);
                    });

                    c.getEl().on("contextmenu", function (event, ele) {
                        if (selectedElement != null || selectedElement != undefined)
                        {
                            if(Ext.get(selectedElement)!=null){
                                Ext.get(selectedElement).removeCls("selected");
                            }
                        }
                        selectedElement = this.id;
                        Ext.getCmp(selectedElement).addClass('selected');
                        event.stopEvent();
//                        selectedElement = ele.parentNode.parentNode.parentNode.id;
                        contextMenu.showAt(event.getXY());
                        return false;
                    });
                }
            }
        });


        for (var i = 0; i < 1; i++)
        {
            var innerPanel = Ext.create("Ext.Panel", {
                fieldType: Ext.fieldID.insertColumnPanel,
                columnwidth: 100,
                columnWidth: 1,
                //resizable: true,
                autoHeight: true,
                marginTop : 0,
                marginLeft : 0,
                marginBottom : 0,
                marginRight : 0,
                cls : "sectionclass_element",
                flex: 1,
                html: space,
                listeners:
                {
                    render: function (c) {
                        c.el.dom.style.marginTop = "0px";
                        c.el.dom.style.marginBottom = "0px";
                        c.el.dom.style.marginLeft = "0px"; 
                        c.el.dom.style.marginRight = "0px";
                        c.getEl().on('click', function (e) {

                            if (selectedElement != null || selectedElement != undefined)
                            {
                                Ext.get(selectedElement).removeCls("selected");
                            }
                            selectedElement = this.id;
                            Ext.getCmp(selectedElement).addClass('selected');
                            e.stopPropagation( );
                            createPropertyPanel(selectedElement);
                            setProperty(selectedElement);
                            showElements(selectedElement);

                        });
                        c.getEl().on("contextmenu", function (event, ele) {
                            if (selectedElement != null || selectedElement != undefined)
                            {
                                if(Ext.get(selectedElement)!=null){
                                    Ext.get(selectedElement).removeCls("selected");
                                }
                            }
                            selectedElement = this.id;
                            Ext.getCmp(selectedElement).addClass('selected');
                            event.stopEvent();
//                            selectedElement = ele.parentNode.parentNode.parentNode.id;
                            contextMenu.showAt(event.getXY());
                            return false;
                        });


                },
                'resize': function () {
                // var panel = this;
                // panel.doLayout();
                }
                }
            });

            tmpElement.add(innerPanel);
        }
        return tmpElement;
    }
    

    /* Add column to row after load document*/
    function addColumToRow(parent, colArr)
    {
        var newColArr = [];
        var obj = parent;
        var rowHeight = obj.rowHeight;      
        var isPrePrinted = false;
        
        if(pagelayoutproperty[0].pagelayoutsettings){
            isPrePrinted = pagelayoutproperty[0].pagelayoutsettings.ispreprinted;
        }
        
        obj.removeAll();
        
        var widthClass = " sectionclass_element_100"
        
        switch(colArr.length){
            case 1 :
                widthClass = " sectionclass_element_100";
                break;
            case 2 :
                widthClass = " sectionclass_element_50";
                break;
            case 3 :
                widthClass = " sectionclass_element_33";
                break;
            case 4 :
                widthClass = " sectionclass_element_25";
                break;
            case 5 :
                widthClass = " sectionclass_element_20";
                break;
        }

        for (var i = 0; i < colArr.length; i++)
        {
            var tmpColObj = colArr[i];
            var containerclass="sectionclass_element";
            var colWidth = tmpColObj.columnwidth;
            var fieldalignment=tmpColObj.fieldalignment!=null?tmpColObj.fieldalignment:"1";
            if(fieldalignment== "2"){
                containerclass='sectionclass_element sectionclass_element_inline';
            } 
            var type = tmpColObj.type!=null?tmpColObj.type:"Auto";
            var borderedgetype = tmpColObj.borderedgetype!=null?tmpColObj.borderedgetype:"1";
            var allowborder = tmpColObj.allowborder!=null?tmpColObj.allowborder:false;
            var backgroundColor = tmpColObj.backgroundColor!=null?tmpColObj.backgroundColor:"#FFFFFF"
            var linespacing = tmpColObj.linespacing!=null?tmpColObj.linespacing:5;
            var marginTop = (tmpColObj && tmpColObj.marginTop) || (tmpColObj && tmpColObj.marginTop === 0)  ? tmpColObj.marginTop  : '0px';
            var marginBottom = (tmpColObj && tmpColObj.marginBottom) || (tmpColObj && tmpColObj.marginBottom === 0) ? tmpColObj.marginBottom : '0px';
            var marginLeft = (tmpColObj && tmpColObj.marginLeft)|| (tmpColObj && tmpColObj.marginLeft === 0) ? tmpColObj.marginLeft : '0px';
            var marginRight = (tmpColObj && tmpColObj.marginRight) || (tmpColObj && tmpColObj.marginRight === 0) ? tmpColObj.marginRight : '0px';
            var isJoinPreviousColumn = (tmpColObj && tmpColObj.isJoinPreviousColumn)?tmpColObj.isJoinPreviousColumn:false;
            var innerPanel = Ext.create("Ext.Panel", {
                // id:tmpColObj.id,
                fieldType: Ext.fieldID.insertColumnPanel,
                columnwidth: colWidth,
                columnWidth: colWidth / 100,
                height: tmpColObj.height,
                fieldalignment:fieldalignment,
                borderedgetype:borderedgetype,
                allowborder:allowborder,
                marginTop : marginTop,
                marginBottom:marginBottom,
                marginLeft:marginLeft,
                marginRight:marginRight,
                backgroundColor:backgroundColor,
                linespacing:linespacing,
                isJoinPreviousColumn:isJoinPreviousColumn,
                //resizable: true,
                type:type,
                autoHeight: true,
                scope: this,
                //cls: containerclass +  widthClass, //+ (allowborder?"elementBorder":""),
                cls : containerclass + ( (i==0) ? "  " : " section_left_margin "),
                flex: 1,
                style: {
                    'height': '100%',
                    'width': 'auto'
                },
                listeners:
                {
                    render: function (c) {
                        c.addCls('sectionclass_element_'+colWidth);
                        c.el.dom.style.marginTop = marginTop + "px";
                        c.el.dom.style.marginBottom = marginBottom  + "px";
                        c.el.dom.style.marginLeft = marginLeft  + "px"; 
                        c.el.dom.style.marginRight = marginRight  + "px";
                        if(isPrePrinted){
//                            c.el.dom.children[0].children[0].children[0].style.setProperty("height",rowHeight+"px","important");
                            c.el.dom.children[0].children[0].children[0].style.setProperty("height",rowHeight+"cm","important");
                            c.el.dom.children[0].children[0].children[0].style.setProperty("overflow","hidden","");
                        }
                        
                        c.getEl().on('click', function (e) {
                            if (selectedElement != null || selectedElement != undefined)
                            {
                                Ext.get(selectedElement).removeCls("selected");
                            }
                            selectedElement = this.id;
                            Ext.getCmp(selectedElement).addClass("selected");
                            e.stopPropagation( );
                            createPropertyPanel(selectedElement);
                            setProperty(selectedElement);
                            showElements(selectedElement);

                        });
                        c.getEl().on("contextmenu", function (event, ele) {
                            if (selectedElement != null || selectedElement != undefined)
                            {
                                if(Ext.get(selectedElement)!=null){
                                    Ext.get(selectedElement).removeCls("selected");
                                }
                            }
                            selectedElement = this.id;
                            Ext.getCmp(selectedElement).addClass('selected');
                            event.stopEvent();
//                            selectedElement = ele.parentNode.parentNode.parentNode.id;
                            contextMenu.showAt(event.getXY());

                            return false;
                        });
                        var ele1 = Ext.get(c.id+"-innerCt");
                        if ( c.allowborder ) {
                            ele1.addCls('elementBorder');
                        }
                        if ( c.borderedgetype == 2 ) {
                            ele1.addCls('elementBorderRound');
                        }
                        if ( c && c.isJoinPreviousColumn ) {
                            c.addCls("joinpreviouscolumn");
                        } else {
                            c.removeCls("joinpreviouscolumn");
                        }
                        ele1.setStyle('background',c.backgroundColor);

                    },
                    'resize': function () {
                    // var panel = this;
                    // panel.doLayout();
                    }
                }
            });
            newColArr.push(innerPanel);
            obj.add(innerPanel);
        }
        obj.doLayout();
        return newColArr;
    }


    

    //    function createColumnPropertyPanel(ele) {
    //        var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    //        var propertyPanel = Ext.getCmp("idPropertyPanel");
    //
    //        if (propertyPanel)
    //            propertyPanelRoot.remove(propertyPanel.id);
    //
    //        var elementId = {
    //            xtype: 'textfield',
    //            fieldLabel: 'Element ID',
    //            id: 'idElementId',
    //            //disabled:true,
    //            readOnly: true
    //        };
    //
    //        var type = {
    //            xtype: 'combo',
    //            fieldLabel: 'Type',
    //            id: 'idType',
    //            store: Ext.create("Ext.data.Store", {
    //                fields: ["id", "type"],
    //                data: [
    //                    {
    //                        id: "1",
    //                        type: "Auto"
    //                    }, {
    //                        id: "2",
    //                        type: "Custom"
    //
    //                    }
    //                ]
    //            }),
    //            value: 'Auto',
    //            displayField: 'type',
    //            valueField: 'id',
    //            width: 220,
    //            labelWidth: 100,
    //            listeners: {
    //                select: function (e) {
    //                    if (this.getRawValue() == "Custom")
    //                    {
    //                        Ext.getCmp("idWidth").setDisabled(false);
    //                        Ext.getCmp("idUnit").setDisabled(false);
    //                    }
    //                    else
    //                    {
    //                        Ext.getCmp("idWidth").setDisabled(true);
    //                        Ext.getCmp("idUnit").setDisabled(true);
    //                    }
    //                }
    //
    //            }
    //
    //        };
    //
    //        var width = {
    //            xtype: 'numberfield',
    //            fieldLabel: 'Width',
    //            id: 'idWidth',
    //            labelWidth: 100,
    //            value: 0,
    //            width: 170,
    //            minLength: 1,
    //            maxLength: 3,
    //            disabled: true
    //
    //
    //        };
    //        var unit = {
    //            xtype: 'combo',
    //            fieldLabel: '    Unit',
    //            id: 'idUnit',
    //            store: Ext.create("Ext.data.Store", {
    //                fields: ["id", "unit"],
    //                data: [
    //                    {
    //                        id: "1",
    //                        unit: "%"
    //                    }
    //                ]
    //            }),
    //            value: '%',
    //            displayField: 'unit',
    //            valueField: 'id',
    //            width: 100,
    //            labelWidth: 30,
    //            disabled: true
    //        };
    //        
    //        var fieldAlignment = {
    //            xtype: 'combo',
    //            fieldLabel: 'Field(s) Alignment',
    //            id: 'fields_Alignment',
    //            store: Ext.create("Ext.data.Store", {
    //                fields: ["id", "type"],
    //                data: [
    //                    {
    //                        id: "1",
    //                        type: "Block"
    //                    }, {
    //                        id: "2",
    //                        type: "Inline"
    //
    //                    }
    //                ]
    //            }),
    //            value: (ele.fieldalignment!=null||ele.fieldalignment!=undefined)?ele.fieldalignment:'1', //on render & selection
    //            displayField: 'type',
    //            valueField: 'id',
    //            width: 220,
    //            labelWidth: 100,
    //            listeners: {
    //                select: function (field) {
    //                     updateProperty(selectedElement);
    //                }
    //                }
    //        };
    //
    //        var updateBtn = {
    //            xtype: 'button',
    //            text: 'Update',
    //            id: 'idUpdateBtn',
    //            width: 55,
    //            style: {
    //                'float': 'right',
    //                'margin-right': '25px'
    //
    //            },
    //            listeners: {
    //                'click': function ()
    //                {
    //                    updateProperty(selectedElement);
    //
    //                }
    //            }
    //
    //        };
    //
    //        propertyPanel = Ext.create("Ext.Panel", {
    //            height: 200,
    //            width: 300,
    //            border: false,
    //            //layout:'column',
    //            id: 'idPropertyPanel',
    //            padding: '5 5 5 5',
    //            items: [
    //                elementId, type,
    //                {
    //                    xtype: 'panel',
    //                    width: 300,
    //                    layout: 'column',
    //                    border: false,
    //                    items: [
    //                        width, unit, fieldAlignment
    //                    ]
    //                }
    //            ]
    //        });
    //        propertyPanel.add(updateBtn);
    //        return propertyPanel;
    //    }




    function getElementJson(designerPanel,typeFlag) {
        var panelItems = [];
        if ( typeFlag == 1) {
            panelItems.push(designerPanel);
        } else {
            panelItems  = designerPanel.items.items;
        }
        var items = [];
        var jsonObj = [];

        for (var cnt = 0; cnt < panelItems.length; cnt++) { // Iterate on Sections
            var obj_1 = panelItems[cnt];
            jsonObj = createJson(obj_1);
            for (var i = 0; i < obj_1.items.items.length; i++)  {// Iterate on Section Columns
                var obj_2 = obj_1.items.items[i];
                var innerJsonObj_1 = createJson(obj_2);
                if (obj_2.items && obj_2.items.items && obj_2.items.items.length > 0) {
                    for (var j = 0; j < obj_2.items.items.length; j++) { // Iterate on Column items
                        var obj_3 = obj_2.items.items[j];
                        var innerJsonObj_2 = createJson(obj_3);
                    var firstRowCells=[];
                    var lastRowCells=[];
                    if( obj_3.fieldType == Ext.fieldID.insertTable ){
                        var firstrow = obj_3.firstRowCells;
                        var lastrow = obj_3.lastRowCells;
                        if ( firstrow ) {
                            for (var k = 0; k < firstrow.length; k++) { // Iterate on Column items
                                var obj_5 = firstrow[k];
                                var innerJsonObj_3 = createJson(obj_5);
                                if (obj_5.items && obj_5.items.items && obj_5.items.items.length > 0) {
                                    for (var l = 0; l < obj_5.items.items.length; l++) { 
                                        var obj_6 = obj_5.items.items[l];
                                        var innerJsonObj_4 = createJson(obj_6);
                                        innerJsonObj_3.data.push(innerJsonObj_4);
                                    }
                                }
                                firstRowCells.push(innerJsonObj_3);
                            }
                        }
                        if ( lastrow )  {
                            for (var m = 0; m < lastrow.length; m++) { // Iterate on Column items
                                var obj_7 = lastrow[m];
                                var innerJsonObj_5 = createJson(obj_7);
                                if (obj_7.items && obj_7.items.items && obj_7.items.items.length > 0) {
                                    for (var l = 0; l < obj_7.items.items.length; l++) { 
                                        var obj_8 = obj_7.items.items[l];
                                        var innerJsonObj_6 = createJson(obj_8);
                                        innerJsonObj_5.data.push(innerJsonObj_6);
                                    }
                                }
                                lastRowCells.push(innerJsonObj_5);
                            }
                        }
                        if (firstrow) {
                            innerJsonObj_2.data.push(firstRowCells);
                        }
                        if ( lastrow ) {
                            innerJsonObj_2.data.push(lastRowCells);
                        }
                    }
                        innerJsonObj_1.data.push(innerJsonObj_2);
                    }
                }
                jsonObj.data.push(innerJsonObj_1);
            } 

            if (jsonObj.isheader == true) {
                headerJson = jsonObj;
                headerHtml = document.getElementById(jsonObj.id).outerHTML;
            }
            if (jsonObj.isfooter == true) {
                footerJson = jsonObj;
                footerHtml = document.getElementById(jsonObj.id).outerHTML;
            }                        
            items.push(jsonObj);
        }
        return items;
    }


    function createJson(obj) {

        var jsonData = {};
        var dataItems = [];

        jsonData["id"] = obj.id
        jsonData["column"] = obj.column;
        jsonData["fieldType"] = obj.fieldType;
        jsonData["layout"] = (obj.fieldType == Ext.fieldID.insertRowPanel) ? "column" : '';
        jsonData["draggable"] = obj.draggable;
        jsonData["unit"] = obj.unit;
        jsonData["width"] = obj.width;
        jsonData["height"] = obj.height;
        jsonData["columnWidth"] = obj.columnWidth;
        jsonData["columnwidth"] = obj.columnwidth;
        jsonData["x"] = obj.x;
        jsonData["y"] = obj.y;

        if (obj.fieldType == Ext.fieldID.insertText)
        {
            jsonData["elementwidth"] = obj.elementwidth;
            jsonData["labelhtml"] = obj.labelhtml;
            jsonData["label"] = obj.label;
            jsonData["textalignclass"] = obj.textalignclass;
            jsonData["textcolor"] = obj.textcolor;
            jsonData["textline"] = obj.textline;
            jsonData["bold"] = obj.bold;
            jsonData["italic"] = obj.italic;
            jsonData["fontsize"] = obj.fontsize;
            jsonData["fieldAlignment"] = obj.fieldAlignment;
            jsonData["elementheight"]=obj.elementheight;
            jsonData["iselementlevel"] = obj.iselementlevel;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom
            jsonData["fontfamily"] = obj.fontfamily
            jsonData["isBulletsApplied"] = obj.isBulletsApplied
            jsonData["bulletType"] = obj.bulletType
        }

        if (obj.fieldType == Ext.fieldID.insertField )
        {
            jsonData["elementwidth"] = obj.elementwidth;
            jsonData["elementheight"]=obj.elementheight;
            jsonData["labelhtml"] = obj.labelhtml;
            jsonData["label"] = obj.label;
            jsonData["isFormula"] = obj.isFormula;
            jsonData["textalignclass"] = obj.textalignclass;
            jsonData["fontsize"] = obj.fontsize;
            jsonData["fieldAlignment"] = obj.fieldAlignment;
            jsonData["iselementlevel"] = obj.iselementlevel;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom;
            jsonData["bold"] = obj.bold;
            jsonData["italic"] = obj.italic;
            jsonData["textcolor"] = obj.textcolor;
            jsonData["isPreText"] = obj.isPreText;
            jsonData["isPostText"] = obj.isPostText;
            jsonData["preTextValue"] = obj.preTextValue;
            jsonData["preTextWordSpacing"] = obj.preTextWordSpacing;
            jsonData["postTextValue"] = obj.postTextValue;
            jsonData["postTextWordSpacing"] = obj.postTextWordSpacing;
            jsonData["preTextbold"] = obj.preTextbold;
            jsonData["postTextbold"] = obj.postTextbold;
            jsonData["fontfamily"] = obj.fontfamily;
            jsonData["preTextItalic"] = obj.preTextItalic;
            jsonData["postTextItalic"] = obj.postTextItalic;
            jsonData["textDecoration"] = obj.textDecoration;
            jsonData["defaultValue"] = obj.defaultValue;
            jsonData["valueSeparator"] = obj.valueSeparator;
            jsonData["specificreccurrency"] = obj.specificreccurrency;
            jsonData["dimensionValue"] = obj.dimensionValue;
            jsonData["valueWithComma"] = obj.valueWithComma;
            jsonData["showzerovalueasblank"] = obj.showzerovalueasblank;
            jsonData["isnowrapvalue"] = obj.isNoWrapValue;
            jsonData["decimalPrecision"] = (obj.decimalPrecision || (obj.decimalPrecision === 0)) ?obj.decimalPrecision:_amountDecimalPrecision;    // Done as decimal precision is not working for zero value.
            if(obj.dimensionorder != undefined && obj.dimensionorder != ""){
                jsonData["dimensionorder"] = obj.dimensionorder;
            }
            if(obj.customfieldorder != undefined && obj.customfieldorder != ""){
                jsonData["customfieldorder"] = obj.customfieldorder;
            }
            jsonData["xType"] = obj.xType?obj.xType:"0";
            jsonData["isNumeric"] = obj.isNumeric?obj.isNumeric:false;
            jsonData["showAmountInWords"] = obj.showAmountInWords ? obj.showAmountInWords : 0;
            var arr = obj.labelhtml.match(/\{PLACEHOLDER:(.*?)}/g);
            if (arr && arr[0]) {
                var matches = arr[0].replace(/\{|\}/gi, '').split(":");
                jsonData['placeholder'] = matches[1];
                var rec = searchRecord(defaultFieldGlobalStoreForSearchRecord, matches[1], 'id');
                if (obj.fieldType < "25") {
                    if (rec) {
                        var recdata = rec.data;
                        jsonData['reftablename'] = recdata.reftablename;
                        jsonData['reftablefk'] = recdata.reftablefk;
                        jsonData['reftabledatacolumn'] = recdata.reftabledatacolumn;
                        jsonData['dbcolumnname'] = recdata.dbcolumnname;
                        jsonData['fieldid'] = recdata.id;
                        jsonData['label'] = recdata.label;
                        jsonData['xtype'] = recdata.xtype;
                        jsonData['isNumeric'] = recdata.isNumeric;
                        jsonData['customfield'] = recdata.customfield;
                    }
                } else {
                    jsonData['fieldid'] = jsonData.id;
                    jsonData['label'] = jsonData.label;
                    jsonData['xtype'] = jsonData.xtype;
                    jsonData['isNumeric'] = jsonData.isNumeric;
                }
            }
        }
        if (obj.fieldType == Ext.fieldID.insertImage)
        {
            jsonData["src"] = obj.src;
            jsonData["elementwidth"]=obj.elementwidth;
            jsonData["elementheight"]=obj.elementheight;
            jsonData["textalignclass"] = obj.textalignclass;
            jsonData["fieldAlignment"] = obj.fieldAlign;
            jsonData["iselementlevel"] = obj.iselementlevel;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom
        }
        if (obj.fieldType == Ext.fieldID.insertTable)
        {

            /*
             var tmpwidth=parseFloat(Ext.getCmp('idWidth').getValue());
             jsonData["width"]=tmpwidth;
             */
            jsonData["labelhtml"] = obj.el.el.dom.childNodes[0].outerHTML;
            jsonData["bordercolor"] = obj.bordercolor;
            jsonData["tablewidth"] = obj.tablewidth;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom;
            jsonData["fontfamily"] = obj.fontfamily;
            jsonData["columns"] = obj.columns;
            jsonData["includeProductCategory"] = obj.includeProductCategory;
            jsonData["includegroupingRowAfterId"] = obj.includegroupingRowAfterId;
            jsonData["groupingRowHTMl"] = obj.groupingRowHTMl;
            jsonData["groupingRowAfterHTML"] = obj.groupingRowAfterHTML;
            jsonData["isGroupingApplied"] = obj.isGroupingApplied;
            jsonData["isFormattingApplied"] = obj.isFormattingApplied;
            jsonData["islineitemrepeat"] = obj.islineitemrepeat;
            jsonData["isconsolidated"] = obj.isconsolidated;
            jsonData["isExtendLineItem"] = obj.isExtendLineItem;
            jsonData["pageSize"] = obj.pageSize;
            jsonData["pageOrientation"] = obj.pageOrientation;
            jsonData["adjustPageHeight"] = obj.adjustPageHeight;
            jsonData["sortfield"] = obj.sortfield;
            jsonData["sortfieldxtype"] = obj.sortfieldxtype;
            jsonData["sortorder"] = obj.sortorder;
            var dataItems = [];
            var columndata = [];
            columndata = documentLineColumns;
            
            var listItems = Ext.get('itemlistconfigsectionPanelGrid');
            var isLineItemWithPrefix = false;
            if(listItems && listItems.dom.childNodes[1].rows[1].cells.length > 0){
                var tds = listItems.dom.childNodes[1].rows[1].cells;
                if(tds[0] && tds[0].getAttribute('isLineItemWithPrefix')!=null){
                    isLineItemWithPrefix = true;
                }
            }
            if (listItems) {
                var cells = listItems.dom.childNodes[1].rows[1].cells;
                if(isLineItemWithPrefix){
                   cells = listItems.dom.childNodes[1].rows[2].cells;
                   var rowCount =  listItems.dom.childNodes[1].rows.length;
                   var summaryTable = Ext.getElementById("summaryTableID");
                   if ( summaryTable ) {
                       rowCount--;
                   }
                    jsonData["firstRowHTML"] = listItems.dom.childNodes[1].rows[1].outerHTML;
                   var rowPresent = false;
                   for ( var varindex = 0 ; varindex < obj.firstRowCells.length; varindex++ ) {
                       if (obj.firstRowCells[varindex].items.items.length > 0) {
                           rowPresent = true;
                           break;
                       }
                   }
                   jsonData["isFirstRowPresent"] = rowPresent;
                   rowPresent = false;
                   jsonData["lastRowHTML"] = listItems.dom.childNodes[1].rows[rowCount-1].outerHTML;
                   for ( var varindex = 0 ; varindex < obj.lastRowCells.length; varindex++ ) {
                       if (obj.lastRowCells[varindex].items.items.length > 0) {
                           rowPresent = true;
                           break;
                       }
                   }
                   jsonData["isLastRowPresent"] = rowPresent;
                }
                var groupingRow = listItems.dom.childNodes[1].rows[2]
                var isGroupingRow = groupingRow.attributes.getNamedItem("isGroupingRow") != null ? groupingRow.attributes.getNamedItem("isGroupingRow").value : "no";
                var groupingItems = [];
                var indexToAdd = 0;
                if ( isGroupingRow === "yes") {
                    cells = listItems.dom.childNodes[1].rows[3].cells;
                    jsonData["isGroupingRowPresent"] = true;
                    jsonData["isGroupingApplied"] = isGroupingApplied;
                    var groupingRowCells =  groupingRow.cells;
                    for (var cellIndex = 0; cellIndex < groupingRowCells.length; cellIndex++) {
                        var div = groupingRowCells[cellIndex].children[0];
                        var divSetting = div.attributes;
                            var divConfig = {};
                            for (var attrCnt = 0; attrCnt < divSetting.length; attrCnt++) {
                                if (divSetting[attrCnt].name != 'class')
                                    divConfig[divSetting[attrCnt].name] = divSetting[attrCnt].value
                            }
                            groupingItems.push(divConfig);
                    }
                    indexToAdd++;
                } else {
                    jsonData["isGroupingRowPresent"] = false;
                }
                var groupingAfterRow = listItems.dom.childNodes[1].rows[3+indexToAdd];
                var isGroupingAfterRow = "no";
                if ( groupingAfterRow ) {
                    isGroupingAfterRow = groupingAfterRow.attributes.getNamedItem("isGroupingRow") != null ? groupingAfterRow.attributes.getNamedItem("isGroupingRow").value : "no";
                }
                
                var groupingAfterItems = [];
                if ( isGroupingAfterRow === "yes") {
                    jsonData["isGroupingAfterRowPresent"] = true;
                    var groupingRowCells =  groupingAfterRow.cells;
                    for (var cellIndex = 0; cellIndex < groupingRowCells.length; cellIndex++) {
                        var div = groupingRowCells[cellIndex].children[0];
                        var divSetting = div.attributes;
                            var divConfig = {};
                            for (var attrCnt = 0; attrCnt < divSetting.length; attrCnt++) {
                                if (divSetting[attrCnt].name != 'class')
                                    divConfig[divSetting[attrCnt].name] = divSetting[attrCnt].value
                            }
                            groupingAfterItems.push(divConfig);
                    }
                    indexToAdd++;
                } else {
                    jsonData["isGroupingAfterRowPresent"] = false;
                }
                if(cells.length > 0){
                var headeritems = [];
                var childItems = listItems.dom.childNodes[1].rows[0].cells;
                for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
                    var child = childItems[itemcnt];
                    var colSetting = child.attributes;
                    var childConfig = {};
                    for (var attrCnt = 0; attrCnt < colSetting.length; attrCnt++) {
                        if (colSetting[attrCnt].name != 'class')
                            childConfig[colSetting[attrCnt].name] = colSetting[attrCnt].value
                        }
                    headeritems.push(childConfig);
                }
                
                var spacecnt = 0;
                for (var hitemscnt = 0; hitemscnt < headeritems.length; hitemscnt++){  //checking all headers contains space (&nbsp;)
                    if(headeritems[hitemscnt]['label'].replace(/&nbsp;|&nbsp/g,'').trim() === ''){
                        spacecnt++;
                    }
                }
                
                if(spacecnt == headeritems.length){  //hide headers if all headers contains space (&nbsp;)
                    spacecnt = 0;
                    while (spacecnt < headeritems.length){
                        headeritems[spacecnt]['style'] += ' display:none;';
                        spacecnt++;
                    }
                }
                var lineitems = [];
                var oldtemplate=false;
                var dataRowNum = 1;
                    if ( isGroupingRow === "yes" ) {
                        dataRowNum++;
                    }
                    var childItems = isLineItemWithPrefix?listItems.dom.childNodes[1].rows[dataRowNum+1].cells:listItems.dom.childNodes[1].rows[dataRowNum].cells;
                    if ( childItems.length > 0 ) {
                    for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
                        var divs = childItems[itemcnt].childNodes;
                        if ( divs[0].nodeName != "DIV" ) {
                            oldtemplate = true;
                            break;
                        }
                        for (var divcnt = 0; divcnt < divs.length; divcnt++) {
                            var child = divs[divcnt];
                            var colSetting = child.attributes;
                            var childConfig = {};
                            for (var attrCnt = 0; attrCnt < colSetting.length; attrCnt++) {
                                if (colSetting[attrCnt].name != 'class')
                                    childConfig[colSetting[attrCnt].name] = colSetting[attrCnt].value
                            }
                            lineitems.push(childConfig);
                        }
                    }
               }
               if (oldtemplate) {
                   lineitems = headeritems;
               }
//                var panelId = listItems.dom.attributes["panelid"].value;
//                var parentPanel = panelId?Ext.getCmp(panelId):"";
                var summaryJson = obj.ownerCt.items.items[0].summaryTableJson;
                var isSummaryTable =  obj.ownerCt.items.items[0].isSummaryTableApplied;
                var summarycellplaceholder = [];
                if(isSummaryTable) {
                    var decimalPrecisionArr = {};
                    var summaryTableDom = obj.getEl().dom.children[0].lastChild.lastChild.lastChild.children[0];//gives summary table dom

                    var summaryTableRows = summaryTableDom.children[0].rows.length;//length of rows in summary table
                    for ( var i = 0; i<summaryTableRows ; i++) { // loop for rows
                        var summaryTableCells = summaryTableDom.children[0].rows[i].cells.length;
                        for ( var j = 0; j<summaryTableCells ; j++ ) { // loop for cells
                            var cell = summaryTableDom.children[0].rows[i].cells[j];
                            var children = cell.children;
                            for ( var k =0 ;k < children.length; k++) { //loop for childs of cell
                                var child = children[k];
                                if ( child.nodeName == "DIV" && (child.getAttribute('type') == null || child.getAttribute('type') === "dataElement")) {//check for Select Field and Data Element
                                    var decimalPrecisionValue =child.attributes.getNamedItem("decimalprecision")?child.attributes.getNamedItem("decimalprecision").value:"";//gives decimalPrecision value of child
                                    var text = child.textContent; 
                                    text = text.substr(text.indexOf('#'),text.lastIndexOf('#')+1);// gives textContent(#Total Amount#,#Total Tax#,#Sub Total#, etc.) of child
                                    decimalPrecisionArr[text] = decimalPrecisionValue; // add decimalPrecision value in object with textContent(#Total Amount#,#Total Tax#,#Sub Total#, etc.) as key
                                }
                            }
                        }
                    }
                    var summarytable = document.getElementById("summaryTableID").innerHTML;
                    var arr = summarytable.match(/\{PLACEHOLDER:(.*?)}/g);
                    if (arr && arr.length>0) {
                        for(var gCnt=0; gCnt<arr.length;gCnt++) {
                            var childConfig = {};
                            var matches = arr[gCnt].replace(/\{|\}/gi, '').split(":");
                            childConfig['placeholder'] = matches[1];
                            var rec = searchRecord(defaultFieldGlobalStoreForSearchRecord, matches[1], 'id');
                            if (rec) {
                                var recdata = rec.data;
                                childConfig['reftablename'] = recdata.reftablename;
                                childConfig['reftablefk'] = recdata.reftablefk;
                                childConfig['reftabledatacolumn'] = recdata.reftabledatacolumn;
                                childConfig['dbcolumnname'] = recdata.dbcolumnname;
                                childConfig['fieldid'] = recdata.id;
                                childConfig['label'] = recdata.label;
                                childConfig['xtype'] = recdata.xtype;
                                childConfig['customfield'] = recdata.customfield;
                                childConfig['decimalPrecision'] = decimalPrecisionArr["#"+recdata.label+"#"];//add decimalPrecision to select field Json and data element json for summary table
                            }
                            summarycellplaceholder.push(childConfig);
                        }
                    }
                }
                var parentRowID = obj.ownerCt.ownerCt.id;
                var fontsize=obj.fontsize?obj.fontsize:""; 
                var tempallign=(obj.align!=null)?obj.align:"center";
                var align=(tempallign===0 || tempallign=="left")?"left":(tempallign==1 || tempallign=="center")?"center":"right"; 
                var bold=obj.bold?((obj.bold=="true")?true:false):false; 
                var italic=obj.italic?((obj.italic=="true")?true:false):false; 
                var underline=obj.underline?((obj.underline=="true")?true:false):false; 
                var bordercolor=obj.bordercolor?obj.bordercolor:"#FFFFFF"; 
                var lineConfig = {
                    'lineitems': lineitems,
                    'headeritems':headeritems,
                    'groupingItems': groupingItems,
                    'groupingAfterItems':groupingAfterItems,
                    id: obj.id,
                    x: obj.x,
                    y: obj.y,
                    height: Ext.get(obj.id).getBox().height,
                    width: Ext.get(obj.id).getBox().width,
                    fieldTypeId: obj.fieldTypeId,
                    //labelhtml: content,
                    tablecolor: this.tcolor,
                    tablebordermode: this.bmode,
                    summaryInfo: isSummaryTable?summaryJson:"",
                    cellplaceholder:isSummaryTable?summarycellplaceholder:"",
                    isSummaryTable:isSummaryTable,
                    parentrowid:parentRowID,
                    fontsize:fontsize,
                    bold:bold,
                    align:align,
                    italic:italic,
                    underline:underline,
                    bordercolor:bordercolor,
                    'columndata': JSON.stringify(documentLineColumns)
                //                    cellplaceholder:lineitems
                };
                dataItems.push(lineConfig);
            }
            }

        }
        if (obj.fieldType == Ext.fieldID.insertGlobalTable)
        {
            jsonData["labelhtml"] = obj.el.el.dom.childNodes[0].outerHTML;
            jsonData["borderedgetype"]=obj.borderedgetype;
            jsonData["rowspacing"]=obj.rowspacing;
            jsonData["columnspacing"]=obj.columnspacing;
            jsonData["fieldAlignment"]=obj.fieldAlignment;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom
            jsonData["tableWidth"] = obj.tableWidth;
            jsonData["borderColor"] = obj.borderColor;
            jsonData["backgroundColor"] = obj.backgroundColor;
            jsonData["headerColor"] = obj.headerColor;
            jsonData["tableAlign"] = obj.tableAlign;
            jsonData["isExtendLineItem"] = obj.isExtendLineItem;
            jsonData["pageSize"] = obj.pageSize;
            jsonData["pageOrientation"] = obj.pageOrientation;
            jsonData["isbalanceoutstanding"] = obj.isbalanceoutstanding;
            jsonData["isOutstandingMultipleCurrency"] = obj.isOutstandingMultipleCurrency;
            jsonData["isSummaryTable"] = obj.isSummaryTable;
            jsonData["summaryTableHeight"] = obj.summaryTableHeight;
            jsonData["isjedetailstable"] = obj.isjedetailstable;
            jsonData["tableHeader"] = obj.tableHeader;
            jsonData["adjustPageHeight"] = obj.adjustPageHeight;
            var content = obj.el.getHTML();
            var defJson = {};
            var decimalPrecisionArr = {};
            var valueSeperatorArr = {};
            var dimensionValueArr = {};
            var valuewithcommaArr = {};
            var zeroValueAsBlankArr = {};
            var specificreccurrencyArr = {};
            var isNoWrapValueArr = {};
            var showAmountInWordsArr = {};
            var dimensionorder;
            var customfieldorder;
            var rows = obj.getEl().dom.children[0].rows.length;
            for ( var i = 0; i<rows ; i++) {
                var cells = obj.getEl().dom.children[0].rows[i].cells.length;
                for ( var j = 0; j<cells ; j++ ) {
                    var cell = obj.getEl().dom.children[0].rows[i].cells[j];
                    var children = cell.children;
                    for ( var k =0 ;k < children.length; k++) {
                        var child = children[k];
                        if ( child.nodeName == "DIV") {
                            var defvalue =child.attributes.getNamedItem("defaultValue")?child.attributes.getNamedItem("defaultValue").value:"";
                            var decimalPrecisionValue =child.attributes.getNamedItem("decimalprecision")?child.attributes.getNamedItem("decimalprecision").value:"";
                            var valueSeperator =child.attributes.getNamedItem("valueseparator")?child.attributes.getNamedItem("valueseparator").value:"";
                            var dimensionValue =child.attributes.getNamedItem("dimensionvalue")?child.attributes.getNamedItem("dimensionvalue").value:"";
                            var valueWithComma =child.attributes.getNamedItem("valueWithComma")?child.attributes.getNamedItem("valueWithComma").value:"";
                            var zeroValueAsBlank =child.attributes.getNamedItem("showzerovalueasblank")?child.attributes.getNamedItem("showzerovalueasblank").value:"false";
                            var specificreccurrencyvalue =child.attributes.getNamedItem("specificreccurrency")?child.attributes.getNamedItem("specificreccurrency").value:"";
                            var isNoWrapValue =child.attributes.getNamedItem("isnowrapvalue")?child.attributes.getNamedItem("isnowrapvalue").value:"false";
                            var showAmountInWords = child.attributes.getNamedItem("showAmountInWords") ? child.attributes.getNamedItem("showAmountInWords").value : 0;
                            var text = child.innerText;
                            if (text === undefined) {
                                text = child.innerHTML;
                            }
                            if (child.children.length > 0 && child.children[child.children.length - 1]) {
                                text = child.children[child.children.length - 1].innerText;
                            }
                            defJson[text] = defvalue;
                            decimalPrecisionArr[text] = decimalPrecisionValue;
                            valueSeperatorArr[text] = valueSeperator;
                            dimensionValueArr[text] = dimensionValue;
                            valuewithcommaArr[text] = valueWithComma;
                            zeroValueAsBlankArr[text] = zeroValueAsBlank;
                            specificreccurrencyArr[text] = specificreccurrencyvalue;
                            isNoWrapValueArr[text] = isNoWrapValue;
                            showAmountInWordsArr[text] = showAmountInWords;
                            if(child.attributes.getNamedItem("columnname") != null){
                                var label = child.attributes.getNamedItem("columnname").value.toLowerCase();
                                if((label==Ext.FieldType.allgloballeveldimensions || label==Ext.FieldType.alldimensions || label==Ext.FieldType.alllineleveldimensions) && child.attributes.getNamedItem("dimensionorder")!=null){
                                    dimensionorder = child.attributes.getNamedItem("dimensionorder").value;
                                }
                                if((label==Ext.FieldType.allgloballevelcustomfields || label==Ext.FieldType.alllinelevelcustomfields) && child.attributes.getNamedItem("customfieldorder")!=null){
                                    customfieldorder = child.attributes.getNamedItem("customfieldorder").value;
                                }
                            }
                        }
                    }
                }
            }
            var arr = content.match(/\{PLACEHOLDER:(.*?)}/g);
            if (arr && arr.length>0) {
                var lineitems = [];
                for(var gCnt=0; gCnt<arr.length;gCnt++) {
                    var childConfig = {};
                    var matches = arr[gCnt].replace(/\{|\}/gi, '').split(":");
                    childConfig['placeholder'] = matches[1];
                    var rec = searchRecord(defaultFieldGlobalStoreForSearchRecord, matches[1], 'id');
                    if (rec) {
                        var recdata = rec.data;
                        childConfig['reftablename'] = recdata.reftablename;
                        childConfig['reftablefk'] = recdata.reftablefk;
                        childConfig['reftabledatacolumn'] = recdata.reftabledatacolumn;
                        childConfig['dbcolumnname'] = recdata.dbcolumnname;
                        childConfig['fieldid'] = recdata.id;
                        childConfig['label'] = recdata.label;
                        childConfig['xtype'] = recdata.xtype;
                        childConfig['isNumeric'] = recdata.isNumeric;
                        childConfig['customfield'] = recdata.customfield;
                        childConfig['defaultValue'] = defJson["#"+recdata.label+"#"];
                        childConfig['decimalPrecision'] = decimalPrecisionArr["#"+recdata.label+"#"];
                        childConfig['valueSeparator'] = valueSeperatorArr["#"+recdata.label+"#"];
                        childConfig['specificreccurrency'] = specificreccurrencyArr["#"+recdata.label+"#"];
                        childConfig['dimensionValue'] = dimensionValueArr["#"+recdata.label+"#"];
                        childConfig['valueWithComma'] = valuewithcommaArr["#"+recdata.label+"#"];
                        childConfig['showzerovalueasblank'] = zeroValueAsBlankArr["#"+recdata.label+"#"];
                        childConfig['isnowrapvalue'] = isNoWrapValueArr["#"+recdata.label+"#"];
                        childConfig['showAmountInWords'] = showAmountInWordsArr["#" + recdata.label + "#"];
                        var label = recdata.label.toLowerCase();
                        if((label==Ext.FieldType.allgloballeveldimensions || label==Ext.FieldType.alldimensions || label==Ext.FieldType.alllineleveldimensions) && dimensionorder){
                            childConfig['dimensionorder'] = dimensionorder;
                    }
                        if((label==Ext.FieldType.allgloballevelcustomfields || label==Ext.FieldType.alllinelevelcustomfields) && customfieldorder){
                            childConfig['customfieldorder'] = customfieldorder;
                        }
                    }
                    lineitems.push(childConfig);
                }
            }
            jsonData["cellplaceholder"] = lineitems;
            jsonData["fixedrowvalue"] =obj.fixedrowvalue;
            
        }

        if (obj.fieldType == Ext.fieldID.insertRowPanel)
        {
            jsonData["isheader"] = obj.isheader;
            jsonData["isfooter"] = obj.isfooter;
            jsonData["isJoinNextRow"] = obj.isJoinNextRow;
            jsonData["rowHeight"] = obj.rowHeight;

        }
        if (obj.fieldType == Ext.fieldID.insertColumnPanel ){
            jsonData["fieldalignment"] = obj.fieldalignment;
            jsonData["type"] = obj.type;
            jsonData["borderedgetype"]=obj.borderedgetype;
            jsonData["allowborder"] = obj.allowborder;
            jsonData["backgroundColor"] = obj.backgroundColor;
            jsonData["linespacing"] = obj.linespacing;
            jsonData["marginTop"] = obj.marginTop;
            jsonData["marginRight"] = obj.marginRight;
            jsonData["marginLeft"] = obj.marginLeft;
            jsonData["marginBottom"] = obj.marginBottom;
            jsonData["borderTop"] = obj.borderTop?obj.borderTop:"solid";
            jsonData["borderLeft"] = obj.borderLeft?obj.borderLeft:"solid";
            jsonData["borderBottom"] = obj.borderBottom?obj.borderBottom:"solid";
            jsonData["borderRight"] = obj.borderRight?obj.borderRight:"solid";
            jsonData["isJoinPreviousColumn"] = obj.isJoinPreviousColumn;
        }
        if (obj.fieldType == Ext.fieldID.insertDataElement){
            jsonData["dataelementhtml"] = obj.dataelementhtml;
            jsonData["labelval"] = obj.labelval;
            jsonData["selectfield"] = obj.selectfield;
            jsonData["labelwidth"] = obj.labelwidth;
            jsonData["datawidth"] = obj.datawidth;
            jsonData["labelbold"] = obj.labelbold;
            jsonData["databold"] = obj.databold;
            jsonData["labelitalic"] = obj.labelitalic;
            jsonData["dataitalic"] = obj.dataitalic;
            jsonData["labelfontsize"] = obj.labelfontsize;
            jsonData["datafontsize"] = obj.datafontsize;
            jsonData["labelalign"] = obj.labelalign;
            jsonData["dataalign"] = obj.dataalign;
            jsonData["topmargin"] = obj.topmargin;
            jsonData["bottommargin"] = obj.bottommargin;
            jsonData["leftmargin"] = obj.leftmargin;
            jsonData["rightmargin"] = obj.rightmargin;
            jsonData["colontype"] = obj.colontype;
            jsonData["allowlabel"] = obj.allowlabel;
            jsonData["componentwidth"] = obj.componentwidth;
            jsonData["textlinelabel"] = obj.textlinelabel;
            jsonData["labelfontfamily"] = obj.labelfontfamily;
            jsonData["datafontfamily"] = obj.datafontfamily;
            jsonData["textlinedata"] = obj.textlinedata;
            jsonData["textcolorlabel"] = obj.textcolorlabel;
            jsonData["textcolordata"] = obj.textcolordata;
            jsonData["fieldalignment"] = obj.fieldalignment;
            jsonData["customlabel"] = obj.customlabel;
            jsonData["decimalPrecision"] = obj.decimalPrecision;
            jsonData["defaultValue"] = obj.defaultValue;
            jsonData["valueSeparator"] = obj.valueSeparator;
            jsonData["specificreccurrency"] = obj.specificreccurrency;
            jsonData["dimensionValue"] = obj.dimensionValue;
            jsonData["valueWithComma"] = obj.valueWithComma;
            jsonData["showzerovalueasblank"] = obj.showzerovalueasblank;
            jsonData["isnowrapvalue"] = obj.isNoWrapValue;
            jsonData["XType"] = obj.XType;
            var arr = obj.dataelementhtml.match(/\{PLACEHOLDER:(.*?)}/g);
            if (arr && arr[0]) {
                var matches = arr[0].replace(/\{|\}/gi, '').split(":");
                jsonData['placeholder'] = matches[1];
                var rec = searchRecord(defaultFieldGlobalStoreForSearchRecord, matches[1], 'id');
                if (obj.fieldType < "25") {
                    if (rec) {
                        var recdata = rec.data;
                        jsonData['reftablename'] = recdata.reftablename;
                        jsonData['reftablefk'] = recdata.reftablefk;
                        jsonData['reftabledatacolumn'] = recdata.reftabledatacolumn;
                        jsonData['dbcolumnname'] = recdata.dbcolumnname;
                        jsonData['fieldid'] = recdata.id;
                        jsonData['label'] = recdata.label;
                        jsonData['xtype'] = recdata.xtype;
                        jsonData['customfield'] = recdata.customfield;
                    }
                } else {
                    jsonData['fieldid'] = jsonData.id;
                    jsonData['label'] = jsonData.label;
                    jsonData['xtype'] = jsonData.xtype;
                }
            }
        }
        if ( obj.fieldType == Ext.fieldID.insertAgeingTable) {
            
         jsonData["interval"]=obj.interval;   
         jsonData["labelhtml"]=obj.labelhtml;   
         jsonData["backgroundColor"]=obj.backgroundColor;   
         jsonData["databackgroundColor"]=obj.databackgroundColor;   
         jsonData["borderColor"]=obj.borderColor;   
         jsonData["tableAlign"]=obj.tableAlign;   
         jsonData["marginTop"]=obj.marginTop;   
         jsonData["marginBottom"]=obj.marginBottom;   
         jsonData["marginLeft"]=obj.marginLeft;   
         jsonData["marginRight"]=obj.marginRight;   
         jsonData["tableWidth"]=obj.tableWidth;
         jsonData["bold"]=obj.bold;
         jsonData["columns"] = obj.columns;
         jsonData["fontSize"]=obj.fontSize;
         jsonData["italic"]=obj.italic;
         jsonData["underline"]=obj.underline;
         jsonData["align"]=obj.align;
         jsonData["fontFamily"]=obj.fontFamily;
         jsonData["fontSizedata"]=obj.fontSizedata;
         jsonData["italicdata"]=obj.italicdata;
         jsonData["underlinedata"]=obj.underlinedata;
         jsonData["aligndata"]=obj.aligndata;
         jsonData["fontFamilydata"]=obj.fontFamilydata;
         jsonData["bolddata"]=obj.bolddata;
         jsonData["textColor"]=obj.textColor;
         jsonData["dataTextColor"]=obj.dataTextColor;
         jsonData["noofintervals"]=obj.noofintervals;   
         jsonData["ismulticurrency"]=obj.ismulticurrency;   
         jsonData["checklistValue"]=obj.checklistValue;   
         jsonData["isCustomerVendorCurrency"]=obj.isCustomerVendorCurrency;   
         jsonData["intervalType"]=obj.intervalType; 
         jsonData["intervalPlaceHolder"]=obj.intervalPlaceHolder;  //ERP-28745
         jsonData["intervalText"]=obj.intervalText; 
         jsonData["isincludecurrent"]=obj.isincludecurrent; 
         jsonData["parentrowid"]=obj.ownerCt.ownerCt.id;
         jsonData['columndata']=JSON.stringify(ageingColumns)
         var ageingTable = Ext.get('idageingtable');
         var headerItems = [];
         var ageingDataItems = [];
         if (ageingTable) {
            var headerCells = Ext.get('idageingtable').el.dom.rows[0].cells;
            for (var cellIndex = 0; cellIndex < headerCells.length; cellIndex++) {
                var headerCell = headerCells[cellIndex]; //getting <td> of header
                var tdSetting = headerCell.attributes; //getting attributes of header <td>
                var tdConfig = {};
                for (var attrCnt = 0; attrCnt < tdSetting.length; attrCnt++) {
                    if (tdSetting[attrCnt].name != 'class')
                        tdConfig[tdSetting[attrCnt].name] = tdSetting[attrCnt].value
                }
                headerItems.push(tdConfig);
            }
            var dataCells = Ext.get('idageingtable').el.dom.rows[1].cells;  
            for (var index = 0; index < headerCells.length; index++) {
                var dataCell = dataCells[index]; //getting <td> of data item
                var tdSettings = dataCell.childNodes[0].attributes; //getting attributes of data item <div>
                var tdConfigs = {};
                for (var cnt = 0; cnt < tdSettings.length; cnt++) {
                    if (tdSettings[cnt].name != 'class' && tdSettings[cnt].name != 'style')
                        tdConfigs[tdSettings[cnt].name] = tdSettings[cnt].value
                    else if (tdSettings[cnt].name == 'style')
                        tdConfigs[tdSettings[cnt].name] = dataCell.attributes.style.value; //Assigning style of <td> to data item
                }
                ageingDataItems.push(tdConfigs);
            }
         }
         jsonData["headerItems"] = headerItems;
         jsonData["dataItems"] = ageingDataItems;
       }
       if ( obj.fieldType == Ext.fieldID.insertGroupingSummaryTable) {
         
         jsonData["groupingOnDisplayValue"]=obj.groupingOnDisplayValue;   
         jsonData["groupingOnValue"]=obj.groupingOnValue;   
         jsonData["labelhtml"]=obj.labelhtml;   
         jsonData["backgroundColor"]=obj.backgroundColor;   
         jsonData["databackgroundColor"]=obj.databackgroundColor;   
         jsonData["borderColor"]=obj.borderColor;   
         jsonData["tableAlign"]=obj.tableAlign;   
         jsonData["marginTop"]=obj.marginTop;   
         jsonData["marginBottom"]=obj.marginBottom;   
         jsonData["marginLeft"]=obj.marginLeft;   
         jsonData["marginRight"]=obj.marginRight;   
         jsonData["tableWidth"]=obj.tableWidth;
         jsonData["bold"]=obj.bold;
         jsonData["columns"] = obj.columns;
         jsonData["fontSize"]=obj.fontSize;
         jsonData["italic"]=obj.italic;
         jsonData["underline"]=obj.underline;
         jsonData["align"]=obj.align;
         jsonData["fontFamily"]=obj.fontFamily;
         jsonData["fontSizedata"]=obj.fontSizedata;
         jsonData["italicdata"]=obj.italicdata;
         jsonData["underlinedata"]=obj.underlinedata;
         jsonData["aligndata"]=obj.aligndata;
         jsonData["fontFamilydata"]=obj.fontFamilydata;
         jsonData["bolddata"]=obj.bolddata;
         jsonData["textColor"]=obj.textColor;
         jsonData["dataTextColor"]=obj.dataTextColor;
         jsonData["parentrowid"]=obj.ownerCt.ownerCt.id;
         jsonData['columndata']=JSON.stringify(groupingSummaryColumns)
         var groupingSummaryTable = Ext.get('idgroupingsummarytable');
         var headerItems = [];
         var groupingSummaryDataItems = [];
         var groupingColPreText = "";
         var groupingColPostText = "";
         if (groupingSummaryTable) {
            var headerCells = Ext.get('idgroupingsummarytable').el.dom.rows[0].cells;
            for (var cellIndex = 0; cellIndex < headerCells.length; cellIndex++) {
                var headerCell = headerCells[cellIndex]; //getting <td> of header
                var tdSetting = headerCell.attributes; //getting attributes of header <td>
                var tdConfig = {};
                for (var attrCnt = 0; attrCnt < tdSetting.length; attrCnt++) {
                    if (tdSetting[attrCnt].name != 'class')
                        tdConfig[tdSetting[attrCnt].name] = tdSetting[attrCnt].value
                }
                headerItems.push(tdConfig);
            }
             var dataCells = Ext.get('idgroupingsummarytable').el.dom.rows[1].cells;  
             for (var index = 0; index < headerCells.length; index++) {
                var dataCell = dataCells[index]; //getting <td> of data item
                var tdSettings = dataCell.childNodes[0].attributes; //getting attributes of data item <div>
                var tdConfigs = {};
                for (var cnt = 0; cnt < tdSettings.length; cnt++) {
                    if (tdSettings[cnt].name != 'class' && tdSettings[cnt].name != 'style')
                        tdConfigs[tdSettings[cnt].name] = tdSettings[cnt].value
                    else if (tdSettings[cnt].name == 'style')
                        tdConfigs[tdSettings[cnt].name] = dataCell.attributes.style.value; //Assigning style of <td> to data item
                }
                
                var seq = tdSettings.getNamedItem("seq") != null ? tdSettings.getNamedItem("seq").value : "";
                var isPreText = dataCell.children[0].isPreText != undefined ? dataCell.children[0].isPreText : false;
                var isPostText = dataCell.children[0].isPostText != undefined ? dataCell.children[0].isPostText : false;
                if(isPreText){
                    var preTextEle = dataCell.children[0].childNodes[0];
                    if(seq === "-1"){
                        tdConfigs["PreText"] = preTextEle.innerHTML;
                        groupingColPreText = preTextEle.innerHTML;
                    }
                    
                }
                if(isPostText){
                    var postTextEle = dataCell.children[0].childNodes[dataCell.children[0].childNodes.length-1];
                    if(seq === "-1"){
                        tdConfigs["PostText"] = postTextEle.innerHTML;
                        groupingColPostText = postTextEle.innerHTML;
                    }
                    
                }
                
                groupingSummaryDataItems.push(tdConfigs);
            }
         }
         jsonData["groupingColPreText"] = groupingColPreText;
         jsonData["groupingColPostText"] = groupingColPostText;
         jsonData["headerItems"] = headerItems;
         jsonData["dataItems"] = groupingSummaryDataItems;
       }
   //create json of Details Table
   if (obj.fieldType == Ext.fieldID.insertDetailsTable) {
        //global info of Details Table
        jsonData["labelhtml"] = obj.el.el.dom.childNodes[0].outerHTML;
        jsonData["bordercolor"] = obj.bordercolor;
        jsonData["tablewidth"] = obj.tablewidth;
        jsonData["marginTop"] = obj.marginTop;
        jsonData["marginRight"] = obj.marginRight;
        jsonData["marginLeft"] = obj.marginLeft;
        jsonData["marginBottom"] = obj.marginBottom;
        jsonData["fontfamily"] = obj.fontfamily;
        jsonData["columns"] = obj.columns;
        jsonData["pageSize"] = obj.pageSize;
        jsonData["pageOrientation"] = obj.pageOrientation;
        jsonData["detailsTableSubType_id"] = obj.detailsTableSubType_id;
        jsonData["detailsTableSubType_value"] = obj.detailsTableSubType_value;
        jsonData["detailsTableId"] = obj.detailsTableId;
        jsonData["consolidatedfield"] = obj.consolidatedfield;
        jsonData["summationfields"] = obj.summationfields;
        //columns data of Details Table
        var dataItems = [];
        var listItems = Ext.get('detailsTableConfigSectionPanelGrid_'+obj.detailsTableId);
        if (listItems) {
            var cells = listItems.dom.childNodes[1].rows[1].cells;
            if(cells.length > 0){
                var headeritems = [];
                var childItems = listItems.dom.childNodes[1].rows[0].cells;
                for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
                    var child = childItems[itemcnt];
                    var colSetting = child.attributes;
                    var childConfig = {};
                    for (var attrCnt = 0; attrCnt < colSetting.length; attrCnt++) {
                        if (colSetting[attrCnt].name != 'class')
                            childConfig[colSetting[attrCnt].name] = colSetting[attrCnt].value
                        }
                    headeritems.push(childConfig);
                }
                var spacecnt = 0;
                for (var hitemscnt = 0; hitemscnt < headeritems.length; hitemscnt++){  //checking all headers contains space (&nbsp;)
                    if(headeritems[hitemscnt]['label'].replace(/&nbsp;|&nbsp/g,'').trim() === ''){
                        spacecnt++;
                    }
                }
                if(spacecnt == headeritems.length){  //hide headers if all headers contains space (&nbsp;)
                    spacecnt = 0;
                    while (spacecnt < headeritems.length){
                        headeritems[spacecnt]['style'] += ' display:none;';
                        spacecnt++;
                    }
                }
                var detailsItems = [];
                var oldtemplate=false;
                var dataRowNum = 1;
                var childItems = isLineItemWithPrefix?listItems.dom.childNodes[1].rows[dataRowNum+1].cells:listItems.dom.childNodes[1].rows[dataRowNum].cells;
                if ( childItems.length > 0 ) {
                    for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
                        var divs = childItems[itemcnt].childNodes;
                        if ( divs[0].nodeName != "DIV" ) {
                            oldtemplate = true;
                            break;
                        }
                        for (var divcnt = 0; divcnt < divs.length; divcnt++) {
                            var child = divs[divcnt];
                            var colSetting = child.attributes;
                            var childConfig = {};
                            for (var attrCnt = 0; attrCnt < colSetting.length; attrCnt++) {
                                if (colSetting[attrCnt].name != 'class')
                                    childConfig[colSetting[attrCnt].name] = colSetting[attrCnt].value
                            }
                            detailsItems.push(childConfig);
                        }
                    }
                }
                if (oldtemplate) {
                    detailsItems = headeritems;
                }
                var parentRowID = obj.ownerCt.ownerCt.id;
                var fontsize=obj.fontsize?obj.fontsize:""; 
                var tempallign=(obj.align!=null)?obj.align:"center";
                var align=(tempallign===0 || tempallign=="left")?"left":(tempallign==1 || tempallign=="center")?"center":"right"; 
                var bold=obj.bold?((obj.bold=="true")?true:false):false; 
                var italic=obj.italic?((obj.italic=="true")?true:false):false; 
                var underline=obj.underline?((obj.underline=="true")?true:false):false; 
                var bordercolor=obj.bordercolor?obj.bordercolor:"#FFFFFF"; 
                var lineConfig = {
                    'detailsTableCols': detailsItems,
                    'detailsTableHeaders':headeritems,
                    id: obj.id,
                    x: obj.x,
                    y: obj.y,
                    height: Ext.get(obj.id).getBox().height,
                    width: Ext.get(obj.id).getBox().width,
                    fieldTypeId: obj.fieldTypeId,
                    tablecolor: this.tcolor,
                    tablebordermode: this.bmode,
                    parentrowid:parentRowID,
                    fontsize:fontsize,
                    bold:bold,
                    align:align,
                    italic:italic,
                    underline:underline,
                    bordercolor:bordercolor,
                    detailsTableSubType_id:obj.detailsTableSubType_id,
                    detailsTableSubType_value:obj.detailsTableSubType_value,
                    'columndata': JSON.stringify(detailsTableColumnsObj[obj.detailsTableId])
                };
                dataItems.push(lineConfig);
            }
        }
     }
     jsonData["data"] = dataItems;

     return jsonData;


    }

    function getGlobalHeaderFooterJson(designerPanel, globalheaderid)
    {
        var panelItems = designerPanel.items.items;
        var items = [];
        var jsonObj = [];

        for (var cnt = 0; cnt < panelItems.length; cnt++) {

            var obj_1 = panelItems[cnt];

            if (obj_1.id != globalheaderid)
                continue;

            jsonObj = createJson(obj_1);

            for (var i = 0; i < obj_1.items.items.length; i++)
            {
                var obj_2 = obj_1.items.items[i];
                var innerJsonObj_1 = createJson(obj_2);

                if (obj_2.items.items.length > 0)
                {
                    for (var j = 0; j < obj_2.items.items.length; j++)
                    {
                        var obj_3 = obj_2.items.items[j];
                        var innerJsonObj_2 = createJson(obj_3);
                        innerJsonObj_1.data.push(innerJsonObj_2);
                    }

                }

                jsonObj.data.push(innerJsonObj_1);

            }

            if (jsonObj.isheader == true)
            {
                headerJson = jsonObj;
                headerHtml = document.getElementById(jsonObj.id).outerHTML;
            }

            if (jsonObj.isfooter == true)
            {
                footerJson = jsonObj;
                footerHtml = document.getElementById(jsonObj.id).outerHTML;

            }

            items.push(jsonObj);

        }
        return items;


    }


    function saveDocument(isPreview)
    {
        var obj1 = getElementJson(Ext.getCmp("idMainPanel"));
        var json = Ext.JSON.encode(obj1);
        var saveHeaderJson = (headerJson!=null)?Ext.JSON.encode(headerJson):"";
        var saveFooterJson = (footerJson!=null)?Ext.JSON.encode(footerJson):"";
        
        if(typeof pagelayoutproperty[0] !="object" && !IsJsonString(pagelayoutproperty[0])) {
            pagelayoutproperty[0] = {};
        }
        if(typeof pagelayoutproperty[1] !="object" && !IsJsonString(pagelayoutproperty[1])){
            pagelayoutproperty[1]={};
        }

        if (typeof pagelayoutproperty[2] != "object" && !IsJsonString(pagelayoutproperty[2])) {
            pagelayoutproperty[2] = {};
        }

        var html = Ext.getCmp("idMainPanel").el.dom.innerHTML;

        Ext.Ajax.request({
            url: "DocumentDesignController/saveDocument.do",
            params: {
                moduleid: _CustomDesign_moduleId,
                templateid: _CustomDesign_templateId,
                json: json,
                html: html,
                templatesubtype:_CustomDesign_templateSubtype,
                headerjson: saveHeaderJson,
                headerhtml: headerHtml,
                footerjson: saveFooterJson,
                footerhtml: footerHtml,
                pagelayoutproperty:JSON.stringify(pagelayoutproperty),
                ispreview:isPreview
            //                ,
            //                isdefault: "5"
            },
            success: function (response, req) {
                /*
                 var result = Ext.decode(response.responseText);
                 if (result.success && isValidSession(result)) {
                 WtfComMsgBox(["Success", "Template saved successfully."], 0);
                 }*/
            var res = eval('('+response.responseText+')');
            if(isPreview){
                var mapForm = document.createElement("form");
                mapForm.target = "mywindow";
                mapForm.method = "post"; 
                mapForm.action = "CustomDesign/showSamplePreviewNew.do";

                // && and == are used for seperation 
                var params = "isShowSamplePreviewWithoutSave=true&moduleid="+_CustomDesign_moduleId + "&templateid="+_CustomDesign_templateId;
                var inputs =params.split('&');
                for(var i=0;i<inputs.length;i++){
                    var KV_pair = inputs[i].split('=');
                    var mapInput = document.createElement("input");
                    mapInput.type = "text";
                    mapInput.name = KV_pair[0];
                    mapInput.value = KV_pair[1];
                    mapForm.appendChild(mapInput); 
                }
                document.body.appendChild(mapForm);
                mapForm.submit();
                mapForm.remove();
            }else{
                if(res.success) {
                    document.getElementById("idDivSaveDoc").style.display = 'block';
                    var task = new Ext.util.DelayedTask(function () {
                        document.getElementById("idDivSaveDoc").style.display = 'none';
                    });
                    task.delay(1000, null, null, null);
                } else {
                    document.getElementById("idDivDefaultSaveDoc").style.display = 'block';
                    var task = new Ext.util.DelayedTask(function () {
                        document.getElementById("idDivDefaultSaveDoc").style.display = 'none';
                    });
                    task.delay(1000, null, null, null);
                }  
            }
        }
    });

    }


    function previewDocument()
    {
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = "CustomDesign/showSamplePreviewNew.do";
        var params = "moduleid="+_CustomDesign_moduleId + "&templateid="+_CustomDesign_templateId;
        var inputs =params.split('&');
        for(var i=0;i<inputs.length;i++){
            var KV_pair = inputs[i].split('=');
            var mapInput = document.createElement("input");
            mapInput.type = "text";
            mapInput.name = KV_pair[0];
            mapInput.value = KV_pair[1];
            mapForm.appendChild(mapInput); 
        }
        document.body.appendChild(mapForm);
        mapForm.submit();
        mapForm.remove();
//        var url = "CustomDesign/showSamplePreviewNew.do?moduleid="+_CustomDesign_moduleId + "&templateid="+_CustomDesign_templateId;
//        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
    //        
    //        Ext.Ajax.request({
    //            url: "DocumentDesignController/previewDocument.do",
    //            params: {
    //                moduleid: _CustomDesign_moduleId,
    //                templateid: _CustomDesign_templateId,
    //                json: "3",
    //                isdefault: "5"
    //            },
    //            success: function (response, req) {
    //                var result = Ext.decode(response.responseText);
    //
    //                var map = window.open("PrintPriview.jsp", "_blank");
    //                map.focus();
    //                // var map =  window.open("a4.jsp", "Map", "status=0,title=0,height=600,width=800,scrollbars=1");
    //                map.onload = function () {
    //                    result = (result.htmlbody[0]).replace("selected", "")
    //                    // map.document.getElementById("idPrintPriviewBody").innerHTML=""+result;
    //                    map.document.getElementById("page1").innerHTML = "" + result;
    //                }
    //
    //            }
    //        })
    }
        
    function loadDocument()
    {

        Ext.Ajax.request({
            url: "DocumentDesignController/loadDocument.do",
            params: {
                moduleid: _CustomDesign_moduleId,
                templateid: _CustomDesign_templateId,
                companyid: companyid,
                json: "3",
                isdefault: "5"
            },
            success: function (response, req) {
                var result = Ext.decode(response.responseText);
                var jsonBody = result.jsonBody;
                createExtElements(jsonBody);
                showElements("idMainPanel");
                selectedElement = "idMainPanel";
                initTreePanel();
            }
        })

    }

    function createExtElements(jsonBody, rowIndex)
    {
        /*
         
         Ext.fieldID = {
         insertText:1,
         insertField:2,
         insertImage:3,
         insertHLine:4,
         insertVLine:5,
         insertDrawBox:6,
         insertTable:11,
         insertGlobalTable : 12,
         insertRowPanel:13,
         insertColumnPanel:14,
         insertTabSpacer:15
         }
         
         **/
        var jsonRowobj = null;
        var mainPanel = null;
        var rowPanel = null;
        var columnPanel = null;
        var columnArray = [];
        jsonBody = Ext.JSON.decode(jsonBody);
        for (var i = 0; i < jsonBody.length; i++) {
            jsonRowobj = jsonBody[i];
            rowPanel = createPanel(jsonRowobj);
            mainPanel = Ext.getCmp("idMainPanel");
            if ( rowIndex === undefined ) {
                mainPanel.add(rowPanel);  /*Insert row*/
            } else  {
                mainPanel.insert(rowIndex,rowPanel);
            }
            for (var j = 0; j < jsonRowobj.data.length; j++)
            {

                var innerObj_1 = jsonRowobj.data[j];
                columnArray.push(innerObj_1);
            }

            /*Get new Array of Column for ID*/
            columnArray = addColumToRow(rowPanel, columnArray); /*Insert  column*/
            for (var k = 0; k < jsonRowobj.data.length; k++)
            {
                var tmpColJsonObj = jsonRowobj.data[k];
                columnPanel = columnArray[k];
                columnPanel = Ext.getCmp(columnPanel.id);

                if(tmpColJsonObj.data.length == 0){
                    columnPanel.update(space);
                }
                for (var l = 0; l < tmpColJsonObj.data.length; l++)
                {
                    var fieldObj = tmpColJsonObj.data[l];
                    var field = null;
                    if (fieldObj.fieldType == Ext.fieldID.insertText)
                    {
                        field = createTextField(columnPanel,fieldObj.labelhtml, fieldObj);

                    }
                    else if (fieldObj.fieldType == Ext.fieldID.insertField)
                    {
                        var xType = fieldObj.xType?fieldObj.xType:"0";
                        var isFormula = fieldObj.isFormula?fieldObj.isFormula:false;
                        field = createExtComponent_2(columnPanel, undefined, fieldObj.fieldType, fieldObj.labelhtml, fieldObj.x, fieldObj.y,null, fieldObj, fieldObj.label,xType, isFormula);
                    }
                    else if (fieldObj.fieldType == Ext.fieldID.insertImage)
                    {
                        field = createExtImgComponent(columnPanel, columnPanel, fieldObj.fieldType, fieldObj.src, fieldObj.x, fieldObj.y, fieldObj);
                    }
                    else if (fieldObj.fieldType == Ext.fieldID.insertDrawBox)
                    {
                        field = createBox(fieldObj);
                    }
                    else if (fieldObj.fieldType == Ext.fieldID.insertTable)
                    {
                        var isSummaryTable=false;
                        var summaryJson=summaryTableJson;
                        if(fieldObj.data){
                            if(fieldObj.data[0]){
                                if(fieldObj.data[0].isSummaryTable){
                                    isSummaryTable = true;
                                }    
                            }    
                        }
                        field = getlineitemstable(fieldObj.x, fieldObj.y, fieldObj.labelhtml, columnPanel, fieldObj, isSummaryTable , summaryJson);
                    }
                    else if (fieldObj.fieldType == Ext.fieldID.insertGlobalTable)
                    {
                        var borderedgetype = fieldObj.borderedgetype!=null?fieldObj.borderedgetype:"1";
                        var rowspacing = fieldObj.rowspacing!=null?fieldObj.rowspacing:"5";
                        var columnspacing = fieldObj.columnspacing!=null?fieldObj.columnspacing:"5"; 
                        var fieldAlignment = fieldObj.fieldAlignment!=null?fieldObj.fieldAlignment:"1"; 
                        var fixedrowvalue=fieldObj.fixedrowvalue!=null?fieldObj.fixedrowvalue:"";
                        var tableHeader=fieldObj.tableHeader!=null?fieldObj.tableHeader:false;
                        field = createGlobalTable(fieldObj.x, fieldObj.y, fieldObj.labelhtml, columnPanel, fieldObj.tableWidth, fieldObj.height,fixedrowvalue, Ext.fieldID.insertGlobalTable,borderedgetype,rowspacing,columnspacing,fieldAlignment,fieldObj,tableHeader);
                                                
                    }else if (fieldObj.fieldType == Ext.fieldID.insertDataElement)
                    {
                        field = createDataElementComponent(columnPanel, fieldObj.fieldType, fieldObj.dataelementhtml, fieldObj.x, fieldObj.y , fieldObj);
                    } else if ( fieldObj.fieldType == Ext.fieldID.insertAgeingTable) {
                        field = createAgeingDetailsTable(fieldObj, fieldObj.labelhtml, columnPanel, fieldObj.fieldType)
                    } else if ( fieldObj.fieldType == Ext.fieldID.insertGroupingSummaryTable) {
                        field = createGroupingSummaryTable(fieldObj, fieldObj.labelhtml, columnPanel, fieldObj.fieldType)
                    } else if (fieldObj.fieldType == Ext.fieldID.insertDetailsTable)
                    {
                        //get current id of details table
                        var old_id = fieldObj.detailsTableId;
                        //create new id for details table
                        if(detailsTableCount[fieldObj.detailsTableSubType_id] != undefined){
                            fieldObj.detailsTableId = fieldObj.detailsTableSubType_id+(parseInt(detailsTableCount[fieldObj.detailsTableSubType_id])+1);
                        } else{
                            fieldObj.detailsTableId = fieldObj.detailsTableSubType_id+1;
                        }
                        //replace old id with new created id in details table html
                        fieldObj.labelhtml = fieldObj.labelhtml.replace(new RegExp(old_id, 'g'), fieldObj.detailsTableId);
                        //get Details Table component
                        field = getDetailsTable(fieldObj.x, fieldObj.y, fieldObj.labelhtml, columnPanel, fieldObj, fieldObj.detailsTableId, false);
                    }

                    columnPanel.add(field);
                    columnPanel.doLayout();
                }
                var ele1 = Ext.get(columnPanel.id+"-innerCt");
                if(ele1) {
                    var linespacing = columnPanel.linespacing;
                    var children = ele1.dom.children.length;
                    for ( var index =0; index<children; index++ ) {
                        if(index!=(children-1)) {
                            ele1.dom.children[index].style.marginBottom = linespacing + "px";
                        }
                    }
                }

            }
            columnArray = [];


        }
        Ext.getCmp("idMainPanel").doLayout();
        if (rowIndex != undefined) {
            selectedElement = rowPanel.id;
            Ext.getCmp(selectedElement).addClass('selected');
            createPropertyPanel(selectedElement);
            setProperty(selectedElement);
            showElements(selectedElement);
        }

    }

    function createGlobalFieldsForLineTable(jsonBody,lineitem,isFirstRow)
    {
        var jsonRowobj = null;
        var columnPanel = null;
        var columns = [];
        jsonBody = Ext.JSON.decode(jsonBody);
        for (var i = 0; i < jsonBody.length; i++) {
            var id = isFirstRow ? ("firstRowid"+i):("lastRowid"+i);
            var td = document.getElementById(id);
            if(td && td.innerHTML){
                td.innerHTML="";
            }
            jsonRowobj = jsonBody[i];
            var columnPanel = createColumn(id,jsonRowobj);
            columns.push(columnPanel);
            for (var k = 0; k < jsonRowobj.data.length; k++)
            {
                var fieldObj = jsonRowobj.data[k];
                var field = null;
                if (fieldObj.fieldType == Ext.fieldID.insertText)
                {
                    field = createTextField(columnPanel,fieldObj.labelhtml, fieldObj);

                }
                else if (fieldObj.fieldType == Ext.fieldID.insertField)
                {
                    var xType = fieldObj.xType?fieldObj.xType:"0";
                    var isFormula = fieldObj.isFormula?fieldObj.isFormula:false;
                    field = createExtComponent_2(columnPanel, undefined, fieldObj.fieldType, fieldObj.labelhtml, fieldObj.x, fieldObj.y,null, fieldObj, fieldObj.label,xType, isFormula);
                }
                else if (fieldObj.fieldType == Ext.fieldID.insertImage)
                {
                    field = createExtImgComponent(columnPanel, columnPanel, fieldObj.fieldType, fieldObj.src, fieldObj.x, fieldObj.y, fieldObj);
                }
                else if (fieldObj.fieldType == Ext.fieldID.insertDrawBox)
                {
                    field = createBox(fieldObj);
                }
                else if (fieldObj.fieldType == Ext.fieldID.insertGlobalTable)
                {
                    var borderedgetype = fieldObj.borderedgetype!=null?fieldObj.borderedgetype:"1";
                    var rowspacing = fieldObj.rowspacing!=null?fieldObj.rowspacing:"5";
                    var columnspacing = fieldObj.columnspacing!=null?fieldObj.columnspacing:"5"; 
                    var fieldAlignment = fieldObj.fieldAlignment!=null?fieldObj.fieldAlignment:"1"; 
                    var fixedrowvalue=fieldObj.fixedrowvalue!=null?fieldObj.fixedrowvalue:"";
                    var tableHeader=fieldObj.tableHeader!=null?fieldObj.tableHeader:false;
                    field = createGlobalTable(fieldObj.x, fieldObj.y, fieldObj.labelhtml, columnPanel, fieldObj.tableWidth, fieldObj.height,fixedrowvalue, Ext.fieldID.insertGlobalTable,borderedgetype,rowspacing,columnspacing,fieldAlignment,fieldObj,tableHeader);

                }else if (fieldObj.fieldType == Ext.fieldID.insertDataElement)
                {
                    field = createDataElementComponent(columnPanel,fieldObj.fieldType, fieldObj.dataelementhtml, fieldObj.x, fieldObj.y,fieldObj);
                }

                columnPanel.add(field);
                columnPanel.doLayout();
                
                var ele1 = Ext.get(columnPanel.id+"-innerCt");
                if(ele1) {
                    var linespacing = columnPanel.linespacing;
                    var children = ele1.dom.children.length;
                    for ( var index =0; index<children; index++ ) {
                        if(index!=(children-1)) {
                            ele1.dom.children[index].style.marginBottom = linespacing + "px";
                        }
                    }
                }

            }
        }
        if(isFirstRow){
            lineitem.firstRowCells = columns;
        } else{
            lineitem.lastRowCells = columns;
        }
//        Ext.getCmp("idMainPanel").doLayout();
    }

    function getTemplateConfigurations(designerPanel, defaultFieldGlobalStore) {
        var panelItems = designerPanel.items.items;
        var items = [];
        for (var cnt = 0; cnt < panelItems.length; cnt++) {
            var obj = panelItems[cnt];
            if (obj.id == "contentImage" || obj.id == "linecanvas")
                continue;

            var initconfig = obj.initialConfig;
            var fieldTypeId = obj.fieldTypeId;
            if (fieldTypeId !== Ext.fieldID.insertTable && fieldTypeId !== Ext.fieldID.insertGlobalTable) {// check for line items (id==5).
                if (fieldTypeId != Ext.fieldID.insertImage && fieldTypeId != Ext.fieldID.insertDrawBox) {// check for image (id==3).
                    if (obj.el.dom.firstChild == null)
                        continue;
                    var label = obj.el.dom.firstChild.innerHTML == undefined ? obj.el.dom.firstChild.nodeValue :
                    (obj.el.dom.firstChild.innerHTML.indexOf("#") == 0 ? obj.el.dom.firstChild.innerHTML : obj.el.dom.firstChild.outerHTML);
                    var config = {
                        id: obj.id,
                        x: obj.x,
                        y: obj.y,
                        labelhtml: label,
                        height: obj.height,
                        width: obj.width,
                        fieldTypeId: obj.fieldTypeId,
                        selectfieldbordercolor: obj.style ? obj.style.borderColor : ''
                    };
                } else if (fieldTypeId == Ext.fieldID.insertDrawBox) { // If Drawbox
                    var label = obj.el.dom.firstChild.innerHTML == undefined ? obj.el.dom.firstChild.nodeValue :
                    (obj.el.dom.firstChild.innerHTML.indexOf("#") == 0 ? obj.el.dom.firstChild.innerHTML : obj.el.dom.firstChild.outerHTML);

                    var config = {
                        id: obj.id,
                        x: obj.x,
                        y: obj.y,
                        labelhtml: label,
                        height: obj.height,
                        width: obj.width,
                        fieldTypeId: obj.fieldTypeId,
                        backgroundColor: obj.el.dom.style ? obj.el.dom.style.backgroundColor : ''
                    };
                } else {// If Image
                    var xPos = document.getElementById(obj.id + '-rzwrap').style.left.replace("px", "") * 1;
                    var yPos = document.getElementById(obj.id + '-rzwrap').style.top.replace("px", "") * 1;
                    config = {
                        id: obj.id,
                        x: xPos,
                        y: yPos,
                        src: obj.src,
                        height: obj.height,
                        width: obj.width,
                        fieldTypeId: obj.fieldTypeId
                    };
                }
                // retrieve field id
                var content = obj.el.getHTML();
                var arr = content.match(/\{PLACEHOLDER:(.*?)}/g);
                if (arr && arr[0]) {
                    var matches = arr[0].replace(/\{|\}/gi, '').split(":");
                    config['placeholder'] = matches[1];
                    var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                    if (fieldTypeId < "25") {
                        if (rec) {
                            var recdata = rec.data;
                            config['reftablename'] = recdata.reftablename;
                            config['reftablefk'] = recdata.reftablefk;
                            config['reftabledatacolumn'] = recdata.reftabledatacolumn;
                            config['dbcolumnname'] = recdata.dbcolumnname;
                            config['fieldid'] = recdata.id;
                            config['label'] = recdata.label;
                            config['xtype'] = recdata.xtype;
                            config['customfield'] = recdata.customfield;
                        }
                    } else {
                        config['fieldid'] = config.id;
                        config['label'] = config.label;
                        config['xtype'] = config.xtype;
                    }
                }
                items.push(config);
            } else if (fieldTypeId == Ext.fieldID.insertGlobalTable) {
                //            var content = obj.el.getHTML();
                var elements = Ext.get(obj.id).select('div.x-resizable-handle').elements;
                for (var i = 0; i < elements.length; i++) {
                    elements[i].remove()
                }
                var content = obj.el.getHTML();
                var arr = content.match(/\{PLACEHOLDER:(.*?)}/g);
                if (arr && arr.length > 0) {
                    var lineitems = [];
                    for (var gCnt = 0; gCnt < arr.length; gCnt++) {
                        var childConfig = {};
                        var matches = arr[gCnt].replace(/\{|\}/gi, '').split(":");
                        childConfig['placeholder'] = matches[1];
                        var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                        if (rec) {
                            var recdata = rec.data;
                            childConfig['reftablename'] = recdata.reftablename;
                            childConfig['reftablefk'] = recdata.reftablefk;
                            childConfig['reftabledatacolumn'] = recdata.reftabledatacolumn;
                            childConfig['dbcolumnname'] = recdata.dbcolumnname;
                            childConfig['fieldid'] = recdata.id;
                            childConfig['label'] = recdata.label;
                            childConfig['xtype'] = recdata.xtype;
                            childConfig['customfield'] = recdata.customfield;
                        }
                        lineitems.push(childConfig);
                    }
                }
                config = {
                    id: obj.id,
                    x: obj.x,
                    y: obj.y,
                    labelhtml: content,
                    height: Ext.get(obj.id).getBox().height,
                    width: Ext.get(obj.id).getBox().width,
                    fieldTypeId: obj.fieldTypeId,
                    cellplaceholder: lineitems,
                    fixedrowvalue: obj.fixedrowvalue
                };
                items.push(config);
            } else {
                var elements = Ext.get(obj.id).select('div.x-resizable-handle').elements;
                if (elements) {
                    for (var i = 0; i < elements.length; i++) {
                        elements[i].remove()
                    }
                }
                /*new Line items of table-neeraj*/
                var content = obj.el.getHTML();
                var listItems = Ext.get('itemlistconfig' + designerPanel.id);
                if (listItems.dom.attributes[5] != undefined && listItems.dom.attributes[4] != undefined) {
                    this.tcolor = listItems.dom.attributes[5].nodeValue;
                    this.bmode = listItems.dom.attributes[4].nodeValue
                }
                if (listItems && listItems.dom.childNodes[0].nextElementSibling.rows[0].cells.length > 0) {
                    var lineitems = [];
                    var childItems = listItems.dom.childNodes[0].nextElementSibling.rows[0].cells;
                    for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
                        var child = childItems[itemcnt];
                        var colSetting = child.attributes;
                        var childConfig = {
                            'fieldid': colSetting[8].value,
                            'label': colSetting[0].value
                        };
                        for (var attrCnt = 0; attrCnt < colSetting.length; attrCnt++) {
                            if (colSetting[attrCnt].name != 'class' && colSetting[attrCnt].name != 'style')
                                childConfig[colSetting[attrCnt].name] = colSetting[attrCnt].value
                        }
                        lineitems.push(childConfig);
                    }
                    var lineConfig = {
                        'lineitems': lineitems,
                        id: obj.id,
                        x: obj.x,
                        y: obj.y,
                        height: Ext.get(obj.id).getBox().height,
                        width: Ext.get(obj.id).getBox().width,
                        fieldTypeId: obj.fieldTypeId,
                        labelhtml: content,
                        tablecolor: this.tcolor,
                        tablebordermode: this.bmode
                    //                    cellplaceholder:lineitems
                    };
                    items.push(lineConfig);
                }
            /* previous code*/
            //            if (listItems && listItems.dom.childNodes.length > 0) {
            //                var lineitems = [];
            //                var childItems = listItems.dom.childNodes;
            //                for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
            //                    var child = childItems[itemcnt];
            //                    var colSetting = child.attributes;
            //                    var childConfig = {'fieldid': child.value, 'label': child.innerHTML};
            //                    for (var attrCnt = 0; attrCnt < colSetting.length; attrCnt++) {
            //                        if (colSetting[attrCnt].name != 'class' && colSetting[attrCnt].name != 'style')
            //                            childConfig[colSetting[attrCnt].name] = colSetting[attrCnt].value
            //                    }
            //                    lineitems.push(childConfig);
            //                }
            //                var lineConfig = {'lineitems': lineitems, id: obj.id, x: obj.x, y: obj.y,height: Ext.get(obj.id).getBox().height,
            //                    width:Ext.get(obj.id).getBox().width, fieldTypeId: obj.fieldTypeId,labelhtml: content,cellplaceholder:lineitems};
            //                    items.push(lineConfig);
            //            }
            }
        }
        var returnConfig = [];
        returnConfig[0] = Ext.JSON.encode(items);
        returnConfig[1] = designerPanel.body.dom.innerHTML;
        return returnConfig;

    }

    //function createExtImgComponent(designerPanel, propertyPanel, fieldTypeId, src, X, Y, obj) {
    //    var field = Ext.create('Ext.Img', {
    //        width: obj ? obj.width : 80,
    //        height: obj ? obj.height : 60,
    //            x: (obj != null || obj != undefined) ? obj.x : '',
    //            y: (obj != null || obj != undefined) ? obj.y : '',
    //            draggable: true,
    //            initDraggable: function () {
    //                var me = this,
    //                        ddConfig;
    //                ddConfig = Ext.applyIf({
    //                    el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
    //                            //delegate: '#' + Ext.escapeId(me.header.id)
    //                }, me.draggable);
    //                me.dd = new Ext.util.ComponentDragger(this, ddConfig);
    //                me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
    //            }, //initDraggable
    //            resizable: true,
    //            fieldType: fieldTypeId,
    //            style: {
    //                borderColor: '#B5B8C8',
    //                borderStyle: 'solid',
    //                borderWidth: '1px',
    //                position: 'relative'
    //            },
    //            src: src,
    //            onRender: function () {
    //                this.superclass.onRender.call(this);
    //                // addPositionObjectInCollection(this);
    //                this.el.on('click', function (eventObject, target, arg) {
    //
    //                    var component = designerPanel.queryById(this.id)
    //                    if (component) {
    //                        if (Ext.getCmp('contentImage')) {
    //                            Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
    //                        }
    //                        eventObject.stopPropagation();
    //                        if (selectedElement != null || selectedElement != undefined)
    //                        {
    //                            Ext.get(selectedElement).removeCls("selected");
    //                        }
    //                        selectedElement = this.id;
    //                        Ext.getCmp(selectedElement).addClass('selected');
    //                        createPropertyPanel(selectedElement);
    //                        setProperty(selectedElement);
    //                        showElements(selectedElement);
    //                    }
    //                });
    //            },
    //            listeners: {
    //                onMouseUp: function (field) {
    //                    field.focus();
    //                }
    //                }
    //        })
    //        //field.on('drag',showAlignedComponent, field);
    //        //field.on('dragend',removeAlignedLine, field);
    //        return field;
    //    }



    function removeinitialComponents(removeheader, removefooter) {
        var componentnopresent = Ext.getCmp("idMainPanel").items.items.length;
        var removedcomponentjson = []; 
        var headerno = 0;
        var footerno = 0;
        if ( removeheader ) {
            var tempjson = getElementJson(Ext.getCmp("idMainPanel"));
            
            for(var k=0; k< tempjson.length; k++){
                if ( tempjson[k].isheader == true) {
                    headerno = k;
                }
            }    
            for ( var i = 0, j =0; i < componentnopresent; i++ ) {
                if ( i != headerno ) {
                    removedcomponentjson[j] = tempjson[i];
                    j++;
                }
            } 
        } else if( removefooter ){
            var tempjson = getElementJson(Ext.getCmp("idMainPanel"));
            
            for(var k=0; k< tempjson.length; k++){
                if ( tempjson[k].isfooter == true) {
                    footerno = k;
                }
            }    
            for ( var i = 0, j =0; i < componentnopresent; i++ ) {
                if ( i != footerno ) {
                    removedcomponentjson[j] = tempjson[i];
                    j++;
                }
            } 
        }else {
            removedcomponentjson = getElementJson(Ext.getCmp("idMainPanel"));
        }
        Ext.getCmp("idMainPanel").removeAll();
        return removedcomponentjson;
        
    }
    
    function importGlobalHeaderFooter(isHeader) {
        Ext.Ajax.request ({
            url: "DocumentDesignController/importGlobalHeaderFooter.do",
            params:{
                moduleid: _CustomDesign_moduleId,
                templateid: _CustomDesign_templateId,
                header:isHeader
            },
            success: function (response, req) {
                var result = Ext.decode(response.responseText);
                if (result.success == "true" && result.headerfooteravailable == "1")
                {
                    var json = result.json;
                    var isHTMLpresent = false;
                    var isHeaderpresent = false;
                    var isFooterpresent = false;
                    var removedcomponentjson;
                    if (  Ext.getCmp("idMainPanel").items.items.length > 0 ) {
                        var rows = Ext.getCmp("idMainPanel").items.items;
                        for(var i=0; i< rows.length; i++){
                            if ( rows[i].isheader == true && isHeader) {
                                isHeaderpresent = true;
                            } else if (rows[i].isfooter == true && !isHeader ) {
                                isFooterpresent = true;
                            } 
                        }
                        if (isHeader && isHeaderpresent) {
                            Ext.MessageBox.show({
                                title: 'Set as Global Header',
                                msg: 'Are you sure you want to Replace Header?',
                                buttons: Ext.MessageBox.YESNO,
                                icon: Ext.MessageBox.QUESTION,
                                fn: function (btn) {
                                    if ( btn == 'yes' ) {
                                        isHTMLpresent = true;
                                        removedcomponentjson = removeinitialComponents(true); 
                                        createExtElements(json);
                                        if ( isHTMLpresent ) {
                                            createExtElements(JSON.stringify(removedcomponentjson));
                                        }
                                        initTreePanel();
                                    } else if ( btn == 'no' ) {
                                        isHTMLpresent = false;
                                    }
                                }
                            });
                        } else if(isFooterpresent){
                            Ext.MessageBox.show({
                                title: 'Set as Global Footer',
                                msg: 'Are you sure you want to Replace Footer?',
                                buttons: Ext.MessageBox.YESNO,
                                icon: Ext.MessageBox.QUESTION,
                                fn: function (btn) {
                                    if ( btn == 'yes' ) {
                                        isHTMLpresent = true;
                                        removedcomponentjson = removeinitialComponents(false,true); 
                                        createExtElements(json);
                                        if ( isHTMLpresent ) {
                                            createExtElements(JSON.stringify(removedcomponentjson));
                                        }
                                        initTreePanel();
                                    } else if ( btn == 'no' ) {
                                        isHTMLpresent = false;
                                    }
                                }
                            });
                        } else {
                            isHTMLpresent = true;
                            removedcomponentjson = removeinitialComponents(false); // do not remove any of the component
                            createExtElements(json);
                        }
                    } else {
                        createExtElements(json);
                    }
                    
                    if ( isHTMLpresent ) {
                        createExtElements(JSON.stringify(removedcomponentjson));
                    }
                    initTreePanel();

                }
                else if (result.success == "true" && result.headerfooteravailable == "0")
                {
                    if ( isHeader ) {
                        WtfComMsgBox(["Import Global Header", "Global header not found for this company."], 0);
                    } else  {
                        WtfComMsgBox(["Import Global Footer", "Global Footer not found for this company."], 0);
                    }
                }
                else
                {
                    if ( isHeader ) {
                        WtfComMsgBox(["Import Global Header", "Error occurred while importing Global Header."], 0);
                    } else {
                        WtfComMsgBox(["Import Global Footer", "Error occurred while importing Global Footer."], 0);
                    }

                }
            }
        })
    }
    
    Ext.dimensionAndCustomColumnWindow = function (conf) {
    Ext.apply(this, conf);
    this.addEvents({
        "okClicked": true
    });
    this.createEditor();
    
    var filtersarr = [];
    filtersarr.push({
        property: 'iscustom',
        value   : this.iscustom
    });
    filtersarr.push({
        property: 'isline',
        value   : this.isline
    });
    this.lineFieldsStore = new Ext.create('Ext.data.Store', {
        fields: ['fieldname', 'fieldlabel', 'iscustom', 'isline', 'customlabel',{name: 'sequence', type: 'int'}],
        data: defaultDimensionAndCustomArray,
        autoLoad: true,
        sorters: [
            {
                property : 'fieldlabel',
                direction: 'ASC'
            }
        ],
        filters: filtersarr
    });
    this.selectField = new Ext.form.field.ComboBox({
        fieldLabel:"Select Field",
        margin:'10 10 10 10',
        displayField:'fieldlabel',
        queryMode: 'local',
        valueField:'fieldname',
        store:this.lineFieldsStore,
        scope:this
    });
    this.addBtn = Ext.create('Ext.Button', {
        text: 'Add',
        margin:'10 10 10 10',
        id: 'idAddButtonForDimensionOrder',
        scope:this,
        handler:function(){
            if(this.selectField && this.selectField.getValue() != undefined && this.selectField.getValue != ""){
                var rec = searchRecord(this.lineFieldsStore, this.selectField.getValue(), 'fieldname');
                if(rec){
                    if(this.reportStore){
                            var recordPresent = false;
                            this.reportStore.each(function(record){
                                if(record.data.fieldlabel == rec.data.fieldlabel){
                                   recordPresent=true;
                                }
                            });
                            if(recordPresent){
                                Ext.MessageBox.show({
                                    title: 'Alert',
                                    msg: 'Selected column is already present',
                                    icon: Ext.MessageBox.QUESTION,
                                    buttons: Ext.MessageBox.OK
                                });
                            } else{
                                rec.data.sequence = this.reportStore.data.items.length + 1;
                                this.reportStore.add(rec);
                            }
                        }
                }
            }
        }
    });
    Ext.dimensionAndCustomColumnWindow.superclass.constructor.call(this, {
        width: 670,
        height: 420,
        resizable: false,
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF !important;background: #FFFFFF !important',
        title: this.iscustom?'Arrange the Custom Field(s) in Sequence ':'Arrange the Dimension(s) in Sequence ',
        modal: true,
        items: [
            {
                border:false,
                height:80,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                items:[{
                    xtype:"panel",
                    border:false,
                    height:70,
                    html:"<div style = 'width:100%;height:100%;position:relative;float:left;'><div style='float:left;height:100%;width:auto;position:relative;margin:5px 0px 0px 0px;'>"
                         +"<img src = ../../images/designer/Clolum-Sequence.png style='height:52px;margin:5px;width:40px;'></img></div><div style='float:left;height:100%;width:90%;position:relative;'>"
                         +"<div style='font-size:12px;font-style:bold;float:left;margin:7px 0px 0px 10px;width:100%;position:relative;'><b>Arrange the "+(this.iscustom?"Custom Field(s)":"Dimension(s)")+" in Sequence</b></div>"
                         +"<div style='font-size:10px;float:left;margin:5px 0px 10px 10px;width:100%;position:relative;'><ul style='list-style-type:disc;padding-left:15px;'>"
                         +"<li>Select a "+(this.iscustom?"Custom Field":"Dimension")+" from <b>Select Field</b> (Dropdown) and click on <b>Add</b> to add specific "+(this.iscustom?"Custom Field":"Dimension")+" </li></ul></div></div></div>"
                }]
            },
            {
                xtype: 'fieldset',
                title: '',
                anchor: '100%',
                height: 45,
                layout : 'column',
                bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF !important;background: #FFFFFF !important',
                items:[
                    {
                        columnWidth : .45,
                        border:false,
                        items:this.selectField
                    },
                    {
                        columnWidth : .55,
                        border:false,
                        items:this.addBtn
                    }
                ]
            },
            this.reportGrid
        ],
        buttons: [{
                text: "OK",
                scope: this,
                handler:function(){
                    var store = this.reportGrid.getStore();
                    if(store){
                        var records = store.data.items;
                        var cnt = 0;
                        for(var i=0 ; i < records.length ; i++ ){
                            var rec= records[i];
                            if(rec.data){
                                if(!rec.data.hidecol){
                                    cnt++;
                                }
                            }
                        }
                        if(cnt>0){
                            this.okClicked();
                        } else{
                            Ext.MessageBox.show({
                                title: 'Alert',
                                msg: 'Please select at least one column',
                                icon: Ext.MessageBox.QUESTION,
                                buttons: Ext.MessageBox.OK
                            });
                        }
                    }
                } 
            }, {
                text: "Cancel",
                scope: this,
                handler: this.cancelClicked
            }]
    });
};

Ext.extend(Ext.dimensionAndCustomColumnWindow, Ext.Window, {
    onRender: function (conf) {
        Ext.dimensionAndCustomColumnWindow.superclass.onRender.call(this, conf);
    },
    createEditor: function () {
        this.reportCM = [
            {
                header: 'Fields',
                dataIndex: "fieldlabel",
                width: 200
            }, 
            {
                header: 'iscustom',
                dataIndex: "iscustom",
                hidden:true
            },{
                header: 'isline',
                dataIndex: "isline",
                hidden:true
            },
            {
                header: 'fieldname',
                dataIndex: "fieldname",
                hidden:true
            },
            {
                header: 'Custom Header Name',
                dataIndex: "customlabel",
                editor: new Ext.form.TextField({
                    validateOnBlur: true,
                    allowBlank: false
                }),
                width: 200
            },
            {
                header: 'sequence',
                dataIndex: "sequence",
                hidden:true
            },{
                header: 'Actions',
                dataIndex: 'id',
                width: 150,
                renderer: function (value, css, record, row, column, store) {
                    var actions = "<image src='images/up.png' title='Move Up' onclick=\"changesequence('" + record.get('sequence') + "',0, 'dimensionAndCustomColumnGrid')\"/>" +
                            "<image src='images/down.png' style='padding-left:5px' title='Move Down' onclick=\"changesequence('" + record.get('sequence') + "',1, 'dimensionAndCustomColumnGrid')\"/>";
                    return actions;
                }
            },
            {
                header: 'Remove',
                width: 150,
                renderer: function (value, css, record, row, column, store) {
                    return "<image src='images/Delete.png' onclick=\"deleteRec('" + record.get('fieldname') + "', 'dimensionAndCustomColumnGrid')\"/>";
                }
            }
        ];
        var previousObj = Ext.getCmp(selectedElement);
        
        var data;
        if(this.iscustom){
            if(this.div && this.div.attributes.getNamedItem("customfieldorder")){
                data = eval("(" + this.div.attributes["customfieldorder"].value + ")");
            } else if( previousObj && previousObj.customfieldorder ){
                data =  eval("(" + previousObj.customfieldorder + ")");
            }
        } else{
            if(this.div && this.div.attributes.getNamedItem("dimensionorder")){
                data =  eval("(" + this.div.attributes["dimensionorder"].value + ")");
            } else if( previousObj && previousObj.dimensionorder ){
                data =  eval("(" + previousObj.dimensionorder + ")");
            }
        }
        this.reportStore = new Ext.create('Ext.data.Store', {
            fields: ['fieldname', 'fieldlabel', 'iscustom', 'isline', 'customlabel',{name: 'sequence', type: 'int'}],
            data: data?data:"",
            autoLoad: true
        });
        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1
        });
        this.reportGrid = Ext.create('Ext.grid.Panel', {
            columns: this.reportCM,
            store: this.reportStore,
            clicksToEdit: 1,
            height:280,
            renderTo: this.id,
            id: 'dimensionAndCustomColumnGrid', 
            plugins: [cellEditing]
        });
    },
    okClicked: function (obj) {
        if (this.fireEvent("okClicked", this))
            this.close();
    },
    cancelClicked: function (obj) {
        this.close();
    },
    getGridConfigSetting: function () {
        var store = this.reportGrid.getStore();
        var recCount = store.getCount();
        var arr = [];
        for (var cnt = 0; cnt < recCount; cnt++) {
            var record = store.getAt(cnt);
            if (record.data.customlabel.trim() == "") {
                WtfComMsgBox(["Alert", "Please enter a valid header name"], 0)
                return false;
            }
            arr.push(store.indexOf(record));
        }
        var jarray = getJSONArrayNew(this.reportGrid, true, arr);
        return jarray;
    }
});
function openDimensionAndCustomColumnWindow( iscustom , isline , iscustom, div) {
    var dcWin = new Ext.dimensionAndCustomColumnWindow({
        iscustom:iscustom,
        isline:isline,
        div:div,
        iscustom:iscustom
    });
    dcWin.on("okClicked", function (obj) {
        var valObj = obj.getGridConfigSetting();
        if(valObj){
            if(this.iscustom){
                if(this.div){
                   this.div.setAttribute("customfieldorder",valObj);
                } else{
                   Ext.getCmp(selectedElement).customfieldorder = valObj;
                }
            } else{
                if(this.div){
                   this.div.setAttribute("dimensionorder",valObj);
                } else{
                   Ext.getCmp(selectedElement).dimensionorder = valObj;
                }
            }
        }
    });
    dcWin.show();
}

function openFormulaBuilderWindow(fieldTypeId, isForGlobal, isForGlobalTableCell, isForLineItem){
    this.saveFormulaWindow = Ext.create('FormulaBuilderDocumentDesigner', {
    isSaveAndCreateNew : true,
    moduleId: _CustomDesign_moduleId,
    moduleName:'',
    fieldSelectionGrid:this.fieldSelectionGrid,
    createStore : function(){
        var url = 'CustomDesign/getGlobalFieldsData.do';
        if(isForLineItem){
            url = 'CustomDesign/getLineFieldsData.do';
        }
        var gridStore = Ext.create('Ext.data.Store', {
            id: 'measurFieldGridStoreId',
            model: 'FieldsModelDocumentDesigner',
            autoLoad: true,
            timeout : 180000,
            groupField: "columntype",
            proxy: {
                type: 'ajax',
                url: url,
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    root: "data"
                },
                extraParams : {
                    id: this.moduleId,
                    xtype:Ext.fieldID.insertField,
                    isforformulabuilder:true,
                    templatesubtype:_CustomDesign_templateSubtype
                }
            }
        });
        return gridStore;
    },
    updateFormulaFieldTextValue:function(){
       var expression=""; 
        for(var i=0;i<this.operatormeasurefieldsDrop.length;i++){
            if(this.operatormeasurefieldsDrop[i].isnumber!=undefined && this.operatormeasurefieldsDrop[i].isnumber){
                    expression +=this.operatormeasurefieldsDrop[i].defaultHeader;
                }else if(this.operatormeasurefieldsDrop[i].isnumber!=undefined && !this.operatormeasurefieldsDrop[i].isnumber){
                    expression +=this.operatormeasurefieldsDrop[i].defaultHeader+" ";
                }else{
                    expression +="#"+this.operatormeasurefieldsDrop[i].defaultHeader+"# ";
                }
        }
        this.formulaText.setValue(expression);
    },
    saveFormula : function (){
        var saveflag=this.validatebeforesave();
        if(saveflag){
            if(isForGlobal){
                createSelectField(this.measureName.getValue(), this.formulaText.getValue(), fieldTypeId, true);// isFormula is always true from here
            } else if(isForGlobalTableCell){
                
            } else if(isForLineItem){
                
            }
            this.close();
            Ext.getCmp('enterfieldlabelwin').close();
            Ext.getCmp(selectedElement).doLayout();
//            win.destroy();
            initTreePanel();
        }
    }
    });
    
    return this.saveFormulaWindow;
}

function deleteRec(columnname, gridid) {
    var store = Ext.getCmp(gridid).getStore();
    var index = store.find('fieldname', columnname);
    var record = store.getAt(index);
    store.remove(record);
    
    for(var i=index; i < store.data.length; i++){
        var rec = store.getAt(i);
        rec.data.sequence = parseInt(rec.data.sequence)-1;
    }
    Ext.getCmp(gridid).getView().refresh();
}
function changesequence(seq, flag, gridid) {
    var store = Ext.getCmp(gridid).getStore();
    var index1 = store.find('sequence', seq);
    var orgseq = seq;
    if (index1 > -1) {
        if (flag == "1") {
            seq++;
        } else if (flag == "0") {
            seq--;
        }
        var record1 = store.getAt(index1);
        var index2 = store.find('sequence', seq);
        if (index2 > -1) {
            var record2 = store.getAt(index2);
            store.remove(record1);
            store.remove(record2);
            if (flag == "0") {
                store.insert(index2, record1);
                store.insert(index1, record2);
            } else if (flag == "1") {
                store.insert(index1, record2);
                store.insert(index2, record1);
            }
            record1.set('sequence', seq);
            record2.set('sequence', orgseq);
            Ext.onlyhighLightRow(Ext.getCmp(gridid),"FFFF00",1000, index2);
        }
    }
}
function getJSONArrayNew(grid, includeLast, idxArr) {
    var indices = "";
    if (idxArr)
        indices = ":" + idxArr.join(":") + ":";
    var store = grid.getStore();
    var arr = [];
    //        var fields=store.fields;
    var len = store.getCount() - 1;
    if (includeLast)
        len++;
    for (var i = 0; i < len; i++) {
        if (idxArr && indices.indexOf(":" + i + ":") < 0)
            continue;
        var recarr = [];
        var recData = store.getAt(i).data;
        for (var prop in recData) {
            if(typeof recData[prop] === 'number'){
                recarr.push("'" + prop + "':" + recData[prop]);
            }else{
                recarr.push("'" + prop + "':'" + recData[prop] + "'");
            }
        }

        //            for(var j=0;j<fields.length;j++){
        //                var value=rec.data[fields.get(j).name];
        //                recarr.push(fields.get(j).name+":"+value);
        //            }
        arr.push("{" + recarr.join(",") + "}");
    }
    return "[" + arr.join(',') + "]";
}

Ext.onlyhighLightRow = function(EditorGrid,color,duration,row) {
    var rowEl = EditorGrid.getView().getNode(row);
    if(rowEl!=undefined){
        Ext.fly(EditorGrid.getView().getNode(row)).highlight(color,{ 
              attr: 'backgroundColor', duration: duration 
        });
    }
}
//    function importGlobalHeader()
//    {
//
//        Ext.Ajax.request({
//            url: "DocumentDesignController/importGlobalHeader.do",
//            params: {
//                moduleid: _CustomDesign_moduleId,
//                templateid: _CustomDesign_templateId
//            },
//            success: function (response, req) {
//                var result = Ext.decode(response.responseText);
//                if (result.success == "true" && result.headeravailable == "1")
//                {
//                    var json = result.json;
//                    createExtElements(json);
//                    initTreePanel();
//
//                }
//                else if (result.success == "true" && result.headeravailable == "0")
//                {
//                    WtfComMsgBox(["Import Global Header", "Global header not found for this company."], 0);
//                }
//                else
//                {
//                    WtfComMsgBox(["Import Global Header", "Error occurred while importing Global Header."], 0);
//
//                }
//
//
//
//            }
//        })
//
//
//    }
//
//    function importGlobalFooter()
//    {
//
//
//        Ext.Ajax.request({
//            url: "DocumentDesignController/importGlobalFooter.do",
//            params: {
//                moduleid: _CustomDesign_moduleId,
//                templateid: _CustomDesign_templateId
//            },
//            success: function (response, req) {
//                var result = Ext.decode(response.responseText);
//                if (result.success == "true" && result.footeravailable == "1")
//                {
//                    var json = result.json;
//                    createExtElements(json);
//                    initTreePanel();
//
//                }
//                else if (result.success == "true" && result.footeravailable == "0")
//                {
//                    WtfComMsgBox(["Import Global Footer", "Global footer not found for this company."], 0);
//                }
//                else
//                {
//                    WtfComMsgBox(["Import Global Header", "Error occurred while importing Global Footer."], 0);
//
//                }
//
//            }
//        })
//    }

function getIndexByExactMatch(store, ID, idname) {
    var index = store.findBy(function (record) {
        if (record.get(idname) == ID) {
            return true;
        } else {
            return false;
        }
    });
    
    if (index == -1) {
        return null;
    }

    return index;
}
