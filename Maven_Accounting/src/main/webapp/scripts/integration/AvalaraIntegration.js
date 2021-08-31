/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Function to get tax details from Avalara by calling their web service and to update tax details in the grid
 * This is main function from a collection of functions which are called in a sequence for Tax Calculation and for updation of taxes in a transaction
 * @param {type} scope
 * @param {type} obj
 * @param {type} productRecordsArr
 * @returns {undefined}
 */
function getTaxFromAvalaraAndUpdateGrid(scope, obj, productRecordsArr) {
    Wtf.avalaraIntegrationFunctions.checkIfAvalaraTaxCalculationIsEnabled(scope, obj, productRecordsArr);
}

/**
 * Function which creates the tooltip for tax column on line level
 * hideUnitPriceAmount -> scope flag should be passes as true if the user does not have permission to view unit price and amounts in transaction(s)
 * if hideUnitPriceAmount flag's value is passed true then amounts are hidden in the Tooltip which is created in scope function
 * @param {type} v
 * @param {type} m
 * @param {type} rec
 * @returns {String}
 */
function getToolTipOfAvalaraTerms(v, m, rec, hideUnitPriceAmount) {
    var table = "";
    var termDetails = "";
    if (!Wtf.isEmpty(rec.data.LineTermdetails)) {
        termDetails = rec.data.LineTermdetails;
    }
    if (!Wtf.isEmpty(termDetails)) {
        var symbol = !Wtf.isEmpty(rec.data.currencysymbol) ? rec.data.currencysymbol : WtfGlobal.getCurrencySymbol();
        var jsonData = eval(termDetails);//JSON.parse(termDetails);
        //headers
        table = "<table cellspacing=10>" +
                "<tr>" +
                "<td><b>Juris Type</b></td>" +
                "<td><b>Juris Code</b></td>" +
                "<td><b>Tax&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b></td>" +
                "<td><b>Tax Type</b></td>" +
                "<td><b>Rate(%)</b></td>" +
                "<td><b>Assessable Value</b></td>" +
                "<td><b>Tax Amount</b></td>" +
                "</tr>";

        //tax rows
        for (var i = 0; i < jsonData.length; i++) {
            var juristype = !Wtf.isEmpty(jsonData[i].juristype) ? jsonData[i].juristype : "";
            var juriscode = !Wtf.isEmpty(jsonData[i].juriscode) ? jsonData[i].juriscode : "";
            var term = !Wtf.isEmpty(jsonData[i].term) ? jsonData[i].term : "";
            var taxtypeName = !Wtf.isEmpty(jsonData[i].taxtypeName) ? jsonData[i].taxtypeName : "";
            var taxvalue = !Wtf.isEmpty(jsonData[i].taxvalue) ? jsonData[i].taxvalue : "";
            var assessablevalue = !Wtf.isEmpty(jsonData[i].assessablevalue) ? jsonData[i].assessablevalue : "0";
            //If user does not have permission to view unit price, then assessable amount is also hidden from user
            assessablevalue = hideUnitPriceAmount ? Wtf.UpriceAndAmountDisplayValue : WtfGlobal.conventInDecimal(assessablevalue, symbol);
            var termamount = !Wtf.isEmpty(jsonData[i].termamount) ? jsonData[i].termamount : "";
            //If user does not have permission to view unit price, then term amount is also hidden from user
            termamount = hideUnitPriceAmount ? Wtf.UpriceAndAmountDisplayValue : WtfGlobal.conventInDecimal(termamount, symbol);

            table += "<tr> <td valign=top>" + juristype + "</td>" +
                    "<td valign=top>" + juriscode + "</td>" +
                    "<td valign=top>" + term + "</td>" +
                    "<td valign=top>" + taxtypeName + "</td>" +
                    "<td valign=top>" + taxvalue + "</td>" +
                    "<td valign=top>" + assessablevalue + "</td>" +
                    "<td valign=top>" + termamount + "</td>" +
                    "</tr>";
        }
        table += "</table>";
        return " <div class='" + getButtonIconCls(Wtf.etype.termCalcWindow) + "' wtf:qtip=\"" + table + " \" wtf:qwidth=\"470\"'> </div> ";
    } else {
        return "<div class='" + getButtonIconCls(Wtf.etype.termCalcWindow) + "'></div>";
    }
}

