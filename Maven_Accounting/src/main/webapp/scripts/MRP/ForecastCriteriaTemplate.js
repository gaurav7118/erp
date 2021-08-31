/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.ForecastInformationPanel = function(config) {
    Wtf.apply(this, config);
    /*
     * Define required Stores
     */
    this.createStores();
    /*
     * Create Form Fields
     */

    this.createFormFields();
    /*
     * Create Buttons
     */
    this.createButton();

    /*
     * Create Form
     */
    this.createForm();
    /*
     * Create panel in Tab
     */
    this.createPanel();
    Wtf.account.ForecastInformationPanel.superclass.constructor.call(this, config);

}

Wtf.extend(Wtf.account.ForecastInformationPanel, Wtf.Panel, {
    onRender: function(config) {
        Wtf.account.ForecastInformationPanel.superclass.onRender.call(this, config);
        this.add(this.centerPanel);
        if (this.isEdit || this.isCopy) {
            /*
             * Load record in edit case
             */
            this.getForecastDataToLoad();
        }

    },
    createStores: function() {
        var typeArr = new Array();
        typeArr.push(['Sales Order', '1']);
        typeArr.push(['Invoice', '2']);
        typeArr.push(['Delivery Order', '3']);
        this.reportTypeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value'}],
            data: typeArr
        });
        var methodArr = new Array();
        methodArr.push(['Percent Over Last Year', '1']);
        methodArr.push(['Last Year To This Year', '2']);
        this.methodStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value'}],
            data: methodArr
        });
        this.productRec = Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'}
        ]);

        this.productStore = new Wtf.data.Store({
            url: "ACCProduct/getProductsForCombo.do",
            baseParams: {
                mode: 22,
                onlyProduct: true,
                isFixedAsset: false,
                includeBothFixedAssetAndProductFlag: false
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });
    },
    createFormFields: function() {

        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'monthid',
                    type: 'int'
                }, 'name'],
            data: [[0, 'January'], [1, 'February'], [2, 'March'], [3, 'April'], [4, 'May'], [5, 'June'], [6, 'July'], [7, 'August'], [8, 'September'], [9, 'October'],
                [10, 'November'], [11, 'December']]
        });

        var data = WtfGlobal.getBookBeginningYear(true);

        this.yearStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'id',
                    type: 'int'
                }, 'yearid'],
            data: data
        });

        this.startMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), //'Month',
            name: 'startMonth',
            displayField: 'name',
            forceSelection: true,
            width: 90,
            valueField: 'name',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true,
            hidden: true
        });

        this.startYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel: WtfGlobal.getLocaleText("mrp.forecase.forecastyear"), //'Year',
            name: 'startYear',
            width: 240,
            displayField: 'yearid',
            valueField: 'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true
        });

        if (this.startMonth.getValue() == "" || this.startYear.getValue() == "") {
            var temp = new Date();
            var year1 = temp.getFullYear();
            if (!this.isMonthlyBookings) {
                year1 = this.yearStore.data.items[0].data.yearid;
            }
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(year1);
        }
        this.forecastTitleonfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("mrp.forecase.tilte") + "'>" + WtfGlobal.getLocaleText("mrp.forecase.tilte") + "</span>" + ' *',
            name: 'title',
            hiddenName: 'title',
            id: "title" + this.id
        };
        this.forecastTitle = WtfGlobal.createTextfield(this.forecastTitleonfig, false, false, 50, this);
        this.forecastIDConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("mrp.forecase.ID") + "'>" + WtfGlobal.getLocaleText("mrp.forecase.ID") + "</span>" + ' *',
            name: 'forecastid',
            hiddenName: 'forecastid',
            id: "forecastid" + this.id
        };
        this.forecastID = WtfGlobal.createTextfield(this.forecastIDConfig, false, false, 50, this);

        this.forecastYearHistoryConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("mrp.forecase.yaerodhistory") + "'>" + WtfGlobal.getLocaleText("mrp.forecase.yaerodhistory") + "</span>" + ' *',
            name: 'forecastyearhistory',
            hiddenName: 'forecastyearhistory',
            id: "forecastyearhistory" + this.id
        };
        this.forecastYearHistory = WtfGlobal.createNumberfield(this.forecastYearHistoryConfig, false, false, 50, this);


        this.forecastYearConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("mrp.forecase.forecastyear") + "'>" + WtfGlobal.getLocaleText("mrp.forecase.forecastyear") + "</span>" + ' *',
            name: 'forecastyear',
            hiddenName: 'forecastyear',
            id: "forecastyear" + this.id
        };
        this.forecastYear = WtfGlobal.createDatefield(this.forecastYearConfig, false, this);

        this.forecastTypeConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("mrp.forecase.reporttype") + "'>" + WtfGlobal.getLocaleText("mrp.forecase.reporttype") + "</span>" + ' *',
            name: 'forecasttype',
            hiddenName: 'forecasttype',
            id: "forecasttype" + this.id,
            allowBlank: false
        };
        this.forecastType = WtfGlobal.createFnCombobox(this.forecastTypeConfig, this.reportTypeStore, 'value', 'name', this);

        this.forecastmethodConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("mrp.forecase.method") + "'>" + WtfGlobal.getLocaleText("mrp.forecase.method") + "</span>" + ' *',
            name: 'forecastmethod',
            hiddenName: 'forecastmethod',
            id: "forecastmethod" + this.id,
            allowBlank: false
        };
        this.forecastmethod = WtfGlobal.createFnCombobox(this.forecastmethodConfig, this.methodStore, 'value', 'name', this);

        this.ProductID = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrp.field.MappedProductID") + "'>" + WtfGlobal.getLocaleText("acc.mrp.field.MappedProductID") + "</span>" + ' *',
            forceSelection: true
        }, {
            name: 'productid',
            id: "productName" + this.id,
            width: 240,
            emptyText: WtfGlobal.getLocaleText("acc.mrp.field.prefix.select") + " " + WtfGlobal.getLocaleText("acc.mrp.field.MappedProductID"),
            store: this.productStore,
            valueField: "productid",
            displayField: "productname",
            addNoneRecord: true,
            allowBlank: false
        }));
    },
    createButton: function() {
        this.saveBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            id: "save" + this.heplmodeid + this.id,
            scope: this,
            handler: function() {
                this.createNew = false;
                this.save();
            },
            iconCls: 'pwnd save'
        });

        this.buttonArray = new Array();
        this.buttonArray.push(this.saveBttn);
    },
    createForm: function() {
        this.ForecastForm = new Wtf.form.FormPanel({
            region: 'north',
            id: "ForecastForm" + this.id,
            border: false,
            defaults: {
                border: false
            },
            split: true,
            autoheight: true,
            layout: 'form',
            //baseCls: 'northFormFormat',
            disabledClass: "newtripcmbss",
            hideMode: 'display',
            cls: "visibleDisabled",
            items: [{
                    xtype: 'panel',
                    id: this.id + 'requiredfieldmessagepanel',
                    hidden: true,
                    border: false,
                    cls: 'invalidfieldinfomessage'
                }, {
                    defaults: {border: false},
                    baseCls: 'northFormFormat',
                    xtype: 'panel',
                    border: false,
                    layout: 'fit',
                    cls: "visibleDisabled",
                    labelWidth: 160,
                    items: [{
                            layout: 'column',
                            border: false,
                            defaults: {border: false},
                            items: [{
                                    layout: 'form',
                                    columnWidth: 0.48,
                                    border: false,
                                    items: [this.forecastTitle, this.forecastID, this.forecastType, this.forecastYearHistory]
                                }, {
                                    layout: 'form',
                                    border: false,
                                    columnWidth: 0.04
                                },
                                {
                                    layout: 'form',
                                    columnWidth: 0.48,
                                    border: false,
                                    items: [this.forecastmethod, this.startYear, this.ProductID]
                                }]
                        }]
                }]
        });
    },
    createPanel: function() {
        this.centerPanel = new Wtf.Panel({
            region: 'center',
            id: 'centerpan' + this.id,
            autoScroll: true,
            border: false,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            items: [this.ForecastForm],
            //html : 'abc',
            bbar: this.buttonArray
        });
    },
    save: function() {
        var isValidForm = this.ForecastForm.getForm().isValid();
        /*
         * Check valid field or not
         */
        if (!isValidForm) {
            WtfGlobal.dispalyErrorMessageDetails(this.id + 'requiredfieldmessagepanel', this.getInvalidFields());
            this.ForecastForm.doLayout();
            return;
        } else {
            Wtf.getCmp(this.id + 'requiredfieldmessagepanel').hide();
        }
        var isValidData = this.validateBlankSpace();
        if (!isValidData) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Pleaseenternew") + " data"], 2);
        }
        if (this.ForecastForm.getForm().isValid() && isValidData) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.je.msg1"), function(btn) {
                if (btn != "yes") {
                    this.isWarnConfirm = false;
                    return;
                }
                WtfComMsgBox(27, 4, true);
                var rec = this.ForecastForm.getForm().getValues();
                rec.product = this.ProductID.getValue();
                var startMonthName = this.startMonth.getValue();
                var startYear = this.startYear.getValue();
                var startDate = new Date(Date.parse(startMonthName + " 01" + ", " + startYear));

                rec.forecastyear = WtfGlobal.convertToGenericDate(startDate);
                var url = "";
                if (this.isEdit) {
                    rec.id = this.record.data.billid
                }
                rec.isCopy = this.isCopy
                url = "ACCJobWorkController/saveForecastTemplate.do";
                Wtf.Ajax.requestEx({
                    url: url,
                    params: rec
                }, this, this.genSuccessResponse, this.genFailureResponse);
            }, this);
        } else if (isValidData) {
            WtfComMsgBox(2, 2);
        }
    },
    validateBlankSpace: function() {
        if (this.forecastID.getValue().replace(/\s+/g, '') == "" || this.forecastTitle.getValue().replace(/\s+/g, '') == "") {
            return false;
        }
        return true;
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
                fn: function(btn, text, option) {
                    this.scopeObj.refreshReportGrid();
                },
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });

            this.disableComponent();
        } else {
            this.showFailureMsg(response);
        }
    },
    refreshReportGrid: function() {
        var comp = null;
        comp = Wtf.getCmp('forecastList');
        if (comp) {
            comp.fireEvent('forecastupdate');
        }
    },
    disableComponent: function() {
        if (this.saveBttn) {
            this.saveBttn.disable();
        }
        if (this.ForecastForm) {
            this.ForecastForm.disable();
        }
    },
    resetAll: function() {
        this.ForecastForm.getForm().reset();
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
    getInvalidFields: function() {
        var invalidFields = []
        this.ForecastForm.getForm().items.filterBy(function(field) {
            if (field.validate())
                return;
            invalidFields.push(field);
        });
        return invalidFields;
    },
    getForecastDataToLoad: function() {
        Wtf.Ajax.requestEx({
            url: 'ACCJobWorkController/getSingleForecastToLoad.do',
            params: {
                billid: this.record.data.billid
            }
        }, this,
        function(result, req) {
                    this.loadForm(result);
        });
    },
    loadForm: function(rec) {
        var recData = rec.data;
        this.setComboValues(recData);
        this.ForecastForm.form.loadRecord(rec);
        if (this.isCopy) {
            this.forecastID.reset();
        }
    },
    setComboValues: function(recData) {
        if (this.ProductID) {
            this.ProductID.setValForRemoteStore(recData.productid, recData.productname);
        }
        if (this.startYear) {
            this.startYear.setValue(recData.yearid);
        }
    }
});