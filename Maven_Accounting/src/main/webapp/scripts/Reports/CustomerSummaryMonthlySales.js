/* 
 *JS for customer summary report which provides product wise sale of each customer for each month. 
 */
function customerSummaryMonthlySalesDynamicLoad(consolidateFlag, withinventory) {
    var panel = Wtf.getCmp('customerSummaryMonthlySalesReport');
    if (panel == null) {
        panel = new Wtf.account.CustomerSummaryMonthlySalesReport({
            id: 'customerSummaryMonthlySalesReport',
            border: false,
            helpmodeid: 96,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.customersales.tabTitle"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.customersales.tabTitle"),
            receivable: true,
            monthlysalesreport: true,
            closable: true
        });
        Wtf.getCmp('as').add(panel);
        panel.on('salesinvoices', MonthlySalesInvoicesList);
    }

    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


Wtf.account.CustomerSummaryMonthlySalesReport = function (config) {
    this.receivable = config.receivable || false;
    this.isSummary = config.isSummary || false;
    this.summary = new Wtf.ux.grid.GridSummary();
    this.expander = new Wtf.grid.RowExpander({});

    // to change this with the month & year drop-down list
    this.monthStore = new Wtf.data.SimpleStore({
        fields: [{name: 'monthid', type: 'int'}, 'name'],
        data: [[0, 'January'], [1, 'February'], [2, 'March'], [3, 'April'], [4, 'May'], [5, 'June'], [6, 'July'], [7, 'August'], [8, 'September'], [9, 'October'],
            [10, 'November'], [11, 'December']]
    });

    var data = WtfGlobal.getBookBeginningYear(true);

    this.yearStore = new Wtf.data.SimpleStore({
        fields: [{name: 'id', type: 'int'}, 'yearid'],
        data: data
    });

    this.startMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), //'Month',
        name: 'startMonth',
        displayField: 'name',
        forceSelection: true,
        width: 90,
        valueField: 'name',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus: true
    });

    this.startYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"), //'Year',
        name: 'startYear',
        width: 90,
        displayField: 'yearid',
        valueField: 'yearid',
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus: true
    });

    this.endMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), //'Month',
        name: 'endMonth',
        displayField: 'name',
        forceSelection: true,
        width: 90,
        valueField: 'name',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus: true
    });

    this.endYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"), //'Year',
        name: 'endYear',
        displayField: 'yearid',
        width: 90,
        valueField: 'yearid',
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus: true
    });

    if (config.sMonth != null && config.sMonth != "")
        this.startMonth.setValue(config.sMonth);

    if (config.sYear != null && config.sYear != "")
        this.startYear.setValue(config.sYear);

    if (config.eMonth != null && config.eMonth != "")
        this.endMonth.setValue(config.eMonth);

    if (config.eYear != null && config.eYear != "")
        this.endYear.setValue(config.eYear);

    this.uPermType = Wtf.UPerm.invoice;
    this.permType = Wtf.Perm.invoice;
    this.exportPermType = (this.receivable ? this.permType.exportdataagedreceivable : this.permType.exportdataagedpayable);
    this.printPermType = (this.receivable ? this.permType.printagedreceivable : this.permType.printagedpayable);
    this.chartPermType = (this.receivable ? this.permType.chartagedreceivable : this.permType.chartagedpayable);
    
    this.Store = new Wtf.data.Store({
        url: "ACCInvoiceCMN/getMonthlyCustomerSalesReport.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'totalCount'
        }),
        baseParams: {
            mode: 18,
            creditonly: false,
            nondeleted: true,
            getRepeateInvoice: false,
            companyids: companyids,
            gcurrencyid: gcurrencyid,
            userid: loginid
        }
    });
    
    this.expGet = Wtf.autoNum.customerMonthlySalesbyProduct;

    this.MonthlySalesStore = new Wtf.data.GroupingStore({
        groupField: 'customername'
    });

    this.summary = new Wtf.grid.GroupSummary({});
    this.gridView = new Wtf.grid.GroupingView({
        forceFit: false,
        showGroupName: true,
        enableNoGroups: true, // REQUIRED!
        hideGroupedColumn: false,
        emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });
    this.grid = new Wtf.grid.GridPanel({
        store: this.MonthlySalesStore,
        columns: [{
            dataIndex: "customername"
        }],
        ctCls: 'monthlySalesreport',
        border: false,
        plugins: [this.expander],
        layout: 'fit',
        viewConfig: this.gridView,
        loadMask: true,
        displayInfo: true,
        disabledClass: "newtripcmbss",
        plugins: [this.summary],
    });

    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        hidden: false,
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });

    // get date from month & year drop-down lists
    if (this.startMonth.getValue() == "" || this.startYear.getValue() == "") {
        var temp = new Date();
        var year1 = temp.getFullYear();
        this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
        this.startYear.setValue(year1);
        this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length - 1].json[1]);
        this.endYear.setValue(year1);
    }

    this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
    this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

    if (this.sDate == "" || this.eDate == "") {
        WtfComMsgBox(42, 2);
        return;
    }

    var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
    var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());

    if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()) {
        WtfComMsgBox(1, 2);
        return;
    }

    var months;
    months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
    months -= startMonthDate.getMonth();
    months += endMonthDate.getMonth();
    if (months < 0)
        months = 0;

    if (months > 12) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Onlymaximum12monthsaresupported")], 2);
        return;
    }

    this.expButton = new Wtf.exportButton({
        obj: this,
        filename: "CustomerSummaryMonthlySalesReport_v1",
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.sales.exportTT"), //'Export report details',
        disabled: true,
        params: {
            stdate: this.sDate,
            enddate: this.eDate,
            accountid: this.accountID || config.accountID
        },
        menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
        get: this.expGet
    })
    this.printButton = new Wtf.exportButton({
        obj: this,
        text: WtfGlobal.getLocaleText("acc.common.print"),
        tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), //'Print report details',
        disabled: true,
        filename: WtfGlobal.getLocaleText("acc.customersales.tabTitle"),
        params: {
            accountid: this.accountID || config.accountID,
            stdate: this.sDate,
            enddate: this.eDate,
            name: WtfGlobal.getLocaleText("acc.customersales.tabTitle")
        },
        label: WtfGlobal.getLocaleText("acc.customersales.tabTitle"),
        menuItem: {print: true},
        get: this.expGet
    })
    var btnArr = [];
    btnArr.push(
            this.quickPanelSearch = new Wtf.KWLTagSearch({
                emptyText: WtfGlobal.getLocaleText("acc.saleByItem.search"), // Search by Product Name
                id: "quickSearch" + config.helpmodeid,
                width: 200,
                hidden: false,
                field: 'customername',
                Store: this.Store
            }), this.resetBttn);


    btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"),
            this.startMonth, this.startYear
            );

    btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"),
            this.endMonth, this.endYear
            );
    this.expandStore = new Wtf.data.Store({
        url: "ACCWorkOrder/getWorkOrderExpanderDetails.do",
        baseParams: {
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.expandRec)
    });

    btnArr.push("-", {
        xtype: 'button',
        text: WtfGlobal.getLocaleText("acc.sales.fetch"), //'Fetch',
        iconCls: 'accountingbase fetch',
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.sales.view"), //"Select a date to view Monthly Sales Report
        handler: this.fetchMonthlySalesReport
    });

    btnArr.push(this.expButton);
    btnArr.push(this.printButton);
    this.resetBttn.on('click', this.handleResetClick, this);
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        tbar: btnArr,
        items: [this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id: "pPageSize_" + this.id
            })
        })
    });

    Wtf.account.CustomerSummaryMonthlySalesReport.superclass.constructor.call(this, config);
    this.addEvents({
        'salesinvoices': true
    });

    this.Store.on("beforeload", function (s, o) {
        o.params.stdate = this.sDate;
        o.params.enddate = this.eDate;
        o.params.ss = this.quickPanelSearch == undefined ? "" :this.quickPanelSearch.getValue();
        this.expButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,
            ss : this.quickPanelSearch == undefined ? "" :this.quickPanelSearch.getValue()
        });
        this.printButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,
            ss : this.quickPanelSearch == undefined ? "" :this.quickPanelSearch.getValue()
       })
    }, this);

    this.Store.on('load', this.loadData, this);
    this.Store.load({
        params: {
            start: 0,
            limit: 30,
            creditonly: true
        }
    });

    this.Store.on('datachanged', function () {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);

}

