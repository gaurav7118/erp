/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This JS file contains components which are used for Shipping Cost calculation/estimation
 * Shipping cost calculation is done in Packing report and estimation is done in Sales Order report
 * All of the components are used only when UPS Integration is enabled for a company
 */

/**
 * UPS shipment form
 * Window for shipping cost calculation via UPS REST Service integration
 */
Wtf.UpsShipmentDetailsWindow = Wtf.extend(Wtf.Window, {
    iconCls: "pwnd deskeralogoposition",
    closable: false,
    initComponent: function () {
        var shipFromAddressItemsArr = this.createShipFromAddressFields();
        var shipToAddressItemsArr = this.createShipToAddressFields();

        //ShipFrom Address fieldset
        var shipFromAddress = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.cust.shipfromAdd"),
            bodyStyle: 'padding: 5px;',
            autoHeight: true,
            labelWidth: 190,
            width: 500,
            items: shipFromAddressItemsArr
        });

        //ShipTo Address Fieldset
        var shipToAddress = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.cust.shiptoAdd"),
            bodyStyle: 'padding: 5px;',
            autoHeight: true,
            width: 500,
            labelWidth: 190,
            items: shipToAddressItemsArr
        });
        var shipmentDetailsLeftItems = this.createShipmentDetailsLeftItems();
        var shipmentDetailsRightItems = this.createShipmentDetailsRightItems();

        //Form Panel for shipment details fields
        this.shipmentDetailFormPanel = new Wtf.form.FormPanel({
            bodyStyle: 'padding: 5px;',
            labelWidth: 200,
            border: false,
            autoHeight: true,
            items: [
                {
                    layout: 'column',
                    border: false,
                    xtype: 'fieldset',
                    title: WtfGlobal.getLocaleText("acc.pickpackship.shipmentDetails"),
                    bodyStyle: 'padding: 5px;',
                    autoHeight: true,
                    width: 1000,
                    labelWidth: 200,
                    items: [{
                            layout: 'form',
                            border: false,
                            columnWidth: 0.50,
                            items: shipmentDetailsLeftItems
                        }, {
                            layout: 'form',
                            border: false,
                            columnWidth: 0.50,
                            items: shipmentDetailsRightItems
                        }]
                }, {
                    layout: 'column',
                    border: false,
                    items: [{
                            layout: 'form',
                            border: false,
                            columnWidth: 0.50,
                            items: [shipFromAddress]
                        }, {
                            layout: 'form',
                            border: false,
                            columnWidth: 0.50,
                            items: [shipToAddress]
                        }]
                }],
        });

        var itemsArr = [];
        //Window description panel
        var northPanel = new Wtf.Panel({
            region: "north",
            height: 80,
            border: true,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: getTopHtml(this.salesOrderCostEstimationFlag ? WtfGlobal.getLocaleText("acc.pickpackship.shipmentDetailsForCostEstimation") : WtfGlobal.getLocaleText("acc.pickpackship.shipmentDetailsForCostCalculation"), WtfGlobal.getLocaleText("acc.pickpackship.provideShipmentDetails"), '../../images/save.png', false, '0px 0px 0px 0px')
        });
        itemsArr.push(northPanel);

        //Panel containing shipment details form
        var centerPanel = new Wtf.Panel({
            region: "center",
            bodyStyle: 'padding: 5px; overflow: auto;',
            items: [this.shipmentDetailFormPanel]
        });
        itemsArr.push(centerPanel);

        var bttnArr = [];//buttons array
        this.packageDetailsBttn = new Wtf.Toolbar.Button({//'Provide Details of Packages' button
            text: WtfGlobal.getLocaleText("acc.pickpackship.packageDetails"),
            tooltip: WtfGlobal.getLocaleText("acc.pickpackship.packageDetailsTT"),
            scope: this,
            handler: this.packageDetailsBttnHandler
        });
        bttnArr.push(this.packageDetailsBttn);

        this.emailNotificationDetailsBttn = new Wtf.Toolbar.Button({//'Provide Details For Email Notification' button
            text: WtfGlobal.getLocaleText("acc.pickpackship.provideEmailMotificationDetails"),
            tooltip: WtfGlobal.getLocaleText("acc.pickpackship.provideEmailMotificationDetailsTT"),
            scope: this,
            handler: this.emailNotificationDetailsBttnHandler
        });
        bttnArr.push(this.emailNotificationDetailsBttn);

        this.calculateTotalCostBttn = new Wtf.Toolbar.Button({//Button to submit form and calculate shipping cost
            text: this.salesOrderCostEstimationFlag ? WtfGlobal.getLocaleText("acc.invoiceList.estimateTotalCost") : WtfGlobal.getLocaleText("acc.invoiceList.calculateTotalCost"),
            tooltip: this.salesOrderCostEstimationFlag ? WtfGlobal.getLocaleText("acc.invoiceList.estimateTotalCostTT") : WtfGlobal.getLocaleText("acc.invoiceList.calculateTotalCostTT"),
            scope: this,
            handler: this.calculateTotalCostBttnHandler
        });
        bttnArr.push(this.calculateTotalCostBttn);

        this.resetBttn = new Wtf.Toolbar.Button({//Button to reset form
            text: WtfGlobal.getLocaleText("acc.common.reset"),
            tooltip: WtfGlobal.getLocaleText("acc.common.reset"),
            scope: this,
            handler: this.resetBttnHandler
        });
        bttnArr.push(this.resetBttn);

        this.cancelBttn = new Wtf.Toolbar.Button({//Button to cancel cost calculation, hides the window
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            tooltip: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler: function () {
                this.hide();
            }
        });
        bttnArr.push(this.cancelBttn);

        var winWidth = 1050;//Width for shipping cost window
        var winHeight = 575;//Height for shipping cost window

        Wtf.apply(this, {
            width: winWidth,
            modal: true,
            constrain: true,
//            closeAction: "hide",
            resizable: false,
            bodyStyle: "background-color:#f1f1f1;",
            height: winHeight,
            layout: "border",
            autoScroll: true,
            items: itemsArr,
            buttons: bttnArr
        });

        Wtf.UpsShipmentDetailsWindow.superclass.initComponent.apply(this, arguments);

        this.shipmentDetailFormPanel.getForm().reset();
    },
    createShipmentDetailsLeftItems: function () {
        var shipmentDetailsLeftItems = [];
        var serviceTypeStore = new Wtf.data.JsonStore({//Store for service type
            fields: ['id', 'name'],
            data: Wtf.integration.upsServiceTypeArr,
            autoLoad: true
        });
        this.serviceTypeCombo = new Wtf.form.ComboBox({//Combo for service type
            name: Wtf.integration.serviceType,
            hiddenName: Wtf.integration.serviceType,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.upsServiceTypeTT") + "'>" + WtfGlobal.getLocaleText("acc.uob.serviceType") + "* " + "</span>",
            selectOnFocus: true,
            allowBlank: false,
            triggerAction: 'all',
            mode: 'local',
            store: serviceTypeStore,
            forceSelection: true,
            displayField: 'name',
            valueField: 'id',
            width: 250
        });
        shipmentDetailsLeftItems.push(this.serviceTypeCombo);

        var shipmentBillingOptionStore = new Wtf.data.JsonStore({//billing options store
            fields: ['id', 'name'],
            data: Wtf.integration.upsShipmentBillingOptionsArr,
            autoLoad: true
        });
        this.shipmentBillingOptionCombo = new Wtf.form.ComboBox({//billing option combo
            name: Wtf.integration.shipmentBillingOption,
            hiddenName: Wtf.integration.shipmentBillingOption,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.upsShipmentBillingOptionTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.upsShipmentBillingOption") + "* " + "</span>",
            selectOnFocus: true,
            allowBlank: false,
            triggerAction: 'all',
            mode: 'local',
            store: shipmentBillingOptionStore,
            forceSelection: true,
            displayField: 'name',
            valueField: 'id',
            width: 250
        });
        this.shipmentBillingOptionCombo.on('change', this.enableDisableBillingAccountFields, this);
        shipmentDetailsLeftItems.push(this.shipmentBillingOptionCombo);
        return shipmentDetailsLeftItems;
    },
    createShipmentDetailsRightItems: function () {
        var shipmentDetailsRightItems = [];
        this.billingAccountNumberField = new Wtf.form.TextField({//'Billing Account Number' Field
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.billingAccountNumberTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.billingAccountNumber") + "* " + "</span>",
            name: Wtf.integration.billingAccountNumber,
            minLength: 6,
            maxLength: 6,
            allowBlank: false,
            disabled: true,
            width: 250
        });
        shipmentDetailsRightItems.push(this.billingAccountNumberField);

        this.billingAccountPostalField = new Wtf.form.TextField({//'Billing Account Postal Code' Field
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.billingAccountPostalTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.billingAccountPostal") + "* " + "</span>",
            name: Wtf.integration.billingAccountPostal,
            minLength: 5,
            maxLength: 10,
            allowBlank: false,
            disabled: true,
            width: 250
        });
        shipmentDetailsRightItems.push(this.billingAccountPostalField);

        this.billingAccountCountryField = new Wtf.form.TextField({//'Billing Account Country Code' Field
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.billingAccountCountryTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.billingAccountCountry") + "* " + "</span>",
            name: Wtf.integration.billingAccountCountry,
            minLength: 2,
            maxLength: 2,
            allowBlank: false,
            disabled: true,
            width: 250
        });
        shipmentDetailsRightItems.push(this.billingAccountCountryField);
        return shipmentDetailsRightItems;
    },
    createShipFromAddressFields: function () {
        var shipFromAddressItemsArr = [];
        //Ship From Address Fields
        this.shipFromContactPersonName = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + this.getTooltipWithMaxLength("acc.field.ContactPersonName", 35) + "'>" + WtfGlobal.getLocaleText("acc.field.ContactPersonName") + "*" + "</span>",
            name: Wtf.integration.shipFrom_contactPersonName,
            value: this.addressesJson.shipFrom_contactPersonName,
            maxLength: 35,
            allowBlank: false,
            width: 250
        });
        shipFromAddressItemsArr.push(this.shipFromContactPersonName);

        this.shipFromName = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + this.getTooltipWithMaxLength("acc.address.AliasName", 35) + "'>" + WtfGlobal.getLocaleText("acc.address.AliasName") + "*" + "</span>",
            name: Wtf.integration.shipFrom_Name,
            value: this.addressesJson.shipFrom_Name,
            maxLength: 35,
            allowNegative: false,
            allowBlank: false,
            width: 250
        });
        shipFromAddressItemsArr.push(this.shipFromName);

        this.shipFromAddress = new Wtf.form.TextArea({
            fieldLabel: "<span wtf:qtip='" + this.getTooltipWithMaxLength("acc.address.Address", 30) + "'>" + WtfGlobal.getLocaleText("acc.address.Address") + "*" + "</span>",
            name: Wtf.integration.shipFrom_AddressLine,
            value: this.addressesJson.shipFrom_AddressLine,
            maxLength: 30,
            height: 60,
            allowBlank: false,
            allowNegative: false,
            width: 250
        });
        shipFromAddressItemsArr.push(this.shipFromAddress);

        this.shipFromCity = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + this.getTooltipWithMaxLength("acc.address.City", 30)+ "'>" + WtfGlobal.getLocaleText("acc.address.City") + "*" + "</span>",
            name: Wtf.integration.shipFrom_City,
            value: this.addressesJson.shipFrom_City,
            maxLength: 30,
            allowNegative: false,
            width: 250,
            allowBlank: false
        });
        shipFromAddressItemsArr.push(this.shipFromCity);

        this.shipFromState = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.addressStateTT") + "'>" + WtfGlobal.getLocaleText("acc.address.State") + "*" + "</span>",
            name: Wtf.integration.shipFrom_StateProvinceCode,
            value: this.addressesJson.shipFrom_StateProvinceCode,
            minLength: 2,
            maxLength: 5,
            allowNegative: false,
            width: 250,
            allowBlank: false
        });
        shipFromAddressItemsArr.push(this.shipFromState);

        this.shipFromCountry = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.addressCountryTT") + "'>" + WtfGlobal.getLocaleText("acc.address.Country") + "*" + "</span>",
            name: Wtf.integration.shipFrom_CountryCode,
            value: this.addressesJson.shipFrom_CountryCode,
            minLength: 2,
            maxLength: 2,
            allowNegative: false,
            width: 250,
            allowBlank: false
        });
        shipFromAddressItemsArr.push(this.shipFromCountry);

        this.shipFromPostal = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.address.PostalCode") + "'>" + WtfGlobal.getLocaleText("acc.address.PostalCode") + "*" + "</span>",
            name: Wtf.integration.shipFrom_PostalCode,
            value: this.addressesJson.shipFrom_PostalCode,
            minLength: 5,
            maxLength: 10,
            allowNegative: false,
            width: 250,
            allowBlank: false
        });
        shipFromAddressItemsArr.push(this.shipFromPostal);

        this.shipFromPhone = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.address.Phone") + "'>" + WtfGlobal.getLocaleText("acc.address.Phone") + "*" + "</span>",
            name: Wtf.integration.shipFrom_PhoneNumber,
            value: this.addressesJson.shipFrom_PhoneNumber,
            maxLength: 15,
            width: 250,
            allowBlank: false
        });
        shipFromAddressItemsArr.push(this.shipFromPhone);

        this.shipFromResidentialAddressCheckbox = new Wtf.form.Checkbox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.upsResidentialAddressTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.upsResidentialAddress") + " " + "</span>",
            name: Wtf.integration.shipFrom_IsResidentialAddress,
            checked: false,
        });
        shipFromAddressItemsArr.push(this.shipFromResidentialAddressCheckbox);
        return shipFromAddressItemsArr;
    },
    createShipToAddressFields: function () {
        var shipToAddressItemsArr = [];
        //Ship To address components      
        this.shipToContactPersonName = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + this.getTooltipWithMaxLength("acc.field.ContactPersonName", 35) + "'>" + WtfGlobal.getLocaleText("acc.field.ContactPersonName") + "*" + "</span>",
            name: Wtf.integration.shipTo_contactPersonName,
            value: this.addressesJson.shipTo_contactPersonName,
            maxLength: 35,
            width: 250,
            allowBlank: false
        });
        shipToAddressItemsArr.push(this.shipToContactPersonName);

        this.shipToName = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + this.getTooltipWithMaxLength("acc.address.AliasName", 35) + "'>" + WtfGlobal.getLocaleText("acc.address.AliasName") + "*" + "</span>",
            name: Wtf.integration.shipTo_Name,
            value: this.addressesJson.shipTo_Name,
            maxLength: 35,
            allowNegative: false,
            width: 250,
            allowBlank: false
        });
        shipToAddressItemsArr.push(this.shipToName);

        this.shipToAddress = new Wtf.form.TextArea({
            fieldLabel: "<span wtf:qtip='" + this.getTooltipWithMaxLength("acc.address.Address", 30) + "'>" + WtfGlobal.getLocaleText("acc.address.Address") + "*" + "</span>",
            name: Wtf.integration.shipTo_AddressLine,
            value: this.addressesJson.shipTo_AddressLine,
            maxLength: 30,
            height: 60,
            allowBlank: false,
            allowNegative: false,
            width: 250
        });
        shipToAddressItemsArr.push(this.shipToAddress);

        this.shipToCity = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + this.getTooltipWithMaxLength("acc.address.City", 30) + "'>" + WtfGlobal.getLocaleText("acc.address.City") + "*" + "</span>",
            name: Wtf.integration.shipTo_City,
            value: this.addressesJson.shipTo_City,
            maxLength: 30,
            allowNegative: false,
            width: 250,
            allowBlank: false
        });
        shipToAddressItemsArr.push(this.shipToCity);

        this.shipToState = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.addressStateTT") + "'>" + WtfGlobal.getLocaleText("acc.address.State") + "*" + "</span>",
            name: Wtf.integration.shipTo_StateProvinceCode,
            value: this.addressesJson.shipTo_StateProvinceCode,
            minLength: 2,
            maxLength: 5,
            allowNegative: false,
            width: 250,
            allowBlank: false
        });
        shipToAddressItemsArr.push(this.shipToState);

        this.shipToCountry = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.addressCountryTT") + "'>" + WtfGlobal.getLocaleText("acc.address.Country") + "*" + "</span>",
            name: Wtf.integration.shipTo_CountryCode,
            value: this.addressesJson.shipTo_CountryCode,
            minLength: 2,
            maxLength: 2,
            allowNegative: false,
            width: 250,
            allowBlank: false
        });
        shipToAddressItemsArr.push(this.shipToCountry);

        this.shipToPostal = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.address.PostalCode") + "'>" + WtfGlobal.getLocaleText("acc.address.PostalCode") + "*" + "</span>",
            name: Wtf.integration.shipTo_PostalCode,
            value: this.addressesJson.shipTo_PostalCode,
            maxLength: 5,
            maxLength: 10,
            allowNegative: false,
            width: 250,
            allowBlank: false
        });
        shipToAddressItemsArr.push(this.shipToPostal);

        this.shipToPhone = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.address.Phone") + "'>" + WtfGlobal.getLocaleText("acc.address.Phone") + "*" + "</span>",
            name: Wtf.integration.shipTo_PhoneNumber,
            value: this.addressesJson.shipTo_PhoneNumber,
            maxLength: 15,
            width: 250,
            allowBlank: false
        });
        shipToAddressItemsArr.push(this.shipToPhone);

        this.shipToResidentialAddressCheckbox = new Wtf.form.Checkbox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.upsResidentialAddressTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.upsResidentialAddress") + " " + "</span>",
            name: Wtf.integration.shipTo_IsResidentialAddress,
            checked: false
        });
        shipToAddressItemsArr.push(this.shipToResidentialAddressCheckbox);
        return shipToAddressItemsArr;
    },
    getTooltipWithMaxLength: function (toolTipMsg, maxLength) {
        if (maxLength) {
            return WtfGlobal.getLocaleText(toolTipMsg) + "<br>" + WtfGlobal.getLocaleText("acc.common.maximumLength") + ": <b>" + maxLength + "</b>";
        } else {
            return WtfGlobal.getLocaleText(toolTipMsg);
        }
    },
    enableDisableBillingAccountFields: function (field, newVal, oldVal) {//On Billing Option selection, enable/disable billing detail fields
        if (newVal !== oldVal) {
            if (newVal === Wtf.integration.upsBillShipperOption) {//For Bill Shipper; no additonal detail is required
                this.billingAccountNumberField.disable();
                this.billingAccountPostalField.disable();
                this.billingAccountCountryField.disable();
            } else if (newVal === Wtf.integration.upsBillReceiverOption) {//For Bill receiver; Account Number, and Postal Code are required
                this.billingAccountNumberField.enable();
                this.billingAccountPostalField.enable();
                this.billingAccountCountryField.disable();
            } else if (newVal === Wtf.integration.upsBillThirdPartyOption) {//For Bill Third party; Account Number, Postal Code, and Country Code are required
                this.billingAccountNumberField.enable();
                this.billingAccountPostalField.enable();
                this.billingAccountCountryField.enable();
            }
            //Reset fields on billing option change
            this.billingAccountNumberField.reset();
            this.billingAccountPostalField.reset();
            this.billingAccountCountryField.reset();
        }
    },
    resetBttnHandler: function () {
        this.shipmentDetailFormPanel.getForm().reset();
    },
    emailNotificationDetailsBttnHandler: function () {
        if (!this.emailNotificationDetailsWin) {
            this.emailNotificationDetailsWin = new Wtf.UpsEmailNotificationDetailsWindow({
                scope: this
            });
            this.emailNotificationDetailsWin.on("close", function () {//Make component undefined on close event
                this.emailNotificationDetailsWin = undefined;
            }, this);
        }
        this.emailNotificationDetailsWin.show();
    },
    packageDetailsBttnHandler: function () {
        if (!this.UpsPackageDetailsWin) {
            this.UpsPackageDetailsWin = new Wtf.UpsPackageDetailsWindow({
                scope: this,
            });
            this.UpsPackageDetailsWin.on("close", function () {//Make component undefined on close event
                this.UpsPackageDetailsWin = undefined;
            }, this);
        }
        this.UpsPackageDetailsWin.show();
    },
    calculateTotalCostBttnHandler: function () {
        if (this.shipmentDetailFormPanel.getForm().isValid()) {
            var packageDetails = undefined;//container for package details
            if (this.UpsPackageDetailsWin && this.UpsPackageDetailsWin.packageDetails) {
                packageDetails = this.UpsPackageDetailsWin.packageDetails;
            } else {//If package details have not been provided, then show message to fill details and open package details window
                this.packageDetailsBttnHandler();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.pickpackship.providePackageDetails")], 2);
                return;
            }
            Wtf.getCmp(this.parentCmpId).Store.load({//Load packing report's store
                params: {
                    start: this.start,
                    limit: this.limit,
                    ss: this.ss,
                    pagingFlag: this.pagingFlag,
                    recordIDForCostCalculation: this.recordIDForCostCalculation,
                    shipmentDetails: this.shipmentDetailFormPanel ? JSON.stringify(this.shipmentDetailFormPanel.getForm().getValues()) : undefined,
                    emailNotificationDetails: (this.emailNotificationDetailsWin && this.emailNotificationDetailsWin.emailNotificationDetails) ? JSON.stringify(this.emailNotificationDetailsWin.emailNotificationDetails) : undefined,
                    packageDetails: packageDetails
                }
            });
            this.hide();
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msgbox.2")], 2);
        }
    }
});

