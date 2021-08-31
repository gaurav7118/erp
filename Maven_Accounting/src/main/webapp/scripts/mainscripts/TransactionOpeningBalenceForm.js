Wtf.account.TransactionOpeningBalanceForm = function(config){
    this.isEdit = (config.isEdit != null && config.isEdit != undefined &&config.isEdit != '')?config.isEdit:false; 
    this.record = (config.record != null && config.record != undefined)?config.record:''; 
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
    this.isDeposit = false;
    /*
     *If Check is warning from company preference for Cheque No
     */
    this.isWarnConfirm = false;
    if(this.isEdit && this.record.data.transactiontype!=undefined && this.record.data.transactiontype!="" && this.record.data.transactiontype=='Deposit'){
        this.isDeposit=true;
    }
    this.paymentMethodType = {
        Cash: 0,
        Card: 1,
        Bank: 2
    };
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    minWidth: 50,
                    scope: this,
                    handler: this.saveTransactionForm.createDelegate(this)
            }),this.closeButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeTransactionForm.createDelegate(this)
            })]
    },config);
    Wtf.account.TransactionOpeningBalanceForm.superclass.constructor.call(this, config);
    
    this.addEvents({
        'datasaved':true//event will be fire when transaction will be saves successfully.
    });
}

Wtf.extend(Wtf.account.TransactionOpeningBalanceForm, Wtf.Window,{
    onRender:function(config){
        Wtf.account.TransactionOpeningBalanceForm.superclass.onRender.call(this,config);
        var image="../../images/accounting_image/calendar.jpg";
        if(this.isExciseInvoice){
            this.createTransactionFormForExciseInvoice();
            this.saveButton.hide();
        }else if(this.isOrder){
            this.createTransactionFormForPO();
            this.saveButton.hide();
        } else{
            this.createCurrencyStoreComboData();
            this.createTermStoreComboData();
            this.createSalesPersonStoreComboData();
            this.createFields();
            this.createTransactionForm();
        }
        
        
        // adding form
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(this.title,WtfGlobal.getLocaleText("acc.field.PleaseFillTransactionInformation"),image)
        }, this.centerPanel=new Wtf.Panel({
                border: false,
                region: 'center',
                id: 'centerpan'+this.id,
                autoScroll:true,
                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
                layout: 'fit',
                items:[this.transactionInfoForm]
            })
        );
                   
    },
    
    loadRecord:function(){
        this.Number.setValue(this.record.get('transactionNo'));
        this.amount.setValue(this.record.get('transactionAmount'));
        this.amountInBase.setValue(this.record.get('transactionAmountInBase'));
        if(this.record.get('transactionAmount') && this.record.get('transactionAmount') != 0 && this.record.get('excludingGstAmount') && this.record.get('excludingGstAmount') == 0){
            this.exlGstamount.setValue(this.record.get('transactionAmount'));
        } else {
            this.exlGstamount.setValue(this.record.get('excludingGstAmount'));
        }    
        this.taxAmount.setValue(this.record.get('taxAmount'));
        if(this.isInvoice){
            this.PONumber.setValue(this.record.get('porefno'));
            this.DueDate.setValue(this.record.get('dueDate'));
            this.poDate.setValue(this.record.get('poRefDate'));
        }
        if(this.isCreditNote || this.isDebitNote){
            this.narration.setValue(this.record.get('narration'));
        }
        if(this.Memo){
            this.Memo.setValue(this.record.get('memo')? this.record.get('memo'):"");
        }
        
        this.exchangeRateForOtherCurrency.setValue(this.record.get('exchangeRateForOtherCurrency').toFixed(Wtf.EXCHANGERATE_DIGIT_AFTER_DECIMAL_FOR_TRANSACTION));        
        this.CurrencyToBaseExchangeRate.setValue(this.record.get('isCurrencyToBaseExchangeRate'));
        this.billDate.setValue(this.record.get('transactionDate'));
        
        
    },
   
    createCurrencyStoreComboData:function(){
        
        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'foreigntobaseexchangerate'},
            {name: 'htmlcode'}
         ]);
         this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStore.load();
        
        this.currencyStoreCMB = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStoreCMB.on('load',function(){
             if(this.isEdit){                
                 this.loadRecord();
                 this.Currency.setValue(this.record.get('currencyid'));
                 if((this.record.get('currencyid') != WtfGlobal.getCurrencyID()) && this.exchangeRateForOtherCurrency){
                     this.exchangeRateForOtherCurrency.enable();
                     this.CurrencyToBaseExchangeRate.enable();
                 }
             }else{
                 this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
             }
         },this)

         this.currencyStoreCMB.load();
         
         
        
        this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  //'Currency',
            hiddenName:'currencyid',
            id:"currency"+this.id,
