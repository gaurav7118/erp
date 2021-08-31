/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


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


Wtf.ReceiptEntry = function(config) {
    this.isReceipt = config.isReceipt;
    this.moduleid = config.moduleId;
    this.modeName = config.modeName;
    this.helpmodeid = config.helpmodeid;
    this.isEdit = config.isEdit;
    this.readOnly = config.readOnly;
    this.record = config.record;
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
      // map default payment method to customer check
    this.mapDefaultPmtMethod = CompanyPreferenceChecks.mapDefaultPaymentMethod();
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
    this.recArray=config.recArray;
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
        if(this.mapDefaultPmtMethod){
           this.setPMData();//ERM-735 Associate default Payment Method for customer
    }
      if (this.loadCurrStore && Wtf.account.companyAccountPref.currencyid) {
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
        disabled: true,
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
    //this.pmtMethod.on('select', this.onPaymentMethodSelect, this);// Event is added for setting the currency of the payment method account again , after changing the currency from currency combobox
//    this.pmtMethod.on('beforeselect', this.onPaymentMethodBeforeSelect, this);
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
        disabled:true,
        emptyText: WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
        anchor: '85%'
    });

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
        parentcompId:this.id
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
                        items: [this.sequenceFormatCombobox, this.No, this.creationDate, this.paidTo]
                    }, {
                        layout: 'form',
                        columnWidth: 0.33,
                        id: this.id + 'MiddleColumnform',
                        items: [this.Currency, this.paymentCurrencyToPaymentMethodCurrencyExchangeRate,this.Amount]
                    }, {
                        layout: 'form',
                        columnWidth: 0.33,
                        items: [this.pmtMethod, this.pmtMethodAcc, this.Memo]
                    }]
            }, this.tagsFieldset]
    });
    this.createGrid();

    

    this.appendID = true;

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
        //panelItemsArra.push(this.toggleBtnPanel);
    }
    panelItemsArra.push(this.grid);
   // panelItemsArra.push(this.southPanel);
    
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
                text: WtfGlobal.getLocaleText("acc.common.bulkPayment"), //'Create Bulk Payment'
                tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
                scope: this,
                id: "save" + config.helpmodeid + this.id,
                hidden: this.readOnly,
                iconCls: getButtonIconCls(Wtf.etype.save),
                handler: function() {
                 
                    this.saveAndCreateNewFlag = false;                    // This flag is used to differentiate between Save button and Save and Create New button
                    this.save();
                }
            }),]
    });
    this.Amount.on('focus', function(field) {
        if (field.getValue() == 0) {
            field.setValue("");
        }
    }, this);
    
    this.Amount.on('blur',this.onAmountFieldBlur,this);
    this.grid.on('datachanged', this.onGridDataChanged, this);
    this.creationDate.on('change',this.onDateChange,this);  
   
    //this.Currency.on('change',this.onCurrencyChange,this);
    Wtf.ReceiptEntry.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true
    });
}

