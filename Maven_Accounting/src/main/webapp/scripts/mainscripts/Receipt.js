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


function openInv(isTran,isCustBill,vendor){
//	vendorid: Wtf.getCmp("account"+config.helpmodeid+this.id).getValue();			//Neeraj

    if(isTran && isCustBill)
        callBillingInvoice(false,null);		// Without Inventory
    else if(isTran)
        callInvoice(false,null);			// Customer Invoice
    else
        callGoodsReceipt(false,null,null,vendor,isCustBill);		// Vendor Invoice
}
/*< COMPONENT USED FOR >
 *      1.Receive Payments
 *          callReceipt() --- < Receive Payments >
 *          [isReceipt:true]
 *      2.Receive Payments
 *          callBillingReceipt() --- < >
 *          [isReceipt:true, isBillReceipt:true]
 *      3.Make Payment
 *          callPayment() --- < Make Payment >
 *          [isReceipt:false]
 *
 *      4.this.appendId --- It is used when this.id is appended in the id of component. This is useful for displaying help.
 */

Wtf.account.OSDetailPanel=function(config){
    this.isReceipt=config.isReceipt;
    this.isMultiDebit=false;
    this.loadCurrStore = true;
    this.contraentryflag = false;
    this.onCheckLifoFifoFlage = false;
    this.val="2";
    this.mailFlag = false;
    this.response = "";
    this.request = "";
    this.userds="";
    this.heplmodeid = config.helpmodeid;
    this.directPayment=config.directPayment?config.directPayment:false;
    this.invoiceRecord=config.invoiceRecord;
    this.isEdit=config.isEdit;
    this.isCopyReceipt=config.isCopyReceipt;
    this.setEditableData=false;
    this.record=config.record;
    this.gridObject=config.gridObject;
    this.actualReceiptType=(config.isEdit!=undefined&&config.isEdit&&config.record!=undefined&&config.record.data!=undefined&&config.record.data.receipttype!=undefined)?config.record.data.receipttype:this.val;
    this.id=config.id;
    this.personwin=false;
    this.symbol=null;
    this.currencyid=null;
    this.bankAccCurrency=null;
    this.bankAccSelected=false;
    this.isCustBill=config.isCustBill;
    this.businessPerson=(config.isReceipt?"Customer":"Vendor");
    this.transectionName=config.isReceipt?"Receipt":"Payment";
    this.label=config.isReceipt?"Invoice":"Vendor Invoice";
    this.masterGroup=config.isReceipt?12:10;
    this.isReceipt ? Wtf.RPReceivedFromStore.load() : Wtf.MPPaidToStore.load();
    this.uPermType=config.isReceipt?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isReceipt?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.amtDue=0;
    this.accEndingBalance=0;
    this.moduleid= config.moduleId;
    this.modeName = config.modeName;
    this.exchangeratetype="";
    this.revexternalcurrencyrate=0;
    this.readOnly=config.readOnly;
    this.isCopyReceipt=config.isCopyReceipt;
    this.isIBGTypeTransaction = false;
    this.isLinkedToClaimedInvoice = false;
    this.ibgDetailsID = "";
    this.paymentCriteria = 1; // '1' for 'NA'
    this.autopopulate=false;
    this.uPermType=(this.isReceipt?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.permType=(this.isReceipt?Wtf.Perm.invoice:Wtf.Perm.vendorinvoice);
    this.exportPermType=(this.isReceipt?this.permType.exportdatareceipt:this.permType.exportdatapayment);
    this.printPermType=(this.isReceipt?this.permType.printreceipt:this.permType.printpayment);
    this.emailPermType=(this.isReceipt?this.permType.emailreceipt:this.permType.emailpayment);    
    
    this.pmtRec = new Wtf.data.Record.create([
        {name: 'methodid'},
        {name: 'methodname'},
        {name: 'accountid'},
        {name: 'acccurrency'},
        {name: 'accountname'},
        {name: 'isIBGBankAccount', type:'boolean'},
        {name: 'isdefault'},
        {name: 'detailtype',type:'int'},
        {name: 'acccustminbudget'},
        {name: 'autopopulate'},
    ]);
    this.pmtStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.pmtRec),
        url : "ACCPaymentMethods/getPaymentMethods.do",
        baseParams:{
            mode:51
        }
    });
    
    this.pmtStore.on('load',this.setPMData,this);
    this.pmtStore.load({params:{grouper:'paymentTrans'}});
    
    config.isReceipt?chkcustaccload():chkvenaccload();
    this.personRec = new Wtf.data.Record.create ([
        {name:'accid'},
        {name:'acccode'},
        {name:'accountid'},
        {name:'accname'},
        {name:'currencyid'},
        {name:'isVendor'},
        {name:'groupname'},
        {name:'paymentCriteria'}
    ]);


    this.personAccStore =  new Wtf.data.Store({
        url : "ACCAccountCMN/getAccountsForCombo.do",
        baseParams:{
            nondeleted:true
        },
        reader: new  Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad:false
        },this.personRec)
    });

    this.creationDate=new Wtf.form.DateField({
        name:"creationdate",
        format:WtfGlobal.getOnlyDateFormat(),
        value:Wtf.serverDate,
        disabled: this.readOnly,
        id:"date"+config.helpmodeid+this.id,
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.date"), //"Date*",
        anchor:'85%'
    });
     this.currencyRec = new Wtf.data.Record.create([
        {name: 'currencyid',mapping:'tocurrencyid'},
        {name: 'symbol'},
        {name: 'currencyname',mapping:'tocurrency'},
        {name: 'exchangerate'},
        {name: 'htmlcode'}
     ]);
     this.currencyStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.currencyRec),
        url:"ACCCurrency/getCurrencyExchange.do"
     });
     this.currencyStore.on("load",function(store){
         if(this.isEdit) {
            this.Currency.setValue(this.record.data.currencyid);
            this.symbol = this.record.data.currencysymbol;
         } else if(this.directPayment) {
            this.Currency.setValue(this.invoiceRecord.data.currencyid);
         } else if(this.loadCurrStore && Wtf.account.companyAccountPref.currencyid){
             
             if(this.pmtMethod!=undefined&&this.pmtMethod.getValue()!=undefined){
                var record=WtfGlobal.searchRecord(this.pmtStore,this.pmtMethod.getValue(),"methodid");
                if(record!=null&&record!=undefined&&record.data!=undefined&&record.data.acccurrency!=undefined){
                    this.Currency.setValue(record.data.acccurrency);
                    this.loadCurrStore = false;
                }else{
                    this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
                    this.loadCurrStore = false;
                }
            }else{
                this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
                this.loadCurrStore = false;
            }
             
         }else if(this.bankAccSelected){             
           this.Currency.setValue(this.bankAccCurrency);             
         }else if(this.Currency.getValue()=="" || this.Currency.getValue()==undefined){
           this.Currency.setValue(WtfGlobal.getCurrencyID());  
         }
         this.fetchPaymentAccBalance();
         
        if(this.Currency.getValue()!=WtfGlobal.getCurrencyID() && store.getCount()<=1){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.10")],2);
        } else {
            var index = 3;
            if(this.Currency.getValue()!=""){
                var FIND = this.Currency.getValue();
                index = this.currencyStore.findBy( function(rec){
                        var parentname=rec.data['currencyid'];
                        if(parentname==FIND)
                            return true;
                        else
                            return false
                    })
            }
            this.applyTemplate(this.currencyStore, index);
            this.updateSouthTemp();
        }       
     },this)
     
     this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'value'
        },
        {
            name: 'oldflag'
        }
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName
            }
        });
        
     this.sequenceFormatStore.on('load',this.setNextNumber,this); 
     this.sequenceFormatStore.load();
     this.currencyStore.load({params:{grouper:'paymentTrans',mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});
     
     var currencyHelp = WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.SelectaCurrencytoprocesstransactionofrequiredcurrency"));
     
     this.Currency= new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.cur") + currencyHelp,  //'Currency*',
        hiddenName:'currencyid',
        id:"currency"+config.helpmodeid+this.id,
        anchor: '85%',
        allowBlank:false,
        store:this.currencyStore,
        disabled:true,//this.isEdit,
        valueField:'currencyid',
        forceSelection: true,
        displayField:'currencyname',
        scope:this,
        selectOnFocus:true
    });
      this.ShowOnlyOneTime= new Wtf.form.Checkbox({
        name:'ShowOnlyOneTime',
        fieldLabel:WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime"),
        id:'ShowOnlyOneTime'+this.heplmodeid+this.id,
        checked:false,
        hideLabel:(this.isEdit != undefined  && !config.isReceipt) ||this.isEdit,
        hidden:( (this.isEdit != undefined) && !config.isReceipt) || this.isEdit,
        cls : 'custcheckbox',
        width: 10
    });  
      this.ShowOnlyOneTime.on('check',function(obj,isChecked){
                this.Name.reset();
                this.personAccStore.load();
    },this); 

    var isEditORisCopy=(this.isEdit !=undefined ?this.isEdit:false) || (this.isCopyReceipt !=undefined ?this.isCopyReceipt:false);
    this.personAccStore.on('beforeload', function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = this.personAccStore.baseParams;
            if(isEditORisCopy){
                currentBaseParams.isPermOrOnetime="";
            }else{
                if(this.ShowOnlyOneTime != undefined && this.ShowOnlyOneTime.getValue() == true){
                    currentBaseParams.isPermOrOnetime=true;
                }else{
                    currentBaseParams.isPermOrOnetime=false;
                }
            }
            this.personAccStore.baseParams=currentBaseParams;
        }, this);
        
   this.Name=new Wtf.form.ExtFnComboBox({
        fieldLabel: (config.isReceipt?WtfGlobal.getLocaleText("acc.mp.cus"):WtfGlobal.getLocaleText("acc.mp.ven")),//
        id:"account"+config.helpmodeid+this.id,
        hiddenName:'accid',
        store:this.personAccStore,
        disabled:this.isEdit || this.directPayment,
        valueField:'accid',
        emptyText:WtfGlobal.getLocaleText("acc.mp.selAcc"),  //'Please select an Account...',
        allowBlank:false,
        listWidth : Wtf.account.companyAccountPref.accountsWithCode?550:450,    
        anchor:'85%',
        displayField:'accname',
        forceSelection:true, 
        mode: 'local',
        typeAheadDelay:30000,
        minChars:1,
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
        addNewFn: this.addPerson.createDelegate(this,[false,null, (config.isReceipt? 'Customerwindow' : 'Vendorwindow') , config.isReceipt ],true),
        hirarchical:true//,
    });
    
    this.pmtMethod= new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.payMethod"),
        name:"pmtmethod",
        store:this.pmtStore,
        id:"paymentMethod"+config.helpmodeid+this.id,
        valueField:'methodid',
        displayField:'methodname',
        allowBlank:false,
        disabled: this.readOnly,
        emptyText:(config.isReceipt?WtfGlobal.getLocaleText("acc.rp.recaacc"):WtfGlobal.getLocaleText("acc.mp.selpayacc")),
        anchor:'85%',
        mode: 'local',
        triggerAction: 'all',
        typeAhead: true,
        forceSelection: true//,
    });

    this.pmtMethodAcc=new Wtf.form.TextField({
        name:"pmtmethodacc",
        disabled : true,
        id:"pmtmethodacc"+this.id,
        fieldLabel:WtfGlobal.getLocaleText("acc.field.PaymentAccount"),
        anchor:'85%'
    });
    
this.isChequePrint=false;
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.edit))
        this.pmtMethod.addNewFn=this.addPaymentMethod.createDelegate(this)
    this.pmtMethod.on('select',this.ShowCheckDetails.createDelegate(this),this);
    
this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
//        labelSeparator:'',
//        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStore,
        disabled:(this.isEdit&&!this.isCopyReceipt&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
        //width:240,
        anchor:'85%',
        typeAhead: true,
        forceSelection: true,
        allowBlank:false,
        name:'sequenceformat',
        hiddenName:'sequenceformat',
        listeners:{
            'select':{
                fn:this.getNextSequenceNumber,
                scope:this
            }
        }
            
    });
         
    this.No=new Wtf.form.TextField({
        fieldLabel:config.isReceipt?WtfGlobal.getLocaleText("acc.rp.RecNO"):WtfGlobal.getLocaleText("acc.mp.payNo"),  //this.transectionName+' No*',
        id:"receiptNo"+config.helpmodeid+this.id,
        name: 'no',
//        readOnly:true,
      disabled:this.isEdit,
        anchor:'85%',
        maxLength:45,
        allowBlank:false
    });
    this.bankCharges=new Wtf.form.TextField({
        fieldLabel:WtfGlobal.getLocaleText("acc.field.BankCharges"),
        id:"bankCharges"+config.helpmodeid+this.id,
        name: 'bankCharges',
         maskRe:/[0-9.]/,
         disabled:this.readOnly,
        anchor:'85%',
        maxLength:45
//        allowBlank:false
    });
    this.bankCharges.on('blur',this.setroundedvalue,this);
    this.bankInterest=new Wtf.form.TextField({
        fieldLabel:WtfGlobal.getLocaleText("acc.field.BankInterest"),
        id:"bankInterest"+config.helpmodeid+this.id,
        name: 'bankInterest',
        maskRe:/[0-9.]/,
        disabled:this.readOnly,
        anchor:'85%',
        maxLength:45
//        allowBlank:false
    });
    this.bankInterest.on('blur',this.setroundedvalue,this);
    this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'acccode'},
            {name:'groupname'}
//            {name:'level',type:'int'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
//            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:2,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.accountStore.load();
    this.bankChargesAccount=new Wtf.form.ExtFnComboBox({
                    fieldLabel:WtfGlobal.getLocaleText("acc.field.BankChargesAccount"),
                    hiddenName:'accountid',
                    store:this.accountStore,
                    minChars:1,
                    anchor:'85%',
                    valueField:'accountid',
                    displayField:'accountname',
                    name:'bankChargesCmb',
                    forceSelection:true,
                    hirarchical:true,
                    disabled:this.readOnly,
//                    addNewFn:this.openCOAWindow.createDelegate(this),
                    extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
                    mode: 'local',
//                    typeAheadDelay:30000,
                    extraComparisionField:'acccode',// type ahead search on acccode as well.
                    listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400
                });
     this.bankInterestAccount=new Wtf.form.ExtFnComboBox({
                    fieldLabel:WtfGlobal.getLocaleText("acc.field.BankInterestAccount"),
                    hiddenName:'accountid',
                    store:this.accountStore,
                    minChars:1,
                    anchor:'85%',
                    valueField:'accountid',
                    displayField:'accountname',
                    name:'bankInterestCmb',
                    forceSelection:true,
                    hirarchical:true,
                    disabled:this.readOnly,
//                    addNewFn:this.openCOAWindow.createDelegate(this),
                    extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
                    mode: 'local',
//                    typeAheadDelay:30000,
                    extraComparisionField:'acccode',// type ahead search on acccode as well.
                    listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400
                });
                
                
                this.ibgTransactionCode = new Wtf.form.ComboBox({
                    fieldLabel:'IBG Transaction Code',
                    store: Wtf.ibgTransactionCodeStore,
                    name:'ibgCode',
                    displayField:'ibgCode',
                    id:'ibgCode'+this.id,
                    valueField:'ibgCode',
                    value:'20',
                    mode: 'local',
                    width:50,
                    listWidth:50,
                    hidden:this.isReceipt || !Wtf.account.companyAccountPref.activateIBG,
                    hideLabel:this.isReceipt || !Wtf.account.companyAccountPref.activateIBG,
                    triggerAction: 'all',
                    typeAhead:true,
                    selectOnFocus:true,
                    disabled:true
                });
                
                
                
                
    this.accountStore.on("load",function(store){    
         if(this.isEdit) {
            var data=this.record.data;
//             this.NorthForm.getForm().loadRecord(this.record);
            this.bankChargesAccount.setValue(data.bankChargesCmb);
            this.bankInterestAccount.setValue(data.bankInterestCmb);
            this.paidTo.setValue(data.paidToCmb);            
             this.bankCharges.setValue(data.bankCharges);
             this.bankInterest.setValue(data.bankInterest);
            
         }
    },this);
    this.paidTo= new Wtf.form.ExtFnComboBox({
         fieldLabel: this.isReceipt ? WtfGlobal.getLocaleText("acc.mp.receivedFrom") : WtfGlobal.getLocaleText("acc.mp.paidTo"),  //'Received From':'Paid To'
         name:"paidToCmb",
         store: this.isReceipt ? Wtf.RPReceivedFromStore : Wtf.MPPaidToStore,
         id:"paidto"+config.helpmodeid+this.id,
         valueField:'id',
         displayField:'name',
         allowBlank:true,
         disabled: this.readOnly,
         emptyText: this.isReceipt ? WtfGlobal.getLocaleText("acc.rp.selreceivedfrom") : WtfGlobal.getLocaleText("acc.mp.selpaidto"), //'Select Received From...':'Select Paid To...'
         minChars:1,
         extraFields:'',
         listWidth :500,
         extraComparisionField:'name',// type ahead search on acccode as well.
         anchor:'85%',
         mode: 'local',
         triggerAction: 'all',
         typeAhead: true,
         forceSelection: true,
         listeners:{
            'select':{
                fn:function(){
                    if(Wtf.account.companyAccountPref.activateIBG && this.isIBGTypeTransaction && this.val == "9") {
                        var rec = WtfGlobal.searchRecord(Wtf.MPPaidToStore, this.paidTo.getValue(), 'id');
                        this.callIBGDetailsGrid(rec,true,true);
                    }
                },
                scope:this            
            }
        }
    });
    this.paidTo.addNewFn=this.addPaidTo.createDelegate(this);   
    this.setOSDetailNumber();
    this.Memo=new Wtf.form.TextArea({
        fieldLabel:Wtf.account.companyAccountPref.descriptionType,  // 'Memo',
        name: 'memo',
        height:40,
        anchor:'85%',
        maxLength:2048,
        disabled:this.readOnly
    });
    this.Amount=new Wtf.form.NumberField({
        name:"amount",
        allowBlank:false,
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.11"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
        id:"amount"+config.helpmodeid+this.id,
        maxLength:15,
        decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
        disabled:true,
        value: 0,
        emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
        anchor:'85%'
    });
    
    this.isadvance= new Wtf.form.Checkbox({
        name:'isadvance',
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.16"),  //'Is Advance',
        checked:false,
        disabled:this.isEdit,
        hideLabel:this.isCustBill,
        hidden:this.isCustBill,
        cls : 'custcheckbox',
        width: 10
    });
    
    this.isadvanceFromVendor= new Wtf.form.Checkbox({
        name:'isadvanceFromVendor',
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.16"),  //'Is Advance',
        checked:false,
        disabled:this.isEdit || this.directPayment,
        hidden:this.directPayment,
        hideLabel:this.directPayment,
//        hideLabel:this.isCustBill,
//        hidden:this.isCustBill,
        cls : 'custcheckbox',
        width: 10
    });
    
    this.advanceAmount=new Wtf.form.NumberField({
        name:"advanceamt",
        allowBlank:false,
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.15"),  //"Advance Amount",
        id:"advanceamt"+config.helpmodeid+this.id,
        maxLength:15,
        decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
        disabled:true,
        emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
        anchor:'85%',
        value :0,
        hideLabel:this.isCustBill,
        hidden:this.isCustBill
    });
    this.advanceAmountStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'int'}],
            data:[['Local Sales',1],['Export Sales',2]]
   });
   
    this.advanceAmountType= new Wtf.form.ComboBox({
            labelSeparator:'',
            labelWidth:0,
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            fieldLabel:"Advance Type:", //WtfGlobal.getLocaleText("acc.mp.15"),  //"Advance Amount",
            id: "advancetype"+this.heplmodeid+this.id,
            store:this.advanceAmountStore,
            hideLabel:this.isCustBill || Wtf.account.companyAccountPref.countryid=='137' && this.isReceipt?false:true,
            hidden:this.isCustBill || Wtf.account.companyAccountPref.countryid=='137' && this.isReceipt?false:true,
//            disabled:true,
//            allowBlank:this.isOrder,
            anchor:'85%',
            value :1,
//            width:100,
            typeAhead: true,
            forceSelection: true,
            name:'advancetype',
            hiddenName:'advancetype'
//            listeners:{
//                'select':{
//                    fn:this.updateSubtotal,
//                    scope:this
//                }
//            }
        });    
    this.isadvance.on('check', function(){    
        if(this.isadvance.getValue()){
            this.advanceAmount.enable();
            this.advanceAmountType.enable();
            if(this.readOnly)
                 this.advanceAmount.disable();
        }else{
            this.advanceAmount.disable();
            this.advanceAmountType.disable();
            this.advanceAmount.reset();
            this.advanceAmountType.reset();
            this.updateSouthTemp();
        }
    }, this);
    
    this.ismanydbcr= new Wtf.form.Checkbox({
        name:'ismanydbcr',
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.18"),  //'Is Advance',
        checked:false,
//        hideLabel:this.isCustBill,
//        hidden:this.isCustBill,
        disabled:this.isEdit || this.directPayment,
        hidden:this.directPayment,
        hideLabel:this.directPayment,
        cls : 'custcheckbox',
        width: 10
    });
    
    this.ismanydbcr.on('check', function(){   
        this.grid.getStore().removeAll();
        this.grid.addNewRow();
        this.Amount.setValue(0);
        if(this.ismanydbcr.getValue()){
//            this.grid.getColumnModel().setHidden(1,false) ;
            WtfGlobal.hideFormElement(this.Amount);
            this.Amount.allowBlank = true;
            this.grid.ismanydbcr = true;
        }else{
//            this.grid.getColumnModel().setHidden(1,true) ;
            WtfGlobal.showFormElement(this.Amount);
            this.Amount.allowBlank = false;
            this.grid.ismanydbcr = false;
        }
        this.updateSouthTemp();
    }, this);
    
    this.Amount.on('blur',function(field){if(field.getValue() == ""){field.setValue(0);this.updateSouthTemp();}},this);
    this.Amount.on('focus',function(field){if(field.getValue() == 0){field.setValue("");}},this);

    
    this.advanceAmount.on('blur',function(field){if(field.getValue() == ""){field.setValue(0);}this.updateSouthTemp();},this);
    this.advanceAmount.on('focus',function(field){if(field.getValue() == 0){field.setValue("");}},this);
    this.lifoFifoStore = new Wtf.data.SimpleStore({
        fields :['id', 'name'],
        data:[['1','NA'],['2','LIFO'],['3','FIFO']]
    });

    this.lifoFifoCombo= new Wtf.form.FnRefreshBtn({
        fieldLabel:WtfGlobal.getLocaleText("acc.invoices.invoiceSelectionCriteria"),//"Selection Criteria",//WtfGlobal.getLocaleText("acc.invoices.invoiceSelectionCriteria"),
        name:"pmtmethod",
//        width:185,
        anchor:'85%',
        store:this.lifoFifoStore,
        valueField:'id',
        displayField:'name',
        hideLabel:false,
        mode: 'local',
        triggerAction: 'all',
        allowBlank:false,
//        typeAhead: true,
        disabled:true,
        emptyText:WtfGlobal.getLocaleText("acc.field.SelectaCriteria"),
        addNewFn: this.refreshBtnHandler.createDelegate(this),
        forceSelection: true//,
    });    
    
    this.lifoFifoCombo.on('select',this.refreshGrid,this);    
