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
function invRecLinkNew(val,isReceipt,isCustBill,rec){
if(isReceipt=="true")
        viewTransactionTemplate1("Sales Invoice", rec,false,val); 
    else
        viewTransactionTemplate1("Purchase Invoice", rec,false,val);
}
function noteRecLinkNew(val, isReceipt, noteType,rec) {
    if (isReceipt == "true")
        viewTransactionTemplate1("Debit Note", rec,false,val); 
    else
        viewTransactionTemplate1("Credit Note", rec,false,val); 
}

function openRecNew(isRec){
    if(isRec=="true") {
        callReceiptNew();
    } else {
        callPaymentNew();
    }
}

/* <COMPONENT USED FOR>
 * 1.Receive Payment Report
 *      ReceiptReport() --- < Payment Received >
 *      [isReceipt:true]
 * 2.Receive Payment Report
 *      BillingReceiptReport() --- <>
 *      [isReceipt:true, isCustBill:true]
 * 3.Payment Report
 *      callPaymentReport() --- <>
 *      [isReceipt:false]
 *
 */
Wtf.account.ReceiptReportNew=function(config){
    this.panelID=config.id;
    this.isMakeOrReceivePayment=true;
    this.isReceipt=config.isReceipt||false;
    this.isCustBill=config.isCustBill||false;
    this.nondeleted=false;
    this.deleted=false;
    this.contraentryflag=false;
    this.onlyOpeningBalanceTransactionsFlag=false;
    this.advancePayment=false;
    this.advanceFromVendor=false;
    this.advanceToCustomer=false;
    this.isPostDatedCheque=false;
    this.allAdvPayment=false;
    this.unUtilizedAdvPayment=false;
    this.partiallyUtilizedAdvPayment=false;
    this.fullyUtilizedAdvPayment=false;
    this.nonorpartiallyUtilizedAdvPayment=false;
    this.pendingapproval=config.pendingapproval;
    this.isDishonouredCheque=false;
    this.label=(this.isReceipt? WtfGlobal.getLocaleText("acc.title.payment.receipt"):WtfGlobal.getLocaleText("acc.title.payment.voucher"));
    this.uPermType=(this.isReceipt?Wtf.UPerm.salesreceivepayment:Wtf.UPerm.purchasemakepayment);
    this.permType=(this.isReceipt?Wtf.Perm.salesreceivepayment:Wtf.Perm.purchasemakepayment);
    this.exportPermType=(this.isReceipt?this.permType.exportdatareceipt:this.permType.exportdatapayment);
    this.printPermType=(this.isReceipt?this.permType.printreceipt:this.permType.printpayment);
    this.removePermType=(this.isReceipt?this.permType.removereceipt:this.permType.removepayment);
    this.editPermType=(this.isReceipt?this.permType.editreceipt:this.permType.editpayment);
    this.emailPermType=(this.isReceipt?this.permType.emailreceipt:this.permType.emailpayment);
    this.copyPermType=(this.isReceipt?this.permType.copyreceipt:this.permType.copypayment);
    this.moduleid= config.moduleid;
    this.winValue= config.winValue;
    this.reportbtnshwFlag=config.reportbtnshwFlag;
    this.fromNavigationPanel= config.fromNavigationPanel;
    this.isReceivable=config.isReceivable;
    this.print=false;
    this.gridConfigId = "";
    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable){
        if (dojoInitCount <= 0) {
            dojo.cometd.init("../../bind");
            dojoInitCount++;
        }
        var channelName = "";
        if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId) {
            channelName = Wtf.ChannelName.VIAndCPReport;
        }
        //Once Goods Receipt created then we should reload "Payment Report" to get updated "IsTDSAmtUsedInGoodsReceipt" flag.
        //Once Goods Receipt deleted permanently then we should reload "Payment Report" to get updated "IsTDSAmtUsedInGoodsReceipt" flag.
        if (channelName != "") {
            dojo.cometd.subscribe(channelName, this, "globalInvoiceListGridAutoRefreshPublishHandler");
        }
    }
    
    this.paymentwindowtype = {
        AgainstVendor: 1,
        AgainstCustomer: 2,
        AgainstGL: 3
    }
    if(this.reportbtnshwFlag== undefined || this.reportbtnshwFlag == null)
       {
          this.reportbtnshwFlag=false;
       }
       
       /*
        * Below method is used to set the Report fileter values 
        */
     this.updateReportFilterValues(this.fromNavigationPanel);
     this.typeEditor = new Wtf.form.ComboBox({
        store: this.delTypeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid+this.panelID,
        valueField:'typeid',
        mode: 'local',
        value:0,
        width:250,
        disabled:(!this.pendingapproval && (this.winValue!=undefined)),     // disabled only in 'Make Payment' tab from Navigation Panel-> 'GL/Cash/Bank'->Entry->Voucher->Make Payment/Receive Payment 
//        hideLabel:this.winValue!=undefined,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    
    if(this.fromNavigationPanel){
        if(this.isReceivable){
            if(this.isReceipt){
                this.typeEditor.setValue(3); 
            }else{
                this.typeEditor.setValue(6);
            }
        }else{
            if(!this.isReceipt){
                this.typeEditor.setValue(3);
            }else{
                this.typeEditor.setValue(6);
            }
        }
//        this.typeEditor.disable(true);
    }
    if(this.winValue!=undefined){
       this.typeEditor.setValue(this.winValue);
       if(this.winValue==9){
          this.isGlcode=true;
       }
       if(config.recordType==10 ||config.recordType==13){
          this.typeEditor.setValue(config.recordType);  
          this.recordType="1,7";  
       }
       if(config.recordType==11 && config.recordType==12){
          this.typeEditor.setValue(config.recordType);  
          this.recordType="6,7";  
       }
    }
    
    this.expandRec = Wtf.data.Record.create ([
        {name:'transectionno'},
        {name:'creationdate',type:'date'},
        {name:'duedate',type:'date'},
        {name:'quantity'},
        {name:'currencysymbol'},
        {name:'amount'},
        {name:'totalamount'},
        {name:'rowid'},
        {name:'transectionid'},
        {name:'billid'},
        {name:'journalentryid'},
        {name:'journalentrydate'},
        {name:'personid'},
        {name:'entryno'},
        {name:'billno'},
        {name:'transectionno'},
        {name:'date',type:'date',mapping:'creationdate'},
        {name:'currencyid'},
        {name:'oldcurrencyrate'},
        {name:'currencyname'},
        {name:'oldcurrencysymbol'},
        {name:'vendorid'},
        {name:'vendorname'},
        {name:'personname'},
        {name:'detailsjarr'},
        {name: 'externalcurrencyrate'},
        {name:'amountdue', mapping:'amountduenonnegative'},
        {name:'taxpercent'},
        {name:'taxname'},
        {name:'discount'},
        {name:'memo'},
        {name:'cinno'},
        {name:'payment'},
        {name:'accountid'},
        {name:'accountname'},
        {name:'description'},
        {name:'dramount'},
        {name:'bankCharges'},
        {name:'bankChargesCmb'},
        {name:'bankInterest'},
        {name:'bankInterestCmb'},
        {name:'amountpaid'},
        {name:'isprinted'},
        {name:'isRepeated'},
        {name: 'type'},
        {name: 'paidamount'},
        {name: 'supplierinvoiceno'},
        {name:'linkingdate',type:'date'},
        {name: 'discountAmount'}

    ]);
    this.expandStoreUrl = "";
    if(this.isReceipt){
        this.expandStoreUrl =  (this.isCustBill?"ACCReceipt/getBillingReceiptRows":"ACCReceiptCMN/getReceiptRowsNew") + ".do";
    }else{
        this.expandStoreUrl =  (this.isCustBill?"ACCVendorPayment/getBillingPaymentRows":"ACCVendorPaymentCMN/getPaymentRowsNew") + ".do";
    }

    this.expandStore = new Wtf.data.Store({
        url : this.expandStoreUrl,
//        url:Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
        baseParams:{
            mode:(this.isCustBill?36:33)
        },
        
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    this.usersRec = new Wtf.data.Record.create([
        {name: 'billid'},
        {name:'companyid'},
        {name:'companyname'},
        {name: 'refid'},
        {name: 'personid'},
        {name: 'billno'},
        {name: 'refno'},
        {name: 'refname'},
        {name: 'refdetail'},
        {name:'personemail'},
        {name:'paymentWithSalesReturn'},
        {name:'billingEmail'},
        {name: 'detailtype'},
        {name: 'expirydate',type:'date'},
        {name: 'journalentryid'},
        {name: 'hasApprovalAuthority'},
        {name: 'isFinalLevelApproval'},
        {name: 'approvalstatusinfo'},
        {name:'journalentrydate'},
        {name: 'entryno'},
        {name: 'currencysymbol'},
        {name: 'externalcurrencyrate'},
        {name: 'personname'},
//        {name: 'customervendorname'},
        {name: 'address'},
        {name: 'deleted'},
        {name: 'billdate',type:'date'},
        {name: 'chequedate',type:'date'},
        {name: 'chequedateforprint',type:'date'},
        {name: 'paymentmethod'},
        {name: 'chequenumber'},
        {name: 'bankname'},
        {name: 'chequedescription'},
        {name: 'memo'},
        {name: 'dishonoured'},
        {name: 'tdsApplicable'},
        {name: 'tdsaccountid'},
        {name: 'challanGenerated'},
        {name: 'cinno'},
        {name: 'amount'},
        {name: 'amountinbase'},
        {name: 'methodid'},
        {name: 'receiptamount'},
        {name: 'currencyid'},
        {name: 'detailsjarr'},
        {name: 'clearancedate',type:'date'},
        {name: 'paymentstatus'},
        {name: 'paymentStatus'},
        {name: 'otherwise'},
        {name: 'isOpeningBalanceTransaction'},
        {name: 'isNormalTransaction'},
        {name: 'receipttype'},
        {name: 'paymentwindowtype'},
        {name: 'isadvancepayment'},
        {name: 'isadvancefromvendor'},
        {name: 'advanceUsed'},
        {name: 'advanceid'},
        {name: 'advanceamount'},
        {name: 'withoutinventory'},
        {name: 'refcardno'},
        {name: 'refexpdate'},
        {name: 'ismanydbcr'},
        {name:'dramount'},
        {name:'bankCharges'},
        {name:'bankChargesCmb'},
        {name:'bankInterest'},
        {name:'bankInterestCmb'},
        {name:'paidToCmb'},
        {name:'paidto'},
        {name:'isprinted'},
        {name:'isEmailSent'},
        {name:'sequenceformatid'},
        {name:'totaltaxamount'},
        {name:'totaltdsamount'},
        {name: 'isIBGTypeTransaction',type:'boolean'},
        {name: 'ibgDetailsID'},
        {name: 'ibgCode'},
        {name:'paymentamountdue'},
        {name:'paymentamountdueinbase'},
        {name:'isLinked'},
        {name:'linkedadvanceMsgFlag'},
        {name:'disableOtherwiseLinking'},        
        {name:'cndnid'},
        {name:'invoiceadvcndntype'},
        {name:'cndnAndInvoiceId'},
        {name:'ischequeprinted', type:'boolean'},
        {name:'recordsHavingAdvancePaymentsAsRefund'},
        {name:'soRecordsHavingAdvancePayments'},
        {name:'isPaymentLinkedToSalesOrder'},
        {name:'isAdvancePaymentUsedAsRefund'},
        {name:'isRepeated'},
        {name:'payee'},
        {name:'exciseunit'},
        {name:'paymentaccountid'},
        {name: 'isDishonouredCheque'},
        {name:'isCopyAllowed'},
        {name:'chequeOption'},
        {name:'paymentaccountid'},
        {name:'attachdoc'},
        {name:'attachment'},
        {name:'amountBeforeTax'},
        {name:'isWrittenOff'},
        {name:'isactive'},
        {name:'currencycode'},
        {name:'parentid'},
        {name: 'isLinkedInvoiceIsClaimed'},
        {name: 'isRefundTransaction', type:'boolean'},
        {name: 'AllowToEditCopy', type:'boolean'},
        {name: 'tdsPaymentJsonFlag', type:'boolean'},
        {name: 'IsTDSAmtUsedInGoodsReceipt', type:'boolean'},
        {name:'isopening'},
        {name:'onlyBillingAdddressData'},
        {name:'approvalLevel'},
        {name:'personcode'},
        {name:'rcmApplicable'}
        
    ]);

    this.userdsUrl = "";
    if(this.isReceipt){
        this.userdsUrl = "ACCReceiptCMN/getReceipts.do";
    }else{
        this.userdsUrl = "ACCVendorPaymentCMN/getPayments.do";
    }
    if(config.consolidateFlag){
        this.userds = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.usersRec),
            url : this.userdsUrl,
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
    //        url:Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
            baseParams:{
                mode:(this.isCustBill?35:32),
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isprinted:false,
                isEmailSent:false
            }
        });
    } else {
        this.userds = new Wtf.data.Store({
            remoteSort:true,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.usersRec),
            url : this.userdsUrl,
    //        url:Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
            baseParams:{
                mode:(this.isCustBill?35:32),
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isprinted:false,
                isEmailSent:false
            }
        });
    }
    
    var colModelArray = GlobalColumnModelForReports[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray,this.userds);
    
    var sdateSavedSearch;
    var edateSavedSearch;
    if(config.searchJson != undefined && config.searchJson != ""){
        sdateSavedSearch = JSON.parse(config.searchJson).data[0].sdate;
        edateSavedSearch = JSON.parse(config.searchJson).data[0].edate;
    }
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        value:WtfGlobal.getDates(true, sdateSavedSearch)
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false, edateSavedSearch)
    });
    
    this.fetchBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
  tooltip:this.isReceipt?WtfGlobal.getLocaleText("acc.pr.fetchTT"):WtfGlobal.getLocaleText("acc.pm.fetchTT"),  //"Select a time period to view corresponding make/receive payments.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.loadStore                        
    });
    this.RepeatPayment=new Wtf.Toolbar.Button({
        text:this.isReceipt?WtfGlobal.getLocaleText("acc.rp.setRecurring"):WtfGlobal.getLocaleText("acc.mp.setRecurring") ,
        iconCls:getButtonIconCls(Wtf.etype.copy),
        id:this.isReceipt?'RecurringRP':'RecurringMP',        
        tooltip :this.isReceipt?WtfGlobal.getLocaleText("acc.field.CreateRecurringReceipt"):WtfGlobal.getLocaleText("acc.field.CreateRecurringPayment"),
        style:"padding-left:0px;",
        scope: this,
        disabled : true,  
        hidden: this.isReceipt || this.pendingApproval,
        handler: this.repeatePaymentHandler
    })
    this.userds.on('beforeload',function(s,o){
        WtfGlobal.setAjaxTimeOut();
        if(!o.params)o.params={};
        o.params.deleted=this.deleted;
        o.params.nondeleted=this.nondeleted;        
        o.params.onlyOpeningBalanceTransactionsFlag=this.onlyOpeningBalanceTransactionsFlag; 
        o.params.ispendingAproval=config.pendingapproval;
       /*
        pass includeexcludechildCmb combobox value and customer/vendor combobox value   
       */
        s.baseParams.includeExcludeChildCmb=this.includeExcludeChildCmb.getValue();
        s.baseParams.isPaymentReport=true; 
        s.baseParams.custVendorID=(this.CustomizedName.getValue()!="")?this.CustomizedName.getValue():"All";
        if(this.contraentryflag) {o.params.contraentryflag=this.contraentryflag;}
        if(this.advancePayment) {o.params.advancePayment=this.advancePayment;}
        if(this.advanceFromVendor) {o.params.advanceFromVendor=this.advanceFromVendor;}
        if(this.advanceToCustomer) {o.params.advanceToCustomer=this.advanceToCustomer;}
        if(this.isPostDatedCheque) {o.params.isPostDatedCheque=this.isPostDatedCheque;}
        
        if (this.allAdvPayment) {
            o.params.allAdvPayment = this.allAdvPayment;
        }
        if (this.unUtilizedAdvPayment) {
            o.params.unUtilizedAdvPayment = this.unUtilizedAdvPayment;
        }
        if (this.partiallyUtilizedAdvPayment) {
            o.params.partiallyUtilizedAdvPayment = this.partiallyUtilizedAdvPayment;
        }
        if (this.nonorpartiallyUtilizedAdvPayment) {
            o.params.nonorpartiallyUtilizedAdvPayment = this.nonorpartiallyUtilizedAdvPayment;
        }
        if (this.fullyUtilizedAdvPayment) {
            o.params.fullyUtilizedAdvPayment = this.fullyUtilizedAdvPayment;
        }
           
        if(this.isDishonouredCheque) {o.params.isDishonouredCheque=this.isDishonouredCheque;}
        if(this.isGlcode) {o.params.isGlcode=this.isGlcode;}    //added into params for GL Code
         if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)&&!config.pendingapproval){
         this.printButton.params.paymentWindowType="";
         }
        if(this.recordType==6){
               o.params.paymentWindowType= 2;                // 2== 'Payment Made against Customer' or 'Payment Received against Vendor'
            if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)&&!config.pendingapproval){
               this.printButton.params.paymentWindowType= 2;   
            }//Flag for F1-Recreation Receipt repor- Mayur Bhokase
        } else if(this.recordType==3){
               o.params.paymentWindowType= 1;   
           if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)&&!config.pendingapproval){// 1== 'Payment Made against Vendor' or 'Payment Received against Customer'
               this.printButton.params.paymentWindowType= 1; 
           }               //Flag for F1-Recreation Receipt repor- Mayur Bhokase
        } else if(this.recordType==9){
               o.params.paymentWindowType= 3;               //  3== Payment Made/Received against GL Code
          if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)&&!config.pendingapproval){
               this.printButton.params.paymentWindowType= 3;  
          }//Flag for F1-Recreation Receipt repor- Mayur Bhokase
        } 
        if(this.isGlcodeValue!=null) {
            o.params.isGlcodeValue=9;
            o.params.paymentWindowType= 3;  
            if (this.printButton) {
                this.printButton.params.paymentWindowType = 3;
                this.printButton.params.exportPtw = true;
            }
//            if (this.exportButton) {
//                this.exportButton.params.paymentWindowType = 3;
//                this.exportButton.params.exportPtw = true;//  3== Payment Made/Received against GL Code
//            }
            
        }
        if(this.fromNavigationPanel){
            if(this.isReceivable){
                if(this.isReceipt){
                    o.params.paymentWindowType= 1; 
                    if (this.printButton) {
                        this.printButton.params.paymentWindowType = 1;
                        this.printButton.params.exportPtw = true;
                    }
//                    if (this.exportButton) {
//                        this.exportButton.params.paymentWindowType = 1;
//                        this.exportButton.params.exportPtw = true;
//                    }
                    
                    this.recordType=1;
                }else{
                    o.params.paymentWindowType= 2; 
                    if (this.printButton) {
                        this.printButton.params.paymentWindowType = 2;
                        this.printButton.params.exportPtw = true;
                    }
//                    if (this.exportButton) {
//                        this.exportButton.params.paymentWindowType = 2;
//                        this.exportButton.params.exportPtw = true;
//                    }
                    this.recordType=2;
                }
            }else{
                if(!this.isReceipt){
                    o.params.paymentWindowType= 1; 
                    if (this.printButton) {
                        this.printButton.params.paymentWindowType = 1;
                        this.printButton.params.exportPtw = true;
                    }
//                    if (this.exportButton) {
//                        this.exportButton.params.paymentWindowType = 1;
//                        this.exportButton.params.exportPtw = true;
//                    }
                    this.recordType=1;
                }else{
                    o.params.paymentWindowType= 2; 
                    if (this.printButton) {
                        this.printButton.params.paymentWindowType = 2;
                        this.printButton.params.exportPtw = true;
                    }
//                    if (this.exportButton) {
//                        this.exportButton.params.paymentWindowType = 2;
//                        this.exportButton.params.exportPtw = true;
//                    }
                    
                    this.recordType=2;
                }
            }
        }
        o.params.stdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());   //ERP-8884 : RP/MP Report
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
       if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)&&!config.pendingapproval){
        this.printButton.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
        this.printButton.params.stdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        this.printButton.params.mode=(this.isCustBill?35:32);
        this.printButton.params.consolidateFlag=config.consolidateFlag;
        this.printButton.params.companyids=companyids;
        this.printButton.params.gcurrencyid=gcurrencyid;
        this.printButton.params.userid=loginid;
        this.printButton.params.isprinted=false;
        this.printButton.params.ss=this.quickPanelSearch.getValue()==undefined?"":this.quickPanelSearch.getValue();
        this.printButton.params.onlyOpeningBalanceTransactionsFlag=this.onlyOpeningBalanceTransactionsFlag;
        this.printButton.params.deleted=this.deleted;
        this.printButton.params.nondeleted=this.nondeleted;
        this.printButton.params.start=0;
       } 
    },this);
    
    
    
    WtfComMsgBox(29,4,true);


    this.expander = new Wtf.grid.RowExpander({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    var btnArr=[];
    var gridBtnArr=[];//ERP-12381 [SJ]
    var bottombtnArr=[];
    this.gridColumnModelArr=[];
    this.gridColumnModelArr.push(this.sm,this.expander,{
        dataIndex:'billid',
        hidden:true,
        hideable:false    //ERP-5269[SJ] 
    },{
        header:WtfGlobal.getLocaleText("acc.field.Company"),  
        dataIndex:'companyname',
        width:20,
        pdfwidth:150,
//            sortable:true,
        hidden:true
    },{
            header: '',
            dataIndex:'isprinted',
            width:25,
            renderer : function(val, meta, record, rowIndex){
                if(record.data.isprinted){
                    return '<img id="printValiFlag" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.printed')+'" src="../../images/printed.gif">';
                }else{
                    return '<img id="printInValiFlag" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.notprinted')+'" src="../../images/not-printed.gif">'
                }
            }
        },{
            
            header: '', 
            dataIndex:'isEmailSent',
            width:40,
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
        header:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.gridReceiptNo"):WtfGlobal.getLocaleText("acc.pmList.gridPaymentNo"),  //"Receipt No":"Payment No",
        dataIndex:'billno',
        autoWidth : true,
        sortable: true,
        groupable: true,
        pdfwidth:100,
        renderer:config.consolidateFlag?WtfGlobal.deletedRenderer:WtfGlobal.linkDeletedRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.userAdmin.name"),  //"Account Name",
        dataIndex: 'personname',
        autoWidth : true,
        sortable: true,
        groupable: true,
        renderer: WtfGlobal.deletedRenderer,
//        hidden:(this.panelID == "VendorPayment" || this.panelID == "Customer_Payment" || this.panelID == "CustomerReceipt" || this.panelID == "VendorReceipt") ? true : false,
        pdfwidth:100
    },
//    {
//        header: WtfGlobal.getLocaleText("acc.userAdmin.name"), //"Name",
//        renderer:WtfGlobal.deletedRenderer,
//        dataIndex: 'customervendorname',
//        autoWidth : true,
//        sortable: true,
//        groupable: true,
//        hidden:(this.panelID == "VendorPayment" || this.panelID == "Customer_Payment" || this.panelID == "CustomerReceipt" || this.panelID == "VendorReceipt") ? false : true,
//        pdfwidth:100
//    },
    {
        header: this.isReceipt?WtfGlobal.getLocaleText("acc.prList.Date"):WtfGlobal.getLocaleText("acc.pmList.Date"),  //"Receipt Date":"Payment Date",
        dataIndex: 'billdate',
        align:'center',
        renderer:WtfGlobal.onlyDateDeletedRenderer,
        autoWidth : true,
        sortable: true,
        groupable: true,
        pdfwidth:100
    },{
        header :WtfGlobal.getLocaleText("acc.pmList.JEno"),  //'Journal Entry No',
        dataIndex: 'entryno',
        autoSize : true,
        sortable: true,
        groupable: true,
        pdfwidth:100,
        renderer:WtfGlobal.multipleJELinkDeletedRenderer
    },{
        header :WtfGlobal.getLocaleText("acc.prList.gridPaymentMethod"),  //'Payment Method',
        dataIndex: 'paymentmethod',
        autoSize : true,
        sortable: true,
        groupable: true,
        renderer:WtfGlobal.deletedRenderer,
        pdfwidth:100
    },{
        header :WtfGlobal.getLocaleText("acc.field.BankName"),  //'Bank Name',
        dataIndex: 'bankname',
        autoSize : true,
        sortable: true,
        groupable: true,
        renderer:WtfGlobal.deletedRenderer,
        pdfwidth:100
    },{
        header :WtfGlobal.getLocaleText("acc.field.ChequeNumber"),  //'Cheque Number',
        dataIndex: 'chequenumber',
        autoSize : true,
        sortable: true,
        groupable: true,
        renderer:WtfGlobal.deletedRenderer,
        pdfwidth:100
    },{
        header :WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountdescription"),  //'Cheque Number',
        dataIndex: 'chequedescription',
        autoSize : true,
        sortable: true,
        groupable: true,
        renderer:WtfGlobal.deletedRenderer,
        pdfwidth:100
    },{
        header :WtfGlobal.getLocaleText("acc.field.paymentDishonoured"),    
        dataIndex: 'dishonoured',
        autoSize : true,
        sortable: true,
        groupable: true,
        pdfwidth:100,
        renderer:WtfGlobal.deletedRenderer
    },{
        header :Wtf.account.companyAccountPref.descriptionType,  //'Memo',
        dataIndex: 'memo',
        autoSize : true,
        sortable: true,
        groupable: true,
        renderer: function(value) {
            value = value.replace(/\'/g, "&#39;");
            value = value.replace(/\"/g, "&#34");
            return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
        },
        pdfwidth:100
    },
//    {
//        header :"<span class=memo_custom  wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isTDSApplicable")+"'>"+WtfGlobal.getLocaleText("acc.field.isTDSApplicable")+"</span>",  //'Is TDS Applicable?',
//        dataIndex: 'tdsApplicable',
//        hidden:true,
//        autoSize : true,
//        sortable: true,
//        groupable: true,
//        fixed:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA && !Wtf.isTDSApplicable),
////        hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable,
//        pdfwidth:100
//    },{
//        header :"<span class=memo_custom  wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isChallanGenerated")+"'>"+WtfGlobal.getLocaleText("acc.field.isChallanGenerated")+"</span>",  //'Is Challan Generated ?',
//        dataIndex: 'challanGenerated',
//        hidden:true,
//        fixed:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA && !Wtf.isTDSApplicable),
//        autoSize : true,
//        sortable: true,
//        groupable: true,
////        hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable,
//        pdfwidth:100
//    },
    {//this column is kept as hidden but it has use when we print report. While printing report we have handle this case seperately in exportinterface.js at L.N. 1907
        header :WtfGlobal.getLocaleText("acc.field.DocumentNumber"),
        dataIndex: 'useddocumentnumber',
        hidden : true,
        hideable:false,
        autoWidth : true,
        pdfwidth:100,
        showInPrint:true
    },{
        header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
        dataIndex:'currencycode',
        hidden:true,
        pdfwidth:85
    },{
        header :WtfGlobal.getLocaleText("acc.payment.amtBeforeTax"),  //"Amount Before Tax",
        dataIndex: 'amountBeforeTax',
        align:'right',
        pdfwidth:100,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
    },{
        header :WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),  //"Tax Amount",
        dataIndex: 'totaltaxamount',
        align:'right',
        pdfwidth:100,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
    },{
        header :WtfGlobal.getLocaleText("acc.prList.amtDue"), 
        dataIndex: 'paymentamountdue',
        align:'right',
        pdfwidth:100,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
    },{
        header :WtfGlobal.getLocaleText("acc.invoice.amtDueInBase"), 
        dataIndex: 'paymentamountdueinbase',
        align:'right',
        pdfwidth:100,
        renderer:WtfGlobal.currencyDeletedRenderer
    },{
        header :this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid"),  //"Amount Received":"Amount Paid",
        dataIndex: 'amount',
        align:'right',
        pdfwidth:100,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
    },{
        header:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRecBase"):WtfGlobal.getLocaleText("acc.prList.amtPaidBase"),  //"Amount Received":"Amount Paid",
        align:'right',
        dataIndex:'amountinbase',
        pdfwidth:75,
        renderer:WtfGlobal.currencyDeletedRenderer   
    },{
        header :this.isReceipt?WtfGlobal.getLocaleText("acc.mp.receivedFrom"):WtfGlobal.getLocaleText("acc.mp.paidTo"),  //Received from":" Paid to"
        dataIndex: 'paidto',
       // align:'right',
        pdfwidth:100
      }, {
        header :WtfGlobal.getLocaleText("acc.field.IsTransactionLinked"),
        dataIndex: 'isLinked',
        hidden : !this.isReceipt,
        align:'right',
        pdfwidth:100,
        renderer:function(value){
            if(value)
                return 'Yes';
            else 
                return 'No';
        }
    });
        this.gridColumnModelArr.push({
            header:WtfGlobal.getLocaleText("acc.field.Approval") +WtfGlobal.getLocaleText("acc.invoiceList.status"),  //Approval Status
            dataIndex:'approvalstatusinfo',
            align:'left',
            width:150,
            pdfwidth:100,
            renderer:WtfGlobal.deletedRenderer,
            hidden : !config.pendingapproval
        })

    
    this.gridColumnModelArr.push({
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"),  //"Attach Documents",
            dataIndex:'attachdoc',
            width:150,
            align:'center',
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
            width:150,
            renderer : Wtf.DownloadLink.createDelegate(this)
    });
    if (!this.isReceipt) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.payment.payee"), //"Payee",
            dataIndex: 'payee',
            width: 150,
            pdfwidth: 100
        });
    }
    this.gridColumnModelArr.push({
        header: WtfGlobal.getLocaleText("acc.exportdetails.custven") + " " + WtfGlobal.getLocaleText("acc.field.Code"),
        dataIndex: 'personcode',
        width: 150,
        pdfwidth: 100
    });
    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.gridTDSAmt"), //"TDS Amount",
            dataIndex: 'totaltdsamount',
            align: 'right',
            pdfwidth: 100,
            renderer: WtfGlobal.withoutRateCurrencyDeletedSymbol
        });
    }
    /*
     for payment receivable report ,if condition will be true and array initialize with include child customer (true) and exclude child customer (false)  
                                    else array initialize with include child vendor (true) and exclude child vendor (true)    
     */
    
       var includeExcludeChildCmbData=new Array();     
      if(this.isReceipt){
        includeExcludeChildCmbData.push([WtfGlobal.getLocaleText("acc.includechildcustomer"), true]);
        includeExcludeChildCmbData.push([WtfGlobal.getLocaleText("acc.excludechildcustomer"), false]);
      }else{
        includeExcludeChildCmbData.push([WtfGlobal.getLocaleText("acc.includechildvendor"), true]);
        includeExcludeChildCmbData.push([WtfGlobal.getLocaleText("acc.excludechildvendor"), false]);   
      }
     
    /*
       store for include exclude child combobox
     */
    
    this.includeExcludeChildStore = new Wtf.data.SimpleStore({
        fields: [{
            name: 'name'
        }, {
            name: 'value',
            type: 'boolean'
        }],
        data: includeExcludeChildCmbData
    });

    /*
     include exclude child combobox
     initial value of combobox is set to All and disabled is true for disable purpose
     */
    this.includeExcludeChildCmb = new Wtf.form.ComboBox({
        labelSeparator: '',
        labelWidth: 0,
        triggerAction: 'all',
        mode: 'local',
        store:this.includeExcludeChildStore,
        valueField: 'value',
        displayField: 'name',          
        value: 'All',
        width: 200,
        disabledClass: "newtripcmbss",
        name: 'includeExcludeChildCmb',
        emptyText:'All',
        disabled:true,
        hiddenName: 'includeExcludeChildCmb'
    });
  
    this.personRec = new Wtf.data.Record.create([
        {name: 'accid'},
        {name: 'accname'},
        {name: 'acccode'},
        {name: 'groupname'},
        {name: 'hasAccess'}
    ]);
    /*
     Customer/vendor multi selection Combo store
      
       */
    this.CustomizedCustomerAccStore =  new Wtf.data.Store({   
        url:this.isReceipt?"ACCCustomer/getCustomersIdNameForCombo.do":"ACCVendor/getVendorsIdNameForCombo.do",
        baseParams:{    
                deleted:false,
                nondeleted:true,
                combineData:this.receivable?1:-1  //Send For Seprate Request
        },
        reader: new  Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty: "totalCount"
        },this.personRec)
    });
    /*
     first customer/vendor combobox value is All
      */
     this.CustomizedCustomerAccStore.on("load", function(store){
        if (this.CustomizedName && this.CustomizedName.el.getValue() == "" && this.CustomizedCustomerAccStore.lastOptions.params.start == 0) {
            var storeNewRecord = new this.personRec({
                accid:'All',
                accname:'All',
                acccode:''
            });
            this.CustomizedName.store.insert(0, storeNewRecord);
        }
    },this);
    
    if (this.isReceipt) {
        this.CustomizedName = CommonERPComponent.createCustomerMultiselectPagingComboBox(this, this.CustomizedCustomerAccStore, {});
    } else {
        this.CustomizedName = CommonERPComponent.createVendorMultiselectPagingComboBox(this, this.CustomizedCustomerAccStore, {});
    }
    
    this.CustomizedName.on('select',function(combo,personRec){
        /*
         on selectition of Customer/Vendor enabling includeExcludeChildCmb 
       */
        this.includeExcludeChildCmb.enable(); 
        this.includeExcludeChildCmb.clearValue();
        this.includeExcludeChildCmb.setValue(false);
    } , this);
    
    this.CustomizedName.on('change', function (combo) {
        if (combo.getValue() == "") {
            this.includeExcludeChildCmb.disable();
            this.includeExcludeChildCmb.clearValue();
        }
    }, this);
    
    this.tbar2 = new Array();
   /*
     for payment Receivable report customer combobox and includeExcludeChildCmb combobox will be display
     otherwise vendor combobox and includeExcludeChildCmb combobox will be display
     */
    if(this.isReceipt) 
    {
        this.tbar2.push(WtfGlobal.getLocaleText("acc.up.3"),this.CustomizedName,"-");
        this.tbar2.push(this.includeExcludeChildCmb,"-");
    }
    else                   
    {
        this.tbar2.push(WtfGlobal.getLocaleText("acc.up.4"),this.CustomizedName,"-");
        this.tbar2.push(this.includeExcludeChildCmb,"-");

    }
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,this.fetchBttn);
    if(!config.pendingapproval){
        this.tbar2.push(this.RepeatPayment);
    }
    this.gridView1 = config.consolidateFlag?new Wtf.grid.GroupingView({
        forceFit:false,
        showGroupName: true,
        enableNoGroups:false, // REQUIRED!
        hideGroupedColumn: true,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    }):{
        forceFit:false,
        emptyText:WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openRecNew(\""+this.isReceipt+"\")'>"+(this.isReceipt?WtfGlobal.getLocaleText("acc.nee.24"):WtfGlobal.getLocaleText("acc.nee.25"))+"</a>")
    };
        
    // appening custom columns
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[this.moduleid],true);
    this.gridBbar=new Array();
    this.totalTextValue = new Wtf.Toolbar.TextItem(""); // total value
    this.gridBbar.push('->',"<B>" +WtfGlobal.getLocaleText("acc.common.grandtotalinbase")+ ":</B>",this.totalTextValue)
    this.totalTextValue.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(0,WtfGlobal.getCurrencySymbol()) +"</B>"; // Setting Default value as 0
    this.grid = new Wtf.grid.GridPanel({
        plugins: this.expander,
        cls:'vline-on',
        tbar:this.tbar2,
        id:"gridmsg"+config.helpmodeid+config.id,
        layout:'fit',
        autoScroll:true,
        store: this.userds,
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr),
        sm :this.sm,
        border : false,
        loadMask : true,
        viewConfig: this.gridView1,
        bbar:this.gridBbar
    });  
    this.grid.getColumnModel().defaultSortable = false;
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleid,
        advSearch: false,
        isAvoidRedundent:true, //Hide Product master custom column.
        customerCustomFieldFlag: this.showCustomerCustomFieldFlag(this.moduleid),
        vendorCustomFieldFlag: this.showVendorCustomField(this.moduleid)
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    this.pendingApprovalBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
        tooltip : WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
        id: 'pendingApprovals' + config.id,
        scope: this,
        handler :this.openPendingApprovalTab,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport)
    });    
    
    
    this.approveNoteBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.cc.24"),
        tooltip : WtfGlobal.getLocaleText("acc.field.ApprovependingPurchaseRequisitions"), //Issue 31009 - [Pending Approval]Window name should be "Approve Pending Invoice" instead of "Approve Pending Approval". it should also have deskera logo
        id: 'approvepending' + this.id,
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.add),
        disabled :true,
        handler :this.approvePendingNote
    });
    
    this.rejectNoteBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Reject"),
        tooltip : WtfGlobal.getLocaleText("acc.field.Rejectpending"),
        id: 'rejectpending' + this.id,
        scope: this,
        iconCls:getButtonIconCls(Wtf.etype.deletebutton),
        disabled :true,
        handler : this.handleReject
    });
    
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:(this.isReceipt)?WtfGlobal.getLocaleText("acc.field.SearchbyReceiptnoJournalntryAccountName"):WtfGlobal.getLocaleText("acc.field.SearchbyPaymentnoJournalEntrynoAccountNameChequeNo"),  //'Search by Account Name...',
                width: 200,      //ERP-12381 [SJ]
                id: this.pendingapproval? "quickSearch"+config.helpmodeid+this.panelID : "quickSearch"+config.helpmodeid,   // 32932 Issue in helpmodeid when opened view pending approcal tabs of MP/RP navigation panel
                field: 'receiptnumber'
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    
   this.newTabButton=getCreateNewButton(config.consolidateFlag,this,WtfGlobal.getLocaleText("acc.WoutI.26"),this.reportbtnshwFlag);
    this.newTabButton.on('click',this.openNewTab,this); 
    
    /*Edit button will be shown in pending tab of
     * 1.Make Payment :- Yes
     * 2.Receive Payment :- No 
     */
     var isShowEditBttn = true;
    if(config.pendingapproval && this.moduleid == Wtf.Acc_Receive_Payment_ModuleId){
        isShowEditBttn = false;
    }
    this.resetBttn.on('click',this.handleResetClick,this);
    if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.editPermType) && Wtf.account.companyAccountPref.editTransaction && isShowEditBttn){
                   btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
                    text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
                    tooltip :(this.isReceipt?WtfGlobal.getLocaleText("acc.prList.editTT"):WtfGlobal.getLocaleText("acc.pmList.editTT")),  //'Allows you to edit Receipt.',
                    id: 'btnEdit' + this.id,
                    scope: this,
                    hidden: (!this.fromNavigationPanel)?(config.consolidateFlag || this.reportbtnshwFlag) : this.reportbtnshwFlag ? true : false, //SDP-12096
                    iconCls :getButtonIconCls(Wtf.etype.edit),
                    disabled :true
                    }));
                this.editBttn.on('click',this.editTransaction,this);
    }
               
