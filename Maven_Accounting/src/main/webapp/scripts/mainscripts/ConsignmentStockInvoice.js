/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
/*< COMPONENT USED FOR >
 *      1.Credit Vendor Invoice
 *          callGoodsReceipt(isEdit,rec,winid) --- <  >
 *          [isEdit=true/false, isCustomer=false, record]
 *      2.Invoice
 *          callInvoice(isEdit,rec,winid) --- < Create Invoice >
 *          [isEdit=true/false, isCustomer=true, record]
 *      3.Invoice
 *          callBillingInvoice(isEdit,rec,winid) --- < Create Invoice >
 *          [isEdit=true/false, isCustomer=true, isCustBill:true, record]
 *      4.Sales Receipt
 *          callBillingSalesReceipt(isEdit,rec,winid) --- < Create Cash Sales >
 *          [isEdit=true/false, isCustomer=true, isCustBill:true, cash:true, record]
 *      5.Cash Sales
 *          callSalesReceipt(isEdit,rec,winid) --- < Credit Cash Sales >
 *          [isEdit=true/false, isCustomer=true, cash:true, record]
 *      6.Cash Purchase
 *          callPurchaseReceipt(isEdit,rec,winid) --- <  >
 *          [isEdit=true/false, isCustomer=false, cash:true, record]
 *      7.Sales Order
 *          callSalesOrder(isEdit,rec,winid) --- < Create sales Order >
 *          [isEdit=true/false, isCustomer=true, isOrder=true, record]
 *      8.Puchase Order
 *          callPurchaseOrder(isEdit,rec,winid) --- <  >
 *          [isEdit=true/false, isCustomer=false, isOrder=true, record]
 *		9.Quotation
 *          callQuotation() --- < Create Quotation >
 *          [quotation=true, isCustomer=false, isOrder=true]
 *
 *      10.this.appendId --- It is used when this.id is appended in the id of component. This is useful for displaying help.
 */

Wtf.account.ConsignmentStockTransactionPanel=function(config){
    this.quotation = (config.quotation!=null && config.quotation!=undefined)?config.quotation:false;
    this.isFixedAsset = (config.isFixedAsset!=null && config.isFixedAsset!=undefined)?config.isFixedAsset:false;
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset==null && config.isLeaseFixedAsset==undefined)?config.isLeaseFixedAsset:false;
    this.isConsignment = (config.isConsignment!=null || config.isConsignment!=undefined)?config.isConsignment:false;
    this.DefaultVendor = config.DefaultVendor;
	this.id=config.id;
	this.titlel = config.title!=undefined?config.title:"null";
    this.dataLoaded=false;
    this.isViewTemplate = (config.isViewTemplate!=undefined?config.isViewTemplate:false);
    this.isMovementWarehouseMapping=Wtf.account.companyAccountPref.isMovementWarehouseMapping;
    this.isRequestApprovalFlow=Wtf.account.companyAccountPref.requestApprovalFlow;
    this.isTemplate = (config.isTemplate!=undefined?config.isTemplate:false);
    this.createTransactionAlso = false;
    this.transactionType = 0;
    this.recordId = "";
    this.isCopyFromTemplate = (config.isCopyFromTemplate!=undefined?config.isCopyFromTemplate:false);
    this.isOpeningBalanceOrder = (config.isOpeningBalanceOrder!=undefined?config.isOpeningBalanceOrder:false);
    this.templateId = config.templateId;
//    this.isOnTemplateSelect = undefined;
    this.sendMailFlag = false;
    this.mailFlag = false;
    this.isExpenseInv=false;
    this.isEdit=config.isEdit;
    this.ispurchaseReq=config.ispurchaseReq;
    this.isFromGrORDO = false;
    this.label=config.label;
    this.copyInv=config.copyInv;
    this.readOnly=(config.readOnly!=undefined?config.readOnly:false);
    this.isInvoice=config.isInvoice;
    this.billid=null;
    this.custChange=false;
    this.record=config.record;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.handleEmptyText=false; //To handle empty text after clicking on save button
    this.isMultiSelectFlag=false; // keep this flag for allow multiselection
    this.isPOfromSO=config.isPOfromSO;
    this.GENERATE_PO=config.isPOfromSO;
    this.isSOfromPO=config.isSOfromPO;   //This flag is used for creating so from po
    this.GENERATE_SO=config.isSOfromPO;    
    this.datechange=0;
    this.oldval="";this.val="";this.pronamearr=[];
    this.changeGridDetails=true;
    this.appendID = true;
    this.heplmodeid = config.heplmodeid;
    this.response = "";
    this.request = "";
    this.amountdue=0;
    this.savecount=0;
    this.CustomStore = "";
    this.defaultsalesperson="";
    this.termid="";
    this.currentAddressDetailrec="";
    this.moduleid=config.moduleid;
    this.gstCurrencyRate=0.0;
    var help=getHelpButton(this,config.heplmodeid);
    if( this.isConsignment){
        this.uPermType=config.isCustomer?Wtf.UPerm.consignmentsales:Wtf.UPerm.consignmentpurchase;
        this.permType= config.isCustomer?Wtf.Perm.consignmentsales:Wtf.Perm.consignmentpurchase;   
        this.exportPermType=config.isCustomer?(this.isInvoice?this.permType.exportsalesconinv:this.permType.exportsalesconreq):(this.isInvoice?this.permType.exportpurchaseconinv:this.permType.exportpurchaseconreq);
        this.printPermType=config.isCustomer?(this.isInvoice?this.permType.printsalesconinv:this.permType.printsalesconreq):(this.isInvoice?this.permType.printpurchaseconinv:this.permType.printpurchaseconreq);
    }
    this.custUPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.custPermType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.soPermType=(config.isCustomer?Wtf.Perm.invoice.createso:Wtf.Perm.vendorinvoice.createpo);
    this.isFromProjectStatusRep = (config.isFromProjectStatusRep!=null&&config.isFromProjectStatusRep!=undefined)?config.isFromProjectStatusRep:false;
    var isbchlFields1=(!config.isCustomer && config.isOrder);
    this.isWithInvUpdate = config.isWithInvUpdate;
    this.GRDOSettings=config.GRDOSettings
    this.viewGoodReceipt = config.viewGoodReceipt;
     var buttonArray = new Array();
    var moduleId=WtfGlobal.getModuleId(this);// to show terms in all SO,CQ,VQ,PO
     this.IsInvoiceTerm = (config.isCustomer && (config.moduleid=='2'|| moduleId==22)) || moduleId==6 || moduleId==23 ;
//     this.IsInvoiceTerm = (config.isCustomer && config.moduleid=='2') || config.moduleid==6;
     this.modeName = config.modeName;
     this.islockQuantityflag=config.islockQuantityflag;
     
          buttonArray.push({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
        id: "save" + config.heplmodeid + this.id,
        hidden:this.isViewTemplate,
        disabled:this.readOnly,
        scope: this,
        handler: function(){
            this.mailFlag = true;
            if(this.isTemplate){
                if(this.moduleTemplateName.getValue() == ''){
                    WtfComMsgBox(["Error","Please Enter Template Name First."], 1);
                    return;
                }
                this.saveTemplate();
            }else{
                this.save();
            }
        },
        iconCls: 'pwnd save'
    });
     
    buttonArray.push({
        text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
        tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
        id: "savencreate" + config.heplmodeid + this.id,
        hidden : (this.isEdit&&!this.isCopyFromTemplate) || (this.copyInv&&!this.isCopyFromTemplate) || this.isTemplate || this.isViewTemplate,
        scope: this,
        handler: function(){
            this.mailFlag = false;
            this.save();
        },
        iconCls: 'pwnd save'
    });
    
    buttonArray.push({
        text:WtfGlobal.getLocaleText("acc.common.email"),  // "Email",
        tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
        id: "emailbut" + this.id,
        hidden : this.isTemplate||this.isViewTemplate,
        scope: this,
        disabled : true,
        handler: function(){this.callEmailWindowFunction(this.response, this.request)},
        iconCls: "accountingbase financialreport"
    });
   
    var tranType=null;
    if(this.isCustBill)
        tranType=config.isCustomer?(config.isOrder?Wtf.autoNum.BillingSalesOrder:Wtf.autoNum.BillingInvoice):(config.isOrder?Wtf.autoNum.BillingPurchaseOrder:Wtf.autoNum.BillingGoodsReceipt);
    else if(config.moduleid==18||config.moduleid==20||config.moduleid==36 || config.moduleid==50||config.moduleid==63){
        if(!config.isCustomer){
        if(config.moduleid==18 ||(config.moduleid==50)||(config.moduleid==63)){
            tranType=Wtf.autoNum.PurchaseOrder;
        }
    }else{
            tranType=Wtf.autoNum.SalesOrder;
        }   
    }else if(config.moduleid==22||config.moduleid==23){
        if(config.moduleid==22){
            tranType=Wtf.autoNum.Quotation;
        }else{
            tranType=Wtf.autoNum.Venquotation;
        } 
    }else if(config.moduleid==2 || config.moduleid==52){
        tranType=Wtf.autoNum.Invoice;
     }else{
        tranType=Wtf.autoNum.GoodsReceipt;
    }
    if(this.isRequisition) {
        tranType= Wtf.autoNum.Requisition;
    } else if(this.isRFQ) {
        tranType= Wtf.autoNum.RFQ;
    }
    	

    var singlePDFtext = null;
    if(this.isQuotation)
    	singlePDFtext = WtfGlobal.getLocaleText("acc.accPref.autoQN");
    else
    	singlePDFtext = config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoSO"):WtfGlobal.getLocaleText("acc.accPref.autoInvoice")):(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoPO"):WtfGlobal.getLocaleText("acc.accPref.autoVI"));

    this.singlePrint=new Wtf.exportButton({
         obj:this,
         id:"exportpdf" + this.id,
         iconCls: 'pwnd exportpdfsingle',
         text:WtfGlobal.getLocaleText("acc.field.ExportPDF"),// + " "+ singlePDFtext,
         tooltip :WtfGlobal.getLocaleText("acc.rem.39.single"),  //'Export Single Record details',
         disabled :true,
         isEntrylevel:true,
         exportRecord:this.exportRecord,
         hidden:this.isRequisition || this.isRFQ || this.isSalesCommissionStmt ||this.readOnly,
         menuItem:{rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
         get:tranType,
         moduleid:config.moduleid
     });
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        buttonArray.push(this.singlePrint);
    }
    this.singleRowPrint = new Wtf.exportButton({
        obj: this,
        id: "printSingleRecord"+ config.id,
        iconCls: 'pwnd printButtonIcon',
        text: WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
        disabled: this.readOnly?false:true,
        isEntrylevel: false,
        exportRecord:this.exportRecord,
        menuItem: {
            rowPrint: true
        },
        get:tranType,
        moduleid:config.moduleid,
        hidden:(config.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId||config.moduleid==Wtf.Acc_ConsignmentInvoice_ModuleId)?false:true
    });
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        buttonArray.push(this.singleRowPrint);
    }
    buttonArray.push({
//        xtype: 'button',
        text:  WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
        cls: 'pwnd add',
        id: "posttext" + this.id,        
        //hidden:(config.moduleid!=Wtf.Acc_Invoice_ModuleId && config.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId),        
        tooltip : 'Use Post Text option to insert text after Signature',       
        style:" padding-left: 15px;",
        scope: this,
        hidden:this.isTemplate || this.isViewTemplate,
        handler: function() {
            this.getPostTextEditor(this.postText);
        }
    });
       this.outstandingreportflag=false;
    buttonArray.push({
        text: (config.isCustomer?'Show Outstandig SO':'Show Outstandig PO'),
        cls: 'pwnd add',
        id: "posttext" + this.id,        
        hidden:!(config.moduleid==Wtf.Acc_Purchase_Order_ModuleId || config.moduleid==Wtf.Acc_Lease_Order) ||this.readOnly,
        tooltip : (config.isCustomer?'Shows Outstandig Sales Order':'Shows Outstandig Purchase Order'),
        style:" padding-left: 15px;",
        scope: this,
               
        handler: function() {
            this.outstandingreportflag=true;
             this.person= this.Name.getValue();
           if(config.moduleid==Wtf.Acc_Lease_Order)
            {
              
               callSalesOrderList(false,false,this.outstandingreportflag,this.person);  //for showing Outstanding SO and Po while creating one flag passed
            }
            else
            {
               callPurchaseOrderList(false,false,this.outstandingreportflag,this.person);
            }
        }
    });
     buttonArray.push({
        text:config.moduleid==Wtf.Acc_Invoice_ModuleId ? WtfGlobal.getLocaleText("acc.field.SetRecurringInvoice"):WtfGlobal.getLocaleText("acc.field.SetRecurringSO") ,
        iconCls:getButtonIconCls(Wtf.etype.copy),
        id:'RecurringSO',        
        hidden:!(this.isInvoice && config.isCustomer) ||this.readOnly,
        tooltip :'Create Recurring Sales Invoice',
        style:" padding-left: 15px;",
        scope: this,
        disabled : true,     
        handler: function() {
                 callRepeatedInvoicesWindow(true,undefined,false,false,true,this.RecordID,this.Term.getValue());//set Forth Variable to false for Invoice  and true for sales order 
        }
    });
    
    buttonArray.push({
        text: WtfGlobal.getLocaleText("acc.field.ShowAddress"),  //WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
        cls: 'pwnd add',
        id: "showaddress" + this.id,                
        tooltip : WtfGlobal.getLocaleText("acc.field.UseShowAddressoptiontoinsertAddresses"),       
        style:" padding-left: 15px;",
        scope: this,
        disabled : true, 
        handler: function() {
            var addressRecord="";
            if (this.linkRecord && this.singleLink) {        //when user link single record
                addressRecord = this.linkRecord;
            } else{
                addressRecord=this.record;
            }
            var isCopy = this.copyInv;
            var isEdit = this.isEdit;
            if (this.isVenOrCustSelect) {
                isEdit = false;
                isCopy = false;
            }
            callAddressDetailWindow(addressRecord,isEdit,isCopy,this.Name.getValue(),this.currentAddressDetailrec,config.isCustomer,this.readOnly,"",this.singleLink,"",this.moduleid);
            Wtf.getCmp('addressDetailWindow').on('update',function(config){
                this.currentAddressDetailrec=config.currentaddress;
            },this);
        }
    });
    
//    if (isbchlFields1 && this.isEdit && !this.copyInv) {
//
//        buttonArray.push(this.exportButton = new Wtf.exportButton({
//            obj: config.POthisObj,
////            id: "exportReports" + config.helpmodeid + config.id,
//            text: WtfGlobal.getLocaleText("acc.common.export"),
//            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
//            disabled: false,
//            menuItem: {csv: false, pdf: false, rowPdf: (config.isSalesCommissionStmt) ? false : true, rowPdfTitle: WtfGlobal.getLocaleText("acc.rem.39")},
//            get: config.POnewtranType
//        }));
//
//    }
    buttonArray.push('->');
    if(!this.isEdit){
       buttonArray.push(help);
    }
    Wtf.apply(this, config);
    Wtf.apply(this, {
        bbar: buttonArray
    });

    Wtf.apply(this, config);
    Wtf.apply(this, {
        bbar:buttonArray
      });
    Wtf.account.ConsignmentStockTransactionPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.ConsignmentStockTransactionPanel,Wtf.account.ClosablePanel,{
    autoScroll: true,// layout:'border',//Bug Fixed: 14871[SK]
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border:'false',
    externalcurrencyrate:0,
    exchangeratetype:"",
    revexternalcurrencyrate:0,
    isCurrencyLoad:false,
    currencyid:null,
    custdatechange:false,
    closable : true,
    cash:false,
    layout : 'border',
    isCustomer:false,
    cls : 'southcollapse',
    isCustBill:false,
    isOrder:false,
    fromOrder:false,
    loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            var data=this.record.data;
            this.NorthForm.getForm().loadRecord(this.record);
            if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash && !this.isFixedAsset) {
                this.InvoiceStore.on("load", function(){
                    this.invoiceList.setValue(data.landedInvoiceNumber);
                }, this);
                this.InvoiceStore.load();
            }
            
            if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash)) {
                this.termds.on("load", function(){
                    this.Term.setValue(data.termdays);
                }, this);
            }
            this.sequenceFormatStore.load();
            if(!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO)
                this.Number.setValue(data.billno);
            if(this.isPOfromSO||this.isSOfromPO){ // for showing link number in number field in case of creating PO from SO or creating SO from PO
                this.fromPO.setValue(true);
                this.fromLinkCombo.setValue(0);                
                this.POStore.proxy.conn.url =(this.isPOfromSO)?"ACCSalesOrderCMN/getSalesOrders.do":(this.isSOfromPO)?"ACCPurchaseOrderCMN/getPurchaseOrders.do":"";
                this.POStore.on("load", function(){
                    if(this.isPOfromSO||this.isSOfromPO){
                        this.PO.disable();
                        this.fromPO.disable();
                        this.setTransactionNumber();
                        this.PO.setValue(data.billid);
                    }
                    if(this.isPOfromSO){
                        this.isPOfromSO = false;
                    }
                    if(this.isSOfromPO){
                        this.isSOfromPO = false;
                    }
                }, this);
                this.POStore.load();                        
                
            }else{ // for showing multiple link numbers in number field
                this.Grid.getStore().on("load",function(){
                if(this.Grid.getStore().data.items.length>0){
                    var linkType=-1;
                    var storeData = [],linkNumbers=[],linkIDS=[];
                    this.POStore.removeAll();
                    this.Grid.getStore().each(function(rec){
                        if(this.copyInv) { 
                            rec.data.linkid=""; 
                            rec.data.rowid=""; 
                            rec.data.linktype="";
                            rec.data.linkto="";
                        } else {
                            if((rec.data.linkto!=""&&rec.data.linkto!=undefined) && (rec.data.linktype!=-1 && rec.data.linktype!=undefined)){
                                var isExistFlag=false;
                                for(var count=0;count<linkNumbers.length;count++){
                                    if(rec.data.linkto==linkNumbers[count]){
                                        isExistFlag=true;
                                        break;
                                    }
                                }
                                if(isExistFlag==false){
                                    linkNumbers.push(rec.data.linkto);
                                    linkIDS.push(rec.data.linkid);
                                }                                                        
                                linkType=rec.data.linktype;                            
                                var newRec=new this.PORec({
                                    billid:rec.data.linkid,
                                    billno:rec.data.linkto    
                                });
                                storeData.push(newRec);
                            }
                        }
                    },this);
                    if(storeData.length>0){
                        this.POStore.add(storeData);
                    }
                    if(linkIDS.length>0){
                        
                        if(this.Grid){
                            this.Grid.fromPO=true;
                            this.Grid.linkedFromOtherTransactions=true;
                        }
                        
                        this.Name.disable();
                        this.Currency.disable();
                        this.fromPO.disable();
                        this.fromLinkCombo.disable();
                        this.PO.disable();
                        this.fromPO.setValue(true);                
                        this.PO.setValue(linkIDS);
                    }
                    if(linkType!=-1){
                        this.fromLinkCombo.setValue(linkType);
                    }
                }
                },this);
            }
            if(this.copyInv || this.isEdit){
                if(Wtf.getCmp("showaddress" + this.id)){
                    Wtf.getCmp("showaddress" + this.id).enable(); 
                }   
            }
            this.template.setValue(data.templateid);
            this.Currency.setValue(data.currencyid);
            this.Name.setValForRemoteStore(data.personid, data.personname, undefined);
           if(data.islockQuantityflag)
             {
                 this.lockQuantity.setValue(true);
            }
            else
            {
                 this.lockQuantity.setValue(false);
//                 this.wareHouseCombo.enable();
            }
            var store=(this.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore)
            var index=store.findBy( function(rec){
                var parentname=rec.data['accid'];
                if(parentname==data.personid)
                    return true;
                 else
                    return false;
            })
            if(index>=0)
                this.Name.setValue(data.personid);
            this.Memo.setValue(data.memo);
            this.postText = data.posttext;
            this.DueDate.setValue(data.duedate);
            if(this.isConsignment && this.isCustomer) {
                this.wareHouseStore.on("load", function(){
                    this.warehouses.setValue(data.custWarehouse);
                    this.Grid.warehouseselcted=true;
                }, this);
                this.wareHouseStore.load();
            }
            if(this.isConsignment && this.isCustomer && this.isMovementWarehouseMapping) {
                    var store1=Wtf.movmentTypeStore
                    var index1=store1.findBy( function(rec){
                        var id=rec.data['id'];
                        if(id==data.movementtype)
                            return true;
                        else
                            return false;
                    })
                    if(index1>=0)
                        this.movmentType.setValue(data.movementtype);
                        this.ProductGrid.movmentType=data.movementtype;
                        this.Grid.movmentType=data.movementtype;
                
                this.reqtestWareHouseStore.on("load", function(){
                    this.wareHouseCombo.setValue(data.requestWarehouse);
                     this.Grid.requestWarehouse=data.requestWarehouse;
                    this.locationStore.load();
                }, this);
                this.reqtestWareHouseStore.load();
                this.locationStore.on("load", function(){
                    this.locationMultiSelect.setValue(data.requestLocation);
                    this.Grid.requestLocation=data.requestLocation;
                }, this);
                    
                        
            }
            
            
            if(this.isOrder && data.isOpeningBalanceTransaction){
                this.isOpeningBalanceOrder = data.isOpeningBalanceTransaction;
                this.billDate.maxValue=this.getFinancialYRStartDatesMinOne(true);
            }
            this.billDate.setValue(data.date);
            this.fromdate.setValue(data.fromdate)
            this.todate.setValue(data.todate)
            this.perDiscount.setValue(data.ispercentdiscount);
            this.Discount.setValue(data.discountval);
            this.isTaxable.setValue(data.taxincluded);
            this.PORefNo.setValue(data.porefno);

            this.CostCenter.setValue(data.costcenterid);
            this.dataLoaded=true;
            if(this.IsInvoiceTerm) {
                this.setTermValues(data.termdetails);
            }
            if(this.isPOfromSO||this.isSOfromPO){
            	this.billDate.setValue(Wtf.serverDate);
            	this.updateDueDate();
            }
            if(this.isCustomer && this.record.data.partialinv){
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                this.Grid.getColumnModel().setHidden( rowindex,false) ;
            }
            var gridID=this.Grid.getId();
            var taxColumnIndex=this.Grid.getColumnModel().getIndexById(gridID+"prtaxid");
            var taxAmtColumnIndex=this.Grid.getColumnModel().getIndexById(gridID+"taxamount");
            if(this.record.data.includeprotax){
                this.Grid.getColumnModel().setHidden( taxColumnIndex,false) ;
                this.Grid.getColumnModel().setHidden( taxAmtColumnIndex,false) ;
                
                this.isTaxable.setValue(false);
                this.isTaxable.disable();
                this.Tax.setValue("");
                this.Tax.disable();
                
            }else{
                this.Grid.getColumnModel().setHidden( taxColumnIndex,true) ;
                this.Grid.getColumnModel().setHidden( taxAmtColumnIndex,true) ;
                
                this.isTaxable.reset();
                this.isTaxable.enable();
            }
            if(data.taxid == ""){
                this.isTaxable.setValue(false);
                this.Tax.setValue("");
                this.Tax.disable();
            }else{
                this.Tax.setValue(data.taxid);
                this.isTaxable.setValue(true);
            }
            this.loadTransStore();
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
                    this.ProductGrid.productComboStore.load({params:{mappingProduct:true,customerid:this.Name.getValue(),common:'1', loadPrice:true,mode:22}}) ;           
            }       
        }
    },
    onRender:function(config){
        if(this.isConsignment && this.moduleid === Wtf.Acc_ConsignmentInvoice_ModuleId){
            this.toggleBtnPanel.getComponent(0).show();
        }
        var centerPanel = new Wtf.Panel({
                region : 'center',
                border : false,
                autoScroll : true
            });
        if(this.isCustomer||this.isCustBill||this.isOrder||this.isEdit && !this.isExpenseInv||this.copyInv && !this.isExpenseInv || this.isTemplate) {
            centerPanel.add(this.NorthForm,this.formpPanelOfbutton,this.Grid,this.southPanel);
        } else if((this.isEdit && this.isExpenseInv) || (this.copyInv && this.isExpenseInv)) {
            centerPanel.add(this.NorthForm,this.ExpenseGrid,this.southPanel);
//            this.Tax.store=this.ExpenseGrid.taxStore;
        } else {
            centerPanel.add(this.NorthForm,this.GridPanel,this.southPanel);
        }
        
        this.add(centerPanel);
        this.add({
                border: true,
                id: 'south' + this.id,
                region: 'south',
                //split: true,
                hidden : true,//this.isCustBill,
                layout: 'fit',
                height:130 ,
                plugins : new Wtf.ux.collapsedPanelTitlePlugin(),
                collapsibletitle : WtfGlobal.getLocaleText("acc.common.recentrec") + " " +this.businessPerson + " for the Product",
                title : WtfGlobal.getLocaleText("acc.common.recentrec") + " " +this.businessPerson + " for the Product",
                collapsible: true,
                collapsed: true,
                items : [
                    this.lastTransPanel
                ]
            });
        Wtf.account.ConsignmentStockTransactionPanel.superclass.onRender.call(this, config);
        
        if(this.isViewTemplate){
            this.Number.hideLabel = true;
            this.Number.hide();
            this.sequenceFormatCombobox.hideLabel = true;
            this.sequenceFormatCombobox.hide();
            this.billDate.hideLabel = true;
            this.billDate.hide();
        }
        
        if(this.isTemplate){
            this.Number.setValue("");
            this.Number.disable();
            this.sequenceFormatCombobox.disable();
            this.billDate.disable();
            this.autoGenerateDO.disable();
            this.templateModelCombo.hideLabel = true;
        }
        if(this.viewGoodReceipt || (this.isViewTemplate && this.moduleid ==Wtf.Acc_ConsignmentRequest_ModuleId)){
        if(this.Number){
            this.Number.hideLabel = false;
            this.Number.show();
        }
        if(this.sequenceFormatCombobox){
            this.sequenceFormatCombobox.hideLabel = false;
            this.sequenceFormatCombobox.show();
        }
        if(this.billDate){
            this.billDate.hideLabel =false;
            this.billDate.show();
        }
    }
        this.initForClose();
        if(this.isFromProjectStatusRep){
            
            this.selectedCustomerStore.on('load',function(){
                var rec = this.selectedCustomerStore.getAt(0);
                if(this.Name)
                    this.Name.setValue(rec.get('accid'));
//                if(this.billingAddrsStore)
//                    this.billingAddrsStore.load({params:{customerid:rec.get('accid')}});
//                if(this.ShippingAddrsStore)
//                    this.ShippingAddrsStore.load({params:{customerid:rec.get('accid')}}); 
            },this);
            this.selectedCustomerStore.load({
                params:{
                    selectedCustomerIds:this.selectedCustomerIds
                }
            });
            
            this.setPOLinks();
        }
        
        // hide form fields
            this.hideFormFields();
            if(this.isConsignment){
                  this.fromPO.hideLabel = true;
                  this.fromPO.hide();
                  this.fromLinkCombo.hideLabel = true;
                  this.fromLinkCombo.hide();
                  this.PO.hideLabel = true;
                  this.PO.hide();
        }
    },
    hideFormFields:function(){
        if(this.isCustomer){
            if(this.isInvoice){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.customerInvoice);

            } else if(this.cash){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.CS);

            } else if(this.isOrder && !this.quotation){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.salesOrder);

            } else if(this.quotation){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.customerQuotation);

            }
        }else{
            if(this.isInvoice){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.vendorInvoice);

            } else if(this.cash){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.CP);

            } else if(this.isOrder && !this.quotation){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.purchaseOrder);

            } else if(this.quotation){

                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.vendorQuotation);
            }
        }
    },
    
    
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel + " *";
                    }
                }
            }
        }
    },
    
setNextNumber:function(config){
    if(this.sequenceFormatStore.getCount()>0){
        if(this.isEdit && !this.copyInv){ //only edit case
            var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
            if(index!=-1){
                this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                this.sequenceFormatCombobox.disable();
                this.Number.disable();   
            } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                    if (this.readOnly != undefined && !this.readOnly) {
                        this.Number.enable();
                    }
                }
            }else if(!this.isEdit || this.copyInv|| this.GENERATE_PO||this.GENERATE_SO){// create new,copy,generate so and po case
                //            var seqRec=this.sequenceFormatStore.getAt(0)
                //            this.sequenceFormatCombobox.setValue(seqRec.data.id);
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i);
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                }
                if(this.sequenceFormatCombobox.getValue()!=""){
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                } else{
                    this.Number.setValue("");
                    WtfGlobal.hideFormElement(this.Number);
                }
            }
    }
},
    initComponent:function(config){
        Wtf.account.ConsignmentStockTransactionPanel.superclass.initComponent.call(this,config);
        this.businessPerson=(this.isCustomer?'Customer':'Vendor');
        this.loadCurrFlag = true;
        if(!this.isCustBill){
            this.isCustBill = false;
        }
//        this.term=0;
        
        this.tplSummary=new Wtf.XTemplate(
            '<div class="currency-view">',
            '<table width="100%">',
//            '<tpl if="'+(!this.isOrder || this.quotation)+'">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td text-align=right>{subtotal}</td></tr>',
//            '<tr><td><b>- '+WtfGlobal.getLocaleText("acc.invoice.discount")+' </b></td><td align=right>{discount}</td></tr>',
            '</table>',
//            '<hr class="templineview">',
//            '</tpl>',
            '<table width="100%">',
//            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.amt")+' </b></td><td align=right>{totalamount}</td></tr>',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.field.AmountBeforeTax")+' </b></td><td align=right>{amountbeforetax}</td></tr>',
            '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.invoice.Tax")+': </b></td><td align=right>{tax}</td></tr>',
            '</table>',
            '<table width="100%">',
            '</table>',
            '<hr class="templineview">',
            '<table width="100%">',
            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmt")+' </b></td><td align=right>{aftertaxamt}</td></tr>',
            '</table>',
            '<table width="100%">',
            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmtInBase")+' </b></td><td align=right>{totalAmtInBase}</td></tr>',
            '</table>',
            '<hr class="templineview">',
            '<hr class="templineview">',
            '</div>'
        );
        if(this.IsInvoiceTerm) { //customer invoice
            this.tplSummary=new Wtf.XTemplate(
                '<div class="currency-view">',
                '<table width="100%">',
                '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td text-align=right>{subtotal}</td></tr>',
//                '<tpl if="'+(!this.isOrder || this.quotation)+'">',
//                '<tr><td><b>- '+WtfGlobal.getLocaleText("acc.invoice.discount")+' </b></td><td align=right>{discount}</td></tr>',
//                '</tpl>',
                '</table>',
//                '<hr class="templineview">',
                '<table width="100%">',
//                '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.amt")+' </b></td><td align=right>{totalamount}</td></tr>',
                '<tr><td><b>+ Invoice Term: </b></td><td align=right>{termtotal}</td></tr>',
                '<tr><td><b>'+WtfGlobal.getLocaleText("acc.field.AmountBeforeTax")+' </b></td><td align=right>{amountbeforetax}</td></tr>',
                '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.invoice.Tax")+': </b></td><td align=right>{tax}</td></tr>',
                '</table>',
                '<table width="100%">',
                '</table>',
                '<hr class="templineview">',
                '<table width="100%">',
                '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmt")+' </b></td><td align=right>{aftertaxamt}</td></tr>',
                '</table>',
                '<table width="100%">',
                '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmtInBase")+' </b></td><td align=right>{totalAmtInBase}</td></tr>',
                '</table>',
                '<hr class="templineview">',
                 '</table>',
                '<table width="100%">',
//                '<tpl if="'+(!this.isOrder || this.quotation)+'">',
                '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.inv.amountdue")+' </b></td><td align=right>{amountdue}</td></tr>',
                '</table>',
                '<hr class="templineview">',
//                '</tpl>',
                '<hr class="templineview">',
                '</div>'
            );
        }
        
        this.GridRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'number'}
        ]);

        this.termRec = new Wtf.data.Record.create([
            {name: 'termname'},
            {name: 'termdays'},
            {name: 'termid'}
        ]);
        this.termds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.termRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTerm/getTerm.do",
            baseParams:{
                mode:91
            }
         });         
         
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'value'
        },
        {
            name: 'oldflag'
        }
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName
            }
        });
