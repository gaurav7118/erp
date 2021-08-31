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
Wtf.RepeatedInvoicesReport = function(config){
    this.isCustBill=config.isCustBill;
    this.isCustomer=config.isCustomer;
    this.moduleid = config.moduleid;
    this.isOrder=config.isOrder;
    this.isLeaseFixedAsset =config.isLeaseFixedAsset;
    this.isConsignment =config.isConsignment;
    this.uPermType= config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice;
    this.permType= config.isCustomer?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice;
    if (config.moduleid == Wtf.Acc_Invoice_ModuleId) {
        this.editPermType = this.permType.editinvoice;
        this.deletePermType = this.permType.removeinvoice;
        this.recurringPermType = this.permType.recurringinvoice;
      } else if (config.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
        this.editPermType = this.permType.editvendorinvoice;
        this.deletePermType = this.permType.removevendorinvoice;
        this.recurringPermType = this.permType.recurringvendorinvoice;
    }
    this.gridConfigId="";
    this.GridRec = Wtf.data.Record.create ([
//        {name:'billid'},
//        {name:'billno'},
//        {name:'personid'},
//        {name:'personname'},
//        {name:'interval'},
//        {name:'intervalType'},
//        {name:'startDate', type:'date'},
//        {name:'nextDate', type:'date'},
//        {name:'expireDate'/*, type:'date'*/},
//        {name:'repeateid'}
        {name:'billid'},
        {name:'companyid'},
        {name:'companyname'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'aliasname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
        {name:'termid'},
        {name:'termname'},
        {name:'incash'},
        {name:'taxamount'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'discountval'},
        {name:'crdraccid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'NoOfpost'}, 
        {name:'NoOfRemainpost'},  
        {name:'childCount'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'advanceDate', type:'date'},
        {name:'advancedays'},
        {name:'repeateid'},
        {name:'templateid'},
        {name:'templatename'},
        {name:'status'},
        {name:'movementtype'},
        {name:'withoutinventory',type:'boolean'},
        {name:'fixedAssetInvoice'},
        {name:'fixedAssetLeaseInvoice'},
        {name:'isactivate',type:'boolean'},
        {name:'approver'},
        {name:'ispendingapproval',type:'boolean'},
        {name:'isFromSalesInvoiceRecurringTab',type:'boolean'},//isFromSalesInvoiceRecurringTab - used at the time of editing rule to identify uniquely that tis is recurring invoice tab and add "allow editng of recurred documetns fieldset" on edit form
        {name:'allowEditingRecurredDocuments',type:'boolean'},
        {name:'editedRecurredDocumentsApprover'},
        {name:'isexpenseinv'},
        {name:'billingAddress'},
        {name:'billingCountry'},
        {name:'billingState'},
        {name:'billingPostal'},
        {name:'billingEmail'},
        {name:'billingFax'},
        {name:'billingMobile'},
        {name:'billingPhone'},
        {name:'billingContactPerson'},
        {name:'billingRecipientName'},
        {name:'billingContactPersonNumber'},
        {name:'billingContactPersonDesignation'},
        {name:'billingWebsite'},
        {name:'billingCounty'},
        {name:'billingCity'},
        {name:'billingAddressType'},
        {name:'shippingAddress'},
        {name:'shippingCountry'},
        {name:'shippingState'},
        {name:'shippingCounty'},
        {name:'shippingCity'},
        {name:'shippingEmail'},
        {name:'shippingFax'},
        {name:'shippingMobile'},
        {name:'shippingPhone'},
        {name:'shippingPostal'},
        {name:'shippingContactPersonNumber'},
        {name:'shippingContactPersonDesignation'},
        {name:'shippingWebsite'},
        {name:'shippingContactPerson'},
        {name:'shippingRecipientName'},
        {name:'shippingRoute'},
        {name:'shippingAddressType'},
        {name:'vendcustShippingAddress'},
        {name:'vendcustShippingCountry'},
        {name:'vendcustShippingState'},
        {name:'vendcustShippingCounty'},
        {name:'vendcustShippingCity'},
        {name:'vendcustShippingEmail'},
        {name:'vendcustShippingFax'},
        {name:'vendcustShippingMobile'},
        {name:'vendcustShippingPhone'},
        {name:'vendcustShippingPostal'},
        {name:'vendcustShippingContactPersonNumber'},
        {name:'vendcustShippingContactPersonDesignation'},
        {name:'vendcustShippingWebsite'},
        {name:'vendcustShippingContactPerson'},
        {name:'vendcustShippingRecipientName'},
        {name:'vendcustShippingAddressType'},
        {name:'isCreditable'},
        {name:'lasteditedby'}
    ]);

//    this.StoreUrl = "ACC" + (this.isCustBill?"InvoiceCMN/getBillingInvoices":"InvoiceCMN/getInvoices") + ".do";
    this.StoreUrl = this.isOrder?"ACCSalesOrderCMN/getSalesOrdersMerged.do":(this.isCustomer ? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do");
    if(config.consolidateFlag){
        this.Store = new Wtf.data.GroupingStore({
            url:this.StoreUrl,
            baseParams: {
              getRepeateInvoice: true,
              consolidateFlag:config.consolidateFlag,
              companyids:companyids,
              isLeaseFixedAsset:this.isLeaseFixedAsset,
              isConsignment:this.isConsignment,
              gcurrencyid:gcurrencyid,
              userid:loginid,
              ispendingapproval:config.ispendingapproval,
              activeInactive:Wtf.RECURRINGINVOICES.ACTIVATED                    //sending default value to display all activated recurring invoices
            },
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.GridRec)
        });
    } else {
        this.Store = new Wtf.data.Store({
            url:this.StoreUrl,
            remoteSort:true,
            baseParams: {
              getRepeateInvoice: true,
              isLeaseFixedAsset:this.isLeaseFixedAsset,
              isConsignment:this.isConsignment,
              consolidateFlag:config.consolidateFlag,
              companyids:companyids,
              gcurrencyid:gcurrencyid,
              userid:loginid,
              ispendingapproval:config.ispendingapproval,
              activeInactive:Wtf.RECURRINGINVOICES.ACTIVATED                    //sending default value to display all activated recurring invoices
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.GridRec)
        });
    }
 //    this.Store.on('load',this.hideLoading, this);
//    this.Store.on('loadexception',this.hideLoading, this);
    this.Store.load({params:{start:0,limit:30}});
    var btnArr=[];
    var searchFields = "";
    var person = "";
    if (this.isCustomer) {
        person = "Customer";
    } else {
        person = "Vendor";
    }
    searchFields = ", Memo, " + person + " Name, " + person + " Alias Name, "
            + "Product Name, Product Id, "
            + "Billing Address, Billing City, Billing Postal, Billing State, Billing Email, Billing Country, "
            + "Shipping Address, Shipping City, Shipping Postal, Shipping State, Shipping Email, Shipping Country";
        
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: this.isOrder?WtfGlobal.getLocaleText("acc.SOList.repeatedSO.search"):WtfGlobal.getLocaleText("acc.invList.repeated.search") + searchFields,  //'Quick Search By Invoice No.',
        width: 200,
        field: 'billno',
        Store:this.Store
    }));
    btnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        handler: this.handleResetClick,
        disabled: false
    }));
    this.addRecurringInvoice=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.add"),  // "Add",
        tooltip :WtfGlobal.getLocaleText("acc.invList.repeated.add"),  // "Allows you to add Recurring Invoice.",
        scope: this,
        hidden:this.isLeaseFixedAsset,
        iconCls : getButtonIconCls(Wtf.etype.add),
        handler : this.addRecurringInvoiceHandler
    });
    this.editBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
            tooltip :  this.isOrder?WtfGlobal.getLocaleText("acc.SOList.repeatedSO.edit"):WtfGlobal.getLocaleText("acc.invList.repeated.edit"),  //'Allows you to edit Recurring Invoice.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.edit),
            handler: this.repeateInvoice,
            hidden: config.consolidateFlag || this.isLeaseFixedAsset,
            disabled: true
        });
    if(!WtfGlobal.EnableDisable(this.uPermType, this.recurringPermType) ||this.isLeaseFixedAsset ||this.isOrder){
           btnArr.push(this.addRecurringInvoice);
           btnArr.push(this.editBttn);
        }
    if(!WtfGlobal.EnableDisable(this.uPermType,this.deletePermType) && Wtf.account.companyAccountPref.deleteTransaction){
        btnArr.push(this.deleteBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.rem.7"),  //'Delete',
            tooltip : WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.deletebutton),
            handler: this.deleteRepeateInvoice ,
            hidden: config.consolidateFlag || this.isLeaseFixedAsset,//implemented only in SI and PI & SO. Therefore, there is a module chkeck
            disabled: true
        }))
   }
    btnArr.push("-");
    var toolTipMsg=this.getActivateDeactivateButtonToolTip(this.moduleid);
    btnArr.push(this.activeDeactiveBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.levelsetting.activatelevel"),  //'Activate/Deactivate',
        tooltip : toolTipMsg,
        scope: this,
        //iconCls: getButtonIconCls(Wtf.etype.edit),
        handler: this.activateDeactivateRecurringInvoice,
        hidden: config.consolidateFlag,
        disabled: true
    }));
    btnArr.push("-");
    btnArr.push(this.pendingApprovalBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
        tooltip : WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
        id: 'pendingApprovals' + this.id,
        scope: this, 
        ispendingapproval:true,
        hidden: config.consolidateFlag,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport)
    }));
    
    btnArr.push(this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        hidden:false,
        scope: this,
        handler: function() {
            if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
                this.expandButtonClicked = true;
            }
            expandCollapseGrid(this.expandCollpseButton.getText(), this.expandStore, this.grid.plugins, this);
        }
    }));
    
    var moduleName;
    if(this.isOrder){
        moduleName = WtfGlobal.getLocaleText("acc.wtfTrans.so");
    }else if(this.isCustomer){
        moduleName = WtfGlobal.getLocaleText("acc.field.CustomerInvoice");
    }else{
        moduleName = WtfGlobal.getLocaleText("acc.agedPay.venInv");
    }
    btnArr.push(this.approveInvBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.cc.24"),
        tooltip : WtfGlobal.getLocaleText("acc.field.ApprovePending")+" "+moduleName, 
        id: 'approvepending' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.add),
        disabled :true,
        isApproveBtn:true,
        hidden:config.consolidateFlag,
        handler : this.approveRecurringPendingInvoice
    }));
    this.approveInvBttn.hide();
    /**
     * Below code is written to add filter only in recurring sales invoice report ERM-384
     */
    if (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.LEASE_INVOICE_MODULEID) { //SDP-11582 : Filter added for Lease Invoice Recurring
        var arr = [[Wtf.RECURRINGINVOICES.ALL, 'All'], [Wtf.RECURRINGINVOICES.ACTIVATED, 'Activated'], [Wtf.RECURRINGINVOICES.DEACTIVATED, 'Deactivated']];
        this.delTypeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'typeid', type: 'int'}, 'name'],
            data: arr
        });
        this.typeEditor = new Wtf.form.ComboBox({
            store: this.delTypeStore,
            displayField: 'name',
            valueField: 'typeid',
            mode: 'local',
            value: Wtf.RECURRINGINVOICES.ACTIVATED,
            width: 250,
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true
        });
        btnArr.push('->', this.typeEditor);
        this.typeEditor.on('select', function (combo, record, index) {
            this.Store.load({
                params: {
                    activeInactive: record.data.typeid,
                    start: 0,
                    limit: this.pP.combo.value,
                    ss: this.quickPanelSearch.getValue()
                }
            });
        }, this);
        this.Store.on('beforeload', function () {
            this.Store.baseParams.activeInactive = this.typeEditor.getValue();
        }, this);
    }
    this.pendingApprovalBttn.on('click', function(){
        var panel = null;
        var ispendingapproval = true;
        panel = this.isOrder?Wtf.getCmp("RecurringSOPending"):Wtf.getCmp("RecurringInvoicePending");
        if(panel==null){
            panel = getPendingRecurringInvoiceTab("", undefined,false,ispendingapproval, this.isCustomer, this.isOrder);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();   
    }, this);
    
    if(config.ispendingapproval){    //Hide all buttons in Pending Approval window
        this.approveInvBttn.show();
        this.editBttn.hide();
        this.addRecurringInvoice.hide();
        this.activeDeactiveBttn.hide();
        this.pendingApprovalBttn.hide();
    }
    
    this.expander = new Wtf.grid.RowExpander({});
    //this.sm = new Wtf.grid.RowSelectionModel({singleSelect: true});
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    this.gridView1 = config.consolidateFlag?new Wtf.grid.GroupingView({
            forceFit:true,
            showGroupName: true,
            enableNoGroups:false, // REQUIRED!
            hideGroupedColumn: true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }):{
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        };
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        border:false,
        sm:this.sm,
        layout:'fit',
      // loadMask:true,
        plugins: this.expander,
        viewConfig:this.gridView1,
        columns:[
            this.expander, this.sm,
            {
                header: this.isOrder?WtfGlobal.getLocaleText("acc.field.SalesOrder.No"):WtfGlobal.getLocaleText("acc.invList.repeated.inv"),  //"Invoice No",
                dataIndex:'billno',
                pdfwidth:75,
                sortable:true,
                renderer:config.consolidateFlag? WtfGlobal.deletedRenderer:WtfGlobal.linkDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.field.Company"),  
                dataIndex:'companyname',
                width:20,
                pdfwidth:150,
                hidden:true
            },{
                header:WtfGlobal.getLocaleText("acc.invList.repeated.cus"),  //"Customer",
                pdfwidth:75,
                renderer:WtfGlobal.deletedRenderer,
                dataIndex:'personname',
                sortable:true
            },{
                header:WtfGlobal.getLocaleText("acc.cust.aliasname"),  //"Customer Alias Name",
                pdfwidth:75,
                renderer:WtfGlobal.deletedRenderer,
                dataIndex:'aliasname',
                sortable:true
            },{
                header:WtfGlobal.getLocaleText("acc.invList.repeated.sched"),  //"Schedular Start Date",
                dataIndex:'startDate',
                pdfwidth:80,
                sortable:true,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.recurringDocs.expiryDate"), 
                dataIndex:'expireDate',
                pdfwidth:80,
                sortable:true,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header: this.isOrder?WtfGlobal.getLocaleText("acc.SOList.repeatedSO.invgen"):WtfGlobal.getLocaleText("acc.invList.repeated.invgen"),  //"No. of invoice generated till now",
                dataIndex:'childCount',
                align: "right",
                pdfwidth:80
            },{
                header: this.isOrder?WtfGlobal.getLocaleText("acc.recurringSo.totalCount"):WtfGlobal.getLocaleText("acc.recurringInvoices.totalCount"), 
                dataIndex:'NoOfpost',
                align: "right",
                pdfwidth:80,
                sortable:true
            },{
                header:WtfGlobal.getLocaleText("acc.invList.repeated.interval"),  //"Interval",
                dataIndex:'interval',
                pdfwidth:80,
                renderer: function(a,b,c){
                    var idx = Wtf.intervalTypeStore.find("id", c.data.intervalType);
                    if(idx == -1) {
                        return a+" "+c.data.intervalType;
                    } else {
                        return a+" "+Wtf.intervalTypeStore.getAt(idx).data.name;
                    }
                }
            },{
                header: this.isOrder?WtfGlobal.getLocaleText("acc.SOList.repeatedSO.nextInv"):WtfGlobal.getLocaleText("acc.invList.repeated.nextInv"),  //"Next Invoice Generation Date",
                dataIndex:'nextDate',
                sortable:true,
                pdfwidth:80,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol6"),
                pdfwidth:75,
                renderer:WtfGlobal.booleanRenderer,
                dataIndex:'isactivate'
            },{
                header: WtfGlobal.getLocaleText("acc.invList.repeated.advancedays"),  //"X number of days",
                dataIndex:'advancedays',
                sortable:true,
                pdfwidth:80,
                hidden:this.isOrder
            }],
            tbar:btnArr,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.Store,
                displayInfo: true,
//                displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
            })
        });
    this.items=[this.grid];
    this.grid.on('render',function (){
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.grid.on('render', function () {
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.grid.on('statesave', this.saveMyStateHandler, this);
            }, this);
        }, this);
    },this);

    this.expandRec = Wtf.data.Record.create ([
        {name:'parentInvoiceId'},
        {name:'invoiceId'},
        {name:'invoiceNo'}
    ]);
