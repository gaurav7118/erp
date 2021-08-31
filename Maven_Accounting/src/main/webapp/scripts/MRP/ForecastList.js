/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.forecastList = function(config) {
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

    this.addEvents({
        'forecastupdate': true
    });

    this.on('forecastupdate', function() {
        this.fetchStatement();
    }, this);
    Wtf.account.forecastList.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.forecastList, Wtf.Panel, {
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
        Wtf.account.forecastList.superclass.onRender.call(this, config);
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
            emptyText: WtfGlobal.getLocaleText("acc.forecastList.quickSearch"),
            width: 200,
            id: "quickSearch" + this.id,
            field: 'forecastid'
        });
        this.btnArr.push(this.quickPanelSearch);
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
        this.addForecast = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.forecase.addforecast"),
            id: "addForecast",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.forecase.addforecast"),
            iconCls: getButtonIconCls(Wtf.etype.menuadd),
            handler: this.showForm.createDelegate(this, [false, false])
        })
        this.editForecast = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.forecase.editforecast"),
            id: 'editForecast',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.forecase.editforecast"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.menuedit),
            handler: this.showForm.createDelegate(this, [true, false])
        })
        this.copyForecast = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.forecase.copyforecast"),
            id: 'copyForecast',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.forecase.copyforecast"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.copy),
            handler: this.showForm.createDelegate(this, [false, true])
        })
        this.deleteForecast = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.forecase.deleteforecast"),
            id: 'deleteForecast',
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.forecase.deleteforecast"),
            disabled: true,
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            handler: this.handleDelete.createDelegate(this)
        })
        this.personnelArr = [];
        this.personnelArr.push(this.addForecast);
        this.personnelArr.push(this.editForecast);
        this.personnelArr.push(this.copyForecast);
        this.personnelArr.push(this.deleteForecast);
        this.btnArr.push({
            text: WtfGlobal.getLocaleText("acc.forecast.buttonAction"),
            tooltip: WtfGlobal.getLocaleText("acc.forecast.buttonAction"),
            iconCls: 'forecastingicon',
            menu: this.personnelArr
        });
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
//        this.btnArr.push('-', this.AdvanceSearchBtn);
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            filename: WtfGlobal.getLocaleText("acc.forecase.forecastlist")+"_v1",
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.ForecastTemplate
        });
        this.bbarBtnArr.push('-', this.exportButton);
//        this.exportButton.on("click", function() {
//            this.exportButton.setParams({
//                productId: this.productId,
//                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
//            });
//        }, this);

    },
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "ACCJobWorkController/getForecastMerge.do",
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
        this.grid.on('cellclick', this.afteredit, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.sm.on('selectionchange', this.onSelectionEvent, this);
    },
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                ss: this.quickPanelSearch.getValue(),
            }
        });
    },
    afteredit: function(grid, rowIndex, columnIndex, e) {
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        if (fieldName == "productid") {
            callForecastDetailList(record);
        }
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
        this.quickPanelSearch.StorageChanged(store);
    },
    handleResetClickNew: function()
    {
        this.quickPanelSearch.reset();
        this.Store.load({
            params: {
                start: 0,
                limit: 30
            }
        })
    },
    showForm: function(isEdit, isCopy) {
        var rec = null;
        var obj = {};
        obj.isEdit = isEdit;
        obj.isCopy = isCopy;
        obj.rec = rec;
        if (isEdit || isCopy) {
            /*
             * Open Edit case 
             */
            this.recArr = this.grid.getSelectionModel().getSelections();
            if (this.grid.getSelectionModel().hasSelection() == false || this.grid.getSelectionModel().getCount() > 1) {
                WtfComMsgBox(128, 2);
                return;
            }
            rec = this.recArr[0];
            obj.rec = rec;

        }
        callForecastInfo(obj);

    },
    handleDelete: function() {
        if (this.grid.getSelectionModel().hasSelection()) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.mrp.forecast.confirmDelete"), function(btn) {
                if (btn == "yes") {
                    var arr = [];
                    this.recArr = this.grid.getSelectionModel().getSelections();
                    for (i = 0; i < this.recArr.length; i++) {
                        arr.push(this.Store.indexOf(this.recArr[i]));
                    }
                    var data = WtfGlobal.getJSONArray(this.grid, true, arr);
                    Wtf.Ajax.requestEx({
                        url: "ACCJobWorkController/deleteForecast.do",
                        params: {
                            data: data
                        }
                    }, this, this.genSuccessResponse, this.genFailureResponse);
                } else {
                    return;
                }
            }, this);
        } else {
            WtfComMsgBox(128, 2);
            return;
        }

    },
    genSuccessResponse: function(response, request) {
        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,
                width: 450,
                scope: {
                    scopeObj: this
                },
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
            this.Store.reload();
        } else {
            this.showFailureMsg(response);
        }
    },
    genFailureResponse: function(response) {
        this.showFailureMsg(response);
    },
    showFailureMsg: function(response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    onSelectionEvent: function() {
        var arr = this.grid.getSelectionModel().getSelections();
        if (arr.length == 1) {
            this.editForecast.enable();
            this.copyForecast.enable();
            this.deleteForecast.enable();
        } else if (arr.length > 1) {
            this.editForecast.disable();
            this.copyForecast.disable();
            this.deleteForecast.enable();
        }
    }
});
