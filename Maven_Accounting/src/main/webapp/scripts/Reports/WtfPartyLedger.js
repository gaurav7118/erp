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

// Customer Party Ledger
function callCustomerPartyLedgerReportDynamicLoad(){
    var panel = Wtf.getCmp("mainCustomerPartyLedger");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title: WtfGlobal.getLocaleText("acc.field.CustomerPartyLedgerReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.CustomerPartyLedgerReport"),
            id:'mainCustomerPartyLedger',
            withinventory:true,
            custVendorID:"",
            closable:true,
            border:false,
            iconCls:'accountingbase balancesheet',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        callCustomerPartyLedgerSummary(true,"")
        callCustomerPartyLedgerDetailReport(true,"");
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }else{
        Wtf.getCmp('as').setActiveTab(panel);
        var obj1 = Wtf.getCmp("CustomerPartyLedgerSummary");
        var obj2 = Wtf.getCmp("CustomerPartyLedgerDetail");
        if(obj1!=undefined && obj2!=undefined){
            obj1.Name.setValue("");
            obj1.PartyLedgerStore.reload();            
            obj2.Name.setValue("");
            obj2.PartyLedgerStore.reload();
        }
    }
}

function callCustomerPartyLedgerSummary(withinventory,custVendorID){
    var CustomerPartyLedgerPanel=Wtf.getCmp("CustomerPartyLedgerSummary");
    if(CustomerPartyLedgerPanel==null){
        callCustomerPartyLedgerReport();
        CustomerPartyLedgerPanel = new Wtf.account.PartyLedger({
            id: 'CustomerPartyLedgerSummary',
            border: false,
            helpmodeid:28,
            isSummary:true,
            withinventory:withinventory,
            custVendorID:custVendorID,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: WtfGlobal.getLocaleText("acc.agedPay.summaryView"),  //'Summary View',
            tabTip:WtfGlobal.getLocaleText("acc.cutomer.partyledgersummary"),
            receivable:true
        });
        Wtf.getCmp('mainCustomerPartyLedger').add(CustomerPartyLedgerPanel);
    }
    Wtf.getCmp('mainCustomerPartyLedger').setActiveTab(CustomerPartyLedgerPanel);
    Wtf.getCmp('mainCustomerPartyLedger').doLayout();
}

function callCustomerPartyLedgerDetailReport(withinventory,custVendorID){
    var CustomerPartyLedgerPanel=Wtf.getCmp("CustomerPartyLedgerDetail");
    if(CustomerPartyLedgerPanel==null){
        callCustomerPartyLedgerReport();
        CustomerPartyLedgerPanel = new Wtf.account.PartyLedger({
            id: 'CustomerPartyLedgerDetail',
            border: false,
            helpmodeid:83,
            withinventory:withinventory,
            custVendorID:custVendorID,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: WtfGlobal.getLocaleText("acc.agedPay.reportView"),  //'Report View',
            tabTip:WtfGlobal.getLocaleText("acc.customer.customerpartyledgerreportview"),
            receivable:true
        });
        CustomerPartyLedgerPanel.on("activate",function(){
            CustomerPartyLedgerPanel.storeloaded(CustomerPartyLedgerPanel.PartyLedgerStore);
            CustomerPartyLedgerPanel.doLayout();
        });
        Wtf.getCmp('mainCustomerPartyLedger').add(CustomerPartyLedgerPanel);
        CustomerPartyLedgerPanel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('mainCustomerPartyLedger').doLayout();
}

// Vendor Party Ledger

function callVendorPartyLedgerReportDynamicLoad(){
    var panel = Wtf.getCmp("mainVendorPartyLedger");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title: WtfGlobal.getLocaleText("acc.field.vendorPartyLedger"),
            tabTip: WtfGlobal.getLocaleText("acc.field.vendorPartyLedger"),
            id:'mainVendorPartyLedger',
            withinventory:true,
            custVendorID:"",
            closable:true,
            border:false,
            iconCls:'accountingbase balancesheet',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        callVendorPartyLedgerSummary(true,"")
        callVendorPartyLedgerDetailReport(true,"");
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }else{
        Wtf.getCmp('as').setActiveTab(panel);
        var obj1 = Wtf.getCmp("VendorPartyLedgerSummary");
        var obj2 = Wtf.getCmp("VendorPartyLedgerDetail");
        if(obj1!=undefined && obj2!=undefined){
            obj1.Name.setValue("");
            obj1.PartyLedgerStore.reload();            
            obj2.Name.setValue("");
            obj2.PartyLedgerStore.reload();
        }
    }
}

