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
function invLink(val,isCustomer, isCustBill){
    if(isCustomer=="true")
        callInvoiceList(val,true);
    else
        callGoodsReceiptList(val,true);
}
function openNote(isNote, isCustBill){
    if(isNote=="true"){
        callCreditNote(true);
    }else{
        callCreditNote(false);           //isCustBill=="true"?callBillingDebitNote():callDebitNote();
    }    
}
/* < COMPONENT USED FOR >
 *  1.Give Refund or Details
 *      callCreditNoteDetails() --- <Credit Note Details>
 *      [isCNReport:true]
 *  2.Debit Note Report
 *      callDebitNoteDetails() --- <Debit Note Details>
 *      [isCNReport:false]
 */
Wtf.account.NoteDetailsPanel=function(config){
    this.businessPerson=(config.isCNReport?'Customer':'Vendor');
    this.costCenterId = "";
    this.isCNDN=true;
    this.recArr=[];
    this.extraFilters = config.extraFilters;
    if(config.extraFilters != undefined){//Cost Center Report View
        this.costCenterId = config.extraFilters.costcenter?config.extraFilters.costcenter:"";
    }
    this.transType=(config.isCNReport?'Credit':'Debit');
    this.isForAgainstInvoice=config.isForAgainstInvoice;
    this.isCustomer=config.isCustRecord;
    this.uPermType=(config.isCNReport?Wtf.UPerm.creditnote:Wtf.UPerm.debitnote);
    this.permType=(config.isCNReport?Wtf.Perm.creditnote:Wtf.Perm.debitnote);
    this.exportPermType=(config.isCNReport?this.permType.exportdatacn:this.permType.exportdatadn);
    this.printPermType=(config.isCNReport?this.permType.printcn:this.permType.printdn);
    this.removePermType=(config.isCNReport?this.permType.removecn:this.permType.removedn);
    this.editPermType=(config.isCNReport?this.permType.editcn:this.permType.editdn);
    this.copyPermType=(config.isCNReport?this.permType.copycn:this.permType.copydn);
    this.moduleid= config.moduleId;
    this.inputType=config.inputType;
    this.reportbtnshwFlag=config.reportbtnshwFlag;
    
     if (dojoInitCount <= 0) {
        dojo.cometd.init("../../bind");
        dojoInitCount++;
    }
    
    var channelName = "";
    if (this.moduleid == Wtf.Acc_Debit_Note_ModuleId) {
        channelName =Wtf.ChannelName.DebitNoteReport;
    }else if(this.moduleid == Wtf.Acc_Credit_Note_ModuleId){
        channelName = Wtf.ChannelName.CreditNoteReport;
    } 
    if (channelName != "") {
        dojo.cometd.subscribe(channelName, this, "globalInvoiceListGridAutoRefreshPublishHandler");
    }
    
    if(this.reportbtnshwFlag== undefined || this.reportbtnshwFlag == null)
       {
          this.reportbtnshwFlag=false;
       }
    this.expandRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'rowid'},
        {name:'billno'},
        {name:'transectionid'},
        {name:'transectionno'},
        {name:'productid'},
        {name:'unitname'},
        {name:'currencysymbol'},
        {name:'productname',mapping:"productdetail"},
        {name:'desc'/*,convert:this.shortString*/},
        {name:'quantity'},
        {name:'discount'},
        {name:'amount'},
        {name:'memo'/*,convert:this.shortString*/},
        {name:'remark'},
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
        {name:'accountname'},
        {name:'description'},
        {name:'totalamount'},
        {name:'isaccountdetails'},
        {name:'isCnDndetails'},
        {name:'isOpeningDnCn'},
        {name:'isCreditNote'},
        {name:'taxpercent'},
        {name:'isIncludingGst'},
        {name:'taxamount'},
        {name: 'debit'},
        {name:'isOpeningInvoice'},
        {name:'customfield'}
    ]);
    this.expandStoreUrl = "";
    //mode:config.isCNReport?(config.isCustBill?63:28):(config.isCustBill?63:29)
    if(config.isCNReport){
        this.expandStoreUrl = config.isCustBill?"ACCCreditNote/getBillingCreditNoteRows.do":"ACCCreditNoteCMN/getCreditNoteRows.do";
    }else {
        this.expandStoreUrl = config.isCustBill?"ACCDebitNote/getBillingDebitNoteRows.do":"ACCDebitNote/getDebitNoteRows.do";
    }
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
//        url:Wtf.req.account+this.businessPerson+'Manager.jsp',
        baseParams:{
            mode:config.isCNReport?(config.isCustBill?63:28):(config.isCustBill?63:29),
            deleted:"false",
            nondeleted:"false",
            dtype : 'report'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expandStore.setDefaultSort('billid', 'desc');
    this.GridRec = Wtf.data.Record.create ([
        {name:"noteid"},
        {name:"noteno"},
        {name:"billno"},
        {name:"linkto"},
        {name:"lasteditedby"},
        {name:"includeprotax"},
        {name:"linktype"},
        {name:"linkid"},
        {name:"linktransactionamountdue"},
        {name:"currencyid"},
        {name:'companyid'},
        {name:'companyname'},
        {name:'journalentryid'},        
        {name:'currencysymbol'},
        {name:'entryno'},
        {name:"personid"},
        {name:"personname"},
        {name:"personaccountid"}, // Person vendor account id
        {name:"aliasname"},
        {name:'amount'},
        {name:'amountinbase'},
        {name:'amountdue'},
        {name:'costcenterid'},
        {name:'deleted'},
        {name:'costcenterName'},
        {name:"date",type:'date'},
        {name:'linkingdate' , type:'date'},
        {name:'memo'},
        {name:'salesPerson'},
        {name:'notetax'},
        {name:'noteSubTotal'},
        {name:'withoutinventory'},
        {name:'isReturnNote'},
        {name:'otherwise'},
        {name:'openflag'},
        {name:'isOpeningBalanceTransaction'},
        {name:'isNormalTransaction'},
        {name:'isOldRecord'},
        {name:'cntype'},
        {name:'isprinted'},
        {name:'isCreatedFromReturnForm'},
        {name:'partlyJeEntryWithCnDn'},
        {name:'sequenceformatid'},
        {name:'externalcurrencyrate'},
        {name:'amountdueinbase'},
        {name:'reason'},
        {name:'isCopyAllowed'},
        {name:'taxamount'},
        {name:'amountbeforegst'},
        {name:'attachdoc'}, //SJ[ERP-16331]
        {name:'attachment'},//SJ[ERP-16331]
        {name:'billid'},//SJ[ERP-16331]
        {name:'isLinked'},
        {name:'isNoteLinkedToAdvancePayment'}, // Whether the CN/DN is linked to Advance MP/RP
        {name:'currencycode'},
        {name: 'salesPersonID'},
        {name: 'isreval'},
        {name: 'isLinkedTransaction'},
        {name:'approvalstatusinfo'},
        {name:'approvalLevel'},
        {name:'isFinalLevelApproval'},
        {name:'hasApprovalAuthority'},
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
        {name:'createdby'},
        {name:'agent'},
        {name:'gTaxId'},
        {name:'agentid'},
        {name:'isLinkedInvoiceIsClaimed'},
        {name:'supplierinvoiceno'},//SDP-4510
        {name:'hasAccess'},
        {name:'mvattransactionno'},
        {name:'linkInvoices'},
        {name:'CustomerVendorTypeId'},
        {name:'GSTINRegistrationTypeId'},
        {name:'gstin'},
        {name:'gstdochistoryid'},
        {name:'termAmount'},
        {name:'personcode'},
        {name:'gstCurrencyRate'}
    ]);
    this.StoreUrl = "";
    this.RemoteSort= false;
    //mode:config.isCNReport?(config.isCustBill?62:27):(config.isCustBill?62:28)
    if(config.isCNReport){
        this.StoreUrl = "ACCCreditNote/getCreditNoteMerged.do";
	this.RemoteSort= true;
    }else {
        this.StoreUrl = "ACCDebitNote/getDebitNoteMerged.do";
	this.RemoteSort= true;
    }
    if(config.consolidateFlag){
        this.Store = new Wtf.data.GroupingStore({
            url:this.StoreUrl,
	    remoteSort: this.RemoteSort,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                mode:config.isCNReport?(config.isCustBill?62:27):(config.isCustBill?62:28),
                costCenterId: this.costCenterId,
                deleted:"false",
                nondeleted:"false",
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid
            },
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.GridRec)
        });
    } else {
        this.Store = new Wtf.data.Store({
            url:this.StoreUrl,
	    remoteSort: this.RemoteSort,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                mode:config.isCNReport?(config.isCustBill?62:27):(config.isCustBill?62:28),
                costCenterId: this.costCenterId,
                deleted:"false",
                nondeleted:"false",
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isprinted:false
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.GridRec)
        });
    }
    if(this.extraFilters != undefined){//Cost Center Report View
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = this.extraFilters.startdate;
        currentBaseParams.enddate = this.extraFilters.enddate;
        this.Store.baseParams=currentBaseParams;
    }    
     WtfComMsgBox(29,4,true);
     
    var dataArr = new Array();   
    if (Wtf.Countryid == Wtf.Country.MALAYSIA) {//ERM-778
        if (config.isCNReport) {
            dataArr.push([1, "Credit Note for Customers"],[6,"Credit Note for Overcharged Sales Invoice"], [4, "Credit Note for Undercharged Purchase Invoice"], [10, "Opening Credit Note for Customers"], [11, "Opening Credit Note for Vendors"], [12, "Credit Note With Sales Return"], [13, "Credit Note Without Sales Return"]);
        } else {
            dataArr.push([1, "Debit Note for Vendors"],[6,"Debit Note for Overcharged Purchase Invoice"], [4, "Debit Note for Undercharged Sales Invoice"], [10, "Opening Debit Note for Vendors"], [11, "Opening Debit Note for Customers"], [12, "Debit Note With Purchase Return"], [13, "Debit Note Without Purchase Return"]);
        }
    } else if (Wtf.Countryid == Wtf.Country.SINGAPORE) {//SDP-13587
        if (config.isCNReport) {
            dataArr.push([1, "Credit Note for Customers"], [4, "Credit Note for Vendors"], [6, "Credit Note for Overcharged Sales Invoice"], [5, "Credit Note for Undercharged Purchase Invoice"], [10, "Opening Credit Note for Customers"], [11, "Opening Credit Note for Vendors"], [12, "Credit Note With Sales Return"], [13, "Credit Note Without Sales Return"]);
        } else {
            dataArr.push([1, "Debit Note for Vendors"], [4, "Debit Note for Customers"], [6, "Debit Note for Overcharged Purchase Invoice"], [5, "Debit Note for Undercharged Sales Invoice"], [10, "Opening Debit Note for Vendors"], [11, "Opening Debit Note for Customers"], [12, "Debit Note With Purchase Return"], [13, "Debit Note Without Purchase Return"]);
        }
    }else{
        if(config.isCNReport){
            dataArr.push([1,"Credit Note for Customers"],[4,"Credit Note for Vendors"],[10,"Opening Credit Note for Customers"],[11,"Opening Credit Note for Vendors"],[12,"Credit Note With Sales Return"],[13,"Credit Note Without Sales Return"]);
        } else {
            dataArr.push([1,"Debit Note for Vendors"],[4,"Debit Note for Customers"],[10,"Opening Debit Note for Vendors"],[11,"Opening Debit Note for Customers"],[12,"Debit Note With Purchase Return"],[13,"Debit Note Without Purchase Return"]);
        }
    }
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :dataArr
    });
    this.typeEditor = new Wtf.form.FnComboBox({
        store: this.typeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid+config.id,
        valueField:'typeid',
        mode: 'local',
        defaultValue:0,
        width:160,
        hidden:this.winValue!=undefined,
        hideLabel:this.winValue!=undefined,
        listWidth:160,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus: true,
        value: 1,
        listeners: {
            scope: this,
            'select': function(combo, record, index) {
                if (record.data.typeid == 10 || record.data.typeid == 11) {
                    this.expandCollpseButton.disable();
                }else{
                    this.expandCollpseButton.enable();
                }
            }
        }
    });    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        value:WtfGlobal.getDates(true)
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    
    this.fetchBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:config.isCNReport?WtfGlobal.getLocaleText("acc.cn.fetchTT"):WtfGlobal.getLocaleText("acc.dn.fetchTT"),  //"Select a time period to view corresponding credit/debit note records.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this
//        handler:this.loadCMStore                        
    }); 
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
//        hidden:(this.moduleid == Wtf.Acc_Sales_Return_ModuleId || this.moduleid == Wtf.Acc_Purchase_Return_ModuleId)?false:true,
        scope: this,
        handler: function() {
            if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
                this.expandButtonClicked = true;
            }
            expandCollapseGrid(this.expandCollpseButton.getText(), this.expandStore, this.grid.plugins, this);
        }
    });
    this.approvalHistoryBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.ApprovalHistory"),
        scope: this,
        disabled : true,
        tooltip: WtfGlobal.getLocaleText("acc.field.ViewApprovalHistory"),
        handler: this.viewApprovalHistory,
        iconCls: "advanceSearchButton"
    });
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.search"):WtfGlobal.getLocaleText("acc.dnList.search")), //'Quick Search by ' +(config.isCNReport?'Credit':'Debit')+ ' Note No',
        width: 200,
        id:"quickSearch"+config.helpmodeid,
        maxLength:50,
        field: "noteno"
    });
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
    this.exportButton= new Wtf.exportButton({
        obj:this,
        //id:"exportReports"+config.helpmodeid, //ERP-39380  [Regression Testing][DN][Export window can be seen at the corner of the screen]
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        usePostMethod:(this.moduleid == Wtf.Acc_Debit_Note_ModuleId || this.moduleid == Wtf.Acc_Credit_Note_ModuleId)?true:false,
        disabled :true,
        menuItem:{
            csv:true,
            pdf:true,
            xls:true,
            print:true,
            detailedXls:true
        },
        filename:config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.tabTitle")+"_v1":WtfGlobal.getLocaleText("acc.dnList.tabTitle")+"_v1",
        get:config.isCustBill?(config.isCNReport?Wtf.autoNum.BillingCreditNote:Wtf.autoNum.BillingDebitNote):(config.isCNReport?Wtf.autoNum.CreditNote:Wtf.autoNum.DebitNote),
        moduleId:config.moduleId
    });
    }
