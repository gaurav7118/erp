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
/* Mode Numbers:
PDM_EMAIL=1;
PDM_PRINT=2;
AUTONUM_JOURNALENTRY=0;
AUTONUM_SALESORDER=1;
AUTONUM_INVOICE=2;
AUTONUM_CREDITNOTE=3;
AUTONUM_RECEIPT=4;
AUTONUM_PURCHASEORDER=5;
AUTONUM_GOODSRECEIPT=6;
AUTONUM_DEBITNOTE=7;
AUTONUM_PAYMENT=8;
AUTONUM_CASHSALE=9;
AUTONUM_CASHPURCHASE=10;
AUTONUM_BILLINGINVOICE=11;
AUTONUM_BILLINGRECEIPT=12;
AUTONUM_BILLINGCASHSALE=13;
AUTONUM_BILLINGCASHPURCHASE=14;
AUTONUM_BILLINGGOODSRECEIPT=15;
AUTONUM_BILLINGPAYMENT=16;
AUTONUM_BILLINGSALESORDER=17;
AUTONUM_BILLINGPURCHASEORDER=18;
AUTONUM_BILLINGCREDITNOTE=19;
AUTONUM_BILLINGDEBITNOTE=20;
AUTONUM_BALANCESHEET=21;*/




function openCashTransTab(isCustomer,isCustBill){
   if(isCustomer)
        callSalesReceipt(false,null);
    else
        callPurchaseReceipt(false,null);
    }

function openInvTab(isTran,isOrder,isCustBill,isQuotation,isJobWorkOrderReciever,isSecurityGateEntry){
//    if(isCustBill){
//        if(isTran && isOrder)
//            callBillingSalesOrder(false,null);
//        else if(isTran)
//            callBillingInvoice(false,null);
//        else if(!isTran && isOrder)
//            callBillingPurchaseOrder(false,null);
//        else
//            callBillingGoodsReceipt(false,null);
//    }else{
        if (isTran && isOrder) {
        if (isJobWorkOrderReciever) {
            callSalesOrder(false, null, undefined, undefined, undefined, undefined, undefined, undefined, undefined, false, true);
        } else {
            callSalesOrder(false, null);
        }
    }
        else if(isTran)
        callInvoice(false,null);
        else if(!isTran && isOrder){
        if(isSecurityGateEntry){
            callPurchaseOrder(false,null,undefined,undefined,undefined,undefined,isSecurityGateEntry);
        }else{
            callPurchaseOrder(false,null);
        }
    }
        else
            callGoodsReceipt(false,null);
//    }
}

function openQuotationTab(){
    callQuotation();
}
/*< COMPONENT USED FOR >
 *      1.Invoice and Cash Sales Report
 *          callInvoiceList(id,check,isCash) --- < Invoice and Cash Sales Report >
 *          [isCash:isCash, isCustomer:true, isOrder:false]
 *      2.Invoice Report
 *          callBillingInvoiceList(id,check,isCash) --- < Invoice and Cash Sales Report >
 *          [isCash:isCash, isCustBill:true, isCustomer:true, isOrder:false]
 *      3.Credit/Cash Purchase Report
 *          callGoodsReceiptList(id,check)
 *          [isOrder:false, isCustomer:false]
 *      4.Purchase Order Report
 *          callPurchaseOrderList()
 *          [isOrder:true, isCustomer:false]
 *      5.Sales Order Report
 *          callSalesOrderList() -- < Sales Order Report >
 *          [isOrder:true, isCustomer:true]
 *
 *      this.appendID:This is useful for displaying help , TRUE => It is used when this.id is appended in the id of component.
 */
Wtf.account.TransactionListPanel=function(config){
    Wtf.apply(this, config);
    this.appendID = true;
    this.invID=null;
    this.exponly=null;
    this.recArr=[];
    this.isCash=true;
    this.readOnly=config.readOnly?config.readOnly:false;
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.costCenterId = "";
    this.extraFilters = config.extraFilters;
    if(config.extraFilters != undefined){//Cost Center Report View
        this.costCenterId = config.extraFilters.costcenter?config.extraFilters.costcenter:"";
    }
    //this.withInvMode : Used flag to check inventory mode of selected records.
    this.viewoption = { //view combo in report
        recurredinvoice: 9,
        normalinvoice:10,
        inventorypurchaseinvoice:16,
        expencepurchaseinvoice:17
    };
    this.index = "";
    this.archiveFlag = "";
    this.label = config.label;
    this.isOrder=config.isOrder;
    this.isCustBill=config.isCustBill;
    this.isSalesCommissionStmt=config.isSalesCommissionStmt;
    this.isCash=config.cash?true:false;
    /*
     * ERM-735 Map default payment method to customer
     */
    this.mapDefaultPmtMethod =CompanyPreferenceChecks.mapDefaultPaymentMethod();
    this.pendingapproval=config.pendingapproval?true:false;
    this.isexpenseinv=false;
    this.nondeleted=false;
    this.deleted=false;
    this.PR_MEMOS = "";
    this.customizeData="";
    this.gridConfigId = "";
    this.isfavourite=false;
    this.isOpeningBalanceInvoices=false;
    this.includeAllrec=true;
    this.onlyRecurredInvoices=false;//this flag is used to fetch only recurred invoices
    this.onlyNormalPendingInvoices=false;//this flag is used to fetch only normal invoices.
    this.isOpeningBalanceOrder=false;
    this.isOutstanding=false;
    this.isPendingInvoiced=false;
    this.isOuststandingproduct=false;
    this.isFixedAsset = (config.isFixedAsset)?config.isFixedAsset:false;
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset)?config.isLeaseFixedAsset:false;
    this.isConsignment = (config.isConsignment)?config.isConsignment:false;
    this.isMRPSalesOrder = (config.isMRPSalesOrder)?config.isMRPSalesOrder:false;
    this.isJobWorkOrderReciever = (config.isJobWorkOrderReciever)?config.isJobWorkOrderReciever:false;
    this.isfromReportList = (config.isfromReportList)?config.isfromReportList:false;//true if outstanding SO/PO Report is Clicked from Reportlist 
    this.isMRPJOBWORKOUT = (config.isMRPJOBWORKOUT)?config.isMRPJOBWORKOUT:false;
    this.isSecurityGateEntry = (config.isSecurityGateEntry)?config.isSecurityGateEntry:false;
    this.isMRPJOBWORKIN = (config.isMRPJOBWORKIN)?config.isMRPJOBWORKIN:false;
    this.uPermType= config.isLeaseFixedAsset?Wtf.UPerm.leaseorder:this.isFixedAsset?(config.isCustomer?Wtf.UPerm.assetsales:(config.isRequisition || config.isRFQ )?Wtf.UPerm.assetpurchasereq:Wtf.UPerm.assetpurchase):(config.isRequisition || config.isRFQ ? Wtf.UPerm.vendorpr :this.isConsignment?(config.isCustomer?Wtf.UPerm.consignmentsales:Wtf.UPerm.consignmentpurchase):(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice));
    this.permType= config.isLeaseFixedAsset?Wtf.Perm.leaseorder:this.isFixedAsset?(config.isCustomer?Wtf.Perm.assetsales:(config.isRequisition || config.isRFQ )?Wtf.Perm.assetpurchasereq:Wtf.Perm.assetpurchase):(config.isRequisition || config.isRFQ ? Wtf.Perm.vendorpr :this.isConsignment?(config.isCustomer?Wtf.Perm.consignmentsales:Wtf.Perm.consignmentpurchase):(config.isCustomer?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice));
    this.uPaymentPermType= config.isRequisition || config.isRFQ ? Wtf.UPerm.vendorpr : (config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.createPaymentPermType=(config.isCustomer?Wtf.Perm.invoice.createreceipt:Wtf.Perm.vendorinvoice.createpayment);
    this.exportPermType=config.isLeaseFixedAsset?(config.isQuotation?this.permType.exportlqt:this.isOrder?this.permType.exportlor:this.permType.exportlinv):this.isFixedAsset?(config.isCustomer?this.permType.exportdispinv:config.isQuotation?this.permType.exportavq:config.isRequisition?this.permType.exportapreq:config.isRFQ?this.permType.exportarfq:this.isOrder?this.permType.exportapo:this.permType.exportacqinv):this.isConsignment?(config.isCustomer?(this.isOrder?this.permType.exportsalesconreq:this.permType.exportsalesconinv):(this.isOrder?this.permType.exportpurchaseconreq:this.permType.exportpurchaseconinv)):(config.isRFQ ? this.permType.exportdatarfq : (config.isRequisition ? this.permType.exportdatapr : (config.isCustomer?(this.isOrder?this.permType.exportdataso:(config.isQuotation?this.permType.exportsalesquotation:this.permType.exportdatainvoice)):(this.isOrder?this.permType.exportdatapo:(config.isQuotation?this.permType.exportvendorquotation:this.permType.exportdatavendorinvoice)))));
    this.printPermType= config.isLeaseFixedAsset?(config.isQuotation?this.permType.printlqt:this.isOrder?this.permType.printlor:this.permType.printlinv):this.isFixedAsset?(config.isCustomer?this.permType.printdispinv:config.isQuotation?this.permType.printavq:config.isRequisition?this.permType.printapreq:config.isRFQ?this.permType.printarfq:this.isOrder?this.permType.printapo:this.permType.printacqinv):this.isConsignment?(config.isCustomer?(this.isOrder?this.permType.printsalesconreq:this.permType.printsalesconinv):(this.isOrder?this.permType.printpurchaseconreq:this.permType.printpurchaseconinv)):(config.isRFQ ? this.permType.printrfq : (config.isRequisition ? this.permType.printpr : (config.isCustomer?(this.isOrder?this.permType.printso:(config.isQuotation?this.permType.printsalesquotation:this.permType.printinvoice)):(this.isOrder?this.permType.printpo:(config.isQuotation?this.permType.printvendorquotation:this.permType.printvendorinvoice)))));
    this.removePermType=config.isLeaseFixedAsset?(config.isQuotation?this.permType.deletelqt:this.isOrder?this.permType.deletelor:this.permType.deletelinv):this.isFixedAsset?(config.isCustomer?this.permType.deletedispinv:config.isQuotation?this.permType.deleteavq:config.isRequisition?this.permType.deleteapreq:config.isRFQ?this.permType.removearfq:this.isOrder?this.permType.deleteapo:this.permType.deleteacqinv):this.isConsignment?(config.isCustomer?(this.isOrder?this.permType.deletesalesconreq:this.permType.deletesalesconinv):(this.isOrder?this.permType.deletepurchaseconreq:this.permType.deletepurchaseconinv)): (config.isRFQ ? this.permType.removerfq : (config.isRequisition ? this.permType.removepr : (config.isCustomer?(this.isOrder?this.permType.removeso:(config.isQuotation?this.permType.deletesalesquotation:this.permType.removeinvoice)):(this.isOrder?this.permType.removepo:(config.isQuotation?this.permType.deletevendorquotation:this.permType.removevendorinvoice)))));
    this.editPermType= config.isLeaseFixedAsset?(config.isQuotation?this.permType.editlqt:this.isOrder?this.permType.editlor:this.permType.editlinv):this.isFixedAsset?(config.isCustomer?this.permType.editdispinv:config.isQuotation?this.permType.editavq:config.isRequisition?this.permType.editapreq:config.isRFQ?this.permType.editarfq:this.isOrder?this.permType.editapo:this.permType.editacqinv):this.isConsignment?(config.isCustomer?(this.isOrder?this.permType.editsalesconreq:this.permType.editsalesconinv):(this.isOrder?this.permType.editpurchaseconreq:this.permType.editpurchaseconinv)):(config.isRFQ ? this.permType.editrfq : (config.isRequisition ? this.permType.editpr : (config.isCustomer?(this.isOrder?this.permType.editso:(config.isQuotation?this.permType.editsalesquotation:this.permType.editinvoice)):(this.isOrder?this.permType.editpo:(config.isQuotation?this.permType.editvendorquotation:this.permType.editvendorinvoice)))));
    this.copyPermType=(config.isCustomer?(this.isOrder?this.permType.copyso:(config.isQuotation?this.permType.copysalesquotation:this.permType.copyinvoice)):(this.isOrder?(config.isRequisition?config.isFixedAsset?this.permType.copyapreq:this.permType.copypr:this.permType.copypo):config.isRFQ?config.isFixedAsset?this.permType.copyarfq:this.permType.copyrfq:(config.isQuotation?this.permType.copyvendorquotation:this.permType.copyvendorinvoice)));
    this.emailPermType=(config.isLeaseFixedAsset||this.isFixedAsset)?true:config.isRFQ ? this.permType.exportdatarfq : (config.isCustomer?this.permType.emailinvoice:this.permType.emailvendorinvoice);
    this.recurringPermType=((config.isLeaseFixedAsset||this.isFixedAsset))?true:config.isCustomer?this.permType.recurringinvoice:this.permType.recurringvendorinvoice;
    this.isQuotation = config.isQuotation;
    this.moduleid= config.moduleId;
    this.isfromsearchwin=config.isfromsearchwin,
    this.linkedWithModuleId=config.linkedWithModuleId,
    this.closeflag=config.closeflag,
    this.blockedDocuments=false;
    this.unblockedDocuments=false;
    this.orderLinkedWithDocType=0;
    this.invoiceLinkedWithDOStatus=0;
    this.invoiceLinkedWithGRNStatus=0;
    this.isRequisitionOutstandingFilterApplied=false;
    this.customerQuotationsWithInvoiceAndDOStatus=0;
    this.myPO=false;
    this.isJobWorkoutInvoice = (config.isJobWorkoutInvoice!=undefined&&config.isJobWorkoutInvoice!=null)?config.isJobWorkoutInvoice:false;
    this.isMovementWarehouseMapping=Wtf.account.companyAccountPref.isMovementWarehouseMapping;
    this.reportbtnshwFlag=config.reportbtnshwFlag;
    if(this.isQuotation == undefined || this.isQuotation == null){
        this.isQuotation = false;
    }
     if(this.reportbtnshwFlag== undefined || this.reportbtnshwFlag == null)
       {
          this.reportbtnshwFlag=false;
       }
    this.isDraft = (config.isDraft != undefined && config.isDraft != null)? config.isDraft : false;
    this.docID = "";
      
    if (dojoInitCount <= 0) {
        dojo.cometd.init("../../bind");
        dojoInitCount++;
    }
   
    var channelName = "";
   
    this.filename = "";
    this.fileNameForPrint = "";
   // this.isFixedAsset?():this.isConsignment?():(config.isRFQ ? WtfGlobal.getLocaleText("acc.requestQuotationList.tabTitle") : (config.isRequisition ? WtfGlobal.getLocaleText("acc.field.PurchaseReqRepTabTitle") : (config.isCustomer?():(this.isOrder?WtfGlobal.getLocaleText("acc.dashboard.consolidatePurchaseOrderReport"):(config.isQuotation?WtfGlobal.getLocaleText("acc.field.vendQuoReport"):WtfGlobal.getLocaleText("acc.grList.tabTitle"))))));
    if ( this.isFixedAsset ) {
        if ( config.isCustomer ) {
            this.filename = WtfGlobal.getLocaleText("erp.navigate.AssetDisposalInvoiceList");
            this.fileNameForPrint = WtfGlobal.getLocaleText("erp.navigate.AssetDisposalInvoiceList");
        } else if ( config.isQuotation ) {
            this.filename = WtfGlobal.getLocaleText("acc.field.assetVendorQuotationList");
            this.fileNameForPrint = WtfGlobal.getLocaleText("acc.field.assetVendorQuotationList");
        } else if ( this.isOrder&& !this.isRequisition) {
            this.filename = WtfGlobal.getLocaleText("acc.field.assetPurchaseOrderList");
            this.fileNameForPrint = WtfGlobal.getLocaleText("acc.field.assetPurchaseOrderList");
        } else if ( this.isRequisition ) {
            this.filename = WtfGlobal.getLocaleText("acc.field.assetPurchaseRequisitionList")+"_v1";
            this.fileNameForPrint = WtfGlobal.getLocaleText("acc.field.assetPurchaseRequisitionList");
        } else if(this.isRFQ){//ERP-12220
            this.filename = WtfGlobal.getLocaleText("acc.requestQuotationList.tabTitle")+"_v1";
            this.fileNameForPrint = WtfGlobal.getLocaleText("acc.requestQuotationList.tabTitle");
        }else{
            this.filename = WtfGlobal.getLocaleText("erp.navigate.AssetAcquiredInvoiceList");
            this.fileNameForPrint = WtfGlobal.getLocaleText("erp.navigate.AssetAcquiredInvoiceList");
        }
    } else if ( this.isConsignment ) {
        if ( config.isCustomer ) {
            if ( this.isOrder ) {
                this.filename = WtfGlobal.getLocaleText("acc.Consignment.order.report");
                this.fileNameForPrint = WtfGlobal.getLocaleText("acc.Consignment.order.report");
            } else {
                this.filename = WtfGlobal.getLocaleText("acc.Consignment.customer.invoice.report");
                this.fileNameForPrint = WtfGlobal.getLocaleText("acc.Consignment.customer.invoice.report");
            }
        } else {
            if ( this.isOrder ) {
                this.filename = WtfGlobal.getLocaleText("acc.VenConsignment.order.report");
                this.fileNameForPrint = WtfGlobal.getLocaleText("acc.VenConsignment.order.report");
            } else {
                this.filename = WtfGlobal.getLocaleText("acc.Consignment.vendor.invoice.report");
                this.fileNameForPrint = WtfGlobal.getLocaleText("acc.Consignment.vendor.invoice.report");
            }
        }
    } else if ( this.isMRPSalesOrder ) {
        if ( config.isCustomer ) {
            if ( this.isOrder ) {
               this.filename = WtfGlobal.getLocaleText("acc.soList.tabTitle")+"_v1";
               this.fileNameForPrint = WtfGlobal.getLocaleText("acc.soList.tabTitle");
            } 
        } 
    } else if ( this.isMRPJOBWORKOUT ) {
        
        if ( this.isOrder ) {
            this.filename = WtfGlobal.getLocaleText("acc.soList.tabTitle")+"_v1";
            this.fileNameForPrint = WtfGlobal.getLocaleText("acc.soList.tabTitle");
        } 
    } else if ( this.isJobWorkOrderReciever ) {
        this.filename = WtfGlobal.getLocaleText("acc.jobWorkOrder.vendorjobworkorder")+"_v1";
        this.fileNameForPrint = WtfGlobal.getLocaleText("acc.jobWorkOrder.vendorjobworkorder");
    } else if ( config.isRFQ ) {
        this.filename = WtfGlobal.getLocaleText("acc.requestQuotationList.tabTitle")+"_v1";
        this.fileNameForPrint = WtfGlobal.getLocaleText("acc.requestQuotationList.tabTitle");
    } else if ( config.isRequisition ) {
        this.filename = WtfGlobal.getLocaleText("acc.field.PurchaseReqRepTabTitle")+"_v1";
        this.fileNameForPrint = WtfGlobal.getLocaleText("acc.field.PurchaseReqRepTabTitle");
    } else if ( config.isCustomer ) {
        if ( this.isOrder ) {
            if ( this.isLeaseFixedAsset ) {
                this.filename = WtfGlobal.getLocaleText("acc.lease.order.report")+"_v1";
                this.fileNameForPrint = WtfGlobal.getLocaleText("acc.lease.order.report");
            } else {
                this.filename = WtfGlobal.getLocaleText("acc.soList.tabTitle")+"_v1";
                this.fileNameForPrint = WtfGlobal.getLocaleText("acc.soList.tabTitle");
            }
        } else if ( config.isQuotation ) {
            if ( this.isLeaseFixedAsset ) {
                this.filename = WtfGlobal.getLocaleText("acc.lease.quotation.report")+"_v1";
                this.fileNameForPrint = WtfGlobal.getLocaleText("acc.lease.quotation.report");
            } else {
                this.filename = WtfGlobal.getLocaleText("acc.customer.Quotationreport")+"_v1";
                this.fileNameForPrint = WtfGlobal.getLocaleText("acc.customer.Quotationreport");
            }
        } else if ( this.isLeaseFixedAsset ) {
            this.filename = WtfGlobal.getLocaleText("acc.lease.customer.invoice.report")+"_v1";
            this.fileNameForPrint = WtfGlobal.getLocaleText("acc.lease.customer.invoice.report");
        } else {
            this.filename = this.isSalesCommissionStmt?WtfGlobal.getLocaleText("acc.field.SalesCommissionStatementReport")+"_v1":WtfGlobal.getLocaleText("acc.dashboard.CustomerInvoicesCashSalesReport")+"_v1";
            this.fileNameForPrint = this.isSalesCommissionStmt?WtfGlobal.getLocaleText("acc.field.SalesCommissionStatementReport"):WtfGlobal.getLocaleText("acc.dashboard.CustomerInvoicesCashSalesReport");
        }
    } else if ( this.isOrder ) {
        if(this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId){
            this.filename = WtfGlobal.getLocaleText("acc.dashboard.consolidateSecuritygateReport")+"_v1";
            this.fileNameForPrint = WtfGlobal.getLocaleText("acc.dashboard.consolidateSecuritygateReport");
        }else{
            this.filename = WtfGlobal.getLocaleText("acc.dashboard.consolidatePurchaseOrderReport")+"_v1";
            this.fileNameForPrint = WtfGlobal.getLocaleText("acc.dashboard.consolidatePurchaseOrderReport");
        }
    } else if ( config.isQuotation ) {
        this.filename = WtfGlobal.getLocaleText("acc.field.vendQuoReport")+"_v1";
        this.fileNameForPrint = WtfGlobal.getLocaleText("acc.field.vendQuoReport");
    } else {
        this.filename = WtfGlobal.getLocaleText("acc.grList.tabTitle")+"_v1";
        this.fileNameForPrint = WtfGlobal.getLocaleText("acc.grList.tabTitle");
    }
   
    if (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId) {
        channelName =Wtf.ChannelName.FixedAssetAIList;
    }else if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId){
        channelName = Wtf.ChannelName.VIAndCPReport;
    } else if(this.moduleid ==Wtf.Acc_Invoice_ModuleId){
        if(this.isLeaseFixedAsset)
            channelName =Wtf.ChannelName.LeaseInvoiceList;
        else
        channelName =Wtf.ChannelName.CIAndCSReport;
    } else if(this.moduleid ==Wtf.Acc_Sales_Order_ModuleId){
        channelName =Wtf.ChannelName.SalesOrderReport;
    } else if(this.moduleid ==Wtf.Acc_Purchase_Order_ModuleId){
        channelName =Wtf.ChannelName.PurchaseOrderReport;
    } else if(this.moduleid ==Wtf.Acc_Customer_Quotation_ModuleId){
        if(this.isLeaseFixedAsset)
            channelName =Wtf.ChannelName.LeaseQuotationReport;
        else
            channelName =Wtf.ChannelName.SalesQuotationReport;
    } else if(this.moduleid ==Wtf.Acc_Vendor_Quotation_ModuleId){
        channelName =Wtf.ChannelName.PurchaseQuotationReport;
    } else if (this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId) {
        channelName =Wtf.ChannelName.FixedAssetDIList;
    } else if (this.moduleid == Wtf.Acc_Lease_Order) {
        channelName =Wtf.ChannelName.LeaseOrderReport;
    } else if (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
        channelName = Wtf.ChannelName.FixedAssetPurchaseOrderList;
    }
   
    if (channelName != "" && !this.pendingapproval && !this.isDraft) {
        dojo.cometd.subscribe(channelName, this, "globalInvoiceListGridAutoRefreshPublishHandler");
    }
      
//    this.outstandingreportflag=config.outstandingreportflag;
    this.person=config.person;
    this.expandRec = Wtf.data.Record.create ([
        {name:'productname'},
        {name:'productdetail'},
        {name:'description'},
        {name:'prdiscount'},
        {name:'discountispercent'},
        {name:'amount'},
        {name:'productid'},
        {name:'accountid'},
        {name:'accountname'},
        {name:'partamount'},
        {name:'partialDiscount'},
        {name:'quantity'},
        {name:'showquantity'},
        {name:'unitname'},
        {name:'uomname'},
        {name:'rate'},
        {name:'rateIncludingGst'},
        {name:'israteIncludingGst'},
        {name:'rateinbase'},
        {name:'externalcurrencyrate'},
        {name:'prtaxpercent'},
        {name:'rowTaxAmount'},
        {name:'orderrate'},
        {name:'desc'},
        {name:'productmoved'},
        {name:'currencysymbol'},
        {name:'currencyrate'},
        {name: 'type'},
        {name: 'pid'},
        {name:'carryin'},
        {name:'approverremark'},
        {name:'permit'},
        {name:'invoicetype', defValue:""},
        {name:'linkto'},
        {name:'customfield'},
        {name:'isrejected'},
        {name:'rejectionreason'},
        {name:'approvalstatus'},
        {name:'approvedserials'},
        {name:'balanceQuantity'},
        {name:'shortfallQuantity'},
        {name:'baseuomquantity'},
        {name:'billid'},
        {name:'isIncludingGst'},
        {name:'isexpenseinv'},
        {name: 'productweightperstockuom'},
        {name: 'productweightincludingpakagingperstockuom'},
        {name:'productvolumeperstockuom'},
        {name:'displayUOM'},
        {name:'productvolumeincludingpakagingperstockuom'},
        {name:'balanceAmount'},
        {name:'baseuomname'}
//        {name:'includeprotax'} // REVERT CHANGES FOR ERP-25414
    ]);
    this.expandStoreUrl = "SalesOrderCMN/getBillingSalesOrderRows";
   
    if(this.isQuotation){
        this.expandStoreUrl = this.isCustomer? "ACCSalesOrderCMN/getQuotationRows.do" : "ACCPurchaseOrderCMN/getQuotationRows.do";
    }
    if(this.isRequisition){
        this.expandStoreUrl = "ACCPurchaseOrderCMN/getRequisitionRows.do";
    }
    if(this.isRFQ){
        this.expandStoreUrl = "ACCPurchaseOrderCMN/getRFQRows.do";
    }
   
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
//        url:Wtf.req.account+this.businessPerson+'Manager.jsp',
        baseParams:{
            mode:this.isOrder?(this.isCustBill?53:43):(this.isCustBill?17:14),
            dtype : 'report'//Display type report/transaction, used for quotation
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
   
    this.expandStore.on("beforeload", function(store){
         WtfGlobal.setAjaxTimeOut();
        if(this.businessPerson=="Customer"){
            //mode:this.isOrder?43:(this.isCustBill?17:14)
            if(this.isQuotation){
                this.expandStoreUrl = "ACCSalesOrderCMN/getQuotationRows.do";
            } else if(this.isRequisition){
                this.expandStoreUrl = "ACCPurchaseOrderCMN/getRequisitionRows.do";
            } else {
                this.expandStoreUrl = "ACC" + (this.isOrder?(this.withInvMode?"SalesOrderCMN/getBillingSalesOrderRows":"SalesOrderCMN/getSalesOrderRows"):(this.withInvMode?"InvoiceCMN/getBillingInvoiceRows":"InvoiceCMN/getInvoiceRows")) + ".do";
            }
           
        }else if(this.businessPerson=="Vendor"){
            if(this.isQuotation){
                this.expandStoreUrl = "ACCPurchaseOrderCMN/getQuotationRows.do";
            } else if(this.isRequisition){
                this.expandStoreUrl = "ACCPurchaseOrderCMN/getRequisitionRows.do";
            } else if(this.isRFQ){
                this.expandStoreUrl = "ACCPurchaseOrderCMN/getRFQRows.do";
            } else {
                this.expandStoreUrl = "ACC" + (this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId?"PurchaseOrderCMN/getSecurityGateEntryRows":this.isOrder?(this.withInvMode?"PurchaseOrderCMN/getBillingPurchaseOrderRows":"PurchaseOrderCMN/getPurchaseOrderRows"):(this.withInvMode?"GoodsReceiptCMN/getBillingGoodsReceiptRows":"GoodsReceiptCMN/getGoodsReceiptRows")) + ".do";
            }
           
        }
        store.proxy.conn.url = this.expandStoreUrl;
    }, this);
    
    this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'companyid'},
        {name:'companyname'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'rfqno'},
        {name:'isjobWorkWitoutGrn'},
        {name:'prno'},
        {name:'PR_IDS'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'aliasname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'includeprotax',type:'boolean'},
        {name:'amount'},
        {name:'amountbeforegst'},
        {name:'GSTINRegTypeDefaultMstrID'},
        {name:'CustVenTypeDefaultMstrID'},
        {name:'amountdue'},
        {name:'amountdueinbase'},
        {name:'exchangeratefortransaction'},
        {name:'termdays'},
        {name:'termid'},
        {name:'termname'},
        {name:'incash',type:'boolean'},
        {name:'taxamount'},
        {name:'OtherTermNonTaxableAmount'},//Other Charges(Other Term NonTaxable Amount), (For India Country)
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'hasautogenpickpackdo',type:'boolean'},
        {name:'termamount'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'createdby'},
        {name:'createdbyid'},
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
        {name:'NoOfpost'},
        {name:'NoOfRemainpost'}, 
        {name:'templateid'},
        {name:'templatename'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'statustype'},
        {name:'amountwithouttax'},
        {name:'amountwithouttaxinbase'},
        {name:'commission'},
        {name:'commissioninbase'},
        {name:'amountDueStatus'},
        {name:'paymentstatus'},
        {name:'salesPerson'},
        {name:'agent'},
        {name:'agentname'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'approvalstatus'},
        {name:'approvalstatusinfo'},
        {name:'approvalstatusint', type:'int', defaultValue:-1},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'isfavourite'},
        {name:'isCapitalGoodsAcquired'},
        {name:'isRetailPurchase'},
        {name:'importService'},
        {name:'othervendoremails'},
        {name:'termdetails'},
        {name:'approvestatuslevel'},// for requisition
        {name:'isrequesteditable'},// for Consignment Request Approval
        {name:'posttext'},
        {name:'isOpeningBalanceTransaction'},
        {name:'isNormalTransaction'},
        {name:'isreval'},
        {name:'islockQuantityflag'},
        {name:'isprinted'},
        {name:'isEmailSent'},
        {name:'validdate', type:'date'},
        {name:'cashtransaction',type:'boolean'},
        {name:'shiplengthval'},
        {name:'invoicetype'},
        {name:'landedInvoiceID'},
        {name:'landedInvoiceNumber'},
        {name:'manualLandedCostCategory'},
        {name:'termdays'},
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
              
        {name: 'dropshipbillingAddressType'},
        {name: 'dropshipbillingAddress'},
        {name: 'dropshipbillingCountry'},
        {name: 'dropshipbillingState'},
        {name: 'dropshipbillingPostal'},
        {name: 'dropshipbillingEmail'},
        {name: 'dropshipbillingFax'},
        {name: 'dropshipbillingMobile'},
        {name: 'dropshipbillingPhone'},
        {name: 'dropshipbillingContactPerson'},
        {name: 'dropshipbillingRecipientName'},
        {name: 'dropshipbillingContactPersonNumber'},
        {name: 'dropshipbillingContactPersonDesignation'},
        {name: 'dropshipbillingWebsite'},
        {name: 'dropshipbillingCounty'},
        {name: 'dropshipbillingCity'},
       /**
         * If Show Vendor Address in purchase side document and India country 
         * then this Fields used to store Vendor Billing Address
         */
        {name: 'vendorbillingAddressTypeForINDIA'},
        {name: 'vendorbillingAddressForINDIA'},
        {name: 'vendorbillingCountryForINDIA'},
        {name: 'vendorbillingStateForINDIA'},
        {name: 'vendorbillingPostalForINDIA'},
        {name: 'vendorbillingEmailForINDIA'},
        {name: 'vendorbillingFaxForINDIA'},
        {name: 'vendorbillingMobileForINDIA'},
        {name: 'vendorbillingPhoneForINDIA'},
        {name: 'vendorbillingContactPersonForINDIA'},
        {name: 'vendorbillingRecipientNameForINDIA'},
        {name: 'vendorbillingContactPersonNumberForINDIA'},
        {name: 'vendorbillingContactPersonDesignationForINDIA'},
        {name: 'vendorbillingWebsiteForINDIA'},
        {name: 'vendorbillingCountyForINDIA'},
        {name: 'vendorbillingCityForINDIA'},
            
        {name:'sequenceformatid'},
        {name:'isTaxCommittedOnAvalara'},
        {name:'gstIncluded'},
        {name:'lasteditedby'},
        {name:'movementtype'},
        {name:'movementtypename'},
        {name:'salespersonname'},
        {name:'isConsignment'},
        {name:'custWarehouse'},
        {name:'custWarehousename'},
        {name:'requestWarehouse'},
        {name:'requestWarehousename'},
        {name:'requestLocationname'},
        {name:'requestLocation'},
        {name:'autoapproveflag'},
        {name:'movementtype'},
        {name:'deliveryTime'},
        {name:'getFullShippingAddress'},
        {name:'selfBilledInvoice'},
        {name:'RMCDApprovalNo'},
        {name:'fixedAssetInvoice'},
        {name:'fixedAssetLeaseInvoice'},
        //Below Fields are used only for Cash Sales and Purchase.
        {name:'methodid'},
        {name:'paymentname'},
        {name:'moduleid'},
        {name:'detailtype'},
        {name:'cardno'},
        {name:'nameoncard'},
        {name:'cardexpirydate', type:'date'},
        {name:'cardtype'},
        {name:'cardrefno'},
        {name:'chequeno'},
        {name:'clearanceDate', type:'date'},
        {name:'paymentStatus'},
        {name:'bankname'},
        {name:'refname'},
        {name:'shippingterm'},
        {name:'chequedate', type:'date'},
        {name:'chequedescription'},
        {name:'termsincludegst'},
        {name:'attachment'},
        {name:'fromdate',type:'date'},
        {name:'todate',type:'date'},
        {name:'customerporefno'},
        {name:'totalprofitmargin'},
        {name:'totalprofitmarginpercent'},
        {name: 'isDraft'},
        {name: 'isLinkedTransaction'},
        {name: 'isMRPJOBWORKOUT'},
        {name: 'isJobWorkOrderReciever'},
        {name: 'closeStatus'},
        {name: 'parentinvoiceid'},
        {name:'isFromPOS'},
        {name:'isactivate',type:'boolean'},
        {name:'approver'},
        {name:'ispendingapproval',type:'boolean'},
        {name: 'parentso'},
        {name: 'isWrittenOff'},
        {name: 'isRecovered'},
        {name:'allowEditingRecurredDocuments',type:'boolean'},
        {name:'editedRecurredDocumentsApprover'},
        {name:'currencycode'},
        {name: 'hasAccess', type: 'boolean'},
        {name: 'isAllowToEdit', type: 'boolean'},
        {name: 'isPaymentStatusCleared', type: 'boolean'},
        {name: 'statusforcrosslinkage'},
        {name: 'statusofpoforrequisition'},
        {name:'closedmanually'},
        {name: 'assetExciseid'},
        {name: 'exciseDetailid'},
        {name: 'suppliers'},
        {name: 'supplierTINSalesTAXNo'},
        {name: 'supplierExciseRegnNo'},
        {name: 'cstnumber'},
        {name: 'supplierRange'},
        {name: 'supplierCommissionerate'},
        {name: 'supplierAddress'},
        {name: 'supplierImporterExporterCode'},
        {name: 'supplierDivision'},
        {name: 'manufacturername'},
        {name: 'manufacturerExciseRegnNo'},
        {name: 'manufacturerRange'},
        {name: 'manufacturerCommissionerate'},
        {name: 'manufacturerDivision'},
        {name: 'manufacturerAddress'},
        {name: 'manufacturerImporterExporterCode'},
        {name: 'InvoicenoManuFacture'},
        {name: 'InvoiceDateManuFacture'},
        {name: 'supplierState'},
        {name: 'isExciseInvoice'},
        {name: 'isExciseInvoiceWithTemplate'},
        {name: 'defaultnatureofpurchase'},
        {name: 'registrationType'},
        {name: 'populateproducttemplate'},
        {name: 'UnitName'},
        {name: 'ECCNo'},
        {name: 'manufacturertype'},
        {name: 'formtypeid'},
        {name: 'gtaapplicable'},
        {name: 'isMerchantExporter'},
        {name: 'additionalMemo'}, // Additional memo column for INDONESIA country in Sales Invoice
        {name: 'additionalMemoName'}, // Additional memo column for INDONESIA country in Sales Invoice
        {name: 'gstapplicable'},
        {name: 'isInterstateParty'},
        {name: 'excisetypeid'},
        {name: 'formseriesno'},
        {name: 'formno'},
        {name: 'formdate',type:'date'},
        {name: 'formamount'},
        {name: 'checkformstatus'},
        {name: 'formstatus'},
        {name: 'issupplementary'},
        {name: 'originalInvoice'},
        {name: 'originalInvoiceId'},
        {name: 'driverID'},
        {name: 'vehicleNoID'},
        {name:'discountinbase'},
        {name:'tdsrate'},
        {name:'tdsmasterrateruleid'},
        {name:'natureOfPayment'},
        {name:'deducteetype'},
        {name:'residentialstatus'},
        {name:'tdsPayableAccount'},
        {name:'natureOfPaymentname'},
        {name:'deducteetypename'},
        {name:'tdsamount'},
        {name:'totalAmountWithTDS'},
        {name:'TotalAdvanceTDSAdjustmentAmt'},
        {name:'AdvancePaymentID'},
        {name:'AdvancePaymentNumber'},
        {name:'isTaxPaidTransaction'},
        {name:'isSupplierLinekd'},
        {name:'isClaimedTransaction'},
        {name:'supplierinvoiceno'},//SDP-4510
        {name: 'importexportdeclarationno'},//ERM-470
        {name:'landingCostCategoryCombo'},//SDP-4510
        {name:'customerbankaccounttype'},
        {name:'customerbankaccounttypevalue'},
        {name:'isGIROFileGeneratedForUOBBank'},
        {name:'isGIROFileGeneratedForUOBBankForReport'},
        {name:'paymentMethodUsedForUOB'},
        {name:'vattinno'},
        {name:'csttinno'},
        {name:'panno'},
        {name:'servicetaxno'},
        {name:'isjobworkoutrec'},
        {name:'tanno'},
        {name:'eccno'},
        {name:'subtotal'},
        {name:'productTotalAmount'},
        {name:'amountBeforeTax'},
        {name:'upsTrackingNumbers'},//Tracking Numbers of shipments created with Pick-Pack-Ship process and UPS REST service
        {name:'totalShippingCost'},//total shipping cost estimated by UPS service
        {name: 'customerShippingAddressType'},
        {name: 'customerShippingAddress'},
        {name: 'customerShippingCountry'},
        {name: 'customerShippingState'},
        {name: 'customerShippingCounty'},
        {name: 'customerShippingCity'},
        {name: 'customerShippingEmail'},
        {name: 'customerShippingFax'},
        {name: 'customerShippingMobile'},
        {name: 'customerShippingPhone'},
        {name: 'customerShippingPostal'},
        {name: 'customerShippingContactPersonNumber'},
        {name: 'customerShippingContactPersonDesignation'},
        {name: 'customerShippingWebsite'},
        {name: 'customerShippingRecipientName'},
        {name: 'customerShippingContactPerson'},
        {name: 'customerShippingRoute'},        
        {name: 'customeridforshippingaddress'},
        {name: 'purchaseinvoicetype'},
        {name: 'isapplytaxtoterms'},
        {name: 'childCount'},
        {name: 'cashReceived'},//for Cash Received field in Sales Invoice
        {name: 'isapplytaxtoterms'},
        {name: 'isTDSApplicable'},
        {name: 'personcode'},
        {name: 'purchaseordertype'},
        {name: 'isSOPOBlock'},
        {name: 'isRoundingAdjustmentApplied'},
        {name:'isFinalLevelApproval'},
        {name:'CustomerVendorTypeId'},
        {name:'GSTINRegistrationTypeId'},
        {name:'gstin'},
        {name:'gstdochistoryid'},
        {name:'uniqueCase'},
        {name: 'billingAddContactPerson'},
        {name: 'shippingAddContactPerson'},
        {name: 'billingAddContactNo'},
        {name: 'shippingAddContactNo'},
        {name: 'BillingAddEmail'},
        {name: 'shippingAddEmail'},
        {name: 'amountDueOriginal'},
        {name: 'applicabledays', type: 'int', convert: function (value) {
                if (value === undefined || value === null || value === "") {
                    value = -1;
                } else if (typeof value === "string") {
                    value = parseInt(value);
                }
                return isNaN(value) ? -1 : value;
            }
        },
        {name: 'discounttype'},
        {name: 'invoicecreationdate'},
        {name: 'grcreationdate'},
        {name: 'discountvalue'},
        {name: 'linkedpayment'},
        {name: 'linkedpaymentID'},
        {name: 'crossLinkingTransaction'},
        {name: 'isCreditable'}
        
    ]);
    this.StoreUrl = "";
    this.RemoteSort= false;
    this.isForJobWorkOut=false;
    if(this.businessPerson=="Customer"){
        //mode:this.isOrder?42:(this.isCustBill?16:12)
        this.StoreUrl = "ACC" + (this.isOrder?("SalesOrderCMN/getSalesOrdersMerged"):("InvoiceCMN/getInvoicesMerged")) + ".do";
       this.RemoteSort = true;
    }else if(this.businessPerson=="Vendor"){
        this.StoreUrl = "ACC" + (this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId?"PurchaseOrderCMN/getSecurityGateEntryMerged":
        this.isOrder?("PurchaseOrderCMN/getPurchaseOrdersMerged"):("GoodsReceiptCMN/getGoodsReceiptsMerged")) + ".do";
        this.RemoteSort = true;
        if(this.moduleid==Wtf.Job_Work_Out_ORDER_REC){
            this.isForJobWorkOut=true;
        }        
    }
    if(this.isQuotation){
        this.StoreUrl = this.isCustomer? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
        this.RemoteSort = true;
    }
    if(this.isRequisition){
        this.StoreUrl = "ACCPurchaseOrderCMN/getRequisitions.do";
        this.RemoteSort = true;    // ERP-20787
    }
    if(this.isRFQ){
        this.StoreUrl = "ACCPurchaseOrderCMN/getRFQs.do";
        this.RemoteSort = true;   // ERP-20787
    }
    if(config.consolidateFlag && !this.isSalesCommissionStmt){
        this.Store = new Wtf.data.GroupingStore({   
            url:this.StoreUrl,
           remoteSort: this.RemoteSort,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                //mode:this.isOrder?(this.isCustBill?52:42):(this.isCustBill?16:12),
                costCenterId: this.costCenterId,
                deleted:false,
                nondeleted:false,
                cashonly:false,
                creditonly:false,
                CashAndInvoice:true,
                salesPersonFilterFlag:true,
                isRFQ:this.isRFQ,
                isFixedAsset:this.isFixedAsset,
                isJobWorkOrderReciever:this.isJobWorkOrderReciever,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isConsignment:this.isConsignment,
                isMRPSalesOrder:this.isMRPSalesOrder,
                isMRPJOBWORKOUT:this.isMRPJOBWORKOUT,
                isMRPJOBWORKIN:this.isMRPJOBWORKIN,
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                isprinted:false,
                isEmailSent:false,
                productids:""                
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
    } else if(this.isSalesCommissionStmt){
        this.Store = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                remoteSort: this.RemoteSort,
                totalProperty:'count'
            },this.GridRec),
            baseParams:{
                //mode:this.isOrder?(this.isCustBill?52:42):(this.isCustBill?16:12),
                costCenterId: this.costCenterId,
                deleted:false,
                nondeleted:false,
                cashonly:false,
                isRFQ:this.isRFQ,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isJobWorkOrderReciever:this.isJobWorkOrderReciever,
                isConsignment:this.isConsignment,
                creditonly:false,
                CashAndInvoice:true,
                salesPersonFilterFlag:true,
                isfavourite:false,
                isprinted:false,
                isEmailSent:false,
                pendingapproval:this.pendingapproval,
                isDraft: this.isDraft                
            },
            url:this.StoreUrl,
            sortInfo : {
                field : 'date',
                direction : 'DESC'
            },
            groupField : 'amountDueStatus'
        });
    } else {
        this.Store = new Wtf.data.Store({
            url:this.StoreUrl,
            remoteSort: this.RemoteSort,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                //mode:this.isOrder?(this.isCustBill?52:42):(this.isCustBill?16:12),
                costCenterId: this.costCenterId,
                deleted:false,
                nondeleted:false,
                cashonly:false,
                creditonly:false,
                CashAndInvoice:true,
                salesPersonFilterFlag:true,
                consolidateFlag:config.consolidateFlag,
                isRFQ:this.isRFQ,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isConsignment:this.isConsignment,
                isJobWorkOrderReciever:this.isJobWorkOrderReciever,
                isMRPSalesOrder:this.isMRPSalesOrder,
                isMRPJOBWORKOUT:this.isMRPJOBWORKOUT,
                isMRPJOBWORKIN:this.isMRPJOBWORKIN,
                companyids:companyids,
                pendingapproval:this.pendingapproval,
                isDraft: this.isDraft,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                isprinted:false,
                isEmailSent:false,
                report:true,
                isForJobWorkOut:this.isForJobWorkOut
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                upsErrorJSON:'upsErrorJSON',
                totalProperty:'count'
            },this.GridRec)
        });
        if (CompanyPreferenceChecks.discountOnPaymentTerms() && ((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) || (this.moduleid == Wtf.Acc_Invoice_ModuleId))) {
            this.discountAndTermMasterRec = new Wtf.data.Record.create([
                {
                    name: 'discountid'
                }, {
                    name: 'termid'
                }, {
                    name: 'applicabledays'
                }, {
                    name: 'discountname'
                }, {
                    name: 'discountvalue'
                }, {
                    name: 'discountaccount'
                }, {
                    name: 'discounttype'
                }
            ]);
            this.discountMasterStore = new Wtf.data.Store({
                url: "AccDiscountController/getDiscountsAndTerms.do",
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty: "count"
                }, this.discountAndTermMasterRec)
            });
            this.discountMasterStore.load();
        }
    }
    var level_1_perm = false;
    var level_2_perm = false;
    if(this.pendingapproval) {
       
        this.Store.on("beforeload", function(){
            if(this.isOrder) {
                if(this.isCustomer) {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.salesorderapprovelevelone)) {
                        level_1_perm = true;
                    }
                } else {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.purchaseorderapprovelevelone)) {
                        level_1_perm = true;
                    }
                }
           
            } else {
                if(this.isCustomer) {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.customerinvoiceapprovelevelone)) {
                        level_1_perm = true;
                    }
                } else {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.vendorinvoiceapprovelevelone)) {
                        level_1_perm = true;
                    }
                }
            }
           
           
            if(this.isOrder) {
                if(this.isCustomer) {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.salesorderapproveleveltwo)) {
                        level_2_perm = true;
                    }
                } else {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.purchaseorderapproveleveltwo)) {
                        level_2_perm = true;
                    }
                }
            } else {
                if(this.isCustomer) {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.customerinvoiceapproveleveltwo)) {
                        level_2_perm = true;
                    }
                }else {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.vendorinvoiceapproveleveltwo)) {
                        level_2_perm = true;
                    }
                }
            }
           
        }, this);
    }
    var sdateSavedSearch;
    var edateSavedSearch;
    if(config.searchJson != undefined && config.searchJson != ""){
        sdateSavedSearch = JSON.parse(config.searchJson).data[0].sdate;
        edateSavedSearch = JSON.parse(config.searchJson).data[0].edate;
    }
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true, sdateSavedSearch)
    });
    if (!this.isfromsearchwin) {
        this.startDate.on("blur", function () {
            this.setQuarterMonthAndyear();
        });
    }
        
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false, edateSavedSearch)
    });
    if (!this.isfromsearchwin) {
        this.endDate.on("blur", function () {
            this.setQuarterMonthAndyear();
        });
    }
    this.monthQuarterStoreData = [['0', 'All'],['1', 'Previous Month'],['2', '1st Quarter'], ['3', '2nd Quarter'], ['4', '3rd Quarter'], ['5', '4th Quarter']]
    this.monthQuarterStoreData1 = [['0', 'All']]
    this.monthQuarterStore = new Wtf.data.SimpleStore({
        fields: [{
                name: 'id'
            }, {
                name: 'name'
            }],
        data: this.monthQuarterStoreData
    });
    var data = WtfGlobal.getBookBeginningYear(true);

            this.yearStore = new Wtf.data.SimpleStore({
                fields: [{name: 'id', type: 'int'}, 'yearid'],
                data: data
            });
    this.monthQuarter = new Wtf.form.ComboBox({
        triggerAction: 'all',
        mode: 'local',
        selectOnFocus: true,
        valueField: 'id',
        displayField: 'name',
        store: this.monthQuarterStore,
        width: 100,
        value: '0',
        typeAhead: true,
        forceSelection: true,
        emptyText: WtfGlobal.getLocaleText("acc.field.SelectMonthQuarter"),
        name: 'monthquarter',
        hiddenName: 'monthquarter',
         listeners: {
            scope: this,
            select:function() {
                this.setQuarterMonthAndyear();
            }}
    });
     this.QuarterYear = new Wtf.form.ComboBox({
        store: this.yearStore,
//                fieldLabel:  WtfGlobal.getLocaleText('acc.field.fiscalyearEnd'), //'Year',
        name: 'quarteryear',
        displayField: 'yearid',
        width: 70,
        valueField: 'yearid',
        forceSelection: true,
        disabled:true,
        mode: 'local',
        triggerAction: 'all',
        value: new Date().getFullYear(), // to show current year as a default value
        selectOnFocus: true,
        listeners: {
            scope: this,
            select:function() {
                this.setQuarterMonthAndyear();
            }}
    });
    
    if(this.extraFilters != undefined){//Cost Center Report View
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = this.extraFilters.startdate;
        currentBaseParams.enddate = this.extraFilters.enddate;
        currentBaseParams.ccrAllRecords = this.extraFilters.ccrAllRecords;      
        this.Store.baseParams=currentBaseParams;
    }
    var cashType=config.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoCS"):WtfGlobal.getLocaleText("acc.accPref.autoCP");    //"Cash Sales":"Cash Purchase";
    var creditType=config.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoInvoice"):WtfGlobal.getLocaleText("acc.accPref.autoVI");   //"Invoice":"Vendor Invoice";
    var dataArr = new Array();
    if (this.isRequisition || this.isRFQ) {

        dataArr.push([0, WtfGlobal.getLocaleText("acc.rem.105")], [5, WtfGlobal.getLocaleText("acc.field.FavouriteRecords")]);
        
        /*----------- Will show the filter when "Show Requisition status for PO" chek is ON from system preferences-------- */
        if (this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId && Wtf.account.companyAccountPref.columnPref.statusOfRequisitionForPO) {
            dataArr.push([6, WtfGlobal.getLocaleText("acc.field.OutstandingPR")]);
        }
    }else if(this.isSalesCommissionStmt){
        dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],[1,cashType],[2,creditType]);       
    } else if (this.isOrder && this.businessPerson == "Customer") {
        if (this.isJobWorkOrderReciever) {
            dataArr.push([0, WtfGlobal.getLocaleText("acc.rem.105")], [5, WtfGlobal.getLocaleText("acc.field.FavouriteRecords")], [7, WtfGlobal.getLocaleText("acc.field.OutstandingJWOs")], [8, WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")], [11, WtfGlobal.getLocaleText("acc.field.OutstandingProducts")], [12, WtfGlobal.getLocaleText("acc.field.JWOWithoutDOInv")], [14, WtfGlobal.getLocaleText("acc.field.JWOWithDOWithoutInv")]);
        } else {
            dataArr.push([0, WtfGlobal.getLocaleText("acc.rem.105")], [5, WtfGlobal.getLocaleText("acc.field.FavouriteRecords")], [7, WtfGlobal.getLocaleText("acc.field.OutstandingSOs")], [8, WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")], [9, WtfGlobal.getLocaleText("acc.invoiceList.blockdocuments")], [10, WtfGlobal.getLocaleText("acc.invoiceList.unblockdocuments")], [11, WtfGlobal.getLocaleText("acc.field.OutstandingProducts")], [12, WtfGlobal.getLocaleText("acc.field.SOWithoutDOInv")], [13, WtfGlobal.getLocaleText("acc.field.SOWithoutDOWithInv")], [14, WtfGlobal.getLocaleText("acc.field.SOWithDOWithoutInv")], 
            [15, WtfGlobal.getLocaleText("acc.field.SOWithDOWithInv")],[Wtf.INDEX_MOBILE_TRANSACTIONS ,WtfGlobal.getLocaleText("acc.field.mobiletransactions")],[Wtf.INDEX_SOs_FOR_INVOICING, WtfGlobal.getLocaleText("acc.field.OutstandingSOsforInvoicing")]);
        }
    }else if(this.isOrder && this.businessPerson=="Vendor"){
        dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],[5,WtfGlobal.getLocaleText("acc.field.FavouriteRecords")], [7, WtfGlobal.getLocaleText("acc.field.OutstandingPOs")],[8,WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")],[9,WtfGlobal.getLocaleText("acc.invoiceList.blockdocuments")], [10,WtfGlobal.getLocaleText("acc.invoiceList.unblockdocuments")],[11,WtfGlobal.getLocaleText("acc.invoiceList.mypo")],[12,WtfGlobal.getLocaleText("acc.field.POWithoutGRInv")],[13,WtfGlobal.getLocaleText("acc.field.POWithoutGRWithInv")],[14,WtfGlobal.getLocaleText("acc.field.POWithGRWithoutInv")],[15,WtfGlobal.getLocaleText("acc.field.POWithGRWithInv")]);
    }else if(this.moduleid ==Wtf.Acc_Invoice_ModuleId && this.pendingapproval){
        dataArr.push([this.viewoption.normalinvoice,WtfGlobal.getLocaleText("acc.field.normalinvoices")],[this.viewoption.recurredinvoice,WtfGlobal.getLocaleText("acc.salesinvoicereport.recurreddocuments.view.title")]);       
    }else{
        dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],[3,WtfGlobal.getLocaleText("acc.rem.106")],[4,WtfGlobal.getLocaleText("acc.rem.107")],[5,WtfGlobal.getLocaleText("acc.field.FavouriteRecords")]);
   //     if(!this.isCustomer) {
            dataArr.push([6, WtfGlobal.getLocaleText("acc.field.PendingPayments")]);
 //   }
        if(!this.isFixedAsset){
            dataArr.push([1,cashType],[2,creditType],[8,WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")]);
        }
        if(this.moduleid ==Wtf.Acc_Invoice_ModuleId && !this.isFixedAsset){
            //Recurred Documetn(s) - to fiter recurred documents in sales invoice report on selection of view combo 
            dataArr.push([this.viewoption.recurredinvoice,WtfGlobal.getLocaleText("acc.salesinvoicereport.recurreddocuments.view.title")]);
            /*---Filter in Sales Invoice Module to fetch records linked with DO as per filter-----*/
            dataArr.push([10,WtfGlobal.getLocaleText("acc.field.invoicewithfulldo")]);
            dataArr.push([11,WtfGlobal.getLocaleText("acc.field.invoicewithnodo")]);
            dataArr.push([12,WtfGlobal.getLocaleText("acc.field.invoicewithpartialdo")]);
            dataArr.push([Wtf.INDEX_MOBILE_TRANSACTIONS ,WtfGlobal.getLocaleText("acc.field.mobiletransactions")]);
        }
        }
    if(this.moduleid ==Wtf.Acc_Vendor_Invoice_ModuleId && !this.isFixedAsset){
        //Inventory Purchase Invoice(s)/Expense Purchase Invoice(s) - to fiter purchase and expence invoice report on selection of view combo 
        dataArr.push([this.viewoption.inventorypurchaseinvoice,WtfGlobal.getLocaleText("acc.purchaseinvoicereport.iventorypurchase.view.title")]);
        dataArr.push([this.viewoption.expencepurchaseinvoice,WtfGlobal.getLocaleText("acc.purchaseinvoicereport.inventoryexpence.view.title")]);
          /*---Filter in Purchase Invoice Module to fetch records linked with GR as per filter-----*/
        dataArr.push([11, WtfGlobal.getLocaleText("acc.field.invoicewithfullgrn")]);
        dataArr.push([12, WtfGlobal.getLocaleText("acc.field.invoicewithnogrn")]);
        dataArr.push([13, WtfGlobal.getLocaleText("acc.field.invoicewithpartialgrn")]);
    }  
    /**
     * Put Filter of Job Order for SO and CI
     */
    if ((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId) && Wtf.account.companyAccountPref.jobOrderItemFlow) {
        dataArr.push([18, WtfGlobal.getLocaleText("acc.field.joborderitem")]);
    }
    /**
     * Put filter for Reversed ITC types
     */
    if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isitcapplicable &&
            (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId)){
        dataArr.push([19, WtfGlobal.getLocaleText("acc.India.ITC.Reversed.dropdown")]);
    }
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :dataArr,
        sortInfo : {
            field : 'name',
            direction : 'ASC'
        }
    });
    if(this.moduleid ==Wtf.Acc_Invoice_ModuleId && this.pendingapproval){
        this.typeEditorComboId=this.isDraft? 'viewDraftpendingapproval'+config.helpmodeid : 'viewpendingapproval'+config.helpmodeid;
    }else{
        this.typeEditorComboId=this.isDraft? 'viewDraft'+config.helpmodeid : 'view'+config.helpmodeid
    }
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'typeid',
        displayField:'name',
        id: this.typeEditorComboId, //+config.id,
        valueField:'typeid',
        mode: 'local',
        defaultValue:0,
        width:70,
        listWidth:200,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
   
    this.summary = new Wtf.grid.GroupSummary();
   
    this.usersRec = new Wtf.data.Record.create([
        {name: 'id', mapping:'userid'},
        {name: 'name', mapping:'username'},
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
   
    if(this.isSalesCommissionStmt){
        this.userds = Wtf.salesPersonStore;
    }else{
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
    }
    if(this.isSalesCommissionStmt){
        this.userds.load();   
    }   

    this.users= new Wtf.form.ComboBox({           
            triggerAction:'all',
            mode: 'local',
            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            store:this.userds,                                                
            width:200,
            typeAhead: true,
            forceSelection: true,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectSalesPerson"),
            allowBlank:false,       
            name:'username',
            hiddenName:'username'           
        });
           
    this.userds.on("load",function(){
        if(this.userds.data.length>0){
            this.users.setValue(this.userds.data.items[0].data.id);
            this.Store.load({params:{start:0,limit:30}});
        }else{
                Wtf.MessageBox.hide();
        }
    },this);       

    this.costCenter=CommonERPComponent.createCostCenterPagingComboBox(100,250,30,this);
    this.costCenter.on("select", function(cmb, rec, ind){
        this.costCenterId = rec.data.id;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.costCenterId = this.costCenterId;
        this.Store.baseParams=currentBaseParams;
    },this);
    
    this.costCenter.store.on("load", function() {
        if(this.costCenter.getRawValue() == "" || this.costCenter.getRawValue() == undefined ||this.costCenter.getRawValue() == null){
            var record = new Wtf.data.Record({
                id: "",
                name: "All Records"
            });
            this.costCenter.store.insert(0, record);
        }
    }, this);

    this.costCenter.store.on("beforeload", function() {
        var currentBaseParams = this.costCenter.store.baseParams;
        currentBaseParams.isForReport =true;
        this.costCenter.store.baseParams=currentBaseParams;
    }, this);
     
     this.includeExcludeChildStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'name'
                }, {
                    name: 'value',
                    type: 'boolean'
                }],
            data: [[WtfGlobal.getLocaleText("acc.includechildcustomer"), true], [WtfGlobal.getLocaleText("acc.excludechildcustomer"), false]]
        });
         /* 
          initial value of combobox is set to All and disabled is true for disable purpose
         */
        this.includeExcludeChildCmb = new Wtf.form.ComboBox({
            labelSeparator: '',
            labelWidth: 0,
            triggerAction: 'all',
            mode: 'local',          
            valueField: 'value',
            displayField: 'name',
            store: this.includeExcludeChildStore,
            value: 'All',
            width: 200,
            disabledClass: "newtripcmbss",
            name: 'includeExcludeChildCmb',                    
            hiddenName: 'includeExcludeChildCmb',
            emptyText:'All',
            disabled:true 
        });  
        
 this.CreditTerm =CommonERPComponent.createCreditTermPagingComboBox(80,250,30,this);
    var baseParams={
        mode: 22,
        onlyProduct:true,
        isFixedAsset:this.isFixedAsset,
        termSalesOrPurchaseCheck:this.isCustomer,
        includeBothFixedAssetAndProductFlag:this.isLeaseFixedAsset,
        excludeParent:true
    };
this.productname =CommonERPComponent.createProductPagingComboBox(100,150,Wtf.ProductCombopageSize,this,baseParams,false);
 
if(this.isJobWorkOrderReciever!=undefined && this.isJobWorkOrderReciever!=""){
    if(this.productname.store!=undefined && this.productname.store!=null){
        var currentBaseParams = this.productname.store.baseParams;
        currentBaseParams.type = this.isCustomer?Wtf.producttype.customerAssembly:Wtf.producttype.assembly
        this.productname.store.baseParams = currentBaseParams;
    }
}
    if (!this.isCustBill)
    {
        WtfGlobal.setAjaxTimeOut();
    }
this.productname.store.on("load", function() {
    if(this.productname.getRawValue() == "" || this.productname.getRawValue() == undefined ||this.productname.getRawValue() == null){

        var record = new Wtf.data.Record({
            productid: "",
            productname: "All Records",
            type:"Type",
            description:"Description",
            pid:"All Records",
        });
        this.productname.store.insert(0, record);    
    }
}, this);
      
this.productCategory =CommonERPComponent.createProductCategoryPagingComboBox(100,250,30,this);
  this.pmtRec = new Wtf.data.Record.create([
        {name: 'methodid'},
        {name: 'methodname'},
        {name: 'accountid'},
        {name: 'acccurrency'},
        {name: 'accountname'},
        {name: 'isIBGBankAccount', type:'boolean'},
        {name: 'isdefault'},
        {name: 'detailtype',type:'int'},
        {name: 'acccustminbudget'},
        {name: 'autopopulate'}
    ]);
    this.pmtStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.pmtRec),
        url : "ACCPaymentMethods/getPaymentMethods.do",
        baseParams:{
            mode:51
        }
    });    
    
    this.pmtMethod = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.PaymentMethod"),
        store: this.pmtStore,
        name: "pmtmethod",
        hiddenName: "pmtmethod",
        id: 'pmtmethod' + this.heplmodeid + this.id,
        emptyText: WtfGlobal.getLocaleText("acc.mp.selpayacc"),
        valueField: 'methodid',
        displayField: 'methodname',
        listWidth: 150,
        width: 100,
        hidden: this.isOrder || !this.isCustomer || !this.mapDefaultPmtMethod,
        hideLabel: !this.mapDefaultPmtMethod,
        triggerAction: 'all',
        typeAhead: true,
        forceSelection: true
    });
    
    this.pmtStore.on("load",function(){
        var newRecord= new Wtf.data.Record({
        methodid:'all',
        methodname:'All'
    });
        this.pmtStore.insert(0,newRecord);
    },this);
    
   var storeForCustomerBankAccountType = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.salesPersonRec),
        url:"ACCMaster/getMasterItems.do",
        baseParams:{
            mode:112,
            groupid:61
        }
    });
   
    /*
     * ERP-29076 - Adding customer bank account type
     */
    this.customerBankAccountType = new Wtf.common.Select({
            triggerAction:'all',
            multiSelect:true,
            mode:'remote',
            valueField:'id',
            displayField:'name',
            extraFields:[],
            id:"customerbankaccounttype"+this.heplmodeid+this.id,
            store:storeForCustomerBankAccountType,
            hidden:!(this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.account.companyAccountPref.activateIBGCollection),
            hideLabel:true,
            disabled:!(this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.account.companyAccountPref.activateIBGCollection),
            width:100,
            listWidth:150,
            forceSelection: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.61"),
            emptyText: WtfGlobal.getLocaleText("acc.invoice.selectCustomerBankAccountType"),
            name:'customerbankaccounttype',
            hiddenName:'customerbankaccounttype',
            activated:this.isCustomer ? true : false
        });
        
        storeForCustomerBankAccountType.on("load", function() {
        var record = new Wtf.data.Record({
            id: "",
            name: "All Records"
        });
        storeForCustomerBankAccountType.insert(0, record);
        this.customerBankAccountType.setValue("");
        }, this);
    
        this.customerBankAccountType.on('beforeselect', function (combo, record, index) {
                return validateSelection(combo, record, index);
        }, this);
        
        if(this.isCustomer){
          storeForCustomerBankAccountType.load();
        }
        this.customerBankAccountType.on('select',function(obj){
            var currentBaseParams = this.Store.baseParams;
            currentBaseParams.customerBankAccountType = obj.getValue();
        this.Store.baseParams = currentBaseParams;
            this.loadStore();
        },this);
    this.productCategory.store.on("load", function() {
        if(this.productCategory.getRawValue() == "" || this.productCategory.getRawValue() == undefined ||this.productCategory.getRawValue() == null){
            var record = new Wtf.data.Record({
                id: "",
                name: "All Records"
            });
            this.productCategory.store.insert(0, record);
        }
    }, this);
   
    this.FormSelectionStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'id',
            type:'string'
        }, 'name'],
        data :[
            ["0","All"],
            ["1","Without Form"],
            ["2","C Form"],
            ["3","E1 Form"],
            ["4","E2 Form"],
            ["5","F Form"],
            ["6","H Form"],
            ["7","I Form"],
            ["8","J Form"]],
    
        sortInfo : {
            field : 'name',
            direction : 'ASC'
        }
    });
    this.FormType=new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.invoice.formtoIssue"), 
        name:'formtypeid',
        store:this.FormSelectionStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        width : 70,
        allowBlank:false,
        hidden:true, //Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ,  // refer ERP-34181
        hideLabel:true,//Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ,  // refer ERP-34181
        listWidth:120,
        hiddenName:'formtypeid',
        emptyText:WtfGlobal.getLocaleText("acc.invoice.selectformtoIssue"),
        forceSelection:true,
        triggerAction:'all'
    });
    this.vatCommodityRec = Wtf.data.Record.create ([
        {name:'id'},
        {name:'name'},
        {name:'vatcommoditycode'},
        {name:'vatscheduleno'},
        {name:'vatscheduleserialno'},
        {name:'vatnotes'}
    ]);

    this.vatCommodityStore=new Wtf.data.Store({
    url:"ACCMaster/getMasterItems.do",
        baseParams:{
            mode:112,
            groupid:42
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.vatCommodityRec)
    });
    this.vatCommodityStore.on('load', function(){
        if(this.vatCommodityStore.find('id',"all")<0){
            var re = new Wtf.data.Record({
                id: "all",
                name: "All"
            });
            this.vatCommodityStore.insert(0, re);
        }
        this.vatCommodityCombo.setValue('all');
    },this);
        
    this.vatCommodityStore.load();
    
    
    this.vatCommodityCombo= new Wtf.form.FnComboBox({
        fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.commodityNm")+"'>"+ WtfGlobal.getLocaleText("acc.product.commodityNm")  +"</span>", //'Product Price Currency*',
        hiddenName:'vatcommoditycode',
        name:'vatcommoditycode',
        id:'vatcommoditycode'+this.id,
        width:70,
        listWidth:120,
        anchor:'80%',
        hidden:(Wtf.Countryid !='105' || !Wtf.account.companyAccountPref.enablevatcst), // 105 for India country
        hideLabel:(Wtf.Countryid !='105' || !Wtf.account.companyAccountPref.enablevatcst),
        allowBlank:true,
        store: this.vatCommodityStore,
        valueField:'id',
        emptyText:WtfGlobal.getLocaleText("acc.product.commodityTT"),  //'Please select Currency...',
        forceSelection: true,
        displayField:'name',
        scope:this,
        selectOnFocus:true
    });
        
    this.FormStatusStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'id',
            type:'string'
        }, 'name'],
        data :[
            ["0","All"],
            ["1","NA"],
            ["2","Pending"],
            ["3","Submitted"]],
        sortInfo : {
            field : 'name',
            direction : 'ASC'
        }
    });
    this.FormStatus=new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.formstatus"), 
        name:'checkformstatus',
        store:this.FormStatusStore,     
        valueField:'id',
        displayField:'name',
        mode: 'local',
        width : 70,
        allowBlank:false,
        hidden:true, //Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA , // refer ERP-34181
        hideLabel:true, //Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA , // refer ERP-34181
        listWidth:120,
        hiddenName:'checkformstatus',
        emptyText:WtfGlobal.getLocaleText("acc.invoiceList.selectformstatus"),
        forceSelection:true,
        triggerAction:'all'
    });
    this.FormDetailsWindowBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.invoiceList.formDetailsWindow"), 
        scope: this,
        disabled : true,
        hidden:true, //Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA, refer ERP-34181
        tooltip: WtfGlobal.getLocaleText("acc.invoiceList.formDetailsToolTip"),
        handler: this.formDetailsWindow,
        iconCls:'accountingbase fetch'
    });
     this.CreditTerm.store.on("load", function() {
        if(this.CreditTerm.getRawValue() == "" || this.CreditTerm.getRawValue() == undefined ||this.CreditTerm.getRawValue() == null){
            var record1 = new Wtf.data.Record({
                termid: "",
                termname: "All Records"
            });
            this.CreditTerm.store.insert(0, record1);
        }
    }, this);

    this.custVendCategory =CommonERPComponent.createCustomerVendorCategoryPagingCombobox(80,250,30,this,false);//false for invoicelist
    this.custmerCmb =CommonERPComponent.createCustomerPagingComboBox(80,Wtf.account.companyAccountPref.accountsWithCode?350:240,Wtf.CustomerCombopageSize,this);
    this.vendorCMB =CommonERPComponent.createVendorPagingComboBox(100,Wtf.account.companyAccountPref.accountsWithCode?350:240,Wtf.VendorCombopageSize,this);

    this.custVendCategory.store.on("load", function() {
        if(this.custVendCategory.getRawValue() == "" || this.custVendCategory.getRawValue() == undefined ||this.custVendCategory.getRawValue() == null){
            var record = new Wtf.data.Record({
                id: "",
                name: "All Records"
            });
            this.custVendCategory.store.insert(0, record);
        }
    }, this);
   
    this.custVendCategory.on("select", function(cmb, rec, ind) {
        this.person="";
        this.filtercustid = rec.data.id;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.customerCategoryid = this.filtercustid;
        this.Store.baseParams = currentBaseParams;
    }, this);

    this.custmerCmb.store.on("load", function() {
        if(this.custmerCmb.getRawValue() == "" || this.custmerCmb.getRawValue() == undefined ||this.custmerCmb.getRawValue() == null){
            var record = new Wtf.data.Record({
                accid: "",
                accname: "All Records",
                acccode:""
            });
            this.custmerCmb.store.insert(0, record);
        }
        if(this.person!="" && this.person!=undefined)
            this.custmerCmb.setValue(this.person);
    }, this);
    
    this.vendorCMB.store.on("load", function() {
        if(this.vendorCMB.getRawValue() == "" || this.vendorCMB.getRawValue() == undefined ||this.vendorCMB.getRawValue() == null){
            var record = new Wtf.data.Record({
                accid: "",
                accname: "All Records",
                acccode:""
            });
            this.vendorCMB.store.insert(0, record);
        }
        if(this.person!="" && this.person!=undefined)
            this.vendorCMB.setValue(this.person);
    }, this);
    
    this.statusArr = [['All', 'All'], ['Open', 'Open'], ['Closed', 'Closed'], ['Rejected', 'Rejected'], ['Partially Delivered', 'Partially Delivered']];
    if (!this.isCustomer) {
        this.statusArr = [['All', 'All'], ['Open', 'Open'], ['Closed', 'Closed'], ['Partially Delivered', 'Partially Delivered']];
    }
    this.statusStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data: this.statusArr
    });

    this.statusFilterCombo= new Wtf.form.ComboBox({           
        triggerAction:'all',
        mode: 'local',
        fieldLabel:"Request Status",
        valueField:'id',
        displayField:'name',
        store:this.statusStore,
//        hidden:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
//        hideLabel:!Wtf.account.companyAccountPref.dependentField || this.isFixedAsset,
        typeAhead: true,
        forceSelection: true,
        name:'requeststatus',
        hiddenName:'requeststatus',
        hideLabel: true,
        emptyText: "Request Status",
        width:100,
        value:"All",
        listWidth:150
           
    });
    
    this.statusFilterCombo.on("select", function(cmb, rec, ind) {
        this.filterstatusid = rec.data.id;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.requestStatus = this.filterstatusid;
        this.Store.baseParams = currentBaseParams;
    }, this);
    this.productname.on("select", function(cmb, rec, ind) {
        this.filterproductid = rec.data.id;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.productid = this.filterproductid;
        this.Store.baseParams = currentBaseParams;
    }, this);
 this.CreditTerm.on("select", function(cmb, rec, ind) {
        this.filtertermid = rec.data.termid;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.termid = this.filtertermid;
        this.Store.baseParams = currentBaseParams;
    }, this);
   this.pmtMethod.on("select",function(cmb, rec, ind){
       this.filterpmtMethod=rec.data.pmtmethod;
       var currentBaseParams = this.Store.baseParams;
       currentBaseParams.pmtmethod=this.filterpmtMethod;
       this.Store.baseParams = currentBaseParams;
   },this);
    this.custmerCmb.on("select", function(cmb, rec, ind) {
        /*
        on selection of customer/vendor enabling includeExcludeChildCmb combobox   
        */
        this.includeExcludeChildCmb.enable(); 
        this.includeExcludeChildCmb.clearValue();
        this.includeExcludeChildCmb.setValue(false);
        this.person="";
        this.filtercustid = rec.data.id;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.newcustomerid = this.filtercustid;
        this.Store.baseParams = currentBaseParams;
    }, this);

    this.vendorCMB.on("select", function(cmb, rec, ind) {
        this.person="";
        this.filtervendorid = rec.data.id;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.newvendorid = this.filtervendorid;
        this.Store.baseParams = currentBaseParams;
    }, this);
   
    this.submitBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),
        tooltip : WtfGlobal.getLocaleText("acc.invReport.fetchTT"), 
        id: 'submitRec' + this.id,
        scope: this,
        iconCls:'accountingbase fetch',
        disabled :false
    });
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        hidden:(this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid == Wtf.Acc_RFQ_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId)?false:(this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId|| this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_RFQ_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId)?false:true,
        scope: this,
        handler: function() {
            if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
                this.expandButtonClicked = true;
            }
            expandCollapseGrid(this.expandCollpseButton.getText(), this.expandStore, this.grid.plugins[0], this);
        }
    });
    
    this.approvalHistoryBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.ApprovalHistory"),
        scope: this,
        hidden:this.moduleid == Wtf.Acc_Security_Gate_Entry_ModuleId|| this.isRFQ || this.isLeaseFixedAsset || this.isConsignment || this.isfromReportList,//true if outstanding SO/PO Report is Clicked from Reportlist 
        disabled : true,
        tooltip: WtfGlobal.getLocaleText("acc.field.ViewApprovalHistory"),
        handler: this.viewApprovalHistory,
        iconCls: "advanceSearchButton"
    });

    var bulkBtnHidden=true;
    if(this.isfromReportList && this.isCustomer && !WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createinvoice) && Wtf.account.companyAccountPref.showBulkDOFromSO){
        bulkBtnHidden=false;
    }else if(!this.isfromReportList && (this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId ) && !WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createinvoice) && Wtf.account.companyAccountPref.showBulkInvoicesFromSO){
       bulkBtnHidden=false;  
    }
    
    /* Button to create bulk invoices from SO  */
    this.bulkInv = new Wtf.Toolbar.Button({
        text: this.isfromReportList ? WtfGlobal.getLocaleText("acc.common.bulkDO")  : WtfGlobal.getLocaleText("acc.common.bulkInvoices"),
        tooltip:this.isfromReportList ?  WtfGlobal.getLocaleText("acc.common.bulkDO")  : WtfGlobal.getLocaleText("acc.common.bulkInvoices"),
        id: 'bulkInv' + this.id,
        hidden:bulkBtnHidden,    
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.add),
        handler: function() {
            var panel = null;
        
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getSequenceFormatStore.do",
                params: {
                
                    mode: this.isCustomer?(this.isfromReportList ? "autodo" :"autoinvoice"):"autogoodsreceipt"
                }
            }, this, function(response) {
                if (response.data && response.data.length > 0) {
                    if (response.data[0].id == "NA") {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), this.isfromReportList ? WtfGlobal.getLocaleText("acc.common.setSeqNoDeliveryOrder"):WtfGlobal.getLocaleText("acc.common.setSeqNoInvoice")], 2);
                        return;
                    }
                    if (panel == null) {
                        panel = callBulkInvoicesList(this.moduleid,this.Store,this.isfromReportList,this.isCustomer);
                    }
                    Wtf.getCmp('as').setActiveTab(panel);
                    Wtf.getCmp('as').doLayout();

                }

            }, function(response) {
                });
        }
    });
//     this.customReportViewBtn = new Wtf.Toolbar.Button({
//        text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"), 
//        scope: this,
//       // hidden:!(this.isOrder && this.isCustomer),
//        tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
//        handler: this.customizeView,
//        iconCls:'accountingbase fetch'
//    });
    this.submitBttn.on("click", this.submitHandler, this);
    this.tbar2 = new Array();
    this.tbar3=new Array();
    if(this.isSalesCommissionStmt){
       this.tbar2.push(WtfGlobal.getLocaleText("acc.field.SelectSalesPerson1"),this.users,'-');
    }
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.quarter"));
    this.tbar2.push(this.monthQuarter);
    this.tbar2.push(WtfGlobal.getLocaleText("acc.reval.year"));
    this.tbar2.push(this.QuarterYear);
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.from"));
    this.tbar2.push(this.startDate);
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.to"));
    this.tbar2.push(this.endDate);
   
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
        if(!config.isOrder&&!this.isQuotation && !this.isRequisition && !this.getRFQs && !this.isRFQ){// For invoice & Vendor Invoice show 'cost center' and 'view' filters in 2nd tbar applied for grid
            //this.tbar2 = new Array();
            this.tbar2.push("-",WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
//           this.tbar2.push("-",WtfGlobal.getLocaleText("acc.field.PaymentTerms"), this.CreditTerm );
        }
    }
    //If Sales invoice report and IBG enabled from system preferences then only show following ibg related fields
    if(this.moduleid ==Wtf.Acc_Invoice_ModuleId && Wtf.account.companyAccountPref.activateIBGCollection){
        this.tbar2.push("-",WtfGlobal.getLocaleText("acc.masterConfig.61"), this.customerBankAccountType);
    }     
    if (!this.isCustBill) {
        this.tbar2.push("-",WtfGlobal.getLocaleText("acc.invReport.prod"), this.productname);
        this.tbar2.push("-",WtfGlobal.getLocaleText("acc.masterConfig.19"),this.productCategory);
    }
     if(this.mapDefaultPmtMethod && this.moduleid ==Wtf.Acc_Invoice_ModuleId){
         /*
          *  //ERM-735 associate default payment method to customer (Push PmtMethod in customer invoice report for filter)
          */
            this.tbar2.push("-",WtfGlobal.getLocaleText("acc.field.PaymentMethod"),this.pmtMethod);      //ERM-735 associate default payment method to customer (Push PmtMethod in report for filter)
        }
//    if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){   
//        this.tbar2.push("-",!this.isCustomer? WtfGlobal.getLocaleText("acc.invoice.formtoIssue"): WtfGlobal.getLocaleText("acc.invoice.formtoreceive"),this.FormType);
//        this.tbar2.push("-",WtfGlobal.getLocaleText("acc.invoiceList.formstatus"),this.FormStatus);
//        this.tbar2.push("-",WtfGlobal.getLocaleText("acc.field.commodity.search"),this.vatCommodityCombo);
//    }
// refer ERP-34181
    if (this.businessPerson == "Customer")
   {
        this.tbar3.push(WtfGlobal.getLocaleText("acc.up.3"), this.custmerCmb);       
    }
    else
    {
        if(!this.isRequisition)
        {  
            this.tbar3.push(WtfGlobal.getLocaleText("acc.up.4"), this.vendorCMB);
        }
    }
    if(!this.isRequisition){
        /*
        show  includeExcludeChildCmb combobox after customer/vendor combobox 
        */
       this.tbar3.push("-", this.includeExcludeChildCmb );
        var label = this.isCustomer ? WtfGlobal.getLocaleText("acc.masterConfig.7") : WtfGlobal.getLocaleText("acc.masterConfig.8") ;
        this.tbar3.push("-",label,this.custVendCategory);
    }
        if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
        if(!config.isOrder&&!this.isQuotation && !this.isRequisition && !this.getRFQs && !this.isRFQ){// For invoice & Vendor Invoice show 'cost center' and 'view' filters in 2nd tbar applied for grid
            //this.tbar2 = new Array();
         this.tbar3.push("-",WtfGlobal.getLocaleText("acc.field.PaymentTerms"), this.CreditTerm );       
        }
    }
    if(this.isConsignment){
     this.tbar3.push("-");
     this.tbar3.push("Status",this.statusFilterCombo); 
    }
    if(this.isRequisition)
    {
        this.tbar2.push("-");
        this.tbar2.push(this.submitBttn);
        this.tbar2.push(this.expandCollpseButton);
        this.tbar2.push(this.approvalHistoryBtn);
    }
    else
    {
        this.tbar3.push("-");
        this.tbar3.push(this.submitBttn); 
        this.tbar3.push(this.expandCollpseButton);
        this.tbar3.push(this.bulkInv);
    }
 
        
    if(this.pendingapproval) {
        this.emptytext1 = this.emptytext2 = this.emptytext3 = WtfGlobal.getLocaleText("acc.common.norec");
    } else {
        if(this.isConsignment){
            if(this.isCustomer){
                this.emptytext1=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:callConsignmentInvoice(false,null,null,false,false,true)'>"+" "+WtfGlobal.getLocaleText("acc.rem.147")+" "+this.label+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")    ;
            }else{           
                this.emptytext1=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:callConsignmentGoodsReceipt(false,null,null,false,false,true)'>"+" "+WtfGlobal.getLocaleText("acc.rem.147")+" "+this.label+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")    ;  
            }
        }else{
            this.emptytext1=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript: openInvTab("+config.isCustomer+","+config.isOrder+","+config.isCustBill+","+config.isQuotation+","+config.isJobWorkOrderReciever+","+this.isSecurityGateEntry+")'>"+" "+WtfGlobal.getLocaleText("acc.rem.147")+" "+this.label+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")    ;
        }
        this.emptytext2=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript: openCashTransTab("+config.isCustomer+","+config.isCustBill+")'>"+WtfGlobal.getLocaleText("acc.rem.147")+" "+cashType+" "+WtfGlobal.getLocaleText("acc.rem.148")+" </a>")    ;
        this.emptytext3=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript: openQuotationTab()'>"+WtfGlobal.getLocaleText("acc.rem.147")+" "+this.businessPerson+" "+WtfGlobal.getLocaleText("acc.accPref.autoQN")+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")    ;
        this.emptytext4=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript: callVendorQuotation()'>"+WtfGlobal.getLocaleText("acc.rem.147")+" "+this.businessPerson+" "+WtfGlobal.getLocaleText("acc.accPref.autoQN")+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")    ;
        this.emptytext5=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript: callPurchaseReq()'>"+WtfGlobal.getLocaleText("acc.rem.147")+" "+WtfGlobal.getLocaleText("acc.accPref.autoPRequisition")+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")    ;
        this.emptytext6=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")+" "+"<br>" +" "+WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn'))  ;
        this.emptytext7=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")+" "+"<br>" +" "+WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn'))  ;
    }
    if(this.isfromsearchwin){
         this.emptytext1=WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn'))    ;
    }
    if (this.isOutstanding){
        // this.emptytext1 = this.emptytext2 = this.emptytext3 = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
        this.emptytext1 = this.emptytext2 = this.emptytext3  = '<div style="font-size:15px; text-align:center; color:#CCCCCC; font-weight:bold; margin-top:8%;">No Outstanding Record Available.<br></div>';
    }

    if(this.isFixedAsset || this.isLeaseFixedAsset){
        this.emptytext2 = '';
        this.emptytext3 = '';
        this.emptytext1 =  WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")+" "+"<br>" +" "+WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn'));
    }
    if(this.isConsignment){
       this.emptytext2 = '';
       this.emptytext3 = '';
    }
    this.deletedRecordsEmptyTxt = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
    this.expander = new Wtf.grid.RowExpander({});
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect :false,                          //this.isRequisition ? false : true,
        hidden:this.isSalesCommissionStmt
    });
    this.gridView1 = (config.consolidateFlag||this.isSalesCommissionStmt)?new Wtf.grid.GroupingView({
            forceFit:false,
            showGroupName: true,
            enableNoGroups:false, // REQUIRED!
            hideGroupedColumn: true,
            emptyText:(config.isQuotation?(this.isCustomer? this.emptytext3: this.emptytext4) : (this.isConsignment?this.emptytext1 : (this.emptytext1+ (config.isOrder?"":"<br>"+this.emptytext2))))
        }):{
            forceFit:false,
            emptyText: this.pendingapproval || config.isRFQ ? WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")) : config.isRequisition ? (this.isFixedAsset? this.emptytext6 : this.emptytext5) : (config.isQuotation?(this.isCustomer? this.emptytext3: this.isFixedAsset? this.emptytext7 : this.emptytext4) : (this.emptytext1+(config.isOrder?"":"<br>"+this.emptytext2)))
           
        };
       
        this.gridColumnModelArr=[];
        this.gridColumnModelArr.push(this.sm,this.expander,{
            id: 'favoritePrinted',
            header: '',
//            dataIndex:'isfavourite',
            hidden:(this.isSalesCommissionStmt || this.pendingapproval)?true:false,
            width:60,
            renderer : function(val, meta, record, rowIndex){
                var value = "";
                if(record.data.isfavourite){
                    value = '<img id="starValiFlag" class="favourite" style="cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.favourite')+'" src="../../images/star-valid.png">';
                }else{
                    value = ' <img id="starInvalidFlag" class="favourite" style="cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.notfavourite')+'" src="../../images/star-invalid.png">'
                }
                if(record.data.isprinted){
                    value += '<img id="printValiFlag" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.printed')+'" src="../../images/printed.gif">';
                }else{
                    value += '<img id="printInValiFlag" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.notprinted')+'" src="../../images/not-printed.gif">'
            }
                return value;
            }
        },{
            
            header: '',
            hidden:(this.isSalesCommissionStmt || this.pendingapproval || this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid==Wtf.Acc_RFQ_ModuleId  || this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId)?true:false,
            width:60,
           dataIndex:'isEmailSent',
            renderer : function(val, meta, record, rowIndex){
                var value = "";
                if(record.data.isEmailSent){
                    value += '<img id="emailFlag" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.emailSent')+'" src="../../images/emailSent.png">';
                }else{
                    value += '<img id="emailFlag" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.emailNotSent')+'" src="../../images/emailNotSent.png">';
            }
                return value;
            }
        },{
            header:" ",
            hidden:true,
            dataIndex:'billid',
            hideable:false     //ERP-5269[SJ] 
        },{
            header:WtfGlobal.getLocaleText("acc.field.Company"), 
            dataIndex:'companyname',
            width:20,
            pdfwidth:150,
            sortable: this.RemoteSort,
            hidden:true
        },{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.cn.9"),
            dataIndex:this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId ? 'prno' : ( this.moduleid==Wtf.Acc_RFQ_ModuleId ? 'rfqno' : 'billno'),
            id: this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId ? "prno"+this.heplmodeid+this.id : ( this.moduleid==Wtf.Acc_RFQ_ModuleId ? 'rfqno'+this.heplmodeid+this.id : 'billno'+this.heplmodeid+this.id),
            width:150,
            pdfwidth:75,
            sortable:this.RemoteSort,
            //renderer:(config.isQuotation||config.isOrder||config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
            renderer:( this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid==Wtf.Acc_RFQ_ModuleId) ? WtfGlobal.linkDeletedRendererForPRandRFQ :((config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer)
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.jeno"),  //"Journal Entry No",
            dataIndex:'entryno',
            hidden:this.isOrder||this.isQuotation ||this.isRFQ,
            sortable: this.RemoteSort,
            width:150,
            pdfwidth:75,
            renderer:WtfGlobal.linkDeletedRenderer
            }
        /*,{
            header:"",
            dataIndex:'this.isOrder?'orderamount':vendorinvoice',
            hidden:this.isOrder,
            pdfwidth:75
        }*/,{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
            dataIndex:'date',
            id:"date"+this.heplmodeid+this.id,            
            align:'center',
            sortable: this.RemoteSort,
            width:150,
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.due"),  //"Due Date",
            dataIndex:'duedate',
            id:"duedate"+this.heplmodeid+this.id,
            sortable:this.RemoteSort,
            align:'center',
            width:150,
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden:(this.isQuotation||this.isSalesCommissionStmt)
        }
    )
    /*
     * Purchase Invoice Type or Purchase Order type column in added in respective grid.
     * if Purchse Invoice then will take data in 'purchaseinvoicetype' otherwise 'purchaseordertype' 
     */
    if(this.moduleid === Wtf.Acc_Vendor_Invoice_ModuleId ||this.moduleid === Wtf.Acc_Purchase_Order_ModuleId){
        this.gridColumnModelArr.push({    
            header:this.label+" "+WtfGlobal.getLocaleText("acc.invoiceList.type"),
            dataIndex :this.moduleid === Wtf.Acc_Vendor_Invoice_ModuleId ?  'purchaseinvoicetype':'purchaseordertype',
            scope: this,
            pdfwidth:80,
            align:'left',
            width:80
        })
    }
    if(this.isConsignment){
      this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.inventorysetup.custwarehouse"), //"Customer Warehousename",
            dataIndex: 'custWarehousename',
            sortable: this.RemoteSort,
            align: 'center',
            width: 80
        })
     }   
     if(this.moduleid!=Wtf.Acc_Sales_Order_ModuleId  && this.moduleid!=Wtf.Acc_Purchase_Order_ModuleId  ){
        this.gridColumnModelArr.push( {
               header:WtfGlobal.getLocaleText("acc.invoiceList.amtduestatus"),
               dataIndex:'amountDueStatus',
               width:150,
               pdfwidth:75,
               hidden:this.isRFQ || !this.isSalesCommissionStmt || this.isOrder ||this.isQuotation,
               summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
           },{
               header:WtfGlobal.getLocaleText("acc.invoiceList.amtDue"),  //"Amount Due",
               dataIndex:'amountdue',
               align:'right',
               hidden:this.isOrder||this.isQuotation||this.isSalesCommissionStmt ||this.isRFQ,
               width:150,
               pdfwidth:75,
               pdfrenderer: 'rowcurrency',
               renderer:this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this)
           })
     }
    
 //    {
//            header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"),  //"Due Date",
//            dataIndex:'requestLocationname',
//            sortable:this.RemoteSort,
//            align:'center',
//            hidden:(this.isRFQ || this.isRequisition),
//            width:80
//        },
          this.gridColumnModelArr.push({
            header:WtfGlobal.getLocaleText("acc.invoiceList.shipdate"),
            dataIndex:'shipdate',
            sortable:this.RemoteSort,
            align:'center',
            width:150,
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden:true
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):(this.isRFQ?WtfGlobal.getLocaleText("acc.invoiceList.ven1"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))),  //this.businessPerson,
            pdfwidth:75,
            width:150,
            renderer:WtfGlobal.deletedRenderer,
            dataIndex:'personname',
            id:"personname"+this.heplmodeid+this.id,
            sortable:  this.RemoteSort,
            hidden:this.isSalesCommissionStmt || this.isRequisition
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.cust.aliasname"):WtfGlobal.getLocaleText("acc.ven.aliasname")),
            pdfwidth:75,
            width:150,
            renderer:WtfGlobal.deletedRenderer,
            dataIndex:'aliasname',
            id:"aliasname"+this.heplmodeid+this.id,
            sortable:  this.RemoteSort
//            hidden:this.isSalesCommissionStmt || this.isRequisition
        },{
            header:"Billing Address",
            dataIndex:'billingAddress',
            id:"billingAddress"+this.heplmodeid+this.id,
            sortable:this.RemoteSort,
            align:'center',
            width:80,
            pdfwidth:80
        },{
            header:"Shipping Address",
            dataIndex:'shippingAddress',
            id:"shippingAddress"+this.heplmodeid+this.id,
            sortable:this.RemoteSort,
            align:'center',
            width:80,
            pdfwidth:80
        },{
            header:WtfGlobal.getLocaleText("acc.pmList.gridPaymentMethod"),  // "Payment Method",
            pdfwidth:75,
            width:150,           
            dataIndex:'paymentname',
            sortable:  this.RemoteSort,
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ || this.isOrder || this.isQuotation,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:config.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.salesPerson"):WtfGlobal.getLocaleText("acc.field.AgentSalesman"),  // "Sales Person" or "Agent"
            pdfwidth:75,
            width:150,
            dataIndex:config.isCustomer?'salespersonname':'agentname',
            renderer:WtfGlobal.deletedRenderer,
            sortable:  this.RemoteSort,
            hidden:this.isRequisition|| this.isRFQ ||this.isConsignment
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.SupplierInvoiceNo"), //Supplier Invoice No (SDP-4510)
            dataIndex: 'supplierinvoiceno',
            align: 'center',
            sortable: this.RemoteSort,
            width: 150,
            pdfwidth: 80,
            hidden: !(this.moduleid === Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid === Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId)
        },{
            header: config.isCustomer ? WtfGlobal.getLocaleText("acc.invoice.exportDeclarationNumber") : WtfGlobal.getLocaleText("acc.invoice.importDeclarationNumber"),
            dataIndex: 'importexportdeclarationno',
            align: 'center',
            sortable: this.RemoteSort,
            width: 150,
            pdfwidth: 80,
            hidden: !((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId) && Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA)
        },
//        {
//            header:WtfGlobal.getLocaleText("acc.invoiceList.PORefNo"),  // "PO Ref. Number",    
//            pdfwidth:75,
//            width:150,
//            dataIndex:'porefno',
//            hidden: !(this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Cash_Sales_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId )
//           
//        }
        {
            header:WtfGlobal.getLocaleText("acc.invoice.CustomerPOrefNo"),  // "Customer PO Ref. Number",    
            pdfwidth:75,
            width:150,
            dataIndex:(this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid ==Wtf.Acc_Cash_Sales_ModuleId )? 'porefno': 'customerporefno',
            hidden: !(this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Cash_Sales_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId),
            hideable: !(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId)
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.ShippingTerm"),  // "Shipping Term",        
            pdfwidth:75,
            width:150,
            dataIndex:'shippingterm',
            hidden: !(this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId )
        },{
            header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
            dataIndex:'currencycode',
            id:"currencycode"+this.heplmodeid+this.id,
            hidden:true,
//            hideable:false,
            pdfwidth:85
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.discount"),  //"Discount",
            dataIndex:'discount',
            align:'right',
            width:150,
            pdfwidth:75,
            renderer:this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this),
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.discountinbase"),  //"Discount (In Base Currency)",
            dataIndex:'discountinbase',
            align:'right',
            width:150,
            pdfwidth:75,
            renderer:this.currencyDeletedRendererWithPermissionCheck.createDelegate(this),
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ||this.isOrder||this.isQuotation
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.taxName"),  //"Tax Name",
            dataIndex:'taxname',
            width:150,
            pdfwidth:75,
//            renderer:WtfGlobal.deletedRenderer,
            hidden:(!this.isOrder||this.isSalesCommissionStmt||this.isRequisition || this.isRFQ || (Wtf.account.companyAccountPref.countryid == '137' && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden()))),// hide if company is malaysian and GST is not enabled for it)
            renderer: function(value) {
            value = value.replace(/\'/g, "&#39;");
            value = value.replace(/\"/g, "&#34");
            return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
        }
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.taxAmt"),  //"Tax Amount",
            dataIndex:'taxamount',
            align:'right',
            width:150,
            pdfwidth:75,
            pdfrenderer: 'rowcurrency',
            renderer:this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this),
            hidden:this.isSalesCommissionStmt || this.isRequisition || this.isRFQ || (Wtf.account.companyAccountPref.countryid == '137' && (!Wtf.account.companyAccountPref.enableGST || !this.shouldTaxBeHidden()))// hide if company is malaysian and GST is not enabled for it
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.terms"),  //"Terms Amount"
            dataIndex:'termamount',
            align:'right',
            width:150,
            pdfwidth:75,
            pdfrenderer: 'rowcurrency',
            renderer:this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this),
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ
        },
//        {
//            header: Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ? WtfGlobal.getLocaleText("acc.payment.amtBeforeTax") :WtfGlobal.getLocaleText("acc.invoiceList.AmntBeforeGST"),  //"Amount before GST",
//            align:'right',
//            dataIndex:'amountbeforegst',
//            width:150,
//            pdfwidth:75,
//            pdfrenderer: 'rowcurrency',
//            renderer:this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this),
//            hidden:this.isRFQ||this.isRequisition || !this.isFixedAsset
//        },
        {
            header:(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)? WtfGlobal.getLocaleText("acc.companypreferences.invoiceNetAmountInd"):this.isRequisition ? WtfGlobal.getLocaleText("acc.field.EstimatedCost") : WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),  //"Total Amount",
            align:'right',
            dataIndex:((this.isOrder && this.moduleid!=Wtf.Acc_Purchase_Requisition_ModuleId && this.moduleid!=Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) || this.isQuotation)? 'orderamountwithTax' : 'amount',
            id:((this.isOrder && this.moduleid!=Wtf.Acc_Purchase_Requisition_ModuleId && this.moduleid!=Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) || this.isQuotation)? "orderamountwithTax"+this.heplmodeid+this.id : "amount"+this.heplmodeid+this.id,                
            width:150,
            pdfwidth:75,
            pdfrenderer: 'rowcurrency',
            renderer:this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this),
            hidden:this.isSalesCommissionStmt || this.isRFQ
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome") + " ("+WtfGlobal.getCurrencyName()+")",  //"Total Amount (In Home Currency)",
            align:'right',
            hidden:this.isQuotation?false:this.isSalesCommissionStmt || this.isRFQ, //this.quotation?false:this.isOrder,
            dataIndex:'amountinbase',
            id:"amountinbase"+this.heplmodeid+this.id,
            width:150,
            pdfwidth:75,
            /*
             *commented below property because it does not have any effect on showing or hiding currency
             *in export, print or report. But it is affecting (ERP-29032) as it shows only one
             *digit after decimal whereas it should show company specific.
             **/
            //hidecurrency : true,
            pdfrenderer: 'rowcurrency',
            renderer:this.currencyDeletedRendererWithPermissionCheck.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.field.InvoiceAmount(ApplicableforCommission)"),  //"Invoice Amount (applicable for Commission)",
            align:'right',
            hidden:!this.isSalesCommissionStmt,
            dataIndex:'amountwithouttax',
            width:150,
            pdfwidth:75,
            renderer:this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this)        
        },{
            header: WtfGlobal.getLocaleText("acc.field.InvoiceAmount(ApplicableforCommission)inBaseCurrency")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Invoice Amount (applicable for Commission)",
            align:'right',
            hidden:!this.isSalesCommissionStmt,
            dataIndex:'amountwithouttaxinbase',
            width:150,
            pdfwidth:75,
            renderer:this.currencyDeletedRendererWithPermissionCheck.createDelegate(this),          
            summaryType:'sum',
            hidecurrency : true,
            summaryRenderer: function(value, m, rec) {
            if (value != 0) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            } else {
                return '';
            }
        }
        },{
            header:WtfGlobal.getLocaleText("acc.field.CommissionAmount"),  //"Commission Amount",
            dataIndex:'commission',
            align:'right',
            hidden:!this.isSalesCommissionStmt,
            width:150,
            pdfwidth:75,
            renderer:this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.field.CommissionAmountinBaseCurrency"),  //"Commission Amount",
            dataIndex:'commissioninbase',
            align:'right',
            hidden:!this.isSalesCommissionStmt,
            width:150,
            pdfwidth:75,
            renderer:this.currencyDeletedRendererWithPermissionCheck.createDelegate(this),
            summaryType:'sum',
            summaryRenderer: function(value, m, rec) {
                return WtfGlobal.currencySummaryRenderer(value, m, rec);
//            if (value != 0) {
//                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
//                return retVal;
//            } else {
//                return '';
//            }
        }
        },{
            header:Wtf.account.companyAccountPref.descriptionType,  //"Memo",
            dataIndex:'memo',
            id:"memo"+this.heplmodeid+this.id,
            hidden:this.isSalesCommissionStmt,
            renderer: WtfGlobal.memoRenderer,
            width:150,             
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.common.validTill"),  //"Valid Till",
            dataIndex:'validdate',
            align:'center',
            width:150,
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden:!this.isQuotation
        },
 //               {
//            header:WtfGlobal.getLocaleText("acc.invoiceList.template"),  //"Template",
//            dataIndex:'templatename',
//            hidden:true,        
//            pdfwidth:100           
//        },
           {
            header:this.isRequisition?WtfGlobal.getLocaleText("acc.invoiceList.PRApprovalstatus"):WtfGlobal.getLocaleText("acc.invoiceList.status"),  //"Status",
            dataIndex:this.moduleId == Wtf.Acc_ConsignmentRequest_ModuleId? 'statustype':'status',
             id:this.moduleId == Wtf.Acc_ConsignmentRequest_ModuleId? "statustype"+this.heplmodeid+this.id : "status"+this.heplmodeid+this.id,
             align:'left',
             hidden:this.isRFQ || !this.isOrder || this.isQuotation||this.isSalesCommissionStmt || this.moduleid==Wtf.Acc_Sales_Order_ModuleId ,
             renderer:function (v, m, rec) {
                    if (!v)
                      return v;
                    v = v.replace(/(<([^>]+)>)/ig,"");
                    v = "<div wtf:qtip=\""+ v +"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.invoiceList.PRApprovalstatus")+"'>"+ v +"</div>";
                    if (rec.data.deleted)
                        v = '<del>' + v + '</del>';
                    return v;
             },
             width:150,
             pdfwidth:100
//         },{
//            header:"Expense Type",
//            dataIndex:'isexpenseinv',
//            hidden:this.isOrder,
//            pdfwidth:100
       
        },
        {
            header:WtfGlobal.getLocaleText("acc.invoiceList.billingAddContactPerson"),
            dataIndex:'billingAddContactPerson',
            id:"billingAddContactPerson"+this.heplmodeid+this.id,
            align:'center',
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ || this.isOrder || this.isQuotation,
            width:80,
            pdfwidth:80
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.shippingAddContactPerson"),
            dataIndex:'shippingAddContactPerson',
            id:"shippingAddContactPerson"+this.heplmodeid+this.id,
            align:'center',
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ || this.isOrder || this.isQuotation,
            width:80,
            pdfwidth:80
        },
        {
            header:WtfGlobal.getLocaleText("acc.invoiceList.billingAddContactNo"),
            dataIndex:'billingAddContactNo',
            id:"billingAddContactNo"+this.heplmodeid+this.id,
            align:'center',
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ || this.isOrder || this.isQuotation,
            width:80,
            pdfwidth:80
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.shippingAddContactNo"),
            dataIndex:'shippingAddContactNo',
            id:"shippingAddContactNo"+this.heplmodeid+this.id,
            align:'center',
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ || this.isOrder || this.isQuotation,
            width:80,
            pdfwidth:80
        },
        {
            header: WtfGlobal.getLocaleText("acc.invoiceList.BillingAddEmail"),
            dataIndex:'BillingAddEmail',
            id:"BillingAddEmail"+this.heplmodeid+this.id,
            align:'center',
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ || this.isOrder || this.isQuotation,
            width:80,
            pdfwidth:80
        },
        {
            header:WtfGlobal.getLocaleText("acc.invoiceList.shippingAddEmail"),
            dataIndex:'shippingAddEmail',
            id:"shippingAddEmail"+this.heplmodeid+this.id,
            align:'center',
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ || this.isOrder || this.isQuotation,
            width:80,
            pdfwidth:80
        });
if(this.moduleId == Wtf.Acc_ConsignmentRequest_ModuleId){
    this.gridColumnModelArr.push({
            header:WtfGlobal.getLocaleText("acc.invoiceList.CRApprovalstatus"),  //"Status",
            dataIndex:'approvalstatus',
            align:'left',
            renderer:WtfGlobal.deletedRenderer,
            width:100,
            hidden: this.moduleId == Wtf.Acc_ConsignmentRequest_ModuleId?false:true
    });
}
    /**
     * If 'Profit Margin' activated from Company pref then it will show only SO and CQ report
     */
    if (Wtf.account.companyAccountPref.activateProfitMargin && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.field.TotalMarginAmountInBaset") + " (" + WtfGlobal.getCurrencyName() + ")", //"Profit Margin Amount In Base Currency,   
            align: 'right',
            width: 60,
            dataIndex: 'totalprofitmargin',
            pdfwidth: 75,
            hidecurrency: true,
            renderer: this.currencyDeletedRendererWithPermissionCheck.createDelegate(this),
            hidden: !(Wtf.account.companyAccountPref.activateProfitMargin && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)),
        }, {
            header: WtfGlobal.getLocaleText("acc.field.TotalMarginPercent"), //"Profit Margin,   
            align: 'right',
            pdfwidth: 60,
            width: 60,
            dataIndex: 'totalprofitmarginpercent',
            hidden: !(Wtf.account.companyAccountPref.activateProfitMargin && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId))
        });
    }
    if (this.moduleid === Wtf.Acc_Sales_Order_ModuleId) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.creditTerm"),
            dataIndex: 'termname',
            scope: this,
            pdfwidth: 80,
            align: 'left',
            width: 50
        })
    }

   // Following coloumns are for India compliance
        
//if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
//    this.gridColumnModelArr.push(
//    {
//        header:WtfGlobal.getLocaleText("acc.invoiceList.formstatus"),  //Form Status,
//        dataIndex:'formstatus',
//        sortable:this.RemoteSort,
//        align:'center',
//        style: 'background:green;',
//        width:100,
//        pdfwidth:80,
//        hideable: (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId ),//To also hide it in sortable list
//        hidden:true, //Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId ),
//        renderer : function(val, meta, record, rowIndex){
//            if(record.data.formstatus == "NA" ){
//                //                    meta.attr = 'style="background-color:#808080;"';
//                meta.attr = 'style="color:#9292a4;font-weight:bold;"';//Gray For NA
//            }else if(record.data.formstatus == "Pending" ){
//                meta.attr = 'style="color:#ff0000;font-weight:bold;"';//Red For Pending
//            }else if(record.data.formstatus == "Submitted" ){
//                meta.attr = 'style="color:#008B00;font-weight:bold;"';//Green For Submitted.
//            }
//            return val;
//        }
//    },{
//        header:WtfGlobal.getLocaleText("acc.formDetailsWindow.formSeriesNo"),  //Form Series No,
//        dataIndex:'formseriesno',
//        sortable:this.RemoteSort,
//        align:'center',
//        width:150,
//        pdfwidth:80,
//        hideable: (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId ),//To also hide it in sortable list
//        hidden:true,//Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId )
//    },{
//        header:WtfGlobal.getLocaleText("acc.formDetailsWindow.formNo"),  //Form No,
//        dataIndex:'formno',
//        sortable:this.RemoteSort,
//        align:'center',
//        width:150,
//        pdfwidth:80,
//        hideable: (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId ),//To also hide it in sortable list
//        hidden:true,//Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId )
//    },{
//        header:WtfGlobal.getLocaleText("acc.formDetailsWindow.formDate"),  //Form Date,
//        dataIndex:'formdate',
//        sortable:this.RemoteSort,
//        align:'center',
//        width:150,
//        pdfwidth:80,
//        renderer:WtfGlobal.onlyDateDeletedRenderer,
//        hideable: (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId ),//To also hide it in sortable list
//        hidden:true,//Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId )
//    },{
//        header:WtfGlobal.getLocaleText("acc.formDetailsWindow.amount"),  //Form Amount,
//        dataIndex:'formamount',
//        sortable:this.RemoteSort,
//        align:'right',
//        width:150,
//        pdfwidth:80,
//        renderer:WtfGlobal.currencyDeletedRenderer,
//        hideable: (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId ),//To also hide it in sortable list
//        hidden:true,//Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId )
//    },
/**
 * this field are removed now. refere ERP-34181
 */
//    {
//        header:WtfGlobal.getLocaleText("acc.invoicelist.OtherChargesAmt"),  //"Other Changes Amount",
//        dataIndex:'OtherTermNonTaxableAmount',
//        align:'right',
//        width:150,
//        pdfwidth:75,
//        hideable: true,//Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA ? (Wtf.account.companyAccountPref.isLineLevelTermFlag && !this.isRequisition):true,//To also hide it in sortable list
//        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
//        hidden: true//Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ? (!Wtf.account.companyAccountPref.isLineLevelTermFlag && this.isRequisition):true//Hidden if LineLevelTerms are not apllicable.
//    });
//}
if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId){ // Only for Purchase report
    this.gridColumnModelArr.push(
    {
        header:WtfGlobal.getLocaleText("acc.agedPay.gridTotal"),  
        dataIndex:'totalAmountWithTDS',
        align:'right',
        width:150,
        pdfwidth:75,
        fixed:(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId) ? false:true,
        hideable: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA && this.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId) ? false:true,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
        hidden: (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId) ? false:true
    },{
        header:WtfGlobal.getLocaleText("acc.invoice.gridTDSAmt"),  //'TDS amount',
        dataIndex:'tdsamount',
        align:'right',
        width:150,
        pdfwidth:75,
        fixed:(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId) ? false:true,
        hideable: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA && this.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId) ? false:true,
        renderer:function(value, m , rec){
                /*
                 * If  in User Administration > Assign Permission > Display Unit Price & Amount in Purchase Document
                 * If it uncheck we will hide amount and show '*****',  
                 */
                if (!Wtf.dispalyUnitPriceAmountInPurchase) {
                    return Wtf.UpriceAndAmountDisplayValue;
                }else{
                    return WtfGlobal.withoutRateCurrencyDeletedSymbol(value, m , rec);
                    
                }
        },
        hidden:(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId) ? false:true
    })
}
        
    /* Column in PO & SO report to identify Whether 
      * 
      * PO is free for SO and SO is free for PO*/
    if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId){
        this.gridColumnModelArr.push({
            header:this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.statusforpo"):WtfGlobal.getLocaleText("acc.invoiceList.statusforso"),
            dataIndex:'statusforcrosslinkage',
            hidden:true,
            renderer:WtfGlobal.deletedRenderer,
            width:150,
            pdfwidth:100
        });
    }
    
       /* Column in PO & SO report to identify Whether 
      * 
      * PO & SO is closed Manually*/
    if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId||this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId){
        this.gridColumnModelArr.push({
            header:WtfGlobal.getLocaleText("acc.invoiceList.closedManually"),
            dataIndex:'closedmanually',
            hidden:false,
            renderer:WtfGlobal.deletedRenderer,
            width:150,
            pdfwidth:100
        });
    }
      if(this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId){
        if(this.moduleid!=Wtf.Acc_ConsignmentRequest_ModuleId){ //for Cosignment Request module we are showing only Request approval
            this.gridColumnModelArr.push({
                header:WtfGlobal.getLocaleText("acc.field.Approval") +WtfGlobal.getLocaleText("acc.invoiceList.status"),  //"Status",
                dataIndex:'approvalstatusinfo',
                align:'center',
                renderer:WtfGlobal.deletedRenderer,
                width:100,
                hidden:(this.moduleid==Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId || this.moduleid== Wtf.Acc_FixedAssets_RFQ_ModuleId || this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId ||this.moduleid==Wtf.Acc_RFQ_ModuleId ||
                        this.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId ||this.moduleid==Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid==Wtf.Acc_Lease_Quotation ||this.moduleid==Wtf.Acc_Lease_Order),
                pdfwidth:100
            });
        }
      }   
        this.gridColumnModelArr.push(
            {
            header:WtfGlobal.getLocaleText("acc.nee.69"),
            dataIndex:'createdby',
            id:"createdby"+this.heplmodeid+this.id,
            width:150,
            pdfwidth:75,
            hidden:(this.isRequisition && this.isRequisition!=undefined)?false:(this.isRFQ || !this.isOrder || !this.isCustomer || this.isQuotation||this.isSalesCommissionStmt) ? true : false
         },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"),  //"Attach Documents",
            dataIndex:'attachdoc',
            id:"attachdoc"+this.heplmodeid+this.id,
            width:150,
            align:'center',
            hidden:(this.isSalesCommissionStmt) ? true : false ,
            renderer : function(val) {
                        return "<div style='height:16px;width:16px;'><div class='pwndbar1 uploadDoc' style='cursor:pointer' wtf:qtitle='"
                        + WtfGlobal
                        .getLocaleText("acc.invoiceList.attachDocuments")
                        + "' wtf:qtip='"
                        + WtfGlobal
                        .getLocaleText("acc.invoiceList.clickToAttachDocuments")
                        +"'>&nbsp;</div></div>";
                    }
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),  //"Attachments",
            dataIndex:'attachment',
            id:"attachment"+this.heplmodeid+this.id,
            width:150,
            hidden:(this.isSalesCommissionStmt) ? true : false ,
            renderer : Wtf.DownloadLink.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.field.LastEditedBy"),
            width:150,
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer,
            dataIndex:'lasteditedby',
            id:"lasteditedby"+this.heplmodeid+this.id,
            hidden:this.isSalesCommissionStmt || !this.isRequisition
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.producttotalamount"),  //Gross Total Amount
            dataIndex:'productTotalAmount',
            id:"productTotalAmount"+this.heplmodeid+this.id,
            align:'right',
            width:150,
            pdfwidth:75,
            renderer:this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this),
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.amountbeforetax"), //Amount before Tax
            dataIndex: 'amountBeforeTax',
            align: 'right',
            width: 150,
            pdfwidth: 75,
            hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ,
            renderer: this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this)
        }/*,{
            header:WtfGlobal.getLocaleText("acc.field.RequestType"),
            width:150,
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer,
            dataIndex:'movementtypename',
            hidden:!this.isConsignment || !this.isMovementWarehouseMapping || !this.isCustomer || this.moduleid==Wtf.Acc_ConsignmentInvoice_ModuleId
    }*/);
       
        var requestTypeConfig = {
            header:WtfGlobal.getLocaleText("acc.field.RequestType"),
            width:150,
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer,
            dataIndex:'movementtypename',
            hidden:!this.isConsignment || !this.isMovementWarehouseMapping || !this.isCustomer
        };
        if(this.moduleid!=Wtf.Acc_ConsignmentInvoice_ModuleId){
            this.gridColumnModelArr.push(requestTypeConfig);
        }
         if(this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId){
            this.gridColumnModelArr.push({
                header:WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate"),//"From Date",
                dataIndex:'fromdate',
                align:'center',
                sortable: this.RemoteSort,
                width:150,
                pdfwidth:80,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.field.Reportlist.returnDate"),  //"Retrun Date",
                dataIndex:'todate',
                sortable:this.RemoteSort,
                align:'center',
                width:150,
                pdfwidth:80,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            });
        }
        /*
         * If Sales invoice report and IBG enabled from system preferences then only show following ibg related fields
         * Adding columns related to ERP-29076
         */    
        if(Wtf.account.companyAccountPref.activateIBGCollection && this.moduleid == Wtf.Acc_Invoice_ModuleId){    
            this.gridColumnModelArr.push(
            {
                header:WtfGlobal.getLocaleText("acc.masterConfig.61"),
                dataIndex:'customerbankaccounttypevalue',
                align:'left',
                width:100,
                sortable:this.RemoteSort,
                pdfwidth:100,
                hidden: !(this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.account.companyAccountPref.activateIBGCollection)
            },{
                header:WtfGlobal.getLocaleText("acc.uob.isGiroGeneratedForInvoice"),
                dataIndex:'isGIROFileGeneratedForUOBBank',
                align:'left',
                width:100,
                sortable:this.RemoteSort,
                pdfwidth:100,
                hidden: !(this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.account.companyAccountPref.activateIBGCollection)
            },{
                header:WtfGlobal.getLocaleText("acc.uob.paymentMethodForGiroForInvoice"),
                dataIndex:'paymentMethodUsedForUOB',
                align:'left',
                width:100,
                sortable:this.RemoteSort,
                pdfwidth:100,
                hidden: !(this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.account.companyAccountPref.activateIBGCollection)
            }
            )
        } 
        // appening custom columns
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[this.moduleid],true);
    
    if (Wtf.account.companyAccountPref.upsIntegration) {
        if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {    //Addition of column 'Total Shipping Cost' when UPS REST Integration is enabled
            this.gridColumnModelArr.push({
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.invoiceList.estimatedShippingCostTT") + "'>" + WtfGlobal.getLocaleText("acc.invoiceList.estimatedShippingCost") + " (USD)" + "</span>", //"Estimated Shipping Cost", Added only if UPS Integration is activated
                dataIndex: 'totalShippingCost',
                align: 'right',
                width: 100,
                renderer: function (value, metaData, record) {
                    if (value != undefined && value != '' && value != 0) {
                        return "<span wtf:qtip='" + "USD " + value + "'>" + "USD " + value + "</span>";
                    } else {
                        return "";
                    }
                },
                pdfwidth: 100
            });
        }
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {    //Addition of column 'Shipment Tracking Number(s)' when UPS REST Integration is enabled
            this.gridColumnModelArr.push({
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.invoiceList.upsTrackingNumbers") + "'>" + WtfGlobal.getLocaleText("acc.invoiceList.upsTrackingNumbers") + "</span>", //"Shipment Tracking Number", Added only if UPS Integration is activated
                dataIndex: 'upsTrackingNumbers',
                align: 'left',
                width: 200,
                renderer: function (value, metaData, record) {
                    return "<span wtf:qtip='" + value + "'>" + value + "</span>";
                },
                pdfwidth: 100
            });
        }
    }
    
    // add Billing Email Address    
    this.gridColumnModelArr.push({
        header: WtfGlobal.getLocaleText("acc.Billing.Email.Address"),
        dataIndex: 'billingEmail',
        align: 'left',
        hidden: true,
        pdfwidth: 100,
        renderer: WtfGlobal.renderDeletedEmailsTo
    });
    this.gridColumnModelArr.push({
        header: WtfGlobal.getLocaleText("acc.invoiceList.subtotal"), //Sub-Total
        dataIndex: 'subtotal',
        id: "subtotal" + this.heplmodeid + this.id,
        align: 'right',
        width: 150,
        pdfwidth: 75,
        renderer: this.withoutRateCurrencyDeletedSymbolWithPermissionCheck.createDelegate(this),
        hidden: this.isSalesCommissionStmt || this.isRequisition || this.isRFQ
    });
    
    /* Status column for Purchase Requisition 
     * To show whether Requisition is open or closed for PO
     */
    if (CompanyPreferenceChecks.statusOfRequisitionForPO() && this.moduleid ==Wtf.Acc_Purchase_Requisition_ModuleId) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.invoiceList.statusforpo"),
            dataIndex: 'statusofpoforrequisition',
            renderer: WtfGlobal.deletedRenderer,
            width: 150,
            pdfwidth: 100
        });
    }
    /*
     * Customer or Vendor code is added into the report grid 
     */
    if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId) {
        this.gridColumnModelArr.push({
            header: this.isCustomer ? WtfGlobal.getLocaleText("acc.common.customer.code") : WtfGlobal.getLocaleText("acc.common.vendor.code"), //"Customer Code or Vendor code",
            dataIndex: 'personcode',
            scope: this,
            pdfwidth: 80,
            align: 'left',
            width: 100
        });
    }
    if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.common.costCenter"),
            dataIndex: 'costcenterName',
            scope: this,
            pdfwidth: 80,
            align: 'left',
            width: 100
        });
    }
    if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.invoiceList.paymentNo"),
            dataIndex: 'linkedpayment',
            scope: this,
            pdfwidth: 80,
            renderer:WtfGlobal.linkDeletedRenderer,
            align: 'left',
            width: 100
        });
    }
    if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDONESIA) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.field.isCreditable"),//Is Creditable,
            dataIndex: 'isCreditable',
            scope: this,
            pdfwidth: 80,
            renderer: function (value, metaData, record) {
                if (value)
                    return "Yes"
                return "No";
            },
            align: 'center',
            width: 100
        });
    }
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        id:"gridmsg"+config.helpmodeid+config.id,
        border:false,
        sm:this.sm,
//        tbar: this.tbar2,
        layout:'fit',
        loadMask:true,
        plugins:(this.isSalesCommissionStmt)?[this.expander,this.summary]:[this.expander],
        viewConfig:this.gridView1,
        forceFit:true,
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr),
        listeners: {
            "reloadexternalgrid": function() {
                if (!this.searchparam)
                    this.loadStore.defer(10, this);
                else
                    this.showAdvanceSearch.defer(10, this);
            },
            scope: this
        }
    });
    this.gridtbarPanel= new Wtf.Panel({
        border: false,
        tbar: this.tbar2,
        layout:'border',
        items: [{
            region:'center',
                layout: 'fit',
                border: false,
            tbar:this.tbar3,
            items:[this.grid]
            }]
    });
    if (this.isfromsearchwin) { // Call from document selection win to link. so no need to show all toolbar.
        this.gridtbarPanel = new Wtf.Panel({
            border: false,
            layout: 'border',
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid]
                }]
        });
    }
    this.grid.getColumnModel().defaultSortable = false;
    var colModelArray = GlobalColumnModelForReports[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray, this.Store);
       this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: (this.isSalesCommissionStmt != undefined && this.isSalesCommissionStmt ==true ) ?  Wtf.Acc_Invoice_ModuleId:this.moduleid,
        advSearch: false,
        customerCustomFieldFlag: this.showCustomerCustomFieldFlag(this.moduleid),
        vendorCustomFieldFlag: this.showVendorCustomField(this.moduleid),
        lineLevelSearch:false
    });
   
    this.grid.on('render', function(){
        this.grid.getView().applyEmptyText();
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    },this);
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.grid.flag = 0;
    this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        //hidden:this.isSalesCommissionStmt,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
   
    this.archieveBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.quote.archieve"),//"Archieve Quotation",
        tooltip :WtfGlobal.getLocaleText("acc.quote.archievetip"),//"Archieve selected Quotation",
        id: 'archieveBtnRec' + this.id,
        scope: this,
        hidden : !this.isQuotation || config.consolidateFlag ||this.isSalesCommissionStmt || this.isLeaseFixedAsset || this.reportbtnshwFlag || this.isConsignment || this.pendingapproval,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport),
        disabled :true
    });
   
    this.unArchieveBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.quote.unarchieve"),//Un-Archive Quotation
        tooltip :WtfGlobal.getLocaleText("acc.quote.unarchievetip"),//Un-Archive selected Quotation
        id: 'unarchieveBtnRec' + this.id,
        scope: this,
        hidden : !this.isQuotation || config.consolidateFlag ||this.isSalesCommissionStmt || this.isLeaseFixedAsset || this.reportbtnshwFlag || this.isConsignment  || this.pendingapproval,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport),
        disabled :true
    });
   
    
    this.approveInvoiceBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.cc.24"),
        tooltip : WtfGlobal.getLocaleText("acc.field.ApprovependingRecord"), //Issue 31009 - [Pending Approval]Window name should be "Approve Pending Invoice" instead of "Approve Pending Approval". it should also have deskera logo
        id: 'approvepending' + this.id,
        scope: this,
//        hidden : this.isQuotation,
        iconCls :this.isRequisition ? "accountingbase prapprove" : getButtonIconCls(Wtf.etype.add),
        disabled :true,
        handler :this.isRequisition? this.approvePendingInvoice :((this.moduleid==Wtf.Acc_Invoice_ModuleId ||this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)?this.BatchApprovePendingTransactions:this.approvePendingTransactions)//if Sales Invoice or Purchase Invoice Report we called 'BatchApprovePendingTransactions' function otherwise single approval function called
    });
   
    this.rejectInvoiceBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Reject"),
        tooltip : WtfGlobal.getLocaleText("acc.field.Rejectpending"),
        id: 'rejectpending' + this.id,
        scope: this,
//        hidden : this.isQuotation,
        iconCls:getButtonIconCls(Wtf.etype.deletebutton),
        disabled :true,
        handler : this.handleReject
    });
   
    this.pendingApprovalBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
        tooltip : WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
        id: 'pendingApprovals' + this.id,
        scope: this,
        hidden : this.isRFQ ||this.isSalesCommissionStmt || this.consolidateFlag || (this.isFixedAsset && !(this.moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) && !(this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) && !(this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId)) || this.isLeaseFixedAsset || this.isConsignment || this.isfromReportList||this.moduleid == Wtf.Acc_Security_Gate_Entry_ModuleId,//true if outstanding SO/PO Report is Clicked from Reportlist 
        iconCls :getButtonIconCls(Wtf.etype.reorderreport)
//        disabled :true
    });
   
    this.pendingApprovalBttn.on('click', function(){
        var panel = null;
       
        if(this.isRequisition) {
            if (this.isFixedAsset) {
                panel = Wtf.getCmp("assetPurchaseRequisitionListPending");
            } else {
                panel = Wtf.getCmp("PurchaseRequisitionListPending");
            }
        }else if(this.isOrder) {
            if (this.isFixedAsset && !this.isCustomer) {
                panel = Wtf.getCmp("assetPurchaseOrderListPending");
            }else{
                panel = this.isCustomer? Wtf.getCmp("SalesOrderListPending") : Wtf.getCmp("PurchaseOrderListPending");
            }
        }else if(this.isQuotation){
            if (this.isFixedAsset) {
                panel = this.isCustomer? Wtf.getCmp("CustomerQuotationListPending") : Wtf.getCmp("assetVendorQuotationListPending");
            } else {
                panel = this.isCustomer? Wtf.getCmp("CustomerQuotationListPending") : Wtf.getCmp("VendorQuotationListPending");
            }
        } else {
            panel = this.isCustomer? Wtf.getCmp("InvoiceListPending") : Wtf.getCmp("GRListPending");
        }
           
  
        if(panel==null){
            if(this.isRequisition) {
                if (this.isFixedAsset) {
                    panel = getFixedAssetPRTab(false, "assetPurchaseRequisitionListPending", "Asset Pending Approval PR(s)", undefined, false, true, true);
                } else {
                    panel = getPRTab(false, "PurchaseRequisitionListPending", "Pending Approval PR(s)", undefined, false, true);
                }
            } else if(this.isCustomer){
                if(this.isQuotation){
                    panel = getQouteTab(false, "CustomerQuotationListPending", "Pending Approval CQ(s)", undefined, false, true,false,false,true);
                }else if(this.isOrder) {
                        panel = getSOTab(false, "SalesOrderListPending", "Pending Approval SO(s)", undefined, false, true);
                }else {
                    panel = getInvoiceTab(false, "InvoiceListPending", "Pending Approval CI(s)", undefined, false, false, true,undefined,undefined,this.moduleid);
                }
            } else {
                if(this.isQuotation){
                    if (this.isFixedAsset) {
                        panel = getFixedAssetQouteTab(false, "assetVendorQuotationListPending", "Asset Pending Approval VQ(s)", undefined, false ,false,false,false,true,true);
                    } else {
                        panel = getQouteTab(false, "VendorQuotationListPending", WtfGlobal.getLocaleText("acc.invoicelist.PendingApprovalVQ(s)"), undefined, false ,false,false,false,true);
                    }
                } else if(this.isOrder) {
                    if (this.isFixedAsset) {
                        panel = getFixedAssetPOTab(false, "assetPurchaseOrderListPending", "Pending Approval Asset PO(s)", undefined, false ,true,undefined,undefined,undefined,true)
                    }else{
                        panel = getPOTab(false, "PurchaseOrderListPending", "Pending Approval PO(s)", undefined, false ,true)
                    }
                }else {
                    panel = getVendorInvoiceTab(false, "GRListPending", "Pending Approval VI(s)", undefined, false, true,undefined,undefined,this.moduleid);
            }
            }
            Wtf.getCmp('as').add(panel);
        }
       
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
       
    }, this);
   
    this.viewDraftedBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.viewDrafts"),
        tooltip: WtfGlobal.getLocaleText("acc.field.viewDrafts"),
        id: 'draftedInvoice' + this.id,
        scope: this,
        hidden: !(this.moduleid == Wtf.Acc_Purchase_Order_ModuleId||this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId||this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid === Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId)||this.isLeaseFixedAsset,
        iconCls: getButtonIconCls(Wtf.etype.reorderreport)
    });
   
    this.viewDraftedBttn.on('click',this.openDraftedDocumentTab, this);
   
    this.archieveBttn.on('click',this.handleArchieveQuate,this);
    this.unArchieveBttn.on('click',this.handleUnArchieveQuate,this);
    
    /*
     * Generate the IBG file
     */
    this.generateIBGBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.uob.generateIBGFile"),    
        scope: this, 
        hidden:!(this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.account.companyAccountPref.activateIBGCollection),
        tooltip: WtfGlobal.getLocaleText("acc.uob.generateIBGFile"),     
        iconCls:getButtonIconCls(Wtf.etype.reorderreport)
    });
    this.generateIBGBtn.on('click',this.validateDataBeforeGeneratingIBGFile,this);
    /*
     * History of IBG file generation
     */
    this.IBGHistoryBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.uob.viewGiroFileGenertedHistory"),    
        scope: this, 
        hidden:!(this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.account.companyAccountPref.activateIBGCollection),
        tooltip: WtfGlobal.getLocaleText("acc.uob.viewGiroFileGenertedHistory"),     
        iconCls: "accountingbase reportsBtnIcon"
    });
    this.IBGHistoryBtn.on('click',this.callIBGHistoryReport,this);
    /*
     * Reverting the IBG generation status of invoice
     */
    this.revertGiroGeneratedStatusBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.uob.revertFileGenerationStatus"),    
        scope: this, 
        hidden: !(this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.account.companyAccountPref.activateIBGCollection),
        tooltip: WtfGlobal.getLocaleText("acc.uob.revertFileGenerationStatus"),     
        iconCls:getButtonIconCls(Wtf.etype.reorderreport)
    });
    this.revertGiroGeneratedStatusBtn.on('click',this.revertGiroGeneratedStatus,this);
    this.archieveType = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data :[['0',WtfGlobal.getLocaleText("acc.quote.quotation")],
            ['1',WtfGlobal.getLocaleText("acc.quote.archieved")],
            ['2',WtfGlobal.getLocaleText("acc.quote.allquote")],
            ['3',WtfGlobal.getLocaleText("acc.field.FavouriteQuotations")],
            ['4',WtfGlobal.getLocaleText("acc.field.CQWithoutDOInv")],
            ['5',WtfGlobal.getLocaleText("acc.field.CQWithoutDOWithInv")],
            ['6',WtfGlobal.getLocaleText("acc.field.CQWithDOWithoutInv")],
            ['7',WtfGlobal.getLocaleText("acc.field.CQWithDOWithInv")]]
            
    });

    this.archieveCombo = new Wtf.form.ComboBox({
        store: this.archieveType,
        fieldLabel:WtfGlobal.getLocaleText("acc.common.view"),
        name:'archievecmb',
        displayField:'name',
        value:'0',
        hidden : this.isRequisition || !this.isQuotation || (this.isFixedAsset && this.moduleId != Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) || this.isLeaseFixedAsset || this.isConsignment,
        editable:false,
        allowBlank:false,
        anchor:"80%",
        valueField:'id',
        mode: 'local',
        triggerAction: 'all'
    });
   
    this.archieveCombo.on("select", function(combo, rec, index){
        this.archiveFlag=rec.data.id;
        this.isfavourite=false;
        if(this.archiveFlag == 3){
            this.isfavourite=true;
        }
        this.customerQuotationsWithInvoiceAndDOStatus=0;
        
        if (this.archiveFlag == "4" ) {
            this.customerQuotationsWithInvoiceAndDOStatus=4;
        }
        if( this.archiveFlag == "5"){
           this.customerQuotationsWithInvoiceAndDOStatus=5;  
        }
        if(this.archiveFlag == "6"){
          this.customerQuotationsWithInvoiceAndDOStatus=6;   
        }
         if(this.archiveFlag == "7"){
          this.customerQuotationsWithInvoiceAndDOStatus=7;   
        }
         
        this.loadStore();
    },this);
   
    var btnArr=[];
    var gridBtnArr=[]; //ERP-12456 [SJ]
    var bottombtnArr=[];
    var tranType=null;
    if(this.isCustBill)
        tranType=config.isCustomer?(config.isOrder?Wtf.autoNum.BillingSalesOrder:Wtf.autoNum.BillingInvoice):(config.isOrder?Wtf.autoNum.BillingPurchaseOrder:Wtf.autoNum.BillingGoodsReceipt);
    else
        tranType=this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId?Wtf.autoNum.securityGateEntry:config.isCustomer?(config.isOrder?Wtf.autoNum.SalesOrder:Wtf.autoNum.Invoice):(config.isOrder?Wtf.autoNum.PurchaseOrder:Wtf.autoNum.GoodsReceipt);
    if(this.isQuotation) {
        tranType= this.isCustomer ? Wtf.autoNum.Quotation : Wtf.autoNum.Venquotation;
    } else if(this.isRequisition) {
        tranType= Wtf.autoNum.Requisition;
    } else if(this.isRFQ) {
        tranType= Wtf.autoNum.RFQ;
    }
       

    var singlePDFtext = null;
    if(this.isQuotation)
        singlePDFtext = WtfGlobal.getLocaleText("acc.accPref.autoQN");
    else
        singlePDFtext = config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoSO"):WtfGlobal.getLocaleText("acc.accPref.autoInvoice")):(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoPO"):WtfGlobal.getLocaleText("acc.accPref.autoVI"));

if(config.extraFilters == undefined){//Cost Center Report View - Don't show Buttons
    
    /*Below method is used to get Edit Button Tool Tip as per Module ID*/
   var toolTipMsg=this.getEditButtonToolTipMsg(this.moduleid);
   this.moduleidForDraft = false;
   if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId){
       this.moduleidForDraft = true;    //  This flag used to allow Edit option for Drafted Documents. ERP-42026
   }
   if((!WtfGlobal.EnableDisable(this.uPermType, this.editPermType) && (Wtf.account.companyAccountPref.editTransaction||this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId||this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId) && ((this.pendingapproval && (this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_Purchase_Order_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId)) || !this.pendingapproval || this.isRequisition)) || (this.isDraft && this.moduleidForDraft)){				//!this.isOrder&&
        btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
                tooltip :toolTipMsg,
                id: 'btnEdit' + this.id,
                scope: this,
                hidden:(this.pendingapproval && (this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_Purchase_Order_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId)) ? false : this.pendingapproval || config.consolidateFlag ||this.isSalesCommissionStmt ||  this.reportbtnshwFlag,//this.isRFQ ||
                iconCls :getButtonIconCls(Wtf.etype.edit),
                disabled :true
        }));
        this.editBttn.on('click',(this.isOrder && !this.isRequisition)?this.editOrderTransaction.createDelegate(this,[false]):this.editTransactionCheckBeforeNew.createDelegate(this,[false]),this);
//        this.editBttn.on('click',this.editTransaction.createDelegate(this,[false]),this);
   }
   if(!WtfGlobal.EnableDisable(this.uPermType, this.copyPermType) && !this.pendingapproval){
        btnArr.push(this.copyInvBttn=new Wtf.Toolbar.Button({
            text:(this.isOrder || this.isQuotation || this.isRFQ)?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.invoiceList.copyInv"),  //'Copy Invoice',
            tooltip :(this.isOrder || this.isQuotation || this.isRFQ)?WtfGlobal.getLocaleText("acc.field.CopyRecord"):WtfGlobal.getLocaleText("acc.invoiceList.copyInvTT"),  //'Allows you to Copy Invoice.',
            id: 'btnCopyInv' + this.id,
            scope: this,
            hidden: this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId||this.isLeaseFixedAsset || config.consolidateFlag ||this.isSalesCommissionStmt ||  this.reportbtnshwFlag || this.isConsignment,//this.isCustBill || this.isRequisition || this.isFixedAsset|| this.isRFQ
            iconCls :getButtonIconCls(Wtf.etype.copy),
            disabled :true
        }));
        this.copyInvBttn.on('click',(this.isOrder?this.editOrderTransaction.createDelegate(this,[true]):this.editTransaction.createDelegate(this,[true])),this);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, Wtf.Perm.consignmentsales.deletesalesconreq)){
        btnArr.push(this.freezeBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.closebtnn"),  //'Edit',
                tooltip :WtfGlobal.getLocaleText("acc.invoiceList.closeRequest"),
                id: 'btnFreez' + this.id,
                scope: this,
                hidden:this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId?false:true,
                iconCls :getButtonIconCls(Wtf.etype.deletebutton),
                disabled :true,
                handler:this.handleConsignmentRequestClose.createDelegate(this,this.del=["del"])
        }));     
    }
   var deletebtnArray=[];
   if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction && !this.pendingapproval){
        deletebtnArray.push(this.deleteTrans=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.rem.7")+' '+this.label,
            scope: this,
            //hidden:config.isOrder,
            tooltip:WtfGlobal.getLocaleText("acc.rem.6"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            hidden: config.consolidateFlag ||this.isSalesCommissionStmt,
            disabled :true,
            handler:this.deleteTransactionCheckBefore.createDelegate(this,this.del=["del"])
       }))
   }
      if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction){
        deletebtnArray.push(this.deleteTransperm=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.rem.7")+' '+this.label+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),
            scope: this,
            //hidden:config.isOrder,
            tooltip:WtfGlobal.getLocaleText("acc.rem.6")+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            hidden: ((config.consolidateFlag || this.isSalesCommissionStmt) || (Wtf.account.companyAccountPref.avalaraIntegration && (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId))),//Disbale permanent deletion of sales invoice in case of Avalara Integration
            disabled :true,
            handler:this.deleteTransactionCheckBefore.createDelegate(this,this.del=["delp"])
       }))
   }
   if(deletebtnArray.length>0) {
        btnArr.push(this.deleteMenu = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            hidden: this.reportbtnshwFlag,
            menu:deletebtnArray
       }));
  }
  
        if ((!WtfGlobal.EnableDisable(this.uPermType, this.permType.unlinksidoc) && this.moduleid == Wtf.Acc_Invoice_ModuleId) || (!WtfGlobal.EnableDisable(this.uPermType, this.permType.unlinkpidoc) && this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) || (!WtfGlobal.EnableDisable(this.uPermType, this.permType.unlinkvqdoc) && this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId) || (!WtfGlobal.EnableDisable(this.uPermType, this.permType.unlinkcqdoc) && this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)
            || (!WtfGlobal.EnableDisable(this.uPermType, this.permType.unlinkpodoc) && this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) || (!WtfGlobal.EnableDisable(this.uPermType, this.permType.unlinksodoc) && this.moduleid == Wtf.Acc_Sales_Order_ModuleId)) {
            btnArr.push(this.unlinkDocumentBtn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.unlink"), // "Unlink",
                tooltip: WtfGlobal.getLocaleText("acc.field.unlink"), // "Unlink",
                id: 'unlinkDocument' + this.id,
                scope: this,
                hidden: this.pendingapproval || this.reportbtnshwFlag,
                iconCls: 'accountingbase pricelistbutton',
                disabled: true,
                handler: this.handleUnlinkDocument
            }));
        }
  
          /* Button in PO & SO report to Disable PO for SO or vice versa*/
        if ((!WtfGlobal.EnableDisable(this.uPermType, this.editPermType)) && Wtf.account.companyAccountPref.editTransaction &&(this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId)){

            btnArr.push(this.blockunblockbtn = new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.invoiceList.blockdocuments"),
                tooltip: WtfGlobal.getLocaleText("acc.invoiceList.blockdocumentscross"), 
                id: 'disabledsoforpo' + this.id,
                scope: this,
                iconCls: 'accountingbase pricelistbutton',
                disabled: true,
                hidden:this.isfromReportList,//true if outstanding SO/PO Report is Clicked from Reportlist 
                handler: this.disableSOorPO
            }));
        }
        
        /* Button in PO & SO report for closing document Manually
         * 
         * same user permission is assigned as Edit button
         * */
           
        if ((!WtfGlobal.EnableDisable(this.uPermType, this.editPermType)) && Wtf.account.companyAccountPref.editTransaction && (this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId)){

            btnArr.push(this.closedocumentbtn = new Wtf.Toolbar.Button({
                text:this.moduleid==Wtf.Acc_Sales_Order_ModuleId ?WtfGlobal.getLocaleText("acc.invoiceList.soclosedManually"):WtfGlobal.getLocaleText("acc.invoiceList.poclosedManually"),
                tooltip: WtfGlobal.getLocaleText("acc.invoiceList.closeDocumentManually"), 
                id: 'disabledsoforpo' + this.id,
                scope: this,
                iconCls: 'accountingbase pricelistbutton',
                disabled: true,
                hidden:this.isfromReportList,//true if outstanding SO/PO Report is Clicked from Reportlist 
                handler: this.closeDocument
            }));
            if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId ){
            btnArr.push(
            this.linkAdvancePaymentbtn = new Wtf.Toolbar.Button({
                text:this.moduleid==Wtf.Acc_Sales_Order_ModuleId ?WtfGlobal.getLocaleText("acc.invoiceList.linkAdvancePayment"):WtfGlobal.getLocaleText("acc.invoiceList.poclosedManually"),
                tooltip: WtfGlobal.getLocaleText("acc.invoiceList.linkAdvancePayment"), 
                id: 'disabledsoforpo' + this.id,
                scope: this,
                iconCls: 'accountingbase pricelistbutton',
                disabled: true,
                handler: this.linkAdvancePayment
            }));
        }
        }
          
        if ((!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType) ||((this.moduleId == Wtf.Acc_FixedAssets_RFQ_ModuleId) ||(this.moduleId==Wtf.Acc_Purchase_Requisition_ModuleId) || (this.moduleId == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) || (this.moduleId == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId)||config.isLeaseFixedAsset||this.isFixedAsset  || this.isConsignment))) {
            btnArr.push(this.email=new Wtf.Toolbar.Button({//ERP-12456 [SJ]
                text:WtfGlobal.getLocaleText("acc.common.email"),  // "Email",
                tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
                scope: this,
                iconCls : "accountingbase financialreport",
                disabled : true,
                hidden: config.consolidateFlag ||this.isSalesCommissionStmt || this.reportbtnshwFlag,
                handler : this.sendMail
            }));
        }
        
        if (Wtf.account.companyAccountPref.deliveryPlanner && this.moduleid == Wtf.Acc_Invoice_ModuleId) {
            btnArr.push(this.viewDeliveryPlannerBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.viewDeliveryPlanner"), // "View Delivery Planner",
                tooltip: WtfGlobal.getLocaleText("acc.field.viewDeliveryPlanner"),
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.reorderreport),
                disabled: true,
                handler: this.handleViewDeliveryPlanner
            }));
        }
        
   if(this.isRequisition && !this.pendingapproval) {
       if(!this.isDraft && ((!WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.createrfq)&&!this.isFixedAsset) ||(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereq, Wtf.Perm.assetpurchasereq.createarfq)&&this.isFixedAsset))) {
            btnArr.push(this.RFQBtn=new Wtf.Toolbar.Button({
                 text: WtfGlobal.getLocaleText("acc.field.InitiateRFQ"),
                 scope: this,
                 //hidden:config.isOrder,
                 tooltip:WtfGlobal.getLocaleText("acc.field.InitiateRFQ"),
                 disabled :!Wtf.account.companyAccountPref.isPRmandatory?false:true,
                 iconCls:'accountingbase initiaterfq',
                 handler: function() {
                     if (this.isFixedAsset) {
                         callFixedAssetRequestForQuotation(false,this.PR_IDS, this.PR_MEMOS, this.isFixedAsset);
                     } else {
                         callRequestForQuotation(false,this.PR_IDS, this.PR_MEMOS);
                     }
                 }
            }))
       }
       if(!this.isDraft && ((!WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.viewrfq)&&!this.isFixedAsset)||(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereq, Wtf.Perm.assetpurchasereq.viewarfq)&&this.isFixedAsset))) {
            btnArr.push(this.RFQListBtn=new Wtf.Toolbar.Button({
                 text: WtfGlobal.getLocaleText("acc.field.RFQList"),
                 scope: this,
                 //hidden:config.isOrder,
                 tooltip:WtfGlobal.getLocaleText("acc.field.RFQList"),
                 iconCls:'accountingbase rfqlist',
                 enabled :true,
                 handler: function() {
                     if (this.isFixedAsset) {
                         callFixedAssetReqForQuotationList(false,this.PR_IDS);
                     } else {
                         callReqForQuotationList(false,this.PR_IDS);
                     }
                 }
            }))
       }
       if(!this.isDraft && ((!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createquotation)||this.isFixedAsset))) {
            btnArr.push(this.RFQVendorBtn=new Wtf.Toolbar.Button({
                 text: WtfGlobal.getLocaleText("acc.field.RecordVendorQuotation"),
                 scope: this,
                 //hidden:config.isOrder,
                 tooltip:WtfGlobal.getLocaleText("acc.field.RecordVendorQuotation"),
                 iconCls:'accountingbase purchaseorder',
                 disabled :true,
                 handler: function() {
                     if (this.isFixedAsset) {
                         var arr = this.grid.getSelectionModel().getSelections();
                         var isClosed = false;
                         var linkedTransaction = "";
                         for (var cnt=0; cnt<arr.length; cnt++) {
                             if (arr[cnt] && arr[cnt].data.closeStatus == "Closed") {
                                 isClosed = true;
                                 linkedTransaction += arr[cnt].data.billno + ", ";
                             }
                         }
                        
                         if (isClosed) {
                             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.assetPurchaseRequisition")+"(s) " + linkedTransaction.substring(0, linkedTransaction.length - 3) + " " + WtfGlobal.getLocaleText("acc.field.recordForQuotationClosedMsg")],2);
                             return;
                         }else {
                             callFixedAssetVendorQuotation(true, 'assetvendorquotation', this.grid.getSelectionModel().getSelected(), undefined, this.PR_IDS,true);
                         }
                     } else {
                         callVendorQuotation(true, 'vendorquotation', this.grid.getSelectionModel().getSelected(), undefined, this.PR_IDS,true);
                     }
                 }
            }))
       }
   }
    btnArr.push(this.archieveBttn);
    btnArr.push(this.unArchieveBttn);
   
    if(this.pendingapproval) {
            btnArr.push(this.approveInvoiceBttn);
            btnArr.push(this.rejectInvoiceBttn);
        } else {
            btnArr.push(this.pendingApprovalBttn);
//        var addFlag = false;
//        if(this.isOrder) {
//            if((this.isCustomer && (!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.salesorderapprovelevelone) || !WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.salesorderapproveleveltwo)))
//                || (!this.isCustomer && (!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.purchaseorderapprovelevelone) || !WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.purchaseorderapproveleveltwo))) ){
//                addFlag = true;
//            }
//        } else {
//            if((this.isCustomer && (!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.customerinvoiceapprovelevelone) || !WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.customerinvoiceapproveleveltwo)))
//                || (!this.isCustomer && (!WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.vendorinvoiceapprovelevelone) || !WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.vendorinvoiceapproveleveltwo))) ){
//                addFlag = true;
//            }
//        }
//        if(addFlag || this.isRequisition) {
//                btnArr.push(this.pendingApprovalBttn);
//    }
    }
   
    if ((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId ||this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid === Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId )&&  !this.isDraft) {
        btnArr.push(this.viewDraftedBttn);
    }
    btnArr.push(this.generateIBGBtn);
    btnArr.push(this.IBGHistoryBtn);
     // disable 'View Pending Approval' if user is selecting 'Outstanding PO(s)' option
    if (this.isOutstanding){
        this.pendingApprovalBttn.disable();
    }else{
        this.pendingApprovalBttn.enable();
    }
   
    this.operationType = tranType;
    if(this.isRFQ || this.operationType==Wtf.autoNum.Invoice || this.operationType==Wtf.autoNum.GoodsReceipt || this.operationType==Wtf.autoNum.BillingInvoice || this.operationType==Wtf.autoNum.BillingGoodsReceipt || this.operationType==Wtf.autoNum.Quotation || this.operationType==Wtf.autoNum.Venquotation || this.operationType==Wtf.autoNum.SalesOrder || this.operationType==Wtf.autoNum.PurchaseOrder) {
        var bText = "";
        var bToolTip = "";
        this.isVendorPayment = false;
        if(this.operationType==Wtf.autoNum.Invoice || this.operationType==Wtf.autoNum.BillingInvoice){
            bText = WtfGlobal.getLocaleText("acc.invoiceList.recPay");  //"Receive Payment";
            bToolTip = WtfGlobal.getLocaleText("acc.invoiceList.recPayTT");  //"Allows you to Receive Payment for invoice.";
        }else if(this.operationType==Wtf.autoNum.GoodsReceipt || this.operationType==Wtf.autoNum.BillingGoodsReceipt){
            bText = WtfGlobal.getLocaleText("acc.invoiceList.mP");  //"Make Payment";
            bToolTip = WtfGlobal.getLocaleText("acc.invoiceList.mPTT");  //"Allows you to Make Payment for vendor invoice.";
        }
     if(!this.isRFQ && !this.isQuotation&&!this.isOrder&&!WtfGlobal.EnableDisable(this.uPaymentPermType, this.createPaymentPermType) && !this.pendingapproval){
//            btnArr.push(this.paymentButton=new Wtf.Toolbar.Button({
//                text: bText,
//                tooltip : bToolTip,
//                scope: this,
//                iconCls : "accountingbase financialreport",
//                disabled : true,
//                hidden: config.consolidateFlag ||this.isSalesCommissionStmt || this.isFixedAsset || this.isLeaseFixedAsset || this.reportbtnshwFlag || this.isConsignment,
//                handler : this.makePayment
//            }));
            btnArr.push(this.GIROFileButton=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.GenerateGIROFile"),
                tooltip : WtfGlobal.getLocaleText("acc.field.GenerateGIROFile"),
                scope: this,
                iconCls : "accountingbase financialreport",
                disabled : true,
                hidden: true,//!this.isVendorPayment && (config.consolidateFlag ||this.isSalesCommissionStmt),
                handler : this.generateGIROFile
            }));
        }
//   if (!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType) || (this.moduleId == Wtf.Acc_FixedAssets_RFQ_ModuleId) || (this.moduleId == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) || (this.moduleId == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId)||config.isLeaseFixedAsset||this.isFixedAsset  || this.isConsignment) {
//         gridBtnArr.push(this.email=new Wtf.Toolbar.Button({//ERP-12456 [SJ]
//                text:WtfGlobal.getLocaleText("acc.common.email"),  // "Email",
//                tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
//                scope: this,
//                iconCls : "accountingbase financialreport",
//                disabled : true,
//                hidden: config.consolidateFlag ||this.isSalesCommissionStmt || this.reportbtnshwFlag,
//                handler : this.sendMail
//        }));
//    }
//	if(this.isRFQ)
//           this.email.hidden = false;
    }
        var rbText = "";
        var rbToolTip = "";
        if(this.isOrder && !this.isLeaseFixedAsset && !this.isConsignment){
            rbText = WtfGlobal.getLocaleText("acc.field.SetRecurringSO");  //repeated so
            rbToolTip = WtfGlobal.getLocaleText("acc.field.SetRecurringSO");  //repeated SO
        }else if(this.isOrder && this.isLeaseFixedAsset && !this.isConsignment){
            rbText = WtfGlobal.getLocaleText("acc.field.SetRecurringLO");  //repeated Lo
            rbToolTip = WtfGlobal.getLocaleText("acc.field.SetRecurringLO");  //repeated LO
        }else if(this.isOrder && !this.isLeaseFixedAsset && this.isConsignment){
            rbText = WtfGlobal.getLocaleText("acc.field.SetRecurringCR");  //repeated CR
            rbToolTip = WtfGlobal.getLocaleText("acc.field.SetRecurringCR");  //repeated CR
        }else {
            rbText =WtfGlobal.getLocaleText("acc.invoiceList.recInv");  // "Recurring Invoice",
            rbToolTip =WtfGlobal.getLocaleText("acc.invoiceList.recInv");  // "Recurring Invoice",
        }

     if(this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId ) {
        if((!WtfGlobal.EnableDisable(this.uPermType, this.recurringPermType) && !this.pendingapproval) ||config.isLeaseFixedAsset||this.isFixedAsset){
            gridBtnArr.push(this.RepeateInvoice=new Wtf.Toolbar.Button({//ERP-12456 [SJ]
                text:rbText,
                tooltip :rbToolTip,
                scope: this,
                iconCls : getButtonIconCls(Wtf.etype.copy),
                disabled : true,
                moduleid : this.moduleid,
                hidden: config.consolidateFlag ||this.isSalesCommissionStmt || this.isFixedAsset || this.reportbtnshwFlag || this.isConsignment || this.isDraft,
                handler : this.repeateInvoiceHandler
            }));
        }
    }
}
  this.newtranType=tranType;
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        this.exportButton=new Wtf.exportButton({
            obj:this,
            isEntrylevel:false,
            id:"exportReports"+config.helpmodeid+this.id,   //+config.id,   //added this.id to avoid dislocation of Export button option i.e. CSV & PDF
            text: WtfGlobal.getLocaleText("acc.common.exportList"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            usePostMethod:(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId)?true:false,
            filename : this.filename,
//            excludeCustomHeaders:this.moduleId==2?true:false,
            excludeCustomHeaders:false,     //SDP-13448 - Show Custom Fields Header in Export Excel (Details) for SI
            moduleId:this.moduleId,
//            menuItem:{csv:true,pdf:true,rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
            menuItem:{
                csv:true,
                pdf: (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId)? false : true, // For Customer Invoice Submenu: true and pdf: false
                xls:true,
                print:true,
                subMenu: (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId)? true : false, // For Customer Invoice Submenu: true and pdf: false
//                summaryPDF:(Wtf.templateflag == Wtf.Monzone_templateflag && this.moduleid == Wtf.Acc_Invoice_ModuleId)?true:false,
                detailPDF:(Wtf.templateflag == Wtf.Monzone_templateflag && (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId))?true:(this.moduleId==Wtf.Acc_Sales_Order_ModuleId)?true:false,
                detailedXls:(this.moduleid == Wtf.Acc_Purchase_Order_ModuleId ||this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId||this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId||this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId||this.moduleid == Wtf.Acc_Sales_Order_ModuleId||this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId ||this.moduleid == Wtf.Acc_RFQ_ModuleId)?true:false
            },  //Currently, Export to Excel available only in Purchase Order
            get:tranType  // Mayur B - Also we have added the export Customer Register functionality for monzone. hence added two separate buttons to export detail and summaries view of template.
          });
         
          this.exportButton.setParams({
            costCenterId : this.costCenter.getValue(),
            productid : this.productname.getValue(),
            newcustomerid : this.custmerCmb.getValue(),
            newvendorid : this.vendorCMB.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            archieve : this.archieveCombo?this.archieveCombo.getValue() : 0
         });
         
             }
             
    if ((this.moduleid == Wtf.Acc_Invoice_ModuleId || (this.moduleid == Wtf.Acc_Sales_Order_ModuleId && !this.isMRPSalesOrder) || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId) && !this.isfromReportList) {
        var extraConfig = this.getImportExtraConfigForModule(this.moduleid);
        var extraParams = this.getImportExtraParamsForModule(this.moduleid);
        var importBtnArray = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(this.moduleid), this.Store, extraParams, extraConfig);
        var importButton = Wtf.documentImportMenuButtonA(importBtnArray, this, this.getModuleNameForImport(this.moduleid));

        if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.importsalesinvoice) && this.moduleid == Wtf.Acc_Invoice_ModuleId) { // For Customer Invoice and Cash Sales Import
            this.createImportButtonsForInvoice(importBtnArray, bottombtnArr);
        } else if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) { // For Vendor Invoice and Cash Purchase Import
            this.createImportButtonsForVendorInvoice(importBtnArray, bottombtnArr);
        } else { // For SO, PI, CQ modules
            bottombtnArr.push(importButton);
        }
    }
                   
//     if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
//          this.printButton=new Wtf.exportButton({
//            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
//            obj:this,
//            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
//            disabled :true,
//            filename : this.fileNameForPrint,
//            excludeCustomHeaders:this.moduleId==2?true:false,
//            moduleId:this.moduleId,
//            label:(this.isSalesCommissionStmt)?WtfGlobal.getLocaleText("acc.field.SalesCommissionStatement"):config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoSO"):WtfGlobal.getLocaleText("acc.accPref.autoInvoice")):(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoPO"):WtfGlobal.getLocaleText("acc.agedPay.venInv")),
//            params:{isexpenseinv:this.isexpenseinv,name:(this.isSalesCommissionStmt)?"Sales Commission Statement":(config.isQuotation?WtfGlobal.getLocaleText("acc.qnList.tabTitle"):(config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.soList.tabTitle"):WtfGlobal.getLocaleText("acc.invoiceList.tabtitle")):(config.isOrder?WtfGlobal.getLocaleText("acc.poList.tabTitle"):WtfGlobal.getLocaleText("acc.grList.tabTitle"))))},
//            menuItem:{print:true},
//            get:tranType
//          });
//         
//          this.printButton.setParams({
//            costCenterId : this.costCenter.getValue(),
//            productid : this.productname.getValue(),
//            newcustomerid : this.custmerCmb.getValue(),
//            newvendorid : this.vendorCMB.getValue(),
//            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
//         });
//     }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType) || !WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        this.singlePrint=new Wtf.exportButton({
            obj:this,
            id:"printReports"+config.helpmodeid+config.id,
            iconCls: 'pwnd printButtonIcon',
            text: WtfGlobal.getLocaleText("acc.rem.236"),// + " "+ singlePDFtext,
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Export selected record(s)'
            disabled :true,
            filename : this.filename,
            hidden:this.isSalesCommissionStmt||this.moduleid == Wtf.Acc_Security_Gate_Entry_ModuleId,
            menuItem:{
//                rowPdf:(this.isSalesCommissionStmt)?false:true,
                rowPdfPrint:(this.isSalesCommissionStmt)?false:true,
                rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext
                },
            get:tranType,
            moduleid:config.moduleId
        });
    
        this.singlePrint.setParams({
            costCenterId : this.costCenter.getValue(),
            productid : this.productname.getValue(),
            newcustomerid : this.custmerCmb.getValue(),
            newvendorid : this.vendorCMB.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            archieve : this.archieveCombo?this.archieveCombo.getValue() : 0
        });
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
//     this.singleRowPrint=new Wtf.exportButton({
//         obj:this,
//         id:"printSingleRecord"+config.helpmodeid+config.id,
//         iconCls: 'pwnd printButtonIcon',
//         text: WtfGlobal.getLocaleText("acc.rem.236"),
//         tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
//         disabled :true,
//         hidden:this.isSalesCommissionStmt||this.moduleid == Wtf.Acc_Security_Gate_Entry_ModuleId,
//         menuItem:{rowPrint:(this.isSalesCommissionStmt)?false:true},
//         get:tranType,
//         moduleid:config.moduleId
//     });
        // Job Order Flow print button
     this.jobOrderPrint=new Wtf.exportButton({
         obj:this,
         id:"printJobOrderRecord"+config.helpmodeid+config.id,
         iconCls: 'pwnd printButtonIcon',
         text: WtfGlobal.getLocaleText("acc.jobOrderFlow.printButton"),
         tooltip :WtfGlobal.getLocaleText("acc.jobOrderFlow.printButton.ttip"),  //'Print Selected Records'
         disabled :true,
         hidden:this.isSalesCommissionStmt,
         menuItem:{rowPrint:(this.isSalesCommissionStmt)?false:true},
         jobOrderFlow:Wtf.account.companyAccountPref.jobOrderItemFlow,
         get:tranType,
         moduleid:config.moduleId
     });
    
//     this.singleRowPrint.setParams({
//            costCenterId : this.costCenter.getValue(),
//            productid : this.productname.getValue(),
//            newcustomerid : this.custmerCmb.getValue(),
//            newvendorid : this.vendorCMB.getValue(),
//            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
//            archieve : this.archieveCombo?this.archieveCombo.getValue() : 0
//     });
    }
   
    /* 
     * Below code is for to auto refresh the grid view when print flag updated.
     * */
//    if(this.singleRowPrint!=undefined){
//        for (var i = 0; i < this.singleRowPrint.printMenu.items.items.length; i++) {
//            this.singleRowPrint.printMenu.items.items[i].on('click', function() {
//                var gridSelections = this.grid.getSelectionModel().getSelections();
//                 for(var gridRecCount=0;gridRecCount < gridSelections.length;gridRecCount++){
//                     gridSelections[gridRecCount].data.isprinted =true;
//                 }
//                this.grid.getView().refresh();
//            }, this);
//        }
//    }
    
    if((this.isOrder && this.businessPerson=="Customer" && !this.isCustBill && !this.pendingapproval && !this.isLeaseFixedAsset && !this.isConsignment&&this.moduleid != Wtf.Acc_Security_Gate_Entry_ModuleId)){				//!this.isOrder&&
        btnArr.push(this.POfromSOBttn=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.invoiceList.gpo"),         //Generate PO
                tooltip :WtfGlobal.getLocaleText("acc.invoiceList.gpott"),  //Generate PO for selected SO.
                scope: this,               
                iconCls :getButtonIconCls(Wtf.etype.edit),               
                disabled :true,
                hidden: config.consolidateFlag ||this.isSalesCommissionStmt || this.reportbtnshwFlag || this.isJobWorkOrderReciever,
                handler : this.createPOfromSO               
        }));   
         if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId ){				//!this.isOrder&&
        btnArr.push(this.SIfromSOBttn=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.invoiceList.gSI"),         //Generate PO
                tooltip :WtfGlobal.getLocaleText("acc.invoiceList.gSItt"),  //Generate PO for selected SO.
                scope: this,               
                iconCls :getButtonIconCls(Wtf.etype.edit),               
                disabled :true,
                hidden: config.consolidateFlag ||this.isSalesCommissionStmt || this.reportbtnshwFlag || this.isJobWorkOrderReciever,
                handler : this.createSIfromSO               
        })); 
//        this.POfromSOBttn.on('click',this.createPOfromSO,this);
//        this.editBttn.on('click',this.editTransaction.createDelegate(this,[false]),this);
  }
}
    if(this.id!="PurchaseRequisitionList" && (this.isOrder && this.businessPerson=="Vendor" && !this.isCustBill && !this.pendingapproval) && !this.isFixedAsset&&this.moduleid != Wtf.Acc_Security_Gate_Entry_ModuleId){//!this.isOrder&&
        btnArr.push(this.SOfromPOBttn=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.invoiceList.gso"),         //Generate SO
                tooltip :WtfGlobal.getLocaleText("acc.invoiceList.gsott"),  //Generate SO for selected PO.
                scope: this,               
                iconCls :getButtonIconCls(Wtf.etype.edit),               
                disabled :true,
                hidden: config.consolidateFlag ||this.isSalesCommissionStmt || this.reportbtnshwFlag || this.isConsignment,
                handler : this.createSOfromPO               
        }));
    }
    var searchByJenoStr='';
    if(!this.isRFQ && !this.isRequisition && !this.isQuotation 
            && this.moduleId != Wtf.Acc_FixedAssets_Purchase_Order_ModuleId
            && this.moduleId != Wtf.Acc_ConsignmentVendorRequest_ModuleId
            && this.moduleId != Wtf.Acc_Purchase_Order_ModuleId
            && this.moduleId != Wtf.Acc_Lease_Order
            && this.moduleId != Wtf.Acc_ConsignmentRequest_ModuleId
            && this.moduleId != Wtf.Acc_Sales_Order_ModuleId
            && this.moduleid!=Wtf.Acc_Security_Gate_Entry_ModuleId ){//do not show journal entry no in quick search in RFQ tab(purchase requisition.) and CQ/VQ/LQ and PO/SO/VReq/LO
        searchByJenoStr=" , "+WtfGlobal.getLocaleText("acc.cnList.gridJEno");
    }
    var searchFields = "";
    if(this.moduleId == Wtf.Acc_Invoice_ModuleId || this.moduleId == Wtf.Acc_Vendor_Invoice_ModuleId || 
       this.moduleId == Wtf.Acc_Purchase_Order_ModuleId || this.moduleId == Wtf.Acc_Sales_Order_ModuleId) {
        
        var person = "";
        if(this.moduleId == Wtf.Acc_Invoice_ModuleId || this.moduleId == Wtf.Acc_Sales_Order_ModuleId) {
            person = "Customer";
        } else {
            person = "Vendor";
        }
        searchFields=", Memo, "+person+" Name, "+person+" Alias Name, "
                +"Product Name, Product Id, "
                +"Billing Address, Billing City, Billing Postal, Billing State, Billing Email, Billing Country, "
                +"Shipping Address, Shipping City, Shipping Postal, Shipping State, Shipping Email, Shipping Country, Supplier Invoice No.";
    }
    if(this.moduleId == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleId == Wtf.Acc_Vendor_Quotation_ModuleId){
        var person = "";
        if(this.moduleId == Wtf.Acc_Customer_Quotation_ModuleId) {
            person = WtfGlobal.getLocaleText("acc.up.3");
            searchFields = ", "+WtfGlobal.getLocaleText("acc.cust.name");
        } else {
            person = WtfGlobal.getLocaleText("acc.up.4");
            searchFields = ", "+WtfGlobal.getLocaleText("acc.ven.name");
        }
        searchFields+=", "+ WtfGlobal.getLocaleText("acc.het.182")+", "+person+" "+WtfGlobal.getLocaleText("acc.address.AliasName")+", "
                + WtfGlobal.getLocaleText("acc.rem.prodName")+", "+ WtfGlobal.getLocaleText("acc.product.gridProductID");
    }
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" "+this.label+" No "+searchByJenoStr + searchFields,
        width: 150,
        id:"quickSearch"+config.helpmodeid+config.id,
        field: 'billno',
        Store:this.Store
    })
 this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden:this.moduleid == Wtf.Acc_Security_Gate_Entry_ModuleId?true:(this.moduleid !=undefined || (this.isSalesCommissionStmt != undefined && this.isSalesCommissionStmt) )?false:true ,       
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    /*
     * ERP-19906
     * Issue was to correct tooltip
     * As existing code with ternary operator is not readable properly, code updated to use if else statement.  
     * Following code is to get required tooltip as per module
     **/
    //this.newTabButton=getCreateNewButton(config.consolidateFlag,this,this.isQuotation?(this.isCustomer?WtfGlobal.getLocaleText("acc.WI.28"):WtfGlobal.getLocaleText("acc.WI.51")):(this.isRequisition?WtfGlobal.getLocaleText("acc.WI.52"):(this.isRFQ?(this.isFixedAsset?WtfGlobal.getLocaleText("acc.WoutI.45"):WtfGlobal.getLocaleText("acc.WoutI.createnewRFQ")):(this.isOrder?(this.isCustomer?WtfGlobal.getLocaleText("acc.WoutI.30"):WtfGlobal.getLocaleText("acc.WoutI.37")):(this.isCustomer?WtfGlobal.getLocaleText("acc.WoutI.43"):WtfGlobal.getLocaleText("acc.WoutI.createnewRFQ"))))),this.reportbtnshwFlag);
    var toolTipText = "";
    if (this.isQuotation) {
        if (this.isCustomer) {
            toolTipText = WtfGlobal.getLocaleText("acc.WI.28")
        } else {
            toolTipText = WtfGlobal.getLocaleText("acc.WI.51")
        }

    } else {
        if (this.isRequisition) {
            toolTipText = WtfGlobal.getLocaleText("acc.WI.52")
        } else {
            if (this.isRFQ) {
                if (this.isFixedAsset) {
                    toolTipText = WtfGlobal.getLocaleText("acc.WoutI.45")
                } else {
                    toolTipText = WtfGlobal.getLocaleText("acc.WoutI.createnewRFQ");
                }

            } else {
                if (this.isOrder) {
                    if (this.isCustomer) {
                        toolTipText = WtfGlobal.getLocaleText("acc.WoutI.30")
                    } else {
                        if(this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId){
                            toolTipText = WtfGlobal.getLocaleText("acc.securitygateentry.create")
                        }else{
                        toolTipText = WtfGlobal.getLocaleText("acc.WoutI.37")
                        }
                    }

                } else {
                    if (this.isCustomer) {
                        toolTipText = WtfGlobal.getLocaleText("acc.WoutI.43")
                    } else {
                        toolTipText = WtfGlobal.getLocaleText("acc.WoutI.44")
                    }

                }

            }

        }

    }
    
    
    this.newTabButton=getCreateNewButton(config.consolidateFlag,this,toolTipText,this.reportbtnshwFlag);
    
    this.newTabButton.on('click',this.openNewTab,this);
   
    if((this.isFixedAsset && ((this.moduleid != Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) && (this.moduleid != Wtf.Acc_FixedAssets_RFQ_ModuleId) && (this.moduleid != Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) && (this.moduleid != Wtf.Acc_FixedAssets_Purchase_Order_ModuleId))) || this.isLeaseFixedAsset || this.isConsignment){
        this.newTabButton.hide();
    }
    
    this.viewVersionHandler = "";
    var viewVersionFlag = false;
    if(this.isQuotation&&Wtf.account.companyAccountPref.versionslist){
        viewVersionFlag = true;
        this.viewVersionHandler = this.viewCustomerQuotationVersions;
    }else if(this.moduleid == Wtf.Acc_Purchase_Order_ModuleId && CompanyPreferenceChecks.activeVersioningInPurchaseOrder()){
        viewVersionFlag = true;
        this.viewVersionHandler = this.viewPurchaseOrderVersions;
    } 
     this.veiwVesions = new Wtf.Toolbar.Button({
        text:"View Versions", //
        scope: this,
        hidden:!viewVersionFlag,  
        disabled:true,
        tooltip: WtfGlobal.getLocaleText("acc.editor.viewVersionBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.viewVersionHandler,
        iconCls: "advanceSearchButton"
    });   
    
    //Button to Estimate Shipping Cost for a sales order with UPS Integration service
    //It is visible only and Sales Order report and only when UPS Integration is enabled
    this.estimateShippingCostBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.invoiceList.estimateTotalCost"),
        scope: this,
        hidden:!(this.moduleid==Wtf.Acc_Sales_Order_ModuleId && Wtf.account.companyAccountPref.upsIntegration),
        disabled : true,
        tooltip: WtfGlobal.getLocaleText("acc.invoiceList.estimateTotalCostTT"),
        handler: this.estimateShippingCost,
        iconCls: 'accountingbase fetch'
    });
   
  
     this.linkinfoViewBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.LinkInformationDetails"),     //button for showing link information
        scope: this,
        // || this.isQuotation || this.isRequisition 
        hidden:this.isSalesCommissionStmt || (this.isConsignment && this.moduleid==Wtf.Acc_Consignment_GoodsReceipt_ModuleId) || (this.isConsignment && this.moduleid== Wtf.Acc_ConsignmentInvoice_ModuleId)||this.moduleid == Wtf.Acc_Security_Gate_Entry_ModuleId,  //shown in SO/PO
        disabled : true,
        tooltip: WtfGlobal.getLocaleText("acc.field.LinkInformationDetails"),
        handler:function(){linkinfo(undefined,undefined,undefined,undefined,undefined,undefined,undefined,undefined,this)},
        iconCls:'accountingbase fetch'
    });
      
      this.relatedTransactionsBtn= new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.RelatedTransaction(s)"), //button for showing all related linking with particular document
        scope: this,
        hidden:this.isConsignment ||this.isFixedAsset ||this.isLeaseFixedAsset||this.moduleid == Wtf.Acc_Security_Gate_Entry_ModuleId,  
        disabled : true,
        tooltip: WtfGlobal.getLocaleText("acc.field.RelatedTransaction(s).tooltip"),
        handler:function(){
            linkPurchaseReportTab(this.isCustomer ? 1 :0,this.moduleId,this.grid.getSelectionModel().getSelected().data.billno);
        } , 
        iconCls:'accountingbase fetch'
    });
    
    this.agedDetailsBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.ageddetails"),     //button for showing Aged information
        scope: this, 
        hidden:!(this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId  || this.moduleid==Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId||this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId),  //shown in CI and VI
        disabled : true,
        tooltip: WtfGlobal.getLocaleText("acc.field.ageddetails"),     
        iconCls:'accountingbase fetch'
    });
    this.bulkReceive = new Wtf.Toolbar.Button({
        text: this.moduleid==Wtf.Acc_Invoice_ModuleId?WtfGlobal.getLocaleText("acc.invoice.bulkRecpaymentbuttontext"):WtfGlobal.getLocaleText("acc.invoice.bulkpaymentbuttontext"),     //button for showing Aged information
        scope: this, 
        disabled :this.isDraft,
        tooltip: this.moduleid==Wtf.Acc_Invoice_ModuleId?WtfGlobal.getLocaleText("acc.invoice.bulkRecpaymentbuttontext"):WtfGlobal.getLocaleText("acc.invoice.bulkpaymentbuttontext"),     //button for showing Aged information     
        iconCls: getButtonIconCls(Wtf.etype.add),
        handler:function(){
                  var panel = null;
        
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getSequenceFormatStore.do",
                params: {
                
                mode: this.isCustomer ? 'autoreceipt' :'autopayment',
                isEdit: this.isCopyReceipt ? false : this.isEdit
                }
            }, this, function(response) {
                if (response.data && response.data.length > 0) {
                 var selectArray = eval(this.getSelectedRecords());
                 var vendorArray =[];
                 var isSelectedSameVendor=true;
               
                /* --------- If sequence Format is Not set for MP/RP then not allow to create bulk payment grouped on different vendors/Customers-----------*/
                if (response.data[0].id == "NA" && selectArray.length>1) {
                    for(var selctedRecLength =0; selctedRecLength<selectArray.length; selctedRecLength++){
                      if(vendorArray.toString() =="" ){
                        vendorArray[0]=  selectArray[selctedRecLength].personid;
                        continue;
                      }
                        
                        if(vendorArray.indexOf(selectArray[selctedRecLength].personid) == -1){
                            isSelectedSameVendor=false;
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("Sequence format is not set for payment")], 2);
                        return; 
                      }  
                    }
                    if(isSelectedSameVendor){
                       this.bulkReceivePayment();  
                    }
                       
                    }else{
                          this.bulkReceivePayment();
                    }
                }

            }, function(response) {
                });
        }               
              
    });

    if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
        if (Wtf.istdsapplicable) {
            btnArr.push(this.TDSJE = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.TDSJEPost"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.TDSJEPost"),
                iconCls: getButtonIconCls(Wtf.etype.add),
                handler: function() {
                    this.postTDSTCS(Wtf.GSTJEType.TDS);
                }
            }));
        }
        if (Wtf.istcsapplicable) {
            btnArr.push(this.TCSJE = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.TCSJEPost"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.TCSJEPost"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function() {
                this.postTDSTCS(Wtf.GSTJEType.TCS);
            }
        }));
        }
    }
    btnArr.push(this.syncInvoices = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.InvoiceSyncFromLMS"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.field.InvoiceSyncFromLMSTT"),
        iconCls: getButtonIconCls(Wtf.etype.sync),        
        hidden:!(Wtf.isLMSSync && (this.moduleid==Wtf.Acc_Invoice_ModuleId)),//hidden when LMS not subscribed and module is invoice module                        
        handler:function(){
            if(!Wtf.account.companyAccountPref.isLMSIntegration){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.lmsnotacivatedalert")],2);
            }else {
                this.syncCustomerfromLMS();              
            }                               
        }  
    }));
    this.agedDetailsBtn.on('click',this.showAgedDetails.createDelegate(this));
    if(!this.isRequisition) gridBtnArr.push(this.approvalHistoryBtn);
    gridBtnArr.push(this.estimateShippingCostBtn);
    //  bottombtnArr.push('-',this.customReportViewBtn);
 
    bottombtnArr.push(' ',this.linkinfoViewBtn);
    bottombtnArr.push(' ',this.relatedTransactionsBtn);
    gridBtnArr.push(this.agedDetailsBtn);
    if((this.moduleid==Wtf.Acc_Invoice_ModuleId||this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)&&!this.pendingapproval){
      gridBtnArr.push(this.bulkReceive);
    }
    if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isitcapplicable && (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid === Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId)) {
        /**
         * 
         * @param {type} paramButton to post ITC JE.
         */
        gridBtnArr.push(this.ITCJE = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.ITCJEPost"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.ITCJEPost.tooltip"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function() {
                this.postITCJE();
            }
        }));
    }
    gridBtnArr.push(this.revertGiroGeneratedStatusBtn);
    if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
       // gridBtnArr.push(this.FormDetailsWindowBtn); // removing this button refer ERP-34181
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        bottombtnArr.push('-', this.exportButton);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType) || !WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        bottombtnArr.push(' ', this.singlePrint);
//        bottombtnArr.push('-', this.printButton);
//        bottombtnArr.push(' ', this.singleRowPrint);
        //Add Job Order Flow print button
        if((this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Sales_Order_ModuleId) && Wtf.account.companyAccountPref.jobOrderItemFlow){
//            bottombtnArr.push(' ', this.jobOrderPrint);
        }
    }
    bottombtnArr.push(' ', this.veiwVesions);
//  this.tbar3.push(gridBtnArr,"->", this.customReportViewBtn);//ERP-12456 [SJ]
   this.tbar3.push(gridBtnArr);//ERP-12456 [SJ]
    this.tbar3.push("-");
    if(config.extraFilters == undefined){
        if(!this.isQuotation && !this.pendingapproval  && !this.isLeaseFixedAsset && !this.isConsignment && !this.isfromReportList&&this.moduleid != Wtf.Acc_Security_Gate_Entry_ModuleId){ //&& !this.isFixedAsset
            this.tbar3.push("->");
            this.tbar3.push("&nbsp;View", this.typeEditor);
        }
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId && this.pendingapproval) {
            this.tbar3.push("&nbsp;View", this.typeEditor);
        }
    }
    this.tbar3.push('->',((this.isQuotation && !this.isLeaseFixedAsset && !this.isConsignment)? WtfGlobal.getLocaleText("acc.common.view") : ""), this.archieveCombo);
    this.tbar1 = new Array();
    this.tbar1.push(this.quickPanelSearch, this.resetBttn,!this.isSalesCommissionStmt?this.newTabButton:"", btnArr,this.AdvanceSearchBtn);
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
        if(config.isOrder && !this.isQuotation && !this.isRequisition && !this.isRFQ){ // For sales/purchase order show 'cost center' filter in main tbar applied for panel
            this.tbar2.push("-", WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
        }
    } else if(config.extraFilters != undefined && !this.pendingapproval){//Invoice Report View - Add type filter in main tbar
        if(!config.isOrder){this.tbar3.push("&nbsp;View", this.typeEditor);}
    }
    var helpmodeid='';
    if(!this.isLeaseFixedAsset  && !this.isConsignment && !this.pendingapproval && !this.isDraft&&!this.reportbtnshwFlag){    //Hide Help icon button pending approval.
        if(this.id="VendorQuotationList" && this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId){
            helpmodeid=95;
        }else if(this.id="securityGateList" && this.moduleid == Wtf.Acc_Security_Gate_Entry_ModuleId){
            helpmodeid=1116;
        }else{
            helpmodeid=config.helpmodeid;
        }
        this.tbar1.push("->", getHelpButton(this,helpmodeid));
    }
    if(this.isfromsearchwin){
        this.tbar1 = new Array();
        this.tbar1.push(this.quickPanelSearch,'-');
        this.tbar1.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.tbar1.push(this.startDate,'-');
        this.tbar1.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.tbar1.push(this.endDate,'-');
        this.tbar1.push(this.submitBttn,'-'); 
        this.tbar1.push(this.AdvanceSearchBtn);
        bottombtnArr=[];
    }
    
     var FirstTopToolaBr = new Wtf.Toolbar(this.tbar1);
     this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [this.objsearchComponent
                    , {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.gridtbarPanel],
                tbar: FirstTopToolaBr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                    items : bottombtnArr  //added utton of link info button
                })
            }]
    });
    //this.isfromReportList is true if outstanding SO/PO Report is Clicked from Reportlist 
    if (!this.pendingapproval && !this.isDraft && !this.isfromReportList && !this.isfromsearchwin) {
        /*
         * getReportMenu() - function is used to add reports related to current module
         * Params: 1)  toolbar object,2)  moduleid, 3)  modulename
         */
        WtfGlobal.getReportMenu(FirstTopToolaBr, this.moduleid, WtfGlobal.getModuleName(this.moduleid));
    }
     
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });
    this.Store.on('beforeload',function(s,o){
        //        var cm=new Wtf.grid.ColumnModel(this.gridColumnModelArr);
        //        this.grid.reconfigure(s, cm);
        WtfGlobal.setAjaxTimeOut();
        //        if(this.moduleid==20||this.moduleid==Wtf.SalesCommisionStmt_Moduleid || this.moduleid==Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || 
        //            this.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid==20 ||this.moduleid==2||this.moduleid==18||
        //            this.moduleid==6||this.moduleid==22||this.moduleid==23||this.moduleid==32||this.moduleid==33 || this.moduleid== Wtf.Acc_Lease_Order || 
        //            this.moduleid== Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId || this.moduleid== Wtf.Acc_FixedAssets_Purchase_Order_ModuleId || 
        //            this.moduleid== Wtf.Acc_FixedAssets_RFQ_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid==Wtf.Acc_ConsignmentVendorRequest_ModuleId ){
        //           
        //            Wtf.Ajax.requestEx({
        //                url: "ACCAccountCMN/getCustomizedReportFields.do",
        //                params: {
        //                    flag: 34,
        //                    moduleid:this.moduleid,
        //                    reportId:1,
        //                    isFormField:false,
        //                    isLineField:false
        //                }
        //            }, this, function(action, response){
        //                if(action.success && action.data!=undefined){
        //                    this.customizeData=action.data;
        //                    var cm=this.grid.getColumnModel();
        //                    for(var i=0;i<action.data.length;i++){
        //                        for(var j=0;j<cm.config.length;j++){
        ////                            if(cm.config[j].dataIndex==action.data[i].fieldDataIndex){
        //////                                alert(cm.config[j].dataIndex);
        ////                                  cm.setHidden(j,true);
        ////                            }
        //                            if (cm.config[j].header == action.data[i].fieldDataIndex || (cm.config[j].dataIndex == action.data[i].fieldDataIndex && cm.config[j].header == action.data[i].fieldname)) {
        //                                if (!action.data[i].isForProductandService) {
        //                                    cm.setHidden(j, action.data[i].hidecol);
        //                                }
        //  
        //                            }
        //                        }
        //                    }
        //                    this.grid.reconfigure( s, cm);
        //                } else {
        ////                    Wtf.Msg.alert('Status', action.msg);
        //                }
        //            },function() {
        ////                Wtf.updateProgress();
        ////                WtfGlobal.resetAjaxReqTimeout();
        //            });
        //       
        //        }
                 
       
                 
        if(!o.params)o.params={};
        o.params.cashonly=this.cashonly;
        o.params.creditonly=this.creditonly;
        o.params.CashAndInvoice=(!this.creditonly&&!this.cashonly)?true:false;
        o.params.salesPersonFilterFlag=true;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.deleted=this.deleted;
        currentBaseParams.nondeleted=this.nondeleted;
        currentBaseParams.cashonly= this.cashonly;
        currentBaseParams.creditonly=this.creditonly;
        currentBaseParams.CashAndInvoice=(!this.creditonly&&!this.cashonly)?true:false;
        currentBaseParams.salesPersonFilterFlag=true;
        currentBaseParams.ispendingpayment=this.ispendingpayment;
        currentBaseParams.joborderitem=this.joborderitem;
        currentBaseParams.isreversalitc=this.isreversalitc;
        currentBaseParams.archieve=this.archieveCombo?this.archieveCombo.getValue() : 0;
        currentBaseParams.costCenterId = this.costCenter.getValue();
        currentBaseParams.productid = this.productname.getValue();
        currentBaseParams.productCategoryid = this.productCategory.getValue();
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            currentBaseParams.formtypeid = this.FormType != undefined ? this.FormType.getValue(): 0;
            currentBaseParams.vatcommodityid = this.vatCommodityCombo != undefined ? this.vatCommodityCombo.getValue() : "all";
            currentBaseParams.checkformstatus = this.FormStatus != undefined ? this.FormStatus.getValue() : 0;
        }
        currentBaseParams.termid = this.CreditTerm.getValue();
        /*
         * ERP-735 Associate default paymemt Method to customer 
         * Put pmtMethod value in currentbaseparam for filter the records
         */
        currentBaseParams.pmtmethod = this.pmtMethod.getValue();
        currentBaseParams.includeExcludeChildCmb = this.includeExcludeChildCmb.getValue();
        currentBaseParams.newcustomerid = this.custmerCmb.getValue(),
        currentBaseParams.newvendorid = this.vendorCMB.getValue(),
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());   //CI & CS Report  //ERP-8521
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());         //CI & CS Report  //ERP-8521
        currentBaseParams.isOpeningBalanceInvoices=this.isOpeningBalanceInvoices;
        currentBaseParams.onlyRecurredInvoices=this.onlyRecurredInvoices;
        currentBaseParams.blockedDocuments=this.blockedDocuments;
        currentBaseParams.unblockedDocuments=this.unblockedDocuments;
        currentBaseParams.onlyNormalPendingInvoices=this.onlyNormalPendingInvoices;
        currentBaseParams.onlyInventoryPI=this.onlyInventoryPI;
        currentBaseParams.onlyExpensePI=this.onlyExpensePI;
        currentBaseParams.isOpeningBalanceOrder=this.isOpeningBalanceOrder;
        currentBaseParams.orderLinkedWithDocType=this.orderLinkedWithDocType;
        currentBaseParams.invoiceLinkedWithDOStatus=this.invoiceLinkedWithDOStatus;
        currentBaseParams.invoiceLinkedWithGRNStatus=this.invoiceLinkedWithGRNStatus;
        currentBaseParams.isRequisitionOutstandingFilterApplied=this.isRequisitionOutstandingFilterApplied
        currentBaseParams.customerQuotationsWithInvoiceAndDOStatus=this.customerQuotationsWithInvoiceAndDOStatus;
        
        currentBaseParams.myPO=this.myPO;
        currentBaseParams.includeAllRec=this.includeAllrec;
        if(this.generatedSource!=undefined &&  this.generatedSource!="undefined"&& this.generatedSource!=null){
            currentBaseParams.generatedSource=this.generatedSource;
        }
        if(this.grid.getStore().getSortState()!=undefined){
            currentBaseParams.sort=this.grid.getStore().getSortState().field;
            currentBaseParams.dir=this.grid.getStore().getSortState().direction;
        }
        if(this.pendingapproval != undefined && this.pendingapproval==true){
            currentBaseParams.pendingapproval=this.pendingapproval;
        }
        if (this.isDraft != undefined && this.isDraft == true) {
            currentBaseParams.isDraft = this.isDraft;
        }
        currentBaseParams.pagingFlag=true;
        if(currentBaseParams.archieve == 3){
            currentBaseParams.isfavourite=true;
        }else{
            currentBaseParams.isfavourite=this.isfavourite;
        }
        if(this.person && this.person!="" && this.businessPerson=="Vendor")
        {
            currentBaseParams.newvendorid = this.person;
        }
        else if(this.person && this.person!="" && this.businessPerson=="Customer")
        {
            currentBaseParams.newcustomerid = this.person;
        }  

        // disable 'Copy' & 'View Pending Approval' buttons if user is viewing 'Oustanding PO(s)'
        if(this.outstandingreportflag){
            this.typeEditor.setValue(7);  // if we click on Outstaning SO/PO button while creating order combobovx value is set
            this.index=7;
        }
        
        if (this.typeEditor != undefined && this.typeEditor.getValue() == this.viewoption.normalinvoice) {
            currentBaseParams.onlyNormalPendingInvoices = true; //when pending approval tab opens first time at that time to show only pending invoices excluding recurred pending invoices.
        }
                 
        if (this.index == 7){
            currentBaseParams.isOutstanding=true;
          
            if(this.copyInvBttn)this.copyInvBttn.disable();
            this.pendingApprovalBttn.disable();
        }else{
            currentBaseParams.isOutstanding=false;
           
            //            if(this.copyInvBttn)this.copyInvBttn.enable();
            this.pendingApprovalBttn.enable();
        }
        if (this.index == 11) {
            currentBaseParams.isOuststandingproduct = true;
        }
        else {
            currentBaseParams.isOuststandingproduct = false;
        }   
        if (this.index == Wtf.INDEX_SOs_FOR_INVOICING) {
            currentBaseParams.isPendingInvoiced = true;
        } else {
            currentBaseParams.isPendingInvoiced = false;
        }
        if(this.isSalesCommissionStmt){
            currentBaseParams.isSalesCommissionStmt = this.isSalesCommissionStmt;
            currentBaseParams.userid = this.users.getValue();
            currentBaseParams.nondeleted=true;
        }
        if(this.customerBankAccountType){
            currentBaseParams.customerBankAccountType=this.customerBankAccountType.getValue();   
        }
        if (this.isfromsearchwin) {
            currentBaseParams.isfromsearchwin = this.isfromsearchwin;
            currentBaseParams.linkedWithModuleId=this.linkedWithModuleId
            currentBaseParams.closeflagForLink=this.closeflag
        }
        this.Store.baseParams=currentBaseParams;
        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
            this.exportButton.params=currentBaseParams;
       
            this.exportButton.setParams({
                costCenterId : this.costCenter.getValue(),
                productid : this.productname.getValue(),
                newcustomerid : this.custmerCmb.getValue(),
                newvendorid : this.isfromsearchwin?this.person:this.vendorCMB.getValue(),// Flag from PI for select PO to link with PI
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                archieve : this.archieveCombo?this.archieveCombo.getValue() : 0,
                //            start : 0, // ERP-12007,ERP-12009
                //limit : this.pP.combo !=undefined?this.pP.combo.value:"",
                ss : (this.quickPanelSearch.getValue()==undefined)?"":this.quickPanelSearch.getValue(),
                pagingFlag:true   //Mayur B- Added values to export parameters when we change any value from it.
            });
        }
       
    },this);
//    this.Store.on('load', this.hideMsg, this);
    this.loadParmStore();
    this.getMyConfig();
    this.typeEditor.on('select',this.loadTypeStore,this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expandStore.on('loadexception', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
    this.expander.on("expand",this.onRowexpand,this);
    //this.grid.on('render',this.loadParmStore,this);
    this.Store.on('load', this.outstandingPOReport, this);
    this.Store.on('load', this.outstandingSOReport, this);
    this.Store.on('load',this.expandRow, this);
    this.Store.on('load',this.hideLoading, this);
    this.Store.on('loadexception',this.hideLoading, this);
    Wtf.account.TransactionListPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
//    this.grid.addEvents({
//        'savemystate' : true
//    });
   this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
   this.grid.on('cellclick',this.onCellClick, this);
}
Wtf.extend(Wtf.account.TransactionListPanel,Wtf.Panel,{
    loadStoreGrid:function(){ //To set initial start and limit of toolbar
        if(this.Store.getRange().length > 0){
            this.Store.reload();
        }
        else{
            this.Store.load({
                params : {
                    start:0,
                    limit: this.pP.combo!=undefined?this.pP.combo.value:30,
                    pagingFlag:true
                }
            });
        }
    },
    withoutRateCurrencyDeletedSymbolWithPermissionCheck:function(v,m,rec){
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
            return Wtf.UpriceAndAmountDisplayValue;
        } else{
            return WtfGlobal.withoutRateCurrencyDeletedSymbol(v,m,rec);
        }
    },
    currencyDeletedRendererWithPermissionCheck:function(v,m,rec){
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
            return Wtf.UpriceAndAmountDisplayValue;
        } else{
            return WtfGlobal.currencyDeletedRenderer(v,m,rec);
        }
    },
  submitHandler : function(){
      if(this.startDate.getValue()>this.endDate.getValue()){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
             return;
      }
      this.loadStore();
  },
    shouldTaxBeHidden: function() {
        var date = new Date();
        var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(date.clearTime());
        return isTaxShouldBeEnable;
    }, 
  customizeView : function() {
    this.customizeViewWin=new Wtf.customizeView({
        scope:this,
        moduleid:this.moduleid,
        parentHelpModeId:this.helpmodeid,
        parentId:this.id,
        isForFormFields : false
    });
    this.customizeViewWin.show();
  },
  formDetailsWindow : function() {
    this.FormDetailsWindow=new Wtf.FormDetailsWindow({
        scope:this,
        isCustomer:this.isCustomer,
        records:this.sm.getSelections()
    });
    this.FormDetailsWindow.show();
  },
    openDraftedDocumentTab: function () {
        var panel;
        switch (this.moduleId) {
            case Wtf.Acc_Invoice_ModuleId :
                panel = Wtf.getCmp("draftedInvoiceList");
                break;
            case Wtf.Acc_Customer_Quotation_ModuleId:
                panel = Wtf.getCmp("draftedcustomerquotationList");
                break;
            case Wtf.Acc_Sales_Order_ModuleId:
                panel = Wtf.getCmp("draftedSalesOrderList");
                break;
            case Wtf.Acc_Purchase_Requisition_ModuleId:
                panel = Wtf.getCmp("draftedPurchaseRequisitionList");
                break;
            case Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId:
                panel = Wtf.getCmp("draftedFixedAssetPurchaseRequisitionList");
                break;
            case Wtf.Acc_Purchase_Order_ModuleId:
                panel = Wtf.getCmp("draftedPurchaseOrderList");
                break;
            case Wtf.Acc_Vendor_Invoice_ModuleId:
                panel = Wtf.getCmp("draftedPurchaseInvoiceList");
                break;    
        }
        if (panel == null) {
            switch (this.moduleId) {
                case Wtf.Acc_Invoice_ModuleId :
                    panel = getInvoiceTab(false, "draftedInvoiceList", "Draft Sales Invoice(s)", undefined, false, false, false, undefined, undefined, this.moduleid, undefined, false, false, false, true);
                    break;
                case Wtf.Acc_Customer_Quotation_ModuleId:
                    panel = getQouteTab(false, "draftedcustomerquotationList", "Draft Customer Quotation(s)", undefined, false, true, undefined, undefined, undefined, undefined, undefined, true);
                    break;
                case Wtf.Acc_Sales_Order_ModuleId://Drafted Sales Order(s) List
                    panel = getSOTab(false, "draftedSalesOrderList", "Draft Sales Order(s)", undefined, false, false, false, undefined, false, false, false, undefined, undefined, false, false, false, true);
                    break;
                case Wtf.Acc_Purchase_Requisition_ModuleId://Drafted Sales Order(s) List
                    panel = getPRTab(false, "draftedPurchaseRequisitionList", "Draft Purchase Requisition(s)", undefined, false, false, false, undefined, false, true);
                    break;
                case Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId:
                    panel= getPRTab(false, "draftedFixedAssetPurchaseRequisitionList", "Draft Fixed Asset Purchase Requisition(s)", undefined, false, false, false, undefined, false, true,true);
                    break;
                case Wtf.Acc_Purchase_Order_ModuleId://Drafted Purchase Order(s) List
                    panel = getPOTab(false, "draftedPurchaseOrderList", "Draft Purchase Orders", undefined, false, false, false, undefined, false, false, false, undefined, undefined, false, false, false, true);
                    break;
                case Wtf.Acc_Vendor_Invoice_ModuleId://Drafted Purchase Invoice(s) List
                    panel = getVendorInvoiceTab(false, "draftedPurchaseInvoiceList", "Draft Purchase Invoices", undefined, false, false, false, undefined, this.moduleid, undefined, undefined, false, false, false, true);
                    break;
            }
            Wtf.getCmp('as').add(panel);
        }

        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    },
    viewApprovalHistory : function(){
     
        var rec = this.sm.getSelected();
        Wtf.Ajax.requestEx({
            url:"ACCReports/getApprovalhistory.do",
            params: {
                billid : rec.data.billid
            }
        },this,function(response, request){
            var historyWin = new Wtf.Window({
                height : 300,
                width : 475,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                title : WtfGlobal.getLocaleText("acc.field.ApprovalHistory"),
                bodyStyle : 'padding:5px;background-color:#ffffff;',
                layout : 'border',
                items : [{
                    region : 'north',
                    border:false,
                    height:70,
                    bodyStyle : 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                    html:getTopHtml(WtfGlobal.getLocaleText("acc.field.ApprovalHistory"),WtfGlobal.getLocaleText("acc.field.ApproveHistoryof") +this.label +" <b>"+rec.data.billno+"</b>"  ,"../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                }, {
                    region : 'center',
                    border : false,
                    autoScroll : true,
                    bodyStyle : 'padding:5px;background-color:#f1f1f1;',
                    html : response.msg
                }],
                buttons : [{
                    text : WtfGlobal.getLocaleText("acc.common.close"),
                    handler : function(){
                        historyWin.close();
                    }
                }],
                autoScroll : true,
                modal : true
            });

            historyWin.show();
        },function(response, request){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
        });
    },
    
    //Function to handle Shipping Cost estimation request from Sales Order report
    //Used only when UPS Integration is enabled as cost estimation option is available to used only when Integration is on
    estimateShippingCost: function () {
        var previouslySelectedRecordID = this.recordIDForCostCalculation;
        var selectedRecordsArr = this.grid.getSelectionModel().getSelections();
        var selectedRecordsIndexArr = [];
        for (var i = 0; i < selectedRecordsArr.length; i++) {
            selectedRecordsIndexArr.push(this.Store.indexOf(selectedRecordsArr[i]));
        }
        var recordsForCostCalculation = WtfGlobal.getJSONArrayWithoutEncodingNew(this.grid, true, selectedRecordsIndexArr);
        var recordsForCostCalculationArr = JSON.parse(recordsForCostCalculation);
        this.recordIDForCostCalculation = recordsForCostCalculationArr[0].billid;

        //if currently selected record is not same as previously selected record, then we destroy existing component and create new one
        if (this.estimateTotalCostWindow && this.recordIDForCostCalculation != previouslySelectedRecordID) {
            this.estimateTotalCostWindow.destroy();
            this.estimateTotalCostWindow = undefined;
        }
        if (!this.estimateTotalCostWindow) {
            //request to fetch ship-from and ship-to addresses for shipment
            Wtf.Ajax.requestEx({
                url: "Integration/getAddressesForUps.do",
                params: {
                    salesOrderCostEstimationFlag: true,
                    billid: this.recordIDForCostCalculation//sending recordID in key bills because it is required in this key by method getPackingRows on javaside
                }
            }, this, this.getDetailsForShippingCostSuccessHandler, this.getDetailsForShippingCostFailureHandler);
        } else {
            this.estimateTotalCostWindow.show();
        }

        this.Store.on('load', this.storeloaded, this);
    },
    
    getDetailsForShippingCostSuccessHandler: function (response, request) {
        //create shipping cost calculation window on successful retrieval of addresses
        this.estimateTotalCostWindow = new Wtf.UpsShipmentDetailsWindow({
            title: WtfGlobal.getLocaleText("acc.pickpackship.shipmentDetails"),
            parentCmpId: this.id,
            recordIDForCostCalculation: this.recordIDForCostCalculation,
            start: 0,
            limit: this.pP.combo.value,
            ss: this.quickPanelSearch.getValue(),
            salesOrderCostEstimationFlag : true,
            addressesJson: response != undefined ? (response.addressesJson != undefined ? response.addressesJson : {}) : {}
        });
        this.estimateTotalCostWindow.show();
    },
    
    getDetailsForShippingCostFailureHandler: function (response) {
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    
    setQuarterMonthAndyear: function() {
        var mindate,maxdate;
        var quarteryear = this.QuarterYear.getValue();
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        var quarterData = WtfGlobal.getMonthQuarterdata(quarteryear,sDate,eDate);
        var qindex = this.monthQuarter.getValue();
        var startDate=quarterData[qindex][qindex].quarterstartDate;
        var endDate=quarterData[qindex][qindex].quarterendDate;
        
        this.startDate.minValue = undefined;
        this.startDate.maxValue = undefined;
        this.endDate.minValue = undefined;
        this.endDate.maxValue = undefined;
        
        this.startDate.setValue(startDate);
        this.endDate.setValue(endDate);
        
        //start date and end is in String formate during previous month condition 
        if (typeof startDate === "string" && typeof endDate === "string") {
            mindate = new Date(startDate);
            maxdate = new Date(endDate);
        } else {
        //start date and end date is in date object formate during All,1st,2nd,3rd and 4th Quarter condition 
            mindate = startDate;
            maxdate = endDate;
        }
        //function to set Tool Tip after selecting quaerter year
        WtfGlobal.setDateToolTipAfterselectQuarterYear(this.startDate);
        WtfGlobal.setDateToolTipAfterselectQuarterYear(this.endDate);
        
        //disable quarter year durin All Condition
        if (qindex == 0) {
            this.QuarterYear.disable();
            this.QuarterYear.reset();
            this.startDate.reset();
            this.endDate.reset();

        } else {
            this.QuarterYear.enable();
            //set min and max value to start and end date respectively.
            this.startDate.minValue = mindate;
            this.startDate.maxValue = maxdate;
            this.endDate.minValue = mindate;
            this.endDate.maxValue = maxdate;
        }
    },
    hideLoading: function() {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        if(this.grid.loadMask) {
            this.grid.loadMask.hide();
        }
    },
    getEditButtonToolTipMsg:function(moduleID){
        var returnMsg="";
        if (this.isOrder) {
            if (moduleID == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) {
                returnMsg = WtfGlobal.getLocaleText("acc.fixedasset.editpurchaserequisition");
            } else if (moduleID == Wtf.Acc_Purchase_Requisition_ModuleId) {
                returnMsg = WtfGlobal.getLocaleText("acc.normal.editpurchaserequisition");
            }else if (moduleID == Wtf.Acc_ConsignmentRequest_ModuleId) {
                returnMsg = WtfGlobal.getLocaleText("acc.invoiceList.editreq");
            }else {
                returnMsg = WtfGlobal.getLocaleText("acc.invoiceList.editO");
            }     
        } else {
                returnMsg = WtfGlobal.getLocaleText("eveacc.invoiceList.editQ");
        }
        return returnMsg;
    },
    loadTypeStore:function(a,rec){
        this.startDate.minValue = undefined;
        this.startDate.maxValue = undefined;
        this.endDate.minValue = undefined;
        this.endDate.maxValue = undefined;
        this.QuarterYear.disable();
        this.monthQuarterStore.loadData(this.monthQuarterStoreData1);
        this.monthQuarter.setValue(0);
        this.monthQuarterStore.removeAll();
        if(this.startDate)
                this.startDate.setValue(WtfGlobal.getDates(true));
        if(this.endDate)
            this.endDate.setValue(WtfGlobal.getDates(false));
        this.showAllBarButtons();
        this.isOpeningBalanceInvoices=false;
        this.includeAllrec=false;
        this.isOpeningBalanceOrder=false;
        this.blockedDocuments=false;
        this.unblockedDocuments=false;
        this.onlyRecurredInvoices=false;//this flag is used to fetch only recurred(child) sales invoices
        this.isPendingInvoiced=false;//this flag is used to fetch only SO(s) which are still available for invoicing, it may be partially invoiced or not invoiced at all.
        this.onlyInventoryPI=false;//this flag is used to fetch only Inventory Purchase Invoices
        this.onlyExpensePI=false;//this flag is used to fetch only Expense Purchase invoices
        this.onlyNormalPendingInvoices=false;//this flag is used in pending approval tab of invoice to fetch only those invoices which are pending but not recurred.
        this.cashonly=false;
        this.creditonly=false;
        this.deleted=false;
        this.nondeleted=false;
        this.isfavourite=false;
        this.outstandingreportflag=false;
        this.outstandingreportflag=undefined;
        this.ispendingpayment=false;
        this.joborderitem=false;
        this.isreversalitc=false;
        this.index=rec.data.typeid;
        this.costCenter.enable();
        this.CreditTerm.enable();
        this.productname.enable();
        this.productCategory.enable();
        if(this.customerBankAccountType){
            this.customerBankAccountType.enable();
        }    
        this.orderLinkedWithDocType=0;
        this.invoiceLinkedWithDOStatus=0;
        this.invoiceLinkedWithGRNStatus=0;
        this.isRequisitionOutstandingFilterApplied=false;
        this.myPO=false;
        if (this.index == 0) {
            this.includeAllrec = true;
            this.monthQuarterStore.loadData(this.monthQuarterStoreData);
        } else if (this.index == 1)
            this.cashonly=true;
        else if(this.index==2)
            this.creditonly=true;
        if(this.index==4)
            this.nondeleted=true;
        if(this.index==3){
           this.deleted=true;
           if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
                if(this.deleteTrans){
               this.deleteTrans.disable();
            }
            if(this.deleteTransperm){
               this.deleteTransperm.disable();
            }
           }
        } else{
            if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
                if(this.deleteTrans){
               this.deleteTrans.enable();
            }
            if(this.deleteTransperm){
               this.deleteTransperm.enable();
            }
            }
        }
        //this.sm.singleSelect = true;
        if(this.index==5){
            this.isfavourite=true;

            this.pendingApprovalBttn.enable();
            if(this.copyInvBttn)this.copyInvBttn.enable();

        }else if(this.index==6){
            this.ispendingpayment=true;
            this.sm.singleSelect = false;

            this.pendingApprovalBttn.enable();
            if(this.copyInvBttn)this.copyInvBttn.enable();

        } else if (this.index==7){
            this.isOutstanding = true;
            // disable 'Copy' & 'View Pending Approval' buttons if user is viewing 'Oustanding PO(s)'
            if(this.copyInvBttn)this.copyInvBttn.disable();
            this.pendingApprovalBttn.disable();
        } else if (this.index==Wtf.INDEX_SOs_FOR_INVOICING){
            this.isPendingInvoiced = true;
            // disable 'Copy' & 'View Pending Approval' buttons if user is viewing 'Oustanding SO(s)'
            if(this.copyInvBttn)this.copyInvBttn.disable();
            this.pendingApprovalBttn.disable();
        } else if(this.index==11){
             this.isOuststandingproduct=true;
             this.myPO=true;
        }
        else if (this.index==8){
            if(this.startDate)
                this.startDate.setValue(this.getLastFinancialYRStartDate(true));
            if(this.endDate)
                this.endDate.setValue(WtfGlobal.getOpeningDocumentDate(true));

            if(this.isOrder){
                this.isOpeningBalanceOrder=true;
            }else{
                this.isOpeningBalanceInvoices=true;
                this.pendingApprovalBttn.enable();
                if(this.copyInvBttn)this.copyInvBttn.enable();
                    this.hideAllBarButtons();
                this.costCenter.reset();
                this.CreditTerm.reset();
                this.productname.reset();
                this.productCategory.reset();
                this.includeExcludeChildCmb.reset();
                this.costCenter.disable();
                this.CreditTerm.disable();
                this.productname.disable();
                this.productCategory.disable();  
                if(this.customerBankAccountType && Wtf.account.companyAccountPref.activateIBGCollection ){
                    if(this.customerBankAccountType.getValue() != "" ) {
                        this.customerBankAccountType.reset() ;
                    }
                    this.customerBankAccountType.disable();
                }
            }
        }else if(this.index==9 && this.isOrder){
            if(this.blockunblockbtn.text==WtfGlobal.getLocaleText("acc.invoiceList.blockdocuments")){
                this.blockunblockbtn.setText(WtfGlobal.getLocaleText("acc.invoiceList.unblockdocuments"));   
                this.blockunblockbtn.setTooltip(WtfGlobal.getLocaleText("acc.invoiceList.unblockdocumentscross"));   
            }  
            this.blockedDocuments=true;
          
        } else if(this.index==10 && this.isOrder){
            if(this.blockunblockbtn.text==WtfGlobal.getLocaleText("acc.invoiceList.unblockdocuments")){
                this.blockunblockbtn.setText(WtfGlobal.getLocaleText("acc.invoiceList.blockdocuments"));   
                this.blockunblockbtn.setTooltip(WtfGlobal.getLocaleText("acc.invoiceList.blockdocumentscross"));
            }  
            this.unblockedDocuments=true;
        } else if(this.index == this.viewoption.recurredinvoice ){
            this.onlyRecurredInvoices=true;
        }else if(this.index == this.viewoption.normalinvoice ){
            this.onlyNormalPendingInvoices=true;
        }else if(this.index == this.viewoption.inventorypurchaseinvoice ){ 
            this.onlyInventoryPI=true;
        }else if(this.index == this.viewoption.expencepurchaseinvoice ){
            this.onlyExpensePI=true;
        }else if(this.index ==12 || this.index ==13 ||  this.index ==14 ||  this.index ==15){
            this.orderLinkedWithDocType=this.index;
        }else if(this.index==18){
            /**
             * If Job Order selected 
             */
            this.joborderitem=true;
        }else if (this.index==19){
            /**
             * Reversal ITC is selected (India GST)
             */
            this.isreversalitc=true;
        }
        if((this.index==10 || this.index==9) && this.isOrder ){
            this.grid.getColumnModel().setHidden(this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage"), false);
            
        }else if (this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage")!=-1){
          this.grid.getColumnModel().setHidden(this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage"), true);  
        }
       /*------Setting Parameter to send Java side to identify applied filter------*/
        if ((this.index == 10 || this.index == 11 || this.index == 12) && this.moduleId == Wtf.Acc_Invoice_ModuleId) {
            this.invoiceLinkedWithDOStatus = this.index;
        }
        if (this.index == Wtf.INDEX_MOBILE_TRANSACTIONS ) {
            this.generatedSource = Wtf.RECORD_Mobile_Application;
        }else{
            this.generatedSource="";
        }
        
         if ((this.index == 11 || this.index == 12 || this.index == 13) && this.moduleId == Wtf.Acc_Vendor_Invoice_ModuleId) {
            this.invoiceLinkedWithGRNStatus = this.index;
        }
       
        /*-----If fetching Outstanding PR ---------- */
        if (this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId && this.index == 6) {
            this.isRequisitionOutstandingFilterApplied = true;
        }
        this.invID = "";
        this.exponly = false;
        this.Store.on('load',this.storeloaded,this);
        this.loadStore();
       WtfComMsgBox(29,4,true);

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
        return fd.add(Date.YEAR, 0).add(Date.DAY, -1);
    },
    saveMyState: function(){
//        var state = this.grid.getState();
//        this.grid.fireEvent("savemystate", this, state);
    },
       
    getMyConfig : function(){
        WtfGlobal.getGridConfig (this.grid, this.moduleid, false, false);
        
        var statusForCrossLinkage = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkage != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkage, true);
        }
    },
   
    saveMyStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
    },
    updateStoreConfig:function(colModelArray, store) {
        if(colModelArray){
            for(var cnt = 0;cnt < colModelArray.length;cnt++){
                var fieldname = colModelArray[cnt].fieldname;
                var newField = new Wtf.data.Field({
                    name:fieldname.replace(".",""),
                    sortDir:'ASC',
                    type:colModelArray[cnt].fieldtype == 3 ?  'date' : (colModelArray[cnt].fieldtype == 2?'float':'auto'),
                    dateFormat:colModelArray[cnt].fieldtype == 3 ?  null : undefined
                });
                store.fields.items.push(newField);
                store.fields.map[fieldname]=newField;
                store.fields.keys.push(fieldname);
            }
            store.reader = new Wtf.data.KwlJsonReader(store.reader.meta, store.fields.items);
        }
    },
   
    getLastFinancialYRStartDate:function(start){
        //var d=Wtf.serverDate;
        //        if(this.statementType=='BalanceSheet'&&start)
        //             return new Date('January 1, 1970 00:00:00 AM');
//        var monthDateStr="";
//        if(Wtf.account.companyAccountPref.fyfrom)
//            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        //var fd=new Date('"Jan 01'+', '+1975+' 12:00:00 AM');
//        if(d<fd)
//            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        var fd = new Date('January 1' + ', ' + 1975 + ' 12:00:00 AM');
        if (start) {
            return fd.add(Date.YEAR, 0);
        } 
    },
   
    hideAllBarButtons:function(){
        if(this.deleteMenu)
            this.deleteMenu.hide();
        if(this.editBttn){
            this.editBttn.hide();
        }
        if(this.copyInvBttn){
            this.copyInvBttn.hide();
        }
        if(this.deleteMenu){
            this.deleteMenu.hide();
        }
//        if(this.RFQBtn){
//            this.RFQBtn.hide();
//        }
//        if(this.RFQListBtn){
//            this.RFQListBtn.hide();
//        }
//        if(this.RFQVendorBtn){
//            this.RFQVendorBtn.hide();
//        }
//        if(this.approveInvoiceBttn){
//            this.approveInvoiceBttn.hide();
//        }
//        if(this.rejectInvoiceBttn){
//            this.rejectInvoiceBttn.hide();
//        }
        if(this.pendingApprovalBttn){
            this.pendingApprovalBttn.hide();
        }
        if(this.paymentButton){
            this.paymentButton.hide();
        }
//        if(this.GIROFileButton){
//            this.GIROFileButton.hide();
//        }
        if(this.email){
            this.email.hide();
        }
        if (this.viewDeliveryPlannerBttn) {
            this.viewDeliveryPlannerBttn.hide();
        }
        if(this.RepeateInvoice){
            this.RepeateInvoice.hide();
        }
        if(this.singlePrint){
            this.singlePrint.hide();
        }
//        if(this.customReportViewBtn){
//            this.customReportViewBtn.hide();
//        }
//        if(this.linkinfoViewBtn){
//            this.linkinfoViewBtn.hide();
//        }
//        if(this.AdvanceSearchBtn){
//            this.AdvanceSearchBtn.hide();
//        }
        if(this.newTabButton){
            this.newTabButton.hide();
        }
        if(this.deleteTransperm){
            this.deleteTransperm.hide();
        }
//        this.tbar2.remove(WtfGlobal.getLocaleText("acc.common.from"));
    },
   
    showAllBarButtons:function(){
       
        if(this.deleteMenu)
            this.deleteMenu.show();
       
        if(this.deleteTransperm){
            this.deleteTransperm.show();
        }
        if(this.editBttn){
            this.editBttn.show();
        }
        if(this.copyInvBttn){
            this.copyInvBttn.show();
        }
        if(this.deleteMenu){
            this.deleteMenu.show();
        }
//        if(this.RFQBtn){
//            this.RFQBtn.show();
//        }
//        if(this.RFQListBtn){
//            this.RFQListBtn.show();
//        }
//        if(this.RFQVendorBtn){
//            this.RFQVendorBtn.show();
//        }
//        if(this.approveInvoiceBttn){
//            this.approveInvoiceBttn.show();
//        }
//        if(this.rejectInvoiceBttn){
//            this.rejectInvoiceBttn.show();
//        }
        if(this.pendingApprovalBttn){
            this.pendingApprovalBttn.show();
        }
        if(this.paymentButton){
            this.paymentButton.show();
        }
//        if(this.GIROFileButton){
//            this.GIROFileButton.show();
//        }
        if(this.email){
            this.email.show();
        }
        if (this.viewDeliveryPlannerBttn) {
            this.viewDeliveryPlannerBttn.show();
        }
        if(this.RepeateInvoice){
            this.RepeateInvoice.show();
        }
        if(this.singlePrint){
            this.singlePrint.show();
        }
//        if(this.customReportViewBtn){
//            this.customReportViewBtn.show();
//        }
//        if(this.linkinfoViewBtn){
//            this.linkinfoViewBtn.show();
//        }
        if(this.approvalHistoryBtn){
            this.approvalHistoryBtn.show();
        }
        if(this.AdvanceSearchBtn){
            this.AdvanceSearchBtn.show();
        }
        if(this.newTabButton){
            this.newTabButton.show();
        }
    },
   
    setCostCenter: function(){
        this.costCenter.setValue(this.costCenterId);//Select Default Cost Center
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    openNewTab:function() {
        if(this.isQuotation) {
            if(this.isCustomer)  {
                callCreateNewButtonFunction(22)
            }
            else if (!this.isCustomer) {
                if (this.isFixedAsset) {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.createavq)){
                        callCreateNewButtonFunction(89);
                    }
                    else{
                        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.lp.createavq"));
                    }
                } else {
                    callCreateNewButtonFunction(23)
                }
               
            }
        } else if(this.isRequisition) {
            if (this.isFixedAsset) {
                if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereq, Wtf.Perm.assetpurchasereq.createapreq)){
                    callFixedAssetPurchaseReq();
                }
                else{
                    WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.lp.createapreq")); 
                }
            }else {
                callPurchaseReq();
            }
        }else if(this.isRFQ ){
            if (this.isFixedAsset) {
                if (!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereq, Wtf.Perm.assetpurchasereq.createarfq)) {
                    callFixedAssetRequestForQuotation(false, undefined, undefined, this.isFixedAsset);
                } else {
                    WtfComMsgBox(46, 0, false, WtfGlobal.getLocaleText("acc.lp.createarfq"));
                }
            } else {
                callRequestForQuotation(false);
            }
        } else if(this.isOrder) {
            if(this.isCustomer) {
                if(this.isMRPSalesOrder){
                    callCreateNewButtonFunction(62);
                } else if (this.isJobWorkOrderReciever) {
                    callCreateNewButtonFunction(Wtf.MRP_Job_Work_ORDER_REC);
                } else {
                    callCreateNewButtonFunction(20);
                }
            }
            else if(!this.isCustomer) {
                if (this.isFixedAsset) {
                    if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.createapo)){
                        callCreateNewButtonFunction(90);
                    }
                    else{
                        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.lp.createapo"));
                    }
                } else  if(this.isMRPJOBWORKOUT){
                           callCreateNewButtonFunction(Wtf.MRP_Job_Work_ModuleID);
                }else if(this.isSecurityGateEntry){
                    callCreateNewButtonFunction(Wtf.Acc_Security_Gate_Entry_ModuleId);
                } else if (this.isJobWorkOrderReciever) {
                    callCreateNewButtonFunction(Wtf.Job_Work_Out_ORDER_REC);
                } else {
                           callCreateNewButtonFunction(18);
                }
            }  
        } else  if(this.isJobWorkOrderReciever){
            callCreateNewButtonFunction(Wtf.MRP_Job_Work_ORDER_REC);        
        } else  {
            /*
             Block is executed if called from job Work invoice
              */
            if(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId && this.isJobWorkoutInvoice!=undefined && this.isJobWorkoutInvoice!=null && this.isJobWorkoutInvoice){
                callSalesOrPurchaseType(this.isCustomer,this.isJobWorkoutInvoice);
            }else{
                callSalesOrPurchaseType(this.isCustomer);    
            }
        }
    },
    enableDisableButtons:function(){
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            if(this.deleteTrans){this.deleteTrans.enable();}
            if(this.deleteTransperm){
            this.deleteTransperm.enable();
         }
        }
        var arr=this.grid.getSelectionModel().getSelections();
        /*
                 *Disable Edit Button When Select Opening trasection count =1
                 */
        if(this.sm.getCount()==1 && arr[0].data.isOpeningBalanceTransaction && this.editBttn!= undefined){
            this.editBttn.disable();
        }
        if(arr.length==0&&!WtfGlobal.EnableDisable(this.uPermType,this.removePermType)){
            if(this.deleteTrans){this.deleteTrans.disable();}
            if(this.deleteTransperm){
               this.deleteTransperm.disable();
         }
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            for(var i=0;i<arr.length;i++){
                if(arr[i]&&arr[i].data.deleted){
                    if(this.deleteTrans){this.deleteTrans.disable();}
                    if(this.deleteTransperm){this.deleteTransperm.enable();}
                }
            }
        }

        if(this.isRequisition) {
            var arr = this.sm.getSelections();
            this.PR_IDS = [];this.PR_MEMOS='';
            if(arr.length==0) {
                if(this.RFQBtn && Wtf.account.companyAccountPref.isPRmandatory)this.RFQBtn.disable();
                if(this.RFQVendorBtn)this.RFQVendorBtn.disable();
            } else {
                if(this.RFQBtn)this.RFQBtn.enable();
                if(this.RFQVendorBtn)this.RFQVendorBtn.enable();
            }
            for(var cnt=0;cnt<arr.length;cnt++){
                if(arr[cnt]&&arr[cnt].data.approvestatuslevel!=11 || arr[cnt].data.deleted) {
                    if(this.RFQBtn && Wtf.account.companyAccountPref.isPRmandatory)this.RFQBtn.disable();
                    if(this.RFQVendorBtn)this.RFQVendorBtn.disable();
                }
                if(arr[cnt].data.memo!="") { 
                    this.PR_MEMOS += (cnt+1)+") "+arr[cnt].data.billno+" - "+arr[cnt].data.memo+"\n";
                }
                this.PR_IDS.push(arr[cnt].data.billid);
            }
        }
        var rec = this.sm.getSelections();
        if(this.sm.getCount()==1 && rec[0].data.deleted != true){
              if((this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Lease_Order) && Wtf.account.companyAccountPref.editso== true)
               {  
                    if(rec[0].data.createdbyid == loginid) {
                    if(this.editBttn && (!this.isRequisition || (this.isRequisition && rec[0].data.approvestatuslevel == -99 ))) {
                        this.editBttn.enable();}
                    }else{
                            if(this.editBttn)this.editBttn.disable();
                    }
                }else{
//                    if(this.editBttn && (!this.isRequisition || (this.isRequisition && rec[0].data.approvestatuslevel == -99 ))) {
                /*
                 *Disable Edit Button When Select Opening trasection
                 */
                    if(this.editBttn) {
                    if(arr[0].data.isOpeningBalanceTransaction && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)){
                        this.editBttn.disable();
                    }else{
                        if(rec[0].data.isjobworkoutrec && rec[0].data.isjobworkoutrec==true){
                           /**
                            * Disable edit button in case of Job Work Out Record
                            */
                            this.editBttn.disable();
                        }else{
                            this.editBttn.enable();
                        }
                        
                    }   
                }
            }             
          }else{
            if(this.editBttn)this.editBttn.disable();
         }
        
            if((this.sm.getCount()==1 && rec[0].data.deleted != true)){
            if (this.email && (this.isDraft == undefined || this.isDraft == "")) {
                this.email.enable();
            }
            if (this.viewDeliveryPlannerBttn) {
                this.viewDeliveryPlannerBttn.enable();
            }
//            if(this.editBttn && (!this.isRequisition || (this.isRequisition && rec.data.approvestatuslevel ==-1 ))) {
//                this.editBttn.enable();
//            }
            if(this.POfromSOBttn && rec[0].data.withoutinventory==false && rec[0].data.statusforcrosslinkage=="Open"&&!this.isDraft)this.POfromSOBttn.enable();
            if(this.SIfromSOBttn && rec[0].data.isLinkedTransaction==false && rec[0].data.statusforcrosslinkage=="Open"&&!this.isDraft)this.SIfromSOBttn.enable();
            if(this.SOfromPOBttn && rec[0].data.withoutinventory==false && rec[0].data.statusforcrosslinkage=="Open" && rec[0].data.isexpenseinv==false)this.SOfromPOBttn.enable();
            if(this.archieveBttn){
                if(rec[0].data.archieve == 0){
                    this.archieveBttn.enable();
                } else {
                    this.archieveBttn.disable();
                }
               
            }
            if(this.unArchieveBttn){
                if(rec[0].data.archieve == 0){
                    this.unArchieveBttn.disable();
                }else {
                    this.unArchieveBttn.enable();
                }
               
            }
                /*
                 *Disable Copy Button When Select Opening trasection
                 */
            if(this.copyInvBttn){
                 if(arr[0].data.isOpeningBalanceTransaction && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)){
                    this.copyInvBttn.disable();
                }else{
                    this.copyInvBttn.enable();
                }
                
            }
            if(rec[0].data.status == "Closed" && rec[0].data.isConsignment == true){
               if(this.freezeBttn)this.freezeBttn.disable(); 
            }else{
                if(this.freezeBttn)this.freezeBttn.enable();
            }
            if (this.unlinkDocumentBtn && arr[0].data.isLinkedTransaction) {
                this.unlinkDocumentBtn.enable();
            }
            if (this.blockunblockbtn ) {
                if(rec[0].data.statusforcrosslinkage=="Open"){
                    this.blockunblockbtn.setText(WtfGlobal.getLocaleText("acc.invoiceList.blockdocuments"));   
                    this.blockunblockbtn.setTooltip(WtfGlobal.getLocaleText("acc.invoiceList.blockdocumentscross"));
                }else if(rec[0].data.statusforcrosslinkage=="Closed"){
                    this.blockunblockbtn.setText(WtfGlobal.getLocaleText("acc.invoiceList.unblockdocuments"));   
                    this.blockunblockbtn.setTooltip(WtfGlobal.getLocaleText("acc.invoiceList.unblockdocumentscross"));   
                }
                    this.blockunblockbtn.enable();
                }
                
                  if (this.closedocumentbtn) {
                if (rec[0].data.closedmanually == "N/A" && rec[0].data.status=="Open") {
                    this.closedocumentbtn.enable();
                } else {
                    this.closedocumentbtn.disable();
                }
                }
            
            this.withInvMode = rec[0].data.withoutinventory;
        }else{
            if(this.email)this.email.disable();
            if (this.viewDeliveryPlannerBttn) {
                this.viewDeliveryPlannerBttn.disable();
            }
            if(this.POfromSOBttn && !Wtf.account.companyAccountPref.activateGroupCompaniesFlag){//if multi group company flag is activated.
                this.POfromSOBttn.disable();
            }
            if(this.SIfromSOBttn && !Wtf.account.companyAccountPref.activateGroupCompaniesFlag){//if multi group company flag is activated.
                this.SIfromSOBttn.disable();
            }
            if(this.SOfromPOBttn)this.SOfromPOBttn.disable();
            if(this.archieveBttn) {
                this.archieveBttn.disable();
            }
            if(this.unArchieveBttn) {
                this.unArchieveBttn.disable();
            }
            if(this.copyInvBttn)this.copyInvBttn.disable();
            if(this.freezeBttn)this.freezeBttn.disable();
            if (this.unlinkDocumentBtn) {
                this.unlinkDocumentBtn.disable();
            }
            if (this.blockunblockbtn) {
               this.blockunblockbtn.disable();
            }
             if (this.closedocumentbtn) {
               this.closedocumentbtn.disable();
            }
        }
        if(this.sm.getCount()==1) {
            if (this.estimateShippingCostBtn) {
                this.estimateShippingCostBtn.enable();
            }
            if(this.pendingapproval) {
                if(rec[0].data.deleted)
                    {
                    if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId && rec[0].data.createdbyid == loginid ){
                        this.editBttn.enable();
                    }
                     this.approveInvoiceBttn.disable();  
                     this.rejectInvoiceBttn.disable();
                        this.deleteMenu.enable();
                  }else{
                    if(this.editBttn && this.moduleid!=Wtf.Acc_Invoice_ModuleId && this.moduleid!=Wtf.Acc_Purchase_Order_ModuleId && this.moduleid != Wtf.Acc_Vendor_Invoice_ModuleId && this.moduleid!=Wtf.Acc_Sales_Order_ModuleId && this.moduleid != Wtf.Acc_Vendor_Quotation_ModuleId && this.moduleid != Wtf.Acc_Customer_Quotation_ModuleId && this.moduleid!=Wtf.Acc_FixedAssets_Purchase_Order_ModuleId && this.moduleid!=Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId){
                        this.editBttn.disable();
                    }
                      this.approveInvoiceBttn.enable();  
                      this.rejectInvoiceBttn.enable();
                      if(this.deleteMenu)
                        this.deleteMenu.disable();
                   }
            }
            /*
             * Close SO / PO manually and  block button need to be disabled when in pending approval window
             */
            if ((this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId) && this.pendingapproval) {
                this.closedocumentbtn.disable();
                this.blockunblockbtn.disable();
            }             
             if (this.linkAdvancePaymentbtn &&  rec[0]!=undefined && rec[0].data.isLinkedTransaction==false  ) {  
                    this.linkAdvancePaymentbtn.enable();
                }
//            if(!this.isQuotation) {             //ERM-145
                this.approvalHistoryBtn.enable();
//            }
//            if(!this.isQuotation) {
                this.linkinfoViewBtn.enable();    //linkinfo button is enabled if it is not quotation
//            }  
            if (this.relatedTransactionsBtn) {
                this.relatedTransactionsBtn.enable();
            }
          
            if(this.agedDetailsBtn)this.agedDetailsBtn.enable();             
        } else {
            if (this.estimateShippingCostBtn) {
                this.estimateShippingCostBtn.disable();
            }
              if (this.linkAdvancePaymentbtn ) {
                    this.linkAdvancePaymentbtn.disable();
                }
            if(this.pendingapproval) {
                /* if other than Sales invoice or Purchase Invoice disable the *approveInvoiceBttn* */
                if(!(this.moduleid==Wtf.Acc_Invoice_ModuleId ||this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)){
                    this.approveInvoiceBttn.disable();
                }
                this.rejectInvoiceBttn.disable();
                var deletedCount=0;
                for(cnt=0;cnt<arr.length;cnt++){
                    if(arr[cnt].data.deleted) {
                        deletedCount++;
                    }
                }
                if(deletedCount==arr.length && this.deleteMenu){
                    this.deleteMenu.enable();
                }else if(this.deleteMenu){
                    this.deleteMenu.disable();
                }
//                if(rec.length>0){
//                    for(var i=0;i<rec.length;i++){
//                        if(!rec[i].data.deleted){
//                            this.rejectInvoiceBttn.enable();  // Reject button will be enabled if atleast on of the selected record is non-rejected/non-deleted
//                        }
//                    }
//                } else {
//                    this.rejectInvoiceBttn.disable();
//                    this.approveInvoiceBttn.disable();
//                }
            }
//            if(!this.isQuotation) {                   //ERM-145
                this.approvalHistoryBtn.disable();
//            }
//            if(!this.isQuotation) {
                this.linkinfoViewBtn.disable();
//            } 

            if(this.relatedTransactionsBtn){
               this.relatedTransactionsBtn.disable(); 
            }
            
            if(this.agedDetailsBtn)this.agedDetailsBtn.disable();            
        }
        if(this.sm.getCount()>=1){
            if(this.singlePrint)this.singlePrint.enable();
//            if(this.singleRowPrint)this.singleRowPrint.enable();
//            if(this.jobOrderPrint)this.jobOrderPrint.enable();
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                var singlePersonID= "";
                var formflag = true;
                var arr = this.sm.getSelections();
                for(var cnt=0;cnt<arr.length;cnt++){
                    //Only those invoices which has status "Pending".
                    if((arr[cnt] && arr[cnt].data.formstatus =="Submitted" || arr[cnt] && arr[cnt].data.formstatus =="NA" )&& arr[cnt].data.deleted == false) {
                        formflag = false;
                    }
                    singlePersonID = arr[0].data.personid;
                    // Records of only Single Vendor/Customer.
                    if((arr[cnt] && arr[cnt].data.personid != singlePersonID) && arr[cnt].data.deleted == false) {
                        formflag = false;
                    }
                }
                if(this.FormDetailsWindowBtn ){
                    if(formflag){
                        this.FormDetailsWindowBtn.enable();             
                    }else if(this.FormDetailsWindowBtn){
                        this.FormDetailsWindowBtn.disable();            
                    }
                }
            }
        }else{
            if(this.singlePrint)this.singlePrint.disable();
//            if(this.singleRowPrint)this.singleRowPrint.disable();
//            if(this.jobOrderPrint)this.jobOrderPrint.disable();
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.FormDetailsWindowBtn)this.FormDetailsWindowBtn.disable();
            /*if selections count ==0 then disable the below button*/
            if (this.approveInvoiceBttn) {
                this.approveInvoiceBttn.disable();
            }
        }
       
        if(this.sm.getCount()==1){
            if(this.veiwVesions)this.veiwVesions.enable();
        }else{
            if(this.veiwVesions)this.veiwVesions.disable();
        }
       
        if(this.operationType==Wtf.autoNum.Invoice || this.operationType==Wtf.autoNum.GoodsReceipt || this.operationType==Wtf.autoNum.BillingInvoice || this.operationType==Wtf.autoNum.BillingGoodsReceipt) {
            if(this.paymentButton != undefined) {
                if(this.sm.getCount()==1 && rec[0].data.amountdue!=0 && rec[0].data.incash != true && rec[0].data.deleted != true){
                    this.paymentButton.enable();this.GIROFileButton.enable();
                }else if(this.sm.getCount()>=1 && this.typeEditor.getValue()==6){
                    this.paymentButton.disable();this.GIROFileButton.enable();
                } else {
                    this.paymentButton.disable();this.GIROFileButton.disable();
                }
            }
        }

        if(this.operationType==Wtf.autoNum.Invoice || this.operationType==Wtf.autoNum.GoodsReceipt || this.operationType==Wtf.autoNum.BillingInvoice ||  this.operationType==Wtf.autoNum.SalesOrder || this.operationType==Wtf.autoNum.BillingSalesOrder) {
            if(this.RepeateInvoice != undefined) {
                if((this.sm.getCount()==1 && rec[0].data.incash != true && rec[0].data.deleted != true) || (this.sm.getCount()==1 && this.businessPerson=="Customer" && this.isOrder)){
                    this.RepeateInvoice.enable();
                } else {
                    this.RepeateInvoice.disable();
                }
            }
        }
        // disable 'Copy' & 'View Pending Approval' buttons if user is viewing 'Oustanding PO(s)'
//        if(rec.data.isNormalTransaction){
            if (this.index == 7){
                if(this.copyInvBttn)this.copyInvBttn.disable();
                this.pendingApprovalBttn.disable();
            }else{
//                if(this.copyInvBttn)this.copyInvBttn.enable();
                this.pendingApprovalBttn.enable();
            } 
                /*
                 *Disable Delete Button When Select Opening trasection
                 */
            if(this.deleteMenu && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId)) {
            var checkIsOpeningTrasection=false;
            var isRepeated=false;
            for(var i=0;i<arr.length;i++){
                if(arr[i].data.isOpeningBalanceTransaction){
                    checkIsOpeningTrasection=true;
                    break;
                }
                if (arr[i].data.isRepeated) {
                    isRepeated = true;
                    break;
                }
            }
            if (checkIsOpeningTrasection) {
                this.deleteMenu.disable();
                this.email.disable();
            } else {
//                this.deleteMenu.enable(); //ERP-38860 [Sales Invoice]: In pending approval tab Delete button should be disable.
                //if(this.sm.getCount()==1 && rec[0].data.deleted != true)   //disable the email button in Purchase/Sales Invoice if 0 or more than one rows are selected in
                if(rec[0]!=undefined && rec[0]!="undefined" && rec[0].data!=undefined && rec[0].data!="undefined"){
                    if (rec[0].data.deleted != true && (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.sm.getCount() == 1))   //disable the email button in Purchase/Sales Invoice if 0 or more than one rows are selected in 
                    { 
                      if(!this.isDraft){
                        this.email.enable();
                      }
                    }    
                }
            }   
            if (isRepeated) {
                /*
                 *commented below code to not to disable print/print(records),export/export(records) 
                 *buttons on selecting recurring invoice
                 **/
//                if (this.exportButton)
//                    this.exportButton.disable();
//                if (this.printButton)
//                    this.printButton.disable();
//                if (this.singlePrint)
//                    this.singlePrint.disable();
//                if (this.singleRowPrint)
//                    this.singleRowPrint.disable();
//                if (this.jobOrderPrint)
//                    this.jobOrderPrint.disable();
            } else {
//                if (this.exportButton)
//                    this.exportButton.enable();
//                if (this.printButton)
//                    this.printButton.enable();
//                if (this.jobOrderPrint)
//                    this.jobOrderPrint.enable();
            } 
        } else if(this.deleteMenu == undefined && this.email){ 
                if (rec[0] != undefined && rec[0] != "undefined" && rec[0].data != undefined && rec[0].data != "undefined") {
                    if (rec[0].data.deleted != true && (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.sm.getCount() == 1))    
                    {
                     if (!this.isDraft) {
                        this.email.enable();
                      }
                    }
                }
            } 
            if(this.sm.getCount()!=1 && CompanyPreferenceChecks.isPostingDateCheck()){
                this.approveInvoiceBttn.disable();
            }
//        }/
    },
    loadParmStore:function(){
        var p = this.pP.combo!=undefined?this.pP.combo.value:30;
        if (this.moduleid ==Wtf.Acc_Invoice_ModuleId && this.pendingapproval){
            this.typeEditor.setValue(this.viewoption.normalinvoice);
        }else{
            this.typeEditor.setValue(0);
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            if(this.FormType != undefined){
                this.FormType.setValue(0);//By default, set To All
            }
            if(this.vatCommodityCombo != undefined){
                this.vatCommodityCombo.setValue('all');//By default, set To All
            }
            if(this.FormStatus != undefined){
                this.FormStatus.setValue(0);//By default, set To All
            }
        }
        if (!(Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId == this.moduleid || Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId == this.moduleid
                || Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId == this.moduleid || Wtf.Acc_FixedAssets_RFQ_ModuleId == this.moduleid
                || Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId == this.moduleid || Wtf.Acc_FixedAssets_Purchase_Order_ModuleId == this.moduleid
                )) {
            if (this.invID == null && !this.isSalesCommissionStmt) {
                this.Store.load({params: {start: 0, limit: p}});
            }
            WtfComMsgBox(29, 4, true);
        }
        this.Store.on('load',this.expandRow, this);
            this.Store.on('datachanged', function() {
                if(this.invID==null){
                        var p =  this.pP.combo!=undefined?this.pP.combo.value:30;
                        this.quickPanelSearch.setPage(p);
                        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
                        this.expandButtonClicked = false;
                        this.expander.resumeEvents('expand');           // event is suspended while expanding all records.
                }
                Wtf.MessageBox.hide();
            }, this);
         
        //}
    },
    handleResetClick:function(){
            if(this.quickPanelSearch.getValue()){
                this.quickPanelSearch.reset();
                this.loadStore();
                this.Store.on('load',this.storeloaded,this);
            }else{
                if(this.isRequisition || this.isRFQ){//for Purchase Requisition,RFQ
                    this.startDate.setValue(WtfGlobal.getDates(true));
                    this.endDate.setValue(WtfGlobal.getDates(false));
                    this.loadStore();
                }
                else if(this.isSalesCommissionStmt){
                   this.startDate.setValue(WtfGlobal.getDates(true));
                   this.endDate.setValue(WtfGlobal.getDates(false));
                   this.userds.load();
                   this.loadStore(); 
                }
            }
        },
   
    handleArchieveQuate : function(){
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoarchiveselected")+this.label+"?",function(btn){
        if(btn=="yes") {
            var formRecord = this.grid.getSelectionModel().getSelected();
            Wtf.Ajax.requestEx({
                url: this.isCustomer ? "ACCSalesOrder/archieveQuotations.do" : "ACCPurchaseOrder/archieveQuotations.do",
                params: {
                    billid : formRecord.data.billid,
                    billno : formRecord.data.billno,
                    isFixedAsset: this.isFixedAsset
                }
            },this,this.genSuccessResponseQuote,this.genFailureResponseQuote);
           
        }
    }, this)
},

    genSuccessResponseQuote : function(response){
        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
        this.loadStore();
        }, this);
    },
    genFailureResponseQuote : function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
    },

    handleUnArchieveQuate : function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttounarchiveselected")+this.label+"?",function(btn){
            if(btn=="yes") {
                var formRecord = this.grid.getSelectionModel().getSelected();
                Wtf.Ajax.requestEx({
                    url: this.isCustomer ? "ACCSalesOrder/unArchieveQuotations.do" : "ACCPurchaseOrder/unArchieveQuotations.do",
                    params: {
                        billid : formRecord.data.billid,
                        billno : formRecord.data.billno,
                        isFixedAsset: this.isFixedAsset
                    }
                },this,this.genSuccessResponseQuote,this.genFailureResponseQuote);

            }
        }, this)
    },
   
   
   approvePendingInvoice : function(){
    var formRecord = this.grid.getSelectionModel().getSelected();
   
    if (this.isRequisition && Wtf.account.companyAccountPref.activatebudgetingforPR) {
        var isRuleExist = false;
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/isRuleExistsForRequisition.do",
            params: {
                totalAmount: formRecord.data.amountinbase,
                approvestatuslevel: formRecord.data.approvestatuslevel
            }
        }, this, function(response) {
            if (response.success) {
                isRuleExist = response.isRuleExist;
                if (isRuleExist) {
                    this.finallyApprovePendingInvoice();
                } else {
                    this.checkForBudgetLimitActivated(formRecord);
                }
            }
        }, this.genFailureResponseApproveInv);
    } else {
        this.finallyApprovePendingInvoice();
    }
},

checkForBudgetLimitActivated: function(formRecord) {
    this.BudgetSetForDepartment = 0;
    this.BudgetSetForDepartmentAndProduct = 1;
    this.BudgetSetForDepartmentAndProductCategory = 2;
       
    if (Wtf.account.companyAccountPref.activatebudgetingforPR) { // Approval for budgeting amount is activated
        var budgetType = Wtf.account.companyAccountPref.budgetType;
        if (budgetType == this.BudgetSetForDepartment) { // If budgeting is applied upon Department
            this.checkIfBugetLimitExceedingForDepartment(formRecord);
        } else if (budgetType == this.BudgetSetForDepartmentAndProduct) { // If budgeting is applied upon Department and specific product
            this.checkIfBugetLimitExceedingForDepartmentAndProduct(formRecord);
        } else { // If budgeting is applied upon Department and specific category of product
            // future enhancement - P2
        }
    }
},

checkIfBugetLimitExceedingForDepartmentAndProduct: function(formRecord) {
    Wtf.Ajax.requestEx({
        url: "ACCPurchaseOrderCMN/checkIfBugetLimitExceeding.do",
        params: {
            isApprove: true,
            billid: formRecord.data.billid,
            budgetingType: this.BudgetSetForDepartmentAndProduct,
            requisitionDate: WtfGlobal.convertToGenericDate(formRecord.data.date),
            currencyID: formRecord.data.currencyid
        }
    }, this, function(response) {
        if (response.success) {
            var isBudgetExceeding = response.isBudgetExceeding;

            if (isBudgetExceeding) {
                if (Wtf.account.companyAccountPref.budgetwarnblock == 0) { // Ignore case
                    this.finallyApprovePendingInvoice();
                } else if (Wtf.account.companyAccountPref.budgetwarnblock == 1) { // Warn case

                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.budgetIsExceedingDoYouWantToContinue"), function(btn) {
                        if (btn != "yes") {
                            return;
                        } else {
                            this.finallyApprovePendingInvoice();
                        }
                    }, this);

                }else if (Wtf.account.companyAccountPref.budgetwarnblock == 2) { // Block case
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),  WtfGlobal.getLocaleText("acc.field.budgetIsExceedingSoYouCannotProceed")], 2);
                    return;
                }
            } else {
                this.finallyApprovePendingInvoice();
            }
        }
    }, this.genFailureResponseApproveInv);
},

checkIfBugetLimitExceedingForDepartment: function(formRecord) {
                               
    Wtf.Ajax.requestEx({
        url: "ACCPurchaseOrderCMN/checkIfBugetLimitExceeding.do",
        params: {
            isApprove: true,
            billid: formRecord.data.billid,
            requisitionTotalAmount: formRecord.data.amount,
            budgetingType: this.BudgetSetForDepartment,
            requisitionDate: WtfGlobal.convertToGenericDate(formRecord.data.date),
            currencyID: formRecord.data.currencyid
        }
    }, this, function(response) {
        if (response.success) {
            var isBudgetExceeding = response.isBudgetExceeding;

            if (isBudgetExceeding) {
                if (Wtf.account.companyAccountPref.budgetwarnblock == 0) { // Ignore case
                    this.finallyApprovePendingInvoice();
                }else if (Wtf.account.companyAccountPref.budgetwarnblock == 1) { // Warn case

                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.budgetIsExceedingDoYouWantToContinue"), function(btn) {
                        if (btn != "yes") {
                            return;
                        }else {
                            this.finallyApprovePendingInvoice();
                        }
                    }, this);

                }else if (Wtf.account.companyAccountPref.budgetwarnblock == 2) { // Block case
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),  WtfGlobal.getLocaleText("acc.field.budgetIsExceedingSoYouCannotProceed")], 2);
                    return;
                }
            } else {
                this.finallyApprovePendingInvoice();
            }
        }
    }, this.genFailureResponseApproveInv);
},

finallyApprovePendingInvoice: function() {
    var formRecord = this.grid.getSelectionModel().getSelected();
    var approvalstatusint = formRecord.data.approvalstatusint;
   
    //Calculate approval permission of selected record
    var alertFlag = false;
    if(approvalstatusint == 1){
        if(this.isOrder) {
            if(this.isCustomer) {
                if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.salesorderapprovelevelone)) {
                    alertFlag = true;
                }
            } else {
                if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.purchaseorderapprovelevelone)) {
                    alertFlag = true;
                }
            }
           
        } else {
            if(this.isCustomer) {
                if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.customerinvoiceapprovelevelone)) {
                    alertFlag = true;
                }
            } else {
                if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.vendorinvoiceapprovelevelone)) {
                    alertFlag = true;
                }
            }
        }
        if(alertFlag){
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.YoudonthavepermissionforLevel1approvalof")+this.label+WtfGlobal.getLocaleText("acc.field.Pleasecontactcompanyadministrotor"));
            return;
        }
    } else if(approvalstatusint == 2) {
        if(this.isOrder) {
            if(this.isCustomer) {
                if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.salesorderapproveleveltwo)) {
                    alertFlag = true;
                }
            } else {
                if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.purchaseorderapproveleveltwo)) {
                    alertFlag = true;
                }
            }
        } else {
            if(this.isCustomer) {
                if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.customerinvoiceapproveleveltwo)) {
                    alertFlag = true;
                }
            } else {
                if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.vendorinvoiceapproveleveltwo)) {
                    alertFlag = true;
                }
            }
        }
        if(alertFlag){
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.YoudonthavepermissionforLevel2approvalof")+this.label+WtfGlobal.getLocaleText("acc.field.Pleasecontactcompanyadministrotor"));
            return;
        }
    }
   
   
   
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+this.label+"?",function(btn){
       
        if(btn=="yes") {
            var URL = this.isCustomer? "ACCInvoice/approvePendingInvoice.do" : "ACCGoodsReceipt/approvePendingInvoice.do";
            if(this.isOrder) {
                URL = this.isCustomer? "ACCSalesOrder/approvePendingOrders.do" : "ACCPurchaseOrder/approvePendingOrders.do";
            }
            if(this.isRequisition) {
                URL = "ACCPurchaseOrder/approvePendingRequisition.do";
            }
            var winTitle=WtfGlobal.getLocaleText("acc.field.ApprovePendingInvoice");
            if(this.isRequisition && this.pendingapproval){
                winTitle=WtfGlobal.getLocaleText("acc.field.ApprovePendingpurchaserequisition");
            }
            var formRecord = this.grid.getSelectionModel().getSelected();
            this.remarkWin = new Wtf.Window({
                height : 270,
                width : 360,
                maxLength : 1000,
                title : winTitle,
                bodyStyle : 'padding:5px;background-color:#f1f1f1;',
                autoScroll : true,
                allowBlank : false,
                layout : 'border',
                items : [{
                        region : 'north',
                        border:false,
                        height:70,
                        bodyStyle : 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                        html:getTopHtml(WtfGlobal.getLocaleText("acc.field.ApprovePending") +this.label ,WtfGlobal.getLocaleText("acc.field.ApprovePending") +this.label +" <b>"+formRecord.data.billno+"</b>"  ,"../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                    },{
                        region : 'center',
                        border:false,
                        layout : 'form',
                        bodyStyle : 'padding:5px;',
                        items : [this.remarkField =new Wtf.form.TextArea({
                                fieldLabel : WtfGlobal.getLocaleText("acc.field.AddRemark*"),
                                width : 200,
                                height : 100,
                                allowBlank : false,
                                maxLength : 1024
                            })]
                    }],
                modal : true,
                buttons : [{
                        text: WtfGlobal.getLocaleText("acc.Lease.addAttach"),
                        scope: this,
                        hidden: !this.isRequisition, // Show only for Purchase Requisition Module
                        handler: this.showApprovalAttachWindow,
                        iconCls: getButtonIconCls(Wtf.etype.save)
                    }, {
                        text : WtfGlobal.getLocaleText("acc.cc.24"),
                        scope : this,
                        id: "Approvebtn"+this.id,
                        handler : function(){
//                            if(this.remarkField.getValue().trim() == ""){
//                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseenterremark")],2);
//                                return;
//                            }
//                           
//                            if(!this.remarkField.isValid()) {
//                                this.remarkField.markInvalid(WtfGlobal.getLocaleText("acc.field.Maximumlengthofthisfieldis1024"));
//                                return;
//                            }
                            Wtf.getCmp("Approvebtn"+this.id).disable();
                            Wtf.Ajax.requestEx({
                                url:URL,
                                params: {
                                    billid : formRecord.data.billid,
                                    billno: formRecord.data.billno,
                                    amount:formRecord.data.amount,
                                    incash : formRecord.data.incash,
                                    isbilling : formRecord.data.withoutinventory,
                                    remark : this.remarkField.getValue(),
                                    docID: this.docID,
                                    isFixedAsset: this.isFixedAsset
                                }
                            },this,this.genSuccessResponseApproveInv,this.genFailureResponseApproveInv);
                        }
                    },{
                        text : WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope : this,
                        handler : function(){
                            this.remarkWin.close();
                        }
                    }]
            });
            this.remarkWin.show();
           
           
        }
    }, this)
},
   
    genSuccessResponseApproveInv : function(response){
        this.remarkWin.close();
        if(this.isRequisition){
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
                this.loadStore();
            if(Wtf.getCmp("PurchaseRequisitionList")!=null && Wtf.getCmp("PurchaseRequisitionList")!=undefined){//to refresh the grid of PRReport
                Wtf.getCmp("PurchaseRequisitionList").Store.reload();//loading the purchaserequisitionreport if tab is open
            }
              }, this);
        }else{
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),this.label+" " + WtfGlobal.getLocaleText("acc.field.hasbeenapprovedsuccessfully")],2+1);
        this.loadStore();
            }
    },
    genFailureResponseApproveInv : function(response){
        Wtf.getCmp("Approvebtn"+this.id).enable();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
    },
   
    approvePendingTransactions : function(){
        var formRecord = this.grid.getSelectionModel().getSelected();
        
        if ((formRecord.data.status != "" && formRecord.data.status == "Closed") && ( this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId) ) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.common.cannotapprovereject")], 2);
            return; 
        }
        var recurredInvoiceApproverID='';
        if(this.moduleid==Wtf.Acc_Invoice_ModuleId && this.pendingapproval && formRecord.data.allowEditingRecurredDocuments !=undefined && formRecord.data.allowEditingRecurredDocuments){
            recurredInvoiceApproverID=formRecord.data.editedRecurredDocumentsApprover;
        }
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected") +" "+ this.label + "?", function(btn) {
            if (btn == "yes") {
                var URL = "",winTitle="";
                if(this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId){
                    URL="ACCSalesOrder/approveCustomerQuotation.do";
                    winTitle=WtfGlobal.getLocaleText("acc.field.ApprovependingCustomerQuotation");
                   
                }else if(this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) {
                    URL="ACCPurchaseOrder/approveVendorQuotation.do";
                    winTitle= WtfGlobal.getLocaleText("acc.field.ApprovependingVendorQuotation");
                   
                } else if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId){
                    URL="ACCSalesOrderCMN/approveSalesOrder.do";
                    winTitle= WtfGlobal.getLocaleText("acc.field.ApprovependingSalesOrder");
                   
                } else if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_Purchase_Order_ModuleId){
                    URL="ACCPurchaseOrder/approvePurchaseOrder.do";
                    winTitle= this.moduleid==Wtf.Acc_FixedAssets_Purchase_Order_ModuleId ? WtfGlobal.getLocaleText("acc.field.ApprovependingAssetPurchaseOrder") :WtfGlobal.getLocaleText("acc.field.ApprovependingPurchaseOrder");
                   
                } else if(this.moduleid==Wtf.Acc_Invoice_ModuleId){
                    URL="ACCInvoice/approveInvoice.do";
                    winTitle=WtfGlobal.getLocaleText("acc.field.ApprovePendingInvoice");
                   
                }else if(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId){
                    URL="ACCGoodsReceipt/approvegr.do";
                    winTitle=WtfGlobal.getLocaleText("acc.field.ApprovependingVendorInvoice");
                }    
                var formRecord = this.grid.getSelectionModel().getSelected();
                this.remarkWindow = new Wtf.Window({
                    height: 270,
                    width: 360,
                    maxLength: 1000,
                    title: winTitle,
                    bodyStyle: 'padding:5px;background-color:#f1f1f1;',
                    autoScroll: true,
                    //allowBlank: false,
                    layout: 'border',
                    items: [{
                            region: 'north',
                            border: false,
                            height: 70,
                            bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.ApprovePending") +" "+ this.label, WtfGlobal.getLocaleText("acc.field.ApprovePending") +" "+ this.label + " <b>" + formRecord.data.billno + "</b>", "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                        }, {
                            region: 'center',
                            border: false,
                            layout: 'form',
                            bodyStyle: 'padding:5px;',
                            items: [this.remarkField = new Wtf.form.TextArea({
                                    fieldLabel: WtfGlobal.getLocaleText("acc.field.AddRemark*"),
                                    width: 200,
                                    height: 100,
                                    //allowBlank: false,
                                    maxLength: 1024
                                })]
                        }],
                    modal: true,
                    buttons: [{
                            text: WtfGlobal.getLocaleText("acc.cc.24"),
                            id: "approvePendingTransactionsBtn" + this.id,
                            scope: this,
                            handler: function() {
//                                if (this.remarkField.getValue().trim() == "") {
//                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseenterremark")], 2);
//                                    return;
//                                }
//
//                                if (!this.remarkField.isValid()) {
//                                    this.remarkField.markInvalid(WtfGlobal.getLocaleText("acc.field.Maximumlengthofthisfieldis1024"));
//                                    return;
//                                }
                                Wtf.getCmp("approvePendingTransactionsBtn" + this.id).disable();
                               
                                Wtf.Ajax.requestEx({
                                    url: URL,
                                    params: {
                                        billid: formRecord.data.billid,
                                        totalorderamount : formRecord.data.amountinbase,
                                        amount:formRecord.data.amount,
                                        isbilling: formRecord.data.withoutinventory,
                                        remark: this.remarkField.getValue(),
                                        isFixedAsset: this.isFixedAsset,
                                        isLeaseFixedAsset: this.isLeaseFixedAsset,
                                        recurredinvoiceapproverid:recurredInvoiceApproverID,
                                        customer:formRecord.data.personid,
                                        totalSUM:formRecord.data.amount,
                                        isOrder:this.isOrder,
                                        termid:formRecord.data.termid,
                                        profitMargin:formRecord.data.totalprofitmargin,
                                        profitMarginPercent:formRecord.data.totalprofitmarginpercent,
                                        currencyid:formRecord.data.currencyid,
                                        invoiceNo:formRecord.data.billno,
                                        billdate:formRecord.data.date,
                                        term:formRecord.data.termid,
                                        customerid:formRecord.data.personid
                                        
            }
                                }, this, this.genSuccessRespApproveTransaction, this.genFailureRespApproveTransaction);
                            }
                        }, {
                            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                            scope: this,
                            handler: function() {
                                this.remarkWindow.close();
                            }
                        }]
                });
                this.remarkWindow.show();
            }
        }, this)
    },
    /* 
     * Below function is used to approve transaction in batch.
     * 
     * */
    BatchApprovePendingTransactions : function(){
        var formRecords = this.grid.getSelectionModel().getSelections();
        var formRecordData = formRecords[0].data;
        var dataArray = [];
        var rec={};
        var pendingInvoices='';
        var recurredInvoiceApproverID = '';
        /*For loop is used to create json array to send multiple transaction details*/
        for (var i = 0; i < formRecords.length; i++) {
            recurredInvoiceApproverID='';
            if (this.moduleid == Wtf.Acc_Invoice_ModuleId && this.pendingapproval && formRecords[i].data.allowEditingRecurredDocuments != undefined && formRecords[i].data.allowEditingRecurredDocuments) {
                recurredInvoiceApproverID = formRecords[i].data.editedRecurredDocumentsApprover;
            }
            var temp1 = {
                billid: formRecords[i].data.billid,
                totalorderamount: formRecords[i].data.amountinbase,
                amount: formRecords[i].data.amount,
                isbilling: formRecords[i].data.withoutinventory,
                recurredinvoiceapproverid: recurredInvoiceApproverID
            }
            dataArray.push(temp1);
            pendingInvoices+=formRecords[i].data.billno+",";
        }
        
        if (dataArray.length > 0){
            rec.data = JSON.stringify(dataArray);
            rec.isFixedAsset= this.isFixedAsset;
            rec.isLeaseFixedAsset= this.isLeaseFixedAsset;
        }
        
        if(pendingInvoices!=''&& pendingInvoices.length>0){
            pendingInvoices=pendingInvoices.substring(0,pendingInvoices.length-1);
        }
        /*showing approval window */
        var itemsArr=[];
        
    this.remarkField = new Wtf.form.TextArea({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.AddRemark*"),
        width: 200,
        height: 100,
        maxLength: 1024
    });
    
    var maxDate = (Wtf.serverDate > this.grid.getSelectionModel().getSelected().data.date) ? Wtf.serverDate : this.grid.getSelectionModel().getSelected().data.date;
    this.postingDate=new Wtf.ExDateFieldQtip({
        name:'postingDate',
        id: 'postingDate',
        fieldLabel: WtfGlobal.getLocaleText("acc.pending.pstingdate"),
        width: 200,
        height: 100,
        maxLength: 1024,
        format:WtfGlobal.getOnlyDateFormat(),
        value:new Date(this.grid.getSelectionModel().getSelected().data.date),
        maxValue:maxDate,
        minValue:this.grid.getSelectionModel().getSelected().data.date
    });
    
    itemsArr.push(this.remarkField);
    var height=270;
    if(CompanyPreferenceChecks.isPostingDateCheck() && formRecordData != undefined && formRecordData.isFinalLevelApproval){
        itemsArr.push(this.postingDate);
        height=300;
    }
    this.postingDate.on('change', function (scope, newVal, oldVal) {
        var record = this.grid.getSelectionModel().getSelected();
        var creationDate = record.data.date;
            var todaysDate = Wtf.serverDate;
            if(!isFromActiveDateRange(newVal)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.pending.pstingdateactivedateerrormsg")], 2);
                this.postingDate.setValue(oldVal);
            } else if (newVal < creationDate) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.pending.pstingdateerrormsg")], 2);
                this.postingDate.setValue(oldVal);
            } else if (newVal > todaysDate) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.pending.pstingdateerrortodaymsg")], 2);
                this.postingDate.setValue(oldVal);
            }
    }, this);
    
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected") +" "+ this.label + "?", function(btn) {
            if (btn == "yes") {
                var URL = "",winTitle="";
                if(this.moduleid==Wtf.Acc_Invoice_ModuleId){
                    URL="ACCInvoice/approveInvoice.do";
                    winTitle=WtfGlobal.getLocaleText("acc.field.ApprovePendingInvoice");
                   
                } else if(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId){
                    URL="ACCGoodsReceipt/approvegr.do";
                    winTitle=WtfGlobal.getLocaleText("acc.field.ApprovependingVendorInvoice");
                }    
                this.remarkWindow = new Wtf.Window({
                    height: height,
                    width: 360,
                    maxLength: 1000,
                    title: winTitle,
                    bodyStyle: 'padding:5px;background-color:#f1f1f1;',
                    autoScroll: true,
                    layout: 'border',
                    items: [{
                    region: 'north',
                    border: false,
                    height: 70,
                    bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.field.ApprovePending") +" "+ this.label, WtfGlobal.getLocaleText("acc.field.ApprovePending") +" "+ this.label + " <b>" + pendingInvoices + "</b>", "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                }, {
                    region: 'center',
                    border: false,
                    layout: 'form',
                    bodyStyle: 'padding:5px;',
                    items: itemsArr
                }],
                    modal: true,
                    buttons: [{
                            text: WtfGlobal.getLocaleText("acc.cc.24"),
                            id: "approvePendingTransactionsBtn" + this.id,
                            scope: this,
                            handler: function() {
                                if (this.postingDate.isValid()) {
                                    Wtf.getCmp("approvePendingTransactionsBtn" + this.id).disable();
                                   rec.remark=this.remarkField.getValue();
                                   rec.postingDate=WtfGlobal.convertToGenericDate(this.postingDate.getValue());
                                    Wtf.Ajax.requestEx({
                                        url: URL,
                                        params: rec
                                    }, this, this.genSuccessRespApproveTransaction, this.genFailureRespApproveTransaction);
                                } else {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.pending.pstingdatevalidationmsg")], 2);
                                }
                            } 
                        }, {
                            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                            scope: this,
                            handler: function() {
                                this.remarkWindow.close();
                            }
                        }]
                });
                this.remarkWindow.show();
            }
        }, this)
    },
    
    genSuccessRespApproveTransaction: function(response) {
        this.remarkWindow.close();
        var formRecords = this.grid.getSelectionModel().getSelections();
        var msg = response.msg;
        /* if Single selections then show below message*/
        if (formRecords.length == 1) {
              var thisObj = this; //storing current object reference in a variable to access it inside the function of WtfComMsgBox below
              WtfComMsgBox([this.label, response.msg], 0, null, "", function (btn) {
                if (btn == "ok")
                    thisObj.loadStore(); //access the store using the variable declared outside because it is not accessible using 'this'
            }, this);      

         } else {
             /* if multiple selections then show below window*/
            this.batchApprovalTransWin = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                id: 'batchapprovaltranwin',
                closable: false,
                modal: true,
                bodyStyle: "border: 1px solid #b5b8c8;padding:5px",
                height: 150,
                resizable: false,
                buttonAlign: "center",
                autoScroll: true,
                border: false,
                buttons: [
                    {
                        text: "OK",
                        scope: this,
                        handler: function() {
                            this.loadStore();
                            this.batchApprovalTransWin.close();
                        }
                    }
                ]
            });
            this.batchApprovalTransWin.show();
            this.batchApprovalTransWin.body.dom.innerHTML = msg;
        }
    },
   
    genFailureRespApproveTransaction: function(response) {
        Wtf.getCmp("approvePendingTransactionsBtn" + this.id).enable();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")], 2);
    },
   
    storeloaded:function(store){
  //      this.hideLoading();
        this.quickPanelSearch.StorageChanged(store);
        
        if(this.Store.reader.jsonData.upsErrorJSON && this.Store.reader.jsonData.upsErrorJSON.ErrorCode) {
            var errormsg = WtfGlobal.getLocaleText("acc.pickpackship.upsErrorMsg")+": </br><br><b>"+WtfGlobal.getLocaleText("acc.pickpackship.upsErrorCode")+": </b>"+ this.Store.reader.jsonData.upsErrorJSON.ErrorCode;
            if(this.Store.reader.jsonData.upsErrorJSON.ErrorDescription) {
                errormsg = errormsg + "</br><br><b>"+WtfGlobal.getLocaleText("acc.pickpackship.upsErrorDescription")+": </b>"+ this.Store.reader.jsonData.upsErrorJSON.ErrorDescription;
            }
            if(this.Store.reader.jsonData.upsErrorJSON.ErrorSeverity) {
                errormsg = errormsg + "</br><br><b>"+WtfGlobal.getLocaleText("acc.pickpackship.upsErrorSeverity")+": </b>"+ this.Store.reader.jsonData.upsErrorJSON.ErrorSeverity;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), errormsg],2);
        }
    },
   
    viewCustomerQuotationVersions:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        var billid=formrec.get("billid");
        var isCustomer=this.businessPerson=="Customer"?true:false;
        if(this.isQuotation){
            callViewCustomerQuotationVersions(formrec,billid, 'ViewCustomerQuotationVersions'+billid,isCustomer,this.isLeaseFixedAsset);
        }
    },
    viewPurchaseOrderVersions: function () {
        var formrec = null;
        if (this.grid.getSelectionModel().hasSelection() == false || this.grid.getSelectionModel().getCount() > 1) {
            WtfComMsgBox(15, 2);
            return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        var billid = formrec.get("billid");
        var isCustomer = this.businessPerson == "Customer" ? true : false;
        callViewPurchaseOrderVersions(formrec, billid, 'ViewPurchaseOrderVersions' + billid, isCustomer, this.isLeaseFixedAsset);
    },
    
    viewTransection:function(grid, rowIndex, columnIndex){
    var formrec=null;
    if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
        WtfComMsgBox(15,2);
        return;
    }
    formrec = this.grid.getStore().getAt(rowIndex);
    var incash=formrec.get("incash");
    var billid=formrec.get("billid");
    var isExpensiveInv = formrec.get("isexpenseinv");
    this.withInvMode = formrec.get("withoutinventory");
    if(this.isCustomer)
        if(incash &&!this.withInvMode)
        {
            callViewCashReceipt(formrec, 'ViewInvoice');
        }
//        else if(incash)
//        {
//            callViewBillingCashReceipt(formrec,billid, 'ViewBillingCSInvoice',true);
//        }
//        else if(this.withInvMode)
//        {
//            callViewBillingInvoice(formrec,billid, 'ViewBillingInvoice',false);
//        }
        else
        {
            if(this.isOrder){
                if(this.isLeaseFixedAsset){
                    callViewFixedAssetLeaseSalesOrder(true,formrec,billid, false,null,false,false,false,true);
                } else if(this.isConsignment){
                    callViewConsignmentRequest(true,formrec,billid, false,null,true,false,false,true,true);
                }else{
                    formrec.data.pendingapproval= this.pendingapproval;
                    if (this.isJobWorkOrderReciever) {
                        callViewSalesOrder(true,formrec,billid, false,undefined, false, false, false,true);
                    } else {
                        callViewSalesOrder(true,formrec,billid, false);
                    }
                }
            }else if(this.isQuotation){
                if(this.isLeaseFixedAsset){
                    callViewLeaseQuotation(true, billid, formrec, false,true);
                }else{
                    callViewQuotation(false,billid, formrec, true);
                }
            } else if(this.isFixedAsset ||this.isLeaseFixedAsset ||formrec.data.fixedAssetInvoice||formrec.data.fixedAssetLeaseInvoice){
                callViewFixedAssetInvoice(formrec, billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
            }else if(this.isConsignment){
                callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
            } else{
                callViewInvoice(formrec, 'ViewCashReceipt');
            }
        }
    else if(incash &&!this.withInvMode)
    {
        callViewPaymentReceipt(formrec, 'ViewPaymentReceipt',isExpensiveInv);
    }
//    else if(incash)
//    {
//        callViewBillingPaymentReceipt(formrec,billid, 'ViewBillingCSInvoice',true);
//    }
//    else if(this.withInvMode)
//    {
//        callViewBillingGoodsReceipt(formrec,billid, 'ViewBillingInvoice',false);
//    }
    else
    {
        if(this.isRequisition)
        {
            if (this.isFixedAsset) {
                callViewFixedAssetPurchaseReq(true,formrec,billid,undefined,true);
            } else {
                callViewPurchaseReq(true,formrec,billid);
            }
        }
        else if(this.isQuotation)
        {
            if (this.isFixedAsset) {
                callViewFixedAssetVendorQuotation(false, billid, formrec, true);
            } else {
                callViewVendorQuotation(false, billid, formrec, true);                           
            }
        }
        else  if(this.isOrder)
        {
            if (this.isFixedAsset) {
                callViewFixedAssetPurchaseOrder(true,formrec,billid,false,this,this.newtranType, true);
            } else {
                this.withInvMode = formrec.get("withoutinventory"); 
                if(!this.withInvMode){				// Without Inventory
                    if(this.isConsignment){
                        callViewConsignmentRequest(true,formrec,billid, false,null,true,false,false,true,false);
                        } else if (this.isJobWorkOrderReciever) {
                            callViewPurchaseOrder(true, formrec, billid, false, this, this.newtranType, undefined, undefined, undefined, true);
                        } else if (this.isSecurityGateEntry) {
                            callViewPurchaseOrder(true, formrec, billid, false, this, this.newtranType, undefined, undefined, undefined, undefined,true);
                        }else {
                            formrec.data.pendingapproval= this.pendingapproval;
                            callViewPurchaseOrder(true,formrec,billid,false,this,this.newtranType, true);
                        }
                }else{								// With Inventory
                    callViewBillingPurchaseOrder(true,formrec,billid,false,this,this.newtranType, false);
                }
            }
        }else if(this.isRFQ){
            if (this.isFixedAsset) {
                callViewFixedAssetRequestForQuotation(true,this.PR_IDS, this.PR_MEMOS,formrec,this.isFixedAsset);
            } else {
                callViewRequestForQuotation(true,this.PR_IDS, this.PR_MEMOS,formrec);
            }
        }else{
            if(this.isFixedAsset ||formrec.data.fixedAssetInvoice){
                 callViewFixedAssetGoodsReceipt(formrec, billid+'GoodsReceipt',false,isExpensiveInv,undefined,false,formrec.data.fixedAssetInvoice);
            }else if(this.isConsignment){
                callViewConsignmentGoodsReceipt(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
            }else{
                callViewGoodsReceipt(formrec, 'ViewGoodsReceipt',isExpensiveInv);
            }
        }
    }
},
   
checkDepreciationPostedOrNot:function(formrec,label,copyInv,isExpensiveInv,isLinkedTransaction){
    var url = '';
    if(this.isCustomer){
        url = 'ACCInvoiceCMN/isInvoicehasDepreciatedAsset.do';
    }else{
        url = 'ACCGoodsReceiptCMN/isInvoicehasDepreciatedORSoldAsset.do';
    }
       
    Wtf.Ajax.requestEx({
        url:url,
        params:{
            billid:formrec.get('billid')
        }
    },this,
    function(res,req){
        if(res.data.isInvoicehasDepreciatedAsset){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.AssetDepreciationhasbeenpostedusedinthisInvoiceSoitcannotbeEdit") ], 3);
        }else{
                if(this.isCustomer){
                    callEditFixedAssetInvoice(formrec, label+'Invoice',copyInv,undefined,false,this.isFixedAsset,this.isLeaseFixedAsset,undefined,isLinkedTransaction);
                }
                else{
                    callEditFixedAssetGoodsReceipt(formrec, label+'GoodsReceipt',copyInv,isExpensiveInv,undefined,false,this.isFixedAsset,isLinkedTransaction);
                }
            }
    },function(res,req){
           
        });
},

    /**
     * Function to check if the invoice is to be allowed to edit in case of Avalara Integration
     * If Avalara Integration is disabled, then 'editTransactionCheckBefore' function is called directly
     * @param {type} copyInv
     * @returns {undefined}
     */
    editTransactionCheckBeforeNew: function (copyInv) {
        if (Wtf.account.companyAccountPref.avalaraIntegration && (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId)) {
            /**
             * In case of Avalara Integration, we first check if invoice should be allowed to edit or not
             * After checking the same in function 'validateTransactionWithAvalaraAndCallBack', the function 'this.editTransactionCheckBefore' is called back
             */
            var selectedRec = this.grid.getSelectionModel().getSelected();
            if (selectedRec && selectedRec.data.isTaxCommittedOnAvalara) {
                validateTransactionWithAvalaraAndCallBack(this, selectedRec,copyInv);
            } else {
                this.editTransactionCheckBefore(copyInv);
            }
        } else {
            this.editTransactionCheckBefore(copyInv);
        }
    },

/**
 * Please note that this function is also called from function 'validateTransactionWithAvalaraAndCallBack' in AvalaraIntegration.js
 * @param {type} copyInv
 * @returns {undefined}
 */
editTransactionCheckBefore:function(copyInv){                          //check whether record from POS or not
   var formrec = this.grid.getSelectionModel().getSelected();
   if(formrec.data.isFromPOS){
       Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.249"),function(btn){
           if(btn=="yes") {
               this.editTransaction(copyInv);
           }
       },this);
   } else {
       this.editTransaction(copyInv);
   }
},

editTransaction:function(copyInv){
    var isLinkedTransaction = false;
    var formrec=null;
    if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
        WtfComMsgBox(15,2);
        return;
    }  
    var isEditForQuotation= true;
    if(copyInv!="" && copyInv!=null && copyInv!=undefined && copyInv){
        isEditForQuotation = false;
    }
    formrec = this.grid.getSelectionModel().getSelected();
    //If the Sales Invoice has a autogenerated Pick Pack DO then do not allow editing 
    if(!copyInv && (formrec.data.hasautogenpickpackdo==true && this.moduleid==Wtf.Acc_Invoice_ModuleId)&& this.grid.getSelectionModel().getCount()==1){
        WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),WtfGlobal.getLocaleText("acc.field.autogenpickpackship")],2);
        return;
    }
    var incash=formrec.get("incash");
    if((formrec.data.isLinkedTransaction != undefined && formrec.data.isLinkedTransaction) && !copyInv){
        isLinkedTransaction = true;
    }
       var isAllowToEdit = false;
        if (formrec.data.isAllowToEdit != undefined && formrec.data.isAllowToEdit != null && formrec.data.isAllowToEdit != "" && (this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid ==Wtf.Acc_Cash_Sales_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId)) { // is invoice is allow to edit
            isAllowToEdit = formrec.data.isAllowToEdit; // true when invoice is created using auto generate DO/GR option and SI/DO/PI/GR is not forward linked to any document
        }
        if(isAllowToEdit){
            isLinkedTransaction=false;
        }
        var isPaymentStatusCleared = false;//True When Cash Sales / Purchase  Cheque Detaisl -> Payment Status is cleared 
        if(formrec.data.isPaymentStatusCleared!=undefined && formrec.data.isPaymentStatusCleared!=null && formrec.data.isPaymentStatusCleared!="" &&(this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid ==Wtf.Acc_Cash_Sales_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId)){
            isPaymentStatusCleared = formrec.data.isPaymentStatusCleared;
        }
        if(isPaymentStatusCleared && !copyInv){
                isLinkedTransaction = true;
        }
    var label=copyInv?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.product.edit");
    var findtablabel=copyInv?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.common.edit");
    var billid=formrec.get("billid");
    this.withoutInvMode = formrec.get("withoutinventory");
    var isExpensiveInv = formrec.get("isexpenseinv");
    var isSelfBilledInvoice = formrec.get("selfBilledInvoice");
    var isreval=formrec.get('isreval');
    var paymentStatus=formrec.get('paymentStatus');
    var isTaxPaidTransaction=formrec.get('isTaxPaidTransaction');
    label=label+billid;
    findtablabel=findtablabel+billid;
    if(isreval!=undefined && isreval!=1){
        if(this.isCustomer){
//            if(this.withoutInvMode){
//                if(incash)
//                    callEditBillingSalesReceipt(formrec, label+'BillingCSInvoice',copyInv);
//                else
//                    callEditBillingInvoice(formrec, label+'BillingInvoice',copyInv);
//            }
//            else
            {
                if(this.isQuotation){
                    this.quotation=true;
                    if(this.isLeaseFixedAsset && this.checkDuplicateTabOpen('leasequotation'+findtablabel)) {
                        callLeaseQuotation(isEditForQuotation, label, formrec, copyInv,true);
                    }else if(this.checkDuplicateTabOpen('quotation'+findtablabel)){
                        if(isLinkedTransaction){
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                            if(btn=="yes") {
                                callQuotation(isEditForQuotation, label, formrec, copyInv, undefined,undefined,undefined,isLinkedTransaction);
                            }else{
                                return;
                            }
                        },this);
                        }else{
                            callQuotation(isEditForQuotation, label, formrec, copyInv, undefined,undefined,undefined,isLinkedTransaction,undefined,this.pendingapproval);
                        }

                    }
                }else if(incash){
                    if(this.checkDuplicateTabOpen(findtablabel+'CashSales')){
                        if(isLinkedTransaction){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                                if(btn=="yes") {
                                    callEditCashReceipt(formrec, label+'CashSales',copyInv,undefined,undefined,isLinkedTransaction,isAllowToEdit);
                                }else{
                                    return;
                                }
                            },this);
                        }else{
                            callEditCashReceipt(formrec, label+'CashSales',copyInv,undefined,undefined,isLinkedTransaction,isAllowToEdit);
                        }
                    }                 
                }                   
                else{
                    //formrec.data.allowEditingRecurredDocuments - this flag is used  to bypass warning message when user tries to edit recurred(child) document.
                    if(!copyInv && formrec.data.isRepeated && formrec.data.childCount>0){
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert")," <b>"+formrec.data.billno +"</b>  "+ WtfGlobal.getLocaleText("acc.field.hasrecurringInvoice.posted.cannotbeEdited")], 2);
                         return;
                    } else if(formrec.data.isRepeated && !copyInv && (formrec.data.parentinvoiceid!="" && formrec.data.parentinvoiceid!=undefined)){
                        /**
                         * Allow to edit non Transactional field for recurred invoice 
                         */
                         var isRecurredRecord = true;
                         isAllowToEdit = true; 
                         Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.RecurredInvoice"),function(btn){
                            if(btn=="yes") {
                                    this.callInvoicemethod(formrec,label,copyInv,this.isFixedAsset,this.isLeaseFixedAsset,findtablabel,this.isDraft,isRecurredRecord,isAllowToEdit);
                            }else{
                                return;
                            }
                        },this);
                    } else if(!copyInv && formrec.data.isWrittenOff && !formrec.data.isRecovered){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.writeOff.invoiceCanNotBeEdited")],2);
                        return;
                    /*
                     * Check if invoice is claimed as bad debt. If claimed, allow editing only selected fields.
                     */
                    
                    } else if(!copyInv && formrec.data.isClaimedTransaction && !formrec.data.isRecovered){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.canNotEditClaimedInvoice"),function(btn){
                            if(btn=="yes") {
                                    this.callInvoicemethod(formrec,label,copyInv,this.isFixedAsset,this.isLeaseFixedAsset,findtablabel,this.isDraft,isLinkedTransaction,isAllowToEdit);
                            }else{
                                return;
                            }
                        },this);
                    } else if(!copyInv && isLinkedTransaction && !formrec.data.allowEditingRecurredDocuments){    // check whether the invoice is paid fully/partially.
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                            if(btn=="yes") {
                                    this.callInvoicemethod(formrec,label,copyInv,this.isFixedAsset,this.isLeaseFixedAsset,findtablabel,this.isDraft,isLinkedTransaction,isAllowToEdit);
                            }else{
                                return;
                            }
                        },this);
                    }  else if(!copyInv && isLinkedTransaction && formrec.data.isRepeated){    // This condition added to allow to edit invoice whose recurred invoice deleted permanently.
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                            if(btn=="yes") {
                                    this.callInvoicemethod(formrec,label,copyInv,this.isFixedAsset,this.isLeaseFixedAsset,findtablabel,this.isDraft,isLinkedTransaction,isAllowToEdit);
                            }else{
                                return;
                            }
                        },this);
                     } else if(!copyInv && isTaxPaidTransaction){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invList.taxpaymentlinkedtoinvoice")],2);
                        return;
                    }
                   else if(!isLinkedTransaction){
                        this.callInvoicemethod(formrec,label,copyInv,this.isFixedAsset,this.isLeaseFixedAsset,findtablabel,this.isDraft,isLinkedTransaction,isAllowToEdit);
                    }
                }
            }
        }else{
            if(this.isQuotation){
                this.quotation=true;
                if (this.isFixedAsset && this.checkDuplicateTabOpen('assetvendorquotation'+findtablabel)) {
                    callFixedAssetVendorQuotation(isEditForQuotation,label, formrec, copyInv,undefined,undefined,this.pendingapproval);
                } else if(this.checkDuplicateTabOpen('vendorquotation'+findtablabel)){
                    if(formrec.data.isLinkedTransaction && !copyInv){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                            if(btn=="yes") {
                                callVendorQuotation(isEditForQuotation,label, formrec, copyInv,undefined,undefined,formrec.data.isLinkedTransaction);
                            } else {
                                return;
                            }
                        },this);
                    }else{
                        if(copyInv){
                            callVendorQuotation(isEditForQuotation,label, formrec, copyInv);
                        }else{
                            callVendorQuotation(isEditForQuotation,label, formrec, copyInv,undefined,undefined,formrec.data.isLinkedTransaction,this.pendingapproval);
                        }
                    }
                }
            }else if(this.isRFQ){
                if (this.isFixedAsset){
                    if(this.checkDuplicateTabOpen('assetrequestforquotation'+billid)){
                        callFixedAssetRequestForQuotation(copyInv!=true?true:false,undefined,undefined,this.isFixedAsset,billid, formrec,this, copyInv);
                    }
                }else if (this.checkDuplicateTabOpen('requestforquotation' + billid)) {
                        if (isLinkedTransaction && !copyInv) {
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.RFQ.linkedInVQ"), function(btn) {
                                if (btn == "yes") {
                                    callRequestForQuotation(copyInv != true ? true : false, undefined, undefined, billid, formrec, this, copyInv, isLinkedTransaction);
                                } else {
                                    return;
                                }
                            }, this);

                        } else {
                            callRequestForQuotation(copyInv != true ? true : false, undefined, undefined, billid, formrec, this, copyInv, isLinkedTransaction);
                        }
                    }
            }
//            else  if(this.withInvMode){
//                if(incash)
//                    callEditBillingPurchaseReceipt(formrec, label+'BillingCSInvoice',copyInv);
//                else
//                    callEditBillingGoodsReceipt(formrec,  label+'BillingInvoice',copyInv,undefined, undefined, isSelfBilledInvoice);
//            }
            else{
                if(this.isRequisition){
                    //                      if(formrec.data['approvestatuslevel']== -99){  
                    if (this.isFixedAsset) {
                        if(this.checkDuplicateTabOpen('assetRequisition'+billid)){
                            callFixedAssetPurchaseReq(true,formrec,billid);
                        }
                    } else if(this.checkDuplicateTabOpen('requisition'+billid)){
                        if(formrec.data.isLinkedTransaction){
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                                if(btn=="yes") {
                                    callPurchaseReq(true,formrec,billid,undefined,undefined,undefined,formrec.data.isLinkedTransaction);
                                }else{
                                    return;
                                }
                            },this);
                        }else{
                            callPurchaseReq(true,formrec,billid,undefined,undefined,undefined,formrec.data.isLinkedTransaction,undefined,undefined,this.isDraft);
                        }
                    }
                //                      }else{
                //                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.statuscheck")],2);
                //                      }
                }
                else if(incash){
                       if(this.checkDuplicateTabOpen(findtablabel+'PaymentReceipt')) {
                        if(isLinkedTransaction){
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                                if(btn=="yes") {
                                    callEdiCashPurchase(formrec, label+'PaymentReceipt',copyInv,isExpensiveInv,undefined,undefined,isLinkedTransaction,isAllowToEdit);
                                }else{
                                    return;
                                }
                            },this);
                        }else{
                            callEdiCashPurchase(formrec, label+'PaymentReceipt',copyInv,isExpensiveInv,undefined,undefined,isLinkedTransaction,isAllowToEdit);
                        }
                    }                     
                }                   
                else{
                    if(!copyInv && formrec.data.isRepeated && formrec.data.childCount>0){
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert")," <b>"+formrec.data.billno +"</b>  "+ WtfGlobal.getLocaleText("acc.field.hasrecurringInvoice.posted.cannotbeEdited")], 2);
                         return;
                    } else if(formrec.data.isRepeated && !copyInv && (formrec.data.parentinvoiceid!="" && formrec.data.parentinvoiceid!=undefined)){   // && this.checkIfInvoiceIsPaidFullyOrPartially(formrec)) //ERP-23504 : Block editing for recurred child transaction
                        /**
                         * Allow to edit non transactional field for recurred transaction
                         */
                            var isRecurredRecord = true;
                            isAllowToEdit = true;
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invList.RecurredInvoice"), function(btn) {
                                if (btn == "yes") {
                                    this.callVendorInvoiceMethods(formrec, label, copyInv, findtablabel, isExpensiveInv, isSelfBilledInvoice, isRecurredRecord, isAllowToEdit);
                                } else {
                                    return;
                                } 
                        },this);
                    } else if(!copyInv && (formrec.data.isSupplierLinekd!="" && formrec.data.isSupplierLinekd!=undefined && formrec.data.isSupplierLinekd )){   // && this.checkIfInvoiceIsPaidFullyOrPartially(formrec)) //ERP-23504 : Block editing for recurred child transaction
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Purchase invoice linked with supplier excise details, so cannot be edited"], 2);
                         return;
                    /*
                     * Check if invoice is claimed as bad debt. If claimed, allow editing only selected fields.
                     */     
                    } else if(!copyInv && formrec.data.isClaimedTransaction && !formrec.data.isRecovered){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.canNotEditClaimedInvoice"),function(btn){
                            if(btn=="yes") {
                                    this.callVendorInvoiceMethods(formrec,label,copyInv,findtablabel,isExpensiveInv,isSelfBilledInvoice,isLinkedTransaction,isAllowToEdit);
                            }else{
                                return;
                            } 
                        },this);
                        
                    } else if(!copyInv && isLinkedTransaction && !formrec.data.allowEditingRecurredDocuments){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                            if(btn=="yes") {
                                    this.callVendorInvoiceMethods(formrec,label,copyInv,findtablabel,isExpensiveInv,isSelfBilledInvoice,isLinkedTransaction,isAllowToEdit);
                            }else{
                                return;
                    } 
                        },this);
                    }else{
                        this.callVendorInvoiceMethods(formrec,label,copyInv,findtablabel,isExpensiveInv,isSelfBilledInvoice,isLinkedTransaction,isAllowToEdit);
                  }
                }
            }
        }
    }else{
        WtfComMsgBox(55,2);
        return; 
    }
       
},

callInvoicemethod : function(formrec,label,copyInv,isFixedAsset,isLeaseFixedAsset,findtablabel,isDraft,isLinkedTransaction,isAllowToEdit){
    if(this.isFixedAsset){
        callEditFixedAssetInvoice(formrec, label+'Invoice',copyInv,undefined,false,isFixedAsset,isLeaseFixedAsset,undefined,isLinkedTransaction);
    } else if(this.isLeaseFixedAsset){
        callEditFixedAssetInvoice(formrec, label+'Invoice',copyInv,undefined,false,isFixedAsset,isLeaseFixedAsset,undefined,isLinkedTransaction);
    }else if(this.isConsignment){
        callConsignmentInvoice(true,formrec,label+'ConsignmentInvoice',false,false,true);
    } else if(this.checkDuplicateTabOpen(findtablabel+'Invoice')) {
        callEditInvoice(formrec, label+'Invoice',copyInv,undefined,false,isFixedAsset,undefined,undefined,isDraft,isLinkedTransaction,isAllowToEdit,undefined,this.pendingapproval);                                          
    }
},

callVendorInvoiceMethods: function(formrec,label,copyInv,findtablabel,isExpensiveInv,isSelfBilledInvoice,isLinkedTransaction,isAllowToEdit){
    if(this.isFixedAsset){
        this.checkDepreciationPostedOrNot(formrec,label,copyInv,isExpensiveInv,isLinkedTransaction);
    }else if(this.checkDuplicateTabOpen(findtablabel+'GoodsReceipt')){
        if(this.isConsignment){
            callConsignmentGoodsReceipt(true,formrec,label+'consGoodsReceipt',false,false,true);
        }else{
            callEditGoodsReceipt(formrec, label+'GoodsReceipt',copyInv,isExpensiveInv,undefined,false,isSelfBilledInvoice,isLinkedTransaction,isAllowToEdit,undefined,this.pendingapproval);    
        }
    }  
},

checkIfInvoiceIsPaidFullyOrPartially : function(formrec){
      if(formrec.data.amount==formrec.data.amountdue){      // If amount of invoice == amount due then there is no payment/receipt/debit note/credit note is made against invoice
          return false;
      } else {
          return true;
      }
},
  
checkDuplicateTabOpen:function(tabid) {
    var tabpanel = Wtf.getCmp(tabid);
    if(tabpanel!=undefined){ //Alerting user due to ERP-3302,ERP-3303,ERP-3309,ERP-3317
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoicelist.alreadyopentablalert")],2);
        return false;
    } else {
        return true;
    }
},

    editOrderTransaction:function(copyInv){			// Editing Sales and Purchase Order with Inventory and Without Inventory
        var isLinkedTransaction = false;
        var isMRPJOBWORKOUT = false;
        var isJobWorkOrderReciever = false;
        var formRecord = null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formRecord = this.grid.getSelectionModel().getSelected();
        if(formRecord.data.isLinkedTransaction != undefined && formRecord.data.isLinkedTransaction && !copyInv){
            isLinkedTransaction = true;
        }
        if(formRecord.data.isMRPJOBWORKOUT != undefined && formRecord.data.isMRPJOBWORKOUT && !copyInv){
            isMRPJOBWORKOUT = true;
        }
        if(formRecord.data.isJobWorkOrderReciever != undefined && formRecord.data.isJobWorkOrderReciever ){
            isJobWorkOrderReciever = true;
        }
        var label=copyInv?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.product.edit");
        var findtablabel=copyInv?WtfGlobal.getLocaleText("acc.product.edit"):WtfGlobal.getLocaleText("acc.common.copy");
        var billid=formRecord.get("billid");
        label=label+billid;
        findtablabel=findtablabel+billid;
        this.withInvMode = formRecord.get("withoutinventory");
        if(!this.isCustomer){
    		if(!this.withInvMode){				// Without Inventory
                      if(this.isConsignment){
                         callConsignmentRequest(true,formRecord,billid, copyInv,null,false,false,false,true,false);
                      }else if(this.isFixedAsset && this.isRequisition ){
                          if(this.checkDuplicateTabOpen('assetRequisition'+billid)){
                              callFixedAssetPurchaseReq(false,formRecord,billid,false,this,copyInv);
                          }
                      }else if (this.isFixedAsset) {
                           callEditFixedAssetPurchaseOrder(true,formRecord,label,false,this,this.newtranType, copyInv,undefined,undefined,this.pendingapproval);
                      }else if(this.isRequisition){
                          if(this.checkDuplicateTabOpen('requisition'+billid)){
                            callPurchaseReq(false,formRecord,billid,false,this,copyInv);
                          }
                      } else if(this.checkDuplicateTabOpen('PurchaseOrder'+findtablabel)){
                          
                if(isLinkedTransaction && !isJobWorkOrderReciever){
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                        if(btn=="yes") {
                            callEditPurchaseOrder(true,formRecord,label,false,this,this.newtranType, copyInv,undefined,undefined,isLinkedTransaction,this.isDraft);
                        }else{
                            return;
                        }
                    },this);
                            } else if(isMRPJOBWORKOUT) {
                    callEditPurchaseOrder(true,formRecord,label,false,this,this.newtranType, copyInv,undefined,undefined,false,true,this.isDraft);
                }else if(isJobWorkOrderReciever) {
                    if(isLinkedTransaction){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                            if(btn=="yes") {
                                callEditPurchaseOrder(true,formRecord,label,false,this,this.newtranType, copyInv,undefined,undefined,isLinkedTransaction,false,undefined,undefined,undefined,true);
                            }else{
                                return;
                            }
                        },this); 
                    }
                    else{
                        callEditPurchaseOrder(true,formRecord,label,false,this,this.newtranType, copyInv,undefined,undefined,false,false,undefined,undefined,undefined,true);
                    }    
                }else if(this.moduleId==Wtf.Acc_Security_Gate_Entry_ModuleId){
                    callEditPurchaseOrder(true,formRecord,label,false,this,this.newtranType, copyInv,undefined,undefined,false,false,undefined,undefined,undefined,undefined,true);
                } else {     
                    callEditPurchaseOrder(true,formRecord,label,false,this,this.newtranType, copyInv,undefined,undefined,false,false,undefined,undefined,undefined,false,undefined,this.pendingapproval,this.isDraft);
                }
            }
                        }
//                else{								// With Inventory
//    			callBillingPurchaseOrder(true,formRecord,billid,false,this,this.newtranType, copyInv);
//    		}
        }
        else{
    		if(!this.withInvMode){				// Without Inventory
                    if(!copyInv && formRecord.data.isRepeated && formRecord.data.childCount>0){
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert")," <b>"+formRecord.data.billno +"</b>  "+ WtfGlobal.getLocaleText("acc.field.hasrecurringInvoice.posted.cannotbeEdited")], 2);
                         return;
                    } 
                    if(this.isLeaseFixedAsset){
                        callFixedAssetLeaseSalesOrder(true,formRecord,billid, copyInv,null,false,false,false,true);
                    }else if(this.isConsignment){
                        var isrequesteditable=formRecord.data.isrequesteditable;
                        if(isrequesteditable!=undefined && isrequesteditable==true){
                             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.approval.requestoreditcheck")], 2);
                        }else{
                            callConsignmentRequest(true,formRecord,billid, copyInv,null,false,false,false,true,true);
                        }
                     }else if(this.checkDuplicateTabOpen('SalesOrder'+findtablabel)){
                            if(isLinkedTransaction){
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.linkedInvoice"),function(btn){
                                    if(btn=="yes") {
                                        callSalesOrder(true,formRecord,'SalesOrder'+label, copyInv,undefined,undefined,undefined,undefined,isLinkedTransaction,this.isMRPSalesOrder,this.isJobWorkOrderReciever,this.isDraft);
                                    }else{
                                        return;
                                    }
                                },this);
                            } else if(formRecord.data.isRepeated && !copyInv && (formRecord.data.parentso!="" && formRecord.data.parentso!=undefined)){
                                /*
                                 *If sales order is recurred then prombt will be shown i.e Selected record(s) is Recurred transaction(s).You cannot edit all the fields.Do you want to continue?
                                 */
                                var isRecurredRecord = true;
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.RecurredInvoice"),function(btn){
                                    if(btn=="yes") {
                                         callSalesOrder(true,formRecord,'SalesOrder'+label, copyInv,undefined,undefined,undefined,undefined,isRecurredRecord,this.isMRPSalesOrder,this.isJobWorkOrderReciever,this.isDraft);
                                    }else{
                                         return;
                                    }
                               },this);
                             } else {
                                callSalesOrder(true,formRecord,'SalesOrder'+label, copyInv,undefined,undefined,undefined,undefined,isLinkedTransaction,this.isMRPSalesOrder,this.isJobWorkOrderReciever,undefined ,this.pendingapproval,this.isDraft);
                            }
                        }
               
            }
//                else{								// With Inventory
//    			callBillingSalesOrder(true,formRecord,billid, copyInv);
//    		}
        }
    },
   
    showAgedDetails:function(){
      var formRecord = null; 
       formRecord = this.grid.getSelectionModel().getSelected();
       var billid=formRecord.get("personid");
       var withoutinventory=formRecord.get("withoutinventory");
        if(this.isCustomer){
            callAgedRecievable({withinventory:withoutinventory,custVendorID:billid});
        }else{
            callAgedPayable({withinventory:withoutinventory,custVendorID:billid});
        }
    },
   
    copyQuotation:function(){
        var copyInv = true;
        var formRecord = null;
        var label=copyInv?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.product.edit");
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formRecord = this.grid.getSelectionModel().getSelected();
        var billid=formRecord.get("billid");
        if(!this.isCustomer){
            callVendorQuotation(false, label+'VendorQuotation', formRecord, copyInv);
        }else{
            callQuotation(false, label+'CustomerQuotation', formRecord, copyInv);
        }
    },
   createSIfromSO:function(){             
        var formRecord = null;
        if(this.grid.getSelectionModel().hasSelection()==false||(this.grid.getSelectionModel().getCount()>1 && !Wtf.account.companyAccountPref.activateGroupCompaniesFlag)){//avoiding single select for multigroupcompany flag
        WtfComMsgBox(15,2);
        return;
    }
        var isLinkedTransaction = false;
        var formRecord = null;
        var copyInv=false;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formRecord = this.grid.getSelectionModel().getSelected();
        if(formRecord.data.isLinkedTransaction != undefined && formRecord.data.isLinkedTransaction && !copyInv){
            isLinkedTransaction = true;
        }
        var label=copyInv?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.product.edit");
        var findtablabel=copyInv?WtfGlobal.getLocaleText("acc.product.edit"):WtfGlobal.getLocaleText("acc.common.copy");
        var billid=formRecord.get("billid");
        label=label+billid;
        findtablabel=findtablabel+billid;
        this.withInvMode = formRecord.get("withoutinventory");
    //If multi group company flag is activated = generating multiple PO on the basis of vendor mapped products
    if (this.isCustomer) {
                /*
                 * Additional reqired  information is sent in case of view mode
                 * 
                 **/
                
                if((!formRecord.data.isLinkedTransaction || this.crossLinkingTransaction) && this.exportRecord == undefined){           
                    this.exportRecord = formRecord.data;
                    this.exportRecord["billid"] =formRecord.data.billid;
                    this.exportRecord["billno"] = formRecord.data.billno;
                    this.exportRecord["term"] = formRecord.data.termid;
                    this.exportRecord["customer"] = formRecord.data.personid;
                    this.exportRecord["ispercentdiscount"]=false;
                    this.exportRecord["isfromviewmode"]=this.isViewTemplate;
                }
                this.exportRecord["includingGST"] =formRecord.data.gstIncluded? "on" :"off";
                if(formRecord.data.includeprotax){
                    this.exportRecord['includeprotax']=true;
                }
                this.exportRecord["isEdit"]=this.isEdit;
                this.exportRecord['personid']=(formRecord != null && formRecord!=undefined) ? formRecord.data.personid:"";
                this.exportRecord['personname']=(formRecord != null && formRecord!=undefined) ? formRecord.data.personname:"";
                this.exportRecord['hasAccess']=(formRecord != null && formRecord!=undefined) ? formRecord.data.hasAccess:"";
                this.exportRecord['isTaxable']=formRecord.data.isTaxable;
                callInvoice(false, undefined, undefined, false, false, true, this.exportRecord);
            }
   	    	
    },
    createPOfromSO:function(){             
        var formRecord = null;
        if(this.grid.getSelectionModel().hasSelection()==false||(this.grid.getSelectionModel().getCount()>1 && !Wtf.account.companyAccountPref.activateGroupCompaniesFlag)){//avoiding single select for multigroupcompany flag
        WtfComMsgBox(15,2);
        return;
    }
     
    //If multi group company flag is activated = generating multiple PO on the basis of vendor mapped products
    if(Wtf.account.companyAccountPref.activateGroupCompaniesFlag){  
        this.recordsArr=this.grid.getSelectionModel().getSelections();
        var billidArr=[];
        var billConfig={};
        for(var i=0;i<this.recordsArr.length;i++){
            var rec= this.recordsArr[i];
            if(rec!=null && rec!=undefined && rec.data!=null && rec.data!=undefined){
                var billid=rec.data.billid;
                billConfig.billid=billid;
                billidArr.push(billConfig);    
                billConfig={};
            } 
        }
        
        Wtf.Ajax.requestEx({
            params:{
                recs:JSON.stringify(billidArr)
            },
            url:"ACCSalesOrderCMN/generatePOFromMultipleSO.do"
        },this,
        this.genSuccessMsg, this.genFailureMsg);
    }else{
        formRecord = this.grid.getSelectionModel().getSelected();
        var billid=formRecord.get("billid");
        formRecord.data['gstIncluded']=false;
        formRecord.data['includeprotax']=false;
        this.withInvMode = formRecord.get("withoutinventory");       
        if(!this.withInvMode){				// Without Inventory
            callEditPurchaseOrder(true,formRecord,"Generate_PO"+billid,true,this,this.newtranType, false);
        }
    }
   	    	
    },
    
genSuccessMsg:function(response, request){
    if(response.success) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.wtfTrans.po"),response.msg]);
    }else{
        this.genFailureMsg(response);
    }
},
    
genFailureMsg:function(response){
    WtfGlobal.resetAjaxTimeOut();
    Wtf.MessageBox.hide();
    var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
    if(response.msg)msg=response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
},
    createSOfromPO:function(){             
        var formRecord = null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formRecord = this.grid.getSelectionModel().getSelected();
        var billid=formRecord.get("billid");
        this.withInvMode = formRecord.get("withoutinventory");                           
        if(!this.withInvMode){				// Without Inventory
            callSalesOrder(true,formRecord,"Generate_PO"+billid, null,null,null,true);
        }
    },
sendMail:function(){
        var formrec=null;
         
        if(this.grid.getSelectionModel().hasSelection()==false||(this.grid.getSelectionModel().getCount()>1 && this.moduleid != Wtf.Acc_Invoice_ModuleId)){
                WtfComMsgBox(15,2);
                return;
        }
       var incash;
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {
            formrec = this.grid.getSelectionModel().getSelections();
            this.withInvMode = formrec[0].get("withoutinventory");
            incash = formrec[0].get("incash");
        }
        else {
            formrec = this.grid.getSelectionModel().getSelected();
            incash = formrec.get("incash");
            this.withInvMode = formrec.get("withoutinventory");
        }

        if (incash == "" || incash == undefined || incash == null) {
            incash = false;
        }
        
        if(this.isCustomer && this.isQuotation){
            if(this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
               callEmailForMultipleRecords('', formrec, this.label, 50, true, '', this.label, 'pdf', 24, undefined, false, true, "", Wtf.Acc_Customer_Quotation_ModuleId,this.Store); 
            }else{
                formrec.set('moduleid', this.moduleid);
            callEmailWin("emailwin",formrec,this.label,50,true,false,true);
        }
        }else if(!this.isCustomer && this.isQuotation){
            if(this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId){
                callEmailForMultipleRecords('', formrec, this.label, 57, true, '', this.label, 'pdf', 24, undefined, false, true, "", Wtf.Acc_Vendor_Quotation_ModuleId,this.Store); 
            }else{
            callEmailWin("emailwin",formrec,this.label,57,false,false,true);
        }
        } else if(this.isRFQ){
            callEmailWin("emailwin",formrec,this.label,59,false,false,false,false,false,false,true);
        }else if(this.isRequisition){ 
             callEmailWin("emailwin",formrec,this.label,60,false,false,false,false,false,false,false,false,false,false,false,true);
        }
        else if(incash){
            if(this.isCustomer){
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    callEmailWin("emailwin",formrec,this.label,11,true,false,false,true);
//                }else
                if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {
                    callEmailForMultipleRecords('', formrec, this.label, 2, true, '', this.label, 'pdf', 24, undefined, false, true, "", Wtf.Acc_Invoice_ModuleId,this.Store);
                } else {
                    callEmailWin("emailwin", formrec, this.label, 2, true, false, false, true);
                }
            } else{
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    callEmailWin("emailwin",formrec,this.label,15,false,false,false,true);
//                } else
                if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) { //ERP-35824
                    callEmailForMultipleRecords('', formrec, this.label, 6, true, '', this.label, 'pdf', 24, undefined, false, true, "", Wtf.Acc_Vendor_Invoice_ModuleId,this.Store);
                } else {
                    callEmailWin("emailwin",formrec,this.label,6,false,false,false,true);
                }
            }
        }else if(this.isOrder){
            if(this.isCustomer){
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    callEmailWin("emailwin",formrec,this.label,17,true,false,false,false,true);
//                }else
            if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId){
                callEmailForMultipleRecords('', formrec, this.label, 1, true,'', this.label,'pdf', 24, undefined,false,true,"",Wtf.Acc_Sales_Order_ModuleId,this.Store);
            }else{
                formrec.set("moduleid",this.moduleid);
                this.isConsignment?callEmailWin("emailwin",formrec,this.label,1,true,false,false,false,true,false,false,false,false,true):callEmailWin("emailwin",formrec,this.label,1,true,false,false,false,true,false,false,false,false,false);
            }
            }else{
//                if(Wtf.account.companyAccountPref.withoutinventory){
//                    callEmailWin("emailwin",formrec,this.label,18,false,false,false,false,true);
//                } else
            if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId){
                callEmailForMultipleRecords('', formrec, this.label, 5, true,'', this.label,'pdf', 24, undefined,false,true,"",Wtf.Acc_Purchase_Order_ModuleId,this.Store);
            }else{
                if(this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId){
                    callEmailWin("emailwin",formrec,this.label,5,false,false,false,false,true,false,false,false,false,false,false,false,true);
                }else{
                    this.isConsignment?callEmailWin("emailwin",formrec,this.label,5,false,false,false,false,true,false,false,false,false,true):(this.isFixedAsset? callEmailWin("emailwin",formrec,this.label,5,false,false,false,false,true,false,false,false,false,false,true): callEmailWin("emailwin",formrec,this.label,5,false,false,false,false,true,false,false,false,false,false));
                }
            }
        }
        }else{
        if(this.isCustomer){
            if(this.withInvMode){
//                if(incash)
//                     callEmailWin("editwin",formrec,this.label,13);
//                else
                    callEmailWin("emailwin",formrec,this.label,11,true,true);
            }
            else{
//                if(incash)
//                    callEmailWin("editwin",formrec,this.label,2);
//                else
                    if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {
                       callEmailForMultipleRecords('', formrec, this.label, 2, true, '', this.label, 'pdf', 24, undefined, false, true, "", Wtf.Acc_Invoice_ModuleId,this.Store);
                    } else {
                       callEmailWin("emailwin", formrec, this.label, 2, true, true);
                    }
                }
        }else{
            if(this.withInvMode){
//                if(incash)
//                     callEmailWin("editwin",formrec,this.label,11);
//                else
                   callEmailWin("emailwin",formrec,this.label,15,false,true);
            }
            else{
//                if(incash)
//                    callEmailWin("editwin",formrec,this.label,15);
//                else
                if(this.isCustomer){
                    callEmailWin("emailwin",formrec,this.label,6,true,true);
                    } else if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
                            callEmailForMultipleRecords('', formrec, this.label, 6, true, '', this.label, 'pdf', 24, undefined, false, true, "", Wtf.Acc_Vendor_Invoice_ModuleId,this.Store);
                        } else {
                            this.isConsignment ? callEmailWin("emailwin", formrec, this.label, 6, false, true, false, false, false, false, false, false, false, true) : callEmailWin("emailwin", formrec, this.label, 6, false, true);
                        }
                }
            }
        }
        
     },
    repeateInvoiceHandler:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.grid.getSelectionModel().getCount()==1 && formrec.data.isRepeated){
            var msg = "<b>"+formrec.data.billno + WtfGlobal.getLocaleText("</b>  has already set a recurring, so cannot be recurred."); //ERP-16576
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2)
            return;
        }
        var isaddRecurringInvoice=false;
        if(this.isCustomer || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId){
              callRepeatedInvoicesWindow(formrec.data.withoutinventory, formrec ,isaddRecurringInvoice,this.isOrder, undefined,undefined,undefined,undefined,this.isCustomer, this.moduleid);  
//            callRepeatedInvoicesWindow(this.isCustBill, formrec);
               Wtf.getCmp("RepeatedInvoicesWin").on('loadInvoiceList',function(){
                    this.grid.getStore().reload();
             },this);
        }
    },
   
    cellclickhandler : function(grid, rowIndex, columnIndex,e){
        var event=e;
        if(event.getTarget('img[class="favourite"]')) {           
        var formrec = grid.getSelectionModel().getSelected();
        var isfavourite = formrec.get('isfavourite');
        if(!formrec.data.deleted && !this.consolidateFlag){
            if(isfavourite){
                this.markUnFavouriteHandler(formrec);
            }else{
                this.markFavouriteHandler(formrec);
            }
        }
    }
},
   
    markFavouriteHandler : function(formrec){
    var url = this.isQuotation?(this.businessPerson=="Customer"?"ACCSalesOrder/updateFavourite.do":"ACCPurchaseOrder/updateFavourite.do"):(this.isOrder?(this.businessPerson=="Customer"?"ACCSalesOrder/updateFavourite.do":"ACCPurchaseOrder/updateFavourite.do"):(this.businessPerson=="Customer"?'ACCInvoice/updateFavourite.do':'ACCGoodsReceipt/updateFavourite.do'));
    var inventoryFlag = this.withInvMode?true:false;
    var quotationFlag = this.isQuotation;
   
    Wtf.Ajax.requestEx({
        url:url,
            params:{
            invoiceid:formrec.get('billid'),
            withInv : inventoryFlag,
            requisitionflag:this.isRequisition,
            rfqflag:this.isRFQ,
            date: WtfGlobal.convertToGenericDate(formrec.data.date),//used as transaction date
            quotationFlag : quotationFlag,
            isfavourite:true,
            isDraft: this.isDraft
        }
    },this,
    function(){
        formrec.set('isfavourite', true);
    },function(){
               
        });
},

    markUnFavouriteHandler : function(formrec){
    var url = this.isQuotation?(this.businessPerson=="Customer"?"ACCSalesOrder/updateFavourite.do":"ACCPurchaseOrder/updateFavourite.do"):(this.isOrder?(this.businessPerson=="Customer"?"ACCSalesOrder/updateFavourite.do":"ACCPurchaseOrder/updateFavourite.do"):(this.businessPerson=="Customer"?'ACCInvoice/updateFavourite.do':'ACCGoodsReceipt/updateFavourite.do'));
    var inventoryFlag = this.withInvMode?true:false;
    var quotationFlag = this.isQuotation;
   
    Wtf.Ajax.requestEx({
        url:url,
        params:{
            invoiceid:formrec.get('billid'),
            withInv : inventoryFlag,
            rfqflag:this.isRFQ,
            requisitionflag:this.isRequisition,
            date: WtfGlobal.convertToGenericDate(formrec.data.date),
            quotationFlag : quotationFlag,
            isfavourite:false,
            isDraft: this.isDraft
        }
    },this,
    function(){
        if(this.index == 5 || this.archiveFlag == 3){
            this.grid.getStore().remove(formrec);
        }else{
            formrec.set('isfavourite', false);
        }
    },function(){
               
        });
},
   
    onRowexpand:function(scope, record, body){
        var colModelArray = [];
        var isoutstandinProduct=false;
        var jobWorkOut=false;
        colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
        colModelArray = [];
        colModelArray = GlobalColumnModelForProduct[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
        this.expanderBody=body;
        this.isexpenseinv=!this.isCustomer&&record.data.isexpenseinv;
        this.withInvMode = record.data.withoutinventory;
        if(this.typeEditor){
           isoutstandinProduct=(this.typeEditor.getValue()==11)?true:false;
        }
        if(this.moduleid==Wtf.Job_Work_Out_ORDER_REC){
            jobWorkOut=true;           
        }
        this.expandStore.on('beforeload', function() {
            WtfGlobal.setAjaxTimeOut();
        }, this);

        this.expandStore.on('load', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
        this.expandStore.on('loadexception', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
        this.expandStore.load({params:{bills:record.data.billid,isexpenseinv:(!this.isCustomer&&record.data.isexpenseinv),isConsignment:this.isConsignment,isFixedAsset:this.isFixedAsset,isLeaseFixedAsset:this.isLeaseFixedAsset,isOuststandingproduct:isoutstandinProduct,isVendorJobWorkOrder:this.isJobWorkOrderReciever,isForJobWorkOut:jobWorkOut}});
    },

    fillExpanderBody:function(){
        WtfGlobal.resetAjaxTimeOut();
        var disHtml = "";
        this.custArr = [];
        this.custArr = WtfGlobal.appendCustomColumn(this.custArr, GlobalColumnModelForProduct[this.moduleid]);//At line level, first Product & Services related fields are add then other fields
        this.custArr = WtfGlobal.appendCustomColumn(this.custArr, GlobalColumnModel[this.moduleid]);
        var expenseInvHeader ="";
        var ProductHeader = "";

        var prevBillid = "";
        var sameParent = false;

        for (var i = 0; i < this.expandStore.getCount(); i++) {
            var header = "";
            var rec = this.expandStore.getAt(i);
            var isexpenseinv = rec.data.isexpenseinv ? rec.data.isexpenseinv : false;
            
            var currentBillid = rec.data['billid'];
            if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
                prevBillid = currentBillid;
                sameParent = false;
            } else {
                sameParent = true;
            }
            
            if (isexpenseinv) {
                expenseInvHeader = this.getExpenseInvoiceHeader(rec);     //expenseInvHeader[0]: HTML text,  expenseInvHeader[1]: minWidth,  expenseInvHeader[2]:widthInPercent
                header = this.getExpInvoiceExpanderData(rec, sameParent, expenseInvHeader[1], expenseInvHeader[2]);
            } else {
                ProductHeader = this.getProductHeader(rec);              //ProductHeader[0]: HTML text,  ProductHeader[1]: minWidth,  ProductHeader[2]:widthInPercent
                header = this.getProductExpanderData(rec, sameParent, ProductHeader[1], ProductHeader[2]);
            }
            var moreIndex = this.grid.getStore().findBy(
                    function(record, id) {
                        if (record.get('billid') === rec.data['billid']) {
                            return true;  // a record with this data exists 
                        }
                        return false;  // there is no record in the store with this data
                    }, this);
            if (moreIndex != -1) {
                var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                if (isexpenseinv) {
                    disHtml = "<div class='expanderContainer1'>" + expenseInvHeader[0] + header + "</div>";
                } else {
                    disHtml = "<div class='expanderContainer1'>" + ProductHeader[0] + header + "</div>";
                }
                body.innerHTML = disHtml;
                if (this.expandButtonClicked) {
                    this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                    this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
                }
            }
        }
    },
    getExpenseInvoiceHeader:function(rec){
        var arr=[];
        var expInvHeaderArray = [];
        this.israteincludegst =  rec.data.isIncludingGst;
//        var includeprotax = rec.data.includeprotax;
        arr=[WtfGlobal.getLocaleText("acc.invoiceList.accName") ,WtfGlobal.getLocaleText("acc.product.gridType"), WtfGlobal.getLocaleText("acc.invoiceList.expand.description") ,
            WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),((rec.data.balanceAmount != undefined && this.isOrder) ? WtfGlobal.getLocaleText("Balance Amount"):''),this.israteincludegst?WtfGlobal.getLocaleText("acc.invoice.gridamountIncludingGST"):'',WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"), WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),'                  '];
        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.invoiceList.expand.accList")+"</span>";   //Account List
       
            var arrayLength=arr.length;
            for(var custCount=0;custCount<this.custArr.length;custCount++){
                var headerFlag=false;
                if(this.custArr[custCount].header != undefined ) {
                   
                for(var j=0;j<this.customizeData.length;j++){
                              if(this.custArr[custCount].header==this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol){
                                 headerFlag=true;
                                    }
                                }
                    if(!headerFlag){
                        arr[arrayLength]=this.custArr[custCount].header;
                        arrayLength=arr.length;
                    }
            }
        }
        var count=0;
        for(var i=0;i<arr.length;i++){
            if(arr[i] != ""){
                count++;
            }
        }
        var widthInPercent=100/count;
        var minWidth = count*100 + 40;
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var i=0;i<arr.length;i++){
             header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";  
        expInvHeaderArray.push(header);
        expInvHeaderArray.push(minWidth);
        expInvHeaderArray.push(widthInPercent);
        return expInvHeaderArray;
    },
    getProductHeader: function(rec){
            var israteincludegst = this.expandStore.getCount()>0 && rec.data.israteIncludingGst ? rec.data.israteIncludingGst : false;
//            var includeprotax = rec.data.includeprotax;
            var productTypeText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pTypeNonInv") : WtfGlobal.getLocaleText("acc.invoiceList.expand.pType");
            var productHeaderArray = [];
            var hideShowFlag=false;
            if((this.moduleid== Wtf.Acc_Invoice_ModuleId || this.moduleid== Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.calculateproductweightmeasurment){
                hideShowFlag=true;
            }
            var arr=[];
            arr=[(this.withInvMode?'':(this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixed.asset.id"):WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
                (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):(this.isFixedAsset)?WtfGlobal.getLocaleText("acc.fixed.asset.name"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
                ((this.moduleid===Wtf.Acc_ConsignmentRequest_ModuleId) || (this.moduleid=== Wtf.Acc_Invoice_ModuleId)|| (this.moduleid=== Wtf.Acc_Vendor_Invoice_ModuleId) || (this.moduleid=== Wtf.Acc_Sales_Order_ModuleId)|| (this.moduleid=== Wtf.Acc_Purchase_Order_ModuleId)||(this.moduleid=== Wtf.Acc_Customer_Quotation_ModuleId)||(this.moduleid=== Wtf.Acc_Vendor_Quotation_ModuleId) || this.isRFQ || this.isRequisition?WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridDescription"):""),//Product Details or Product Description
                (this.isCustomer?
                    productTypeText:(this.moduleid!=Wtf.Acc_Security_Gate_Entry_ModuleId&&Wtf.account.companyAccountPref.countryid == '203' && this.moduleid!=Wtf.Acc_Vendor_Quotation_ModuleId && this.moduleid!=Wtf.Acc_RFQ_ModuleId &&  this.moduleid!=Wtf.Acc_Purchase_Requisition_ModuleId)?
                    (WtfGlobal.getLocaleText("acc.field.PermitNo.")):productTypeText),//Product Type
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity,
                WtfGlobal.getLocaleText("acc.uomgrid.baseuomqty"),//Base UOM Quantity,
                (!this.withInvMode && this.isMRPJOBWORKOUT &&(this.isOrder&&this.isCustomer||this.isOrder&&!this.isCustomer))?WtfGlobal.getLocaleText("Job In Quantity"):'',
                (!this.withInvMode && (this.isOrder&&this.isCustomer||this.isOrder&&!this.isCustomer || this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId))?WtfGlobal.getLocaleText("acc.field.BalanceQty"):'',
                (!this.withInvMode && this.moduleid=== Wtf.Acc_Sales_Order_ModuleId) ? WtfGlobal.getLocaleText("acc.field.ShortfallQty"):'',
                (hideShowFlag?WtfGlobal.getLocaleText("acc.productList.unitWeight"):''),//Unit Weight
                (hideShowFlag?WtfGlobal.getLocaleText("acc.productList.unitWeightWithPackaging"):''),//Unit Weight with Packageing
                (hideShowFlag?WtfGlobal.getLocaleText("acc.productList.unitVolume"):''),//Unit Volume
                (hideShowFlag?WtfGlobal.getLocaleText("acc.productList.unitVolumeWithPackaging"):''),//Unit Volume with Packageing
                ((this.isRFQ || (this.isRequisition && (this.grid.getColumnModel().findColumnIndex("amountinbase") != -1 && this.grid.getColumnModel().config[this.grid.getColumnModel().findColumnIndex("amountinbase")].hidden))) ? '' : (israteincludegst ? ((Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDONESIA) ? WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST") : WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingVAT")) : WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"))),//Unit Price
                (this.isCustomer && !this.isQuotation&& !this.isOrder&&!this.withInvMode)?WtfGlobal.getLocaleText("acc.field.PartialAmount(%)") : '',
                (this.isRFQ || this.isRequisition)? '' :WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),//Discount
                (this.isRFQ || this.isRequisition)? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),//Tax
                (this.isRFQ || (this.grid.getColumnModel().findColumnIndex("amountinbase") != -1 && this.isRequisition && (this.grid.getColumnModel().config[this.grid.getColumnModel().findColumnIndex("amountinbase")].hidden))) ? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),//Amount
                (!this.withInvMode&&(this.isOrder&&this.isCustomer))?WtfGlobal.getLocaleText("acc.field.BalanceAmt"):'',
                this.isRequisition ? WtfGlobal.getLocaleText("acc.field.ApproverRemark") : '',//Approver Remark
                (this.isCustomer && this.isQuotation && !this.isOrder)?WtfGlobal.getLocaleText("acc.field.VQ.No"): '',
                (!this.isCustomer && this.isQuotation && !this.isOrder)?WtfGlobal.getLocaleText("acc.field.PurchaseRequisitionRFQ.No"): '',
                (this.isCustomer && !this.isQuotation && !this.isOrder)?((this.isLeaseFixedAsset)?WtfGlobal.getLocaleText("acc.field.LeaseDONO"):((this.isConsignment)?"":WtfGlobal.getLocaleText("acc.field.SO/DO/CQNo"))):(!this.isCustomer && !this.isQuotation&& !this.isOrder && !this.isRFQ)?(this.isConsignment?"":WtfGlobal.getLocaleText("acc.field.PO/GR/VQNo")):"",
               (this.isCustomer && !this.isQuotation && this.isOrder && !this.isRequisition && !this.isConsignment)?(this.isLeaseFixedAsset?WtfGlobal.getLocaleText("acc.field.LQ/RN.NO"):WtfGlobal.getLocaleText("acc.field.CQ/PO.No")):this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId?WtfGlobal.getLocaleText("acc.field.PONo"):(!this.isCustomer && !this.isQuotation && this.isOrder && !this.isRequisition && !this.isConsignment)?WtfGlobal.getLocaleText("acc.field.SO/VQ.No"):"",
//                (this.isCustomer && !this.isQuotation && this.isOrder && this.isConsignment)?WtfGlobal.getLocaleText("acc.field.CNApprovedSerials"): '',
                (this.isCustomer && !this.isQuotation && this.isOrder && this.isConsignment)?WtfGlobal.getLocaleText("acc.invoiceList.status"): '',
              // (this.isOrder && !this.isConsignment) ?WtfGlobal.getLocaleText("acc.invoiceList.Action"):'',
               (!this.isJobWorkOrderReciever && (this.isOrder || this.moduleid===Wtf.Acc_ConsignmentRequest_ModuleId) && this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId&&this.moduleid!=Wtf.Acc_Security_Gate_Entry_ModuleId)? ((!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))?WtfGlobal.getLocaleText("acc.invoiceList.closedManually"):""):'', //ERP-28389
               (this.moduleid===Wtf.Acc_ConsignmentRequest_ModuleId)? ((!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))?WtfGlobal.getLocaleText("acc.invoiceList.rejectItem"):""):'',
               (this.moduleid===Wtf.Acc_ConsignmentRequest_ModuleId)? ((!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))?WtfGlobal.getLocaleText("acc.invoiceList.ReasonofRejection"):""):'',
               (CompanyPreferenceChecks.displayUOMCheck() === true ? WtfGlobal.getLocaleText("acc.product.displayUoMLabel"):'')//Display UOM,
           ];
            var gridHeaderText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
            var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";   //Product List
              
            var arrayLength=arr.length;
            for(var custCount=0;custCount<this.custArr.length;custCount++){
                var headerFlag=false;
                if(this.custArr[custCount].header != undefined ) {
                   
                for(var j=0;j<this.customizeData.length;j++){
                      if(this.custArr[custCount].header==this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol){
                           headerFlag=true;
                           for (var k = j + 1; k < this.customizeData.length; k++) {
                                if (this.customizeData[j].fieldDataIndex == this.customizeData[k].fieldDataIndex && this.customizeData[k].isForProductandService && !this.customizeData[k].hidecol) {
                                    headerFlag = false;
                                }
                            }
                        }
                    }
                    if(!headerFlag){
//                        alert(custArr[i].header);
                        arr[arrayLength]=this.custArr[custCount].header;
                        arrayLength=arr.length;
                    }
            }
            }
            var count=0;
            for(var i=0;i<arr.length;i++){
                if(arr[i] != ""){
                    count++;
                }
            }
            count++; // from grid no
            var widthInPercent=100/count;
            var minWidth = count*100;
            header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
            for(var arrI=0;arrI<arr.length;arrI++){
                if(arr[arrI]!=undefined)
                   header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[arrI] + "</span>";
            }
            header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";  
            productHeaderArray.push(header);
            productHeaderArray.push(minWidth);
            productHeaderArray.push(widthInPercent);
            return productHeaderArray;
    },
    getExpInvoiceExpanderData: function(rec, sameParent, minWidth, widthInPercent) {
        var accountname = rec.data['accountname'];
        var balanceAmount = rec.data['balanceAmount']  != null  && rec.data['balanceAmount']  != undefined ? rec.data['balanceAmount'] : "";
        var description = "";
        if (rec.data['desc'] != null && rec.data['desc'] != undefined){
            description = rec.data['desc'];          
        }
        description = description.replace(/<\/?[^>]+(>|$)/g, "");   //SDP-9944
        if(description=="") {
           description='&nbsp';    //If HTML Content is <br> only.
        }
        if (!sameParent) {
            this.expenseHeader = "";
            this.srNumber = 0;
        }
        var type="Credit";
        if(rec.json.debit){
            type="Debit";
        }
        this.expenseHeader += "<div style='width: 100%;min-width:"+minWidth+"px'>";
    this.expenseHeader += "<span class='gridNo'>" + (++this.srNumber) + ".</span>";
    this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + accountname + "'>" + Wtf.util.Format.ellipsis(accountname, 15) + "&nbsp;</span>";
    this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + type + "'>" + Wtf.util.Format.ellipsis(type, 25) + "&nbsp;</span>";
    this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + description + "'>" + Wtf.util.Format.ellipsis(description, 25) + "&nbsp;</span>";
    this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.data.rate, rec.data['currencysymbol'], [true]) + "</span>";
    if(rec.json.isgstincluded){
        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.json.rateIncludingGstEx, rec.data['currencysymbol'], [true]) + "</span>";
    }
    this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(balanceAmount, rec.data['currencysymbol'], [true]) + "</span>";
//        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + rec.data.prdiscount + "% " + "&nbsp;</span>";
    if(rec.data.discountispercent == 0){
        this.expenseHeader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
    } else {
        this.expenseHeader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
    }
        
        
//        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + rec.data.prdiscount + "% " + "&nbsp;</span>";
//        if (rec.json.includeprotax) {
            this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'], rec.data['currencysymbol'], [true]) + "</span>";
//        }
        var amount=rec.data.rate;
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 0){
                amount = rec.data.rate -rec.data.prdiscount;
            } else {
                amount = rec.data.rate -getRoundedAmountValue( (rec.data.rate * rec.data.prdiscount) / 100);
            }
        }
//        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'], rec.data['currencysymbol'], [true]) + "</span>";
//        var amount = rec.data.rate - (rec.data.rate * rec.data.prdiscount) / 100;
        if(rec.json.isgstincluded!=true){// when GST not applied we does not need to add tax separately. But not applied then we need to add it
            amount += rec.data['rowTaxAmount'];//amount+=(amount*rec.data.prtaxpercent/100);
        }
        
        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(amount, rec.data['currencysymbol'], [true]) + "</span>";
        for (var cust = 0; cust < this.custArr.length; cust++) {
            var headerFlag = false;
            if (this.custArr[cust].header != undefined) {
                for (var j = 0; j < this.customizeData.length; j++) {
                    if (this.custArr[cust].header == this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol) {
                        headerFlag = true;
                    }
                }
                if (!headerFlag) {
                    if (rec.data[this.custArr[cust].dataIndex] != undefined && rec.data[this.custArr[cust].dataIndex] != "null"){
                       if(this.custArr[cust].xtype=="datefield"){
                         var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(rec.data[this.custArr[cust].dataIndex] * 1));
                         this.expenseHeader += "<span class='gridRow' wtf:qtip=\"<div style='word-wrap: break-word;'>"+linelevel_datefield+"<div>\" style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(linelevel_datefield,15)+"&nbsp;</span>";
                       } else if(this.custArr[cust].fieldtype == 1 || this.custArr[cust].fieldtype == 13){
                            var regex = /(<([^>]+)>)/ig;
                            var val = rec.data[this.custArr[cust].dataIndex];
                            val = val.replace(/(<([^>]+)>)/ig,"");
                            var tip = val.replace(/"/g,'&rdquo;');
                            this.expenseHeader += "<span class='gridRow' wtf:qtip='" + tip + "' style='width: " + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(val, 15) + "&nbsp;</span>";
                    } else
                        this.expenseHeader += "<span class='gridRow' wtf:qtip='" + rec.data[this.custArr[cust].dataIndex] + "' style='width: " + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(rec.data[this.custArr[cust].dataIndex], 15) + "&nbsp;</span>";
                    }
                    else
                        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;&nbsp;</span>";
                }
            }
        }
        this.expenseHeader += "<br>";
        this.expenseHeader += "</div>";
        return this.expenseHeader;
    },
    getProductExpanderData: function(rec,sameParent, minWidth, widthInPercent){
        var israteincludegst = this.expandStore.getCount()>0 && rec.data.israteIncludingGst ? rec.data.israteIncludingGst : false;
    if (!sameParent) {
        this.Repeatheader = "";
        this.serialNumber = 0;
    }
    this.Repeatheader += "<div style='width: 100%;min-width:" + minWidth + "px'>";  
    var productname=this.withInvMode?rec.data['productdetail']: rec.data['productname'];

    this.Repeatheader += "<span class='gridNo'>"+(++this.serialNumber)+".</span>";
     if (productname == null || productname == "") {
            productname = '&nbsp';
      }
    //Column : Product Id for Inventory
    if(!this.withInvMode)
        var pid=rec.data['pid'];
    var productid=rec.data['productid']; // ERP-13247 [SJ]
    this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+pid+"' style='width: "+widthInPercent+"% ! important;'><a class='jumplink' wtf:qtip='"+pid+"' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\""+productid+"\","+this.isFixedAsset+")'>"+Wtf.util.Format.ellipsis(pid,10)+"</a></span>";  // ERP-13247 [SJ]

    //Column : Product Name
    this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+productname+"' style='width: "+widthInPercent+"% ! important;'><a class='jumplink' wtf:qtip='"+productname+"' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\""+productid+"\","+this.isFixedAsset+")'>"+Wtf.util.Format.ellipsis(productname,10)+"</a></span>";   // ERP-13247 [SJ]
    //Column : Product description
    if(rec.data['description']!=null && rec.data['description']!=""){
        var productdesc=rec.data['description'];
	productdesc = productdesc.replace(/<\/?[^>]+(>|$)/g, "");   //SDP-9944
	if(productdesc=="") {
            productdesc='&nbsp';    //If HTML Content is <br> only.
        }
    }else if(rec.data['desc']!=null && rec.data['desc']!="" && (this.moduleid=== Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId || this.moduleid=== Wtf.Acc_Customer_Quotation_ModuleId||this.moduleid=== Wtf.Acc_Vendor_Quotation_ModuleId||this.moduleid=== Wtf.Acc_Purchase_Requisition_ModuleId || this.isRFQ)){
        var productdesc=rec.data['desc'];
	productdesc = productdesc.replace(/<\/?[^>]+(>|$)/g, "");   //SDP-9944
	if(productdesc=="") {
            productdesc='&nbsp';    //If HTML Content is <br> only.
        }
    }else{
        var productdesc='&nbsp';
    }
    ((this.moduleid===Wtf.Acc_ConsignmentRequest_ModuleId) || (this.moduleid=== Wtf.Acc_Invoice_ModuleId)|| (this.moduleid=== Wtf.Acc_Vendor_Invoice_ModuleId) || (this.moduleid=== Wtf.Acc_Sales_Order_ModuleId)|| (this.moduleid=== Wtf.Acc_Purchase_Order_ModuleId)||(this.moduleid=== Wtf.Acc_Customer_Quotation_ModuleId)||(this.moduleid=== Wtf.Acc_Vendor_Quotation_ModuleId) || (this.moduleid=== Wtf.Acc_Purchase_Requisition_ModuleId) || this.isRFQ || (this.moduleid=== Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId)?this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+productdesc+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(productdesc,15)+"</a></span>":""); // Asset Description value is productdesc

    if(this.moduleid!=Wtf.Acc_Security_Gate_Entry_ModuleId &&!this.isCustomer &&  Wtf.account.companyAccountPref.countryid == '203' && this.moduleid!=Wtf.Acc_Vendor_Quotation_ModuleId && this.moduleid!=Wtf.Acc_RFQ_ModuleId &&  this.moduleid!=Wtf.Acc_Purchase_Requisition_ModuleId){
                    var permitno=rec.data['permit']=="undefined" ?"":rec.data['permit'];
        this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+permitno+"&nbsp;</span>";
    }else if(!this.withInvMode){
        var type = "";
        type = rec.data['type']
        this.Repeatheader += "<span class='gridRow' wtf:qtip='"+type+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(type,15)+"</span>";
    }
    else {
        this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
    }


    //Quantity
     if (companyid === SATSCOMPANY_ID) {
            if (isNaN(getRoundofValue(rec.data['showquantity']))) {//for quantity writtent in word
                this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + (Wtf.util.Format.ellipsis(rec.data['showquantity'], 15)) + "</span>";
                this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + (Wtf.util.Format.ellipsis(rec.data['showquantity'], 15)) + "</span>";
            } else {
                this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + parseFloat(getRoundofValue(rec.data['showquantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + " " + rec.data['unitname'] + "</span>";
                this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + parseFloat(getRoundofValue(rec.data['showquantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + " " + rec.data['unitname'] + "</span>";
            }
        } else {
            var v = parseFloat(getRoundofValue(rec.data['quantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var quantity = WtfGlobal.convertInDecimalWithDecimalDigit(v,"",Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var v1 = parseFloat(getRoundofValue(rec.data['baseuomquantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var baseuomquantity = WtfGlobal.convertInDecimalWithDecimalDigit(v1,"",Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + quantity + " " + rec.data['unitname'] + "</span>";
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + baseuomquantity + " " + rec.data['baseuomname'] + "</span>";
        }
    
               
    //balance Quantity
    if(!this.withInvMode && this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId &&(this.isOrder&&this.isCustomer||this.isOrder&&!this.isCustomer))
           if (!this.withInvMode && (this.isOrder && this.isCustomer || this.isOrder && !this.isCustomer)) {
            if (companyid === SATSCOMPANY_ID) {
                if (isNaN(getRoundofValue(rec.data['showquantity']))) {
                    this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + (Wtf.util.Format.ellipsis(rec.data['showquantity'], 15)) + "</span>";
                } else {
                    this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + parseFloat(getRoundofValue(rec.data['showquantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + " " + rec.data['unitname'] + "</span>";
                }
            } else {
                var v = parseFloat(getRoundofValue(this.isMRPJOBWORKOUT ? 1 : rec.data['balanceQuantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                var balanceQuantity = WtfGlobal.convertInDecimalWithDecimalDigit(v,"",Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + balanceQuantity + " " + rec.data['unitname'] + "</span>";
            }
        }
    
    /*-------Balance Quantity data will be shown when "To Show Requisition Status For PO" check ON from System Preferences-------  */
        if (!this.withInvMode && this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId) {
            if (Wtf.account.companyAccountPref.columnPref.statusOfRequisitionForPO) {
                this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + parseFloat(getRoundofValue(this.isMRPJOBWORKOUT ? 1 : rec.data['balanceQuantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + " " + rec.data['unitname'] + "</span>";
            } else {
                this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + "NA" + "</span>";
            }
        }
        
        if (!this.withInvMode && this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + parseFloat(getRoundofValue(this.isMRPJOBWORKOUT ? 1 : rec.data['shortfallQuantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + " " + rec.data['unitname'] + "</span>";
        }
    
    if (this.isMRPJOBWORKOUT) {
        this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+parseFloat(getRoundofValue(3)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+rec.data['unitname']+"</span>";
    }

    if((this.moduleid== Wtf.Acc_Invoice_ModuleId || this.moduleid== Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.calculateproductweightmeasurment){
        //Column : Unit Weight
        this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.weightRenderer(rec.data['productweightperstockuom'])+ "</span>";
        //Column : Unit Weight with Packageing
        this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.weightRenderer(rec.data['productweightincludingpakagingperstockuom']) + "</span>";
        //Column : Unit Volume
        this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.volumeRenderer(rec.data['productvolumeperstockuom'])+ "</span>";
        //Column : Unit Volume with Packageing
        this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.volumeRenderer(rec.data['productvolumeincludingpakagingperstockuom']) + "</span>";
    }
    //Unit Price
    if(!this.isRFQ) {
        var rate = (this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) ? rec.data.rate : (this.isQuotation || this.isOrder && !this.withInvMode? (israteincludegst ? rec.data.rateIncludingGst : rec.data.orderrate) : (israteincludegst ? rec.data.rateIncludingGst : rec.data.rate));
        var unitprice=WtfGlobal.withCurrencyUnitPriceRenderer(rate,true,rec);
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
            unitprice=Wtf.UpriceAndAmountDisplayValue;
        }
        if(this.isRequisition && (this.grid.getColumnModel().findColumnIndex("amountinbase") != -1 && this.grid.getColumnModel().config[this.grid.getColumnModel().findColumnIndex("amountinbase")].hidden)) {
            this.Repeatheader +="";
        } else {
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + unitprice + "</span>";
    }
            }

    //Partial Amount
    if((this.isCustomer && !this.isQuotation&& !this.isOrder&&!this.withInvMode)) {
        this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['partamount']+"% "+"&nbsp;</span>";
    }
               
    //Discount
    //                if(!this.isOrder) {
    if(!this.isRequisition && !this.isRFQ){
        if(rec.data.discountispercent == 0){
            this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
        } else {
            this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
        }
    }
    //Tax
    if(!this.isRequisition && !this.isRFQ){
        var taxAmount=WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'],rec.data['currencysymbol'],[true]);
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
            taxAmount=Wtf.UpriceAndAmountDisplayValue;
        }
        this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+taxAmount+"</span>";
    }
               
                                           
    //Amount
    var amount=0;
    //                if(this.isOrder && !this.isQuotation){
    //                    amount=rec.data['quantity']*rate;
    //                    amount+=(amount*rec.data['prtaxpercent']/100);
    //                }else{
                   
    amount=rec.data['quantity']*rate;
    amount = parseFloat(getRoundedAmountValue(amount)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
    if(rec.data['partamount'] != 0){
        amount = amount * (rec.data['partamount'] /100);
    }
                   
    var discount = 0;
    if(rec.data.prdiscount > 0) {
        if(rec.data['partamount'] != 0){
            discount = rec.data.partialDiscount;
        }else{
            if(rec.data.discountispercent == 0){
                discount = rec.data.prdiscount;
            } else {
                discount = (amount * rec.data.prdiscount) / 100;
            }
        }
    }
    discount = parseFloat(getRoundedAmountValue(discount)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
       
    amount=(amount-discount);
    if(!israteincludegst) {
        amount+=rec.data['rowTaxAmount'];//(amount*rec.data['prtaxpercent']/100);
    }
                   
    //                }
    if(!this.isRFQ){
        var amountValue = WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true]);
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
            amountValue=Wtf.UpriceAndAmountDisplayValue;
        }
        if(this.isRequisition && (this.grid.getColumnModel().findColumnIndex("amountinbase") != -1 && this.grid.getColumnModel().config[this.grid.getColumnModel().findColumnIndex("amountinbase")].hidden)) {
            this.Repeatheader += "";
        } else {
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + amountValue + "</span>";
    }
    }
                   
    if(!this.withInvMode&&(this.isOrder&&this.isCustomer)){
        // Balanec amount for the Sales order Report  
        var balanceAmt="";
        if(!Wtf.dispalyUnitPriceAmountInSales) {// when no permission to show unit price and amount for sales side docuemnt
            balanceAmt=Wtf.UpriceAndAmountDisplayValue;
        } else{
            var balanceamount = 0;
            balanceamount = (amount/rec.data['quantity'])*rec.data['balanceQuantity'];
            balanceAmt = WtfGlobal.addCurrencySymbolOnly(balanceamount,rec.data['currencysymbol'],[true]);
        }
        this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+balanceAmt+"</span>";
    }
    this.Repeatheader += (!this.isQuotation && !this.isOrder && !this.isConsignment && !this.isRFQ)?"<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['linkto']+"&nbsp;</span>":"";
    this.Repeatheader += (this.isCustomer && !this.isQuotation && this.isOrder && !this.isConsignment)?"<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['linkto']+"&nbsp;</span>":"";
    this.Repeatheader += (!this.isCustomer && !this.isQuotation && this.isOrder && !this.isRequisition&&!this.isConsignment)?"<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['linkto']+"&nbsp;</span>":"";
    this.Repeatheader += (this.isQuotation && !this.isOrder)?"<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['linkto']+"&nbsp;</span>":"";    
    if(this.isRequisition)
        this.Repeatheader += "<span class='gridRow'  style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.URLDecode(rec.data['approverremark'])+"&nbsp;</span>";
    var displayuom = rec.data['displayUOM'];
    if (displayuom == null || displayuom == "") {
        displayuom = '&nbsp';
    }
    if(CompanyPreferenceChecks.displayUOMCheck() === true )  
        this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+displayuom+"&nbsp;</span>";

    if(this.isOrder && !this.isConsignment && this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId&&this.moduleid!=Wtf.Acc_Security_Gate_Entry_ModuleId){
     var lineLevelstatus=rec.json.status;//status of line level
      var record = WtfGlobal.searchRecord(this.Store, rec.json.billid, 'billid');//Global record
           
            /* Displaying value of Action N/A if any PO/SO is
             * 
             * 1.Globally manually Closed.
             * 
             * 2.Status of PO/SO is closed due to completely used in transaction.
             * 
             * 3.If Line level is Used Completely then value of that particular line level is displaying N/A.
             * 
             * 4.If any line is manually Closed then that particular line leve value is dispalying N/A.*/
        
            if (lineLevelstatus == "Yes" || record.data.status=="Closed" || rec.json.balanceQuantity==0) {
                 (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))? this.Repeatheader +="<span class='gridRow' style='width: "+widthInPercent+"% ! important; '><span style='margin:-2px 14px;'>"+Wtf.util.Format.ellipsis('Closed', 25) +"&nbsp;</span></span>":"";
                //this.Repeatheader +="<span class='gridRow' style='width: "+widthInPercent+"% ! important; '><span style='margin:-2px 14px;'>"+Wtf.util.Format.ellipsis('N/A', 25) +"&nbsp;</span></span>";
            } else {
                var rowid = "";
                /* If PO/SO is linked with any parent document then product id key is saverowid otherwise it is rowid*/
                if (rec.json != undefined && rec.json.linkid != undefined && rec.json.linkid != "") {
                    rowid = rec.json.savedrowid;
                } else {
                    rowid = rec.json.rowid;
                }
                if(!this.isJobWorkOrderReciever){   //ERP-28389
                    (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))?this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + 'Mark Close' + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + 'Mark Item As Closed' + "' href='#' onClick='javascript:Wtf.onCellClickCloseLineItem(\"" + rowid + "\"," + this.isCustomer + ")'>" + Wtf.util.Format.ellipsis('Mark Close', 10) + "</a></span>":"";  // ERP-13247 [SJ]
                }
            }
           
     //this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+'Open'+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(lineLevelstatus,10)+"&nbsp;</span>";    
    }         
    if(this.isCustomer && !this.isQuotation && this.isOrder && this.isConsignment){
        var lineLevelstatus=rec.json.status;//status of line level
        var recordso = WtfGlobal.searchRecord(this.Store, rec.json.billid, 'billid');//Global record
        var approvedserials=rec.data['approvedserials']=="undefined" ?"":rec.data['approvedserials'];
        var approvalstatus=rec.data['approvalstatus']=="undefined" ?"":rec.data['approvalstatus'];
        //Approved Serials
        //                    header += "<span class='gridRow'  wtf:qtip='"+approvedserials+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(approvedserials,25)+"&nbsp;</span>";
                   
        //Approval Status
        this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+approvalstatus+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(approvalstatus,25)+"&nbsp;</span>";  
        if(this.moduleid===Wtf.Acc_ConsignmentRequest_ModuleId && approvalstatus=="Rejected"){
             (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))? this.Repeatheader +="<span class='gridRow' style='width: "+widthInPercent+"% ! important; '><span style='margin:-2px 14px;'>&nbsp;</span></span>":"";
        }else if((this.moduleid===Wtf.Acc_ConsignmentRequest_ModuleId && (rec.json.isrejected != undefined &&  rec.json.isrejected == true ))){
            (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))? this.Repeatheader +="<span class='gridRow' style='width: "+widthInPercent+"% ! important; '><span style='margin:-2px 14px;'>"+Wtf.util.Format.ellipsis('Rejected by Delivery', 25) +"&nbsp;</span></span>":"";
        }else if(lineLevelstatus != "Yes" && recordso.data.status!="Closed" && rec.json.balanceQuantity>=0){
            (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))? this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + 'Mark Close' + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + 'Mark Item As Closed' + "' href='#' onClick='javascript:Wtf.onCellClickCloseLineItem(\"" + rec.json.rowid + "\"," + this.isCustomer + ")'>" + Wtf.util.Format.ellipsis('Mark Close', 10) + "</a></span>":"";  // ERP-13247 [SJ]
        }else{
            (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))? this.Repeatheader +="<span class='gridRow' style='width: "+widthInPercent+"% ! important; '><span style='margin:-2px 14px;'>"+Wtf.util.Format.ellipsis('Closed', 25) +"&nbsp;</span></span>":"";
        }
        //Reject Line Item
        if(this.moduleid===Wtf.Acc_ConsignmentRequest_ModuleId){ 
            if(lineLevelstatus != "Yes" && recordso.data.status!="Closed" && (rec.json.isrejected != undefined &&  rec.json.isrejected != true ) && rec.json.balanceQuantity>=0){
                (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))? this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + 'Reject Item' + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + 'Reject Item' + "' href='#' onClick='javascript:Wtf.onCellClickRejectLineItem(\"" + rec.json.rowid + "\"," + this.isCustomer + ")'>" + Wtf.util.Format.ellipsis('Reject Item', 12) + "</a></span>" : "";  
            }else{
                (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))? this.Repeatheader +="<span class='gridRow' style='width: "+widthInPercent+"% ! important; '><span style='margin:-2px 14px;'>"+Wtf.util.Format.ellipsis('', 25) +"&nbsp;</span></span>":"";
            }
            if((rec.data['isrejected'] != undefined &&  rec.data['isrejected'] == true) && rec.data['rejectionreason'] != undefined && rec.data['rejectionreason'] != ''){
                (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))? this.Repeatheader +="<span class='gridRow' style='width: "+widthInPercent+"% ! important; '><span style='margin:-2px 14px;'>"+Wtf.util.Format.ellipsis(rec.data['rejectionreason'], 25) +"&nbsp;</span></span>":"";
            }else{
                (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))? this.Repeatheader +="<span class='gridRow' style='width: "+widthInPercent+"% ! important; '><span style='margin:-2px 14px;'>"+Wtf.util.Format.ellipsis('', 25) +"&nbsp;</span></span>":"";
            }
        }
        
    }
    else if(this.moduleid=="63" && this.isOrder && this.isConsignment){
        var lineLevelstatusPo=rec.json.status;//status of line level
        var recordPo = WtfGlobal.searchRecord(this.Store, rec.json.billid, 'billid');//Global record
        if(lineLevelstatusPo != "Yes" && recordPo.data.status!="Closed" && rec.json.balanceQuantity>=0){
            this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + 'Mark Close' + "' style='width: " + widthInPercent + "% ! important;'><a class='jumplink' wtf:qtip='" + 'Mark Item As Closed' + "' href='#' onClick='javascript:Wtf.onCellClickCloseLineItem(\"" + rec.json.rowid + "\"," + this.isCustomer + ")'>" + Wtf.util.Format.ellipsis('Mark Close', 10) + "</a></span>";  // ERP-13247 [SJ]
                }
        }  

    //Blank Column
    if(!this.withInvMode && !this.isRequisition)
        this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['productmoved']+"</span>";
                  
    for(var cust=0;cust<this.custArr.length;cust++){
        var headerFlag=false;
        if(this.custArr[cust].header != undefined ) {
                   
            for(var j=0;j<this.customizeData.length;j++){
                if(this.custArr[cust].header==this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol){
                    headerFlag=true;
                }
            }
            if(!headerFlag){
                if(rec.data[this.custArr[cust].dataIndex]!=undefined && rec.data[this.custArr[cust].dataIndex]!="null" && rec.data[this.custArr[cust].dataIndex]!=""){
                    if(this.custArr[cust].xtype=="datefield"){
                        var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(rec.data[this.custArr[cust].dataIndex] * 1));
                        this.Repeatheader += "<span class='gridRow' wtf:qtip=\"<div style='word-wrap: break-word;'>"+linelevel_datefield+"<div>\" style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(linelevel_datefield,15)+"&nbsp;</span>";
                    } else if(this.custArr[cust].fieldtype == 1 || this.custArr[cust].fieldtype == 13 |this.custArr[cust].fieldtype == 15){
                        var regex = /(<([^>]+)>)/ig;
                        var val = rec.data[this.custArr[cust].dataIndex];
                        val = val.replace(/(<([^>]+)>)/ig,"");
                        var tip = val.replace(/"/g,'&rdquo;');
                        this.Repeatheader += "<span class='gridRow' wtf:qtip=\"<div style='word-wrap: break-word;'>"+tip+"<div>\" style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(val,15)+"&nbsp;</span>";//ERP-4374 [SJ]
                    
                   } else{
                        var val = rec.data[this.custArr[cust].dataIndex];
                        this.Repeatheader += "<span class='gridRow' wtf:qtip=\"<div style='word-wrap: break-word;'>"+tip+"<div>\" style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(val,15)+"&nbsp;</span>";//ERP-4374 [SJ]
                   }
                }else
                    this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
            }
        } 
                  
                  
                  
    }
    //           if(!this.isCustBill)
    //                header += "<span class='gridRow'>"+rec.data['desc']+"&nbsp;</span>";
    this.Repeatheader += "<br>";
    this.Repeatheader += "</div>";
    return this.Repeatheader;
 
    },
    onCellClick:function(g,i,j,e){
        this.cellclickhandler(g,i,j,e);
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return; 
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.Store.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true, this.consolidateFlag,null,null,null,this.startDate.getValue(),this.endDate.getValue());
        } else if(header=="billno"){
            this.viewTransection(g,i,e)
        } else if(header == "linkedpayment"){
            var formrec = this.Store.getAt(i);
            var type="Payment Received";
            var withoutinventory=formrec.data['withoutinventory'];  
            var billid=formrec.data['linkedpaymentID'];
            
            if(type !='' && type != null && type != undefined){
                viewTransactionTemplate1(type, formrec,withoutinventory,billid);            
            }    
        }
        else if(header=="prno"){//when clicked on Purchase requition number in RFQ Reoprt
            if(i<0&&this.grid.getStore().getAt(i)==undefined ||this.grid.getStore().getAt(i)==null ){
                WtfComMsgBox(15,2);
                return;
            }
            
            var purchase_req_no = "";
            if (e.target.innerHTML != undefined && e.target.innerHTML != "")   // Multiple links in single row
                purchase_req_no = e.target.innerHTML;
            //var formRecord = this.grid.getStore().getAt(i);
            var moduleid= this.isFixedAsset?Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId:Wtf.Acc_Purchase_Requisition_ModuleId;
            this.viewTransection(g,i,e)
//            WtfGlobal.callViewMode(purchase_req_no,moduleid,undefined,false,this.isDraft);
        } else if(header=="rfqno"){//when clicked on RFQ number in Purchase Requisition Reoprt
            if(i<0&&this.grid.getStore().getAt(i)==undefined ||this.grid.getStore().getAt(i)==null ){
                WtfComMsgBox(15,2);
                return;
            }
            var rfq_no = "";
            if (e.target.innerHTML != undefined && e.target.innerHTML != "")   // Multiple links in single row
                rfq_no = e.target.innerHTML;
            // var formRecord = this.grid.getStore().getAt(i);
            var moduleid= this.isFixedAsset?Wtf.Acc_FixedAssets_RFQ_ModuleId:Wtf.Acc_RFQ_ModuleId;
            WtfGlobal.callViewMode(rfq_no,moduleid,undefined,false);
        }
    },
    expandInvoice:function(id,exponly){
        this.invID=id
        if(exponly){
            this.pagingToolbar.hide();
            this.exponly=exponly;
            this.Store.load({
                params:{
                    billid:id
                }
            });
            this.Store.on('load',this.expandRow, this);
        }
    },
    expandRow:function(){
WtfGlobal.resetAjaxTimeOut();
        if(this.Store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
//            if(this.printButton)this.printButton.disable();
            var selTypeVal = this.typeEditor.getValue();
            var emptyTxt = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            if(!this.pendingapproval) {
                if(selTypeVal == 3) {//deleted
                    emptyTxt = this.deletedRecordsEmptyTxt;
                } else if(selTypeVal == 0 || selTypeVal == 4) {//All or Exclude deleted
                    emptyTxt = this.isRequisition ? (this.isFixedAsset? this.emptytext6 : this.emptytext5) : this.emptytext1+(this.isOrder?"":"<br>"+this.emptytext2);
                }else if(selTypeVal == 1) {//Cash Sales
                    emptyTxt = this.isOrder?"":"<br>"+this.emptytext2;
                } else if(selTypeVal == 2) {//Invoice
                    emptyTxt = this.emptytext1;
                } else if(selTypeVal == 5) {//Invoice
                    emptyTxt = '<div style="font-size:15px; text-align:center; color:#CCCCCC; font-weight:bold; margin-top:7%;">'+WtfGlobal.getLocaleText("acc.field.NoFavouriteRecordAvailable")+'<br></div>';
                }
                if(this.isQuotation){
                    emptyTxt = this.isCustomer ? this.emptytext3 : this.isFixedAsset? this.emptytext7:this.emptytext4;
                } else if(this.isRFQ){
                    emptyTxt = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                }
            }else if(this.pendingapproval){//ERP-20502
                emptyTxt = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            }
           
            this.grid.getView().emptyText=emptyTxt;
            this.grid.getView().refresh();
        }else{
            if(this.exportButton)this.exportButton.enable();
//            if(this.printButton)this.printButton.enable();
        }
        this.Store.filter('billid',this.invID);
        if(this.exponly && (this.Store.getCount() !== 0)){
            this.expander.toggleRow(0);
        }
     },
     
     
    /*  Applying Color for Outstanding PO to show Colored PO in Outstanding Report
     * Called from Report List
     * */
    outstandingPOReport: function() {
        if (this.isfromReportList && !this.isCustomer) {

            //Overrided function of HirarchicalGridPanel
            this.grid.getView().getRowClass = function(record) {
                var colorCss = " x-grid3-row-expandedacc";
                switch (record.json.color) {
                    case "G":
                        colorCss = " green-background";
                        break;
                    case "R":
                        colorCss = " darkred-background";
                        break;
                    case "Y":
                        colorCss = " yellow-background";
                        break;
                }
                return colorCss;
            }


        }
    },
    
//    hideMsg: function(store){
//         
//       if(this.isRFQ){
//           this.hideTransactionReportFields(Wtf.account.HideFormFieldProperty.requestForQuotation,store); 
//        } else{
//           this.hideTransactionReportFields(Wtf.account.HideFormFieldProperty.purchaseRequisition,store);
//        }
//         
//         
//    },
//
//
//    
//     hideTransactionReportFields: function(array, store) {
//        if (array) {
//            var cm = this.grid.getColumnModel();
//            for (var i = 0; i < array.length; i++) {
//                for (var j = 0; j < cm.config.length; j++) {
//                    if (cm.config[j].header === array[i].dataHeader || (cm.config[j].dataIndex === array[i].fieldId)) {
//                       cm.setHidden(j,(cm.config[j].iscustomcolumn !== undefined && cm.config[j].iscustomcolumn) ? array[i].isHidden : array[i].isReportField);
//                    }
//                }
//            }
//            this.grid.reconfigure(store, cm);
//        }
//    },
    
    /*  Applying Color for Outstanding SO to show Colored SO in Outstanding Report
     * Called from Report List
     * */
    outstandingSOReport: function() {
        if (this.isfromReportList && this.isCustomer) {
            for (var i = 0; i < this.Store.getCount(); i++) {
                this.recArr = this.Store.getAt(i);

                if (this.recArr.json.greenColor) {
                    WtfGlobal.highLightRowColor(this.grid, this.recArr, true, 0, 5);//green
                } else if (this.recArr.json.yellowColor) {
                    WtfGlobal.highLightRowColor(this.grid, this.recArr, true, 0, 6);//yellow
                } else {
                    WtfGlobal.highLightRowColor(this.grid, this.recArr, true, 0, 7);//red
                }


            }


        }
    },
    
    loadStore:function(){
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
       this.Store.load({
           params : {
               start : 0,
               limit :  this.pP.combo!=undefined?this.pP.combo.value:30,
               ss : this.quickPanelSearch.getValue(),
               pagingFlag:true
           }
       });
       this.Store.on('load',this.storeloaded,this);
    },
   
    deleteTransactionCheckBefore:function(del){                           //check whether record from POS or not
   this.recArr = this.grid.getSelectionModel().getSelections();
    var biilNo;
    var isFirst=true;
    var checkHasRecurringInvFlag = false;var invoiceno="";
    var failToDeleteDueToWriteOff=false;
    var invoicesNotDeletedDueToWriteOff='';
    for(var k=0;k< this.recArr.length;k++){
        var rec=this.recArr[k];
        if(rec.data.isRepeated && (!(rec.data.childCount==0 && this.moduleid==Wtf.Acc_Invoice_ModuleId))){  //Source Invoice cannot delete if it has recurred Invoice.
            checkHasRecurringInvFlag=true;
            invoiceno=rec.data.billno;
            break;
        }
        if(rec.data.isFromPOS  && isFirst ){
            biilNo=rec.data.billno;
            isFirst=false;
        }else if(rec.data.isFromPOS){
            biilNo+=", "+rec.data.billno;
        }
    }
    if(this.isCustomer && !this.isQuotation && !this.isOrder){
        for(var j=0;j< this.recArr.length;j++){
            var rec=this.recArr[j];
            if(rec.data.isWrittenOff && !rec.data.isRecovered){
                failToDeleteDueToWriteOff=true;
                invoicesNotDeletedDueToWriteOff+='<b>'+rec.data.billno+'</b>'+",";
            }
        }
        if(failToDeleteDueToWriteOff && invoicesNotDeletedDueToWriteOff!=''){
            invoicesNotDeletedDueToWriteOff=invoicesNotDeletedDueToWriteOff.substring(0,invoicesNotDeletedDueToWriteOff.length-1);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.writeOff.invoices")+" "+invoicesNotDeletedDueToWriteOff+" "+WtfGlobal.getLocaleText("acc.writeOff.invoiceCanNotBeDeleted")], 2);
            return;
        }
    }
   if(biilNo !=undefined){
       Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),""+biilNo+" "+WtfGlobal.getLocaleText("acc.rem.250"),function(btn){
           if(btn=="yes") {
               this.handleDelete(del);
           }
       },this);
   } else if(!checkHasRecurringInvFlag){
       this.handleDelete(del);
   } else {
       WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.label +" <b>"+invoiceno+"</b> "+WtfGlobal.getLocaleText("acc.field.hasrecurringInvoice.postedsocannotbedeleted")], 2);
   }
},
       handleDelete:function(del){
          var delFlag=del;
        if(this.pendingapproval){
            var formRecord = this.grid.getSelectionModel().getSelected();
            var approvalstatusint = formRecord.data.approvalstatusint;
           
            //Calculate approval permission of selected record
            var alertFlag = false;
            if(approvalstatusint == 1){
                if(this.isOrder) {
                    if(this.isCustomer) {
                        if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.salesorderapprovelevelone)) {
                            alertFlag = true;
                        }
                    } else {
                        if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.purchaseorderapprovelevelone)) {
                            alertFlag = true;
                        }
                    }

                } else {
                    if(this.isCustomer) {
                        if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.customerinvoiceapprovelevelone)) {
                            alertFlag = true;
                        }
                    } else {
                        if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.vendorinvoiceapprovelevelone)) {
                            alertFlag = true;
                        }
                    }
                }
                if(alertFlag){
                    Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.YoudonthavepermissionforLevel1rejectionof")+this.label+WtfGlobal.getLocaleText("acc.field.Pleasecontactcompanyadministrotor"));
                    return;
                }
            } else if(approvalstatusint == 2) {
                if(this.isOrder) {
                    if(this.isCustomer) {
                        if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.salesorderapproveleveltwo)) {
                            alertFlag = true;
                        }
                    } else {
                        if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.purchaseorderapproveleveltwo)) {
                            alertFlag = true;
                        }
                    }
                }else {
                    if(this.isCustomer) {
                        if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.customerinvoiceapproveleveltwo)) {
                            alertFlag = true;
                        }
                    } else {
                        if(WtfGlobal.EnableDisable(Wtf.UPerm.approvals, Wtf.Perm.approvals.vendorinvoiceapproveleveltwo)) {
                            alertFlag = true;
                        }
                    }
                }
                if(alertFlag){
                    Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.YoudonthavepermissionforLevel2rejectionof")+this.label+WtfGlobal.getLocaleText("acc.field.Pleasecontactcompanyadministrotor"));
                    return;
                }
            }
        }
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.withInvMode = this.recArr[0].data.withoutinventory;
        this.grid.getSelectionModel().clearSelections();
           WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
           var unblockMsg="";
           if ((this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId)) {
                for (var i = 0; i < this.recArr.length; i++) {
                    if(this.recArr[i].data != undefined && this.recArr[i].data.isSOPOBlock != undefined && this.recArr[i].data.isSOPOBlock){
                       unblockMsg = this.moduleid == Wtf.Acc_Sales_Order_ModuleId ? WtfGlobal.getLocaleText("acc.field.blockedPOWillBeDeleted") : WtfGlobal.getLocaleText("acc.field.blockedSOWillBeDeleted");
                    }
                }
            }
         Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), unblockMsg+" "+WtfGlobal.getLocaleText("acc.rem.146")+" "+this.label+"?",function(btn){
        if(btn!="yes") {
            for(var i=0;i<this.recArr.length;i++){
                var ind=this.Store.indexOf(this.recArr[i])
                var num= ind%2;
                WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
            }
            return;
        }
            this.loadingMask = new Wtf.LoadMask(document.body, {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            });
            this.loadingMask.show();
        for(i=0;i<this.recArr.length;i++){
                arr.push(this.Store.indexOf(this.recArr[i]));
        }
        var mode=(this.withInvMode?23:15);
        if(this.isOrder){
            mode=(this.withInvMode?54:44);
        }
//        data= WtfGlobal.getJSONArray(this.grid,true,arr);
        var idData = "";
        for(var i=0;i<this.recArr.length;i++){
            var rec = this.recArr[i];
            idData += "{\"billid\":\""+rec.get('billid')+"\","+"\"journalentryid\":\""+rec.get('journalentryid')+"\"},";
        }
        if(idData.length>1){
            idData=idData.substring(0,idData.length-1);
        }
        data="["+idData+"]";
        this.ajxUrl = "";
        //this.isCustBill?23:15
        if(this.businessPerson=="Customer"){
           if(delFlag=='del' ){ 
            this.ajxUrl = "ACCInvoiceCMN/"+(this.withInvMode?"deleteBillingInvoices":"deleteInvoice")+".do";
           }else if(delFlag=='delp' ){
            this.ajxUrl = "ACCInvoiceCMN/"+(this.withInvMode?"deleteBillingInvoices":"deleteInvoicePermanent")+".do";
          }
            if(this.isQuotation){
                  if(delFlag=='del' ){
                     this.ajxUrl = "ACCSalesOrder/deleteQuotations.do";
                 }else if(delFlag=='delp' ){
                     this.ajxUrl = "ACCSalesOrder/deleteQuotationsPermanent.do";
               }
            } else if(this.isOrder){
               if(delFlag=='del' ){
                 this.ajxUrl = this.withInvMode?"ACCSalesOrder/deleteBillingSalesOrders.do":"ACCSalesOrder/deleteSalesOrders.do"
           }else if(delFlag=='delp' ){
            this.ajxUrl = this.withInvMode?"ACCSalesOrder/deleteBillingSalesOrders.do":"ACCSalesOrder/deleteSalesOrdersPermanent.do"
          }
         }
        }else if((this.businessPerson=="Vendor")){
           if(delFlag=='del' ){           
               this.ajxUrl = "ACCGoodsReceiptCMN/"+(this.withInvMode?"deleteBillingGoodsReceipt":"deleteGoodsReceipt")+".do";
            }else if(delFlag=='delp' ){
                this.ajxUrl = "ACCGoodsReceiptCMN/"+(this.withInvMode?"deleteBillingGoodsReceipt":"deleteGoodsReceiptPermanent")+".do";
          }
            if(this.isQuotation){
                 if(delFlag=='del' ){
                     this.ajxUrl = "ACCPurchaseOrder/deleteQuotations.do";
                 }else if(delFlag=='delp' ){
                     this.ajxUrl = "ACCPurchaseOrder/deleteQuotationsPermanent.do";
               }
            } else if(this.isOrder){
               if(delFlag=='del' ){
                   this.ajxUrl = this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId?"ACCPurchaseOrder/deleteSecuriyGateEntry.do":this.withInvMode?"ACCPurchaseOrder/deleteBillingPurchaseOrders.do":"ACCPurchaseOrder/deletePurchaseOrders.do"
                 }else if(delFlag=='delp' ){
                 this.ajxUrl = this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId?"ACCPurchaseOrder/deleteSecurityGateEntryPermanent.do":this.withInvMode?"ACCPurchaseOrder/deleteBillingPurchaseOrders.do":"ACCPurchaseOrder/deletePurchaseOrdersPermanent.do"
             }
         }
        }
        if(this.isRequisition){
            if(delFlag=='del' ){
                this.ajxUrl = "ACCPurchaseOrder/deletePurchaseRequisition.do";
            }else{
                this.ajxUrl = "ACCPurchaseOrder/deletePurchaseRequisitionPermanent.do";
            }
        } else if(this.isRFQ){
            if(delFlag=='del' ){
                this.ajxUrl = "ACCPurchaseOrder/deleteRFQ.do"
            }else{
                this.ajxUrl = "ACCPurchaseOrder/deleteRFQPermanent.do";
            }
        }
     var incash=this.recArr[0].data.incash;
     WtfGlobal.setAjaxTimeOut();
              Wtf.Ajax.requestEx({
                url:this.ajxUrl,
//                url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                params:{
                   data:data,
                    mode:mode,
                    isFixedAsset:this.isFixedAsset,
                    isMultiGroupCompanyFlag:Wtf.account.companyAccountPref.activateGroupCompaniesFlag,
                    isLeaseFixedAsset:this.isLeaseFixedAsset,
                    isConsignment:this.isConsignment,
                    incash:incash,
                    countryid: Wtf.account.companyAccountPref.countryid,
                    isVendorJobWorkOrder:this.isJobWorkOrderReciever
                }
            },this,this.genSuccessResponse,this.genFailureResponse);           
        },this);
    },
    genSuccessResponse:function(response){
         this.loadingMask.hide();
         WtfGlobal.resetAjaxTimeOut();
         var superThis = this;
         WtfComMsgBox([this.label,response.msg],response.success*2+2,"","",function(btn){
             if(btn=="ok"){
                for(var i=0;i<superThis.recArr.length;i++){
                    var ind=superThis.Store.indexOf(superThis.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(superThis.grid,superThis.recArr[i],false,num,2,true);
                }
                if (response.success) {
                    superThis.loadStore();
                    Wtf.productStore.reload();
                    Wtf.productStoreSales.reload();
                }
             }
         });
    },
    genFailureResponse:function(response){
         this.loadingMask.hide();
        WtfGlobal.resetAjaxTimeOut();
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        if (response.failure) {
            superThis.loadStore();
            Wtf.productStore.reload();
            Wtf.productStoreSales.reload();
        }
    },
   handleConsignmentRequestClose:function(del){  
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.withInvMode = this.recArr[0].data.withoutinventory;
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
         Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rem.146.1")+" "+this.label+"?",function(btn){
        if(btn!="yes") {
            for(var i=0;i<this.recArr.length;i++){
                var ind=this.Store.indexOf(this.recArr[i])
                var num= ind%2;
                WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
            }
            return;
        }
        for(i=0;i<this.recArr.length;i++){
                arr.push(this.Store.indexOf(this.recArr[i]));
        }
        var mode=(this.withInvMode?23:15);
        if(this.isOrder){
            mode=(this.withInvMode?54:44);
        }
//        data= WtfGlobal.getJSONArray(this.grid,true,arr);
        var idData = "";
        for(i=0;i<this.recArr.length;i++){
            var rec = this.recArr[i];
            idData += "{\"billid\":\""+rec.get('billid')+"\"},";
        }
        if(idData.length>1){
            idData=idData.substring(0,idData.length-1);
        }
        data="["+idData+"]";
        this.ajxUrl = "";
        //this.isCustBill?23:15
//        if(this.businessPerson=="Customer"){
//            this.ajxUrl ="ACCSalesOrder/closeConsignmentRequest.do"
//        }
      
     WtfGlobal.setAjaxTimeOut();
              Wtf.Ajax.requestEx({
                url:"ACCSalesOrder/closeConsignmentRequest.do",
                params:{
                   data:data,
                    mode:mode
                  
                }
            },this,this.genSuccessResponseclose,this.genFailureResponseclose);           
        },this);
    },
    genSuccessResponseclose:function(response){
         WtfGlobal.resetAjaxTimeOut();
         var superThis = this;
         WtfComMsgBox([this.label,response.msg],response.success*2+1,"","",function(btn){
             if(btn=="ok"){
                for(var i=0;i<superThis.recArr.length;i++){
                    var ind=superThis.Store.indexOf(superThis.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(superThis.grid,superThis.recArr[i],false,num,2,true);
                }
                if(response.success){
                (function(){
                superThis.loadStore();
                }).defer(WtfGlobal.gridReloadDelay(),superThis);
                }
             }
         });
    },
    genFailureResponseclose:function(response){
        WtfGlobal.resetAjaxTimeOut();
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    handleReject:function(){
       
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        if (this.recArr[0].data.status != "" && this.recArr[0].data.status == "Closed" && ( this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId)) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.common.cannotapprovereject")], 2);
            return;
        }
        this.withInvMode = this.recArr[0].data.withoutinventory;
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttorejectselected")+this.label+"?",function(btn){
        if(btn!="yes") {
            for(var i=0;i<this.recArr.length;i++){
                var ind=this.Store.indexOf(this.recArr[i])
                var num= ind%2;
                WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
            }
            return;
        }
        for(i=0;i<this.recArr.length;i++){
                arr.push(this.Store.indexOf(this.recArr[i]));
        }
        var mode=(this.withInvMode?23:15);
        if(this.isOrder){
            mode=(this.withInvMode?54:44);
        }
        data= WtfGlobal.getJSONArray(this.grid,true,arr);
        this.ajxUrl = "";
        //this.isCustBill?23:15
        if(this.businessPerson=="Customer"){
            this.ajxUrl = "ACCInvoice/rejectPendingInvoice.do";
            if(this.isOrder){
                this.ajxUrl = this.withInvMode?"ACCSalesOrder/deleteBillingSalesOrders.do":"ACCSalesOrder/rejectPendingSalesOrder.do"
            }else if(this.isQuotation){
                this.ajxUrl = "ACCSalesOrder/rejectPendingCustomerQuotation.do";
            }
        }else if((this.businessPerson=="Vendor")){
            this.ajxUrl = "ACCGoodsReceipt/rejectPendingGR.do";
            if(this.isOrder){
                this.ajxUrl = this.withInvMode?"ACCPurchaseOrder/deleteBillingPurchaseOrders.do":"ACCPurchaseOrder/rejectPendingPurchaseOrder.do"
            }else if(this.isQuotation){
                this.ajxUrl = "ACCPurchaseOrder/rejectPendingVendorQuotation.do";
            }
        }
//        if(this.isQuotation){
//        	this.ajxUrl = "ACCSalesOrder/deleteQuotations.do";
//        }
        if(this.isRequisition){
            this.ajxUrl = "ACCPurchaseOrder/rejectPurchaseRequisition.do";
        } 
        /*
         * Added Remark window while rejecting document 
         * changes done under SDP-16342
         */
            this.remarkWin = new Wtf.Window({
                height: 270,
                width: 360,
                maxLength: 1000,
                title: WtfGlobal.getLocaleText("Reject Requisition"),
                bodyStyle: 'padding:5px;background-color:#f1f1f1;',
                autoScroll: true,
                allowBlank: false,
                layout: 'border',
                items: [{
                        region: 'north',
                        border: false,
                        height: 70,
                        bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                        html: getTopHtml(WtfGlobal.getLocaleText("reject pending ") + this.label, WtfGlobal.getLocaleText("reject pending ") + this.label + " <b>" + this.recArr[0].data.billno + "</b>", "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                    }, {
                        region: 'center',
                        border: false,
                        layout: 'form',
                        bodyStyle: 'padding:5px;',
                        items: [this.remarkField = new Wtf.form.TextArea({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.AddRemark*"),
                                width: 200,
                                height: 100,
                                allowBlank: false,
                                maxLength: 1024
                            })]
                    }],
                modal: true,
                buttons: [{
                        text: WtfGlobal.getLocaleText("acc.field.Reject"),
                        scope: this,
                        id: "Rejectbtn" + this.id,
                        handler: function () {
                            Wtf.getCmp("Rejectbtn" + this.id).disable();
                            var incash = this.recArr[0].data.incash;
                            Wtf.Ajax.requestEx({
                                url: this.ajxUrl,
//                url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                                params: {
                                    data: data,
                                    mode: mode,
                                    isReject: true,
                                    incash: incash,
                                    isFixedAsset: this.isFixedAsset,
                                    isLeaseFixedAsset: this.isLeaseFixedAsset,
                                    totalorderamount: this.recArr[0].data.amountinbase,
                                    remark: this.remarkField.getValue()
                                }
                            }, this, this.genSuccessResponseReject, this.genFailureResponseReject);
                        }
                    }, {
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope: this,
                        handler: function () {
                            this.remarkWin.close();
                        }
                    }]
            });
            this.remarkWin.show();
        }, this);
    },
    genSuccessResponseReject:function(response){
        this.remarkWin.close();
        if(this.isQuotation || this.isOrder || this.isRequisition || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId){
            WtfComMsgBox([this.label,response.msg],response.success*2+1);
        } else {
            WtfComMsgBox([this.label,this.label+" " + WtfGlobal.getLocaleText("acc.field.hasbeenrejectedsuccessfully")],response.success*2+1);
        }
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            (function(){
                this.loadStore();
            }).defer(WtfGlobal.gridReloadDelay(),this);

        }
    },
    genFailureResponseReject:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg) {
            msg=response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
//    makePayment:function(){
//        if(this.sm.getCount()==1){
//            var invoiceRecord = this.sm.getSelected();
//            this.withInvMode = invoiceRecord.data.withoutinventory;
////            if(this.withInvMode){
////                this.isCustomer? callBillingReceipt(true, invoiceRecord) : callBillingPayment(true, invoiceRecord);
////            } else {
//                this.isCustomer? callReceipt(true, invoiceRecord) : callPayment(true, invoiceRecord);
////            }
//           
//            /*if(this.operationType==Wtf.autoNum.Invoice) {
//                callReceipt(true, invoiceRecord);
//            } else if(this.operationType==Wtf.autoNum.BillingInvoice) {
//                callBillingReceipt(true, invoiceRecord);
//            } else if(this.operationType==Wtf.autoNum.GoodsReceipt) {
//                callPayment(true, invoiceRecord);
//            } else if(this.operationType==Wtf.autoNum.BillingGoodsReceipt) {
//                callBillingPayment(true, invoiceRecord);
//            }*/
//        }
//    },
   
    generateGIROFile : function() {
        this.vendorRec = new Wtf.data.Record.create([
            {name: 'billid'},
            {name: 'billno'},
            {name: 'personname'},
            {name: 'amount'},
            {name: 'amountdue'},
        ]);
        this.vendorRecDS = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.vendorRec),
//            url:Wtf.req.account+'CompanyManager.jsp',
            url : "ACCPurchaseOrder/getRequisitionFlowData.do"
        });
//        this.reqRuleds.load();
        this.vendorReccm= new Wtf.grid.ColumnModel([{
            hidden:true,
            dataIndex:'billid'
        },{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.cn.9"),
            dataIndex:'billno',
            pdfwidth:75,
            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header:(this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven")),  //this.businessPerson,
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer,
            dataIndex:'personname'
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),  //"Total Amount",
            align:'right',
            dataIndex:'amount',
            pdfwidth:75,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.amtDue"),  //"Amount Due",
            dataIndex:'amountdue',
            align:'right',
            pdfwidth:75,
            renderer:(this.isOrder?WtfGlobal.currencyRendererDeletedSymbol:WtfGlobal.withoutRateCurrencyDeletedSymbol)           
        }]);
        this.vendorRecGrid = new Wtf.grid.GridPanel({
//            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:270,
            autoWidth : true,
//            width:580,
            title:WtfGlobal.getLocaleText("acc.field.SelectedInvoices"),
            store: this.vendorRecDS,
            cm: this.vendorReccm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
    
        this.GIROBankDS = new Wtf.data.SimpleStore({
           fields:['id','name'],
           data: [
           ['1','DBS Bank Ltd']
           ]
       });
       this.GIROBankCombo=new Wtf.form.ComboBox({
           fieldLabel: WtfGlobal.getLocaleText("coa.masterType.bank"), //WtfGlobal.getLocaleText("hrms.common.Gender")+"*",
           hiddenName:'id',
           store:this.GIROBankDS,
           displayField:'name',
           valueField:'id',
           forceSelection: true,
           selectOnFocus:true,
           triggerAction: 'all',
           typeAhead:true,
           mode: 'local',
           width:220,
           allowBlank:false
       });

       this.GIROFileForm=new Wtf.form.FormPanel({
//            frame:true,
            url:"GIROFile/generateGIROFile.do",
            labelWidth: 125,
            border : false,
            autoHeight:true,
            bodyStyle:'padding:5px 5px 0',
            autoWidth:true,
            defaults: {anchor:'94%'},
            defaultType: 'textfield',
            items:[this.GIROBankCombo, this.vendorRecGrid],
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.field.GenerateGIROFile"),
                handler:function(){
                    var arr=this.grid.getSelectionModel().getSelections();
                    if(arr.length>0 && this.GIROBankCombo.getValue()!="") {
                        var invoiceID = [];
                        for(var cnt=0;cnt<arr.length;cnt++) {
                            invoiceID.push(arr[cnt].data.billid);
                        }
                        this.GIROFileForm.getForm().submit({
                            waitMsg:WtfGlobal.getLocaleText("acc.field.GeneratingGIROFile"),
                            baseParams:{
                                invoiceids : invoiceID,
                                bankid : this.GIROBankCombo.getValue()
                            },
                            scope:this,
                            success:function(f,a){
                                this.GIROFileWin.close();
                                var response = eval('('+a.response.responseText+')')
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.GIRO.Status=Status"),response.data.msg],response.success*2+1);
                            },
                            failure:function(f,a){this.Rulewin.close();this.genFailureResponse(eval('('+a.response.responseText+')'))}
                        });
                    }
                },
                scope:this
            }]
        });

        this.GIROFileWin=new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.GenerateGIROFile"),
            closable:true,
//            layout: 'border',
//            border : false,
            iconCls : 'iconwin',
//            width:300,
            width: 455,
            autoHeight:true,
//            plain:true,
            modal:true,
            buttonAlign : 'right',
            items:this.GIROFileForm
        });
        this.GIROFileWin.on("show", function() {
            var arr=this.grid.getSelectionModel().getSelections();
            this.vendorRecDS.add(arr);
        },this)
        this.GIROFileWin.show();
    },
   
    getTransName:function(type){
       switch(type){
           case Wtf.autoNum.SalesOrder:return "Sales Order";
           case Wtf.autoNum.Invoice:return "Invoice";
           case Wtf.autoNum.PurchaseOrder:return "Purchase Order";
           case Wtf.autoNum.GoodsReceipt:return "Vendor Invoice";
           case Wtf.autoNum.BillingSalesOrder:return "Sales Order";
           case Wtf.autoNum.BillingInvoice:return "Invoice";
           case Wtf.autoNum.BillingPurchaseOrder:return "Purchase Order";
           case Wtf.autoNum.BillingGoodsReceipt:return "Vendor Invoice";
           case Wtf.autoNum.Quotation:return "Quotation";
       }
    },    
//    DownloadLink : function(a, b, c, d, e, f) {       
//            var msg = "";
//            var url = "ACCInvoiceCMN/getAttachDocuments.do";           
//            if (c.data['attachment']!=0)
//              msg ='('+c.data['attachment']+')'+'<div class = "pwnd downloadDoc" wtf:qtitle="'
//                + WtfGlobal.getLocaleText("acc.invoiceList.attachments")
//                + '" wtf:qtip="'
//                + WtfGlobal
//                .getLocaleText("acc.invoiceList.clickToDownloadAttachments")
//                + '" onclick="displayDocList(\''
//                + c.data['billid']
//                + '\',\''
//                + url
//                + '\',\''
//                + 'invoiceGridId'
//                + this.id
//                + '\', event,\''
//                + ""
//                + '\',\''
//                + ""
//                + '\',\''
//                + false
//                + '\',\''
//                + 0
//                + '\',\''
//                + 0
//                + '\',\''
//                + ""
//                + '\',\''
//                + this.grid.id     //ERP-13011 [SJ]
//                + '\')" style="width: 16px; height: 16px;cursor:pointer; margin-left: 16px; margin-top: -15px;" id=\''
//                + c.data['leaveid'] + '\'>&nbsp;</div>';
//            //else
//              //  msg = "";
//            return msg;
//        },
//    docuploadhandler : function(e, t) {
//            if (e.target.className != "pwndbar1 uploadDoc")
//                return;
//            var selected = this.sm.getSelections();           
//            if (this.grid.flag == 0) {
//                this.fileuploadwin = new Wtf.form.FormPanel(
//                {                  
//                    url : "ACCInvoiceCMN/attachDocuments.do",
//                    waitMsgTarget : true,
//                    fileUpload : true,
//                    method : 'POST',
//                    border : false,
//                    scope : this,
//                    // layout:'fit',
//                    bodyStyle : 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
//                    lableWidth : 50,
//                    items : [
//                    this.sendInvoiceId = new Wtf.form.Hidden(
//                    {
//                        name : 'invoiceid'
//                    }),
//                    this.tName = new Wtf.form.TextField(
//                    {
//                        fieldLabel : WtfGlobal.getLocaleText("acc.invoiceList.filePath") + '*',
//                        //allowBlank : false,
//                        name : 'file',
//                        inputType : 'file',
//                        width : 200,
//                        //emptyText:"Select file to upload..",
//                        blankText:WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
//                        allowBlank:false,
//                        msgTarget :'qtip'
//                    }) ]
//                });
//
//                this.upwin = new Wtf.Window(
//                {
//                    id : 'upfilewin',
//                    title : WtfGlobal
//                    .getLocaleText("acc.invoiceList.uploadfile"),
//                    closable : true,
//                    width : 450,
//                    height : 120,
//                    plain : true,
//                    iconCls : 'iconwin',
//                    resizable : false,
//                    layout : 'fit',
//                    scope : this,
//                    listeners : {
//                        scope : this,
//
//                        close : function() {
//                            thisclk = 1;
//                                scope: this;
//                            this.fileuploadwin.destroy();
//                            this.grid.flag = 0
////                              this.upwin.close();
//                        }
//                    },
//                    items : this.fileuploadwin,
//                    buttons : [
//                    {
//                        anchor : '90%',
//                        id : 'save',
//                        text : WtfGlobal
//                        .getLocaleText("acc.invoiceList.bt.upload"),
//                        scope : this,
//                        handler : this.upfileHandler
//                    },
//                    {
//                        anchor : '90%',
//                        id : 'close',
//                        text : WtfGlobal
//                        .getLocaleText("acc.invoiceList.bt.cancel"),
//                        handler : this.close1,
//                        scope : this
//                    } ]
//
//                });
//                this.sendInvoiceId.setValue(selected[0].get('billid'));
//                this.upwin.show();
//                this.grid.flag = 1;
//            }
//        },
//         close1 : function() {
//            Wtf.getCmp('upfilewin').close();
//            this.grid.flag = 0;
//        },
//
//        upfileHandler : function() {
//            if (this.fileuploadwin.form.isValid()) {
//                Wtf.getCmp('save').disabled = true;
//            }
//            //var selected = this.sm.getSelections();
//           // if (selected[0].get('doccnt') < 3) {
//                if (this.fileuploadwin.form.isValid()) {
//                    this.fileuploadwin.form.submit({
//                        scope : this,
//                        failure : function(frm, action) {
//                            this.upwin.close();
//                            //this.genSaveSuccessResponse(eval('('+action.response.responseText+')'));
//                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), "File not uploaded! File should not be empty.");
//                        },
//                        success : function(frm, action) {
//                            this.upwin.close();
//                            this.Store.reload();
//                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), "File uploaded successfully.");
//                        }
//                    })
//                }
////            } else {
////                msgBoxShow(
////                    [
////                    WtfGlobal
////                    .getLocaleText("el.msg.head.info"),
////                    WtfGlobal
////                    .getLocaleText("el.msg.threemax") ],
////                    1, 1);
////                this.upwin.close();
////            }
//        },
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
       
    },
    filterStore: function(json, filterConjuctionCriteria) {
        /**
         * ERP-33751 - Start Date Required for saved Search
         */        
        this.objsearchComponent.advGrid.sdate = this.startDate.getValue(); 
        this.objsearchComponent.advGrid.edate = this.endDate.getValue();
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: (this.isSalesCommissionStmt != undefined && this.isSalesCommissionStmt ==true ) ?  Wtf.Acc_Invoice_ModuleId:this.moduleid,
            isFixedAsset:this.isFixedAsset,
            isLeaseFixedAsset:this.isLeaseFixedAsset,
            isJobWorkOrderReciever:this.isJobWorkOrderReciever,
            filterConjuctionCriteria: filterConjuctionCriteria,
            pendingapproval: this.pendingapproval,
            isDraft: this.isDraft,
            isConsignment:this.isConsignment,
            report:true
        }
        this.Store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit:  this.pP.combo!=undefined?this.pP.combo.value:30}});
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: (this.isSalesCommissionStmt != undefined && this.isSalesCommissionStmt ==true ) ?  Wtf.Acc_Invoice_ModuleId:this.moduleid,
            isFixedAsset:this.isFixedAsset,
            isLeaseFixedAsset:this.isLeaseFixedAsset,
            isJobWorkOrderReciever:this.isJobWorkOrderReciever,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            pendingapproval: this.pendingapproval,
            isDraft: this.isDraft,
            isConsignment:this.isConsignment,
            report:true
        }
        this.Store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit:  this.pP.combo!=undefined?this.pP.combo.value:30}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },
       
globalInvoiceListGridAutoRefreshPublishHandler: function(response) {
    var res = eval("("+response.data+")");       
    if (res.success && ( Wtf.isAutoRefershReportonDocumentSave || (Wtf.userSessionId != undefined && Wtf.userSessionId==res.userSessionId ))) {
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
        this.Store.load({
            params : {
                start:0,
                limit: this.pP.combo!=undefined?this.pP.combo.value:30,
                pagingFlag:true
                           
            }
        });
        if (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId) {               
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.acquiredInvoicesReportHasBeenRefreshedAsSomeRecordsHasBeenModified"));
        } else if(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId){
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.VI&CPReportRefreshedmsg"));
        }else if(this.moduleid ==Wtf.Acc_Invoice_ModuleId){
            if(this.isLeaseFixedAsset)
                Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.LeaseInvoiceListRefreshedmsg"));
            else
                Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.CI&CSReportRefreshedmsg"));
        } else if(this.moduleid ==Wtf.Acc_Sales_Order_ModuleId){
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.SOReportRefreshedmsg"));
        } else if(this.moduleid ==Wtf.Acc_Purchase_Order_ModuleId){
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.POReportRefreshedmsg"));
        } else if(this.moduleid ==Wtf.Acc_Customer_Quotation_ModuleId){
            if(this.isLeaseFixedAsset)
                Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.LeaseQuotationReportRefreshedmsg"));
            else
                Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.CQReportRefreshedmsg"));
        } else if(this.moduleid ==Wtf.Acc_Vendor_Quotation_ModuleId){
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.VQReportRefreshedmsg"));
        } else if(this.moduleid ==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId){
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.FixedAssetDisposalInvoiceList"));
        } else if(this.moduleid ==Wtf.Acc_Lease_Order){
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.LeaseOrderReportRefreshedmsg"));
        } else if (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.assetOrderReportRefreshedmsg"));
        }
    }
},

    showApprovalAttachWindow: function() {
        this.approvalfileuploadwin = new Wtf.form.FormPanel({
            url: "ACCPurchaseOrderCMN/attachApprovalDocuments.do",
            waitMsgTarget: true,
            fileUpload: true,
            method: 'POST',
            border: false,
            scope: this,
            bodyStyle: 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
            lableWidth: 50,
            items: [
                this.sendApprovalInvoiceId = new Wtf.form.Hidden({
                    name : 'invoiceid'
                }),
                this.approvaltName = new Wtf.form.TextField({
                    fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.filePath") + '*',
                    name: 'file',
                    inputType: 'file',
                    width: 200,
                    blankText: WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                    allowBlank: false,
                    msgTarget: 'qtip'
                })
            ]
        });

        this.approvalupwin = new Wtf.Window({
            id: 'approvalupfilewin',
            title: WtfGlobal.getLocaleText("acc.invoiceList.uploadfile"),
            closable: true,
            width: 450,
            height: 120,
            plain: true,
            iconCls: 'iconwin',
            resizable: false,
            layout: 'fit',
            scope: this,
            modal: true,
            listeners: {
                scope: this,
                close : function() {
                    scope: this;
                    this.approvalfileuploadwin.destroy();
                }
            },
            items: this.approvalfileuploadwin,
            buttons: [
                {
                    anchor : '90%',
                    id : 'approvalsave',
                    text : WtfGlobal.getLocaleText("acc.invoiceList.bt.upload"),
                    scope : this,
                    handler : this.approvalUpfileHandler
                }, {
                    anchor: '90%',
                    id: 'approvalclose',
                    text: WtfGlobal.getLocaleText("acc.invoiceList.bt.cancel"),
                    handler: this.approvalclose,
                    scope: this
                }
            ]
        });
        this.approvalupwin.show();
    },
   
    approvalclose : function() {
        Wtf.getCmp('approvalupfilewin').close();
    },
    bulkReceivePayment:function(){
    var selectArray = eval(this.getSelectedRecords());
        /*
     * Atleast one invoice need to select from invoice report
     */
    
    if(selectArray.length==0){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promtmsg")], 2);
        return;
    }
    var cnt=0;
    var isCutomerSame=true;
    var isAmountDue=true;
    var isDeleted=false;
    
    
    /*
     * Amount due must be greater than zero
     */
    
    /*------ Checking whether selected invoices of same vendor/Customer------*/
    while(cnt< selectArray.length){
        if(selectArray[0].personid!=selectArray[cnt].personid){
           isCutomerSame=false;
            break;
        }
        cnt++;
    }
    
    cnt=0;
     /*------ Checking whether selected invoices of Amount Due is > or <0------*/
        while (cnt < selectArray.length) {
            if (selectArray[cnt].amountdue <= 0) {
                isAmountDue = false;
                break;
            }
            cnt++;
        }
        
         cnt=0;
     /*------ Checking whether selected invoices of Amount Due is > or <0------*/
        while (cnt < selectArray.length) {
            if (selectArray[cnt].deleted =="true") {
                isDeleted = true;
                break;
            }
            cnt++;
        }
    
    
    /*---- Vendor/Customer is not same & amount due<=0 -------*/
        if (!isCutomerSame && !isAmountDue) {

            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promptforAmountDue")], 2);

            return;
            /* If Vendor/Customer not same & Amount Due <=0 */
        } else  if (isCutomerSame && !isAmountDue) {

            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promptforAmountDue")], 2);

            return;
        } else  if (isDeleted) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promptfordeleted")], 2);

            return;
        }

/*-------- If Vendor/Customer different & Amount Due>0---------*/
if(!isCutomerSame && isAmountDue){
    this.callBulkPaymentforDifferentCustomerOrVendor(selectArray);
}else if(isCutomerSame && isAmountDue){//If Vendor/Customer Same & Amount Due > 0
    var winid=this.moduleid==Wtf.Acc_Invoice_ModuleId?"receiptwindow":"paymentwindow";
    var modeName = this.moduleid==Wtf.Acc_Invoice_ModuleId?"autoreceipt":"autopayment";
    var panel = Wtf.getCmp(winid);
    if(this.moduleid==Wtf.Acc_Invoice_ModuleId){
        if(panel==null){
            panel=new Wtf.account.ReceiptEntry({
                id : winid,
                paymentType: 1,
                border : false,
                isReceipt:this.moduleid==Wtf.Acc_Invoice_ModuleId?true:false,
                isDirectCustomer:false,
                invObj:this,
                isBulkPayment:true,
                moduleId:this.moduleid==Wtf.Acc_Invoice_ModuleId?Wtf.Acc_Receive_Payment_ModuleId:Wtf.Acc_Make_Payment_ModuleId,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 9, //This is help mode id
                title:this.moduleid==Wtf.Acc_Invoice_ModuleId?Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoRP"),Wtf.TAB_TITLE_LENGTH):Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoMP"),Wtf.TAB_TITLE_LENGTH),
                tabTip:this.moduleid==Wtf.Acc_Invoice_ModuleId?WtfGlobal.getLocaleText("acc.accPref.autoRP"):WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Receive Payments',
                iconCls:'accountingbase receivepayment',
                closable: true,
                isCustomer:true,
                modeName:modeName
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }else{         
        if(panel==null){
            panel=new Wtf.account.PaymentEntry({
                id : winid,
                paymentType: 1,
                border : false,
                isReceipt:false,
                invObj:this,
                isBulkPayment:true,
                isDirectCustomer:false,
                moduleId:Wtf.Acc_Make_Payment_ModuleId,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 10, //This is help mode id
                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoMP"),Wtf.TAB_TITLE_LENGTH),
                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Receive Payments',
                iconCls:'accountingbase receivepayment',
                closable: true,
                isCustomer:false,
                modeName:modeName
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
    }
},
    postTDSTCS: function(gstrtype) {
        var selectArray = eval(this.getSelectedRecords());
        var amountdue=0;
        var customerid="";
        var invoiceid="";
        var invoiceno="";
        /*
         * Atleast one invoice need to select from invoice report
         */
        if (selectArray.length == 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promtmsg")], 2);
            return;
        }
        var cnt = 0;
        var isCutomerSame = true;
        var isAmountDue = true;
        /*
         * Amount due must be greater than zero
         */
        /*------ Checking whether selected invoices of same vendor/Customer------*/
        while (cnt < selectArray.length) {
            if (selectArray[0].personid != selectArray[cnt].personid) {
                isCutomerSame = false;
                break;
            }
            customerid=selectArray[cnt].personid;
            invoiceid+=selectArray[cnt].billid+",";
            invoiceno+=selectArray[cnt].billno+",";
            cnt++;
        }
        cnt = 0;
        /*------ Checking whether selected invoices of Amount Due is > or <0------*/
        while (cnt < selectArray.length) {
            if (selectArray[cnt].amountdue <= 0) {
                isAmountDue = false;
                break;
            }
            amountdue=amountdue+parseFloat(selectArray[cnt].amountdueinbase);
            cnt++;
        }
        /*---- Vendor/Customer is not same & amount due<=0 -------*/
        if (!isCutomerSame) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promptforcustomer")], 2);
            return;
            /* If Vendor/Customer not same & Amount Due <=0 */
        } else if ( !isAmountDue) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promptforAmountDue")], 2);
            return;
        }
        var adjustedaccount="";
        var reason="";
        if(gstrtype==Wtf.GSTJEType.TDS){
            adjustedaccount = Wtf.account.companyAccountPref.columnPref.tdsAccount;
            reason="TDS JE";
        } else if(gstrtype==Wtf.GSTJEType.TCS){
            reason="TCS JE";
            adjustedaccount= Wtf.account.companyAccountPref.columnPref.tcsAccount;
        }
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.TDSTCS.jeconfirm"), function(btn) {
            if (btn == "yes") {
                Wtf.Ajax.requestEx({
                    url: "ACCJournalCMN/saveManualJournalEntries.do",
                    params: {
                        customerid: customerid,
                        amount: amountdue,
                        adjustedaccount: adjustedaccount,
                        invoiceid: invoiceid,
                        reason: reason,
                        invoiceno: invoiceno,
                        invoiceArray:  JSON.stringify(selectArray)
                    }
                },
                this,
                        function(res) {
                            if (res) {
                                Wtf.MessageBox.show({
                                    title: WtfGlobal.getLocaleText("acc.common.success"),
                                    msg: res.msg,
                                    width: 450,
                                    scope: {
                                        scopeObj: this
                                    },
                                    fn: function(btn, text, option) {
                                        this.scopeObj.refreshReportGrid();
                                    },
                                    buttons: Wtf.MessageBox.OK,
                                    animEl: 'mb9',
                                    icon: Wtf.MessageBox.INFO
                                });
                            }
                        },
                        function(res) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer")], 2)
                        });
            } else {
                return;
            }
        }, this);

    },
    /**
     * Function to post ITC JE against Reversal ITC Invoices.
     */
    postITCJE : function (){
        var selectArray = eval(this.getSelectedRecords());
                /*
         * Atleast one invoice need to select from invoice report
         */
        if (selectArray.length == 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promtmsg")], 2);
            return;
        }
        var cnt = 0;
        var invoiceid="";
        var invoiceno = "Being Input Tax Credit Reversed for this Purchase Invoice: ";
        var itcjeconfig = {};
        while (cnt < selectArray.length) {
            invoiceid += selectArray[cnt].billid + ",";
            invoiceno += selectArray[cnt].billno + ",";
            cnt++;
        }
        if (invoiceno != "" && cnt > 0) {
            invoiceno = invoiceno.substring(0, invoiceno.length - 1);
        }
        if (invoiceid != "" && cnt > 0) {
            invoiceid = invoiceid.substring(0, invoiceid.length - 1);
        }
        itcjeconfig.itctransactionids=invoiceid;
        itcjeconfig.gstrType=Wtf.GSTJEType.ITC;
        itcjeconfig.invoiceno=invoiceno;
        this.validateAndOpenJEForm(itcjeconfig)
        
    },
    /**
     * Open JE form
     */
    validateAndOpenJEForm: function(itcjeconfig){
            Wtf.Ajax.requestEx({
            url: "ACCGoodsReceiptCMN/isAllITCReversal.do",
            params:{
                documentids:itcjeconfig.itctransactionids,
                itctype:Wtf.GSTITCTYPEID.ITCREVERSAL
            }
        }, this, function(response) {
            if (response.success) {
                if (response.isvalidselection) {
                    callJournalEntryTab(undefined, undefined, undefined, 1, undefined, undefined, undefined, undefined, true, itcjeconfig);
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promtmsgsforitc")], 2);
            return;
                }
            }
        });   
    },
/*---Open transaction Form while diffrent Vendor/Customer get selected from grid------- */
callBulkPaymentforDifferentCustomerOrVendor :function(recArray){
  
      
     
    var winid=this.moduleid==Wtf.Acc_Invoice_ModuleId?"receiptwindow":"paymentwindow";
    var modeName = this.moduleid==Wtf.Acc_Invoice_ModuleId?"autoreceipt":"autopayment";
    var panel = Wtf.getCmp(winid);
    if(this.moduleid==Wtf.Acc_Invoice_ModuleId){
        if(panel==null){
            panel=new Wtf.ReceiptEntry({
                id : winid,
                paymentType: 1,
                border : false,
                isReceipt:this.moduleid==Wtf.Acc_Invoice_ModuleId?true:false,
                isDirectCustomer:false,
                invObj:this,
                isBulkPayment:true,
                moduleId:this.moduleid==Wtf.Acc_Invoice_ModuleId?Wtf.Acc_Receive_Payment_ModuleId:Wtf.Acc_Make_Payment_ModuleId,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 9, //This is help mode id
                title:this.moduleid==Wtf.Acc_Invoice_ModuleId?Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoRP"),Wtf.TAB_TITLE_LENGTH):Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoMP"),Wtf.TAB_TITLE_LENGTH),
                tabTip:this.moduleid==Wtf.Acc_Invoice_ModuleId?WtfGlobal.getLocaleText("acc.accPref.autoRP"):WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Receive Payments',
                iconCls:'accountingbase receivepayment',
                closable: true,
                isCustomer:true,
                modeName:modeName,
                recArray:recArray
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }else{         
        if(panel==null){
            panel=new Wtf.PaymentEntry({
                id : winid,
                paymentType: 1,
                border : false,
                isReceipt:false,
                invObj:this,
                isBulkPayment:true,
                isDirectCustomer:false,
                moduleId:Wtf.Acc_Make_Payment_ModuleId,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 10, //This is help mode id
                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoMP"),Wtf.TAB_TITLE_LENGTH),
                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Receive Payments',
                iconCls:'accountingbase receivepayment',
                closable: true,
                isCustomer:false,
                modeName:modeName,
                recArray:recArray
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
     
 },



getSelectedRecords: function() {
    var arr = [];
    var selectionArray=this.grid.getSelectionModel().getSelections();
    for( var i=0;i<selectionArray.length;i++){
        /*
         *system is considering \n for next line in customers/vendor address so problem is occurring while getting eval their address
         */
        if(selectionArray[i].data.billingAddress!=undefined){
            selectionArray[i].data.billingAddress="";
        }
        if(selectionArray[i].data.memo!=undefined){
            selectionArray[i].data.memo="";
        }
        if(selectionArray[i].data.shippingAddress!=undefined){
            selectionArray[i].data.shippingAddress="";
        }
        if(selectionArray[i].data.manufacturerAddress!=undefined){
            selectionArray[i].data.manufacturerAddress="";
        }
        if(selectionArray[i].data.getFullShippingAddress!=undefined){
            selectionArray[i].data.getFullShippingAddress="";
        }
        if(selectionArray[i].data.vendcustShippingAddress!=undefined){
            selectionArray[i].data.vendcustShippingAddress="";
        }
        arr.push(this.Store.indexOf(selectionArray[i]));
    }
    var jarray = WtfGlobal.getJSONArrayWithoutEncodingNew(this.grid, true, arr);
    return jarray;
},
    approvalUpfileHandler : function() {
        if (this.approvalfileuploadwin.form.isValid()) {
            Wtf.getCmp('approvalsave').disabled = true;
        }
        if (this.approvalfileuploadwin.form.isValid()) {
            this.approvalfileuploadwin.form.submit({
                scope: this,
                failure: function(frm, action) {
                    this.approvalupwin.close();
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), "File not uploaded! File should not be empty.");
                },
                success: function(frm, action) {
                    if (eval('('+action.response.responseText+')').data != undefined) {
                        this.docID = eval('('+action.response.responseText+')').data.docID;
                    }
                   
                    this.approvalupwin.close();
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), eval('('+action.response.responseText+')').data.msg);
                }
            })
        }
    },
    syncCustomerfromLMS: function () {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosyncInvoicesfromLMS"), //"Are you sure you want to sync products from Inventory?",          
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function (btn) {
                if (btn == "ok") {

                    WtfGlobal.setAjaxTimeOut();
                    var param = {
                        deleted: this.deleted,
                        nondeleted: this.nondeleted
                    }
                    Wtf.Ajax.requestEx({
                        url: "ACCRemote/getInvoiceFromLMS.do",
                        param: param
                    }, this, this.genSuccessResponseforSynch, this.genFailureResponseforSynch);
                }
            }
        });
    },
    genSuccessResponseforSynch: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.customerList.gridInvoice"), response.msg], response.success * 2 + 1);
            this.Store.reload();
        } else {
            if (response.isused = "true")
            {
                var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg]);
            }
            else
            {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.customerList.gridInvoice"), WtfGlobal.getLocaleText("acc.field.NoInvoiceareavailableforsyncing")], response.success * 2 + 1);
            }
        }
    },
    genFailureResponseforSynch: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg]);
    },
    
    handleUnlinkDocument: function() {
        var formrec = this.grid.getSelectionModel().getSelected();
        
        /*----Restricted dropship documents from unlinking----------- */
        if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId) {
            if (formrec.json.isdropshipchecked!=undefined && formrec.json.isdropshipchecked) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.cannotUnlinkDropShipDoc")],2);
                return false;
            }
        }
      if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid== Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId) {
            linkinfo(formrec.get('billid'), this.isOrder, this.businessPerson, formrec.get('billno'), "true", this.isFixedAsset, this.moduleid, true,this);
        } else {
            var url = this.getUnlinkDocURL(this.moduleid);
            Wtf.Ajax.requestEx({
                url: url,
                params: {
                    billid: formrec.get('billid')
                }
            }, this, this.unlinkDocSuccessResponse, this.unlinkDocFailureResponse);
        }
        
    },

disableSOorPO: function() {
    var formrec = this.grid.getSelectionModel().getSelected();
  
    var url=this.isCustomer?"ACCSalesOrder/saveSalesOrderStatusForPO.do":"ACCPurchaseOrder/savePurchaseOrderStatusForSO.do";
    Wtf.Ajax.requestEx({
        url: url,
        params: {
            billid: formrec.get('billid'),
            status:formrec.get('statusforcrosslinkage')
        }
    }, this,this.disableDocSuccessResponse, this.disableDocFailureResponse);
    
        
},
    linkAdvancePayment: function () {
        var formrec = this.grid.getSelectionModel().getSelected();

        this.showAdvancePaymentGrid({
            currency: formrec.data.currencyid,
            accid: formrec.data.personid,
            date: formrec.data.date,
            billid: formrec.data.billid,
            isCustomer: this.isCustomer
        });

    },
    showAdvancePaymentGrid: function (config) {
        Wtf.account.calllinkadvancepaymentWindow({moduleid: this.moduleId, isCustomer: this.isCustomer, billid: config.billid, currency: config.currency, accid: config.accid,
            billdate: WtfGlobal.convertToGenericDate(config.date)});
    },
closeDocument: function() {
    var formrec = this.grid.getSelectionModel().getSelected();
  
    var url=this.isCustomer?"ACCSalesOrder/closeDocument.do":"ACCPurchaseOrder/closeDocument.do";
    Wtf.Ajax.requestEx({
        url: url,
        params: {
            billid: formrec.get('billid')
        }
    }, this,this.disableDocSuccessResponse, this.disableDocFailureResponse);
    
        
},
    getUnlinkDocURL: function(moduleid) {
        var url = "";
        if (moduleid == Wtf.Acc_Invoice_ModuleId) {
            url = "ACCInvoiceCMN/unlinkInvoiceDocuments.do";
        } else if (moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
            url = "ACCGoodsReceiptCMN/unlinkPurchaseInvoiceDocuments.do";
        } else if (moduleid == Wtf.Acc_Vendor_Quotation_ModuleId) {
            url = "ACCPurchaseOrderCMN/unlinkVendorQuotationDocuments.do";
        } else if (moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
            url = "ACCSalesOrderCMN/unlinkCustomerQuotationDocuments.do";
        } else if (moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
            url = "ACCPurchaseOrderCMN/unlinkPurchaseOrderDocuments.do";
        } else if (moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            url = "ACCSalesOrderCMN/unlinkSalesOrderDocuments.do";
        }
        return url;
    },

    unlinkDocSuccessResponse: function(response) {
        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO,
                scope: this,
                fn: function(btn ,text, option) {
                    this.Store.reload();
                }
            });
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg], 2);
        }
    },

   disableDocSuccessResponse: function(response) {
    if (response.success) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.success"),
            msg: response.msg,
            buttons: Wtf.MessageBox.OK,
            icon: Wtf.MessageBox.INFO,
            scope: this,
            fn: function(btn ,text, option) {
                this.Store.reload();
            }
        });
    } else {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg], 2);
    }
},

    unlinkDocFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
     disableDocFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    showCustomerCustomFieldFlag: function(moduleid){
        var customerCustomFieldFlag = false;
        if(moduleid===Wtf.Acc_Invoice_ModuleId || moduleid===Wtf.Acc_Sales_Order_ModuleId || moduleid===Wtf.Acc_Customer_Quotation_ModuleId 
        || moduleid===Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || moduleid===Wtf.Acc_ConsignmentRequest_ModuleId || moduleid===Wtf.Acc_ConsignmentInvoice_ModuleId
        || moduleid===Wtf.Acc_Lease_Quotation || moduleid===Wtf.Acc_Lease_Order || moduleid===Wtf.LEASE_INVOICE_MODULEID){
            customerCustomFieldFlag = true;
        }
        return customerCustomFieldFlag;
    },
  
    showVendorCustomField: function(moduleid){
        var vendorCustomFieldFlag = false;
        if(moduleid===Wtf.Acc_Vendor_Invoice_ModuleId || moduleid===Wtf.Acc_Purchase_Order_ModuleId || moduleid===Wtf.Acc_Vendor_Quotation_ModuleId
            || moduleid===Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || moduleid===Wtf.Acc_FixedAssets_Purchase_Order_ModuleId 
            || moduleid===Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || moduleid===Wtf.Acc_ConsignmentVendorRequest_ModuleId
            || moduleid===Wtf.Acc_Consignment_GoodsReceipt_ModuleId || moduleid===Wtf.Acc_Purchase_Requisition_ModuleId
            || moduleid===Wtf.Acc_RFQ_ModuleId){
            vendorCustomFieldFlag = true;
        }
        return vendorCustomFieldFlag;
    },
    
    getImportExtraConfigForModule: function(moduleid) {
        var extraConfig = {};
        extraConfig.isExcludeXLS = true;
        
        if (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Cash_Sales_ModuleId) {
            extraConfig.url = "ACCImportTransaction/importInvoices.do";
            extraConfig.incash = moduleid == Wtf.Acc_Cash_Sales_ModuleId?true:false;
        } else if (moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            extraConfig.url = "ACCSalesOrder/importSalesOrders.do";
        } else if (moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
            extraConfig.url = "ACCImportTransaction/importPurchaseInvoices.do";
        } else if (moduleid == Wtf.Acc_Cash_Purchase_ModuleId) {
            extraConfig.url = "ACCImportTransaction/importPurchaseInvoices.do";
            extraConfig.incash = true;//flag for identifying cash purchase transaction
        } else if (moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
            extraConfig.url = "ACCSalesOrder/importCustomerQuotations.do";
        } else if (moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
            extraConfig.url = "ACCPurchaseOrder/importPurchaseOrders.do";
        } else if (moduleid == Wtf.Acc_Vendor_Quotation_ModuleId) {
            extraConfig.url = "ACCPurchaseOrder/importVendorQuotations.do";
        } else if (moduleid == Wtf.EXP_WITH_CASH_PURCHASE_INVOICE || moduleid == Wtf.EXP_WITHOUT_CASH_PURCHASE_INVOICE) {
            extraConfig.url = "ACCGoodsReceipt/importExpenceInvoices.do";
            extraConfig.isExpenseInvoiceImport = true;
            extraConfig.incash = true;
            /*
             * IF Expense PI without cash then send 'incash' parameter as 'false'
             */
            if (moduleid == Wtf.EXP_WITHOUT_CASH_PURCHASE_INVOICE) {
                extraConfig.incash = false;
            }
        }
        
        return extraConfig;
    },
    
    getImportExtraParamsForModule: function(moduleid) {
        var extraParams = "";
        
        if (moduleid == Wtf.Acc_Invoice_ModuleId) {
            extraParams = "{\"DepreciationAccont\":\"" + Wtf.account.companyAccountPref.depreciationaccount + "\",\"isDraft\":" + this.isDraft + "}";
        } else if (moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            extraParams = "";
        } else if (moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
            extraParams = "";
        } else if (moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
            extraParams = "";
        } else if (moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
            extraParams = "";
        }else if (moduleid == Wtf.Acc_Vendor_Quotation_ModuleId) {
            extraParams = "";
        }else if (moduleid == Wtf.Acc_Cash_Sales_ModuleId) {
            extraParams = "";
        }else if (moduleid == Wtf.Acc_Cash_Purchase_ModuleId) {
            extraParams = "";
        }
        
        return extraParams;
    },
    
    getModuleNameForImport: function(moduleid) {
        var moduleName = "";
        
        if (moduleid == Wtf.Acc_Invoice_ModuleId) {
            moduleName = "Customer Invoices";
        } else if (moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            moduleName = "Sales Order";
        } else if (moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
            moduleName = "Vendor Invoice";
        } else if (moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
            moduleName = "Quotation";
        } else if (moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
            moduleName = "Purchase Order";
        } else if (moduleid == Wtf.Acc_Vendor_Quotation_ModuleId) {
            moduleName = Wtf.Vendor_Quotation_List;
        } else if (moduleid == Wtf.Acc_Cash_Sales_ModuleId) {
            moduleName = Wtf.Cash_Sales_List;
        } else if (moduleid == Wtf.Acc_Cash_Purchase_ModuleId) {
            moduleName = Wtf.Cash_Purchase_List;
        }
        
        return moduleName;
    },
    
    handleViewDeliveryPlanner: function() {
        if (this.grid.getSelectionModel().hasSelection()) {
            var rec = this.grid.getSelectionModel().getSelected();
            getDeliveryPlannerTabView(this.moduleid, rec.data.billid)
        }
    },
    
    createImportButtonsForInvoice: function(importBtnArray, bottombtnArr) {
        var importBtnArr = [];
        // For Customer Invoice import button
        this.importInvoiceButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.ImportCustomerInvoice"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.ImportCustomerInvoice"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArray
        });
        importBtnArr.push(this.importInvoiceButton);

        // For Cash Invoice import button
        var extraConfigCashInvoice = this.getImportExtraConfigForModule(Wtf.Acc_Cash_Sales_ModuleId);
        var extraParamsCashInvoice = this.getImportExtraParamsForModule(Wtf.Acc_Cash_Sales_ModuleId);
        var importBtnArrayCashInvoice = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(Wtf.Acc_Cash_Sales_ModuleId), this.Store, extraParamsCashInvoice, extraConfigCashInvoice);

        this.importCashInvoiceButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.importCashSales"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.importCashSales"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayCashInvoice
        });
        importBtnArr.push(this.importCashInvoiceButton);

        // For Cash Invoice import button
        var extraConfigConvertInToCashInvoice = {
            url: "ACCInvoiceCMN/importConvertSalesInvoiceInToCashSales.do", 
            isExcludeXLS: true
        };
        var extraParamsConvertInToCashInvoice = "";
        var importBtnArrayConvertInToCashInvoice = Wtf.documentImportMenuArray(this, "Convert Sales Invoice in to Cash Sales", this.Store, extraParamsConvertInToCashInvoice, extraConfigConvertInToCashInvoice);

        this.importCashInvoiceButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.convertSalesInvoiceInToCashSales"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.convertSalesInvoiceInToCashSales"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayConvertInToCashInvoice
        });
        importBtnArr.push(this.importCashInvoiceButton);
        /**
         * Import Button for E-wat Fields (Eway bill no and Date)
         * ERP-39530
         */
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && (this.moduleId !=undefined && this.moduleId !='' && Wtf.EwayUnitDimCustmFieldsActivatedModules.indexOf(parseInt(this.moduleId))> -1)) {
            importBtnArr.push(this.createImportEwayFiledsButton());
        } 
        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu: importBtnArr
        });
        bottombtnArr.push(this.importBtn);
    },
    /**
     * Import Button for E-wat Fields (Eway bill no and Date)
     */
    createImportEwayFiledsButton: function () {
        var extraConfig = {};
        extraConfig.url = "AccEntityGST/importEwayFieldsData.do";
        extraConfig.isExcludeXLS = false;
        extraConfig.ImportModuleId = this.moduleId;
        var extraParams = {};
        extraParams.isCustomer = this.isCustomer;
        var importEwayBtnArray = Wtf.importMenuArray(this, Wtf.EWAY_BILL_IMPORT_MODULENAME, this.Store, extraParams, extraConfig);
        if (importEwayBtnArray.length == 2) {
            importEwayBtnArray.remove(importEwayBtnArray[0]);
        }
        this.importEwayBtn = new Wtf.Action({//
            text: WtfGlobal.getLocaleText("acc.import.document.eway.fieldsdata.txt"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.import.document.eway.fieldsdata.txt"),
            iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import'),
            menu: importEwayBtnArray
        });
        return this.importEwayBtn;
    },
    //Create import buttons for Vendor Invoice and Cash Purchase
    createImportButtonsForVendorInvoice: function(importBtnArray, bottombtnArr) {
        var importBtnArr = [];
        // For Vendor Invoice import button
        this.importInvoiceButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.ImportVendorInvoice"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.ImportVendorInvoice"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArray
        });
        importBtnArr.push(this.importInvoiceButton);

        // For Cash purchase import button
        //get extra config and params for import
        var extraConfigCashInvoice = this.getImportExtraConfigForModule(Wtf.Acc_Cash_Purchase_ModuleId);
        var extraParamsCashInvoice = this.getImportExtraParamsForModule(Wtf.Acc_Cash_Purchase_ModuleId);
        var importBtnArrayCashInvoice = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(Wtf.Acc_Cash_Purchase_ModuleId), this.Store, extraParamsCashInvoice, extraConfigCashInvoice);
        //create Cash Purchase import button
        this.importCashInvoiceButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.importCashPurchase"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.importCashPurchase"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayCashInvoice
        });
         importBtnArr.push(this.importCashInvoiceButton);
        
        // For Cash purchase with expense type import button
        //get extra config and params for import
        var extraConfigForExpInv = this.getImportExtraConfigForModule(Wtf.EXP_WITH_CASH_PURCHASE_INVOICE);
        var importBtnArrayForExpInv = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(Wtf.Acc_Cash_Purchase_ModuleId), this.Store, "", extraConfigForExpInv);
        
        //create Cash Purchase import button
        this.importExpInvButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("Import Expense Cash Purchase Invoice"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("Import Expense Cash Purchase Invoice"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayForExpInv
        });
        
        var extraConfigForExpInvWithoutCash = this.getImportExtraConfigForModule(Wtf.EXP_WITHOUT_CASH_PURCHASE_INVOICE);
        var importBtnArrayForExpInvWithoutCash = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(Wtf.Acc_Cash_Purchase_ModuleId), this.Store, "", extraConfigForExpInvWithoutCash);
        
        //create Cash Purchase import button
        this.importExpInvWithoutCashButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("Import Expense Purchase Invoice"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("Import Expense Purchase Invoice"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayForExpInvWithoutCash
        });
        
        importBtnArr.push(this.importExpInvWithoutCashButton);
        importBtnArr.push(this.importExpInvButton);
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            importBtnArr.push(this.createImportEwayFiledsButton());
        }
        //create main import button
        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu: importBtnArr
        });
        //put import button in bottom bar
        bottombtnArr.push(this.importBtn);
    },
    /*
     * Validation before generating the file
     */
    validateDataBeforeGeneratingIBGFile: function(){    
        if(this.customerBankAccountType.getValue()){
            if (this.grid.getSelectionModel().hasSelection()) {
                this.generateIBGFileForSelectedInvoices();
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uob.selectRecordForGeneratingIBG")],2);
                return;
            }
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uob.selectCustomerBankAccountTypeFirst")],2);
            return;
        }
    },
    callIBGHistoryReport : function(){
        callGiroFileGenerationHistoryReport();
    },
    /*
     * Generating the GIRO file for UOB bank
     */
    generateIBGFileForSelectedInvoices:function(){
      var arr = this.grid.getSelectionModel().getSelections();
      var arrayOfBillIds=[];
      var arrayOfBaseAmount=[];
      var arrayOfInvoiceNumbers=[];
      var invoicesNotEligible='';
      var totalAmount = 0;
      for(var i=0;i<arr.length;i++){
          if(arr[i].data.isGIROFileGeneratedForUOBBankForReport){
              invoicesNotEligible += arr[i].data.billno+", ";
          }
          arrayOfInvoiceNumbers.push(arr[i].data.billno);
          arrayOfBillIds.push(arr[i].data.billid);
          totalAmount += arr[i].data.amountdueinbase;       //SDP-7140
          arrayOfBaseAmount.push(arr[i].data.amountdueinbase);//SDP-7140
      }
      if(invoicesNotEligible != ''){
          invoicesNotEligible = invoicesNotEligible.substring(0, invoicesNotEligible.length-2);
          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uob.GIROAlreadyGenerated")+'</br>'+invoicesNotEligible],2);
          return;
      }
      if(totalAmount > Wtf.IBGBanks.MAXAmount.UOBBank){
          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uob.totalAmountLimitExceeding")+'</br>'+invoicesNotEligible],2);
          return;
      }
      var configObject={};
      configObject.arrayOfBillIds = arrayOfBillIds;
      configObject.arrayOfBaseAmount = arrayOfBaseAmount;
      configObject.arrayOfInvoiceNumbers = arrayOfInvoiceNumbers;
      configObject.invoiceListComponent = this;
      configObject.totalAmount = totalAmount;
      generateIBGFileForUOB(configObject);
    },
    
    revertGiroGeneratedStatus:function(){
        var arr = this.grid.getSelectionModel().getSelections();
        if(arr.length == 0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.5")],2);
            return;
        }
        var arrayOfBillIds = [];
        var invoicesNotEligible='';
        for(var i=0;i<arr.length;i++){
          if(!arr[i].data.isGIROFileGeneratedForUOBBankForReport){
              invoicesNotEligible += arr[i].data.billno+", ";
          }else {
              arrayOfBillIds.push(arr[i].data.billid);
          }
      }
      if(invoicesNotEligible != ''){
          invoicesNotEligible = invoicesNotEligible.substring(0, invoicesNotEligible.length-2);
          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uob.giroNotGeneratedYet")+'</br>'+invoicesNotEligible],2);
          return;
      }
      
       Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.uob.confirmRevertAction"), function(btn) {
        if (btn == 'yes') {
            Wtf.Ajax.requestEx({
                url: "ACCInvoiceCMN/revertFileGenerationStatus.do",
                params: {
                    arrayOfBillIds : arrayOfBillIds.toString()
                }
            }, this, this.successOnStatusRevert, this.genFailureResponse);
        }else if (btn == 'no') {
            return;
        }
    }, this);
    },
    successOnStatusRevert : function(response){
        if(response.success){
             Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,
                width:450,
                scope: {
                  scopeObj:this  
                },
                buttons: Wtf.MessageBox.OK,
                fn: function(btn ,text, option) {
                    this.scopeObj.Store.reload();
                },
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });  
            
        }
    }
});
//
//function displayDocList(id, url, gridid, event,creatorid,approverid,docReq,cnt,statusid,showleaves,reportGridId){   
//   if(Wtf.getCmp('DocListWindow'))
//        Wtf.getCmp("DocListWindow").destroy();
//                new Wtf.DocListWindow({
//                                wizard:false,
//                                closeAction : 'hide',
//                                layout: 'fit',
//                                title:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),
//                                shadow:false,
//                bodyStyle: "background-color: white",
//                                closable: true,
//                                width : 250,
//                                heigth:250,
//                                url: url,
//                gridid: gridid,
//                                modal:true,
//                                autoScroll:true,
//                                recid:id,
//                                delurl: "ACCInvoiceCMN/deleteDocument.do?docid=",
//                                id:"DocListWindow",
//                                docCount:cnt,
//                                isDocReq:docReq, 
//                                statusID:statusid, 
//                                showleaves:showleaves, 
//                                dispto:"pmtabpanel",
//                                reportGridId:reportGridId    //ERP-13011 [SJ]
//                            });
//
//   var docListWin = Wtf.getCmp("DocListWindow");
//   var leftoffset =event.pageX-200;
//
//   var topoffset = event.pageY+10;
//
//    if (document.all) {
//        xMousePos = window.event.x+document.body.scrollLeft;
//        yMousePos = window.event.y+document.body.scrollTop;
//        xMousePosMax = document.body.clientWidth+document.body.scrollLeft;
//        yMousePosMax = document.body.clientHeight+document.body.scrollTop;
//        leftoffset=xMousePos-200;//xMousePos;
//        topoffset=yMousePos+120;//yMousePos;
//       
//    }
//   if(docListWin.innerpanel==null||docListWin.hidden==true){
//       docListWin.setPosition(leftoffset, topoffset);
//
//       docListWin.show();
//   }else{
//       docListWin.hide();
//
//   }
//}
//function deletedocs(url, docid , gridid){
//   
//    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.invoiceList.deletedocumentmsg"), function (btn){
//        if(btn.toString()=="yes")
//        {
//            Wtf.Ajax.requestEx({
//                method:'POST',
//                url:url,
//                params:{
//                    dummy:1
//                }
//            },
//            this,
//            function(response){
//                var retstatus = response;
//                if(retstatus[0].success){
//                    Wtf.getCmp("DocListWindow").hide();
//                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), retstatus[0].msg);
//                     if(Wtf.getCmp(gridid) && Wtf.getCmp(gridid).getStore()) {            //ERP-13011 [SJ]
//                        Wtf.getCmp(gridid).getStore().reload();
//                    }   
//                }
//                else{
//                    var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";                   
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//                }                   
//            },
//            function(response){               
//                Wtf.MessageBox.hide();
//                var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";       
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//            }
//            );
//        }
//    }, this);
//}


Wtf.account.TransactionListPanelView=function(config){
    this.appendID = true;
    this.invID=null;
    this.exponly=null;
    this.recArr=[];
    this.productid = null;
    this.isCash=true;
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.costCenterId = "";
    this.extraFilters = config.extraFilters;
    this.storeGlobalCount = 0;
    if(config.extraFilters != undefined){
        this.costCenterId = config.extraFilters.costcenter?config.extraFilters.costcenter:"";
    }
   
    this.label = config.label;
    this.isOrder=config.isOrder;
    this.isCustBill=config.isCustBill;
    this.isCash=config.cash?true:false;
    this.isexpenseinv=false;
    this.nondeleted=false;
    this.deleted=false;
   
    this.isQuotation = config.isQuotation;
    if(this.isQuotation == undefined || this.isQuotation == null){
        this.isQuotation = false;
    }
    
    this.GridRec = Wtf.data.Record.create ([
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
        {name: 'parentinvoiceid'},
        {name: 'parentso'}
       
    ]);
    this.StoreUrl = this.businessPerson=="Customer" ? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
   
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
        baseParams:{
            costCenterId: this.costCenterId,
            deleted:false,
            nondeleted:false,
            cashonly:false,
            creditonly:false,
            CashAndInvoice:true,
            salesPersonFilterFlag:true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
 
    if(this.extraFilters != undefined){
        var currentBaseParams = this.Store.baseParams;
        this.Store.baseParams=currentBaseParams;
    }
    var cashType=config.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoCS"):WtfGlobal.getLocaleText("acc.accPref.autoCP");    //"Cash Sales":"Cash Purchase";
    var creditType=config.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoInvoice"):WtfGlobal.getLocaleText("acc.accPref.autoVI");   //"Invoice":"Vendor Invoice";
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[[0,WtfGlobal.getLocaleText("acc.rem.105")],[1,cashType],[2,creditType],[3,WtfGlobal.getLocaleText("acc.rem.106")],[4,WtfGlobal.getLocaleText("acc.rem.107")]]
    });
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid, //+config.id,
        valueField:'typeid',
        mode: 'local',
        defaultValue:0,
        width:80,
        listWidth:200,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
   
    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:150,
        listWidth:150,
        displayField:'name',
        valueField:'id',
        triggerAction: 'all',
        mode: 'local',
        typeAhead:true,
        value: this.costCenterId,
        selectOnFocus:true,
        forceSelection: true
        });
    this.costCenter.on("focus", function(cmb, rec, ind){
        chkEmptyCmb(cmb.store.getCount(),'1');
    });
    this.costCenter.on("select", function(cmb, rec, ind){
        this.costCenterId = rec.data.id;
       
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.costCenterId = this.costCenterId;
        this.Store.baseParams=currentBaseParams;
       
    },this);
   
   
   
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect : true
    });
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        id:"gridmsg"+config.helpmodeid+config.id,
        border:false,
        layout:'fit',
        trackMouseOver: true,
        viewConfig:{forceFit:true,emptyText:WtfGlobal.getLocaleText("acc.field.Norecordstodisplay.")},
        forceFit:true,
        columns:[new Wtf.grid.RowNumberer(),{
            hidden:true,
            dataIndex:'billid',
            hideable: false    //ERP-5269[SJ] 
        },{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.cn.9"),
            dataIndex:'billno'
           
        },{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
            dataIndex:'date',
            align:'center',
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.ProductName"),
            dataIndex:'rowproductname'
        },{
            header:WtfGlobal.getLocaleText("acc.product.gridQty"),
            dataIndex:'rowquantity',
            align:'center',
            renderer:function(value){
                return parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            }
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),
            dataIndex:'rowrate',
            align:'right',
            renderer:WtfGlobal.currencyRendererSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),
            dataIndex:'rowprdiscount',
            align:'right',
            renderer : function(v) {
                return v + "%"
            }
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.Tax"),
            dataIndex:'rowprtaxpercent',
            align:'right',
            renderer : function(v) {
                return v + "%"
            }
        }]
    });
    this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
            this.grid.getView().refresh();
        },this);
    this.grid.flag = 0;

    Wtf.apply(this,{
        border:false,
        layout : "fit",
         bodyStyle : "background-color:#ffffff;padding-right:10px;",
        items:[this.grid]
    });
    this.Store.on('beforeload',function(s,o){
        if(!o.params)o.params={};
        o.params.cashonly=false;
        o.params.creditonly=this.creditonly;
        o.params.CashAndInvoice=(!this.creditonly)?true:false;
        o.params.salesPersonFilterFlag=true;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.deleted=false;
        currentBaseParams.nondeleted=true;
        currentBaseParams.cashonly= false;
        currentBaseParams.creditonly=this.creditonly;
        currentBaseParams.CashAndInvoice=(!this.creditonly)?true:false;       
        currentBaseParams.salesPersonFilterFlag=true;       
        currentBaseParams.archieve=this.archieveCombo?this.archieveCombo.getValue() : 0;
        currentBaseParams.costCenterId = this.costCenter.getValue();      
        this.Store.baseParams=currentBaseParams;
    },this);
//    this.Store.on('load',this.hideLoading, this);
//    this.Store.on('loadexception',this.hideLoading, this);
    Wtf.account.TransactionListPanelView.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
}
Wtf.extend(Wtf.account.TransactionListPanelView,Wtf.Panel,{
 
  hideLoading:function(){Wtf.MessageBox.hide();}
});



Wtf.account.TransactionListPanelViewSales=function(config){
//    this.businessPerson='Customer';
    Wtf.apply(this, config);
    this.isBlockQtyReport=config.isBlockQtyReport!=undefined?config.isBlockQtyReport:false;
   
    this.arr = [];
    this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'currencysymbol'},
        {name:'customerid'},
        {name:'personname'},
        {name:'personid'},
        {name:'sequenceformatid'},
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'lasteditedby'},
        {name:'deliveryTime'},
        {name:'shipdate', type:'date'},
        {name:'termid'},
        {name:'duedate', type:'date'},
        {name:'memo'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'taxamount'},
        {name:'includeprotax'},
        {name:'taxincluded'},
        {name:'taxid'},
        {name:'taxname'},
        {name:'partialinv'},
        {name:'salesPerson'},
        {name:'salespersonname'},
        {name:'agentname'},
        {name:'currencyid'},
        {name:'productid'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'amount'},
        {name:'customername'},
        {name:'incash', type:'boolean'},
        {name:'rowproductname'},
        {name:'rowproductid'},
        {name:'rowquantity'},
        {name:'rowbaseuomquantity'},
        {name:'lockquantity'},
        {name:'uomname'},
        {name:'rowrate'},
        {name:'amountinbasewithouttax'},
        {name:'purchasecost'},
        {name:'profitmargin'},
        {name:'percentmargin'},
        {name:'rowprdiscount'},
        {name:'rowprtaxpercent'},
        {name:'amountinbase'},
        {name:'isOpeningBalanceTransaction'},
        {name:'amountinbasewithtax'},
        {name:'rowproductdescription'},
        {name: 'parentinvoiceid'},
        {name: 'parentso'},
        {name: 'isLessMargin', type: 'boolean'},
        {name: 'currencycode'},
        {name: 'billtoaddress'},
        {name: 'shiptoaddress'},
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
        {name: 'shippingRecipientName'},
        {name: 'shippingContactPerson'},
        {name: 'shippingRoute'},
        {name: 'vendcustShippingAddress'},
        {name: 'vendcustShippingCountry'},
        {name: 'vendcustShippingState'},
        {name: 'vendcustShippingCounty'},
        {name: 'vendcustShippingCity'},
        {name: 'vendcustShippingEmail'},
        {name: 'vendcustShippingFax'},
        {name: 'vendcustShippingMobile'},
        {name: 'vendcustShippingPhone'},
        {name: 'vendcustShippingPostal'},
        {name: 'vendcustShippingContactPersonNumber'},
        {name: 'vendcustShippingContactPersonDesignation'},
        {name: 'vendcustShippingContactPerson'},
        {name: 'vendcustShippingRecipientName'},
        {name: 'vendcustShippingAddressType'},
        /**
         * If Show Vendor Address in purchase side document and India country 
         * then this Fields used to store Vendor Billing Address
         */
        {name: 'vendorbillingAddressTypeForINDIA'},
        {name: 'vendorbillingAddressForINDIA'},
        {name: 'vendorbillingCountryForINDIA'},
        {name: 'vendorbillingStateForINDIA'},
        {name: 'vendorbillingPostalForINDIA'},
        {name: 'vendorbillingEmailForINDIA'},
        {name: 'vendorbillingFaxForINDIA'},
        {name: 'vendorbillingMobileForINDIA'},
        {name: 'vendorbillingPhoneForINDIA'},
        {name: 'vendorbillingContactPersonForINDIA'},
        {name: 'vendorbillingRecipientNameForINDIA'},
        {name: 'vendorbillingContactPersonNumberForINDIA'},
        {name: 'vendorbillingContactPersonDesignationForINDIA'},
        {name: 'vendorbillingWebsiteForINDIA'},
        {name: 'vendorbillingCountyForINDIA'},
        {name: 'vendorbillingCityForINDIA'},
        {name:'shippingWebsite'},
        {name:'billingWebsite'},
        {name:'vendcustShippingWebsite'},
        {name:'isReturnTransaction'},
        {name:'termamount'},
        {name:'isSupplierLinekd'},
        {name:'displayUOM'},
        {name:'externalcurrencyrate'},
        {name:'vendorname'},
        {name:'vendorunitcost'},
        {name:'vendorcurrexchangerate'},
        {name:'vendorcurrencyid'},
        {name:'vendorcurrencysymbol'},
        {name:'profitmarginpercent'},
        {name:'totalcost'}
    ]);
    if(this.isSOPOByProductReport){
        this.StoreUrl = this.businessPerson=="Customer" ? "ACCSalesOrderCMN/getSalesOrderByProduct.do" : "ACCPurchaseOrderCMN/getPurchaseOrderByProduct.do";
    }else{
        this.StoreUrl = (this.isOrder==true && this.businessPerson=="Customer")?"ACCSalesOrderCMN/getSalesByCustomerForSalesOrder.do":this.businessPerson=="Customer" ? "ACCInvoiceCMN/getSalesByCustomer.do" : "ACCGoodsReceiptCMN/getPurchaseByVendor.do";
    }
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
   
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
   
    this.productRec = Wtf.data.Record.create ([
        {name:'productid'},
        {name:'pid'},
        {name:'type'},
        {name:'productname'},
        {name:'desc'},
        {name: 'producttype'}
    ]);
   
    this.productStore = new Wtf.data.Store({
//        url:Wtf.req.account+'CompanyManager.jsp',
//        url:"ACCProduct/getProductsForCombo.do",
        url:"ACCProductCMN/getProductsForDropdownOptimised.do",
        baseParams:{mode:22,termSalesOrPurchaseCheck:this.isCustomer, onlyProduct: true},
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
        },this.productRec)
    });
   
    this.ProductComboconfig = {
            hiddenName:"productid",        
            store: this.productStore,
            valueField:'productid',
            hideLabel:true,
            hidden:(this.reportID == Wtf.autoNum.PurchaseByVendorReport)?false:this.iscustreport && !this.isSalesPersonName,
            displayField:'productname',
            emptyText:WtfGlobal.getLocaleText("acc.msgbox.17"),
            mode: 'remote',
            typeAhead: true,
            selectOnFocus:true,
            isProductCombo: true,//for Product search Start with or Anywhere Match
            triggerAction:'all',
            scope:this
        };
    /*
     * Added paging to product combo.
     */
    this.productname = new Wtf.common.SelectPaging(Wtf.applyIf({
         multiSelect:true,
         fieldLabel:WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
         forceSelection:true,
         extraFields:['pid','type'],
         extraComparisionField:'pid',// type ahead search on product id as well.
         listWidth:Wtf.ProductComboListWidth,
         width:240,
         pageSize: Wtf.ProductCombopageSize
    },this.ProductComboconfig));
   
    this.productCategoryRec = Wtf.data.Record.create([
        {name: 'id'},
        {name: 'name'},
    ]);
    this.productCategoryStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams: {
            mode:112,
            groupid:19
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.productCategoryRec)
    });
    this.productCategory = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.19"),
        hiddenName: 'id',
        name: 'id',
        hidden: this.iscustreport,
        store: this.productCategoryStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectaproductcategory"),
        width:100,
        listWidth:150
    });
    if (!this.iscustreport) {
        this.productCategoryStore.load();
    }
    this.productCategoryStore.on("load", function() {
        var record = new Wtf.data.Record({
            id: "",
            name: "All Records"
        });
        this.productCategoryStore.insert(0, record);
        this.productCategory.setValue("");
    }, this);
           
     this.transactionSelectionStore=new Wtf.data.SimpleStore({ 
        fields:[{
            name:'id'
        },{
            name:'name'
        }],
        data: [
        ['0', 'Invoices Only'],
        ['1', 'Invoices with Sales Return and Credit Notes']
        ]
    }); 
    this.transactionSelection = new Wtf.form.ComboBox({
        fieldLabel: '',
        hiddenName: 'transactionSelection',
        name: 'transactionSelection',
        hidden: this.iscustreport || this.isBlockQtyReport || this.isSalesByProductReport,
        store: this.transactionSelectionStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        triggerAction: 'all',
        hideLabel: true,
        value:'0',
        width:200,
        listWidth:300
    });
    
     this.usersRec = new Wtf.data.Record.create([
        {name: 'id', mapping:'userid'},
        {name: 'name', mapping:'username'},
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
   
    if(this.isSalesPersonName||this.salesbycustSalesOrder){
        this.userds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },Wtf.salesPersonRec),
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
            groupid:15,
            onlyloggedinusersalespersons:true//this flag is used to to fetch only logged in users salesperson 
            }
        });
    }else{
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
    }
   
    this.SalesPersonComboconfig = {
            hiddenName:"userid",        
            store: this.userds,
            valueField:'id',
            hideLabel:true,
            hidden : (this.isSalesPersonName||this.salesbycustSalesOrder)?false:true,
            displayField:'name',
            emptyText:WtfGlobal.getLocaleText("acc.field.Pleaseselectasalesperson"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };

    this.salesPersonName = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         id:'salesbysalesperson'+this.id,
         fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.salesbyperson") + '*' ,
         forceSelection:true,        
         width:240
    },this.SalesPersonComboconfig));
   
    this.personRec = new Wtf.data.Record.create ([
        {
            name:'accid'
        },{
            name:'accname'
        },{
            name:'acccode'
        },{
            name: 'termdays'
        },{
            name: 'billto'
        },{
            name: 'currencysymbol'
        },{
            name: 'currencyname'
        },{
            name: 'currencyid'
        },{
            name:'deleted'
        }
    ]);

    this.customerAccStore =  new Wtf.data.Store({
        url:this.isCustomer? "ACCCustomer/getCustomersForCombo.do": "ACCVendor/getVendorsForCombo.do",
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
        },this.personRec)
    });
       
    this.CustomerComboconfig = {
        hiddenName:this.businessPerson.toLowerCase(),        
        store: this.customerAccStore,
        valueField:'accid',
        hideLabel:true,
        hidden : this.isSalesPersonName ? this.isSalesPersonName : !this.iscustreport,
        displayField:'accname',
        emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };

    this.Name = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         fieldLabel:this.isCustomer ?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven")+ '*' ,
         forceSelection:true,   
         extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
         extraComparisionField:'acccode',// type ahead search on acccode as well.
         listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
         width:240
    },this.CustomerComboconfig));
       
    this.customerAccStore.on("load", function(store) {
        if(this.isCustomer){
            var record = new this.personRec({
                accid: "All",
                accname: "All Customers",
                acccode:""
            });
            this.Name.store.insert( 0,record);
            this.Name.setValue("All");
            this.loaddata();
        }else{
            var record = new this.personRec({
                accid: "All",
                accname: "All Vendors"
            });
            this.Name.store.insert( 0,record);
            this.Name.setValue("All");
            this.loaddata();   
        }
    }, this);

     this.Name.on('select',function(combo,personRec){
        if(personRec.get('accid')=='All'){
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){
            combo.clearValue();
            combo.setValue(personRec.get('accid'));
        }
    } , this);
       
    this.customerCategoryRec = Wtf.data.Record.create([
        {name: 'id'},
        {name: 'name'},
    ]);
    this.customerCategoryStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams: {
            mode:112,
            groupid:7
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.customerCategoryRec)
    });
    this.customerCategory = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.7"),
        hiddenName: 'id',
        name: 'id',
        hidden: !this.isSalesPersonName,
        store: this.customerCategoryStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectacustomercategory"),
        width:80,
        listWidth:150
    });
    if (this.isSalesPersonName) {
        this.customerCategoryStore.load();
    }
    this.customerCategoryStore.on("load", function() {
        var record = new Wtf.data.Record({
            id: "All",
            name: "All Records"
        });
        this.customerCategoryStore.insert(0, record);
        this.customerCategory.setValue("All");
    }, this);
   this.isProductStoreLoaded=false;
    this.productStore.on("load", function(store) {
        WtfGlobal.resetAjaxTimeOut();
        var record = new this.productRec({
            productid: "All",
            pid: "",
            type: "",
            productname: "All Products"
        });
         if(this.isBlockQtyReport){
             this.isProductStoreLoaded=true;
             this.productname.setValue(this.ProductId);
         }
         //this.loaddata();
    }, this);
   
    this.productStore.on("loadexception", function(store) {
        WtfGlobal.resetAjaxTimeOut();
    },this);

     this.productname.on('select',function(combo,productRec){
        if(productRec.get('productid')=='All'){
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){
            combo.clearValue();
            combo.setValue(productRec.get('productid'));
        }
    } , this);
        
    this.userds.on("load", function(store) {
        var record = new this.usersRec({
            id: "All",
            name: "All Persons"
        });
        if (this.isSalesPersonName||this.salesbycustSalesOrder) {
            var salespersonids = "";
            for (var i = 0; i < store.getCount(); i++) {
                if (store.data.items[i].data.userid != undefined) {
                    if (loginid === store.data.items[i].data.userid) {
                        salespersonids += store.data.items[i].data.id + ',';
                    }
                }

            }
            if (salespersonids != "") {
                salespersonids = salespersonids.substring(0, salespersonids.length - 1);
            }
        }
      
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.viewall) || salespersonids=="" ) {
           //user is admin or has view all permission or if user has no salesperson mapped then in that case show all salespersons in combo
            this.salesPersonName.store.insert(0, record);
            this.salesPersonName.setValue("All");
        } else {
            this.salesPersonName.setValue(salespersonids);
        }
    }, this);

     this.salesPersonName.on('select',function(combo,usersRec){
        if(usersRec.get('id')=='All'){
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){
            combo.clearValue();
            combo.setValue(usersRec.get('id'));
        }
    } , this);

    if(this.isSalesPersonName){
        this.userds.load();
        this.productStore.load();
    } else if(this.iscustreport) {
        this.customerAccStore.load();
        if(this.reportID == Wtf.autoNum.PurchaseByVendorReport){
            WtfGlobal.setAjaxTimeOut();
            this.productStore.load();
        }
    } else {
        WtfGlobal.setAjaxTimeOut();
        this.productStore.load();
    }
   if(this.salesbycustSalesOrder){
       this.userds.load(); 
   }
   /**
    * Block for Block qty Report i.e. applying group store
    */
      if(this.isBlockQtyReport){
         this.Store = new Wtf.data.GroupingStore({
        url:this.StoreUrl,
        sortInfo : {
            field : 'rowproductid',
            direction : 'ASC'
        },
        groupField : 'rowproductid',
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });    
   }else{
      this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
       
   }
   var quickSearchEmptyText = "";
   if(this.isSOPOByProductReport){
       quickSearchEmptyText=WtfGlobal.getLocaleText("acc.field.QuickSearchBySOPONumberCustomerProductDesc");
   }else{
       quickSearchEmptyText=this.isOrder?(this.isSalesByProductReport?WtfGlobal.getLocaleText("acc.field.SeacrhBySalesOrderProductProductDesc"):WtfGlobal.getLocaleText("acc.field.SeacrhBySalesOrderProduct")):(WtfGlobal.getLocaleText("acc.field.QuickSearchbyInvoice") + (this.iscustreport? WtfGlobal.getLocaleText("acc.field.ProductName") +","+  WtfGlobal.getLocaleText("acc.product.gridProductID"): this.isSalesPersonName ? WtfGlobal.getLocaleText("acc.masterConfig.15 ") :WtfGlobal.getLocaleText("acc.up.3")));
   }
   
   
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: quickSearchEmptyText,
        width: 150,
        field: 'billno',
        Store:this.Store
    });
   
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid, //+config.id,
        valueField:'typeid',
        mode: 'local',
        defaultValue:0,
        width:160,
        listWidth:200,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    
    this.marginPercentage = new Wtf.form.NumberField({
        name: 'marginPercentage',
        allowNegative: false,
        minvalue: 0,
        width: 50
    });
    this.marginPercentage.on('change', function(comp, newValue, oldValue) {
        if (this.isSalesByProductReport) {
            if (this.marginPercentage.getValue() > 100) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Highlight Margin below % value cannot be greater than 100."],2);
                this.marginPercentage.setValue(oldValue);
            }
        }
    }, this);

    this.Store.on('datachanged', function() {
//                    var p = this.pP.combo.value;
                    var p = (this.pP!= undefined && this.pP.combo != undefined) ? this.pP.combo.value : 15;
                    this.quickPanelSearch.setPage(p);
        }, this);
       
    this.Store.on('beforeload', function(){
        WtfGlobal.setAjaxTimeOut();
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.prodfiltercustid = this.iscustreport ? (this.isSalesPersonName ? '-1' : this.Name.getValue()) : '-1';//this.Name.getValue(),
            currentBaseParams.productid = this.iscustreport && !this.isSalesPersonName ? '-1' : this.productname.getValue();
        currentBaseParams.productCategoryid = this.iscustreport ? '-1' : this.productCategory.getValue();
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
           currentBaseParams.formtypeid = this.FormType != undefined ? this.FormType.getValue(): 0;
           currentBaseParams.vatcommodityid = this.vatCommodityCombo != undefined ? this.vatCommodityCombo.getValue() : "all";
           currentBaseParams.checkformstatus = this.FormStatus != undefined ? this.FormStatus.getValue() : 0;
        }
        currentBaseParams.salesPersonid = this.iscustreport ? (this.isSalesPersonName ||this.salesbycustSalesOrder? this.salesPersonName.getValue() : '-1') : '-1';
        currentBaseParams.customerCategoryid = this.iscustreport ? (this.isSalesPersonName ? this.customerCategory.getValue() : '-1') : '-1';
        currentBaseParams.ss = this.quickPanelSearch.getValue();
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());   //For UI Report  //ERP-8487
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());         //For UI Report  //ERP-8487
        currentBaseParams.userMarginPercentage = this.marginPercentage.getValue();
        if(this.isSalesByProductReport && this.moduleid == Wtf.Acc_Invoice_ModuleId){
            currentBaseParams.transactionSelection = this.transactionSelection.getValue();
            currentBaseParams.isSalesByProductReport = this.isSalesByProductReport;
        }  
        if (this.isBlockQtyReport != undefined && this.isBlockQtyReport == true) {
            currentBaseParams.isBlockQtyReport = this.isBlockQtyReport;
            currentBaseParams.productid = this.isProductStoreLoaded ? this.productname.getValue() : this.ProductId;
            if (this.salesOrderTypeCombo != undefined) {
                currentBaseParams.salesordertypeid = this.salesOrderTypeCombo.getValue();
            }
        }
        if(this.reportID == Wtf.autoNum.PurchaseByVendorReport){
            currentBaseParams.productids = this.productname.getValue();
            this.exportButton.setParams({
                productids :this.productname.getValue(),
                enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),         
                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                prodfiltercustid:this.Name.getValue()=="All"?"":this.Name.getValue()
            });
        }
        this.Store.baseParams = currentBaseParams;
        
        currentBaseParams.isSalesBysalesPerosnReport = this.isSalesPersonName;
    }, this);
    this.Store.on('load', function() {
        WtfGlobal.resetAjaxTimeOut();
        if(this.Store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
        }else if(this.Store.getCount()>0){
            if(this.exportButton)this.exportButton.enable();
        }
    }, this);
    this.Store.on('loadexception', function() {
        WtfGlobal.resetAjaxTimeOut();
    }, this);
    /**
     *  for Block qty report
     */
    if (this.isBlockQtyReport) {
        this.summary = new Wtf.grid.GroupSummary({});
    } else {
        this.summary = new Wtf.ux.grid.GridSummary();
    }
    
    
    var viewConfig;
    if(this.isBlockQtyReport){
            viewConfig= new Wtf.grid.GroupingView({
        forceFit:false,
        showGroupName: false,
        enableNoGroups:true, // REQUIRED!
        hideGroupedColumn: true,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });
    }else if (this.isSalesByProductReport) {
        viewConfig = {
            forceFit: false,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
            getRowClass: function(record) {
                    this.isLessMargin = record.get('isLessMargin');
                    if (this.isLessMargin) {
                        return 'occurrenceNo_N'
                    }else {
                        return 'occurrenceNo_1'
                    }
                }
        }
    }else{
        viewConfig = {
            forceFit: false,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
    }
    var isSalesByProductReport=this.isSalesByProductReport;
    var isBlockQtyReport=this.isBlockQtyReport;
    this.remortSort = false;
    if(this.reportID == Wtf.autoNum.PurchaseByVendorReport){
        this.remortSort = true
    }
    
    this.gridColumnModelArray=[];
    this.gridColumnModelArray.push(new Wtf.grid.RowNumberer(),{
        hidden:true,
        header:"",
        dataIndex:'billid'
    },{
        header:this.isCustomer?WtfGlobal.getLocaleText("acc.cust.name"):WtfGlobal.getLocaleText("acc.ven.name"),
//            hidden : this.iscustreport,
        pdfwidth:80,
        dataIndex:'customername',
        renderer : this.isSOPOByProductReport ? "" :WtfGlobal.linkDeletedRenderer,
        sortable : this.remortSort,
        summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header:this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.salesPerson"):WtfGlobal.getLocaleText("acc.field.AgentSalesman"),
        pdfwidth:80,
        dataIndex:this.isCustomer?'salespersonname':'agentname',
        sortable : this.remortSort
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),
//            hidden : !this.iscustreport,
        pdfwidth:80,
        sortable : this.remortSort,
        dataIndex:'rowproductname'
//            summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header:WtfGlobal.getLocaleText("acc.productList.gridProductID"),
        pdfwidth:80,
        sortable : this.remortSort,
        dataIndex:'rowproductid'
    },{
        header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),
//            hidden : !this.iscustreport,
        pdfwidth:80,
        sortable : this.remortSort,
        dataIndex:'rowproductdescription'
//            summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header:this.isSalesByProductReport?this.isBlockQtyReport?WtfGlobal.getLocaleText("acc.MailWin.somsg7"):WtfGlobal.getLocaleText("acc.InvoiceList.InvoiceSalesReturnNo"):this.label+" "+WtfGlobal.getLocaleText("acc.cn.9"),
        pdfwidth:80,
        dataIndex:'billno',
        sortable : this.remortSort,
//            renderer : WtfGlobal.linkDeletedRenderer,
        renderer:function(v,m,rec){
            return isSalesByProductReport?isBlockQtyReport?WtfGlobal.linkDeletedRenderer(v,m,rec):v:WtfGlobal.linkDeletedRenderer(v,m,rec);
        }

    },{
        header:this.label+" "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
        dataIndex:'date',
        align:'center',
        pdfwidth:80,
        sortable : this.remortSort,
        renderer:WtfGlobal.onlyDateDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.saleByItem.gridQty"),
        dataIndex:'rowquantity',
        width : 50,
        pdfwidth:50,
        sortable : this.remortSort,
        align:'center',
        renderer:function(value){
            return parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },{
            header: WtfGlobal.getLocaleText("acc.productList.gridLockQuantity"), //"Lock Quantity
            dataIndex: 'lockquantity',
            align: 'right',
            width: 80,
            hidden: !this.isBlockQtyReport,
            renderer:this.unitRenderer,
            pdfwidth: 50,
            summaryType: 'sum',
            sortable : this.remortSort,
//                summaryRenderer:WtfGlobal.quantityInsummaryRenderer
            summaryRenderer: function(v,val,record) {
                return "<b> " + parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
            }
        }, {
        header:WtfGlobal.getLocaleText("acc.uomgrid.baseuomqty"),
        dataIndex:'rowbaseuomquantity',
        width : 50,
        sortable : this.remortSort,
        pdfwidth:50,
        align:'center',
        renderer:function(value){
            return parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },{
        header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
        dataIndex:'currencycode',
        hidden:true,
        hideable:false,
        sortable : this.remortSort,
        pdfwidth:85
    },{
        header:WtfGlobal.getLocaleText("acc.rem.188.Mixed"),
        dataIndex:'rowrate',
        width : 100,
        align:'right',
        pdfwidth:100,
        pdfrenderer : "unitpricecurrency",
        sortable : this.remortSort,
//            hidden: this.isSalesByProductReport,
        renderer:WtfGlobal.withCurrencyUnitPriceRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.rem.193.Mixed"),
        dataIndex:'amount',
        align:'right',
        pdfwidth:100,
        sortable : this.remortSort,
        pdfrenderer : "rowcurrency",
        renderer : WtfGlobal.currencyRendererSymbol

    });
        
    if(this.reportID != Wtf.autoNum.PurchaseByVendorReport){
        this.gridColumnModelArray.push({
            header:(this.isCustomer? WtfGlobal.getLocaleText("acc.field.TotalSellingPriceInBase") : WtfGlobal.getLocaleText("acc.field.TotalPurchasePriceInBase"))+ " ("+WtfGlobal.getCurrencyName()+")",
            dataIndex:'amountinbasewithouttax',
            align:'right',
            pdfwidth:100,
            hidden: !this.isSOPOByProductReport,
            pdfrenderer : "amountinbasewithouttax",
            sortable : this.remortSort,
            renderer : WtfGlobal.currencyDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.TotalPurchaseCostInBase")+ " ("+WtfGlobal.getCurrencyName()+")",
            dataIndex:'purchasecost',
            width : 100,
            align:'right',
            pdfwidth:100,
            pdfrenderer : "purchasecost",
            hidden: !(this.isSOPOByProductReport && this.isCustomer),
            sortable : this.remortSort,
            renderer:WtfGlobal.currencyDeletedRenderer
        });
    }

    this.gridColumnModelArray.push({
        header:WtfGlobal.getLocaleText("acc.field.Margin")+" ("+WtfGlobal.getCurrencyName()+")",
        dataIndex:'profitmargin',
        width : 100,
        align:'right',
        pdfwidth:100,
        hidecurrency : true,
        sortable : this.remortSort,
        pdfrenderer : "profitmargin",
        hidden: !((this.isSOPOByProductReport && this.isCustomer) || (this.isSalesByProductReport && this.moduleid == Wtf.Acc_Invoice_ModuleId)),
        renderer:WtfGlobal.currencyDeletedRenderer
    },
    {
        header:WtfGlobal.getLocaleText("acc.saleByItem.gridPercentageProfitMargin"),  //"Percentage Profit Margin",
        dataIndex:'percentmargin',
        align:'right',
        sortable : this.remortSort,
        hidden: !((this.isSalesByProductReport && this.moduleid == Wtf.Acc_Invoice_ModuleId)),
        pdfwidth:100,
        renderer : function(v){return parseFloat(v).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)}
    },
    {
        header:WtfGlobal.getLocaleText("acc.field.AmountWithoutTaxinBaseCurrency")+ " ("+WtfGlobal.getCurrencyName()+")",
        dataIndex:'amountinbase',
        align:'right',
        summaryType:'sum',
        pdfwidth:100,
        hidecurrency : true,
        hidden: this.isSOPOByProductReport,
        renderer : WtfGlobal.currencyDeletedRenderer,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    },{
        header:(this.isSOPOByProductReport?(this.isCustomer? WtfGlobal.getLocaleText("acc.field.TotalSellingPricewithTaxInBase") : WtfGlobal.getLocaleText("acc.field.TotalPurchasePricewithTaxInBase")) : WtfGlobal.getLocaleText("acc.field.AmountWithTaxinBaseCurrency"))+ " ("+WtfGlobal.getCurrencyName()+")",
        dataIndex:'amountinbasewithtax',
        align:'right',
        summaryType:'sum',
        hidecurrency : true,
        pdfwidth:80,
        renderer : WtfGlobal.currencyDeletedRenderer,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    });
    
    if (Wtf.account.companyAccountPref.activateProfitMargin && this.moduleid == Wtf.Acc_Sales_Order_ModuleId && this.isSOPOByProductReport) {
        this.gridColumnModelArray.push({
            header: WtfGlobal.getLocaleText("acc.invoiceList.ven"), //"Vendor",
            width: 100,
            pdfwidth: 75,
            dataIndex: 'vendorname'
        }, {
            header: WtfGlobal.getLocaleText("acc.field.UnitCost"), // "Unit Cost",
            dataIndex: "vendorunitcost",
            align: 'right',
            renderer:WtfGlobal.withVendorCurrencyUnitCostRenderer,
            pdfwidth: 75,
            width: 100
        }, {
            header: WtfGlobal.getLocaleText("acc.field.VendorCurrencyExchangeRate"), // "Vendor Currency Exchange Rate",
            dataIndex: "vendorcurrexchangerate",
            align: 'right',
            pdfwidth: 75,
            width: 100,
            renderer:WtfGlobal.exchangeRateRenderer
        }, {
            header: WtfGlobal.getLocaleText("acc.field.TotalCostInBase"), //"Total Cost In Base",
            dataIndex: "totalcost",
            renderer : WtfGlobal.currencyDeletedRenderer,
            align: 'right',
            pdfwidth: 75,
            width: 100,
        }, {
            header: WtfGlobal.getLocaleText("acc.field.ProfitMargin(%)"), //"Profit Margin(%)",
            dataIndex: "profitmarginpercent",
            align: 'right',
            pdfwidth: 75,
            width: 100
        });
    }
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        loadMask : true,
        id:"gridmsg"+config.id,
        border:false,
        plugins:[this.summary],
        layout:'fit',
        trackMouseOver: true,
        viewConfig: viewConfig,
        forceFit:!this.remortSort,
        columns:this.gridColumnModelArray
    });
   
    var colModelArray = GlobalColumnModelForReports[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray, this.Store);
    if(this.moduleid != Wtf.Acc_Purchase_Requisition_ModuleId && this.moduleid != Wtf.Acc_RFQ_ModuleId){
       this.getMyConfig();
    }
    WtfGlobal.getGridConfig(this,this.moduleid,false,true);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        hidden: this.isSOPOByProductReport || this.isProductView,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
   
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.isOrder?Wtf.Acc_Sales_Order_ModuleId:this.businessPerson=="Customer"? Wtf.Acc_Invoice_ModuleId:Wtf.Acc_Vendor_Invoice_ModuleId,
        advSearch: false,
        ignoreDefaultFields:true,
        isAddressFieldSearch:this.isAddressFieldSearch  //Flag used to enable advance search on address fields
    });
   
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
           
    this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
            this.grid.getView().refresh();
            this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo != undefined) ? this.pP.combo.value : 15,
                pagingFlag: true

            }
        });
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveGridStateHandler, this);
        }, this);
    },this);
    this.grid.on('cellclick',this.onCellClick, this);
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClickNew,this);
    
    if (this.isSOPOByProductReport && this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            hidden: this.isSummary || this.isCustomWidgetReport,
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        });
    }
   var filename=this.isSOPOByProductReport?(this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SOByProductReport")+"_v1" : WtfGlobal.getLocaleText("acc.field.POByProductReport")+"_v1"):this.isSalesPersonName?WtfGlobal.getLocaleText("acc.field.SalesBySalesPerson")+"_v1":(this.isOrder?WtfGlobal.getLocaleText("acc.field.SalesByCustomerSO")+"_v1":this.isCustomer ? this.isSalesByProductReport ? WtfGlobal.getLocaleText("acc.field.SalesByProduct")+"_v1" : WtfGlobal.getLocaleText("acc.field.SalesByCustomer")+"_v1":WtfGlobal.getLocaleText("acc.field.PurchaseByVendor")+"_v1");
   if(this.isBlockQtyReport && this.isBlockQtyReport==true){
       filename=WtfGlobal.getLocaleText("acc.field.blockqtyReport")+"_v1";
   }
    this.exportButton=new Wtf.exportButton({
            obj:this,
            filename:filename,
            hidden : this.isProductView,
            id:"exportReports"+this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
//            disabled :true,
            scope : this,
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:this.isSOPOByProductReport?(this.isCustomer?300:301):(this.isOrder?776:this.isCustomer?152:245)
    });
   
    this.printButton=new Wtf.exportButton({
                    obj:this,
                    hidden : this.isProductView,
                    text:WtfGlobal.getLocaleText("acc.common.print"),
                    tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
                    label:WtfGlobal.getLocaleText("acc.field.SalesBy") +(this.iscustreport? (this.isSalesPersonName ? WtfGlobal.getLocaleText("acc.masterConfig.15"):WtfGlobal.getLocaleText("acc.up.3")) : WtfGlobal.getLocaleText("acc.product.gridProduct")),
                    menuItem:{print:true},
                    get:this.isSOPOByProductReport?(this.isCustomer?300:301):(this.isOrder?776:this.isCustomer?152:245),
                    params:{
                             name: this.isSOPOByProductReport?(this.isCustomer?"Sales Order By Product": "Purchase Order By Product") : ("Sales By " +(this.iscustreport? (this.isSalesPersonName ? 'sales person':'Customer') : 'Product')) + " Report"
                    }
                });
    /*
     * this.salesOrderTypeCombo - Used in Sales Order Report By Block Quantity
     * */
    this.salesOrderTypeStore = new Wtf.data.SimpleStore({
        fields: [{name: 'salesordertypeid'}, {name: 'name'}],
        data: [['0', WtfGlobal.getLocaleText("acc.field.salesorderbyblockquantity.salesorder.type.normalso")],
            ['3', WtfGlobal.getLocaleText("acc.field.salesorderbyblockquantity.salesorder.type.consignmentso")]]
    });
    this.salesOrderTypeCombo = new Wtf.form.ComboBox({
        store: this.salesOrderTypeStore,
        name: 'salesordertypeid',
        valueField: 'salesordertypeid',
        displayField: 'name',
        mode: 'local',
        value: '0',
        width: 150,
        listWidth: 200,
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });            
    this.btnArr = [];
    this.btnArr2 = [];
    this.bottomBtnArr = [];
    
    this.btnArr.push(this.quickPanelSearch, WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, '-',this.isSalesPersonName? "": WtfGlobal.getLocaleText("acc.field.Select") +" "+ (this.iscustreport? (this.isCustomer? WtfGlobal.getLocaleText("acc.up.3") : WtfGlobal.getLocaleText("acc.up.4")) : ""),  this.Name, (this.iscustreport? "" : WtfGlobal.getLocaleText("acc.masterConfig.19")), this.productCategory);
    
    if (this.isSalesByProductReport && !this.isBlockQtyReport) {
        this.btnArr.push('-', "<span wtf:qtip= '"+WtfGlobal.getLocaleText("acc.field.highlightMarginBelowPercentageToolTip")+"'>"+WtfGlobal.getLocaleText("acc.field.highlightMarginBelowPercentage")+"</span>", this.marginPercentage);
        this.btnArr.push('-', this.transactionSelection);
    }
    if (this.isBlockQtyReport) {
        this.btnArr.push(WtfGlobal.getLocaleText("acc.salesorderbyblockquantity.salesorder.type"), this.salesOrderTypeCombo);
    }
    this.btnArr.push((this.salesbycustSalesOrder || this.isSalesPersonName) ? WtfGlobal.getLocaleText("acc.field.Select") +" "+ WtfGlobal.getLocaleText("acc.masterConfig.15"): "", this.salesPersonName,(this.isSalesPersonName ? WtfGlobal.getLocaleText("acc.field.SelectProducts") : ""),this.productname,(this.iscustreport? (this.isSalesPersonName ? WtfGlobal.getLocaleText("acc.masterConfig.custCategory") : "") : ""), this.customerCategory);
    
    this.btnArr2.push({
        text: WtfGlobal.getLocaleText("acc.agedPay.fetch"),
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.loaddata
    }, this.AdvanceSearchBtn, '-', this.resetBttn);
    
    if (this.isSOPOByProductReport && this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
        this.btnArr2.push(this.customReportViewBtn);
    }
    this.bottomBtnArr.push('-', this.exportButton, '-', this.printButton);
    
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbar" + this.id,
        store: this.Store,
        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), // "No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id: "pPageSize_" + this.id
        }),
        items: this.bottomBtnArr
    });
               
    this.leadpan = new Wtf.Panel({
        border:false,
        layout : "border",
        items:[this.objsearchComponent,{
            region: 'center',
            layout: 'fit',
            border: false,
            tbar: this.btnArr,
            items: [{
                region:'center',
                layout: 'fit',
                border: false,
                tbar:this.btnArr2,
                items:[this.grid]
            }],
            bbar: this.pagingToolbar
        }]
    });
    
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        bodyStyle : "background-color:#ffffff;padding-right:10px;",
        items:[this.leadpan]
    },config);        
   
  
    Wtf.account.TransactionListPanelViewSales.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewSales,Wtf.Panel,{
 
  hideLoading:function(){
      Wtf.MessageBox.hide();
  },
    getGrid : function(){
        return this.grid;
    },
    unitRenderer: function(value, metadata, record) {
        if (record.data['type'] == "Service") {
            return "N/A";
        }
        var unit = record.data['uomname'];
        value = parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + " " + unit;
        if (record.data.deleted)
            value = '<del>' + value + '</del>';
        return value;
    },
  loaddata : function(){
        if (this.startDate.getValue() > this.endDate.getValue()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;
        }
      if(this.isSalesPersonName) {
            if(this.salesPersonName.getValue() == ''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseselectasalespersonfromdropdown")], 2);
                this.Store.removeAll();
                return;
            }
            if(this.customerCategory.getValue() == ''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseselectacustomercategoryfromdropdown")], 2);
                this.Store.removeAll();
                return;
            }
      }  
      else  if(this.salesbycustSalesOrder) {
           if(this.Name.getValue() == ''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseselectacustomerfromdropdown")], 2);
                this.Store.removeAll();
                return;
            }
      }else if(this.isSalesByProductReport){
           /**
            * Check for Product combobox entries valid or not
            */
           var isInvalidProductsSelected = WtfGlobal.isInvalidProductsSelected(this.productname);
           if(isInvalidProductsSelected){
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproduct")],3);
               return;
           }
      }else if(this.iscustreport) {
             if(this.Name.getValue() == ''){
                if(!this.isCustomer){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseselectavendorfromdropdown")], 2);
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseselectacustomerfromdropdown")], 2);
                }
                this.Store.removeAll();
                return;
            }
        } else if(!this.iscustreport) {
           
           
            //  Check valid Products Selected or Not
           
            var isInvalidProductsSelected = WtfGlobal.isInvalidProductsSelected(this.productname);
            if(isInvalidProductsSelected){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproduct")],3);
                return;
            }
           
           
            if(this.productname.getValue() == ''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseselectaproductfromdropdown")], 2);
                this.Store.removeAll();
                return;
        }
        }
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
        this.Store.load({
            params : {
                start:0,
                limit:(this.pP.combo!=undefined) ? this.pP.combo.value : 15,
                pagingFlag:true
                           
            }
        });
//        this.exportButton.enable();
    },
   
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
       
    },
    customizeView: function() {
        var modules='' + Wtf.Acc_Sales_Order_ModuleId;
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.autoNum.SO_By_ProductReport,
            modules: modules
        });
        this.customizeViewWin.show();
        var arr = this.arr;
    },
    appendGridColumn: function(reportId) {
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getAgedCustomFieldsToShow.do",
            params: {
                reportId: reportId
            }
        }, this, function(request, response) {
            var customProductField = request.data;
            this.updateStoreConfig(customProductField);
            this.cm = new Wtf.grid.ColumnModel(this.gridColumnModelArray);
            var config = this.cm.config.slice(0, this.initialColumnCnt);
            if (customProductField && customProductField.length > 0) {
                for (var ccnt = 0; ccnt < customProductField.length; ccnt++) {
                    config.push({
                        header: customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: 100,
                        pdfwidth: 50
                    })
                }
            }
            this.grid.getColumnModel().setConfig(config);
            var newcm = this.grid.getColumnModel();
            this.grid.reconfigure(this.Store, newcm);
            this.grid.getView().refresh(true);

        });
    },
    updateStoreConfig: function(customProductField) {
        for (var cnt = 0; cnt < customProductField.length; cnt++) {
            var fieldname = customProductField[cnt].dataindex;
            var newField = new Wtf.data.Field({
                name: fieldname
            });
            this.Store.fields.items.push(newField);
            this.Store.fields.map[fieldname] = newField;
            this.Store.fields.keys.push(fieldname);
            /**
             * Customized view requires updating field's length to reflect response data.
             */
            this.Store.fields.length++;
        }
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: (this.isOrder==true && this.isOrder!=undefined)?Wtf.Acc_Sales_Order_ModuleId:Wtf.Acc_Invoice_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
                }
            });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: (this.isOrder==true && this.isOrder!=undefined)?Wtf.Acc_Sales_Order_ModuleId:Wtf.Acc_Invoice_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
                }
            });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
   
    onCellClick:function(g,i,j,e){
        var formrec = this.grid.getStore().getAt(i);
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="billno"){
            var incash=formrec.get("incash");
            formrec.data.pendingapproval= this.pendingapproval;
            if(incash){
                callViewCashReceipt(formrec, 'ViewInvoice');
            }else if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId){
                callViewSalesOrder(true,formrec,formrec.data.billid, false);
            }else if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId){
                callViewPurchaseOrder(true,formrec,formrec.data.billid,false,this,undefined, true);
            }else{
                if(formrec.data.fixedAssetInvoice||formrec.data.fixedAssetLeaseInvoice){
                    callViewFixedAssetInvoice(formrec, formrec.data.billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
                } else if(formrec.data.isConsignment){
                    callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
                }else if(this.isOrder){
                     callViewSalesOrder(true,formrec,formrec.data.billid+"SalesOrder", false);
                } else{
                    if(this.isCustomer==false){
                        callViewGoodsReceipt(formrec, 'ViewGoodsReceipt');
                    }else{
                        if(!this.isSalesByProductReport){
                            callViewInvoice(formrec, 'ViewCashReceipt');
                        }
                    }
                }
            }
        } else if(header=="customername"){
            if(!this.isSOPOByProductReport)
                openAccountStatement(formrec.data.customerid, "true");
        }
    },
     handleResetClickNew:function(){

           this.quickPanelSearch.reset();
           this.startDate.setValue(WtfGlobal.getDates(true));
           this.endDate.setValue(WtfGlobal.getDates(false));
        if (this.isSalesPersonName) {
               this.salesPersonName.setValue(this.userds.getAt(0).data.id);
               this.customerCategory.setValue(this.customerCategoryStore.getAt(0).data.id);
               this.productname.setValue("");
        } else if (this.iscustreport) {
                this.Name.setValue(this.customerAccStore.getAt(0).data.accid);
            if (this.salesbycustSalesOrder) {
                this.salesPersonName.setValue(this.userds.getAt(0).data.id);
            }
        } else if (!this.iscustreport) {
                this.productname.setValue(this.productStore.getAt(0).data.productid);
            }

         this.Store.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value,
                    pagingFlag:true
                }
            });
      
    },
    getMyConfig : function(){
        WtfGlobal.getGridConfig (this.grid, Wtf.autoNum.PurchaseByVendorReport, false, false);
    },
    saveGridStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.autoNum.PurchaseByVendorReport, grid.gridConfigId, false);
    }
});
Wtf.account.salesOrPurchaseTypeWindow = function(config){
    this.value="1",
    this.butnArr = [];
  var  isJobWorkoutInvoice = (config.isJobWorkoutInvoice!=undefined&&config.isJobWorkoutInvoice!=null)?config.isJobWorkoutInvoice:false;
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler:function(){
            this.saveForm(isJobWorkoutInvoice)
        } 
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });

    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.salesOrPurchaseTypeWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.salesOrPurchaseTypeWindow, Wtf.Window, {

    onRender: function(config){
        Wtf.account.salesOrPurchaseTypeWindow.superclass.onRender.call(this, config);
        this.createForm();
        var title=this.isCustomer?WtfGlobal.getLocaleText("acc.field.SalesType"):WtfGlobal.getLocaleText("acc.field.PurchaseType");
        var msg=this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectSalesType"):WtfGlobal.getLocaleText("acc.field.SelectPurchaseType");
        var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.TypeForm
        });
    },
    
    createForm:function(){
        this.accountType= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:'1',
            checked:this.isCustomer?Wtf.account.companyAccountPref.salesTypeFlag==true:Wtf.account.companyAccountPref.purchaseTypeFlag==true,
            name:'rectype',
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoCS"):WtfGlobal.getLocaleText("acc.accPref.autoCP")
        });
        this.accountType1= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'2',
            checked:this.isCustomer?Wtf.account.companyAccountPref.salesTypeFlag==false:Wtf.account.companyAccountPref.purchaseTypeFlag==false,
            width: 50,
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.field.CreditSales"):WtfGlobal.getLocaleText("acc.field.CreditPurchase")
        });
        this.accountType2= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:'3',
            checked:this.isCustomer?Wtf.account.companyAccountPref.salesTypeFlag==true:Wtf.account.companyAccountPref.purchaseTypeFlag==true,
            name:'rectype',
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("Excise Sales"):WtfGlobal.getLocaleText("Excise Purchase")
        });
        var itermsArr = [];
        itermsArr.push(this.accountType);
        itermsArr.push(this.accountType1);
        /**
         * SDP-10564
         * Removed Excise Sales And Purchase Tab in form type select window
         */
//        if( Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isExciseApplicable ){
//            this.accountType2.checked=true;
//            itermsArr.push(this.accountType2);
//        }
        this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            autoHeight : true,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            items:itermsArr
        });
        this.accountType.setValue(true);
    },

    saveForm:function(isJobWorkoutInvoice){ 
        var rec=this.TypeForm.getForm().getValues();
        this.value = rec.rectype;
        isJobWorkoutInvoice=(isJobWorkoutInvoice!=undefined && isJobWorkoutInvoice!=null)?isJobWorkoutInvoice:false;
        if(this.value==1)
        {
            if(!this.isWithoutInventory)
            {
                if(this.isCustomer)
                {
                    callSalesReceipt(false,null);
                }
                if(!this.isCustomer)
                {
                    callPurchaseReceipt(false,null);
                } 
            }
//            else
//            {
//                if(this.isCustomer)
//                {
//                    callBillingSalesReceipt(false,null);
//                }
//                if(!this.isCustomer)
//                {
//                    callBillingPurchaseReceipt(false,null);
//                }
//            }
        } else if(this.value==3){
            if(this.isCustomer)
            {
                callCreateNewButtonFunction(2,null,null,null,null,null,true);
            }
            if(!this.isCustomer)
            {
                callCreateNewButtonFunction(6,null,null,null,null,null,true);
            }  
        } else{
            if(this.isCustomer)
            {
                callCreateNewButtonFunction(2);
            }
            if(!this.isCustomer)
            {
                /*isJobWorkoutInvoice is true if called from job workout navigation panel
               */
                if(isJobWorkoutInvoice!=undefined){
                    callCreateNewButtonFunction(6,null,null,null,null,null,null,null,isJobWorkoutInvoice);
                }else{
                    callCreateNewButtonFunction(6);
                }
            }
        }
        this.close();
    }
});

  function linkinfo(billid,isorder,buissenessPersion,billno,delpinfo,isFixedAsset,moduleid,unlinkflag,obj) {   //handler of link info button
      if(delpinfo&&obj==undefined){
        
        obj=this;   
        obj.moduleid=moduleid;
       }
    if(obj.sm !=undefined){
        obj.rec = obj.sm.getSelected();
    }
    var id = "";
    obj.billid = (obj.rec != undefined ? obj.rec.data.billid : billid);
    if (unlinkflag != undefined && unlinkflag) {
        obj.unlinkflag = unlinkflag;
        id = "linkinginfo_"+obj.billid;
    } else {
        id = "unlinkwin_"+obj.billid;
    }
    var panel = Wtf.getCmp(id);
    if(panel==undefined || panel==null){
        if(delpinfo=="true"){

            obj.isOrder=(isorder==="true"?true:false);
            obj.isCustBill=false;
            obj.businessPerson=buissenessPersion;
            if(obj.businessPerson === "Customer"){
                obj.isCustomer=true;
            }else{
                obj.isCustomer=false;
            }

         
        }

        
        if (moduleid !== undefined && moduleid == Wtf.Acc_Delivery_Order_ModuleId || moduleid==Wtf.Acc_Goods_Receipt_ModuleId ) {
            obj.isGRorDO = true;
            obj.moduleid = moduleid;
            obj.uPermType =(moduleid==Wtf.Acc_Goods_Receipt_ModuleId)?Wtf.UPerm.goodsreceiptreport: Wtf.UPerm.deliveryreport;
            obj.permType = (moduleid==Wtf.Acc_Goods_Receipt_ModuleId)?Wtf.Perm.goodsreceiptreport:Wtf.Perm.deliveryreport;
            obj.doid=obj.billid;
        } else if(moduleid !== undefined && (moduleid == Wtf.Acc_Sales_Order_ModuleId  || moduleid == Wtf.Acc_Purchase_Order_ModuleId)){
            obj.isOrder=true;
            obj.moduleid = moduleid;
            obj.uPermType =(moduleid ==  Wtf.Acc_Purchase_Order_ModuleId)? Wtf.UPerm.vendorinvoice:Wtf.UPerm.invoice;
            obj.permType =(moduleid ==  Wtf.Acc_Purchase_Order_ModuleId)? Wtf.Perm.vendorinvoice: Wtf.Perm.invoice;
        }else if(moduleid !== undefined && (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Vendor_Invoice_ModuleIdd )){
            obj.moduleid = moduleid;
            obj.uPermType =(moduleid ==  Wtf.Acc_Vendor_Invoice_ModuleId)?Wtf.UPerm.vendorinvoice:Wtf.UPerm.invoice;
            obj.permType =(moduleid ==  Wtf.Acc_Vendor_Invoice_ModuleId)?Wtf.Perm.vendorinvoice: Wtf.Perm.invoice;
        }else if(moduleid !== undefined && moduleid == Wtf.Acc_Vendor_Quotation_ModuleId || moduleid==Wtf.Acc_Customer_Quotation_ModuleId){
            obj.isQuotation=true;  
            obj.moduleid = moduleid;
            obj.uPermType =(moduleid == Wtf.Acc_Vendor_Quotation_ModuleId)? Wtf.UPerm.vendorinvoice:Wtf.UPerm.invoice;
            obj.permType =( moduleid == Wtf.Acc_Vendor_Quotation_ModuleId)? Wtf.Perm.vendorinvoice: Wtf.Perm.invoice;
        }else if(moduleid !== undefined && (moduleid == Wtf.Acc_Purchase_Return_ModuleId || moduleid == Wtf.Acc_Sales_Return_ModuleId)){
            obj.moduleid = moduleid;
            obj.uPermType =(moduleid==Wtf.Acc_Purchase_Return_ModuleId)?Wtf.UPerm.purchasereturn:Wtf.UPerm.salesreturn;
            obj.permType =(moduleid==Wtf.Acc_Purchase_Return_ModuleId)?Wtf.Perm.purchasereturn:Wtf.Perm.salesreturn;
        }
        
//        if(this.rec.data.incash){
//          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.cashtransaction")],2);
//          return;
//        }
        obj.linkRecord = Wtf.data.Record.create([
        {
           name: 'transactionNo'
        },{
            name: 'amount'
        },{
            name: 'withoutinventory'
        },{
            name: 'date',type: 'date'
        }, {
            name: 'mergedCategoryData'
        },{
            name: 'journalEntryNo'
        },{
            name: 'isexpenseinv'
        },{
            name: 'billid'
        },
        {name: 'journalentryid'},
        {name: 'entryno'},
        {name: 'isPaymentAlso'},
        {name: 'billto'},
        {name: 'discount'},
        {name: 'shipto'},
        {name: 'mode'},
        {name: 'billno'},
        {name: 'duedate', type: 'date'},
        {name: 'shipdate', type: 'date'},
        {name: 'personname'},
        {name: 'creditoraccount'},
        {name: 'personid'},
        {name: 'shipping'},
        {name: 'othercharges'},
        {name: 'taxid'},
        {name: 'discounttotal'},
        {name: 'isAppliedForTax'}, // in Malasian company if DO is applied for tax
        {name: 'discountispertotal', type: 'boolean'},
        {name: 'currencyid'},
        {name: 'currencysymbol'},
        {name: 'amount'},
        {name: 'amountinbase'},
        {name: 'amountdue'},
        {name: 'costcenterid'},
        {name: 'lasteditedby'},
        {name: 'costcenterName'},
        {name: 'memo'},
        {name: 'shipvia'},
        {name: 'status'},
        {name: 'statusID'},
        {name: 'fob'},
        {name: 'includeprotax', type: 'boolean'},
        {name: 'salesPerson'},
        {name: 'islockQuantityflag'},
        {name: 'agent'},
        {name:'landedInvoiceID'},
        {name:'landedInvoiceNumber'},
        {name: 'termdetails'},
        {name: 'gstIncluded'},
        {name: 'quotationtype'},
        {name: 'contract'},
        {name: 'termid'},
        {name: 'externalcurrencyrate'}, //    ERP-9886
        {name: 'customerporefno'},
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
        {name: 'shippingRecipientName'},
        {name: 'shippingContactPerson'},
        {name: 'shippingRoute'},
        {name: 'vendcustShippingAddress'},
        {name: 'vendcustShippingCountry'},
        {name: 'vendcustShippingState'},
        {name: 'vendcustShippingCounty'},
        {name: 'vendcustShippingCity'},
        {name: 'vendcustShippingEmail'},
        {name: 'vendcustShippingFax'},
        {name: 'vendcustShippingMobile'},
        {name: 'vendcustShippingPhone'},
        {name: 'vendcustShippingPostal'},
        {name: 'vendcustShippingContactPersonNumber'},
        {name: 'vendcustShippingContactPersonDesignation'},
        {name: 'vendcustShippingContactPerson'},
        {name: 'vendcustShippingRecipientName'},
        {name: 'vendcustShippingAddressType'},
        /**
         * If Show Vendor Address in purchase side document and India country 
         * then this Fields used to store Vendor Billing Address
         */
        {name: 'vendorbillingAddressTypeForINDIA'},
        {name: 'vendorbillingAddressForINDIA'},
        {name: 'vendorbillingCountryForINDIA'},
        {name: 'vendorbillingStateForINDIA'},
        {name: 'vendorbillingPostalForINDIA'},
        {name: 'vendorbillingEmailForINDIA'},
        {name: 'vendorbillingFaxForINDIA'},
        {name: 'vendorbillingMobileForINDIA'},
        {name: 'vendorbillingPhoneForINDIA'},
        {name: 'vendorbillingContactPersonForINDIA'},
        {name: 'vendorbillingRecipientNameForINDIA'},
        {name: 'vendorbillingContactPersonNumberForINDIA'},
        {name: 'vendorbillingContactPersonDesignationForINDIA'},
        {name: 'vendorbillingWebsiteForINDIA'},
        {name: 'vendorbillingCountyForINDIA'},
        {name: 'vendorbillingCityForINDIA'},
        {name:'sequenceformatid'},
        {name:'gstIncluded'},
        {name:'lasteditedby'},
        {name:'movementtype'},
        {name:'movementtypename'},
        {name:'salespersonname'},
        {name:'isConsignment'},
        {name:'custWarehouse'},
        {name:'requestWarehouse'},
        {name:'requestLocation'},
        {name:'autoapproveflag'},
        {name:'movementtype'},
        {name:'deliveryTime'},
        {name:'getFullShippingAddress'},
        {name:'selfBilledInvoice'},
        {name:'RMCDApprovalNo'},
        {name:'fixedAssetInvoice'},
        {name:'fixedAssetLeaseInvoice'},
        //Below Fields are used only for Cash Sales and Purchase.
        {name:'methodid'},
        {name:'paymentname'},
        {name:'detailtype'},
        {name:'cardno'},
        {name:'nameoncard'},
        {name:'cardexpirydate', type:'date'},
        {name:'cardtype'},
        {name:'cardrefno'},
        {name:'chequeno'},
        {name:'bankname'},
        {name:'shippingterm'},
        {name:'chequedate', type:'date'},
        {name:'chequedescription'},
        {name:'termsincludegst'},
        {name:'attachment'},
        {name:'fromdate',type:'date'},
        {name:'todate',type:'date'},
        {name:'customerporefno'},
        {name:'totalprofitmargin'},
        {name:'totalprofitmarginpercent'},
        {name: 'isDraft'},
        {name: 'closeStatus'},
        {name: 'parentinvoiceid'},
        {name:'isFromPOS'},
        {name:'isactivate',type:'boolean'},
        {name:'approver'},
        {name:'ispendingapproval',type:'boolean'},
        {name: 'parentso'},
        {name: 'isWrittenOff'},
        {name:'noteno'},
        {name:'noteid'},
        {name: 'cntype'},
        {name:'paymentwindowtype'},
        {name:'porefno'},
        {name: 'isRecovered'},
        {name:'type'},
        {name:'shippingWebsite'},
        {name:'permitNumber'},
        {name:'vendcustShippingWebsite'},   
        {name:'isOpeningBalanceTransaction'},//added for identifying whether it is opening transaction
        {name:'salesPersonID'},
        {name:'gtaapplicable'},
        {name:'isMerchantExporter'},
        {name:'additionalMemo'}, // Additional memo column for INDONESIA country in Sales Invoice
        {name:'additionalMemoName'}, // Additional memo column for INDONESIA country in Sales Invoice
        {name:'agentid'},
        {name:'linkingdate' , type:'date'},//Linking for the document
        /*Below four keys are missing when we are Viewing SR from Invoice Linking Information.
         *SDP-13965
         */
        {name:'isNoteAlso'},
        {name:'isAssignSRNumberntocn'},
        {name:'cndnsequenceformatid'},
        {name: 'isRoundingAdjustmentApplied'},
        {name:'cndnnumber'},
        {name:'partialinv',type:'boolean'}
    ]);
      obj.GlobalCountOfExpandedRecords=0;    // Used for maintaining the count of expanded records in 'Link information details' window.
      obj.groupStoreUrl = "";
      if(obj.isOrder != undefined && !obj.isOrder && (obj.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId || obj.moduleid==Wtf.Acc_Invoice_ModuleId || obj.moduleid==Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || obj.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || obj.moduleid==Wtf.LEASE_INVOICE_MODULEID)){
            if(obj.businessPerson=="Customer"){
          
                obj.groupStoreUrl = 'ACCInvoiceCMN/getInvoiceLinkedInTransaction.do'
                
            }else{
              
                obj.groupStoreUrl = 'ACCGoodsReceiptCMN/getVendorInvoiceLinkedInTransaction.do'  
              
            }
        }else if(obj.isGRorDO != undefined && obj.isGRorDO){
        if(obj.businessPerson=="Customer"){
            obj.groupStoreUrl ='ACCInvoiceCMN/getDOLinkedInTransaction.do'
        }else{
            obj.groupStoreUrl = 'ACCGoodsReceiptCMN/getGRLinkedInTransaction.do'
        }
    }else if(obj.isQuotation != undefined && obj.isQuotation){
        if(obj.businessPerson=="Customer"){
            obj.groupStoreUrl ='ACCSalesOrderCMN/getCQLinkedInTransaction.do'
        }else{
            obj.groupStoreUrl ='ACCPurchaseOrderCMN/getVQLinkedInTransaction.do'
        }
    }else if(obj.isCNDN != undefined && obj.isCNDN){
            if (obj.rec.data.cntype == 5) {
                if (obj.businessPerson == "Customer") {
                    obj.groupStoreUrl = 'ACCReceiptCMN/getDNLinkedInTransaction.do'
                } else {
                    obj.groupStoreUrl = 'ACCVendorPaymentCMN/getCNLinkedInTransaction.do'
                }
            }
            if(obj.rec.data.cntype!=5){
                if(obj.businessPerson=="Customer"){
                    obj.groupStoreUrl ='ACCVendorPaymentCMN/getCNLinkedInTransaction.do'
                }else{
                    obj.groupStoreUrl ='ACCReceiptCMN/getDNLinkedInTransaction.do'
                }
            }
    }else if(obj.isRequisition != undefined && obj.isRequisition){
        if(obj.businessPerson!="Customer"){
            obj.groupStoreUrl ='ACCPurchaseOrderCMN/getPRLinkedInTransaction.do'
        }
    }else if(obj.isRFQ != undefined && obj.isRFQ){
        if(obj.businessPerson!="Customer"){
            obj.groupStoreUrl ='ACCPurchaseOrderCMN/getRFQLinkedInTransaction.do'
        }
    }else if(obj.isMakeOrReceivePayment){
            if(obj.isReceipt){
                obj.groupStoreUrl ='ACCReceiptCMN/getReceiptLinkedInTransaction.do'
            } else {
                obj.groupStoreUrl ='ACCVendorPaymentNew/getPaymentLinkedInTransaction.do'
            }
        } else if(obj.isSRorPR){
         
            if(obj.isCustomer){
               obj.groupStoreUrl ='ACCSalesReturnCMN/getSalesReturnLinkedInTransaction.do' 
            }else{
                obj.groupStoreUrl = 'ACCSalesReturnCMN/getPurchaseReturnLinkedInTransaction.do'
            }
        
        } else{
            if(obj.businessPerson=="Customer"){
                obj.groupStoreUrl ='ACCSalesOrderCMN/getSoLinkedInTransaction.do'
            }else{
                obj.groupStoreUrl = 'ACCPurchaseOrderCMN/getPoLinkedInTransaction.do'
            }
        }
    obj.groupStore = new Wtf.data.Store({
         url:obj.groupStoreUrl,
         baseParams: {
                  billid : obj.rec != undefined?(obj.isCNDN != undefined && obj.isCNDN)?obj.rec.data.noteid:obj.rec.data.billid:billid,
                  isFixedAsset :obj.rec != undefined?obj.rec.data.fixedAssetInvoice:isFixedAsset ,  //passing paramete as SO/PO number
                  isExpenseInvoice :obj.rec != undefined?obj.rec.data.isexpenseinv:false ,  //passing paramete as SO/PO number
                  isConsignment:obj.isConsignment,
                  cntype:obj.rec != undefined?obj.rec.data.cntype:"",
                  isLeaseFixedAsset:obj.isLeaseFixedAsset
            }, 
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, obj.linkRecord),
        autoLoad: false,
        sortInfo: {field: 'mergedCategoryData',direction: "DESC"},
        groupField: 'mergedCategoryData'
    });
                    
      obj.groupStore.on('loadexception',function(){
//          alert("hey");
      },obj);
      obj.groupStore.on('load',function(){
          obj.linkinfogrid.getView().refresh();
      },obj);
     
       obj.expaRec = Wtf.data.Record.create ([
        {name:'productname'},
        {name:'productdetail'},
        {name:'prdiscount'},
        {name:'discountispercent'},
        {name:'amount'},
        {name:'productid'},
        {name:'accountid'},
        {name:'accountname'},
        {name:'partamount'},
        {name:'partialDiscount'},
        {name:'quantity'},
        {name:'dquantity'},
        {name:'unitname'},
        {name:'rate'},
        {name:'rateinbase'},
        {name:'externalcurrencyrate'},
        {name:'prtaxpercent'},
        {name:'rowTaxAmount'},
        {name:'orderrate'},
        {name:'desc', convert:WtfGlobal.shortString},
        {name:'productmoved'},
        {name:'currencysymbol'},
        {name:'currencyrate'},
        {name: 'type'},
        {name: 'pid'},
        {name: 'partno'},
        {name: 'unitname'},
        {name:'carryin'},
        {name:'remark'},
        {name:'approverremark'},
        {name:'permit'},
        {name:'linkto'},
        {name:'customfield'},
        {name:'usedflag'},
        {name:'balanceQuantity'},
        {name:'balanceAmount'},
        {name:'transectionno'},
        {name:'discount'},
        {name:'memo'/*,convert:this.shortString*/},
        {name:'creationdate',type:'date'},
        {name:'duedate',type:'date'},
        {name:'totalamount'},
        {name:'amountdue',mapping:'amountduenonnegative'},
        
        //Below parameters of record are related to Payment /Receipt
        {name:'amountpaid'},
        {name:'personname'},
        {name:'rowid'},
        {name:'transectionid'},
        {name:'billid'},
        {name:'journalentryid'},
        {name:'journalentrydate'},
        {name:'personid'},
        {name:'entryno'},
        {name:'billno'},
        {name:'date',type:'date',mapping:'creationdate'},
        {name:'currencyid'},
        {name:'oldcurrencyrate'},
        {name:'currencyname'},
        {name:'oldcurrencysymbol'},
        {name:'vendorid'},
        {name:'vendorname'},
        {name:'detailsjarr'},
        {name: 'externalcurrencyrate'},
        {name:'taxpercent'},
        {name:'payment'},
        {name:'description'},
        {name:'dramount'},
        {name:'bankCharges'},
        {name:'bankChargesCmb'},
        {name:'bankInterest'},
        {name:'bankInterestCmb'},
        {name:'isprinted'},
       {name:'isRepeated'},
        //Below parameters of record are related to Credit /Debit Note linkedin Salesinvoice
        {name:'withoutinventory'},
        {name:'isReturnNote'},
        {name:'otherwise'},
        {name: 'cntype'},
        {name:'paidinvflag'},
        {name:'isprinted'},
        {name:'invcreationdate', type:'date'},
        {name:'invduedate' , type:'date'},
        {name:'grlinkdate' , type:'date'},//Linking invoice date in report CN,DN
        {name:'invamount'},
        {name:'invamountdue'},
        {name:'description'},
        {name:'isaccountdetails'},
        {name:'taxamount'},
        {name: 'debit'},
        {name: 'discountAmount'},
        {name: 'rateIncludingGst'}
    ]);
    obj.expaStoreUrl = "";
 
      obj.expaStore = new Wtf.data.Store({
          url:"ACCInvoiceCMN/getDeliveryOrderRows.do",
        baseParams:{
            mode:obj.isOrder?(obj.isCustBill?53:43):(obj.isCustBill?17:14),
            dtype : 'report'//Display type report/transaction, used for quotation,
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },obj.expaRec)
    });
   
     obj.expanderforlink = new Wtf.grid.RowExpander({});
    
     obj.groupStore.on("load", onGroupStoreLoad,obj) ;
       
        obj.smlink=(unlinkflag != undefined && unlinkflag)?new Wtf.grid.CheckboxSelectionModel({
            singleSelect: false
        }): new Wtf.grid.RowSelectionModel({
            singleSelect:true
        }) ;
         obj.gridColumnModelArrforlink=[];
            if (unlinkflag != undefined && unlinkflag){
                obj.gridColumnModelArrforlink.push(obj.smlink);
            }
        obj.gridColumnModelArrforlink.push(obj.expanderforlink,
            {
            header:WtfGlobal.getLocaleText("acc.reval.transaction"),
            dataIndex: 'transactionNo',
            width: 100,
                    renderer: function(v, m, rec) {                  
                        if (rec.json.deleted) {
                           /* Applying renderer for 
                            * Temporary Deleted transaction
                            */
                            v = "<span class='deletedlink'><del>" + v + "</del></span>";
                        } else if (rec.data.isOpeningBalanceTransaction && !rec.data.isNormalTransaction) {
                            v = "<span style='float:left;margin:-2px 14px;'>" + v + "</span>";
                        } else {
                            v = "<a class='jumplink' href='#'>" + v + "</a>";
                        }
                        return v;
                    }
            },{
            header: WtfGlobal.getLocaleText("acc.reval.evaldate"),
            align:'center',
            dataIndex: 'date',
            width: 100,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:'Linking Date',//WtfGlobal.getLocaleText("acc.reval.evaldate"),
            align:'center',
            dataIndex: 'linkingdate',
            width: 100,
            hidden: !(obj.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId || obj.moduleid==Wtf.Acc_Invoice_ModuleId || obj.moduleid==Wtf.Acc_Make_Payment_ModuleId ||obj.moduleid==Wtf.Acc_Receive_Payment_ModuleId ||obj.moduleid==Wtf.Acc_Credit_Note_ModuleId || obj.moduleid==Wtf.Acc_Debit_Note_ModuleId ),
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.ven"),
            dataIndex: 'personname',
            hidden: ((obj.isRequisition != undefined && obj.isRequisition) || (obj.isRFQ != undefined && obj.isRFQ)) ? false : true,
            width: 100
         },{
            header: WtfGlobal.getLocaleText("acc.accPref.autoJE"),
            dataIndex: 'journalEntryNo',
            width: 100
         },{
             header: WtfGlobal.getLocaleText("acc.field.withoutinventory"),
            dataIndex: 'withoutinventory',
            hidden:true       
            
         },{
            header: WtfGlobal.getLocaleText("acc.field.UsedIn"),
            dataIndex: 'mergedCategoryData',
              width: 100
        });
      
   
    var groupView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
         enableGroupingMenu: true,
          groupTextTpl: '{group} '
        });
   
        var title = WtfGlobal.getLocaleText("acc.field.LinkingInformationof") + " " + ((obj.isCNDN != undefined && obj.isCNDN) ? "Debit/Credit Note " : (obj.label != undefined ? obj.label : "Invoice")) + " " + "<b>" + (obj.rec != undefined ? (obj.isCNDN!=undefined?obj.rec.data.noteno:obj.rec.data.billno) :billno);
        var tooltip = WtfGlobal.getLocaleText("acc.field.LinkingInformationof") + " " + ((obj.isCNDN != undefined && obj.isCNDN) ? "Debit/Credit Note " : (obj.label != undefined ? obj.label : "Invoice")) + " " + "<b>" + (obj.rec != undefined ? (obj.isCNDN!=undefined?obj.rec.data.noteno:obj.rec.data.billno) : billno);//+((this.isCNDN!=undefined && this.isCNDN)?"Debit/Credit Note ":(this.label!=undefined?this.label:"Invoice")) +"<b>"+(this.isCNDN!=undefined && this.isCNDN)?(this.rec!=undefined?this.rec.data.noteno:billno):(this.rec!=undefined?this.rec.data.billno:billno)
          if(unlinkflag!==undefined){
          if (moduleid !== undefined && moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
            title = "Unlink Delivery Order " + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = "Select document(s) to unlink from/to delivery order " + (obj.rec != undefined ? obj.rec.data.billno : billno);
        }else if(moduleid !== undefined && moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            title = WtfGlobal.getLocaleText("acc.field.UnlinkSO")+" " + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = "Select document(s) to unlink from/to Sales Order " + (obj.rec != undefined ? obj.rec.data.billno : billno);
        }else if(moduleid !== undefined && moduleid == Wtf.Acc_Invoice_ModuleId) {
            title = "Unlink Invoice " + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = "Select document(s) to unlink from/to Invoice " + (obj.rec != undefined ? obj.rec.data.billno : billno);
        } else if(moduleid !== undefined && moduleid ==  Wtf.Acc_Customer_Quotation_ModuleId) {
            title = "Unlink Customer Quotation " + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = "Select document(s) to unlink from/to Customer Quotation " + (obj.rec != undefined ? obj.rec.data.billno : billno);
        } else if(moduleid !== undefined &&  moduleid==Wtf.Acc_Goods_Receipt_ModuleId) {
            title = "Unlink Goods Receipt " + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = "Select document(s) to unlink from/to Goods Receipt " + (obj.rec != undefined ? obj.rec.data.billno : billno);
        }else if(moduleid !== undefined &&  moduleid==Wtf.Acc_Purchase_Order_ModuleId) {
            title = "Unlink Purchase Order " + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = "Select document(s) to unlink from/to Purchase Order " + (obj.rec != undefined ? obj.rec.data.billno : billno);
        }else if(moduleid !== undefined &&  moduleid==Wtf.Acc_Vendor_Invoice_ModuleId) {
            title = "Unlink Purchase Invoice " + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = "Select document(s) to unlink from/to Purchase Invoice " + (obj.rec != undefined ? obj.rec.data.billno : billno);
        }else if(moduleid !== undefined &&  moduleid==Wtf.Acc_Vendor_Quotation_ModuleId) {
            title = WtfGlobal.getLocaleText("acc.invoicelist.UnlinkVendorQuotation") + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = WtfGlobal.getLocaleText("acc.invoicelist.UnlinkducumentfromVendorQuotation") + (obj.rec != undefined ? obj.rec.data.billno : billno);
        }else if(moduleid !== undefined &&  moduleid==Wtf.Acc_Purchase_Return_ModuleId) {
            title = "Unlink Purchase Return " + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = "Select document(s) to unlink from/to Purchase Return " + (obj.rec != undefined ? obj.rec.data.billno : billno);
        }else if(moduleid !== undefined &&  moduleid==Wtf.Acc_Sales_Return_ModuleId) {
            title = WtfGlobal.getLocaleText("acc.sr.UnlinkSalesReturn") +" " + (obj.rec != undefined ? obj.rec.data.billno : billno);
            tooltip = "Select document(s) to unlink from/to Sales Return " + (obj.rec != undefined ? obj.rec.data.billno : billno);
        }
          }
        var tbarArr = [];
        if (unlinkflag != undefined && unlinkflag && obj.moduleid != undefined && ((obj.moduleid == Wtf.Acc_Delivery_Order_ModuleId && !WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinkdodoc)) || (obj.moduleid == Wtf.Acc_Sales_Order_ModuleId && !WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinksodoc)) || (!WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinksidoc) && obj.moduleid == Wtf.Acc_Invoice_ModuleId) || (obj.moduleid== Wtf.Acc_Customer_Quotation_ModuleId && !WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinkcqdoc)) || (obj.moduleid== Wtf.Acc_Goods_Receipt_ModuleId && !WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinkgrdoc)) || (obj.moduleid == Wtf.Acc_Purchase_Order_ModuleId && !WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinkpodoc)) || (obj.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId && !WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinkpidoc)) ||(obj.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId && !WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinkvqdoc)) || (obj.moduleid==Wtf.Acc_Purchase_Return_ModuleId && !WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinkprdoc)) || (obj.moduleid==Wtf.Acc_Sales_Return_ModuleId && !WtfGlobal.EnableDisable(obj.uPermType, obj.permType.unlinksrdoc)))) {
            tbarArr.push('-',obj.unlinkDocumentBtn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.unlink"), // "Unlink",
                tooltip: WtfGlobal.getLocaleText("acc.field.unlink"), // "Unlink",
                id: 'unlinkDocument' +id,
                scope: obj,
                hidden: obj.pendingapproval || obj.reportbtnshwFlag,
                iconCls: 'accountingbase pricelistbutton',
                disabled: true,
                handler: unlinkDocumentsFromDO
            }));
        }
    obj.linkinfogrid = new Wtf.grid.GridPanel({
        id:id,
        store:obj.groupStore,
        sm:obj.smlink,
        layout:'fit',
        closable : true,
        title:title,
        tabTip:tooltip,
        cm:new Wtf.grid.ColumnModel(obj.gridColumnModelArrforlink),
        plugins:obj.expanderforlink,
        iconCls :'accountingbase debitnotereport',
        scroll: true,
        viewConfig:{
            forceFit: true,
            view:groupView,
             emptyText: '<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>'
       },
       tbar:tbarArr
        });
        Wtf.getCmp('as').add(obj.linkinfogrid);
        obj.expaStore.on('load', fillExpanderBody1, obj);
        obj.expanderforlink.on("expand", onRowexpand1, obj);
        obj.linkinfogrid.on('cellclick', onCellClick, obj);
        obj.smlink.on("selectionchange",function(){
            if (obj.smlink.getCount() > 0) {
                if(obj.unlinkDocumentBtn){
                    obj.unlinkDocumentBtn.enable();
                }
            } else {
                if(obj.unlinkDocumentBtn){
                    obj.unlinkDocumentBtn.disable();
                }
            }
        },obj);
     
        obj.groupStore.load();
    }else {
        this.linkinfogrid=panel;
    }
    Wtf.getCmp('as').setActiveTab(obj.linkinfogrid);
    Wtf.getCmp('as').doLayout();
      
          
  }
function unlinkDocumentsFromDO() {
    this.recSelArr = this.smlink.getSelections();
    this.partialRecArr = [];
    this.recArr=[];
    for(var i=0;i<this.recSelArr.length;i++){
        if(this.recSelArr != undefined && this.recSelArr[i].data != undefined && this.recSelArr[i].data.partialinv){
            this.partialRecArr.push(this.recSelArr[i]);
        }else{
            this.recArr.push(this.recSelArr[i]);
        }
    }
    if((this.partialRecArr.length==1 && this.recSelArr.length==1) || (this.partialRecArr.length>0 && this.recArr.length==0)){
       WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText('acc.so.UnlinkpartialInvtransaction')], 2);
       return; 
    }
    if (this.recArr != undefined && this.recArr.length < 1) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Please select a document to unlink."], 2);
        return;
    }
    this.json = "";
    for (var i = 0; i < this.recArr.length; i++) {
        this.json += "{\"type\":\"" + this.recArr[i].data.type + "\",";
        this.json += "\"billid\":\"" + this.recArr[i].data.billid + "\",";
        this.json += "\"cntype\":\"" + this.recArr[i].data.cntype + "\"},";
    }
    this.json = this.json.substr(0, this.json.length - 1);
    this.json = "[" + this.json + "]";
    var url="";
    if(this.moduleid == Wtf.Acc_Delivery_Order_ModuleId){
        url = "ACCInvoiceCMN/unlinkDeliveryOrderDocuments.do";
    }else if(this.moduleid == Wtf.Acc_Sales_Order_ModuleId){
        url=        "ACCSalesOrderCMN/unlinkSalesOrderDocuments.do";
    }else if(this.moduleid==Wtf.Acc_Invoice_ModuleId){
        url = "ACCInvoiceCMN/unlinkInvoiceDocuments.do";
    }else if (this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
        url = "ACCSalesOrderCMN/unlinkCustomerQuotationDocuments.do";
    }else if (this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId) {
        url = "ACCGoodsReceiptCMN/unlinkGoodsReceiptDocuments.do";
    }else if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
        url = "ACCPurchaseOrderCMN/unlinkPurchaseOrderDocuments.do";
    }else if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
        url = "ACCGoodsReceiptCMN/unlinkPurchaseInvoiceDocuments.do";
    }else if (this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId) {
        url = "ACCPurchaseOrderCMN/unlinkVendorQuotationDocuments.do";
    }else if (this.moduleid == Wtf.Acc_Purchase_Return_ModuleId) {
        url = "ACCGoodsReceiptCMN/unlinkPurchaseReturnDocuments.do";
    }else if (this.moduleid == Wtf.Acc_Sales_Return_ModuleId) {
         url = "ACCInvoiceCMN/unlinkSalesReturnDocuments.do";
    }
    
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.unlink.selected.document"), function(btn) {
        if (btn == 'yes') {
            Wtf.Ajax.requestEx({
                url: url,
                method: 'POST',
                params: {
                    doid: this.doid,
                    billid:this.billid,
                    data: this.json
                }
            }, this, unlinkDocSuccessResponse, unlinkDocFailureResponse);
        } else if (btn == 'no') {
            return;
        }
    }, this);
}
function unlinkDocSuccessResponse(response) {
    if (response.success) {
        if (response.isPIlinktoMP || response.isSItoRP) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.alert"),
                msg: response.msg,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.WARNING,
                scope: this,
                fn: function(btn, text, option) {
                    if (this.groupStore != undefined) {
                        this.groupStore.reload();
                    }
                }
            });
        } else {
            var noteMsg = WtfGlobal.getLocaleText('acc.so.UnlinkpartialInvtransaction');
            if(this.partialRecArr.length>0){
                var finalMsg = response.msg + "<br><b>Note:<b> " + noteMsg;
            } else{
                finalMsg = response.msg;
            }
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: finalMsg,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO,
                scope: this,
                fn: function(btn, text, option) {
                    if (this.groupStore != undefined) {
                        this.groupStore.reload();
                        /* Refreshing report after unlinking document*/
                        this.Store.reload();
                    }

                }
            });    
                    }
    } else {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
    }
}

function unlinkDocFailureResponse(response) {
    var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
    if (response.msg) {
        msg = response.msg;
    }
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
}
function onCellClick(g, i, j, e) {
    var el = e.getTarget("a");
    if (el == null)
        return;
    var formrec = this.linkinfogrid.getStore().getAt(i);
    var isExpensiveInv = formrec.get("isexpenseinv");
    this.withInvMode = formrec.get("withoutinventory");
    //var formrec = this.grid.getSelectionModel().getSelected();
    var header = g.getColumnModel().getDataIndex(j);
    if (header == "transactionNo") {
        var linktransactionname = formrec.get("mergedCategoryData");
        if (linktransactionname == "Sales Order") {
              viewTransactionTemplate1(linktransactionname, formrec,this.withInvMode,formrec.data.billid);
            //callViewSalesOrder(true, formrec, formrec.data.billid + "SalesOrder linkinfo", false);
        } else if (linktransactionname == "Consignment Request") {
            callViewConsignmentRequest(true, formrec, formrec.data.billid, false, null, true, false, false, true, true);
        } else if (linktransactionname == "Goods Receipt" || linktransactionname == "Fixed Asset Goods Receipt") {
            if(this.isFixedAsset){       
                callViewFixedAssetGoodsReceiptDelivery(true,formrec,formrec.data.billid,false,this.isFixedAsset);
            }else if(this.isConsignment){
                callViewConsignmentGoodsReceiptDelivery(true,formrec,formrec.data.billid,false,this.isFixedAsset)
            }else{
                callViewGoodsReceiptDelivery(true, formrec, formrec.data.billid + "Goods Receipt linkinfo", false, this.isFixedAsset)
            }
        } else if (linktransactionname == "Vendor Invoice" || linktransactionname == "Fixed Asset Acquired Invoice"  ) {
            callViewGoodsReceipt(formrec, formrec.data.billid + 'ViewGoodsReceipt', isExpensiveInv);
        } else if (formrec.data.cntype==5&&linktransactionname == "Debit Note") {
            callViewCreditNoteGst(true,formrec,false,false,false);
        } else if (formrec.data.cntype==5&&linktransactionname == "Credit Note") {
            callViewCreditNoteGst(true,formrec,false,false,true);
        } else if (formrec.data.cntype == Wtf.NoteForOvercharge && linktransactionname == "Debit Note") {
            var winid = 'debitnoteForOverchargeView' + formrec.get("noteno");
            callEditNoteForOvercharge(winid, formrec, true, true, false, true);
        } else if (formrec.data.cntype == Wtf.NoteForOvercharge && linktransactionname == "Credit Note") {
            var winid = 'creditnoteForOverchargeView' + formrec.get("noteno");
            callEditNoteForOvercharge(winid, formrec, true, true, true,true);
        }else if(linktransactionname=="Consignment Sales Return"){
            callViewConsignmentSalesReturn(true,formrec,formrec.data.billid,this.isLeaseFixedAsset,this.isConsignment,this.isCustomer);     
        } else if(linktransactionname=="Lease Sales Return"){
            callViewSalesFixedAssetSalesReturn(true,formrec,formrec.data.billid,false,this.isLeaseFixedAsset)
        } else if(linktransactionname=="Delivery Order" || linktransactionname=="Lease Delivery Order" || linktransactionname=="Fixed Asset Delivery Order") {
            if(this.isConsignment){
                callViewConsignmentDeliveryOrder(true,formrec,formrec.data.billid,this.isFixedAsset,this.isLeaseFixedAsset,this.isConsignment);
            } else if(this.isFixedAsset ||this.isLeaseFixedAsset){         
                callViewFixedAssetDeliveryOrder(true,formrec,formrec.data.billid,this.isFixedAsset,this.isLeaseFixedAsset);
            }else{
                callViewDeliveryOrder(true,formrec,formrec.data.billid,false,this.isFixedAsset)
            }
        } else if(linktransactionname=="Consignment Purchase Return") {
            callViewConsignmentSalesReturn(true,formrec,formrec.data.billid,this.isLeaseFixedAsset,this.isConsignment,this.isCustomer);
        } else if(linktransactionname=="Purchase Return" || linktransactionname=="Fixed Asset Purchase Return"){
            if (this.isFixedAsset) {
                callViewFixedAssetPurchaseReturn(true,formrec,formrec.data.billid,false,true);
            } else {
                callViewPurchaseReturn(true,formrec,formrec.data.billid,false,formrec.data.isNoteAlso);
            }
        }else if(linktransactionname=="Lease Order"){
            callViewFixedAssetLeaseSalesOrder(true,formrec,formrec.data.billid, false,null,false,false,false,true);
        }else if(linktransactionname=="Lease Invoice"){
            callViewFixedAssetInvoice(formrec, formrec.data.billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
        }else if(linktransactionname=="Fixed Asset Vendor Quotation"){
            callViewFixedAssetVendorQuotation(false, formrec.data.billid, formrec, true);
        }else if(linktransactionname=="Vendor Quotation" || linktransactionname=="Fixed Asset Vendor Quotation"){
            callViewVendorQuotation(false, formrec.data.billid, formrec, true);
        }else if(linktransactionname=="Fixed Asset Purchase Order"){
            callViewFixedAssetPurchaseOrder(true,formrec,formrec.data.billid,false,this,this.newtranType, true);
        }else if(linktransactionname=="Sales Return"){
            callViewSalesReturn(true,formrec,formrec.data.billid,false,formrec.data.isNoteAlso,formrec.data.isPaymentAlso);
        }
        else if (linktransactionname == "Purchase Order") {
            callViewPurchaseOrder(true, formrec, formrec.data.billid + "Purchase Order", false, this, this.newtranType, true);
        } else if (linktransactionname == "Delivery Order" || linktransactionname=="Fixed Asset Delivery Order") {
            callViewDeliveryOrder(true, formrec, formrec.data.billid + "Delivery Order", false, this.isFixedAsset)
        }
        else if(linktransactionname=="Customer Invoice"){
             viewTransactionTemplate1(linktransactionname, formrec,this.withInvMode,formrec.data.billid);
//            callViewInvoice(formrec, 'Customer Invoice');
        }
        else if(linktransactionname=="Consignment Vendor Invoice"){
            callViewConsignmentGoodsReceipt(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
        }else if(linktransactionname=="Payment Voucher" ||linktransactionname=="Payment For Refund"){
            callViewPaymentNew(formrec, 'ViewPaymentMade', false, this.grid);
        }    
        else if(linktransactionname=="Consignment Customer Invoice"){
            callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
        }
        else if(linktransactionname=="Payment Receipt" || linktransactionname=="Receipt For Refund"){
            callViewPaymentNew(formrec, 'ViewReceivePayment', true, this.grid);
        }
        else if(linktransactionname=="Debit Note"){
            callViewDebitNote( "ViewDebitNote" + formrec.get("transactionNo"), true,false,formrec.get('cntype'),formrec, null);
        }
        else if(linktransactionname=="Credit Note"){
            callViewCreditNote( "ViewcreditNote" + formrec.get("transactionNo"), true,true,formrec.get("cntype"),formrec, null);
        }else if (linktransactionname == "Customer Quotation" || linktransactionname == "Lease Quotation") {
            callViewQuotation(false,  formrec.get("billid"), formrec, true);
        }else if (linktransactionname == "Purchase Requisition") {
            
           /*Function used for showing Purchase Requisition in view mode 

            after clicking on Purchase Requisition No Linked with VQ in VQ report */
            
           callViewPurchaseReq(true,  formrec,formrec.get("billid"),  true);
        } else if (linktransactionname == "RFQ") {

            /*Function used for showing RFQ in view mode 
             
             after clicking on RFQ No Linked with VQ in VQ report */

            callViewRequestForQuotation(true, undefined, undefined, formrec);
        }
    }
}
  function expandbeforeload(scope, record, body){
       this.expaStore.proxy.conn.url = this.expaStoreUrl;       
 }
function onRowexpand1(scope, record, body){
//    this.updateStoreConfig();
    this.expanderBody=body;
    this.isexpenseinv=!this.isCustomer&&record.data.isexpenseinv;
    this.withInvMode = record.data.withoutinventory;
    this.usedflag=record.data.mergedCategoryData;
    this.invoiceRecord = record;
    this.isOpening = false;
    if(record.data.mergedCategoryData=="Customer Invoice"||record.data.mergedCategoryData=="Consignment Customer Invoice"||record.data.mergedCategoryData=="Fixed Asset Disposal Invoice" ||record.data.mergedCategoryData=="Lease Invoice"){
        this.usedflag="Customer Invoice";
        this.expaStoreUrl = "ACC" + ((this.withInvMode?"InvoiceCMN/getBillingInvoiceRows":"InvoiceCMN/getInvoiceRows")) + ".do";
        if (record.data.isOpeningBalanceTransaction != undefined && record.data.isOpeningBalanceTransaction != null && record.data.isOpeningBalanceTransaction !== "") {
            this.isOpening = record.data.isOpeningBalanceTransaction;
        }
    }else if(record.data.mergedCategoryData=="Delivery Order" || record.data.mergedCategoryData=="Lease Delivery Order" ||record.data.mergedCategoryData=="Fixed Asset Delivery Order"){
        this.usedflag="Delivery Order";
        this.expaStoreUrl = "ACCInvoiceCMN/getDeliveryOrderRows.do";
    }else if(record.data.mergedCategoryData=="Vendor Invoice"||record.data.mergedCategoryData=="Consignment Vendor Invoice"||record.data.mergedCategoryData=="Fixed Asset Acquired Invoice"){
        this.usedflag= "Vendor Invoice";
        this.expaStoreUrl = "ACC" + ((this.withInvMode?"GoodsReceiptCMN/getBillingGoodsReceiptRows.do":"GoodsReceiptCMN/getGoodsReceiptRows.do"));
        if (record.data.isOpeningBalanceTransaction != undefined && record.data.isOpeningBalanceTransaction != null && record.data.isOpeningBalanceTransaction !== "") {
            this.isOpening = record.data.isOpeningBalanceTransaction;
        }
    }else if(record.data.mergedCategoryData=="Goods Receipt" || record.data.mergedCategoryData=="Fixed Asset Goods Receipt"){
        this.expaStoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
    }else if(record.data.mergedCategoryData=="Credit Note"){
        this.expaStoreUrl = "ACCCreditNoteCMN/getCreditNoteRows.do";
        if (record.data.isOpeningBalanceTransaction != undefined && record.data.isOpeningBalanceTransaction != null && record.data.isOpeningBalanceTransaction !== "") {
            this.isOpening = record.data.isOpeningBalanceTransaction;
        }
    }else if(record.data.mergedCategoryData=="Payment Receipt"){
        this.expaStoreUrl="ACCReceiptCMN/getReceiptRowsNew.do"
    }else if(record.data.mergedCategoryData=="Debit Note"){
        this.expaStoreUrl = "ACCDebitNote/getDebitNoteRows.do";
        if (record.data.isOpeningBalanceTransaction != undefined && record.data.isOpeningBalanceTransaction != null && record.data.isOpeningBalanceTransaction !== "") {
            this.isOpening = record.data.isOpeningBalanceTransaction;
        }
    }else if(record.data.mergedCategoryData=="Payment Voucher"){
        this.expaStoreUrl="ACCVendorPaymentCMN/getPaymentRowsNew.do"
    }else if(record.data.mergedCategoryData=="Sales Return"||record.data.mergedCategoryData=="Consignment Sales Return"||record.data.mergedCategoryData=="Lease Sales Return"){
        this.usedflag="Sales Return";
        this.expaStoreUrl = "ACCInvoiceCMN/getSalesReturnRows.do";
    }else if(record.data.mergedCategoryData=="Purchase Return" ||record.data.mergedCategoryData=="Consignment Purchase Return" ||record.data.mergedCategoryData=="Fixed Asset Purchase Return" ){
        this.usedflag="Purchase Return";
        this.expaStoreUrl = "ACCGoodsReceiptCMN/getPurchaseReturnRows.do";
    } else if(record.data.mergedCategoryData=="Sales Order"||record.data.mergedCategoryData=="Consignment Sales Order"||record.data.mergedCategoryData=="Lease Order"){//ERP-11337
        this.usedflag="Sales Order";
        this.expaStoreUrl = "ACCSalesOrderCMN/getSalesOrderRows.do";
    } else if(record.data.mergedCategoryData=="Master Contract"){
        this.usedflag="Master Contract";
        this.expaStoreUrl = "ACCContractMasterCMN/getMasterContractRows.do";
    } else if(record.data.mergedCategoryData=="Purchase Order"||record.data.mergedCategoryData=="Consignment Purchase Order"||record.data.mergedCategoryData=="Fixed Asset Purchase Order"){//ERP-11337
        this.usedflag="Purchase Order";
        this.expaStoreUrl = "ACCPurchaseOrderCMN/getPurchaseOrderRows.do";
    }else if(record.data.mergedCategoryData=="Vendor Quotation"||record.data.mergedCategoryData=="Fixed Asset Vendor Quotation"){
        this.usedflag="Vendor Quotation";
        this.expaStoreUrl = "ACCPurchaseOrderCMN/getQuotationRows.do";
    }else if(record.data.mergedCategoryData=="Receipt For Refund"){
        this.usedflag="Payment Receipt";
        this.expaStoreUrl="ACCReceiptCMN/getReceiptRowsNew.do"
    }else if(record.data.mergedCategoryData=="Payment For Refund"){
        this.usedflag="Payment Voucher";
        this.expaStoreUrl="ACCVendorPaymentCMN/getPaymentRowsNew.do"
    } else if(record.data.mergedCategoryData=="Customer Quotation" || record.data.mergedCategoryData=="Lease Quotation"){
        this.usedflag="Customer Quotation";
        this.expaStoreUrl="ACCSalesOrderCMN/getQuotationRows.do"
    }else if(record.data.mergedCategoryData=="Purchase Requisition"){
        this.expaStoreUrl= "ACCPurchaseOrderCMN/getRequisitionRows.do"
    } else if (record.data.mergedCategoryData == "RFQ") {
        this.expaStoreUrl = "ACCPurchaseOrderCMN/getRFQRows.do"
    }   

    this.expaStore.proxy.conn.url = this.expaStoreUrl;       
    this.expaStore.load({
        params:{
            bills:record.data.billid,
            isexpenseinv:(this.isexpenseinv != undefined && this.isexpenseinv !="")?(!this.isCustomer&&record.data.isexpenseinv):false
            }
        });    
}
  function fillExpanderBody1(){
    var disHtml = "";
    var header = "";
    var cndnHeader="";
    if(this.usedflag=="Payment Voucher" ) {
        disHtml = getExpanderDataForPayment(this.expaStore,false);
    }else if (this.usedflag=="Payment Receipt"){
        disHtml = getExpanderDataForPayment(this.expaStore,true);
    }else if(this.usedflag=="Credit Note" || this.usedflag=="Debit Note"){
            
        if(this.expaStore.getCount()>0){            
            var rec=this.expaStore.getAt(0);
            var disHtml = "";
            var disHtmlPaidInv = "";
            var arr=[];
            var arrPaidInv=[];
            var custArr = [];
            var cndnCount=0;
            custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[this.usedflag=="Debit Note" ? Wtf.Acc_Debit_Note_ModuleId:Wtf.Acc_Credit_Note_ModuleId]);
            var creditJobTextPaidInv = WtfGlobal.getLocaleText("acc.cnList.prod") ;
            if(rec.data.withoutinventory){
                creditJobTextPaidInv = WtfGlobal.getLocaleText("acc.cnList.prodNonInv")+' '+WtfGlobal.getLocaleText("acc.cnList.Desc");
            }
            arrPaidInv=[creditJobTextPaidInv,
            (rec.data.withoutinventory)?"":WtfGlobal.getLocaleText("acc.cnList.Desc"),WtfGlobal.getLocaleText("acc.cnList.TransNo"),
            WtfGlobal.getLocaleText("acc.cnList.qty"),
            WtfGlobal.getLocaleText("acc.cnList.gridAmt"),
            WtfGlobal.getLocaleText("acc.cnList.gridMemo"),
            WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
            "                "];
            var gridHeaderTextPaidInv = (rec.data.withoutinventory)?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInvPaidInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pListPaidInv");
            var headerPaidInv = "<span class='gridHeader'>"+gridHeaderTextPaidInv+"</span>"; //Product List
            headerPaidInv += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";    //S.No.
            for(var j=0;j<arrPaidInv.length;j++){
                headerPaidInv += "<span class='headerRow'>" + arrPaidInv[j] + "</span>";
            }
            headerPaidInv += "<span class='gridLine'></span>"; 

            arr=[WtfGlobal.getLocaleText("acc.prList.invNo"),
            WtfGlobal.getLocaleText("acc.prList.creDate"),
            WtfGlobal.getLocaleText("acc.prList.dueDate"),            
            WtfGlobal.getLocaleText("acc.prList.invoicelinkingDate"),//Linking invoice date in report CN,DN
            WtfGlobal.getLocaleText("acc.prList.invAmt"),
            WtfGlobal.getLocaleText("acc.prList.amtDue")];
            var gridHeaderText = WtfGlobal.getLocaleText("acc.field.InvoiceDetails");//(rec.data.withoutinventory)?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>"; //Product List
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";    //S.No.
            for(var j=0;j<arr.length;j++){
                header += "<span class='headerRow'>" + arr[j] + "</span>";
            }                       
            header += "<span class='gridLine'></span>";    
            
            arrDNCN = [this.usedflag=="Credit Note"?WtfGlobal.getLocaleText("Debit Note No"):WtfGlobal.getLocaleText("Credit Note No"),
            WtfGlobal.getLocaleText("Creation Date"),
            WtfGlobal.getLocaleText("Amount"),
            WtfGlobal.getLocaleText("acc.prList.amtDue")];

            var gridHeaderText = this.usedflag=="Credit Note"?WtfGlobal.getLocaleText("Debit Note Details"):WtfGlobal.getLocaleText("Credit Note Details");//(rec.data.withoutinventory)?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
            cndnHeader = "<span class='gridHeader'>" + gridHeaderText + "</span>"; //Product List
            cndnHeader += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";    //S.No.
            for (var j = 0; j < arrDNCN.length; j++) {
                cndnHeader += "<span class='headerRow'>" + arrDNCN[j] + "</span>";
            }
            cndnHeader += "<span class='gridLine'></span>";    
            //code for account details
            var type="";
            if (Wtf.account.companyAccountPref.manyCreditDebit)
            {
                type= WtfGlobal.getLocaleText("acc.CNDNList.expand.Type");
            }
            var AccArr=[
            WtfGlobal.getLocaleText("acc.je.acc"),
            type,
            /*In case of opening Debit/Credit Note not showing Tax amount & Tax percent column*/
            this.isOpening?"":WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"),
            this.isOpening?"": WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),
            WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
            WtfGlobal.getLocaleText("acc.cnList.Desc"),
            ];
            var arrayLength=AccArr.length;
            for(var custArrcount=0;custArrcount<custArr.length;custArrcount++){
                if(custArr[custArrcount].header != undefined )
                    AccArr[arrayLength+custArrcount]=custArr[custArrcount].header;
            }
            var count=0;
            for(var custArrcount=0;custArrcount<AccArr.length;custArrcount++){
                if(AccArr[custArrcount] != ""){
                    count++;
                }
            }
            var widthInPercent=80/count;
            var minWidth = count*100 + 40;
            var AccGridHeaderText =   WtfGlobal.getLocaleText("acc.field.accountDetails");
            var AccHeader = "<span class='gridHeader'>"+AccGridHeaderText+"</span>";  
            AccHeader += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";    //S.No.
            for(var j=0;j<AccArr.length;j++){
                AccHeader += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + AccArr[j] + "</span>";
            } 
            AccHeader += "<span class='gridLine'></span>";  
            var AccCount=1; 
            var disHtmlAccHeader='';
       
            var cntype = '2';
            var paidInvCnt = 1;
            var invCnt = 1;
            var ptransaction="";
            for(i=0;i<this.expaStore.getCount();i++){
                rec=this.expaStore.getAt(i); 
                
                var accountDetailsRec=rec.json;   
                cntype = rec.data['cntype'];
                if (cntype == '5') {
                    if (ptransaction == rec.data.transectionno) {
                        continue;
                    }
                }
                ptransaction=rec!=""?rec.data.transectionno:"";
                if(rec.data['paidinvflag'] == '1') {
                    headerPaidInv += "<span class='gridNo'>"+(paidInvCnt)+".</span>";
                    headerPaidInv += "<span class='gridRow'  wtf:qtip='"+rec.data['productname']+"'>"+Wtf.util.Format.ellipsis(rec.data['productname'],20)+"</span>";
                    if(!(rec.data.withoutinventory))
                        headerPaidInv += "<span class='gridRow' wtf:qtip='"+rec.data['productname']+"' >"+Wtf.util.Format.ellipsis(rec.data['desc'],20)+"&nbsp;</span>";
                    headerPaidInv += "<span class='gridRow ' >"+"<a  class='jumplink' href='#' onClick='javascript:invLink(\""+rec.data['transectionid']+"\",\""+this.isCNReport+"\",\""+this.isCustBill+"\")'>"+rec.data['transectionno']+"</a>"+"</span>";
                    headerPaidInv += "<span class='gridRow'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
                    headerPaidInv += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['discount'],rec.data['currencysymbol'],[true])+"</span>";
                    if(rec.data['memo']==''){
                        headerPaidInv += "<span class='gridRow'>&nbsp;</span>";
                    }
                    else{
                        headerPaidInv += "<span class='gridRow' style='width:30%'  wtf:qtip='"+rec.data['memo']+"'>"+unescape(Wtf.util.Format.ellipsis(rec.data['memo'],80))+"</span>";
                    }
                    headerPaidInv += "<span class='gridRow' style='width:30%'  wtf:qtip='"+rec.data['remark']+"'>"+unescape(Wtf.util.Format.ellipsis(rec.data['remark'],80))+"</span>";
                    headerPaidInv +="<br>";
                    paidInvCnt++;
                }else if(rec.data['isaccountdetails'] == true) {
                    AccHeader += "<span class='gridNo' >"+(AccCount)+".</span>";
                    AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'  wtf:qtip='"+accountDetailsRec['accountname']+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec['accountname'],20)+"</span>";
                    if (Wtf.account.companyAccountPref.manyCreditDebit){
                        AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;' wtf:qtip='"+accountDetailsRec['debit']+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec['debit'],20)+"</span>";
                    }
                    /*In case of opening Debit/Credit Note not showing Tax amount & Tax percent */
                    this.isOpening?"":AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;' wtf:qtip='"+accountDetailsRec['taxpercent']+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec['taxpercent'],20)+"</span>";
                    this.isOpening?"":AccHeader += "<span class='gridRow'style='width:"+widthInPercent+"% ! important;' >"+WtfGlobal.addCurrencySymbolOnly(accountDetailsRec['taxamount'],accountDetailsRec['currencysymbol'],[true])+"</span>";
                    AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;' >"+WtfGlobal.addCurrencySymbolOnly(accountDetailsRec['totalamount'],accountDetailsRec['currencysymbol'],[true])+"</span>";
                    /* Assigning non-breaking space for description data if it is blank or null*/
                    if (accountDetailsRec['description'] != null && accountDetailsRec['description'] != "") {
                        AccHeader += "<span class='gridRow'style='width:"+widthInPercent+"% ! important;'  wtf:qtip='"+accountDetailsRec['description']+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec['description'],20)+"</span>";
                    } else {
                        AccHeader += "<span class='gridRow'style='width:"+widthInPercent+"% ! important;'  wtf:qtip='"+accountDetailsRec['description']+"'>"+Wtf.util.Format.ellipsis("&nbsp",20)+"</span>";
                    }
                    //AccHeader += "<span class='gridRow'style='width:"+widthInPercent+"% ! important;'  wtf:qtip='"+accountDetailsRec['description']+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec['description'],20)+"</span>";
                    for(var j=0;j<custArr.length;j++){
                        if(accountDetailsRec[custArr[j].dataIndex]!=undefined && accountDetailsRec[custArr[j].dataIndex]!="null" && accountDetailsRec[custArr[j].dataIndex]!="")
                            AccHeader += "<span class='gridRow'style='width:"+widthInPercent+"% ! important;'  wtf:qtip='"+accountDetailsRec[custArr[j].dataIndex]+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec[custArr[j].dataIndex],15)+"</span>";
                        else
                            AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                    }
                    AccHeader +="<br>";
                    AccCount++;
                }else if ((this.usedflag == "Debit Note" || this.usedflag == "Credit Note") && this.isCNDN) {
                    /*Showing Debit Note & Credit Note details while linking with each other*/
                    cndnHeader += "<span class='gridNo'>" + (invCnt) + ".</span>";
                    /* LInk not showing when document linked with opening document*/
                    if (rec.json.isOpeningDnCn) {
                        cndnHeader += "<span class='gridRow'>"+rec.data['transectionno']+"</span>";
                    } else {
                        cndnHeader += "<span class='gridRow ' >" + "<a  class='jumplink' href='#' onClick='javascript:noteRecLinkNew(\"" + rec.data['transectionid'] + "\",\"" + (this.usedflag == "Debit Note" ? false : true) + "\",\"" + this.isCustBill + "\")'>" + rec.data['transectionno'] + "</a>" + "</span>";
                    }
                      
                    cndnHeader += "<span class='gridRow'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['invcreationdate'])+"</span>";
                    cndnHeader += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['invamount'],rec.data['currencysymbol'],[true])+"</span>";
                    cndnHeader += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['invamountdue'],rec.data['currencysymbol'],[true])+"</span>";

                    cndnHeader +="<br>";
                    cndnCount++;
                    invCnt++;
                } else {
                    header += "<span class='gridNo'>"+(invCnt)+".</span>";
                    if(cntype==5 && this.usedflag == "Credit Note"){
                        header += "<span class='gridRow ' >"+"<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\""+rec.data['transectionid']+"\",\""+false+"\",\""+this.isCustBill+"\")'>"+rec.data['transectionno']+"</a>"+"</span>";
                    }else if(cntype==5 && this.usedflag == "Debit Note"){
                        header += "<span class='gridRow ' >"+"<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\""+rec.data['transectionid']+"\",\""+true+"\",\""+this.isCustBill+"\")'>"+rec.data['transectionno']+"</a>"+"</span>";
                    }else{
                        header += "<span class='gridRow ' >"+"<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\""+rec.data['transectionid']+"\",\""+(cntype == 4 ? false :(cntype ==1)?false: true)+"\",\""+this.isCustBill+"\")'>"+rec.data['transectionno']+"</a>"+"</span>";
                    }
                    
                    header += "<span class='gridRow'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['invcreationdate'])+"</span>";
                    header += "<span class='gridRow'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['invduedate'])+"</span>";
                    header += "<span class='gridRow'>"+(rec.data['grlinkdate']!=''?WtfGlobal.onlyDateLeftRenderer(rec.data['grlinkdate']):'-')+"</span>";//Linking invoice date in report CN,DN
                    header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['invamount'],rec.data['currencysymbol'],[true])+"</span>";
                    header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['invamountdue'],rec.data['currencysymbol'],[true])+"</span>";

                    header +="<br>";    
                    invCnt++;
                }
            }
            disHtmlAccHeader += "<div class='expanderContainer' style='width:100%'>" + AccHeader + "</div>";
            if (cndnCount>0) {
                disHtml += "<div class='expanderContainer' style='width:100%'>" + cndnHeader + "</div>";
            } else {
                disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
            }
        
            disHtmlPaidInv += "<br><div class='expanderContainer' style='width:100%'>" + headerPaidInv + "</div>";
            if(cntype == '3') {//CN against Paid Invoice
                if(invCnt == 1 && AccCount > 1){
                    this.expanderBody.innerHTML = disHtmlAccHeader+disHtmlPaidInv;
                }else if(invCnt >  1 && AccCount == 1){
                    this.expanderBody.innerHTML = disHtml+disHtmlPaidInv;
                }else{
                    this.expanderBody.innerHTML = disHtmlPaidInv;
                //                this.expanderBody.innerHTML = disHtml + disHtmlPaidInv + disHtmlAccHeader;
                }
            } else if(invCnt == 1 && AccCount > 1){
                this.expanderBody.innerHTML = disHtmlAccHeader;
                disHtml=disHtmlAccHeader;
            }else if(invCnt >  1 && AccCount == 1){
                this.expanderBody.innerHTML = disHtml;
            }else {
                this.expanderBody.innerHTML = disHtml+disHtmlAccHeader;
                disHtml=disHtml+disHtmlAccHeader;
            }
        } else {
            this.expanderBody.innerHTML = "<br><b><div class='expanderContainer' style='width:100%'>"+WtfGlobal.getLocaleText("acc.prList.gridAmtReceived")+"</div></b>"      //This transaction is not linked with any invoice.
        }
        if (this.isOpening != undefined && this.isOpening &&  ! (this.usedflag=="Credit Note"||this.usedflag=="Debit Note")) {
            var openingArr = [WtfGlobal.getLocaleText("acc.invoiceList.totAmt"), WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome")];
            var arrayLength = openingArr.length;
            var openingHeader = "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";    //S.No.
            for (var j = 0; j < openingArr.length; j++) {
                openingHeader += "<span class='headerRow' style='width: 15% ! important;'>" + openingArr[j] + "</span>";
            }
            openingHeader += "<span class='gridLine'></span>";
            openingHeader+="<br>";
            if (this.invoiceRecord.data != undefined && this.invoiceRecord.data.amount != "" && this.invoiceRecord.data.amountinbase != "") {
                openingHeader += "<span class='gridNo'>" + (1) + ".</span>";
                openingHeader += "<span class='gridRow' style='width: 15% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(this.invoiceRecord.data.amount, this.invoiceRecord.data.currencysymbol, [true]) + "</span>";
                openingHeader += "<span class='gridRow' style='width: 15% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(this.invoiceRecord.data.amountinbase, WtfGlobal.getCurrencySymbol(), [true]) + "</span>";
            }
            disHtml += "<div class='expanderContainer' style='width:100%'>" + openingHeader + "</div>";
        }
    } else {
        var arr=[];
        if(this.isexpenseinv){//for vendor expense invoice[PS]
            arr = getExpenseInvoiceHeader();
            var prevBillid = "";
            var sameParent = false;
            for (var i = 0; i < this.expaStore.getCount(); i++) {
                var rec = this.expaStore.getAt(i);
                var currentBillid = rec.data['billid'];
                if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
                    prevBillid = currentBillid;
                    sameParent = false;
                } else {
                    sameParent = true;
                }
                var header = getExpInvoiceExpanderData(rec, sameParent, arr[1], arr[2]);
                disHtml = "<div class='expanderContainer1'>" + arr[0] + header + "</div>";
            }
        }else{
            var israteincludegst =  false;
            if(this.expaStore.getCount()>0){      // Taken first record for expander heand
                var exprec=this.expaStore.getAt(0);
                israteincludegst = exprec.json.israteIncludingGst;
            }
            if(this.usedflag=="Customer Invoice" && !this.isOpening)
            {
                arr=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
                (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity,
                //                this.isRFQ ? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"),//Unit Price
                /* Rate Including GST Must Be Handled*/
                (this.isRFQ ? '' : (israteincludegst ? ((Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDONESIA && Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDIA) ? WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST") : WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingVAT")) : WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"))),//Unit Price

                (this.isCustomer || this.businessPerson == "Customer")?WtfGlobal.getLocaleText("acc.field.PartialAmount(%)") : '',
                WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),//Discount
                (this.isRFQ || this.isRequisition)? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),//Tax
                this.isRFQ ? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),//Amount
                (this.withInvMode && (this.isCustomer)?WtfGlobal.getLocaleText("acc.field.SONo"):WtfGlobal.getLocaleText("acc.field.SO/DO/CQNo"))];          
            }
            else if(this.usedflag=="Delivery Order" || this.usedflag=="Fixed Asset Delivery Order"){
                arr=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
                (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
                WtfGlobal.getLocaleText("acc.do.partno"),//Model/Serial no.
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity
                (this.isCustomer)?WtfGlobal.getLocaleText("acc.accPref.deliQuant"):"",//Delivered Quantity
                (this.isCustomer)?WtfGlobal.getLocaleText("acc.field.CI/SONo"):"",               
                WtfGlobal.getLocaleText("acc.invoice.gridRemark")]//Reason 
            }else if(this.usedflag=="Vendor Invoice"  && !this.isOpening || this.usedflag==" Fixed Asset Acquired Invoice")  {
                arr=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
                (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity,
                (this.isRFQ ? '' : (israteincludegst ? ((Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDONESIA && Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDIA) ? WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST") : WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingVAT")) : WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"))),//Unit Price
                WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),//Discount
                (this.isRFQ || this.isRequisition)? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),//Tax
                this.isRFQ ? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),//Amount
                (this.withInvMode&& (!(this.isCustomer))?WtfGlobal.getLocaleText("acc.field.PONo"):WtfGlobal.getLocaleText("acc.field.PO/GR/VQNo"))];          
            }else if(this.usedflag=="Vendor Quotation" || this.usedflag==" Fixed Asset Vendor Quotation")  {
                arr=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
                (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity,
                WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"),//Unit Price
                WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),//Discount
                WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),//Tax
                WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),//Amount
                WtfGlobal.getLocaleText("acc.field.PurchaseRequisitionRFQ.No")];           
            }else if(this.usedflag=="Goods Receipt" || this.usedflag=="Fixed Asset Goods Receipt") {
                arr=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
                (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
                WtfGlobal.getLocaleText("acc.do.partno"),//Model/Serial no.
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity
                (this.isCustomer)?WtfGlobal.getLocaleText("acc.accPref.deliQuant"):WtfGlobal.getLocaleText("acc.accPref.recQuant"),//Delivered Quantity
                (this.isCustomer)?"":WtfGlobal.getLocaleText("acc.field.VI/PONo"),               
                WtfGlobal.getLocaleText("acc.invoice.gridRemark")]//Reason 
            }else if(this.usedflag=="Credit Note"||this.usedflag=="Debit Note") {
                arr=[
                WtfGlobal.getLocaleText("acc.cnList.TransNo"),
                WtfGlobal.getLocaleText("acc.cnList.gridAmt"),
                WtfGlobal.getLocaleText("acc.cnList.gridMemo"),
                "                "];
            }else if(this.usedflag=="Payment Receipt"||this.usedflag=="Payment Voucher") {
                if(this.isCNDN){
                    arr=[
                    (this.usedflag=="Payment Receipt")?WtfGlobal.getLocaleText("acc.prList.gridReceiptNo"):WtfGlobal.getLocaleText("acc.pmList.gridPaymentNo"),
                    WtfGlobal.getLocaleText("acc.prList.creDate"),
                    WtfGlobal.getLocaleText("acc.prList.dueDate"),
                    WtfGlobal.getLocaleText("acc.cnList.gridAmt"),
                    WtfGlobal.getLocaleText("acc.prList.amtDue"),
                    this.usedflag=="Payment Voucher"?WtfGlobal.getLocaleText("acc.prList.amtPaid"):WtfGlobal.getLocaleText("acc.prList.amtRec"),
                    "                "];
                }else{
                    arr=[
                    WtfGlobal.getLocaleText("acc.prList.invNo"),
                    WtfGlobal.getLocaleText("acc.prList.creDate"),
                    WtfGlobal.getLocaleText("acc.prList.dueDate"),
                    WtfGlobal.getLocaleText("acc.prList.invAmt"),
                    WtfGlobal.getLocaleText("acc.prList.amtDue"),
                    this.usedflag=="Payment Voucher"?WtfGlobal.getLocaleText("acc.prList.amtPaid"):WtfGlobal.getLocaleText("acc.prList.amtRec"),
                    "                "];
                }
            }else if(this.usedflag=="Sales Return"||this.usedflag=="Purchase Return" || this.usedflag=="Fixed Asset Purchase Return") {
                   
                var productTypeText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pTypeNonInv") : WtfGlobal.getLocaleText("acc.invoiceList.expand.pType");

                arr=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
                (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
                (this.isCustomer?
                    productTypeText:(Wtf.account.companyAccountPref.countryid == '203' && !this.isQuotation && !this.isOrder)?
                    WtfGlobal.getLocaleText("acc.field.PermitNo."):productTypeText),//Product Type
                WtfGlobal.getLocaleText("acc.do.partno"),//Model/Serial no.
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity
                WtfGlobal.getLocaleText("acc.accPref.returnQuant"),
                WtfGlobal.getLocaleText("acc.invoice.gridRemark"),//Reason
                (this.businessPerson =="Customer")?(this.isLeaseFixedAsset? WtfGlobal.getLocaleText("acc.field.LeaseDONO"):WtfGlobal.getLocaleText("acc.field.CI/DONo")):WtfGlobal.getLocaleText("acc.field.VI/GRNo"),               

                "                  "];
            }else if(this.usedflag=="Sales Order"||this.usedflag=="Purchase Order"){//Showing SO information in PO & Vice versa
                arr=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
                (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity,
                (this.isRFQ ? '' : (israteincludegst ? ((Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDONESIA && Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDIA) ? WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST") : WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingVAT")) : WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"))),//Unit Price
                WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),//Discount
                (this.isRFQ || this.isRequisition)? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),//Tax
                this.isRFQ ? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),//Amount
                //                        (this.withInvMode && (this.isCustomer)?WtfGlobal.getLocaleText("acc.field.SONo"):WtfGlobal.getLocaleText("acc.field.SO/DO/CQNo"))];          
                (this.usedflag=="Sales Order"?WtfGlobal.getLocaleText("acc.field.CQ/PO.No"):WtfGlobal.getLocaleText("acc.field.SO/VQ.No"))];          
            }else if(this.usedflag=="Master Contract"){//Showing SO information in PO & Vice versa
                arr=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
                (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity,
                ];          
            }
            else if(this.usedflag=="Customer Quotation"){
                arr = [ WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"),
                WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"), //Quantity,
                WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"),
                WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"), //Discount
                WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"), //Tax
                WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),//Amount
                this.isLeaseFixedAsset ?'': WtfGlobal.getLocaleText("acc.field.VQ.No")];
            }else if(this.usedflag=="Purchase Requisition"){
                            
                /* Creating Record for Purchase Requisition Rows when showing in VQ linking/Unliking tab*/
                            
                arr = [ WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"),
                WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"), //Quantity,
                WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"),
                //WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"), //Discount
                WtfGlobal.getLocaleText("acc.invoiceList.expand.amt")] //Amount
                      
            } else if (this.usedflag == "RFQ") {

                /* Creating Record for RFQ Rows when showing in VQ linking/Unliking tab*/

                arr = [WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"),
                WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty")] //Quantity
                      
            } else if (this.isOpening) {
                arr = [WtfGlobal.getLocaleText("acc.invoiceList.totAmt"), WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome")];
            }

            //         
            //            arr=[(this.isCustBill?'':'Product ID'),(this.isCustBill?'Product Details':'Product Name' ),(this.isCustBill?'':'Product Type'),'Quantity','Unit Price',(this.isOrder||this.isQuotation)?'':'Discount','Tax Percent','Amount',"                  "];//(this.isCustBill?'':'Remark'),
            var productTypeText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pTypeNonInv") : WtfGlobal.getLocaleText("acc.invoiceList.expand.pType");
            var gridHeaderText = this.usedflag=="Credit Note"||this.usedflag=="Debit Note"?WtfGlobal.getLocaleText("acc.field.InvoiceDetails"):this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
            if(this.expaStore.getCount()>0){            
                header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
            }   //Product List
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
            var custArr = [];
            custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[this.moduleid]);
            var arrayLength=arr.length;
            var arrCounter=0;
            for(var custCount=0;custCount<custArr.length;custCount++){
                var headerFlag=false;
                if(custArr[custCount].header != undefined ) {
                   
                    if(headerFlag){
                        arr[arrayLength]=custArr[custCount].header;
                        arrayLength=arr.length;
                    }
                }
            }
            for(var arrI=0;arrI<arr.length;arrI++){
                if(arr[arrI]!=undefined && this.usedflag=="Sales Return"||this.usedflag=="Purchase Return"|| this.usedflag=="Fixed Asset Purchase Return" || this.usedflag=="Sales Order"||this.usedflag=="Purchase Order"){
                    header += "<span class='headerRow' style='width: 10%'>" + arr[arrI] + "</span>";
                }else if(this.isOpening){
                    header += "<span class='headerRow' style='width: 15% ! important;'>" + arr[arrI] + "</span>";
                }else{
                    header += "<span class='headerRow' style='width: 10% ! important;'>" + arr[arrI] + "</span>";
                }
            }
            header += "<span class='gridLine'></span>";
           
                    
                 
            for(var storeCount=0;storeCount<this.expaStore.getCount();storeCount++){
                rec=this.expaStore.getAt(storeCount);
                //column prod name
                var productname=this.withInvMode?rec.data['productdetail']: rec.data['productname'];
                israteincludegst = rec.json.israteIncludingGst; // CHECK IF RATE INCLUDE GST 
                //Column : S.No.
                header += "<span class='gridNo'>"+(storeCount+1)+".</span>";

                //Column : Product Id for Inventory
                if(!this.withInvMode)
                {
                    var pid=rec.data['pid'];
                    var productid=rec.data['productid']; // ERP-13247 [SJ]
                    if(this.usedflag=="Sales Return"||this.usedflag=="Purchase Return"||this.usedflag=="Fixed Asset Purchase Return" || this.usedflag=="Sales Order"||this.usedflag=="Purchase Order" ||this.usedflag=="Customer Quotation" || this.usedflag=="Master Contract") {
                        header += "<span class='gridRow'  wtf:qtip='"+pid+"' style='width: 10% ! important;'><a class='jumplink' wtf:qtip='"+pid+"' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\""+productid+"\","+this.isFixedAsset+")'>"+Wtf.util.Format.ellipsis(pid,10)+"</a></span>";// ERP-13247 [SJ]
                    }else{
                        header += "<span class='gridRow'  wtf:qtip='"+pid+"' style='width: 10% ! important;'><a class='jumplink' wtf:qtip='"+pid+"' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\""+productid+"\","+this.isFixedAsset+")'>"+Wtf.util.Format.ellipsis(pid,10)+"</a></span>";// ERP-13247 [SJ]
                    }
                    //Column : Product Name
                    if(this.usedflag=="Sales Return"||this.usedflag=="Purchase Return"||this.usedflag=="Fixed Asset Purchase Return" || this.usedflag=="Sales Order"||this.usedflag=="Purchase Order" ||this.usedflag=="Purchase Requisition" ||this.usedflag=="Master Contract") {
                        header += "<span class='gridRow'  wtf:qtip='"+productname+"' style='width: 10% ! important;'><a class='jumplink' wtf:qtip='"+productname+"' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\""+productid+"\","+this.isFixedAsset+")'>"+Wtf.util.Format.ellipsis(productname,10)+"</a></span>";// ERP-13247 [SJ]
                    }else{
                        header += "<span class='gridRow'  wtf:qtip='"+productname+"' style='width: 10% ! important;'><a class='jumplink' wtf:qtip='"+productname+"' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\""+productid+"\","+this.isFixedAsset+")'>"+Wtf.util.Format.ellipsis(productname,10)+"</a></span>";// ERP-13247 [SJ]
                    }
           
           
                    //balance Quantity
                    //                if(!this.withInvMode&&(this.isOrder&&this.isCustomer||this.isOrder&&!this.isCustomer))
                    //                    header += "<span class='gridRow' style='width: 7% ! important;'>"+rec.data['balanceQuantity']+" "+rec.data['unitname']+"</span>";
                    if(this.usedflag=="Delivery Order" || this.usedflag=="Fixed Asset Delivery Order")
                    // if(rec.data.usedflag=="Delivery Order")
                    {
                        //Part No for delivery order
                        if(rec.data['partno'] != ""){
                            header += "<span class='gridRow' style='word-wrap:break-word;width: 10%'>"+rec.data['partno']+"</span>";
                        } else {
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
                        //Quantity
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
               
                        //Delivered Quantity for delicery order
                        if(rec.data['dquantity']!=undefined)
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['dquantity']+" "+rec.data['unitname']+"</span>";
                        else
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
          
                        // link to CI/So for delivery order
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['linkto']+"&nbsp;</span>";               
                        //remark for delicery order
                        if(rec.data['remark']!="")
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['remark']+"</span>";
                        else
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                    }
                    if(this.usedflag=="Customer Invoice")
                    //if(rec.data.usedflag=="Customer Invoice")
                    {
                        //Quantity
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
                        //Unit Price
                        var rate=(israteincludegst? rec.data.rateIncludingGst:rec.data.rate);
                        var rateValue = WtfGlobal.addCurrencySymbolOnly(rate,rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            rateValue = Wtf.UpriceAndAmountDisplayValue;
                        }
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rateValue+"</span>";
                        //partial amount
                        if (arr.indexOf(WtfGlobal.getLocaleText("acc.field.PartialAmount(%)")) >= 0) {
                            header += "<span class='gridRow' style='width: 10% ! important;'>" + rec.data['partamount'] + "% " + "&nbsp;</span>";
                        }
                        //discount
                        if(rec.data.discountispercent == 0){
                            header += "<span class='gridRow' style='width: 10% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
                        } else {
                            header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
                        }
                        //tax amount
                        var taxValue = WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'],rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            taxValue = Wtf.UpriceAndAmountDisplayValue;
                        }
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+taxValue+"</span>";
                        //amount
                        amount=0;
                        amount=rec.data['quantity']*rate;
                        if(rec.data['partamount'] != 0){
                            amount = amount * (rec.data['partamount'] /100);
                        }
                   
                        var discount = 0;
                        if(rec.data.prdiscount > 0) {
                              if(rec.data['partamount'] != 0){
                                  discount = rec.data.partialDiscount;
                              }else{
                                  if(rec.data.discountispercent == 0){
                                      discount = rec.data.prdiscount;
                                  } else {
                                      discount = (amount * rec.data.prdiscount) / 100;
                                  }
                              }
                        }
       
                        amount=(amount-discount);
                        if(!israteincludegst){ // IF RATE INCLUDE GST THEN NO NEED TO ADD TAX AMOUNT
                            amount+=rec.data['rowTaxAmount'];//(amount*rec.data['prtaxpercent']/100);
                        }
                        var amtValue = WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            amtValue = Wtf.UpriceAndAmountDisplayValue;
                        }
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+amtValue+"</span>";
                        // link to for customer invoice
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['linkto']+"&nbsp;</span>";         
                    }
               
                    if(this.usedflag=="Sales Order"||this.usedflag=="Purchase Order")  //Showing SO information in PO & Vice versa
                    //if(rec.data.usedflag=="Customer Invoice")
                    {
                        //Quantity
                        header += "<span class='gridRow' style='width: 10% ! important;text-align:left;'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
                        //Unit Price
                        var rate=(israteincludegst? rec.data.rateIncludingGst:rec.data.rate);
                        var rateValue = WtfGlobal.addCurrencySymbolOnly(rate,rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            rateValue = Wtf.UpriceAndAmountDisplayValue;
                        }
                        header += "<span class='gridRow' style='width: 10% ! important;text-align:left;'>"+rateValue+"</span>";
                        //discount
                        if(rec.data.discountispercent == 0){
                            header += "<span class='gridRow' style='width: 10% ! important;text-align:left;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
                        } else {
                            header += "<span class='gridRow' style='width: 10% ! important;text-align:left;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
                        }
                        //tax amount
                        var taxValue = WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'],rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            taxValue = Wtf.UpriceAndAmountDisplayValue;
                        }
                        header += "<span class='gridRow' style='width: 10% ! important;text-align:left;'>"+taxValue+"</span>";
                        //amount
                        amount=0;
                        amount=rec.data['quantity']*rate;
                        if(rec.data['partamount'] != 0){
                            amount = amount * (rec.data['partamount'] /100);
                        }
                   
                        var discount = 0;
                        if(rec.data.prdiscount > 0) {
                            if(rec.data.discountispercent == 0){
                                discount = rec.data.prdiscount;
                            } else {
                                discount = (amount * rec.data.prdiscount) / 100;
                            }
                        }
       
                        amount=(amount-discount);
                        if(!israteincludegst){ // IF RATE INCLUDE GST THEN NO NEED TO ADD TAX AMOUNT
                            amount+=rec.data['rowTaxAmount'];//(amount*rec.data['prtaxpercent']/100);
                        }
            
                        var amtValue = WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            amtValue = Wtf.UpriceAndAmountDisplayValue;
                        }
                        header += "<span class='gridRow' style='width: 10% ! important;text-align:left;'>"+amtValue+"</span>";
                        // link to for customer invoice
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['linkto']+"&nbsp;</span>";         
                    }
                    if(this.usedflag=="Master Contract")  //Showing SO information for Master Contract
                    {
                        //Quantity
                        header += "<span class='gridRow' style='width: 10% ! important;text-align:left;'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";           
                    }
               
                    if(this.usedflag=="Vendor Invoice" ||this.usedflag==" Fixed Asset Acquired Invoice")
                    //if(rec.data.usedflag=="Vendor Invoice")
                    {
                        //Quantity
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
                        //Unit Price
                        var rate=(israteincludegst? rec.data.rateIncludingGst:rec.data.rate);
                        var rateValue = WtfGlobal.addCurrencySymbolOnly(rate,rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            rateValue = Wtf.UpriceAndAmountDisplayValue;
                        } 
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rateValue+"</span>";
                 
                        //discount
                        if(rec.data.discountispercent == 0){
                            header += "<span class='gridRow' style='width: 10% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
                        } else {
                            header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
                        }
                        //tax amount
                        var taxValue = WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'],rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            taxValue = Wtf.UpriceAndAmountDisplayValue;
                        }
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+taxValue+"</span>";
                        //amount
                        amount=0;
                        amount=rec.data['quantity']*rate;
                        if(rec.data['partamount'] != 0){
                            amount = amount * (rec.data['partamount'] /100);
                        }
                   
                        var discount = 0;
                        if(rec.data.prdiscount > 0) {
                            if(rec.data.discountispercent == 0){
                                discount = rec.data.prdiscount;
                            } else {
                                discount = (amount * rec.data.prdiscount) / 100;
                            }
                        }
       
                        amount=(amount-discount);
                        if(!israteincludegst){ // IF RATE INCLUDE GST THEN NO NEED TO ADD TAX AMOUNT
                            amount+=rec.data['rowTaxAmount'];//(amount*rec.data['prtaxpercent']/100);
                        }
                        var amtValue = WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            amtValue = Wtf.UpriceAndAmountDisplayValue;
                        }
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+amtValue+"</span>";
                        // link to for customer invoice
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['linkto']+"&nbsp;</span>";         
                    }
                    
                    if(this.usedflag=="Vendor Quotation" || this.usedflag=="Customer Quotation" || this.usedflag=="Purchase Requisition" || this.usedflag==" Fixed Asset Vendor Quotation") 
                    //if(rec.data.usedflag=="Vendor Invoice")
                    {
                        //Quantity
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
                        //Unit Price
                        var rate=rec.data.rate;
                        var rateValue = WtfGlobal.addCurrencySymbolOnly(rate,rec.data['currencysymbol'],[true]);
                        //if(this.isRFQ==undefined || (!this.isRFQ && !(this.usedflag=="Vendor Quotation")) || (this.isRFQ && (this.usedflag=="Purchase Requisition"))){
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            rateValue = Wtf.UpriceAndAmountDisplayValue;
                        } 
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rateValue+"</span>"; 
                        // }
                          

                        //discount
                        if(this.usedflag!="Purchase Requisition"){
                            if(rec.data.discountispercent == 0){
                                header += "<span class='gridRow' style='width: 10% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
                            } else {
                                header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
                            }
                        }
                        //tax amount
                        //  header += "<span class='gridRow' style='width: 10% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'],rec.data['currencysymbol'],[true])+"</span>";
                        //amount
                        amount=0;
                        amount=rec.data['quantity']*rate;
                        if(rec.data['partamount'] != 0){
                            amount = amount * (rec.data['partamount'] /100);
                        }
                    
                        var discount = 0;
                        if(rec.data.prdiscount > 0) {
                            if(rec.data.discountispercent == 0){
                                discount = rec.data.prdiscount;
                            } else {
                                discount = (amount * rec.data.prdiscount) / 100;
                            }
                        }
        
                        amount=(amount-discount);
                        amount+=rec.data['rowTaxAmount'];//(amount*rec.data['prtaxpercent']/100);
                        //                    if(this.usedflag=="Customer Quotation"){
                        if(this.usedflag!="Purchase Requisition"){
                            //if(!this.isRFQ ){
                            var taxValue = WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'],rec.data['currencysymbol'],[true]);
                            if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                                taxValue = Wtf.UpriceAndAmountDisplayValue;
                            }
                            header += "<span class='gridRow' style='width: 10% ! important;'>" + taxValue + "</span>";  
                        //}     
                             
                        }
                        //                    }
                   
                        //if(this.isRFQ==undefined || (!this.isRFQ && !(this.usedflag=="Vendor Quotation")) || (this.isRFQ && (this.usedflag=="Purchase Requisition"))){
                        var amtValue = WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true]);
                        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                            amtValue = Wtf.UpriceAndAmountDisplayValue;
                        }
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+amtValue+"</span>";
                        // }
                          
                        // link to for customer invoice
                        if(this.usedflag!="Purchase Requisition"){
                            header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['linkto']+"&nbsp;</span>";       
                        }                           
                    }
                                
                    if (this.usedflag == "RFQ") {
                        header += "<span class='gridRow' style='width: 10% ! important;'>" + rec.data['quantity'] + " " + rec.data['unitname'] + "</span>";
                    }
                    if(this.usedflag=="Goods Receipt" || this.usedflag=="Fixed Asset Goods Receipt")
                    //if(rec.data.usedflag=="Goods Receipt")
                    {
                        //Part No for delivery order
                        if(rec.data['partno'] != ""){
                            header += "<span class='gridRow' style='word-wrap:break-word;width: 10%'>"+rec.data['partno']+"</span>";
                        } else {
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
                        //Quantity
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
               
                        //Delivered Quantity for delicery order
                        if(rec.data['dquantity']!="")
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['dquantity']+" "+rec.data['unitname']+"</span>";
                        else
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
          
                        // link to CI/So for delivery order
                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['linkto']+"&nbsp;</span>";               
                        //remark for delicery order
                        if(rec.data['remark']!="")
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['remark']+"</span>";
                        else
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                    }
               
                    if(this.usedflag=="Credit Note"||this.usedflag=="Debit Note") {
               
                        if(rec.data['transectionno']!=""){
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['transectionno']+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
               
                        if(rec.data['discount']!=""){
                            header += "<span class='gridRow' style='width: 10%'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['discount'],rec.data['currencysymbol'],[true])+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
               
                        if(rec.data['memo']!=""){
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['memo']+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
                    }
              
              
                    if(this.usedflag=="Payment Receipt"||this.usedflag=="Payment Voucher") {
               
                        if(rec.data['transectionno']!=""){
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['transectionno']+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
               
                        if(rec.data['creationdate']!=""){
                            header += "<span class='gridRow' style='width: 10%'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['creationdate'])+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
               
                        if(rec.data['duedate']!=""){
                            header += "<span class='gridRow' style='width: 10%'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['duedate'])+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
               
                        if(rec.data['totalamount']!==""){
                            header += "<span class='gridRow' style='width: 10%'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['totalamount'],rec.data['currencysymbol'],[true])+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
               
                        if(rec.data['amountdue']!==""){
                            header += "<span class='gridRow' style='width: 10%'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['amountdue'],rec.data['currencysymbol'],[true])+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
               
                        if(rec.data['amountpaid']!==""){
                            header += "<span class='gridRow' style='width: 10%'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['amountpaid'],rec.data['currencysymbol'],[true])+"</span>";
                        }else{
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
                    }
           
                    if(this.usedflag=="Sales Return"||this.usedflag=="Purchase Return" || this.usedflag=="Fixed Asset Purchase Return") {
                        if(!this.isCustomer && !this.isQuotation && !this.isOrder && Wtf.account.companyAccountPref.countryid == '203')
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['permit']+"&nbsp;</span>";
                        else if(!this.withInvMode){
                            var type = "";
                            type = rec.data['type']
                            header += "<span class='gridRow' style='width: 10%'>"+type+"</span>";
                        }
                        else {
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }

                        //Part No
                        if(rec.data['partno'] != ""){
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['partno']+"</span>";
                        } else {
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                        }
                
                        //Quantity
                        header += "<span class='gridRow' style='width: 10%'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
               
                        //Delivered Quantity
                        header += "<span class='gridRow' style='width: 10%'>"+rec.data['dquantity']+" "+rec.data['unitname']+"</span>";

                        //Reason
                        if(rec.data['remark']!="")
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['remark']+"</span>";
                        else
                            header += "<span class='gridRow' style='width: 10%'>&nbsp;</span>";
                   
                        //VI/GR
                        if(rec.data['linkto']!="")
                        {
                            header += "<span class='gridRow' style='width: 10%'>"+rec.data['linkto']+"</span>";      
                        }        
                    //                for(i=0;i<custArr.length;i++){
                    //                    if(rec.data[custArr[i].dataIndex]!=undefined && rec.data[custArr[i].dataIndex]!="null")
                    //                        header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data[custArr[i].dataIndex]+"&nbsp;</span>";
                    //                    else
                    //                        header += "<span class='gridRow' style='width: 10% ! important;'>&nbsp;&nbsp;</span>";
                    //                }
                    //               header +="</div>";
                    }
                }
           
                //Blank Column
                if(!this.withInvMode)
                    header += "<span class='gridRow' style='width: 10% ! important;'>"+rec.data['productmoved']+"</span>";
                header +="<br>";
            }
            if(this.isOpening && this.invoiceRecord!=undefined){
                if(this.invoiceRecord.data!=undefined && this.invoiceRecord.data.amount!="" && this.invoiceRecord.data.amountinbase!=""){
                    header += "<span class='gridNo'>" + (1) + ".</span>";
                    header += "<span class='gridRow' style='width: 15% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(this.invoiceRecord.data.amount, this.invoiceRecord.data.currencysymbol, [true]) + "</span>";
                    header += "<span class='gridRow' style='width: 15% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(this.invoiceRecord.data.amountinbase, WtfGlobal.getCurrencySymbol(), [true]) + "</span>";
                }
            }
            header +="</div>";
            disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
        }       
    }
    this.expanderBody.innerHTML = disHtml;
    expandAllLinkedDocuments(this) ;    // After filling the  HTML contents for some particular record, function 'expandAllLinkedDocuments' will be called again to expand next record.
}
    
    function  getExpenseInvoiceHeader(){
        var arr=[];
        var expInvHeaderArray = [];
        arr=[WtfGlobal.getLocaleText("acc.invoiceList.accName") ,WtfGlobal.getLocaleText("acc.product.gridType"), WtfGlobal.getLocaleText("acc.invoiceList.expand.description") ,WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),WtfGlobal.getLocaleText("acc.invoice.gridamountIncludingGST"),WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),'                  '];
        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.invoiceList.expand.accList")+"</span>";   //Account List
        var count=0;
        for(var i=0;i<arr.length;i++){
            if(arr[i] != ""){
                count++;
            }
        }
        var widthInPercent=100/count;
        var minWidth = count*100 + 40;
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";  
        expInvHeaderArray.push(header);
        expInvHeaderArray.push(minWidth);
        expInvHeaderArray.push(widthInPercent);
        return expInvHeaderArray;
    }
   
    function getExpInvoiceExpanderData(rec, sameParent, minWidth, widthInPercent) {
        var accountname = rec.data['accountname'];
        var description = "";
        if (rec.data['desc'] != null && rec.data['desc'] != undefined)
            description = rec.data['desc'];

        if (!sameParent) {
            this.expenseHeader = "";
            this.srNumber = 0;
        }
        var type="Credit";
        if(rec.json.debit){
            type="Debit";
        }

        this.expenseHeader += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        this.expenseHeader += "<span class='gridNo'>" + (++this.srNumber) + ".</span>";
        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + accountname + "'>" + Wtf.util.Format.ellipsis(accountname, 15) + "&nbsp;</span>";
        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + type + "'>" + Wtf.util.Format.ellipsis(type, 25) + "&nbsp;</span>";
        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + description + "'>" + Wtf.util.Format.ellipsis(description, 25) + "&nbsp;</span>";
        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.data.rate, rec.data['currencysymbol'], [true]) + "</span>";
        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.json.rateIncludingGstEx, rec.data['currencysymbol'], [true]) + "</span>";
        
        if(rec.data.discountispercent == 0){
            this.expenseHeader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
        } else {
            this.expenseHeader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
        }


        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'], rec.data['currencysymbol'], [true]) + "</span>";
        var amount=rec.data.rate;
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 0){
                amount = rec.data.rate -rec.data.prdiscount;
            } else {
                amount = rec.data.rate -getRoundedAmountValue( (rec.data.rate * rec.data.prdiscount) / 100);
            }
        }
        if(rec.json.isgstincluded!=true){// when GST not applied we does not need to add tax separately. But not applied then we need to add it
            amount += rec.data['rowTaxAmount'];//amount+=(amount*rec.data.prtaxpercent/100);
        }
        this.expenseHeader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(amount, rec.data['currencysymbol'], [true]) + "</span>";
        this.expenseHeader += "<br>";
        this.expenseHeader += "</div>";
        return this.expenseHeader;
    }
    
    function getExpanderDataForPayment(expandStore,isReceipt){
        if(expandStore.getCount()>0) {
            var disHtml = "";
            var custArr = [];
            custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[this.moduleid]);
            for(var i=0;i<expandStore.getCount();i++){
                var expandStoreRec=expandStore.getAt(i);
                if(expandStoreRec.json['type']==1){ 
                    var arr=[];
                    var isRefund = false;
                    var isGSTUsedForAdvancePayment=false;
                    var paymentWinowType= expandStoreRec.json['paymentWindowType']
                    var gridHeaderText = "Advance Payment";
                    if(isReceipt){
                        paymentWinowType==1?arr.push('Customer'):arr.push('Vendor');
                        if(paymentWinowType!=1) {
                            isRefund = true;
                            arr.push('Payment No')
                            gridHeaderText = "Refund/ Deposit";
                        }
                        if(paymentWinowType==1 && Wtf.account.companyAccountPref.countryid==Wtf.Country.MALAYSIA){
                            isGSTUsedForAdvancePayment=true;
                            arr.push('GST Code')
                        }
                    } else {
                        paymentWinowType==1?arr.push('Vendor'):arr.push('Customer');
                        if(paymentWinowType!=1) {
                            isRefund = true;
                            arr.push('Receipt No')
                            gridHeaderText = "Refund/ Deposit";
                        }
                    }    
                    arr.push('Total Amount','Amount Due','Amount Paid',"                ");
                    var recArray=expandStoreRec.json['typedata']                    
                    var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";  //Product List
                    
                    var arrayLength=arr.length;
                    for(var custArrcount=0;custArrcount<custArr.length;custArrcount++){
                        if(custArr[custArrcount].header != undefined )
                            arr[arrayLength+custArrcount]=custArr[custArrcount].header;
                    }
                    var count=0;
                    for(var custArrcount=0;custArrcount<arr.length;custArrcount++){
                        if(arr[custArrcount] != ""){
                            count++;
                        }
                    }
                    var widthInPercent=100/count;
                    var minWidth = count*100 + 40;
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
                    for(var l1=0;l1<arr.length;l1++){
                         header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[l1] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for(var l2=0;l2<recArray.length;l2++){
                        var rec=recArray[l2];
                        header += "<span class='gridNo'>"+(l2+1)+".</span>";
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+rec.accountname+"</span>";
                        if(isRefund) {
                            header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+(rec.transectionno!==undefined ? rec.transectionno : "&nbsp;&nbsp;-&nbsp;&nbsp;")+"</span>";
                        }
                        if(isGSTUsedForAdvancePayment){
                            header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+(rec.gstAccountName!==undefined ? rec.gstAccountName : "&nbsp;&nbsp;-&nbsp;&nbsp;")+"</span>";
                        }
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountdue,rec.currencysymbol,[true])+"</span>";
                        var paidAmount = rec.paidamount;
                        if(isRefund) {
                            paidAmount = rec.paidamountOriginal;
                        }
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(paidAmount,rec.currencysymbol,[true])+"</span>";
                        for(var j=0;j<custArr.length;j++){
                            if(rec[custArr[j].dataIndex]!=undefined && rec[custArr[j].dataIndex]!="null" && rec[custArr[j].dataIndex]!="")
                                header += "<span class='gridRow' wtf:qtip='"+rec[custArr[j].dataIndex]+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex],15)+"&nbsp;</span>";
                            else
                                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                        }
                        header +="<br>";
                    }
                    header +="</div>";
                    disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
                }
                if(expandStoreRec.json['type']==5){
                    var arr=[];
                    var recArray=expandStoreRec.json['typedata']
                    // arr=['Invoice No','Creation Date','Due Date','Invoice Amount','Amount Due',(this.isReceipt?'Amount Received':'Amount Paid'),"
                    arr=[WtfGlobal.getLocaleText("acc.prList.invNo"),WtfGlobal.getLocaleText("acc.prList.creDate"),WtfGlobal.getLocaleText("acc.prList.dueDate"),WtfGlobal.getLocaleText("acc.prList.invAmt"),WtfGlobal.getLocaleText("acc.prList.amtDue"),(isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")),"                "];
                    var gridHeaderText = "Used Advance Payment Against Invoices";
                    var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";  //Product List
//                    var custArr = [];
//                    custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[this.moduleid]);
                    var arrayLength=arr.length;
                    for(var custArrcount=0;custArrcount<custArr.length;custArrcount++){
                        if(custArr[custArrcount].header != undefined )
                            arr[arrayLength+custArrcount]=custArr[custArrcount].header;
                    }
                    var count=0;
                    for(var custArrcount=0;custArrcount<arr.length;custArrcount++){
                        if(arr[custArrcount] != ""){
                            count++;
                        }
                    }
                    var widthInPercent=100/count;
                    var minWidth = count*100 + 40;
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
                    for(var i2=0;i2<arr.length;i2++){
                            header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i2] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for(var m2=0;m2<recArray.length;m2++){
                        var rec=recArray[m2];
                        var creationdate=(rec.creationdate!=undefined||rec.creationdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.creationdate)):"";
                        var duedate=(rec.duedate!=undefined||rec.duedate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.duedate)):"";
                        header += "<span class='gridNo'>"+(m2+1)+".</span>";
                        header += "<span class='gridRow ' style='width:"+widthInPercent+"% ! important;'>"+"<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\""+rec.transectionid+"\",\""+isReceipt+"\",\""+rec+"\")'>"+rec.transectionno+"</a>"+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+creationdate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+duedate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountDueOriginal,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountpaid,rec.currencysymbol,[true])+"</span>";
                        for(var j=0;j<custArr.length;j++){
                            if(rec[custArr[j].dataIndex]!=undefined && rec[custArr[j].dataIndex]!="null" && rec[custArr[j].dataIndex]!="")
                                header += "<span class='gridRow' wtf:qtip='"+rec[custArr[j].dataIndex]+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex],15)+"&nbsp;</span>";
                            else
                                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                        }
                        header +="<br>";
                    }
                    header +="</div>";
                    disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
                }
                if(expandStoreRec.json['type']==2){
                    var arr=[];
                    var recArray=expandStoreRec.json['typedata']
                    arr=[WtfGlobal.getLocaleText("acc.prList.invNo"),WtfGlobal.getLocaleText("acc.prList.creDate"),WtfGlobal.getLocaleText("acc.prList.dueDate"),WtfGlobal.getLocaleText("acc.prList.invAmt"),WtfGlobal.getLocaleText("acc.prList.amtDue"),((CompanyPreferenceChecks.discountOnPaymentTerms())?WtfGlobal.getLocaleText("acc.field.discount"):""),(isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")),"                "];
                    var gridHeaderText = isReceipt?"Payment Against Customer Invoice":"Payment Against Vendor Invoice";
                    var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";  
                    var arrayLength=arr.length;
                    for(var custArrcount=0;custArrcount<custArr.length;custArrcount++){
                        if(custArr[custArrcount].header != undefined )
                            arr[arrayLength+custArrcount]=custArr[custArrcount].header;
                    }
                    var count=0;
                    for(var custArrcount=0;custArrcount<arr.length;custArrcount++){
                        if(arr[custArrcount] != ""){
                            count++;
                        }
                    }
                    var widthInPercent=100/count;
                    var minWidth = count*100 + 40;
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
                    for(var i1=0;i1<arr.length;i1++){
                         header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i1] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for(var i2=0;i2<recArray.length;i2++){
                        var rec=recArray[i2];
                        var creationdate=(rec.creationdate!=undefined||rec.creationdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.creationdate)):"";
                        var duedate=(rec.duedate!=undefined||rec.duedate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.duedate)):"";
                        header += "<span class='gridNo'>"+(i2+1)+".</span>";
                        header += "<span class='gridRow '  style='width:"+widthInPercent+"% ! important;' >"+"<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\""+rec.transectionid+"\",\""+isReceipt+"\",\""+rec+"\")'>"+rec.transectionno+"</a>"+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+creationdate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+duedate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountdue,rec.currencysymbol,[true])+"</span>";
                        if (CompanyPreferenceChecks.discountOnPaymentTerms()) {
                            header += "<span class='gridRow'  style='width:" + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.discountAmount, rec.currencysymbol, [true]) + "</span>";
                            header += "<span class='gridRow'  style='width:" + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly((rec.amountpaid), rec.currencysymbol, [true]) + "</span>";
                        } else {
                            header += "<span class='gridRow'  style='width:" + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.amountpaid, rec.currencysymbol, [true]) + "</span>";
                        }
                        for(var j=0;j<custArr.length;j++){
                            if(rec[custArr[j].dataIndex]!=undefined && rec[custArr[j].dataIndex]!="null" && rec[custArr[j].dataIndex]!="")
                                header += "<span class='gridRow' wtf:qtip='"+rec[custArr[j].dataIndex]+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex],15)+"&nbsp;</span>";
                            else
                                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                        }
                        header +="<br>";
                    }
                    header +="</div>";
                    disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
                }
                if(expandStoreRec.json['type']==3 || expandStoreRec.json['type']==8){       // 3 refers to CN/DN used in payment/receipt at line level . 8 refers to CN/DN linked to Advance Payment/Receipts
                    var type = expandStoreRec.json['type'];
                    var arr=[];
                    this.noteType="";
                    var recArray=expandStoreRec.json['typedata']
                    var paymentWinowType= expandStoreRec.json['paymentWindowType']
                    if(isReceipt){
                        arr.push('Debit Note');
                        paymentWinowType==1?arr.push('Customer'):arr.push('Vendor');
                        paymentWinowType==1?this.noteType=4:this.noteType=1;        // notetype 1=DN against vendor,notetype 4=DN against customer

                    } else {
                        arr.push('Credit Note');
                        paymentWinowType==1?arr.push('Vendor'):arr.push('Customer');
                        paymentWinowType==1?this.noteType=4:this.noteType=1;        // notetype 4=CN against vendor,notetype 1=CN against customer
                    }
                    arr.push('Total Amount','Amount Due','Amount Paid',"                ")
                    var gridHeaderText ='' ;
                    if(isReceipt){
                        type == 3 ?gridHeaderText = "Payment Against Debit Note":gridHeaderText = "Used Advance Payment Against Debit Note";
                    } else {
                        type == 3 ?gridHeaderText = "Payment Against Credit Note":gridHeaderText = "Used Advance Payment Against Credit Note";
                    } 
                    var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";  //Product List
                    var arrayLength=arr.length;
                    for(var custArrcount=0;custArrcount<custArr.length;custArrcount++){
                        if(custArr[custArrcount].header != undefined )
                            arr[arrayLength+custArrcount]=custArr[custArrcount].header;
                    }
                    var count=0;
                    for(var custArrcount=0;custArrcount<arr.length;custArrcount++){
                        if(arr[custArrcount] != ""){
                            count++;
                        }
                    }
                    var widthInPercent=100/count;
                    var minWidth = count*100 + 40;
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
                    for(var k1=0;k1<arr.length;k1++){
                         header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[k1] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for(var k2=0;k2<recArray.length;k2++){
                        var rec=recArray[k2];
                        header += "<span class='gridNo'>"+(k2+1)+".</span>";
                        if(rec.isopening){
                            header += "<span class='gridRow ' style='width:"+widthInPercent+"% ! important;' >"+rec.transectionno+"</span>";    
                        }else{
                            header += "<span class='gridRow ' style='width:"+widthInPercent+"% ! important;' >"+"<a  class='jumplink' href='#' onClick='javascript:noteRecLinkNew(\""+rec.transectionid+"\",\""+isReceipt+"\",\""+this.noteType+"\",\""+rec+"\")'>"+rec.transectionno+"</a>"+"</span>";
                        }
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+rec.accountname+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountdue,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.cnpaidamount,rec.currencysymbol,[true])+"</span>";
                         for(var j=0;j<custArr.length;j++){
                            if(rec[custArr[j].dataIndex]!=undefined && rec[custArr[j].dataIndex]!="null" && rec[custArr[j].dataIndex]!="")
                                header += "<span class='gridRow' wtf:qtip='"+rec[custArr[j].dataIndex]+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex],15)+"&nbsp;</span>";
                            else
                                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                        }
                        header +="<br>";
                    }
                    header +="</div>";
                    disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
                }
                if(expandStoreRec.json['type']==4){
                    var arr=[];
                    var recArray=expandStoreRec.json['typedata']
                    arr=['Account','Tax Percent','Tax Amount','Amount Paid',"                "];
                    var gridHeaderText = "Payment Against GL";
                    var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";  //Product List

                    var arrayLength=arr.length;
                    for(var custArrcount=0;custArrcount<custArr.length;custArrcount++){
                        if(custArr[custArrcount].header != undefined )
                            arr[arrayLength+custArrcount]=custArr[custArrcount].header;
                    }
                    var count=0;
                    for(var custArrcount=0;custArrcount<arr.length;custArrcount++){
                        if(arr[custArrcount] != ""){
                            count++;
                        }
                    }
                    var widthInPercent=100/count;
                    var minWidth = count*100 + 40;
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
                    for(var j1=0;j1<arr.length;j1++){
                         header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[j1] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for(var j2=0;j2<recArray.length;j2++){
                        var rec=recArray[j2];
                        header += "<span class='gridNo'>"+(j2+1)+".</span>";
                        header += "<span class='gridRow'  wtf:qtip='"+rec.accountname+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.accountname,15)+"</span> ";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+rec.taxpercent+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.taxamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        for(var j=0;j<custArr.length;j++){
                            if(rec[custArr[j].dataIndex]!=undefined && rec[custArr[j].dataIndex]!="null" && rec[custArr[j].dataIndex]!="")
                                header += "<span class='gridRow' wtf:qtip='"+rec[custArr[j].dataIndex]+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex],15)+"&nbsp;</span>";
                            else
                                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                        }
                        header +="<br>";
                    }
                    header +="</div>";
                    disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
                }
                
            }
            
        }
        else{
            disHtml = "<br><b><div class='expanderContainer' style='width:100%'>"+WtfGlobal.getLocaleText("acc.prList.gridAmtReceived")+"</div></b>"      //This transaction is not linked with any invoice.
        }

        return disHtml;
    }
    function onGroupStoreLoad(storeObject,recordObj){
        expandAllLinkedDocuments(this)
    }
   
    function expandAllLinkedDocuments(scopeObject){
        if(scopeObject.groupStore.getCount()>0 && scopeObject.GlobalCountOfExpandedRecords<scopeObject.groupStore.getCount()){  // If loaded records>0
            var index = scopeObject.GlobalCountOfExpandedRecords;
            scopeObject.expanderforlink.expandRow(index);
            scopeObject.GlobalCountOfExpandedRecords=scopeObject.GlobalCountOfExpandedRecords+1;
        }  
    }
    
    
    
