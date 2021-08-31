/* 
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* 
 * Old this.val values 
 1 = Payment Against Invoice
 4 = Against Contra Entry
 5 = Advance Payment
 6 = Receive/Make payment from Customer/ Vendor
 7 = Receive payment against Debit Note / Make payment against Credit note
 8 = Receive payment against Credit Note / Make payment against Debit note
 9 = Against GL Code
 
 * Payment method type - 
 0 = Cash
 1 = Card
 2 = Bank
 
 * 
 isReceipt = 'TRUE' for Receive Payment and 'FALSE' for Make Payment
 1 = Payment Against Vendor
 2 = Payment Against Customer
 3 = Payment Against GL Code
 */


Wtf.account.ReceiptEntry = function(config) {
    this.isReceipt = config.isReceipt;
    this.moduleid = config.moduleId;
    //Flag to indicate whether Avalara integration is enabled and module is enabled for Avalara Integration or not
    this.isModuleForAvalara = (Wtf.account.companyAccountPref.avalaraIntegration && (config.moduleid == Wtf.Acc_Receive_Payment_ModuleId)) ? true : false;
    this.modeName = config.modeName;
    this.helpmodeid = config.helpmodeid;
    this.isEdit = config.isEdit;
    this.isIndiaGST=WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.istaxonadvancereceipt;   //ERP-32829 
    this.isShipping=CompanyPreferenceChecks.getGSTCalCulationType();
    this.CustomerVendorTypeId="";
    this.GSTINRegistrationTypeId="";
    this.gstin="";
    this.gstdochistoryid="";
    this.ignoreHistory=false;
    this.readOnly = config.readOnly;
    this.record = config.record;
    // map default payment method to customer check
    this.mapDefaultPmtMethod =CompanyPreferenceChecks.mapDefaultPaymentMethod();
    /*
     * isbulkpayment is true if click on bulkpayment from invoice report
     */
    this.isBulkPayment=(config.isBulkPayment == null || config.isBulkPayment == undefined)? false : config.isBulkPayment;
    this.invObj=config.invObj;
    this.id = config.id;
    this.isLinkedToClaimedInvoice = false;
    this.paymentDetailsRecord=config.paymentDetailsRecord;
    this.loanFlag=(config.loanFlag == null || config.loanFlag == undefined)? false : config.loanFlag;
    this.paymentType = config.paymentType;
    this.currencyid = null;
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
     /*
     *Check for Warning message If balance amount is Greater than zero
     */
    this.checkWarning=true;
    this.uPermType=(this.isReceipt?Wtf.UPerm.salesreceivepayment:Wtf.UPerm.purchasemakepayment);
    this.permType=(this.isReceipt?Wtf.Perm.salesreceivepayment:Wtf.Perm.purchasemakepayment);
    this.exportPermType=(this.isReceipt?this.permType.exportdatareceipt:this.permType.exportdatapayment);
    this.printPermType=(this.isReceipt?this.permType.printreceipt:this.permType.printpayment);
    this.emailPermType=(this.isReceipt?this.permType.emailreceipt:this.permType.emailpayment);
    this.isAllowedSpecificFields=(config.isAllowedSpecificFields == null || config.isAllowedSpecificFields == undefined)? false : config.isAllowedSpecificFields;
    this.isCopyReceipt = config.isCopyReceipt;
    this.LifoFifoMode=false;         // Will be true when Lifo Fifo Invoices are loaded    
    this.receiptCurrencyBeforeSelect = "";//used to refresh grid on payment method change.
    this.pendingApproval=(config.ispendingAproval == null || config.ispendingAproval == undefined)? false : config.ispendingAproval;
    this.paymentOption = {
        AgainstCustomer: 1,
        AgainstVendor: 2,        
        AgainstGL: 3
    }
    this.paymentMethodType = {
        Cash: 0,
        Card: 1,
        Bank: 2
    }
    this.isCustomer = (this.record)?this.record.data.paymentwindowtype==this.paymentOption.AgainstCustomer:config.isCustomer;
    this.pmtRec = new Wtf.data.Record.create([
        {name: 'methodid'},
        {name: 'methodname'},
        {name: 'accountid'},
        {name: 'acccurrency'},
        {name: 'accountname'},
        {name: 'isIBGBankAccount',
            type: 'boolean'},
        {name: 'isdefault'},
        {name: 'detailtype',
            type: 'int'},
        {name: 'acccustminbudget'},
        {name: 'autopopulate'},
        {name: 'autopopulateinloan'},
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

    this.personRec = new Wtf.data.Record.create([
        {name: 'accid'},
        {name: 'acccode'},
        {name: 'accountid'},
        {name: 'accname'},
        {name: 'currencyid'},
        {name: 'isVendor'},
        {name: 'groupname'},
        {name: 'hasAccess'},//SDP-13638
        {name: 'paymentCriteria'},
        {name:'overseas',mapping:'overseas'}
    ]);
    this.personAccStore = new Wtf.data.Store({
        url: "ACCAccountCMN/getAccountsForCombo.do",
        baseParams: {
            nondeleted: true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad: false
        }, this.personRec)
    });
    this.creationDate = new Wtf.ServerDateField({
        name: "creationdate",
        format: WtfGlobal.getOnlyDateFormat(),
        value: new Date(),
        disabled: this.readOnly,
        id: "date" + config.helpmodeid + this.id,
        fieldLabel: WtfGlobal.getLocaleText("acc.mp.date"), //"Date*",
        anchor: '85%',
        allowBlank: false
    });
    this.currencyRec = new Wtf.data.Record.create([
        {name: 'currencyid',
            mapping: 'tocurrencyid'},
        {name: 'symbol'},
        {name: 'currencyname',
            mapping: 'tocurrency'},
        {name: 'exchangerate'},
        {name: 'htmlcode'},
        {name: 'erdid',mapping:'id'},
        {name: 'companyid'},
        {name: 'fromcurrencyid'},
        {name: 'fromcurrency'},
        {name: 'currencycode'},
        {name: 'ismaxnearestexchangerate'}
    ]);
    this.currencyStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count"
        }, this.currencyRec),
        url: "ACCCurrency/getCurrencyExchange.do"
    });
    this.pmtStore.on('beforeload',function(s,o){
        if(!o.params)o.params={};
        var currentBaseParams = this.pmtStore.baseParams;
        currentBaseParams.loanFlag=this.loanFlag;
        this.pmtStore.baseParams=currentBaseParams;        
    },this); 
    Wtf.customerAccRemoteStore.on('beforeload',function(s,o){
        if(!o.params)o.params={};
        var currentBaseParams =  Wtf.customerAccRemoteStore.baseParams;
        currentBaseParams.loanFlag=this.loanFlag;
        Wtf.customerAccRemoteStore.baseParams=currentBaseParams; 
    },this);
    this.currencyStore.on("load", function(store) {
//        this.setPMData();
        if (this.isEdit) {
            this.Currency.setValue(this.record.data.currencyid);
            this.symbol = this.record.data.currencysymbol;
        }if (this.loadCurrStore && Wtf.account.companyAccountPref.currencyid) {
            if (this.pmtMethod != undefined && this.pmtMethod.getValue() != undefined) {
                var record = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
                if (record != null && record != undefined && record.data != undefined && record.data.acccurrency != undefined) {
                    this.Currency.setValue(record.data.acccurrency);
                    this.loadCurrStore = false;
                } else {
                    this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
                    this.loadCurrStore = false;
                }
            } else {
                this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
                this.loadCurrStore = false;
            }

        } else if (this.bankAccSelected) {
          //  this.Currency.setValue(this.bankAccCurrency);
            this.updateExternalCurrencyRateOnCurrencyChange();
        } else if (this.Currency.getValue() == "" || this.Currency.getValue() == undefined) {
            this.Currency.setValue(WtfGlobal.getCurrencyID());
        }
        if (this.Currency.getValue() != WtfGlobal.getCurrencyID() && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null) {
            this.Currency.setValue("");
            callCurrencyExchangeWindow();
            this.creationDate.setValue("");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.10")], 2);
        } else {
            var index = 3;
            if (this.Currency.getValue() != "") {
                var FIND = this.Currency.getValue();
                index = this.currencyStore.findBy(function(rec) {
                    var parentname = rec.data['currencyid'];
                    if (parentname == FIND)
                        return true;
                    else
                        return false
                })
            }
            this.applyTemplate(this.currencyStore, index);
            this.updateSouthTemp();
//            this.checkForMultiCurrencyToDecideExchangeRate();
        }
        if ((!this.isEdit) && (!this.isCopyReceipt)){
            if (this.pmtMethod != undefined && this.pmtMethod.getValue() != undefined) {
                var rec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
                if (rec != null && rec != undefined && rec.data != undefined && rec.data.acccurrency != undefined) {
                    if(this.Currency.getValue()!=(rec.data.acccurrency)){
                        this.Currency.setValue(rec.data.acccurrency); 
                    }
                }
            }
        }
        this.receiptCurrencyBeforeSelect = this.Currency.getValue();
    }, this)


    var currencyHelp = WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.SelectaCurrencytoprocesstransactionofrequiredcurrency"));

    this.Currency = new Wtf.form.FnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.mp.cur") + currencyHelp, //'Currency*',
        hiddenName: 'currencyid',
        name:'currencyid',
        id: "currency" + config.helpmodeid + this.id,
        anchor: '85%',
        allowBlank: false,
        store: this.currencyStore,
        disabled: this.isEdit,
        valueField: 'currencyid',
        forceSelection: true,
        displayField: 'currencyname',
        scope: this,
        selectOnFocus: true
    });
    this.currencyStore.load({
            params: {
                grouper: 'paymentTrans',
                mode: 201,
                transactiondate:this.isEdit?WtfGlobal.convertToGenericDate(this.record.data.billdate):WtfGlobal.convertToGenericDate(new Date())
            }
        });
    this.ShowOnlyOneTime = new Wtf.form.Checkbox({
        name: 'ShowOnlyOneTime',
        fieldLabel: WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime"),
        id: 'ShowOnlyOneTime' + this.helpmodeid + this.id,
        checked: false,
        hideLabel: (this.isEdit != undefined && !config.isCustomer) || this.isEdit || !config.isCustomer,
        hidden: ((this.isEdit != undefined) && !config.isCustomer) || this.isEdit || !config.isCustomer,
        cls: 'custcheckbox',
        width: 10
    });
    this.ShowOnlyOneTime.on('check', function(obj, isChecked) {
        this.isOneTimeCustomer(isChecked);
    }, this);

    this.pmtMethod = new Wtf.form.FnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.mp.payMethod"),
        name: "pmtmethod",
        hiddenName:'pmtmethod',
        store: this.pmtStore,
        id: "paymentMethod" + config.helpmodeid + this.id,
        valueField: 'methodid',
        displayField: 'methodname',
        allowBlank: false,
        disabled: this.readOnly,
        emptyText: (config.isReceipt ? WtfGlobal.getLocaleText("acc.rp.recaacc") : WtfGlobal.getLocaleText("acc.mp.selpayacc")),
        anchor: '85%',
//        mode: 'remote',
        triggerAction: 'all',
        typeAhead: true,
        forceSelection: true//,
    });
    this.pmtStore.on('load', this.setPMData, this);
    this.pmtStore.load();
    this.pmtMethodAcc = new Wtf.form.TextField({
        name: "paymentmethodaccname",
        disabled: true,
        id: "pmtmethodacc"+this.helpmodeid+this.id,
        fieldLabel: WtfGlobal.getLocaleText("acc.field.PaymentAccount"),
        anchor: '85%'
    });

    this.isChequePrint = false;
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.edit))
        this.pmtMethod.addNewFn = this.addPaymentMethod.createDelegate(this)
    this.pmtMethod.on('change', this.onPaymentMethodChange, this);
    this.pmtMethod.on('select', this.onPaymentMethodSelect, this);// Event is added for setting the currency of the payment method account again , after changing the currency from currency combobox
    this.pmtMethod.on('beforeselect', this.onPaymentMethodBeforeSelect, this);
    this.getSequenceFormatCombo();
    
    this.No = new Wtf.form.TextField({
        fieldLabel: config.isReceipt ? WtfGlobal.getLocaleText("acc.rp.RecNO") : WtfGlobal.getLocaleText("acc.mp.payNo"), //this.transectionName+' No*',
        id: "receiptNo" + config.helpmodeid + this.id,
        name: 'no',
        disabled: this.isEdit,
        anchor: '85%',
        maxLength: 45,
        allowBlank: false
    });
    this.bankCharges = new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.BankCharges"),
        id: "bankCharges" + config.helpmodeid + this.id,
        hiddenName: 'bankCharges',
        name : 'bankCharges',
        maskRe: /[0-9.]/,
        disabled: true,
        anchor: '85%',
        maxLength: 45,
        validator: function(val) {
            if (val>0) {
                return true;
            } else {
                return "Value should be greater than 0";
            }
        }
    });
    this.bankCharges.on('blur', this.setroundedvalue.createDelegate(this,[this.bankCharges,true]),this);
    this.bankInterest = new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.BankInterest"),
        id: "bankInterest" + config.helpmodeid + this.id,
        name: 'bankInterest',
        hiddenName: 'bankInterest',
        maskRe: /[0-9.]/,
        disabled: true,
        anchor: '85%',
        maxLength: 45,
//        hidden:true,
//        hideLabel:true,
        validator: function(val) {
            if (val>0) {
                return true;
            } else {
                return "Value should be greater than 0";
            }
        }
    });
    this.bankInterest.on('blur', this.setroundedvalue.createDelegate(this,[this.bankInterest,false]),this);
    this.accRec = Wtf.data.Record.create([
        {name: 'accountname',
            mapping: 'accname'},
        {name: 'accountid',
            mapping: 'accid'},
        {name: 'acccode'},
        {name: 'groupname'},
        {
            name: 'hasAccess',
            type: 'boolean',
            convert: function (value) {
                if (value == true || value == 'true' || value == 'T' ) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    ]);
    this.accountAccessStore = new Wtf.data.Store({
        url: "ACCAccountCMN/getAccountsIdNameForCombo.do",
        baseParams: {
            mode: 2,
            nondeleted: true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.accRec)
    });
//    this.accountAccessStore.load();
    this.accountStore = new Wtf.data.Store({
        url: "ACCAccountCMN/getAccountsForCombo.do",
        baseParams: {
            mode: 2,
            nondeleted: true,
            ignorecustomers:true,
            ignorevendors : true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.accRec)
    });
    this.bankChargesAccount = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.BankChargesAccount"),
        hiddenName: 'bankChargesCmb',
        store: this.accountStore,
        minChars: 1,
        id:'bankChangesAccount'+this.helpmodeid+this.id,
        anchor: '85%',
        valueField: 'accountid',
        displayField: 'accountname',
        name: 'bankChargesCmb',
        forceSelection: true,
        isAccountCombo:true,
        hirarchical: true,
        emptyText: WtfGlobal.getLocaleText("acc.mp.selectBankChargesAccount"),
        disabled: this.readOnly,
        extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'],
        mode: 'remote',
        extraComparisionField: 'acccode', // type ahead search on acccode as well.
        listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 500 : 400
    });
    this.bankChargesAccount.on('beforeselect',function(combo,record,index){
        return validateSelection(combo,record,index);
    },this);
    this.bankChargesAccount.on('select',function(){
        this.bankCharges.enable();
        this.bankCharges.allowBlank=false;
    },this);
    this.bankChargesAccount.on('blur',function(){
        if(this.bankChargesAccount.getRawValue()==''){
            this.bankChargesAccount.clearValue();
            this.bankCharges.setValue('');
            this.bankCharges.disable();
            this.bankCharges.allowBlank=true;
            this.bankCharges.validate();
        }else{
            this.bankCharges.allowBlank=false;
            this.bankCharges.validate();
        }
    },this);
    this.bankInterestAccount = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.BankInterestAccount"),
        hiddenName: 'bankInterestCmb',
        store: this.accountStore,
        minChars: 1,
        anchor: '85%',
        id:'bankInterestAccount'+this.helpmodeid+this.id,
        valueField: 'accountid',
        displayField: 'accountname',
        name: 'bankInterestCmb',
        forceSelection: true,
        isAccountCombo:true,
        hirarchical: true,
        emptyText: WtfGlobal.getLocaleText("acc.mp.selectBankInterestAccount"),
        disabled: this.readOnly,
        extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'],
        mode: 'remote',
        extraComparisionField: 'acccode', // type ahead search on acccode as well.
        listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 500 : 400
