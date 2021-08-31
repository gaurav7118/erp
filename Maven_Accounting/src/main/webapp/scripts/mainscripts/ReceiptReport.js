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
function invRecLink(val,isReceipt,rec,isCustBill){
if(isReceipt=="true")
        viewTransactionTemplate1("Sales Invoice", rec,false,val);     
    else
        viewTransactionTemplate1("Purchase Invoice", rec,false,val);
}
function openRec(isRec,isCustBill){
    if(isRec=="true")
        callReceipt();
    else
        callPayment();
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
Wtf.account.ReceiptReport=function(config){
    this.panelID=config.id;
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
    this.isDishonouredCheque=false;
     this.label=(this.isReceipt?'Receipt':'Payment');
    this.uPermType=(this.isReceipt?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.permType=(this.isReceipt?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice);
    this.exportPermType=(this.isReceipt?this.permType.exportdatareceipt:this.permType.exportdatapayment);
    this.printPermType=(this.isReceipt?this.permType.printreceipt:this.permType.printpayment);
    this.removePermType=(this.isReceipt?this.permType.removereceipt:this.permType.removepayment);
    this.editPermType=(this.isReceipt?this.permType.editreceipt:this.permType.editpayment);
    this.emailPermType=(this.isReceipt?this.permType.emailreceipt:this.permType.emailpayment);
    this.moduleid= config.moduleid;
    this.winValue= config.winValue;
    this.reportbtnshwFlag=config.reportbtnshwFlag;
    this.fromNavigationPanel= config.fromNavigationPanel;
    this.print=false;
    if(this.reportbtnshwFlag== undefined || this.reportbtnshwFlag == null)
       {
          this.reportbtnshwFlag=false;
       }
    if(this.isReceipt) {
        var arr = [[0,WtfGlobal.getLocaleText("acc.rem.105")+WtfGlobal.getLocaleText("acc.field.ExcludingContraEntry")],[4,WtfGlobal.getLocaleText("acc.field.ContraEntryRecords")],[3,WtfGlobal.getLocaleText("acc.field.AdvancePaymentfromCustomer")],[6,WtfGlobal.getLocaleText("acc.field.AdvancePaymentFromVendor")],[1,WtfGlobal.getLocaleText("acc.rem.106")],[2,WtfGlobal.getLocaleText("acc.rem.107")],[5,WtfGlobal.getLocaleText("acc.rem.225")],[14,WtfGlobal.getLocaleText("acc.rem.229")],[9,WtfGlobal.getLocaleText("acc.field.PaymentagainstGLCode")],[10,WtfGlobal.getLocaleText("acc.field.ReceivepaymentagainstCustomerInvoiceDebitNote")],[12,WtfGlobal.getLocaleText("acc.field.ReceivepaymentfromVendoragainstDebitNote")],[15,WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")]];
    } else {
        arr = [[0,WtfGlobal.getLocaleText("acc.rem.105")+WtfGlobal.getLocaleText("acc.field.ExcludingContraEntry")],[4,WtfGlobal.getLocaleText("acc.field.ContraEntryRecords")],[3,WtfGlobal.getLocaleText("acc.field.AdvancePaymenttoVendor")],[6,WtfGlobal.getLocaleText("acc.field.AdvancePaymenttoCustomer")],[1,WtfGlobal.getLocaleText("acc.rem.106")],[2,WtfGlobal.getLocaleText("acc.rem.107")],[5,WtfGlobal.getLocaleText("acc.rem.225")],[14,WtfGlobal.getLocaleText("acc.rem.229")],[9,WtfGlobal.getLocaleText("acc.field.PaymentagainstGLCode")],[11,WtfGlobal.getLocaleText("acc.field.MakepaymenttoCustomerCreditNote")],[13,WtfGlobal.getLocaleText("acc.field.MakepaymentagainstVendorInvoiceagainstCreditNote")],[15,WtfGlobal.getLocaleText("acc.field.OpeningBalanceRecords")]];
    }
    this.delTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :arr
    });
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.delTypeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid,
        valueField:'typeid',
        mode: 'local',
        value:0,
        disabled:this.winValue!=undefined,
//        hideLabel:this.winValue!=undefined,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    
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
        {name:'discount'},
        {name:'memo'},
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
        {name:'isprinted'}
        
    ]);
    this.expandStoreUrl = "";
    if(this.isReceipt){
        this.expandStoreUrl =  (this.isCustBill?"ACCReceipt/getBillingReceiptRows":"ACCReceiptCMN/getReceiptRows") + ".do";
    }else{
        this.expandStoreUrl =  (this.isCustBill?"ACCVendorPayment/getBillingPaymentRows":"ACCVendorPaymentCMN/getPaymentRows") + ".do";
    }

    this.expandStore = new Wtf.data.Store({
        url : this.expandStoreUrl,
//        url:Wtf.req.account+(this.isReceipt?'CustomerManager.jsp':'VendorManager.jsp'),
        baseParams:{
            mode:(this.isCustBill?36:33),
            dtype : 'report'
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
        {name: 'detailtype'},
        {name: 'expirydate',type:'date'},
        {name: 'journalentryid'},
        {name: 'entryno'},
        {name: 'currencysymbol'},
        {name: 'externalcurrencyrate'},
        {name: 'personname'},
//        {name: 'customervendorname'},
        {name: 'address'},
        {name: 'deleted'},
        {name: 'billdate',type:'date'},
        {name: 'paymentmethod'},
        {name: 'chequenumber'},
        {name: 'chequedescription'},
        {name: 'memo'},
        {name: 'amount'},
        {name: 'amountinbase'},
        {name: 'methodid'},
        {name: 'receiptamount'},
        {name: 'currencyid'},
        {name: 'detailsjarr'},
        {name: 'clearanceDate',type:'date'},
        {name: 'paymentStatus'},
        {name: 'otherwise'},
        {name: 'isOpeningBalanceTransaction'},
        {name: 'isNormalTransaction'},
        {name: 'receipttype'},
        {name: 'isadvancepayment'},
        {name: 'advanceamounttype'},
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
        {name:'sequenceformatid'},
        {name:'totaltaxamount'},
        {name: 'isIBGTypeTransaction',type:'boolean'},
        {name: 'ibgDetailsID'},
        {name: 'ibgCode'},
        {name:'paymentamountdue'},
        {name:'isLinked'},
        {name:'linkedadvanceMsgFlag'},
        {name:'disableOtherwiseLinking'},        
        {name:'cndnid'},
        {name:'invoiceadvcndntype'},
        {name:'cndnAndInvoiceId'},
        {name:'ischequeprinted', type:'boolean'}

    ]);

    this.userdsUrl = "";
    if(this.isReceipt){
        this.userdsUrl = "ACCReceipt/getReceipts.do";
    }else{
        this.userdsUrl = "ACCVendorPayment/getPayments.do";
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
                isprinted:false
            }
        });
    } else {
        this.userds = new Wtf.data.Store({
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
                isprinted:false
            }
        });
    }
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        value:WtfGlobal.getDates(true)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    
    this.fetchBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:this.isReceipt?WtfGlobal.getLocaleText("acc.pr.fetchTT"):WtfGlobal.getLocaleText("acc.pm.fetchTT"),  //"Select a time period to view corresponding make/receive payments.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.loadStore                        
    });
    
    this.userds.on('beforeload',function(s,o){
        WtfGlobal.setAjaxTimeOut();
        if(!o.params)o.params={};
        o.params.deleted=this.deleted;
        o.params.nondeleted=this.nondeleted;        
        o.params.onlyOpeningBalanceTransactionsFlag=this.onlyOpeningBalanceTransactionsFlag;        
        if(this.contraentryflag) {o.params.contraentryflag=this.contraentryflag;}
        if(this.advancePayment) {o.params.advancePayment=this.advancePayment;}
        if(this.advanceFromVendor) {o.params.advanceFromVendor=this.advanceFromVendor;}
        if(this.advanceToCustomer) {o.params.advanceToCustomer=this.advanceToCustomer;}
        if(this.isPostDatedCheque) {o.params.isPostDatedCheque=this.isPostDatedCheque;}
        if(this.isDishonouredCheque) {o.params.isDishonouredCheque=this.isDishonouredCheque;}
        if(this.isGlcode) {o.params.isGlcode=this.isGlcode;}    //added into params for GL Code
        if(this.recordType==10 || this.recordType==13){
            if(this.recordType!=undefined) {
                o.params.recordType="1,7";
            }   
        }else if(this.recordType==11 || this.recordType==12){
            if(this.recordType!=undefined) {
                o.params.recordType="6,7";
            } 
        } 
        if(this.isGlcodeValue!=null) {o.params.isGlcodeValue=9;}    //added into params for GL Code
        o.params.stdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        o.params.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
    },this);
    
    
    
    WtfComMsgBox(29,4,true);


    this.expander = new Wtf.grid.RowExpander({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    var btnArr=[];
    var bottombtnArr=[];
    this.gridColumnModelArr=[];
    this.gridColumnModelArr.push(this.sm,this.expander,{
        dataIndex:'billid',
        hidden:true
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
        renderer:WtfGlobal.rendererForAccountNameWithTax,
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
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header :WtfGlobal.getLocaleText("acc.prList.gridPaymentMethod"),  //'Payment Method',
        dataIndex: 'paymentmethod',
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
        header :Wtf.account.companyAccountPref.descriptionType,  //'Memo',
        dataIndex: 'memo',
        autoSize : true,
        sortable: true,
        groupable: true,
        renderer:WtfGlobal.deletedRenderer,
        pdfwidth:100
    },{
        header :WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),  //"Tax Amount",
        dataIndex: 'totaltaxamount',
        align:'right',
        pdfwidth:100,
        hidden:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
    },{
        header :WtfGlobal.getLocaleText("acc.prList.amtDue"), 
        dataIndex: 'paymentamountdue',
        align:'right',
        pdfwidth:100,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
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
        dataIndex: 'otherwise',
        hidden : !this.isReceipt,
        align:'right',
        pdfwidth:100,
        renderer:function(value){
            if(value)
                return 'No';
            else 
                return 'Yes';
        }
    });
 
    this.tbar2 = new Array();
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,this.fetchBttn);
    this.tbar2.push('->',(this.isReceipt?WtfGlobal.getLocaleText("acc.mp.prtype"):WtfGlobal.getLocaleText("acc.mp.payType")),this.typeEditor),
    this.gridView1 = config.consolidateFlag?new Wtf.grid.GroupingView({
            forceFit:false,
            showGroupName: true,
            enableNoGroups:false, // REQUIRED!
            hideGroupedColumn: true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }):{
            forceFit:false,
            emptyText:config.recordType==14?WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")):WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openRec(\""+this.isReceipt+"\",\""+this.isCustBill+"\")'>"+(this.isReceipt?WtfGlobal.getLocaleText("acc.nee.24"):WtfGlobal.getLocaleText("acc.nee.25"))+"</a>")//recordtype = 14 for dishonored cheques reports
        };
                // appening custom columns
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[this.moduleid],true);

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
        viewConfig: this.gridView1
    });  
    
    var colModelArray = GlobalColumnModelForReports[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray,this.userds);
     
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleid,
        advSearch: false
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
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:(this.isReceipt)?WtfGlobal.getLocaleText("acc.field.SearchbyReceiptnoJournalntryAccountName"):WtfGlobal.getLocaleText("acc.field.SearchbyPaymentnoJournalEntrynoAccountNameChequeNo"),  //'Search by Account Name...',
                width: 300,
                id:"quickSearch"+config.helpmodeid,
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
    
    this.resetBttn.on('click',this.handleResetClick,this);
    if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.editPermType) && Wtf.account.companyAccountPref.editTransaction){
                   btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
                    text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
                    tooltip :(this.isReceipt?WtfGlobal.getLocaleText("acc.prList.editTT"):WtfGlobal.getLocaleText("acc.pmList.editTT")),  //'Allows you to edit Receipt.',
                    id: 'btnEdit' + this.id,
                    scope: this,
                    hidden: (!this.fromNavigationPanel)?(config.consolidateFlag || this.reportbtnshwFlag):false,
                    iconCls :getButtonIconCls(Wtf.etype.edit),
                    disabled :true
                    }));
                this.editBttn.on('click',this.editTransaction,this);
               }
               
