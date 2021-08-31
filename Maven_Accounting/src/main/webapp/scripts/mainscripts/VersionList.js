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
 *  SagarM - Commented below unused code 
 */
//function openCashTransTab(isCustomer,isCustBill){
//    if(isCustBill)
//        callBillingSalesReceipt(false,null);
//    else if(isCustomer)
//        callSalesReceipt(false,null);
//    else
//        callPurchaseReceipt(false,null);
//}
//
//function openInvTab(isTran,isOrder,isCustBill){
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
//        if(isTran && isOrder)
//            callSalesOrder(false,null);
//        else if(isTran)
//            callInvoice(false,null);
//        else if(!isTran && isOrder)
//            callPurchaseOrder(false,null);
//        else
//            callGoodsReceipt(false,null);
//    }
//}
//
//function openQuotationTab(){
//    callQuotation();
//}

Wtf.account.VersionListPanel=function(config){
    Wtf.apply(this, config);
    this.appendID = true;
    this.invID=null;
    this.exponly=null;
    this.recArr=[];
    this.isCash=true;
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.costCenterId = "";
    this.extraFilters = config.extraFilters;
    if(config.extraFilters != undefined){//Cost Center Report View
        this.costCenterId = config.extraFilters.costcenter?config.extraFilters.costcenter:"";
    }
    this.index = "";
    this.archiveFlag = "";
    this.label = config.label;
    this.isOrder=config.isOrder;
    this.isCustBill=config.isCustBill;
    this.isSalesCommissionStmt=config.isSalesCommissionStmt;
    this.isCash=config.cash?true:false;
    this.isexpenseinv=false;
    this.nondeleted=false;
    this.deleted=false;
    this.PR_MEMOS = "";
    this.customizeData="";
    this.isfavourite=false;
    this.isOpeningBalanceInvoices=false;
    this.isOpeningBalanceOrder=false;
    this.isOutstanding=false;
    this.isFixedAsset = (config.isFixedAsset)?config.isFixedAsset:false;
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset)?config.isLeaseFixedAsset:false;
    this.isConsignment = (config.isConsignment)?config.isConsignment:false;
    this.uPermType= (config.isRequisition || config.isRFQ ? Wtf.UPerm.vendorpr : (config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice));
    this.permType= config.isRequisition || config.isRFQ ? Wtf.Perm.vendorpr :(config.isCustomer?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice);
    this.uPaymentPermType= config.isRequisition || config.isRFQ ? Wtf.UPerm.vendorpr : (config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.createPaymentPermType=(config.isCustomer?Wtf.Perm.invoice.createreceipt:Wtf.Perm.vendorinvoice.createpayment);
    this.exportPermType= config.isRFQ ? this.permType.exportdatarfq : (config.isRequisition ? this.permType.printpr : (config.isCustomer?(this.isOrder?this.permType.exportdataso:this.permType.exportdatainvoice):(this.isOrder?this.permType.exportdatapo:this.permType.exportdatavendorinvoice)));
    this.printPermType= config.isRFQ ? this.permType.printrfq : (config.isRequisition ? this.permType.exportdatapr : (config.isCustomer?(this.isOrder?this.permType.printso:this.permType.printinvoice):(this.isOrder?this.permType.printpo:this.permType.printvendorinvoice)));
    this.removePermType= config.isRFQ ? this.permType.removerfq : (config.isRequisition ? this.permType.removepr : (config.isCustomer?(this.isOrder?this.permType.removeso:this.permType.removeinvoice):(this.isOrder?this.permType.removepo:this.permType.removevendorinvoice)));
    this.editPermType= config.isRFQ ? this.permType.editrfq : (config.isRequisition ? this.permType.editpr : (config.isCustomer?(this.isOrder?this.permType.editso:this.permType.editinvoice):(this.isOrder?this.permType.editpo:this.permType.editvendorinvoice)));
    this.copyPermType=(config.isCustomer?this.permType.copyinvoice:this.permType.copyvendorinvoice);
    this.emailPermType=config.isRFQ ? this.permType.exportdatarfq : (config.isCustomer?this.permType.emailinvoice:this.permType.emailvendorinvoice);
    this.recurringPermType=this.permType.recurringinvoice;
    this.isQuotation = config.isQuotation;
    this.moduleid= config.moduleId;
    this.isPurchaseOrder= config.isPurchaseOrder;
    this.reportbtnshwFlag=config.reportbtnshwFlag;
    if(this.isQuotation == undefined || this.isQuotation == null){
        this.isQuotation = false;
    }
    if(this.reportbtnshwFlag== undefined || this.reportbtnshwFlag == null){
        this.reportbtnshwFlag=false;
    }
    this.person=config.person;
    this.versionid=config.versionid;
    this.expandRec = Wtf.data.Record.create ([
    {
        name:'productname'
    },

    {
        name:'productdetail'
    },

    {
        name:'prdiscount'
    },

    {
        name:'discountispercent'
    },

    {
        name:'amount'
    },

    {
        name:'productid'
    },

    {
        name:'accountid'
    },

    {
        name:'accountname'
    },

    {
        name:'partamount'
    },

    {
        name:'quantity'
    },

    {
        name:'unitname'
    },

    {
        name:'uomname'
    },

    {
        name:'rate'
    },

    {
        name:'rateinbase'
    },

    {
        name:'externalcurrencyrate'
    },

    {
        name:'prtaxpercent'
    },

    {
        name:'rowTaxAmount'
    },

    {
        name:'orderrate'
    },

    {
        name:'desc', 
        convert:WtfGlobal.shortString
        },

        {
        name:'productmoved'
    },

    {
        name:'currencysymbol'
    },

    {
        name:'currencyrate'
    },

    {
        name: 'type'
    },

    {
        name: 'pid'
    },

    {
        name:'carryin'
    },

    {
        name:'approverremark'
    },

    {
        name:'permit'
    },

    {
        name:'invoicetype', 
        defValue:""
    },

    {
        name:'linkto'
    },

    {
        name:'customfield'
    },

    {
        name:'balanceQuantity'
    }
    ]);
    this.expandStoreUrl = "SalesOrderCMN/getBillingSalesOrderRows";
    
    if(this.isQuotation){
        this.expandStoreUrl = this.isCustomer? "ACCSalesOrderCMN/getQuotationVersionRows.do" : "ACCPurchaseOrderCMN/getQuotationVersionRows.do";
    }
    if(this.isPurchaseOrder){
        this.expandStoreUrl = "ACCPurchaseOrderCMN/getPurchaseOrderRows.do";
    }
    if(this.isRequisition){
        this.expandStoreUrl = "ACCPurchaseOrderCMN/getRequisitionRows.do";
    }
    if(this.isRFQ){
        this.expandStoreUrl = "ACCPurchaseOrderCMN/getRFQRows.do";
    }
    
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
        baseParams:{
            mode:this.isOrder?(this.isCustBill?53:43):(this.isCustBill?17:14),
            dtype : 'report'//Display type report/transaction, used for quotation
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.expandStore.on("beforeload", function(store){
        if(this.businessPerson=="Customer"){
            if(this.isQuotation){
                this.expandStoreUrl = "ACCSalesOrderCMN/getQuotationVersionRows.do";
            } else if(this.isRequisition){
                this.expandStoreUrl = "ACCPurchaseOrderCMN/getRequisitionRows.do";
            } else {
                this.expandStoreUrl = "ACC" + (this.isOrder?(this.withInvMode?"SalesOrderCMN/getBillingSalesOrderRows":"SalesOrderCMN/getSalesOrderRows"):(this.withInvMode?"InvoiceCMN/getBillingInvoiceRows":"InvoiceCMN/getInvoiceRows")) + ".do";
            }
            
        }else if(this.businessPerson=="Vendor"){
            if(this.isQuotation){
                this.expandStoreUrl = "ACCPurchaseOrderCMN/getQuotationVersionRows.do";
            } else if(this.isRequisition){
                this.expandStoreUrl = "ACCPurchaseOrderCMN/getRequisitionRows.do";
            } else if(this.isRFQ){
                this.expandStoreUrl = "ACCPurchaseOrderCMN/getRFQRows.do";
            } else if(this.isPurchaseOrder){
                this.expandStoreUrl = "ACCPurchaseOrderCMN/getPurchaseOrdersVersionRow.do";
            }else{
                this.expandStoreUrl = "ACC" + (this.isOrder?(this.withInvMode?"PurchaseOrderCMN/getBillingPurchaseOrderRows":"PurchaseOrderCMN/getPurchaseOrderRows"):(this.withInvMode?"GoodsReceiptCMN/getBillingGoodsReceiptRows":"GoodsReceiptCMN/getGoodsReceiptRows")) + ".do";
            }
            
        }
        store.proxy.conn.url = this.expandStoreUrl;
    }, this);
    
    this.GridRec = Wtf.data.Record.create ([
    {
        name:'billid'
    },

    {
        name:'journalentryid'
    },

    {
        name:'entryno'
    },

    {
        name:'billto'
    },

    {
        name:'companyid'
    },

    {
        name:'companyname'
    },

    {
        name:'discount'
    },

    {
        name:'currencysymbol'
    },

    {
        name:'orderamount'
    },

    {
        name:'isexpenseinv'
    },

    {
        name:'currencyid'
    },

    {
        name:'shipto'
    },

    {
        name:'mode'
    },

    {
        name:'billno'
    },

    {
        name:'date', 
        type:'date'
    },

    {
        name:'duedate', 
        type:'date'
    },

    {
        name:'shipdate', 
        type:'date'
    },

    {
        name:'personname'
    },

    {
        name:'personemail'
    },

    {
        name:'personid'
    },

    {
        name:'shipping'
    },

    {
        name:'othercharges'
    },

    {
        name:'partialinv',
        type:'boolean'
    },

    {
        name:'includeprotax',
        type:'boolean'
    },

    {
        name:'amount'
    },

    {
        name:'amountdue'
    },

    {
        name:'termdays'
    },

    {
        name:'termname'
    },

    {
        name:'incash',
        type:'boolean'
    },

    {
        name:'taxamount'
    },

    {
        name:'taxid'
    },

    {
        name:'orderamountwithTax'
    },

    {
        name:'taxincluded',
        type:'boolean'
    },

    {
        name:'taxname'
    },

    {
        name:'deleted'
    },

    {
        name:'termamount'
    },

    {
        name:'amountinbase'
    },

    {
        name:'memo'
    },

    {
        name:'createdby'
    },

    {
        name:'createdbyid'
    },

    {
        name:'externalcurrencyrate'
    },

    {
        name:'ispercentdiscount'
    },

    {
        name:'discountval'
    },

    {
        name:'crdraccid'
    },

    {
        name:'creditDays'
    },

    {
        name:'isRepeated'
    },

    {
        name:'porefno'
    },

    {
        name:'costcenterid'
    },

    {
        name:'costcenterName'
    },

    {
        name:'interval'
    },

    {
        name:'intervalType'
    },

    {
        name:'NoOfpost'
    }, 

    {
        name:'NoOfRemainpost'
    },  

    {
        name:'templateid'
    },

    {
        name:'templatename'
    },

    {
        name:'startDate', 
        type:'date'
    },

    {
        name:'nextDate', 
        type:'date'
    },

    {
        name:'expireDate', 
        type:'date'
    },

    {
        name:'repeateid'
    },

    {
        name:'status'
    },

    {
        name:'amountwithouttax'
    },

    {
        name:'amountwithouttaxinbase'
    },

    {
        name:'commission'
    },

    {
        name:'commissioninbase'
    },

    {
        name:'amountDueStatus'
    },

    {
        name:'salesPerson'
    },

    {
        name:'agent'
    },

    {
        name:'shipvia'
    },

    {
        name:'fob'
    },

    {
        name:'approvalstatus'
    },

    {
        name:'approvalstatusinfo'
    },

    {
        name:'approvalstatusint', 
        type:'int', 
        defaultValue:-1
    },

    {
        name:'archieve', 
        type:'int'
    },

    {
        name:'withoutinventory',
        type:'boolean'
    },

    {
        name:'isfavourite'
    },

    {
        name:'isCapitalGoodsAcquired'
    },

    {
        name:'importService'
    },

    {
        name:'othervendoremails'
    },

    {
        name:'termdetails'
    },

    {
        name:'approvestatuslevel'
    },// for requisition

    {
        name:'posttext'
    },

    {
        name:'isOpeningBalanceTransaction'
    },

    {
        name:'isNormalTransaction'
    },

    {
        name:'isreval'
    },

    {
        name:'islockQuantityflag'
    },

    {
        name:'isprinted'
    },

    {
        name:'validdate', 
        type:'date'
    },

    {
        name:'cashtransaction',
        type:'boolean'
    },

    {
        name:'shiplengthval'
    },

    {
        name:'invoicetype'
    },

    {
        name:'landedInvoiceID'
    },

    {
        name:'landedInvoiceNumber'
    },

    {
        name:'termdays'
    },

    {
        name:'billingAddress'
    },

    {
        name:'billingCountry'
    },

    {
        name:'billingState'
    },

    {
        name:'billingPostal'
    },

    {
        name:'billingEmail'
    },

    {
        name:'billingFax'
    },

    {
        name:'billingMobile'
    },

    {
        name:'billingPhone'
    },

    {
        name:'billingContactPerson'
    },

    {
        name:'billingContactPersonNumber'
    },
    {
        name:'billingContactPersonDesignation'
    },
    {
        name:'billingWebsite'
    },
    {
        name:'billingCounty'
    },
    {
        name:'billingCity'
    },

    {
        name:'billingAddressType'
    },

    {
        name:'shippingAddress'
    },

    {
        name:'shippingCountry'
    },

    {
        name:'shippingState'
    },

    {
        name:'shippingCounty'
    },

    {
        name:'shippingCity'
    },

    {
        name:'shippingEmail'
    },

    {
        name:'shippingFax'
    },

    {
        name:'shippingMobile'
    },

    {
        name:'shippingPhone'
    },

    {
        name:'shippingPostal'
    },

    {
        name:'shippingContactPersonNumber'
    },
    {
        name:'shippingContactPersonDesignation'
    },
    {
        name:'shippingWebsite'
    },
    {
        name:'shippingContactPerson'
    },

    {
        name:'shippingRoute'
    },

    {
        name:'shippingAddressType'
    },

    {
        name:'sequenceformatid'
    },

    {
        name:'gstIncluded'
    },

    {
        name:'lasteditedby'
    },

    {
        name:'salespersonname'
    },

    {
        name:'isConsignment'
    },

    {
        name:'custWarehouse'
    },

    {
        name:'deliveryTime'
    },

    {
        name:'getFullShippingAddress'
    },

    {
        name:'selfBilledInvoice'
    },

    {
        name:'RMCDApprovalNo'
    },

    {
        name:'fixedAssetInvoice'
    },

    {
        name:'fixedAssetLeaseInvoice'
    },

    //Below Fields are used only for Cash Sales and Purchase.

    {
        name:'methodid'
    },

    {
        name:'paymentname'
    },

    {
        name:'detailtype'
    },

    {
        name:'cardno'
    },

    {
        name:'nameoncard'
    },

    {
        name:'cardexpirydate', 
        type:'date'
    },

    {
        name:'cardtype'
    },

    {
        name:'cardrefno'
    },

    {
        name:'chequeno'
    },

    {
        name:'bankname'
    },

    {
        name:'chequedate', 
        type:'date'
    },

    {
        name:'chequedescription'
    },
    
    {
        name:"version"
    },
    {
        name: "termid"
    },
    {
        name:'supplierinvoiceno'
    }]);

    this.StoreUrl = "";
    this.RemoteSort= false;
    if(this.businessPerson=="Customer"){
        this.StoreUrl = "ACC" + (this.isOrder?("SalesOrderCMN/getSalesOrdersMerged"):("InvoiceCMN/getInvoicesMerged")) + ".do";
        this.RemoteSort = true;
    }else if(this.businessPerson=="Vendor"){
        this.StoreUrl = "ACC" + (this.isOrder?("PurchaseOrderCMN/getPurchaseOrdersMerged"):("GoodsReceiptCMN/getGoodsReceiptsMerged")) + ".do";
        this.RemoteSort = true;
    }
    if(this.isQuotation){
        this.StoreUrl = this.isCustomer? "ACCSalesOrderCMN/getVersionQuotations.do" : "ACCPurchaseOrderCMN/getVersionQuotations.do";
        this.RemoteSort = true;
    }
    if(this.isPurchaseOrder){
        this.StoreUrl = "ACCPurchaseOrderCMN/getPurchaseOrdersVersion.do";
        this.RemoteSort = true;
    }
    if(this.isRequisition){
        this.StoreUrl = "ACCPurchaseOrderCMN/getRequisitions.do";
        this.RemoteSort = false;
    } 
    if(this.isRFQ){
        this.StoreUrl = "ACCPurchaseOrderCMN/getRFQs.do";
        this.RemoteSort = false;
    }
    if(config.consolidateFlag && !this.isSalesCommissionStmt){
        this.Store = new Wtf.data.GroupingStore({
            url:this.StoreUrl,
            remoteSort: this.RemoteSort,
            baseParams:{
                costCenterId: this.costCenterId,
                deleted:false,
                nondeleted:false,
                cashonly:false,
                creditonly:false,
                CashAndInvoice:true,
                salesPersonFilterFlag:true,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isConsignment:this.isConsignment,
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                isprinted:false,
                versionid:this.versionid
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
                costCenterId: this.costCenterId,
                deleted:false,
                nondeleted:false,
                cashonly:false,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isConsignment:this.isConsignment,
                creditonly:false,
                CashAndInvoice:true,
                salesPersonFilterFlag:true,
                isfavourite:false,
                isprinted:false,
                versionid:this.versionid
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
            baseParams:{
                costCenterId: this.costCenterId,
                deleted:false,
                nondeleted:false,
                cashonly:false,
                creditonly:false,
                CashAndInvoice:true,
                salesPersonFilterFlag:true,
                consolidateFlag:config.consolidateFlag,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isConsignment:this.isConsignment,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                isprinted:false,
                report:true,
                versionid:this.versionid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.GridRec)
        });
    }
    var level_1_perm = false;
    var level_2_perm = false;    
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
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
    if(this.isRequisition ||this.isRFQ){
        dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],[5,WtfGlobal.getLocaleText("acc.field.FavouriteRecords")]);   
    }else if(this.isSalesCommissionStmt){
        dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],[1,cashType],[2,creditType]);        
    }else if(this.isOrder && this.businessPerson=="Customer"){
        dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],[5,WtfGlobal.getLocaleText("acc.field.FavouriteRecords")], [7, WtfGlobal.getLocaleText("acc.field.OutstandingSOs")],[8,WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")]);
    }else if(this.isOrder && this.businessPerson=="Vendor"){
        dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],[5,WtfGlobal.getLocaleText("acc.field.FavouriteRecords")], [7, WtfGlobal.getLocaleText("acc.field.OutstandingPOs")],[8,WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")]);
    }else{
        dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")],[1,cashType],[2,creditType],[3,WtfGlobal.getLocaleText("acc.rem.106")],[4,WtfGlobal.getLocaleText("acc.rem.107")],[5,WtfGlobal.getLocaleText("acc.field.FavouriteRecords")],[8,WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")]);
        dataArr.push([6, WtfGlobal.getLocaleText("acc.field.PendingPayments")]);
    }
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'typeid',
            type:'int'
        }, 'name'],
        data :dataArr
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
        listWidth:160,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
     
    this.summary = new Wtf.grid.GroupSummary();
    
    this.usersRec = new Wtf.data.Record.create([
    {
        name: 'id', 
        mapping:'userid'
    },

    {
        name: 'name', 
        mapping:'username'
    },

    {
        name: 'fname'
    },

    {
        name: 'lname'
    },

    {
        name: 'image'
    },

    {
        name: 'emailid'
    },

    {
        name: 'lastlogin',
        type: 'date'
    },

    {
        name: 'aboutuser'
    },

    {
        name: 'address'
    },

    {
        name: 'contactno'
    },

    {
        name: 'rolename'
    },

    {
        name: 'roleid'
    }
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
            this.Store.load({
                params:{
                    start:0,
                    limit:30,
                    pagingFlag:true
                }
            });
    }else{
        Wtf.MessageBox.hide();
    }
    },this);        

    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:100,
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
    
    this.termRec = new Wtf.data.Record.create([
    {
        name: 'termid'
    },

    {
        name: 'termname'
    },

    {
        name: 'termdays'
    }
    ]);

    this.termds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.termRec),
        url : "ACCTerm/getTerm.do",
        baseParams:{
            mode:91
        }
    });
    this.CreditTerm = new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.field.Term"),
        hiddenName: 'CreditTerm',
        name: 'CreditTerm',
        hidden: this.isCustBill,
        store: this.termds,
        record:Wtf.termRec,
        valueField: 'termid',
        displayField: 'termname',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.field.SelectaTerm"),
        width:100,
        listWidth:150
    });
    this.productRec = Wtf.data.Record.create([
    {
        name: 'productid'
    },

    {
        name: 'productname'
    },

    {
        name: 'desc'
    },

    {
        name: 'producttype'
    }
    ]);