//    if(!WtfGlobal.EnableDisable(this.uPermType, this.copyPermType) && !this.pendingapproval){
    if(!this.isOrder){
       if(!WtfGlobal.EnableDisable(this.uPermType,this.copyPermType)&&!config.pendingapproval){
        btnArr.push(this.copyReceiptBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.copy"),
            tooltip :WtfGlobal.getLocaleText("acc.field.CopyRecord"),
            id: 'btnCopy' + this.id,
            scope: this,
            hidden:this.reportbtnshwFlag || this.isOrder,
            iconCls :getButtonIconCls(Wtf.etype.copy),
            disabled :true
        }));
        this.copyReceiptBttn.on('click',this.copyTransaction,this);
       }
    }
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.dishonouredCheque, Wtf.Perm.dishonouredCheque.dishonouredChequeView)){
            if(!config.pendingapproval){
                btnArr.push(this.chequeDishonoured=new Wtf.Toolbar.Button({
                    text:WtfGlobal.getLocaleText("acc.filed.MarkChequeDishonoured"),
                    tooltip :WtfGlobal.getLocaleText("acc.filed.MarkChequeDishonoured"),
                    id: 'btnChequeDishonoured' + this.id,
                    scope: this,
                    iconCls :getButtonIconCls(Wtf.etype.copy),
                    disabled :true
                }));
                this.chequeDishonoured.on('click',this.AddChequeDishonoured,this);
            }
        }
    
    var deletebtnArray=[];
    if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction&&!config.pendingapproval){
                   deletebtnArray.push(this.deleteTrans=new Wtf.Action({
                    text: (this.isReceipt?WtfGlobal.getLocaleText("acc.prList.delete"):WtfGlobal.getLocaleText("acc.pmList.delete")),
                    scope: this,
                    disabled :true,
                    hidden: config.consolidateFlag,
                    tooltip: (this.isReceipt?WtfGlobal.getLocaleText("acc.prList.deleteTT"):WtfGlobal.getLocaleText("acc.pmList.deleteTT")),
                    iconCls:getButtonIconCls(Wtf.etype.menudelete),
                    handler:this.handleDelete.createDelegate(this,this.del=["del"])
                    }));
                }
     if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction){
                    deletebtnArray.push(this.deleteTransPerm=new Wtf.Action({
                    text: (this.isReceipt?WtfGlobal.getLocaleText("acc.prList.delete"):WtfGlobal.getLocaleText("acc.pmList.delete"))+" "+WtfGlobal.getLocaleText("acc.field.Permanently"),
                    scope: this,
                    disabled :true,
                    hidden: config.consolidateFlag || this.reportbtnshwFlag,
                    tooltip: (this.isReceipt?WtfGlobal.getLocaleText("acc.prList.deleteTT"):WtfGlobal.getLocaleText("acc.pmList.deleteTT"))+WtfGlobal.getLocaleText("acc.field.Permanently"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
                    iconCls:getButtonIconCls(Wtf.etype.menudelete),
                    handler:this.handleDelete.createDelegate(this,this.del=["delp"])
                    }));
                }  
               
 if(deletebtnArray.length>0) {
        btnArr.push(this.deleteMenu = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"), 
            scope: this,
            hidden: this.reportbtnshwFlag,
            tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"), 
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            menu:deletebtnArray
       }));
  }
  //  btnArr.push('-');
    if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType)){
    //    btnArr.push('-');
        gridBtnArr.push(this.email=new Wtf.Toolbar.Button({      //ERP-12381 [SJ]
            text: WtfGlobal.getLocaleText("acc.common.email"),  //"Email",
            tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
            scope: this,
            hidden: this.isOrder || config.consolidateFlag,
            disabled: true,
            iconCls : "accountingbase financialreport",
            handler : this.sendMail
        }));
    }
   /*
    * Button For Import
    */
   if (this.moduleid == Wtf.Acc_Receive_Payment_ModuleId || this.moduleid == Wtf.Acc_Make_Payment_ModuleId) {
        var extraConfig = this.getImportExtraConfigForModule(this.moduleid);
        var extraParams = this.getImportExtraParamsForModule(this.moduleid);
        var importBtnArray = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(this.moduleid), this.Store, extraParams, extraConfig);
        var importButton = Wtf.documentImportMenuButtonA(importBtnArray, this, this.getModuleNameForImport(this.moduleid));
        this.createImportButtonsForReceivepayment(importBtnArray, bottombtnArr);
    }
    
    bottombtnArr.push('-',
        this.linkinfoViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.LinkInformationDetails"),     //button for showing link information
            scope: this,
            disabled : true,
            tooltip: WtfGlobal.getLocaleText("acc.field.LinkInformationDetails"),
            handler:function(){linkinfo(undefined,undefined,undefined,undefined,undefined,undefined,undefined,undefined,this)},
            iconCls:'accountingbase fetch'
        })
        );
    bottombtnArr.push('-',
            this.relatedTransactionsBtn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.RelatedTransaction(s)"), //button for showing all related linking with particular document
                scope: this,
                disabled: true,
                tooltip: WtfGlobal.getLocaleText("acc.field.RelatedTransaction(s).tooltip"),
                handler: function() {
                    linkPurchaseReportTab(this.isReceipt? 1 : 0, this.moduleid, this.grid.getSelectionModel().getSelected().data.billno);
                },
                iconCls: 'accountingbase fetch'
            }));
    
    bottombtnArr.push('-',
        this.exportchallan281 = new Wtf.Toolbar.Button({
            text: "Export Challan 281",
            scope: this,
            hidden: true,//Wtf.Countryid == "105" ? false : true,
            disabled: true,
            tooltip: WtfGlobal.getLocaleText("acc.field.LinkInformationDetails"),
            handler: function () {
                var recArr = this.grid.getSelectionModel().getSelections();
                var arr = [];
                for (var i = 0; i < recArr.length; i++) {
                    arr.push(recArr[i].data.billid);
                }
                var url = "ACCInvoiceCMN/exportChallanNo281Report.do?paymentaccountid=" + arr;
                Wtf.get('downloadframe').dom.src = url;
            },
            iconCls: 'accountingbase fetch'
        })
        );
   if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)&&!config.pendingapproval){
   //     btnArr.push('-');
        bottombtnArr.push('-', this.printButton=new Wtf.exportButton({
            obj:this,
            id:"exportReports"+config.helpmodeid,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            usePostMethod:true,
            filename:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.tabTitle")+"_v1":WtfGlobal.getLocaleText("acc.pmList.tabTitle")+"_v1", 
//            menuItem:this.isReceipt?{csv:true,pdf:true,detailedXls:true,xls:true}:{csv:true,pdf:true,xls:true},
            menuItem:{csv:true,pdf:true,print:true,detailedXls:true,xls:true},
            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment),
            moduleId:config.moduleid,
            params:{
                isCustomReportForF1Recreation : this.isReceipt && ( Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag)?true:false   //This param's are set because f1 recreation have their own report for Receive Payment. [Mayur Bhokase]
        }
        }));
    }
   if((!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.printPermType) || !WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))&&!config.pendingapproval){
        bottombtnArr.push('-', this.singlePrint=new Wtf.exportButton({
            obj:this,
            id:"printReports"+config.helpmodeid+config.id,
            iconCls: 'pwnd printButtonIcon',
            text: WtfGlobal.getLocaleText("acc.rem.236"),//Export single Record(Jasper)
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),
            disabled :true,
            filename:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.tabTitle"):WtfGlobal.getLocaleText("acc.pmList.tabTitle"), 
            isEntrylevel:false,
            onlyOpeningBalanceTransactionsFlag : this.onlyOpeningBalanceTransactionsFlag,
            menuItem:{
                rowPdf:true,
                rowPdfPrint:true,
                rowPdfTitle:(WtfGlobal.getLocaleText("acc.rem.39")+" "+(this.isReceipt?WtfGlobal.getLocaleText("acc.receipt.1"):WtfGlobal.getLocaleText("acc.receipt.2")))
                },// + " "+ (this.isReceipt?WtfGlobal.getLocaleText("acc.invoiceList.recPay"):WtfGlobal.getLocaleText("acc.invoiceList.mP"))},
            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment),
            moduleId:config.moduleid
        }));
   }
   // btnArr.push('-');
