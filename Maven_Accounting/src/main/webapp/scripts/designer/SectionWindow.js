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
Ext.sectionWin = function(config){
   config.designerPanel = Ext.create('Ext.Panel', {
        id: 'sectionPanelGrid', 
        columnWidth: 0.7,
        bodyStyle: 'background-color: #FFFFFF;border-right-color: black;border-left-color: white;border-top-color: white;border-bottom-color: black',
        width      : 900,  /* A4 Size dimensions */
        maxWidth : 900,
        height: 1123.2,
        autoScroll: true,
        autoHeight: true,
        margins: '0 0 0 3',
        layout: 'absolute',
//        ,
//        html : config.contents,
        items: [new Ext.Component({
		id: 'contentImage',
                width: 16,
                height: 16,
                maxWidth: 16,
                maxHeight: 16,                
		autoEl: {tag: 'img', src: Ext.BLANK_IMAGE_URL}
	}),new Ext.Panel({
            baseCls: "linkPanel",
            id :"linecanvas"
        })
//        ,new Ext.Component({
//            id: 'dottedvline',
//            cls : 'dottedvline'
////            "<div id='dottedvline' class='dottedvline'></a>"
//        })
    ]
    });
    config.designerPanel.on("render", function() {
        if(pagelayoutproperty[0].pagefontstyle){
            Ext.get('sectionPanelGrid-innerCt').setStyle('font-family',''+pagelayoutproperty[0].pagefontstyle.fontstyle+'');
        }
        componentCollection = [];
        lineobj = new jsGraphics('linecanvas');
        renderItemsOnDesignPanel(this.contentitems,this.designerPanel, this.propertyPanel);
    }, this);
    //Left hand side for html editor and selected field -NeerajD 
    this.propertyPanel = Ext.create('Ext.Panel', {
        columnWidth: 0.30,
        id:'propertypanel',
        height: 600,
        border : false,
        items: [{
            xtype : 'fieldset',
            title: 'Item Properties',
            items: [{
                    xtype: 'fieldcontainer',
    //                width: 400,
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
                        },{
                            xtype: 'hiddenfield',
                            id: 'hidden_fieldName',
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
                    text: 'Edit & Update',
                    id: 'editformattingbtn',
                    margin: '10 0 30 5',
                    hidden: true,
                    scope : this,
                    handler: function(button) {
                        if (Ext.getCmp('hidden_fieldType').getValue() == '11') {
                            var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                            var Posx=selectedfield.x;
                            var Posy=selectedfield.y;
                            var isEdit=true;
                            openProdWindowAndSetConfig(selectedfield, this.designerPanel.id,isEdit,Posx,Posy,documentLineColumns);
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
                        }
                    }
                },
                {
                    xtype: 'button',
                    text: 'Delete',
                    hidden : true,
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
                                        removePositionObjectFromCollection(Ext.getCmp('hidden_fieldID').getValue());
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
                    width: 350,
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
            }]
        }]
    });



    Ext.apply(this,{
        constrainHeader :true,
        id : "sectionPanelContainer" ,
//        buttons: [{
//            text: 'Update',  //'Save',
//            scope: this,
//            handler:this.updateForm
//        }, {
//            text: 'Close',  //'Cancel',
//            scope: this,
//            handler:this.closeForm
//        }],
        items : [
            {
//            region: 'north',
            id : 'sectionnorth',
            height: 73,
            border: false,
            viewConfig: {
                plugins: {
                    ddGroup: 'DocDesigner',
                    ptype: 'gridviewdragdrop',
                    enableDrop: false
                }
            },
            items : [{
                xtype : 'button',
                iconCls : 'selectFieldImg',
                autoHeight : true,
                scale : 'large',
                scope : this,
                handler : function() {
                    this.addComponent('2');
                }
            },{
                xtype : 'button',
                iconCls : 'insertTextImg',
                autoHeight : true,
                scale : 'large',
                scope : this,
                handler : function() {
                    this.addComponent('1');
                }
            },{
                xtype : 'button',
                iconCls : 'insertImg',
                autoHeight : true,
                scale : 'large',
                scope : this,
                handler : function() {
                    this.addComponent('3');
                }
            },{
                xtype : 'button',
                iconCls : 'hLineImg',
                autoHeight : true,
                scale : 'large',
                scope : this,
                handler : function() {
                    this.addComponent('4');
                }
            },{
                xtype : 'button',
                iconCls : 'vLineImg',
                autoHeight : true,
                hidden : true,
                scope : this,
                scale : 'large'
            },{
                xtype : 'button',
                iconCls : 'drawBoxImg',
                autoHeight : true,
                scope : this,
                scale : 'large',
                handler : function() {
                    this.addComponent('6');
                }
            },{
                xtype : 'button',
                iconCls : 'copyImg',
                autoHeight : true,
                scope : this,
                scale : 'large',
                id : 'copyformattingbtn',
                handler : function() {
                      this.addComponent('7'); 
                }
                
            },{
                xtype : 'button',
                iconCls : 'pasteImg',
                autoHeight : true,
                scope : this,
                scale : 'large',
                id : 'pasteformattingbtn',
                handler : function() {
                    this.addComponent('8'); 
                }
                
            },{
                xtype : 'button',
                iconCls : 'inserttable',
                autoHeight : true,
                scope : this,
                scale : 'large',
                handler : function() {
                    this.addComponent('11'); 
                }
            },{
                xtype : 'button',
                iconCls : 'propertiesImg',
                autoHeight : true,
                scope : this,
                scale : 'large',
                disabled:true,
                id:'propertiesbox1',
                handler : function() {
                    this.addComponent('9'); 
                }
                
            },{
                xtype : 'button',
                iconCls : 'removeObjImg',
                autoHeight : true,
                scope : this,
                scale : 'large',
                handler : function() {
                    this.addComponent('10'); 
                }
            }, {
                xtype: 'button',
                iconCls: 'updateImg',
                autoHeight: true,
                scale: 'large',
                scope : this,
                handler : this.updateForm
            }]
        },{
//            region: 'center',
            xtype: 'panel',
            title : 'Layout',
            border: false,
//            layout: 'border',
            items : [{
//                region: 'north',
                height: 25,
                border: false,
                style : 'background-color: #FFFFFF;',
                id : 'sectionhorizontalRuler'
            }]
        },{
               layout:'column',
                border: false,
//                layout: 'border',
                items : [{
//                    region: 'west',
                    width: 25,
                    height: 1200,
                    border: false,
                    id : 'sectionverticalRuler' 
                },config.designerPanel,this.propertyPanel]
                
        }]
//        }
    },config);
    Ext.sectionWin.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true,
        'cancel':true,
        'loadingcomplete':true
    });
}