//        hidden:true,
//        hideLabel:true
    });
    this.bankInterestAccount.on('beforeselect',function(combo,record,index){
        return validateSelection(combo,record,index);
    },this);
    this.bankInterestAccount.on('select',function(){
        this.bankInterest.enable();
        this.bankInterest.allowBlank=false;
    },this);
    
     this.bankInterestAccount.on('blur',function(){
        if(this.bankInterestAccount.getRawValue()==''){
            this.bankInterestAccount.clearValue();
            this.bankInterest.setValue('');
            this.bankInterest.disable();
            this.bankInterest.allowBlank=true;
            this.bankInterest.validate();
        }else{
            this.bankInterest.allowBlank=false;
            this.bankInterest.validate();
        }
    },this);
    this.RPPaidToRec=new Wtf.data.Record.create([
    {
        name: 'id'
    },

    {
        name: 'name'
    },

    {
        name: 'isIbgActivItematedForPaidTo', 
        type:'boolean'
    }
    ]);
    this.RPReceivedFromStore=new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.RPPaidToRec),
        url:"ACCMaster/getMasterItems.do",
        baseParams:{
            mode:112,
            groupid:18
        }
    });
    this.paidTo = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.mp.receivedFrom"), //'Received From'
        hiddenName: "paidToCmb",
        store: this.RPReceivedFromStore,
        id: "paidto"+config.helpmodeid+this.id,
        valueField: 'id',
        displayField: 'name',
        allowBlank: true,
        disabled: this.readOnly,
        emptyText: WtfGlobal.getLocaleText("acc.rp.selreceivedfrom"), //'Select Received From...'
        minChars: 1,
        extraFields: '',
        listWidth: 500,
        extraComparisionField: 'name', // type ahead search on acccode as well.
        anchor: '85%',
        mode: 'local',
        triggerAction: 'all',
        typeAhead: true,
        forceSelection: true
    });
    this.RPReceivedFromStore.load();
    
    this.nonRefundable = new Wtf.form.Checkbox({
        name: 'NonRefundable',
        fieldLabel: WtfGlobal.getLocaleText("acc.cust.nonRefundable"),
        id: 'nonRefundable' + this.helpmodeid + this.id,
        checked: false,
        disabled: this.readOnly,
        hideLabel: (Wtf.account.companyAccountPref.countryid!='137')?true:(this.paymentType != this.paymentOption.AgainstCustomer),
        hidden:  (Wtf.account.companyAccountPref.countryid!='137')?true:(this.paymentType != this.paymentOption.AgainstCustomer),
        cls: 'custcheckbox',
        width: 10
    });
    
    
    this.paidTo.addNewFn = this.addPaidTo.createDelegate(this);
    this.Memo = new Wtf.form.TextArea({
        fieldLabel: Wtf.account.companyAccountPref.descriptionType, // 'Memo',
        name: 'memo',
        hiddenName: "memo",
        height: 40,
        anchor: '85%',
        id:"memo"+this.helpmodeid+this.id,
        readOnly: this.readOnly,
        maxLength:2048, 
        qtip:(this.record==undefined)?' ':this.record.data.memo,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }
    });
    this.Amount = new Wtf.form.FinanceNumberField({
        hiddenName: "amount",
        name: "amount",
        allowBlank: false,
        fieldLabel: WtfGlobal.getLocaleText("acc.mp.11"), //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
        id: "amount" + config.helpmodeid + this.id,
        maxLength: 15,
        decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
        value: 0,
        disabled:this.readOnly,
        emptyText: WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
        anchor: '85%'
    });
    /**
     * In Payment againstGL amount should be disabled
     * ERP-37263
     */
    if (this.paymentType == this.paymentOption.AgainstGL) {
        this.Amount.disable();
    }
    this.paymentCurrencyToPaymentMethodCurrencyExchangeRate = new Wtf.form.NumberField({
        hiddenName: "paymentCurrencyToPaymentMethodCurrencyExchangeRate",
        name:"paymentCurrencyToPaymentMethodCurrencyExchangeRate",
        fieldLabel: WtfGlobal.getLocaleText("acc.mp.paymentCurrencyToPaymentMethodCurrencyExchangeRate"), 
        id:"paymentCurrencyToPaymentMethodCurrencyEx"+this.helpmodeid+this.id,
        maxLength: 20,
        decimalPrecision: 16,
        value: 1,
        disabled:true,
        emptyText: WtfGlobal.getLocaleText("acc.mp.spotRateEmptyText"), //"Enter Spot rate",
        anchor: '85%',
        validator: function(val) {
            if (val>0) {
                return true;
            } else {
                return "Value should be greater than 0";
            }
        }
    });
    
    this.centerPanel = new Wtf.Panel({
        region: "center",
        layout: "fit",
        autoHeight: true
    });
    this.mainInvoiceId = "";
    this.totalValidValue = 0;
    this.tagsFieldset = new Wtf.account.CreateCustomFields({
        border: false,
        compId: this.id + 'Northform',
        compId1: this.id + "wrapperPanelNorth",
        autoHeight: true,
        moduleid: this.moduleid,
        isEdit: this.isEdit,
        record: this.record,
        isViewMode:this.readOnly,
        parentcompId:this.id,
        isBulkPayment:this.isBulkPayment
    });   
    this.NorthForm = new Wtf.form.FormPanel({
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
        id: this.id + 'Northform',
        cls: "visibleDisabled",
        labelWidth: 140,
        //disabled: this.readOnly,
        items: [
            {
              xtype: 'panel',
              id:this.id+'requiredfieldmessagepanel',
              hidden:true,
              cls: 'invalidfieldinfomessage'
            },
            {
                layout: 'column',
                defaults: {
                    border: false
                },
                items: [{
                        layout: 'form',
                        columnWidth: 0.33,
                        items: [this.sequenceFormatCombobox, this.No, this.creationDate, this.paidTo,this.nonRefundable]
                    }, {
                        layout: 'form',
                        columnWidth: 0.33,
                        id: this.id + 'MiddleColumnform',
                        items: [this.ShowOnlyOneTime, this.Currency, this.paymentCurrencyToPaymentMethodCurrencyExchangeRate,this.Amount, this.bankChargesAccount, this.bankCharges]
                    }, {
                        layout: 'form',
                        columnWidth: 0.33,
                        items: [this.pmtMethod, this.pmtMethodAcc, this.Memo, this.bankInterestAccount, this.bankInterest]
                    }]
            }, this.tagsFieldset]
    });
    this.createGrid();
    this.tplSummary = new Wtf.XTemplate(
            '<div class="currency-view">',
            '<table width="100%">', //       
            '<tr><td><b>' + WtfGlobal.getLocaleText("acc.mp.receiptAmount") + ': </b></td><td text-align=right>{due}</td></tr>',
            '</table>',
            '<div id="hideIdForTax">',
            '<hr class="templineview">',
            '<table width="100%">',
            '<tr><td><b>' + WtfGlobal.getLocaleText("acc.invoice.amt") + ' </b></td><td text-align=right>{amountwithouttax}</td></tr>',
            '<tr><td><b>' + WtfGlobal.getLocaleText("acc.rem.196") + ': </b></td><td text-align=right>{taxamount}</td></tr>',
            '</table>',
            '<hr class="templineview">',
            '</div>',
            '<table width="100%">',
            '<tr><td style="width:40%;"><b>' + WtfGlobal.getLocaleText("acc.mp.12") + '</b></td><td text-align=right>{received}</td></tr>',
            '<tr><td style="width:40%;"><b>' + WtfGlobal.getLocaleText("acc.rp.amountrecInBase") + '</b></td><td text-align=right>{receivedinbase}</td></tr>',
            '</table>',
            '<hr class="templineview">',
            '</div>'
            );
          this.LineLevelTermTplSummary = new Wtf.XTemplate(
        '<div> &nbsp;</div>',
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',   
        '<div><hr class="templineview"></div>',
        '<div class ="currency-view">',
        '<table width="95%">',
        '<tpl for="lineLevelArray">',
        '<tr><td><b>{name} Amount </b></td><td text-align=right>{taxAmount}</td></tr>',
        '</tpl>',
        '</table>',
        '<div><hr class="templineview"></div>',
        '<table width="95%">',
        '<tr><td><b>'+"GST on Advance"+' </b></td><td text-align=right>{TotalTaxAmt}</td></tr>',
        '</table>',
        '<div><hr class="templineview"></div>',
        '</div>'
        );
    this.southCalTemp = new Wtf.Panel({
        border: false,
        boaseCls: 'tempbackgroundview',
        html: this.tplSummary.apply({
            receivable: WtfGlobal.currencyRenderer(0),
            receivableInBase: WtfGlobal.currencyRenderer(0),
            received: WtfGlobal.currencyRenderer(0),
            due: WtfGlobal.currencyRenderer(0),
            taxamount: WtfGlobal.currencyRenderer(0),
            amountwithouttax: WtfGlobal.currencyRenderer(0),
            receivedinbase: WtfGlobal.currencyRenderer(0)
        })
    });
    var lineLevelArray = [];
    if (this.isIndiaGST) {
        for (var i = 0; i < Wtf.LineTermsMasterStore.getRange().length; i++) {
            var temp = Wtf.LineTermsMasterStore.getRange()[i].data;
            temp['taxAmount'] = WtfGlobal.currencyRenderer(0);
            lineLevelArray.push(temp);
        }
    }
    this.LineLevelTermTpl = new Wtf.Panel({
        border: false,
        width: '35%',
        hidden: !Wtf.account.companyAccountPref.isLineLevelTermFlag,
        baseCls: 'tempbackgroundview',
        html: this.LineLevelTermTplSummary.apply({
            lineLevelArray: lineLevelArray,
            TotalTaxAmt: WtfGlobal.currencyRenderer(0)
        })
    });
    this.bankBalanceTplSummary = new Wtf.XTemplate(
            '<div class="currency-view">',
            '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',
            '<div><hr class="templineview"></div>',
            '<div>',
            '<table width="100%">' +
            '<tr>' +
            '<td style="width:40%;"><b>' + WtfGlobal.getLocaleText("acc.field.AccountBalance") + '</b></td><td style="width:50%;"><span style="width: auto;float: left;display:block;">' + Wtf.util.Format.ellipsis('{endingBalance}', 20) + '</span></td>' +
            '</tr>' +
            '</table>' +
            '</div>',
            '<div><hr class="templineview"></div>',
            '</div>',
            '</div>'
            );
    this.bankBalanceTpl = new Wtf.Panel({
        //id:'productDetailsTpl',
        border: false,
        baseCls: 'tempbackgroundview paymentformbankbaldiv',
        width: '35%',
        html: this.bankBalanceTplSummary.apply({
            endingBalance: "&nbsp;&nbsp;&nbsp;&nbsp;"
        })
    });
    this.balanceAmountTplSummary = new Wtf.XTemplate(
            '<div class="currency-view">',
            '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',
            '<div><hr class="templineview"></div>',
            '<div>',
            '<table width="100%">' +
            '<tr>' +
            '<td style="width:40%;"><b>' + WtfGlobal.getLocaleText("acc.field.BalanceAmount") + '</b></td><td style="width:50%;"><span style="width: auto;float: left;display:block;">' + Wtf.util.Format.ellipsis('{balanceAmount}', 20) + '</span></td>' +
            '</tr>' +
            '</table>' +
            '</div>',
            '<div><hr class="templineview"></div>',
            '</div>',
            '</div>'
            );
    this.balanceAmountTpl = new Wtf.Panel({
        //id:'productDetailsTpl',
        border: false,
        baseCls: 'tempbackgroundview paymentformbalanceamount',
        width: '35%',
        html: this.balanceAmountTplSummary.apply({
            balanceAmount: WtfGlobal.addCurrencySymbolOnly(0, this.symbol)
        })
    });
    this.noteTplSummary = new Wtf.XTemplate(
            '<div class="currency-view" style="width: 59% !important;">',
            '<div style="padding-top: 5px; border: 1px solid rgb(153, 187, 232);">',            
            '<div>',
            '<table width="100%">' +
            '<tr>' +
            '<td style="width:15%;"><b>' + 'Note:' + '</b></td><td style="width:100%;"><span style="width: auto;float: left;display:block;">' + Wtf.util.Format.ellipsis('{noteText}', 20) + '</span></td>' +
            '</tr>' +
            '</table>' +
            '</div>',            
            '</div>',
            '</div>'
            );
    this.noteTpl = new Wtf.Panel({
        id:'receiptNoteTpl'+this.id,
        border: false,
        cls: 'paymentformbankbaldiv ',
        style : "margin-top: 10px; width: 59%;",
        html: this.noteTplSummary.apply({
            noteText: "Total advance amount is inclusive of GST."
        })
    });
        var blockSpotRateLink_first = "";
        var blockSpotRateLink_second = "";
        if(!Wtf.account.companyAccountPref.activateToBlockSpotRate){ // If activateToBlockSpotRate is set then block the Spot Rate Links
            blockSpotRateLink_first = "<br/>"+WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div><div style='padding-left:30px;padding-top:5px;padding-bottom:10px;'><a class='tbar-link-text' href='#' onClick='javascript: editReceiptExchangeRates(\""+this.id+"\",\"{foreigncurrency}\",\"{basecurrency}\",\"{revexchangerate}\",\"foreigntobase\")'wtf:qtip=''>{foreigncurrency} to {basecurrency}</a></div>";
            blockSpotRateLink_second = "<br/>"+WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div> <div style='padding-left:30px;padding-top:5px;'><a class='tbar-link-text' href='#' onClick='javascript: editReceiptExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\",\"basetoforeign\")'wtf:qtip=''>{basecurrency} to {foreigncurrency}</a></div>";
        }
       this.southCenterTplSummary=new Wtf.XTemplate(
    "<div> &nbsp;</div>",  //Currency:
             '<tpl if="editable==true">',
         "<b>"+WtfGlobal.getLocaleText("acc.invoice.msg8")+"</b>",  //Applied Exchange Rate for the current transaction:
           "<div style='line-height:18px;padding-left:30px;'>1 {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+" = {revexchangerate} {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+". "+
         blockSpotRateLink_first,
         "</div><div style='line-height:18px;padding-left:30px;'>1 {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+" = {exchangerate} {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+". ",    
         blockSpotRateLink_second,
             '</tpl>'
        );
    this.southCenterTpl = new Wtf.Panel({
        border: false,
        baseCls: 'paymentformbankbaldiv',
        html: this.southCenterTplSummary.apply({
            basecurrency: WtfGlobal.getCurrencyName(),
            exchangerate: 'x',
            foreigncurrency: "Foreign Currency",
            editable: false
        })
    });
    var tranType = null;
    if (this.isCustBill) {
        tranType = config.isCustomer ? (config.isOrder ? Wtf.autoNum.BillingSalesOrder : Wtf.autoNum.BillingInvoice) : (config.isOrder ? Wtf.autoNum.BillingPurchaseOrder : Wtf.autoNum.BillingGoodsReceipt);
    }else if (config.moduleId == 14 || config.moduleId == 16) {
        if (config.moduleId == 14) {
            tranType = Wtf.autoNum.Payment;
        } else {
            tranType = Wtf.autoNum.Receipt;
        }
    }
    if (this.isRequisition) {
        tranType = Wtf.autoNum.Requisition;
    }else if (this.isRFQ) {
        tranType = Wtf.autoNum.RFQ;
    }
    var singlePDFtext = null;
    if (this.isQuotation) {
        singlePDFtext = WtfGlobal.getLocaleText("acc.accPref.autoQN");
    } else {
        singlePDFtext = config.isCustomer ? (config.isOrder ? WtfGlobal.getLocaleText("acc.accPref.autoSO") : WtfGlobal.getLocaleText("acc.accPref.autoInvoice")) : (config.isOrder ? WtfGlobal.getLocaleText("acc.accPref.autoPO") : WtfGlobal.getLocaleText("acc.accPref.autoVI"));
    }
    this.southPanel = new Wtf.Panel({
        region: 'south',
        border: false,
        style: 'padding:0px 10px 10px 10px',
        layout: 'border',
        disabledClass: "newtripcmbss",
        disabled: this.readOnly,
        autoScroll: true,
        height: 230,
        items: [{
                region: 'center',
                border: false,
                autoHeight: true,
                items: (Wtf.account.companyAccountPref.countryid==Wtf.Country.MALAYSIA && (this.paymentType == this.paymentOption.AgainstCustomer))?[this.bankBalanceTpl, this.balanceAmountTpl,this.noteTpl,this.southCenterTpl]:[this.bankBalanceTpl, this.LineLevelTermTpl, this.balanceAmountTpl,this.southCenterTpl]
            }, {
                region: 'east',
                id: this.id + 'southEastPanel',
                cls: 'bckgroundcolor',
                bodyStyle: 'padding:10px',
                width: 350,
                items: [this.southCalTemp]
            }]
    });
    this.appendID = true;
    this.toggleBtnPanel = new Wtf.Panel({
        style: 'padding: 10px 10px 0;',
        border : false,
        autoScroll: true,
        items : [{
                xtype: 'button',
                enableToggle: true,
                hidden: this.readOnly,
                cls : 'paymentinvoicebtns0',
                text: WtfGlobal.getLocaleText("acc.inv.InvoiceDateFIFO"),
                toggleGroup: 'ratings',
                handler: this.handleInvoiceDateFIFO.createDelegate(this)
            }, {
                xtype: 'button',
                enableToggle: true,
                hidden: this.readOnly,
                cls : 'paymentinvoicebtns',
                text: WtfGlobal.getLocaleText("acc.inv.InvoiceDateLIFO"),
                toggleGroup: 'ratings',
                handler: this.handleInvoiceDateLIFO.createDelegate(this)
            }, {
                xtype: 'button',
                hidden: this.readOnly,
                cls : 'paymentinvoicebtns',
                text: WtfGlobal.getLocaleText("acc.mp.invoiceMonthWiseButton"),
                handler: this.getMonthWiseInvoices.createDelegate(this)
            },{
                xtype: 'button',
                enableToggle: true,
                hidden: this.readOnly,
                cls : 'paymentinvoicebtns',
                text: WtfGlobal.getLocaleText("acc.inv.InvoiceDueDateFIFO"),
                hidden:true,
                toggleGroup: 'ratings',
                handler: this.handleInvoiceDueDateFIFO.createDelegate(this)
            }, {
                xtype: 'button',
                enableToggle: true,
                hidden: this.readOnly,
                cls : 'paymentinvoicebtns1',
                text: WtfGlobal.getLocaleText("acc.inv.InvoiceDueDateLIFO"),
                hidden:true,
                toggleGroup: 'ratings',
                handler: this.handleInvoiceDueDateLIFO.createDelegate(this)
            },{
                xtype: 'button',
                id: "deleteButton"+ this.id,
                disabled: true,
                cls: 'paymentinvoicebtns',
                text: WtfGlobal.getLocaleText("acc.common.deleteselected"),
                toggleGroup: 'ratings',
                hidden: this.readOnly,
                handler: this.deleteSelectedRecord.createDelegate(this)
            }]
    })
    var panelItemsArra = [];
    panelItemsArra.push(this.wrapperNorth = new Wtf.Panel({
        region: "north",
        //height:245,
        autoHeight: true,
        style: 'padding:0px 10px 0px 0px',
        id: this.id + "wrapperPanelNorth",
        border: false,
        defaults: {
            border: false
        },
        items: [{
                xtype:'hidden',
                name:'isCustomer',
                value:this.isCustomer,
                id:'isCustomer'+ this.helpmodeid + this.id
        },this.NorthForm, this.centerPanel]
    }))
    if(this.paymentType !== this.paymentOption.AgainstGL && this.paymentType !== this.paymentOption.AgainstVendor) {
        panelItemsArra.push(this.toggleBtnPanel);
    }
    else{
        for(var i=0;i<this.toggleBtnPanel.items.length;i++){
            if(this.toggleBtnPanel.items.items[i] != this.toggleBtnPanel.findById("deleteButton"+ this.id))
                this.toggleBtnPanel.items.items[i].hide();
        }
        panelItemsArra.push(this.toggleBtnPanel);
     
    }
    panelItemsArra.push(this.grid);
    panelItemsArra.push(this.southPanel);
    
    this.newPanel = new Wtf.Panel({
        autoScroll: true,
        region: 'center',
        items: panelItemsArra
    });
    this.newPanel.on("resize", function() {
        this.newPanel.doLayout();
    }, this);
    
    Wtf.apply(this, {
        items: [this.newPanel],
        bbar: [this.saveBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save'
                tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
                scope: this,
                id: "save" + config.helpmodeid + this.id,
                hidden: this.readOnly,
                iconCls: getButtonIconCls(Wtf.etype.save),
                handler: function() {
                    if(this.isEdit && !this.isCopyReceipt){
                        var documentNo=this.No.getValue();
                        if(documentNo==null || documentNo==undefined || documentNo==""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.NumberBlankAlert")], 2);
                            return ;
                        }
                    }
                    this.saveAndCreateNewFlag = false;                    // This flag is used to differentiate between Save button and Save and Create New button
                    this.beforeSave();
                }
            }), this.savencreateBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
                tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
                scope: this,
                hidden:(this.isEdit) || this.readOnly,
                id: "savencreate" + config.helpmodeid + this.id,
                iconCls: getButtonIconCls(Wtf.etype.save),
                handler: function() {
                    if(this.isEdit && !this.isCopyReceipt){
                        var documentNo=this.No.getValue();
                        if(documentNo==null || documentNo==undefined || documentNo==""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.NumberBlankAlert")], 2);
                            return ;
                        }
                    }
                    this.saveAndCreateNewFlag = true;
                    this.beforeSave();
                }
            }), (!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType))?this.emailBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.email"),
                tooltip: WtfGlobal.getLocaleText("acc.common.emailTT"), //"Email",
                scope: this,
                hidden: this.readOnly,  //ERP-38935
                id: "emailbut" + this.id,
                iconCls: "accountingbase financialreport",
                disabled: true,
                handler: function() {
                    this.callEmailWindowFunction(this.response, this.request)
                }
            }):'',
            (!WtfGlobal.EnableDisable(this.uPermType, this.printPermType) || !WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))?this.exportPdfBttn = new Wtf.exportButton({
                obj: this,
                id: "exportpdf" + this.id,
                isEntrylevel: true,
                iconCls: 'pwnd printButtonIcon',
                text:WtfGlobal.getLocaleText("acc.rem.236"),
                tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
                disabled: true,
                exportRecord: this.exportRecord,
                hidden: this.readOnly || this.isRequisition || this.isRFQ || this.isSalesCommissionStmt,
                menuItem: {
                    rowPdf: (this.isSalesCommissionStmt) ? false : true,
                    rowPdfPrint: (this.isSalesCommissionStmt) ? false : true,
                    rowPdfTitle: WtfGlobal.getLocaleText("acc.rem.39") + " " + singlePDFtext
                },
                get: tranType,
                moduleid: config.moduleId
            }):'',
            