//    this.expandStoreUrl =this.isOrder ?"ACCSalesOrder/getSalesOrderRepeateDetails.do":(this.isCustomer ? ("ACCInvoice/" + (this.isCustBill?"getBillingInvoiceRepeateDetails":"getInvoiceRepeateDetails") + ".do") :("ACCGoodsReceipt/" + (this.isCustBill?"getRepeateBillingGoodsReceiptDetails":"getRepeateVendorInvoicesDetails") + ".do"))  ;
     this.expandStoreUrl =this.isOrder ?"ACCSalesOrder/getSalesOrderRepeateDetails.do":(this.isCustomer ? ("ACCInvoice/getInvoiceRepeateDetails.do"):("ACCGoodsReceipt/getRepeateVendorInvoicesDetails.do"))  ;
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.expandStore.on("beforeload", function(store){
//        this.expandStoreUrl = this.isOrder ?"ACCSalesOrder/getSalesOrderRepeateDetails.do": (this.isCustomer ? "ACCInvoice/" + (this.withInvMode?"getBillingInvoiceRepeateDetails":"getInvoiceRepeateDetails") + ".do" : ("ACCGoodsReceipt/" + (this.isCustBill?"getRepeateBillingGoodsReceiptDetails":"getRepeateVendorInvoicesDetails") + ".do")) ;
        this.expandStoreUrl =this.isOrder ?"ACCSalesOrder/getSalesOrderRepeateDetails.do":(this.isCustomer ? ("ACCInvoice/getInvoiceRepeateDetails.do"):("ACCGoodsReceipt/getRepeateVendorInvoicesDetails.do"))  ;
        store.proxy.conn.url = this.expandStoreUrl;
    }, this);
    /*
     * Below code checks for the module and on the basis of module the recurring module id is passed to get the gridconfig SDP-10659
     */
    if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {
        WtfGlobal.getGridConfig(this.grid,Wtf.Acc_Recurring_SalesInvoice_ModuleId,false,false);
    }else if(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId){
        WtfGlobal.getGridConfig(this.grid,Wtf.Acc_Recurring_PurchaseInvoice_ModuleId,false,false);
    }else if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId){
        WtfGlobal.getGridConfig(this.grid,Wtf.Acc_Recurring_SalesOrder_ModuleId,false,false);
    }
