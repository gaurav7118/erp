/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.assignTaskList = function(config) {
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createTBar();
    /*
     * Create Grid 
     */
    this.createGrid();
    Wtf.account.assignTaskList.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.assignTaskList, Wtf.Panel, {
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
        Wtf.account.assignTaskList.superclass.onRender.call(this, config);
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
            emptyText: WtfGlobal.getLocaleText("acc.labourList.quickSearch"), 
            width: 200,
            hidden: false,
            field: 'empid'
        });
        this.btnArr.push(this.quickPanelSearch);
                this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.btnArr.push('-', this.fetchBttn);
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), 
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
            filename: WtfGlobal.getLocaleText("acc.field.labourList") + "_v1",
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.LabourList
        });
        this.bbarBtnArr.push('-', this.exportButton);
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                type: this.transactionType.getValue(),
                productId: this.productId,
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        }, this);

        this.assigntaskBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.labourProfile.assigntask"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.labourProfile.assigntask"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        });
        this.bbarBtnArr.push('-', this.assigntaskBtn);
    },
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "ACCLabourCMN/getAssignTaskList.do",
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

        this.Store.on('load', this.handleStoreOnLoad, this);
    },
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
    },
    handleResetClickNew: function()
    {
        this.Store.load({
            params: {
                start: 0,
                limit: 30
            }
        })
    }
});