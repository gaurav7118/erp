/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Ext.onReady(function() {
    if (_CustomDesign_moduleId == 27 || _CustomDesign_moduleId == 28) {
        var myData = [
            {id: '1', name: "Field Label"},
            {id: '2', name: "Field Value"},
            {id: '3', name: "Insert Image"},
            {id: '5', name: "Line Items"},
            {id: '17', name: "Prepared by"},
            {id: '18', name: "Approved by"},
            {id: '19', name: "Signature"}
        ];

    } else if (_CustomDesign_moduleId == 10 || _CustomDesign_moduleId == 12) {
        var myData = [
            {id: '1', name: "Field Label"},
            {id: '2', name: "Field Value"},
            {id: '3', name: "Insert Image"},
            //            { id : '4', name : "Insert Field Set"},
            {id: '5', name: "Line Items"},
            {id: '6', name: "Total Amount"},
            {id: '7', name: "Sub Total"},
            {id: '8', name: "Total Discount"},
            {id: '9', name: "Total Tax"},
            {id: '10', name: "Horizontal Line"},
            {id: '15', name: "Amount in words"},
            {id: '16', name: "PostText Master"},
            {id: '20', name: "Receiver's Signature"},
            {id: '21', name: "Checked By:"},
            {id: '22', name: "Authorized Signature:"},
            {id: '23', name: "Verified By:"}
        ];

    } else if (_CustomDesign_moduleId == 16 || _CustomDesign_moduleId == 14) {
        var myData = [
            {id: '1', name: "Field Label"},
            {id: '2', name: "Field Value"},
            {id: '3', name: "Insert Image"},
            {id: '5', name: "Line Items"},
            {id: '6', name: "Total Amount"},
            {id: '15', name: "Amount in words"}
        ];
    } else {

        var myData = [
            {id: '1', name: "Field Label"},
            {id: '2', name: "Field Value"},
            {id: '3', name: "Insert Image"},
            //            { id : '4', name : "Insert Field Set"},
            {id: '5', name: "Line Items"},
            {id: '6', name: "Total Amount"},
            {id: '7', name: "Sub Total"},
            {id: '8', name: "Total Discount"},
            {id: '9', name: "Total Tax"},
            {id: '10', name: "Horizontal Line"},
            {id: '15', name: "Amount in words"},
            {id: '16', name: "PostText Master"},
            {id: '17', name: "Prepared by"},
            {id: '18', name: "Approved by"},
            {id: '19', name: "Signature"}

        ];

    }

    // Generic fields array to use in both store defs.
    Ext.define('DataObject', {
        extend: 'Ext.data.Model',
        fields: ['id', 'name']
    });
    // Define dfault header 
    Ext.define('HeaderData', {
        extend: 'Ext.data.Model',
        fields: ['id', 'label', 'dbcolumnname', 'reftablename', 'reftablefk', 'reftabledatacolumn', 'dummyvalue', 'xtype', 'customfield']
    });

    // create the data store
    var itemStore = Ext.create('Ext.data.Store', {
        model: 'DataObject',
        data: myData
    });

    // Column Model shortcut array
    var columns = [
        {id: 'name', flex: 1, header: "Record Name", sortable: true, dataIndex: 'name'}
    ];

    // declare the source Grid
    var grid = Ext.create('Ext.grid.Panel', {
        viewConfig: {
            plugins: {
                ddGroup: 'DocDesigner',
                ptype: 'gridviewdragdrop',
                enableDrop: false
            }
        },
        store: itemStore,
        columns: columns,
        enableDragDrop: true,
        stripeRows: true,
        width: 100,
        margins: '0 2 0 0',
        region: 'west',
        title: 'Item List',
        selModel: Ext.create('Ext.selection.RowModel', {singleSelect: true})
    });

    var designerPanel = Ext.create('Ext.Panel', {
        region: 'center',
        title: 'Designer Panel',
        height: 750,
        autoHeight: true,
        id: '_designerPanel',
        autoScroll: true,
        //            autoWidth : false,
        //            baseCls        : 'designPanelView',
        bodyStyle: 'padding: 10px; background-color: #FFFFFF',
        //labelWidth : 100,
//                    width      : 275,
        //            maxWidth : 650,
        margins: '0 0 0 3',
        layout: 'absolute'

    });
    designerPanel.on("render", function() {
        Ext.Ajax.request({
            url: "CustomDesign/getDesignTemplate.do",
            method: 'POST',
            params: {
                moduleid: _CustomDesign_moduleId,
                templateid: _CustomDesign_templateId
            },
            success: function(response, req) {
                var result = Ext.decode(response.responseText);
                if (result.success && isValidSession(result)) {
                    var resData = result.data.data[0];
                    var json = resData.json
                    if (json) {
                        var arr = Ext.JSON.decode(json);
                        for (var cnt = 0; cnt < arr.length; cnt++) {
                            var obj = arr[cnt];
                            if (obj.fieldTypeId == '5') {
                                var field = configureItemList(obj, designerPanel, propertyPanel)
                            } else if (obj.fieldTypeId == '3') {
                                field = createExtImgComponent(designerPanel, propertyPanel, obj.fieldTypeId, obj.src, obj.x, obj.y, obj);
                            } else if (obj.fieldTypeId == '10') {
                                field = createHLineComponent(designerPanel, obj.fieldTypeId, propertyPanel, obj.x, obj.y)
                            } else {
                                var labelHTML = obj.fieldTypeId != '2' ? obj.labelhtml : "<span attribute='{PLACEHOLDER:" + obj.fieldid + "}'>" + obj.labelhtml + "</span>";
                                field = createExtComponent(designerPanel, propertyPanel, obj.fieldTypeId, labelHTML, obj.x, obj.y, {
                                    width: obj.width,
                                    height: obj.height
                                });
                            }
                            designerPanel.items.add(field);
                        }
                        designerPanel.doLayout();
                    }
                    defaultFieldGlobalStore = new Ext.create('Ext.data.Store', {
                        model: 'HeaderData',
                        data: resData.defaultfield
                    });
                    documentLineColumns = resData.linecolumns;
                    //                                getPropertyPanelItems(propertyPanel);
                }
            }
        }) // End of Ajax Request 
    });

    var propertyPanel = Ext.create('Ext.Panel', {
        region: 'east',
        width: 325,
        height: 600,
        title: 'Item Properties',
//            html : "<div style='padding:10px;font-weight: bold'>Select field to delete</div>",
        items: [{
                xtype: 'fieldcontainer',
                width: 400,
                layout: {
                    type: 'column'
                },
                margin: '10 0 30 5',
                fieldLabel: '<b>Selected Field</b>',
                defaults: {
                    hideLabel: true
                },
                items: [
                    {
                        xtype: 'displayfield',
                        value: '-',
                        itemId: 'setfieldproperty',
                        id: 'setfieldproperty',
                        width: 200
                    }, {
                        xtype: 'hiddenfield',
                        id: 'hidden_fieldID',
                        value: ''
                    }, {
                        xtype: 'hiddenfield',
                        id: 'hidden_fieldType',
                        value: ''
                    }, {
                        xtype: 'hiddenfield',
                        id: 'hidden_allowformatting',
                        value: ''
                    }
                ]
            },
            {
                xtype: 'button',
                text: 'Edit & Save',
                id: 'editformattingbtn',
                margin: '10 0 30 5',
                hidden: true,
                handler: function(button) {
                    if (Ext.getCmp('hidden_fieldType').getValue() == '5') {
                        var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                        openProdWindowAndSetConfig(selectedfield);
                    } else if (isValidFieldSelected() && Ext.getCmp('hidden_allowformatting').getValue() == 'true') {
                        var myView = button.up('panel');
                        var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                        var ItemDescription = Ext.getCmp('setfieldproperty');
                        var myEditor = Ext.getCmp('myDesignFieldEditor');
                        var bd = myEditor.getEditorBody();
                        var html = bd.innerHTML;
                        var firstChild = selectedfield.el.dom.firstChild;
                        var firstChildAttributes = firstChild.attributes;
                        var nexSibling = firstChild.nextSibling;
                        selectedfield.el.dom.removeChild(firstChild);
                        var wrapper = document.createElement('div');
                        wrapper.innerHTML = html;
                        if (firstChildAttributes) {
                            for (var cnt = 0; cnt < firstChildAttributes.length; cnt++) {
                                wrapper.setAttribute(firstChildAttributes[cnt]['name'], firstChildAttributes[cnt]['value']);
                            }
                        }
                        selectedfield.el.dom.insertBefore(wrapper, nexSibling);

                        ItemDescription.setValue(myEditor.getValue());
//                                                Ext.getCmp('cancelformattingbtn').hide();

//                                                myEditor.hide();
//                                                }
                    }
                }
            },
//                                    {
//                                        xtype: 'button',
//                                        text: 'Cancel Changes',
//                                        id : 'cancelformattingbtn',
//                                        hidden : true,
//                                        margin: '10 0 30 20',
//                                        handler: function(button) {
//                                            var myEditor = Ext.getCmp('myDesignFieldEditor');
//                                            Ext.getCmp('editformattingbtn').setText('Edit');
//                                            button.hide();
//                                            myEditor.setValue("");
//                                            myEditor.hide();
//                                        }
//                                    },
            {
                xtype: 'button',
                text: 'Delete',
                margin: '10 0 30 5',
                handler: function(deleteBtn) {
                    if (isValidFieldSelected()) {
                        Ext.MessageBox.show({
                            title: 'Confirm',
                            msg: 'Do you really want to delete this field?',
                            icon: Ext.MessageBox.QUESTION,
                            buttons: Ext.MessageBox.YESNO,
                            scope: this,
                            fn: function(button) {
                                var designerp = Ext.getCmp('_designerPanel');
                                if (button == 'yes')
                                {
                                    var obj = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                                    designerp.remove(obj, true);
                                } else {
                                    return;
                                }
                            }
                        });
                    }
                }
            },
            {
                flex: 1,
                xtype: 'htmleditor',
                width: 330,
                id: 'myDesignFieldEditor',
                height: 150,
                style: 'background-color: white;',
                value: '',
                margin: '10 0 30 5',
                enableLinks: false,
                enableLists: false,
                enableSourceEdit: false,
                enableAlignments: false
//                                hidden: true,
            }
        ]
    });

    var documentinfo = Ext.create('Ext.Panel', {
        region: 'north',
        title: 'Document Designer - Document Information',
        height: 60
    });

    //Simple 'border layout' panel to house both grids
    var displayPanel = Ext.create('Ext.Panel', {
        //width    : 650,
        height: 650,
        autoHeight: true,
        layout: 'border',
        align: 'stretch',
        renderTo: 'designerpanel',
        bodyPadding: '5',
        items: [
            documentinfo,
            grid,
            designerPanel,
            propertyPanel
        ],
        bbar: [
            '->',
            {
                text: 'Save & Set as Default Template',
                handler: function() {
                    var returnConfig = [];
                    returnConfig = getTemplateConfigurations(designerPanel, defaultFieldGlobalStore);
                    saveCustomDesign(returnConfig[0], returnConfig[1], 1);
                }
            },
            {
                text: 'Save Template',
                handler: function() {
                    var returnConfig = [];
                    returnConfig = getTemplateConfigurations(designerPanel, defaultFieldGlobalStore);
                    saveCustomDesign(returnConfig[0], returnConfig[1], 0);
                }
            }, {
                text: 'Preview with Sample Data',
                handler: function() {
                    //                        Wtf.get('downloadframe').dom.src  = "CustomDesign/showSamplePreview.do?moduleid=1";
                    var returnConfig = [];
                    returnConfig = getTemplateConfigurations(designerPanel, defaultFieldGlobalStore);
                    var reqform = document.getElementById('designpanelpreview');
                    reqform.elements['json'].value = returnConfig[0];
                    reqform.elements['html'].value = returnConfig[1];
                    reqform.elements['moduleid'].value = _CustomDesign_moduleId;
                    reqform.submit();
                    //                        Wtf.get('downloadframe').dom.src  = "CustomDesign/showSamplePreview.do?moduleid=1&json="+returnConfig[0]+"&html="+returnConfig[1];
                }
            }
        ]
    });


    /****
     * Setup Drop Targets
     ***/

    // This will make sure we only drop to the view container
    var formPanelDropTargetEl = designerPanel.body.dom;

    var formPanelDropTarget = Ext.create('Ext.dd.DropTarget', formPanelDropTargetEl, {
        ddGroup: 'DocDesigner',
        notifyEnter: function(ddSource, e, data) {
            //Add some flare to invite drop.
            designerPanel.body.stopAnimation();
            designerPanel.body.highlight();
        },
        notifyDrop: function(ddSource, e, data) {
            var event = e;
            var selectedRecord = ddSource.dragData.records[0];
            if (selectedRecord.data.id == 10) {// 10 - horizontal line
                var field = createHLineComponent(designerPanel, selectedRecord.data.id, propertyPanel, e.getX() - designerPanel.x - 4, e.getY() - designerPanel.y - 28)
                designerPanel.items.add(field);
            }
            if (selectedRecord.data.id == 17 || selectedRecord.data.id == 18 || selectedRecord.data.id == 19 || selectedRecord.data.id == 20 || selectedRecord.data.id == 21 || selectedRecord.data.id == 22 || selectedRecord.data.id == 23) {// 10 - horizontal line
                var field = createHLineComponent(designerPanel, selectedRecord.data.id, propertyPanel, e.getX() - designerPanel.x - 4, e.getY() - designerPanel.y - 10)
                designerPanel.items.add(field);
            } else if (selectedRecord.data.id >= 6) { // if >=6 - some static amount summary fields
                staticFieldLabel(event, designerPanel, selectedRecord.data.id, propertyPanel, e.getX() - designerPanel.x - 4, e.getY() - designerPanel.y - 28)
            } else if (selectedRecord.data.id == 4) { // 4- fieldSet
                field = createExtFieldSet(designerPanel, propertyPanel, selectedRecord.data.id, "", e.getX() - designerPanel.x - 4, e.getY() - designerPanel.y - 28)
                designerPanel.items.add(field);
            } else if (selectedRecord.data.id == '3') {
                CustomImageUpload(event, designerPanel, selectedRecord.data.name, selectedRecord.data.id, propertyPanel, (e.getX() - designerPanel.x - 4), (e.getY() - designerPanel.y - 28))
            }
            else if (selectedRecord.data.id != '5') { // if 1-Insert Label, 2-Select Field
                takeFieldLabel(event, designerPanel, selectedRecord.data.name, selectedRecord.data.id, propertyPanel, (e.getX() - designerPanel.x - 4), (e.getY() - designerPanel.y - 28))
            } else {
                // if 5 - Line items
                field = Ext.create(Ext.TemplateHolder, {
                    x: 0,
                    y: e.getY() - designerPanel.y,
                    draggable: true,
//                                width : 650,
                    height: 45,
                    fieldTypeId: selectedRecord.data.id,
                    cls: 'tpl-content',
                    style: {width: '100%', height: 'auto !important', borderColor: '#B5B8C8', borderStyle: 'solid', borderWidth: '1px', position: 'absolute'},
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
                            this.el.on('click', function(eventObject, target, arg) {
                                var component = designerPanel.queryById(this.id)
                                if (component) {
                                    getPropertyPanel(component, designerPanel, propertyPanel);
                                }
                            });
                        }
                    }
                });
                designerPanel.items.add(field);
            }
            designerPanel.doLayout();
            return true;
        }
    });
});


