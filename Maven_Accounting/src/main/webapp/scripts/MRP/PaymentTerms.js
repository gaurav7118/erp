/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
Wtf.account.PaymentTerms = function(config) {

    this.id = config.moduleid;
    this.isEdit = config.isEdit!=undefined ? config.isEdit : false;
    this.record = config.record;
    
    Wtf.apply(this, config);

    Wtf.account.PaymentTerms.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.PaymentTerms, Wtf.Panel, {
    autoScroll: true,
    bodyStyle: {background: "#DFE8F6 none repeat scroll 0 0"},
    border: false,
    closable: false,
    autoHeight: true,
    initComponent: function(config) {
        Wtf.account.PaymentTerms.superclass.initComponent.call(this, config);

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
            items: [this.PaymentTermsForm]
        });
        this.add(this.newPanel);
        Wtf.account.PaymentTerms.superclass.onRender.call(this, config);
    },
    createStores: function() {
        this.TypeStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'id'
                }, {
                    name: 'value'
                }],
            data: [['1', '1'], ['2', '2']]
        });

        this.billingAddrsStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: "id"
                }, {
                    name: "name"
                }],
            data: (this.isEdit || this.isCompany) ? [] : [["Billing Address1", "Billing Address1"]]
        });

        this.pmtRec = new Wtf.data.Record.create([
            {name: 'methodid'},
            {name: 'methodname'}
//            {name: 'accountid'},
//            {name: 'acccurrency'},
//            {name: 'accountname'},
//            {name: 'isIBGBankAccount',
//                type: 'boolean'},
//            {name: 'isdefault'},
//            {name: 'detailtype',
//                type: 'int'},
//            {name: 'acccustminbudget'},
//            {name: 'autopopulate'},
//            {name: 'bankType'}
        ]);
        this.pmtStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.pmtRec),
            url: "ACCPaymentMethods/getPaymentMethods.do",
            baseParams: {
                mode: 51
            }
        });
        this.termRec = new Wtf.data.Record.create([
            {name: 'termname'},
            {name: 'termdays'},
            {name: 'termid'}
        ]);
        this.termds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.termRec),
            //        url: Wtf.req.account + 'CompanyManager.jsp',
            url: "ACCTerm/getTerm.do",
            baseParams: {
                mode: 91
            }
        });
    },
    addAddress: function(id, store) {
        
    },
    loadData: function() {
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.accountName.setValue(data.accountname);
            this.detailType.setValue(data.detailtype);
            this.autoPopulate.setValue(data.autopopulate);
            this.shownInCsCp.setValue(data.showincscp);
            this.bankName.setValue(data.bankname);
            this.bankAccountNumber.setValue(data.bankaccountnumber);
            this.bankAddress.setValue(data.bankaddress);
            if(data.paymenttermdays!=''){
                this.paymenttermdays.setValue(data.paymenttermdays);
            }
            this.paymentTermDate.setValue(data.paymenttermdate);
        }
    },
    pmtStoreOnLoad: function(){
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.paymentMethodName.setValue(data.paymentmethodid);
        }
    },
    termdsOnLoad: function(){
        if (this.record != null && this.isEdit) {
            var data = this.record.data;
            this.paymentTermName.setValue(data.paymenttermid);
        }
    },
    handlePaymentMethodOnChangfeEvent: function(combo , newVal , OldVal){
        var routingTemplateRec = WtfGlobal.searchRecord(combo.store, newVal, 'methodid');
        var recData=routingTemplateRec.data;
        
    },
    createFields: function() {



        this.paymentMethodName = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.paymentMethodName"),
            name: "paymentmethodname",
            id: this.id + 'paymentmethodname',
            maxLength: 49,
            allowNegative: false,
            width: 240,
            store: this.pmtStore,
            valueField: 'methodid',
            displayField: 'methodname',
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true,
            emptyText: WtfGlobal.getLocaleText("acc.paymentTerms.paymentMethodName.emptytext")
        });
        this.paymentMethodName.addNewFn = this.addPaymentMethod.createDelegate(this);
        this.paymentMethodName.on('change',this.handlePaymentMethodOnChangfeEvent,this);
        this.pmtStore.load();
        this.pmtStore.on('load', this.pmtStoreOnLoad, this);        


        this.accountName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.accountName"),
            name: "accountname",
            id: this.id + 'accountname',
//            allowBlank:false,
            maxLength: 49,
            width: 240,
            allowNegative: false
            
        });

        this.detailType = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.detailType"),
            name: "detailtype",
            id: this.id + 'detailtype',
//            allowBlank:false,
            maxLength: 49,
            width: 240,
            allowNegative: false
            
        });

        this.autoPopulate = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.autoPopulate"),
            name: "autopopulate",
            id: this.id + 'autopopulate',
