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

function callCustomerVendorTLedgerReportDynamicLoad(){
    var panel = Wtf.getCmp("customerVendorTLedger");
    if(panel==null){
        panel = new Wtf.TabPanel({
            title:Wtf.util.Format.ellipsis("[T Shape]"+WtfGlobal.getLocaleText("acc.field.AnalysisReport"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.field.YoucanviewyourCustomerandVendorLedgerReportshere"),
            id:'customerVendorTLedger',
            closable:true,
            border:false,
            iconCls:'accountingbase balancesheet',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
        callCustomerVendorTLedgerTab(true);
        callCustomerVendorTLedgerTab(false);

    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
function callCustomerVendorTLedgerTab(isCustomer) {
    var id = (isCustomer ? 'customer' : 'vendor') + " TLedgerRpt"
    var title = (isCustomer ? WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.invoice.vendor")) +" "+ WtfGlobal.getLocaleText("acc.dashboard.consolidateLedgerReport");
    var customerLedgerRpt = Wtf.getCmp(id);
    if (customerLedgerRpt == null) {
        customerLedgerRpt = new Wtf.account.CustomerVendorLedger({
            id: id,
            border: false,
            isCustomer: isCustomer,
            layout: 'fit',
            iconCls: 'accountingbase ledger',
            title: title,
            tabTip: title
        });
        customerLedgerRpt.on('journalentry', callJournalEntryDetails)
        Wtf.getCmp('customerVendorTLedger').add(customerLedgerRpt);
    }
}

//******************************************************************************

Wtf.account.CustomerVendorLedger=function(config){
    this.summary = new Wtf.grid.GroupSummary({});
    this.isCustomer = config.isCustomer;

     this.custRec = Wtf.data.Record.create ([{
        name:'accountname',
        mapping:'accname'
    },{
        name:'accountid',
        mapping:'accid'
    },{
        name:'acccode'
    },{
        name:'groupname'
    }]);

    this.custStore = new Wtf.data.Store({
        url : this.isCustomer? "ACCCustomer/getCustomersForCombo.do": "ACCVendor/getVendorsForCombo.do",
        baseParams:{
            nondeleted:true,
            combineData:this.isCustomer?1:-1  //Send For Seprate Request
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.custRec)
    });
    
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=this.permType.exportdataledger;
    this.printPermType=this.permType.printledger;
    this.tabOpenFlag = true;

    this.LedgerRec = new Wtf.data.Record.create([
                {name: 'd_date',type:'date'},
                {name: 'd_accountname'},
                {name: 'd_acccode'},
                {name: 'd_entryno'},
                {name: 'd_journalentryid'},
                {name: 'd_amount'},
                {name: 'd_transactionID'},
                {name: 'd_transactionDetails'},
                {name: 'c_date',type:'date'},
                {name: 'c_accountname'},
                {name: 'c_acccode'},
                {name: 'c_entryno'},
                {name: 'c_journalentryid'},
                {name: 'c_amount'},
                {name: 'c_transactionID'},
                {name: 'c_transactionDetails'},
                {name: 'accountid'},
                {name: 'accountname'},
                {name: 'accCode'},
                {name: 'accCodeName'},
                {name: 'billid'},
                {name: 'd_type'},
                {name: 'c_type'},
                {name: 'noteid'},
                {name: 'c_amountAccountCurrency'},
                {name: 'd_amountAccountCurrency'},
                {name: 'currencysymbol'},
                {name: 'isCash'},
                {name: 'isLeaseFixedAsset'}
             
    ]);
            
    this.LedgerStore = new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.LedgerRec),
        //                url: Wtf.req.account+'CompanyManager.jsp',
        url:"ACCOtherReports/"+ (this.isCustomer ? "getCustomerLedger": "getVendorLedger") +  ".do", 
        baseParams:{
            mode:61,
            withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
            consolidateFlag:config.consolidateFlag,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            ledgerReport:true
        },               
        sortInfo : {
            field : 'accCodeName',
            direction : 'ASC'
        },
        groupField : 'accCodeName'
    });

    this.rowNo=new Wtf.grid.RowNumberer();
    this.gridcm= new Wtf.grid.ColumnModel([this.rowNo,
    {
        header: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //"Date",
        dataIndex: 'd_date',
        align:'center',
        width:100,
        pdfwidth:110,
        renderer:WtfGlobal.onlyDateRenderer
    },
    {
        header: WtfGlobal.getLocaleText("acc.ledger.gridAccount"),  //"Account",
        dataIndex: 'accCodeName',
        align:'center',
        width:100,
        pdfwidth:110,
        hidden:true
  
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.accName"),  //"Account Name",
        dataIndex: 'd_accountname',
        renderer:this.formatAccountName,
        width:150,
        pdfwidth:100,
         summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.gridJournalFolio"),  //"Journal Folio (J/F)",
        dataIndex: 'd_entryno',
        width:140,
        pdfwidth:100,
        renderer:WtfGlobal.linkRenderer
    },{
        header:"Memo",  // WtfGlobal.getLocaleText("acc.product.description"),
        dataIndex: 'd_transactionDetails',
        width:130,
        pdfwidth:100,
        renderer:function(value,meta){
            meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Description' ";
            return value;

        }
    },{
        header: WtfGlobal.getLocaleText("acc.field.TranID"),
        dataIndex: 'd_transactionID',
        width:130,
        pdfwidth:100,
        renderer:config.consolidateFlag?"":WtfGlobal.linkRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.product.description"),
        dataIndex: 'd_type',
        // renderer:this.formatAccountName,
        width:150,
        pdfwidth:100
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.gridDebitAmount"),//WtfGlobal.getLocaleText("acc.ledger.debitAmountAccountCcy"),
        dataIndex: 'd_amountAccountCurrency',
        id:'d_amountAccountCurrency',
        align:'right',
        width:140,
        pdfwidth:100,
        renderer:this.accountCurrencyRenderer,
        pdfrenderer : "rowcurrency"    
    },{
        header: WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency")+" ("+WtfGlobal.getCurrencyName()+")",  //"Debit Amount",
        dataIndex: 'd_amount',
        align:'right',
        renderer:WtfGlobal.currencyRenderer,
        width:140,
        summaryType:'sum1',
        pdfwidth:125,
        hidecurrency : true,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    
    },{
        width:1,
        renderer:this.showSeperator
    },{
        header: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //"Date",
        dataIndex: 'c_date',
        align:'center',
        width:130,
        renderer:WtfGlobal.onlyDateRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.accName"),  //"Account Name",
        dataIndex: 'c_accountname',
        renderer:this.formatAccountName,
        width:150,
        //                pdfwidth:100,
        summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.gridJournalFolio"),  //"Journal Folio (J/F)",
        dataIndex: 'c_entryno',
        width:140,
        renderer:WtfGlobal.linkRenderer
    },{
        header:"Memo", // WtfGlobal.getLocaleText("acc.product.description"),
        dataIndex: 'c_transactionDetails',
        width:130,
        renderer:function(value,meta){
            meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Description' ";
            return value;

        }
    },{
        header: WtfGlobal.getLocaleText("acc.field.TranID"),
        dataIndex: 'c_transactionID',
        width:130,
        renderer:config.consolidateFlag?"":WtfGlobal.linkRenderer
      },{
        header: WtfGlobal.getLocaleText("acc.product.description"),
        dataIndex: 'c_type',
        // renderer:this.formatAccountName,
        width:150,
        pdfwidth:100
    },{
        header:WtfGlobal.getLocaleText("acc.ledger.gridCreditAmount"),//WtfGlobal.getLocaleText("acc.ledger.creditAmountAccountCcy"),  //"Account",,
        dataIndex: 'c_amountAccountCurrency',
        id:'c_amountAccountCurrency',
        align:'right',
        width:140,
        pdfwidth:100,
        renderer:this.accountCurrencyRenderer,
        pdfrenderer : "rowcurrency"
    },{    
        header: WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency")+" ("+WtfGlobal.getCurrencyName()+")",  //"Credit Amount",
        align:'right',
        dataIndex: 'c_amount',
        renderer:WtfGlobal.currencyRenderer,
        width:140,
        summaryType:'sum1',
        pdfwidth:125,
        hidecurrency : true,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    
    }]);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store: this.LedgerStore,
        cm: this.gridcm,
        border : false,
        loadMask : true,
        view : new Wtf.grid.GroupingView({
            forceFit : false,
            showGroupName : true,
            startCollapsed :true,
            enableGroupingMenu : true,
            hideGroupedColumn : false
        }),
        plugins:[this.summary],
        viewConfig: {
            forceFit:false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        tbar:[WtfGlobal.getLocaleText("acc.trial.debit"),'->',WtfGlobal.getLocaleText("acc.trial.credit")]
    });    
    this.LedgerStore.on("beforeload", function(s,o) {
        s.baseParams.stdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        s.baseParams.enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        s.baseParams.accountid=this.cmbAccount.getValue(),
        s.baseParams.intercompanytypeid=this.cmbInterCompanyType.getValue(),
        s.baseParams.intercompanyflag=this.intercompanyFlag?true:false
       
        if(this.pP.combo!=undefined){
            if(this.pP.combo.value=="All"){
                var count = this.LedgerStore.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                s.paramNames.limit = count;
            }
        }        
        WtfGlobal.setAjaxTimeOut();
    }, this);
    this.LedgerStore.on("loadexception", function(store) {
        WtfGlobal.resetAjaxTimeOut();
    }, this);
    this.LedgerStore.on("load", function(store,rec,option){
        WtfGlobal.resetAjaxTimeOut();
        if(store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();

            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{

            //    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermTypee)){
            if(this.expButton)this.expButton.enable();//}
            // if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
            if(this.printButton)this.printButton.enable();//}
        }
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
    },this);
    
     this.MSComboconfig = {
                store: this.custStore,
                valueField:'accountid',
                hideLabel:true,
                displayField:'accountname',
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,
                triggerAction:'all',
                scope:this
            };
             this.cmbAccount = new Wtf.common.Select(Wtf.applyIf({
               name:'accountid',
               multiSelect:true,
               emptyText:WtfGlobal.getLocaleText("acc.field.SelectAccount"),
               fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',
               forceSelection:true,         
               extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
               extraComparisionField:'acccode',// type ahead search on acccode as well.
               listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
               width:240
             },this.MSComboconfig));      
 
    this.cmbAccount.on('select',function(combo,custRec,index){ //multiselection in case of all 
            if(custRec.get('accountid')=='All'){  //case of multiple record after all
                combo.clearValue();
                combo.setValue('All');
            }else 
                if(combo.getValue().indexOf('All')>=0){  // case of all after record
                combo.clearValue();
                combo.setValue(custRec.get('accountid'));
            }
        } , this);       
        
    this.interCompanyTypeRec = Wtf.data.Record.create ([
                {name:'id'},
                {name:'name'}
    ]);

    this.InterCompanyTypeStore=new Wtf.data.Store({
        url:"ACCMaster/getMasterItems.do",
        baseParams:{
            mode:112,
            groupid:14,
            common:'1'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.interCompanyTypeRec)
    });
           
    this.cmbInterCompanyType=new Wtf.form.ExtFnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',
        //                id:'accountIdForCombo'+config.id ,
        name:'intercompanytypeid',
        store:this.InterCompanyTypeStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        width:150,
        listWidth:400,
        hiddenName:'intercompanytypeid',
        emptyText:WtfGlobal.getLocaleText("acc.field.SelectCompanyType"),
        allowBlank:false,
        forceSelection:true,
        extraFields:[],
        triggerAction:'all'
    });
      this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
           
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        value:this.getDates(true)
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
     //   readOnly:true,
        name:'enddate',
        value:this.getDates(false)
    });
    /*
     * Provided button to expand or collapse all row details. 
     * We display Date,Account Name,Journel Folio,Memeo,Tran.ID,Description,
     * Debit Amount,Debit Amount in Base Curreny
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
    
    if(config.stDate!="" && config.stDate!=undefined){
        this.startDate.setValue(config.stDate);
    }
    if(config.enDate!="" && config.enDate!=undefined){
        this.endDate.setValue(config.enDate);
    }
           
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: config.moduleid,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        //        id: 'advanced3', // In use, Do not delete
        scope: this,
        //        hidden:(this.moduleid==undefined)?true:false,       
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
           
    var btnArr=[];
    if(config.intercompanyFlag) {
        btnArr.push(WtfGlobal.getLocaleText("acc.cust.intercompanytype"),this.cmbInterCompanyType);
    }
    btnArr.push(
        WtfGlobal.getLocaleText("acc.ledger.accName"),this.cmbAccount, 
        WtfGlobal.getLocaleText("acc.common.from"),this.startDate,
        WtfGlobal.getLocaleText("acc.common.to"),this.endDate,
        '-',{
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip:WtfGlobal.getLocaleText("acc.bankReconcile.fetchTT"),  //"Select a time period to view corresponding ledger records.",
            iconCls:'accountingbase fetch',
            scope:this,
            handler:this.onClick
            },'-',this.resetBttn, '-', this.expandCollpseButton           //,'-',this.AdvanceSearchBtn
        );
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push("-",this.expButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.export"),
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),
            filename:config.title+"_v1", //ERP-17330
            disabled :true,
                params:{stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                accountid:this.accountID||config.accountID,
                name: "Ledger"
            },
            label:this.isCustomer?"Customer Ledger":"Vendor Ledger",
                    menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:this.isCustomer?Wtf.autoNum.CustomerAccountLedger:Wtf.autoNum.VendorAccountLedger
        }));
    }

    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        btnArr.push("-",this.printButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
            filename:config.title,
            disabled :true,
                    params:{stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                accountid:this.accountID||config.accountID,
                name: WtfGlobal.getLocaleText("acc.ledger.tabTitle")
            },
            label:this.isCustomer?"Customer Ledger":"Vendor Ledger",   //WtfGlobal.getLocaleText("acc.ledger.tabTitle"),
                    menuItem:{print:true},
            get:this.isCustomer?Wtf.autoNum.CustomerAccountLedger:Wtf.autoNum.VendorAccountLedger
        }));
    }   
    Wtf.apply(this,{
        items:[{
            layout:'border',
            border:false,
            scope:this,
            items:[this.objsearchComponent,{
                region:'center',
                layout:'fit',
                border:false,
                items:this.grid
            }],
            tbar:btnArr,
            bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.LedgerStore,                 
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
            })
        }]
    },config)

    Wtf.account.Ledger.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
    this.grid.on('cellclick',this.onCellClick, this);
    this.custStore.on('load',function(){this.showCustomerVendorLedger(config.accountID,this.startDate.getValue(),this.endDate.getValue());},this);
    this.custStore.on("load", function(store){
        var storeNewRecord=new this.custRec({
            accountname:'All',
            accountid:'All'
        });
        this.cmbAccount.store.insert( 0,storeNewRecord);
        if(config.intercompanyFlag){
            this.cmbAccount.setValue("All");
            if(this.tabOpenFlag){
                this.tabOpenFlag = false;
                this.onClick();
            }
        }
    },this);

   
    if(config.intercompanyFlag) {       
        this.InterCompanyTypeStore.on("load", function(store){
            var storeNewRecord=new this.custRec({
                name:'All',
                id:'All'
            });
            this.cmbInterCompanyType.store.insert( 0,storeNewRecord);
            this.cmbInterCompanyType.setValue("All");           
            this.custStore.load({params:{
                    intercompanytypeid:"All",
                    intercompanyflag:true
            }});
        },this);
       
    this.InterCompanyTypeStore.load();
       
    this.cmbInterCompanyType.on("select", function() {           
        this.LedgerStore.removeAll();
        this.grid.getView().emptyText=WtfGlobal.getLocaleText("acc.field.ClickonFetchbuttontoviewrecords");
        this.grid.getView().refresh();
            this.custStore.load({params:{
                intercompanytypeid:this.cmbInterCompanyType.getValue(),
                intercompanyflag:true
            }});
    }, this);
this.cmbAccount.on("select", function() {
    this.LedgerStore.removeAll();
    this.grid.getView().emptyText=WtfGlobal.getLocaleText("acc.field.ClickonFetchbuttontoviewrecords");
    this.grid.getView().refresh();
}, this);
} else {
        this.custStore.load();
}

this.LedgerStore.load({
    params:{
        start:0,
        limit: (this.pP.combo!=undefined) ? this.pP.combo.value:30,
        accountid:this.cmbAccount.getValue(),
        stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
        enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
        intercompanytypeid:this.cmbInterCompanyType.getValue(),
        intercompanyflag:this.intercompanyFlag?true:false
    }
});

}

Wtf.extend( Wtf.account.CustomerVendorLedger,Wtf.Panel,{
    formatAccountName:function(val,m,rec){
         if(val=="Total"){return "<b>"+val+"</b>";}
         else{return val}
    },
    onClick:function(){
        this.accountID=this.cmbAccount.getValue();
        this.fetchLedger();
        this.custStore.findBy( function(rec){
            if(rec.data['accountid'] == this.accountID) {
                if(rec.data['currencyid'] == Wtf.pref.Currencyid) {
                    this.gridcm.setHidden(this.gridcm.getIndexById("d_amountAccountCurrency"), true);
                    this.gridcm.setHidden(this.gridcm.getIndexById("c_amountAccountCurrency"), true);
                } else {
                    this.gridcm.setHidden(this.gridcm.getIndexById("d_amountAccountCurrency"), false);
                    this.gridcm.setHidden(this.gridcm.getIndexById("c_amountAccountCurrency"), false);
                }
            }
        },this)
    },
    fetchLedger:function(){
            WtfGlobal.setAjaxTimeOut();
            var sDate=this.startDate.getValue();
            var eDate=this.endDate.getValue();
       
            if(sDate>eDate){
                WtfComMsgBox(1,2);
                return;
            }
            if(this.accountID&&this.accountID.length>0){
                this.LedgerStore.load({
                    params:{
                        start:0,        //    ERP-17303
                        limit: (this.pP.combo!=undefined) ? this.pP.combo.value:30,
                        accountid:this.accountID,
                        stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                        enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                        searchJson: this.searchJson,
                        filterConjuctionCriteria: this.filterConjuctionCrit,
                        intercompanytypeid:this.cmbInterCompanyType.getValue(),
                        intercompanyflag:this.intercompanyFlag?true:false
                    }
                });
                if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
                    this.expButton.setParams({
                        accountid:this.accountID,
                        stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                        enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                        name: WtfGlobal.getLocaleText("acc.ledger.tabTitle")+"-"+this.cmbAccount.getRawValue()
                    });
                }
                if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                    this.printButton.setParams({
                        accountid:this.accountID,                   
                        stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                        enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                        name: WtfGlobal.getLocaleText("acc.ledger.tabTitle")+"-"+this.cmbAccount.getRawValue()
                    });
                }
            }
    },

    showLedger:function(accid,startDate,endDate){

     var i=this.custStore.find("accountid",accid);
        if(i>=0){
            this.cmbAccount.setValue(accid);
            if(startDate!="" && startDate!=undefined){
                this.startDate.setValue(startDate);
            }
            if(endDate!="" && endDate!=undefined){
                this.endDate.setValue(endDate);
            }
            if(accid!='None'){   
                this.accountID=accid;
                this.onClick();
            }
        }
    },

    onRender:function(config){
        Wtf.account.CustomerVendorLedger.superclass.onRender.call(this,config);
    },

    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var rec=this.LedgerStore.getAt(i);
        var jid=rec.data['d_journalentryid'];
        if(!jid||jid.length<=0)
            jid=rec.data['c_journalentryid'];
        this.fireEvent('journalentry',jid,true,this.consolidateFlag);
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="c_entryno" || header=="d_entryno"){
            var rec=this.LedgerStore.getAt(i);
            var jid=rec.data['d_journalentryid'];
            if(!jid||jid.length<=0)
                jid=rec.data['c_journalentryid'];
            this.fireEvent('journalentry',jid,true,this.consolidateFlag);
        } else if(header=="d_transactionID"){
            var formrec = this.LedgerStore.getAt(i);
            var type=formrec.data['d_type'];
            viewTransactionTemplate(type, formrec);
        }else if(header=="c_transactionID"){
            var formrec = this.LedgerStore.getAt(i);
            var type=formrec.data['c_type'];
            viewTransactionTemplate(type, formrec);
        }
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

    dateRenderer:function(v){
        if(v) return v.format('Y M d');
        return "";
    },

    showSeperator:function(){
        return '<div style="margin:-5px"><img src="../../images/header.gif"></div>';
    },

    accountCurrencyRenderer:function(val,m,rec){ //added forigin currency symbol for  Amount in Account Currency
        if (!(val==="")) {
            var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);        
            var v=parseFloat(val);
            if(isNaN(v)) return val;
            if(rec.data.deleted)
                v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
            else
                    v=WtfGlobal.conventInDecimal(v,symbol);

            return '<div class="currency">'+v+'</div>';
        } else {
            return "";
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
        this.fetchLedger();
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.fetchLedger();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },   
    storeLoad: function() {
        this.fetchLedger();
        this.doLayout();


    } ,  
    handleResetClick:function(){
        this.cmbAccount.reset(); 
        this.endDate.reset();
        this.cmbAccount.reset();  
        this.startDate.reset();        
        this.LedgerStore.removeAll();
        this.LedgerStore.load();//    ERP-17303
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