function callVendorPartyLedgerSummary(withinventory,custVendorID){
    var VendorPartyLedgerPanel=Wtf.getCmp("VendorPartyLedgerSummary");
    if(VendorPartyLedgerPanel==null){
        callVendorPartyLedgerReport();
        VendorPartyLedgerPanel = new Wtf.account.PartyLedger({
            id: 'VendorPartyLedgerSummary',
            border: false,
            helpmodeid:28,
            isSummary:true,
            withinventory:withinventory,
            custVendorID:custVendorID,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: WtfGlobal.getLocaleText("acc.agedPay.summaryView"),  //'Summary View',
            tabTip:WtfGlobal.getLocaleText("acc.vendor.partyledgersummary"),
            receivable:false
        });
        Wtf.getCmp('mainVendorPartyLedger').add(VendorPartyLedgerPanel);
    }
    Wtf.getCmp('mainVendorPartyLedger').setActiveTab(VendorPartyLedgerPanel);
    Wtf.getCmp('mainVendorPartyLedger').doLayout();
}

function callVendorPartyLedgerDetailReport(withinventory,custVendorID){
    var VendorPartyLedgerPanel=Wtf.getCmp("VendorPartyLedgerDetail");
    if(VendorPartyLedgerPanel==null){
        callVendorPartyLedgerReport();
        VendorPartyLedgerPanel = new Wtf.account.PartyLedger({
            id: 'VendorPartyLedgerDetail',
            border: false,
            helpmodeid:83,
            withinventory:withinventory,
            custVendorID:custVendorID,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: WtfGlobal.getLocaleText("acc.agedPay.reportView"),  //'Report View',
            tabTip:WtfGlobal.getLocaleText("acc.customer.vendorpartyledgerreportview"),
            receivable:false
        });
        VendorPartyLedgerPanel.on("activate",function(){
            VendorPartyLedgerPanel.storeloaded(VendorPartyLedgerPanel.PartyLedgerStore);
            VendorPartyLedgerPanel.doLayout();
        });
        Wtf.getCmp('mainVendorPartyLedger').add(VendorPartyLedgerPanel);
        VendorPartyLedgerPanel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('mainVendorPartyLedger').doLayout();
}

//***********************************************************************************************


Wtf.account.PartyLedger=function(config){
    this.receivable=config.receivable||false;
    this.withinventory=config.withinventory||false;
    this.isSummary=config.isSummary||false;
    this.totalaged=config.totalaged||false;
    this.summary = new Wtf.ux.grid.GridSummary({});
    this.custVendorID=config.custVendorID;
    
    this.PartyLedgerRec = new Wtf.data.Record.create([{
        name:'billid'
    },{
        name:'journalentryid'
    },{
        name:'entryno'
    },{
        name:'billno'
    },{
        name:'noteid'
    },{
        name:'noteno'
    },{
        name:'date', 
        type:'date'
    },{
        name:'duedate', 
        type:'date'
    },{
        name:'personname'
    },{
        name:'aliasname'
    },{
        name:'personemail'
    },{
        name:'personid'
    },{
        name:'code'
    },{
        name:'salespersonname'
    },{
        name:'salespersonid',
        mapping:'salesPerson'
    },{
        name: 'currencysymbol'
    },{
        name: 'currencyname'
    },{
        name: 'currencyid'
    },{
        name: 'd_open_amount_base'
    },{
        name: 'c_open_amount_base'
    },{
        name: 'd_amount_base'
    },{
        name: 'c_amount_base'
    },{
        name: 'balance_base'
    },{
        name: 'withoutinventory', 
        type:'boolean'
    },{
        name:'type'
    },{
        name:'fixedAssetInvoice'
    },{
        name:'fixedAssetLeaseInvoice'
    },{
        name:'customerId'
    },{
        name:'start'
    },{
        name: 'fCustomerId'
    },{
        name: 'exchangerate'
    }]);

    this.PartyLedgerStoreUrl = "";
    this.PartyLedgerStoreSummaryUrl = "";
    if(this.receivable){
        this.expGet =  Wtf.autoNum.CustomerPartyLedgerDetails;
        this.PartyLedgerStoreUrl = "ACCInvoiceCMN/getCustomerPartyLedgerDetails.do";
        this.PartyLedgerStoreSummaryUrl = this.isSummary?"ACCInvoiceCMN/getCustomerPartyLedgerSummary.do":"ACCInvoiceCMN/getCustomerPartyLedgerDetails.do";
        this.expSummGet = this.isSummary? Wtf.autoNum.CustomerPartyLedgerSummary : Wtf.autoNum.CustomerPartyLedgerDetails;  
    }else{
        this.expGet = Wtf.autoNum.VendorPartyLedgerDetails;
        this.PartyLedgerStoreUrl = "ACCGoodsReceiptCMN/getVendorPartyLedgerDetails.do";
        this.PartyLedgerStoreSummaryUrl = this.isSummary?"ACCGoodsReceiptCMN/getVendorPartyLedgerSummary.do":"ACCGoodsReceiptCMN/getVendorPartyLedgerDetails.do";
        this.expSummGet = this.isSummary? Wtf.autoNum.VendorPartyLedgerSummary : Wtf.autoNum.VendorPartyLedgerDetails;  
    }
    
    this.PartyLedgerStore =this.isSummary? new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.PartyLedgerRec),
        groupField:'personname', 
        //sortInfo: {field: 'personname',direction: "DESC"},
        url: this.PartyLedgerStoreSummaryUrl,
        baseParams:{
            mode:(this.isSummary?18:(this.withinventory?12:16)),
            creditonly:true,
            withinventory:this.withinventory,
            nondeleted:true,
            isAged:true
        }
    }):new Wtf.ux.grid.MultiGroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.PartyLedgerRec),
        groupField:['personname','type'],
        sortInfo: {
            field: 'billno',
            direction: "ASC"
        },
        url: this.PartyLedgerStoreUrl,
        baseParams:{
            mode:(this.withinventory?12:16),
            creditonly:true,
            withinventory:this.withinventory,
            nondeleted:true,
            isAged:true
        }
    });

    this.rowNo=new Wtf.KWLRowNumberer();
    this.chkselModel = new Wtf.grid.CheckboxSelectionModel({
        singleSelect : false
    });
    this.rowselModel = new Wtf.grid.RowSelectionModel();
    this.selModel = this.isSummary?this.chkselModel:this.chkselModel;
    
    this.cm= new Wtf.grid.ColumnModel([this.selModel, this.rowNo,{
        header: WtfGlobal.getLocaleText("acc.field.DocumentNumber"),
        hidden:this.isSummary,
        dataIndex:'billno',
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.agedPay.gridJEno"),  //"Journal Entry Number",
        dataIndex:'entryno',
        hidden:(this.isSummary),
        width:150,
        pdfwidth:100,
        sortable: true,
        groupable: true,
        groupRenderer: function(v){
            return v
            },
        renderer:WtfGlobal.linkRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.agedPay.gridDate"),  //"Bill Date",
        dataIndex:'date',
        width:150,
        pdfwidth:100,
        align:'center',
        groupRenderer:this.groupDateRender.createDelegate(this),
        hidden:(this.isSummary),
        renderer:WtfGlobal.onlyDateRenderer
    },{
        header:this.receivable?"Customer Code":"Vendor Code",
        dataIndex:'code',
        width:150,
        pdfwidth:150,
        sortable: true
    },{
        header:(this.receivable?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.agedPay.ven"))+ " "+WtfGlobal.getLocaleText("acc.masterconfig.AddEditWin.AddEditMasterData.nameText"),
        dataIndex:'personname',
        width:200,
        pdfwidth:150,
        hidden:!this.isSummary,
        sortable: true,
        groupable: true
    },{
        header:(this.receivable?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.agedPay.ven"))+ " "+WtfGlobal.getLocaleText("acc.masterconfig.AddEditWin.AddEditMasterData.nameText"),
        dataIndex:'personname',
        width:150,
        pdfwidth:150,
        hidden:this.isSummary,
        sortable: true,
        groupable: true
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.salesPersonName"),
        dataIndex:'salespersonname',
        hidden:this.isSummary,
        width:150,
        pdfwidth:150
    },{
        header: WtfGlobal.getLocaleText("acc.field.TransactionType"),
        dataIndex:'type',
        align:'center',
        pdfwidth:120,
        width:150,
        groupable: true,
        hidden:this.isSummary
    },{
        header:WtfGlobal.getLocaleText("acc.field.OpeningDebit"),  //Opening Debit
        dataIndex:'d_open_amount_base',
        align:'right',
        width:150,
        pdfwidth:120,
        hidden : !this.isSummary,
        renderer:WtfGlobal.currencyRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.field.OpeningCredit"),  //"Opening Credit",
        dataIndex:'c_open_amount_base',
        hidden : !this.isSummary,
        align:'right',
        width:150,
        pdfwidth:120,
        renderer:WtfGlobal.currencyRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.common.debit"), // "Debit"
        dataIndex:'d_amount_base',
        width:150,
        pdfwidth:120,
        align:'right',
        renderer:WtfGlobal.currencyRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.common.credit"), // "Credit"
        dataIndex:'c_amount_base',
        width:150,
        pdfwidth:120,
        align:'right',
        renderer:WtfGlobal.currencyRenderer
    },{
        header: this.isSummary? WtfGlobal.getLocaleText("acc.field.Closing"):WtfGlobal.getLocaleText("acc.saleByItem.gridBalance"),  //"Closing" / "Balance",
        dataIndex:'balance_base',
        align:'right',
        pdfwidth:100,
        width:150,
        renderer:WtfGlobal.currencyRenderer
    }]);   
    
    this.groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit: false,
        showGroupName: false,
        enableNoGroups: false,
        isGrandTotal: false,
        isGroupTotal: false,
        hideGroupedColumn:false,
        emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec')),
        groupTextTpl: '{group} '    
    });
    this.GrandTotalSummary=new Wtf.XTemplate(// to display the grand total 
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            
        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.grandtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{grandTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '</table>'+
        '</div>',            
                                 
        '</div>'
        );
            
    this.GrandTotalSummaryTPL=new Wtf.Panel({
        id:this.isSummary?'GrandTotalSummaryTPL'+this.id:'GrandTotalReportTPL'+this.id,
        border:false,
        width:'95%',
        baseCls:'tempbackgroundview',
        html:this.GrandTotalSummary.apply({
            grandTotal:WtfGlobal.currencyRenderer(0)                    
        })
    }); 
    
    this.GrandTotalReport=new Wtf.XTemplate(// to display the grand total 
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            
        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.grandtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{grandTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '</table>'+
        '</div>',            
                                 
        '</div>'
        );
            
    this.GrandTotalReportTPL=new Wtf.Panel({
        id:this.isSummary?'GrandTotalSummaryTPL'+this.id:'GrandTotalReportTPL'+this.id,
        border:false,
        width:'95%',
        baseCls:'tempbackgroundview',
        html:this.GrandTotalReport.apply({
            grandTotal:WtfGlobal.currencyRenderer(0)                    
        })
    }); 
            
    this.tbar3 = new Array();
    this.bbar = new Array();
    this.grid = this.isSummary?new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.PartyLedgerStore,
        cm:this.cm,
        sm: this.selModel,
        ctCls : 'agedview' ,
        border:false,
        layout:'fit',
        tbar:this.tbar3,
        view:new Wtf.grid.GridView({
            emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec')),
            forceFit:true            
        }),
        bbar:this.bbar,
        loadMask : true
    }):new Wtf.ux.grid.MultiGroupingGrid({
        stripeRows :true,
        store:this.PartyLedgerStore,
        border:false,
        ctCls:'agedsummary',
        view: this.groupView,
        plugins: [this.summary],
        cm:this.cm,
        sm: this.selModel,
        tbar:this.tbar3,
        bbar:this.bbar
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        hidden:this.isSummary,
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,       
        hidden:this.isSummary,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    /*
     * Provided button to expand or collapse all row details. 
     * We display Document Number,Journal Entry Number,Bill Date,Vendor Code,
     * Vendor Name,Sales Person Name,Transaction Type,Debit,Credit,Balance
     */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });

    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.receivable?2:6,
        advSearch: false
    });
    
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'startdate',
        id: 'stdate'+config.id+config.helpmodeid,
        format:WtfGlobal.getOnlyDateFormat(),
        value:this.getDates(true)
    });
        
    this.curDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        id: 'dueDate'+config.id+config.helpmodeid,
        value:this.getDates(false)
    });
    this.startDate.on("change",this.checkDates,this);
    this.curDate.on("change",this.checkDates,this);

    this.expButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.exportTT"),  //'Export report details',
        disabled :true,
        filename:(this.receivable?"Customer Party Ledger Report_v1":"Vendor Party Ledger Report_v1"),
        params:{
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            accountid:this.accountID||config.accountID,
            curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            isAged:true,
            agedDetailsFlag: true,
            checkforex:true
        },
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            CRLetter:true,
            xls:true
        },
        get:this.isSummary?this.expSummGet:this.expGet
    });
    
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.printTT"),  //'Print report details',
        disabled :true,
        filename:(this.receivable?"Customer Party Ledger Report": "Vendor Party Ledger Report"), 
        params:{
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            accountid:this.accountID||config.accountID,
            curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            name: this.receivable?"Customer Party Ledger Report":"Vendor Party Ledger Report",
            isAged:true,
            checkforex:true
        },
        lable: this.receivable?"Customer Party Ledger Report":"Vendor Party Ledger Report",
        menuItem:{
            print:true
        },
        get:this.isSummary?this.expSummGet:this.expGet
    })
   
    this.personRec = new Wtf.data.Record.create([
    {
        name: 'accid'
    }, {
        name: 'accname'
    }, {
        name: 'acccode'
    }
    ]);       
   
    var nameUrl="ACCVendor/getVendorsForCombo.do";
    var baseParamArray={
        deleted:false,
        nondeleted:true,
        combineData:-1  //Send For Seprate Request
    };
    
    if(this.receivable){
        nameUrl="ACCCustomer/getCustomersForCombo.do";
    } 
    
    this.customerAccStore =  new Wtf.data.Store({   //Customer/vendor multi selection Combo
        url:nameUrl,
        baseParams:baseParamArray,
        reader: new  Wtf.data.KwlJsonReader({
            root: "data"
        },this.personRec)
    });

    this.customerAccStore.on("load", function(store){
        var storeNewRecord=new this.personRec({
            accname:'All',
            accid:'All',
            acccode:''
        });
        this.Name.store.insert( 0,storeNewRecord);
        if((this.custVendorID==undefined ||this.custVendorID=="")){
            this.Name.setValue("All");   
        }else{
            this.Name.setValue(this.custVendorID); 
        }         
    },this);
    this.customerAccStore.load();
   
    this.CustomerComboconfig = {    
        store: this.customerAccStore,
        valueField:'accid',
        hideLabel:true,
        displayField:'accname',
        emptyText:this.receivable?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") ,
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
    
    this.Name = new Wtf.common.Select(Wtf.applyIf({
        multiSelect:true,
        fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*' ,
        forceSelection:true,    
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        width:240
    },this.CustomerComboconfig));

    this.Name.on('select',function(combo,personRec){
        if(personRec.get('accid')=='All'){
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){
            combo.clearValue();
            combo.setValue(personRec.get('accid'));
        }
    } , this);
    var btnArr=[];
    var bottombtnArr=[];
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.agedPay.search"), 
            id:"quickSearch"+config.helpmodeid,
            width: 180,
            hidden:this.isSummary,
            field: 'personname'
        }),
        this.resetBttn,this.Name,'-',
        WtfGlobal.getLocaleText("acc.common.from"),this.startDate,"-",
        WtfGlobal.getLocaleText("acc.common.to"),this.curDate);

    btnArr.push(
    {
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
        iconCls:'accountingbase fetch',
        scope:this,
        tooltip:WtfGlobal.getLocaleText("acc.common.fetchbuttonview")+this.isSummary?(this.receivable?WtfGlobal.getLocaleText("acc.customer.customerpartyledgerreport"):WtfGlobal.getLocaleText("acc.customer.vendorpartyledgerreport")):(this.receivable?WtfGlobal.getLocaleText("acc.field.CustomerPartyLedgerReport"):WtfGlobal.getLocaleText("acc.field.vendorPartyLedger")),  
        handler:this.fetchAgedData
    }, this.AdvanceSearchBtn);
    
    if (!this.isSummary) {
        btnArr.push('-', this.expandCollpseButton);
    }

    if(this.isSummary){// to display the grand total 
        this.bbar.push("->",this.GrandTotalSummaryTPL);  
    }else{
        this.bbar.push("->",this.GrandTotalReportTPL);  
    }

    bottombtnArr.push('-', this.expButton);
    bottombtnArr.push('-', this.printButton);

    this.resetBttn.on('click',this.handleResetClick,this);
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 30,
        id: "pagingtoolbar" + this.id,
        store: this.PartyLedgerStore,
        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
        }),
        items:bottombtnArr
    })
        
    this.leadpan = new Wtf.Panel({
        border:false,
        layout : "border",
        items:[this.objsearchComponent
        , {
            region: 'center',
            layout: 'fit',
            border: false,
            tbar:btnArr,
            items: [this.grid],
            bbar: this.pagingToolbar
        }]
    });
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });
    
    Wtf.account.PartyLedger.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
    
    this.PartyLedgerStore.on("beforeload", function(s,o) {      
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
        o.params.curdate= WtfGlobal.convertToGenericEndDate(this.curDate.getValue());
        o.params.startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        o.params.enddate=WtfGlobal.convertToGenericEndDate(this.curDate.getValue());
//        o.params.isAged=true;
        s.baseParams.custVendorID=(this.Name.getValue()!="")?this.Name.getValue():this.custVendorID;        
        if(this.pP.combo!=undefined){
            if(this.pP.combo.value=="All" && this.Name.lastSelectionText==""){
                var count = this.PartyLedgerStore.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                o.params.limit = count;
            }
        }
    },this);
    this.PartyLedgerStore.on('load',this.storeloaded,this);
    
    this.PartyLedgerStore.load({
        params:{
            start:0,
            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),            
            enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            limit:30,
            creditonly:true
        }
    });
    
    this.PartyLedgerStore.on('datachanged', function() {
        var p = 30;
        this.quickPanelSearch.setPage(p);
    }, this);
    
    this.on('activate',function(){
        if(this.Name!=undefined){
            this.doLayout();
            this.Name.syncSize();
            this.Name.setWidth(240);
        }
    },this);
    this.grid.on('cellclick',this.onCellClick, this);
}
       
