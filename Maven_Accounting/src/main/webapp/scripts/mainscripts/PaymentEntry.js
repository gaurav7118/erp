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

// editReceiptExchangeRates() function used for Payment and Receipt side. 
function editReceiptExchangeRates(winid,basecurrency,foreigncurrency,exchangerate,exchangeratetype){
    function showReceiptExternalExchangeRate(btn,txt){
        if(btn == 'ok'){
             if(txt.indexOf('.')!=-1)
                 var decLength=(txt.substring(txt.indexOf('.'),txt.length-1)).length;
            if(isNaN(txt)||txt.length>15||decLength>7||txt==0){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.setupWizard.curEx"), //'Exchange Rate',
                    msg: WtfGlobal.getLocaleText("acc.nee.55")+  //"You have entered an incorrect exchange rate. Please note:"+
                    "<br>"+WtfGlobal.getLocaleText("acc.nee.56")+
                    "<br>"+WtfGlobal.getLocaleText("acc.nee.57"),
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this,
                    fn: function(){
                        if(btn=="ok"){
                            editReceiptExchangeRates(winid,basecurrency,foreigncurrency,exchangerate,exchangeratetype);
                        }
                    }
                });
            } else {
                if(exchangeratetype!=undefined)
                    Wtf.getCmp(winid).exchangeratetype=exchangeratetype
                if(exchangeratetype!=undefined&&exchangeratetype=='foreigntobase'){
                    if((txt*1)>0) {
                        Wtf.getCmp(winid).revexternalcurrencyrate=txt;
                        var exchangeRateNormal = 1/((txt*1)-0);
                        exchangeRateNormal = (Math.round(exchangeRateNormal*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
                        Wtf.getCmp(winid).externalcurrencyrate=exchangeRateNormal;
                    } 
                }else{
                    Wtf.getCmp(winid).externalcurrencyrate=txt;
                }
//                Wtf.getCmp(winid).applyCurrencySymbol();
                Wtf.getCmp(winid).updateSouthTemp();
                Wtf.getCmp(winid).checkForMultiCurrencyToDecideExchangeRate();
            }
        }
    }
    Wtf.MessageBox.prompt(WtfGlobal.getLocaleText("acc.setupWizard.curEx"),'<b>'+WtfGlobal.getLocaleText("acc.nee.58")+'</b>'+WtfGlobal.getLocaleText("acc.field.1")+basecurrency+' = '+exchangerate+' '+foreigncurrency +
        '<br><b>'+WtfGlobal.getLocaleText("acc.nee.59")+'</b>', showReceiptExternalExchangeRate);
}

Wtf.account.PaymentEntry = function(config) {
    this.isReceipt = config.isReceipt;
    this.moduleid = config.moduleId;
    this.isWarnConfirm = false;
    this.modeName = config.modeName;
    this.amountDisable = config.amountDisable;
    this.helpmodeid = config.helpmodeid;
    this.isLinkedToClaimedInvoice = false;
    this.isEdit = config.isEdit;
    this.readOnly = config.readOnly;
    this.record = config.record;
    this.id = config.id;
     // map default payment method to customer check
    this.mapDefaultPmtMethod = CompanyPreferenceChecks.mapDefaultPaymentMethod();
    //For Create New case of TDS Payment(For India Country).
    this.tdsPaymentJsonFlag = (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && config.taxPaymentDataParams != null && config.taxPaymentDataParams != undefined && config.taxPaymentDataParams != "" )? config.taxPaymentDataParams.tdsPaymentJsonFlag : false;
    /*
     * isbulkpayment is true if click on bulkpayment from invoice report
     */
    this.isGST = WtfGlobal.isIndiaCountryAndGSTApplied();   //ERP-32829 
    this.CustomerVendorTypeId="";
    this.GSTINRegistrationTypeId="";
    this.gstin="";
    this.gstdochistoryid="";
    this.ignoreHistory=false;
    this.isRCMApplicableInPreferences = Wtf.account.companyAccountPref.RCMApplicable != undefined ? Wtf.account.companyAccountPref.RCMApplicable : false;
    this.purchaseFromURD = false;
    this.isBulkPayment=(config.isBulkPayment == null || config.isBulkPayment == undefined)? false : config.isBulkPayment;
    this.invObj=config.invObj;
    this.uPermType=(this.isReceipt?Wtf.UPerm.salesreceivepayment:Wtf.UPerm.purchasemakepayment);
    this.permType=(this.isReceipt?Wtf.Perm.salesreceivepayment:Wtf.Perm.purchasemakepayment);
    this.exportPermType=(this.isReceipt?this.permType.exportdatareceipt:this.permType.exportdatapayment);
    this.printPermType=(this.isReceipt?this.permType.printreceipt:this.permType.printpayment);
    this.emailPermType=(this.isReceipt?this.permType.emailreceipt:this.permType.emailpayment);
    this.paymentType = config.paymentType;
    this.currencyid = null;
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
    /*
     * About flag this.isIBGTransactionCheckedInEditCase:
     * this flag is used for those payments which are having payment method of IBG type. Usable in EDIT and COPY case. Inititally it is set to false.
     * Suppose some payment has IBG type payment method but transaction is not set IBG type by user. When user edit/copy such payment, before saving the payment, system should ask user whether he wants to save this edited payment
     * or copied payment as IBG activated payment. For this, system will use this flag for first time to check for payment method type from payment method record and not from flag 'this.isIBGTypeTransaction'
     * which is used in create new case.
     * Once user gives the response to system means set the transaction as either IBG type or Non IBG type, flag 'this.isIBGTransactionCheckedInEditCase' will set to true and further flag 'this.isIBGTypeTransaction' will be used.
     * More details could be found in 'ERP-20172'
     */
    this.isIBGTransactionCheckedInEditCase=false;
    this.remainAtSameLevel=false;
    /*
     *Check for Warning message If balance amount is Greater than zero
     */
    this.checkWarning=true;
    this.isCopyReceipt = config.isCopyReceipt;
    this.isAllowedSpecificFields=(config.isAllowedSpecificFields == null || config.isAllowedSpecificFields == undefined)? false : config.isAllowedSpecificFields;
    this.pendingApproval=(config.ispendingAproval == null || config.ispendingAproval == undefined)? false : config.ispendingAproval;
    this.isIBGTypeTransaction = false; // this value will changed on Payment Method. If payment method is of IBG type then value will set to true
    this.LifoFifoMode=false;         // Will be true when Lifo Fifo Invoices are loaded    
    this.paymentCurrencyBeforeSelect="";//used to refresh grid on payment method change.
    this.basicExemptionApplied=false;//used to refresh grid on payment method change.
    this.basicExemptionExceed=false;//used to refresh grid on payment method change.
    this.basicExemptionPerTransaction=0;//used to refresh grid on payment method change.
    this.basicExemptionPerAnnual=0;
    this.debitbalance = 0;
    this.creditbalance = 0;
    this.totalbalance = 0;
    this.paymentOption = {
        AgainstVendor: 1,
        AgainstCustomer: 2,
        AgainstGL: 3
    }
    this.paymentMethodType = {
        Cash: 0,
        Card: 1,
        Bank: 2
    }
    this.isCustomer = (this.record)?(this.record.data.paymentwindowtype==this.paymentOption.AgainstCustomer):config.isCustomer;
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
        {name:'bankType'}
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
        {name: 'hasAccess'}, //SDP-13638
        {name: 'paymentCriteria'}
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
        id: "date" + config.heplmodeid + this.id,
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

    this.currencyStore.on("load", function(store) {
   //     this.setPMData();
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
           // this.Currency.setValue(this.bankAccCurrency);
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
        this.paymentCurrencyBeforeSelect = this.Currency.getValue();
    }, this);

     this.currencyStore.load({
            params: {
                grouper: 'paymentTrans',
                mode: 201,
                transactiondate: (this.isEdit && this.record!= undefined) ? WtfGlobal.convertToGenericDate(this.record.data.billdate) : WtfGlobal.convertToGenericDate(new Date())
            }
        });
    var currencyHelp = WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.SelectaCurrencytoprocesstransactionofrequiredcurrency"));

    this.Currency = new Wtf.form.FnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.mp.cur") + currencyHelp, //'Currency*',
        hiddenName: 'currencyid',
        name:'currencyid',
        id: "currency" + config.heplmodeid + this.id,
        anchor: '85%',
        allowBlank: false,
        store: this.currencyStore,
        disabled:this.isEdit,
        valueField: 'currencyid',
        forceSelection: true,
        displayField: 'currencyname',
        scope: this,
        selectOnFocus: true
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
        id: "paymentMethod" + config.hemodeid + this.id,
        valueField: 'methodid',
        displayField: 'methodname',
        allowBlank: false,
        disabled:   this.readOnly,
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
        id: "pmtmethodacc" + this.heplmodeid+this.id,
        fieldLabel: WtfGlobal.getLocaleText("acc.field.PaymentAccount"),
        anchor: '85%'
    });

    this.isChequePrint = false;
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.edit))
        this.pmtMethod.addNewFn = this.addPaymentMethod.createDelegate(this)
    this.pmtMethod.on('change', this.onPaymentMethodChange, this);
    this.pmtMethod.on('select', this.onPaymentMethodSelect, this); // Event is added for setting the currency of the payment method account again , after changing the currency from currency combobox
    this.pmtMethod.on('beforeselect', this.onPaymentMethodBeforeSelect, this);
    this.getSequenceFormatCombo();
    
    this.No = new Wtf.form.TextField({
        fieldLabel: config.isReceipt ? WtfGlobal.getLocaleText("acc.rp.RecNO") : WtfGlobal.getLocaleText("acc.mp.payNo"), //this.transectionName+' No*',
        id: "receiptNo" + config.heplmodeid + this.id,
        name: 'no',
        disabled: this.isEdit,
        anchor: '85%',
        maxLength: 45,
        allowBlank: false
    });
    this.bankCharges = new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.BankCharges"),
        id:"bankCharges"+this.heplmodeid+this.id,
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
        id:"bankInterest"+this.heplmodeid+this.id,
        name: 'bankInterest',
        hiddenName: 'bankInterest',
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
    this.MPPaidToRec = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'name'}
    ]);

    this.MPPaidToStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.MPPaidToRec),
        url: "ACCMaster/getMasterItems.do",
        baseParams: {
            mode: 112,
            groupid: 17
        }
    });
    this.bankChargesAccount = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.BankChargesAccount"),
        hiddenName: 'bankChargesCmb',
        store: this.accountStore,
        minChars: 1,
        anchor: '85%',
        id:'bankChangesAccount'+this.heplmodeid+this.id,
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
        id:'bankInterestAccount'+this.heplmodeid+this.id,
        valueField: 'accountid',
        displayField: 'accountname',
        isAccountCombo:true,
        name: 'bankInterestCmb',
        forceSelection: true,
        hirarchical: true,
        emptyText: WtfGlobal.getLocaleText("acc.mp.selectBankInterestAccount"),
        disabled: this.readOnly,
        extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'],
        mode: 'remote',
        extraComparisionField: 'acccode', // type ahead search on acccode as well.
        listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 500 : 400
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
    this.ibgTransactionCode = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.ibg.transmsg"),
        store: Wtf.ibgTransactionCodeStore,
        hiddenName: 'ibgCode',
        name:'ibgCode',
        displayField: 'ibgCode',
        id: 'ibgCode' + this.id,
        valueField: 'ibgCode',
        value: '20',
        mode: 'local',
        width: 50,
        listWidth: 50,
        hidden: this.isReceipt || !Wtf.account.companyAccountPref.activateIBG,
        hideLabel: this.isReceipt || !Wtf.account.companyAccountPref.activateIBG,
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true,
        disabled: true
    });

    this.paidTo = new Wtf.form.ExtFnComboBox({
        fieldLabel: this.isReceipt ? WtfGlobal.getLocaleText("acc.mp.receivedFrom") : WtfGlobal.getLocaleText("acc.mp.paidTo"), //'Received From':'Paid To'
        hiddenName: "paidToCmb",
        store: this.isReceipt ? Wtf.RPReceivedFromStore : this.MPPaidToStore,
        id:"paidto"+this.heplmodeid+this.id,
        valueField: 'id',
        displayField: 'name',
        allowBlank: true,
        disabled: this.readOnly,
        emptyText: this.isReceipt ? WtfGlobal.getLocaleText("acc.rp.selreceivedfrom") : WtfGlobal.getLocaleText("acc.mp.selpaidto"), //'Select Received From...':'Select Paid To...'
        minChars: 1,
        extraFields: '',
        listWidth: 500,
        extraComparisionField: 'name', // type ahead search on acccode as well.
        anchor: '85%',
        mode: 'remote',
        triggerAction: 'all',
        typeAhead: true,
        forceSelection: true,
        listeners: {
            'select': {
                fn: function() {
                    if (Wtf.account.companyAccountPref.activateIBG && this.isIBGTypeTransaction && this.paymentType == this.paymentOption.AgainstGL) {
                        var rec = WtfGlobal.searchRecord(this.MPPaidToStore, this.paidTo.getValue(), 'id');
                        this.callIBGDetailsGrid(rec, true, true);
                    }
                    if(this.paymentType == this.paymentOption.AgainstGL && this.paidTo.lastSelectionText){
                        this.Payee.setValue(this.paidTo.lastSelectionText);
                    }
                },
                scope: this
            },
            'change':{
              fn : function(){
                  this.ibgDetailsID="";   // Set blank when paid to is changed.
              },
              scope:this
            }
        }
    });
    this.MPPaidToStore.load();
    this.Payee = new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.payment.payee"),
        id:"payee"+this.heplmodeid+this.id,
        name: 'payee',
        anchor: '85%',
        maxLength: 100,
        disabled: this.readOnly
    });
    
    this.rcmApplicable = new Wtf.form.Checkbox({
        name: 'rcmApplicable',
        id: "rcmApplicable" + this.id,
        fieldLabel: "<span wtf:qtip=" + WtfGlobal.getLocaleText("acc.compref.india.rcm.applicable") + ">" + WtfGlobal.getLocaleText("acc.compref.india.rcm.applicable") + "</span>",
        checked: false,
        disabled: this.readOnly,
        hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !(this.isRCMApplicableInPreferences  && this.paymentOption.AgainstVendor === this.paymentType),
        hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !(this.isRCMApplicableInPreferences  && this.paymentOption.AgainstVendor === this.paymentType),
        cls: 'custcheckbox',
        width: 10
    });
    
    this.advanceToVendor = new Wtf.form.Checkbox({
        name: 'advanceToVendor',
        id: "advanceToVendor" + this.id,
        fieldLabel: "<span wtf:qtip=" + "Advance To Vendor" + ">" + "Advance To Vendor" + "</span>",
        checked: false,
        hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !(this.isRCMApplicableInPreferences  && this.paymentOption.AgainstGL === this.paymentType),
        hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !(this.isRCMApplicableInPreferences  && this.paymentOption.AgainstGL === this.paymentType),
        cls: 'custcheckbox',
        width: 10
    });
    
    this.rcmApplicable.on('change', this.onRCMApplicableChange, this);    
    
    this.ExciseUnitCombo = new Wtf.form.ExtFnComboBox({
        store: Wtf.FormUnitStore,
        typeAhead: true,
        selectOnFocus:true,
        valueField:'id',
        displayField:'name',
        extraComparisionField:'id', 
        extraFields:[],
        fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.paymentEntry.ExciseUnit") +"'>"+ WtfGlobal.getLocaleText("acc.paymentEntry.ExciseUnit")+"</span>",//" Excise Unit",
        hiddenName:"exciseunit",
        id:"exciseunit"+this.heplmodeid+this.id,
        mode: 'local',
        forceSelection: true,
        anchor: '85%',
        listWidth: 500,
        editable : true,
        triggerAction:'all',
        addNewFn:this.addTemplateUnit,
        hirarchical:true,
        scope:this,
        disabled: this.readOnly,
        hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isExciseApplicable && this.paymentType ==3), 
        hideLabel: !(Wtf.account.companyAccountPref.countryid ==Wtf.Country.INDIA && Wtf.isExciseApplicable && this.paymentType ==3),
        emptyText:WtfGlobal.getLocaleText("acc.field.pleaseselectcompanyunit")
    });
    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isExciseApplicable && this.paymentType == 3) {
        Wtf.FormUnitStore.load();
    }
    this.nonRefundable = new Wtf.form.Checkbox({
        name: 'NonRefundable',
        fieldLabel: WtfGlobal.getLocaleText("acc.cust.nonRefundable"),
        id: 'nonRefundable' + this.helpmodeid + this.id,
        checked: false,
        disabled: this.readOnly,
        hideLabel: (Wtf.account.companyAccountPref.countryid!='137')?true:(this.paymentType != this.paymentOption.AgainstVendor),
        hidden:  (Wtf.account.companyAccountPref.countryid!='137')?true:(this.paymentType != this.paymentOption.AgainstVendor),
        cls: 'custcheckbox',
        width: 10
    });
    
    
    this.paidTo.addNewFn = this.addPaidTo.createDelegate(this);
    this.Memo = new Wtf.form.TextArea({
        fieldLabel: Wtf.account.companyAccountPref.descriptionType, // 'Memo',
        name: 'memo',
        id:"memo"+this.heplmodeid+this.id,
        hiddenName: "memo",
        height: 40,
        anchor: '85%',
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
        disabled:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && ( this.amountDisable || (this.record && this.record.data.AllowToEditCopy!=undefined && !this.record.data.AllowToEditCopy)))?true:(this.readOnly),
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
        id: "paymentCurrencyToPaymentMethodCurrencyExchangeRate" + this.heplmodeid + this.id,
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
    
    this.CINNo = new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.CIN"),//'CIN',
        name: 'cinno',
        hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable ),
        hideLabel: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA || !Wtf.isTDSApplicable),
        anchor: '85%',
        maxLength: 45,
        disabled: this.readOnly,
        regex:/^[0-9]*$/, 