//          this.sequenceFormatStore.on('load',function(){
//             if(this.sequenceFormatStore.getCount()>0){
//                 var seqRec=this.sequenceFormatStore.getAt(0)
//                this.sequenceFormatCombobox.setValue(seqRec.data.id);
//                if((!this.isEdit&&!this.copyInv) || this.isPOfromSO || this.isSOfromPO){
//                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
//                 }
//             }
//         },this);
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
         this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
         ]);
         this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
    //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStoreCMB = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
    //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStoreCMB.load();
         
         var transdate=(this.isEdit?WtfGlobal.convertToGenericDate(this.record.data.date):WtfGlobal.convertToGenericDate(new Date()));

         this.selectedCustomerStore = new Wtf.data.Store({
        //    url:Wtf.req.account+'CustomerManager.jsp',
            url:"ACCCustomer/getCustomersForCombo.do",
            baseParams:{
                mode:2,
                group:10,
                deleted:false,
                nondeleted:true,
                common:'1'
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad:false
            },Wtf.personRec)
        });

         this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  //'Currency',
            hiddenName:'currencyid',
            id:"currency"+this.heplmodeid+this.id,
//            anchor: '94%',
            width : 240,
            //disabled:true,
            store:this.currencyStore,
            valueField:'currencyid',
            allowBlank : false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
        });

        this.Currency.on('select', function(){
            var customer="",currency="";
             this.externalcurrencyrate=0;
            if(this.Name.getValue() != undefined && this.Name.getValue() != ""){
                customer= this.Name.getValue();
            }    
            if(this.Currency.getValue() != undefined && this.Currency.getValue() != ""){
                currency= this.Currency.getValue();        
            }                 
            if(!this.GENERATE_PO&&!this.GENERATE_SO){
                this.onCurrencyChangeOnly();
                this.Name.setValue(customer);
                this.Currency.setValue(currency);
            }    
            if (this.Grid) {
                this.Grid.forCurrency = this.Currency.getValue();
            }
            this.currencychanged = true;
            this.updateFormCurrency();
//            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),tocurrencyid:this.Currency.getValue()}});
        }, this);
        
        this.Term= new Wtf.form.FnComboBox({
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.creditTerm"):WtfGlobal.getLocaleText("acc.invoice.debitTerm"))+' *',
            itemCls : (this.cash)?"hidden-from-item":"",  //||this.isOrder
            hideLabel:this.cash,    //||this.isOrder
            id:"creditTerm"+this.heplmodeid+this.id,
            hidden:this.cash,                //||this.isOrder,
            hiddenName:'term',
//            anchor: '93.5%',
            width : 240,
            store:this.termds,
            valueField:'termdays',
            allowBlank:this.cash,               //||this.isOrder,
            emptyText:(this.isCustomer?WtfGlobal.getLocaleText("acc.inv.ct"):WtfGlobal.getLocaleText("acc.inv.dt")),
            forceSelection: true,
            displayField:'termname',
//            addNewFn:this.addCreditTerm.createDelegate(this),
            scope:this,
            listeners:{
                'select':{
                    fn:this.updateDueDate,
                    scope:this
                }
            },
            selectOnFocus:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditterm, Wtf.Perm.creditterm.edit))
            this.Term.addNewFn=this.addCreditTerm.createDelegate(this);
        
        this.moduleTemplateSection();
        this.postText=(this.record)?this.record.data.posttext:"";
        if(!this.isFromProjectStatusRep){
            if(this.isCustomer){
                Wtf.customerAccStore.reload();
            }else{
                Wtf.vendorAccStore.reload();
            }
        }
        
        this.Name= new Wtf.form.ExtFnComboBox({
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") , //this.businessPerson+"*",
            hiddenName:this.businessPerson.toLowerCase(),
            id:"customer"+this.heplmodeid+this.id,
            store: this.isFromProjectStatusRep?this.selectedCustomerStore:(this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore),
            valueField:'accid',
            displayField:'accname',
            minChars:1,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            allowBlank:false,
            hirarchical:true,
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") , //'Select a '+this.businessPerson+'...',
            mode: 'remote',
            typeAhead: true,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            forceSelection: true,
            selectOnFocus:true,
            isVendor:!(this.isCustomer),
            isCustomer:this.isCustomer,
//            anchor:"50%",
            width : 240,
            triggerAction:'all',
 //           addNewFn:this.addPerson.createDelegate(this,[false,null,this.businessPerson+"window",this.isCustomer],true),
            scope:this,
            listeners:{
                'select':{
                    fn:function(){
                        this.singleLink = false;
                        if (this.isEdit || this.isCopy) {
                            this.isVenOrCustSelect = true;
                        }
                        this.currentAddressDetailrec="";//If customer/vendor change in this case,previously stored addresses in this.currentAddressDetailrec will be clear    
                        var customer= this.Name.getValue();
                        this.Grid.warehouseselcted=false; 
                        this.warehouses.clearValue();
                        if(this.ispurchaseReq)
                        {
                        this.Name.setValue(customer);
                        }
                        else
                        {
                            if(!this.GENERATE_PO&&!this.GENERATE_SO){
                                this.loadStore();
                                this.Name.setValue(customer);
                            }  
                        }
                        this.updateData();  
                       if(this.isCustomer)
                        {this.wareHouseStore.load({params:{customerid:this.Name.getValue()}})};
//                        this.ProductGrid.productComboStore.load({params:{mappingProduct:true,customerid:this.Name.getValue(),common:'1', loadPrice:true,mode:22}}) No need to Load product Combo as we are not using mapping functionality in ConsignMent.
                         if(!this.isCustomer){
                            this.populateAllConsignData();
                        }
                        var customer = this.Name.getValue();
                        this.tagsFieldset.resetCustomComponents();
                        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
                        this.tagsFieldset.setValuesForCustomer(moduleid, customer);
                    },
            scope:this            
                }
            }
        });        
        
        // Neeraj
        if(!(this.DefaultVendor==null || this.DefaultVendor==undefined) && !this.isCustomer){
        	this.Name.value = this.DefaultVendor;
        	this.updateData();
        }

        if(!WtfGlobal.EnableDisable(this.custUPermType,this.custPermType.create))
            this.Name.addNewFn=this.addPerson.createDelegate(this,[false,null,this.businessPerson+"window",this.isCustomer],true);
        
          chkmovementtypeload();
        this.movmentType = new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.RequestType")+"*",
            valueField:'id',
            displayField:'name',
            hideLabel:!this.isConsignment || !this.isMovementWarehouseMapping || !this.isCustomer || (this.moduleid==Wtf.Acc_ConsignmentInvoice_ModuleId),
            hidden:!this.isConsignment || !this.isMovementWarehouseMapping || !this.isCustomer || (this.moduleid==Wtf.Acc_ConsignmentInvoice_ModuleId),
            store:Wtf.movmentTypeStore,
            //anchor:'90%',
              width : 240,
            allowBlank : !this.isMovementWarehouseMapping || !this.isCustomer || (this.moduleid==Wtf.Acc_ConsignmentInvoice_ModuleId)?true:false,
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            name:'movemettype',
            hiddenName:'movemettype'

        }); 
        this.movmentType.on('select',function(){
            this.ProductGrid.movmentType=this.movmentType.getValue();
            this.reqtestWareHouseStore.load();
            this.Grid.movmentType=this.movmentType.getValue();
            this.wareHouseCombo.enable();   
            this.locationMultiSelect.reset();//ERP-18541
            this.wareHouseCombo.reset(); 
        },this);       
        
        this.warehouseRec = new Wtf.data.Record.create([//  warehouse record
            {name: 'id'},
            {name: 'name'},
            {name: 'customer'},
            {name: 'company'},
            {name: 'doids'},
            {name: 'warehouse'},
            {name: 'isdefault'}
            
        ]);
        this.wareHouseStore = new Wtf.data.Store({  //  warehouse store for combo box
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.warehouseRec),
        url:"ACCCustomerCMN/getAllCustomerWarehouse.do",
        baseParams:{
            isForCustomer:true
        }
        });
    this.wareHouseStore.on('load',function(){
        var index=this.wareHouseStore.find('isdefault','T');
        var rec =this.wareHouseStore.getAt(index);
        if(rec != undefined && !this.isEdit){
            this.warehouses.setValue(rec.data.warehouse);
            this.warehouses.fireEvent('select',this);
        }
    },this)   
        this.warehouses= new Wtf.form.FnComboBox({     //  warehouse store
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'warehouse',
            displayField:'name',
            hideLabel:!this.isConsignment || !this.isCustomer,
            hidden:!this.isConsignment || !this.isCustomer,
            disabled:this.isEdit,
            id:"warehouse"+this.heplmodeid+this.id,
//            store:this.isCustomer ? Wtf.salesPersonStore : Wtf.agentStore,
            store:this.wareHouseStore,
            addNoneRecord: true,
            width : 240,
//            typeAhead: true,
            forceSelection: true,
            allowBlank:this.isCustomer?false:true,
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.Consignment.Warehouses")+"*":"",
            emptyText:WtfGlobal.getLocaleText("acc.warehouse.SelectWarehouses"),
            name:'custWarehouse',
            hiddenName:'custWarehouse',
             listeners:{
                'select':{
                      fn:this.populateAllData,
                      scope:this
                }
            }
        });
//         this.wareHouseStore.load();
        
        this.allAccountRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'level'},
            {name: 'leaf'},
            {name: 'openbalance'},
            {name: 'parentid'},
            {name: 'parentname'}
        ]);
        this.allAccountStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.allAccountRec),
    //        url: Wtf.req.account +'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
               ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            }
        });
         
        this.creditTo= new Wtf.form.FnComboBox({
            fieldLabel:(this.isCustomer?"Credit Account*":"Debit Account*"),
            hiddenName:"creditoraccount",
//            anchor:"50%",
            width : 240,
            store: this.allAccountStore,
            valueField:'accid',
            displayField:'accname',
            hidden:!this.isCustBill,
            hideLabel:!this.isCustBill,
            itemCls : (!this.isCustBill)?"hidden-from-item":"",
            allowBlank:!this.isCustBill||this.isOrder,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText"),  //'Select an Account...',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            triggerAction:'all',
//            addNewFn: this.addAccount.createDelegate(this,[this.allAccountStore],true),
            scope:this
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.creditTo.addNewFn=this.addAccount.createDelegate(this,[this.allAccountStore],true);
        
        this.perDiscountStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Percentage',true],['Flat',false]]
        });
        this.fromPOStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        var arrfromLink = new Array();
        
        if(this.isCustomer) {
//            arrfromLink.push(['Sales Order',0]);
            if(this.isOrder && this.isCustomer && !this.quotation){
                arrfromLink.push(['Lease Quotation',0]); // this value is used for setting flag -- isLinkedFromCustomerQuotation in record
                arrfromLink.push(['Replacement Number',1]); // this value is used for setting flag -- isLinkedFromReplacementNumber in record
            } else if(this.quotation){
//                arrfromLink.push(['Vendor Quotation',0]);  Commented Because of No Use of Vendor Quotation in Lease Order 
                arrfromLink.push(['Replacement Number',1]); // this value is used for setting flag -- isLinkedFromReplacementNumber in record
            }else{
                if(Wtf.account.companyAccountPref.withinvupdate){
                    arrfromLink.push(['Delivery Order',1]);    
                }
            }
                        
//            arrfromLink.push(['Customer Quotation',2]);                            
        } else {
            if(this.isOrder){
                arrfromLink.push(['Vendor Quotation',2]);
                arrfromLink.push(['Sales Order',0]);
            } else {
//             arrfromLink.push(['Purchase Order',0]);
                if(Wtf.account.companyAccountPref.withinvupdate){
                    arrfromLink.push(['Goods Receipt',1]);    
                }
//                arrfromLink.push(['Vendor Quotation',2]);
            }
        }
        
        
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value'}],
            data:arrfromLink
        });
        
        this.vendorInvoice=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.inv.invno"),  //'Vendor Invoice Number*',
            name: 'vendorinvoice',
            id:"vendorInvoiceNo"+this.heplmodeid+this.id,
            hidden:this.label=='Vendor Invoice'?false:true,
//            anchor:'50%',
            width : 240,
            maxLength:50,
            scope:this,
            allowBlank:this.checkin
        });      

        
this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
//        labelSeparator:'',
//        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        id:'sequenceFormatCombobox'+this.heplmodeid+this.id,
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStore,
        disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
        width:240,
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformat',
        hiddenName:'sequenceformat',
        listeners:{
            'select':{
                fn:this.getNextSequenceNumber,
                scope:this
            }
        }
            
    });
        this.Number=new Wtf.form.TextField({
            fieldLabel:(this.isPOfromSO?WtfGlobal.getLocaleText("acc.accPref.autoPO"):(this.isSOfromPO)?WtfGlobal.getLocaleText("acc.accPref.autoSO"):(this.isEdit?this.label:this.titlel)) + " " + ((this.isTemplate)?'Number':WtfGlobal.getLocaleText("acc.common.number")),  //,  //this.label+' Number*',
            name: 'number',
            disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),
            id:"invoiceNo"+this.heplmodeid+this.id,
//            anchor:'50%',
            width : 240,
            maxLength:50,
            scope:this,
            allowBlank:this.checkin || (this.isTemplate&&!this.createTransactionAlso),
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenternumber")
        });
        this.PORefNo=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.POrefNo"),  //PO Reference Number',
            name: 'porefno',
            id:"porefno"+this.heplmodeid+this.id,
            hidden:this.isOrder||!this.isCustomer,
            hideLabel:this.isOrder||!this.isCustomer,
            itemCls : (this.isOrder||!this.isCustomer)?"hidden-from-item":"",
//            anchor:'50%',
           width : 240,
            maxLength:45,
            scope:this

        });
        this.Memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo/Note',
            name: 'memo',
            id:"memo"+this.heplmodeid+this.id,
            height:40,
//            anchor:'94%',
            width : 240,
//            allowBlank:false,
            maxLength:2048,
            disabled:this.readOnly,
            readOnly:this.isViewTemplate,
            qtip:(this.record==undefined)?' ':this.record.data.memo,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        trackMouse: true,
                        text: c.qtip
                    });
                }
            }
        });
        
         this.Memo.on('change',function(a,b){
            this.Memo.qtip=b;
            Wtf.QuickTips.register({
                target: a.getEl(),
                trackMouse: true,
                text: a.qtip
            });
        },this);
        
        this.Discount=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:true,
            defaultValue:0,
            hideLabel:true,
            allowBlank:this.isOrder,
            maxLength: 10,
            width:100,
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.discount"),  //'Discount',
            name:'discount',
            id:"discount"+this.heplmodeid+this.id,
            listeners:{
                'change':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
        });
        this.perDiscount= new Wtf.form.ComboBox({
            labelSeparator:'',
            labelWidth:0,
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            store:this.perDiscountStore,
            hidden:true,
            hideLabel:true,
            allowBlank:this.isOrder,
            value:false,
            width:100,
            typeAhead: true,
            forceSelection: true,
            name:'perdiscount',
            hiddenName:'perdiscount',
            listeners:{
                'select':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
        });
         this.sequenceFormatStoreRecDo = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'value'
        },
        {
            name: 'oldflag'
        }
        ]);
  this.sequenceFormatStoreDo = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRecDo),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.isCustomer ?"autodo":"autogro"
            }
        });
        this.sequenceFormatStoreDo.on('load',function(){
            if(this.sequenceFormatStoreDo.getCount()>0){
                var count=this.sequenceFormatStoreDo.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStoreDo.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatComboboxDo.setValue(seqRec.data.id) 
                        break;
                    }
                }
                if(this.sequenceFormatComboboxDo.getValue()!=""){
                    this.getNextSequenceNumberDo(this.sequenceFormatComboboxDo);
                } else{
                    this.no.setValue("");
                    this.no.disable();
                }
            }
        },this);
      if((this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ))//only load when customer Invoice
          this.sequenceFormatStoreDo.load();
         
       this.sequenceFormatComboboxDo = new Wtf.form.ComboBox({            
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStoreDo,
        width:240,
        maxLength:2048,
        typeAhead: true,
        forceSelection: true,
        name:this.isCustomer ?'sequenceformatDo':'sequenceformatGR',
        hiddenName:this.isCustomer ?'sequenceformatDo':'sequenceformatGR',
        hideLabel:!(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) || this.isFixedAsset || this.isLeaseFixedAsset,
        hidden: !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) || this.isFixedAsset || this.isLeaseFixedAsset,
        listeners:{
            'select':{
                fn:this.getNextSequenceNumberDo,
                scope:this
            }
        }
            
    });
    
     this.no=new Wtf.form.TextField({
            fieldLabel:this.isCustomer ?"Delivery Order Number*" :"Goods Receipt Number*" ,
            name:this.isCustomer ? 'numberDo' : 'numberGR',
            scope:this,
            maxLength:45,
            width : 240,
            maxLength:2048,
            hiddenName:this.isCustomer ?'numberDo':'numberGR',
            hideLabel:!(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) || this.isFixedAsset || this.isLeaseFixedAsset,
            hidden: !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) || this.isFixedAsset || this.isLeaseFixedAsset,
//            hideLabel:true,
//            hidden:true,
     //       anchor:'90%',
            allowBlank:false
        });
         this.PORec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'journalentryid'},
            {name:'entryno'},
            {name:'billto'},
            {name:'discount'},
            {name:'shipto'},
            {name:'mode'},
            {name:'billno'},
            {name:'date', type:'date'},
            {name:'duedate', type:'date'},
            {name:'shipdate', type:'date'},
            {name:'personname'},
            {name:'creditoraccount'},
            {name:'personid'},
            {name:'shipping'},
            {name:'othercharges'},
            {name:'taxid'},
            {name:'discounttotal'},
            {name:'discountispertotal',type:'boolean'},
            {name:'currencyid'},
            {name:'amount'},
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'shipvia'},
            {name:'fob'},
            {name:'includeprotax',type:'boolean'},
            {name:'salesPerson'},
            {name:'islockQuantityflag'},
            {name:'replacementQuantity'},
            {name:'isAsset'},
            {name:'contractstatus'},
            {name:'contract'},
            {name:'batchSerialId'},
            {name:'contractId'},
            {name:'agent'},
            {name: 'billingAddressType'},
            {name: 'billingAddress'},
            {name: 'billingCountry'},
            {name: 'billingState'},
            {name: 'billingPostal'},
            {name: 'billingEmail'},
            {name: 'billingFax'},
            {name: 'billingMobile'},
            {name: 'billingPhone'},
            {name: 'billingContactPerson'},
            {name: 'billingRecipientName'},
            {name: 'billingContactPersonNumber'},
            {name: 'billingContactPersonDesignation'},
            {name: 'billingWebsite'},
            {name: 'billingCounty'},
            {name: 'billingCity'},
            {name: 'shippingAddressType'},
            {name: 'shippingAddress'},
            {name: 'shippingCountry'},
            {name: 'shippingState'},
            {name: 'shippingCounty'},
            {name: 'shippingCity'},
            {name: 'shippingEmail'},
            {name: 'shippingFax'},
            {name: 'shippingMobile'},
            {name: 'shippingPhone'},
            {name: 'shippingPostal'},
            {name: 'shippingContactPersonNumber'},
            {name: 'shippingContactPersonDesignation'},
            {name: 'shippingWebsite'},
            {name: 'shippingRecipientName'},
            {name: 'shippingContactPerson'},
            {name: 'shippingRoute'},
            {name:' vendcustShippingAddress'},
            {name:' vendcustShippingCountry'},
            {name:' vendcustShippingState'},
            {name:' vendcustShippingCounty'},
            {name:' vendcustShippingCity'},
            {name:' vendcustShippingEmail'},
            {name:' vendcustShippingFax'},
            {name:' vendcustShippingMobile'},
            {name:' vendcustShippingPhone'},
            {name:' vendcustShippingPostal'},
            {name:' vendcustShippingContactPersonNumber'},
            {name:' vendcustShippingContactPersonDesignation'},
            {name: 'vendcustShippingWebsite'},
            {name:' vendcustShippingContactPerson'},
            {name:' vendcustShippingRecipientName'},
            {name:' vendcustShippingAddressType'}
        ]);
        this.POStoreUrl = "";
        var closeFlag = true;
        if(this.businessPerson=="Customer"){
            //mode:(this.isCustBill?52:42)
            if(this.quotation){                
                this.POStoreUrl = "ACCPurchaseOrderCMN/getQuotations.do"
            }else if(this.isOrder) {
                  if(this.isLeaseFixedAsset){
                      this.POStoreUrl = "ACCSalesOrderCMN/getQuotations.do";
                  }else{
                      this.POStoreUrl = "ACCSalesOrderCMN/getQuotations.do"
                      if(this.isSOfromPO){
                          closeFlag = false;
                          this.POStoreUrl = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrders.do":"ACCPurchaseOrderCMN/getPurchaseOrders.do";
                      }
                  }
            } else {
                this.POStoreUrl = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrders.do":"ACCSalesOrderCMN/getSalesOrders.do";
            }
            
            
        }else if(this.businessPerson=="Vendor"){
            if(this.isOrder) {
                if(this.isPOfromSO){
                    closeFlag = false;
                }    
                this.POStoreUrl = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrders.do":"ACCSalesOrderCMN/getSalesOrders.do";
            } else {
                this.POStoreUrl = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrders.do":"ACCPurchaseOrderCMN/getPurchaseOrders.do";
            }
        }
        this.POStore = new Wtf.data.Store({
            url:this.POStoreUrl,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                mode:(this.isCustBill?52:42),
                closeflag:closeFlag
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.PORec)
        });
        var colModelArray = GlobalColumnModelForReports[this.moduleid];
        if(colModelArray){
           for(var cnt = 0;cnt < colModelArray.length;cnt++){
               var fieldname = colModelArray[cnt].fieldname;
               var newField = new Wtf.data.Field({
                   name:fieldname.replace(".",""),
//                   sortDir:'ASC',
                   type:colModelArray[cnt].fieldtype == 3 ?  'date' : (colModelArray[cnt].fieldtype == 2?'float':'auto'),
                   dateFormat:colModelArray[cnt].fieldtype == 3 ?  'time' : undefined
               });
               this.POStore.fields.items.push(newField);
               this.POStore.fields.map[fieldname]=newField;
               this.POStore.fields.keys.push(fieldname);
           }
           this.POStore.reader = new Wtf.data.KwlJsonReader(this.POStore.reader.meta, this.POStore.fields.items);
       }
        this.fromPO= new Wtf.form.ComboBox({
            triggerAction:'all',
            hideLabel:this.cash|| (this.quotation&&!this.isCustomer) || (this.isOrder && this.isCustBill) || this.isTemplate ||this.isConsignment,
            hidden:this.cash|| (this.quotation&&!this.isCustomer)|| (this.isOrder && this.isCustBill) || this.isTemplate ||this.isConsignment,
            mode: 'local',
            valueField:'value',
            displayField:'name',
            disabled:this.isEdit?false:true,
            store:this.fromPOStore,
            id: "linkToOrder"+this.heplmodeid+this.id,
            fieldLabel:((!this.isCustBill && !this.isOrder && !this.cash)?WtfGlobal.getLocaleText("acc.field.Link"):(this.isOrder && this.isCustomer)? (this.isSOfromPO)?"Link to Purchase Order":(this.quotation ? "Link to" : "Link to Customer Quotation") :(this.isOrder && !this.isCustomer)?WtfGlobal.getLocaleText("acc.field.Link"): (this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.linkToSO"):WtfGlobal.getLocaleText("acc.invoice.linkToPO"))) ,  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:this.isOrder,
            value:false,
            width:50,
            typeAhead: true,
            forceSelection: true,
            name:'prdiscount',
            hiddenName:'prdiscount',
            listeners:{
                'select':{
                    fn:this.enablePO,
                    scope:this
                }
            }
        });
       
        
     this.usersRec = new Wtf.data.Record.create([
        {name: 'userid'},
        {name: 'username'},
        {name: 'fname'},
        {name: 'lname'},
        {name: 'image'},
        {name: 'emailid'},
        {name: 'lastlogin',type: 'date'},
        {name: 'aboutuser'},
        {name: 'address'},
        {name: 'contactno'},
        {name: 'rolename'},
        {name: 'roleid'}
    ]);

    this.userds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },this.usersRec),
        url : "ProfileHandler/getAllUserDetails.do",
        baseParams:{
            mode:11
        }
    });
    if(!(this.isOrder && this.quotation)){ //
         this.isCustomer ? Wtf.salesPersonFilteredByCustomer.load() : Wtf.agentStore.load();    
    }
    
//    this.salesPersonStore

    this.users= new Wtf.form.ExtFnComboBox({
            triggerAction:'all',
              mode:'remote',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            extraFields:[],//it is required when  ExtFnComboBox component
            id:"salesperson"+this.heplmodeid+this.id,
            store:this.isCustomer ? Wtf.salesPersonFilteredByCustomer : Wtf.agentStore,
            addNoneRecord: true,
//            anchor: '94%',
            width : 240,
//            typeAhead: true,
            forceSelection: true,
            hidden:!this.isCustomer,
            hideLabel:!this.isCustomer,
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.invoiceList.salesPerson") : WtfGlobal.getLocaleText("acc.masterConfig.20"),
            emptyText: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectSalesPerson") : WtfGlobal.getLocaleText("acc.field.SelectAgent"),
//            hideLabel:(this.isOrder && this.quotation),
//            hidden:( this.isOrder && this.quotation),
            name:this.isCustomer ? 'salesPerson' : 'agent',
            hiddenName:this.isCustomer ? 'salesPerson' : 'agent',
            activated:this.isCustomer ? true : false
        });
        
         this.users.on('beforeselect', function (combo, record, index) {
        if (this.isCustomer) {
            return validateSelection(combo, record, index);
        } else {
            return true;
        }
    }, this);
        
            this.users.addNewFn=this.addSalesPerson.createDelegate(this);
            
        this.users.store.on('load', this.setDefaultSalesPerson, this);
      
      this.lockQuantity= new Wtf.form.Checkbox({
            name:'lockQuantity',
             id:'lockQuantitySO'+this.heplmodeid+this.id,
            hiddeName:'lockQuan',
            fieldLabel:WtfGlobal.getLocaleText("Block Quantity"),  
            checked:this.isCustomer?true:false,
            hideLabel:(!this.isCustomer || (this.isCustomer && (!this.isOrder || this.quotation))),
            hidden:( !this.isCustomer || (this.isCustomer && (!this.isOrder || this.quotation))),                        
            cls : 'custcheckbox',
            width: 10
        }); 
//        this.lockQuantity.on('check',function(o,newval,oldval){
//                if(this.lockQuantity.getValue()){
//                    this.wareHouseCombo.disable();
//                }else{
//                    this.wareHouseCombo.enable();
//                }
//          },this);
       
       
       
        this.warehouseStoreRec = new Wtf.data.Record.create([//  warehouse record
            {name: 'id'},
            {name: 'name'},
            {name: 'parentid'},
            {name: 'company'},
            {name: 'parentname'},
            {name: 'warehouse'},
           
            
        ]);
        this.reqtestWareHouseStore = new Wtf.data.Store({  //  warehouse store for combo box
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.warehouseStoreRec),
        url:"ACCMaster/getWarehouseItems.do",
        baseParams:{
            movementtypeid:(this.movmentType.getValue()!=undefined && this.movmentType.getValue() !="")?this.movmentType.getValue():""
        }
        });
        
     this.reqtestWareHouseStore.on('beforeload',function(s,o){
//                if(!o.params)o.params={};
                var currentBaseParams = this.reqtestWareHouseStore.baseParams;
                currentBaseParams.movementtypeid=(this.movmentType.getValue()!=undefined && this.movmentType.getValue() !="")?this.movmentType.getValue():"";
                this.reqtestWareHouseStore.baseParams=currentBaseParams; 
            },this); 
    this.reqtestWareHouseStore.load();

    this.wareHouseCombo = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),
        valueField:'id',
        displayField:'name',
        store:this.reqtestWareHouseStore,
        lastQuery:'',
        disabled:true,
        allowBlank:(this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId && this.isMovementWarehouseMapping ==true && this.isRequestApprovalFlow==true )?false:true,