//if(!this.isOrder)
    this.grid.on('cellclick',this.onRowClick, this);

    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
 //   this.Store.on('load',this.expandRow, this);

    Wtf.apply(this,config);
    Wtf.RepeatedInvoicesReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.RepeatedInvoicesReport, Wtf.Panel,{
    //hideLoading:function(){  Wtf.MessageBox.hide(); },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.withInvMode = record.data.withoutinventory;
        this.expandStore.load({params:{parentid:record.data['billid']}});
    },
    expandRow:function(){
        Wtf.MessageBox.hide();
    },

    fillExpanderBody:function(){
        var disHtml = "";
        var header = "";
         var prevBillid = "";
         var Data = "";
         var FinalData = "";

        if(this.expandStore.getCount()==0){
             var head=this.isOrder?"No any Sales Orders generated till now.":WtfGlobal.getLocaleText("acc.invList.repeated.invgen"); //Generated Invoices
            header = "<span style='color:#15428B;display:block;'>"+ head +"</span>";   // No any invoices generated till now
        } else {
            var head=this.isOrder?"Generated Sales Orders.":WtfGlobal.getLocaleText("acc.invList.repeated.genInv"); //Generated Invoices
//            var jumpcall= this.isOrder ?"": "<a class='jumplink' onclick=\"jumpToTemplate('"+this.sm.getselected().data.billid+"',"+this.withInvMode+")\">";
           var jumpcall="";
            var blkStyle = "display:block;float:left;width:150px;Height:14px;"
            header = "<span class='gridHeader'>"+head+"</span>"; 
            for (var i = 0; i < this.expandStore.getCount(); i++) {
                var invoiceNoStr = "";
                var rec = this.expandStore.getAt(i);
                invoiceNoStr = rec.data.invoiceNo;
                var currentBillid = rec.data['parentInvoiceId'];
                if (prevBillid != currentBillid) {
                    // Check if last record also has same 'billid'.  
                    Data = "";
                }
                prevBillid = currentBillid;

                Data += "<span style=" + blkStyle + ">" + jumpcall + invoiceNoStr + "</a>" + "</span>";

                if (rec.json.isExpander) {
                    FinalData = header + Data;
                } else {
                    var bankHead = this.isOrder ? "No any Sales Orders generated till now." : WtfGlobal.getLocaleText("acc.invList.repeated.invgen"); //Generated Invoices
                    var blankHeader = "<span style='color:#15428B;display:block;'>" + bankHead + "</span>";   // No any invoices generated till now 
                    FinalData = blankHeader;
                }

                var moreIndex = this.grid.getStore().findBy(
                        function(record, id) {
                            if (record.get('billid') === rec.data['parentInvoiceId']) {
                                return true;  // a record with this data exists 
                            }
                            return false;  // there is no record in the store with this data
                        }, this);
                        
                if (moreIndex != -1) {
                    var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                    disHtml = "<div style='width:95%;margin-left:3%;'>" + FinalData + "<br/></div>";
                    body.innerHTML = disHtml;
                    if (this.expandButtonClicked) {
                        this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                        this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
                        invoiceNoStr = "";
                    }
                }
            }
        }
    },

    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.Store.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value,
                    activeInactive:this.typeEditor.getValue()                   //Passing the filter combo box value when reset button is clicked ERM-384
                }
            });
            this.Store.on('load',this.storeloaded,this);
        }
    },
    addRecurringInvoiceHandler:function(){
        var formrec;
        var withoutinventory;
        var isaddRecurringInvoice = true;
        callRepeatedInvoicesWindow(withoutinventory, formrec, isaddRecurringInvoice,this.isOrder,undefined,undefined,undefined,this.isLeaseFixedAsset,this.isCustomer,this.moduleid,this.isConsignment);
    },
    storeloaded:function(store){
     //   Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    enableDisableButtons:function(){
        if(this.sm.getCount()==1){
            this.editBttn.enable();
        } else {
            this.editBttn.disable();
            }
        if(this.sm.getCount()>=1){
            this.approveInvBttn.enable();
            this.activeDeactiveBttn.enable();
            if (this.deleteBttn) {
                this.deleteBttn.enable();
            }
        } else {
            this.approveInvBttn.disable();
            this.activeDeactiveBttn.disable();
            if (this.deleteBttn) {
                this.deleteBttn.disable();
            }
        }
    },
    repeateInvoice:function(){
        var formrec= this.grid.getSelectionModel().getSelected();
        var withoutinventory= this.grid.getSelectionModel().getSelected().data.withoutinventory;
        var isaddRecurringInvoice=false;
        callRepeatedInvoicesWindow(withoutinventory, formrec, isaddRecurringInvoice,this.isOrder,undefined,undefined,undefined,this.isLeaseFixedAsset,this.isCustomer,this.moduleid);
    },
    getActivateDeactivateButtonToolTip:function(moduleID){
        var returnMsg="";
            if (moduleID == Wtf.Acc_Invoice_ModuleId) {
                returnMsg = WtfGlobal.getLocaleText("acc.recurring.salesinvoice.tooltip")
            }else if (moduleID == Wtf.Acc_Vendor_Invoice_ModuleId) {
                returnMsg = WtfGlobal.getLocaleText("acc.recurring.purchaseinvoice.tooltip")
            }else if (moduleID == Wtf.LEASE_INVOICE_MODULEID) {
                returnMsg = WtfGlobal.getLocaleText("acc.recurring.leasesalesinvoice.tooltip")
            }  else {
                returnMsg = WtfGlobal.getLocaleText("acc.recurringJEActivate.tooltip");
            }     
        return returnMsg;
    },
    
    activateDeactivateRecurringInvoice:function(){
        var data=[];
        this.formrec = this.grid.getSelectionModel().getSelections();       
        for(var i=0;i<this.formrec.length;i++) {
            var rec = this.formrec[i];
            var rowObject = new Object();
            rowObject['repeatedid'] = rec.data.repeateid;
            rowObject['isactivate'] = rec.data.isactivate;   //If already activate then make it deactivate & vice versa            
            rowObject['billno'] = rec.data.billno;
            data.push(rowObject);
        }        
        var url="";
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.je.msg3"),function(btn){
            if(btn!="yes") return;
            url=this.isOrder?"ACCSalesOrder/activateDeactivateRecurringSO.do":"ACCInvoiceCMN/activateDeactivateRecurringInvoice.do";
            Wtf.Ajax.requestEx({
                url:url,
                params: {
                    data: JSON.stringify(data)
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        }, this)
    }, 

    deleteRepeateInvoice:function(){
        var data=[];
        var rulesInvoiceNo="";
        this.formrec = this.grid.getSelectionModel().getSelections(); 
        var selectedRecordCount=this.formrec.length;
        var rulesHavingChild=0;
        var rulesHavingNoChild=0;
        for(var i=0;i<selectedRecordCount;i++) {
            var recData = this.formrec[i].data;
            if(recData.childCount>0){
                rulesHavingChild++;
                rulesInvoiceNo+=recData.billno+","
            } else {
                rulesHavingNoChild++;
                var rowObject = new Object();
                rowObject['invoiceid'] = recData.billid;
                rowObject['invoicenumber'] = recData.billno;
                rowObject['repeatedid'] = recData.repeateid;
                data.push(rowObject);
            }
        }
        if(rulesInvoiceNo.length>1){
            rulesInvoiceNo=rulesInvoiceNo.substring(0,rulesInvoiceNo.length-1);
        }
        if(selectedRecordCount==rulesHavingChild){//It means all selected record having child, No need to go for deleted.
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.repeated.allSelectedRecordUsed")],2);
        } else {
            var confmsg="";
            if(rulesHavingChild>0){
                confmsg+=WtfGlobal.getLocaleText("acc.repeated.repeatedrecords")+" "+rulesInvoiceNo+" "+WtfGlobal.getLocaleText("acc.repeated.isbeingusedsocannotbedeleted");
            }else {
                confmsg+=WtfGlobal.getLocaleText("acc.rem.238");
            }
            var url="";
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), confmsg,function(btn){
                if(btn!="yes") return;
                url=this.isOrder?"ACCSalesOrder/deleteRecurringSalesOrder.do":"ACCInvoiceCMN/deleteRecurringInvoiceRule.do";
                Wtf.Ajax.requestEx({
                    url:url,
                    params: {
                        data: JSON.stringify(data),
                        isSalesInvoice:this.isCustomer?true:false
                    }
                },this,this.genDeleteSuccessResponse,this.genDeleteFailureResponse);
            }, this)
        }
    },  
    genDeleteSuccessResponse:function(response){
        if(response.success){
            var msg=WtfGlobal.getLocaleText("acc.repeated.allrecordaredeleted");
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.delete"),msg],0);
            this.Store.load();
        }
    },
   genDeleteFailureResponse:function(response){
        this.enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("Failed"),msg],1);
    },
    
    genSuccessResponse:function(response){
        if(response.success){
            var msg=WtfGlobal.getLocaleText("acc.recurringjeUpdate.approved");
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.update"),msg],0);
            this.Store.load();
        }
    },
    genFailureResponse:function(response){
        this.enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("Failed"),msg],1);
    },
    approveRecurringPendingInvoice:function(){
        var data=[];
        this.formRecord = this.grid.getSelectionModel().getSelections();
        for(var i=0;i<this.formRecord.length;i++) {
            var rec = this.formRecord[i];
            var rowObject = new Object();
            rowObject['repeatedid'] = rec.data.repeateid;
            rowObject['ispendingapproval'] = rec.data.ispendingapproval;   //If already activate then make it deactivate & vice versa            
            data.push(rowObject);
        }
        var moduleName;
        if(this.isOrder){
            moduleName = WtfGlobal.getLocaleText("acc.wtfTrans.so");
        } else if(this.isCustomer){
            moduleName = WtfGlobal.getLocaleText("acc.field.CustomerInvoice");
        }else{
            moduleName = WtfGlobal.getLocaleText("acc.agedPay.venInv");
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected") +" "+ moduleName + "?", function(btn) {
            if (btn == "yes") {
                var url = this.isOrder?"ACCSalesOrder/activateDeactivateRecurringSO.do":"ACCInvoiceCMN/activateDeactivateRecurringInvoice.do";
                Wtf.Ajax.requestEx({
                    url:url,
                    params: {
                        data: JSON.stringify(data)
                    }
                },this,this.genSuccessResp,this.genFailureResp);
            }
        }, this)
    },
    genSuccessResp:function(response){
        if(response.success){
            var msg=WtfGlobal.getLocaleText("acc.recurringjeApproval.approved");
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.Approved"),msg],0);
            this.Store.load();
        }
    },
    genFailureResp:function(response){
        this.enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("Failed"),msg],1);
    },
    onRowClick:function(g,i,j,e){
        e.stopEvent();
//        var el=e.getTarget("a");
//        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="billno"){
            var formrec = this.grid.getSelectionModel().getSelected();
            this.withInvMode = formrec.data.withoutinventory;
            var isExpensiveInv = formrec.data.isexpenseinv;
            var incash=formrec.get("incash");
            if(this.isCustomer){
                
            if(incash &&!this.withInvMode)
                callViewCashReceipt(formrec, 'ViewInvoice');
//            else if(incash)
//                callViewBillingCashReceipt(formrec,null, 'ViewBillingCSInvoice',true);
//            else if(this.withInvMode){
//                callViewBillingInvoice(formrec,null, 'ViewBillingInvoice',false);
//            }
            else{ 
                if(formrec.data.fixedAssetInvoice||formrec.data.fixedAssetLeaseInvoice){
                    callViewFixedAssetInvoice(formrec, formrec.data.billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
                } else if(formrec.data.isConsignment){
                    callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
                }else if(this.isOrder){
                    callViewSalesOrder(true,formrec,formrec.data.billid, false);
                }else{
                    callViewInvoice(formrec, 'ViewCashReceipt');
                }
            }
        } else {
                if (this.isOrder) {

                } else {
                    callViewGoodsReceipt(formrec, 'ViewGoodsReceipt', isExpensiveInv);
                }
            }
        }
    },
    saveMyStateHandler: function(grid,state){
        /*
         * Below code checks for the module and on the basis of module the recurring module id is passed to save the gridconfig SDP-10659
         */
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {
            WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Acc_Recurring_SalesInvoice_ModuleId, this.gridConfigId, false);
        } else if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
            WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Acc_Recurring_PurchaseInvoice_ModuleId, this.gridConfigId, false);
        } else if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Acc_Recurring_SalesOrder_ModuleId, this.gridConfigId, false);
        }
    }
});