//        regexText:'Invalid PAN eg."AAAAA1234A"',
        allowBlank: true
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
                        items: [this.sequenceFormatCombobox, this.No, this.creationDate, this.paidTo,this.Payee,this.nonRefundable,this.ExciseUnitCombo,this.rcmApplicable,this.advanceToVendor]
                    }, {
                        layout: 'form',
                        columnWidth: 0.33,
                        id: this.id + 'MiddleColumnform',
                        items: [this.ShowOnlyOneTime, this.Currency, this.paymentCurrencyToPaymentMethodCurrencyExchangeRate, this.Amount, this.bankChargesAccount, this.bankCharges]
                    }, {
                        layout: 'form',
                        columnWidth: 0.33,
                        items: [this.pmtMethod, this.pmtMethodAcc, this.Memo, this.bankInterestAccount, this.bankInterest, this.ibgTransactionCode, this.CINNo]
                    }]
            }, this.tagsFieldset]
    });
    this.createGrid();
    
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
            
    var lineLevelArray = [];
    if(WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.OLDNEW){
        for(var i=0; i<Wtf.LineTermsMasterStore.getRange().length; i++){
            var temp = Wtf.LineTermsMasterStore.getRange()[i].data;
            temp['taxAmount'] = WtfGlobal.currencyRenderer(0);
            lineLevelArray.push(temp);
        }
    }
    
    this.LineLevelTermTpl=new Wtf.Panel({  
        border:false,
        width: '35%',
//        hidden: !Wtf.account.companyAccountPref.isLineLevelTermFlag,
        hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !(this.isRCMApplicableInPreferences  && this.paymentOption.AgainstVendor === this.paymentType),
        baseCls:'tempbackgroundview',
        html:this.LineLevelTermTplSummary.apply({
            lineLevelArray : lineLevelArray,
            TotalTaxAmt : WtfGlobal.currencyRenderer(0)
        })
    });
    
    this.tplSummary = new Wtf.XTemplate(
            '<div class="currency-view">',
            '<table width="100%">', //       
            '<tr><td><b>' + (this.isReceipt ? WtfGlobal.getLocaleText("acc.mp.receiptAmount") : WtfGlobal.getLocaleText("acc.mp.paymentAmount")) + ': </b></td><td text-align=right>{due}</td></tr>',
            '</table>',
            '<div id="hideIdForTax">',
            '<hr class="templineview">',
            '<table width="100%">',
            '<tr><td><b>' + WtfGlobal.getLocaleText("acc.invoice.amt") + ' </b></td><td text-align=right>{amountwithouttax}</td></tr>',
            '<tr><td><b>' + WtfGlobal.getLocaleText("acc.rem.196") + ': </b></td><td text-align=right>{taxamount}</td></tr>',
            //TDS Amount to be shown only for India country.
            (Wtf.account.companyAccountPref.countryid ==Wtf.Country.INDIA &&  Wtf.isTDSApplicable && this.paymentType != this.paymentOption.AgainstCustomer)? '<tr><td><b>' + WtfGlobal.getLocaleText("acc.rem.256") + ': </b></td><td text-align=right>{totaltdsamount}</td></tr>' : "",
            '</table>',
            '<hr class="templineview">',
            '</div>',
            '<table width="100%">',
            '<tr><td width="40%"><b>' + (this.isReceipt ? WtfGlobal.getLocaleText("acc.mp.12") : WtfGlobal.getLocaleText("acc.mp.13")) + '</b></td><td text-align=right width="60%">{received}</td></tr>',
            '<tr><td width="40%"><b>' + (this.isReceipt ? WtfGlobal.getLocaleText("acc.rp.amountrecInBase") : WtfGlobal.getLocaleText("acc.mp.paidInBase")) + '</b></td><td text-align=right width="60%">{receivedinbase}</td></tr>',
            '</table>',
            /*  '<hr class="templineview">',
             '<table width="100%">',
             '<tr><td><b>'+(this.isReceipt?WtfGlobal.getLocaleText("acc.rp.resce"):WtfGlobal.getLocaleText("acc.mp.paya"))+ ' </b></td><td align=right>{receivable}</td></tr>',
             '</table>',
             '<table width="100%">',
             '<tr><td><b>'+(this.isReceipt?WtfGlobal.getLocaleText("acc.rp.resceInBase"):WtfGlobal.getLocaleText("acc.mp.payaInBase"))+ ' </b></td><td align=right>{receivableInBase}</td></tr>',
             '</table>',
             '<hr class="templineview">',*/
            '<hr class="templineview">',
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
            totaltdsamount: WtfGlobal.currencyRenderer(0),
            amountwithouttax: WtfGlobal.currencyRenderer(0),
            receivedinbase: WtfGlobal.currencyRenderer(0)
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
            balanceAmount: WtfGlobal.addCurrencySymbolOnly(0, this.symbol)  // Default value is set to 0
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
    } else if (config.moduleId == 14 || config.moduleId == 16) {
        if (config.moduleId == 14) {
            tranType = Wtf.autoNum.Payment;
        } else {
            tranType = Wtf.autoNum.Receipt;
        }
    }
    if (this.isRequisition) {
        tranType = Wtf.autoNum.Requisition;
    } else if (this.isRFQ) {
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
        height: 200,
        items: [{
                region: 'center',
                border: false,
                autoHeight: true,
                items: [this.bankBalanceTpl, this.balanceAmountTpl,this.southCenterTpl,this.LineLevelTermTpl]
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
            }, {
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
            }, {
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
    if(this.paymentType !== this.paymentOption.AgainstGL && this.paymentType !== this.paymentOption.AgainstCustomer) {
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
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),//'Save',
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
                hidden: (this.isEdit) || this.readOnly,
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
//            (!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))?this.singleRowPrint=new Wtf.exportButton({
//            obj:this,
//            id:"printSingleRecord"+ this.id,
//            iconCls: 'pwnd printButtonIcon',
//            text:WtfGlobal.getLocaleText("acc.rem.236"),
//            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
//            disabled :true,
//            hidden: this.readOnly || this.isRequisition || this.isRFQ || this.isSalesCommissionStmt,
//            filename: WtfGlobal.getLocaleText("acc.pmList.tabTitle"), 
//            isEntrylevel:true,
//            exportRecord: this.exportRecord,
//            menuItem:{
//                rowPrint:true
//            },
//            get: tranType,
//            moduleid:this.moduleid
//        }):'',
            this.recurringBtn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.repeatedMP.recPayment"), //'Recurring Payment',
                scope: this,
                id: "RecurringMP" +this.id,
                iconCls: getButtonIconCls(Wtf.etype.copy),
                hidden: !(config.moduleId==Wtf.Acc_Make_Payment_ModuleId && config.paymentType==3), //Payment Type : 1-Payment To Vendor, 2-Payment To Customer, 3-GL Payment
                disabled : true,
                handler: function() {
                if(this.recordForRecurring){
                    if(config.moduleId==Wtf.Acc_Make_Payment_ModuleId && config.paymentType==3) {
                        var moduleid = config.moduleId;
                        var isCustomer = (moduleid==!Wtf.Acc_Make_Payment_ModuleId) ? true : false;
                        callRepeatedPaymentWindow(isCustomer,this.recordForRecurring, false, this.isEdit, false, this.RecordID, moduleid);
                        Wtf.getCmp('RepeatedPaymentWin').on('cancel',function(config){                        
                            this.recurringBtn.enable();
                        },this);   
                        this.recurringBtn.disable();
                    }
                }
            }
            }),
            this.savePrintBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.savePrintBtn"), //'Save',
                scope: this,
                id: "printsave" + config.helpmodeid + this.id,
                iconCls: getButtonIconCls(Wtf.etype.save),
                hidden: this.readOnly,
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
    this.grid.on('updatecustomdata', this.setcustomdata, this);
    this.creationDate.on('change',this.onDateChange,this);  
    this.Currency.on('change',this.onCurrencyChange,this);
    new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
        this.grid.on('columnmove', this.saveGridStateHandler, this);
        this.grid.on('columnresize', this.saveGridStateHandler, this);
    }, this);
    if (this.readOnly) {
        this.grid.enableColumnMove = false;
        this.grid.enableColumnResize = false;
    }
    Wtf.account.PaymentEntry.superclass.constructor.call(this, config);
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

