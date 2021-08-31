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
function UnpaidInvoicesDynamicLoad() {
    Wtf.Ajax.requestEx({
        url: "ACCAccountCMN/getFieldParams.do",
        params: {
            moduleid: Wtf.Acc_Invoice_ModuleId,
            iscustomfield: 1,
            isActivated: 1
        }
    }, this,
            function (responseObj) {
                if (responseObj.success == true) {
                    this.UnpaidInvoicesListPanel(responseObj.data);
                }
            },
            function () {

            }
    );
}
function UnpaidInvoicesListPanel(customCol) {

    var panel = Wtf.getCmp("UnpaidInvoicesList");
    if (panel == null) {
        panel = new Wtf.account.UnpaidInvoicesListPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.invoiceList.unpaidInvoices"), Wtf.TAB_TITLE_LENGTH), //"Finance Details"
            tabTip: WtfGlobal.getLocaleText("acc.field.YoucanviewUnpaidInvoicesListfromhere"),
            id: 'UnpaidInvoicesList',
            border: false,
            customColArr: customCol,
            moduleId: Wtf.Acc_Invoice_ModuleId,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            closable: true

        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}


function FinanceDetailsReportDynamicLoad() {
    Wtf.Ajax.requestEx({
        url: "ACCAccountCMN/getFieldParams.do",
        params: {
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            iscustomfield: 1,
            isActivated: 1
        }
    }, this,
            function (responseObj) {
                if (responseObj.success == true) {
                    this.FinanceDetailsReportPanel(responseObj.data);
                }
            },
            function () {

            }
    );
}
function FinanceDetailsReportPanel(customCol){

    var panel = Wtf.getCmp("financeDetailsReport");
    if(panel==null){
        panel = new Wtf.account.FinanceReportPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.je.financeReport"),Wtf.TAB_TITLE_LENGTH) ,//"Finance Details"
            tabTip:WtfGlobal.getLocaleText("acc.field.YoucanviewFinanceDetailsReportfromhere"),
            id:'financeDetailsReport',
            border:false,
            customColArr :customCol,
            moduleId:Wtf.Acc_GENERAL_LEDGER_ModuleId,
            financeDetailsReport:true,
            //label:WtfGlobal.getLocaleText("acc.je.financeReport"),
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            closable: true

        });
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}

//******************************************************************************************************

Wtf.account.FinanceReportPanel=function(config){
    Wtf.apply(this, config);
    this.moduleid=config.moduleId;
    this.customColArr=config.customColArr,
     Wtf.grid.GroupSummary.Calculations['last'] = function(v, record, colName){
        var result = 0;
        var val = record.data[colName];
        var count = record.store.getCount();
        var rowIdx = record.store.find("referid",record.data.referid);

        if(rowIdx == (count-1)){
            result = val;
        }else{
            var nextRec = record.store.getAt(rowIdx + 1);
            if(record.data["currencyname"] != nextRec.data["currencyname"])
                result = val;
        }

        return result;
    };

    Wtf.grid.GroupSummary.Calculations['amount'] = function(v, record, colName){
            return v + record.data[colName];
    };
    Wtf.grid.GroupSummary.Calculations['amountinbase'] = function(v, record, colName){
            return v + record.data[colName];
    };

    this.GridRec = Wtf.data.Record.create ([
    {
        name:'journalentryid'
    },{
        name:'entryno'
    },{
        name:'companyname'
    },{
        name:'companyid'
    },{
        name:'remitto'
    },{
        name:'deleted'
    },{
        name:'entrydate',
        type:'date'
    },{
        name:'amount'
    },{
        name:'amountinbase'
    },{
        name:'referid'
    },{
        name:'jeDetails'
    },{
        name:'currencyid'
    },{
        name:'currencysymbol'
    },{
        name:'currencyname'
    },{
        name:'accountname'
    },{
        name:'isreverseje',
        type:'boolean'
    },{
        name:'reversejeno'
    }
    ]);
    
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

     this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'acccode'},
            {name:'groupname'}