//    this.lifoFifoCombo.on('change',this.alertMessage,this);    
    
    this.SouthForm=new Wtf.account.PayMethodPanel({
        region : "center",
        hideMode:'display',
        baseCls:'bodyFormat',
        isReceipt:this.isReceipt,
        disabledClass:"newtripcmbss",
       // height:100,
        autoHeight:true,
        disabled:this.readOnly,
        hidden:true,
        style:'margin:10px 10px;',
        id:this.id+'southform',
        border:false
    });
    this.invGrGrid = new Wtf.account.OSDetailGrid({
        region: 'center',
        style: 'padding:10px 10px 0px 10px',
        isNote: true,
        height: 140,
        winType:this.val,
        receiptObject:this,
        hidden: true,
        moduleid: this.moduleid,
        disabled:this.readOnly,
        sm: new Wtf.grid.CheckboxSelectionModel({singleSelect: true}),
        isReceipt: !this.isReceipt,
        viewConfig: {forceFit: false, emptyText: "<div class='grid-empty-text'>" + (!this.isReceipt ? WtfGlobal.getLocaleText("acc.rem.121") : WtfGlobal.getLocaleText("acc.rem.122")) + "</div>"},
        closable: true
    });
    this.cndnGrid = new Wtf.account.OSDetailGrid({
        region: 'center',
        style: 'padding:10px 10px 0px 10px',
        isNote: false,
        height: 140,
        winType:this.val,
        hidden: true,
        moduleid: this.moduleid,
        advanceCnDnFlag:true,
        disabled:this.readOnly,
        sm: new Wtf.grid.CheckboxSelectionModel({singleSelect: true}),
        isReceipt:this.isReceipt,
        viewConfig: {forceFit: false, emptyText: "<div class='grid-empty-text'>" + (!this.isReceipt ? WtfGlobal.getLocaleText("acc.rem.121") : WtfGlobal.getLocaleText("acc.rem.122")) + "</div>"},
        closable: true
    });
    this.centerPanel = new Wtf.Panel({
        region: "center",
        layout: "fit",
        //height:300,
        autoHeight:true,
        items: [this.SouthForm, this.invGrGrid]
    });
    this.invGrGrid.on("row");
    this.mainInvoiceId="";
    this.totalValidValue=0;
    this.invGrGrid.getSelectionModel().on("rowselect",function(a,b,record){        
       this.grid.venCustAmount=record.data.amountdue;
       this.totalValidValue=record.data.amountdue;
       this.mainInvoiceId=record.data.billid;
       this.grid.getStore().reload();
    },this);
     this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:this.id+'Northform',
            compId1:this.id+"wrapperPanelNorth",
            autoHeight: true,
            moduleid: this.moduleid,
            isEdit: this.isEdit,
            record: this.record
        });        
    this.NorthForm=new Wtf.form.FormPanel({
        region : "north",
        //height:200,
        autoHeight:true,
        border:false,
        defaults:{border:false},
        split:true,
        layout:'form',
        baseCls:'northFormFormat',
        disabledClass:"newtripcmbss",
        hideMode:'display',
        id:this.id+'Northform',
        cls:"visibleDisabled",
        labelWidth:140,
        disabled:this.readOnly,
        items:[{
            layout:'column',
            defaults:{border:false},
            items:[{
                layout:'form',
                columnWidth:0.33,
                items:[this.sequenceFormatCombobox,this.No,this.creationDate,this.isadvance,this.advanceAmount,this.advanceAmountType,this.paidTo]
           },{
                layout:'form',
                columnWidth:0.33,
                items:[this.ShowOnlyOneTime,this.Name, this.Currency, this.ismanydbcr, this.Amount,this.lifoFifoCombo,this.isadvanceFromVendor,this.bankCharges,this.bankChargesAccount]
            },{
                layout:'form',
                columnWidth:0.33,
                items:[this.pmtMethod,this.pmtMethodAcc,this.Memo,this.bankInterest,this.bankInterestAccount,this.ibgTransactionCode]
            }]
        }, this.tagsFieldset]
    });
     this.createGrid();   
     this.tplSummary=new Wtf.XTemplate(
        '<div class="currency-view">',
        '<table width="100%">', //       
        '<tr><td><b>'+(this.isReceipt?WtfGlobal.getLocaleText("acc.mp.receiptAmount"):WtfGlobal.getLocaleText("acc.mp.paymentAmount"))+': </b></td><td text-align=right>{due}</td></tr>',
        '</table>',      
        '<div id="hideIdForTax">',
        '<hr class="templineview">',
        '<table width="100%">',
        '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.amt")+' </b></td><td text-align=right>{amountwithouttax}</td></tr>',
        '<tr><td><b>'+WtfGlobal.getLocaleText("acc.rem.196")+': </b></td><td text-align=right>{taxamount}</td></tr>',
        '</table>',
        '<hr class="templineview">',
        '</div>',
        '<table width="100%">',
        '<tr><td><b>'+(this.isReceipt?WtfGlobal.getLocaleText("acc.mp.12"):WtfGlobal.getLocaleText("acc.mp.13"))+ '</b></td><td text-align=right>{received}</td></tr>',
        '<tr><td><b>'+(this.isReceipt?WtfGlobal.getLocaleText("acc.rp.amountrecInBase"):WtfGlobal.getLocaleText("acc.mp.paidInBase"))+ '</b></td><td text-align=right>{receivedinbase}</td></tr>',
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
        '<div id="hideIdForAdvance">',
        '<table width="100%">',// style="opacity:'+((this.val!="1")? '0' : '200')+' style="display:'+((this.val==1)? 'none' : 'block')+'; this.val!="1" 
        '<tr><td><b>'+(WtfGlobal.getLocaleText("acc.mp.advanceAmount"))+ ' </b></td><td align=right>{advance}</td></tr>',        
        '</table>',
        '<tr><td><b>'+(WtfGlobal.getLocaleText("acc.mp.advanceAmountInBase"))+ ' </b></td><td align=right>{advanceInBase}</td></tr>',        
        '</table>',
        '<hr class="templineview" >',
      //  '<hr class="templineview" >',
        '</div>',
        '</div>'    
    );
    this.southCalTemp=new Wtf.Panel({
        border:false,
        boaseCls:'tempbackgroundview',
        html:this.tplSummary.apply({receivable:WtfGlobal.currencyRenderer(0),receivableInBase:WtfGlobal.currencyRenderer(0),received:WtfGlobal.currencyRenderer(0),due:WtfGlobal.currencyRenderer(0),advance:WtfGlobal.currencyRenderer(0),advanceInBase:WtfGlobal.currencyRenderer(0),taxamount:WtfGlobal.currencyRenderer(0),amountwithouttax:WtfGlobal.currencyRenderer(0),receivedinbase:WtfGlobal.currencyRenderer(0)})
    });
    this.bankBalanceTplSummary=new Wtf.XTemplate(
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
        '<div><hr class="templineview"></div>',
        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:40%;"><b>'+WtfGlobal.getLocaleText("acc.field.AccountBalance")+'</b></td><td style="width:50%;"><span style="width: auto;float: left;display:block;">'+Wtf.util.Format.ellipsis('{endingBalance}',20)+'</span></td>'+        
        '</tr>'+
        '</table>'+
        '</div>',            
        '<div><hr class="templineview"></div>',                        
        '</div>'
    );
    this.bankBalanceTpl=new Wtf.Panel({
        //id:'productDetailsTpl',
        border:false,
        baseCls:'tempbackgroundview',
        width:'35%',
        hidden:(this.val=="4")?true:false,
        html:this.bankBalanceTplSummary.apply({endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"})
    });    
        var blockSpotRateLink_first = "";
        var blockSpotRateLink_second = "";
        if(!Wtf.account.companyAccountPref.activateToBlockSpotRate){ // If activateToBlockSpotRate is set then block the Spot Rate Links
            blockSpotRateLink_first = "<br/>"+WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div><div style='padding-left:30px;padding-top:5px;padding-bottom:10px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{foreigncurrency}\",\"{basecurrency}\",\"{revexchangerate}\",\"foreigntobase\")'wtf:qtip=''>{foreigncurrency} to {basecurrency}</a></div>";
            blockSpotRateLink_second = "<br/>"+WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div> <div style='padding-left:30px;padding-top:5px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\",\"basetoforeign\")'wtf:qtip=''>{basecurrency} to {foreigncurrency}</a></div>";
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
   this.southCenterTpl=new Wtf.Panel({
        border:false,
        html:this.southCenterTplSummary.apply({basecurrency:WtfGlobal.getCurrencyName(),exchangerate:'x',foreigncurrency:"Foreign Currency",editable:false})
    });
    this.hideAdvanceAmount();
     var tranType=null;
    if(this.isCustBill)
        tranType=config.isCustomer?(config.isOrder?Wtf.autoNum.BillingSalesOrder:Wtf.autoNum.BillingInvoice):(config.isOrder?Wtf.autoNum.BillingPurchaseOrder:Wtf.autoNum.BillingGoodsReceipt);
    else if(config.moduleId==14||config.moduleId==16){
        if(config.moduleId==14){
            tranType=Wtf.autoNum.Payment;
        }else{
            tranType=Wtf.autoNum.Receipt;
        }   
    }
    if(this.isRequisition) {
        tranType= Wtf.autoNum.Requisition;
    } else if(this.isRFQ) {
        tranType= Wtf.autoNum.RFQ;
    }
    	

    var singlePDFtext = null;
    if(this.isQuotation)
    	singlePDFtext = WtfGlobal.getLocaleText("acc.accPref.autoQN");
    else
    	singlePDFtext = config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoSO"):WtfGlobal.getLocaleText("acc.accPref.autoInvoice")):(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoPO"):WtfGlobal.getLocaleText("acc.accPref.autoVI"));

    this.southPanel=new Wtf.Panel({
        region:'south',
        border:false,
        style:'padding:0px 10px 10px 10px',
        layout:'border',
        disabledClass:"newtripcmbss",
        disabled:this.readOnly,
        autoScroll:true,
        height:200,
        items:[{
            region:'center',
            border:false,
            autoHeight:true,
            items:[this.bankBalanceTpl,this.southCenterTpl]
        },{
            region:'east',
            id: this.id + 'southEastPanel',
            cls:'bckgroundcolor',
            bodyStyle:'padding:10px',
            width:350,
            items:[this.southCalTemp]
        }]
    });
    this.appendID = true;
    this.newPanel=new Wtf.Panel({
        autoScroll:true, 
        region : 'center',
        items:[this.wrapperNorth = new Wtf.Panel({
              region:"north",
              //height:245,
              autoHeight:true,
              style:'padding:0px 10px 0px 0px',
              id:this.id+"wrapperPanelNorth",
               border:false,
//                layout:'border',
              defaults:{border:false},
              items:[this.NorthForm,this.centerPanel]
           }),this.grid,this.cndnGrid,this.southPanel]
    });
        this.newPanel.on("resize",function(){
                this.newPanel.doLayout();
        },this);
            Wtf.apply(this,{
                items:[this.newPanel],
                bbar:[this.saveBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                scope:this,
                id:"save"+config.helpmodeid+this.id,
                hidden : this.readOnly || this.contraentryflag,
                iconCls :getButtonIconCls(Wtf.etype.save),
                handler: function(){
                    this.mailFlag = true;
                    this.disableSaveButtons();
                    this.save();
                }
            }),this.savencreateBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
                scope:this,
                hidden : this.isEdit || this.directPayment|| this.readOnly,
                id:"savencreate"+config.helpmodeid+this.id,
                iconCls :getButtonIconCls(Wtf.etype.save),
                handler: function(){
                    this.mailFlag = false;
                    this.disableSaveButtons();
                    this.save();
                }
            }),(!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType))?this.emailBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.email"),
                tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
                scope:this,
                hidden : this.isEdit || this.directPayment,
                id:"emailbut" + this.id,
                iconCls: "accountingbase financialreport",
                disabled : true,
                handler: function(){this.callEmailWindowFunction(this.response, this.request)}
            }):'',
               (!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))?this.exportPdfBttn=new Wtf.exportButton({
                obj:this,
                id:"exportpdf" + this.id,
                isEntrylevel:true,
                iconCls: 'pwnd exportpdfsingle',
                text:WtfGlobal.getLocaleText("acc.field.ExportPDF"),// + " "+ singlePDFtext,
                tooltip :WtfGlobal.getLocaleText("acc.rem.39.single"),  //'Export Single Record details',
                disabled :true,
                exportRecord:this.exportRecord,
                hidden:this.readOnly ||this.isRequisition || this.isRFQ || this.isSalesCommissionStmt,
                menuItem:{rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
                get:tranType,
                moduleid:config.moduleId
            }):'',
            this.savePrintBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.savePrintBtn"),  //'Save',
                scope:this,
                id:"printsave"+config.helpmodeid+this.id,
                iconCls :getButtonIconCls(Wtf.etype.save),
                hidden:this.readOnly,
                handler: this.savePringCheque.createDelegate(this)
            }),"->",(!this.readOnly)?getHelpButton(this,config.helpmodeid):""]
        });
    this.Amount.on('change',this.checkAmount,this);
    this.grid.on('datachanged',this.updateSouthTemp,this);
    this.cndnGrid.on('datachanged',this.updateSouthTemp,this);
    this.cndnGrid.on('datachangedCN',this.updateAmount,this);
    this.cndnGrid.on('datachangedCNCheckAmount',this.setLifoFifoAmount,this);
    this.creationDate.on('change',this.changeCurrencyStore,this);    
    this.grid.getStore().on('load',this.updateAmount,this);
    this.cndnGrid.getStore().on('load',this.updateAmount,this);
    this.grid.on('datachangedenterpaymentfield',this.updateAmountPaymentField,this);
    this.cndnGrid.on('datachangedenterpaymentfield',this.updateAmountPaymentField,this);
    this.Name.on('select',this.loadGrid,this);
    this.Currency.on('select',this.updateTemplateCurr,this);
  //  this.Currency.on('select',this.loadGrid,this);
    //    this.Amount.on('change',function(a,b){if(this.val!="2")this.grid.updateAmount(b);},this);
    Wtf.account.OSDetailPanel.superclass.constructor.call(this,config);
    this.addEvents({
    update:true
    });
 }
