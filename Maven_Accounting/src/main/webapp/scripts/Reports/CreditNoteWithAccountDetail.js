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

function callCreditNoteAccountDetailReportDynamicLoad(searchStr, filterAppend) {
    var panel = Wtf.getCmp("creditNoteAccountDetailReport");
    if (panel == null) {
        panel = new Wtf.account.creditNoteAccountDetailReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.creditNoteAccountDetailReport"), Wtf.TAB_TITLE_LENGTH), // "Sales by Service Product Detail Report",
            tabTip: WtfGlobal.getLocaleText("acc.creditNoteAccountDetailReport.toolTip"),
            id: "creditNoteAccountDetailReport",
            iconCls: 'accountingbase invoicelist',
            layout: 'fit',
            closable: true,
            moduleid:Wtf.Acc_Credit_Note_ModuleId,
            isCNReport:true,
            border: false
        });
        Wtf.getCmp('as').add(panel);
    }
    panel.on('journalentry',callJournalEntryDetails);
    Wtf.getCmp('as').setActiveTab(panel);
    showAdvanceSearch(panel, searchStr, filterAppend);
    Wtf.getCmp('as').doLayout();
}
//**************************************************************************************

Wtf.account.creditNoteAccountDetailReport = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    this.moduleid= config.moduleid;
    this.isCNReport= config.isCNReport;
    this.createGrid();
    this.createTBar();
    this.costCenterId = "";
    Wtf.account.creditNoteAccountDetailReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.creditNoteAccountDetailReport, Wtf.Panel, {
    onRender: function(config) {
        this.getMyConfig();
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent, 
            {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.gridtbarPanel],
                tbar: this.btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    }),
                    items: this.bBarBtnArr
                })
            }]
        });
        
        this.add(this.leadpan);
        this.fetchStatement();
        
        Wtf.account.creditNoteAccountDetailReport.superclass.onRender.call(this,config);
        this.addEvents({
            'journalentry':true
        });
    },
    
    createTBar: function(config) {
        this.btnArr = [];
        this.btnArr2 = [];
        this.bBarBtnArr = [];
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.cnList.search"), 
            width: 200,
            hidden: false,
            field: 'noteno'
        });
       
        
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: false,
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
         
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });
       
        
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });
        
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"),  
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement                        
        });
        
        /*For the credit note with account amount acolumns */
        this.accRec = Wtf.data.Record.create ([
        {
            name:'accountname',
            mapping:'accname'
        },

        {
            name:'accountid',
            mapping:'accid'
        },

        {
            name:'currencyid',
            mapping:'currencyid'
        },

        {
            name:'acccode'
        },

        {
            name:'groupname'
        }
        ]);
                
        this.accStore = new Wtf.data.Store({
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true,
                headerAdded:true,
                isForBS_PL_to_GL:this.isForBS_PL_to_GL
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.accStore.on('load',function(){
            if(this.isGeneralLedger){
                this.showGeneralLedger(this.accountID,this.startDate.getValue(),this.endDate.getValue());
            }
        },this);
        this.accStore.load();
                
        this.MSComboconfig = {
            hiddenName:'accountmulselectcombo',         
            store: this.accStore,
            valueField:'accountid',
            hideLabel:false,
            hidden : false,
            displayField:'accountname',
            emptyText:WtfGlobal.getLocaleText("acc.salescomission.SelectAccounttobedisplayedascolumn"),//WtfGlobal.getLocaleText("acc.rem.111"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };
        this.MultiSelectAccCombo = new Wtf.common.Select(Wtf.applyIf({
            id:'mulaccountcombo'+this.id,
            multiSelect:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectAccounts") ,
            forceSelection:true,  
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            width:250
        },this.MSComboconfig));
       
        
        
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        
        var dataArr = new Array();   
        if(this.isCNReport){
            dataArr.push([1,WtfGlobal.getLocaleText("acc.salescomission.CreditNoteforCustomers")],[4,WtfGlobal.getLocaleText("acc.salescomission.CreditNoteforVendors")],[10,WtfGlobal.getLocaleText("acc.salescomission.OpeningCreditNoteforCustomers")],[11,WtfGlobal.getLocaleText("acc.salescomission.OpeningCreditNoteforVendors")]);
        } 
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'typeid',
                type:'int'
            }, 'name'],
            data :dataArr
        });
        this.typeEditor = new Wtf.form.FnComboBox({
            store: this.typeStore,
            name:'typeid',
            displayField:'name',
            id:'view'+this.id,
            valueField:'typeid',
            mode: 'local',
            defaultValue:0,
            width:160,
            hidden:this.winValue!=undefined,
            hideLabel:this.winValue!=undefined,
            listWidth:160,
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true,
            value:1
        });
         
        
        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        });
        this.btnArr.push(this.quickPanelSearch);
        this.btnArr.push(this.resetBttn);
        this.btnArr.push('-', this.AdvanceSearchBtn);
        this.btnArr2.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.btnArr2.push(WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.cn.payType"));
        this.btnArr.push(this.typeEditor);
        this.btnArr.push('-', this.MultiSelectAccCombo);
        this.btnArr2.push('-', this.fetchBttn);  
        this.btnArr2.push('->', this.customReportViewBtn);
        this.gridtbarPanel = new Wtf.Panel({
            border: false,
            tbar: this.btnArr2,
            layout: 'border',
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items:[this.grid]
                }]
        });
        
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: this.moduleid,
            reportid:Wtf.autoNum.CreditNoteWithAccountDetail,
            advSearch: false,
            customerCustomFieldFlag: true,
            vendorCustomFieldFlag: false
        });
        
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        
      
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),  // 'Export report details',
            filename: WtfGlobal.getLocaleText("acc.field.creditNoteAccountDetailReport")+"_v1",
            disabled: true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.CreditNoteWithAccountDetail
        });
        this.bBarBtnArr.push('-', this.exportButton);
        
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        },this);
        
        this.printButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), // 'Print report details',
            disabled: true,
            filename: WtfGlobal.getLocaleText("acc.field.creditNoteAccountDetailReport"),
            menuItem: {
                print: true
            },
            get: Wtf.autoNum.CreditNoteWithAccountDetail
        });
        this.bBarBtnArr.push('-', this.printButton);
        
        this.printButton.on("click", function() {
            this.printButton.setParams({
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        },this);
    },
    
    createGrid: function(config) {
         
        this.Store = new Wtf.data.Store({
            url: "ACCCreditNote/getCreditNoteWithAccountMerged.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
            
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.grid.on('cellclick',this.onCellClick, this);
        this.grid.on('render', function () {
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.grid.on('statesave', this.saveGridStateHandler, this);
            }, this);
        }, this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('datachanged', this.handleStoreDataChanged, this);
         
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    fetchStatement: function() {
        this.sDate = this.startDate.getValue();
        this.eDate = this.endDate.getValue();
        if (this.sDate > this.eDate) {
            WtfComMsgBox(1,2);
            return;
        }
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    
    handleStoreBeforeLoad: function() {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.deleted="false",
        currentBaseParams.nondeleted="false",
        currentBaseParams.reportID=Wtf.autoNum.CreditNoteWithAccountDetail,
        currentBaseParams.startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.cntype = this.typeEditor.getValue();
        currentBaseParams.cnAccountIds=this.MultiSelectAccCombo.getValue();
        this.Store.baseParams=currentBaseParams;
        
        this.exportButton.enable();
        this.printButton.enable();
    },
    
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(new Wtf.grid.CheckboxSelectionModel());  
        columns.push(new Wtf.grid.RowNumberer({width: 30}));
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if(column.renderer){
                column.renderer = eval('('+ column.renderer +')');
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh(); 
        
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    
    handleStoreDataChanged: function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
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
            moduleid: this.moduleid,
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
            searchJson: this.searchJson,
            moduleid: this.moduleId,
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
    
   
    getMyConfig : function(){
        WtfGlobal.getGridConfig (this.grid, Wtf.autoNum.CreditNoteWithAccountDetail, false, false);
    },
    saveGridStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.autoNum.CreditNoteWithAccountDetail, grid.gridConfigId, false);
    },
    customizeView: function() {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            moduleid:this.moduleid,
            reportId: Wtf.autoNum.CreditNoteWithAccountDetail,
            modules:'12'
        });
        this.customizeViewWin.show();
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid = this.Store.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true,false,null,null,null,this.startDate.getValue(),this.endDate.getValue());
        }else if(header=="noteno"){
            this.viewTransection(g,i,j);
        }
    },
    viewTransection:function(grid, rowIndex, columnIndex){
        var formrec=null;
    if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
                WtfComMsgBox(15,2);
                return;
        }
    formrec = this.grid.getStore().getAt(rowIndex);
        callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,this.typeEditor.getValue(),formrec, null);
    }
   
});