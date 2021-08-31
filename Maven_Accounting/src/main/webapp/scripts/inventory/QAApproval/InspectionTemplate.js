
function callInspectionTemplateMaster(){
    if (Wtf.account.companyAccountPref.activateQAApprovalFlow) {
        var inventoryTab = Wtf.getCmp("InspectionTemplateMasterId");
        if(inventoryTab == null){
            inventoryTab = new Wtf.InspectionTemplatePanel({
                title:WtfGlobal.getLocaleText("acc.lp.inspectiontemplate"), //Inspection Template
                id:"InspectionTemplateMasterId",
                layout:"fit",
                iconCls:'accountingbase masterconfiguration',
                closable:true
            });
            Wtf.getCmp("as").add(inventoryTab);
        }
        Wtf.getCmp("as").setActiveTab(inventoryTab);
    }else{
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.lp.inspectiontemplate.QA.Setting")],0);
    }
}

Wtf.InspectionTemplatePanel = function (config){
    Wtf.apply(this,config);
    Wtf.InspectionTemplatePanel.superclass.constructor.call(this);
}

Wtf.extend(Wtf.InspectionTemplatePanel,Wtf.Panel,{
    
    title:WtfGlobal.getLocaleText("acc.lp.inspectiontemplate"), //Inspection Template
    iconCls : 'iconwin',
    scrollable:true,
    layout:'fit',
    border:false,
    
    initComponent:function (){
        Wtf.InspectionTemplatePanel.superclass.initComponent.call(this);
        this.initTemplatePanel();
        this.initTemplateDetailPanel();

        this.mainPanel = new Wtf.Panel({
            layout:'border',
            border:false,
            items:[this.templateGrid,this.templatedetailGrid]
        });

        this.add(this.mainPanel);
        
    },
    
    initTemplatePanel:function (){
        
        var templategridRec = new Wtf.data.Record.create([
        {
            name:"templateId"
        },
        {
            name:"templateName"
        },
        {
            name:"templateDescription"
        }
        ]);
        
        var templatereader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },templategridRec);
        
        var templategridStore = new Wtf.data.Store({
            url:'INVTemplate/getInspectionTemplateList.do',
            reader:templatereader
        });
        
        templategridStore.load();
        
        var templatesm = new Wtf.grid.RowSelectionModel({
            width:25,
            singleSelect:true
        });
        var templatecm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.inspection.grid.header.template"),
                dataIndex:"templateName",
                renderer: function(v,m,r){
                    return "<div wtf:qtip='"+r.get('templateDescription')+"'>"+v+"</div>";
                }
            },{
                width:50,
                header:WtfGlobal.getLocaleText("acc.masterConfig.costCenter.action"),  //'Action',
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
                }
            }
            ]);
        var addTemplateButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.add"),
            iconCls:getButtonIconCls(Wtf.etype.menuadd),
            tooltip:WtfGlobal.getLocaleText("acc.lp.inspectiontemplate.new.template"),
            handler: this.addTemplateHandler,
            scope:this
        });
        var editTemplateButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),
            iconCls:getButtonIconCls(Wtf.etype.menuedit),
            tooltip:WtfGlobal.getLocaleText("acc.lp.inspectiontemplate.edit.template"),
            title:WtfGlobal.getLocaleText("acc.lp.inspectiontemplate"), //Inspection Template
            handler: this.editTemplateHandler,
            scope:this
        });
        
        var tbarArr = [];
        
        tbarArr.push(addTemplateButton);
        tbarArr.push('-', editTemplateButton);
       
        this.templateGrid=new Wtf.grid.GridPanel({
            region: 'west',
            width:250,
            store: templategridStore,
            cm: templatecm,
            sm:templatesm,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: true
            },
            tbar:tbarArr
        })
        
        var selModel = this.templateGrid.getSelectionModel();
        selModel.on('selectionchange', this.templateSelectionhandler, this);
        
        this.templateGrid.on('cellclick', this.onCellClickHandler, this);
    },
    initTemplateDetailPanel:function (){
        
        var iareaRec = new Wtf.data.Record.create([
        {
            name:"templateId"
        },
        {
            name:"areaId"
        },
        {
            name:"areaName"
        },
        {
            name:"faults"
        },
        {
            name:"passingValue"
        }
        ]);
        
        var iareareader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },iareaRec);
        
        var iareaStore = new Wtf.data.Store({
            url:'INVTemplate/getInspectionAreaList.do',
            reader:iareareader
        });
                
        var iareasm = new Wtf.grid.RowSelectionModel({
            width:25,
            singleSelect:true
        });
        var iareacm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.inspection.grid.header.template.area"),
                dataIndex:"areaName"
            },{
                header:WtfGlobal.getLocaleText("acc.inspection.temp.fieldLabel.faults"),
                dataIndex:"faults",
                renderer: function(v){
                    var tipv = WtfGlobal.replaceAll(v, ";",  "<br>");
                    v = WtfGlobal.replaceAll(v, ";",  "; ");
                    return "<div wtf:qtip='"+tipv+"'>"+v+"</div>"
                }
            },{
                /**
                 * Column for MRP QA flow
                 */
                header:WtfGlobal.getLocaleText("acc.inspection.temp.fieldLabel.passingValue"),
                dataIndex:"passingValue",
                hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP)
            },{
                width:50,
                header:WtfGlobal.getLocaleText("acc.masterConfig.costCenter.action"),  //'Action',
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
                }
            }]);
        var addInspectionAreaButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.inspection.area.add"),
            iconCls:getButtonIconCls(Wtf.etype.menuadd),
            tooltip:WtfGlobal.getLocaleText("acc.inspection.area.add.tooltip"),
            handler: this.addInspectionAreaHandler,
            scope:this
        });
        var editInspectionAreaButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.inspection.area.edit"),
            iconCls:getButtonIconCls(Wtf.etype.menuedit),
            tooltip:WtfGlobal.getLocaleText("acc.inspection.area.edit.tooltip"),
            handler: this.editInspectionAreaHandler,
            scope:this
        });
        
        var tbarArr = [];
        
        tbarArr.push( addInspectionAreaButton);
        tbarArr.push('-', editInspectionAreaButton);
        
        this.templatedetailGrid=new Wtf.grid.GridPanel({
            region: 'center',
            disabled:true,
            store: iareaStore,
            cm: iareacm,
            sm:iareasm,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: true
            },
            tbar:tbarArr
        })
        
        this.templatedetailGrid.on('cellclick',this.onCellClickHandler, this);
        
    },
    addTemplateHandler: function(){
        this.openAddEditTemplateForm();
    },
    editTemplateHandler: function(){
        var recs = this.templateGrid.getSelectionModel().getSelections();
        if(recs.length == 1){
            this.openAddEditTemplateForm(recs[0])
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.inspection.temp.select.temp")], 0);
            return false;
        }
    },
    addInspectionAreaHandler: function(){
        var templateRecs = this.templateGrid.getSelectionModel().getSelections();
        if(templateRecs.length == 1){
            this.openAddEditInspectionForm(templateRecs[0].get('templateId'))
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.inspection.temp.select.insp.temp")], 0);
            return false;
        }
    },
    editInspectionAreaHandler: function(){
        var recs = this.templatedetailGrid.getSelectionModel().getSelections();
        if(recs.length == 1){
            var templateRecs = this.templateGrid.getSelectionModel().getSelections();
            if(templateRecs.length == 1){
                this.openAddEditInspectionForm(templateRecs[0].get('templateId'), recs[0])
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.inspection.temp.select.insp.temp")], 0);
                return false;
            }
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.inspection.temp.select.insp.area")], 0);
            return false;
        }
    },
    openAddEditTemplateForm: function(templateRec){
        var templateForm = new Wtf.TemplateForm({
            TemplateRec: templateRec,
            TemplateGridStore : this.templateGrid.getStore()
        });
        var formWindow = new Wtf.AddEditFormWindow({
            FormPanel:templateForm,
            WinTitle : WtfGlobal.getLocaleText("acc.inspection.temp.Edit.Temp"),
            WinDetail : WtfGlobal.getLocaleText("acc.inspection.temp.add.Edit.Temp"),
            WinImage :'images/defaultuser.png',
            
            id: "AddEditTemplateId",
            title : WtfGlobal.getLocaleText("acc.inspection.temp.Edit.Temp"),
            layout : 'fit',
            width:400,
            height:300,
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
                handler:function (){
                    templateForm.saveTemplate();
                },
                scope:this
            },
            {
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                handler:function (){
                    formWindow.close();
                },
                scope:this
            }]
        });
        formWindow.show();
        
        templateForm.on('destroy', function(){
            formWindow.close();
        }, this)
    },
    openAddEditInspectionForm: function(templateId, inspectionRec){
        var inspectionForm = new Wtf.InspectionForm({
            InspectionRec : inspectionRec,
            InspectionGridStore : this.templatedetailGrid.getStore()
        });
        var formWindow = new Wtf.AddEditFormWindow({
            FormPanel:inspectionForm,
            WinTitle: WtfGlobal.getLocaleText("acc.inspection.temp.add.Edit.Detail"),
            WinDetail: WtfGlobal.getLocaleText("acc.inspection.temp.fill.detail"),
            WinImage :'images/defaultuser.png',
            
            id: "AddEditInspectionDetailId",
            title: WtfGlobal.getLocaleText("acc.inspection.temp.add.Edit.Insp"),
            layout : 'fit',
            width:400,
            height:(Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP) ? 350 : 300,
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
                handler:function (){
                    inspectionForm.saveInspectionDetail(templateId);
                },
                scope:this
            },
            {
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                handler:function (){
                    formWindow.close();
                },
                scope:this
            }]
        });
        formWindow.show();
        
        inspectionForm.on('destroy', function(){
            formWindow.close();
        }, this)
    },
    
    templateSelectionhandler: function(){
        var recs = this.templateGrid.getSelectionModel().getSelections();
        if(recs.length == 1){
            this.loadInspectionDetails(recs[0].get('templateId'));
            this.templatedetailGrid.enable();
        }else{
            this.templatedetailGrid.disable();
        }
    },
    loadInspectionDetails: function (templateId){
        if(templateId != undefined){
            this.templatedetailGrid.getStore().load({
                params: {
                    templateId : templateId
                }
            });
        }
    },
    onCellClickHandler:function(grid,rowIndex,colIndex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var rec=store.getAt(rowIndex);
            if(rec.get('areaId') == null){
                this.deleteInspectionTemplate(rec.get('templateId'), rec.get('templateName'))
            }else{
                this.deleteInspectionArea(rec.get('areaId'), rec.get('areaName'))
            }
        }
    },
    deleteInspectionTemplate: function(templateId, templateName){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inspection.temp.delete.insp.temp") + " <b>" + templateName + "</b>?", function (btn) {
            if(btn!="yes") return;
            this.deleteFromServer('INVTemplate/deleteInspectionTemplate.do',{
                templateId : templateId
            },
            function(response){
                if(response.success){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],0);
                    this.templatedetailGrid.getStore().reload();
                    this.templateGrid.getStore().reload();
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],0);
                }
            });
        }, this);
    },
    deleteInspectionArea: function(areaId, areaName){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inspection.temp.delete.insp.area")+"<b>"+areaName+"</b>?", function(btn){
            if(btn!="yes") return;
            this.deleteFromServer('INVTemplate/deleteInspectionArea.do',{
                areaId : areaId
            },
            function(response){
                if(response.success){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],0);
                    this.templatedetailGrid.getStore().reload();
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],0);
                }
            });
        }, this);
    },
    deleteFromServer: function(url, params, successFn){
        Wtf.Ajax.requestEx({
            url:url,
            params: params
        },this, 
        successFn
        );
    }
    
});

