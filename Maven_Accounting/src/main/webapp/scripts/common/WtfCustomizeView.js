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
Wtf.customizeView= function(config){
    this.parentHelpModeId=config.parentHelpModeId;
    this.isForFormFields=(config.isForFormFields!=null && config.isForFormFields!=undefined)?config.isForFormFields:false;
    this.isForLineFields=(config.isForLineFields!=null && config.isForLineFields!=undefined)?config.isForLineFields:false;
    this.isOrderCustOrDimFields=(config.isOrderCustOrDimFields!=null && config.isOrderCustOrDimFields!=undefined)?config.isOrderCustOrDimFields:false;
    this.parentId=config.parentId;
    Wtf.customizeView.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.customizeView,Wtf.Window, {
    resizable: false,
    scope: this,
    autoScroll:true,
    layout: 'border',
    modal:true,
    width: 800,
    height: 550,
    iconCls: getButtonIconCls(Wtf.etype.deskera),  //'pwnd favwinIcon',
    id: 'acc_customize_header',
    title: this.isOrderCustOrDimFields?WtfGlobal.getLocaleText("acc.masterconfig.orderingOfCustomOrDimensions"):WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),//'Customize View',
    initComponent: function() {
        Wtf.customizeView.superclass.initComponent.call(this);
        this.addEvents("aftersave");
        this.on('beforeclose', this.askToClose,this);
        this.isEdit=false;
        this.changeData = [];
        this.addButton({text : WtfGlobal.getLocaleText("acc.common.saveBtn"), disabled:this.isOrderCustOrDimFields?true:false, id:'savebtn'+this.id}, function(){
            this.saveColumnChanges();
        },this);
        this.addButton({text:WtfGlobal.getLocaleText("acc.common.close"), id:'closebtn'+this.id}, function(e){
            this.askToClose(e);
        },this);
    },
    
    askToClose:function(e){
        if(this.isEdit) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.field.Confirmation"),
                msg: WtfGlobal.getLocaleText("acc.field.Maybethedatayoufilledisstillunsaved.")+"<br>"+WtfGlobal.getLocaleText("acc.field.Doyouwanttoclosethepanel"),
                buttons: Wtf.MessageBox.YESNO,
                animEl: 'mb9',
                fn:function(btn){
                    if(btn=="yes")
                        this.closeWin();
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
            return false;
        } else {
            this.closeWin();
        }
    },
    
    closeWin : function () {
        this.un('beforeclose', this.askToClose,this);
        this.close();
    },
    
    saveColumnChanges : function() {
 
        //            WtfGlobal.setAjaxReqTimeout();
        //            Wtf.commonWaitMsgBox("Validating records...");
            
        //                var records = this.moduleHeaderGrid.getStore().queryBy(function(record) {
        //                    return record.get('hidecol') === true;
        //                });
        // Collect ids of those records
        var idsArray = [];
        var records = this.moduleHeaderGrid.getStore();
        for (var i = 0; i < this.moduleHeaderGrid.getStore().getCount(); i++) {
            if (this.moduleHeaderGrid.getStore().getAt(i).data.fieldname == "Including VAT") {
                this.moduleHeaderGrid.getStore().getAt(i).data.fieldname == "Including GST";
            }
        }
        if (this.isOrderCustOrDimFields) {
            Wtf.getCmp('savebtn'+this.id).disable();
            for(var i=0;i<this.moduleHeaderGrid.getStore().getCount();i++){
                idsArray.push({
                    "id": this.moduleHeaderGrid.getStore().getAt(i).get("id"),
                    "sequence": i+1
                });
            }
        } else {
            records.each(function(record) {
                var ids = [];
                idsArray.push("{'id'='"+record.get('id')+"','isManadatoryField'='"+record.get('isManadatoryField')+"','isForProductandServices'='"+record.get('isForProductandServices')+"','fieldname'='"+record.get('fieldname')+"','fieldDataIndex'='"+record.get('fieldDataIndex')+"','parentid'='"+record.get('parentid')+"','hidecol'='"+record.get('hidecol')+"','hidefieldfromreport'='"+record.get('hidefieldfromreport')+"','isreadonlycol'='"+record.get('isreadonlycol')+"','fieldlabeltext'='"+record.get('fieldlabeltext')+"','isUserManadatoryField'='"+record.get('isUserManadatoryField')+"','modulestoshowintheirforms'='"+record.get('modulestoshowintheirforms')+"'}");
            //                    ids.push("id="+record.get('id'));
            //                    ids.push("fieldname="+record.get('fieldname'));
            //                    ids.push("fieldDataIndex="+record.get('fieldDataIndex'));
            //                    idsArray.push(ids);
            });
        }
        
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/saveCustomizedReportFields.do",
            params: {
                flag: 34,
                moduleid:this.moduleid,
                reportId:1,
                isFormField:this.isForFormFields,
                isLineField:this.isForLineFields,
                //                    data: ids,
                data:Wtf.util.JSON.encode(idsArray),
                isOrderCustOrDimFields:this.isOrderCustOrDimFields
            }
        }, this, function(action, response){
            if(action.success){
                this.closeWin();
//                loadCustomFieldColModel(undefined, '2,4,6,8,10,12,14,16,18,20,22,23,24,27,28,29,30,31,32,33,36,38,39,40,41,79,50,51,52,53,57,58,59,63,92'.split(','));
                loadCustomFieldColModel(undefined, (this.moduleid+',30').split(','));
                
                if (this.moduleid == Wtf.Acc_PRO_ModuleId) {
                    loadGlobalProductMasterFields()
                }
//                loadCustomFeildsColumnModelForReports(undefined, '20,18,2,6,14,16,22,23,12,10,24,27,28,29,31,32,33,36,38,39,40,41,42,79,50,51,52,53,57,58,59,35,63,87,88,89,90,92,93,64,65,67,68'.split(','));  //(Mayur B) load column model for invoice and purchase order 
                loadCustomFeildsColumnModelForReports(undefined, (this.moduleid+',30').split(','));  //(Mayur B) load column model for invoice and purchase order 
                Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.field.Confirmation"),
                    msg: WtfGlobal.getLocaleText("acc.field.Configurationsavedsuccessfully"),
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    fn:function(btn){
                        if(this.isForFormFields){
                            return;
                        }
                        if(this.isOrderCustOrDimFields){
                            return;
                        }
                        if(this.moduleid==Wtf.Acc_Customer_ModuleId||this.moduleid==Wtf.Acc_Vendor_ModuleId){
                            this.parentGrid=Wtf.getCmp(this.parentId).grid;
                        }else{
                            this.parentGrid=Wtf.getCmp("gridmsg"+this.parentHelpModeId+this.parentId);
                        }
                        if(this.parentGrid){
                            this.parentGrid.store.load({
                                params:{
                                    start : 0,
                                    limit : 30
                                }
                            });
                        }
                    },
                    scope:this,
                    icon: Wtf.MessageBox.INFO
                });
            //                    return false;
                    
            //                    alert("success");
            } else {
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.invoiceList.status"), action.msg);
            }
        },function() {
            //                Wtf.updateProgress();
            //                WtfGlobal.resetAjaxReqTimeout();
            });
        
    },
    
    onRender: function(config) {
        Wtf.customizeView.superclass.onRender.call(this, config);
        
//        this.parentGrid=Wtf.getCmp("gridmsg"+this.parentHelpModeId+this.parentId);

        this.moduleHeaderRec = new Wtf.data.Record.create([
            {name:"id"},
            {name:"fieldname"},
            {name:"fieldlabeltext"},
            {name:"isManadatoryField"},
            {name:"isUserManadatoryField"},
            {name:"headerid"},
            {name:"newheader"},
            {name:"ismandotory"},
            {name:"iseditable"},
            {name:"pojoname"},
            {name:"xtype"},
            {name:"columntype"},
            {name:"required"},
            {name:"roleid"},
            {name:"rolename"},
            {name:"roleidforeditableperm"},
            {name:"rolenameforeditableperm"},
            {name:"hidecol"},
            {name:"isreadonlycol"},
            {name:"fieldDataIndex"},
            {name:"sequence"},
            {name:"modulestoshowintheirforms"},
            {name:"isReportField"},
            {name:"hidefieldfromreport"},
            {name:"isForProductandServices"},
            {name:"parentid"}
        ]);

        this.moduleHeaderReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.moduleHeaderRec);

        this.moduleHeaderStore = new Wtf.data.GroupingStore({
            url: (this.isForFormFields)?"ACCAccountCMN/getTransactionFormFields.do":"ACCAccountCMN/getModuleFields.do",
            reader:this.moduleHeaderReader,
            baseParams:{
                flag:35,
                moduleid:this.moduleid,
                reportId:1,
                isOrderCustOrDimFields:this.isOrderCustOrDimFields
            },
            groupField:"columntype",
            sortInfo: this.isOrderCustOrDimFields?{field: 'sequence', direction: "ASC"}:{field: 'fieldname', direction: "ASC"}
        });
        if(!this.isOrderCustOrDimFields){
            this.moduleHeaderStore.load();
        }
         var checkColumn = new Wtf.grid.CheckColumnCustomized({
            header: (this.isForFormFields)?WtfGlobal.getLocaleText("acc.field.HideField"):WtfGlobal.getLocaleText("acc.field.HideColumn"),
            dataIndex: 'hidecol',
            hidden:this.isOrderCustOrDimFields,
            width: 80
        });
        
         var hideFieldFromReport = new Wtf.grid.CheckColumnCustomized({
            header: "<div wtf:qtip=\""+WtfGlobal.getLocaleText("acc.product.hidefieldsfromreport.title")+"\">"+WtfGlobal.getLocaleText("acc.product.hidefieldsfromreport.title")+"</div>",//"Hide Field from report"
            dataIndex: 'hidefieldfromreport',
            hidden:(this.moduleid==Wtf.Acc_PRO_ModuleId || this.moduleid==Wtf.Acc_Contract_ModuleId || this.moduleid==Wtf.Acc_Lease_Contract || this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || Wtf.Acc_RFQ_ModuleId) ? false : true,
            width: 130
        });
        
        var isReadOnlyCol = new Wtf.grid.CheckColumnCustomized({
            header: WtfGlobal.getLocaleText("acc.field.ReadOnly"),                       // "Read Only"
            hidden : !this.isForFormFields || this.isOrderCustOrDimFields,
            dataIndex: 'isreadonlycol',            
            width: 80
        });
        
        var setMandatoryCol = new Wtf.grid.CheckColumnCustomized({
            header: "<div wtf:qtip=\""+WtfGlobal.getLocaleText("acc.customfield.isMandetory")+"\">"+WtfGlobal.getLocaleText("acc.customfield.isMandetory")+"</div>",                       // "Read Only"
            hidden : !this.isForFormFields || this.isOrderCustOrDimFields,
            dataIndex: 'isUserManadatoryField',            
            width: 75
        });
        
        this.moduleStoreForshowinforms=new Wtf.data.SimpleStore({
            fields:[{name:"id"},{name:"name"}],
            data:[
                [Wtf.Acc_Invoice_ModuleId, WtfGlobal.getLocaleText("acc.field.CustomerInvoice")],
                [Wtf.Acc_Vendor_Invoice_ModuleId, WtfGlobal.getLocaleText("acc.pi.PurchaseInvoice")],
                [Wtf.Acc_Purchase_Order_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.10")],
                [Wtf.Acc_Sales_Order_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.9")],
                [Wtf.Acc_Customer_Quotation_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.12")],
                [Wtf.Acc_Vendor_Quotation_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.11")],
            ]
        });
        this.moduleNameConbo = new Wtf.common.Select({
            labelStyle: 'width:120px;margin-left: 5px;',
            name: 'modulestoshowintheirforms',
            hiddenName: 'modulestoshowintheirforms',
            store: this.moduleStoreForshowinforms,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            id:'moduleNameConbo',
            multiSelect: true,
            forceSelection: true,
            xtype: 'select',
            triggerAction: 'all',
            typeAhead: true,
            scope: this
        });
        
        var selModel = new Wtf.grid.RowSelectionModel({});
        this.moduleHeaderColumn = new Wtf.grid.ColumnModel([
            {
                header:WtfGlobal.getLocaleText("acc.customfield.customtype"),//"Custom Type",
                hidden:true,
                fixed:true,
                dataIndex:"columntype"
 
            },{
                header:(this.isForFormFields)?WtfGlobal.getLocaleText("acc.field.Field"):WtfGlobal.getLocaleText("acc.customfield.fieldname"),//"Header",
                dataIndex:"fieldname",
                groupRenderer: WtfGlobal.nameRenderer

            }, {
                header: WtfGlobal.getLocaleText("acc.field.FieldLabel"),  //"Field Label",
                dataIndex: 'fieldlabeltext',
                hidden : !this.isForFormFields || this.isOrderCustOrDimFields,
                editor: new Wtf.form.ExtendedTextField({
                    maxLength:50,
                    regex:Wtf.specialChar
                })
            } ,{
                header:WtfGlobal.getLocaleText("acc.customfield.customizename"),//"Customize Header",
                dataIndex:"newheader",
                hidden:true,
                editor: new Wtf.ux.TextField({
                     allowBlank: true,
                     maskRe:/[^,]+/
                })
            },{
                header: "<div wtf:qtip=\""+WtfGlobal.getLocaleText("acc.field.IsManadatory")+"\">"+WtfGlobal.getLocaleText("acc.field.IsManadatory")+"</div>",
                dataIndex:"isManadatoryField",
                hidden:!this.isForFormFields || this.isOrderCustOrDimFields,
                renderer:function(val){
                    if(val != null && val != undefined){
                        if(val){
                            return 'Yes';
                        }else{
                            return 'No';
                        }
                    }
                }
            },{
                header: WtfGlobal.getLocaleText("acc.field.UserMandatory"),
                dataIndex:"isUserManadatoryField",
                hidden:true,
                renderer:function(val){
                    if(val != null && val != undefined){
                        if(val){
                            return 'Yes';
                        }else{
                            return 'No';
                        }
                    }
                }
            },{
                header: WtfGlobal.getLocaleText("acc.field.Sequence"),//"Sequence",
                width: 50,
                align: 'center',
                dataIndex: 'sequence',
                sortable: false,
                hidden:!this.isOrderCustOrDimFields,
                renderer:function(val, cell, row, rowIndex, colIndex, ds) {
                    var target = row.data.targeted;
                    var storecount=ds.getTotalCount();
                    var str = "";
                    if(target!="" || target=="0"){
                        if(rowIndex<storecount-1)
                            str +=  '<div class=\'pwndBar2 shiftrowdownIcon\'></div>';
                        if(rowIndex > 0)
                            str += ' <div class=\'pwndBar2 shiftrowupIcon\'></div>';
                    }
                    return str;
                }
            },
//            {
//                header:WtfGlobal.getLocaleText("acc.customfield.isMandetory"),
//                dataIndex:"ismandatory"
//            },
            setMandatoryCol,checkColumn,hideFieldFromReport,isReadOnlyCol,{
                 header:WtfGlobal.getLocaleText("acc.product.showinforms.title"),//"Show in forms",
                width:150,
                dataIndex: 'modulestoshowintheirforms',
                hidden:this.moduleid==Wtf.Acc_PRO_ModuleId ? false : true,
                renderer:Wtf.MulticomboBoxRenderer(this.moduleNameConbo),
                editor:this.moduleNameConbo
            }
        ]);
        this.groupingView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: true,
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ?"'+WtfGlobal.getLocaleText("acc.item.plural")+'":"'+WtfGlobal.getLocaleText("acc.item")+'"]})',//"Items" : "Item"]})',
            hideGroupedColumn:true,
            emptyText: "<div class='grid-empty-text'>"+WtfGlobal.getLocaleText("acc.common.norec")+"</div>"
        });
        this.moduleHeaderGrid = new Wtf.grid.EditorGridPanel({
            store:this.moduleHeaderStore,
            region:this.isOrderCustOrDimFields?"south":"center",
//            sm:sm,
            loadMask:true,
            plugins:[setMandatoryCol,checkColumn,hideFieldFromReport,isReadOnlyCol],
            cm:this.moduleHeaderColumn,
            view: this.groupingView
        });
        this.moduleHeaderGrid.on('rowclick', function(grid,rowIndex,e){
            if(e.target.className == "pwndBar2 shiftrowupIcon") {
                moveSelectedRow(this.moduleHeaderGrid,0);
                Wtf.getCmp('savebtn'+this.id).enable();
            }
            if(e.target.className == "pwndBar2 shiftrowdownIcon") {
                moveSelectedRow(this.moduleHeaderGrid,1);
                Wtf.getCmp('savebtn'+this.id).enable();
            }
        },this);
        
         this.moduleHeaderGrid.on('beforeedit', function(e){
            var record = e.record;
            if (record.get('columntype') == 'Custom Field(s)'||record.get('columntype') == 'Dimension Field(s)' &&  (Wtf.Acc_Contract_ModuleId==35 || Wtf.Acc_Lease_Contract==64)) {
                return false;
            } else if (record.get('isReportField') && record.get('columntype') == "Report Item(s)" && Wtf.Acc_PRO_ModuleId == 34) {
                return false;
            } else {
                return true;
            }
        },this);
        
        var customizeMsg = (this.isForFormFields)?WtfGlobal.getLocaleText("acc.field.FormFields"):WtfGlobal.getLocaleText("acc.field.GridColumns.");
        
        this.moduleStore=new Wtf.data.SimpleStore({
            fields:[{name:"id"},{name:"name"}],
            data:[
                [Wtf.Acc_Invoice_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.1")],
                [Wtf.Acc_Vendor_Invoice_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.2")],
                [Wtf.Acc_Debit_Note_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.3")],
                [Wtf.Acc_Credit_Note_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.4")],
                [Wtf.Acc_Make_Payment_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.5")],
                [Wtf.Acc_Receive_Payment_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.6")],
                [Wtf.Acc_GENERAL_LEDGER_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.8")],
                [Wtf.Acc_Customer_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.14")],
                [Wtf.Acc_Vendor_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.15")],
                [Wtf.Acc_Purchase_Order_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.10")],
                [Wtf.Acc_Sales_Order_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.9")],
                [Wtf.Acc_Delivery_Order_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.13")],
                [Wtf.Acc_Goods_Receipt_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.16")],
                [Wtf.Acc_Sales_Return_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.17")],
                [Wtf.Acc_Purchase_Return_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.18")],
                [Wtf.Acc_Customer_Quotation_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.12")],
                [Wtf.Acc_Vendor_Quotation_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.11")],
                [Wtf.Acc_Purchase_Requisition_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.20")],
                [Wtf.Acc_RFQ_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.23")],
                [Wtf.Acc_Lease_Order, WtfGlobal.getLocaleText("acc.dimension.module.21")],
                [Wtf.Acc_Contract_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.22")],
                [Wtf.Acc_Product_Master_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.7")],
                [Wtf.Account_Statement_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.19")],
                [Wtf.SerialWindow_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.54")],
                [Wtf.Inventory_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.55")],
                [Wtf.LEASE_INVOICE_MODULEID, WtfGlobal.getLocaleText("acc.dimension.module.56")],
                [Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.24")],
                [Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.25")],
                [Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.40")],
                [Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.41")],
                [Wtf.Acc_FixedAssets_AssetsGroups_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.28")],
                [Wtf.Acc_FixedAssets_Purchase_Order_ModuleId, WtfGlobal.getLocaleText("acc.field.assetPurchaseOrder")],
                [Wtf.Acc_ConsignmentRequest_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.50")],
                [Wtf.Acc_ConsignmentInvoice_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.52")],
                [Wtf.Acc_ConsignmentDeliveryOrder_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.51")],
                [Wtf.Acc_ConsignmentSalesReturn_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.53")],
                [Wtf.Acc_ConsignmentVendorRequest_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.63")],
                [Wtf.Acc_Consignment_GoodsReceipt_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.58")],
                [Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.57")],
                [Wtf.Acc_ConsignmentPurchaseReturn_ModuleId, WtfGlobal.getLocaleText("acc.dimension.module.59")]
            ]
        });
        this.moduleType= new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName") + '*',
            labelStyle:'width:120px;margin-left: 5px;',
            name:'modules',
            hiddenName:'modules',
            store:this.moduleStore,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            disableKeyFilter:true,
            allowBlank:false,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectaModule")
        });
        this.moduleType.on('select',this.onModuleSelect,this);
        
        this.moduleForm = new Wtf.FormPanel({
            width:'90%',
            method :'POST',
            scope: this,
            border:false,
            waitMsgTarget: true,
            region:"center",
            height : 80,
            labelWidth: 80,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:15px 0 0 60px;',
            layout: 'form',
            items:[
                this.moduleType
            ]
        });
        
        this.add(
            {
                region : 'north',
                height : 100,
                border : false,
                id:'resolveConflictNorth_panel_Overrite',
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : this.isOrderCustOrDimFields?getTopHtml(WtfGlobal.getLocaleText("acc.masterconfig.orderingOfCustomOrDimensions"), WtfGlobal.getLocaleText("acc.masterconfig.orderingOfCustomOrDimensions.tooltip"),"../../images/accounting_image/role-assign.gif")
                                                  :getTopHtml(WtfGlobal.getLocaleText("acc.field.CustomizeView"), WtfGlobal.getLocaleText("acc.field.CustomizetheViewbyHide/Show")+customizeMsg,"../../images/accounting_image/role-assign.gif")
            }
        );
        if(this.isOrderCustOrDimFields){
            this.add(this.moduleForm);
            this.moduleHeaderGrid.height=300;
            this.moduleHeaderGrid.sm=selModel;
        }
        this.add(this.moduleHeaderGrid);
        this.moduleHeaderGrid.on("afteredit", this.afterEdit, this);
         
    },
    
    onModuleSelect : function() {
        this.moduleid = this.moduleType.getValue();
        this.moduleHeaderStore.baseParams['moduleid'] = this.moduleid;
        this.moduleHeaderStore.removeAll();
        this.moduleHeaderStore.load();
    },
    
    afterEdit:function(e){
        var rec = e.record;
        if (e.field=='newheader'){
           rec.data[e.field]=WtfGlobal.HTMLStripper(rec.data.newheader)
        }
    }
});

/*Mobile Field Setup UI*/
Wtf.mobileCustomizeView= function(config){
    this.isForFormFields=(config.isForFormFields!=null && config.isForFormFields!=undefined)?config.isForFormFields:false;
    this.moduleid=config.moduleid;
    this.type=config.type;
    Wtf.mobileCustomizeView.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.mobileCustomizeView,Wtf.Window, {
    resizable: false,
    scope: this,
    autoScroll:true,
    layout: 'border',
    modal:true,
    width:600,
    height: 650,
    iconCls: getButtonIconCls(Wtf.etype.deskera),
    id: 'accmobile_customize_header',
    title:this.type==Wtf.mobilefield.summaryView?WtfGlobal.getLocaleText("acc.field.SummaryView"):(this.type==Wtf.mobilefield.detailView?WtfGlobal.getLocaleText("acc.field.DetailView"):WtfGlobal.getLocaleText("acc.field.EntryView")) ,//'Customize View',
    initComponent: function() {
        Wtf.mobileCustomizeView.superclass.initComponent.call(this);
        this.addEvents("aftersave");
        this.on('beforeclose', this.askToClose,this);
        this.isEdit=false;
        this.addButton({
            text : WtfGlobal.getLocaleText("acc.common.saveBtn"), 
            id:'savebutton'+this.id
            }, function(){
            this.saveFieldConfigs();
        },this);
        this.addButton({text:WtfGlobal.getLocaleText("acc.common.close"), id:'closebtn'+this.id}, function(e){
            this.askToClose(e);
        },this);
    },
    
    askToClose:function(e){
        if(this.isEdit) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.field.Confirmation"),
                msg: WtfGlobal.getLocaleText("acc.field.Maybethedatayoufilledisstillunsaved.")+"<br>"+WtfGlobal.getLocaleText("acc.field.Doyouwanttoclosethepanel"),
                buttons: Wtf.MessageBox.YESNO,
                animEl: 'mb9',
                fn:function(btn){
                    if(btn=="yes")
                        this.closeWin();
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
            return false;
        } else {
            this.closeWin();
        }
    },
    
    closeWin : function () {
        this.un('beforeclose', this.askToClose,this);
        this.close();
    },
    
    saveFieldConfigs : function() {
        var count =1;//to maintain the sequence of fields
        var idString="[";
        var records = this.moduleHeaderGrid.getStore();
        records.each(function(record) {
            idString += "{'fieldid':'"+record.get('fieldid')+"','showfield':"+record.get('showfield')+",'ismandatory':'"+record.get('ismandatory')+"','fieldlabel':'"+record.get('fieldlabel')+"','changedfieldlabel':'"+record.get('changedfieldlabel')+"','seq':"+count+"},";
            count++;
        });
        
        if(idString.length > 1){
            idString=idString.substring(0,idString.length - 1);
        }
        idString+="]";
        
        Wtf.Ajax.requestEx({
            url: "ACCFieldSetup/saveMobileFieldsConfigSettings.do",
            params: {
                moduleid:this.moduleid,
                type:this.type,
                data: idString
            }
        }, this, function(action, response){
            if(action.success){
                this.closeWin();
//                loadCustomFieldColModel(undefined, (this.moduleid+',30').split(','));
//                if (this.moduleid == Wtf.Acc_PRO_ModuleId) {
//                    loadGlobalProductMasterFields()
//                }
//                loadCustomFeildsColumnModelForReports(undefined, (this.moduleid+',30').split(','));  //(Mayur B) load column model for invoice and purchase order 
                Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.field.Confirmation"),
                    msg: action.msg,
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    fn:function(btn){
                    },
                    scope:this,
                    icon: Wtf.MessageBox.INFO
                });
            } else {
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.invoiceList.status"), action.msg);
            }
        },function() {
            });
    },
    
    onRender: function(config) {
       Wtf.mobileCustomizeView.superclass.onRender.call(this, config);
        
        this.moduleHeaderRec = new Wtf.data.Record.create([
            {name:"fieldid"},
            {name:"fieldlabel"},
            {name:"changedfieldlabel"},
            {name:"seq"},
            {name:"showfield"},
            {name:"ismandatory"},
        ]);

        this.moduleHeaderReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.moduleHeaderRec);

     this.moduleHeaderStore = new Wtf.data.Store({
            url:"ACCFieldSetup/getMobileFieldsConfig.do",
            baseParams:{
                moduleIds:this.moduleid,
                type:this.type
            },
            reader:this.moduleHeaderReader,
            sortInfo:{field: 'seq', direction: "ASC"}
        });
        
        this.moduleHeaderStore.load();
        
        var checkColumn= new Wtf.CheckColumnComponent({   
            dataIndex: 'showfield',
            header:WtfGlobal.getLocaleText("acc.field.ShowField"),
            width: 90,
            align:'center',
            id: 'showfield',
            scope:this
        });

        this.moduleHeaderColumn = new Wtf.grid.ColumnModel([
        {
            header: WtfGlobal.getLocaleText("acc.field.FieldLabel"),  //"Field Label",
            dataIndex: 'fieldlabel',
            width: 350,
            //            disabled:true,
            editor: new Wtf.form.ExtendedTextField({
                maxLength:50,
                regex:Wtf.specialChar
            })
        },checkColumn,{
            header: WtfGlobal.getLocaleText("acc.field.Sequence"),//"Sequence",
            width: 100,
            align: 'center',
            dataIndex: 'seq',
            sortable: false,
            renderer:function(val, cell, row, rowIndex, colIndex, ds) {
                var target = row.data.targeted;
                var storecount=ds.getTotalCount();
                var str = "";
                if(target!="" || target=="0"){
                    if(rowIndex<storecount-1)
                        str +=  '<div class=\'pwndBar2 shiftrowdownIcon\'></div>';
                    if(rowIndex > 0)
                        str += ' <div class=\'pwndBar2 shiftrowupIcon\'></div>';
                }
                return str;
            }
        }
        ]);
        
        this.moduleHeaderGrid = new Wtf.grid.EditorGridPanel({
            clicksToEdit: 1,
            stripeRows :true,
            store:this.moduleHeaderStore,
            hirarchyColNumber:0,
            layout:'fit',
            region:"center",
            plugins:[checkColumn],
                        viewConfig: {
                forceFit: true
            },
            cm:this.moduleHeaderColumn
        });
        this.moduleHeaderGrid.on('cellclick', this.handleCellClick, this);
        this.add(this.moduleHeaderGrid);
    }, 
    handleCellClick: function (grid, rowIndex, columnIndex, e) {
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name of column on which you click ringh now
        var formrec = grid.getStore().getAt(rowIndex);
        if((fieldName == 'showfield' && formrec.data.ismandatory)||fieldName == 'fieldlabel'){
            if((fieldName == 'showfield' && formrec.data.ismandatory)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.mandatoryalert")], 2);
            }
            return;
        }else if(fieldName == 'showfield'){
            formrec.set(fieldName, !formrec.data[fieldName]);//to check/uncheck checkbox on user click
            formrec.commit();
        }
        if(e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRow(this.moduleHeaderGrid,0);
            WtfGlobal.highLightRowColor(this.moduleHeaderGrid,formrec,true,0,4);
            Wtf.getCmp('savebutton'+this.id).enable();
        }
        if(e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRow(this.moduleHeaderGrid,1);
            WtfGlobal.highLightRowColor(this.moduleHeaderGrid,formrec,true,0,4);
            Wtf.getCmp('savebutton'+this.id).enable();
        }
    }
    ////    afterEdit:function(e){
//        var rec = e.record;
//        if (e.field=='showfield'){
//            rec.data[e.field]=WtfGlobal.HTMLStripper(rec.data.fieldlabel)
//        }
//    },
});

function moveSelectedRow(grid, direction) {
    var record = grid.getSelectionModel().selection.record;
    if (!record) {
        return;
    }
    var index = grid.getStore().indexOf(record);
    var tempindex;
    var temprecord;
    if (direction == 0) {
        if (index < 0) {
            return;
        }
        tempindex=index-1;
        temprecord=grid.getStore().getAt(tempindex);
    } else {
        if (index >= grid.getStore().getCount()) {
            return;
        }
        tempindex=index+1;
        temprecord=grid.getStore().getAt(tempindex);
    }
    if(record.data.columntype==temprecord.data.columntype){
        grid.getStore().remove(record);
        grid.getStore().insert(tempindex, record);
        grid.getStore().remove(temprecord);
        grid.getStore().insert(index, temprecord);
//        grid.getSelectionModel().selectRow(tempindex, true);
    }
}

function moveSelectedRowFormasterItems(grid, direction, rowindex) {
    var record;
    record = grid.getStore().getAt(rowindex);
    if (!record) {
        return;
    }
    var tempindex;
    var temprecord;
    if (direction == 0) {
        if (rowindex < 0) {
            return;
        }
        tempindex = rowindex - 1;
    } else {
        if (rowindex >= grid.getStore().getCount()) {
            return;
        }
        tempindex = rowindex + 1;
    }
    temprecord=grid.getStore().getAt(tempindex);
    grid.getStore().remove(record);
    grid.getStore().insert(tempindex, record);
    grid.getStore().remove(temprecord);
    grid.getStore().insert(rowindex, temprecord);
}
function moveSelectedRowAccordingtoParentChild(grid, direction, lineItemsFlag) {

    var clicktrecord = grid.getSelectionModel().getSelected();
    var clickindex = grid.getStore().indexOf(clicktrecord);
    var ds = grid.getStore();
    var isleaf = true;
    if (clicktrecord.data.parentid == ""){
        isleaf=false;
    }
    
    var ClicAarrayRecord = [];
    var parentAarrayRecord = [];

    var parentindex = direction == 0 ? getIndexOfPreviousParent(clickindex, ds, isleaf) : getIndexOfNextParent(clickindex, ds, isleaf);
    ClicAarrayRecord = getchildRecordArray(parentindex, clickindex, grid, direction, isleaf);
    parentAarrayRecord = getParentRecordArray(parentindex, clickindex, ds, direction, isleaf);

    direction==0?addRecords(parentindex,ClicAarrayRecord, parentAarrayRecord, ds):addRecords(clickindex,parentAarrayRecord, ClicAarrayRecord, ds);
    
}


function addRecords(index, recordArray1, recordArray2, store) {
    for (var i = 0; i < recordArray1.length; i++) {
        store.remove(recordArray1[i]);
        store.insert(index++, recordArray1[i]);
    }
    for (var i = 0; i < recordArray2.length; i++) {
        store.remove(recordArray2[i]);
        store.insert(index++, recordArray2[i]);
    }
}


function getIndexOfPreviousParent(clickindex, store, leaf) {
    var parentindex = -1;

    if (leaf) {
        for (var i = clickindex - 1; i >= 0; i--) {
            // Find out Previous child whose parent is same
            if (store.data.items[i].data.parentid != store.data.items[clickindex].data.parentid) {
                continue;
            } else {
                parentindex = store.indexOf(store.data.items[i]);
                break;
            }
        }
    } else {
        for (var i = clickindex - 1; i >= 0; i--) {
            // Find out previous parent which is not leaf i.e whose level=0 
            if (store.data.items[i].data.parentid != "") {
                continue;
            } else {
                parentindex = store.indexOf(store.data.items[i]);
                break;
            }
        }
    }
    return parentindex;
}


function getIndexOfNextParent(clickindex, store, leaf) {
    var parentindex = -1;

    if (leaf) {
        for (var i = clickindex + 1; i < store.getCount(); i++) {
            // Find out Next child whose parent is same
            if (store.data.items[i].data.parentid != store.data.items[clickindex].data.parentid) {
                continue;
            } else {
                parentindex = store.indexOf(store.data.items[i]);
                break;
            }
        }
    } else {
        for (var i = clickindex + 1; i < store.getCount(); i++) {
            // Find out Next parent which is not leaf i.e whose level=0 
            if (store.data.items[i].data.parentid != "") {
                continue;
            } else {
                parentindex = store.indexOf(store.data.items[i]);
                break;
            }
        }
    }
    return parentindex;
}


function getParentRecordArray(parentindex, clickindex, store, direction, leaf) {

    var parentArrCount = 1;
    var parentAarrayRecord = [];
    var parentPreviousRecord = store.getAt(parentindex);
    parentAarrayRecord[0] = parentPreviousRecord;
    if (direction == 0) {
        for (var i = parentindex+1; i < clickindex; i++) {
            parentAarrayRecord[parentArrCount++] = store.data.items[i];
        }
    } else {
        if (leaf) {
            for (var i = parentindex + 1; i < store.getCount(); i++) {
                //Check until child which has same parent or child whose level=0
                if (store.data.items[i].data.parentid != store.data.items[parentindex].data.parentid && store.data.items[i].data.parentid != "") {   
                    parentAarrayRecord[parentArrCount++] = store.data.items[i];
                } else {
                    break;
                }
            }
        } else {
            for (var i = parentindex + 1; i < store.getCount(); i++) {
                if (store.data.items[i].data.parentid != "") {                   //check until child whose level=0
                    parentAarrayRecord[parentArrCount++] = store.data.items[i];
                } else {
                    break;
                }
            }
        }

    }
    return parentAarrayRecord;
}

function getchildRecordArray(parentindex, clickindex, grid, direction, leaf){
    var clickArrCount = 1;
    var ClicAarrayRecord = [];
    var store = grid.getStore();
    var clicktrecord = grid.getSelectionModel().getSelected();
     ClicAarrayRecord[0] = clicktrecord;
     
     if(direction==1){
         for (var i = clickindex + 1; i < parentindex; i++) {
                ClicAarrayRecord[clickArrCount++] = store.data.items[i];
            }
     }else{
         if(leaf){
              for (var i = clickindex + 1; i < store.getCount(); i++) {
                  //Check until child which has same parent or child whose level=0
                if (store.data.items[i].data.parentid != store.data.items[clickindex].data.parentid && store.data.items[i].data.parentid != "") {
                    ClicAarrayRecord[clickArrCount++] = store.data.items[i];
                } else {
                    break;
                }
            }
         }else{
             for (var i = clickindex + 1; i < store.getCount(); i++) {
                if (store.data.items[i].data.parentid != "") {                  //check until child whose level=0
                    ClicAarrayRecord[clickArrCount++] = store.data.items[i];
                } else {
                    break;
                }
            }
         }
     }
    
    return ClicAarrayRecord;
}
