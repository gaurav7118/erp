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
var _CustomDesign_moduleId;
var _countryid;
var _CustomDesign_templateId;
Wtf.DesignerDocTemplateList=function(config){
    Wtf.DesignerDocTemplateList.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.DesignerDocTemplateList,Wtf.Panel,{
    initComponent:function(config){
        Wtf.DesignerDocTemplateList.superclass.initComponent.call(this,config);

        this.sm= new Wtf.grid.CheckboxSelectionModel({
//            singleSelect:true
        });
        
        this.createTemplateBtn= new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.CreateTemplate"),//"Create Template",
            scope:this,
            disabled :true,
            tooltip:WtfGlobal.getLocaleText("acc.createTemplate"),//'Preview.'},
            handler:function() {
                this.createNewTemplateWin();
            }
        });

        this.createNewDocumentDesignerBtn= new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.CreateTemplate"),
            scope:this,
            hidden:true,
            tooltip:{text:WtfGlobal.getLocaleText("acc.field.CreateTemplate")},//'Preview.'},
            //            iconCls:"pwnd addEmailMarketing",
            handler:function() {
                this.createNewDocumentDesignerWin();
            }
        });

        this.copyTemplateBtn= new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.CopyTemplate"),//"Copy Template",
            scope:this,
            disabled :true,
            hidden:true,
            tooltip:{text:WtfGlobal.getLocaleText("acc.common.copy")},//'Copy'},
            id:'idCopyTemplateBtn'+this.id,
            handler:function() {
                if(this.templatelistgrid){
                    if(this.templatelistgrid.getSelectionModel()){
                        if(this.templatelistgrid.getSelectionModel().getSelected()){
                            var isCopy = true;
                            var rec = this.templatelistgrid.getSelectionModel().getSelected();
                            this.createNewDocumentDesignerWin(isCopy, rec,false);
                        }
                    }
                }
            }
        });
        
        this.ExportXLSTemplateMenu = [];
        this.ExportXLSTemplateSelected = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.customdesigner.export.selected"),
            tooltip: WtfGlobal.getLocaleText("acc.customdesigner.export.selected"),
            handler:this.ExportTemplateConfig.createDelegate(this),
            scope: this,
            iconCls: Wtf.isChrome?'pwnd exportChrome':'pwnd export'
        });
        this.ExportXLSTemplateAll = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.customdesigner.export.all"),
            tooltip: WtfGlobal.getLocaleText("acc.customdesigner.export.all"),
            handler: function() {
              callTemplateMultipleExport();  
            },
            scope: this,
            iconCls: Wtf.isChrome?'pwnd exportChrome':'pwnd export'
        });
        this.ExportXLSTemplateMenu.push(this.ExportXLSTemplateSelected);
        this.ExportXLSTemplateMenu.push(this.ExportXLSTemplateAll);
//        this.ExportXLSTemplateMenu.push(this.ExportXLSTemplateAll);
        this.ExportMenu = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.customdesigner.export.template"),
            tooltip: WtfGlobal.getLocaleText("acc.customdesigner.export.template"),
//            hidden:true, // hidden for time being until all the issues get finished.
            scope: this,
            iconCls: Wtf.isChrome?'pwnd exportChrome':'pwnd export',
            menu: this.ExportXLSTemplateMenu
        });
        this.ImportXLSTemplateMenu = [];
        this.extraConfig = {};
        this.extraConfig["url"] = "DocumentDesignController/importTemplates.do?";
        this.ImportXLSTemplate = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