Wtf.extend(Wtf.account.OSDetailPanel,Wtf.account.ClosablePanel,{
    addAccount: function(store){
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update",function(){store.reload()},this);
        
    },
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
           this.isReceipt?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
           this.personAccStore.reload();
        }, this);
    },
    setNextNumber:function(config){
       if(this.sequenceFormatStore.getCount()>0){
            if(this.isEdit&&!this.isCopyReceipt){
                var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
                if(index!=-1){
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                    this.sequenceFormatCombobox.disable();
                    this.No.disable();   
                } else {
                    this.sequenceFormatCombobox.setValue("NA"); 
                    this.sequenceFormatCombobox.disable();
                    this.No.enable();  
                }
            } else{
                if(this.isCopyReceipt!=undefined&&this.isCopyReceipt){
                    var indexCopyCase=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
                    if(indexCopyCase==-1){
                        this.sequenceFormatCombobox.setValue("NA"); 
                        this.sequenceFormatCombobox.disable();
                        this.No.enable(); 
                        this.No.setValue("");
                    }else{
                        var seqRecCopyCase=this.sequenceFormatStore.getAt(0)
                        this.sequenceFormatCombobox.setValue(seqRecCopyCase.data.id);
                        this.getNextSequenceNumber(this.sequenceFormatCombobox); 
                    }
                } else {
                var seqRec=this.sequenceFormatStore.getAt(0)
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
                    var count=this.sequenceFormatStore.getCount();
                    for(var i=0;i<count;i++){
                        var seqRec=this.sequenceFormatStore.getAt(i)
                        if(seqRec.json.isdefaultformat=="Yes"){
                            this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                            break;
                        }
                   
                    }
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
            }
        } 
        } 
    },
    fetchPaymentAccBalance:function(){
        if(this.val != '4' && this.Currency.getValue() != "" && this.pmtMethod.getValue() != "") {//No contra entry
            var currencyid = this.Currency.getValue();
            var methodAccindx = this.pmtStore.find('methodid',this.pmtMethod.getValue());
            if(methodAccindx != -1){
                var payaccid = this.pmtStore.getAt(methodAccindx).get("accountid");
            }
            if((currencyid != undefined || currencyid != "") && (payaccid != undefined || payaccid != "")) {
                Wtf.Ajax.requestEx({
                    url: "ACCReports/getAccountBalanceInSelectedCurrency.do",
                    params: {
                        tocurrencyid:currencyid,
                        accountid:payaccid
                    }
                },this,function(response, request){
                    if(response.success) {
                        this.accEndingBalance=response.endingBalance;
                        this.bankBalanceTplSummary.overwrite(this.bankBalanceTpl.body,{endingBalance:WtfGlobal.addCurrencySymbolOnly(getRoundedAmountValue(response.endingBalance).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL),this.symbol)});
                    } else {
                        this.bankBalanceTplSummary.overwrite(this.bankBalanceTpl.body,{endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
                    }
                },function(response, request){
                    this.bankBalanceTplSummary.overwrite(this.bankBalanceTpl.body,{endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
                });
            }
        }        
    },
    updateTemplateCurr : function(a, rec, index){
        this.fetchPaymentAccBalance();
        if(this.val == '9'|| this.val == '2' || this.val == '5'){
            this.grid.setCurrencyid(rec.data.currencyid,rec.data.symbol);
        } else {
            if(!this.isEdit){
                if(this.val != '6'){       
                     this.grid.getStore().removeAll();
                }
                if(this.Name.getValue() != ""){
                    
                    var isNoteForPayment = false;
                    var isVendor = false;
                    
                    var vendorId="";
                    var customerId="";
                    var gridParams={
                            onlyAmountDue:true,
                            accid:this.Name.getValue(),
                            mode:((this.val=="3")?16:12),
                            deleted:false,
                            cntype:(this.val==7)?8:"",
                            nondeleted:true,
                            isNoteForPayment:isNoteForPayment,
                            isVendor:isVendor,
                            currencyfilterfortrans : rec.data.currencyid,
                            isReceipt:this.isReceipt
                        };
                    if(this.val=="7" ||this.val=="8"){
                        isNoteForPayment = true;
                        var idx = this.Name.store.find('accid', this.Name.getValue());
                        var cmbRec = this.Name.store.getAt(idx);
                        isVendor= cmbRec.get('isVendor');
                        
                        if(isVendor !=undefined && isVendor){
                            vendorId=this.Name.getValue();
                        }
                        if(isVendor !=undefined && !isVendor){
                            customerId=this.Name.getValue();
                        }   
                        gridParams.vendorid=vendorId,
                        gridParams.customerid=customerId,
                        gridParams.isVendor=isVendor
                    }
                    this.grid.getStore().load({
                        params:gridParams
                    });
                    
//                    this.cndnGrid.getView().emptyText = "<div class='grid-empty-text'>"+(this.isReceipt?WtfGlobal.getLocaleText("acc.rem.223"):WtfGlobal.getLocaleText("acc.rem.224"));  
                    
                }
                if(this.isReceipt){//Customer
                          isVendor=false;
                          this.cndnGrid.getStore().proxy.conn.url = 'ACCDebitNote/getDebitNoteMerged.do';
                    }else{//vendor
                          isVendor=true;
                          this.cndnGrid.getStore().proxy.conn.url = 'ACCCreditNote/getCreditNoteMerged.do';
                    }
                    var idxCNDN = this.Name.store.find('accid', this.Name.getValue());
                    var cmbRecCNDN = this.Name.store.getAt(idxCNDN);
                    if(isVendor !=undefined && isVendor){
                        vendorId=this.Name.getValue();
                    }
                    if(isVendor !=undefined && !isVendor){
                        customerId=this.Name.getValue();
                    }  
                    if(cmbRecCNDN!=undefined){
                            var isVendorOnly = isVendor;
                            this.cndnGrid.getStore().load({params:{
                                    transactiontype:1,
                                    cntype:8,
                                    isNoteForPayment:true,
                                    isVendor:isVendorOnly,
                                    vendorid:vendorId,
                                    customerid:customerId,
                                    onlyAmountDue:true,
                                    accid:cmbRecCNDN.data['accid'],
                                    isReceipt:this.isReceipt, 
                                    deleted:false,
                                    nondeleted:true, 
                                    currencyfilterfortrans : rec.data.currencyid 
                                    }});
                        }
                
                if(this.val == '4') {
                    this.loadContraGrid(); 
                }
        }
           //this.applyTemplate(this.currencyStore,index); 
    }
   this.updateSouthTemp(),
   this.hideAdvanceAmount();
   
   
},
    setPMData:function(){
        if(this.isEdit){
            this.setAdvanceFields(); 
            this.setIsmanydbcrFields(this.record.data.ismanydbcr);
            this.pmtMethod.setValue(this.record.data.methodid);
            this.ShowCheckDetails(null,this.record);
            var type=this.record.data.detailtype;            
            if(type==2){
                if(this.isCopyReceipt!=undefined&&this.isCopyReceipt){
                    this.SouthForm.checkNo.setValue("");
                }else{
                this.SouthForm.checkNo.setValue(this.record.data.refno);
                }
                this.SouthForm.description.setValue(unescape(this.record.data.refdetail));
                this.SouthForm.bank.setValue(unescape(this.record.data.refname));
                this.SouthForm.bankTypeStore.on('load',function(){
                    this.SouthForm.bank.setValue(this.record.data.refname);
                },this)
                if(this.record.data.paymentStatus) {
                    this.SouthForm.paymentStatus.setValue("Cleared");
                    this.SouthForm.clearanceDate.getEl().up('.x-form-item').setDisplayed(true);
                    this.SouthForm.clearanceDate.setValue(this.record.data.clearanceDate);
                }
            } else if(type==1){
//                this.expDate.setValue(new Date());

                if(this.record.data.refno==""){
                   this.SouthForm.hide(); 
                }else{
                    this.SouthForm.refNo.setValue(this.record.data.refno);
                    this.SouthForm.cardNo.setValue(this.record.data.refcardno);
                    this.SouthForm.nameOnCard.setValue(this.record.data.refname);
                    this.SouthForm.cardType.setValue(this.record.data.refdetail);
                    this.SouthForm.expDate.setValue(this.record.data.expirydate);                
                }
                
            }       
            var rec=WtfGlobal.searchRecord (this.pmtStore, this.record.data.methodid, 'methodid'); 
            if(rec!=undefined&&rec!=undefined&&rec!=-1&&rec.data!=undefined&&rec.data.autopopulate!=undefined){
                this.autopopulate=rec.data.autopopulate; 
            }
            if(this.grid!=undefined){
                this.grid.autopopulate = this.autopopulate;
            }
            if(this.cndnGrid!=undefined){
                this.cndnGrid.autopopulate = this.autopopulate;
            }
            this.doLayout();
        } else {
            var rowIndex = this.pmtStore.find("isdefault", "true");
            if(rowIndex != -1){
                var rec = this.pmtStore.getAt(rowIndex);
                var methodid = rec.get('methodid');
                this.pmtMethod.setValue(methodid);
                this.ShowCheckDetails(this.pmtMethod, rec);
                this.autopopulate=rec.data.autopopulate;
                if(this.grid!=undefined){
                    this.grid.autopopulate = this.autopopulate;
                }
                if(this.cndnGrid!=undefined){
                    this.cndnGrid.autopopulate = this.autopopulate;
                }
            }
        }
//        this.fetchPaymentAccBalance();
    },
    setAdvanceFields:function(){
        this.advanceid = "";
        if(this.record.data.advanceid!="") {
            this.advanceid = this.record.data.advanceid;
            this.isadvance.setValue(true);
            this.advanceAmount.setValue(this.record.data.advanceamount);
            if(this.record.data.advanceUsed){//Can not edit advance if it is already used against invoice.
                this.advanceUsed = true;
                this.isadvance.disable();
                this.advanceAmount.disable();
            }
        }
        if(this.val!="6" || (this.val=="6" && !this.isReceipt)) {
            WtfGlobal.hideFormElement(this.isadvanceFromVendor);
        } else if(this.val=="6" && this.record.data.isadvancefromvendor) {
            this.isadvanceFromVendor.setValue(this.record.data.isadvancefromvendor);
        }
    },
    
    setIsmanydbcrFields:function(ismanydbcrtemp){        
        this.grid.ismanydbcr = false;
        if(this.isEdit) {
            ismanydbcrtemp = this.record.data.ismanydbcr;
            this.ismanydbcr.setValue(this.record.data.ismanydbcr);
            this.ismanydbcr.disable();
            if(ismanydbcrtemp){
                this.grid.ismanydbcr = true;                
            } else {
                this.grid.ismanydbcr = false;
                WtfGlobal.hideFormElement(this.ismanydbcr);
            }
        }
        if(ismanydbcrtemp) {
            this.grid.ismanydbcr = true;
            WtfGlobal.hideFormElement(this.Amount);
            this.Amount.allowBlank = true;
//        }else{
//            this.grid.getColumnModel().setHidden(0,true) ;
//            WtfGlobal.showFormElement(this.Amount);
//            this.Amount.allowBlank = false;
//            this.updateSouthTemp();
        }
    },
    loadRecord:function(){
        if(this.record!=null){
            var data=this.record.data;
            this.NorthForm.getForm().loadRecord(this.record);
            this.setEditableData=true;
            this.Currency.setValue(data.currencyid);
            this.Memo.setValue(data.memo);
            this.creationDate.setValue(data.billdate);
            this.isIBGTypeTransaction = data.isIBGTypeTransaction;
            this.ibgDetailsID = data.ibgDetailsID;
            this.ibgTransactionCode.setValue(data.ibgCode);
//            this.fetchPaymentAccBalance();
        }
    },


    changeCurrencyStore:function(a,val,oldval){
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.creationDate.getValue())}});
        this.currencyStore.on('load',this.changeTemplateSymbol.createDelegate(this,[oldval]),this);
        this.hideAdvanceAmount();    
    },
    changeTemplateSymbol:function(oldval){
        if(this.currencyStore.getCount()==0){
            this.currencyStore.purgeListeners();
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.10")], 2);
             this.creationDate.setValue(oldval);
             this.changeCurrencyStore();
        }
        else
            this.getCurrencySymbol();
    },
    onRender:function(config){
        if(this.isEdit){
           this.checkWinVal(null,"1");
           this.loadRecord();
           this.loadGrid(this.record);
        } else if(this.directPayment){
           this.checkWinVal(null,this.isCustBill?"3":"1");
           this.loadGrid();
        } else{
            var winid=this.isCustBill?"billrecwin":"recWin";
            if(this.isDirectCustomer){
                this.currencyStore.on('load',function(){
                this.checkWinVal(this,this.winValue)
            },this);
        }else{
            callReceiptWindow(winid,this.isReceipt,false,this.isCustBill,this.id);
            Wtf.getCmp(winid).on('update',this.checkWinVal,this);
        }
//            Wtf.getCmp(winid).on('cancel',function(){
//                this.close();
//            },this);
        }
        this.isClosable=false          // Set Closable flag for on render
        Wtf.account.OSDetailPanel.superclass.onRender.call(this,config);
    },
    checkWinVal:function(c,val){        
        this.val=val;   
        this.accountStoreRec={};
        this.accountStoreRec.mode=2;
        this.accountStoreRec.grouper="paymentTrans";
        this.accountStoreRec.ignorecustomers=true;
        if (this.val == '4') {
            var panelHeight=this.wrapperNorth.height
            this.wrapperNorth.setHeight(panelHeight+130);
            this.contraentryflag = true;
            this.grid.contraentryflag = true;
            this.savePrintBttn.hide();
            this.SouthForm.hide();
            hideField(this.pmtMethod);
            hideField(this.pmtMethodAcc);
            this.bankBalanceTpl.hide();
            this.invGrGrid.show();
            this.loadContraGrid();
//            this.exportPdfBttn.hide();
            if(this.emailBttn){
                this.emailBttn.hide();
            }
            this.saveBttn.hide();
            
            WtfGlobal.hideFormElement(this.bankCharges);
            WtfGlobal.hideFormElement(this.bankChargesAccount);
            WtfGlobal.hideFormElement(this.bankInterest);
            WtfGlobal.hideFormElement(this.bankInterestAccount);
            
            WtfGlobal.hideFormElement(this.isadvanceFromVendor);
            WtfGlobal.hideFormElement(this.isadvance);                        
            WtfGlobal.hideFormElement(this.advanceAmount);     
            WtfGlobal.hideFormElement(this.advanceAmountType);     
            WtfGlobal.hideFormElement(this.ShowOnlyOneTime);  
            WtfGlobal.hideFormElement(this.lifoFifoCombo);
            //        	if(this.invoiceRecord == undefined){
           WtfGlobal.hideFormElement(this.Amount);
//        	}else{
//            	this.Amount.hideLabel = true;
//            	this.Amount.hidden = true;
            this.lifoFifoCombo.hideLabel = true;
            this.lifoFifoCombo.hide();
            this.lifoFifoCombo.allowBlank = true;
            
        } else {
            if(this.isReceipt) {this.savePrintBttn.hide()};
             var panelHeight=this.wrapperNorth.height
            this.wrapperNorth.setHeight(panelHeight+20);
//            if(this.val=="1"){
//                 WtfGlobal.updateFormLabel(this.Amount,WtfGlobal.getLocaleText("acc.field.TotalAmount*"));
//        }
        }
         if(!this.isEdit && !this.directPayment){this.doLayout();}
        var params={mode:2,grouper:'paymentTrans'};
        if(this.val=="1" && !this.isEdit){
            var pmrec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), 'methodid');
            if (pmrec != null && pmrec != undefined) {
                this.checkIsIBGTransaction(pmrec);
            }
//                this.Currency.disable();
//                this.onCheckLifoFifoFlage = true;            
                this.grid.onCheckLifoFifoFlage = true;
//        	if(this.invoiceRecord == undefined){
//        		WtfGlobal.hideFormElement(this.Amount);
//        	}else{
//            	this.Amount.hideLabel = true;
//            	this.Amount.hidden = true;
//        	}
                this.cndnGrid.winType="7";
                this.cndnGrid.reconfigureCreditDebitNoteGrid(); 
                this.cndnGrid.show();
        }        
        if(this.val=="1" && this.isEdit){
                this.pmtMethod.disable();
        }
        if(this.isReceipt && this.val=="6"){
               WtfGlobal.hideFormElement(this.ShowOnlyOneTime);  
        }
        if(!this.isReceipt && this.val=="1"){
               WtfGlobal.hideFormElement(this.ShowOnlyOneTime);  
        }
        if((this.val == '9'|| this.val=="2" || this.val=="5" ||  this.val=="7" || this.val=="8")&&!this.isEdit){
            WtfGlobal.hideFormElement(this.ShowOnlyOneTime);  
        }
        if((this.val == '9'|| this.val=="2" || this.val=="5" || this.val=="6" || this.val=="7" || this.val=="8")&&!this.isEdit){
            this.Name.allowBlank=false;
            WtfGlobal.hideFormElement(this.lifoFifoCombo);
            this.lifoFifoCombo.hideLabel = true;
            this.lifoFifoCombo.hide();
            this.lifoFifoCombo.allowBlank = true;            
            WtfGlobal.hideFormElement(this.isadvance);
            WtfGlobal.hideFormElement(this.advanceAmount);
            WtfGlobal.hideFormElement(this.advanceAmountType);
            if(this.val=='5' || this.val=='6') {
                this.grid.winType=this.val;
            }
            if(this.isReceipt){
                this.isMultiDebit = true;
                this.Amount.enable();
//                this.Currency.enable();
//            	this.grid.getView().emptyText="";
//                this.grid.getStore().removeAll();
//                this.grid.hide();
                if(this.val!="5" && this.val!="6" && this.val!="7" && this.val!="8" ) {
                    WtfGlobal.hideFormElement(this.Name);
                    this.Name.allowBlank=true;
                } else {
                    WtfGlobal.updateFormLabel(this.Name, this.isReceipt ? WtfGlobal.getLocaleText("acc.mp.ven") : WtfGlobal.getLocaleText("acc.rp.cus"));
                    this.isReceipt ? params.group=13 : params.group=10;
                    params.nondeleted=true;
                    if(this.val!="7" && this.val!="8") {
                        this.grid.isAdvPayment = true;
                        this.grid.hide();
                        Wtf.getCmp(this.id + 'southEastPanel').hide();
                    }else if(this.isReceipt){
                        WtfGlobal.updateFormLabel(this.Name, WtfGlobal.getLocaleText("acc.field.Account*"));
                    }
//                    this.grid.store.getAt(i).data.accountid = b.data.accid;
                }
                if(this.val!="7" && this.val!="8" ) {
                    this.grid.reconfigureGrid(this.isMultiDebit);
                } else {
                    this.grid.winType=this.val;
                    this.grid.reconfigureCreditDebitNoteGrid();
                }
               // Wtf.getCmp(this.id + 'southEastPanel').hide();
//                WtfGlobal.updateFormLabel(this.Name, WtfGlobal.getLocaleText("acc.het.101"));
//                this.Name.addNewFn= this.addAccount.createDelegate(this,[this.personAccStore,true,false],true);
                if(!this.isEdit && this.val!="6" && this.val!="7" ){
                    params.ignorevendors=true;
                    //params.nature=3; //3 means income type of nature
                }
                
            }else{
                this.isMultiDebit=true;
                if(this.val!="5" && this.val!="6" && this.val!="7" && this.val!="8") {
                    WtfGlobal.hideFormElement(this.Name);
                    this.Name.allowBlank=true;
                } else if(this.val!="7" && this.val!="8") {
                    this.grid.isAdvPayment = true;
                    this.grid.hide();
                    Wtf.getCmp(this.id + 'southEastPanel').hide();
                    WtfGlobal.updateFormLabel(this.Name, this.isReceipt ? WtfGlobal.getLocaleText("acc.mp.ven"): WtfGlobal.getLocaleText("acc.rp.cus"));
                    this.isReceipt ? params.group=13 : params.group=10;
//                    params.group=13;
                }
                if(this.val!="7" && this.val!="8" ) {
                     this.grid.reconfigureGrid(this.isMultiDebit);
                }else{
                     this.grid.winType=this.val;
                     this.grid.reconfigureCreditDebitNoteGrid();
                      WtfGlobal.updateFormLabel(this.Name, this.isReceipt ?  WtfGlobal.getLocaleText("acc.rp.cus"):WtfGlobal.getLocaleText("acc.mp.ven"));
                }
                if (this.val == "9"){
                     this.accountStoreRec.ignorevendors= true;
                }                
//                if (this.val != "9")
//                    this.grid.accountStore.load({params: {mode: 2, grouper: "paymentTrans", ignorecustomers: true}});  //nature:2 means expense type of nature
//                else
//                    this.grid.accountStore.load({params: {mode: 2, grouper: "paymentTrans", ignorecustomers: true, ignorevendors: true}});  //nature:2 means expense type of nature
//                this.Currency.enable();
                this.Amount.enable();										//Neeraj
                if(!this.isEdit && this.val!="6")
                    params.ignorecustomers=true;
            }  
            //this.Amount.setValue(0);
            //this.SouthForm.getForm().reset();
            var defaultPayMenthodId = this.pmtMethod.getValue();
            var rowIndex = this.pmtStore.findBy( function(rec){
                        var methidid=rec.data['methodid'];
                        if(methidid==defaultPayMenthodId)
                            return true;
                        else
                            return false
                    }, this);
             
            if(rowIndex != -1){
                var rec = this.pmtStore.getAt(rowIndex);
                this.ShowCheckDetails(this.pmtMethod, rec);
            }

        }

        else if(this.isReceipt&&!this.isEdit) {
            params.group=10;
            params.nondeleted=true;
        }
        else if(!this.isEdit) {
            params.group=13;
            params.nondeleted=true;
        }
        if(val=="3"){
                WtfGlobal.hideFormElement(this.isadvanceFromVendor);
                WtfGlobal.hideFormElement(this.isadvance);
                WtfGlobal.hideFormElement(this.advanceAmount);
                WtfGlobal.hideFormElement(this.advanceAmountType);
                WtfGlobal.hideFormElement(this.ShowOnlyOneTime);  
        	this.isCustBill=true;
        	this.Amount.hideLabel = true;
            	this.Amount.hidden = true;
//                this.onCheckLifoFifoFlage=true;
                this.grid.onCheckLifoFifoFlage = true;
                //WtfGlobal.hideFormElement(this.Amount);				//Neeraj
        }
        if(this.isEdit){
            this.lifoFifoCombo.hideLabel = true;
            this.lifoFifoCombo.hide();
            this.lifoFifoCombo.allowBlank = true;
        }
         if (this.val == "9"){
                  this.accountStoreRec.ignorevendors= true;
             }     