//        allowBlank: this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId?false:true,
        typeAhead: true,
        forceSelection: true,
        name:'warehouse',
        hiddenName:'warehouse',
        hideLabel:(this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId && this.isMovementWarehouseMapping ==true && this.isRequestApprovalFlow==true)?false:true,
        hidden:(this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId && this.isMovementWarehouseMapping ==true && this.isRequestApprovalFlow==true)?false:true,                        
        width: 250
    });

    if(Wtf.account.companyAccountPref.activateInventoryTab){
        this.wareHouseCombo.on('select',function(){
            this.locationStore.load({
                params:{
                    storeid:this.wareHouseCombo.getValue()
                }
            });
            this.locationMultiSelect.enable();
            this.Grid.requestWarehouse=this.wareHouseCombo.getValue();
        },this);
    }
        
    this.locationRec = new Wtf.data.Record.create([
    {
        name:"id"
    },
    {
        name:"name"
    },
    {
        name: 'parentid'
    },
    {
        name: 'parentname'
    }
    ]);
    this.locationReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.locationRec);
    var locationStoreUrl="ACCMaster/getLocationItems.do"
    if(Wtf.account.companyAccountPref.activateInventoryTab){
        locationStoreUrl="ACCMaster/getLocationItemsFromStore.do";
    }
    this.locationStore = new Wtf.data.Store({
        url:locationStoreUrl,
        reader:this.locationReader
    });
    this.locationStore.load();
    
    //    this.locationMultiSelect = new Wtf.common.Select({
    this.locationMultiSelect = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        multiSelect:false,
        fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
        valueField:'id',
        displayField:'name',
        lastQuery:'',
        store:this.locationStore,
        typeAhead: true,
        allowBlank: (this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId && this.isMovementWarehouseMapping ==true && this.isRequestApprovalFlow==true)?false:true,
        disabled:true,
        forceSelection: true,
        hirarchical:true,
        name:'location',
        hiddenName:'location',
        hideLabel:(this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId && this.isMovementWarehouseMapping ==true &&  this.isRequestApprovalFlow==true)?false:true,
        hidden:(this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId && this.isMovementWarehouseMapping ==true &&  this.isRequestApprovalFlow==true)?false:true,                        
        width: 250
    });

        this.locationMultiSelect.on('beforeselect', function (combo, record, index) {
            this.locationBeforeSelect = combo.getValue();
        }, this);
        
        this.locationMultiSelect.on('change',function (combo, record, index) {
            var dataLength = this.Grid.getStore().data.length;
            for (var i = 0; i < dataLength; i++) {
                var rec = this.Grid.getStore().getAt(i);
                rec.set("batchdetails", "");
            }
            Wtf.dupsrno.length=0;
        }, this);
        
        this.locationMultiSelect.on('select', function (combo, record, index) {
            this.confirmResetBatchDetailData(combo,this.locationBeforeSelect);
            this.Grid.requestLocation=this.locationMultiSelect.getValue();
        }, this);
        
        this.wareHouseCombo.on('beforeselect', function (combo, record, index) {
            this.warehouseBeforeSelect = combo.getValue();
        }, this);
        
        this.wareHouseCombo.on('clearval', function (combo, record, index) {
            var dataLength = this.Grid.getStore().data.length;
            for (var i = 0; i < dataLength; i++) {
                var rec = this.Grid.getStore().getAt(i);
                rec.set("batchdetails", "");
            }
            Wtf.dupsrno.length=0;
        }, this);
        
        this.wareHouseCombo.on('select', function (combo, record, index) {
            this.confirmResetBatchDetailData(combo,this.warehouseBeforeSelect);
        }, this);
     
        this.copyAddress= new Wtf.form.Checkbox({
            name:'copyadress',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.sameasbillingadd"),  //'Copy Address',
            checked:false,
            hideLabel:(this.quotation || !this.isCustomer || this.isOrder),
            hidden:(this.quotation || !this.isCustomer || this.isOrder),
            cls : 'custcheckbox',
            width: 10
        });
        
        this.generateReceipt= new Wtf.form.Checkbox({
            name:'generateReceipt',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.generateReceipt"),  //'Generate Receipt',
            checked:false,
            hideLabel:(this.quotation || this.isOrder || !this.cash || !this.isCustomer),//|| !this.isCustomer || this.isOrder),
            hidden:(this.quotation || this.isOrder || !this.cash || !this.isCustomer),//|| !this.isCustomer || this.isOrder),
            cls : 'custcheckbox',
            width: 10
        });
        
        this.capitalGoodsAcquired= new Wtf.form.Checkbox({
            name:'isCapitalGoodsAcquired',
            id:"isCapitalGoodsAcquired"+this.heplmodeid+this.id,
            fieldLabel:WtfGlobal.getLocaleText("acc.capital.goods.acquired"),  //'Capital Goods Acquired',
            checked: false,
            hideLabel: !(!this.isExpenseInv && this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && Wtf.account.companyAccountPref.countryid=='137'),// if country is Malasia and this is an vendor Invoice and not an expense invoice then only it will be showns
            hidden: !(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && Wtf.account.companyAccountPref.countryid=='137'),
            cls : 'custcheckbox',
            width: 10
        });
        
        this.autoGenerateDO= new Wtf.form.Checkbox({
            name:'autogenerateDO',
            id:"autogenerateDO"+this.heplmodeid+this.id,
            fieldLabel:this.isCustomer ? WtfGlobal.getLocaleText("acc.cust.generateDO") : WtfGlobal.getLocaleText("acc.vend.generateGR"),  //'Generate Delivery Order',
            checked:this.GRDOSettings != null ? this.GRDOSettings : false,
            hideLabel:(this.isWithInvUpdate == null? true: !this.isWithInvUpdate) || !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) || this.isFixedAsset || this.isLeaseFixedAsset ||this.isConsignment,
            hidden:(this.isWithInvUpdate == null? true: !this.isWithInvUpdate) || !(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) || this.isFixedAsset || this.isLeaseFixedAsset || this.isConsignment,
            cls : 'custcheckbox',
            width: 10
        });        

        this.autoGenerateDO.on('check',function(o,newval,oldval){
            if(this.autoGenerateDO.getValue()){
              this.showDO();
            }else{
               this.hideDO();
            }            
        },this);
                 
        this.templateRec = new Wtf.data.Record.create([
            {name: 'tempid'},
            {name: 'tempname'}
        ]);
        
        this.templateStore = new Wtf.data.Store({
            url : "ExportPDF/getAllReportTemplate.do",
            
            method: 'GET',
            baseParams : {
                templatetype : this.doctype
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.templateRec)
        });
         this.templateStore.load();
         this.templateStore.on("load", function() {
            if (!this.isEdit)
                this.template.setValue(Wtf.Acc_Basic_Template_Id);
        }, this);
        this.template= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.grid.header.template")+"*",
            hiddenName:"template",
//            anchor:"94%",
            width : 240,
            store: this.templateStore,
            valueField:'tempid',
            displayField:'tempname',
//            itemCls : (!this.isCustBill)?"hidden-from-item":"",
//            allowBlank:!this.isCustBill||this.isOrder,
//            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.grid.template.emptyText"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            allowBlank:true, 
            hidden:true, 
            hideLabel:true,      
            selectOnFocus:true,
           // value : this.isEdit ? this.record.data.templatename :'',
            triggerAction:'all',
//            addNewFn: this.addAccount.createDelegate(this,[this.allAccountStore],true),
            scope:this,
            listeners:{
                'change':{
                    fn:this.setTemplateID,
                    scope:this
                }
            }
        });
        this.templateID=new Wtf.form.Hidden({
        	scope:this,
        	value: this.isEdit ? this.record.data.templateid : ''
        });
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create))
        this.template.addNewFn=this.addInvoiceTemplate.createDelegate(this,[this.templateStore],true);
        
        this.includeTaxStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        this.partialInvoiceStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        this.includeProTax= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            store:this.includeTaxStore,
           // id: "linkToOrder"+this.id+this.heplmodeid,
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.productTax"),//"Include Product Tax",
            id:"includeprotax"+this.heplmodeid+this.id,
          //  allowBlank:this.isOrder,
            value:(this.isEdit?true:false),
//            anchor:'94%',
            width : 240,
            typeAhead: true,
            forceSelection: true,
            name:'includeprotax',
            hiddenName:'includeprotax',
            listeners:{
                'select':{
                    fn:this.includeProTaxHandler,
                    scope:this
                }
            }
        });
        
        this.validTillDate = new Wtf.form.DateField({
            fieldLabel : WtfGlobal.getLocaleText("acc.common.validTill"),  //"Valid Till",
            format : WtfGlobal.getOnlyDateFormat(),
            name : 'validdate',
            id : "validdate"+this.heplmodeid+this.id,
            width : 240,
            hidden : !(this.moduleid==22 || this.moduleid==23),
            hideLabel : !(this.moduleid==22 || this.moduleid==23)
//            anchor:'94%'
        });
        
        this.partialInvoiceCmb= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            hidden : (this.isCustomer && !this.isCustBill && !this.isOrder && !this.cash) ? false : true,
            hideLabel : (this.isCustomer && !this.isCustBill && !this.isOrder && !this.cash) ? false : true,
            displayField:'name',
            store:this.partialInvoiceStore,
           // id: "linkToOrder"+this.id+this.heplmodeid,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.IsPartialInvoice"),
            id:"isPartialInv"+this.heplmodeid+this.id,
          //  allowBlank:this.isOrder,
            value:false,
//            anchor:'94%',
            width : 240,
            disabled : true,
            typeAhead: true,
            forceSelection: true,
            name:'partialinv',
            hiddenName:'partialinv',
            listeners:{
                'select':{
                    fn:this.showPartialDiscount,
                    scope:this
                }
            }
        });
        /*
         *hideLabel:(!this.isCustBill && !(this.isOrder&&this.isCustomer) && !this.cash )?false:true,
            hidden:(!this.isCustBill && !(this.isOrder&&this.isCustomer) && !this.cash )?false:true,
         **/
        var emptyText = "Select VQ/SO";
        if(!this.isCustBill){
            if(this.isOrder && !this.isCustomer) {
                emptyText = "Select VQ/SO";
            } else if(this.isOrder&&this.isCustomer && !this.quotation){
                emptyText = "Select LQ/RN";
            } else if(this.isCustomer && this.quotation){
                emptyText = "Select RN";//VQ/RN
            } else {
                emptyText = Wtf.account.companyAccountPref.withinvupdate? (this.isCustomer? WtfGlobal.getLocaleText("acc.field.SelectaDO") : WtfGlobal.getLocaleText("acc.field.SelectaGR")) : (this.isCustomer? "Select SO/CQ" : "Select PO/VQ");
            }
        }
        this.fromLinkCombo= new Wtf.form.ComboBox({
            name:'fromLinkCombo',
            triggerAction:'all',
            hideLabel:(this.isCustBill  || this.cash || this.isTemplate || this.isConsignment)?true:false,
            hidden:(this.isCustBill || this.cash || this.isTemplate || this.isConsignment)?true:false,
            mode: 'local',
            valueField:'value',
            id:'fromLinkComboId'+this.heplmodeid+this.id,
            displayField:'name',
            disabled:true,
            store:this.fromlinkStore,                        
            emptyText: emptyText,//Wtf.account.companyAccountPref.withinvupdate? (this.isCustomer? "Select SO/DO" : "Select PO/GR") : (this.isCustomer? "Select SO" : "Select PO"),
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Linkto"),  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:false,            
//            value:false,            
            typeAhead: true, 
            width:130,
            forceSelection: true,                        
            selectOnFocus:true,           
            scope:this,
            listeners:{
                'select':{
                    fn:this.enableNumber,
                    scope:this
                }
            }
        });
        
//        this.PO= new Wtf.form.FnComboBox({
//            fieldLabel:"Number",//(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer)?"Number ":((this.isOrder && this.isCustomer)? WtfGlobal.getLocaleText("acc.invoice.QO") : (this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.SO"):WtfGlobal.getLocaleText("acc.invoice.PO"))) ,  //(this.isCustomer?"SO":"PO")+" Number",
//            hiddenName:"ordernumber",
//            id:"orderNumber"+this.heplmodeid+this.id,
//            store: this.POStore,
//            valueField:'billid',
//            hideLabel:this.cash|| this.quotation|| (this.isOrder && this.isCustBill),
//            hidden:this.cash||this.quotation|| (this.isOrder && this.isCustBill),
//            displayField:'billno',
//            disabled:true,
//            emptyText:this.isOrder ? (( this.isCustomer)? WtfGlobal.getLocaleText("acc.inv.QOe") : WtfGlobal.getLocaleText("acc.inv.SOe")) : (Wtf.account.companyAccountPref.withinvupdate ? (this.isCustomer?"Select SO/DO":"Select PO/GR") : (this.isCustomer?WtfGlobal.getLocaleText("acc.inv.SOe"):WtfGlobal.getLocaleText("acc.inv.POe"))),
//            mode: 'local',
//            typeAhead: true,
//            forceSelection: true,
//            selectOnFocus:true,
//            anchor:"50%",
//            triggerAction:'all',
////            addNewFn:this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true),
//            scope:this,
//            listeners:{
//                'select':{
//                    fn:this.populateData,
//                    scope:this
//                }
//            }
//        });

           this.MSComboconfig = {
                hiddenName:"ordernumber",
                //id:"orderNumber"+this.heplmodeid+this.id,         
                store: this.POStore,
                valueField:'billid',
                hideLabel:this.cash|| (this.quotation&&!this.isCustomer)|| (this.isOrder && this.isCustBill) || this.isTemplate || this.isConsignment,
                hidden:this.cash||(this.quotation&&!this.isCustomer)|| (this.isOrder && this.isCustBill) || this.isTemplate || this.isConsignment,
                displayField:'billno',
                disabled:true,
                emptyText:this.isOrder ? (( this.isCustomer)? ((this.isLeaseFixedAsset)?((this.quotation)?WtfGlobal.getLocaleText("acc.inv.VQOrReplacement"):WtfGlobal.getLocaleText("acc.inv.QOeOrReplacement")):WtfGlobal.getLocaleText("acc.inv.QOe")) : "Select VQ/SO") : (Wtf.account.companyAccountPref.withinvupdate ? (this.isCustomer?"Select DO":"Select GR") : (!this.isCustBill)?(this.isCustomer?"Select SO/CQ":"Select PO/VQ"):(this.isCustomer?WtfGlobal.getLocaleText("acc.inv.SOe"):WtfGlobal.getLocaleText("acc.inv.POe"))),
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,                            
                allowBlank:false,
                triggerAction:'all',
                scope:this
//                listeners:{                       
//                    'blur':{
//                        fn:this.populateData,
//                        scope:this
//                    }
//                }
            };

        this.PO = new Wtf.common.Select(Wtf.applyIf({
             multiSelect:true,
             fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,
             id:"poNumberID"+this.heplmodeid+this.id ,
             forceSelection:true,
             width:240
        },this.MSComboconfig));
        
        this.PO.on("clearval",function(){
            if(this.PO.getValue()=="" && !this.isEdit && !this.handleEmptyText){            
                this.Grid.getStore().removeAll();            
                this.Grid.addBlankRow();      
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id)) {
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue('');
                    }
                }
            }
            this.handleEmptyText=false;
        },this);

        if(!WtfGlobal.EnableDisable(this.soUPermType, this.soPermType))
            this.PO.addNewFn=this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true)
        this.DueDate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.dueDate"),//'Due Date*',
            name: 'duedate',
            id: "duedate"+this.heplmodeid+this.id,
            itemCls : (this.cash||this.isOrder)?"hidden-from-item":"",
            hideLabel:this.cash||this.isOrder,
            hidden:this.cash||this.isOrder,
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank:false,
            width : 240
//            anchor:'94%'
        });

        this.billDate= new Wtf.form.DateField({
            fieldLabel:(this.isPOfromSO?WtfGlobal.getLocaleText("acc.accPref.autoPO"):(this.isSOfromPO)?WtfGlobal.getLocaleText("acc.accPref.autoSO"):(this.isEdit?this.label:this.titlel)) +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id:"invoiceDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
            maxValue:this.isOpeningBalanceOrder?this.getFinancialYRStartDatesMinOne(true):null,
//            anchor:'50%',
            width : 240,
            listeners:{
                'change':{
                    fn:this.updateDueDate,
                    scope:this
                }
            },
            allowBlank:(this.isTemplate&&!this.createTransactionAlso) 
        });
        this.shipDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShipDate"),
            format:WtfGlobal.getOnlyDateFormat(),
            id:"shipdate"+this.heplmodeid+this.id,
            hidden:!this.isCustomer,
            hideLabel:!this.isCustomer,
            name: 'shipdate',
            width : 240,
            minValue: (this.isCustomer && !this.isEdit)?Wtf.serverDate.clearTime(true):(this.isCustomer && this.isEdit)?(this.record.data['shipdate']!=""?this.record.data['shipdate'].clearTime(true):""):"",
             listeners:{
                'change':{
                    fn:this.updateDueDate,
                    scope:this
                }
            }
//            anchor:'94%'
        });
        this.shipvia = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShipVia"),
            name: 'shipvia',
            id:"shipvia"+this.heplmodeid+this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            hidden:!this.isCustomer,
            hideLabel:!this.isCustomer,
            scope: this
        });
        
        this.fob = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.FOB"),
            name: 'fob',
            id:"fob"+this.heplmodeid+this.id,
//            anchor: '94%',
            width : 240,
            hidden:!this.isCustomer,
            hideLabel:!this.isCustomer,
            maxLength: 255,
            scope: this
        });
        
        var isbchlFields=(this.quotation||this.isCustomer || !this.isOrder ||this.isCustBill || (BCHLCompanyId.indexOf(companyid) == -1));
        this.youtReftxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.yourref.label"), //'Vendor Invoice Number*',
            name: 'poyourref',
            hidden: isbchlFields ,
            hideLabel: isbchlFields ,
            id: "poyourref" + this.heplmodeid + this.id,
//            anchor: "50%",
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.delydatetxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.delydate.label"), //'Vendor Invoice Number*',
            name: 'delydate',
            hidden: isbchlFields ,
            hideLabel: isbchlFields ,
            id: "delydate" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.delytermtxt = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.delyterm.label"), //'Vendor Invoice Number*',
            name: 'delyterm',
            hidden: isbchlFields,
            hideLabel:isbchlFields ,
            id: "delyterm" + this.heplmodeid + this.id,
//            anchor: "50%",
            width : 240,
            height: 40,
            maxLength: 255,
            scope: this
        });
        this.invoiceTotxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.invoiceto.label"), //'Vendor Invoice Number*',
            name: 'invoiceto',
            hidden:isbchlFields ,
            hideLabel: isbchlFields ,
            id: "invoiceto" + this.heplmodeid + this.id,
//            anchor: '50%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.projecttxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.project.label"), //'Vendor Invoice Number*',
            name: 'project',
            hidden: isbchlFields ,
            hideLabel: isbchlFields ,
            id: "project" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.depttxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.dept.label"), //'Vendor Invoice Number*',
            name: 'podept',
            hidden:isbchlFields ,
            hideLabel: isbchlFields ,
            id: "podept" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.requestortxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.requestor.label"), //'Vendor Invoice Number*',
            name: 'requestor',
            hidden: isbchlFields ,
            hideLabel:isbchlFields ,
            id: "requestor" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        this.mernotxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.merno.label"), //'Vendor Invoice Number*',
            name: 'merno',
            hidden: isbchlFields,
            hideLabel: isbchlFields ,
            id: "merno" + this.heplmodeid + this.id,
//            anchor: '94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        chkLineLevelCostCenterload();
        this.CostCenter= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.costCenter"),//"Cost Center",
            hiddenName:"costcenter",
            id:"costcenter"+this.heplmodeid+this.id,
            store: Wtf.LineLevelCostCenterStore,
            valueField:'id',
            displayField:'name',
            extraComparisionField:'ccid', 
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['ccid']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            isProductCombo: true,
            maxHeight:250,
            lastQuery:'',
            hirarchical:true,
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
//            anchor:"50%",
            width : 240,
            triggerAction:'all',
            addNewFn:this.addCostCenter,
            scope:this,
            hidden: this.quotation,
            hideLabel: this.quotation
        });
        
//        this.CostCenter.listWidth=300;
        
          this.fromdate= new Wtf.ExDateFieldQtip({
                fieldLabel:WtfGlobal.getLocaleText("acc.consignment.fromdate"),
                format:WtfGlobal.getOnlyDateFormat(),
                hideLabel:(this.isCustomer && !this.isOrder) || !this.isCustomer,
                hidden:(this.isCustomer && !this.isOrder) || !this.isCustomer,
                allowBlank:(Wtf.account.companyAccountPref.activatefromdateToDate && this.isCustomer && this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId)?false:true,
                name: 'fromdate',
                width : 240
//                anchor:'75%'
            });
            this.todate = new Wtf.ExDateFieldQtip({
                fieldLabel: WtfGlobal.getLocaleText("acc.consignment.todate"),
                name: 'todate',
                hideLabel:(this.isCustomer && !this.isOrder) || !this.isCustomer,
                hidden:(this.isCustomer && !this.isOrder) || !this.isCustomer,
                format : WtfGlobal.getOnlyDateFormat(),
                allowBlank:(Wtf.account.companyAccountPref.activatefromdateToDate && this.isCustomer && this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId)?false:true,
                maxLength: 255,
//                anchor:'75%',
               width : 240,
                scope: this
            });
         this.todate.on('blur', function(){
             if(this.todate.getValue()<this.fromdate.getValue()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),"To Date Should Be greater than From Date"], 3);
                 this.todate.setValue(this.fromdate.getValue());
             }
             
               }, this);
        var itemArr=[];
        if(this.isTemplate){
            itemArr.push(this.moduleTemplateName,this.createAsTransactionChk);
        }
            itemArr.push(this.templateModelCombo, this.Name, this.Currency,{
                layout:'column',
                border:false,
                defaults:{border:false},items:[ {
                    layout:'form',
                    ctCls : (this.cash||this.isOrder)?"hidden-from-item":"",
                    width:215,
                    items:this.fromPO
                },{
                    width:210,
                    layout:'form',
                    labelWidth:50,
                    items:this.fromLinkCombo
               }]},this.PO,this.warehouses,this.movmentType,this.sequenceFormatCombobox,this.Number,this.billDate,
            this.PORefNo, this.CostCenter,this.fromdate,this.todate,this.youtReftxt,this.delytermtxt,this.invoiceTotxt);
          this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm"+this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: this.moduleid,
            isEdit: this.isEdit,
            record: this.record,
            isViewMode:this.readOnly
        });
       var ht=(this.isOrder?(Wtf.isIE?260:360):(Wtf.isIE?360:300));
       if(this.isCustBill)ht+=25;
       if(!(this.quotation || !this.isCustomer || this.isOrder)) {
           ht+=160;
       }
       
       if(this.isTemplate){
           ht+=(this.isOrder?100:10);
       }
       
       if(this.createTransactionAlso){
           ht+=50;
       }
       //if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer)ht+=25;
       
       /*if(this.moduleid=='2' || this.moduleid=='3')
           ht+=90;
       */
       
       // For link the Vendor Invoice Number (landed invoice)
       this.InvoiceRec = Wtf.data.Record.create ([
           {name:'billid'},
           {name:'billno'}
       ]);
       this.InvoiceStoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
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
               excludeInvoiceId : (this.isEdit && !this.copyInv)? this.record.data.billno : "",
               report:true,
               companyids:companyids,
               gcurrencyid:gcurrencyid,
               userid:loginid
           },
           reader: new Wtf.data.KwlJsonReader({
               root: "data",
               totalProperty:'count'
           },this.InvoiceRec)
       });
       if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {           
           this.InvoiceStore.load();           
       }
       this.invoiceList = new Wtf.form.ComboBox({
           fieldLabel:WtfGlobal.getLocaleText("acc.invoice.consignmentNumber"), // "Consignment Number"
           id: "consignmentnumber" + this.heplmodeid + this.id,
           store: this.InvoiceStore,
           displayField:'billno',
           valueField:'billid',
           emptyText:WtfGlobal.getLocaleText("acc.invoice.consignmentNumberEmptyText"), // 'Select Consignment Number',
           mode: 'local',
           width: 240,
           name:'landedInvoiceNumber',
           hiddenName:'landedInvoiceNumber',
           hidden: true,//!(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash),
           hideLabel: true,//!(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash),
           triggerAction: 'all',
           forceSelection: true,
           selectOnFocus:true
       });
       var ArrItemsMain=[];
       ArrItemsMain.push({
                    layout:'column',
                    border:false,
                    defaults:{border:false},
                    items:[{
                        layout:'form',
                        columnWidth:0.55,
                        border:false,
                        items:itemArr
                    },{
                        layout:'form',
                        columnWidth:0.45,
                        border:false,
                        items:[this.shipDate,this.Term,this.DueDate,{
                            itemCls : "",
                            layout:'column',
                            border:false,
                            defaults:{border:false},
                            items:[{
                                layout:'form',
                                columnWidth:0.55,
                                items:this.Discount
                            },{
                                columnWidth:0.45,
                                layout:'form',
                                items:this.perDiscount
                           }]
                        },this.Memo,this.shipvia, this.fob, this.includeProTax, this.validTillDate, this.partialInvoiceCmb,this.template,this.templateID,this.users,this.lockQuantity,this.wareHouseCombo,this.locationMultiSelect,this.invoiceList,this.generateReceipt,this.capitalGoodsAcquired,this.autoGenerateDO,this.sequenceFormatComboboxDo,this.no,this.delydatetxt,this.projecttxt,this.depttxt,this.requestortxt,this.mernotxt]
                    }]
            });
//       if(this.moduleid == Wtf.Acc_Lease_Order && this.isLeaseFixedAsset){
            ArrItemsMain.push(this.tagsFieldset);