function staticFieldLabel(e, designerPanel, fieldTypeId, propertyPanel, XPos, YPos) {
    var label = "";// label should be same as set in InvoiceProductSummaryItems variable of LineItemColumnModuleMapping.java
    switch (fieldTypeId) {
        case "6" :
            label = "Total Amount";
            break;
        case "7" :
            label = "Sub Total";
            break;
        case "8" :
            label = "Total Discount";
            break;
        case "9" :
            label = "Total Tax";
            break;
        case "15" :
            label = "Amount in words";
            break;
        case "16" :
            label = "PostText Master";
            break;
        case "17" :
            label = "Prepared by";
            break;
        case "18" :
            label = "Approved by";
            break;
        case "19" :
            label = "Signature";
            break;

    }
    var field = createExtComponent(designerPanel, propertyPanel, fieldTypeId, "#" + label + "#", XPos, YPos);
    designerPanel.items.add(field);
    designerPanel.doLayout();
}
function takeFieldLabel(e, designerPanel, fieldtype, fieldTypeId, propertyPanel, XPos, YPos) {
    //    alert('x='+XPos+'y='+YPos);
    var form = new Ext.form.FormPanel({
        frame: true,
        labelWidth: 125,
        autoHeight: true,
        bodyStyle: 'padding:5px 5px 0',
        autoWidth: true,
        defaults: {
            width: 175
        },
        defaultType: 'textfield',
        items: [{
                fieldLabel: 'Field Label',
                id: 'customdesignerfieldlabel',
                name: 'fieldlabel',
                value: '',
                width: 240,
                maskRe: /[A-Za-z0-9_: ]+/,
                hidden: fieldTypeId != '1',
                hideLabel: fieldTypeId != '1',
                allowBlank: false
            }, {
                id: 'customdesignerimage',
                //            width : 280,
                fieldLabel: "Image URL",
                hidden: fieldTypeId != '3',
                width: 240,
                hideLabel: fieldTypeId != '3'
            }, {
                xtype: 'combo',
                fieldLabel: 'Select Field',
                store: defaultFieldGlobalStore,
//                            readOnly : true,
                id: 'customdesignerfieldselectcombo',
                displayField: 'label',
                valueField: 'id',
                queryMode: 'local',
//                            editable:false,
                hidden: fieldTypeId != '2',
                hideLabel: fieldTypeId != '2',
                width: 240,
//                            triggerAction: 'all',
                blankText: 'Select a field',
                allowBlank: fieldTypeId != '2'
            }],
        buttons: [{
                text: 'Create',
                handler: function() {
                    //                if(fieldtype=='') {
                    //                alert('x='+XPos+'y='+YPos);
                    if (fieldTypeId == 3) { // if image 
                        var field = createExtImgComponent(designerPanel, propertyPanel, fieldTypeId, Ext.getCmp('customdesignerimage').getValue(), XPos, YPos);
                    } else {
                        var label = Ext.getCmp('customdesignerfieldlabel').getValue();
                        if (fieldTypeId == '2') {
                            var rec = searchRecord(defaultFieldGlobalStore, Ext.getCmp('customdesignerfieldselectcombo').getValue(), 'id');
                            if (rec) {
                                label = rec.data.label;
                            } else {
                                return; // if record not found then return
                            }
                        }
                        var labelHTML = fieldTypeId != '2' ? label : "<span attribute='{PLACEHOLDER:" + Ext.getCmp('customdesignerfieldselectcombo').getValue() + "}'>#" + label + "#</span>";
                        field = createExtComponent(designerPanel, propertyPanel, fieldTypeId, labelHTML, XPos, YPos);
                    }
                    designerPanel.items.add(field);
                    Ext.getCmp('enterfieldlabelwin').close();
                    designerPanel.doLayout();
                },
                scope: this
            }]
    });
    var win = new Ext.Window({
        title: 'Add Field',
        closable: true,
        width: 300,
        id: 'enterfieldlabelwin',
        autoHeight: true,
        plain: true,
        modal: true,
        items: form
    });
    win.show();
}

