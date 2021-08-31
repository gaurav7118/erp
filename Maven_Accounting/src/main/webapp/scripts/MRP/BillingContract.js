/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.BillingContract = function(config) {
    this.id = config.moduleid;
    this.customerName = config.customerName;
    this.currentaddress = config.currentaddress != undefined ? config.currentaddress : "";
    this.isEdit = config.isEdit!=undefined ? config.isEdit : false;
    this.record = config.record;
    
    Wtf.apply(this, config);

    Wtf.account.BillingContract.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.BillingContract, Wtf.Panel, {
    autoScroll: true,
    bodyStyle: {background: "#DFE8F6 none repeat scroll 0 0"},
    border: false,
    closable: false,
    autoHeight: true,
    initComponent: function(config) {
        Wtf.account.BillingContract.superclass.initComponent.call(this, config);

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
            items: [this.BillingContractForm]

        });
        this.add(this.newPanel);
        
        this.billingAddrsCombo.on('beforeselect',function(combo){this.billingComboValueBeforeSelect=combo.getValue();},this);
        this.billingAddrsCombo.on('select',this.setBillingAddressDataOnSelect,this);
        
        Wtf.account.BillingContract.superclass.onRender.call(this, config);
    },
    createStores: function() {
        this.addrsRec = new Wtf.data.Record.create([
            {name:'aliasName'},
            {name:'address'},
            {name:'county'},
            {name:'city'},
            {name:'state'},
            {name:'country'},
            {name:'postalCode'},
            {name:'phone'},
            {name:'mobileNumber'},
            {name:'fax'},
            {name:'emailID'},
            {name:'contactPerson'},
            {name:'recipientName'},
            {name:'contactPersonNumber'},
            {name:'contactPersonDesignation'},
            {name:'website'},
            {name:'shippingRoute'},
            {name:'isDefaultAddress'},
            {name:'isBillingAddress'}
        ]);

        this.billingAddrsStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.addrsRec),
            url :"ACCCustomer/getAddresses.do",
            baseParams:{
                isBillingAddress:true
//                customerid: this.customerName
            }
        });
        this.billingAddrsStore.on('load', this.billingAddrsStoreOnLoad, this); 
    },
    addAddress: function(id, store) {
        
    },
    loadData: function() {
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.billingAliasName.setValue(data.billingaliasname);
            this.billingAddress.setValue(data.billingaddress);
            this.billingCounty.setValue(data.billingcounty);
            this.billingCity.setValue(data.billingcity);
            this.billingState.setValue(data.billingstate);
            this.billingCountry.setValue(data.billingcountry);
            this.billingPostal.setValue(data.billingpostal);
            this.billingPhone.setValue(data.billingphone);
            this.billingMobile.setValue(data.billingmobile);
            this.billingFax.setValue(data.billingfax);
            this.billingEmail.setValue(data.billingemail);

            this.billingRecipientName.setValue(data.billingrecipientname);
            this.billingContactPerson.setValue(data.billingcontactperson);
            this.billingContactNumber.setValue(data.billingcontactpersonnumber);
            this.billingContactDesignation.setValue(data.billingcontactpersondesignation);
            this.billingWebsite.setValue(data.billingwebsite);
        }
    },
    billingAddrsStoreOnLoad: function(){
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.billingAddrsCombo.setValue(data.billingaddresscombo);
        }
    },
    createFields: function() {
        this.achorValue = '70%';
        
        this.billingAddrsCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            name:'billingaddresscombo',
            valueField:'aliasName',
            displayField:'aliasName',
            disabled: this.viewGoodReceipt || this.isViewTemplate,
            store:this.billingAddrsStore,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectBillingAddress..."), 
            fieldLabel: WtfGlobal.getLocaleText("acc.field.BillingAddress*"),
            width: 240,
            typeAhead: true,
            allowBlank: false,
            forceSelection: true
        });
        
        this.billingAliasName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.AliasName") + "*",
            name: "billingaliasname",
            id: this.id + 'billingaliasname',
            allowBlank: false,
            maxLength: 49,
            allowNegative: false,
            width: 240
        });
        
        this.billingAddress = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Address") + "*",
            name: "billingaddress",
            id: this.id + 'billingaddress',
            maxLength: 250,
            height: 60,
            width: 240,
            allowBlank: false,
            allowNegative: false
            
        });

        this.billingCounty = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
            name: "billingcounty",
            id: this.id + 'billingcounty',
            width: 240,
            maxLength: 49,
            allowNegative: false,
            hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
            hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)            
        });
        
        this.billingCity = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
            name: "billingcity",
            id: this.id + 'billingcity',
            width: 240,
            maxLength: 49,
            allowNegative: false
            
        });

        this.billingState = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
            name: "billingstate",
            id: this.id + 'billingstate',
