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

Wtf.account.IBGBankDetails = function(config) {
    
//=============================================== For Applying Config to the Window ========================================================

    this.record = config.record;
    this.isEdit = config.isEdit;
    this.isView = config.isView? config.isView : false;
    this.isEditCase = config.isEditCase;
    if(this.record){
        var data = (this.record.data != undefined)? this.record.data : this.record;
        this.ibgbankdetailid = data.ibgbankdetailid;
        this.cimbbankdetailid = data.cimbbankdetailid;
        this.uobbankdetailid = data.uobbankdetailid;
        this.ocbcbankdetailid = data.ocbcbankdetailid;
        this.ibgbanktype = data.ibgbanktype;
    }
    Wtf.apply(this,{
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  // 'Save',
            scope: this,
            hidden : this.isView,
            handler:this.saveForm.createDelegate(this)
        }, {
            text: this.isView?WtfGlobal.getLocaleText("acc.msgbox.ok"):WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler:this.closeForm.createDelegate(this)
        }]
    },config);
    
    Wtf.account.IBGBankDetails.superclass.constructor.call(this, config);

//=============================================== For Addding Events to the Window ========================================================

    this.addEvents({
        'update':true,
        'cancel':true
    });
}

Wtf.extend(Wtf.account.IBGBankDetails, Wtf.Window, {
    
//=============================================== For Loading Records in Edit case ========================================================    

    loadRecord : function() {
        var data = (this.record.data != undefined)? this.record.data : this.record;
        
        if(data.ibgbank != undefined && data.ibgbanktype == Wtf.IBGBanks.DBSBank) {            // IF bak is DBS bank
            this.radioIBGbank.setValue(true);
            if(data.bankCode != undefined && data.bankCode != "") {
                this.bankCode.setValue(data.bankCode);
            }
            if(data.branchCode != undefined && data.branchCode != "") {
                this.branchCode.setValue(data.branchCode);
            }
            if(data.accountNumber != undefined && data.accountNumber != "") {
                this.accountNumber.setValue(data.accountNumber);
            }
            if(data.accountName != undefined && data.accountName != "") {
                this.accountName.setValue(data.accountName);
            }
            if(data.sendersCompanyID != undefined && data.sendersCompanyID != "") {
                this.sendersCompanyID.setValue(data.sendersCompanyID);
            }
            if(data.bankDailyLimit != undefined && data.bankDailyLimit != "") {
                this.bankDailyLimit.setValue(data.bankDailyLimit);
            }
            this.radioIBGbank.fireEvent('change',this.radioIBGbank,this);        
        }
        if(data.ibgbank != undefined && data.ibgbanktype == Wtf.IBGBanks.CIMBBank) {           // If bank is CIMB bank
            this.radioCIMBGbank.setValue(true);
            if(data.serviceCode != undefined && data.serviceCode != "") {
                this.serviceCode.setValue(data.serviceCode);
            }
            if(data.bankAccountNumber != undefined && data.bankAccountNumber != "") {
                this.bankAccountNumber.setValue(data.bankAccountNumber);
            }
            if(data.ordererName != undefined && data.ordererName != "") {
                this.ordererName.setValue(data.ordererName);
            }
            if(data.settlementMode != undefined && data.settlementMode != "") {
                this.settlementMode.setValue(data.settlementMode);
            }
            if(data.postingIndicator != undefined && data.postingIndicator != "") {
                this.postingIndicator.setValue(data.postingIndicator);
            }
            this.radioCIMBGbank.fireEvent('change',this.radioCIMBGbank,this);        
        }
        if(data.ibgbank != undefined && data.ibgbanktype == Wtf.IBGBanks.UOBBank) {
            this.radioUOBbank.setValue(true);
            if(data.uobOriginatingBICCode != undefined && data.uobOriginatingBICCode != "") {
                this.UOBOriginatingBICCode.setValue(data.uobOriginatingBICCode);
            }
            if(data.uobCurrencyCode != undefined && data.uobCurrencyCode != "") {
                this.UOBOriginatingAccountNumberCurrency.setValue(data.uobCurrencyCode);
            }
            if(data.uobOriginatingAccountNumber != undefined && data.uobOriginatingAccountNumber != "") {
                this.UOBOriginatingAccountNumber.setValue(data.uobOriginatingAccountNumber);
            }
            if(data.uobOriginatingAccountName != undefined && data.uobOriginatingAccountName != "") {
                this.UOBOriginatingAccountName.setValue(data.uobOriginatingAccountName);
            }
            if(data.uobUltimateOriginatingCustomer != undefined && data.uobUltimateOriginatingCustomer != "") {
                this.UOBUltimateOriginatingCustomer.setValue(data.uobUltimateOriginatingCustomer);
            }
            if(data.uobCompanyId != undefined && data.uobCompanyId != "") {
                this.UOBCompanyId.setValue(data.uobCompanyId);
            }
            this.radioUOBbank.fireEvent('change',this.radioUOBbank,this);
        }
        //ERM-576 - OCBC Giro Format for IBG
        if (data.ibgbank != undefined && data.ibgbanktype == Wtf.IBGBanks.OCBCBank) {
            this.radioOCBCbank.setValue(true);
            if (data.ocbcOriginatingBankCode != undefined && data.ocbcOriginatingBankCode != "") {
                this.OCBCBankCode.setValue(data.ocbcOriginatingBankCode);
            }
            if (data.ocbcAccountNumber != undefined && data.ocbcAccountNumber != "") {
                this.OCBCAccountNumber.setValue(data.ocbcAccountNumber);
            }
            if (data.ocbcReferenceNumber != undefined && data.ocbcReferenceNumber != "") {
                this.OCBCReferenceNumber.setValue(data.ocbcReferenceNumber);
            }
            this.radioOCBCbank.fireEvent('change', this.radioOCBCbank, this);
        }
    },
    
//===================================================== For Rendering Window ===============================================================    

    onRender : function(config) {
        Wtf.account.IBGBankDetails.superclass.onRender.call(this, config);
        
        this.createFields();
        this.createForm();
        
        this.add(this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'border',
            items:[this.bankDetailsForm]
        }));
        
        if(this.isView || this.isEdit) {
            this.loadRecord();
        }
    },
    
    