function saveCustomDesign(json, html, defaultTemplate) {
    Ext.Ajax.request({
        url: "CustomDesign/saveDesignTemplate.do",
        params: {
            moduleid: _CustomDesign_moduleId,
            templateid: _CustomDesign_templateId,
            json: json,
            html: html,
            isdefault: defaultTemplate
        }, success: function(response, req) {
            var result = Ext.decode(response.responseText);
            if (result.success && isValidSession(result)) {
                WtfComMsgBox(["Success", "Template saved successfully."], 0);
            }
        }
    })
}

function configureItemList(obj, designerPanel, propertyPanel) {
    var jArr = eval(obj.lineitems);
    var tab1Row = document.createElement("ul");
    tab1Row.setAttribute("id", "itemlistconfig");
    tab1Row.setAttribute('style', 'padding-left: inherit;width:100%;');
//                var width = ((this.width -100) / jArr.length);
//                var width = (100 / jArr.length);
    for (var cnt = 0; cnt < jArr.length; cnt++) {
        var item = jArr[cnt];
        if (!item.hidecol) {
            var tab2Row = document.createElement("li");
            tab2Row.setAttribute("class", "tpl-colname");
            var widthInPercent = (item.colwidth - 2) + "%";// decreased 2% because while rendering header in html we used marging of 2%
            tab2Row.setAttribute('style', 'width: ' + widthInPercent);
            tab2Row.setAttribute("colwidth", item.colwidth);
            tab2Row.setAttribute("coltotal", item.coltotal);
            tab2Row.setAttribute("fieldid", item.fieldid);
            tab2Row.setAttribute("seq", item.seq);
            tab2Row.setAttribute("xtype", item.xtype);
            tab2Row.value = item.fieldid;
            tab2Row.innerHTML = decodeURIComponent(item.label);
            tab1Row.appendChild(tab2Row);
        }
    }
    var html = tab1Row.outerHTML;

    var field = Ext.create(Ext.TemplateHolder, {
        x: 0,
        y: obj.y,
        draggable: true,
//                    width : 650,
        height: 45,
        fieldTypeId: 5,
        cls: 'tpl-content',
        style: {width: '100%', height: 'auto !important', borderColor: '#B5B8C8', borderStyle: 'solid', borderWidth: '1px', position: 'absolute'},
        bodyHtml: html,
        listeners: {
            onMouseUp: function(field) {
                field.focus();
            },
            removed: function() {
                onDeleteUpdatePropertyPanel();
//                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            },
            afterrender: function() {
                this.el.on('click', function(eventObject, target, arg) {
                    var component = designerPanel.queryById(this.id)
                    if (component) {
                        getPropertyPanel(component, designerPanel, propertyPanel);
                    }
                });
            }
        }
    });
    return field;
}

function getPropertyPanel(obj, designerPanel, propertyPanel) {
    var content = obj.el.dom.textContent;
    var fieldType = obj.fieldTypeId;
    if (obj.initialCls && obj.initialCls.indexOf("tpl-content") > -1) {// it is line item field
        content = "Line Items";
    } else if (fieldType == '3') {//
        content = "Image";
    } else if (fieldType == '10') {//
        content = "Horizontal Line";
    }
    else if (fieldType == '15') {
        content = "Amount in words";
    } else if (fieldType == '16') {
        content = "PostText Master";
    } else if (fieldType == '17') {
        content = "Created by";
    }

    Ext.getCmp('setfieldproperty').setValue(content);
    Ext.getCmp('hidden_fieldID').setValue(obj.id);
    Ext.getCmp('hidden_fieldType').setValue(obj.fieldTypeId);
    Ext.getCmp('hidden_allowformatting').setValue(obj.fieldTypeId != '5');
    var myEditor = Ext.getCmp('myDesignFieldEditor');
    myEditor.hide();
    Ext.getCmp('editformattingbtn').hide();
    if (fieldType != '5' && fieldType != '3' && fieldType != '10') {// if not line items, image or horizontal line, then show htmleditor
        var value = obj.el.dom.firstChild.innerHTML == undefined ? obj.el.dom.firstChild.data : obj.el.dom.firstChild.innerHTML;
        if (is_html(value)) {
            myEditor.setValue(value);
        } else {
            myEditor.setValue('<div contenteditable="true">' + value + '</div>');
        }
        myEditor.show();
        Ext.getCmp('editformattingbtn').show();
    }

    if (fieldType == '5') {// if line item, show edit button and on click on line item configuration window
        Ext.getCmp('editformattingbtn').show();
    }
}
function deleteCustomDesignField(id, containerID) {
    Ext.MessageBox.show({
        title: 'Confirm',
        msg: 'Do you really want to delete this field?',
        icon: Ext.MessageBox.QUESTION,
        buttons: Ext.MessageBox.YESNO,
        scope: this,
        fn: function(button) {
            if (button == 'yes')
            {
                Ext.getCmp(containerID).remove(Ext.getCmp(id), true);
                Ext.getCmp('myDesignFieldEditor').setValue("");
            } else {
                return;
            }
        }
    });
}


            