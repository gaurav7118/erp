/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.DocumentRequired = function(config) {

    this.id = config.id;
    this.modeName = config.modeName;
    this.isEdit = config.isEdit!=undefined ? config.isEdit : false;
    this.record = config.record;
    
    Wtf.apply(this, config);

    Wtf.account.DocumentRequired.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.DocumentRequired, Wtf.Panel, {
    autoScroll: true,
    bodyStyle: {background: "#DFE8F6 none repeat scroll 0 0"},
    border: false,
    closable: false,
    autoHeight: true,
    initComponent: function(config) {
        Wtf.account.DocumentRequired.superclass.initComponent.call(this, config);

        this.createStores();
        
        this.createFields();
        
        this.createForm();

    },
    loadRecord: function() {



    },
    onRender: function(config) {

        this.newPanel = new Wtf.Panel({
            autoScroll: true,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            region: 'center',
            border: false,
            items: [this.DocumentRequired]

        });
        
        this.add(this.newPanel);
        
        Wtf.account.DocumentRequired.superclass.onRender.call(this, config);
    },
    createStores: function() {
        this.TypeStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'id'
                }, {
                    name: 'value'
                }],
            data: [['1', 'Yes'], ['2', 'No']]
        });

        this.billingAddrsStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: "id"
                }, {
                    name: "name"
                }],
            data: (this.isEdit || this.isCompany) ? [] : [["Billing Address1", "Billing Address1"]]
        });

        this.countryAggrementRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.countryAggrementStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 46
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.countryAggrementRec)
        });
        this.countryAggrementStore.on('load', this.countryAggrementStoreOnLoad, this);
        this.countryAggrementStore.load();
    },
    loadData: function() {
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.contractorTeeName.setValue(data.contractorTeeName);
            this.PANNumber.setValue(data.PANNumber);
            this.TANNumber.setValue(data.TANNumber);
            this.dateOfAggrement.setValue(data.dateOfAggrement);
            this.countryAggrement.setValForRemoteStore(this.record.data.countryAggrement, this.record.data.countryAggrementname)
            this.stateAggrement.setValue(data.stateAggrement);
            this.previousContractId.setValue(data.previousContractId);
            this.remarks.setValue(data.remarks);
        }
    },
    countryAggrementStoreOnLoad: function(){
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.countryAggrement.setValue(data.countryAggrement);
        }
    },
    addAddress: function(id, store) {
        
    },
    createFields: function() {
        this.customerName = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.documentRequired.customerName"),
            id: "customer" + this.id,
            store: Wtf.customerAccRemoteStore,
            valueField: 'accid',
            displayField: 'accname',
            minChars: 1,
            name: 'customer',
            listWidth: 400,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            mode: 'remote',
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            typeAhead: true,
            typeAheadDelay: 30000,
            forceSelection: true,
            selectOnFocus: true,
            width: 240,
            triggerAction: 'all',
            scope: this,
            disabled:true
        });
       
        this.contractorTeeName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.documentRequired.contractorTeeName"),
            name: "contractorTeeName",
            id: this.id + 'contractorTeeName',
            maxLength: 49,
            width: 240,
            allowNegative: false
            
        });

        this.PANNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.documentRequired.PANNumber"),
            name: "PANNumber",
            id: this.id + 'PANNumber',
            maxLength: 49,
            width: 240,
            allowNegative: false
            
        });

        this.TANNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.documentRequired.TANNumber"),
            name: "TANNumber",
            id: this.id + 'TANNumber',
            maxLength: 49,
            width: 240,
            allowNegative: false
            
        });

        this.dateOfAggrement = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.documentRequired.dateOfAggrement"),
            name: 'dateOfAggrement',
            id: "dateOfAggrement" + this.id,
            hidden: false,
            width: 240,
            maxLength: 50,
            scope: this,
            format: WtfGlobal.getOnlyDateFormat()
        });

        this.countryAggrement = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.documentRequired.countryAggrement"),
            name: "countryAggrement",
            mode: 'remote',
            hiddenName: 'countryAggrement',
            id: 'countryAggrement' + this.id,
            store: this.countryAggrementStore,
            triggerAction: 'all',
            width: 240,
            listWidth: 300,
            typeAhead: true,
            forceSelection: true,
            valueField: 'id',
            displayField: 'name',
            extraFields: [],
            addNoneRecord: true
        });
        this.countryAggrement.addNewFn = this.addCountryAggrement.createDelegate(this);

        this.stateAggrement = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.documentRequired.stateAggrement"),
            name: 'stateAggrement',
            id: this.id + 'stateAggrement',
            maxLength: 250,
            width: 240
            
        });

        this.previousContractId = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.documentRequired.previousContractId"),
            name: 'previousContractId',
            id: this.id + 'previousContractId',
            maxLength: 250,
            width: 240
            
        });

        this.remarks = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Remarks"),
            name: 'remarks',
            id: this.id + 'remarks',
            maxLength: 254,
            height: 50,
            width: 240
        });

        this.attachment = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.documentRequired.attachment"),
            scope: this,
            handler: this.showContractAttachWindow
        });

    },
    createForm: function() {

        this.DocumentRequired = new Wtf.form.FormPanel({
            region: 'north',
            id: "northForm" + this.id,
            disabledClass: "newtripcmbss",
            disabled: this.readOnly,
            border: false,
            fileUpload: true,
            items: [{
                    layout: 'form',
                    defaults: {
                        border: false
                    },
                    baseCls: 'northFormFormat',
                    cls: "visibleDisabled",
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
                                        this.customerName,
                                        this.contractorTeeName,
                                        this.PANNumber,
                                        this.TANNumber,
                                        this.dateOfAggrement,
                                        this.countryAggrement,
                                        this.stateAggrement,
                                        this.previousContractId
                                    ]
                                }, {
                                    layout: 'form',
                                    columnWidth: 0.49,
                                    items: [
                                        this.remarks,
                                        this.attachment
                                    ]
                                }]
                        }]
                }]

        });

    },
    addCountryAggrement: function() {
        addMasterItemWindow('46');
    },
    save: function() {

    },
    showConfirmAndSave: function(rec, incash) {

    },
    genSuccessResponse: function(response, request) {

    },
    genFailureResponse: function(response) {


    },
    showContractAttachWindow: function () {
        this.imageUploadURL = (this.savedFilesMappingId == undefined || this.savedFilesMappingId == null) ? 'ACCContractMasterCMN/attachDocuments.do?type=doc' : 'ACCContractMasterCMN/attachDocuments.do?type=doc&savedFilesMappingId=' + this.savedFilesMappingId;
        var scopeObj = this;
        var comp = Wtf.getCmp('loanFileUploalWindow');
        if (!comp) {
            this.uploadForm = new Wtf.MRPMultiFlieUploadPanel({
                methodType: 'upload',
                id: 'loanFileUploalWindow',
                layout: 'fit',
                closable: false,
                border: false,
                url: this.imageUploadURL,
                savedFilesMappingId: this.savedFilesMappingId,
                isDisbursement: true,
                fileStr: this.isFromSaveAndCreateNewButton ? undefined : scopeObj.attachedFilesStr
            });
            var buttons =[new Wtf.MRPFileBrowseButton({
                        text: WtfGlobal.getLocaleText("acc.activitydetailpanel.addfilesBTN"), //'Add Files',
                        tooltip: WtfGlobal.getLocaleText("acc.template.addfiles.ttip"), //'Click here to browse and add your files to upload',
                        handler: function (btn) {
                            this.uploadForm.addFiles(btn);
                        },
                        scope: this
                    }), {
                        text: WtfGlobal.getLocaleText("acc.uploadbtn"), //'Upload',
                        tooltip: WtfGlobal.getLocaleText("acc.template.uploadbtn.ttip"), //'Click here to start uploading your files which are listed above',
                        handler: function () {
                            this.uploadForm.startUpload();
                        },
                        scope: this
                    }, {
                        text: WtfGlobal.getLocaleText("acc.template.stop"), //'Stop',
                        tooltip: WtfGlobal.getLocaleText("acc.template.canceluploading.ttip"), //'Cancel uploading of files which are not uploaded yet',
                        handler: function () {
                            this.uploadForm.cancelUpload();
                        },
                        scope: this
                    }, {
                        text: WtfGlobal.getLocaleText("acc.template.clear"), //'Clear',
                        tooltip: WtfGlobal.getLocaleText("acc.template.removefilesttip"), //'remove all files which are listed above',
                        handler: function () {
                            this.uploadForm.clearAll();
                        },
                        scope: this
                    },{
                        text: WtfGlobal.getLocaleText("acc.msgbox.ok"), //'ok',
                        //            tooltip : WtfGlobal.getLocaleText("acc.template.removefilesttip"),//'remove all files which are listed above',
                        handler: function () {
                            this.attachWindow.close();
                        },
                        scope: this
                    }]

            this.uploadForm.on('uploadComplete', function () {
                this.savedFilesMappingId = this.uploadForm.savedFilesMappingId;
                this.attachedFilesStr = this.uploadForm.savedFilesId;
            }, this);
            this.attachWindow = new Wtf.Window({
                width: 600,
                height: 300,
                modal: true,
                title: WtfGlobal.getLocaleText("acc.contract.uploadnewfile"), //'Upload File',
                layout: 'fit',
                items: [this.uploadForm],
                buttonAlign:'left',
                buttons:buttons
            });
            this.attachWindow.show();
        }

    }

});