//    if(!WtfGlobal.EnableDisable(this.uPermType, this.copyPermType) && !this.pendingapproval){
    if(!this.isOrder){
        btnArr.push(this.copyReceiptBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.copy"),
            tooltip :WtfGlobal.getLocaleText("acc.field.CopyRecord"),
            id: 'btnCopy' + this.id,
            scope: this,
            hidden:(!this.isOrder),
            iconCls :getButtonIconCls(Wtf.etype.copy),
            disabled :true
        }));
        this.copyReceiptBttn.on('click',this.copyTransaction,this);
    }
     var deletebtnArray=[];
    if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction){
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
            hidden:(!this.isOrder),
            tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"), 
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            menu:deletebtnArray
       }));
  }
  //  btnArr.push('-');
    if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType)){
    //    btnArr.push('-');
        btnArr.push(this.email=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.email"),  //"Email",
            tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
            scope: this,
            hidden: this.isOrder || config.consolidateFlag,
            disabled: true,
            iconCls : "accountingbase financialreport",
            handler : this.sendMail
        }));
    }
   // btnArr.push('-');
   if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
   //     btnArr.push('-');
        bottombtnArr.push('-', this.printButton=new Wtf.exportButton({
            obj:this,
            id:"exportReports"+config.helpmodeid,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            disabled :true,
            menuItem:{csv:true,pdf:true},
            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment)
        }));
        bottombtnArr.push('-', this.singlePrint=new Wtf.exportButton({
            obj:this,
            id:"printReports"+config.helpmodeid+config.id,
            iconCls: 'pwnd exportpdfsingle',
            text: WtfGlobal.getLocaleText("acc.rem.39.single"),//Export single Record(Jasper)
            tooltip :WtfGlobal.getLocaleText("acc.rem.39.single"),
            disabled :true,
            isEntrylevel:false,
            menuItem:{
                rowPdf:true,
                rowPdfTitle:(WtfGlobal.getLocaleText("acc.rem.39")+" "+(this.isReceipt?WtfGlobal.getLocaleText("acc.receipt.1"):WtfGlobal.getLocaleText("acc.receipt.2")))
                },// + " "+ (this.isReceipt?WtfGlobal.getLocaleText("acc.invoiceList.recPay"):WtfGlobal.getLocaleText("acc.invoiceList.mP"))},
            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment),
            moduleid:config.moduleid
        }));
   }
   // btnArr.push('-');
  if(!this.isOrder&&!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
   //    btnArr.push('-');
        bottombtnArr.push('-', this.exportButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            menuItem:{print:true},
            params:{name:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.tabTitle"):WtfGlobal.getLocaleText("acc.pmList.tabTitle")},
            label:this.isReceipt?WtfGlobal.getLocaleText("acc.receipt.1"):WtfGlobal.getLocaleText("acc.receipt.2"),
            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment)
        }));

        bottombtnArr.push('-', this.singleRowPrint=new Wtf.exportButton({
            obj:this,
            id:"printSingleRecord"+config.helpmodeid+config.id,
            iconCls: 'pwnd printButtonIcon',
            text:WtfGlobal.getLocaleText("acc.rem.236"),
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
            disabled :true,
            isEntrylevel:false,
            menuItem:{rowPrint:true},
            get:this.isCustBill?(this.isReceipt?Wtf.autoNum.BillingReceipt:Wtf.autoNum.BillingPayment):(this.isReceipt?Wtf.autoNum.Receipt:Wtf.autoNum.Payment),
            moduleid:config.moduleid
        }));
    }

    btnArr.push(this.linkTrans=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.LinkTransaction"),
        scope: this,
        disabled :true,
        hidden: config.consolidateFlag,
        tooltip: WtfGlobal.getLocaleText("acc.field.LinkTransaction"),
        iconCls:'accountingbase pricelistbutton'
    }));

    this.linkTrans.on('click',this.calllinkInvoice,this);
    btnArr.push(this.printCheck=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.chequePrint"),
        scope: this,
        disabled :true,
        hidden: !(this.moduleid==Wtf.Acc_Make_Payment_ModuleId),
        tooltip: WtfGlobal.getLocaleText("acc.common.chequePrint"),
        iconCls:'accountingbase pricelistbutton'
    }));

    this.printCheck.on('click',this.printCkeck,this);
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
                tbar: [this.quickPanelSearch, 
                    this.resetBttn, this.newTabButton, this.AdvanceSearchBtn,btnArr, '-', getHelpButton(this, config.helpmodeid)
                ],
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
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.leadpan]
    });
   
    Wtf.account.ReceiptReport.superclass.constructor.call(this,config);
    this.addEvents({
        'invoice':true,
        'journalentry':true,
        'paymentupdate' : true
    });
    
    this.on('paymentupdate', function(){
        this.loadStore();
    }, this);
    
    this.userds.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
     }, this);
    this.userds.on('load',this.storeloaded,this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    this.grid.on('cellclick',this.onCellClick, this);
    this.userds.load({params:{start:0,limit:30}});
    this.typeEditor.on('select',this.loadTypeStore,this);
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);

}
Wtf.extend(Wtf.account.ReceiptReport,Wtf.Panel,{
   
   loadTypeStore:function(a,rec){
       if(this.startDate)
            this.startDate.setValue(WtfGlobal.getDates(true));
        if(this.endDate)
            this.endDate.setValue(WtfGlobal.getDates(false));
       this.showBarButtons();
       this.recordType=undefined;
        if(this.editBttn)this.editBttn.show();
        if(this.deleteMenu)this.deleteMenu.show();
        if(this.deleteTrans)this.deleteTrans.show();
      if(this.deleteTransPerm)this.deleteTransPerm.show();
        if(this.email)this.email.show();
        if(this.linkTrans)this.linkTrans.show();
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
        this.nondeleted=false;
        this.isGlcode=false;    //for  Advance payment against GL code
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
            this.advancePayment = true;        
        }else if(index==5){ // Advance Payments
           // this.nondeleted=true;
            this.isPostDatedCheque = true;
        }else if(index==6){ // Advance Receipts From Vendor
          //  this.nondeleted=true;
            this.advanceFromVendor = true;
            this.advanceToCustomer = true;
        }else if(index==9){ // Advance payment against GL code
           this.isGlcode=true;
             
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
                this.endDate.setValue(this.getFinancialYRStartDatesMinOne(false));
            
            this.onlyOpeningBalanceTransactionsFlag = true;
            this.hideButtons();       //To hide the buttons of Opening Balance RP Records
        } 
        
        if(index==14){ // Dishonoured Cheques
            this.isDishonouredCheque = true;
           if(this.deleteTransPerm) 
                this.deleteTransPerm.setText("Dishonoured");
            this.newTabButton.hide();
            this.AdvanceSearchBtn.hide();
            this.editBttn.hide();
            this.email.hide();
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
            if(this.singlePrint)this.singlePrint.hide();
            if(this.singleRowPrint)this.singleRowPrint.hide();
//            this.grid.getColumnModel().setHidden('paymentmethod',true) ;
            this.grid.getColumnModel().setHidden(8,true) ;//Payment method
            this.grid.getColumnModel().setColumnHeader(10,WtfGlobal.getLocaleText("acc.field.AmountAdjusted")) ;//Amount
        } else {
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
        if(this.exportButton)
            this.exportButton.show();   //Export to CSV/PDF.
    },
    
    showBarButtons:function(){
        if(this.newTabButton)
            this.newTabButton.show();
        if(this.AdvanceSearchBtn)
            this.AdvanceSearchBtn.show();
        if(this.printButton)
            this.printButton.show();
        if(this.exportButton)
            this.exportButton.show();
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
        var d=Wtf.serverDate;
        //        if(this.statementType=='BalanceSheet'&&start)
        //             return new Date('January 1, 1970 00:00:00 AM');
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
    
    setRecordType:function(recordType){
        this.typeEditor.setValue(recordType);
         var index=recordType;
      if(this.deleteTransPerm)     
        this.deleteTransPerm.enable();
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
            this.singlePrint.hide();
        } else {
            if(this.isReceipt){
                if(this.deleteTransPerm)   
                    this.deleteTransPerm.setText((WtfGlobal.getLocaleText("acc.prList.delete"))+" "+WtfGlobal.getLocaleText("acc.field.Permanently"));
            } else{
                if(this.deleteTransPerm)  
                    this.deleteTransPerm.setText(WtfGlobal.getLocaleText("acc.pmList.delete"));
            }  
        }
    
        if(index==4) {//Contra Entry
            this.contraentryflag = true;
            if(this.editBttn)this.editBttn.hide();
            if(this.deleteTransPerm)this.deleteTransPerm.hide();
            if(this.email)this.email.hide();
            if(this.linkTrans)this.linkTrans.hide();
            if(this.singlePrint)this.singlePrint.hide();
//            this.grid.getColumnModel().setHidden('paymentmethod',true) ;
            this.grid.getColumnModel().setHidden(8,true) ;//Payment method
            this.grid.getColumnModel().setColumnHeader(10,WtfGlobal.getLocaleText("acc.field.AmountAdjusted")) ;//Amount
        } else {
            this.grid.getColumnModel().setHidden(8,false) ;//Payment method
            this.grid.getColumnModel().setColumnHeader(10,this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")) ;//Amount Paid
        }
        
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
            if(this.email)this.email.enable();
            if(this.editBttn && !(rec.data.receipttype == 6 || rec.data.receipttype == 7)&& (rec.data.invoiceadvcndntype != 3))this.editBttn.enable();
            if(this.copyReceiptBttn && (rec.data.receipttype == 9||rec.data.receipttype == 2))this.copyReceiptBttn.enable();
//            if(this.deleteTrans)this.deleteTrans.enable();
            if(rec.data.otherwise  && !(rec.data.receipttype == 6 || rec.data.receipttype == 7) && !rec.data.ismanydbcr) {
                if(this.linkTrans)this.linkTrans.enable();
            }
            if(rec.data.paymentamountdue!=undefined && rec.data.paymentamountdue!=0 && !(rec.data.receipttype == 6 || rec.data.receipttype == 7) && !rec.data.ismanydbcr) {
                if(this.linkTrans)this.linkTrans.enable();
            }
//            if(rec.data.isLinked!=undefined && rec.data.isLinked) {//for checking if the payment is already linked 
//                if(this.editBttn)this.editBttn.disable();
//            }
            if(rec.data.disableOtherwiseLinking!=undefined && rec.data.disableOtherwiseLinking) {//disableling the linking button for otherwise
                if(this.linkTrans)this.linkTrans.disable();
            }
            if(rec.data.detailtype==2) {//enabling the print cheque button for bank type
                if(this.printCheck)this.printCheck.enable();
            }
            
        }else{
            if(this.email)this.email.disable();
            if(this.editBttn)this.editBttn.disable();
//            if(this.deleteTrans)this.deleteTrans.disable();
            if(this.linkTrans)this.linkTrans.disable();
            if(this.copyReceiptBttn)this.copyReceiptBttn.disable();
            if(this.printCheck)this.printCheck.disable();
          }
        if(this.sm.getCount()>=1){
            if(this.singlePrint)this.singlePrint.enable();
            if(this.singleRowPrint)this.singleRowPrint.enable();
        }else{
            if(this.singlePrint)this.singlePrint.disable();
            if(this.singleRowPrint)this.singleRowPrint.disable();
        }
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
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.userds.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true,this.consolidateFlag);
        }
        if(header=="billno"){
            this.viewTransection(g,i,j);
        }
    },
    loadStore:function(){
      this.userds.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
    },
    onRowexpand:function(scope, record, body){
        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray,this.expandStore);
        this.expanderBody=body;        
        if(this.label=='Receipt'){
            if(record.data.withoutinventory){
                this.expandStore.proxy.conn.url="ACCReceipt/getBillingReceiptRows.do";                                
            }                
            else{
                this.expandStore.proxy.conn.url="ACCReceiptCMN/getReceiptRows.do";
            }                
        }
        if(this.label=='Payment'){
            if(record.data.withoutinventory){
                this.expandStore.proxy.conn.url="ACCVendorPayment/getBillingPaymentRows.do";                
            }                
            else{
                this.expandStore.proxy.conn.url="ACCVendorPaymentCMN/getPaymentRows.do";
            }                
        }
        this.expandStore.load({params:{bills:record.data['billid']}});
    },
    openNewTab:function(){
        callCreateNewButtonFunction(this.moduleid,this.recordType); 
    },
    fillExpanderBody:function(){
        if(this.expandStore.getCount()>0) {
            var disHtml = "";
            var arr=[];
            // arr=['Invoice No','Creation Date','Due Date','Invoice Amount','Amount Due',(this.isReceipt?'Amount Received':'Amount Paid'),"
            arr=[WtfGlobal.getLocaleText("acc.prList.invNo"),WtfGlobal.getLocaleText("acc.prList.creDate"),WtfGlobal.getLocaleText("acc.prList.dueDate"),WtfGlobal.getLocaleText("acc.prList.invAmt"),WtfGlobal.getLocaleText("acc.prList.amtDue"),(this.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")),"                "];
            var gridHeaderText = this.isCustBill?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
            var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";  //Product List
            var custArr = [];
            custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[this.moduleid]);
            var arrayLength=arr.length;
            for(i=0;i<custArr.length;i++){
                if(custArr[i].header != undefined )
                    arr[arrayLength+i]=custArr[i].header;
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
            for(i=0;i<this.expandStore.getCount();i++){
                var rec=this.expandStore.getAt(i);
                header += "<span class='gridNo'>"+(i+1)+".</span>";
                header += "<span class='gridRow ' style='width:"+widthInPercent+"% ! important;'>"+"<a  class='jumplink' href='#' onClick='javascript:invRecLink(\""+rec.data['transectionid']+"\",\""+this.isReceipt+"\",\""+rec+"\",\""+this.isCustBill+"\")'>"+rec.data['transectionno']+"</a>"+"</span>";
                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['creationdate'])+"</span>";
                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['duedate'])+"</span>";
                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['totalamount'],rec.data['currencysymbol'],[true])+"</span>";
                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['amountdue'],rec.data['currencysymbol'],[true])+"</span>";
                header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['amountpaid'],rec.data['currencysymbol'],[true])+"</span>";
                for(var j=0;j<custArr.length;j++){
                    if(rec.data[custArr[j].dataIndex]!=undefined && rec.data[custArr[j].dataIndex]!="null" && rec.data[custArr[j].dataIndex]!="")
                         header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[j].dataIndex]+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[j].dataIndex],15)+"&nbsp;</span>";
                    else
                        header += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                }
                header +="<br>";
            }
            header +="</div>";
            disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
            this.expanderBody.innerHTML = disHtml;
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
     viewTransection:function(grid, rowIndex, columnIndex){
        var formrec=null;
    if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
                WtfComMsgBox(15,2);
                return;
        }
    formrec = this.grid.getStore().getAt(rowIndex);
        if(this.isReceipt)
            {           
                callViewPayment(formrec, 'ViewReceivePayment',true,this.grid);
            }
        else
            {              
                callViewPayment(formrec, 'ViewPaymentMade',false,this.grid);
            }
    },
    editTransaction:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
         var rec = this.sm.getSelected();
         if(this.sm.getCount()==1 && rec.data.deleted != true){
            if(rec.data.isLinked!=undefined && rec.data.isLinked) {//for checking if the payment is already linked 
                if(rec.data.linkedadvanceMsgFlag!=undefined&&rec.data.linkedadvanceMsgFlag){
                    WtfComMsgBox(115,2);
                }else{
                    WtfComMsgBox(114,2);
                }
                return;
            }
         }
        formrec = this.grid.getSelectionModel().getSelected();
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
        if(formrec.data.invoiceadvcndntype!=undefined&&formrec.data.invoiceadvcndntype!=0){
            Wtf.Ajax.requestEx({
                url:this.isReceipt?"ACCReceipt/getInvoiceAdvanceCNDN.do":"ACCVendorPayment/getInvoiceAdvanceCNDN.do",
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
                        (formrec.data.withoutinventory)?callEditBillPayment(formrec, 'EditBillingReceivePayment',true):callEditPayment(formrec, 'EditReceivePayment',true,this.grid);
                    else
                        (formrec.data.withoutinventory)?callEditBillPayment(formrec, 'EditBillingPaymentMade',false):callEditPayment(formrec, 'EditPaymentMade',false,this.grid);
                }
            },function(){});
        }else{
            if(this.isReceipt)
                (formrec.data.withoutinventory)?callEditBillPayment(formrec, 'EditBillingReceivePayment',true):callEditPayment(formrec, 'EditReceivePayment',true,this.grid);
            else
                (formrec.data.withoutinventory)?callEditBillPayment(formrec, 'EditBillingPaymentMade',false):callEditPayment(formrec, 'EditPaymentMade',false,this.grid);

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
            if(this.isReceipt)
                (formrec.data.withoutinventory)?callEditBillPayment(formrec, 'EditBillingReceivePayment',true):callEditPayment(formrec, 'CopyReceivePayment',true,this.grid,copyReceiptTransaction);
            else
                (formrec.data.withoutinventory)?callEditBillPayment(formrec, 'EditBillingPaymentMade',false):callEditPayment(formrec, 'CopyPaymentMade',false,this.grid,copyReceiptTransaction);
    },
    sendMail:function(){

        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                WtfComMsgBox(15,2);
                return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.isReceipt)
            (formrec.data.withoutinventory)? callEmailWin("emailwin",formrec,this.label,12,false,false): callEmailWin("emailwin",formrec,this.label,4,false,false);
        else
            (formrec.data.withoutinventory)? callEmailWin("emailwin",formrec,this.label,16,false,false): callEmailWin("emailwin",formrec,this.label,8,false,false);




    },
    handleDelete:function(del){
        if(!this.grid.getSelectionModel().hasSelection()){
            WtfComMsgBox(34,2);
            return;
        }
        var delFlag=del;
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
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
                this.deleteUrl = "ACCReceipt/deleteReceiptMerged.do";
              }
              else
              {
//                  this.deleteUrl = "ACCReceipt/deleteReceiptPermanent.do";
                  this.deleteUrl = "ACCReceipt/deleteReceiptForEdit.do";
              }   
            
        }else{
             if(delFlag=='del')
              {
                    this.deleteUrl = "ACCVendorPayment/deletePaymentMerged.do";
              }
              else
              {
//                  this.deleteUrl = "ACCVendorPayment/deletePaymentPermanent.do";
                    this.deleteUrl = "ACCVendorPayment/deletePaymentForEdit.do";
              }   
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
        var Printdate=WtfGlobal.convertToGenericDate(formRecord.get("billdate"));
        var paymentMethod=formRecord.get("methodid");
        var name=formRecord.get("paidto");
        var paymentid=formRecord.get("billid");
        var paymentno=formRecord.get("billno");
        var currencyid=formRecord.get("currencyid");
        var chequeno=formRecord.get("chequenumber"); 
        this.printUrl = "ACCVendorPayment/printCheck.do";              
    if(formRecord.get("receipttype")=="9"){
        name=formRecord.get("paidto");
        if(formRecord.get("paidto")==undefined||formRecord.get("paidto")==""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PleaseEnterPaidto")],2);     
            return;
        } 
    }else{
        name= formRecord.get("personname"); 
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
                chequeno:chequeno
            }
        },this,this.genPrintSuccessResponse,this.genFailureResponse);  
    },
    genSuccessResponse:function(response){ 
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
        this.printDetailswin=new Wtf.account.OSDetailPanel({
            });
        this.printDetailswin.printCheque(resdata.accountName, resdata.amount, resdata.amountinword,resdata.amountinword1, resdata.date, resdata.dateLeft, resdata.nameLeft, resdata.amtinwordLeft,resdata.amtinwordLeftLine2,resdata.amtLeft,resdata.dateTop, resdata.nameTop, resdata.amtinwordTop,resdata.amtinwordTopLine2,resdata.amtTop,this);
        this.print = false;
}
    },   
    genFailureResponse:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.grid.getStore().indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },


    calllinkInvoice:function(winid, reconRec){
        reconRec=(reconRec==undefined?"":reconRec);
        winid=(winid==null?"iaffilewin":winid);
        var panel = Wtf.getCmp(winid);
        var record = this.sm.getSelected();
        if(!panel){
            new Wtf.account.linkInvoicePayment({
                id:winid,
                reloadGrid:this.grid.id,
                isCustBill:this.isCustBill,
                isReceipt:this.isReceipt,
                closable: true,
                record: record,
                modal: true,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                width: 650,
                height: 600,
                resizable: false,
                layout: 'border',
                buttonAlign: 'right',
                renderTo: document.body
            }).show();
        }
    },
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


    }
});
    