// if(config.isCustRecord && config.isCustRecord!=undefined){
    if(config.cntypeNo){// in case of creating CN/DN From navigation panel cntypeno will contain value else not
        this.typeEditor.setValue(config.cntypeNo);
        this.cntype=config.cntypeNo;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.cntype = this.cntype;
        this.Store.baseParams=currentBaseParams;
    }
    
    if(this.inputType!=undefined && Wtf.Countryid == Wtf.Country.MALAYSIA){// in case of creating CN/DN From navigation panel cntypeno will contain value else not
        this.typeEditor.setValue(config.cmbNo);
    }
    this.Store.on('beforeload',function(store,option){
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.cntype = this.typeEditor.getValue();
        currentBaseParams.pendingapproval=config.pendingapproval;
        if(this.grid.getStore().getSortState()!=undefined){
            currentBaseParams.sort=this.grid.getStore().getSortState().field;
            currentBaseParams.dir=this.grid.getStore().getSortState().direction;
        }
        this.Store.baseParams=currentBaseParams;
        /*The params for Exportbutton Set on Store Loaded ERP-19292 (SDP-881) */
        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
            this.exportButton.params=currentBaseParams;
            this.exportButton.setParams({
                cntype : this.typeEditor.getValue(),
                enddate :  WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                isCreditNote : config.isCNReport,
                ss : (this.quickPanelSearch.getValue()==undefined)?"":this.quickPanelSearch.getValue(),
                mode:config.isCNReport?(config.isCustBill?62:27):(config.isCustBill?62:28),
                costCenterId: this.costCenterId,
                deleted:"false",
                nondeleted:"false",
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isprinted:false
            });
        }
    },this);
//    this.Store.load({params:{start:0,limit:30}});
    this.Store.on('datachanged', function(){        
        var p = this.pP?this.pP.combo.value:30;        
        this.quickPanelSearch.setPage(p);
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
        this.expander.resumeEvents('expand');           // event is suspended while expanding all records.
    }, this);
    this.Store.on('load',this.storeloaded,this);
    this.expander = new Wtf.grid.RowExpander();
    
    this.fetchBttn.on("click", this.fetchHandler, this);
    this.tbar2 = new Array();
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate);
                        
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.gridView1 = config.consolidateFlag?new Wtf.grid.GroupingView({
            forceFit:false,
            showGroupName: true,
            enableNoGroups:false, // REQUIRED!
            hideGroupedColumn: true,
            emptyText:(this.isForAgainstInvoice)?"<center>No record to display</center>":WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openNote(\""+config.isCNReport+"\",\""+config.isCustBill+"\")'> "+WtfGlobal.getLocaleText("acc.rem.147")+" "+(config.isCNReport?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN"))+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")
        }):{
            forceFit:false,
            emptyText:(this.isForAgainstInvoice)?"<center>No record to display</center>":WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openNote(\""+config.isCNReport+"\",\""+config.isCustBill+"\")'> "+WtfGlobal.getLocaleText("acc.rem.147")+" "+(config.isCNReport?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN"))+" "+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")
        };

    this.gridColumnModelArr=[];
    this.gridColumnModelArr.push(this.sm,this.expander,{
            header:WtfGlobal.getLocaleText("acc.field.Company"),  
            dataIndex:'companyname',
            width:20,
            pdfwidth:150,
            sortable:true,
            hidden:true
        },{
            header:"",
            dataIndex:'isprinted',
            width:60,
            renderer : function(val, meta, record, rowIndex){
                if(record.data.isprinted){
                    return '<img id="printValiFlag" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.printed')+'" src="../../images/printed.gif">';
                }else{
                    return '<img id="printInValiFlag" style="margin-left: 3px;cursor:pointer" wtf:qtip="'+WtfGlobal.getLocaleText('acc.invoiceList.notprinted')+'" src="../../images/not-printed.gif">'
                }
            }
        },{
            header:(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.gridNoteNo"):WtfGlobal.getLocaleText("acc.dnList.gridNoteNo")),  //this.transType+" Note No",
            dataIndex:"noteno",
            sortable:this.RemoteSort,
            width:150,
            pdfwidth:100,
            renderer:config.consolidateFlag?WtfGlobal.deletedRenderer:WtfGlobal.linkDeletedRenderer
        },{
            header:(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.gridDate"):WtfGlobal.getLocaleText("acc.dnList.gridDate")),  //this.transType+" Date",
            dataIndex:"date",
            align:'center',
            width:150,
            pdfwidth:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            sortable:this.RemoteSort
        },{
            header:WtfGlobal.getLocaleText("acc.userAdmin.name"), //"Name",   /(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.gridCustomerName"):WtfGlobal.getLocaleText("acc.dnList.gridVendorName")),  //this.businessPerson+" Name",
            dataIndex:"personname",
            width:150,
            pdfwidth:100,
            renderer:WtfGlobal.deletedRenderer,
            sortable:this.RemoteSort
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridAliasName"), 
            dataIndex:"aliasname",
            width:150,
            pdfwidth:100,
            renderer:WtfGlobal.deletedRenderer,
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.gridJEno"),  //"Journal Entry No",
            dataIndex:'entryno',
            width:150,
            sortable:this.RemoteSort,
            pdfwidth:100,
            renderer:WtfGlobal.multipleJELinkDeletedRenderer
        },{
            header:Wtf.account.companyAccountPref.descriptionType,  //"Memo",
            dataIndex:'memo',
            width:150,
            pdfwidth:100,
            sortable:true,
            //renderer:WtfGlobal.deletedRenderer
             renderer : function(value,meta,rec){
            value = value.replace(/\'/g, "&#39;");
            value = value.replace(/\"/g, "&#34");
            value = "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
            if (!value)
                return value;
            if(rec.data.deleted)
                value='<del>'+value+'</del>';
            return value;
           
                          }
        }, {
            header: (this.moduleid == Wtf.Acc_Credit_Note_ModuleId) ? WtfGlobal.getLocaleText("acc.invoiceList.salesPerson") : WtfGlobal.getLocaleText("acc.common.agent"),
            dataIndex: (this.moduleid == Wtf.Acc_Credit_Note_ModuleId) ? 'salesPerson' : 'agent',
            pdfwidth:75,
            hidden:this.moduleid!= Wtf.Acc_Credit_Note_ModuleId
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.SupplierInvoiceNo"), //Supplier Invoice No (SDP-4510)
            dataIndex: 'supplierinvoiceno',
            width: 150,
            pdfwidth: 80,
            hidden: config.isCNReport
        },{
            header:WtfGlobal.getLocaleText("acc.nee.69"),
            dataIndex:'createdby',
            width:150,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
            dataIndex:'currencycode',
            hidden:true,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.taxAmt"),  //"Tax Amount",
            dataIndex:'taxamount',
            align:'right',
            width:150,
            pdfwidth:75,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            hidden: (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST)// hide if company is malaysian and GST is not enabled for it
        },{
            header: Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ? WtfGlobal.getLocaleText("acc.payment.amtBeforeTax") :WtfGlobal.getLocaleText("acc.invoiceList.AmntBeforeGST"),  //"Amount before GST / VAT",
            align:'right',
            dataIndex:'amountbeforegst',
            width:150,
            pdfwidth:75,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            hidden: (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST)// hide if company is malaysian and GST is not enabled for it
        },
        {
            header: WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome") + " ("+WtfGlobal.getCurrencyName()+")",  //Total Amount in Base
            dataIndex:'amountinbase',
            align:'right',
            width:150,
            pdfwidth:100,
            renderer:WtfGlobal.currencyDeletedRenderer
        },
        {
            header:WtfGlobal.getLocaleText("acc.dnList.gridAmt"),  //"Amount",
            dataIndex:'amount',
            align:'right',
            width:150,
            pdfwidth:100,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.gridAmtDue"),  //"Amount Due",
            dataIndex:'amountdue',
            align:'right',
            width:150,
            pdfwidth:100,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.amtDueInBase"),  //"Amount Due",
            dataIndex:'amountdueinbase',
            align:'right',
            width:150,
            pdfwidth:100,
            renderer:WtfGlobal.currencyDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.reason"),  //"Reason",
            dataIndex:'reason',
            align:'left',
            width:150,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.field.Approval") +WtfGlobal.getLocaleText("acc.invoiceList.status"),  //Approval Status
            dataIndex:'approvalstatusinfo',
            align:'left',
            width:150,
            pdfwidth:100,
            renderer:WtfGlobal.deletedRenderer,
            hidden : !config.pendingapproval
        });
         //* Attachment Document in grid report Column model   SJ[ERP-16331]   
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
    this.gridColumnModelArr.push({
        header: (this.moduleid == Wtf.Acc_Credit_Note_ModuleId) ? WtfGlobal.getLocaleText("acc.common.customer.code") : WtfGlobal.getLocaleText("acc.common.vendor.code"),
        dataIndex: 'personcode',
        width: 150,
        pdfwidth: 100
    });
    
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[this.moduleid],true);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        id:"gridmsg"+config.helpmodeid+config.id,
        store:this.Store,
        tbar:this.tbar2,
        autoScroll:true,
        sm : this.sm,
        border:false,
        layout:'fit',
        viewConfig:this.gridView1,
//        forceFit:true,
        loadMask : true,
        plugins: this.expander,
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr)
    });
    var colModelArray = GlobalColumnModelForReports[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray,this.Store);
    this.Store.load({params:{start:0,limit:30}});
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleid,
        advSearch: false,
        customerCustomFieldFlag: this.showCustomerCustomFieldFlag(this.moduleid),
        vendorCustomFieldFlag: this.showVendorCustomField(this.moduleid)
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    this.deletebtnArray=[];
    this.deleteBtn=new Wtf.Action({
        text: ((this.transType=="Credit")?WtfGlobal.getLocaleText("acc.cnList.deleteNote"):WtfGlobal.getLocaleText("acc.dnList.deleteNote")),  //'Delete '+this.transType+' Note',
        scope: this,
        disabled :true,
        hidden : this.reportbtnshwFlag || config.consolidateFlag, //SDP-12096
        tooltip:((this.transType=="Credit")?WtfGlobal.getLocaleText("acc.cnList.deleteNoteTT"):WtfGlobal.getLocaleText("acc.dnList.deleteNoteTT")),  //{text:"Select a "+this.transType+" Note to delete.",dtext:"Select a "+this.transType+" Note to delete.", etext:"Delete selected "+this.transType+" Note details."},
        iconCls:getButtonIconCls(Wtf.etype.menudelete),
        handler:this.performDelete.createDelegate(this,this.del=["del"])
    });
    if(!config.pendingapproval){//In Pending approval Report we did not need Delete temporary button
        this.deletebtnArray.push(this.deleteBtn);
    }
    this.deleteBtnPerm=new Wtf.Action({
        text: ((this.transType=="Credit")?WtfGlobal.getLocaleText("acc.cnList.deleteNoteP"):WtfGlobal.getLocaleText("acc.dnList.deleteNoteP")),  //'Delete '+this.transType+' Note',
        scope: this,
        disabled :true,
        hidden : config.consolidateFlag || this.reportbtnshwFlag ,
        tooltip:((this.transType=="Credit")?WtfGlobal.getLocaleText("acc.cnList.deleteNoteTT"):WtfGlobal.getLocaleText("acc.dnList.deleteNoteTT")),  //{text:"Select a "+this.transType+" Note to delete.",dtext:"Select a "+this.transType+" Note to delete.", etext:"Delete selected "+this.transType+" Note details."},
        iconCls:getButtonIconCls(Wtf.etype.menudelete),
        handler:this.performDelete.createDelegate(this,this.del=["delp"])
    });
    this.deletebtnArray.push(this.deleteBtnPerm);
    var btnArr=[];
    var bottombtnArr=[];
     this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    btnArr.push(this.quickPanelSearch, this.resetBttn);
        btnArr.push(this.newTabButton=getCreateNewButton(config.consolidateFlag,this,config.isCNReport?WtfGlobal.getLocaleText("acc.WoutI.27"):WtfGlobal.getLocaleText("acc.WoutI.34"),this.reportbtnshwFlag));
    this.newTabButton.on('click',this.openNewTab,this); 
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.editPermType)){
        this.editBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
            tooltip :WtfGlobal.getLocaleText("acc.field.ClicktoEditTransaction"),  //acc.invoiceList.editO:-'Allows you to edit Order.':acc.invoiceList.editQ:-'Allows you to edit Quotation.'
            id: 'btnEdit' + this.id,
            scope: this,
            hidden:this.reportbtnshwFlag, //SDP-12096
            iconCls :getButtonIconCls(Wtf.etype.edit),
            disabled :true
        }); 
        if(!config.pendingapproval){
             btnArr.push(this.editBttn);
        }
        this.editBttn.on('click',this.editNoteHandler,this);
    }
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.copyPermType)){
        this.copyBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.copy"),  //'Edit',
            tooltip :WtfGlobal.getLocaleText("acc.field.CopyRecord"),  
            id: 'btnCopy' + this.id,
            scope: this,
            hidden:this.reportbtnshwFlag || config.pendingapproval, //SDP-12096
            iconCls :getButtonIconCls(Wtf.etype.copy),
            disabled :true
        }); 
        if(!config.pendingapproval){
             btnArr.push(this.copyBttn);
        }
        this.copyBttn.on('click',this.copyNoteHandler,this);
    }
    
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'Delete' Button
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction){
         //  btnArr.push('-',this.deleteBtn,this.deleteBtnPerm);
          //  btnArr.push('-');
            // btnArr.push(this.deleteBtnPerm);
              if(this.deletebtnArray.length>0) {
        btnArr.push(this.deleteMenu = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"), 
            scope: this,
            hidden:this.reportbtnshwFlag, //SDP-12096
            tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"), 
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            menu:this.deletebtnArray
       }));
  }
        }
    }
     /* Import Button Functionality*/
    if (this.moduleid == Wtf.Acc_Credit_Note_ModuleId) {
        this.createImportButtonsForCreditNote(bottombtnArr);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        bottombtnArr.push('-',this.exportButton);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType) || !WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        bottombtnArr.push('-',this.singlePrint=new Wtf.exportButton({
            obj:this,
            id:"printReports"+config.helpmodeid+config.id,
            iconCls: 'pwnd printButtonIcon',
            text:WtfGlobal.getLocaleText("acc.rem.236"),
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Export Record(s)
            disabled :true,
            filename:config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.tabTitle"):WtfGlobal.getLocaleText("acc.dnList.tabTitle"),
            menuItem:{
                rowPdf:true,
                rowPdfPrint:true,
                rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39")+" "+(config.isCNReport?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN"))
                },// + " "+ (config.isCNReport?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN"))},
            get:config.isCustBill?(config.isCNReport?Wtf.autoNum.BillingCreditNote:Wtf.autoNum.BillingDebitNote):(config.isCNReport?Wtf.autoNum.CreditNote:Wtf.autoNum.DebitNote),
            moduleid:config.moduleId
        }));
    }
     if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
         bottombtnArr.push(' ', 
            this.linkinfoViewBtn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.LinkInformationDetails"),     //button for showing link information
                scope: this,
//                hidden: this.isRequisition || this.isRFQ || this.isQuotation||this.isSalesCommissionStmt || (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId),  //shown in SO/PO
                disabled : true,
                tooltip: WtfGlobal.getLocaleText("acc.field.LinkInformationDetails"),
                iconCls:'accountingbase fetch',
                handler:function(){linkinfo(undefined,undefined,undefined,undefined,undefined,undefined,undefined,undefined,this)}
                
            }));
            bottombtnArr.push(' ',
                this.relatedTransactionsBtn = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.field.RelatedTransaction(s)"), //button for showing all related linking with particular document
                    scope: this,
                    disabled: true,
                    tooltip: WtfGlobal.getLocaleText("acc.field.RelatedTransaction(s).tooltip"),
                    handler: function() {
                        linkPurchaseReportTab(this.isCNReport ? 1: 0, this.moduleId, this.grid.getSelectionModel().getSelected().data.noteno);
                    },
                    iconCls: 'accountingbase fetch'
                }));
     }

