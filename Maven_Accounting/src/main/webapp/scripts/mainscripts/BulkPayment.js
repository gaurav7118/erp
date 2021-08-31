
Wtf.PaymentEntry = function(config) {
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
    //For Create New case of TDS Payment(For India Country).
    this.tdsPaymentJsonFlag = (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && config.taxPaymentDataParams != null && config.taxPaymentDataParams != undefined && config.taxPaymentDataParams != "" )? config.taxPaymentDataParams.tdsPaymentJsonFlag : false;
    /*
     * isbulkpayment is true if click on bulkpayment from invoice report
     */
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
    this.recArray=config.recArray;
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
    /*
     *Check for Warning message If balance amount is Greater than zero
     */
    this.checkWarning=true;
    this.isCopyReceipt = config.isCopyReceipt;
    this.isAllowedSpecificFields=(config.isAllowedSpecificFields == null || config.isAllowedSpecificFields == undefined)? false : config.isAllowedSpecificFields;
    this.pendingApproval=(config.ispendingAproval == null || config.ispendingAproval == undefined)? false : config.ispendingAproval;
    this.isIBGTypeTransaction = false; // this value will changed on Payment Method. If payment method is of IBG type then value will set to true
   // this.LifoFifoMode=false;         // Will be true when Lifo Fifo Invoices are loaded    
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
        disabled:true,
        valueField: 'currencyid',
        forceSelection: true,
        displayField: 'currencyname',
        scope: this,
        selectOnFocus: true
    });
   

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
    //this.pmtMethod.on('select', this.onPaymentMethodSelect, this); // Event is added for setting the currency of the payment method account again , after changing the currency from currency combobox
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
 
  
        Wtf.FormUnitStore.load();       
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
    
    
    //this.paidTo.addNewFn = this.addPaidTo.createDelegate(this);
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
        disabled:true,
        emptyText: WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
        anchor: '85%'
    });

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
                        items: [this.sequenceFormatCombobox,this.No, this.creationDate,this.nonRefundable]
                    }, {
                        layout: 'form',
                        columnWidth: 0.33,
                        id: this.id + 'MiddleColumnform',
                        items: [this.Currency, this.paymentCurrencyToPaymentMethodCurrencyExchangeRate, this.Amount]
                    }, {
                        layout: 'form',
                        columnWidth: 0.33,
                        id: this.id + 'LastColumnform',
                        items: [this.pmtMethod, this.pmtMethodAcc, this.Memo,this.CINNo]
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

    panelItemsArra.push(this.grid);
  
    
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
                text: "Create Bulk Payment",//'Save',
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
                    this.save();
                }
            }),this.saveAndPrintCheque = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.bulkPaymentandprintcheque"), //'Create Bulk Payment and Print Cheque'
                tooltip: WtfGlobal.getLocaleText("acc.common.bulkPaymentandprintchequetooltip"),
                scope: this,
                id: "saveandprintcheque" + config.helpmodeid + this.id,
                hidden: true,
                iconCls: getButtonIconCls(Wtf.etype.save),
                handler: function() {
                    this.saveAndCreateNewFlag = false;                    // This flag is used to differentiate between Save button and Save and Create New button
                    this.isChequePrint = true;
                    this.save();
                }
            })
                              
          ]
    });
    this.Amount.on('focus', function(field) {
        if (field.getValue() == 0) {
            field.setValue("");
        }
    }, this);
    //this.Amount.on('blur',this.onAmountFieldBlur,this);
    this.grid.on('datachanged', this.onGridDataChanged, this);
    this.grid.on('updatecustomdata', this.setcustomdata, this);
    /**
     * Added event when creation date is changed as we have to validate that invoice date is not after the payment date 
     * and to validate whether discount will be applied or not. ERM-981
     */
    this.creationDate.on('change',this.onDateChange,this);  
    //this.Currency.on('change',this.onCurrencyChange,this);
    Wtf.PaymentEntry.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true
    });
}