//           (!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))?this.singleRowPrint=new Wtf.exportButton({
//             obj:this,
//             id:"printSingleRecord"+ this.id,
//             iconCls: 'pwnd printButtonIcon',
//             text:WtfGlobal.getLocaleText("acc.rem.236"),
//             tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
//             disabled :true,
//             hidden: this.readOnly || this.isRequisition || this.isRFQ || this.isSalesCommissionStmt,
//             filename:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.tabTitle"):WtfGlobal.getLocaleText("acc.pmList.tabTitle"), 
//             isEntrylevel:true,
//             exportRecord: this.exportRecord,
//             menuItem:{
//                 rowPrint:true
//             },
//             get: tranType,
//             moduleid:this.moduleid
//            }):'',
            this.savePrintBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.savePrintBtn"), //'Save',
                scope: this,
                id: "printsave" + config.helpmodeid + this.id,
                iconCls: getButtonIconCls(Wtf.etype.save),
                hidden: true, // as this is not available for Receive Payment
                handler: this.savePringCheque.createDelegate(this)
            }), "->", (!this.readOnly) ? getHelpButton(this, config.helpmodeid) : ""]
    });
    this.Amount.on('focus', function(field) {
        if (field.getValue() == 0) {
            field.setValue("");
        }
    }, this);
    
    this.Amount.on('blur',this.onAmountFieldBlur,this);
    this.grid.on('datachanged', this.onGridDataChanged, this);
    this.creationDate.on('change',this.onDateChange,this);  
    //this.Currency.on('select', this.updateTemplateCurr, this);
    this.Currency.on('change',this.onCurrencyChange,this);
    new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
        this.grid.on('columnmove', this.saveGridStateHandler, this);
        this.grid.on('columnresize', this.saveGridStateHandler, this);
    }, this);
    if (this.readOnly) {
        this.grid.enableColumnMove = false;
        this.grid.enableColumnResize = false;
    }
    Wtf.account.ReceiptEntry.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true
    });
     this.grid.on("onselection", function(){
            
            if (this.grid.selectionModel.getCount() >= 1 ) {
                if (Wtf.getCmp("deleteButton"  + this.id))
                    Wtf.getCmp("deleteButton"  + this.id).enable();
            } else {
                if (Wtf.getCmp("deleteButton"  + this.id))
                    Wtf.getCmp("deleteButton"  + this.id).disable();
            }
    },this);
    this.on("resize", function () {
        this.doLayout();
        if(this.newPanel != undefined){
            this.newPanel.doLayout();
        }
        if(this.southPanel != undefined){
            this.southPanel.doLayout();
        }
        if(this.grid != undefined && this.grid.getView() != undefined){
            this.grid.doLayout();
            this.grid.getView().refresh();
        }
    }); 
    this.on('populateGlobalDimensionValueInBulkPayment',this.populateGlobalDimensionValueInBulkPayment,this);
}