////////////////////////////// Form Window //////////////////////////

Wtf.AddEditFormWindow = function (config){
    Wtf.apply(this,config);
    Wtf.AddEditFormWindow.superclass.constructor.call(this);
}

Wtf.extend(Wtf.AddEditFormWindow,Wtf.Window,{
    border : false,
    title : WtfGlobal.getLocaleText("acc.inspection.temp.Edit.Temp"),
    layout : 'fit',
    closable: true,
    width:400,
    height:300,
    modal:true,
    resizable:false,
        
    FormPanel:undefined,
    WinTitle : WtfGlobal.getLocaleText("acc.inspection.temp.Edit.Temp"),
    WinDetail:WtfGlobal.getLocaleText("acc.inspection.temp.add.Edit.Temp"),
    WinImage :'images/defaultuser.png',
        
    initComponent:function (){
        Wtf.AddEditFormWindow.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        
        var mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.FormPanel
            ]
        });

        this.add(mainPanel);
    },
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(this.WinTitle,this.WinDetail,this.WinImage)
        });
    },
    GetAddEditForm:function (){
    //        this.FormPanel.region = 'center'
    }
    
});

////////////////////////////////////////////////////////////

/////////////////////Template Form/////////////////////////

Wtf.TemplateForm = function (config){
    Wtf.apply(this,config);
    Wtf.TemplateForm.superclass.constructor.call(this);
}