//       }
       this.NorthForm=new Wtf.form.FormPanel({
            region:'north',
            autoHeight:true,
            id:"northForm"+this.id,
            border:false,
            disabledClass:"newtripcmbss",
//            disabled:this.isViewTemplate ||this.readOnly,
            items:[{
                layout:'form',
                baseCls:'northFormFormat',
                labelWidth:155,
                cls:"visibleDisabled",
                items:ArrItemsMain
            }]
        });
        this.NorthForm.on('render',function(){
            this.termds.load({
                params: {               
                    cash_Invoice:this.cash
                }
            });
        this.termds.on("load",function(){
            if(this.autoGenerateDO.getValue() ){
                this.showDO();
            }else{
                this.hideDO();
            }  
            if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId)&&this.isFixedAsset ){
                WtfGlobal.hideFormElement(this.invoiceList);
            }
        },this);
        
        
        },this);
        if(this.isViewTemplate || this.readOnly){this.setdisabledbutton();}
        this.productDetailsTplSummary=new Wtf.XTemplate(
            '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            '<div><hr class="templineview"></div>',
            '<div>',
            '<table width="100%">'+
            '<tr>'+
            '<td style="width:25%;"><b>Product Name:</b></td><td style="width:55%;"><span wtf:qtip="{productname}">'+Wtf.util.Format.ellipsis('{productname}',60)+'</span></td>'+                   
            '</tr>'+
            '<tr>'+
            '<td><b>In Stock: </b></td><td style="width:10%;">{qty}</td>'+
            "<td><b>Open PO: </b></td><td style='width:10%;'><a href='#' onclick='Showproductdetails(\"{productid}\",\"{productname}\",false)'>{poqty}</a></td>"+  
            "<td><b>Open SO: </b></td><td style='width:30%;'><a href='#' onclick='Showproductdetails(\"{productid}\",\"{productname}\",true)'>{soqty}</a></td>"+         //provided link on wich we will get product quantity details
            '</tr>'+
            '</table>'+
            '</div>',            
            '<div><hr class="templineview"></div>',                        
            '</div>'
        );
    var blockSpotRateLink_first = "";
    var blockSpotRateLink_second = "";
    if(!Wtf.account.companyAccountPref.activateToBlockSpotRate){ // If activateToBlockSpotRate is set then block the Spot Rate Links
        blockSpotRateLink_first = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div><div style='padding-left:30px;padding-top:5px;padding-bottom:10px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{foreigncurrency}\",\"{basecurrency}\",\"{revexchangerate}\",\"foreigntobase\")'wtf:qtip=''>{foreigncurrency} to {basecurrency}</a>";
        blockSpotRateLink_second = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div> <div style='padding-left:30px;padding-top:5px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\",\"basetoforeign\")'wtf:qtip=''>{basecurrency} to {foreigncurrency}</a></div>";
    }
    this.southCenterTplSummary = new Wtf.XTemplate(
        "<div> &nbsp;</div>", //Currency:
        '<tpl if="editable==true">',
        "<b>" + WtfGlobal.getLocaleText("acc.invoice.msg8") + "</b>", //Applied Exchange Rate for the current transaction:
        "<div style='line-height:18px;padding-left:30px;'>1 {foreigncurrency} " + WtfGlobal.getLocaleText("acc.inv.for") + " = {revexchangerate} {basecurrency} " + WtfGlobal.getLocaleText("acc.inv.hom") + ". " +
        blockSpotRateLink_first,
        "</div><div style='line-height:18px;padding-left:30px;'>1 {basecurrency} " + WtfGlobal.getLocaleText("acc.inv.hom") + " = {exchangerate} {foreigncurrency} " + WtfGlobal.getLocaleText("acc.inv.for") + ". " +
        blockSpotRateLink_second,
        '</tpl>'
        );
        this.productDetailsTpl=new Wtf.Panel({
            //id:'productDetailsTpl',
            border:false,
            baseCls:'tempbackgroundview',
            width:'95%',
            hidden:true,//(this.isCustBill)?true:false,
            html:this.productDetailsTplSummary.apply({productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0})
        });    
        this.southCenterTpl=new Wtf.Panel({
            border:false,
            html:this.southCenterTplSummary.apply({basecurrency:WtfGlobal.getCurrencyName(),exchangerate:'x',foreigncurrency:"Foreign Currency", editable:false})
        });
        this.southCalTemp=new Wtf.Panel({  
            border:false,
            baseCls:'tempbackgroundview',
            html:this.tplSummary.apply({subtotal:WtfGlobal.currencyRenderer(0),discount:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),termtotal:WtfGlobal.currencyRenderer(0),amountbeforetax:WtfGlobal.currencyRenderer(0),amountdue:WtfGlobal.currencyRenderer(0)})
        });
        this.helpMessage= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.dashboard.help"),  //'Help',
            handler:this.helpmessage,
            scope:this,
            tooltip: WtfGlobal.getLocaleText("acc.common.click"),  //'Click for help',
            iconCls: 'help'
        });
        this.addGrid();
        this.isTaxable= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            id:"includetax"+this.heplmodeid+this.id,
            store:this.fromPOStore,
            listWidth:50,
            fieldLabel:WtfGlobal.getLocaleText("acc.inv.totax"),  //"Include Total Tax",
            allowBlank:this.isOrder,
            value:false,
            width:50,
            typeAhead: true,
            forceSelection: true,
            name:'includetax',
            hiddenName:'includetax',
            listeners:{
                'select':{
                    fn:this.enabletax,
                    scope:this
                }
            }
        });
        this.Tax= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //'Tax',
            id:"tax"+this.heplmodeid+this.id,
            disabled:!this.isEdit,
            hiddenName:'tax',
            anchor: '97%',
            store:this.Grid.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            typeAhead: true,
            mode: 'remote',
            minChars:0,
            addNoneRecord: false,         //For 'None' option in Tax Combo.
            scope:this,
            extraFields: [],
            isTax: true,
            listeners:{
                'select':{
                    //To Show'GST Currency Rate' window for Consignment Sales/Purchase Invoice
                    fn:this.callGSTCurrencyRateandUpdateSubtotal,
                    scope:this
                }, 'beforeselect': {
                    fn: function (combo, record, index) {
                        return validateSelection(combo, record, index);
                    },
                    scope: this
                }
            },
            selectOnFocus:true
        });
        
        var prodDetailSouthItems = [this.productDetailsTpl,this.southCenterTpl];
        if(this.IsInvoiceTerm) {
            this.addInvoiceTermGrid(this.isEdit);
            prodDetailSouthItems.push(this.termgrid);
        }
            
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.view))
            this.Tax.addNewFn=this.addTax.createDelegate(this);

        this.southPanel=new Wtf.Panel({
            region:'center',
            border:false,
            disabledClass:"newtripcmbss",
//            disabled:this.isViewTemplate,
            style:'padding:0px 10px 10px 10px',
            layout:'column',//layout:'border',//Bug Fixed: 14871[SK] Scrolling issue : changed layout from border to column
            height:(Wtf.isIE?210:150) + (prodDetailSouthItems.length>2 ? 100 : 50),
            items:[{
                columnWidth: .45,// width: 570,//region:'center',
                border:false,
                items:prodDetailSouthItems
            },{
//                region:'east',
                id : this.id + 'southEastPanel',
                columnWidth: .55,//width:650,
                border:false,
                layout:'column',
                items:[{
                    layout:'form',
                    width:170,
                    labelWidth:100,
                    border:false,
                    items:this.isTaxable
                },{
                    layout:'form',
                    columnWidth:0.4,
                    labelWidth:30,
                    border:false,
                    items:this.Tax
                },{
                    columnWidth:0.6,
                    layout:'form',
                    cls:'bckgroundcolor',
                    bodyStyle:'padding:10px',
                    labelWidth:70,
                    items:this.southCalTemp
               }]
            }]
        });
        
        
        this.toggleBtnPanel = new Wtf.Panel({                         // for consignment invoice select product from window
            style: 'padding: 10px 10px 0;',
            border: false,
            autoScroll: true,
//            disabled:true,
//            hidden: true,
            items: [{
                    xtype: 'button',
                    enableToggle: true,
                    id: "setButton" + this.heplmodeid + this.id,
//                    hidden: this.readOnly,
                    disabled: true,
                    hidden:true,
                    cls: 'setlocationwarehousebtn',
                    text: WtfGlobal.getLocaleText("acc.field.SelectProduct"),
//                    toggleGroup: 'massupdate'
                    handler: this.showProductGrid.createDelegate(this)
                }]
        });
        
        this.formpPanelOfbutton = new Wtf.Panel({
            border: false,
            autoScroll: true,
            layout: 'table',
            items: [this.toggleBtnPanel]

        })
        var lastTransPanelId = "";
        if(this.quotation) {
            lastTransPanelId = "quotation";
        } else if(this.isOrder) {
            lastTransPanelId = this.isCustomer ? "salesorder" : "purchaseorder";
        } else if(this.cash){
            lastTransPanelId = this.isCustomer ? "cashsales": "cashpurchase";
        } else {
            lastTransPanelId = this.isCustomer ? "CInvoiceList": "VInvoiceList";
        }
        
        
        this.lastTransPanel = this.isCustomer ? getCustInvoiceTabView(false, lastTransPanelId, '', undefined, true) : getVendorInvoiceTabView(false, lastTransPanelId, '', undefined, true) ;
        
        this.NorthForm.doLayout();
        this.NorthForm.doLayout();
        this.southPanel.doLayout();
        this.POStore.on('load',this.updateSubtotal,this)
        this.DueDate.on('blur',this.dueDateCheck,this);
        this.billDate.on('change',this.onDateChange,this);


        this.setTransactionNumber();
        WtfComMsgBox(29,4,true);
        this.isCustomer?chkcustaccload():chkvenaccload();
        this.ajxUrl = "CommonFunctions/getInvoiceCreationJson.do";
        var params={
            transactiondate:transdate,
            loadtaxstore:true,
            moduleid :this.moduleid,
//            loadpricestore: false,//!(this.isCustBill||this.isExpenseInv),
            loadcurrencystore:true,
            loadtermstore:true
//            loadInventory:this.isCustomer
        }
        Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
       this.currencyStore.on('load',this.changeTemplateSymbol,this);
       if(!this.isCustBill&&!this.isCustomer&&!this.isOrder&&!this.isEdit&&!this.copyInv){
           this.ProductGrid.on('pricestoreload',function(arr){//alert("1111"+arr.length)
               if(!this.isExpenseInv){
                    this.datechange=1;
                    this.changeCurrencyStore(arr);
               }
           },this);//.createDelegate(this)
       }else if(!this.isCustBill&&!this.isExpenseInv){//alert("2222"+arr.length)
           this.Grid.on('pricestoreload',function(arr){
                this.datechange=1;
                this.changeCurrencyStore(arr);
       }.createDelegate(this),this);}   
    },
    
    addSalesPerson:function(){
        this.isCustomer ? addMasterItemWindow('15') : addMasterItemWindow('20');
    },
    setdisabledbutton: function(){
        this.moduleTemplateName.setDisabled(true);
        this.createAsTransactionChk.setDisabled(true);
        this.templateModelCombo.setDisabled(true);
        this.Name.setDisabled(true);
        this.Currency.setDisabled(true);
        this.fromPO.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);
        this.PO.setDisabled(true);
        this.warehouses.setDisabled(true);
        this.movmentType.setDisabled(true);
        this.sequenceFormatCombobox.setDisabled(true);
        this.Number.setDisabled(true);
        this.billDate.setDisabled(true);
        this.PORefNo.setDisabled(true);
        this.CostCenter.setDisabled(true);
        this.fromdate.setDisabled(true);
        this.todate.setDisabled(true);
        this.youtReftxt.setDisabled(true);
        this.delytermtxt.setDisabled(true);
        this.invoiceTotxt.setDisabled(true);
        this.shipDate.setDisabled(true);
        this.Term.setDisabled(true);
        this.DueDate.setDisabled(true);
        this.Discount.setDisabled(true);
        this.perDiscount.setDisabled(true);
        this.shipvia.setDisabled(true);
        this.fob.setDisabled(true);
        this.includeProTax.setDisabled(true);
        this.validTillDate.setDisabled(true);
        this.partialInvoiceCmb.setDisabled(true);
        this.template.setDisabled(true);
        this.templateID.setDisabled(true);
        this.users.setDisabled(true);
        this.lockQuantity.setDisabled(true);
        this.wareHouseCombo.setDisabled(true);
        this.locationMultiSelect.setDisabled(true);
        this.invoiceList.setDisabled(true);
        this.generateReceipt.setDisabled(true);
        this.capitalGoodsAcquired.setDisabled(true);
        this.autoGenerateDO.setDisabled(true);
        this.sequenceFormatComboboxDo.setDisabled(true);
        this.no.setDisabled(true);
        this.delydatetxt.setDisabled(true);
        this.projecttxt.setDisabled(true);
        this.depttxt.setDisabled(true);
        this.requestortxt.setDisabled(true);
        this.mernotxt.setDisabled(true);
    },
    moduleTemplateSection:function(){
        this.moduleTemplateRecord = new Wtf.data.Record.create([
            {
                name: 'templateId'
            },
            {
                name: 'templateName'
            },
            {
                name: 'moduleRecordId'
            }
        ]);

        this.moduleTemplateStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.moduleTemplateRecord),
            url : "ACCCommon/getModuleTemplate.do",
            baseParams:{
                moduleId:WtfGlobal.getModuleId(this)
            }
        });
        
        this.moduleTemplateStore.on('load', function(store){
            if(this.isCopyFromTemplate && this.templateId!= undefined ){
                this.templateModelCombo.setValue(this.templateId);
            }     
        },this);
        
        
        this.templateModelCombo= new Wtf.form.FnComboBox({
            fieldLabel:(this.isViewTemplate?'Template Name': WtfGlobal.getLocaleText("acc.field.SelectTemplate")),
            id:"templateModelCombo"+this.heplmodeid+this.id,
            store: this.moduleTemplateStore,
            valueField:'templateId',
            displayField:'templateName',
            hideTrigger:this.isViewTemplate,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.grid.template.emptyText"),
            mode: 'local',
            typeAhead: true,
            hidden:this.isTemplate || this.quotation || this.isFixedAsset || this.isLeaseFixedAsset || this.isConsignment,
            hideLabel:this.isTemplate || this.quotation || this.isFixedAsset || this.isLeaseFixedAsset || this.isConsignment,
            forceSelection: true,
            selectOnFocus:true,
            addNoneRecord: true,
            width : 240,
            triggerAction:'all',
            scope:this,
            listeners:{
                'select':{
                    fn:function(){
                        if(this.templateModelCombo.getValue() != ""){
                            this.loadingMask = new Wtf.LoadMask(document.body,{
                                msg : 'Loading...'
                            });
                            this.loadingMask.show();
                            var templateId = this.templateModelCombo.getValue();
                            var recNo = this.moduleTemplateStore.find('templateId', templateId);
                            var rec = this.moduleTemplateStore.getAt(recNo);
                            var moduleId = rec.get('moduleRecordId');
                            this.SelectedTemplateStore.load({
                                params:{
                                    billid:moduleId,
                                    isForTemplate:true
                                }
                            }); 
                      }else{
                          this.loadStore();
                      }
                    },
            scope:this            
                }
            }
        });
        
        this.moduleTemplateName = new Wtf.form.TextField({
            fieldLabel:'Template Name',
            name: 'moduletempname',
            hidden:!this.isTemplate,
            hideLabel:!this.isTemplate,
            id:"moduletempname"+this.id,
            width : 240,
            maxLength:50,
            scope:this,
            allowBlank:!this.isTemplate
        });
        
        this.createAsTransactionChk = new Wtf.form.Checkbox({
            fieldLabel : 'Create Transaction Also',
            name:'createAsTransactionChkbox',
            hidden:!this.isTemplate,
            hideLabel:!this.isTemplate,
            cls : 'custcheckbox',
            width : 10
        });
        
        this.createAsTransactionChk.on('check', function(){
            if(this.createAsTransactionChk.getValue()){
                this.createTransactionAlso = true;
                this.Number.enable();
                this.sequenceFormatCombobox.enable();
                this.billDate.enable();
                this.autoGenerateDO.enable();
                this.setTransactionNumber();
                this.billDate.setValue(Wtf.serverDate);
            }else{
                this.createTransactionAlso = false;
                this.Number.disable();
                this.sequenceFormatCombobox.disable();                
                this.billDate.disable();
                this.autoGenerateDO.setValue(false);
                this.autoGenerateDO.disable();
                this.Number.setValue('');
            }
        },this);
        
        this.SelectedTemplateRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
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
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'rowproductname'},
        {name:'rowquantity'},
        {name:'rowrate'},
        {name:'rowprdiscount'},
        {name:'rowprtaxpercent'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'salesPerson'},
        {name:'agent'}
        
    ]);
    
    this.SelectedTemplateStoreUrl = "";
    
    if(this.isOrder && !this.quotation){
        this.SelectedTemplateStoreUrl= this.businessPerson=="Customer" ? "ACCSalesOrderCMN/getSalesOrdersMerged.do":"ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do"  
    }else if(this.quotation){
        this.SelectedTemplateStoreUrl = this.isCustomer? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
    }else{
        this.SelectedTemplateStoreUrl= this.businessPerson=="Customer" ? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    }
    
    this.SelectedTemplateStore = new Wtf.data.Store({
            url:this.SelectedTemplateStoreUrl,
            scope:this,
           baseParams:{
                archieve:0,
                deleted:false,
                nondeleted:false,
                cashonly:(this.cash == undefined)?false:this.cash,
                creditonly:false,
                consolidateFlag:false,
                companyids:companyids,
                enddate:'',
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                startdate:''
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.SelectedTemplateRec)
        });
        
        this.SelectedTemplateStore.on('load', this.fillData,this);
        this.SelectedTemplateStore.on('loadexception', function(){
            this.loadingMask.hide();
        },this);
        this.moduleTemplateStore.load();
        
    },
    
    onDateChange:function(a,val,oldval){
        this.val=val;
        this.oldval=oldval;
        this.loadTax(val);
        this.externalcurrencyrate=0;
        this.custdatechange=true;
        this.datechange=1;
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),tocurrencyid:this.Currency.getValue()}});
        if(!(this.isCustBill||this.isExpenseInv)) {
            var affecteduser = this.Name.getValue();
//            this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore, affecteduser);
//            this.Grid.setGridProductValues(true, false);
        }
            
        else{
            this.changeCurrencyStore();
            this.updateSubtotal();
            this.applyCurrencySymbol();
            var subtotal=0.00;
            var tax=0.00;
            var taxAndSubtotal=this.Grid.calLineLevelTax();
            if(this.includeProTax.getValue()){
                subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
            }else{
                subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            }
//            var subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            var discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
            var totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
//            var tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            var aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol);
            var totalAmtInBase=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:discount,totalamount:totalamount,tax:tax,aftertaxamt:aftertaxamt,totalAmtInBase:totalAmtInBase,amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
   },
    confirmResetBatchDetailData: function (combo, valueBeforeSelect) {
        if (valueBeforeSelect != "" && valueBeforeSelect != undefined) {
            if (combo.getValue() === valueBeforeSelect) {
                return;
            } else {
                var dataLength = this.Grid.getStore().data.length;
                var anyBatchDetailsFilled=false;
                var firstRowProd = this.Grid.getStore().getAt(0).get("productid"); // this is to check if it is not blank
                for (var i = 0; i < dataLength; i++) {
                    var rec = this.Grid.getStore().getAt(i);
                    if(rec.get("batchdetails") != "" && rec.get("batchdetails") != undefined){
                        anyBatchDetailsFilled=true;
                        break;
                    }
                }
                if (dataLength >= 1 && firstRowProd != "" && firstRowProd != undefined && anyBatchDetailsFilled) {
                    Wtf.MessageBox.confirm("Warning", WtfGlobal.getLocaleText("acc.wm.beforechange.batchdetailclearmsg"), function (btn) {
                        if (btn == 'yes') {
                            combo.setValue(combo.getValue());
                            for (var i = 0; i < dataLength; i++) {
                                var rec = this.Grid.getStore().getAt(i);
                                rec.set("batchdetails", "");
                            }
                            Wtf.dupsrno.length=0;
                            return;
                        } else if (btn == 'no') {
                            combo.setValue(valueBeforeSelect);
                            return;
                        }
                    }, this);
                } else {
                    combo.setValue(combo.getValue());
                }
            }
        }

    },
    loadTax:function(val){
        this.Grid.taxStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(val)}});
        this.Tax.setValue("");
        this.Grid.getStore().each(function(rec){
            rec.set('prtaxid','')
            rec.set('taxamount',0)
        },this);
    },
    successCallback:function(response){
        if(response.success){
            if(!this.isCustBill&&!this.isCustomer&&!this.isOrder&&!this.isEdit&&!this.copyInv&&this.isQuotation){
                this.ProductGrid.taxStore.loadData(response.taxdata);
                this.ExpenseGrid.taxStore.loadData(response.taxdata);
            }
            else
                this.Grid.taxStore.loadData(response.taxdata);
                this.termds.loadData(response.termdata);
                this.currencyStore.loadData(response.currencydata);
//            if(!(this.isCustBill||this.isExpenseInv)){
//                this.Grid.priceStore.loadData(response.productdata);}
             if(this.currencyStore.getCount()<1){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2);
            }
            else{
                this.isCurrencyLoad=true;
                this.applyTemplate(this.currencyStore,0);
            }
            if(this.cash)
                this.Term.setValue(0);

            if(this.isEdit && this.record!=null) {
                    this.Tax.setValue(this.record.data.taxid);
                }
            if(!((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash))) {
                if(this.isEdit || this.copyInv)this.getTerm();
            }
            if(this.isEdit || this.copyInv)this.loadRecord();            
            this.hideLoading();
        if(this.isExpenseInv){
            if(this.Grid.accountStore.getCount()<=1){
                this.Grid.accountStore.on("load",function(){
                    this.loadDetailsGrid();
                },this);
            }else{
                this.loadDetailsGrid();
            }
                        
        }else{
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
                this.Grid.productComboStore.on("load",function(){
                    this.loadDetailsGrid();
                },this);
            }    
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag!= Wtf.Show_all_Products ){
                this.loadDetailsGrid();
            }
            var loadDetailsGrid=false;
            if(this.isCustomer){
                if(Wtf.StoreMgr.containsKey("productstoresales")){
                    loadDetailsGrid=true;
                }
            }
            else{
                if(Wtf.StoreMgr.containsKey("productstore")){
                    loadDetailsGrid=true;
                }
            }
            if(loadDetailsGrid){
                this.loadDetailsGrid();
            }
        } 
        
    }
    },

    loadDetailsGrid:function(){
            if(this.isEdit && !this.isOrder){
                this.loadEditableGrid();     
            }else if(this.isEdit && this.isOrder && !this.quotation){
                this.loadEditableGridisOrder();
            }else if(this.quotation){
                this.loadEditableGridForQuotation();
            }
            if(this.isEdit && this.isOrder && !this.isCustomer && (BCHLCompanyId.indexOf(companyid) != -1)){
                this.loadOtherOrderdetails();
            }
},
    failureCallback:function(response){
         this.hideLoading();
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Fail to load the record(s). "+response.msg], 2);
    },
    hideLoading:function(){Wtf.MessageBox.hide();},
    applyTemplate:function(store,index){
        var editable=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!=""//&&!this.isOrder;
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if(this.externalcurrencyrate>0) {
            exchangeRate = this.externalcurrencyrate;
        } else if(this.isEdit && this.record.data.externalcurrencyrate&&!this.custdatechange){
            var externalCurrencyRate = this.record.data.externalcurrencyrate-0;//??[PS]
            if(externalCurrencyRate>0){
                exchangeRate = externalCurrencyRate;
            }
        }
        this.externalcurrencyrate = exchangeRate;  //ERP-17367 I have been refer invoice.js
        var revExchangeRate = 1/(exchangeRate-0);
        if(this.exchangeratetype!=undefined&&this.exchangeratetype=="foreigntobase"&&this.revexternalcurrencyrate!=undefined&&this.revexternalcurrencyrate!=0)
            {
                revExchangeRate=this.revexternalcurrencyrate
                this.revexternalcurrencyrate=0;
            }
        revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
        this.southCenterTplSummary.overwrite(this.southCenterTpl.body,{foreigncurrency:store.getAt(index).data['currencyname'],exchangerate:exchangeRate,basecurrency:WtfGlobal.getCurrencyName(),editable:editable,revexchangerate:revExchangeRate         
            });
    },

    changeCurrencyStore:function(pronamearr){
        this.pronamearr=pronamearr;
        var currency=this.Currency.getValue();
        if(this.val=="")this.val=this.billDate.getValue();
        if(currency!=""||this.custChange)
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val),tocurrencyid:this.Currency.getValue()}});
        else
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val)}});
    },
    changeTemplateSymbol:function(){ 

        if(this.loadCurrFlag && Wtf.account.companyAccountPref.currencyid){
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.loadCurrFlag = false;
        }
        
     /*if date of without inventory changes. price store will not be loaded in this case.[PS]*/
        if(this.isCustBill||this.isExpenseInv){
            if(this.currencyStore.getCount()==0){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.billDate.setValue("");
            }
            else
                this.updateFormCurrency();
        }


     /*if date of withinventory changes. After price store load. [PS]   */
//     alert(this.datechange+"---"+this.pronamearr.length)
        if(this.datechange==1){
            var str=""
            if(this.pronamearr!=undefined&&this.pronamearr.length>0){
                str+=this.pronamearr[0];
                for(var i=1;i<this.pronamearr.length;i++){
                    str+="</b>, <b>"+ this.pronamearr[i]
                }
            }
                if(this.currencyStore.getCount()==0){
                    callCurrencyExchangeWindow();
                    str=" and price of <b>"+str+"</b>";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please set the currency rate"+str+" for the selected date: <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                    this.billDate.setValue("");
                    //                if(this.oldval!=""||this.oldval!=undefined){
                    //                    if(!this.isCustBill)
                    //                        this.Grid.loadPriceStoreOnly(this.oldval,this.Grid.priceStore);
                    //                    this.Grid.taxStore.load({params:{transactiondate:this.oldval}});
                    //                }
            } else {
                    this.updateFormCurrency();
                    if(this.pronamearr!=undefined&&this.pronamearr.length>0){
                        str=" price of <b>"+str+"</b>";
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please set the "+str+" for the selected date: <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);}
                    this.Grid.pronamearr=[];
                    this.updateFormCurrency();
            }
            this.datechange=0;
            this.updateSubtotal();
            this.applyCurrencySymbol();
            var subtotal=0.00;
            var tax=0.00;
            var taxAndSubtotal=this.Grid.calLineLevelTax();
            if(this.includeProTax.getValue()){
                subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
            }else{
                subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            }
//            var subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            var discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
            var totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
//            var tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            var aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol);
            var totalAmtInBase=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:discount,totalamount:totalamount,tax:tax,aftertaxamt:aftertaxamt,totalAmtInBase:totalAmtInBase,amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,this.symbol)});
        }
        
        if(this.currencychanged){
            if(this.currencyStore.getCount()<1){
                    callCurrencyExchangeWindow();
                    
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please set the currency rate for the selected date: <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                     this.Currency.setValue("");   
            } else {
                this.updateFormCurrency();
            }
            this.currencychanged = false;
        }
        
    /*when customer/vendor name changes [PS]*/
        if(this.custChange){
            if(this.currencyStore.getCount()==0){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.Name.setValue("");
        } else{
            this.Currency.setValue(this.currencyid)
                this.updateFormCurrency();}
            this.custChange=false;
        }
        this.Grid.pronamearr=[];
    },
    updateFormCurrency:function(){
       this.applyCurrencySymbol();
       var subtotal=0.00;
       var subtotalValue=0.00;
        var tax=0.00;
        var taxAndSubtotal=this.Grid.calLineLevelTax();
        if(this.includeProTax.getValue()){
            subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
            subtotalValue=taxAndSubtotal[0]-taxAndSubtotal[1];
            tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
        }else{
            subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            subtotalValue=this.Grid.calSubtotal();
            tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
        }
       var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
       var amountbeforetax = WtfGlobal.addCurrencySymbolOnly(subtotalValue+this.findTermsTotal(),this.symbol);
       this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,termtotal:calTermTotal,amountbeforetax:amountbeforetax,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
    },

    getCurrencySymbol:function(){
        var index=null;
//        this.currencyStore.clearFilter(true); //ERP-9962
        var FIND = this.Currency.getValue();
        if(FIND == "" || FIND == undefined || FIND == null) {
            FIND = WtfGlobal.getCurrencyID();
        }
        index=this.currencyStore.findBy( function(rec){
             var parentname=rec.data['currencyid'];
            if(parentname==FIND)
                return true;
             else
                return false
            })
       this.currencyid=this.Currency.getValue();
       return index;
    },

    applyCurrencySymbol:function(){
        var index=this.getCurrencySymbol();
        var rate=this.externalcurrencyrate;
        if(index>=0){
           rate=(rate==""?this.currencyStore.getAt(index).data.exchangerate:rate);
            this.symbol=  this.currencyStore.getAt(index).data.symbol;
            this.Grid.setCurrencyid(this.currencyid,rate,this.symbol,index);
            this.applyTemplate(this.currencyStore,index);
       }
       return this.symbol;
    },
 getPostTextEditor: function(posttext)
    {
    	var _tw=new Wtf.EditorWindowQuotation({
    		val:this.postText
    	});
    	
    	 _tw.on("okClicked", function(obj){
             this.postText = obj.getEditorVal().textVal;
             var styleExpression  =  new RegExp("<style.*?</style>");
             this.postText=this.postText.replace(styleExpression,"");
                 
             
         }, this);
         _tw.show();
        return this.postText;
    },
    loadEditableGrid:function(){
    
    var isEditAndLinkingWithGRO = false;
    
    if(this.isEdit){
    }
    this.StoreUrl = "";
        this.subGridStoreUrl = "";
        if (this.quotation) {
         if (this.businessPerson=='Customer') {
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getQuotations.do";
            this.subGridStoreUrl = this.isCustBill?"ACCInvoiceCMN/getQuotationRows.do":"ACCInvoiceCMN/getQuotationRows.do";
        } else{
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getQuotations.do";
            this.subGridStoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getQuotationRows.do":"ACCGoodsReceiptCMN/getQuotationRows.do";
        }               
        }
        else
        {
        if (this.businessPerson=='Customer') {
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getInvoices.do";
            this.subGridStoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoiceRows.do":"ACCInvoiceCMN/getInvoiceRows.do";
            //this.subGridStoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoiceRows.do":"ACCInvoiceCMN/getQuotationRows.do";
        } else{
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getGoodsReceipts.do";
            this.subGridStoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceiptRows.do":"ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
        }
        }
        this.billid=this.record.data.billid;
        var mode=this.isCustBill?17:14;
        this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.Grid.getStore().load({params:{bills:this.billid,mode:mode,isexpenseinv:this.isExpenseInv,isConsignment:true}});
        this.EditisAutoCreateDO=false;
      if(this.isEdit && (this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId  ) && !this.copyInv)
         {
           Wtf.Ajax.requestEx({
                url: "ACCInvoiceCMN/getDOFromInvoice.do",
                params: {
                    invoiceId: this.billid,
                    CallFromCI:this.isCustomer ? true : false//true:get DO from invoice and false : get GRO from Vendor Invoice
                }
            }, this, function(response) {
                if (response.data && response.data.length > 0) {
                  // WtfGlobal.hideFormElement(this.sequenceFormatComboboxDo);
                    this.autoGenerateDO.disable();
                    this.EditisAutoCreateDO=true;
                    this.sequenceFormatComboboxDo.setValue(response.data[0].sequenceformatDo)
                    this.no.setValue(response.data[0].SequenceNumDO);
                    this.DeliveryOrderid=response.data[0].DeliveryOrderID;
                    WtfGlobal.showFormElement(this.no);
                }
            }, function(response) {
                });
               
         }
    },

    loadEditableGridisOrder:function(){
        this.subGridStoreUrl = "";
            if (!this.isCustomer) {
            	if(!this.isCustBill){
            		if(this.isPOfromSO){                         
                            this.subGridStoreUrl ="ACCSalesOrderCMN/getSalesOrderRows.do";     
                            this.Grid.soLinkFlag = true;
                        }else{
                            this.subGridStoreUrl ="ACCPurchaseOrderCMN/getPurchaseOrderRows.do";
                        }
            	}else{
            		this.subGridStoreUrl = "ACCPurchaseOrderCMN/getBillingPurchaseOrderRows.do";
            	}
            } else{                
            	if(!this.isCustBill){
                    if(this.isSOfromPO){                         
                         this.subGridStoreUrl ="ACCPurchaseOrderCMN/getPurchaseOrderRows.do";     
                         this.Grid.soLinkFlag = true;
                    }else{
                         this.subGridStoreUrl = "ACCSalesOrderCMN/getSalesOrderRows.do";
                    }     
            	}else{
            		this.subGridStoreUrl = "ACCSalesOrderCMN/getBillingSalesOrderRows.do";
            	}
            }
            this.billid=this.record.data.billid;
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
            
            this.Grid.getStore().load({params:{bills:this.billid,isConsignment:true}});
            
    },
    
    loadEditableGridForQuotation:function(){
        if (!this.isCustomer) {
            this.subGridStoreUrl = "ACCPurchaseOrderCMN/getQuotationRows.do";
        }else{
            this.subGridStoreUrl = "ACCSalesOrderCMN/getQuotationRows.do";
        }
        if(!this.isCustomer && this.PR_IDS) {
            this.Grid.getStore().proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitionRows.do";
            this.Grid.getStore().load({params:{bills:this.PR_IDS}});
            
            // reset to original config
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;  
            this.Grid.getStore().params = {bills:this.billid}
        } else {
            this.billid=this.record.data.billid;
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;  
            this.Grid.getStore().load({params:{bills:this.billid,copyInvoice:this.copyInv}});
        }
    },
    
    loadOtherOrderdetails: function() {
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/getPurchaseOrderOtherDetails.do",
            params: {
                poid: this.record.data.billid
            }
        }, this, function(response) {
            if (response.data && response.data.length > 0) {
                this.youtReftxt.setValue(response.data[0].poyourref);
                this.delydatetxt.setValue(response.data[0].podelydate);
                this.delytermtxt.setValue(response.data[0].podelyterm);
                this.invoiceTotxt.setValue(response.data[0].poinvoiceto);
                this.depttxt.setValue(response.data[0].podept);
                this.projecttxt.setValue(response.data[0].poproject);
                this.requestortxt.setValue(response.data[0].porequestor);
                this.mernotxt.setValue(response.data[0].pomerno);
            }
        }, function(response) {
        });

    },
    addGrid:function(){
//        if(Optimized_CompanyIds.indexOf(companyid)!= -1){
//           this.ProductGrid=new Wtf.account.ProductDetailsGridOptimized({
//                height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                layout:'fit',
//                title: 'Asset Group',//WtfGlobal.getLocaleText("acc.invoice.inventory"),  //'Inventory',
//                border:true,
//                //cls:'gridFormat',
//                helpedit:this.heplmodeid,
//                moduleid: this.moduleid,
//                id:this.id+"editproductdetailsgrid",
//                viewConfig:{forceFit:true},
//                isCustomer:this.isCustomer,
//                currencyid:this.currencyid,
//                disabledClass:"newtripcmbss",
//                fromOrder:true,
//                isEdit:this.isEdit,
//                isOrder:this.isOrder,
//                readOnly:this.readOnly,
//                isInvoice:this.isInvoice,
//                isQuotation:this.quotation,
//                isFromGrORDO:this.isFromGrORDO,
//                forceFit:true,
//                isCash:this.cash,
//                loadMask : true,
//                parentObj :this
//            }); 
//        }else
        {
           this.ProductGrid=new Wtf.account.ConsignmentStockProductDetailsGrid({
                height: 300,//region:'center',//Bug Fixed: 14871[SK]
                layout:'fit',
                title:WtfGlobal.getLocaleText("acc.invoice.inventory"),  //'Inventory',
                border:true,
                //cls:'gridFormat',
                helpedit:this.heplmodeid,
                moduleid: this.moduleid,
                id:this.id+"editproductdetailsgrid",
                viewConfig:{forceFit:true},
                isCustomer:this.isCustomer,
                currencyid:this.currencyid,
                disabledClass:"newtripcmbss",
                isFromGrORDO:this.isFromGrORDO,
                fromOrder:true,
                isEdit:this.isEdit,
                readOnly:this.readOnly,
                isOrder:this.isOrder,
                isInvoice:this.isInvoice,
                isQuotation:this.quotation,
                forceFit:true,
                isCash:this.cash,
                loadMask : true,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isConsignment:this.isConsignment,  //consignment flag
                isLinkedFromReplacementNumber:false,
                isLinkedFromCustomerQuotation:false,
                parentObj :this
            }); 
        }
         if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
                this.ProductGrid.productComboStore.load();
         }
         this.ProductGrid.on("productselect", this.loadTransStore, this);
         this.ProductGrid.on("productdeleted", this.removeTransStore, this);

        if(this.isCustBill){  //Without Inventory.[PS]
//            if(this.isCustomer){
//                 this.Grid=new Wtf.account.BillingProductDetailsGrid({
//                    //region:'center',//Bug Fixed: 14871[SK]
//                    height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                    cls:'gridFormat',
//                    layout:'fit',
//                    moduleid: this.moduleid,
//                    viewConfig:{forceFit:true},
//                    disabledClass:"newtripcmbss",
//                    disabled:this.isViewTemplate,
//                    isCustomer:this.isCustomer,
//                    editTransaction:this.isEdit,
//                    isCustBill:this.isCustBill,
//                    id:this.id+"billingproductdetailsgrid",
//                    currencyid:this.Currency.getValue(),
//                    fromOrder:true,
//                    isOrder:this.isOrder,
//                    isInvoice:this.isInvoice,
//                    forceFit:true,
//                    loadMask : true
//                });
//            }else{ //Add this code if ixpense invoice needed in non-inventory[PS]
//                this.ProductGrid=new Wtf.account.BillingProductDetailsGrid({
//                    height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                    border:true,
//                    title: 'Inventory',
//                    viewConfig:{forceFit:true},
//                    isCustomer:this.isCustomer,
//                    editTransaction:this.isEdit,
//                    isCustBill:this.isCustBill,
//                    id:this.id+"billingproductdetailsgrid",
//                    currencyid:this.Currency.getValue(),
//                    fromOrder:true,
//                    isOrder:this.isOrder,
//                    closable: false,
//                    forceFit:true,
//                    loadMask : true
//                });
//               this.ExpenseGrid=new Wtf.account.ExpenseInvoiceGrid({
//                    height: 200,
//                    border:true,
//                    title: 'Expense',
//                    viewConfig:{forceFit:true},
//                    isCustomer:this.isCustomer,
//                    editTransaction:this.isEdit,
//                    isCustBill:this.isCustBill,
//                    id:this.id+"expensegrid",
//                    currencyid:this.Currency.getValue(),
//                    fromOrder:true,
//                    closable: false,
//                    isOrder:this.isOrder,
//                    forceFit:true,
//                    loadMask : true
//                });
//                this.GridPanel= new Wtf.TabPanel({
//                    id : this.id+'invoicegrid',
//                    iconCls:'accountingbase coa',
//                    border:false,
//                    style:'margin:10px;',
//                    cls:'invgrid',
//                    activeTab:0,
//                    height: 200,
//                    items: [this.ExpenseGrid,this.ProductGrid]
//                });
//                this.Grid = Wtf.getCmp(this.id+"expensegrid");
//                this.ProductGrid.on('datachanged',this.updateSubtotal,this);
//                this.ProductGrid.on("activate", function(){//alert("A")
//                    this.Grid = Wtf.getCmp(this.id+"billingproductdetailsgrid");
//                    this.isExpenseInv=false;
//                    this.applyCurrencySymbol();
//                    if(this.southCalTemp.body!=undefined)
//                        this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol)});
//                }, this);
//                this.ExpenseGrid.on("activate", function(){//alert("B")
//                    this.Grid = Wtf.getCmp(this.id+"expensegrid");
//                    this.isExpenseInv=true
//                    this.applyCurrencySymbol();
//                    if(this.southCalTemp.body!=undefined)
//                        this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol)});
//                }, this);
//            }
        }else{    //With Inventory[PS]
            if(this.isEdit && !this.isOrder){
                if(this.isExpenseInv){
                    this.ExpenseGrid=new Wtf.account.ExpenseInvoiceGrid({
                        height: 300,
                        //layout : 'fit',
                        border:true,
                        cls:'gridFormat',
//                        title: WtfGlobal.getLocaleText("acc.invoice.gridExpenseTab"),//'Expense',
                        viewConfig:{forceFit:true},
                        isCustomer:this.isCustomer,
                        editTransaction:this.isEdit,
                        moduleid: this.moduleid,
                        isCustBill:this.isCustBill,
                        disabledClass:"newtripcmbss",
                        readOnly:this.isViewTemplate ||this.readOnly,
                        id:this.id+"expensegrid",
                        currencyid:this.Currency.getValue(),
                        fromOrder:true,
                        isOrder:this.isOrder,
                        isInvoice:this.isInvoice,
                        forceFit:true,
                        loadMask : true,
                        parentObj :this
                    });
                    this.ExpenseGrid.on('datachanged',this.updateSubtotal,this);
                    this.Grid = this.ExpenseGrid; 
                    
                }else{
//                    if(Optimized_CompanyIds.indexOf(companyid)!= -1){
//                        this.Grid=new Wtf.account.ProductDetailsGridOptimized({
//                            //region:'center',//Bug Fixed: 14871[SK]
//                            height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                            cls:'gridFormat',
//                            layout:'fit',
//                            moduleid: this.moduleid,
//                            id:this.id+"productdetailsgrid",
//                            isCash:this.cash,
//                            viewConfig:{forceFit:false},
//                            autoScroll:true,
//                            editTransaction:true,
//                            disabledClass:"newtripcmbss",
//                            readOnly:this.isViewTemplate ||this.readOnly,
//                            record:this.record,
//                            copyInv:this.copyInv,
//                            fromPO:false,
//                            isEdit:this.isEdit,
//                            isFromGrORDO:this.isFromGrORDO,
//                            isOrder:this.isOrder,
//                            isInvoice:this.isInvoice,
//                            isCN:false,
//                            isCustomer:this.isCustomer,
//                            loadMask : true,
//                            parentObj :this
//                        });
//                    }else
                    {
                        this.Grid=new Wtf.account.ConsignmentStockProductDetailsGrid({
                            //region:'center',//Bug Fixed: 14871[SK]
                            height: 300,//region:'center',//Bug Fixed: 14871[SK]
                            cls:'gridFormat',
                            layout:'fit',
                            moduleid: this.moduleid,
                            id:this.id+"productdetailsgrid",
                            isCash:this.cash,
                            viewConfig:{forceFit:false},
                            autoScroll:true,
                            editTransaction:true,
                            disabledClass:"newtripcmbss",
                            readOnly:this.isViewTemplate ||this.readOnly,
                            isFromGrORDO:this.isFromGrORDO,
                            record:this.record,
                            copyInv:this.copyInv,
                            fromPO:false,
                            isEdit:this.isEdit,
                            isCN:false,
                            isCustomer:this.isCustomer,
                            isOrder:this.isOrder,
                            isInvoice:this.isInvoice,
                            isQuotation:this.quotation,
                            loadMask : true,
                            isFixedAsset:this.isFixedAsset,
                            isLeaseFixedAsset:this.isLeaseFixedAsset,
                            isConsignment:this.isConsignment,  //consignment flag
                            isLinkedFromReplacementNumber:false,
                            isLinkedFromCustomerQuotation:false,
                             parentObj :this
                        });
                    }
                    this.Grid.on("productselect", this.loadTransStore, this);
                    this.Grid.on("productdeleted", this.removeTransStore, this);
                }
            }
            else{
                if(this.isCustomer||this.isOrder){
//                    if(Optimized_CompanyIds.indexOf(companyid)!= -1){
//                        this.Grid=new Wtf.account.ProductDetailsGridOptimized({
//                            //region:'center',//Bug Fixed: 14871[SK]
//                            height: 200,//region:'center',//Bug Fixed: 14871[SK]
//                            cls:'gridFormat',
//                            layout:'fit',
//                            parentCmpID:this.id,
//                            moduleid: this.moduleid,
//                            id:this.id+"editproductdetailsgrid",
//                            isCash:this.cash,
//                            viewConfig:{forceFit:false},
//                            record:this.record,
//                            isQuotation:this.quotation,
//                            isQuotationFromPR : this.isQuotationFromPR,
//                            isCustomer:this.isCustomer,
//                            currencyid:this.currencyid,
//                            disabledClass:"newtripcmbss",
//                            readOnly:this.isViewTemplate ||this.readOnly,
//                            isFromGrORDO:this.isFromGrORDO,
//                            fromPO:this.isOrder,
//                            fromOrder:true,
//                            isEdit:this.isEdit,
//                            isOrder:this.isOrder,
//                            forceFit:true,
//                            editTransaction: this.isEdit,
//                            loadMask : true,
//                            parentObj :this
//                        });
//                    }else
                    {
                        this.Grid=new Wtf.account.ConsignmentStockProductDetailsGrid({
                            //region:'center',//Bug Fixed: 14871[SK]
                            height: 300,//region:'center',//Bug Fixed: 14871[SK]
                            cls:'gridFormat',
                            layout:'fit',
                            parentCmpID:this.id,
                            moduleid: this.moduleid,
                            id:this.id+"editproductdetailsgrid",
                            isCash:this.cash,
                            viewConfig:{forceFit:false},
                            record:this.record,
                            isQuotation:this.quotation,
                            isQuotationFromPR : this.isQuotationFromPR,
                            isCustomer:this.isCustomer,
                            currencyid:this.currencyid,
                            disabledClass:"newtripcmbss",
                            readOnly:this.isViewTemplate ||this.readOnly,
                            fromPO:this.isOrder,
                            fromOrder:true,
                            isEdit:this.isEdit,
                            isFromGrORDO:this.isFromGrORDO,
                            isConsignment:this.isConsignment,  //consignment flag
                            isOrder:this.isOrder,
                            isInvoice:this.isInvoice,
                            forceFit:true,
                            editTransaction: this.isEdit,
                            loadMask : true,
                            isFixedAsset:this.isFixedAsset,
                            isLeaseFixedAsset:this.isLeaseFixedAsset,
                            isLinkedFromReplacementNumber:false,
                            isLinkedFromCustomerQuotation:false,
                            parentObj :this
                        });
                    }
                    this.Grid.on("productselect", this.loadTransStore, this);
                    this.Grid.on("productdeleted", this.removeTransStore, this);
                  }else{

                   this.ExpenseGrid=new Wtf.account.ExpenseInvoiceGrid({
                        height: 300,
                        //layout : 'fit',
                        border:true,
                        title: WtfGlobal.getLocaleText("acc.invoice.gridExpenseTab"),//'Expense',
                        viewConfig:{forceFit:true},
                        isCustomer:this.isCustomer,
                        editTransaction:this.isEdit,
                        moduleid: this.moduleid,
                        isCustBill:this.isCustBill,
                        disabledClass:"newtripcmbss",
                        id:this.id+"expensegrid",
                        currencyid:this.Currency.getValue(),
                        fromOrder:true,
                        closable: false,
                        isOrder:this.isOrder,
                        isInvoice:this.isInvoice,
                        forceFit:true,
                        loadMask : true,
                        parentObj :this
                    });
                    this.GridPanel= new Wtf.TabPanel({
                        id : this.id+'invoicegrid',
                        iconCls:'accountingbase coa',
                        disabled:this.isViewTemplate,
                        border:false,
                        style:'padding:10px;',
                        disabledClass:"newtripcmbss",
                        cls:'invgrid',
                        //cls:'gridFormat',
                        activeTab:0,
                        height: 300,
                        //region : 'center',
                        //layout : 'fit',
                        
                        items: (this.isFixedAsset || this.isLeaseFixedAsset || this.isConsignment)?[this.ProductGrid]:[this.ProductGrid,this.ExpenseGrid]
                    });
                    this.Grid = Wtf.getCmp(this.id+"editproductdetailsgrid");                    
                    this.ExpenseGrid.on('datachanged',this.updateSubtotal,this);
                    this.ProductGrid.on('datachanged',this.updateSubtotal,this);
                    if(this.symbol==undefined)this.symbol=WtfGlobal.getCurrencySymbol();
                    this.isExpenseInv=false; //work fine in case of 2 tabs
                    this.GridPanel.on('beforetabchange', this.beforeTabChange,this);
                }
            }
        }
       
        this.Name.on('select',this.onCustomerVendorSelect,this);
        this.Name.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
        }, this);
