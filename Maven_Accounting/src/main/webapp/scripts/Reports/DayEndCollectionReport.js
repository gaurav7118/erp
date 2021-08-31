/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Load Dynamic DayEndCollectionReport.js
 */
function  callDayEndCollectionReportDynamicLoad(searchStr, filterAppend){
    var panel = Wtf.getCmp("dayEndCollectionReport");
    if(panel==null){
        panel = new Wtf.account.DayEndCollectionDetailReport({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.dayendcollection.tabTitle")),
            tabTip:WtfGlobal.getLocaleText("acc.dayendcollection.tabTitle"),  // "Account Forecast Report",
            id:'dayEndCollectionReport',
            closable:true,
            border:false,
            iconCls:'accountingbase vendor',
            activeTab:0
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    showAdvanceSearch(panel,searchStr, filterAppend);
    Wtf.getCmp('as').doLayout();
}


//*************************************************************************************************************

Wtf.account.DayEndCollectionDetailReport = function(config) {
    this.arr = [];
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: false
    });
    this.Store = new Wtf.data.Store({
        url: "ACCReceipt/getDayEndcollectionDetails.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'totalCount'
        })
    });
    
    this.startDate = new Wtf.ExDateFieldQtip({
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        value: this.getDates(true)
    });
    this.personRec = new Wtf.data.Record.create([
    {
        name: 'accid'
    }, {
        name: 'accname'
    }, {
        name: 'acccode'
    },{
        name: 'taxId'
    }
    ]);
    
    var colArr = [];
    this.cm = new Wtf.grid.ColumnModel(colArr);
    this.summary = new Wtf.grid.GroupSummary();
    this.pmtRec = new Wtf.data.Record.create([
    {
        name: 'methodid'
    },

    {
        name: 'methodname'
    },

    {
        name: 'accountid'
    },

    {
        name: 'acccurrency'
    },

    {
        name: 'accountname'
    },

    {
        name: 'isIBGBankAccount', 
        type:'boolean'
    },

    {
        name: 'isdefault'
    },

    {
        name: 'detailtype',
        type:'int'
    },

    {
        name: 'acccustminbudget'
    },

    {
        name: 'autopopulate'
    },
    ]);
    this.pmtStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.pmtRec),
        url : "ACCPaymentMethods/getPaymentMethods.do"
    }); 
    
    this.pmtStore.on("load", function() {
        var record = new Wtf.data.Record({
            methodid: "",
            methodname: "All"
        });
        this.pmtStore.insert(0, record);
        this.pmtMethod.setValue("");
    }, this);
   
    this.endDate = new Wtf.ExDateFieldQtip({
        name: 'enddate',
        format: WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        value: this.getDates(false)
    });
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        hidden: this.isSOPOByProductReport,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    this.Store.on('beforeload',function(s,o){
        if(!o.params)o.params={};
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startDate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());   
        currentBaseParams.endDate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());  
        currentBaseParams.reportId=Wtf.autoNum.dayEndCollectionReport;
        currentBaseParams.paymentMethodId= this.pmtMethod.getValue();
        this.Store.baseParams=currentBaseParams;
    },this);
    this.fetchButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.loadStore
    });
    
    /*
     * Provided button to expand or collapse all row details. 
     * We display CustomerName,CustomerCode,CashSales/ReceiptNo.,Document Date,Pay in Ref,
     * Amount,Total Amount(In Base Currency)
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
   
    this.pmtMethod= new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.payMethod"),
        name:"pmtmethod",
        store:this.pmtStore,
        id:'pmtmethod'+this.heplmodeid+this.id,
        valueField:'methodid',
        displayField:'methodname',
        allowBlank:this.cash?false:true,
        emptyText:WtfGlobal.getLocaleText("acc.mp.selpayacc"),
        width:150,
        mode: 'local',
        triggerAction: 'all',
        typeAhead: true,
        forceSelection: true//,
    });
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.dayendcollection.search"),
        width: 150,
        id: "quickSearch" + config.helpmodeid + config.id,
        field: 'billno',
        Store: this.Store
    })
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClick, this);
    
    this.customReportViewBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        scope: this,
        // hidden:!(this.isOrder && this.isCustomer),
        tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        handler: this.customizeView,
        iconCls: 'accountingbase fetch'
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        filename: WtfGlobal.getLocaleText("acc.dayendcollection.tabTitle"),
        menuItem:{
            print:true
        },
        get: Wtf.autoNum.dayEndCollectionReport
    });
    
    this.printButton.on("click", function() {
        this.printButton.setParams({
            startDate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            endDate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            reportId:Wtf.autoNum.dayEndCollectionReport
        });
    },this);
    this.exportButton = new Wtf.exportButton({
        obj: this,
        id: "exportReports" + this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        //        disabled: true,
        scope: this,
        filename: WtfGlobal.getLocaleText("acc.dayendcollection.tabTitle")+"_v1",
        menuItem: {
            csv: true, 
            pdf: true, 
            rowPdf: false, 
            xls: true
        },
        get: Wtf.autoNum.dayEndCollectionReport
    });
   this.exportButton.on("click", function() {
        this.exportButton.setParams({
            startDate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            endDate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            reportId:Wtf.autoNum.dayEndCollectionReport,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit,
        });
    },this);
    this.bottomBtnArr = [];
    this.bottomBtnArr.push('-', this.exportButton,"-",this.printButton);
    this.pmtStore.load(); 
    this.groupStore = new Wtf.data.GroupingStore({
        groupField: ['customerCode']
    });
    this.Store.on("dataChanged", function(){
        this.loadData();
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false; 
    }, this);
    
    this.Store.on('load', function() {
        this.loadData();               
    }, this);
    this.tbar1 = [];
    this.tbar1.push(this.quickPanelSearch, "-", this.resetBttn, "-", this.AdvanceSearchBtn, "-", this.startDate, "-", WtfGlobal.getLocaleText("acc.common.to"), this.endDate, "-", WtfGlobal.getLocaleText("acc.pmList.gridPaymentMethod"), "-", this.pmtMethod, "-", this.fetchButton, '-', this.expandCollpseButton, "->", this.customReportViewBtn);
  
    var grpView = new Wtf.grid.GroupingView({
        forceFit: false,
        showGroupName: true,
        startCollapsed :true,
        enableGroupingMenu: true,
        hideGroupedColumn: false,
        emptyText: "<div class='grid-empty-text'>" + WtfGlobal.getLocaleText("acc.common.norec") + "</div>"
    });
    this.pag=new Wtf.PagingSearchToolbar({
        pageSize: 30,
        border : false,
        id : "paggintoolbar_ProductGrid"+this.id,
        store: this.Store,
        searchField: this.quickPanelSearch,
        scope:this,
        plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
            id : "pPageSize_ProductGrid_"+this.id
        }),
        items: this.bottomBtnArr,
        autoWidth : true,
        displayInfo:true//,
    })
    this.grid = new Wtf.grid.GridPanel({
        store: this.groupStore,
        height: 500,
        scope:this,
        columns: [{
            dataIndex: "customerCode"
        }],
        border: false,
        loadMask: true,
        sm: this.sm,
        displayInfo: true,
        disabledClass: "newtripcmbss",
        //        layout: 'fit',
        autoScroll: true,
        view: grpView,
        plugins: [this.summary]
    });
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: '25,16'.split(','),
        advSearch: true,
        parentPanelSearch: this,
        //        isOnlyGlobalCustomColumn: true,
//        customerCustomFieldFlag: true,
        moduleid:Wtf.autoNum.dayEndCollectionReport,
        reportid: Wtf.autoNum.dayEndCollectionReport,
        ignoreDefaultFields:true
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [this.objsearchComponent,
        {
            region: 'center',
            border: false,
            layout: "fit",
            autoScroll: true,
            tbar: this.tbar1,
            items: [this.grid],
            bbar:  this.pag
        }]
    });
    
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.leadpan]
    });
    this.Store.load({
        params: {
            start: 0,
            limit: 30
        }
    });
    Wtf.account.DayEndCollectionDetailReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.DayEndCollectionDetailReport, Wtf.Panel, {
    onRender:function(config){
        Wtf.account.DayEndCollectionDetailReport.superclass.onRender.call(this,config);   
    },
    handleResetClick:function(){
        this.startDate.setValue(this.getDates(true));
        this.endDate.setValue(this.getDates(false));
        this.pmtMethod.setValue("");
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
        }
        this.loadStore();
    },
    loadData:function(){
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            if (column.summaryRenderer) {
                column.summaryRenderer = eval('(' + column.summaryRenderer + ')');
            }
            if(column.dataIndex == "amountinbase"){
                column.renderer = WtfGlobal.currencyDeletedRenderer ;
            }
            if (column.hidden) {
                column.hidden = eval('(' + column.hidden + ')');
            }
            columns.push(column);
        });
        var Arr = [];
        Wtf.each(this.Store.reader.jsonData.metaData.fields, function(column) {
            Arr.push(column);
        });
        this.groupStore.removeAll();
        this.groupStore.fields = Arr;
        var array=[];
        this.groupStore.add(this.Store.getRange(0, (this.Store.data.items.length - 1)));
       
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getColumnModel().on("configchanged", function(){
            alert("configchanged");
        },this);
        this.grid.getView().refresh();  
    },
    getDates: function(start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
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
            pendingApproval: this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria,
            //            storeid: this.storeCmb.getValue(),
            startDate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),   
            endDate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),  
            reportId: Wtf.autoNum.dayEndCollectionReport
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pPageSizeObj.combo.value
            }
        });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_Invoice_ModuleId,
            isFixedAsset: false,
            isLeaseFixedAsset: false,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pPageSizeObj.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },
    customizeView: function() {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.autoNum.dayEndCollectionReport,
            modules:'16,25'
        });
        this.customizeViewWin.show();
        var arr = this.arr;
    },
    loadStore: function() {
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        if(sDate > eDate){
            WtfComMsgBox(1,2);
            return;
        }
        this.Store.load({
            params: {
                start: 0,
                limit: this.pPageSizeObj.combo.value,
                ss: this.quickPanelSearch.getValue()
            }
        });
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