/**
 * Window to fill package details
 * This window opens when use clicks on 'Package Details' button in UPS shipment form
 */
Wtf.UpsPackageDetailsWindow = Wtf.extend(Wtf.Window, {
    initComponent: function () {
        var columnArr = [{
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>",
                width: 40,
                dataIndex: 'srno'//Serial Number
            }];

        var packageNumberEditor = new Wtf.form.TextField({
            name: Wtf.integration.packageNumber,
        });
        var packageNumberColumn = {
            header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.packageNumberTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.packageNumber") + "</span>",
            dataIndex: Wtf.integration.packageNumber, //Package Number - Identifier of package
            width: 120,
            editor: packageNumberEditor
        };
        columnArr.push(packageNumberColumn);

        var packagingTypeComboEditor = new Wtf.form.ComboBox({//Editor for packaging type column
            name: Wtf.integration.packagingType,
            hiddenName: Wtf.integration.packagingType,
            store: new Wtf.data.JsonStore({
                fields: ['id', 'name'],
                data: Wtf.integration.upsPackagingTypeArr,
                autoLoad: true
            }),
            mode: 'local',
            selectOnFocus: true,
            allowBlank: false,
            triggerAction: 'all',
            forceSelection: true,
            displayField: 'name',
            valueField: 'id'
        });
        var packagingTypeColumn = {
            header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.upsPackagingTypeTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.upsPackagingType") + "* " + "</span>",
            dataIndex: Wtf.integration.packagingType, //Field for packaging type
            editor: packagingTypeComboEditor,
            width: 180,
            renderer: Wtf.comboBoxRenderer(packagingTypeComboEditor)
        };
        columnArr.push(packagingTypeColumn);

        var packageWeightEditor = new Wtf.form.NumberField({
            name: Wtf.integration.packageWeight,
            regex: new RegExp("^[1-9]\\d{0,4}$"), //Regex to validate the input value must be integer between (1 and 99999)
            allowBlank: false
        });
        var packageWeightColumn = {
            header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.packageWeightTT") + "'>" + WtfGlobal.getLocaleText("erp.PackageWeightinLbs") + "* " + "</span>",
            dataIndex: Wtf.integration.packageWeight, //Field for package weight
            width: 180,
            editor: packageWeightEditor
        };
        columnArr.push(packageWeightColumn);

        var packageDimensionsEditor = new Wtf.form.TextField({
            name: Wtf.integration.packageDimensions,
            regex: new RegExp("^([1-9]\\d?\\d?)\\s?\\*\\s?([1-9]\\d?\\d?)\\s?\\*\\s?([1-9]\\d?\\d?)$"), //Regex to validate the input value
            allowBlank: false
        });
        var packageDimensionsColumn = {
            header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.packageDimensionsTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.packageDimensions") + "* " + "</span>",
            dataIndex: Wtf.integration.packageDimensions, //Field for package dimensions
            width: 180,
            editor: packageDimensionsEditor
        };
        columnArr.push(packageDimensionsColumn);

        var deliveryConfirmationTypeComboEditor = new Wtf.form.ComboBox({//Editor for delivery confirmation type column
            name: Wtf.integration.deliveryConfirmationType,
            hiddenName: Wtf.integration.deliveryConfirmationType,
            store: new Wtf.data.JsonStore({
                fields: ['id', 'name'],
                data: Wtf.integration.upsDeliveryConfirmationTypeArr,
                autoLoad: true
            }),
            mode: 'local',
            selectOnFocus: true,
            allowBlank: false,
            triggerAction: 'all',
            forceSelection: true,
            displayField: 'name',
            valueField: 'id'
        });
        var deliveryConfirmationTypeColumn = {
            header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.deliveryConfirmationTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.deliveryConfirmation") + "* " + "</span>",
            dataIndex: Wtf.integration.deliveryConfirmationType, //Field for packaging type
            editor: deliveryConfirmationTypeComboEditor,
            width: 180,
            renderer: Wtf.comboBoxRenderer(deliveryConfirmationTypeComboEditor)
        };
        columnArr.push(deliveryConfirmationTypeColumn);

        var declaredValueEditor = new Wtf.form.NumberField({
            name: Wtf.integration.declaredValue,
            minValue: 0.01,
            maxValue: 50000.00,
            allowDecimal: true,
            decimalPrecision: 2,
            allowNegative: false
        });
        var declaredValueColumn = {
            header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.declaredValueTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.declaredValue") + " </span>",
            dataIndex: Wtf.integration.declaredValue, //Field for declared value of package
            width: 180,
            editor: declaredValueEditor
        };
        columnArr.push(declaredValueColumn);

        var additionalHandlingEditor = new Wtf.form.ComboBox({//Editor for Additional Handling column
            name: Wtf.integration.additionalHandling,
            hiddenName: Wtf.integration.additionalHandling,
            store: new Wtf.data.JsonStore({
                fields: ['id', 'name'],
                data: [//Store data for Additional Handling
                    {id: "1", name: "Yes"},
                    {id: "0", name: "No"}
                ],
                autoLoad: true
            }),
            mode: 'local',
            selectOnFocus: true,
            allowBlank: false,
            triggerAction: 'all',
            forceSelection: true,
            displayField: 'name',
            valueField: 'id'
        });
        var additionalHandlingColumn = {
            header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.upsAdditionalHandlingTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.upsAdditionalHandling") + " " + "</span>",
            dataIndex: Wtf.integration.additionalHandling, //Checkbox field to check if additional handling is required for package
            editor: additionalHandlingEditor,
            width: 180,
            renderer: Wtf.comboBoxRenderer(additionalHandlingEditor)
        };
        columnArr.push(additionalHandlingColumn);

        columnArr.push({//Delete Row Action
            header: WtfGlobal.getLocaleText("acc.invoice.gridAction"), //"Action",
            dataIndex: 'deleteRow',
            align: 'center',
            width: 60,
            renderer: this.deleteRenderer.createDelegate(this)
        });

        //column model for package details grid
        this.packageDetailsCM = new Wtf.grid.ColumnModel(columnArr);
        this.packageDetailsStoreRec = new Wtf.data.Record.create([//store record data
            {name: 'srno'},
            {name: Wtf.integration.packageNumber},
            {name: Wtf.integration.declaredValue},
            {name: Wtf.integration.packagingType},
            {name: Wtf.integration.packageWeight},
            {name: Wtf.integration.packageDimensions},
            {name: Wtf.integration.deliveryConfirmationType},
            {name: Wtf.integration.additionalHandling},
            {name: 'deleteRow'}
        ]);
        this.packageDetailsStore = new Wtf.data.Store({//store for package details grid
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.packageDetailsStoreRec)
        });
        this.packageDetailsGrid = new Wtf.grid.EditorGridPanel({//package details grid
            clicksToEdit: 1,
            width: '100%',
            store: this.packageDetailsStore,
            cm: this.packageDetailsCM,
            viewConfig: {
//                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.packageDetailsGrid.on('render', this.addBlankRow, this);//To add blank row in grid when window is opened for first time
        this.packageDetailsGrid.on('rowclick', this.handleRowClick, this);
        this.packageDetailsGrid.on('afteredit', this.updateRow, this);


        var itemsArr = [];
        var northPanel = new Wtf.Panel({//Panel containing window description
            region: "north",
            height: 80,
            border: true,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: getTopHtml(WtfGlobal.getLocaleText("erp.PackageDetails"), WtfGlobal.getLocaleText("acc.pickpackship.providePackageDetails"), '../../images/save.png', false, '0px 0px 0px 0px')
        });
        itemsArr.push(northPanel);

        var centerPanelItemsArr = [];
        centerPanelItemsArr.push(this.packageDetailsGrid);
        var centerPanel = new Wtf.Panel({//Panel containing package details grid
            region: "center",
            layout: "fit",
            border: true,
            items: centerPanelItemsArr
        });
        itemsArr.push(centerPanel);


        var bttnArr = [];
        this.saveBttn = new Wtf.Toolbar.Button({//Save button
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
            tooltip: WtfGlobal.getLocaleText("acc.common.savdat"),
            scope: this,
            handler: this.saveBttnHandler
        });
        bttnArr.push(this.saveBttn);

        this.cancelBttn = new Wtf.Toolbar.Button({//Cancel button
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            tooltip: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler: function () {
                this.hide();//hide the window
            }
        });
        bttnArr.push(this.cancelBttn);

        Wtf.apply(this, {
            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("erp.PackageDetails") + "'>" + WtfGlobal.getLocaleText("erp.PackageDetails") + " " + "</span>",
            width: 1000,
            modal: true,
            constrain: true,
            resizable: false,
            autoScroll: true,
            bodyStyle: "background-color:#f1f1f1;",
            height: 375,
            layout: "border",
            items: itemsArr,
            buttons: bttnArr
        });
        this.addEvents({
            'rowdeleted': true
        });
        this.on('rowdeleted', this.afterRowDelete, this);

        Wtf.UpsPackageDetailsWindow.superclass.initComponent.apply(this, arguments);
    },
    deleteRenderer: function (v, m, rec) {
        return "<div class='" + getButtonIconCls(Wtf.etype.deletegridrow) + "' style='display:table; margin-right:auto; margin-left:auto;'></div>";
    },
    afterRowDelete: function () {
        this.packageDetailsStore.each(function (record) {
            record.set('srno', (this.packageDetailsStore.indexOf(record) + 1));//Update Serial Numbers after row-delete
        }, this);
        this.packageDetailsGrid.reconfigure(this.packageDetailsStore, this.packageDetailsCM);
    },
    handleRowClick: function (grid, rowindex, e) {
        var store = grid.getStore();
        var totalCount = store.getCount();
        var record = store.getAt(rowindex);
        //Delete row from grid on Delete-Icon click.
        //If click is in last row which is blank row, then do nothing. That's what (rowindex != totalCount - 1) condition is for.
        if (e.getTarget(".delete-gridrow") && rowindex != totalCount - 1) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function (btn) {
                if (btn != "yes")
                    return;
                store.remove(record);
                this.fireEvent('rowdeleted', this);
            }, this);
        }
    },
    updateRow: function (obj) {
        if (obj != null) {
            var record = obj.record;
            //If user edits and changes last blank row, then add a new balnk row to grid.
            if (this.packageDetailsStore.indexOf(record) == this.packageDetailsStore.getCount() - 1 && record.dirty) {
                this.addBlankRow();
            }
        }
    },
    addBlankRow: function () {//Add a blank row to grid
        var Record = this.packageDetailsStore.reader.recordType;
        var fields = Record.prototype.fields, itemsArr = fields.items, fieldsLength = fields.length;
        var recordCount = this.packageDetailsStore.getCount();
        var blankObj = {};
        for (var j = 0; j < fieldsLength; j++) {
            var field = itemsArr[j];
            if (field.name != 'deleteRow') {
                blankObj[field.name] = '';
                if (!Wtf.isEmpty(field.defValue)) {
                    blankObj[field.name] = field.convert((typeof field.defValue == "function" ? field.defValue.call() : field.defValue));
                }
            }
        }
        var newRec = new Record(blankObj);
        newRec.data.srno = (recordCount + 1);
        this.packageDetailsStore.add(newRec);
        this.packageDetailsGrid.reconfigure(this.packageDetailsStore, this.packageDetailsCM);
    },
    saveBttnHandler: function () {
        this.packageDetails = undefined;
        var arr = [];
        var recordCount = this.packageDetailsStore.getCount() - 1;//Subtract '1' because of blank row at the end of store data.
        if (recordCount == 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.pickpackship.addAtLeastOnePackageMsg")], 2);
        } else {
            for (var i = 0; i < recordCount; i++) {
                var record = this.packageDetailsStore.getAt(i);
                //Validation whether all mandatory fields have been filled or not
                if (record.get(Wtf.integration.packagingType) && record.get(Wtf.integration.packageWeight) && record.get(Wtf.integration.packageDimensions) && record.get(Wtf.integration.deliveryConfirmationType)) {
                    arr.push(i);
                }
            }
            //save package details only if number of valid records is number of records in store i.e. only if all records are valid
            if (arr.length === recordCount) {
                this.packageDetails = WtfGlobal.getJSONArray(this.packageDetailsGrid, true, arr);//this.packageDetails array contains package details which have been set by user in the grid
                this.hide();
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.pickpackship.packageRequiredFieldsMsg")], 2);
            }
        }
    }
});