//  if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
//   //    btnArr.push('-');
//        bottombtnArr.push('-', this.exportButton=new Wtf.exportButton({
//            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
//            obj:this,
//            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
//            disabled :true,
//            filename:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.tabTitle"):WtfGlobal.getLocaleText("acc.pmList.tabTitle"), 
//            menuItem:{print:true},
//            params:{name:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.tabTitle"):WtfGlobal.getLocaleText("acc.pmList.tabTitle")},
//            label:this.isReceipt?WtfGlobal.getLocaleText("acc.receipt.1"):WtfGlobal.getLocaleText("acc.receipt.2"),
//            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment),
//            moduleId:this.moduleid,
//            ispendingApproval:this.pendingapproval      //used to print pending receipt and payment
//        }));
//
//        bottombtnArr.push('-', this.singleRowPrint=new Wtf.exportButton({
//            obj:this,
//            id:"printSingleRecord"+config.helpmodeid+config.id,
//            iconCls: 'pwnd printButtonIcon',
//            text:WtfGlobal.getLocaleText("acc.rem.236"),
//            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
//            disabled :true,
//            filename:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.tabTitle"):WtfGlobal.getLocaleText("acc.pmList.tabTitle"), 
//            isEntrylevel:false,
//            menuItem:{rowPrint:true},
//            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment),
//            moduleid:config.moduleid
//        }));
//    }
    if(!config.pendingapproval){
        gridBtnArr.push(this.linkTrans=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.LinkTransaction"),
            scope: this,
            disabled :true,
            hidden: config.consolidateFlag,
            tooltip: WtfGlobal.getLocaleText("acc.field.LinkTransaction"),
            iconCls:'accountingbase pricelistbutton'
        }));
        this.linkTrans.on('click',this.calllinkInvoiceNew,this);
    }

    if(!config.pendingapproval){
        gridBtnArr.push(this.unlinkTrans=new Wtf.Toolbar.Button({   //ERP-12381 [SJ]
            text: WtfGlobal.getLocaleText("acc.field.UnLinkTransaction"),
            scope: this,
            disabled :true,
            hidden: config.consolidateFlag,
            tooltip: WtfGlobal.getLocaleText("acc.field.UnLinkTransaction"),
            iconCls:'accountingbase pricelistbutton'
        }));
        this.unlinkTrans.on('click',this.callluninkInvoiceNew,this);
    }
    if(!config.pendingapproval){
        gridBtnArr.push(this.printCheck=new Wtf.Toolbar.Button({     //ERP-12381 [SJ]
            text: WtfGlobal.getLocaleText("acc.common.chequePrint"),
            scope: this,
            disabled :true,
            hidden: !(this.moduleid==Wtf.Acc_Make_Payment_ModuleId),
            tooltip: WtfGlobal.getLocaleText("acc.common.chequePrint"),
            iconCls:'accountingbase pricelistbutton'
        }));
        this.printCheck.on('click',this.printCkeck,this); 
    }
    if(config.pendingapproval){
        btnArr.push(this.approveNoteBttn);
        btnArr.push(this.rejectNoteBttn);
    } else {
            btnArr.push(this.pendingApprovalBttn);
        }
    gridBtnArr.push( this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
//        hidden:(this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId)?false:true,
        scope: this,
        handler: function() {
            if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
                this.expandButtonClicked = true;
            }
            expandCollapseGrid(this.expandCollpseButton.getText(), this.expandStore, this.grid.plugins, this);
        }
    }));
    
    gridBtnArr.push(this.approvalHistoryBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.ApprovalHistory"),
        scope: this,
        disabled : true,
        tooltip: WtfGlobal.getLocaleText("acc.field.ViewApprovalHistory"),
        handler: this.viewApprovalHistory,
        iconCls: "advanceSearchButton"
    }));
    
    if(Wtf.isLMSSync && this.moduleid==Wtf.Acc_Receive_Payment_ModuleId){ //condition = LMS is subscibed flag is true and Receipt report
        gridBtnArr.push(this.syncReceiptFromLMS=new Wtf.Toolbar.Button({    
            text: WtfGlobal.getLocaleText("acc.common.syncReceiptFromLMS"),
            scope: this,                   
            tooltip: WtfGlobal.getLocaleText("acc.common.syncReceiptFromLMS"),
            iconCls: getButtonIconCls(Wtf.etype.sync), 
            handler:this.syncReceiptFromLMS
        }));  
    }   
    
    var tbarArray = [this.quickPanelSearch, this.resetBttn, this.newTabButton,btnArr, this.AdvanceSearchBtn, '->',WtfGlobal.getLocaleText("acc.lp.view") ,this.typeEditor, '-'];
    if(!this.pendingapproval){  //ERP-39732
        tbarArray.push(getHelpButton(this, config.helpmodeid));
    }
    