//            allowBlank:false,
            maxLength: 49,
            width: 240,
            allowNegative: false
            
        });

        this.billingCountry = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Country"),
            name: "billingcountry",
            id: this.id + 'billingcountry',
            width: 240,
            maxLength: 49,
            allowNegative: false
            
        });

        this.billingPostal = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name: "billingpostal",
            id: this.id + 'billingpostal',
//            allowBlank:false,
            width: 240,
            maxLength: 50,
            allowNegative: false
            
        });

        this.billingPhone = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Phone"),
            name: 'billingphone',
            id: this.id + 'billingphone',
            width: 240,
            maxLength: 250
            
        });

        this.billingMobile = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Mobile"),
            name: 'billingmobile',
            id: this.id + 'billingmobile',
//            allowBlank:false,
            width: 240,
            maxLength: 250
            
        });

        this.billingFax = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Fax"),
            name: 'billingfax',
            id: this.id + 'billingfax',
            width: 240,
            maxLength: 250
            
        });

        this.billingEmail = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Email"),
            name: 'billingemail',
            id: this.id + 'billingemail',
//            allowBlank:false,
            width: 240,
            maxLength: 254,
            validator: WtfGlobal.validateMultipleEmail
            
        });

        this.billingRecipientName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'billingrecipientname',
            id: this.id + 'billingrecipientname',
            width: 240,
            maxLength: 200
            
        });

        this.billingContactPerson = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPerson"),
            name: 'billingcontactperson',
            id: this.id + 'billingcontactperson',