function jumpToTemplate(billid, isCustBill){
    Wtf.Ajax.requestEx({
//        url: "ACC" + this.isCustomer ? (isCustBill?"InvoiceCMN/getBillingInvoices":"InvoiceCMN/getInvoices") : (isCustBill?"ACCGoodsReceipt/getBillingGoodsReceipts":"ACCGoodsReceipt/getGoodsReceipts") + ".do",   
         url: "ACC" + this.isCustomer ? ("InvoiceCMN/getInvoices") : ("ACCGoodsReceipt/getGoodsReceipts") + ".do",   
        params: {billid:billid}
        },this,
        function(response){
            var rec = response;
            rec.data = response.data[0];
            var incash=rec.data.incash;
            if(incash &&!isCustBill)
                callViewCashReceipt(rec, 'ViewInvoice');
//            else if(incash)
//                callViewBillingCashReceipt(rec,null, 'ViewBillingCSInvoice',true);
//            else if(isCustBill){
//                callViewBillingInvoice(rec,null, 'ViewBillingInvoice',false);
//            }
            else{
                if(rec.data.fixedAssetInvoice||rec.data.fixedAssetLeaseInvoice){
                    callViewFixedAssetInvoice(rec, rec.data.billid+'Invoice',false,undefined,false,rec.data.fixedAssetInvoice,rec.data.fixedAssetLeaseInvoice);
                } else if(rec.data.isConsignment){
                    callViewConsignmentInvoice(true,rec,rec.data.billid+'ConsignmentInvoice',false,false,true);
                }else{
                    callViewInvoice(rec, 'ViewCashReceipt');
                }
            }
        },
        function(response){

        });
}
/*/////////////////////////////////////   FORM   ////////////////////////////////////////////////////*/

