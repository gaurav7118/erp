function StockReportOnDimensionDynamicLoad(params) {
    var searchStr = params.searchStr || "";
    var filterAppend = params.filterAppend || "";

    /*
     *Function to add this report in widget report.
     **/
    if (params.isCustomWidgetReport) {
        panel = new Wtf.StockReportOnDimension({
            layout: "fit",
            closable: true,
            border: false,
            isCustomWidgetReport : params.isCustomWidgetReport,
            iconCls: getButtonIconCls(Wtf.etype.inventorysmr),
            searchJson: searchStr,
            filterAppend: filterAppend
        });
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    } else {
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockmovementregister)) {
            var mainTabId = Wtf.getCmp("as");
            var newTab = Wtf.getCmp("StockReportOnDimension");
            if (newTab == null) {
                newTab = new Wtf.StockReportOnDimension({
                    layout: "fit",
                    title: WtfGlobal.getLocaleText("acc.inventoryList.StockMovementReportWithAdvanceSearch"),
                tabTip:WtfGlobal.getLocaleText("acc.inventoryList.StockMovementReportWithAdvanceSearch"),
                    closable: true,
                    border: false,
                    iconCls: getButtonIconCls(Wtf.etype.inventorysmr),
                    id: "StockReportOnDimension",
                    searchJson: searchStr,
                    filterAppend: filterAppend
                });
                mainTabId.add(newTab);
            }
            mainTabId.setActiveTab(newTab);
            showAdvanceSearch(newTab, searchStr, filterAppend);
            mainTabId.doLayout();
        } else {
            WtfComMsgBox(46, 0, false, WtfGlobal.getLocaleText("acc.common.viewing") + " " + "this feature");
        }
    }
}
Wtf.StockReportOnDimension = function(config) {
    Wtf.apply(this, config);
    this.createTBar();
    this.arr = [];
    this.dmflag = 1;
    this.Store = new Wtf.data.GroupingStore({
        url: 'ACCProductCMN/getDetailedStockMovementList.do',
        reader: new Wtf.data.KwlJsonReader({
            root: 'data',
            totalProperty: 'totalCount'
        }
        )
    });

    this.groupStore = new Wtf.data.GroupingStore({
        groupField: ['itemcode']
    });
    this.sm = new Wtf.grid.RowSelectionModel({
    });
    var cmDefaultWidth = 106;
    var colArr = [];
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
    this.cm = new Wtf.grid.ColumnModel(colArr);
    this.summary = new Wtf.grid.GroupSummary();
    this.gridSummary = new Wtf.grid.GridSummary();
    var grpView = new Wtf.grid.GroupingView({
        startCollapsed :true,
        forceFit: false,
        showGroupName: true,
        enableGroupingMenu: true,
        hideGroupedColumn: false
    });
    this.grid =  new Wtf.grid.GridPanel({
        columns: [],
        store: this.groupStore,
        displayInfo: true,
        qsWidth: 200,
        sm: this.sm,
        loadMask: true,
        searchLabel: "Quick Search",
        searchLabelSeparator: ":",
        searchEmptyText: "Search By Product ID,Product Name,Serial Name ",
        serverSideSearch: true,
        searchField: "itemcode",
        view: grpView,
//                tbar:tbarArray,
        bbar: this.bbarArray,
        plugins: [this.summary]//, this.gridSummary]
    });
    this.quickPanelSearch.StorageChanged(this.Store);
    this.Store.on("load", this.storeLoaded, this);
    this.Store.on('beforeload',function(s,o){
        this.ReportloadingMask=undefined;
        if(this.ReportloadingMask==undefined){
            this.ReportloadingMask = new Wtf.LoadMask(document.body,{
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
            });
            this.ReportloadingMask.show();
        }
    },this);
//        this.Store.on('beforeload', function() {
//        var currentBaseParams = this.Store.baseParams;
//        currentBaseParams.limit = 30;
//    },this);
//    Wtf.getCmp("paggintoolbar" + this.grid.id).on('beforerender', function() {
//        Wtf.getCmp("paggintoolbar" + this.grid.id).pageSize = 30;
//    }, this);
//    Wtf.getCmp("paggintoolbar" + this.grid.id).store = this.Store;
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: '30,27,28,29,31'.split(','),
        advSearch: false,
        parentPanelSearch: this,
        isAvoidRedundent: true,
        reportid: Wtf.autoNum.StockReportOnDimension
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    Wtf.StockReportOnDimension.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.StockReportOnDimension, Wtf.Panel, {
    onRender: function(config) {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.tbarArray,
                    bbar:this.pag
                }]
        });

        this.add(this.leadpan);
        this.fetchData();
        Wtf.StockReportOnDimension.superclass.onRender.call(this, config);

    },
    createTBar: function() {
        this.format = 'Y-m-d';
        this.fromdateVal = new Date().getFirstDateOfMonth();
        this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
        var companyDateFormat = 'Y-m-d'
        this.frmDate = new Wtf.ExDateFieldQtip({
            emptyText: WtfGlobal.getLocaleText("acc.stock.Fromdate"),
            readOnly: true,
            width: 100,
            value: WtfGlobal.getDates(true),
            name: 'frmdate',
            minValue: Wtf.archivalDate,
            format: companyDateFormat//Wtf.getDateFormat()
        });

        this.todateVal = new Date().getLastDateOfMonth();
        this.todateVal.setDate(new Date().getLastDateOfMonth().getDate());

        this.toDate = new Wtf.ExDateFieldQtip({
            emptyText: WtfGlobal.getLocaleText("acc.stock.Todate"),
            readOnly: true,
            width: 100,
            minValue: Wtf.archivalDate,
            name: 'todate',
            value: WtfGlobal.getDates(false),
            format: companyDateFormat//Wtf.getDateFormat()
        });

        this.storeCmbRecord = new Wtf.data.Record.create([
            {
                name: 'store_id'
            },
            {
                name: 'abbrev'
            },
            {
                name: 'description'
            },
            {
                name: 'fullname'
            }
        ]);

        this.storeCmbStore = new Wtf.data.Store({
            url: 'INVStore/getStoreList.do',
            baseParams: {
//                isActive: true,         //ERP-40021 :To get all Stores.
                byStoreExecutive: "true",
                byStoreManager: "true",
                includeQAAndRepairStore:true,
                includePickandPackStore:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, this.storeCmbRecord)
        });
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.dayendcollection.search"),
            width: this.isCustomWidgetReport ? 50 : 100,
            id: "quickSearch",
            field: 'billno',
            Store: this.Store
        })
        this.storeCmb = new Wtf.form.ComboBox({
            hiddenName: 'storeid',
            store: this.storeCmbStore,
            typeAhead: true,
            displayField: 'fullname',
            valueField: 'store_id',
            mode: 'local',
            width: 120,
            triggerAction: 'all',
            emptyText:WtfGlobal.getLocaleText("acc.je.Selectstore"),
            listWidth:300,
            tpl: new Wtf.XTemplate(
                    '<tpl for=".">',
                    '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                    '<div>{fullname}</div>',
                    '</div>',
                    '</tpl>')
        });

        this.InOutStore = new Wtf.data.SimpleStore({
            fields: ["id", "name"],
            data: [["ALL", "ALL"], ["IN", "IN"], ["OUT", "OUT"]]
        });
        this.InOutCmb = new Wtf.form.ComboBox({
            hiddenName: 'inOutFilter',
            store: this.InOutStore,
            typeAhead: true,
            displayField: 'name',
            valueField: 'id',
            mode: 'local',
            width: 120,
            triggerAction: 'all',
            emptyText: 'Select transaction Type...'
        });

        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.common.search"),
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
            },
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function() {
                if (this.storeCmb.getValue() != "") {
                    this.sDate=this.frmDate.getValue();
                    this.eDate=this.toDate.getValue();
                    if(this.sDate > this.eDate){
                        WtfComMsgBox(1,2);
                        return;
                    }
                    this.loadGrid( WtfGlobal.convertToGenericEndDate(this.frmDate.getValue()),  WtfGlobal.convertToGenericEndDate(this.toDate.getValue()), this.storeCmb.getValue());
                } else {
                    var msg = ["Stock Movement Report",WtfGlobal.getLocaleText("acc.stockrequest.PleaseSelectStore")];
                   WtfComMsgBox(msg, 0); 
                }
            }
        });

        this.resetBtn = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.stock.ClicktoResetFilter")
            },
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            hidden : this.isCustomWidgetReport,
            scope: this,
            handler: function() {
                this.frmDate.setValue(this.fromdateVal);
                this.toDate.setValue(this.todateVal);
                this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
            }
        });

        /*
         * Provided button to expand or collapse all row details. 
         * We display ProductId,ProductName,Description,Warehouse,location Document Date,Cost Center,Transaction Module,
         * References,Remarks,Customer/Vendor,Type,UOM,QuantityIn,QuantityOut,Lot/Batch,SeriaNo.,UnitPrice,Amount,Reusable
         * or Consumable,Reusability Count.
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
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: 'stockmovementondomensionexport',
            tooltip: WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"), //"Export Report details.",  
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.StockReportOnDimension,
            label: "Export",
            hidden : this.isCustomWidgetReport,
            filename: WtfGlobal.getLocaleText("acc.stockMovement.unblockdocuments")
        });

        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                storeid: this.storeCmb.getValue(),
                fromDate:  WtfGlobal.convertToGenericEndDate(this.frmDate.getValue()),
                toDate:  WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                reportId: Wtf.autoNum.StockReportOnDimension,
                isExport: true,
                ss: this.quickPanelSearch.getValue(),
                searchJson: this.searchJson == undefined ? "" : this.searchJson,
                filterConjuctionCriteria: this.filterConjuctionCrit == undefined ? "" : this.filterConjuctionCrit
            });
        }, this);

        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            hidden: ( this.isSOPOByProductReport || this.isCustomWidgetReport),
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        this.tbarArray = new Array();

        this.tbarArray.push(this.quickPanelSearch, "-", WtfGlobal.getLocaleText("acc.common.from")+" : ", this.frmDate, "-", WtfGlobal.getLocaleText("acc.common.to")+" : ", this.toDate, "-", WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+" : ", this.storeCmb, "-", //"-","Transaction Type: ", this.InOutCmb,"-",
                this.search, "-", this.resetBtn, "-", this.AdvanceSearchBtn, '-', this.expandCollpseButton);
        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            hidden : this.isCustomWidgetReport,
            iconCls: 'accountingbase fetch'
        });
        this.tbarArray.push('->', this.customReportViewBtn);
        this.bbarArray = [this.exportButton]
        this.storeCmbStore.load();
    },
    storeLoaded: function() {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({width: 30}));
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            if (column.summaryRenderer) {
                column.summaryRenderer = eval('(' + column.summaryRenderer + ')');
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
//        this.Store.each(function(rec) {
        this.groupStore.add(this.Store.getRange(0, (this.Store.data.items.length - 1)));
//        this.groupStore.add(this.Store.getRange(0,29));
//        Wtf.getCmp("paggintoolbar" + this.grid.id).store = this.groupStore;
//        }, this);
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();
        WtfGlobal.resetAjaxTimeOut();
        if(this.Store.getCount() < 1) {
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        if (this.ReportloadingMask) {
            this.ReportloadingMask.hide();
        }
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
    },
    fetchData: function() {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        this.Store.load({
            params: {
                start: 0,
                limit: 30, //Wtf.companyPref.recperpage,
                frmDate: WtfGlobal.convertToGenericEndDate(this.frmDate.getValue()),
                toDate:  WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                reportId: Wtf.autoNum.StockReportOnDimension
            }
        });
    },
    loadGrid: function(frm, to, storeid, vendorid, ownershipid, itemcode) {
        this.Store.baseParams = {
            fromDate: frm,
            toDate: to,
            storeid: storeid,
            reportId: Wtf.autoNum.StockReportOnDimension
        }
        if (storeid != "") {
            WtfGlobal.setAjaxTimeOut();
            this.Store.load({
                params: {
                    start: 0,
                    limit: 30, //30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                    ss: this.quickPanelSearch.getValue(),
                    reportId: Wtf.autoNum.StockReportOnDimension
                }
            });
        }
        else
            return;
    },
    customizeView: function() {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: Wtf.autoNum.StockReportOnDimension,
            modules: '' + Wtf.Acc_Delivery_Order_ModuleId + ',' + Wtf.Acc_Goods_Receipt_ModuleId + ',' + Wtf.Acc_Sales_Return_ModuleId + ',' + Wtf.Acc_Purchase_Return_ModuleId
            + ',' + Wtf.Acc_Product_Master_ModuleId  + ',' + Wtf.Inventory_Stock_Adjustment_ModuleId  + ',' + Wtf.Acc_InterStore_ModuleId
        });
        this.customizeViewWin.show();
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
            storeid: this.storeCmb.getValue(),
            fromDate:  WtfGlobal.convertToGenericEndDate(this.frmDate.getValue()),
            toDate:  WtfGlobal.convertToGenericEndDate(this.toDate.getValue().format(this.format)),
            reportId: Wtf.autoNum.StockReportOnDimension
        }
        this.Store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: 30}});
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            pendingApproval: this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            storeid: this.storeCmb.getValue(),
            fromDate:  WtfGlobal.convertToGenericEndDate(this.frmDate.getValue()),
            toDate:  WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
            reportId: Wtf.autoNum.StockReportOnDimension
        }
        this.Store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: 30}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
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