//        this.Name.on('select',this.setTerm,this)
//        this.Name.on('select',this.setSalesPerson,this)
        this.NorthForm.on('render',this.setDate,this);
        this.Grid.on('datachanged',this.updateSubtotal,this);
        this.Grid.on('gridconfigloaded',function(){
            this.showGridTax(null,null,!this.includeProTax.getValue());
        },this);
        this.Grid.getStore().on('load',function(store, recArr){
            if(!this.isOrder && !this.quotation && this.isCustomer && this.copyInv && !this.isViewTemplate){
            this.confirmMsg = "";
            if(!Wtf.account.companyAccountPref.withinvupdate){
                for(var i=0; i<recArr.length; i++){
                    if(recArr[i].data.productid !== undefined){
                        var index=this.ProductGrid.productComboStore.find('productid',recArr[i].data.productid);
                        var prorec=this.ProductGrid.productComboStore.getAt(index);
                        if(recArr[i].data['quantity'] > this.ProductGrid.productComboStore.getAt(index).data['quantity'] && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'){
                            this.confirmMsg += "Maximum available Quantity for Product "+this.ProductGrid.productComboStore.getAt(index).data['productname']+" is "+this.ProductGrid.productComboStore.getAt(index).data['quantity']+".<br>";
                            Wtf.MessageBox.confirm("Confirm",this.confirmMsg+"Do you want to continue?",function(btn){
                                if(btn=="yes") {
                                
                                }else{
                                    this.ownerCt.remove(this);
                                }
                            }, this);
                            recArr[i].set('quantity', 0);
                            recArr[i].set('amount', 0);
                        }
                    }
                }
            }
        }
            this.updateSubtotal();
            this.Grid.addBlank(store);
            if(this.isEdit){
                if(this.record.data.externalcurrencyrate!=undefined){
                    this.externalcurrencyrate=this.record.data.externalcurrencyrate;
                    this.updateFormCurrency();
                }
            }
        }.createDelegate(this),this);
        this.Grid.getStore().on('update',function(store,record,opr){
            var isExpensive = (this.isExpenseInv != null && this.isExpenseInv != undefined)?this.isExpenseInv:false;
            if(!this.isCustBill && !isExpensive){
                var index=this.Grid.productComboStore.findBy(function(rec){
                    if(rec.data.productid==record.data.productid)
                        return true;
                    else
                        return false;
                });
                var prorec=this.Grid.productComboStore.getAt(index);
                if(prorec!=undefined&&prorec!=-1&&prorec!=""){
                    this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{
                        productid:prorec.data['productid'],
                        productname:prorec.data['productname'],
                        qty:parseFloat(getRoundofValue(prorec.data['quantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+prorec.data['uomname'],
                        soqty:parseFloat(getRoundofValue(prorec.data['socount'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+prorec.data['uomname'],
                        poqty:parseFloat(getRoundofValue(prorec.data['pocount'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+prorec.data['uomname']
                        });
                }
            }                            
        },this);
    },        
    beforeTabChange:function(a,newTab,currentTab){
    	if(currentTab!=null && newTab!=currentTab){
            
            if(this.capitalGoodsAcquired){
                this.capitalGoodsAcquired.reset();
            }
            
             Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),this.isExpenseInv?WtfGlobal.getLocaleText("acc.invoice.msg4"):WtfGlobal.getLocaleText("acc.invoice.msg5"),function(btn){ ///"Switching to "+(this.isExpenseInv?"Inventory":"Expense")+" section will empty the data filled so far in "+(this.isExpenseInv?"Expense":"Inventory")+" section. Do you wish to continue?",function(btn){
              if(btn=="yes") {                
                (this.productDetailsTpl.isVisible())?this.productDetailsTpl.setVisible(false):this.productDetailsTpl.setVisible(true);
                a.suspendEvents();
                a.activate(newTab);
                this.Discount.setValue(0);
            var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
            var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
            if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                        this.isTaxable.setValue(true);
                        this.Tax.enable();
                        this.Tax.setValue(actualTaxId);
                }else{
                    this.isTaxable.setValue(false);
                    this.Tax.setValue("");
                    this.Tax.disable();   
                }
                a.resumeEvents();
                this.onGridChange(newTab,currentTab);
                this.showGridTax(null,null,!this.includeProTax.getValue());       // Show/hide Product tax and Tax Amount for Inventory/Expense Tab               
              }            
             }.createDelegate(this),this)
            return false;
        }
        else{
            return true;
        }
   },

    onGridChange:function(newTab){
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow();
            this.Grid = newTab;
            this.Tax.store=this.Grid.taxStore;
            this.isExpenseInv=!this.isExpenseInv; //work fine in case of 2 tabs
//            if(!this.isCustBill&&!this.isExpenseInv&&this.Grid.priceStore.getCount()==0)
//                this.Grid.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
            this.applyCurrencySymbol();
            if(this.southCalTemp.body!=undefined){
                var subtotal=0.00;
                var tax=0.00;
                var taxAndSubtotal=this.Grid.calLineLevelTax();
                if(this.includeProTax.getValue()){
                    subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
                    tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
                }else{
                    subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                    tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
                }
                 this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
            }
    },
    caltax:function(){
        var totalamount=this.calTotalAmount();
        var rec= this.Grid.taxStore.getAt(this.Grid.taxStore.find('prtaxid',this.Tax.getValue()));
        var taxamount=((rec==null || rec.data.prtaxid == "None")?0:(totalamount*rec.data["percent"])/100);
        return taxamount;
     },
    addAccount: function(store){
        callCOAWindow(false,null,"coaWin",this.isCustomer,false,false,false,false,false,true);
        Wtf.getCmp("coaWin").on('update',function(){store.reload();},this);
    },
        addOrder:function(){
        var tabid = "ordertab";
        if(this.isCustomer){
            if(this.quotation){
                tabid = 'vendorquotation';
                callVendorQuotation(false, tabid);
            } else if(this.isOrder){
                tabid = 'quotation';
                callQuotation(false, tabid);
            } else {
//                if(this.isCustBill) {
//                    tabid = "bsalesorder";
//                    callBillingSalesOrder(false,null,tabid);
//                } else 
                {
                    if(this.fromLinkCombo.getValue() == 1) {
                        callDeliveryOrder(false,null, "deliveryorder");
                    } else {
                        tabid = "salesorder";
                        callSalesOrder(false,null,tabid);
                    }
                   
                }
            }
        }else{
//            if(this.isCustBill) {
//                tabid = "bpurchaseorder";
//                callBillingPurchaseOrder(false,null,tabid);
//            } else 
            {
                if(this.isOrder){
                    if(this.fromLinkCombo.getValue() == 0) {
                        tabid = "salesorder";
                        callSalesOrder(false,null,tabid);
                    } else if(this.fromLinkCombo.getValue() == 2){
                        tabid = 'vendorquotation';
                       callVendorQuotation(false, tabid);
                    }
                    
                } else if(this.fromLinkCombo.getValue() == 1) {
                        callGoodsReceiptDelivery(false,null, "goodsreceiptdelivery");
                    } else {
                        tabid = "purchaseorder";
                        callPurchaseOrder(false,null,tabid);
                    }
            }
        }
        if(Wtf.getCmp(tabid)!=undefined) {
            Wtf.getCmp(tabid).on('update',function(){
                this.POStore.reload();
            },this);
        }
    },
    showGridTax:function(c,rec,val){
        var hide=(val==null||undefined?!rec.data['value']:val) ;
        var id=this.Grid.getId()
        var rowtaxindex=this.Grid.getColumnModel().getIndexById(id+"prtaxid");
        var rowtaxamountindex=this.Grid.getColumnModel().getIndexById(id+"taxamount");
            this.Grid.getColumnModel().setHidden( rowtaxindex,hide) ;
            this.Grid.getColumnModel().setHidden( rowtaxamountindex,hide) ;
        this.Grid.getStore().each(function(rec){
            if(this.includeProTax && this.includeProTax.getValue() == true
                && (rec.data.prtaxid == "" || rec.data.prtaxid == undefined)) {//In Edit, values are resetting after selection Product level Tax value as No
                if(this.ExpenseGrid && this.ExpenseGrid.isVisible()) {//(!this.isCustBill && !(this.isEdit && !this.isOrder) && !(this.isCustomer||this.isOrder))
                    var index=this.ExpenseGrid.accountStore.find('accountid',rec.data.accountid);
                    var taxid = index > 0 ? this.ExpenseGrid.accountStore.getAt(index).data["acctaxcode"]:"";
                    var taxamount = this.ExpenseGrid.setTaxAmountAfterSelection(rec);
//                    rec.set('prtaxid',taxid);
//                    rec.set('taxamount',taxamount);
                } else {
                    index=this.ProductGrid.productComboStore.find('productid',rec.data.productid);
                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                    taxid = index > 0 ? this.ProductGrid.productComboStore.getAt(index).data[acctaxcode]:"";
//                    rec.set('prtaxid',taxid);
                    taxamount = this.ProductGrid.setTaxAmountAfterSelection(rec);
//                    rec.set('taxamount',taxamount);
                }

                rec.set('prtaxid',taxid);
                rec.set('taxamount',taxamount);
                rec.set('isUserModifiedTaxAmount', false);
            } else {
                rec.set('prtaxid','')
                rec.set('taxamount',0)
            }
         },this);
//         if(hide)
        if (this.includeProTax && this.includeProTax.getValue() == true) {
            WtfGlobal.calculateTaxAmountUsingAdaptiveRoundingAlgo(this.Grid, this.isExpenseInv);//ERM-1085
        }
        this.updateSubtotal();
    },
    
    
    includeProTaxHandler : function(c,rec,val){
        if(this.includeProTax.getValue() == true){
            this.isTaxable.setValue(false);
            this.isTaxable.disable();
            this.Tax.setValue("");
            this.Tax.disable();
        }else{
            this.isTaxable.reset();
            this.isTaxable.enable();
        }
        this.showGridTax(c,rec,val);
    },
    
    showPartialDiscount : function(c,rec,val) {
        var hide=val;
        var id=this.Grid.getId();
        var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");

        this.Grid.getColumnModel().setHidden( rowindex,hide) ;
        this.Grid.getStore().each(function(rec){
            rec.set('partamount',0)
        },this);
        this.updateSubtotal();
    },
       
     enableNumber:function(c,rec){
        this.PO.clearValue();
        this.fromLinkCombo.enable();
        this.fromPO.setValue(true);
           if(this.Grid){
            this.Grid.isFromGrORDO=false;
        }
        if(rec.data['value']==0){          // 0 for Sales Order
            this.PO.multiSelect=true;
            this.isMultiSelectFlag=true;
            this.PO.removeListener("select",this.populateData,this);  // for selection of multiple sales order 
            this.PO.addListener("blur",this.populateData,this);
            
            if(this.isLeaseFixedAsset && this.isOrder && this.isCustomer && !this.quotation){
                this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getQuotations.do"
                      if(this.isSOfromPO){
//                          closeFlag = false;
                          this.POStore.proxy.conn.url = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrders.do":"ACCPurchaseOrderCMN/getPurchaseOrders.do";
                      }
                this.POStore.load({
                    params:{
                        id:this.Name.getValue(),
                        currencyid:this.Currency.getValue(),
                        validflag:true,
                        isLeaseFixedAsset:this.isLeaseFixedAsset,
                        billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),
                        newcustomerid:this.Name.getValue()
                    }
                });
                this.PO.enable();
            }else if(this.isLeaseFixedAsset && this.isCustomer && this.quotation){
                this.POStore.proxy.conn.url = "ACCPurchaseOrderCMN/getQuotations.do"
                
                this.POStore.load({
                    params:{
                        id:this.Name.getValue(),
                        currencyid:this.Currency.getValue(),
                        validflag:true,
                        isLeaseFixedAsset:this.isLeaseFixedAsset,
                        billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),
                        newcustomerid:this.Name.getValue()
                    }
                });
                this.PO.enable();
            }else{
                this.fromLinkCombo.setValue(0); 
                this.autoGenerateDO.enable();
                if(this.isOrder){
                    this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getSalesOrders.do";
                    this.POStore.load({params:{currencyfilterfortrans:this.Currency.getValue()}});        
                } else {
                    this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
                    this.POStore.load({params:{id:this.Name.getValue(),exceptFlagINV:true,currencyfilterfortrans:this.Currency.getValue()}});        
                }
                //this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
            //this.POStore.load({params:{id:this.Name.getValue(),exceptFlagINV:true}});            
                this.PO.enable();
                if(this.partialInvoiceCmb){
                    this.partialInvoiceCmb.disable();
                    var id=this.Grid.getId();
                    var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                    this.Grid.getColumnModel().setHidden( rowindex,true) ;
                }
            }
            //this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
           //this.POStore.load({params:{id:this.Name.getValue(),exceptFlagINV:true}});            
            this.PO.enable();
            if(this.partialInvoiceCmb){
                this.partialInvoiceCmb.disable();
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                this.Grid.getColumnModel().setHidden( rowindex,true) ;
            }
        }
        else if(rec.data['value']==1){     // 1 for Delivery  
            if(this.isLeaseFixedAsset && this.isOrder && this.isCustomer && !this.quotation){
                this.PO.multiSelect=false;
                this.isMultiSelectFlag=false;
                this.PO.addListener("select",this.populateData,this);
                this.PO.removeListener("blur",this.populateData,this);
                
                this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getReplacementRequests.do";
                
                this.POStore.load({params:{
                        id:this.Name.getValue(),
                        currencyid:this.Currency.getValue(),
                        validflag:true,
                        billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
                    }
                });
                this.PO.enable();
                
            }else if(this.isLeaseFixedAsset && this.isCustomer && this.quotation){
                this.PO.multiSelect=false;
                this.isMultiSelectFlag=false;
                this.PO.addListener("select",this.populateData,this);
                this.PO.removeListener("blur",this.populateData,this);
                
                this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getReplacementRequests.do";
                
                this.POStore.load({params:{
                        id:this.Name.getValue(),
                        currencyid:this.Currency.getValue(),
                        validflag:true,
                        isForQuotation:this.quotation,
                        billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
                    }
                });
                this.PO.enable();
                
            }else{
                this.isFromGrORDO=true;
                this.Grid.isFromGrORDO=true;
                this.PO.multiSelect=true;
                this.isMultiSelectFlag=true;
                this.PO.removeListener("select",this.populateData,this);
                this.PO.addListener("blur",this.populateData,this);                
                this.fromLinkCombo.setValue(1);
                this.autoGenerateDO.setValue(false);
                this.autoGenerateDO.disable();            
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrdersMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
                this.POStore.load({params:{id:this.Name.getValue(),isFixedAsset:this.isFixedAsset,isLeaseFixedAsset:this.isLeaseFixedAsset,nondeleted:true}});        
                this.PO.enable(); 
                if(this.partialInvoiceCmb){
                    this.partialInvoiceCmb.disable();
                    var id=this.Grid.getId();
                    var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                    this.Grid.getColumnModel().setHidden( rowindex,true) ;
                }
            }
        } else if(rec.data['value']==2){ //2 for Quotation                        
            this.PO.multiSelect=true;
            this.isMultiSelectFlag=true;
            this.autoGenerateDO.enable();
            this.PO.removeListener("select",this.populateData,this);
            this.PO.addListener("blur",this.populateData,this);                            
            this.fromLinkCombo.setValue(2);
            this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
            this.POStore.load({params:{id:this.Name.getValue(),currencyid:this.Currency.getValue(),validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),nondeleted:true}});        
            this.PO.enable();
        }
    },
    
    enablePO:function(c,rec){
        this.autoGenerateDO.enable();
        if(rec.data['value']==true){
            if(!this.isCustBill&&!this.isCustomer&&!this.isEdit&&!this.copyInv&&!(this.isOrder&&(!this.isCustomer))){//this.isExpenseInv=false;
                this.GridPanel.setActiveTab(this.ProductGrid);
                this.ExpenseGrid.disable();

            }
            if(!(this.isCustBill ||this.cash)){
                this.fromLinkCombo.enable();
            }else{
                if(!this.isCustBill && this.isOrder && this.isCustomer && this.quotation)   //loading vendor Quotations in Customer Quotations
                    this.POStore.load({params:{validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
                else if(!this.isCustBill && this.isOrder && this.isCustomer && !this.quotation){   //loading Quotations in sales order
//                    if(this.isLeaseFixedAsset){
//                        this.POStore.load({
//                            params:{
//                                id:this.Name.getValue(),
//                                currencyid:this.Currency.getValue(),
//                                validflag:true,
//                                billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
//                            }
//                        });
//                    }else{
//                        this.POStore.load({params:{id:this.Name.getValue(),currencyid:this.Currency.getValue(),validflag:true,billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
//                    }
                    this.fromLinkCombo.enable();
                }
                else if(!this.isCustomer && !this.isCustBill && this.isOrder) //loading for Sales Orders in Purchase Orders
                    this.POStore.load();    
                else                                                         //loading for so and po in CI and VI in With/Without inventory mode and but not in trading flow
                    this.POStore.load({params:{id:this.Name.getValue()}});                
                this.PO.enable();
            }                                                      
            this.fromOrder=true;
            if(!this.isCustBill && this.isOrder && this.isCustomer && !this.quotation){
                if(this.isLeaseFixedAsset){
                    this.PO.multiSelect=false;
                    this.isMultiSelectFlag=false;
                    this.PO.removeListener("blur",this.populateData,this);
                    this.PO.addListener("select",this.populateData,this);
                }else{
                    this.PO.multiSelect=true;
                    this.isMultiSelectFlag=true;
                    this.PO.removeListener("select",this.populateData,this);
                    this.PO.addListener("blur",this.populateData,this);
                }
            }else if(this.isCustBill && !this.isOrder && !this.quotation){// this.isCustBill ||this.isCustomer
                this.PO.multiSelect=true;
                this.isMultiSelectFlag=true;
                this.PO.removeListener("select",this.populateData,this);
                this.PO.addListener("blur",this.populateData,this);     
            }else {
                this.PO.multiSelect=false;
                this.isMultiSelectFlag=false;
                this.PO.removeListener("blur",this.populateData,this);
                this.PO.addListener("select",this.populateData,this);
            }                        
        }
        else{
            if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer)
            {
                this.fromLinkCombo.disable();
                this.PO.disable();
            }    
            this.loadStore();
            if(!this.isCustBill&&!this.isCustomer&&!this.isEdit&&!this.copyInv){//this.isExpenseInv=false;
                this.ExpenseGrid.enable();
            }    
            this.setDate();
            
            if(this.partialInvoiceCmb){
                this.partialInvoiceCmb.disable();
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                this.Grid.getColumnModel().setHidden( rowindex,true) ;
            }
        }
        this.currencyStore.load(); 	       // Currency id issue 20018
    },
    enabletax:function(c,rec){
        if(rec.data['value']==true)
           this.Tax.enable();
        else{
            this.Tax.disable();
            this.Tax.setValue("");
        }
        this.updateSubtotal();
    },
    /*
     * This method is called For Project Status Report- For PM-Accounting Integration
     */
    setPOLinks:function(){
        this.fromPO.enable();
        this.fromPO.setValue(true); 
        this.fromLinkCombo.enable();
        this.fromLinkCombo.setValue(0);
        this.PO.enable()
//        this.POStore.on('load', function(){
//            alert('kk');
//        if(this.isFromProjectStatusRep){
//            if(this.SOLinkedArr.length>0){
//                this.PO.setValue(this.SOLinkedArr);
//            }
//        this.populateData("","");
//        }
//        },this);
        this.POStore.load(); 
        
    },
    populateData:function(c,rec) {
        this.singleLink = false;
         if(this.PO.getValue()!=""){
             
            // if you are creating Sales Order by linking with Quotation && Quotation is containing Contract(if it is created from Replacement Number)
            //OR - you are creating Invoice through linking with delivery order and DO is containing Contract
            if(this.isCustomer && this.isLeaseFixedAsset && !this.quotation){
                var doIdsArray = this.PO.getValue().split(",");
                var isMultipleContractsSelected = WtfGlobal.isMultipleContractsSelected(doIdsArray,this.PO.store);

                if(isMultipleContractsSelected){
                    if(this.isOrder){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.linking.cq.selection.msg") ], 3);
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.linking.do.selection.msg") ], 3);
                    }
                    
                    return;
                }
            }
             
          var billid=this.PO.getValue();
          this.clearComponentValues();
          this.Grid.fromPO=true; 
          this.Grid.linkedFromOtherTransactions=true; 
          if(this.isLeaseFixedAsset && this.isOrder){// in case of Lease Sales Order
              if(this.fromLinkCombo.getValue()==1){
                  this.Grid.isLinkedFromReplacementNumber=true;
              }
          }else{
              this.Grid.isLinkedFromReplacementNumber=false;
          }
          
        if(this.isLeaseFixedAsset && this.quotation){
            if(this.isOrder && this.fromLinkCombo && this.fromLinkCombo.getValue() == 0){// if SO linked with CQ
                this.Grid.isLinkedFromCustomerQuotation=true;
            }    
        } else{
            this.Grid.isLinkedFromCustomerQuotation=false;
        }
          
          var multipleSelectProdIncludeFlag=false;
          if(this.isMultiSelectFlag){ //For MultiSelection 
            var selectedids=this.PO.getValue();
            var selectedValuesArr = selectedids.split(',');
            if(selectedValuesArr.length==1){  // Load value of Include product tax according to PO
                    rec = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[0]));
                    this.linkRecord = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[0]));
                    this.singleLink = true;
                if(rec.data["includeprotax"]){
                    this.includeProTax.setValue(true);
                    this.showGridTax(null,null,false);
                }else{
                    this.includeProTax.setValue(false);
                    this.showGridTax(null,null,true);
                }
            } else if(selectedValuesArr.length>1){
                for(var cnt=0;cnt<selectedValuesArr.length;cnt++){
                    rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[cnt]));
                    if(rec.data.contract!=undefined && rec.data.contract!=""){   // check for quoatation with contract
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.alert.quotationtype")], 2);
                        this.PO.clearValue();
                        return; 
                    }
                    if(rec.data["includeprotax"]){
                        this.includeProTax.setValue(true);
                        this.showGridTax(null,null,false);
                        multipleSelectProdIncludeFlag=true;
                        break;
                    }
                }
                if(!multipleSelectProdIncludeFlag){
                    this.includeProTax.setValue(false);
                    this.showGridTax(null,null,true);
                }
            } else{
                this.includeProTax.setValue(false);
                this.showGridTax(null,null,true);
            }
            
            this.isTaxable.setValue(false);
            this.isTaxable.disable();
            this.Tax.setValue("");
            this.Tax.disable();
            
