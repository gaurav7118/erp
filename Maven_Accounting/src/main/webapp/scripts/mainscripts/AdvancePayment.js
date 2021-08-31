/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.AdvancePaymentWindow = function (config) {
    this.isCustomer = config.isCustomer,
    this.currencyfilterfortrans = config.currencyfilterfortrans,
    this.isReceipt = config.isReceipt,
    this.personInfo = config.personInfo,
    this.butnArr = [];
    this.isSubmitBtnClicked = false;
    this.isEdit = config.isEdit;
    this.billid = config.billid;
    this.isMulticurrency=config.isMulticurrency;    // ERP-40513 result of comparision of payment currency and payment method currency (true when different; false otherwise) 
    this.moduleid = this.isReceipt ? Wtf.Acc_Receive_Payment_ModuleId : Wtf.Acc_Make_Payment_ModuleId;
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: "Search by "+(this.isReceipt ? "Payment No" : "Receipt No"),
        width: 200,
        id: "quickSearch" + config.id
    });
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClick, this);
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
        name: 'startdate',
        style: "margin-left: 15px;",
        format: WtfGlobal.getOnlyDateFormat(),
        value: WtfGlobal.getDates(true)
    });
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format: WtfGlobal.getOnlyDateFormat(),
        style: "margin-left: 15px;",
        name: 'enddate',
        value: new Date(this.personInfo.upperLimitDate)
    });
    this.endDate.on('change', this.onEndDateChange, this);
    this.fetchBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: this.isCN ? WtfGlobal.getLocaleText("acc.cn.fetchTT") : WtfGlobal.getLocaleText("acc.dn.fetchTT"), //"Select a time period to view corresponding credit/debit note records.",
        style: "margin-left: 15px;",
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.loadStore
    });
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"), //'Submit',
        scope: this,
        handler: function () {
            this.isSubmitBtnClicked = true;
            this.submitSelectedRecords();
        }
    }, {
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
        scope: this,
        handler: function () {
            this.isSubmitBtnClicked = false;
            this.close();
        }
    });
    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.AdvancePaymentWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.AdvancePaymentWindow, Wtf.Window, {
    height: 485,
    width: 800,
    modal: true,
    iconCls: 'pwnd deskeralogoposition',
    onRender: function (config) {
        Wtf.account.InvoiceInfoWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        var msg = (this.isCustomer ? "<b>"+WtfGlobal.getLocaleText("acc.contractDetails.CustomerName")+"</b> : " : "<b>"+WtfGlobal.getLocaleText("acc.ven.name")+"</b> : ") + this.personInfo.personName + (this.isCustomer ? "<br> <b>"+WtfGlobal.getLocaleText("acc.stockLedgerCust.Code")+"</b> : " : "<br> <b>"+WtfGlobal.getLocaleText("acc.stockLedgerven.Code")+"</b> : ") + this.personInfo.personCode;
        msg = msg + '<div style="font-size:14px; text-align:left; font-weight:bold; margin-top:1%;">'+WtfGlobal.getLocaleText("acc.field.Note")+': </div>' + '<div style="font-size:12px; text-align:left; margin-top:1%;">'+WtfGlobal.getLocaleText("acc.common.Onlythoseadvancepaymentswhoseamountdueisgreaterthan0")+'</div>'
        var isgrid = true;
        this.add({
            region: 'north',
            height: 115,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('', msg, "../../images/accounting_image/price-list.gif", isgrid)
        }, this.centerPanel = new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan' + this.id,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: [this.grid],
            tbar: [this.quickPanelSearch, this.resetBttn, WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, this.fetchBttn],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id: "pPageSize_" + this.id
                })
            })
        }))
    },
    createDisplayGrid: function () {
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.cm = new Wtf.grid.ColumnModel([this.sm, {
                header: this.isCustomer ? WtfGlobal.getLocaleText("acc.prList.gridReceiptNo") : WtfGlobal.getLocaleText("acc.pmList.gridPaymentNo"),
                dataIndex: 'documentno'
            },
            {
                header: this.isCustomer ? WtfGlobal.getLocaleText("acc.mp.custAccount") : WtfGlobal.getLocaleText("acc.rp.venAccount"),
                dataIndex: 'accountnames',
                align: 'center'
            },
            {
                header: this.isCustomer ? WtfGlobal.getLocaleText("acc.prList.Date") : WtfGlobal.getLocaleText("acc.pmList.Date"),
                dataIndex: 'date',
                align: 'center',
                renderer: WtfGlobal.onlyDateDeletedRenderer
            },
            {
                header: WtfGlobal.getLocaleText("acc.het.53"), //"Amount"
                dataIndex: 'amount',
                align: 'right',
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction
            },
            {
                header: WtfGlobal.getLocaleText("acc.mp.amtDue"), //"Amount Due"
                dataIndex: 'amountDueOriginal',
                align: 'right',
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction
            }]);


        this.Rec = Wtf.data.Record.create([
            {name: 'documentno', mapping: 'billno'},
            {name: 'date', type: 'date'},
            {name: 'amount', type: 'float'},
            {name: 'amountdue', type: 'float'},
            {name: 'exchangeratefortransaction'},
            {name: 'currencysymboltransaction'},
            {name: 'currencyidtransaction'},
            {name: 'currencynametransaction'},
            {name: 'currencyname'},
            {name: 'currencyid'},
            {name: 'documentid', mapping: 'billid'},
            {name: 'amountDueOriginal', type: 'float'},
            {name: 'amountDueOriginalSaved', type: 'float'},
            {name: 'accountid'},
            {name: 'recTermAmount'},
            {name: 'LineTermdetails'},
            {name: 'accountnames'}
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.Rec),
            url: this.isCustomer ? "ACCReceiptCMN/getAdvanceCustomerPaymentForRefunds.do" : "ACCVendorPaymentCMN/getAdvanceVendorPaymentForRefunds.do",
            baseParams: {
                onlyAmountDue: true,
                accid: this.personInfo.accid,
                deleted: false,
                nondeleted: true,
                currencyfilterfortrans: this.currencyfilterfortrans,
                //isReceipt: this.isReceipt,    //ERP-40513 'isReceipt' will be send in request based on 'isMulticurrency' flag
                upperLimitDate: this.personInfo.upperLimitDate,
                billId: this.billid,
                isEdit: this.isEdit,
                requestModuleid: this.moduleid
            }
        });
        if(!this.isMulticurrency){  // 'isMulticurrency' is true when payment method and payment currency are different and false otherwise, and when true means 'isReceipt is missing in the request and condition for currency records will get appended 
            this.store.baseParams['isReceipt']=this.isReceipt;  // 'isReceipt' flag is used at back-end to decide whether all currency invoices are to be returnde or only payment currency invoices to be returned
        }
        this.store.on("loadexception", function () {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.mp.unableToLoadData")], 1);
        }, this)
        this.store.on('load', this.storeLoaded, this);
        var GlobalColumnModelArr = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(GlobalColumnModelArr, this.store);
        this.store.on('beforeload', function (store, option) {
            var currentBaseParams = this.store.baseParams;
            currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
            currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
            this.store.baseParams = currentBaseParams;
        }, this);
        this.store.on('datachanged', function () {
            var p = this.pP ? this.pP.combo.value : 30;
            this.quickPanelSearch.setPage(p);
        }, this);
        this.store.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: 30}});
        this.grid = new Wtf.grid.GridPanel({
            store: this.store,
            height: 235,
            autoScroll: true,
            cm: this.cm,
            sm: this.sm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });

    },
    submitSelectedRecords: function () {
        var selections = this.grid.getSelectionModel().getSelections();
        if (selections.length == 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.15")], 2);
            return;
        }
        this.close();
    },
    getSelectedRecords: function () {
        var arr = [];
        var selectionArray = this.grid.getSelectionModel().getSelections();
        for (var i = 0; i < selectionArray.length; i++) {
            arr.push(this.store.indexOf(selectionArray[i]));
        }
        var jarray = WtfGlobal.getJSONArrayWithoutEncodingNew(this.grid, true, arr);
        return jarray;
    },
    storeLoaded: function () {
        this.quickPanelSearch.StorageChanged(this.store);
    },
    handleResetClick: function () {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.store.load();
        }
    },
    loadStore: function () {
        this.store.reload();
    },
    onEndDateChange: function (obj, val, oldVal) {
        if (val.getTime() > new Date(this.personInfo.upperLimitDate).getTime()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeGreaterThanPaymentDate")], 2);
            this.endDate.setValue(oldVal);
        }
    }
})