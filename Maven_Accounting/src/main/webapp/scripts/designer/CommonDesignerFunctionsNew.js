/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


function getPosition(element) {
    var xPosition = 0;
    var yPosition = 0;

    while (element) {
        xPosition += (element.offsetLeft - element.scrollLeft + element.clientLeft);
        yPosition += (element.offsetTop - element.scrollTop + element.clientTop);
        element = element.offsetParent;
    }
    return {x: xPosition, y: yPosition};
}

//function configureItemList(obj, designerPanel, propertyPanel) {
//    var jArr = eval(obj.lineitems);
//    var tab1Row = document.createElement("ul");
//    tab1Row.setAttribute("id", "itemlistconfig"+designerPanel.id);
//    tab1Row.setAttribute('style', 'padding-left: inherit;width:100%;');
////                var width = ((this.width -100) / jArr.length);
////                var width = (100 / jArr.length);
//    for (var cnt = 0; cnt < jArr.length; cnt++) {
//        var item = jArr[cnt];
//        if (!item.hidecol) {
//            var tab2Row = document.createElement("li");
//            tab2Row.setAttribute("class", "tpl-colname");
//            var widthInPercent = (item.colwidth - 2) + "%";// decreased 2% because while rendering header in html we used marging of 2%
//            tab2Row.setAttribute('style', 'width: ' + widthInPercent);
//            tab2Row.setAttribute("colwidth", item.colwidth);
//            tab2Row.setAttribute("coltotal", item.coltotal);
//            tab2Row.setAttribute("fieldid", item.fieldid);
//            tab2Row.setAttribute("showtotal", item.showtotal);
//            tab2Row.setAttribute("seq", item.seq);
//            tab2Row.setAttribute("xtype", item.xtype);
//            tab2Row.setAttribute("headerproperty", item.headerproperty);
//            tab2Row.value = item.fieldid;
//            tab2Row.innerHTML = decodeURIComponent(item.label);
//            tab1Row.appendChild(tab2Row);
//        }
//    }
//    var html = tab1Row.outerHTML;
//
//    var field = Ext.create(Ext.TemplateHolder, {
//        x: 0,
//        y: obj.y,
//        draggable: true,
//        containerId : designerPanel.id,
////                    width : 650,
//        height: 45,
//        fieldTypeId: Ext.fieldID.insertTable,
//        cls: 'tpl-content',
//        style: {width: '100%', height: 'auto !important', borderColor: '#B5B8C8', borderStyle: 'solid', borderWidth: '1px', position: 'absolute'},
//        bodyHtml: html,
//        listeners: {
//            onMouseUp: function(field) {
//                field.focus();
//            },
//            removed: function() {
////                onDeleteUpdatePropertyPanel();
////                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
//            },
//            afterrender: function() {
//                this.el.on('click', function(eventObject, target, arg) {
//                    var component = designerPanel.queryById(this.id)
//
//                    if(component && Ext.getCmp('contentImage')) {
//                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
//                        eventObject.stopPropagation();
//                        getPropertyPanel(component, designerPanel, propertyPanel);
//                    }
//                });
//            }
//        }
//    });
//    return field;
//}

function getPropertyPanel(obj, designerPanel, propertyPanel) {


    var content = obj.el.dom.textContent;
    var fieldType = obj.fieldTypeId;
    if (designerPanel.id != "pagelayout1") {
        Ext.getCmp('propertypanel').enable();

        Ext.getCmp('propertiesbox1').setDisabled(true);
        if (obj.initialCls && obj.initialCls.indexOf("tpl-content") > -1) {// it is line item field
            content = "Line Items";
        } else if (fieldType == Ext.fieldID.insertImage) {//
            content = "Image";
        } else if (fieldType == Ext.fieldID.insertHLine) {//
            content = "Horizontal Line";
        }
        else if (fieldType == Ext.fieldID.insertTabSpacer) {
            content = "Amount in words";
        } else if (fieldType == Ext.fieldID.pagePanel) {
            content = "PostText Master";
        } else if (fieldType == '17') {
            content = "Created by";
        } else if (fieldType == Ext.fieldID.insertGlobalTable) {
            content = "Global Table";
        }
        Ext.getCmp('setfieldproperty').setValue(content);
        Ext.getCmp('hidden_fieldID').setValue(obj.id);
        Ext.getCmp('hidden_fieldType').setValue(obj.fieldTypeId);
        Ext.getCmp('hidden_allowformatting').setValue(obj.fieldTypeId != Ext.fieldID.insertTable || obj.fieldTypeId != Ext.fieldID.insertGlobalTable);
        var myEditor = Ext.getCmp('myDesignFieldEditor');
        myEditor.hide();
        Ext.getCmp('editformattingbtn').hide();
        if (fieldType != Ext.fieldID.insertTable && fieldType != Ext.fieldID.insertImage && fieldType != Ext.fieldID.insertHLine && fieldType != Ext.fieldID.insertGlobalTable) {// if not line items, image or horizontal line, then show htmleditor
            var value = obj.el.dom.firstChild.innerHTML == undefined ? obj.el.dom.firstChild.data : obj.el.dom.firstChild.innerHTML;
            if (is_html(value)) {
                myEditor.setValue(value);
            } else {
                myEditor.setValue('<div contenteditable="true">' + value + '</div>');
            }
            Ext.getCmp('hidden_fieldName').setValue(value);
            Ext.getCmp('setfieldproperty').setValue(value);
            myEditor.show();
            Ext.getCmp('editformattingbtn').show();
        }

        if (fieldType == Ext.fieldID.insertText || fieldType == Ext.fieldID.insertField || fieldType == Ext.fieldID.insertDrawBox || fieldType == Ext.fieldID.insertTable || fieldType == Ext.fieldID.insertGlobalTable) {// if line item, show edit button and on click on line item configuration window
            if (fieldType == Ext.fieldID.insertDrawBox || fieldType == Ext.fieldID.insertText || fieldType == Ext.fieldID.insertField) {
                Ext.getCmp('propertiesbox1').setDisabled(false);
            } else {
                Ext.getCmp('editformattingbtn').show();
            }

            if (fieldType == Ext.fieldID.insertTable) {// if line item, show edit button and on click on line item configuration window
                Ext.getCmp('editformattingbtn').show();
            }

            if (fieldType == Ext.fieldID.insertGlobalTable) {// if Global Table, hide edit button and on click on line item configuration window
                Ext.getCmp('editformattingbtn').hide();
            }
        }
        if (fieldType == Ext.fieldID.insertGlobalTable ) {// if Confluence 2
            var value = obj.el.dom.innerHTML;

            myEditor.setValue('<div contenteditable="true">' + value + '</div>');
            Ext.getCmp('hidden_fieldName').setValue(value);
            Ext.getCmp('setfieldproperty').setValue(value);
            myEditor.show();
            Ext.getCmp('editformattingbtn').show();

        }
        if (fieldType == Ext.fieldID.insertRowPanel) {// if Confluence 2
            var value = obj.el.dom.innerHTML;
            myEditor.setValue('<div contenteditable="true">' + value + '</div>');
            Ext.getCmp('hidden_fieldName').setValue(value);
            Ext.getCmp('setfieldproperty').setValue(value);
            myEditor.show();
            Ext.getCmp('editformattingbtn').show();


        }
        if (fieldType == Ext.fieldID.insertColumnPanel ) {// if Confluence 1/3
            var value = obj.el.dom.innerHTML;

            myEditor.setValue('<div contenteditable="true">' + value + '</div>');
            Ext.getCmp('hidden_fieldName').setValue(value);
            Ext.getCmp('setfieldproperty').setValue(value);
            myEditor.show();
            Ext.getCmp('editformattingbtn').show();

        }
        if (fieldType == Ext.fieldID.insertTabSpacer) {// if Confluence 3/4
            var value = obj.el.dom.innerHTML;
            myEditor.setValue('<div contenteditable="true">' + value + '</div>');
            Ext.getCmp('hidden_fieldName').setValue(value);
            Ext.getCmp('setfieldproperty').setValue(value);
            myEditor.show();
            Ext.getCmp('editformattingbtn').show();


        }

        /*Confluence 2/3*/
        if (fieldType == Ext.fieldID.insertGlobalTable || fieldType == Ext.fieldID.insertRowPanel || fieldType == Ext.fieldID.insertColumnPanel  || fieldType == Ext.fieldID.insertTabSpacer)
        {
            Ext.getCmp('idTextAlign').setDisabled(true);
            Ext.getCmp('idUnit').setDisabled(true);
            Ext.getCmp('idWidth').setDisabled(true);
            Ext.getCmp('idUseColon').setDisabled(true);
            Ext.getCmp('idColonAlign').setDisabled(true);
            Ext.getCmp('editformattingbtn').setDisabled(true);
            myEditor.setDisabled(true);

        } else if (fieldType == Ext.fieldID.insertImage)
{
            Ext.getCmp('idTextAlign').setDisabled(true);
            Ext.getCmp('idUnit').setDisabled(true);
            Ext.getCmp('idWidth').setDisabled(true);
            Ext.getCmp('idUseColon').setDisabled(true);
            Ext.getCmp('idColonAlign').setDisabled(true);
            Ext.getCmp('editformattingbtn').setDisabled(true);
            myEditor.setDisabled(true);


        }else if (fieldType == Ext.fieldID.insertHLine)
{
            Ext.getCmp('idTextAlign').setDisabled(true);
            Ext.getCmp('idUnit').setDisabled(true);
            Ext.getCmp('idWidth').setDisabled(true);
            Ext.getCmp('idUseColon').setDisabled(true);
            Ext.getCmp('idColonAlign').setDisabled(true);
            Ext.getCmp('editformattingbtn').setDisabled(true);
            myEditor.setDisabled(true);

        } else if (fieldType == Ext.fieldID.insertDrawBox)
{
            Ext.getCmp('idTextAlign').setDisabled(false);
            Ext.getCmp('idWidth').setDisabled(false);
            Ext.getCmp('editformattingbtn').setDisabled(false);
            myEditor.setDisabled(false);
            Ext.getCmp('idUnit').setDisabled(true);
            Ext.getCmp('idUseColon').setDisabled(true);
            Ext.getCmp('idColonAlign').setDisabled(true);



        }

        else
        {
            Ext.getCmp('idTextAlign').setDisabled(false);
            Ext.getCmp('idUnit').setDisabled(false);
            Ext.getCmp('idWidth').setDisabled(false);
            Ext.getCmp('idUseColon').setDisabled(false);
            Ext.getCmp('idColonAlign').setDisabled(false);
            Ext.getCmp('editformattingbtn').setDisabled(false);
            myEditor.setDisabled(false);
            Ext.getCmp('idWidth').setValue(obj.width);
            var flg = Ext.getCmp('idUseColon').getValue();
            if (flg == true) {
                Ext.getCmp("idColonAlign").setDisabled(false);
            } else {
                Ext.getCmp("idColonAlign").setDisabled(true);
            }


        }




    }
}

function clearHiddenFieldID() {
    Ext.getCmp('hidden_fieldID').setValue('');
}
function createHoLineComponent(designerPanel, fieldTypeId, propertyPanel, X, Y, widthnew) {
    var field = Ext.create('Ext.Component', {
        x: X,
        y: Y,
        height: 20,
        width: (widthnew == undefined || widthnew == 0) ? 180 : widthnew,
        style: {
            borderColor: '#B5B8C8',
            borderStyle: 'solid',
            borderWidth: '1px',
            position: 'absolute'
        },
        fieldDefaults: {
            anchor: '100%'
        },
        layout: {
            type: 'vbox',
            align: 'stretch'  // Child items are stretched to full width
        },
        draggable: true,
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
        resizable: true,
        fieldTypeId: fieldTypeId,
        html: "<hr>",
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function (eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if (component && Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            //                var component = designerPanel.queryById(this.id)
            //                if (component) {
            //                    getPropertyPanel(component, this.designerPanel, this.propertyPanel);
            //                }
            });
        },
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            },
            removed: function () {
                onDeleteUpdatePropertyPanel();
            //                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            }
        }
    });
    field.on('drag', showAlignedComponent, field);
    field.on('dragend', removeAlignedLine, field);
    return field;
}

/*Insert Global Table*/
function createGlobalTable(X, Y, tablehtml, designerPanel, widthnew, heightnew, fixedrowvalue, fieldTypeId,borderedgetype,rowspacing,columnspacing,fieldAlignment,obj,tableHeader) {
    
    var marginTop = (obj && obj.marginTop) || (obj && obj.marginTop === 0)  ? obj.marginTop.toString().replace('px','') + 'px'  : '0px'; //ERP-19449
    var marginBottom = (obj && obj.marginBottom) || (obj && obj.marginBottom === 0) ? obj.marginBottom.toString().replace('px','') + 'px' : '5px';//ERP-19449 // Bottom Margin is 5px for make separation from bottom elements in design
    var marginLeft = (obj && obj.marginLeft)|| (obj && obj.marginLeft === 0) ? obj.marginLeft.toString().replace('px','') + 'px' : '0px'; //ERP-19449
    var marginRight = (obj && obj.marginRight) || (obj && obj.marginRight === 0) ? obj.marginRight.toString().replace('px','') + 'px' : '0px'; //ERP-19449
    var tableWidth = (obj && obj.tableWidth)? obj.tableWidth : 100;
    var borderColor = (obj && obj.borderColor)? obj.borderColor : "#000000";
    var backgroundColor = (obj && obj.backgroundColor)? obj.backgroundColor : "#FFFFFF";
    var headerColor = (obj && obj.headerColor)? obj.headerColor : "#FFFFFF";
    var tableAlign = (obj && (obj.tableAlign || obj.tableAlign === 0))? obj.tableAlign : 1; // added extra check for checking tableAlign is 0 (Left) - ERP-18759 : [Document Designer] Global table Alignment issue.
    var isbalanceoutstanding = (obj && obj.isbalanceoutstanding)? obj.isbalanceoutstanding : false; 
    var isOutstandingMultipleCurrency = (obj && obj.isOutstandingMultipleCurrency)? obj.isOutstandingMultipleCurrency : false; 
    var isSummaryTable = (obj && obj.isSummaryTable)? obj.isSummaryTable : false; 
    var summaryTableHeight = (obj && (obj.summaryTableHeight != undefined && obj.summaryTableHeight != ""))? obj.summaryTableHeight : "leaveOnPage"; 
    var isjedetailstable = (obj && obj.isjedetailstable)? obj.isjedetailstable : false; 
    var pageSize = (obj && obj.pageSize)? obj.pageSize : 'a4'; 
    var pageOrientation = (obj && obj.pageOrientation)? obj.pageOrientation : 'Portrait'; 
    var cls = "";
    var ctcls = "";
    var containerclassfieldalignvalue = (designerPanel.fieldalignment != null || designerPanel.fieldalignment != undefined) ? designerPanel.fieldalignment : "1";
    if (containerclassfieldalignvalue == "2") {
        ctcls = "sectionclass_field_container_inline ";
    } else {
        ctcls = "sectionclass_table_container ";
    }
    if(tableHeader){ //Apply classes based on repeate row table or normal table
        cls = "sectionclass_field_without_border global_table_container repeaterowTableContainer";
        ctcls = "sectionclass_field_container sectionclass_repeat_table_container";
    } else{
        cls = "sectionclass_field_without_border global_table_container";
        ctcls = ctcls;
    }
    var field = Ext.create('Ext.Component', {
        x: X,
        y: Y,
        labelhtml:tablehtml,
        //height: (heightnew == undefined ||heightnew ==0) ? 180 : heightnew,
        width: (widthnew && fieldAlignment == "2")?widthnew + "%":"100%",
        unit:"",
        borderedgetype:borderedgetype,
        backgroundColor:backgroundColor,
        borderColor:borderColor,
        headerColor:headerColor,
        tableAlign:tableAlign,
        rowspacing:rowspacing,
        columnspacing:columnspacing,
        fixedrowvalue: fixedrowvalue,
        fieldAlignment:fieldAlignment,
        marginTop : marginTop,
        marginBottom:marginBottom,
        marginLeft:marginLeft,
        isExtendLineItem:obj.isExtendLineItem?obj.isExtendLineItem:false,
        marginRight:marginRight,
        tableWidth:tableWidth,
        isbalanceoutstanding:isbalanceoutstanding,
        isOutstandingMultipleCurrency:isOutstandingMultipleCurrency,
        isSummaryTable:isSummaryTable,
        summaryTableHeight:summaryTableHeight,
        pageSize:pageSize,
        pageOrientation:pageOrientation,
        isjedetailstable:isjedetailstable,
        adjustPageHeight:obj.adjustPageHeight?obj.adjustPageHeight:"0",
        tableHeader:tableHeader,
        //        style: {
        //            borderColor: '#B5B8C8'
        //
        //                    /* borderStyle: 'solid',
        //                     borderWidth: '1px',*/
        //                    //position: 'absolute'
        //        },
        fieldDefaults: {
            anchor: '100%'
        },
        layout: {
            type: 'vbox',
            align: 'stretch'  // Child items are stretched to full width
        },
        cls : cls,//ERP-18757 : [Document Designer] On Selecting Global table it should be shown as selected in designer
        ctCls : ctcls,
        draggable: true,
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
        resizable: false,
        fieldType: fieldTypeId,
        html: tablehtml,
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            if ( fieldAlignment == "2" ) {
                this.el.dom.style.display = "inline-table"; 
            } else {
                this.el.dom.style.display = "inline-block"; 
            }
            if( tableHeader ){ //If Repeate row table with header then set display as 'block' for page break
                this.el.dom.style.display = "block !important"; 
            }
            this.el.dom.style.marginTop = marginTop;        //Removed "px" appended because px is already appended in top
            this.el.dom.style.marginBottom = marginBottom;  //Removed "px" appended because px is already appended in top
            this.el.dom.style.marginLeft = marginLeft;      //Removed "px" appended because px is already appended in top
            this.el.dom.style.marginRight = marginRight;    //Removed "px" appended because px is already appended in top
            this.el.on('click', function (eventObject, target, arg) {
                var component = designerPanel.queryById(this.id);
                if (component && Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                }
                if (component) {
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
                }
            });
        },
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            },
            removed: function () {
            }
        }
    });
    return field;
}

function createDrawComponent(designerPanel, propertyPanel, fieldTypeId, changedhtml, x, y, widthnew, heightnew, backgroundColor) {
    var field = Ext.create('Ext.Component', {
        x: x,
        y: y,
        width: (widthnew == 0 || widthnew == undefined) ? 60 : widthnew,
        height: (heightnew == 0 || heightnew == undefined) ? 40 : heightnew,
        style: {
            bodyStyle: '',
            borderColor: '#B5B8C8',
            borderStyle: 'solid',
            borderWidth: '1px',
            position: 'relative',
            backgroundColor: backgroundColor
        },
        //        id: 'configuredrawboxtoset',
        draggable: true,
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
        resizable: true,
        fieldTypeId: fieldTypeId,
        html: changedhtml != undefined ? changedhtml : 'drawbox',
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function (eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if (component && Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            //                var component = designerPanel.queryById(this.id)
            //                if (component) {
            //                    getPropertyPanel(component, this.designerPanel, this.propertyPanel);
            //                }
            });
        },
        listeners: {
            onMouseUp: function (field) {
                field.focus();

            },
            removed: function () {
                onDeleteUpdatePropertyPanel();
            }
        }
    });
    field.on('drag', showAlignedComponent, field);
    field.on('dragend', removeAlignedLine, field);
    return field;
}

function addConfluence_2(designerPanel, propertyPanel, x, y, componentID)
{
    var itemCount = designerPanel.items.items.length;
    var newX = 8;
    var newY = 5;
    var maxHeightComp = null
    /*
     if(itemCount>2)
     {
     for(var i=4;i<itemCount;i++)
     {
     var tmpItem=designerPanel.items.items[i];
     if(tmpItem.y>=newY)
     {
     newY=tmpItem.y;
     maxHeightComp=tmpItem;
     
     }
     
     
     }
     newY=newY+maxHeightComp.height+5;
     
     }
     */

    var field = Ext.create('Ext.Component', {
        x: 5,
        y: y,
        id: "idConfluenceRoot" + y,
        fieldTypeId: componentID,
        width: designerPanel.width - 10,
        autoHeight: true,
        draggable: false,
        resizable: false,
        border: false,
        //resizeHandles:'s n',
        buildSettings: {
            "scopeResetCSS": true
        },
        autoEl: {
            html: "<div class='contentLayout2'>" +
        "<div class='columnLayout three-equal' data-layout='three-equal'>" +
        "<div contenteditable='true' class='cell normal tableCellBorder'>" +
        "<div class='innerCell' contenteditable='true'>&nbsp;</div>" +
        "</div>" +
        "<div contenteditable='true' class='cell normal tableCellBorder'>" +
        "<div class='innerCell' contenteditable='true'>&nbsp;</div>" +
        "</div>" +
        "</div >" +
        "</div >"
        },
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);

            this.el.on('click', function (eventObject, target, arg) {
                removeAbsoluteLayout();

                var component = designerPanel.queryById(this.id)
                if (component && Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            });


        },
        listeners: {
    }

    });




    return field;
}


function addConfluence_3(designerPanel, propertyPanel, x, y, componentID)
{
    var itemCount = designerPanel.items.items.length;
    var newX = 8;
    var newY = 5;
    var maxHeightComp = null
    /*
     if(itemCount>2)
     {
     for(var i=4;i<itemCount;i++)
     {
     var tmpItem=designerPanel.items.items[i];
     if(tmpItem.y>=newY)
     {
     newY=tmpItem.y;
     maxHeightComp=tmpItem;
     
     }
     
     
     }
     newY=newY+maxHeightComp.height+5;
     
     }
     */

    var field = Ext.create('Ext.Component', {
        x: 5,
        y: y,
        id: "idConfluenceRoot" + y,
        fieldTypeId: componentID,
        width: designerPanel.width - 10,
        autoHeight: true,
        draggable: false,
        resizable: false,
        border: false,
        //resizeHandles:'s n',
        buildSettings: {
            "scopeResetCSS": true
        },
        autoEl: {
            html: "<div class='contentLayout2'>" +
        "<div class='columnLayout three-equal' data-layout='three-equal'>" +
        "<div contenteditable='true' class='cell normal tableCellBorder'>" +
        "<div class='innerCell' contenteditable='true'>&nbsp;</div>" +
        "</div>" +
        "<div contenteditable='true' class='cell normal tableCellBorder'>" +
        "<div class='innerCell' contenteditable='true'>&nbsp;</div>" +
        "</div>" +
        "<div contenteditable='true' class='cell normal tableCellBorder'>" +
        "<div class='innerCell' contenteditable='true'>&nbsp;</div>" +
        "</div>" +
        "</div >" +
        "</div >"
        },
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);

            this.el.on('click', function (eventObject, target, arg) {
                removeAbsoluteLayout();

                var component = designerPanel.queryById(this.id)
                if (component && Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            });
        },
        listeners: {
            resize: Ext.Function.bind(function (comp, width, height,
                oldWidth, oldHeight, eOpts) {
                var el = comp.getEl();
            /*
                 var div1=el.el.dom.childNodes[0];
                 var div2=el.el.dom.childNodes[1];
                 div1.height=height;
                 div2.height=height;
                 
                 if(designerPanel.items.items.length>3)
                 {
                 var tmpItem=designerPanel.items.items[3];
                 // tmpItem.y=comp.y+comp.height+5;
                 }
                 designerPanel.doLayout();
                 */
            }, this)
        }

    });


    return field;

}


function addConfluence_13(designerPanel, propertyPanel, x, y, componentID)
{
    var itemCount = designerPanel.items.items.length;
    var newX = 8;
    var newY = 5;
    var maxHeightComp = null
    /*
     if(itemCount>2)
     {
     for(var i=4;i<itemCount;i++)
     {
     var tmpItem=designerPanel.items.items[i];
     if(tmpItem.y>=newY)
     {
     newY=tmpItem.y;
     maxHeightComp=tmpItem;
     
     }
     
     
     }
     newY=newY+maxHeightComp.height+5;
     
     }
     */

    var field = Ext.create('Ext.Component', {
        x: 5,
        y: y,
        id: "idConfluenceRoot" + y,
        fieldTypeId: componentID,
        width: designerPanel.width - 10,
        autoHeight: true,
        draggable: false,
        resizable: false,
        border: false,
        //resizeHandles:'s n',
        buildSettings: {
            "scopeResetCSS": true
        },
        autoEl: {
            html: "<div class='contentLayout2'>" +
        "<div class='columnLayout three-equal' data-layout='three-equal'>" +
        "<div contenteditable='true' class='cell_13 normal tableCellBorder'>" +
        "<div class='innerCell' contenteditable='true'>&nbsp;</div>" +
        "</div>" +
        "<div contenteditable='true' class='cell normal tableCellBorder'>" +
        "<div class='innerCell' contenteditable='true'>&nbsp;</div>" +
        "</div>" +
        "</div >" +
        "</div >"
        },
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);

            this.el.on('click', function (eventObject, target, arg) {
                removeAbsoluteLayout();

                var component = designerPanel.queryById(this.id)
                if (component && Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            });


        },
        listeners: {
    }

    });


    return field;
}

function addConfluence_23(designerPanel, propertyPanel, x, y, componentID)
{
    var itemCount = designerPanel.items.items.length;
    var newX = 8;
    var newY = 5;
    var maxHeightComp = null
    /*
     if(itemCount>2)
     {
     for(var i=4;i<itemCount;i++)
     {
     var tmpItem=designerPanel.items.items[i];
     if(tmpItem.y>=newY)
     {
     newY=tmpItem.y;
     maxHeightComp=tmpItem;
     
     }
     
     
     }
     newY=newY+maxHeightComp.height+5;
     
     }
     */

    var field = Ext.create('Ext.Component', {
        x: 5,
        y: y,
        id: "idConfluenceRoot" + y,
        fieldTypeId: componentID,
        width: designerPanel.width - 10,
        autoHeight: true,
        draggable: false,
        resizable: false,
        border: false,
        //resizeHandles:'s n',
        buildSettings: {
            "scopeResetCSS": true
        },
        autoEl: {
            html: "<div class='contentLayout2'>" +
        "<div class='columnLayout three-equal' data-layout='three-equal'>" +
        "<div contenteditable='true' class='cell_23 normal tableCellBorder'>" +
        "<div class='innerCell' contenteditable='true'>&nbsp;</div>" +
        "</div>" +
        "<div contenteditable='true' class='cell normal tableCellBorder'>" +
        "<div class='innerCell' contenteditable='true'>&nbsp;</div>" +
        "</div>" +
        "</div >" +
        "</div >"
        },
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);

            this.el.on('click', function (eventObject, target, arg) {
                removeAbsoluteLayout();

                var component = designerPanel.queryById(this.id)
                if (component && Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            });


        },
        listeners: {
    }

    });


    return field;
}


function addPanel(designerPanel, propertyPanel, x, y, componentID)
{
    var field = Ext.create('Ext.Panel', {
        x: 5,
        y: y,
        id: "idConfluenceRoot" + y,
        text: 'Panel',
        fieldTypeId: componentID,
        width: designerPanel.width - 10,
        height: 200,
        //draggable:true,
        resizable: false,
        border: true,
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);

            this.el.on('click', function (eventObject, target, arg) {
                removeAbsoluteLayout();

                var component = designerPanel.queryById(this.id)
                if (component && Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            });


        }
    });

    return field;
}
function addConfluence_3_new(designerPanel, propertyPanel, x, y, componentID)
{
    var itemCount = designerPanel.items.items.length;
    var newX = 8;
    var newY = 5;
    var maxHeightComp = null
    /*
     if(itemCount>2)
     {
     for(var i=4;i<itemCount;i++)
     {
     var tmpItem=designerPanel.items.items[i];
     if(tmpItem.y>=newY)
     {
     newY=tmpItem.y;
     maxHeightComp=tmpItem;
     
     }
     
     
     }
     newY=newY+maxHeightComp.height+5;
     
     }
     */

    var field = Ext.create('Ext.Component', {
        x: 5,
        y: y,
        id: "idConfluenceRoot" + y,
        fieldTypeId: componentID,
        width: designerPanel.width - 10,
        autoHeight: true,
        draggable: false,
        resizable: false,
        border: false,
        //resizeHandles:'s n',
        buildSettings: {
            "scopeResetCSS": true
        },
        autoEl: {
            html: "<table width='100%'  cellpadding='5' cellspacing='5'>" +
        "<tbody>" +
        "<tr>" +
        "<td class='tableCellBorder' width='35%' align='left' valign='top' contenteditable='true' >&nbsp;</td>" +
        "<td class='tableCellBorder' width='32%' align='left' valign='top' contenteditable='true'>&nbsp;</td>" +
        "<td class='tableCellBorder' width='33%' align='left' valign='top' contenteditable='true'>&nbsp;</td>" +
        "</tr>" +
        "</tbody>" +
        "</table>"
        },
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);

            this.el.on('click', function (eventObject, target, arg) {
                removeAbsoluteLayout();

                var component = designerPanel.queryById(this.id)
                if (component && Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            });
        },
        listeners: {
            resize: Ext.Function.bind(function (comp, width, height,
                oldWidth, oldHeight, eOpts) {
                var el = comp.getEl();
            /*
                 var div1=el.el.dom.childNodes[0];
                 var div2=el.el.dom.childNodes[1];
                 div1.height=height;
                 div2.height=height;
                 
                 if(designerPanel.items.items.length>3)
                 {
                 var tmpItem=designerPanel.items.items[3];
                 // tmpItem.y=comp.y+comp.height+5;
                 }
                 designerPanel.doLayout();
                 */
            }, this)
        }

    });


    return field;

}



//delete
function deleteCustomDesignField() {
    if (isValidFieldSelected()) {
        var selectedField = Ext.getCmp('setfieldproperty').getValue();
        Ext.MessageBox.show({
            title: 'Confirm',
            msg: 'Do you really want to delete "' + selectedField + '" field?',
            icon: Ext.MessageBox.QUESTION,
            buttons: Ext.MessageBox.YESNO,
            scope: this,
            fn: function (button) {
                var designerp = Ext.getCmp('sectionPanelGrid');
                if (button == 'yes')
                {
                    var obj = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                    if (obj.fieldTypeId) {
                        Ext.tableinsert = false;
                    }
                    removePositionObjectFromCollection(Ext.getCmp('hidden_fieldID').getValue());
                    designerp.remove(obj, true);
                    removeAbsoluteLayout();
                } else {
                    return;
                }
            }
        });
    }
}

function enableSectionWindowButtons() {

}