//            {name:'level',type:'int'}
        ]);
        var baseparam = {
            mode:2,
            deleted:false,
            nondeleted:true,
            ignoreAssets:true,
            ignoreCashAccounts:true,
            ignoreGSTAccounts:true,  
           ignorecustomers:true,  
           ignorevendors:true
        };
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:baseparam,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
    this.MSComboconfig = {
        store: this.accountStore,
        valueField:'accountid',
        hideLabel:true,
        displayField:'accountname',
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
    this.Account = new Wtf.common.Select(Wtf.applyIf({
    name:'accountid',
    multiSelect:true,
    emptyText:WtfGlobal.getLocaleText("acc.field.SelectAccount"),
    fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',
    forceSelection:true,         
    extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
    extraComparisionField:'acccode',// type ahead search on acccode as well.
    listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
    width:200
    },this.MSComboconfig));      

    this.Account.on('select',function(combo,accRec,index){ //multiselection in case of all 
    if(accRec.get('accountid')=='All'){  //case of multiple record after all
        combo.clearValue();
        combo.setValue('All');
    }else 
        if(combo.getValue().indexOf('All')>=0){  // case of all after record
        combo.clearValue();
        combo.setValue(accRec.get('accountid'));
    }
    } , this);       
    this.accountStore.load();
      this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid', mapping: 'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname', mapping: 'tocurrency'},
            {name: 'exchangerate'},
            {name: 'currencycode'},
            {name: 'htmlcode'}
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.currencyRec),
//        url:Wtf.req.account+'CompanyManager.jsp'
            url: "ACCCurrency/getCurrencyExchange.do"
        });
        this.Currency = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.coa.gridCurrency"), //'Currency',
            hiddenName: 'currencyid',
            width: 200,
            allowBlank: false,
            store: this.currencyStore,
            valueField: 'currencyid',
            emptyText: WtfGlobal.getLocaleText("acc.cust.currencyTT"), //'Please select Currency...',
            forceSelection: true,
            displayField: 'currencyname',
            scope: this,
            selectOnFocus: true

        });
    this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(new Date()), isAll: true}});
     this.currencyStore.on("load", function() {
        var record = new Wtf.data.Record({
            currencyid: "",
            currencyname: "All Records"
        });
        this.currencyStore.insert(0, record);
        this.Currency.setValue("");
    }, this);
     this.accountStore.on("load", function() {
        var record1 = new Wtf.data.Record({
            accountid: "All",
            accountname: "All"
        });
       this.accountStore.insert(0, record1);
        this.Account.setValue("All");
    }, this);
    this.Store = new Wtf.ux.grid.MultiGroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            //totalProperty:'count',
            root: "data"
        },this.GridRec),
        url: "ACCOtherReports/getJournalEntryForFinance.do",
        baseParams:{
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            currencyid:this.Currency.getValue(),
            accountid:this.Account.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            mode:917
               
        },
        sortInfo : {
            field : 'accountname',
            direction : 'ASC'
        },
        groupField : ['accountname','currencyname']
    });
    this.Store.load({
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            currencyid:this.Currency.getValue(),
            accountid:this.Account.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            mode:917
               
        });
    WtfGlobal.updateStoreConfig(this.customColArr,this.Store);   // updated store for custom fileds
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbar" + this.id,
        store: this.Store,
        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
    });
        
    this.Store.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
      
    this.Store.on('load',function(){
        this.exportButton.enable();
        this.grid.getView().refresh(true);
    },this);  
      
    this.Store.on('beforeload', function(){
        this.Store.baseParams = {
            ss : this.quickPanelSearch.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            currencyid:this.Currency.getValue(),
            accountid:this.Account.getValue()
        }
       
    }, this);
   
    this.summary = new Wtf.grid.GroupSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    //this.sm = new Wtf.grid.CheckboxSelectionModel();
   
    var columnArr =[];   
    columnArr.push(this.rowNo,{          //this.sm,
        header: WtfGlobal.getLocaleText("acc.setupWizard.BankNam"),  //"Account Name",
        dataIndex: 'accountname',
        autoWidth : true,
        sortable: true,
        width:150,
        groupable: true,
       // renderer:WtfGlobal.rendererForAccountNameWithTax,
        pdfwidth:120
    },{
        header:WtfGlobal.getLocaleText("acc.je.trNumber"),  //"JE Number",
        dataIndex:'entryno',
        width:150,
        //hidden:true,
        pdfwidth:80,
        renderer:WtfGlobal.linkDeletedRenderer,
        sortable:true
        // groupRenderer: function(v){return v},
         
    },{ 
        header:WtfGlobal.getLocaleText("acc.gridFiananceDate"),  //"Entry Date",
        dataIndex:'entrydate',
        width:150,
        align:'center',
        pdfwidth:80,
        //sortable:true,
        summaryRenderer:function(){
            return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            },
        renderer:WtfGlobal.onlyDateDeletedRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.gridFiananceRemitto"),  //"REMIT To",
        dataIndex: 'remitto',
        autoWidth : true,
        sortable: true,
        width:150,
        //renderer:WtfGlobal.rendererForAccountNameWithTax,
        pdfwidth:120
    });         
    columnArr = WtfGlobal.appendCustomColumn(columnArr,this.customColArr,true);
    columnArr.push({
            header :WtfGlobal.getLocaleText("acc.coa.gridCurrency"), //'Currency',
            //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            groupable: true,
            width:150,
            pdfwidth:100,
            dataIndex:'currencyname'
        },{
        header:WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
        dataIndex:'amount',
        align:'right',
        width:150,
        pdfwidth:100,
        summaryType:'sum',
          summaryRenderer: function(value, m, rec) {
            if (value != 0) {
                var retVal = WtfGlobal.withoutRateCurrencyDeletedSymbol(value, m, rec)
                return retVal;
            } else {
                return '';
            }
        },
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
        header:WtfGlobal.getLocaleText("acc.field.AmountInBase")+ " ("+WtfGlobal.getCurrencyName()+")",
        dataIndex:'amountinbase',
        align:'right',
        width:150,
        pdfwidth:100,
        summaryType: 'sum',
        renderer:WtfGlobal.currencyRenderer,
        hidecurrency : true,
        summaryRenderer: function(value, m, rec) {
            if (value != 0) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            } else {
                return '';
            }
        }
            
    });  
    
    var groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit: false,
        showGroupName: true,
        enableNoGroups: true,
        isGrandTotal:false,
        isGroupTotal:false,
        hideGroupedColumn: false,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
        groupTextTpl: '{group} '
    });
    
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        //        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
        label:WtfGlobal.getLocaleText("acc.je.financeReport"),
        menuItem:{
            print:true
        },
        get:917,
        params:{
            name:"Finance Details Report",
            stdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
                    

        }
    });
    this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        filename:"Finance Details Report _v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
            name:"Finance Details Report",
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            isfinancereport : true
        },
        get:917
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClickNew,this);
    /*
     * Provided button to expand or collapse all row details. 
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
    
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid =  new Wtf.ux.grid.MultiGroupingGrid({
       // stripeRows :true,
        store:this.Store,
        border:false,
//       viewConfig: this.gridView1,
       view: groupView,
       tbar:[WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"), this.Currency, '-',{
            text : WtfGlobal.getLocaleText("acc.ra.fetch"),
            iconCls:'accountingbase fetch',
            scope : this,
            handler : this.loaddata
            }, '-', this.resetBttn, this.AdvanceSearchBtn, this.exportButton, this.printButton, '-', this.expandCollpseButton],
        //   forceFit:true,
        plugins: [gridSummary],
        //loadMask : true,  
        cm:new Wtf.grid.ColumnModel(columnArr),
        listeners: {
            "reloadexternalgrid": function() {
                if (!this.searchparam)
                    this.loaddata.defer(10, this);
                else
                    this.showAdvanceSearch.defer(10, this);
            },
            scope: this
        }
    });
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleid,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
     this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+WtfGlobal.getLocaleText("acc.field.JENumber"),
        width: 150,
        //      id:"quickSearch"+config.helpmodeid+config.id,
        field: 'billno',
        Store:this.Store
    });
    this.tbar1 = [];
    this.tbar1.push(this.quickPanelSearch); 
    this.tbar1.push(WtfGlobal.getLocaleText("acc.common.from"));this.tbar1.push(this.startDate);
    this.tbar1.push(WtfGlobal.getLocaleText("acc.common.to"));this.tbar1.push(this.endDate);this.tbar1.push('-');
    this.tbar1.push(WtfGlobal.getLocaleText("acc.je.acc"));this.tbar1.push(this.Account);this.tbar1.push('-');
    
    this.leadpan = new Wtf.Panel({
    layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items:[this.objsearchComponent,
            {
            region:'center',
            layout:'fit',
            border:false,
            tbar : this.tbar1,
            items:[this.grid],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({
             id : "pPageSize_"+this.id
            })
        })
        }]
       
    });
     Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });  
    
    Wtf.account.FinanceReportPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
     });
     this.grid.on('cellclick',this.onCellClick, this);
}
Wtf.extend(Wtf.account.FinanceReportPanel,Wtf.Panel,{
  
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    loaddata : function(){
     
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
        this.Store.load({
            params : {
                start:0,
               limit:(this.pP.combo!=undefined) ? this.pP.combo.value : 30
                            
            }
        });
        this.exportButton.params.stdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
        this.exportButton.params.ss=this.quickPanelSearch.getValue();
        this.exportButton.params.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
        this.exportButton.params.enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
        this.exportButton.params.currencyid=this.Currency.getValue();
        this.exportButton.params.accountid=this.Account.getValue();
        this.exportButton.enable();
    },
    handleResetClickNew:function(){ 

        this.quickPanelSearch.reset();
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));

        this.Store.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
       
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
                var accid=this.Store.getAt(i).data['journalentryid'];
                this.fireEvent('journalentry',accid,true,this.consolidateFlag);
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

        this.Store.load({
            params: {
                flag: 1,
                searchJson: this.searchJson,
                moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
                filterConjuctionCriteria: filterConjuctionCriteria,
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.load({
            params: {
                flag: 1,
                searchJson: this.searchJson,
                moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
                filterConjuctionCriteria: this.filterConjuctionCrit,
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
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

//           ****************************************************************
//                                 Unpaid Invoices List
//           ****************************************************************

Wtf.account.UnpaidInvoicesListPanel=function(config){
    Wtf.apply(this, config);
    this.moduleid=config.moduleId;
    this.customColArr=config.customColArr,
    
    this.invoiceTypeStore = new Wtf.data.SimpleStore({
        fields:[{
            name:'name'
        },{
            name:'value'
        }],
        data:[[WtfGlobal.getLocaleText("acc.invoice.normalInvoice"),1],[WtfGlobal.getLocaleText("acc.invoice.openingInvoice"),2]] //DATA CONTAINS PAYMENT METHOD TYPES. 
    });
    
    this.invoiceType=new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.invoice.type"),  //'Invoice Type',            
        name:'paymenttype',
        store:this.invoiceTypeStore,
        valueField:'value',
        displayField:'name',
        mode: 'local',
        width:150,
        hiddenName:'invoicetype',
        allowBlank:false,
        value:1,          // dfault is Normal Invoice
        forceSelection:true,
        triggerAction:'all'
    });
    this.invoiceType.on('select',this.onSelectInvoiceType,this);
    
   this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'entryno'},
        {name:'companyid'},
        {name:'currencysymbol'},
        {name:'currencyid'},
        {name:'billno'},
        {name:'personname'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'amount'},
        {name:'amountinbase'},
        {name:'amountdueinbase'},
        {name:'amountdue'},
        {name:'externalcurrencyrate'},
        {name:'withoutinventory',type:'boolean'},
    ]);
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
     this.StoreUrl =  "ACCInvoiceCMN/getInvoicesMerged.do" ;//this.businessPerson=="Customer" ?: "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    
    this.Store = new Wtf.data.GroupingStore({
        url:this.StoreUrl,
        baseParams:{
            deleted:false,
            nondeleted:true,
            cashonly:false,
            creditonly:false,
            onlyOutsatnding:true,
            isUnpaidInvoiceReport:true,
            report:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
        },
        sortInfo : {
            field : 'personname',
            direction : 'ASC'
        },
        groupField : 'personname',
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
  
    WtfGlobal.updateStoreConfigStringDate(this.customColArr,this.Store);   // updated store for custom fileds
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbar" + this.id,
        store: this.Store,
        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
    });
        
    this.Store.on('datachanged', function() {
         var p =  this.pP.combo!=undefined?this.pP.combo.value:30;
        this.quickPanelSearch.setPage(p);
        this.grid.getView().refresh();
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
    }, this);
      
    this.Store.on('load',function(){
        this.exportButton.enable()
        this.grid.getView().refresh();
       // this.quickPanelSearch.StorageChanged(this.Store);
    },this);  
      
    this.Store.on('beforeload', function(){
        var startDate="",endDate="",isOpening="";
        var invoiceType=this.invoiceType.getValue();
        
        if(invoiceType==1){ //date only for normal invoices not for opening invoices
            startDate=  WtfGlobal.convertToGenericDate(this.startDate.getValue());
            endDate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            isOpening=false;
        }else if(invoiceType==2){           
            isOpening=true;
        }
        
        this.Store.baseParams = {
            deleted:false,
            nondeleted:true,
            cashonly:false,
            creditonly:false,
            onlyOutsatnding:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            startdate : startDate,
            enddate : endDate,
            isOpeningBalanceInvoices:isOpening,
            isUnpaidInvoiceReport:true,
            report:true
        }
        this.exportButton.setParams({
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
        });
        
    }, this);
    
    this.summary = new Wtf.grid.GroupSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.gridView1 = new Wtf.grid.GroupingView({
        forceFit:false,
        startCollapsed :true,
        showGroupName: true,
        enableNoGroups:true, // REQUIRED!
        hideGroupedColumn: false,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });
    var columnArr =[];   
    columnArr.push(this.sm,this.rowNo,{
        header:WtfGlobal.getLocaleText("acc.field.Invoice")+WtfGlobal.getLocaleText("acc.cn.9"),
        dataIndex:'billno',
        width:150,
        pdfwidth:70
       // renderer:(config.isQuotation||config.isOrder||config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.cust"),       //(config.isCustomer? :WtfGlobal.getLocaleText("acc.invoiceList.ven")),  //this.businessPerson,
        pdfwidth:75,
        width:150,
        renderer:WtfGlobal.deletedRenderer,
        dataIndex:'personname',
        sortable:true
    });         
    columnArr = WtfGlobal.appendCustomColumn(columnArr,this.customColArr,true);
    columnArr.push({ 
            header:WtfGlobal.getLocaleText("acc.rem.34"),  //"Invoice  Date
            dataIndex:'date',
            width:150,
            align:'center',
            pdfwidth:80,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.due"),  //"Due Date",
            dataIndex:'duedate',
            align:'center',
            width:150,
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
              summaryRenderer:function(){
            return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),   //+ " ("+WtfGlobal.getCurrencyName()+")",  //"Total Amount (In Home Currency)",
            align:'right',
            dataIndex:'amount',
            width:150,
            pdfwidth:80,
            //hidecurrency : true,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.mp.amtDue"),  //"Amount Due",
            dataIndex:'amountdue',
            align:'right',
            width:150,
            pdfwidth:70,
            //pdfrenderer : "rowcurrency",
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol            //currencyRendererDeletedSymbol
                 
        },{
        header:WtfGlobal.getLocaleText("acc.agedPay.gridAmtDueHomeCurrency")+ " ("+WtfGlobal.getCurrencyName()+")",        //"Amount in Base Currency",
        dataIndex:'amountdueinbase',
        align:'right',
        width:150,
        pdfwidth:80,
        //pdfrenderer : "rowcurrency",
        summaryType:'sum',
        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer, 
        summaryRenderer: function(value, m, rec) {
            if (value != 0) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            } else {
                return '';
            }
        }
            
    });  
        
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid = new Wtf.grid.GridPanel({
        //id:"gridmsg"+this.id,
        stripeRows :true,
        store:this.Store,
        //tbar : this.tbar2,
        sm:this.sm,
        border:false,
        viewConfig: this.gridView1,
        forceFit:false,
        layout:'fit',
        plugins: [gridSummary],
        loadMask : true,  
        cm:new Wtf.grid.ColumnModel(columnArr),
        listeners: {
            "reloadexternalgrid": function() {
                if (!this.searchparam)
                    this.loaddata.defer(10, this);
                else
                    this.showAdvanceSearch.defer(10, this);
            },
            scope: this
        }
    });
     this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleid,
        advSearch: false
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
    this.resetBttn.on('click',this.handleResetClickNew,this);
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" Invoice",
        width: 150,
        //      id:"quickSearch"+config.helpmodeid+config.id,
        field: 'billno',
        Store:this.Store
    })
    this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        filename:WtfGlobal.getLocaleText("acc.je.financeReport")+"_v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
            name:WtfGlobal.getLocaleText("acc.invoiceList.unpaidInvoices")+"_v1",
            isUnpaidInvoiceReport:true,
            isOpeningBalanceInvoices:false,
            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue())

        },
        get:Wtf.autoNum.Invoice
    });
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
        label:WtfGlobal.getLocaleText("acc.invoiceList.unpaidInvoices"),
        filename:WtfGlobal.getLocaleText("acc.invoiceList.unpaidInvoices"),
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.Invoice,
        params:{
            name:"Unpaid Invoices List",
            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                    

        }
    });
      this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    
    var buttonArray = new Array();
    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"), this.endDate);   
     buttonArray.push('-',this.invoiceType);
   buttonArray.push('-', {
        text : WtfGlobal.getLocaleText("acc.ra.fetch"),
        iconCls:'accountingbase fetch',
        scope : this,
        handler : this.loaddata
    });
    buttonArray.push('-', this.resetBttn, '-', this.AdvanceSearchBtn, '-', this.exportButton, '-', this.printButton, '-', this.expandCollpseButton);
    
this.leadpan = new Wtf.Panel({
    layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items:[this.objsearchComponent,
            {
            region:'center',
            layout:'fit',
            border:false,
            items:[this.grid],
            tbar : buttonArray,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({
             id : "pPageSize_"+this.id
            })
        })
        }]
       
    });
     Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });   
    Wtf.account.UnpaidInvoicesListPanel.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.UnpaidInvoicesListPanel,Wtf.Panel,{
  
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    onRender:function(config){
        Wtf.account.UnpaidInvoicesListPanel.superclass.onRender.call(this,config);
        this.loaddata();
    },
    loaddata : function(){
     
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
        this.exportButton.enable();
    },
    
    onSelectInvoiceType:function(combo){
        if(combo.getValue()==1){//normal invoices
            this.startDate.setValue(WtfGlobal.getDates(true));
            this.startDate.enable();
            this.endDate.setValue(WtfGlobal.getDates(false));
            this.endDate.enable();
            this.exportButton.params.isOpeningBalanceInvoices = false;
        }else if(combo.getValue()==2){//opening invoices
            this.startDate.setValue("");
            this.startDate.disable();
            this.endDate.setValue("");
            this.endDate.disable();
            this.exportButton.params.isOpeningBalanceInvoices = true;
        }
       this.loaddata();            
    },
    handleResetClickNew:function(){ 

        this.quickPanelSearch.reset();
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));

        this.Store.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
       
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

        this.Store.load({
            params: {
                flag: 1,
                searchJson: this.searchJson,
                moduleid: this.moduleId,
                filterConjuctionCriteria: filterConjuctionCriteria,
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.load({
            params: {
                flag: 1,
                searchJson: this.searchJson,
                moduleid: this.moduleId,
                filterConjuctionCriteria: this.filterConjuctionCrit,
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    }
});
