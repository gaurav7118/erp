/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



Wtf.account.calllinkadvancepaymentWindow = function (config) {

    this.Currency = config.currency;
    this.billDate = config.billdate;
    this.accid = config.accid;
    this.moduleid = config.moduleid;
    this.billid = config.billid;
    this.isCustomer = config.isCustomer;

    this.ajxurl = "ACCSalesOrder/saveSalesOrderLinking.do";
    this.submit = new Wtf.Toolbar.Button({
        text: "Submit",
        tooltip: 'Submit',
        id: 'submit',
        scope: this,
        handler: function () {

            Wtf.Ajax.requestEx({
                url: this.ajxurl,
                params: {
                    accid: this.accid,
                    deleted: false,
                    nondeleted: true,
                    currencyfilterfortrans: this.Currency,
                    isReceipt: this.isCustomer,
                    upperLimitDate: this.billDate,
                    docid: this.billid,
                    isEdit: this.isEdit,
                    requestModuleid: this.moduleid,
                    applyFilterOnCurrency: true,
                    linkedAdvancePaymentId: this.advancePaymentSelCombobox.getValue() || "",
                    linkedAdvancePaymentNo: this.advancePaymentSelCombobox.getRawValue() || ""}
            }, this,
                    function (response) {
                        if (response.success)
                        {
                            this.win.close();
                            var title = WtfGlobal.getLocaleText("acc.invoiceList.linkAdvancePayment");//scope not available in on load function of store
                            WtfComMsgBox([title, response.msg], response.success * 2 + 1);
                        }
                        //on success    

                    }, function (response) {
                if (!response.success)
                {
                    var title = WtfGlobal.getLocaleText("acc.invoiceList.linkAdvancePayment");//scope not available in on load function of store
                    WtfComMsgBox([title, response.msg], response.success * 2 + 1);

                }
            });
        }
    });
    this.cancel = new Wtf.Toolbar.Button({
        text: "Cancel",
        tooltip: 'Cancel',
        id: 'Cancel',
        scope: this,
        handler: function () {
            this.win.close();
        }
    });
    this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },
        {
            name: 'value'
        }
    ]);

    // Added advance payment combo in sales order form
    this.advancePaymentRec = Wtf.data.Record.create([
        {name: 'documentno', mapping: 'billno'},
        {name: 'date', type: 'string'}, //,dateFormat:"Y-m-d" convert: WtfGlobal.convertToDateOnly
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

    this.advancePaymentStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count"
        }, this.advancePaymentRec),
        url: "ACCReceiptCMN/getAdvanceCustomerPaymentForRefunds.do",
        baseParams: {
            onlyAmountDue: true,
            accid: this.accid,
            deleted: false,
            nondeleted: true,
            currencyfilterfortrans: this.Currency,
            isReceipt: this.isCustomer,
            upperLimitDate: this.billDate,
            billId: this.billid,
            isEdit: this.isEdit,
            requestModuleid: this.moduleid,
            applyFilterOnCurrency: true
        }
    });

    this.advancePaymentStore.on('load', function () {
        Wtf.Ajax.requestEx({
            url: "ACCSalesOrderCMN/getLinkedAdvancePayments.do",
            params: {
                billid: this.billid
            }
        }, this, function (response) {
            /*
             setting value to advance payment combo in view and edit case.
             */
            if (response && response.data) {
                var valueFieldArr = [];
                var dataArr = response.data || [];
                for (var i = 0; i < dataArr.length; i++) {
                    if (dataArr[i]["billid"]) {
                        var valueField = this.advancePaymentSelCombobox.valueField;
                        var displayField = this.advancePaymentSelCombobox.displayField;
                        var rec = this.advancePaymentSelCombobox.findRecord(valueField, dataArr[i]["billid"]);
                        if (!rec && dataArr[i]["billno"]) {
                            rec = {}
                            rec[valueField] = dataArr[i]["billid"];
                            rec[displayField] = dataArr[i]["billno"];
                            rec = new Wtf.data.Record(rec);
                            this.advancePaymentSelCombobox.store.add(rec);
                        }
                        valueFieldArr.push(dataArr[i]["billid"]);
                    }
                }
                if (valueFieldArr.length != 0) {
                    this.advancePaymentSelCombobox.setValue(valueFieldArr.join(","));
                }
            }
        }, this.failureCallback);
        this.advancePaymentSelCombobox.store.un("load", this.getLinkedAdvancePayments, this);

    }, this);




    this.advancePaymentSelCombobox = new Wtf.form.ExtFnComboBox({
        labelWidth: 100,
        addNoneRecord: true,
        triggerAction: 'all',
        mode: 'local',
        valueField: 'documentid',
        displayField: 'documentno',
        store: this.advancePaymentStore,
        disabled: false,
        listWidth: 450,
        extraFields: ['date', 'amountdue'],
        forceSelection: true,
        emptyText:  WtfGlobal.getLocaleText("acc.paymentSelection.window.selectpayment"),
        id: 'advancepaymentid123',
        fieldLabel: WtfGlobal.getLocaleText("acc.paymentSelection.window.selectpayment"),
        allowBlank: false,
    });
    this.advancePaymentStore.load();
    var descWindow = Wtf.getCmp(this.id + 'DescWindow')
    if (descWindow == null) {
        this.win = new Wtf.Window
                ({
                    width: 400,
                    height: 130,
                    title:  WtfGlobal.getLocaleText("acc.paymentSelection.window.selectpayment"),
                    id: 'advancePaymentSelWindow',
                    bodyBorder: false,
                    layout: 'fit',
                    items: [
                        {
                            xtype: 'panel',
                            layout: 'form',
                            //autoScroll: true,
                            border: false,
                            method: 'POST',
                            scope: this,
                            width: 400,
                            labelWidth: 150,
                            region: 'center',
                            bodyStyle: 'background:#F1F1F1;padding-left:10px;padding-top:30px;padding-bottom:10px;',
                            items: [this.advancePaymentSelCombobox]
                        }

                    ],
                    closable: true,
                    resizable: false,
                    modal: true,
                    buttons: [this.submit, this.cancel]
                });
    }
    this.win.show();
}

