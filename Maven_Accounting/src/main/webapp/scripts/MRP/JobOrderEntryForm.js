/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//
function getJobOrderEntryForm(removegrid, workorderUUDID) {
    return  new Wtf.account.JobOrderEntryForm({
        title: WtfGlobal.getLocaleText("acc.field.jobworkentryformworkorder.tab.title"), //"Job Order Details",
//            region:'center',
        id: "joborderdetails",
        border: false,
        closable: false,
        removegrid: removegrid,
        workorderUUDID: workorderUUDID
    });

}

Wtf.account.JobOrderEntryForm = function (config) {

    this.moduleId = Wtf.MRP_Job_Work_ModuleID;
    Wtf.apply(this, config);
    /*
     * Define required Stores
     */
    this.createStores();
    /*
     * Create Form Fields
     */

    this.createFormFields();
    /**
     * create Custom Fields
     */
    this.createCustomFields();
    /*
     * Create Buttons
     */
    this.createButtons();
    /*
     * Create Form
     */
    this.createForm();
    /*
     * Create panel in Tab
     */
    this.createGrid();
    this.createPanel();
    Wtf.account.JobOrderEntryForm.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.account.JobOrderEntryForm, Wtf.Panel, {
    onRender: function (config) {
        this.newPanel.on("resize", function () {
            this.newPanel.doLayout();
        }, this);
        this.add(this.newPanel);
        this.productStore.load();
        this.workOrderStore.load();
        this.sequenceFormatStore.load();
        Wtf.account.JobOrderEntryForm.superclass.onRender.call(this, config);
    },
    createStores: function () {
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
        this.productStore.on('load', this.loadrecord, this);

        this.workOrderRec = Wtf.data.Record.create([
            {name: 'workorderid'},
            {name: 'workordername'}
        ]);

        this.workOrderStore = new Wtf.data.Store({
            url: "ACCJobWorkController/getWorkOrdersForCombo.do",
            baseParams: {
                getOpenWO:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workOrderRec)
        });

        this.workOrderStore.on('load', this.workOrderOnLaod, this);
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'value'}
        ]);

        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: Wtf.MRP_JOB_WORK_MODULENAME,
                isEdit: this.isEdit
            }
        });
        this.sequenceFormatStore.on('load', this.setNextNumber, this);

        this.locationRec = new Wtf.data.Record.create([
            {
                name: "id"
            },
            {
                name: "name"
            },
            {
                name: 'parentid'
            },
            {
                name: 'parentname'
            }
        ]);
        this.locationReader = new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.locationRec);
        var locationStoreUrl = "ACCMaster/getLocationItems.do"
        if (Wtf.account.companyAccountPref.activateInventoryTab) {
            locationStoreUrl = "ACCMaster/getLocationItemsFromStore.do";
        }
        this.locationStore = new Wtf.data.Store({
            url: locationStoreUrl,
            reader: this.locationReader
        });
        this.locationStore.load();
    },
    setNextNumber: function (store) {
        if (this.sequenceFormatStore.getCount() > 0) {
            if (this.isEdit) {
                var index = this.sequenceFormatStore.find('id', this.record.data.seqformat);
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.record.data.seqformat);
                    this.sequenceFormatCombobox.disable();
                    this.jobOrderNumber.disable();
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                    this.jobOrderNumber.enable();
                }
            } else {
                var count = this.sequenceFormatStore.getCount();
                for (var i = 0; i < count; i++) {
                    var seqRec = this.sequenceFormatStore.getAt(i)
                    if (seqRec.json.isdefaultformat == "Yes") {
                        this.sequenceFormatCombobox.setValue(seqRec.data.id)
                        break;
                    }
                }
                if(this.sequenceFormatCombobox.getvalue()!=""){
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                } else{
                    this.jobOrderNumber.setValue("");
                    this.jobOrderNumber.disable();
                }
            }
        }
    },
    workOrderOnLaod: function () {
        if (!this.removegrid) {
            this.workOrder.setValue(this.workorderUUDID);
        }
    },
    getNextSequenceNumber: function (combo) {
        if (combo.getValue() == "NA") {
            this.jobOrderNumber.reset();
            this.jobOrderNumber.enable();
        } else {
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from: Wtf.MRP_Job_Work_ModuleID,
                    sequenceformat: combo.getValue()
                }
            }, this, function (resp) {
                if (resp.data == "NA") {
                    this.jobOrderNumber.reset();
                    this.jobOrderNumber.enable();
                } else {
                    this.jobOrderNumber.setValue(resp.data);
                    this.jobOrderNumber.disable();
                }
            });
        }
    },
    loadrecord: function () {
        if (this.isEdit) {
            this.JoborderForm.getForm().loadRecord(this.record);
            this.Vendor.setValForRemoteStore(this.record.data.vendorid, this.record.data.vendorname);
            this.jobWorkLocation.setValue(this.record.data.jobworklocationid);
            this.workOrder.setValForRemoteStore(this.record.data.workorderid, this.record.data.workordercode); 
        }
        /*
         * In edit case while loading component setted min and max value to respective component.
         */
        this.dateOfshipment.minValue = this.jobWorkDate.getValue();
        this.dateofdelivery.minValue = this.jobWorkDate.getValue();
        this.dateofdelivery.minValue = this.dateOfshipment.getValue();
        this.dateOfshipment.maxValue = this.dateofdelivery.getValue();
        if (!this.isEdit) {
            this.jobWorkDate.setValue(Wtf.serverDate);
        }
    },
    createFormFields: function () {


        this.jobOrdernName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.name') + '*', //"Job Order Name",
            name: 'jobordername',
            hidden: false,
            width: 250,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });

        this.sequenceFormatCombobox = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"), //sequence  format
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            width: 250,
            typeAhead: true,
            forceSelection: true,
            name: 'seqformat',
            hiddenName: 'sequenceformat'