Wtf.RepeateInvoiceForm = function(config){
    this.isOrder=config.isOrder;
    this.isCustomer=config.isCustomer;
    this.moduleid=config.moduleid,
    this.isLeaseFixedAsset =config.isLeaseFixedAsset;
    this.isConsignment =(config.isConsignment==undefined ||config.isConsignment==""?false:config.isConsignment);
    if(config.iscreateSO!=undefined)
    {
        this.iscreateSO=config.iscreateSO;
        this.SOID=config.SOID;
        this.termdays=config.Termdays;
    }
    else 
        this.iscreateSO=false;
    Wtf.apply(this,{
        title:this.isOrder ? WtfGlobal.getLocaleText("acc.repeatedSO.recInv"):WtfGlobal.getLocaleText("acc.repeated.recInv"),  //"Recurring Invoice",
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.repeated.savNclose"),  //'Save and Close',
            scope: this,
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.RepeateInvoiceForm.superclass.constructor.call(this, config);
    this.addEvents({
        'loadInvoiceList': true,
        'update': true,
        'cancel': true
    });
}
Wtf.extend( Wtf.RepeateInvoiceForm, Wtf.Window, {
    defaultCurreny:false,
    onRender: function(config){
        Wtf.RepeateInvoiceForm.superclass.onRender.call(this, config);

this.isEdit = false;
if(this.invoiceRec!=undefined){
    if(this.invoiceRec.data.repeateid){
        this.isEdit = true;
    }
}
this.startDateValue = new Date();
this.startDateValue = new Date(this.startDateValue.getFullYear(),this.startDateValue.getMonth(),this.startDateValue.getDate()+1);
//this.startDateValue.setDate(this.startDateValue.getDate()+1);
this.nextDateValue = this.startDateValue;
if(this.isEdit){
    this.nextDateValue = this.invoiceRec.data.nextDate;
}
this.creditTermDays = 0;
if(this.invoiceRec!=undefined){
    if(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId){
        this.creditTermDays = this.invoiceRec.data.termdays?this.invoiceRec.data.termdays:0;
    }else{
        this.creditTermDays = this.invoiceRec.data.creditDays?this.invoiceRec.data.creditDays:0;
    }
}
if(this.termdays)
    {
      this.creditTermDays=this.termdays;  
    }
this.dueDateValue = this.calculateDueDate();

this.InvoiceRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'billno'},
        {name:'creditDays'},
        {name:'repeateid'},
        {name:'nextDate', type:'date'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'expireDate', type:'date'},
        {name:'withoutinventory',type:'boolean'},
        {name:'NoOfpost'},
        {name:'advanceDate', type:'date'},
        {name:'advancedays'},
        {name:'memo'}
    ]);

this.InvoiceStoreUrl = this.isOrder?"ACCSalesOrderCMN/getSalesOrdersMerged.do":(this.isCustomer)?"ACCInvoiceCMN/getInvoicesMerged.do":"ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
this.InvoiceStore = new Wtf.data.Store({
    url:this.InvoiceStoreUrl,
    baseParams: {
        cashonly:false,
        consolidateFlag:false,
        creditonly:true,
        isOutstanding:false,
        isfavourite:false,
        ispendingpayment:false,        
        nondeleted:true,
        report:true,
        isLeaseFixedAsset:this.isLeaseFixedAsset,
        isConsignment:this.isConsignment,
        companyids:companyids,
        gcurrencyid:gcurrencyid,
        userid:loginid
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:'count'
    },this.InvoiceRec)
});
if(this.isaddRecurringInvoice) {
    this.InvoiceStore.load();
}
  this.MemoCM= new Wtf.grid.ColumnModel([{
            header:this.isOrder?WtfGlobal.getLocaleText("acc.repeated.MemoForso"):WtfGlobal.getLocaleText("acc.repeated.MemoGridcol1"),
            dataIndex:'no',
            width:120
          
        },{
            header:WtfGlobal.getLocaleText("acc.repeated.Gridcol3"),
            dataIndex:"memo",
            width:310,
            editor:new Wtf.form.TextField({
                name:'memo'
            
            })
        }]);
       
       
        this.accRec = new Wtf.data.Record.create([
        {
            name: 'no'
        },{
            name: 'memo'
        }
        ]);
        this.Memostore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec),
           url : 'ACCJournal/getJERepeateMemoDetails.do'
        });
        if(this.invoiceRec != undefined){
            if(this.invoiceRec.data.repeateid){
                 this.Memostore.load({
                     params:{
                             memofor:this.isOrder ?"RepeatedSOID":"RepeatedInvoiceID", 
                             repeateid:this.invoiceRec.data.repeateid
                     }
             });
            }
         }
        
        this.grid1 = new Wtf.grid.EditorGridPanel({
            clicksToEdit:1,
            height:130,
            width:430,
            store: this.Memostore,
            cm: this.MemoCM,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        var hideAllowEditingRecurredDocumentsFieldSet = true;
        if (this.moduleid != undefined && this.moduleid == Wtf.Acc_Invoice_ModuleId) {
            hideAllowEditingRecurredDocumentsFieldSet = false;
        }
        /*
         * 
         * isFromSalesInvoiceRecurringTab - this flag is used at the time of editing recurring rule on rule edit window  moduleid is not available/present so in this case 
         * I have brought this flag with invoice to identify that this rule is of sales invoice and show this.AllowEditingRecurredDocumentsFieldSet fieldset on rule edit window.
         */
        var isFromSalesInvoiceRecurringTab;
        if(this.invoiceRec){
            isFromSalesInvoiceRecurringTab=this.invoiceRec.data.isFromSalesInvoiceRecurringTab != undefined ? this.invoiceRec.data.isFromSalesInvoiceRecurringTab : false;
        }
        if (this.isEdit && (this.invoiceRec && this.invoiceRec.data!=undefined) && isFromSalesInvoiceRecurringTab != undefined && isFromSalesInvoiceRecurringTab) {
            hideAllowEditingRecurredDocumentsFieldSet = false;
            
        }
        
        this.approverForModifiedRecurrredInvoices = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.repatedInvoice.setapprover.title"), //Approver
            store: Wtf.userds,
            name: "modifiedrecurredinvoiceapprover",
            displayField: 'fname',
            valueField: 'id',
            forceSelection: true,
            selectOnFocus: true,
            triggerAction: 'all',
            editable: false,
            mode: 'local',
            width: 193,
            //autoWidth:true,
            allowBlank: true
        });


        this.AllowEditingRecurredDocumentsFieldSet = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.repatedInvoice.alloweditingrecurreddocuments.title"),
            hidden: hideAllowEditingRecurredDocumentsFieldSet,
            checkboxToggle: true,
            autoHeight: true,
            border: false,
            disabledClass: "newtripcmbss",
            checkboxName: 'alloweditingrecurreddocuments',
            style: 'margin-left:0px;margin-right: 3px;',
            collapsed: true,
            items: [this.approverForModifiedRecurrredInvoices]
        });
        
        this.AllowEditingRecurredDocumentsFieldSet.on('collapse',this.enableDisableCombo.createDelegate(this,[false]),this);
        
       this.repeateForm = new Wtf.form.FormPanel({
            border: false,
            labelWidth:230,
            autoScroll:true,
            items : [
                this.repeateId = new Wtf.form.Hidden({
                    hidden:true,
                    name:"repeateid"
                }),
                this.invoiceList = new Wtf.form.ComboBox({
                    fieldLabel:(this.isOrder ? WtfGlobal.getLocaleText("acc.MailWin.somsg7"):WtfGlobal.getLocaleText("acc.repeated.invNo")) +"*",  //"Invoice Number *",
                    store: this.InvoiceStore,
                    displayField:'billno',
                    valueField:'billid',
                    mode: 'local',
                    width: 200,
                    hidden: !this.isaddRecurringInvoice,
                    hideLabel: !this.isaddRecurringInvoice,
                    disabled :!this.isaddRecurringInvoice,
                    triggerAction: 'all',
                    typeAhead:true,
                    selectOnFocus:true,
                    allowBlank:false,
                    forceSelection : true
                }),
                this.nextDate = new Wtf.form.DateField({
                    fieldLabel:this.isOrder ? WtfGlobal.getLocaleText("acc.repeatedSO.nextDate"):WtfGlobal.getLocaleText("acc.repeated.nextDate") +"*",  //"Next Invoice Generation Date*",
                    width:200,
                    name:'startDate',
                    readOnly:true,
//                    minValue : this.isEdit?null:new Date(),
                    value: this.nextDateValue,
                    format: "Y-m-d"
                }),
                this.dueDate = new Wtf.form.TextField({
                    fieldLabel:this.isOrder ? WtfGlobal.getLocaleText("acc.repeatedSO.nextDueDate"):WtfGlobal.getLocaleText("acc.repeated.nextDueDate"),  //"Next Invoice Due Date",
                    width:200,
                    maxLength:50,
                    allowBlank: false,
                    value: this.dueDateValue,
                    disabled: true,
                    readOnly: true,
                    name:"dueDate"
                }),
                this.intervalPanel = new Wtf.Panel({
                    layout: "column",
                    border: false,
                    items:[
                        new Wtf.Panel({
                            columnWidth: 0.54,
                            layout: "form",
                            border: false,
                            anchor:'100%',
                            items : this.interval = new Wtf.form.NumberField({
                                    fieldLabel:this.isOrder ? WtfGlobal.getLocaleText("acc.repeatedSO.repInv"):WtfGlobal.getLocaleText("acc.repeated.repInv") +"*",  //"Repeate this invoice every*",
                                    width: 50,
                                    allowBlank: false,
                                    minValue: 1,
                                    maxValue: 999,
                                    allowNegative: false,
                                    maxLength: 50,
                                    name: "interval"
                                })
                        }),
                        new Wtf.Panel({
                            columnWidth: 0.3,
                            layout: "form",
                            border: false,
                            anchor:'100%',
                            items : this.intervalType = new Wtf.form.ComboBox({
                                        store: Wtf.intervalTypeStore,
                                        hiddenName:'intervalType',
                                        displayField:'name',
                                        valueField:'id',
                                        mode: 'local',
                                        value: "day",
                                        triggerAction: 'all',
                                        typeAhead:true,
                                        hideLabel: true,
                                        labelWidth: 5,
                                        width: 128,
                                        selectOnFocus:true
                                    })
                        })
                        
                    ]
                }),                
                this.NoOfJEPanel = new Wtf.Panel({
                    layout: "column",
                    border: false,
                    items:[
                        new Wtf.Panel({
                            columnWidth: 0.54,
                            layout: "form",
                            border: false,
                            anchor:'100%',
                            items : this.NoOfpost = new Wtf.form.NumberField({
                                    fieldLabel:this.isOrder ?WtfGlobal.getLocaleText("acc.repeatedSO.SOPost"):WtfGlobal.getLocaleText("acc.repeated.invPost"),
                                    width: 50,
                                    allowBlank: false,
                                    minValue: 1,
                                    maxValue: 999,
                                    allowNegative: false,
                                    maxLength: 3, //ERP-10640 : If max value is 999 then max length should be 3 else end user ll cross the max value limit which ll lead to form disable problem
                                    name:"NoOfpost"
                                })
                        }),
                        new Wtf.Panel({
                            columnWidth: 0.3,
                            layout: "form",
                            border: false,
                            anchor:'100%',
                            items : this.expireDate = new Wtf.form.DateField({
                                    name:'expireDate',
                                    format: "Y-m-d",
                                    typeAhead:true,
                                    hideLabel: true,
                                    labelWidth: 5,
                                    width: 128
                                })
                        })
                    ]
                }),
                
                this.advancedaysPanel = new Wtf.Panel({
                    layout: "column",
                    border: false,
                    hidden: !(this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId),
                    items: [
                        new Wtf.Panel({
                            columnWidth: 1,
                            layout: "column",
                            border: false,
                            width: 350,
                            items: [
                                {
                                    columnWidth: '.52',
                                    border: false,
                                    layout: 'form',
                                    items: [
                                        this.advancedays = new Wtf.form.NumberField({
                                            fieldLabel: this.isOrder ? WtfGlobal.getLocaleText("acc.repeated.repInv.generatesalesorder") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.repeatedSO.helpmessage")) : WtfGlobal.getLocaleText("acc.common.generateInvoice") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.repeated.repInv.helpmessage")),
                                            width: 50,
                                            allowBlank: true,
                                            minValue: 0,
                                            maxValue: 999,
                                            allowNegative: false,
                                            maxLength: 3,
                                            value: 0,
                                            name: "advancedays",
                                            labelWidth: 1
                                        })]
                                }, {
                                    columnWidth: '.47',
                                    border: false,
                                    layout: 'form',
                                    items: [
                                        {
                                            xtype: 'textfield',
                                            value: ' day(s) before invoice date',
                                            readOnly: true,
                                            cls: 'clearStyle',
                                            hideLabel: true,
                                            width: 150
                                        }]
                                }]
                        })]
                }),
                this.advanceDatePanel = new Wtf.Panel({
                    columnWidth: 1,
                    layout: "form",
                    width: 350,
                    border: false,
                    hidden: !(this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId),
                    items: this.advanceDate = new Wtf.form.DateField({
                        name: 'advanceDate',
                        fieldLabel: WtfGlobal.getLocaleText("acc.repeated.repInv.advancegenerationdate"),
                        format: "Y-m-d",
                        width: 90,
                        disabled: true,
                        value: this.nextDateValue
                    })
                }),                
                this.notifyRecurring = new Wtf.form.FieldSet({
                title:WtfGlobal.getLocaleText("Notification Mode"),
                id:this.id+'notifyme',
                //bodyStyle:'padding:5px',
                autoHeight : true,
                //autoWidth : true,
                hidden:(this.moduleid!=Wtf.Acc_Invoice_ModuleId && this.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId && this.moduleid!=Wtf.Acc_Sales_Order_ModuleId)?true:false,
                width: 555,
                items:[
                    this.notify0 = new Wtf.form.Radio({
                    hideLabel:true,
                    checked: (this.invoiceRec!=undefined && this.invoiceRec.data.ispendingapproval!=undefined)?(this.invoiceRec!=undefined && this.invoiceRec.data.ispendingapproval?false:true):true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.recurring.autoentry"),  //Auto Entry
                    name: 'notifyme',
                    inputValue: 0
                }),
                this.notify1 = new Wtf.form.Radio({       
                    hideLabel:true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.field.Notifyme"),  //Remind me for confirmation
                    name: 'notifyme',
                    inputValue: 1
                }),
                this.approval=new Wtf.form.ComboBox({
                    fieldLabel:WtfGlobal.getLocaleText("acc.field.Approvers"), //Approver(s) 
                    store:Wtf.userds,
                    name:"approver",
                    displayField:'fname',
                    valueField:'userid',
                    forceSelection: true,
                    selectOnFocus:true,
                    triggerAction: 'all',
                    editable:false,
                    mode: 'local',
                    width: 193,
                    //autoWidth:true,
                    allowBlank:true
                })
                ]
            }),
            this.AllowEditingRecurredDocumentsFieldSet,
                this.GridPanel = new Wtf.Panel({
                    layout: "fit",
                    border: true,
                    //height:100, //ERP-10640
                    width: 555,
                    //autoScroll:true,  //ERP-10072
                    items:[this.grid1]
                })
            ]
        });
