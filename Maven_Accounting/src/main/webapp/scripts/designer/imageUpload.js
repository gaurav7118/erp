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

Ext.imageUploadWin = function(config){
    Ext.apply(this, config);
    this.form = new Ext.form.FormPanel({
        methodType: 'upload',
        title: 'Insert image',
        fileUpload: true,
        labelWidth: 125,
        url: "ACCCommon/saveDesignTemplateImage.do",
        bodyStyle: "border: 1px solid #B5B8C8; padding: 10px 10px 6px 10px;",
        frame: true,
        method: 'POST',
        autoHeight: true,
        autoWidth: true,
        defaults: {
            width: 175
        },
        defaultType: 'filefield',
        items: [{
                id: 'logo',
                width: 400,
                name: 'logo',
                fieldLabel: "Image path",
                hidden: this.fieldTypeId != '3',
                hideLabel: this.fieldTypeId != '3'
            }],
        buttons: []
    });

    this.urlFieldSet = new Ext.form.FormPanel({
        methodType: 'upload',
        title: 'Image URL',
        fileUpload: true,
        labelWidth: 125,
        bodyStyle: "border: 1px solid #B5B8C8; padding: 10px 10px 6px 10px;",
        frame: true,
        method: 'POST',
        autoHeight: true,
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
                hidden: this.fieldTypeId != '1',
                hideLabel: this.fieldTypeId != '1',
                allowBlank: false
            },
            {
                id: 'customdesignerimage',
                fieldLabel: "Image URL",
                hidden: this.fieldTypeId != '3',
                width: 500,
                hideLabel: this.fieldTypeId != '3'
            }, {
                xtype: 'combo',
                fieldLabel: 'Select Field',
                store: defaultFieldGlobalStore,
                id: 'customdesignerfieldselectcombo',
                displayField: 'label',
                valueField: 'id',
                queryMode: 'local',
                hidden: this.fieldTypeId != '2',
                hideLabel: this.fieldTypeId != '2',
                width: 240,
                blankText: 'Select a field',
                allowBlank: this.fieldTypeId != '2'
            }]
    });      

    this.newImage = new Ext.Panel({
        border:false,
        layout:'accordion',
        layoutConfig:{
            animate:true
        },
        items: [this.form,this.urlFieldSet]
    });
    Ext.imageUploadWin.superclass.constructor.call(this, {
        title:"Upload Image",
        bodyStyle: "background-color:#FFFFFF",
        modal: true,
        resizable: false,
        height: 400,
        width: 550,
        layout:'fit',
        items: this.newImage,
        buttons: [{
                text: 'OK',
                scope: this,
                handler: function(button) {
                    this.parentScope.resetCursorPosition();
                    if (Ext.getCmp('logo').getValue() != "" && Ext.getCmp('customdesignerimage').getValue() == "") {
                        var field = "";
                        var path1 = "";

                        this.form.getForm().submit({
                            success: function(req, response) {
                                var resultObj = eval('(' + response.response.responseText + ')');
                                resultObj = resultObj.data
                                path1 = resultObj.path;
                                this.close();
                                field = createExtImgComponent(this.designerPanel, this.propertyPanel, this.fieldTypeId, "video.jsp?id=" + path1, this.XPos, this.YPos);
                                this.designerPanel.items.add(field);
                                this.designerPanel.doLayout();

                            },
                            scope: this,
                            failure: function() {
                                alert("Template image upload failed!");
                                this.close();
                            }
                        });
                    } else if (Ext.getCmp('logo').getValue() == "" && Ext.getCmp('customdesignerimage').getValue() != "") {
                        var field1 = "";
                        if (this.fieldTypeId == 3) { // if image 
                            field1 = createExtImgComponent(this.designerPanel, this.propertyPanel, this.fieldTypeId, Ext.getCmp('customdesignerimage').getValue(), this.XPos, this.YPos);
                        } else {
                            var label = Ext.getCmp('customdesignerfieldlabel').getValue();
                            if (this.fieldTypeId == '2') {
                                var rec = searchRecord(defaultFieldGlobalStore, Ext.getCmp('customdesignerfieldselectcombo').getValue(), 'id');
                                if (rec) {
                                    label = rec.data.label;
                                } else {
                                    return; // if record not found then return
                                }
                            }
                            var labelHTML = fieldTypeId != '2' ? label : "<span attribute='{PLACEHOLDER:" + Ext.getCmp('customdesignerfieldselectcombo').getValue() + "}'>#" + label + "#</span>";
                            field1 = createExtComponent(this.designerPanel, this.propertyPanel, this.fieldTypeId, labelHTML, this.XPos, this.YPos);
                        }
                        this.designerPanel.items.add(field1);
                        this.close();
                        this.designerPanel.doLayout();
                    } else if (Ext.getCmp('logo').getValue() == "" && Ext.getCmp('customdesignerimage').getValue() == "") {
                        WtfComMsgBox(["Custom Designer", "Please Select Image."], 2);
                    } else {
                        WtfComMsgBox(["Custom Designer", "Please Select Only one Image."], 2);
                    }

                },
                scope:this
            }, {
                text: "Cancel", //"Cancel",
                scope: this,
                handler: function(btn) {
                    this.close();
                }
            }]
    })
}

Ext.extend(Ext.imageUploadWin, Ext.Window, {
    onRender: function(config) {
        Ext.imageUploadWin.superclass.onRender.call(this, config);
    }
})
