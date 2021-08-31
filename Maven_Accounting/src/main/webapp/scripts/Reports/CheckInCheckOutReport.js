/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function  checkInCheckOutReportLoad(objParams) {
    var reportPanel = Wtf.getCmp('checkInCheckOutList');
    if (reportPanel == null) {
        reportPanel = new Wtf.account.CheckInCheckOutList({
            id: 'checkInCheckOutList',
            border: false,
            title: WtfGlobal.getLocaleText('acc.report.chechInCheckOutReport.title'),
            tabTip: WtfGlobal.getLocaleText('acc.report.chechInCheckOutReport.title'),
            layout: 'fit',
            label: WtfGlobal.getLocaleText('acc.report.chechInCheckOutReport.title'),
            iconCls: 'checkincheckouticon',
            isFromIncidentReport:false,
            closable: true
        });
        Wtf.getCmp('as').add(reportPanel);

    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();

}
function  checkIncidentCasesReportLoad(objParams) {
    var reportPanel = Wtf.getCmp('incidentCaseList');
    if (reportPanel == null) {
        reportPanel = new Wtf.account.CheckInCheckOutList({
            id: 'incidentCaseList',
            border: false,
            title: WtfGlobal.getLocaleText('acc.report.incidentCasesReport.title'),
            tabTip: WtfGlobal.getLocaleText('acc.report.incidentCasesReport.title'),
            layout: 'fit',
            label: WtfGlobal.getLocaleText('acc.report.incidentCasesReport.title'),
            iconCls: 'incidentcasesicon',
            isFromIncidentReport:true,
            closable: true
        });
        Wtf.getCmp('as').add(reportPanel);

    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();

}

Wtf.account.CheckInCheckOutList = function(config) {

    this.arr = [];
    this.btnArr = [];
    this.bottombtnArr = [];
    this.isFromIncidentReport=(config.isFromIncidentReport!=undefined || config.isFromIncidentReport!=''?config.isFromIncidentReport:false);
    Wtf.apply(this, config);
    this.createGrid();
    this.CreateReportButtons();
    this.addButtonInArray();

    this.exportButton = new Wtf.exportButton({
        obj: this,
        id: "exportButton" + this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        filename: (this.isFromIncidentReport)?WtfGlobal.getLocaleText('acc.report.incidentCasesReport.title')+"_v1":WtfGlobal.getLocaleText("acc.report.chechInCheckOutReport.title")+"_v1",
        disabled: false,
        scope: this,
        menuItem: {
            csv: true,
            pdf: true,
            rowPdf: false,
            xls: true
        },
        get: (this.isFromIncidentReport)?Wtf.autoNum.IncidentCasesReport:Wtf.autoNum.checkInandCheckOut
    });
    this.Store.on('load', this.storeloaded, this);
    this.bottombtnArr.push(this.exportButton);
    this.bottombtnArr.push('-');
    Wtf.account.CheckInCheckOutList.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.CheckInCheckOutList, Wtf.Panel, {
    onRender: function(config) {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.gridtbarPanel],
                    tbar: [this.firstToolBar],
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        }),
                        items: [this.exportButton]
                    })
                }
            ]
        });

        this.add(this.leadpan);
        this.loadStore();
        Wtf.account.CheckInCheckOutList.superclass.onRender.call(this, config);
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(this.sm);
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "description") {
                column.renderer = function(value, meta, rec) {
                    var value = rec.data.description;
                    return "<span wtf:qtip=\"" + value + "\">" + value + "</span>";
                }
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
    loadStore: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                ss: this.quickPanelSearch.getValue(),
                pagingFlag: true

            }
        });

    },
    storeloaded: function(store) {
        WtfGlobal.resetAjaxTimeOut();
        if (store.getCount() == 0) {
            if (this.exportButton)
                this.exportButton.disable();
        } else {
            if (this.exportButton)
                this.exportButton.enable();
        }
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                ss: this.quickPanelSearch.getValue(),
                pagingFlag: true
            }
        });
    },
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },
    createGrid: function() {
        
        var URL="ACCCustomerCMN/getCustomerCheckInandCheckOutDetails.do";
        if(this.isFromIncidentReport){
            URL="ACCCustomerCMN/getIncidentCasesDetails.do";
        }
        this.Store = new Wtf.data.Store({
            url: URL,
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            })
        });

        this.Store.on('beforeload', function(s, o) {
            WtfGlobal.setAjaxTimeOut();
            if (this.pP.combo != undefined) {
                if (this.pP.combo.value == "All") {
                    var count = this.Store.getTotalCount();
                    var rem = count % 5;
                    if (rem == 0) {
                        count = count;
                    } else {
                        count = count + (5 - rem);
                    }
                    s.paramNames.limit = count;
                }
            }
        }, this);
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            border: false,
            columns: [],
            layout: 'fit',
            sm: this.sm,
            viewConfig: {
                forceFit: true
            },
            loadMask: true
        });

        this.gridtbarPanel = new Wtf.Panel({
            border: false,
            layout: 'border',
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid]
                }]
        });

    },
    CreateReportButtons: function() {
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: (this.isFromIncidentReport)?WtfGlobal.getLocaleText("acc.report.incidentCasesReport.searchBy"):WtfGlobal.getLocaleText("acc.field.SearchbyCustomerNameandCustomerID"), 
            width: 200,
            id: "quickSearch" + this.id,
            field: 'transactionNumber'

        });

        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false,
            handler: this.handleResetClick
        });

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });

    },
    genSuccessResponse: function(response) {
        WtfGlobal.resetAjaxTimeOut();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), response.msg], response.success * 2 + 1);
        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.mrp.machinemasterReport.reportName"),
                msg: response.msg,
                width: 500,
                scope: {
                    scopeObj: this
                },
                buttons: Wtf.MessageBox.OK,
                fn: function(btn, text, option) {
                },
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
        }
    },
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    addButtonInArray: function() {
        this.btnArr.push(this.quickPanelSearch);
        this.btnArr.push(this.resetBttn);
        this.btnArr.push(this.fetchBttn);
        this.firstToolBar = new Wtf.Toolbar(this.btnArr);
    },
});