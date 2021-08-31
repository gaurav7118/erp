function callPhilippiensReliefReports(params) {
    var reportId = params.reportID;
    var reliefReportPanel = Wtf.getCmp(reportId);
    if (reliefReportPanel == null) {
        reliefReportPanel = new Wtf.account.callPhilippiensReliefReports({
            title: params.title,
            tabTip: params.titleQtip,
            id: reportId,
            params:params,
            layout: 'fit',
            closable: true
        });
        Wtf.getCmp('as').add(reliefReportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reliefReportPanel);
    Wtf.getCmp('as').doLayout();

}
Wtf.account.callPhilippiensReliefReports = function (config) {

    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createTBar();
    this.createStore();
    /*
     * Create Grid 
     */
    this.createGrid();

    Wtf.account.callPhilippiensReliefReports.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.callPhilippiensReliefReports, Wtf.Panel, {
    onRender: function (config) {

        this.createPanel();
        this.add(this.leadpan);

        Wtf.account.callPhilippiensReliefReports.superclass.onRender.call(this, config);
    },
    createPanel: function () {
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
                    bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
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
    createStore: function () {
        this.Store = new Wtf.data.Store({
            url:this.params.reportStoreURL,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
    },
    createTBar: function () {
        this.btnArr = [];
        this.mnuBtn=[];
        this.bbarBtnArr=[];
        this.exportMenu = [];
        this.exportMenuItem = new Wtf.menu.Item({
            text: "Export XLS",
            iconCls: 'pwnd ' + 'exportcsv',
            scope: this,
            handler: function () {
                var url = "";
                var parameters = "";
                if (this.params.reportType == Wtf.PHPReportType.PurchaseRelief) {
                    url = "ACCPhilippinesCompliance/exportPurchaseReliefReport.do";
                    parameters = "&startdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                    parameters += "&enddate=" + WtfGlobal.convertToGenericStartDate(this.endDate.getValue());
                    parameters += "&filename=Relief_Purchases_Report";
                    parameters += "&filetype=xls";
                    parameters += "&get=" + Wtf.autoNum.putchasesReliefReport;
                    parameters += "&reportType=" + this.params.reportType;
                }
                 Wtf.get('downloadframe').dom.src = url + "?" +parameters;
            }
        });
        this.exportMenu.push(this.exportMenuItem);
        this.exportBtn = new Wtf.Action({
            iconCls: 'pwnd ' + 'exportcsv',
            text: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToXLSTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToXLSX") + "</span>",
                    menu: {
                        items: this.exportMenu
                    }
        })
        this.mnuBtn.push(this.exportBtn);
        this.bbarBtnArr.push("-",this.mnuBtn);
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(false)
        });
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function () {
                this.fetchStatement();
            }
        });
        this.btnArr.push('-', this.fetchBttn);
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.btnArr.push('-', this.resetBttn);
    },
    createGrid: function () {
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
            border: false,
            loadMask: true,
            autoScroll: true,
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),
                getRowClass: function (record, index) {
                    return 'red-background'
                }
            }
        });
        this.grid.on('render', function () {
            this.grid.getView().refresh();
        }, this);
        this.Store.on('load', this.handleStoreOnLoad, this);


    },
    fetchStatement: function () {
        this.Store.load({
            params: {
                start: 0,
                limit: 30,
                reportType:this.params.reportType,
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())

            }
        });

    },
    handleStoreOnLoad: function (store) {
        var columns = [];
        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
    }
})

