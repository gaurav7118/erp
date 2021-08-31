/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function callGSTHistoryInputDynamicLoad(config) {
    var winid = (config.winid == null ? "gsthistorydata" + config.gsthistorydetails : config.winid);
    var panel = Wtf.getCmp(winid);
    if (!panel) {
        panel = new Wtf.account.callGSTHistoryInput({
            id: winid,
            masterid: config.masterid,
            isCustomer: config.isCustomer != undefined ? config.isCustomer : false,
            isProduct: config.isProduct != undefined ? config.isProduct : false,
            isFixedAsset: config.isFixedAsset != undefined ? config.isFixedAsset : false,
            gsthistorydetails: config.gsthistorydetails,
            closable: true,
            modal: true,
            dataObj: config.dataObj,
            parentObj: config.parentObj,
            record: config.record,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            width: 600,
            height: 400,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        });
        panel.show();
        return panel;
    }
}
Wtf.account.callGSTHistoryInput = function(config) {
    Wtf.apply(this, config);
    this.storeloadcount=0;
    this.gstinInValid=false;
    var btnArr = [];
    btnArr.push(this.save = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.currency.sav"), //'Save and Close',
        scope: this,
        hidden: this.gsthistorydetails,
        handler: this.saveData.createDelegate(this)
    }))
    btnArr.push(this.cancel = new Wtf.Toolbar.Button({
        text: this.gsthistorydetails ? WtfGlobal.getLocaleText("acc.common.backBtn") : WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope: this,
        handler: this.closeWin.createDelegate(this)
    }))
    Wtf.apply(this, {
        title: WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstdetails"),
        buttons: btnArr
    }, config);
    Wtf.account.callGSTHistoryInput.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true
    });
}
Wtf.extend(Wtf.account.callGSTHistoryInput, Wtf.Window, {
    defaultCurreny: false,
    draggable: true,
    onRender: function(config) {
        Wtf.account.callGSTHistoryInput.superclass.onRender.call(this, config);
        this.createStore();
//        if(this.isProduct==false){
        this.createEditorFieldsForCustVend();
//        }else{
        this.createEditorFieldsForProduct();
//        }
        this.createGrid();

        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstdetails"), 
            this.gsthistorydetails? '':WtfGlobal.getLocaleText("acc.gsthisttory.edittext"), "../../images/gstDetails-icon-40x52.png", true)
        }, {
            region: 'center',
            border: false,
            height: (this.gsthistorydetails ? 260 : 150),
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: this.grid
        });
        if (!this.gsthistorydetails)
            this.grid.on('cellclick', this.onCellClick, this);
