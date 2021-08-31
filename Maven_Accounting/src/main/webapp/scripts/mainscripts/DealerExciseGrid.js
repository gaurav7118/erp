Wtf.account.dealerExciseGrid = function (config) {
    Wtf.apply(this, config);
    this.parentScope = config.parentScope;
    var hideManufacturerDetails = false;
    if (!Wtf.isEmpty(this.parentScope.defaultNatureOfPurchase)) {
        var indexDNOPAt = Wtf.defaultNatureOfPurchaseStore.find('id', this.parentScope.defaultNatureOfPurchase.getValue())
        if (indexDNOPAt != -1) {
            var DNOPDetails = Wtf.defaultNatureOfPurchaseStore.getAt(indexDNOPAt);
            if (DNOPDetails.data.defaultMasterItem == Wtf.DNOP.From_Agent_of_Manufacturer || DNOPDetails.data.defaultMasterItem == Wtf.DNOP.Manufacturer_Depot || DNOPDetails.data.defaultMasterItem == Wtf.DNOP.Manufacturer) {
                hideManufacturerDetails = true;
            }
        }
    }
    this.expanderFP = new Wtf.grid.RowExpander({
        tpl: new Wtf.Template(
                '<style type="text/css">',
                ' .bb td, .bb th {',
                '  border-bottom: 1px solid black !important;',
                '  font-weight: bold; width: 25%;text-align: right;',
                ' }',
                ' .cc td, .cc th {',
                ' text-align: right;',
                ' }',
                ' </style>',
                '<p style="margin-left: 1.2%"><b>Excise Details :</b></p>',
                '<p style="margin-left: 1.2%">',
                '<table style="margin-left: 1.2%; background-color: rgb(216, 216, 216); width:80%;">',
                '<tr class=\'bb\'>',
                '<td>Type of Duty</td>',
                '<td>Rate of duty</td>',
                '<td>Duty Amount</td>',
                '<td>Manufacture/Importer Duty Amount</td>',
                '</tr>',
                '<tr class=\'cc\'>',
                '<td>{termName}</td>',
                '<td>{termpercentage}</td>',
                '<td>{supplierAmount}</td>',
                '<td>{manuAmount}</td>',
                '</tr>',
                '</table>'
                )
    });
    this.gridRec = new Wtf.data.Record.create([
        {
            name: 'productname'
        }, {
            name: 'productid'
        }, {
            name: 'RG23DEntryNumber'
        }, {
            name: 'supplierInvoiceNumber'
        }, {
            name: 'supplierInvoiceDate'
        }, {
            name: 'SupplierRG23DEntry'
        }, {
            name: 'AssessableValue'
        }, {
            name: 'manufactureInvoiceNo'
        }, {
            name: 'manufactureInvoiceDate'
        }, {
            name: 'PLARG23DEntryNo'
        }, {
            name: 'billQty'
        }, {
            name: 'termName'
        }, {
            name: 'termpercentage'
        }, {
            name: 'supplierAmount'
        }, {
            name: 'manuAmount'
        }
    ]);

    var columnModelExcise = new Wtf.grid.ColumnModel([
        this.expanderFP, new Wtf.grid.RowNumberer(),
        {
            header: 'Product Name',
            width: 150,
//            hidden:true,
            align: 'center',
            dataIndex: 'productname'
        }, {
            header: 'RG 23D Entry Number',
            width: 150,
            hidden: !this.isEdit,
            align: 'center',
            dataIndex: 'RG23DEntryNumber'
        }, {
            header: 'Supplier Invoice Number',
            width: 150,
            align: 'center',
            dataIndex: 'supplierInvoiceNumber'
//            hidden:hideManufacturerDetails
        }, {
            header: "Supplier Invoice Date",
            width: 150,
            dataIndex: 'supplierInvoiceDate',
            align: 'center',
            renderer: WtfGlobal.onlyDateDeletedRenderer
//            hidden:hideManufacturerDetails
        }, {
            header: hideManufacturerDetails?"PLA/RG 23D Entry No":"Supplier RG 23D Entry No",
            width: 150,
            dataIndex: 'SupplierRG23DEntry',
            align: 'center'
//            hidden:hideManufacturerDetails
        }, {
            header: "Supplier Assessable Value",
            width: 150,
            dataIndex: 'AssessableValue',
            align: 'right',
            renderer: WtfGlobal.withCurrencyUnitPriceRenderer
//            hidden:hideManufacturerDetails
        }, {
            header: "Manufacture Invoice Number",
            width: 150,
            dataIndex: 'manufactureInvoiceNo',
            align: 'center',
            hidden:hideManufacturerDetails
        }, {
            header: "Manufacture Invoice Date",
            width: 150,
            dataIndex: 'manufactureInvoiceDate',
            align: 'center',
            renderer: WtfGlobal.getOnlyDateFormat(),
            hidden:hideManufacturerDetails
        }, {
            header: "PLA/RG 23D Entry No",
            width: 150,
            dataIndex: 'PLARG23DEntryNo',
            align: 'center',
            hidden:hideManufacturerDetails
        }, {
            header: "Billed Qty",
            width: 150,
            dataIndex: 'billQty',
            align: 'center',
            renderer: this.quantityRenderer
        }, {
            header: "Manufacture Assessable Value",
            width: 150,
            dataIndex: 'ManuAssessableValue',
            align: 'right',
            renderer: WtfGlobal.withCurrencyUnitPriceRenderer,
            hidden:hideManufacturerDetails
        }
    ]);

    this.dealerExciseInvoiceStore = new Wtf.data.GroupingStore({
        sortInfo: {
            field: 'term',
            direction: 'ASC'
        },
//        groupField : 'productname',
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.gridRec)
    });

    this.loadDealerInvoiceDetails();

    this.gridView1 = new Wtf.grid.GroupingView({
//        forceFit:true,
        showGroupName: false,
        enableNoGroups: false, // REQUIRED!
        hideGroupedColumn: false,
        emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });
    this.grid = new Wtf.grid.EditorGridPanel({
        store: this.dealerExciseInvoiceStore,
        cm: columnModelExcise,
        stripeRows: true,
        border: false,
        layout: 'fit',
        loadMask: true,
        bodyStyle: 'padding:0px',
        emptyText: WtfGlobal.getLocaleText("acc.common.norec"),
        height: 332,
        plugins: this.expanderFP,
        viewConfig: this.gridView1
    });

    this.exciseDealerDetailsGrid = new Wtf.Window({
        modal: true,
        closeAction: 'hide',
        closable: false,
        id: 'exciseDealerDetailsGrid' + this.id,
        title: "Dealer Excise Details",
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        buttonAlign: 'right',
        autoScroll: true,
        width: 800,
        height: 500,
        scope: this,
        items: [{
                region: "north",
                height: 80,
                border: false,
                bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
                html: getTopHtml("Confirm Dealer Excise Details", "Please confirm dealer details for each product", '../../images/accounting_image/tax.gif', true)
            }, {
                region: "center",
                bodyStyle: 'padding:10px 10px 10px 10px;',
                baseCls: 'bckgroundcolor',
                items: [
                    this.grid
                ]
            }],
        buttons:
                [{
                        text: "Confirm", //'Save',
                        id: "dealerExciseDetails",
                        scope: this,
                        handler: function ()
                        {
                            var proceedToSave = false;
                            proceedToSave = this.checkForDealerExciseNumber();
                            if(proceedToSave){
                                this.exciseDealerDetailsGrid.close();
                                this.parentScope.checkMemo(this.param1, this.param2, this.param3);
                            } else{
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.grid.dealerExcisedetails.alert")], 2);
                            }
                        }
                    }, {
                        text: this.viewMode ? WtfGlobal.getLocaleText('acc.common.close') : WtfGlobal.getLocaleText('acc.field.Cancel'), //'Cancel',
                        scope: this,
                        handler: function ()
                        {
                            this.parentScope.enableSaveButtons();
                            this.exciseDealerDetailsGrid.close();
                        }
                    }]
    });

    this.exciseDealerDetailsGrid.show();

    this.grid.getView().refresh();

    Wtf.account.dealerExciseGrid.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.dealerExciseGrid, Wtf.Panel, {
    onRender: function (config) {
        Wtf.account.dealerExciseGrid.superclass.onRender.call(this, config);
    },
    checkForDealerExciseNumber: function () {
        var productgridStore = this.productGrid.getStore();
        var isDealerExciseRegistered = true;
        if (productgridStore.getCount() - 1 > 0) {
            for (var x = 0; x < productgridStore.getCount() - 1; x++) {
                var record = productgridStore.getAt(x);
                var dealerExciseDetails = eval(record.data.dealerExciseDetails);
                var lineTermdetails = eval(record.data.dealerExciseTerms);
                if (!Wtf.isEmpty(dealerExciseDetails) && !Wtf.isEmpty(lineTermdetails)) {
                    for (var y = 0; y < dealerExciseDetails.length; y++) {
                        if(!dealerExciseDetails[y].RG23DEntryNumber){
                            isDealerExciseRegistered = false;
                            break;  
                        }
                    }
                } 

            }
        }
        return isDealerExciseRegistered;
    },
    loadDealerInvoiceDetails: function () {
        var productgridStore = this.productGrid.getStore();
        if (productgridStore.getCount() - 1 > 0) {
            for (var x = 0; x < productgridStore.getCount() - 1; x++) {
                var record = productgridStore.getAt(x);
                var productName = record.data.productname;
                var productid = record.data.productid;
                var dealerExciseDetails = eval(record.data.dealerExciseDetails);
                var lineTermdetails = eval(record.data.dealerExciseTerms);
                if (!Wtf.isEmpty(dealerExciseDetails) && !Wtf.isEmpty(lineTermdetails)) {
                    for (var y = 0; y < dealerExciseDetails.length; y++) {
                        var recs = {
                            "productname": productName,
                            "productid": productid,
                            "RG23DEntryNumber": dealerExciseDetails[y].RG23DEntryNumber,
                            "supplierInvoiceNumber": this.parentScope != undefined ? this.parentScope.Number.getValue() : "",
                            "supplierInvoiceDate": this.parentScope != undefined ? this.parentScope.billDate.getValue() : undefined,
                            "SupplierRG23DEntry": dealerExciseDetails[y].SupplierRG23DEntry,
                            "AssessableValue": dealerExciseDetails[y].AssessableValue,
                            "manufactureInvoiceNo": dealerExciseDetails[y].ManuInvoiceNumber,
                            "manufactureInvoiceDate": !Wtf.isEmpty(dealerExciseDetails[y].ManuInvoiceDate) ? dealerExciseDetails[y].ManuInvoiceDate : undefined,
                            "PLARG23DEntryNo": dealerExciseDetails[y].PLARG23DEntry,
                            "billQty": record != undefined ? record.data.baseuomquantity : "",
                            "ManuAssessableValue": dealerExciseDetails[y].ManuAssessableValue,
                            "dealerExciseTerms":record.data.dealerExciseTerms,
                            "company":companyid
                        };
                        this.dealerExciseInvoiceStore.add(new this.gridRec(recs));
                    }
                } else {
                    var recs = {
                        "productname": productName,
                        "productid": productid,
                        "supplierInvoiceNumber": this.parentScope != undefined ? this.parentScope.Number.getValue() : "",
                        "supplierInvoiceDate": this.parentScope != undefined ? this.parentScope.billDate.getValue() : undefined,
                        "AssessableValue": record.data.amount,
                        "ManuAssessableValue": record.data.amount,
                        "billQty": record != undefined ? record.data.baseuomquantity : "",
                        "dealerExciseTerms":record.data.dealerExciseTerms,
                        "company":companyid
                    };
                    this.dealerExciseInvoiceStore.add(new this.gridRec(recs));
                }

                if ((this.dealerExciseInvoiceStore.find("productid", record.data.productid)) != -1) {
                    var gridRecord = this.dealerExciseInvoiceStore.getAt(this.dealerExciseInvoiceStore.find("productid", record.data.productid));
                    var recordQuantity = !Wtf.isEmpty(lineTermdetails)?lineTermdetails.length:0;
                    var termname = "";
                    var termPercentage = "";
                    var termSupplierAmount = "";
                    var termManuAmount = "";
                    if (recordQuantity != 0) {
                        for (var h = 0; h < recordQuantity; h++) {
                            if (dealerExciseDetails != "" && dealerExciseDetails != undefined) {
                                var prevDealerExciseTerms = eval(dealerExciseDetails[0].dealerExciseTerms);
                                if (prevDealerExciseTerms != "" && prevDealerExciseTerms != undefined) {
                                    var prevManufactureTermAmount = prevDealerExciseTerms[h].manufactureTermAmount;
                                    var prevTermAmount = prevDealerExciseTerms[h].termamount;
                                }
                            }
                            var termRecord = lineTermdetails[h];
                            if (termRecord.termtype == Wtf.term.Excise) {
                                termname += lineTermdetails[h].term + "<br>";
                                if (lineTermdetails[h].taxtype == 1) {
                                    termPercentage += lineTermdetails[h].termpercentage + "%<br>";
                                } else {
                                    termPercentage += lineTermdetails[h].termpercentage + "<br>";
                                }
                                var supplierAssValue = !Wtf.isEmpty(gridRecord.data.AssessableValue) ? gridRecord.data.AssessableValue : record.data.amount;
                                if(prevTermAmount != undefined && prevTermAmount != "" ){
                                    //show previously saved term amount.
                                    termSupplierAmount += getRoundedAmountValue(prevTermAmount) + "<br>";
                                } else if (lineTermdetails[h].taxtype == 1) {
                                    termSupplierAmount += getRoundedAmountValue(supplierAssValue * lineTermdetails[h].termpercentage / 100) + "<br>";
                                } else {
                                    termSupplierAmount += getRoundedAmountValue(lineTermdetails[h].termamount) + "<br>";
                                }
                                var ManuAssValue = !Wtf.isEmpty(gridRecord.data.ManuAssessableValue) ? gridRecord.data.ManuAssessableValue : record.data.amount;
                                if(prevManufactureTermAmount != undefined && prevManufactureTermAmount != "" ){
                                    //show previously saved manufacture/Importer Term amount.
                                    termManuAmount += getRoundedAmountValue(prevManufactureTermAmount) + "<br>";
                                } else if (lineTermdetails[h].taxtype == 1) {
                                    termManuAmount += getRoundedAmountValue(ManuAssValue * lineTermdetails[h].termpercentage / 100) + "<br>";
                                } else {
                                    termManuAmount += getRoundedAmountValue(lineTermdetails[h].termamount) + "<br>";
                                }
                            }
                        }
                        gridRecord.data.termName = termname;
                        gridRecord.data.termpercentage = termPercentage;
                        gridRecord.data.supplierAmount = termSupplierAmount;
                        gridRecord.data.manuAmount = termManuAmount;
                    }
                }

            }
        }
    }
});