if(config.recordType!=undefined)
        this.setRecordType(config.recordType);
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
//                tbar: [this.quickPanelSearch, this.resetBttn, this.newTabButton,btnArr, this.AdvanceSearchBtn, '->',WtfGlobal.getLocaleText("acc.lp.view") ,this.typeEditor, '-', !this.pendingapproval ? getHelpButton(this, config.helpmodeid) : ""],
                tbar : tbarArray,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.userds,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                    items: bottombtnArr
                })
            }]
    });
    this.tbar2.push(gridBtnArr);    //ERP-12381 [SJ]
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.leadpan]
    });
   
    Wtf.account.ReceiptReportNew.superclass.constructor.call(this,config);
    this.addEvents({
        'invoice':true,
        'journalentry':true,
        'paymentupdate' : true
    });
    this.getMyConfig();
    this.on('paymentupdate', function(){
        this.loadStore();
    }, this);
    
    this.userds.on('datachanged', function(store) {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
        var grandTotalInBaseCurrency= 0;  
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
        this.expander.resumeEvents('expand');           // event is suspended while expanding all records.
        for(var i=0;i <= store.data.length-1;i++){ // calculating total in base currency- ERP- 10495
            grandTotalInBaseCurrency= parseFloat(grandTotalInBaseCurrency) + parseFloat(store.getAt(i).get('amountinbase'));
        }
        // setting calculated grand total in base currency to total text field
        this.totalTextValue.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(grandTotalInBaseCurrency,WtfGlobal.getCurrencySymbol()) +"</B>"; 
     }, this);
    this.userds.on('load',this.storeloaded,this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    this.grid.on('cellclick',this.onCellClick, this);
    this.grid.flag = 0;
    this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
    this.grid.on('render', function() {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);
    this.userds.load({params:{start:0,limit:30}});
    this.typeEditor.on('select',this.loadTypeStore,this);
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
}
Wtf.extend(Wtf.account.ReceiptReportNew,Wtf.Panel,{
    syncReceiptFromLMS: function() {
        if (!Wtf.account.companyAccountPref.isLMSIntegration) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.lmsnotacivatedalert")], 2);
        } else {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText("acc.common.receiptSyncConfirmationMessage"), //Are you sure you want to sync Receipts from LMS?       
                buttons: Wtf.MessageBox.OKCANCEL,
                animEl: 'upbtn',
                icon: Wtf.MessageBox.QUESTION,
                scope: this,
                fn: function(btn) {
                    if (btn == "ok") {
                        WtfGlobal.setAjaxTimeOut();
                        var param = {
                            deleted: this.deleted,
                            nondeleted: this.nondeleted
                        }
                        Wtf.Ajax.requestEx({
                            url: "ACCRemote/getReceiptFromLMS.do",
                            param: param
                        }, this, this.genSuccessResponseforSync, this.genFailureResponseforSync);
                    }
                }
            });
        }
    },
    genSuccessResponseforSync: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        if (response.success) {
//            this.loadStore();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.receipt.1"), WtfGlobal.getLocaleText("acc.common.receiptsyncedmessage")], response.success * 2 + 1);          
        } else {        
            var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg]);         
        }
    },
    genFailureResponseforSync: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg]);
    },
    getImportExtraConfigForModule: function(moduleid) {
        var extraConfig = {};
        extraConfig.url = moduleid == Wtf.Acc_Receive_Payment_ModuleId ? "ACCReceiptNew/importReceivePayment.do" : "ACCVendorPaymentNew/importMakePayment.do";
//        extraConfig.url = moduleid == Wtf.Acc_Receive_Payment_ModuleId ? "ACCReceiptNew/importReceivePayment.do" : "ACCImportFunctionality/createReceiveCSVFile.do";       //for testing purpose
        extraConfig.isExcludeXLS = true;
        return extraConfig;
    },
    getImportExtraParamsForModule: function(moduleid) {
        var extraParams = "";
        return extraParams;
    },
    getModuleNameForImport: function(moduleid) {
        var moduleName = "";
        moduleName = moduleid == Wtf.Acc_Receive_Payment_ModuleId ? "Receipt" : "Payment";
        return moduleName;
    },
    createImportButtonsForReceivepayment: function(importBtnArray, bottombtnArr) {
        var importBtnArr = [];
        /*
        * Receive payment or Make Payment against Customer
        */        
        var extraConfigCustomer = this.getImportExtraConfigForModule(this.moduleid);
        var extraParamsCustomer = this.getImportExtraParamsForModule(this.moduleid);
        extraConfigCustomer.customer=true;
        var importBtnArrayCustomer = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(this.moduleid), this.Store, extraParamsCustomer, extraConfigCustomer);

        this.importCustomerButton = new Wtf.Action({
            text: this.moduleid == Wtf.Acc_Receive_Payment_ModuleId ? WtfGlobal.getLocaleText("acc.field.ImportReceivePaymentfromCustomer") : WtfGlobal.getLocaleText("acc.field.ImportMakePaymentfromCustomer"),
            scope: this,
            tooltip: this.moduleid == Wtf.Acc_Receive_Payment_ModuleId ? WtfGlobal.getLocaleText("acc.field.ImportReceivePaymentfromCustomer") : WtfGlobal.getLocaleText("acc.field.ImportMakePaymentfromCustomer"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayCustomer
        });
        importBtnArr.push(this.importCustomerButton);

        /*
        * Receive payment or Make Payment against Vendor
        */
        
        var extraConfigVendor = this.getImportExtraConfigForModule(this.moduleid);
        var extraParamsVendor = this.getImportExtraParamsForModule(this.moduleid);
        var importBtnArrayVendor = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(this.moduleid), this.Store, extraParamsVendor, extraConfigVendor);
        extraConfigVendor.vendor=true;
        
        this.importVendorButton = new Wtf.Action({
            text: this.moduleid == Wtf.Acc_Receive_Payment_ModuleId ? WtfGlobal.getLocaleText("acc.field.ImportReceivePaymentfromVendor") : WtfGlobal.getLocaleText("acc.field.ImportMakePaymentfromVendor"),
            scope: this,
            tooltip: this.moduleid == Wtf.Acc_Receive_Payment_ModuleId ? WtfGlobal.getLocaleText("acc.field.ImportReceivePaymentfromVendor") : WtfGlobal.getLocaleText("acc.field.ImportMakePaymentfromVendor"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayVendor
        });
        importBtnArr.push(this.importVendorButton);

       /*
        * Receive payment or Make Payment against GL
        */
        var extraConfigGL = this.getImportExtraConfigForModule(this.moduleid);
        var extraParamsGL = this.getImportExtraParamsForModule(this.moduleid);
        var importBtnArrayGL = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(this.moduleid), this.Store, extraParamsGL, extraConfigGL);
        extraConfigGL.GL=true;
        
        this.importGL = new Wtf.Action({
            text: this.moduleid == Wtf.Acc_Receive_Payment_ModuleId ? WtfGlobal.getLocaleText("acc.field.ImportReceivePaymentfromGL") : WtfGlobal.getLocaleText("acc.field.ImportMakePaymentfromGL"),
            scope: this,
            tooltip:this.moduleid == Wtf.Acc_Receive_Payment_ModuleId ? WtfGlobal.getLocaleText("acc.field.ImportReceivePaymentfromGL") : WtfGlobal.getLocaleText("acc.field.ImportMakePaymentfromGL"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayGL
        });
        importBtnArr.push(this.importGL);

        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu: importBtnArr
        });
        bottombtnArr.push(this.importBtn);
    },
    globalInvoiceListGridAutoRefreshPublishHandler: function(response) {
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable){
            var res = eval("(" + response.data + ")");
            if (res.success && ( Wtf.isAutoRefershReportonDocumentSave || (res.userSessionId != undefined && Wtf.userSessionId==res.userSessionId ))) {
                if (this.userds.baseParams && this.userds.baseParams.searchJson) {
                    this.userds.baseParams.searchJson = "";
                }
                this.userds.load({
                    params : {
                        start:0,
                        limit:this.pP.combo.value,
                        pagingFlag:true
                    }
                });
                if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId){
                    Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.MakePaymentRefreshedmsg"));
                }
            }
        }
    },
   loadTypeStore:function(a,rec){
        if(this.startDate)
            this.startDate.setValue(WtfGlobal.getDates(true));
        if(this.endDate)
            this.endDate.setValue(WtfGlobal.getDates(false));
       this.showBarButtons();
       this.recordType=undefined;
       if (this.reportbtnshwFlag) {
            this.newTabButton.hide();
       }
       if (!this.reportbtnshwFlag) {
           if (this.editBttn) {
               this.editBttn.show();
           }
           if (this.copyReceiptBttn) {
               this.copyReceiptBttn.show();
           }
           if(this.deleteMenu)this.deleteMenu.show();
           if(this.deleteTrans)this.deleteTrans.show();
           if(this.deleteTransPerm)this.deleteTransPerm.show();
        }
        if(this.email)this.email.show();
        if(this.linkTrans)this.linkTrans.show();
        if(this.unlinkTrans)this.unlinkTrans.show();
        if(this.singlePrint)this.singlePrint.show();
        if(this.singleRowPrint)this.singleRowPrint.show();
        this.deleted=false;
        this.contraentryflag=false;
        this.onlyOpeningBalanceTransactionsFlag=false;
        this.advancePayment=false;
        this.advanceFromVendor=false;
        this.advanceToCustomer=false;
        this.isPostDatedCheque=false;
        this.isDishonouredCheque=false;
       
        this.allAdvPayment=false;
        this.unUtilizedAdvPayment = false;
        this.partiallyUtilizedAdvPayment = false;
        this.nonorpartiallyUtilizedAdvPayment = false;
        this.fullyUtilizedAdvPayment = false;
        
       this.nondeleted=false;
        this.isGlcode=false;    //for  Advance payment against GL code
        this.mainReport=true;
        var index=rec.data.typeid;
        if(this.deleteTrans){
            this.deleteTrans.enable();
        }
        if(this.deleteTransPerm){
            this.deleteTransPerm.enable();
        }
        
        if(index==2) // non deleted
            this.nondeleted=true;
        else if(index==1){ // deleted records
             this.deleted=true;
           this.deleteTrans.disable();
          this.deleteTransPerm.disable();
        }else if(index==3){ // Advance Payments
           // this.nondeleted=true;
          //  this.advancePayment = true;        
          this.recordType=3;
        }else if(index==5){ // Advance Payments
           // this.nondeleted=true;
            this.isPostDatedCheque = true;
        }else if(index==6){ // Advance Receipts From Vendor
          //  this.nondeleted=true;
          //  this.advanceFromVendor = true;
          //  this.advanceToCustomer = true;
          this.recordType=6;
        }else if(index==9){ // Advance payment against GL code
           this.recordType=9;
        } else if(index==10){
            if(this.recordType==undefined) {
                this.recordType="10";
            }   
        }else if(index==11){
            if(this.recordType==undefined) {
                this.recordType="11";
            } 
        }else if(index==12){
            if(this.recordType==undefined) {
                this.recordType="12";
            } 
        }else if(index==13){
            if(this.recordType==undefined) {
                this.recordType="13";
            }
        } else if(index==15){ // Opening Balance RP Records
            if(this.startDate)
                this.startDate.setValue(this.getLastFinancialYRStartDate(true));
            if(this.endDate)
                this.endDate.setValue(WtfGlobal.getOpeningDocumentDate(true));
            
            this.onlyOpeningBalanceTransactionsFlag = true;
            if (!this.reportbtnshwFlag){
                this.hideButtons();       //To hide the buttons of Opening Balance RP Records
            }
        }else if(index==17){
            this.allAdvPayment=true;
        }else if(index==18){
            this.unUtilizedAdvPayment = true;
        }else if(index==19){
             this.partiallyUtilizedAdvPayment = true;
        }else if(index==20){
             this.fullyUtilizedAdvPayment = true;
        }else if(index==21){
            this.nonorpartiallyUtilizedAdvPayment=true;
        }
        
        if(index==14){ // Dishonoured Cheques
            this.isDishonouredCheque = true;
           if(this.deleteTransPerm) 
                this.deleteTransPerm.setText("Dishonoured");
            this.newTabButton.hide();
            this.AdvanceSearchBtn.hide();
            this.editBttn.hide();
            this.email.hide();
            this.unlinkTrans.hide();
            this.linkTrans.hide();
            this.singlePrint.hide();
        } else {
            if(this.isReceipt)
               if(this.deleteTransPerm) 
                    this.deleteTransPerm.setText((WtfGlobal.getLocaleText("acc.prList.delete"))+" "+WtfGlobal.getLocaleText("acc.field.Permanently"));
            else
                this.deleteTransPerm.setText(WtfGlobal.getLocaleText("acc.pmList.delete"));
        }
        if(index==4) {//Contra Entry
            this.contraentryflag = true;
            if(this.editBttn)this.editBttn.hide();
            if(this.deleteTrans)this.deleteTrans.hide();
            if(this.deleteTransPerm)this.deleteTransPerm.hide();
            if(this.email)this.email.hide();
            if(this.linkTrans)this.linkTrans.hide();
            if(this.unlinkTrans)this.unlinkTrans.hide();
            if(this.singlePrint)this.singlePrint.hide();
            if(this.singleRowPrint)this.singleRowPrint.hide();
//            this.grid.getColumnModel().setHidden('paymentmethod',true) ;
            this.grid.getColumnModel().setHidden(8,true) ;//Payment method
            this.grid.getColumnModel().setColumnHeader(10,WtfGlobal.getLocaleText("acc.field.AmountAdjusted")) ;//Amount
        } else if (index === 9) {  //Advance payment against GL code
            var colIndex = -1;
            colIndex = this.getColumnIndex('personcode');
            if (colIndex != -1) {
                this.grid.getColumnModel().setHidden(colIndex, true);
            }
        } else {
            var colIndex = -1;
            colIndex = this.getColumnIndex('personcode');
            if (colIndex != -1) {
                this.grid.getColumnModel().setHidden(colIndex, false);
            }
            this.grid.getColumnModel().setHidden(8,false) ;//Payment method
            this.grid.getColumnModel().setColumnHeader(10,this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")) ;//Amount Paid
        }
        this.userds.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value,
                    ss : this.quickPanelSearch.getValue()
                }
            });
            WtfComMsgBox(29,4,true);
        this.userds.on('load',this.storeloaded,this);
    },
    
    hideButtons:function(){
        if(this.deleteMenu)
            this.deleteMenu.hide();
        if(this.newTabButton)
            this.newTabButton.hide();
//        if(this.AdvanceSearchBtn)
//            this.AdvanceSearchBtn.hide();
        if(this.editBttn)
            this.editBttn.hide();
        if(this.deleteTransPerm)
            this.deleteTransPerm.hide();
        if(this.email)
            this.email.hide();
        if(this.printButton)
            this.printButton.show();    //Print all records.
        if(this.singlePrint)
            this.singlePrint.show();    //Export record in Jasper
//        if(this.exportButton)
//            this.exportButton.show();   //Export to CSV/PDF.
        if (this.copyReceiptBttn) {
            this.copyReceiptBttn.hide();
        }
    },
    
    showBarButtons:function(){
        if(this.newTabButton)
            this.newTabButton.show();
        if(this.AdvanceSearchBtn)
            this.AdvanceSearchBtn.show();
        if(this.printButton)
            this.printButton.show();
//        if(this.exportButton)
//            this.exportButton.show();
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
    
    getLastFinancialYRStartDate:function(start){
//        var d=Wtf.serverDate;
//        //        if(this.statementType=='BalanceSheet'&&start)
//        //             return new Date('January 1, 1970 00:00:00 AM');
//        var monthDateStr=d.format('M d');
//        if(Wtf.account.companyAccountPref.fyfrom)
//            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
//        var fd=new Date(monthDateStr+', '+1975+' 12:00:00 AM');
//        if(d<fd)
//            fd=new Date(monthDateStr+', '+1975+' 12:00:00 AM');
        var fd = new Date('January 1' + ', ' + 1975 + ' 12:00:00 AM');
        if (start) {
            return fd.add(Date.YEAR,0);
        } 
    },
    
    setRecordType:function(recordType){
        this.typeEditor.setValue(recordType);
         var index=recordType;
//      if(this.deleteTransPerm)     
//        this.deleteTransPerm.enable();
        if(index==2) // non deleted
            this.nondeleted=true;
        else if(index==1){ // deleted records
             this.deleted=true;
           if(this.deleteTransPerm)  
                this.deleteTransPerm.disable();
        }else if(index==3){ // Advance Payments
          //  this.nondeleted=true;
            this.advancePayment = true;        
        }else if(index==5){ // Advance Payments
          //  this.nondeleted=true;
            this.isPostDatedCheque = true;
        }else if(index==6){ // Advance Receipts From Vendor
          //  this.nondeleted=true;
            this.advanceFromVendor = true;
            this.advanceToCustomer = true;
        }
        
        if(index==14){ // Dishonoured Cheques
            this.isDishonouredCheque = true;
          if(this.deleteTransPerm)    
                this.deleteTransPerm.setText("Dishonoured");
            this.newTabButton.hide();
            this.AdvanceSearchBtn.hide();
            if(this.editBttn)this.editBttn.hide();
            this.email.hide();
            this.linkTrans.hide();
            this.unlinkTrans.hide();
            this.singlePrint.hide();
        } 
//        else {
//            if(this.isReceipt){
//                if(this.deleteTransPerm)   
//                    this.deleteTransPerm.setText((WtfGlobal.getLocaleText("acc.prList.delete"))+" "+WtfGlobal.getLocaleText("acc.field.Permanently"));
//            } else{
//                if(this.deleteTransPerm)  
//                    this.deleteTransPerm.setText(WtfGlobal.getLocaleText("acc.pmList.delete"));
//        }
//        }
    
        if(index==4) {//Contra Entry
            this.contraentryflag = true;
            if(this.editBttn)this.editBttn.hide();
            if(this.deleteTransPerm)this.deleteTransPerm.hide();
            if(this.email)this.email.hide();
            if(this.linkTrans)this.linkTrans.hide();
            if(this.unlinkTrans)this.unlinkTrans.hide();
            if(this.singlePrint)this.singlePrint.hide();
//            this.grid.getColumnModel().setHidden('paymentmethod',true) ;
            var paymentmethodindex=this.grid.getColumnModel().findColumnIndex("paymentmethod");
            var amountindex=this.grid.getColumnModel().findColumnIndex("amount");
            this.grid.getColumnModel().setHidden(paymentmethodindex,true) ;//Payment method //
            this.grid.getColumnModel().setColumnHeader(amountindex,WtfGlobal.getLocaleText("acc.field.AmountAdjusted")) ;//Amount
        } else {
            var paymentmethodindex=this.grid.getColumnModel().findColumnIndex("paymentmethod");
            var amountindex=this.grid.getColumnModel().findColumnIndex("amount");
            this.grid.getColumnModel().setHidden(paymentmethodindex,false) ;//Payment method
            this.grid.getColumnModel().setColumnHeader(amountindex,this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")) ;//Amount Paid
        }
        
    },
    getColumnIndex: function (dataIndex) {   //This is used to get index of perticular Header.
        var colIndex = 0;
        for (colIndex; colIndex < this.grid.getColumnModel().getColumnCount(); colIndex++) {
            if (this.grid.getColumnModel().getDataIndex(colIndex) === dataIndex) {
                return colIndex;
            }
        }
        return -1;
    },
    enableDisableButtons:function(){
         Wtf.uncheckSelAllCheckbox(this.sm);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){ 
        if(this.deleteTrans)this.deleteTrans.enable();
        if(this.deleteTransPerm)this.deleteTransPerm.enable();
       // var arr=this.grid.getSelectionModel().getSelections();
//        for(var i=0;i<arr.length;arr++){
//            if(arr[i]&&arr[i].data.deleted)
//                this.deleteTrans.disable();
        }
        var arr=this.grid.getSelectionModel().getSelections();
        if(arr.length==0&&!WtfGlobal.EnableDisable(this.uPermType,this.removePermType)){
            if(this.deleteTrans){
                this.deleteTrans.disable();
            }
            if(this.deleteTransPerm){
                this.deleteTransPerm.disable();
            }
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            for(var i=0;i<arr.length;i++){
                if(arr[i]&&arr[i].data.deleted){
                    if(this.deleteTrans){
                        this.deleteTrans.disable();
                    }
                    if(this.deleteTransperm){
                        this.deleteTransPerm.enable();
                    }
                }
            }
        }

        var rec = this.sm.getSelected();
        if(this.sm.getCount()==1 && rec.data.deleted != true){
            var recData = rec.data;
            if(this.email)this.email.enable();
            if(this.editBttn && !(rec.data.receipttype == 6 || rec.data.receipttype == 7)&& (rec.data.invoiceadvcndntype != 3))this.editBttn.enable();
            if(this.copyReceiptBttn)this.copyReceiptBttn.enable();
            if(this.approvalHistoryBtn)this.approvalHistoryBtn.disable();
//            if(this.deleteTrans)this.deleteTrans.enable();
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA  && this.moduleid == Wtf.Acc_Make_Payment_ModuleId){
                if(rec.data.AllowToEditCopy){
                    if(this.copyReceiptBttn) this.copyReceiptBttn.enable();
                }else{
                    if(this.copyReceiptBttn) this.copyReceiptBttn.disable();
                }
                //In case of TDS Payment, Disable the copy button.
                if(rec.data.tdsPaymentJsonFlag != undefined && rec.data.tdsPaymentJsonFlag != "" && rec.data.tdsPaymentJsonFlag){
                    if(this.copyReceiptBttn) this.copyReceiptBttn.disable();
                    if(this.editBttn) this.editBttn.disable();
                }else{
                    if(this.copyReceiptBttn) this.copyReceiptBttn.enable();
                    if(this.editBttn) this.editBttn.enable();
                }
            }
            if(rec.data.otherwise  && !(rec.data.receipttype == 6 || rec.data.receipttype == 7) && !rec.data.ismanydbcr  && rec.data.dishonoured!="Dishonoured") {
                if(this.linkTrans)this.linkTrans.enable();
                if(this.unlinkTrans && rec.data.isLinked)this.unlinkTrans.enable();
            }
            if(rec.data.paymentamountdue!=undefined && rec.data.paymentamountdue!=0 && !(rec.data.receipttype == 6 || rec.data.receipttype == 7) && !rec.data.ismanydbcr) {
                if(this.linkTrans)this.linkTrans.enable();
                if(this.unlinkTrans && rec.data.isLinked)this.unlinkTrans.enable();
            } else if(this.unlinkTrans && rec.data.isLinked) {
                this.unlinkTrans.enable();
            }
//            if(rec.data.isLinked!=undefined && rec.data.isLinked) {//for checking if the payment is already linked 
//                if(this.editBttn)this.editBttn.disable();
//            }
            if(rec.data.disableOtherwiseLinking!=undefined && rec.data.disableOtherwiseLinking) {//disableling the linking button for otherwise
                if(this.linkTrans)this.linkTrans.disable();
                if(this.unlinkTrans)this.unlinkTrans.disable();
                if(rec.data.isLinked) {
                    this.unlinkTrans.enable()
                }
            }
            if (rec.data.detailtype == 2 && !rec.data.isOpeningBalanceTransaction) {//enabling the print cheque button for bank type
                /* 
                 * dishonored isn't handle for opening document(s).*/
                if (this.printCheck)
                    this.printCheck.enable();
                if (this.chequeDishonoured && !rec.data.isDishonouredCheque && !rec.data.paymentstatus) {
                    this.chequeDishonoured.enable();
                    this.chequeDishonoured.setText(WtfGlobal.getLocaleText("acc.filed.MarkChequeDishonoured"));
                    this.chequeDishonoured.setTooltip(WtfGlobal.getLocaleText("acc.filed.MarkChequeDishonoured"));
                }
                if (this.chequeDishonoured && rec.data.isDishonouredCheque && !rec.data.paymentstatus) {
                    this.chequeDishonoured.enable();
                    this.chequeDishonoured.setText(WtfGlobal.getLocaleText("acc.filed.RevertChequeDishonoured"));
                    this.chequeDishonoured.setTooltip(WtfGlobal.getLocaleText("acc.filed.RevertChequeDishonoured"));
                }
            }
            if(rec.data.paymentwindowtype==3 && this.RepeatPayment){
                this.RepeatPayment.enable();
            }
            /*
             * Hide Copy and Edit button after creating payment from salesreturn
             */
            if(rec.data.paymentWithSalesReturn){
                this.editBttn.disable();
                this.copyReceiptBttn.disable();
            }else{
                if(this.editBttn) this.editBttn.enable();  
                if(this.copyReceiptBttn) this.copyReceiptBttn.enable();
            }
        }else{
            if(this.email)this.email.disable();
            if(this.editBttn)this.editBttn.disable();
//            if(this.deleteTrans)this.deleteTrans.disable();
            if(this.linkTrans)this.linkTrans.disable();
            if(this.unlinkTrans)this.unlinkTrans.disable();
            
            if(this.copyReceiptBttn)this.copyReceiptBttn.disable();
            if(this.printCheck)this.printCheck.disable();
            if(this.chequeDishonoured){
                this.chequeDishonoured.disable();
            }
            if(this.RepeatPayment)this.RepeatPayment.disable();
      
            
          }
                        
        /* If record is temporary deleted, 
         * 
         * then Linking Information button is being enabled,
         * to see its Linking Information
         */
        if (this.sm.getCount() == 1) {

            if (this.linkinfoViewBtn) {
                this.linkinfoViewBtn.enable();
            }
            if (this.relatedTransactionsBtn) {
                this.relatedTransactionsBtn.enable();
            }
        } else {
            if (this.linkinfoViewBtn) {
                this.linkinfoViewBtn.disable();
            }
            if (this.relatedTransactionsBtn) {
                this.relatedTransactionsBtn.disable();
            }
        }
        
        if (this.approvalHistoryBtn && this.sm.getCount() == 1) {
            this.approvalHistoryBtn.enable();
        } else {
            this.approvalHistoryBtn.disable();
        }
        
        if(this.sm.getCount()>=1){
            if(this.singlePrint)this.singlePrint.enable();
            if(this.singleRowPrint)this.singleRowPrint.enable();
            this.exportchallan281.enable();
        }else{
            if(this.singlePrint)this.singlePrint.disable();
            if(this.singleRowPrint)this.singleRowPrint.disable();
            this.exportchallan281.disable();
        }
         if(this.pendingapproval){
            if(this.sm.getCount()==1 && rec.data.deleted!=true){//when sinle record slected
                this.rejectNoteBttn.enable();
                this.approveNoteBttn.enable();
            } else {//when no record or more than on record selected
                this.rejectNoteBttn.disable();
                this.approveNoteBttn.disable();
            }
            if (this.sm.getCount() == 1 && rec.data.deleted == false) {
                this.deleteMenu.disable();
            } else {
                this.deleteMenu.enable();
            }
        }
    },
    approvePendingNote: function() {
        var formRecord = this.grid.getSelectionModel().getSelected();
        var formRecordData = formRecord.data;
        var check=this.moduleid==Wtf.Acc_Make_Payment_ModuleId;
        var type=check?WtfGlobal.getLocaleText("acc.invoiceList.mP"):WtfGlobal.getLocaleText("acc.invoiceList.recPay");
        if(formRecordData.hasApprovalAuthority){
            if(formRecordData.isFinalLevelApproval && Wtf.Acc_Make_Payment_ModuleId == this.moduleid || Wtf.Acc_Receive_Payment_ModuleId == this.moduleid){ 
                Wtf.Ajax.requestEx({
                    url:check?"ACCVendorPaymentNew/checkInvoiceKnockedOffDuringMakePaymentPending.do":"ACCReceiptNew/checkInvoiceKnockedOffDuringReceivePaymentPending.do",
                    params: {
                        billid : formRecordData.billid,
                        companyid:companyid                                
                    }
                },this,function(response){
                    if(response.issuccessCredit&&response.issuccessInv&&response.issuccessReceipt){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+type+"?",function(btn){
                            if(btn=="yes") {
                                this.callApprovalRemarkWindow();
                            } else {
                                return;
                            }
                        }, this); 
                    }else{
                        var msg = this.isReceipt?WtfGlobal.getLocaleText("acc.field.sinceinvoiceofreceivepaymentpartiallyutilized"):WtfGlobal.getLocaleText("acc.field.sinceinvoiceofmakepaymentpartiallyutilized");
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), msg+"?",function(btn){
                            if(btn=="yes") {
                                if (this.isReceipt) {
                                    callEditReceiptNew(formRecord, 'EditReceivePayment', true, this.grid, undefined, undefined, true);                                    
                                } else {
                                    callEditPaymentNew(formRecord, 'EditPaymentMade', false, this.grid, undefined, undefined, true);
                                }
                            } else {
                                return;
                            }
                        }, this);
                         
                    }
                },function(response){

                    });
            }else{
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+type+"?",function(btn){
                    if(btn=="yes") {
                        this.callApprovalRemarkWindow();
                    } else {
                        return;
                    }
                }, this);
            }
        }else{            
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel")+formRecordData.approvalLevel]);
        }
        
    },callApprovalRemarkWindow :function(){
        var URL = this.moduleid==Wtf.Acc_Make_Payment_ModuleId?"ACCVendorPaymentNew/approvePendingMakePayment.do":"ACCReceiptNew/approvePendingReceivePayment.do";
        var winTitle=this.moduleid==Wtf.Acc_Make_Payment_ModuleId?WtfGlobal.getLocaleText("acc.field.makepaymentpending"):WtfGlobal.getLocaleText("acc.field.receivepaymentpending");
        var formRecord = this.grid.getSelectionModel().getSelected();
        var formRecordData = formRecord.data;
        var htmlText=this.moduleid==Wtf.Acc_Make_Payment_ModuleId?" make Payment":" receive Payment"
        var type=this.moduleid==Wtf.Acc_Make_Payment_ModuleId?WtfGlobal.getLocaleText("acc.invoiceList.mP"):WtfGlobal.getLocaleText("acc.invoiceList.recPay");
        var itemsArr=[];
        this.remarkField =new Wtf.form.TextArea({
            fieldLabel : WtfGlobal.getLocaleText("acc.field.AddRemark*"),
            width : 200,
            height : 100,
            allowBlank : false,
            maxLength : 1024
        })
        var maxDate = (Wtf.serverDate  > this.grid.getSelectionModel().getSelected().data.billdate) ? Wtf.serverDate  : this.grid.getSelectionModel().getSelected().data.billdate;
        this.postingDate=new Wtf.ExDateFieldQtip({
            name:'postingDate',
            id: 'postingDate',
            fieldLabel: WtfGlobal.getLocaleText("acc.pending.pstingdate"),
            width: 200,
            height: 100,
            maxLength: 1024,
            format:WtfGlobal.getOnlyDateFormat(),
            value:new Date(this.grid.getSelectionModel().getSelected().data.billdate),
            maxValue:maxDate,
            minValue:this.grid.getSelectionModel().getSelected().data.billdate
        });
    
        itemsArr.push(this.remarkField);
        var height=270;
        if(CompanyPreferenceChecks.isPostingDateCheck() && formRecordData != undefined && formRecordData.isFinalLevelApproval){
            itemsArr.push(this.postingDate);
            height=300;
        }
        
        this.postingDate.on('change', function (scope, newVal, oldVal) {
            var record = this.grid.getSelectionModel().getSelected();
            var creationDate = record.data.billdate;
            var todaysDate=Wtf.serverDate;
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
        
        this.remarkWin = new Wtf.Window({
            height : height,
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
                html:getTopHtml(WtfGlobal.getLocaleText("acc.field.ApprovePending") +(htmlText) ,WtfGlobal.getLocaleText("acc.field.ApprovePending") +type+" <b>"+formRecord.data.billno+"</b>"  ,"../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
            },{
                region : 'center',
                border:false,
                layout : 'form',
                bodyStyle : 'padding:5px;',
                items : itemsArr
            }],
            modal : true,
            buttons : [ {
                text : WtfGlobal.getLocaleText("acc.cc.24"),
                scope : this,
                id: "Approvebtn"+this.id,
                handler : function() {
                        if (this.postingDate.isValid()) {
                            Wtf.getCmp("Approvebtn" + this.id).disable();
                            Wtf.Ajax.requestEx({
                                url: URL,
                                params: {
                                    billid: formRecord.data.billid,
                                    billno: formRecord.data.billno,
                                    amount: formRecord.data.amount,
                                    remark: this.remarkField.getValue(),
                                    postingDate: WtfGlobal.convertToGenericDate(this.postingDate.getValue())
                                }
                            }, this, this.genSuccessResponseApproveNote, this.genFailureResponseApproveNote);
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.pending.pstingdatevalidationmsg")], 2);
                        }
                    }
                }, {
                text : WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope : this,
                handler : function(){
                    this.remarkWin.close();
                }
            }]
        });
        this.remarkWin.show();
    },genSuccessResponseApproveNote : function(response){
        this.remarkWin.close();
        if (response.success) {
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function () {
                this.loadStore();
            }, this);
        }else{
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), response.msg, function () {
                this.loadStore();
            }, this);
        }
    },
    genFailureResponseApproveNote : function(response){
        Wtf.getCmp("Approvebtn"+this.id).enable();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
    }, handleReject:function(){
        var URL = this.moduleid==Wtf.Acc_Make_Payment_ModuleId?"ACCVendorPaymentNew/approvePendingMakePayment.do":"ACCReceiptNew/approvePendingReceivePayment.do";
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.withInvMode = false;
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        var label=this.moduleid==Wtf.Acc_Make_Payment_ModuleId?WtfGlobal.getLocaleText("acc.invoiceList.mP"):WtfGlobal.getLocaleText("acc.invoiceList.recPay");       
        var formRecordData=this.recArr[0].data;
        if(formRecordData!=undefined && formRecordData.hasApprovalAuthority){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttorejectselected")+label+"?",function(btn){
            if(btn!="yes") {
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.userds.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
            for(i=0;i<this.recArr.length;i++){
                arr.push(this.userds.indexOf(this.recArr[i]));
            }
            data= WtfGlobal.getJSONArray(this.grid,true,arr);
            //to work on reject button
            this.ajxUrl = this.moduleid==Wtf.Acc_Make_Payment_ModuleId?"ACCVendorPaymentNew/rejectPendingMakePayment.do":"ACCReceiptNew/rejectPendingReceivePayment.do";
            Wtf.Ajax.requestEx({
                url:this.ajxUrl,
                params:{
                    data:data,
                    isRejected:true,                   
                    amount:this.recArr[0].data.amountinbase
                }
            },this,this.genSuccessResponseReject,this.genFailureResponseReject);
        },this);
    }else {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.alert"),
                msg: WtfGlobal.getLocaleText("acc.vq.notAuthorisedToRejectThisRecord") + " at level "+formRecordData.approvalLevel,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO,
                width: 420,
                scope:this,
                fn: function (btn) {
                    if (btn == "ok") {
                        WtfGlobal.highLightRowColor(this.grid, this.recArr, false, 0, 2);
                    }
                }
            });            
        }
    },
    genSuccessResponseReject:function(response){
        WtfComMsgBox([this.label,response.msg],response.success*2+1);
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.userds.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
                this.loadStore();
            }, this);
        }
    },
    genFailureResponseReject:function(response){
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.userds.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg) {
            msg=response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
     handleResetClick:function(){
         if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
         }
    },
    storeloaded:function(store){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.uncheckSelAllCheckbox(this.sm);
        if(store.getCount()==0){
//            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
//            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.userds.getAt(i).data['journalentryid'];
            var jestartdate=this.startDate.getValue();
            var jeentrydate=this.endDate.getValue();
            if(e.target.getAttribute('name')!=undefined && e.target.getAttribute('name')!="")   // multiple links in single row
                accid=e.target.getAttribute('name');
            if(e.target.getAttribute('jedate')!=undefined && e.target.getAttribute('jedate')!="") {  // multiple links in single row
                jeentrydate= new Date(e.target.getAttribute('jedate'));
                jestartdate= new Date(e.target.getAttribute('jedate'));
                /*
                 * ERP-12771 : JE details not appearing
                 * Filter for JE report set in such a way that Start date = one day previous of Journal Entry date and End date = one day later the Journal Entry Date.
                 * This is done for avoiding the time zone issues.
                 */
                jestartdate = new Date(jestartdate.setDate(jeentrydate.getDate()-1));
                
                jeentrydate = new Date(jeentrydate.setDate(jeentrydate.getDate()+1));
            }
            this.fireEvent('journalentry',accid,true,this.consolidateFlag,null,null,null,jestartdate, jeentrydate);
        }
        if(header=="billno"){
            this.viewTransection(g,i,j);
        }
    },
    loadStore:function(){
        if (this.startDate.getValue() > this.endDate.getValue()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;
        }
        if (this.userds.baseParams && this.userds.baseParams.searchJson) {
            this.userds.baseParams.searchJson = "";
        }
       this.userds.load({
           params : {
               start : 0,
               limit : this.pP.combo.value,
               ss : this.quickPanelSearch.getValue(),
               pagingFlag:true             
           }
       });
       this.userds.on('load',this.storeloaded,this);
    },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;        
        if(this.label=='Payment Receipt'){
            if(record.data.withoutinventory){
                this.expandStore.proxy.conn.url="ACCReceipt/getBillingReceiptRows.do";                                
            }                
            else{
                this.expandStore.proxy.conn.url="ACCReceiptCMN/getReceiptRowsNew.do";
            }                
        }
        if(this.label=='Payment Voucher'){
            if(record.data.withoutinventory){
                this.expandStore.proxy.conn.url="ACCVendorPayment/getBillingPaymentRows.do";                
            }                
            else{
                this.expandStore.proxy.conn.url="ACCVendorPaymentCMN/getPaymentRowsNew.do";
            }                
        }
        this.expandStore.load({params:{bills:record.data['billid'],isForReport:true}});//isForReport - to get field type 4,7's value on row expand
    },
    openNewTab:function(){
        callCreateNewButtonFunction(this.moduleid,this.recordType,undefined,undefined,this.mainReport,this.fromNavigationPanel); 
    },
    fillExpanderBody:function(){
        if(this.expandStore.getCount()>0) {
            
                var disHtml = "";
            var custArr = [];
            var prevBillid = "";
            custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[this.moduleid]);
            for(var i=0;i<this.expandStore.getCount();i++){
//                var disHtml = "";
                var expandStoreRec=this.expandStore.getAt(i);
            var currentBillid = expandStoreRec.data['billid'];
            if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
                prevBillid = currentBillid;
                disHtml="";
            } 
                if(expandStoreRec.json['type']==1){ // If Advance / Refund Payment Type
                    var arr=[];
                    var isRefund = false;
                    var isGSTUsedForAdvancePayment=false;
                    var paymentWinowType= expandStoreRec.json['paymentWindowType']
                    var gridHeaderText = "Advance Payment";
                    if(this.isReceipt){
                        paymentWinowType==1?arr.push('Customer'):arr.push('Vendor');
                        if(paymentWinowType!=1) {
                            isRefund = true;
                            arr.push('Payment No')
                            arr.push(WtfGlobal.getLocaleText("acc.common.linkingDate"));
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
                            arr.push(WtfGlobal.getLocaleText("acc.common.linkingDate"));
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
                        var linkingdate=(rec.linkingdate!=undefined && rec.linkingdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.linkingdate)):"-";
                        header += "<span class='gridNo'>"+(l2+1)+".</span>";
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+rec.accountname+"</span>";
                        if(isRefund) {
                            header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+(rec.transectionno!==undefined ? rec.transectionno : "&nbsp;&nbsp;-&nbsp;&nbsp;")+"</span>";
                            header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+linkingdate+"</span>";
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
                            if (rec[custArr[j].dataIndex] != undefined && rec[custArr[j].dataIndex] != "null" && rec[custArr[j].dataIndex] != "") {
                                if (custArr[j].xtype == "datefield") {
                                    var dateString = "";
                                    dateString = isNaN(rec[custArr[j].dataIndex] * 1) ? (rec[custArr[j].dataIndex]) : (rec[custArr[j].dataIndex] * 1);
                                    var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(dateString));
                                    header += "<span class='gridRow' wtf:qtip='" + linelevel_datefield + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(linelevel_datefield, 15) + "&nbsp;</span>";
                                } else
                                    header += "<span class='gridRow' wtf:qtip='" + rec[custArr[j].dataIndex] + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex], 15) + "&nbsp;</span>";
                            } else
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
                    arr=[WtfGlobal.getLocaleText("acc.prList.invNo"),WtfGlobal.getLocaleText("acc.prList.creDate"),WtfGlobal.getLocaleText("acc.prList.dueDate"),WtfGlobal.getLocaleText("acc.common.linkingDate"),WtfGlobal.getLocaleText("acc.prList.invAmt"),WtfGlobal.getLocaleText("acc.prList.amtDue"),(this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")),"                "];
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
                        var creationdate=(rec.creationdate!=undefined && rec.creationdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.creationdate)):"";
                        var duedate=(rec.duedate!=undefined && rec.duedate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.duedate)):"";
                        var linkingdate=(rec.linkingdate!=undefined && rec.linkingdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.linkingdate)):"";
                        var isOpeningInvoice = rec.isopening;
                        var transactionNoLink = isOpeningInvoice?"<a  class='jumplink' href='#' onClick='javascript:alertForOpeningTransactions()'>"+rec.transectionno+"</a>":"<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\""+rec.transectionid+"\",\""+this.isReceipt+"\",\""+rec+"\")'>"+rec.transectionno+"</a>";
                        header += "<span class='gridNo'>"+(m2+1)+".</span>";
                        header += "<span class='gridRow ' style='width:"+widthInPercent+"% ! important;'>"+transactionNoLink+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+creationdate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+duedate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+linkingdate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountDueOriginal,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountpaid,rec.currencysymbol,[true])+"</span>";
                        for(var j=0;j<custArr.length;j++){
                            if (rec[custArr[j].dataIndex] != undefined && rec[custArr[j].dataIndex] != "null" && rec[custArr[j].dataIndex] != "") {
                                if (custArr[j].xtype == "datefield") {
                                    var dateString = "";
                                    dateString = isNaN(rec[custArr[j].dataIndex] * 1) ? (rec[custArr[j].dataIndex]) : (rec[custArr[j].dataIndex] * 1);
                                    var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(dateString));
                                    header += "<span class='gridRow' wtf:qtip='" + linelevel_datefield + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(linelevel_datefield, 15) + "&nbsp;</span>";
                                } else
                                    header += "<span class='gridRow' wtf:qtip='" + rec[custArr[j].dataIndex] + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex], 15) + "&nbsp;</span>";
                            } else
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
                    // arr=['Invoice No','Creation Date','Due Date','Invoice Amount','Amount Due',(this.isReceipt?'Amount Received':'Amount Paid'),"
                    arr=[WtfGlobal.getLocaleText("acc.prList.invNo"),this.moduleid==Wtf.Acc_Make_Payment_ModuleId?WtfGlobal.getLocaleText("acc.invoice.SupplierInvoiceNo"):"",WtfGlobal.getLocaleText("acc.prList.creDate"),WtfGlobal.getLocaleText("acc.prList.dueDate"),WtfGlobal.getLocaleText("acc.common.linkingDate"),WtfGlobal.getLocaleText("acc.prList.invAmt"),WtfGlobal.getLocaleText("acc.prList.amtDue"),((CompanyPreferenceChecks.discountOnPaymentTerms())?WtfGlobal.getLocaleText("acc.field.discount"):""),(this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")),"                "];
//                    var gridHeaderText = "Payment Against Vendor Invoice";
                    var  gridHeaderText=this.isReceipt ? WtfGlobal.getLocaleText("acc.gridheadertext.paymentagainstcustomer"):WtfGlobal.getLocaleText("acc.gridheadertext.paymentagainstvendor");
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
                    for(var i1=0;i1<arr.length;i1++){
                         header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i1] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for(var i2=0;i2<recArray.length;i2++){
                        var rec=recArray[i2];
                        var creationdate=(rec.creationdate!=undefined && rec.creationdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.creationdate)):"";
                        var duedate=(rec.duedate!=undefined && rec.duedate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.duedate)):"";
                        var linkingdate=(rec.linkingdate!=undefined && rec.linkingdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.linkingdate)):"";
                        var isOpeningInvoice = rec.isopening;
                        var supplierinvoiceno = (rec.supplierinvoiceno!=undefined && rec.supplierinvoiceno!="")?rec.supplierinvoiceno:"-";
                        var transactionNoLink = isOpeningInvoice? "<a  class='jumplink' href='#' onClick='javascript:alertForOpeningTransactions()'>"+rec.transectionno+"</a>":"<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\""+rec.transectionid+"\",\""+this.isReceipt+"\",\""+rec+"\")'>"+rec.transectionno+"</a>";
                        header += "<span class='gridNo'>"+(i2+1)+".</span>";
                        header += "<span class='gridRow '  style='width:"+widthInPercent+"% ! important;' >"+transactionNoLink+"</span>";
                        if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId) {
//                            header += "<span class='gridRow '  style='width:" + widthInPercent + "% ! important;' >" + supplierinvoiceno + "</span>";
                            /* Assigning non-breaking space for supplierinvoiceno data if it is undefined or blank or null*/
                            if (rec.supplierinvoiceno != null && rec.supplierinvoiceno != undefined && rec.supplierinvoiceno != "") {
                                header += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + supplierinvoiceno + "'>" + Wtf.util.Format.ellipsis(supplierinvoiceno, 20) + "</span>";
                            } else {
                                header += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + supplierinvoiceno + "'>" + Wtf.util.Format.ellipsis("&nbsp", 20) + "</span>";
                            }
                        }
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+creationdate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+duedate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+linkingdate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountdue,rec.currencysymbol,[true])+"</span>";
                        if (CompanyPreferenceChecks.discountOnPaymentTerms()) {
                            header += "<span class='gridRow'  style='width:" + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.discountAmount, rec.currencysymbol, [true]) + "</span>";
                            header += "<span class='gridRow'  style='width:" + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly((rec.amountpaid), rec.currencysymbol, [true]) + "</span>";
                        } else {
                            header += "<span class='gridRow'  style='width:" + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.amountpaid, rec.currencysymbol, [true]) + "</span>";
                        }
                        for(var j=0;j<custArr.length;j++){
                            if (rec[custArr[j].dataIndex] != undefined && rec[custArr[j].dataIndex] != "null" && rec[custArr[j].dataIndex] != "") {
                                if (custArr[j].xtype == "datefield") {
                                    var dateString = "";
                                    dateString = isNaN(rec[custArr[j].dataIndex] * 1) ? (rec[custArr[j].dataIndex]) : (rec[custArr[j].dataIndex] * 1);
                                    var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(dateString));
                                    header += "<span class='gridRow' wtf:qtip='" + linelevel_datefield + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(linelevel_datefield, 15) + "&nbsp;</span>";
                                } else
                                    header += "<span class='gridRow' wtf:qtip='" + rec[custArr[j].dataIndex] + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex], 15) + "&nbsp;</span>";
                            } else
                                header += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'>&nbsp;&nbsp;</span>";
                        }
                        header +="<br>";
                    }
                    header +="</div>";
                    disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
                }
                if(expandStoreRec.json['type']==3 || expandStoreRec.json['type']==8){  // 3 refers to CN/DN used in payment/receipt at line level . 8 refers to CN/DN linked to Advance Payment/Receipts
                    var type = expandStoreRec.json['type'];
                    var arr=[];
                    this.noteType="";
                    var recArray=expandStoreRec.json['typedata']
                    var paymentWinowType= expandStoreRec.json['paymentWindowType']
                    if(this.isReceipt){
                        arr.push('Debit Note');
                        paymentWinowType==1?arr.push('Customer'):arr.push('Vendor');
                        paymentWinowType==1?this.noteType=4:this.noteType=1;        // notetype 1=DN against vendor,notetype 4=DN against customer

                    } else {
                        arr.push('Credit Note');
                        paymentWinowType==1?arr.push('Vendor'):arr.push('Customer');
                        paymentWinowType==1?this.noteType=4:this.noteType=1;        // notetype 4=CN against vendor,notetype 1=CN against customer
                    }
                    arr.push(WtfGlobal.getLocaleText("acc.common.linkingDate"),'Total Amount','Amount Due','Amount Paid',"                ")
                    var gridHeaderText ='' ;
                    if(this.isReceipt){
                        type == 3 ?gridHeaderText = "Payment Against Debit Note":gridHeaderText = "Used Advance Payment Against Debit Note";
                    } else {
                        type == 3 ?gridHeaderText = "Payment Against Credit Note":gridHeaderText = "Used Advance Payment Against Credit Note";
                    }    
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
                    for(var k1=0;k1<arr.length;k1++){
                         header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[k1] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for(var k2=0;k2<recArray.length;k2++){
                        var rec=recArray[k2];
                        var isOpeningInvoice = rec.isopening;
                        var linkingdate=(rec.linkingdate!=undefined && rec.linkingdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.linkingdate)):"";
                        var transactionNoLink = isOpeningInvoice?"<a  class='jumplink' href='#' onClick='javascript:alertForOpeningTransactions()'>"+rec.transectionno+"</a>":"<a  class='jumplink' href='#' onClick='javascript:noteRecLinkNew(\""+rec.transectionid+"\",\""+this.isReceipt+"\",\""+this.noteType+"\",\""+rec+"\")'>"+rec.transectionno+"</a>";
                        header += "<span class='gridNo'>"+(k2+1)+".</span>";
                        header += "<span class='gridRow ' style='width:"+widthInPercent+"% ! important;' >"+transactionNoLink+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+rec.accountname+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+linkingdate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountdue,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.cnpaidamount,rec.currencysymbol,[true])+"</span>";
                         for(var j=0;j<custArr.length;j++){
                            if (rec[custArr[j].dataIndex] != undefined && rec[custArr[j].dataIndex] != "null" && rec[custArr[j].dataIndex] != "") {
                                if (custArr[j].xtype == "datefield") {
                                    var dateString = "";
                                    dateString = isNaN(rec[custArr[j].dataIndex] * 1) ? (rec[custArr[j].dataIndex]) : (rec[custArr[j].dataIndex] * 1);
                                    var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(dateString));
                                    header += "<span class='gridRow' wtf:qtip='" + linelevel_datefield + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(linelevel_datefield, 15) + "&nbsp;</span>";
                                } else
                                    header += "<span class='gridRow' wtf:qtip='" + rec[custArr[j].dataIndex] + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex], 15) + "&nbsp;</span>";
                            }
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
                    var isdebit=false;
                    var recArray=expandStoreRec.json['typedata']
                    arr=['Account','Type','Tax Percent','Tax Name','Tax Amount','Amount Paid',"                "];
                    var gridHeaderText = "Payment Against GL";
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
                    for(var j1=0;j1<arr.length;j1++){
                         header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[j1] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for(var j2=0;j2<recArray.length;j2++){
                        var rec=recArray[j2];
                        isdebit=rec.debit;
                        header += "<span class='gridNo'>"+(j2+1)+".</span>";
                        header += "<span class='gridRow'  wtf:qtip='"+rec.accountname+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.accountname,15)+"</span> ";
                        if (isdebit){
                            header += "<span class='gridRow'  style='width:" + widthInPercent + "% ! important;'>" + "Debit" + "</span>";
                        }else{
                            header += "<span class='gridRow'  style='width:" + widthInPercent + "% ! important;'>" + "Credit" + "</span>";
                        }
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+rec.taxpercent+"</span>";
                        header += "<span class='gridRow'  wtf:qtip='"+rec.taxname+"'  style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.taxname,15)+"&nbsp;"+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.taxamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        for(var j=0;j<custArr.length;j++){
                            if (rec[custArr[j].dataIndex] != undefined && rec[custArr[j].dataIndex] != "null" && rec[custArr[j].dataIndex] != "") {
                                if (custArr[j].xtype == "datefield") {
                                    var dateString = "";
                                    dateString = isNaN(rec[custArr[j].dataIndex] * 1) ? (rec[custArr[j].dataIndex]) : (rec[custArr[j].dataIndex] * 1);
                                    var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(dateString));
                                    header += "<span class='gridRow' wtf:qtip='" + linelevel_datefield + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(linelevel_datefield, 15) + "&nbsp;</span>";
                                } else
                                    header += "<span class='gridRow' wtf:qtip='" + rec[custArr[j].dataIndex] + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex], 15) + "&nbsp;</span>";
                            } else
                                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                        }
                        header +="<br>";
                    }
                    header +="</div>";
                    disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
                }
                if(expandStoreRec.json['type']==9){       // Loan Disbursement
                    var arr=[];
                    var recArray=expandStoreRec.json['typedata']
                    arr=['Loan Reference No.','Disbursement Date',WtfGlobal.getLocaleText("acc.common.linkingDate"),'Instalment','Amount Paid','Amount Due',"                "];
                    var gridHeaderText = "Payment Against Disbursement";
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
                    for(var j1=0;j1<arr.length;j1++){
                         header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[j1] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for(var j2=0;j2<recArray.length;j2++){
                        var rec=recArray[j2];
                        var linkingdate=(rec.linkingdate!=undefined && rec.linkingdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.linkingdate)):"";
                        header += "<span class='gridNo'>"+(j2+1)+".</span>";
                        header += "<span class='gridRow'  wtf:qtip='"+rec.accountname+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.transectionno,15)+"</span> ";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+rec.creationdateinuserformat+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+linkingdate+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+rec.instalment+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.enteramount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow'  style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountdue,rec.currencysymbol,[true])+"</span>";
                        for(var j=0;j<custArr.length;j++){
                            if (rec[custArr[j].dataIndex] != undefined && rec[custArr[j].dataIndex] != "null" && rec[custArr[j].dataIndex] != "") {
                                if (custArr[j].xtype == "datefield") {
                                    var dateString = "";
                                    dateString = isNaN(rec[custArr[j].dataIndex] * 1) ? (rec[custArr[j].dataIndex]) : (rec[custArr[j].dataIndex] * 1);
                                    var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(dateString));
                                    header += "<span class='gridRow' wtf:qtip='" + linelevel_datefield + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(linelevel_datefield, 15) + "&nbsp;</span>";
                                } else
                                    header += "<span class='gridRow' wtf:qtip='" + rec[custArr[j].dataIndex] + "' style='width:" + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex], 15) + "&nbsp;</span>";
                            } else
                                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                        }
                        header +="<br>";
                    }
                    header +="</div>";
                    disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
                }
                
                // if Refund Payment against Advance Payment
                if (expandStoreRec.json['type'] == 10) {
                    var arr = [];
                    var isRefund = false;
                    var isGSTUsedForAdvancePayment = false;
                    var paymentWinowType = expandStoreRec.json['paymentWindowType'];
                    var gridHeaderText = "Advance Payment";
                    if (this.isReceipt) {
                        paymentWinowType ==1 ? arr.push('Customer'):arr.push('Vendor');
                        if (paymentWinowType != 1) {
                            isRefund = true;
                            arr.push('Payment No')
                            gridHeaderText = "Used Refund/ Deposit Against Advance Payment";
                        }
                        if (paymentWinowType == 1 && Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA) {
                            isGSTUsedForAdvancePayment = true;
                            arr.push('GST Code')
                        }
                    } else {
                        paymentWinowType == 1 ? arr.push('Vendor') : arr.push('Customer');
                        if (paymentWinowType != 1) {
                            isRefund = true;
                            arr.push('Receipt No')
                            gridHeaderText = "Used Refund/ Deposit Against Advance Payment";
                        }
                    }
                    arr.push(WtfGlobal.getLocaleText("acc.common.linkingDate"),'Total Amount','Amount Due','Amount Paid',"                ");
                    var recArray = expandStoreRec.json['typedata']                    
                    var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";

                    var arrayLength = arr.length;
                    for (var custArrcount=0; custArrcount<custArr.length; custArrcount++) {
                        if (custArr[custArrcount].header != undefined ) {
                            arr[arrayLength+custArrcount] = custArr[custArrcount].header;
                        }
                    }
                    var count = 0;
                    for (var custArrcount=0; custArrcount<arr.length; custArrcount++) {
                        if (arr[custArrcount] != "") {
                            count++;
                        }
                    }
                    var widthInPercent = 100/count;
                    var minWidth = count*100 + 40;
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
                    for (var l1=0; l1<arr.length; l1++) {
                        header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[l1] + "</span>";
                    }
                    header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
                    header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
                    for (var l2=0; l2<recArray.length; l2++) {
                        var rec = recArray[l2];
                        var linkingdate=(rec.linkingdate!=undefined && rec.linkingdate!="")?WtfGlobal.onlyDateLeftRenderer(new Date(rec.linkingdate)):"";
                        header += "<span class='gridNo'>"+(l2+1)+".</span>";
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+rec.accountname+"</span>";
                        if (isRefund) {
                            header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+(rec.transectionno!==undefined ? rec.transectionno : "&nbsp;&nbsp;-&nbsp;&nbsp;")+"</span>";
                        }
                        if (isGSTUsedForAdvancePayment) {
                            header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+(rec.gstAccountName!==undefined ? rec.gstAccountName : "&nbsp;&nbsp;-&nbsp;&nbsp;")+"</span>";
                        }
                         header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+linkingdate+"</span>";
                        
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.totalamount,rec.currencysymbol,[true])+"</span>";
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.amountdue,rec.currencysymbol,[true])+"</span>";
                        var paidAmount = rec.paidamount;
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(paidAmount,rec.currencysymbol,[true])+"</span>";
                        for (var j=0; j<custArr.length; j++) {
                            if (rec[custArr[j].dataIndex]!=undefined && rec[custArr[j].dataIndex]!="null" && rec[custArr[j].dataIndex]!="") {
                                header += "<span class='gridRow' wtf:qtip='"+rec[custArr[j].dataIndex]+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex],15)+"&nbsp;</span>";
                            } else {
                                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                            }
                        }
                        header +="<br>";
                    }
                    header +="</div>";
                    disHtml += "<div class='expanderContainer1' style='width:100%'>" + header + "</div>";
                    
                }
            var moreIndex = this.grid.getStore().findBy(
                function(record, id) {
                    if (record.get('billid') === expandStoreRec.data['billid']) {
                        return true;  // a record with this data exists 
                    }
                    return false;  // there is no record in the store with this data
                }, this);
            if (moreIndex != -1) {
                var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                body.innerHTML = disHtml;
                if (this.expandButtonClicked) {
                    this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                    this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
                }
            }
        }
    }
        else
            this.expanderBody.innerHTML = "<br><b><div class='expanderContainer' style='width:100%'>"+WtfGlobal.getLocaleText("acc.prList.gridAmtReceived")+"</div></b>"      //This transaction is not linked with any invoice.

    },
    expandRow:function(){
        this.expandInvoice(this.invoiceID);
    },
    expandInvoice:function(id){
        this.Store.filter('billid',id);
        for(var i=0;i<this.Store.getCount();i++){
            var row = this.grid.view.getRow(i);
            if(Wtf.fly(row).hasClass('x-grid3-row-collapsed')==false) this.expander.toggleRow(row);
            if(this.Store.getAt(i).data['billid']==id){
                this.expander.toggleRow(i);
            }
        }
    },
    viewTransection: function(grid, rowIndex, columnIndex){
    var formrec=null;
    if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
        WtfComMsgBox(15,2);
            return;
        }
    formrec = this.grid.getStore().getAt(rowIndex);
        if (this.isReceipt) {
            callViewPaymentNew(formrec, 'ViewReceivePayment', true, this.grid,this.pendingapproval);
        } else {
            callViewPaymentNew(formrec, 'ViewPaymentMade', false, this.grid,this.pendingapproval);
        }
    },
    editTransaction:function(){
        var formrec=null;
        var isAllowedSpecificFields=false;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
         var rec = this.sm.getSelected();
         if(this.sm.getCount()==1 && rec.data.deleted != true){
            if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable && rec.data.IsTDSAmtUsedInGoodsReceipt ) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.advancePaymentIsUsedIntransactionForTDSSoCanNotbeEdited")], 2);
                return;
            }
            if(rec.data.isLinked!=undefined && rec.data.isLinked) {//for checking if the payment is already linked 
                if(rec.data.linkedadvanceMsgFlag!=undefined&&rec.data.linkedadvanceMsgFlag){
                    WtfComMsgBox(115,2);
                } else {
                    if (this.isReceipt) {
                        if (rec.data.isRefundTransaction != undefined && rec.data.isRefundTransaction) {
                            WtfComMsgBox(122,2);
                        } else {
                            WtfComMsgBox(114,2);
                        }
                    } else {
                        if (rec.data.isRefundTransaction != undefined && rec.data.isRefundTransaction) {
                            WtfComMsgBox(122,2);
                        } else {
                            WtfComMsgBox(121,2);
                        }
                    }
                }
                return;
            }
            
            /* Due to allow Edit Functionality we are comment below lines Ticket No: ERP-19022  */
            