//        this.grid.on('afteredit',this.updateRow,this);
    },
    createEditorFieldsForCustVend: function() {
        // GSTIN Registration Type *********************************
        this.GSTINRegistrationTypeRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'defaultMasterItem'} // Added default master key in records
        ]);
        this.GSTINRegistrationTypeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.GSTINRegistrationTypeRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 62
            }
        });
        this.GSTINRegistrationTypeStore.load();
        this.GSTINRegistrationTypeStore.on('load', function() {
            /**
             * Hide GST Registration type
             * ERP-35464
             */
             removeGSTDetailsNotUsedFromStore(this.GSTINRegistrationTypeStore,false,this.isCustomer);
            this.onStoreLoad();
        }, this);
        this.GSTINRegistrationTypeCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.62") + "*", //GSTIN Registration Type
            hiddenName: "mappingPaidToCmb",
            id: "GSTINRegistrationType" + this.heplmodeid + this.id,
            store: this.GSTINRegistrationTypeStore,
            valueField: 'id',
            displayField: 'name',
            allowBlank: (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW) ? false : true, //ERP-35237
            disabled: this.readOnly,
            emptyText: WtfGlobal.getLocaleText('acc.common.select') + " " + WtfGlobal.getLocaleText("acc.masterConfig.62"), //Select GSTIN Registration Type
            minChars: 1,
            extraFields: '',
            anchor: '80%',
            extraComparisionField: 'name', // type ahead search 
            mode: 'local',
            //  addNewFn: this.addPaidTo.createDelegate(this),
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true
        });
        this.CustomerVendorTypeRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'defaultMasterItem'} // Added default master key in records
        ]);
        this.CustomerVendorTypeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.CustomerVendorTypeRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 63
            }
        });
        this.CustomerVendorTypeStore.load();
        this.CustomerVendorTypeStore.on('load', function() {
            /**
             * Hide GST Customer/ Vendor type
             * ERP-35464
             */
            removeGSTDetailsNotUsedFromStore(this.CustomerVendorTypeStore,true,this.isCustomer);
            this.onStoreLoad();
        }, this);
        this.CustomerVendorTypeCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.customer.GST.type") + "*" : WtfGlobal.getLocaleText("acc.vendor.GST.type") + "*", //Customer/ Vendor Type
            hiddenName: "mappingPaidToCmb",
            id: "CustomerVendorType" + this.heplmodeid + this.id,
            store: this.CustomerVendorTypeStore,
            valueField: 'id',
            displayField: 'name',
            allowBlank: (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW) ? false : true, //ERP-35237
            disabled: this.readOnly,
            emptyText: WtfGlobal.getLocaleText('acc.common.select') + " " + (this.isCustomer ? WtfGlobal.getLocaleText("acc.customer.GST.type") : WtfGlobal.getLocaleText("acc.vendor.GST.type")), //Select Customer/ Vendor Type
            minChars: 1,
            extraFields: '',
            anchor: '80%',
            extraComparisionField: 'name', // type ahead search 
            mode: 'local',
            //  addNewFn: this.addPaidTo.createDelegate(this),
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true
        });
        this.GSTIN = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.india.vecdorcustomer.column.gstin') + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstininfo")),
            name: 'gstin',
            anchor: '80%',
            hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            maxLength: 15,