//            this.showGridTax(null,null,false);        
            this.Tax.disable();
            this.isTaxable.reset();
            //this.updateData();
            this.Tax.reset();            
//            if(!(!this.isCustomer || (this.isOrder&&!this.isCustomer))){
//                    if(this.Name.getValue()!=""){
//                      this.billingAddrsStore.load({params:{customerid:this.Name.getValue()}});
//                      this.ShippingAddrsStore.load({params:{customerid:this.Name.getValue()}});         
//                    }                
//                }              
            this.setValues(billid);//In MultiSelection if the user select only one                 
            rec=this.PO.getValue();
            var selectedValuesArr = rec.split(',');
            if(selectedValuesArr.length==1){
                var record=this.POStore.getAt(this.POStore.find('billid',billid));
                this.isCustomer ? this.users.setValue(record.data["salesPerson"]) : this.users.setValue(record.data["agent"]); 
                if(this.partialInvoiceCmb){
                    this.partialInvoiceCmb.enable();
                }
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id)) {
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                    }
                }
            }else{
                this.users.reset();
                if(this.partialInvoiceCmb){
                    this.partialInvoiceCmb.reset();
                    this.partialInvoiceCmb.disable();
                    var id=this.Grid.getId();
                    var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
                    if(rowindex>=0){    
                        this.Grid.getColumnModel().setHidden( rowindex,true) ;
                    }
                }
            }
        }else{                
                rec=this.POStore.getAt(this.POStore.find('billid',billid));
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id)) {
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                    }
                }
                // if(this.isOrder && this.isCustomer && !this.isCustBill){//Temporary check to hide/display product tax for order. Need to fix for Invoices also
                if(rec.data["includeprotax"]){
                    this.includeProTax.setValue(true);
                    
                    this.isTaxable.setValue(false);
                    this.isTaxable.disable();
                    this.Tax.setValue("");
                    this.Tax.disable();
                    
                    this.showGridTax(null,null,false);
                } else {
                    this.includeProTax.setValue(false);
                    this.isTaxable.reset();
                    this.isTaxable.enable();
                    this.showGridTax(null,null,true);
                }
    //        } else {
    //            this.includeProTax.setValue(true);
    //            this.showGridTax(null,null,false);
    //        }
              if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer){
               if(this.fromLinkCombo.getValue()==1){
                    this.includeProTax.setValue(false);
                    this.showGridTax(null,null,true);            
                }
            }
            this.Memo.setValue(rec.data['memo']);
            this.shipDate.setValue(rec.data['shipdate']);
            this.validTillDate.setValue(rec.data['validdate']);
            if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash &&!this.isFixedAsset) { // set value only in VI module
                this.invoiceList.setValue(rec.data['landedInvoiceNumber']);
            }
            if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash)) {
                this.Term.setValue(rec.data['termdays']);
            }
            this.postText=rec.data['posttext'];
            this.shipvia.setValue(rec.data['shipvia']);
            this.fob.setValue(rec.data['fob']);
            if(this.users != null && this.users != undefined){
                this.isCustomer ? this.users.setValue(rec.data['salesPerson']) : this.users.setValue(rec.data['agent']);
            }
            //this.Name.setValue(rec.data['personid']);
            this.loadTransStore();
            
            if(rec.data["discounttotal"] && this.Discount){
            this.Discount.setValue(rec.data["discounttotal"]);
            this.perDiscount.setValue(rec.data["discountispertotal"]);
        }
        
            if(rec.data['taxid']!=""){
                this.Tax.enable();
                this.isTaxable.setValue(true);
                this.Tax.setValue(rec.data['taxid']);
            }else{
                this.Tax.disable();
                this.isTaxable.reset();
                this.Tax.reset();
            }
            this.getCreditTo(rec.data.creditoraccount);            
            if(this.fromLinkCombo.getValue()==1){
                this.updateData();               
            }else{
                this.Currency.setValue(rec.data['currencyid']);
            }
            var perstore=this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore
            var index = perstore.find('accid',this.Name.getValue());
            if(index != -1){
                var storerec=perstore.getAt(index);
//                if(!(!this.isCustomer || (this.isOrder&&!this.isCustomer))){
//                    if(this.Name.getValue()!=""){
//                      this.billingAddrsStore.load({params:{customerid:this.Name.getValue()}});
//                      this.ShippingAddrsStore.load({params:{customerid:this.Name.getValue()}});         
//                    }                
//                }
                this.Term.setValue(storerec.data['termdays']);
            }        
            this.CostCenter.setValue(rec.data.costcenterid);
            rec=rec.data['billid'];
        }                                       
        this.updateDueDate();
        var url = "";
		//(this.isCustBill?53:43)
        var soLinkFlag = false;        
        var VQtoCQ = false;
        var isLinkingOfReplacementRequest = false;
        var linkingFlag = false; //For removing cross reference of DO-CI or GR-VI
        if(!this.isCustBill && !this.isOrder && !this.cash ){
            if(this.fromLinkCombo.getValue()==0){
                url = this.isCustomer ? 'ACCSalesOrderCMN/getSalesOrderRows.do' : 'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
            } else if(this.fromLinkCombo.getValue()==1){
                url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrderRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
                linkingFlag=true;
            }else if(this.fromLinkCombo.getValue()==2){
                url = this.isCustomer ? "ACCSalesOrderCMN/getQuotationRows.do" : "ACCPurchaseOrderCMN/getQuotationRows.do";
                VQtoCQ = true;//Linking Quotation when creating invoice, we need to display Unit Price excluding row discount
            }
        } else {
            if(this.isCustomer){
                if(this.quotation){
                    if(this.fromLinkCombo.getValue()==0){
                        soLinkFlag = true;
                        url = "ACCPurchaseOrderCMN/getQuotationRows.do";
                        VQtoCQ = true;
                    }else{
                        url = "ACCSalesOrderCMN/getReplacementRequestRows.do";
                    }
                } else if(this.isOrder){
                    if(this.isLeaseFixedAsset){
                        if(this.fromLinkCombo.getValue()==0){
                            url = "ACCSalesOrderCMN/getQuotationRows.do";
                            VQtoCQ = true;
                        }else{
                            url = "ACCSalesOrderCMN/getReplacementRequestRows.do";
                        }
                    }else{
                        url = "ACCSalesOrderCMN/getQuotationRows.do";
                        VQtoCQ = true;
                    }
                } else {
                    url = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrderRows.do":'ACCSalesOrderCMN/getSalesOrderRows.do';  
                }
            } else {
                if(this.isOrder){
                    url = 'ACCSalesOrderCMN/getSalesOrderRows.do';
                    if(this.isCustBill) {
                        url = "ACCSalesOrderCMN/getBillingSalesOrderRows.do";
                    } else {
                        if(this.fromLinkCombo.getValue()==0){
                            url = 'ACCSalesOrderCMN/getSalesOrderRows.do';
                            soLinkFlag = true;
                        } else if(this.fromLinkCombo.getValue()==2){
                            url = 'ACCPurchaseOrderCMN/getQuotationRows.do';
                            VQtoCQ = true;
                        }
                    }
                } else {
                    url = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrderRows.do":'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
                }
            }
        }                
	this.Grid.getStore().proxy.conn.url = url;
            this.Grid.loadPOGridStore(rec, soLinkFlag, VQtoCQ,linkingFlag);          
        }
    },
populateAllData:function(){
    if(!this.isEdit && !this.copyInv){  //in edit and copy case do not remove record from store
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow();
    }
    if(this.warehouses.getValue()!="" && this.warehouses.getValue()!=undefined){
        this.Grid.warehouseselcted=true;
    }else{
        this.Grid.warehouseselcted=false;
    }
   if(this.isConsignment && this.isInvoice){
   this.Grid.getStore().proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getAllUninvoicedConsignmentDetails.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
   if(this.Grid.warehouseselcted){
       if(this.isConsignment && this.moduleid === Wtf.Acc_ConsignmentInvoice_ModuleId){
            this.toggleBtnPanel.getComponent(0).enable();
       }else{
            this.Grid.getStore().load({params:{customerid:this.Name.getValue(),custWarehouse:this.warehouses.getValue(),closeflag:true,isConsignment:this.isConsignment,Currency:this.Currency.getValue()}});
       
       }
   }else{
       
          this.toggleBtnPanel.getComponent(0).disable()
   }
//    this.Grid.loadAllGridStore("");          
   }
},

showProductGrid : function() {//ERP-23308 :
    this.consignmentProductSelectionArray = [];
    var productid='';
    //var size = ;
    
    
    for(var i=0 ; i<this.Grid.getStore().getCount() ; i++){
        productid= this.Grid.getStore().getAt(i).data.productid;
        if( this.consignmentProductSelectionArray.indexOf(productid) != -1) {
            break;
        }else{
            this.consignmentProductSelectionArray.push(productid);
                    
        }
    }
            
//    if(i==this.consignmentProductSelectionArray.length) {
//    }
        this.productSelWin = new Wtf.account.ProductSelectionWindow({
            renderTo: document.body,
            height : 600,
            width : 700,
            title:'Product Selection Window',
            layout : 'fit',
            modal : true,
            resizable : true,
            id:this.id+'ProductSelectionWindow',
            moduleid:Wtf.Acc_ConsignmentInvoice_ModuleId,
            modulename:"Consignment Invoice",
            invoiceGrid:this.Grid,
            parentCmpID:this,
            isConsignment:this.isConsignment,
            consignmentProductSelectionArray:this.consignmentProductSelectionArray,
            customerid : this.Name.getValue(),
            Currency:this.Currency.getValue(),
            custWarehouse:this.warehouses.getValue(),
            closeflag :true 
        });
        this.productSelWin.show();
    },
populateAllConsignData:function(){
    if(!this.isEdit && !this.copyInv){  //in edit and copy case do not remove record from store
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow();
    }
    if(this.warehouses.getValue()!="" && this.warehouses.getValue()!=undefined){
        this.Grid.warehouseselcted=true;
    }else{
        this.Grid.warehouseselcted=false;
    }
   if(this.isConsignment && this.isInvoice){
   this.Grid.getStore().proxy.conn.url = "ACCGoodsReceiptCMN/getAllUninvoicedConsignmentDetails.do"; 
    this.Grid.getStore().load({params:{vendorid:this.Name.getValue(),closeflag:true,isConsignment:this.isConsignment}});

   }
},
    loadDataForProjectStatusReport:function(){
        this.isFromProjectStatusRep = false;
        var url = "ACCSalesOrderCMN/getSalesOrderRows.do";
        var rec = "";
        for(var i=0;i<this.SOLinkedArr.length;i++){
            rec+=this.SOLinkedArr[i]+',';
        }
        if(rec !=""){
            rec = rec.substring(0,rec.length-1);
        }
        this.Grid.getStore().proxy.conn.url = url;
        this.Grid.loadPOGridStore(rec, false, false,false); 
    },

    onCustomerVendorSelect: function(c,rec,ind) {
        this.Term.setValue(rec.data['termdays']);
    this.updateDueDate();
    if(!this.isInvoice && this.isEdit){
        this.warehouses.enable();
    }
    if(rec.data['masterSalesPerson'] != ""){
        this.users.setValue(rec.data['masterSalesPerson']);
    }else{//ERP-12165
        this.users.setValue(this.defaultsalesperson);
    }
        if(this.productOptimizedFlag == Wtf.Show_all_Products && Wtf.account.companyAccountPref.isFilterProductByCustomerCategory && this.isCustomer) {
            this.ProductGrid.productComboStore.baseParams['iscustomercategoryfilter'] = true;
            this.ProductGrid.productComboStore.baseParams['customerid'] = this.Name.getValue();
            this.ProductGrid.productComboStore.load({
                params:{
                    mappingProduct:true,
                    customerid:this.Name.getValue(),
                    common:'1', 
                    loadPrice:true,
                    mode:22, 
                    "iscustomercategoryfilter":true
                }
            }) ; 
        }else if(Wtf.account.companyAccountPref.isFilterProductByCustomerCategory && this.isCustomer) {
            this.ProductGrid.productComboStore.baseParams['iscustomercategoryfilter'] = true;
            this.ProductGrid.productComboStore.baseParams['customerid'] = this.Name.getValue();
        }
    },
    /**
     * need to call for implement 'GST Currency Rate' functionality for Consignment Sales/Purchase invoice.
     */
    callGSTCurrencyRateandUpdateSubtotal:function(a,val){
        if (WtfGlobal.singaporecountry() && WtfGlobal.getCurrencyID() != Wtf.Currency.SGD && this.isConsignment && this.isInvoice && this.Grid.forCurrency != Wtf.Currency.SGD) {
            var record = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
            callGstCurrencyRateWin(this.id, record.data.currencyname, undefined, this.gstCurrencyRate);
        }
        this.updateSubtotal(a,val);
    },
    updateSubtotal:function(a,val){
        
        if(this.calDiscount())return;
        this.isClosable=false; // Set Closable flag after updating grid data
        this.applyCurrencySymbol();
        
        var subtotal=0.00;
        var subtotalValue=0.00;
        var tax=0.00;
        var taxAndSubtotal=this.Grid.calLineLevelTax();
        if(this.includeProTax.getValue()){
            subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
            subtotalValue=taxAndSubtotal[0]-taxAndSubtotal[1];
            tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
        }else{
            subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            subtotalValue=this.Grid.calSubtotal();
            tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
        }
        
        var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
        var amountbeforetax = WtfGlobal.addCurrencySymbolOnly(subtotalValue+this.findTermsTotal(),this.symbol);
        if(a!=undefined && a.id!=undefined && a.id.indexOf("editproductdetailsgrid") != -1){
            this.termStore.reload();
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,termtotal:calTermTotal,amountbeforetax:amountbeforetax,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.findTermsTotal()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
        else{
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,termtotal:calTermTotal,amountbeforetax:amountbeforetax,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.findTermsTotal()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        }
        
        if(this.isFromProjectStatusRep){
            if(this.SOLinkedArr.length>0){
                this.PO.setValue(this.SOLinkedArr);
            }
        this.isMultiSelectFlag = true;    
        this.loadDataForProjectStatusReport();
        }
    },
    
    getNextSequenceNumber:function(a,val){
       if(!(a.getValue()=="NA")){
         WtfGlobal.hideFormElement(this.Number);
         this.setTransactionNumber(true);
         var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.fromnumber,
                sequenceformat:a.getValue(),
                oldflag:oldflag
            }
        }, this,function(resp){
            if(resp.data=="NA"){
                WtfGlobal.showFormElement(this.Number);
                this.Number.reset();
                this.Number.enable();
            }else {
                this.Number.setValue(resp.data);  
                this.Number.disable();
                WtfGlobal.hideFormElement(this.Number);
            }
            
        });
       } else {
           WtfGlobal.showFormElement(this.Number);
           this.Number.reset();
                this.Number.enable();
           }
    },
     getNextSequenceNumberDo:function(a,val){
      if(!(a.getValue()=="NA")){
        this.setTransactionNumberDo(true);
         var rec=WtfGlobal.searchRecord(this.sequenceFormatStoreDo, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.fromnumberDo,
                sequenceformat:a.getValue(),
                oldflag:oldflag
            }
        }, this,function(resp){
            if(resp.data=="NA"){
                this.no.reset();
                this.no.enable();
            }else {
                    this.no.setValue(resp.data);
                    this.no.disable();
            }
            
        });
        } else {
            this.no.reset();
            this.no.enable();
        }
    },
    
    getDiscount:function(){
        var disc = 0;
        var per = 1;
//        if(!(this.isOrder && !this.quotation)){
            disc=this.Discount.getValue();
            per=this.perDiscount.getValue();
//        }
        var subtotalAfterTerm = this.Grid.calSubtotal() + this.findTermsTotal();
        return isNaN(parseFloat(disc))?0:(per?(disc*subtotalAfterTerm)/100:disc);
    },
    calDiscount:function(){
        var disc=this.Discount.getValue();
        var per=this.perDiscount.getValue();
        if(per && disc > 100){
            WtfComMsgBox(28,2);
            this.NorthForm.getForm().setValues({perdiscount:false});
            return true;
        }
        else
            return false;
    },
    calTotalAmount:function(){
        var subtotal=this.Grid.calSubtotal();
        var discount=this.getDiscount();
//        return subtotal-discount + this.findTermsTotal();
        return subtotal-discount;
    },
    calTotalAmountInBase:function(){
        var subtotal=this.Grid.calSubtotal(); 
        var discount=this.getDiscount();   
        var taxVal = this.caltax();
        var returnValInOriginalCurr = subtotal-discount + this.findTermsTotal()+taxVal;
        returnValInOriginalCurr = returnValInOriginalCurr*this.getExchangeRate();
        return returnValInOriginalCurr; 
    },
    getExchangeRate:function(){
        var index=this.getCurrencySymbol();
        var rate=this.externalcurrencyrate;
        var revExchangeRate = 0;
        if(index>=0){
            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
            if(this.externalcurrencyrate>0) {
                exchangeRate = this.externalcurrencyrate;
            }
            revExchangeRate = 1/(exchangeRate);
            revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
        }
        return revExchangeRate;
    }, 
    save:function(){
    this.savecount++;
    this.Number.setValue(this.Number.getValue().trim());       
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();   //This check is added to check mandatory  custom field
        if(this.NorthForm.getForm().isValid() && isValidCustomFields){
            if(this.isCustBill){
                for(var datacount=0;datacount<this.Grid.getStore().getCount();datacount++){
                    var creditoracc=this.Grid.getStore().getAt(datacount).data['creditoraccount'];                    
                    if(creditoracc==undefined||creditoracc==""){
                        if(this.Grid.getStore().getAt(datacount).data['productdetail'].length>0){
                            var account=(this.isCustomer)?"Credit account":"Debit account";
                            WtfComMsgBox(["Warning","Please select "+account], 2);
                            return;
                        } 
                    }            
                }
            }
            // Checking for deactivated products
            var inValidProducts=this.checkForDeActivatedProductsAdded();
                if(inValidProducts!=''){
                    inValidProducts = inValidProducts.substring(0, inValidProducts.length-2);
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), 
                        msg: WtfGlobal.getLocaleText("acc.common.followingProductsAreDeactivated")+'</br>'+'<b>'+inValidProducts+'<b>',
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this,
                        scopeObj :this,
                        fn: function(btn){
                            if(btn=="ok"){
                                this.enableSaveButtons();
                                return;
                            }
                        }
                    });
                    return;
            }
            
             if (this.moduleid == Wtf.Acc_ConsignmentRequest_ModuleId && !(this.currentAddressDetailrec.shippingContactPerson)){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Contact Person is not set. Set it from Show Address." ], 2);
                return;
              }
            //check is there duplicate product in transaction
            var isDuplicate = false;
            var confirmMsg = "";
            var duplicateval = ", ";
            if (this.isConsignment && Wtf.account.companyAccountPref.isDuplicateItems) {
                var prodLength = this.Grid.getStore().data.items.length;
                for (var i = 0; i < prodLength - 1; i++)
                {
                    var prodID = this.Grid.getStore().getAt(i).data['productid'];
                    for (var j = i + 1; j < prodLength - 1; j++) {
                        var productid = this.Grid.getStore().getAt(j).data['productid'];
                        if (prodID == productid) {
                            isDuplicate = true;
                            var prorec = this.Grid.getStore().getAt(this.Grid.getStore().find('productid', prodID));
                            if (duplicateval.indexOf(", " + prorec.data.pid + ",") == -1) {
                                duplicateval += prorec.data.pid + ", ";//Add duplicate product id 
                            }
                        }
                    }
                }
            }
            if (isDuplicate == true) {
                duplicateval = duplicateval.substring(2, (duplicateval.length - 2));
                confirmMsg = duplicateval + " " + WtfGlobal.getLocaleText("acc.field.duplicateproduct") + ". " + WtfGlobal.getLocaleText("acc.msgbox.Doyouwanttoproceed");
            }
            if (this.isConsignment && isDuplicate && confirmMsg != "") {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"),
                    msg: confirmMsg,
                    buttons: Wtf.MessageBox.YESNO,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this,
                    scopeObj: this,
                    fn: function(btn) {
                        if (btn == "yes") {
                            this.continueWithDuplicateProducts();
                        } else {
                            return;
                        }
                    }
                });
            } else {
                this.continueWithDuplicateProducts();
            }
        }else{
            WtfComMsgBox(2, 2);
            this.savecount--;
        }
    }, 
    continueWithDuplicateProducts:function(){
        var incash=false;
        for(var i=0;i<this.Grid.getStore().getCount()-1;i++){
                var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                var rate=this.Grid.getStore().getAt(i).data['rate'];
                if(quantity<=0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Quantity for Product "+this.Grid.getStore().getAt(i).data['productname']+" should be greater than Zero"], 2);
                    return;
                } 
                if(rate===""||rate==undefined||rate<0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Rate for Product "+this.Grid.getStore().getAt(i).data['productname']+" cannot be empty."], 2);
                    return;
                }
            }
            
            // In Case of Fixed Asset OR Lease Fixed Asset Check external and internal quantities are equal or not
            if(this.isFixedAsset || this.isLeaseFixedAsset){
                for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// exclude last row
                    var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                    
                    var productId = this.Grid.getStore().getAt(i).data['productid'];
                    
                    var proRecord = WtfGlobal.searchRecord(this.Grid.productComboStore,productId,'productid');
                    
                    if(proRecord.get('isAsset') && !this.quotation){
                    
                    var assetDetails = this.Grid.getStore().getAt(i).data['assetDetails'];
                    
                    if(assetDetails == "" || assetDetails == undefined){
                        WtfComMsgBox(['Information','Please Provide Asset Details for Asset Group '+this.Grid.getStore().getAt(i).data['productname']],0);
                        return;
                    }
                    
//                        var rateIntoQuantityVal = rate*quantity - assetDetailTotalSellingAmount;
//                        rateIntoQuantityVal = (rateIntoQuantityVal<0)?(-1)*rateIntoQuantityVal:rateIntoQuantityVal;

                    var assetDetailArray = eval('(' + assetDetails + ')');
                    
                    if(assetDetailArray == null || assetDetailArray == undefined){
                        WtfComMsgBox(['Information','Please Provide Asset Details for Asset Group '+this.Grid.getStore().getAt(i).data['productname']],0);
                        return;
                    }
                    
                    if(quantity != assetDetailArray.length){
                        WtfComMsgBox(['Information','Entered quantity does not match with the Asset Rows entered. Please give complete Asset Details for Asset Group '+this.Grid.getStore().getAt(i).data['productname']+'.'],0);
                        return;
                    }
                    
                    var rate=this.Grid.getStore().getAt(i).data['rate'];
//                        if(!this.isLeaseFixedAsset){
                            if(this.isCustomer){
                                var assetDetailTotalSellingAmount = 0;

                                for(var j=0;j<assetDetailArray.length;j++){
                                    assetDetailTotalSellingAmount+=parseFloat(assetDetailArray[j].sellAmount);
                                }
                                var sellMsg = "Sell Amount";
                                if(this.isLeaseFixedAsset){
                                    sellMsg = "Leasing Amount";
                                }

                                var rateIntoQuantityVal = rate*quantity - assetDetailTotalSellingAmount;
                                rateIntoQuantityVal = (rateIntoQuantityVal<0)?(-1)*rateIntoQuantityVal:rateIntoQuantityVal;

                                if(rateIntoQuantityVal > Wtf.decimalLimiterValue){// due to java script rounding off problem
                                    WtfComMsgBox(['Information','Rate entered is not equal to Asset Details total Sell Amount value  for Asset Group <b>'+this.Grid.getStore().getAt(i).data['productname']+'</b>'],0);
                                    return;
                                }
                            }else{
                                var assetDetailTotalCost = 0;

                                for(var j=0;j<assetDetailArray.length;j++){
                                    assetDetailTotalCost+=parseFloat(assetDetailArray[j].costInForeignCurrency);
                                }


                                var rateQuantityVal = rate*quantity - assetDetailTotalCost;
                                rateQuantityVal = (rateQuantityVal<0)?(-1)*rateQuantityVal:rateQuantityVal;

                                if(rateQuantityVal > Wtf.decimalLimiterValue){// due to java script rounding off problem
                                    WtfComMsgBox(['Information','Rate entered is not equal to Asset Details total Cost value  for Asset Group <b>'+this.Grid.getStore().getAt(i).data['productname']+'</b>'],0);
                                    return;
                                }
                            }
//                        }
                    
                }
            }
            }
            
            
            if(!this.isCustBill && (Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel && ((Wtf.account.companyAccountPref.withinvupdate && (!this.isInvoice && !this.isOrder)) || (!Wtf.account.companyAccountPref.withinvupdate && !this.isOrder)))){
                
                var validstore=WtfGlobal.isValidInventoryInfo(this.Grid.getStore(),'invstore');
                if(!validstore){
                    WtfComMsgBox(["Warning","Please select valid inventory store."], 2);
                    return;
                }
                
                var validloc=WtfGlobal.isValidInventoryInfo(this.Grid.getStore(),'invlocation');
                if(!validloc){
                    WtfComMsgBox(["Warning","Please select valid inventory location."], 2);
                    return;
                }
            }
            
            var count=this.Grid.getStore().getCount();
            if(count<=1){
                WtfComMsgBox(33, 2);
                return;
            }                        
            if(this.getDiscount()>this.Grid.calSubtotal()){
                WtfComMsgBox(12, 2);
                return;
            }
            if(this.Grid.calSubtotal()<=0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Total amount should be greater than Zero."], 2);
                return;
            }
//            var datediff=new Date(this.billDate.getValue()).getElapsed(this.DueDate.getValue());
//            if(datediff==0)
//                  incash=true;
//              else
                  incash=this.cash;
            var rec=this.NorthForm.getForm().getValues();
            
            if(rec.vendor==undefined&&this.linkIDSFlag!=undefined&&this.linkIDSFlag){
                rec.vendor=this.Name.getValue();
            }
            
            if(rec.vendor==undefined && !this.isCustomer && this.isInvoice && this.isEdit){// In case of edition of purchase invoice some times vendor is being undefined
                rec.vendor=this.Name.getValue();
            }
            
            if(rec.customer==undefined&&this.linkIDSFlag!=undefined&&this.linkIDSFlag){
                rec.customer=this.Name.getValue();
            }
            
            this.isGenerateReceipt = this.generateReceipt.getValue();
            this.isAutoCreateDO = this.autoGenerateDO.getValue();
            rec.islockQuantity =this.lockQuantity.getValue();
            rec.requestWarehouse =this.wareHouseCombo.getValue();
            rec.requestLocation =this.locationMultiSelect.getValue();
            rec.gstCurrencyRate=this.gstCurrencyRate;
            if(this.isRequestApprovalFlow==true  && this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId){//&& this.lockQuantity.getValue()==false 
                rec.autoapproveflag=true;
            }
            
            rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.fromdate=WtfGlobal.convertToGenericDate(this.fromdate.getValue());
            rec.todate=WtfGlobal.convertToGenericDate(this.todate.getValue());          
            if(this.cash)
                {
                   var index = this.termds.find('termdays','-1');
                    if(index != -1){
                        var storerec=this.termds.getAt(index);                        
                       this.termid=storerec.get("termid");
                    }          
                }
             else
                      this.updateDueDate();
            rec.termid=this.termid;
            this.ajxurl = ""; 
              var leaselectValue=this.PO.getValue();
        if(leaselectValue!=undefined&& leaselectValue!=""){
            var poindex=this.POStore.findBy( function(rec){
                var parentname=rec.data['billid'];
                if(parentname==leaselectValue)
                    return true;
                else
                    return false;
            })
            if(poindex>=0) {
                var leaseRec= this.POStore.getAt(poindex);
                if(leaseRec.data.contractstatus==2){
                    WtfComMsgBox(114, 2);
                    return false;
                } 
            }
                
        }
            if(this.businessPerson=="Customer"){
    //(this.isOrder?(this.isCustBill?51:41):(this.isCustBill?13:11))
                    if(this.quotation)
                            this.ajxurl = "ACCSalesOrder/saveQuotation.do";
                    else
                            this.ajxurl = "ACC" + (this.isOrder?(this.isCustBill?"SalesOrder/saveBillingSalesOrder":"SalesOrder/saveSalesOrder"):(this.isCustBill?"Invoice/saveBillingInvoice":"Invoice/saveInvoice")) + ".do";
        }else if(this.businessPerson=="Vendor"){
                if(this.quotation){
                    this.ajxurl = "ACCPurchaseOrder/saveQuotation.do";
                } else {
                    this.ajxurl = "ACC"+ (this.isOrder?(this.isCustBill?"PurchaseOrder/saveBillingPurchaseOrder":"PurchaseOrder/savePurchaseOrder"):(this.isCustBill?"GoodsReceipt/saveBillingGoodsReceipt":"GoodsReceipt/saveGoodsReceipt")) +".do";
                }
                
            }
            var currencychange=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!=""&&!this.isOrder;
            var msg=currencychange?"Currency rate you have applied cannot be changed again. ":"";           
            var detail = this.Grid.getProductDetails();
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
                }
            var validLineItem = this.Grid.checkDetails(this.Grid);
            if (validLineItem != "" && validLineItem != undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);
                return;
            }
                var batchserialAvailaleorNot=false;
             if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory  || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details 