//            hidden:true,  // hidden for time being until all the issues get finished.
            handler:function() {
                callTemplateImportWin('', '',WtfGlobal.getLocaleText("acc.customdesigner.custom.template"), "DocumentDesignController/importTemplates.do?", false,this.templatelistgrid);
            },
            scope: this,
            iconCls:  Wtf.isChrome?'pwnd importChrome':'pwnd import'
        });
        this.renameTemplateBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.RenameTemplate"), //"Rename Template",
            scope: this,
            disabled: true,
            hidden: true,
            tooltip: {text: WtfGlobal.getLocaleText("acc.common.Rename")}, //'Remane'},
            id:'idRenameTemplateBtn'+this.id,
            handler: function() {
                if (this.templatelistgrid) {
                    if (this.templatelistgrid.getSelectionModel()) {
                        if (this.templatelistgrid.getSelectionModel().getSelected()) {
                            var isRename = true;
                            var rec = this.templatelistgrid.getSelectionModel().getSelected();
                            if (!rec.data.isdefaulttemplate) {
                                this.createNewDocumentDesignerWin(false, rec, isRename);
                            }
                        }
                    }
                }
            }
        });
        
        this.deleteTemplateBtn= new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.DeleteTemplate"),//"Delete Template",
            scope:this,
            disabled :true,
            tooltip:WtfGlobal.getLocaleText("acc.common.delete"),
            id:'idDeleteTemplateBtn'+this.id,
            handler:function() {
                this.deleteTemplatesWin();
            }
        });
        
        var Rec=new Wtf.data.Record.create([
            {name:'templateid'},
            {name:'templatename'},
            {name:'isdefault'},
            {name:'createdby'},
            {name:'isnewdesign'},
            {name:'isdefaulttemplate'},
            {name:'createdon',type:'date'},	//SDP-4325
            {name:'updatedon',type:'date'},	//SDP-12433
            {name:'templatesubtype'}
        ]);

        this.templatelistReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"totalCount"
        },Rec);


        this.templatelistStore=new Wtf.data.Store({
            url: "CustomDesign/getDesignTemplateList.do",
            baseParams:{
                moduleid : this.moduleid
            },
            reader:this.templatelistReader,
            remoteSort:true,
             sortInfo: {
                field: 'templatename',
                direction: 'ASC'
            }
        });
        
        
        this.templatelistStore.on("beforeload", function(){
            this.templatelistStore.baseParams = {
                ss:this.quickPanelSearch.getValue(),
                isActive : this.activeFilter.getValue(),
                moduleid : this.moduleidForStoreLoad
            }
        }, this);

        this.activeFilterStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data :[['all','All'],[1,'Active'],[0,'Inactive']]
        });
        this.activeFilter = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.customdesigner.active.filter"),
            id: 'activefilterid',
            store: this.activeFilterStore,  
            scope: this,
            readOnly :true,
            valueField: 'id',
            displayField: 'name',
            forceSelection: true,
            width:100,
            allowBlank: false,
            emptyText:WtfGlobal.getLocaleText("acc.customdesigner.active.filter"),
            mode: 'local',
            triggerAction: 'all',
            value:1
        });
        
        this.activeFilterBtn = new Wtf.Toolbar.Button({
            text :  WtfGlobal.getLocaleText("acc.agedPay.fetch"),
            iconCls:'accountingbase fetch',
            scope: this,
            handler: this.getActiveTemplate
        });
        
        this.cm1=new Wtf.grid.ColumnModel(
            [ this.sm,
              new Wtf.grid.RowNumberer(),
              {
                header:WtfGlobal.getLocaleText("acc.designerTemplateName"),//'Template Name',
                dataIndex:'templatename',
                sortable: true,
                renderer : function(val, css, record, row, column, store) {
                    var tmpVal=''
                    
                    if(record.data.isnewdesign==1)
                        tmpVal="<a href = '#' class='tempdetailsNew' wtf:qtip="+WtfGlobal.getLocaleText("acc.field.Clicktoeditemailtemplate")+"> "+val+"</a>";
                    else
                        tmpVal="<a href = '#' class='tempdetails' wtf:qtip="+WtfGlobal.getLocaleText("acc.field.Clicktoeditemailtemplate")+"> "+val+"</a>";
                   
                    return tmpVal;

                }
            },{
                header:WtfGlobal.getLocaleText("acc.customdesigner.template.subtype"),
                dataIndex:'templatesubtype',
                sortable: true,
                renderer:function(val,css, record, row, column, store){
                    var moduleid = store.baseParams.moduleid; 
                    var returnvalue="";
                    if(moduleid==Wtf.Acc_Credit_Note_ModuleId){
                        if(val==0){
                            returnvalue = "Default"; 
                        }else if(val==1){
                            returnvalue = "Sales Return";
                        }else if(val == Wtf.Subtype_Undercharge){
                            returnvalue = "Undercharged Purchase Invoice";
                        }else if(val == Wtf.Subtype_Overcharge){
                            returnvalue = "Overcharged Sales Invoice";
                        }
                    }else if(moduleid==Wtf.Acc_Debit_Note_ModuleId){
                        if(val==0){
                            returnvalue = "Default"; 
                        }else if(val==1){
                            returnvalue = "Purchase Return";
                        }else if(val == Wtf.Subtype_Undercharge){
                            returnvalue = "Undercharged Sales Invoice";
                        }else if(val == Wtf.Subtype_Overcharge){
                            returnvalue = "Overcharged Purchase Invoice";
                        }
                    }else if(moduleid==Wtf.Acc_Customer_Quotation_ModuleId
                        ||moduleid==Wtf.Acc_Receive_Payment_ModuleId){ 
                        if(val==0){
                            returnvalue = "Sales"; 
                        }else if(val==1){
                            returnvalue = "Consignment";
                        }else if(val==2){
                            returnvalue = "Lease"
                        }
                    }else if(moduleid==Wtf.Acc_Delivery_Order_ModuleId || moduleid==Wtf.Acc_Sales_Return_ModuleId){ 
                        if(val==0){
                            returnvalue = "Sales"; 
                        }else if(val==1){
                            returnvalue = "Consignment";
                        }else if(val==2){
                            returnvalue = "Lease"
                        }else if(val==6){
                            returnvalue = "Asset"
                        }
                    }else if(moduleid==Wtf.Acc_Stock_Request_ModuleId ||moduleid==Wtf.Inventory_Stock_Repair_ModuleId
                        ||moduleid==Wtf.Acc_InterLocation_ModuleId||moduleid==Wtf.Inventory_ModuleId){
                        if(val==0){
                            returnvalue = "Inventory"; 
                        }
                    }else if(moduleid==Wtf.Acc_Stock_Adjustment_ModuleId||moduleid==Wtf.Acc_InterStore_ModuleId){
                        if(val==0){
                            returnvalue = "Inventory"; 
                        } else if(val==9){
                            returnvalue = "Job Work"; 
                        }
                    }
                    else if (moduleid==Wtf.Acc_Customer_AccountStatement_ModuleId || moduleid==Wtf.Acc_Vendor_AccountStatement_ModuleId  ){
                        if(val==0){
                            returnvalue = "Normal"; 
                        }else if(val==1){
                            returnvalue = "Invoice";
                        }else if(val==2){
                            returnvalue = "Transaction Currency";
                        }
                    }else if(moduleid==Wtf.Acc_Sales_Order_ModuleId){
                        if(val==0){
                            returnvalue = "Sales"; 
                        }else if(val==1){
                            returnvalue = "Consignment";
                        }else if(val==2){
                            returnvalue = "Lease"
                        }else if(val==3){
                            returnvalue = "Job Order"
                        }else if(val==4){
                            returnvalue = "Job Order Label"
                        }else if(val == 5){
                            returnvalue = "Opening Invoice"
                        }else if(val == 9){
                            returnvalue = "Job Work"
                        }
                    } else if(moduleid==Wtf.Acc_Invoice_ModuleId){
                        if(val==0){
                            returnvalue = "Sales"; 
                        }else if(val==1){
                            returnvalue = "Consignment";
                        }else if(val==2){
                            returnvalue = "Lease"
                        }else if(val==3){
                            returnvalue = "Job Order"
                        }else if(val==4){
                            returnvalue = "Job Order Label"
                        }else if(val == 5){
                            returnvalue = "Opening Invoice"
                        }else if(val == 6){
                            returnvalue = "Asset"
                        }else if(val == 9){
                            returnvalue = "Job Work"
                        }
                    }else if(moduleid==Wtf.QA_Approval_ID){
                        if(val==0){
                            returnvalue = "Delivery Order"; 
                        }
                    }else if(moduleid === Wtf.autoNum.buildAssemblyReport || moduleid === Wtf.Bank_Reconciliation_ModuleId || moduleid === Wtf.MRP_Work_Order_ModuleID) {//ERM-26 Template Subtype renderer in template list grid
                        if(val == 0){
                            returnvalue = "Default";
                        }
                    }else{
                        if(val==0){
                            returnvalue = "Purchase"; 
                        }else if(val==1){
                            returnvalue = "Consignment";
                        }else if(val==6){
                            returnvalue = "Asset";
                        }else if(val==9){
                            returnvalue = "Job Work";
                        } 
                    }
                    return returnvalue;
                }
            },{
                header:WtfGlobal.getLocaleText("acc.nee.69"),//'Created By',
                sortable: true,
                dataIndex:'createdby'
            },{
                header:WtfGlobal.getLocaleText("acc.nee.51"),//'Created On',
                dataIndex:'createdon',
                xtype:'datefield',
                sortable: true,
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.common.reportupdatedon"),//'Updated On',
                dataIndex:'updatedon',
                xtype:'datefield',
                sortable: true,
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.designerTemplate.isActive"),//'Created On',
                dataIndex:'isdefault',
                renderer : function(val) {
                    var ret = "<span>"+ WtfGlobal.getLocaleText("acc.msgbox.no")+" </span>" ;
                    if(val == 1){
                        ret = "<span>"+WtfGlobal.getLocaleText("acc.msgbox.yes")+"</span>";
                    }
                    return ret +"<a href = '#' class='changeactivemode'>"+WtfGlobal.getLocaleText("acc.field.Change")+"</a>";
                }
            },{
                header:WtfGlobal.getLocaleText("acc.customdesigner.default.template"),
                align:'center',
                dataIndex:'isdefaulttemplate',
                renderer : function(val){
                    if(val == true) {
                        return "Default Template";
                    } else {
                        return "Custom Template";
                    }
                }
            }
//            ,{
//                header:WtfGlobal.getLocaleText("acc.designerPreview"),//'Preview with sample data',
//                align:'center',
//                dataIndex:'templateid',
//                renderer : function(val){
//                    return "<a href = '#' class='urlcampaignreport'>"+WtfGlobal.getLocaleText("acc.common.view")+" </a>";
//                }
//            }
            ,{
                header : WtfGlobal.getLocaleText("acc.common.delete"),
                dataIndex: '',
                sortable: true,
                renderer:function(value, css, record, row, column, store){
                    if(record.data.isdefaulttemplate==false) {
                        return "<div class='delete pwnd delete-gridrow'  title="+WtfGlobal.getLocaleText("acc.field.DeleteTemplate")+"></div>";
                    } else {
                        return '';
                    }
                }
            }
            ]);

        this.getDocumentModuleGrid();
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.customdesigner.template.searchBy.name"), // "Search by Product ID, Batch ...",
            width: 150,
            id:"quickSearch",
            field: 'id'
        });
 
        this.pg = new Wtf.PagingSearchToolbar({
            pageSize: 15,
            store: this.templatelistStore,
            id: "pagingtoolbar" + this.id,
            displayInfo:true,
            searchField: this.quickPanelSearch,
            emptyMsg:WtfGlobal.getLocaleText("acc.common.nores"),// "No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
        });
        this.toolbarItems = [];
        //        this.toolbarItems.push(WtfGlobal.getLocaleText("acc.dnList.searchText"));
        //        this.toolbarItems.push('-');
        //        this.toolbarItems.push(this.quickSearchTF);

        this.templatelistgrid = new Wtf.grid.GridPanel({
            store: this.templatelistStore,
            sm:this.sm,
            autoScroll :true,
            border:false,
            id:'templatelistgrid'+this.id,
            scope:this,
            viewConfig: {
                forceFit: true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.designerTemplate.templategrid.mtytext"))//"Please select a module to view its configured templates.
            },
            loadMask:true,
            clicksToEdit :1,
            displayInfo:true,
            bbar:this.pg,
            tbar:[WtfGlobal.getLocaleText("acc.common.search"),' ',this.quickPanelSearch,'-',this.createTemplateBtn,this.createNewDocumentDesignerBtn,this.copyTemplateBtn,this.renameTemplateBtn,this.deleteTemplateBtn, this.ExportMenu, this.ImportXLSTemplate ,'-', WtfGlobal.getLocaleText("acc.cc.8"), this.activeFilter,this.activeFilterBtn ],
            cm: this.cm1
        });

        //  this.getDetailPanel();
        this.templatelistStore.on('datachanged', function() {
            var p = this.pP.pagingToolbar.pageSize;
                this.quickPanelSearch.setPage(p);
        }, this);