//=================================================== For Creating Form Fields =============================================================

    createFields : function() {
        
        
        this.ibgBankSetting = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.field.selectIBGbank"),
            id:this.id+'ibgbanksetting',
            autoHeight : true,
            width: 300,
            items:[
            this.radioIBGbank = new Wtf.form.Radio({
                hideLabel:true,
                height:22,
                boxLabel:WtfGlobal.getLocaleText("acc.field.developmentBankOfSingapore"), // "Development Bank Of Singapore",
                id:'ibgbank'+'dbs'+this.id,
                name:'ibgbank',
                labelAlign : 'left',
                inputValue :1,
                labelSeparator:'',
                disabled:this.isView || (this.isEditCase && this.record && this.record.accountHasJedTransaction),
                checked:true
            }),
            this.radioCIMBGbank = new Wtf.form.Radio({
                hideLabel:true,
                height:22,
                boxLabel:WtfGlobal.getLocaleText("acc.name.cimbbank"), // Commerce International Merchant Bankers
                id:'ibgbank'+'cimb'+this.id,
                name:'ibgbank',
                labelAlign : 'left',
                inputValue :2,
                labelSeparator:'',
                disabled:this.isView || (this.isEditCase && this.record && this.record.accountHasJedTransaction),
                checked:false
            }),
            this.radioUOBbank = new Wtf.form.Radio({
                hideLabel:true,
                height:22,
                boxLabel:WtfGlobal.getLocaleText("acc.uob.fullname"), // United Overseas Bank
                id:'ibgbank'+'uob'+this.id,
                name:'ibgbank',
                labelAlign : 'left',
                inputValue :3,
                labelSeparator:'',
                disabled:this.isView || (this.isEditCase && this.record && this.record.accountHasJedTransaction),
                checked:false
            }),
            this.radioOCBCbank = new Wtf.form.Radio({
                hideLabel: true,
                height: 22,
                boxLabel: WtfGlobal.getLocaleText("acc.obbcBank.fullname"), //Oversea-Chinese Banking Corporation
                id: 'ibgbankocbc' + this.id,
                name: 'ibgbank',
                labelAlign: 'left',
                inputValue: 4,
                labelSeparator: '',
                disabled: this.isView || (this.isEditCase && this.record && this.record.accountHasJedTransaction),
                checked: false
            })
            ]
        });
        this.dbsBankFields = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.ibg.bank.ibg.details"),
            id:this.id+'ibgbanksettingdbs',
            autoHeight : true,
            width: 600,
            items:[
            this.bankCode = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.originatingBankNumberOrCode")+"*", // "Originating Bank Number/Code"+"*",
                name: 'bankCode',
                width : 270,
                maskRe: /[0-9.]/,
                maxLength:4,
                allowNegative:false,
                scope:this,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.field.pleaseEnterOriginatingBankNumberOrCode") // "Please enter Originating Bank Number/Code",
            }),
    
            this.branchCode = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.originatingBranchNumberOrCode")+"*", // "Originating Bank Number/Code"+"*",
                name: 'branchCode',
                width : 270,
                maxLength:3,
                maskRe: /[0-9.]/,
                regex:/^[0-9\b]+$/,
                scope:this,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.field.pleaseEnterOriginatingBranchNumberOrCode") // "Please enter Originating Branch Number/Code",
            }),
    
            this.accountNumber = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.originatingAccountNumber")+"*", // "Originating Account Number"+"*",
                name: 'accountNumber',
                width : 270,
                maxLength:11,
                allowNegative:false,
                maskRe: /[0-9.]/,
                regexpregex:/[\d]/,
                scope:this,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.field.pleaseEnterOriginatingAccountNumber") // "Please enter Originating Account Number",
            }),
    
            this.accountName = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.originatingAccountNameOrOriginatorsName")+"*", // "Originating Account Name/Originator's Name"+"*",
                name: 'accountName',
                width : 270,
                maxLength:20,
                scope:this,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.field.pleaseEnterOriginatingAccountNameOrOriginatorsName") // "Please enter Originating Account Name/Originator's Name",
            }),
    
            this.sendersCompanyID = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.sendersCompanyID")+"*", // "Sender's Company Id"+"*",
                name: 'sendersCompanyID',
                width : 270,
                maxLength:8,
                scope:this,
                maskRe:/[A-Za-z_]+/,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.field.pleaseEntersendersCompanyID") // "Please enter Sender's Company Id",
            }),
    
            this.bankDailyLimit = new Wtf.form.NumberField({
                fieldLabel:WtfGlobal.getLocaleText("acc.field.BankDailyLimit")+"*",  // "Bank Daily Limit"+"*",
                allowNegative:false,
                defaultValue:0,
                allowBlank:false,
                maxLength: 11,
                width:270,
                name:'bankDailyLimit',
                emptyText: WtfGlobal.getLocaleText("acc.field.pleaseEnterBankDailyLimit") // "Please enter Bank Daily Limit"
            })
            ]
        });
        this.sendersCompanyID.on('blur',function(){
            this.sendersCompanyID.setValue(this.sendersCompanyID.getValue().trim());
        },this)
        this.accountName.on('blur',function(){
            this.accountName.setValue(this.accountName.getValue().trim());
        },this)
        this.settlementStoreArray = [[1,'Batch'],[2,'Real Time']];
        this.settlementStore = new Wtf.data.SimpleStore({
            fields: [{
                name: 'id'
            }, {
                name: 'type'
            }],
            data: this.settlementStoreArray
        });
        this.postingIndicatorStoreArray = [[1,'Consolidated'],[2,'Individual']];
        this.postingIndicatorStore = new Wtf.data.SimpleStore({
            fields: [{
                name: 'id'
            }, {
                name: 'type'
            }],
            data: this.postingIndicatorStoreArray
        });
        this.cimbBankFields = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.ibg.bank.ibg.details"),
            id:this.id+'ibgbanksettingcimb',
            autoHeight : true,
            hidden:true,
            width: 600,
            items:[
                this.serviceCode = new Wtf.form.NumberField({
                fieldLabel:WtfGlobal.getLocaleText("acc.cimb.serviceCode")+"*",  
                allowNegative:false,
                defaultValue:1,
                allowBlank:false,
                disabled:true,
                maxLength: 1,
                maxValue:3,
                minValue:1,
                width:270,
                name:'serviceCode'
            }),
            this.bankAccountNumber = new Wtf.form.NumberField({
                fieldLabel: WtfGlobal.getLocaleText("acc.cimb.bankaccnumber")+"*", 
                name: 'bankAccountNumber',
                allowNegative:false,
                minValue:1,
                width : 270,
                maxLength:10,
                disabled:true,
                scope:this,
                allowBlank:false
            }),
            this.ordererName = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.cimb.ordererName")+"*", 
                name: 'ordererName',
                width : 270,
                maxLength:140,
                disabled:true,
                scope:this,
                allowBlank:false
//                maskRe: /[a-zA-Z0-9]/
            }),
            this.currencyCode = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.cimb.currencyCode")+"*", 
                name: 'currencyCode',
                width : 270,
                scope:this,
                allowBlank:false,
                disabled:true,
                readOnly:true,
                value:'SGD'
            }),
            this.settlementMode = new Wtf.form.ComboBox({
                store: this.settlementStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.cimb.settlementMode")+"*", 
                name: 'settlementMode',
                hiddenName: 'settlement',
                displayField: 'type',
                valueField: 'id',
                disabled:true,
                mode: 'local',
                typeAhead: true,
                triggerAction: 'all',
                forceSelection: true,
                allowBlank: false
            }),
            this.postingIndicator = new Wtf.form.ComboBox({
                store: this.postingIndicatorStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.cimb.postingIndicator")+"*", 
                name: 'postingIndicator',
                hiddenName: 'postingindicator',
                displayField: 'type',
                valueField: 'id',
                mode: 'local',
                typeAhead: true,
                disabled:true,
                triggerAction: 'all',
                forceSelection: true,
                allowBlank: false
            }),
            ]
        });
        this.ordererName.on('blur',function(obj){
            obj.setValue(obj.getValue().trim());
            
        },this);
        this.ordererName.on('change',function(obj){
            obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\.\,\'\"\;\:\<\>\!\~\`\_]/g, ""));
        },this);        
        
        
        this.UOBBankFields = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.ibg.bank.ibg.details"),
            id:this.id+'ibgbanksettinguob',
            autoHeight : true,
            width: 600,
            hidden:true,
            items:[
            this.UOBOriginatingBICCode = new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.originatingBICCode.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.originatingBICCode") + "* </span>",
                name: 'uobOriginatingBICCode',
                hiddenName: 'uobOriginatingBICCode',
                width : 270,
                maxLength:11,
                disabled:true,
                scope:this,
                value : Wtf.account.companyAccountPref.originatingBICCodeForUOBBank,
                allowBlank:false,
                readOnly : true
            }),
            this.UOBOriginatingAccountNumberCurrency = new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.currencyFilterLable.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.originatingAccountNumberCurrency") + "* </span>",
                name: 'uobCurrencyCode',
                hiddenName: 'uobCurrencyCode',
                width : 270,
                scope:this,
                allowBlank:false,
                disabled:true,
                readOnly:true,
                value:'SGD'
            }),
            this.UOBOriginatingAccountNumber = new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.originatingAccountNumber.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.originatingAccountNumber") + "* </span>",
                name: 'uobOriginatingAccountNumber',
                hiddenName: 'uobOriginatingAccountNumber',
                width : 270,
                maxLength:34,
                scope:this,
                readOnly:this.isView,
                disabled:true,
                allowBlank:false
            }),
    
            this.UOBOriginatingAccountName = new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.originatingAccountName") + "'>" + WtfGlobal.getLocaleText("acc.uob.originatingAccountName") + "* </span>",
                name: 'uobOriginatingAccountName',
                hiddenName: 'uobOriginatingAccountName',
                width : 270,
                maxLength:140,
                disabled:true,
                scope:this,
                readOnly:this.isView,
                allowBlank:false                
            }),
            this.UOBUltimateOriginatingCustomer = new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.ultimateOriginatingCustomer.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.ultimateOriginatingCustomer") + " </span>",
                name: 'uobUltimateOriginatingCustomer',
                hiddenName: 'uobUltimateOriginatingCustomer',
                width : 270,
                maxLength:140,
                disabled:true,
                scope:this,
                readOnly:this.isView,
                allowBlank:true                
            }),
            this.UOBCompanyId = new Wtf.form.TextField({
                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.companyId.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.companyId") + " </span>",
                name: 'uobCompanyId',
                hiddenName: 'uobCompanyId',
                width : 270,
                maxLength: 12,
                scope: this,
                readOnly:this.isView
            })
            ]
        });
        
        this.UOBOriginatingBICCode.on('blur',function(obj){
            obj.setValue(obj.getValue().trim());
        },this);
        this.UOBOriginatingAccountNumberCurrency.on('blur',function(obj){
            obj.setValue(obj.getValue().trim());
        },this);
        this.UOBOriginatingAccountNumber.on('blur',function(obj){
            obj.setValue(obj.getValue().trim());
        },this);
        this.UOBOriginatingAccountName.on('blur',function(obj){
            obj.setValue(obj.getValue().trim());
        },this);
        this.UOBUltimateOriginatingCustomer.on('blur',function(obj){
            obj.setValue(obj.getValue().trim());
        },this);
        this.UOBCompanyId.on('blur',function(obj){
            obj.setValue(obj.getValue().trim());
        },this);
        
        this.OCBCBankFields = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.ibg.bank.ibg.details"),
            id: this.id + 'ibgbanksettingocbc',
            autoHeight: true,
            width: 600,
            hidden: true,
            items: [
                this.OCBCBankCode = new Wtf.form.TextField({
                    fieldLabel: WtfGlobal.getLocaleText("acc.ocbcBank.originating.bank.code") + "*", //Originating Bank Code
                    name: 'ocbcOriginatingBankCode',
                    hiddenName: 'ocbcOriginatingBankCode',
                    width: 270,
                    maxLength: 11,
                    scope: this,
                    allowBlank: false,
                    readOnly : true,
                    disabled:true,
                    value : Wtf.account.companyAccountPref.originatingBankCodeForOCBCBank
                }),
                this.OCBCAccountNumber = new Wtf.form.TextField({
                    fieldLabel: WtfGlobal.getLocaleText("acc.ocbcBank.accountNumber") + "*", //Your Account Number
                    emptyText: WtfGlobal.getLocaleText("acc.ocbcBank.accountNumber.emptyText"), //Enter your OCBC Account Number
                    name: 'ocbcAccountNumber',
                    hiddenName: 'ocbcAccountNumber',
                    width: 270,
                    maxLength: 34,
                    scope: this,
                    allowBlank: false,
                    disabled:true
                }),
                this.OCBCReferenceNumber = new Wtf.form.TextField({
                    fieldLabel: WtfGlobal.getLocaleText("acc.ocbcBank.referenceNumber"), //Your Reference Number
                    emptyText: WtfGlobal.getLocaleText("acc.ocbcBank.referenceNumber.emptyText"), //Enter your Reference Number
                    name: 'ocbcReferenceNumber',
                    hiddenName: 'ocbcReferenceNumber',
                    width: 270,
                    maxLength: 16,
                    scope: this,
                    disabled:true
                }),
            ]
        });
        
        this.OCBCAccountNumber.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        this.OCBCReferenceNumber.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        this.OCBCAccountNumber.on('change', function (obj) {
            obj.setValue(obj.getValue().replace(/[\-\'\;\/\.\,\(\)\+\"\:\?\~\`\!\@\#\$\%\^\&\*\_\=\<\>\[\]\{\}\\\|]/g, ""));
        }, this);
        
        // Setting initial data
        this.radioIBGbank.on('change',this.onBankChange,this);
        this.radioCIMBGbank.on('change',this.onBankChange,this);
        this.radioUOBbank.on('change',this.onBankChange,this);
        this.radioOCBCbank.on('change',this.onBankChange,this);
        if(!this.isEdit){
            this.SetDefaultValues();
        }
    },
    
//======================================================= For Creating Form =================================================================    
    
    createForm : function() {
        this.bankDetailsForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            method: 'POST',
            border: false,
            region: 'center',
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding:20px;",
            labelWidth: 270,
            frame: false,
            fileUpload: true,
            autoScroll:true,
            items: [
                this.ibgBankSetting,
                this.dbsBankFields,
                this.cimbBankFields,
                this.UOBBankFields,
                this.OCBCBankFields
            ]
        });
    },
    
