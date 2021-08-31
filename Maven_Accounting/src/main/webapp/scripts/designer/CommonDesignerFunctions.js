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
    if(designerPanel.id!="pagelayout1"){
        Ext.getCmp('propertypanel').enable();
    
        Ext.getCmp('propertiesbox1').setDisabled(true);
        if (obj.initialCls && obj.initialCls.indexOf("tpl-content") > -1) {// it is line item field
            content = "Line Items";
        } else if (fieldType == Ext.fieldID.insertImage) {//
            content = "Image";
        } else if (fieldType == Ext.fieldID.insertHLine) {//
            content = "Horizontal Line";
        }
        else if (fieldType == '15') {
            content = "Amount in words";
        } else if (fieldType == '16') {
            content = "PostText Master";
        } else if (fieldType == '17') {
            content = "Created by";
        } else if(fieldType == Ext.fieldID.insertGlobalTable){
            content = "Global Table";
        }
        Ext.getCmp('setfieldproperty').setValue(content);
        Ext.getCmp('hidden_fieldID').setValue(obj.id);
        Ext.getCmp('hidden_fieldType').setValue(obj.fieldTypeId);
        Ext.getCmp('hidden_allowformatting').setValue(obj.fieldTypeId != Ext.fieldID.insertTable || obj.fieldTypeId != Ext.fieldID.insertGlobalTable);
        var myEditor = Ext.getCmp('myDesignFieldEditor');
        myEditor.hide();
        Ext.getCmp('editformattingbtn').hide();
        if (fieldType != Ext.fieldID.insertTable && fieldType != Ext.fieldID.insertImage && fieldType != Ext.fieldID.insertHLine && fieldType !=Ext.fieldID.insertGlobalTable) {// if not line items, image or horizontal line, then show htmleditor
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

        if (fieldType == Ext.fieldID.insertText || fieldType == Ext.fieldID.insertField ||fieldType == Ext.fieldID.insertDrawBox|| fieldType == Ext.fieldID.insertTable || fieldType == Ext.fieldID.insertGlobalTable) {// if line item, show edit button and on click on line item configuration window
            if(fieldType == Ext.fieldID.insertDrawBox || fieldType == Ext.fieldID.insertText || fieldType == Ext.fieldID.insertField){
                Ext.getCmp('propertiesbox1').setDisabled(false);
            }else{
                Ext.getCmp('editformattingbtn').show();
            }    

            if (fieldType == Ext.fieldID.insertTable ) {// if line item, show edit button and on click on line item configuration window
                Ext.getCmp('editformattingbtn').show();
            }

            if (fieldType == Ext.fieldID.insertGlobalTable) {// if Global Table, hide edit button and on click on line item configuration window
                Ext.getCmp('editformattingbtn').hide();
            }
        }
    }
}