//        this.templatelistgrid.on('rowclick', function(grid, recIndex, event) {
//            this.copyTemplateBtn.enable();
//            this.renameTemplateBtn.enable();
//            this.deleteTemplateBtn.enable();
//        }, this);
        
        this.sm.on('selectionchange', function(selModel){
            var selectedEl = selModel.selections.length;
            if(selectedEl===0){
                Wtf.getCmp('idDeleteTemplateBtn'+this.grid.scope.id).disable();
                Wtf.getCmp('idCopyTemplateBtn'+this.grid.scope.id).disable();
                Wtf.getCmp('idRenameTemplateBtn'+this.grid.scope.id).disable();
            } else if(selectedEl === 1){
                Wtf.getCmp('idDeleteTemplateBtn'+this.grid.scope.id).enable();
                Wtf.getCmp('idCopyTemplateBtn'+this.grid.scope.id).enable();
                Wtf.getCmp('idRenameTemplateBtn'+this.grid.scope.id).enable();
            } else{
                Wtf.getCmp('idDeleteTemplateBtn'+this.grid.scope.id).enable();
                Wtf.getCmp('idCopyTemplateBtn'+this.grid.scope.id).disable();
                Wtf.getCmp('idRenameTemplateBtn'+this.grid.scope.id).disable();
            }
        });

        this.templatelist= new Wtf.Panel({
            layout:'fit',
            region:'center',
            border:true,
            id:this.id+'templatelist',

            items:[
            {
                layout:'fit',
                border:false,
                items:[this.templatelistgrid]
            }
            ]
        });
        this.emailcampaign= new Wtf.Panel({
            layout:'fit',
            region:'west',
            border:true,
            width:250, //ERP-20593
            items:[
            {
                layout:'fit',
                border:false,
                items:[this.ShortUserGrid]
            }
            ]
        });
        this.MembergridPanel = new Wtf.Panel({//Wtf.common.KWLListPanel
            title: '<span  style="">'+WtfGlobal.getLocaleText("acc.designerTemplate.moduleGridListName")+'</span>',
            autoLoad: false,
            autoScroll:true,
            paging: false,
            //            width:500,
            tbar:this.toolbarItems,
            layout: 'border',
            items: [this.emailcampaign,this.templatelist]
        });
        this.add(this.MembergridPanel);

        this.selectionModel.on('selectionchange', function(selModel) {
            var sm =this.selectionModel;
            if(sm.getSelections().length==1){
                var moduleId =  sm.selections.items[0].data.moduleid;
                if( moduleId == Wtf.Acc_Invoice_ModuleId || moduleId == Wtf.Acc_Customer_Quotation_ModuleId 
                    || moduleId == Wtf.Acc_Vendor_Quotation_ModuleId||moduleId==Wtf.Acc_Credit_Note_ModuleId
                    || moduleId == Wtf.Acc_Delivery_Order_ModuleId || moduleId == Wtf.Acc_Purchase_Order_ModuleId 
                    || moduleId == Wtf.Acc_Sales_Order_ModuleId || moduleId == Wtf.Acc_Vendor_Invoice_ModuleId || 
                    moduleId == Wtf.Acc_Sales_Return_ModuleId || moduleId == Wtf.Acc_Goods_Receipt_ModuleId
                    ||moduleId == Wtf.Acc_Debit_Note_ModuleId|| moduleId == Wtf.Acc_Make_Payment_ModuleId|| moduleId == Wtf.Acc_Receive_Payment_ModuleId
                    ||moduleId == Wtf.Acc_Purchase_Return_ModuleId||moduleId==Wtf.Acc_Stock_Adjustment_ModuleId||moduleId==Wtf.Acc_Stock_Request_ModuleId
                    ||moduleId==Wtf.Acc_InterStore_ModuleId||moduleId==Wtf.Acc_InterLocation_ModuleId||moduleId==Wtf.Inventory_ModuleId ||moduleId==Wtf.Acc_RFQ_ModuleId
                    ||moduleId==Wtf.Acc_Customer_AccountStatement_ModuleId || moduleId==Wtf.Acc_Vendor_AccountStatement_ModuleId  
                    ||moduleId==Wtf.Acc_Purchase_Requisition_ModuleId || moduleId==Wtf.Inventory_Stock_Repair_ModuleId || moduleId==Wtf.QA_Approval_ID
                    ||moduleId == Wtf.Build_Assembly_Report_ModuleId || moduleId == Wtf.Bank_Reconciliation_ModuleId || moduleId == Wtf.MRP_Work_Order_ModuleID){

                    this.createNewDocumentDesignerBtn.setVisible(true);
                    this.copyTemplateBtn.setVisible(true);
                    this.renameTemplateBtn.setVisible(true);
                    this.createTemplateBtn.setVisible(false);
                }else{
                    this.createNewDocumentDesignerBtn.setVisible(false);
                    this.copyTemplateBtn.setVisible(false);
                    this.renameTemplateBtn.setVisible(false);
                    this.createTemplateBtn.setVisible(true);
                }
                this.createTemplateBtn.enable();
                var rec = sm.getSelected();
                this.moduleidForStoreLoad = rec.get('moduleid');
                this.templatelistStore.load({
                             params : {start : 0,limit : this.pP.pagingToolbar.pageSize}
                });
                this.templatelistgrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.designerTemplate.templatelist.mtytxt"));//"No template designed till now to the selected module.");
            }else{
                this.createTemplateBtn.disable();
                this.templatelistgrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.designerTemplate.templatelist.emtry.mtytxt"));//"Please select a module to view its templates...
                this.templatelistgrid.getStore().removeAll();
            }
        }, this);
        this.templatelistStore.on('load', function(store) {
            if(this.templatelistStore.getCount() < 1) {
                this.templatelistgrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.templatelistgrid.getView().refresh();
            }
            this.quickPanelSearch.StorageChanged(store);
            this.templatelistgrid.getView().refresh();
        }, this);
        this.templatelistgrid.on("cellclick", this.afterGridCellClick, this);
   
        this.templatelistgrid.on('render',function(){this.templatelistgrid.getStore().removeAll();},this);
    },
    askConfirmToRunCamp: function(){
        if(this.templatelistgrid.getSelectionModel().getCount()!=1){
            WtfComMsgBox([WtfGlobal.getLocaleText("crm.campaigndetails.msg.title"),WtfGlobal.getLocaleText("crm.emailmarketing.selcampconftoruncamp.msg")],0);//"Please select a Campaign Configuration to Run Your Campaign"],0);
            return;
        }
        Wtf.MessageBox.show({
            title:WtfGlobal.getLocaleText("crm.msgbox.CONFIRMTITLE"),//'Confirm',
            msg:WtfGlobal.getLocaleText("crm.emailmarketing.confirmmsgtoruncamp.msg"),//"Are you sure you want to run your selected campaign?",
            icon:Wtf.MessageBox.QUESTION,
            buttons:Wtf.MessageBox.YESNO,
            scope:this,
            fn:function(button){
                if(button=='yes')
                {
                    this.runCamp();
                }else{
                    return;
                }
            }
        });
    },
    
    ExportTemplateConfig: function() {
        var align = "none,none,none,none,none,none,none,none,none,none,none,none,none,none,none";
        var moduleid = this.selectionModel.getSelected().data.moduleid;
        if ( this.templatelistgrid.getSelections().length === 0 ) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.customdesigner.export.warning")],0);
            return;
        }
        var templateid = this.templatelistgrid.getSelections()[0].data.templateid;
        var templatename = this.templatelistgrid.getSelections()[0].data.templatename;
        var filename = "Template_" + templatename;
        var exportUrl = "DocumentDesignController/exportTemplates.do";
        var type = "xlsx";
        var get = 1;
        var header = "templatename,moduleid,json,pagelayoutproperty,footerjson,headerjson,html,headerhtml,footerhtml,sqlquery,headersqlquery,footersqlquery,customColJson,image,templatesubtype";
        var nondeleted = "true";
        var title = "Template Name,Module ID,JSON,Page Layout Property,Footer JSON, Header JSON,HTML,Header HTML,Footer HTML,SQL Query,Header SQL Query,Footer SQL Query,Custom Filed Json,Image,Sub Type";
        var width = "75,75,75,75,75,75,75,75,75,75,75,75,75,75,75";
        var url = exportUrl + "?filename=" + encodeURIComponent(filename) + "+&filetype=" + type + "&nondeleted=" + nondeleted + "&header=" + header + "&title=" + encodeURIComponent(title) + "&width=" + width + "&get=" + get + "&align=" + align +"&moduleid="+moduleid+"&templateid="+templateid;
        Wtf.get('downloadframe').dom.src = url;
    },
    getDocumentModuleGrid : function(){
        this.moduleRec = new Wtf.data.Record.create([
        {
            name: 'moduleid'
        },{
            name: 'modulename'
        }]);
    
        /* Refer moduleids from Constants.java from KwlCommonLibs
     * Acc_Invoice_ModuleId = 2
     * Acc_BillingInvoice_ModuleId = 3
     */
        this.moduleDS = new Wtf.data.SimpleStore({
            fields:['moduleid','modulename'],

            data: [
            [Wtf.Acc_Invoice_ModuleId,WtfGlobal.getLocaleText("acc.module.name.02")],
            [Wtf.Acc_Purchase_Order_ModuleId,WtfGlobal.getLocaleText("acc.module.name.18")],
            [Wtf.Acc_Sales_Order_ModuleId,WtfGlobal.getLocaleText("acc.module.name.20")],
            [Wtf.Acc_Credit_Note_ModuleId,WtfGlobal.getLocaleText("acc.module.name.12")],
            [Wtf.Acc_Debit_Note_ModuleId,WtfGlobal.getLocaleText("acc.module.name.10")],
            [Wtf.Acc_Delivery_Order_ModuleId,WtfGlobal.getLocaleText("acc.module.name.27")],
            [Wtf.Acc_Goods_Receipt_ModuleId,WtfGlobal.getLocaleText("acc.module.name.28")],
            [Wtf.Acc_Make_Payment_ModuleId,WtfGlobal.getLocaleText("acc.module.name.14")],
            [Wtf.Acc_Receive_Payment_ModuleId,WtfGlobal.getLocaleText("acc.module.name.16")],
            [Wtf.Acc_Vendor_Quotation_ModuleId,WtfGlobal.getLocaleText("acc.module.name.23")],
            [Wtf.Acc_Customer_Quotation_ModuleId,WtfGlobal.getLocaleText("acc.module.name.22")],
            [Wtf.Acc_Vendor_Invoice_ModuleId,WtfGlobal.getLocaleText("acc.module.name.6")],
            [Wtf.Acc_Sales_Return_ModuleId,WtfGlobal.getLocaleText("acc.module.name.29")],
            [Wtf.Acc_Purchase_Return_ModuleId,WtfGlobal.getLocaleText("acc.module.name.31")],
            [Wtf.Acc_RFQ_ModuleId,WtfGlobal.getLocaleText("acc.module.name.33")],
            [Wtf.Acc_Stock_Adjustment_ModuleId,WtfGlobal.getLocaleText("acc.module.name.1000")],
            [Wtf.Acc_Stock_Request_ModuleId,WtfGlobal.getLocaleText("acc.module.name.1001")],
            [Wtf.Inventory_ModuleId,WtfGlobal.getLocaleText("acc.module.name.92")],
            [Wtf.Acc_InterStore_ModuleId,WtfGlobal.getLocaleText("acc.module.name.1002")],
            [Wtf.Acc_InterLocation_ModuleId,WtfGlobal.getLocaleText("acc.module.name.1003")],
            [Wtf.Acc_Customer_AccountStatement_ModuleId,WtfGlobal.getLocaleText("acc.module.name.60")],
            [Wtf.Acc_Vendor_AccountStatement_ModuleId,WtfGlobal.getLocaleText("acc.module.name.61")],
            [Wtf.Acc_Purchase_Requisition_ModuleId,WtfGlobal.getLocaleText("acc.module.name.32")], //ERP-19851
            [Wtf.Inventory_Stock_Repair_ModuleId,WtfGlobal.getLocaleText("acc.up.56")],
            [Wtf.QA_Approval_ID,WtfGlobal.getLocaleText("acc.up.47")],
            [Wtf.Build_Assembly_Report_ModuleId,WtfGlobal.getLocaleText("acc.moduleList.buildUnbuildAssembly")],    //ERM-26 Build Assembly
            [Wtf.Bank_Reconciliation_ModuleId,WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation")], // Bank Reconciliation module
            [Wtf.MRP_Work_Order_ModuleID, WtfGlobal.getLocaleText("mrp.workorder.entry.title")] // MRP Work Order module
            ],

            autoLoad: true
        });
        //        this.moduleDS = new Wtf.data.Store({
        //            reader: new Wtf.data.KwlJsonReader({
        //                root: "data",
        //                totalProperty:"totalCount"
        //            },this.moduleRec),
        //            url: Wtf.req.springBase+"Campaign/action/getCampaigns.do",
        //            baseParams:{
        //                config:true,
        //                emailcampaign:true
        //            }
        //        });
        //        this.moduleDS.load({
        //            params : {
        //                start:0,
        //                limit:15
        //            }
        //        });
        this.selectionModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true,
            width: 30,
            id:"emailcampaignlist_selectionmodel"
        });

        this.gridcm= new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            this.selectionModel,
            {
                header: WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName"),//"Module Name",
                tip:WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName"),//'Module Name',
                dataIndex: 'modulename',
                autoWidth : true,
                sortable: true,
                renderer:function(v) {
                    var abc = createTextWithToolTip(v);
                    return abc;
                }
            }]);
        this.quickSearchTF = new Wtf.KWLTagSearch({
            id: 'administration_goal'+this.id,
            width: 150,
            emptyText: WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName")//'Enter Module Name '
        });
        this.ShortUserGrid=new Wtf.grid.GridPanel({
            layout:'fit',
            store: this.moduleDS,
            cm: this.gridcm,
            sm : this.selectionModel,
            border : false,
            loadMask : true,
            view:new Wtf.ux.KWLGridView({
                forceFit:true
            }),
            bbar:new Wtf.PagingSearchToolbar({
                pageSize: 15,
                searchField:this.quickSearchTF,
                id: "emailcampaignlist_toolbar",
                store: this.moduleDS,
                displayInfo: false
            })
        });
        
        /*Hiding refresh button for Short User grid  as Store for this grid is static .
          Short User grid remains loading on clicking refersh button.
        */
        this.ShortUserGrid.on("render", function(config) {     
            config.getBottomToolbar().loading.hideParent = true;
            config.getBottomToolbar().loading.hide(); 
        });
    //        this.moduleDS.on('load',function(){
    //            this.quickSearchTF.StorageChanged(this.moduleDS);
    //            this.quickSearchTF.on('SearchComplete', function() {
    //            }, this);
    //        },this);
    //
    //        this.moduleDS.on("datachanged",function(){
    //            this.quickSearchTF.setPage(this.pP.combo.value);
    //        },this);

    },

    onRender: function(config) {
        Wtf.DesignerDocTemplateList.superclass.onRender.call(this, config);
    },

    afterGridCellClick:function(Grid,rowIndex,columnIndex, e ) {
        var recData =this.selectionModel.getSelections()[0].data; 
        var campID = recData.moduleid;
        var templateRec= Grid.store.getAt(rowIndex).data;
        var templateId = templateRec.templateid;
        var templateName=templateRec.templatename;
        var isdefaulttemplate = templateRec.isdefaulttemplate;
        var templatesubtype=templateRec.templatesubtype;
        var event = e ;
        if(event.getTarget("a[class='urlcampaignreport']")) {
            //            Wtf.Ajax.timeout=1200000;
            //            Wtf.commonWaitMsgBox(WtfGlobal.getLocaleText("acc.designerTemplate.loadsampletemplate"));
            var url = "CustomDesign/showSamplePreview.do?filetype=print&templateid="+templateId+"&moduleid="+campID;//filetype=print&
            //            Wtf.get('downloadframe').dom.src  = url;
            window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
        //            Wtf.updateProgress();
        //            Wtf.Ajax.timeout=30000;
        //            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        //                        Wtf.get('downloadframe').dom.src  = "CustomDesign/showSamplePreview.do?moduleid=1&json="+returnConfig[0]+"&html="+returnConfig[1];

        //            Wtf.Ajax.requestEx({
        //                url: Wtf.req.springBase+'emailMarketing/action/sendEmailMarketMail.do',
        //                params:{
        //                    emailmarkid : Grid.store.getAt(rowIndex).data.id,
        //                    campid : campID,
        //                    flag : 11
        //                }},this,function(obj ,req){
        //                    Wtf.updateProgress();
        //                    Wtf.Ajax.timeout=30000;
        //                    Wtf.MessageBox.show({
        //                        title:WtfGlobal.getLocaleText("crm.emailmarketing.runcamploadmask.msg"),//"Sending E-mails for your Campaign...",
        //                        msg:obj.msgs,
        //                        icon:Wtf.MessageBox.INFO,
        //                        buttons:Wtf.MessageBox.OK
        //                    });
        //                 },function() {
        //                    Wtf.updateProgress();
        //                    Wtf.Ajax.timeout=30000;
        //            });
        }
        if(event.getTarget("a[class='tempdetails']")) {
            //                var recdata = Grid.getSelectionModel().getSelected().data;
            _CustomDesign_moduleId = campID;
            _CustomDesign_templateId = templateId;
            _CustomDesign_templateSubtype = templatesubtype;
            var url ='designTemplate.jsp?_m='+_CustomDesign_moduleId+'&_t='+_CustomDesign_templateId+'&_s='+_CustomDesign_templateSubtype;
            var newwindow=window.open(url,'_blank');
                if (newwindow.focus) {newwindow.focus()}
                
        //                var tab = Wtf.getCmp('tabdesignerpanel');
        //                if(tab==null) {
        //                    mainPanel.loadTab("../../designer1.html", "   designerpanel", "Custom Designer", "navareadashboard", Wtf.etype.adminpanel,false);
        //                } else {
        //                    mainPanel.remove(tab);
        //                    
        //                }
        //                
        //                
        //            var recdata = Grid.getSelectionModel().getSelected().data;
        //            var panel = Wtf.getCmp('template_designer_'+recdata.templateid);
        //            if(panel==null) {
        //                panel = new Wtf.DesignerComponent({});
        //                mainPanel.add(panel);
        //            }
        //            mainPanel.setActiveTab(panel);
        //            mainPanel.doLayout();
            
        //            var tipTitle=recdata.templatename+" : Edit Template";
        //            var title = Wtf.util.Format.ellipsis(tipTitle,18);
        //            if(panel==null) {
        //                panel=new Wtf.newEmailTemplate({
        //                    templateid : recdata.templateid,
        //                    tname : recdata.templatename,
        //                    tdesc : recdata.templatedescription,
        //                    templateClass :recdata.templateclass,
        //                    tsubject : recdata.templatesubject,
        //                    tbody : recdata.bodyhtml,
        //                    store: this.EditorStore,
        //                    title:"<div wtf:qtip=\""+tipTitle+"\"wtf:qtitle='Email Template'>"+title+"</div>",
        //                    tipTitle:tipTitle,
        //                    dashboardCall:true
        //                });
        //                Wtf.getCmp("crmemailcampaigntabpanel").add(panel);
        //            }
        //            Wtf.getCmp("crmemailcampaigntabpanel").setActiveTab(panel);
        //            Wtf.getCmp("crmemailcampaigntabpanel").doLayout();

        }
        if(event.getTarget("a[class='tempdetailsNew']")) {
            //                var recdata = Grid.getSelectionModel().getSelected().data;
            _CustomDesign_moduleId = campID;
            _CustomDesign_templateId = templateId;
            _CustomDesign_templateSubtype = templatesubtype;
            _countryid = Wtf.Countryid;
            var url ='documentDesignerNew.jsp?_m='+_CustomDesign_moduleId+'&_t='+_CustomDesign_templateId+'&_tname='+templateName+'&_isdft='+isdefaulttemplate+'&_s='+_CustomDesign_templateSubtype+'&_amountdecimal='+ Wtf.AMOUNT_DIGIT_AFTER_DECIMAL+'&_unitpricedecimal='+Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL+'&_quantitydecimal='+Wtf.QUANTITY_DIGIT_AFTER_DECIMAL+'&_companyid='+companyid+'&_countryid='+_countryid;
            var newwindow=window.open(url,'_blank');
   
        }
        
        /*
        *    Deleting a template option-Neeraj   
        */

        if(event.getTarget("div[class='delete pwnd delete-gridrow']")) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedcustomlayouttemplate"),function(btn){
                if(btn=="yes") {
                    Wtf.Ajax.requestEx({
                        url:"CustomDesign/deleteCustomTemplatemodule.do",
                        params: {
                            templateid : templateId,
                            moduleid: campID
                        }
                    },this,this.genSuccessResponse,this.genFailureResponse);

                }
            }, this);

        } 
        if(event.getTarget("a[class='changeactivemode']")) {
            var isactivemode="";
            if(templateRec.isdefault==1){
                isactivemode=0;
            }else{
                isactivemode=1;
            }
            Wtf.Ajax.requestEx({
                url:"CustomDesign/saveActiveModeTemplate.do",
                params: {
                    templateid : templateId,
                    isactive : isactivemode,
                    moduleid: campID
                }
            },this,this.genActiveModeSuccessResp,this.genActiveModeFailureResp);
        }   
        
    },
    genSuccessResponse: function(res){
        if(res.success == true) {
            WtfComMsgBox([0,WtfGlobal.getLocaleText("acc.field.Templatedeletedsuccessfully")], 0);
            this.templatelistStore.load({
                params : {
                    start : 0,
                    limit : this.pP.pagingToolbar.pageSize
                }
            });
        }
    },
    genFailureResponse:function(){
        WtfComMsgBox([0,WtfGlobal.getLocaleText("acc.field.Erroroccuredatserverside")], 1);
    },
    genActiveModeSuccessResp: function(res){
        if(res.success == true) {
            WtfComMsgBox([0,WtfGlobal.getLocaleText("acc.field.ActiveModechangedsuccessfully.")], 0);
            this.templatelistStore.load({
                params : {
                    start : 0,
                    limit : this.pP.pagingToolbar.pageSize
                }
            });
        }
    },
    genActiveModeFailureResp:function(){
        WtfComMsgBox([0,WtfGlobal.getLocaleText("acc.field.Erroroccuredatserverside")], 1);
    },
    
    createNewTemplateWin : function() {           //Create Button in the template name on the right hand side
        var moduleid = this.selectionModel.getSelections()[0].data.moduleid;        
        this.DN_SubtypeStore = new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            ['1', 'Against Invoice'],
            ['2','Otherwise case']
            ]
        });
        this.CN_SubtypeStore = new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Default, 'Default'],
            [Wtf.Subtype_SalesReturn,'Sales Return']
            ]
        });
        this.salesmodulestore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Sales, 'Sales'],
            [Wtf.Subtype_Consignment,'Consignment'],
            [Wtf.Subtype_Lease,'Lease']
            ]
        });
        this.deliveryOrderAndSalesReturnModulestore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Sales, 'Sales'],
            [Wtf.Subtype_Consignment,'Consignment'],
            [Wtf.Subtype_Lease,'Lease'],
            [Wtf.Subtype_Asset,'Asset']
            ]
        });
        
        this.salesOrderModuleStore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Sales, "Sales"],
            [Wtf.Subtype_Consignment,"Consignment"],
            [Wtf.Subtype_Lease,'Lease'],
            [Wtf.Subtype_Job_Order,'Job Order'],
            [Wtf.Subtype_Job_Order_Label,'Job Order Label'],
            [Wtf.Subtype_Opening_Invoice,'Opening Invoice'],
            [Wtf.Subtype_JobWork,"Job Work"]
            ]
        });
        /**
         * Store for Sales Invoice module subtype
         */
        this.salesInvoiceModuleStore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Sales, "Sales"],
            [Wtf.Subtype_Consignment,"Consignment"],
            [Wtf.Subtype_Lease,'Lease'],
            [Wtf.Subtype_Job_Order,'Job Order'],
            [Wtf.Subtype_Job_Order_Label,'Job Order Label'],
            [Wtf.Subtype_Opening_Invoice,'Opening Invoice'],
            [Wtf.Subtype_Asset,'Asset'],
            [Wtf.Subtype_JobWork,"Job Work"]
            ]
        });
        
        this.purchasemodulestore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Purchase, 'Purchase'],
            [Wtf.Subtype_Consignment,'Consignment'],
            [Wtf.Subtype_Asset,'Asset'],
            [Wtf.Subtype_JobWork,"Job Work"]
            ]
        });
        this.inventorymodulestore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Inventory, "Inventory"]
            ]
        });
        /**
         * Store for Inventory and Job work module
         */
        this.inventoryAndJobWorkModuleStore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Inventory, "Inventory"],
            [Wtf.Subtype_JobWork, "Job Work"]
            ]
        });
        this.QA_Approval_Module_Store =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_DeliveryOrder, "Delivery Order"]
            ]
        });
        this.Build_Assembly_Report_Module_Store = new Wtf.data.SimpleStore({//Template Subtype Store Create new template window ERM-26
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Default, "Default"]
            ]
        });
        
        if(moduleid==Wtf.Acc_Credit_Note_ModuleId){
            this.subtypestore=this.CN_SubtypeStore; 
        }else if(moduleid==Wtf.Acc_Debit_Note_ModuleId){
            this.subtypestore=this.DN_SubtypeStore; 
        }else if(moduleid==Wtf.Acc_Sales_Order_ModuleId){
            this.subtypestore=this.salesOrderModuleStore; 
        }else if(moduleid==Wtf.Acc_Invoice_ModuleId){
            this.subtypestore=this.salesInvoiceModuleStore; 
        }else if(moduleid==Wtf.Acc_Customer_Quotation_ModuleId || moduleid==Wtf.Acc_Receive_Payment_ModuleId){
            this.subtypestore=this.salesmodulestore; 
        }else if(moduleid==Wtf.Acc_Delivery_Order_ModuleId || moduleid==Wtf.Acc_Sales_Return_ModuleId){
            this.subtypestore=this.deliveryOrderAndSalesReturnModulestore; 
        }else if(moduleid==Wtf.Acc_Stock_Request_ModuleId ||moduleid==Wtf.Inventory_Stock_Repair_ModuleId
            ||moduleid==Wtf.Acc_InterLocation_ModuleId||moduleid==Wtf.Inventory_ModuleId||moduleid==Wtf.Acc_InterLocation_ModuleId){
            this.subtypestore=this.inventorymodulestore;
        }else if(moduleid==Wtf.Acc_Stock_Adjustment_ModuleId||moduleid==Wtf.Acc_InterStore_ModuleId){
            this.subtypestore=this.inventoryAndJobWorkModuleStore;
        }else if(moduleid==Wtf.QA_Approval_ID){
            this.subtypestore=this.QA_Approval_Module_Store; 
        }else if(moduleid === Wtf.Build_Assembly_Report_ModuleId || moduleid === Wtf.Bank_Reconciliation_ModuleId || moduleid === Wtf.MRP_Work_Order_ModuleID){
            this.subtypestore = this.Build_Assembly_Report_Module_Store;
        }else{
            this.subtypestore=this.purchasemodulestore; 
        }
        
        this.createTemplateForm =new Wtf.form.FormPanel({
            url: "CustomDesign/createTemplate.do", 
            frame:true,
            method : 'POST',
                
            labelWidth: 125,
            autoHeight:true,
            bodyStyle:'padding:5px 5px 0',
            autoWidth:true,
            defaults: {
                width: 175
            },
            defaultType: 'textfield',
            items:[{
                fieldLabel:WtfGlobal.getLocaleText("acc.campaigndetails.campaigntemplate.templatename"),
                id : 'templatefield'+this.id,
                name:'templatename',
                value : '',
                maskRe:/[A-Za-z0-9_: ]+/,
                allowBlank:false
            },this.SubtypeCombo=new Wtf.form.ComboBox({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectSubtype"),
                id:'subtype'+this.id,
                displayField: 'name',
                valueField: 'val',
                store:this.subtypestore,
                labelWidth:150,
                minWidth:300,
                mode: 'local',
                disabled:false
            })
            ],
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.field.Create"),
                id:'createBtn'+this.id,
                handler:function(){
                    Wtf.getCmp('createBtn'+this.id).disable();
                    if(this.createTemplateForm.form.isValid()){
                        this.createTemplateForm.form.submit({
                            scope: this,
                            params:{
                                moduleid : this.selectionModel.getSelections()[0].data.moduleid,
                                templatesubtype : this.SubtypeCombo.getValue()
                            },
                            success: function(frm, action){
                                var resObj = eval("(" + action.response.responseText + ")");
                                if(resObj.success == true) {
                                    WtfComMsgBox(["Message",resObj.data.msg], 0);
                                    this.createTemplateWin.close();
                                    this.templatelistStore.load({
                                        params : {
                                            start : 0,
                                            limit : this.pP.pagingToolbar.pageSize
                                        }
                                    });
                                    Wtf.getCmp('createBtn'+this.id).enable();
                                }
                            },
                            failure: function(frm, action){
                                WtfComMsgBox([0,WtfGlobal.getLocaleText("acc.field.Erroroccuredatserverside")], 1);
                                Wtf.getCmp('createBtn'+this.id).enable();
                            }
                        });
                    }else{
                        Wtf.getCmp('createBtn'+this.id).enable();
                    }    
                },
                scope:this
            }, {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function(){
                    this.createTemplateWin.close();
                }
            }]
        });
        //Create new Template form
        this.createTemplateWin=new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.CreateTemplate"),
            closable:true,
            width:500,
            autoHeight:true,
            plain:true,
            modal:true,
            items:this.createTemplateForm      //Custtom Layout call to line 391
        });
        this.createTemplateWin.show();
    },
    createNewDocumentDesignerWin : function(isCopy,rec,isRename) {  /* documentDesingerNew */
        var moduleid=this.selectionModel.getSelections()[0].data.moduleid;
        this.DN_SubtypeStore = new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Default, "Default"],
            [Wtf.Subtype_PurchaseReturn,"Purchase Return"],
            [Wtf.Subtype_Undercharge,"Undercharged Sales Invoice"],
            [Wtf.Subtype_Overcharge,"Overcharged Purchase Invoice"]
            ]
        });
        this.CN_SubtypeStore = new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Default, "Default"],
            [Wtf.Subtype_SalesReturn,"Sales Return"],
            [Wtf.Subtype_Undercharge,"Undercharged Purchase Invoice"],
            [Wtf.Subtype_Overcharge,"Overcharged Sales Invoice"]
            ]
        });
        
        this.salesmodulestore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Sales, "Sales"],
            [Wtf.Subtype_Consignment,"Consignment"],
            [Wtf.Subtype_Lease,'Lease']
            ]
        });
        this.deliveryOrderAndSalesReturnModulestore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Sales, "Sales"],
            [Wtf.Subtype_Consignment,"Consignment"],
            [Wtf.Subtype_Lease,'Lease'],
            [Wtf.Subtype_Asset,'Asset']
            ]
        });
        
        this.salesOrderModuleStore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Sales, "Sales"],
            [Wtf.Subtype_Consignment,"Consignment"],
            [Wtf.Subtype_Lease,'Lease'],
            [Wtf.Subtype_Job_Order,'Job Order'],
            [Wtf.Subtype_Job_Order_Label,'Job Order Label'],
            [Wtf.Subtype_Opening_Invoice,"Opening Invoice"],
            [Wtf.Subtype_JobWork,"Job Work"]
            ]
        });
        /**
         * Store for Sales Invoice module subtype
         */
        this.salesInvoiceModuleStore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Sales, "Sales"],
            [Wtf.Subtype_Consignment,"Consignment"],
            [Wtf.Subtype_Lease,'Lease'],
            [Wtf.Subtype_Job_Order,'Job Order'],
            [Wtf.Subtype_Job_Order_Label,'Job Order Label'],
            [Wtf.Subtype_Opening_Invoice,"Opening Invoice"],
            [Wtf.Subtype_Asset,"Asset"],
            [Wtf.Subtype_JobWork,"Job Work"]
            ]
        });
        
        this.purchasemodulestore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Purchase, "Purchase"],
            [Wtf.Subtype_Consignment,"Consignment"],
            [Wtf.Subtype_Asset,"Asset"],
            [Wtf.Subtype_JobWork,"Job Work"]
            ]
        });
        
        this.inventorymodulestore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Inventory, "Inventory"]
            ]
        });
        /**
         * Store for Inventory and Job work module
         */
        this.inventoryAndJobWorkModuleStore = new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Inventory, "Inventory"],
            [Wtf.Subtype_JobWork,"Job Work"]
            ]
        });
        this.SOAmodulestore =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_SOA, "Normal"],
            [Wtf.Subtype_SOI, "Invoice"],
            [Wtf.Subtype_SOA_Transaction_Currency, "Transaction Currency"]
            ]
        });
        this.QA_Approval_Module_Store =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_DeliveryOrder, "Delivery Order"]
            ]
        });
        
        this.Build_Assembly_Report_Module_Store =new Wtf.data.SimpleStore({
            fields: ['val', 'name'],
            data: [
            [Wtf.Subtype_Default, "Default"]
            ]
        });
        
        if(moduleid==Wtf.Acc_Credit_Note_ModuleId){
            this.subtypestore=this.CN_SubtypeStore; 
        }else if(moduleid==Wtf.Acc_Debit_Note_ModuleId){
            this.subtypestore=this.DN_SubtypeStore; 
        }else if(moduleid==Wtf.Acc_Sales_Order_ModuleId){
            this.subtypestore=this.salesOrderModuleStore; 
        }else if(moduleid==Wtf.Acc_Invoice_ModuleId){
            this.subtypestore=this.salesInvoiceModuleStore; 
        }else if(moduleid==Wtf.Acc_Customer_Quotation_ModuleId || moduleid==Wtf.Acc_Receive_Payment_ModuleId){
            this.subtypestore=this.salesmodulestore; 
        }else if(moduleid==Wtf.Acc_Delivery_Order_ModuleId || moduleid==Wtf.Acc_Sales_Return_ModuleId){
            this.subtypestore=this.deliveryOrderAndSalesReturnModulestore; 
        }else if(moduleid==Wtf.Acc_Stock_Request_ModuleId ||moduleid==Wtf.Inventory_Stock_Repair_ModuleId
            ||moduleid==Wtf.Acc_InterLocation_ModuleId||moduleid==Wtf.Inventory_ModuleId||moduleid==Wtf.Acc_InterLocation_ModuleId){
            this.subtypestore=this.inventorymodulestore;
        }else if(moduleid==Wtf.Acc_Stock_Adjustment_ModuleId||moduleid==Wtf.Acc_InterStore_ModuleId){
            this.subtypestore=this.inventoryAndJobWorkModuleStore;
        }else if (moduleid==Wtf.Acc_Customer_AccountStatement_ModuleId || moduleid==Wtf.Acc_Vendor_AccountStatement_ModuleId  ){
            this.subtypestore=this.SOAmodulestore;
        }else if(moduleid==Wtf.QA_Approval_ID){
            this.subtypestore=this.QA_Approval_Module_Store; 
        }else if(moduleid === Wtf.Build_Assembly_Report_ModuleId || moduleid === Wtf.Bank_Reconciliation_ModuleId || moduleid === Wtf.MRP_Work_Order_ModuleID){
            this.subtypestore = this.Build_Assembly_Report_Module_Store;
        }else {
            this.subtypestore=this.purchasemodulestore; 
        }
        
        this.createTemplateForm =new Wtf.form.FormPanel({
            url:isCopy?"CustomDesign/copyTemplate.do":(isRename?"DocumentDesignController/renameNewDocument.do":"DocumentDesignController/createNewDocument.do"),
            frame:true,
            method : 'POST',

            labelWidth: 125,
            autoHeight:true,
            bodyStyle:'padding:5px 5px 0',
            autoWidth:true,
            defaults: {
                width: 175
            },
            defaultType: 'textfield',
            items:[{
                fieldLabel:WtfGlobal.getLocaleText("acc.campaigndetails.campaigntemplate.templatename"),
                id : 'templatefield'+this.id,
                name:'templatename',
                value : (rec === undefined || isCopy)? '':rec.data.templatename,
                maskRe:/[A-Za-z0-9_: ]+/,
                allowBlank:false
            },this.SubtypeCombo=new Wtf.form.ComboBox({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectSubtype"),
                id:'subtype'+this.id,
                displayField: 'name',
                valueField: 'val',
                store:this.subtypestore,
                labelWidth:150,
                minWidth:300,
                mode: 'local',
                triggerAction: 'all',
                value : (rec === undefined || (!isRename && !isCopy)) ? '0' : rec.data.templatesubtype,
                disabled:isRename
            })
            ],
            buttons:[{
                text:isCopy?WtfGlobal.getLocaleText("acc.common.copy"):(isRename?WtfGlobal.getLocaleText("acc.common.Rename"):WtfGlobal.getLocaleText("acc.field.Create")),
                id:'createBtnForNewTemplate'+this.id,
                handler:function(){
                    Wtf.getCmp('createBtnForNewTemplate'+this.id).disable();
                    if(Wtf.getCmp('templatefield'+this.id)){
                        var templatename = Wtf.getCmp('templatefield'+this.id).getValue();
                        if(templatename.trim() === ""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.field.CustomTemplateName.blankmsg")], 2);
                            Wtf.getCmp('createBtnForNewTemplate'+this.id).enable();
                            return false;
                        }
                    }
                    if(this.createTemplateForm.form.isValid()){
                        this.createTemplateForm.form.submit({
                            scope: this,
                            params:{
                                moduleid : this.selectionModel.getSelections()[0].data.moduleid,
                                templatesubtype : this.SubtypeCombo.getValue()!=""?this.SubtypeCombo.getValue():'0',
                                iscopy : isCopy? isCopy :false,
                                record : rec? rec :"",
                                templateid : isCopy || isRename? rec.data.templateid :"",
                                isdefault : isCopy? ((rec.data.isdefaulttemplate)?rec.data.isdefaulttemplate:false) :false
                            },
                            success: function(frm, action){
                               var resObj = eval("(" + action.response.responseText + ")");
                               var keyMsg = resObj.data.msg;
                                if(resObj.success == true) {
                                    WtfComMsgBox(["Message",WtfGlobal.getLocaleText(keyMsg)], 0);
                                    this.createTemplateWin.close();
                                    this.templatelistStore.load({
                                        params : {
                                            start : 0,
                                            limit : this.pP.pagingToolbar.pageSize
                                        }
                                    });
                                     Wtf.getCmp('createBtnForNewTemplate'+this.id).enable();
                                }
                            },
                            failure: function(frm, action){
                                var resObj = eval("(" + action.response.responseText + ")");
                                if(resObj.success == false) {
                                    if(resObj.data.msg){
                                        WtfComMsgBox(["Message",resObj.data.msg], 0);
                                    } else{
                                        WtfComMsgBox([0,WtfGlobal.getLocaleText("acc.field.Erroroccuredatserverside")], 1);
                                    }
                                }else{
                                    WtfComMsgBox([0,WtfGlobal.getLocaleText("acc.field.Erroroccuredatserverside")], 1);
                                }
                                Wtf.getCmp('createBtnForNewTemplate'+this.id).enable();
                            }
                        });
                    }else{
                        Wtf.getCmp('createBtnForNewTemplate'+this.id).enable();
                    }
                },
                scope:this
            }, {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function(){
                    this.createTemplateWin.close();
                }
            }]
        });
        //Create new Template form
        this.createTemplateWin=new Wtf.Window({
            title: isCopy?WtfGlobal.getLocaleText("acc.field.CopyTemplate"):isRename?WtfGlobal.getLocaleText("acc.field.RenameTemplate"):(WtfGlobal.getLocaleText("acc.field.CreateTemplate")),
            closable:true,
            width:500,
            autoHeight:true,
            plain:true,
            modal:true,
            items:this.createTemplateForm      //Custtom Layout call to line 391
        });
        this.createTemplateWin.show();
    },
    deleteTemplatesWin:function(){
        var templateIds = [];
        var cnt = 0;
        while(cnt < this.sm.selections.length){
            if (!this.sm.selections.items[cnt].data.isdefaulttemplate){
                templateIds[cnt] = this.sm.selections.items[cnt].data.templateid;
                cnt++;
            } else {
                //                Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Youcannotdeletedefaulttemplates"));
                Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.common.warning"),//'Warning',
                    msg:WtfGlobal.getLocaleText("acc.field.Youcannotdeletedefaulttemplates"),//"You cannot delete Default Templates.",
                    icon:Wtf.MessageBox.INFO,
                    buttons:Wtf.MessageBox.OK,
                    scope:this
                });
                return false;
            }
        }
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedcustomlayouttemplates"),function(btn){
            if(btn=="yes") {
                Wtf.Ajax.requestEx({
                    url:"CustomDesign/deleteCustomTemplatemodule.do",
                    params: {
                        templateid : templateIds.toString(),
                        moduleid: this.selectionModel.getSelections()[0].data.moduleid
                    }
                },this,this.genSuccessResponse,this.genFailureResponse);

            }
        }, this);
    },
    getActiveTemplate:function(){
        var moduleid = this.ShortUserGrid.selModel.getSelected().data.moduleid;
            this.templatelistStore.load({
                params : {
                    start : 0,
                    limit : this.pP.pagingToolbar.pageSize
                }
            });
    }
});