Wtf.extend(Wtf.account.ReceiptEntry, Wtf.account.ClosablePanel, {
    onRender: function(config) {
        WtfGlobal.getGridConfig(this.grid, this.moduleid + "_" + this.paymentType, true, false);
        Wtf.account.ReceiptEntry.superclass.onRender.call(this, config);
        if(!this.readOnly){
            this.isClosable= false;         // This flag is used to give an alert if user is closing the form without saving the data. In view case, alert will not be given.
        }
        if(this.sequenceFormatStore.getCount() > 0){
            this.setNextNumber();
        } else {
            this.sequenceFormatStore.on('load', this.setNextNumber, this);
        }
        chkReceiptSeqFormatStoreLoad();
        this.createNameField();
        if (this.isEdit) {
            if ((this.paymentType !== this.paymentOption.AgainstGL) && this.personstore && this.record && this.record.data && this.record.data.personid) {
                this.loadPersonStore(this.record.data.personid);
            }
            this.getPaymentDataFromServer();
            this.fetchPaymentAccBalance();
        }
        if(this.loanFlag && this.paymentDetailsRecord.isSelected){
            this.setCustomerInfo();
        }
        
         if(this.isBulkPayment){
            var cnt=0;
            var maxDate;
            var date;
            var currentDate=new Date();
            var checkDate=false;
            var recordArr= eval(this.invObj.getSelectedRecords());
            this.loadPersonStore(recordArr[0].personid);
            this.Name.setValForRemoteStore(recordArr[0].personid,recordArr[0].personname);
             /*
             * If selecting multiple invoice at time then max date of invoice is set for payment
             * Note:1) If  selected invoices have future invoice date so max future date is set in paymnet
             *      2) If Invoice date has less than curren date then current date is set in payment
             */
            while(cnt<recordArr.length){
                date=new Date(recordArr[cnt].date);
                if(cnt==0){
                    maxDate=date;
                }
                if(date>currentDate){
                    checkDate=true;
                    if(date>=maxDate){
                        maxDate=date;
                    }
                }
                cnt++;
            }
            if(checkDate){
             this.creationDate.setValue(maxDate);   
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.bulkRecpaymentfuture")], 2);
            }
            
            this.updateAmount(getRoundedAmountValue(this.grid.getMultiDebitAmount()));
        }
         
        this.hideFormFields();
    },
    /**
     * Populate dimensions custom fields data in buld payment
     */
    populateGlobalDimensionValueInBulkPayment: function() {
        if (this.isBulkPayment) {
            WtfGlobal.populateGlobalDimensionValueInBulkPayment(this);
        }
    },
    hideFormFields:function(){
        this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.receivePayment);
    },
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id)){
                    if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false))){
                        continue;
                    }
                    if(fieldArray.isHidden){
                        Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).hidden = fieldArray.isHidden;
                    }
                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).disabled = fieldArray.isReadOnly;
                    }
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        var fieldLabel="";
                        if(fieldArray.fieldLabelText!="" && fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined){
                            fieldLabel= fieldArray.fieldLabelText+" *";
                        }else{
                            fieldLabel=(Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).fieldLabel) + " *";
                        }
                        Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).fieldLabel = fieldLabel;
                    }else{
                        if( fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined && fieldArray.fieldLabelText!=""){
                            if(fieldArray.isManadatoryField && fieldArray.isFormField )
                                Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).fieldLabel=fieldArray.fieldLabelText +"*";
                            else
                                Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).fieldLabel=fieldArray.fieldLabelText;
                        }
                    }
                }
            }
        }
    },

    clearStore: function() {
        this.grid.store.removeAll();
    },
    
    handleInvoiceDateFIFO: function(btn) {
        if(btn.pressed) {
            var isValid =this.validateFormForLifoFifoMode(btn);
            if(isValid){
                this.clearStore();
                this.LifoFifoMode=true;
                var parameters=this.getParametersForLifoFifo();
                parameters['direction']='ASC';
                Wtf.Ajax.requestEx({
                    url: "ACCInvoiceCMN/getInvoicesForLifoFifo.do",
                    params: parameters
                },this,this.genSuccessResponseLifoFifo,this.genFailureResponseLifoFifo);
            }
        }
    },
    handleInvoiceDateLIFO: function(btn) {
        if(btn.pressed) {
            var isValid =this.validateFormForLifoFifoMode(btn);
            if(isValid){
                this.clearStore();
                this.LifoFifoMode=true;
                var parameters=this.getParametersForLifoFifo();
                parameters['direction']='DESC';
                Wtf.Ajax.requestEx({
                    url: "ACCInvoiceCMN/getInvoicesForLifoFifo.do",
                    params:parameters
                },this,this.genSuccessResponseLifoFifo,this.genFailureResponseLifoFifo);
            }
        } 
    },
    handleInvoiceDueDateFIFO: function(btn) {
        if(btn.pressed) {
            var isValid =this.validateFormForLifoFifoMode(btn);
            if(isValid){
                this.clearStore();
                this.LifoFifoMode=true;
                var parameters=this.getParametersForLifoFifo();
                parameters['direction']='ASC';
                Wtf.Ajax.requestEx({
                    url: "ACCInvoiceCMN/getInvoicesForLifoFifo.do",
                    params: parameters
                },this,this.genSuccessResponseLifoFifo,this.genFailureResponseLifoFifo);
            }
        } 
    },
    handleInvoiceDueDateLIFO: function(btn) {
        if(btn.pressed) {
            var isValid =this.validateFormForLifoFifoMode(btn);
            if(isValid){
                this.clearStore();
                this.LifoFifoMode=true;
                var parameters=this.getParametersForLifoFifo();
                parameters['direction']='DESC';
                Wtf.Ajax.requestEx({
                    url: "ACCInvoiceCMN/getInvoicesForLifoFifo.do",
                    params: parameters
                },this,this.genSuccessResponseLifoFifo,this.genFailureResponseLifoFifo);
            }
        } 
    },
    genSuccessResponseLifoFifo : function(response,request){
        if(response.success){
            this.grid.addSelectedRecords(response.data.Invoices,this.grid.INVType, false,true,undefined,undefined,true);
            this.grid.addSelectedRecords(response.data.AdvancePayment,this.grid.ADVType, false,true);
            if(this.grid.store.getCount()>0) {
                this.grid.addBlankRow();
                this.updateSouthTemp();
                this.updateBalanceAmount();
            } else {
                this.resetLifoFifoButtons();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),WtfGlobal.getLocaleText("acc.invoicefifolifo.donothavedata")],0);
            }
        } else{
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
             return;
        }
    },
    genFailureResponseLifoFifo: function(response,request){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    getPaymentDataFromServer: function() {
        Wtf.Ajax.requestEx({
            url: this.isReceipt?'ACCReceiptNew/getSinglePaymentDataToLoad.do':'ACCVendorPaymentNew/getSinglePaymentDataToLoad.do',
            params: {
                billid: this.record.data.billid,
                isCopyTransaction:this.isCopyReceipt,    // Used to load the data conditionaly
                ispendingAproval:this.ispendingAproval,      //used to open pending receipt from pending approval tab in edit case 
                isView:this.readOnly                    //used to load discount only when view the receipt from pending receipt report as for edit case we do not load the discount
            }
        }, this,
        function(result, req) {
            this.loadForm(result);
        });
    },
    loadForm: function(rec) {
        var recData = rec.data;
        var recDetails = recData.Details;
        this.lineRec = recDetails;
        this.presonCode=recData.personcode;  // IN edit case, vendor/customer store is not loaded. So, for getting vendor/customer code, we have set this variable in edit case.
        /*
            In copy case no need to send billid otherwise it will be consider as edit case
        */
        if(!this.isCopyReceipt){
           this.setHiddenFieldValues(recData);
        }
        if(this.nonRefundable!=undefined){
            this.nonRefundable.setValue(recData.nonRefundable);
        }
        this.setComboValues(recData);
        if(this.isCopyReceipt){
            this.sequenceFormatCombobox.fireEvent('select',this.sequenceFormatCombobox);
        }
        /**
         * Display Account code with account name if Present.
         */
        for(var i = 0; i<recDetails.data.length; i++){
            if (recDetails.data[i]["acccode"] != undefined && recDetails.data[i]["acccode"] != "") {
                recDetails.data[i]['documentno'] = '[' + recDetails.data[i]["acccode"] + '] ' + recDetails.data[i]['documentno'];
            }
        }
        this.NorthForm.form.loadRecord(rec);
        if (this.isIndiaGST) {
            if (recData.CustomerVendorTypeId != undefined) {
                this.CustomerVendorTypeId = recData.CustomerVendorTypeId;
            }
            if (recData.GSTINRegistrationTypeId != undefined) {
                this.GSTINRegistrationTypeId = recData.GSTINRegistrationTypeId;
            }
            if (recData.gstin != undefined) {
                this.gstin = recData.gstin;
            }
            if (recData.gstdochistoryid != undefined) {
                this.gstdochistoryid = recData.gstdochistoryid;
            }
        }
//        this.pmtMethod.setValForRemoteStore(recData.pmtmethod, recData.paymentmethodname,recData.hasAccess);
        /*
         *  Currency store load to get exchange rates on billdate in the south panel.  
         */
//        this.changeCurrencyStore();
        if(this.isCopyReceipt && this.sequenceFormatCombobox.getValue()=='NA'){
            this.No.setValue("");
        }
        this.loadCheckDetails(rec);
        if(this.SouthForm) {
            this.SouthForm.form.loadRecord(rec.data.paydetail);
            this.SouthForm.bank.setValForRemoteStore(rec.data.paydetail.data.paymentthroughid, rec.data.paydetail.data.paymentthrough);
            this.SouthForm.setOtherFieldInfoOnRecordLoad();
        }
        this.loadLineDetails(recDetails);
        if ((this.readOnly || this.isEdit) && this.grid.taxStore) {
            this.grid.taxStore.load({
                scope: this,
                callback: function () {
                    if (this.lineRec) {
                        this.loadLineDetails(this.lineRec);
                    }
                    if (this.isCopyReceipt) {
                        this.checkForValidLineLevelData();
                        this.updateAmount(getRoundedAmountValue(this.grid.getMultiDebitAmount() + this.grid.getMultiDebitTaxAmount()));
                    }
                }
            });
        }
        if(this.isCopyReceipt){
            this.checkForValidLineLevelData();
            this.updateAmount(getRoundedAmountValue(this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount()));
        }
        this.updateSouthTemp();
        this.updateBalanceAmount();
        if (this.isIndiaGST) {
            updateTermDetails(this.grid);
        }
        if((!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.viewBankTypeAccountBalance))
            ||(!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.viewCashTypeAccountBalance))){
            this.fetchPaymentAccBalance();
            this.hideShowAccountBalance();
        }
           this.enableDisablePaymentCurrencyToPaymentMethodCurrencyRate(); 
        if (this.isAllowedSpecificFields) {
            this.disableField();
        } 
        /**
         * Populate Customer/ Vendor GST details in Edit/   Copy Case
         */
        this.populateGSTDataOnEditCopy();
    },
    
    setComboValues: function(recData) {
        if(this.Name) {
            this.Name.setValForRemoteStore(recData.personid, recData.personname);
        }
        if(recData.sequenceformatid!='') {
            this.sequenceFormatCombobox.setValForRemoteStore(recData.sequenceformatid,recData.sequenceformatvalue);
            if(!this.isCopyReceipt){                 // Sequence format combo will be disable in edit case but will be enabled in Copy case
                this.sequenceFormatCombobox.disable();
                this.No.disable();
            }
            
        } else {
            this.sequenceFormatCombobox.setValForRemoteStore("NA","NA");
            if(this.isCopyReceipt){                   
                this.sequenceFormatCombobox.enable();     // In Copy case, user can change sequence format and payment number
                this.No.enable();
            } else{
                this.sequenceFormatCombobox.disable(); // In Edit case, user can not change sequence format but able to change payment number in 'NA' sequence format
                if (this.readOnly) {
                    this.No.disable();
                }
            }           
        }
        if (recData.bankInterestCmb !== '') {
            this.bankInterestAccount.setValForRemoteStore(recData.bankInterestCmb, recData.bankInterestCmbValue);
            if(!this.readOnly)
                this.bankInterest.enable();
        }
        if (recData.bankChargesCmb !== '') {
            this.bankChargesAccount.setValForRemoteStore(recData.bankChargesCmb, recData.bankChargesCmbValue);
            if(!this.readOnly)
                this.bankCharges.enable();
        }
        if (recData.paidToCmb !== '') {
            this.paidTo.setValForRemoteStore(recData.paidToCmb, recData.paidToCmbValue);
        }
    },
    
    setHiddenFieldValues: function(recData) {
        Wtf.getCmp(this.id+'MiddleColumnform').add(new Wtf.form.Hidden({
            name : 'billid',
            value:recData.billid
        }));
    },
    disableField: function() {
        if (this.NorthForm.getForm().items != undefined && this.NorthForm.getForm().items != null) {
            for (var i = 0; i < this.NorthForm.getForm().items.length; i++) {
                this.NorthForm.getForm().items.item(i).disable();
            }
        }
        if(this.SouthForm){
            this.SouthForm.disable();
        }
        this.toggleBtnPanel.disable();
        this.Memo.enable();
        this.paidTo.enable();

    },
    
    loadLineDetails: function(recDetails) {
        this.grid.store.loadData(recDetails);
        if(this.isEdit&&this.pendingApproval&&!this.readOnly){      //In case of edit making line level item enter amount to 0
            this.grid.getStore().each(function(rec){
                rec.set('enteramount',0)
            },this);
        }
        this.grid.addBlankRow();
        if (this.isEdit) {
            this.grid.getView().refresh();
        }
    },
    updateTemplateCurr: function(a, rec, index) {
        var currencyRecord = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
        this.symbol= currencyRecord.data['symbol'];
        this.fetchPaymentAccBalance();
        this.grid.setCurrencyid(currencyRecord.data.currencyid, currencyRecord.data.symbol);
    },
    addAccount: function(store) {
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update", function() {
            store.reload()
        }, this);

    },
    addPerson: function(isEdit, rec, winid, isCustomer) {
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid = this.isCustomer ? 'contactDetailCustomerTab' : 'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function() {
         this.isCustomer ? Wtf.customerAccRemoteStore.reload() : Wtf.vendorAccRemoteStore.reload();
            this.personAccStore.reload();
        }, this);
    },
    addPaymentMethod: function() {
        PaymentMethod('PaymentMethodWin');
        Wtf.getCmp('PaymentMethodWin').on('update', function() {
            this.pmtStore.reload();
        }, this);
    },
    setNextNumber: function(config) {
        if (this.sequenceFormatStore.getCount() > 0) {              //Edit case
            if (this.isEdit && !this.isCopyReceipt) {
                var index = this.sequenceFormatStore.find('id', this.record.data.sequenceformatid);
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid);
                    this.sequenceFormatCombobox.disable();
                    this.No.disable();
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                    this.No.enable();
                }
            } else {                                                    // Copy case
                if (this.isCopyReceipt != undefined && this.isCopyReceipt) {
                    var indexCopyCase = this.sequenceFormatStore.find('id', this.record.data.sequenceformatid);
                    if (indexCopyCase == -1) {
                        this.sequenceFormatCombobox.setValue("NA");
                        this.sequenceFormatCombobox.disable();
                        this.No.enable();
                        this.No.setValue("");
                    } else {
                        var seqRecCopyCase = this.sequenceFormatStore.getAt(indexCopyCase);
                        this.sequenceFormatCombobox.setValue(seqRecCopyCase.data.id);
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    }
                } else {
                    this.setSequenceFormatForCreateNewCase();      // Create new case              
                        }
                    }
                }
    },
    createGrid: function() {
        this.grid = new Wtf.account.ReceiptDetail({
            region: 'center',
            border: true,
            invObj:this.invObj,
            isBulkPayment:this.isBulkPayment,
            currencyid: this.currencyid,
            paymentType: this.paymentType,
            paymentOption: this.paymentOption,
            isEdit: this.isEdit,
            isCopyReceipt:this.isCopyReceipt,
            symbol:WtfGlobal.getCurrencySymbol(),
            parentObj: this,
//            disabled: this.readOnly,
            moduleid: this.moduleid,
            paymentDetailsRecord:this.paymentDetailsRecord,
            loanFlag:this.loanFlag,
            isCustomer:this.isCustomer,
            readOnly:this.readOnly,
            pendingApproval:this.pendingApproval,
            height: 200,
            receiptEntryObject: this,
            isAllowedSpecificFields:this.isAllowedSpecificFields,
            cls: 'gridFormat',
            isMultiDebit: this.isMultiDebit,
            amount: 0,
            //id: this.id + 'customergrid',   // SDP-13345
            closable: true,
            isIndiaGST:this.isIndiaGST,
            billid:(this.record!= undefined)?this.record.data.billid:''
        });
        if(this.loanFlag&&this.paymentDetailsRecord.isSelected){
            this.grid.addSelectedLoanRepaymentDetails(this.paymentDetailsRecord.jsonArray);
            
        }
    },
    setCustomerInfo:function(){
        this.Name.setValForRemoteStore(this.paymentDetailsRecord.get("customer"),this.paymentDetailsRecord.get("customerName"));
        this.updateAmount(getRoundedAmountValue(this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount()));
        this.RPReceivedFromStore.on('load',function(){
            this.paidTo.setValue(this.paymentDetailsRecord.data['masterReceivedForm']);
        },this);
    },
    savePringCheque: function(a, b) {
        this.isChequePrint = true;
        if(!this.Name){                // If user selects payment against GL code from main choice payment window(i.e from dashboard), Vendor/Customer combo will not be there. So value from paidTo combo will be printed on cheque. Otherwise Vendor/Customer name will be printed on cheque
            this.paidTo.allowBlank=false;
        }
        this.beforeSave();
    },
    loadCheckDetails: function(rec) {
        this.hideShowChequeFields(rec.data.detailtype, rec.data.paymentmethodacc, rec.data.paymentmethodaccname);
    },
    ShowCheckDetails: function(combo, rec,isBankToBankMethodChange) {
        this.savePrintBttn.hide();
        var accCurr = rec.data['acccurrency'];
        /*
         * ERP-16098
         * Below code block will be exexuted in create new case only
         * For edit and copy cases, payment method change is allowed only with same currency. So grid currency, original excghange rate will not be changes=d in copy and edit case.
         */
        if(!this.isEdit){
            var index = this.currencyStore.findBy(function(rec) {
                var currid = rec.data['currencyid'];
                if (currid == accCurr)
                    return true;
                else
                    return false
            }, this);
        
            if (index != -1) {
                // this.bankAccCurrency = rec.data['acccurrency'];
                // this.bankAccSelected = true;
                this.Currency.setValue(rec.data['acccurrency']);
                this.updateExternalCurrencyRateOnCurrencyChange();
                this.updateTemplateCurr();
            }
        }
        var methodAccindx = this.pmtStore.find('methodid', rec.data['methodid']);
        var paymentMethodAccountId = "";
        if (methodAccindx != -1) {
            this.pmtMethodAcc.setValue(this.pmtStore.getAt(methodAccindx).get("accountname"));
            paymentMethodAccountId = this.pmtStore.getAt(methodAccindx).get("accountid");
        }
        this.hideShowChequeFields(rec.data.detailtype, paymentMethodAccountId, rec.data.accountname,isBankToBankMethodChange);
    },
    hideShowChequeFields: function(detailType, paymentMethodAccountId, methodAccountName,isBankToBankMethodChange) {
        if (detailType == this.paymentMethodType.Bank || detailType == this.paymentMethodType.Card) {//Shown only if make payment and peyment method bank account.
         /*
             * Flag isBankToBankMethodChange will be true in edit and copy case, when payment method will be changed from one to another
             * and both are of bank type. In this case, cheque details will not be cleared.
             */
         if(!isBankToBankMethodChange){  
            if(this.SouthForm) {
                this.centerPanel.items.remove(this.SouthForm)
                this.SouthForm.destroy();
                this.SouthForm = undefined;
            }
            this.SouthForm = new Wtf.account.PayMethodPanelNew({
                region: "center",
                hideMode: 'display',
                baseCls: 'bodyFormat',
                isReceipt: true,
                type: detailType,
                disabledClass: "newtripcmbss",
                autoHeight: true,
                disabled: this.readOnly,
                style: 'margin:10px 10px;',
                id: this.id + 'southform',
                border: false
            });
            this.centerPanel.items.add(this.SouthForm);
            this.SouthForm.on("render",function(){
                if (!this.isReceipt && detailType == this.paymentMethodType.Bank) {
                    if(this.savePrintBttn && !this.readOnly) {
                        this.savePrintBttn.show()
                    }
//                    if (!this.isEdit) {//This code never execute as !this.isReceipt is always false. So commenting it
//                        this.SouthForm.setNextChequeNumber(paymentMethodAccountId);
//                    }
                }
                if (!this.isReceipt && methodAccountName != undefined) {
                    this.SouthForm.setBankName(methodAccountName);
                }
            }, this);
        }   
        } else {
            if(this.SouthForm) {
                this.centerPanel.items.remove(this.SouthForm)
                this.SouthForm.destroy();
                this.SouthForm = undefined;
            }
        }
        /*
         * In edit and copy case, when payment method is changed , and both old and new methods are of bank type, cheque details will not be cleared.
         * Following alert is given to notify user about this.
         */
        if(isBankToBankMethodChange){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),WtfGlobal.getLocaleText("acc.mprp.confirmChequeDetailsBeforeSave")],0);
        }
        this.wrapperNorth.doLayout();
        this.southPanel.doLayout();
        this.grid.doLayout();
        this.doLayout();
    },
    
    getNextSequenceNumber: function(a, val) {
        if (!(a.getValue() == "NA")) {
            WtfGlobal.hideFormElement(this.No);
            var rec = WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag = rec != null ? rec.get('oldflag') : true;
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from: this.fromnumber,
                    sequenceformat: a.getValue(),
                    oldflag: oldflag
                }
            }, this, function(resp) {
                if (resp.data == "NA") {
                    WtfGlobal.showFormElement(this.No);
                    this.No.reset();
                    this.No.allowBlank=false;
                    this.No.enable();
                } else {
                    this.No.setValue(resp.data);
                    this.No.disable();
                    this.No.allowBlank=true;
                    WtfGlobal.hideFormElement(this.No);
                }

            });
        }else {
            WtfGlobal.showFormElement(this.No);
            this.No.reset();
            this.No.enable();
            this.No.allowBlank=false;
        }
    },
    fetchPaymentAccBalance: function() {
        var currencyid = this.Currency.getValue();
        var methodAccindx = this.pmtStore.find('methodid', this.pmtMethod.getValue());
        if (methodAccindx != -1) {
            var payaccid = this.pmtStore.getAt(methodAccindx).get("accountid");
        }
        if ((currencyid != undefined || currencyid != "") && (payaccid != undefined || payaccid != "")) {
            Wtf.Ajax.requestEx({
                url: "ACCReports/getAccountBalanceInSelectedCurrency.do",
                params: {
                    tocurrencyid: currencyid,
                    accountid: payaccid
                }
            }, this, function(response, request) {
                if (response.success) {
                    this.accEndingBalance = response.endingBalance;
                    this.bankBalanceTplSummary.overwrite(this.bankBalanceTpl.body, {
                        endingBalance: WtfGlobal.addCurrencySymbolOnly(getRoundedAmountValue(response.endingBalance).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL), this.symbol)
                    });
                } else {
                    this.bankBalanceTplSummary.overwrite(this.bankBalanceTpl.body, {
                        endingBalance: "&nbsp;&nbsp;&nbsp;&nbsp;"
                    });
                }
            }, function(response, request) {
                this.bankBalanceTplSummary.overwrite(this.bankBalanceTpl.body, {
                    endingBalance: "&nbsp;&nbsp;&nbsp;&nbsp;"
                });
            });
        }
    },
    applyTemplate: function(store, index) {
        var isedit = this.Currency.getValue() != WtfGlobal.getCurrencyID() && this.Currency.getValue() != "";
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if (this.externalcurrencyrate > 0) {
            exchangeRate = this.externalcurrencyrate;
        } else if (this.isEdit && this.record.data.externalcurrencyrate) {
            var externalCurrencyRate = this.record.data.externalcurrencyrate - 0;
            if (externalCurrencyRate > 0) {
                exchangeRate = externalCurrencyRate;
            }
        }
        var revExchangeRate = 1 / (exchangeRate - 0);
        if (this.exchangeratetype != undefined && this.exchangeratetype == "foreigntobase" && this.revexternalcurrencyrate != undefined && this.revexternalcurrencyrate != 0)
        {
            revExchangeRate = this.revexternalcurrencyrate
            this.revexternalcurrencyrate = 0;
        }
        revExchangeRate = (Math.round(revExchangeRate * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
        this.southCenterTplSummary.overwrite(this.southCenterTpl.body, {
            foreigncurrency: store.getAt(index).data['currencyname'],
            exchangerate: exchangeRate,
            basecurrency: WtfGlobal.getCurrencyName(),
            editable: isedit,
            revexchangerate: revExchangeRate
        });
       
    },
    applyCurrencySymbol: function() {
        var index = this.getCurrencySymbol();
        if (index >= 0) {
            this.grid.setCurrencyid(this.currencyid, this.symbol, index);
            this.applyTemplate(this.currencyStore, index);
        }
    },
    getCurrencySymbol: function() {
        var index = null;
//        this.currencyStore.clearFilter(true) //ERP-9962
        var FIND = this.Currency.getValue();
        index = this.currencyStore.findBy(function(rec) {
            var parentname = rec.data['currencyid'];
            if (parentname == FIND)
                return true;
            else
                return false
        })
        if (index >= 0)
            this.symbol = this.currencyStore.getAt(index).data['symbol'];
        return index;
    },
    
    getPayableAmountInBase: function() {
        var amountInOriganalCurr = 0;
        var amountInBaseCurr = 0;
        if (this.isMultiDebit) {
            amountInOriganalCurr = this.grid.getMultiDebitAmount() - this.Amount.getValue();
        } else {
            amountInOriganalCurr = this.grid.getAmount(true) - this.grid.getAmount(false);
        }
        amountInBaseCurr = amountInOriganalCurr * this.getExchangeRate();
        return amountInBaseCurr;
    },
    getExchangeRate: function() {
        var index = this.getCurrencySymbol();
        var rate = this.externalcurrencyrate;
        var revExchangeRate = 0;
        if (index >= 0) {
            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
            if(this.externalcurrencyrate!=undefined&&this.externalcurrencyrate>0) {
                exchangeRate = this.externalcurrencyrate;
            }else if(this.isEdit && this.record.data.externalcurrencyrate!=undefined){
                var externalCurrencyRate = this.record.data.externalcurrencyrate-0;
                if(externalCurrencyRate>0){
                    exchangeRate = externalCurrencyRate;
                }
            }
            revExchangeRate = 1 / (exchangeRate);
            revExchangeRate = (Math.round(revExchangeRate * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
        }
        return revExchangeRate;
    },
    getPayableAmountInBaseCNDN: function() {
        var amountInOriganalCurr = 0;
        var amountInBaseCurr = 0;
        if (this.isMultiDebit) {
            amountInOriganalCurr = this.grid.getMultiDebitAmount() - this.Amount.getValue();
        } else {
            //            amountInOriganalCurr = this.grid.getAmount(true) + this.cndnGrid.getAmount(true) - this.grid.getAmount(false) + this.cndnGrid.getAmount(false);
        }
        amountInBaseCurr = amountInOriganalCurr * this.getExchangeRate();
        return amountInBaseCurr;
    },
    getAmountPaidInBase: function(amountInOriganalCurr) {
        var amountInBaseCurr = 0;
        amountInBaseCurr = amountInOriganalCurr * this.getExchangeRate();
        return WtfGlobal.conventInDecimalWithoutSymbol(amountInBaseCurr);
    },
    addPaidTo: function() {
        addMasterItemWindow('18');
        Wtf.getCmp("masterconfigurationonly").on('update', function() {
            this.paidTo.store.reload();
        }, this);

    },
    
    beforeSave: function () {
        var selectedAccounts = "";
        var receiptDetails = eval(this.grid.getData());
        if (receiptDetails != null && receiptDetails != undefined) {
            for (var i = 0; i < receiptDetails.length; i++) {
                if (receiptDetails[i] != null && receiptDetails[i] != undefined && receiptDetails[i].type == "4") {
                    if (i != receiptDetails.length - 1) {
                        selectedAccounts += receiptDetails[i].documentid + ",";
                    } else {
                        selectedAccounts += receiptDetails[i].documentid;
                    }
                }
            }
        }

        this.accountAccessStore.load({
            params: {
                mode: 2,
                grouper: 'paymentTrans',
                ignorecustomers: true,
                ignorevendors: true,
                isForPaymentReceipt: true,
                requestModuleid: this.moduleid,
                ids: selectedAccounts,
                nondeleted: true
            },
            scope: this,
            callback: this.save
        });
    },
    
    save: function() {
        if(this.isBulkPayment){
            this.updateBalanceAmount();
        }
        this.No.setValue(this.No.getValue().trim()); 
        var isValidNorthForm=this.NorthForm.getForm().isValid();
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        var isValidChequeDetails= true;       
        var isValidForAddingGLTypePayment;
        var isValidForMultipleAccounts;
        var isValidCreditAndDebitDetails=false;
        if(this.SouthForm)
            isValidChequeDetails = this.SouthForm.getForm().isValid();
        if(!isValidNorthForm || !isValidChequeDetails || !isValidCustomFields){
            WtfGlobal.dispalyErrorMessageDetails(this.id+'requiredfieldmessagepanel', this.getInvalidFields());
            this.NorthForm.doLayout();
            this.isChequePrint=false;
            this.paidTo.allowBlank=true;
            return;
        } else {
            Wtf.getCmp(this.id+'requiredfieldmessagepanel').hide();
        }
        if (this.grid.getData() == '[]' && this.grid.typeCombo.getValue()!=this.grid.ADVType) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.5")], 2);
            return;
        }
        var validLineItem = this.grid.checkDetails(this.grid);
        if (validLineItem != "" && validLineItem != undefined) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);           
            return;
        }
        /*
         * Validate GST dimension values present or Not
         */
        if (this.isIndiaGST && Wtf.isShowAlertOnDimValueNotPresent.indexOf(parseInt(this.moduleid))> -1 &&
                this.isAdvanceTypeTransaction(this.grid)) {
            if (!isGSTDimensionValuePresent(this, this.grid)) {
                return false;
            }
            /**
             * Show alert on Save document if GST details not presnet 
             * ERP-39257
             */
             if(this.paymentType !== this.paymentOption.AgainstGL){
            if (!isGSTHistoryPresentOnDocumentCreation(this)) {
                return false;
            }
        }
        }
        if(this.paymentType == this.paymentOption.AgainstCustomer){
            isValidForAddingGLTypePayment= this.grid.isValidForAddingGlTypePayment();
            if(!isValidForAddingGLTypePayment){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rp.noAdvInvNoteEntry")], 2);
                return;
            }
        }
        if(this.paymentType == this.paymentOption.AgainstGL){
            var isValidForGSTTypeOfAccounts=this.isValidForGSTTypeOfAccounts();
            if(!isValidForGSTTypeOfAccounts){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.alertForSelectingGst")], 2);
                return;
            }
        }
//        if(this.paymentType == this.paymentOption.AgainstVendor || this.paymentType == this.paymentOption.AgainstCustomer){
//            var data=this.grid.isValidForMultipleAccounts();
//            isValidForMultipleAccounts=data.isValid;
//            if(!isValidForMultipleAccounts){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mprp.transactionsBelongsToDiffAccounts")], 2);
//                return;
//            }    
//        }
        isValidCreditAndDebitDetails = this.grid.isValidCreditAndDebitDetails();
        if(!isValidCreditAndDebitDetails){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rp.alertCreditDebitDetailsInvalid")],2);
            return
        }
        if((getRoundedAmountValue(this.Amount.getValue())==0 || getRoundedAmountValue(this.grid.getTotalAmountIncludingTax())==0) && getRoundedAmountValue(this.grid.getTotalDiscountOfAllLineLevelItems()) == 0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.totalAmtCanNotBeZero")],2);
                return
        }           
        /*
           *Check for Total amount of selected transactions does not match to  amount  you have entered.
         */
        if(this.paymentType != this.paymentOption.AgainstGL){
            var amount=getRoundedAmountValue(this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount());
            if(this.Amount.getValue()< getRoundedAmountValue(amount)){		//SDP-11802
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.totalAmountDoesNotMatchToAmount")],2);
                return;
            }
        }
        /*
         *For display warning message when balace amount is greater than zero.
         * i.e. "There is some Balance Amount for this Payment which will get adjusted in Adjustment Account set in Company Preferences".
         */
        
        if(this.finalBalanceAmount!= 0 ){
            if(this.checkWarning){
                if(this.paymentType != this.paymentOption.AgainstGL){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.messages.adjustmentaccountpaymentamount"),
                        width:370,
                        buttons: Wtf.MessageBox.YESNO,
                        fn:function(btn){
                            if(btn==='no'){
                                this.checkWarning=true;
                                return;
                            }else{
                                this.checkWarning=false;
                                this.beforeSave();
                            } 
                        },
                        icon: Wtf.MessageBox.INFO,
                        scope: this
                    });
                    return;
                
                }else{
                   /*
                    *Total amount of selected transactions does not match to amount you have entered.For Against GL
                    */
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.totalAmountDoesNotMatchToAmount")],2);
                    return
                }
            }else{
                 /*
                  *If Adjustment account not set in company preferance then show pop message "Select Adjustment Account For Payment in Company Preferences"
                  */
                if (Wtf.account.companyAccountPref.adjustmentAccountReceipt == "") {
                    this.checkWarning=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.messages.selectadjustmentaccountReceipt")],2);
                    return;
                }
            }
                
        }
      
        if(this.paymentType == this.paymentOption.AgainstCustomer){
//            var store = this.Name.store;
            var record = WtfGlobal.searchRecord(this.personstore, this.Name.getValue(), "accid");
            var isFullPaymentRequired = record.data['overseas']
            if(isFullPaymentRequired){     
                var obj = this.validateForFullPayment();
                if(obj.isInvalid){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), obj.msg], 2);
                    return;
                }    
            }
        }
        if(this.isCopyReceipt){
            var notAccessAccountsList="";
            var accHasAccessFlag=false;
            if(!checkForAccountActivate(this.accountAccessStore,this.bankChargesAccount.getValue(),"accountid")){
                accHasAccessFlag=true;
                notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.field.BankChargesAccount")+ ", ";
            }
            if(!checkForAccountActivate(this.accountAccessStore,this.bankInterestAccount.getValue(),"accountid")){
                accHasAccessFlag=true;
                notAccessAccountsList = notAccessAccountsList + WtfGlobal.getLocaleText("acc.field.BankInterestAccount")+ ", ";
            }
            if(notAccessAccountsList!=""){
                notAccessAccountsList = notAccessAccountsList.substring(0, notAccessAccountsList.length-2);
            }
            if(accHasAccessFlag){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.field.Pleaseselectactivatedaccount")+notAccessAccountsList+".",
                    width:370,
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this
                });
                return;
            }
            var receiptDetails = eval(this.grid.getData());
            if(receiptDetails!=null && receiptDetails!=undefined){
                var notAccessAccList="";
                var hasAccessFlag=false;
                for(var i=0;i<receiptDetails.length;i++){
                    var rec= receiptDetails[i];
                    if(rec.type=="4"){
                        if(!checkForAccountActivate(this.accountAccessStore,rec.documentid,"accountid")){
                            hasAccessFlag=true;
                            notAccessAccList=notAccessAccList+decodeURIComponent(rec.documentno)+", ";
                        }
                    }
                }
                if(notAccessAccList!=""){
                    notAccessAccList = notAccessAccList.substring(0, notAccessAccList.length-2);
                }
                if(hasAccessFlag){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.field.Inselectedaccountssomeaccountsaredeactivated")+
                        "<br>"+WtfGlobal.getLocaleText("acc.field.DeactivatedAccounts")+notAccessAccList,
                        width:370,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this
                    });
                    return;
                }
            }
        }
        this.saveData();  
    },
    /**
     * Function to check whether advance is selected or not
     */
    isAdvanceTypeTransaction: function(grid) {
        for (var i = 0; i < grid.store.getCount(); i++) {
            var type = grid.store.getAt(i).data['type'];
            if (type == 1) {
                return true;
                break;
            }
        }
        return false;
    },
    calCustMinBugetInSelectedCurrency:function(){
        var custMinBudget=this.pmtStore.getAt(this.pmtStore.find('methodid',this.pmtMethod.getValue())).data.acccustminbudget;
        var currency=this.pmtStore.getAt(this.pmtStore.find('methodid',this.pmtMethod.getValue())).data.acccurrency;
        var i=0;
        while(i<this.currencyStore.getCount()){
            var currRec = this.currencyStore.getAt(i).data; 
            if(currency==currRec.currencyid){
                var tempExchangeRate=currRec.exchangerate
                break;
            } 
            i++;
        }
        var budgetInBaseCurrency=(custMinBudget)/tempExchangeRate;
        var index=this.getCurrencySymbol();
        if(index>=0){
            var exchangeRate = currRec.exchangerate;
        }
        var budgetInSelectedCurrency=budgetInBaseCurrency*exchangeRate;
        return budgetInSelectedCurrency;
    },
    
    saveData: function() {
        Wtf.getCmp(this.id + 'requiredfieldmessagepanel').hide();
        
        var msg = WtfGlobal.getLocaleText("acc.je.msg1");
        if (Wtf.Countryid == Wtf.CountryID.MALAYSIA && this.isNonZeroRatedTaxCodeUsedInTransaction()) {
            msg = WtfGlobal.getLocaleText("acc.tax.nonZeroTaxcode.alert");
        }
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"), msg, function(btn) {
            if (btn != "yes") {
                this.isChequePrint = false;
                this.checkWarning=true;
                return;
            }
//            this.makeFieldsEnableDisable(false);  // Values of the fields which are disabled are null. So before saving those fiels, we are making those fields enable and read only.
            var jsonObject = this.NorthForm.form.getValues();
            WtfGlobal.onFormSumbitGetDisableFieldValues(this.NorthForm.form.items, jsonObject);
            if (this.isIndiaGST) {
                jsonObject['CustomerVendorTypeId'] = this.CustomerVendorTypeId;
                jsonObject['GSTINRegistrationTypeId'] = this.GSTINRegistrationTypeId;
                jsonObject['gstin'] = this.gstin;
                if (this.isEdit && !this.isCopyReceipt) {
                    jsonObject['gstdochistoryid'] = this.gstdochistoryid;
                }
            }
            var custFieldArr = this.tagsFieldset.createFieldValuesArray();
            if (custFieldArr.length > 0)
                jsonObject['customfield'] = JSON.stringify(custFieldArr);
            jsonObject['Details'] = this.grid.getData();
            if (this.SouthForm) {
                var object=this.SouthForm.form.getValues();
                object['paymentthrough']=this.SouthForm.bank.getRawValue();
                jsonObject['paydetail'] = JSON.stringify(object);
            }
            jsonObject['balaceAmount'] =this.finalBalanceAmount;
            jsonObject['accountIdComPreAdjRec'] =Wtf.account.companyAccountPref.adjustmentAccountReceipt;
            jsonObject['iscustomer'] = Wtf.getCmp('isCustomer' + this.helpmodeid + this.id).getValue();
            jsonObject['isChequePrint']=this.isChequePrint;
            if(this.externalcurrencyrate!=undefined&&this.externalcurrencyrate>0) {
                jsonObject['externalcurrencyrate'] = this.externalcurrencyrate;
            }else if(this.isEdit && this.record.data.externalcurrencyrate!=undefined){
                var externalCurrencyRate = this.record.data.externalcurrencyrate-0;
                if(externalCurrencyRate>0){
                    jsonObject['externalcurrencyrate'] = externalCurrencyRate;
                }
            }
            this.isLinkedToClaimedInvoice = false;
            
            var store=this.grid.getStore();
            var len=store.getCount();
            
            for(var i=0;i<len;i++){ 
                if(store.getAt(i).data.isClaimedInvoice == "true"){
                    this.isLinkedToClaimedInvoice = true;
                }
            }
            
            jsonObject['isLinkedToClaimedInvoice'] = this.isLinkedToClaimedInvoice;
            jsonObject['ismulticurrencypaymentje']= this.isMultiCurrencyPayment();
            jsonObject['isEditToApprove'] = this.isEditToApprove
            this.saveBttn.disable();
            this.savencreateBttn.disable();
            WtfComMsgBox(27, 4, true);
            this.ajxUrl = this.getUrlForSave();
            WtfGlobal.setAjaxTimeOut();             // ERP-12447  increse the request timeout to avoid duplicate entries
            Wtf.Ajax.requestEx({
                url: this.ajxUrl,
                params: jsonObject
            }, this, this.genSavePaymentSuccessResponse, this.genSavePaymentFailureResponse);
        }, this);
    },
    isNonZeroRatedTaxCodeUsedInTransaction: function () {
        var isNonZeroRatedTaxCodeUsedInTransaction = false;
        if (this.creationDate != undefined && this.creationDate.getValue() != undefined && (new Date(this.creationDate.getValue()) >= new Date(Wtf.ZeroRatedTaxAppliedDateForMalasia))) {
            this.grid.getStore().each(function (rec) {
                if (!Wtf.isEmpty(rec.data, false) && !Wtf.isEmpty(rec.data.prtaxid, false)) {
                    var taxrec = WtfGlobal.searchRecord(this.grid.taxStore, rec.data.prtaxid, 'prtaxid');
                    if (taxrec && taxrec.data && taxrec.data.percent > 0) {
                        isNonZeroRatedTaxCodeUsedInTransaction = true;
                        return;
                    }
                }
            }, this);
        }
        return isNonZeroRatedTaxCodeUsedInTransaction;
    },
    getUrlForSave: function() {
        var url = "ACCReceiptNew/saveReceipt.do";
        if(this.isAllowedSpecificFields){
            url="ACCReceiptNew/updateReceipt.do";
        }
        return url
    },
    genSavePaymentSuccessResponse: function(response, request) {
        WtfGlobal.resetAjaxTimeOut();            // ERP-12447  reset request timeout to default
        if (!response.success) {/* Failed Case*/
             /*
             *For Check Duplicate documnet no
             */
            if ((this.moduleid == Wtf.Acc_Receive_Payment_ModuleId) && response.isAccountingExe) {
                Wtf.MessageBox.hide();
                var label="";
                label = WtfGlobal.getLocaleText("acc.payment.newreceivepaymnetno");
                this.newdowin = new Wtf.Window({
                    title: WtfGlobal.getLocaleText("acc.common.success"),
                    closable: true,
                    iconCls: getButtonIconCls(Wtf.etype.deskera),
                    width: 330,
                    autoHeight: true,
                    modal: true,
                    bodyStyle: "background-color:#f1f1f1;",
                    closable:false,
                    buttonAlign: 'right',
                    items: [new Wtf.Panel({
                        border: false,
                        html: (response.msg.length>60)?response.msg:"<br>"+response.msg,
                        height: 50,
                        bodyStyle: "background-color:white; padding: 7px; font-size: 11px; border-bottom: 1px solid #bfbfbf;"
                    }),
                    this.newdoForm = new Wtf.form.FormPanel({
                        labelWidth: 190,
                        border: false,
                        autoHeight: true,
                        bodyStyle: 'padding:10px 5px 3px; ',
                        autoWidth: true,
                        defaultType: 'textfield',
                        items: [this.newdono = new Wtf.form.TextField({
                            fieldLabel: label,
                            allowBlank: false,
                            labelSeparator: '',
                            width: 90,
                            itemCls: 'nextlinetextfield',
                            name: 'newdono',
                            id: 'newdono'
                        })],
                        buttons: [{
                            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                            handler: function () {
                                if (this.newdono.validate()) {
                                    Wtf.getCmp("receiptNo"+this.helpmodeid+this.id).setValue(this.newdono.getValue());
                                    this.saveData(); 
                                    this.newdowin.close();
                                }
                            },
                            scope: this
                        }, {
                            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                            scope: this,
                            handler: function () {
                                this.newdowin.close();
                            }
                        }]
                    })]
                });
                this.newdowin.show();    
            }else if ((this.moduleid == Wtf.Acc_Receive_Payment_ModuleId) && this.isEdit && response.isAccountingExe) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
            } else {
                if(this.isCopyReceipt){
                    WtfComMsgBox(["Copy Receipt",response.msg],response.success*2+2);
                }else if(this.isEdit){
                    WtfComMsgBox(["Edit Receipt",response.msg],response.success*2+2);
                }else{
                    WtfComMsgBox(["Receipt",response.msg],response.success*2+2);
                }
            }
            
            
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.accPref.autoReceipt"), response.msg], response.success * 2 + 1);
//            this.restoreFieldsPreviousState();
            this.enableSaveButton();
            return;
        } else {
             Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,
                width:370,
                scope: {
                  scopeObj:this  
                },
                buttons: Wtf.MessageBox.OK,
                fn: function(btn ,text, option) {
                    this.scopeObj.refreshReportGrid();
                    if(this.scopeObj.moduleid != Wtf.Acc_Receive_Payment_ModuleId){
                        this.scopeObj.fireEvent("update",this);   
                    }
                     //Added an event for Import Bank reconciliation window.
                },
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });            
        }        
        if(this.saveAndCreateNewFlag){/* Save And Create New Case*/
                this.enableSaveButton();
                var defaultPayMenthodId = this.pmtMethod.getValue();
                var index = this.pmtStore.findBy( function(rec){
                    var methidid=rec.data['methodid'];
                    if(methidid==defaultPayMenthodId)
                        return true;
                    else
                        return false
                }, this);
                this.resetComponents();
                this.grid.getStore().removeAll();
                this.grid.addBlankRow();
                this.pmtMethod.setValue(defaultPayMenthodId);
                if(index > -1){
                    this.ShowCheckDetails(null,this.pmtStore.getAt(index));
                }
                if(this.SouthForm)
                    this.SouthForm.clearanceDate.getEl().up('.x-form-item').setDisplayed(false);
                this.tplSummary.overwrite(this.southCalTemp.body,{taxamount:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),amountwithouttax:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),received:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),due:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),receivable:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),receivableInBase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),advance:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),advanceInBase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),receivedinbase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol())});
                this.tagsFieldset.resetCustomComponents();  // Reset custom fields and dimensions
            } else {/*Save Case*/
                this.enableButtons();
                this.disableComponents();
                this.response = response;
                this.request = request;
                var rec=this.NorthForm.getForm().getValues();
                this.exportRecord=rec;
                this.exportRecord['billid']=response.paymentid;
                this.exportRecord['billno']=response.billno;
                this.exportRecord['amount']=response.amount||"";
                this.exportRecord['advanceamount']=(response.advanceamount==""||response.advanceamount==undefined)?((this.exportRecord.advanceamt==undefined)?"":this.exportRecord.advanceamt):response.advanceamount;
                this.exportRecord['personname']=this.Accountname;
                this.exportRecord['personid']=this.personid;
                this.exportRecord['advanceid']="";
                this.exportRecord['address']=response.address?response.address:"";
                this.exportRecord['receipttype']=response.receipttype;	
                this.exportRecord['isExportPayment']=true;	
                if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType) || !WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
                    this.exportPdfBttn.exportRecord=this.exportRecord;