//            anchor: '94%',
            width : 220,
            store:this.currencyStoreCMB,
            emptyText:WtfGlobal.getLocaleText("acc.field.Selectacurrency"),
            valueField:'currencyid',
            allowBlank : false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
        });
        
        
        this.Currency.on('select', function(){
            if(WtfGlobal.getCurrencyID() == this.Currency.getValue()){
                this.exchangeRateForOtherCurrency.setValue(1);
                this.exchangeRateForOtherCurrency.disable();
                this.CurrencyToBaseExchangeRate.disable();
            }else{
//                var currencyRecord = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
                this.updateExchangeRate(this.CurrencyToBaseExchangeRate.getValue());
                this.exchangeRateForOtherCurrency.enable();
                this.CurrencyToBaseExchangeRate.enable();
            }
            this.amount.setValue(0);
            this.updateSubtotal();
//            var customer="",currency="";
//            if(this.Name.getValue() != undefined && this.Name.getValue() != ""){
//                customer= this.Name.getValue();
//            }    
//            if(this.Currency.getValue() != undefined && this.Currency.getValue() != ""){
//                currency= this.Currency.getValue();        
//            }                 
//            if(!this.GENERATE_PO&&!this.GENERATE_SO){
//                this.onCurrencyChangeOnly();
//                this.Name.setValue(customer);
//                this.Currency.setValue(currency);
//            }    
            this.currencychanged = true;
//            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),tocurrencyid:this.Currency.getValue()}});
        }, this);
        
    },
    
    createTermStoreComboData:function(){
        
        this.termRec = new Wtf.data.Record.create([
            {name: 'termname'},
            {name: 'termdays'},
            {name: 'termid'}
        ]);
        this.termds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.termRec),
            url : "ACCTerm/getTerm.do",
            baseParams:{
                mode:91
            }
         });
          if(!this.custVenOptimizedFlag && this.isInvoice){
            if(this.isCustomer){
                Wtf.customerAccStore.reload();
            }else{
                Wtf.vendorAccStore.reload();
            }
        }
         this.termds.on('load',function(){
             if(this.isEdit){
                 this.Term.setValue(this.record.get('termid'));
             }else{
                 var perstore = null;
                if(this.custVenOptimizedFlag) {
                    perstore = this.isCustomer? Wtf.customerAccRemoteStore : Wtf.vendorAccRemoteStore;
                } else {
                    perstore = this.isCustomer? Wtf.customerAccStore : Wtf.vendorAccStore;
                }
                 
                var index = perstore.find('accid',this.accountId);
                if (index != -1) {
                    var storerec=perstore.getAt(index);
                    this.Term.setValue(storerec.data['termid']);
                    this.updateDueDate();
             }
             }
         },this);
        if(this.isInvoice){
            this.termds.load({
                params: {               
                    cash_Invoice:false
                }
            });
        }
        this.Term= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Term"),
            id:"creditTerm"+this.id,
            hidden:!this.isInvoice,
            hideLabel:!this.isInvoice,
//            anchor: '93.5%',
            width : 220,
            store:this.termds,
            valueField:'termid',// Changed the value field "Term days" to "Term ID
            allowBlank:!this.isInvoice, 
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectTerm"),
            forceSelection: true,
            displayField:'termname',
//            addNewFn:this.addCreditTerm.createDelegate(this),
            scope:this,
            listeners:{
                'select':{
                    fn:this.updateDueDate,
                    scope:this
                }
            },
            selectOnFocus:true
        });
        
        this.exlGstamount=new Wtf.form.NumberField({
            allowNegative:false,
            hideLabel:!this.isInvoice,
//            allowBlank:false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            maxValue: 999999999999999,   //SDP-10157
            width:220,
            hidden:!this.isInvoice,
            fieldLabel:WtfGlobal.getLocaleText("acc.1099.gridAmt"),
            name:'transactionGstAmount',
            id:"transactionGstAmount"+this.id,
            value:0,
            listeners:{
                'change':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
            
        });
        this.exlGstamount.on('blur',function(obj){
            if(obj.getValue() == '' || obj.getValue() == undefined){
                obj.setValue(0);
            }
            var val = obj.getValue();
            if(val !=undefined && val.toString().length > 15 && !(val.toString().length == 16 && val.toString().indexOf('.') > -1)){
                this.exlGstamount.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
                this.amount.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
                this.amountInBase.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
            }
        },this);
        this.taxAmount=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:!this.isInvoice,
            hideLabel:!this.isInvoice,
//            allowBlank:false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            maxValue: 999999999999999,    //SDP-10157
            width:220,
            fieldLabel:WtfGlobal.getLocaleText("acc.taxReport.taxAmount"),
            name:'taxAmount',
            id:"taxAmount"+this.id,
            value:0,
            listeners:{
                'change':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
            
        });
        this.taxAmount.on('blur',function(obj){
            if(obj.getValue() == '' || obj.getValue() == undefined){
                obj.setValue(0);
            }
            var val = obj.getValue();
            if(val !=undefined && val.toString().length > 15 && !(val.toString().length == 16 && val.toString().indexOf('.') > -1)){
                this.taxAmount.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
                this.amount.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
                this.amountInBase.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
            }
        },this);
    },
    
    createSalesPersonStoreComboData:function(){
    
     this.salesPersonRec=new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'name'
        },{
            name: 'userid'
        }]
        );

        this.salesPersonStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },Wtf.salesPersonRec),
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:15
            }
        });
        
        this.agentStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, Wtf.agentRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 20
            }
        });
        
        this.users=new Wtf.form.FnComboBox({            
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            store:this.isCustomer?this.salesPersonStore:this.agentStore,
            addNoneRecord: true,
//            anchor: '94%',
            width : 220,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.masterConfig.15"):WtfGlobal.getLocaleText("acc.field.AgentSalesman"),
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectSalesPerson"),
            hideLabel:false,
            hidden:false,                        
            name:'salesPerson',
            hiddenName:'salesPerson'            
        });
        
        this.users.addNewFn=this.addSalesPerson.createDelegate(this);
        
        this.users.store.on('load',function(){
            if(this.isEdit && this.record!=null && this.record.get("salesPerson") != "" ){
                this.users.setValue(this.record.get("salesPerson"));
            }
        },this);
        this.users.store.load();
    },
    
    addSalesPerson:function(){
        this.isCustomer ? addMasterItemWindow('15') : addMasterItemWindow('20');
        },
    
    createFields:function(){
     if(this.isCustomer){
        if(this.isInvoice){
            this.moduleId=Wtf.Acc_Invoice_ModuleId;
        }else if(this.isPayment){
            this.moduleId=Wtf.Acc_Receive_Payment_ModuleId;
        }else if(this.isCreditNote){
            this.moduleId=Wtf.Acc_Credit_Note_ModuleId;
        }else if(this.isDebitNote){
            this.moduleId=Wtf.Acc_Debit_Note_ModuleId;
        }
    }else{
        if(this.isInvoice){
            this.moduleId=Wtf.Acc_Vendor_Invoice_ModuleId;
        }else if(this.isPayment){
            this.moduleId=Wtf.Acc_Make_Payment_ModuleId;
        }else if(this.isCreditNote){
            this.moduleId=Wtf.Acc_Credit_Note_ModuleId;
        }else if(this.isDebitNote){
            this.moduleId=Wtf.Acc_Debit_Note_ModuleId;
        }
    }
        
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"formPanel"+this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: this.moduleId,
            isEdit: this.isEdit,
            isOpeningTransaction:true,
            record: this.record
    });
        this.Number=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.openingbalances.TransNo"),
            name: 'number',
            disabled:this.isEdit,
            disabledClass:"newtripcmbss",
            id:"invoiceNo"+this.id,
            //            anchor:'50%',
            width : 220,
            maxLength:50,
            scope:this,
            allowBlank:false,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenternumber")
        });
        
        
        this.billDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.TransactionDate"),
            id:"invoiceDate"+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
            value:WtfGlobal.getOpeningDocumentDate(true), // Set transaction date as before first financial year date.
            maxValue:WtfGlobal.getOpeningDocumentDate(true),//Set transaction date max value as before first financial year date.