//    if(Optimized_CompanyIds.indexOf(companyid)!= -1){
//        this.productStore = new Wtf.data.Store({
//            url: "ACCProduct/getProductsForCombo.do",
//            autoLoad:false,
//            baseParams: {
//                mode: 22,
//                onlyProduct:true
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            }, this.productRec)
//        });
//
//        this.productname = new Wtf.form.ComboBox({
//            fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
//            hiddenName: 'productid',
//            name: 'productid',
//            hidden: this.isCustBill,
//            store: this.productStore,
//            valueField: 'productid',
//            displayField: 'productname',
//            mode: 'remote',
//            hideTrigger:true,
//            typeAhead: true,
//            scope:this,
//            editable : true,
//            minChars : 2,
//            triggerAction: 'all',
//            hideLabel: true,
//            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
//            width:100,
//            listWidth:150
//        });
//        if (!this.isCustBill) {
//            this.termds.load();
//        }
//    }else
    {
        this.productStore = new Wtf.data.Store({
            url: "ACCProduct/getProductsForCombo.do",
            baseParams: {
                mode: 22,
                onlyProduct:true,
                isFixedAsset:this.isFixedAsset,
                includeBothFixedAssetAndProductFlag:this.isLeaseFixedAsset
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });

        this.productname = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
            hiddenName: 'productid',
            name: 'productid',
            hidden: this.isCustBill,
            store: this.productStore,
            valueField: 'productid',
            displayField: 'productname',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            width:100,
            listWidth:150
        });
        if (!this.isCustBill)
        {
            this.productStore.load();
            this.termds.load();
        }
    }    
    this.productStore.on("load", function() {
        var record = new Wtf.data.Record({
            productid: "",
            productname: "All Records"
        });
        this.productStore.insert(0, record);
        this.productname.setValue("");
    }, this);
    
    this.productCategoryRec = Wtf.data.Record.create([
    {
        name: 'id'
    },

    {
        name: 'name'
    },
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
        hidden: this.isCustBill,
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
    if (!this.isCustBill) {
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
    
    this.termds.on("load", function() {
        var record1 = new Wtf.data.Record({
            termid: "",
            termname: "All Records"
        });
        this.termds.insert(0, record1);
        this.CreditTerm.setValue("");
    }, this);
    
    this.personRec = new Wtf.data.Record.create([
    {
        name: 'accid'
    }, {
        name: 'accname'
    }, {
        name: 'acccode'
    },{
        name: 'taxId'
    },{
        name:'interstateparty'
    },{
        name:'cformapplicable'
    }
    ]);
    
    this.vendorAccStore = new Wtf.data.Store({
        url: "ACCVendor/getVendorsForCombo.do",
        baseParams: {
            mode: 2,
            group: 13,
            deleted: false,
            nondeleted: true,
            common: '1'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad: false
        }, this.personRec)
    });
    
    this.customerAccStore = new Wtf.data.Store({
        url: "ACCCustomer/getCustomersForCombo.do",
        baseParams: {
            mode: 2,
            group: 10,
            deleted: false,
            nondeleted: true,
            common: '1'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad: false
        }, this.personRec)
    });
    
    this.custVendCategoryRec = Wtf.data.Record.create([
    {
        name: 'id'
    },

    {
        name: 'name'
    },
    ]);
    this.custVendCategoryStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams: {
            mode: 112 ,
            groupid:this.isCustomer ? 7 : 8
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.custVendCategoryRec)
    });
    this.custVendCategory = new Wtf.form.ComboBox({
        fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.masterConfig.7") : WtfGlobal.getLocaleText("acc.masterConfig.8") ,
        hiddenName: 'id',
        name: 'id',
        store: this.custVendCategoryStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectacategory"),
        width:100,
        listWidth:150
    });
    
    this.custVendCategoryStore.on("load", function() {
        var record = new Wtf.data.Record({
            id: "All",
            name: "All Records"
        });
        this.custVendCategoryStore.insert(0, record);
        this.custVendCategory.setValue("All");
    }, this);
    
    this.custVendCategory.on("select", function(cmb, rec, ind) {
        this.person="";
        this.filtercustid = rec.data.id;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.customerCategoryid = this.filtercustid;
        this.Store.baseParams = currentBaseParams;
    }, this);
    
    this.custVendCategoryStore.load();

    this.custmerCmb = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.cust"),
        hiddenName: 'customerid',
        id: "customer" + this.id,
        store: this.customerAccStore,
        valueField: 'accid',
        displayField: 'accname',
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        allowBlank: false,
        hirarchical: true,
        emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
        mode: 'local',
        typeAheadDelay:30000,
        minChars:1,
        typeAhead: true,
        forceSelection: true,
        selectOnFocus: true,
        anchor: "50%",
        triggerAction: 'all',
        scope: this,
        width:100
    });
    this.vendorCMB = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.ven"),
        hiddenName: "vendor",
        id: "vendor" + this.id,
        store: this.vendorAccStore,
        valueField: 'accid',
        displayField: 'accname',
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
        allowBlank: false,
        hirarchical: true,
        emptyText: WtfGlobal.getLocaleText("acc.msgbox.19"),
        mode: 'local',
        typeAheadDelay:30000,
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        minChars:1,
        typeAhead: true,
        forceSelection: true,
        selectOnFocus: true,
        anchor: "50%",
        triggerAction: 'all',
        scope: this,
        width:100
    });
    if (this.businessPerson == "Customer")
        this.customerAccStore.load();
    else
        this.vendorAccStore.load();

    this.customerAccStore.on("load", function() {
        var record = new Wtf.data.Record({
            accid: "",
            accname: "All Records"
        });
        this.customerAccStore.insert(0, record);
        this.custmerCmb.setValue("");
        if(this.person!="" && this.person!=undefined) 
            this.custmerCmb.setValue(this.person);
    }, this);
    this.vendorAccStore.on("load", function() {
        var record = new Wtf.data.Record({
            accid: "",
            accname: "All Records"
        });
        this.vendorAccStore.insert(0, record);
        this.vendorCMB.setValue("");
        if(this.person!="" && this.person!=undefined) 
            this.vendorCMB.setValue(this.person);
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
    
    this.custmerCmb.on("select", function(cmb, rec, ind) {
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
    this.submitBttn.on("click", this.submitHandler, this);
    this.tbar2 = new Array();
    this.tbar3=new Array();
    if(this.isSalesCommissionStmt){ 
        this.tbar2.push(WtfGlobal.getLocaleText("acc.field.SelectSalesPerson1"),this.users,'-');
    }
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.from"));
    this.tbar2.push(this.startDate);
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.to"));
    this.tbar2.push(this.endDate);
    
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
        if(!config.isOrder&&!this.isQuotation && !this.isRequisition && !this.getRFQs && !this.isRFQ && !this.isPurchaseOrder){// For invoice & Vendor Invoice show 'cost center' and 'view' filters in 2nd tbar applied for grid
            this.tbar2.push("-",WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
            this.tbar2.push("-",WtfGlobal.getLocaleText("acc.field.PaymentTerms"), this.CreditTerm );
        }
    }
    if (!this.isCustBill) {
        this.tbar2.push("-",WtfGlobal.getLocaleText("acc.invReport.prod"), this.productname);
        this.tbar2.push("-",WtfGlobal.getLocaleText("acc.masterConfig.19"),this.productCategory);
    }
   
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
        var label = this.isCustomer ? WtfGlobal.getLocaleText("acc.masterConfig.7") : WtfGlobal.getLocaleText("acc.masterConfig.8") ;
        this.tbar3.push("-",label,this.custVendCategory);
    }
    if(this.isRequisition)
    {
        this.tbar2.push("-");
        this.tbar2.push(this.submitBttn);
    }
    else
    {
        this.tbar3.push("-");
        this.tbar3.push(this.submitBttn);  
    }
    
    this.tbar3.push("-");
    if(config.extraFilters == undefined){
        if(!this.isQuotation &&!this.isFixedAsset && !this.isLeaseFixedAsset && !this.isConsignment && !this.isPurchaseOrder){
            this.tbar3.push("->");
            this.tbar3.push("&nbsp;View", this.typeEditor);
        }
    }

    this.emptytext1=WtfGlobal.getLocaleText("acc.common.norec");
    this.emptytext2=WtfGlobal.getLocaleText("acc.common.norec") ;
    this.emptytext3=WtfGlobal.getLocaleText("acc.common.norec");
    this.emptytext4=WtfGlobal.getLocaleText("acc.common.norec");
    this.emptytext5=WtfGlobal.getLocaleText("acc.common.norec");

    if (this.isOutstanding){
        this.emptytext1 = this.emptytext2 = this.emptytext3  = '<div style="font-size:15px; text-align:center; color:#CCCCCC; font-weight:bold; margin-top:8%;">No Outstanding Record Available.<br></div>';
    }
    if(this.isFixedAsset || this.isLeaseFixedAsset || this.isConsignment){
        this.emptytext2 = '';
        this.emptytext3 = '';
        this.emptytext1 = WtfGlobal.getLocaleText("acc.common.norec");
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
        emptyText:(config.isQuotation?(this.isCustomer? this.emptytext3: this.emptytext4) : (this.emptytext1+(config.isOrder?"":"<br>"+this.emptytext2)))
    }):{
        forceFit:false,
        emptyText: config.isRFQ ? WtfGlobal.getLocaleText("acc.common.norec") : config.isRequisition ? this.emptytext5 : (config.isQuotation?(this.isCustomer? this.emptytext3: this.emptytext4) : (this.emptytext1+(config.isOrder?"":"<br>"+this.emptytext2)))
            
    };
        
    this.gridColumnModelArr=[];
    this.gridColumnModelArr.push(this.sm,this.expander,{
        header: '',
        hidden:(this.isSalesCommissionStmt)?true:false,
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
        header:" ",
        hidden:true,
        dataIndex:'billid'
    },{
        header:WtfGlobal.getLocaleText("acc.field.Company"),  
        dataIndex:'companyname',
        width:20,
        pdfwidth:150,
        sortable: this.RemoteSort,
        hidden:true
    },{
        header:this.label+" "+WtfGlobal.getLocaleText("erp.field.Version"),
        dataIndex:'version',
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.deletedRenderer,
        sortable:this.RemoteSort
    },{
        header:this.label+" "+WtfGlobal.getLocaleText("acc.cn.9"),
        dataIndex:'billno',
        width:150,
        pdfwidth:75,
        sortable:this.RemoteSort,
        renderer:(config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.jeno"),  //"Journal Entry No",
        dataIndex:'entryno',
        hidden:this.isOrder||this.isQuotation ||this.isRFQ || this.isPurchaseOrder,
        sortable: this.RemoteSort,
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header:this.label+" "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
        dataIndex:'date',
        align:'center',
        sortable: this.RemoteSort,
        width:150,
        pdfwidth:80,
        renderer:WtfGlobal.onlyDateDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.due"),  //"Due Date",
        dataIndex:'duedate',
        sortable:this.RemoteSort,
        align:'center',
        width:150,
        pdfwidth:80,
        renderer:WtfGlobal.onlyDateDeletedRenderer,
        hidden:(this.isQuotation||this.isSalesCommissionStmt || this.isPurchaseOrder)
    },{
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
        sortable:  this.RemoteSort,
        hidden:this.isSalesCommissionStmt || this.isRequisition
    },{
        header:WtfGlobal.getLocaleText("acc.pmList.gridPaymentMethod"),  // "Payment Method",
        pdfwidth:75,
        width:150,            
        dataIndex:'paymentname',
        sortable:  this.RemoteSort,
        hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ || this.isOrder||this.isPurchaseOrder,
        renderer:WtfGlobal.deletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.salesPerson"),  // "Sales Person",     
        pdfwidth:75,
        width:150,
        dataIndex:'salespersonname',
        hidden: !(this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.modulid == Wtf.Acc_Purchase_Order_ModuleId)
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.discount"),  //"Discount",
        dataIndex:'discount',
        align:'right',
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
        hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.taxName"),  //"Tax Name",
        dataIndex:'taxname',
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.deletedRenderer,
        hidden:(!this.isOrder||this.isSalesCommissionStmt||this.isRequisition || this.isRFQ|| this.isPurchaseOrder || (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST))// hide if company is malaysian and GST is not enabled for it)
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.taxAmt"),  //"Tax Amount",
        dataIndex:'taxamount',
        align:'right',
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
        hidden:this.isSalesCommissionStmt || this.isRequisition || this.isRFQ || (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST)// hide if company is malaysian and GST is not enabled for it
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.terms"),  //"Terms Amount"
        dataIndex:'termamount',
        align:'right',
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
        hidden:this.isSalesCommissionStmt|| this.isRequisition|| this.isRFQ || this.isPurchaseOrder
    },{
        header:this.isRequisition ? WtfGlobal.getLocaleText("acc.field.EstimatedCost") : WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),  //"Total Amount",
        align:'right',
        dataIndex:((this.isOrder||this.isQuotation)?'orderamountwithTax':'amount'),
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
        hidden:this.isSalesCommissionStmt || this.isRFQ
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome") + " ("+WtfGlobal.getCurrencyName()+")",  //"Total Amount (In Home Currency)",
        align:'right',
        hidden:this.isQuotation?false:this.isSalesCommissionStmt || this.isRFQ || this.isPurchaseOrder, //this.quotation?false:this.isOrder,
        dataIndex:'amountinbase',
        width:150,
        pdfwidth:75,
        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer            
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.amtDue"),  //"Amount Due",
        dataIndex:'amountdue',
        align:'right',
        hidden:this.isOrder||this.isQuotation||this.isSalesCommissionStmt ||this.isRFQ || this.isPurchaseOrder,
        width:150,
        pdfwidth:75,
        renderer:(this.isOrder?WtfGlobal.currencyRendererDeletedSymbol:WtfGlobal.withoutRateCurrencyDeletedSymbol)            
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.status"),
        dataIndex:'amountDueStatus',
        width:150,
        pdfwidth:75,
        hidden:this.isRFQ || !this.isSalesCommissionStmt,
        summaryRenderer:function(){
            return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }
    },{
        header:WtfGlobal.getLocaleText("acc.field.InvoiceAmount(ApplicableforCommission)"),  //"Invoice Amount (applicable for Commission)",
        align:'right',
        hidden:!this.isSalesCommissionStmt,
        dataIndex:'amountwithouttax',
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol         
    },{
        header: WtfGlobal.getLocaleText("acc.field.InvoiceAmount(ApplicableforCommission)inBaseCurrency")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Invoice Amount (applicable for Commission)",
        align:'right',
        hidden:!this.isSalesCommissionStmt,
        dataIndex:'amountwithouttaxinbase',
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.currencyDeletedRenderer,           
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
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
    },{
        header:WtfGlobal.getLocaleText("acc.field.CommissionAmountinBaseCurrency"),  //"Commission Amount",
        dataIndex:'commissioninbase',
        align:'right',
        hidden:!this.isSalesCommissionStmt,
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.currencyDeletedRenderer,
        summaryType:'sum',
        summaryRenderer: function(value, m, rec) {
            return WtfGlobal.currencySummaryRenderer(value, m, rec);
        }
    },{
        header:Wtf.account.companyAccountPref.descriptionType,  //"Memo",
        dataIndex:'memo',
        hidden:this.isSalesCommissionStmt,
        renderer : function(val){
            return "<div wtf:qtip=\""
            +"<div style=\'word-wrap: break-word; width:200px;text-wrap:unrestricted;\'>"+val+"</div>"
            + "\" wtf:qtitle='"
            +   "'>" + Wtf.util.Format.ellipsis(val,60) + "</div>";
        },
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
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.status"),  //"Status",
        dataIndex:'status',
        hidden:this.isRFQ || !this.isOrder || this.isQuotation||this.isSalesCommissionStmt,
        renderer:WtfGlobal.deletedRenderer,
        width:150,
        pdfwidth:100
    },{
        header:WtfGlobal.getLocaleText("acc.field.Approval") +WtfGlobal.getLocaleText("acc.invoiceList.status"),  //"Status",
        dataIndex:'approvalstatus',
        hidden:this.isRequisition || this.isQuotation || this.isPurchaseOrder,
        renderer:function(value, m, rec){
            var retString = '<span wtf:qtip="'+value+'">'+value+'</span>';
            var approvalstatusint = rec.data.approvalstatusint;
            if(approvalstatusint == 1) {
                if(level_1_perm) {
                    retString = '<span style="color:green;" wtf:qtip="'+value+'">'+value+'</span>';
                }
            } else if(approvalstatusint == 2){
                if(level_2_perm) {
                    retString = '<span style="color:green;" wtf:qtip="'+value+'">'+value+'</span>';
                }
            }                
            if(rec.data.deleted)
                retString='<del>'+retString+'</del>';
            return retString;
        },
        width:150,
        pdfwidth:100
    },{
        header:WtfGlobal.getLocaleText("acc.field.Approval") +WtfGlobal.getLocaleText("acc.invoiceList.status"),  //"Status",
        dataIndex:'approvalstatusinfo',
        hidden:this.isRequisition || !this.isQuotation,// Show column in Pending Approval Report for Quotation and Requisition module
        renderer:WtfGlobal.deletedRenderer,
        width:150,
        pdfwidth:100
    },{
        header:WtfGlobal.getLocaleText("acc.nee.69"),
        dataIndex:'createdby',
        width:150,
        hidden:(this.isRFQ || !this.isOrder || !this.isCustomer || this.isQuotation||this.isSalesCommissionStmt) ? true : false
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"),  //"Attach Documents",
        dataIndex:'attachdoc',
        width:150,
        align:'center',
        hidden:(this.isQuotation||this.isSalesCommissionStmt || this.isPurchaseOrder) ? true : false ,
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
        dataIndex:'attachments',
        width:150,
        hidden:(this.isQuotation||this.isSalesCommissionStmt|| this.isPurchaseOrder) ? true : false ,
        renderer : Wtf.DownloadLink.createDelegate(this)
    },{
        header:WtfGlobal.getLocaleText("acc.field.LastEditedBy"),
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.deletedRenderer,
        dataIndex:'lasteditedby',
        hidden:this.isSalesCommissionStmt || this.isRequisition
    });
         
    // appening custom columns
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[this.moduleid],true);

    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        id:"gridmsg"+config.helpmodeid+config.id,
        border:false,
        sm:this.sm,
        tbar: this.tbar2,
        layout:'fit',
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
    this.grid.getColumnModel().defaultSortable = false;
    var colModelArray = GlobalColumnModelForReports[this.moduleid];
    WtfGlobal.updateStoreConfigStringDate(colModelArray, this.Store);
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: (this.isSalesCommissionStmt != undefined && this.isSalesCommissionStmt ==true ) ?  Wtf.Acc_Invoice_ModuleId:this.moduleid,
        advSearch: false
    });
    
    this.grid.on('render',
        function(){
            new Wtf.Toolbar({
                renderTo: this.grid.tbar,
                items:  this.tbar3
            });
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
        hidden:false,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
        
    var btnArr=[];
    var bottombtnArr=[];
    var tranType=null;
    if(this.isCustBill)
        tranType=config.isCustomer?(config.isOrder?Wtf.autoNum.BillingSalesOrder:Wtf.autoNum.BillingInvoice):(config.isOrder?Wtf.autoNum.BillingPurchaseOrder:Wtf.autoNum.BillingGoodsReceipt);
    else
        tranType=config.isCustomer?(config.isOrder?Wtf.autoNum.SalesOrder:Wtf.autoNum.Invoice):(config.isOrder?Wtf.autoNum.PurchaseOrder:Wtf.autoNum.GoodsReceipt);
    if(this.isQuotation) {
        tranType= this.isCustomer ? Wtf.autoNum.CustomerQuoationVersion : Wtf.autoNum.VendorQuotationVersion;
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
        if(!WtfGlobal.EnableDisable(this.uPermType, this.editPermType) && Wtf.account.companyAccountPref.editTransaction && (this.isRequisition)){				//!this.isOrder&&
            btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
                tooltip :(this.isOrder)?WtfGlobal.getLocaleText("acc.invoiceList.editO"):WtfGlobal.getLocaleText("eveacc.invoiceList.editQ"),  //acc.invoiceList.editO:-'Allows you to edit Order.':acc.invoiceList.editQ:-'Allows you to edit Quotation.'
                id: 'btnEdit' + this.id,
                scope: this,
                hidden:true,
                iconCls :getButtonIconCls(Wtf.etype.edit),
                disabled :true
            }));
            this.editBttn.on('click',(this.isOrder && !this.isRequisition)?this.editOrderTransaction.createDelegate(this,[false]):this.editTransaction.createDelegate(this,[false]),this);
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.copyPermType)){
            btnArr.push(this.copyInvBttn=new Wtf.Toolbar.Button({
                text:(this.isOrder || this.isQuotation)?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.invoiceList.copyInv"),  //'Copy Invoice',
                tooltip :(this.isOrder || this.isQuotation)?WtfGlobal.getLocaleText("acc.field.CopyRecord"):WtfGlobal.getLocaleText("acc.invoiceList.copyInvTT"),  //'Allows you to Copy Invoice.',
                id: 'btnCopyInv' + this.id,
                scope: this,
                hidden:true,
                iconCls :getButtonIconCls(Wtf.etype.copy),
                disabled :true
            }));
            this.copyInvBttn.on('click',(this.isQuotation?(this.copyQuotation.createDelegate(this,[true])):(this.isOrder?this.editOrderTransaction.createDelegate(this,[true]):this.editTransaction.createDelegate(this,[true]))),this);
        }
        var deletebtnArray=[];
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction){
            deletebtnArray.push(this.deleteTrans=new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.rem.7")+' '+this.label,
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.rem.6"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                hidden:!(this.isQuotation),
                disabled :true,
                handler:this.handleDelete.createDelegate(this,this.del=["del"])
            }))
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction){
            deletebtnArray.push(this.deleteTransperm=new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.rem.7")+' '+this.label+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.rem.6")+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                hidden: config.consolidateFlag ||this.isSalesCommissionStmt ||this.isRFQ||!(this.isQuotation), 
                disabled :true,
                handler:this.handleDelete.createDelegate(this,this.del=["delp"])
            }))
        }
        if(deletebtnArray.length>0) {
            btnArr.push(this.deleteMenu = new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"), 
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"), 
                iconCls:getButtonIconCls(Wtf.etype.deletebutton),
                hidden: !(this.isQuotation), 
                menu:deletebtnArray
            }));
        }
        if(this.isRequisition) {
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.editrfq)) {
                btnArr.push(this.RFQBtn=new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.field.InitiateRFQ"),
                    scope: this,
                    tooltip:WtfGlobal.getLocaleText("acc.field.InitiateRFQ"),
                    disabled :true,
                    iconCls:'accountingbase initiaterfq',
                    handler: function() {
                        callRequestForQuotation(false,this.PR_IDS, this.PR_MEMOS)
                    }
                }))
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.viewrfq)) {
                btnArr.push(this.RFQListBtn=new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.field.RFQList"),
                    scope: this,
                    tooltip:WtfGlobal.getLocaleText("acc.field.RFQList"),
                    iconCls:'accountingbase rfqlist',
                    enabled :true,
                    handler: function() {
                        callReqForQuotationList(false,this.PR_IDS)
                    }
                }))
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.createquotation)) {
                btnArr.push(this.RFQVendorBtn=new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.field.RecordVendorQuotation"),
                    scope: this,
                    tooltip:WtfGlobal.getLocaleText("acc.field.RecordVendorQuotation"),
                    iconCls:'accountingbase purchaseorder',
                    disabled :true,
                    handler: function() {
                        callVendorQuotation(true, 'vendorquotation', this.grid.getSelectionModel().getSelected(), undefined, this.PR_IDS,true);
                    }
                }))
            }
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
            if(!this.isRFQ && !this.isQuotation&&!this.isOrder&&!WtfGlobal.EnableDisable(this.uPaymentPermType, this.createPaymentPermType)){
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
            if(!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType)){
                btnArr.push(this.email=new Wtf.Toolbar.Button({
                    text:WtfGlobal.getLocaleText("acc.common.email"),  // "Email",
                    tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
                    scope: this,
                    iconCls : "accountingbase financialreport",
                    disabled : true,
                    hidden: true,
                    handler : this.sendMail
                }));
            }
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

        if(this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid==Wtf.Acc_Sales_Order_ModuleId) {
            if(!WtfGlobal.EnableDisable(this.uPermType, this.recurringPermType)){
                btnArr.push(this.RepeateInvoice=new Wtf.Toolbar.Button({
                    text:rbText,
                    tooltip :rbToolTip,
                    scope: this,
                    iconCls : getButtonIconCls(Wtf.etype.copy),
                    disabled : true,
                    hidden: config.consolidateFlag ||this.isSalesCommissionStmt || this.isFixedAsset || this.reportbtnshwFlag || this.isConsignment ,
                    handler : this.repeateInvoiceHandler
                }));
            }
        }
    } 
    this.newtranType=tranType;

        this.exportButton=new Wtf.exportButton({
            obj:this,
            isEntrylevel:false,
            id:"exportReports"+config.helpmodeid+this.id,   //+config.id,   //added this.id to avoid dislocation of Export button option i.e. CSV & PDF
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            hidden:true,
            menuItem:{
                csv:true,
                pdf:true
            },
            get:tranType
        });
        this.exportButton.setParams({
            costCenterId : this.costCenter.getValue(),
            productid : this.productname.getValue(),
            newcustomerid : this.custmerCmb.getValue(),
            newvendorid : this.vendorCMB.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            archieve : this.archieveCombo?this.archieveCombo.getValue() : 0
        });

        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            hidden:true,
            label:(this.isSalesCommissionStmt)?WtfGlobal.getLocaleText("acc.field.SalesCommissionStatement"):config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoSO"):WtfGlobal.getLocaleText("acc.accPref.autoInvoice")):(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoPO"):WtfGlobal.getLocaleText("acc.agedPay.venInv")),
            params:{
                isexpenseinv:this.isexpenseinv,
                name:(this.isSalesCommissionStmt)?"Sales Commission Statement":(config.isQuotation?WtfGlobal.getLocaleText("acc.qnList.tabTitle"):(config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.soList.tabTitle"):WtfGlobal.getLocaleText("acc.invoiceList.tabtitle")):(config.isOrder?WtfGlobal.getLocaleText("acc.poList.tabTitle"):WtfGlobal.getLocaleText("acc.grList.tabTitle"))))
                },
            menuItem:{
                print:true
            },
            get:tranType
        });
          
        this.printButton.setParams({
            costCenterId : this.costCenter.getValue(),
            productid : this.productname.getValue(),
            newcustomerid : this.custmerCmb.getValue(),
            newvendorid : this.vendorCMB.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        });
        
    this.singlePrint=new Wtf.exportButton({
        obj:this,
        id:"printReports"+config.helpmodeid+config.id,
        iconCls: 'pwnd exportpdfsingle',
        text: WtfGlobal.getLocaleText("acc.rem.39.single"),// + " "+ singlePDFtext,
        tooltip :WtfGlobal.getLocaleText("acc.rem.39.singletooltip"),  //'Export selected record(s)'
        disabled :true,
        menuItem:{
            rowPdf:(this.isSalesCommissionStmt)?false:true,
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
        enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
        archieve : this.archieveCombo?this.archieveCombo.getValue() : 0
    });
         
    this.singleRowPrint=new Wtf.exportButton({
        obj:this,
        id:"printSingleRecord"+config.helpmodeid+config.id,
        iconCls: 'pwnd printButtonIcon',
        text: WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
        disabled :true,
        hidden:true,
        menuItem:{
            rowPrint:(this.isSalesCommissionStmt)?false:true
            },
        get:tranType,
        moduleid:config.moduleId
    }); 
     
    this.singleRowPrint.setParams({
        costCenterId : this.costCenter.getValue(),
        productid : this.productname.getValue(),
        newcustomerid : this.custmerCmb.getValue(),
        newvendorid : this.vendorCMB.getValue(),
        startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
        enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
        archieve : this.archieveCombo?this.archieveCombo.getValue() : 0
    });
     
    if((this.isOrder && this.businessPerson=="Customer" && !this.isCustBill && !this.isLeaseFixedAsset && !this.isConsignment)){				//!this.isOrder&&
        btnArr.push(this.POfromSOBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.invoiceList.gpo"),         //Generate PO
            tooltip :WtfGlobal.getLocaleText("acc.invoiceList.gpott"),  //Generate PO for selected SO.
            scope: this,                
            iconCls :getButtonIconCls(Wtf.etype.edit),                
            disabled :true,
            hidden: true,
            handler : this.createPOfromSO                
        }));    
    }
    if(this.id!="PurchaseRequisitionList" && (this.isOrder && this.businessPerson=="Vendor" && !this.isCustBill)){//!this.isOrder&&
        btnArr.push(this.SOfromPOBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.invoiceList.gso"),         //Generate SO
            tooltip :WtfGlobal.getLocaleText("acc.invoiceList.gsott"),  //Generate SO for selected PO.
            scope: this,                
            iconCls :getButtonIconCls(Wtf.etype.edit),                
            disabled :true,
            hidden: true,
            handler : this.createSOfromPO                
        }));
    }
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" "+this.label,
        width: 150,
        id:"quickSearch"+config.helpmodeid+config.id,
        field: 'billno',
        Store:this.Store
    })
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        //        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden:true,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });

    this.linkinfoViewBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.LinkInformationDetails"),     //button for showing link information
        scope: this,
        hidden: this.isRequisition || this.isRFQ || this.isQuotation||this.isSalesCommissionStmt,  //shown in SO/PO
        disabled : true,
        tooltip: WtfGlobal.getLocaleText("acc.field.LinkInformationDetails"),
        handler:function(){linkinfo(undefined,undefined,undefined,undefined,undefined,undefined,undefined,undefined,this)},
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
    
    this.agedDetailsBtn.on('click',this.showAgedDetails.createDelegate(this)); 
