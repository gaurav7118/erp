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
////
/*this.type
 *0-Cash
 *1-Card
 *2-Bank*/


Wtf.account.PayMethodPanelNew = function(config) {
    this.isReceipt = config.isReceipt;
    this.isSequenceformat = (config.isSequenceformat!=undefined?config.isSequenceformat:false);
    this.isAccount = config.isAccount;
    this.paymentMethodAccountId = config.paymentMethodAccountId;
    this.isEdit=config.isEdit;
    this.isCopyReceipt=config.isCopyReceipt;
    this.chequeSequenceFormatID=config.chequeSequenceFormatID;
    this.paymentAccType = {
        Cash: 0,
        Card: 1,
        Bank: 2
    }
    
    Wtf.account.PayMethodPanelNew.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.account.PayMethodPanelNew, Wtf.form.FormPanel, {
    labelWidth: 120,
    cls: "visibleDisabled",
    onRender: function(config) {
        this.createStore();
        this.createFields(this.type);
        this.add(this.paymentAccTypeSet);
        this.hideFormFields();
        Wtf.account.PayMethodPanelNew.superclass.onRender.call(this, config);
    },hideFormFields:function(){
        this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.receivePayment);
    },hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id)){
                    if(fieldArray.isHidden){
                        Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).hidden = fieldArray.isHidden;
                    }
                    /*
                     * if Field is read only
                     */
                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.helpmodeid+this.id).disabled = fieldArray.isReadOnly;
                    }
                    /*
                     * if Field is user manadatory
                     */
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
    createStore: function() {
        this.bankRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}]);

        this.bankTypeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.bankRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 2
            }
        });
        this.bankTypeStore.load();
        if(this.type == this.paymentAccType.Bank && !this.isReceipt){
            this.sequenceFormatRec = new Wtf.data.Record.create([
            {
                name: 'id'
            },

            {
                name: 'value'
            },            
            {
                name: 'accid'
            }
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
                    isEdit:(this.isEdit && !this.isCopyReceipt),//Only Edit case
                    isFromPaymentModule:true
                }
            });
            this.sequenceFormatStore.on('load',this.setNextNumber, this);
        }
    },
    
    setOtherFieldInfoOnRecordLoad: function() {
        if (this.paymentStatus.getValue() == "Cleared")
            if(this.clearanceDate.getEl()) this.clearanceDate.getEl().up('.x-form-item').setDisplayed(true);
        else {
            if(this.clearanceDate.getEl()) this.clearanceDate.getEl().up('.x-form-item').setDisplayed(false);
        }
    },
    
    setNextNumber: function(config) {
        if (this.sequenceFormatStore.getCount() > 0) {
            if (this.isEdit && !this.isCopyReceipt && !this.paymentMethodAccountChanged) {                       //Edit case
                var index=WtfGlobal.searchRecordIndex(this.sequenceFormatStore,this.chequeSequenceFormatID,"id");
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.chequeSequenceFormatID);
                    this.sequenceFormatCombobox.enable();
                    this.checkNo.enable();
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                    if (!this.isEdit){
                        /*
                         * To disable checkNo field when viewed
                         */
                        this.checkNo.enable();
                    }
                    WtfGlobal.showFormElement(this.checkNo);
                }
            } else if (this.isCopyReceipt != undefined && this.isCopyReceipt && !this.paymentMethodAccountChanged) {  // Copy case
                var indexCopyCase=WtfGlobal.searchRecordIndex(this.sequenceFormatStore,this.chequeSequenceFormatID,"id");
                if (indexCopyCase != -1) {
                    this.sequenceFormatCombobox.setValue(this.chequeSequenceFormatID);
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
//                    WtfGlobal.hideFormElement(this.checkNo);
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
                this.sequenceFormatCombobox.setValue(seqRecord.data.id);
//                WtfGlobal.hideFormElement(this.checkNo);
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
        if (combo.getValue() != "NA") {
//            this.checkNo.disable();
//            WtfGlobal.hideFormElement(this.checkNo);
            Wtf.Ajax.requestEx({
                url: "ACCVendorPayment/getNextChequeNumber.do",
                params: {
                    bankAccountId: this.paymentMethodAccountId,
                    sequenceformat: combo.getValue()
//                    billdate:(this.PostDate!=null && this.PostDate!=undefined && this.PostDate!="")?this.PostDate:""
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
        } else {
            this.checkNo.reset();
            this.checkNo.enable();
            WtfGlobal.showFormElement(this.checkNo);
        }
    },
    setNextChequeNumber: function(bankAccountId) {
        // set Next Cheque Number
        if (Wtf.account.companyAccountPref.showAutoGeneratedChequeNumber) {
            Wtf.Ajax.requestEx({
                url: "ACCVendorPayment/getNextChequeNumber.do",
                params: {
                    bankAccountId: bankAccountId
                }
            }, this,
            function(req, res) {
                var restext = req;
                if (restext.success) {
                    this.checkNo.setValue(restext.nextChequeNumber)
                }
            });
        }
    },
    loadChequeSequenceFormatOnMethodAccountChanged:function(changedPaymentAccountID,paymentMethodAccountChanged){
        //this method  get called from payment and receipt module in edit case only, when payment method account get changed on changing payment method
        this.paymentMethodAccountChanged=paymentMethodAccountChanged;
        this.paymentMethodAccountId=changedPaymentAccountID;
        this.sequenceFormatStore.baseParams.paymentMethodAccountId=changedPaymentAccountID;
        this.sequenceFormatCombobox.setValue("");
        this.sequenceFormatCombobox.enable();
        this.sequenceFormatStore.load();
    },
    
    createFields: function(type) {
        if(type == this.paymentAccType.Bank && !this.isReceipt){//Sequence Format does not required only for payments with bank type payment method
            this.sequenceFormatStore.load();
            this.sequenceFormatCombobox = new Wtf.form.FnComboBox({
                triggerAction: 'all',
                mode: 'local',
                fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
                valueField: 'id',
                displayField: 'value',
                store: this.sequenceFormatStore,
                disabled: (this.isEdit && !this.isCopyReceipt? true : false),
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
        }
        
        this.bank = (!this.isReceipt ? new Wtf.form.TextField({
            name: "paymentthrough",
            fieldLabel: WtfGlobal.getLocaleText("acc.nee.47"), //'Bank From Name*',
            anchor: '90%',
            allowBlank: false,
            id:'paymentthrough'+this.heplmodeid+this.id,
            maxLength: 50
        }) : new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.setupWizard.BankName"), //'Bank Name*',
            name: "paymentthroughid",
            hiddenName: 'paymentthroughid',
            store: this.bankTypeStore,
            anchor: '90%',
//            allowBlank: false,
            listWidth: 188,
            maxHeight:220,
            valueField: 'id',
            id:'paymentthrough'+this.heplmodeid+this.id,
            displayField: 'name',
            mode: 'remote',
            typeAhead: true,        //ERM-66
            minChars: 1,
            triggerAction: 'all',
            forceSelection: true
        }));
        this.bank.on("render", function() {
            this.bank.allowBlank = false;
        }, this);

        if (!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
            this.bank.addNewFn = this.addMaster.createDelegate(this, [2, this.bankTypeStore])

        this.checkNo = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.nee.46.Cheque"), //Cashier's Cheque/Cheque Number:",
            name: 'chequenumber',
            maxLength: 16,
            id:'chequenumber'+this.heplmodeid+this.id,
            vtype: "alphanum",
            disabled:this.isReceipt?false:Wtf.account.companyAccountPref.showAutoGeneratedChequeNumber,
            allowNegative: false,
            anchor: '80%'
        });
        this.checkNo.on('blur',function(){
            var newValue= this.checkNo.getValue().trim();
            this.checkNo.setValue(newValue);
        },this);
        this.PostDate = new Wtf.ServerDateField({
            name: "postdate",
            anchor: '90%',
            allowBlank: false,
            fieldLabel: WtfGlobal.getLocaleText("payment.date.postDate") + "*", // Cheque Date
            format: WtfGlobal.getOnlyDateFormat(),
            id:'postdate'+this.heplmodeid+this.id,
            value: new Date()   //Wtf.serverDate
        });
        this.PostDate.on('change',this.checkForChequeDateValidation,this);
        this.cardNo = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.CardNumber"),
            name: "CardNo",
            maxLength: 16,
            minLength: 16,
            anchor: '90%'
        });

        this.description = new Wtf.form.TextArea({
            name: "description",
            height: 40,
            anchor: '90%',
            id:'refdescription'+this.heplmodeid+this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.nee.46.Description"), //Reference Number/ Description:
            maxLength: 255
        });

        this.expDate = new Wtf.ServerDateField({
            name: "expirydate",
            anchor: '90%',
            format: WtfGlobal.getOnlyDateFormat(),
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ExpiryDate"),
            format: WtfGlobal.getOnlyDateFormat()
        });

        this.clearanceDate = new Wtf.ServerDateField({
            name: "clearancedate",
            anchor: this.isReceipt?'83%':'87%',
            fieldLabel: WtfGlobal.getLocaleText("acc.bankReconcile.clearanceDate") + "*", //"Clearance Date*",
            format: WtfGlobal.getOnlyDateFormat()
        });

        this.clearanceDate.on('render', function() {
            this.clearanceDate.getEl().up('.x-form-item').applyStyles("margin-top:16px;")
            this.clearanceDate.getEl().up('.x-form-item').setDisplayed(false);
        }, this);
        this.clearanceDate.on('change',this.checkForClearanceDateValidation,this);
        this.paymentStatusStore = new Wtf.data.SimpleStore({
            fields: ['statusValue', 'statusName'],
            data: [['Cleared', 'Cleared'], ['Uncleared', 'Uncleared']]
        });

        this.paymentStatus = new Wtf.form.ComboBox({
            store: this.paymentStatusStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.mp.pmtstatus") + "*", //"Payment Status*",
            id:'paymentstatus'+this.heplmodeid+this.id,
            name: 'paymentstatus',
            displayField: 'statusName',
            value: 'Uncleared',
            disabled: this.isAccount,
            editable: false,
            //allowBlank:false,
            anchor: this.isReceipt?"80%":"90.2%",
            valueField: 'statusValue',
            mode: 'local'
            //triggerAction: 'all'
            /*Changes done to show only uncleared value in combobox (Ticket--> ERM-1004)*/
        });

        this.paymentStatus.on('select', function(combo, record, index) {
            if (record.data['statusName'] == "Cleared"){
                if(this.clearanceDate.getEl()) this.clearanceDate.getEl().up('.x-form-item').setDisplayed(true);
                this.clearanceDate.allowBlank=false;
//                this.doLayout();
            } else {
                if(this.clearanceDate.getEl()) this.clearanceDate.getEl().up('.x-form-item').setDisplayed(false);
                this.clearanceDate.reset();
                this.clearanceDate.allowBlank=true;
//                this.doLayout();
            }
        }, this);

        this.paymentStatus.on('change', function() {
            if (this.paymentStatus.getValue() == "Cleared")
                if(this.clearanceDate.getEl()) this.clearanceDate.getEl().up('.x-form-item').setDisplayed(true);
            else {
                if(this.clearanceDate.getEl()) this.clearanceDate.getEl().up('.x-form-item').setDisplayed(false);
            }
        }, this);

        this.nameOnCard = new Wtf.form.TextField({
            name: "nameoncard",
            fieldLabel: WtfGlobal.getLocaleText("acc.field.CardHolderName*"),
            anchor: '90%',
            maxLength: 50
        });
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'typeid', type: 'int'}, 'name'],
            data: [['0', 'Master Card'], ['1', 'Visa']]
        });
        this.cardType = new Wtf.form.TextField({
            name: "cardtype",
            maxLength: 20,
            fieldLabel: "Card Type:<br><span CLASS=\"x-formsmaller-item\">(eg. Master Card)</font></span>",
            anchor: '90%'
        });

        this.refNo = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.nee.45"),
            name: "refno",
            maxLength: 49,
            vtype: "alphanum",
            anchor: '90%',
            allowNegative: false
        });
        if(type == this.paymentAccType.Card) {
            this.paymentAccTypeSet = new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.mp.payMethodDetails"),
                height: 120,
                layout: 'column',
                items: [{
                        layout: 'form',
                        columnWidth: 0.34,
                        border: false,
                        items: [this.refNo, this.cardNo]
                    }, {
                        layout: 'form',
                        columnWidth: 0.32,
                        border: false,
                        items: [this.nameOnCard, this.cardType]
                    }, {
                        layout: 'form',
                        columnWidth: 0.32,
                        border: false,
                        items: [this.expDate]
                    }]
            });
            this.expDate.on('change', this.checkExpDate, this);
        } else if(type == this.paymentAccType.Bank) {
            this.paymentAccTypeSet = new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.mp.payMethodDetails"), //'Payment Method Details',
//                bodyStyle: 'padding:10px',
                height: 120,
                layout: 'column',
                defaults: {border: false},
                items: [{
                        layout: 'form',
                        columnWidth: 0.34,
                        items: this.isReceipt?[this.checkNo, this.paymentStatus]:[this.sequenceFormatCombobox,this.checkNo]
                    }, {
                        layout: 'form',
                        columnWidth: 0.32,
                        items: this.isReceipt?[this.bank, this.clearanceDate]:[this.bank, this.paymentStatus, this.clearanceDate]
                    }, {
                        layout: 'form',
                        columnWidth: 0.32,
                        items: [this.PostDate, this.description]
                    }]
            });
        }
    },

    GetPaymentFormData: function() {
        var bankname = "";
        if (this.type == this.paymentAccType.Bank) {
            var bankindex = this.bankTypeStore.find("id", this.bank.getValue());
            bankname = bankindex < 0 ? "" : this.bankTypeStore.getAt(bankindex).data["name"];
        }
        var data = "{}";
        switch (this.type) {
            case this.paymentAccType.Card:
                data = "{refno:'" + this.refNo.getValue() + "',cardno:'" + this.cardNo.getValue() + "',nameoncard:'" + this.nameOnCard.getValue() + "',cardtype:'" + this.cardType.getValue() + "',expirydate:'" + WtfGlobal.convertToGenericDate(this.expDate.getValue()) + "'}";
                break;
            case this.paymentAccType.Bank:
                data = "{chequeno:'" + this.checkNo.getValue() + "',bankname:'" + (!this.isReceipt ? (escape(this.bank.getValue())) : bankname) + "',bankmasteritemid:'" + (!this.isReceipt ? null : this.bank.getValue()) + "',paymentStatus:'" + this.paymentStatus.getValue() + "',clearanceDate:'" + (this.paymentStatus.getValue() == "Cleared" ? WtfGlobal.convertToGenericDate(this.clearanceDate.getValue()) : null) + "',description:" + "'" + escape(this.description.getValue()) + "',payDate:" + "'" + WtfGlobal.convertToGenericDate(this.PostDate.getValue()) + "'" + "}";
                break;
        }
        return data;
    },
    addMaster: function(id, store) {
        addMasterItemWindow(id);
        Wtf.getCmp('masterconfigurationonly').on('update', function() {
            store.reload();
        }, this);
    },
    checkExpDate: function(obj, nval, oval) {
        if (nval < (new Date()))
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.msgbox.CONFIRMTITLE"), WtfGlobal.getLocaleText("acc.field.CardisexpiredDoyouwishtocontinue"), function(btn) {
                if (btn != "yes") {
                    obj.setValue(oval);
                    return
                }
                ;
            }, this)
    },
    setBankName: function(val) {
        if(this.bank) {
            /*
             * If payment Account is Blank then bank name field will show as "NA"
             */
            if(val==""){
                this.bank.setValue("NA")
            }else{
                this.bank.setValue(val)
            }
        }
    },
    checkForClearanceDateValidation:function(field,newval,oldval){
        if(field.getValue()!='' && this.PostDate.getValue()!=''){
            if(field.getValue().getTime()<this.PostDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mprp.clearanceDateCanNotBeLessThanChequedate")], 2);
                field.setValue(oldval);                    
            }
        }
    },
    checkForChequeDateValidation:function(field,newval,oldval){
        if(field.getValue()!='' && this.clearanceDate.getValue()!=''){
            if(field.getValue().getTime()>this.clearanceDate.getValue().getTime()){
                this.clearanceDate.setValue(newval);                    
            }
        }
    }
});