//                    this.singleRowPrint.exportRecord=this.exportRecord;
                }
                this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
            }
         if (this.isChequePrint) {
                var resdata = response.data[0];
                var accountName=(this.Name)?this.Name.getRawValue():this.paidTo.getRawValue(); //If payment against Vendor/Customer, then name of the corresponding person will be printed otherwise name will be as selected in paid to combobox.
                resdata.accountName=accountName;
                this.printCheque(resdata);//Passing parameter as JSON object
                this.isChequePrint = false;
            }   
    },
    genSavePaymentFailureResponse: function(response) {
        Wtf.MessageBox.hide();
//        this.restoreFieldsPreviousState();
        this.enableSaveButton();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    
    isOneTimeCustomer: function(isChecked) {
        if(isChecked) {
            this.personAccStore.proxy.conn.url= "ACCCustomer/getCustomersForCombo.do";
            var currentBaseParams = this.personAccStore.baseParams;
            currentBaseParams.isPermOrOnetime=true;
            this.createNameField(true); // isOneTimeCustomer flag - true
            this.personAccStore.baseParams = currentBaseParams;
            this.personAccStore.load();
        } else {
            this.createNameField(false); // isOneTimeCustomer flag - false
        }
    },
    
    setroundedvalue: function(obj,isbankcharges) {
        var bankcharges = obj.getValue();
        if (bankcharges != "" && bankcharges != undefined) {
            var roundedvalue = parseFloat(getRoundofValueWithValues(bankcharges, Wtf.AMOUNT_DIGIT_AFTER_DECIMAL).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
            obj.setValue(roundedvalue);
        } else if(isbankcharges && bankcharges=='' && this.bankChargesAccount.getRawValue()!=''){
            obj.allowBlank=false;
            obj.validate();
        } else if (!isbankcharges && bankcharges=='' && this.bankInterestAccount.getRawValue()!=''){
            obj.allowBlank=false;
            obj.validate();
        }
    },
    setPMData: function() {  
        var rec;
        var methodid ;
        var rowIndex;
        if(!this.isEdit && !this.isCopyReceipt && !this.readOnly && !this.isBulkPayment){
            rowIndex = this.pmtStore.find("isdefault", "true");
            if (rowIndex != -1) {
                rec = this.pmtStore.getAt(rowIndex);                
                methodid = rec.get('methodid');
                this.pmtMethod.setValue(methodid);
                this.ShowCheckDetails(this.pmtMethod, rec);
            }
            this.hideShowAccountBalance();
        }
        if (this.isBulkPayment) {
            
            if (!this.mapDefaultPmtMethod && this.isCustomer) {     // ERM735 Map Default payment method to customer
               this.setCashMethod();
            }else if(this.mapDefaultPmtMethod && this.Name && !this.pmtMethod.isValid()  && this.isCustomer){
                var rec = WtfGlobal.searchRecord(this.personstore, this.Name.getValue(), "accid");
                if(!Wtf.isEmpty(rec)){
                    this.mapDefaultPaymentMethod(this.Name,rec);
            }
            }
        }
        if(this.isEdit || this.readOnly){// Set Payment Method value in edit, copy and view case.
            this.pmtMethod.setValue(this.record!=null?this.record.data.methodid:"");
        }
    },
    setCashMethod:function(){
            var rowIndex = this.pmtStore.find("methodname", "cash");
            var rec = this.pmtStore.getAt(rowIndex);
            var methodid = rec.get('methodid');
            this.pmtMethod.setValue(methodid);                          
            this.ShowCheckDetails(this.pmtMethod, rec);
            this.fetchPaymentAccBalance();
            this.hideShowAccountBalance();
    },
    updateAmount: function(amount) {
        this.Amount.setValue(getRoundedAmountValue(amount));
    },
    
    updateSouthTemp:function() {
        this.applyCurrencySymbol();
        this.tplSummary.overwrite(this.southCalTemp.body, {
            taxamount: WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitTaxAmount(), this.symbol),
            amountwithouttax: WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount(), this.symbol),
            received: WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount(), this.symbol),
            due: WtfGlobal.addCurrencySymbolOnly(this.Amount.getValue(), this.symbol),
            receivable: WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount() - this.Amount.getValue(), this.symbol),
            receivableInBase: WtfGlobal.addCurrencySymbolOnly(this.getPayableAmountInBase(), WtfGlobal.getCurrencySymbol()),
            receivedinbase: WtfGlobal.addCurrencySymbolOnly(this.getAmountPaidInBase(this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount()), WtfGlobal.getCurrencySymbol())
        });

    },
    
    getInvalidFields: function() {
        var invalidFields = []
        this.NorthForm.getForm().items.filterBy(function(field) {
            if (field.validate()) return;
            invalidFields.push(field);
        });
        if(this.SouthForm) {
            this.SouthForm.getForm().items.filterBy(function(field) {
                if (field.validate()) return;
                invalidFields.push(field);
            });
        }
        var invalidCustomFieldsArray=this.tagsFieldset.getInvalidCustomFields();// Function for getting invalid custom fields and dimensions 
        for(var i=0;i<invalidCustomFieldsArray.length;i++){
            invalidFields.push(invalidCustomFieldsArray[i]);
        }
        return invalidFields;     
    },
    
    showChequeInformation: function() {
        this.ShowCheckDetails(null, this.record);
        var type = this.record.data.detailtype;
        if (type == 2) {
            if (this.isCopyReceipt != undefined && this.isCopyReceipt) {
                this.SouthForm.checkNo.setValue("");
            } else {
                this.SouthForm.checkNo.setValue(this.record.data.refno);
            }
            this.SouthForm.description.setValue(unescape(this.record.data.refdetail));
            this.SouthForm.bank.setValue(unescape(this.record.data.refname));
            this.SouthForm.bankTypeStore.on('load', function() {
                this.SouthForm.bank.setValue(this.record.data.refname);
            }, this)
            if (this.record.data.paymentStatus) {
                this.SouthForm.paymentStatus.setValue("Cleared");
                this.SouthForm.clearanceDate.getEl().up('.x-form-item').setDisplayed(true);
                this.SouthForm.clearanceDate.setValue(this.record.data.clearanceDate);
            }
        } else if (type == 1) {
            //                this.expDate.setValue(new Date());

            if (this.record.data.refno == "") {
                this.SouthForm.hide();
            } else {
                this.SouthForm.refNo.setValue(this.record.data.refno);
                this.SouthForm.cardNo.setValue(this.record.data.refcardno);
                this.SouthForm.nameOnCard.setValue(this.record.data.refname);
                this.SouthForm.cardType.setValue(this.record.data.refdetail);
                this.SouthForm.expDate.setValue(this.record.data.expirydate);
            }
        }
        this.doLayout();
    },
    getConfirmationToChangePmtMethod: function (comboboxObject, newValue, oldValue) {
        var pmtMethodRecord = WtfGlobal.searchRecord(this.pmtStore, newValue, "methodid");
        if (oldValue == '') {                                       // Alert will not be shown when payment method is changed initially, as its default valu will be blank('')
            this.hideShowCheckDetailsOnPaymentMethodChange(comboboxObject);
            this.grid.getStore().removeAll();
            this.grid.addBlankRow();
            this.updateSouthTemp();
            this.updateBalanceAmount();
            this.updatePaymentDetailsOnPaymentMethodChange(comboboxObject);
        } else if (pmtMethodRecord != undefined && pmtMethodRecord != null && this.receiptCurrencyBeforeSelect == pmtMethodRecord.data["acccurrency"] && newValue == oldValue) {
            return;
        } else if (pmtMethodRecord != undefined && pmtMethodRecord != null && this.receiptCurrencyBeforeSelect == pmtMethodRecord.data["acccurrency"] && newValue != oldValue) {
            this.hideShowCheckDetailsOnPaymentMethodChange(comboboxObject);
            this.checkForMultiCurrencyToDecideExchangeRate();
        } else {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.masterConfig.payMethod"), WtfGlobal.getLocaleText("acc.mp.dataWillBeCleared"), function (btn) {
                if(btn!="yes") {
                    comboboxObject.setValue(oldValue);
                    var oldPmtRec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
                    this.pmtMethod.fireEvent('select',this.pmtMethod,oldPmtRec,0,this);
                    this.updateExternalCurrencyRateOnCurrencyChange();
                    this.checkForMultiCurrencyToDecideExchangeRate();
                    return;
                } else {
                    /*
                     * If there is no currency rate is set for currency of selected payment method, window will be pop up to add exchange rate
                     */
                    var currencyRecord = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
                    if(currencyRecord==undefined || currencyRecord==null){
                        comboboxObject.setValue(oldValue);
                        var oldrec = WtfGlobal.searchRecord(this.pmtStore, oldValue, "methodid");
                        var oldCurrency=oldrec.data.acccurrency;
                        this.Currency.setValue(oldCurrency);
                        var panel = callCurrencyExchangeWindow();
                        panel.on("update",function() {             // If exchange rate is added for particular date, again request will be sent to server to fetch exchange rate at that Date (i.e. exchange rate added just now)
                            this.changeCurrencyStore();
                        }, this);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.10")], 2);
                        return;
                    }
                    this.hideShowCheckDetailsOnPaymentMethodChange(comboboxObject);
                    this.grid.getStore().removeAll();
                    this.grid.addBlankRow();
                    this.updateExternalCurrencyRateOnCurrencyChange();
                    this.Amount.setValue(0);
                    this.updateSouthTemp();
                    this.updateBalanceAmount();
                    this.checkForMultiCurrencyToDecideExchangeRate();
                }
            },this);
        }
    },
    
    hideShowCheckDetailsOnPaymentMethodChange: function (comboboxObject) {
        var selectedMethod = comboboxObject.value;
        var index = this.pmtStore.findBy(function (rec) {
            var methodId = rec.data['methodid'];
            if (methodId == selectedMethod)
                return true;
        }, this);
        var record = this.pmtStore.getAt(index);
        this.ShowCheckDetails(comboboxObject, record);
    },
    createNameField: function(isOneTimeCustomer) {
        if(this.paymentType !== this.paymentOption.AgainstGL){
            var nameIndex = false;
            if(this.Name) {
                this.NorthForm.getForm().items.remove(this.Name);
                this.Name.destroy();
                this.Name = undefined;
                nameIndex = true;
            }
            var comboConfig = {
                id: this.isCustomer?"customer" + this.helpmodeid + this.id:"vendor" + this.helpmodeid + this.id,
                hiddenName: 'accid',
                name:'accid',
                disabled: this.isEdit,
                emptyText: WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup.SelectAccount"), //' Select an Account...',
                listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 450,
                anchor: '86%',
                isVendor:!(this.isCustomer),
                isCustomer:this.isCustomer,
                loanFlag:this.loanFlag,
                minChars: 0,        // ERP-12377 : For remote store, minchar=4 by default. Changed to 0 for loading the data when search filter is cleared. 
                extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'],
                addNewFn: this.addPerson.createDelegate(this, [false, null, (this.isCustomer ? 'Customerwindow' : 'Vendorwindow'), this.isCustomer], true),
            };
            
            if(this.custVenOptimizedFlag){
                comboConfig.ctCls = 'optimizedclass';
                comboConfig.hideTrigger = true;
            }
            
            if(this.isCustomer) {
                this.Name = CommonERPComponent.createCustomerPagingComboBox('auto', 550, Wtf.ProductCombopageSize, this, comboConfig);
            } else {
                this.Name = CommonERPComponent.createVendorPagingComboBox('auto', 550, Wtf.ProductCombopageSize, this, comboConfig);
            }
            
            this.personstore = this.getNameComboStore();
            
            this.Name.on('change',this.toggleButtonRelease,this);
            this.Name.on('select',function(combo, record, index){
                this.loadPersonStore(record.data.accid);
            },this);
            this.Name.on('beforeselect', function(combo, record, index) {
                    this.nameBeforeSelect = combo.getValue();
                    return validateSelection(combo, record, index);
            }, this);
            /**
             * SDP-13638 Added check for 'ShowOnlyOneTime' Customer. 
             */
            if (this.Name != null && this.Name != undefined) {
                var isEditORisCopy = (this.isEdit != undefined ? this.isEdit : false) || (this.copyInv != undefined ? this.copyInv : false) || (this.isCopyFromTemplate != undefined ? this.isCopyFromTemplate : false);
                this.Name.store.on('beforeload', function (store, object) {
                    if (!object.params) {
                        object.params = {};
                    }
                    var currentBaseParams = store.baseParams;
                    if (isEditORisCopy) {
                        currentBaseParams.isPermOrOnetime = "";
                    } else {
                        if (this.ShowOnlyOneTime != undefined && this.ShowOnlyOneTime.getValue() == true) {
                            currentBaseParams.isPermOrOnetime = true;
                        } else {
                            currentBaseParams.isPermOrOnetime = false;
                        }
                    }
                    store.baseParams = currentBaseParams;
                }, this);
            }
            Wtf.getCmp(this.id+'MiddleColumnform').insert(0,this.Name);
            if(nameIndex) {
                this.NorthForm.getForm().items.add(this.Name);
            }
            this.NorthForm.doLayout();
        } 
    },
    onPersonStoreLoad: function (recArr, options, success) {
        var combo = this.Name;
        var record = recArr[0];
        if (this.mapDefaultPmtMethod && this.isCustomer) {
            if (combo.getValue() == this.nameBeforeSelect) { //If same name selected no need to do any action 
                return;
            }
            this.mapDefaultPaymentMethod(combo,record); //display default paymentmethod on customer select.
        }
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.addressMappingRec=record.data.addressMappingRec;
            /**
             * ERP-32829 
             * code for New GST  i.e. populate dimension using dimension
             */
            if (record.data.currentAddressDetailrec != undefined) {
                this.applyGSTFieldsBasedOnDate();
                this.addressDetailRecForGST=record.data.currentAddressDetailrec[0];
                if (!this.isModuleForAvalara) {
                    var obj = {};
                    obj.tagsFieldset=this.tagsFieldset;
                    obj.currentAddressDetailrec=this.addressDetailRecForGST;
                    obj.mappingRec=this.addressMappingRec;
                    obj.isCustomer=this.isCustomer;
                    obj.isShipping=this.isShipping;
                    populateGSTDimensionValues(obj);
                }
            }
        }
        /**
          * SDP-12299
          * code for populate dimension using dimension
          */
        if (WtfGlobal.isUSCountryAndGSTApplied()) {
            this.addressMappingRec = record.data?record.data.addressMappingRec:"";
            if (record.data && record.data.currentAddressDetailrec != undefined) {
                this.addressDetailRecForGST = record.data.currentAddressDetailrec[0];
                if (!Wtf.account.companyAccountPref.avalaraIntegration) {
                    var obj = {};
                    obj.tagsFieldset = this.tagsFieldset;
                    obj.currentAddressDetailrec = this.addressDetailRecForGST;
                    obj.mappingRec = this.addressMappingRec;
                    obj.isCustomer = this.isCustomer;
                    obj.isShipping = this.isShipping;
                    populateGSTDimensionValues(obj);
                }
            }
        }
        this.paidTo.setValue(record.data['masterReceivedForm']);
    },
    applyGSTFieldsBasedOnDate: function() {
        if (this.Name.getValue() == undefined || this.Name.getValue() == ''){
           return;
        }
        Wtf.Ajax.requestEx({
            url: this.isCustomer ? "ACCCustomerCMN/getCustomerGSTHistory.do" : "ACCVendorCMN/getVendorGSTHistory.do",
            params: {
                customerid: this.Name.getValue(),
                vendorid: this.Name.getValue(),
                returnalldata: true,
                isfortransaction: true,
                transactiondate: WtfGlobal.convertToGenericDate(this.creationDate.getValue())

            }
        }, this, function(response) {
            if (response.success) {
                 /**
                 * Validate GST details
                 */
                isGSTDetailsPresnetOnTransactionDate(response,this,this.grid,this.Name);
                this.ignoreHistory = true;
                this.GSTINRegistrationTypeId = response.data[0].GSTINRegistrationTypeId;
                this.gstin = response.data[0].gstin;
                this.CustomerVendorTypeId = response.data[0].CustomerVendorTypeId;
                this.uniqueCase = response.data[0].uniqueCase;
                this.transactiondateforgst = this.creationDate.getValue();
                this.CustVenTypeDefaultMstrID=response.data[0].CustVenTypeDefaultMstrID;
                this.GSTINRegTypeDefaultMstrID=response.data[0].GSTINRegTypeDefaultMstrID;
                getLineTermDetailsAndCalculateGSTForAdvance(this, this.grid, this.grid.productid);
            }
        });
    },
 populateGSTDataOnEditCopy: function () {
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) && (this.isEdit || this.isCopyReceipt)) {
            //  var perStore = this.isCustomer ? Wtf.customerAccRemoteStore : Wtf.vendorAccRemoteStore;
            this.individualPersonDetails = new Wtf.data.Store({
                url: this.isCustomer ? "ACCCustomer/getCustomersForCombo.do" : "ACCVendor/getVendorsForCombo.do",
                baseParams: {
                    mode: 2,
                    group: this.isCustomer ? 10 : 13,
                    deleted: false,
                    nondeleted: true,
                    common: '1'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, Wtf.personRec)
            });
            this.individualPersonDetails.on('load', function (storeObj, recArr) {
                var index = this.individualPersonDetails.find('accid', this.record.data.personid);
                if (index != -1) {
                    var record = this.individualPersonDetails.getAt(index);
                    this.setGSTDetailsOnEditCase(record);
                }
            }, this);
            if (this.isCustomer) {
                this.individualPersonDetails.load({
                    params: {
                        selectedCustomerIds: this.record.data.personid
                    },
                    scope: this
                });
            } else {
                this.individualPersonDetails.load({
                    params: {
                        vendorid: this.record.data.personid
                    },
                    scope: this
                });
            }
        }
    },
    /**
     * On Edit/ Copy case set GST details
     * Populate Customer/ Vendor GST details in Edit/ Copy Case
     */
    setGSTDetailsOnEditCase: function (record) {
        this.addressMappingRec = record.data.addressMappingRec;
//        if (record.data.currentAddressDetailrec != undefined) {
//            if (record.data.uniqueCase != undefined) {
//                this.uniqueCase = record.data.uniqueCase;
//                this.sezfromdate = record.data.sezfromdate;
//                this.seztodate = record.data.seztodate;
//                this.transactiondateforgst = this.creationDate.getValue();
//            }
//            this.addressDetailRecForGST = record.data.currentAddressDetailrec[0];
//        }
    },
    
     mapDefaultPaymentMethod:function(combo,record){
        var defaultPmtMethod=record.data.paymentmethod;
        if (defaultPmtMethod != "" && defaultPmtMethod != null)
        {
            this.pmtMethod.setValue(defaultPmtMethod);
            var rowIndex = this.pmtStore.findBy(function(rec){
                var methodid=rec.data['methodid'];
                if(methodid==defaultPmtMethod)
                    return true;
                else
                    return false;
            });
            if(rowIndex !=-1){
                var record = this.pmtStore.getAt(rowIndex);
                this.ShowCheckDetails(this.pmtMethod, record);
                this.fetchPaymentAccBalance();
                this.hideShowAccountBalance();// display account balance of payment method               
            }
        }else{//If Default Payment for customer not specified is blank then setting cash as sefaultpayment method
            this.setCashMethod();
        }
    },
    
    getNameComboStore: function() {
        return (this.paymentType == this.paymentOption.AgainstCustomer ? Wtf.customerAccRemoteStore : 
                (this.paymentType == this.paymentOption.AgainstVendor ? Wtf.vendorAccRemoteStore : this.personAccStore))  
    },
    
    disableComponents: function(){
        if(Wtf.getCmp("savencreate" + this.helpmodeid + this.id)){
            Wtf.getCmp("savencreate" + this.helpmodeid + this.id).disable();
        }
        if(this.saveBttn){
            this.saveBttn.disable();
        }
        if(this.savePrintBttn){
            this.savePrintBttn.hide();
        }
        if(this.grid){
            this.grid.disable();
        }
        if(this.NorthForm){
            this.NorthForm.disable();
        }
        if(this.southPanel){
            this.southPanel.disable();
        }
        if(this.SouthForm){
            this.SouthForm.disable();
        }
        for(var i=0;i<this.toggleBtnPanel.items.items.length;i++){
            this.toggleBtnPanel.items.items[i].disable();
        }
    },
    resetComponents: function(){
        /*
         * When save and create new is clicked second time, we get Vendor/Customer store is undefined. so, to supress the exception, we have used try catch block. 
         * So that, further steps will be executed even if exeception occurs.
         */
        if(this.NorthForm){
            try{
                this.NorthForm.getForm().reset();
            } catch(e){
            }
        }
        if(this.Amount){
            this.Amount.setValue(0.0);
        }
        this.setSequenceFormatForCreateNewCase();           // when form is reset on 'save and create new' case, default sequence format will be set to combobox again.
        if(this.SouthForm){
            this.SouthForm.getForm().reset();
        }
        this.applyCurrencySymbol();
        this.grid.getStore().removeAll();
        this.grid.getView().emptyText=  WtfGlobal.getLocaleText("acc.field.Norecordstodisplay.");
        this.grid.getView().refresh();
    },
    enableButtons : function(){
        Wtf.getCmp("emailbut" + this.id).enable();
        Wtf.getCmp("exportpdf" + this.id).enable();
        if (Wtf.getCmp("printSingleRecord" + this.id)) {
            Wtf.getCmp("printSingleRecord" + this.id).enable();
        }
    },
    callEmailWindowFunction : function(response, request){
        if(response){
            if(response.pendingApproval){
                WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),WtfGlobal.getLocaleText("acc.module.name.16")+' '+WtfGlobal.getLocaleText("acc.field.ispendingforapprovalSoyoucannotsendmailrightnow")],3);
                return;
            }
            var rec =this.createRecordForEmail(response,request);
            callEmailWin("emailwin",rec,'Payment Receipt',4,false,false);
        }
    },    
    updateBalanceAmount: function(){
        var amount=this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount();
        var balanceAmount=this.Amount.getValue()-amount;
        var roundedAmount=getRoundedAmountValue(balanceAmount);
        this.balanceAmountTplSummary.overwrite(this.balanceAmountTpl.body, {
            balanceAmount:WtfGlobal.addCurrencySymbolOnly(roundedAmount, this.symbol)
        });
        this.finalBalanceAmount=roundedAmount;   // Each time balance amount is updated, this value awill also be updated. This value is used at the time of saving payment.
        this.updateSouthTemp();
            
    },
    printCheque : function(chequeDetailObject){           
        var newwin=window.open('','printwin','left=0,top=0,width=800,height=300,scrollbars = no' )
        newwin.document.write('<HTML>\n<HEAD>\n')
        newwin.document.write('<TITLE>Print Page</TITLE>\n')
        if(chequeDetailObject.isnewlayout){
            newwin.document.write('<style>')
            newwin.document.write('@page {size: portrait;margin:0mm 0mm 0mm 0mm;}')
            newwin.document.write('body  {margin: 0mm 0mm 0mm 0mm;}')
            newwin.document.write('</style>')
        }else{
            newwin.document.write('<link rel="stylesheet" type="text/css" href="style/print.css"/>\n')
        }
        newwin.document.write('<script>\n')
        newwin.document.write('function chkstate(){\n')
        newwin.document.write('if(document.readyState=="complete"){\n')
        newwin.document.write('window.close()\n')
        newwin.document.write('}\n')
        newwin.document.write('else{\n')
        newwin.document.write('setTimeout("chkstate()",2000)\n')
        newwin.document.write('}\n')
        newwin.document.write('}\n')
        newwin.document.write('function print_win(){\n')
        newwin.document.write('window.print();\n')
        newwin.document.write('chkstate();\n')
        newwin.document.write('}\n')
        newwin.document.write('</script>\n')
        newwin.document.write('</HEAD>\n')
        newwin.document.write('<BODY onload="print_win()">\n')
        if(chequeDetailObject.isnewlayout){
            newwin.document.write('<div style="position: relative;">')
        }
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetailObject.dateLeft +'cm; top:'+chequeDetailObject.dateTop+'cm;">'+chequeDetailObject.date+'</div>')
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetailObject.nameLeft +'cm; top:'+chequeDetailObject.nameTop+'cm;">'+chequeDetailObject.accountName+'</div>')
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetailObject.amtinwordLeft +'cm; top:'+chequeDetailObject.amtinwordTop+'cm;">'+chequeDetailObject.amountinword+'</div>')
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetailObject.amtinwordLeftLine2 +'cm; top:'+chequeDetailObject.amtinwordTopLine2+'cm;">'+chequeDetailObject.amountinword1+'</div>')
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetailObject.amtLeft +'cm; top:'+chequeDetailObject.amtTop+'cm;">'+chequeDetailObject.amount+'</div>')
         /*
         * if Other Field option true the this Field will show on check.
         */
        if (chequeDetailObject.activateExtraFields !== undefined && chequeDetailObject.activateExtraFields) {
            newwin.document.write('<div style="position: absolute; left:' + chequeDetailObject.memoLeft + 'cm; top:' + chequeDetailObject.memoTop + 'cm;">' + chequeDetailObject.memo + '</div>')
            newwin.document.write('<div style="position: absolute; left:' + chequeDetailObject.addressLine1Left + 'cm; top:' + chequeDetailObject.addressLine1Top + 'cm;">' + chequeDetailObject.addressLine1 + '</div>')
            newwin.document.write('<div style="position: absolute; left:' + chequeDetailObject.addressLine2Left + 'cm; top:' + chequeDetailObject.addressLine2Top + 'cm;">' + chequeDetailObject.addressLine2 + '</div>')
            newwin.document.write('<div style="position: absolute; left:' + chequeDetailObject.addressLine3Left + 'cm; top:' + chequeDetailObject.addressLine3Top + 'cm;">' + chequeDetailObject.addressLine3 + '</div>')
            newwin.document.write('<div style="position: absolute; left:' + chequeDetailObject.addressLine4Left + 'cm; top:' + chequeDetailObject.addressLine4Top + 'cm;">' + chequeDetailObject.addressLine4 + '</div>')
        }
        if(chequeDetailObject.isnewlayout){
            newwin.document.write('</div>')
        }
        newwin.document.write('</BODY>\n')
        newwin.document.write('</HTML>\n')
        newwin.document.close()
    },
    
    getParametersForLifoFifo : function(){              // To avoid repeatation of same code for all 4 Lifo Fifo buttons.
        var params={
            accid:this.Name.getValue(),                    
            amount: this.Amount.getValue(),
            isLifoFifo:true,
            deleted:false,
            nondeleted:true,
            currencyfilterfortrans: this.Currency.getValue(),
            onlyAmountDue:true,
            isEdit:this.isEdit,
            isCopy:this.isCopyReceipt,
            billId:(this.record!= undefined)?this.record.data.billid:'',
            includeFixedAssetInvoicesFlag:true,
            filterForClaimedDateForPayment : true,
            upperLimitDate:WtfGlobal.convertToGenericDate(this.creationDate.getValue()),
            requestModuleid: Wtf.Acc_Receive_Payment_ModuleId
        }
        if(!this.isMultiCurrencyPayment()){
            params['isReceipt']=true
        }
        return params;
    },
    resetLifoFifoButtons: function(){
        if(this.LifoFifoMode) {
            for(var i=0;i<this.toggleBtnPanel.items.items.length;i++){
                this.toggleBtnPanel.items.items[i].toggle(false);
            }
            this.toggleButtonRelease();
        }
    },
    validateFormForLifoFifoMode: function(btn){         // To avoid repeatation of same code for all 4 Lifo Fifo buttons.
        if(this.Name.getValue()==''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), this.isCustomer?WtfGlobal.getLocaleText("acc.mp.selectCustFirst"):WtfGlobal.getLocaleText("acc.mp.selectVenFirst")], 2);
                btn.toggle(false);
                return false;
            }
            if(this.Amount.getValue()==0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.enterAmtFirst")], 2);
                btn.toggle(false);
                return false;
            }
            if(this.pmtMethod.getValue()==''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.selectPmtMethodFirst")], 2);// 'Select Payment method first''
                 btn.toggle(false);
                return false;
            }
            return true;
    },
    toggleButtonRelease: function(){                  // To avoid repeatation of same code for all 4 Lifo Fifo buttons.
        this.clearStore();
        this.grid.addBlankRow();
        this.LifoFifoMode=false;
        this.updateSouthTemp();
        this.updateBalanceAmount();
        if (!(WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied())) {
            this.tagsFieldset.resetCustomComponents();
        }
        var customer = this.Name.getValue();
        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
        var fetchdataid = Wtf.Acc_Receive_Payment_ModuleId
        this.tagsFieldset.setValuesForCustomer(moduleid, customer, fetchdataid);
    },
        
    getSequenceFormatCombo: function() {
//        this.sequenceFormatStore = Wtf.ReceiptSeqFormatStore;
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, Wtf.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: 'autoreceipt',
                isEdit: this.isCopyReceipt ? false : this.isEdit
            }
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatCombobox = new Wtf.form.FnComboBox({
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            valueField: 'id',
            id:"sequenceformat"+this.helpmodeid+this.id,
            displayField: 'value',
            store: this.sequenceFormatStore,
            disabled: (this.isEdit && !this.isCopyReceipt? true : false),
            anchor: '85%',
            typeAhead: true,
            forceSelection: true,
            allowBlank: false,
            name: 'sequenceformat',
            hiddenName: 'sequenceformat',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectSequenceFromat"),
            listeners: {
                'select': {
                    fn: this.getNextSequenceNumber,
                    scope: this
                }
            }

        });
        
    },
    refreshReportGrid: function(){
        var comp = null;
        comp = Wtf.getCmp('receiptReport');
        if(comp){
            comp.fireEvent('paymentupdate');
        }  
    },
    createRecordForEmail: function(response,request){
        var paymentRecord = {
            data:
            {    
                billno:response.billno,
                templateflag:Wtf.templateflag,  //Send templateflag from Entry Form email button
                billid:response.paymentid,    //Send billid from Entry Form email button
                billingEmail:response.billingEmail,
                billdate:this.creationDate.getValue(),
                personname:this.Name?this.Name.getRawValue():this.grid.getGLAccountNames()
            }
        }
        return paymentRecord;
    },
    onGridDataChanged: function(){
        this.updateAmount(getRoundedAmountValue(this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount()));
        this.updateBalanceAmount();
        this.updateSouthTemp();
    },
    onPaymentMethodChange: function(comboboxObject,newValue,oldValue){
        if(this.isEdit){
            /* ERP-16095
                 * Flag isBankToBankMethodChange will be true if user changes One payment method to another and both are of bank type.
                 * This flag will be undefined in created new case. And either true or false in copy and edit case. 
                 */
            var newrec = WtfGlobal.searchRecord(this.pmtStore, newValue, "methodid");
            var newCurrency=newrec.data.acccurrency;
            var newMethodType=newrec.data.detailtype;
            
            var oldrec = WtfGlobal.searchRecord(this.pmtStore, oldValue, "methodid");
            var oldCurrency=oldrec.data.acccurrency;
            var oldMethodType = oldrec.data.detailtype;
            
            if(this.isEdit ){
                var isBankToBankMethodChange=false;
                if(newMethodType == Wtf.bank_detail_type && newMethodType==oldMethodType){
                    isBankToBankMethodChange=true;
                }
                if(newCurrency==oldCurrency){
                    this.ShowCheckDetails(comboboxObject, newrec,isBankToBankMethodChange);
                }
            }else{
                this.ShowCheckDetails(comboboxObject, newrec);
                if(newCurrency!=oldCurrency){
                    this.reloadGridOnCurrencyChange();
                    this.updateSouthTemp();
                }
            }
        } else {
            this.getConfirmationToChangePmtMethod(comboboxObject,newValue,oldValue);
        }
    },
    reloadGridOnCurrencyChange:function(){
        Wtf.Ajax.requestEx({
            url: "ACCReceiptNew/reloadGridOnCurrencyChange.do",
            params: {
                newcurrency:this.Currency.getValue(),
                paymentwindowtype:this.record.data.paymentwindowtype,
                billid:this.record.data.billid,
                date:WtfGlobal.convertToGenericDate(this.creationDate.getValue()),
                isCopyTransaction:this.isCopyReceipt,    // Used to load the data conditionaly
            }
        },this,this.genSuccessResponsereOnCurrencyChange,this.genFailureResponseOnCurrencyChange);
    },
    genSuccessResponsereOnCurrencyChange:function(response,request){
        if(response.success){
            if(response.exchangeRateFound){
                this.loadLineDetails(response.Details);
                this.assignDiscountAfterCalculation();
                if (this.isCopyReceipt){
                    this.checkForValidLineLevelData(true);
                    this.updateAmount(getRoundedAmountValue(this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount()));
                }
            } else if(!response.exchangeRateFound){
                this.addExchangeRate();
                return;
            }   
            this.onGridDataChanged();   // Purpose of calling this function is only that updating new amount in 'Amount' field,Balance amount and south temp form after reloading grid
            if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
                var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.creationDate.getValue()).clearTime());
                if (!isTaxShouldBeEnable) {
                    this.grid.getStore().removeAll();
                    this.grid.addBlankRow();
                }
            }
        } else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
            return;
        }
    },
    genFailureResponseOnCurrencyChange:function(response,request){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    getPersonInformation: function(){
        var record = WtfGlobal.searchRecord(this.personstore, this.Name.getValue(), "accid");
        var vendorId = "";
        var customerId = "";
        vendorId = record.data['accid'];
        customerId = record.data['accid'];
        var parameters = {
            vendorId: vendorId,
            customerId: customerId,
            currencyid: this.Currency.getValue(),
            accid: record.data['accid'],
            personName: record.data.accname,
            personCode: (record.data.acccode)?record.data.acccode:this.presonCode,
            upperLimitDate: WtfGlobal.convertToGenericDate(this.creationDate.getValue())
        }
        return parameters;
    },
    onDateChange:function(a,val,oldval){
        if (this.isIndiaGST) {
            this.applyGSTFieldsBasedOnDate();
        }
        if (this.pmtMethod != undefined && this.Currency != undefined) {
            var rec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
            if (rec != null && rec != undefined && rec.data != undefined && rec.data.acccurrency != undefined) {
                if (this.Currency.getValue() != (rec.data.acccurrency)) {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.het.49"), WtfGlobal.getLocaleText("acc.mp.dataWillBeClearedOnDateChange"), function (btn) {
                        if (btn != "yes") {
                            this.creationDate.setValue(oldval);
                        } else {
                            this.clearStore();
                            this.grid.addBlankRow();
                            this.dateChange(a, val, oldval);
                        }
                    }, this);
                    return;
                }else{
                    this.dateChange(a, val, oldval);
                }
            }
        }
        else
        {
            this.dateChange(a, val, oldval);
        }
    },
        
    dateChange:function(a,val,oldval){
     
        var isDateValid= false;
        if(this.getNewestTransactionDate(val,oldval)){
            /**
             * If date is valid than only we have to calculate and assign discount. ERM-981
             */
            isDateValid = true;
            if(this.Currency.getValue()==WtfGlobal.getCurrencyID() && this.isEdit){ //when tranaction in base currency for all cases (edit,copy, create new)
                this.reloadGridOnCurrencyChange();
                this.changeCurrencyStore(a,val,oldval);
            } else if((this.isEdit && !this.isCopyReceipt) && Wtf.account.companyAccountPref.retainExchangeRate){ //edit case: when user want to retain exchange rate        
                this.assignDiscountAfterCalculation();
                return;                                       
            } else if(this.isEdit || this.isCopyReceipt) { //1.Edit case when user do not want to retain exchange rate 2.copy case
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.invoice.exchangeRateMsg"),function(btn){
                    if(btn=="yes"){
                        this.reloadGridOnCurrencyChange();
                        this.changeCurrencyStore(a,val,oldval);
                    } else{
                        this.creationDate.setValue(oldval);
                        this.setChequeDateAccordingly();
                        this.assignDiscountAfterCalculation();
                        return;
                    }
                },this);
            } else { //Normal Create New Case           
                 this.changeCurrencyStore(a,val,oldval);    
            }
            this.setChequeDateAccordingly();
        }
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
            var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.creationDate.getValue()).clearTime());
            if (!isTaxShouldBeEnable) {
                this.grid.getStore().removeAll();
                this.grid.addBlankRow();
            }
        }
        if (isDateValid) {
            this.assignDiscountAfterCalculation();
        }
    },
    assignDiscountAfterCalculation: function () {
        if (CompanyPreferenceChecks.discountOnPaymentTerms()) {
            var gridStore = this.grid != undefined ? this.grid.getStore() : "";
            var discount = 0.0;
            if (gridStore != undefined && gridStore != "") {
                var length = gridStore.data.items.length
                for (var cnt = 0; cnt < length; cnt++) {
                    var amount=gridStore.data.items[cnt].data.amount;
                    var amountDueOriginal=gridStore.data.items[cnt].data.amountDueOriginal;
                    if (amount == amountDueOriginal) {
                        discount = this.grid.calculateDiscount(gridStore.data.items[cnt].data);
                    }
//                    gridStore.data.items[cnt].data.discountname = discount;
                    var exchangeRateForTransaction=gridStore.data.items[cnt].data.exchangeratefortransaction;
                    var amountDueOriginal=gridStore.data.items[cnt].data.amountDueOriginal;
                    var amountDueOriginalWithExchangeRate=getRoundedAmountValue(amountDueOriginal*exchangeRateForTransaction);
                    if (discount > amountDueOriginalWithExchangeRate) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.receiptpayment.greaterdiscounterrormessage")], 1);
                        gridStore.data.items[cnt].data.discountname = amountDueOriginalWithExchangeRate;
                        gridStore.data.items[cnt].data.amountdueafterdiscount = getRoundedAmountValue((gridStore.data.items[cnt].data.amount*exchangeRateForTransaction)-gridStore.data.items[cnt].data.discountname);
                    }else {
                        gridStore.data.items[cnt].data.discountname = discount;
                        if (discount == 0) {
                            gridStore.data.items[cnt].data.amountdueafterdiscount = getRoundedAmountValue(gridStore.data.items[cnt].data.amountdue);
                        } else {
                            gridStore.data.items[cnt].data.amountdueafterdiscount = getRoundedAmountValue(gridStore.data.items[cnt].data.amountdue - discount);
                        }
                    }
                    gridStore.data.items[cnt].data.enteramount = gridStore.data.items[cnt].data.amountdueafterdiscount;
                }
                this.onGridDataChanged();   // Purpose of calling this function is only that updating new amount in 'Amount' field,Balance amount and south temp form after reloading grid
            }
            this.grid.getView().refresh();
        }
    },
    addExchangeRate:function(){
         var panel = callCurrencyExchangeWindow();
         panel.on("update",function() {
             this.reloadGridOnCurrencyChange();
         }, this);
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.10")], 2);
    },
    enableSaveButton: function(){
        this.saveBttn.enable();
        this.savencreateBttn.enable();
    },
     getNewestTransactionDate: function(val,oldval){
        var enteredDate = val;
        var valid=true;
        for(var i=0;i<this.grid.store.getCount();i++){
            var rec= this.grid.store.getAt(i);
            if(rec.data.type==2 || rec.data.type==3){
                var documentDate=new Date(rec.data.date).setHours(0,0,0,0);
                var isOpeningBalanceTransaction = ((typeof rec.data.isOpeningBalanceTransaction === "string") ? ((rec.data.isOpeningBalanceTransaction == "true") ? true : false) : rec.data.isOpeningBalanceTransaction);
//                var isOpeningBalanceTransaction = (rec.data.isOpeningBalanceTransaction == "true") ? true : false
                if((enteredDate.getTime()<documentDate) && (!isOpeningBalanceTransaction)){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlder")], 2);
                    this.creationDate.setValue(oldval);
                    valid=false;
                    break;
                }
            }
            /**
             * If isPostingDateCheck is true then checking wether enteredDate is less than jePostingDate if yes displaying "Date cannot be older than the JE posting date of transactions loaded in grid"
             */
            if (CompanyPreferenceChecks.isPostingDateCheck()) {
                var jePostingDate = getTimeEcludingBrowsertimezone(rec.data.jeDate);
                if (rec.data.type == 2 || rec.data.type == 3) {
                    if ((enteredDate.getTime() < jePostingDate) && (!isOpeningBalanceTransaction)) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlderThenJEPosting")], 2);
                        this.creationDate.setValue(oldval);
                        valid = false;
                        break;
                    }
                }
            }
            /*
             * For Malaysian country, payment date can not be older than claim date of any invoice that loaded in grid
             */
            if(rec.data.type==2 && Wtf.account.companyAccountPref.countryid==Wtf.Country.MALAYSIA){   
                if((rec.data.claimedDate != undefined) && (rec.data.claimedDate!='') && (enteredDate.getTime()<new Date(rec.data.claimedDate).getTime())){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlderThanClaimeddate")], 2);
                    this.creationDate.setValue(oldval);
                    valid=false;
                    break;
                }
            }
        }  
        return valid;
    },
    changeCurrencyStore:function(a,val,oldval){
        this.currencyStore.load({
            params:{
                mode:201,
                transactiondate:WtfGlobal.convertToGenericDate(this.creationDate.getValue())
            }
        });
        this.currencyStore.on('load',this.changeTemplateSymbol.createDelegate(this,[oldval]),this);
    },
    changeTemplateSymbol:function(oldval){
        if(this.currencyStore.getCount()==0){
            this.currencyStore.purgeListeners();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.10")], 2);
            this.creationDate.setValue(oldval);
            this.changeCurrencyStore();
        }
        else{
            var index = this.getCurrencySymbol();
            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
            this.externalcurrencyrate = exchangeRate;
            this.applyTemplate(this.currencyStore, index);
            this.updateSouthTemp();
            this.checkForMultiCurrencyToDecideExchangeRate();
        } 
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
            var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.creationDate.getValue()).clearTime());
            if (!isTaxShouldBeEnable) {
                this.grid.getStore().removeAll();
                this.grid.addBlankRow();
            }
        }
    },
    setSequenceFormatForCreateNewCase:function(){
        var count = this.sequenceFormatStore.getCount();
        for (var i = 0; i < count; i++) {
            var seqRec = this.sequenceFormatStore.getAt(i)
            if (seqRec.json.isdefaultformat == "Yes") {
                this.sequenceFormatCombobox.setValue(seqRec.data.id)
                break;
            }

        }
        if(this.sequenceFormatCombobox.getValue()){
            this.getNextSequenceNumber(this.sequenceFormatCombobox); 
        } else{
            this.No.setValue("");
            this.No.allowBlank=true;
            WtfGlobal.hideFormElement(this.No);
        }
    },
    onCurrencyChange: function(comboboxObject,val,oldValue){
     Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.het.804"),WtfGlobal.getLocaleText("acc.mp.dataWillBeClearedOnCurrencyChange"),function(btn){
            if(Wtf.account.companyAccountPref.activateToDateforExchangeRates){
                var rec = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
                if(rec!=null && rec!=undefined){
                    checkForNearestExchangeRate(this,rec,this.creationDate.getValue());
                }
            }
            if(btn!="yes") {               
                comboboxObject.setValue(oldValue);
                return;
            } else {
                this.clearStore();
                this.updateExternalCurrencyRateOnCurrencyChange();
                var pmtMethodRecord = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
                var currencyForSelectedPaymentMethod= pmtMethodRecord.data['acccurrency'];
                this.grid.filterDocumentTypeStore(currencyForSelectedPaymentMethod,this.Currency.getValue());
                this.Amount.setValue(0);
                this.updateSouthTemp();
                this.updateTemplateCurr();
                this.updateBalanceAmount();
                this.checkForMultiCurrencyToDecideExchangeRate();                                
    }
        },this);  
    },
    updateExternalCurrencyRateOnCurrencyChange : function(){
        var currencyRecord = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
        this.externalcurrencyrate = currencyRecord.data['exchangerate'];
    },
    onPaymentMethodBeforeSelect :function ( combo, record,index ) {
        if(this.isEdit ){
            var currencyForSelectedRecord= record.data['acccurrency'];
            var paymentMethodBeforeSelect = WtfGlobal.searchRecord(this.pmtStore, combo.getValue(), "methodid");
            var paymentMethodCurrencyBeforeSelect = paymentMethodBeforeSelect.data['acccurrency'];
            if(currencyForSelectedRecord == paymentMethodCurrencyBeforeSelect){
                return true;
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.paymentmethodchangemessage")], 2);
                return false;
            }
        }else{
            this.receiptCurrencyBeforeSelect = this.Currency.getValue();
        }
    },
    onPaymentMethodSelect: function(combobox,rec,index){
     this.updateTemplateCurr();
    /*if(!this.isEdit){
        var currencyForSelectedRecord= rec.data['acccurrency'];
        this.Currency.setValue(currencyForSelectedRecord);
        this.grid.filterDocumentTypeStore(this.Currency.getValue(),currencyForSelectedRecord);
        this.updateExternalCurrencyRateOnCurrencyChange();
        this.updateSouthTemp();
            this.updateBalanceAmount(); 
        this.checkForMultiCurrencyToDecideExchangeRate();
    }*/
    if((WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.viewBankTypeAccountBalance))
            ||(WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.viewCashTypeAccountBalance))){
            this.hideShowAccountBalance();
        }
    },
    setPaymentCurrencyToPaymentMethodCurrencyRate : function(){
       if(this.pmtMethod.getValue()!=''){
                
                var pmtMethodRecord = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
                var currencyForSelectedPaymentMethod= pmtMethodRecord.data['acccurrency'];
                var currencyRecordOfPaymentMethod = WtfGlobal.searchRecord(this.currencyStore, currencyForSelectedPaymentMethod, "currencyid");
                var exchangeRatePMTMethodCurrencyToBase = currencyRecordOfPaymentMethod.data['exchangerate'];
                
                var currencyRecordOfSelectedCurrency = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
                var exchangeRateSelectedCurrencyToBase = currencyRecordOfSelectedCurrency.data['exchangerate'];
                
                /*
                * When this function is called, system will check whether the sopt rate (Payment currency to base currency rate) is valid or not
                * If valid, exchangeRateSelectedCurrencyToBase will be calculated from this spot rate.
                * If it is undefined (the case when form is loaded initially and payment currecny is same as base currency), system will take the rate applied on payment date
                */
                if(this.externalcurrencyrate!=undefined && this.externalcurrencyrate!=0){
                    exchangeRateSelectedCurrencyToBase=this.externalcurrencyrate;
                } 
                
                var paymentCurrencyToPaymentMethodCurrency= (exchangeRatePMTMethodCurrencyToBase)/(exchangeRateSelectedCurrencyToBase);
                
                this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.setValue(paymentCurrencyToPaymentMethodCurrency);
    }
   },
   isMultiCurrencyPayment : function(){
        if(this.pmtMethod.getValue()!=''){
            var pmtMethodRecord = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
            var currencyForSelectedPaymentMethod= pmtMethodRecord ? pmtMethodRecord.data['acccurrency'] : "";
            if(currencyForSelectedPaymentMethod!=this.Currency.getValue()){
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }   
    },
    checkForMultiCurrencyToDecideExchangeRate: function(){
       if(this.isMultiCurrencyPayment()){
            this.enableDisablePaymentCurrencyToPaymentMethodCurrencyRate();
            this.setPaymentCurrencyToPaymentMethodCurrencyRate();
        } else {
            this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.disable();
            this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.setValue(1);
        }
   },
   enableDisablePaymentCurrencyToPaymentMethodCurrencyRate: function(){
       var pmtMethodRecord = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
       var currencyForSelectedPaymentMethod= pmtMethodRecord ? pmtMethodRecord.data['acccurrency'] : "";
       
       if(currencyForSelectedPaymentMethod == WtfGlobal.getCurrencyID() || !(this.isMultiCurrencyPayment()) || this.readOnly){
           this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.disable();
       }else {
           this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.enable();
       }
   },   
   setChequeDateAccordingly: function(){
        if( !( this.isEdit && !this.isCopyReceipt) ){        //  If this is not 'Edit Case' but  this is 'Create New' or 'Copy Case'
                if(this.SouthForm && this.SouthForm.PostDate){                    
                        this.SouthForm.PostDate.setValue(this.creationDate.getValue());                    
                }
        }
   },
   isValidForGSTTypeOfAccounts: function(){        
        var valueToReturn = this.grid.isValidForGSTTypeOfAccounts();        
        return valueToReturn;        
    },
    getMonthWiseInvoices : function(){
    var isValid = false;
    isValid = this.isValidForMonthwiseInvoices();
    if(isValid){
        this.invoiceMonthwiseWindow = new Wtf.account.InvoiceMonthWiseWindow({
            id: 'invoicemonthwisewindow',
            title: this.isCustomer?WtfGlobal.getLocaleText("acc.lp.customerinvoiceapprovelevelone"):WtfGlobal.getLocaleText("acc.lp.vendorinvoiceapprovelevelone"),
            border: false,
            isReceipt: true,
            isCustomer: this.isCustomer,
            currencyfilterfortrans: this.Currency.getValue(),
            personInfo:this.getPersonInformation(),
            isEdit:this.isEdit,
            billid:(!Wtf.isEmpty(this.record) && !Wtf.isEmpty(this.record.data)) ? this.record.data.billid : '',     //ERP-41122
            isMulticurrency:this.isMultiCurrencyPayment(),
            parentObject:this
        });
        this.invoiceMonthwiseWindow.on('beforeclose', function(winObj) {
            if(winObj.isSubmitBtnClicked) {
                winObj.getSelectedMonthsInvoices();
            }
        }, this);
        this.invoiceMonthwiseWindow.show();
    }
  },
  isValidForMonthwiseInvoices : function(){
    if(this.Name.getValue()==''){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), this.isCustomer?WtfGlobal.getLocaleText("acc.mp.selectCustFirst"):WtfGlobal.getLocaleText("acc.mp.selectVenFirst")], 2);
        return false;
    }
    if(this.pmtMethod.getValue()==''){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.selectPmtMethodFirst")], 2);
        return false;
    }
    return true;
  },
  validateForFullPayment:function (){
        var returnObj={};
//        var store = this.Name.store;
        var record = WtfGlobal.searchRecord(this.personstore, this.Name.getValue(), "accid");
        var isInvalid = false;
        var invalidInvoices='';
        var invalidDNs='';
        for(var x=0;x<this.grid.getStore().getCount();x++){
            var gridRecord = this.grid.getStore().getAt(x);
            if((gridRecord.data['type'] == this.grid.INVType) && (gridRecord.data['amountdue']!=gridRecord.data['enteramount'])){
                invalidInvoices+=gridRecord.data['documentno']+',';
                isInvalid = true;
            } else if ((gridRecord.data['type'] == this.grid.NoteType) && (gridRecord.data['amountdue']!=gridRecord.data['enteramount'])){
                invalidDNs+=gridRecord.data['documentno']+',';
                isInvalid = true;
            }
        }
        if(isInvalid){
            var msg='';
            var name = record.data['accname'];
            msg+=WtfGlobal.getLocaleText("acc.rp.warningForFullPaymentRequired")+' '+name+'. '+WtfGlobal.getLocaleText("acc.rp.warningForFullPaymentEditTransactionAmounts");
            if(invalidInvoices!=''){
                invalidInvoices = invalidInvoices.substring(0, invalidInvoices.length-1);
                invalidInvoices = '<b>'+WtfGlobal.getLocaleText("acc.lp.customerinvoiceapprovelevelone")+':'+'</b>'+' '+invalidInvoices;
                msg+='<br>'+invalidInvoices;
            }
            if(invalidDNs!=''){
                invalidDNs = invalidDNs.substring(0, invalidDNs.length-1);
                invalidDNs = '<b>'+WtfGlobal.getLocaleText("acc.rp.debitNotes")+':'+'</b>'+' '+invalidDNs;
                msg+='<br>'+invalidDNs;
            }
            returnObj['msg']=msg;
        }
        returnObj['isInvalid']=isInvalid;
        return returnObj;
    },
    checkForValidLineLevelData:function(isCallFromReloadGridOnCurrencyChange){
      this.grid.checkForValidLineLevelData(isCallFromReloadGridOnCurrencyChange);
  },
  hideShowAccountBalance:function(){
    var pmtMethodeValue = this.pmtMethod.getValue();
    var methodAccindx=this.pmtStore.findBy(function(rec){
        if(rec.data.methodid==pmtMethodeValue)
            return true;
        else
            return false;
    });
    if (methodAccindx != -1) {
        var pmtRec=this.pmtStore.getAt(methodAccindx); 
        
        if ((pmtRec.data.detailtype !=undefined || pmtRec.data.detailtype!="")){
            if ((pmtRec.data.detailtype == 2 && !WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.viewBankTypeAccountBalance))
                ||(pmtRec.data.detailtype == 0 && !WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.viewCashTypeAccountBalance))){
                this.bankBalanceTpl.show();
            }else{
                this.bankBalanceTpl.hide();
            }
        }
    }
},
onAmountFieldBlur:function(obj){
    if(obj.getValue().toString() == "NaN" || obj.getValue() == '' || obj.getValue() == undefined || obj.getValue()== null){
        obj.setValue(0);
    }
    this.updateBalanceAmount();
},
deleteSelectedRecord: function() {
    var arr = [];
    var store = this.grid.getStore();
    var selectedCount = this.grid.selectionModel.getCount();
    var index=0;
    var rowindex=0;
    var message = "";
    if(this.isEdit){
         message += "</b> Selected record will be Removed. </br>" + WtfGlobal.getLocaleText("acc.nee.48")
         Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), message, function(btn) {
            if (btn != "yes")
                return;            
            for (rowindex = 0; rowindex < selectedCount; rowindex++) {
                arr[rowindex] = this.grid.selectionModel.getSelections()[index];
                if (arr[rowindex] != "") {
                    var id = arr[rowindex].id;
                    store.remove(store.getById(id));
                }
            }
            this.grid.getView().refresh(); //ERP-36023 sequence no. of documents on line level grid not updating
            this.grid.fireEvent('datachanged', this);
        },this);
    }
    
    else{
        for (rowindex = 0; rowindex < selectedCount; rowindex++) {
                arr[rowindex] = this.grid.selectionModel.getSelections()[index];
                if (arr[rowindex] != "") {
                    var id = arr[rowindex].id;
                    store.remove(store.getById(id));
                }
            }
            this.grid.getView().refresh(); //ERP-36023 sequence no. of documents on line level grid not updating
            this.grid.fireEvent('datachanged', this);
    }
    },
    
    saveGridStateHandler: function () {
        WtfGlobal.saveGridStateHandler(this, this.grid, this.grid.getState(), this.moduleid + "_" + this.paymentType, this.grid.gridConfigId, true);
    },
    loadPersonStore: function (personid) {
        var params = {};
        if (this.isCustomer) {
            params.selectedCustomerIds = personid;
        } else {
            params.vendorid = personid;
        }

        this.personstore.load({
            params: params,
            scope: this,
            callback: this.onPersonStoreLoad
        });
    }

});
