/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This js file contains components/functions related to Integration Services
 * Components in this file are following:
 * 1. Wtf.integration.IntegrationSettingsWindow -> Window for Integration Credentials and Settings which opens from System Controls
 * @type @exp;Wtf@call;extend
 */

/**
 * This component is used to update credentials and settings for third party integration
 * @type @exp;Wtf@call;extend
 */
Wtf.integration.IntegrationSettingsWindow = Wtf.extend(Wtf.Window, {
    iconCls: "pwnd deskeralogoposition",
    initComponent: function () {
        this.integrationPartyData = Wtf.account.companyAccountPref[Wtf.integration.integrationPartiesData][this.integrationPartyId];

        var northPanel = new Wtf.Panel({
            region: "north",
            autoHeight: true,
            border: false,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: getTopHtml(this.integrationPartyData.integrationPartyName + " " + WtfGlobal.getLocaleText("acc.integration.integrationSettings"), WtfGlobal.getLocaleText("acc.integration.integrationSettingsTT"), '../../images/save.png', false, '0px 0px 0px 0px')
        });

        var centerPanelItemsArr = [];

        this.credentialsFromPanel = this.createCredentialsPanel();
        centerPanelItemsArr.push(this.credentialsFromPanel);

        if (this.integrationPartyId === Wtf.integrationPartyId.AVALARA) {
            this.avalaraConfigFormPanel = Wtf.avalaraIntegrationFunctions.createAvalaraConfigPanel(this);
            centerPanelItemsArr.push(this.avalaraConfigFormPanel);
            this.avaTaxAdminConsoleLinkPanel = Wtf.avalaraIntegrationFunctions.createAvaTaxAdminConsoleLinkPanel(this);
            centerPanelItemsArr.push(this.avaTaxAdminConsoleLinkPanel);
        }

        var centerPanel = new Wtf.Panel({
            autoHeight: true,
            region: "center",
            items: centerPanelItemsArr
        });

        var bttnArr = this.createToolbarButtons();

        //Loadmask to be used while Ajax request processing
        this.loadMask = new Wtf.LoadMask(document.body, {
            msg: WtfGlobal.getLocaleText("acc.field.LoadingMask")
        });

        Wtf.apply(this, {
            title: this.integrationPartyData.integrationPartyName + " " + WtfGlobal.getLocaleText("acc.integration.integrationSettings"),
            autoWidth: true,
            modal: true,
            constrain: true,
            closeAction: "hide",
            resizable: false,
            bodyStyle: "background-color:#f1f1f1;",
            autoHeight: true,
            items: [northPanel, centerPanel],
            buttons: (bttnArr && (bttnArr.length != 0)) ? bttnArr : undefined
        });

        Wtf.integration.IntegrationSettingsWindow.superclass.initComponent.apply(this, arguments);
    },
    createCredentialsPanel: function () {
        var credentialsFromPanelItemsArr = [];

        var settingsFieldsData = this.integrationPartyData.integrationConfigJson.settingsFieldsData;
        if (settingsFieldsData) {
            for (var i = 0; i < settingsFieldsData.length; i++) {
                var settingsFieldData = settingsFieldsData[i];
                if (settingsFieldData) {
                    var additionalConfig = {
                        width: this.integrationPartyData.integrationConfigJson.settingsFieldWidth,
                        value: this.integrationAccountDetails[settingsFieldData.name],
                        scope: this,
                        integrationPartyName: this.integrationPartyData.integrationPartyName,
                        integrationPartyId: this.integrationPartyData.integrationPartyId
                    };
                    var field = Wtf.integrationFunctions.createField(settingsFieldData, additionalConfig);
                    credentialsFromPanelItemsArr.push(field);
                }
            }
        }

        /*
         * Form Panel with Integration Account Credentials Fields
         */
        var credentialsFromPanel = new Wtf.form.FormPanel({
            title: this.integrationPartyData.integrationPartyName + " " + WtfGlobal.getLocaleText("acc.integration.accountDetails"),
            bodyStyle: "padding:10px;",
            labelWidth: this.integrationPartyData.integrationConfigJson.settingsFieldLabelWidth,
            border: false,
            items: credentialsFromPanelItemsArr
        });
        return credentialsFromPanel;
    },
    createToolbarButtons: function () {
        var settingsButtonsData = this.integrationPartyData.integrationConfigJson.settingsButtonsData;
        var bttnArr = [];
        if (settingsButtonsData) {
            for (var i = 0; i < settingsButtonsData.length; i++) {
                var buttonData = settingsButtonsData[i];
                if (buttonData) {
                    var additionalConfig = {
                        scope: this,
                        integrationPartyName: this.integrationPartyData.integrationPartyName,
                        integrationPartyId: this.integrationPartyData.integrationPartyId
                    };
                    if (buttonData.handler) {
                        buttonData.handler = eval(buttonData.handler);
                    }
                    var button = Wtf.integrationFunctions.createField(buttonData, additionalConfig);
                    bttnArr.push(button);
                }
            }
        }
        return bttnArr;
    },
    integrationSettingsValidationBttnHandler: function () {
        if (this.credentialsFromPanel.getForm().isValid()) {//Validate credentials only if credentials fields are filled with valid input
            this.loadMask.show();
            var integrationOperationId = undefined;
            switch (this.integrationPartyId) {
                case Wtf.integrationPartyId.AVALARA:
                    integrationOperationId = Wtf.integrationOperationId.avalara_credentialsValidation;
                    break;
            }
            Wtf.Ajax.requestEx({
                url: this.integrationPartyData.integrationConfigJson.validateCredentialsUrl,
                method: "POST",
                params: {
                    credentialsData: JSON.stringify(this.credentialsFromPanel.getForm().getValues()),
                    integrationPartyId: this.integrationPartyId, //Identifier for Integration Service owner party
                    integrationOperationId: integrationOperationId//Identifier for Integration operation which is to be performed
                }
            }, this, function (res, req) {
                this.loadMask.hide();
                if (res.success) {
                    this.areSettingsValid = true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), res.msg], 0);
                } else {
                    this.areSettingsValid = false;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 1);
                }
                if (this.integrationPartyId === Wtf.integrationPartyId.AVALARA) {
                    this.avalaraCompanyId = (res.data && res.data.avalaraCompanyId) ? res.data.avalaraCompanyId : undefined;
                    if (!this.avalaraCompanyId) {
                        this.areSettingsValid = false;
                    }
                }
            }, function () {
                this.loadMask.hide();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Anerroroccurredwhileconnectingtoservice")], 1)
            });
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.2")], 2);
        }
    },
    resetBttnHandler: function () {//Reset forms on Reset Button's click
        this.credentialsFromPanel.getForm().reset();
        if (this.integrationPartyId === Wtf.integrationPartyId.AVALARA) {
            this.avalaraConfigFormPanel.getForm().reset();
            //Reset Account Combo value on reset click. Need to reset separately because value is set separately on render.
            this.avalaraTaxAccountCombo.setValue(this.integrationAccountDetails.configJson ? this.integrationAccountDetails.configJson[Wtf.integration.taxAccountId] : undefined);
            //Disable Tax Commit check-box on reset click.
            if (this.activateTaxCalculationCheck && !this.activateTaxCalculationCheck.getValue()) {
                this.activateTaxCommittingCheck.disable();
            }
        }
    },
    cancelBttnHandler: function () {
        this.hide();
    },
    saveBttnHandler: function () {
        var isAvalaraFormValid = true;
        var configJson = undefined;
        if (this.integrationPartyId === Wtf.integrationPartyId.AVALARA) {
            isAvalaraFormValid = this.avalaraConfigFormPanel.getForm().isValid();
            configJson = this.avalaraConfigFormPanel.getForm().getValues();
            configJson.avalaraCompanyId = this.avalaraCompanyId;
        }

        var isCredentialsFormValid = true;
        var credentialsData = undefined;
        if (this.credentialsFromPanel) {
            isCredentialsFormValid = this.credentialsFromPanel.getForm().isValid();
            credentialsData = this.credentialsFromPanel.getForm().getValues();
        }

        var areFormsValid = isCredentialsFormValid && isAvalaraFormValid;
        if (areFormsValid) {//Save settings only if all fields are filled with valid input
            if (!Wtf.getCmp(Wtf.integrationFieldId.integrationSettingsValidationBttn + "_" + this.integrationPartyData.integrationPartyId) || this.areSettingsValid) {
                this.saveSettings(credentialsData, configJson);
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.integration.validationAlert")], 2);
            }
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.2")], 2);
        }
    },
    saveSettings: function (credentialsData, configJson) {
        this.loadMask.show();
        Wtf.Ajax.requestEx({
            url: "Integration/saveOrUpdateIntegrationAccountDetails.do",
            method: "POST",
            params: {
                credentialsData: credentialsData ? JSON.stringify(credentialsData) : undefined,
                configJson: configJson ? JSON.stringify(configJson) : undefined,
                integrationPartyId: this.integrationPartyId
            }
        }, this, function (res, req) {
            this.loadMask.hide();
            if (res.success) {
                successMsg = res.msg
                if (!successMsg) {
                    var successMsg = WtfGlobal.getLocaleText("acc.common.success");
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), successMsg], 0);
                this.hide();
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 1);
            }
        }, function () {
            this.loadMask.hide();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Anerroroccurredwhileconnectingtoservice")], 1)
        });
    }
});

