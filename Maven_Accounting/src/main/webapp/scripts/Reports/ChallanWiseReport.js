/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
function ChallanReport() {
    var mainTabId = Wtf.getCmp("as");
    var newTab = Wtf.getCmp("ChallanReport");
    if (newTab == null) {
        newTab = new Wtf.account.ChallanReport({
            layout: "fit",
            title: WtfGlobal.getLocaleText("acc.challanreport.challanreport"),
            tabTip: WtfGlobal.getLocaleText("acc.challanreport.challanreport"),
            closable: true,
            border: false,
            iconCls: getButtonIconCls(Wtf.etype.inventorysmr),
            id: "ChallanReport"
        });
        mainTabId.add(newTab);
    }
    mainTabId.setActiveTab(newTab);
    mainTabId.doLayout();
}

Wtf.account.ChallanReport = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createTBar();
    /*
     * Create Grid 
     */
    this.createGrid();

    Wtf.account.ChallanReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.ChallanReport, Wtf.Panel, {
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
        Wtf.account.ChallanReport.superclass.onRender.call(this, config);
    },
    createPanel: function() {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.btnArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
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
            emptyText: "", //WtfGlobal.getLocaleText("acc.labourList.quickSearch"), 
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
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(false)
        });
        this.personRec = new Wtf.data.Record.create([
            {
                name: 'accid'
            }, {
                name: 'accname'
            }, {
                name: 'acccode'
            }, {
                name: 'taxId'
            }
        ]);
        this.customerAccStore = new Wtf.data.Store({
            url: "ACCCustomer/getCustomersForCombo.do",
            baseParams: {
                mode: 2,
                group: 10,
                deleted: false,
                nondeleted: true,
                common: '1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad: false
            }, this.personRec)
        });
        this.customerAccStore.on("load", function() {
            var record = new Wtf.data.Record({
                accid: "",
                accname: "All Records",
                acccode: ""
            });
            this.customerAccStore.insert(0, record);
            this.custmerCmb.setValue("");
        }, this);
        this.btnArr.push(this.quickPanelSearch);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        this.custmerCmb = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.cust"),
            hiddenName: 'customerid',
            id: "customer" + this.id,
            store: this.customerAccStore,
            valueField: 'accid',
            displayField: 'accname',
            allowBlank: false,
            typeAhead: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            mode: 'remote',
            anchor: "50%",
            triggerAction: 'all',
            scope: this,
            width: 150
        });

        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.up.3"), this.custmerCmb);
        this.productRec = Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'}
        ]);
        this.productStore = new Wtf.data.Store({
            url: "ACCProduct/getProductsForCombo.do",
            baseParams: {mode: 22,
                onlyProduct: true,
                isFixedAsset: false,
                includeBothFixedAssetAndProductFlag: false,
                excludeParent: true,
                type: Wtf.producttype.customerInventory
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });
        this.productStore.on("load", function() {
            var record = new Wtf.data.Record({
                productid: "",
                productname: "All Records"
            });
            this.productStore.insert(0, record);
            this.productcmb.setValue("");
        }, this);
        this.productcmb = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
            hiddenName: 'productid',
            name: 'productid',
            hidden: this.isCustBill,
            store: this.productStore,
            valueField: 'productid',
            displayField: 'productname',
            mode: 'remote',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            width: 150,
            listWidth: 150
        });
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.invReport.prod"), this.productcmb);
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
        this.btnArr.push('-', this.resetBttn);
        this.resetBttn.on('click', this.handleResetClickNew, this);
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            filename: WtfGlobal.getLocaleText("acc.challanreport.challanreport") + "_v1",
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
                productid: this.productcmb.getValue(),
                customerid: this.custmerCmb.getValue(),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        }, this);

    },
    createGrid: function() {
        this.Store = new Wtf.data.GroupingStore({
            url: "ACCJobWorkController/getChallanReport.do",
            sortInfo:{field: 'product', direction: "ASC"},
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
                productid: this.productcmb.getValue(),
                customerid: this.custmerCmb.getValue(),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        });
    },
    handleStoreBeforeLoad: function(store) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        this.Store.groupBy(undefined);
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.customerid=this.custmerCmb.getValue();
        currentBaseParams.productid=this.productcmb.getValue();
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
//        this.grid.getView().setConfig()
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        } else {
            this.Store.sortInfo = {
                field: 'product',
                direction: "ASC"
            };
            this.Store.groupBy("product");
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    handleResetClickNew: function()
    {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.productcmb.setValue("");
        this.custmerCmb.setValue("");
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