Wtf.extend(Wtf.account.CustomerSummaryMonthlySalesReport, Wtf.Panel, {
    handleResetClick: function () {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.Store.load({
                params: {
                    start: 0,
                    limit: this.pP.combo.value,
                    aged: true,
                    creditonly: true
                }
            });
        }
    },
    loadData:function(){
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if(column.dataIndex != "customername" && column.dataIndex != "productname"){
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
        this.MonthlySalesStore.removeAll();
        this.MonthlySalesStore.fields = Arr;
        this.MonthlySalesStore.add(this.Store.getRange(0, (this.Store.data.items.length - 1)));
       
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();  
        
        if (this.grid.getStore().getCount() == 0) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();

            if (this.expButton)
                this.expButton.disable();
            if (this.printButton)
                this.printButton.disable();
        } else {
            if (this.expButton)
                this.expButton.enable();
            if (this.printButton)
                this.printButton.enable();

        }
    },
    fetchMonthlySalesReport: function () {
        if (this.startYear.getValue() > this.endYear.getValue()) {
            WtfComMsgBox(1, 2);
            return;
        }
        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == "") {
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length - 1].json[1]);
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }
        
        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());

        if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()) {
            WtfComMsgBox(1, 2);
            return;
        }

        var months;
        months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
        months -= startMonthDate.getMonth();
        months += endMonthDate.getMonth();
        if (months < 0)
            months = 0;

        if (months > 11) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Onlymaximum12monthsaresupported")], 2);
            return;
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        if (this.sDate == "" || this.eDate == "") {
            WtfComMsgBox(42, 2);
            return;
        }

        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo.value,
                creditonly: true
            }
        });


    }
});