//            Wtf.Ajax.requestEx({
//                url: "ACCInvoice/getStockAvailabilityforAllproducts.do",
//                params: {
//                    proddetails:detail,
//                    movementtype:this.movmentType.getValue()
//                }
//            },this,this.genSuccessResponseBatch,this.genFailureResponseBatch);
            var batchdetailcount=0;
            var prodLength=this.Grid.getStore().data.items.length;
            for(var g=0;g<prodLength-1;g++)
            { 
              var batchDetail= this.Grid.getStore().getAt(g).data['batchdetails'];
              if(batchDetail == undefined || batchDetail == "" || batchDetail=="[]"){
                  batchdetailcount++;
              }
            }
            
//            if(this.isConsignment && this.isCustomer && this.isOrder && this.savecount==1 && (batchdetailcount>0)){ //in consignment sales return proces
//                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.consignment.batchserialdetails"),function(btn){
//                if(btn!="yes") {
//                        batchserialAvailaleorNot=false
//                        this.saveDatawithBatchDeatils(rec,detail,incash,batchserialAvailaleorNot);
//                    }else{
                       
                       this.saveDatawithBatchDeatils(rec,detail,incash,batchserialAvailaleorNot);
                     
//                    }
//                },this);
//            }else{
                batchserialAvailaleorNot=false;
//                this.saveDatawithBatchDeatils(rec,detail,incash,batchserialAvailaleorNot);  
//            }
        }else{
          if(Wtf.account.companyAccountPref.memo== true && (rec.memo==""))    //memo related setting wether option is true
            {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText({
                    key:"acc.common.memoempty",
                    params:[Wtf.account.companyAccountPref.descriptionType]
                }),function(btn){
                    if(btn!="yes") {
                        return;
                    }
                    this.checkMemo(rec,detail,incash)                              
                },this);  
            }else {
                this.checkMemo(rec,detail,incash);     
            }
        }
        
    },
    saveDatawithBatchDeatils:function(rec,detail,incash,batchserialAvailaleorNot){
          var prodLength=this.Grid.getStore().data.items.length;
             var isblocked=this.lockQuantity.getValue();   //falg to get is so is blocked or not
            for(var i=0;i<prodLength-1;i++)
            { 
                var prodID=this.Grid.getStore().getAt(i).data['productid'];
            var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
            if(prorec==undefined){
                prorec=this.Grid.getStore().getAt(i);
            }
             var availableQuantity = prorec.data.quantity;
             var productQty= this.Grid.getStore().getAt(i).data['quantity'];
             //if products avaibale quantity is gereater tahn requiored quantity then only save batchserial detail otherwise save details as it is
            if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory  || Wtf.account.companyAccountPref.islocationcompulsory || Wtf.account.companyAccountPref.iswarehousecompulsory){ //if company level option is on then only check batch and serial details
                if(((Wtf.account.companyAccountPref.activateCRblockingWithoutStock && isblocked) || (!Wtf.account.companyAccountPref.activateCRblockingWithoutStock && isblocked)|| this.isInvoice) && (prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct)&& this.moduleid != Wtf.Acc_ConsignmentVendorRequest_ModuleId){  
                    if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'){
                        var batchDetail= this.Grid.getStore().getAt(i).data['batchdetails'];
                      //  var productQty= this.Grid.getStore().getAt(i).data['quantity'];
                        var baseUOMRateQty= this.Grid.getStore().getAt(i).data['baseuomrate'];
                        if(batchserialAvailaleorNot || this.isOrder){
                        if(batchDetail == undefined || batchDetail == "" || batchDetail=="[]"){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                            return;
                        }else{
                            var jsonBatchDetails= eval(batchDetail);
                            var batchQty=0;
                             for(var batchCnt=0;batchCnt<jsonBatchDetails.length;batchCnt++){
                                         if(jsonBatchDetails[batchCnt].quantity>0){
                                             if(prorec.data.isSerialForProduct){
                                              batchQty=batchQty+ parseInt(jsonBatchDetails[batchCnt].quantity);
                                           }else{
                                              batchQty=batchQty+ parseFloat(jsonBatchDetails[batchCnt].quantity);
                                          }
                                         }
                                     }
                        
                            if((batchQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) != (productQty*baseUOMRateQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);
                                return;
                            }                       
                        }
                       }
                    }
                }
                }
            } 
            if(Wtf.account.companyAccountPref.memo== true && (rec.memo==""))    //memo related setting wether option is true
            {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText({
                    key:"acc.common.memoempty",
                    params:[Wtf.account.companyAccountPref.descriptionType]
                }),function(btn){
                    if(btn!="yes") {
                        return;
                    }
                    this.checkMemo(rec,detail,incash,batchserialAvailaleorNot)                              
                },this);  
            }else {
                this.checkMemo(rec,detail,incash,batchserialAvailaleorNot);     
            }
    },
    checkMemo:function(rec,detail,incash,batchserialAvailaleorNot){
               if(this.businessPerson=="Vendor" && !this.isOrder){//Only for cash purchase and vendor invoice
                    Wtf.Ajax.requestEx({
                        url:"ACCReports/getAccountsExceedingBudget.do",
                        params: {
                            detail: detail,
                            stdate: this.getDates(true).format("M d, Y h:i:s A"),
                            enddate: this.getDates(false).format("M d, Y h:i:s A"),
                            isExpenseInv: this.isExpenseInv,
                            billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
                        }
                    },this,function(response){
                        if(response.data && response.data.length > 0){
                            var accMsg = "Following Accounts are exceeding their Monthly Budget Limit.<br><br><center>";
                            var budgetMsg = "";
                            for(var i=0; i< response.data.length; i++){
                                var recTemp = response.data[i];
                                if (!this.isCustBill)
                                    budgetMsg = (recTemp.productName == "" ? "" : "<b>Product: </b>" + recTemp.productName + ",") + " <b>Account: </b>" + recTemp.accountName + ", <b>Balance: </b>" + recTemp.accountBalance + ", <b>Budget: </b>" + recTemp.accountBudget;
                                else
                                    budgetMsg = (recTemp.productName == "" ? "" : "<b>Job Description: </b>" + recTemp.productName + ",") + " <b>Account: </b>" + recTemp.accountName + ", <b>Balance: </b>" + recTemp.accountBalance + ", <b>Budget: </b>" + recTemp.accountBudget;
                                accMsg += budgetMsg + "<br>";
                            }

                            accMsg += "<br>Do you wish to proceed ?</center>";
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),accMsg,function(btn){
                                if(btn!="yes") {return;}
                                this.checkLimit(rec,detail,incash,batchserialAvailaleorNot);
                            },this);
                        }else{
                            this.checkLimit(rec,detail,incash,batchserialAvailaleorNot);
                        }
                    },function(response){
                        this.checkLimit(rec,detail,incash,batchserialAvailaleorNot);
                    });

                }else{
                    this.checkLimit(rec,detail,incash,batchserialAvailaleorNot);
                } 
             
   },
    
    saveTemplate : function(){
        if(this.createTransactionAlso){
            this.transactionType = 1;
            this.save();
            Wtf.getCmp("emailbut" + this.id).show();
            Wtf.getCmp("exportpdf" + this.id).show();
        }else{
            if(this.Name.getValue() == ''){
                    var fieldLabel = this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") ; //this.businessPerson+"*",
                    WtfComMsgBox(["Warning","Please select "+fieldLabel], 2);
                    return;
            }
            if(this.isCustBill){
                for(var datacount=0;datacount<this.Grid.getStore().getCount();datacount++){
                    var creditoracc=this.Grid.getStore().getAt(datacount).data['creditoraccount'];                    
                    if(creditoracc==undefined||creditoracc==""){
                        if(this.Grid.getStore().getAt(datacount).data['productdetail'].length>0){
                            var account=(this.isCustomer)?"Credit account":"Debit account";
                            WtfComMsgBox(["Warning","Please select "+account], 2);
                            return;
                        } 
                    }            
                }
                var count=this.Grid.getStore().getCount();
                if(count<=1){
                    WtfComMsgBox(33, 2);
                    return;
                }  
             }
            this.transactionType = 2;
            var rec=this.NorthForm.getForm().getValues();
            rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            var detail = this.Grid.getProductDetails();
            var incash=this.cash;
            this.ajxurl = "";
            if(this.businessPerson=="Customer"){
                if(this.quotation)
                    this.ajxurl = "ACCSalesOrder/saveQuotation.do";
                else
                    this.ajxurl = "ACC" + (this.isOrder?(this.isCustBill?"SalesOrder/saveBillingSalesOrder":"SalesOrder/saveSalesOrder"):(this.isCustBill?"Invoice/saveBillingInvoice":"Invoice/saveInvoice")) + ".do";
            }else if(this.businessPerson=="Vendor"){
                if(this.quotation){
                    this.ajxurl = "ACCPurchaseOrder/saveQuotation.do";
                } else {
                    this.ajxurl = "ACC"+ (this.isOrder?(this.isCustBill?"PurchaseOrder/saveBillingPurchaseOrder":"PurchaseOrder/savePurchaseOrder"):(this.isCustBill?"GoodsReceipt/saveBillingGoodsReceipt":"GoodsReceipt/saveGoodsReceipt")) +".do";
                }
                
            }
            this.showConfirmAndSave(rec,detail,incash);
       }
    },
    
    
    checkLimit:function(rec,detail,incash,batchserialAvailaleorNot){
        if(!this.quotation && !this.isOrder && !this.cash){
            if(rec!=null&&rec!=undefined&&this.calTotalAmount()!=null)
            {
                if(rec.customer!=null&&rec.customer!="")
                    {
                        rec.customerid=rec.customer;
                    }
                rec.totalSUM=this.calTotalAmount()+this.caltax();
                Wtf.Ajax.requestEx({
                    url:"ACC"+this.businessPerson+"CMN/get"+this.businessPerson+"Exceeding"+(this.businessPerson=="Vendor"?"Debit":"Credit")+"Limit.do",
                    params:rec                                                                                                                                            
                },this,function(response){
                    if(response.data && response.data.length > 0){
                        var msg = (this.businessPerson=="Vendor"?WtfGlobal.getLocaleText("acc.cust.debitLimit"):WtfGlobal.getLocaleText("acc.cust.creditLimit"))+" "+WtfGlobal.getLocaleText("acc.field.forthis")+" "+(this.businessPerson== "Vendor"?WtfGlobal.getLocaleText("acc.agedPay.ven"):WtfGlobal.getLocaleText("acc.agedPay.cus"))+" "+WtfGlobal.getLocaleText("acc.field.hasreached")+"<center><br>";
                        var limitMsg = "";
                        for(var i=0; i< response.data.length; i++){
                            var recTemp = response.data[i];
                            limitMsg = (recTemp.name == "" ? "" : "<b> "+this.businessPerson+": </b>" + recTemp.name + ", ") +"<b>"+" "+WtfGlobal.getLocaleText("acc.field.amountdue")+": </b>" + " "+WtfGlobal.conventInDecimalWithoutSymbol(recTemp.amountDue) + ", <b>"+(this.businessPerson=="Vendor"?WtfGlobal.getLocaleText("acc.cust.debitLimit"):WtfGlobal.getLocaleText("acc.cust.creditLimit"))+": </b>" + recTemp.limit+". ";
                            msg += limitMsg;
                        }
                        var limitControlType="";
                        if(this.isCustomer){
                            limitControlType=Wtf.account.companyAccountPref.custcreditlimit;
                        } else {
                            limitControlType=Wtf.account.companyAccountPref.vendorcreditcontrol;
                        }
                        if(limitControlType == Wtf.controlCases.BLOCK){//block
                            msg += "You cannot proceed.";
                            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),msg],3);
                            return;
                        }else if(limitControlType == Wtf.controlCases.WARN){//warn
                           
                            msg += WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed");
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),msg,function(btn){
                                if(btn!="yes") {
                                    return;
                                }
                                this.showConfirmAndSave(rec,detail,incash,batchserialAvailaleorNot);
                            },this);
                        }else{//ignore
                            this.showConfirmAndSave(rec,detail,incash,batchserialAvailaleorNot);
                        }

                    }else{
                            this.showConfirmAndSave(rec,detail,incash,batchserialAvailaleorNot);
                        }
                },function(response){
                        this.showConfirmAndSave(rec,detail,incash,batchserialAvailaleorNot);
                });                
            }else{
                    this.showConfirmAndSave(rec,detail,incash,batchserialAvailaleorNot);
                }
        }else{
            this.showConfirmAndSave(rec,detail,incash,batchserialAvailaleorNot);
        }
    },
    
    showConfirmAndSave: function(rec,detail,incash,batchserialAvailaleorNot){
        
        if(this.EditisAutoCreateDO && (this.isFixedAsset || this.isLeaseFixedAsset)){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.invoice.msg17") ], 3);
            return;
        }

        var promptmessage = this.EditisAutoCreateDO ?  WtfGlobal.getLocaleText("acc.invoice.msg16"):WtfGlobal.getLocaleText("acc.invoice.msg7");
        if (Wtf.Countryid == Wtf.CountryID.MALAYSIA && WtfGlobal.isNonZeroRatedTaxCodeUsedInTransaction(this)) {
            promptmessage = this.EditisAutoCreateDO ? WtfGlobal.getLocaleText("acc.customerInvoice.nonZeroTaxcode.alert") : WtfGlobal.getLocaleText("acc.tax.nonZeroTaxcode.alert");
        }

        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),promptmessage,function(btn){
                if(btn!="yes") {this.savecount--;return;}
                rec.taxid=this.Tax.getValue();
                rec.isfavourite = false;
                if(!this.copyInv){
                    if((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)){
                        rec.isfavourite = this.record.get('isfavourite');
                    }
                }
                rec.taxamount=this.caltax();
                if(this.isExpenseInv){
                    rec.expensedetail=detail;
                    rec.isExpenseInv=this.isExpenseInv;
                }
                else
                    rec.detail=detail;
                
                var custFieldArr=[];
//                if(this.moduleid == Wtf.Acc_Lease_Order && this.isLeaseFixedAsset){
                    custFieldArr=this.tagsFieldset.createFieldValuesArray();
//                }
                
                if(this.copyInv && this.record && this.record.data.contract){
                     rec.contractId=this.record.data.contract;
                }
                
                this.msg= WtfComMsgBox(27,4,true);
                rec.subTotal=this.Grid.calSubtotal()
                this.applyCurrencySymbol();
                rec.batchserialAvailaleorNot=batchserialAvailaleorNot;
                rec.perdiscount=this.perDiscount.getValue();
                rec.isOpeningBalanceOrder=this.isOpeningBalanceOrder;
                rec.currencyid=this.Currency.getValue();
                rec.externalcurrencyrate=this.externalcurrencyrate;
                rec.discount=this.Discount.getValue();
                rec.posttext=this.postText;
                rec.istemplate=this.transactionType;
                rec.moduletempname=this.isTemplate;
                rec.templatename=this.moduleTemplateName.getValue();
                rec.isFixedAsset=this.isFixedAsset;
                rec.isLeaseFixedAsset=this.isLeaseFixedAsset;
                rec.isConsignment=this.isConsignment;
                rec.custWarehouse= this.warehouses.getValue();
                rec.movementtype= this.movmentType.getValue();
                rec.batchDetails=this.Grid.batchDetails;
                                
//                rec.vendorinvoice = this.vendorInvoice!=null?this.vendorInvoice.getValue():'';
                if (custFieldArr.length > 0)
                    rec.customfield = JSON.stringify(custFieldArr);
                rec.invoicetermsmap = this.getInvoiceTermDetails();
                if(this.Grid.deleteStore!=undefined && this.Grid.deleteStore.data.length>0)
                    rec.deletedData=this.getJSONArray(this.Grid.deleteStore,false,0);
                rec.number=this.Number.getValue();
                rec.linkNumber=(this.PO != undefined && this.PO.getValue()!="")?this.PO.getValue():"";
                if(this.isLeaseFixedAsset && (this.isOrder || this.quotation)){
                    if(this.fromLinkCombo && this.fromLinkCombo.getValue() == 1){
                        rec.isLinkedFromReplacementNumber=true;
                        rec.replacementId=this.PO.getValue();
                    }
                    if(this.isOrder && this.fromLinkCombo && this.fromLinkCombo.getValue() == 0){// if SO linked with CQ
                        rec.isLinkedFromCustomerQuotation=true;
                    }
                    
                }
                rec.fromLinkCombo=this.fromLinkCombo.getRawValue();
                rec.duedate=WtfGlobal.convertToGenericDate(this.DueDate.getValue());
                rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
                rec.shipdate=WtfGlobal.convertToGenericDate(this.shipDate.getValue());
                rec.validdate=WtfGlobal.convertToGenericDate(this.validTillDate.getValue());
                rec.invoiceid=this.copyInv?"":this.billid;
                rec.doid=this.DeliveryOrderid;
                rec.mode=(this.isOrder?(this.isCustBill?51:41):(this.isCustBill?13:11));
                rec.incash=incash;
                rec.partialinv = (this.partialInvoiceCmb)? this.partialInvoiceCmb.getValue() : false;
                this.totalAmount = rec.subTotal + rec.taxamount - this.getDiscount();
                rec.includeprotax = (this.includeProTax)? this.includeProTax.getValue() : false;
                rec.landedInvoiceNumber = this.invoiceList.getValue();
                rec.customer = this.Name.getValue();
                if(this.autoGenerateDO.getValue() ||  this.EditisAutoCreateDO){
                    var seqFormatRecDo=WtfGlobal.searchRecord(this.sequenceFormatStoreDo, this.sequenceFormatComboboxDo.getValue(), 'id');
                    rec.seqformat_oldflagDo=seqFormatRecDo!=null?seqFormatRecDo.get('oldflag'):false;
                    rec.numberDo = this.no.getValue();
                    rec.sequenceformatDo=this.sequenceFormatComboboxDo.getValue();
                }
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):false;
                rec.isEdit = this.isEdit; //    ERP-18011
            var isCopy = this.copyInv;
            var isEdit = this.isEdit;
            if (this.isVenOrCustSelect) {
                isEdit = false;
                isCopy = false;
            }
                rec=WtfGlobal.getAddressRecordsForSave(rec,this.record,this.linkRecord,this.currentAddressDetailrec,this.isCustomer,this.singleLink,isEdit,isCopy);
                if(this.isAutoCreateDO ||  this.EditisAutoCreateDO){
                    rec.isAutoCreateDO = this.EditisAutoCreateDO ? this.EditisAutoCreateDO : this.isAutoCreateDO;
                    rec.fromLinkComboAutoDO =this.isCustomer ? "Customer Invoice" : "Vendor Invoice";
                }
                if(this.capitalGoodsAcquired){
                    rec.isCapitalGoodsAcquired = this.capitalGoodsAcquired.getValue();
                }
                Wtf.Ajax.requestEx({
                    url:this.ajxurl,
//                    url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);

            },this);
    },
    
    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
getFinancialYRStartDatesMinOne:function(start){
    var d=Wtf.serverDate;
    //        if(this.statementType=='BalanceSheet'&&start)
    //             return new Date('January 1, 1970 00:00:00 AM');
    var monthDateStr=d.format('M d');
    if(Wtf.account.companyAccountPref.bbfrom)
        monthDateStr=Wtf.account.companyAccountPref.bbfrom.format('M d');
    var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
    if(d<fd)
        fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
    if(start)
        return fd.add(Date.YEAR, 0).add(Date.DAY, -1);
    return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
},

    loadTransStore : function(productid){
        if(this.Name.getValue() != ""){
            var customer= (this.businessPerson=="Vendor")? "" : this.Name.getValue();
            var vendor= (this.businessPerson=="Vendor")? this.Name.getValue() : "" ;
            if((productid == undefined || productid == "") && this.Grid.getStore().getCount() > 0){
                productid = this.Grid.getStore().getAt(0).get("productid");
            }
            this.lastTransPanel.Store.on('load', function(){
                Wtf.getCmp('south' + this.id).doLayout();
            }, this);
            if(productid) {
                this.lastTransPanel.productid = productid;
                this.lastTransPanel.Store.load({
                    params:{
                        start:0,
                        limit:5, 
                        prodfiltercustid:customer,
                        prodfilterventid:vendor,
                        productid : productid
                    }
                });
           }
//           this.wareHouseStore.load({params:{customerid:this.Name.getValue()}});  //on customer change load the warehouses of particular customer
        }
    },
    
    removeTransStore : function(){
        this.lastTransPanel.Store.removeAll();
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0});
    },
    
    updateData:function(){
        var customer= this.Name.getValue();
       if(Wtf.getCmp("showaddress" + this.id)){
          Wtf.getCmp("showaddress" + this.id).enable(); 
       } 
        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
         var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
            if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            } else {
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
        if(this.Grid) {
            this.Grid.affecteduser = this.Name.getValue();
        }
        
        this.loadTransStore();
        if(!(this.isCustBill||this.isExpenseInv|| this.isEdit)) {
            if(!(this.fromPO && this.fromPO.getValue())){
                var val = this.billDate.getValue();
//                this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore, customer);
            }
            
        }
        Wtf.Ajax.requestEx({
            url:"ACC"+this.businessPerson+"CMN/getCurrencyInfo.do",
//            url:Wtf.req.account+this.businessPerson+'Manager.jsp',
            params:{
                mode:4,
                customerid:customer,
                isBilling : this.isCustBill
            }
        }, this,this.setCurrencyInfo);       
        if(this.fromPO){
            this.fromPO.enable();
        }
    },
    
    setCurrencyInfo:function(response){
        if(response.success){
            this.externalcurrencyrate=0;
            this.custdatechange=true;
            this.Currency.setValue(response.currencyid);
            this.currencyid=response.currencyid;
            this.symbol = response.currencysymbol;
            var taxid = response.taxid
            var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
            var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            } else {
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
            this.custChange=true;
            this.changeCurrencyStore();

            if(this.fromPO)					// Currency id issue 20018
            	this.currencyStore.load();
            this.amountdue=0;
            this.amountdue=response.amountdue;
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.currencyRenderer(0),discount:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),termtotal:WtfGlobal.currencyRenderer(0),amountbeforetax:WtfGlobal.currencyRenderer(0),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,this.symbol)})
        }
    },
    getTerm:function(val1,val2){
            if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {
                val1=new Date(this.record.data.shipdate);
            } else {
                val1=new Date(this.record.data.date);
            }
        val2=new Date(this.record.data.duedate);
        var msPerDay = 24 * 60 * 60 * 1000
        var termdays = Math.floor((val2-val1)/ msPerDay) ;
        var FIND =termdays;
        var index=this.termds.findBy( function(rec){
             var parentname=rec.data.termdays;
            if(parentname==FIND)
                return true;
             else
                return false
            })
            if(index>=0){
                var  rec=this.termds.getAt(index)
                this.Term.setValue(rec.data.termdays);
            }
    },
    getCreditTo:function(val){
        var index=this.allAccountStore.findBy( function(rec){
             var name=rec.data.accid;
            if(name==val)
                return true;
             else
                return false
            },this)
            if(index>=0)
                this.creditTo.setValue(val);
    },
    updateDueDate:function(a,val){
        var term=null;
        if(this.Term.getValue()!=""&&isNaN(this.Term.getValue())==false && (this.billDate.getValue()!=undefined && this.billDate.getValue()!="") ){
            term=new Date(this.billDate.getValue()).add(Date.DAY, this.Term.getValue());
        }
        else
            term=this.billDate.getValue();
        
        if(Wtf.account.companyAccountPref.shipDateConfiguration) {
            if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {
                if(this.shipDate.getValue() != "") {
                    if(this.Term.getValue()!="" && isNaN(this.Term.getValue()) == false) {
                        term = new Date(this.shipDate.getValue()).add(Date.DAY, this.Term.getValue());
                    } else {
                        term = this.shipDate.getValue();
                    }
                } else {
                    term = null;
                }
            }
        }
        
        if(term != null) {
            this.NorthForm.getForm().setValues({duedate:term});
        }
        if(this.Grid){
            this.Grid.billDate = this.billDate.getValue()
        }
        var rec = this.Term.store.getAt(this.Term.store.find('termdays',this.Term.getValue()));
        if(rec != null && rec != undefined)
            this.termid=rec.data.termid;
    },

    genSuccessResponse:function(response, request){
        this.RecordID=response.SOID!=undefined?response.SOID : response.invoiceid;
        Wtf.dupsrno.length=0;
        if(response.success && this.GENERATE_PO) {
            if(response.pendingapproval == 1) {
                WtfComMsgBox([this.titlel,"Purchase Order successfully generated but pending for Approval. PO Number : " + this.Number.getValue()],response.success*2+1);
            } else {
                WtfComMsgBox([this.titlel,"Purchase Order successfully generated. PO Number : " + this.Number.getValue()],response.success*2+1);
            }
            
        }if(response.success && this.GENERATE_SO) {
              if(response.pendingapproval == 1) {
                WtfComMsgBox([this.titlel,"Sales Order successfully generated but pending for Approval. SO Number : " + this.Number.getValue()],response.success*2+1);
            } else {
                WtfComMsgBox([this.titlel,"Sales Order successfully generated. SO Number : " + this.Number.getValue()],response.success*2+1);
            }
            
        } else if (response.isTaxDeactivated) {
            WtfComMsgBox([this.title, response.msg], 2);
        } else {
            WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
        }
        var rec=this.NorthForm.getForm().getValues();
        this.exportRecord=rec;
        this.exportRecord['billid']=response.billid||response.invoiceid;
        this.exportRecord['billno']=response.billno||response.invoiceNo;
        this.exportRecord['amount']=(this.moduleid==22||this.moduleid==23||this.moduleid==63||this.moduleid==50)?this.totalAmount:response.amount;
        this.exportRecord['isConsignment']=this.isConsignment;
        this.singlePrint.exportRecord=this.exportRecord;
        if (this.singleRowPrint) {
            this.singleRowPrint.exportRecord = this.exportRecord;      
        }
        
         if(response.success){
             if(this.productOptimizedFlag==Wtf.Show_all_Products){
               if(!this.isCustBill){
                 Wtf.productStoreSales.reload();
                Wtf.productStore.reload();   //Reload all product information to reflect new quantity, price etc                
               }            	
             }
            if(this.isGenerateReceipt){
                var mode = "";                
                if(this.businessPerson=="Customer"){
                    if(!this.quotation && !this.isOrder){
                        mode = (this.isCustBill?23:22);
                    }
                }
                var fileName="Cash Sales Payment Recieved "+response.invoiceNo;
                var selRec = "&amount="+this.totalAmount+"&bills="+response.invoiceid+"&customer=Cash&accname="+response.accountName+"&personid="+response.accountid;//+"&address="+recData.address;
                Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+response.accountid+"&filename="+fileName+"&filetype=pdf"
            }
            
            if(this.isTemplate){
                this.ownerCt.remove(this);
            }
            
            if(this.mailFlag){
                this.loadUserStoreForInvoice(response, request);
                this.disableComponent();
//                Wtf.getCmp("emailbut" + this.id).enable();
//                Wtf.getCmp("exportpdf" + this.id).enable();
                this.response = response;
                this.request = request;
                this.fireEvent('update',this);
                return;
            }
            this.currentAddressDetailrec="";
        this.singleLink = false;
        this.isVenOrCustSelect=false;
        this.lastTransPanel.Store.removeAll();
        this.symbol = WtfGlobal.getCurrencySymbol();
        this.currencyid = WtfGlobal.getCurrencyID();
        if(this.Memo){
            this.Memo.qtip="";
            Wtf.QuickTips.register({
                target: this.Memo.getEl(),
                trackMouse: true,
                text: this.Memo.qtip
            });
        }
        this.loadStore();
        this.fromPO.disable();
            this.currencyStore.load(); 
            this.Grid.movmentType="";
            this.Currency.setValue(WtfGlobal.getCurrencyID()); // Reset to base currency 
            this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.           
            Wtf.dirtyStore.product = true;
            var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
         var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            }else{
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
            if (this.toggleBtnPanel && this.isConsignment && this.moduleid === Wtf.Acc_ConsignmentInvoice_ModuleId) {
                this.toggleBtnPanel.getComponent(0).disable()
            }
            this.postText="";
            var customFieldArray = this.tagsFieldset.customFieldArray;
            for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                var fieldId = customFieldArray[itemcnt].id
                if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                       Wtf.getCmp(fieldId).reset();
                }
            }    
            var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
            for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                if (Wtf.getCmp(checkfieldId) != undefined) {
                       Wtf.getCmp(checkfieldId).reset();
                }
            } 
            var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  //Reset Custom Dimension
            for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
                var fieldId1 = customDimensionArray[itemcnt1].id
                if (Wtf.getCmp(fieldId1) != undefined) {
                       Wtf.getCmp(fieldId1).reset();
                }
            } 
            this.fireEvent('update',this);
            this.amountdue=0;
       }
    },

    genFailureResponse:function(response){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    
callEmailWindowFunction : function(response, request){
    
    if(response.pendingApproval){
        var titleMsg = this.getLables();
        WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),titleMsg+' is pending for approval, So you cannot send mail right now.'],3);
        return;
    }
    if(this.CustomStore != null){
        var rec="";
        if(response.billid!=undefined && response.billid!=''){
            rec = this.CustomStore.getAt(this.CustomStore.find('billid',response.billid));
        }else if(response.invoiceid!=undefined && response.invoiceid!=''){
            rec = this.CustomStore.getAt(this.CustomStore.find('billid',response.invoiceid));
        }
        var label = "";
        if(this.cash){
            if(this.isCustomer){
                label = "Cash Sales Receipt";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,11,true,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,2,true,false,false,true);
                }
            }else{
                label = "Cash Purchase Receipt";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,15,false,false,false,true);
                }else{
                    callEmailWin("emailwin",rec,label,6,false,false,false,true);
                }
            }
        } else if(this.isOrder && !this.quotation){
            if(this.isCustomer){
                label =this.isConsignment?WtfGlobal.getLocaleText("acc.consignment.order"): "Sales Order ";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,17,true,false,false,false,true);
                }else{
                   // this.isConsignment?callEmailWin("emailwin",rec,label,53,true,false,false,false,true,false,false,false,false,true):callEmailWin("emailwin",rec,label,53,true,false,false,false,true,false,false,false,false,false);
                    this.isConsignment?callEmailWin("emailwin",rec,label,1,true,false,false,false,true,false,false,false,false,true):callEmailWin("emailwin",rec,label,1,true,false,false,false,true,false,false,false,false,false);
               }
            }else{
            label =this.isConsignment?WtfGlobal.getLocaleText("acc.venconsignment.order"): "Purchase Order ";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,18,false,false,false,false,true);
                    
                }else{
                                                                                                                                      
                    this.isConsignment?callEmailWin("emailwin",rec,label,5,false,false,false,false,true,false,false,false,false,true):callEmailWin("emailwin",rec,label,5,false,false,false,false,true,false,false,false,false,false);
                }
            }
        } else if(this.quotation){
            if(this.isCustomer){
                label = WtfGlobal.getLocaleText("acc.accPref.autoCQN");
                callEmailWin("emailwin",rec,label,50,true,false,true);
            }else{
                label = "Vendor Quotation";
                callEmailWin("emailwin",rec,label,57,false,false,true);
            }
        }else{
            if(this.isCustomer){
                label =  this.isConsignment?"Consignment Sales Invoice":"Sales Invoice";
                if(rec.data.withoutinventory){
                    this.isConsignment?callEmailWin("emailwin",rec,label,11,true,true,false,false,true,false,false,false,false,true):callEmailWin("emailwin",rec,label,11,true,true);
                }else{
                    this.isConsignment?callEmailWin("emailwin",rec,label,2,true,true,false,false,true,false,false,false,false,true):callEmailWin("emailwin",rec,label,2,true,true);
                }
            }else{
                label =  this.isConsignment?"Consignment Purchase Invoice":"Purchase Invoice";
                if(rec.data.withoutinventory){
                    callEmailWin("emailwin",rec,label,15,false,true);
                }else{
                this.isConsignment?callEmailWin("emailwin",rec,label,6,false,true,false,false,false,false,false,false,false,true):callEmailWin("emailwin",rec,label,6,false,true,false,false,false,false,false,false,false,false);
               }
            }
        }
    }
},