//            disabled : true,
            minLength: 15,
            //regex:Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?/\d{2}[A-Za-z]{5}\d{4}[A-Za-z]{1}\d[Zz]{1}[A-Za-z\d]{1}/:null,
            invalidText: 'Invalid GSTIN.' //WtfGlobal.getLocaleText('acc.india.vecdorcustomer.column.gstin.invalid')
        });
        this.GSTIN.on('change',this.validateGSTIN,this);
    },
    createEditorFieldsForProduct: function() {
        this.taxclassComboRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.taxclassComboStore = new Wtf.data.Store({
            url: "AccEntityGST/getFieldComboDataForModule.do",
            baseParams: {
                moduleid: this.isFixedAsset?Wtf.Acc_FixedAssets_AssetsGroups_ModuleId:Wtf.Acc_Product_Master_ModuleId,
                fieldlable: Wtf.GSTProdCategory
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.taxclassComboRec)
        });

        this.taxclassCombo = new Wtf.form.ExtFnComboBox({
            hiddenName: 'id',
            name: 'id',
            store: this.taxclassComboStore,
            valueField: 'id',
            displayField: 'name',
            typeAhead: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectstate"),
            width: 240,
            listWidth: 240,
            minChars: 1,
            extraFields: '',
            anchor: '80%',
            extraComparisionField: 'name', // type ahead search 
            mode: 'local',
            triggerAction: 'all',
                    typeAhead: true,
                    forceSelection: true
        });
        this.taxclassComboStore.load();
        if (!this.gsthistorydetails) {
            /**
             * Set tax class in Edit case using on load method.
             */
            this.taxclassComboStore.on('load', function() {
                this.loadProuctStore();
            }, this);
        }

    },
    createStore: function() {
        this.gridRec = new Wtf.data.Record.create([{
                name: 'id'
            }, {
                name: 'applydate',
                type: 'date'
            }, {
                name: 'GSTINRegistrationTypeId'
            }, {
                name: 'CustomerVendorTypeId'
            }, {
                name: 'gstin'
            }, {
                name: 'taxclassId'
            },{
                name:'GSTINRegTypeDefaultMstrID'
            }
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.gridRec),
            url: Wtf.req.account + 'CompanyManager.jsp'
        });
        this.store.on('load',this.onStoreLoad,this);
    },
    /**
     * This function called from 
     * 1) GSTINRegistrationTypeStore
     * 2) CustomerVendorTypeStore
     * 3) GST grid store i.e. - this.store
     * On edit case if storeloadcount == 3 means above three store loaded and then setRecordValues function called 
     */
    onStoreLoad: function(store) {
        if(!this.gsthistorydetails && this.isProduct == false){
            this.storeloadcount++;
        }
        if(!this.gsthistorydetails && this.isProduct == false && this.storeloadcount==3){
            /**
             * If all store loaded then set values for masters i.e. GSTR type etc
             */
            this.setRecordValues();
        }

    },
    /**
     * Set value in Edit case where every master store must be loaded
     */
    setRecordValues: function() {
        var record = this.grid.getStore().getAt(0);
        if (record) {
            record.set('GSTINRegistrationTypeId', record.data.GSTINRegistrationTypeId);
            record.set('CustomerVendorTypeId', record.data.CustomerVendorTypeId);
            /**
             * Validate GSTIN Number On GST History Load
             */
            this.validateGSTINNumberOnLoad();
        }
        /**
         * Refresh grid on data set 
         * ERP-38833
         */
        this.grid.getView().refresh();
    },
    createGrid: function() {
        this.editorFlag = (this.gsthistorydetails ? true : false);
        this.gridcm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(), {
                header: this.isProduct ? WtfGlobal.getLocaleText("acc.rem.prodName") : this.isCustomer ? WtfGlobal.getLocaleText("acc.cnList.gridCustomerName") : WtfGlobal.getLocaleText("acc.ven.name"), //"Currency",
                dataIndex: 'id',
                width: 200,
                hidden: this.gsthistorydetails,
                renderer: this.currencylink.createDelegate(this), //function(){WtfGlobal.currencyLinkRenderer(val,),
                autoWidth: true

            }, {
                header: WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstin"), //"Base to Foreign Exchange Rate",
                dataIndex: 'gstin',
                hidden: this.isProduct,
                width: 200,
//            renderer:this.setRateRenderer.createDelegate(this),
//            hidden:this.isRevaluation,
                editor: this.editorFlag ? "" : this.GSTIN
            }, {
                header: WtfGlobal.getLocaleText("acc.masterConfig.62"),
                width: 200,
                dataIndex: 'GSTINRegistrationTypeId',
                hidden: this.isProduct,
                renderer: Wtf.comboBoxRenderer(this.GSTINRegistrationTypeCombo),
                editor: this.editorFlag ? "" : this.GSTINRegistrationTypeCombo
            }, {
                header: WtfGlobal.getLocaleText("acc.gstrr.productTaxClass"),
                width: 200,
                dataIndex: 'taxclassId',
                hidden: !this.isProduct,
                renderer: Wtf.comboBoxRenderer(this.taxclassCombo),
                editor: this.editorFlag ? "" : this.taxclassCombo
            }, {
                header: this.isCustomer ? WtfGlobal.getLocaleText("acc.customer.GST.type") : WtfGlobal.getLocaleText("acc.vendor.GST.type"),
                width: 200,
                dataIndex: 'CustomerVendorTypeId',
                hidden: this.isProduct,
                renderer: Wtf.comboBoxRenderer(this.CustomerVendorTypeCombo),
                editor: this.editorFlag ? "" : this.CustomerVendorTypeCombo
            }, {
                header: WtfGlobal.getLocaleText("acc.currency.FromDate"),
                dataIndex: 'applydate',
                renderer: WtfGlobal.onlyDateRenderer,
                minValue: new Date().clearTime(true),
                width: 200,
                editor: this.editorFlag ? "" : new Wtf.form.DateField({
                    name: 'applydate',
                    format: WtfGlobal.getOnlyDateFormat()
                })
            }
        ]);
        this.grid = new Wtf.grid.EditorGridPanel({
            cls: 'vline-on',
            layout: 'fit',
            autoScroll: true,
            height: 200,
            id: (this.gsthistorydetails ? 'gsthistorydetails' : 'gsthistoryinput'),
            store: this.store,
            cm: this.gridcm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.grid.on('afteredit', this.updateRow, this);
//        if (this.gsthistorydetails && this.gsthistorydetails == true) {
//            this.loadMasterStore()
//        } else {
            if (this.isProduct == true && this.gsthistorydetails) {
                this.loadProuctStore();
            } else {
                this.loadMasterStore();
            }
//        }
    },
    /**
     * Validate GSTIN Number On GST History Load
     * @returns {undefined}
     */
    validateGSTINNumberOnLoad: function () {
        if (this.isProduct == false) {
            var rec = this.grid.getStore().getAt(0);
            var GSTIndex = this.GSTINRegistrationTypeCombo.store.find('id', rec.data.GSTINRegistrationTypeId);
            if (GSTIndex != -1) {
                var record = this.GSTINRegistrationTypeCombo.store.getAt(GSTIndex);
                if (record != undefined && record.data != undefined && record.data.defaultMasterItem != undefined && record.data.defaultMasterItem != '') {
                    if (record.data.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Unregistered || record.data.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Consumer) {
                        this.GSTIN.allowBlank = true;
                        this.GSTIN.setDisabled(true); // Disable GSTIN number for Consumer and Unregistered Customer/ Vendor
                        rec.set("gstin", '');
                    } else {
                        this.GSTIN.allowBlank = false;
                        this.GSTIN.setDisabled(false);// Enable GSTIN number for Consumer and Unregistered Customer/ Vendor
                    }
                    this.GSTIN.validate();
                } else {
                    this.GSTIN.allowBlank = true;
                }
            }
        }
    },
    updateRow: function(obj) {
        if (this.isProduct == false) {
            if (obj != null) {
                var rec = this.grid.getStore().getAt(0);
                //this.parentObj.GSTINRegistrationTypeCombo.setValue(rec.data.GSTINRegistrationTypeId);
                //this.parentObj.CustomerVendorTypeCombo.setValue(rec.data.CustomerVendorTypeId);
                if (obj.field == 'GSTINRegistrationTypeId') {
                    var GSTIndex = this.GSTINRegistrationTypeCombo.store.find('id', rec.data.GSTINRegistrationTypeId);
                    var record = this.GSTINRegistrationTypeCombo.store.getAt(GSTIndex);
                       if (record != undefined && record.data != undefined && record.data.defaultMasterItem != undefined && record.data.defaultMasterItem != '') {
                        if (record.data.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Unregistered || record.data.defaultMasterItem == Wtf.GSTRegMasterDefaultID.Consumer) {
                            this.GSTIN.allowBlank = true;
                            this.GSTIN.setDisabled(true); // Disable GSTIN number for Consumer and Unregistered Customer/ Vendor
                            obj.record.set("gstin", '');
                        } else {
                            this.GSTIN.allowBlank = false;
                            this.GSTIN.setDisabled(false);// Enable GSTIN number for Consumer and Unregistered Customer/ Vendor
                        }
                        this.GSTIN.validate();
                    } else {
                        this.GSTIN.allowBlank = true;
                    }
                    obj.record.set("CustomerVendorTypeId", '');
                } else if (obj.field == 'CustomerVendorTypeId') {
                    var GSTIndex = this.CustomerVendorTypeCombo.store.find('id', rec.data.CustomerVendorTypeId);
                    var record = this.CustomerVendorTypeCombo.store.getAt(GSTIndex);
                    this.parentObj.validateCustomerVendorType(this.CustomerVendorTypeCombo, record,undefined,true ,obj , rec.data.GSTINRegistrationTypeId);
                    }
                }
            }
    },
//    addBlankRow: function() {
//        var Record = this.store.reader.recordType;
//        var rec = {};
//        if (this.isProduct == false) {
//            rec['GSTINRegistrationTypeId'] = this.dataObj.GSTINRegistrationTypeId;
//            rec['CustomerVendorTypeId'] = this.dataObj.CustomerVendorTypeId;
//            rec['gstin'] = this.dataObj.gstin;
//            rec['id'] = this.dataObj.mastername;
//        }
//        var newrec = new Record(rec);
//        this.store.add(newrec);
////        this.grid.getView().refresh();
//    },
    loadMasterStore: function() {
//        if (this.isProduct == false) {
            if (this.isCustomer) {
                this.store.proxy.conn.url = "ACCCustomerCMN/getCustomerGSTHistory.do";
                this.store.load({
                    params: {
                        customerid: this.masterid,
                        returnalldata:true,
                        returncurrentsingledata: !this.gsthistorydetails
                    }
                });
            }
            else {
                this.store.proxy.conn.url = "ACCVendorCMN/getVendorGSTHistory.do";
                this.store.load({
                    params: {
                        vendorid: this.masterid,
                        returnalldata:true,
                        returncurrentsingledata: !this.gsthistorydetails
                    }
                });
                this.gridcm.setRenderer(1, this.currencylink.createDelegate(this));
            }
//        } else {
//            this.loadProuctStore();
//        }
    },
    loadProuctStore: function() {
        if (this.isProduct == true) {
            this.store.proxy.conn.url = "ACCProductCMN/getProductGSTHistory.do";
            this.store.load({
                params: {
                    productid: this.masterid,
                    gsthistorydetails: this.gsthistorydetails,
                    isFixedAsset:this.isFixedAsset
                }
            });
        }
    },
    currencylink: function(val) {
        return WtfGlobal.currencyLinkRenderer(val, WtfGlobal.getLocaleText("acc.currency.his"));
    },
    onCellClick: function(g, i, j, e) {
        e.stopEvent();
        var el = e.getTarget("a");
        if (el == null)
            return;
        var header = g.getColumnModel().getDataIndex(j);
        var rec = this.store.getAt(i);
        if (header == "id") {
            var config = {};
            config.masterid = this.masterid;
            config.isCustomer = this.isCustomer;
            config.isProduct = this.isProduct;
            config.isFixedAsset = this.isFixedAsset;
            config.gsthistorydetails = true;
//        config.winid='gsthistorydata';
            callGSTHistoryInput(config);
        }
    },
    closeWin: function() {
        this.fireEvent('cancel', this)
        this.close();
    },
    setRateRenderer: function(val) {
        return  WtfGlobal.conventCurrencyDecimal(val, "")
    },
    saveData: function() {
        var rec = this.grid.getStore().getAt(0);
        var applydate = rec.data.applydate
        if (this.isProduct == true) {
            Wtf.Ajax.requestEx({
                method: 'POST',
                url: "ACCProductCMN/getProductUsedHistory.do",
                params: {
                    productid: this.masterid,
                    applydate: WtfGlobal.convertToGenericDate(applydate)
                }
            }, this, this.genSuccessResponse, this.genFailureResponse);
        }
        if (this.isProduct == false) {
            /**
             * Check invalid records for edit case
             */
            var isInvalid = false;
            for (var k = 0; k < this.grid.colModel.config.length; k++) {
                var dataIndex = this.grid.colModel.config[k].dataIndex || "";
                var value = rec.get(dataIndex);
                if (this.grid.colModel.config[k].editor && this.grid.colModel.config[k].editor.field.allowBlank == false) {
                    if (value == undefined || value == "") {
                        isInvalid = true;
                    }
                }
            }
            if (isInvalid == true) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gsthistory.invalidfield")], 2);
                return;
            }
            if (rec.data.GSTINRegTypeDefaultMstrID == Wtf.GSTRegMasterDefaultID.Regular || rec.data.GSTINRegTypeDefaultMstrID == Wtf.GSTRegMasterDefaultID.Composition) {
                this.validateGSTIN();
            }
            var applydate = rec.data.applydate

            Wtf.Ajax.requestEx({
                method: 'POST',
                url: this.isCustomer ? "ACCCustomerCMN/getCustomerUsedGSTHistory.do" : "ACCVendorCMN/getVendorUsedGSTHistory.do",
                params: {
                    custvenid: this.masterid,
                    applydate: WtfGlobal.convertToGenericDate(applydate)
                }
            }, this, this.custVenSuccessResponse, this.custVenFailureResponse);            
        }
    },
    genSuccessResponse: function (response, request) {
        if (response.success) {
            var rec = this.grid.getStore().getAt(0);
            var record = response.data[0];
            if (record.isUsedProduct) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gst.masterUsedInRecords")], 2);
                return;
            }
            this.parentObj.gstapplieddate = rec.data.applydate;
            this.parentObj.isgstdetailsupdated = true;
            this.storeloadcount=0;
            /**
             * Set newly tagged tax class to dimension value of product tax class in dimensions fieldset
             */
            this.setCustomFieldData();
            this.close();
        }
    },
    genFailureResponse: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg){
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    custVenSuccessResponse:function(response){
         if (response.success) {
            var rec = this.grid.getStore().getAt(0);
            var record = response.data[0];
            if (record.isUsedCustomerVendor) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gst.masterUsedInRecords")], 2);
                return;
            }
//            if(this.gstinInValid){                
//                return;
//            }
            this.parentObj.CustomerVendorTypeCombo.setValue(rec.data.CustomerVendorTypeId);
            this.parentObj.GSTINRegistrationTypeCombo.setValue(rec.data.GSTINRegistrationTypeId);
            this.parentObj.GSTIN.setValue(rec.data.gstin);
            /**
             * Validate Customer / Vendor Address fields.
             */
            this.parentObj.changeAddressFields();
            this.parentObj.gstapplieddate = rec.data.applydate;
            this.parentObj.isgstdetailsupdated = true;
            this.storeloadcount = 0;
            this.close();
        }
    },
    custVenFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg){
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    setCustomFieldData: function() {
        var rec = this.grid.getStore().getAt(0);
        var fieldValue = rec.data.taxclassId;
        this.dimensionFieldArray = this.parentObj.tagsFieldset.dimensionFieldArray;
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldLabel = this.dimensionFieldArray[itemcnt].fieldLabel;
            if (fieldLabel == Wtf.GSTProdCategory + "*") {
                var fieldId = this.dimensionFieldArray[itemcnt].id;
                Wtf.getCmp(fieldId).setValue(fieldValue);
            }
        }
    },
    validateGSTIN: function () {
        var rec = this.grid.getStore().getAt(0);
        var gstin = this.GSTIN.getValue() == undefined ? rec.data.gstin : this.GSTIN.getValue();
        rec.set('gstin', gstin);

        Wtf.Ajax.requestEx({
            method: 'POST',
            url: "ACCCustomerCMN/validateCustomerGSTIN.do",
            params: {
                gstin: gstin
            }
        }, this, function (response, request) {
            if (response.success) {
                var isValid = response.Valid;
                if (isValid == false) {
                    this.gstinInValid = true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.cust.gstinInvalid") + WtfGlobal.addLabelHelp('') + WtfGlobal.getLocaleText("acc.cust.gstinInvalid1")], 2);
//                    rec.set('gstin', '');
//                    return;
                } else {
                    this.gstinInValid = false;
                }
            }
        }, function (response) {
            WtfGlobal.resetAjaxTimeOut();
            Wtf.MessageBox.hide();
            var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if (response.msg) {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        });

    },
});