//            if(!(this.isReceipt) && rec.data.isAdvancePaymentUsedAsRefund!=undefined && rec.data.isAdvancePaymentUsedAsRefund && rec.data.recordsHavingAdvancePaymentsAsRefund!=undefined){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isReceipt?WtfGlobal.getLocaleText("acc.rp.recordUsedInRefund")+" "+ rec.data.recordsHavingAdvancePaymentsAsRefund:WtfGlobal.getLocaleText("acc.mp.recordUsedInRefund")+" "+ rec.data.recordsHavingAdvancePaymentsAsRefund],2);     
//                return;
//            }
//            if(!(this.isReceipt) && rec.data.detailtype==Wtf.bank_detail_type && rec.data.clearancedate!=undefined && rec.data.clearancedate!='' && rec.data.paymentstatus == true){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isReceipt?WtfGlobal.getLocaleText("acc.rp.clearedSoCanNotEditReceipt"):WtfGlobal.getLocaleText("acc.mp.clearedSoCanNotEditPayment")],2);     
//                return;
//            }
//            if(!(this.isReceipt) && rec.data.isWrittenOff){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isReceipt?WtfGlobal.getLocaleText("acc.writeOff.receiptCanNotBeEdited"):WtfGlobal.getLocaleText("acc.writeOff.paymentCanNotBeEdited")],2);     
//                return;
//         }
         }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.grid.getSelectionModel().getCount()==1 && formrec.data.isRepeated){
            var msg = "<b>"+formrec.data.billno+ "  </b>" + WtfGlobal.getLocaleText("acc.field.hasrecurringInvoice.posted.cannotbeEdited");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2)
            return;
        }
        if(formrec.data.isadvancepayment) {
            var formCurrentAdvanceRec = WtfGlobal.searchRecord(this.grid.getStore(), formrec.data.billid, "advanceid");
              if(formCurrentAdvanceRec!=undefined&&formCurrentAdvanceRec!=null&&formCurrentAdvanceRec!=-1){
                formrec=formCurrentAdvanceRec;
            }else if(formCurrentAdvanceRec==null){
                formrec.data.receipttype=0;
            }
        }else if(formrec.data.invoiceadvcndntype!=undefined&&formrec.data.invoiceadvcndntype!=0&&formrec.data.invoiceadvcndntype==3&&formrec.data.cndnAndInvoiceId!=""){
             var formCurrentMainRec = WtfGlobal.searchRecord(this.grid.getStore(), formrec.data.cndnAndInvoiceId, "billid");
             if(formCurrentMainRec!=undefined&&formCurrentMainRec!=null&&formCurrentMainRec!=-1){
                formrec=formCurrentMainRec;
            }
        } 
        var jsonArr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        for(var i=0;i<this.recArr.length;i++){
            jsonArr.push(this.grid.getStore().indexOf(this.recArr[i]));
        }
        var selectedData= WtfGlobal.getJSONArray(this.grid,true,jsonArr);
        
        /* Due to allow Edit Functionality we are comment below lines Ticket No: ERP-19022  */
        