Wtf.extend( Wtf.account.PartyLedger,Wtf.Panel,{ 
    sumBaseAmount:function(dataindex,v,m,rec){       
        if(!this.isSummary){
            v=rec.data[dataindex];
            return WtfGlobal.withoutRateCurrencySymbol(v,m,rec)
        }
        return "";
    }, 
    groupDateRender:function(v){
        return v.format(WtfGlobal.getOnlyDateFormat())
    },
    totalRender:function(v,m,rec){
        var val=WtfGlobal.withoutRateCurrencySymbol(v,m,rec);
        return "<b>"+val+"</b>"
    },
    
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.PartyLedgerStore.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value,
                    creditonly:true,
                    startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                    enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                    isAged:true
                }
            });
        }
    },
    
    storeloaded:function(store){
        WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
        if(store.getCount()==0){
            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        this.quickPanelSearch.StorageChanged(store);
        var i = 0;       
        var grandTotalinbase = 0;             // to display the grand total 

        if(this.isSummary){
            for(i=0;i <= store.data.length-1;i++){
                grandTotalinbase+=store.getAt(i).get('balance_base');
            }
            this.GrandTotalSummary.overwrite(this.GrandTotalSummaryTPL.body,{
                grandTotal:WtfGlobal.withoutRateCurrencySymbol(grandTotalinbase)
            });
        }else{
            for(i=0;i <= store.data.length-1;i++){
                if(store.getAt(i).get('d_amount_base') != undefined || store.getAt(i).get('d_amount_base') != ""){
                    grandTotalinbase+=store.getAt(i).get('d_amount_base');
                }
                if(store.getAt(i).get('c_amount_base') != undefined || store.getAt(i).get('c_amount_base') != ""){
                    grandTotalinbase-=store.getAt(i).get('c_amount_base');
                }
            }
            this.GrandTotalReport.overwrite(this.GrandTotalReportTPL.body,{
                grandTotal:WtfGlobal.withoutRateCurrencySymbol(grandTotalinbase)
            });
        }
            
        this.grid.getView().refresh();
        if(this.grid.loadMask){
            this.grid.loadMask.hide();   
        }
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.PartyLedgerStore.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true);
        }
        if(header=="billno"){
            var formrec = this.PartyLedgerStore.getAt(i);
            var type=formrec.data['type'];
            var withoutinventoryFlag = formrec.data.withoutinventory;
            if(type=="Credit Note" && !withoutinventoryFlag){
                if(this.receivable){
                    callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,formrec.get('cntype'),formrec, null); 
                } else{
                    callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,4,formrec, null); //Credit Note Against Vendor need to send cntype 4
                }     
            }else if(type=="Debit Note" && !withoutinventoryFlag){
                if(this.receivable){
                    callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true,false,4,formrec, null);;  //Debit Note Against Customer need to send cntype 4
                }else{
                    callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true,false,formrec.get('cntype'),formrec, null);   
                }
            }else{
                viewTransactionTemplate(type, formrec);   
            }
        }
    },
    
    checkDates : function(curDate,newVal,oldVal){
        if(this.curDate.getValue()<this.startDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 2);  //From Date can not be greater than To Date.
            this.curDate.setValue(oldVal);
        }           
    },
        
    viewTransection:function(){						// Function for viewing the invoice details from the invoice list 
        var formrec=null;
        formrec = this.grid.getSelectionModel().getSelected();
        var type=formrec.data['type'];
        var withoutinventoryFlag = formrec.data.withoutinventory;            
        if(type=="Customer Invoice" && !withoutinventoryFlag){
            if(formrec.data.fixedAssetInvoice||formrec.data.fixedAssetLeaseInvoice){
                callViewFixedAssetInvoice(formrec, formrec.data.billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
            } else if(formrec.data.isConsignment){
                callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
            }else{
                callViewInvoice(formrec, 'ViewCashReceipt');
            }
        } else if(type=="Vendor Invoice" && !withoutinventoryFlag){
            if(formrec.data.fixedAssetInvoice){
                callViewFixedAssetGoodsReceipt(formrec, formrec.data.billid+'GoodsReceipt',false,formrec.data.isExpensiveInv,undefined,false,formrec.data.fixedAssetInvoice);
            } else{
                callViewGoodsReceipt(formrec, 'ViewGoodsReceipt',formrec.get("isexpenseinv"));
            }
        } else if(type == "Payment Received"&& !withoutinventoryFlag) {
            callViewPayment(formrec, 'ViewReceivePayment',true);
        } else if(type == "Payment Made" && !withoutinventoryFlag) {
            if(Wtf.isNewPaymentStructure) {
                callViewPaymentNew(formrec, 'ViewPaymentMade',false);
            }
        } else if(type == "Credit Note" && !withoutinventoryFlag) {
            callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,formrec.get('cntype'),formrec, null);
        } else if(type == "Debit Note" && !withoutinventoryFlag) {
            callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true,false,formrec.get('cntype'),formrec, null);
        } 
    },
    
    fetchAllAgedData:function(){
        this.allPageData=true;
        this.PartyLedgerStore.load({
            params:{
                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                custVendorID:this.Name.getValue(),
                start:0,
                limit:this.pP.combo.value,
                checkforex:true,
                isAged:true
            }
        });
    },
    
    fetchAgedData:function(){
        this.fetch=true;
        this.PartyLedgerStore.load({
            params:{
                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
                custVendorID:this.Name.getValue(),
                start:0,
                limit:this.pP.combo.value,
                isAged:true
            }
        });
        
        this.expButton.setParams({
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            accountid:this.accountID,
            enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            isAged:true
        });

        this.printButton.setParams({
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            accountid:this.accountID,
            enddate:WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            curdate: WtfGlobal.convertToGenericEndDate(this.curDate.getValue()),
            name: this.receivable?"Customer Party Ledger":"Vendor Party Ledger",
            isAged:true
        })
    }, 
    
    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.PartyLedgerStore.getAt(i).data['accountid'];
        this.fireEvent('account',accid);
    },

    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.PartyLedgerStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.receivable?2:6,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            isAged: true
        }
        this.PartyLedgerStore.load({
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
    
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.PartyLedgerStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.receivable?2:6,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.PartyLedgerStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
            }
        });
    },

    getTemplateConfig : function(){
        var title = WtfGlobal.getLocaleText("acc.field.Aged")+((this.receivable)?WtfGlobal.getLocaleText("acc.field.Receivables"):WtfGlobal.getLocaleText("acc.field.Payables"));
        var config = '{"landscape":"true","pageBorder":"true","gridBorder":"true","title":'+title+',"subtitles":"","headNote":"Aged Report","showLogo":"true","headDate":"true","footDate":"false","footPager":"false","headPager":"true","footNote":"","textColor":"000000","bgColor":"FFFFFF"}';
        return config;
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
    
    withoutRateCurrencySymbol: function(value,m,rec) {
        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        v= WtfGlobal.conventInDecimal(v,symbol)
        return v;
    },
    /*
     * ExpandCollapse button handler
     * To expand or collapse all row details
     * If grid rows are already in expand mode then collapse rows and vise versa
     */
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    }
});