Wtf.extend(Wtf.account.PaymentEntry, Wtf.account.ClosablePanel, {
    onRender: function(config) {
        Wtf.account.PaymentEntry.superclass.onRender.call(this, config);
        WtfGlobal.getGridConfig(this.grid, this.moduleid + "_" + this.paymentType, true, false);
        if(!this.readOnly){
            this.isClosable=false;         // This flag is used to give an alert if user is closing the form without saving the data. In view case, alert will not be given.
        }
        if(this.sequenceFormatStore.getCount() > 0){
            this.setNextNumber();
        } else {
            this.sequenceFormatStore.on('load', this.setNextNumber, this);
        }
        chkPaySeqFormatStoreLoad();
        this.createNameField();
        if (this.isEdit) {
            this.getPaymentDataFromServer();
            this.fetchPaymentAccBalance();
        }
        
        if (this.isEdit || this.isCopyReceipt) {
            if (this.record.data.detailType != this.paymentMethodType.Bank) {
                this.savePrintBttn.hide();
            }
            if ((this.paymentType !== this.paymentOption.AgainstGL) && this.personstore && this.record && this.record.data && this.record.data.personid) {
                this.loadPersonStore(this.record.data.personid);
            }
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
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.bulkRecpaymentfuture")], 2);
             this.creationDate.setValue(maxDate);   
            }
            
            this.updateAmount(getRoundedAmountValue(this.grid.getMultiDebitAmount()));
        }
        
        if(this.taxPaymentDataParams != undefined){
            this.getTaxPaymentDataFromServer();
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
     this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.makePayment);
    },hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false))){
                        continue;
                    }
                    if(fieldArray.isHidden){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    }
                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).disabled = fieldArray.isReadOnly;
                    }
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        var fieldLabel="";
                        if(fieldArray.fieldLabelText!="" && fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined){
                            fieldLabel= fieldArray.fieldLabelText+" *";
                        }else{
                            fieldLabel=(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel) + " *";
                        }
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = fieldLabel;
                    }else{
                        if( fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined && fieldArray.fieldLabelText!=""){
                            if(fieldArray.isManadatoryField && fieldArray.isFormField )
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText +"*";
                            else
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText;
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
                    url: this.isCustomer ? "ACCInvoiceCMN/getInvoicesForLifoFifo.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsForLifoFifo.do",
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
                    url: this.isCustomer ? "ACCInvoiceCMN/getInvoicesForLifoFifo.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsForLifoFifo.do",
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
                    url: this.isCustomer ? "ACCInvoiceCMN/getInvoicesForLifoFifo.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsForLifoFifo.do",
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
                    url: this.isCustomer ? "ACCInvoiceCMN/getInvoicesForLifoFifo.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsForLifoFifo.do",
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
            url: 'ACCVendorPaymentNew/getSinglePaymentDataToLoad.do',
            params: {
                billid: this.record.data.billid,
                isCopyTransaction:this.isCopyReceipt,   // Used to load the data conditionaly
                ispendingAproval:this.pendingApproval,
                isView:this.readOnly                    //used to load discount only when view payment from pending payment report as for edit case we do not load the discount
            }
        }, this,
        function(result, req) {
            this.loadForm(result);
        });
    },
    getTaxPaymentDataFromServer: function(comboboxObject) {
        var url = (this.taxPaymentDataParams.excisePaymentFlag != undefined && this.taxPaymentDataParams.excisePaymentFlag) ? "ACCInvoiceCMN/getExcisePaymentJEData.do" : "ACCCombineReports/getIndiaComplianceReportData.do";
        Wtf.Ajax.requestEx({
            url: url,
            params: this.taxPaymentDataParams
        }, this,
        function(result, req) {
            var rec = {
                data : result.data[0]
            };
            this.loadForm(rec);
            this.onGridDataChanged();
            this.setPaymentMethodData(rec);
            this.grid.getColumnModel().setEditable(this.grid.getColumnModel().findColumnIndex("enteramount"), false);
            if(!Wtf.isEmpty(comboboxObject)){
                this.hideShowCheckDetailsOnPaymentMethodChange(comboboxObject);
            }
        });
    },
    loadForm: function(rec) {
        var recData = rec.data;
        var recDetails = recData.Details;
        this.lineRec = recDetails;
        this.presonCode=recData.personcode;  // IN edit case, vendor/customer store is not loaded. So, for getting vendor/customer code, we have set this variable in edit case.
        this.isIBGTypeTransaction = recData.isIBGTypeTransaction;
        this.ibgDetailsID = recData.ibgDetailsID;
        /*
            In copy case no need to send billid otherwise it will be consider as edit case
        */
        if(!this.isCopyReceipt){
           this.setHiddenFieldValues(recData);
        }
        /**
         * Display Account code with account name if Present.
         */
        for(var i = 0; i<recDetails.data.length; i++){
            if (recDetails.data[i]["acccode"] != undefined && recDetails.data[i]["acccode"] != "") {
                recDetails.data[i]['documentno'] = '[' + recDetails.data[i]["acccode"] + '] ' + recDetails.data[i]['documentno'];
            }
        }
        if(this.nonRefundable!=undefined){
            this.nonRefundable.setValue(recData.nonRefundable);
        }
        this.setComboValues(recData);
        if(this.isCopyReceipt){
            this.sequenceFormatCombobox.fireEvent('select',this.sequenceFormatCombobox);
        } 
        this.checkIsIBGTransaction(rec,true);
        this.NorthForm.form.loadRecord(rec);
        if (this.isGST) {
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

        var flag=true;
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && !this.isEdit){
            flag=false;  
        }
        if(flag){
//            this.pmtMethod.setValForRemoteStore(recData.pmtmethod, recData.paymentmethodname,recData.hasAccess);
        }
        
        /*
         *  Currency store load to get exchange rates on billdate in the south panel.  
         */
//        this.changeCurrencyStore();
        if(this.isCopyReceipt && this.sequenceFormatCombobox.getValue()=='NA'){
            this.No.setValue("");
        }
        this.paymentAccountInEditRecord= rec.data.paymentmethodacc//this value needs for comparison when payment method get changed
        this.chequeSequenceFormatID=recData.chequesequenceformatid;//We are setting cheque Seq format combo after loading of seqeunce format. So assigning this value to variable for later use
        this.chequeNumber=recData.chequenumber;//this value needs later when payment method get changed
        this.loadCheckDetails(rec);
        if(this.SouthForm) {
            this.SouthForm.form.loadRecord(rec.data.paydetail);
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
                        this.updateAmount(getRoundedAmountValue(this.totalAmountWithTDS()));
                    }
                }
            });
        }
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.rcmApplicable!=undefined && this.isRCMApplicableInPreferences ) {
            if (rec.data.GSTINRegTypeDefaultMstrID != undefined && rec.data.GSTINRegTypeDefaultMstrID !== "" && rec.data.GSTINRegTypeDefaultMstrID === Wtf.GSTRegMasterDefaultID.Unregistered) {
//                this.rcmApplicable.setValue(true);
//                this.rcmApplicable.setDisabled(true);
                this.purchaseFromURD = true;
            } else {
//                this.rcmApplicable.setDisabled(false);
                this.purchaseFromURD = false;
            }
        }
        if(this.isCopyReceipt){
            this.checkForValidLineLevelData();
            this.updateAmount(getRoundedAmountValue(this.totalAmountWithTDS()));
        }
        this.updateSouthTemp();
        this.updateBalanceAmount();
        if(this.grid!=undefined){
            this.grid.updateTermDetails();
        }
        /*Call If permission from Payment Methode Matrix is Enable*/
       if((WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.viewBankTypeAccountBalance))
            ||(WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.viewCashTypeAccountBalance))){
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
    setComboValues: function(recData) {
        if(this.Name) {
            var extraRec = new Wtf.personRec({
                deducteetype : recData.deducteetype,
                deducteetypename : recData.deducteetypename,
                residentialstatus : recData.residentialstatus,
                natureOfPayment : recData.natureOfPayment,
                deductionReason : recData.deductionReason
            });
            this.Name.setValForRemoteStore(recData.personid, recData.personname, undefined, extraRec);
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
                this.sequenceFormatCombobox.disable(); // In Edit case, user cannot change sequence format but able to change payment number in 'NA' sequence format
                if (this.readOnly) {
                    this.No.disable();
                }
            }           
        }
        /*Sequence Format Should be enabled during TDS payment ERP-34473*/
        if(!Wtf.isEmpty(this.tdsPaymentJsonFlag) && this.tdsPaymentJsonFlag){
            this.sequenceFormatCombobox.enable();
            this.No.enable();
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
        if(this.isEdit && this.Name){
            if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable && !this.isCustomer){ 
                var rec = WtfGlobal.searchRecord(this.getNameComboStore(), this.Name.getValue(), 'accid');
                if(rec != undefined){
                    this.checkBasicExemption(rec);                        
                }
            }
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
        this.ExciseUnitCombo.disable();
        this.Memo.enable();
        this.paidTo.enable();
        this.Payee.enable();
        this.savePrintBttn.disable();

    },
    loadLineDetails: function(recDetails) {
        this.grid.store.loadData(recDetails);
        if(this.isEdit&&this.pendingApproval&&!this.readOnly){
            this.grid.getStore().each(function(rec){
                
                /*If any Invoice is linked with multiple payment
                 * And all are in Pending tab
                 * 1.Approved first payment with partial amount
                 * 2.When we Editing/Approving another payment     
                 * then We resetting "Enter Amount" field to Zero in Payment grid
                 * for user to adjust amount as per amountdue of invoice
                 * */
                if(rec.data.transactionAmount > rec.data.amountDueOriginal){
                   rec.set('enteramount',0)  
                   this.remainAtSameLevel=true;
                }
               
            },this);
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            this.grid.getStore().each(function(rec){
                if(rec.data['productid']!=undefined && rec.data['productid']!=''){
                    this.grid.productid= rec.data['productid'];
                }
            },this);
        }
        this.grid.addBlankRow();
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
        if (this.sequenceFormatStore.getCount() > 0) {
            if (this.isEdit && !this.isCopyReceipt) {                       //Edit case
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
            } else {
                if (this.isCopyReceipt != undefined && this.isCopyReceipt) {  // Copy case
                    var indexCopyCase = this.sequenceFormatStore.find('id', this.record.data.sequenceformatid);
                    if (indexCopyCase == -1) {
                        this.sequenceFormatCombobox.setValue("NA");
                        this.sequenceFormatCombobox.disable();
                        this.No.enable();
                        this.No.setValue("");
                    } else {
//                        var seqRecCopyCase = this.sequenceFormatStore.getAt(0)
                        var seqRecCopyCase = this.sequenceFormatStore.getAt(indexCopyCase)//ERP-12259
                        this.sequenceFormatCombobox.setValue(seqRecCopyCase.data.id);
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    }
                } else {                                                     // Create new case
                    this.setSequenceFormatForCreateNewCase();
                        }

                    }
                }
    },
    onRCMApplicableChange: function(o, val, oldval) {
        if (this.grid.getStore().getCount() > 1) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.gridDataWillCleared"), function (btn) {
                if (btn == "yes") {
                    this.grid.getStore().removeAll();
                    this.grid.addBlankRow();
                    this.updateSouthTemp();
                    this.updateBalanceAmount();
                } else {
                    o.setValue(oldval);
                }
            }, this);
        }
    },
    createGrid: function() {
        this.disableGridInVATCSTExcisePayment = ((Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA) && (this.isEdit != undefined && this.isEdit) && (this.record!= undefined && this.record.data.AllowToEditCopy!=undefined)) ? !this.record.data.AllowToEditCopy: false;
        this.disableGridInTDSPayment = false;
        if ((Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA)) {
            if ((this.isEdit != undefined && this.isEdit)) { // TDS Payment Edit Case(Copy case doesn't exist).
                this.disableGridInTDSPayment = (this.record != undefined && this.record.data.tdsPaymentJsonFlag != undefined) ? this.record.data.tdsPaymentJsonFlag : false;
            } else { // TDS Payment (Create New Case)
                this.disableGridInTDSPayment = this.tdsPaymentJsonFlag;
            }
        }
        this.grid = new Wtf.account.OSDetailGridNew({
            region: 'center',
            border: true,
            currencyid: this.currencyid,
            paymentType: this.paymentType,
            paymentOption: this.paymentOption,
            isEdit: this.isEdit,
            invObj:this.invObj,
            isBulkPayment:this.isBulkPayment,
            isCopyReceipt:this.isCopyReceipt,
            symbol:WtfGlobal.getCurrencySymbol(),
            parentObj: this,
            readOnly:this.readOnly || this.disableGridInTDSPayment,
            pendingApproval:this.pendingApproval,
            disabled: this.disableGridInVATCSTExcisePayment ,
            moduleid: this.moduleid,
            isCustomer:this.isCustomer,
            height: 200,
            paymentEntryObject: this,
            isAllowedSpecificFields:this.isAllowedSpecificFields,
            cls: 'gridFormat',
            isReceipt: this.isReceipt,
            isMultiDebit: this.isMultiDebit,
            amount: 0,
            //id: this.id + (this.isReceipt ? 'customergrid' : 'vendorgrid'),  //SDP-13345
            closable: true,
            billid:(this.record!= undefined)?this.record.data.billid:'',
            AllowToEditCopy:(this.record!= undefined && this.record.data.AllowToEditCopy!=undefined)?this.record.data.AllowToEditCopy:true
        });
    },
    savePringCheque: function(a, b) {
        this.saveAndCreateNewFlag = false; 
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
        this.checkIsIBGTransaction(rec);
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
                    isReceipt: this.isReceipt,
                    type: detailType,
                    isSequenceformat:this.isSequenceformat,
                    paymentMethodAccountId:paymentMethodAccountId,
                    isEdit:this.isEdit,
                    isCopyReceipt:this.isCopyReceipt,
                    chequeSequenceFormatID:this.chequeSequenceFormatID,
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
        } else {
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
        this.isReceipt ? addMasterItemWindow('18') : addMasterItemWindow('17');
        Wtf.getCmp("masterconfiguration").on('update', function() {
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
        var creationDate = this.creationDate.getValue();
        var isValidDate = this.getNewestTransactionDate(creationDate,creationDate);
        var isValidForAddingGLTypePayment;
        var isValidForMultipleAccounts;
        var isValidCreditAndDebitDetails=false;
        if(this.SouthForm){
            isValidChequeDetails = this.SouthForm.getForm().isValid();
            if(this.SouthForm.checkNo.getValue()=="" || this.SouthForm.checkNo.getValue()==undefined || (this.SouthForm.sequenceFormatCombobox!=undefined && this.SouthForm.sequenceFormatCombobox.getValue()!='NA' && this.SouthForm.sequenceFormatCombobox.getRawValue().length!=this.SouthForm.checkNo.getValue().length)){
                this.SouthForm.checkNo.markInvalid();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Entered cheque number is invalid or empty"], 2);
                return;
            }
        }
        if(!isValidNorthForm || !isValidChequeDetails || !isValidCustomFields){
            WtfGlobal.dispalyErrorMessageDetails(this.id+'requiredfieldmessagepanel', this.getInvalidFields());
            this.NorthForm.doLayout();
            this.isChequePrint=false;
            this.saveAndCreateNewFlag = false; 
            return;
        } else {
            Wtf.getCmp(this.id+'requiredfieldmessagepanel').hide();
        }
        if(!isValidDate){
            return;
        }
        /*
         * Validate GST dimension values present or Not
         */
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isShowAlertOnDimValueNotPresent.indexOf(parseInt(this.moduleid))> -1
                && this.isAdvanceTypeTransaction(this.grid)) {
            if (!isGSTDimensionValuePresent(this, this.grid)) {
                return false;
            }
            /**
             * Show alert on Save document if GST details not presnet 
             * ERP-39257
             */
            if(this.paymentOption.AgainstGL !== this.paymentType){
            if (!isGSTHistoryPresentOnDocumentCreation(this)) {
                return false;
             }
        }
        }
        // Checking whether fom is valid or not if payment is IBG type.
        var validForIBGTYpeTransaction = this.validateForIBGTypePayment();
        if(!validForIBGTYpeTransaction){
            if(this.paymentType == this.paymentOption.AgainstVendor){ 
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.information"), 
                    msg: WtfGlobal.getLocaleText("acc.ibg.youHaveNotSelectedDetailsForVendor"),
                    buttons: Wtf.MessageBox.YESNOCANCEL,
                    fn: function(btn) {
                        if (btn == "yes") {
                            var rec = WtfGlobal.searchRecord(this.personstore, this.Name.getValue(), 'accid');
                            this.callIBGDetailsGrid(rec, false, true);
                        }else if(btn == "no"){
                            this.paidTo.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.mp.paidTo"));
                            this.paidTo.allowBlank = true;
                            this.isIBGTransactionCheckedInEditCase=true;
                            this.isIBGTypeTransaction=false;
                            this.ibgDetailsID="";
                        }
                    },
                    closable:false,
                    animEl: 'mb9',
                    scope: this,
                    icon: Wtf.MessageBox.QUESTION
                });
                return;
            }else if(this.paymentType == this.paymentOption.AgainstGL){
                
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.information"), 
                    msg: WtfGlobal.getLocaleText("acc.ibg.youHaveNotSelectedDetailsForPaidTo"),
                    buttons: Wtf.MessageBox.YESNOCANCEL,
                    fn: function(btn) {
                        if (btn == "yes") {
                            this.paidTo.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.mp.paidTo")+'*');
                            this.paidTo.allowBlank = false;
                            var rec = WtfGlobal.searchRecord(this.MPPaidToStore, this.paidTo.getValue(), 'id');
                            this.callIBGDetailsGrid(rec, true, true);
                        }else if(btn == "no"){
                            this.paidTo.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.mp.paidTo"));
                            this.paidTo.allowBlank = true;
                            this.isIBGTransactionCheckedInEditCase=true;
                            this.isIBGTypeTransaction=false;
                            this.ibgDetailsID="";
                        }
                    },
                    animEl: 'mb9',
                    scope: this,
                    closable:false,
                    icon: Wtf.MessageBox.QUESTION
                });
                return;;
            }
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
        if(this.paymentType == this.paymentOption.AgainstVendor){
            isValidForAddingGLTypePayment= this.grid.isValidForAddingGlTypePayment();
            if(!isValidForAddingGLTypePayment){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.noAdvInvNoteEntry")], 2);
                return;
            }
        }
     
        // For India Country, Taxes are not used, so not mapped to GST type of Accounts
        if(this.paymentType == this.paymentOption.AgainstGL && Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA){
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
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mprp.transactionsBelongsToDiffAccounts") ], 2);
//                return;
//            }    
//        }
        isValidCreditAndDebitDetails = this.grid.isValidCreditAndDebitDetails();
        if(!isValidCreditAndDebitDetails){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.alertCreditDebitDetailsInvalid")],2);
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
            var amount=this.totalAmountWithTDS();
            amount=getRoundedAmountValue(amount);
            if(this.Amount.getValue()<amount){
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
                        title: WtfGlobal.getLocaleText("acc.common.confirm"), //'Warning',
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
                if (Wtf.account.companyAccountPref.adjustmentAccountPayment == "") {
                    this.checkWarning=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.messages.selectadjustmentaccount")],2);
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
        var payAccName= '';
        var pmRec = WtfGlobal.searchRecord(this.pmtStore,this.pmtMethod.getValue(),'methodid');
        if(pmRec) {
            payAccName = pmRec.data.accountname;
        }
        
        receiptDetails = eval(this.grid.getJSONData());
        if(receiptDetails!=null && receiptDetails!=undefined){
            for(i=0;i<receiptDetails.length;i++){
                rec= receiptDetails[i];
                if(rec.RCMApplicable){
                    var invRec = {
                        data : rec
                    }
                    if (!WtfGlobal.validateKnockOffFieldsData(this.moduleid, this.tagsFieldset, invRec,"isformultientity")) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please select same values of <b>Entity </b> Custom Field(s)/Dimension(s) for RCM Invoice and Payment."], 2);
                        return;
                    }
                }
                if (WtfGlobal.isIndiaCountryAndGSTApplied() && this.isRCMApplicableInPreferences  && this.rcmApplicable != undefined && this.rcmApplicable.getValue()) {
                    if (rec.type === 1 && this.grid != undefined && (this.grid.productid == undefined || this.productid == "")) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.prod.comboEmptytext")], 2);
                        return;
                    }
                }
            }
        }
             
        var remBalance=this.accEndingBalance-(this.Amount.getValue());
        var budgetInSelectedCurrency=this.calCustMinBugetInSelectedCurrency();
        if(remBalance<budgetInSelectedCurrency)
        {
            if( Wtf.account.companyAccountPref.custMinBudget==1){          //Block case
                var accMsg=WtfGlobal.getLocaleText("acc.field.MinimumBudgetLimitforthisPaymentAccounthasreached")+"<br><br><center>";
                accMsg +="<b>"+WtfGlobal.getLocaleText("acc.field.PaymentAccount")+"</b> "+payAccName+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.CurrentBalance")+"</b> "+parseFloat(getRoundedAmountValue(this.accEndingBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.BalanceAfterPayment")+"</b> "+parseFloat(getRoundedAmountValue(remBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.Min.BudgetLimit")+"</b> "+parseFloat(getRoundedAmountValue(budgetInSelectedCurrency)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                accMsg += "<br><br>"+WtfGlobal.getLocaleText("acc.field.Youcannotproceed")+"</center>";
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),accMsg],3);
                return;
            }
            else if( Wtf.account.companyAccountPref.custMinBudget==2){     //Warn Case
                accMsg=WtfGlobal.getLocaleText("acc.field.MinimumBudgetLimitforthisPaymentAccounthasreached")+"<br><br><center>";
                accMsg +="<b>"+WtfGlobal.getLocaleText("acc.field.PaymentAccount")+"</b> "+payAccName+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.CurrentBalance")+"</b> "+parseFloat(getRoundedAmountValue(this.accEndingBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.BalanceAfterPayment")+"</b> "+parseFloat(getRoundedAmountValue(remBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.Min.BudgetLimit")+"</b> "+parseFloat(getRoundedAmountValue(budgetInSelectedCurrency)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                accMsg += "<br><br>"+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+"</center>";
                Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg:accMsg,
                    width:500,
                    buttons: Wtf.MessageBox.YESNO,
                    scope:this,
                    icon: Wtf.MessageBox.INFO,
                    fn: function(btn){
                        if(btn =="yes") {              
                            this.saveData(); 
                        }else{
                            return;
                        }
                    }
                }, this); 
            }
            else{      //Ignore Case
                this.saveData();  
            }
        } else {
            this.saveData();  
        }
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
        Wtf.MessageBox.show({
            title:WtfGlobal.getLocaleText("acc.common.savdat"), 
            msg:WtfGlobal.getLocaleText("acc.je.msg1"), 
            scope:this,
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.INFO,
            width:300,
            fn:function(btn) {
                if (btn != "yes") {
                    this.checkWarning=true;
                    this.isChequePrint = false;
                    this.isWarnConfirm=false;
                    this.enableSaveButton();
                    return;
                }
//            this.makeFieldsEnableDisable(false);  // Values of the fields which are disabled are null. So before saving those fiels, we are making those fields enable and read only.
            var jsonObject = this.NorthForm.form.getValues();
                if (this.isGST) {
                    jsonObject['CustomerVendorTypeId'] = this.CustomerVendorTypeId;
                    jsonObject['GSTINRegistrationTypeId'] = this.GSTINRegistrationTypeId;
                    jsonObject['gstin'] = this.gstin;
                    if (this.isEdit && !this.isCopyReceipt) {
                        jsonObject['gstdochistoryid'] = this.gstdochistoryid;
                    }
                }
            WtfGlobal.onFormSumbitGetDisableFieldValues(this.NorthForm.form.items, jsonObject);
            var custFieldArr = this.tagsFieldset.createFieldValuesArray();
            if (custFieldArr.length > 0)
                jsonObject['customfield'] = JSON.stringify(custFieldArr);            
            jsonObject['Details'] = this.grid.getData();
            if (this.SouthForm) {
                var paydetail=this.SouthForm.getForm().getValues();
                paydetail.sequenceformat=this.SouthForm.sequenceFormatCombobox.getValue();
                paydetail.chequenumber=this.SouthForm.checkNo.getValue();
                jsonObject['paydetail'] = JSON.stringify(paydetail);
            }
            jsonObject['balaceAmount'] =this.finalBalanceAmount;
            if(this.advanceToVendor){
                jsonObject['advanceToVendor'] =this.advanceToVendor.getValue();
            }
            if(this.rcmApplicable){
                jsonObject['rcmApplicable'] =this.rcmApplicable.getValue();
            }
            jsonObject['accountIdComPreAdjPay'] =Wtf.account.companyAccountPref.adjustmentAccountPayment;
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
            if(Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoWarn && !this.isWarnConfirm){
              jsonObject['isWarn'] = true;
            }
            if(this.isWarnConfirm){
              jsonObject['isWarn'] = false;
            }
            jsonObject['isLinkedToClaimedInvoice'] = this.isLinkedToClaimedInvoice;
            jsonObject['ismulticurrencypaymentje']= this.isMultiCurrencyPayment();
            jsonObject['isIBGTypeTransaction']= this.isIBGTypeTransaction;
            jsonObject['ibgDetailsID']= this.ibgDetailsID;
            jsonObject['ibgCode'] = this.ibgTransactionCode.getValue();
            jsonObject['isEditToApprove'] = this.isEditToApprove
    
             
                /*-----If Pending document is edited----------  */
                if (this.pendingApproval) {//sent from Make Payment
                    jsonObject['isEditedPendingDocument'] = true;                   
                }
                
                if (this.remainAtSameLevel) {//To Approve/Edit Make Payment

                    jsonObject['remainAtSameLevel'] = true;
                }
            var rec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
            var bankType = rec.data.bankType;
            jsonObject['bankType']= bankType;
            this.saveBttn.disable();
            this.savencreateBttn.disable();
            WtfComMsgBox(27, 4, true);
            this.ajxUrl = this.getUrlForSave();
            
            WtfGlobal.setAjaxTimeOut();
            Wtf.Ajax.requestEx({
                url: this.ajxUrl,
                params: jsonObject
            }, this, this.genSavePaymentSuccessResponse, this.genSavePaymentFailureResponse);
               }
        }, this);
    },
    addTemplateUnit:function(){
        callAddTemplateUnit();
    },
    getUrlForSave: function() {
        var url;
        if(this.isAllowedSpecificFields){
            url="ACCVendorPaymentNew/updatePayment.do";
        }else  if (this.isReceipt) {
            url = "ACCReceipt/saveReceipt.do";
        } else if (!this.isReceipt) {
            url = "ACCVendorPaymentNew/savePayment.do";
        }
        return url
    },
    genSavePaymentSuccessResponse: function(response, request) {
        WtfGlobal.resetAjaxTimeOut();
        if(!response.success && response.isWarn){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), // 'Warning',
                msg: response.msg+""+WtfGlobal.getLocaleText("acc.ven.msg4"),
                buttons: Wtf.MessageBox.YESNO,
                width:450,
                fn: function(btn) {
                    if(btn =="yes") {
                        this.isWarnConfirm = true;
                        this.saveData(); 
                    }else{
                        this.enableSaveButton();
                    }
                },
                scope: this,
                icon: Wtf.MessageBox.QUESTION
            });
        }else if (!response.success) {/* Failed Case*/
            /*
             *For Check Duplicate documnet no
             */
            if ((this.moduleid == Wtf.Acc_Make_Payment_ModuleId) && response.isAccountingExe) {
                Wtf.MessageBox.hide();
                var label="";
                label = WtfGlobal.getLocaleText("acc.payment.newpaymnetno");
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
                                    Wtf.getCmp("receiptNo"+this.heplmodeid+this.id).setValue(this.newdono.getValue());
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
            }else if ((this.moduleid == Wtf.Acc_Make_Payment_ModuleId) && this.isEdit && response.isAccountingExe) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
            } else {
                if(this.isCopyReceipt){
                    WtfComMsgBox(["Copy Payment",response.msg],response.success*2+2);
                }else if(this.isEdit){
                    WtfComMsgBox(["Edit Payment",response.msg],response.success*2+2);
                }else {
                    WtfComMsgBox(["Payment",response.msg],response.success*2+2);
                }
            }
            //            WtfComMsgBox([WtfGlobal.getLocaleText("acc.accPref.autoPayment"), response.msg], response.success * 2 + 1);
            this.enableSaveButton();
            return;
        } else {
             Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg: response.msg,
                width:450,
                scope: {
                  scopeObj:this  
                },
                buttons: Wtf.MessageBox.OK,
                fn: function(btn ,text, option) {
                    this.scopeObj.refreshReportGrid();
                    if(this.scopeObj.moduleid != Wtf.Acc_Make_Payment_ModuleId || this.scopeObj.taxPaymentDataParams != undefined){
                        this.scopeObj.fireEvent("update",this, response.paymentid); //Added an event for Import Bank reconciliation window.
                    }
                },
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });  
        }        
        if(this.saveAndCreateNewFlag){/* Save And Create New Case*/
                this.enableSaveButton();
                this.isWarnConfirm=false;
                if(response.success ||!response.isWarn){
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
                this.tplSummary.overwrite(this.southCalTemp.body,{taxamount:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),amountwithouttax:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),totaltdsamount:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),received:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),due:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),receivable:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),receivableInBase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),advance:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),advanceInBase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),receivedinbase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol())});
                this.tagsFieldset.resetCustomComponents();  // Reset custom fields and dimensions
            }
            } else {/*Save Case*/
                this.enableButtons();
                if(response.success){
                     this.disableComponents();
                }
                this.response = response;
                this.request = request;
                var rec=this.NorthForm.getForm().getValues();
                this.exportRecord=rec;
                if(response.paymentid!=undefined){
                    this.RecordID=response.paymentid;
                }
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
            if(response.success && (this.moduleId==Wtf.Acc_Make_Payment_ModuleId && this.paymentType==3)){
                this.recordForRecurring = {};
                this.recordForRecurring.data={};
                this.recordForRecurring.data.billno = response.billno;
                this.recordForRecurring.data.billid = response.paymentid;
                this.recordForRecurring.data.repeatedid = response.repeatedid;
                this.recordForRecurring.data.nextdate = response.nextdate;
                this.recordForRecurring.data.interval= response.intervalUnit;
                this.recordForRecurring.data.intervalType = '';
                this.recordForRecurring.data.NoOfpost = '';
            }
            }
         if (this.isChequePrint) {
                var resdata = response.data[0];
//                var accountName=(this.Name)?this.Name.getRawValue():this.paidTo.getRawValue(); //If payment against Vendor/Customer, then name of the corresponding person will be printed otherwise name will be as selected in paid to combobox.
//                this.printCheque(accountName, resdata.amount, resdata.amountinword,resdata.amountinword1, resdata.date, resdata.dateLeft, resdata.nameLeft, resdata.amtinwordLeft,resdata.amtinwordLeftLine2,resdata.amtLeft,resdata.dateTop, resdata.nameTop, resdata.amtinwordTop,resdata.amtinwordTopLine2,resdata.amtTop);
                  this.printCheque(resdata); //Passing Paramter as JSON Object
                this.isChequePrint = false;
            }   
    },
    genSavePaymentFailureResponse: function(response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
//        this.restoreFieldsPreviousState();
        this.enableSaveButton();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    callIBGDetailsGrid: function(rec, isFromMasterConfiguration, isFromMP) {
        var PmtRec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
        var bankType = PmtRec.data.bankType;
        var ibgDetailsGrid = new Wtf.account.VendorIBGDetailsGrid({
            title: 'Receiving Bank Details',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            height: 500,
            bankType:bankType,
            width: 700,
            accRec: rec,
            isFromMasterConfiguration: isFromMasterConfiguration,
            isFromMP: isFromMP,
            closable: true,
            modal: true,
            layout: 'border'
        });

        ibgDetailsGrid.on('update', function(config) {
            this.isIBGTypeTransaction = true;  // Set transaction as IBG type when user selects the record from IBG detail window
            this.ibgDetailsID = (bankType == 1) ? config.sm.getSelected().data.ibgId : (bankType == 2 ? config.sm.getSelected().data.cimbReceivingBankDetailId : config.sm.getSelected().data.ocbcIBGDetailId);
        }, this);

        ibgDetailsGrid.show();
    },
    
    isOneTimeCustomer: function(isChecked) {
        if(isChecked) {
            this.personAccStore.proxy.conn.url= "ACCCustomer/getCustomersForCombo.do";
            var currentBaseParams = this.personAccStore.baseParams;
            currentBaseParams.isPermOrOnetime=true;
            this.createNameField(true); // isIBG/ isOneTimeCustomer flag - true
            this.personAccStore.baseParams = currentBaseParams;
            this.personAccStore.load();
        } else {
            this.createNameField(false); // isIBG/ isOneTimeCustomer flag - false
        }
    },
    
    checkIsIBGTransaction: function(rec,firstLoadOnEditOrCopy) {
        /*
         * Wtf.IBGBanks.UOBBank for UOB Bank
         */
        if (Wtf.account.companyAccountPref.activateIBG && (this.paymentOption.AgainstVendor == this.paymentType || this.paymentOption.AgainstGL == this.paymentType) && rec.data['isIBGBankAccount'] && rec.data['detailtype'] == this.paymentMethodType.Bank&&rec.data['bankType']!=Wtf.IBGBanks.UOBBank) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), // 'Warning',
                msg: WtfGlobal.getLocaleText("acc.msg.mpIsIBGPayment"),
                buttons: Wtf.MessageBox.YESNO,
                fn: function(btn) {
                    if (btn != "yes") {
                        this.isIBGTypeTransaction = false;
                        this.ibgTransactionCode.disable();
                        this.isIBGTransactionCheckedInEditCase=true;
                        if (this.paidTo != undefined) {
                            this.paidTo.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.mp.paidTo"));
                            this.paidTo.allowBlank = true;
                        }
                        return;
                    }
                    this.isIBGTransactionCheckedInEditCase=true;
                    this.isIBGTypeTransaction = true;
                    this.ibgTransactionCode.enable();
                    var rec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
                    var bankType = rec.data.bankType
                    if (this.paymentOption.AgainstGL == this.paymentType) {
                        if (this.paidTo != undefined) {
                            this.paidTo.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.mp.paidTo") + "*");
                            this.paidTo.allowBlank = false;
                        }
                    }
                },
                scope: this,
                closable:false,
                icon: Wtf.MessageBox.QUESTION
            });
        } else {
            /*
             *  For edit/copy case, for the first time of loading the form data, paid to value will not be filtered as it will be copied from original document.
             */
            if (this.isIBGTypeTransaction && !firstLoadOnEditOrCopy) {    
                if (this.paymentOption.AgainstGL == this.paymentType) {
                    if (this.paidTo != undefined) {
                        this.paidTo.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.mp.paidTo"));
                        this.paidTo.allowBlank = true;
                    }
                }
                this.isIBGTransactionCheckedInEditCase=true;
                this.isIBGTypeTransaction = false;
                this.ibgDetailsID = "";
                this.ibgTransactionCode.disable();
            }
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
        if(!this.isEdit && !this.isCopyReceipt && !this.readOnly&&!this.isBulkPayment){
            rowIndex = this.pmtStore.find("isdefault", "true");
            if (rowIndex != -1) {
                rec = this.pmtStore.getAt(rowIndex);                
                methodid = rec.get('methodid');
                this.pmtMethod.setValue(methodid);
                this.ShowCheckDetails(this.pmtMethod, rec);
            //                this.chechSequenceFormatExist(true,rec);//chevks the sequence format is exist for payment method                   
            }
            this.hideShowAccountBalance();
        }
        if(this.isBulkPayment){
            /*
             *rowIndex  is used for payment method cash
             */
            rowIndex = this.pmtStore.find("methodname","cash");
            rec = this.pmtStore.getAt(rowIndex);                
            methodid = rec.get('methodid');
            this.pmtMethod.setValue(methodid);
            this.ShowCheckDetails(this.pmtMethod, rec);
            this.hideShowAccountBalance();
        }
        if(this.isEdit || this.readOnly){ // Set Payment Method value in edit, copy and view case.
            this.pmtMethod.setValue(this.record!=null?this.record.data.methodid:"");
        }
    },
    setPaymentMethodData: function(rec) {
        var rowIndex = this.pmtStore.find("methodid", rec.data.pmtmethod);
        if (rowIndex != -1) {
            var rec = this.pmtStore.getAt(rowIndex);                
            var methodid = rec.get('methodid');
            this.pmtMethod.setValue(methodid); 
            this.ShowCheckDetails(this.pmtMethod, rec); 
        }
         this.hideShowAccountBalance();
    },
    updateAmount: function(amount) {
        this.Amount.setValue(getRoundedAmountValue(amount));
    },
    
     updateSouthTemp:function() {
        this.applyCurrencySymbol();
        var amountWithoutTax= WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount(), this.symbol)
        
        this.tplSummary.overwrite(this.southCalTemp.body, {
            taxamount: WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitTaxAmount(), this.symbol),
            totaltdsamount: WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiTDSAmount(), this.symbol),
            amountwithouttax:amountWithoutTax,
            received: WtfGlobal.addCurrencySymbolOnly(this.totalAmountWithTDS(), this.symbol),
            due: WtfGlobal.addCurrencySymbolOnly(this.Amount.getValue(), this.symbol),
            receivable: WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount() - this.Amount.getValue(), this.symbol),
            receivableInBase: WtfGlobal.addCurrencySymbolOnly(this.getPayableAmountInBase(), WtfGlobal.getCurrencySymbol()),
            receivedinbase: WtfGlobal.addCurrencySymbolOnly(this.getAmountPaidInBase(this.totalAmountWithTDS()), WtfGlobal.getCurrencySymbol())
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
    getConfirmationToChangePmtMethod : function(comboboxObject,newValue,oldValue){
        var pmtMethodRecord = WtfGlobal.searchRecord(this.pmtStore, newValue, "methodid");
        if(oldValue==''){                                       // Alert will not be shown when payment method is changed initially, as its default valu will be blank('')
            this.hideShowCheckDetailsOnPaymentMethodChange(comboboxObject);
            this.grid.getStore().removeAll();
            if(!Wtf.isEmpty(this.tdsPaymentJsonFlag) && this.tdsPaymentJsonFlag){ // If default Payment method account "Cash in hand" name changed manually, so during select payment method reload grid.
                this.getTaxPaymentDataFromServer(comboboxObject);
            }
            this.grid.addBlankRow();
            this.updateSouthTemp();
            this.updateBalanceAmount();
        } else if (pmtMethodRecord != undefined && pmtMethodRecord !=null && this.paymentCurrencyBeforeSelect == pmtMethodRecord.data["acccurrency"] && newValue == oldValue) {
            return;
        } else if (pmtMethodRecord != undefined && pmtMethodRecord !=null && this.paymentCurrencyBeforeSelect == pmtMethodRecord.data["acccurrency"] && newValue != oldValue) {
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
    createNameField: function(isIBGOrOneTimeCustomer) {
        if(this.paymentType !== this.paymentOption.AgainstGL){
            var nameIndex = false;
            if(this.Name) {
                this.NorthForm.getForm().items.remove(this.Name);
                this.Name.destroy();
                this.Name = undefined;
                nameIndex = true;
            }
            var comboConfig = {
                id: "account" + this.heplmodeid + this.id,
                hiddenName: 'accid',
                name:'accid',
                disabled: this.isEdit,
                emptyText: WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup.SelectAccount"), //' Select an Account...',
                listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 450,
                anchor: '86%',
                isVendor:!(this.isCustomer),
                isCustomer:this.isCustomer,
                minChars: 0,           // ERP-12377 : For remote store, minchar=4 by default. Changed to 0 for loading the data when search filter is cleared. 
                extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'],
                addNewFn: this.addPerson.createDelegate(this, [false, null, (this.isCustomer ? 'Customerwindow' : 'Vendorwindow'), this.isCustomer], true)
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
            this.Name.on('select', function (combo, record, index) {
                this.loadPersonStore(record.data.accid);
            }, this);
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
        var record = recArr[0];
        var combo = this.Name;
        if (this.mapDefaultPmtMethod && this.isCustomer) {
            if (combo.getValue() == this.nameBeforeSelect) { //If same name selected no need to do any action 
                return;
            }
            this.mapDefaultPaymentMethod(combo, record);
        }
        if (this.isGST || WtfGlobal.isUSCountryAndGSTApplied()) { // Populate GST dimension for US country : ERP-38917
            this.addressMappingRec = record.data.addressMappingRec;
            /**
             * ERP-32829 
             * code for New GST  i.e. populate dimension using dimension
             */
            if (record.data.currentAddressDetailrec != undefined) {
                if (this.isGST) {// below function call only for INDIA
                    this.applyGSTFieldsBasedOnDate();
                }

                this.addressDetailRecForGST = record.data.currentAddressDetailrec[0];
                var obj = {};
                obj.tagsFieldset = this.tagsFieldset;
                obj.currentAddressDetailrec = this.addressDetailRecForGST;
                obj.mappingRec = this.addressMappingRec;
                obj.isCustomer = this.isCustomer;
                obj.isShipping = this.isShipping;
                populateGSTDimensionValues(obj);
            }
        }
        this.paidTo.setValue(record.data['mappedPaidToId']);
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable && !this.isCustomer) {
            if (record != undefined) {
                this.checkBasicExemption(record);
            }
        }
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
            }else{//If Default Payment for customer not specified is blank then setting cash as defaultpayment method
                var rowIndex = this.pmtStore.find("methodname", "cash");
                var rec = this.pmtStore.getAt(rowIndex);
                var methodid = rec.get('methodid');
                this.pmtMethod.setValue(methodid);
                this.ShowCheckDetails(this.pmtMethod, rec);
                this.fetchPaymentAccBalance();
                this.hideShowAccountBalance();
            }
        }
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
                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && this.isRCMApplicableInPreferences && this.rcmApplicable != undefined) {
                    if (response.data[0].GSTINRegTypeDefaultMstrID != undefined && response.data[0].GSTINRegTypeDefaultMstrID !== "" && response.data[0].GSTINRegTypeDefaultMstrID === Wtf.GSTRegMasterDefaultID.Unregistered) {
                        this.purchaseFromURD = true;
                    } else {
                        this.purchaseFromURD = false;
                    }
                }
                getLineTermDetailsAndCalculateGSTForAdvance(this, this.grid, this.grid.productid);
            }
        });
    },
    checkBasicExemption:function(rec){
        
        if(rec &&  rec.data.deductionReason && rec.data.deductionReason == Wtf.BASIC_EXEMPTION_APPLIED){
            this.basicExemptionApplied = true;
        } else{
            this.basicExemptionApplied = false;
        }
        Wtf.Ajax.requestEx({// request to fetch tdsrate and amount as per nature of payment and other attributes
            url: "ACCVendorPaymentCMN/getTDSCalculationDetails.do",
            params: {
                natureofPayment: rec.data.natureOfPayment,
                deducteetype: rec.data.deducteetype,
                residentialstatus: rec.data.residentialstatus,
                vendorID: !Wtf.isEmpty(rec.data.accid)?rec.data.accid : rec.data.personid,
                billdate: new Date().format('Y-m-d')
            }
        }, this, function (resp) {
            if (resp != "") {
                if (resp.success) {
                    this.tdsRate = resp.tdsrate;
                    this.tdsmasterrateruleid = resp.tdsmasterrateruleid;                    
                     if(resp.basicexemptionpertransaction ){
                        this.basicExemptionPerTransaction=resp.basicexemptionpertransaction;
                     }
                     if(resp.basicexemptionperannum ){
                        this.basicExemptionPerAnnual=resp.basicexemptionperannum;
                    }
                } 
            }
        }, function (resp) {
        });
        Wtf.Ajax.requestEx({// request to fetch tdsrate and amount as per nature of payment and other attributes
            url: "ACCReports/getVendorLedger.do",
            params: {
                asofdate: WtfGlobal.convertToGenericDate(new Date()),
                customerIds:!Wtf.isEmpty(rec.data.accid)?rec.data.accid : rec.data.personid,
                enddate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates()),
                idparamname:'vendorid',
                invoiceAmountDueFilter:true,
                isPostDatedCheque:false,
                isSortedOnCreationDate:false,
                startdate:WtfGlobal.convertToGenericDate(new Date(Wtf.account.companyAccountPref.firstfyfrom)),	
                stdate:WtfGlobal.convertToGenericDate(new Date(Wtf.account.companyAccountPref.firstfyfrom)),	
                vendorIds:!Wtf.isEmpty(rec.data.accid)?rec.data.accid : rec.data.personid,
                withoutinventory:false
            }
        }, this, function (resp) {
            if (resp != "") {
                if (resp.success) {
                    if( resp.data && resp.data.length > 0 ){
                        for(var i = 0; i< resp.data.length; i++){
                            var transactionData = resp.data[i];
                            if(transactionData.debitAmountInBase){
                                this.debitbalance += transactionData.debitAmountInBase;
                            }
                            if(transactionData.creditAmountInBase){
                                this.creditbalance += transactionData.creditAmountInBase;
                            }
                        }
                    } else{
                        this.debitbalance = 0; 
                        this.creditbalance = 0; 
                        this.totalbalance = 0;
                    }
                    this.totalbalance = this.debitbalance + this.creditbalance
                    
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), resp.msg], 2);
                }
            }
        }, function (resp) {
        });
        
    },

    getNameComboStore: function() {
      return this.isReceipt ? 
        (this.paymentType == this.paymentOption.AgainstCustomer ? Wtf.customerAccRemoteStore : 
            (this.paymentType == this.paymentOption.AgainstVendor ? Wtf.vendorAccRemoteStore : this.personAccStore))
        : (this.paymentType == this.paymentOption.AgainstVendor ? Wtf.vendorAccRemoteStore : 
            (this.paymentType == this.paymentOption.AgainstCustomer ? Wtf.customerAccRemoteStore : this.personAccStore));  
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
        this.setSequenceFormatForCreateNewCase();        // when form is reset on 'save and create new' case, default sequence format will be set to combobox again.
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
        Wtf.getCmp("RecurringMP" +this.id).enable();
    },
    callEmailWindowFunction : function(response, request){
        if(response){
            if(response.pendingApproval){
                WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),WtfGlobal.getLocaleText("acc.module.name.14")+' '+WtfGlobal.getLocaleText("acc.field.ispendingforapprovalSoyoucannotsendmailrightnow")],3);
                return;
            }
            var rec =this.createRecordForEmail(response,request);
            if(this.isReceipt){
                callEmailWin("emailwin",rec,'Payment Receipt',4,false,false);
            }else{
                //callEmailWin("emailwin",rec,'Payment Voucher',8,false,false);
                callEmailForMultipleRecords('', rec, this.label, 8, false,'', 'Payment Voucher','pdf', 24, undefined,false,true,"",Wtf.Acc_Make_Payment_ModuleId,this.userds);
            }
        }
    },    
    updateBalanceAmount: function(){
        var amount=this.totalAmountWithTDS();
        var balanceAmount=this.Amount.getValue()-amount;
        var roundedAmount=getRoundedAmountValue(balanceAmount);
        this.balanceAmountTplSummary.overwrite(this.balanceAmountTpl.body, {
            balanceAmount:WtfGlobal.addCurrencySymbolOnly(roundedAmount, this.symbol)
        });
        this.finalBalanceAmount=roundedAmount;   // Each time balance amount is updated, this value awill also be updated. This value is used at the time of saving payment.
        this.updateSouthTemp();
            
    },
    printCheque : function(chequeDetail){           
        var newwin=window.open('','printwin','left=0,top=0,width=800,height=300,scrollbars = no' )
        newwin.document.write('<HTML>\n<HEAD>\n')
        newwin.document.write('<TITLE>Print Page</TITLE>\n')
        if(chequeDetail.isnewlayout){
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
        if(chequeDetail.isnewlayout){
            newwin.document.write('<div style="position: relative;">')
        }
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetail.dateLeft +'cm; top:'+chequeDetail.dateTop+'cm;">'+chequeDetail.date+'</div>')
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetail.nameLeft +'cm; top:'+chequeDetail.nameTop+'cm;">'+chequeDetail.accountName+'</div>')
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetail.amtinwordLeft +'cm; top:'+chequeDetail.amtinwordTop+'cm;">'+chequeDetail.amountinword+'</div>')
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetail.amtinwordLeftLine2 +'cm; top:'+chequeDetail.amtinwordTopLine2+'cm;">'+chequeDetail.amountinword1+'</div>')
        newwin.document.write('<div style="position: absolute; left:'+ chequeDetail.amtLeft +'cm; top:'+chequeDetail.amtTop+'cm;">'+chequeDetail.amount+'</div>')
        
        /*
         * if Other field option true the this filed will show on check.
         */
        if (chequeDetail.activateExtraFields !==undefined && chequeDetail.activateExtraFields) {  
            newwin.document.write('<div style="position: absolute; left:' + chequeDetail.memoLeft + 'cm; top:' + chequeDetail.memoTop + 'cm;">' + chequeDetail.memo + '</div>')
            newwin.document.write('<div style="position: absolute; left:' + chequeDetail.addressLine1Left + 'cm; top:' + chequeDetail.addressLine1Top + 'cm;">' + chequeDetail.addressLine1 + '</div>')
            newwin.document.write('<div style="position: absolute; left:' + chequeDetail.addressLine2Left + 'cm; top:' + chequeDetail.addressLine2Top + 'cm;">' + chequeDetail.addressLine2 + '</div>')
            newwin.document.write('<div style="position: absolute; left:' + chequeDetail.addressLine3Left + 'cm; top:' + chequeDetail.addressLine3Top + 'cm;">' + chequeDetail.addressLine3 + '</div>')
            newwin.document.write('<div style="position: absolute; left:' + chequeDetail.addressLine4Left + 'cm; top:' + chequeDetail.addressLine4Top + 'cm;">' + chequeDetail.addressLine4 + '</div>')
        }
        if(chequeDetail.isnewlayout){
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
            requestModuleid: Wtf.Acc_Make_Payment_ModuleId
        }
        if(!this.isMultiCurrencyPayment()){
            params['isReceipt']=false
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
        if (this.Name.lastSelectionText)
            this.Payee.setValue(this.Name.lastSelectionText);
        if (!this.isGST) {
            this.tagsFieldset.resetCustomComponents();
        }
        var customer = this.Name.getValue();
        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
        var fetchdataid=Wtf.Acc_Make_Payment_ModuleId
        this.tagsFieldset.setValuesForCustomer(moduleid, customer,fetchdataid);
        this.ibgDetailsID = ""; // Set to blank on Vendor change
        /*
         * Call IBG details window for selecting the details.
         */
        if (Wtf.account.companyAccountPref.activateIBG && this.isIBGTypeTransaction && this.paymentType == this.paymentOption.AgainstVendor) {
            var rec = WtfGlobal.searchRecord(this.personstore, this.Name.getValue(), 'accid');
            this.callIBGDetailsGrid(rec, false, true);
        }
    },
    
    getSequenceFormatCombo: function() {
//        this.sequenceFormatStore = Wtf.PaySeqFormatStore;
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, Wtf.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: 'autopayment',
                isEdit: this.isCopyReceipt ? false : this.isEdit
            }
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatCombobox = new Wtf.form.FnComboBox({
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            disabled: (this.isEdit && !this.isCopyReceipt? true : false),
            anchor: '85%',
            typeAhead: true,
            forceSelection: true,
            allowBlank: false,
            name: 'sequenceformat',
            hiddenName: 'sequenceformat',
            id:"sequenceformat"+this.heplmodeid+this.id,
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
        if(this.isReceipt){
            comp = Wtf.getCmp('receiptReport');
        }else {
            comp = Wtf.getCmp('paymentReport') ;
        }
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
                billid:response.paymentid,  //Send billid from Entry Form email button
                billingEmail:response.billingEmail,
                billdate:this.creationDate.getValue(),
                personname:this.Name?this.Name.getRawValue():this.grid.getGLAccountNames()
            }
        }
        return paymentRecord;
    },
    onGridDataChanged: function(){        
        this.updateAmount(getRoundedAmountValue(this.totalAmountWithTDS()));
        this.updateBalanceAmount();
        this.updateSouthTemp();
        this.grid.updateTermDetails();
    },
    setcustomdata:function(grid){
        if(this.lineRec){
           WtfGlobal.setCustomFieldValueAfterStoreLoad(grid,this.moduleId,this.lineRec);
        }
    },
    onPaymentMethodChange: function(comboboxObject,newValue,oldValue){
        if(this.isEdit){
            
            var newrec = WtfGlobal.searchRecord(this.pmtStore, newValue, "methodid");
            var newCurrency=newrec.data.acccurrency;
            var newMethodType=newrec.data.detailtype;
            var newMethodAccountID=newrec.data.accountid;
            var newMethodAccountName=newrec.data.accountname;
            
            var oldrec = WtfGlobal.searchRecord(this.pmtStore, oldValue, "methodid");
            var oldCurrency=oldrec.data.acccurrency;
            var oldMethodType = oldrec.data.detailtype;
            var oldMethodAccountID = oldrec.data.accountid;
            
                /* ERP-16095
                 * Flag isBankToBankMethodChange will be true if user changes One payment method to another and both are of bank type.
                 * This flag will be undefined in created new case. And either true or false in copy and edit case. 
                 */
                var isBankToBankMethodChange=false;
                if(newMethodType == Wtf.bank_detail_type && newMethodType==oldMethodType){
                       isBankToBankMethodChange=true; 
                }
                if(newCurrency==oldCurrency){
                    this.ShowCheckDetails(comboboxObject, newrec,isBankToBankMethodChange);
                }
                
            if(newMethodType == Wtf.bank_detail_type && this.SouthForm){//If method account id get changed then need to restore cheque sequence format
                if(this.paymentAccountInEditRecord==newMethodAccountID){//Selected payment method same as first time loaded account then we need to set earlier sequenceformat and chequeno 
                    this.SouthForm.checkNo.setValue(this.chequeNumber);
                    this.SouthForm.loadChequeSequenceFormatOnMethodAccountChanged(newMethodAccountID,false);
                } else if(newMethodAccountID!=oldMethodAccountID){//Selected payment method account different from newly selected payment account method. then we need to set new sequenceformat and chequeno 
                    this.SouthForm.loadChequeSequenceFormatOnMethodAccountChanged(newMethodAccountID,true);
                }
                if (newMethodAccountName != undefined) {
                    this.SouthForm.setBankName(newMethodAccountName);
                }
             }
        } else {
            this.getConfirmationToChangePmtMethod(comboboxObject,newValue,oldValue);
        }
    },
    reloadGridOnCurrencyChange:function(){
        Wtf.Ajax.requestEx({
            url: "ACCVendorPaymentNew/reloadGridOnCurrencyChange.do",
            params: {
                newcurrency:this.Currency.getValue(),
                paymentwindowtype:(this.record)?this.record.data.paymentwindowtype:this.paymentType,
                billid:this.record.data.billid,
                date:WtfGlobal.convertToGenericDate(this.creationDate.getValue()),
                isCopyTransaction:this.isCopyReceipt
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
            upperLimitDate: WtfGlobal.convertToGenericDate(this.creationDate.getValue()),
            residentialstatus:record.data.residentialstatus,
            deducteetype:record.data.deducteetype,
            deducteetypename:record.data.deducteetypename,
            isTDSapplicableonvendor:record.data.isTDSapplicableonvendor,
            natureOfPayment:record.data.natureOfPayment,
            deductionReason : record.data.deductionReason
        }
        return parameters;
    },
    onDateChange:function(a,val,oldval){
        if(this.isGST){
            this.applyGSTFieldsBasedOnDate();
        }
        var isDateValid = false;
        if(this.getNewestTransactionDate(val,oldval)){
            /**
             * If date is valid than only we have to calculate and assign discount. ERM-981
             */
            isDateValid = true;
            if(this.Currency.getValue()==WtfGlobal.getCurrencyID() && this.isEdit){ //when tranaction in base currency for all cases (edit,copy, create new)
//                this.reloadGridOnCurrencyChange();
                this.changeCurrencyStore(a,val,oldval);
            } else if((this.isEdit && !this.isCopyReceipt) && Wtf.account.companyAccountPref.retainExchangeRate){ //edit case: when user want to retain exchange rate     
                this.assignDiscountAfterCalculation();
                return;                                       
            } else if(this.isEdit || this.isCopyReceipt) { //1.Edit case when user do not want to retain exchange rate 2.copy case
                 Wtf.Msg.show({
                    title:WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg: WtfGlobal.getLocaleText("acc.invoice.exchangeRateMsg"),
                    buttons: Wtf.Msg.YESNO,
                    scope:this,
                    width:600,
                    fn: function(btn){
                        if(btn=="yes"){
                            this.reloadGridOnCurrencyChange();
                            this.changeCurrencyStore(a,val,oldval);
                        } else{
                            this.creationDate.setValue(oldval);
                            this.setChequeDateAccordingly();
                            this.assignDiscountAfterCalculation();
                            return;
                        }
                    },
                    icon: Wtf.MessageBox.QUESTION
                });
            } else { //Normal Create New Case
                 this.changeCurrencyStore(a,val,oldval);
            }            
            this.setChequeDateAccordingly();
            if (Wtf.isTDSApplicable) {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.mp.dataWillBeClearedOndateChange"), function (btn) {
                    if (btn != "yes") {
                        return;
                    } else {
                        //Reset Grid.
                        this.toggleButtonRelease();
                    }
                }, this);
            }
        }
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
            var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.creationDate.getValue()).clearTime());
            if (!isTaxShouldBeEnable) {
                this.grid.getStore().removeAll();
                this.grid.addBlankRow();
            }
        }
        if(isDateValid){
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
                    gridStore.data.items[cnt].data.discountname = discount;
                    if (discount == 0) {
                        gridStore.data.items[cnt].data.amountdueafterdiscount = getRoundedAmountValue(gridStore.data.items[cnt].data.amountdue);
                    } else {
                        gridStore.data.items[cnt].data.amountdueafterdiscount = getRoundedAmountValue(gridStore.data.items[cnt].data.amountdue - discount);
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
         panel.on("update",function() {             // If exchange rate is added for particular date, again request will be sent to server to fetch exchange rate at that Date (i.e. exchange rate added just now)
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
            /**
             * As time zone issue occuring  
             * No need to convert date to millisecond directly compared dates with date object Ticket no:-SDP-15986
             */
//            var invoiceDate = getTimeEcludingBrowsertimezone(rec.data.date);
            var invoiceDate = new Date(rec.data.date);
            invoiceDate.setHours(0,0,0,0);
            var isOpeningBalanceTransaction = ((typeof rec.data.isOpeningBalanceTransaction === "string") ? ((rec.data.isOpeningBalanceTransaction == "true") ? true : false) : rec.data.isOpeningBalanceTransaction);
            if(rec.data.type==2 || rec.data.type==3){
                if((enteredDate< invoiceDate) && (!isOpeningBalanceTransaction)){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlder")], 2);
                    this.creationDate.setValue(oldval);
                    valid=false;
                    break;
                }
            }
            /**
             * If isPostingDateCheck is true then checking wether enteredDate is less than jePostingDate if yes displaying "Date cannot be older than the JE posting date of transactions loaded in grid"
             */
            if(CompanyPreferenceChecks.isPostingDateCheck()){
//                var jePostingDate = getTimeEcludingBrowsertimezone(rec.data.jeDate);
                var jePostingDate = new Date(rec.data.jeDate);
                jePostingDate.setHours(0,0,0,0);
                if (rec.data.type == 2 || rec.data.type == 3) {
                    if ((enteredDate < jePostingDate) && (!isOpeningBalanceTransaction)) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlderThenJEPosting")], 2);
                        this.creationDate.setValue(oldval);
                        valid = false;
                        break;
                    }
                }
            }
            /*
             * For Malaysian country, payment date cannot be older than claim date of any invoice that loaded in grid
             */
            if(rec.data.type==2 && Wtf.account.companyAccountPref.countryid==Wtf.Country.MALAYSIA){ 
                var claimedDate=new Date(rec.data.claimedDate);
                claimedDate.setHours(0,0,0,0);
                if((rec.data.claimedDate != undefined) && (rec.data.claimedDate!='') && (enteredDate< claimedDate)){
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
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.creationDate.getValue())}});
        this.currencyStore.on('load',this.changeTemplateSymbol.createDelegate(this,[oldval]),this);
    },
    changeTemplateSymbol:function(oldval){
        if(this.currencyStore.getCount()==0){
            this.currencyStore.purgeListeners();
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.10")], 2);
             this.creationDate.setValue(oldval);
             this.changeCurrencyStore();
        }
        else {
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
    setSequenceFormatForCreateNewCase :function(){
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
                this.updateExternalCurrencyRateOnCurrencyChange();
                this.clearStore();
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
            this.paymentCurrencyBeforeSelect = this.Currency.getValue();
        }
    },
    onPaymentMethodSelect: function(combobox,rec,index){
        this.updateTemplateCurr();
//    this.chechSequenceFormatExist();
    /*if(!this.isEdit ){
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
            var currencyForSelectedPaymentMethod= pmtMethodRecord.data['acccurrency'];
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
       var currencyForSelectedPaymentMethod= pmtMethodRecord.data['acccurrency'];
       
       if(currencyForSelectedPaymentMethod == WtfGlobal.getCurrencyID() || !(this.isMultiCurrencyPayment()) || this.readOnly || (Wtf.account.companyAccountPref.makePayment[10] != undefined && Wtf.account.companyAccountPref.makePayment[10].isReadOnly)){
           this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.disable();
       }else {
           this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.enable();
       }
   },
//   chechSequenceFormatExist:function(onFirstLoad,pmtMethodRecord){
//       var rec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
//       Wtf.Ajax.requestEx({
//            url: "ACCVendorPayment/getChequeSequenceFormatList.do",
//            params: {
//                bankAccountId:rec.data.accountid,
//                companyid:companyid
//            }
//        },this,function(req, res) {
//            var restext = req;
//            if (restext.success) {
//               this.isSequenceformat=restext.isSequenceformat;
//               if(onFirstLoad){//method checks the bank type of payment method either it is cash type or bank type
//                   this.ShowCheckDetails(this.pmtMethod, pmtMethodRecord);
//               }
//            } 
//        }); 
//    },
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
            isReceipt: false,
            isCustomer: this.isCustomer,
            currencyfilterfortrans: this.Currency.getValue(),
            personInfo:this.getPersonInformation(),
            isEdit:this.isEdit,
            billid: (!Wtf.isEmpty(this.record) && !Wtf.isEmpty(this.record.data)) ? this.record.data.billid : '',     //ERP-41122
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
validateForIBGTypePayment:function(){
    var isIBGTransaction=null;
    /*
     * For edit/copy case, for the first time , system will check whether transaction is IBG type or not from payment method
     * Later on, it will check from flag  'this.isIBGTypeTransaction' 
     */
    if(!this.isIBGTransactionCheckedInEditCase && this.isEdit){
        var rec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
        isIBGTransaction = rec.data.isIBGBankAccount;
    } else{
        isIBGTransaction = this.isIBGTypeTransaction;
    }
    if(isIBGTransaction){
        if(this.ibgDetailsID==null || this.ibgDetailsID == ''){ 
            return false;
        }else{
            return true;
        }
    }else{
        return true;
    }
},
totalAmountWithTDS:function(){
    var amount=0;
    if(Wtf.account.companyAccountPref.countryid ==Wtf.Country.INDIA &&  Wtf.isTDSApplicable){
        amount=this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount()-this.grid.getMultiTDSAmount()
    }else{
        amount=this.grid.getMultiDebitAmount()+this.grid.getMultiDebitTaxAmount()
    }
    return amount;
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
            this.grid.getView().refresh();  //ERP-36023 sequence no. of documents on line level grid not updating
            this.grid.fireEvent('datachanged', this);
        },this);
    } else {
        for (rowindex = 0; rowindex < selectedCount; rowindex++) {
                arr[rowindex] = this.grid.selectionModel.getSelections()[index];
                if (arr[rowindex] != "") {
                    var id = arr[rowindex].id;
                    store.remove(store.getById(id));
                }
            }
            this.grid.getView().refresh();  //ERP-36023 sequence no. of documents on line level grid not updating
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
