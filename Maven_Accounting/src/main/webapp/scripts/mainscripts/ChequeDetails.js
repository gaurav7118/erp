Wtf.account.ChequeDetails = function(config) {
    this.isEdit = (config.isEdit)?config.isEdit:false;
    this.isCopy = (config.isCopy)?config.isCopy:false;
    this.readOnly = (config.readOnly) ? config.readOnly : false;    //ERP-38828
    this.chequeNumber = config.chequeNumber;
    this.isFromPaymentModule = config.isFromPaymentModule?config.isFromPaymentModule:false;
    this.paymentMethodAccountId=config.paymentMethodAccountId;
    this.chequeSequenceFormatID=config.chequeSequenceFormatID;
    Wtf.account.ChequeDetails.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.account.ChequeDetails, Wtf.form.FormPanel,{
    labelWidth:150,
    cls:"visibleDisabled",
    onRender: function(config) {
        this.createStore();
        this.createFields();
        this.add(this.cheque);
        Wtf.account.ChequeDetails.superclass.onRender.call(this, config);
    },
    
    createStore:function(){
        this.sequenceFormatRec = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'value'},            
        {name: 'accid'}
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatRec),
            url: "ACCCompanyPref/getChequeSequenceFormatStore.do",
            baseParams: {
                paymentMethodAccountId: this.paymentMethodAccountId,
                isAllowNA:true,
                isEdit:(this.isEdit && !this.isCopy),//Only Edit case
                isFromPaymentModule:this.isFromPaymentModule
            }
        });
        this.sequenceFormatStore.on('load',this.setNextNumber, this);
    },
    
    createFields:function(){
        //
        if(this.paymentMethodAccountId!="" && this.paymentMethodAccountId!=undefined){
            this.sequenceFormatStore.load();
        }
        
        this.sequenceFormatCombobox = new Wtf.form.FnComboBox({
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            anchor: '80%',
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
        
        this.checkNo = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.nee.46.Cheque"),  //"Cashier's Check/Check/Reference Number*",
            name:"checkNo",
//            allowBlank:false,
//            maskRe:/^\d+$/,
            maxLength:16,
            vtype : "alphanum",
            allowNegative:false,
            anchor: '80%'
        });
        
        //
        this.bank= new Wtf.form.TextField({
            name:"paymentthrough",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.nee.47"),  //'Bank From Name*',
            anchor: '90%',
            maxLength:50
        });
        
        //
        this.PostDate=new Wtf.form.DateField({
            name:"postdate",
            anchor: '90%',
            allowBlank:false,
            value:new Date(),
            fieldLabel:WtfGlobal.getLocaleText("payment.date.postDate")+"*",
            format:WtfGlobal.getOnlyDateFormat()
        });
        
        //
        this.description =new Wtf.form.TextArea({
            name:"description",
            height:40,
            anchor: '90%',
            fieldLabel:WtfGlobal.getLocaleText("acc.nee.46.Description"),  //"Description",
            maxLength: 255
        });
        
        //
        this.cheque=new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.mp.payMethodDetails"),  //'Cheque Details',
            id:this.id+'cheque',
            bodyStyle:'padding:10px',
            height:100,
            layout:'column',
            defaults:{border:false},
            items:[{
                layout:'form',
                columnWidth:0.34,
                items:[this.sequenceFormatCombobox,this.checkNo]
               },{
                layout:'form',
                columnWidth:0.33,
                items:[this.bank]
               },{
                layout:'form',
                columnWidth:0.32,
                items:[this.PostDate,this.description]
               }]
        });
        
    },
    
    setNextNumber: function(config) {
        if (this.sequenceFormatStore.getCount() > 0) {
            if (this.isEdit && !this.isCopy && !this.paymentMethodAccountChanged) {                       //Edit case
                var index=WtfGlobal.searchRecordIndex(this.sequenceFormatStore,this.chequeSequenceFormatID,"id");
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.chequeSequenceFormatID);
//                    this.sequenceFormatCombobox.disable();
                    this.checkNo.setValue(this.chequeNumber);
//                    this.checkNo.disable();
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                }
                if (!this.readOnly) {   //ERP-38828
                    this.checkNo.enable();
                }
            } else if (this.isCopy != undefined && this.isCopy && !this.paymentMethodAccountChanged) {  // Copy case
                var indexCopyCase=WtfGlobal.searchRecordIndex(this.sequenceFormatStore,this.chequeSequenceFormatID,"id");
                if (indexCopyCase != -1) {
                    this.sequenceFormatCombobox.setValue(this.chequeSequenceFormatID);
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
//                    this.sequenceFormatCombobox.disable();
                    this.checkNo.enable();
                    this.checkNo.setValue("");
                }
            } else {                                                     // Create new case
                this.setSequenceFormatForCreateNewCase();
            }      
        }
    },
    
    setSequenceFormatForCreateNewCase :function(){
        var count = this.sequenceFormatStore.getCount();
        for (var i = 0; i < count; i++) {
            var seqRecord = this.sequenceFormatStore.getAt(i)
            if (seqRecord.json.isdefault === true) {
                this.sequenceFormatCombobox.setValue(seqRecord.data.id)
                break;
            }
        }
        if(this.sequenceFormatCombobox.getValue()!=""){
           this.getNextSequenceNumber(this.sequenceFormatCombobox); 
        } else{
           this.checkNo.setValue("");
           this.checkNo.disable();
        }
    },
    
    getNextSequenceNumber: function(combo, val) {
        /*
         * ERP -39212
         * Condition added for same selection of sequence format of cheque number in edit case otherwise it generate next cheque number
         */
        if (combo.getValue() !== "NA" && (this.chequeSequenceFormatID !== this.sequenceFormatCombobox.getValue() || this.isCopy)) {
            Wtf.Ajax.requestEx({
                url: "ACCVendorPayment/getNextChequeNumber.do",
                params: {
                    bankAccountId: this.paymentMethodAccountId,
                    sequenceformat: combo.getValue(),
                    billdate:(this.billdate!=null && this.billdate!=undefined && this.billdate!="")?WtfGlobal.convertToGenericDate(this.billdate):""
                }
            }, this, function(resp) {
                if (resp.data == "NA") {
                    this.checkNo.reset();
                    this.checkNo.enable();
                } else {
                    this.checkNo.setValue(resp.nextChequeNumber);
                    this.checkNo.enable();
                }
            });
            
          /*
           * ERP -39212
           * In Edit case if user select same sequence format as previous the cheque number should remain same 
           */  
        } else if (this.isEdit && !this.isCopy && (this.chequeSequenceFormatID === this.sequenceFormatCombobox.getValue())) {  
            this.checkNo.setValue(this.chequeNumber);
            this.checkNo.enable();
        } else {
            this.checkNo.reset();
            this.checkNo.enable();
        }
    },
    
    loadChequeSequenceFormatOnMethodAccountChanged:function(changedPaymentAccountID,isPayMethodAccountChanged){
        //this method  get called from payment and receipt module in edit case only, when payment method account get changed on changing payment method
        this.paymentMethodAccountChanged=isPayMethodAccountChanged;
        this.paymentMethodAccountId=changedPaymentAccountID;
        this.sequenceFormatStore.baseParams.paymentMethodAccountId=changedPaymentAccountID;
        this.sequenceFormatCombobox.enable();
        this.sequenceFormatStore.load();
    },
    
    
    GetPaymentFormData:function(gridStore){
        
        // getting bank account id from grid store
        var bankAccountId = "";
        for(var i=0;i<gridStore.getCount();i++){
            var rec=gridStore.getAt(i);
            if(!rec.data['debit'] && rec.data['masterTypeValueOfAccount'] == 3){
                bankAccountId = rec.data['mappedaccountid'];
            }
        }
        
        var data="{}";
        data="{sequenceformat:'" + this.sequenceFormatCombobox.getValue() + "',bankAccountId:'"+bankAccountId+"',chequeno:'"+this.checkNo.getValue()+"',bankname:'"+escape(this.bank.getValue())+"',description:"+"'"+escape(this.description.getValue())+"',payDate:"+"'"+ WtfGlobal.convertToGenericDate(this.PostDate.getValue())+"'"+"}";
        return data;
    },
    
    setValuesOnEditandCopy:function(rec){
        this.sequenceFormatStore.load();
        this.bank.setValue(rec.get('bankName'));
        this.PostDate.setValue(rec.get('chequeDate'));
        this.description.setValue(rec.get('description'));
        if(this.isEdit && !this.isCopy && !this.paymentMethodAccountChanged && rec.get('chequeNumber')!=undefined && rec.get('chequeNumber')!=""){
            this.checkNo.setValue(rec.get('chequeNumber'));
        }
        if (this.isCopy != undefined && this.isCopy && !this.paymentMethodAccountChanged ){
            var indexCopyCase=WtfGlobal.searchRecordIndex(this.sequenceFormatStore,rec.get('chequesequenceformatid'),"id");
            if (indexCopyCase != -1) {
                this.sequenceFormatCombobox.setValue(rec.get('chequesequenceformatid'));
                this.checkNo.setValue(rec.get('chequeNumber'));  
            } else {
                this.sequenceFormatCombobox.setValue("NA");
//                this.sequenceFormatCombobox.disable();
                this.checkNo.enable();
                this.checkNo.setValue("");
            }
        }
    },
    
    setNextChequeNumber:function(bankAccountId,bankName){
        this.bank.setValue(bankName)
    }
});