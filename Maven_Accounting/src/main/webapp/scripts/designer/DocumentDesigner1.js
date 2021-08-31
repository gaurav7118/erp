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

/*
 * 
 * @type type
 */

/*
 1 - Insert Text
 2 - Insert Field
 3 - Insert Image
 4 - Horizontal Line
 5 - Vertical Line
 6 - Draw Box
 7 - Copy Button
 8 - Paste Button
 9 - Property Window
 10 - Remove Object
 11 - Insert Line Table
 12 - Global Table
 */
var pageLayout;
var mainPanel;
Ext.onReady(function() {
    Ext.fieldID = {
        insertText:1,
        insertField:2,
        insertImage:3,
        insertHLine:4,
        insertVLine:5,
        insertDrawBox:6,
        insertTable:11,
        insertGlobalTable : 12
    }
    Ext.bandID = {
        body:1,
        footer:2,
        header:3
    }
    // Generic fields array to use in both store defs.
    Ext.define('DataObject', {
        extend: 'Ext.data.Model',
        fields: ['id', 'name']
    });
    // Define dfault header 
    Ext.define('HeaderData', {
        extend: 'Ext.data.Model',
        fields: ['id', 'label', 'dbcolumnname', 'reftablename', 'reftablefk', 'reftabledatacolumn', 'dummyvalue', 'xtype', 'customfield','pagelayoutproperty']
    });
    
    pageLayout = Ext.create('Ext.Panel', {
        columnWidth: 1, 
        id : 'pagelayout1',
//                region: 'center',
        bodyStyle: 'background-color: #FFFFFF;border-right-color: black;border-left-color: white;border-top-color: white;border-bottom-color: black',
//                maxWidth: 800,      
        width      : 900,  /* A4 Size dimensions */
        maxWidth : 900,
        height: 1123.2,
//        border: false,
        autoScroll: true,
        items : [new Ext.Panel({
//            renderTo: 'sectionPanelGrid',
            baseCls: "linkPanel",
            id :"linecanvas"
        })
         ]
    });
    pageLayout.on("render", function() {
        lineobj = new jsGraphics('linecanvas');
        componentCollection = [];
        
        Ext.Ajax.request({
            url: "CustomDesign/getDesignTemplate.do",
            method: 'POST',
            params: {
                moduleid: _CustomDesign_moduleId,
                templateid: _CustomDesign_templateId,
                bandID : Ext.bandID.body
            },
            success: function(response, req) {
                var result = Ext.decode(response.responseText);
                if (result.success && isValidSession(result)) {
                    var resData = result.data.data[0];
                    var json = resData.json
                    if (json) {
                        var field;
                        var arr = Ext.JSON.decode(json);
                        renderItemsOnDesignPanel(arr,pageLayout, this.propertyPanel);
                    }
                    if(resData.pagelayoutproperty!=undefined && resData.pagelayoutproperty!=""){
                        var pagelay = eval(resData.pagelayoutproperty);
                    
                        if(pagelay[1]==null||pagelay[1]==undefined) {
                            pagelayoutproperty[1]={};
                        } else {
                            pagelayoutproperty[1]=pagelay[1];
                        }
                        if(pagelay[0]==null||pagelay[0]==undefined) {
                            pagelayoutproperty[0]={};
                        } else {
                            pagelayoutproperty[0]=pagelay[0];
                            if( pagelayoutproperty[0].pagefontstyle){
                                Ext.get('pagelayout1-innerCt').setStyle('font-family',''+pagelayoutproperty[0].pagefontstyle.fontstyle+'');
                            }
                        }
                    }
                    
//                    pagelayoutproperty
                    //Ext.pagelayoutitems=pageLayout.body.dom.innerHTML;
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

    mainPanel = Ext.create('Ext.tab.Panel', {
//        renderTo: document.body,
        minTabWidth: 155,
        region:'center',
        titleCollapse: true,
//        activeTab: 0,
//        enableTabScroll: true,
        items: [{
            title : 'Document Designer - Graphics',
            autoScroll : true,
            items : [{
//            title: 'Document Designer - Graphics',
            id: 'designernorth',
            
            height: 78,
            border: false,
            items: [{
                    xtype: 'button',
                    iconCls: 'newImg',
                    style : 'margin:2px 0px 2px 2px',
                    scale: 'large',
                    hidden:true,
                    disabled:true,
                    handler: function() {
                        var returnConfig = [];
                        returnConfig = getTemplateConfigurations(pageLayout, defaultFieldGlobalStore);
//                    var clone = pageLayout.cloneConfig();
                        this.notAllToChange = true;
                        new Ext.sectionWin({
                            contentitems: returnConfig[0],
                            pagelayoutID: pageLayout.id                           
                        }).show();
                    }
                }, {
                    xtype: 'button',
                    iconCls: 'openImg',
                    style : 'margin:2px 0px 2px 2px',
                    autoHeight: true,
                    scale: 'large',
                    hidden:true
                }, {
                    xtype: 'button',
                    iconCls: 'editImg',
                    autoHeight: true,
                    style : 'margin:2px 0px 2px 2px',
                    scale: 'large',
                    handler: function() {
                        var returnConfig = [];
                        returnConfig = getTemplateConfigurations(pageLayout, defaultFieldGlobalStore);
                        this.notAllToChange = true;
                        var panel =  Ext.getCmp('sectionPanelContainer');
                        if(panel==null) {
                            var panel = new Ext.sectionWin({
                                contentitems: eval(returnConfig[0]),
                                pagelayoutID: pageLayout.id,
                                bandID: Ext.bandID.body
                            });
                            mainPanel.add(panel);
                            mainPanel.setActiveTab(panel);
                            mainPanel.doLayout();
                        }else{
                            if(panel.bandID != Ext.bandID.body) {
                                WtfComMsgBox(["Alert","Sorry, you can't open multiple tabs at a time.<br>Please, close opened tab first to open new tab."], 3);
                            }
                            mainPanel.setActiveTab(panel);
                            mainPanel.doLayout();
                        }                        
                    }
                }, {
                    xtype: 'button',
                    iconCls: 'saveImg',
                    style : 'margin:2px 0px 2px 2px',
                    autoHeight: true,
                    scale: 'large',
                    handler : function() {
                        var returnConfig = [];
                        var saveflag=true;
                        var templatename='';
                        if(!pagelayoutproperty)
                            pagelayoutproperty="";
                      
//                       var newpagelayoutproperty=[ pagelayoutproperty];
                        var pagelayoutproperty1=Ext.JSON.encode(pagelayoutproperty);
                        returnConfig = getTemplateConfigurations(pageLayout, defaultFieldGlobalStore);
                        saveCustomDesign(returnConfig[0], returnConfig[1], 0,saveflag,templatename,pagelayoutproperty1, false);
                    }
                }, {
                    xtype: 'button',
                    iconCls: 'saveasImg',
                    style : 'margin:2px 0px 2px 2px',
                    autoHeight: true,
                    scale: 'large',            
                    handler : function() {
                       var returnConfig = [];
                       var saveflag=false;
                       if(!pagelayoutproperty)
                             pagelayoutproperty="";
                       returnConfig = getTemplateConfigurations(pageLayout, defaultFieldGlobalStore);
                       saveAsWindow(returnConfig[0], returnConfig[1], 0,saveflag,pagelayoutproperty);
                    }
                }, {
                    xtype: 'button',
                    iconCls: 'removeImg',
                    style : 'margin:2px 0px 2px 2px',
                    hidden : true,
                    autoHeight: true,
                    scale: 'large'
                }, {
                    xtype: 'button',
                    iconCls: 'samplePreviewImg',
                    style : 'margin:2px 0px 2px 2px',
                    autoHeight: true,
                    scale: 'large',
                    handler : function() {
                        var returnConfig = [];
                        returnConfig = getTemplateConfigurations(pageLayout, defaultFieldGlobalStore);
                        var reqform = document.getElementById('designpanelpreview');
                        reqform.elements['json'].value = returnConfig[0];
                        reqform.elements['html'].value = returnConfig[1];
                        reqform.elements['moduleid'].value = _CustomDesign_moduleId;
                        reqform.submit();
                    }
                }, {
                    xtype: 'button',
                    iconCls: 'pageLayoutImg',
                    style : 'margin:2px 0px 2px 2px',
                    autoHeight: true,
                    scale: 'large',
                    handler : function() {
                        var winObj = new Ext.PageLayoutPropWin({
                            isnewdocumentdesigner: false
                        });
                        winObj.show();
                    }
                }, {
                    xtype: 'button',
                    iconCls: 'pageFooterImg',
                    style : 'margin:2px 0px 2px 2px',
                    autoHeight: true,
                    scale: 'large',
                    handler : function() {
                        this.notAllToChange = true;
                        var panel =  Ext.getCmp('sectionPanelContainer');
                        if(panel==null || panel==undefined) {                            
                            Ext.Ajax.request({
                                url: "CustomDesign/getDesignTemplate.do",
                                method: 'POST',
                                params: {
                                    moduleid: _CustomDesign_moduleId,
                                    templateid: _CustomDesign_templateId,
                                    bandID : Ext.bandID.footer
                                },
                                success: function(response, req) {
                                    var result = Ext.decode(response.responseText);
                                    if (result.success && isValidSession(result)) {
                                        var resData = result.data.data[0];
                                        var pagefooterjson = resData.pagefooterjson
                                        if (pagefooterjson) {
                                            var arr = Ext.JSON.decode(pagefooterjson);
                                            var panel = new Ext.sectionWin({
                                                contentitems: arr,
                                                pagelayoutID: pageLayout.id,
                                                bandID : Ext.bandID.footer
                                            });
                                            mainPanel.add(panel);
                                            mainPanel.setActiveTab(panel);
                                            mainPanel.doLayout();
                                        }
                                    }
                                }
                            }) // End of Ajax Request 
                        } else {
                            if(panel.bandID != Ext.bandID.footer) {
                                WtfComMsgBox(["Alert","Sorry, you can't open multiple tabs at a time.<br>Please, close opened tab first to open new tab."], 3);
                            }
                            mainPanel.setActiveTab(panel);
                            mainPanel.doLayout();
                        }
                        
                    }
                },{
                    xtype: 'button',
                    iconCls: 'pageHeaderImg',
                    style : 'margin:2px 0px 2px 2px',
                    autoHeight: true,
                    scale: 'large',
                    handler : function() {
                        this.notAllToChange = true;
                        var panel =  Ext.getCmp('sectionPanelContainer');
                        if(panel==null || panel==undefined) {                            
                            Ext.Ajax.request({
                                url: "CustomDesign/getDesignTemplate.do",
                                method: 'POST',
                                params: {
                                    moduleid: _CustomDesign_moduleId,
                                    templateid: _CustomDesign_templateId,
                                    bandID : Ext.bandID.header
                                },
                                success: function(response, req) {
                                    var result = Ext.decode(response.responseText);
                                    if (result.success && isValidSession(result)) {
                                        var resData = result.data.data[0];
                                        var pageheaderjson = resData.pageheaderjson
                                        if (pageheaderjson) {
                                            var arr = Ext.JSON.decode(pageheaderjson);
                                            var panel = new Ext.sectionWin({
                                                contentitems: arr,
                                                pagelayoutID: pageLayout.id,
                                                bandID : Ext.bandID.header
                                            });
                                            mainPanel.add(panel);
                                            mainPanel.setActiveTab(panel);
                                            mainPanel.doLayout();
                                        }
                                    }
                                }
                            }) // End of Ajax Request 
                        } else {
                            if(panel.bandID != Ext.bandID.footer) {
                                WtfComMsgBox(["Alert","Sorry, you can't open multiple tabs at a time.<br>Please, close opened tab first to open new tab."], 3);
                            }
                            mainPanel.setActiveTab(panel);
                            mainPanel.doLayout();
                        }
                        
                    }
                }, {
                    xtype: 'button',
                    iconCls: 'quiteImg',
                    style : 'margin:2px 0px 2px 2px',
                    autoHeight: true,
                    scale: 'large',
                    handler : function() {
                        close();
                    }
                }]
        },{
//            region: 'center',
            xtype: 'panel',
            title: 'Page View',
            border: false,
            items: [{
//                    region: 'north',
                    height: 25,
                    border: false,
                    style: 'background-color: #FFFFFF;',
                    id: 'horizontalRuler'
            }]
        }, {
            border: false,
            layout:'column',
//            layout : 'border',
            items: [{
//                region: 'west',
                width: 25,
                height: 1200,
                border: false,
                id: 'verticalRuler'
            },pageLayout]
        }]
        }]
    })
    
    var viewport = new Ext.Viewport({
        layout: 'border',
        items:[mainPanel]
    })
    viewport.doLayout();
})