//            anchor:'50%',
            width : 220,
            listeners:{
                'change':{
                    fn:this.updateDueDate,
                    scope:this
                }
            },
            allowBlank:false 
        });
        
         this.Memo=new Wtf.form.TextArea({
            fieldLabel: Wtf.account.companyAccountPref.descriptionType, //'Memo/Note',
            name: 'memo',
            id: "memo"+this.id,
            height: 40,
            width: 220,
            readOnly: this.isViewTemplate,
            maxLength: 2048,
            qtip: (this.record && this.record.data && this.record.data.memo) ?this.record.data.memo:  ' ' ,
            listeners: {
                render: function (c) {
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }
        });
        
        this.DueDate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.dueDate"),//'Due Date*',
            name: 'dueDate',
//            itemCls : (this.cash||this.isOrder)?"hidden-from-item":"",
            hidden:!this.isInvoice,
            hideLabel:!this.isInvoice,
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank:!this.isInvoice,
            width : 220
//            anchor:'94%'
        });
        
        
        this.amount=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            value:0,
            hideLabel:false,
            allowBlank:false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            maxValue: 999999999999999,    //SDP-10157
            width:220,
            readOnly:this.isInvoice,
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),
            name:'transactionAmount',
            id:"transactionAmount"+this.id,
            listeners:{
                'change':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
        });

        this.amountInBase=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            value:0,
            hideLabel:false,
            allowBlank:false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            maxLength: 20,
            readOnly:true,
            width:220,
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome"),
            name:'transactionAmountInBase',
            id:"transactionAmountInBase"+this.id
        });
        
        
        this.CurrencyToBaseExchangeRate = new Wtf.form.Checkbox({
            name:'CurrencyToBaseExchangeRate',
            fieldLabel:WtfGlobal.getLocaleText("acc.currencytobaseexchangerate")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.currencytobaseexchangerate.help")),  //'acc.currencytobaseexchangerate',
            checked:true,
            disabled:true,
            cls : 'custcheckbox',
            width: 10,
            listeners:{
                'change':{
                    fn:this.onCurrencyToBaseExchangeRateChange,
                    scope:this
                }
            }
        });
        
//        this.CurrencyToBaseExchangeRate.on('change',this.onCurrencyToBaseExchangeRateChange,this);
        
        this.exchangeRateForOtherCurrency=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            value:1,
            hideLabel:false,
            disabled:true,
            allowBlank:false,
//            maxValue: 999999999,
            decimalPrecision:Wtf.EXCHANGERATE_DIGIT_AFTER_DECIMAL_FOR_TRANSACTION,
            width:220,
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.curEx"),
            name:'exchangeRateForOtherCurrency',        
            id:"exchangerate"+this.id,
            listeners:{
                'change':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
        });
        
        
        this.PONumber=new Wtf.form.TextField({
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.MailWin.pomsg7"):WtfGlobal.getLocaleText("acc.field.PartyInvoiceNumber"),
            name: 'porefno',
            disabled:false,
            id:"ponumber"+this.id,
            //            anchor:'50%',
            width : 220,
            maxLength:50,
            scope:this,
            hidden:!this.isInvoice,
            hideLabel:!this.isInvoice,
//            allowBlank:false,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenternumber")
        });
        
        this.poDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.stockLedger.Date"),
            id:"poDate"+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'poRefDate',
//            value:this.getDates(true),
//            maxValue:this.getDates(true),
            value:WtfGlobal.getOpeningDocumentDate(true), // Set transaction date as before first financial year date.
            maxValue:WtfGlobal.getOpeningDocumentDate(true),//Set transaction date max value as before first financial year date.
            hidden:!this.isInvoice,
            hideLabel:!this.isInvoice,
//            anchor:'50%',
            width : 220
//            listeners:{
//                'change':{
//                    fn:this.updateDueDate,
//                    scope:this
//                }
//            },
//            allowBlank:false 
        });

        this.narration = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Narration"),
            name:'narration',            
            hidden: !(this.isCreditNote || this.isDebitNote),
            hideLabel:!(this.isCreditNote || this.isDebitNote),
            //itemCls : this.isOrder?"hidden-from-item":"",
            //allowBlank:(this.cash?true:this.isOrder),
            height:40,            
//            labelStyle:'margin-top: 10px;padding-left:0px;',            
//            allowBlank:true,
            maxLength: 200,
            width : 220