//    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
//        bottombtnArr.push('-',this.printButton=new Wtf.exportButton({
//            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
//            obj:this,
//            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
//            disabled :true,
//            menuItem:{print:true},
//            filename:config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.tabTitle"):WtfGlobal.getLocaleText("acc.dnList.tabTitle"),
//            params:{name:(config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.tabTitle"):WtfGlobal.getLocaleText("acc.dnList.tabTitle"))},
//            label:(config.isCNReport?WtfGlobal.getLocaleText("acc.trial.credit"):WtfGlobal.getLocaleText("acc.trial.debit"))+WtfGlobal.getLocaleText("acc.field.Note"),
//            get:config.isCustBill?(config.isCNReport?Wtf.autoNum.BillingCreditNote:Wtf.autoNum.BillingDebitNote):(config.isCNReport?Wtf.autoNum.CreditNote:Wtf.autoNum.DebitNote)
//        }));
//        
//         bottombtnArr.push('-',this.singleRowPrint=new Wtf.exportButton({
//            obj:this,
//            id:"printSingleRecord"+config.helpmodeid+config.id,
//            iconCls: 'pwnd printButtonIcon',
//            text:WtfGlobal.getLocaleText("acc.rem.236"),
//            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record details',
//            disabled :true,
//            filename:config.isCNReport?WtfGlobal.getLocaleText("acc.cnList.tabTitle"):WtfGlobal.getLocaleText("acc.dnList.tabTitle"),
//            menuItem:{rowPrint:true},
//            get:config.isCustBill?(config.isCNReport?Wtf.autoNum.BillingCreditNote:Wtf.autoNum.BillingDebitNote):(config.isCNReport?Wtf.autoNum.CreditNote:Wtf.autoNum.DebitNote),
//            moduleid:config.moduleId
//        }));
//    }
    btnArr.push(this.linkTrans=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.LinkTransaction"),
        scope: this,
        disabled :true,
        hidden: (config.consolidateFlag || config.pendingapproval),
        tooltip: WtfGlobal.getLocaleText("acc.field.LinkTransaction"),
        iconCls:'accountingbase pricelistbutton'
    }));
    btnArr.push(this.unlinkTrans=new Wtf.Toolbar.Button({   
        text: WtfGlobal.getLocaleText("acc.field.UnLinkTransaction"),
        scope: this,
        disabled :true,
        hidden: config.pendingapproval,
        tooltip: WtfGlobal.getLocaleText("acc.field.UnLinkTransaction"),
        iconCls:'accountingbase pricelistbutton'
    }));
    
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
        tooltip :config.isCNReport ? WtfGlobal.getLocaleText("acc.field.ApprovePendingCreditNote"): WtfGlobal.getLocaleText("acc.field.ApprovePendingDebitNote"),//Issue 31009 - [Pending Approval]Window name should be "Approve Pending Invoice" instead of "Approve Pending Approval". it should also have deskera logo
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
    
    this.linkTrans.on('click',this.calllinkInvoice,this);
    this.unlinkTrans.on('click',this.callUnlinkInvoice,this);
  
    if(config.pendingapproval){
        btnArr.push(this.approveNoteBttn);
        btnArr.push(this.rejectNoteBttn);
    } else {
        btnArr.push(this.pendingApprovalBttn);
    }
    
    btnArr.push(this.AdvanceSearchBtn);
    
   this.costCenter=CommonERPComponent.createCostCenterPagingComboBox(100,250,30,this);
    this.costCenter.on("select", function(cmb, rec, ind){
        this.costCenterId = rec.data.id;

        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.costCenterId = this.costCenterId;
        this.Store.baseParams=currentBaseParams;

        this.loadCMStore();
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
    
    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
       this.tbar2.push("-",WtfGlobal.getLocaleText("acc.common.costCenter"),this.costCenter);     //"Cost Center"
    }
    this.tbar2.push("-",this.fetchBttn,this.expandCollpseButton, this.approvalHistoryBtn);
    this.typeEditor.on("select", function(cmb, rec, ind){
        if(rec.data.typeid == 10 || rec.data.typeid == 11){
            if(this.startDate)
                this.startDate.setValue(this.getLastFinancialYRStartDate(true));
            if(this.endDate)
                this.endDate.setValue(WtfGlobal.getOpeningDocumentDate(true));

        }else{
            if(this.startDate)
                this.startDate.setValue(WtfGlobal.getDates(true));
            if(this.endDate)
                this.endDate.setValue(WtfGlobal.getDates(false));
        }
        var colIndex=-1;
        if (this.moduleid == Wtf.Acc_Credit_Note_ModuleId) {
            /* set the Header as per filter changed */
            if (rec.data.typeid == 4) {
                colIndex = this.getColumnIndex('salesPerson');
                if (colIndex != -1) {
                    this.grid.getColumnModel().setColumnHeader(colIndex, 'Agent');
                    this.grid.getColumnModel().setDataIndex(colIndex, 'agent');
                }
            } else {
                colIndex = this.getColumnIndex('agent');
                if (colIndex != -1) {
                    this.grid.getColumnModel().setColumnHeader(colIndex, 'Sales Person');
                    this.grid.getColumnModel().setDataIndex(colIndex, 'salesPerson');
                }
            }
        } else {
            if (rec.data.typeid == 4) {
                colIndex = this.getColumnIndex('agent');
                if (colIndex != -1) {
                    this.grid.getColumnModel().setColumnHeader(colIndex, 'Sales Person');
                    this.grid.getColumnModel().setDataIndex(colIndex, 'salesPerson');
                }
            } else {
                colIndex = this.getColumnIndex('salesPerson');
                if (colIndex != -1) {
                    this.grid.getColumnModel().setColumnHeader(colIndex, 'Agent');
                    this.grid.getColumnModel().setDataIndex(colIndex, 'agent');
                }
            }
        }
        var colIndex = -1;
        if (this.moduleid == Wtf.Acc_Credit_Note_ModuleId || this.moduleid == Wtf.Acc_Debit_Note_ModuleId) {
            /*
             * set the Header as per filter changed 
             */
            if (this.moduleid == Wtf.Acc_Debit_Note_ModuleId) {
                if (rec.data.typeid == 4 || rec.data.typeid == 11) {
                    colIndex = this.getColumnIndex('personcode');
                    if (colIndex != -1) {
                        this.grid.getColumnModel().setColumnHeader(colIndex, WtfGlobal.getLocaleText("acc.common.customer.code"));
                        this.grid.getColumnModel().setDataIndex(colIndex, 'personcode');
                    }
                } else {
                    colIndex = this.getColumnIndex('personcode');
                    if (colIndex != -1) {
                        this.grid.getColumnModel().setColumnHeader(colIndex, WtfGlobal.getLocaleText("acc.common.vendor.code"));
                        this.grid.getColumnModel().setDataIndex(colIndex, 'personcode');
                    }
                }
            } else {
                if (rec.data.typeid == 4 || rec.data.typeid == 11) {
                    colIndex = this.getColumnIndex('personcode');
                    if (colIndex != -1) {
                        this.grid.getColumnModel().setColumnHeader(colIndex, WtfGlobal.getLocaleText("acc.common.vendor.code"));
                        this.grid.getColumnModel().setDataIndex(colIndex, 'personcode');
                    }
                } else {
                    colIndex = this.getColumnIndex('personcode');
                    if (colIndex != -1) {
                        this.grid.getColumnModel().setColumnHeader(colIndex, WtfGlobal.getLocaleText("acc.common.customer.code"));
                        this.grid.getColumnModel().setDataIndex(colIndex, 'personcode');
                    }
                }
            }
        }
        if (!this.reportbtnshwFlag){
            this.showHideBarButtons(rec.data.typeid);
        }
        this.cntype = rec.data.typeid;

        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.cntype = this.cntype;
        this.Store.baseParams=currentBaseParams;

        this.loadCMStore();
    },this);
    btnArr.push("-",(config.isCNReport?WtfGlobal.getLocaleText("acc.cn.payType"):WtfGlobal.getLocaleText("acc.dn.payType")));
    btnArr.push(this.typeEditor);     
  
    btnArr.push("->");
    btnArr.push(getHelpButton(this,config.helpmodeid));
    
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
                tbar: btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                    items: bottombtnArr
                })
            }]
    }); 
    
     Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });
    this.expander.on("expand",this.onRowexpand,this);
    Wtf.account.NoteDetailsPanel.superclass.constructor.call(this,config);
     this.addEvents({
        'invoice':true,
        'journalentry':true,
        'goodsreceipt':true
     });