//        if(!(this.isReceipt) && formrec.data.isDishonouredCheque){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isReceipt?WtfGlobal.getLocaleText("erp.dishonouredreceiptedit"):WtfGlobal.getLocaleText("acc.alert.dishonouredpaymentedit")]);     
//            return; 
//        }
//         
         /* Below code Block is used to when if any payment 
          * is dishonoured,status is clear,used in Written Off or used in advance payment then allow to edit some fields. */
         
        var alertMessage = WtfGlobal.getLocaleText("acc.invList.linkedInvoice");;
        if (rec.data.isAdvancePaymentUsedAsRefund) {
            isAllowedSpecificFields = true;
            alertMessage = WtfGlobal.getLocaleText("acc.invList.linkedInvoice");
        } else if (rec.data.paymentstatus == true) {
            isAllowedSpecificFields = true;
            alertMessage = WtfGlobal.getLocaleText("acc.payment.statusMsg");
        } else if (formrec.data.isDishonouredCheque) {
            isAllowedSpecificFields = true;
            alertMessage = WtfGlobal.getLocaleText("acc.payment.dishonouredMsg");
        } else if (rec.data.isWrittenOff) {
            isAllowedSpecificFields = true;
            alertMessage = WtfGlobal.getLocaleText("acc.payment.writtenoffMsg");
        } else if (rec.data.isLinkedInvoiceIsClaimed) {
            isAllowedSpecificFields = true;
            alertMessage = this.isReceipt?WtfGlobal.getLocaleText("acc.malaysiangst.blockReceiptEdit"):WtfGlobal.getLocaleText("acc.malaysiangst.blockPaymentEdit");
        }else if (rec.data.isPaymentLinkedToSalesOrder) {          //ERM-736
            isAllowedSpecificFields = true;
            alertMessage = WtfGlobal.getLocaleText("acc.recieptReportList.linkedSalesOrder");
        }
        if (this.grid.getSelectionModel().getCount() == 1 && (formrec.data.parentid!="" && formrec.data.parentid!=undefined)) {
            /**
             * Allow Edit recurred Payment
             */
            isAllowedSpecificFields = true;
            alertMessage = WtfGlobal.getLocaleText("acc.invList.RecurredInvoice");
        }
        if(formrec.data.invoiceadvcndntype!=undefined&&formrec.data.invoiceadvcndntype!=0){
            Wtf.Ajax.requestEx({
                url:this.isReceipt?"ACCReceiptNew/getSinglePaymentDataToLoad.do":"ACCVendorPaymentNew/getSinglePaymentDataToLoad.do",
                params: {
                    selectedData: selectedData
                }
            },this,function(response){
                if(response.success){
                    var datainvoiceadvcndn=response.data;
                        var invoicebillid="";
                        var advbillid="";
                        var cndnbillid="";
                        for(var i=0;i<datainvoiceadvcndn.length;i++){
                            if(datainvoiceadvcndn[i].invoiceadvcndntype==1){
                                invoicebillid=datainvoiceadvcndn[i].paymentID;
                            }
                            if(datainvoiceadvcndn[i].invoiceadvcndntype==2){
                                advbillid=datainvoiceadvcndn[i].paymentID;
                            }
                            if(datainvoiceadvcndn[i].invoiceadvcndntype==3){
                                cndnbillid=datainvoiceadvcndn[i].paymentID;
                            }
                        }
                    formrec.data.datainvoiceadvcndn=datainvoiceadvcndn;
                    if(this.isReceipt)
                        callEditReceiptNew(formrec, 'EditReceivePayment',true,this.grid);
                    else
                        callEditPaymentNew(formrec, 'EditPaymentMade',false,this.grid);
                }
            },function(){});
        } else {
            if (this.isReceipt) {

                if (isAllowedSpecificFields) { //isAllowedSpecificFields - used when Receipt Document is used as source to another Document.
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),alertMessage, function(btn) {
                        if (btn == "yes") {
                            callEditReceiptNew(formrec, 'EditReceivePayment', true, this.grid, false, isAllowedSpecificFields);
                        } else {
                            return;
                        }
                    }, this);
                } else {
                    callEditReceiptNew(formrec, 'EditReceivePayment', true, this.grid, false, isAllowedSpecificFields);
                }
            }
            else {
                if (isAllowedSpecificFields) {  //isAllowedSpecificFields - used when Payment Document is used as source to another Document.
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), alertMessage, function(btn) {
                        if (btn == "yes") {
                            callEditPaymentNew(formrec, 'EditPaymentMade', false, this.grid, false, isAllowedSpecificFields);
                        } else {
                            return;
                        }
                    }, this);
                } else {
                    callEditPaymentNew(formrec, 'EditPaymentMade', false, this.grid, false, isAllowedSpecificFields,this.pendingapproval);
                }
            }

        }
        },
     copyTransaction:function(){
        var formrec=null;
        var copyReceiptTransaction=true;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(formrec.data.isadvancepayment) {
            var formCurrentAdvanceRec = WtfGlobal.searchRecord(this.grid.getStore(), formrec.data.billid, "advanceid");
            if(formCurrentAdvanceRec!=undefined&&formCurrentAdvanceRec!=null&&formCurrentAdvanceRec!=-1){
                formrec=formCurrentAdvanceRec;
            }else if(formCurrentAdvanceRec==null){
                formrec.data.receipttype=0;
            }
        }
        var isVendor=false;
        var title="";
        if (this.isReceipt) {
            if (formrec.data.paymentwindowtype == "2") {
                 isVendor = true;
            }
        } else {
            if (formrec.data.paymentwindowtype == "1") {
                 isVendor = true;
            }
        }
        if (isVendor) {
            title = WtfGlobal.getLocaleText("acc.vendor.vendorName");
        } else {
            title = WtfGlobal.getLocaleText("acc.customer.customerName");
        }
        if(!formrec.data.isactive && formrec.data.paymentwindowtype != "3" ) {
             Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                        msg: title + formrec.data.personname + " " + WtfGlobal.getLocaleText("acc.field.iscurrentlydeactivated"),
                        width: 370,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this
                    });
            return;
        }
        if (this.isReceipt) {
            callEditReceiptNew(formrec, 'CopyReceivePayment', true, this.grid, copyReceiptTransaction);
        } else {
            callEditPaymentNew(formrec, 'CopyPaymentMade', false, this.grid, copyReceiptTransaction);
        }
    },
    ChequeDishonoured: function (billid, minFromDate) {

        this.DateWin = new Wtf.Window({
            height: 260,
            width: 400,
            title: WtfGlobal.getLocaleText("acc.filed.MarkChequeDishonoured"),
            bodyStyle: 'padding:5px;background-color:#f1f1f1;',
            layout: 'border',
            items: [{
                    region: 'north',
                    border: false,
                    height: 70,
                    bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.date.markchequedishonoured"), WtfGlobal.getLocaleText("acc.date.postedJE.chequedishonoured"), "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                },
                {
                    region: 'center',
                    border: false,
                    layout: 'form',
                    bodyStyle: 'padding:40px 5px 5px 19px;',
                    items: [
                        this.chequeDisDate = new Wtf.form.DateField({//SDP-11553
                            fieldLabel: WtfGlobal.getLocaleText("acc.het.512"),
                            width: 200,
                            height: 50,
                            bodyStyle: 'margin-top: 25px',
                            allowBlank: false,
                            format: WtfGlobal.getOnlyDateFormat(),
                            value:new Date(),
                            minValue: minFromDate,
                        })
                    ]
                }],
            buttons: [{
                    text: WtfGlobal.getLocaleText("acc.common.submit"),
                    scope: this,
                    handler: function () {

                        if (!this.chequeDisDate.isValid()) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.alert.datechequedishonoured")], 2);
                            return;
                        }
                        if (this.isReceipt) {
                            this.url = "ACCReceiptNew/saveDishonouredReceipt.do";
                        } else {
                            this.url = "ACCVendorPaymentNew/saveDishonouredPayment.do";
                        }
                        Wtf.Ajax.requestEx({
                            url: this.url,
                            params: {
                                billid: billid,
                                entrydate:WtfGlobal.convertToGenericStartDate(this.chequeDisDate.getValue())                            
                            }
                        }, this, this.AddChequeDishonouredSuccessResponse, this.AddChequeDishonouredFailureResponse);
                        this.DateWin.close();

                    }
                }, {
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    scope: this,
                    handler: function () {
                        this.DateWin.close();
                    }
                }]
        });
        this.DateWin.show();
    },

    AddChequeDishonoured: function () {
        var formrec = null;
        if (this.grid.getSelectionModel().hasSelection() == false || this.grid.getSelectionModel().getCount() > 1) {
            WtfComMsgBox(15, 2);
            return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(formrec.data.isLinkedInvoiceIsClaimed){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(this.isReceipt?WtfGlobal.getLocaleText("acc.malaysiangst.blockReceiptDishonor"):WtfGlobal.getLocaleText("acc.malaysiangst.blockPaymentDishonor"))], 2);
            return;
        }
        var billdate = formrec.data.billdate;
        var activefromDate = new Date(Wtf.account.companyAccountPref.activeDateRangeFromDate);
        var minFromDate = ((activefromDate!=undefined && activefromDate!=null && activefromDate > billdate) ? activefromDate : billdate);
        var billid = formrec.data.billid;
        var isDishonouredCheque = formrec.data.isDishonouredCheque;
        if (!isDishonouredCheque)           //True for Mark Cheque Dishonoured  and False for Revert Mark Cheque dishonoured
        {
          this.ChequeDishonoured(billid, minFromDate);
        } else {
            this.url = '';
            if (this.isReceipt) {
                this.url = "ACCReceiptNew/revertDishonouredReceipt.do";
            } else {
                this.url = "ACCVendorPaymentNew/revertDishonouredPayment.do";
            }
            Wtf.Ajax.requestEx({
                url: this.url,
                params: {
                    billid: billid,
                }
            }, this, this.AddChequeDishonouredSuccessResponse, this.AddChequeDishonouredFailureResponse);
        }
        
        
    },

    AddChequeDishonouredSuccessResponse:function(response){
        if (response.success) {
            var msg;
            if (response.msg) {
                msg = response.msg;
            }
            
            this.grid.getStore().on('load', function() {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
            }, this.grid.getStore(), {
                single: true
            });

            this.grid.getStore().reload();

        } else {
            this.grid.getStore().on('load', function() {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
            }, this.grid.getStore(), {
                single: true
            });
            this.grid.getStore().reload();
        }
    },   
    AddChequeDishonouredFailureResponse:function(response){
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.grid.getStore().indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    sendMail:function(){

        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.isReceipt)
              (formrec.data.withoutinventory)? callEmailWin("emailwin",formrec,this.label,12,false,false):callEmailForMultipleRecords('', formrec, this.label, 4, false,'', this.label,'pdf', 24, undefined,false,true,"",Wtf.Acc_Receive_Payment_ModuleId,this.userds);
        else
             (formrec.data.withoutinventory)? callEmailWin("emailwin",formrec,this.label,16,false,false):callEmailForMultipleRecords('', formrec, this.label, 8, false,'', this.label,'pdf', 24, undefined,false,true,"",Wtf.Acc_Make_Payment_ModuleId,this.userds);




    },
    handleDelete:function(del){
        if(!this.grid.getSelectionModel().hasSelection()){
            WtfComMsgBox(34,2);
            return;
        }                     
        var recArr = this.grid.getSelectionModel().getSelections();
        var formRecordData = recArr != undefined ? recArr[0].data : "";
        if (formRecordData != undefined && formRecordData != "" && !formRecordData.hasApprovalAuthority && this.pendingapproval) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.alert"),
                msg: WtfGlobal.getLocaleText("acc.vq.notAuthorisedToDeleteThisRecord") + " at level " + formRecordData.approvalLevel,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO,
                width: 420,
                scope: this,
                fn: function (btn) {
                    if (btn == "ok") {
                        WtfGlobal.highLightRowColor(this.grid, this.recArr, false, 0, 2);
                    }
                }
            });
            return;
        }
        var failToDeleteDueToWriteOff=false;
        var recordsNotDeletedDueToWriteOff='';
        var failToDeleteDueToDishonouredCheque=false;
        var recordsNotDeletedDueToDishonouredCheque='';
        var failToDeleteDueToClaimedInvoices=false;
        var recordsNotDeletedDueToClaimedInvoices=''; // Payments with at least one linked invoice is claimed .
        var failToDeleteDueToLinkingWithRefund=false;
        var recordsNotDeletedAsLinkedWithRefund='';
        var checkHasRecurringInvFlag = false;var invoiceno="";
        //To check whether TDS Amt of Advance is adjusted with GR
        var failToDeleteAsTDSAmtIsAdjustedWithGR = false;var goodsreceiptnos = "";
        for(var j=0;j< recArr.length;j++){
            var rec=recArr[j];
            if(rec.data.isWrittenOff){
                failToDeleteDueToWriteOff=true;
                recordsNotDeletedDueToWriteOff+='<b>'+rec.data.billno+'</b>'+", ";
            } else if(rec.data.isDishonouredCheque){
                failToDeleteDueToDishonouredCheque=true;
                recordsNotDeletedDueToDishonouredCheque+='<b>'+rec.data.billno+'</b>'+", ";
            } else if(rec.data.isLinkedInvoiceIsClaimed){
                failToDeleteDueToClaimedInvoices = true;
                recordsNotDeletedDueToClaimedInvoices+='<b>'+rec.data.billno+'</b>'+", ";
            } else if(rec.data.isAdvancePaymentUsedAsRefund){
                failToDeleteDueToLinkingWithRefund = true;
                recordsNotDeletedAsLinkedWithRefund+='<b>'+rec.data.billno+'</b>'+", ";
            } else if(rec.data.isRepeated){  //Source Invoice cannot delete if it has recur Invoice.
                checkHasRecurringInvFlag=true;
                invoiceno=rec.data.billno;
                break;
            }else if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isTDSApplicable && rec.data.IsTDSAmtUsedInGoodsReceipt){
                failToDeleteAsTDSAmtIsAdjustedWithGR = true;
                goodsreceiptnos += '<b>'+rec.data.billno+'</b>'+", ";
            }
        }
        if(failToDeleteDueToWriteOff && recordsNotDeletedDueToWriteOff!=''){
            recordsNotDeletedDueToWriteOff=recordsNotDeletedDueToWriteOff.substring(0,recordsNotDeletedDueToWriteOff.length-2);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(this.isReceipt?WtfGlobal.getLocaleText("acc.writeOff.receipts"):WtfGlobal.getLocaleText("acc.writeOff.payments"))+" "+recordsNotDeletedDueToWriteOff+" "+WtfGlobal.getLocaleText("acc.writeOff.receiptsCanNotBeDeleted")], 2);
            return;
        }
        if(failToDeleteDueToDishonouredCheque && recordsNotDeletedDueToDishonouredCheque!=''){
            recordsNotDeletedDueToDishonouredCheque=recordsNotDeletedDueToDishonouredCheque.substring(0,recordsNotDeletedDueToDishonouredCheque.length-2);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(this.isReceipt?WtfGlobal.getLocaleText("acc.writeOff.receipts"):WtfGlobal.getLocaleText("acc.writeOff.payments"))+" "+recordsNotDeletedDueToDishonouredCheque+" "+WtfGlobal.getLocaleText("erp.dishonouredreceiptcannotdelete")], 2);
            return;
        }
        if(failToDeleteDueToClaimedInvoices && recordsNotDeletedDueToClaimedInvoices!=''){
            recordsNotDeletedDueToClaimedInvoices=recordsNotDeletedDueToClaimedInvoices.substring(0,recordsNotDeletedDueToClaimedInvoices.length-2);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(this.isReceipt?WtfGlobal.getLocaleText("acc.malaysiangst.blockReceiptdelete"):WtfGlobal.getLocaleText("acc.malaysiangst.blockPaymentDelete"))+"<br>"+recordsNotDeletedDueToClaimedInvoices], 2);
            return;
        }
        if(failToDeleteDueToLinkingWithRefund && recordsNotDeletedAsLinkedWithRefund !=''){
            recordsNotDeletedAsLinkedWithRefund=recordsNotDeletedAsLinkedWithRefund.substring(0,recordsNotDeletedAsLinkedWithRefund.length-2);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isReceipt?WtfGlobal.getLocaleText("acc.rp.recordUsedInRefundSoCanNotDelete")+" "+ recordsNotDeletedAsLinkedWithRefund:WtfGlobal.getLocaleText("acc.mp.recordUsedInRefundSoCanNotDelete")+" "+ recordsNotDeletedAsLinkedWithRefund],2);     
            return;
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isTDSApplicable && failToDeleteAsTDSAmtIsAdjustedWithGR && goodsreceiptnos != "" ){
            goodsreceiptnos=goodsreceiptnos.substring(0,goodsreceiptnos.length-2);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.recordUsedInPISoCanNotDelete")+" "+ goodsreceiptnos],2);     
            return;
        }
        if(checkHasRecurringInvFlag){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.label +" <b>"+invoiceno+"</b> "+WtfGlobal.getLocaleText("acc.field.hasrecurringInvoice.postedsocannotbedeleted")], 2);
            return;
        }
        var delFlag=del;
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.selectedReceiptsCount=this.recArr.length;
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), this.isReceipt? WtfGlobal.getLocaleText("acc.nee.14"):WtfGlobal.getLocaleText("acc.nee.15") ,function(btn){
        if(btn!="yes") {
            for(var i=0;i<this.recArr.length;i++){
                var ind=this.grid.getStore().indexOf(this.recArr[i])
                var num= ind%2;
                WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
            }
            return;
        }
        for(i=0;i<this.recArr.length;i++){
                arr.push(this.grid.getStore().indexOf(this.recArr[i]));
        }
        data= WtfGlobal.getJSONArray(this.grid,true,arr);
        var mainData="";
        var jsonArr=[];      
        for(i=0;i<this.recArr.length;i++){
            var recMain=WtfGlobal.searchRecordIndex(this.grid.getStore(), this.recArr[i].data.billid, "advanceid")
            if(recMain!=undefined&&recMain!=-1&&recMain!=null){
                jsonArr.push(recMain);
            }
        }
        var rec="";
        mainData= WtfGlobal.getJSONArray(this.grid,true,jsonArr);     
        this.deleteUrl = "";
        //(this.isCustBill?21:22)
        if(this.isReceipt){
           if(delFlag=='del')
              {
                this.deleteUrl = "ACCReceiptNew/deleteReceiptMerged.do";
              }
              else
              {
//                  this.deleteUrl = "ACCReceipt/deleteReceiptPermanent.do";
                  this.deleteUrl = "ACCReceiptNew/deleteReceiptForEdit.do";
              }   
            
        }else{
             if(delFlag=='del')
              {
                    this.deleteUrl = "ACCVendorPaymentNew/deletePaymentMerged.do";
              }
              else
              {
//                  this.deleteUrl = "ACCVendorPayment/deletePaymentPermanent.do";
                    this.deleteUrl = "ACCVendorPaymentNew/deletePaymentForEdit.do";
              }   
           }
            if (this.selectedReceiptsCount != "" && this.selectedReceiptsCount >= 30){
                WtfGlobal.setAjaxTimeOutFor5Minutes();
            }
            Wtf.Ajax.requestEx({
                url:this.deleteUrl,
//                url: Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
                params:{
                   data:data,
                   mainData:mainData,
                    mode:(this.isCustBill?21:22)
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
      printCkeck:function(){
        this.print=true;
        var formRecord = null;  
        formRecord = this.grid.getSelectionModel().getSelected();
    if(formRecord.get("ischequeprinted")==true){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.printCheque"),WtfGlobal.getLocaleText("acc.je.printmsg"),function(btn){
            if(btn!="yes") {
                return;
            }              
            this.confirmPrint(formRecord);
        },this);
    }else{
        this.confirmPrint(formRecord);
    }
    }, 
    
    confirmPrint:function(formRecord){
        var amount=formRecord.get("amount");
        var Printdate=WtfGlobal.convertToGenericDate(formRecord.get("chequedateforprint"));
        var paymentMethod=formRecord.get("methodid");
        var name=formRecord.get("paidto");
        var paymentid=formRecord.get("billid");
        var paymentno=formRecord.get("billno");
        var currencyid=formRecord.get("currencyid");
        var chequeno=formRecord.get("chequenumber"); 
        var bankname=formRecord.get("bankname"); 
        /*
         * Get the value form record 
        */
        var memo=formRecord.get("memo"); 
        var address=formRecord.get("onlyBillingAdddressData"); 
        this.printUrl = "ACCVendorPaymentNew/printCheck.do";              
    if(formRecord.data.paymentwindowtype==this.paymentwindowtype.AgainstGL){ // If payment is against GL code, name of 'Paid to ' is printed on cheque. otherwise name of respective Vendor/Customer will be printed.
        name=formRecord.get("paidto");
        if(formRecord.get("paidto")==undefined||formRecord.get("paidto")==""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PleaseEnterPaidto")],2);     
            return;
        } 
    }else{
        name= formRecord.get("personname"); 
    }
        if (formRecord.get("payee") != undefined && formRecord.get("payee") != "") {
            name = formRecord.get("payee");
        }
        Wtf.Ajax.requestEx({
            url:this.printUrl,                
            params:{
                amount:amount,
                Printdate:Printdate,
                paymentMethod:paymentMethod,
                name:name,
                currencyid:currencyid,
                paymentid:paymentid,
                paymentno:paymentno,
                chequeno:chequeno,
                bankname:bankname,
                memo:memo,
                address:address
            }
        },this,this.genPrintSuccessResponse,this.genFailureResponse);  
    },
    genSuccessResponse:function(response){
        if (this.selectedReceiptsCount != "" && this.selectedReceiptsCount >= 30){
            WtfGlobal.resetAjaxTimeOut();
        }
        WtfComMsgBox([this.isReceipt?WtfGlobal.getLocaleText("acc.prList.tabTitle"):WtfGlobal.getLocaleText("acc.pmList.tabTitle"),response.msg],response.success*2+1);
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.grid.getStore().indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            (function(){
                this.userds.load({params:{start:0,limit:this.pP.combo.value,ss : this.quickPanelSearch.getValue()}});
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }

    }, 
    genPrintSuccessResponse:function(response){
    if (response.data) {
        var resdata = response.data[0];
        this.printDetailswin=new Wtf.account.ReceiptEntry({
            });
        this.printDetailswin.printCheque(resdata);//passing parameter as JSON object
        this.print = false;
    }
    },   
    genFailureResponse:function(response){
        if (this.selectedReceiptsCount != "" && this.selectedReceiptsCount >= 30){
            WtfGlobal.resetAjaxTimeOut();
        }
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.grid.getStore().indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    calllinkInvoiceNew:function(winid, reconRec){
        var record = this.sm.getSelected();
	var reportgrid = this.grid;
        if(record.data.billdate > new Date()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.linking.canNotLinkFutureRecord")],2);
            return;
        }
        reconRec=(reconRec==undefined?"":reconRec);
        var tabid=(tabid==null?this.isReceipt?"LinkAdvanceReceipt":"LinkAdvancePayment":tabid);
        var panel = Wtf.getCmp(tabid);        
        if(panel == null){
            panel = new Wtf.account.LinkAdvancePayment({
                id:tabid,
                isCustBill:this.isCustBill,
                isReceipt:this.isReceipt,
                border:false,
                closable: true,
                record: record,
                isCustomer:record.data.isOpeningBalanceTransaction?this.isReceipt:(this.isReceipt?record.data.paymentwindowtype==1:record.data.paymentwindowtype==2),
                title:this.isReceipt?Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.rp.linkAdvReceipt"),Wtf.TAB_TITLE_LENGTH):Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.mp.linkAdvPayment"),Wtf.TAB_TITLE_LENGTH),
                tabTip:this.isReceipt?WtfGlobal.getLocaleText("acc.rp.linkAdvReceipt"):WtfGlobal.getLocaleText("acc.mp.linkAdvPayment"),  //'Receive Payments',
                islinkflag : true,
                iconCls :'accountingbase receivepayment',
                layout: 'border',
		listeners:{
                 'close':function(panel){
                        if(reportgrid!=null && reportgrid!=undefined){
                            reportgrid.getStore().reload();   //SDP-12807                         
                        }
                  }
                }
            });
            panel.on("activate", function(){
                panel.doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    },


callluninkInvoiceNew: function(winid, reconRec){
        reconRec=(reconRec==undefined?"":reconRec);
        var tabid=(tabid==null?this.isReceipt?"UnlinkAdvanceReceipt":"UnlinkAdvancePayment":tabid);
        var panel = Wtf.getCmp(tabid);
        var record = this.sm.getSelected();
	var reportgrid = this.grid;
        if(!panel){
            panel=new Wtf.account.LinkAdvancePayment({
                id:tabid,
                isCustBill:this.isCustBill,
                isReceipt:this.isReceipt,
                closable: true,
                border:true,
                record: record,
                islinkflag : false,
                title:this.isReceipt?Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.unlink.rp"),Wtf.TAB_TITLE_LENGTH):Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.unlink.mp"),Wtf.TAB_TITLE_LENGTH),
                tabTip:this.isReceipt?WtfGlobal.getLocaleText("acc.unlink.rp"):WtfGlobal.getLocaleText("acc.unlink.mp"),  //'Receive Payments',
                iconCls :'accountingbase receivepayment',
                layout: 'border',
		listeners:{
                 'close':function(panel){
                        if(reportgrid!=null && reportgrid!=undefined){
                            reportgrid.getStore().reload();   //SDP-12807                         
                        }
                  }
                }
                
            });
             panel.on("activate", function(){
                panel.doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    },

    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    openPendingApprovalTab:function(){
    var panelID=this.moduleid==Wtf.Acc_Make_Payment_ModuleId?"makePaymentPendingApproval":"receivePaymentPendingApproval";
    var panel = Wtf.getCmp(panelID);
    if(panel==null){
        var pendingApproval= true;
//        getPaymentTab(consolidateFlag,searchStr, filterAppend,reportbtnshwFlag,fromNavigationPanel,titlelabel,panelID,isReceivable)
//        panel=getPaymentTab(false,"", "",false,undefined,WtfGlobal.getLocaleText("acc.field.PendingApprovalPaymentReport"),panelID,false,pendingApproval)
            if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId) {
                panel = getPaymentTab(false, "", "", false, undefined, WtfGlobal.getLocaleText("acc.field.PendingApprovalPaymentReport"), panelID, false, pendingApproval, this.moduleid,this.winValue);
            } else {
                panel = getPaymentTab(false, "", "", false, undefined, WtfGlobal.getLocaleText("acc.field.ReceiveApprovalPaymentReport"), panelID, true, pendingApproval, this.moduleid,this.winValue);
            }
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
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
        this.userds.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.userds.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: this.pP.combo.value}});
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.userds.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.userds.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: this.pP.combo.value}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    repeatePaymentHandler:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.grid.getSelectionModel().getCount()==1 && formrec.data.isRepeated){
            var msg = "<b>"+formrec.data.billno + WtfGlobal.getLocaleText("</b>   has already set a recurring, so cannot be recurred.");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2)
            return;
        }
        var isaddRecurringPayment=false;
        var isEdit=false;
        callRepeatedPaymentWindow(formrec.data.withoutinventory, formrec ,isaddRecurringPayment, isEdit,this.isReceipt);          
    },
    showCustomerCustomFieldFlag: function(moduleid){
        var customerCustomFieldFlag = false;
        if(moduleid===Wtf.Acc_Receive_Payment_ModuleId){
            customerCustomFieldFlag = true;
        }
        return customerCustomFieldFlag;
    },
    
    showVendorCustomField: function(moduleid){
        var vendorCustomFieldFlag = false;
        if(moduleid===Wtf.Acc_Make_Payment_ModuleId){
            vendorCustomFieldFlag = true;
        }
        return vendorCustomFieldFlag;
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
    updateReportFilterValues: function(isOpenFromNavigationTree) {
        var arr = [];
        if (isOpenFromNavigationTree) {
            /*
             * Below fileter is showing when open from navigational panel
            */
            arr.push([0, WtfGlobal.getLocaleText("acc.rem.105")]);  //All Option
            if (this.isReceivable) {
                if (this.isReceipt) {
                    arr.push([3, WtfGlobal.getLocaleText("acc.rp.receivedFromCustomer")]);
                    arr.push([15, WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")]);           // Account Receivable -Opening Balance
                } else {
                    arr.push([6, WtfGlobal.getLocaleText("acc.mp.payToCustomer")]);
                }
            } else {
                if (this.isReceipt) {
                    arr.push([6, WtfGlobal.getLocaleText("acc.rp.receivedFromVendor")]);
                } else {
                    arr.push([3, WtfGlobal.getLocaleText("acc.mp.payToVendor")]);
                    arr.push([15, WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")]);           // Account Payable -Opening Balance
                }
            }

            /*
             * Below fileter is always showing in both Account Receivable/Account Payable Reports
             */
            arr.push([1, WtfGlobal.getLocaleText("acc.rem.106")]);            // Deleted Records
            arr.push([2, WtfGlobal.getLocaleText("acc.rem.107")]);            // Exclude Deleted Record(s)
            arr.push([5, WtfGlobal.getLocaleText("acc.rem.225")]);           // Post dated Cheque Payment
            arr.push([14, WtfGlobal.getLocaleText("acc.rem.229")]);
            arr.push([17, WtfGlobal.getLocaleText("acc.payment.advancepayment.allpayments")]);
            arr.push([18, WtfGlobal.getLocaleText("acc.payment.advancepayment.unutilized")]);           // unutilized
            arr.push([19, WtfGlobal.getLocaleText("acc.payment.advancepayment.partiallyutilized")]);         //partiallyutilized                      
            arr.push([20, WtfGlobal.getLocaleText("acc.payment.advancepayment.fullyutilized")]);            //fullyutilized
            arr.push([21, WtfGlobal.getLocaleText("acc.payment.advancepayment.nonorpartialutilized")]);     //non or patiallyutilized
        } else {
           
             /*
             * Below Filter is shown when open from Dashboard
             */
            if (this.isReceipt) {
                arr = [[0, WtfGlobal.getLocaleText("acc.rem.105")],
                    [3, WtfGlobal.getLocaleText("acc.rp.receivedFromCustomer")],
                    [6, WtfGlobal.getLocaleText("acc.rp.receivedFromVendor")],
                    [1, WtfGlobal.getLocaleText("acc.rem.106")], // Deleted Records
                    [2, WtfGlobal.getLocaleText("acc.rem.107")], // Exclude Deleted Record(s)
                    [5, WtfGlobal.getLocaleText("acc.rem.225")], // Post dated Cheque Payment
                    [14, WtfGlobal.getLocaleText("acc.rem.229")], // Dishonoured Cheque
                    [9, WtfGlobal.getLocaleText("acc.field.PaymentagainstGLCode")], // Agsinst GL                          
                    [15, WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")], // Opening Balance
                    [17, WtfGlobal.getLocaleText("acc.payment.advancepayment.allpayments")], // all payments.
                    [18, WtfGlobal.getLocaleText("acc.payment.advancepayment.unutilized")], // unutilized
                    [19, WtfGlobal.getLocaleText("acc.payment.advancepayment.partiallyutilized")], //partiallyutilized                      
                    [20, WtfGlobal.getLocaleText("acc.payment.advancepayment.fullyutilized")],      //fullyutilized
                    [21, WtfGlobal.getLocaleText("acc.payment.advancepayment.nonorpartialutilized")]];            //non or patiallyutilized
                
            } else {
                arr = [[0, WtfGlobal.getLocaleText("acc.rem.105")],
                    [3, WtfGlobal.getLocaleText("acc.mp.payToVendor")],
                    [6, WtfGlobal.getLocaleText("acc.mp.payToCustomer")],
                    [1, WtfGlobal.getLocaleText("acc.rem.106")], // Deleted Records
                    [2, WtfGlobal.getLocaleText("acc.rem.107")], // Exclude Deleted Record(s)
                    [5, WtfGlobal.getLocaleText("acc.rem.225")], // Post dated Cheque Payment
                    [14, WtfGlobal.getLocaleText("acc.rem.229")], // Dishonoured Cheque
                    [9, WtfGlobal.getLocaleText("acc.field.PaymentagainstGLCode")], // Agsinst GL          
                    [15, WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")], // Opening Balance
                    [17, WtfGlobal.getLocaleText("acc.payment.advancepayment.allpayments")], // all payments.
                    [18, WtfGlobal.getLocaleText("acc.payment.advancepayment.unutilized")], // unutilized
                    [19, WtfGlobal.getLocaleText("acc.payment.advancepayment.partiallyutilized")], //partiallyutilized                      
                    [20, WtfGlobal.getLocaleText("acc.payment.advancepayment.fullyutilized")],  //fullyutilized
                    [21, WtfGlobal.getLocaleText("acc.payment.advancepayment.nonorpartialutilized")]];    //non or patiallyutilized
            }
        }
        if (this.delTypeStore) {
            this.delTypeStore.loadData(arr);
        } else {
            this.delTypeStore = new Wtf.data.SimpleStore({
                fields: [{name: 'typeid', type: 'int'}, 'name'],
                data: arr
            });
        }
    },
    
    viewApprovalHistory: function () {
        var rec = this.sm.getSelected();
        Wtf.Ajax.requestEx({
            url: "ACCReports/getApprovalhistory.do",
            params: {
                billid: rec.data.billid
            }
        }, this, function (response, request) {
            var historyWin = new Wtf.Window({
                height: 300,
                width: 475,
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                title: WtfGlobal.getLocaleText("acc.field.ApprovalHistory"),
                bodyStyle: 'padding:5px;background-color:#ffffff;',
                layout: 'border',
                items: [
                    {
                        region: 'north',
                        border: false,
                        height: 70,
                        bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                        html: getTopHtml(WtfGlobal.getLocaleText("acc.field.ApprovalHistory"), WtfGlobal.getLocaleText("acc.field.ApproveHistoryof") + this.label + " <b>" + rec.data.billno + "</b>", "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                    },
                    {
                        region: 'center',
                        border: false,
                        autoScroll: true,
                        bodyStyle: 'padding:5px;background-color:#f1f1f1;',
                        html: response.msg
                    }
                ],
                buttons: [
                    {
                        text: WtfGlobal.getLocaleText("acc.common.close"),
                        handler: function () {
                            historyWin.close();
                        }
                    }
                ],
                autoScroll: true,
                modal: true
            });
            
            historyWin.show();

        }, function (response, request) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")], 2);
        });
    }
});
    