//            emptyText :'Enter a narration'
//            anchor:"50%"
        });
        
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
                {name: 'autopopulate'}
        ]);

        this.pmtStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.pmtRec),
                url : "ACCPaymentMethods/getPaymentMethods.do",
                baseParams:{
                    mode:51,
                    accountid:(this.isAccount && this.accountId!=null && this.accountId != undefined && this.accountId!='')?this.accountId:''
                }
                
        });  
        
         if(this.isAccount && this.accountId!=null && this.accountId != undefined && this.accountId!=''){
            this.pmtStore.filterBy(function(rec){
                if(rec.data.accountid==this.accountId)
                    return true
                else
                    return false
            },this);  
        }
        
        this.pmtMethod= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.mp.payMethod"),
            name:"pmtmethod",
            hiddenName:'pmtmethod',
            store:this.pmtStore,
            id:'pmtmethod'+this.id,
            valueField:'methodid',
            displayField:'methodname',
            allowBlank:false,
            hidden:!(this.isPayment||this.isAccount),
            hideLabel:!(this.isPayment||this.isAccount),
            disabled:!(this.isPayment||this.isAccount),
            emptyText:WtfGlobal.getLocaleText("acc.mp.selpayacc"),
            width:220,
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true//,
        });
        
         
        this.pmtStore.load();
        this.pmtStore.on('load',this.setPMData,this);
        this.pmtMethod.on('change', this.onPaymentMethodChange, this);
        this.pmtMethod.on('select', this.onPaymentMethodSelect, this); // Event is added for setting the currency of the payment method account again , after changing the currency from currency combobox
        this.pmtMethod.on('beforeselect', this.onPaymentMethodBeforeSelect, this);
    
        this.pmtMethodAcc=new Wtf.form.TextField({
            name:"pmtmethodacc",
            disabled : true,
            hidden:!(this.isPayment||this.isAccount),
            hideLabel:!(this.isPayment||this.isAccount),
            id:"pmtmethodacc"+this.id,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.PaymentAccount"),
            width : 220
        });       
        
        this.paymentTypeStore = new Wtf.data.SimpleStore({
            fields: ['paymentTypeValue', 'paymentTypeName'],
            data: [['Withdraw', 'Withdraw'],['Deposit', 'Deposit']]
        });

        this.paymentType = new Wtf.form.ComboBox({
            store: this.paymentTypeStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalanceType") + "*", //"Opening Balance Type*",
            name: 'paymentType',
            displayField: 'paymentTypeName',
            value: this.isEdit?((this.record!=null && this.record.data.transactiontype!=undefined && this.record.data.transactiontype!="")?this.record.data.transactiontype:'Withdraw'):'Withdraw',
            editable: false,
            valueField: 'paymentTypeValue',
            mode: 'local',
            triggerAction: 'all',
            hidden:!this.isAccount,
            hideLabel:!this.isAccount,
            width:220,
            forceSelection: true,
            disabled:this.isEdit
        });

        this.paymentType.on('change', this.onPaymentTypeChange, this);
    },
    
    setPMData: function() {
        if(this.isEdit){
            if(this.record!=null && this.record.data.methodid!=undefined && this.record.data.methodid!=""){//when we copy older record in which payment not available 
                this.pmtMethod.setValue(this.record.data.methodid);                                          //methodid will be undefined so 
                var rec = WtfGlobal.searchRecord(this.pmtStore, this.record.data.methodid, "methodid");
                this.ShowCheckDetails(this.pmtMethod, rec);
                //                    this.ShowCheckDetails(null,this.record);  
                if(this.record.data.transactiontype!=undefined && this.record.data.transactiontype!="" && this.record.data.transactiontype=='Deposit'){
                    this.isDeposit=true;
                }    
                var type=this.record.data.detailtype;  
                if (type == 2) {
                    this.SouthForm.checkNo.setValue(this.record.data.refno);
                    this.SouthForm.description.setValue(unescape(this.record.data.refdetail));
                    if((this.isCustomer && this.isPayment) || (this.isAccount && this.isDeposit)){
                        this.SouthForm.bankTypeStore.on('load', function() {
                            this.SouthForm.bank.setValue(this.record.data.refname);
                        }, this)
                    }else{
                        this.SouthForm.bank.setValue(unescape(this.record.data.refname));
                    }
                    if (this.record.data.paymentStatus) {
                        this.SouthForm.paymentStatus.setValue("Cleared");
                        this.SouthForm.clearanceDate.getEl().up('.x-form-item').setDisplayed(true);
                        this.SouthForm.clearanceDate.setValue(this.record.data.clearanceDate);
                        this.SouthForm.disable();
                    }
                    this.SouthForm.PostDate.setValue(this.record.data.dueDate);
                } else if (type == 1) {
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
            }
        }else{
            var rowIndex = this.pmtStore.find("isdefault", "true");
            if (rowIndex != -1) {
                var rec = this.pmtStore.getAt(rowIndex);                
                var methodid = rec.get('methodid');
                this.pmtMethod.setValue(methodid);                                
                this.ShowCheckDetails(this.pmtMethod, rec);       
            }
        }
    },
    
    onPaymentMethodChange: function(comboboxObject,newValue,oldValue){
        if(this.isEdit && oldValue!=undefined && oldValue!=""){
            
            var newrec = WtfGlobal.searchRecord(this.pmtStore, newValue, "methodid");
            var newCurrency=newrec.data.acccurrency;
            var newMethodType=newrec.data.detailtype;
            
            var oldrec = WtfGlobal.searchRecord(this.pmtStore, oldValue, "methodid");
            var oldCurrency=oldrec.data.acccurrency;
            var oldMethodType = oldrec.data.detailtype;
            
            if(this.isEdit ){
                /* 
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
            }else{
                this.ShowCheckDetails(comboboxObject, newrec);
            }
        } else {
            this.getConfirmationToChangePmtMethod(comboboxObject,newValue,oldValue);
        }
        this.updateSubtotal();
    },
    
    onPaymentTypeChange: function(comboboxObject,newValue,oldValue){
        if(oldValue==newValue){                                       
            
        } else {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalanceType"),WtfGlobal.getLocaleText("acc.account.OpeningBalanceType"),function(btn){
                if(btn!="yes") {               
                    comboboxObject.setValue(oldValue);
                    return;
                } else {
                    if (comboboxObject.getValue() == "Withdraw") {
                        this.isDeposit=false;
                    } else {
                        this.isDeposit=true;
                    }
                    this.pmtStore.load();
                    this.pmtMethod.setValue("");
                    this.pmtMethodAcc.setValue("");
                    if(this.SouthForm) {
                        this.centerPanel.items.remove(this.SouthForm)
                        this.SouthForm.destroy();
                        this.SouthForm = undefined;
                } 
                    this.amount.setValue(0);
                    this.amountInBase.setValue(0);
                } 
            },this);
        }
    },
    
    onPaymentMethodSelect: function(combobox,rec,index){
        if(!this.isEdit ){
            var currencyForSelectedRecord= rec.data['acccurrency'];
            this.Currency.setValue(currencyForSelectedRecord);
            this.updateExternalCurrencyRateOnCurrencyChange();
        }
        this.updateSubtotal();
    },
    
    onPaymentMethodBeforeSelect :function ( combo, record,index ) {
        if(this.isEdit ){
            var currencyForSelectedRecord= record.data['acccurrency'];
            if(currencyForSelectedRecord == this.Currency.getValue())
            {
                return true;
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.paymentmethodchangemessage")], 2);
                return false;
            }
        }
    },
    
    getConfirmationToChangePmtMethod : function(comboboxObject,newValue,oldValue){
        if(oldValue==''){                                       // Alert will not be shown when payment method is changed initially, as its default valu will be blank('')
            var selectedMethod=newValue;
                var index = this.pmtStore.findBy(function(rec) {
                    var methodId = rec.data['methodid'];
                    if (methodId == selectedMethod)
                        return true;
                },this);
                var record= this.pmtStore.getAt(index);
                this.ShowCheckDetails(comboboxObject, record);
        } else {
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.masterConfig.payMethod"),WtfGlobal.getLocaleText("acc.openingBalances.exchangeRate"),function(btn){
            if(btn!="yes") {               
                comboboxObject.setValue(oldValue);
                var oldPmtRec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
                this.pmtMethod.fireEvent('select',this.pmtMethod,oldPmtRec,0,this);
                this.updateExternalCurrencyRateOnCurrencyChange();
                return;
            } else {
                this.amount.setValue(0);
                this.amountInBase.setValue(0);
                var selectedMethod=comboboxObject.value;
                var index = this.pmtStore.findBy(function(rec) {
                    var methodId = rec.data['methodid'];
                    if (methodId == selectedMethod)
                        return true;
                },this);
                var record= this.pmtStore.getAt(index);
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
                          this.changeCurrencyStore()    ;
                    }, this);
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.10")], 2);
                    return;
                }
                this.ShowCheckDetails(comboboxObject, record);
            } 
        },this);
        }
    },
    
    updateExternalCurrencyRateOnCurrencyChange : function(){
        var currencyRecord = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
        this.externalcurrencyrate = currencyRecord.data['exchangerate'];
        if(WtfGlobal.getCurrencyID() == this.Currency.getValue()){
            this.exchangeRateForOtherCurrency.setValue(1);
            this.exchangeRateForOtherCurrency.disable();
            this.CurrencyToBaseExchangeRate.disable();
        }else{
            this.exchangeRateForOtherCurrency.setValue(currencyRecord.data['exchangerate']);
            this.exchangeRateForOtherCurrency.enable();
            this.CurrencyToBaseExchangeRate.enable();
        }
    },
    
    loadCheckDetails: function(rec) {
        this.hideShowChequeFields(rec.data.detailtype, rec.data.paymentmethodacc, rec.data.paymentmethodaccname);
    },
    
    ShowCheckDetails: function(combo, rec,isBankToBankMethodChange) {
        var accCurr = rec.data['acccurrency'];
        /*
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
                var isReceipt = (this.isAccount != null && this.isAccount != undefined && this.isAccount != '')? this.isDeposit :(this.isCustomer?true:false);
                this.SouthForm = new Wtf.account.PayMethodPanelNew({
                    region: "south",
                    hideMode: 'display',
                    isReceipt: isReceipt,
                    isAccount:(this.isAccount != null && this.isAccount != undefined && this.isAccount != '')?this.isAccount:false,
                    type: detailType,
                    disabledClass: "newtripcmbss",
                    autoHeight: true,
                    autoWidth: true,
                    style: 'padding-left: 20px; background: #f1f1f1;',
                    id: this.id + 'southform',
                    border: false,
                    isEdit:this.isEdit
                });
                this.centerPanel.items.add(this.SouthForm);
                this.SouthForm.on("render",function(){
                    if(!this.isReceipt){
                        if (detailType == this.paymentMethodType.Bank) {
                            if (!this.isEdit) {
                                this.SouthForm.setNextChequeNumber(paymentMethodAccountId);
                            }
                        }
                        if (methodAccountName != undefined) {
                            this.SouthForm.setBankName(methodAccountName);
                        }
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
        this.doLayout();
    },
    
    createTransactionForm:function(){
        this.transactionInfoForm=new Wtf.form.FormPanel({
            region:'center',
            autoHeight:true,
            layout:'fit',
            id:"formPanel"+this.id,
            labelWidth:150,
            border:false,
            bodyStyle: "background: transparent; padding-left: 20px; padding-right: 20px; padding-top: 20px;",
            defaultType: 'textfield',
            items:[{
                xtype:'fieldset',
                autoHeight:true,
                autoWidth:true,
                title:WtfGlobal.getLocaleText("acc.field.DocumentInformation"),
                border : false,
                labelWidth:150,
                items: [{
                    layout : 'column',
                    border : false,
                    items: [
                    {
                        columnWidth: '.45',
                        layout : 'form',
                        border : false,
                        items:[this.Number,this.billDate,this.Memo]
                    },{
                        columnWidth: '.45',
                        layout : 'form',
                        border : false,
                        items:[this.Term,this.DueDate,this.paymentType,this.pmtMethod,this.pmtMethodAcc]
                    }
                    ]
                }]
            },{
                xtype:'fieldset',
                title:WtfGlobal.getLocaleText("acc.field.AmountInformation"),
                autoHeight:true,
                autoWidth:true,
                border : false,
                labelWidth:150,
                items: [{
                    layout : 'column',
                    border : false,
                    items: [
                    {
                        columnWidth: '.45',
                        layout : 'form',
                        border : false,
                        items:[this.Currency,this.CurrencyToBaseExchangeRate,this.exchangeRateForOtherCurrency,this.narration]
                    },{
                        columnWidth: '.45',
                        layout : 'form',
                        border : false,
                        items:[this.exlGstamount,this.taxAmount,this.amount,this.amountInBase]
                    }
                    ]
                }]
            },{
                xtype:'fieldset',
                title:WtfGlobal.getLocaleText("acc.customerList.gridOtherInfo"),
                hidden:!this.isInvoice,
                autoHeight:true,
                autoWidth:true,
                border : false,
                labelWidth:150,
                items: [{
                    layout : 'column',
                    border : false,
                    items: [
                    {
                        columnWidth: '.45',
                        layout : 'form',
                        border : false,
                        items:[this.PONumber,this.users]
                    },{
                        columnWidth: '.45',
                        layout : 'form',
                        border : false,
                        items:[this.poDate]
                    }
                    ]
                }]
            },this.tagsFieldset]
        });
    },
    
    createTransactionFormForPO:function(){
        
        var orderInfo="";
        if(this.isCustomer){
            orderInfo=new Wtf.XTemplate(
                "<div> &nbsp;</div>",  //Currency:
                '<tpl>',
                "<div style='padding-left:30px;' class='openingOrderText'>"+WtfGlobal.getLocaleText("acc.field.ClickonthelinkgivenbelowtocreateopeningSalesOrder")+" \n\ "+
                    WtfGlobal.getLocaleText("acc.field.YouwillberedirectedtoSalesOrdercreationformwhereyoumaycreateSalesOrder")+"\n\ "+
                    WtfGlobal.getLocaleText("acc.field.TheorderdatewillbesomedatebeforeFinancialYearStartDate")+"\n\
                </div>",
                "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: callOpeningOrderForm(\""+this.isCustomer+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.field.CreateOrder")+"</a>"+"</div>",
                '</tpl>'
                );
        }else{
            orderInfo=new Wtf.XTemplate(
                "<div> &nbsp;</div>",  //Currency:
                '<tpl>',
                "<div style='padding-left:30px;' class='openingOrderText'>"+WtfGlobal.getLocaleText("acc.field.ClickonthelinkgivenbelowtocreateopeningPurchaseOrder") +"\n\ "+
                    WtfGlobal.getLocaleText("acc.field.YouwillberedirectedtoPurchaseOrdercreationformwhereyoumaycreatePurchaseOrder")+"\n\ "+
                    WtfGlobal.getLocaleText("acc.field.TheorderdatewillbesomedatebeforeFinancialYearStartDate")+"\n\
                </div>",
                "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: callOpeningOrderForm(\""+this.isCustomer+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.field.CreateOrder")+"</a>"+"</div>",
                '</tpl>'
                );
        }
        
        
            
        var orderInfoPanel = new Wtf.Panel({
            border:false,
            html:orderInfo.apply()
        })
        
        this.transactionInfoForm=new Wtf.form.FormPanel({
            region:'center',
            autoHeight:true,
            layout:'fit',
            labelWidth:150,
            border:false,
            bodyStyle: "background: transparent; padding: 20px;",
            defaultType: 'textfield',
            items:[{
                xtype:'fieldset',
                autoHeight:true,
                autoWidth:true,
                title:'Order',
                border : false,
                labelWidth:150,
                items: [orderInfoPanel]
            }]
        });
    },
    // Excise Opening Balance check from Vendor Master ERP-27108 
    createTransactionFormForExciseInvoice:function(){
        var orderInfo="";
        if(this.isCustomer){
            orderInfo=new Wtf.XTemplate(
            "<div> &nbsp;</div>",  //Currency:
            '<tpl>',            
            "<div style='padding-left:30px;' class='openingOrderText'>"+WtfGlobal.getLocaleText("acc.field.ClickonthelinkgivenbelowtocreateopeningExciseInvoice")+" \n\ "+
                WtfGlobal.getLocaleText("acc.field.YouwillberedirectedtoSalesInvoicercreationformwhereyoumaycreateExciseInvoice")+"\n\ "+
                WtfGlobal.getLocaleText("acc.field.TheorderdatewillbesomedatebeforeFinancialYearStartDate")+"\n\
                </div>",
            "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: callExicesOpeningGoodsReceipt(\""+this.isCustomer+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.field.india.excise.invoice")+"</a>"+"</div>",
            '</tpl>'
        );
        }else{
             orderInfo=new Wtf.XTemplate(
            "<div> &nbsp;</div>",  //Currency:
            '<tpl>',            
            "<div style='padding-left:30px;' class='openingOrderText'>"+WtfGlobal.getLocaleText("acc.field.ClickonthelinkgivenbelowtocreateopeningExcisePurchaseInvoice")+" \n\ "+
                WtfGlobal.getLocaleText("acc.field.YouwillberedirectedtoPurchaseOrdercreationformwhereyoumaycreateExcisePurchaseInvoice")+"\n\ "+
                WtfGlobal.getLocaleText("acc.field.TheorderdatewillbesomedatebeforeFinancialYearStartDate")+"\n\
                </div>",
            "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: callExicesOpeningGoodsReceipt(\""+this.isCustomer+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.field.india.excise.invoice.purchase")+"</a>"+"</div>",
            '</tpl>'
        );
        }
        var exciseInfoPanel = new Wtf.Panel({
            border:false,
            html:orderInfo.apply()
        })
        
        this.transactionInfoForm=new Wtf.form.FormPanel({
            region:'center',
            autoHeight:true,
            layout:'fit',
            labelWidth:150,
            border:false,
            bodyStyle: "background: transparent; padding: 20px;",
            defaultType: 'textfield',
            items:[{
                xtype:'fieldset',
                autoHeight:true,
                autoWidth:true,
                title:(this.isCustomer ?WtfGlobal.getLocaleText("acc.field.india.excise.invoice"):WtfGlobal.getLocaleText("acc.field.india.excise.invoice.purchase")),
                border : false,
                labelWidth:150,
                items: [exciseInfoPanel]
            }]
        });
    },    
    closeTransactionForm:function(){
        this.close();
    },

    getInvalidFields: function() {
        var invalidFields = []
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
    
    saveTransactionForm:function(){
        this.Number.setValue(this.Number.getValue().trim()); 
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        var isValidChequeDetails= true;
        if(this.SouthForm){
            isValidChequeDetails = this.SouthForm.getForm().isValid();
        }
        if(!isValidChequeDetails || !isValidCustomFields){
//            WtfGlobal.dispalyErrorMessageDetails(this.id+'requiredfieldmessagepanel', this.getInvalidFields());
            return;
        } 
        
        var isAmountValid = true;
        var val = this.exlGstamount.getValue();
        if(val !=undefined && val.toString().length > 15 && !(val.toString().length == 16 && val.toString().indexOf('.') > -1)){
            this.exlGstamount.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
            isAmountValid = false;
        }
        val = this.amount.getValue();
        if(val !=undefined && val.toString().length > 15 && !(val.toString().length == 16 && val.toString().indexOf('.') > -1)){
            this.amount.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
            isAmountValid = false;
        }
        val = this.amountInBase.getValue();
        if(val !=undefined && val.toString().length > 15 && !(val.toString().length == 16 && val.toString().indexOf('.') > -1)){
            this.amountInBase.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
            isAmountValid = false;
        }
        val = this.taxAmount.getValue();
        if(val !=undefined && val.toString().length > 15 && !(val.toString().length == 16 && val.toString().indexOf('.') > -1)){
            this.taxAmount.markInvalid(WtfGlobal.getLocaleText("acc.openingInvBalance.invalid"));
            isAmountValid = false;
        }
        if(!isAmountValid){
            WtfComMsgBox(2, 2);
            return;
        }
        
    this.Number.setValue(this.Number.getValue().trim());
    if(this.transactionInfoForm.getForm().isValid() && isValidCustomFields){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
            if(btn!="yes") {
                this.isWarnConfirm=false;
                return;
            }
            var rec = this.transactionInfoForm.getForm().getValues();
            rec.number=this.Number.getValue();
            if(this.PONumber.emptyText == rec.porefno){
              rec.porefno="";    
            }
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            if (custFieldArr.length > 0)
            rec.customfield = JSON.stringify(custFieldArr);
            rec.dueDate=WtfGlobal.convertToGenericDate(this.DueDate.getValue());
            rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.poRefDate=WtfGlobal.convertToGenericDate(this.poDate.getValue());
            if (this.SouthForm) {
                rec.paydetail = JSON.stringify(this.SouthForm.form.getValues());
            }
//            rec.chequeDate=WtfGlobal.convertToGenericDate(this.chequeDate.getValue());
            rec.exchangeRateForOpeningTransaction = this.exchangeRateForOtherCurrency.getValue();
            rec.CurrencyToBaseExchangeRate = this.CurrencyToBaseExchangeRate.getValue();
            var rec1 = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
            rec.paymentMethodID = (rec1)?rec1.data.methodid:"";
            rec.isPayment = !this.isDeposit;
            
            
            if(this.isInvoice){
            rec.termid = this.Term.getValue();
            rec.excludingGstAmount = this.exlGstamount.getValue();
            rec.taxAmount = this.taxAmount.getValue();
            }
            if(this.isEdit){
                rec.transactionId = this.record.get('transactionId');
                if(this.isInvoice){
                    rec.invoiceid = this.record.get('transactionId');
                }
            }
            if(Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoWarn && !this.isWarnConfirm){
              rec.isWarn = true;
            }
            if(this.isWarnConfirm){
              rec.isWarn = false;
            }
            rec.accountId = this.accountId;
            if(this.Memo){
                rec.memo = this.Memo.getValue();
            }

            // Passing incash Flag as false for Opening Document to save There terms. 
            rec.incash = false;
            
            this.url = "";
                if(this.isAccount != null && this.isAccount != undefined && this.isAccount != ''){
                    this.url = "ACCVendorPaymentNew/saveAccountOpeningBalanceTransaction.do";
                }else if(this.isCustomer){
                    if(this.isPayment){
                        this.url = "ACCReceipt/saveOpeningBalanceReceipt.do";
                    }else if(this.isInvoice){
                        this.url = "ACCInvoice/saveOpeningBalanceInvoice.do";
                    }else if(this.isCreditNote){
                        this.url = "ACCCreditNote/saveOpeningBalanceCN.do";
                    }else if(this.isDebitNote){
                        this.url = "ACCDebitNote/saveOpeningBalanceCustomerDN.do";
                    }
                }else{
                    if(this.isPayment){
                        this.url = "ACCVendorPaymentNew/saveOpeningBalancePayment.do";
                    }else if(this.isInvoice){
                        this.url = "ACCGoodsReceipt/saveOpeningBalanceGoodsReceipt.do";
                    }else if(this.isCreditNote){ 
                        this.url = "ACCCreditNote/saveOpeningBalanceVendorCN.do";
                    }else if(this.isDebitNote){
                        this.url = "ACCDebitNote/saveOpeningBalanceDN.do";
                    }
                }

            Wtf.Ajax.requestEx({
                url:this.url,
                //                    url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    }else{
         WtfComMsgBox(2, 2);
    }
    
//        this.close();
},
    
genSuccessResponse:function(response, request){
    if(response.success){
        WtfComMsgBox([this.title,response.msg],response.success*2+1);
        this.fireEvent('datasaved',this);
        this.close();
    }else if(response.accException){
        Wtf.MessageBox.hide();
            var label = "";
            switch (this.moduleId) {
                case Wtf.Acc_Credit_Note_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.CN.newcreditnoteno");
                    break;
                case Wtf.Acc_Debit_Note_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.DN.newdebitnoteno");
                    break;
                case Wtf.Acc_Vendor_Invoice_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.INV.newpurchaseinvno");
                    break;
                case Wtf.Acc_Invoice_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.INV.newdinvoiceno");
                    break;
                case Wtf.Acc_Make_Payment_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.payment.newpaymnetno");
                    break;
                case Wtf.Acc_Receive_Payment_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.payment.newreceivepaymnetno");
                    break;
            }
            if(this.isAccount != null && this.isAccount != undefined && this.isAccount != ''){
                label = WtfGlobal.getLocaleText("acc.cnList.TransNo");
            }
            this.newnowin = new Wtf.Window({
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
                        html: (response.msg.length > 60) ? response.msg : "<br>" + response.msg,
                        height: 55,
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
                                        switch (this.moduleId) {
                                            case Wtf.Acc_Credit_Note_ModuleId:
                                            case Wtf.Acc_Debit_Note_ModuleId:
                                            case Wtf.Acc_Vendor_Invoice_ModuleId:
                                            case Wtf.Acc_Invoice_ModuleId:
                                            case Wtf.Acc_Make_Payment_ModuleId:
                                            case Wtf.Acc_Receive_Payment_ModuleId:
                                                 Wtf.getCmp("invoiceNo"+this.id).setValue(this.newdono.getValue());
                                                 break;
                                        }
                                        
                                        if(this.isAccount != null && this.isAccount != undefined && this.isAccount != ''){
                                            Wtf.getCmp("invoiceNo"+this.id).setValue(this.newdono.getValue());
                                        }
                                        this.saveTransactionForm();
                                        this.newnowin.close();
                                    }
                                },
                                scope: this
                            }, {
                                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                                scope: this,
                                handler: function () {
                                    this.newnowin.close();
                                }
                            }]
                    })]
            });
            this.newnowin.show();
    }else {
        if(!response.success && response.isWarn){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), // 'Warning',
                    msg: response.msg+""+WtfGlobal.getLocaleText("acc.ven.msg4"),
                    buttons: Wtf.MessageBox.YESNO,
                    width:450,
                    fn: function(btn) {
                        if(btn =="yes") {
                            this.isWarnConfirm = true;
                            this.saveTransactionForm();
                        }
                    },
                    scope: this,
                    icon: Wtf.MessageBox.QUESTION
                },this);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
            }
        
        }
},
    
genFailureResponse:function(response){
    //        Wtf.MessageBox.hide();
    var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
    if(response.msg)msg=response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
},
    
setTransactionNumber:function(isSelectNoFromCombo){
    var temp2=Wtf.autoNum.Invoice;
    if(isSelectNoFromCombo){
        this.fromnumber = temp2;
    }
},
    
updateDueDate:function(a,val){
    var term=null;
        if(this.Term.getValue()!="" && this.Term.getValue()!=null && this.Term.getValue()!=undefined){
            rec = this.Term.store.getAt(this.Term.store.find('termid',this.Term.getValue()));
            term=new Date(this.billDate.getValue()).add(Date.DAY, rec.data.termdays);
    }
        else{
        term=this.billDate.getValue();
        }
    this.transactionInfoForm.getForm().setValues({
        dueDate:term
    });
    if(this.Grid){
        this.Grid.billDate = this.billDate.getValue()
    }
    var rec = this.Term.store.getAt(this.Term.store.find('termdays',this.Term.getValue()));
    this.termid=rec.data.termid;
},


onCurrencyToBaseExchangeRateChange:function(field,newValue,oldValue){
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.currencytobaseexchangeratechange")],3);
    this.updateExchangeRate(newValue);   
    this.updateSubtotal();
},

updateSubtotal:function(){
    var amount;
        if(this.isInvoice){
            amount = this.exlGstamount.getValue();
            var taxAmt=this.taxAmount.getValue();
            amount+=taxAmt;
        }else{
            amount = this.amount.getValue();
        }
    this.amount.setValue(amount);
    if(amount == undefined || amount == null || amount == ''){
        amount = 0;
    }
    
    var exchangeRateForOtherCurrency = this.exchangeRateForOtherCurrency.getValue();
    if(exchangeRateForOtherCurrency == undefined || exchangeRateForOtherCurrency == null || exchangeRateForOtherCurrency == ''){
        exchangeRateForOtherCurrency = 0;
    }
    
    var amountInBase = 0;
    var isCurrencyToBaseExchangeRate = this.CurrencyToBaseExchangeRate.getValue()
    if(isCurrencyToBaseExchangeRate){
        amountInBase = amount*exchangeRateForOtherCurrency;
    }else{
        amountInBase = amount/exchangeRateForOtherCurrency;
    }
    
    amountInBase = WtfGlobal.conventInDecimalWithoutSymbol(amountInBase);
    this.amountInBase.setValue(amountInBase);
},
    
getExchangeRate:function(){
    var index=this.getCurrencySymbol();
    //        var rate=this.externalcurrencyrate;
    var revExchangeRate = 0;
    if(index>=0){
        var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
        if(this.externalcurrencyrate>0) {
            exchangeRate = this.externalcurrencyrate;
        }
        revExchangeRate = 1/(exchangeRate);
        revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
    }
    return revExchangeRate;
},

getDates:function(start){
    var d=Wtf.serverDate;
    //        if(this.statementType=='BalanceSheet'&&start)
    //             return new Date('January 1, 1970 00:00:00 AM');
    var monthDateStr=d.format('M d');
    if(Wtf.account.companyAccountPref.bbfrom)
        monthDateStr=Wtf.account.companyAccountPref.bbfrom.format('M d');
    var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
    if(d<fd)
        fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
    if(start)
        return fd.add(Date.YEAR, 0).add(Date.DAY, -1);
    return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
},
getCurrencySymbol:function(){
    var index=null;
    this.currencyStore.clearFilter(true);
    var FIND = this.Currency.getValue();
    if(FIND == "" || FIND == undefined || FIND == null) {
        FIND = WtfGlobal.getCurrencyID();
    }
    index=this.currencyStore.findBy( function(rec){
        var parentname=rec.data['currencyid'];
        if(parentname==FIND)
            return true;
        else
            return false
    })
    this.currencyid=this.Currency.getValue();
    return index;
},
updateExchangeRate:function(isCurrencyToBaseExchangeRate){
    // Update Exchange Rate according to 'Currency To Base Exchange Rate' check.
    var currencyRecord = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
    if(isCurrencyToBaseExchangeRate){
        this.exchangeRateForOtherCurrency.setValue(currencyRecord.data['foreigntobaseexchangerate']);
    }else{
        this.exchangeRateForOtherCurrency.setValue(currencyRecord.data['exchangerate']);
    } 
}
});