//     if(Wtf.getCmp("custCreditMemo")!=null)
//        Wtf.getCmp("CreditMemo").on('update',this.loadCMStore,this);
    this.getMyConfig();
    this.grid.on('cellclick',this.onCellClick, this);
    this.grid.on('render', function () {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    // * Attachment document in Grid SJ[ERP-16331]
   this.grid.flag = 0;
   this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
   // * Attachment document in Grid SJ[ERP-16331]
}
Wtf.extend(Wtf.account.NoteDetailsPanel,Wtf.Panel,{
    fetchHandler : function(){
         if(this.startDate.getValue()>this.endDate.getValue()){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
             return;
         }
         this.loadCMStore();
     }, 
    enableDisableButtons:function(){        
        Wtf.uncheckSelAllCheckbox(this.sm);
          if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            if(this.deleteBtn){this.deleteBtn.enable();}
            if(this.deleteBtnPerm){
            this.deleteBtnPerm.enable();
         }
        }
        var arr=this.grid.getSelectionModel().getSelections();
        if(arr.length==0&&!WtfGlobal.EnableDisable(this.uPermType,this.removePermType)){
            if(this.deleteBtn){this.deleteBtn.disable();}
            if(this.deleteBtnPerm){
               this.deleteBtnPerm.disable();
         }
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType)){
            for(var i=0;i<arr.length;i++){
                if(arr[i]){
                    if(this.pendingapproval){
                        if(!arr[i].data.deleted){//If record is not temporary deleted(not rejected) and pending approval. For such records delete button will not be enable.
                            if(this.deleteBtnPerm){
                                this.deleteBtnPerm.disable();
                            }
                        }
                    } else{
                        if(arr[i].data.deleted){
                            if(this.deleteBtn){this.deleteBtn.disable();}
                            if(this.deleteBtnPerm){this.deleteBtnPerm.enable();}
                        } 
                    }
                }
            }
        }
        //var arr=this.grid.getSelectionModel().getSelections();
        if(this.singlePrint)this.singlePrint.disable();
        if(this.linkinfoViewBtn)this.linkinfoViewBtn.disable();
        if(this.editBttn)this.editBttn.disable();
        if(this.copyBttn)this.copyBttn.disable();
        if(this.approvalHistoryBtn)this.approvalHistoryBtn.disable();
        //if(this.singleRowPrint)this.singleRowPrint.enable();
        if(this.linkTrans)this.linkTrans.disable();
        if(this.unlinkTrans)this.unlinkTrans.disable();
//        if (this.sm.hasSelection()) {
//            if (this.singleRowPrint)
//                this.singleRowPrint.enable();
//        } else {
//            if (this.singleRowPrint)
//                this.singleRowPrint.disable();
//        }
       // if(this.deleteBtn)this.deleteBtn.enable();
        var rec = this.sm.getSelected();        
        if(this.sm.getCount()>=1 && rec.data.deleted != true){ 
            if(this.singlePrint) {
//                if(!rec.data.otherwise||(rec.data.otherwise && rec.data.isReturnNote)||(rec.data.otherwise && Wtf.templateflag == 1)  || (rec.data.otherwise && rec.data.amountdue<rec.data.amount)) {//rec.data.openflag != true && rec.data.cntype != 4
                    this.singlePrint.enable();
//                }
            }
            if(this.linkinfoViewBtn && rec.data.isOldRecord == false){
                    this.linkinfoViewBtn.enable();
                }

            if(this.singleRowPrint) {
                if(!rec.data.otherwise||(rec.data.otherwise && Wtf.templateflag == 1)  || (rec.data.otherwise && rec.data.amountdue<rec.data.amount)) {//rec.data.openflag != true && rec.data.cntype != 4
                    this.singleRowPrint.enable();
                }
            }
            /* Link button keeping disable
             * 
             *   When DN against Customer or CN against Vendor */
//            if(this.linkTrans && rec.data.openflag == true && rec.data.cntype != 4 && !(rec.data.isOpeningBalanceTransaction && rec.data.cntype==11)) {
//                this.linkTrans.enable();
//             }
            if(rec.data.cntype!=5 && this.linkTrans && (rec.data.amountdue!=undefined && rec.data.amountdue!="" && rec.data.amountdue>0) && rec.data.cntype != 4 && !(rec.data.isOpeningBalanceTransaction && rec.data.cntype==11)) {
                this.linkTrans.enable();
             }
             if(this.unlinkTrans && rec.data.isLinked == true) {
                this.unlinkTrans.enable();
             }
             if(this.editBttn && rec.data.isOldRecord == false){
                 this.editBttn.enable();
             }
             if(this.editBttn && this.sm.getCount()==1 ){
                 this.editBttn.enable();
             }else{
                 this.editBttn.disable();
             }
             if(this.copyBttn && this.sm.getCount()==1 && rec.data.isCopyAllowed && rec.data.partlyJeEntryWithCnDn == 0){
                 this.copyBttn.enable();
             } else {
                 this.copyBttn.disable();
             }
             if(this.approvalHistoryBtn && this.sm.getCount()==1) {
                 this.approvalHistoryBtn.enable();
             } else {
                 this.approvalHistoryBtn.disable();
             }
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
                if(rec.data.cntype!=5 && rec.data.cntype != Wtf.NoteForOvercharge){
                    this.relatedTransactionsBtn.enable();
                }
            }

        } else {
            if (this.linkinfoViewBtn) {
                this.linkinfoViewBtn.disable();
                
            }
            if (this.relatedTransactionsBtn) {
                this.relatedTransactionsBtn.disable();
            }
            /*on multij select disable the linked and unlinked button*/
                this.unlinkTrans.disable();
                this.linkTrans.disable();
                
        }
       
        if(this.pendingapproval){
            if(this.sm.getCount()==1 && rec.data.deleted!=true){//when sinle record slected
                this.rejectNoteBttn.enable();
                this.approveNoteBttn.enable();
            } else {//when no record or more than on record selected
                this.rejectNoteBttn.disable();
                this.approveNoteBttn.disable();
            }
        }
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
//        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
//        if(d<fd)
//            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        var fd = new Date('January 1' + ', ' + 1975 + ' 12:00:00 AM');
        if (start) {
            return fd.add(Date.YEAR, 0);
        }
    },
    