/**
 * Window to fill details for email notification for shipment
 * This window opens when user clicks on 'Email Notification Details' button in UPS shipment form
 */
Wtf.UpsEmailNotificationDetailsWindow = Wtf.extend(Wtf.Window, {
    initComponent: function () {
        this.emailAddress1Field = new Wtf.form.TextField({//Email address 1
            name: Wtf.integration.emailAddress1,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.emailAddressTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.emailAddress") + " 1* " + "</span>",
            allowBlank: false,
            vtype: 'email',
            width: 230
        });
        this.emailAddress2Field = new Wtf.form.TextField({//Email address 2
            name: Wtf.integration.emailAddress2,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.emailAddressTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.emailAddress") + " 2 " + "</span>",
            vtype: 'email',
            width: 230
        });
        this.undeliverableEMailAddressField = new Wtf.form.TextField({
            name: Wtf.integration.undeliverableEMailAddress,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.emailUndeliverableTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.emailUndeliverable") + " " + "</span>",
            vtype: 'email',
            width: 230
        });
        this.fromEmailAddressField = new Wtf.form.TextField({
            name: Wtf.integration.fromEmailAddress,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.emailFromAddressTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.emailFromAddress") + "* " + "</span>",
            allowBlank: false,
            vtype: 'email',
            width: 230
        });
        this.fromEmailNameField = new Wtf.form.TextField({
            name: Wtf.integration.fromEmailName,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.emailFromNameTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.emailFromName") + " " + "</span>",
            width: 230
        });
        this.emailMemoField = new Wtf.form.TextField({
            name: Wtf.integration.emailMemo,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.emailMemoTT") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.emailMemo") + " " + "</span>",
            width: 230
        });

        this.emailNotificationDetailsFormPanel = new Wtf.form.FormPanel({//Form panel container for fields
            bodyStyle: 'padding: 5px;',
            labelWidth: 200,
            border: false,
            items: [this.emailAddress1Field, this.emailAddress2Field, this.undeliverableEMailAddressField, this.fromEmailAddressField, this.fromEmailNameField, this.emailMemoField]
        });

        var centerPanel = new Wtf.Panel({//Panel containing the form with Email details fields
            region: "center",
            bodyStyle: 'padding: 5px;',
            items: [this.emailNotificationDetailsFormPanel]
        });

        var northPanel = new Wtf.Panel({//Window description panel
            region: "north",
            height: 80,
            border: true,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: getTopHtml(WtfGlobal.getLocaleText("acc.pickpackship.emailNotificationDetails"), WtfGlobal.getLocaleText("acc.pickpackship.emailNotificationDetailText"), '../../images/save.png', false, '0px 0px 0px 0px')
        });

        this.saveBttn = new Wtf.Toolbar.Button({//Save input (only if valid) and hide window
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
            tooltip: WtfGlobal.getLocaleText("acc.common.savdat"),
            scope: this,
            handler: this.saveBttnHandler
        });

        this.cancelBttn = new Wtf.Toolbar.Button({//Hides window
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            tooltip: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler: function () {
                this.hide();
            }
        });

        this.resetBttn = new Wtf.Toolbar.Button({//Reset all fields to initial values
            text: WtfGlobal.getLocaleText("acc.common.reset"),
            tooltip: WtfGlobal.getLocaleText("acc.common.reset"),
            scope: this,
            handler: this.resetBttnHandler
        });

        Wtf.apply(this, {
            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.pickpackship.emailNotificationDetails") + "'>" + WtfGlobal.getLocaleText("acc.pickpackship.emailNotificationDetails") + " " + "</span>",
            width: 500,
            modal: true,
            constrain: true,
            resizable: false,
            bodyStyle: "background-color:#f1f1f1;",
            height: 330,
            layout: "border",
            items: [northPanel, centerPanel],
            buttons: [this.saveBttn, this.resetBttn, this.cancelBttn]
        });

        Wtf.UpsEmailNotificationDetailsWindow.superclass.initComponent.apply(this, arguments);
    },
    resetBttnHandler: function () {
        this.emailNotificationDetailsFormPanel.getForm().reset();
    },
    saveBttnHandler: function () {
        if (this.emailNotificationDetailsFormPanel.getForm().isValid()) {//validate form and if valid, set all field's values into this.emailNotificationDetails
            this.emailNotificationDetails = this.emailNotificationDetailsFormPanel.getForm().getValues();
            this.hide();
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msgbox.2")], 2);
        }
    }
});