Wtf.integrationFunctions = {
    createIntegrationSettingsFieldSet: function (scope) {
        var fieldPanelItems = [];
        var integrationPartiesData = Wtf.account.companyAccountPref[Wtf.integration.integrationPartiesData];
        if (integrationPartiesData) {
            for (var key in integrationPartiesData) {
                if (integrationPartiesData.hasOwnProperty(key)) {
                    var integrationPartyData = integrationPartiesData[key];
                    if (integrationPartyData) {
                        var integrationConfigJson = integrationPartyData.integrationConfigJson;
                        if (integrationConfigJson) {
                            var fieldsData = integrationConfigJson.fieldsData;
                            if (fieldsData) {
                                for (var j = 0; j < fieldsData.length; j++) {
                                    var fieldData = fieldsData[j];
                                    if (fieldData) {
                                        var additionalConfig = {
                                            scope: scope,
                                            integrationPartyName: integrationPartyData.integrationPartyName,
                                            integrationPartyId: integrationPartyData.integrationPartyId
                                        };
                                        var field = Wtf.integrationFunctions.createField(fieldData, additionalConfig);
                                        fieldPanelItems.push(field);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        var fieldSet = {
            xtype: 'fieldset',
            autoHeight: true,
            hidden: fieldPanelItems.length === 0,
            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.integration.integrationSettings") + "'>" + WtfGlobal.getLocaleText("acc.integration.integrationSettings") + "</span>",
            defaults: {
                maxLength: 50
            },
            items: fieldPanelItems.length !== 0 ? fieldPanelItems : [{
                    xtype: 'hidden'
                }]
        };
        return fieldSet;
    },
    createField: function (config, additionalConfig) {
        var field = undefined;
        var integrationPartyId = additionalConfig.integrationPartyId;

        if (additionalConfig) {
            for (var key in additionalConfig) {
                if (additionalConfig.hasOwnProperty(key) && !config[key]) {
                    config[key] = additionalConfig[key];
                }
            }
        }

        field = Wtf.integrationFunctions.createFieldByXtype(config);

        if (!field) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), "Integration configuration is not correct for third party Id: " + integrationPartyId], 1);
        }

        return field;
    },
    addTooltipToText: function (text, isTooltip, tooltip) {
        if (text) {
            text = eval(text);
            if (tooltip) {
                tooltip = eval(tooltip);
            } else if (isTooltip) {//if tooltip is required but no tooltip is passed, then text is used as tooltip
                tooltip = text;
            }
            if (tooltip) {//Tooltip is added only if tooltip is non-empty
                text = "<span wtf:qtip='" + tooltip + "'>" + text + "</span>";
            }
        }
        return text;
    },
    createFieldByXtype: function (config) {
        var field;
        switch (config.xtype) {
            case 'checkbox':
                field = Wtf.integrationFunctions.createCheckBox(config);
                break;
            case 'button':
                field = Wtf.integrationFunctions.createButton(config);
                break;
            case 'textfield':
                field = Wtf.integrationFunctions.createTextField(config);
                break;
            case 'tbbutton':
                field = Wtf.integrationFunctions.createToolbarButton(config);
                break;
        }
        return field;
    },
    createCheckBox: function (config) {
        var field;
        if (config) {
            if (config.id == (Wtf.integrationFieldId.integrationCheck + '_' + config.integrationPartyId)) {
                config.checked = Wtf.account.companyAccountPref[config.name];
                config.disabled = (config.oneTimeActivation && Wtf.account.companyAccountPref[config.name]);
                config.listeners = {
                    check: {
                        fn: Wtf.integrationFunctions.integrationActivationDeactivationHandler
                    }
                };
            }
            if (config.fieldLabel) {
                config.fieldLabel = Wtf.integrationFunctions.addTooltipToText(config.fieldLabel, config["fieldLabelHasSameTooltip"], config["fieldLabelTT"]);
            }
            field = new Wtf.form.Checkbox(config);
        }
        return field;
    },
    createButton: function (config) {
        var field;
        if (config) {
            if (config.id == (Wtf.integrationFieldId.integrationSettingsBttn + '_' + config.integrationPartyId)) {
                if (Wtf.getCmp(Wtf.integrationFieldId.integrationCheck + '_' + config.integrationPartyId)) {
                    config.disabled = !Wtf.getCmp(Wtf.integrationFieldId.integrationCheck + '_' + config.integrationPartyId).getValue();
                }
            }
            if (config.text) {
                config.text = Wtf.integrationFunctions.addTooltipToText(config.text, config["textHasSameTooltip"], config["textTT"]);
                if (config["textHasSameTooltip"]) {
                    config.tooltip = config.text;
                }
            }
            if (!config["textHasSameTooltip"] && config.tooltip) {
                config.tooltip = Wtf.integrationFunctions.addTooltipToText(config.tooltip, false, undefined);
            }
            if (config.handler) {
                config.handler = eval(config.handler);
            }
            field = new Wtf.Button(config);
        }
        return field;
    },
    createTextField: function (config) {
        var field;
        if (config) {
            if (config.regex) {
                config.regex = new RegExp(config.regex);
            }
            if (config.fieldLabel) {
                config.fieldLabel = Wtf.integrationFunctions.addTooltipToText(config.fieldLabel, config["fieldLabelHasSameTooltip"], config["fieldLabelTT"]);
            }

            field = new Wtf.form.TextField(config);
        }
        return field;
    },
    createToolbarButton: function (config) {
        var field;
        if (config) {
            if (config.text) {
                config.text = Wtf.integrationFunctions.addTooltipToText(config.text, config["textHasSameTooltip"], config["textTT"]);
                if (config["textHasSameTooltip"]) {
                    config.tooltip = config.text;
                }
            }
            if (!config["textHasSameTooltip"] && config.tooltip) {
                config.tooltip = Wtf.integrationFunctions.addTooltipToText(config.tooltip, false, undefined);
            }
            if (config.handler) {
                config.handler = eval(config.handler);
            }
            field = new Wtf.Toolbar.Button(config);
        }
        return field;
    },
    integrationActivationDeactivationHandler: function (obj, value) {
        if (obj.oneTimeActivation && value && !Wtf.account.companyAccountPref[obj.name]) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), obj.integrationPartyName + " " + WtfGlobal.getLocaleText("acc.integration.activationConfirmationMsg"), function (btn) {
                if (btn != "yes") {
                    obj.setValue(false);
                }
                Wtf.integrationFunctions.toggleSettingsBttnOnIntegrationCheckboxToggle(obj);
                return;
            }, this);
        } else {
            Wtf.integrationFunctions.toggleSettingsBttnOnIntegrationCheckboxToggle(obj);
        }

    },
    toggleSettingsBttnOnIntegrationCheckboxToggle: function (integrationCheckboxCmp) {
        if (integrationCheckboxCmp && integrationCheckboxCmp.settingsBttnId) {
            var settingsBttn = Wtf.getCmp(integrationCheckboxCmp.settingsBttnId);
            if (settingsBttn) {
                if (integrationCheckboxCmp.getValue()) {
                    settingsBttn.enable();
                } else {
                    settingsBttn.disable();
                }
            }
        }
    },
    /**
     * This function is used in integration buttons which are create dynamically based on data fetched from database
     * Therefore this function must not be removed
     * @param {type} obj
     * @param {type} e
     * @returns {undefined}
     */
    integrationSettingsBttnHandler: function (obj, e) {
        var windowObj = Wtf.getCmp(Wtf.integrationFieldId.integrationSettingsWindow + '_' + obj.integrationPartyId);
        if (!windowObj) {
            Wtf.Ajax.requestEx({
                url: "Integration/getIntegrationAccountDetails.do",
                method: "POST",
                params: {
                    integrationPartyId: obj.integrationPartyId//Identifier for Integration Service party.
                }
            }, obj, function (res, req) {//This function is executed on successful completion of Ajax request
                if (res.success) {
                    windowObj = Wtf.integrationFunctions.createIntegrationSettingsWindow(res.data, obj.integrationPartyId, Wtf.integrationFieldId.integrationSettingsWindow + '_' + obj.integrationPartyId); //Create Window on successful response
                    windowObj.show();
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 0);
                }
            }, function () {//This function is executed whe Ajax request fails
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Anerroroccurredwhileconnectingtoservice")], 1)
            });
        } else {
            windowObj.show();
        }
    },
    createIntegrationSettingsWindow: function (data, integrationPartyId, id) {
        var windowObj;
        var config = {
            id: id, //Id for the component - ("integrationSettingsWindow" + "_" + #integrationPartyId#)
            integrationPartyId: integrationPartyId, //Identifier for Integration party.
            integrationAccountDetails: data ? data : {}   //Current Settings fethced from database to populate in the window fields
        };
        windowObj = new Wtf.integration.IntegrationSettingsWindow(config);
        return windowObj;
    },
    addIntegrationSettingsIntoColumnPref: function (columnPref, scope) {
        if (scope && scope.integrationSettingsFieldSet) {
            if (!columnPref) {
                columnPref = {};
            }
            var integrationSettingsFieldSetItems = scope.integrationSettingsFieldSet.items;
            if (integrationSettingsFieldSetItems) {
                for (var i = 0; i < integrationSettingsFieldSetItems.length; i++) {
                    var item = integrationSettingsFieldSetItems[i];
                    if (item) {
                        if (item.xtype == 'checkbox') {
                            columnPref[item.name] = item.getValue();
                        }
                    }
                }
            }
        }
        return columnPref;
    },
    toggleUpsIntegrationOnPickPackShipChange: function (isPickPackShip) {
        var upsCheckbox = Wtf.getCmp(Wtf.integrationFieldId.integrationCheck + '_' + Wtf.integrationPartyId.UPS);
        if (upsCheckbox) {
            if (isPickPackShip) {
                if (Wtf.account.companyAccountPref[upsCheckbox.name]) {
                    //Reset value true if integration is already active and disable checkbox
                    upsCheckbox.setValue(true);
                    upsCheckbox.disable();
                } else {
                    upsCheckbox.enable();
                }
            } else {
                upsCheckbox.disable();
                upsCheckbox.setValue(false);
            }
        }
    }
};