showHideBarButtons:function(typeId){
    if(typeId == 10 || typeId == 11){
//        if(this.AdvanceSearchBtn)
//            this.AdvanceSearchBtn.hide();
        if(this.deleteMenu)
            this.deleteMenu.hide();
        if(this.newTabButton)
            this.newTabButton.hide();
        if(this.deleteBtnPerm)
            this.deleteBtnPerm.hide()
        if(this.singlePrint)
            this.singlePrint.show();     //Export record in Jasper
        if(this.exportButton)
            this.exportButton.show()    //Export to CSV/PDF.
//        if(this.printButton)
//            this.printButton.show()     //Print all records.
        if(this.editBttn)
            this.editBttn.hide();
        if(this.copyBttn)
            this.copyBttn.hide()
//        if(this.unlinkTrans && typeId == 10){
//            this.unlinkTrans.show();
//        } else {
//            this.unlinkTrans.hide();
//        }    
    }else{
        if(this.AdvanceSearchBtn)
            this.AdvanceSearchBtn.show();
        if(this.newTabButton)
            this.newTabButton.show();
        if(this.deleteBtnPerm)
            this.deleteBtnPerm.show()
        if(this.singlePrint)
            this.singlePrint.show()
        if(this.exportButton)
            this.exportButton.show()
//        if(this.printButton)
//            this.printButton.show()
        if(this.deleteMenu)
            this.deleteMenu.show();
//        if(this.unlinkTrans )
//            this.unlinkTrans.hide();
//        if(typeId == 4){
//            if(this.editBttn)
//                this.editBttn.hide()
//        }else{
            if(this.editBttn)
                this.editBttn.show();
        if(this.copyBttn)
             this.copyBttn.show()    
//        }
    }
},

    editNoteHandler:function(){
        var isCN = this.isCNReport;
        
        var isEdit= true;
        
        var arr=this.grid.getSelectionModel().getSelections();
        if(arr.length !=1 ){
            return;
        }
        
        var record = arr[0];
        var noteType = record.get('cntype');
        //CnType =5 is for Credit note Against Vendor for Gst
        if (noteType == 5) {
            callEditCreditNoteGst(true, record, true, false, isCN);
        } else if (noteType == Wtf.NoteForOvercharge) {
            var winid = isCN ? "creditnoteForOverchargeEdit" + record.get("noteno") : "debitnoteForOverchargeEdit" + record.get("noteno");
            callEditNoteForOvercharge(winid,record,true,true,isCN);//CN/DN for Overcharge
        } else {
            var isNoteLinkedWithPayment = false;
            var isNoteLinkedToAdvancePayment = record.data['isNoteLinkedToAdvancePayment'];
            var isLinkedTransaction = record.data['isLinkedTransaction'];
            var isLinkedInvoiceIsClaimed = record.data['isLinkedInvoiceIsClaimed'];
            if(isNoteLinkedToAdvancePayment){
                if(isCN){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.cn.linkedSoCanNotbeEdited")],2);
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.dn.linkedSoCanNotbeEdited")],2);
                }    
                return;
            }
            /*
             * Applicable for Malaysian Country
             */
            if(isLinkedInvoiceIsClaimed){
                if(isCN){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.blockCNEdit")],2);
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.blockDNEdit")],2);
                }    
                return;
            }
            /*
             * Remove Below code as to allow to Edit Note 
             */
    //        var isLinked = record.data['isLinked'];
    //        if(isLinked != undefined && isLinked ==true){
    //            if(isCN){
    //                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.cn.linkedtransactionSoCanNotbeEdited")],2);
    //            }else{
    //                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.dn.linkedintransactionSoCanNotbeEdited")],2);
    //            }    
    //            return;
    //        }
           var isCreatedFromReturnForm=record.get('isCreatedFromReturnForm')!=undefined&&record.get('isCreatedFromReturnForm')!=null?record.get('isCreatedFromReturnForm'):false;
            if (isLinkedTransaction) {
                if (noteType != Wtf.CNDN_TYPE_FOR_MALAYSIA && !isCreatedFromReturnForm) {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invList.linkedInvoice"), function (btn) {
                        if (btn == "yes") {
                            var isreval=record.get('isreval');
                            if(isreval!=undefined && isreval > 0){
                                WtfComMsgBox(58,2);
                                return; 
                            }

                            // Check if record is created from Party JE

                            if(record.get('partlyJeEntryWithCnDn') !=undefined && record.get('partlyJeEntryWithCnDn') !="" && record.get('partlyJeEntryWithCnDn') == 1){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.common.partyJournalCNDNEditAlert")],3);
                                return;
                            }                                                                                                                                                                                                                                                                                                                                                                               

                            // check whether note is linked with payment or not

                            Wtf.Ajax.requestEx({
                                    url: (isCN)?"ACCVendorPaymentCMN/getPaymentsLinkedWithNCreditNote.do":"ACCReceiptCMN/getPaymentsLinkedWithDebitNote.do",
                                    params:{
                                        noteId:record.get('noteid')
                                    }
                                },this,function(response, request){
                                    isNoteLinkedWithPayment = response.isNoteLinkedWithPayment;
                                    var notetabid="EditDebitNote"+ record.get("noteno")
                                    if(isCN){
                                        notetabid="EditCreditNote"+ record.get("noteno")  
                                    }
                                    if (noteType == Wtf.CNDN_TYPE_FOR_MALAYSIA) {
                                        callEditCreditNoteGst(true, record, true, false, isCN);
                                    } else {
                                        createNote(notetabid, isEdit, isCN, noteType, record, this.grid.id, false, isLinkedTransaction, undefined, isCreatedFromReturnForm);
                                    }
                                },function(response, request){

                            });
                        } else {
                            return;
                        }
                    }, this);
                } else if (noteType != Wtf.CNDN_TYPE_FOR_MALAYSIA && isCreatedFromReturnForm) {// if created from Purchase/Sales Return form
                            if (isCN)
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.note.sales.return.created")], 3);
                            else
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.note.purchase.return.created")], 3);

                    return;
                } else {
                    isCN ? WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.malayasian.editcreditnote")], 2) : WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.malayasian.editdebitnote")], 2);
                    return;
                }
            }else{

                var isreval=record.get('isreval');
                if(isreval!=undefined && isreval > 0){
                    WtfComMsgBox(58,2);
                    return; 
                }
                var isCreatedFromReturnForm=record.get('isCreatedFromReturnForm');
    //            if(record.get('isCreatedFromReturnForm')){// if created from Purchase/Sales Return form
    //                if(isCN)
    //                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.note.sales.return.created")],3);
    //                else
    //                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.note.purchase.return.created")],3);
    //
    //                return;
    //            }

                // Check if record is created from Party JE

                if(record.get('partlyJeEntryWithCnDn') !=undefined && record.get('partlyJeEntryWithCnDn') !="" && record.get('partlyJeEntryWithCnDn') == 1){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.common.partyJournalCNDNEditAlert")],3);
                    return;
                }

                // check whether note is linked with payment or not

                Wtf.Ajax.requestEx({
                        url: (isCN)?"ACCVendorPaymentCMN/getPaymentsLinkedWithNCreditNote.do":"ACCReceiptCMN/getPaymentsLinkedWithDebitNote.do",
                        params:{
                            noteId:record.get('noteid')
                        }
                    },this,function(response, request){
                            isNoteLinkedWithPayment = response.isNoteLinkedWithPayment;
                            var notetabid="EditDebitNote"+ record.get("noteno")
                            if(isCN){
                                notetabid="EditCreditNote"+ record.get("noteno")  
                            }
                            createNote(notetabid,isEdit,isCN,noteType,record,this.grid.id,false,isLinkedTransaction,undefined,isCreatedFromReturnForm);
                    },function(response, request){

                });
            }
        }
    },

    copyNoteHandler:function(){
            var isCN = this.isCNReport;
            var isEdit= true;
            var isCopy=true;
            var arr=this.grid.getSelectionModel().getSelections();
            if(arr.length !=1 ){
                return;
            } 

            var record = arr[0];
            var noteType = record.get('cntype');
            
            var notetabid="CopyDebitNote"+ record.get("noteno")
            if(isCN){
                notetabid="CopyCreditNote"+ record.get("noteno")  
            }
            createNote(notetabid,isEdit,isCN,noteType,record,this.grid.id,isCopy);
        },
    
   handleResetClick:function(){
       if(this.quickPanelSearch.getValue()){
           this.quickPanelSearch.reset();
              this.Store.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
       }
    },
    setCostCenter: function(){
        this.costCenter.setValue(this.costCenterId);//Select Default Cost Center
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    storeloaded:function(store){
      if(this.exportButton){
        this.exportButton.params.cntype= this.Store && this.Store.baseParams ? this.Store.baseParams.cntype : "";
        this.exportButton.params.enddate =  WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.exportButton.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.exportButton.params.mode=this.Store && this.Store.baseParams ? this.Store.baseParams.mode : "";
        this.exportButton.params.consolidateFlag=this.Store && this.Store.baseParams ? this.Store.baseParams.consolidateFlag : "";
        this.exportButton.params.companyids=this.Store && this.Store.baseParams ? this.Store.baseParams.companyids : "";
        this.exportButton.params.gcurrencyid=this.Store && this.Store.baseParams ? this.Store.baseParams.gcurrencyid : "";
        this.exportButton.params.userid=this.Store && this.Store.baseParams ? this.Store.baseParams.userid : "";
        this.exportButton.params.isprinted=this.Store && this.Store.baseParams ? this.Store.baseParams.isprinted : "";
        this.exportButton.params.deleted=this.Store && this.Store.baseParams ? this.Store.baseParams.deleted : "";
        this.exportButton.params.nondeleted=this.Store && this.Store.baseParams ? this.Store.baseParams.nondeleted : "";
        this.exportButton.params.costCenterId=this.Store && this.Store.baseParams ? this.Store.baseParams.costCenterId : "";
        this.exportButton.params.isCreditNote=this.isCNReport;
        this.exportButton.params.ss = this.quickPanelSearch.getValue();
     }
        
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
//            if(this.printButton)this.printButton.disable();
            //if(this.deleteBtn)this.deleteBtn.disable();
            //if(this.deleteBtnPerm)this.deleteBtnPerm.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
//            if(this.printButton)this.printButton.enable();
            //if(this.deleteBtn)this.deleteBtn.enable();
            //if(this.deleteBtnPerm)this.deleteBtnPerm.enable();
        }
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    onRowexpand:function(scope, record, body, rowIndex){
        var colModelArray = [];
        colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
        colModelArray = [];
        colModelArray = GlobalColumnModelForProduct[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
        this.expanderBody=body;
        if(this.isCNReport){
            if(record.data.withoutinventory){                
                this.expandStore.proxy.conn.url="ACCCreditNote/getBillingCreditNoteRows.do";                                
            }                
            else{
                this.expandStore.proxy.conn.url="ACCCreditNoteCMN/getCreditNoteRows.do";
            }                
        }else{
            if(record.data.withoutinventory){                
                this.expandStore.proxy.conn.url="ACCDebitNote/getBillingDebitNoteRows.do";                
            }                
            else{
                this.expandStore.proxy.conn.url="ACCDebitNote/getDebitNoteRows.do";
            }                
        }
        this.expandStore.load({params:{bills:record.data["noteid"],isForReport:true}});
    },
//    fillExpanderBody:function(){
//    if(this.expandStore.getCount()>0){            
//        var rec=this.expandStore.getAt(0);
//        var disHtml = "";
//        var disHtmlPaidInv = "";
//        var arr=[];
//        var arrPaidInv=[];
//        var custArr = [];
//        custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[this.moduleid]);
//        var creditJobTextPaidInv = WtfGlobal.getLocaleText("acc.cnList.prod") ;
//        if(rec.data.withoutinventory){
//            creditJobTextPaidInv = WtfGlobal.getLocaleText("acc.cnList.prodNonInv")+' '+WtfGlobal.getLocaleText("acc.cnList.Desc");
//        }
//        arrPaidInv=[creditJobTextPaidInv,
//        (rec.data.withoutinventory)?"":WtfGlobal.getLocaleText("acc.cnList.Desc"),WtfGlobal.getLocaleText("acc.cnList.TransNo"),
//        WtfGlobal.getLocaleText("acc.cnList.qty"),
//        WtfGlobal.getLocaleText("acc.cnList.gridAmt"),
//        WtfGlobal.getLocaleText("acc.cnList.gridMemo"),
//        WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
//        "                "];
//        var gridHeaderTextPaidInv = (rec.data.withoutinventory)?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInvPaidInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pListPaidInv");
//        var headerPaidInv = "<span class='gridHeader'>"+gridHeaderTextPaidInv+"</span>"; //Product List
//        headerPaidInv += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";    //S.No.
//        for(var j=0;j<arrPaidInv.length;j++){
//            headerPaidInv += "<span class='headerRow'>" + arrPaidInv[j] + "</span>";
//        }
//        headerPaidInv += "<span class='gridLine'></span>"; 
//
//        arr=[WtfGlobal.getLocaleText("acc.prList.invNo"),
//        WtfGlobal.getLocaleText("acc.prList.creDate"),
//        WtfGlobal.getLocaleText("acc.prList.dueDate"),
//        WtfGlobal.getLocaleText("acc.prList.invoicelinkingDate"),//Linking invoice date in report CN,DN
//        WtfGlobal.getLocaleText("acc.prList.invAmt"),
//        WtfGlobal.getLocaleText("acc.prList.amtDue")];
//        var gridHeaderText = WtfGlobal.getLocaleText("acc.field.InvoiceDetails");//(rec.data.withoutinventory)?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
//        var header = "<span class='gridHeader'>"+gridHeaderText+"</span>"; //Product List
//        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";    //S.No.
//        for(var j=0;j<arr.length;j++){
//            header += "<span class='headerRow'>" + arr[j] + "</span>";
//        }                       
//        header += "<span class='gridLine'></span>";    
//            
//        //code for account details
//        var type="";
//        if (Wtf.account.companyAccountPref.manyCreditDebit)
//        {
//          type= WtfGlobal.getLocaleText("acc.CNDNList.expand.Type");
//        }
//        var AccArr=[
//        WtfGlobal.getLocaleText("acc.je.acc"),
//        type,
//        WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"),
//        WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),
//        WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
//        WtfGlobal.getLocaleText("acc.cnList.Desc"),
//        ];
//        var arrayLength=AccArr.length;
//        for(var custArrcount=0;custArrcount<custArr.length;custArrcount++){
//            if(custArr[custArrcount].header != undefined )
//                AccArr[arrayLength+custArrcount]=custArr[custArrcount].header;
//        }
//        var count=0;
//        for(var custArrcount=0;custArrcount<AccArr.length;custArrcount++){
//            if(AccArr[custArrcount] != ""){
//                count++;
//            }
//        }
//        var widthInPercent=80/count;
//        var minWidth = count*100 + 40;
//        var AccGridHeaderText =   WtfGlobal.getLocaleText("acc.field.accountDetails");
//        var AccHeader = "<span class='gridHeader'>"+AccGridHeaderText+"</span>";  
//        AccHeader += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";    //S.No.
//        for(var j=0;j<AccArr.length;j++){
//            AccHeader += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + AccArr[j] + "</span>";
//        } 
//        AccHeader += "<span class='gridLine'></span>";  
//        var AccCount=1; 
//        var disHtmlAccHeader='';
//       
//        var cntype = '2';
//        var paidInvCnt = 1;
//        var invCnt = 1;
//        for(i=0;i<this.expandStore.getCount();i++){
//            rec=this.expandStore.getAt(i); 
//            var accountDetailsRec=rec.json;   
//            cntype = rec.data['cntype'];
//            if(rec.data['paidinvflag'] == '1') {
//                headerPaidInv += "<span class='gridNo'>"+(paidInvCnt)+".</span>";
//                headerPaidInv += "<span class='gridRow'  wtf:qtip='"+rec.data['productname']+"'>"+Wtf.util.Format.ellipsis(rec.data['productname'],20)+"</span>";
//                if(!(rec.data.withoutinventory))
//                    headerPaidInv += "<span class='gridRow' wtf:qtip='"+rec.data['productname']+"' >"+Wtf.util.Format.ellipsis(rec.data['desc'],20)+"&nbsp;</span>";
//                headerPaidInv += "<span class='gridRow ' >"+"<a  class='jumplink' href='#' onClick='javascript:invLink(\""+rec.data['transectionid']+"\",\""+this.isCNReport+"\",\""+this.isCustBill+"\")'>"+rec.data['transectionno']+"</a>"+"</span>";
//                headerPaidInv += "<span class='gridRow'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
//                headerPaidInv += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['discount'],rec.data['currencysymbol'],[true])+"</span>";
//                if(rec.data['memo']==''){
//                    headerPaidInv += "<span class='gridRow'>&nbsp;</span>";
//                }
//                else{
//                    headerPaidInv += "<span class='gridRow' style='width:30%'  wtf:qtip='"+rec.data['memo']+"'>"+unescape(Wtf.util.Format.ellipsis(rec.data['memo'],80))+"</span>";
//                }
//                headerPaidInv += "<span class='gridRow' style='width:30%'  wtf:qtip='"+rec.data['remark']+"'>"+unescape(Wtf.util.Format.ellipsis(rec.data['remark'],80))+"</span>";
//                headerPaidInv +="<br>";
//                paidInvCnt++;
//            }else if(rec.data['isaccountdetails'] == true) {
//                AccHeader += "<span class='gridNo' >"+(AccCount)+".</span>";
//                AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'  wtf:qtip='"+accountDetailsRec['accountname']+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec['accountname'],20)+"</span>";
//                if (Wtf.account.companyAccountPref.manyCreditDebit){
//                    AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;' wtf:qtip='"+accountDetailsRec['debit']+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec['debit'],20)+"</span>";
//                }
//                AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;' wtf:qtip='"+accountDetailsRec['taxpercent']+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec['taxpercent'],20)+"</span>";
//                AccHeader += "<span class='gridRow'style='width:"+widthInPercent+"% ! important;' >"+WtfGlobal.addCurrencySymbolOnly(accountDetailsRec['taxamount'],accountDetailsRec['currencysymbol'],[true])+"</span>";
//                AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;' >"+WtfGlobal.addCurrencySymbolOnly(accountDetailsRec['totalamount'],accountDetailsRec['currencysymbol'],[true])+"</span>";
//                if(accountDetailsRec['description']!=null && accountDetailsRec['description']!=""){
//                    AccHeader += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec['description'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec['description'], 20) + "</span>";
//                }else{
//                    AccHeader += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec['description'] + "'>" + Wtf.util.Format.ellipsis("&nbsp", 20) + "</span>";
//                }
////                AccHeader += "<span class='gridRow'style='width:"+widthInPercent+"% ! important;'  wtf:qtip='"+accountDetailsRec['description']+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec['description'],20)+"</span>";
//                for(var j=0;j<custArr.length;j++){
//                    if(accountDetailsRec[custArr[j].dataIndex]!=undefined && accountDetailsRec[custArr[j].dataIndex]!="null" && accountDetailsRec[custArr[j].dataIndex]!="")
//                        AccHeader += "<span class='gridRow'style='width:"+widthInPercent+"% ! important;'  wtf:qtip='"+accountDetailsRec[custArr[j].dataIndex]+"'>"+Wtf.util.Format.ellipsis(accountDetailsRec[custArr[j].dataIndex],15)+"</span>";
//                    else
//                        AccHeader += "<span class='gridRow' style='width:"+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
//                }
//                AccHeader +="<br>";
//                AccCount++;
//            } else {
//                var isCustomer=(this.businessPerson=="Customer")?true:false
//                header += "<span class='gridNo'>"+(invCnt)+".</span>";
//                header += "<span class='gridRow ' >"+"<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\""+rec.data['transectionid']+"\",\""+isCustomer+"\",\""+this.isCustBill+"\")'>"+rec.data['transectionno']+"</a>"+"</span>";
//                header += "<span class='gridRow'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['invcreationdate'])+"</span>";
//                header += "<span class='gridRow'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['invduedate'])+"</span>";
//                header += "<span class='gridRow'>"+WtfGlobal.onlyDateLeftRenderer(rec.data['grlinkdate'])+"</span>";//Linking invoice date in report CN ,DN
//                header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['invamount'],rec.data['currencysymbol'],[true])+"</span>";
//                header += "<span class='gridRow'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['invamountdue'],rec.data['currencysymbol'],[true])+"</span>";
//
//                header +="<br>";    
//                invCnt++;
//            }
//        }
//        disHtmlAccHeader += "<div class='expanderContainer' style='width:100%'>" + AccHeader + "</div>";
//        disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
//        disHtmlPaidInv += "<br><div class='expanderContainer' style='width:100%'>" + headerPaidInv + "</div>";
//        if(cntype == '3') {//CN against Paid Invoice
//            if(invCnt == 1 && AccCount > 1){
//                this.expanderBody.innerHTML = disHtmlAccHeader+disHtmlPaidInv;
//            }else if(invCnt >  1 && AccCount == 1){
//                this.expanderBody.innerHTML = disHtml+disHtmlPaidInv;
//            }else{
//                this.expanderBody.innerHTML = disHtmlPaidInv;
//            //                this.expanderBody.innerHTML = disHtml + disHtmlPaidInv + disHtmlAccHeader;
//            }
//        } else if(invCnt == 1 && AccCount > 1){
//            this.expanderBody.innerHTML = disHtmlAccHeader;
//        }else if(invCnt >  1 && AccCount == 1){
//            this.expanderBody.innerHTML = disHtml;
//        }else {
//            this.expanderBody.innerHTML = disHtml+disHtmlAccHeader;
//        }           
//    } else {
//        this.expanderBody.innerHTML = "<br><b><div class='expanderContainer' style='width:100%'>"+WtfGlobal.getLocaleText("acc.prList.gridAmtReceived")+"</div></b>"      //This transaction is not linked with any invoice.
//    }
//},

fillExpanderBody:function(){
    var disHtml = "";
    this.custArr = [];
    this.custArr = WtfGlobal.appendCustomColumn(this.custArr, GlobalColumnModel[this.moduleid]);
    var header = "";            //ProductHeader[0]: HTML text,  ProductHeader[1]: minWidth,  ProductHeader[2]:widthInPercent
    var pheader="";
    var AccHeaderArray = "";
    var headerArr = "";
    var AccHeader = "";
    var cnDnHeader = "";

    var cntype = '2';
    var widthInPercent = 0;
    var widthInPercentHeader = 0;

    var prevBillid = "";
    var sameParent = false;
    
    var rec="";
    for (var i = 0; i < this.expandStore.getCount(); i++) {
        var ptransaction=rec!=""?rec.data.transectionno:"";
        rec = this.expandStore.getAt(i);
        var headerPaidInv = this.getHeaderPaidInv(rec);
        pheader=header;
        if (Wtf.isEmpty(header)) {
            headerArr = this.getHeader();
            header = headerArr[0];
            widthInPercentHeader = headerArr[1];
        }
//        if (Wtf.isEmpty(AccHeaderArray)) {
            AccHeaderArray = this.getAccHeader(rec);
            AccHeader = AccHeaderArray[0];
            widthInPercent = AccHeaderArray[1];
//        }
        if(rec.data['isCnDndetails']!=undefined && rec.data['isCnDndetails']!=""){
            cnDnHeader= this.getCnDnNoteHeader(rec);
        }
            
           
        var currentBillid = rec.data['billid'];
        if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
            prevBillid = currentBillid;
            sameParent = false;
            var headerPaidInvData = "";
            var AccHeaderData = "";
            var headerData = "";
            var cnDnheaderData = "";
            this.AccCount = 1;
            this.paidInvCnt = 1;
            this.invCnt = 1;
            this.debitDnCnt=1;
        } else {
            sameParent = true;
        }

        var accountDetailsRec = rec.json;
        cntype = rec.data['cntype'];
//        if (cntype == Wtf.CNDN_TYPE_FOR_MALAYSIA || cntype == Wtf.NoteForOvercharge) {
//            if (ptransaction == rec.data.transectionno) {
//                continue;
//            }
//        }
        if (rec.data['paidinvflag'] == '1') {
            headerPaidInvData = this.getHeaderPaidInvData(rec, sameParent);
        } else if (rec.data['isaccountdetails'] == true) {
            AccHeaderData = this.getAccHeaderData(accountDetailsRec, sameParent, widthInPercent);
        } else if(rec.data['isCnDndetails']!=undefined && rec.data['isCnDndetails']!="") {
            /*
             * get headers While linking CN in DN or DN in CN
             */
            cnDnheaderData = this.getCnDnNoteData(rec, sameParent);
        }else{
            headerData = this.getHeaderData(rec, sameParent,widthInPercentHeader);
        }

            /* Expander for opening transaction*/
            if (rec.data.isOpeningDnCn) {
                this.expanderBody.innerHTML = "<br><b><div class='expanderContainer' style='width:100%'>" + WtfGlobal.getLocaleText("acc.field.Nodatatodisplay") + "</div></b>"      //No data to display. 
                break;
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

            var disHtmlAccHeader = "<div class='expanderContainer' style='width:100%'>" + AccHeader + AccHeaderData + "</div>";
            var disHtml = "<div class='expanderContainer' style='width:100%'>" + header + headerData + "</div>";
            var disDebitNoteHtml = "<div class='expanderContainer' style='width:100%'>" + cnDnHeader + cnDnheaderData + "</div>";
            var disHtmlPaidInv = "<br><div class='expanderContainer' style='width:100%'>" + headerPaidInv + headerPaidInvData + "</div>";
            if (cntype == '3') {//CN against Paid Invoice
                if (this.invCnt == 1 && this.AccCount > 1) {
                    body.innerHTML = disHtmlAccHeader + disHtmlPaidInv;
                } else if (this.invCnt > 1 && this.AccCount == 1) {
                    body.innerHTML = disHtml + disHtmlPaidInv;
                } else {
                    body.innerHTML = disHtmlPaidInv;
                //                this.expanderBody.innerHTML = disHtml + disHtmlPaidInv + disHtmlAccHeader;
                }
            } else if (this.invCnt == 1 && this.AccCount > 1&&this.debitDnCnt== 1) {
                /*
                 * When only accounts are map to debit note or credit note
                 */
                body.innerHTML = disHtmlAccHeader;
            } else if (this.invCnt > 1 && this.AccCount == 1&&this.debitDnCnt== 1) {
                /*
                 * When only invoices are map to debit note or credit note
                 */
                body.innerHTML = disHtml;
            }else if (this.invCnt == 1 && this.AccCount == 1&&this.debitDnCnt> 1) {
                /*
                 * When only CnDn are map to debit note or credit note
                 */
                body.innerHTML = disDebitNoteHtml;
            }
            else if (this.debitDnCnt == 1 && this.AccCount > 1 &&this.invCnt > 1) {
                /*
                 * When only invoices and accounts are map to debit note or credit note
                 */
                body.innerHTML = disHtmlAccHeader+disHtml;
            } else if (this.debitDnCnt > 1 && this.AccCount > 1 &&this.invCnt ==1) {
                /*
                 * When only CnDn and accounts are map to debit note or credit note
                 */
                body.innerHTML = disHtmlAccHeader+disDebitNoteHtml;
            } else if (this.debitDnCnt > 1 && this.AccCount ==1 &&this.invCnt >1) {
                /*
                 * When only invoices and CnDn are map to debit note or credit note
                 */
                body.innerHTML = disHtml+disDebitNoteHtml
            }else if (this.AccCount > 1 && this.invCnt > 1&&this.debitDnCnt > 1) {
                /*
                 * When only invoices,accounts and CnDn are map to debit note or credit note
                 */
                body.innerHTML = disHtml + disHtmlAccHeader+disDebitNoteHtml;
            }
            //                else {
            //                    body.innerHTML = disHtml + disHtmlAccHeader;
            //                }
                
            if (this.expandButtonClicked) {
                this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
            }
        }
    }
},

getHeaderPaidInv:function(rec){
        var arrPaidInv = [];
        var creditJobTextPaidInv = WtfGlobal.getLocaleText("acc.cnList.prod");
        if (rec.data.withoutinventory) {
            creditJobTextPaidInv = WtfGlobal.getLocaleText("acc.cnList.prodNonInv") + ' ' + WtfGlobal.getLocaleText("acc.cnList.Desc");
        }
        arrPaidInv = [creditJobTextPaidInv,
            (rec.data.withoutinventory) ? "" : WtfGlobal.getLocaleText("acc.cnList.Desc"), WtfGlobal.getLocaleText("acc.cnList.TransNo"),
            WtfGlobal.getLocaleText("acc.cnList.qty"),
            WtfGlobal.getLocaleText("acc.cnList.gridAmt"),
            WtfGlobal.getLocaleText("acc.cnList.gridMemo"),
            WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
            "                "];
        var gridHeaderTextPaidInv = (rec.data.withoutinventory) ? WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInvPaidInv") : WtfGlobal.getLocaleText("acc.invoiceList.expand.pListPaidInv");
        var headerPaidInv = "<span class='gridHeader'>" + gridHeaderTextPaidInv + "</span>"; //Product List
        headerPaidInv += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";    //S.No.
        for (var j = 0; j < arrPaidInv.length; j++) {
            headerPaidInv += "<span class='headerRow'>" + arrPaidInv[j] + "</span>";
        }
        headerPaidInv += "<span class='gridLine'></span>";
        return headerPaidInv;
},

getHeader:function(){
        var arr = [];
        var headerPaidInvArray = [];
        arr = [WtfGlobal.getLocaleText("acc.prList.invNo"),
            WtfGlobal.getLocaleText("acc.prList.creDate"),
            WtfGlobal.getLocaleText("acc.prList.dueDate"),
            WtfGlobal.getLocaleText("acc.common.linkingDate"),
            WtfGlobal.getLocaleText("acc.prList.invAmt"),
            WtfGlobal.getLocaleText("acc.prList.amtDue")];
        var gridHeaderText = WtfGlobal.getLocaleText("acc.field.InvoiceDetails");//(rec.data.withoutinventory)?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
        var header = "<span class='gridHeader'>" + gridHeaderText + "</span>"; //Product List
        header += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";    //S.No.
        for (var j = 0; j < arr.length; j++) {
            header += "<span class='headerRow'>" + arr[j] + "</span>";
        }
        
        var arrayLength = arr.length;
        for (var custArrcount = 0; custArrcount < this.custArr.length; custArrcount++) {
            if (this.custArr[custArrcount].header != undefined)
                arr[arrayLength + custArrcount] = this.custArr[custArrcount].header;
        }
        var count = 0;
        for (var custArrcount = 0; custArrcount < arr.length; custArrcount++) {
            if (arr[custArrcount] != "") {
                count++;
            }
        }
        var widthInPercent =80 / count;
        for (var j = 0; j < this.custArr.length; j++) {
            header += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + this.custArr[j].header + "</span>";
        }
        header += "<span class='gridLine'></span>";

        headerPaidInvArray.push(header);
        headerPaidInvArray.push(widthInPercent);
        return headerPaidInvArray;
        
//        header += "<span class='gridLine'></span>";
//        return header;
},
getCnDnNoteHeader:function(rec){
    var arr = [];
    var isCreditNote=false;
    if(rec.data['isCreditNote']!=undefined){
        isCreditNote=rec.data['isCreditNote'];    
    }
    arr = [
    isCreditNote?WtfGlobal.getLocaleText("acc.cnList.gridNoteNo"):WtfGlobal.getLocaleText("acc.dnList.gridNoteNo"),
    WtfGlobal.getLocaleText("acc.prList.creDate"),
    WtfGlobal.getLocaleText("acc.invoice.gridAmount"),
    WtfGlobal.getLocaleText("acc.prList.amtDue")];
    var gridHeaderText = isCreditNote?WtfGlobal.getLocaleText("acc.CreditNote.cnDetails"):WtfGlobal.getLocaleText("acc.DebitNote.dnDetails");//(rec.data.withoutinventory)?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
    var header = "<span class='gridHeader'>" + gridHeaderText + "</span>"; //Product List
    header += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";    //S.No.
    for (var j = 0; j < arr.length; j++) {
        header += "<span class='headerRow'>" + arr[j] + "</span>";
    }
    header += "<span class='gridLine'></span>";
    return header;
},
getCnDnNoteData:function(rec,sameParent){
    if (!sameParent || this.debitDnCnt == 1) {
        this.cnDnHeader = "";
        this.debitDnCnt = 1;
    }

    var isCustomer = (this.businessPerson == "Customer") ? true : false
    var isOpeningDnCn = false;
    if(rec.data.isOpeningDnCn!=undefined){
        isOpeningDnCn = rec.data.isOpeningDnCn;
    }
    var transactionNoLink = isOpeningDnCn? "<a  class='jumplink' href='#' onClick='javascript:alertForOpeningTransactions()'>"+rec.data.transectionno+"</a>":"<a  class='jumplink' href='#' onClick='javascript:noteRecLinkNew(\"" + rec.data['transectionid'] + "\",\"" + isCustomer + "\",\"" + false + "\")'>" + rec.data['transectionno'] + "</a>";
    this.cnDnHeader += "<span class='gridNo'>" + (this.debitDnCnt) + ".</span>";
    this.cnDnHeader += "<span class='gridRow ' >" + transactionNoLink + "</span>";
    this.cnDnHeader += "<span class='gridRow'>" + WtfGlobal.onlyDateLeftRenderer(rec.data['invcreationdate']) + "</span>";
    this.cnDnHeader += "<span class='gridRow'>" + WtfGlobal.onlyDateLeftRenderer(rec.data['invduedate']) + "</span>";
    this.cnDnHeader += "<span class='gridRow'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['invamount'], rec.data['currencysymbol'], [true]) + "</span>";
    this.cnDnHeader += "<span class='gridRow'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['invamountdue'], rec.data['currencysymbol'], [true]) + "</span>";
    
    this.cnDnHeader += "<br>";
    this.debitDnCnt++;
    return this.cnDnHeader;
},
getAccHeader:function(rec){
        //code for account details
        var AccHeaderArray = [];
        var type = "";
        this.israteincludegst =  rec.data.isIncludingGst;
        
            type = WtfGlobal.getLocaleText("acc.CNDNList.expand.Type");
        
        var AccArr = [
            WtfGlobal.getLocaleText("acc.je.acc"),
            type,
            WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"),
            WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),
            WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
            this.israteincludegst?WtfGlobal.getLocaleText("acc.invoice.gridamountIncludingGST"):'',
            WtfGlobal.getLocaleText("acc.cnList.Desc"),
        ];
        var arrayLength = AccArr.length;
        for (var custArrcount = 0; custArrcount < this.custArr.length; custArrcount++) {
            if (this.custArr[custArrcount].header != undefined)
                AccArr[arrayLength + custArrcount] = this.custArr[custArrcount].header;
        }
        var count = 0;
        for (var custArrcount = 0; custArrcount < AccArr.length; custArrcount++) {
            if (AccArr[custArrcount] != "") {
                count++;
            }
        }
        var widthInPercent = 80 / count;
        var minWidth = count * 100 + 40;
        var AccGridHeaderText = WtfGlobal.getLocaleText("acc.field.accountDetails");
        var AccHeader = "<span class='gridHeader'>" + AccGridHeaderText + "</span>";
        AccHeader += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";    //S.No.
        for (var j = 0; j < AccArr.length; j++) {
            AccHeader += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + AccArr[j] + "</span>";
        }
        AccHeader += "<span class='gridLine'></span>";

        AccHeaderArray.push(AccHeader);
        AccHeaderArray.push(widthInPercent);
        return AccHeaderArray;
},