Wtf.extend(Wtf.TemplateForm, Wtf.form.FormPanel,{
    TemplateRec : undefined,
    region:"center",
    border:false,
    iconCls:'win',
    bodyStyle:"background-color:#f1f1f1;padding:15px",
    
    labelWidth:130,
    
    initComponent:function (){
        Wtf.TemplateForm.superclass.initComponent.call(this);
        var templateId = new Wtf.form.Hidden({
            name:'templateId',
            value: this.TemplateRec != undefined ? this.TemplateRec.get('templateId') : null
        }) 
        var templateName = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.inspection.temp.name"),
            name:'templateName',
            value: this.TemplateRec != undefined ? this.TemplateRec.get('templateName') : null,
            width:200
        }) 
        var templateDescription = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.inspection.temp.description"),
            name:'templateDescription',
            value: this.TemplateRec != undefined ? this.TemplateRec.get('templateDescription') : null,
            width:200
        }) 
        this.add(templateId);
        this.add(templateName);
        this.add(templateDescription);
    },
    saveTemplate:function (){
        var saveLoadMask = new Wtf.LoadMask(document.body,{
            msg:WtfGlobal.getLocaleText("acc.msgbox.49")
        });
        var form = this.getForm();
        if(form.isValid()){
            saveLoadMask.show();
            form.submit({
                url:"INVTemplate/addUpdateInspectionTemplate.do",
                success:function (response,request){
                    saveLoadMask.hide();
                    var success = request.result.data.success;
                    var msg = request.result.data.msg;
                    if(!msg){
                        msg = WtfGlobal.getLocaleText("acc.inspection.temp.saved.success")
                    }
                    Wtf.MessageBox.show({
                        title:WtfGlobal.getLocaleText("acc.cc.8"),
                        msg:msg,
                        icon:Wtf.MessageBox.INFO,
                        buttons:Wtf.MessageBox.OK
                    });
                    if(this.TemplateGridStore!=null){
                        this.TemplateGridStore.reload();
                    }
                    if(success){
                        this.destroy();
                    }
                },
                failure:function (response,request){
                    saveLoadMask.hide();
                    var msg = request.result.data.msg;
                    if(!msg){
                        msg = WtfGlobal.getLocaleText("acc.inspection.temp.saved.Error")
                    }
                    Wtf.MessageBox.show({
                        title:WtfGlobal.getLocaleText("acc.cc.8"),
                        msg:msg,
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                },
                scope : this
            });
        }
    }
});

