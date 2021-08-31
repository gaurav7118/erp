/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.bulkPaymentGrid = function(config) {
    this.isReceipt = config.isReceipt;
    this.id = config.id;
    this.recArray = config.recArr;
    this.paymentMethodType = config.paymentMethodType != undefined ? config.paymentMethodType : 0;
    this.isPaymentMethodChanged = config.isPaymentMethodChanged != undefined ? config.isPaymentMethodChanged : false;
    this.paymentType = config.paymentType;
    this.isCustomer = config.isCustomer;
    this.isAllowedSpecificFields = (config.isAllowedSpecificFields == null || config.isAllowedSpecificFields == undefined) ? false : config.isAllowedSpecificFields;
    this.AllowToEditCopy = config.AllowToEditCopy
    this.isChequeNoExist = [];
    this.issameVendor = [];
    this.totalRowEntered = [];
    this.parentObj = config.parentObj;
    this.invObj = config.invObj;

    var count = 0;
    this.gridRec = Wtf.data.Record.create([
        {name: 'documentno'},
        {name: 'vendor'},
        {name: 'invoicedate'},
        {name: 'amountdue'},
        {name: 'enteramount'},
        {name: 'paymentstatus'},
        {name: 'postdate'},
        {name: 'chequenumber'},
        {name: 'exchangeratefortransaction'},
        {name: 'transactionAmount'},
        {name: 'documentid'},
        {name: 'vendorid'},
        {name: 'paymentthrough'},
        {name: 'description', defValue: ""},
        {name: 'clearancedate'},
        {name: 'amount', convert: function (value) {
                if (value === undefined || value === null || value === "") {
                    value = 0;
                } else if (typeof value === "string") {
                    value = parseFloat(value);
                }
                return isNaN(value) ? 0 : value;
            }},
        {name: 'discountname', defValue: 0},
        {name: 'discountvalue', convert: function (value) {
                if (value === undefined || value === null || value === "") {
                    value = 0;
                } else if (typeof value === "string") {
                    value = parseFloat(value);
                }
                return isNaN(value) ? 0 : value;
            }},
        {name: 'discounttype', type: 'boolean', defValue: false},
        {name: 'applicabledays', convert: function (value) {
                if (value === undefined || value === null || value === "") {
                    value = -1;
                } else if (typeof value === "string") {
                    value = parseInt(value);
                }
                return isNaN(value) ? -1 : value;
            }},
        {name: 'invoicecreationdate', type: 'date'},
        {name: 'grcreationdate', defValue: 0},
        {name: 'amountdueafterdiscount', defValue: 0},
        {name: 'isDiscountFieldChanged', convert: function (value) {
                if (value === undefined || value === null || value === "") {
                    value = false;
                } else if (typeof value === "string") {
                    value = value == "true";
                }
                return value;
            }},
        {name: 'amountDueOriginal', defValue: 0}




    ]);

    this.store = new Wtf.data.SimpleStore({
        fields: this.gridRec
    });

    var columnArr = [];



    columnArr.push({
        header: 'Invoice Number',
        dataIndex: 'documentno',
        width: 80,
        pdfwidth: 75

    }, {
        header: this.isCustomer ? 'Customer Name' : 'Vendor Name',
        dataIndex: 'vendor'
    }, {
        header: 'Document Id',
        dataIndex: 'documentid',
        hidden: true
    }, {
        header: 'Vendor Id',
        dataIndex: 'accid',
        hidden: true

    }
    ,
            {
                header: 'Invoice Date',
                dataIndex: 'invoicedate',
                renderer: WtfGlobal.onlyDateRenderer
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.transactionAmount"),
                dataIndex: 'transactionAmount',
                width: 150,
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction


            }, {
        header: WtfGlobal.getLocaleText("acc.field.transactionAmountDue"),
        dataIndex: 'amountDueOriginal',
        hidelabel: true,
        hidden: true,
        width: 150,
        renderer: WtfGlobal.withoutRateCurrencySymbolTransaction
    },
    {
        header: 'Amount Due',
        dataIndex: 'amountdue',
        renderer: WtfGlobal.withoutRateCurrencySymbol

    }, {
        header: 'Enter Amount',
        dataIndex: 'enteramount',
        disable: false,
        renderer: WtfGlobal.withoutRateCurrencySymbol,
        editor: new Wtf.form.NumberField({
            allowBlank: false,
            value: 0,
            maxLength: 15, //
            decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        })



    }, {
        header: WtfGlobal.getLocaleText("acc.setupWizard.curEx"),
        dataIndex: 'exchangeratefortransaction',
        hidelabel: false,
        hidden: false,
        renderer: this.conversionFactorRenderer,
        editor: this.exchangeratefortransaction = new Wtf.form.NumberField({
            decimalPrecision: 10,
            allowNegative: false,
            validator: function(val) {
                if (val != 0) {
                    return true;
                } else {
                    return false;
                }
            }
        })

    });
    
    /**
     * If discount on Payment term is enabled in company preferences adding discount and Amount due after discount colomn in payment grid.
     * ERM-981.
     */
    if (CompanyPreferenceChecks.discountOnPaymentTerms()) {
        this.discountEditor = new Wtf.form.NumberField({
            value: 0,
            maxLength: 15, 
            allowNegative: false,
            decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.discount"), //"Discount",
            align: 'right',
            dataIndex: 'discountname',
            editor: this.discountEditor,
            renderer: function (value, m, rec) {
                if ((value == undefined || value == "") && (rec.discountname == 0 || rec.discountname == undefined)) {
                    value = 0;
                }
                return WtfGlobal.withoutRateCurrencySymbol(value, m, rec);
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscount"), //"Amount Due after Discount",
            align: 'right',
            dataIndex: 'amountdueafterdiscount',
            renderer: function (value, m, rec) {
                if (value == undefined || value == "") {
                    value = 0;
                }
                return WtfGlobal.withoutRateCurrencySymbol(value, m, rec);
            }
        });
    }
        
    columnArr = WtfGlobal.appendCustomColumn(columnArr, GlobalColumnModel[config.moduleid]);
    var CustomtotalStoreCount = 0;
    var CustomloadedStoreCount = 0;

    for (var j = 0; j < columnArr.length; j++) {
        if (columnArr[j].dataIndex.indexOf('Custom_') != -1 && (columnArr[j].fieldtype === 4 || columnArr[j].fieldtype === 7)) {
            CustomtotalStoreCount++;
            columnArr[j].editor.store.on('load', function() {
                CustomloadedStoreCount++;
                if (CustomtotalStoreCount === CustomloadedStoreCount) {
                    this.fireEvent('updatecustomdata', this);
                    this.getView().refresh();
                }
            }, this)
        }

    }

    var colModelArray = GlobalColumnModel[config.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray, this.store);

    var Record = this.store.reader.recordType, f = Record.prototype.fields, fi = f.items, fl = f.length;
    var values = {}, blankObj = {};
    for (var j = 0; j < fl; j++) {
        f = fi[j];
        if (f.name != 'rowid') {
            blankObj[f.name] = '';
            if (!Wtf.isEmpty(f.defValue))
                blankObj[f.name] = f.convert((typeof f.defValue == "function" ? f.defValue.call() : f.defValue));
        }
    }
    var invoiceNos = "";
    for (var j = 0; j < this.recArray.length; j++) {
        this.gridrec = JSON.clone(blankObj);
//        blankObj = {
        this.gridrec.documentno = this.recArray[j].billno;
        this.gridrec.vendor = this.recArray[j].personname;
        this.gridrec.invoicedate = new Date(this.recArray[j].date);
        this.gridrec.amountdue = this.recArray[j].amountdueinbase;
        this.gridrec.enteramount = this.recArray[j].amountdueinbase;
        this.gridrec.amountDueOriginal = this.recArray[j].amountdue;
        this.gridrec.exchangeratefortransaction = this.recArray[j].exchangeratefortransaction;
        this.gridrec.currencysymboltransaction = this.recArray[j].currencysymbol;
        this.gridrec.transactionAmount = this.recArray[j].amount;
        this.gridrec.currencysymbol = WtfGlobal.getCurrencySymbol();
        this.gridrec.documentid = this.recArray[j].billid;
        this.gridrec.accid = this.recArray[j].personid;
        this.gridrec.bankname = this.recArray[j].bankname;
        
        /**
         * Calculating discount and assigning discount on the basis of Applicable days and discount master.
         * If discount amount is greater than amount due of invoice then returning the invoice nos and displaying it to user.
         * ERM-981
         */       
        invoiceNos += this.assignAndCalculateDiscount(this.gridrec, this.recArray[j]);
        var newrec = new Record(this.gridrec);
//        var record = new this.store.recordType(this.gridrec, count);
        this.store.add(newrec);
        count++;
    }
    if (invoiceNos != "") {
        invoiceNos = invoiceNos.substring(0, invoiceNos.length-1);
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText({
                key: "acc.receiptpayment.greaterdiscounterrormessageinbulk",
                params: [invoiceNos]
        })], 1);
    }


    this.cm = new Wtf.grid.ColumnModel(columnArr);


    this.summary = new Wtf.ux.grid.GridSummary();
    Wtf.apply(this, {
        store: this.store,
        stripeRows: true,
        cm: this.cm
    });


    Wtf.bulkPaymentGrid.superclass.constructor.call(this, config);
    this.addEvents({
        'datachanged': true,
        'updatecustomdata': true
    });

    this.on('afteredit', this.fireAmountChange, this);
    this.on('populateDimensionValue', this.populateDimensionValueingrid, this);
}