//            allowBlank:false,
            maxLength: 49,
            width: 240,
            allowNegative: false
            
        });

        this.shownInCsCp = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.shownInCsCp"),
            name: "showincscp",
            id: this.id + 'showincscp',
            maxLength: 50,
            width: 240,
            allowNegative: false
            
        });



        this.bankName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.bankName"),
            name: 'bankname',
            id: this.id + 'bankname',
            forceSelection: true,
            width: 240, 
            valueField: 'id',
            displayField: 'name'
            
        });

        this.bankAccountNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.bankAccountNumber"),
            name: 'bankaccountnumber',
            id: this.id + 'bankaccountnumber',
            forceSelection: true,
            width: 240
            
        });

        this.bankAddress = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.bankAddress"),
            name: 'bankaddress',
            id: this.id + 'bankaddress',
            forceSelection: true,
            width: 240,
            valueField: 'id',
            displayField: 'name'
        });

        this.paymentTermName = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.paymentTermName"),
            name: 'paymenttermname',
            id: this.id + 'paymenttermname',
            store: this.termds,
            valueField: 'termid',
            forceSelection: true,
            displayField: 'termname',
//            allowBlank:false,
            maxLength: 250,
            width: 240,
            emptyText: WtfGlobal.getLocaleText("acc.paymentTerms.paymentTermName.emptytext"),
            listeners: {
                'select': {
                    fn: this.updateDueDate,
                    scope: this
                }
            },
            selectOnFocus: true
        });
        this.termds.load();
        this.termds.on('load', this.termdsOnLoad, this);

        this.paymenttermdays = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.paymentTermDays"),
            name: 'paymenttermdays',
            id: this.id + 'paymenttermdays',
            width: 240,
            allowNegative:false,
            disabled:true
        });

        this.paymentTermDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.paymentTerms.paymentTermDate"),
            name: 'paymenttermdate',
            id: "paymenttermdate" + this.id,
            hidden: false,
            width: 240,
            maxLength: 50,
            scope: this,
            format: WtfGlobal.getOnlyDateFormat(),
            emptyText: WtfGlobal.getLocaleText("acc.paymentTerms.paymentTermDate.emptytext")
        });




    },
    createForm: function() {

        this.PaymentTermsForm = new Wtf.form.FormPanel({
            region: 'north',
//            autoHeight: true,
            id: "northForm" + this.id,
            disabledClass: "newtripcmbss",
            disabled: this.readOnly,
            border: false,
//            height: 700,
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
                                        this.paymentMethodName,
                                        this.accountName,
                                        this.detailType,
                                        this.autoPopulate,
                                        this.shownInCsCp,
                                        this.bankName,
                                        this.bankAccountNumber,
                                        this.bankAddress
                                    ]
                                }, {
                                    layout: 'form',
                                    columnWidth: 0.49,
                                    items: [
                                        this.paymentTermName,
                                        this.paymenttermdays,
                                        this.paymentTermDate,
                                    ]
                                }]
                        }]
                }]

        });

    },
    save: function() {

    },
    showConfirmAndSave: function(rec, incash) {

    },
    genSuccessResponse: function(response, request) {

    },
    genFailureResponse: function(response) {


    },
    addPaymentMethod: function() {
        PaymentMethod('PaymentMethodWin');
        Wtf.getCmp('PaymentMethodWin').on('update', function() {
            this.pmtStore.reload();
        }, this);
    },
    updateDueDate: function(a, val, index, isSave) {
        var term = null;
        var rec = null;
        var validTillDate = null;

        if (this.paymentTermName.getValue() != "" && this.paymentTermName.getValue() != null && this.paymentTermName.getValue() != undefined) {
            rec = this.paymentTermName.store.getAt(this.paymentTermName.store.find('termid', this.paymentTermName.getValue()));
            var days = rec.data.termdays;
            this.paymenttermdays.setValue(days);
//            this.PaymentTermsForm.getForm().setValue(days);
        }

        if (this.ownerCt.initialConfig.items[0].creationDate.getValue() !== "") {
            if (this.paymentTermName.getValue() !== "" && this.paymentTermName.getValue() !== null && this.paymentTermName.getValue() !== undefined) {
                rec = this.paymentTermName.store.getAt(this.paymentTermName.store.find('termid', this.paymentTermName.getValue()));
                term = new Date(this.ownerCt.initialConfig.items[0].creationDate.getValue()).add(Date.DAY, rec.data.termdays);
            } else {
                term = this.ownerCt.initialConfig.items[0].creationDate.getValue();

            }

        } else {
            term = null;
        }
        this.paymentTermDate.setValue(term);


    }
});

