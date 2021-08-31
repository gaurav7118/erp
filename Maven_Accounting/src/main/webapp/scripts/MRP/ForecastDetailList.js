/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.forecastDetailList = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
//    this.createTBar();
    /*
     * Create Grid 
     */
    this.createGrid();

    Wtf.account.forecastDetailList.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.forecastDetailList, Wtf.Panel, {
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
        Wtf.account.forecastDetailList.superclass.onRender.call(this, config);
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
//                    tbar: this.btnArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
//                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        }),
//                        items: this.bbarBtnArr
                    })
                }]
        });
    },
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "ACCJobWorkController/getForecastDetailsMerge.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
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
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.Store.on('beforeload', this.addParamsInRequest, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.sm.on('selectionchange', this.onSelectionEvent, this);
    },
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
//                ss: this.quickPanelSearch.getValue(),
                billid: this.record.data.billid,
                productid: this.record.data.productid
            }
        });
    },
    addParamsInRequest: function(s, o) {
        if (!o.params)
            o.params = {};
        o.params.billid = this.record.data.billid;
        o.params.productid = this.record.data.productid;
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        columns.push(this.sm);
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "productid") {
                column.renderer = function(v, m, rec) {
                    return "<a href='#'>View Details</a>";
                };
            } else
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
//        this.quickPanelSearch.StorageChanged(store);
    },
    handleResetClickNew: function()
    {
//        this.quickPanelSearch.reset();
        this.Store.load({
            params: {
                start: 0,
                limit: 30
            }
        })
    },
});