Wtf.extend(Wtf.bulkPaymentGrid, Wtf.grid.EditorGridPanel, {
    disabledClass: "newtripcmbss",
    updateConfig: function(isPaymentMethodChanged, paymentMethodType, paymentMethodAccountId, bankName,paymentMethodRec ,externalcurrencyrate) {
        var columnArr = [];

        this.externalcurrencyrate=externalcurrencyrate;
        this.paymentMethodRec=paymentMethodRec;
        this.paymentMethodAccountId = paymentMethodAccountId;
        this.isPaymentMethodChanged = isPaymentMethodChanged;
        this.paymentMethodType = paymentMethodType;
        this.bankName = bankName;

        if (isPaymentMethodChanged && (paymentMethodType == 1 || paymentMethodType == 2)) {



            this.sequenceFormatRec = new Wtf.data.Record.create([
                {
                    name: 'id'
                },
                {
                    name: 'value'
                },
                {
                    name: 'accid'
                }
            ]);
            this.sequenceFormatStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    totalProperty: 'count',
                    root: "data"
                }, this.sequenceFormatRec),
                url: "ACCCompanyPref/getChequeSequenceFormatStore.do",
                baseParams: {
                    paymentMethodAccountId: paymentMethodAccountId,
                    isAllowNA: true,
                    isEdit: false, //(this.isEdit && !this.isCopyReceipt), //Only Edit case
                    isFromPaymentModule: true
                }
            });

            
            if (!this.isCustomer) {
                this.sequenceFormatStore.load();
            }
            this.sequenceFormatStore.on('load', this.setNextNumber, this);


            if (this.isCustomer) {
                this.bankRec = new Wtf.data.Record.create([
                    {name: 'id'},
                    {name: 'name'}]);


                this.bankTypeStore = new Wtf.data.Store({
                    reader: new Wtf.data.KwlJsonReader({
                        root: "data"
                    }, this.bankRec),
                    url: "ACCMaster/getMasterItems.do",
                    baseParams: {
                        mode: 112,
                        groupid: 2
                    }
                });
                this.bankTypeStore.load();
            }

            this.paymentStatusStore = new Wtf.data.SimpleStore({
                fields: ['statusValue', 'statusName'],
                data: [['Cleared', 'Cleared'], ['Uncleared', 'Uncleared']]
            });





            columnArr.push({
                header: 'Invoice Number',
                dataIndex: 'documentno'

            }, {
                header: this.isCustomer ? 'Customer Name' : 'Vendor Name',
                dataIndex: 'vendor'
            }, {
                header: 'Vendor Id',
                dataIndex: 'accid',
                hidden: true
            }, {
                header: 'Document Id',
                dataIndex: 'documentid',
                hidden: true
            },
            {
                header: 'Invoice Date',
                dataIndex: 'invoicedate',
                renderer: WtfGlobal.onlyDateRenderer
            }, {
                header: WtfGlobal.getLocaleText("acc.field.transactionAmount"),
                dataIndex: 'transactionAmount',
                width: 150,
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction
            }, {
                header: WtfGlobal.getLocaleText("acc.field.transactionAmountDue"),
                dataIndex: 'amountDueOriginal',
                hidelabel: true,
                hidden: true,
                width: 150,
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction
            },
            {
                header: 'Amount Due',
                dataIndex: 'amountdue',
                renderer: WtfGlobal.withoutRateCurrencySymbol
            }, {
                header: 'Enter Amount',
                dataIndex: 'enteramount',
                allign: 'right',
                editor: (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.AllowToEditCopy == false) ? '' : (this.isAllowedSpecificFields ? '' : new Wtf.form.FinanceNumberField({
                    allowBlank: false,
                    value: 0,
                    maxLength: 15, //
                    decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
                })),
                renderer: WtfGlobal.withoutRateCurrencySymbol
            })
            /**
             * If discount on Payment term is enabled in company preferences adding discount and Amount due after discount colomn in payment grid.
             * ERM-981.
             */
            if (CompanyPreferenceChecks.discountOnPaymentTerms()) {
                this.discountEditor = new Wtf.form.NumberField({
                    value: 0,
                    maxLength: 15,
                    allowNegative: false,
                    decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
                });
                columnArr.push({
                    header: WtfGlobal.getLocaleText("acc.field.discount"), //"Discount",
                    align: 'right',
                    dataIndex: 'discountname',
                    editor: this.discountEditor,
                    renderer: function (value, m, rec) {
                        if ((value == undefined || value == "") && (rec.discountname == 0 || rec.discountname == undefined)) {
                            value = 0;
                        }
                        return WtfGlobal.withoutRateCurrencySymbol(value, m, rec);
                    }
                }, {
                    header: WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscount"), //"Amount Due after Discount",
                    align: 'right',
                    dataIndex: 'amountdueafterdiscount',
                    renderer: function (value, m, rec) {
                        if (value == undefined || value == "") {
                            value = 0;
                        }
                        return WtfGlobal.withoutRateCurrencySymbol(value, m, rec);
                    }
                });
            }
        
            if (!this.isCustomer) {
                columnArr.push({//Only for MP
                    header: WtfGlobal.getLocaleText("acc.MissingAutoNumber.ChequeSequenceFormat"),
                    dataIndex: 'sequenceformat',
                    editor: this.typeCombo = new Wtf.form.ComboBox({
                        store: this.sequenceFormatStore,
                        name: 'typeCombo',
                        hiddenName: 'type',
                        displayField: 'value',
                        valueField: 'id',
                        mode: 'local',
                        typeAhead: true,
                        triggerAction: 'all',
                        forceSelection: true,
                        allowBlank: true,
                        emptyText: WtfGlobal.getLocaleText("acc.field.SelectSequenceFromat"),
                        listeners: {
                            'select': {
                                fn: this.getNextSequenceNumber,
                                scope: this
                            }
                        }
                    }),
                    renderer: Wtf.comboBoxRenderer(this.typeCombo)
                })
            }

            columnArr.push({
                header: 'Cheque Number',
                dataIndex: 'chequenumber',
                editor: this.documentList = new Wtf.form.TextField({
                    name: 'documentno',
                    emptyText: WtfGlobal.getLocaleText("Enter cheque No")
                })
                })

            if (this.isCustomer) {
                /*----Combo for RP & Text field for MP------- */
                columnArr.push({
                    header: WtfGlobal.getLocaleText("acc.setupWizard.BankName"),
                    dataIndex: 'paymentthrough',
                    editor: this.typeCombo = new Wtf.form.ComboBox({
                        name: "typeCombo",
                        store: this.bankTypeStore,
                        anchor: '90%',
                        listWidth: 188,
                        maxHeight: 220,
                        valueField: 'name',
                        displayField: 'name',
                        id: 'paymentthrough' + this.heplmodeid + this.id,
                        mode: 'local',
                        typeAhead: true, //ERM-66
                        minChars: 1,
                        triggerAction: 'all',
                        forceSelection: true
                    })
                })

            } else {
                columnArr.push({
                    header: 'Bank Name',
                    dataIndex: 'paymentthrough'
                })

            }

            columnArr.push({
                header: 'Cheque Date',
                dataIndex: 'postdate',
                renderer: WtfGlobal.onlyDateRenderer
            }, {
                header: WtfGlobal.getLocaleText("acc.mp.pmtstatus") + "*", //"Payment Status*",
                dataIndex: 'paymentstatus',
                editor: this.paymentStatus = new Wtf.form.ComboBox({
                    store: this.paymentStatusStore,
                    //fieldLabel: WtfGlobal.getLocaleText("acc.mp.pmtstatus") + "*", //"Payment Status*",
                    id: 'paymentstatus' + this.heplmodeid + this.id,
                    name: 'paymentstatus',
                    displayField: 'statusName',
                    value: 'Uncleared',
                    disabled: this.isAccount,
                    editable: false,
                    //allowBlank:false,
                    anchor: this.isReceipt ? "80%" : "90.2%",
                    valueField: 'statusValue',
                    mode: 'local',
                    /*Commented for ticket ERM-1004
                    triggerAction: 'all',*/
                    listeners: {
                        'select': {
                            fn: this.showCleareanceDate,
                            scope: this
                        }
                    }
                })
            }, {
                header: WtfGlobal.getLocaleText("acc.nee.46.Description"),
                dataIndex: 'description',
                editor: this.description = new Wtf.form.TextArea({
                    name: 'description',
                    value: '',
                    emptyText: WtfGlobal.getLocaleText("Enter Cheque No/Description")
                })
            }, {
                header: WtfGlobal.getLocaleText("acc.bankReconcile.clearanceDate") + "*", //"Clearance Date*",
                dataIndex: 'clearancedate',
                hidden: true,
                editor: this.clearanceDate = new Wtf.ServerDateField({
                }),
                //format: WtfGlobal.getOnlyDateFormat(),
                renderer: WtfGlobal.onlyDateRenderer
            }, {
                header: WtfGlobal.getLocaleText("acc.setupWizard.curEx"),
                dataIndex: 'exchangeratefortransaction',
                hidelabel: false,
                hidden: false,
                renderer: this.conversionFactorRenderer,
                editor: this.exchangeratefortransaction = new Wtf.form.NumberField({
                    decimalPrecision: 10,
                    allowNegative: false,
                    validator: function(val) {
                        if (val != 0) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
            });
        } else {
            columnArr.push({
                header: 'Invoice Number',
                dataIndex: 'documentno',
                width: 80,
                pdfwidth: 75

            }, {
                header: this.isCustomer ? 'Customer Name' : 'Vendor Name',
                dataIndex: 'vendor'
            }, {
                header: 'Vendor Id',
                dataIndex: 'accid',
                hidden: true
            }, {
                header: 'Document Id',
                dataIndex: 'documentid',
                hidden: true
            }, {
                header: 'Invoice Date',
                dataIndex: 'invoicedate',
                renderer: WtfGlobal.onlyDateRenderer
            }, {
                header: WtfGlobal.getLocaleText("acc.field.transactionAmount"),
                dataIndex: 'transactionAmount',
                width: 150,
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction
            }, {
                header: WtfGlobal.getLocaleText("acc.field.transactionAmountDue"),
                dataIndex: 'amountDueOriginal',
                hidelabel: true,
                hidden: true,
                width: 150,
                renderer: WtfGlobal.withoutRateCurrencySymbolTransaction
            }, {
                header: 'Amount Due',
                dataIndex: 'amountdue',
                renderer: WtfGlobal.withoutRateCurrencySymbol
            }, {
                header: 'Enter Amount',
                dataIndex: 'enteramount',
                disable: false,
                renderer: WtfGlobal.withoutRateCurrencySymbol
            }, {
                header: WtfGlobal.getLocaleText("acc.setupWizard.curEx"),
                dataIndex: 'exchangeratefortransaction',
                hidelabel: false,
                hidden: false,
                renderer: this.conversionFactorRenderer,
                editor: this.exchangeratefortransaction = new Wtf.form.NumberField({
                    decimalPrecision: 10,
                    allowNegative: false,
                    disable: false,
                    validator: function(val) {
                        if (val != 0) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
            });
            /**
             * If discount on Payment term is enabled in company preferences adding discount and Amount due after discount colomn in payment grid.
             * ERM-981.
             */
            if (CompanyPreferenceChecks.discountOnPaymentTerms()) {
                this.discountEditor = new Wtf.form.NumberField({
                    value: 0,
                    maxLength: 15,
                    allowNegative: false,
                    decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
                });
                columnArr.push({
                    header: WtfGlobal.getLocaleText("acc.field.discount"), //"Discount",
                    align: 'right',
                    dataIndex: 'discountname',
                    editor: this.discountEditor,
                    renderer: function (value, m, rec) {
                        if ((value == undefined || value == "") && (rec.discountname == 0 || rec.discountname == undefined)) {
                            value = 0;
                        }
                        return WtfGlobal.withoutRateCurrencySymbol(value, m, rec);
                    }
                }, {
                    header: WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscount"), //"Amount Due after Discount",
                    align: 'right',
                    dataIndex: 'amountdueafterdiscount',
                    renderer: function (value, m, rec) {
                        if (value == undefined || value == "") {
                            value = 0;
                        }
                        return WtfGlobal.withoutRateCurrencySymbol(value, m, rec);
                    }
                });
            }
        }

        /* Code for the custom column at Line Level */
        columnArr = WtfGlobal.appendCustomColumn(columnArr, GlobalColumnModel[this.moduleid]);
        var CustomtotalStoreCount = 0;
        var CustomloadedStoreCount = 0;

        for (var j = 0; j < columnArr.length; j++) {
            if (columnArr[j].dataIndex.indexOf('Custom_') != -1 && (columnArr[j].fieldtype === 4 || columnArr[j].fieldtype === 7)) {
                CustomtotalStoreCount++;
                columnArr[j].editor.store.on('load', function() {
                    CustomloadedStoreCount++;
                    if (CustomtotalStoreCount === CustomloadedStoreCount) {
                        this.fireEvent('updatecustomdata', this);
                        this.getView().refresh();
                    }
                }, this)
            }

        }


        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.store);

        this.getColumnModel().setConfig(columnArr);
        if (this.view) {
            this.getView().refresh();
        }

        this.sequenceid="";
       this.getNextSequenceNumber(undefined);
    
    },
    setNextNumber: function(config) {
        if (this.sequenceFormatStore.getCount() > 0) {
            // Create new case
            this.setSequenceFormatForCreateNewCase();
        }

    },
    setSequenceFormatForCreateNewCase: function(combo, val) {
        this.chequeEndNumberArray = [];
        var count = this.sequenceFormatStore.getCount();
        this.sequenceid = "";
        var i = 0;
        for (i = 0; i < count; i++) {
            var seqRecord = this.sequenceFormatStore.getAt(i)
            this.chequeEndNumberArray[seqRecord.data.id] = seqRecord.json.chequeEndNumber;
            if (seqRecord.json.isdefault === true) {
                this.sequenceid = seqRecord.data.id;
                //this.sequenceFormatCombobox.setValue(seqRecord.data.id)
                //this.chequeEndNumber=seqRecord.data.chequeEndNumber;
                break;
            }
        }
        if (i < count) {

            for (; i < count; i++) {
                var seqRecord = this.sequenceFormatStore.getAt(i)
                this.chequeEndNumberArray[seqRecord.data.id] = seqRecord.json.chequeEndNumber;

            }

        }

        if (this.isPaymentMethodChanged) {
            this.getNextSequenceNumber(this.sequenceFormatCombobox);
        }
    },
    getNextSequenceNumber: function(combo, val) {

        var count = 0;
        /* --------- Showing cheque information at line level-----*/
        if (combo == undefined) {

            var Record = this.store.reader.recordType, f = Record.prototype.fields, fi = f.items, fl = f.length;
            var values = {}, blankObj = {};
            for (var j = 0; j < fl; j++) {
                f = fi[j];
                if (f.name != 'rowid') {
                    blankObj[f.name] = '';
                    if (!Wtf.isEmpty(f.defValue))
                        blankObj[f.name] = f.convert((typeof f.defValue == "function" ? f.defValue.call() : f.defValue));
                }
            }


/*----------- Calculate amountdue in document currency------------ */

            if (this.isPaymentMethodChanged && this.paymentMethodType != 0) {
                this.store.removeAll();
                var invoiceNos = "";
                for (var j = 0; j < this.recArray.length; j++) {
                   
                    if (this.paymentMethodRec.json.acccurrencysymbol == this.recArray[j].currencysymbol) {//If document currency  =  Invoice currency at line level
                        this.exchangerate = 1;
                    } else if (this.paymentMethodRec.json.acccurrencysymbol == WtfGlobal.getCurrencySymbol()) {//If document currency = Base currency
                        this.exchangerate= this.recArray[j].amountdueinbase / this.recArray[j].amountdue
                    } else {//If invoice currency is not document as well as base
                        this.exchangerate = this.externalcurrencyrate * this.recArray[j].exchangeratefortransaction;
                    }
                    
                    this.gridrec = JSON.clone(blankObj);
                    this.gridrec.documentno = this.recArray[j].billno;
                    this.gridrec.vendor = this.recArray[j].personname;
                    this.gridrec.invoicedate = new Date(this.recArray[j].date);
                    this.gridrec.amountdue = getRoundedAmountValue(this.recArray[j].amountdue*this.exchangerate) ;
                    this.gridrec.enteramount = getRoundedAmountValue(this.recArray[j].amountdue*this.exchangerate) ;
                    this.gridrec.amountDueOriginal = this.recArray[j].amountdue;
                    this.gridrec.postdate = new Date();//WtfGlobal.convertToGenericDate(new Date())
                    this.gridrec.exchangeratefortransaction =this.exchangerate
                    this.gridrec.currencysymboltransaction = this.recArray[j].currencysymbol;
                    this.gridrec.transactionAmount = this.recArray[j].amount;
                    this.gridrec.currencysymbol = this.paymentMethodRec.json.acccurrencysymbol;
                    this.gridrec.sequenceformat = this.isCustomer ? "NA" : this.sequenceid;
                    this.gridrec.documentid = this.recArray[j].billid;
                    this.gridrec.accid = this.recArray[j].personid;
                    this.gridrec.paymentthrough = this.isCustomer ? "" :this.bankName;
                    this.gridrec.paymentstatus = 'Uncleared';
                    this.gridrec.description = '';
                    
                    /**
                     * Calculating discount and assigning discount on the basis of Applicable days and discount master.
                     * If discount amount is greater than amount due of invoice then returning the invoice nos and displaying it to user.
                     * ERM-981
                     */
                    invoiceNos += this.assignAndCalculateDiscount(this.gridrec, this.recArray[j]);
                    
                    var newrec = new Record(this.gridrec);

                    this.store.add(newrec);
                    count++;
                }
            } else {
                this.store.removeAll();
                for (var j = 0; j < this.recArray.length; j++) {
                    this.gridrec = JSON.clone(blankObj);

                    if (this.paymentMethodRec.json.acccurrencysymbol == this.recArray[j].currencysymbol) {//If document currency  =  Invoice currency at line level
                        this.exchangerate = 1;
                    } else if (this.paymentMethodRec.json.acccurrencysymbol == WtfGlobal.getCurrencySymbol()) {//If document currency = Base currency
                        this.exchangerate = this.recArray[j].amountdueinbase / this.recArray[j].amountdue
                    } else {//If invoice currency is not document as well as base
                        this.exchangerate = this.externalcurrencyrate * this.recArray[j].exchangeratefortransaction;
                    }


                    this.gridrec.documentno = this.recArray[j].billno;
                    this.gridrec.vendor = this.recArray[j].personname;
                    this.gridrec.invoicedate = new Date(this.recArray[j].date);
                    this.gridrec.amountdue = getRoundedAmountValue(this.recArray[j].amountdue*this.exchangerate) ;
                    this.gridrec.enteramount = getRoundedAmountValue(this.recArray[j].amountdue*this.exchangerate) ;
                    this.gridrec.amountDueOriginal = this.recArray[j].amountdue;
                    this.gridrec.exchangeratefortransaction = this.exchangerate
                    this.gridrec.currencysymboltransaction = this.recArray[j].currencysymbol;
                    this.gridrec.transactionAmount = this.recArray[j].amount;
                    this.gridrec.currencysymbol = this.paymentMethodRec.json.acccurrencysymbol;
                    this.gridrec.documentid = this.recArray[j].billid;
                    this.gridrec.accid = this.recArray[j].personid

                    /**
                     * Calculating discount and assigning discount on the basis of Applicable days and discount master.
                     * If discount amount is greater than amount due of invoice then returning the invoice nos and displaying it to user.
                     * ERM-981
                     */
                    invoiceNos += this.assignAndCalculateDiscount(this.gridrec, this.recArray[j]);


                    var newrec = new Record(this.gridrec);

                    this.store.add(newrec);
                    count++;
                }
            }
            
            if (invoiceNos != "") {
                invoiceNos = invoiceNos.substring(0, invoiceNos.length - 1);
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText({
                        key: "acc.receiptpayment.greaterdiscounterrormessageinbulk",
                        params: [invoiceNos]
                    })], 1);
            }
          /*-------------  Calculating & setting total amount at global level form---------*/
                this.totalAmount=getRoundedAmountValue(this.getMultiDebitAmount());
                this.parentObj.Amount.setValue(getRoundedAmountValue(this.totalAmount));  
        }


        if (!this.isCustomer) {
            /*1. (combo == undefined &&(this.sequenceid != "NA" && this.sequenceid != "") )->True if we select Payment method which has sequence format
             * 
             * 2.(combo != undefined && combo.value != "NA")->True if we select sequence format from cheque sequence format except NA 
             */

            if ((combo == undefined && (this.sequenceid != "NA" && this.sequenceid != "")) || (combo != undefined && combo.value != "NA")) {

                Wtf.Ajax.requestEx({
                    url: "ACCVendorPayment/getNextChequeNumber.do",
                    params: {
                        bankAccountId: this.paymentMethodAccountId,
                        sequenceformat: combo != undefined ? combo.value : this.sequenceid,
                        isfromBulkPayment: true
                    }
                }, this, function(resp) {
                    if (resp.data == "NA") {
                        this.checkNo.reset();
                        this.checkNo.enable();
                    } else {
                        var vendorArray = [];
                        var vendorKeyArray = [];
                        var chequeNoValueArray = [];
                        var nextChequeNumberInStringFormat = "";
                        var nextSequenceNumber = "";
                        var leadingZeroCount = 0;
                        var sequeceFormatCount = 0;
                        /* ---------Incremeting cheque no grouped on vendor------*/
                        this.sequenceid = combo != undefined ? combo.value : this.sequenceid//sequence id should be selected combo sequence id 

                        for (var j = 0; j < this.store.getCount(); j++) {
                            sequeceFormatCount++;//Counter to check whether all rows filled with auto generated Cheque Number

                            /*If we select Sequence format from combo manually
                             * Then Populating same sequence for all rows
                             */
                            if (combo != undefined) {
                                this.store.getAt(j).data.sequenceformat = combo.value;
                            }


                            /*---------- For the first entry at line level-------------- */
                            if (vendorArray.toString() == "") {
                                vendorArray[j] = this.store.getAt(j).data.accid;
                                vendorKeyArray[j] = this.store.getAt(j).data.accid;
                                nextSequenceNumber = resp.nextSequenceNumber;

                                leadingZeroCount = nextSequenceNumber.length;

                                /* -----------Preparing complete cheque Number-------------*/
                                if (resp.dateprefix != "" || resp.datesuffix != "" || resp.prefix != "" || resp.suffix != "") {
                                    nextChequeNumberInStringFormat = resp.dateprefix + resp.prefix + resp.dateAfterPrefix + nextSequenceNumber + resp.suffix + resp.datesuffix;
                                } else {
                                    nextChequeNumberInStringFormat = resp.prefix + nextSequenceNumber + resp.suffix;
                                }

                                this.store.getAt(j).data.chequenumber = nextChequeNumberInStringFormat;
                                chequeNoValueArray[j] = nextChequeNumberInStringFormat;
                                continue;
                            }


                            /* --------------Generating entry from 2nd rows onwards--------------------- */
                            if (vendorArray.toString() != "" && vendorArray.indexOf(this.store.getAt(j).data.accid) == -1) {//True, If cheque no is generating  for the first time for this vendor
                                var nextChequeNo = parseInt(nextSequenceNumber);


                                /*--------------- If Cheque end no reached  then setting blank value for sequenceformat & chequeNo-------- */
                                if (nextChequeNo >= this.chequeEndNumberArray[this.sequenceid]) {
                                    this.store.getAt(j).data.chequenumber = "";
                                    this.store.getAt(j).data.sequenceformat = "";

                                    /* If cheque end no reached & some cheque no is still blank then resetting all cheque no & sequence format to blank value*/
                                    if (this.store.getCount() > j) {
                                        for (var j = 0; j < this.store.getCount(); j++) {
                                            /* When selecting combo value*/
                                            if (combo)
                                                combo.setValue("");
                                            this.store.getAt(j).data.chequenumber = "";
                                            this.store.getAt(j).data.sequenceformat = "";
                                        }
                                    }
                                    if (this.view) {
                                        this.getView().refresh();
                                    }
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.common.alertmessageforresettingchequeNo")], 2);
                                    break;
                                }


                                nextChequeNo += 1;//Next cheque Nummber   
                                var numberofdigit = nextSequenceNumber.length;
                                var nextnumberLength = nextChequeNo.toString().length;

                                if (resp.showleadingzero) {

                                    /* ---------Appending leading Zero for the next Cheque Number----------- */
                                    for (var l = nextnumberLength; l < numberofdigit; l++) {
                                        if (l == nextnumberLength) {
                                            nextSequenceNumber = "0" + nextChequeNo.toString();
                                        } else {
                                            nextSequenceNumber = "0" + nextSequenceNumber;
                                        }

                                    }
                                } else {
                                    nextSequenceNumber = nextChequeNo.toString();
                                }

                                /* -----------Preparing complete cheque Number-------------*/
                                if (resp.dateprefix != "" || resp.datesuffix != "" || resp.prefix != "" || resp.suffix != "") {
                                    nextChequeNumberInStringFormat = resp.dateprefix + resp.prefix + resp.dateAfterPrefix + nextSequenceNumber + resp.suffix + resp.datesuffix;
                                } else {
                                    nextChequeNumberInStringFormat = resp.prefix + nextSequenceNumber + resp.suffix;
                                }



                                this.store.getAt(j).data.chequenumber = nextChequeNumberInStringFormat;

                                vendorKeyArray[j] = this.store.getAt(j).data.accid;//Using for the purpose of finding cheque No for already entered vendor
                                chequeNoValueArray[j] = nextChequeNumberInStringFormat;
                                vendorArray[j] = this.store.getAt(j).data.accid;
                            } else {

                                /* If Cheque Number is already generated for this particular Vendor
                                 * then setting same cheque number for this rows also
                                 */
                                var index = vendorKeyArray.indexOf(this.store.getAt(j).data.accid, 0);

                                this.store.getAt(j).data.chequenumber = chequeNoValueArray[index];
                            }

                        }


                        /*-----------If Cheque Number is generated for all rows then setting Editable false to Cheque Number column----------------   */
                        if ((this.store.getCount() == sequeceFormatCount) || (combo != undefined && (combo.getValue() != "NA" || combo.getValue() != ""))) {
                            var chequeNumber = this.getColumnModel().findColumnIndex("chequenumber")
                            this.getColumnModel().setEditable(chequeNumber, false)
                        }

                        if (this.view) {
                            this.getView().refresh();
                        }

                    }
                });

            } else  if(this.isPaymentMethodChanged && this.paymentMethodType != 0){
                /*----------- If selected Sequence Format is NA or Selected Payment method has not any Sequence Format---------- */
                for (var j = 0; j < this.store.getCount(); j++) {
                    if (this.isPaymentMethodChanged && combo == undefined) {//Selected Payment method has not any Sequence Format
                        this.store.getAt(j).data.sequenceformat = "";
                    } else {
                        this.store.getAt(j).data.sequenceformat = "NA";//Selected NA Sequence Format
                    }

                    /*------------- Cheque Number will b blanck in both the cases---------- */
                    this.store.getAt(j).data.chequenumber = "";

                }

                /*-----------If Cheque Number is NA or blank for all rows then setting Editable true to Cheque Number column----------------   */
                var chequeNumber = this.getColumnModel().findColumnIndex("chequenumber")
                this.getColumnModel().setEditable(chequeNumber, true)
                if (this.view) {
                    this.getView().refresh();
                }

            }
        } else {

            /*----------- If selected Sequence Format is NA or Selected Payment method has not any Sequence Format---------- */
            for (var j = 0; j < this.store.getCount(); j++) {


                /*------------- Cheque Number will b blank in both the cases---------- */
                this.store.getAt(j).data.chequenumber = "";

            }

            /*-----------If Cheque Number is NA or blank for all rows then setting Editable true to Cheque Number column----------------   */
            var chequeNumber = this.getColumnModel().findColumnIndex("chequenumber")
            this.getColumnModel().setEditable(chequeNumber, true)
            if (this.view) {
                this.getView().refresh();
            }
        }

    },
    conversionFactorRenderer: function(value, meta, record) {
        var currencysymbol = ((record == undefined || record.data.currencysymbol == null || record.data['currencysymbol'] == undefined || record.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : record.data['currencysymbol']);
        var currencysymboltransaction = ((record == undefined || record.data.currencysymboltransaction == null || record.data['currencysymboltransaction'] == undefined || record.data['currencysymboltransaction'] == "") ? currencysymbol : record.data['currencysymboltransaction']);
        var v = parseFloat(value);
        if (isNaN(v))
            return value;
        return "1 " + currencysymboltransaction + " = " + value + " " + currencysymbol;
    },
    isValidCreditAndDebitDetails: function() {
        var isValid = false;
        for (var i = 0; i < this.store.getCount(); i++) {
            var record = this.store.getAt(i);
            if (record.data['debit'] == false && (record.data['enteramount'] != 0)) {
                isValid = true;
                break;
            }
        }
        return isValid
    },
    getMultiDebitAmount: function() {
        var amt = 0;
        for (var i = 0; i < this.store.getCount(); i++) {

            /*------------- In case of Bulk Make Payment against Invoice, Amount will always debit type------------ */

//            if (Wtf.Countryid == Wtf.Country.INDIA || (this.paymentType == 3 && this.store.getAt(i).data['debit'] != undefined)) {
//                if (this.store.getAt(i).data['debit'] == true) {
//                    amt += WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['enteramount']);
//                } else {
//                    amt -= WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['enteramount']);
//                }
//            } else {

            amt += WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['enteramount']);
//            }

        }
        return WtfGlobal.conventInDecimalWithoutSymbol(amt);
    },
    getData: function() {
        var arr = [];


        /*  Put here custom field also*/
        for (var j = 0; j < this.store.getCount(); j++) {
            var recarr = [];
            for (var i = 0; i < this.colModel.getColumnCount(); i++) {
                if (this.colModel.getDataIndex(i).indexOf("Custom_") == 0) {

                } else {
                    if (this.colModel.getDataIndex(i).indexOf("clearancedate") == 0) {
                        recarr.push(this.colModel.getDataIndex(i) + ":" + '"' + WtfGlobal.convertToGenericDate(this.store.data.items[j].data[this.colModel.getDataIndex(i)]) + '"');
                    } else {
                        recarr.push(this.colModel.getDataIndex(i) + ":" + '"' + this.store.data.items[j].data[this.colModel.getDataIndex(i)] + '"');
                    }

                }

            }
            recarr.push("currencyid " + ":" + '"' + WtfGlobal.getCurrencyID() + '"');
            /*---------- Code for preparing custom field json for saving in Database--------- */
            recarr.push(CUSTOM_FIELD_KEY + ":" + WtfGlobal.getCustomColumnData(this.store.getAt(j).data, this.moduleid).substring(13));
            /**
             * Passing isDiscountFieldChanged flag true when user had edited the discount value calculated by system because while posting JE we need to add discount in Discount Given goes to Account which is mapped in Copmany preferences 
             * but if field is not edited then discount goes to account which is mapped in discount master.
             * ERM-981
             */
            recarr.push("isDiscountFieldChanged : "+ this.store.data.items[j].data["isDiscountFieldChanged"]);
            
            arr.push("{" + recarr.join(",") + "}");
        }


        return "[" + arr.join(',') + "]";
    },
    fireAmountChange: function(obj) {
        if (obj.field == 'enteramount') {
            /*
             *  Allow negative amount in against GL case only
             */
            if (obj.value < 0 && obj.record.data.type != this.GLType) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.amountCanNotBeNegative")], 2)
                obj.record.set('enteramount', obj.originalValue);
                this.fireEvent('datachanged', this);
                return;
            }

            if (Wtf.Countryid == Wtf.Country.INDIA && obj.originalValue != obj.record.get("enteramount")) {
                obj.record.set("appliedTDS", "");
                obj.record.set("rowdetailid", "");
                obj.record.set("tdsamount", 0);
            }
            /*
             *  Check for entered amount. It should not be greater than amount due. Applicable for Invoice and CN/DN case
             */
            if (obj.record.data.enteramount > obj.record.data.amountdue &&
                    (obj.record.data.type == this.INVType || obj.record.data.type == this.NoteType ||
                            (this.isAdvanceTypeOfRefundMode(obj.record.data.type) && obj.record.data.documentid !== '')))
            {
                var amountDueOf = "";
                if (obj.record.data.type == this.INVType) {
                    amountDueOf = WtfGlobal.getLocaleText("acc.mp.ofInvoice");
                } else if (obj.record.data.type == this.NoteType) {
                    amountDueOf = this.isCustomer ? WtfGlobal.getLocaleText("acc.mp.ofDebitNote") : WtfGlobal.getLocaleText("acc.mp.ofCreditNote");
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan") + WtfGlobal.getLocaleText("acc.customerList.gridAmountDue") + " " + amountDueOf], 2)
                if (CompanyPreferenceChecks.discountOnPaymentTerms() && obj.record.data.discountname != undefined && obj.record.data.discountname > 0.0) {
                    obj.record.set('enteramount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                    obj.record.set('amountdueafterdiscount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                } else {
                    obj.record.set('enteramount', obj.record.data['amountdue']);         // obj.originalValue will be set automatically
                }

                this.fireEvent('datachanged', this);
                return;
            }
            /*
             *  Check for entered (amount+discount). It should not be greater than amount due. Applicable for Invoice only
             */
            if (CompanyPreferenceChecks.discountOnPaymentTerms()  && obj.record.data.discountname != undefined && obj.record.data.discountname > 0.0) {
                var amountDueOf = "";
                if (((obj.record.data.enteramount + obj.record.data.discountname) > obj.record.data.amountdue) && (obj.record.data.type == this.INVType)) {
                    amountDueOf = WtfGlobal.getLocaleText("acc.mp.ofInvoice");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscounterrormsg") + " " + amountDueOf], 2);
                    obj.record.set('enteramount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                    obj.record.set('amountdueafterdiscount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                    this.fireEvent('datachanged', this);
                    return;
                } 
//                else if (obj.record.data.enteramount != obj.record.data.amountdueafterdiscount) {
//                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.receiptpayment.discountresetmessage"), function (btn) {
//                        if (btn == "yes") {
//                            obj.record.set('discountname', 0);
//                            obj.record.set('amountdueafterdiscount', getRoundedAmountValue(obj.record.data['amountdue']));
//                            this.fireEvent('datachanged', this);
//                        } else {
//                            obj.record.set('enteramount', obj.originalValue);
//                        }
//                    }, this);
//                    return;
//                }
            } else if (!CompanyPreferenceChecks.discountOnPaymentTerms() || obj.record.data.discountvalue == "") {
                obj.record.set('discountvalue', 0);
            }
            var val = 0;
            var taxamount = 0;
            val = getRoundedAmountValue(obj.record.get("enteramount"));

            if (obj.record.get("prtaxid") != undefined && obj.record.get("prtaxid") != '-1') {
                taxamount = this.calTaxAmount(obj.record);
                var taxRoundAmount = getRoundedAmountValue(taxamount);
                val += taxRoundAmount;
                if (val !== 0 && (obj.record.get("enteramount") != undefined || obj.record.get("prtaxid") != ""))
                {
                    obj.record.set("taxamount", taxRoundAmount);
                    obj.record.set("prtaxid", obj.record.get("prtaxid"));
                }
            }
            this.fireEvent('datachanged', this);
        } else if (obj.field == "clearancedate") {

            /*---------Clearance Date must not be less than Cheque Date----------  */
            if (obj.record.data.clearancedate.clearTime().getTime() < obj.record.data.postdate.clearTime().getTime()) {

                for (var storeCount = 0; storeCount < this.store.getCount(); storeCount++) {
                    this.store.getAt(storeCount).data.clearancedate = new Date();
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mprp.clearanceDateCanNotBeLessThanChequedate")], 2);

            }
            for (var storeCount = 0; storeCount < this.store.getCount(); storeCount++) {
                this.store.getAt(storeCount).data.clearancedate = obj.record.data.clearancedate;
            }

            if (this.view) {
                this.getView().refresh();
            }

        } else if (obj.field == "description") {
            var vendor = obj.record.data.vendor;
            var value = obj.value;
            for (var storeCount = 0; storeCount < this.store.getCount(); storeCount++) {
                if (this.store.getAt(storeCount).data.vendor == vendor) {
                    this.store.getAt(storeCount).data.description = value;
                }

            }

            if (this.view) {
                this.getView().refresh();
            }
        } else if (obj.field == "chequenumber") {

            var isChequeNoAlreadyEntered = false;
            /* If sequence Format of Cheque Number is NA
             * then we prompt user to select different cheque Number
             *  for different vendors
             */

            var record = this.store.getAt(obj.row);//Entered Cheque Number by User
            this.store.remove(record);
            var index = this.store.find('chequenumber', obj.value);


            if (index != -1) {
                /**
                 * Remove restrications of same cheque number for bulk Make Payment according to duplicate cheque functionality in company setting
                 * and Receive payment (No duplicate cheque functionality in company setting) SDP-12577
                 */
                if ((!this.isCustomer && Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoBlock) && obj.record.data.accid != this.store.getAt(index).data.accid) {
//              if (obj.record.data.accid != this.store.getAt(index).data.accid) {
                    this.store.insert(obj.row, record);
                    obj.record.set('chequenumber', "");
                    isChequeNoAlreadyEntered = true;
                } else {
                    this.store.insert(obj.row, record);
                }
            } else {
                this.store.insert(obj.row, record);
            }

            if (isChequeNoAlreadyEntered) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("Please enter unique cheque no,As it is already entered for another Vendor")], 2);
            }


        } else if (this.isCustomer && obj.field == "paymentthrough") {//Execute only in case of Bulk Receive Payment

            for (var storeCount = 0; storeCount < this.store.getCount(); storeCount++) {
                this.store.getAt(storeCount).data.paymentthrough = obj.record.data.paymentthrough;
            }

            if (this.view) {
                this.getView().refresh();
            }

        } else if(obj.field == "discountname"){
            /*
             *  Calculating Enter amount when user manually changes the discount field enter amount = (amountdue of invoice - entried discount amount) and show confirmation box if user had changed discount amount which system had calculated.
             *  ERM-981
             */
            if (CompanyPreferenceChecks.discountOnPaymentTerms() && obj.record.data.discountname != undefined && obj.record.data.discountname > 0.0) {
                var amountDueOf = "";
                var isDiscountFieldChanged = false;
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.bulkpayment.discountresetmessage"), function (btn) {
                    if (btn == "yes") {
                        isDiscountFieldChanged = true;
                        if ((obj.record.data.discountname > obj.record.data.amountdue) && (obj.record.data.type == this.INVType)) {
                            amountDueOf = WtfGlobal.getLocaleText("acc.mp.ofInvoice");
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscounterrormsginbulk") + " " + amountDueOf], 2);
                            obj.record.set('discountname', obj.originalValue);
                        } else if (((obj.record.data.enteramount + obj.record.data.discountname) > obj.record.data.amountdue) && (obj.record.data.type == this.INVType)) {
//                            amountDueOf = WtfGlobal.getLocaleText("acc.mp.ofInvoice");
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscounterrormsginbulk") + " " + amountDueOf], 2);
                            var enterAmt = (obj.record.data['amountdue'] - obj.record.data['discountname']) > 0 ? (obj.record.data['amountdue'] - obj.record.data['discountname']) : 0.0;
                            obj.record.set('enteramount', getRoundedAmountValue(enterAmt));
                            obj.record.set('amountdueafterdiscount', getRoundedAmountValue(enterAmt));
                        } else {
                            obj.record.set('enteramount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                            obj.record.set('amountdueafterdiscount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                        }
                        obj.record.set('isDiscountFieldChanged', isDiscountFieldChanged);
                        this.fireEvent('datachanged', this);
                        return;
                    } else {
                        obj.record.set('discountname', obj.originalValue);
                        obj.record.set('isDiscountFieldChanged', isDiscountFieldChanged);
                    }
                }, this);
            }
        } if (obj.field == 'exchangeratefortransaction') {
            var rec = obj.record.data;
            var amountDueOriginal = 0;
            var exchangeRate = 0;
            amountDueOriginal = parseFloat(rec.amountDueOriginal);
            exchangeRate = rec.exchangeratefortransaction;
            var discount = 0.0;
            if (exchangeRate != '') {
                obj.record.set("amountdue", getRoundedAmountValue(amountDueOriginal * exchangeRate));
                if (rec.amount == rec.amountDueOriginal) {
                    discount = this.calculateDiscount(rec);
                    if (getRoundedAmountValue(discount) > getRoundedAmountValue(amountDueOriginal * exchangeRate)) {
                        discount = (amountDueOriginal * exchangeRate);
                    }
                }
                discount = getRoundedAmountValue(discount);
                obj.record.set("discountname", discount);
                obj.record.set("amountdueafterdiscount", getRoundedAmountValue((amountDueOriginal * exchangeRate)) - discount);
            }
            obj.record.set("enteramount", 0);
        }


    },
    populateDimensionValueingrid: function(rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
    showCleareanceDate: function(combo) {
        if (combo.getValue() == "Cleared") {
            var columnIndex = this.getColumnModel().findColumnIndex("clearancedate");
            this.getColumnModel().setHidden(columnIndex, false)

            for (var storeCount = 0; storeCount < this.store.getCount(); storeCount++) {
                this.store.getAt(storeCount).data.paymentstatus = "Cleared";

            }
            if (this.view) {
                this.getView().refresh();
            }
        } else {
            var columnIndex = this.getColumnModel().findColumnIndex("clearancedate");
            this.getColumnModel().setHidden(columnIndex, true)
            for (var storeCount = 0; storeCount < this.store.getCount(); storeCount++) {
                this.store.getAt(storeCount).data.paymentstatus = "Uncleared";

            }
            if (this.view) {
                this.getView().refresh();
            }
        }
    },
    formValidationBeforeSave: function() {

        var isFormValid = true;
        for (var storeCount = 0; storeCount < this.store.getCount(); storeCount++) {
            if (this.store.getAt(storeCount).data.paymentstatus != undefined && this.store.getAt(storeCount).data.paymentstatus == "Cleared") {
                if (this.store.getAt(storeCount).data.clearancedate == undefined || this.store.getAt(storeCount).data.clearancedate == "") {
                    isFormValid = false;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("Please enter Clearance Date field")], 2);
                    break;
                }
            } else if (this.isPaymentMethodChanged && (this.store.getAt(storeCount).data.sequenceformat == undefined || this.store.getAt(storeCount).data.sequenceformat == "")) {
                isFormValid = false;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("Please enter Cheque Sequence Format")], 2);
                break;
            } else if (this.isPaymentMethodChanged && (this.store.getAt(storeCount).data.chequenumber == undefined || this.store.getAt(storeCount).data.chequenumber == "")) {
                isFormValid = false;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("Please enter Cheque Number")], 2);
                break;
            } else if (this.isCustomer && this.isPaymentMethodChanged && (this.store.getAt(storeCount).data.paymentthrough == undefined || this.store.getAt(storeCount).data.paymentthrough == "")) {
                isFormValid = false;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("Please enter Bank Name")], 2);
                break;
            }

        }

        return isFormValid;
    },
    /**
     * Below function calculates discount on the basis of discount master mapped to payment term and payment term mapped to invoice
     * @param {type} jsonObj
     * @param {type} exchangeratefortransaction
     * @returns {Number}
     */
    calculateDiscount: function (jsonObj,exchangeratefortransaction) {
        Date.prototype.addDays = function (days) {
            var dat = new Date(this.valueOf());
            dat.setDate(dat.getDate() + days);
            return dat;
        }
        var discount = 0;
        var exchangeRateForTransaction = exchangeratefortransaction ? exchangeratefortransaction : 1;
        if (jsonObj != undefined && jsonObj != "" && jsonObj != null) {
            var invoicecreationdate = new Date();
            if(this.isReceipt){
                invoicecreationdate = new Date(jsonObj.invoicecreationdate);
            }else{
                invoicecreationdate = new Date(jsonObj.grcreationdate);
            }
            var dateAfterAddingApplicableDays;
            var isPercentDiscount = ((typeof jsonObj.discounttype === "string") ? ((jsonObj.discounttype == "1") ? true : false) : jsonObj.discounttype);
            if (jsonObj.applicabledays != undefined && (jsonObj.applicabledays !== "") && jsonObj.applicabledays > -1) {
                dateAfterAddingApplicableDays = invoicecreationdate.addDays(jsonObj.applicabledays);
                if (dateAfterAddingApplicableDays >= this.parentObj.creationDate.getValue()) {
                    if (isPercentDiscount) {
                        discount = getRoundedAmountValue((jsonObj.amount * exchangeRateForTransaction) * (jsonObj.discountvalue / 100));
                    } else {
                        discount = getRoundedAmountValue(jsonObj.discountvalue);
                    }
                }
            }
        }
        return discount;
    },
    /**
     * Calculating discount and assigning discount on the basis of Applicable days and discount master.
     * If discount amount is greater than amount due of invoice then returning the invoice nos and displaying it to user.
     * ERM-981
     * @param {type} gridrec
     * @param {type} recArray
     * @returns {String}
     */
    assignAndCalculateDiscount: function (gridrec, recArray) {
        var invoiceNos = "";
        if (this.isReceipt) {
            gridrec.invoicecreationdate = recArray.invoicecreationdate;
        } else {
            gridrec.grcreationdate = recArray.grcreationdate;
        }
        if(!Wtf.isEmpty(this.invObj) && !Wtf.isEmpty(this.invObj.discountMasterStore)){
            var discountMasterRecIndex = WtfGlobal.searchRecordIndex(this.invObj.discountMasterStore,recArray.termid,'termid');
            if (discountMasterRecIndex != -1) {
                var discountMasterRec = this.invObj.discountMasterStore.getAt(discountMasterRecIndex);
                if(discountMasterRec){
                    recArray.applicabledays=discountMasterRec.get("applicabledays");
                    recArray.discounttype=discountMasterRec.get("discounttype");
                    recArray.discountvalue=discountMasterRec.get("discountvalue");
                }
            }
        }
        gridrec.amount = recArray.amount;
        gridrec.applicabledays = recArray.applicabledays;
        gridrec.discounttype = recArray.discounttype;
        gridrec.discountvalue = recArray.discountvalue;
        var amountDueAfterDiscount = 0.0;
        var discount = 0.0;
        var exchangeRateForTransaction = gridrec.exchangeratefortransaction;
        recArray.exchangerate = gridrec.exchangeratefortransaction;
        if (CompanyPreferenceChecks.discountOnPaymentTerms() && recArray['amount'] != undefined &&
                (recArray['amount'] == recArray['amountDueOriginal'])) {
            discount = this.calculateDiscount(recArray,exchangeRateForTransaction);
            if (discount > (recArray['amountDueOriginal'] * exchangeRateForTransaction)) {
                invoiceNos += recArray.billno + ",";
                gridrec['discountname'] = getRoundedAmountValue(recArray['amountDueOriginal'] * exchangeRateForTransaction);
                gridrec['enteramount'] = gridrec['enteramount'] - gridrec['discountname'];
                amountDueAfterDiscount = getRoundedAmountValue((recArray['amount'] * exchangeRateForTransaction) - gridrec['discountname']);
            } else {
                gridrec['discountname'] = getRoundedAmountValue(discount);
                gridrec['enteramount'] = gridrec['enteramount'] - gridrec['discountname'];
                amountDueAfterDiscount = getRoundedAmountValue((recArray['amount'] * exchangeRateForTransaction) - gridrec['discountname']);
            }
        } else {
            gridrec['discountname'] = 0;
            amountDueAfterDiscount = (recArray['amountdue'] != undefined && recArray['amountdue'] != "" && recArray['amountdue'] != null) ? recArray['amountdue'] : 0.0;
        }
        gridrec.amountdueafterdiscount = amountDueAfterDiscount;
        return invoiceNos;
    }
});