/**
 * Function which checks whether an invoice should be allowed to edit or not
 * It checks whether the corresponding invoice on AvaTax side is locked or not
 * copyInv - Flag to be passed in callback to Edit Invoice function
 * @param {type} copyInv
 * @param {type} scope
 * @param {type} rec
 * @returns {undefined}
 */
function validateTransactionWithAvalaraAndCallBack(scope, rec, copyInv) {
    var billnumber = rec.data.billno;
    var billid = rec.data.billid;
    var moduleid = scope.moduleid;
    var docType;
    if (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Cash_Sales_ModuleId) {
        docType = Wtf.integration.avalaraSalesInvoice;
    } else if (moduleid == Wtf.Acc_Sales_Return_ModuleId) {
        docType = Wtf.integration.avalaraReturnInvoice;
    }
    var loadMask = new Wtf.LoadMask(document.body, {
        msg: WtfGlobal.getLocaleText("acc.field.LoadingMask")
    });
    loadMask.show();
    Wtf.Ajax.requestEx({
        url: "Integration/getTransaction.do",
        params: {
            integrationPartyId: Wtf.integrationPartyId.AVALARA,
            integrationOperationId: Wtf.integrationOperationId.avalara_getTransaction,
            moduleid: moduleid,
            billnumber: billnumber,
            DocCode: billnumber,
            DocType: docType,
            billid: billid
        }
    }, scope, function (response) {
        loadMask.hide();
        if (response.success && response.data) {
            if (!response.data.isLocked) {
                /**
                 * Callback appropriate function on success response
                 */
                if (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Cash_Sales_ModuleId) {
                    scope.editTransactionCheckBefore(copyInv);
                } else if (moduleid == Wtf.Acc_Sales_Return_ModuleId) {
                    scope.editOrderTransaction();
                }
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.integration.canNotBeEditedBecauseLocked")], 2);
            }
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
        }
    }, function () {
        loadMask.hide();
        //Request failure handler
        //Add code here
    });
}