getHeaderPaidInvData:function(rec,sameParent){
        if (!sameParent || this.paidInvCnt==1) {
            this.headerPaidInv = "";
            this.paidInvCnt = 1;
        }

        this.headerPaidInv += "<span class='gridNo'>" + (this.paidInvCnt) + ".</span>";
        this.headerPaidInv += "<span class='gridRow'  wtf:qtip='" + rec.data['productname'] + "'>" + Wtf.util.Format.ellipsis(rec.data['productname'], 20) + "</span>";
        if (!(rec.data.withoutinventory))
            this.headerPaidInv += "<span class='gridRow' wtf:qtip='" + rec.data['productname'] + "' >" + Wtf.util.Format.ellipsis(rec.data['desc'], 20) + "&nbsp;</span>";
        this.headerPaidInv += "<span class='gridRow ' >" + "<a  class='jumplink' href='#' onClick='javascript:invLink(\"" + rec.data['transectionid'] + "\",\"" + this.isCNReport + "\",\"" + this.isCustBill + "\")'>" + rec.data['transectionno'] + "</a>" + "</span>";
        this.headerPaidInv += "<span class='gridRow'>" + rec.data['quantity'] + " " + rec.data['unitname'] + "</span>";
        this.headerPaidInv += "<span class='gridRow'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['discount'], rec.data['currencysymbol'], [true]) + "</span>";
        if (rec.data['memo'] == '') {
            this.headerPaidInv += "<span class='gridRow'>&nbsp;</span>";
        }
        else {
            this.headerPaidInv += "<span class='gridRow' style='width:30%'  wtf:qtip='" + rec.data['memo'] + "'>" + unescape(Wtf.util.Format.ellipsis(rec.data['memo'], 80)) + "</span>";
        }
        this.headerPaidInv += "<span class='gridRow' style='width:30%'  wtf:qtip='" + rec.data['remark'] + "'>" + unescape(Wtf.util.Format.ellipsis(rec.data['remark'], 80)) + "</span>";
        this.headerPaidInv += "<br>";
        this.paidInvCnt++;
        return this.headerPaidInv;
},
getAccHeaderData:function(accountDetailsRec,sameParent,widthInPercent){
        if (!sameParent || this.AccCount == 1) {
            this.AccHeader = "";
            this.AccCount = 1;
        }

        this.AccHeader += "<span class='gridNo' >" + (this.AccCount) + ".</span>";
        this.AccHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec['accountname'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec['accountname'], 20) + "</span>";
        
            this.AccHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;' wtf:qtip='" + accountDetailsRec['debit'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec['debit'], 20) + "</span>";
        
        this.AccHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;' wtf:qtip='" + accountDetailsRec['taxpercent'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec['taxpercent'], 20) + "</span>";
        this.AccHeader += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;' >" + WtfGlobal.addCurrencySymbolOnly(accountDetailsRec['taxamount'], accountDetailsRec['currencysymbol'], [true]) + "</span>";
        this.AccHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;' >" + WtfGlobal.addCurrencySymbolOnly(accountDetailsRec['totalamount'], accountDetailsRec['currencysymbol'], [true]) + "</span>";
        if(accountDetailsRec.isIncludingGst){
            this.AccHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;' >" + WtfGlobal.addCurrencySymbolOnly(accountDetailsRec['totalamountforaccount'], accountDetailsRec['currencysymbol'], [true]) + "</span>";
        }
        if (accountDetailsRec['description'] != null && accountDetailsRec['description'] != "") {
            this.AccHeader += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec['description'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec['description'], 20) + "</span>";
        } else {
            this.AccHeader += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec['description'] + "'>" + Wtf.util.Format.ellipsis("&nbsp", 20) + "</span>";
        }
        for (var j = 0; j < this.custArr.length; j++) {
            if (accountDetailsRec[this.custArr[j].dataIndex] != undefined && accountDetailsRec[this.custArr[j].dataIndex] != "null" && accountDetailsRec[this.custArr[j].dataIndex] != "") {
                if (this.custArr[j].xtype == "datefield") {
                   var dateString="";
                   dateString=isNaN(accountDetailsRec[this.custArr[j].dataIndex]*1) ? (accountDetailsRec[this.custArr[j].dataIndex]) : (accountDetailsRec[this.custArr[j].dataIndex]*1);
                    var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(dateString));
                    this.AccHeader += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + linelevel_datefield + "'>" + Wtf.util.Format.ellipsis(linelevel_datefield, 15) + "</span>";
                } else
                    this.AccHeader += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec[this.custArr[j].dataIndex] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec[this.custArr[j].dataIndex], 15) + "</span>";
            }
            else
                this.AccHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'>&nbsp;&nbsp;</span>";
        }
        this.AccHeader += "<br>";
        this.AccCount++;
        return this.AccHeader;
},

