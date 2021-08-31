/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Wtf.account.IBGFileForUOBWindow = function(config) {

    this.arrayOfBillIds = config.arrayOfBillIds;
    this.arrayOfBaseAmount = config.arrayOfBaseAmount;
    this.arrayOfInvoiceNumbers = config.arrayOfInvoiceNumbers;
    this.id = config.id;
    this.invoiceListComponent = config.invoiceListComponent;
    this.totalAmount = config.totalAmount;
    Wtf.apply(this,{
        buttons: [
            {
            text: WtfGlobal.getLocaleText("acc.uob.generateIBGFile"),
            scope: this,
            handler:this.generateFile
            },
            {
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler:this.closeForm.createDelegate(this)
            }
        ]
    },config);
    
    Wtf.account.IBGFileForUOBWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.IBGFileForUOBWindow, Wtf.Window, {
//    layout: 'border',
    onRender : function(config) {
        Wtf.account.IBGFileForUOBWindow.superclass.onRender.call(this, config);
        
        this.createFields();
        this.createForm();
        
        this.add(this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            height :300,
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            items:[this.IBGDetailsForm]
        }));
    },
    createFields : function() {
        
             this.totalAmountField = new Wtf.form.FinanceNumberField({
                hiddenName: "totalamount",
                name: "totalamount",
                readOnly : true,
                fieldLabel: WtfGlobal.getLocaleText("acc.uob.totalAmtInSGD"), 
                id: "totalamount" + this.id,
                maxLength: 16,
                value: this.totalAmount,
                anchor: '85%'
            });
            this.pmtRec = new Wtf.data.Record.create([
                {name: 'methodid'},
                {name: 'methodname'},
                {name: 'accountid'},
                {name: 'accountname'},
                {name: 'uobCompanyId'}
            ]);
            
            this.pmtStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.pmtRec),
                url: "ACCPaymentMethods/getPaymentMethods.do",
                baseParams: {
                    mode: 51,
                    onlyIBGAccounts :true,
                    IBGBankType : Wtf.IBGBanks.UOBBank
                }
            });
    
            this.pmtMethod = new Wtf.form.FnComboBox({
                fieldLabel: WtfGlobal.getLocaleText("acc.mp.payMethod"),
                name: "pmtmethod",
                hiddenName:'pmtmethod',
                store: this.pmtStore,
                id: "paymentMethod" + this.id,
                valueField: 'methodid',
                displayField: 'methodname',
                allowBlank: false,
                anchor: '85%',
                triggerAction: 'all',
                typeAhead: true,
                forceSelection: true
            });
            this.pmtStore.load();
            
            this.paymentType = new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mp.payType.qtip") + "'>" + WtfGlobal.getLocaleText("acc.mp.payType") + " </span>",
                name: 'paymentType',
                scope:this,
                readOnly:true,
                anchor: '85%',
                value:'Collection'
            });
            
            this.processingModeStore = new Wtf.data.SimpleStore({
                fields:[{name:"id"},{name:"value"}],
                data:[[1,"None"],[2,"Immediate(Fast)"],[3,"Batch(GIRO)"]]
            });
            this.processingMode = new Wtf.form.ComboBox({
                store: this.processingModeStore,
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.processingMode.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.processingMode") + " </span>",
                name: 'processingMode',
                hiddenName: 'processingMode',
                displayField: 'value',
                valueField: 'id',
                mode: 'local',
                typeAhead: true,
                anchor: '85%',
                value:1,
                triggerAction: 'all',
                forceSelection: true
            });
            this.processingMode.on('change',function(obj){
                if(obj.getValue() == 2){
                    if(this.serviceType.getValue() != 2){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.uob.serviceTypeShouldBeNormal")], 3);
                    }
                    this.serviceType.setValue(2);
                }
            },this);
            
            this.serviceTypeStore = new Wtf.data.SimpleStore({
                fields:[{name:"id"},{name:"value"}],
                data:[[1,"EXPRESS"],[2,"NORMAL"]]
            });
            this.serviceType = new Wtf.form.ComboBox({
                store: this.serviceTypeStore,
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.serviceType.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.serviceType") + "* </span>",
                name: 'serviceType',
                hiddenName: 'serviceType',
                displayField: 'value',
                valueField: 'id',
                mode: 'local',
                anchor: '85%',
                typeAhead: true,
                value:1,
                triggerAction: 'all',
                allowBlank:false,
                forceSelection: true
            });
            this.serviceType.on('change',function(obj){
                if(this.processingMode.getValue() == 2){
                    if(obj.getValue() != 2){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.uob.serviceTypeShouldBeNormal")], 3);
                    }
                    obj.setValue(2);
                }
            },this);
            this.companyId = new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.companyId.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.companyId") + " </span>",
                name: 'companyId',
                hiddenName: 'companyId',
                anchor: '85%',
                maxLength:12,
                scope:this
            });
            this.creationDate = new Wtf.ServerDateField({
                name: "creationDate",
                format: WtfGlobal.getOnlyDateFormat(),
                value: new Date(),
                id: "creationDate" + this.id,
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.assetworkorder.CreationDate.qtip") + "'>" + WtfGlobal.getLocaleText("acc.assetworkorder.CreationDate") + "* </span>",
                anchor: '85%',
                allowBlank: false
            });
            this.creationDate.on('change',function(field,newval,oldval){
                var today = new Date();
                if(field.getValue()!=''){
                    if(newval.getTime()>today.getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.uob.creationDateCanNotBeFutureDate")], 3);                        
                        field.setValue(oldval);
                    }
                }
            },this);
            this.valueDate = new Wtf.ServerDateField({
                name: "valueDate",
                format: WtfGlobal.getOnlyDateFormat(),
                value: new Date(),
                id: "valueDate" + this.id,
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.IBG.ValueDate.qtip") + "'>" + WtfGlobal.getLocaleText("acc.IBG.ValueDate") + "* </span>",
                anchor: '85%',
                allowBlank: false
            });
            this.valueDate.on('change',function(field,newval,oldval){
                var today = new Date();
                var dateToCompare = today.add(Date.DAY,30);
                if(field.getValue()!=''){
                    if(newval.getTime()>dateToCompare.getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.uob.valueDateCanNotBeFutureDate")], 3);                        
                        field.setValue(oldval);
                    }
                }
            },this);
            
            this.bulkCustomerReference = new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.bulkCustomerReference.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.bulkCustomerReference") + "* </span>",
                name: 'companyId',
                hiddenName: 'companyId',
                scope:this,
                maxLength:16,
                anchor: '85%',
                allowBlank : false
            });
            
            this.bulkCustomerReference.on('blur',function(obj){
                obj.setValue(obj.getValue().trim());
            },this);
            this.bulkCustomerReference.on('change',function(obj){
                obj.setValue(obj.getValue().replace(/[\~\`\!\@\#\$\%\^\&\*\_\=\<\>\[\]\{\}\\\|]/g, ""));
            },this);
            /*
             * On Payment Method change companyId get populated from UOBBankDetails
             */
            this.pmtMethod.on('change', function(comboboxObject,newval,oldval){
                var newrec = WtfGlobal.searchRecord(this.pmtStore, newval, "methodid");
                var newaccountid = newrec.data.accountid;
                
                if (oldval != undefined && oldval != "") {
                    var oldrec = WtfGlobal.searchRecord(this.pmtStore, oldval, "methodid");
                    var oldaccountid = oldrec.data.accountid;
                    if (oldaccountid != undefined && oldaccountid != "" && oldaccountid != newaccountid) {
                        this.companyId.setValue(newrec.data.uobCompanyId);
                    }
                } else {
                    this.companyId.setValue(newrec.data.uobCompanyId);
                }
            },this);
    },
    
    
    createForm : function() {
        this.IBGDetailsForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            border: false,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding:20px;",
            labelWidth: 155,
            frame: false,
            fileUpload: true,
            autoScroll:true,
            layout : 'column',
            items: [
                    {
                        layout: 'form',
                        columnWidth: 0.45,
                        border: false,
                        id: this.id + 'LeftColumnform',
                        items: [this.totalAmountField,this.pmtMethod,this.paymentType,this.processingMode,this.serviceType]
                    }, {
                        layout: 'form',
                        columnWidth: 0.1,
                        border: false
                    },{
                        layout: 'form',
                        columnWidth: 0.45,
                        border: false,
                        id: this.id + 'RightColumnform',
                        items: [this.companyId,this.creationDate,this.valueDate,this.bulkCustomerReference]
                    }
            ]
        });
    },
    
    generateFile: function(){
      if(!this.IBGDetailsForm.getForm().isValid()){
          WtfComMsgBox(2,2);
          this.IBGDetailsForm.doLayout();
          return;
      } else {
            var processingMode = this.processingMode.getValue();
            if (processingMode == 2) {
                var amount = 0;
                var invoicesNotEligible = '';
                for (var x = 0; x < this.arrayOfBaseAmount.length; x++) {
                    amount = this.arrayOfBaseAmount[x];
                    if (amount > Wtf.MaxLimitForFastProcessingMode) {
                        invoicesNotEligible = this.arrayOfInvoiceNumbers[x] + ", ";
                    }
                }
                if (invoicesNotEligible != '') {
                    invoicesNotEligible = invoicesNotEligible.substring(0, invoicesNotEligible.length - 2);
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uob.invoiceAmountExceeding") + '</br>' + invoicesNotEligible], 2);
                    return;
                }
            }
            var url = 'ACCCustomerCMN/isCustomerBankAccountPresent.do';
            Wtf.Ajax.requestEx({
                url: url,
                params: {
                    arrayOfBillIds: this.arrayOfBillIds.toString()
                }
            }, this, this.genSuccessResponse, this.genFailureResponse);
      }
    },
    genSuccessResponse : function(response) {
        if (response.success) {
            var processingMode = this.processingMode.getValue();
            if (processingMode == 2) {
                var amount = 0;
                var invoicesNotEligible = '';
                for (var x = 0; x < this.arrayOfBaseAmount.length; x++) {
                    amount = this.arrayOfBaseAmount[x];
                    if (amount > Wtf.MaxLimitForFastProcessingMode) {
                        invoicesNotEligible = this.arrayOfInvoiceNumbers[x] + ", ";
                    }
                }
                if (invoicesNotEligible != '') {
                    invoicesNotEligible = invoicesNotEligible.substring(0, invoicesNotEligible.length - 2);
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uob.invoiceAmountExceeding") + '</br>' + invoicesNotEligible], 2);
                    return;
                }
            }
            var pmtMethodRec = WtfGlobal.searchRecord(this.pmtStore, this.pmtMethod.getValue(), "methodid");
            var accountId = pmtMethodRec.data.accountid;
            var paymentMethodId = this.pmtMethod.getValue();
            var paymentType = this.paymentType.getValue();
            var serviceType = this.serviceType.getValue();
            var companyId = this.companyId.getValue();
            var creationDate = WtfGlobal.convertToGenericDate(this.creationDate.getValue());
            var valueDate = WtfGlobal.convertToGenericDate(this.valueDate.getValue());
            var bulkCustomerReference = this.bulkCustomerReference.getValue();
            var url = 'ACCCombineReports/generateGIORFileForUOBBank.do';
            Wtf.get('downloadframe').dom.src = url + "?accountId=" + accountId + "&paymentMethodId=" + paymentMethodId + "&paymentType=" + paymentType + "&serviceType=" + serviceType + "&processingMode=" + processingMode + "&companyId=" + companyId + "&creationDate=" + creationDate + "&valueDate=" + valueDate + "&arrayOfBillIds=" + this.arrayOfBillIds.toString() + "&bulkCustomerReference=" + bulkCustomerReference;
            this.closeForm();
        } else {
            var msg = WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
            if (response.msg) {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), msg], 2);
        }
    },
    
    genFailureResponse : function(response) {
        var msg = WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg) {
            msg=response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },

    closeForm : function() {
        this.close();
    }
});