//    bottombtnArr.push('-',this.linkinfoViewBtn);
//    bottombtnArr.push('-',this.agedDetailsBtn);
//    bottombtnArr.push('-',this.exportButton);
//    bottombtnArr.push('-', this.printButton);
    bottombtnArr.push('-', this.singlePrint);
//    bottombtnArr.push('-', this.singleRowPrint);
    this.tbar1 = new Array();
    this.tbar1.push(this.quickPanelSearch, this.resetBttn,this.AdvanceSearchBtn, btnArr);
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
        if(config.isOrder && !this.isQuotation && !this.isRequisition && !this.isRFQ){ // For sales/purchase order show 'cost center' filter in main tbar applied for panel
            this.tbar2.push("-", WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
        }
    } else if(config.extraFilters != undefined ){//Invoice Report View - Add type filter in main tbar
        if(!config.isOrder){
            this.tbar3.push("&nbsp;View", this.typeEditor);
        }
    }
    if (this.isPurchaseOrder) {
        bottombtnArr = [];
    }
    
    this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [this.objsearchComponent
        , {
            region: 'center',
            layout: 'fit',
            border: false,
            items: [this.grid],
            tbar: this.tbar1,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.Store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id: "pPageSize_" + this.id
                    }),
                items : bottombtnArr  //added utton of link info button
            })
        }]
    }); 
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });    
    this.Store.on('beforeload',function(s,o){      
        if(this.moduleid==20|| this.moduleid==Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid==20 ||this.moduleid==2||this.moduleid==18||this.moduleid==6||this.moduleid==22||this.moduleid==23||this.moduleid==32||this.moduleid==33 || this.moduleid== Wtf.Acc_Lease_Order){
            
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getCustomizedReportFields.do",
                params: {
                    flag: 34,
                    moduleid:this.moduleid,
                    reportId:1,
                    isFormField:false,
                    isLineField:false
                }
            }, this, function(action, response){
                if(action.success && action.data!=undefined){
                    this.customizeData=action.data;
                    var cm=this.grid.getColumnModel();
                    for(var i=0;i<action.data.length;i++){
                        for(var j=0;j<cm.config.length;j++){
                            if(cm.config[j].header==action.data[i].fieldDataIndex||(cm.config[j].dataIndex==action.data[i].fieldDataIndex && cm.config[j].header==action.data[i].fieldname)){
                                cm.setHidden(j,action.data[i].hidecol);
                            }
   
                        }
                    }
                    this.grid.reconfigure( s, cm);
                } else {
                //                    Wtf.Msg.alert('Status', action.msg);
                }
            },function() {
                });
        
        }
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
        currentBaseParams.archieve=this.archieveCombo?this.archieveCombo.getValue() : 0;
        currentBaseParams.costCenterId = this.costCenter.getValue();
        currentBaseParams.productid = this.productname.getValue();
        currentBaseParams.productCategoryid = this.productCategory.getValue();
        currentBaseParams.termid = this.CreditTerm.getValue();
        currentBaseParams.newcustomerid = this.custmerCmb.getValue(),
        currentBaseParams.newvendorid = this.vendorCMB.getValue(),
        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        currentBaseParams.isOpeningBalanceInvoices=this.isOpeningBalanceInvoices;
        currentBaseParams.isOpeningBalanceOrder=this.isOpeningBalanceOrder;
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
        if(this.isSalesCommissionStmt){
            currentBaseParams.isSalesCommissionStmt = this.isSalesCommissionStmt;
            currentBaseParams.userid = this.users.getValue();
            currentBaseParams.nondeleted=true;
        } 
        this.Store.baseParams=currentBaseParams;
        
    },this);
    this.loadParmStore();
    this.typeEditor.on('select',this.loadTypeStore,this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    this.Store.on('load',this.expandRow, this);
    this.Store.on('load',this.hideLoading, this);
    this.Store.on('loadexception',this.hideLoading, this);
    Wtf.account.VersionListPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    this.grid.on('cellclick',this.onCellClick, this);
}
Wtf.extend(Wtf.account.VersionListPanel,Wtf.Panel,{
    submitHandler : function(){
        this.loadStore();
    },
    
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    loadTypeStore:function(a,rec){
        if(this.startDate)
            this.startDate.setValue(WtfGlobal.getDates(true));
        if(this.endDate)
            this.endDate.setValue(WtfGlobal.getDates(false));
        this.showAllBarButtons();
        this.isOpeningBalanceInvoices=false;
        this.isOpeningBalanceOrder=false;
        this.cashonly=false;
        this.creditonly=false;
        this.deleted=false;
        this.nondeleted=false;
        this.isfavourite=false;
        this.outstandingreportflag=false;
        this.outstandingreportflag=undefined;
        this.ispendingpayment=false;
        this.index=rec.data.typeid;
        this.costCenter.enable();
        this.CreditTerm.enable();
        this.productname.enable();
        this.productCategory.enable();
        this.hideAllBarButtons();
        if(this.index==1)
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
        this.sm.singleSelect = true;
        if(this.index==5){
            this.isfavourite=true;
            if(this.copyInvBttn)this.copyInvBttn.enable();
        } else if(this.index==6){
            this.ispendingpayment=true;
            this.sm.singleSelect = false;
            if(this.copyInvBttn)this.copyInvBttn.enable();
        } else if (this.index==7){
            this.isOutstanding = true;
            if(this.copyInvBttn)this.copyInvBttn.disable();
        } else if (this.index==8){
            if(this.startDate)
                this.startDate.setValue(this.getLastFinancialYRStartDate(true));
            if(this.endDate)
                this.endDate.setValue(this.getFinancialYRStartDatesMinOne(false));

            if(this.isOrder){
                this.isOpeningBalanceOrder=true;
            }else{
                this.isOpeningBalanceInvoices=true;
                if(this.copyInvBttn)this.copyInvBttn.enable();
                this.hideAllBarButtons();
                this.costCenter.reset();
                this.CreditTerm.reset();
                this.productname.reset();
                this.productCategory.reset();
                this.costCenter.disable();
                this.CreditTerm.disable();
                this.productname.disable();
                this.productCategory.disable();                
            }
        } 
        this.invID = "";
        this.exponly = false;
        this.Store.on('load',this.storeloaded,this);
        this.loadStore();
        WtfComMsgBox(29,4,true);

    },
    
    getFinancialYRStartDatesMinOne:function(start){
        var d=Wtf.serverDate;
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
    
    getLastFinancialYRStartDate:function(start){
        var d=Wtf.serverDate;
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd.add(Date.YEAR, -1);
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
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
        if(this.email){
            this.email.hide();
        }
        if(this.singlePrint){
            this.singlePrint.hide();
        }
        if(this.deleteTransperm){
            this.deleteTransperm.hide();
        }
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
        if(this.email){
            this.email.show();
        }
        if(this.singlePrint){
            this.singlePrint.show();
        }
        if(this.AdvanceSearchBtn){
            this.AdvanceSearchBtn.show();
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
                callCreateNewButtonFunction(23)
            }
        } else if(this.isRequisition) {
            callPurchaseReq();
        } else if(this.isRFQ ){
            callRequestForQuotation(false);
        } else if(this.isOrder) {
            if(this.isCustomer) {
                callCreateNewButtonFunction(20);
            }
            else if(!this.isCustomer) {
                callCreateNewButtonFunction(18);
            }   
        } else  { 
            callSalesOrPurchaseType(this.isCustomer);
        }
    },
    enableDisableButtons:function(){
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            if(this.deleteTrans){
                this.deleteTrans.enable();
            }
            if(this.deleteTransperm){
                this.deleteTransperm.enable();
            }
        }
        var arr=this.grid.getSelectionModel().getSelections();
        if(arr.length==0&&!WtfGlobal.EnableDisable(this.uPermType,this.removePermType)){
            if(this.deleteTrans){
                this.deleteTrans.disable();
            }
            if(this.deleteTransperm){
                this.deleteTransperm.disable();
            }
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            for(var i=0;i<arr.length;i++){
                if(arr[i]&&arr[i].data.deleted){
                    if(this.deleteTrans){
                        this.deleteTrans.disable();
                    }
                    if(this.deleteTransperm){
                        this.deleteTransperm.enable();
                    }
                }
            }
        }
        if(this.isRequisition) {
            var arr = this.sm.getSelections();
            this.PR_IDS = [];
            this.PR_MEMOS='';
            if(arr.length==0) {
                if(this.RFQBtn)this.RFQBtn.disable();
                if(this.RFQVendorBtn)this.RFQVendorBtn.disable();
            } else {
                if(this.RFQBtn)this.RFQBtn.enable();
                if(this.RFQVendorBtn)this.RFQVendorBtn.enable();
            }
            for(var cnt=0;cnt<arr.length;cnt++){
                if(arr[cnt]|| arr[cnt].data.deleted) {
                    if(this.RFQBtn)this.RFQBtn.disable();
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
                    if(this.editBttn && (!this.isRequisition || (this.isRequisition ))) {
                        this.editBttn.enable();
                    }
                }else{
                    if(this.editBttn)this.editBttn.disable();
                }
            }else{
                if(this.editBttn && (!this.isRequisition || (this.isRequisition))) {
                    this.editBttn.enable();
                }    
            }
        }else{
            if(this.editBttn)this.editBttn.disable();
        }
 
        if((this.sm.getCount()==1 && rec[0].data.deleted != true)){
            if(this.email)this.email.enable();
            if(this.POfromSOBttn && rec[0].data.withoutinventory==false)this.POfromSOBttn.enable();
            if(this.SOfromPOBttn && rec[0].data.withoutinventory==false)this.SOfromPOBttn.enable();
            if(this.copyInvBttn)this.copyInvBttn.enable();
            this.withInvMode = rec[0].data.withoutinventory;
        }else{
            if(this.email)this.email.disable();
            if(this.POfromSOBttn)this.POfromSOBttn.disable();
            if(this.SOfromPOBttn)this.SOfromPOBttn.disable();
            if(this.copyInvBttn)this.copyInvBttn.disable();   
        }
        if(this.sm.getCount()==1) {
            if(!this.isQuotation) {
                this.linkinfoViewBtn.enable();    //linkinfo button is enabled if it is not quotation
            }                                  
            if(this.agedDetailsBtn)this.agedDetailsBtn.enable();              
        } else {
            if(!this.isQuotation) {
                this.linkinfoViewBtn.disable();
            }            
            if(this.agedDetailsBtn)this.agedDetailsBtn.disable();             
        }
        if(this.sm.getCount()>=1){
            if(this.singlePrint)this.singlePrint.enable();
            if(this.singleRowPrint)this.singleRowPrint.enable();
        }else{
            if(this.singlePrint)this.singlePrint.disable();
            if(this.singleRowPrint)this.singleRowPrint.disable();
        }

    },
    loadParmStore:function(){
        this.typeEditor.setValue(0);
        this.Store.on('load',this.expandRow, this);
        if(this.invID==null && !this.isSalesCommissionStmt)
            this.Store.load({
                params:{
                    start:0,
                    limit:30,
                    pagingFlag:true
                }
            });
    this.Store.on('datachanged', function() {
        if(this.invID==null){
            var p = this.pP.combo.value;
            this.quickPanelSearch.setPage(p);
        }
    }, this);
    WtfComMsgBox(29,4,true);
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
    }
},