function clearHiddenFieldID () {
    Ext.getCmp('hidden_fieldID').setValue('');
}
function createHoLineComponent(designerPanel, fieldTypeId, propertyPanel, X, Y, widthnew) {
    var field = Ext.create('Ext.Component', {
        x: X,
        y: Y,
        height: 20,
        width: (widthnew == undefined ||widthnew ==0) ? 180 : widthnew,
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
        initDraggable: function() {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        },//initDraggable
        resizable: true,
        fieldTypeId: fieldTypeId,
        html: "<hr>",
        onRender: function() {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function(eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if(component && Ext.getCmp('contentImage')) {
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
            onMouseUp: function(field) {
                field.focus();
            },
            removed: function() {
                onDeleteUpdatePropertyPanel();
                //                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            }
        }
    });
    field.on('drag',showAlignedComponent, field);
    field.on('dragend',removeAlignedLine, field);
    return field;
}

function createGlobalTable(X, Y, tablehtml,designerPanel, propertyPanel,widthnew,heightnew,fixedrowvalue) {
    var field = Ext.create('Ext.Component', {
        x: X,
        y: Y,
        height: (heightnew == undefined ||heightnew ==0) ? 180 : heightnew,
        width: (widthnew == undefined ||widthnew ==0) ? 180 : widthnew,
        fixedrowvalue:fixedrowvalue,
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
        initDraggable: function() {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        },//initDraggable
        resizable: true,
        fieldTypeId: 12,
        html: tablehtml,
        onRender: function() {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function(eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if (component &&  Ext.getCmp('contentImage')) {
                    Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            });
        },
        listeners: {
            onMouseUp: function(field) {
                field.focus();
            },
            removed: function() {
                onDeleteUpdatePropertyPanel();
                //                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            }
        }
    });
    field.on('drag',showAlignedComponent, field);
    field.on('dragend',removeAlignedLine, field);
    return field;
}

function createDrawComponent(designerPanel, propertyPanel, fieldTypeId, changedhtml, x, y,widthnew,heightnew,backgroundColor) {
    var field = Ext.create('Ext.Component', {
        x: x,
        y: y,
        width: (widthnew==0 || widthnew==undefined)?60:widthnew,
        height: (heightnew==0|| heightnew==undefined)?40:heightnew,
        style: {
            bodyStyle: '',
            borderColor: '#B5B8C8',
            borderStyle: 'solid',
            borderWidth: '1px',
            position: 'absolute',
            backgroundColor:backgroundColor
        },
//        id: 'configuredrawboxtoset',
        draggable: true,
        initDraggable: function() {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        },//initDraggable
        resizable: true,
        fieldTypeId: fieldTypeId,
        html: changedhtml!=undefined?changedhtml:'drawbox',
        onRender: function() {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function(eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if(component && Ext.getCmp('contentImage')) {
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
            onMouseUp: function(field) {
                field.focus();

            },
            removed: function() {
                onDeleteUpdatePropertyPanel();
            }
        }
    });
    field.on('drag',showAlignedComponent, field);
    field.on('dragend',removeAlignedLine, field);
    return field;
}

//delete
function deleteCustomDesignField() {
    if (isValidFieldSelected()) {
        var selectedField=Ext.getCmp('setfieldproperty').getValue();
        Ext.MessageBox.show({
            title: 'Confirm',
            msg: 'Do you really want to delete "'+selectedField+'" field?',
            icon: Ext.MessageBox.QUESTION,
            buttons: Ext.MessageBox.YESNO,
            scope: this,
            fn: function(button) {
                var designerp = Ext.getCmp('sectionPanelGrid');
                if (button == 'yes')
                {   
                    var obj = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                    if(obj.fieldTypeId){
                        Ext.tableinsert=false;
                    }
                    removePositionObjectFromCollection(Ext.getCmp('hidden_fieldID').getValue());
                    designerp.remove(obj, true);
                } else {
                    return;
                }
            }
        });
    }
}

function enableSectionWindowButtons () {
    
}

//Used for Copy Button            
function copyButton(designerpanel,propertypanel)
{
    if(isValidFieldSelected() && Ext.getCmp('hidden_allowformatting').getValue()=='true') {
        var obj =Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
        var items = [];
        var initconfig = obj.initialConfig;
        var fieldTypeId = obj.fieldTypeId;
        if (fieldTypeId != Ext.fieldID.insertTable) {// check for line items (id==5). 
            if (fieldTypeId != '3') {// check for image (id==3).
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
            this.copyfield=items;
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
function pasteButton(designerPanel,propertyPanel){
      var PosX = designerPanel.cursorCustomX;
       var PosY = designerPanel.cursorCustomY;
       var arr =this.copyfield;
        for (var cnt = 0; cnt < arr.length; cnt++) {
           var obj = arr[cnt];
           if (obj.fieldTypeId == '5') {
               var field = configureItemList(obj, designerPanel, propertyPanel)
            } else if (obj.fieldTypeId == '3') {
               field = createExtImgComponent(designerPanel, propertyPanel, obj.fieldTypeId, obj.src, obj.x, obj.y, obj);
           } else if (obj.fieldTypeId == '4') {
               field = createHoLineComponent(designerPanel, obj.fieldTypeId, propertyPanel, PosX, PosY,obj.width);
           } else {
                var labelHTML = obj.fieldTypeId != '2' ? obj.labelhtml : "<span attribute='{PLACEHOLDER:" + obj.fieldid + "}'>" + obj.labelhtml + "</span>";
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
        if(obj.id =="contentImage" || obj.id =="linecanvas")
            continue;
        
        var initconfig = obj.initialConfig;
        var fieldTypeId = obj.fieldTypeId;
        if (fieldTypeId !== Ext.fieldID.insertTable && fieldTypeId !== Ext.fieldID.insertGlobalTable) {// check for line items (id==5). 
            if (fieldTypeId != Ext.fieldID.insertImage && fieldTypeId != Ext.fieldID.insertDrawBox ) {// check for image (id==3).
                var label = obj.el.dom.firstChild.innerHTML == undefined ? obj.el.dom.firstChild.nodeValue :
                        (obj.el.dom.firstChild.innerHTML.indexOf("#") == 0 ? obj.el.dom.firstChild.innerHTML : obj.el.dom.firstChild.outerHTML);
                var config = {id: obj.id, x: obj.x, y: obj.y, labelhtml: label,
                    height: obj.height,
                    width: obj.width,
                    fieldTypeId: obj.fieldTypeId,selectfieldbordercolor:obj.style ? obj.style.borderColor : ''};
            } else if(fieldTypeId == Ext.fieldID.insertDrawBox){ // If Drawbox
                var label = obj.el.dom.firstChild.innerHTML == undefined ? obj.el.dom.firstChild.nodeValue :
                        (obj.el.dom.firstChild.innerHTML.indexOf("#") == 0 ? obj.el.dom.firstChild.innerHTML : obj.el.dom.firstChild.outerHTML);
                      
                var config = {id: obj.id, x: obj.x, y: obj.y, labelhtml: label,
                    height: obj.height,
                    width: obj.width,
                    fieldTypeId: obj.fieldTypeId,backgroundColor:obj.el.dom.style ? obj.el.dom.style.backgroundColor : ''};
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
                if(fieldTypeId<"25"){
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
            }else{
                 config['fieldid'] = config.id;
                 config['label'] = config.label;
                 config['xtype'] = config.xtype;
            }
          }
            items.push(config);
        } else if (fieldTypeId == Ext.fieldID.insertGlobalTable) {
//            var content = obj.el.getHTML();
            var elements = Ext.get(obj.id).select('div.x-resizable-handle').elements;
            for(var i=0;i<elements.length;i++){
                elements[i].remove()
            }
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
            config = {id: obj.id, x: obj.x, y: obj.y,labelhtml: content, 
                    height: Ext.get(obj.id).getBox().height,
                    width: Ext.get(obj.id).getBox().width,
                    fieldTypeId: obj.fieldTypeId,cellplaceholder:lineitems,fixedrowvalue:obj.fixedrowvalue};
            items.push(config);
        } else {
            var elements = Ext.get(obj.id).select('div.x-resizable-handle').elements;
            if(elements){
                for(var i=0;i<elements.length;i++){
                    elements[i].remove()
                }
            }
            /*new Line items of table-neeraj*/
            var content = obj.el.getHTML();
            var listItems = Ext.get('itemlistconfig'+designerPanel.id);
            if(listItems.dom.attributes[5]!=undefined && listItems.dom.attributes[4]!=undefined){
                this.tcolor=listItems.dom.attributes[5].nodeValue;
                this.bmode=listItems.dom.attributes[4].nodeValue
            }
            if (listItems && listItems.dom.childNodes[0].nextElementSibling.rows[0].cells.length > 0) {
                var lineitems = [];
                var childItems = listItems.dom.childNodes[0].nextElementSibling.rows[0].cells;
                for (var itemcnt = 0; itemcnt < childItems.length; itemcnt++) {
                    var child = childItems[itemcnt];
                    var colSetting = child.attributes;
                    var childConfig = {
                        'fieldid': colSetting[8].value, 
                        'label':  colSetting[0].value
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
                    width:Ext.get(obj.id).getBox().width, 
                    fieldTypeId: obj.fieldTypeId,
                    labelhtml: content,
                    tablecolor:this.tcolor,
                    tablebordermode:this.bmode
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


function saveAsWindow(json, html, defaultTemplate,saveflag,pagelayoutproperty) {
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
            handler: function() {    
                var templatename=Ext.getCmp('templatenamefield').getValue(); 
                var pagelayoutproperty1=Ext.JSON.encode(pagelayoutproperty);
                saveCustomDesign(json, html, defaultTemplate,saveflag,templatename,pagelayoutproperty1, false);
                Ext.getCmp('saveaswin').close();
            },
            scope: this
        },{
            text: 'Cancel',
            handler: function() {
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
function saveCustomDesign(json, html, defaultTemplate,saveflag,templatename,pagelayoutproperty, allowReLoad,bandID) {
    Ext.Ajax.request({
        url : "CustomDesign/saveDesignTemplate.do",
        params: {
            moduleid:_CustomDesign_moduleId,
            templateid : _CustomDesign_templateId, 
            templatename:templatename,
            json : json,
            html : html,
            isdefault : defaultTemplate,
            saveasflag:saveflag,
            pagelayoutproperty:pagelayoutproperty,
            bandid : bandID
                    }, 
        success : function(response, req) {
            var result = Ext.decode(response.responseText);
            if(result.success && isValidSession(result)) {
                if(allowReLoad )
                    location.reload();
                else if(bandID!=Ext.bandID.footer||bandID!=Ext.bandID.header)
                    WtfComMsgBox(["Message",result.data.msg], 0);                    
            }
        }
    })
}

function savePageForm() {
    var top,right,bottom,left,isportrait,pagefont;
    var ispagenumber=false,pagenoformat,pagenoalign;
    var portrait=Ext.getCmp('portrait');
    var landscape=Ext.getCmp('landscape');
    var pagenumber=Ext.getCmp('pagenumbercheck');
    var pagefooter=Ext.getCmp('pagefootercheck');
    var pagefontstyle=Ext.getCmp('pagefontstyleid');
 
    if(!portrait.collapsed==true){
        isportrait=true;
        top=Ext.getCmp('portraittop').getValue(); 
        right=Ext.getCmp('portraitright').getValue(); 
        bottom=Ext.getCmp('portraitbottom').getValue(); 
        left=Ext.getCmp('portraitleft').getValue(); 
    }
    if(!landscape.collapsed==true){
        isportrait=false;
        top=Ext.getCmp('landscapetop').getValue(); 
        right=Ext.getCmp('landscaperight').getValue(); 
        bottom=Ext.getCmp('landscapebottom').getValue(); 
        left=Ext.getCmp('landscapeleft').getValue();  
    }
    var ispagefooter= "";
     if(!pagefooter.collapsed==true){
         ispagefooter=true;
    var isHAlign=Ext.getCmp('horizontalalign').getValue()?true:false;
    var hAlign=Ext.getCmp('horizontalalign').getValue();
    var headerheight=Ext.getCmp('headerheight').getValue();
    var footerheight=Ext.getCmp('footerheight').getValue();
    var isVAlign=Ext.getCmp('verticalalign').getValue()?true:false;
    var vAlign=Ext.getCmp('verticalalign').getValue();
     }
        if(!pagenumber.collapsed==true){
        if(Ext.getCmp('pagenumberformat').getValue()){
            ispagenumber=true;
            pagenoformat=Ext.getCmp('pagenumberformat').getValue();
        }else{
            pagenoformat="";
        } 
        if(Ext.getCmp('pagenumberalignment').getValue())
            pagenoalign=Ext.getCmp('pagenumberalignment').getValue();
        else
            pagenoalign="";
    }
    if(!pagefontstyle.collapsed==true){
        pagefont=Ext.getCmp('pagefontid').getValue();
        Ext.get('pagelayout1-innerCt').setStyle('font-family',''+pagefont+'');
    }else{
        pagefont="sans-serif";
        Ext.get('pagelayout1-innerCt').setStyle('font-family',''+pagefont+'');
    }
    this.pagejson={
        "isportrait":isportrait,
        "top": top,
        "right": right,
        "bottom":bottom,
        "left":left ,       
        "pagefooter":{
            "ispagefooter":ispagefooter,
            "ishalign":isHAlign,       
            "halign":hAlign,
            "isvalign":isVAlign,
            "valign":vAlign,
            "footerheight":footerheight,
            "headerheight":headerheight
        },
        "pagenumber":{
            "ispagenumber":ispagenumber,
            "pagenumberformat":pagenoformat,       
            "pagenumberalign":pagenoalign           
        },
        "pagefontstyle":{
            "fontstyle":pagefont
        }
    };  
//    var returnConfig= Ext.JSON.encode(this.pagejson);        
    return this.pagejson;       
}


function getlineitems(designerPanel,propertyPanel){
    var  field = Ext.create(Ext.TemplateHolder, {
        x: 0,
        y:150,
        draggable: true,
        initDraggable: function() {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        },//initDraggable
        containerId : designerPanel.id,
        //                                width : 650,
        height: 45,
        fieldTypeId: Ext.fieldID.insertTable,
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
            onMouseUp: function(field) {
                field.focus();
            },
            removed: function() {
                onDeleteUpdatePropertyPanel();
            //                                        propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            },
            afterrender: function() {
                addPositionObjectInCollection(this);
                this.el.on('click', function(eventObject, target, arg) {
                    var component = designerPanel.queryById(this.id)
                    if (component  && Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                        eventObject.stopPropagation();
                        getPropertyPanel(component, designerPanel, propertyPanel);
                    }
                });
            }
        }
    });
    field.on('drag',showAlignedComponent, field);
    field.on('dragend',removeAlignedLine, field);
    return field;
}
function getTopHtml(text, body,img,isgrid,margin){
    if(isgrid===undefined)isgrid=false;
    if(margin===undefined)margin='15px 0px 10px 10px';
     if(img===undefined||img==null) {
        img = '../../images/createuser.png';
    }
     var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
                    +"<div style='float:left;height:100%;width:auto;position:relative;'>"
                    +"<img src = "+img+"  class = 'adminWinImg'></img>"
                    +"</div>"
                    +"<div style='float:left;height:100%;width:70%;position:relative;'>"
                    +"<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>"+text+"</b></div>"
                    +"<div style='font-size:10px;float:left;margin:15px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>"
                    +"</div>"
                    +"</div>" ;
     return str;
}
Ext.designtable = function(config) {
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
    title : 'Insert Table',
    width : 400,
    height : 250,
    bodyStyle: 'background: #F5F5F5;',
    onRender: function(config) {
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
    createForm: function() {

        this.TypeForm = new Ext.form.FormPanel({
            region: 'center',
            bodyStyle: 'margin-left:10px;',
            anchor :'100%',
            autoScroll: true,
            border: false,
            bodyStyle: 'background: #F5F5F5;',
            defaultType: 'textfield',
            items: [{
                    xtype: 'fieldset',
                    flex: 1,
                    title: 'Table Type',
                    defaultType: 'radio', // each item will be a radio button
                    layout: 'anchor',
                    defaults: {
                        anchor: '80%',
                        hideEmptyLabel: false
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
                            disabled:( _CustomDesign_moduleId=="10" || (_CustomDesign_moduleId=="12" && !(_CustomDesign_templateSubtype=="1")) || _CustomDesign_moduleId=="14" || _CustomDesign_moduleId=="16" ) ? true : false
                        }]
                }]
        });
    },
            
    closeForm : function() {
        this.close();
        this.destroy();
    },
    saveForm: function() {
        var rec = this.TypeForm.getForm().getValues();
        this.value = rec.rectype;
        this.closeForm();
        if (this.value == 2)
        {
            var designp = Ext.getCmp('sectionPanelGrid');
            var propanel = Ext.getCmp('propertypanel');
            if( Ext.tableinsert==true){
                Ext.Msg.alert('LineItems','Sorry only one lineitem is applicable at a time' );
            }else{
                openProdWindowAndSetConfig(this, this.containerId);
            }
//            var field = getlineitems(designp, propanel);
//            designp.items.add(field);
//            designp.doLayout();
        } else  if (this.value == 1) {
            addGlobalTable(this.parentScope);
        } 
        
    }
});

function saveTableProperty(selectedcolor,selectedmode) {
    var selectpickercolor=selectedcolor;
    var borderselectedmode=selectedmode;
     Ext.borderproperties={"tableproperties":{"bordercolor":selectpickercolor,"borderstylemode":borderselectedmode}};
     var returnConfig=Ext.borderproperties; 
    return returnConfig;   
}

Ext.tablepanel= function(config){
    Ext.apply(this,{
                width:520,
//                autoHeight:true,
                layout:'column',
                bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
                id:'tablepanel',
                items:[{   
                    xtype:'fieldset',
                    height: 50,  
                    title:'Border ',
                    layout:'column',
                    width: 120,
                    items:[
                    this.newmenubtn = Ext.create('Ext.Button', {
                        id:'newmenubtn'  ,  
                        menu: this.colortypemenu= Ext.create('Ext.menu.ColorPicker', {
//                            xtype: 'colormenu',
                            value: '000000',
                            id:'colorpicker',
                            handler: function (obj, rgb) {
                               
                                //this.selectedcolor='FFFFFF';
                                Ext.colorpicker=Ext.getCmp('colorpicker'); 
                                var selectednewcolor="#"+rgb.toString();
                                Ext.colorpicker.value=selectednewcolor;
                            } // handler
                        }), // menu
//                        renderTo: Ext.getBody(),
                        text: 'Choose Color'
                    })]
                },  {
                    xtype: 'fieldset',
                    columns: 4,
                    title: 'BorderStyle',
                    width: 400,
                    height: 200,  
                    bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
                    items: [
            this.border1img=Ext.create('Ext.Button', {//by creating we get the object--by xtype we don't get the object'
                //                        xtype: 'button',
                enableToggle: true,
                scale : 'large',
                scope : this,
                rowspan: 3,
                height: 63,
                width: 74,
                id:'border1Img',
                iconCls : 'border1Img ',
                cls:'table,td',
                enableToggle: true,
                autoHeight : true,
                pressed:true,
                margin: '10 20 30 50',
                toggleGroup: 'ratings'
            }), 
            this.border2img=Ext.create('Ext.Button', {
                //                xtype: 'button',
                enableToggle: true,
                scale : 'large',
                scope : this,
                rowspan: 3,
                height: 63,
                width: 74,
                id:'border2Img',
                iconCls : 'border2Img ',
                enableToggle: true,
                autoHeight : true,
                         
                margin:'10 20 30 20',
                toggleGroup: 'ratings'
            }),
            this.border3img=Ext.create('Ext.Button', {
                //                xtype: 'button',
                enableToggle: true,
                scale : 'large',
                rowspan: 3,
                height: 63,
                width: 74,
                id:'border3Img',
                iconCls : 'border3Img ',
                scope : this,
                margin:'10 20 30 20',
                enableToggle: true,
                autoHeight : true,
                toggleGroup: 'ratings'
            }),
            this.border4img=Ext.create('Ext.Button', {//by creating we get the object--by xtype we don't get the object'
                //                        xtype: 'button',
                enableToggle: true,
                scale : 'large',
                scope : this,
                rowspan: 3,
                height: 63,
                width: 74,
                id:'border4Img',
                iconCls : 'border4Img ',
                cls:'table,td',
                enableToggle: true,
                autoHeight : true,
                margin: '10 20 30 50',
                toggleGroup: 'ratings'
            }) 
            ]
        }]
            },config);  
    if(config.tablebordercolorconfig){
        this.colortypemenu.picker.select(config.tablebordercolorconfig);
        this.colortypemenu.picker.value=config.tablebordercolorconfig;
    } 
    if(config.tablebordermodeconfig) {
        if(config.tablebordermodeconfig=="borderstylemode1"){
            this.border2img.pressed=false;
            this.border3img.pressed=false;
            this.border4img.pressed=false;
            this.border1img.pressed=true;
        }else if(config.tablebordermodeconfig=="borderstylemode2"){
            this.border1img.pressed=false;
            this.border3img.pressed=false;
            this.border4img.pressed=false;
            this.border2img.pressed=true;           
        } else if(config.tablebordermodeconfig=="borderstylemode3"){
            this.border1img.pressed=false;
            this.border2img.pressed=false;
            this.border4img.pressed=false;
            this.border3img.pressed=true;           
        }else if(config.tablebordermodeconfig=="borderstylemode4"){
            this.border1img.pressed=false;
            this.border2img.pressed=false;
            this.border3img.pressed=false; 
            this.border4img.pressed=true;
        }
    }
    Ext.tablepanel.superclass.constructor.call(this, config)
}
            
Ext.tablepanelCmp=Ext.extend(Ext.tablepanel, Ext.Panel, {
   autoHeight:true
});
this.tpanel=new Ext.tablepanel({});

function addGlobalTable(parentScope) {
    Ext.application({
        name: 'myApp',

        appFolder: 'app',
        autoCreateViewport: false,
        controllers: [
                      'MyController'
                  ],

        launch: function() {
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
    autoHeight:true,
    layout: 'card',
    closable: true,
//    resizable: false,
//    layout: 'fit',
    items: [{
        id: 'card-0',
        xtype: 'panel1'
    },{
        id: 'card-1',
        xtype: 'panel2'
    },{
        id: 'card-2',
        html : 'Yes - 3'
        }]
}) 

Ext.define('myApp.controller.MyController', {
    extend: 'Ext.app.Controller',
    init: function() {
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
    headerContinue: function(button) {
        var panel = button.up('loginform');
        var colcnt = panel.down('panel1 #columncnt').getValue();
        var rowcnt = panel.down('panel1 #rowcnt').getValue();
        var tableWidth  = panel.down('panel1 #tablewidth').getValue();
        var fixedrowvalue=false;
        if(panel.down('panel1 #fixedrowvalue').getValue()){
            fixedrowvalue=true;    
        };
        //        var tablebordern= "#"+panel.down('panel1 #bordercolor').value;
        var tablebordern= "#"+ Ext.globaltablecolor;
        var res = tablebordern.match(/undefined/g);
        if(res!=null){
            tablebordern="#000000";
        }
        var bordereffect1=panel.down('panel1 #border1').pressed;
        //        var bordereffect2=panel.down('panel1 #border2').pressed;
        var bordereffect3=panel.down('panel1 #border3').pressed;
        var cellWidth = tableWidth / colcnt;
        if(bordereffect1==true){
            var tableHTML="";
            if(!fixedrowvalue){
                tableHTML = '<table align="center" class="globaltable" border="1" bordercolor="'+tablebordern+'" cellspacing="0" style="border-collapse:collapse;">';  
            }else{
                tableHTML = '<table align="center" class="globaltablerepeat" border="1" bordercolor="'+tablebordern+'" cellspacing="0" style="border-collapse:collapse;">';    
            }
            for(var cnt=0;cnt<rowcnt;cnt++) {
                tableHTML += '<tr >';
                for(var cnt1=0;cnt1<colcnt;cnt1++) {
                    tableHTML += '<td valign="top" style="width:'+cellWidth+'px;" onclick="getval(this)"> R'+cnt+'C'+cnt1+' </td>';
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
        else if(bordereffect3==true){
            var tableHTML ="";
            if(!fixedrowvalue){
                tableHTML= '<table align="center" class="globaltable" border="1" bordercolor="'+tablebordern+'" cellspacing="0" style="border-collapse:collapse;width:100%;height:100%">';  
            }else{
                tableHTML= '<table align="center" class="globaltablerepeat" border="1" bordercolor="'+tablebordern+'" cellspacing="0" style="border-collapse:collapse;width:100%;height:100%">';  
            }
            for(var cnt=0;cnt<rowcnt;cnt++) {
                tableHTML += '<tr>';
                for(var cnt1=0;cnt1<colcnt;cnt1++) {
                    tableHTML += '<td style="width:'+cellWidth+'px;border-left: medium none; border-right: medium none;" onclick="getval(this)"> R'+cnt+'C'+cnt1+' </td>';
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        }else{
            var tableHTML ="";
            if(!fixedrowvalue){
                tableHTML='<table align="center" class="globaltable" border="1" bordercolor="'+tablebordern+'"  cellspacing="0"  style="border:1px solid black; cursor:pointer; border-color:'+tablebordern+';">';
            }else{
                tableHTML='<table align="center" class="globaltablerepeat" border="1" bordercolor="'+tablebordern+'"  cellspacing="0"  style="border:1px solid black; cursor:pointer; border-color:'+tablebordern+';">'; 
            }  
            for(var cnt=0;cnt<rowcnt;cnt++) {
                tableHTML += '<tr>';
                for(var cnt1=0;cnt1<colcnt;cnt1++) {
                    tableHTML += '<td style="width:'+cellWidth+'px;" onclick="getval(this)"> R'+cnt+'C'+cnt1+' </td>';
                }
                tableHTML += '</tr>';
            }
        }
        tableHTML += '</table>';
        panel.down('panel2 #tablehtml').setValue(tableHTML);
        panel.getLayout().setActiveItem(1);
        panel.doLayout();
        // <br /> 
    },
    headerPrevious: function(button) {
       var panel = button.up('loginform');
       panel.getLayout().setActiveItem(0);
    },
    headerSubmit: function(button) {
        var panel = button.up('loginform');
        var designerPanel = Ext.getCmp('sectionPanelGrid');
        var PosX = designerPanel.cursorCustomX;
        var PosY = designerPanel.cursorCustomY;
        Ext.fixedrowvalue=panel.down('panel1 #fixedrowvalue').getValue();
        var field = createGlobalTable(PosX,PosY,Ext.getCmp('paneltable').body.dom.innerHTML,designerPanel,Ext.getCmp('propertypanel'),Ext.getCmp('paneltable').getBox().width, Ext.getCmp('paneltable').getBox().height,Ext.fixedrowvalue);
        designerPanel.items.add(field);
        Ext.globaltablecolor="";
        designerPanel.doLayout();
        panel.close();
    }

}); 

Ext.define('myApp.view.Panel1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.panel1',
    title: 'Step 1: Order Entry - Header',
    layout : {
        type: 'vbox'
    },
    bodyStyle : 'background-color:#ffffff;padding:15px 5px 5px 20px;',
    bbar: [{
            xtype: 'button',
            id: 'continue',
            text: 'Next',
            iconCls: 'icon-go'
        }
    ],
        items: [{
            xtype: 'label',
            //                         width: 200,
            text: 'Create an empty table with'
        },{
            xtype: 'numberfield',
            width: 200,
            name: 'columnscnt',
            itemId: 'columncnt',
            fieldLabel: 'Columns',
            value: 1,
            maxValue: 10,
            minValue: 1
        },{
            xtype: 'numberfield',
            width: 200,
            name: 'rowscnt',
            itemId: 'rowcnt',
            fieldLabel: 'Rows',
            value: 1,
            maxValue: 10,
            minValue: 1
        },{
            xtype: 'numberfield',
            width: 200,
            name: 'tablewidth',
            itemId: 'tablewidth',
            fieldLabel: 'Table Width',
            value: 200,
            maxValue: 700,
            minValue: 1
        },{
            xtype: 'tbspacer', 
            height: 20
        },{
            fieldLabel: 'Repeat Row index(Starting from 1): ',
            xtype: 'textfield',
            name: 'myTextField',
            id:'fixedrow',
            itemId: 'fixedrowvalue',
            disabled: false
        },
        {         
            xtype:'panel',
            itemId: 'bordercolor',
            dataIndex: 'newheaderpropertyaaa',
            height:40,
            border:false,
            text: 'Choose Color',
            titleAlign:"Center",
            titleCollapse:true,
            width: 120,
            html :"<a style='color:red'onclick='colorpanel()' href = '#' class=''><b>Table border color</b></a>",
            scope:this
        },  
        {
            xtype: 'fieldset',
            columns: 3,
            title: 'BorderStyle',
            width: 400,
            height: 100,  
            bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
            items: [
            {
                xtype: 'button',
                enableToggle: true,
                scale : 'large',
                scope : this,
                rowspan: 3,
                height: 63,
                width: 74,
                //                    id:'border1Img',
                itemId: 'border1',
                iconCls : 'border1Img ',
                cls:'table,td',
                enableToggle: true,
                autoHeight : true,
                margin: '10 20 30 50',
                toggleGroup: 'ratings',
                pressed : true
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
                scale : 'large',
                rowspan: 3,
                height: 63,
                width: 74,
                itemId: 'border3',
                //                    id:'border3Img',
                iconCls : 'border3Img ',
                scope : this,
                margin:'10 20 30 20',
                enableToggle: true,
                autoHeight : true,
                toggleGroup: 'ratings'
            } ]

        }
        ],
        layout: 'auto'
    });


Ext.define('myApp.view.Panel2' ,{
    extend: 'Ext.Panel',
    alias: 'widget.panel2',
    bodyStyle : 'background-color:#ffffff;padding:15px 5px 5px 20px;',
    layout : {
        type: 'vbox'
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
        itemId : 'tablehtml',
        name: 'hidden_field_1',
        text: 'value from hidden field'
      },{
        xtype: 'panel',
        autoWidth : true,
        layout : {
            type: 'vbox'
        },
        height : 400,
        border : false,
        id : 'paneltable'
    }],
    listeners: {
        activate: {
            fn: function(e){ 
                Ext.getCmp('paneltable').body.dom.innerHTML = this.items.items[0].rawValue;
//                var tbl = document.getElementById("tblMain"); 
//                if (tbl != null) { 
//                    for (var i = 0; i < tbl.rows.length; i++) {
//                        for (var j = 0; j < tbl.rows[i].cells.length; j++) 
//                            tbl.rows[i].cells[j].onclick = function () { getval(this); }; 
//                    } 
//                } 
//                function getval(cel) { 
//                    new Ext.GlobalCellProperty({
//                        cellVal : cel
//                    }).show();
//                }
            }
        }
    }
});

function getval(cel) { 
    var htmleditlabel=cel.innerHTML;
    var celltextnode=cel.textContent;
    var valtype=cel.valtype;
    var cellbgcolor=cel.style.backgroundColor;
    var celltextalign=cel.style.textAlign;
    new Ext.GlobalCellProperty({
        cellVal : cel,
        htmleditlabel:htmleditlabel,
        cellbgcolor:cellbgcolor,
        celltextalign:celltextalign,
        valtype:valtype,
        celltextnode:celltextnode
    }).show();
}

Ext.GlobalCellProperty= function(config){
    Ext.apply(this,{
        region:'center',
        scope:this,
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
        autoHeight:true, 
        border: false,
        buttons : [{
            text: "Set Value",
            scope: this,
            handler: function(){
                var cellHeight = this.cellheight.getValue();
                if(cellHeight!="") {
                    this.cellVal.height = cellHeight;
                }
                if(this.valType == 2) { // Insert Text 
                    if(Ext.getCmp( 'myDesignFieldEditor2').hidden==true){
                        this.cellVal.innerHTML = Ext.getCmp('customdesignerfieldlabel').getValue();
                        this.cellVal.id=Ext.getCmp('customdesignerfieldlabel').getValue();
                        labelHTML=this.cellVal.innerHTML;
                    }else{
                        var htmleditvalue=Ext.getCmp( 'myDesignFieldEditor2').getValue();
                        this.cellVal.innerHTML=htmleditvalue;
                        labelHTML=this.cellVal.innerHTML;
                        this.cellVal.id=Ext.getCmp('myDesignFieldEditor2').getValue();
                         }
                } else {
                    var rec = searchRecord(defaultFieldGlobalStore, Ext.getCmp('customdesignerfieldselectcombo').getValue(), 'id');
                    var htmleditvalue=Ext.getCmp( 'myDesignFieldEditor2').getValue();
                    this.cellVal.innerHTML=htmleditvalue;
                    if(this.htmllabelnew){
                        this.cellVal.className=this.htmllabelnew;
                    }else{
                        this.cellVal.className=Ext.getCmp('customdesignerfieldselectcombo').getValue();
                        this.cellVal.id=Ext.getCmp('customdesignerfieldselectcombo').getValue();
                    }
                    
                    if(this.celldecimalpoints.getValue()!=null) {
                        this.cellVal.setAttribute("decimalpoint",this.celldecimalpoints.getValue());
                    }
                    else{
                        this.cellVal.setAttribute("decimalpoint","");
                    }
//                      var labelHTML=this.cellVal.innerHTML;
//                    if (rec) {
//                        label = rec.data.label;
//                        this.valType=1;
//                        var labelHTML = "<span attribute='{PLACEHOLDER:" + Ext.getCmp('customdesignerfieldselectcombo').getValue() + "}'>#" + label + "#</span>";
//                    } else {
//                        return; // if record not found then return
//                    }
//                    //                    var labelHTML = "<span attribute='{PLACEHOLDER:" + Ext.getCmp('customdesignerfieldselectcombo').getValue() + "}'>#" + label + "#</span>";
                        
//                    this.cellVal.innerHTML = labelHTML;
                }
                var headervalues=getHeaderlineproperties();
                var headerObj = eval('('+headervalues+')');
                this.cellVal.style.textAlign=headerObj['alignment'];
                this.cellVal.style.backgroundColor=headerObj["backgroundcolor"]
                if(this.valType!==2){
                    this.cellVal.innerHTML=headerObj["changedlabel"]
                }
                this.cellVal.valtype=this.valType;
                this.close();
            }
        },{
            text: "Cancel",
            scope: this,
            handler: function(){
                this.close();
            }
        }],
        items:[
            
          this.radiocell= new Ext.form.RadioGroup({
//                xtype: 'radiogroup',
                fieldLabel: 'Place holder',
                layout : {
                    align: 'stretch',
                    type: 'hbox'
                },
//                 columns: 2,
                width : 400,
                border: false,
            items: [
                {
                boxLabel  : 'Select Field',
                name      : 'type',
                inputValue: '1',
                checked:true,
                flex  : 1
                }, {
                boxLabel  : 'Insert Text',
                name      : 'type',
                inputValue: '2',
                checked   :false,
                flex  : 1
            }],
            listeners: {
                scope : this,
                change : function(obj, value){ 
                    if(value.type==2) {
                        this.valType = 2;
                        var celltextlabel=Ext.getCmp('customdesignerfieldlabel');
                        celltextlabel.setValue("");
                        var comboalign=Ext.getCmp('alignselectcombo');
                        comboalign.setValue("");
                        celltextlabel.show();
                        Ext.getCmp('customdesignerfieldselectcombo').hide();
                        Ext.getCmp( 'myDesignFieldEditor2').hide();
                    } else {
                        this.valType = 1;
                        Ext.getCmp('customdesignerfieldlabel').hide();
                        var cellcombo=Ext.getCmp('customdesignerfieldselectcombo');
                        cellcombo.setValue("");
                        var comboalign=Ext.getCmp('alignselectcombo');
                        comboalign.setValue("");
                        var setdefaulthtml=Ext.getCmp( 'myDesignFieldEditor2');
                        setdefaulthtml.setValue("");
                        cellcombo.show();
                        setdefaulthtml.show();
                    }
                }
            }
        }),{
            xtype: 'fieldset',
            flex: 1,
            title: 'Set Value',
            layout: 'anchor',
            defaults: {
                anchor: '80%',
                hideEmptyLabel: false
            },
            defaultType: 'textfield',
            items : [
            this.fieldinserttext=new Ext.form.field.Text({
                fieldLabel: 'Insert Text',
                xtype: 'textfield',
                id: 'customdesignerfieldlabel',
                name: 'fieldlabel',
                width: 300,
                hidden: true,
                hideLabel: true,
                allowBlank: false
            }), 
            this.cellcombo=new Ext.form.field.ComboBox({
                //                    xtype: 'combo',
                fieldLabel: 'Select Field',
                store: defaultFieldGlobalStore,
                id: 'customdesignerfieldselectcombo',
                displayField: 'label',
                valueField: 'id',
                queryMode: 'local',
                width: 300,
                multiSelect:true,
                emptyText: 'Select a field',
                forceSelection: false,
                editable: false,
		triggerAction: 'all',
                listeners:{
                    scope: this,
                    'select': function(combo, selection,index,record){
                        this.myEditorheader = Ext.getCmp('myDesignFieldEditor2');
                        if(combo.displayTplData.length-1==0) {
                            this.myEditorheader.setValue("");
                            this.htmllabelnew =selection[combo.displayTplData.length-1].data.label;
                            this.htmllabel = "<span attribute='{PLACEHOLDER:" + selection[combo.displayTplData.length-1].data.id + "}'>#" + this.htmllabelnew + "#</span>";
                            this.myEditorheader.setValue(this.htmllabel);
                        } else {
                            this.htmllabel=this.myEditorheader.getValue();
                            var itemselectcount=combo.displayTplData.length-1;
                            this.htmllabelnew =selection[itemselectcount].data.label;
                            this.htmllabeledit = "<span attribute='{PLACEHOLDER:" + selection[itemselectcount].data.id + "}'>#" + this.htmllabelnew + "#</span>";
                            this.myEditorheader.setValue(this.htmllabel+"<br>"+this.htmllabeledit);
                            this.htmllabel=this.myEditorheader.getValue();
                        }
                    }
                }
            })]
        },
        this.cellheight=new Ext.form.field.Number({
            width: 200,
            name: 'cellheight',
            itemId: 'cellheight',
            fieldLabel: 'Row Height',
            maxValue: 100,
            minValue: 10
        }), this.celldecimalpoints=new Ext.form.field.Number({
            width: 200,
            name: 'decimalconfigure',
            itemId: 'celldecimal',
            fieldLabel: 'Decimal Points',
            maxValue: 10,
            minValue: 0
        })
        ,new Ext.HeaderWin({
            cellparameters:config.htmleditlabel,
            cellbgcolor:config.cellbgcolor,
            celltextalign:config.celltextalign,
            valtype:config.valtype,
            celltextnode:config.celltextnode
        })
//        , 
//        this.isRepeatfield = new Ext.form.Checkbox({
//            fieldLabel: 'Repeat fields',
//            name: 'repeatfields'
////            hidden:
//        }) 
        ]
    },config);
    this.fieldinserttext.on("blur",function(objbase, obj){
        var celltextlabel=Ext.getCmp('customdesignerfieldlabel');
        var celllabel=celltextlabel.getValue();
        var sethtmlvalue=Ext.getCmp( 'myDesignFieldEditor2').show();
        sethtmlvalue.setValue(celllabel);
    },this)
    
    if(config.htmleditlabel!=undefined && config.htmleditlabel!="" ){
        var arr = config.htmleditlabel.match(/\{PLACEHOLDER:(.*?)}/g);
        if(arr){
            var arraylength=arr.length;
            for(var i=0;i<=arraylength-1;i++){
                var matches = arr[i].replace(/\{|\}/gi, '').split(":");
                config['placeholder'] = matches[1];
                var rec = searchRecord(defaultFieldGlobalStore, matches[1], 'id');
                if(rec==undefined||rec==null){
                    var newinnerhtml=this.cellVal.innerHTML;
                    this.newlabel=this.newlabel+","+this.cellVal.className;
                }else{
                      var newlabelindividual=rec.data.label
                    if(i>0){
                        this.newlabel=this.newlabel+","+newlabelindividual;
                    }else{
                        this.newlabel=newlabelindividual;
                    }
                }
            }
            this.cellcombo.setValue(this.newlabel);
            this.radiocell.items.items[0].setValue(true);
            this.radiocell.items.items[1].setValue(false);
            Ext.getCmp('customdesignerfieldlabel').hide();
            Ext.getCmp('customdesignerfieldselectcombo').show();
        }else{
            this.radiocell.items.items[0].setValue(false);
            this.radiocell.items.items[1].setValue(true);
            var check=Ext.getCmp('customdesignerfieldlabel');
            var cellaligncombo=Ext.getCmp('alignselectcombo');
            cellaligncombo.setValue(config.celltextalign);
            check.setValue(config.celltextnode);
            var htcell=Ext.getCmp('myDesignFieldEditor2');
            htcell.setValue(config.htmleditlabel);
            Ext.getCmp('customdesignerfieldlabel').show();
            htcell.show();
            Ext.getCmp('customdesignerfieldselectcombo').hide();
        }
                
    }
    Ext.GlobalCellProperty.superclass.constructor.call(this, config);
}

Ext.extend(Ext.GlobalCellProperty, Ext.Window, {
    title: 'Cell Property',
    closable:true,
    width:470,
    autoHeight:true,
    resizable:true,
    autoScroll:true,
    plain:true,
    scope:this,
    modal:true  
})

//function for header property 
function getHeaderlineproperties(){
    Ext.bgcolor=Ext.getCmp('colorpickerbg');
    Ext.selectcombo = Ext.getCmp('alignselectcombo')
      var value=Ext.selectcombo.value;
      if(value==null){
          value="";
      }
      var str;
    if(Ext.bgcolor.isBackgroundColor==true){               //value for background color
        var selectedcolor=Ext.bgcolor.value;
        if(selectedcolor){
             str="#"+selectedcolor;
        }else{
            str="";
        }
    }    
      var myEditorheader = Ext.getCmp('myDesignFieldEditor2');
      var htmlee=myEditorheader.getValue();
//       htmlee=JSON.stringify(htmlee);
      this.cellproperties={
        "alignment":value,
        "backgroundcolor": str,
//        "htmlvalue": displayvalue,
        'changedlabel' : htmlee.replace(/"/g, '\\"')
    };  
    var returnConfig= Ext.JSON.encode(this.cellproperties);        
    return returnConfig;  
}


function createheaderwindow(){

 var headerbutton= new Ext.HeaderWin({});             
                        headerbutton.show();
                         headerbutton.doLayout();
}

function rgbToHex(r, g, b) {
    return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
}

function getlineitemstable(PosX,PosY,html,designerPanel,propertyPanel){
    var field = Ext.create(Ext.Component, {
        x: PosX,
        y:PosY,
        id : 'itemlistcontainer'+designerPanel.id,
        draggable: true,
        initDraggable: function() {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        },//initDraggable
        containerId : designerPanel.id,
        fieldTypeId: Ext.fieldID.insertTable,
        resizable: true,
        cls: 'tpl-content',
        style: {
            width: '95%', 
            height: 'auto !important', 
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
        onRender: function() {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function(eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if(component) {
                    if(Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    }
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel,propertyPanel);
                }
            });
        },//onRender
        listeners: {
            onMouseUp: function(field) {
                field.focus();
            },
            removed: function() {
                onDeleteUpdatePropertyPanel();
            //                                        propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            },
            afterrender: function() {
                this.el.on('click', function(eventObject, target, arg) {
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
    field.on('drag',showAlignedComponent, field);
    field.on('dragend',removeAlignedLine, field);
    return field;
}


function configureItemListcell(PosX,PosY,obj,html, designerPanel, propertyPanel,tablebordermode,tablecolor,height,width) {
    var jArr = eval(obj.lineitems);
    //        var tab1Row = document.createElement("ul");
    //        tab1Row.setAttribute("id", "itemlistconfig"+panelId);
    //        tab1Row.setAttribute('style', 'padding-left: inherit;width:100%');
    //                var width = (100 / jArr.length);
     var c, r, t;
    
     var tableConfig  = document.createElement('table');
        r = tableConfig.insertRow(0);
        t=tableConfig.insertRow(1);
        tableConfig.setAttribute("class", "linetableproperties");
        tableConfig.setAttribute("id", "itemlistconfig" + designerPanel.id);
        tableConfig.setAttribute("cellSpacing","0");
        if( tablebordermode){
            this.tablebgcolor=tablecolor;
            this.borderstylemode=tablebordermode;
            var res = this.tablebgcolor.match(/#/g);
            if(res==null){
                this.tablebgcolor='#'+this.tablebgcolor;
            }
            tableConfig.setAttribute("borderstylemode", this.borderstylemode);
            tableConfig.setAttribute("tcolor", this.tablebgcolor);
            if(this.borderstylemode=="borderstylemode2"){
                tableConfig.setAttribute("border",'0px solid ');
                tableConfig.setAttribute('style', 'border-top: thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode3"){
                tableConfig.setAttribute("border",'0px solid');
                tableConfig.setAttribute('style', 'border-color:'+this.tablebgcolor+';border-left: thin solid'+this.tablebgcolor+';border-right: thin solid '+this.tablebgcolor+';border-top: thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode4"){
                tableConfig.setAttribute("border",'0px solid');
                tableConfig.setAttribute('style', 'border-color:none;border-left:none;border-right:none;border-top: none;');
            }else{
                tableConfig.setAttribute("border",'1px solid ');
                tableConfig.setAttribute('style', 'border-color:'+this.tablebgcolor+';');
            }
        }else{
            tableConfig.setAttribute("border",'1px solid black ');
        }
       
        for(var cnt=0; cnt<jArr.length; cnt++) {
            var obj = jArr[cnt];
            var newTH =document.createElement('th');
            if(jArr[cnt].headerproperty){
                var headerpropertyparam=JSON.parse(jArr[cnt].headerproperty);
                newTH.setAttribute("bgColor", headerpropertyparam.backgroundcolor);
                newTH.setAttribute("align", headerpropertyparam.alignment);
                newTH.style.textAlign=headerpropertyparam.alignment;
                newTH.innerHTML=headerpropertyparam.changedlabel;
                newTH.setAttribute("label", headerpropertyparam.changedlabel);
            }else{
                newTH.innerHTML=obj.label;
                newTH.setAttribute("label", obj.label);
            }
            //            newTH.setAttribute("class", "tpl-content");
            var widthInPercent = (obj.colwidth-2)+"%";// decreased 2% because while rendering header in html we used marging of 2%
            if(this.borderstylemode=="borderstylemode2"){
                newTH.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-bottom:thin solid '+this.tablebgcolor+';border-top:thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode3"){
                newTH.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-left: none; border-right: none;border-bottom:thin solid'+this.tablebgcolor+';border-top:thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode4"){
                newTH.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-left:none; border-right:none;border-bottom:thin solid'+this.tablebgcolor+';border-top:thin solid'+this.tablebgcolor+';');
            }else{
                if(this.tablebgcolor==undefined){
                    newTH.setAttribute('style', 'width: '+widthInPercent);
                }else{
                    newTH.setAttribute('style', 'width: '+widthInPercent+';border-color:'+this.tablebgcolor+';');
                }
            }
            newTH.setAttribute("colwidth", obj.colwidth);
            newTH.setAttribute("coltotal", obj.coltotal);
            newTH.setAttribute("showtotal", obj.showtotal);
            newTH.setAttribute("commaamount", obj.commaamount);
            newTH.setAttribute("headercurrency", obj.headercurrency);
            newTH.setAttribute("recordcurrency", obj.recordcurrency);
            newTH.setAttribute("decimalpoint", (obj.decimalpoint!=undefined && obj.decimalpoint!=null?((obj.decimalpoint!="undefined" && obj.decimalpoint!="null")?obj.decimalpoint:defaultdecimalvalue):defaultdecimalvalue));
            newTH.setAttribute("seq", obj.seq);
            newTH.setAttribute("xtype", obj.xtype);
            newTH.setAttribute("headerproperty", obj.headerproperty);
            newTH.setAttribute("fieldid", obj.fieldid);
            newTH.setAttribute("tablecolor",this.tablebgcolor);
            newTH.setAttribute("tableborderstyle",this.borderstylemode);
            newTH.setAttribute("basequantitywithuom", (obj.basequantitywithuom!=undefined?(obj.basequantitywithuom!="undefined"?obj.basequantitywithuom:false):false));
            newTH.setAttribute("baserate", (obj.baserate!=undefined?(obj.baserate!="undefined"?obj.baserate:false):false));
            newTH.value = obj.fieldid;
            newTH.cellIndex=obj.seq;
            r.appendChild(newTH);
            //             var tabletd=t.insertCell(cnt);;
            var tabletd=document.createElement('td');
            tabletd.cellIndex=newTH.cellIndex;
            tabletd.setAttribute("cellIndex",newTH.cellIndex);
            tabletd.innerHTML="&nbsp;";
            if(this.borderstylemode=="borderstylemode2"){
                tabletd.setAttribute('style', 'width: '+widthInPercent+';border-width: thin;border-bottom:thin solid '+this.tablebgcolor+';border-top:thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode3"){
                tabletd.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-left: none; border-right: none;border-bottom:thin solid '+this.tablebgcolor+';border-top:thin solid '+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode4"){
                tabletd.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-left: none; border-right: none;border-bottom:none;border-top:none;');
            }else{
                if(this.tablebgcolor==undefined){
                    tabletd.setAttribute('style', 'width: '+widthInPercent);
                }else{
                    tabletd.setAttribute('style', 'width: '+widthInPercent+';border-color:'+this.tablebgcolor+';');
                }
            }
            tabletd.setAttribute("colwidth", obj.colwidth);
            tabletd.setAttribute("tablecolor",this.tablebgcolor);
            tabletd.setAttribute("tableborderstyle",this.borderstylemode);
            t.appendChild(tabletd);
            tableConfig.appendChild(r);
        }
        tableConfig.appendChild(r);
        tableConfig.appendChild(t);
        Ext.tableinsert=true;
    var html = tableConfig.outerHTML;
    var field = Ext.create(Ext.Component, {
        x: PosX,
        y:PosY,
        draggable: true,
        initDraggable: function() {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        },//initDraggable
        id:'itemlistcontainer' + designerPanel.id,
        containerId : designerPanel.id,
        width : (width!=null||width!=undefined)? width:'95%', 
        height: (height!=null||height!=undefined)?height:'auto !important',
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
            onMouseUp: function(field) {
                field.focus();
            },
            removed: function() {
                onDeleteUpdatePropertyPanel();
            //                                        propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            },
            afterrender: function() {
                addPositionObjectInCollection(this);
                this.el.on('click', function(eventObject, target, arg) {
                    var component = designerPanel.queryById(this.id)
                    if (component  && Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                        eventObject.stopPropagation();
                        getPropertyPanel(component, designerPanel, propertyPanel);
                    }
                });
            }
        }
    });
    field.on('drag',showAlignedComponent, field);
    field.on('dragend',removeAlignedLine, field);
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

function renderItemsOnDesignPanel(arr,designerPanel,propertyPanel) {
    for (var cnt = 0; cnt < arr.length; cnt++) {
        var obj = arr[cnt];
        var field;
        if (obj.fieldTypeId == Ext.fieldID.insertTable) {
             var tablebordermode=arr[cnt].tablebordermode;
             var tablecolor=arr[cnt].tablecolor;
            //                field = configureItemListcell(obj, this.designerPanel, this.propertyPanel)
            field =configureItemListcell(obj.x,obj.y,obj, obj.labelhtml,designerPanel,propertyPanel,tablebordermode,tablecolor,obj.height,obj.width);
        } else if (obj.fieldTypeId == '3') {
            field = createExtImgComponent(designerPanel, propertyPanel, obj.fieldTypeId, obj.src, obj.x, obj.y, obj);
        } else if (obj.fieldTypeId == '4') {
            field = createHoLineComponent(designerPanel, obj.fieldTypeId, propertyPanel, obj.x, obj.y,obj.width,obj.widthnew);
        } else if (obj.fieldTypeId == '6') {
            field = createDrawComponent(designerPanel,propertyPanel, obj.fieldTypeId,obj.labelhtml, obj.x,obj.y,obj.width,obj.height,obj.backgroundColor);
        } else if (obj.fieldTypeId == Ext.fieldID.insertGlobalTable) {
            field = createGlobalTable( obj.x,obj.y, obj.labelhtml,designerPanel,propertyPanel,obj.width,obj.height,obj.fixedrowvalue);
        } else { 
            var labelHTML = obj.fieldTypeId != Ext.fieldID.insertField ? obj.labelhtml : "<span attribute='{PLACEHOLDER:" + obj.fieldid + "}'>" + obj.labelhtml + "</span>";
            field = createExtComponent(designerPanel, undefined, obj.fieldTypeId, labelHTML, obj.x, obj.y, {
                width: obj.width,
                height: obj.height
            },obj.selectfieldbordercolor);
        }
        designerPanel.items.add(field);
    }
    designerPanel.doLayout();    
}

function colorpanel(){
    var headerbutton= new Ext.Window({
        closable:true,
        title: 'Table Border color',
        id:'bordercolorproperty',
        autoWidth:true,
        autoHeight:true,
        scope:this,
        items : [
        this.smallcolorpanel=Ext.create('Ext.picker.Color', {
            value:  (Ext.globaltablecolor==undefined||Ext.globaltablecolor=="")?'FFFFFF':Ext.globaltablecolor,  // initial selected color
            itemId: 'bordercolor',
            //                    id:'colorpickerbg',
            isBackgroundColor:true,
               listeners: {
                select: function(picker, selColor) {
                    Ext.globaltablecolor=selColor;
                    headerbutton.close();
                }
            }
        })]
    })
    headerbutton.show();
    headerbutton.doLayout();

}

function addPositionObjectInCollection(component) {
    var originalConfig = {
        leftX:component.getBox().x,
        topY:component.getBox().y,
        rightX:component.getBox().right,
        bottom:component.getBox().bottom,
        pid:component.id
    }
    componentCollection.push( originalConfig);
}

function removePositionObjectFromCollection(currentcompid) {
    for(var cnt=0;cnt<componentCollection.length; cnt++) {
        var collectionid=componentCollection[cnt].pid;
        if(collectionid==currentcompid){
            componentCollection.splice(cnt, 1);
        }
    }
}
function showAlignedComponent(object, event) {
    var sectionWindow_Top = 0;
    var sectionWindow_Left = 0;
    if(Ext.getCmp('sectionPanelGrid')) {
         sectionWindow_Top = Ext.getCmp('sectionPanelGrid').getBox().top;
         sectionWindow_Left = Ext.getCmp('sectionPanelGrid').getBox().left - 7;
    } else {
        sectionWindow_Top = Ext.getCmp('pagelayout1').getBox().top;
        sectionWindow_Left = Ext.getCmp('pagelayout1').getBox().left;
    }
    var freezePixels = 1;
    for(var j=0;j<componentCollection.length; j++)
    {
        //current selected panel for dragging   
        var currentelemntid= object.el.id;
        var currentCollectionObject = Ext.getCmp(componentCollection[j].pid);
        //Check whether the draggable Component & Collection component is not same
        if(currentelemntid!=componentCollection[j].pid)
        {
            if(((object.comp.getXY()[0] <= (currentCollectionObject.getXY()[0]+freezePixels)) && (object.comp.getXY()[0] >= (currentCollectionObject.getXY()[0]-freezePixels))))
            {
                //logic to draw the line
//                console.log("Dimention matched...From-1st");
                object.comp.setPagePosition(currentCollectionObject.getXY()[0],  object.comp.getPosition()[1]);
                isXMatched = true;
                //                                    lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line... 
                lineobj.drawLine(currentCollectionObject.getXY()[0] - sectionWindow_Left, currentCollectionObject.getXY()[1]  - sectionWindow_Top , object.comp.getXY()[0] - sectionWindow_Left, object.comp.getXY()[1]  - sectionWindow_Top );
                lineobj.paint();
            }//1st_if
            
            else if(((object.comp.getXY()[0] <= (currentCollectionObject.getBox().right+freezePixels)) && (object.comp.getXY()[0] >= (currentCollectionObject.getBox().right-freezePixels))))
            {
                //logic to draw the line
//                console.log("Dimention matched...From-3rd");
                object.comp.setPagePosition(currentCollectionObject.getBox().right , object.comp.getPosition()[1]);
                lineobj.clear();
                isrightMatched = true;
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line... 
                lineobj.drawLine(currentCollectionObject.getBox().right- sectionWindow_Left, currentCollectionObject.getBox().bottom - sectionWindow_Top , object.comp.getXY()[0]- sectionWindow_Left, object.comp.getXY()[1] - sectionWindow_Top);
                lineobj.paint();
            }//2rd_if
                                   
            else if(((object.comp.getXY()[1] <= (currentCollectionObject.getXY()[1]+freezePixels)) && (object.comp.getXY()[1] >= (currentCollectionObject.getXY()[1]-freezePixels))))
            {
                //logic to draw the line
//                console.log("Dimention matched...From-2nd");
                object.comp.setPagePosition(object.comp.getPosition()[0], currentCollectionObject.getXY()[1]);
                lineobj.clear();
                isYMatched = true;
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line... 
                lineobj.drawLine(currentCollectionObject.getXY()[0] - sectionWindow_Left, currentCollectionObject.getXY()[1] - sectionWindow_Top , object.comp.getXY()[0]- sectionWindow_Left, object.comp.getXY()[1] - sectionWindow_Top);
                lineobj.paint();
            }//3nd_if
                                                                       
            else if(((object.comp.getXY()[1] <= (currentCollectionObject.getBox().bottom+freezePixels)) && (object.comp.getXY()[1] >= (currentCollectionObject.getBox().bottom-freezePixels))))
            {
                //logic to draw the line
//                console.log("Dimention matched...From-4th");
                object.comp.setPagePosition(object.comp.getPosition()[0], currentCollectionObject.getBox().bottom);
                lineobj.clear();
                isbottomMatched = true;
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line... 
                lineobj.drawLine(currentCollectionObject.getBox().right - sectionWindow_Left, currentCollectionObject.getBox().bottom  - sectionWindow_Top  , object.comp.getXY()[0] - sectionWindow_Left, object.comp.getXY()[1] - sectionWindow_Top );
                lineobj.paint();
            }//4_if
                                    
            else if(((object.comp.getBox().right <= (currentCollectionObject.getXY()[0]+freezePixels)) && (object.comp.getBox().right >= (currentCollectionObject.getXY()[0]-freezePixels))))
            {
                //logic to draw the X-Coordinate line
//                console.log("Dimention matched...From-freezePixelsth");
                this.x1 = (currentCollectionObject.getXY()[0] - object.comp.getBox().width);
                object.comp.setPagePosition(this.x1,  object.comp.getPosition()[1]);
                isrightMatched = true;
                lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line...     
                lineobj.drawLine(currentCollectionObject.getXY()[0]- sectionWindow_Left, currentCollectionObject.getBox().bottom - sectionWindow_Top, object.comp.getBox().right- sectionWindow_Left, object.comp.getBox().y - sectionWindow_Top);
                lineobj.paint();
            }//5th_if
            else if(((object.comp.getBox().right <= (currentCollectionObject.getBox().right+freezePixels)) && (object.comp.getBox().right >= (currentCollectionObject.getBox().right-freezePixels))))
            {
                //logic to draw the line
//                console.log("Dimention matched...From 6th");
                //                                        this.x2 = (object.comp.getBox().right - object.comp.getBox().width);
                object.comp.setPagePosition(currentCollectionObject.getBox().right - object.comp.getBox().width,  object.comp.getPosition()[1]);
                isrightMatched = true;
                lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line... 
                lineobj.drawLine(currentCollectionObject.getBox().right- sectionWindow_Left, currentCollectionObject.getBox().bottom- sectionWindow_Top , object.comp.getBox().right- sectionWindow_Left, object.comp.getXY()[1]- sectionWindow_Top);
                lineobj.paint();
            }//6th_if
            //                                    
            else if(((object.comp.getBox().bottom <= (currentCollectionObject.getBox().bottom+freezePixels)) && (object.comp.getBox().bottom >= (currentCollectionObject.getBox().bottom-freezePixels))))
            {
                //logic to draw the X-Coordinate line
//                console.log("Dimention matched...From-7th");
                this.y1 = (currentCollectionObject.getBox().bottom - object.comp.getBox().height);
                object.comp.setPagePosition(object.comp.getPosition()[0], this.y1);
                isbottomMatched = true;
                lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line... 
                lineobj.drawLine(currentCollectionObject.getBox().right- sectionWindow_Left, currentCollectionObject.getBox().bottom - sectionWindow_Top  , object.comp.getBox().x- sectionWindow_Left, object.comp.getBox().bottom - sectionWindow_Top );
                lineobj.paint();
            }//7th_if
                                   
            else if(((object.comp.getBox().bottom <= (currentCollectionObject.getXY()[1]+freezePixels)) && (object.comp.getBox().bottom >= (currentCollectionObject.getXY()[1]-freezePixels))))
            {
                //logic to draw the X-Coordinate line
//                console.log("Dimention matched...From-8th");
                this.y2 = (currentCollectionObject.getXY()[1] - object.comp.getBox().height);
                object.comp.setPagePosition(object.comp.getPosition()[0], this.y2);
                isrightMatched = true;
                lineobj.clear();
                lineobj.setColor('red');
                lineobj.setStroke(1); //OR lineobj.setStroke(Stroke.DOTTED); //For Dotted Line... 
                lineobj.drawLine(currentCollectionObject.getBox().right- sectionWindow_Left, currentCollectionObject.getXY()[1]  - sectionWindow_Top , object.comp.getXY()[0]- sectionWindow_Left, object.comp.getBox().bottom - sectionWindow_Top );
                lineobj.paint();
            }//8th_if
            else
            {
                if(!isXMatched && !isYMatched && !isrightMatched && !isbottomMatched) 
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
    var currentpanelid=object.comp.id;
    for(var j=0;j<componentCollection.length; j++) {
        var collectionid=componentCollection[j].pid;
        if(collectionid==currentpanelid){
            indexObj = Ext.getCmp(collectionid);
            var updateConfig = {
                leftX:indexObj.getBox().x,
                topY:indexObj.getBox().y,
                rightX:indexObj.getBox().right,
                bottom:indexObj.getBox().bottom,
                pid : collectionid
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