function callTemplateImportWin(storeageName,fileName,titleMsg,importUrl,showdateformater,grid) {
    if(Wtf.getCmp('SettingsImportWindow')==undefined){
            callTemplateImportWindow(storeageName,fileName,titleMsg,importUrl,showdateformater,grid);
    }
}
function callTemplateImportWindow(storeageName,fileName,titleMsg,importUrl,showdateformater,grid) {

    var downloadSampleFileLink=new Wtf.XTemplate(
        "<div> &nbsp;</div>",  //Currency:
        '<tpl>',
        WtfGlobal.getLocaleText("acc.field.DownloadSampleFile")+" <a class='tbar-link-text' href='#' onClick='javascript: downloadSampleFie(\""+storeageName+"\",\""+fileName+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.field.SampleFile")+"</a>",
        '</tpl>'
        );

    var sampleLinkPanel = new Wtf.Panel({
        border:false,
        html:downloadSampleFileLink.apply()
    })

    var delimiterStore = new Wtf.data.SimpleStore({
        fields: ['delimiterid','delimiter'],
        data : [
        [0,'Comma']
        ]
    });
    this.conowner= new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.rem.88")+" ",  //'Delimiter ',
        hiddenName:'Delimiter',
        store:delimiterStore,
        valueField:'delimiter',
        displayField:'delimiter',
        mode: 'local',
        triggerAction: 'all',
        emptyText:WtfGlobal.getLocaleText("acc.rem.89"),  //'--Select delimiter--',
        typeAhead:true,
        selectOnFocus:true,
        allowBlank:false,
        width: 200,
        itemCls : (this.typeXLSFile)?"hidden-from-item":"",
        hidden: this.typeXLSFile,
        hideLabel: this.typeXLSFile,
        forceSelection: true,
        value:'Comma'
    });

    this.dfRec = Wtf.data.Record.create ([
    {
        name:'formatid'
    },

    {
        name:'name'
    }
    ]);

    this.dfStore=new Wtf.data.Store({
        url:"kwlCommonTables/getAllDateFormats.do",
        baseParams:{
            mode:32
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.dfRec)
    });
    this.dfStore.load();
    this.dfStore.on('load',function(){
        if(this.dfStore.getCount()>0){
            this.datePreference.setValue("2"); // Default for dd/MM/yy
        }
    },this);

    this.datePreference= new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.import.df"),  //'Date Format',
        hiddenName:'dateFormat',
        store:this.dfStore,
        valueField:'formatid',
        displayField:'name',
        mode: 'local',
        triggerAction: 'all',
        width: 200,
        itemCls : (this.typeXLSFile)?"hidden-from-item":"",
        hidden: !showdateformater,
        hideLabel:!showdateformater,
        forceSelection: true
    });

    this.importForm = new Wtf.form.FormPanel({
        waitMsgTarget: true,
        method: 'POST',
        border: false,
        region: 'center',
        bodyStyle: "background: transparent;",
        style: "background: transparent;padding:20px;",
        labelWidth: 100,
        frame: false,
        fileUpload: true,
        items: [{
            xtype: 'textfield',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectFile*"),
            name: "importTransactionsFile",
            inputType: 'file',
            //            msgTarget: 'under',
            allowBlank:false,
            id: "importTransactionsFileID"
        }
        ]
    });
    this.importTransactionsWindow = new Wtf.Window({
        modal: true,
        iconCls :'importIcon',
        id:'SettingsImportWindow',
        title: titleMsg,//WtfGlobal.getLocaleText("acc.common.import")+titleMsg
        bodyStyle: 'padding:5px;',
        buttonAlign: 'right',
        width: 500,
        scope: this,
        items: [{
            region: 'north',
            height: 90,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(titleMsg, "<ul style='list-style-type:disc;padding-left:15px;'><li>" + WtfGlobal.getLocaleText("acc.rem.252") + "</li><li><b>" + WtfGlobal.getLocaleText("acc.common.note") + "</b>: " + WtfGlobal.getLocaleText("acc.rem.92") + "</li></ul>", "../../images/import.png", true, "5px 0px 0px 0px", "7px 0px 0px 10px")
        }, {
            region: 'center',
            border: false,
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            autoScroll: true,
            items: this.importForm
        }],
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.uploadbtn"),
            scope: this,
            handler: function(button) {
                var value = Wtf.getCmp('importTransactionsFileID').getValue();
                var extension =value.substr(value.lastIndexOf(".")+1);
                var patt1 = new RegExp("xls","i");
                if (this.importForm.form.isValid() && value != '') {
                    if(patt1.test(extension)) {

                        this.importForm.getForm().submit({
                            url: importUrl+"&method=upload&delimiterType=Comma&dateFormat="+this.datePreference.getValue()+"&titleMsg="+titleMsg,
                            waitMsg :WtfGlobal.getLocaleText("acc.rem.167"),  //'Uploading File...',
                            success: function(req, res) {
                                this.importTransactionsWindow.close();
//                                    WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.result.msg], 0);
//                                    grid.store.reload();
//                                    grid.getView().refresh();
                                var filePath = res.result.filePath;
                                var data = JSON.parse(res.result.customFieldJsonStr);     // All Custom fields present in template.
                                if(data && data.data!=""){  //If custom fields are present in template then create window showing those custom fields else directly import that template.
                                    callTemplateImportCustomColCreateWin(storeageName,fileName,titleMsg,importUrl,showdateformater,grid,filePath,data); 
                                }else{
                                    Wtf.Ajax.request({
                                        url:importUrl,
                                        params: {
                                            customFieldJson:"{data:[]}",
                                            method:"getMapXLS",
                                            filePath:filePath,
                                            discardFieldJson:"{data:[]}"
                                        },
                                        success:function(req,res) {
                                            var result = JSON.parse(req.responseText);
                                            if(result.msg!= null){
                                                 WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), result.msg], 0);
                                            }
                                            grid.store.reload();
                                            grid.getView().refresh();
                                        },
                                        failure: function(req, response) {
                                            this.customColTemplateImportWin.close();
                                            var result = JSON.parse(req.responseText);
                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),result.msg],2);
                                        }
                                    })
                                }
                                    
                            },
                            scope: this,
                            failure: function(req,response) {
                                this.importTransactionsWindow.close();
                                var resultObj = eval('('+response.response.responseText+')');
                                if(resultObj.msg!= null){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),resultObj.msg],2);
                                }
                            }
                        })
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.84")],2);
                    }
                } else {
                    return false;
                }
            }
        }, {
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler: function() {
                this.importTransactionsWindow.close();
            }
        }]
    });

    this.importTransactionsWindow.show();
}
function callTemplateImportCustomColCreateWin(storeageName,fileName,titleMsg,importUrl,showdateformater,grid,filePath,data) {
    if(Wtf.getCmp('templateImportCustomColWindow')==undefined){
            callTemplateImportCustomColCreateWindow(storeageName,fileName,titleMsg,importUrl,showdateformater,grid,filePath,data);
    }
}
function callTemplateImportCustomColCreateWindow(storeageName,fileName,titleMsg,importUrl,showdateformater,grid,filePath,data) {
    
    this.customColRec = new Wtf.data.Record.create([
        {
            name: 'fieldno'
        },
        {
            name: 'fieldname'
        },
        {
            name: 'xtype'
        },
        {
            name: 'values'
        },
        {
            name: 'typeName'
        },
        {
            name: 'fieldLevelName'
        },
        {
            name: 'moduleid'
        },
        {
            name: 'isLineItem'
        }
    ]);
    this.customColStore=new Wtf.data.Store({
        url:importUrl,
        baseParams:{
            mode:32
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.customColRec),
//        fields:this.customColRec,
        data:data
    });
    this.columnArr = [];
    this.sm = new Wtf.grid.CheckboxSelectionModel({
//        singleSelect: true
    });
    this.columnArr.push(
            this.sm,
            {
                header:"Name",
                dataIndex:"fieldname"
            },
            {
                header:"Type",
                dataIndex:"xtype"
            },
            {
                header:"Values",
                dataIndex:"values",
                editor: new Wtf.form.TextField({
                    
                })
            },
            {
                header:"Custom/ Dimension Field",
                dataIndex:"typeName"
            },
            {
                header:"Global/ Line Level Field",
                dataIndex: "fieldLevelName"
            }
    )
    this.colModel = new Wtf.grid.ColumnModel(this.columnArr);

    this.custFieldGrid = new  Wtf.grid.EditorGridPanel({
        store: this.customColStore,
        cm: this.colModel,
        height: 400,
        sm: this.sm,
        scope:this,
        viewConfig :{
            emptyText: "There are no Custom Fields present in this template.",
            forceFit:true
        }
       
    });
    this.custFieldGrid.on("beforeedit",function(e) {
        return importTemplateCustBeforeEdit(e,this);
    },this);
    this.panel = new Wtf.Panel({
        width:"100%",
        height:"100%",
        html: "&nbsp;&nbsp;" + WtfGlobal.getLocaleText("acc.please.select.customFields") + " </br></br>",
        border:false
    })
    this.customColTemplateImportWin = new Wtf.Window({
        modal:true,
        iconCls :'importIcon',
        id:'templateImportCustomColWindow',
        title: titleMsg,//WtfGlobal.getLocaleText("acc.common.import")+titleMsg
        bodyStyle: 'padding:5px;',
        buttonAlign: 'right',
        width: 500,
        scope: this,
        items:[this.panel,this.custFieldGrid],
        buttons:[
            {
                text:WtfGlobal.getLocaleText("acc.common.import"),
                scope:this,
                handler: function () {
                    var custJson={};
                    var custJarr = [];
                    var custFieldNames=[];
                    var jobj = this.custFieldGrid.selModel.getSelections();
                    for ( var index =0; index< jobj.length; index++) {
                        custJarr.push(jobj[index].data);
                        custFieldNames.push(jobj[index].data.fieldname);
                    }
                    custJson.data = custJarr;
                    
                    var disCustJarr = [];
                    var disCustjson = {};
                    var disJobj = this.customColStore.data.items;
                    for ( index =0; index< disJobj.length; index++) {
                        if ( custFieldNames.indexOf(disJobj[index].data.fieldname) === -1 ){
                            disCustJarr.push(disJobj[index].data);
                        }
                    }
                    disCustjson.data = disCustJarr;
                    
                    Wtf.Ajax.request({
                        url:importUrl,
                        params: {
                            customFieldJson:JSON.stringify(custJson),
                            method:"getMapXLS",
                            filePath:filePath,
                            discardFieldJson:JSON.stringify(disCustjson)
                        },
                        success:function(req,res) {
                            this.customColTemplateImportWin.close();
                            var result = JSON.parse(req.responseText);
                            WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), result.msg], 0);
                            grid.store.reload();
                            grid.getView().refresh();
                        },
                        failure: function(req, response) {
                            this.customColTemplateImportWin.close();
                            var result = JSON.parse(req.responseText);
//                            var resultObj = eval('('+response.response.responseText+')');
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),result.msg],2);
                        }
                    });
                    
                }
            },
            {
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope:this,
                handler:function(){
                    if ( this.customColTemplateImportWin ) {
                        this.customColTemplateImportWin.close();
                    }
                }
            }
        ]
    });
    
    this.customColTemplateImportWin.on("render", function(){
        if(data.data.length === 0) {
            this.custFieldGrid.getStore().removeAll();
        }
    },this);
    this.customColTemplateImportWin.show();
}
function importTemplateCustBeforeEdit(e, obj) {
    if (e.field === "values") {
        if (e.record.data.xtype === "4") {
            return true;
        } else {
            return false;
        }
    }
}