genSuccessResponseQuote : function(response){
    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
        this.loadStore();
    }, this);
},
genFailureResponseQuote : function(response){
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
},

 
storeloaded:function(store){
    this.quickPanelSearch.StorageChanged(store);
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
    if(this.isCustomer && this.isQuotation){
        callViewCustomerQuotationVersions(formrec,billid, 'ViewCustomerQuotationVersions',isCustomer);
    }
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
                    callViewConsignmentRequest(true,formrec,billid, false,null,false,false,false,true);
                }else{
                    callViewSalesOrder(true,formrec,billid, false);
                }
            }else if(this.isQuotation){
                if(this.isLeaseFixedAsset){
                    callViewLeaseQuotation(true, billid, formrec, false,true,true);
                }else{
                    callViewQuotation(false,billid, formrec, true,true);
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
            callViewPurchaseReq(true,formrec,billid);
            
        }
        else if(this.isQuotation)
        {
            callViewVendorQuotation(false, billid, formrec, true, false, false, true);                            
        }
        else  if(this.isOrder)
        {    
            this.withInvMode = formrec.get("withoutinventory");  
            if(!this.withInvMode){				// Without Inventory
                callViewPurchaseOrder(true,formrec,billid,false,this,this.newtranType, true);
            }else{								// With Inventory
                callViewBillingPurchaseOrder(true,formrec,billid,false,this,this.newtranType, false);
    }
        }else if(this.isPurchaseOrder){
                callViewPurchaseOrderVersion(false,billid, formrec, true,true);
        }else if(this.isRFQ){
            callViewRequestForQuotation(true,this.PR_IDS, this.PR_MEMOS,formrec);
        }else{
            if(this.isFixedAsset ||formrec.data.fixedAssetInvoice){
                callViewFixedAssetGoodsReceipt(formrec, billid+'GoodsReceipt',false,isExpensiveInv,undefined,false,formrec.data.fixedAssetInvoice);
            }else{
                callViewGoodsReceipt(formrec, 'ViewGoodsReceipt',isExpensiveInv);
            }
        }
    }
},
    