//             allowBlank:false
        });
        this.sequenceFormatCombobox.on('select', this.getNextSequenceNumber, this);

        this.jobOrderNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.number') + '*', //"Job order number
            name: 'jobordernumber',
            id: this.id + 'acccode',
            allowBlank: false,
            width: 250,
            maxLength: 50
        });

        this.workOrder = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.workorder.joborder.fields.joborder.workordercombotitle"), //Work Order
            name: 'workorderid',
//            id: "workorder" + this.id,
            store: this.workOrderStore,
            valueField: 'workorderid',
            displayField: 'workordername',
            mode: 'remote',
//            d: this.removegrid ? false : true,
            disabled: this.removegrid ? false : true,
            width: 250,
            maxLength: 50,
            scope: this,
            lastQuery: '',
            typeAhead: true,
            forceSelection: true,
            hirarchical: true,
            triggerAction: 'all',
            extraFields: [],
            allowBlank: true
        });
        this.jobWorkDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.date') + '*', //Job work date
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'jobworkdate',
            displayField: 'value',
            allowBlank: false,
            width: 250,
            maxValue : Wtf.serverDate //Passed maxValue config for maximaum value. 
        });
        
        /*
         * jobWorkDate on change event setted min values of dateOfshipment and dateofdelivery.
         */
        this.jobWorkDate.on('change', function(dateField, newVal ,oldVal){
            if (this.dateOfshipment) {
                this.dateOfshipment.minValue = newVal;
            }
            if(this.dateofdelivery){
                this.dateofdelivery.minValue = newVal;
            }
                
        }, this);

        this.Vendor = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.ven") + ' *', //Vendor
            hiddenName: 'vendorid',
            name: 'vendorid',
            store: Wtf.vendorAccRemoteStore,
            valueField: 'accid',
            displayField: 'accname',
            minChars: 1,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
            allowBlank: false,
            hirarchical: true,
            mode: 'remote',
            typeAhead: true,
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            typeAheadDelay: 30000,
            forceSelection: true,
            selectOnFocus: true,
            isVendor: true,
            isCustomer: false,
            width: 250,
            triggerAction: 'all',
            scope: this,
            addNewFn: this.createVendor.createDelegate(this)
        });

        this.productNameCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.product") + "*",
            name: 'productid',
            store: this.productStore,
            typeAhead: true,
            isProductCombo: true,
            selectOnFocus: true,
            maxHeight: 250,
            listAlign: "bl-tl?",
            valueField: 'productid',
            displayField: 'pid',
            extraFields: ['productname', 'type'], //Removed pid because two times pid displayed in combo. 
            listWidth: 450,
            extraComparisionField: 'pid',
            extraComparisionFieldArray: ['pid', 'productname'],
            lastQuery: '',
            width: 250,
            hirarchical: true,
            addNewFn: this.createProduct.createDelegate(this),
            forceSelection: true
        });

        this.productQuantity = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.productqty') + '*', //product quantity
            name: 'productquantity',
            hidden: false,
            width: 250,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });
        this.dateOfshipment = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.dateofshipment') + '*', ///date of shipment
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'dateofshipment',
            width: 250,
            allowBlank: false
        });

        /*
         * dateOfshipment on focus event called showNextMonth() and showPrevMonth() for refreshing datepicker.
         */
        this.dateOfshipment.on("focus",function(c){
            this.dateOfshipment.menu.picker.showNextMonth();
            this.dateOfshipment.menu.picker.showPrevMonth();
        },this);
        
        /*
         * dateOfshipment on change event setted min values of dateofdelivery.
         */
         this.dateOfshipment.on('change', function(dateField, newVal ,oldVal){
            if (this.dateofdelivery) {
                this.dateofdelivery.minValue = newVal;
            }
        }, this);  
        
        this.dateofdelivery = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.dateOfDelivery') + '*' + WtfGlobal.addLabelHelp("<ul style='list-style-type:disc; margin-left:10px;'><li>"+WtfGlobal.getLocaleText('acc.mrp.jobworkout.dateofdelivery.helpmsg')+"</li></ul>"), //Date Of Delivery
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'dateofdelivery',
            allowBlank: false,
            width: 250
        });
        
        /*
         * dateofdelivery on focus event called showNextMonth() and showPrevMonth() for refreshing datepicker.
         */
          this.dateofdelivery.on("focus",function(c){
            this.dateofdelivery.menu.picker.showNextMonth();
            this.dateofdelivery.menu.picker.showPrevMonth();
        },this);
        
        /*
         * dateofdelivery on change event setted min values of dateofdelivery.
         */
        this.dateofdelivery.on('change', function(dateField, newVal ,oldVal){
            if (this.dateOfshipment) {
                this.dateOfshipment.maxValue = newVal;
            }
        }, this);

        this.exiceseDutyCharges = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.exduty'), //Excise Duty Charges
            name: 'excisedutychargees',
            hidden: false,
            width: 250,
            maxLength: 50,
            scope: this
        });

        this.jobWorkLocation = new Wtf.form.ExtFnComboBox({
            triggerAction: 'all',
            mode: 'remote',
            multiSelect: false,
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.workorder.joborder.fields.joborder.jobworklocation"),
            valueField: 'id',
            displayField: 'name',
            lastQuery: '',
            store: this.locationStore,
            typeAhead: true,
            forceSelection: true,
            hirarchical: true,
            name: 'jobworklocation',
            hiddenName: 'jobworklocation',
            width: 250,
            typeAheadDelay: 30000,
            forceSelection: true,
                    selectOnFocus: true,
            extraFields: [],
            addNewFn: this.createLocation.createDelegate(this)
        });

        this.shipmentRoute = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.shipmentroute'), //Shipment Route
            name: 'shipmentroute',
            hidden: false,
            width: 250,
            maxLength: 50,
            scope: this
        });

        this.gatepass = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.gatepass'), //Gatepass
            name: 'gatepass',
            hidden: false,
            width: 250,
            maxLength: 50,
            scope: this
        });

        this.otherRemarks = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText('acc.mrp.workorder.joborder.fields.joborder.otherremarks'), //OtherRemarks
            name: 'otherremarks',
            hidden: false,
            width: 250,
            maxLength: 50,
            scope: this
        });

    },
    createCustomFields: function () {
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:'Northform' + this.id,
            autoHeight: true,
            moduleid: this.moduleId,
            isEdit: this.isEdit,
            record: this.record !=undefined ? this.record.data !=undefined?this.record:null : null,
            isViewMode: this.readOnly,
            parentcompId: this.id
        });
    },
    createButtons: function () {
        this.btnArr = [];
        this.newBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.New"), // "New",
            iconCls: getButtonIconCls(Wtf.etype.add),
            tooltip: WtfGlobal.getLocaleText("acc.mrp.jobwork.newbtn"),
            id: 'BtnNew1' + this.id
        });

        this.newBttn.on('click', this.resetComponents, this);
        this.addToList = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.submit"), // "Submit",
            iconCls: getButtonIconCls(Wtf.etype.save),
            tooltip: WtfGlobal.getLocaleText("acc.mrp.jobwork.submitbtn"),
            scope: this,
            handler: function () {
                this.isEdit = false;
                this.saveAndCreateNewFlag = true;                    // This flag is used to differentiate between Save button and Save and Create New button
                this.save();
            }
        });

        this.editButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.currency.update"), // "Update",
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            tooltip: WtfGlobal.getLocaleText("acc.mrp.jobwork.updatebtn"),
            disabled: true,
            scope: this,
            handler: this.editRecord.createDelegate(this, [true])
        });

        this.deleteTrans = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("mrp.workorder.report.delete"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.jobwork.deletebtn"),
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            disabled: true,
            handler: this.confirmDelete.createDelegate(this, [false, true])
        });
        this.btnArr.push(this.newBttn);
        this.btnArr.push(this.addToList);
        this.btnArr.push(this.editButton);
        this.btnArr.push(this.deleteTrans);
        var firsttbar = new Wtf.Toolbar(this.btnArr);
        this.toolbarPanel = new Wtf.Panel({
            border: false,
            items: [firsttbar]
        });

        this.saveBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            hidden: this.isViewTemplate,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.save),
            handler: function () {
                this.saveAndCreateNewFlag = false;
                this.save();
            }
        });

        this.savencreateBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.save),
            handler: function () {
                this.saveAndCreateNewFlag = true;                    // This flag is used to differentiate between Save button and Save and Create New button
                this.save();
            }
        });

        this.buttonArray = [];
        if (this.removegrid) {
            this.buttonArray.push(this.saveBttn);
            if (!this.isEdit) {
                this.buttonArray.push(this.savencreateBttn);
            }
        }
    },
    confirmDelete: function (isTempDelete, isPermDelete) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg: WtfGlobal.getLocaleText("acc.rem.146") + " " + WtfGlobal.getLocaleText("acc.machineMaster.deleteMsgJob"),
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.QUESTION,
            width: 300,
            scope: {
                scopeObject: this
            },
            fn: function (btn) {
                if (btn == "yes") {
                    var obj = {};
                    obj.isTempDelete = isTempDelete;
                    obj.isPermDelete = isPermDelete;
                    this.scopeObject.handleDelete(obj);
                } else {
                    return;
                }

            }
        }, this);
    },
    handleDelete: function (obj) {

        if (this.grid.JobworkGrid.getSelectionModel().hasSelection() === false) {
            WtfComMsgBox(34, 2);
            return;
        }
//        var data = [];
        var arr = [];
        this.recArr = this.grid.JobworkGrid.getSelectionModel().getSelections();

        for (i = 0; i < this.recArr.length; i++) {
            arr.push(this.recArr[i].data.id);
        }
        var data = {
            root: arr
        }
        var json = Wtf.encode(data);

        var params = {
            jsonObj: json,
            isTempDelete: obj.isTempDelete,
            isPermDelete: obj.isPermDelete
        }
        this.ajxUrl = "ACCJobWorkController/deleteJobWorkOrders.do";
        Wtf.Ajax.requestEx({
            url: this.ajxUrl,
            params: params
        }, this, this.genSuccessResponseDelete, this.genFailureResponsedelete);
    },
    genSuccessResponseDelete: function (response, request) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), "Job work has been deleted successfully."], 0);
            this.grid.Store.load();
            this.grid.JobworkGrid.getView().refresh();
        }
    },
    genFailureResponsedelete: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    editRecord: function (isEdit, isClone) {

        if (this.grid.JobworkGrid.getSelectionModel().hasSelection()) {
            this.record = this.grid.JobworkGrid.getSelectionModel().getSelected();
            this.isEdit = isEdit;
            this.loadrecord();
        }
//        calllJobWorkEntryMasterForm(null, isEdit, this.record, this);

    },
    enableDisableButtons: function () {
        var selectionModel = this.grid.JobworkGrid.getSelectionModel();
        if (selectionModel.hasSelection() && selectionModel.getCount() == 1) {
            if (this.editButton) {
                this.editButton.enable();
            }
            if (this.deleteTrans) {
                this.deleteTrans.enable();
            }

        } else if (selectionModel.getCount() > 1) {
            if (this.editButton) {
                this.editButton.disable();
            }
            if (this.deleteTrans) {
                this.deleteTrans.enable();
            }

        } else if (selectionModel.getCount() === 0) {
            if (this.editButton) {
                this.editButton.disable();
            }
            if (this.deleteTrans) {
                this.deleteTrans.disable();
            }

        }

    },
    createForm: function () {
        this.JoborderForm = new Wtf.form.FormPanel({
            region: "north",
            autoHeight: true,
            border: false,
            defaults: {
                border: false
            },
            split: true,
            layout: 'form',
            baseCls: 'northFormFormat',
            disabledClass: "newtripcmbss",
            hideMode: 'display',
            id: 'Northform' + this.id,
            cls: "visibleDisabled",
            labelWidth: 140,
            items: [{
                    xtype: 'panel',
                    id: this.id + 'requiredfieldmessagepanel',
                    hidden: true,
                    cls: 'invalidfieldinfomessage'
                }, {
                    layout: 'form',
                    defaults: {
                        border: false
                    },
                    bodyStyle: {
                        margin: "20px"
                    },
                    labelWidth: 160,
                    items: [{
                            layout: 'column',
                            defaults: {
                                border: false
                            },
                            items: [{
                                    layout: 'form',
                                    columnWidth: 0.49,
                                    items: [
                                        this.jobOrdernName, this.sequenceFormatCombobox, this.jobOrderNumber, this.workOrder, this.jobWorkDate, this.Vendor, this.productNameCombo, this.productQuantity, this.dateOfshipment
                                    ]
                                }, {
                                    layout: 'form',
                                    columnWidth: 0.49,
                                    items: [this.dateofdelivery, this.exiceseDutyCharges, this.jobWorkLocation, this.shipmentRoute, this.gatepass, this.otherRemarks]
                                }]
                        },this.tagsFieldset]
                }]
        });

    },
    createPanel: function () {
        this.itemArr = [];
        this.itemArr.push(this.JoborderForm);
        if (!this.removegrid) {
            this.itemArr.push(this.toolbarPanel);
            this.itemArr.push(this.grid);
        }



        this.newPanel = new Wtf.Panel({
            autoScroll: true,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            region: 'center',
            items: this.itemArr,
            border: false,
            bbar: this.buttonArray
        });
    },
    createGrid: function () {
        this.grid = getJobWorkOrderGrid();
//        this.grid.JobworkGrid.on('rowclick', this.handleRowClick, this);
        this.grid.JobworkGrid.getSelectionModel().on('selectionchange', this.enableDisableButtons, this);
        this.grid.Store.on("beforeload", this.setParamsBeforeLoadingJobWorkLineGrid, this);

    },
    setParamsBeforeLoadingJobWorkLineGrid: function (s, o) {

        s.baseParams.workorderid = this.workorderUUDID;
    },
    getInvalidFields: function () {
        var invalidFields = []
        this.JoborderForm.getForm().items.filterBy(function (field) {
            if (field.validate()) {
                return;
            }
            invalidFields.push(field);
        });
        var invalidCustomFieldsArray = this.tagsFieldset.getInvalidCustomFields();// Function for getting invalid custom fields and dimensions 
        for (var i = 0; i < invalidCustomFieldsArray.length; i++) {
            invalidFields.push(invalidCustomFieldsArray[i]);
        }
        return invalidFields;
    },
    save: function () {
        var isValidNorthForm = this.JoborderForm.getForm().isValid();
        var isValidCustomFields = this.tagsFieldset.checkMendatoryCombo();
        if (!isValidNorthForm || !isValidCustomFields) {
            WtfGlobal.dispalyErrorMessageDetails(this.id + "requiredfieldmessagepanel", this.getInvalidFields());
            this.JoborderForm.doLayout();
            return;
        } else {
            Wtf.getCmp(this.id + "requiredfieldmessagepanel").hide();
        }

        if (isValidNorthForm && isValidCustomFields) {
            this.saveData();
        }

    },
    saveData: function () {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.savdat"),
            msg: WtfGlobal.getLocaleText("acc.je.msg1"),
            scope: this,
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.INFO,
            width: 300,
            fn: function (btn) {
                if (btn != "yes") {
                    this.enableSaveButton();
                    return;
                }
                var jsonObject = this.JoborderForm.form.getValues();
                var id = '';
                try {
                    id = this.record.data.id;
                } catch (e) {
                    id = '';
                }
                jsonObject.vendorid = this.Vendor.getValue();
                jsonObject.workorderid = this.workOrder.getValue();
                jsonObject.jobworklocationid = this.jobWorkLocation.getValue();
                jsonObject.id = id;
                jsonObject.productid = this.productNameCombo.getValue();
                jsonObject.jobworkdate = WtfGlobal.convertToGenericDate(this.jobWorkDate.getValue());
                jsonObject.dateofdelivery = WtfGlobal.convertToGenericDate(this.dateofdelivery.getValue());
                jsonObject.dateofshipment = WtfGlobal.convertToGenericDate(this.dateOfshipment.getValue());
                jsonObject.isEdit = this.isEdit;    //ERP-30663 : Job Work Out Edit Case flag
                jsonObject.seqformat = this.sequenceFormatCombobox.getValue();
                var seqFormatRec = WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                jsonObject.seqformat_oldflag = seqFormatRec !== null ? seqFormatRec.get('oldflag') : true;

                var custFieldArr = this.tagsFieldset.createFieldValuesArray();
                if (custFieldArr.length > 0)
                    jsonObject.customfield = JSON.stringify(custFieldArr);
                WtfGlobal.onFormSumbitGetDisableFieldValues(this.JoborderForm.form.items, jsonObject);
//                this.disableSaveButton();
                WtfComMsgBox(27, 4, true);
                this.ajxUrl = "ACCJobWorkController/saveJobWork.do";
                Wtf.Ajax.requestEx({
                    url: this.ajxUrl,
                    params: jsonObject
                }, this, this.genSuccessResponse, this.genFailureResponse);
            }
        }, this);
    },
    disableSaveButton: function () {
        if (this.saveBttn) {
            this.saveBttn.disable();
        }
        if (this.savencreateBttn) {
            this.savencreateBttn.disable();
        }
    },
    enableSaveButton: function () {
        if (this.saveBttn) {
            this.saveBttn.enable();
        }
        if (this.savencreateBttn) {
            this.savencreateBttn.enable();
        }
    },
    genSuccessResponse: function (response, request) {
        Wtf.MessageBox.hide();
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
            if (this.removegrid) {
                this.refreshReportGrid();
            } else {
                this.grid.Store.load();
                this.grid.JobworkGrid.getView().refresh();
            }
            if (this.record != undefined) {
                this.record = {};
            }
            if (this.saveAndCreateNewFlag) {
                this.enableComponent();
                this.enableSaveButton();
                this.resetComponents();



            } else {
                this.disableSaveButton();
                this.disableComponent();
            }
        }

        if (!response.success) {
            this.enableSaveButton();
            this.enableComponent();
            this.setWorkOrderID();
            var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
            if (response.msg) {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
    },
    enableComponent: function () {
        if (this.JoborderForm) {
            this.JoborderForm.enable();
        }
    },
    disableComponent: function () {
        if (this.saveBttn) {
            this.saveBttn.disable();
            if (this.JoborderForm) {
                this.JoborderForm.disable();
            }
        }
    },
    resetComponents: function () {
        if (this.JoborderForm) {
            try {
                this.JoborderForm.getForm().reset();
                this.sequenceFormatStore.load();
                this.tagsFieldset.resetCustomComponents();
                Wtf.getCmp(this.id + "requiredfieldmessagepanel").hide();   //ERP-30665 : Hide Required Field message on Create New Action.
                this.setNextNumber();
                this.setWorkOrderID();                
            } catch (e) {
            }
        }
    },
    setWorkOrderID: function () {
        if (!this.removegrid) {
            this.workOrder.setValue(this.workorderUUDID);
            this.workOrder.disable();
        }
    },
    genFailureResponse: function (response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    createVendor: function () {
        callVendorDetails(true);
    },
    createProduct: function () {
        callProductDetails();
    },
    refreshReportGrid: function () {
        var comp = null;
        if (this.removegrid) {
            comp = Wtf.getCmp('joborderreporttabEntry');
        } else {
            comp = Wtf.getCmp('joborderreporttabReport');
        }
        if (comp) {
            comp.fireEvent('updatejobworkreport');
        }
    },
    createLocation: function () {
        this.win = new Wtf.LocationaddWindow({
            title: 'Add' + " Location",
            floating: true,
            closable: true,
            id: 'StoreformId' + 'Add' + this.id,
            modal: true,
            autoShow: true,
            iconCls: 'win',
            storerec: {},
            width: 420,
            action: 'Add',
            height: 470,
            layout: 'fit',
            createFlag: true,
            resizable: false,
            autoScroll: true
        }).show();
    }
});