//        if(this.isEdit && !this.isReceipt && this.val=="1"){
//            this.grid.accountStore.load({params:{mode:2,grouper:"paymentTrans",ignorecustomers:true}});
//        }else if(this.val=="9"){
//              this.grid.accountStore.load({params: {mode: 2, grouper: "paymentTrans", ignorecustomers: true, ignorevendors: true}});  //nature:2 means expense type of nature
//        }
        if(this.isReceipt && (this.val=="7"|| this.val=="8" ||this.actualReceiptType=="7"||this.actualReceiptType=="8") ){
             WtfGlobal.updateFormLabel(this.Name, WtfGlobal.getLocaleText("acc.masterConfig.costCenter.name"));
            params={"mode":2,ignoreGLAccounts:false,"deleted":false,"nondeleted":true,"common":"1"};
        }else if(this.val=="7" ||this.actualReceiptType=="7"){
             WtfGlobal.updateFormLabel(this.Name, WtfGlobal.getLocaleText("acc.masterConfig.costCenter.name"));
             params={"mode":2,ignoreGLAccounts:false,"deleted":false,"nondeleted":true,"common":"1"};
        }
        if(!this.isEdit){
            this.grid.accountStore.load({
                params:this.accountStoreRec
            });
        }
         //Following code for new changes for customer and vendors which is done when Accounts separation of custmer and vendor
            if((this.val==1 || this.val==6)){
                    if(this.isReceipt){
                        if(this.val==1)
                            this.personAccStore.proxy.conn.url= "ACCCustomer/getCustomersForCombo.do";
                        else
                            this.personAccStore.proxy.conn.url= "ACCVendor/getVendorsForCombo.do"; 
                    }else{
                        if(this.val==1)
                            this.personAccStore.proxy.conn.url= "ACCVendor/getVendorsForCombo.do";
                            
                        else
                            this.personAccStore.proxy.conn.url= "ACCCustomer/getCustomersForCombo.do";
                    }
                    params={
                        mode:2,
//                        group:(this.val==1)?10:13,
                        ignoreGLAccounts:false,
                        deleted:false,
                        nondeleted:true,
                        common:'1',
                        combineData:-1  //Send For Seprate Request
                    }
                }else if(this.val==7){
                    this.personAccStore.proxy.conn.url= "ACCAccountCMN/getVenorCustomerForCombo.do"; 
                    params={
                        mode:2,
//                        group:(this.val==1)?10:13,
                        ignoreGLAccounts:false,
                        deleted:false,
                        nondeleted:true,
                        common:'1',
                        combineData:-1  //Send For Seprate Request
                    }
                }
            this.personAccStore.load({params:params});
        if(this.isEdit){
            this.personAccStore.on("load",function(){this.Name.setValue(this.record.data.personid);},this);
        }else if(this.directPayment) {
            this.personAccStore.on("load",function(){this.Name.setValue(this.invoiceRecord.data.personid);},this);
        }
        if(this.val!="6" || (this.val=="6" && !this.isReceipt)) {//Show only for receive payment from vendor
            WtfGlobal.hideFormElement(this.isadvanceFromVendor);               
        }
        this.hideAdvanceAmount();
        if(this.val=='2' || this.val=='9') {
            this.setIsmanydbcrFields(this.ismanydbcr.getValue());
        } else {
//            this.ismanydbcr.checked = false;
            this.ismanydbcr.disable();
            WtfGlobal.hideFormElement(this.ismanydbcr);
        }
    },
        hideAdvanceAmount:function(){         
            if(this.val!="1" || this.actualReceiptType==7||this.actualReceiptType==8 || (this.isEdit && this.actualReceiptType==9)) { //edit case for MP/RP against GL
                if(document.getElementById("hideIdForAdvance") !=null && document.getElementById("hideIdForAdvance"))
                        document.getElementById("hideIdForAdvance").style.display="none";
            }else if(document.getElementById("hideIdForAdvance") !=null && !document.getElementById("hideIdForAdvance")){
                    document.getElementById("hideIdForAdvance").style.display="block"; 
            }
            if(this.val!="9" && this.actualReceiptType!=9) {
                if(document.getElementById("hideIdForTax") !=null && document.getElementById("hideIdForTax"))
                        document.getElementById("hideIdForTax").style.display="none";
            }else if((document.getElementById("hideIdForTax") !=null && !document.getElementById("hideIdForTax"))){
                    document.getElementById("hideIdForTax").style.display="block"; 
            }
            
            if(this.val=="1" && this.actualReceiptType==9) {//written to hide the advance amount label while editing/copying Payment against glcode
                if(document.getElementById("hideIdForAdvance") !=null && document.getElementById("hideIdForAdvance"))
                    document.getElementById("hideIdForAdvance").style.display = "none";
            }
            
        },
        loadContraGrid:function(){
//    	var type= this.isReceipt?'invoice':'Vendor invoice';
        if(this.contraentryflag && this.Name.getValue() != "" && this.Currency.getValue() != "") {
            this.invGrGrid.getStore().proxy.conn.url = this.isReceipt ? (this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getGoodsReceipts.do") : (this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getInvoices.do") ;
//            this.invGrGrid.getStore().proxy.conn.url = this.isReceipt ? (this.isCustBill?"ACCInvoiceCMN/getInvoices.do":"ACCInvoiceCMN/getBillingInvoices.do") : (this.isCustBill?"ACCGoodsReceiptCMN/getGoodsReceipts.do":"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do");
            this.invGrGrid.getStore().load({params:{currencyfilterfortrans:this.Currency.getValue(),contraentryflag:true,accid:this.Name.getValue(),mode:(!this.isCustBill?16:12)}});
        }
    },
    loadGrid:function(a,b,c,sortingInfo,amountValue){
        
        var rec = WtfGlobal.searchRecord(this.personAccStore, this.Name.getValue(), 'accid');
//        if (rec != null && rec.data["paymentCriteria"] != "" || rec.data["paymentCriteria"] != undefined) {
//            this.paymentCriteria = rec.data["paymentCriteria"];
//        }
//        if (sortingInfo == undefined) {
//            this.lifoFifoCombo.setValue(this.paymentCriteria);
//        }
        
        if(Wtf.account.companyAccountPref.activateIBG && !this.isReceipt && this.isIBGTypeTransaction && this.val =="1" && !this.isEdit) {
            this.callIBGDetailsGrid(rec,false,true);
        }
        
        // if this.val = 5 for accepting advance payment against customer irrespective of any payment transactions. So no need to check current open invoices or any transactions
        if(this.val!='5' && this.val!='6' ) { 
            this.grid.getView().emptyText = "<div class='grid-empty-text'>"+(this.isReceipt?WtfGlobal.getLocaleText("acc.rem.121"):WtfGlobal.getLocaleText("acc.rem.122"))+"</div><br><br>"+WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick=\"javascript: openInv("+this.isReceipt+","+this.isCustBill+",'"+this.Name.getValue()+"')\">"+(this.isReceipt?WtfGlobal.getLocaleText("acc.nee.52"):WtfGlobal.getLocaleText("acc.nee.53"))+"</a>");
            var transactiontype=1;
            if(this.val=="8")
               transactiontype=8;
            if(this.val=="7" ||this.val=="8"){
                if((this.isReceipt && this.val=="7") || (!this.isReceipt && this.val=="8")){//Customer
                        this.grid.getStore().proxy.conn.url = 'ACCDebitNote/getDebitNoteMerged.do';
                    
                } else {//vendor
                        this.grid.getStore().proxy.conn.url = 'ACCCreditNote/getCreditNoteMerged.do';
                }
                this.grid.getView().emptyText = "<div class='grid-empty-text'>"+(this.isReceipt?WtfGlobal.getLocaleText("acc.rem.223"):WtfGlobal.getLocaleText("acc.rem.224"));
                var isVendor = b.data['isVendor'];
                var vendorId="";
                var customerId="";
                if(isVendor !=undefined && isVendor){
                    vendorId=b.data['accid'];
                }
                if(isVendor !=undefined && !isVendor){
                    customerId=b.data['accid'];
                }
                this.grid.getStore().load({params:{transactiontype:transactiontype,cntype:8,isNoteForPayment:true,isVendor:isVendor,onlyAmountDue:true,accid:this.Name.getValue(),isReceipt:this.isReceipt, deleted:false,nondeleted:true, currencyfilterfortrans : this.Currency.getValue(), direction:direction, isLifoFifo:isLifoFifo ,vendorid:vendorId,customerid:customerId}});
//                this.grid.getStore().load({params:{onlyAmountDue:true,accid:b.data['accid'],mode:((this.val=="3")?16:12),deleted:false,nondeleted:true, currencyfilterfortrans : b.data['currencyid'], direction:direction, isLifoFifo:isLifoFifo}});               
//                this.setCurrency(b.data['currencyid']);
                this.grid.amount=this.Amount.getValue();
            } else if(this.val!="2"&& this.val != '9'){
                if(this.isEdit&&this.actualReceiptType!=0){
                    if(this.isReceipt){//Customer
                        this.grid.getStore().proxy.conn.url =(this.isCustBill?"ACCReceipt/getBillingReceiptRows":"ACCReceiptCMN/getReceiptRows") + ".do";
                        this.cndnGrid.getStore().proxy.conn.url = 'ACCDebitNote/getDebitNoteMerged.do';
                    }else{//vendor
                        this.grid.getStore().proxy.conn.url =  (this.isCustBill?"ACCVendorPayment/getBillingPaymentRows":"ACCVendorPaymentCMN/getPaymentRows") + ".do";
                        this.cndnGrid.getStore().proxy.conn.url = 'ACCCreditNote/getCreditNoteMerged.do';
                    }              
                    
           this.grid.accountStore.load({
                params:this.accountStoreRec
            });
    
            this.grid.accountStore.on("load",function(){
                var datainvoiceadvcndn= this.record.data.datainvoiceadvcndn;
                var dataCNDNFlag=false;
                var invoicecndnbillid="";
                if(datainvoiceadvcndn!=undefined){
                    for(var i=0;i<datainvoiceadvcndn.length;i++){
                        if(datainvoiceadvcndn[i].invoiceadvcndntype==1){
                            invoicecndnbillid=datainvoiceadvcndn[i].paymentID;
                            dataCNDNFlag=true;
                        }
                    }
                }
                
                if(!dataCNDNFlag&&datainvoiceadvcndn!=undefined){
                    if(this.isReceipt){//Customer
                            this.grid.getStore().proxy.conn.url = (this.isCustBill)? "ACCInvoiceCMN/getBillingInvoices.do" : "ACCInvoiceCMN/getInvoices.do";
                        }else{//vendor
                            this.grid.getStore().proxy.conn.url = (this.isCustBill)? "ACCGoodsReceiptCMN/getBillingGoodsReceipts.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
                        }
                    this.grid.getStore().load({
                        params:{
                            onlyAmountDue:true,
                            accid:this.Name.getValue(),
                            mode:((this.val=="3")?16:12),
                            deleted:false,
                            nondeleted:true, 
                            currencyfilterfortrans : this.Currency.getValue(), 
                            direction:direction, 
                            isLifoFifo:isLifoFifo,
                            isReceipt:this.isReceipt
                            }
                        });               
            }else{
                if(this.isReceipt){//Customer
                        this.grid.getStore().proxy.conn.url =(this.isCustBill?"ACCReceipt/getBillingReceiptRows":"ACCReceiptCMN/getReceiptRows") + ".do";
                    }else{//vendor
                        this.grid.getStore().proxy.conn.url =  (this.isCustBill?"ACCVendorPayment/getBillingPaymentRows":"ACCVendorPaymentCMN/getPaymentRows") + ".do";
                    } 
                this.grid.getStore().load({
                    params:{
                        grouper:'paymentTrans',
                        bills:this.record.data.invoiceadvcndntype!=undefined&&this.record.data.invoiceadvcndntype==3?invoicecndnbillid:this.record.data['billid'],
                        mode:(this.isCustBill?36:33),
                        isReceiptEdit:true,
                        currencyfilterfortrans : a.data['currencyid']
                    }
                });
            }
            },this);
//                        this.grid.accountStore.on("load",function(){
//                            this.grid.getStore().load({params:{grouper:'paymentTrans',bills:this.record.data['billid'],mode:(this.isCustBill?36:33),isReceiptEdit:true,currencyfilterfortrans : a.data['currencyid']}});
//                        },this);
                        
                    this.grid.getStore().on('load',function(store){
                        if(store.getCount()==0){
                            if(this.isEdit&&!this.isReceipt){
                                if(this.val=="1"){
                                    this.isMultiDebit=true;
                                    WtfGlobal.hideFormElement(this.Name); 
                                    WtfGlobal.hideFormElement(this.isadvanceFromVendor);
                                    WtfGlobal.hideFormElement(this.isadvance);
                                    WtfGlobal.hideFormElement(this.advanceAmount);      
                                    WtfGlobal.hideFormElement(this.advanceAmountType);      
                                    WtfGlobal.hideFormElement(this.ShowOnlyOneTime);  
                                    if(this.actualReceiptType=="7"||this.actualReceiptType=="8"){
                                         WtfGlobal.showFormElement(this.Name);
                                         this.grid.reconfigureCreditDebitNoteGrid(this.isMultiDebit, this.record.data,this.actualReceiptType);
                                    }else{
                                        this.grid.reconfigureGrid(this.isMultiDebit, this.record.data.detailsjarr);
                                    }
//                                    this.Currency.enable();
                                    this.Amount.enable();
                                    this.setCurrency(this.record.data.currencyid);
                                }
                            }
                            else{                            
                                this.saveBttn.show();
//                                this.exportPdfBttn.hide();
                                if(this.emailBttn){
                                    this.emailBttn.hide();
                                }
                                this.savencreateBttn.show();
                                //this.southCalTemp.hide();
                                this.isMultiDebit=true;
                                WtfGlobal.hideFormElement(this.Name);
                                WtfGlobal.hideFormElement(this.isadvanceFromVendor);
                                WtfGlobal.hideFormElement(this.isadvance);
                                WtfGlobal.hideFormElement(this.advanceAmount);    
                                WtfGlobal.hideFormElement(this.advanceAmountType);    
                                WtfGlobal.hideFormElement(this.ShowOnlyOneTime);  
                                if(this.actualReceiptType=="7"||this.actualReceiptType=="8"){
                                     WtfGlobal.showFormElement(this.Name);
                                     this.grid.reconfigureCreditDebitNoteGrid(this.isMultiDebit, this.record.data,this.actualReceiptType);                                     
                                }else{
                                    this.grid.reconfigureGrid(true, this.record.data.detailsjarr);
                                }
                                this.val="2";
                                if(this.val == '9')
                                    this.val="9";
                                    
                            }
                           }else if(this.val!="1"){
                            WtfGlobal.hideFormElement(this.Amount);		// Neeraj
                            WtfGlobal.hideFormElement(this.lifoFifoCombo);
                            this.lifoFifoCombo.allowBlank = true;
                        }
                        this.grid.setWidth("100%");
                        if(this.readOnly) 
                            {
                                this.Amount.disable();
                                if(this.exportPdfBttn){
                                    this.exportPdfBttn.hide();
                                }
                                this.saveBttn.hide();
                                this.savencreateBttn.hide();
                                this.savePrintBttn.hide();
                                for(var i=0;i<store.getCount();i++)
                                {
                                    var rec = store.getAt(i);
                                    rec.set('select',true);
                                }
                                if(this.actualReceiptType=='6')
                                    Wtf.getCmp(this.id + 'southEastPanel').hide();
                            }                                                                          
                        else if(store.getCount()>0&&this.isEdit&&this.val=="1")
                        {
                            for(var i=0;i<store.getCount();i++){
                                    var rec = store.getAt(i);
                                    rec.set('select',true);
                            }
//                            this.grid.getView().refresh();
                        }
                        if((this.val == '2' || this.val=="9") && this.isReceipt && this.isEdit){
                            WtfGlobal.updateFormLabel(this.Name, WtfGlobal.getLocaleText("acc.het.101"));
                            //Wtf.getCmp(this.id + 'southEastPanel').hide();
                            //this.southCenterTpl.hide();
                        }
                    },this);
                    

                            this.cndnGrid.getStore().on("load",function(store){
                                if(this.isEdit&&(this.actualReceiptType==0||this.actualReceiptType==1)){
                                    this.isadvance.setValue(true);
                                    this.cndnGrid.winType="7";
                                    this.cndnGrid.reconfigureCreditDebitNoteGrid(this.isMultiDebit, this.record.data,this.actualReceiptType,true);
                                    this.cndnGrid.show();
                                }
                            },this);
                            if(this.isEdit&&(this.actualReceiptType==0||this.actualReceiptType==1)){
                                if(this.cndnGrid.getStore().getCount()==0){
                                    if(this.isReceipt){//Customer
                                        this.cndnGrid.getStore().proxy.conn.url = 'ACCDebitNote/getDebitNoteMerged.do';
                                    }else{//vendor
                                        this.cndnGrid.getStore().proxy.conn.url = 'ACCCreditNote/getCreditNoteMerged.do';
                                    }
                                    var paramsCNDN="";
                                    this.Name.setValue(this.record.data.personid);
                                    this.personAccStore.proxy.conn.url= "ACCAccountCMN/getVenorCustomerForCombo.do"; 
                                    paramsCNDN={
                                        mode:2,
                                        //                        group:(this.val==1)?10:13,
                                        ignoreGLAccounts:false,
                                        deleted:false,
                                        nondeleted:true,
                                        common:'1',
                                        combineData:-1  //Send For Seprate Request
                                    }

                                    this.personAccStore.proxy.conn.url= "ACCAccountCMN/getVenorCustomerForCombo.do"; 
                                    this.personAccStore.load({
                                        params:paramsCNDN
                                    });
                                    this.Name.setValue(this.record.data.personid);
                                    this.personAccStore.on('load',function(){
                                        var recordIndex = this.Name.store.find('accid',this.Name.getValue());
                                        var storeRecord = this.Name.store.getAt(recordIndex);
                                        if(storeRecord!=undefined){//Anup need to handle it Edit Case 
                                            var isVendorOnly = storeRecord.data['isVendor'];
                                            var vendorIdCnDn="";
                                            var customerIdCnDn="";
                                            if(isVendorOnly !=undefined && isVendorOnly){
                                                vendorIdCnDn=this.Name.getValue();
                                            }
                                            if(isVendorOnly !=undefined && !isVendorOnly){
                                                customerIdCnDn=this.Name.getValue();
                                            }  
                                            this.cndnGrid.getStore().load({
                                                params:{
                                                    transactiontype:transactiontype,
                                                    cntype:8,
                                                    isNoteForPayment:true,
                                                    isVendor:isVendorOnly,
                                                    onlyAmountDue:true,
                                                    accid:storeRecord.data['accid'],
                                                    isReceipt:this.isReceipt, 
                                                    customerid:customerIdCnDn,
                                                    vendorid:vendorIdCnDn,
                                                    deleted:false,
                                                    nondeleted:true, 
                                                    currencyfilterfortrans : this.Currency.getValue(), 
                                                    direction:direction, 
                                                    isLifoFifo:isLifoFifo
                                                }
                                            });
                                        }
                                    },this);
                                }
                            }

                } else {
                        if(this.isReceipt){//Customer
                            this.grid.getStore().proxy.conn.url = (this.isCustBill)? "ACCInvoiceCMN/getBillingInvoices.do" : "ACCInvoiceCMN/getInvoices.do";
                            this.cndnGrid.getStore().proxy.conn.url = 'ACCDebitNote/getDebitNoteMerged.do';
                        }else{//vendor
                            this.grid.getStore().proxy.conn.url = (this.isCustBill)? "ACCGoodsReceiptCMN/getBillingGoodsReceipts.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
                            this.cndnGrid.getStore().proxy.conn.url = 'ACCCreditNote/getCreditNoteMerged.do';
                        }
                        this.cndnGrid.getView().emptyText = "<div class='grid-empty-text'>"+(this.isReceipt?WtfGlobal.getLocaleText("acc.rem.223"):WtfGlobal.getLocaleText("acc.rem.224"));
                        if(b!=undefined){//Anup need to handle it Edit Case 
                            var isVendorOnly = b.data['isVendor'];
                            if(this.isReceipt){//Customer
                                isVendorOnly=false;
                            }else{//vendor
                                isVendorOnly=true;
                            }
                            var vendorId="";
                            var customerId="";
                            var idxCNDN = this.Name.store.find('accid', this.Name.getValue());
                            var cmbRecCNDN = this.Name.store.getAt(idxCNDN);
                            if(isVendorOnly !=undefined && isVendorOnly){
                                vendorId=this.Name.getValue();
                            }
                            if(isVendorOnly !=undefined && !isVendorOnly){
                                customerId=this.Name.getValue();
                            } 
//                            var isVendorOnly = b.data['isVendor'];
                                var cndnGridParams={
                                            transactiontype:transactiontype,
                                            cntype:8,
                                            isNoteForPayment:true,
                                            isVendor:isVendorOnly,
                                            onlyAmountDue:true,
                                            accid:b.data['accid'],
                                            customerid:customerId,
                                            vendorid:vendorId,
                                            isReceipt:this.isReceipt, 
                                            deleted:false,
                                            nondeleted:true, 
                                            currencyfilterfortrans : this.Currency.getValue(), 
                                            direction:direction, 
                                            isLifoFifo:isLifoFifo
                                }; 
                                cndnGridParams.vendorid=vendorId,
                                cndnGridParams.customerid=customerId,
                                cndnGridParams.isVendor=isVendorOnly

                            this.cndnGrid.getStore().load({
                                params:cndnGridParams
                            });
                        }
//                        this.cndnGrid.getStore().load({params:{transactiontype:transactiontype,cntype:8,isNoteForPayment:true,isVendor:isVendorOnly,onlyAmountDue:true,accid:b.data['accid'],isReceipt:this.isReceipt, deleted:false,nondeleted:true, currencyfilterfortrans : this.Currency.getValue(), direction:direction, isLifoFifo:isLifoFifo}});
//                        }
                        if(this.directPayment) {
                            this.grid.getStore().load({params:{billid:this.invoiceRecord.data.billid,deleted:false,
                            nondeleted:true}});
                            this.grid.getStore().on("load",function(store){
                                if(store.getCount()>1){
                                    store.filter("billid",this.invoiceRecord.data.billid);
                                    this.updateAmount();
                                    this.updateSouthTemp();
                                    this.hideAdvanceAmount();
                                }
                            },this);
                        } else {
                            if(this.isEdit&&this.actualReceiptType==0){                       
                                    this.Name.setValue(this.record.data.personid);                                    
                            } 
                            
                           this.cndnGrid.getStore().on("load",function(store){
                                if(this.isEdit&&(this.actualReceiptType==0||this.actualReceiptType==1)){
                                    this.isadvance.setValue(true);
                                    this.cndnGrid.winType="7";
                                    this.cndnGrid.reconfigureCreditDebitNoteGrid(this.isMultiDebit, this.record.data,this.actualReceiptType,true);
                                    this.cndnGrid.show();
                                    this.checkAmount(this.Amount,this.Amount.getValue());
                                }
                            },this);
                            if(this.isEdit&&(this.actualReceiptType==0||this.actualReceiptType==1)){
                                if(this.cndnGrid.getStore().getCount()==0){
                                    if(this.isReceipt){//Customer
                                        this.cndnGrid.getStore().proxy.conn.url = 'ACCDebitNote/getDebitNoteMerged.do';
                                    }else{//vendor
                                        this.cndnGrid.getStore().proxy.conn.url = 'ACCCreditNote/getCreditNoteMerged.do';
                                    }
                                    var paramsCNDN="";
                                    this.Name.setValue(this.record.data.personid);
                                    this.personAccStore.proxy.conn.url= "ACCAccountCMN/getVenorCustomerForCombo.do"; 
                                    paramsCNDN={
                                        mode:2,
                                        //                        group:(this.val==1)?10:13,
                                        ignoreGLAccounts:false,
                                        deleted:false,
                                        nondeleted:true,
                                        common:'1',
                                        combineData:-1  //Send For Seprate Request
                                    }

                                    this.personAccStore.proxy.conn.url= "ACCAccountCMN/getVenorCustomerForCombo.do"; 
                                    this.personAccStore.load({
                                        params:paramsCNDN
                                    });
                                    this.Name.setValue(this.record.data.personid);
                                    this.personAccStore.on('load',function(){
                                        var recordIndex = this.Name.store.find('accid',this.Name.getValue());
                                        var storeRecord = this.Name.store.getAt(recordIndex);
                                        
                                        if(storeRecord!=undefined){//Anup need to handle it Edit Case 
                                            var isVendorOnly = storeRecord.data['isVendor'];
                                            var vendorIdCnDn="";
                                            var customerIdCnDn="";
                                            var idxCNDN = this.Name.store.find('accid', this.Name.getValue());
                                            var cmbRecCNDN = this.Name.store.getAt(idxCNDN);
                                            if(isVendorOnly !=undefined && isVendorOnly){
                                                vendorIdCnDn=this.Name.getValue();
                                            }
                                            if(isVendorOnly !=undefined && !isVendorOnly){
                                                customerIdCnDn=this.Name.getValue();
                                            } 
                                            this.cndnGrid.getStore().load({
                                                params:{
                                                    transactiontype:transactiontype,
                                                    cntype:8,
                                                    isNoteForPayment:true,
                                                    isVendor:isVendorOnly,
                                                    onlyAmountDue:true,
                                                    accid:storeRecord.data['accid'],
                                                    isReceipt:this.isReceipt, 
                                                    deleted:false,
                                                    nondeleted:true, 
                                                    customerid:customerIdCnDn,
                                                    vendorid:vendorIdCnDn,
                                                    currencyfilterfortrans : this.Currency.getValue(), 
                                                    direction:direction, 
                                                    isLifoFifo:isLifoFifo
                                                }
                                            });
                                        }
                                    },this);
                                }
                            }
                            this.grid.getStore().on("load",function(store){
                            if(this.isEdit&&this.actualReceiptType==0){
                                    this.isadvance.setValue(true);
                                    this.advanceAmount.setValue(this.Amount.getValue());
                                    this.checkAmount(this.Amount,this.Amount.getValue());
                            }
                            if((this.actualReceiptType!=0&&this.isEdit)||!this.isEdit){
                              if(sortingInfo != undefined && (sortingInfo == "LIFO" || sortingInfo == "FIFO" || sortingInfo == "NA" )) {
                                  var isLifoFifo = true;

                                    if(sortingInfo == "NA"){
                                        this.Amount.setValue("0");
                                        this.grid.updateAmount(0,isLifoFifo);
                                        amountValue = "";
                                    }

                                    this.lifoFifoCombo.value = "";
                                    this.Amount.value = 0;
                                    sortingInfo = "";

                                    if(amountValue != undefined && amountValue != ""){
                                        this.grid.updateAmount(amountValue,isLifoFifo);
                                        amountValue = "";
                                    }

                                    this.grid.lifoAmountValue=0;
                                    this.onCheckLifoFifoFlage = true;
                                  this.grid.onCheckLifoFifoFlage = true;
                              } else {
    //                              store.sort('date', "ASC");
                                  this.grid.updateAmount(0,isLifoFifo);
//                                  this.onCheckLifoFifoFlage = false;
//                                  this.grid.onCheckLifoFifoFlage = false;
                              }  
                            }
                            },this);
                             var direction = "";
                            if(sortingInfo != undefined && (sortingInfo == "LIFO" || sortingInfo == "FIFO" || sortingInfo == "NA" )) {
                                var isLifoFifo = true;
                                if(sortingInfo == "LIFO"){
                                    direction = "desc";
                                } else if((sortingInfo == "FIFO" || sortingInfo == "NA")){
                                    direction = "asc";
                                }
                            }
                            //Need to comment below line to remove recursive error
                            if(this.val!=1){//disable in case of MP/RP against Vendor/Customer Invoice.
                                this.setCurrency(b.data['currencyid']);
                            }
                            this.grid.getStore().load({params:{onlyAmountDue:true,accid:this.Name.getValue(),mode:((this.val=="3")?16:12),deleted:false,nondeleted:true, currencyfilterfortrans : this.Currency.getValue(), direction:direction, isLifoFifo:isLifoFifo,isReceipt:this.isReceipt}});               
                            this.grid.amount=this.Amount.getValue();
                        }
                }
           }else{
               this.setCurrency(b.data['currencyid'])
           }
           this.loadContraGrid();
       } else {
//           this.grid.advancePayAcc = b.data.accid;
           var len= this.grid.store.getCount();
           for(var i=0;i<len;i++){
               this.grid.store.getAt(i).data.accountid = b.data.accountid
           }
       }
       this.hideAdvanceAmount();
    },   
    setroundedvalue:function(obj){
        var bankchares=obj.getValue();
        if(bankchares!="" && bankchares!=undefined){
           var roundedvalue=parseFloat(getRoundofValueWithValues(bankchares,Wtf.AMOUNT_DIGIT_AFTER_DECIMAL).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
           obj.setValue(roundedvalue);    
        }        
    },
    applyTemplate:function(store,index){
        var isedit=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!="";
        if(this.val=="4"){//Cureency rate can not be change in contra entry
            isedit =false;
        }
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if(this.externalcurrencyrate>0) {
            exchangeRate = this.externalcurrencyrate;
        } else if(this.isEdit && this.record.data.externalcurrencyrate){
            var externalCurrencyRate = this.record.data.externalcurrencyrate-0;
            if(externalCurrencyRate>0){
                exchangeRate = externalCurrencyRate;
            }
        }
        var revExchangeRate = 1/(exchangeRate-0);
        if(this.exchangeratetype!=undefined&&this.exchangeratetype=="foreigntobase"&&this.revexternalcurrencyrate!=undefined&&this.revexternalcurrencyrate!=0)
        {
            revExchangeRate=this.revexternalcurrencyrate
            this.revexternalcurrencyrate=0;
        }
        revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
       this.southCenterTplSummary.overwrite(this.southCenterTpl.body,{foreigncurrency:store.getAt(index).data['currencyname'],exchangerate:exchangeRate,basecurrency:WtfGlobal.getCurrencyName(),editable:isedit,revexchangerate:revExchangeRate
            });
    },
    setCurrency:function(currencyid){
        if(currencyid==undefined){
            this.Currency.setValue(WtfGlobal.getCurrencyID());
            this.currencyid=WtfGlobal.getCurrencyID();
        }
        this.Currency.setValue(currencyid);
        this.currencyid=currencyid;
        this.getCurrencySymbol() ;
        this.updateSouthTemp();
        this.ApplyCurrencySymbol();
        this.hideAdvanceAmount();
//        this.fetchPaymentAccBalance();
    },
    updateAmount:function(){   
        this.amtdue=(this.isMultiDebit?this.Amount.getValue():this.grid.getAmountAmountPaid(false));
         this.amtdueCNDN=this.cndnGrid.getAmount(false);
         this.amtdue+=this.amtdueCNDN;
         if(!this.isMultiDebit){
             this.Amount.enable();
             this.grid.onCheckLifoFifoFlage = true;
            if(this.setEditableData){
            if(this.record.data.advanceid!=undefined&&this.record.data.advanceid!=""&&this.isEdit){
                this.isadvance.setValue(true);
                this.Amount.setValue(getRoundedAmountValue((this.amtdue*1)+(this.record.data.advanceamount*1)));
                this.advanceAmount.setValue(this.record.data.advanceamount);
            }else{
                if(this.actualReceiptType==0){
                    this.Amount.setValue(getRoundedAmountValue((this.record.data.amount*1)+(this.amtdue*1)));
//                    this.Amount.setValue(this.record.data.amount);
                }else{
                    if(this.actualReceiptType==1){
                        this.Amount.setValue(this.amtdue);
                    }else{
                        this.Amount.setValue(this.record.data.amount);
                    }
                }
            }
                this.grid.amount=this.Amount.getValue();
                this.grid.updateAmount(this.Amount.getValue());
            }
            else{
               if((this.val == '1' || this.val == '3') && (this.lifoFifoCombo.getValue() == "" )){
                    this.Amount.setValue(0);	//this.Amount.setValue(this.grid.getAmount(true).toFixed(2));				// Amount NumberField not used anymore in make or recieve payment logic     Neeraj                    
                    this.lifoFifoCombo.setValue("1");
                    this.lifoFifoCombo.disable();
                    this.grid.amount=0;
                    this.grid.updateAmount(0,true);
               } else if (this.lifoFifoCombo.getValue() == ""){
                   this.Amount.setValue(getRoundedAmountValue(this.grid.getAmount(true)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));				// Amount NumberField not used anymore in make or recieve payment logic     Neeraj
                   this.grid.amount=0;
                   this.grid.updateAmount(0);
               }

            }
        }
        this.hideAdvanceAmount();
    },
    updateAmountPaymentField:function(){  
            if(this.isMultiDebit && (this.val!="7" && this.val!="8")){
                this.amtmultidebit=this.grid.getMultiDebitAmount();
                    this.Amount.enable();
                    this.grid.onCheckLifoFifoFlage = true;
                    this.Amount.setValue(getRoundedAmountValue(this.amtmultidebit*1));
            }else{
                this.amtinvoice=this.grid.getAmount(false);
                this.amtCNDN=this.cndnGrid.getAmount(false);
//                if(!this.isMultiDebit){
                    this.Amount.enable();
//                    if((this.amtCNDN*1+this.amtinvoice*1)>0 || !(this.lifoFifoCombo.getRawValue() == "NA" || this.lifoFifoCombo.getRawValue()=="")){
                    if((this.amtCNDN*1+this.amtinvoice*1)>0){
                        this.lifoFifoCombo.enable();
                    }else{
                        this.lifoFifoCombo.setValue("1");
                        this.lifoFifoCombo.disable();
                    }
                    
                    this.grid.onCheckLifoFifoFlage = true;
                    this.Amount.setValue(getRoundedAmountValue(this.amtCNDN*1+this.amtinvoice*1));
//                    if (this.val == '1' && !(this.lifoFifoCombo.getRawValue() == "NA" || this.lifoFifoCombo.getRawValue()=="")&&(this.amtCNDN*1+this.amtinvoice*1)>0) {
//                        var storeRecord = WtfGlobal.searchRecord(this.lifoFifoStore, this.lifoFifoCombo.getValue(), "id");
//                        var recordIndex = WtfGlobal.searchRecordIndex(this.lifoFifoStore, this.lifoFifoCombo.getValue(), "id")
//                        this.refreshGrid(this.lifoFifoCombo, storeRecord, recordIndex);
//                    }
//                }
                this.hideAdvanceAmount();
            }
    },
    checkAmount:function(a,b){                    
        if(this.isMultiDebit)
            this.updateSouthTemp();
         this.amtdue=(this.isMultiDebit?this.Amount.getValue():this.grid.getAmount(false));
         this.amtdueCNDN=this.cndnGrid.getAmount(false);
         this.amtdue+=this.amtdueCNDN;
        if((this.val=="1" && !this.isEdit)||this.val=="3"||(this.actualReceiptType==0 &&this.isEdit))
            if(b>this.amtdue){
            if(this.val=="1"){
                var valueDiff=b-this.amtdue;
                //            this.Amount.setValue(this.amtdue.toFixed(2));
                this.isadvance.setValue(true);
                this.advanceAmount.setValue(valueDiff);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.8")],2);
                this.Amount.setValue(getRoundedAmountValue(this.amtdue).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
            }
        }
//        if (b > 0 || this.paymentCriteria != 1) { // this.paymentCriteria != 1 - to avoid the case of reset selection criteria to NA when amount is changed.
        if (b > 0) { // this.paymentCriteria != 1 - to avoid the case of reset selection criteria to NA when amount is changed.
            this.lifoFifoCombo.enable()
        } else {
            this.lifoFifoCombo.setValue("1");
            this.grid.updateAmount(0,true);
            this.lifoFifoCombo.disable();
        }
        if(!this.isMultiDebit && !this.Amount.hidden) {//Added checks for amount entered in payment column should not be greater than amount entered in top Amount field.
            var isLifoFifo = true;            
            if(this.lifoFifoCombo.getRawValue() == "NA" || this.lifoFifoCombo.getRawValue()==""){
                this.grid.updateAmount(0,isLifoFifo);
                this.cndnGrid.updateAmount(0,isLifoFifo);
            }
//            this.onCheckLifoFifoFlage = true;
            this.grid.onCheckLifoFifoFlage = true;
            this.grid.lifoAmountValue = this.Amount.getValue();
            this.grid.sharedGrid=this.cndnGrid;
            this.cndnGrid.lifoAmountValue=this.Amount.getValue();
            this.cndnGrid.sharedGrid=this.grid;
        }
        
//        if (this.val == '1' && this.paymentCriteria != 1) {
//            var storeRecord = WtfGlobal.searchRecord(this.lifoFifoStore, this.lifoFifoCombo.getValue(), "id");
//            var recordIndex = WtfGlobal.searchRecordIndex(this.lifoFifoStore, this.lifoFifoCombo.getValue(), "id")
//            this.refreshGrid(this.lifoFifoCombo, storeRecord, recordIndex);
//        }
        
        this.hideAdvanceAmount();
    },
    setLifoFifoAmount:function(a,b){                    
            this.grid.lifoAmountValue = this.advanceAmount.getValue();
            this.cndnGrid.lifoAmountValue=this.advanceAmount.getValue();
            if(this.advanceAmount.getValue()==0){
                this.isadvance.setValue(false);
            }
             var datainvoiceadvcndn= this.record.data.datainvoiceadvcndn;
             var dataCNDNFlag=false;
                if(datainvoiceadvcndn!=undefined){
                    for(var i=0;i<datainvoiceadvcndn.length;i++){
                        if(datainvoiceadvcndn[i].invoiceadvcndntype==1){
                            dataCNDNFlag=true;
                        }
                    }
                }
                if(datainvoiceadvcndn!=undefined&&!dataCNDNFlag){
                     for(var i=0; i<this.grid.store.getCount();i++){
                        this.grid.store.getAt(i).set('payment',0);
                                this.grid.store.getAt(i).data.select = false;
                                this.grid.store.getAt(i).commit();
                        }
                }
            
            
    },
    updateAmountForAdvance:function(){   
    this.amtdue=(this.isMultiDebit?this.Amount.getValue():this.grid.getAmount(false));
    this.amtdueCNDN=this.cndnGrid.getAmount(false);
    this.amtdue+=this.amtdueCNDN;
    var b=this.Amount.getValue();
    if(this.val=="1"){
        if(b >= this.amtdue && b!=0){
            if(this.val=="1"){
                var valueDiff=b-this.amtdue;
               if(valueDiff>0 &&this.isadvance.getValue()){ 
                this.isadvance.setValue(true);
                valueDiff=getRoundedAmountValue(valueDiff).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
                this.advanceAmount.setValue(valueDiff);
               }else if(this.isadvance.getValue()){
                    this.isadvance.setValue(false);
                    this.advanceAmount.setValue(valueDiff);
                } else if(!this.isadvance.getValue() && this.amtdue<b && b>0){
                    this.isadvance.setValue(true);
                    valueDiff=getRoundedAmountValue(valueDiff).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
                    this.advanceAmount.setValue(valueDiff);
                }
            }else{
                this.advanceAmount.setValue(0);
                this.isadvance.setValue(false);                
            }           
        }    
         if(this.amtdue==0 && b==0 && this.isadvance.getValue()){
            this.advanceAmount.setValue(0);
            this.isadvance.setValue(false);
        }
         if( this.advanceAmount.getValue()==0 && this.isadvance.getValue()){
            this.isadvance.setValue(false);
        }
        this.grid.sharedGrid=this.cndnGrid;
        this.cndnGrid.sharedGrid=this.grid;              
    }
},
    updateSouthTemp:function(){
        this.ApplyCurrencySymbol();
        if(this.isMultiDebit && (this.val!="7" && this.val!="8")){
           this.getCurrencySymbol();
           //in make payment otherwise, Debit Amount should be auto fetched in Amount field.
           //this.Amount.setValue(this.grid.getMultiDebitAmount());getMultiDebitTaxAmount
              this.tplSummary.overwrite(this.southCalTemp.body,{taxamount:WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitTaxAmount(),this.symbol),amountwithouttax:WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount()-this.grid.getMultiDebitTaxAmount(),this.symbol),received:WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount(),this.symbol),due:WtfGlobal.addCurrencySymbolOnly(this.Amount.getValue(),this.symbol),receivable:WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitAmount()-this.Amount.getValue(),this.symbol),receivableInBase:WtfGlobal.addCurrencySymbolOnly(this.getPayableAmountInBase(),WtfGlobal.getCurrencySymbol()),receivedinbase:WtfGlobal.addCurrencySymbolOnly(this.getAmountPaidInBase(this.grid.getMultiDebitAmount()),WtfGlobal.getCurrencySymbol())});
           
           }  
        else{
                if(this.val=="1"){
                   this.updateAmountForAdvance();
                }
                this.getCurrencySymbol();
                if(this.cndnGrid.getAmount(true)>0){
                        var actualAmount=getRoundedAmountValue(this.grid.getAmount(false)+this.cndnGrid.getAmount(false));
                        var totalAmount=getRoundedAmountValue(this.grid.getAmount(true)+this.cndnGrid.getAmount(true));
                        this.tplSummary.overwrite(this.southCalTemp.body,{taxamount:WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitTaxAmount(),this.symbol),amountwithouttax:(WtfGlobal.addCurrencySymbolOnly(actualAmount,this.symbol)),received:WtfGlobal.addCurrencySymbolOnly(actualAmount,this.symbol),due:WtfGlobal.addCurrencySymbolOnly(totalAmount,this.symbol),receivable:WtfGlobal.addCurrencySymbolOnly(totalAmount-actualAmount,this.symbol),receivableInBase:WtfGlobal.addCurrencySymbolOnly(this.getPayableAmountInBaseCNDN(),WtfGlobal.getCurrencySymbol()),advance:WtfGlobal.addCurrencySymbolOnly(this.Amount.getValue()-actualAmount,this.symbol),advanceInBase:WtfGlobal.addCurrencySymbolOnly(this.getAdvanceAmountInBase(),WtfGlobal.getCurrencySymbol()),receivedinbase:WtfGlobal.addCurrencySymbolOnly(this.getAmountPaidInBase(actualAmount),WtfGlobal.getCurrencySymbol())});
           } else{
                        this.tplSummary.overwrite(this.southCalTemp.body,{taxamount:WtfGlobal.addCurrencySymbolOnly(this.grid.getMultiDebitTaxAmount(),this.symbol),amountwithouttax:(WtfGlobal.addCurrencySymbolOnly(this.grid.getAmount(false),this.symbol)),received:WtfGlobal.addCurrencySymbolOnly(this.grid.getAmount(false),this.symbol),due:WtfGlobal.addCurrencySymbolOnly(this.grid.getAmount(true),this.symbol),receivable:WtfGlobal.addCurrencySymbolOnly(this.grid.getAmount(true)-this.grid.getAmount(false),this.symbol),receivableInBase:WtfGlobal.addCurrencySymbolOnly(this.getPayableAmountInBase(),WtfGlobal.getCurrencySymbol()),advance:WtfGlobal.addCurrencySymbolOnly(this.advanceAmount.getValue(),this.symbol),advanceInBase:WtfGlobal.addCurrencySymbolOnly(this.getAdvanceAmountInBase(),WtfGlobal.getCurrencySymbol()),receivedinbase:WtfGlobal.addCurrencySymbolOnly(this.getAmountPaidInBase(this.grid.getAmount(false)),WtfGlobal.getCurrencySymbol())});
           }
           }
           this.hideAdvanceAmount();
            if(this.val=="1"){
                this.updateAmountForAdvance();
            }
    },
    addBusinessPerson:function(isEdit,rec,winid,isCustomer){
        if(this.val!="1" ||(this.val!="2" || this.val!="9"&& this.personwin))
            this.checkperson(isEdit,rec,"perwindow",isCustomer)
       else{
            callBusinessContactWindow(isEdit, rec, null, isCustomer);
            var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
            Wtf.getCmp(tabid).on('update', function(){
                this.isReceipt?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
                this.personAccStore.reload();
            }, this);
       }
    },
    checkperson:function(){
           callAccountTypeWindow('perwindow',this.isReceipt,true);
           Wtf.getCmp('perwindow').on('update',this.addwin,this);
    },
    addwin:function(scope,val){
        var win='custwin'
        if(val=="1"){
          callBusinessContactWindow(false, null, null, this.isReceipt);
          var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab'; 
        }else{
            win='coaWin'
            callCOAWindow(false, null, "coaWin");
        }
         Wtf.getCmp(tabid).on('update', function(){
            this.isReceipt?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
            this.personAccStore.reload();
        }, this);
    },
    addPaymentMethod:function(){
      PaymentMethod('PaymentMethodWin');
      Wtf.getCmp('PaymentMethodWin').on('update', function(){
            this.pmtStore.reload();
      }, this);
    },
    
    addPaidTo:function(){
      this.isReceipt ? addMasterItemWindow('18'):addMasterItemWindow('17');
      Wtf.getCmp("masterconfiguration").on('update', function(){
             this.paidTo.store.reload();
        }, this);
      
    },
    
  refreshBtnHandler:function(){

        if(this.LifoFifoIndex != undefined){
            this.paymentCriteria = this.lifoFifoCombo.getValue();
            this.refreshGrid(this.lifoFifoCombo,null,this.LifoFifoIndex);
            
        }    
        
    },
    refreshGrid:function(combo, record, index ) {
//        if(index != 0){
            var customerName = this.Name.getValue();
            var amountValue = this.Amount.getValue();
            var LifoFifoValue = "";
            if(record != undefined){
                LifoFifoValue = record.data.name;
                this.LifoFifoIndex = record.data.id;
            } else {
                record  = this.lifoFifoStore.getAt(index-1);
                LifoFifoValue = record.data.name;
                this.lifoFifoCombo.setValue(index);
            }
            if(customerName != "" ) {
                if(amountValue != ""){
                    if(LifoFifoValue == "NA"){
                        amountValue = 0;
                    }
                    var recordIndex = this.Name.store.find('accid',this.Name.getValue());
                    var storeRecord = this.Name.store.getAt(recordIndex);
                    this.loadGrid(undefined,storeRecord,undefined,LifoFifoValue,amountValue);
//                    this.grid.updateAmount(amountValue,amountValue)
                } else {
                    Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.error"),
                    msg: WtfGlobal.getLocaleText("acc.field.Pleaseenteramount"),
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.ERROR,
                    scope: this
                });
                }
            } else {
                    Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.error"),
                    msg: WtfGlobal.getLocaleText("acc.field.Pleaseselect")+(this.isReceipt?WtfGlobal.getLocaleText("acc.up.3"):WtfGlobal.getLocaleText("acc.up.4")),
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.ERROR,
                    scope: this
                });
            }  
            this.hideAdvanceAmount();