//Used for Copy Button
function copyButton(designerpanel, propertypanel)
{
    if (isValidFieldSelected() && Ext.getCmp('hidden_allowformatting').getValue() == 'true') {
        var obj = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
        var items = [];
        var initconfig = obj.initialConfig;
        var fieldTypeId = obj.fieldTypeId;
        if (fieldTypeId != Ext.fieldID.insertTable) {// check for line items (id==5).
            if (fieldTypeId != Ext.fieldID.insertImage) {// check for image (id==3).
                var label = obj.el.dom.firstChild.innerHTML == undefined ? obj.el.dom.firstChild.nodeValue :
                (obj.el.dom.firstChild.innerHTML.indexOf("#") == 0 ? obj.el.dom.firstChild.innerHTML : obj.el.dom.firstChild.outerHTML);
                var config = {
                    x: obj.x,
                    y: obj.y,
                    labelhtml: label,
                    height: obj.height,
                    width: obj.width,
                    fieldTypeId: obj.fieldTypeId
                };
            } else {
                var xPos = document.getElementById(obj.id + '-rzwrap').style.left.replace("px", "") * 1;
                var yPos = document.getElementById(obj.id + '-rzwrap').style.top.replace("px", "") * 1;
                config = {
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
            }
            items.push(config);
            this.copyfield = items;
            Ext.getCmp('pasteformattingbtn').enable();
            Ext.getCmp('copyformattingbtn').disable();
        }
        else {
            var listItems = Ext.get('itemlistconfig');
            if (listItems && listItems.dom.childNodes.length > 0) {
                var lineitems = [];
                var childItems = listItems.dom.childNodes;
                for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
                    var child = childItems[itemcnt];
                    var colSetting = child.attributes;
                    var childConfig = {
                        'fieldid': child.value,
                        'label': child.innerHTML
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
                    width: obj.width,
                    fieldTypeId: obj.fieldTypeId
                };
                items.push(lineConfig);
            }
        }
    }
}

//Used for Paste Button
function pasteButton(designerPanel, propertyPanel) {
    var PosX = designerPanel.cursorCustomX;
    var PosY = designerPanel.cursorCustomY;
    var arr = this.copyfield;
    for (var cnt = 0; cnt < arr.length; cnt++) {
        var obj = arr[cnt];
        if (obj.fieldTypeId == Ext.fieldID.insertVLine) {
            var field = configureItemList(obj, designerPanel, propertyPanel)
        } else if (obj.fieldTypeId == Ext.fieldID.insertImage) {
            field = createExtImgComponent(designerPanel, propertyPanel, obj.fieldTypeId, obj.src, obj.x, obj.y, obj);
        } else if (obj.fieldTypeId == Ext.fieldID.insertHLine) {
            field = createHoLineComponent(designerPanel, obj.fieldTypeId, propertyPanel, PosX, PosY, obj.width);
        } else {
            var labelHTML = obj.fieldTypeId != Ext.fieldID.insertField ? obj.labelhtml : "<span attribute='{PLACEHOLDER:" + obj.fieldid + "}'>" + obj.labelhtml + "</span>";
            field = createExtComponent(designerPanel, propertyPanel, obj.fieldTypeId, labelHTML, PosX, PosY, {
                width: obj.width,
                height: obj.height
            });
        }
        designerPanel.items.add(field);
    }
    designerPanel.doLayout();

    Ext.getCmp('pasteformattingbtn').enable();
    Ext.getCmp('copyformattingbtn').enable();
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
                var label = obj.el.dom.firstChild.innerHTML == undefined ? obj.el.dom.firstChild.nodeValue :
                (obj.el.dom.firstChild.innerHTML.indexOf("#") == 0 ? obj.el.dom.firstChild.innerHTML : obj.el.dom.firstChild.outerHTML);
                if (fieldTypeId == Ext.fieldID.insertGlobalTable || fieldTypeId == Ext.fieldID.insertRowPanel 
                    || fieldTypeId == Ext.fieldID.insertColumnPanel || fieldTypeId == Ext.fieldID.insertTabSpacer)
                    label = obj.el.dom.innerHTML;
                var config = {id: obj.id, x: obj.x, y: obj.y, labelhtml: label,
                    border: false,
                    height: obj.height,
                    width: obj.width,
                    fieldTypeId: obj.fieldTypeId, selectfieldbordercolor: obj.style ? obj.style.borderColor : ''};
            } else if (fieldTypeId == Ext.fieldID.insertDrawBox) { // If Drawbox
                var label = obj.el.dom.firstChild.innerHTML == undefined ? obj.el.dom.firstChild.nodeValue :
                (obj.el.dom.firstChild.innerHTML.indexOf("#") == 0 ? obj.el.dom.firstChild.innerHTML : obj.el.dom.firstChild.outerHTML);

                var config = {id: obj.id, x: obj.x, y: obj.y, labelhtml: label,
                    height: obj.height,
                    width: obj.width,
                    fieldTypeId: obj.fieldTypeId, backgroundColor: obj.el.dom.style ? obj.el.dom.style.backgroundColor : ''};
            } else {// If Image
                var xPos = document.getElementById(obj.id + '-rzwrap').style.left.replace("px", "") * 1;
                var yPos = document.getElementById(obj.id + '-rzwrap').style.top.replace("px", "") * 1;
                config = {id: obj.id, x: xPos, y: yPos, src: obj.src,
                    height: obj.height,
                    width: obj.width,
                    fieldTypeId: obj.fieldTypeId};
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
                //                elements[i].remove()
                remove(elements[i]);        //Done for IE.
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
            config = {id: obj.id, x: obj.x, y: obj.y, labelhtml: content,
                height: Ext.get(obj.id).getBox().height,
                width: Ext.get(obj.id).getBox().width,
                fieldTypeId: obj.fieldTypeId, cellplaceholder: lineitems, fixedrowvalue: obj.fixedrowvalue};
            items.push(config);
        } else {
            var elements = Ext.get(obj.id).select('div.x-resizable-handle').elements;
            if (elements) {
                for (var i = 0; i < elements.length; i++) {
                    //                    elements[i].remove()
                    remove(elements[i]);       //Done for IE.
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


function saveAsWindow(json, html, defaultTemplate, saveflag, pagelayoutproperty) {
    var saveasform = new Ext.form.FormPanel({
        frame: true,
        labelWidth: 175,
        autoHeight: true,
        bodyStyle: 'padding:5px 5px 0',
        autoWidth: true,
        defaults: {
            width: 300
        },
        defaultType: 'textfield',
        items: [{
            fieldLabel: 'Template Name',
            id: 'templatenamefield',
            name: 'saveas',
            value: '',
            width: 300,
            allowBlank: false
        }],
        buttons: [{
            text: 'Save',
            handler: function () {
                var templatename = Ext.getCmp('templatenamefield').getValue();
                saveCustomDesign(json, html, defaultTemplate, saveflag, templatename, pagelayoutproperty, false);
                Ext.getCmp('saveaswin').close();
            },
            scope: this
        }, {
            text: 'Cancel',
            handler: function () {
                Ext.getCmp('saveaswin').close();
            },
            scope: this
        }]
    });
    var saveaswin = new Ext.Window({
        title: 'Save As',
        closable: true,
        width: 400,
        id: 'saveaswin',
        autoHeight: true,
        plain: true,
        modal: true,
        items: saveasform
    });
    saveaswin.show();
}
function saveCustomDesign(json, html, defaultTemplate, saveflag, templatename, pagelayoutproperty, allowReLoad, bandID) {
    Ext.Ajax.request({
        url: "CustomDesign/saveDesignTemplate.do",
        params: {
            moduleid: _CustomDesign_moduleId,
            templateid: saveflag ? _CustomDesign_templateId : '',
            templatename: templatename,
            json: json,
            html: html,
            isdefault: defaultTemplate,
            pagelayoutproperty: pagelayoutproperty,
            bandid: bandID
        },
        success: function (response, req) {
            var result = Ext.decode(response.responseText);
            if (result.success && isValidSession(result)) {
                if (allowReLoad && bandID != Ext.bandID.footer && Ext.bandID.header)
                    location.reload();
                else if (bandID != Ext.bandID.footer || bandID != Ext.bandID.header)
                    WtfComMsgBox(["Success", "Template saved successfully."], 0);
            }
        }
    })
}

function savePageForm() {
    var top, right, bottom, left, isportrait, pagefont;
    var ispagenumber = false, pagenoformat, pagenoalign;
    var portrait = Ext.getCmp('portrait');
    var landscape = Ext.getCmp('landscape');
    var pagenumber = Ext.getCmp('pagenumbercheck');
    var pagefooter = Ext.getCmp('pagefootercheck');
    var pagefontstyle = Ext.getCmp('pagefontstyleid');

    if (!portrait.collapsed == true) {
        isportrait = true;
        top = Ext.getCmp('portraittop').getValue();
        right = Ext.getCmp('portraitright').getValue();
        bottom = Ext.getCmp('portraitbottom').getValue();
        left = Ext.getCmp('portraitleft').getValue();
    }
    if (!landscape.collapsed == true) {
        isportrait = false;
        top = Ext.getCmp('landscapetop').getValue();
        right = Ext.getCmp('landscaperight').getValue();
        bottom = Ext.getCmp('landscapebottom').getValue();
        left = Ext.getCmp('landscapeleft').getValue();
    }
    var ispagefooter = "";
    if (!pagefooter.collapsed == true) {
        ispagefooter = true;
        var isHAlign = Ext.getCmp('horizontalalign').getValue() ? true : false;
        var hAlign = Ext.getCmp('horizontalalign').getValue();
        var headerheight = Ext.getCmp('headerheight').getValue();
        var footerheight = Ext.getCmp('footerheight').getValue();
        var isVAlign = Ext.getCmp('verticalalign').getValue() ? true : false;
        var vAlign = Ext.getCmp('verticalalign').getValue();
    }
    if (!pagenumber.collapsed == true) {
        if (Ext.getCmp('pagenumberformat').getValue()) {
            ispagenumber = true;
            pagenoformat = Ext.getCmp('pagenumberformat').getValue();
        } else {
            pagenoformat = "";
        }
        if (Ext.getCmp('pagenumberalignment').getValue())
            pagenoalign = Ext.getCmp('pagenumberalignment').getValue();
        else
            pagenoalign = "";
    }
    if (!pagefontstyle.collapsed == true) {
        pagefont = Ext.getCmp('pagefontid').getValue();
        Ext.getCmp("idMainPanel").body.setStyle('font-family', '' + pagefont + '');
    } else {
        pagefont = "sans-serif";
        Ext.getCmp("idMainPanel").body.setStyle('font-family', '' + pagefont + '');
    }
    this.pagejson = {
        "isportrait": isportrait,
        "top": top,
        "right": right,
        "bottom": bottom,
        "left": left,
        "pagefooter": {
            "ispagefooter": ispagefooter,
            "ishalign": isHAlign,
            "halign": hAlign,
            "isvalign": isVAlign,
            "valign": vAlign,
            "footerheight": footerheight,
            "headerheight": headerheight
        },
        "pagenumber": {
            "ispagenumber": ispagenumber,
            "pagenumberformat": pagenoformat,
            "pagenumberalign": pagenoalign
        },
        "pagefontstyle": {
            "fontstyle": pagefont
        }
    };
    //    var returnConfig= Ext.JSON.encode(this.pagejson);
    return this.pagejson;
}


function getlineitems(designerPanel, propertyPanel) {
    var field = Ext.create(Ext.TemplateHolder, {
        x: 0,
        y: 150,
        draggable: true,
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
        //                                width : 650,
        height: 45,
        fieldType: Ext.fieldID.insertTable,
        cls: 'tpl-content',
        style: {
            width: '100%',
            height: 'auto !important',
            borderColor: '#B5B8C8',
            borderStyle: 'solid',
            borderWidth: '1px',
            position: 'absolute'
        },
        bodyHtml: '<span class="tpl-content"> Click here to customize line items </span>', // this string is compared. Search with 'customize line items
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            },
            removed: function () {
                onDeleteUpdatePropertyPanel();
            //                                        propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            },
            afterrender: function () {
                addPositionObjectInCollection(this);
                this.el.on('click', function (eventObject, target, arg) {
                    var component = designerPanel.queryById(this.id)
                    if (component && Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                        eventObject.stopPropagation();
                        getPropertyPanel(component, designerPanel, propertyPanel);
                    }
                });
            }
        }
    });
    field.on('drag', showAlignedComponent, field);
    field.on('dragend', removeAlignedLine, field);
    return field;
}
function getTopHtml(text, body, img, isgrid, margin) {
    if (isgrid === undefined)
        isgrid = false;
    if (margin === undefined)
        margin = '15px 0px 10px 10px';
    if (img === undefined || img == null) {
        img = '../../images/createuser.png';
    }
    var str = "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
    + "<div style='float:left;height:100%;width:auto;position:relative;'>"
    + "<img src = " + img + "  class = 'adminWinImg'></img>"
    + "</div>"
    + "<div style='float:left;height:100%;width:70%;position:relative;'>"
    + "<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>" + text + "</b></div>"
    + "<div style='font-size:10px;float:left;margin:15px 0px 10px 10px;width:100%;position:relative;'>" + body + "</div>"
    + "</div>"
    + "</div>";
    return str;
}
Ext.designtable = function (config) {
    Ext.apply(this, {
        buttons: [{
            text: 'Submit',
            scope: this,
            handler: this.saveForm
        }, {
            text: 'Cancel',
            scope: this,
            handler: this.closeForm
        }]
    }, config);
    Ext.designtable.superclass.constructor.call(this, config);

}

Ext.extend(Ext.designtable, Ext.Window, {
    title: 'Insert Table',
    width: 400,
//    height: 250,
    modal:true,               //ERP-19208
    bodyStyle: 'background: #F5F5F5;',
    onRender: function (config) {
        Ext.designtable.superclass.onRender.call(this, config);
        this.createForm();
        var title = "Select Table Type";
        var msg = "";
        var isgrid = true;

        this.add({
            region: 'north',
            height: 90,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title, msg, "images/designer/insert-table.png", false)
        }, {
            region: 'center',
            border: false,
            bodyStyle: 'background: #F5F5F5;',
            items: [this.TypeForm]
        });
    },
    createForm: function () {
        var isLineItemTableActive = false; // Flag to enable Line Item Table 
        if((_CustomDesign_moduleId ==Ext.moduleID.Acc_Debit_Note_ModuleId && !(_CustomDesign_templateSubtype==Ext.Subtype.PurchaseReturn) && !(_CustomDesign_templateSubtype==Ext.Subtype.Undercharge)&& !(_CustomDesign_templateSubtype==Ext.Subtype.Overcharge))
            || (_CustomDesign_moduleId==Ext.moduleID.Acc_Credit_Note_ModuleId && !(_CustomDesign_templateSubtype==Ext.Subtype.SalesReturn)&& !(_CustomDesign_templateSubtype==Ext.Subtype.Undercharge)&& !(_CustomDesign_templateSubtype==Ext.Subtype.Overcharge))
            || _CustomDesign_moduleId == Ext.moduleID.Acc_Make_Payment_ModuleId
            || _CustomDesign_moduleId ==Ext.moduleID.Acc_Receive_Payment_ModuleId
            || (_CustomDesign_moduleId==Ext.moduleID.Acc_Sales_Order_ModuleId && _CustomDesign_templateSubtype==Ext.Subtype.JobOrder)
            || (_CustomDesign_moduleId==Ext.moduleID.Acc_Invoice_ModuleId && (_CustomDesign_templateSubtype==Ext.Subtype.JobOrderLabel || _CustomDesign_templateSubtype==Ext.Subtype.OpeningInvoice))){
            isLineItemTableActive = true;
        }
        
        //Store for details table sub type
        this.detailsTableSubTypeStore = Ext.create("Ext.data.Store", {
            fields: ["id", "value"],
            data: []
        });
        //MRP Work Order
        if(parseInt(_CustomDesign_moduleId) == Ext.moduleID.MRP_WORK_ORDER_MODULEID){
            this.detailsTableSubTypeStore = Ext.create("Ext.data.Store", {
                fields: ["id", "value"],
                data: [{
                    id: "component_availability",
                    value: "Component Availability"
                }, {
                    id: "tasks",
                    value: "Tasks"
                }, {
                    id: "consumption",
                    value: "Consumption"
                }]
            });
        } else if(parseInt(_CustomDesign_moduleId) == Ext.moduleID.Bank_Reconciliation_ModuleId){
            //Bank Reconciliation
            this.detailsTableSubTypeStore = Ext.create("Ext.data.Store", {
                fields: ["id", "value"],
                data: [{
                    id: "deposits_and_other_credits",
                    value: "Deposits and Other Credits"
                },{
                    id: "checks_and_payments",
                    value: "Checks and Payments"
                }]
            });
        } else {
            this.detailsTableSubTypeStore = Ext.create("Ext.data.Store", {
                fields: ["id", "value"],
                data: {
                    id: "gsttaxsummary",
                    value: "GST Tax Summary"
                }
            });
                }
        this.TypeForm = new Ext.form.FormPanel({
            region: 'center',
            bodyStyle: 'margin-left:10px;background: #F5F5F5;',
            anchor: '100%',
            autoScroll: true,
            border: false,
            defaultType: 'textfield',
            items: [{
                xtype: 'container',
                flex: 1,
                //                    title: 'Table Type',
                //                    height:auto,
                defaultType: 'radio', // each item will be a radio button
                layout: 'anchor',
                defaults: {
                    anchor: '80%',
                    hideEmptyLabel: false
                },
                style: {
                    width: '95%',
                    margin: '20px'
                },
                items: [{
                    checked: true,
                    fieldLabel: 'Select Table Type',
                    boxLabel: 'Global Section',
                    name: 'rectype',
                    id: 'radio1',
                    inputValue: '1'

                }, {
                    boxLabel: 'Line Items',
                    name: 'rectype',
                    inputValue: '2',
                    id: 'radio2', 
                    hidden: isLineItemTableActive,
                    disabled: isLineItemTableActive
                }, {
                    boxLabel: 'Grouping Summary',
                    name: 'rectype',
                    inputValue: '4',
                    id: 'radio4', 
                    hidden: isLineItemTableActive,
                    disabled: isLineItemTableActive
                },{
                    boxLabel: _CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID ? 'Checklist Table' : 'Ageing Details',
                    name: 'rectype',
                    inputValue: '3',
                    id: 'radio3', 
                    hidden: (_CustomDesign_moduleId != Ext.moduleID.MRP_WORK_ORDER_MODULEID) && (_CustomDesign_moduleId != Ext.moduleID.Acc_Customer_AccStatement_moduleid) && (_CustomDesign_moduleId != Ext.moduleID.Acc_Vendor_AccStatement_moduleid),
                    disabled: (_CustomDesign_moduleId != Ext.moduleID.MRP_WORK_ORDER_MODULEID) && (_CustomDesign_moduleId != Ext.moduleID.Acc_Customer_AccStatement_moduleid) && (_CustomDesign_moduleId != Ext.moduleID.Acc_Vendor_AccStatement_moduleid)
                },{//details table option under Insert Table menu
                    boxLabel: 'Details Table',
                    name: 'rectype',
                    inputValue: '5',
                    id: 'radio5', 
                    hidden: (_CustomDesign_moduleId == Ext.moduleID.Bank_Reconciliation_ModuleId || _CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID) || ((_CustomDesign_moduleId == Ext.moduleID.Acc_Invoice_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Purchase_Order_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Sales_Order_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Delivery_Order_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Goods_Receipt_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Receive_Payment_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Vendor_Quotation_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Customer_Quotation_ModuleI || _CustomDesign_moduleId == Ext.moduleID.Acc_Vendor_Invoice_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Sales_Return_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Purchase_Return_ModuleId) && (_countryid == Ext.countryid.INDIA || _countryid == Ext.countryid.USA)) ? false : true,
                    disabled: (_CustomDesign_moduleId == Ext.moduleID.Bank_Reconciliation_ModuleId || _CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID) || ((_CustomDesign_moduleId == Ext.moduleID.Acc_Invoice_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Purchase_Order_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Sales_Order_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Delivery_Order_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Goods_Receipt_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Receive_Payment_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Vendor_Quotation_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Customer_Quotation_ModuleI || _CustomDesign_moduleId == Ext.moduleID.Acc_Vendor_Invoice_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Sales_Return_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Purchase_Return_ModuleId) && (_countryid == Ext.countryid.INDIA || _countryid == Ext.countryid.USA)) ? false : true,
                    listeners: {
                        change: function (cb, newVal, oldVal) {
                            if(newVal){
                                getEXTComponent("detailsTableSubType").show();
                            } else{
                                getEXTComponent("detailsTableSubType").hide();
                            }
                        }
                    }
                },{//details table subtype combo
                    xtype: 'combo',
                    fieldLabel: 'SubType',
                    store: this.detailsTableSubTypeStore,
                    id: 'detailsTableSubType',
                    displayField: 'value',
                    valueField: 'id',
                    queryMode: 'local',
                    hidden: true,
                    emptyText: 'Select a SubType',
                    allowBlank: false,
                    listeners: {
                        scope: this,
                        'select': function (combo, selection) {
                            if(selection[0].data.xtype!="NAN"){
                                var htmllabel = selection[0].data.label
                                this.label = htmllabel;
                                this.xType = selection[0].data.xtype;
                            }
                        }
                    }
                }]
            }]
        });
    },
    closeForm: function () {
        this.close();
        this.destroy();
    },
    saveForm: function () {
        var rec = this.TypeForm.getForm().getValues();
        this.value = rec.rectype;
        //If Details Table option selected then get subtype details
        if( this.value == 5 ){
            this.detailsTableSubType_id = Ext.getCmp("detailsTableSubType").getValue();
            this.detailsTableSubType_value = Ext.getCmp("detailsTableSubType").getDisplayValue();
            //If subtype details are empty or not selected then restrict user for creating table
            if(this.detailsTableSubType_id == undefined || this.detailsTableSubType_id == "" || this.detailsTableSubType_value == undefined || this.detailsTableSubType_value == ""){
                Ext.Msg.show({
                    title      : 'Details Table',
                    msg        : 'Please select SubType first.',
                    width      : 370,
                    buttons    : Ext.MessageBox.OK,
                    icon       : Ext.Msg.WARNING
                });
                return;
            }
        }
        this.closeForm();
        if (this.value == 2)
        {
            var lineitemparentpanel = Ext.getCmp(selectedElement);
            var targetPanel = Ext.getCmp("idMainPanel");
            var component = targetPanel.queryById("itemlistcontainer");
            if (component) {
                Ext.Msg.show({
                    title      : 'Line Items',
                    msg        : 'Only one line item is applicable at a time',
                    width      : 370,
                    buttons    : Ext.MessageBox.OK,
                    icon       : Ext.Msg.WARNING
                });
            } else {
                var containerpanel = Ext.getCmp(selectedElement); 
                if ( containerpanel ) {
                    var itemlength =  containerpanel.items.items.length;
                    if ( itemlength > 0 ) {
                        WtfComMsgBox(["Warning", "Cannot add Line Item in the row containing one or more elements."], 0);
                        return;
                    }
                }
                openProdWindowAndSetConfig(this, lineitemparentpanel.id,false,undefined,undefined,documentLineColumns,lineitemparentpanel);
            }
        //            var field = getlineitems(designp, propanel);
        //            designp.items.add(field);
        //            designp.doLayout();
        } else if (this.value == 1) {
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
            addGlobalTable(this.parentScope);
        } else if ( this.value == 3 ) {
            var ageingTableparentpanel = Ext.getCmp(selectedElement);
            var targetPanel = Ext.getCmp("idMainPanel");
            var component = targetPanel.queryById("idageingtablecontainer");
            var tablename = (_CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID ? 'Checklist Table' : 'Ageing Details');
            if (component) {
                Ext.Msg.show({
                    title      : tablename,
                    msg        : 'Only one '+tablename+' is applicable at a time',
                    width      : 370,
                    buttons    : Ext.MessageBox.OK,
                    icon       : Ext.Msg.WARNING
                });
            } else {
                var containerpanel = Ext.getCmp(selectedElement); 
                if ( containerpanel ) {
                    var itemlength =  containerpanel.items.items.length;
                    if ( itemlength > 0 ) {
                        WtfComMsgBox(["Warning", "Cannot add "+tablename+" in the row containing one or more elements."], 0);
                        return;
                    }
                }
                openAgeingDetailsSettingWindow( ageingTableparentpanel,this,false);
            }
            
        } else if( this.value == 4 ){
            var groupingSummaryTableparentpanel = Ext.getCmp(selectedElement);
            var targetPanel = Ext.getCmp("idMainPanel");
            var component = targetPanel.queryById("idgroupingsummarytablecontainer");
            if (component) {
                Ext.Msg.show({
                    title      : 'Grouping Summary Table',
                    msg        : 'Only one Grouping Summary Table is applicable at a time',
                    width      : 370,
                    buttons    : Ext.MessageBox.OK,
                    icon       : Ext.Msg.WARNING
                });
            } else {
                var containerpanel = Ext.getCmp(selectedElement); 
                if ( containerpanel ) {
                    var itemlength =  containerpanel.items.items.length;
                    if ( itemlength > 0 ) {
                        WtfComMsgBox(["Warning", "Cannot add Grouping Summary Table in the row containing one or more elements."], 0);
                        return;
                    }
                }
                
                openGroupingSummaryWindow(groupingSummaryTableparentpanel,this,false);
            }
            
        } else if( this.value == 5 ){//Details Table
            //get Details Table container panel
            var detailsTableParentPanel = Ext.getCmp(selectedElement);
            var targetPanel = Ext.getCmp("idMainPanel");
            var containerpanel = Ext.getCmp(selectedElement); 
            //only one Details Table can be added in one section
            if ( containerpanel ) {
                var itemlength =  containerpanel.items.items.length;
                if ( itemlength > 0 ) {
                    WtfComMsgBox(["Warning", "Cannot add Details Table in the row containing one or more elements."], 0);
                    return;
                }
            }
            //set subtype details in container panel object
            detailsTableParentPanel.detailsTableSubType_id = this.detailsTableSubType_id;
            detailsTableParentPanel.detailsTableSubType_value = this.detailsTableSubType_value;
            //Open Details Table creation window
            openDetailsTableWindowAndSetConfig(this, detailsTableParentPanel.id,false,undefined,undefined,detailsTableColumnsObj,detailsTableParentPanel);
        }
    }
});

function saveTableProperty(selectedcolor, selectedmode,backgroundcolor,isRoundBorder) {
    var selectpickercolor = selectedcolor;
    var borderselectedmode = selectedmode;
    this.tablebordercolorconfig=borderselectedmode;
    Ext.borderproperties = {
        "tableproperties": {
            "bordercolor": selectpickercolor, 
            "borderstylemode": borderselectedmode,
            "backgroundcolor":backgroundcolor,
            "isRoundBorder":isRoundBorder
        }
    };
var returnConfig = Ext.borderproperties;
return returnConfig;
}

Ext.tablepanel = function (config) {
 
    var border1flag,border2flag,border3flag,border4flag,border5flag,border6flag,border7flag,border8flag,border9flag,border10flag =false ;

    if (config.tablebordermodeconfig == "borderstylemode2") {
        border2flag = true;
    } else if (config.tablebordermodeconfig == "borderstylemode3") {
        border3flag = true;
    } else if (config.tablebordermodeconfig == "borderstylemode4") {
        border4flag = true;
    }else if (config.tablebordermodeconfig == "borderstylemode5") {
        border5flag = true;
    }else if (config.tablebordermodeconfig == "borderstylemode6") {
        border6flag = true;
    } else if (config.tablebordermodeconfig == "borderstylemode7") {
        border7flag = true;
    }else if (config.tablebordermodeconfig == "borderstylemode8") {
        border8flag = true;
    }else if (config.tablebordermodeconfig == "borderstylemode9") {
        border9flag = true;
    }else if (config.tablebordermodeconfig == "borderstylemode10") {
        border10flag = true;
    }else {
        border1flag = true;
    }
    var tablebordercolor=config.tablebordercolor;
    var isRoundBorder=config.isRoundBorder;

    Ext.apply(this, {
        width: 540,
        //                autoHeight:true,
        layout: 'column',
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
        id: 'tablepanel',
        items: [{
            xtype: 'fieldset',
            height: 70,
            title: 'Border ',
            layout: 'column',
            width: 300,
            items: [
            {
                layout: 'column',
                border: false,
                width: 220,
                items:[
                this.borderColorLabel = Ext.create("Ext.form.Label", {
                    text: "Choose Color:",
                    padding: '5 0 5 0',
                    columnWidth : .5,
                    width: 105

                }),
                this.selectedBorderColorPanel = Ext.create("Ext.Panel", {
                    id: 'selectedBorderPanelid',
                    height: 20,
                    width: 30,
                    columnWidth : .12,
                    border: false,
                    margin :'2 0 0 0',
                    bodyStyle:{
                        'background-color':tablebordercolor
                    }
                }),  
                this.colorPicker = Ext.create('Ext.ux.ColorPicker', {
                    luminanceImg: '../../images/luminance.png',
                    spectrumImg: '../../images/spectrum.png',
                    value: tablebordercolor,
                    id: 'tablebordercolorpicker',
                    margin :'2 0 0 5',
                    columnWidth : .38,
                    listeners: {
                        change: function (picker, newVal) {
                            var selectednewcolor = newVal;
                            Ext.getCmp('selectedBorderPanelid').body.setStyle('background-color', selectednewcolor);
                        }

                    }
                })
//                this.newmenubtn = Ext.create('Ext.Button', {
//                    id: 'newmenubtnid',
//                    columnWidth : .15,
//                    menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//                        value: tablebordercolor,
//                        id: 'tablebordercolorpicker',
//                        handler: function (obj, rgb) {
//                            var selectednewcolor = "#" + rgb.toString();
//                            Ext.getCmp('selectedBorderPanelid').body.setStyle('background-color', selectednewcolor);
//                            }
//                    }),
//                    text: ''
//                })
                ]
            },
            {

                fieldLabel: 'Round Border',
                xtype: 'checkbox',
                name: 'applyRoundBorderCheckBox',
                id: 'applyRoundBorderCheckBox',
                disabled: false,
                checked: isRoundBorder,
                listeners: {
                    change: function (obj, newvalue, oldvalue) {
                    }
                }

            }
                    
            ]
        }, {
            xtype: 'fieldset',
            columns: 4,
            title: 'BorderStyle',
            width: 525,
            height: 310,
            bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
            items: [
            this.border1img = Ext.create('Ext.Button', {//by creating we get the object--by xtype we don't get the object'
                //                        xtype: 'button',
                enableToggle: true,
                scale: 'large',
                scope: this,
                rowspan: 3,
                height: 63,
                width: 74,
                id: 'border1Img',
                iconCls: 'border1Img ',
                cls: 'table,td',
                enableToggle: true,
                autoHeight: true,
                pressed:border1flag,
                margin: '10 20 30 50',
                toggleGroup: 'ratings',
                listeners: {
                    toggle: function (btn, pressed) {
                        var roundBorderCheckBox = Ext.getCmp('applyRoundBorderCheckBox');
                        if (roundBorderCheckBox) {
                            if (pressed) {
                                roundBorderCheckBox.enable(true);
                            } else {
                                roundBorderCheckBox.setValue(false);
                                roundBorderCheckBox.disable(true);
                            }
                        }
                    }
                }
            }),
            this.border2img = Ext.create('Ext.Button', {
                //                xtype: 'button',
                enableToggle: true,
                scale: 'large',
                scope: this,
                rowspan: 3,
                height: 63,
                width: 74,
                id: 'border2Img',
                iconCls: 'border2Img_1 ',
                enableToggle: true,
                autoHeight: true,
                pressed:border2flag,
                margin: '10 20 30 20',
                toggleGroup: 'ratings'
            }),
            this.border3img = Ext.create('Ext.Button', {
                //                xtype: 'button',
                enableToggle: true,
                scale: 'large',
                rowspan: 3,
                height: 63,
                width: 74,
                id: 'border3Img',
                iconCls: 'border3Img_1 ',
                scope: this,
                margin: '10 20 30 20',
                enableToggle: true,
                autoHeight: true,
                pressed:border3flag,
                toggleGroup: 'ratings',
                listeners : {
                    toggle: function (btn, pressed) {
                        var roundBorderCheckBox = Ext.getCmp('applyRoundBorderCheckBox');
                        if (roundBorderCheckBox) {
                            if (pressed) {
                                roundBorderCheckBox.enable(true);
                            } else {
                                roundBorderCheckBox.setValue(false);
                                roundBorderCheckBox.disable(true);
                            }
                        }
                    }
                }
            }),
            this.border4img = Ext.create('Ext.Button', {//by creating we get the object--by xtype we don't get the object'
                //                        xtype: 'button',
                enableToggle: true,
                scale: 'large',
                scope: this,
                rowspan: 3,
                height: 63,
                width: 74,
                id: 'border4Img',
                iconCls: 'border4Img ',
                cls: 'table,td',
                enableToggle: true,
                pressed:border4flag,
                autoHeight: true,
                margin: '10 20 30 20',
                toggleGroup: 'ratings'
            }),
            this.border5img = Ext.create('Ext.Button', {
                enableToggle: true,
                scale: 'large',
                scope: this,
                height: 63,
                width: 74,
                id: 'border5Img',
                iconCls: 'border5Img ',
                cls: 'table,td',
                enableToggle: true,
                pressed:border5flag,
                autoHeight: true,
                margin: '10 20 30 50',
                toggleGroup: 'ratings'
            }),
            this.border6img = Ext.create('Ext.Button', {
                enableToggle: true,
                scale: 'large',
                scope: this,
                height: 63,
                width: 74,
                id: 'border6Img',
                iconCls: 'border6Img ',
                pressed:border6flag,
                cls: 'table,td',
                enableToggle: true,
                autoHeight: true,
                margin: '10 20 30 20',
                toggleGroup: 'ratings',
                listeners: {
                    toggle: function (btn, pressed) {
                        var roundBorderCheckBox = Ext.getCmp('applyRoundBorderCheckBox');
                        if (roundBorderCheckBox) {
                            if (pressed) {
                                roundBorderCheckBox.enable(true);
                            } else {
                                roundBorderCheckBox.setValue(false);
                                roundBorderCheckBox.disable(true);
                            }
                        }
                    }
                }
            }),
            this.border7img = Ext.create('Ext.Button', {
                enableToggle: true,
                scale: 'large',
                scope: this,
                height: 63,
                width: 74,
                id: 'border7Img',
                iconCls: 'border7Img ',
                cls: 'table,td',
                enableToggle: true,
                pressed:border7flag,
                autoHeight: true,
                margin: '10 20 30 20',
                toggleGroup: 'ratings'
            }),
            this.border8img = Ext.create('Ext.Button', {
                enableToggle: true,
                scale: 'large',
                scope: this,
                height: 63,
                width: 74,
                id: 'border8Img',
                iconCls: 'border8Img ',
                pressed:border8flag,
                cls: 'table,td',
                enableToggle: true,
                autoHeight: true,
                margin: '10 20 30 20',
                toggleGroup: 'ratings'
            }),
            this.border9img = Ext.create('Ext.Button', {
                enableToggle: true,
                scale: 'large',
                scope: this,
                height: 63,
                width: 74,
                id: 'border9Img',
                pressed:border9flag,
                iconCls: 'border9Img ',
                cls: 'table,td',
                enableToggle: true,
                autoHeight: true,
                margin: '10 20 30 50',
                toggleGroup: 'ratings'
            }),
            this.border10img = Ext.create('Ext.Button', {
                enableToggle: true,
                scale: 'large',
                scope: this,
                height: 63,
                width: 74,
                id: 'border10Img',
                pressed:border10flag,
                iconCls: 'border10Img ',
                cls: 'table,td',
                enableToggle: true,
                autoHeight: true,
                margin: '10 20 30 20',
                toggleGroup: 'ratings',
                listeners : {
                    toggle: function (btn, pressed) {
                        var roundBorderCheckBox = Ext.getCmp('applyRoundBorderCheckBox');
                        if (roundBorderCheckBox) {
                            if (pressed) {
                                roundBorderCheckBox.enable(true);
                            } else {
                                roundBorderCheckBox.setValue(false);
                                roundBorderCheckBox.disable(true);
                            }
                        }
                    }
                }
            })
            ]
        }]
    }, config);
    Ext.tablepanel.superclass.constructor.call(this, config)
}

Ext.tablepanelCmp = Ext.extend(Ext.tablepanel, Ext.Panel, {
    autoHeight: true
});
this.tpanel = new Ext.tablepanel({});

function addGlobalTable(parentScope) {
    Ext.application({
        name: 'myApp',
        appFolder: 'app',
        autoCreateViewport: false,
        controllers: [
        'MyController'
        ],
        launch: function () {
            console.log("launch the application");
            Ext.create("LoginAppDemo.view.LoginForm")
        }
    });
}

Ext.define("LoginAppDemo.view.LoginForm", {
    extend: 'Ext.window.Window',
    alias: 'widget.loginform',
    requires: ['Ext.form.Panel'],
    title: 'Table Wizard',
    autoShow: true,
    width: 500,
    autoHeight: true,
    layout: 'card',
    closable: true,
    modal:true,              //ERP-19208
    listeners: {
        'close': function () {
            if ( Ext.getCmp('bordercolorproperty') ) {
                Ext.getCmp('bordercolorproperty').close();
            }
        }  
    },
    //    resizable: false,
    //    layout: 'fit',
    items: [{
        id: 'card-0',
        xtype: 'panel1'
    }, {
        id: 'card-1',
        xtype: 'panel2'
    }, {
        id: 'card-2',
        html: 'Yes - 3'
    }]
})

Ext.define('myApp.controller.MyController', {
    extend: 'Ext.app.Controller',
    init: function () {
        console.log("Controller init called");
        this.control({
            'panel1 button[id=continue]': {
                click: this.headerContinue
            },
            'panel2 button[id=previous]': {
                click: this.headerPrevious
            },
            'panel2 button[id=submit]': {
                click: this.headerSubmit
            }

        });
    },
    headerContinue: function (button) {
        var panel = button.up('loginform');
        var colcnt = panel.down('panel1 #columncnt').getValue();
        var rowcnt = panel.down('panel1 #rowcnt').getValue();
        var tableWidth = panel.down('panel1 #tablewidth').getValue();
        var widthin = panel.down('panel1 #globaltablewidthin').getValue();
        var fixedrowvalue = false;
        if (panel.down('panel1 #fixedrowvalue').getValue()) {
            fixedrowvalue = true;
        };
        //        var tablebordern= "#"+panel.down('panel1 #bordercolor').value;
        var tablebordern = Ext.globaltablecolor;
        //        var res = tablebordern.match(/undefined/g);
        if (Ext.globaltablecolor == undefined || Ext.globaltablecolor== "") {
            tablebordern = "#000000";
        }
        var bordereffect1 = panel.down('panel1 #border1').pressed;
        //        var bordereffect2=panel.down('panel1 #border2').pressed;
        var bordereffect3 = panel.down('panel1 #border3').pressed;
        var horizontalBorderEffect = panel.down('panel1 #horizontalBorder').pressed;
        var verticalBorderEffect = panel.down('panel1 #verticalBorder').pressed;
        var horizontalVerticalBorderEffect = panel.down('panel1 #horizontalVerticalBorder').pressed;
        var onlytableborderEffect = panel.down('panel1 #onlytableborder').pressed
        var widthInType = "%";
        var cellWidth = 100;
        var globalPixelStyle = "";
        var tableHeader=true;
        var border=true;
        var roundBorder = false;
        var applyHeaderCheckBox= Ext.getCmp('applyHeaderCheckBox');
        var applyBorderCheckBox= Ext.getCmp('applyBorderCheckBox');
        var roundBorderCheckBox= Ext.getCmp('applyRoundBorderCheckBox');
        if(applyHeaderCheckBox != null){ //get flag for table header is required or not
            tableHeader=applyHeaderCheckBox.getValue();
        }
        if(applyBorderCheckBox != null){
            border=applyBorderCheckBox.getValue();
        }
        if(roundBorderCheckBox != null){
            roundBorder=roundBorderCheckBox.getValue();
        }
        if(widthin=='Pixel') {
            widthInType = "px";
            cellWidth = tableWidth / colcnt;
            globalPixelStyle = "style='width:"+tableWidth+"px;'";
        } else {
            cellWidth = cellWidth / colcnt;
        //            globalPixelStyle = "style='width:"+tableWidth+widthInType+";'";
        }
        var roundBorderClass = "globaltableroundborder";
        if(tableHeader && roundBorder){
            roundBorderClass = "repeatRowGlobalTableRoundBorder";
        }
        
        if (bordereffect1 == true) {
            if (roundBorder) {
                if (!fixedrowvalue) {
                    var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltable globaltablebordereffect1 '+roundBorderClass+'" ' + globalPixelStyle + '  cellspacing="0" width="' + tableWidth + '%" >';
                } else {
                    var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltablerepeat globaltablebordereffect1 borderseparate '+roundBorderClass+'" ' + globalPixelStyle + '  cellspacing="0" width="' + tableWidth + '%" >';
                }
            } else {
                if (!fixedrowvalue) {
                    var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltable" ' + globalPixelStyle + ' cellspacing="0" width="' + tableWidth + '%" >';
                } else {
                    var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltablerepeat borderseparate" ' + globalPixelStyle + ' cellspacing="0" width="' + tableWidth + '%" >';
                }
            }
           
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                if(tableHeader && cnt == 0){ //If repeat row table with header then isheader is 'true'
                    tableHTML += '<tr isheader="true">';
                } else{
                    tableHTML += '<tr >';
                }
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if (roundBorder) {
                        if(!border){
                            tableHTML += '<td style="position:relative; border-color: inherit ;" valign="top" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" > R' + cnt + 'C' + cnt1 + ' </td>';
                        } else{
                            tableHTML += '<td style="position:relative; border-color: inherit ;" valign="top" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                        }
                    } else {
                        if(!border){
                            tableHTML += '<td style=" border-color: inherit ;" valign="top" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" > R' + cnt + 'C' + cnt1 + ' </td>';
                        } else{
                            tableHTML += '<td style=" border: 1px solid ; border-color: inherit ;" valign="top" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                        }
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        }
        //        else if(bordereffect2==true){
        //            var tableHTML = '<table align="center" class="globaltable tablefirst tablelast" border="1" bordercolor="'+tablebordern+'";" cellspacing="0" ;style="border-collapse:collapse;">';
        //            for(var cnt=0;cnt<rowcnt;cnt++) {
        //                tableHTML += '<tr>';
        //                for(var cnt1=0;cnt1<colcnt;cnt1++) {
        //                    tableHTML += '<td style="width:'+cellWidth+'px;border-left: medium none; border-right: medium none;" class=""onclick="getval(this)"> R'+cnt+'C'+cnt1+' </td>';
        //                }
        //                tableHTML += '</tr>';
        //            }
        //            tableHTML += '</table>';
        //        }
        else if (bordereffect3 == true) {
            if (!fixedrowvalue) {  
                var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltable"' + globalPixelStyle + ' cellspacing="0" width="' + tableWidth +'%" >';
            }else{
                var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltablerepeat borderseparate"' + globalPixelStyle + '  cellspacing="0" width="' + tableWidth +'%" >';
            }
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                if(tableHeader && cnt == 0){ //If repeat row table with header then isheader is 'true'
                    tableHTML += '<tr isheader="true">';
                } else{
                    tableHTML += '<tr >';
                }
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(!border){
                        tableHTML += '<td style="position:relative;  border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                    } else{
                        if(cnt==0){
                            tableHTML += '<td style=" border: 1px solid;border-left: none ;border-right: none;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                        } else if(cnt==(rowcnt-1)){
                            tableHTML += '<td style=" border: 1px solid ;border-left: none ;border-right: none ;border-top: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                        } else{
                            tableHTML += '<td style="  border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                        }
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        } else if (horizontalBorderEffect == true) {
            if (!fixedrowvalue) {  
                var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltable"' + globalPixelStyle + '  cellspacing="0" width="' + tableWidth +'%" >';
            }else
            {
                var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltablerepeat borderseparate"' + globalPixelStyle + '  cellspacing="0" width="' + tableWidth +'%" >';             
            }
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                if(tableHeader && cnt == 0){ //If repeat row table with header then isheader is 'true'
                    tableHTML += '<tr isheader="true">';
                } else{
                    tableHTML += '<tr >';
                }
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(!border){
                        tableHTML += '<td style=" border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                    } else{
                        tableHTML += '<td style=" border: 1px solid ;border-left: none ;border-right: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        } else if (verticalBorderEffect == true) {
            if (!fixedrowvalue) {  
                var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltable" ' + globalPixelStyle + ' cellspacing="0" width="' + tableWidth +'%" >';
            }else{
                var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltablerepeat borderseparate" ' + globalPixelStyle + '  cellspacing="0" width="' + tableWidth +'%" >';
            }
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                if(tableHeader && cnt == 0){ //If repeat row table with header then isheader is 'true'
                    tableHTML += '<tr isheader="true">';
                } else{
                    tableHTML += '<tr >';
                }
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(!border){
                        tableHTML += '<td style=" border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                    } else{
                        tableHTML += '<td onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" style=" border: 1px solid ;border-bottom: none ;border-top: none ;border-color: inherit ;" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        } else if (horizontalVerticalBorderEffect == true) {
            if (!fixedrowvalue) { 
                var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltable"' + globalPixelStyle + '  cellspacing="0" width="' + tableWidth +'%" >';
            }else{
                var tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltablerepeat borderseparate"' + globalPixelStyle + '  cellspacing="0" width="' + tableWidth +'%" >';
            }
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                if(tableHeader && cnt == 0){ //If repeat row table with header then isheader is 'true'
                    tableHTML += '<tr isheader="true">';
                } else{
                    tableHTML += '<tr >';
                }
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(!border){
                        tableHTML += '<td style="position:relative; border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                    } else{
                        if(cnt==0){
                            if(cnt1==0){
                                tableHTML += '<td style=" border: 1px solid ;border-top: none ;border-left: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else if(cnt1==(colcnt-1)){
                                tableHTML += '<td style=" border: 1px solid ;border-top: none ;border-right: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else{
                                tableHTML += '<td style=" border: 1px solid ;border-top: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                            }
                        } else if(cnt==(rowcnt-1)){
                            if(cnt1==0){
                                tableHTML += '<td style=" border: 1px solid ;border-bottom: none ;border-left: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else if(cnt1==(colcnt-1)){
                                tableHTML += '<td style=" border: 1px solid ;border-bottom: none ;border-right: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else{
                                tableHTML += '<td style=" border: 1px solid ;border-bottom: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                            }
                        } else{
                            if(cnt1==0){
                                tableHTML += '<td style=" border: 1px solid ;border-left: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else if(cnt1==(colcnt-1)){
                                tableHTML += '<td style=" border: 1px solid ;border-right: none ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else{
                                tableHTML += '<td style=" border: 1px solid ;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                            }
                        }
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        } else if (onlytableborderEffect == true) {
            var tableHTML = "";
            tableHTML = '<table align="center" style="border-collapse:separate; border: 1px solid ' + tablebordern + '"class="globaltable separated" border="1" bordercolor="' + tablebordern + '" cellspacing="0"  frame="box" rules="all" width="' + tableWidth +'%" >';
            if(!border){
                tableHTML = '<table align="center" class="globaltable separated" border="0" bordercolor="' + tablebordern + '" cellspacing="0"  rules="all">';
            }
            if(fixedrowvalue){ // ERP-25374
                if (!roundBorder) { //ERP-25374
                    tableHTML = '<table align="center" style="border-collapse:separate; border: 1px solid ' + tablebordern + '"class="globaltablerepeat separated" border="1" bordercolor="' + tablebordern + '" cellspacing="0"  frame="box" rules="all" width="' + tableWidth +'%" >';
                    if(!border){
                        tableHTML = '<table align="center" style ="border-color:'+tablebordern+';" class="globaltablerepeat separated" border="0" bordercolor="' + tablebordern + '" cellspacing="0"  rules="all">';
                    }
                } 
                else{
                    tableHTML = '<table align="center" style="border-collapse:separate;border-color:'+tablebordern+';" class="globaltablerepeat separated globaltableonlytableborderEffect '+roundBorderClass+'" border="0" bordercolor="' + tablebordern + '" cellspacing="0"  frame="box" rules="all" width="' + tableWidth +'%" >';
                }
            }
            
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                if(tableHeader && cnt == 0){ //If repeat row table with header then isheader is 'true'
                    tableHTML += '<tr isheader="true">';
                } else{
                    tableHTML += '<tr >';
                }
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(roundBorder){
                        tableHTML += '<td style="width:' + cellWidth + widthInType +';border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)"  oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                    }else{
                        tableHTML += '<td style="  width:' + cellWidth + widthInType +';border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" class="removeallborder" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                    }
                 }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        } else {
            var tableHTML = "";
            if (!fixedrowvalue) {
                tableHTML = '<table align="center" class="globaltable" border="1" bordercolor="' + tablebordern + '"  cellspacing="0" width="' + tableWidth +'%" style="border:1px solid black; cursor:pointer; border-color:' + tablebordern + ';">';
                if(!border){
                    tableHTML = '<table align="center" class="globaltable" border="0" bordercolor="' + tablebordern + '"  cellspacing="0" width="' + tableWidth +'%" style="border:1px solid black; cursor:pointer; border-color:' + tablebordern + ';">';
                }
            } else {
                tableHTML = '<table align="center" class="globaltablerepeat borderseparate" border="1" bordercolor="' + tablebordern + '"  cellspacing="0" width="' + tableWidth +'%" style="border:1px solid black; cursor:pointer; border-color:' + tablebordern + ';">';
                if(!border){
                    tableHTML = '<table align="center" class="globaltablerepeat borderseparate" border="0" bordercolor="' + tablebordern + '"  cellspacing="0" width="' + tableWidth +'%" style="border:1px solid black; cursor:pointer; border-color:' + tablebordern + ';">';
                }
                }
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                if(tableHeader && cnt == 0){ //If repeat row table with header then isheader is 'true'
                    tableHTML += '<tr isheader="true">';
                } else{
                    tableHTML += '<tr >';
                }
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    tableHTML += '<td style=" width:' + cellWidth + 'px;border-color: inherit ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')" onmouseleave="destroydiv(this)" oncontextmenu="getContextMenu(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+')"> R' + cnt + 'C' + cnt1 + ' </td>';
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        }
        panel.down('panel2 #tablehtml').setValue(tableHTML);
        panel.getLayout().setActiveItem(1);
        panel.doLayout();
    // <br />
    },
    headerPrevious: function (button) {
        var panel = button.up('loginform');
        panel.getLayout().setActiveItem(0);
    },
    headerSubmit: function (button) {
        if(Ext.getCmp(selectedElement).items.items.length == 0){
            Ext.getCmp(selectedElement).update("");
        }
        var panel = button.up('loginform');
        var designerPanel = Ext.getCmp(selectedElement);
        var tableHeader = true;
        var applyHeaderCheckBox= Ext.getCmp('applyHeaderCheckBox');
        if(applyHeaderCheckBox != null){ //get flag for table header is present or not
            tableHeader=applyHeaderCheckBox.getValue();
        }
        var PosX = designerPanel.x;
        var PosY = designerPanel.y;
        Ext.fixedrowvalue = panel.down('panel1 #fixedrowvalue').getValue();
        
        var widthIn = panel.down('panel1 #globaltablewidthin').getValue();
        var globalTableWidth = '';
        if(widthIn == "Pixel") {
            globalTableWidth = Ext.get('paneltable').dom.children[0].children[0].offsetWidth;//Ext.getCmp('paneltable').getBox().width
        }
        Ext.getCmp('paneltable').borderColor = Ext.getCmp('paneltable').getEl().dom.children[0].children[0].style.borderColor
        var field = createGlobalTable(PosX, PosY, Ext.getCmp('paneltable').body.dom.innerHTML, designerPanel, globalTableWidth, Ext.getCmp('paneltable').getBox().height, Ext.fixedrowvalue, Ext.fieldID.insertGlobalTable,"1","5","5","1",Ext.getCmp('paneltable'),tableHeader);
        designerPanel.add(field);
        initTreePanel();//Refreshing the tree panel after adding global table
        Ext.globaltablecolor = "";
        designerPanel.doLayout();
        panel.close();
        if ( Ext.getCmp('bordercolorproperty') ) {
            Ext.getCmp('bordercolorproperty').close();
        }
    }

});

Ext.define('myApp.view.Panel1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.panel1',
    title: 'Step 1: Configure Table',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    bodyStyle: 'background-color:#ffffff;padding:15px 5px 5px 20px;',
    bbar: [{
        xtype: 'button',
        id: 'continue',
        text: 'Next',
        iconCls: 'icon-go'
    }
    ],
    items: [{
        xtype: 'label',
        text: 'Create an empty table with',
        height : 25,
        style : {
            color : 'black',
            textAlign: 'center',
            'font-weight':'bold'
        }
    }, {
        xtype: 'numberfield',
        anchor: '100%',
        name: 'columnscnt',
        itemId: 'columncnt',
        fieldLabel: 'Columns',
        value: 1,
        maxValue: 10,
        minValue: 1
    }, {
        xtype: 'numberfield',
        anchor: '100%',
        name: 'rowscnt',
        itemId: 'rowcnt',
        fieldLabel: 'Rows',
        value: 1,
        maxValue: 10,
        minValue: 1
    }, {
        xtype:'combo',
        fieldLabel:'Width In',
        name:'widthin',
        queryMode:'local',
        store:['Percentage','Pixel'],
        displayField:'widthin',
        value:'Percentage',
        autoSelect:true,
        itemId: 'globaltablewidthin',
        forceSelection:true,
        listeners :{
            'select': function(combo, selection) { // ERP-19450 : Global table width can not be greater than 100% at the time of creating
                var tablewidth = combo.up('loginform').down('panel1 #tablewidth').getValue();
                if(combo.getValue() == 'Percentage'){
                    if(tablewidth > 100){
                        combo.up('loginform').down('panel1 #tablewidth').setValue(100);
                    }
                }
            }
        }
    }, {
        xtype: 'numberfield',
        anchor: '100%',
        name: 'tablewidth',
        itemId: 'tablewidth',
        fieldLabel: 'Table Width',
        value: 100,
        maxValue: 700,
        minValue: 1,
        listeners :{
            change: function (obj, newvalue, oldvalue) { // ERP-19450 : Global table width can not be greater than 100% at the time of creating
                var widthIn = obj.up('loginform').down('panel1 #globaltablewidthin').getValue();
                if(widthIn == 'Percentage'){
                    if(newvalue > 100){
                        obj.setValue(100);
                    }
                }
            }
        }
    }, {
        xtype: 'tbspacer',
        height: 20
    }, {
        fieldLabel: 'Repeat Row index(Starting from 1) ',
        xtype: 'textfield',
        width: 255,
        labelWidth:210,
        name: 'myTextField',
        id: 'fixedrow',
        itemId: 'fixedrowvalue',
        disabled: false,
        listeners:{
            change: function (obj, newvalue, oldvalue) {

                if(newvalue == ""){ //If repeat row index not given then hide 'Apply Header' component
                    getEXTComponent("applyHeaderCheckBox").setVisible(false);
                    getEXTComponent("applyHeaderCheckBox").setValue(false);
                } else{ //If repeat row index given then show 'Apply Header' component
                    getEXTComponent("applyHeaderCheckBox").setVisible(true);
                    getEXTComponent("applyHeaderCheckBox").setValue(true);
                }
            }
        }
    },
    {
        xtype: 'panel',
        border: false,
        anchor: '100%',
        layout:'column',
        items:[{
            xtype: 'panel',
            itemId: 'bordercolor',
            dataIndex: 'newheaderpropertyaaa',
            height: 40,
            border: false,
            text: 'Choose Color',
            titleAlign: "Center",
            titleCollapse: true,
            html: "<span>Table border color</span>",
            scope: this
        },{
            xtype: 'panel',
            height: 19,
            id:'idSelectedTableBorder',
            border: false,
            width:40,
            bodyStyle:'margin-left:10px;margin-top:2px;background-color:#000000;'
        },{
//            xtype:'button',
//            id: 'idtablebordercolor',
//            iconCls: 'edit-table-property-icon',
//            width:26,
//            height:19,
//            handler:function(){
//                colorpanel();
//            },
//            text: ''
//        }],
//        scope: this
           xtype:'AdvanceColorPicker',
            id: 'idtablebordercolor',
            iconCls: 'edit-table-property-icon',
            width:80,
            height:19,
            luminanceImg: '../../images/luminance.png',
            spectrumImg: '../../images/spectrum.png',
            value: (Ext.globaltablecolor == undefined || Ext.globaltablecolor == "") ? '#000000' : Ext.globaltablecolor,
           // itemId: 'bordercolor',
            isBackgroundColor: true,
            listeners: {
                scope:this,
                change: function (obj, newVal) {
                    Ext.globaltablecolor = newVal;
                    Ext.getCmp('idSelectedTableBorder').body.setStyle('background-color', newVal);                            
                }
            }
        }],
        scope: this
    },
    {
        fieldLabel: 'Table Header', //CheckBox for table row is required or not when inserting repeat row table
        xtype: 'checkbox',
        name: 'applyHeaderCheckBox',
        id: 'applyHeaderCheckBox',
        disabled: false,
        checked : false,
        hidden:true
    },
    {
        fieldLabel: 'Apply Border',
        xtype: 'checkbox',
        name: 'applyBorderCheckBox',
        id: 'applyBorderCheckBox',
        disabled: false,
        checked : true,
        listeners :{
            change: function (obj, newvalue, oldvalue) {
                var borderStyleComponent = Ext.getCmp('borderStyleFieldset');
                var roundBorderCheckBox = Ext.getCmp('applyRoundBorderCheckBox');
                if(borderStyleComponent != null){
                    if(Ext.getCmp('applyBorderCheckBox').checked){
                        borderStyleComponent.enable(true);
                    } else {
                        borderStyleComponent.disable(true);
                    }
                }
                if(roundBorderCheckBox != null){
                    if(Ext.getCmp('applyBorderCheckBox').checked){
                        roundBorderCheckBox.enable(true);
                    } else {
                        roundBorderCheckBox.disable(true);
                    }
                }
            }
        }
    },
    {
        fieldLabel: 'Round Border',
        xtype: 'checkbox',
        name: 'applyRoundBorderCheckBox',
        id: 'applyRoundBorderCheckBox',
        disabled: false,
        checked : false,
        listeners :{
            change: function (obj, newvalue, oldvalue) {
            }
        }
    },
    {
        xtype: 'fieldset',
        columns: 3,
        title: 'BorderStyle',
        anchor: '100%',
        height: 210,
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
            pressed: true,
            listeners : {
                toggle: function (btn,pressed) {
                    var roundBorderCheckBox = Ext.getCmp('applyRoundBorderCheckBox');
                    if ( roundBorderCheckBox ) {
                        if ( pressed ) {
                            roundBorderCheckBox.enable(true);
                        } else {
                            roundBorderCheckBox.setValue(false);
                            roundBorderCheckBox.disable(true);
                        }
                    }
                }
            }
        },
        //                {
        //                    xtype: 'button',
        //                    enableToggle: true,
        //                    scale : 'large',
        //                    scope : this,
        //                    rowspan: 3,
        //                    height: 63,
        //                    width: 74,
        ////                    id:'border2Img',
        //                    itemId: 'border2',
        //                    iconCls : 'border2Img ',
        //                    enableToggle: true,
        //                    autoHeight : true,
        //
        //                    margin:'10 20 30 20',
        //                    toggleGroup: 'ratings'
        //                },
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
        },
        {
            xtype: 'button',
            enableToggle: true,
            scale: 'large',
            scope: this,
            rowspan: 3,
            height: 63,
            width: 74,
            //                    id:'border1Img',
            itemId: 'horizontalBorder',
            iconCls: 'horizontalborderImg',
            cls: 'table,td',
            enableToggle: true,
            autoHeight: true,
            margin: '10 20 30 10',
            toggleGroup: 'ratings'
        },
        {
            xtype: 'button',
            enableToggle: true,
            scale: 'large',
            rowspan: 3,
            height: 63,
            width: 74,
            itemId: 'verticalBorder',
            //                    id:'border3Img',
            iconCls: 'verticalborderImg',
            scope: this,
            margin: '10 20 30 10',
            enableToggle: true,
            autoHeight: true,
            toggleGroup: 'ratings'
        },
        {
            xtype: 'button',
            enableToggle: true,
            scale: 'large',
            rowspan: 4,
            height: 63,
            width: 74,
            itemId: 'horizontalVerticalBorder',
            //                    id:'border3Img',
            iconCls: 'horizontalverticalborderImg',
            scope: this,
            margin: '10 20 30 10',
            enableToggle: true,
            autoHeight: true,
            toggleGroup: 'ratings'
        },
        {
            xtype: 'button',
            enableToggle: true,
            scale: 'large',
            rowspan: 4,
            height: 63,
            width: 74,
            itemId: 'onlytableborder',
            //                    id:'border3Img',
            iconCls: 'onlytableborderImg',
            scope: this,
            margin: '10 20 30 10',
            enableToggle: true,
            autoHeight: true,
            toggleGroup: 'ratings',
            listeners : {
                toggle: function (btn,pressed) {
                    var roundBorderCheckBox = Ext.getCmp('applyRoundBorderCheckBox');
                    if ( roundBorderCheckBox ) {
                        if ( pressed ) {
                            roundBorderCheckBox.enable(true);
                        } else {
                            roundBorderCheckBox.setValue(false);
                            roundBorderCheckBox.disable(true);
                        }
                    }
                }
            }
        }]

    }
    ],
    layout: 'auto'
});


Ext.define('myApp.view.Panel2', {
    extend: 'Ext.Panel',
    alias: 'widget.panel2',
    bodyStyle: 'background-color:#ffffff;padding:15px 5px 5px 20px;',
    layout: {
        type: 'vbox',
        flex: 1,
        align: 'stretch'
    },
    bbar: [{
        xtype: 'button',
        id: 'previous',
        text: 'Previous',
        iconCls: 'icon-add'
    },
    {
        xtype: 'button',
        id: 'submit',
        text: 'Create',
        iconCls: 'icon-go'
    }],
    items: [{
        xtype: 'hiddenfield',
        itemId: 'tablehtml',
        name: 'hidden_field_1',
        text: 'value from hidden field'
    }, {
        xtype: 'panel',
        autoWidth: true,
        layout: {
            type: 'vbox',
            flex: 1,
            align: 'stretch'
        },
        height: 400,
        border: false,
        id: 'paneltable'
    }],
    listeners: {
        activate: {
            fn: function (e) {
                Ext.getCmp('paneltable').body.dom.innerHTML = this.items.items[0].rawValue;
            //                var tbl = document.getElementById("tblMain");
            //                if (tbl != null) {
            //                    for (var i = 0; i < tbl.rows.length; i++) {
            //                        for (var j = 0; j < tbl.rows[i].cells.length; j++)
            //                            tbl.rows[i].cells[j].onclick = function () { getval(this); };
            //                    }
            //                }
            //                function getval(cel) {
            //                    new Ext.GlobalCellPropertyNew({
            //                        cellVal : cel
            //                    }).show();
            //                }
            }
        }
    }
});

function getpropertypanelforglobaltable(newselectedElement) {
    if (selectedElement != null || selectedElement != undefined)
    {
        Ext.get(selectedElement).removeCls("selected");
    }
    selectedElement = newselectedElement.parentNode.id;
    Ext.getCmp(selectedElement).addClass('selected');
    //event.stopPropagation();
    createPropertyPanel(selectedElement);
    setProperty(selectedElement);
    showElements(selectedElement);
};
function destroydiv(cel) {
    var div = document.getElementById("editdiv121");
    if ( div ) {
        remove(div);
    }
};
function getval1(event, cel,row,column,rowCount,columnCount,isSummaryTable) {
    var div = document.getElementById("editdiv121");
    if ( !div ) {
        var editdiv = document.createElement('span');
        editdiv.setAttribute("onclick", "getval(event, this.parentNode,"+row+","+column+","+rowCount+","+columnCount+","+isSummaryTable+")");
        editdiv.setAttribute("id", "editdiv121");
        editdiv.setAttribute("class", "editdiv");
    
        editdiv.innerHTML="<img style=\"float:right\" src=\"../../images/designer/editicon1.png\"/>"
        var left = event.clientX;
        var top = event.clientY;
        cel.appendChild(editdiv);
    }
//    editdiv.showAt(.getXY())''
};

function getval(event,cel,row,column,rowCount,columnCount, isSummaryTable) {
    var htmleditlabel = cel.innerHTML;
    var celltextnode = cel.textContent;
    var valtype = cel.valtype? cel.valtype : '2';
    var cellbgcolor = cel.style.backgroundColor;
    var celltextalign = cel.style.textAlign;
    var colontype = cel.colontype?cel.colontype:-1;
    
    var cellpropertypanel = new Ext.GlobalCellPropertyNew({
        cellVal: cel,
        htmleditlabel: htmleditlabel,
        cellbgcolor: cellbgcolor,
        celltextalign: celltextalign,
        valtype: valtype,
        celltextnode: celltextnode,
        colontype:colontype,
        row:row,
        column:column,
        rowCount:rowCount,
        columnCount:columnCount,
        isSummaryTable:isSummaryTable
    });
    if ( event ) {
        event.stopPropagation();
    }
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
 
    if (propertyPanel){
        propertyPanelRoot.remove(propertyPanel.id);
    }
    propertyPanel = cellpropertypanel;
    propertyPanelRoot.add(propertyPanel);
    if (cel.nodeName =="TH"){
        Ext.getCmp("idEastPanel").setTitle("Header Property Panel");
    } else{
        Ext.getCmp("idEastPanel").setTitle("Cell Property Panel");
    }
    Ext.getCmp("idEastPanel").add(propertyPanelRoot);
    Ext.getCmp("idEastPanel").doLayout();
        
    
};

Ext.GlobalCellPropertyNew = function (config) {
    var colonStore;
    colonStore=new Ext.data.Store({
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
    var lineStore;
    lineStore=new Ext.data.Store({
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
            id: 'customdesignerdataselectcombo',
            displayField: 'label',
            valueField: 'id',
            queryMode: 'local',
            width: 250,
            emptyText: 'Select a field',
            triggerAction: 'all',
            listeners: {
                scope: this,
                'select': function(combo, selection) {
                    if(selection[0].data.xtype!="NAN"){
                        Ext.getCmp('idCreateBtn').setDisabled(false);
                        var htmllabel = selection[0].data.label
                        this.label = htmllabel;
                        this.xType = selection[0].data.xtype;
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
                                    
                if(this.cellVal.childNodes){
                    if(this.cellVal.childNodes[0]){
                        if ( this.cellVal.childNodes[0].nodeName.toLowerCase() == "label" ) {
                            //                          this.cellVal.childNodes[0].remove();
                            remove(this.cellVal.childNodes[0]);           //Done for IE.
                        }
                    }
                }
                 
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
                if ( label === Ext.pagenumberlabel ) {
                    span2.setAttribute("id", "pagenumberspan");
                }
                if(allowlabel){
                    span2.setAttribute("style", "width:60%;float:left;");
                }else{
                    span2.setAttribute("style", "width:100%;float:left;");
                }
                span2.setAttribute("attribute","{PLACEHOLDER:"+ Ext.getCmp('customdesignerdataselectcombo').getValue() +"}");
                                    
                                    
                var finalElement = document.createElement("div");
                finalElement.setAttribute("style", "overflow: hidden;display:inline-block;width:100%;");
                finalElement.setAttribute("class", "fieldItemDivBorder");
                finalElement.setAttribute("type", "dataElement");
                finalElement.setAttribute("onclick", "getDataElementPropertyPanel(event,this,true,false,true)");
                finalElement.setAttribute("allowlabel", allowlabel);
                
                
                if(allowlabel){
                    finalElement.innerHTML += span1.outerHTML;
                }
                finalElement.innerHTML += span2.outerHTML;
                           
                this.cellVal.appendChild(finalElement);
                this.valtype= 3;
                this.cellVal.valtype = 3;
                Ext.getCmp('idInsertDataElementWindow').close();
                win.destroy();
                
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
                    
    var isFormulaFunctionHidden = false;
    if((_CustomDesign_moduleId == Ext.moduleID.Acc_Sales_Order_ModuleId || _CustomDesign_moduleId == Ext.moduleID.Acc_Invoice_ModuleId) && (_CustomDesign_templateSubtype == Ext.Subtype.JobOrder || _CustomDesign_templateSubtype == Ext.Subtype.JobOrderLabel)){
        isFormulaFunctionHidden = true;
    }
    Ext.apply(this, {
        region: 'center',
        scope: this,
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
        autoHeight: true,
        border: false,
        items: [
        {
            xtype: 'fieldset',
            flex: 1,
            title: 'Value Settings',
            layout: 'anchor',
            width:260,
            hidden:(config.cellVal.nodeName == "TH" || config.cellVal.offsetParent.getAttribute("isbalanceoutstanding") == "true")?true:false,  
            defaults: {
                anchor: '100%',
                hideEmptyLabel: false
            },
            defaultType: 'textfield',
            items: [
            this.radiocell = new Ext.form.RadioGroup({
                fieldLabel: 'Place holder',
                id:"idradiocell",
                layout: {
                    align: 'stretch',
                    type: 'vbox'
                },
                width: 250,
                border: false,
                items: [
                {
                    boxLabel: 'Select Field',
                    name: 'type',
                    inputValue: '1',
                    checked: config.valtype=='1',
                    flex: 1
                }, {
                    boxLabel: 'Insert Text',
                    name: 'type',
                    inputValue: '2',
                    checked: config.valtype=='2',
                    flex: 1
                },{
                    boxLabel: 'Data Element',
                    name: 'type',
                    inputValue: '3',
                    checked: config.valtype=='3',
                    flex: 1
                },{
                    boxLabel: 'Create Formula', 
                    name: 'type', 
                    inputValue:'4',
                    hidden: isFormulaFunctionHidden,
                    checked: config.valtype=='4',
                    flex: 1
                }],
                listeners: {
                    scope: this,
                    change: function (obj, value) {
                        if(value.type == 3){
                            this.valtype = 3;
                            Ext.getCmp('colonPositionCombo').hide();
                            getEXTComponent('customdesignerfieldselectcombo').hide();
                            var dataCellCombo=getEXTComponent('customdesignerdataselectcombo');
                            dataCellCombo.setValue("");                    //set value to data element combo
                            dataCellCombo.show();
                            getEXTComponent('idcreatelabel').show();
                            getEXTComponent('idallowlabel').show();
                            Ext.getCmp('customdesignerfieldlabel').hide();
                            Ext.getCmp('inserttextfieldlabel').hide();
                            Ext.getCmp('idApplyBullets').setDisabled(true);
                            Ext.getCmp('idbulletType').setDisabled(true);
                            Ext.getCmp('selectfieldlabel').show();
                            //                            var comboalign = Ext.getCmp('alignselectcombo');
                            //                            comboalign.setValue("");
                            //                            var setdefaulthtml = Ext.getCmp('myDesignFieldEditor2');
                            //                            setdefaulthtml.setValue("");
//                            win.show();

                        } else if (value.type == 2) {
                            this.valtype = 2;
                            var celltextlabel = Ext.getCmp('customdesignerfieldlabel');
                            celltextlabel.setValue("");
                            //                            var comboalign = Ext.getCmp('alignselectcombo');
                            //                            comboalign.setValue("");
                            Ext.getCmp('inserttextfieldlabel').show();
                            celltextlabel.show();
                            Ext.getCmp('colonPositionCombo').show();
                            Ext.getCmp('customdesignerfieldselectcombo').hide();
                            Ext.getCmp('idApplyBullets').setDisabled(false);
                            Ext.getCmp('idbulletType').setDisabled(false);
                            getEXTComponent('customdesignerdataselectcombo').hide();         // hide data element components.
                            getEXTComponent('idcreatelabel').hide();
                            getEXTComponent('idallowlabel').hide();
                            Ext.getCmp('selectfieldlabel').hide();
                        //                            this.cellVal.innerHTML = "";
                        //                            Ext.getCmp('myDesignFieldEditor2').hide();
                        } else if (value.type == 4) {
                            this.valtype = 4;
                            Ext.getCmp('colonPositionCombo').hide();
                            var cellcombo = Ext.getCmp('customdesignerfieldselectcombo');
                            cellcombo.setValue("");
                            Ext.getCmp('customdesignerfieldlabel').hide();
                            Ext.getCmp('inserttextfieldlabel').hide();
                            Ext.getCmp('idApplyBullets').setDisabled(true);
                            Ext.getCmp('idbulletType').setDisabled(true);
                            getEXTComponent('customdesignerdataselectcombo').hide();         // hide data element components.
                            getEXTComponent('idcreatelabel').hide();
                            getEXTComponent('idallowlabel').hide();
                            Ext.getCmp('selectfieldlabel').hide();
                            cellcombo.hide();
                            
                            var cellVal = this.cellVal;
                            this.createFormulaWindow = openFormulaBuilderWindow(Ext.fieldID.insertField, false, true, false);
                            this.createFormulaWindow.cellVal = cellVal;
                            this.createFormulaWindow.saveFormula = function(){
                                var saveflag = this.validatebeforesave();
                                if(saveflag){
                                    if(this.cellVal.childNodes){
                                        if(this.cellVal.childNodes[0]){
                                            if ( this.cellVal.childNodes[0].nodeName.toLowerCase() == "label" ) {
                                                remove(this.cellVal.childNodes[0]);           //Done for IE.
                                            }
                                        }
                                    }
                                    var label = this.measureName.getValue();
                                    var checkLabel = this.formulaText.getValue();
                                    var div = document.createElement('div');
                                    div.innerHTML = "#" + label + "#";
                                    div.setAttribute("onclick", "getPropertyPanelForLineItemFields(event,this,true)");
                                    div.setAttribute("class", "fieldItemDivBorder");
                                    div.setAttribute("type", "formula");
                                    div.setAttribute("style", "width:100%; display:inline-block;");
                                    div.setAttribute("attribute","{PLACEHOLDER:"+ checkLabel+"}");
                                    div.setAttribute("columnname", label);
                                    div.setAttribute("isformula", 'true');
                                    div.setAttribute("defaultValue", "0");
                                    div.setAttribute("colno", config.column);
                                    div.setAttribute("valueSeparator", '');
                                    div.setAttribute("xtype", "2");
                                    this.cellVal.appendChild(div);
                                    this.valtype= 4;
                                    this.cellVal.valtype = 4;
                                    this.close();
                                }
                            };
                            this.createFormulaWindow.show();
                            
                        } else {
                            this.valtype = 1;
                            //                            Ext.getCmp('customdesignerfieldlabel').hide();
                            Ext.getCmp('colonPositionCombo').hide();
                            var cellcombo = Ext.getCmp('customdesignerfieldselectcombo');
                            cellcombo.setValue("");
                            Ext.getCmp('customdesignerfieldlabel').hide();
                            Ext.getCmp('inserttextfieldlabel').hide();
                            Ext.getCmp('idApplyBullets').setDisabled(true);
                            Ext.getCmp('idbulletType').setDisabled(true);
                            getEXTComponent('customdesignerdataselectcombo').hide();         // hide data element components.
                            getEXTComponent('idcreatelabel').hide();
                            getEXTComponent('idallowlabel').hide();
                            Ext.getCmp('selectfieldlabel').show();
                            //                            var comboalign = Ext.getCmp('alignselectcombo');
                            //                            comboalign.setValue("");
                            //                            var setdefaulthtml = Ext.getCmp('myDesignFieldEditor2');
                            //                            setdefaulthtml.setValue("");
                            cellcombo.show();
                        //                            this.cellVal.innerHTML = "";
                        //                            setdefaulthtml.show();
                        }
                    }
                }
            }),
            this.fieldinserttextlabel = new Ext.form.Label({
                id: 'inserttextfieldlabel',
                //                xtype: 'label',
                fieldLabel: 'Insert Text',
                text: 'Insert Text:'
            }),
            this.fieldinserttext = new Ext.form.field.TextArea({
                //                fieldLabel: 'Insert Text',
                xtype: 'textarea',
                id: 'customdesignerfieldlabel',
                name: 'fieldlabel',
                width: 250,
                hidden: true,
                scope:this,
                allowBlank: false,
                //                value:config.cellVal.valType==2?config.cellVal.innerHTML:"",
                value:config.cellVal.valType==2?config.cellVal.innerHTML.replace(/&nbsp;/g,' '):"",
                listeners:{
                    scope:this,
                    blur: function () {
                        updatePropertyofCell(this);
                    }
                }
                        
            }),
            this.selectfieldlabel = new Ext.form.Label({//For Select field Lable
                id: 'selectfieldlabel',
                fieldLabel: 'Select Field',
                text: 'Select Field:'
            }),
            this.cellcombo = new Ext.form.field.ComboBox({
                hideLabel:true,
                store: defaultFieldGlobalStore,
                id: 'customdesignerfieldselectcombo',
                displayField: 'label',
                valueField: 'id',
                queryMode: 'local',
                width: 250,
                emptyText: 'Select a field',
                triggerAction: 'all',
                multiSelect:true,
                listeners: {
                    scope: this,
                    'beforeselect': function (combo, selectedRec) {
                        if(selectedRec.data.xtype != "NAN"){
                            if(this.cellVal.childNodes){
                                if(this.cellVal.childNodes[0]){
                                    if ( this.cellVal.childNodes[0].nodeName.toLowerCase() == "label" ) {
                                        //                                        this.cellVal.childNodes[0].remove();
                                        remove(this.cellVal.childNodes[0]);           //Done for IE.
                                    }
                                }
                            }
                            
                            var div = document.createElement('div');
                            div.innerHTML = "#" + selectedRec.data.label + "#";
                            div.setAttribute("onclick", "getPropertyPanelForLineItemFields(event,this,true)");
                            div.setAttribute("class", "fieldItemDivBorder");
                            div.setAttribute("style", "width:100%; display:inline-block;");
                            if ( selectedRec.data.label === Ext.pagenumberlabel ) {
                                div.setAttribute("id", "pagenumberspan");
                            }
                            div.setAttribute("attribute","{PLACEHOLDER:"+ selectedRec.data.id+"}");
                            div.setAttribute("columnname", selectedRec.data.label);
                            div.setAttribute("defaultValue", "");
                            div.setAttribute("colno", config.column);
                            div.setAttribute("valueSeparator", 'comma');
                            div.setAttribute("xtype", selectedRec.data.xtype);
                            this.cellVal.appendChild(div);
                            this.valtype= 1;
                            this.cellVal.valtype = 1;
                            if( config.isSummaryTable ){
                                if( summaryTableJson!=null && summaryTableJson!="" ){
                                    summaryTableJson.html=config.cellVal.parentNode.parentNode.parentNode.outerHTML;
                                    var panelId = (Ext.get('itemlistconfigsectionPanelGrid').dom.attributes["panelid"]!=null)?Ext.get('itemlistconfigsectionPanelGrid').dom.attributes["panelid"]:"";
                                    if(panelId !=""){
                                        var lineitemparentpanel = Ext.getCmp(panelId.value).ownerCt;
                                        if(lineitemparentpanel!=null){
                                            if(lineitemparentpanel.items){
                                                if(lineitemparentpanel.items.items){
                                                    if(lineitemparentpanel.items.items[0]){
                                                        lineitemparentpanel.items.items[0].summaryTableJson=summaryTableJson;
                                                        lineitemparentpanel.items.items[0].isSummaryTableApplied= true;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                }
            }),
            this.dataCellcombo = new Ext.form.field.ComboBox({
                //                    xtype: 'combo',
                hideLabel:true,
                store: defaultFieldGlobalStore,
                id: 'customdesignerdataselectcombo',
                displayField: 'label',
                valueField: 'id',
                queryMode: 'local',
                width: 250,
                emptyText: 'Select a field',
                triggerAction: 'all',
                //                multiSelect:true,
                listeners: {
                    scope: this,
                    'beforeselect': function (combo, selectedRec) {
                        if(selectedRec.data.xtype != "NAN"){
                            var isinsertnew=true;
                            if(this.cellVal.childNodes){
                                if(this.cellVal.childNodes[0]){
                                    if ( this.cellVal.childNodes[0].nodeName.toLowerCase() == "label" ) {
                                        //                                        this.cellVal.childNodes[0].remove();
                                        remove(this.cellVal.childNodes[0]);           //Done for IE.
                                    } else if ( this.cellVal.childNodes[0].nodeName.toLowerCase() == "div" ) {
                                        var div=this.cellVal.children[0];          //if div is already present in cell get that div. and if this div is data element then do following processes.
                                        for(var i=0;i<this.cellVal.children.length;i++){                 
                                            if(this.cellVal.children[i].getAttribute('type')=="dataElement"){                     //get dataelement from cell.
                                                div=this.cellVal.children[i];
                                                break;
                                            }
                                        }
                                        var oldDiv=div;
                                        var allowLabel=div.getAttribute("allowLabel");
                                        var customLabel=div.getAttribute("customlabel");
                                        if(allowLabel=="false"){                                //if user changes select field then change select field from span2.
                                            var span2=div.children[0];
                                            span2.innerHTML = "#" + selectedRec.data.label + "#";
                                            span2.setAttribute("class", "fieldItemDivBorder");
                                            if ( selectedRec.data.label === Ext.pagenumberlabel ) {
                                                span2.setAttribute("id", "pagenumberspan");
                                            }
                                            span2.style.width="100%";
                                            span2.setAttribute("attribute","{PLACEHOLDER:"+ selectedRec.data.id +"}");
                                            div.setAttribute("xtype", selectedRec.data.xtype);
                                            div.innerHTML=span2.outerHTML;
                                            isinsertnew=false;
                                        }else if(allowLabel=="true"){
                                            var span1=div.children[0];                             //if user changes select field then change select field from span2 and label from span1.
                                            var span2=div.children[1];
                                            if(!customLabel){
                                                span1.innerHTML ='<label>'+ selectedRec.data.label+'</label>';
                                            }
                                            span2.innerHTML = "#" + selectedRec.data.label + "#";
                                            span2.setAttribute("class", "fieldItemDivBorder");
                                            if ( selectedRec.data.label === Ext.pagenumberlabel ) {
                                                span2.setAttribute("id", "pagenumberspan");
                                            }
                                            span2.style.width="60%";
                                            span2.setAttribute("attribute","{PLACEHOLDER:"+ selectedRec.data.id +"}");
                                            div.setAttribute("xtype", selectedRec.data.xtype);
                                            div.innerHTML=span1.outerHTML;
                                            div.innerHTML+=span2.outerHTML;
                                            isinsertnew=false;
                                        }
                                
                                        this.cellVal.replaceChild(div,oldDiv);
                                    }
                                }
                            }
                            
                            if(isinsertnew){                           //Insert new dataelement.
                                var span2 = document.createElement("span");
                                span2.innerHTML = "#" + selectedRec.data.label + "#";
                                span2.setAttribute("class", "fieldItemDivBorder");
                                if ( selectedRec.data.label === Ext.pagenumberlabel ) {
                                    span2.setAttribute("id", "pagenumberspan");
                                }
                                span2.setAttribute("style", "width:100%;float:left;");
                                span2.setAttribute("attribute","{PLACEHOLDER:"+ selectedRec.data.id +"}");
                                    
                                    
                                var finalElement = document.createElement("div");
                                finalElement.setAttribute("style", "overflow: hidden;display:inline-block;width:100%;");
                                finalElement.setAttribute("class", "fieldItemDivBorder");
                                finalElement.setAttribute("type", "dataElement");
                                finalElement.setAttribute("onclick", "getDataElementPropertyPanel(event,this,true,false,true)");
                                finalElement.setAttribute("allowlabel", false);
                                finalElement.setAttribute("xtype", selectedRec.data.xtype);
                
                
                                finalElement.innerHTML += span2.outerHTML;
                           
                                this.cellVal.appendChild(finalElement);
                                this.valtype= 3;
                                this.cellVal.valtype = 3;
                            }
                        }
                    },
                    'select': function(combo, selection) {
                        var htmllabel = selection[0].data.label;
                        var div=this.cellVal.children[0];
                        for(var i=0;i<this.cellVal.children.length;i++){                 
                            if(this.cellVal.children[i].getAttribute('type')=="dataElement"){                     //get dataelement from cell.
                                div=this.cellVal.children[i];
                                break;
                            }
                        }
                        var customLabel=div.getAttribute("customlabel");
                        this.label = htmllabel;
                        this.xType = selection[0].data.xtype;
                        if(!customLabel){
                            getEXTComponent('idcreatelabel').setValue(htmllabel);               
                        }
                    }
                    
                }
            }),
            this.allowLabel= new Ext.form.field.Checkbox({
                fieldLabel: 'Allow Label',
                id: 'idallowlabel',
                name: 'allowlabel',
                checked:false,
                width: 300,
                listeners: {
                    scope: this,
                    change: function(field, nval, oval) {
                        if(Ext.getCmp('idcreatelabel')){
                            var div=this.cellVal.children[0];
                            for(var i=0;i<this.cellVal.children.length;i++){                 
                                if(this.cellVal.children[i].getAttribute('type')=="dataElement"){                     //get dataelement from cell.
                                    div=this.cellVal.children[i];
                                    break;
                                }
                            }
                            if(nval==true){   //allow label checkbox is checked.
                                var oldDiv=div;                           //store div in oldDiv
                                var allowLabel=div.getAttribute("allowLabel");
                                if(allowLabel=="false"){                          //if allow label of dataelement is false i.e dataelement doesn't have label and allow label checkbox is checked then add label to dataelement
                                    var span2=div.children[0];
                                    var placeHolder=span2.getAttribute("attribute");
                                    span2.style.width="60%";
                                    span2.setAttribute("attribute", placeHolder);
                                    var label = Ext.getCmp('idcreatelabel').getValue();
                                    var labelElement = document.createElement("label");
                                    labelElement.innerHTML = label;
                                    
                                    var span1 = document.createElement("span");
                                    span1.innerHTML = labelElement.outerHTML;
                                    span1.setAttribute("class", "fieldItemDivBorder");
                                    span1.setAttribute("style", "width:40%;float:left;position:relative;");
                                
                                    div.innerHTML=span1.outerHTML;
                                    div.innerHTML+=span2.outerHTML;
                                    div.setAttribute("allowlabel", true); 
                                }
                                
                                this.cellVal.replaceChild(div,oldDiv);          //replace oldDiv with new div
                                getEXTComponent('idcreatelabel').setDisabled(false);
                            }else if(nval==false){  //allow label checkbox is unchecked.
                                var oldDiv=div;
                                var allowLabel=div.getAttribute("allowLabel");
                                if(allowLabel=="true"){                  //remove label from dataelement.
                                    var span2=div.children[1];
                                    var placeHolder=span2.getAttribute("attribute");
                                    span2.style.width="100%";
                                    span2.setAttribute("attribute", placeHolder);
                                    div.innerHTML=span2.outerHTML;
                                    div.setAttribute("allowlabel", false);
                                    
                                }
                                
                                this.cellVal.replaceChild(div,oldDiv);  //replace oldDiv with new div.
                                getEXTComponent('idcreatelabel').setDisabled(true);

                            }
                        }
                    }
                }
            }),
            this.dataLabel= new Ext.form.field.Text({
                fieldLabel: 'Label',
                id: 'idcreatelabel',
                name: 'createlabel',
                value: '',
                readOnly:true,
                disabled:true,
                width: 300,
                allowBlank: false
            }),
            this.colonCombo = new Ext.form.field.ComboBox({
                fieldLabel: 'Colon Position',
                store: colonStore,
                id: 'colonPositionCombo',
                displayField: 'position',
                valueField: 'id',
                queryMode: 'local',
                width: 250,
                emptyText: 'Select a Position',
                forceSelection: false,
                editable: false,
                triggerAction: 'all',
                hidden : false,
                value :-1,
                listeners:{
                    scope:this,
                    change: function () {
                        updatePropertyofCell(this);
                    }
                }
            })]
        },
        {
            xtype: 'fieldset',
            width:260,
            id:'idfontsettingfieldset',
            title:'Font Settings',
            items:[
            this.boldText = new Ext.form.field.Checkbox({
                //                xtype: 'checkbox',
                id: 'idBoldText',
                fieldLabel: 'Bold',
                checked:config.cellVal.nodeName=='TH' ,
                listeners:{
                    scope:this,
                    change: function () {
                        updatePropertyofCell(this);
                    }
                }
            }),
            this.italicText = new Ext.form.field.Checkbox({
                //                xtype: 'checkbox',
                id: 'idItalicText',
                fieldLabel: 'Italic',
                checked:false,
                listeners:{
                    scope:this,
                    change: function () {
                        updatePropertyofCell(this);
                    }
                }
            }),
            this.applyBullets = new Ext.form.field.Checkbox ({    //  apply bullets checkbox
                id: 'idApplyBullets',
                fieldLabel: 'Apply Bullets',
                disabled: (config.valtype=='1' || config.valtype=='3' ) ? true: false,
                listeners: {
                    scope:this,
                    change: function (e) {
                        updatePropertyofCell(this);
                    }
                }
            }),
            this.bulletType = new Ext.form.field.ComboBox({   //  bullet type combo, to select bullet type
                fieldLabel: 'Bullet Type',
                id: 'idbulletType',
                store: bulletStore,
                displayField: 'value',
                disabled:(config.valtype=='1' || config.valtype=='3' ) ? true: false,
                valueField: 'id',
                width: 210,
                listeners : {
                    scope:this,
                    'select' : function(e) {
                        updatePropertyofCell(this); 
                    }
                }
            }),

            this.fontsize = new Ext.form.field.Number({
                xtype: 'numberfield',
                fieldLabel: 'Font Size:',
                id:'idfontsize',
                value: 12,
                width: 210,
                minLength: 1,
                minValue:0,
                //                scope: this,
                listeners:{
                    scope:this,
                    change: function () {
                        updatePropertyofCell(this);
                    }
                }
            }),   
            
            this.pagefontfields = new Ext.form.field.ComboBox({
                xtype: 'combo',
                fieldLabel: 'Font',
                id:'pagefontid',
                displayField: 'name',
                valueField: 'val',
                store: Ext.create("Ext.data.Store", {
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
                }),
                padding:'2 2 2 2',
                emptytext:'sans-serif',
                labelWidth:98,
                width:208,
                listeners: {
                    scope:this,
                    'select': function (e) {
                        updatePropertyofCell(this);
                    }
                }
            }),          

            this.textColorPanel = Ext.create("Ext.Panel", {
                layout: 'column',
                width: 250,
                padding:'2 2 2 0',
                border: false,
                //            hidden:(config.cellVal.nodeName == "TH")?true:false,
                items: [
                this.textColorLabel = Ext.create("Ext.form.Label", {
                    text: "Text Color",
                    width: 105
                }),
                this.selectedTextColorPanel = Ext.create("Ext.Panel", {
                    id: 'idSelectedTextPanel',
                    height: 20,
                    width: 25,
                    border: false,
                    bodyStyle: 'margin-top:1px'
                }),
                this.textColorPicker = Ext.create('Ext.ux.ColorPicker', {
                    luminanceImg: '../../images/luminance.png',
                    spectrumImg: '../../images/spectrum.png',
                    value: config.cellVal.style.color ? rgbToHex(config.cellVal.style.color, false) : '#000000',
                    id: 'colorpicker',
                    listeners: {
                        scope: this,
                        change: function (obj, newVal) {
                            Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', newVal);
                            updatePropertyofCell(this);
                        }
                    }
                })
//                this.textColorBtn = Ext.create('Ext.Button', {
//                    id: 'idTextColorBtn',
//                    menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//                        value: config.cellVal.style.color ? rgbToHex(config.cellVal.style.color,false) : '000000',
//                        id: 'colorpicker',
//                        scope:this,
//                        handler: function (obj, rgb) {
//                            Ext.colorpicker = Ext.getCmp('colorpicker');
//                            var selectednewcolor = "#" + rgb.toString();
//                            Ext.colorpicker.value = selectednewcolor;
//                            Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', selectednewcolor);
//                            updatePropertyofCell(this);
//                        }// handler
//                    }), // menu
//                    text: ''
//                }),
            ]
        }),
   
            this.textLine =new Ext.form.field.ComboBox({
                fieldLabel: 'Text Line',
                id: 'idTextLine',
                store: Ext.create("Ext.data.Store", {
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
                }),
                displayField: 'textline',
                valueField: 'id',
                padding: '2 2 2 0',
                listWidth: 200,
                width: 210,
                listeners: {
                    scope:this,
                    'select': function(e) {
                        updatePropertyofCell(this);
                    }
                }

            })
            ]
          
        },
        {
            xtype: 'fieldset',
            width:260,
            height :50,
            title:'Color Settings',
            id:'idcellcolorsettingsfieldset',
            items: [
            this.cellColorPanel = Ext.create("Ext.Panel", {
                layout: 'column',
                width: 250,
                padding:'2 2 2 0',
                border: false,
                items: [
                this.cellColorLabel = Ext.create("Ext.form.Label", {
                    text: "Background Color",
                    width: 110
                }),
                this.selectedCellColorPanel = Ext.create("Ext.Panel", {
                    id: 'idSelectedCellPanel',
                    height: 20,
                    width: 25,
                    border: false,
                    bodyStyle: 'margin-top:1px'
                }),
                this.cellColorPicker = Ext.create('Ext.ux.ColorPicker', {
                    luminanceImg: '../../images/luminance.png',
                    spectrumImg: '../../images/spectrum.png',
                    value: config.backgroundColor ? rgbToHex(config.backgroundColor, false) : '#FFFFFF',
                    id: 'cellcolorpicker',
                    listeners: {
                        scope: this,
                        change: function (config, newVal) {
                            Ext.getCmp('idSelectedCellPanel').body.setStyle('background-color', newVal);
                            updatePropertyofCell(this);
                        }
                    }
                })
//                this.cellColorBtn = Ext.create('Ext.Button', {
//                    id: 'idCellColorBtn',
//                    menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//                      //                        value: '#ffffff',
//                        id: 'cellcolorpicker',
//                        scope:this,
//                        handler: function (obj, rgb) {
//                            Ext.colorpicker = Ext.getCmp('cellcolorpicker');
//                            var selectednewcolor = "#" + rgb.toString();
//                            Ext.colorpicker.value = selectednewcolor;
//                            Ext.getCmp('idSelectedCellPanel').body.setStyle('background-color', selectednewcolor);
//                            updatePropertyofCell(this);
//                        }
//                    }), 
//                    text: ''
//                })]
//               
            ]
            })
            ]
        },
        {
            xtype:'fieldset',
            title:(config.cellVal.nodeName == "TH")?'Alignment Settings':'Space Settings',
            width:260,
            id:'idspacesettingfieldset',
            items:[
            this.cellwidth = new Ext.form.field.Number({
                width: 210,
                name: 'cellwidth',
                id:'idcellwidth',
                itemId: 'idcellwidth',
                fieldLabel: 'Width (%)',
                maxValue: 100,
                minValue: 1,
                value:config.cellVal.style.width?config.cellVal.style.width:10,
                hidden:(config.isSummaryTable || config.cellVal.nodeName == "TH")?true:false,
                scope: this,
                listeners:{
                    scope:this,
                    change: function (field) {
                        if ( field.getValue() > 100 ) {
                            field.setValue(100);
                            WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                        }
                        updatePropertyofCell(this);
                    }
                }
            }),
            this.cellheight = new Ext.form.field.Number({
                width: 210,
                name: 'cellheight',
                id:'idcellheight',
                itemId: 'cellheight',
                fieldLabel: 'Row Height',
                hidden:(config.cellVal.nodeName == "TH")?true:false, 
                minValue: 0,
                value:config.cellVal.height?config.cellVal.height:10 ,
                listeners:{
                    scope:this,
                    change: function () {
                        updatePropertyofCell(this);
                    }
                }
            }),
            this.textaligntd = new Ext.form.field.ComboBox({
                fieldLabel: 'Alignments',
                store: Ext.alignStore,
                id: 'alignselectcomboid',
                displayField: "name",
                valueField: 'position',
                queryMode: 'local',
                width:210,
                value:(config.celltextalign)?config.celltextalign:"center",
                //                hidden:(config.cellVal.nodeName == "TH")?false:true,
                listeners:{
                    scope:this,
                    change: function () {
                        updatePropertyofCell(this);
                    }
                }
            }),
            this.VerticalTextAlignTD = new Ext.form.field.ComboBox({
                fieldLabel: 'Vertical Alignments',
                store: Ext.ValignStore,
                id: 'Valignselectcomboid',
                displayField: "name",
                valueField: 'position',
                queryMode: 'local',
                width:210,
                value:(config.cellVal.style.verticalAlign)?config.cellVal.style.verticalAlign:((config.cellVal.nodeName == "TH")?"middle":"top"),
                listeners:{
                    scope:this,
                    change: function () {
                        updatePropertyofCell(this);
                    }
                }
            })
            ]
        },
        {
            xtype:'fieldset',
            title:'Border Settings',
            //            hidden:(config.cellVal.nodeName == "TH")?true:false,
            width: 260,
            items:[
            {
                layout:'column',
                border:false, 
                items:[{
                    xtype:'panel',
                    columnWidth:.90,
                    border:false,
                    items : [
                    this.lineComboT = new Ext.form.field.ComboBox({
                        columnWidth:.50,
                        fieldLabel: 'Top',
                        store: lineStore,
                        id: 'lineStoreComboTop',
                        displayField: 'type',
                        valueField: 'id',
                        queryMode: 'local',
                        width: 210,
                        emptyText: 'Select a type',
                        forceSelection: false,
                        editable: false,
                        triggerAction: 'all',
                        hidden : false,
                        value :0,
                        listeners:{
                            scope:this,
                            change: function () {
                                updatePropertyofCell(this);
                            }
                        }
                    }),
                    this.lineComboB = new Ext.form.field.ComboBox({
                        columnWidth:.50,
                        fieldLabel: 'Bottom',
                        store: lineStore,
                        id: 'lineStoreComboBottom',
                        displayField: 'type',
                        valueField: 'id',
                        queryMode: 'local',
                        width: 210,
                        emptyText: 'Select a type',
                        forceSelection: false,
                        editable: false,
                        triggerAction: 'all',
                        hidden : false,
                        value :0,
                        listeners:{
                            scope:this,
                            change: function () {
                                updatePropertyofCell(this);
                            }
                        }
                    }),
                    this.lineComboL = new Ext.form.field.ComboBox({
                        columnWidth:.50,
                        fieldLabel: 'Left',
                        store: lineStore,
                        id: 'lineStoreComboLeft',
                        displayField: 'type',
                        valueField: 'id',
                        queryMode: 'local',
                        width: 210,
                        emptyText: 'Select a type',
                        forceSelection: false,
                        editable: false,
                        triggerAction: 'all',
                        hidden : false,
                        value :0,
                        listeners:{
                            scope:this,
                            change: function () {
                                updatePropertyofCell(this);
                            }
                        }
                    }),
                    this.lineComboR = new Ext.form.field.ComboBox({
                        columnWidth:.50,
                        fieldLabel: 'Right',
                        store: lineStore,
                        id: 'lineStoreComboRight',
                        displayField: 'type',
                        valueField: 'id',
                        queryMode: 'local',
                        width: 210,
                        emptyText: 'Select a type',
                        forceSelection: false,
                        editable: false,
                        triggerAction: 'all',
                        hidden : false,
                        value :0,
                        listeners:{
                            scope:this,
                            change: function () {
                                updatePropertyofCell(this);
                            }
                        }
                    }),
                    this.BorderColorPanel = Ext.create("Ext.Panel", {
                        layout: 'column',
                        width: 250,
                        padding:'2 2 2 0',
                        border: false,
                        //            hidden:(config.cellVal.nodeName == "TH")?true:false,
                        items: [
                        this.ColorLabel = Ext.create("Ext.form.Label", {
                            text: "Border Color",
                            width: 105
                        }),
                        this.selectedBorderColorPanel = Ext.create("Ext.Panel", {
                            id: 'idSelectedBorderPanel',
                            height: 20,
                            width: 25,
                            border: false,
                            bodyStyle:'margin-top:1px'
                        }),
                        this.textcolorpick = Ext.create('Ext.ux.ColorPicker', {
                            luminanceImg: '../../images/luminance.png',
                            spectrumImg: '../../images/spectrum.png',
                            value: config.cellVal.style.color ? rgbToHex(config.cellVal.style.color, false) : '#000000',
                            id: 'Bordercolorpicker',
                            listeners: {
                                scope: this,
                                change: function (obj, newVal) {
                                    Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', newVal);
                                    updatePropertyofCell(this);
                                }
                            }
                        })
//                        this.textColorBtn = Ext.create('Ext.Button', {
//                            id: 'idBorderColorBtn',
//                            menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//                                value: config.cellVal.style.color ? rgbToHex(config.cellVal.style.color,false) : '000000',
//                                id: 'Bordercolorpicker',
//                                scope:this,
//                                handler: function (obj, rgb) {
//                                    Ext.colorpicker = Ext.getCmp('Bordercolorpicker');
//                                    var selectednewcolor = "#" + rgb.toString();
//                                    Ext.colorpicker.value = selectednewcolor;
//                                    Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', selectednewcolor);
//                                    updatePropertyofCell(this);
//                                }// handler
//                            }), // menu
//                            text: ''
//                        })
//                        
                    ]
                    })]
                
                }]
            }]
        },
        {
            xtype:'fieldset',
            title:'Margin Settings',
            width: 260,
            items:[
            {
                layout:'column',
                border:false, 
                items:[{
                    xtype:'panel',
                    columnWidth:.90,
                    border:false,
                    items : [
                    this.topPadding = Ext.create('Ext.form.NumberField',{
                        fieldLabel: 'Top Margin',
                        id: 'idtoppadding',
                        labelWidth: 105,
                        value: 0,
                        padding:'2 2 2 2',
                        width: 170,
                        minLength: 1,
                        maxLength: 3,
                        maxValue:100,
                        minValue:0,
                        hidden:false,
                        listeners: {
                            scope:this,
                            change: function () {
                                updatePropertyofCell(this);
                            }
                        }
                    }),
                    this.bottomPadding = Ext.create('Ext.form.NumberField',{
                        fieldLabel: 'Bottom Margin',
                        id: 'idbottompadding',
                        labelWidth: 105,
                        value: 0,
                        padding:'2 2 2 2',
                        width: 170,
                        minLength: 1,
                        maxLength: 3,
                        maxValue:100,
                        minValue:0,
                        hidden:false,
                        listeners: {
                            scope:this,
                            change: function () {
                                updatePropertyofCell(this);
                            }
                        }
                    }),
                    this.leftPadding = Ext.create('Ext.form.NumberField',{
                        fieldLabel: 'Left Margin',
                        id: 'idleftpadding',
                        labelWidth: 105,
                        value: 0,
                        padding:'2 2 2 2',
                        width: 170,
                        minLength: 1,
                        maxLength: 3,
                        maxValue:100,
                        minValue:0,
                        hidden:false,
                        listeners: {
                            scope:this,
                            change: function () {
                                updatePropertyofCell(this);
                            }
                        }
                    }),
                    this.rightPadding = Ext.create('Ext.form.NumberField',{
                        fieldLabel: 'Right Margin',
                        id: 'idrightpadding',
                        labelWidth: 105,
                        value: 0,
                        padding:'2 2 2 2',
                        width: 170,
                        minLength: 1,
                        maxLength: 3,
                        maxValue:100,
                        minValue:0,
                        hidden:false,
                        listeners: {
                            scope:this,
                            change: function () {
                                updatePropertyofCell(this);
                            }
                        }
                    })]
            
                }]
            }]
        },
        {
            xtype:'fieldset',
            layout:'column',
            width: 250,
            title : 'Merge Cells',
            hidden:(config.cellVal.nodeName == "TH" || config.cellVal.offsetParent.getAttribute("isbalanceoutstanding") == "true")?true:false, 
            items:[
            this.colspan = new Ext.Button({
                text: 'Merge Columns',
                name: 'colSpanId',
                id: 'colSpanId',
                columnWidth:.50,
                margin:'5 5 5 5',
                config:config,
                listeners:{
                    scope:this,
                    config:config,
                    click: function () {
                        var table = config.cellVal.parentNode.parentNode.parentNode;
                        var rowspan = config.cellVal.rowSpan;
                        var rowIndex = config.row;
                        var columnIndex = config.column;
                        var cellArray =[];
                        if( config.cellVal.nextSibling ){
                            if( rowspan > 1 ){
                                if(table.rows){
                                    for( var i= 1; i< rowspan; i++){
                                        var row = table.rows[rowIndex + i];
                                        if(row.cells){
                                            if(row.cells[columnIndex]){
                                                var cell = row.cells[columnIndex + 1];
                                                cellArray.push(cell);
                                            }
                                        }
                                    }
                                    for(var cellCnt = 0; cellCnt < cellArray.length; cellCnt++){
                                        var cell = cellArray[cellCnt];
                                        //                                                cell.remove();
                                        remove(cell);           //Done for IE.
                                    }
                                }
                                var siblingWidth = parseInt(config.cellVal.nextSibling.style.width);
                                var width = parseInt(config.cellVal.style.width);
                                config.cellVal.colSpan = config.cellVal.colSpan +1;
                                config.cellVal.style.width = (siblingWidth + width) + "%";
                                //                                       config.cellVal.nextSibling.remove();
                                remove(config.cellVal.nextSibling);             //Done for IE.
                            } else{
                                var siblingWidth = parseInt(config.cellVal.nextSibling.style.width);
                                var width = parseInt(config.cellVal.style.width);
                                var nextColSpan = (config.cellVal.nextSibling.colSpan > 1) ? config.cellVal.nextSibling.colSpan-1 : 0;
                                config.cellVal.colSpan = config.cellVal.colSpan +1 + nextColSpan;
                                config.cellVal.style.width = (siblingWidth + width) + "%";
                                //                                        config.cellVal.nextSibling.remove();
                                remove(config.cellVal.nextSibling);          //Done for IE.
                            }
                        }
                        if (config.valType == 2 ) {
                            Ext.getCmp('customdesignerfieldlabel').setValue(config.cellVal.innerHTML);
                        }    
                    }
                }
            }),
            this.rowspan = new Ext.Button({
                text: 'Merge Rows',
                name: 'rowSpanId',
                id: 'rowSpanId',
                columnWidth:.50,
                margin:'5 5 5 5',
                config:config,
                listeners:{
                    scope:this,
                    config:config,
                    click: function () {
                        var originalInnerHtml = config.cellVal.innerHTML ;
                        var table = config.cellVal.parentNode.parentNode.parentNode;
                        var rowIndex = config.row;
                        var columnIndex = config.column;
                        var rowspan = config.cellVal.rowSpan;
                        var colspan = config.cellVal.colSpan;
                        var innerHtml = " ";
                        var cellArray =[];
                        if(table.rows){
                            if(table.rows[rowIndex+rowspan]){
                                var row =  table.rows[rowIndex+rowspan] ;
                                if(row.cells){
                                    if( colspan > 1 ){
                                        for(var colCnt = 0 ; colCnt < colspan; colCnt++){
                                            if(row.cells[columnIndex+colCnt]){
                                                var cell = row.cells[columnIndex+colCnt];
                                                innerHtml += cell.innerHTML;
                                                cellArray.push(cell);
                                            }
                                        }
                                        for(var cellCnt = 0; cellCnt < cellArray.length; cellCnt++){
                                            var cell = cellArray[cellCnt];
                                            //                                                    cell.remove();
                                            remove(cell);            //Done for IE.
                                        }
                                        config.cellVal.innerHTML = originalInnerHtml;
                                        config.cellVal.rowSpan = rowspan+1;
                                    } else{
                                        if(row.cells[columnIndex]){
                                            var cell = row.cells[columnIndex];
                                            var nextRowSpan = (cell.rowSpan > 1) ? cell.rowSpan-1 : 0;

                                            config.cellVal.innerHTML = originalInnerHtml;
                                            config.cellVal.rowSpan = rowspan+1 + nextRowSpan;
                                            cell.setAttribute('style',"display:none;");
                                        }
                                    }
                                }
                            }
                        }
                        if (config.valType == 2 ) {
                            Ext.getCmp('customdesignerfieldlabel').setValue(config.cellVal.innerHTML);
                        }
                    }
                }
            })
            ] 
        }
        ]
    }, config);
    
    setPropertyofCell(config);
    Ext.GlobalCellPropertyNew.superclass.constructor.call(this, config);
}

Ext.extend(Ext.GlobalCellPropertyNew, Ext.Panel, {
    width: 300,
    autoHeight : true,
    autoScroll: true,
    id: 'idPropertyPanel',
    padding: '5 5 5 5',
    plain: true,
    scope: this,
    modal: true
});

function setPropertyofCell(obj) {
    var fontsize = obj.cellVal.style.fontSize ? obj.cellVal.style.fontSize : ''
    var fontfamily = obj.cellVal.style.fontFamily ? obj.cellVal.style.fontFamily : '';
    var isitalic = (obj.cellVal.style.fontStyle) ? obj.cellVal.style.fontStyle == "italic" : false;
    var cellheight = (obj.cellVal.parentNode.style.height) ? obj.cellVal.parentNode.style.height : '10';
    var cellwidth = (obj.cellVal.style.width) ?  obj.cellVal.style.width : (100/obj.columnCount);
    var textLine = obj.cellVal.style.textDecoration?obj.cellVal.style.textDecoration:"none";
    var textColor = obj.cellVal.style.color?obj.cellVal.style.color:"#000000";
    var cellColor = obj.cellVal.style.backgroundColor?obj.cellVal.style.backgroundColor:"";
    var borderColor = obj.cellVal.style.borderColor?obj.cellVal.style.borderColor:"#000000";
    var vAlign = obj.cellVal.style.verticalAlign?obj.cellVal.style.verticalAlign:((obj.cellVal.nodeName == "TH")?"middle":"top");

    var bordertopstyle;
    var borderbottomstyle;
    var borderrightstyle;
    var borderleftstyle;
    var alignstyle;
    
    var isbold ='';
    var topPadding = '';
    var bottomPadding = '';
    var leftPadding = '';
    var rightPadding = '';
    
    if(obj.cellVal.nodeName=='TH'){
       if(obj.cellVal.style.fontWeight){
            isbold = (obj.cellVal.style.fontWeight == "bold") ? true: false;
        }else{
            isbold=true;
        }
        cellwidth = obj.cellVal.getAttribute("colWidth");
        alignstyle = obj.cellVal.style.textAlign?obj.cellVal.style.textAlign:'center';
        topPadding = obj.cellVal.style.paddingTop ? obj.cellVal.style.paddingTop :1;
        bottomPadding = obj.cellVal.style.paddingBottom ? obj.cellVal.style.paddingBottom :1;
        leftPadding = obj.cellVal.style.paddingLeft ? obj.cellVal.style.paddingLeft :1;
        rightPadding = obj.cellVal.style.paddingRight ? obj.cellVal.style.paddingRight :1;
    }else{
        isbold = (obj.cellVal.style.fontWeight) ? obj.cellVal.style.fontWeight == "bold" : false;
        alignstyle = obj.cellVal.style.textAlign?obj.cellVal.style.textAlign:'left';
        topPadding = obj.cellVal.style.paddingTop ? obj.cellVal.style.paddingTop :5;
        bottomPadding = obj.cellVal.style.paddingBottom ? obj.cellVal.style.paddingBottom :5;
        leftPadding = obj.cellVal.style.paddingLeft ? obj.cellVal.style.paddingLeft :5;
        rightPadding = obj.cellVal.style.paddingRight ? obj.cellVal.style.paddingRight :5;
    }
    
    bordertopstyle = obj.cellVal.style.borderTopStyle? obj.cellVal.style.borderTopStyle : "solid";
    borderbottomstyle = obj.cellVal.style.borderBottomStyle? obj.cellVal.style.borderBottomStyle : "solid";
    borderrightstyle = obj.cellVal.style.borderRightStyle? obj.cellVal.style.borderRightStyle : "solid";
    borderleftstyle = obj.cellVal.style.borderLeftStyle? obj.cellVal.style.borderLeftStyle : "solid";

    
    if ( Ext.getCmp("idTextLine") ) {
        Ext.getCmp("idTextLine").setValue(textLine);
    }
    if ( Ext.getCmp("colorpicker") ) {
        Ext.getCmp("colorpicker").value = textColor;
    }
    if ( Ext.getCmp("idSelectedTextPanel") ) {
        Ext.getCmp("idSelectedTextPanel").setBodyStyle("background-color",textColor);
    }
    if ( Ext.getCmp("cellcolorpicker") ) {
        Ext.getCmp("cellcolorpicker").value = cellColor;
    }
    if ( Ext.getCmp("idSelectedCellPanel") ) {
        Ext.getCmp("idSelectedCellPanel").setBodyStyle("background-color",cellColor);
    }
    if ( Ext.getCmp("Bordercolorpicker") ) {
        Ext.getCmp("Bordercolorpicker").value = borderColor;
    }
    if ( Ext.getCmp("idSelectedBorderPanel") ) {
        Ext.getCmp("idSelectedBorderPanel").setBodyStyle("background-color",borderColor);
    }
    
    if (obj.htmleditlabel != undefined && obj.htmleditlabel != "") {
        var arr = obj.htmleditlabel.match(/\{PLACEHOLDER:(.*?)}/g);
        var dataelement = obj.htmleditlabel.match(/<label>.*?\{PLACEHOLDER:(.*?)}/g);
        var records = [];
        var div=obj.cellVal.children[0];
        for(var i=0;i<obj.cellVal.children.length;i++){
            if(obj.cellVal.children[i].getAttribute('type')=="dataElement"){             //get data element from cell.
                div=obj.cellVal.children[i];
                break;
            }
        }
        if (div.attributes && div.attributes.getNamedItem("type") && div.attributes.getNamedItem("type").value=="dataElement") {
            var datalength = 0;
            if(dataelement!=null){
                datalength=dataelement.length;
            }else{
                datalength=arr.length;
            }
            for (var i = 0; i <= datalength - 1; i++) {
                var matches = arr[i].replace(/\{|\}/gi, '').split(":");
                obj['placeholder'] = matches[1];
                var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                if (rec == undefined || rec == null) {
                    var newinnerhtml = obj.cellVal.innerHTML;
                    this.newlabel = this.newlabel + "," + obj.cellVal.className;
                } else {
                    var newlabelindividual = rec.data.label;
                    records.push(rec);
                    if (i > 0) {
                        this.newlabel = this.newlabel + "," + newlabelindividual;
                    } else {
                        this.newlabel = newlabelindividual;
                    }
                }
            }
            var allowLabel=div.getAttribute('allowLabel');
            if(allowLabel){
                var label=div.children[0].textContent.replace(/#/ig,"").replace(':','');                //get label for data element.
            }
//            Ext.getCmp('idLabelValue').show();
            Ext.getCmp('idradiocell').items.items[0].setValue(false);
            Ext.getCmp('idradiocell').items.items[1].setValue(false);
            Ext.getCmp('idradiocell').items.items[2].setValue(true);
            getEXTComponent('customdesignerdataselectcombo').show();
            getEXTComponent('idallowlabel').show();
            getEXTComponent('idcreatelabel').show();
            getEXTComponent('customdesignerdataselectcombo').setValue(matches[1]);               //set value for select field combo.
            getEXTComponent('idcreatelabel').setValue(label);                                 //set value for label.
            getEXTComponent('idallowlabel').setValue(allowLabel);
            Ext.getCmp('customdesignerfieldlabel').hide();
            Ext.getCmp('inserttextfieldlabel').hide();
            Ext.getCmp('customdesignerfieldselectcombo').hide();
            Ext.getCmp('colonPositionCombo').hide();
            obj.valtype = 3;
        } else if (arr) {
            var arraylength = arr.length;
            for (var i = 0; i <= arraylength - 1; i++) {
                var matches = arr[i].replace(/\{|\}/gi, '').split(":");
                obj['placeholder'] = matches[1];
                var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                if (rec == undefined || rec == null) {
                    var newinnerhtml = this.cellVal.innerHTML;
                    this.newlabel = this.newlabel + "," + this.cellVal.className;
                } else {
                    var newlabelindividual = rec.data.label;
                    records.push(rec);
                    if (i > 0) {
                        this.newlabel = this.newlabel + "," + newlabelindividual;
                    } else {
                        this.newlabel = newlabelindividual;
                    }
                }
            }
            Ext.getCmp('idradiocell').items.items[0].setValue(true);
            Ext.getCmp('idradiocell').items.items[1].setValue(false);
            Ext.getCmp('customdesignerfieldlabel').hide();
            Ext.getCmp('inserttextfieldlabel').hide();
            Ext.getCmp('customdesignerfieldselectcombo').setValue(this.newlabel);
            Ext.getCmp('customdesignerfieldselectcombo').select(records);
            Ext.getCmp('customdesignerfieldselectcombo').show();
            Ext.getCmp('colonPositionCombo').hide();
            getEXTComponent('customdesignerdataselectcombo').hide();      //hide data element component.
            getEXTComponent('idallowlabel').hide();
            getEXTComponent('idcreatelabel').hide();
            obj.valtype = 1;
        } else {
            var applyBulletCheckBox = getEXTComponent("idApplyBullets"); 
            var bulletTypeCombo = getEXTComponent("idbulletType");
            Ext.getCmp('idradiocell').items.items[0].setValue(false);
            Ext.getCmp('idradiocell').items.items[1].setValue(true);
            var textvalue = obj.cellVal.innerHTML;
            var isBulletApplied = (obj.cellVal && obj.cellVal.attributes && obj.cellVal.attributes.getNamedItem('isBulletApplied') && (obj.cellVal.attributes.getNamedItem('isBulletApplied').value === 'true') ) ? true : false;
            var bulletType= (obj.cellVal && obj.cellVal.attributes && obj.cellVal.attributes.getNamedItem('bulletType')) ? obj.cellVal.attributes.getNamedItem('bulletType').value : 'none';
            
            textvalue = textvalue.replace(/<span[\s\w\d=;:"'().\/,\-]*>(.|\n)*?<\/span>/gi,"");
            textvalue = textvalue.replace(/<div[\s\w\d=;:"'().\/,\-]*>(.|\n)*?<\/div>/gi,"");
            textvalue = textvalue.replace(/<label[\s\w\d=;:"'().\/,\-]*>/gi,"");
            //            textvalue = textvalue.replace("<label>","");
            textvalue = textvalue.replace("</label>","");
            textvalue = textvalue.replace(/<br>/g,"\n");
            textvalue = textvalue.replace(/&nbsp;/g,' ');
            textvalue = textvalue.replace(/<[^<>]*>/g,'');
            Ext.getCmp('customdesignerfieldlabel').setValue(textvalue);
            Ext.getCmp('customdesignerfieldlabel').show();
            Ext.getCmp('inserttextfieldlabel').show();
            Ext.getCmp('customdesignerfieldselectcombo').hide();
            Ext.getCmp('colonPositionCombo').show();
            Ext.getCmp('colonPositionCombo').setValue(parseInt(obj.cellVal.getAttribute("colontype")?obj.cellVal.getAttribute("colontype"):"-1"));
            getEXTComponent('customdesignerdataselectcombo').hide();           //hide data element component.
            getEXTComponent('idallowlabel').hide();
            getEXTComponent('idcreatelabel').hide();
            Ext.getCmp('selectfieldlabel').hide();
            obj.valtype = 2;
            if ( applyBulletCheckBox ) {
                applyBulletCheckBox.setValue(isBulletApplied);
            }
            if ( bulletTypeCombo ) {
                bulletTypeCombo.setValue(bulletType);
            }
        }
    }
    if ( Ext.getCmp('idfontsize') ) {
        Ext.getCmp('idfontsize').setValue(fontsize);
    }
    if ( Ext.getCmp('pagefontid') ) {
        Ext.getCmp('pagefontid').setValue(fontfamily);
    }
    if ( Ext.getCmp('idBoldText') ) {
        Ext.getCmp('idBoldText').setValue(isbold);
    }
    if ( Ext.getCmp('idItalicText') ) {
        Ext.getCmp('idItalicText').setValue(isitalic);
    }
    if ( Ext.getCmp("idcellheight") ) {
        Ext.getCmp('idcellheight').setValue(cellheight);
    }
    if ( Ext.getCmp("idcellwidth") ) {
        Ext.getCmp('idcellwidth').setValue(cellwidth);
    }
    if ( Ext.getCmp('lineStoreComboTop') ) {
        Ext.getCmp('lineStoreComboTop').setValue(bordertopstyle);
    }
    if ( Ext.getCmp('lineStoreComboBottom') ) {
        Ext.getCmp('lineStoreComboBottom').setValue(borderbottomstyle);
    }
    if ( Ext.getCmp('lineStoreComboLeft') ) {
        Ext.getCmp('lineStoreComboLeft').setValue(borderleftstyle);
    }
    if ( Ext.getCmp('lineStoreComboRight') ) {
        Ext.getCmp('lineStoreComboRight').setValue(borderrightstyle);
    }
    if ( Ext.getCmp("alignselectcomboid") ) {
        Ext.getCmp("alignselectcomboid").setValue(alignstyle);
    }
    if ( Ext.getCmp("Valignselectcomboid")) {
        Ext.getCmp("Valignselectcomboid").setValue(vAlign);
    }
    if ( Ext.getCmp("idtoppadding") ) {
        Ext.getCmp("idtoppadding").setValue(topPadding);
    }
    if ( Ext.getCmp("idbottompadding") ) {
        Ext.getCmp("idbottompadding").setValue(bottomPadding);
    }
    if ( Ext.getCmp("idleftpadding") ) {
        Ext.getCmp("idleftpadding").setValue(leftPadding);
    }
    if ( Ext.getCmp("idrightpadding") ) {
        Ext.getCmp("idrightpadding").setValue(rightPadding);
    }
};

function updatePropertyofCell(obj) {
    
    var applyBulletCheckBox = getEXTComponent("idApplyBullets"); 
    var bulletTypeCombo = getEXTComponent("idbulletType");
    var isBulletsApplied = false;  
    var bulletType = "none";       
    var borderTopType = obj.lineComboT.getValue();
    var borderBottomType = obj.lineComboB.getValue();
    var borderLeftType = obj.lineComboL.getValue();
    var borderRightType = obj.lineComboR.getValue();
    
    if ( applyBulletCheckBox ) {   // checking for component  
        isBulletsApplied = applyBulletCheckBox.getValue()?applyBulletCheckBox.getValue():false;   // checking if bullets are appllied or not.
    }
    if ( bulletTypeCombo ) { // checking for component
        bulletType = bulletTypeCombo.getValue()?bulletTypeCombo.getValue():"none";  // fetching bullet type, if not present setting it to "none".
    }
    if ( Ext.getCmp("idtoppadding") ) {
        obj.cellVal.style.paddingTop = Ext.getCmp("idtoppadding").getValue()+"px";
    }
    if ( Ext.getCmp("idbottompadding") ) {
        obj.cellVal.style.paddingBottom = Ext.getCmp("idbottompadding").getValue()+"px";
    }
    if ( Ext.getCmp("idleftpadding") ) {
        obj.cellVal.style.paddingLeft = Ext.getCmp("idleftpadding").getValue()+"px";
    }
    if ( Ext.getCmp("idrightpadding") ) {
        obj.cellVal.style.paddingRight = Ext.getCmp("idrightpadding").getValue()+"px";
    }
    if ( Ext.getCmp("idcellheight") ) {
        obj.cellVal.style.height = Ext.getCmp("idcellheight").getValue() + "px";
        obj.cellVal.parentNode.style.height = Ext.getCmp("idcellheight").getValue() + "px";
    }
    if ( Ext.getCmp("idcellwidth") && !obj.isSummaryTable ) {
        if(Ext.getCmp("idcellwidth").getValue() <= 100){
            obj.cellVal.style.width = Ext.getCmp("idcellwidth").getValue()+"%";
        }
    }
    if ( Ext.getCmp("Valignselectcomboid")) {
        obj.cellVal.style.verticalAlign = Ext.getCmp("Valignselectcomboid").getValue();
    }
    if( Ext.getCmp("idBoldText") ) {
        if ( Ext.getCmp("idBoldText").getValue() == true) {
            obj.cellVal.style.fontWeight="bold";
        } else {
            obj.cellVal.style.fontWeight="normal";
        }
    }
                    
    if( Ext.getCmp("idItalicText") ) {
        if ( Ext.getCmp("idItalicText").getValue() == true) {
            obj.cellVal.style.fontStyle="italic";
        } else {
            obj.cellVal.style.fontStyle="normal";
        }
    }
        
    var textLine;
    if ( Ext.getCmp("idTextLine") ) {
        textLine = Ext.getCmp("idTextLine").getValue();
        obj.cellVal.style.textDecoration = textLine;
    }
    
    var  textColor;
    if ( Ext.getCmp("colorpicker") ) {
        textColor = Ext.getCmp("colorpicker").value;
        obj.cellVal.style.color = textColor;
    }
    var  cellColor;
    if ( Ext.getCmp("cellcolorpicker") ) {
        cellColor = Ext.getCmp("cellcolorpicker").value;
        obj.cellVal.style.backgroundColor = cellColor;
    }
    var  BorderColor;
    if ( Ext.getCmp("Bordercolorpicker") ) {
        BorderColor = Ext.getCmp("Bordercolorpicker").value;
        obj.cellVal.style.borderColor = BorderColor;
    }
    
    if( Ext.getCmp("alignselectcomboid")){
        var position = Ext.getCmp("alignselectcomboid").getValue();
        obj.cellVal.celltextalign = position;
        if(position == "center"){
            obj.cellVal.style.textAlign = "center";
        } else if(position == "left"){
            obj.cellVal.style.textAlign = "left";
            obj.cellVal.style.paddingLeft = "4px";
        } else if(position == "right"){
            obj.cellVal.style.textAlign = "right";
            obj.cellVal.style.paddingRight = "4px";
        }
    }
    
    obj.cellVal.style.borderWidth="1px thin"; 
    
    obj.cellVal.style.borderLeftStyle= borderLeftType;
    if ( borderLeftType === "double") {
        obj.cellVal.style.borderLeftWidth="4px";
    } else {
        obj.cellVal.style.borderLeftWidth="1px thin";
    }
    
    
    
    obj.cellVal.style.borderRightStyle= borderRightType;
    if ( borderRightType === "double") {
        obj.cellVal.style.borderRightWidth="4px";
    } else {
        obj.cellVal.style.borderRightWidth="1px thin";
    }
    
    obj.cellVal.style.borderTopStyle= borderTopType;
    if ( borderTopType === "double") {
        obj.cellVal.style.borderTopWidth="4px";
    } else {
        obj.cellVal.style.borderTopWidth="1px thin";
    }
    
    obj.cellVal.style.borderBottomStyle= borderBottomType;
    if ( borderBottomType === "double") {
        obj.cellVal.style.borderBottomWidth="4px";
    } else {
        obj.cellVal.style.borderBottomWidth="1px thin";
    }
        
    if (obj.valtype == 2 ) {                                               // Handling InnerHTML
        var editvalue = Ext.getCmp('customdesignerfieldlabel').getValue();
        if(editvalue === " " && editvalue.length === 1){
            editvalue = "&nbsp;";
        }
        editvalue = editvalue.replace(/\n/g,'<br>');
        editvalue = editvalue.replace(/\s\s/g," "+space);
        if (isBulletsApplied) {
            editvalue = addBullets(editvalue,bulletType);
            obj.cellVal.setAttribute('isBulletApplied',true);
            obj.cellVal.setAttribute('bulletType',bulletType);
        } else {
            editvalue =  remBullets(editvalue,bulletType);
            obj.cellVal.setAttribute('isBulletApplied',false);
        }
        var colonType = -1
        if(obj.colonCombo){
            if(obj.colonCombo.getValue()!=null && obj.colonCombo.getValue()===0){//With the text
                editvalue = editvalue+"<span class='colonwiththetext'>:</span>";
                colonType=0;
            } else if(obj.colonCombo.getValue()===1){ // Right Aligned
                editvalue = editvalue+"<span class='rightalignedcolon'>:</span>";
                colonType=1;
            }
        }
        obj.cellVal.innerHTML = "<label>"+editvalue+"</label>";
        obj.cellVal.setAttribute('colontype',colonType);
    //                        labelHTML = obj.cellVal.innerHTML;
    }
    obj.cellVal.valtype = obj.valtype;
    
    if ( Ext.getCmp("idfontsize") ) {
        if ( Ext.getCmp("idfontsize").getValue() > 0 ){
            obj.cellVal.style.fontSize = Ext.getCmp("idfontsize").getValue() + "px"; // Set FontSize
            var children ;
            if ( obj.cellVal.children ) {
                children = obj.cellVal.children;
                for ( var ifs = 0; ifs < children.length; ifs++) {
                    obj.cellVal.children[ifs].style.fontSize =  Ext.getCmp("idfontsize").getValue() + "px";
                }
            }
        } else {
            obj.cellVal.style.fontSize = "";
            var children ;
            if ( obj.cellVal.children ) {
                children = obj.cellVal.children;
                for ( var ifs = 0; ifs < children.length; ifs++) {
                    obj.cellVal.children[ifs].style.fontSize =  "";
                }
            }
        }
    }  
    if ( Ext.getCmp("pagefontid") ) {
        
        obj.cellVal.style.fontFamily = Ext.getCmp("pagefontid").getValue(); // Set FontFamily
        var children ;
        if ( obj.cellVal.children ) {
            children = obj.cellVal.children;
            for ( var ifs = 0; ifs < children.length; ifs++) {
                obj.cellVal.children[ifs].style.fontFamily =  Ext.getCmp("pagefontid").getValue();
            }
        }
    }  
     
    if( obj.isSummaryTable ){
        if( summaryTableJson!=null && summaryTableJson!="" ){
            summaryTableJson.html=obj.cellVal.parentNode.parentNode.parentNode.outerHTML;
            var panelId = (Ext.get('itemlistconfigsectionPanelGrid').dom.attributes["panelid"]!=null)?Ext.get('itemlistconfigsectionPanelGrid').dom.attributes["panelid"]:"";
            if(panelId !=""){
                var lineitemparentpanel = Ext.getCmp(panelId.value).ownerCt;
                if(lineitemparentpanel!=null){
                    if(lineitemparentpanel.items){
                        if(lineitemparentpanel.items.items){
                            if(lineitemparentpanel.items.items[0]){
                                lineitemparentpanel.items.items[0].summaryTableJson=summaryTableJson;
                                lineitemparentpanel.items.items[0].isSummaryTableApplied= true;
                            }
                        }
                    }
                }
            }
        }
    }
};

//function for header property
function getHeaderlineproperties() {
    Ext.bgcolor = Ext.getCmp('colorpickerbg');
    Ext.selectcombo = Ext.getCmp('alignselectcombo')
    var value = Ext.selectcombo.value;
    if (value == null) {
        value = "";
    }
    var str;
    if (Ext.bgcolor.isBackgroundColor == true) {               //value for background color
        var selectedcolor = Ext.bgcolor.value;
        if (selectedcolor) {
            str = "#" + selectedcolor;
        } else {
            str = "";
        }
    }
    var myEditorheader = Ext.getCmp('myDesignFieldEditor2');
    var htmlee = myEditorheader.getValue();
    //       htmlee=JSON.stringify(htmlee);
    this.cellproperties = {
        "alignment": value,
        "backgroundcolor": str,
        //        "htmlvalue": displayvalue,
        'changedlabel': htmlee.replace(/"/g, '\\"')
    };
    var returnConfig = Ext.JSON.encode(this.cellproperties);
    return returnConfig;
}


function createheaderwindow() {

    var headerbutton = new Ext.HeaderWin({});
    headerbutton.show();
    headerbutton.doLayout();
}

function rgbToHex(r, g, b) {
    return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
}

function getlineitemstable(PosX, PosY, html, designerPanel, propertyPanel) {
    var field = Ext.create(Ext.Component, {
        x: PosX,
        y: PosY,
        id: 'itemlistcontainer',
        draggable: false,
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
        //        cls: 'tpl-content',
        cls : 'sectionclass_field_without_border sectionclass_element_100',
        ctCls : 'sectionclass_field_container classzeropadding',
        style: {
            //            width:'100%',
            height: 'auto !important',
            //            borderColor: '#B5B8C8',
            //            borderStyle: 'solid',
            //            borderWidth: '1px',
            position: 'relative'
        },
        fieldDefaults: {
            anchor: '100%'
        },
        layout: {
            type: 'vbox',
            align: 'stretch'  // Child items are stretched to full width
        },
        html: html, // this string is compared. Search with 'customize line items
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function (eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if (component) {
                    if (Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    }
                    eventObject.stopPropagation();
                    Ext.get(selectedElement).removeCls("selected");
                    selectedElement = this.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    initTreePanel();
                }
            });
        }, //onRender
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            },
            removed: function () {
            // onDeleteUpdatePropertyPanel();
            //                                        propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
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
    /*field.on('drag',showAlignedComponent, field);
     field.on('dragend',removeAlignedLine, field);
     */
    return field;
}


function configureItemListcell(PosX, PosY, obj, html, designerPanel, propertyPanel, tablebordermode, tablecolor, height, width) {
    var jArr = eval(obj.lineitems);
    //        var tab1Row = document.createElement("ul");
    //        tab1Row.setAttribute("id", "itemlistconfig"+panelId);
    //        tab1Row.setAttribute('style', 'padding-left: inherit;width:100%');
    //                var width = (100 / jArr.length);
    var c, r, t;

    var tableConfig = document.createElement('table');
    r = tableConfig.insertRow(0);
    t = tableConfig.insertRow(1);
    tableConfig.setAttribute("class", "linetableproperties");
    tableConfig.setAttribute("id", "itemlistconfig" + designerPanel.id);
    tableConfig.setAttribute("cellSpacing", "0");
    if (tablebordermode) {
        this.tablebgcolor = tablecolor;
        this.borderstylemode = tablebordermode;
        var res = this.tablebgcolor.match(/#/g);
        if (res == null) {
            this.tablebgcolor = '#' + this.tablebgcolor;
        }
        tableConfig.setAttribute("borderstylemode", this.borderstylemode);
        tableConfig.setAttribute("tcolor", this.tablebgcolor);
        if (this.borderstylemode == "borderstylemode2") {
            tableConfig.setAttribute("border", '0px solid ');
            tableConfig.setAttribute('style', 'border-top: thin solid' + this.tablebgcolor + ';');
        } else if (this.borderstylemode == "borderstylemode3") {
            tableConfig.setAttribute("border", '0px solid');
            tableConfig.setAttribute('style', 'border-color:' + this.tablebgcolor + ';border-left: thin solid' + this.tablebgcolor + ';border-right: thin solid ' + this.tablebgcolor + ';border-top: thin solid' + this.tablebgcolor + ';');
        } else if (this.borderstylemode == "borderstylemode4") {
            tableConfig.setAttribute("border", '0px solid');
            tableConfig.setAttribute('style', 'border-color:none;border-left:none;border-right:none;border-top: none;');
        } else {
            tableConfig.setAttribute("border", '1px solid ');
            tableConfig.setAttribute('style', 'border-color:' + this.tablebgcolor + ';');
        }
    } else {
        tableConfig.setAttribute("border", '1px solid black ');
    }

    for (var cnt = 0; cnt < jArr.length; cnt++) {
        var obj = jArr[cnt];
        var newTH = document.createElement('th');
        if (jArr[cnt].headerproperty) {
            var headerpropertyparam = JSON.parse(jArr[cnt].headerproperty);
            newTH.setAttribute("bgColor", headerpropertyparam.backgroundcolor);
            newTH.setAttribute("align", headerpropertyparam.alignment);
            newTH.style.textAlign = headerpropertyparam.alignment;
            newTH.innerHTML = headerpropertyparam.changedlabel;
            newTH.setAttribute("label", headerpropertyparam.changedlabel);
        } else {
            newTH.innerHTML = obj.label;
            newTH.setAttribute("label", obj.label);
        }
        //            newTH.setAttribute("class", "tpl-content");
        var widthInPercent = (obj.colwidth - 2) + "%";// decreased 2% because while rendering header in html we used marging of 2%
        if (this.borderstylemode == "borderstylemode2") {
            newTH.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-bottom:thin solid ' + this.tablebgcolor + ';border-top:thin solid' + this.tablebgcolor + ';');
        } else if (this.borderstylemode == "borderstylemode3") {
            newTH.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:thin solid' + this.tablebgcolor + ';border-top:thin solid' + this.tablebgcolor + ';');
        } else if (this.borderstylemode == "borderstylemode4") {
            newTH.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left:none; border-right:none;border-bottom:thin solid' + this.tablebgcolor + ';border-top:thin solid' + this.tablebgcolor + ';');
        } else {
            if (this.tablebgcolor == undefined) {
                newTH.setAttribute('style', 'width: ' + widthInPercent);
            } else {
                newTH.setAttribute('style', 'width: ' + widthInPercent + ';border-color:' + this.tablebgcolor + ';');
            }
        }
        newTH.setAttribute("colwidth", obj.colwidth);
        newTH.setAttribute("coltotal", obj.coltotal);
        newTH.setAttribute("showtotal", obj.showtotal);
        newTH.setAttribute("totalname", obj.totalname);
        newTH.setAttribute("commaamount", obj.commaamount);
        newTH.setAttribute("headercurrency", obj.headercurrency);
        newTH.setAttribute("recordcurrency", obj.recordcurrency);
        newTH.setAttribute("decimalpoint", (obj.decimalpoint != undefined && obj.decimalpoint != null ? ((obj.decimalpoint != "undefined" && obj.decimalpoint != "null") ? obj.decimalpoint : defaultdecimalvalue) : defaultdecimalvalue));
        newTH.setAttribute("seq", obj.seq);
        newTH.setAttribute("xtype", obj.xtype);
        newTH.setAttribute("headerproperty", obj.headerproperty);
        newTH.setAttribute("fieldid", obj.fieldid);
        newTH.setAttribute("tablecolor", this.tablebgcolor);
        newTH.setAttribute("tableborderstyle", this.borderstylemode);
        newTH.setAttribute("basequantitywithuom", (obj.basequantitywithuom != undefined ? (obj.basequantitywithuom != "undefined" ? obj.decimalpoint : false) : false));
        newTH.value = obj.fieldid;
        newTH.cellIndex = obj.seq;
        r.appendChild(newTH);
        //             var tabletd=t.insertCell(cnt);;
        var tabletd = document.createElement('td');
        tabletd.cellIndex = newTH.cellIndex;
        tabletd.setAttribute("cellIndex", newTH.cellIndex);
        tabletd.innerHTML = "&nbsp;";
        if (this.borderstylemode == "borderstylemode2") {
            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width: thin;border-bottom:thin solid ' + this.tablebgcolor + ';border-top:thin solid' + this.tablebgcolor + ';');
        } else if (this.borderstylemode == "borderstylemode3") {
            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:thin solid ' + this.tablebgcolor + ';border-top:thin solid ' + this.tablebgcolor + ';');
        } else if (this.borderstylemode == "borderstylemode4") {
            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:none;border-top:none;');
        } else {
            if (this.tablebgcolor == undefined) {
                tabletd.setAttribute('style', 'width: ' + widthInPercent);
            } else {
                tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-color:' + this.tablebgcolor + ';');
            }
        }
        tabletd.setAttribute("colwidth", obj.colwidth);
        tabletd.setAttribute("tablecolor", this.tablebgcolor);
        tabletd.setAttribute("tableborderstyle", this.borderstylemode);
        t.appendChild(tabletd);
        tableConfig.appendChild(r);
    }
    tableConfig.appendChild(r);
    tableConfig.appendChild(t);
    Ext.tableinsert = true;
    var html = tableConfig.outerHTML;
    var field = Ext.create(Ext.Component, {
        x: PosX,
        y: PosY,
        draggable: true,
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
        id: 'itemlistcontainer' + designerPanel.id,
        containerId: designerPanel.id,
        width: (width != null || width != undefined) ? width : '95%',
        height: (height != null || height != undefined) ? height : 'auto !important',
        fieldTypeId: Ext.fieldID.insertTable,
        resizable: true,
        cls: 'tpl-content',
        style: {
            //            width:(width!=null||width!=undefined)? width:'95%',
            //            height:(height!=null||height!=undefined)?height:'auto !important',
            borderColor: '#B5B8C8',
            borderStyle: 'solid',
            borderWidth: '1px',
            position: 'absolute'
        },
        fieldDefaults: {
            anchor: '100%'
        },
        layout: {
            type: 'vbox',
            align: 'stretch'  // Child items are stretched to full width
        },
        html: html, // this string is compared. Search with 'customize line items
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            },
            removed: function () {
                onDeleteUpdatePropertyPanel();
            //                                        propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            },
            afterrender: function () {
                addPositionObjectInCollection(this);
                this.el.on('click', function (eventObject, target, arg) {
                    var component = designerPanel.queryById(this.id)
                    if (component && Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                        eventObject.stopPropagation();
                        getPropertyPanel(component, designerPanel, propertyPanel);
                    }
                });
            }
        }
    });
    field.on('drag', showAlignedComponent, field);
    field.on('dragend', removeAlignedLine, field);
    return field;
}

function IsJsonString(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}

function renderItemsOnDesignPanel(arr, designerPanel, propertyPanel) {
    for (var cnt = 0; cnt < arr.length; cnt++) {
        var obj = arr[cnt];
        var field;
        if (obj.fieldTypeId == Ext.fieldID.insertTable) {
            var tablebordermode = arr[cnt].tablebordermode;
            var tablecolor = arr[cnt].tablecolor;
            //                field = configureItemListcell(obj, this.designerPanel, this.propertyPanel)
            field = configureItemListcell(obj.x, obj.y, obj, obj.labelhtml, designerPanel, propertyPanel, tablebordermode, tablecolor, obj.height, obj.width);
        } else if (obj.fieldTypeId == Ext.fieldID.insertImage) {
            field = createExtImgComponent(designerPanel, propertyPanel, obj.fieldTypeId, obj.src, obj.x, obj.y, obj);
        } else if (obj.fieldTypeId == Ext.fieldID.insertHLine) {
            field = createHoLineComponent(designerPanel, obj.fieldTypeId, propertyPanel, obj.x, obj.y, obj.width, obj.widthnew);
        } else if (obj.fieldTypeId == Ext.fieldID.insertDrawBox) {
            field = createDrawComponent(designerPanel, propertyPanel, obj.fieldTypeId, obj.labelhtml, obj.x, obj.y, obj.width, obj.height, obj.backgroundColor);
        } else if (obj.fieldTypeId == Ext.fieldID.insertGlobalTable) {
            var borderedgetype = obj.borderedgetype!=null?obj.borderedgetype:"1";
            var rowspacing = obj.rowspacing!=null?obj.rowspacing:"5";
            var columnspacing = obj.columnspacing!=null?obj.columnspacing:"5";
            var fieldAlignment = obj.fieldAlignment!=null?obj.fieldAlignment:"1"; 
            var tableHeader=obj.tableHeader!=null?obj.tableHeader:false;
            field = createGlobalTable(obj.x, obj.y, obj.labelhtml, designerPanel, propertyPanel, obj.width, obj.height, obj.fixedrowvalue, obj.fieldTypeId,borderedgetype,rowspacing,columnspacing,fieldAlignment,obj,tableHeader);
        } else if (obj.fieldTypeId == Ext.fieldID.insertRowPanel) {
            field = createGlobalTable(obj.x, obj.y, obj.labelhtml, designerPanel, propertyPanel, obj.width, obj.height, obj.fixedrowvalue, obj.fieldTypeId);
        } else {
            var labelHTML = obj.fieldTypeId != Ext.fieldID.insertField ? obj.labelhtml : "<span attribute='{PLACEHOLDER:" + obj.fieldid + "}'>" + obj.labelhtml + "</span>";
            field = createExtComponent(designerPanel, undefined, obj.fieldTypeId, labelHTML, obj.x, obj.y, {
                width: obj.width,
                height: obj.height
            }, obj.selectfieldbordercolor);
        }
        designerPanel.items.add(field);
    }
    designerPanel.doLayout();
}

function colorpanel() {
    var headerbutton = new Ext.Window({
        closable: true,
        title: 'Table Border color',
        id: 'bordercolorproperty',
        autoWidth: true,
        autoHeight: true,
        scope: this,
        modal:true,              //ERP-19208
        items: [
        this.smallcolorpanel = Ext.create('Ext.picker.Color', {
        value: (Ext.globaltablecolor == undefined || Ext.globaltablecolor == "") ? '000000' : Ext.globaltablecolor, // initial selected color
                itemId: 'bordercolor',           
            //                    id:'colorpickerbg',
                isBackgroundColor: true,
                listeners: {
                select: function (picker, selColor) {
                        Ext.globaltablecolor = selColor;
                    getEXTComponent('idSelectedTableBorder').body.setStyle('background-color', '#'+selColor);
                        headerbutton.close();
                    }
                }
        })]
            })
    headerbutton.show();
    headerbutton.doLayout();

}
function getGroupingFieldPropertyPanel(event,div,type) {
    event.stopPropagation();
    createGroupingFieldPropertyPanel(div,type);
    setGroupingFieldPropertyPanel(div)
}
function createGroupingFieldPropertyPanel(div,type) {
    var textColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color",
        width: 105

    });

//    var textColorBtn = Ext.create('Ext.Button', {
//        id: 'idTextColorBtn',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            //                            xtype: 'colormenu',
//            value: div.style.color ? rgbToHex(div.style.color,false) : '000000',
//            id: 'colorpicker',
//            handler: function (obj, rgb) {
//
//                //this.selectedcolor='FFFFFF';
//                Ext.colorpicker = Ext.getCmp('colorpicker');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', selectednewcolor);
//                updateGroupingFieldProperty(div);
//
//            }// handler
//        }), // menu
//        text: ''
//    });
        var textcolorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: div.style.color ? rgbToHex(div.style.color,false) : '#000000',
        id: 'colorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', newVal);
                updateGroupingFieldProperty(div);
            }

        }
    });
    var selectedTextColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedTextPanel',
        height: 20,
        width: 25,
        border: false,
        margin :'1 0 0 0'
    });
    
    var textColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 220,
        padding:'2 2 2 2',
        border: false,
        items: [textColorLabel, selectedTextColorPanel, textcolorPicker]
    });
    
    Ext.define('GroupingField', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'id', type: 'string'},
            {name: 'value',  type: 'string'},
        ]
    });

    this.groupingFieldStore = Ext.create('Ext.data.Store', {
        model: 'GroupingField',
        proxy: {
            type: 'ajax',
            url : 'CustomDesign/getGroupingFields.do',
            extraParams : {
              moduleid : _CustomDesign_moduleId,
              companyid : companyid
            },
            reader: {
                type: 'json',
                root: 'data'
            }
        },
        listeners: {
            load: function(store, operation, options){
                this.insert(0, {id:"None", value:"None", key:"None"});
                if(_CustomDesign_moduleId == 60 || _CustomDesign_moduleId == 61){
                    this.insert(1, {id:"Billing Address", value:"Billing Address", key:"Billing Address"});
                    this.insert(2, {id:"Shipping Address", value:"Shipping Address", key:"Shipping Address"});
                } else{
                    this.insert(1, {id:"Product Category", value:"Product Category", key:"Product Category"});
                    this.insert(2, {id:"Product ID", value:"Product ID", key:"Product ID"});
                }
            }
        }
    });
    var valueStoreAmount = Ext.create("Ext.data.Store", {
        fields: ["id", "value"],
        data: [],
        listeners: {
            load: function(store, operation, options){
                this.insert(0, {id:"None", value:"None", key:"None"});
                for(var ind = 0; ind < documentLineColumns.length; ind++){
                    var lineLevelFields = documentLineColumns[ind];
                    var fieldName = lineLevelFields.columnname;
                    if(lineLevelFields.xtype == "2" && (fieldName == "Quantity" || fieldName == "Sub Total")){
                        this.insert(this.data.length, {id:fieldName, value:fieldName, key:lineLevelFields.fieldid});
                    }
                    if(lineLevelFields.xtype == "2" && (fieldName == "Amount")){
                        this.insert(this.data.length, {id:fieldName, value:fieldName, key:lineLevelFields.fieldid});
                    }
                }
            }
        }
    });
    var xtype = "";
    if ( div.attributes.getNamedItem("xtype") != null ) {
        xtype = div.attributes.getNamedItem("xtype").value;
    }
    var isValueFieldDisabled = true;
    if ( type === 0 ) {
        if ( isGroupingApplied ) {
            if ( div.attributes.getNamedItem("label") != null ) {
                isValueFieldDisabled = false;
            }
        } else {
            isValueFieldDisabled = false;
        }
    } else {
        if ( xtype === "2" ) { // checking number type fields like amount, sub-total etc.
//            if ( isFormattingApplied ) {
//                if ( div.attributes.getNamedItem("label") != null ) {
                    isValueFieldDisabled = false;
//                }
//            } else {
//                isValueFieldDisabled = false;
//            }
        } 
    }
    var valueField = {
        xtype: 'combo',
        fieldLabel: 'Value',
        id: 'idValueField',
        store: type===0?this.groupingFieldStore:valueStoreAmount,
        displayField: 'value',
        valueField: 'id',
        padding: '2 2 2 2',
        listWidth: 200,
        disabled:isValueFieldDisabled,
        value: div.attributes.getNamedItem("label") != null?div.attributes.getNamedItem("label").value:"",
        width: 210,
        listeners: {
            'select': function(e) {
                var component = getEXTComponent("idValueField");
                var id = component.getValue();
                var key = component.getStore().getById(id).raw.key;
                var value = component.getDisplayValue();
                if ( value === "None") {
                    div.removeAttribute("label");
                    div.removeAttribute("columnname");
                    div.removeAttribute("key");
                    if ( type === 0 ) {
                        isGroupingApplied = false;
                        div.innerHTML = "Grouping Field";
                    } else {
                        isFormattingApplied = false;
                        div.innerHTML = "Formatting Field";
                    }
                } else {
                    div.innerHTML = value;
                    div.setAttribute("label", value);
                    div.setAttribute("columnname", value);
                    div.setAttribute("key", key);
                    div.setAttribute("putin", key);
                    if ( type === 0 ) {
                        isGroupingApplied = true;
                    } else {
                        isFormattingApplied = true;
                    }
                }
                var tr = div.parentNode.parentNode;
                var ele = Ext.getCmp("itemlistcontainer");
                if ( ele ) {
                    if ( type === 0 ) {
                        if ( value === "None") { 
                            ele.isGroupingApplied = false;
                        } else {
                            ele.isGroupingApplied = true;
                        }
                        ele.groupingRowHTMl = tr.outerHTML;
                    } else {
                        if ( value === "None") { 
                            ele.isFormattingApplied = false;
                        } else {
                            ele.isFormattingApplied = true;
                        }
                        ele.groupingRowAfterHTML = tr.outerHTML;
                    }
                }
                
                
            }
        }

    };

   
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
        displayField: 'textline',
        valueField: 'id',
        padding: '2 2 2 2',
        listWidth: 200,
        width: 210,
        listeners: {
            'select': function(e) {
                updateGroupingFieldProperty(div);
            }
        }

    };


    
    var boldText = {
        xtype: 'checkbox',
        id: 'idBoldText',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        listeners: {
            change: function (e) {
                updateGroupingFieldProperty(div);
            }
        }
    };
    var italicText = {
        xtype: 'checkbox',
        id: 'idItalicText',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                updateGroupingFieldProperty(div);
            }
        }
    };
    var fontSize = {
        xtype: 'numberfield',
        fieldLabel: 'Font Size',
        id: 'idFontSize',
        width: 210,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        listeners: {
            'change': function () {
                updateGroupingFieldProperty(div);
            }
        }
    };
    
    var textAlignmentStore = Ext.create("Ext.data.Store", {
        fields: ["id", "textalign"],
        data: [
        {
            id: "left",
            textalign: "Left"
        }, {
            id: "center",
            textalign: "Center"

        }, {
            id: "right",
            textalign: "Right"
        }
        ]
    });
    var textalign = {
        xtype: 'combo',
        fieldLabel: 'Text Alignment',
        id: 'idTextAlign',
        store: textAlignmentStore,
        padding:'2 2 2 2',
        displayField: 'textalign',
        valueField: 'id',
        width: 210,
        listeners: {
            'select': function (e) {
                updateGroupingFieldProperty(div);
            }
        }
    };
    
    
    var width = {
        xtype: 'numberfield',
        fieldLabel: 'Width',
        id: 'idWidth',
        padding:'2 2 2 2',
        width: 210,
        maxValue:100,
        minValue:0,
        minLength: 1,
        scope:this,
        listeners: {
            'change': function (e) {
                if(!e.hidden){
                    if ( e.getValue() > 100 ) {
                        e.setValue(100);
                        WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                    }
                    updateGroupingFieldProperty(div);
                }
            }
        }
    };
    
    var verticalAlign = {
        xtype:'combo',
        fieldLabel: 'Vertical Alignments',
        store: Ext.ValignStore,
        id: 'Valignselectcomboid',
        displayField: "name",
        valueField: 'position',
        labelWidth:100,
        queryMode: 'local',
        hidden:true,
        width:210,
        //        value:(config.cellVal.style.verticalAlign)?config.cellVal.style.verticalAlign:((config.cellVal.nodeName == "TH")?"middle":"top"),
        listeners:{
            scope:this,
            change: function () {
                updateGroupingFieldProperty(div);
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
                updateGroupingFieldProperty(div);
            }
        }
    };
    var decimalPrecision = {
        xtype: 'numberfield',
        fieldLabel: 'Decimal Precision',
        id: 'iddecimalPrecision',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        value: div.attributes.getNamedItem("decimalPrecision") != null?div.attributes.getNamedItem("decimalPrecision").value:"2",
        hidden:type===0?true:false,
        listeners: {
            change: function (field) {
                var value = Ext.getCmp("iddecimalPrecision").getValue();
                div.setAttribute("decimalPrecision", value);
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
                updateGroupingFieldProperty(div);
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
                updateGroupingFieldProperty(div);
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
                updateGroupingFieldProperty(div);
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
    
    var fontSettings = {
        xtype:'fieldset',
        title:'Font Settings',
        autoHeight:true,
        id:'idFontSettingsfieldset',
        width:270,
        items:[
        boldText,italicText,fontSize,textLine,textColorPanel,verticalAlign,valueField,decimalPrecision
        ]
    };
    var spaceSettings = {
        xtype:'fieldset',
        title:'Alignment Settings',
        autoHeight:true,
        id:'idSpaceSettingsfieldset',
        width:270,
        items:[
        width,textalign
        ]
    };
    
    var fieldPropertyPanel =  Ext.create("Ext.Panel", {
        width: 300,
        div:div,
        autoHeight:true,
        border: false,
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [
        spaceSettings,fontSettings,marginsettings
        ]
    });
    
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
    if (propertyPanel){
        propertyPanelRoot.remove(propertyPanel.id);
    }
    propertyPanel = fieldPropertyPanel;
    propertyPanelRoot.add(propertyPanel);
    if ( type === 0) {
        Ext.getCmp("idEastPanel").setTitle("Grouping Property Panel");
    } else {
        Ext.getCmp("idEastPanel").setTitle("Formatting Property Panel");
    }
    Ext.getCmp("idEastPanel").add(propertyPanelRoot);
    Ext.getCmp("idEastPanel").doLayout();
}

function setGroupingFieldPropertyPanel(div) {
    var parent = div.parentNode;
    var fieldValue = div.innerHTML;
    var width = div.style.width?div.style.width:100;
    var textAlign = div.style.textAlign?div.style.textAlign:"left";
    var fontSize = div.style.fontSize?div.style.fontSize:'';
    var fontWeight = div.style.fontWeight?div.style.fontWeight == "bold":false;
    var fontStyle = div.style.fontStyle?div.style.fontStyle == "italic":false;
    var textLine = div.style.textDecoration?div.style.textDecoration:"none";
    var textColor = div.style.color?div.style.color:"#000000";
    var marginTop = div.style.marginTop?div.style.marginTop:"0px";
    var marginBottom = div.style.marginBottom?div.style.marginBottom:"0px";
    var marginLeft = div.style.marginLeft?div.style.marginLeft:"0px";
    var marginRight = div.style.marginRight?div.style.marginRight:"0px";
    var verticalAlign = parent.style.verticalAlign?parent.style.verticalAlign:"middle";

    if ( Ext.getCmp("Valignselectcomboid") ) {
        Ext.getCmp("Valignselectcomboid").setValue(verticalAlign);
    }
    if ( Ext.getCmp("idTextLine") ) {
        Ext.getCmp("idTextLine").setValue(textLine);
    }
    if ( Ext.getCmp("idtopmargin") ) {
        Ext.getCmp("idtopmargin").setValue(marginTop);
    }
    if ( Ext.getCmp("idbottommargin") ) {
        Ext.getCmp("idbottommargin").setValue(marginBottom);
    }
    if ( Ext.getCmp("idleftmargin") ) {
        Ext.getCmp("idleftmargin").setValue(marginLeft);
    }
    if ( Ext.getCmp("idrightmargin") ) {
        Ext.getCmp("idrightmargin").setValue(marginRight);
    }
    if ( Ext.getCmp("colorpicker") ) {
        Ext.getCmp("colorpicker").value = textColor;
    }
    if ( Ext.getCmp("idSelectedTextPanel") ) {
        Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', textColor);
    }
    if ( Ext.getCmp("idItalicText") ) {
        Ext.getCmp("idItalicText").setValue(fontStyle);
    }
    if ( Ext.getCmp("idBoldText") ) {
        Ext.getCmp("idBoldText").setValue(fontWeight);
    }
    if ( Ext.getCmp("idFontSize")) {
        Ext.getCmp("idFontSize").setValue(fontSize);
    }
    if (Ext.getCmp("fields_Alignment")) {
        Ext.getCmp("fields_Alignment").setValue(fieldAlign);
    }
    if ( Ext.getCmp("idTextAlign") ) {
        Ext.getCmp("idTextAlign").setValue(textAlign);
    }
    if ( Ext.getCmp("idWidth") ) {
        Ext.getCmp("idWidth").setValue(width);
    }
}

function updateGroupingFieldProperty(div) {
    var width, imagewidth, height;
    if ( Ext.getCmp("idWidth") ) {
        width = Ext.getCmp("idWidth").getValue();
        div.style.width = width + "%";
    }
    var textAlign;
    if ( Ext.getCmp("idTextAlign") ) {
        textAlign = Ext.getCmp("idTextAlign").getValue();
        div.style.textAlign = textAlign;
        if ( textAlign == "left" ) {
            div.style.marginLeft="0px";
            div.style.marginRight="auto";
        } else if ( textAlign == "center" ) {
            div.style.marginLeft="auto";
            div.style.marginRight="auto";
        } else if ( textAlign == "right" ) {
            div.style.marginLeft="auto";
            div.style.marginRight="0px";
        } 
    }
    var fontSize;
    if ( Ext.getCmp("idFontSize")) {
        if ( Ext.getCmp("idFontSize").getValue() > 0 ) {
            fontSize =Ext.getCmp("idFontSize").getValue();
            div.style.fontSize=fontSize + "px";
        } else {
            div.style.fontSize="";
        }
    }
    var fontWeight;
    if ( Ext.getCmp("idBoldText") ) {
        fontWeight = Ext.getCmp("idBoldText").getValue();
        if (fontWeight) {
            div.style.fontWeight="bold";
        } else {
            div.style.fontWeight="normal";
        } 
    }
    var fontStyle;
    if ( Ext.getCmp("idItalicText") ) {
        fontStyle = Ext.getCmp("idItalicText").getValue();
        if (fontStyle) {
            div.style.fontStyle="italic";
        } else {
            div.style.fontStyle="normal";
        } 
    }
    
    var textLine;
    if ( Ext.getCmp("idTextLine") ) {
        textLine = Ext.getCmp("idTextLine").getValue();
        div.style.textDecoration = textLine;
    }
    
    var  textColor;
    if ( Ext.getCmp("colorpicker") ) {
        textColor = Ext.getCmp("colorpicker").value;
        div.style.color = textColor;
    }
    var marginTop;
    if ( Ext.getCmp("idtopmargin")) {
        marginTop = Ext.getCmp("idtopmargin").value;
        div.style.marginTop = marginTop + "px";
    }
    var marginBottom;
    if ( Ext.getCmp("idbottommargin")) {
        marginBottom = Ext.getCmp("idbottommargin").value;
        div.style.marginBottom = marginBottom + "px";
    }
    var marginLeft;
    if ( Ext.getCmp("idleftmargin")) {
        marginLeft = Ext.getCmp("idleftmargin").value;
        div.style.marginLeft = marginLeft + "px";
    }
    var marginRight;
    if ( Ext.getCmp("idrightmargin")) {
        marginRight = Ext.getCmp("idrightmargin").value;
        div.style.marginRight = marginRight + "px";
    }
    var verticalAlign;
    if ( Ext.getCmp("Valignselectcomboid")) {
        verticalAlign = Ext.getCmp("Valignselectcomboid").value;
        var parent = div.parentNode;
        parent.style.verticalAlign = verticalAlign;
    }
}

function getPropertyPanelForLineItemFields(event,div,isGlobalTable, isImage, isPrePost) {
    event.stopPropagation();
    createPropertyPanelForLineItemFields(div,isGlobalTable, isImage, isPrePost);
    setPropertyPanelForLineItemFields(div,isGlobalTable, isImage, isPrePost)
}
function createPropertyPanelForLineItemFields (div,isGlobalTable, isImage, isPrePost) {
   
   
    var isFormula = div.attributes.isformula ? (div.attributes.isformula.value == "true" ? true : false):false;
    var textColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color",
        width: 105

    });

//    var textColorBtn = Ext.create('Ext.Button', {
//        id: 'idTextColorBtn',
//        menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            //                            xtype: 'colormenu',
//            value: div.style.color ? rgbToHex(div.style.color,false) : '000000',
//            id: 'colorpicker',
//            handler: function (obj, rgb) {
//
//                //this.selectedcolor='FFFFFF';
//                Ext.colorpicker = Ext.getCmp('colorpicker');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', selectednewcolor);
//                updatePropertyForLineItemFields(div);
//
//            }// handler
//        }), // menu
//        text: ''
//    });
    var textColorpicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: div.style.color ? rgbToHex(div.style.color,false) : '#000000',
        id: 'colorpicker',
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', newVal);
                updatePropertyForLineItemFields(div);
            }
        }
    });
    var selectedTextColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedTextPanel',
        height: 20,
        width: 25,
        border: false,
        margin :'1 0 0 0'
    });

    var textColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 220,
        padding:'2 2 2 2',
        border: false,
        items: [textColorLabel,selectedTextColorPanel,textColorpicker]
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
        }, {
            id: "horizotal",
            textline: "Horizontal Line"
        }
        ]
    });
    
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
                updatePropertyForLineItemFields(div);
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
                updatePropertyForLineItemFields(div);
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
                updatePropertyForLineItemFields(div);
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
                updatePropertyForLineItemFields(div);
            }
        }
    });
    var borderColor='#000000';
    if(div.style.borderLeftColor){
        borderColor = div.style.borderLeftColor?rgbToHex(div.style.borderLeftColor, false):'#000000';
    }else if(div.style.borderRightColor){
        borderColor = div.style.borderRightColor?rgbToHex(div.style.borderRightColor, false):'#000000';
    }else if(div.style.borderTopColor){
        borderColor = div.style.borderTopColor?rgbToHex(div.style.borderTopColor, false):'#000000';
    }else if(div.style.borderBottomColor){
        borderColor = div.style.borderBottomColor?rgbToHex(div.style.borderBottomColor, false):'#000000';
    }
    var borderColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        padding:'2 2 2 0',
        border: false,
        //            hidden:(config.cellVal.nodeName == "TH")?true:false,
        items: [
        this.ColorLabel = Ext.create("Ext.form.Label", {
        text: "Border Color",
        width: 105
        }),
        this.selectedBorderColorPanel = Ext.create("Ext.Panel", {
        id: 'idBorderPanel',
        height: 20,
        bodyStyle:'background-color:#000000; margin-top : 1px;',
        width: 25,
        border: false
        }),
//        this.textColorBtn = Ext.create('Ext.Button', {
//            id: 'idBorderBtn',
//            menu: this.colortypemenu = Ext.create('Ext.menu.ColorPicker', {
//            value:borderColor,
//            id: 'idborderpicker',
//                scope:this,
//            handler: function (obj, rgb) {
//                Ext.colorpicker = getEXTComponent('idborderpicker');
//                var selectednewcolor = "#" + rgb.toString();
//                Ext.colorpicker.value = selectednewcolor;
//                getEXTComponent('idBorderPanel').body.setStyle('background-color', selectednewcolor);
//                updatePropertyForLineItemFields(div);
//            }// handler
//            }), // menu
//        text: ''
//        })
            this.borderColorPicker = Ext.create('Ext.ux.ColorPicker', {
                luminanceImg: '../../images/luminance.png',
                spectrumImg: '../../images/spectrum.png',
                value:borderColor,
                id: 'idborderpicker',
                listeners: {
                    scope:this,
                    change: function (thiz, newVal) {                        
                        getEXTComponent('idBorderPanel').body.setStyle('background-color', newVal);                            
                        updatePropertyForLineItemFields(div);
                    }
                }
            })
        ]
    });  
   
    var separatorCombo = {
        xtype: 'combo',
        fieldLabel: 'Value Separator',
        id: 'idvalSeparator',
        store: separatorStore,
        displayField: 'textline',
        valueField: 'id',
        padding: '2 2 2 2',
        listWidth: 200,
        width: 210,
        value:div.attributes.getNamedItem("valueSeparator") != null?div.attributes.getNamedItem("valueSeparator").value:"linebreak",
        listeners: {
            'select': function(e) {
                //                updatePropertyForLineItemFields(div);
                var separator = Ext.getCmp("idvalSeparator").getValue();
                div.setAttribute("valueSeparator", separator);
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
     var dimensiondisabled = true;
    if(div.attributes.getNamedItem("attribute") != null){
        var placeholder = div.attributes.getNamedItem("attribute").value;
        var arr = placeholder.match(/\{PLACEHOLDER:(.*?)}/g);
        if (arr) {
            var arraylength = arr.length;
            for (var i = 0; i <= arraylength - 1; i++) {
                var matches = arr[i].replace(/\{|\}/gi, '').split(":");
                var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                if(rec && rec.data && rec.data.xtype == "4" ){
                    dimensiondisabled = false;
                }
            }
        }
    }else if(div.attributes !=undefined && div.attributes.xtype != undefined && div.attributes.xtype.value=="4" ){
        dimensiondisabled=false;
    }
    
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
        queryMode: 'local',
        fieldLabel:  'Currency',
        value:div.attributes.getNamedItem("specificreccurrency") != null?div.attributes.getNamedItem("specificreccurrency").value:"none",
        //value:obj.specificreccurrency?obj.specificreccurrency:"none",
       // disabled: (obj.selectfield == 'GlobalSpecificCurrencyExchangeRate' || obj.selectfield == 'GlobalSpecificCurrencyAmount' || obj.selectfield == 'GlobalSpecificCurrencyDiscount' || obj.selectfield == 'GlobalSpecificCurrencySubTotal' || obj.selectfield == 'GlobalSpecificCurrencySubTotalWithDicount' || obj.selectfield == 'GlobalSpecificCurrencyTaxAmount' || obj.selectfield == 'GlobalSpecificCurrencyUnitPrice') ? false : true,
        disabled: div.attributes.getNamedItem("columnname") != null ? ((div.attributes.getNamedItem("columnname").value == 'Specific Currency Amount' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Discount' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Exchange Rate' || div.attributes.getNamedItem("columnname").value == 'Specific Currency SubTotal' || div.attributes.getNamedItem("columnname").value == 'Specific Currency SubTotal-Discount' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Tax Amount' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Unit Price' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Term Amount') && isGlobalTable == true ) ? false : true : true,
        hidden: div.attributes.getNamedItem("columnname") != null ? ((div.attributes.getNamedItem("columnname").value == 'Specific Currency Amount' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Discount' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Exchange Rate' || div.attributes.getNamedItem("columnname").value == 'Specific Currency SubTotal' || div.attributes.getNamedItem("columnname").value == 'Specific Currency SubTotal-Discount' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Tax Amount' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Unit Price' || div.attributes.getNamedItem("columnname").value == 'Specific Currency Term Amount') && isGlobalTable == true ) ? false : true : true,
        listeners:{
            'select': function(e) {
                //                updatePropertyForLineItemFields(div);
                var reccurrency = Ext.getCmp("recordCurrencyId").getValue();
                div.setAttribute("specificreccurrency", reccurrency);
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
        hidden: div.getAttribute("xtype") != '2',
        checked: (div.getAttribute("showzerovalueasblank") == 'true') ? true : false,
        listeners: {
            'change': function (e) {
                var showzerovalueasblank = Ext.getCmp("showZeroValueAsBlankId").getValue();
                div.setAttribute("showzerovalueasblank", showzerovalueasblank);
            }
        }
    };
    
     var dimensionsettingCombo = {
        xtype: 'combo',
        fieldLabel: 'Dimension Value',
        id: 'iddimensionselectField',
        store: dimensionStore,
        displayField: 'text',
        valueField: 'id',
        listWidth: 200,
        padding:'2 2 2 2',
        width: 210,
        disabled: dimensiondisabled,
        value:div.attributes.getNamedItem("dimensionValue") != null?div.attributes.getNamedItem("dimensionValue").value:"2",
        listeners: {
            'select': function(e) {
                //                updatePropertyForLineItemFields(div);
                var separator = Ext.getCmp("iddimensionselectField").getValue();
                div.setAttribute("dimensionValue", separator);
            }
        }
    }
    var textLine = {
        xtype: 'combo',
        fieldLabel: 'Text Line',
        id: 'idTextLine',
        store: textLineStore,
        displayField: 'textline',
        valueField: 'id',
        padding: '2 2 2 2',
        listWidth: 200,
        width: 210,
        listeners: {
            'select': function(e) {
                updatePropertyForLineItemFields(div);
            }
        }
    };


    var preText = {
        xtype: 'checkbox',
        id: 'idpreText',
        fieldLabel: 'Apply Pre-text',
        padding: '2 2 2 2',
        listeners: {
            change: function (e) {
                if (e.getValue()) {
                    addPrePostText(div,1); // 1 for pretext
                } else {
                    removePrePostText(div,1); 
                }
            }
        }
    };
    var postText = {
        xtype: 'checkbox',
        id: 'idpostText',
        fieldLabel: 'Apply Post-text',
        padding:'2 2 2 2',
        listeners: {
            change: function (e) {
                if (e.getValue()) {
                    addPrePostText(div,2); // 2 for pretext
                } else {
                    removePrePostText(div,2); 
                }
            }
        }
    };
    var boldText = {
        xtype: 'checkbox',
        id: 'idBoldText',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        listeners: {
            change: function (e) {
                updatePropertyForLineItemFields(div);
            }
        }
    };
    var italicText = {
        xtype: 'checkbox',
        id: 'idItalicText',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                updatePropertyForLineItemFields(div);
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
        emptytext:'sans-serif',
        labelWidth:100,
        width:210,
        listeners: {
            'select': function (e) {
                updatePropertyForLineItemFields(div);
            }
        }
    };
    var fontSize = {
        xtype: 'numberfield',
        fieldLabel: 'Font Size',
        id: 'idFontSize',
        width: 210,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        listeners: {
            'change': function () {
                updatePropertyForLineItemFields(div);
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
                id: "inline-block",
                type: "Block"
            }, {
                id: "inline-table",
                type: "Inline"

            }
            ]
        }),
        displayField: 'type',
        valueField: 'id',
        width: 210,
        labelWidth: 100,
        listeners: {
            select: function (field) {
                updatePropertyForLineItemFields(div);
            }
        }
    };
    var textAlignmentStore = Ext.create("Ext.data.Store", {
        fields: ["id", "textalign"],
        data: [
        {
            id: "left",
            textalign: "Left"
        }, {
            id: "center",
            textalign: "Center"

        }, {
            id: "right",
            textalign: "Right"
        }
        ]
    });
    var textalign = {
        xtype: 'combo',
        fieldLabel: 'Text Alignment',
        id: 'idTextAlign',
        store: textAlignmentStore,
        padding:'2 2 2 2',
        displayField: 'textalign',
        valueField: 'id',
        width: 210,
        listeners: {
            'select': function (e) {
                updatePropertyForLineItemFields(div);
            }
        }
    };
    var fieldValue = {
        xtype: 'textfield',
        fieldLabel: 'Field Value',
        id: 'idFieldValue',
        padding:'2 2 2 2',
        width:210,
        labelWidth:100,
        readOnly: true,
        listeners: {
            'change': function (e) {
                updatePropertyForLineItemFields(div);
            }
        }
    };
    var defaultField = {
        xtype: 'textfield',
        fieldLabel: 'Default Value',
        id: 'iddefaultFieldValue',
        padding:'2 2 2 2',
        width:210,
        labelWidth:100,
        hidden:!isGlobalTable,
        value:isFormula?"0":"",
        disabled:isFormula?true:false,
        listeners: {
            'change': function (e) {
                updatePropertyForLineItemFields(div);
            }
        }
    };
    var width = {
        xtype: 'numberfield',
        fieldLabel: 'Width',
        id: 'idWidth',
        padding:'2 2 2 2',
        width: 210,
        maxValue:100,
        minValue:0,
        minLength: 1,
        hidden : isImage?true:false,
        scope:this,
        listeners: {
            'change': function (e) {
                if(!e.hidden){
                    if ( e.getValue() > 100 ) {
                        e.setValue(100);
                        WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                    }
                    updatePropertyForLineItemFields(div);
                }
            }
        }
    };
    var selectField = {
        xtype: 'combo',
        fieldLabel: 'Select Field',
        store: defaultFieldGlobalStore,
        id: 'idcustomdesignerfieldselectcombo',
        displayField: 'label',
        valueField: 'id',
        queryMode: 'local',
        padding:'2 2 2 2',
        width: 210,
        div:div,
        hidden:isGlobalTable?isFormula?true:false:true,
        blankText: 'Select a field',
        value : div.innerHTML.replace(/#/g,""),
        listeners: {
            scope: this,
            'select': function (combo, selection) {
                if(selection[0].data.xtype!="NAN"){
                    div.innerHTML = "#" + selection[0].data.label + "#";
                    div.setAttribute("attribute","{PLACEHOLDER:"+ selection[0].data.id+"}");
                    div.setAttribute("defaultValue", "");
                }
            }
        }
    };
    
    // for formula builder - to show formula
    var formula = "";
    if(isFormula){
        var labelHtml = "";
        if(isGlobalTable){
            labelHtml = div.attributes.attribute ? div.attributes.attribute.value : "";
            labelHtml = labelHtml.split(/\{PLACEHOLDER\:|}/)[1];
        } else{
            labelHtml = div.attributes.formula ? div.attributes.formula.value : "";
        }
        formula = labelHtml;
    }
    
    var formulaTitle = {
        id: 'selectedValue',
        xtype: 'label',
        fieldLabel: 'Selected',
        hidden: isFormula?false:true,
        text: 'Formula:'
    };
    
    var formulaText = {
        xtype: 'textarea',
        id: 'idFormula',
        padding:'5 5 5 5',
        value: formula,
        readOnly: true,
        hidden: isFormula?false:true,
        width:230
    };
    
    var height = {
        xtype: 'numberfield',
        fieldLabel: 'Height (px)',
        id: 'idHeight',
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        minValue: 0,
        scope:this,
        hidden : isImage?false:true,
        listeners: {
            'change': function (e) {
                updatePropertyForLineItemFields(div);
            }
        }
    };
    
    var imagewidth = {
        xtype: 'numberfield',
        fieldLabel: 'Width (px)',
        id: 'idImageWidth',
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        minValue: 0,
        scope:this,
        hidden : isImage?false:true,
        listeners: {
            'change': function (e) {
                updatePropertyForLineItemFields(div);
            }
        }
    };
    
    var removeDivBtn = new Ext.button.Button({
        text : 'Remove Field',
        hidden : isGlobalTable?false:true,
        width : 106,
        style: {
            'float': 'right',
            'margin-bottom': '10px',
            'margin-right': '36px'
        },
        scope : this,
        handler : function(){
            //            div.remove();
            // ERP-19452 : Remove select field with PreText or Post Text if applied - Ashish Mohite
            if(div.innerHTML.startsWith('#') && div.innerHTML.endsWith('#')){
                var field = div.parentNode; //get select field with PreText and PostText
                var index = field.children.length;
                while(field && index-- ){
                    if(field.children[index].innerHTML == div.innerHTML){
                        //Remove select field and PreText and/or PostText if applied
                        //Note: Do not change sequence of select field deletion
                        //1. Remove PostText if applied
                        if((index+1) < field.children.length && field.children[index+1].id == (div.innerHTML.replace(/#/g,"")+"id2")){
                            remove(field.children[index+1]);
                        }
                        //2. Remove Select Field
                        remove(field.children[index]);
                        //3. Remove PreText if applied
                        if((index-1) >=0 && field.children[index-1].id == (div.innerHTML.replace(/#/g,"")+"id1")){
                            remove(field.children[index-1]);
                        }
                        
                    }
                }
            } else{
                remove(div);             //Done for IE.
            }
            
            if(propertyPanelRoot && propertyPanel){
                propertyPanelRoot.remove(propertyPanel);
            }
        }
    });
    var label = "";
    if(div.attributes.getNamedItem("columnname") != null){
        label = div.attributes.getNamedItem("columnname").value.toLowerCase();
    }
    
    var sequence={
        xtype:'button',
        text:(label==Ext.FieldType.allgloballevelcustomfields || label==Ext.FieldType.alllinelevelcustomfields)?'Set Custom Field Sequence':'Set Dimensions Sequence',
        id:'iddimensionsequence',
        width:150,
        hidden:(label==Ext.FieldType.allgloballeveldimensions || label==Ext.FieldType.alldimensions || label==Ext.FieldType.alllineleveldimensions || label==Ext.FieldType.alllinelevelcustomfields || label==Ext.FieldType.allgloballevelcustomfields)?false:true,
        style:{
            'float':'left'
        },
        listeners:{
            'click':function()
            {
                var label = "";
                if(div.attributes.getNamedItem("columnname") != null){
                    label = div.attributes.getNamedItem("columnname").value.toLowerCase();
                }
                if(label==Ext.FieldType.allgloballeveldimensions || label==Ext.FieldType.alldimensions){
                    openDimensionAndCustomColumnWindow(false , false, false, div);
                } else if(label==Ext.FieldType.alllineleveldimensions){
                    openDimensionAndCustomColumnWindow(false , true, false, div);
                } else if(label==Ext.FieldType.alllinelevelcustomfields){
                    openDimensionAndCustomColumnWindow(true , true, true, div);
                } else if(label==Ext.FieldType.allgloballevelcustomfields){
                    openDimensionAndCustomColumnWindow(true , false, true, div);
                }
            }
        }
    };
    
    var disabled = true;
    if(div.attributes.getNamedItem("attribute") != null){
        var placeholder = div.attributes.getNamedItem("attribute").value;
        var arr = placeholder.match(/\{PLACEHOLDER:(.*?)}/g);
        if (arr) {
            var arraylength = arr.length;
            for (var i = 0; i <= arraylength - 1; i++) {
                var matches = arr[i].replace(/\{|\}/gi, '').split(":");
                var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                if(rec && rec.data && (rec.data.xtype == "2" || rec.data.isNumeric == true ) ){
                    disabled = false;
                }
            }
        }
    }
    
    if(isFormula){
        disabled = false;
    }
    var decimalprecision = {
        xtype: 'numberfield',
        fieldLabel: 'Decimals',
        id: 'decimalprecisionglobaltableid',
        width:210,
        labelWidth:100,
        hidden : isGlobalTable?false:true,
        padding:'2 2 2 2',
        minValue: 0,
        maxValue: 6,
        value : div.attributes.getNamedItem("decimalPrecision") ? div.attributes.getNamedItem("decimalPrecision").value : _amountDecimalPrecision,
        disabled: disabled,
        listeners: {
            'change': function(){
                div.isNumeric = rec.data.isNumeric;
                updatePropertyForLineItemFields(div);
            }
        }
    };
    
    var showAmountInWords = {
        xtype: 'combo',
        fieldLabel: 'Amount In Words',
        id: 'showAmountInWordsId',
        padding: '2 2 2 2',
        hidden: isFormula ? false : true,
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
        value: Ext.isNumber(parseInt(div.getAttribute("showAmountInWords"))) ? parseInt(div.getAttribute("showAmountInWords")) : 0,
        listeners: {
            change: function (combo, newVal, oldVal, eOpts) {
                div.setAttribute("showAmountInWords", newVal);
            }
        }
    };
    
    var valuewithcomma = {
        xtype: 'checkbox',
        id: 'valuewithcommaid',
        fieldLabel: 'Value With Comma',
        padding:'2 2 2 2',
        checked: (div.getAttribute("valueWithComma") == 'true') ? true : false,
        listeners: {
            'change': function (e) {
                var valuewithcommavalue = Ext.getCmp("valuewithcommaid").getValue();
                div.setAttribute("valueWithComma", valuewithcommavalue);
            }
        }
    };
    
    var isNoWrapValue = {
        xtype: 'checkbox',
        id: 'isNoWrapValueId',
        fieldLabel: 'Don\'t Wrap Text' + "<span data-qtip='This property works for</br>comma separated multiple values</br>like PO Ref No., CQ Ref No.' class=\"formHelpButton\">&nbsp;&nbsp;&nbsp;&nbsp;</span>",
        hidden: (isGlobalTable) ? false : true,
        padding:'2 2 2 2',
        checked: (div.getAttribute("isNoWrapValue") == 'true') ? true : false,
        listeners: {
            'change': function (e) {
                div.setAttribute("isNoWrapValue", Ext.getCmp("isNoWrapValueId").getValue());
            }
        }
    };
    
    var verticalAlign = {
        xtype:'combo',
        fieldLabel: 'Vertical Alignments',
        store: Ext.ValignStore,
        id: 'Valignselectcomboid',
        displayField: "name",
        valueField: 'position',
        labelWidth:100,
        queryMode: 'local',
        hidden:false,
        width:210,
        //        value:(config.cellVal.style.verticalAlign)?config.cellVal.style.verticalAlign:((config.cellVal.nodeName == "TH")?"middle":"top"),
        listeners:{
            scope:this,
            change: function () {
                updatePropertyForLineItemFields(div);
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
                updatePropertyForLineItemFields(div);
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
                updatePropertyForLineItemFields(div);
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
                updatePropertyForLineItemFields(div);
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
                updatePropertyForLineItemFields(div);
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
    var bordersettings = {
        xtype: 'fieldset',
        title: 'Border Settings',
        id: 'idbordersettings',
        width:270,
        items:[
        borderComboTop, borderComboBottom, borderComboLeft, borderComboRight,borderColorPanel 
        ]
    };
    var valueSettings = {
        xtype:'fieldset',
        title:'Value',
        autoHeight:true,
        id:'idValueSettingsfieldset',
        width:270,
        hidden: isImage?true:false,
        items:[
            fieldValue,selectField,formulaTitle,formulaText,defaultField,decimalprecision,showAmountInWords,valuewithcomma,isNoWrapValue,removeDivBtn,separatorCombo,dimensionsettingCombo,recordCurrencyCombo,showZeroValueAsBlank
        ]
    };
    var fontSettings = {
        xtype:'fieldset',
        title:'Font Settings',
        autoHeight:true,
        id:'idFontSettingsfieldset',
        width:270,
        hidden: isImage?true:false,
        items:[
        boldText,italicText,fontSize,pagefontfields,textLine,textColorPanel
        ]
    };
    var spaceSettings = {
        xtype:'fieldset',
        title:'Alignment Settings',
        autoHeight:true,
        id:'idSpaceSettingsfieldset',
        width:270,
        items:[
        width,imagewidth,height,textalign,fieldAlignment,verticalAlign
        ]
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
        preText
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
        postText
        ]
    };
    var fieldPropertyPanel =  Ext.create("Ext.Panel", {
        width: 300,
        div:div,
        autoHeight:true,
        border: false,
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [
        valueSettings,spaceSettings,fontSettings,marginsettings,bordersettings,preTextSettings, postTextSettings,sequence
        ]
    });   
    
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
    if (propertyPanel){
        propertyPanelRoot.remove(propertyPanel.id);
    }
    propertyPanel = fieldPropertyPanel; 
    propertyPanelRoot.add(propertyPanel);
    if(isGlobalTable){
        Ext.getCmp("idEastPanel").setTitle("Select Field Property Panel");
    } else if(isImage){
        Ext.getCmp("idEastPanel").setTitle("Image Property Panel");
    } else if(isPrePost){
        Ext.getCmp("idEastPanel").setTitle("PreText/PostText Property Panel");
    } else{
        Ext.getCmp("idEastPanel").setTitle("Line Item Field Property Panel");
    }
    Ext.getCmp("idEastPanel").add(propertyPanelRoot);
    Ext.getCmp("idEastPanel").doLayout();
}

function getDataElementPropertyPanel(event,div,isGlobalTable, isImage,isDataElement) {
    event.stopPropagation();
    createPropertyPanelForDataElement(div,isGlobalTable, isImage,isDataElement);
    setPropertyPanelForDataElement(div,isGlobalTable, isImage,isDataElement);
}

function createPropertyPanelForDataElement (div,isGlobalTable, isImage,isDataElement) {
   
   
    var textColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color",
        width: 105

    });

    var dataTextColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color",
        width: 105

    });
    
    /**
     * ERP-29026 
     * Provide Rich color Picker for Data Value in Global table
     */
    var colorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: (div != null && div != undefined) ? div.textcolordata : '#000000',
        id: 'datacolorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedDataTextPanel').body.setStyle('background-color', newVal);
                updatePropertyForDataElement(div);
            }

        }
    });
    
    
    /**
     * ERP-29026 
     * Provide Rich color Picker for Label in Global table
     */
    var textcolorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: (div != null && div != undefined) ? div.textcolorlabel : '#000000',
        id: 'labelcolorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedlabelTextPanel').body.setStyle('background-color', newVal);
                updatePropertyForDataElement(div);
            }

        }
    });

    var selectedDataTextColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedDataTextPanel',
        height: 20,
        width: 25,
        border: false
    });

    var selectedLabelTextColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedlabelTextPanel',
        height: 20,
        width: 25,
        border: false
    });
    var dataTextColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        padding:'2 2 2 2',
        border: false,
        items: [dataTextColorLabel, selectedDataTextColorPanel, colorPicker]
    });
    
    var labelTextColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        padding:'2 2 2 2',
        border: false,
        items: [textColorLabel, selectedLabelTextColorPanel, textcolorPicker]
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
    
    var dataTextLine = {
        xtype: 'combo',
        fieldLabel: 'Text Line',
        id: 'idDataTextLine',
        store: textLineStore,
        displayField: 'textline',
        valueField: 'id',
        value:  div.getAttribute('textlinedata') ? div.getAttribute('textlinedata') : 'None',
        padding: '2 2 2 2',
        listWidth: 200,
        width: 210,
        listeners: {
            'select': function(e) {
            updatePropertyForDataElement(div);
            }
        }
    };

    var labelTextLine = {
        xtype: 'combo',
        fieldLabel: 'Text Line',
        id: 'idLabelTextLine',
        store: textLineStore,
        displayField: 'textline',
        valueField: 'id',
        padding: '2 2 2 2',
        listWidth: 200,
        width: 210,
        listeners: {
            'select': function(e) {
            updatePropertyForDataElement(div);
            }
        }
    };

    var dataBoldText = {
        xtype: 'checkbox',
        id: 'idBoldDataText',
        fieldLabel: 'Bold',
        checked: ( div.getAttribute('databold') == 'true') ? true : false,
        padding:'2 2 2 2',
        listeners: {
            change: function (e) {
            updatePropertyForDataElement(div);
            }
        }
    };
    var dataItalicText = {
        xtype: 'checkbox',
        id: 'idDataItalicText',
        fieldLabel: 'Italic',
        checked:( div.getAttribute('dataitalic') == 'true') ? true : false,
        padding:'2 2 2 2',
        listeners: {
            change: function (checkbox, newVal, oldVal) {
            updatePropertyForDataElement(div);
            }
        }
    };
    
    var labelBoldText = {
        xtype: 'checkbox',
        id: 'idLabelBoldText',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        listeners: {
            change: function (e) {
            updatePropertyForDataElement(div);
            }
        }
    };
    var labelItalicText = {
        xtype: 'checkbox',
        id: 'idLabelItalicText',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        listeners: {
            change: function (checkbox, newVal, oldVal) {
            updatePropertyForDataElement(div);
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
        }
        ]
    });
    
    
    var datapagefontfields = {
        xtype: 'combo',
        fieldLabel: 'Font',
        id:'datapagefontid',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        padding:'2 2 2 2',
        labelWidth:100,
        width:210,
        value:div.getAttribute('datafontfamily') ? div.getAttribute('datafontfamily'):'',
        listeners: {
            'select': function (e) {
            updatePropertyForDataElement(div);
            }
        }
    };
    
    var labelpagefontfields = {
        xtype: 'combo',
        fieldLabel: 'Font',
        id:'labelpagefontid',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        padding:'2 2 2 2',
        labelWidth:100,
        value:div ? div.labelfontfamily:'',
        width:210,
        listeners: {
            'select': function (e) {
            updatePropertyForDataElement(div);
            }
        }
    };
    var labelFontSize = {
        xtype: 'numberfield',
        fieldLabel: 'Font Size',
        id: 'idLabelFontSize',
        width: 210,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        listeners: {
            'change': function () {
            updatePropertyForDataElement(div);
            }
        }
    };
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
    var labelColonCombo = new Ext.form.field.ComboBox({
        fieldLabel: 'Colon Position',
        store: colonStore,
        id: 'labelColonPositionCombo',
        displayField: 'position',
        valueField: 'id',
        queryMode: 'local',
        width: 210,
        emptyText: 'Select a Position',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        hidden : false,
        value :-1,
        listeners:{
            scope:this,
            change: function () {
            updatePropertyForDataElement(div);
            }
        }
    });
    var  fieldValue = "";
    if(div && div.labelval){
        fieldValue = div.labelval;
    }else if(div && div.attributes.getNamedItem("allowlabel") && div.attributes.getNamedItem("allowlabel").value=="true"){
       fieldValue = div.children[0].children[0].innerHTML;
    }else if(div && div.attributes.getNamedItem("allowlabel") && div.attributes.getNamedItem("allowlabel").value=="false"){
       fieldValue = div.children[0].innerHTML;
    }
    fieldValue = fieldValue.replace(/<span[\s\w\d=;:"'().\/,\-]*>(.|\n)*?<\/span>/gi,"");
    var labelValue = {
        xtype: 'textfield',
        fieldLabel: 'Label',
        id: 'idLabelValue',
        padding:'2 2 2 2',
        width:210,
        labelWidth:100,
        value:fieldValue,
        disabled:div?!div.customlabel:true,
        listeners: {
            'change': function (e) {
            updatePropertyForDataElement(div);
            }
        }
    };
    
    var customizeLabel = {
        xtype:'checkbox',
        fieldLabel: 'Custom Label',
        id: 'idcustomlabel',
        padding:'2 2 2 2',
        name: 'customlabel',
        checked:div?div.customlabel:false,
        width: 300,
        listeners: {
            scope: this,
            change: function(field, nval, oval) {
                if(Ext.getCmp('idLabelValue')){
                    if(nval==true){
                        Ext.getCmp('idLabelValue').setDisabled(false);
                    } else{
                        var rec = searchRecord(defaultFieldGlobalStore, Ext.getCmp('customdesignerdataselectcombo').getValue(), 'id');
                        if(rec && rec.data){
                            Ext.getCmp('idLabelValue').setValue(rec.data.label);
                        }
                        Ext.getCmp('idLabelValue').setDisabled(true);

                    }
                }
                div.setAttribute('customlabel',nval);
            }
        }
    };
    var dataFontSize = {
        xtype: 'numberfield',
        fieldLabel: 'Font Size',
        id: 'idDataFontSize',
        width: 210,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        listeners: {
            'change': function () {
            updatePropertyForDataElement(div);
            }
        }
    };
    var fieldAlignment = {
        xtype: 'combo',
        fieldLabel: 'Field Alignment',
        id: 'comp_fields_Alignment',
        padding:'2 2 2 2',
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "type"],
            data: [
            {
                id: "inline-block",
                type: "Block"
            }, {
                id: "inline-table",
                type: "Inline"

            }
            ]
        }),
        displayField: 'type',
        valueField: 'id',
        width: 210,
        value:div.style.display ? div.style.display:"inline-block",
        labelWidth: 100,
        listeners: {
            select: function (field) {
            updatePropertyForDataElement(div);
            }
        }
    };
    var textAlignmentStore = Ext.create("Ext.data.Store", {
        fields: ["id", "textalign"],
        data: [
        {
            id: "left",
            textalign: "Left"
        }, {
            id: "center",
            textalign: "Center"

        }, {
            id: "right",
            textalign: "Right"
        }
        ]
    });
    var datatextalign = {
        xtype: 'combo',
        fieldLabel: 'Text Alignment',
        id: 'idDataTextAlign',
        store: textAlignmentStore,
        padding:'2 2 2 2',
        displayField: 'textalign',
        valueField: 'id',
        width: 210,
        listeners: {
            'select': function (e) {
           updatePropertyForDataElement(div);
            }
        }
    };
    var labeltextalign = {
        xtype: 'combo',
        fieldLabel: 'Text Alignment',
        id: 'idLabelTextAlign',
        store: textAlignmentStore,
        padding:'2 2 2 2',
        displayField: 'textalign',
        valueField: 'id',
        width: 210,
        listeners: {
            'select': function (e) {
            updatePropertyForDataElement(div);
            }
        }
    };
    
    var dataDefaultField = {
        xtype: 'textfield',
        fieldLabel: 'Default Value',
        id: "iddefaultFieldValue",
        padding:'2 2 2 2',
        width:210,
        labelWidth:100,
        value:div.getAttribute("defaultValue") ? div.getAttribute("defaultValue"):'',
        listeners: {
            'change': function (e) {
            updatePropertyForDataElement(div);
            }
        }
    };
    var dataWidth = {
        xtype: 'numberfield',
        fieldLabel: 'Width',
        id: 'idDataWidth',
        padding:'2 2 2 2',
        width: 210,
        maxValue:100,
        minValue:0,
        minLength: 1,
        hidden : isImage?true:false,
        scope:this,
        listeners: {
            'change': function (field) {
                if ( field.getValue() > 100 ) {
                    field.setValue(100);
                    WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                }
                var allowlabel = div.attributes.getNamedItem("allowlabel") && div.attributes.getNamedItem("allowlabel").value=="true" ? true:false;
                if(allowlabel){
                    var labelspan = div.children[0];
                    var selectField = div.children[1];
                    var dataWidth = Ext.getCmp("idDataWidth").getValue();
                    var labelWidth = 100-dataWidth;
                    selectField.style.width= dataWidth+"%";
                    labelspan.style.width= labelWidth+"%";
                    div.labelwidth = labelWidth;
                    div.datawidth = dataWidth;
                    if(Ext.getCmp("idLabelWidth")){
                        Ext.getCmp("idLabelWidth").setValue(labelWidth);
                    }
                } else{
                    var selectField = div.children[0];
                    var dataWidth = Ext.getCmp("idDataWidth").getValue();
                    selectField.style.width= dataWidth+"%";
                    div.labelwidth = 0;
                    div.datawidth = dataWidth;
                    if(Ext.getCmp("idLabelWidth")){
                        Ext.getCmp("idLabelWidth").setValue(labelWidth);
                    }
                }
                div.dataelementhtml= div.outerHTML;
            }
        }
    };
    
    var labelwidth = {
        xtype: 'numberfield',
        fieldLabel: 'Width',
        id: 'idlabelWidth',
        padding:'2 2 2 2',
        width: 210,
        maxValue:100,
        minValue:0,
        minLength: 1,
        hidden : isImage?true:false,
        scope:this,
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
                    var allowlabel = div.attributes.getNamedItem("allowlabel") && div.attributes.getNamedItem("allowlabel").value=="true" ? true:false;
                    if(allowlabel){
                        var labelspan = div.children[0];
                        var selectField = div.children[1];
                        var labelWidth = field.getValue();
                        var dataWidth = 100 - labelWidth;
                        labelspan.style.width= labelWidth+"%";
                        selectField.style.width= dataWidth+"%";
                        div.labelwidth = labelWidth;
                        div.datawidth = dataWidth;
                        if(Ext.getCmp("idDataWidth")){
                            Ext.getCmp("idDataWidth").setValue(dataWidth);
                        }
                    }
                    
                    div.dataelementhtml= div.outerHTML;
            }
        }
    };
    var componentwidth = {
        xtype: 'numberfield',
        fieldLabel: 'Width',
        id: 'idComponentWidth',
        padding:'2 2 2 2',
        width: 210,
        maxValue:100,
        minValue:0,
        minLength: 1,
        hidden : isImage?true:false,
        scope:this,
        listeners: {
            'change': function (e) {
                if(!e.hidden){
                    if ( e.getValue() > 100 ) {
                        e.setValue(100);
                        WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                    }
                updatePropertyForDataElement(div);
                }
            }
        }
    };
    var selectFieldValue='';
    if(div.attributes.getNamedItem("allowlabel") && div.attributes.getNamedItem("allowlabel").value=="true"){
        selectFieldValue=div.children[1].childNodes[0].data.replace(/#/ig,"");
    }else{
        selectFieldValue=div.children[0].childNodes[0].data.replace(/#/ig,"");
    }
    var dataSelectField = {
        xtype: 'combo',
        fieldLabel: 'Select Field',
        store: defaultFieldGlobalStore,
        id: 'customdesignerdataselectcombo',
        displayField: 'label',
        valueField: 'id',
        queryMode: 'local',
        padding:'2 2 2 2',
        width: 245,
        div:div,
        hidden:isGlobalTable?false:true,
        blankText: 'Select a field',
        value : selectFieldValue,
        listeners: {
            scope: this,
            'select': function (combo, selection) {
                if(selection[0].data.xtype!="NAN"){
                    var dataSpan='';
                    if(div.attributes.getNamedItem("allowlabel") && div.attributes.getNamedItem("allowlabel").value=="true"){
                        if(getEXTComponent('idcustomlabel').getValue()==false){
                            getEXTComponent('idLabelValue').setValue(selection[0].data.label);
                        }
                        dataSpan=div.children[1];
                    }else{
                        dataSpan=div.children[0];
                    }
                    dataSpan.innerHTML = "#" + selection[0].data.label + "#";
                    dataSpan.setAttribute("attribute","{PLACEHOLDER:"+ selection[0].data.id+"}");
                    dataSpan.setAttribute("defaultValue", "");
                }
            }
        }
    };
    
    var compRemoveDivBtn = new Ext.button.Button({
        text : 'Remove Field',
        hidden : isGlobalTable?false:true,
        width : 106,
        style: {
            'float': 'right',
            'margin-bottom': '10px',
            'margin-right': '36px'
        },
        scope : this,
        handler : function(){
            //            div.remove();
            remove(div);             //Done for IE.
            if(propertyPanelRoot && propertyPanel){
                propertyPanelRoot.remove(propertyPanel);
            }
        }
    });
    var disabled = true;
    if(div.attributes.getNamedItem("attribute") != null){
        var placeholder = div.attributes.getNamedItem("attribute").value;
        var arr = placeholder.match(/\{PLACEHOLDER:(.*?)}/g);
        if (arr) {
            var arraylength = arr.length;
            for (var i = 0; i <= arraylength - 1; i++) {
                var matches = arr[i].replace(/\{|\}/gi, '').split(":");
                var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                if(rec && rec.data && rec.data.xtype == "2" ){
                    disabled = false;
                }
            }
        }
    }

    var decimalprecision = {
        xtype: 'numberfield',
        fieldLabel: 'Decimals',
        id: 'decimalprecisiondataid',
        width:210,
        labelWidth:100,
        padding:'2 2 2 2',
        minValue: 0,
        maxValue: 6,
        value : div.attributes.getNamedItem("decimalPrecision") ? div.attributes.getNamedItem("decimalPrecision").value : 2,
        listeners: {
            'change': function(){
            updatePropertyForDataElement(div);
            }
        }
    };
    
    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value:div.getAttribute("topmargin") ? div.getAttribute("topmargin"):0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,
        listeners: {
            change: function (field) {
            updatePropertyForDataElement(div);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value:div.getAttribute("bottommargin") ? div.getAttribute("bottommargin"):0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,
        listeners: {
            change: function (field) {
            updatePropertyForDataElement(div);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value:div.getAttribute("leftmargin") ? div.getAttribute("leftmargin"):0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,
        listeners: {
            change: function (field) {
            updatePropertyForDataElement(div);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value:div.getAttribute("rightmargin") ? div.getAttribute("rightmargin"):0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,
        listeners: {
            change: function (field) {
            updatePropertyForDataElement(div);
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
        value:div.getAttribute("valueseparator")?div.getAttribute("valueseparator"):"linebreak",
        listeners: {
            'select': function(e) {
                updatePropertyForDataElement(div);
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
        id: 'iddimensionDataField',
        store: dimensionStore,
        displayField: 'text',
        valueField: 'id',
        listWidth: 200,
        padding:'2 2 2 2',
        width: 230,
        value:div.getAttribute("dimensionvalue")?div.getAttribute("dimensionvalue"):"2",
        listeners: {
            'select': function(e) {
                updatePropertyForDataElement(div);
            }
        }
       
    };
    var valuewithcomma = {
        xtype: 'checkbox',
        id: 'valuewithcommadataid',
        fieldLabel: 'Value With Comma',
        padding:'2 2 2 2',
        checked: (div.getAttribute("valueWithComma") == 'true') ? true : false,
        listeners: {
            change: function (e) {
                updatePropertyForDataElement(div);
            }
        }
    };
    /**
     * Checkbox for Show zero(0) value as blank or not
     */
    var showZeroValueAsBlank = {
        xtype: 'checkbox',
        id: 'showZeroValueAsBlankId',
        fieldLabel: 'Show Zero(0) Value as Blank',
        padding:'2 2 2 2',
        hidden: div.getAttribute("xtype") != '2',
        checked: (div.getAttribute("showzerovalueasblank") == 'true') ? true : false,
        listeners: {
            'change': function (e) {
                var showzerovalueasblank = Ext.getCmp("showZeroValueAsBlankId").getValue();
                div.setAttribute("showzerovalueasblank", showzerovalueasblank);
            }
        }
    };
    
    var isNoWrapValue = {
        xtype: 'checkbox',
        id: 'isNoWrapValueId',
        fieldLabel: 'Don\'t Wrap Text' + "<span data-qtip='This property works for</br>comma separated multiple values</br>like PO Ref No., CQ Ref No.' class=\"formHelpButton\">&nbsp;&nbsp;&nbsp;&nbsp;</span>",
        padding:'2 2 2 2',
        checked: (div.getAttribute("isNoWrapValue") == 'true') ? true : false,
        listeners: {
            'change': function (e) {
                div.setAttribute("isNoWrapValue", Ext.getCmp("isNoWrapValueId").getValue());
            }
        }
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
    var componentSettings = {
        xtype:'fieldset',
        title:'Component Settings',
        autoHeight:true,
        id:'idComponentSettingsfieldset',
        width:270,
        //        hidden: isImage?true:false,
        items:[
        componentwidth,fieldAlignment,compRemoveDivBtn
        ]
    };
    var labelSettings = {
        xtype:'fieldset',
        title:'Label Settings',
        autoHeight:true,
        id:'idlabelSettingsfieldset',
        width:270,
        hidden:div.attributes.getNamedItem("allowlabel") && div.attributes.getNamedItem("allowlabel").value=="false" ? true:false,
        items:[
        labelBoldText,labelItalicText,customizeLabel,labelValue,labelwidth,labelTextColorPanel,labelFontSize,labelpagefontfields,labeltextalign,labelTextLine,labelColonCombo
        ]
    };
    
    var dataSettings = {
        xtype:'fieldset',
        title:'Data Settings',
        autoHeight:true,
        id:'idDataSettingsfieldset',
        width:270,
        hidden: isImage?true:false,
        items:[
            dataBoldText, dataItalicText, separatorCombo, dimensionsettingCombo, dataSelectField, dataDefaultField, decimalprecision, valuewithcomma, isNoWrapValue, dataWidth, dataTextColorPanel, dataFontSize, datapagefontfields, datatextalign, dataTextLine, showZeroValueAsBlank
        ]
    };
    
    
    var fieldPropertyPanel =  Ext.create("Ext.Panel", {
        width: 300,
        div:div,
        autoHeight:true,
        border: false,
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [componentSettings,labelSettings,dataSettings,marginsettings]
    });   
    
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
    if (propertyPanel ){
        propertyPanelRoot.remove(propertyPanel.id);
    }
    propertyPanel = fieldPropertyPanel; 
    propertyPanelRoot.add(fieldPropertyPanel);
    if(isGlobalTable && isDataElement){
        Ext.getCmp("idEastPanel").setTitle("Data Element Property Panel");
    }else if(isGlobalTable && !isDataElement){
        Ext.getCmp("idEastPanel").setTitle("Select Field Property Panel");
    } else if(isImage){
        Ext.getCmp("idEastPanel").setTitle("Image Property Panel");
    } else{
        Ext.getCmp("idEastPanel").setTitle("Line Item Field Property Panel");
    }
    Ext.getCmp("idEastPanel").add(propertyPanelRoot);
    Ext.getCmp("idEastPanel").doLayout();
}

function setPropertyPanelForDataElement (div,isGlobalTable,isImage) {
    
    var dataspan = "";
    var allowlabel = div.attributes.getNamedItem("allowlabel") && div.attributes.getNamedItem("allowlabel").value=="true" ? true:false;
    if(allowlabel){
        var labelspan = div.children[0];
        dataspan = div.children[1];
        var labelWidth = labelspan.style.width?labelspan.style.width:100;
        var labelTextAlign = labelspan.style.textAlign ? labelspan.style.textAlign:"left";
        var labelFontSize = labelspan.style.fontSize ? labelspan.style.fontSize:'';
        var labelFontFamily = labelspan.style.fontFamily ? labelspan.style.fontFamily:'';
        var labelFontWeight = labelspan.style.fontWeight ? labelspan.style.fontWeight == "bold":false;
        var labelFontStyle = labelspan.style.fontStyle ? labelspan.style.fontStyle == "italic":false;
        var labelTextLine = labelspan.style.textDecoration ? labelspan.style.textDecoration:"none";
        var labelTextColor = labelspan.style.color ? labelspan.style.color:"#000000";
        var customLabel = div.getAttribute("customlabel") ? Boolean(div.getAttribute("customlabel")):"";
        var labelColon = parseInt(div.getAttribute("colontype")?div.getAttribute("colontype"):"-1");
        
        
        if ( Ext.getCmp('idLabelTextLine') ) {
            Ext.getCmp('idLabelTextLine').setValue(labelTextLine);
        }
   
        if ( Ext.getCmp('labelcolorpicker') ) {
            Ext.getCmp('labelcolorpicker').value = labelTextColor;
        }
        if ( Ext.getCmp('idSelectedlabelTextPanel') ) {
            Ext.getCmp('idSelectedlabelTextPanel').body.setStyle('background-color', labelTextColor);
        }
        if ( Ext.getCmp('idLabelItalicText') ) {
            Ext.getCmp('idLabelItalicText').setValue(labelFontStyle);
        }
        if ( Ext.getCmp('idLabelBoldText') ) {
            Ext.getCmp('idLabelBoldText').setValue(labelFontWeight);
        }
        if ( Ext.getCmp('idLabelFontSize')) {
            Ext.getCmp('idLabelFontSize').setValue(labelFontSize);
        }
        if ( Ext.getCmp('labelpagefontid')) {
            Ext.getCmp('labelpagefontid').setValue(labelFontFamily);
        }
    
        if ( Ext.getCmp('idLabelTextAlign') ) {
            Ext.getCmp('idLabelTextAlign').setValue(labelTextAlign);
        }
        if ( Ext.getCmp('idlabelWidth') ) {
            Ext.getCmp('idlabelWidth').setValue(labelWidth);
        }
        if ( Ext.getCmp('labelColonPositionCombo') ) {
            Ext.getCmp('labelColonPositionCombo').setValue(labelColon);
        }
        if ( Ext.getCmp('idcustomlabel') ) {
            Ext.getCmp('idcustomlabel').setValue(customLabel);
        }
    }
    else{
        dataspan = div.children[0];
        dataspan.style.width = "100%";
    }
    
    var componentWidth = div.style.width?div.style.width:100;
    var fieldAlign = div.style.display?div.style.display:"inline-block";
    var marginTop = div.style.marginTop?div.style.marginTop:"0px";
    var marginBottom = div.style.marginBottom?div.style.marginBottom:"0px";
    var marginLeft = div.style.marginLeft?div.style.marginLeft:"0px";
    var marginRight = div.style.marginRight?div.style.marginRight:"0px";
    
    
    var dataWidth = dataspan.style.width?dataspan.style.width:100;
    var dataTextAlign = dataspan.style.textAlign ? dataspan.style.textAlign:"left";
    var dataFontSize = dataspan.style.fontSize ? dataspan.style.fontSize:'';
    var dataFontFamily = dataspan.style.fontFamily ? dataspan.style.fontFamily:'';
    var dataFontWeight = dataspan.style.fontWeight ? dataspan.style.fontWeight == "bold":false;
    var dataFontStyle = dataspan.style.fontStyle ? dataspan.style.fontStyle == "italic":false;
    var dataTextLine = dataspan.style.textDecoration ? dataspan.style.textDecoration:"none";
    var dataTextColor = dataspan.style.color ? dataspan.style.color:"#000000";
    
    var defValue = div.attributes.getNamedItem("defaultValue") ? div.attributes.getNamedItem("defaultValue").value : "";
    if (Ext.getCmp("iddefaultFieldValue")) {
        Ext.getCmp("iddefaultFieldValue").setValue(defValue.replace(/&nbsp;/g,' '));
    }
    var decimalPrecision = div.attributes.getNamedItem("decimalPrecision") ? div.attributes.getNamedItem("decimalPrecision").value : 2;
    if (Ext.getCmp('decimalprecisiondataid')) {
        Ext.getCmp('decimalprecisiondataid').setValue(decimalPrecision);
    }
    
    if ( Ext.getCmp('idDataTextLine') ) {
        Ext.getCmp('idDataTextLine').setValue(dataTextLine);
    }
   
    if ( Ext.getCmp('datacolorpicker') ) {
        Ext.getCmp('datacolorpicker').value = dataTextColor;
    }
    if ( Ext.getCmp('idSelectedDataTextPanel') ) {
        Ext.getCmp('idSelectedDataTextPanel').body.setStyle('background-color', dataTextColor);
    }
    if ( Ext.getCmp('idDataItalicText') ) {
        Ext.getCmp('idDataItalicText').setValue(dataFontStyle);
    }
    if ( Ext.getCmp("idBoldDataText") ) {
        Ext.getCmp("idBoldDataText").setValue(dataFontWeight);
    }
    if ( Ext.getCmp('idDataFontSize')) {
        Ext.getCmp('idDataFontSize').setValue(dataFontSize);
    }
    if ( Ext.getCmp('datapagefontid')) {
        Ext.getCmp('datapagefontid').setValue(dataFontFamily);
    }
    
    if ( Ext.getCmp('idDataTextAlign') ) {
        Ext.getCmp('idDataTextAlign').setValue(dataTextAlign);
    }
    if ( Ext.getCmp('idDataWidth') ) {
        Ext.getCmp('idDataWidth').setValue(dataWidth);
    }
//    Common Settings
    if ( Ext.getCmp('idComponentWidth') ) {
        Ext.getCmp('idComponentWidth').setValue(componentWidth);
    }
    if (Ext.getCmp('comp_fields_Alignment')) {
        Ext.getCmp('comp_fields_Alignment').setValue(fieldAlign);
    }
     if ( Ext.getCmp("idtopmargin") ) {
        Ext.getCmp("idtopmargin").setValue(marginTop);
    }
    if ( Ext.getCmp("idbottommargin") ) {
        Ext.getCmp("idbottommargin").setValue(marginBottom);
    }
    if ( Ext.getCmp("idleftmargin") ) {
        Ext.getCmp("idleftmargin").setValue(marginLeft);
    }
    if ( Ext.getCmp("idrightmargin") ) {
        Ext.getCmp("idrightmargin").setValue(marginRight);
    }
}


function updatePropertyForDataElement (div) {
  
  var dataspan = "";
    var allowlabel = div.attributes.getNamedItem("allowlabel") && div.attributes.getNamedItem("allowlabel").value=="true" ? true:false;
    if(allowlabel){
        var label = div.children[0].children[0];
        var labelspan = div.children[0];
        dataspan = div.children[1];
        if(Ext.getCmp("idLabelValue")){
            var newlabel = Ext.getCmp("idLabelValue").getValue();
            label.innerHTML= newlabel;
            div.labelval = newlabel;
        }
        if(Ext.getCmp('idLabelBoldText')){
            var value = Ext.getCmp('idLabelBoldText').getValue();
            if(value==true){
                labelspan.style.fontWeight = "bold";
            } else {
                labelspan.style.fontWeight = "normal";
            }
            div.labelbold = value;
        }
        if(Ext.getCmp('idLabelItalicText')){
            var valueL = Ext.getCmp('idLabelItalicText').getValue();
            if(valueL==true){
                labelspan.style.fontStyle = "Italic";
            } else {
                labelspan.style.fontStyle = "normal";
            }
            div.labelitalic = valueL;
        }
        if (Ext.getCmp('idLabelFontSize')) {
            var fontsize = Ext.getCmp('idLabelFontSize').getValue();
            labelspan.style.fontSize = fontsize + "px";
            div.labelfontsize = fontsize;
        }
        if (Ext.getCmp('labelpagefontid')) {
            var tmpFontFamily = Ext.getCmp('labelpagefontid').getValue();
            labelspan.style.fontFamily = tmpFontFamily;
            div.labelfontfamily = tmpFontFamily;
        }
        if(Ext.getCmp('idLabelTextAlign')){
            var align = Ext.getCmp('idLabelTextAlign').getValue();
            if(align == "center"){
                labelspan.style.textAlign = "center";
            } else if(align == "left"){
                labelspan.style.textAlign = "left";
            } else if(align == "right"){
                labelspan.style.textAlign = "right";
            }
            div.labelalign = align;
        }
        if(Ext.getCmp('labelColonPositionCombo')){
            var editvalue = Ext.getCmp("idLabelValue").getValue();
            var colonType =Ext.getCmp('labelColonPositionCombo').getValue();
            if(colonType!=null && colonType===0){//With the text
                editvalue = editvalue+"<span class='colonwiththetextwithoutmargin'>:</span>";
                colonType=0;
            } else if(colonType==1){ // Right Aligned
                editvalue = editvalue+"<span class='rightalignedcolonwithoutmargin'>:</span>";
                colonType=1;
            }
            label.innerHTML = editvalue;
            div.labelval = editvalue;
            div.setAttribute('colontype', colonType);
        }
        if(Ext.getCmp('idLabelTextLine')){
            var textLine = Ext.getCmp('idLabelTextLine').getRawValue();
            if (textLine == "Overline") {
                labelspan.style.textDecoration = 'overline';
                div.textlinelabel = "Overline";
            } else if (textLine == "Line-through") {
                labelspan.style.textDecoration = 'line-through';
                div.textlinelabel = "Line-through";
            } else if (textLine == "Underline") {
                labelspan.style.textDecoration = 'underline';
                div.textlinelabel = "Underline";
            } else {
                labelspan.style.textDecoration = '';
                div.textlinelabel = "None";
            }
                
        }
        if(Ext.getCmp('labelcolorpicker')){
            var selectednewcolor = Ext.getCmp('labelcolorpicker').value;
            labelspan.style.color = selectednewcolor;
            div.textcolorlabel = selectednewcolor;
        }
                
    } else{
        dataspan = div.children[0];
        dataspan.style.width = "100%";
    }
            
    if(Ext.getCmp("idBoldDataText")){
        var valueB = Ext.getCmp("idBoldDataText").getValue();
        if(valueB==true){
            dataspan.style.fontWeight = "bold";
        } else {
            dataspan.style.fontWeight = "normal";
        }
        div.setAttribute('databold',valueB) ;
    }
    if(Ext.getCmp('idDataItalicText')){
        var valueD = Ext.getCmp('idDataItalicText').getValue();
        if(valueD==true){
            dataspan.style.fontStyle = "Italic";
        } else {
            dataspan.style.fontStyle = "normal";
        }
        div.setAttribute('dataitalic',valueD) ;
    }
            
    if(Ext.getCmp("idDataFontSize")){
        var fontsizeD = Ext.getCmp("idDataFontSize").getValue();
        dataspan.style.fontSize = fontsizeD + "px";
        div.datafontsize = fontsizeD;
    }
    if(Ext.getCmp('datapagefontid')){
        var fontfamily = Ext.getCmp('datapagefontid').getValue();
        dataspan.style.fontFamily = fontfamily;
        div.datafontfamily = fontfamily;
        div.setAttribute('datafontfamily',fontfamily) ;
    }
            
    if(Ext.getCmp('idDataTextAlign')){
        var alignD = Ext.getCmp('idDataTextAlign').getValue();
        if(alignD == "center"){
            dataspan.style.textAlign = "center";
        } else if(alignD == "left"){
            dataspan.style.textAlign = "left";
        } else if(alignD == "right"){
            dataspan.style.textAlign = "right";
        }
        div.dataalign = alignD;
    }
    // common settings
    var margin= 0;
    if(Ext.getCmp("idtopmargin")){
        margin = Ext.getCmp("idtopmargin").getValue();
        div.style.marginTop= margin+"px";
        div.setAttribute("topmargin",margin);
    }
    if(Ext.getCmp("idbottommargin")){
        margin = Ext.getCmp("idbottommargin").getValue();
        div.style.marginBottom= margin+"px";
        div.setAttribute("bottommargin",margin);
    }
    if(Ext.getCmp("idleftmargin")){
        margin = Ext.getCmp("idleftmargin").getValue();
        div.style.marginLeft= margin+"px";
        div.setAttribute("leftmargin",margin);
    }
    if(Ext.getCmp("idrightmargin")){
        margin = Ext.getCmp("idrightmargin").getValue();
        div.style.marginRight= margin+"px";
        div.setAttribute("rightmargin",margin);
    }
            
    if(Ext.getCmp('idDataTextLine')){
        var textLineD = Ext.getCmp('idDataTextLine').getRawValue();
        if (textLineD == "Overline") {
            dataspan.style.textDecoration = 'overline';
            div.setAttribute('textlinedata',"Overline") ;
        } else if (textLineD == "Line-through") {
            dataspan.style.textDecoration = 'line-through';
            div.setAttribute('textlinedata',"Line-through") ;
        } else if (textLineD == "Underline") {
            dataspan.style.textDecoration = 'underline';
            div.setAttribute('textlinedata',"Underline") ;
        } else {
            dataspan.style.textDecoration = '';
            div.setAttribute('textlinedata',"None") ;
        }
                
    }
            
    if(Ext.getCmp('datacolorpicker')){
        var selectednewcolor = Ext.getCmp('datacolorpicker').value;
        dataspan.style.color = selectednewcolor;
        div.textcolordata = selectednewcolor;
    }
            
    if(Ext.getCmp('decimalprecisiondataid')){
        div.setAttribute('decimalPrecision', Ext.getCmp('decimalprecisiondataid').getValue());
    }
    if (Ext.getCmp("iddefaultFieldValue")) {
        div.setAttribute("defaultValue", Ext.getCmp("iddefaultFieldValue").getValue().replace(/\s/g,space) );
    } 
    if (Ext.getCmp("idvalSeparatorDataField")) {
        div.setAttribute("valueSeparator", Ext.getCmp("idvalSeparatorDataField").getValue() );
    } 
    if (Ext.getCmp("iddimensionDataField")) {
        div.setAttribute("dimensionValue", Ext.getCmp("iddimensionDataField").getValue() );
    } 
    
    if (Ext.getCmp("valuewithcommadataid")) {
        div.setAttribute("valueWithComma", Ext.getCmp("valuewithcommadataid").getValue() );
    } 
            
    var dataelement = div.children[0];
            
    if(Ext.getCmp("idComponentWidth")){
        div.style.width=Ext.getCmp("idComponentWidth").getValue()+"%";
        div.width = Ext.getCmp("idComponentWidth").getValue()+"%";
        div.componentwidth = Ext.getCmp("idComponentWidth").getValue();
    }
    if(Ext.getCmp( 'comp_fields_Alignment')){
        var fieldAlignment = Ext.getCmp( 'comp_fields_Alignment').getValue();
        if ( fieldAlignment == "inline-block" ) {
            div.style.display="inline-block";
            div.fieldalignment = "inline-block"
        } else if ( fieldAlignment == "inline-table" ) {
            div.style.display="inline-table";
            div.fieldalignment = "inline-table"
        }
    }
            
    div.dataelementhtml = dataelement.outerHTML;
   
}


function addPrePostText(div , type, text, style) {
    var parentNode = div.parentNode;
    var colName = div.attributes.getNamedItem("columnname").value
    var colno = div.attributes.getNamedItem("colno").value
    var id = colName + "id" + type;
    if (!document.getElementById(id)) {
        var label = document.createElement('div');
        if(text!=undefined){
            label.innerHTML = text;
        } else{
            label.innerHTML = type == 1 ? "PreText" : "PostText";
        }
        label.setAttribute("id", colName + "id" + type);
        label.setAttribute("label", (type == 1 ? "PreText" : "PostText"));
        label.setAttribute("type", type );
        label.setAttribute("onclick", "getPropertyPanelForLineItemFields(event,this,false,false,true)");
        label.setAttribute("class", "fieldItemDivBorder");
        label.setAttribute("style", "width:100%");
        label.setAttribute("colno", colno);
        if (type == 1) {
            parentNode.insertBefore(label, div);
            div.setAttribute("ispretext", true );
        } else {
            div.setAttribute("isposttext", true );
            if(div.nextSibling) {
                parentNode.insertBefore(label, div.nextSibling);
            } else {
                parentNode.appendChild(label);
            }
        }
        if(style){
            label.setAttribute("style", style);
        }
    }
}

function removePrePostText(div,type) {
    var colName = div.attributes.getNamedItem("columnname").value
    var id = colName + "id" + type;
    var parentNode = div.parentNode;
    if (document.getElementById(id)) {
        //        document.getElementById(id).remove();
        remove(document.getElementById(id));                //Done for IE.
    }
    if ( type == 1 ) {
        div.setAttribute("ispretext", false );
    } else  {
        div.setAttribute("isposttext", false );
    }
}

function updatePropertyForLineItemFields (div) {
    var width, imagewidth, height;
    var borderColor='#000000';
    if(getEXTComponent("idborderpicker")){
        borderColor=getEXTComponent("idborderpicker").value ? getEXTComponent("idborderpicker").value:'#000000';
    }
    getEXTComponent('idBorderPanel').body.setStyle('background-color', borderColor);
    if ( Ext.getCmp("idWidth") ) {
        width = Ext.getCmp("idWidth").getValue();
        div.style.width = width + "%";
    }
    if ( Ext.getCmp("idHeight") ) {
        height = Ext.getCmp("idHeight").getValue();
        div.style.height = height + "px";
    }
    
    if(getEXTComponent('borderComboLeft')){
        var borderLeftType = getEXTComponent('borderComboLeft').getValue();
        if(borderLeftType!="none"){
            div.style.setProperty("border-left", "1px "+borderLeftType+borderColor ,"important");
            if ( borderLeftType === "double") {
                div.style.setProperty("border-left-width", "4px ","important");
            } else {
                div.style.setProperty("border-left-width", "1px thin","important");
            }    
        }else{
            div.style.setProperty("border-left", "");
        }
    }
    
    if(getEXTComponent('borderComboRight')){
        var borderRightType = getEXTComponent('borderComboRight').getValue();
        if(borderRightType!="none"){
            div.style.setProperty("border-right", "1px "+borderRightType+borderColor , "important");
            if ( borderRightType === "double") {
                div.style.setProperty("border-right-width", "4px" ,"important");
            } else {
                div.style.setProperty("border-right-width", "1px thin","important");
            }
        }else{
            div.style.setProperty("border-right", "");
        }
    }
    if(getEXTComponent('borderComboTop')){
        var borderTopType = getEXTComponent('borderComboTop').getValue();
        if(borderTopType!="none"){
            div.style.setProperty("border-top","1px "+ borderTopType+borderColor , "important");
            if ( borderTopType === "double") {
                div.style.setProperty("border-top-width", "4px" ,"important");
            } else {
                div.style.setProperty("border-top-width", "1px thin","important");
            }
        }else{
            div.style.setProperty("border-top","");
        }
    }
    if ( getEXTComponent('borderComboBottom')){
        var borderBottomType = getEXTComponent('borderComboBottom').getValue();
        if(borderBottomType!="none"){
            div.style.setProperty("border-bottom","1px "+ borderBottomType+borderColor , "important");
            if ( borderBottomType === "double") {
                div.style.setProperty("border-bottom-width", "4px" ,"important");
            } else {
                div.style.setProperty("border-bottom-width", "1px thin","important");
            }
        }else{
            div.style.setProperty("border-bottom","");
        }
    }
    if ( Ext.getCmp("idImageWidth") ) {
        imagewidth = Ext.getCmp("idImageWidth").getValue();
        div.style.width = imagewidth + "px";
    }
    
    
    var fontSize;
    if ( Ext.getCmp("idFontSize")) {
        if ( Ext.getCmp("idFontSize").getValue() > 0 ) {
            fontSize =Ext.getCmp("idFontSize").getValue();
            div.style.fontSize=fontSize + "px";
        } else {
            div.style.fontSize="";
        }
    }
    var fontFamily;
    if ( Ext.getCmp("pagefontid")) {
        fontFamily =Ext.getCmp("pagefontid").getValue();
        div.style.fontFamily=fontFamily;
    }
    var fontWeight;
    if ( Ext.getCmp("idBoldText") ) {
        fontWeight = Ext.getCmp("idBoldText").getValue();
        if (fontWeight) {
            div.style.fontWeight="bold";
        } else {
            div.style.fontWeight="normal";
        } 
    }
    var fontStyle;
    if ( Ext.getCmp("idItalicText") ) {
        fontStyle = Ext.getCmp("idItalicText").getValue();
        if (fontStyle) {
            div.style.fontStyle="italic";
        } else {
            div.style.fontStyle="normal";
        } 
    }
    var value;
    if ( Ext.getCmp("idFieldValue") ) {
        if(div.attributes["type"] && div.attributes["type"].value != "3"){
            //            value = Ext.getCmp("idFieldValue").getValue(); 
            value = Ext.getCmp("idFieldValue").getValue().replace(/\s\s/g, doubleSpace); // replacing spaces to &nbsp;
            if ( value ) {
                div.innerHTML = value ;
                div.setAttribute("label",value);
            } else {
                div.innerHTML = "&nbsp;";
                div.setAttribute("label","&nbsp;");
            }
        }
    }
    
    var textLine;
    if ( Ext.getCmp("idTextLine") ) {
        textLine = Ext.getCmp("idTextLine").getValue();
        div.style.textDecoration = textLine;
    }
    
    var  textColor;
    if ( Ext.getCmp("colorpicker") ) {
        textColor = Ext.getCmp("colorpicker").value;
        div.style.color = textColor;
    }
    var defValue;
    if ( Ext.getCmp("iddefaultFieldValue")) {
        defValue = Ext.getCmp("iddefaultFieldValue").value != undefined ? Ext.getCmp("iddefaultFieldValue").value:'';
        div.setAttribute("defaultValue",defValue.replace(/\s/g,space));
    }
    var decimal;
    if ( Ext.getCmp("decimalprecisionglobaltableid")) {
        decimal = Ext.getCmp("decimalprecisionglobaltableid").value;
        div.setAttribute("decimalPrecision",decimal);
    }
    var marginTop;
    if ( Ext.getCmp("idtopmargin")) {
        marginTop = Ext.getCmp("idtopmargin").value;
        div.style.marginTop = marginTop + "px";
    }
    var marginBottom;
    if ( Ext.getCmp("idbottommargin")) {
        marginBottom = Ext.getCmp("idbottommargin").value;
        div.style.marginBottom = marginBottom + "px";
    }
    var marginLeft;
    if ( Ext.getCmp("idleftmargin")) {
        marginLeft = Ext.getCmp("idleftmargin").value;
        div.style.marginLeft = marginLeft + "px";
    }
    var marginRight;
    if ( Ext.getCmp("idrightmargin")) {
        marginRight = Ext.getCmp("idrightmargin").value;
        div.style.marginRight = marginRight + "px";
    }
    var verticalAlign;
    if ( Ext.getCmp("Valignselectcomboid")) {
        verticalAlign = Ext.getCmp("Valignselectcomboid").value;
        div.style.verticalAlign = verticalAlign;
        div.setAttribute("valign",verticalAlign);
    }
    var textAlign;
    if ( Ext.getCmp("idTextAlign") ) {
        textAlign = Ext.getCmp("idTextAlign").getValue();
        div.style.textAlign = textAlign;
        // for product image field only
        if ( textAlign == "left" && div.children.length > 0 && div.children[0].nodeName == "IMG") {
            div.style.marginLeft="0px";
            div.style.marginRight="auto";
        } else if ( textAlign == "center" && div.children.length > 0 && div.children[0].nodeName == "IMG") {
            div.style.marginLeft="auto";
            div.style.marginRight="auto";
        } else if ( textAlign == "right" && div.children.length > 0 && div.children[0].nodeName == "IMG") {
            div.style.marginLeft="auto";
            div.style.marginRight="0px";
        } 
    }
    var fieldAlign;
    if (Ext.getCmp("fields_Alignment")) {
        fieldAlign =  Ext.getCmp("fields_Alignment").getValue();
        if(div.children.length != 0 && div.children[0].nodeName == "IMG"){ //For image alignment - ERP-25033
            div.style.display = "block";
        } else{
            div.style.display = fieldAlign;
        }
        if(fieldAlign === "inline-table"){
            div.style.width = "";
        }
    }
           
}
function setPropertyPanelForLineItemFields (div,isGlobalTable,isImage) {
    var parent = div.parentNode;
    //    var fieldValue = div.innerHTML;
    var fieldValue = div.innerHTML.replace(/&nbsp;/g,' ');
    var width = div.style.width?div.style.width:100;
    var height = isImage?div.style.height?div.style.height:80:"";
    var imagewidth = isImage?div.style.width?div.style.width:80:"";
    var textAlign = div.style.textAlign?div.style.textAlign:"left";
    var fieldAlign = div.style.display?div.style.display:"inline-block";
    var fontSize = div.style.fontSize?div.style.fontSize:'';
    var fontFamily = div.style.fontFamily?div.style.fontFamily:'';
    var fontWeight = div.style.fontWeight?div.style.fontWeight == "bold":false;
    var fontStyle = div.style.fontStyle?div.style.fontStyle == "italic":false;
    var textLine = div.style.textDecoration?div.style.textDecoration:"none";
    var textColor = div.style.color?div.style.color:"#000000";
    var marginTop = div.style.marginTop?div.style.marginTop:"0px";
    var marginBottom = div.style.marginBottom?div.style.marginBottom:"0px";
    var marginLeft = div.style.marginLeft?div.style.marginLeft:"0px";
    var marginRight = div.style.marginRight?div.style.marginRight:"0px";
    var isFormula = div.attributes.getNamedItem("isformula") ? (div.attributes.getNamedItem("isformula").value == "true" ? true : false) : false;
    var isValueEditable = !div.attributes.getNamedItem("type") || (div.attributes.getNamedItem("type").value == 0) ? false : true; 
    if(isFormula){
        isValueEditable = false;
    }
    var isposttext = div.attributes.getNamedItem("isposttext") ? div.attributes.getNamedItem("isposttext").value : false;
    var ispretext = div.attributes.getNamedItem("ispretext") ? div.attributes.getNamedItem("ispretext").value : false;
    var verticalAlign = div.style.verticalAlign?div.style.verticalAlign:"middle";
    
    var bordertopstyle = div.style.borderTopStyle? div.style.borderTopStyle : "none";
    var borderbottomstyle = div.style.borderBottomStyle? div.style.borderBottomStyle : "none";
    var borderrightstyle = div.style.borderRightStyle? div.style.borderRightStyle : "none";
    var borderleftstyle = div.style.borderLeftStyle? div.style.borderLeftStyle : "none";
    var borderColor;
    if(div.style.borderLeftColor){
        borderColor = div.style.borderLeftColor?rgbToHex(div.style.borderLeftColor, true):'#000000';
    }else if(div.style.borderRightColor){
        borderColor = div.style.borderRightColor?rgbToHex(div.style.borderRightColor, true):'#000000';
    }else if(div.style.borderTopColor){
        borderColor = div.style.borderTopColor?rgbToHex(div.style.borderTopColor, true):'#000000';
    }else if(div.style.borderBottomColor){
        borderColor = div.style.borderBottomColor?rgbToHex(div.style.borderBottomColor, true):'#000000';
    }
    
    if ( isGlobalTable) {
        var defValue = div.attributes.getNamedItem("defaultValue") ? div.attributes.getNamedItem("defaultValue").value : "";
        if (Ext.getCmp("iddefaultFieldValue")) {
            Ext.getCmp("iddefaultFieldValue").setValue(defValue.replace(/&nbsp;/g,' '));
        }
    }
    var decimalPrecision = div.attributes.getNamedItem("decimalPrecision") ? div.attributes.getNamedItem("decimalPrecision").value : _amountDecimalPrecision;
    if (Ext.getCmp("decimalprecisionglobaltableid")) {
        Ext.getCmp("decimalprecisionglobaltableid").setValue(decimalPrecision);
    }
    
    
    if ( getEXTComponent('borderComboTop') ) {
        getEXTComponent('borderComboTop').setValue(bordertopstyle);
    }
    if ( getEXTComponent('borderComboBottom') ) {
        getEXTComponent('borderComboBottom').setValue(borderbottomstyle);
    }
    if ( getEXTComponent('borderComboLeft') ) {
        getEXTComponent('borderComboLeft').setValue(borderleftstyle);
    }
    if ( getEXTComponent('borderComboRight') ) {
        getEXTComponent('borderComboRight').setValue(borderrightstyle);
    }
    if ( getEXTComponent("idborderpicker") ) {
        getEXTComponent("idborderpicker").value = borderColor;
    }
    if ( getEXTComponent("idBorderPanel") ) {
        getEXTComponent("idBorderPanel").setBodyStyle("background-color",borderColor);
    }
    if ( Ext.getCmp("idFieldValue")) {
        Ext.getCmp("idFieldValue").setValue(fieldValue);
        Ext.getCmp("idFieldValue").setReadOnly(!isValueEditable);
    }
    if ( Ext.getCmp("Valignselectcomboid") ) {
        Ext.getCmp("Valignselectcomboid").setValue(verticalAlign);
    }
    if ( Ext.getCmp("idTextLine") ) {
        Ext.getCmp("idTextLine").setValue(textLine);
    }
    if ( Ext.getCmp("idtopmargin") ) {
        Ext.getCmp("idtopmargin").setValue(marginTop);
    }
    if ( Ext.getCmp("idbottommargin") ) {
        Ext.getCmp("idbottommargin").setValue(marginBottom);
    }
    if ( Ext.getCmp("idleftmargin") ) {
        Ext.getCmp("idleftmargin").setValue(marginLeft);
    }
    if ( Ext.getCmp("idrightmargin") ) {
        Ext.getCmp("idrightmargin").setValue(marginRight);
    }
    if ( Ext.getCmp("colorpicker") ) {
        Ext.getCmp("colorpicker").value = rgbToHex(textColor,false);
    }
    if ( Ext.getCmp("idSelectedTextPanel") ) {
        Ext.getCmp('idSelectedTextPanel').body.setStyle('background-color', rgbToHex(textColor,true));
    }
    if ( Ext.getCmp("idpostText") ) {
        Ext.getCmp("idpostText").setValue(isposttext);
    }
    if ( Ext.getCmp("idpreText") ) {
        Ext.getCmp("idpreText").setValue(ispretext);
    }
    if ( Ext.getCmp("idItalicText") ) {
        Ext.getCmp("idItalicText").setValue(fontStyle);
    }
    if ( Ext.getCmp("idBoldText") ) {
        Ext.getCmp("idBoldText").setValue(fontWeight);
    }
    if ( Ext.getCmp("idFontSize")) {
        Ext.getCmp("idFontSize").setValue(fontSize);
    }
    if ( Ext.getCmp("pagefontid")) {
        Ext.getCmp("pagefontid").setValue(fontFamily);
    }
    if (Ext.getCmp("fields_Alignment")) {
        Ext.getCmp("fields_Alignment").setValue(fieldAlign);
    }
    if ( Ext.getCmp("idTextAlign") ) {
        Ext.getCmp("idTextAlign").setValue(textAlign);
    }
    if ( Ext.getCmp("idWidth") ) {
        Ext.getCmp("idWidth").setValue(width);
    }
    if ( Ext.getCmp("idHeight") ) {
        Ext.getCmp("idHeight").setValue(height);
    }
    if ( Ext.getCmp("idImageWidth") ) {
        Ext.getCmp("idImageWidth").setValue(imagewidth);
    }
    if ( Ext.getCmp("idPretextSettingsfieldset") ) {
        if ( isValueEditable ) {
            Ext.getCmp("idPretextSettingsfieldset").hide();
        } else {
            Ext.getCmp("idPretextSettingsfieldset").show();
        }
    }
    if ( Ext.getCmp("idPosttextSettingsfieldset") ) {
        if ( isValueEditable ) {
            Ext.getCmp("idPosttextSettingsfieldset").hide();
        } else {
            Ext.getCmp("idPosttextSettingsfieldset").show();
        }
    }
    if(Ext.getCmp('idcustomdesignerfieldselectcombo')){
        if(isGlobalTable){
            var arr = div.attributes["attribute"].value.match(/\{PLACEHOLDER:(.*?)}/g);
            if (arr && arr[0]) {
                var matches = arr[0].replace(/\{|\}/gi, '').split(":");
                var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                Ext.getCmp('idcustomdesignerfieldselectcombo').select(rec);
            }
        }
    }
}
function addPositionObjectInCollection(component) {
    var originalConfig = {
        leftX: component.getBox().x,
        topY: component.getBox().y,
        rightX: component.getBox().right,
        bottom: component.getBox().bottom,
        pid: component.id
    }
    componentCollection.push(originalConfig);
}

function removePositionObjectFromCollection(currentcompid) {
    for (var cnt = 0; cnt < componentCollection.length; cnt++) {
        var collectionid = componentCollection[cnt].pid;
        if (collectionid == currentcompid) {
            componentCollection.splice(cnt, 1);
        }
    }
}
function showAlignedComponent(object, event) {
    var sectionWindow_Top = 0;
    var sectionWindow_Left = 0;
    if (Ext.getCmp('sectionPanelGrid')) {
        sectionWindow_Top = Ext.getCmp('sectionPanelGrid').getBox().top;
        sectionWindow_Left = Ext.getCmp('sectionPanelGrid').getBox().left - 7;
    } else {
        sectionWindow_Top = Ext.getCmp('pagelayout1').getBox().top;
        sectionWindow_Left = Ext.getCmp('pagelayout1').getBox().left;
    }
    var freezePixels = 1;
    for (var j = 0; j < componentCollection.length; j++)
    {
        //current selected panel for dragging
        var currentelemntid = object.el.id;
        var currentCollectionObject = Ext.getCmp(componentCollection[j].pid);
        //Check whether the draggable Component & Collection component is not same
        if (currentelemntid != componentCollection[j].pid)
        {
            if (((object.comp.getXY()[0] <= (currentCollectionObject.getXY()[0] + freezePixels)) && (object.comp.getXY()[0] >= (currentCollectionObject.getXY()[0] - freezePixels))))
            {
                //logic to draw the line
                //                console.log("Dimention matched...From-1st");
                object.comp.setPagePosition(currentCollectionObject.getXY()[0], object.comp.getPosition()[1]);
                isXMatched = true;
                //                                    lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line...
                lineobj.drawLine(currentCollectionObject.getXY()[0] - sectionWindow_Left, currentCollectionObject.getXY()[1] - sectionWindow_Top, object.comp.getXY()[0] - sectionWindow_Left, object.comp.getXY()[1] - sectionWindow_Top);
                lineobj.paint();
            }//1st_if

            else if (((object.comp.getXY()[0] <= (currentCollectionObject.getBox().right + freezePixels)) && (object.comp.getXY()[0] >= (currentCollectionObject.getBox().right - freezePixels))))
            {
                //logic to draw the line
                //                console.log("Dimention matched...From-3rd");
                object.comp.setPagePosition(currentCollectionObject.getBox().right, object.comp.getPosition()[1]);
                lineobj.clear();
                isrightMatched = true;
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line...
                lineobj.drawLine(currentCollectionObject.getBox().right - sectionWindow_Left, currentCollectionObject.getBox().bottom - sectionWindow_Top, object.comp.getXY()[0] - sectionWindow_Left, object.comp.getXY()[1] - sectionWindow_Top);
                lineobj.paint();
            }//2rd_if

            else if (((object.comp.getXY()[1] <= (currentCollectionObject.getXY()[1] + freezePixels)) && (object.comp.getXY()[1] >= (currentCollectionObject.getXY()[1] - freezePixels))))
            {
                //logic to draw the line
                //                console.log("Dimention matched...From-2nd");
                object.comp.setPagePosition(object.comp.getPosition()[0], currentCollectionObject.getXY()[1]);
                lineobj.clear();
                isYMatched = true;
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line...
                lineobj.drawLine(currentCollectionObject.getXY()[0] - sectionWindow_Left, currentCollectionObject.getXY()[1] - sectionWindow_Top, object.comp.getXY()[0] - sectionWindow_Left, object.comp.getXY()[1] - sectionWindow_Top);
                lineobj.paint();
            }//3nd_if

            else if (((object.comp.getXY()[1] <= (currentCollectionObject.getBox().bottom + freezePixels)) && (object.comp.getXY()[1] >= (currentCollectionObject.getBox().bottom - freezePixels))))
            {
                //logic to draw the line
                //                console.log("Dimention matched...From-4th");
                object.comp.setPagePosition(object.comp.getPosition()[0], currentCollectionObject.getBox().bottom);
                lineobj.clear();
                isbottomMatched = true;
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line...
                lineobj.drawLine(currentCollectionObject.getBox().right - sectionWindow_Left, currentCollectionObject.getBox().bottom - sectionWindow_Top, object.comp.getXY()[0] - sectionWindow_Left, object.comp.getXY()[1] - sectionWindow_Top);
                lineobj.paint();
            }//4_if

            else if (((object.comp.getBox().right <= (currentCollectionObject.getXY()[0] + freezePixels)) && (object.comp.getBox().right >= (currentCollectionObject.getXY()[0] - freezePixels))))
            {
                //logic to draw the X-Coordinate line
                //                console.log("Dimention matched...From-freezePixelsth");
                this.x1 = (currentCollectionObject.getXY()[0] - object.comp.getBox().width);
                object.comp.setPagePosition(this.x1, object.comp.getPosition()[1]);
                isrightMatched = true;
                lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line...
                lineobj.drawLine(currentCollectionObject.getXY()[0] - sectionWindow_Left, currentCollectionObject.getBox().bottom - sectionWindow_Top, object.comp.getBox().right - sectionWindow_Left, object.comp.getBox().y - sectionWindow_Top);
                lineobj.paint();
            }//5th_if
            else if (((object.comp.getBox().right <= (currentCollectionObject.getBox().right + freezePixels)) && (object.comp.getBox().right >= (currentCollectionObject.getBox().right - freezePixels))))
            {
                //logic to draw the line
                //                console.log("Dimention matched...From 6th");
                //                                        this.x2 = (object.comp.getBox().right - object.comp.getBox().width);
                object.comp.setPagePosition(currentCollectionObject.getBox().right - object.comp.getBox().width, object.comp.getPosition()[1]);
                isrightMatched = true;
                lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line...
                lineobj.drawLine(currentCollectionObject.getBox().right - sectionWindow_Left, currentCollectionObject.getBox().bottom - sectionWindow_Top, object.comp.getBox().right - sectionWindow_Left, object.comp.getXY()[1] - sectionWindow_Top);
                lineobj.paint();
            }//6th_if
            //
            else if (((object.comp.getBox().bottom <= (currentCollectionObject.getBox().bottom + freezePixels)) && (object.comp.getBox().bottom >= (currentCollectionObject.getBox().bottom - freezePixels))))
            {
                //logic to draw the X-Coordinate line
                //                console.log("Dimention matched...From-7th");
                this.y1 = (currentCollectionObject.getBox().bottom - object.comp.getBox().height);
                object.comp.setPagePosition(object.comp.getPosition()[0], this.y1);
                isbottomMatched = true;
                lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line...
                lineobj.drawLine(currentCollectionObject.getBox().right - sectionWindow_Left, currentCollectionObject.getBox().bottom - sectionWindow_Top, object.comp.getBox().x - sectionWindow_Left, object.comp.getBox().bottom - sectionWindow_Top);
                lineobj.paint();
            }//7th_if

            else if (((object.comp.getBox().bottom <= (currentCollectionObject.getXY()[1] + freezePixels)) && (object.comp.getBox().bottom >= (currentCollectionObject.getXY()[1] - freezePixels))))
            {
                //logic to draw the X-Coordinate line
                //                console.log("Dimention matched...From-8th");
                this.y2 = (currentCollectionObject.getXY()[1] - object.comp.getBox().height);
                object.comp.setPagePosition(object.comp.getPosition()[0], this.y2);
                isrightMatched = true;
                lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line...
                lineobj.drawLine(currentCollectionObject.getBox().right - sectionWindow_Left, currentCollectionObject.getXY()[1] - sectionWindow_Top, object.comp.getXY()[0] - sectionWindow_Left, object.comp.getBox().bottom - sectionWindow_Top);
                lineobj.paint();
            }//8th_if
            else
            {
                if (!isXMatched && !isYMatched && !isrightMatched && !isbottomMatched)
                {
                    isXMatched = false;
                    isYMatched = false;
                    isrightMatched = false;
                    isbottomMatched = false;
                    lineobj.clear();
                }
            }//else
        }//outer if
    }//for
    isXMatched = false;
    isYMatched = false;
    isrightMatched = false;
    isbottomMatched = false;
}

function removeAlignedLine(object, event) {
    //    console.log("From Dragstart event....");
    var currentpanelid = object.comp.id;
    for (var j = 0; j < componentCollection.length; j++) {
        var collectionid = componentCollection[j].pid;
        if (collectionid == currentpanelid) {
            indexObj = Ext.getCmp(collectionid);
            var updateConfig = {
                leftX: indexObj.getBox().x,
                topY: indexObj.getBox().y,
                rightX: indexObj.getBox().right,
                bottom: indexObj.getBox().bottom,
                pid: collectionid
            }//updateConfig
            componentCollection[j] = updateConfig;
            break;
        }//if
    }//for
    isXMatched = false;
    isYMatched = false;
    isrightMatched = false;
    isbottomMatched = false;
    lineobj.clear();
    return true;
}

function removeAbsoluteLayout() {
    var tmp2 = document.getElementById("sectionPanelGrid-innerCt");
    if (tmp2.childNodes.length > 2)
    {
        for (var i = 2; i < tmp2.childNodes.length; i++)
        {
            var newDiv = tmp2.childNodes[i];
            newDiv.className = newDiv.className.replace(/(?:^|\s)x-abs-layout-item(?!\S)/, '');

        }
    }

}

function removeConfluenceBorder() {
    var tmp2 = document.getElementById("sectionPanelGrid-innerCt");
    if (tmp2.childNodes.length > 2)
    {
        for (var i = 2; i < tmp2.childNodes.length; i++)
        {
            var newDiv = tmp2.childNodes[i];
            newDiv.innerHTML = newDiv.innerHTML.replace("tableCellBorder", "");
        }
    }

}

function addConfluenceBorder() {
    var tmp2 = document.getElementById("sectionPanelGrid-innerCt");
    if (tmp2.childNodes.length > 2)
    {
        for (var i = 2; i < tmp2.childNodes.length; i++)
        {
            var newDiv = tmp2.childNodes[i];
            newDiv.className = newDiv.className.replace(/(?:^|\s)x-abs-layout-item(?!\S)/, '');
        }
    }

}

function getContextMenu(event, obj, row,column,rowCount,columnCount){
    event.stopPropagation();
    event.preventDefault();
    if (selectedElement != null || selectedElement != undefined)
    {
        if(Ext.get(selectedElement)!=null){
            Ext.get(selectedElement).removeCls("selected");
        }
    }
    selectedElement = this.id;
    var contextMenu1 = Ext.create('Ext.menu.Menu', {
        items: [
        {
            text: 'Insert Row',
            obj:obj,
            row:row,
            column:column,
            rowCount:rowCount,
            columnCount:columnCount,
            handler: function (menubuttonObj) {
                var obj= menubuttonObj.obj;
                var row= menubuttonObj.row;
                var column= menubuttonObj.column;
                var rowCount= menubuttonObj.rowCount;
                var columnCount= menubuttonObj.columnCount;
                var table = obj.parentNode.parentNode.parentNode;
                var rowSpan = obj.rowSpan;
                var style= obj.getAttribute('style');
                var tr = table.insertRow(row+rowSpan);
                for(var colCnt = 0; colCnt < columnCount; colCnt++){
                    var td = document.createElement('td');
                    td.innerHTML = "&nbsp;";
                    td.setAttribute('style', style);
                    td.setAttribute('onmouseover', 'getval1()');
                    td.setAttribute('onmouseleave', 'destroydiv(this)'); //ERP-19169 : On inserting Row/Column in global table,edit icon button is loading lately
                    td.setAttribute('oncontextmenu', 'getContextMenu()');
                    tr.appendChild(td);
                }
                updateTableConfigs(table);
                return true;
            }
        },
        {
            text: 'Insert Column',
            obj:obj,
            row:row,
            column:column,
            rowCount:rowCount,
            columnCount:columnCount,
            handler: function (menubuttonObj) {
                var obj= menubuttonObj.obj;
                var row= menubuttonObj.row;
                var column= menubuttonObj.column;
                var rowCount= menubuttonObj.rowCount;
                var columnCount= menubuttonObj.columnCount;
                var table = obj.parentNode.parentNode.parentNode;
                var tableRows = table.rows;
                var style= obj.getAttribute('style');
                for(var rowCnt = 0 ; rowCnt < tableRows.length ; rowCnt++ ){
                    var currentTableRow = tableRows[rowCnt];
                    var td = currentTableRow.insertCell(column+1);
                    td.innerHTML = "&nbsp;";
                    td.setAttribute('style', style);
                    td.setAttribute('onmouseover', 'getval1()');
                    td.setAttribute('onmouseleave', 'destroydiv(this)'); //ERP-19169 : On inserting Row/Column in global table,edit icon button is loading lately
                    td.setAttribute('oncontextmenu', 'getContextMenu()');
                }
                updateTableConfigs(table);
                return true;
            }
        },
        {
            text: 'Copy Row',
            obj:obj,
            row:row,
            column:column,
            rowCount:rowCount,
            columnCount:columnCount,
            handler: function (menubuttonObj) {
                var obj= menubuttonObj.obj;
                var row= menubuttonObj.row;
                var column= menubuttonObj.column;
                var rowCount= menubuttonObj.rowCount;
                var columnCount= menubuttonObj.columnCount;
                var table = obj.parentNode.parentNode.parentNode;
                var rowSpan = obj.rowSpan;
                var tr = table.insertRow(row+rowSpan);
                var originalTrData =table.rows[row].cells;
                for(var colCnt = 0; colCnt < originalTrData.length; colCnt++){
                    if(originalTrData[colCnt]){
                        var cell = originalTrData[colCnt];
                        var innerHtml = cell.innerHTML;
                        var colspan = cell.colSpan;
                        var style = cell.getAttribute('style');
                        var td = document.createElement('td');
                        td.innerHTML = innerHtml;
                        td.colSpan = colspan;
                        td.setAttribute('style', style);
                        td.setAttribute('onmouseover', 'getval1()');
                        td.setAttribute('onmouseleave', 'destroydiv(this)'); //ERP-19169 : On inserting Row/Column in global table,edit icon button is loading lately
                        td.setAttribute('oncontextmenu', 'getContextMenu()');
                        tr.appendChild(td);
                    }
                }
                updateTableConfigs(table);
            }
        },
        {
            text: 'Delete Row',
            obj:obj,
            row:row,
            column:column,
            rowCount:rowCount,
            columnCount:columnCount,
            handler: function (menubuttonObj) {
                var obj= menubuttonObj.obj;
                var row= menubuttonObj.row;
                var column= menubuttonObj.column;
                var rowCount= menubuttonObj.rowCount;
                var columnCount= menubuttonObj.columnCount;
                var table = obj.parentNode.parentNode.parentNode;
                if(table.rows[row]){
                    var tableRow = table.rows[row];
                    var cells = tableRow.cells[column];
                    var rowSpan = cells.rowSpan;
                    var style = cells.getAttribute('style');
                    if(rowSpan > 1){
                        for(var rowSpanCnt=1; rowSpanCnt<rowSpan; rowSpanCnt++ ){
                            var nextTableRow= table.rows[row + rowSpanCnt];
                            var td = nextTableRow.insertCell(column);
                            td.innerHTML = "&nbsp;";
                            td.setAttribute('style', style);
                            td.setAttribute('onmouseover', 'getval1()');
                            td.setAttribute('onmouseleave', 'destroydiv(this)'); //ERP-19169 : On inserting Row/Column in global table,edit icon button is loading lately
                            td.setAttribute('oncontextmenu', 'getContextMenu()');
                        }
                    }
                    //                    tableRow.remove();
                    remove(tableRow);             //Done for IE.
                }
                updateTableConfigs(table);
                return true;
            }
        },
        {
            text: 'Delete Column',
            obj:obj,
            row:row,
            column:column,
            rowCount:rowCount,
            columnCount:columnCount,
            handler: function (menubuttonObj) {
                var obj= menubuttonObj.obj;
                var row= menubuttonObj.row;
                var column= menubuttonObj.column;
                var rowCount= menubuttonObj.rowCount;
                var columnCount= menubuttonObj.columnCount;
                var table = obj.parentNode.parentNode.parentNode;
                var tableRows = table.rows;
                for(var rowCnt = 0 ; rowCnt < tableRows.length ; rowCnt++ ){
                    var currentTableRow = tableRows[rowCnt];
                    var cell = currentTableRow.cells[column];
                    //                    cell.remove();
                    remove(cell);             //Done for IE.
                }
                updateTableConfigs(table);
                return true;
            }
        },
        {
            text:'Copy Table',
            handler: function() {
                var table = obj.parentNode.parentNode.parentNode;
                var parentId = table.parentNode.id;
                elementJson = createJson(Ext.getCmp(parentId));
            }
        },
        {
            text:'Remove',
            handler: function() {
                var table = obj.parentNode.parentNode.parentNode;
                var tableCmpId = table.parentNode.id;
                var cmp = Ext.getCmp(tableCmpId);
                if ( isExtendedGlobalTable ) {
                    if (cmp && cmp.isExtendLineItem) {
                        isExtendedGlobalTable = false;
                    }
                }
                var parentId = table.parentNode.id;
                removeObject(parentId);
            }
        }
        ]
    });
    var left = event.clientX;
    var top = event.clientY;
    contextMenu1.showAt(left,top);
}

function updateTableConfigs( table ){
    /* This function is used to set global table's getVal1() and getContextMenu() functions*/
    var tableRowCount = table.rows.length;
    for( var rows=0 ; rows < tableRowCount ; rows++){
        var cells = table.rows[rows].cells;
        var columnCnt = cells.length;
        for( var columns=0 ; columns < cells.length ; columns++){
            var getValRegex= /getval1\([^\)]*\)(\.[^\)]*\))?/g;
            var getContextRegex= /getContextMenu\([^\)]*\)(\.[^\)]*\))?/g;
            var newGetValFunction = "getval1(event,this,"+rows+","+columns+","+tableRowCount+","+columnCnt+")";
            var newGetContextMenuFunction = "getContextMenu(event,this,"+rows+","+columns+","+tableRowCount+","+columnCnt+")";
            var outerHtml = cells[columns].outerHTML;
            outerHtml = outerHtml.replace(getValRegex,newGetValFunction);
            outerHtml = outerHtml.replace(getContextRegex,newGetContextMenuFunction);
            cells[columns].outerHTML = outerHtml;
        } 
    }
}