checkDepreciationPostedOrNot:function(formrec,label,copyInv,isExpensiveInv){
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
                callEditFixedAssetInvoice(formrec, label+'Invoice',copyInv,undefined,false,this.isFixedAsset,this.isLeaseFixedAsset);
            }
            else{
                callEditFixedAssetGoodsReceipt(formrec, label+'GoodsReceipt',copyInv,isExpensiveInv,undefined,false,this.isFixedAsset);
            }
        }
    },function(res,req){
            
        });
},
    
editTransaction:function(copyInv){
    var formrec=null;
    if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
        WtfComMsgBox(15,2);
        return;
    }   
    formrec = this.grid.getSelectionModel().getSelected();
    var label=copyInv?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.product.edit");
    var findtablabel=copyInv?WtfGlobal.getLocaleText("acc.product.edit"):WtfGlobal.getLocaleText("acc.common.copy");
    var incash=formrec.get("incash");
    var billid=formrec.get("billid");
    this.withoutInvMode = formrec.get("withoutinventory");
    var isExpensiveInv = formrec.get("isexpenseinv");
    var isSelfBilledInvoice = formrec.get("selfBilledInvoice");
    var isreval=formrec.get('isreval');
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
                    if(this.isLeaseFixedAsset) {
                        callLeaseQuotation(true, billid, formrec, false,true);
                    } else{
                        callQuotation(true, null, formrec, false);
                    }
                }else if(incash){
                    if(this.checkDuplicateTabOpen(findtablabel+'CashSales')){
                        callEditCashReceipt(formrec, label+'CashSales',copyInv);   
                    }                  
                }                    
                else{
                    if(this.isFixedAsset){
                        //                        this.checkDepreciationPostedOrNot(formrec,label,copyInv);
                        callEditFixedAssetInvoice(formrec, label+'Invoice',copyInv,undefined,false,this.isFixedAsset);
                        
                    } else if(this.isLeaseFixedAsset){
                        callEditFixedAssetInvoice(formrec, label+'Invoice',copyInv,undefined,false,this.isFixedAsset,this.isLeaseFixedAsset);
                    }else if(this.isConsignment){
                        callConsignmentInvoice(true,formrec,label+'ConsignmentInvoice',false,false,true);
                    } else if(this.checkDuplicateTabOpen(findtablabel+'Invoice')) {
                        callEditInvoice(formrec, label+'Invoice',copyInv,undefined,false,this.isFixedAsset);                                           
                    }
                } 
            }
        }else{
            if(this.isQuotation){
                this.quotation=true;
                callVendorQuotation(true,label+'VendorQuotation', formrec, false);
            } 
//            else  if(this.withInvMode){
//                if(incash)
//                    callEditBillingPurchaseReceipt(formrec, label+'BillingCSInvoice',copyInv);
//                else
//                    callEditBillingGoodsReceipt(formrec,  label+'BillingInvoice',copyInv,undefined, undefined, isSelfBilledInvoice);
//            } 
            else{
                if(this.isRequisition){
                    if(formrec.data['approvestatuslevel']== -99){   
                        callPurchaseReq(true,formrec,billid);
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.statuscheck")],2);
                    }
                }
                else if(incash){
                    if(this.checkDuplicateTabOpen(findtablabel+'PaymentReceipt')) {
                        callEdiCashPurchase(formrec, label+'PaymentReceipt',copyInv,isExpensiveInv);
                    }                      
                }                    
                else{
                    if(this.isFixedAsset){
                        this.checkDepreciationPostedOrNot(formrec,label,copyInv,isExpensiveInv);
                    }else if(this.checkDuplicateTabOpen(findtablabel+'GoodsReceipt')){
                        callEditGoodsReceipt(formrec, label+'GoodsReceipt',copyInv,isExpensiveInv,undefined,false,isSelfBilledInvoice);     
                    }                                         
                }   
            }
        }
    }else{
        WtfComMsgBox(55,2);
        return;
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
    var formRecord = null;
    if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
        WtfComMsgBox(15,2);
        return;
    }
    formRecord = this.grid.getSelectionModel().getSelected();
    var label=copyInv?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.product.edit");
    var billid=formRecord.get("billid");
    this.withInvMode = formRecord.get("withoutinventory");
    if(!this.isCustomer){
        if(!this.withInvMode){				// Without Inventory
            callEditPurchaseOrder(true,formRecord,billid,false,this,this.newtranType, copyInv);
        }
//        else{								// With Inventory
//            callBillingPurchaseOrder(true,formRecord,billid,false,this,this.newtranType, copyInv);
//        }
    }
    else{
        if(!this.withInvMode){				// Without Inventory
            if(this.isLeaseFixedAsset){
                callFixedAssetLeaseSalesOrder(true,formRecord,billid, copyInv,null,false,false,false,true);
            }else if(this.isConsignment){
                callConsignmentRequest(true,formRecord,billid, copyInv,null,false,false,false,true);
            }else{
                callSalesOrder(true,formRecord,billid, copyInv);
            }
    			
        }
//        else{								// With Inventory
//            callBillingSalesOrder(true,formRecord,billid, copyInv);
//        }
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
    
createPOfromSO:function(){              
    var formRecord = null;
    if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
        WtfComMsgBox(15,2);
        return;
    }
    formRecord = this.grid.getSelectionModel().getSelected();
    var billid=formRecord.get("billid");
    this.withInvMode = formRecord.get("withoutinventory");    	
    if(!this.withInvMode){				// Without Inventory
        callEditPurchaseOrder(true,formRecord,"Generate_PO"+billid,true,this,this.newtranType, false);
    }
//    else{								// With Inventory
//        callBillingPurchaseOrder(true,formRecord,billid);
//    }    	    	
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
    if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
        WtfComMsgBox(15,2);
        return;
    }
    formrec = this.grid.getSelectionModel().getSelected();
    var incash=formrec.get("incash");
    if(incash == "" || incash == undefined || incash == null){
        incash = false;
    }
    this.withInvMode = formrec.get("withoutinventory");
    if(this.isCustomer && this.isQuotation){
        callEmailWin("emailwin",formrec,this.label,50,true,false,true);
    }else if(!this.isCustomer && this.isQuotation){
        callEmailWin("emailwin",formrec,this.label,57,false,false,true);
    } else if(this.isRFQ){
        callEmailWin("emailwin",formrec,this.label,59,false,false,false,false,false,false,true);
    } else if(incash){
        if(this.isCustomer){
            callEmailWin("emailwin",formrec,this.label,2,true,false,false,true);
        }else{
            callEmailWin("emailwin",formrec,this.label,6,false,false,false,true);
        }
    }else if(this.isOrder){
        if(this.isCustomer){
            this.isConsignment?callEmailWin("emailwin",formrec,this.label,53,true,false,false,false,true,false,false,false,false,true):callEmailWin("emailwin",formrec,this.label,1,true,false,false,false,true,false,false,false,false,false);
        }else{
            callEmailWin("emailwin",formrec,this.label,5,false,false,false,false,true);
        }
    }else{
        if(this.isCustomer){
            if(this.withInvMode){
                callEmailWin("emailwin",formrec,this.label,11,true,true);
            }
            else{
                callEmailWin("emailwin",formrec,this.label,2,true,true);
            }
        }else{
            if(this.withInvMode){
                callEmailWin("emailwin",formrec,this.label,15,false,true);
            }
            else{
                if(this.isCustomer){
                    callEmailWin("emailwin",formrec,this.label,6,true,true);
                } else {
                    callEmailWin("emailwin",formrec,this.label,6,false,true);
                }
            }
        }
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
            isfavourite:true
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
            isfavourite:false
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
    var colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
    this.expanderBody=body;
    this.isexpenseinv=!this.isCustomer&&record.data.isexpenseinv;
    this.withInvMode = record.data.withoutinventory;
    this.expandStore.load({
        params:{
            bills:record.data.billid,
            isexpenseinv:(!this.isCustomer&&record.data.isexpenseinv)
            }
        });
},
fillExpanderBody:function(){
    var disHtml = "";
    var arr=[];
    if(this.isexpenseinv){//for vendor expense invoice[PS]
        arr=[WtfGlobal.getLocaleText("acc.invoiceList.accName") , WtfGlobal.getLocaleText("acc.invoiceList.expand.description") ,WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),'                  '];
        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.invoiceList.expand.accList")+"</span>";   //Account List
        var custArr = [];
        custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[this.moduleid]);
        var arrayLength=arr.length;
        var arrCounter=0;
        for(var custCount=0;custCount<custArr.length;custCount++){
            var headerFlag=false;
            if(custArr[custCount].header != undefined ) {
                    
                for(var j=0;j<this.customizeData.length;j++){
                    if(custArr[custCount].header==this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol){
                        headerFlag=true; 
                    }
                }
                if(!headerFlag){
                    arr[arrayLength]=custArr[custCount].header;
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
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        for(var i=0;i<this.expandStore.getCount();i++){
            var rec=this.expandStore.getAt(i);
            var accountname= rec.data['accountname'];
            var description= "";
            if(rec.data['desc']!=null && rec.data['desc']!=undefined)
                description = rec.data['desc'];
            header += "<span class='gridNo'>"+(i+1)+".</span>";
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'  wtf:qtip='"+accountname+"'>"+Wtf.util.Format.ellipsis(accountname,15)+"&nbsp;</span>";
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'  wtf:qtip='"+description+"'>"+Wtf.util.Format.ellipsis(description,25)+"&nbsp;</span>";
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data.rate,rec.data['currencysymbol'],[true])+"</span>";
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data.prdiscount+"% "+"&nbsp;</span>";
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'],rec.data['currencysymbol'],[true])+"</span>";
            var amount=rec.data.rate-(rec.data.rate*rec.data.prdiscount)/100;
            amount+=rec.data['rowTaxAmount'];//amount+=(amount*rec.data.prtaxpercent/100);
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true])+"</span>";
            for(var cust=0;cust<custArr.length;cust++){
                var headerFlag=false;
                if(custArr[cust].header != undefined ) {
                    for(var j=0;j<this.customizeData.length;j++){
                        if(custArr[cust].header==this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol){
                            headerFlag=true; 
                        }
                    }
                    if(!headerFlag){
                        if(rec.data[custArr[cust].dataIndex]!=undefined && rec.data[custArr[cust].dataIndex]!="null")
                            header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[cust].dataIndex]+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[cust].dataIndex],15)+"&nbsp;</span>";
                        else
                            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                    }
                }  
            } 
            header +="<br>";
        }
        header += "</div>";
        disHtml += "<div class='expanderContainer1'>" + header + "</div>";
    }else{
        //            arr=[(this.isCustBill?'':'Product ID'),(this.isCustBill?'Product Details':'Product Name' ),(this.isCustBill?'':'Product Type'),'Quantity','Unit Price',(this.isOrder||this.isQuotation)?'':'Discount','Tax Percent','Amount',"                  "];//(this.isCustBill?'':'Remark'),
        var productTypeText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pTypeNonInv") : WtfGlobal.getLocaleText("acc.invoiceList.expand.pType");

        arr=[(this.withInvMode?'':WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
        (this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
        (this.isCustomer?
            productTypeText:(Wtf.account.companyAccountPref.countryid == '203' && !this.isQuotation && !this.isOrder)?
            (WtfGlobal.getLocaleText("acc.field.PermitNo.")):productTypeText),//Product Type
        WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity,
        (!this.withInvMode&&(this.isOrder&&this.isCustomer||this.isOrder&&!this.isCustomer))?WtfGlobal.getLocaleText("acc.field.BalanceQty"):'',
        this.isRFQ ? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"),//Unit Price
        (this.isCustomer && !this.isQuotation&& !this.isOrder&&!this.withInvMode)?WtfGlobal.getLocaleText("acc.field.PartialAmount(%)") : '',
        WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),//Discount
        (this.isRFQ || this.isRequisition)? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),//Tax
        this.isRFQ ? '' : WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),//Amount
        this.isRequisition ? WtfGlobal.getLocaleText("acc.field.ApproverRemark") : '',//Approver Remark
        (this.isCustomer && this.isQuotation && !this.isOrder)?WtfGlobal.getLocaleText("acc.field.VQ.No"): '',
        (!this.isCustomer && this.isQuotation && !this.isOrder)?WtfGlobal.getLocaleText("acc.field.PurchaseRequisition.No"): '',
        (this.isCustomer && !this.isQuotation && !this.isOrder)?((this.isLeaseFixedAsset)?WtfGlobal.getLocaleText("acc.field.LeaseDONO"):((this.isConsignment)?"":WtfGlobal.getLocaleText("acc.field.SO/DO/CQNo"))):(!this.isCustomer && !this.isQuotation&& !this.isOrder && !this.isRFQ)?WtfGlobal.getLocaleText("acc.field.PO/GR/VQNo"):"",
        (this.isCustomer && !this.isQuotation && this.isOrder && !this.isRequisition && !this.isConsignment)?WtfGlobal.getLocaleText("acc.field.CQ/PO.No"):(!this.isCustomer && !this.isQuotation && this.isOrder && !this.isRequisition && !this.isConsignment)?WtfGlobal.getLocaleText("acc.field.SO/VQ.No"):""];
        var gridHeaderText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
        header = "<span class='gridHeader'>"+gridHeaderText+"</span>";   //Product List
        var custArr = [];
        custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[this.moduleid]);
        var arrayLength=arr.length;
        var arrCounter=0;
        for(var custCount=0;custCount<custArr.length;custCount++){
            var headerFlag=false;
            if(custArr[custCount].header != undefined ) {
                    
                for(var j=0;j<this.customizeData.length;j++){
                    if(custArr[custCount].header==this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol){
                        headerFlag=true; 
                    }
                }
                if(!headerFlag){
                    arr[arrayLength]=custArr[custCount].header;
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
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                
        for(var storeCount=0;storeCount<this.expandStore.getCount();storeCount++){
            rec=this.expandStore.getAt(storeCount);
            var productname=this.withInvMode?rec.data['productdetail']: rec.data['productname'];

            //Column : S.No.
            header += "<span class='gridNo'>"+(storeCount+1)+".</span>";

            //Column : Product Id for Inventory
            if(!this.withInvMode)
                var pid=rec.data['pid'];
            header += "<span class='gridRow'  wtf:qtip='"+pid+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(pid,10)+"</span>";

            //Column : Product Name
            header += "<span class='gridRow'  wtf:qtip='"+productname+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(productname,10)+"</span>";

            if(!this.isCustomer && !this.isQuotation && !this.isOrder && Wtf.account.companyAccountPref.countryid == '203'){
                var permitno=rec.data['permit']=="undefined" ?"":rec.data['permit'];
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+permitno+"&nbsp;</span>";
            }else if(!this.withInvMode){
                var type = "";
                type = rec.data['type']
                header += "<span class='gridRow' wtf:qtip='"+type+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(type,15)+"</span>";
            }
            else {
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
            }

            //Quantity
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+parseFloat(getRoundofValue(rec.data['quantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+rec.data['unitname']+"</span>";
                
            //balance Quantity
            if(!this.withInvMode&&(this.isOrder&&this.isCustomer||this.isOrder&&!this.isCustomer))
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+parseFloat(getRoundofValue(rec.data['balanceQuantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+rec.data['unitname']+"</span>";

            //Unit Price
            if(!this.isRFQ) {
                var rate=this.isQuotation||this.isOrder&&!this.withInvMode?rec.data.orderrate:rec.data.rate;
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.withCurrencyUnitPriceRenderer(rate,true,rec)+"</span>";
            }

            //Partial Amount
            if((this.isCustomer && !this.isQuotation&& !this.isOrder&&!this.withInvMode)) {
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['partamount']+"% "+"&nbsp;</span>";
            }
                
            //Discount
            if(rec.data.discountispercent == 0){
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
            } else {
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
            }

            //Tax
            if(!this.isRequisition && !this.isRFQ)
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'],rec.data['currencysymbol'],[true])+"</span>";  
                
            //Amount
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
                    
            if(!this.isRFQ)
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true])+"</span>";
                    
            header += (!this.isQuotation && !this.isOrder)?"<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['linkto']+"&nbsp;</span>":"";
            header += (this.isCustomer && !this.isQuotation && this.isOrder)?"<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['linkto']+"&nbsp;</span>":"";
            header += (!this.isCustomer && !this.isQuotation && this.isOrder && !this.isRequisition)?"<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['linkto']+"&nbsp;</span>":"";
            header += (this.isQuotation && !this.isOrder)?"<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['linkto']+"&nbsp;</span>":"";                
            if(this.isRequisition)
                header += "<span class='gridRow'  style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.URLDecode(rec.data['approverremark'])+"&nbsp;</span>";

            //Blank Column
            if(!this.withInvMode && !this.isRequisition)
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['productmoved']+"</span>";
            for(var cust=0;cust<custArr.length;cust++){
                   
                var headerFlag=false;
                if(custArr[cust].header != undefined ) {
                    
                    for(var j=0;j<this.customizeData.length;j++){
                        if(custArr[cust].header==this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol){
                            headerFlag=true; 
                        }
                    }
                    if(!headerFlag){
                        if(rec.data[custArr[cust].dataIndex]!=undefined && rec.data[custArr[cust].dataIndex]!="null")
                            header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[cust].dataIndex]+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[cust].dataIndex],15)+"&nbsp;</span>";
                        else
                            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                    }
                }     
            } 
            header +="<br>";
        }
        if(this.expandStore.getCount()==0){
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
            header += "<span class='headerRow'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</span>"
        }
        header += "</div>";
        disHtml += "<div class='expanderContainer1'>" + header + "</div>";
    }
    this.expanderBody.innerHTML = disHtml;
},

onCellClick:function(g,i,j,e){
    this.cellclickhandler(g,i,j,e);
    e.stopEvent();
    var el=e.getTarget("a");
    if(el==null)return;
    var header=g.getColumnModel().getDataIndex(j);
    if(header=="entryno"){
        var accid=this.Store.getAt(i).data['journalentryid'];
        this.fireEvent('journalentry',accid,true, this.consolidateFlag);
    }
    if(header=="billno"){
        this.viewTransection(g,i,e)
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
    if(this.Store.getCount()==0){
        if(this.exportButton)this.exportButton.disable();
        if(this.printButton)this.printButton.disable();
        var selTypeVal = this.typeEditor.getValue();
        var emptyTxt = WtfGlobal.getLocaleText("acc.common.norec");
        if(selTypeVal == 3) {//deleted
            emptyTxt = this.deletedRecordsEmptyTxt;
        } else if(selTypeVal == 0 || selTypeVal == 4) {//All or Exclude deleted
            emptyTxt = this.isRequisition ? this.emptytext5 : this.emptytext1+(this.isOrder?"":"<br>"+this.emptytext2);
        } else if(selTypeVal == 1) {//Cash Sales
            emptyTxt = this.isOrder?"":"<br>"+this.emptytext2;
        } else if(selTypeVal == 2) {//Invoice
            emptyTxt = this.emptytext1;
        } else if(selTypeVal == 5) {//Invoice
            emptyTxt = '<div style="font-size:15px; text-align:center; color:#CCCCCC; font-weight:bold; margin-top:7%;">'+WtfGlobal.getLocaleText("acc.field.NoFavouriteRecordAvailable")+'<br></div>';
        }
        if(this.isQuotation){
            emptyTxt = this.isCustomer ? this.emptytext3 : this.emptytext4;
        } else if(this.isRFQ){
            emptyTxt = WtfGlobal.getLocaleText("acc.common.norec");
        }  
        this.grid.getView().emptyText=emptyTxt; 
        this.grid.getView().refresh();
    }else{
        if(this.exportButton)this.exportButton.enable();
        if(this.printButton)this.printButton.enable();
    }
    this.Store.filter('billid',this.invID);
    if(this.exponly && (this.Store.getCount() !== 0)){
        this.expander.toggleRow(0);
    }
},
     
loadStore:function(){
    if (this.Store.baseParams && this.Store.baseParams.searchJson) {
        this.Store.baseParams.searchJson = "";
    }
    this.Store.load({
        params : {
            start : 0,
            limit : this.pP.combo.value,
            ss : this.quickPanelSearch.getValue(),
            pagingFlag:true
        }
    });
    this.Store.on('load',this.storeloaded,this);
},

handleDelete:function(del){
    var delFlag=del;
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
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rem.146")+" "+this.label+" "+ WtfGlobal.getLocaleText("acc.common.version") +"?",function(btn){
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
        var idData = "";
        for(var i=0;i<this.recArr.length;i++){
            var rec = this.recArr[i];
            idData += "{\"billid\":\""+rec.get('billid')+"\"},";
        }
        if(idData.length>1){
            idData=idData.substring(0,idData.length-1);
        }
        data="["+idData+"]";
        this.ajxUrl = "";
        if(this.businessPerson=="Customer"){
            if(delFlag=='del' ){  
                this.ajxUrl = "ACCInvoiceCMN/"+(this.withInvMode?"deleteBillingInvoices":"deleteInvoice")+".do";
            }else if(delFlag=='delp' ){
                this.ajxUrl = "ACCInvoiceCMN/"+(this.withInvMode?"deleteBillingInvoices":"deleteInvoicePermanent")+".do";
            }
            if(this.isQuotation){
                if(delFlag=='del' ){
                    this.ajxUrl = "ACCSalesOrder/deleteQuotationVersions.do";           //This code is used to delete Customer Quotation Versions temporarily.
                }else if(delFlag=='delp' ){
                    this.ajxUrl = "ACCSalesOrder/deleteQuotationVersionsPermanent.do";  //This code is used to delete Customer Quotation Versions permanently.
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
                    this.ajxUrl = "ACCPurchaseOrder/deleteQuotationVersions.do";
                }else if(delFlag=='delp' ){
                    this.ajxUrl = "ACCPurchaseOrder/deleteQuotationVersionsPermanent.do";
                }
            } else if(this.isOrder){
                if(delFlag=='del' ){
                    this.ajxUrl = this.withInvMode?"ACCPurchaseOrder/deleteBillingPurchaseOrders.do":"ACCPurchaseOrder/deletePurchaseOrders.do"
                }else if(delFlag=='delp' ){
                    this.ajxUrl = this.withInvMode?"ACCPurchaseOrder/deleteBillingPurchaseOrders.do":"ACCPurchaseOrder/deletePurchaseOrdersPermanent.do"
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
            this.ajxUrl = "ACCPurchaseOrder/deleteRFQ.do"
        }
        var incash=this.recArr[0].data.incash; 
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:this.ajxUrl,
            params:{
                data:data,
                mode:mode,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset,
                isConsignment:this.isConsignment,
                incash:incash
            }
        },this,this.genSuccessResponse,this.genFailureResponse);            
    },this);
},

genSuccessResponse:function(response){
    WtfGlobal.resetAjaxTimeOut();
    WtfComMsgBox([this.label,response.msg],response.success*2+1);
    for(var i=0;i<this.recArr.length;i++){
        var ind=this.Store.indexOf(this.recArr[i])
        var num= ind%2;
        WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
    }
    if(response.success){
        (function(){
            this.loadStore();
        }).defer(WtfGlobal.gridReloadDelay(),this);
        Wtf.productStore.reload();
        Wtf.productStoreSales.reload();
    }
},

genFailureResponse:function(response){
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

generateGIROFile : function() {
    this.vendorRec = new Wtf.data.Record.create([
    {
        name: 'billid'
    },

    {
        name: 'billno'
    },

    {
        name: 'personname'
    },

    {
        name: 'amount'
    },

    {
        name: 'amountdue'
    },
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
        defaults: {
            anchor:'94%'
        },
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
                        failure:function(f,a){
                            this.Rulewin.close();
                            this.genFailureResponse(eval('('+a.response.responseText+')'))
                            }
                    });
                }
            },
            scope:this
        }]
    });

    this.GIROFileWin=new Wtf.Window({
        title: WtfGlobal.getLocaleText("acc.field.GenerateGIROFile"),
        closable:true,
        iconCls : 'iconwin',
        width: 455,
        autoHeight:true,
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
        case Wtf.autoNum.SalesOrder:
            return "Sales Order";
        case Wtf.autoNum.Invoice:
            return "Invoice";
        case Wtf.autoNum.PurchaseOrder:
            return "Purchase Order";
        case Wtf.autoNum.GoodsReceipt:
            return "Vendor Invoice";
        case Wtf.autoNum.BillingSalesOrder:
            return "Sales Order";
        case Wtf.autoNum.BillingInvoice:
            return "Invoice";
        case Wtf.autoNum.BillingPurchaseOrder:
            return "Purchase Order";
        case Wtf.autoNum.BillingGoodsReceipt:
            return "Vendor Invoice";
        case Wtf.autoNum.Quotation:
            return "Quotation";
    }
},
//DownloadLink : function(a, b, c, d, e, f) {        
//    var msg = "";
//    var url = "ACCInvoiceCMN/getAttachDocuments.do";
//    msg = '<div class = "pwnd downloadDoc" wtf:qtitle="'
//    + WtfGlobal.getLocaleText("acc.invoiceList.attachments")
//    + '" wtf:qtip="'
//    + WtfGlobal
//    .getLocaleText("acc.invoiceList.clickToDownloadAttachments")
//    + '" onclick="displayDocList(\''
//    + c.data['billid']
//    + '\',\''
//    + url
//    + '\',\''
//    + 'invoiceGridId'
//    + this.id
//    + '\', event,\''
//    + ""
//    + '\',\''
//    + ""
//    + '\',\''
//    + false
//    + '\',\''
//    + 0
//    + '\',\''
//    + 0
//    + '\',\''
//    + ""
//    + '\')" style="width: 16px; height: 16px;cursor:pointer" id=\''
//    + c.data['leaveid'] + '\'>&nbsp;</div>';
//    return msg;
//},
//docuploadhandler : function(e, t) {
//    if (e.target.className != "pwndbar1 uploadDoc")
//        return;
//    var selected = this.sm.getSelections();            
//    if (this.grid.flag == 0) {
//        this.fileuploadwin = new Wtf.form.FormPanel(
//        {                   
//            url : "ACCInvoiceCMN/attachDocuments.do",
//            waitMsgTarget : true,
//            fileUpload : true,
//            method : 'POST',
//            border : false,
//            scope : this,
//            bodyStyle : 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
//            lableWidth : 50,
//            items : [
//            this.sendInvoiceId = new Wtf.form.Hidden(
//            {
//                name : 'invoiceid'
//            }),
//            this.tName = new Wtf.form.TextField(
//            {
//                fieldLabel : WtfGlobal.getLocaleText("acc.invoiceList.filePath") + '*',
//                name : 'file',
//                inputType : 'file',
//                width : 200,
//                blankText:WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
//                allowBlank:false,
//                msgTarget :'qtip'
//            }) ]
//        });
//
//        this.upwin = new Wtf.Window(
//        {
//            id : 'upfilewin',
//            title : WtfGlobal
//            .getLocaleText("acc.invoiceList.uploadfile"),
//            closable : true,
//            width : 450,
//            height : 120,
//            plain : true,
//            iconCls : 'iconwin',
//            resizable : false,
//            layout : 'fit',
//            scope : this,
//            listeners : {
//                scope : this,
//
//                close : function() {
//                    thisclk = 1;
//                        scope: this;
//                    this.fileuploadwin.destroy();
//                    this.grid.flag = 0
//                }
//            },
//            items : this.fileuploadwin,
//            buttons : [
//            {
//                anchor : '90%',
//                id : 'save',
//                text : WtfGlobal
//                .getLocaleText("acc.invoiceList.bt.upload"),
//                scope : this,
//                handler : this.upfileHandler
//            },
//            {
//                anchor : '90%',
//                id : 'close',
//                text : WtfGlobal
//                .getLocaleText("acc.invoiceList.bt.cancel"),
//                handler : this.close1,
//                scope : this
//            } ]
//
//        });
//        this.sendInvoiceId.setValue(selected[0].get('billid'));
//        this.upwin.show();
//        this.grid.flag = 1;
//    }
//},
//close1 : function() {
//    Wtf.getCmp('upfilewin').close();
//    this.grid.flag = 0;
//},
//
//upfileHandler : function() {
//    if (this.fileuploadwin.form.isValid()) {
//        Wtf.getCmp('save').disabled = true;
//    }
//    if (this.fileuploadwin.form.isValid()) {
//        this.fileuploadwin.form.submit({
//            scope : this,
//            failure : function(frm, action) {
//                this.upwin.close();
//            },
//            success : function(frm, action) {
//                this.upwin.close();                            
//                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), "File uploaded successfully.");
//            }
//        })
//    }
//},
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
    this.searchJson = json;
    this.filterConjuctionCrit = filterConjuctionCriteria;
    this.Store.baseParams = {
        flag: 1,
        searchJson: this.searchJson,
        moduleid: (this.isSalesCommissionStmt != undefined && this.isSalesCommissionStmt ==true ) ?  Wtf.Acc_Invoice_ModuleId:this.moduleid,
        isFixedAsset:this.isFixedAsset,
        isLeaseFixedAsset:this.isLeaseFixedAsset,
        filterConjuctionCriteria: filterConjuctionCriteria
    }
    this.Store.load({
        params: {
            ss: this.quickPanelSearch.getValue(), 
            start: 0, 
            limit: this.pP.combo.value,
            pagingFlag:true
        }
    });
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
        filterConjuctionCriteria: this.filterConjuctionCrit
    }
    this.Store.load({
        params: {
            ss: this.quickPanelSearch.getValue(), 
            start: 0, 
            limit: this.pP.combo.value,
            pagingFlag:true
        }
    });