getHeaderData:function(rec,sameParent,widthInPercent){
        if (!sameParent || this.invCnt == 1) {
            this.header = "";
            this.invCnt = 1;
        }

        var isCustomer = (this.businessPerson == "Customer") ? true : false
        if(rec.data.cntype==5){
            isCustomer=!isCustomer;
        }
        var isOpeningInvoice = rec.data.isOpeningInvoice;
        var transactionNoLink = isOpeningInvoice? "<a  class='jumplink' href='#' onClick='javascript:alertForOpeningTransactions()'>"+rec.data.transectionno+"</a>":"<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\"" + rec.data['transectionid'] + "\",\"" + isCustomer + "\",\"" + this.isCustBill + "\")'>" + rec.data['transectionno'] + "</a>";
        this.header += "<span class='gridNo'>" + (this.invCnt) + ".</span>";
        this.header += "<span class='gridRow ' >" + transactionNoLink + "</span>";
        this.header += "<span class='gridRow'>" + WtfGlobal.onlyDateLeftRenderer(rec.data['invcreationdate']) + "</span>";
        this.header += "<span class='gridRow'>" + WtfGlobal.onlyDateLeftRenderer(rec.data['invduedate']) + "</span>";
        this.header += "<span class='gridRow'>" + WtfGlobal.onlyDateLeftRenderer(rec.data['grlinkdate']) + "</span>";
        this.header += "<span class='gridRow'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['invamount'], rec.data['currencysymbol'], [true]) + "</span>";
        this.header += "<span class='gridRow'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['invamountdue'], rec.data['currencysymbol'], [true]) + "</span>";

          for (var j = 0; j < this.custArr.length; j++) {
            if (rec.data[this.custArr[j].dataIndex] != undefined && rec.data[this.custArr[j].dataIndex] != "null" && rec.data[this.custArr[j].dataIndex] != "") {
                if (this.custArr[j].xtype == "datefield") {
                    var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(rec.data[this.custArr[j].dataIndex]*1));
                    this.header += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + linelevel_datefield + "'>" + Wtf.util.Format.ellipsis(linelevel_datefield, 15) + "</span>";
                } else
                    this.header += "<span class='gridRow'style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + rec.data[this.custArr[j].dataIndex] + "'>" + Wtf.util.Format.ellipsis(rec.data[this.custArr[j].dataIndex], 15) + "</span>";
            }
            else
                this.header += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'>&nbsp;&nbsp;</span>";
        }

        this.header += "<br>";
        this.invCnt++;
        return this.header;
},
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
//        if(header=="billno"){
//            var accid=this.Store.getAt(i).data['invoiceid'];
//                   this.fireEvent('invoice',accid);
//        }
    if(header=="entryno"){
            var accid = this.Store.getAt(i).data['journalentryid'];
            if (e.target.getAttribute('name') != undefined && e.target.getAttribute('name') != "")   // multiple links in single row
                accid = e.target.getAttribute('name');
            this.fireEvent('journalentry',accid,true,this.consolidateFlag,null,null,null,this.startDate.getValue(),this.endDate.getValue());
        }
        if(header=="noteno"){
           this.viewTransection(g,i,j);
        }
    },
    loadCMStore:function(){
        this.Store.load({
           params : {
               start : 0,
               limit : this.pP.combo.value,
               ss : this.quickPanelSearch.getValue()
           }
       });
        this.Store.on('load',this.storeloaded,this);
    },
    shortString:function(name){
        if(name.length > 20){
            return name.substr(0, 17) + '...';
        }
        return name;
    },
    viewTransection: function (grid, rowIndex, columnIndex) {
        if (rowIndex < 0 && this.grid.getStore().getAt(rowIndex) == undefined || this.grid.getStore().getAt(rowIndex) == null) {
            WtfComMsgBox(15, 2);
            return;
        }
        var formrec = this.grid.getStore().getAt(rowIndex);
        var cntype = formrec.data.cntype;
        if (cntype == Wtf.CNDN_TYPE_FOR_MALAYSIA) {
            callViewCreditNoteGst(true, formrec, false, false, this.isCNReport);////cntype = 5 - CN/DN Against Vendor for Gst
        } else if (cntype == Wtf.NoteForOvercharge) {
            var winid = this.isCNReport ? 'creditnoteForOverchargeView' + formrec.get("noteno") : 'debitnoteForOverchargeView' + formrec.get("noteno");
            callEditNoteForOvercharge(winid, formrec, true, true, this.isCNReport,true);//cntype=6 - CN/DN for Overcharge
        } else {
            this.isCNReport ? callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true, true, formrec.get('cntype'), formrec, null) : callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true, false, formrec.get('cntype'), formrec, null);
        }
    },

    performDelete:function(del){
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var delFlag=del;
        var data=[];
        var arr=[];
        var arrPartyJournal=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        var partyJournalFlag=false;
        var isLinkedInvoiceIsClaimed=false;
        var recNotDeletedDueToLinkedInvoicesClaimed='';
            for(i=0;i<this.recArr.length;i++){
                if(this.recArr[i].data.partlyJeEntryWithCnDn!=undefined&&this.recArr[i].data.partlyJeEntryWithCnDn!=""&&this.recArr[i].data.partlyJeEntryWithCnDn==1){
                   partyJournalFlag=true;
                   arrPartyJournal.push(this.Store.indexOf(this.recArr[i]));
                }else{
                   arr.push(this.Store.indexOf(this.recArr[i]));
                }
                if(this.recArr[i].data.isLinkedInvoiceIsClaimed){
                    isLinkedInvoiceIsClaimed=true;
                    recNotDeletedDueToLinkedInvoicesClaimed += recNotDeletedDueToLinkedInvoicesClaimed+='<b>'+this.recArr[i].data.noteno+'</b>'+", ";
                }
            }
            /*
             *Applicable for Malaysian Country
             */
            if(isLinkedInvoiceIsClaimed){
                if(recNotDeletedDueToLinkedInvoicesClaimed != ''){
                    recNotDeletedDueToLinkedInvoicesClaimed = recNotDeletedDueToLinkedInvoicesClaimed.substring(0,recNotDeletedDueToLinkedInvoicesClaimed.length-2);
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(this.isCNReport?WtfGlobal.getLocaleText("acc.malaysiangst.blockCNDelete"):WtfGlobal.getLocaleText("acc.malaysiangst.blockDNDelete"))+"<br>"+recNotDeletedDueToLinkedInvoicesClaimed], 2);
                    return;
                }
            }
            var deletePartyJournalFlag=false;
            if(partyJournalFlag&&arr.length==0&&this.recArr.length!=0){
                WtfComMsgBox(109,2);
                return;
            }