function callTemplateMultipleExport() {
    
    this.templateExpRec = new Wtf.data.Record.create([
        {
            name: 'templateid'
        },
        {
            name: 'templatename'
        },
        {
            name: 'moduleid'
        },
        {
            name: 'moduleName'
        }
    ]);
    this.templateExportStore=new Wtf.data.GroupingStore({
        url:"CustomDesign/getDesignTemplateList.do",
        baseParams:{
            moduleid:0,
            isActive:1
        },
        groupField: 'moduleName',
         sortInfo:{field: 'templatename', direction: "ASC"},
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"totalCount"
        },this.templateExpRec)
        
    });
    this.sm = new Wtf.grid.CheckboxSelectionModel({});
    this.columnArr = [];
    this.columnArr.push(
            this.sm,
            {
                header:WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup.name"),
                dataIndex:"templatename"
            },
            {
                header:WtfGlobal.getLocaleText("acc.common.moduleName"),
                dataIndex:"moduleName"
            }
    );
    this.colModel = new Wtf.grid.ColumnModel(this.columnArr);
       
    this.templateExpGrid = new  Wtf.grid.GridPanel({
        store: this.templateExportStore,
        cm: this.colModel,
        height: 400,
        sm: this.sm,
        scope:this,
        view: new Wtf.grid.GroupingView({
            forceFit:true,
            // custom grouping text template to display the number of items per group
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})',
             enableGroupingMenu:true
        })
       
    });
    this.templateExpGrid.on('groupclick', function(grid, field, value, e){
        var t = e.getTarget('.my-class');
        if (t) {
            var checked = t.checked;
            grid.getStore().each(function(rec, index){
                if(rec.get('field') == value){
                    grid.getSelectionModel()[checked ? 'selectRow' : 'deselectRow'](index);
                }
            });
        }
    });
    this.templateExportStore.load();
    this.templateExportWin = new Wtf.Window({
        modal:true,
        iconCls :'importIcon',
        id:'templateExportAllWindow',
        title: WtfGlobal.getLocaleText("acc.customdesigner.export.template"),
        bodyStyle: 'padding:5px;',
        buttonAlign: 'right',
        width: 500,
        scope: this,
        items:[this.templateExpGrid],
        buttons:[
            {
                text:WtfGlobal.getLocaleText("acc.ra.export"),
                scope:this,
                handler: function () {
                    exportMultipleTemplateConfig(this.templateExpGrid);
//                    this.close();
                }
            },
            {
                text:WtfGlobal.getLocaleText("acc.common.close"),
                scope:this,
                handler: function () {
                    if ( Wtf.getCmp("templateExportAllWindow") ) {
                        Wtf.getCmp("templateExportAllWindow").close()
                    }
                }
            }
            ]
    });
    this.templateExportWin.show(); 
}
function groupSelect() {
//    alert("a");
}