getLables : function(){
    var label = "";
    if(this.cash){
        if(this.isCustomer){
            label = "Cash Sales Receipt";
        }else{
            label = "Cash Purchase Receipt";
        }
    }else if(this.isOrder && !this.quotation){
        if(this.isCustomer){
            label = "Sales Order";
        }else{
            label = "Purchase Order";
        }
    }else if(this.quotation){
        if(this.isCustomer){
            label = "Customer Quotation";
        }else{
            label = "Vendor Quotation";
        }
    }else{
        if(this.isCustomer){
            label = "Customer Invoice";
        }else{
            label = "Vendor Invoice";
        }
    }
    
    return label;
},

disableComponent: function(){ // disable following component in case of save button press.
   
    if(this.fromLinkCombo && this.fromLinkCombo.getValue() === ''){
       // this.fromLinkCombo.emptyText = "";
        this.fromLinkCombo.clearValue();
    }
    
    if(this.PO && this.PO.getValue() === ''){
        this.handleEmptyText=true;
     //   this.PO.emptyText = "";
        this.PO.clearValue();        
    }

    if(Wtf.getCmp("savencreate" + this.heplmodeid + this.id)){
        Wtf.getCmp("savencreate" + this.heplmodeid + this.id).disable();
    }
    if(Wtf.getCmp("save" + this.heplmodeid + this.id)){
        Wtf.getCmp("save" + this.heplmodeid + this.id).disable();
    }
    
    if(Wtf.getCmp("posttext" + this.id)){
        Wtf.getCmp("posttext" + this.id).disable();
    }
    
    if(Wtf.getCmp("showaddress" + this.id)){
        Wtf.getCmp("showaddress" + this.id).disable(); 
    } 
       
    if(this.Grid){
        var GridStore = this.Grid.getStore();
        var count2 = GridStore.getCount();
        var lastRec2 = GridStore.getAt(count2-1);
        GridStore.remove(lastRec2);
    }

    if(this.GridPanel){
        this.GridPanel.disable();
    }else{
        this.Grid.disable();
    }

    if(this.NorthForm){
        this.NorthForm.disable();
    }

    if(this.southPanel){
        this.southPanel.disable();
    }
},


fillData : function(store){
    this.loadingMask.hide();
    var rec = store.getAt(0);
    this.openModuleTab(rec);
    this.ownerCt.remove(this);
},

openModuleTab:function(formrec){
    var templateId = this.templateModelCombo.getValue();
    var copyInv = true;
    var isQuotation = false;
    var isQuotation =formrec.get("isQuotation");
    WtfGlobal.openModuleTab(this, this.isCustomer, isQuotation, this.isOrder, copyInv, templateId, formrec);
},

loadSelectedTemplateRecord : function(recordId){
    
    this.SelectedTemplateStore = new Wtf.data.Store({
            url:this.SelectedTemplateStoreUrl,
            scope:this,
            baseParams:{
                archieve:0,
//                costCenterId: this.CostCenter.getValue(),
                deleted:false,
                nondeleted:false,
                cashonly:(this.cash == undefined)?false:this.cash,
                creditonly:false,
                consolidateFlag:false,
                companyids:companyids,
                enddate:'',
//                pendingapproval:response.pendingApproval,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                startdate:'',
                moduleRecordId:recordId
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.SelectedTemplateRec)
        });
        this.SelectedTemplateStore.load();
},

                
loadUserStoreForInvoice : function(response, request){
    
   var customRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'billingEmail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
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
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'rowproductname'},
        {name:'rowquantity'},
        {name:'rowrate'},
        {name:'rowprdiscount'},
        {name:'rowprtaxpercent'},
        {name:'agent'},
        {name:'isConsignment'}
        
    ]);
    
    var customStoreUrl = "";
    
    if(this.isOrder && !this.quotation){
        customStoreUrl= this.businessPerson=="Customer" ? "ACCSalesOrderCMN/getSalesOrdersMerged.do":"ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do"  
    }else if(this.quotation){
        customStoreUrl = this.isCustomer? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
    }else{
        customStoreUrl= this.businessPerson=="Customer" ? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    }
    this.CustomStore = new Wtf.data.GroupingStore({
            url:customStoreUrl,
            scope:this,
            baseParams:{
                archieve:0,
                costCenterId: this.CostCenter.getValue(),
                deleted:false,
                nondeleted:false,
                cashonly:(this.cash == undefined)?false:this.cash,
                creditonly:false,
                consolidateFlag:false,
                companyids:companyids,
                enddate:'',
                pendingapproval:response.pendingApproval,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                startdate:'',
                ss:request.params.number,
                isFixedAsset:this.isFixedAsset,
                isConsignment:this.isConsignment,
                includepending:true   // this flag is true after saving request we are reloading the store at that time we do not need check for pending request
            },
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },customRec)
        });
        
        this.CustomStore.on('load', this.enableButtons(), this);
        this.CustomStore.load();
        
},

enableButtons : function(){
    Wtf.getCmp("emailbut" + this.id).enable();
    Wtf.getCmp("exportpdf" + this.id).enable();
    Wtf.getCmp("printSingleRecord" + this.id).enable();
    if(Wtf.getCmp("RecurringSO"))
    Wtf.getCmp("RecurringSO").enable(); 
},

exportPdfFunction : function(response, request){
    if(this.CustomStore != null){
        var rec = this.CustomStore.getAt(0);
        var recData = rec.data;
        var billno = recData.billno;
        var selRec = "&amount="+recData.amount+"&isexpenseinv="+recData.isexpenseinv+"&bills="+recData.billid;
        var fileName = this.label+" "+billno; 
        var mode = "";
        if(this.cash){
            if(this.isCustomer){
                if(recData.withoutinventory){
                    mode = 11;
                }else{
                    mode = 2;
                }
            }else{
                if(recData.withoutinventory){
                    mode = 15;
                }else{
                    mode = 6;
                }
            }
        } else if(this.isOrder && !this.quotation){
            if(this.isCustomer){
                fileName=this.isConsignment?WtfGlobal.getLocaleText("acc.field.consignmentrequestorder")+" ": "Sales Order "+recData.billno;
                if(recData.withoutinventory){
                    mode = 17;
                }else{
                    mode = 1;
                }
            }else{
                fileName=this.isConsignment?WtfGlobal.getLocaleText("acc.venconsignment.order")+" ": "Purchase Order "+recData.billno;
                if(recData.withoutinventory){
                    mode = 18;
                }else{
                    mode = 5;
                }
            }
        } else if(this.quotation){
            fileName="Quotation "+recData.billno;
            if(this.isCustomer){
                mode = 50;
            }else{
                mode = 57;
            }
        }else{
            if(this.isCustomer){
                fileName="Invoice "+recData.billno;
                if(recData.withoutinventory){
                    mode = 11;
                }else{
                    mode = 2;
                }
            }else{
                fileName="Vendor Invoice "+recData.billno;
                if(recData.withoutinventory){
                    mode = 15;
                }else{
                    mode = 6;
                }
            }
        }
         if (mode==50 && Wtf.templateflag == 2) {
            Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSenwanGroupSingleCustomerQuotation.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf";
        }
        else if ((mode==2||mode==11)&&!rec.data.isadvancepayment && Wtf.templateflag == 2) {
            Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSenwanCommercialInvoiceJasper.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf";
        }
        else if((mode==2||mode==11)&&!rec.data.isadvancepayment && Wtf.templateflag == 1){
              Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportCustomerInvoiceReport.do?moduleid=12&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf";
        }
        else if((mode==2||mode==11)&&!rec.data.isadvancepayment && Wtf.templateflag == 4){
              Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportLSHCustomerInvoiceReport.do?moduleid=12&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf";
        }
        else{
             Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+fileName+"&filetype=pdf";
        }
    }
},



    loadStore:function(){
//        if(!(this.isCustBill||this.isExpenseInv))
//            this.Grid.priceStore.purgeListeners();
        if(!this.isEdit && !this.copyInv){  //in edit and copy case do not remove record from store
                this.Grid.getStore().removeAll();
            }
//        this.setTransactionNumber();
        this.PO.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);
        if(this.isTemplate){
            this.createTransactionAlsoOldVal = this.createTransactionAlso;
            this.oldTempNameVal = this.moduleTemplateName.getValue();
        }
        
        if(!this.isEdit){
            this.NorthForm.getForm().reset();
        }
        this.sequenceFormatStore.load();
        if(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId )
        this.sequenceFormatStoreDo.load();
        if(this.isTemplate){
            this.moduleTemplateName.setValue(this.oldTempNameVal);
            if(this.createTransactionAlsoOldVal){
                this.createAsTransactionChk.setValue(true);
                this.Number.enable();
                this.sequenceFormatCombobox.enable();
            }
        }
        this.setTransactionNumber();
        if(this.fromPO){
            //this.PO.enable();
            this.fromPO.enable();
        }
        if(this.fromLinkCombo){
            this.fromLinkCombo.setDisabled(true);
            this.fromLinkCombo.clearValue();
        }
        this.fromPO.setValue(false); 
//        this.POStore.reload();			Code Optimizing :)  Unnecessary Reload removed
        if(!this.isEdit && !this.copyInv){   //in edit and copy case do not remove record from store
                    this.Grid.getStore().removeAll();
                }
        if(this.partialInvoiceCmb){
            this.partialInvoiceCmb.disable();
            var id=this.Grid.getId();
            var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
            if(rowindex != -1){
                this.Grid.getColumnModel().setHidden( rowindex,true);
            }
            
        }
//         var currentTaxItems=this.Name.store.data.items;
//         var currentTaxId="";
//         var customer= this.Name.getValue();
//            for(var i=0;i<currentTaxItems.length;i++)
//                {
//                     if(this.Name.store.data.items[i].json.accid==customer)
//                         {
//                             currentTaxId=this.Name.store.data.items[i].json.taxId;
//                         }
//                }
        this.showGridTax(null,null,true);
        this.Grid.symbol=undefined; // To reset currency symbol. BUG Fixed #16202
        this.Grid.updateRow(null);
        this.resetForm = true;
         var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
         var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                        this.isTaxable.setValue(true);
                        this.Tax.enable();
                        this.Tax.setValue(actualTaxId);
                }else{
                        this.Tax.setValue("");
                        this.Tax.setDisabled(true);				// 20148 fixed
                        this.isTaxable.setValue(false);
                }
        
        this.template.setValue(Wtf.Acc_Basic_Template_Id);
//        var date=WtfGlobal.convertToGenericDate(new Date())
//        if(!(this.isCustBill||this.isExpenseInv)) {
//            var affecteduser = this.Name.getValue();
////            this.Grid.loadPriceStoreOnly(new Date(),this.Grid.priceStore, affecteduser);
//        } else
        this.currencyStore.load({params:{tocurrencyid:WtfGlobal.getCurrencyID(),mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});
//        this.wareHouseStore.load({ params:{customer:this.Name.getValue()}});
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0});
        this.currencyStore.on("load",function(store){
            if(this.resetForm){
                if(this.currencyStore.getCount()<1){
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2); //"Please set Currency Exchange Rates"
                } else {
                    this.isCurrencyLoad=true;
//                    this.Currency.setValue(WtfGlobal.getCurrencyID());
//                    this.currencyid=WtfGlobal.getCurrencyID();
                    this.applyCurrencySymbol();
                    this.showGridTax(null,null,true);
                    var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
                     var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                        this.isTaxable.setValue(true);
                        this.Tax.enable();
                        this.Tax.setValue(actualTaxId);
                    } else {
                        this.isTaxable.setValue(false);
                        this.Tax.setValue('');
                        this.Tax.disable();
                    }
                    //                    this.applyTemplate(this.currencyStore,0);
                    this.resetForm = false;
        }
    }
},this);
},

    setDate:function(){
        var height = 0;
        if((this.isOrder&&!this.isCustomer)||(this.isOrder&&this.isCustomer)){
            height=430;
        }
        if(!this.quotation&&this.isOrder&&!this.isCustomer && !this.isCustBill && ((BCHLCompanyId.indexOf(companyid) != -1)))
            height=485;
        if(!this.isCustomer&&!this.isCash&&!this.isOrder&&!this.quotation)
            height=400;
        if(!this.isCustomer&&this.quotation)
            height=310;
        if(this.isCustBill){
        	if(this.isEdit)
                this.allAccountStore.on('load',this.getCreditTo.createDelegate(this,[this.record.data.crdraccid]),this)
            this.allAccountStore.load();
            height+=20;
        }
        if(height>=178) this.NorthForm.setHeight(height);

        if(!this.isEdit || this.isCopyFromTemplate){
            this.Discount.setValue(0);
            if(this.isOpeningBalanceOrder){
                this.billDate.setValue(this.getFinancialYRStartDatesMinOne(true));
            }else{
                this.billDate.setValue(new Date());//(new Date());
            }
            this.shipDate.setValue(new Date());//(new Date());
//            this.DueDate.setValue(Wtf.serverDate);
        }
    },
    addTax:function(){
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.Grid.taxStore.reload();}, this);
    },
    addCreditTerm:function(){
        callCreditTerm('credittermwin');
        Wtf.getCmp('credittermwin').on('update', function(){this.termds.reload();}, this);
    },
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
           this.isCustomer?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
        }, this);
    },
    
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },
setTransactionNumberDo:function(isSelectNoFromCombo){
    var format= this.isCustomer ? Wtf.account.companyAccountPref.autodo : Wtf.account.companyAccountPref.autogro;
    var temp2=this.isCustomer ? Wtf.autoNum.DeliveryOrder : Wtf.autoNum.GoodsReceiptOrder;
    if(isSelectNoFromCombo){
        this.fromnumberDo = temp2;
    } else if(format&&format.length>0){
        WtfGlobal.fetchAutoNumber(temp2, function(resp){
            if(this.isEdit)this.no.setValue(resp.data)
                }, this);
    }
},   
setDefaultSalesPerson:function(records){
    if(this.users){
        for(var i=0 ; i<records.data.length ; i++){
            var rec = records.data.get(i).store.data.items[i].json;
            if(rec && rec.userid == loginid && rec.hasAccess){
                if(!this.users.store){
                    this.users.store=this.isCustomer ? Wtf.salesPersonFilteredByCustomer : Wtf.agentStore
                }
                    this.defaultsalesperson = rec.id;//ERP-12165
                if(!this.isEdit){//ERP-11903
                //For Create New Consignment request value should be set but not in view  or Edit case.     
                    this.users.setValue(rec.id);
                }
                break;
            }
        }
    }
},
hideDO:function(){
    WtfGlobal.hideFormElement(this.sequenceFormatComboboxDo);
    WtfGlobal.hideFormElement(this.no);
    this.no.allowBlank = true;
},
showDO:function(){
    WtfGlobal.showFormElement(this.sequenceFormatComboboxDo);
    WtfGlobal.showFormElement(this.no);
    this.no.allowBlank = false;
      
    },
    setTransactionNumber:function(isSelectNoFromCombo){
    	if(this.quotation==null || this.quotation==undefined)
    		this.quotation = false;
        if(!this.isEdit||this.copyInv||this.isPOfromSO||this.isSOfromPO){
            var temp=this.isCustBill*1000+this.isCustomer*100+this.isOrder*10+this.cash*1+this.quotation*1;
            var temp2=0;
            var format="";
            switch(temp){
                case 0:format=Wtf.account.companyAccountPref.autogoodsreceipt;
                    temp2=Wtf.autoNum.GoodsReceipt;
                    break;
                case 1:format=Wtf.account.companyAccountPref.autocashpurchase;
                    temp2=Wtf.autoNum.CashPurchase;
                    break;
                case 10:format=Wtf.account.companyAccountPref.autopo;
                    temp2=Wtf.autoNum.PurchaseOrder;
                    break;
                case 100:format=Wtf.account.companyAccountPref.autoinvoice;
                    temp2=Wtf.autoNum.Invoice;
                    break;
                case 101:format=Wtf.account.companyAccountPref.autocashsales;
                    temp2=Wtf.autoNum.CashSale;
                    break;
                case 110:format=Wtf.account.companyAccountPref.autoso;
                    temp2=Wtf.autoNum.SalesOrder;
                    break;
                case 1000:format=Wtf.account.companyAccountPref.autobillinggoodsreceipt;
                    temp2=Wtf.autoNum.BillingGoodsReceipt;
                    break;
                case 1001:format=Wtf.account.companyAccountPref.autobillingcashpurchase;
                    temp2=Wtf.autoNum.BillingCashPurchase;
                    break;
                case 1010:format=Wtf.account.companyAccountPref.autobillingpo;
                    temp2=Wtf.autoNum.BillingPurchaseOrder;
                    break;
                case 1100:format=Wtf.account.companyAccountPref.autobillinginvoice;
                    temp2=Wtf.autoNum.BillingInvoice;
                    break;
                case 1101:format=Wtf.account.companyAccountPref.autobillingcashsales;
                    temp2=Wtf.autoNum.BillingCashSale;
                    break;
                case 1110:format=Wtf.account.companyAccountPref.autobillingso;
                    temp2=Wtf.autoNum.BillingSalesOrder;
                    break;
                case 111:format=Wtf.account.companyAccountPref.autoquotation;
                	temp2=Wtf.autoNum.Quotation;
                	break;
                case 11:format=Wtf.account.companyAccountPref.autovenquotation;
                	temp2=Wtf.autoNum.Venquotation;
                	break;         
            }

            if(isSelectNoFromCombo){
                this.fromnumber = temp2;
            } else if(format&&format.length>0 && (!this.isTemplate || this.createTransactionAlso)){
                WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit){this.Number.setValue(resp.data)}}, this);
            }
        }
    },
    dueDateCheck:function(){
        if(this.DueDate.getValue().getTime()<this.billDate.getValue().getTime()){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.msg11")], 2);    //"The Due Date should be greater than the Order Date."
           this.DueDate.setValue(this.billDate.getValue());
        }
    },

    initForClose:function(){
        this.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){this.isClosable=false;},this);
            }
        },this);
    },
     loadTemplateStore : function() {
        this.templateStore.load();
    },
    addInvoiceTemplate : function(isCreatedNow,tempid) {
        if(isCreatedNow===true){
            this.loadTemplateStore();
            this.templateStore.on("load",function(){
            	this.template.setValue(tempid);
            	this.templateID.setValue(tempid);
            },this)
            	
        }else{
        new Wtf.selectNewTempWin({
            isreport : false,
            tabObj : this,
            templatetype : this.doctype 
        });
        }
    },
    getJSONArray:function(store, includeLast, idxArr){
        var indices="";
        if(idxArr)
            indices=":"+idxArr.join(":")+":";        
        var arr=[];
        var fields=store.fields;
        var len=store.getCount();
        //if(includeLast)len++;
        
        for(var i=0;i<len;i++){
            if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                switch(fields.get(j).type){
                    case "auto":if(value!=undefined){value=(value+"").trim();}value=encodeURI(value);value="\""+value+"\"";break;
                    case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
    }
                recarr.push(fields.get(j).name+":"+value);
            }
            recarr.push("modified:"+rec.dirty);
            arr.push("{"+recarr.join(",")+"}");
        }
        return "["+arr.join(',')+"]";
    },
    setValues:function(billid){
            if(billid.indexOf(",")==-1){  //In MultiSelection if the user select only one                              
                    var rec=this.POStore.getAt(this.POStore.find('billid',billid));
                    if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer){
                        if(this.fromLinkCombo.getValue()==1){
                            this.includeProTax.setValue(false);
                            this.showGridTax(null,null,true);            
                        }
                    }
                    this.Memo.setValue(rec.data['memo']);
                    this.shipDate.setValue(rec.data['shipdate']);
                    this.validTillDate.setValue(rec.data['validdate']);
                    if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash && !this.isFixedAsset) { // set value only in VI module
                        this.invoiceList.setValue(rec.data['landedInvoiceNumber']);
                    }
                    if((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash)) {
                        this.Term.setValue(rec.data['termdays']);
                    }
                    this.postText=rec.data['posttext'];
                    this.shipvia.setValue(rec.data['shipvia']);
                    this.fob.setValue(rec.data['fob']);  
                    this.isCustomer ? this.users.setValue(rec.data['salesPerson']) : this.users.setValue(rec.data['agent']);

                    if(rec.data["discounttotal"] && this.Discount){
                        this.Discount.setValue(rec.data["discounttotal"]);
                        this.perDiscount.setValue(rec.data["discountispertotal"]);
                    }

                    if(rec.data['taxid']!=""){
                        this.Tax.enable();
                        this.isTaxable.setValue(true);
                        this.Tax.setValue(rec.data['taxid']);
                    }else{
                        this.Tax.disable();
                        this.isTaxable.reset();
                        this.Tax.reset();
                    }
                    this.getCreditTo(rec.data.creditoraccount);                                
                    var perstore=this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore
                    var index = perstore.find('accid',this.Name.getValue());
                    if(index != -1){
                        var storerec=perstore.getAt(index);                        
                        this.Term.setValue(storerec.data['termdays']);
                    }        
                    this.CostCenter.setValue(rec.data.costcenterid);
                }else{ //if the user select multiple values
                    this.clearComponentValues();
                }
    },
    clearComponentValues:function(){
        this.Memo.setValue('');
        this.shipDate.setValue('');
        this.validTillDate.setValue('');
        this.shipvia.setValue('');
        this.fob.setValue('');
        this.loadTransStore();
        this.Discount.setValue(0);
        this.perDiscount.setValue(false);
        this.Tax.disable();
        this.isTaxable.reset();
        this.Tax.reset();                    
        this.CostCenter.setValue('');
        if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash && !this.isFixedAsset) { // set value only in VI module
            this.invoiceList.setValue('');
        }
    },
    onCurrencyChangeOnly:function(){
        this.fromPO.reset();
        this.fromLinkCombo.reset();this.fromLinkCombo.setDisabled(true);
        this.PO.reset();this.PO.setDisabled(true);                                       
        if(this.partialInvoiceCmb){
            this.partialInvoiceCmb.reset();
            this.partialInvoiceCmb.disable();
            var id=this.Grid.getId();
            var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
            if(rowindex != -1){
                this.Grid.getColumnModel().setHidden( rowindex,true);
            }
        }                                
        this.Discount.setValue(0);
        this.perDiscount.setValue(false);
        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
            if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            }else{     
                this.Tax.disable();
                this.isTaxable.reset();
                this.Tax.reset();
            }
        this.includeProTax.setValue(false);
        this.showGridTax(null,null,true);
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow(); 
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0});                
    },

    setTermValues : function(termDetails)  {
        if(termDetails!=""&&termDetails!=null&&termDetails!=undefined){
        var detailArr = eval(termDetails);
        for(var cnt=0; cnt<detailArr.length; cnt++ ){
            var jObj = detailArr[cnt];
            
            var record = this.termStore.queryBy(function(record){
                return (record.get('id') == jObj.id);
            }, this).items[0];
            if(record) {
                record.set('termamount',jObj.termamount);
                record.set('termpercentage',jObj.termpercentage);
            }

        }
        }
    },

    addInvoiceTermGrid : function() {
        this.termcm=[{
            header: 'Term',
            dataIndex: 'term'
        },{
            header: 'Percentage',
            dataIndex: 'termpercentage',
            editor:new Wtf.form.NumberField({
                xtype : "numberfield", 
                maxLength : 15,
                allowNegative : false,
                minValue : 0,
                maxValue: 100,
                regexText:Wtf.MaxLengthText+"15"
            })
        },{
            header: 'Amount',
            dataIndex: 'termamount',
            renderer : function(val, meta, rec) {
                if(typeof val=='number' && val>=0 && rec.data.sign==0) {
                    rec.set('termamount',val*(-1));
                    return val*(-1)
                } else 
                    return val;
            },
            editor:new Wtf.form.NumberField({
                xtype : "numberfield", 
                maxLength : 15,
                allowNegative : true,
                regexText:Wtf.MaxLengthText+"15"
            })
        }];
        
        this.termRec =new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'term'},
        {name: 'glaccount'},
        {name: 'sign'
//        },{ name: 'category'
//        }, {name: 'includegst'
//        },{ name: 'includeprofit'
//        },{name: 'suppressamnt'
        },{name: 'formula'
        },{name: 'formulaids'
        },{name: 'termamount'
        },{name: 'termpercentage'
        }
        ]);
        this.termStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.termRec),
//            url:Wtf.req.account+'CompanyManager.jsp',
            url: 'ACCAccount/getInvoiceTermsSales.do',
            baseParams:{
                isSalesOrPurchase:this.isCustomer?true:false
        }
        });
//        if(!this.isEdit) {
            this.termStore.load();
            this.termStore.on('load',this.closeTermGrid,this);
//        }
//        this.calInvoiceTermBtn= new Wtf.Toolbar.Button({
//            text:"Apply Terms",
//            scope:this,
//            tooltip:{text:'Apply Terms'},
//            handler:function() {this.updateSubtotal()}
//        });
        this.termgrid = new Wtf.grid.EditorGridPanel({
            layout:'fit',
            clicksToEdit:1,
            store: this.termStore,
            height:100,
            autoScroll : true,
            disabledClass:"newtripcmbss",
            style:'padding-top:10px;',
            cm: new Wtf.grid.ColumnModel(this.termcm),
            border : false,
            loadMask : true,
//            tbar : this.calInvoiceTermBtn,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.termgrid.on('afteredit',this.updateSubtotalOnTermChange,this); 
        this.termgrid.on('cellclick',function(grid, rowIndex, columnIndex, e) {
            var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
            if(this.isViewTemplate){
                if(fieldName=='termamount' || fieldName=='termpercentage') {
                    return false;
                }
           }
        },this);        
    },
    
    closeTermGrid : function(obj){
        var store = this.termgrid.store;
//        if(this.termStore.data.length==0||this.cash)
            if(this.termStore.data.length==0)
            {
                this.termgrid.hide();
            }
            
    },  
    updateSubtotalOnTermChange : function(obj) {
        var recdata=obj.record.data;
        var store = obj.grid.store;
        var subtotal = this.calProdSubtotalWithoutDiscount();
        var formula = recdata.formulaids.split(",");
        var termtotal = 0;
        for(var cnt=0; cnt<formula.length; cnt++){
            if(formula[cnt]=='Basic') {
                termtotal +=(subtotal);
            }
            var record = store.queryBy(function(record){
                return (record.get('id') == formula[cnt]);
            }, this).items[0];
            if(record && (typeof record.data.termamount=='number')) {
//                termtotal +=(record.data.termamount*(record.data.sign==1 ? 1 : -1 ));
                  termtotal +=(record.data.termamount);
            }
        }
        if(obj.field=="termpercentage"){
//            obj.record.set('termamount',0);
            var opmod = recdata.sign==0 ? -1 : 1;
            var this_termTotal = ((Math.abs(termtotal) * recdata.termpercentage*1) / 100)*opmod;
            obj.record.set('termamount',this_termTotal);
        } 
        this.updateSubtotal();
//        else if(obj.field=="termamount") {
//            
//        } 
    },
    
    findTermsTotal : function() {
        var termTotal = 0;
        if(this.termgrid) {
            var store = this.termgrid.store;
            var totalCnt = store.getCount();
            for(var cnt=0; cnt<totalCnt; cnt++) {
                var lineAmt = store.getAt(cnt).data.termamount;
                if(typeof lineAmt=='number')
                    termTotal += lineAmt;
            }
        }
        return termTotal;
    },
    
    getInvoiceTermDetails : function() {
        var arr=[];
        if(this.termgrid) {
            var store = this.termgrid.store;
            store.each(function(rec){
                var lineAmt = rec.data.termamount;
                if(typeof lineAmt=='number' && lineAmt !=0) {
                    arr.push(store.indexOf(rec));
                }            
            }, this);
            return WtfGlobal.getJSONArray(this.termgrid,true,arr)
        }
    },
    
    calProdSubtotalWithoutDiscount:function(){
        var subtotal=0;
        var count=this.Grid.store.getCount();
        var store = this.Grid.store;
        for(var i=0;i<count;i++){
            var total=store.getAt(i).data.amountwithouttax;
//            if(this.editTransaction&&!this.fromPO){
//                    total=total/this.store.getAt(i).data['oldcurrencyrate'];
//                }
            subtotal+=total;
        }
        return subtotal;
    },
    checkForDeActivatedProductsAdded:function(){
    var invalidProducts='';
    if(!this.isEdit){ // Create New case
        invalidProducts = this.checkDeactivatedProductsInGrid();
    }
    return invalidProducts;
   },
    checkDeactivatedProductsInGrid :function(){
        var inValidProducts=''
        var rec = null;
        var productId = null;
        var productRec = null;
        for(var count=0;count<this.Grid.store.getCount();count++){
            rec = this.Grid.store.getAt(count);
            productId = rec.data.productid;
            if(productId!= undefined && productId != null && productId != ''){
                if(!this.fromPO.getValue() && !this.copyInv){
                    productRec = WtfGlobal.searchRecord(this.Grid.productComboStore, productId, "productid");
                } else {
                    productRec = rec;
                }
                if(productRec && (productRec.data.hasAccess === false)){
                    inValidProducts+=productRec.data.productname+', ';
                }
            }    
        }
        return inValidProducts; // List of deactivated products
    }
})