//        if(this.moduleid!=Wtf.Acc_Invoice_ModuleId && this.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId){
//            this.notifyRecurring.hide();
//        }
        this.approval.disable();
        this.notify0.on('change',this.onButtonCheck,this);
        this.notify1.on('change',this.onButtonCheck,this); 
       this.on('show',this.togglesetRecurringEdit,this);//after window is visible toggle recurring fieldset in edit case

       this.interval.on("change",function(df, nvalue, ovalue){
                this.endDateValue = this.calculateEndDate();
                this.expireDate.setValue(this.endDateValue);
                this.setMemoDetails();
            },this);
            this.nextDate.on("change",function(df, nvalue, ovalue){
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.endDateValue = this.calculateEndDate();
                this.expireDate.setValue(this.endDateValue);                
                this.setMemoDetails();
                if (this.advancedays != undefined && this.advancedays && this.advancedays.getValue() > 0) {                    
                    this.advanceDateValue=this.addDays(nvalue,this.advancedays.getValue());
                    this.advanceDate.setValue(this.advanceDateValue);  
                }
                else {
                    this.advanceDateValue=this.addDays(nvalue,0);
                    this.advanceDate.setValue(this.advanceDateValue);  
                }
            },this);
            this.NoOfpost.on("change",function(){
                this.endDateValue = this.calculateEndDate();
                this.expireDate.setValue(this.endDateValue);
                if( this.Memostore.getCount() < this.NoOfpost.getValue()){
                    for(var i=this.Memostore.getCount()+1 ;i <=this.NoOfpost.getValue(); i++){    
                        this.addGridRec(i);
                    }
                }else{
                    var reccount=this.Memostore.getCount()-1;
                    for(var j=this.NoOfpost.getValue() ;j <=reccount; j++){   
                        var rec=this.Memostore.getAt(this.NoOfpost.getValue());
                        this.Memostore.remove(rec);
                    }
                }
                this.setMemoDetails();
            },this);
            this.advancedays.on("change",function(df, nvalue, ovalue){
                this.advanceDateValue = this.addDays(this.nextDate.getValue(),nvalue);
                this.advanceDate.setValue(this.advanceDateValue);         
                if (this.advanceDateValue <= new Date()) {         
                 this.advanceDate.markInvalid(WtfGlobal.getLocaleText("acc.repeated.adverrormsg"));   //'Advance Date' should be greater than 'Todays date'                 
                }
            },this);
            this.intervalType.on("select",function(){
                this.endDateValue = this.calculateEndDate();
                this.expireDate.setValue(this.endDateValue);
                this.setMemoDetails();
            },this);
                  
        this.invoiceList.on("select",function(){
            var idx = this.InvoiceStore.find('billid',this.invoiceList.getValue());
            if(idx!=-1){
                this.record = this.InvoiceStore.getAt(idx);
            }
            if(this.record.data.repeateid){ //Update
        //          this.repeateForm.getForm().loadRecord(this.invoiceRec);
                this.nextDateValue = this.record.data.nextDate;
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.repeateId.setValue(this.record.data.repeateid);
                this.nextDate.setValue(this.nextDateValue);
                this.dueDate.setValue(this.dueDateValue);
                this.interval.setValue(this.record.data.interval);
                this.intervalType.setValue(this.record.data.intervalType);
                //this.expireDate.setValue(this.record.data.expireDate);
                this.NoOfpost.setValue(this.record.data.NoOfpost);
                this.expDateValue = this.calculateEndDate();
                this.expireDate.setValue(this.expDateValue);
                this.advancedays.setValue(this.record.data.advancedays);
                this.advanceDate.setValue(this.record.data.advanceDate);
                this.record.data.ispendingapproval?this.notify1.setValue(true):this.notify0.setValue(true);
                if(this.record.data.ispendingapproval){
                    this.approval.enable();
                    var pos=Wtf.userds.find("fname",this.record.data.approver)
                    if(pos!=-1)
                        this.approval.setValue(Wtf.userds.getAt(pos).data.userid);
                }
                if( this.Memostore.getCount() < this.NoOfpost.getValue()){
                    for(var i=this.Memostore.getCount()+1 ;i <=this.NoOfpost.getValue(); i++){    
                        this.addGridRec(i);
                    }
                }else{
                    var reccount=this.Memostore.getCount()-1;
                    for(var j=this.NoOfpost.getValue() ;j <=reccount; j++){   
                        var rec=this.Memostore.getAt(this.NoOfpost.getValue());
                        this.Memostore.remove(rec);
                    }
                }//else
                this.Memostore.load({
                    params:{
                        memofor:this.isOrder ?"RepeatedSOID":"RepeatedInvoiceID", 
                        repeateid:this.record.data.repeateid
                    }
                });
            }
            this.setMemoDetails();
        },this);

        if(this.invoiceRec!=undefined){
            if(this.invoiceRec.data.repeateid){ //Update
        //          this.repeateForm.getForm().loadRecord(this.invoiceRec);
                this.nextDateValue = this.invoiceRec.data.nextDate;
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.repeateId.setValue(this.invoiceRec.data.repeateid);
                this.nextDate.setValue(this.nextDateValue);
                this.dueDate.setValue(this.dueDateValue);
                this.interval.setValue(this.invoiceRec.data.interval);
                this.intervalType.setValue(this.invoiceRec.data.intervalType);
                //this.expireDate.setValue(this.invoiceRec.data.expireDate);
                this.NoOfpost.setValue(this.invoiceRec.data.NoOfpost);
                this.expDateValue = this.calculateEndDate();
                this.expireDate.setValue(this.expDateValue);
                this.advancedays.setValue(this.invoiceRec.data.advancedays);
                this.advanceDate.setValue(this.invoiceRec.data.advanceDate);
                this.invoiceRec.data.ispendingapproval?this.notify1.setValue(true):this.notify0.setValue(true);
                if(this.invoiceRec.data.ispendingapproval){
                    this.approval.enable();
                    var pos=Wtf.userds.find("fname",this.invoiceRec.data.approver)
                    if(pos!=-1)
                        this.approval.setValue(Wtf.userds.getAt(pos).data.userid);
                }
                if( this.Memostore.getCount() < this.NoOfpost.getValue()){
                    for(var i=this.Memostore.getCount()+1 ;i <=this.NoOfpost.getValue(); i++){    
                        this.addGridRec(i);
                    }
                }else{
                    var reccount=this.Memostore.getCount()-1;
                    for(var j=this.NoOfpost.getValue() ;j <=reccount; j++){   
                        var rec=this.Memostore.getAt(this.NoOfpost.getValue());
                        this.Memostore.remove(rec);
                    }
                }//else
                this.Memostore.load({
                    params:{
                        memofor:this.isOrder ?"RepeatedSOID":"RepeatedInvoiceID", 
                        repeateid:this.invoiceRec.data.repeateid
                    }
                });
                this.setMemoDetails();
//                this.Memostore.load();
            } else {    //Perform Recurring from Lease Sales Invoice Entry Form
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.nextDate.setValue(this.nextDateValue);
                this.dueDate.setValue(this.dueDateValue);
                this.interval.setValue((this.invoiceRec.data.interval=="" || this.invoiceRec.data.interval==undefined)?1:this.invoiceRec.data.interval);
                this.intervalType.setValue(this.invoiceRec.data.intervalType =="" || this.invoiceRec.data.intervalType ==undefined ?"day":this.invoiceRec.data.intervalType);
                //this.expireDate.setValue(this.invoiceRec.data.expireDate);
                this.NoOfpost.setValue((this.invoiceRec.data.NoOfpost)==""?1:this.invoiceRec.data.NoOfpost);
                this.expDateValue = this.calculateEndDate();
                this.expireDate.setValue(this.expDateValue);
                this.advancedays.setValue(0);
                this.advanceDate.setValue(this.addDays(this.nextDate.getValue(),(-this.advancedays.getValue())));
                this.invoiceRec.data.ispendingapproval?this.notify1.setValue(true):this.notify0.setValue(true);
                if(this.invoiceRec.data.ispendingapproval){
                    this.approval.enable();
                    var pos=Wtf.userds.find("fname",this.invoiceRec.data.approver)
                    if(pos!=-1)
                        this.approval.setValue(Wtf.userds.getAt(pos).data.userid);
                }
                if( this.Memostore.getCount() < this.NoOfpost.getValue()){
                    for(var i=this.Memostore.getCount()+1 ;i <=this.NoOfpost.getValue(); i++){    
                        this.addGridRec(i);
                    }
                }else{
                    var reccount=this.Memostore.getCount()-1;
                    for(var j=this.NoOfpost.getValue() ;j <=reccount; j++){   
                        var rec=this.Memostore.getAt(this.NoOfpost.getValue());
                        this.Memostore.remove(rec);
                    }
                }//else
                this.Memostore.load();
                this.setMemoDetails();
            }            
        }
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:this.isOrder ? getTopHtml(WtfGlobal.getLocaleText("acc.repeatedSO.recInv"),WtfGlobal.getLocaleText("acc.repeatedSO.recInvInfo"),"../../images/accounting_image/Chart-of-Accounts.gif", false):getTopHtml(WtfGlobal.getLocaleText("acc.repeated.recInv"),WtfGlobal.getLocaleText("acc.repeated.recInvInfo"),"../../images/accounting_image/Chart-of-Accounts.gif", false)
        },new Wtf.Panel({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            bodyStyle: 'border-bottom:1px solid #bfbfbf;padding:15px 0px 15px 15px',
            items: this.repeateForm
        }));
    },
    togglesetRecurringEdit :function(){
          if (this.isEdit && this.invoiceRec.data && this.invoiceRec.data.isFromSalesInvoiceRecurringTab != undefined && this.invoiceRec.data.isFromSalesInvoiceRecurringTab) {
            if (this.invoiceRec.data.allowEditingRecurredDocuments) {
                this.AllowEditingRecurredDocumentsFieldSet.toggleCollapse();
                this.approverForModifiedRecurrredInvoices.setValue(this.invoiceRec.data.editedRecurredDocumentsApprover);
            }
        }
    },
     enableDisableCombo:function(disabled){        
        this.approverForModifiedRecurrredInvoices.reset();
        this.approverForModifiedRecurrredInvoices.setValue("");
    },
    onButtonCheck:function(radio, value){
        if(radio.inputValue==0){
            this.approval.disable();
        } else if(radio.inputValue==1){ 
            Wtf.userds.load();
            this.approval.enable();
        }        
    }, 
    calculateDueDate: function() {
        var stdate = this.nextDate==undefined ? new Date(this.startDateValue): new Date(this.nextDate.getValue());
        stdate.setDate(stdate.getDate()+this.creditTermDays);
        return stdate.format("Y-m-d");
    },
    calculateEndDate: function() {
        var noOfdays=1;
        var interval=1;
        /*ERP-10675 : Here, we need not to worry about timezone. Because we are simply calculating next date based on provided
         days OR month OR year. The problem is geeting due to timezone. To avoid this, we ll subtract/add browser timeoffset
         from end date. Here we are calculating enddate in long. */
        
        var currentDate = new Date();
        var browserTimezoneOffset = currentDate.getTimezoneOffset();
        var enddate = this.nextDate==undefined ? new Date(this.startDateValue): new Date(this.nextDate.getValue());
        enddate.setDate(enddate.getDate()+noOfdays);
        //ERP-10675 : Subtracted 1 Invoice from Total No.of Invoices, because while calculating next date we are not considering next generation date.
        if(this.intervalType.getValue()=="month" && this.NoOfpost.getValue()!="" && this.interval.getValue()!=""){            
            var monthsadd = (this.NoOfpost.getValue()-1) * this.interval.getValue();    //ERP-10675
            enddate = new Date((enddate.setMonth(enddate.getMonth()+monthsadd))+browserTimezoneOffset); //ERP-9539
        }else{
            if(this.intervalType.getValue()=="week" && this.NoOfpost.getValue()!="" && this.interval.getValue()!="")
                    noOfdays = (this.NoOfpost.getValue()-1) * 7 * this.interval.getValue();   //ERP-10675
            if(this.intervalType.getValue()=="day" && this.NoOfpost.getValue()!="" && this.interval.getValue()!="")
                noOfdays = (this.NoOfpost.getValue()-1) * this.interval.getValue();
              
            enddate = new Date((enddate.setDate(enddate.getDate()+noOfdays))+browserTimezoneOffset);    //ERP-9539
        }
        return enddate.format("Y-m-d");
    },
    addGridRec:function(recno){ 
        var rec= this.accRec;
        rec = new rec({});
        rec.beginEdit();
        var fields=this.Memostore.fields;
        rec.set(fields.get('memo').name, "");
        rec.set(fields.get('no').name,recno);
        rec.endEdit();
        rec.commit();
        this.Memostore.add(rec);
    },
    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
    },
   getRecords:function(){
        var data=[];
        for(var i=0;i<this.Memostore.getCount();i++){
            var rec=this.Memostore.getAt(i);
            data.push('{no:"'+rec.data['no']+'",memo:"'+rec.data['memo']+'"}');
        }
        return data;
    },

    saveData:function(){

        var valid = this.repeateForm.getForm().isValid();
        var minNextDate = this.startDateValue<this.nextDateValue ? this.startDateValue : this.nextDateValue;
        if(this.nextDate.getValue()<minNextDate){
            var minDate = new Date(minNextDate);
            minDate.setDate(minDate.getDate()-1);
            this.nextDate.markInvalid(WtfGlobal.getLocaleText("acc.repeated.msg")+minDate.format("Y-m-d"));    //"Please select 'Next date' greater than "
            valid = false;
        }

        if(this.expireDate.getValue()!="" && this.expireDate.getValue()<this.nextDate.getValue()){
            this.expireDate.markInvalid(WtfGlobal.getLocaleText("acc.repeated.msg1"));    // "'End date' should be greater than 'Next date'"
            valid = false;
        }
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
            if (this.advanceDate.getValue() > this.nextDate.getValue()) {
                this.advanceDate.markInvalid(WtfGlobal.getLocaleText("acc.repeated.advmsg"));   //'Advance Date' should be greater than 'Next date'
                valid = false;
            }
            if (this.advanceDate.getValue() <= new Date()) {
                this.advanceDate.markInvalid(WtfGlobal.getLocaleText("acc.repeated.adverrormsg"));   //'Advance Date' should be greater than 'Todays date'  
                valid = false;
            }
        }
        if(!valid){
            WtfComMsgBox(2,2);
            return;
        }
        var rec=[];
        rec = this.repeateForm.getForm().getValues();
        
        
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId || (this.isEdit && this.invoiceRec.data != undefined && this.invoiceRec.data.isFromSalesInvoiceRecurringTab != undefined && this.invoiceRec.data.isFromSalesInvoiceRecurringTab)) {
            /*
             * In add case of recurring rule moduleid is available but in edit case moduleid is not available thats why i used or in in if block.
             * so in edit case of recurring rule "this.invoiceRec.data.isFromSalesInvoiceRecurringTab" flag is used to identify that this rule is belongs to invoice module.
             */
            rec.alloweditingrecurreddocuments = (rec.alloweditingrecurreddocuments == "on" ? true : false);
            var approverofrecurredInvoice = this.approverForModifiedRecurrredInvoices.getValue();
            if (rec.alloweditingrecurreddocuments && (approverofrecurredInvoice == undefined || approverofrecurredInvoice == '')) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.editedrecurring.approver.message.title")], 2);
                return;
            } else if(rec.alloweditingrecurreddocuments){
                rec.approverofediteddocument = this.approverForModifiedRecurrredInvoices.getValue();
            }
        }
        
        if(this.isaddRecurringInvoice) {
            rec.invoiceid = this.invoiceList.getValue();
            rec.isCustomer=this.isCustomer;
            rec.isCustBill = this.record.data.withoutinventory;
            rec.billno = this.record.data.billno;
        }else {
            rec.isCustBill = this.iscreateSO ? false : this.isCustBill;
            rec.isCustomer=this.isCustomer;
            rec.invoiceid = this.iscreateSO ? this.SOID:this.invoiceRec.data.billid; 
            rec.billno=this.invoiceRec.data.billno;
        }
        rec.advanceDate = WtfGlobal.convertToGenericDate(this.advanceDate.getValue());