function exportMultipleTemplateConfig(grid) {
        var align = "none,none,none,none,none,none,none,none,none,none,none,none,none,none,none";
        if ( grid.getSelections().length === 0 ) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.customdesigner.export.warning")], 0);
            return;
        }
        var selection = grid.getSelections();
        var selectedTemplates = [];
        for ( var index = 0 ; index < selection.length; index++ ) {
            selectedTemplates.push(selection[index].data);
        }
        var selectedTemplatesStr = JSON.stringify(selectedTemplates);
        var filename = "Templates";
        var exportUrl = "DocumentDesignController/exportTemplates.do";
        var type = "xlsx";
        var get = 1;
        var header = "templatename,moduleid,json,pagelayoutproperty,footerjson,headerjson,html,headerhtml,footerhtml,sqlquery,headersqlquery,footersqlquery,customColJson,image,templatesubtype";
        var nondeleted = "true";
        var title = "Template Name,Module ID,JSON,Page Layout Property,Footer JSON, Header JSON,HTML,Header HTML,Footer HTML,SQL Query,Header SQL Query,Footer SQL Query,Custom Filed Json,Image,Sub Type";
        var width = "75,75,75,75,75,75,75,75,75,75,75,75,75,75,75";
        var url = exportUrl + "?filename=" + encodeURIComponent(filename) + "+&filetype=" + type + "&nondeleted=" + nondeleted + "&header=" + header + "&title=" + encodeURIComponent(title) + "&width=" + width + "&get=" + get + "&align=" + align + "&selectedTemplates=" + selectedTemplatesStr + "&isExportMultiple=true"  ;
        Wtf.get('downloadframe').dom.src = url;
//        alert("templateExported");
}

function createTextWithToolTip(text){ //ERP-20593
    return "<span  wtf:qtip='"+text+"' style='width:100%;'>"+text+"</span>";
}