//        }
    },
    getCurrencySymbol:function(){
       var index=null;
//       this.currencyStore.clearFilter(true) //ERP-9962
       var FIND = this.Currency.getValue();
       index=this.currencyStore.findBy( function(rec){
       var parentname=rec.data['currencyid'];
       if(parentname==FIND)
            return true;
         else
            return false
       })
        if(index>=0)
            this.symbol=  this.currencyStore.getAt(index).data['symbol'];
         return index;
    },
    
    getPayableAmountInBase:function(){
        var amountInOriganalCurr = 0;
        var amountInBaseCurr = 0;
        if(this.isMultiDebit){
            amountInOriganalCurr = this.grid.getMultiDebitAmount()-this.Amount.getValue();
        }else{
            amountInOriganalCurr = this.grid.getAmount(true)-this.grid.getAmount(false);
        }
        amountInBaseCurr = amountInOriganalCurr*this.getExchangeRate();
       return amountInBaseCurr;
    },
    getPayableAmountInBaseCNDN:function(){
        var amountInOriganalCurr = 0;
        var amountInBaseCurr = 0;
        if(this.isMultiDebit){
            amountInOriganalCurr = this.grid.getMultiDebitAmount()-this.Amount.getValue();
        }else{
            amountInOriganalCurr = this.grid.getAmount(true)+this.cndnGrid.getAmount(true)-this.grid.getAmount(false)+this.cndnGrid.getAmount(false);
        }
        amountInBaseCurr = amountInOriganalCurr*this.getExchangeRate();
       return amountInBaseCurr;
    },
    getAdvanceAmountInBase:function(){
        var amountInOriganalCurr = 0;
        var amountInBaseCurr = 0;
        var actualAmount=getRoundedAmountValue(this.grid.getAmount(false)+this.cndnGrid.getAmount(false));
        var totalAmount=getRoundedAmountValue(this.grid.getAmount(true)+this.cndnGrid.getAmount(true));
        amountInOriganalCurr=this.Amount.getValue()-actualAmount;
        amountInBaseCurr = amountInOriganalCurr*this.getExchangeRate();
       return amountInBaseCurr;
    },
    getAmountPaidInBase:function(amountInOriganalCurr){
        var amountInBaseCurr = 0;
        amountInBaseCurr = amountInOriganalCurr*this.getExchangeRate();
       return amountInBaseCurr;
    },
    
    getExchangeRate:function(){
        var index=this.getCurrencySymbol();
        var rate=this.externalcurrencyrate;
        var revExchangeRate = 0;
        if(index>=0){
            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
            if(this.externalcurrencyrate!=undefined&&this.externalcurrencyrate>0) {
                exchangeRate = this.externalcurrencyrate;
            }else if(this.isEdit && this.record.data.externalcurrencyrate!=undefined){
               var externalCurrencyRate = this.record.data.externalcurrencyrate-0;
               if(externalCurrencyRate>0){
                   exchangeRate = externalCurrencyRate;
            }
           }
            revExchangeRate = 1/(exchangeRate);
            revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
        }
        return revExchangeRate;
    },

    ApplyCurrencySymbol:function(){
       var index=this.getCurrencySymbol();
       if(index>=0){
            this.grid.setCurrencyid(this.currencyid,this.symbol,index);
            this.applyTemplate(this.currencyStore,index);
       }
    },    
    createGrid:function(){
         var type= this.isReceipt?'invoice':'Vendor invoice';

         this.grid = new Wtf.account.OSDetailGrid({
           region:'center', 
           border:true,
           currencyid:this.currencyid,
           isEdit:this.isEdit,
           moduleid: this.moduleid,
           height:200,
           winType:this.val,
           receiptObject:this,
           cellClickView:((this.actualReceiptType==9)&&this.readOnly)?true:false,
           disabled: (this.actualReceiptType==9)?false:this.readOnly,
           hidden: (this.actualReceiptType=='6' && this.readOnly)?true:false,
           journalentryid: this.isEdit?this.record.data.journalentryid:"",
           cls:'gridFormat', 
           viewConfig:{forceFit:false, emptyText:"<div class='grid-empty-text'>"+WtfGlobal.getLocaleText("acc.field.No")+type+WtfGlobal.getLocaleText("acc.field.ismadeagainstthis")+this.businessPerson+"</div><br><br>"+WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick=\"javascript: openInv("+this.isReceipt+","+this.isCustBill+",'"+this.Name.getValue()+"')\">"+WtfGlobal.getLocaleText("acc.field.GetStartedbyaddinga")+this.label+WtfGlobal.getLocaleText("acc.rem.148")+"</a>")},
           isReceipt:this.isReceipt,
           isMultiDebit:this.isMultiDebit,
           amount:0,
           id : this.id+(this.isReceipt ? 'customergrid' : 'vendorgrid'),
           closable: true
    });
    },
    
    ShowCheckDetails:function(combo,rec){
        
        this.checkIsIBGTransaction(rec);
        
        this.savePrintBttn.hide();
//        if(this.val ==2 || this.val==9 || this.val==1){//Make/Receive Payment : Commented check of "Receive/Make payment otherwise"
            //var index = this.currencyStore.find('currencyid',rec.data['acccurrency']);
            var accCurr = rec.data['acccurrency'];
            var index = this.currencyStore.findBy( function(rec){
                        var currid=rec.data['currencyid'];
                        if(currid==accCurr)
                            return true;
                        else
                            return false
                    }, this);
                    
            if(index != -1){
                this.bankAccCurrency=rec.data['acccurrency'];
                this.bankAccSelected=true;
                this.Currency.setValue(rec.data['acccurrency']);
                this.Currency.fireEvent("select", this.Currency, this.currencyStore.getAt(index));
            }
//        }
        
        if(this.contraentryflag) {
             var panelHeight=this.wrapperNorth.height
            this.wrapperNorth.setHeight(panelHeight+130);
            this.SouthForm.hide();    
            this.invGrGrid.show();
        } else {
            var methodAccindx = this.pmtStore.find('methodid',rec.data['methodid']);
            var paymentMethodAccountId = "";
            if(methodAccindx != -1){
                this.pmtMethodAcc.setValue(this.pmtStore.getAt(methodAccindx).get("accountname"));
                paymentMethodAccountId = this.pmtStore.getAt(methodAccindx).get("accountid");
            }
            this.fetchPaymentAccBalance();

            this.SouthForm.ShowCheckDetails(rec.data['detailtype']);
            if(rec.data['detailtype']==2) {//Shown only if make payment and peyment methos bank account.
                if(!this.isReceipt){
                    this.savePrintBttn.show()
                    
                    // set auto generated cheque no. in case of make payment and if bank account is selected
                    if(!this.isEdit){
                        this.SouthForm.setNextChequeNumber(paymentMethodAccountId);
                    }
                };
                 var panelHeight=this.wrapperNorth.height
                 this.wrapperNorth.setHeight(panelHeight+130);
                this.SouthForm.hide();
                WtfGlobal.showFormElement(this.SouthForm.checkNo);
//                this.SouthForm.checkNo.allowBlank = false;
                WtfGlobal.showFormElement(this.SouthForm.paymentStatus);
                WtfGlobal.showFormElement(this.SouthForm.bank);
                this.SouthForm.bank.allowBlank = false;
                WtfGlobal.showFormElement(this.SouthForm.description);

                WtfGlobal.hideFormElement(this.SouthForm.clearanceDate);
                this.SouthForm.clearanceDate.allowBlank = true;
                WtfGlobal.hideFormElement(this.SouthForm.expDate);
                this.SouthForm.expDate.allowBlank = true;
                WtfGlobal.hideFormElement(this.SouthForm.nameOnCard);
                this.SouthForm.nameOnCard.allowBlank = true;
                WtfGlobal.hideFormElement(this.SouthForm.cardType);
                this.SouthForm.cardType.allowBlank = true;
                WtfGlobal.hideFormElement(this.SouthForm.refNo);
                this.SouthForm.refNo.allowBlank = true;
                WtfGlobal.hideFormElement(this.SouthForm.cardNo);
                this.SouthForm.cardNo.allowBlank = true;
                this.SouthForm.cheque.setTitle(WtfGlobal.getLocaleText("acc.nee.43"));
                this.SouthForm.PostDate.setValue(this.creationDate.getValue());
                this.SouthForm.show();
                this.SouthForm.cheque.doLayout();
                this.SouthForm.getForm().items.items[1].disabled = false;
            } else if(rec.data['detailtype']==1) {                
                var panelHeight=this.wrapperNorth.height
            this.wrapperNorth.setHeight(panelHeight+130);
                this.SouthForm.hide();
                WtfGlobal.hideFormElement(this.SouthForm.checkNo);
                this.SouthForm.checkNo.allowBlank = true;
                WtfGlobal.hideFormElement(this.SouthForm.paymentStatus);
                this.SouthForm.paymentStatus.allowBlank = true;
                WtfGlobal.hideFormElement(this.SouthForm.bank);
                this.SouthForm.bank.allowBlank = true;
                WtfGlobal.hideFormElement(this.SouthForm.clearanceDate);
                this.SouthForm.clearanceDate.allowBlank = true;
                WtfGlobal.hideFormElement(this.SouthForm.description);
                this.SouthForm.description.allowBlank = true;

                WtfGlobal.showFormElement(this.SouthForm.expDate);
                WtfGlobal.showFormElement(this.SouthForm.nameOnCard);
                this.SouthForm.nameOnCard.allowBlank = false;
                WtfGlobal.showFormElement(this.SouthForm.cardType);
                this.SouthForm.cardType.allowBlank = false;
                WtfGlobal.showFormElement(this.SouthForm.refNo);
                this.SouthForm.refNo.allowBlank = false;
                WtfGlobal.showFormElement(this.SouthForm.cardNo);
                this.SouthForm.cardNo.allowBlank = false;
                this.SouthForm.cheque.setTitle(WtfGlobal.getLocaleText("acc.nee.44"));
                this.SouthForm.show();
                this.SouthForm.getForm().items.items[1].disabled = false;
                this.SouthForm.cheque.doLayout();
            } else {
                var panelHeight=this.wrapperNorth.height
                this.wrapperNorth.setHeight(panelHeight+20);
                this.SouthForm.hide();
                this.SouthForm.getForm().items.items[1].disabled = true;
            }
            if(!this.isReceipt && rec.data.methodname != undefined){
                this.SouthForm.setBankName(rec.data.methodname);
            }
        }
        this.autopopulate=rec.data.autopopulate;
        if(this.grid!=undefined){
                this.grid.autopopulate = this.autopopulate;
                this.grid.lifoAmountValue = this.Amount.getValue();
        }
        if(this.cndnGrid!=undefined){
             this.cndnGrid.autopopulate = this.autopopulate;
             this.cndnGrid.lifoAmountValue=this.Amount.getValue();
        }
        
        this.wrapperNorth.doLayout();
        this.southPanel.doLayout();
        this.grid.doLayout();
        this.doLayout();
        
    },
    
    checkIsIBGTransaction : function(rec) {
        if(Wtf.account.companyAccountPref.activateIBG && !this.isReceipt && (this.val == "1" || this.val == "9") && rec.data['isIBGBankAccount'] && rec.data['detailtype'] == 2) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), // 'Warning',
                msg: WtfGlobal.getLocaleText("acc.msg.mpIsIBGPayment"),
                buttons: Wtf.MessageBox.YESNO,
                fn:function(btn) {
                    if(btn!="yes") {
                        this.isIBGTypeTransaction = false;
                        this.ibgTransactionCode.disable();
                        if(this.paidTo != undefined) {
                            this.paidTo.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.mp.paidTo"));
                            this.paidTo.allowBlank = true;
                        }
                        return;
                    }
                    
                    if(this.val == "1") {
                        this.personAccStore.proxy.conn.url= "ACCVendor/getVendorsForCombo.do"; 
                        var params = {
                            mode:2,
                            isIBGVendors : true,
                            ignoreGLAccounts : false,
                            deleted : false,
                            nondeleted : true,
                            common : '1',
                            combineData : -1  // Send For Seprate Request
                        }
                        this.personAccStore.load({
                            params:params
                        });
                    } else if(this.val == "9") {
                        if(this.paidTo != undefined) {
                            this.paidTo.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.mp.paidTo") + "*");
                            this.paidTo.allowBlank = false;
                        }
                        
                        Wtf.MPPaidToStore.load({
                            params:{isIBGPaidTo:true}
                        });
                    }
                    
                    this.isIBGTypeTransaction = true;
                    this.ibgTransactionCode.enable();
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
        } else {
            if(this.isIBGTypeTransaction && !this.isEdit) {
                if(this.val == "1") {
                    this.personAccStore.proxy.conn.url= "ACCVendor/getVendorsForCombo.do"; 
                    var params = {
                        mode:2,
                        ignoreGLAccounts : false,
                        deleted : false,
                        nondeleted : true,
                        common : '1',
                        combineData : -1  // Send For Seprate Request
                    }
                    this.personAccStore.load({
                        params:params
                    });
                } else if(this.val == "9") {
                    if(this.paidTo != undefined) {
                        this.paidTo.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.mp.paidTo"));
                        this.paidTo.allowBlank = true;
                    }
                    Wtf.MPPaidToStore.load();
                }
                this.isIBGTypeTransaction = false;
                this.ibgDetailsID = "";
                this.ibgTransactionCode.disable();
            }
        }
    },

    savePringCheque: function(a, b) {
        this.isChequePrint = true;  
        if(this.val=="9"){
           if(this.paidTo.getValue()==""||this.paidTo.getValue()==undefined){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PleaseEnterPaidto")],2);
                this.isChequePrint=false;
                this.enableSaveButtons();
                return;
           }   
        }    
        this.disableSaveButtons();
        this.save();
    },
    save:function(a,b){        
        var isMultidebitOrAdvCheck=this.isMultiDebit?this.grid.getMultiDebitAmount():(this.grid.getAmount(false)||this.cndnGrid.getAmount(false));
        if(this.No){
            this.No.setValue(this.No.getValue().trim());
        }
        if(this.isadvance.getValue() && !this.grid.hidden &&  isMultidebitOrAdvCheck== 0) {//Save as only advance
            this.validateForSaveAdvance();
        } else if(!this.isMultiDebit && this.grid.getAmount(false)==0&&this.cndnGrid.getAmount(false)){
            this.validateForSaveAdvance();
        } else {
//            if((!this.isMultiDebit && !this.grid.hidden)||this.readOnly){                     // Issue ERPS-902
//                    this.Amount.disable();
//            }            
            if(this.val=='5' || this.val=='6') {
                this.grid.store.getAt(0).data.dramount = this.Amount.getValue();
                isMultidebitOrAdvCheck=this.isMultiDebit?this.grid.getMultiDebitAmount():this.grid.getAmount(false);
            }
            var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
            var valid=this.NorthForm.getForm().isValid();
            var southFormValid = this.SouthForm.hidden ? true : this.SouthForm.getForm().isValid();
            if(southFormValid&&valid && isValidCustomFields&& (isMultidebitOrAdvCheck>0 || this.ismanydbcr.getValue())){                
                var amt=0;
                if(this.isMultiDebit){
//                    if((this.val=='2' || this.val=='9') && this.ismanydbcr.getValue()) {
                    if(this.ismanydbcr.getValue()) {//val = 1 in edit case
                        this.Amount.setValue(0);
                        if(isMultidebitOrAdvCheck<0) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isReceipt?WtfGlobal.getLocaleText("acc.mp.19"):WtfGlobal.getLocaleText("acc.mp.20")],2);
                            this.enableSaveButtons();
                            return;
                        }
                        this.Amount.setValue(isMultidebitOrAdvCheck);
                    } else {
                        amt=this.Amount.getValue();
                        var due=this.grid.getMultiDebitAmount();
                        if(amt==0 || amt==""||due==0){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.7")],2);
                            if(amt ==0){
                                this.Amount.markInvalid(WtfGlobal.getLocaleText("acc.mp.7"));
                            }
                            this.enableSaveButtons();
                            return;
                        }
                        if(amt!=due){
                            if(this.isReceipt){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rp.1")],2);
                            } else {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.6")],2);
                            }

                            this.Amount.markInvalid(WtfGlobal.getLocaleText("acc.mp.6"));
                            this.enableSaveButtons();
                            return;
                        }
                    }
                    if(this.grid.getData()=='[]'){
                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.5")],2);
                         this.enableSaveButtons();
                         return;
                    }
//                    else if((this.val==9||this.val==2)&&!this.ismanydbcr.getValue()){
//                         var arr = eval(this.grid.getData());
//                        var totalAmount = 0.0;
//                        for (var cnt = 0; cnt < arr.length; cnt++) {
//                            var obj = arr[cnt];
//                            if(!this.isReceipt) {//Make Payment and Debit Type
//                                if(arr[cnt].isdebit) {
//                                    if(arr[cnt].curamount!=undefined) {
//                                        totalAmount+=WtfGlobal.conventInDecimalWithoutSymbol(arr[cnt].curamount);
//                                    } else {
//                                        totalAmount+=WtfGlobal.conventInDecimalWithoutSymbol(arr[cnt].dramount);
//                                    }
//                                } else {
//                                    if(arr[cnt].curamount!=undefined) {
//                                        totalAmount-=WtfGlobal.conventInDecimalWithoutSymbol(arr[cnt].curamount);
//                                    } else {
//                                        totalAmount-=WtfGlobal.conventInDecimalWithoutSymbol(arr[cnt].dramount);
//                                    }
//                                }
//                            } else { //Receive Payment and Credit Type
//                                if(!arr[cnt].isdebit) {
//                                    if(arr[cnt].curamount!=undefined) {
//                                        totalAmount+=WtfGlobal.conventInDecimalWithoutSymbol(arr[cnt].curamount);
//                                    } else {
//                                        totalAmount+=WtfGlobal.conventInDecimalWithoutSymbol(arr[cnt].dramount);
//                                    }
//                                } else {
//                                    if(arr[cnt].curamount!=undefined) {
//                                        totalAmount-=WtfGlobal.conventInDecimalWithoutSymbol(arr[cnt].curamount);
//                                    } else {
//                                        totalAmount-=WtfGlobal.conventInDecimalWithoutSymbol(arr[cnt].dramount);
//                                    }
//                                }
//                            }
//                        }
//                        if(this.Amount.getValue()!=WtfGlobal.conventInDecimalWithoutSymbol(Math.abs(totalAmount))){
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.5")],2);
//                            return;
//                        }
//
//                    }
                }
                else{
                     amt=this.grid.getAmount(false);
                     due=this.grid.getAmount(true);
                     var amtCnDn=this.cndnGrid.getAmount(false);
                     var dueCnDn=this.cndnGrid.getAmount(true);
                     amt+=amtCnDn;
                     due+=dueCnDn;
                    if(amt==0 && due==0 && (this.val=="1"||this.val=="3") && this.Amount.getValue()!=0){
                        if(this.isReceipt){
                            this.Amount.enable();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.4")],2);
                        }else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.3")],2);
                        }
                         this.enableSaveButtons();
                         return;
                    }
                    var advance=this.advanceAmount.getValue();
                    if(this.Amount.getValue() != getRoundedAmountValue(amt).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL) && !this.isEdit && (this.val != "4" || this.val != "1")  && this.onCheckLifoFifoFlage){
                      this.Amount.enable();
                      var amountTag = "Paid"; //this for message
                      if(this.isReceipt){
                          amountTag = "Received";
                      }
                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnterAmountshouldbeequaltoAmount")+amountTag],2);
                      this.enableSaveButtons();
                      return;
                    }else if(this.Amount.getValue() != parseFloat(getRoundedAmountValue(amt+advance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL) && this.val == "1" && !this.isEdit)  { //Checked for this when advanced amount present
                      this.Amount.enable();
                      var amountTag = "Paid"; //this for message
                      if(this.isReceipt){
                          amountTag = "Received";
                      }
                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnterAmountshouldbeequaltoAmount")+amountTag],2);
                      this.enableSaveButtons();
                      return;
                    }

                    if((this.Amount.getValue()==0 || this.Amount.getValue()==""||(amt==0 && due!=0)) && this.onCheckLifoFifoFlage){
                        this.Amount.enable();
                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.2")],2);
                      this.enableSaveButtons();
                      return;
                    }      
                }
                if(this.SouthForm.paymentStatus.getValue() == "Cleared"){
                    if(!this.SouthForm.clearanceDate.getValue()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PleaseEnterClearanceDate")],2);
                        this.SouthForm.clearanceDate.markInvalid(WtfGlobal.getLocaleText("acc.field.PleaseEnterClearanceDate"));
                        this.enableSaveButtons();
                        return;
                    }else{
                        var details = this.grid.getData();
                        var invalidDate = false;
                        var billNos = "";
                        var templDetails = eval('('+details+')');
                        for(var i=0; i<templDetails.length; i++){
                            if(this.SouthForm.clearanceDate.getValue() < new Date(templDetails[i].date)){
                                invalidDate = true;
                                billNos += templDetails[i].billno +", "
                            }
                        }
                        if(invalidDate){
                            billNos =billNos.substring(0,billNos.length-2);
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ClearanceDatecannotbebeforeanyinvoicesdateInvoiceNumbers") + billNos],2);
                            this.enableSaveButtons();
                            return;
                        }
                    }
                }
                //Check for contra entry records
                if((this.val == '4')){
                    var recArray=this.invGrGrid.getSelectionModel().getSelections();
                    if(recArray==0){
                        var typename = (this.businessPerson=="Customer")?"Vendor":"Customer";
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Select")+typename+WtfGlobal.getLocaleText("acc.field.Invoicefromthetopgrid")],2);
                        this.enableSaveButtons();
                        return;
                    }
                }
                //For checking minimum budget limit of payment account
                if(!this.isReceipt)
                {    
                var advance=0;
                if(this.isadvance.getValue())
                    advance=this.advanceAmount.getValue();
                var payAccName=this.pmtStore.getAt(this.pmtStore.find('methodid',this.pmtMethod.getValue())).data.accountname;
                var remBalance=this.accEndingBalance-(amt+advance);
                var budgetInSelectedCurrency=this.calCustMinBugetInSelectedCurrency();
                if(remBalance<budgetInSelectedCurrency)
                 {
                      if( Wtf.account.companyAccountPref.custMinBudget==1){          //Block case
                           var accMsg=WtfGlobal.getLocaleText("acc.field.MinimumBudgetLimitforthisPaymentAccounthasreached")+"<br><br><center>";
                               accMsg +="<b>"+WtfGlobal.getLocaleText("acc.field.PaymentAccount")+"</b> "+payAccName+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.CurrentBalance")+"</b> "+parseFloat(getRoundedAmountValue(this.accEndingBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.BalanceAfterPayment")+"</b> "+parseFloat(getRoundedAmountValue(remBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.Min.BudgetLimit")+"</b> "+parseFloat(getRoundedAmountValue(budgetInSelectedCurrency)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                               accMsg += "<br><br>"+WtfGlobal.getLocaleText("acc.field.Youcannotproceed")+"</center>";
                              WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),accMsg],3);
                              this.enableSaveButtons();
                              return;
                          }
                       else if( Wtf.account.companyAccountPref.custMinBudget==2){     //Warn Case
                               accMsg=WtfGlobal.getLocaleText("acc.field.MinimumBudgetLimitforthisPaymentAccounthasreached")+"<br><br><center>";
                               accMsg +="<b>"+WtfGlobal.getLocaleText("acc.field.PaymentAccount")+"</b> "+payAccName+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.CurrentBalance")+"</b> "+parseFloat(getRoundedAmountValue(this.accEndingBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.BalanceAfterPayment")+"</b> "+parseFloat(getRoundedAmountValue(remBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.Min.BudgetLimit")+"</b> "+parseFloat(getRoundedAmountValue(budgetInSelectedCurrency)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                               accMsg += "<br><br>"+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+"</center>";
                               Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),accMsg,function(btn){
                                if(btn =="yes") {              
                                    this.saveData(); 
                                }else{
                                    this.enableSaveButtons();
                                    return;
                                }
                            }, this); 
                          }
                          else{      //Ignore Case
                            this.saveData();  
                          }
                   }
                   else{
                      this.saveData();             
                  } 
                }
                  else{
                      this.saveData();             
                  }                 
            }
            else{
                var forAccount=false;
                if(this.isReceipt)  //Receive Payment Case
                {
                  if(this.val==7 || this.val==9 || this.val==2)                   
                    forAccount=true;
                }else  {    //Make Payment Case
                   if(this.val==7 || this.val==9 || (this.isCopyReceipt&& this.val==1) || (this.actualReceiptType==9 &&this.isEdit&& this.val==1))
                       forAccount=true; 
                }
                
//                
                if(southFormValid&&valid&&isValidCustomFields&&isMultidebitOrAdvCheck==0){
                    if( (Wtf.accountgridname==undefined|| Wtf.accountgridname=="")&& forAccount){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msgbox.0")],2);
                        this.enableSaveButtons();
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseenteramount")],2);
                        this.enableSaveButtons();
                    }
                }else{
                    this.enableSaveButtons();
                    WtfComMsgBox(2, 2);
                }
            }
                
        }
    }, 
    calCustMinBugetInSelectedCurrency:function(){
         var custMinBudget=this.pmtStore.getAt(this.pmtStore.find('methodid',this.pmtMethod.getValue())).data.acccustminbudget;
         var currency=this.pmtStore.getAt(this.pmtStore.find('methodid',this.pmtMethod.getValue())).data.acccurrency;
                var i=0;
                while(i<this.currencyStore.getCount()){
                   if(currency==this.currencyStore.getAt(i).data['currencyid']){
                     var tempExchangeRate=this.currencyStore.getAt(i).data['exchangerate']
                     break;
                   } 
                   i++;
                }
                var budgetInBaseCurrency=(custMinBudget)/tempExchangeRate;
                var index=this.getCurrencySymbol();
                if(index>=0){
                     var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
                 }
                var budgetInSelectedCurrency=budgetInBaseCurrency*exchangeRate;
         return budgetInSelectedCurrency;
    },  
    
    saveData:function(){    
             Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.je.msg1"),function(btn){
                    if(btn!="yes") {
                        this.Amount.enable();
                        this.isChequePrint=false;
                        this.enableSaveButtons();
                        return;
                    }
                    WtfComMsgBox(27,4,true);                
                    var rec=this.NorthForm.getForm().getValues();
                    this.rec1 = "";
                    rec.bankChargesCmb=this.bankChargesAccount.getValue();
                    rec.bankInterestCmb=this.bankInterestAccount.getValue();
                    rec.paidToCmb=this.paidTo.getValue();
                    rec.mode=(this.isCustBill?34:31);
                    rec.isChequePrint=this.isChequePrint;
                    rec.maininvoiceid=this.mainInvoiceId;
                    var custFieldArr=this.tagsFieldset.createFieldValuesArray();
                    rec.externalcurrencyrate=this.externalcurrencyrate;
                    if(this.externalcurrencyrate!=undefined&&this.externalcurrencyrate>0) {
                        rec.externalcurrencyrate = this.externalcurrencyrate;
                    }else if(this.isEdit && this.record.data.externalcurrencyrate!=undefined){
                        var externalCurrencyRate = this.record.data.externalcurrencyrate-0;
                        if(externalCurrencyRate>0){
                            rec.externalcurrencyrate = externalCurrencyRate;
                        }
                    }
                    rec.pmtmethod=this.pmtMethod.getValue();
                    rec.isIBGTypeTransaction = this.isIBGTypeTransaction;
                    rec.ibgDetailsID = this.ibgDetailsID;
                    rec.ibgCode = this.ibgTransactionCode.getValue();
                    if (custFieldArr.length > 0)
                        rec.customfield = JSON.stringify(custFieldArr);
                    if(this.SouthForm.paymentStatus.getValue() == "Cleared"){
                        rec.bankaccid = this.pmtStore.getAt(this.pmtStore.find('methodid',this.pmtMethod.getValue())).data.accountid;
                        rec.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                        rec.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));
                    }
                    if((this.val!="2" && this.val!="9") ||this.isMultiDebit) {
                    var arr=[];
                    var store=this.grid.getStore();
                    var len=store.getCount();
                    var storeCnDn=this.cndnGrid.getStore();
                    var lenCnDn=storeCnDn.getCount();
                    this.isLinkedToClaimedInvoice = false;
                    for(var i=0;i<len;i++){
                        if(store.getAt(i).data.payment!=undefined&&store.getAt(i).data.amountdue!=undefined&&store.getAt(i).data.payment==store.getAt(i).data.amountdue){
                            store.getAt(i).data.exchangeratefortransaction=store.getAt(i).data.amountdue/store.getAt(i).data.amountDueOriginal;
                        }
                        
                        
                        if(store.getAt(i).data.isClaimedInvoice){
                            this.isLinkedToClaimedInvoice = true;
                        }

                    }
                        rec.detail=this.grid.getData();
                    } else {
                        rec.detail = "[{payment:'"+ this.Amount.getValue()+"',currencyid :'"+this.Currency.getValue()+"'}]";
                        rec.otherwise = true;
                    }
                    
                    rec.isLinkedToClaimedInvoice = this.isLinkedToClaimedInvoice;
                    rec.isCNDN=false;
                    var accountidforCNDN = this.Name.getValue();
                    var accountidforCNDNRec=WtfGlobal.searchRecord(this.personAccStore, accountidforCNDN, "accid");