//            if(partyJournalFlag&&arrPartyJournal.length>0&&arr.length>0&&this.recArr.length!=0){
//            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.common.deletePartyJournalCNDNAlert") ,function(btn){           //"Confirm"
//                if(btn!="yes") {
//                    deleteFlag=false;
//                    return;
//                }
//            });
//            }
            deletePartyJournalFlag=partyJournalFlag&&arrPartyJournal.length>0&&arr.length>0&&this.recArr.length!=0;
            
             
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), deletePartyJournalFlag?WtfGlobal.getLocaleText("acc.common.deletePartyJournalCNDNAlert"):(this.transType!="Credit"? WtfGlobal.getLocaleText("acc.rem.150"):WtfGlobal.getLocaleText("acc.rem.149")) ,function(btn){           //"Confirm"
            if(btn!="yes") {
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.Store.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
            data= WtfGlobal.getJSONArray(this.grid,true,arr);
            var mode=(this.isCNReport?(this.isCustBill?64:45):(this.isCustBill?64:45));
            this.deleteUrl = "";
            if(this.businessPerson=="Customer") {
                if(delFlag=="del")
                {
                    this.deleteUrl = "ACCCreditNote/deleteCreditNoteTemporary.do";
                }
                else
                {
                    this.deleteUrl = "ACCCreditNoteCMN/deleteCreditNotesPermanent.do";   
                }
                
            } else if(this.businessPerson=="Vendor") {
                if(delFlag=="del")
                {
                    this.deleteUrl = "ACCDebitNote/deleteDebitNotesMerged.do";
                }
                else
                {
                    this.deleteUrl = "ACCDebitNoteCMN/deleteDebitNotesPermanent.do";
                }
                
            }

            Wtf.Ajax.requestEx({
                //                url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                url: this.deleteUrl,
                params:{
                    data:data,
                    mode:mode
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },

    genSuccessResponse:function(response){
       Wtf.Msg.show({
            title:(this.transType=="Credit")?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN"),
            msg: response.msg,
            buttons: Wtf.Msg.OK,
            scope:this,
            fn: function(btn){
                if(btn=='ok' && response.success){
                    (function(){
                        this.loadCMStore();
                    }).defer(WtfGlobal.gridReloadDelay(),this);
                }
            },
            animEl: 'elId',
            icon:(response.success)? Wtf.MessageBox.INFO:Wtf.MessageBox.WARNING
        });

        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
    },

    genFailureResponse:function(response){
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],1);    //'Alert'
    },
   
    calllinkInvoice:function(winid, reconRec){
        reconRec=(reconRec==undefined?"":reconRec);
        winid=(winid==null?"iaffilewin":winid);
        var panel = Wtf.getCmp(winid);
        var record = this.sm.getSelected();
        if(!panel){
            if(new Date(Wtf.account.companyAccountPref.activeDateRangeToDate) < new Date(WtfGlobal.convertToGenericStartDate(new Date()))){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.linkingDate.date.alert.ofActiveDateRange"),function(btn){
                    if(btn=="no") {
                        return;
                    }else{
            new Wtf.account.linkInvoiceCN({
                id:winid,
                isLinking:true,
                reloadGrid:this.grid.id,
                isCustBill:this.isCustBill,
                isCN:this.isCNReport,
                closable: true,
                record: record,
                modal: true,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                width: 800,
                height: 640,
                resizable: false,
                layout: 'border',
                buttonAlign: 'right',
                renderTo: document.body
            }).show();
        }
                },this);
            }else{
                new Wtf.account.linkInvoiceCN({
                    id:winid,
                    isLinking:true,
                    reloadGrid:this.grid.id,
                    isCustBill:this.isCustBill,
                    isCN:this.isCNReport,
                    closable: true,
                    record: record,
                    modal: true,
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    width: 800,
                    height: 640,
                    resizable: false,
                    layout: 'border',
                    buttonAlign: 'right',
                    renderTo: document.body
                }).show();  
            }
        }
    },
    
    callUnlinkInvoice:function(winid, reconRec){
        reconRec=(reconRec==undefined?"":reconRec);
        winid=(winid==null?"unlinkCn":winid);
        var panel = Wtf.getCmp(winid);
        var record = this.sm.getSelected();
        
        /*Debit/Credit Note against Invoice could not be unlinked */
        if ((this.moduleid == Wtf.Acc_Credit_Note_ModuleId || this.moduleid == Wtf.Acc_Debit_Note_ModuleId) && record.data.cntype == 1) {
            var message = this.moduleid == Wtf.Acc_Debit_Note_ModuleId ? WtfGlobal.getLocaleText("acc.pi.UnlinkDN.purchaseInvoice") : WtfGlobal.getLocaleText("acc.pi.UnlinkCN.salesInvoice");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), message], 2);
            return;
        }
        if(!panel){
            if(new Date(Wtf.account.companyAccountPref.activeDateRangeToDate).getTime() < new Date().getTime()){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.linkingDate.date.alert.ofActiveDateRange"),function(btn){
                    if(btn=="no") {
                        return;
                    }else{
            new Wtf.account.linkInvoiceCN({
                id:winid,
                reloadGrid:this.grid.id,
                isLinking:false,
                isCustBill:this.isCustBill,
                isCN:this.isCNReport,
                closable: true,
                record: record,
                modal: true,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                width: 800,
                height: 410,
                resizable: false,
                layout: 'border',
                buttonAlign: 'right',
                renderTo: document.body
            }).show();
        }
                },this);
            } else {
                new Wtf.account.linkInvoiceCN({
                    id:winid,
                    reloadGrid:this.grid.id,
                    isLinking:false,
                    isCustBill:this.isCustBill,
                    isCN:this.isCNReport,
                    closable: true,
                    record: record,
                    modal: true,
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    width: 800,
                    height: 410,
                    resizable: false,
                    layout: 'border',
                    buttonAlign: 'right',
                    renderTo: document.body
                }).show();
            }
        }
    },
    openPendingApprovalTab:function(){
        var panelID=this.isCNReport?"CreditNoteDetailsPendingApproval":"DebitNoteDetailsPendingApproval";
        var panel = Wtf.getCmp(panelID);
        if(panel==null){
            var pendingApproval= true;
            if(this.isCNReport){
                panel=getCNTab(false, panelID, WtfGlobal.getLocaleText("acc.field.PendingApprovalCNReport"), undefined, false,"", "",Wtf.Acc_Credit_Note_ModuleId,"",pendingApproval);
            } else {
                panel=getDNTab(false, panelID, WtfGlobal.getLocaleText("acc.field.PendingApprovalDNReport"), undefined, false,"", "",Wtf.Acc_Debit_Note_ModuleId,"",pendingApproval);
            }
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    },

    handleReject:function(){
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.withInvMode = false;
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        var label= this.isCNReport?"Credit Note":"Debit Note";
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttorejectselected")+label+"?",function(btn){
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
            data= WtfGlobal.getJSONArray(this.grid,true,arr);
            this.ajxUrl = this.isCNReport?"ACCCreditNote/rejectPendingCreditNote.do":"ACCDebitNote/rejectPendingDebitNote.do";
            Wtf.Ajax.requestEx({
                url:this.ajxUrl,
                params:{
                    data:data,
                    isReject:true,                   
                    amount:this.recArr[0].data.amountinbase
                }
            },this,this.genSuccessResponseReject,this.genFailureResponseReject);
        },this);
    },
    genSuccessResponseReject:function(response){
        WtfComMsgBox([this.label,response.msg],response.success*2+1);
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
                this.loadCMStore();
            }, this);
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

    approvePendingNote: function() {
        var formRecord = this.grid.getSelectionModel().getSelected();
        var formRecordData = formRecord.data;
        if(formRecordData.hasApprovalAuthority){
            if(formRecordData.cntype == 1 && formRecordData.isFinalLevelApproval){ 
                // For note Against invoice when approval level is final then we need to check for invoices wheather they are used in other transaction during pending or not
                // If they are used and their amound due changed then we need to as per approval type came from response 
                Wtf.Ajax.requestEx({
                    url:this.isCNReport?"ACCCreditNote/checkInvoiceKnockedOffDuringCreditNotePending.do":"ACCDebitNote/checkInvoiceKnockedOffDuringDebitNotePending.do",
                    params: {
                        billid : formRecordData.noteid
                    }
                },this,function(response){
                    if(response.success){
                        var approvalType = response.approvalType;
                        if(approvalType == 1){//Approve As normal Way
                            
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+(this.isCNReport?" Credit Note":" Debit Note")+"?",function(btn){
                                if(btn=="yes") {
                                    this.callApprovalRemarkWindow(approvalType);
                                } else {
                                    return;
                                }
                            }, this);
                        } else if(approvalType == 2){// Approve CN/DN against Invoice As otherwise
                            var msg = "";
                            if(this.isCNReport){
                                msg = WtfGlobal.getLocaleText("acc.field.sinceinvoiceofcreditnotefullyutilized");
                            } else {
                                msg = msg = WtfGlobal.getLocaleText("acc.field.sinceinvoiceofdebitnotefullyutilized");
                            }
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), msg+WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+(this.isCNReport?" Credit Note":" Debit Note")+"?",function(btn){
                                if(btn=="yes") {
                                    this.callApprovalRemarkWindow(approvalType);
                                } else {
                                    return;
                                }
                            },this); 
                        } else if(approvalType == 3){ //Approve after Edit record
                            var msg = "";
                            if(this.isCNReport){
                                msg = WtfGlobal.getLocaleText("acc.field.sinceinvoiceofcreditnotepartiallyutilized");
                            } else {
                                msg = msg = WtfGlobal.getLocaleText("acc.field.sinceinvoiceofdebitnotepartiallyyutilized");
                            }
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), msg+WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+(this.isCNReport?" Credit Note":" Debit Note")+"?",function(btn){
                                if(btn=="yes") {
                                    var isEdit= true;
                                    var isEditToApprove = true;
                                    var isLinkedTransaction = formRecord.get("isLinkedTransaction");
                                    var notetabid=(this.isCNReport?"EditCreditNote":"EditDebitNote")+ formRecord.get("noteno");  
                                    createNote(notetabid,isEdit,this.isCNReport,formRecordData.cntype,formRecord,this.grid.id,false,isLinkedTransaction,isEditToApprove);
                                } else {
                                    return;
                                }
                            },this); 
                        } 
                    }
                },function(response){

                });
            } else {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+(this.isCNReport?" Credit Note":" Debit Note")+"?",function(btn){
                    if(btn=="yes") {
                        var approvalType = 1;//Normal Approval
                        this.callApprovalRemarkWindow(approvalType);
                    } else {
                        return;
                    }
                }, this);
            }
        } else {//Has No Athority then message will shown
            var msg = WtfGlobal.getLocaleText("acc.msgbox.YouarenotauthorizedtoapprovethisrecordatLevel")+" "+formRecordData.approvalLevel;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        }
    },

    callApprovalRemarkWindow :function(approvalType){
        var URL = this.isCNReport?"ACCCreditNote/approvePendingCreditNote.do":"ACCDebitNote/approvePendingDebitNote.do";
        var winTitle=this.isCNReport?"Approve Pending Credit Note":"Approve Pending Debit Note";
        var formRecord = this.grid.getSelectionModel().getSelected();
        var formRecordData = this.grid.getSelectionModel().getSelected().data;
        var itemsArr=[];
        this.remarkField =new Wtf.form.TextArea({
        fieldLabel : WtfGlobal.getLocaleText("acc.field.AddRemark*"),
        width : 200,
        height : 100,
        allowBlank : false,
        maxLength : 1024
    })
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
        this.postingDate.on('change', function (scope,newVal,oldVal) {
            var record = this.grid.getSelectionModel().getSelected();
            var creationDate = record.data.date;
            var todaysDate=Wtf.serverDate;
            if(!isFromActiveDateRange(newVal)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.pending.pstingdateactivedateerrormsg")],2);
                this.postingDate.setValue(oldVal);
            }else if (newVal < creationDate){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.pending.pstingdateerrormsg")],2);
                this.postingDate.setValue(oldVal);
            }else if(newVal > todaysDate) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.pending.pstingdateerrortodaymsg")],2);
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
                html:getTopHtml(WtfGlobal.getLocaleText("acc.field.ApprovePending") +(this.isCNReport?" Credit Note":" Debit Note") ,WtfGlobal.getLocaleText("acc.field.ApprovePending") +(this.isCNReport?" Credit Note":" Debit Note") +" <b>"+formRecord.data.noteno+"</b>"  ,"../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
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
                handler : function () {
                        if (this.postingDate.isValid()) {
                            Wtf.getCmp("Approvebtn" + this.id).disable();
                            Wtf.Ajax.requestEx({
                                url: URL,
                                params: {
                                    billid: formRecord.data.noteid,
                                    billno: formRecord.data.noteno,
                                    amount: formRecord.data.amountinbase,
                                    remark: this.remarkField.getValue(),
                                    approvalType: approvalType,
                                    postingDate: WtfGlobal.convertToGenericDate(this.postingDate.getValue())
                                }
                            }, this, this.genSuccessResponseApproveNote, this.genFailureResponseApproveNote);
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.pending.pstingdatevalidationmsg")], 2);
                        }
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
    },
    
    genSuccessResponseApproveNote : function(response){
        this.remarkWin.close();
        var thisObj = this; //storing current object reference in a variable to access it inside the function of WtfComMsgBox below
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0, null, "", function (btn) {
            if (btn == "ok")
                thisObj.loadCMStore(); //access the store using the variable declared outside because it is not accessible using 'this'
        }, this);      
    },
    genFailureResponseApproveNote : function(response){
        Wtf.getCmp("Approvebtn"+this.id).enable();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
    },
    
    openNewTab:function(){
        if(this.moduleid == Wtf.Acc_Debit_Note_ModuleId && this.cntypeNo != undefined) {
            callCreditNote(false, true, this.isCustomer);
        } else if (this.moduleid == Wtf.Acc_Credit_Note_ModuleId && this.cntypeNo != undefined) {
            callCreditNote(true, true, this.isCustomer);
        } else {
            callCreateNewButtonFunction(this.moduleid,this.cntypeNo,this.isCustomer,this.isForAgainstInvoice,undefined,undefined,undefined,this.inputType);
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
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.Store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: this.pP.combo.value}});
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: this.pP.combo.value}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },
    expandNote: function (id, exponly, noteType) {
        this.invID = id
        if (exponly) {
            this.pagingToolbar.hide();
            this.exponly = exponly;
            this.typeEditor.setValue(noteType);
            this.Store.load({
                params: {
                    noteid: id
                }
            });
            this.Store.on('load', this.expandRow, this);
        }
    },
    expandRow: function () {
        if (this.Store.getCount() == 0) {
            if (this.exportButton)
                this.exportButton.disable();
//            if (this.printButton)
//                this.printButton.disable();
            var emptyTxt = WtfGlobal.getLocaleText("acc.common.norec");
            this.grid.getView().refresh();
        } else {
            if (this.exportButton)
                this.exportButton.enable();
//            if (this.printButton)
//                this.printButton.enable();
        }
        this.Store.filter('noteid', this.invID);
        if (this.exponly && (this.Store.getCount() !== 0)) {
            this.expander.toggleRow(0);
        }
    },
    
    globalInvoiceListGridAutoRefreshPublishHandler: function(response) {
    var res = eval("("+response.data+")");       
    if (res.success && ( Wtf.isAutoRefershReportonDocumentSave || (res.userSessionId != undefined && Wtf.userSessionId==res.userSessionId ))) {
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
        this.Store.load({
            params : {
                start:0,
                limit:this.pP.combo.value,
                pagingFlag:true
                           
            }
        });
        if (this.moduleid == Wtf.Acc_Credit_Note_ModuleId) {               
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.CreditNoteRefreshedmsg"));
        } else if(this.moduleid == Wtf.Acc_Debit_Note_ModuleId){
            Wtf.notify.msg("", WtfGlobal.getLocaleText("acc.field.DebitNoteRefreshedmsg"));
        }
      }
    },
    
    showCustomerCustomFieldFlag: function(moduleid){
        var customerCustomFieldFlag = true;
//        if(moduleid===Wtf.Acc_Credit_Note_ModuleId){
//            customerCustomFieldFlag = true;
//        }
        return customerCustomFieldFlag;
    },
    
    showVendorCustomField: function(moduleid){
        var vendorCustomFieldFlag = true;
//        if(moduleid===Wtf.Acc_Debit_Note_ModuleId){
//            vendorCustomFieldFlag = true;
//        }
        return vendorCustomFieldFlag;
    },
    getImportExtraConfigForModule: function(moduleid) {
        var extraConfig = {};
        if (moduleid == Wtf.Acc_Credit_Note_ModuleId) {
            extraConfig.url = "ACCCreditNote/importCreditNotes.do";
            extraConfig.isExcludeXLS = true;
        }
        return extraConfig;
    },
    getImportExtraParamsForModule: function(moduleid) {
        var extraParams = "";
        if (moduleid == Wtf.Acc_Credit_Note_ModuleId) {
            extraParams = "";
        }
        return extraParams;
    },
    getModuleNameForImport: function(moduleid) {
        var moduleName = "";
        if (moduleid == Wtf.Acc_Credit_Note_ModuleId) {
            moduleName = "Credit Note";
        }
        return moduleName;
    },
    createImportButtonsForCreditNote:function(bottombtnArr){
        var importBtnArr = [];
        
        // For Credit Note import button
         var extraConfig = this.getImportExtraConfigForModule(this.moduleid);
         var extraParams = this.getImportExtraParamsForModule(this.moduleid);
         extraConfig.otherwise=true;
         var importBtnArrayOtherWise = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(Wtf.Acc_Credit_Note_ModuleId), this.Store, extraParams, extraConfig);
         this.importCNOtherwiseButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.ImportCreditNoteOtherwise"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.ImportCreditNoteOtherwise"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayOtherWise
        });
        
        importBtnArr.push(this.importCNOtherwiseButton);
        var extraConfigForAgainstVendor=this.getImportExtraConfigForModule(this.moduleid);
        extraConfigForAgainstVendor.againstVendor=true;
        var importBtnArrayAgainstVendor = Wtf.documentImportMenuArray(this, this.getModuleNameForImport(Wtf.Acc_Credit_Note_ModuleId), this.Store, extraParams, extraConfigForAgainstVendor);
        
        this.importCNAgainstVenButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.ImportCreditNoteAgainstVendor"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.ImportCreditNoteAgainstVendor"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importBtnArrayAgainstVendor
        });
        
        importBtnArr.push(this.importCNAgainstVenButton);
        
        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu: importBtnArr
        });
        bottombtnArr.push(this.importBtn);
    },
    getColumnIndex: function(dataIndex) {   //This is used to get index of perticular Header.
        var colIndex = 0;
        for (colIndex; colIndex < this.grid.getColumnModel().getColumnCount(); colIndex++) {
            if (this.grid.getColumnModel().getDataIndex(colIndex) === dataIndex) {
                return colIndex;
            }
        }
        return -1;

    },
    getMyConfig:function (){
        WtfGlobal.getGridConfig (this.grid, this.moduleid, false, false);
        
        var statusForCrossLinkage = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkage != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkage, true);
        }
    },
    saveMyStateHandler: function (grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
    }
    ,

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