//======================================================= For Saving Data of Form =================================================================        
    
    saveForm : function() {
        var flag1 = this.bankDetailsForm.getForm().isValid();        
        if(!flag1) {
            WtfComMsgBox(2,2);
        } else {
            if(this.radioUOBbank.getValue()){
                if(this.UOBOriginatingAccountName.getValue() == this.UOBUltimateOriginatingCustomer.getValue()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.uob.coaFieldsShouldHaveDifferentValues")],2);
                    return;
                }
            }
            if(this.isView) {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.je.msg1"),
                    buttons: Wtf.MessageBox.YESNO,
                    fn:function(btn){
                        if(btn!="yes")return;
                        this.saveFormData();
                    },
                    scope:this,
                    icon: Wtf.MessageBox.QUESTION
                });
            } else {
                this.saveFormData();
            }
        }
    },
    
    saveFormData : function() {
        if(this.isView) {
            var rec = this.bankDetailsForm.getForm().getValues();
            rec.ibgbank = (this.radioIBGbank.getValue())? 1 : 2; // '1' for 'Development Bank Of Singapore'
            rec.bankCode = this.bankCode.getValue();
            rec.branchCode = this.branchCode.getValue();
            rec.accountNumber = this.accountNumber.getValue();
            rec.accountName = this.accountName.getValue();
            rec.sendersCompanyID = this.sendersCompanyID.getValue();
            rec.bankDailyLimit = this.bankDailyLimit.getValue();
            rec.ibgbankdetailid = this.record.data.ibgbankdetailid;
            rec.cimbbankdetailid = this.record.data.cimbbankdetailid;
            rec.AccountId = this.record.data.accid;
            
            Wtf.Ajax.requestEx({
                url:this.getURLToSaveData(),
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
            
        } else {
            this.ibgBankDetail = this.bankDetailsForm.getForm().getValues();
            this.ibgBankDetail.ibgbank = (this.radioIBGbank.getValue())? 1 : 0; // '1' for 'Development Bank Of Singapore'
            this.ibgBankDetail.ibgbanktype = this.getBankType();
            this.ibgBankDetail.settlementMode = this.settlementMode.getValue();
            this.ibgBankDetail.postingIndicator = this.postingIndicator.getValue();
            
            this.ibgBankDetail.cimbbankdetailid = this.cimbbankdetailid;
            this.ibgBankDetail.ibgbankdetailid = this.ibgbankdetailid;
            this.ibgBankDetail.uobbankdetailid = this.uobbankdetailid;
            this.ibgBankDetail.ocbcbankdetailid = this.ocbcbankdetailid;
            
            if(this.radioIBGbank.getValue()){
                this.ibgbanktype = Wtf.IBGBanks.DBSBank;
                this.cimbbankdetailid = "";
                this.uobbankdetailid = "";
                this.ocbcbankdetailid = "";
            } else if(this.radioCIMBGbank.getValue()){
                this.ibgbanktype = Wtf.IBGBanks.CIMBBank;
                this.ibgbankdetailid = "";
                this.uobbankdetailid = "";
                this.ocbcbankdetailid = "";
            } else if(this.radioUOBbank.getValue()){
                this.ibgbanktype = Wtf.IBGBanks.UOBBank;
                this.ibgbankdetailid = "";
                this.cimbbankdetailid = "";
                this.ocbcbankdetailid = "";
            } else if (this.radioOCBCbank.getValue()) {
                this.ibgbanktype = Wtf.IBGBanks.OCBCBank;
                this.ibgbankdetailid = "";
                this.cimbbankdetailid = "";
                this.uobbankdetailid = "";
            }
            this.fireEvent("update",this);
            this.close();
        }
    },
    
    genSuccessResponse : function(response) {
        this.fireEvent("update",this);
        this.close();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.ibgBankDetail"),WtfGlobal.getLocaleText("acc.msg.ibgBankDetailHasBeenSavedSuccessfully")],response.success*2+1);
    },
    
    genFailureResponse : function(response) {
        var msg = WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg) {
            msg=response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },

//========================================================== For Closing Form ===================================================================

    closeForm : function() {
        this.fireEvent('cancel',this);
        this.close();
    },
    onBankChange:function(radio,value){
        if(radio.inputValue == Wtf.IBGBanks.DBSBank){           
            this.enableFieldsetFileds(this.dbsBankFields);
            this.dbsBankFields.show();
            this.resetAndDisableFieldsetComponents(this.cimbBankFields);
            this.resetAndDisableFieldsetComponents(this.UOBBankFields);
            this.resetAndDisableFieldsetComponents(this.OCBCBankFields);
            this.cimbBankFields.hide();
            this.UOBBankFields.hide();
            this.OCBCBankFields.hide();
        } else if(radio.inputValue == Wtf.IBGBanks.CIMBBank){ 
            this.enableFieldsetFileds(this.cimbBankFields);
            this.cimbBankFields.show();
            this.resetAndDisableFieldsetComponents(this.dbsBankFields);
            this.resetAndDisableFieldsetComponents(this.UOBBankFields);
            this.resetAndDisableFieldsetComponents(this.OCBCBankFields);
            this.dbsBankFields.hide();
            this.UOBBankFields.hide();
            this.OCBCBankFields.hide();
        } else if(radio.inputValue == Wtf.IBGBanks.UOBBank){
            this.enableFieldsetFileds(this.UOBBankFields);
            this.UOBBankFields.show();
            this.resetAndDisableFieldsetComponents(this.dbsBankFields);
            this.resetAndDisableFieldsetComponents(this.cimbBankFields);
            this.resetAndDisableFieldsetComponents(this.OCBCBankFields);
            this.dbsBankFields.hide();
            this.cimbBankFields.hide();
            this.OCBCBankFields.hide();
        } else if(radio.inputValue == Wtf.IBGBanks.OCBCBank){
            this.enableFieldsetFileds(this.OCBCBankFields);
            this.OCBCBankFields.show();
            this.resetAndDisableFieldsetComponents(this.dbsBankFields);
            this.resetAndDisableFieldsetComponents(this.cimbBankFields);
            this.resetAndDisableFieldsetComponents(this.UOBBankFields);
            this.dbsBankFields.hide();
            this.cimbBankFields.hide();
            this.UOBBankFields.hide();
        }
    },
    resetAndDisableFieldsetComponents:function(fieldSet){
        for(var x=0;x<fieldSet.items.length;x++){
//            fieldSet.items.itemAt(x).reset();
            fieldSet.items.itemAt(x).disable();
        }
    },
    enableFieldsetFileds :function(fieldSet){
        for(var x=0;x<fieldSet.items.length;x++){
            fieldSet.items.itemAt(x).enable();
        }
    },
    SetDefaultValues: function(){
        this.settlementMode.setValue(1);
        this.postingIndicator.setValue(1);
    },
    getURLToSaveData: function(){
        var url = "ACCAccount/saveIBGBankDetails.do";
        if(this.radioIBGbank.getValue()){
            url = "ACCAccount/saveIBGBankDetails.do";
        } else if(this.radioCIMBGbank.getValue()){
            url = "ACCAccount/saveCIMBBankDetails.do";
        } else if(this.radioUOBbank.getValue()){
            url = "ACCAccount/saveUOBBankDetails.do";
        } else if(this.radioOCBCbank.getValue()){
            url = "ACCAccount/saveOCBCBankDetails.do";
        }
        return url;
    },
    getBankType : function(){
        var val =0;
        
        if(this.radioIBGbank.getValue()){
            val = Wtf.IBGBanks.DBSBank;
        }else if(this.radioCIMBGbank.getValue()){
            val = Wtf.IBGBanks.CIMBBank;
        } else if(this.radioUOBbank.getValue()){
            val = Wtf.IBGBanks.UOBBank;
        } else if(this.radioOCBCbank.getValue()){
            val = Wtf.IBGBanks.OCBCBank;
        }
        return val;
    }
});