this.objsearchComponent.hide();
this.AdvanceSearchBtn.enable();
this.doLayout();
}
});

function displayDocList(id, url, gridid, event,creatorid,approverid,docReq,cnt,statusid,showleaves,reportGridId,moduleid,attachmentIds,isbatch,rowIndex,isReadOnly){    
    if(Wtf.getCmp('DocListWindow'))
        Wtf.getCmp("DocListWindow").destroy();
    new Wtf.DocListWindow({
        wizard:false,
        closeAction : 'hide',
        layout: 'fit',
        title:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),
        shadow:false,
        bodyStyle: "background-color: white",
        closable: true,
        width : 450,
        heigth:250,
        url: url,
        gridid: gridid,
        modal:true,
        autoScroll:true,
        recid:id,
        delurl: isbatch?"ACCInvoiceCMN/deleteDocument.do?docid=":"ACCInvoiceCMN/deleteAttachedDocument.do?transactionid="+id+"&docid=",
        id:"DocListWindow",
        docCount:cnt,
        isDocReq:docReq,  
        statusID:statusid,  
        showleaves:showleaves,  
        dispto:"pmtabpanel",
        reportGridId:reportGridId,            //ERP-13011 [SJ]
        moduleid:moduleid,
        attachmentIds:attachmentIds, //document ids
        isbatch:isbatch, //flag to check whether document is uploaded in from batch 
        rowIndex:rowIndex,
        isReadOnly:(isReadOnly==undefined ||isReadOnly=='')?false:isReadOnly
    });

    var docListWin = Wtf.getCmp("DocListWindow");
    var leftoffset =event.pageX-400;

    var topoffset = event.pageY+10;
    if (document.all) {
        xMousePos = window.event.x+document.body.scrollLeft;
        yMousePos = window.event.y+document.body.scrollTop;
        xMousePosMax = document.body.clientWidth+document.body.scrollLeft;
        yMousePosMax = document.body.clientHeight+document.body.scrollTop;
        leftoffset=xMousePos-400;//xMousePos;
        topoffset=yMousePos+120;//yMousePos;
        
    }
    if(docListWin.innerpanel==null||docListWin.hidden==true){
        docListWin.setPosition(leftoffset, topoffset);

        docListWin.show();
    }else{
        docListWin.hide();

    }
}