//                    var index=this.personAccStore.find("accid",accountidforCNDN);
                    if(accountidforCNDNRec!=-1&&accountidforCNDNRec!=null){
                        accountidforCNDN= accountidforCNDNRec.data.accountid;
                    }
                    if((this.val==7 || this.val==8) && !this.isReceipt) {
                        rec.detailForCNDN =rec.detail;
                            rec.detail = '[{select:undefined,billid:"undefined",journalentryid:"undefined",personid:"undefined",entryno:"undefined",billno:"undefined",transectionno:"undefined",creationdate:"undefined",date:"undefined",duedate:"undefined",currencyid:"undefined",oldcurrencyrate:"undefined",currencyname:"undefined",currencysymbol:"SGD",oldcurrencysymbol:"undefined",vendorid:"undefined",vendorname:"undefined",personname:"undefined",externalcurrencyrate:"undefined",amountdue:"undefined",taxpercent:"0",prpercent:"undefined",discount:"undefined",amount:"undefined",memo:"undefined",payment:"undefined",accountid:"'+accountidforCNDN+'",accountname:"undefined",amountpaid:"undefined",description:"",costcenterid:"undefined",costcenterName:"undefined",dramount:"'+rec.amount+'",deductDiscount:"undefined",totalamount:"undefined",modified:true}]';
                        rec.isCNDN=true;
                        rec.isAgainstCN=true;
                   } else if((this.val==7 || this.val==8)){
                        rec.detailForCNDN =rec.detail;
                            rec.detail = '[{select:undefined,billid:"undefined",journalentryid:"undefined",personid:"undefined",entryno:"undefined",billno:"undefined",transectionno:"undefined",creationdate:"undefined",date:"undefined",duedate:"undefined",currencyid:"undefined",oldcurrencyrate:"undefined",currencyname:"undefined",currencysymbol:"SGD",oldcurrencysymbol:"undefined",vendorid:"undefined",vendorname:"undefined",personname:"undefined",externalcurrencyrate:"undefined",amountdue:"undefined",taxpercent:"0",prpercent:"undefined",discount:"undefined",amount:"undefined",memo:"undefined",payment:"undefined",accountid:"'+accountidforCNDN+'",accountname:"undefined",amountpaid:"undefined",description:"",costcenterid:"undefined",costcenterName:"undefined",dramount:"'+rec.amount+'",deductDiscount:"undefined",totalamount:"undefined",modified:true}]';
                        rec.isCNDN=true;
                        rec.isAgainstDN=true;
                   }                   
                   if(this.val==7){
                       rec.isAgainstDN=false;
                   }else if(this.val==8){
                       rec.isAgainstDN=true;
                   }
                    rec.isReceiptEdit=this.isEdit;
                    if(this.isEdit&&!(this.isCopyReceipt!=undefined?this.isCopyReceipt:false)){
                        var datainvoiceadvcndn= this.record.data.datainvoiceadvcndn;
                        var invoicebillid="";
                        var cndnbillid="";
                        if(datainvoiceadvcndn!=undefined){
                            for(var z=0;z<datainvoiceadvcndn.length;z++){
                                if(datainvoiceadvcndn[z].invoiceadvcndntype==1){
                                    invoicebillid=datainvoiceadvcndn[z].paymentID;
                                }
                                if(datainvoiceadvcndn[z].invoiceadvcndntype==3){
                                    cndnbillid=datainvoiceadvcndn[z].paymentID;
                                }
                            }
                        }
                        
                        if(!(invoicebillid==""&&cndnbillid!="")){
                            rec.billid=this.record.data.billid;
                        }
                    }
                    rec.accid=this.Name.getValue();
                    rec.ismultidebit=this.isMultiDebit;
                    if(this.val=="5"){
                         rec.isadvpayment = true;
                         rec.advanceAmountType = this.advanceAmountType.getValue();
                    }else 
                        rec.isadvpayment = false;
                    