Wtf.avalaraIntegrationFunctions = {
    createAvalaraConfigPanel: function (scope) {
        var avalaraConfigFormPanelItemsArr = [];
        var configJson = scope.integrationAccountDetails.configJson ? scope.integrationAccountDetails.configJson : {};//contains configuration of integration control checks
        /*
         * Check to activate/deactivate tax calculation
         */
        scope.activateTaxCalculationCheck = new Wtf.form.Checkbox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.integration.activateTaxCalculationTT") + "'>" + WtfGlobal.getLocaleText("acc.integration.activateTaxCalculation") + "</span>",
            name: Wtf.integration.taxCalculation,
            checked: configJson[Wtf.integration.taxCalculation] ? (configJson[Wtf.integration.taxCalculation] == 'on') : false,
        });
        avalaraConfigFormPanelItemsArr.push(scope.activateTaxCalculationCheck);
        /*
         * Check to activate/deactivate tax committing
         */
        scope.activateTaxCommittingCheck = new Wtf.form.Checkbox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.integration.activateTaxCommittingTT") + "'>" + WtfGlobal.getLocaleText("acc.integration.activateTaxCommitting") + "</span>",
            name: Wtf.integration.taxCommitting,
            disabled: !configJson[Wtf.integration.taxCalculation],
            checked: configJson[Wtf.integration.taxCommitting] ? (configJson[Wtf.integration.taxCommitting] == 'on') : false,
        });
        avalaraConfigFormPanelItemsArr.push(scope.activateTaxCommittingCheck);
        /**
         * If tax calculation is disabled then tax committing is also to be disabled because tax can not be committed without tax calculation
         * Also disable Tax Commit check-box if Tax Calculation check-box is unchecked
         */
        scope.activateTaxCalculationCheck.on('change', function () {
            if (scope.activateTaxCalculationCheck.getValue()) {
                scope.activateTaxCommittingCheck.enable();
            } else {
                scope.activateTaxCommittingCheck.setValue(false);
                scope.activateTaxCommittingCheck.disable();
            }
        }, scope);
        /**
         * Store for avalaraTaxAccountCombo
         */
        var avalaraTaxAccountComboStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getAccountsForCombo.do",
            baseParams: {
                mode: 2,
                ignoreAssets: true,
                ignorecustomers: true,
                ignorevendors: true,
                nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },
            Wtf.data.Record.create([
                {name: 'accname'},
                {name: 'accid'},
                {name: 'acccode'},
                {name: 'groupname'}
            ]))
        });
        /*
         * Combo to select Account For Tax
         */
        scope.avalaraTaxAccountCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.setupWizard.gridTaxAccount") + "'>" + WtfGlobal.getLocaleText("acc.setupWizard.gridTaxAccount") + "</span>",
            name: Wtf.integration.taxAccountId,
            hiddenName: Wtf.integration.taxAccountId,
            store: avalaraTaxAccountComboStore,
            valueField: 'accid',
            displayField: 'accname',
            mode: 'local',
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            width: scope.integrationPartyData.integrationConfigJson.settingsFieldWidth,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectAccount"),
            allowBlank: false,
            forceSelection: true,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'],
            triggerAction: 'all'
        });
        avalaraConfigFormPanelItemsArr.push(scope.avalaraTaxAccountCombo);
        avalaraTaxAccountComboStore.load();
        /**
         * Populate currently saved value in Account Combo after store load
         */
        avalaraTaxAccountComboStore.on('load', function () {
            if (configJson && configJson[Wtf.integration.taxAccountId]) {
                scope.avalaraTaxAccountCombo.setValue(configJson[Wtf.integration.taxAccountId])
            }
        }, scope);
        /*
         * Check to activate/deactivate address validation
         */
        scope.activateAddressValidationCheck = new Wtf.form.Checkbox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.activateAddressValidation") + "'>" + WtfGlobal.getLocaleText("acc.common.activateAddressValidation") + "</span>",
            name: Wtf.integration.addressValidation,
            checked: configJson[Wtf.integration.addressValidation] ? (configJson[Wtf.integration.addressValidation] == 'on') : false,
        });
        avalaraConfigFormPanelItemsArr.push(scope.activateAddressValidationCheck);
        /*
         * Check to activate/deactivate transaction logging
         */
        scope.transactionLoggingCheck = new Wtf.form.Checkbox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.integration.activateLogging") + "'>" + WtfGlobal.getLocaleText("acc.integration.activateLogging") + "</span>",
            name: Wtf.integration.transactionLogging,
            checked: configJson[Wtf.integration.transactionLogging] ? (configJson[Wtf.integration.transactionLogging] == 'on') : false,
        });
        avalaraConfigFormPanelItemsArr.push(scope.transactionLoggingCheck);
        /*
         * Field for Request Timeout value
         */
        scope.requestTimeoutField = new Wtf.form.NumberField({//Request Timeout field
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.integration.requestTimeoutTT") + "'>" + WtfGlobal.getLocaleText("acc.integration.requestTimeout") + "</span>",
            name: Wtf.integration.requestTimeout,
            allowBlank: true,
            value: configJson[Wtf.integration.requestTimeout] ? configJson[Wtf.integration.requestTimeout] : 300,
            maxValue: 60000,
            allowDecimal: false,
            allowNegative: false,
            width: scope.integrationPartyData.integrationConfigJson.settingsFieldWidth
        });
        avalaraConfigFormPanelItemsArr.push(scope.requestTimeoutField);
        /*
         * Form Panel with Integration Settings checks and timeout field
         */
        var avalaraConfigFormPanel = new Wtf.form.FormPanel({
            title: WtfGlobal.getLocaleText("acc.integration.integrationControls"),
            bodyStyle: "padding:10px;",
            labelWidth: scope.integrationPartyData.integrationConfigJson.settingsFieldLabelWidth,
            border: false,
            items: avalaraConfigFormPanelItemsArr
        });
        return avalaraConfigFormPanel;
    },
    createAvaTaxAdminConsoleLinkPanel: function (scope) {
        /*
         * Panel to display AvaTax Admin Console Link
         */
        var avaTaxAdminConsoleLink = "<a href=" + Wtf.integration.avalaraAdminConsoleLink + " target='blank'> " + WtfGlobal.getLocaleText("acc.integration.goToAvaTaxAdminConsolePage") + "</a>";
        var avaTaxAdminConsoleLinkPanel = new Wtf.Panel({
            title: WtfGlobal.getLocaleText("acc.integration.avaTaxAdminConsoleLink"),
            bodyStyle: "padding:10px;",
            autoHeight: true,
            layout: "column",
            border: false,
            items: [{
                    border: false,
                    html: avaTaxAdminConsoleLink
                }]
        });
        return avaTaxAdminConsoleLinkPanel;
    },
    /**
     * Function to update tax details in grid
     * @param {type} prodTermArray
     * @param {type} grid
     * @returns {undefined}
     */
    updateAvalaraTaxDetailsInGrid: function (prodTermArray, grid) {
        var gridRowCount = grid.store.getCount() - 1;//subtract 1 becuase of blank row at the end of store
        if (gridRowCount > 0) {
            if (prodTermArray.length == 0) {
                for (var i = 0; i < gridRowCount; i++) {
                    var rec = grid.store.getAt(i);
                    var termStore = [];
                    if (grid.parentObj && grid.parentObj.includingGST && grid.parentObj.includingGST.getValue() == true) {
                        grid.getColumnModel().setRenderer(grid.getColumnModel().findColumnIndex("amount"), WtfGlobal.withoutRateCurrencySymbol);
                        termStore = grid.calculateTermLevelTaxesInclusive(termStore, rec);
                    } else {
                        grid.getColumnModel().setRenderer(grid.getColumnModel().findColumnIndex("amount"), grid.calAmountWithoutExchangeRate.createDelegate(grid));
                        if (grid.isFixedAsset) {
                            termStore = calculateTermLevelTaxes(termStore, rec, undefined, true);
                        } else {
                            termStore = grid.calculateTermLevelTaxes(termStore, rec, undefined, true);
                        }
                    }
                    rec.set('LineTermdetails', Wtf.encode(termStore));
                    if (grid.moduleid && (grid.moduleid == Wtf.Acc_Delivery_Order_ModuleId || grid.moduleid == Wtf.Acc_Sales_Return_ModuleId)) {
                        updateTermDetails(grid);
                    } else {
                        grid.updateTermDetails();
                    }

                    grid.fireEvent('datachanged', grid);
                }
            } else {
                for (var index = 0; index < prodTermArray.length; index++) {
                    var prodTermDetailsObj = prodTermArray[index];
                    var rowIndex = prodTermDetailsObj.rowIndex;//Index of product row in grid store
                    var rec = grid.store.getAt(rowIndex);
                    var quantity = rec.data.quantity;
                    quantity = (quantity == "NaN" || !quantity) ? 0 : quantity;
                    var rate = getRoundofValueWithValues(rec.data.rate, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                    quantity = getRoundofValue(quantity);

                    var amount = 0;
                    amount = rate * quantity;
                    var discount = 0;//origionalAmount*rec.data.prdiscount/100   
                    if (rec.data.prdiscount > 0) {
                        var prdiscount = getRoundedAmountValue(rec.data.prdiscount);
                        if (rec.data.discountispercent == 1) {
                            discount = getRoundedAmountValue((amount * prdiscount) / 100);
                        } else {
                            discount = prdiscount;
                        }
                        amount -= discount;
                    }
                    rec.set('amount', getRoundedAmountValue(amount));
                    var LineTermdetails = prodTermDetailsObj.LineTermdetails;
                    LineTermdetails = eval(LineTermdetails);
                    /**
                     * Calculate Tax and Total amount
                     */
                    var taxamount = 0;
                    var LineTermdetailsUpdated = [];
                    var taxableAmountFromTermDetailsObj = prodTermDetailsObj.taxableAmount ? prodTermDetailsObj.taxableAmount : 0;
                    var lineAmountFromTermDetailsObj = prodTermDetailsObj.lineAmount ? prodTermDetailsObj.lineAmount : 0;
                    if (lineAmountFromTermDetailsObj != 0 && taxableAmountFromTermDetailsObj != 0) {
                        var taxableAmountFactor = (taxableAmountFromTermDetailsObj / lineAmountFromTermDetailsObj);
                        for (var j = 0; j < LineTermdetails.length; j++) {
                            var LineTermDetailObj = LineTermdetails[j];
                            var amountWithoutTax = rec.data.amountwithouttax ? rec.data.amountwithouttax : (rec.data.amountWithoutTax ? rec.data.amountWithoutTax : 0);
                            var termPercentage = LineTermDetailObj.termpercentage ? LineTermDetailObj.termpercentage : 0;
                            var taxableAmount = (taxableAmountFactor * amountWithoutTax)
                            var termamount = getRoundedAmountValue((taxableAmount * termPercentage) / 100);
                            LineTermDetailObj.assessablevalue = taxableAmount
                            LineTermDetailObj.termamount = termamount
                            taxamount += termamount;
                            LineTermdetailsUpdated.push(LineTermDetailObj);
                        }
                    }
                    rec.set('LineTermdetails', Wtf.encode(LineTermdetailsUpdated));
                    rec.set('recTermAmount', getRoundedAmountValue(taxamount));
                    rec.set('taxamount', getRoundedAmountValue(taxamount));
                    if (grid.moduleid && (grid.moduleid == Wtf.Acc_Delivery_Order_ModuleId || grid.moduleid == Wtf.Acc_Sales_Return_ModuleId)) {
                        updateTermDetails(grid);
                    } else {
                        grid.updateTermDetails();
                    }

                    grid.fireEvent('datachanged', grid);
                }
            }
        }

    },
    /**
     * Function which sends an ajax request to java side and receives tax details in response
     * After receiving success response, function 'Wtf.avalaraIntegrationFunctions.updateAvalaraTaxDetailsInGrid' is called which updates tax details in grid
     * @param {type} grid
     * @param {type} productRecordsArr
     * @param {type} extraParams
     * @param {type} shippingAddressObj
     * @returns {undefined}
     */
    getAvalaraTaxDetails: function (grid, productRecordsArr, extraParams, shippingAddressObj) {
        var billdate = extraParams.billdate;
        var currencyid = extraParams.currencyid;
        var moduleid = extraParams.moduleid;
        var customerid = extraParams.customerid;
        var salespersonid = extraParams.salespersonid;
        var avalaraExemptionCode = extraParams.avalaraExemptionCode;
        var exemptionCodeColNum = extraParams.exemptionCodeColNum;
        var loadMask = new Wtf.LoadMask(document.body, {
            msg: WtfGlobal.getLocaleText("acc.field.LoadingMask")
        });
        /**
         * ERM-294
         * Check Shipping address are avalable or not before send getTax request.
         */
        if (shippingAddressObj) {
            loadMask.show();
            Wtf.Ajax.requestEx({
                url: "Integration/getTax.do",
                params: {
                    integrationPartyId: Wtf.integrationPartyId.AVALARA,
                    integrationOperationId: Wtf.integrationOperationId.avalara_createOrAdjustTransaction, //Identifier for Integration operation which is to be performed
                    detail: JSON.stringify(productRecordsArr),
                    customerid: customerid,
                    salespersonid: salespersonid,
                    avalaraExemptionCode: avalaraExemptionCode,
                    exemptionCodeColNum: exemptionCodeColNum,
                    moduleid: moduleid,
                    shipToAddressForAvalara: JSON.stringify(shippingAddressObj),
                    commit: false,
                    currencyid: currencyid,
                    baseCurrencyCode: Wtf.account.companyAccountPref.currencycode, //Base Currency code
                    billdate: WtfGlobal.convertToGenericDate(billdate)
                }
            }, grid, function (response) {
                loadMask.hide();
                Wtf.avalaraIntegrationFunctions.updateAvalaraTaxDetailsInGrid(response.data.prodTermArray, grid);
            }, function () {
                loadMask.hide();
                //Request failure handler
                //Add code here
            });

        } else {
            if (!grid.isSaveWithoutAddressForAvalara) {
                Wtf.MessageBox.confirm("Warning", WtfGlobal.getLocaleText("acc.integration.defaultAddressErrorForAvalaraMsg"), function (btn) {
                    if (btn == 'yes') {
                        /**
                         * If user want to proceed without addresses, then
                         * we set below flag to true
                         * This flag is used to alert user about address
                         * whenever tax request is made
                         */
                        grid.isSaveWithoutAddressForAvalara = true;
                    }
                    return;
                }, grid);

            }
        }
    },
    /**
     * Function to create address JSON for ajax request
     * After creating proper address JSON, scope function calls 'Wtf.avalaraIntegrationFunctions.getAvalaraTaxDetails' function which proceeds with tax calculation
     * @param {type} grid
     * @param {type} productRecordsArr
     * @param {type} extraParams
     * @returns {undefined}
     */
    getAvalaraTaxAddressDetails: function (grid, productRecordsArr, extraParams) {
        var customerid = extraParams.customerid;
        /**
         * When address has been provided in transaction form by user, use the address from the form
         */
        if (grid.parentObj.currentAddressDetailrec) {
            var recordAddressDetailObj = grid.parentObj.currentAddressDetailrec;
            var shippingAddressObj = {
                phone: recordAddressDetailObj.shippingPhone,
                fax: recordAddressDetailObj.shippingFax,
                website: recordAddressDetailObj.shippingWebsite,
                contactPersonDesignation: recordAddressDetailObj.shippingContactPersonDesignation,
                state: recordAddressDetailObj.shippingState,
                contactPersonNumber: recordAddressDetailObj.shippingContactPersonNumber,
                city: recordAddressDetailObj.shippingCity,
                country: recordAddressDetailObj.shippingCountry,
                aliasName: recordAddressDetailObj.shippingAddressType,
                postalCode: recordAddressDetailObj.shippingPostal,
                emailID: recordAddressDetailObj.shippingEmail,
                address: recordAddressDetailObj.shippingAddress,
                county: recordAddressDetailObj.shippingCounty,
                recipientName: recordAddressDetailObj.shippingRecipientName,
                shippingRoute: recordAddressDetailObj.shippingRoute,
                isBillingAddress: false,
                mobileNumber: recordAddressDetailObj.shippingMobile,
                contactPerson: recordAddressDetailObj.shippingContactPerson
            };
            Wtf.avalaraIntegrationFunctions.getAvalaraTaxDetails(grid, productRecordsArr, extraParams, shippingAddressObj);
            /**
             * When a transaction is edited and address has not been edited by user in transaction form, then use the address saved in the transaction which is being edited
             */
        } else if (grid.parentObj.isEdit) {
            var recordObj = grid.parentObj.record.data;
            var shippingAddressObj = {
                phone: recordObj.shippingPhone,
                fax: recordObj.shippingFax,
                website: recordObj.shippingWebsite,
                contactPersonDesignation: recordObj.shippingContactPersonDesignation,
                state: recordObj.shippingState,
                contactPersonNumber: recordObj.shippingContactPersonNumber,
                city: recordObj.shippingCity,
                country: recordObj.shippingCountry,
                aliasName: recordObj.shippingAddrsCombo,
                postalCode: recordObj.shippingPostal,
                emailID: recordObj.shippingEmail,
                address: recordObj.shippingAddress,
                county: recordObj.shippingCounty,
                recipientName: recordObj.shippingRecipientName,
                shippingRoute: recordObj.shippingRoute,
                isBillingAddress: false,
                mobileNumber: recordObj.shippingMobile,
                contactPerson: recordObj.shippingContactPerson
            };
            Wtf.avalaraIntegrationFunctions.getAvalaraTaxDetails(grid, productRecordsArr, extraParams, shippingAddressObj);
            /**
             * When new transaction is created and address has not been provided in transaction form, then fetch shipping address from customer master
             */
        } else {
            var loadMask = new Wtf.LoadMask(document.body, {
                msg: WtfGlobal.getLocaleText("acc.field.LoadingMask")
            });
            loadMask.show();
            Wtf.Ajax.requestEx({
                url: "ACCCustomer/getAddresses.do",
                params: {
                    customerid: customerid,
                    isDefaultAddress: true, //Flag which indicates only default address to be fetched
                    isBillingAddress: false //Flag which indicates whether address to be fetched is billing or shipping
                }
            }, grid, function (response) {
                loadMask.hide();
                if (response.success) {
                    var shippingAddressObj = response.data[0];
                    Wtf.avalaraIntegrationFunctions.getAvalaraTaxDetails(grid, productRecordsArr, extraParams, shippingAddressObj);
                }
            }, function () {
                loadMask.hide();
                //Request failure handler
                //Add code here
            });
        }
    },
    /**
     * This function performs some manipulations on the parameters and creates new set of parameters which are passed to next function
     * After processing, function 'Wtf.avalaraIntegrationFunctions.processAvalaraTaxRequest' is called
     * @param {type} grid
     * @param {type} obj
     * @param {type} productRecordsArr
     * @returns {undefined}
     */
    processDataForAvalaraTaxCalculation: function (grid, obj, productRecordsArr) {
        var moduleid = grid.moduleid;
        if (productRecordsArr && productRecordsArr.length > 0) {
            for (var i = 0; i < productRecordsArr.lenght; i++) {
                if (productRecordsArr[i]) {
                    var isQuantityInvalid = !WtfGlobal.checkAllowZeroQuantityForProduct(moduleid) && !(productRecordsArr[i].quantity || productRecordsArr[i].dquantity);
                    var isAmountOrRateInvalid = !Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct && !(productRecordsArr[i].rate || productRecordsArr[i].amountwithouttax || productRecordsArr[i].amountWithoutTax);
                    if (isQuantityInvalid || isAmountOrRateInvalid) {
                        productRecordsArr.splice(i, 1);//If record does not have valid amount or quantity then record is removed from array
                    }
                } else {
                    productRecordsArr.splice(i, 1);//If record is undefined or null, then record is removed from array
                }
            }
        } else if (obj) {
            productRecordsArr = [];
            var productRecord = (obj.record && obj.record.data) ? obj.record.data : {};
            var isQuantityInvalid = !WtfGlobal.checkAllowZeroQuantityForProduct(moduleid) && !(productRecord.quantity || productRecord.dquantity);
            var isAmountOrRateInvalid = !Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct && !(productRecord.rate || productRecord.amountwithouttax || productRecord.amountWithoutTax);
            if (!(isQuantityInvalid || isAmountOrRateInvalid)) {//Record is added to array only if it has valid amount and quantity
                productRecord.rowIndex = obj.row;
                productRecordsArr.push(productRecord);
            }
        }

        //Further processing is done if and only if there is at least one product record in array
        if (productRecordsArr.length > 0) {
            var parentObj = grid.parentObj;
            var extraParams = {};
            extraParams.customerid = parentObj.Name.getValue();
            extraParams.salespersonid = parentObj.users.getValue();
            extraParams.billdate = parentObj.billDate.getValue();
            extraParams.currencyid = parentObj.Currency.getValue();
            extraParams.moduleid = moduleid;

            var dimensionFieldValuesArr = (parentObj.tagsFieldset && parentObj.tagsFieldset.dimensionFieldArrayValues) ? parentObj.tagsFieldset.dimensionFieldArrayValues : [];
            var dimensionFieldArr = (parentObj.tagsFieldset && parentObj.tagsFieldset.dimensionFieldArray) ? parentObj.tagsFieldset.dimensionFieldArray : [];
            for (var i = 0; i < dimensionFieldValuesArr.length; i++) {
                var diemnsionField = dimensionFieldValuesArr[i] ? dimensionFieldValuesArr[i] : {};
                var diemnsionFieldObj = dimensionFieldArr[i] ? dimensionFieldArr[i] : {};
                if (diemnsionField.fieldlabel == Wtf.integration.avalaraExemptionCode) {
                    extraParams.avalaraExemptionCode = (diemnsionFieldObj.getRawValue() != Wtf.integration.None) ? diemnsionFieldObj.getRawValue() : undefined;//Value of 'AvaTax Exemption Code' dimension
                    extraParams.exemptionCodeColNum = diemnsionField.column_number;
                }
            }

            //If Sales Retutn is linked with a transaction, then we add details of linked transaction for tax override
            if (moduleid == Wtf.Acc_Sales_Return_ModuleId && parentObj.fromLinkCombo && parentObj.PO && parentObj.PO.getValue() && parentObj.PO.getValue() != "") {
                for (var i = 0; i < productRecordsArr.length; i++) {
                    if (parentObj.fromLinkCombo.getValue() == 1) {//link with Sales Invoice
                        productRecordsArr[i].taxOverrideDocModuleId = Wtf.Acc_Invoice_ModuleId;
                    }
                    productRecordsArr[i].taxOverrideDocId = productRecordsArr[i].billid;
                    productRecordsArr[i].taxOverrideType = Wtf.integration.TaxDate;
                    productRecordsArr[i].taxOverrideReason = Wtf.integration.Return;
                }
            }

            Wtf.avalaraIntegrationFunctions.processAvalaraTaxRequest(grid, productRecordsArr, extraParams);
        }

    },
    /**
     * Intermediate function which is called during tax calculation
     * This function calls another function 'Wtf.avalaraIntegrationFunctions.getAvalaraTaxAddressDetails' which creates address JSON for ajax request to get tax details
     * @param {type} grid
     * @param {type} productRecordsArr
     * @param {type} extraParams
     * @returns {undefined}
     */
    processAvalaraTaxRequest: function (grid, productRecordsArr, extraParams) {
        Wtf.avalaraIntegrationFunctions.getAvalaraTaxAddressDetails(grid, productRecordsArr, extraParams);
    },
    /**
     * This function checks whether Tax calculation check is enabled in System Control or not
     * Taxes calculation is performed only if tax calculation check is enabled in System Control
     * If tax calculation check is found to be enabled, then the function 'Wtf.avalaraIntegrationFunctions.processDataForAvalaraTaxCalculation' is called which takes over the tax calculation task
     * @param {type} grid
     * @param {type} obj
     * @param {type} productRecordsArr
     * @returns {undefined}
     */
    checkIfAvalaraTaxCalculationIsEnabled: function (grid, obj, productRecordsArr) {
        if (Wtf.account.companyAccountPref.avalaraTaxCalculation) {
            Wtf.avalaraIntegrationFunctions.processDataForAvalaraTaxCalculation(grid, obj, productRecordsArr);
        }
    }
};
