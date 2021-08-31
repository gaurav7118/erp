/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function  salesByProductCategoryDetailReportDynamicLoad(searchStr, filterAppend){
    var panel = Wtf.getCmp("salesByProductReportDetails");
    if(panel==null){
        panel = new Wtf.account.SalesProductCategoryDetailReport({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.salesbyproductCategory.tabTitle")),
            tabTip:WtfGlobal.getLocaleText("acc.salesbyproductCategory.tabTitle"),  // "Account Forecast Report",
            id:'salesByProductReportDetails',
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

//*******************************************************************************************
Wtf.account.SalesProductCategoryDetailReport = function(config) {
    this.arr = [];
    this.id=config.id;
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: false
    });

    this.Store = new Wtf.data.Store({
        url: "ACCInvoiceCMN/getInvoiceForSalesProductCategoryDetails.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'totalCount'
        }, this.GridRec)
    });

    this.customReportViewBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        scope: this,
        // hidden:!(this.isOrder && this.isCustomer),
        tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        handler: this.customizeView,
        iconCls: 'accountingbase fetch'
    });

    this.startDate = new Wtf.ExDateFieldQtip({
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        // readOnly:true,
        value: this.getDates(true)
    });

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
    this.fetchButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.loadStore
    });
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.prList.searchProductcategory"),
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
    
     this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        params:{
            name: "Sales By Product Category Report"
            },
        filename: WtfGlobal.getLocaleText("acc.salesbyproductCategory.tabTitle"),
        menuItem:{print:true},
        get:Wtf.autoNum.salesByProductCategoryDetail
    });
    this.exportButton = new Wtf.exportButton({
        obj: this,
        id: "exportReports" + this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
//        disabled: true,
        scope: this,
        filename: WtfGlobal.getLocaleText("acc.salesbyproductCategory.tabTitle")+"_v1",
        menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
        params: {
            name: "Sales By Product Category Report",
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())

        },
        get: Wtf.autoNum.salesByProductCategoryDetail
    });
    this.bottomBtnArr = [];
    this.bottomBtnArr.push('-', this.exportButton,"-",this.printButton);
    this.Store.on('load', function() {
//            WtfGlobal.closeProgressbar();
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({width: 30}));
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex != "date" && column.dataIndex != "billid" && column.dataIndex != "fileRef" && column.dataIndex != "clientName" && column.custom != "true") {
                    if(column.baseCurrencyRenderer !=undefined && column.baseCurrencyRenderer == true){
                        column.renderer = WtfGlobal.currencyDeletedRenderer;
                    }else{
                        column.renderer = WtfGlobal.withoutRateCurrencySymbol;
                    }
                }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
//            WtfGlobal.closeProgressbar();
        this.grid.getView().refresh();
    }, this);

    this.tbar1 = [];
    this.tbar1.push(this.quickPanelSearch, "-", this.resetBttn,"-",this.startDate, "-", WtfGlobal.getLocaleText("acc.common.to"), this.endDate, "-", this.fetchButton, "-", this.AdvanceSearchBtn,"->", this.customReportViewBtn );
    this.grid = new Wtf.grid.GridPanel({
        //stripeRows :true,
        store: this.Store,
        id: "gridmsg1",
        height: 500,
        columns: [],
        border: false,
        disabledClass: "newtripcmbss",
        layout: 'fit',
        autoScroll: true,
        loadMask: true,
        viewConfig: {
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
    });
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: Wtf.Acc_Invoice_ModuleId,
        advSearch: false,
        parentPanelSearch: this,
        isOnlyGlobalCustomColumn: true,
        customerCustomFieldFlag: true,
        reportid: Wtf.autoNum.salesByProductCategoryDetail
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
                bbar: this.pag=new Wtf.PagingSearchToolbar({
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
            }]
    });
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.leadpan]
    });
    this.Store.on("beforeload", function(s, o) {    //ERP-9201
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.arr = this.arr;
        //        currentBaseParams.start =0;
        //        currentBaseParams.limit = 30;
        currentBaseParams.startDate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.limit = this.pPageSizeObj.combo==undefined?30:this.pPageSizeObj.combo.value;
        currentBaseParams.reportId = Wtf.autoNum.salesByProductCategoryDetail;
        if(this.pPageSizeObj.combo!=undefined){         //*ERP-21147 ISSUE IN PAIGING WHEN SHOW ALL IS SELECTED*//
            if(this.pPageSizeObj.combo.value=="All"){
                var count = this.Store.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                currentBaseParams.limit = count;
            }
        }   
    }, this);
    this.Store.load({
        params: {
            start: 0,
            limit: 30
        }
    });
    Wtf.account.SalesProductCategoryDetailReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.SalesProductCategoryDetailReport, Wtf.Panel, {
     handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
        }
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
    }, configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();

    }, customizeView: function() {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.autoNum.salesByProductCategoryDetail,
            modules:'25',
        });
        this.customizeViewWin.show();
        var arr = this.arr;
    }, filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_Invoice_ModuleId,
            isFixedAsset: false,
            isLeaseFixedAsset: false,
            filterConjuctionCriteria: filterConjuctionCriteria
//            pendingapproval: false,
//            isDraft: this.isDraft
        }
        this.Store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: this.pPageSizeObj.combo.value}});
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
//            pendingapproval: this.pendingapproval,
//            isDraft: this.isDraft
        }
        this.Store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: this.pPageSizeObj.combo.value}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


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
                limit:(this.pPageSizeObj.combo!=undefined) ? this.pPageSizeObj.combo.value : 30,
                ss: this.quickPanelSearch.getValue()
            }
        });
    }
});