//                    if((this.val=='2' || this.val=='9') && this.ismanydbcr.getValue()) {//val = 1 in edit case
                    if(this.ismanydbcr.getValue()) {//val = 1 in edit case
                        rec.ismanydbcr = true;
                    } else {
                        rec.ismanydbcr = false;
                    }
                    if(this.isadvanceFromVendor.getValue()) {//val = 1 if user check option in Receive payment from vendor form case.
                        rec.isadvanceFromVendor = true;
                    } else {
                        rec.isadvanceFromVendor = false;
                    }
                    rec.receipttype = this.val;
                    if(this.isEdit){
                        if(this.actualReceiptType==0){
                            rec.actualReceiptType = 1;
                        }else{
                            rec.actualReceiptType = this.actualReceiptType;
                        }
                    }else{
                        rec.actualReceiptType = this.val;
                    }
                    if(Wtf.accountgridname==undefined||Wtf.accountgridname==""){
                        this.personid=rec.accid;
                        this.Accountname=this.Name.el.dom.value;
                    }else{
                        this.Accountname=Wtf.accountgridname;
                        this.personid=Wtf.persongridid;
                        Wtf.persongridid="";
                        Wtf.accountgridname="";
                    }
                    rec.isEdit=this.isEdit;
                    rec.invoiceadvcndntype = 1;
                    rec.paydetail = this.SouthForm.GetPaymentFormData();
                    rec.currencyid=this.Currency.getValue();
                    rec.no = this.No.getValue();
                    rec.creationdate=WtfGlobal.convertToGenericDate(this.creationDate.getValue());
                    var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                    rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                    if(rec.sequenceformat==undefined){
                        rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                    }
                    this.ajxUrl = "";
                    //(this.isCustBill?34:31)
                    if(this.businessPerson=="Customer"){
                        if(this.val == '4') {
                            this.ajxUrl = "ACCReceipt/" + (this.isCustBill?"saveContraBillingReceipt":"saveContraReceipt") + ".do";
                        } else {
                            this.ajxUrl = "ACCReceipt/" + (this.isCustBill?"saveBillingReceipt":"saveReceipt") + ".do";
                        }
                    }else if(this.businessPerson=="Vendor"){
                        if(this.val == '4') {
                            this.ajxUrl = "ACCVendorPayment/" + (this.isCustBill?"saveContraBillingPayment":"saveContraPayment") + ".do";
                        } else {
                            this.ajxUrl = "ACCVendorPayment/" + (this.isCustBill?"saveBillingPayment":"savePayment") + ".do";
                        }
                    }
        var data="";
        var mainData="";
        
        
//        alert(this.advanceAmount.getValue());
//        alert(this.grid.getAmount(false));
//        alert(this.cndnGrid.getAmount(false));
          rec.advanceAmountFlag=(this.advanceAmount.getValue()==0);
          rec.invoiceAmountFlag=(this.grid.getAmount(false)==0);
          rec.cndnAmountFlag=(this.cndnGrid.getAmount(false)==0);


        
        if(!this.isadvance.getValue() && this.advanceAmount.getValue()==0&&this.isEdit){
            if(this.val != '4') {
                var jsonArr=[];
                jsonArr.push(WtfGlobal.searchRecordIndex(this.gridObject.getStore(), this.record.data.advanceid, "billid"));
                data= WtfGlobal.getJSONArray(this.gridObject,true,jsonArr);
                jsonArr.pop();
                jsonArr.push(WtfGlobal.searchRecordIndex(this.gridObject.getStore(), this.record.data.billid, "billid"));
                mainData= WtfGlobal.getJSONArray(this.gridObject,true,jsonArr);
                if(mainData!="")
                    rec.mainData = mainData;
                if(data!="")
                    rec.data = data;
            }
        }
        this.rec1 = rec; 
        if(this.record!=undefined&&this.record.data!=undefined&&this.record.data.datainvoiceadvcndn!=undefined){
            var datainvoiceadvcndnstring = JSON.stringify(this.record.data.datainvoiceadvcndn);
            rec.datainvoiceadvcndn=datainvoiceadvcndnstring;
        }
        rec.deleteCNDN=false;
        if(this.isEdit&&this.cndnGrid.getAmount(false)==0){
            rec.deleteCNDN=true;
        }
        rec.isCopyReceipt=this.isCopyReceipt;        
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:this.ajxUrl,
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    },this);                
    },
    validateForSaveAdvance:function(){
            if(!this.isMultiDebit && !this.grid.hidden){
                    this.Amount.disable();
            }
            var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
            var valid=this.NorthForm.getForm().isValid();
            var southFormValid = this.SouthForm.hidden ? true : this.SouthForm.getForm().isValid();
            if(southFormValid&&valid && isValidCustomFields){
                if(this.SouthForm.paymentStatus.getValue() == "Cleared"){
                    if(!this.SouthForm.clearanceDate.getValue()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PleaseEnterClearanceDate")],2);
                        this.SouthForm.clearanceDate.markInvalid(WtfGlobal.getLocaleText("acc.field.PleaseEnterClearanceDate"));
                        this.enableSaveButtons();
                        return;
                    }
                }
                
               if(!this.isReceipt){
                var advance=this.advanceAmount.getValue();
                var payAccName=this.pmtStore.getAt(this.pmtStore.find('methodid',this.pmtMethod.getValue())).data.accountname;
                var remBalance=this.accEndingBalance-advance;
                var budgetInSelectedCurrency=this.calCustMinBugetInSelectedCurrency();
                if(remBalance<budgetInSelectedCurrency && !this.isReceipt)
                 {
                     if( Wtf.account.companyAccountPref.custMinBudget==1){          //Block case
                           var accMsg=WtfGlobal.getLocaleText("acc.field.MinimumBudgetLimitforthisPaymentAccounthasreached")+"<br><br><center>";
                               accMsg +="<b>"+WtfGlobal.getLocaleText("acc.field.PaymentAccount")+"</b> "+payAccName+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.CurrentBalance")+"</b> "+parseFloat(getRoundedAmountValue(this.accEndingBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.BalanceAfterPayment")+"</b> "+parseFloat(getRoundedAmountValue(remBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.Min.BudgetLimit")+"</b> "+parseFloat(getRoundedAmountValue(budgetInSelectedCurrency)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                               accMsg += "<br><br>"+WtfGlobal.getLocaleText("acc.field.Youcannotproceed")+"</center>";
                              WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),accMsg],3);
                              this.enableSaveButtons();
                              return;
                          }
                       else if( Wtf.account.companyAccountPref.custMinBudget==2){     //Warn Case
                               accMsg=WtfGlobal.getLocaleText("acc.field.MinimumBudgetLimitforthisPaymentAccounthasreached")+"<br><br><center>";
                               accMsg +="<b>"+WtfGlobal.getLocaleText("acc.field.PaymentAccount")+"</b> "+payAccName+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.CurrentBalance")+"</b> "+parseFloat(getRoundedAmountValue(this.accEndingBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.BalanceAfterPayment")+"</b> "+parseFloat(getRoundedAmountValue(remBalance)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+", "+"<b>"+WtfGlobal.getLocaleText("acc.field.Min.BudgetLimit")+"</b> "+parseFloat(getRoundedAmountValue(budgetInSelectedCurrency)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                               accMsg += "<br><br>"+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+"</center>";
                               Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),accMsg,function(btn){
                                if(btn =="yes") {
                                    this.saveAdvanceData(); 
                                }else{
                                    this.enableSaveButtons();
                                    return;
                                }
                            }, this); 
                       }else{            //Ignore case
                            this.saveAdvanceData();  
                          }
                  }else{
                        this.saveAdvanceData();  
                  }
               }else{
                      this.saveAdvanceData();             
               }                                
            }else{
                  this.enableSaveButtons();
                  WtfComMsgBox(2, 2);   
            }
                
    },
    saveAdvanceData:function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.je.msg1"),function(btn){
                    if(btn!="yes") {
                        this.Amount.enable();
                        this.isChequePrint=false;
                        this.enableSaveButtons();
                        return;
                    }
                    WtfComMsgBox(27,4,true);                
                    var rec=this.NorthForm.getForm().getValues();
                    rec.mode=(this.isCustBill?34:31);
                    rec.isChequePrint=this.isChequePrint;
                    rec.maininvoiceid=this.mainInvoiceId;
                    rec.bankChargesCmb=this.bankChargesAccount.getValue();
                    rec.bankInterestCmb=this.bankInterestAccount.getValue();
                    rec.paidToCmb=this.paidTo.getValue();
                    var custFieldArr=this.tagsFieldset.createFieldValuesArray();
                    rec.externalcurrencyrate=this.externalcurrencyrate;
                    rec.pmtmethod=this.pmtMethod.getValue();
                    if (custFieldArr.length > 0)
                        rec.customfield = JSON.stringify(custFieldArr);
                    if(this.SouthForm.paymentStatus.getValue() == "Cleared"){
                        rec.bankaccid = this.pmtStore.getAt(this.pmtStore.find('methodid',this.pmtMethod.getValue())).data.accountid;
                        rec.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                        rec.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));
                    }
                    if((this.val!="2"&& this.val!="9") ||this.isMultiDebit) {
                        rec.detail=this.grid.getData();
                    } else {
                        rec.detail = "[{payment:'"+ this.Amount.getValue()+"',currencyid :'"+this.Currency.getValue()+"'}]";
                        rec.otherwise = true;
                    }
                    rec.isReceiptEdit=this.isEdit;
                    if(this.isEdit){
                        rec.billid=this.record.data.billid;
                    }
                    if(rec.sequenceformat==undefined){
                        rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                    }
                    rec.accid=this.Name.getValue();
                    rec.ismultidebit=this.isMultiDebit;
                    if(this.val=="5"){
                        rec.isadvpayment = true;
                        rec.advanceAmountType = this.advanceAmountType.getValue();
                    }else 
                        rec.isadvpayment = false;
                    rec.paydetail = this.SouthForm.GetPaymentFormData();
                    rec.currencyid=this.Currency.getValue();
                    rec.creationdate=WtfGlobal.convertToGenericDate(this.creationDate.getValue());
                    var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                    rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                    this.ajxUrl = "";
                    //(this.isCustBill?34:31)
                    if(this.businessPerson=="Customer"){
                        if(this.val == '4') {
                            this.ajxUrl = "ACCReceipt/" + (this.isCustBill?"saveContraBillingReceipt":"saveContraReceipt") + ".do";
                        } else {
                            this.ajxUrl = "ACCReceipt/" + (this.isCustBill?"saveBillingReceipt":"saveReceipt") + ".do";
                        }
                    }else if(this.businessPerson=="Vendor"){
                        if(this.val == '4') {
                            this.ajxUrl = "ACCVendorPayment/" + (this.isCustBill?"saveContraBillingPayment":"saveContraPayment") + ".do";
                        } else {
                            this.ajxUrl = "ACCVendorPayment/" + (this.isCustBill?"saveBillingPayment":"savePayment") + ".do";
                        }
                    }
                    this.saveAdvance(rec, true);
                },this);
    },
    saveAdvance:function(rec, onlyAdvance){
        if(this.isadvance.getValue() && this.advanceAmount.getValue() < 0) {
//            if(onlyAdvance) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.AmountenteredinAdvanceAmountfieldcannotbelessthanZero")],2);
//            }
            this.enableSaveButtons();
            return;
        }
        var accountid = this.Name.getValue();
        var index=this.personAccStore.find("accid",accountid);
        if(index!=-1){
            accountid= this.personAccStore.getAt(index).data.accountid;
        }
        this.personid=accountid;
        this.Accountname=this.Name.el.dom.value;
        var dramount = this.advanceAmount.getValue();
        rec.onlyAdvance = onlyAdvance;
        rec.isadvpayment = true;
        rec.ismultidebit = true;
        rec.invoiceadvcndntype = 2;
        rec.amount = this.advanceAmount.getValue();
        rec.advanceAmountType = this.advanceAmountType.getValue();
        rec.detail = '[{select:undefined,billid:"undefined",journalentryid:"undefined",personid:"undefined",entryno:"undefined",billno:"undefined",transectionno:"undefined",creationdate:"undefined",date:"undefined",duedate:"undefined",currencyid:"undefined",oldcurrencyrate:"undefined",currencyname:"undefined",currencysymbol:"SGD",oldcurrencysymbol:"undefined",vendorid:"undefined",vendorname:"undefined",personname:"undefined",externalcurrencyrate:"undefined",amountdue:"undefined",taxpercent:"0",prpercent:"undefined",discount:"undefined",amount:"undefined",memo:"undefined",payment:"undefined",accountid:"'+accountid+'",accountname:"undefined",amountpaid:"undefined",description:"",costcenterid:"undefined",costcenterName:"undefined",dramount:"'+dramount+'",deductDiscount:"undefined",totalamount:"undefined",modified:true}]';
//        this.rec1 = "";
        rec.no = this.No.getValue();
        rec.isEdit = this.isEdit;
        if(this.isEdit) {
            rec.billid = this.advanceid!=""?this.advanceid:"";
        }
        
        var data="";
        var mainData="";
              
        if(!this.isadvance.getValue() && this.advanceAmount.getValue()==0&&this.isEdit){
        if(this.val != '4') {
            var jsonArr=[];
            jsonArr.push(WtfGlobal.searchRecordIndex(this.gridObject.getStore(), this.record.data.advanceid, "billid"));
            data= WtfGlobal.getJSONArray(this.gridObject,true,jsonArr);
            jsonArr.pop();
            jsonArr.push(WtfGlobal.searchRecordIndex(this.gridObject.getStore(), this.record.data.billid, "billid"));
            mainData= WtfGlobal.getJSONArray(this.gridObject,true,jsonArr);
            if(mainData!="")
                rec.mainData = mainData;
            if(data!="")
                rec.data = data;
        }
    }
    if(this.record!=undefined&&this.record.data!=undefined&&this.record.data.datainvoiceadvcndn!=undefined){
            var datainvoiceadvcndnstring = JSON.stringify(this.record.data.datainvoiceadvcndn);
            rec.datainvoiceadvcndn=datainvoiceadvcndnstring;
    }
    rec.deleteCNDN=false;
        if(this.isEdit&&this.cndnGrid.getAmount(false)==0){
            rec.deleteCNDN=true;
        }
     if(onlyAdvance&&this.isEdit){
        if(this.val != '4') {
            var jsonArr=[];
            jsonArr.push(WtfGlobal.searchRecordIndex(this.gridObject.getStore(), this.record.data.billid, "billid"));
            data= WtfGlobal.getJSONArray(this.gridObject,true,jsonArr);
            if(data!="")
                rec.data = data;
        }
     }
          rec.advanceAmountFlag=(this.advanceAmount.getValue()==0);
          rec.invoiceAmountFlag=(this.grid.getAmount(false)==0);
          rec.cndnAmountFlag=(this.cndnGrid.getAmount(false)==0);
        WtfGlobal.setAjaxTimeOut();
        if(onlyAdvance) {
            if(this.advanceAmount.getValue()==0){        
                this.saveCNDNWithInvoice(rec, onlyAdvance);
            }else{
                this.rec1 = rec;
                Wtf.Ajax.requestEx({
                    url:this.ajxUrl,
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);
            }
        } else {
                rec.ignoreDuplicateChk = true;        
                Wtf.Ajax.requestEx({
                    url:this.ajxUrl,
                    params:rec
                },this,this.genAdvanceSuccessResponse,this.genFailureResponse);
        }
    },
    saveCNDNWithInvoice:function(rec, onlyAdvance){
        if(this.isadvance.getValue() && this.advanceAmount.getValue() < 0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.AmountenteredinAdvanceAmountfieldcannotbelessthanZero")],2);
                this.enableSaveButtons();
            return;
        }
        this.cndnsaveonly=onlyAdvance; //used in cndn save success responce
        var accountid = this.Name.getValue();
        var index=this.personAccStore.find("accid",accountid);
        if(index!=-1){
            accountid= this.personAccStore.getAt(index).data.accountid;
        }
        this.personid=accountid;
        this.Accountname=this.Name.el.dom.value;
        var dramount = this.cndnGrid.getAmount(false);
        rec.onlyAdvance = onlyAdvance;
        rec.isadvpayment = false;
        rec.ismultidebit = true;
        rec.amount = this.cndnGrid.getAmount(false);
        rec.invoiceadvcndntype = 3;
        this.rec1 = "";
//        rec.no = this.No.getValue();
        rec.isEdit = this.isEdit;
        if(this.isEdit) {
            rec.billid = this.record.data.cndnid;
        }
        var storeCnDn=this.cndnGrid.getStore();
        var lenCnDn=storeCnDn.getCount();
        var accountidforCNDN = this.Name.getValue();
        var accountidforCNDNRec=WtfGlobal.searchRecord(this.personAccStore, accountidforCNDN, "accid");
//                    var index=this.personAccStore.find("accid",accountidforCNDN);
        if(accountidforCNDNRec!=-1&&accountidforCNDNRec!=null){
            accountidforCNDN= accountidforCNDNRec.data.accountid;
        }
        rec.detail=this.cndnGrid.getData();
        if(this.isReceipt) {
            rec.detailForCNDN =rec.detail;
                rec.detail = '[{select:undefined,billid:"undefined",journalentryid:"undefined",personid:"undefined",entryno:"undefined",billno:"undefined",transectionno:"undefined",creationdate:"undefined",date:"undefined",duedate:"undefined",currencyid:"undefined",oldcurrencyrate:"undefined",currencyname:"undefined",currencysymbol:"SGD",oldcurrencysymbol:"undefined",vendorid:"undefined",vendorname:"undefined",personname:"undefined",externalcurrencyrate:"undefined",amountdue:"undefined",taxpercent:"0",prpercent:"undefined",discount:"undefined",amount:"undefined",memo:"undefined",payment:"undefined",accountid:"'+accountidforCNDN+'",accountname:"undefined",amountpaid:"undefined",description:"",costcenterid:"undefined",costcenterName:"undefined",dramount:"'+rec.amount+'",deductDiscount:"undefined",totalamount:"undefined",modified:true}]';
            rec.isCNDN=true;
            rec.isAgainstDN=false;
            rec.isAgainstCN=true;
        } else {
            rec.detailForCNDN =rec.detail;
                rec.detail = '[{select:undefined,billid:"undefined",journalentryid:"undefined",personid:"undefined",entryno:"undefined",billno:"undefined",transectionno:"undefined",creationdate:"undefined",date:"undefined",duedate:"undefined",currencyid:"undefined",oldcurrencyrate:"undefined",currencyname:"undefined",currencysymbol:"SGD",oldcurrencysymbol:"undefined",vendorid:"undefined",vendorname:"undefined",personname:"undefined",externalcurrencyrate:"undefined",amountdue:"undefined",taxpercent:"0",prpercent:"undefined",discount:"undefined",amount:"undefined",memo:"undefined",payment:"undefined",accountid:"'+accountidforCNDN+'",accountname:"undefined",amountpaid:"undefined",description:"",costcenterid:"undefined",costcenterName:"undefined",dramount:"'+rec.amount+'",deductDiscount:"undefined",totalamount:"undefined",modified:true}]';
            rec.isCNDN=true;
            rec.isAgainstDN=false;
            rec.isAgainstCN=true;
        }
        var data="";
        var mainData="";
        
        rec.advanceAmountFlag=(this.advanceAmount.getValue()==0);
        rec.invoiceAmountFlag=(this.grid.getAmount(false)==0);
        rec.cndnAmountFlag=(this.cndnGrid.getAmount(false)==0);

        if(!this.isadvance.getValue() && this.advanceAmount.getValue()==0&&this.isEdit){
        if(this.val != '4') {
            var jsonArr=[];
            jsonArr.push(WtfGlobal.searchRecordIndex(this.gridObject.getStore(), this.record.data.advanceid, "billid"));
            data= WtfGlobal.getJSONArray(this.gridObject,true,jsonArr);
            jsonArr.pop();
            jsonArr.push(WtfGlobal.searchRecordIndex(this.gridObject.getStore(), this.record.data.billid, "billid"));
            mainData= WtfGlobal.getJSONArray(this.gridObject,true,jsonArr);
            if(mainData!="")
                rec.mainData = mainData;
            if(data!="")
                rec.data = data;
        }
    }
     if(this.record!=undefined&&this.record.data!=undefined&&this.record.data.datainvoiceadvcndn!=undefined){
         var datainvoiceadvcndnstring = JSON.stringify(this.record.data.datainvoiceadvcndn);
            rec.datainvoiceadvcndn=datainvoiceadvcndnstring;
        }
        
    rec.deleteCNDN=false;
        if(this.isEdit&&this.cndnGrid.getAmount(false)==0){
            rec.deleteCNDN=true;
        }
    if(onlyAdvance&&this.isEdit){
        if(this.val != '4') {
            var jsonArr=[];
            jsonArr.push(WtfGlobal.searchRecordIndex(this.gridObject.getStore(), this.record.data.billid, "billid"));
            data= WtfGlobal.getJSONArray(this.gridObject,true,jsonArr);
            if(data!="")
                rec.data = data;
        }
    }

    rec.isIBGTypeTransaction = this.isIBGTypeTransaction;
    rec.ibgDetailsID = this.ibgDetailsID;
    rec.ibgCode = this.ibgTransactionCode.getValue();
    if(this.cndnGrid.getAmount(false)>0){
//               rec.ignoreDuplicateChk = true;        
                    WtfGlobal.setAjaxTimeOut();
                    Wtf.Ajax.requestEx({
                        url:this.ajxUrl,
                        params:rec
                        },this,this.genCNDNSuccessResponse,this.genFailureResponse);
        }
    },
    updatePayments:function(){
        var comp = null;
        if(this.businessPerson=="Customer"){
            comp = (this.isCustBill? Wtf.getCmp('receiptBillingReport'):Wtf.getCmp('receiptReport'));
        }else if(this.businessPerson=="Vendor"){
            comp = (this.isCustBill?Wtf.getCmp('paymentBillingReport'):Wtf.getCmp('paymentReport')) ;
        }
        
        if(comp){
            comp.fireEvent('paymentupdate');
        }
    },
    genAdvanceSuccessResponse:function(response, request){
         WtfGlobal.resetAjaxTimeOut();
            if(response.success) {
                if(response.paymentid)this.rec1.advancePaymentIdForCnDn = response.paymentid;
                this.rec1.ignoreDuplicateChk = true;
                this.saveCNDNWithInvoice(this.rec1, false);
                this.rec1 = "";                
                if(this.cndnGrid.getAmount(false)<=0){
                    if(response.success){
                        if(this.isEdit!=undefined&&this.isEdit){
                            Wtf.MessageBox.hide();
                            this.updatePayments();
                     }
                    }
                }
            }
    },
    genCNDNSuccessResponse:function(response, request){
        WtfGlobal.resetAjaxTimeOut();        
        if(!response.success){
            this.enableSaveButtons();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
            return;
        }
        if(response.success){
            if(this.isEdit!=undefined&&this.isEdit){
            Wtf.MessageBox.hide();
                this.updatePayments();
            }else if(this.cndnsaveonly){
                this.enableSaveButtons();
                Wtf.MessageBox.hide();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
                if(this.mailFlag){
                    this.disableComponent();
                    return;
                }
               this.NorthForm.getForm().reset();
               this.cndnGrid.getStore().removeAll();                              
               this.SouthForm.getForm().reset();
            }
        }
    },
//    genAdvanceFailureResponse:function(response, request){
//        
//    },
    genSuccessResponse:function(response, request){
        WtfGlobal.resetAjaxTimeOut();
        this.enableSaveButtons();
        
//        Wtf.MessageBox.hide();
        
        if(!response.success){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
            return;
        }
        
        var formatid = this.sequenceFormatCombobox.getValue();        
        if(this.rec1 != "" && this.isadvance.getValue()&&this.grid.getAmount(false)>0) {//Save advance entry for payment used to invoice
            if(response.success) {
                
                if(response.paymentid){
                    this.rec1.mainpaymentid = response.paymentid;
                    this.rec1.mainPaymentForCNDNId = response.paymentid;
                }
                    this.saveAdvance(this.rec1, false,false);    
            }
        }else if(this.cndnGrid.getAmount(false)>0){
            if(response.success) {
                 if(response.paymentid){
                     this.rec1.advancePaymentIdForCnDn = response.paymentid;
                     this.rec1.mainPaymentForCNDNId = response.paymentid;
                 }
                 this.rec1.ignoreDuplicateChk = true;
                this.saveCNDNWithInvoice(this.rec1, false);    
            }
        }
        this.symbol = WtfGlobal.getCurrencySymbol();
        var defaultPayMenthodId = this.pmtMethod.getValue();
        if(!this.mailFlag){
            var customFieldArray = this.tagsFieldset.customFieldArray;      //Reset Custom Feilds
                for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                   var fieldId = customFieldArray[itemcnt].id
                    if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                        Wtf.getCmp(fieldId).reset();
                    }
              }
             var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
                    for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                        var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                        if (Wtf.getCmp(checkfieldId) != undefined) {
                            Wtf.getCmp(checkfieldId).reset();
                        }
               } 
            var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  //Reset Custom Dimensions
            for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
                var fieldId1 = customDimensionArray[itemcnt1].id
                if (Wtf.getCmp(fieldId1) != undefined) {
                        Wtf.getCmp(fieldId1).reset();
                }
            }     
        }
        var index = this.pmtStore.findBy( function(rec){
                        var methidid=rec.data['methodid'];
                        if(methidid==defaultPayMenthodId)
                            return true;
                        else
                            return false
                    }, this);
        this.currencyStore.load();                              
           var rec=this.NorthForm.getForm().getValues();
        this.exportRecord=rec;
        this.exportRecord['billid']=response.paymentid;
        this.exportRecord['billno']=response.billno;
        this.exportRecord['amount']=response.amount||"";
        this.exportRecord['advanceamount']=(response.advanceamount==""||response.advanceamount==undefined)?((this.exportRecord.advanceamt==undefined)?"":this.exportRecord.advanceamt):response.advanceamount;
        this.exportRecord['personname']=this.Accountname;
        this.exportRecord['personid']=this.personid;
        this.exportRecord['advanceid']="";
        this.exportRecord['isadvancepayment']=this.isadvance.checked;
        this.exportRecord['address']=response.address?response.address:"";
        this.exportRecord['receipttype']=response.receipttype;	
        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
            this.exportPdfBttn.exportRecord=this.exportRecord;
        }
        this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
