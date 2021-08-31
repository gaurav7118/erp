/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 * 
 * @param {type} productId = ProductId for whose transaction to be shown
 * @returns {undefined}
 * @description : Component used for Show Product Transaction details
 */
function callProductTransactionDetailReport(productId) {
    var panel = Wtf.getCmp("productTransactionDetailReport");
    if (panel == null) {
        panel = new Wtf.account.productTransactionDetailReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.productTransactionDetailReport"), Wtf.TAB_TITLE_LENGTH), // "Sales by Service Product Detail Report",
            tabTip: WtfGlobal.getLocaleText("acc.field.productTransactionDetailReport"),
            id: "productTransactionDetailReport",
            iconCls: 'accountingbase invoicelist',
            layout: 'fit',
            closable: true,
            border: false,
            productId: productId
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
//    showAdvanceSearch(panel, searchStr, filterAppend);
    Wtf.getCmp('as').doLayout();
}


Wtf.account.productTransactionDetailReport = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    this.createTBar();
    this.createGrid();
    Wtf.account.productTransactionDetailReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.productTransactionDetailReport, Wtf.Panel, {
    onRender: function(config) {
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
                        })
                    })
                }]
        });

        this.add(this.leadpan);
        this.fetchStatement();
        Wtf.account.productTransactionDetailReport.superclass.onRender.call(this, config);
    },
    createTBar: function() {
        this.btnArr = [];
        var tranStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data: [["0", "All"],
                ["" + Wtf.Acc_Sales_Order_ModuleId, "Sales Order"],
                ["" + Wtf.Acc_Purchase_Order_ModuleId, "Purchase Order"],
                ["" + Wtf.Acc_Invoice_ModuleId, "Customer Invoice"],
                ["" + Wtf.Acc_Vendor_Invoice_ModuleId, "Vendor Invoice"],
                ["" + Wtf.Acc_Goods_Receipt_ModuleId, "Goods Receipt"],
                ["" + Wtf.Acc_Delivery_Order_ModuleId, "Delivery Order"],
                ["" + Wtf.Acc_Customer_Quotation_ModuleId, "Customer Quotation"],
                ["" + Wtf.Acc_Vendor_Quotation_ModuleId, "Vendor Quotation"],
                ["" + Wtf.Acc_Purchase_Return_ModuleId, "Purchase Return"],
                ["" + Wtf.Acc_Sales_Return_ModuleId, "Sales Return"],
                ["" + Wtf.Acc_Purchase_Requisition_ModuleId, "Purchase Requisition"],
                ["" + Wtf.Acc_RFQ_ModuleId, "RFQ"],
                ["" + Wtf.Inventory_Stock_Adjustment_ModuleId, "Stock Adjustment"]
            ],
            autoLoad: true
        });
        this.searchCaseComboValue = "0";

        this.transationData = WtfGlobal.getLocaleText("acc.common.PLType");
        this.transactionType = new Wtf.form.ComboBox({
            selectOnFocus: true,
            triggerAction: 'all',
            mode: 'local',
            store: tranStore,
            useDefault: true,
            displayField: 'name',
            typeAhead: true,
            valueField: 'id',
            anchor: '100%',
            value: this.searchCaseComboValue
        });
        this.btnArr.push(this.transationData);
        this.btnArr.push(this.transactionType);
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
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.btnArr.push('-', this.fetchBttn);
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: this.isSummary,
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
            filename: WtfGlobal.getLocaleText("acc.field.productTransactionDetailReport") + "_v1",
//            disabled: true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.ProductSummary
        });
        this.btnArr.push('-', this.exportButton);

        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                type: this.transactionType.getValue(),
                productId: this.productId,
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        }, this);
    },
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "ACCProductCMN/getproductTransactionDetailReport.do",
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

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('datachanged', this.handleStoreDataChanged, this);
        this.grid.on('cellclick',this.onCellClick, this);
    },
    fetchStatement: function() {
       if(this.startDate.getValue()>this.endDate.getValue()){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
             return;
        }
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    handleStoreBeforeLoad: function() {
        this.Store.baseParams = {
            type: this.transactionType.getValue(),
            productId: this.productId,
            startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
        }
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
    handleStoreDataChanged: function() {
        var p = this.pP.combo.value;
    },
    onCellClick: function(g, i, j, e) {
        e.stopEvent();
        var el = e.getTarget("a");
        if (el == null)
            return;
        var header = g.getColumnModel().getDataIndex(j);
        if (header == "transactionno") {
            var formrec = this.grid.getStore().getAt(i);
            var billid=formrec.data.billid;
            var type=formrec.data.type;
            viewTransactionTemplate1(type, formrec,false,billid);            
        }
    },
    handleResetClickNew: function()
    {
        this.startDate.reset();
        this.endDate.reset();
        this.Store.load({
            params: {
                start: 0,
                limit: 30
            }
        })
    }
});