//        alert(rec);return;
        rec.detail="["+this.getRecords().join(",")+"]";
        rec.isEdit=this.isEdit;
        Wtf.Ajax.requestEx({
            url:this.isOrder ?"ACCSalesOrder/saveRepeateSalesOrderInfo.do":"ACCInvoice/saveRepeateInvoiceInfo.do",
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    },

    genSuccessResponse:function(response){
        var superThis = this;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1,"","",function(btn){
            if(btn=="ok"){
                superThis.fireEvent('loadInvoiceList',superThis);
            }
            if(Wtf.getCmp("RepeatedInvoicesWin")){
                Wtf.getCmp("RepeatedInvoicesWin").close(); 
                if(Wtf.getCmp("RepeateSalesOrderList")){
                    Wtf.getCmp("RepeateSalesOrderList").grid.getStore().reload();
                }
            }
          
            if(Wtf.getCmp("RecurringSO"))
                Wtf.getCmp("RecurringSO").disable(); 
                superThis.close();
                if(superThis.isOrder){
                    if(Wtf.getCmp("SalesOrderList")){
                        Wtf.getCmp("SalesOrderList").grid.getStore().reload();
                        Wtf.getCmp("RepeateSalesOrderList").grid.getStore().reload();
                    }
            } else {    //ERP-21555
                if(Wtf.getCmp("InvoiceListEntry")){     //Sales Invoice
                    Wtf.getCmp("InvoiceListEntry").grid.getStore().reload();
                    Wtf.getCmp("RepeateInvoiceListEntry").grid.getStore().reload();
                } 
                if(Wtf.getCmp("GRListEntry")){   //Purchase Invoice
                    Wtf.getCmp("GRListEntry").grid.getStore().reload();
                    Wtf.getCmp("RepeateGRListEntry").grid.getStore().reload();
                }
            }
        },this);
            
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
        
    setMemoDetails:function(){
        var interval=this.interval.getValue();
        var store = this.Memostore;        
        var browserTimezoneOffset = 0;//currentDate.getTimezoneOffset();    
        var nextDate=new Date(this.nextDate.getValue());
        var rec;
        var moduleName = "Invoice";
        var dateToSet=new Date();
        /**
         * Below code sets the memo in recurring sales invoice setting on the bases of company preference check ERM-384
         */
        var memo="";
        if (this.record && this.record != undefined && this.record.data != undefined) {
            memo = this.record.data.memo;
        } else if (this.invoiceRec != undefined && this.invoiceRec.data && this.invoiceRec.data.memo != undefined) {
            memo = this.invoiceRec.data.memo;
        }
        if(this.moduleid == Wtf.Acc_Invoice_ModuleId){
            if (CompanyPreferenceChecks.recuringSalesInvoiceMemo() == Wtf.RECURRINGINVOICESMEMO.DATEDON) {
                moduleName = "Sales Invoice as of ";
            } else if (CompanyPreferenceChecks.recuringSalesInvoiceMemo() == Wtf.RECURRINGINVOICESMEMO.ORIGANALINVOICE) {
                moduleName = memo;
            } else if (CompanyPreferenceChecks.recuringSalesInvoiceMemo() == Wtf.RECURRINGINVOICESMEMO.DATEDONORIGANALINVOICE) {
                moduleName = memo + " " + "Sales Invoice as of ";
            }
        } else if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId){
            moduleName = "Purchase Invoice as of ";
        } else if(this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            moduleName = "Sales Order as of ";
        }
        for(var i=0;i<store.getCount();i++){            
            rec=store.getAt(i); 
            if (this.moduleid == Wtf.Acc_Invoice_ModuleId && CompanyPreferenceChecks.recuringSalesInvoiceMemo() == Wtf.RECURRINGINVOICESMEMO.ORIGANALINVOICE) {
                rec.set('memo', moduleName);
            } else {
                if (i == 0) {
                    var date = new Date(this.nextDate.getValue());
                    date = date.format("Y-m-d");
                    rec.set('memo', moduleName + date);
                    continue;
                }
                if (this.intervalType.getValue() == "month") {
                    dateToSet = new Date(nextDate.setMonth(nextDate.getMonth() + (interval)) + browserTimezoneOffset);
                    dateToSet = dateToSet.format("Y-m-d");
                    rec.set('memo', moduleName + dateToSet);
                } else {
                    if (this.intervalType.getValue() == "week") {
                        dateToSet = new Date(nextDate.setDate(nextDate.getDate() + (interval * 7)) + browserTimezoneOffset);
                        dateToSet = dateToSet.format("Y-m-d");
                        rec.set('memo', moduleName + dateToSet);
                    }
                    if (this.intervalType.getValue() == "day") {
                        dateToSet = new Date(nextDate.setDate(nextDate.getDate() + (interval)) + browserTimezoneOffset);
                        dateToSet = dateToSet.format("Y-m-d");
                        rec.set('memo', moduleName + dateToSet);
                    }
                }
            }
        }
    },
    addDays: function (date, days) {
        var result = new Date(date);
        result.setDate(result.getDate() - days);
        return result;
    }
});