//            allowBlank:false,
            width: 240,
            maxLength: 200
            
        });

        this.billingContactNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
            name: 'billingcontactpersonnumber',
            id: this.id + 'billingcontactpersonnumber',
            width: 240,
            maxLength: 250
            
        });
        this.billingContactDesignation = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
            name: 'billingcontactpersondesignation',
            id: this.id + 'billingcontactpersondesignation',
            width: 240,
            maxLength: 250
            
        });
        this.billingWebsite = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'billingwebsite',
            id: this.id + 'billingwebsite',
            maxLength: 250,
            width: 240
            
        });

    },
    setNextNumber: function(config) {

    },
    getInvalidFields: function() {
        var invalidFields = []
        this.BillingContractForm.getForm().items.filterBy(function(field) {
            if (field.validate())
                return;
            invalidFields.push(field);
        });
        return invalidFields;
    },
    getNextSequenceNumber: function(a, val) {
    },
    createForm: function() {

        this.BillingContractForm = new Wtf.form.FormPanel({
            region: 'north',
            id: "northForm" + this.id,
            disabledClass: "newtripcmbss",
            disabled: this.readOnly,
            border: false,
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
                                        this.billingAddrsCombo,
                                        this.billingAliasName,
                                        this.billingAddress,
                                        this.billingCounty,
                                        this.billingCity,
                                        this.billingState,
                                        this.billingCountry,
                                        this.billingPostal,
                                        this.billingPhone,
                                        this.billingMobile,
                                        this.billingFax,
                                        this.billingEmail
                                    ]
                                }, {
                                    layout: 'form',
                                    columnWidth: 0.49,
                                    items: [
                                        this.billingRecipientName,
                                        this.billingContactPerson,
                                        this.billingContactNumber,
                                        this.billingContactDesignation,
                                        this.billingWebsite
                                    ]
                                }]
                        }]
                }]

        });

    },
    save: function() {
        var isValidNorthForm = this.BillingContractForm.getForm().isValid();

        if (!isValidNorthForm) {
            WtfGlobal.dispalyErrorMessageDetails(this.id + "requiredfieldmessagepanel", this.getInvalidFields());
            this.BillingContractForm.doLayout();
            return;
        } else {
            Wtf.getCmp(this.id + "requiredfieldmessagepanel").hide();
        }
        if (isValidNorthForm) {
            this.saveData();
        }
    },
    showConfirmAndSave: function(rec, incash) {

    },
    genSuccessResponse: function(response, request) {

    },
    genFailureResponse: function(response) {


    },
    saveData: function() {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.savdat"),
            msg: WtfGlobal.getLocaleText("acc.je.msg1"),
            scope: this,
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.INFO,
            width: 300,
            fn: function(btn) {
                if (btn != "yes") {
                    this.enableSaveButton();
                    return;
                }
                var jsonObject = this.machineForm.form.getValues();
//                WtfGlobal.onFormSumbitGetDisableFieldValues(this.BillingContractForm.form.items, jsonObject);
                this.disableSaveButton();
                WtfComMsgBox(27, 4, true);
//                this.ajxUrl = "ACCMachineMasterCMN/getMachineMasterDetails.do";
                Wtf.Ajax.requestEx({
                    url: this.ajxUrl,
                    params: jsonObject
                }, this, this.genSuccessResponse, this.genFailureResponse);
            }
        }, this);
    },
    enableSaveButton: function() {
        this.saveBttn.enable();
        this.savencreateBttn.enable();
    },
    disableSaveButton: function() {
        this.saveBttn.disable();
        this.savencreateBttn.disable();
    },
    setBillingAddressDataOnSelect:function(combo,rec){
        if(combo.getValue()==this.billingComboValueBeforeSelect){ //If same name selected no need to do any action 
            return;
        }
        if(this.currentaddress!="" && this.currentaddress.billingAddrsCombo==rec.data.aliasName){
            this.setBillingAddress(this.currentaddress); 
        } else {
            this.setDefaultBillingAddress(rec.data); 
        }       
    },
    setBillingAddress:function(addr){
        this.billingAliasName.setValue(addr.aliasName);
        this.billingAddress.setValue(addr.billingAddress);
        this.billingCounty.setValue(addr.billingCounty);
        this.billingCity.setValue(addr.billingCity);
        this.billingState.setValue(addr.billingState);
        this.billingCountry.setValue(addr.billingCountry);
        this.billingPostal.setValue(addr.billingPostal);
        this.billingPhone.setValue(addr.billingPhone);
        this.billingMobile.setValue(addr.billingMobile);
        this.billingFax.setValue(addr.billingFax);
        this.billingEmail.setValue(addr.billingEmail);
        this.billingRecipientName.setValue(addr.billingRecipientName);
        this.billingContactPerson.setValue(addr.billingContactPerson);
        this.billingContactNumber.setValue(addr.billingContactPersonNumber);  
        this.billingContactDesignation.setValue(addr.billingContactPersonDesignation);
        this.billingWebsite.setValue(addr.billingWebsite);
    },
    setDefaultBillingAddress:function(addr){
        this.billingAliasName.setValue(addr.aliasName);
        this.billingAddress.setValue(addr.address);
        this.billingCounty.setValue(addr.county);
        this.billingCity.setValue(addr.city);
        this.billingState.setValue(addr.state);
        this.billingCountry.setValue(addr.country);
        this.billingPostal.setValue(addr.postalCode);
        this.billingPhone.setValue(addr.phone);
        this.billingMobile.setValue(addr.mobileNumber);
        this.billingFax.setValue(addr.fax);
        this.billingEmail.setValue(addr.emailID);
        this.billingRecipientName.setValue(addr.recipientName);
        this.billingContactPerson.setValue(addr.contactPerson);
        this.billingContactNumber.setValue(addr.contactPersonNumber);  
        this.billingContactDesignation.setValue(addr.contactPersonDesignation);
        this.billingWebsite.setValue(addr.website);
    }
});
