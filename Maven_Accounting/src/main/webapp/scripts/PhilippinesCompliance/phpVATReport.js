/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * Author - Rahul A. Bhawar
 */
Wtf.account.phpVATReport = function (config) {
    Wtf.apply(this, config);
    /*
     *  create Grid Store
     */
    this.createStore();
    /**
     * Create Tool Bar and Bottom bar Buttons 
     */
    this.createToolBarButtons();
    this.createBottomBarButtons();
    /*
     * Create Grid 
     */
    this.createGrid();
    Wtf.account.phpVATReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.phpVATReport, Wtf.Panel, {
    onRender: function (config) {
        /*
         * create panel to show grid
         */
        this.createMainPanel();
        this.add(this.VATReportMainPanel);
        Wtf.account.phpVATReport.superclass.onRender.call(this, config);
    },
    createMainPanel: function () {
        this.VATReportMainPanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            layout: 'fit',
                    attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    border: false,
                    layout: 'fit',
                    items: [this.VATReportGrid],
                    tbar: this.toolBarButtonArray,
                    bbar: this.pagingToolbar
                }]
        });
    },
    createToolBarButtons: function () {
        this.toolBarButtonArray = [];
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: (this.params && this.params.reportType == 1) ? WtfGlobal.getDates(true) : new Date(this.params.startdate)
        });
        this.toolBarButtonArray.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: ((this.params && this.params.reportType == 1)) ? WtfGlobal.getDates(false) : new Date(this.params.enddate)
        });
        this.toolBarButtonArray.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);

        if (this.params && this.params.reportType == 2) { // reportType 2 is for detail view
            this.createSectionCombo();
        }
        this.fetchButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this
        });
        this.toolBarButtonArray.push('-', this.fetchButton);
        this.fetchButton.on('click', this.handleFetchButtonClick, this);
        if (this.params && this.params.reportType == 1) {
            this.resetButton = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
                tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.resetbutton),
                disabled: false
            });
            this.toolBarButtonArray.push('-', this.resetButton);
            this.resetButton.on('click', this.handleResetButtonClick, this);
        }
    },
    handleResetButtonClick: function () {
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));
        this.handleFetchButtonClick();
    },
    handleFetchButtonClick: function () {
        this.VATReportGridStore.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 20 : this.pP.combo.value
            }
        });
    },
    createBottomBarButtons: function () {
        this.bottomBarButtonArray = [];
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 20,
            store: this.VATReportGridStore,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
            plugins: this.pP = new Wtf.common.pPageSize(),
            items: this.bottomBarButtonArray
        })
    },
    createGrid: function () {
        this.VATReportGrid = new Wtf.grid.GridPanel({
            store: this.VATReportGridStore,
            columns: [],
            border: false,
            loadMask: true,
            autoScroll: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),
                getRowClass: function (record, index) {
                    return record.data.personname ? '' : 'red-background';
                }
            }
        });
        this.VATReportGrid.on('cellclick', this.onGridCellClick, this);
        this.VATReportGrid.on('render', this.refreshVATReportGrid, this);
        this.VATReportGridStore.on('load', this.handleStoreOnLoad, this);
        this.VATReportGridStore.on('beforeload', this.handleStoreBeforeLoad, this);
        this.VATReportGridStore.on('loadexception', WtfGlobal.resetAjaxTimeOut(), this);
    },
    refreshVATReportGrid: function () {
        this.VATReportGrid.getView().refresh();
    },
    createStore: function () {
        this.VATReportGridStore = new Wtf.data.Store({
            url: this.params.reportStoreURL,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
    },
    createSectionCombo: function () {
        /**
         * Section combo box for detail view
         */
        this.sectionComboRec = Wtf.data.Record.create([
            {
                name: 'sectionName'
            },
            {
                name: 'sectionId'
            }
        ]);
        this.sectionComboStore = new Wtf.data.Store({
            url: this.params.sectionComboURL,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.sectionComboRec)
        });
        this.sectionCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("Type") + " *",
            store: this.sectionComboStore,
            valueField: 'sectionId',
            displayField: 'sectionName',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("Please Select Type"),
            width: 220,
            listWidth: 220,
            extraFields: [],
        });
        this.sectionComboStore.on('load', this.setSectionComboValue, this);
        this.sectionComboStore.load();
        this.toolBarButtonArray.push('-', WtfGlobal.getLocaleText("acc.field.TransactionType") + ":");
        this.toolBarButtonArray.push('-', this.sectionCombo);
    },
    setSectionComboValue: function () {
        if (this.params.section != '') {
            var record = WtfGlobal.searchRecord(this.sectionComboStore, this.params.section, 'sectionName');
            if (record) {
                this.sectionCombo.setValue(record.data.sectionId);
            }
        }
    },
    handleStoreOnLoad: function (store) {
        WtfGlobal.resetAjaxTimeOut();
        var gridColumns = [];
        Wtf.each(this.VATReportGridStore.reader.jsonData.columns, function (column) {
            if (column.dataIndex == "view") {
                column.renderer = this.viewDetailsRenderer;
            } else if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            gridColumns.push(column);
        }, this);
        this.VATReportGrid.getColumnModel().setConfig(gridColumns);
        if (this.VATReportGridStore.getCount() < 1) {
            this.VATReportGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
        }
        this.refreshVATReportGrid();
    },
    handleStoreBeforeLoad: function (store) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        var currentBaseParams = this.VATReportGridStore.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        if (this.params && this.params.reportType == 2) {
            currentBaseParams.section = this.sectionCombo.getValue();
        }
        this.VATReportGridStore.baseParams = currentBaseParams;
    },
    viewDetailsRenderer: function (value, css, record, row, column, store) {
        if (record.data.particulars == Wtf.emptySectionVatReport.NO_Others || record.data.particulars == Wtf.emptySectionVatReport.Others) {
            return "";
        } else {
            return "<img class='add'  style='height:18px; width:18px;' src='images/report.gif' title='View Report '></img>";
        }
    },
    onGridCellClick: function (g, i, j, event) {
        if (event.getTarget("img[class='add']")) {
            var header = g.getColumnModel().getDataIndex(j);
            if (header == "view") {
                var formrec = this.VATReportGrid.getStore().getAt(i);
                var section = formrec.get("particulars");
                var params = {};
                params.title = WtfGlobal.getLocaleText("acc.statutoryPanel.philippines.vat.details.report") + "- " + section;
                params.titleQtip = WtfGlobal.getLocaleText("acc.statutoryPanel.philippines.vat.details.report") + "- " + section;
                params.reportID = "phpVATDetailReport" + WtfGlobal.replaceAll(section, "[^a-zA-Z]", "");
                params.reportStoreURL = "ACCPhilippinesCompliance/getVATDetailReportData.do";
                params.sectionComboURL = "ACCPhilippinesCompliance/getVATReportSectionData.do";
                params.reportType = 2; // 2 for Details data Type
                params.section = section;
                params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
                phpVATReport(params);
            }
        }
    }
});
/**
 * Create philippines VAT Report object and add it to mainPanel
 */
function phpVATReport(params) {
    if (params) {
        var VATReportObject = Wtf.getCmp(params.reportID);
        if (VATReportObject == null) {
            VATReportObject = new Wtf.account.phpVATReport({
                title: Wtf.util.Format.ellipsis(params.title),
                tabTip: params.titleQtip,
                id: params.reportID,
                closable: true,
                border: false,
                params: params,
                layout: 'fit',
                iconCls: 'accountingbase receivepaymentreport'
            });
            Wtf.getCmp('as').add(VATReportObject);
        }
        Wtf.getCmp('as').setActiveTab(VATReportObject);
        Wtf.getCmp('as').doLayout();
    }
}
Wtf.emptySectionVatReport = {
    NO_Others: "18 N/O. Others",
    Others: "23F Others"
}