Wtf.extend(Wtf.ReceiptEntry, Wtf.account.ClosablePanel, {
    onRender: function(config) {
        Wtf.ReceiptEntry.superclass.onRender.call(this, config);
        if(!this.readOnly){
            this.isClosable= false;         // This flag is used to give an alert if user is closing the form without saving the data. In view case, alert will not be given.
        }
        if(this.sequenceFormatStore.getCount() > 0){
            this.setNextNumber();
        } else {
            this.sequenceFormatStore.on('load', this.setNextNumber, this);
        }
        chkReceiptSeqFormatStoreLoad();
    
         if(this.isBulkPayment){
            var cnt=0;
            var maxDate;
            var date;
            var currentDate=new Date();
            var checkDate=false;
            var recordArr= eval(this.invObj.getSelectedRecords());
           // this.Name.setValForRemoteStore(recordArr[0].personid,recordArr[0].personname);
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
         
        //this.hideFormFields();
    },

    
    loadForm: function(rec) {
        var recData = rec.data;
        var recDetails = recData.Details;
        this.presonCode=recData.personcode;  // IN edit case, vendor/customer store is not loaded. So, for getting vendor/customer code, we have set this variable in edit case.
        /*
            In copy case no need to send billid otherwise it will be consider as edit case
        */
        if(!this.isCopyReceipt){
           this.setHiddenFieldValues(recData);
        }
//        if(this.nonRefundable!=undefined){
//            this.nonRefundable.setValue(recData.nonRefundable);
//        }
        this.setComboValues(recData);
      
        this.NorthForm.form.loadRecord(rec);
//        this.pmtMethod.setValForRemoteStore(recData.pmtmethod, recData.paymentmethodname,recData.hasAccess);
        /*
         *  Currency store load to get exchange rates on billdate in the south panel.  
         */

        if(this.SouthForm) {
            this.SouthForm.form.loadRecord(rec.data.paydetail);
            this.SouthForm.bank.setValForRemoteStore(rec.data.paydetail.data.paymentthroughid, rec.data.paydetail.data.paymentthrough);
            this.SouthForm.setOtherFieldInfoOnRecordLoad();
        }
        this.loadLineDetails(recDetails);
  
    
           this.enableDisablePaymentCurrencyToPaymentMethodCurrencyRate(); 
        if (this.isAllowedSpecificFields) {
            this.disableField();
        } 
    },
    
    setComboValues: function(recData) {
      
        if(recData.sequenceformatid!='') {
            this.sequenceFormatCombobox.setValForRemoteStore(recData.sequenceformatid,recData.sequenceformatvalue);
            if(!this.isCopyReceipt){                 // Sequence format combo will be disable in edit case but will be enabled in Copy case
                this.sequenceFormatCombobox.disable();
                this.No.disable();
            }
            
        } else {
            this.sequenceFormatCombobox.setValForRemoteStore("NA","NA");
           
                this.sequenceFormatCombobox.disable(); // In Edit case, user can not change sequence format but able to change payment number in 'NA' sequence format
                if (this.readOnly) {
                    this.No.disable();
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
  
        this.grid.addBlankRow();
    },


    addPaymentMethod: function() {
        PaymentMethod('PaymentMethodWin');
        Wtf.getCmp('PaymentMethodWin').on('update', function() {
            this.pmtStore.reload();
        }, this);
    },
    setNextNumber: function(config) {
        if (this.sequenceFormatStore.getCount() > 0) {             

            this.setSequenceFormatForCreateNewCase();      // Create new case              
        }


    },
    createGrid: function() {
        this.grid = new Wtf.bulkPaymentGrid({
            region: 'center',
            border: true,
            invObj:this.invObj,
            isBulkPayment:this.isBulkPayment,
            currencyid: this.currencyid,
            paymentType: this.paymentType,
            paymentOption: this.paymentOption,
            isEdit: this.isEdit,
            symbol:WtfGlobal.getCurrencySymbol(),
            parentObj: this,
            moduleid: this.moduleid,
            paymentDetailsRecord:this.paymentDetailsRecord,
            loanFlag:this.loanFlag,
            isCustomer:this.isCustomer,
            readOnly:this.readOnly,
            height: 200,
            receiptEntryObject: this,
            isAllowedSpecificFields:this.isAllowedSpecificFields,
            cls: 'gridFormat',
            isMultiDebit: this.isMultiDebit,
            amount: 0,
            id: this.id + 'customergrid',
            closable: true,
            billid:(this.record!= undefined)?this.record.data.billid:'',
            isReceipt: this.isReceipt,
            recArr:this.recArray
        });
     
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

    applyTemplate: function(store, index) {
        var isedit = this.Currency.getValue() != WtfGlobal.getCurrencyID() && this.Currency.getValue() != "";
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if (this.externalcurrencyrate > 0) {
            exchangeRate = this.externalcurrencyrate;
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
    

    getExchangeRate: function() {
        var index = this.getCurrencySymbol();
        var rate = this.externalcurrencyrate;
        var revExchangeRate = 0;
        if (index >= 0) {
            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
            if(this.externalcurrencyrate!=undefined&&this.externalcurrencyrate>0) {
                exchangeRate = this.externalcurrencyrate;
            }
            revExchangeRate = 1 / (exchangeRate);
            revExchangeRate = (Math.round(revExchangeRate * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
        }
        return revExchangeRate;
    },


    addPaidTo: function() {
        addMasterItemWindow('18');
        Wtf.getCmp("masterconfigurationonly").on('update', function() {
            this.paidTo.store.reload();
        }, this);

    },
    
   save: function() {
//        if(this.isBulkPayment){
//            this.updateBalanceAmount();
//        }
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
     
    
        isValidCreditAndDebitDetails = true;//this.grid.isValidCreditAndDebitDetails();
        if(!isValidCreditAndDebitDetails){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rp.alertCreditDebitDetailsInvalid")],2);
            return
        }
        if(getRoundedAmountValue(this.Amount.getValue())==0 ){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.totalAmtCanNotBeZero")],2);
                return
        }
        /*
           *Check for Total amount of selected transactions does not match to  amount  you have entered.
         */
        if(this.paymentType != this.paymentOption.AgainstGL){
            var amount=this.grid.getMultiDebitAmount();
            if(this.Amount.getValue()<amount){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.totalAmountDoesNotMatchToAmount")],2);
                return;
            }
        }

        this.saveData();  
    },
    
    saveData: function() {
        Wtf.getCmp(this.id + 'requiredfieldmessagepanel').hide();
        
        /*-------- Validate form before save------------ */
        var isFormValid = this.formValidationBeforeSave()
        if (!isFormValid) {
            return;
        }
        
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"), WtfGlobal.getLocaleText("acc.je.msg1"), function(btn) {
            if (btn != "yes") {
                this.isChequePrint = false;
                this.checkWarning=true;
                return;
            }

            var jsonObject = this.NorthForm.form.getValues();
            WtfGlobal.onFormSumbitGetDisableFieldValues(this.NorthForm.form.items, jsonObject);
            var custFieldArr = this.tagsFieldset.createFieldValuesArray();
            if (custFieldArr.length > 0)
                jsonObject['customfield'] = JSON.stringify(custFieldArr);
            jsonObject['Details'] = this.grid.getData();

            jsonObject['paydetail'] =  this.grid.getData();
              jsonObject['postdate'] =WtfGlobal.convertToGenericDate(new Date());
            jsonObject['balaceAmount'] =this.finalBalanceAmount;
            jsonObject['accountIdComPreAdjRec'] =Wtf.account.companyAccountPref.adjustmentAccountReceipt;
            jsonObject['iscustomer'] = Wtf.getCmp('isCustomer' + this.helpmodeid + this.id).getValue();
            jsonObject['isChequePrint']=this.isChequePrint;
            if(this.externalcurrencyrate!=undefined&&this.externalcurrencyrate>0) {
                jsonObject['externalcurrencyrate'] = this.externalcurrencyrate;
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
            //this.savencreateBttn.disable();
            WtfComMsgBox(27, 4, true);
            this.ajxUrl = this.getUrlForSave();
            WtfGlobal.setAjaxTimeOut();             // ERP-12447  increse the request timeout to avoid duplicate entries
            Wtf.Ajax.requestEx({
                url: this.ajxUrl,
                params: jsonObject
            }, this, this.genSavePaymentSuccessResponse, this.genSavePaymentFailureResponse);
        }, this);
    },
    getUrlForSave: function() {
        var url = "ACCReceiptNew/saveBulkCustomerReceipt.do";
        if(this.isAllowedSpecificFields){
            url="ACCReceiptNew/updateReceipt.do";
        }
        return url
    },
    genSavePaymentSuccessResponse: function(response, request) {
        WtfGlobal.resetAjaxTimeOut();            // ERP-12447  reset request timeout to default
    
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
        /*Save Case*/
        if (response.success) {
             this.isClosable = true;
            this.disableComponents();
        } else {
            this.enableSaveButton();
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
            }
        
        }
        if(this.isBulkPayment){
            /*
             *rowIndex is used for payment method cash
             */
            rowIndex = this.pmtStore.find("methodname","cash");
            rec = this.pmtStore.getAt(rowIndex);                
            methodid = rec.get('methodid');
            this.pmtMethod.setValue(methodid);
            var methodAccindx = this.pmtStore.find('methodid', rec.data['methodid']);
            var paymentMethodAccountId = "";
            if (methodAccindx != -1) {
                this.pmtMethodAcc.setValue(this.pmtStore.getAt(methodAccindx).get("accountname"));
                paymentMethodAccountId = this.pmtStore.getAt(methodAccindx).get("accountid");
            }
            if(this.mapDefaultPmtMethod && this.isCustomer){  //Associate default payment method to customerERM-735
                var defaultPaymentMethod= this.invObj.pmtMethod.getValue();
                if(defaultPaymentMethod !="all" && defaultPaymentMethod !=""){
                    this.pmtMethod.setValue(defaultPaymentMethod);
                    var methodAccindx = this.pmtStore.find('methodid', defaultPaymentMethod);
                    var paymentMethodAccountId = "";
                    if (methodAccindx != -1) {
                        this.pmtMethodAcc.setValue(this.pmtStore.getAt(methodAccindx).get("accountname"));
                        paymentMethodAccountId = this.pmtStore.getAt(methodAccindx).get("accountid");
                    }
                    this.onPaymentMethodChange(this.pmtMethod);
                }
            }
          
        }
      
    },
    updateAmount: function(amount) {
        this.Amount.setValue(getRoundedAmountValue(amount));
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
   
    
    disableComponents: function(){
        if(this.saveBttn){
            this.saveBttn.disable();
        }
    
        if(this.grid){
            this.grid.disable();
        }
        if(this.NorthForm){
            this.NorthForm.disable();
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
        Wtf.getCmp("printSingleRecord" + this.id).enable();
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

    onGridDataChanged: function(){
        this.updateAmount(getRoundedAmountValue(this.grid.getMultiDebitAmount()));     
    },
    onPaymentMethodChange: function(comboboxObject,newValue,oldValue){
        
        
         var selectedMethod = comboboxObject.value;
        var index = this.pmtStore.findBy(function(rec) {
            var methodId = rec.data['methodid'];
            if (methodId == selectedMethod)
                return true;
        }, this);
        var record = this.pmtStore.getAt(index);
        
        if (record != undefined) {
            this.ispPaymentMethodChange = true;
            this.paymentMethodType = record.data.detailtype;
        }
          var methodAccindx = this.pmtStore.find('methodid', record.data['methodid']);
        var paymentMethodAccountId = "";
        if (methodAccindx != -1) {
            this.pmtMethodAcc.setValue(this.pmtStore.getAt(methodAccindx).get("accountname"));
            paymentMethodAccountId = this.pmtStore.getAt(methodAccindx).get("accountid");
        }
        
        var accCurr = record.data['acccurrency'];
        /*
         * ERP-16098
         * Below code block will be exexuted in create new case only
         * For edit and copy cases, payment method change is allowed only with same currency. So grid currency, original excghange rate will not be changes=d in copy and edit case.
         */
      
        var index = this.currencyStore.findBy(function(rec) {
            var currid = rec.data['currencyid'];
            if (currid == accCurr)
                return true;
            else
                return false
        }, this);

        if (index != -1) {
            this.Currency.setValue(record.data['acccurrency']);
        }
    
        this.updateExternalCurrencyRateOnCurrencyChange();
                            
               
        this.grid.updateConfig(this.ispPaymentMethodChange,this.paymentMethodType,paymentMethodAccountId,this.pmtStore.getAt(methodAccindx).get("accountname"),record,this.externalcurrencyrate);
        
    },
  
    
    onDateChange:function(a,val,oldval){
        if(this.getNewestTransactionDate(val,oldval)){            
            //Normal Create New Case           
            this.changeCurrencyStore(a, val, oldval);
            WtfGlobal.assignDiscountAfterCalculation(this);
            // this.setChequeDateAccordingly();
        }
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
            var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.creationDate.getValue()).clearTime());
            if (!isTaxShouldBeEnable) {
                this.grid.getStore().removeAll();
                this.grid.addBlankRow();
            }
        }
    },
  
    enableSaveButton: function(){
        this.saveBttn.enable();
       
    },
     getNewestTransactionDate: function(val,oldval){
        var enteredDate = val;
        var valid=true;
        for(var i=0;i<this.grid.store.getCount();i++){
            var rec= this.grid.store.getAt(i);
            /*
             * Removed opening invoice check as 
             */
//            if(rec.data.type==2 || rec.data.type==3){
//                var isOpeningBalanceTransaction = ((typeof rec.data.isOpeningBalanceTransaction === "string") ? ((rec.data.isOpeningBalanceTransaction == "true") ? true : false) : rec.data.isOpeningBalanceTransaction);
                if((enteredDate.getTime()<new Date(rec.data.invoicedate).getTime())){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlder")], 2);
                    this.creationDate.setValue(oldval);
                    valid=false;
                    break;
                }
//            }
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
            //this.updateSouthTemp();
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

    updateExternalCurrencyRateOnCurrencyChange : function(){
        var currencyRecord = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
        this.externalcurrencyrate = currencyRecord.data['exchangerate'];
    },

//    onPaymentMethodSelect: function(combobox,rec,index){
//     this.updateTemplateCurr();
// 
//    },
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
       
       if(currencyForSelectedPaymentMethod == WtfGlobal.getCurrencyID() || !(this.isMultiCurrencyPayment()) || this.readOnly){
           this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.disable();
       }else {
           this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.enable();
       }
   },   

onAmountFieldBlur:function(obj){
    if(obj.getValue().toString() == "NaN" || obj.getValue() == '' || obj.getValue() == undefined || obj.getValue()== null){
        obj.setValue(0);
    }
},

formValidationBeforeSave : function(){
    
  var isValid=this.grid.formValidationBeforeSave();  
     return isValid;
},

});