Wtf.pPageSizeForAllOption = function(config){
    Wtf.apply(this, config);
};

Wtf.extend(Wtf.pPageSizeForAllOption, Wtf.util.Observable, {
    /**
     * @cfg {String} beforeText
     * Text to display before the comboBox
     */
    beforeText: WtfGlobal.getLocaleText("acc.rem.151"), //'Show',
    
    /**
     * @cfg {String} afterText
     * Text to display after the comboBox
     */
    afterText: WtfGlobal.getLocaleText("acc.rem.152"), //'items',
    
    /**
     * @cfg {Mixed} addBefore
     * Toolbar item(s) to add before the PageSizer
     */
    addBefore: '-',
    
    /**
     * @cfg {Mixed} addAfter
     * Toolbar item(s) to be added after the PageSizer
     */
    addAfter: null,
    
    /**
     * @cfg {Array} variations
     * Variations used for determining pageSize options
     */
    variations: [5, 10, 20, 50, 100],
    
    init: function(pagingToolbar){
        this.pagingToolbar = pagingToolbar;
        this.pagingToolbar.on('render', this.onRender, this);
    },
    
    //private
    addToStore: function(value){
        if (value > 0) {
            this.sizes.push([value]);
        }
    },
    
    //private
    updateStore: function(){
        var middleValue = this.pagingToolbar.pageSize, start;
        middleValue = (middleValue > 0) ? middleValue : 1;
        this.sizes = [];
        var v = this.variations;
        for (var i = 0, len = v.length; i < len; i++) {
            this.addToStore(middleValue - v[v.length - 1 - i]);
        }
        this.addToStore(middleValue);
        for (var i = 0, len = v.length; i < len; i++) {
            this.addToStore(middleValue + v[i]);
        }

        this.combo.store.loadData(this.sizes);
        this.combo.setValue(this.pagingToolbar.pageSize);
    },

    changePageSize: function(value){
        var pt = this.pagingToolbar;
        value = parseInt(value) || parseInt(this.combo.getValue());
        value = (value > 0) ? value : 1;
        if (value < pt.pageSize) {
            pt.pageSize = value;
            var ap = Math.round(pt.cursor / value) + 1;
            var cursor = (ap - 1) * value;
            var store = pt.store;
            store.suspendEvents();
            for (var i = 0, len = cursor - pt.cursor; i < len; i++) {
                store.remove(store.getAt(0));
            }
            while (store.getCount() > value) {
                store.remove(store.getAt(store.getCount() - 1));
            }
            store.resumeEvents();
            store.fireEvent('datachanged', store);
            pt.cursor = cursor;
            var d = pt.getPageData();
            pt.afterTextEl.el.innerHTML = String.format(pt.afterPageText, d.pages);
            pt.field.dom.value = ap;
            pt.first.setDisabled(ap == 1);
            pt.prev.setDisabled(ap == 1);
            pt.next.setDisabled(ap == d.pages);
            pt.last.setDisabled(ap == d.pages);
            pt.updateInfo();
        } else {
            this.pagingToolbar.pageSize = value;
            this.pagingToolbar.doLoad(Math.floor(this.pagingToolbar.cursor / this.pagingToolbar.pageSize) * this.pagingToolbar.pageSize);
        }
        this.updateStore();
        this.combo.collapse();
    },
    //private
    onRender: function(){
        var component = Wtf.form.ComboBox;
        this.combo = new component({
            store: new Wtf.data.SimpleStore({
                fields: ['pageSize'],
                data: []
            }),
            clearTrigger: false,
            displayField: 'pageSize',
            valueField: 'pageSize',
            editable: false,
            mode: 'local',
            triggerAction: 'all',
            width: 50
        });
        this.combo.on('select', this.changePageSize, this);
        this.updateStore();
        
        if (this.addBefore) {
            this.pagingToolbar.add(this.addBefore);
        }
        if (this.beforeText) {
            this.pagingToolbar.add(this.beforeText);
        }
        this.pagingToolbar.add(this.combo);
        if (this.afterText) {
            this.pagingToolbar.add(this.afterText);
        }
        if (this.addAfter) {
            this.pagingToolbar.add(this.addAfter);
        }
    }
})