Wtf.extend(Wtf.PaymentEntry, Wtf.account.ClosablePanel, {
    onRender: function(config) {
        Wtf.PaymentEntry.superclass.onRender.call(this, config);
        if(!this.readOnly){
            this.isClosable=false;         // This flag is used to give an alert if user is closing the form without saving the data. In view case, alert will not be given.
        }
        if(this.sequenceFormatStore.getCount() > 0){
            this.setNextNumber();
        } else {
            this.sequenceFormatStore.on('load', this.setNextNumber, this);
        }
        chkPaySeqFormatStoreLoad();
        
        if (this.isEdit || this.isCopyReceipt) {
            
            if (this.record!=undefined && this.record.data.detailType != this.paymentMethodType.Bank) {
                this.saveAndPrintCheque.hide();
            }
        }
        if(this.isBulkPayment){
            var cnt=0;
            var maxDate;
            var date;
            var currentDate=new Date();
            var checkDate=false;
            var recordArr= eval(this.invObj.getSelectedRecords());
            //this.Name.setValForRemoteStore(recordArr[0].personid,recordArr[0].personname);
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
         
        }
    
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
        if(this.nonRefundable!=undefined){
            this.nonRefundable.setValue(recData.nonRefundable);
        }
        this.setComboValues(recData);
        if(this.isCopyReceipt){
            this.sequenceFormatCombobox.fireEvent('select',this.sequenceFormatCombobox);
        } 
        this.checkIsIBGTransaction(rec,true);
        this.NorthForm.form.loadRecord(rec);
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
        //this.loadCheckDetails(rec);
        if(this.SouthForm) {
            this.SouthForm.form.loadRecord(rec.data.paydetail);
            this.SouthForm.setOtherFieldInfoOnRecordLoad();
        }
        this.loadLineDetails(recDetails);
        if(this.isCopyReceipt){
            this.checkForValidLineLevelData();
            this.updateAmount(getRoundedAmountValue(this.totalAmountWithTDS()));
        }
        //this.updateSouthTemp();
        //this.updateBalanceAmount();
     
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
            
        this.Memo.enable();
            
    },
    loadLineDetails: function(recDetails) {
        this.grid.store.loadData(recDetails);
        if(this.isEdit&&this.pendingApproval&&!this.readOnly){
            this.grid.getStore().each(function(rec){
                rec.set('enteramount',0)
            },this);
        }
        this.grid.addBlankRow();
    },
  
    addAccount: function(store) {
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update", function() {
            store.reload()
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
        this.grid = new Wtf.bulkPaymentGrid({
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
            disabled: this.disableGridInVATCSTExcisePayment ,
            moduleid: this.moduleid,
            isCustomer:this.isCustomer,
            height: 300,
            paymentEntryObject: this,
            isAllowedSpecificFields:this.isAllowedSpecificFields,
            cls: 'gridFormat',
            isReceipt: this.isReceipt,
            isMultiDebit: this.isMultiDebit,
            amount: 0,
            closable: true,
            billid:(this.record!= undefined)?this.record.data.billid:'',
            AllowToEditCopy:(this.record!= undefined && this.record.data.AllowToEditCopy!=undefined)?this.record.data.AllowToEditCopy:true,
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
        } else {
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
  
        
},
    applyCurrencySymbol: function() {
        var index = this.getCurrencySymbol();
        if (index >= 0) {
          
            this.applyTemplate(this.currencyStore, index);
        }
    },
    getCurrencySymbol: function() {
        var index = null;
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

    getAmountPaidInBase: function(amountInOriganalCurr) {
        var amountInBaseCurr = 0;
        amountInBaseCurr = amountInOriganalCurr * this.getExchangeRate();
        return WtfGlobal.conventInDecimalWithoutSymbol(amountInBaseCurr);
    },
  
    
    save: function() {
        var isValidNorthForm=this.NorthForm.getForm().isValid();
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        var isValidChequeDetails= true;
        var isValidCreditAndDebitDetails=false;
        if(this.SouthForm){
            isValidChequeDetails = this.SouthForm.getForm().isValid();

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
     
      isValidCreditAndDebitDetails = true;//this.grid.isValidCreditAndDebitDetails();
        if(!isValidCreditAndDebitDetails){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.alertCreditDebitDetailsInvalid")],2);
            return
        }
        if(getRoundedAmountValue(this.Amount.getValue())==0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.totalAmtCanNotBeZero")],2);
                return
        }
        
        /*
         *Check for Total amount of selected transactions does not match to  amount  you have entered.
         */
        if(this.paymentType != this.paymentOption.AgainstGL){
            var amount=this.totalAmountWithTDS();
            if(this.Amount.getValue()<amount){
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.totalAmountDoesNotMatchToAmount")],2);
                return;
            }
        }
        

      
        var payAccName= '';
        var pmRec = WtfGlobal.searchRecord(this.pmtStore,this.pmtMethod.getValue(),'methodid');
        if(pmRec) {
            payAccName = pmRec.data.accountname;
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
       
        
          var isFormValid=this.formValidationBeforeSave()
            if(!isFormValid){
              return;
            }
        
        
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
            WtfGlobal.onFormSumbitGetDisableFieldValues(this.NorthForm.form.items, jsonObject);
            var custFieldArr = this.tagsFieldset.createFieldValuesArray();
            if (custFieldArr.length > 0)
                jsonObject['customfield'] = JSON.stringify(custFieldArr);   
            
            jsonObject['Details'] = this.grid.getData();
            jsonObject['postdate'] =WtfGlobal.convertToGenericDate(new Date());
              jsonObject['paydetail'] =  this.grid.getData();
            jsonObject['balaceAmount'] =this.finalBalanceAmount;
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
            //jsonObject['ibgCode'] = this.ibgTransactionCode.getValue();
            jsonObject['isEditToApprove'] = this.isEditToApprove
            var rec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
            var bankType = rec.data.bankType;
            jsonObject['bankType']= bankType;
            this.saveBttn.disable();
            this.saveAndPrintCheque.disable();
            
            //this.savencreateBttn.disable();
            WtfComMsgBox(27, 4, true);
            this.ajxUrl = this.getUrlForSave();
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
        
           var url = "ACCVendorPaymentNew/saveBulkPayment.do";
        
        return url
    },
    genSavePaymentSuccessResponse: function(response, request) {
        /**
         * Handle warn case for cheque duplication. SDP-12577
         */
        if (!response.success && response.isWarn) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), // 'Warning',
                msg: response.msg + "" + WtfGlobal.getLocaleText("acc.ven.msg4"),
                buttons: Wtf.MessageBox.YESNO,
                width: 450,
                fn: function (btn) {
                    if (btn == "yes") {
                        this.isWarnConfirm = true;
                        this.saveData();
                    } else {
                        this.enableSaveButton();
                    }
                },
                scope: this,
                icon: Wtf.MessageBox.QUESTION
            });
        } else{
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
        /*Save Case*/
        if (response.success) {
            this.isClosable = true;
            this.disableComponents();
        } else {
            this.enableSaveButton();
        }
        
        if (this.isChequePrint && response.success) {
            var newwin = window.open('', 'printwin', 'left=0,top=0,width=800,height=300,scrollbars = no');
            var printHtml = "";
            var resdata = response.data[0];
            printHtml += '<HTML>\n<HEAD>\n';
            printHtml += '<TITLE>Print Page</TITLE>\n';
            if (resdata.isnewlayout) {
                printHtml += '<style>';
                printHtml += '@page {size: portrait;margin:0mm 0mm 0mm 0mm;}';
                printHtml += 'body  {margin: 0mm 0mm 0mm 0mm;}';
                printHtml += '</style>';
            } else {
                printHtml += '<link rel="stylesheet" type="text/css" href="style/print.css"/>\n';
            }
            printHtml += '<script>\n';
            printHtml += 'function chkstate(){\n';
            printHtml += 'if(document.readyState=="complete"){\n';
            printHtml += 'window.close()\n';
            printHtml += '}\n';
            printHtml += 'else{\n';
            printHtml += 'setTimeout("chkstate()",2000)\n';
            printHtml += '}\n';
            printHtml += '}\n';
            printHtml += 'function print_win(){\n';
            printHtml += 'window.print();\n';
            printHtml += 'chkstate();\n';
            printHtml += '}\n';
            printHtml += '</script>\n';
            printHtml += '</HEAD>\n';
            printHtml += '<BODY onload="print_win()">\n';
            for (var cnt = 0; cnt < response.data.length; cnt++) {
                resdata = response.data[cnt];
                printHtml += this.printCheque(resdata); //Passing Paramter as JSON Object
            }
            printHtml += '</BODY>\n';
            printHtml += '</HTML>\n';
            newwin.document.write(printHtml);
            newwin.document.close();

//        newwin.document.write('<HTML>\n<HEAD>\n')
//        newwin.document.write('<TITLE>Print Page</TITLE>\n')
////        if (resdata.isnewlayout) {
//            newwin.document.write('<style>')
//            newwin.document.write('@page {size: portrait;margin:0mm 0mm 0mm 0mm;}')
//            newwin.document.write('body  {margin: 0mm 0mm 0mm 0mm;}')
//            newwin.document.write('</style>')
////        } else {
////            newwin.document.write('<link rel="stylesheet" type="text/css" href="style/print.css"/>\n')
////        }
//        newwin.document.write('<script>\n')
//        newwin.document.write('function chkstate(){\n')
//        newwin.document.write('if(document.readyState=="complete"){\n')
//        newwin.document.write('window.close()\n')
//        newwin.document.write('}\n')
//        newwin.document.write('else{\n')
//        newwin.document.write('setTimeout("chkstate()",2000)\n')
//        newwin.document.write('}\n')
//        newwin.document.write('}\n')
//        newwin.document.write('function print_win(){\n')
//        newwin.document.write('\n')
//        newwin.document.write('chkstate();\n')
//        newwin.document.write('}\n')
//        newwin.document.write('</script>\n')
//        newwin.document.write('</HEAD>\n')
//        newwin.document.write('<BODY onload="print_win()">\n')
//            for(var cnt=0;cnt<response.data.length;cnt++){
//                var resdata = response.data[cnt];
//               newwin.document.write(this.printCheque(resdata)); //Passing Paramter as JSON Object
//            }
//            newwin.document.write('</BODY>\n')
//        newwin.document.write('</HTML>\n');
//        newwin.document.close()
            this.isChequePrint = false;
        }
    },
    genSavePaymentFailureResponse: function(response) {
        Wtf.MessageBox.hide();
        this.enableSaveButton();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
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
                      
                        this.isIBGTransactionCheckedInEditCase=true;
                      
                        return;
                    }
                    this.isIBGTransactionCheckedInEditCase=true;
                    this.isIBGTypeTransaction = true;
                   
                    var rec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
                    var bankType = rec.data.bankType
                  
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
                this.isIBGTransactionCheckedInEditCase=true;
                this.isIBGTypeTransaction = false;
                this.ibgDetailsID = "";
              
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
                      
            }
          
        }
        if(this.isBulkPayment){
            /*
             *rowIndex  is used for payment method cash
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
           // this.ShowCheckDetails(this.pmtMethod, rec); 
        }
        // this.hideShowAccountBalance();
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
        if(this.saveAndPrintCheque){
            this.saveAndPrintCheque.disable();
        }
     
        if(this.grid){
            this.grid.disable();
        }
        if(this.NorthForm){
            this.NorthForm.disable();
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
 
    onGridDataChanged: function(){        
        this.updateAmount(getRoundedAmountValue(this.totalAmountWithTDS()));
        //this.updateBalanceAmount();
        //this.updateSouthTemp();
    },
    setcustomdata:function(grid){
        if(this.lineRec){
           WtfGlobal.setCustomFieldValueAfterStoreLoad(grid,this.moduleId,this.lineRec);
        }
    },
    onPaymentMethodChange: function(comboboxObject, newValue, oldValue) {

        var selectedMethod = comboboxObject.value;
        var index = this.pmtStore.findBy(function(rec) {
            var methodId = rec.data['methodid'];
            if (methodId == selectedMethod)
                return true;
        }, this);
        var record = this.pmtStore.getAt(index);
        
        if (record != undefined) {
            this.ispPaymentMethodChange = true;
            if(this.paymentMethodType.Bank==record.data.detailtype){
                this.saveAndPrintCheque.show();
            }else{
                this.saveAndPrintCheque.hide();
            }
            this.paymentMethodTypeNew = record.data.detailtype;
        }
//        var newMethodAccountID=record.data.accountid;
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
       // if (!this.isEdit) {
            var index = this.currencyStore.findBy(function(rec) {
                var currid = rec.data['currencyid'];
                if (currid == accCurr)
                    return true;
                else
                    return false
            }, this);
            
            if (index != -1) {
                this.Currency.setValue(record.data['acccurrency']);
                this.updateExternalCurrencyRateOnCurrencyChange();
               // this.updateTemplateCurr();
            }
       // }
                   
                
             this.updateExternalCurrencyRateOnCurrencyChange();
                            
        
        this.grid.updateConfig(this.ispPaymentMethodChange,this.paymentMethodTypeNew,paymentMethodAccountId,this.pmtStore.getAt(methodAccindx).get("accountname"),record,this.externalcurrencyrate);
            
       
    },
    
    reloadGridOnCurrencyChange:function(){
        Wtf.Ajax.requestEx({
            url: "ACCVendorPaymentNew/reloadGridOnCurrencyChange.do",
            params: {
                newcurrency:this.Currency.getValue(),
                paymentwindowtype:(this.record)?this.record.data.paymentwindowtype:this.paymentType,
                billid:this.record.data.billid,
                date:WtfGlobal.convertToGenericDate(this.creationDate.getValue())
            }
        },this,this.genSuccessResponsereOnCurrencyChange,this.genFailureResponseOnCurrencyChange);
    },
    genSuccessResponsereOnCurrencyChange:function(response,request){
        if(response.success){
            if(response.exchangeRateFound){
                this.loadLineDetails(response.Details)
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


    addExchangeRate:function(){
         var panel = callCurrencyExchangeWindow();
         panel.on("update",function() {             // If exchange rate is added for particular date, again request will be sent to server to fetch exchange rate at that Date (i.e. exchange rate added just now)
             this.reloadGridOnCurrencyChange();     
         }, this);
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.10")], 2);
    },
    onDateChange: function (a, val, oldval) {
        if (this.getNewestTransactionDate(val, oldval)) {
            WtfGlobal.assignDiscountAfterCalculation(this);
        }
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
            var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.creationDate.getValue()).clearTime());
            if (!isTaxShouldBeEnable) {
                this.grid.getStore().removeAll();
                this.grid.addBlankRow();
            }
        }
    },
    getNewestTransactionDate: function (val, oldval) {
        var enteredDate = val;
        var valid = true;
        for (var i = 0; i < this.grid.store.getCount(); i++) {
            var rec = this.grid.store.getAt(i);
            if ((enteredDate.getTime() < new Date(rec.data.invoicedate).getTime())) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlder")], 2);
                this.creationDate.setValue(oldval);
                valid = false;
                break;
            }

        }
        return valid;
    },
    enableSaveButton: function(){
        this.saveBttn.enable();
        if(this.saveAndPrintCheque){
            this.saveAndPrintCheque.enable();
        }
            
        //this.savencreateBttn.enable();
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
   
   
   enableDisablePaymentCurrencyToPaymentMethodCurrencyRate: function(){
       var pmtMethodRecord = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
       var currencyForSelectedPaymentMethod= pmtMethodRecord.data['acccurrency'];
       
       if(currencyForSelectedPaymentMethod == WtfGlobal.getCurrencyID() || !(this.isMultiCurrencyPayment()) || this.readOnly ){
           this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.disable();
       }else {
           this.paymentCurrencyToPaymentMethodCurrencyExchangeRate.enable();
       }
   },


 

totalAmountWithTDS:function(){
    var amount=0;
//    if(Wtf.account.companyAccountPref.countryid ==Wtf.Country.INDIA &&  Wtf.isTDSApplicable){
//        amount=this.grid.getMultiDebitAmount()-this.grid.getMultiTDSAmount()
//    }else{
        amount=this.grid.getMultiDebitAmount()
//    }
    return amount;
}, 



formValidationBeforeSave : function(){
    
  var isValid=this.grid.formValidationBeforeSave();  
     return isValid;
},
printCheque: function (chequeDetail) {
    var text = "";
        
//        if (chequeDetail.isnewlayout) {
            text += '<div style="position: relative;page-break-after:always;">';
//        }
        
        text += '<div style="position: relative; left:' + chequeDetail.dateLeft + 'cm; top:' + chequeDetail.dateTop + 'cm;">' + chequeDetail.date + '</div>';
        text += '<div style="position: relative; left:' + chequeDetail.nameLeft + 'cm; top:' + chequeDetail.nameTop + 'cm;">' + chequeDetail.accountName + '</div>';
        text += '<div style="position: relative; left:' + chequeDetail.amtinwordLeft + 'cm; top:' + chequeDetail.amtinwordTop + 'cm;">' + chequeDetail.amountinword + '</div>';
        text += '<div style="position: relative; left:' + chequeDetail.amtinwordLeftLine2 + 'cm; top:' + chequeDetail.amtinwordTopLine2 + 'cm;">' + chequeDetail.amountinword1 + '</div>';
        text += '<div style="position: relative; left:' + chequeDetail.amtLeft + 'cm; top:' + chequeDetail.amtTop + 'cm;">' + chequeDetail.amount + '</div>';

        /*
         * if Other field option true the this filed will show on check.
         */
        if (chequeDetail.activateExtraFields !== undefined && chequeDetail.activateExtraFields) {
            text += '<div style="position: relative; left:' + chequeDetail.memoLeft + 'cm; top:' + chequeDetail.memoTop + 'cm;">' + chequeDetail.memo + '</div>';
            text += '<div style="position: relative; left:' + chequeDetail.addressLine1Left + 'cm; top:' + chequeDetail.addressLine1Top + 'cm;">' + chequeDetail.addressLine1 + '</div>';
            text += '<div style="position: relative; left:' + chequeDetail.addressLine2Left + 'cm; top:' + chequeDetail.addressLine2Top + 'cm;">' + chequeDetail.addressLine2 + '</div>';
            text += '<div style="position: relative; left:' + chequeDetail.addressLine3Left + 'cm; top:' + chequeDetail.addressLine3Top + 'cm;">' + chequeDetail.addressLine3 + '</div>';
            text += '<div style="position: relative; left:' + chequeDetail.addressLine4Left + 'cm; top:' + chequeDetail.addressLine4Top + 'cm;">' + chequeDetail.addressLine4 + '</div>';
        }
//        if (chequeDetail.isnewlayout) {
            text += '</div>';
//        }
        return  text;
    }
});



