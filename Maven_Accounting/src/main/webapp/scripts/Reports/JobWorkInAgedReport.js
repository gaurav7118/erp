/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
function jobWorkInAgedReport() {
    var mainTabId = Wtf.getCmp("as");
    var newTab = Wtf.getCmp("jobWorkInAgedReport");
    if (newTab == null) {
        newTab = new Wtf.account.jobWorkInAgedReport({
            layout: "fit",
            title: WtfGlobal.getLocaleText("acc.field.jobWorkInDetailReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.jobWorkInDetailReport"),
            closable: true,
            border: false,
            iconCls: getButtonIconCls(Wtf.etype.inventorysmr),
            id: "jobWorkInAgedReport"
        });
        mainTabId.add(newTab);
    }
    mainTabId.setActiveTab(newTab);
    mainTabId.doLayout();
}

Wtf.account.jobWorkInAgedReport = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createGrid();
    
    this.createTBar();
    /*
     * Create Grid 
     */

    Wtf.account.jobWorkInAgedReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.jobWorkInAgedReport, Wtf.Panel, {
    onRender: function(config) {
        /*
         * create panel to show grid
         */
        this.createPanel();
        this.add(this.leadpan);
        /*
         * fetch data in report
         */
        this.fetchStatement();
        Wtf.account.jobWorkInAgedReport.superclass.onRender.call(this, config);
    },
    createPanel: function() {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
                items: [this.objsearchComponent,{
                    region: 'center',
                    border: false,
                    layout: "fit",
                    autoScroll: true,
                    tbar: this.btnArr,
                    items: [this.grid],
                    bbar:  this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        }),
                        items: this.bbarBtnArr
                    })
            }]
        });
    },
    createTBar: function() {
        this.btnArr = [];
        this.bbarBtnArr = [];
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: "", 
            width: 200,
            id: "quickSearch" + this.id,
            field: 'empid',
            hidden: true
        });
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true)
        });
        this.asOfDate=new Wtf.ExDateFieldQtip({
            name:'asofdate',
            id: 'asofdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:new Date()
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(false)
        });
        this.btnArr.push(this.quickPanelSearch);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.asOf"),this.asOfDate);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.btnArr.push('-', this.fetchBttn);
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
         this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        
         this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleidarray: '30'.split(','),
            advSearch: true,
            parentPanelSearch: this,
             isOnlyGlobalCustomColumn: true,
            moduleid:Wtf.Acc_Product_Master_ModuleId,
            reportid:Wtf.Acc_Product_Master_ModuleId,
            ignoreDefaultFields:true
        });
    
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        this.btnArr.push('-', this.resetBttn,'-',this.AdvanceSearchBtn);
        this.resetBttn.on('click', this.handleResetClickNew, this);
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            filename: WtfGlobal.getLocaleText("acc.field.jobWorkDetailReport") + "_v1",
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.ChallanReport
        });
        this.bbarBtnArr.push('-', this.exportButton);
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                jobWorkOderInAged: true
            });
        }, this);

    },
    createGrid: function() {
        this.Store = new Wtf.data.GroupingStore({
            url: "ACCJobWorkController/getChallanReport.do",
            sortInfo:{
                field: 'product', 
                direction: "ASC"
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        this.Store.on('loadexception', function () {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            });
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
            border: false,
            loadMask: true,
            sm: this.sm,
            view : new Wtf.grid.GroupingView({
                forceFit:false,
                // custom grouping text template to display the number of items per group
                groupTextTpl: '{text}',
                enableGroupingMenu:true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            })
            
        });

        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
    },
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                ss: this.quickPanelSearch.getValue(),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        });
    },
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();

    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            type: this.type,
            frmDate: this.startDate.getValue().format('Y-m-d'),
            toDate: this.endDate.getValue().format('Y-m-d'),
            asofdate: this.asOfDate.getValue().format('Y-m-d'),
            isJobWorkOutRemain: true,
            jobWorkStockOut: true,
            isJobWorkStockOut: true,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_Product_Master_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria,
            reportId: Wtf.Acc_Product_Master_ModuleId
        }
        this.Store.load({
            params: {
                start: 0,
                limit: Wtf.getCmp("pagingtoolbar" + this.id).pageSize//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
//                ss:this.grid.quickSearchTF.getValue(),
//                type:this.type
            }
        }, this);
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            type: this.type,
            frmDate: this.startDate.getValue().format('Y-m-d'),
            toDate: this.endDate.getValue().format('Y-m-d'),
            asofdate: this.asOfDate.getValue().format('Y-m-d'),
            isJobWorkOutRemain: true,
            jobWorkStockOut: true,
            isJobWorkStockOut: true,
            moduleid: Wtf.Acc_Product_Master_ModuleId
        }
        this.Store.load({
            params: {
                start: 0,
                limit: Wtf.getCmp("pagingtoolbar" + this.id).pageSize, //30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
//                ss:this.grid.quickSearchTF.getValue(),
                type: this.type
            }
        }, this);
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },
    handleStoreBeforeLoad: function(store) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        this.Store.groupBy(undefined);
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.asOfDate = WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue());
        currentBaseParams.jobWorkOderInAged = true;
        this.Store.baseParams = currentBaseParams;
    },
    handleStoreOnLoad: function(store) {
        WtfGlobal.resetAjaxTimeOut();
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        columns.push(this.sm);
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            column.renderer = WtfGlobal.deletedRenderer;
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        } else {
            this.Store.sortInfo = {
                field: 'jobinno',
                direction: "ASC"
            };
            this.Store.groupBy("jobworkin");
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    handleResetClickNew: function()
    {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.startDate.setValue(this.getDates(true));
        this.endDate.setValue(this.getDates(false));
        this.fetchStatement();
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
    }
    });