Wtf.InspectionForm = function (config){
    Wtf.apply(this,config);
    Wtf.InspectionForm.superclass.constructor.call(this);
}

Wtf.extend(Wtf.InspectionForm, Wtf.form.FormPanel,{
    InspectionRec : undefined,
    region:"center",
    border:false,
    iconCls:'win',
    bodyStyle:"background-color:#f1f1f1;padding:15px",
    
    labelWidth:130,
    
    initComponent:function (){
        Wtf.InspectionForm.superclass.initComponent.call(this);
        var areaId = new Wtf.form.Hidden({
            name:'areaId',
            value: this.InspectionRec != undefined ? this.InspectionRec.get('areaId') : null
        }) 
        var areaName = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.inspection.temp.area.name"),
            name:'areaName',
            value: this.InspectionRec != undefined ? this.InspectionRec.get('areaName') : null,
            width:200
        }) 
        var faults = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.inspection.temp.fieldLabel.faults"),
            name:'faults',
            value: this.InspectionRec != undefined ? this.InspectionRec.get('faults') : null,
            width:200
        }) 
        this.add(areaId);
        this.add(areaName);
        this.add(faults);
        /**
         * If MRP QA flow is on then only show passing value field
         */
        if(Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP){
            var passingValue = new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.inspection.temp.fieldLabel.passingValue"),
                name:'passingValue',
                value: this.InspectionRec != undefined ? this.InspectionRec.get('passingValue') : null,
                width:200
            }) 
            this.add(passingValue);
        }
    },
    saveInspectionDetail:function (templateId){
        var saveLoadMask = new Wtf.LoadMask(document.body,{
            msg:WtfGlobal.getLocaleText("acc.msgbox.49")
        });
        var form = this.getForm();
        if(form.isValid()){
            saveLoadMask.show();
            form.submit({
                url:"INVTemplate/addUpdateInspectionArea.do",
                params:{
                    templateId: templateId
                },
                success:function (response,request){
                    saveLoadMask.hide();
                    var success = request.result.data.success;
                    var msg = request.result.data.msg;
                    if(!msg){
                        msg = WtfGlobal.getLocaleText("acc.inspection.temp.area.saved.success")
                    }
                    Wtf.MessageBox.show({
                        title:WtfGlobal.getLocaleText("acc.cc.8"),
                        msg:msg,
                        icon:Wtf.MessageBox.INFO,
                        buttons:Wtf.MessageBox.OK
                    });
                    if(this.InspectionGridStore!=null){
                        this.InspectionGridStore.reload();
                    }
                    if(success){
                        this.destroy();
                    }
                },
                failure:function (response,request){
                    saveLoadMask.hide();
                    var msg = request.result.data.msg;
                    if(!msg){
                        msg = WtfGlobal.getLocaleText("acc.inspection.temp.saved.Error")
                    }
                    Wtf.MessageBox.show({
                        title:WtfGlobal.getLocaleText("acc.cc.8"),
                        msg:msg,
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                },
                scope : this
            });
        }
    }
});
///////////////////////////////////////////////////////