Ext.extend(Ext.sectionWin, Ext.Panel, {
    title : 'Edit Template',
    region:'center',
    closable:true,
    width:1300,
    height:720,
//    draggable:true,
//    resizable:true,
    autoScroll:true,
//    id : 'enterfieldlabelwin',                   
//    plain:true,
//    modal:true,
//    layout:'border', 
    listeners: {
        show: function() {
            var formPanelDropTargetEl = this.designerPanel.body.dom;
            var formPanelDropTarget = Ext.create('Ext.dd.DropTarget', formPanelDropTargetEl, {
                ddGroup: 'DocDesigner',
                notifyEnter: function(ddSource, e, data) {
                    //Add some flare to invite drop.
                    this.designerPanel.body.stopAnimation();
                    this.designerPanel.body.highlight();
                },notifyDrop: function(ddSource, e, data) {
                    var event = e;
                }
            });
            
            this.designerPanel.body.on({
                'click': function (e) {
                     Ext.getCmp('propertiesbox1').setDisabled(true);
                     Ext.getCmp('propertypanel').disable();
                    var parentPosition = getPosition(e.currentTarget);
                    var correctionXMargin = 10;
                    var correctionYMargin = 10;
                    var xPosition = e.getX() - parentPosition.x - correctionXMargin;
                    var yPosition = e.getY() - parentPosition.y - correctionYMargin;
                    Ext.getCmp('sectionPanelGrid').cursorCustomX = xPosition;
                    Ext.getCmp('sectionPanelGrid').cursorCustomY = yPosition;
                    Ext.getCmp('contentImage').setPosition(xPosition , yPosition);
                    Ext.getCmp('contentImage').getEl().dom.src = '../../images/designer/pointer2.png';
                    clearHiddenFieldID();
                }
            },this);
        }
    },
    onRender: function(config) {
        Ext.sectionWin.superclass.onRender.call(this, config);
    },
   
   updateForm:function(){
        var returnConfig = [];
        if(typeof pagelayoutproperty[0] !="object" && !IsJsonString(pagelayoutproperty[0])) {
            pagelayoutproperty[0] = {};
        }
        if(typeof pagelayoutproperty[1] !="object" && !IsJsonString(pagelayoutproperty[1])){
            pagelayoutproperty[1]={};
            }
        returnConfig = getTemplateConfigurations(this.designerPanel, defaultFieldGlobalStore);
        
        saveCustomDesign(returnConfig[0], returnConfig[1], 0,true,'',JSON.stringify(pagelayoutproperty), true,this.bandID);
        this.closeForm();
//        var arr = eval(returnConfig[0]);
////        var pageLayout = Ext.getCmp('pagelayoutid');
//        pageLayout.items.removeAll();
//        for (var cnt = 0; cnt < arr.length; cnt++) {
//            var obj = arr[cnt];
//            
//            
//            if (obj.fieldTypeId == '5') {
//                var field = configureItemList(obj, pageLayout, this.propertyPanel)
//            } else if (obj.fieldTypeId == '3') {
//                field = createExtImgComponent(pageLayout, this.propertyPanel, obj.fieldTypeId, obj.src, obj.x, obj.y, obj);
//            } else if (obj.fieldTypeId == '10') {
//                field = createHoLineComponent(pageLayout, obj.fieldTypeId, this.propertyPanel, obj.x, obj.y,obj.width)
//            } else {
//                var labelHTML = obj.fieldTypeId != '2' ? obj.labelhtml : "<span attribute='{PLACEHOLDER:" + obj.fieldid + "}'>" + obj.labelhtml + "</span>";
//                field = createExtComponent(pageLayout, undefined, obj.fieldTypeId, labelHTML, obj.x, obj.y, {
//                    width: obj.width,
//                    height: obj.height
//                });
//            }
//            pageLayout.items.add(field);
//        }
//        pageLayout.doLayout();
////        pageLayout.body.dom.innerHTML = this.designerPanel.body.dom.innerHTML;
////        this.closeForm();
    },
 
    closeForm:function(){
        this.close();
        this.destroy();
    },
            
    addComponent: function(componentID) {
        var PosX = this.designerPanel.cursorCustomX;
        var PosY = this.designerPanel.cursorCustomY;
        if(PosX>0 && PosY>0) {
            switch (componentID) {
                case '1' : // Insert Text
                    this.takeFieldLabel(componentID);
                    Ext.getCmp('propertiesbox1').setDisabled(false);
                    break;
                case '2' : // Insert placeholder
                    this.takeFieldLabel(componentID);
                    Ext.getCmp('propertiesbox1').setDisabled(false);
                    break;
                case '3' : // Insert Image
                    new Ext.imageUploadWin({
                        designerPanel : this.designerPanel,
                        fieldTypeId : componentID,
                        XPos : PosX,
                        YPos : PosY,
                        parentScope : this
                    }).show();
                    break;     
                case '4': // Horizontal Line
                     var widthnew=0; 
                     var field=createHoLineComponent(this.designerPanel, componentID,this.propertyPanel, PosX, PosY,widthnew);
                     this.designerPanel.add(field)
                    break;
                case '6': // Draw Box
                         var heightnew=0;
                         var backgroundColor="";
                        var field = createDrawComponent(this.designerPanel,this.propertyPanel, componentID,this.changedhtml, PosX, PosY,widthnew,heightnew,backgroundColor);
                        this.resetCursorPosition();
                        this.designerPanel.add(field);
                        Ext.getCmp('propertiesbox1').setDisabled(false);
                   break;
                case '8' :
                    pasteButton(this.designerPanel,this.propertyPanel);
                    break;   
                    
                 case '11':
                    var dtable=new Ext.designtable({
                        parentScope : this
                    });
                    dtable.show();
            }
        }
        
        if(Ext.getCmp('hidden_fieldID')!="") {
            switch (componentID) {
                 case '7' :
                        copyButton(this.designerPanel,this.propertyPanel);
                    break;
                 case '9' :
                    var selectedFieldID=Ext.getCmp('hidden_fieldType').getValue();
                    if(selectedFieldID==Ext.fieldID.insertText||selectedFieldID==Ext.fieldID.insertField){
                        var newvalue= Ext.getCmp('hidden_fieldID').getValue();  
                        var division = document.getElementById(newvalue);
                        var selectcolor = division.style.borderColor; 
                        if(selectcolor!=""||selectcolor!=undefined){
                        var digits = /(.*?)rgb\((\d+), (\d+), (\d+)\)/.exec(selectcolor);
                        var red = parseInt(digits[2]);
                        var green = parseInt(digits[3]);
                        var blue = parseInt(digits[4]);
                        //        var rgb = blue | (green << 8) | (red << 16);
                        //        var getselectedcolor=digits[1] + rgb.toString(16);
                        var getselectedcolor=rgbToHex(red,green,blue);
                        selectcolor=getselectedcolor.toUpperCase();
                        selectcolor=selectcolor.replace(/#/gi,'');
                        }
                        var propertywin= new Ext.PropertyWin({
                            designerPanel : this.designerPanel.id,
                            fieldId : Ext.getCmp('hidden_fieldID').getValue(),
                            fieldName : Ext.getCmp('setfieldproperty').getValue(),
                            selectedFieldID:selectedFieldID,
                            selectcolor:selectcolor
                        });
                        propertywin.show();        
                        Ext.getCmp('propertiesbox1').setDisabled(true);
                    }                          
                    else if(selectedFieldID==Ext.fieldID.insertDrawBox){
                        this.newvalue= Ext.getCmp('hidden_fieldID').getValue();  
                        var div2 = document.getElementById(this.newvalue);
                        this.colorPanel = div2.style.backgroundColor;                                    
                                
                        var propertiesbox= new Ext.PropertiesBoxWin({
                            colorPanel : this.colorPanel
                        });
                        propertiesbox.show();
                        propertiesbox.doLayout();
                        Ext.getCmp('propertiesbox1').setDisabled(true);
                    }                          
//                    else{
//                        this.newvalue= Ext.getCmp('hidden_fieldID').getValue();  
//                        var div2 = document.getElementById(this.newvalue);
//                        this.colorPanel = div2.style.backgroundColor;                                    
//                                
//                        var propertiesbox= new Ext.PropertiesBoxWin({
//                            colorPanel : this.colorPanel
//                        });
//                        propertiesbox.show();
//                        propertiesbox.doLayout();
//                        Ext.getCmp('propertiesbox1').setDisabled(true);
//                    }      
                    break;
                case '10':
                        deleteCustomDesignField();
                    break;
            }
        }
    },
            
    takeFieldLabel : function(fieldTypeId) {
        var form = new Ext.form.FormPanel({
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
                    fieldLabel: 'Field Label',
                    id: 'customdesignerfieldlabel',
                    name: 'fieldlabel',
                    value: '',
                    width: 300,
//                    maskRe: /[A-Za-z0-9_: ]+/,
                    hidden: fieldTypeId != '1',
                    hideLabel: fieldTypeId != '1',
                    allowBlank: false
                }, {
                    xtype: 'combo',
                    fieldLabel: 'Select Field',
                    store: defaultFieldGlobalStore,
                    id: 'customdesignerfieldselectcombo',
                    displayField: 'label',
                    valueField: 'id',
                    queryMode: 'local',
                    hidden: fieldTypeId != '2',
                    hideLabel: fieldTypeId != '2',
                    width: 300,
                    blankText: 'Select a field',
                    allowBlank: fieldTypeId != '2',
                      listeners:{
                    scope: this,
                    'select': function(combo, selection){
                        var htmllabel =selection[0].data.label
                        this.label=htmllabel;
                        var applycolorcomponent=Ext.getCmp('selectfieldapplycolor'); 
                        applycolorcomponent.enable();
                    }
                }
                },{
                xtype:'fieldset',
                checkboxToggle:true,
                title: 'Apply Border Color',
                autoHeight:true,
                id:'selectfieldapplycolor', 
                disabled:fieldTypeId==Ext.fieldID.insertText?false:true,
                collapsed: true,
                width:150,
                autoHeight:true,
                fieldDefaults: {
                    labelAlign: 'left',
                    labelWidth: 105
                },
                items:[
                this.selectColorPalette = new Ext.ColorPalette({
                    cls:'palette',
                    value:"",
                    id :'fieldbordercolor',
                    scope:this,
                    height:70,
                    colors: ["FFFFFF","CC3333", "DD4477", "994499", "6633CC", "336699", "3366CC", "22AA99", "329262", "109618", "66AA00", "AAAA11", "D6AE00", "EE8800", "DD5511", "A87070", "8C6D8C", "627487", "7083A8", "5C8D87", "898951", "B08B59","000000","008B8B","00FFFF","4B0082","6B8E23","808080","87CEEB","87CEFA","ADFF2F"]
                })]
    }],
            buttons: [{
                    text: 'Create',
                    handler: function() {
                        var label = Ext.getCmp('customdesignerfieldlabel').getValue();
                        var selectedapplyfieldcolor = Ext.getCmp('fieldbordercolor').getValue();
                        if(selectedapplyfieldcolor){
                        var res = selectedapplyfieldcolor.match(/#/g);
                        if(res==null){
                            selectedapplyfieldcolor="#"+selectedapplyfieldcolor;
                        }
                    }
                        var labelHTML = (fieldTypeId != '2') ? label : "<span attribute='{PLACEHOLDER:" + Ext.getCmp('customdesignerfieldselectcombo').getValue() + "}'>#" + this.label + "#</span>";
                        var field = createExtComponent(this.designerPanel, undefined, fieldTypeId, labelHTML, this.designerPanel.cursorCustomX, this.designerPanel.cursorCustomY,undefined,selectedapplyfieldcolor);
                        this.label=undefined;
                        this.designerPanel.items.add(field);
                        this.resetCursorPosition();
                        Ext.getCmp('enterfieldlabelwin').close();
                        this.designerPanel.doLayout();
                        win.destroy();
                    },
                    scope: this
                },{
                    text: 'Close',
                    handler: function() {
                        Ext.getCmp('enterfieldlabelwin').close();
                    },
                    scope: this
                }]
        });
        var win = new Ext.Window({
            title: fieldTypeId == '1' ? 'Add Text' : 'Select Field',
            closable: true,
            width: 400,
            id: 'enterfieldlabelwin',
            autoHeight: true,
            plain: true,
            modal: true,
            items: form
        });
        win.show();
    },
    
    resetCursorPosition : function() {
        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
        this.designerPanel.cursorCustomX = 0;
        this.designerPanel.cursorCustomY = 0;
    }
})