//        this.sequenceFormatCombobox.setValue(formatid);
//        this.getNextSequenceNumber(this.sequenceFormatCombobox);
        
        Wtf.MessageBox.hide();
        if(response.success){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
        }
        this.fireEvent('update',this);
        if(this.val == '4') {//Contra Entry
            this.invGrGrid.getStore().removeAll();
        }
         if(!this.directPayment&&!this.isEdit){//case 3:against debit note or credit note
                if(response.success){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
                    if(this.mailFlag){
                        this.loadUserStore(response, request);
                        this.disableComponent();
//                        Wtf.getCmp("emailbut" + this.id).enable();
//                        Wtf.getCmp("exportpdf" + this.id).enable();
                        this.response = response;
                        this.request = request;
                        return;
                    }
               this.NorthForm.getForm().reset();
               this.cndnGrid.getStore().removeAll();
               this.pmtMethod.setValue(defaultPayMenthodId);
               
               this.SouthForm.getForm().reset();
               if(index > -1){
                   this.ShowCheckDetails(null,this.pmtStore.getAt(index));
               }
               this.SouthForm.clearanceDate.getEl().up('.x-form-item').setDisplayed(false);
               //this.Currency.setValue(WtfGlobal.getCurrencyID());
               this.tplSummary.overwrite(this.southCalTemp.body,{taxamount:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),amountwithouttax:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),received:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),due:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),receivable:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),receivableInBase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),advance:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),advanceInBase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),receivedinbase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol())});
                   this.grid.getStore().removeAll();
                   this.grid.getView().emptyText=  "<div class='grid-empty-text'>"+ (this.isReceipt ? WtfGlobal.getLocaleText("acc.mp.cust") : WtfGlobal.getLocaleText("acc.mp.vend")) +"</div>";
                   this.grid.getView().refresh();
                   this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
                   this.ApplyCurrencySymbol();
               if(this.isMultiDebit)
                    this.grid.addNewRow();
                
                this.updatePayments();
            }
            this.setOSDetailNumber();       
        } else {
            if(response.success){//edit & save condtion
                this.updatePayments();
                Wtf.getCmp('as').remove(this);
            }
        }
    if(!this.isCopyReceipt &&!this.isEdit){
        this.sequenceFormatCombobox.setValue(formatid);
        this.getNextSequenceNumber(this.sequenceFormatCombobox);    
    }
        if (response.data) {
            var resdata = response.data[0];
            if (this.isChequePrint) {
                this.printCheque(resdata.accountName, resdata.amount, resdata.amountinword,resdata.amountinword1, resdata.date, resdata.dateLeft, resdata.nameLeft, resdata.amtinwordLeft,resdata.amtinwordLeftLine2,resdata.amtLeft,resdata.dateTop, resdata.nameTop, resdata.amtinwordTop,resdata.amtinwordTopLine2,resdata.amtTop);
                this.isChequePrint = false;
            }
        }
        
    },
    genFailureResponse:function(response){
      WtfGlobal.resetAjaxTimeOut();
      Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    callEmailWindowFunction : function(response, request){
        
    if(this.userds != null){
        var rec = this.userds.getAt(0);
        if(rec.data.withoutinventory){
            if(this.isReceipt){
                callEmailWin("emailwin",rec,'Payment Receipt',12,false,false);
            }else{
                callEmailWin("emailwin",rec,'Payment Voucher',16,false,false);
            }
        }else{
            if(this.isReceipt){
                callEmailWin("emailwin",rec,'Payment Receipt',4,false,false);
            }else{
                callEmailWin("emailwin",rec,'Payment Voucher',8,false,false);
            }
        }
    }
    },
    
    disableComponent: function(){  // disable following component in case of save button press.
    if(Wtf.getCmp("savencreate" + this.heplmodeid + this.id)){
        Wtf.getCmp("savencreate" + this.heplmodeid + this.id).disable();
    }
    if(this.saveBttn){
        this.saveBttn.disable();
    }
    if(this.savePrintBttn){
        this.savePrintBttn.hide();
    }
    if(this.isMultiDebit){
        var GridStore = this.grid.getStore();
        var count2 = GridStore.getCount();
        var lastRec2 = GridStore.getAt(count2-1);
        if(lastRec2!=""||lastRec2!=undefined)
            {
                var lastReccheck=lastRec2.data['accountid'];
                if(lastReccheck==""||lastReccheck==undefined){
                GridStore.remove(lastRec2);
            }
            }
    }
    if(this.grid){
        this.grid.disable();
    }
    if(this.cndnGrid){
        this.cndnGrid.disable();
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
},

enableSaveButtons:function(){
  this.savencreateBttn.enable();
  this.saveBttn.enable();
  this.savePrintBttn.enable();
},
disableSaveButtons:function(){
  this.savencreateBttn.disable();
  this.saveBttn.disable();
  this.savePrintBttn.disable();
},

loadUserStore : function(response, request){
    this.usersRec = new Wtf.data.Record.create([
            {name: 'billid'},
            {name:'companyid'},
            {name:'companyname'},
            {name: 'refid'},
            {name: 'personid'},
            {name: 'billno'},
            {name: 'refno'},
            {name: 'refname'},
            {name: 'refdetail'},
            {name:'personemail'},
            {name: 'detailtype'},
            {name: 'expirydate',type:'date'},
            {name: 'journalentryid'},
            {name: 'entryno'},
            {name: 'currencysymbol'},
            {name: 'externalcurrencyrate'},
            {name: 'personname'},
            {name: 'address'},
            {name: 'deleted'},
            {name: 'billdate',type:'date'},
            {name: 'paymentmethod'},
            {name: 'memo'},
            {name: 'amount'},
            {name: 'methodid'},
            {name: 'receiptamount'},
            {name: 'currencyid'},
            {name: 'detailsjarr'},
            {name: 'clearanceDate',type:'date'},
            {name: 'paymentStatus'},
            {name: 'otherwise'},
            {name: 'isadvancepayment'},
            {name: 'isadvancefromvendor'},
            {name: 'advanceUsed'},
            {name: 'advanceid'},
            {name: 'advanceamount'},
            {name: 'withoutinventory'},
            {name: 'refcardno'},
            {name: 'refexpdate'}
    ]);
    var userdsUrl="";
    if(this.isReceipt){
        userdsUrl = "ACCReceipt/getReceipts.do";
    }else{
        userdsUrl = "ACCVendorPayment/getPayments.do";
    }
    this.userds = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.usersRec),
            url : userdsUrl,
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
            baseParams:{
                mode:(this.isCustBill?35:32),
                consolidateFlag:false,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                deleted:false,
                enddate:'',
                nondeleted:false,
                ss:request.params.no,
                chkAdvanceFlag:true
            }
        });
        
        this.userds.on('load', this.enableButtons(), this);
        
        this.userds.load(); 
},

enableButtons : function(){
    Wtf.getCmp("emailbut" + this.id).enable();
    Wtf.getCmp("exportpdf" + this.id).enable();
},

exportPdfFunction : function(){
    if(this.userds != null){
        var rec = this.userds.getAt(0);
        var amount = rec.data.amount;
        var billid = rec.data.billid;
        var billno = rec.data.billno;
        var personname = rec.data.personname;
        var personid = rec.data.personid;
        var address = rec.data.address;
        var paymentmethod = rec.data.paymentmethod;
        var advanceFlag=false;
        if(rec.data.advanceid!=undefined)
            {
                if(rec.data.advanceid!="")
                    advanceFlag=true;
            }
        var advanceAmount=0.0;
        if(rec.data.advanceamount!=undefined)
            {
                advanceAmount=rec.data.advanceamount;
            }
        var mode = "";
        var fileName = "";
        if(rec.data.withoutinventory){
            if(this.isReceipt){
                fileName="Payment Recieved "+billno;
                mode = 12;
            }else{
                fileName="Payment Made "+billno;
                mode = 16;
            }
        }else{
            if(this.isReceipt){
                fileName="Payment Recieved "+billno;
                mode = 4;
            }else{
                fileName="Payment Made "+billno;
                mode = 8;
            }
        }

        var selRec = "&amount="+amount+"&bills="+billid+"&customer="+paymentmethod+"&accname="+personname+"&personid="+personid+"&address="+address+"&advanceFlag="+advanceFlag+"&advanceAmount="+advanceAmount;
        
        
        if((mode==8||mode==16)&&!rec.data.isadvancepayment && Wtf.templateflag == 1){
             Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportPettyCashVoucher.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + personid + "&filename=" + fileName + "&filetype=pdf";
        }else if((mode==4||mode==12)&&!rec.data.isadvancepayment && Wtf.templateflag == 1){
            Wtf.get('downloadframe').dom.src = "ACCReceiptCMN/exportPettyCashVoucher.do?moduleid=16&mode=" + mode + "&rec=" + selRec + "&personid=" + personid + "&filename=" + fileName + "&filetype=pdf";
        }else{
            Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+personid+"&filename="+fileName+"&filetype=pdf";
        }       
    }
},
getNextSequenceNumber:function(a,val){
       if(!(a.getValue()=="NA")){
            WtfGlobal.hideFormElement(this.No);
            this.setOSDetailNumber(true);        
            var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
             var oldflag=rec!=null?rec.get('oldflag'):true;
             Wtf.Ajax.requestEx({
                url:"ACCCompanyPref/getNextAutoNumber.do",
                params:{
                    from:this.fromnumber,
                    sequenceformat:a.getValue(),
                    oldflag:oldflag
                }
            }, this,function(resp){
                if(resp.data=="NA"){
                    WtfGlobal.showFormElement(this.No);
                    this.No.reset();
                    this.No.enable();
                }else {
                    this.No.setValue(resp.data);
                    this.No.disable();
                    WtfGlobal.hideFormElement(this.No);
                }

            });
        } else {
            WtfGlobal.showFormElement(this.No);
            this.No.reset();
            this.No.enable();
        }
    },
    
    setOSDetailNumber:function(isSelectNoFromCombo){
        if(this.isEdit&&!this.isCopyReceipt)
            this.No.setValue(this.record.data.billno);
        else{
            var temp=0;
            if(this.isCustBill)
                temp=this.isCustBill*10
            temp+=this.isReceipt*1;
            var format="";var temp2="";
            switch(temp){
                case 0:format=Wtf.account.companyAccountPref.autopayment;
                    temp2=Wtf.autoNum.Payment;
                    break;
                case 1:format=Wtf.account.companyAccountPref.autoreceipt;
                    temp2=Wtf.autoNum.Receipt;
                    break;
                case 10:format=Wtf.account.companyAccountPref.autobillingpayment;
                    temp2=Wtf.autoNum.BillingPayment;
                    break;
                case 11:format=Wtf.account.companyAccountPref.autobillingreceipt;
                    temp2=Wtf.autoNum.BillingReceipt;
                    break;
            }
            if(isSelectNoFromCombo){
                this.fromnumber = temp2;
            } else if(format&&format.length>0){
                WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit)this.No.setValue(resp.data)}, this);
            }
        }
    },
    printCheque : function(custName,amount,amountinwords,amountinword1,date,dateLeft,nameLeft,amtinwordLeft,amtinwordLeftLine2,amtLeft,dateTop,nameTop,amtinwordTop,amtinwordTopLine2,amtTop){           
     var newwin=window.open('','printwin','left=0,top=0,width=800,height=300,scrollbars = no' )
        newwin.document.write('<HTML>\n<HEAD>\n')
        newwin.document.write('<TITLE>Print Page</TITLE>\n')
        newwin.document.write('<link rel="stylesheet" type="text/css" href="style/print.css"/>\n')
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
        newwin.document.write('<\/script>\n')
        newwin.document.write('</HEAD>\n')
        newwin.document.write('<BODY onload="print_win()">\n')
        newwin.document.write('<div style="position: fixed; left:'+ dateLeft +'cm; top:'+dateTop+'cm;">'+date+'</div>')
        newwin.document.write('<div style="position: fixed; left:'+ nameLeft +'cm; top:'+nameTop+'cm;">'+custName+'</div>')
        newwin.document.write('<div style="position: fixed; left:'+ amtinwordLeft +'cm; top:'+amtinwordTop+'cm;">'+amountinwords+'</div>')
        newwin.document.write('<div style="position: fixed; left:'+ amtinwordLeftLine2 +'cm; top:'+amtinwordTopLine2+'cm;">'+amountinword1+'</div>')
        newwin.document.write('<div style="position: fixed; left:'+ amtLeft +'cm; top:'+amtTop+'cm;">'+amount+'</div>')
        newwin.document.write('</BODY>\n')
        newwin.document.write('</HTML>\n')
        newwin.document.close()
    },
    
    callIBGDetailsGrid:function(rec,isFromMasterConfiguration,isFromMP) {
        var ibgDetailsGrid = new Wtf.account.VendorIBGDetailsGrid({
            title:'Receiving Bank Details',
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            height:500,
            width:700,
            accRec:rec,
            isFromMasterConfiguration:isFromMasterConfiguration,
            isFromMP:isFromMP,
            closable:false,
            modal:true,
            layout:'border'
        });
        
        ibgDetailsGrid.on('update',function(config){
            this.ibgDetailsID = config.sm.getSelected().data.ibgId;
        },this);

        ibgDetailsGrid.show();
    }
});