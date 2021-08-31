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
Wtf.account.Disbursement = function(config){
    this.moduleid=config.moduleid;
    this.externalcurrencyrate=0;
    this.oldval="";
    this.val="";
    this.loanRuleType="";
    this.loanRuleTypeValue="";
    this.isEdit=(config.isEdit)?config.isEdit:false;
    this.record=config.record;
    this.readOnly=config.readOnly;
    this.dataLoaded=false;
    this.loanrefno="";
    this.disbursementid="";
    Wtf.apply(this,config);
    Wtf.account.Disbursement.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.Disbursement, Wtf.Panel, {
    loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            var data=this.record.data;
            this.disbursementForm.getForm().loadRecord(this.record);
//            this.custmerCmb.setValue(data.customerid); 
//            this.leaseStatus.setValue(data.leasestatus);
            this.disbursementid=data.id;
            this.loanRuleTypeValue=data.loanRuleTypeValue;
            this.loanRuleType=data.loanRuleType;
            this.Currency.store.on('load',function(){
                this.Currency.setValue(data.currencyid);
            },this);
            this.suretyCmb.store.on('load',function(){
                this.suretyCmb.setValue(data.surety);
            },this);
            this.disburseAccount.store.on('load',function(){
                this.disburseAccount.setValue(data.disburseAccount);
            },this);
            this.loanTypeCombo.store.on('load',function(){
                this.loanTypeCombo.setValue(data.loanType);
            },this);
            this.loancategoryCombo.store.on('load',function(){
                this.loancategoryCombo.setValue(data.loancategory);
            },this);
//            Wtf.Ajax.requestEx({
//                url:"ACCContract/getContractFiles.do",
//                params:{
//                    loanrefno:data.cid
//                }
//            }, this,this.onSuccessResponse); 
            this.dataLoaded=true;

        }
    },
    
    onSuccessResponse:function(response, request){
        
        this.attachedFilesStr=""
        var fileStore=response.data;
        for(var i=0;i<response.data.length;i++){
            var rec=fileStore[i];
            if(i==0){
                this.attachedFilesStr=rec.id;
            }else{
                this.attachedFilesStr+=","+rec.id;
            }
        }
                
    },
    onRender: function(config){
        Wtf.account.Disbursement.superclass.onRender.call(this, config);
        this.createStore();
        this.createFields();
        this.createForm();     
        
        this.saveBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            scope: this,
            hidden:this.readOnly,
            iconCls :getButtonIconCls(Wtf.etype.save),
            handler: function(){
                this.saveOnlyFlag = 1;
                this.save();
            }
        });
        
        this.attachFilesBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.Lease.addAttach"),
            scope:this,
            hidden:this.readOnly,
            handler:this.showContractAttachWindow,
            iconCls :getButtonIconCls(Wtf.etype.save)
        });
        
        this.btnArr=[];
        this.btnArr.push(this.saveBttn); 
        if(!this.isEdit){
            this.savencreateBttn=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
                tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
                scope: this,
                hidden:this.readOnly,
                handler: function(){
                    this.saveOnlyFlag = 2;
                    this.save();
                },
                iconCls: 'pwnd save'
            });
            this.btnArr.push(this.savencreateBttn);  
        }
        this.btnArr.push(this.attachFilesBttn);  

        
        this.newPanel=new Wtf.Panel({
            autoScroll:true,
            bodyStyle:' background: none repeat scroll 0 0 #DFE8F6;',
            region : 'center',
            items:[this.disbursementForm] ,
            bbar:this.btnArr

        });
        this.newPanel.on("resize",function(){
            this.newPanel.doLayout();
        },this);
        
       
//        Wtf.Ajax.requestEx({
//            url:ajxUrl,
//            params:params
//        }, this, this.successCallback, this.failureCallback);
        
        this.add(this.newPanel);
       
    //        WtfComMsgBox(29,4,true);
    },
    
//    successCallback:function(){
//        if(this.isEdit)
//            this.loadRecord();
//    },
//    
//    failureCallback:function(){
//        
//    },
    
    createStore:function(){
      
        
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        
        this.personRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }, {
            name: 'acccode'
        },{
            name: 'taxId'
        },{
            name: 'crmAccountId'
        }
        ]);
         
        this.salesAccRec = new Wtf.data.Record.create ([{
            name: 'accid'
        },{
            name: 'acccode'
        },{
            name: 'accname'
        }]);
        
         this.salesAccStore=new Wtf.data.Store({
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:false,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true
                //nature:[3]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });
        this.salesAccStore.load();
        this.customerAccStore = new Wtf.data.Store({
            url: "ACCCustomer/getCustomersForCombo.do",
            baseParams: {
                mode: 2,
                group: 10,
                deleted: false,
                nondeleted: true,
                common: '1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad: false
            }, this.personRec)
        });
        this.customerAccStore.load();
        this.customerAccStore.on('load',function(){
            if(this.isEdit){
                this.loadRecord();
            }
        },this);
        this.suretyRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }, {
            name: 'acccode'
        },{
            name: 'taxId'
        },{
            name: 'crmAccountId'
        }
        ]);
    
        this.suretyAccStore = new Wtf.data.Store({
            url: "ACCCustomer/getCustomersForCombo.do",
            baseParams: {
                mode: 2,
                group: 10,
                deleted: false,
                nondeleted: true,
                common: '1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad: false
            }, this.suretyRec)
        });
        this.suretyAccStore.load();
          
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
                mode:"autoloanrefnumber",
                isEdit:this.isEdit
            }
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
        this.currencyRec = new Wtf.data.Record.create([
        {
            name: 'currencyid',
            mapping:'tocurrencyid'
        },

        {
            name: 'symbol'
        },

        {
            name: 'currencyname',
            mapping:'tocurrency'
        },

        {
            name: 'exchangerate'
        },

        {
            name: 'htmlcode'
        }
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
        });
         
        this.currencyStore.load();
        this.currencyStoreCMB = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
        });
         
        this.currencyStoreCMB.load();
        

        
        this.loantermStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','Month'],['2','Year'],['3','Weekly'],['4','Daily'],['5','Bi-Weekly']]   //['1','Day'],['2','Week'],
        });
        this.installmentIntervalStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','1'],['2','2'],['3','3'],['4','4'],['5','5'],['6','6'],['7','7'],['8','8'],['9','9'],['10','10'],['11','11'],['12','12']]   //['1','Day'],['2','Week'],
        });
        this.installmentTypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','Decline']]  
        });
        this.loanCategoryStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','Secured']]  
        });
        this.sheduleTypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','Fixed'],['2','Contract']]
        });
    },
    
    createFields:function(){
      

       
        chkloantypeload();
        chkloanCategoryLoad();
        this.loanTypeCombo= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            //            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            id:"loantype"+this.id,
            store:Wtf.LoanTypeStore,
            anchor:'75%',
            forceSelection: true,
            fieldLabel:  WtfGlobal.getLocaleText("acc.masterConfig.32")+"*",
            emptyText: WtfGlobal.getLocaleText("acc.loan.Selectloantype"),
            name: 'loanType' ,
            hiddenName: 'loanType',
            addNewFn:this.loanType.createDelegate(this),
            allowBlank: false            
        });
        this.loancategoryCombo= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            id:"loancategory"+this.id,
            store:Wtf.LoanCategoryStore,
            addNoneRecord: true,
            anchor:'75%',
            addNewFn:this.loanCategory.createDelegate(this),
            forceSelection: true,
            emptyText: WtfGlobal.getLocaleText("acc.loan.Selectloancategory"),
            fieldLabel:  WtfGlobal.getLocaleText("acc.loan.disbursementtab.loancategory"),
            name: 'loancategory' ,
            hiddenName: 'loancategory'              
        });
        this.disburseAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:'Disburse Account'+"*",                   //WtfGlobal.getLocaleText("acc.product.salesRevenueRecognitionAccount"),
            store:this.salesAccStore,
            name:'disburseAccount',
            anchor:'75%',
            hiddenName:'disburseAccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false
        });
        this.scheduleTypeCombo= new Wtf.form.FnComboBox({   
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            id:"scheduletype"+this.id,
            store:this.sheduleTypeStore,
            addNoneRecord: true,
            anchor:'75%',
            forceSelection: true,
            fieldLabel:  WtfGlobal.getLocaleText("acc.loan.disbursementtab.scheduletype"),
            name: 'scheduletype' ,
            hiddenName: 'scheduletype'            
        });
//        this.installmentIntervalCombo= new Wtf.form.FnComboBox({
//            triggerAction:'all',
//            mode: 'local',
//            //            selectOnFocus:true,
//            valueField:'id',
//            displayField:'name',
//            id:"installmentinterval"+this.id,
//            store:this.installmentIntervalStore,
//            addNoneRecord: true,
//            anchor:'75%',
//            //width : 240,
//            //            typeAhead: true,
//            forceSelection: true,
//            fieldLabel:  WtfGlobal.getLocaleText("acc.loan.disbursementtab.installmentInterval"),
////            emptyText: WtfGlobal.getLocaleText("acc.loan.Selectloantype"),
//            name: 'installmentinterval' ,
//            hiddenName: 'installmentinterval'              
//        });

          this.installmentInterval=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            fieldLabel:  WtfGlobal.getLocaleText("acc.loan.disbursementtab.installmentInterval"),
            maxLength: 10,
            anchor:'75%',
//            allowBlank: false,
            disabled:true,
            hiddenName:'installmentinterval',
            name:'installmentinterval'
            
        });
        
        this.installmentTypeCombo= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            //            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            id:"installmenttype"+this.id,
            store:this.installmentTypeStore,
            addNoneRecord: true,
            anchor:'75%',
            //width : 240,
            //            typeAhead: true,
            forceSelection: true,
            fieldLabel:  WtfGlobal.getLocaleText("acc.loan.disbursementtab.installmentType"),
//            emptyText: WtfGlobal.getLocaleText("acc.loan.Selectloantype"),
            name: 'installmenttype' ,
            hiddenName: 'installmenttype'            
        });
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            valueField:'id',
            displayField:'value',
            store:this.sequenceFormatStore,
            disabled:(this.isEdit ?true:false),  
            anchor:'75%',
            typeAhead: true,
            forceSelection: true,
            name:'sequenceformat',
            hiddenName:'sequenceformat',
            listeners:{
                'select':{
                    fn:this.getNextSequenceNumber,
                    scope:this
                }
            }
            
        });
        
        this.loanrefno=new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.loan.disbursementtab.loanrefno")+"*",//'Disbursement ID*',
            disabled:(this.isEdit )?true:false,
            name: 'loanrefno',
            hiddenName:'loanrefno',
            anchor:'75%',
            allowBlank:false,
            maxLength:50
        });       

        this.custmerCmb = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.cust")+"*",
            hiddenName: 'customerid',
            name: 'customerid',
            id: "customer" + this.id,
            store: this.customerAccStore,
            valueField: 'accid',
            displayField: 'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            allowBlank: false,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            mode: 'local',
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus: true,
            anchor:'75%',
            triggerAction: 'all',
            scope: this,
            width:100,
            listeners:{
                'select':{
                    fn:this.updateData,
                    scope:this    
                }
            }
        });
        this.suretyCmb = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.loan.disbursementtab.surety")+"*",
            hiddenName: 'surety',
            name: 'surety',
            id: "surety" + this.id,
            store: this.suretyAccStore,
            valueField: 'accid',
            displayField: 'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            allowBlank: false,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            mode: 'local',
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus: true,
            anchor:'75%',
            triggerAction: 'all',
            scope: this,
            width:100
        //             listeners:{
        //                'select':{
        //                    fn:this.updateData,
        //                    scope:this    
        //                }
        //            }
        });
        this.Currency= new Wtf.form.FnComboBox({  
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur")+"*",  //'Currency',
            hiddenName:'currencyid',
            name: 'currencyid',
            id:"currency"+this.heplmodeid+this.id,
            anchor: '75%',
            store:this.currencyStoreCMB,
            valueField:'currencyid',
            allowBlank : false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true,
            listeners:{
                'select':{
                    fn:this.updateFormCurrency,
                    scope:this
                }
            }
        });

       
        this.Currency.on('change', function(){
            var customer="",currency="";
            if(this.custmerCmb.getValue() != undefined && this.custmerCmb.getValue() != ""){
                customer= this.custmerCmb.getValue();
            }    
            if(this.Currency.getValue() != undefined && this.Currency.getValue() != ""){
                currency= this.Currency.getValue();        
            } 
            this.loanamount.setValue("");
            this.custmerCmb.setValue(customer);
            this.Currency.setValue(currency);
            this.applyCurrencySymbol();
        }, this);
       
        //        this.Name.on('select',this.setSalesPerson,this)
            
        this.attachment=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.attachment"),  //'File Attachment
            inputType:'file',
            hidden:true,
            hideLabel:true,
            name:'fileattachment',
            maxLength:100,
            anchor:'75%',
            scope:this,
            allowBlank:true
        });     
        
        this.loanTermType= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            anchor:'50%',
            hideLabel:true,
            displayField:'name',
            store:this.loantermStore,
            typeAhead: true,
            forceSelection: true,
            allowBlank:false,
            name:'loanTermType',
            hiddenName:'loanTermType'
        });
         
        
        this.termValue=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            allowBlank:false,
            maxLength: 10,
            fieldLabel:WtfGlobal.getLocaleText("acc.loan.disbursementtab.loanterm")+"*",  //'Loan term',
            anchor: '95%',
            name:'termValue',
            hiddenName:'termValue'
        });
        
        if(!this.isEdit){
             this.termValue.setValue(1);
            this.loanTermType.setValue(2);
            this.installmentInterval.setValue(12);
        }
        
        this.termValue.on('blur',this.setInstallmentInterval,this);
        this.loanTermType.on('blur',this.setInstallmentInterval,this);
        
         this.applicationdate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.loan.disbursementtab.Applicationdate"),
            name: 'applicationdate',
            hiddenName:'applicationdate',
            format : WtfGlobal.getOnlyDateFormat(),
            allowBlank: false,
            maxLength: 255,
            anchor:'75%',
            scope: this
        });
        this.applicationdate.setValue(new Date());
        
        this.approveddate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.loan.disbursementtab.approveddate")+"*",
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'approveddate',
            hiddenName:'approveddate',
            allowBlank: false,
            anchor:'75%'
        });
        this.approveddate.setValue(new Date());
        
        this.disbursementdate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.loan.disbursementtab.disbursementdate")+"*",
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'disbursementdate',
            hiddenName:'disbursementdate',
            allowBlank: false,
            anchor:'75%'
        });
        this.disbursementdate.setValue(new Date());
        
        this.firstpaymentdate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.loan.disbursementtab.firstpaymentdate")+"*",
            name: 'firstpaymentdate',
            hiddenName:'firstpaymentdate',
            format : WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            allowBlank: false,
            anchor:'75%',
            scope: this
        });
        
        
        
        this.applicationdate.on('blur',function(){
            if(this.approveddate.getValue()!= undefined && this.approveddate.getValue() > this.applicationdate.getValue()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.applicationdatebeforeappieddate")], 2);
                this.applicationdate.reset();
                return false;
            }
        },this)
        this.approveddate.on('blur',function(){
            if(this.applicationdate.getValue()!= undefined && this.applicationdate.getValue() > this.approveddate.getValue()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.approveddateafterapplication")], 2);
                this.approveddate.reset();
                return false;
            }
        },this)
        this.disbursementdate.on('blur',function(){
            if(this.applicationdate.getValue()!= undefined && this.applicationdate.getValue() > this.disbursementdate.getValue()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.disbursementafterapproved")], 2);
                this.disbursementdate.reset();
                return false;
            }
        },this)
        this.firstpaymentdate.on('blur',function(){
            if(this.disbursementdate.getValue()!= undefined && this.disbursementdate.getValue() > this.firstpaymentdate.getValue()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.firstpaymentafterdisbursement")], 2);
                this.firstpaymentdate.reset();
                return false;
            }
        },this)
        
        this.loanrate=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            fieldLabel: WtfGlobal.getLocaleText("acc.loan.disbursementtab.loanrate")+"*",
            maxLength: 10,
            anchor:'75%',
            allowBlank: false,
            //            hideLabel:true,
            hiddenName:'loanrate',
            name:'loanrate'
            
        });
        this.loanamount=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            fieldLabel: WtfGlobal.getLocaleText("acc.loan.disbursementtab.loanamount")+"*",
            maxLength: 10,
            allowBlank: false,
            anchor:'75%',
            //            hideLabel:true,
            hiddenName:'loanamount',
            name:'loanamount'
        });
        
        this.percentloanfee = new Wtf.form.NumberField({
            allowNegative: false,
            defaultValue: 10,
            allowDecimals :true,
            fieldLabel: WtfGlobal.getLocaleText("acc.loan.disbursementtab.loanfee")+"*",
            allowBlank: false,
            maxLength: 3,
            maxValue:100,
            anchor: '95%',
            name: 'percentloanfee'
        });
        if (!this.isEdit) {
            this.percentloanfee.setValue(10);
        }
        this.loanfee=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            allowDecimals :true,
            fieldLabel: WtfGlobal.getLocaleText("acc.loan.disbursementtab.loanfee")+"*",
            maxLength: 10,
            allowBlank: false,
            anchor:'50%',
            hideLabel:true,
            hiddenName:'loanfee',
            name:'loanfee',
            disabled:true
        });
         this.loanrate.on('change',function(){
            if(this.loanrate.getValue()==0){
                this.loanrate.setValue("");
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.disbursementloanrate")], 2); 
            }
        },this);
         
        this.loanfee.on('change',function(){
            if(this.loanfee.getValue()==0){
                this.loanfee.setValue("");
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.disbursementloanfee")], 2); 
            }
        },this);
        
        this.percentloanfee.on('blur', this.getloanfee,this);
        
        this.loanamount.on('blur',function(){
            if((this.loanRuleType != ""  && this.loanamount.getValue() != "")){
                var exchangeRate=this.externalcurrencyrate == 0 ? 1:(1 /this.externalcurrencyrate);
                this.loanfee.setValue((this.loanamount.value * this.percentloanfee.value) / 100);
                if(this.loanamount.getValue()== 0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.disbursementloanamountexceedseligiableamount")], 2);
                }else if(this.loanRuleType != "UNLIMITED" && (this.loanamount.getValue() * exchangeRate ) > this.maximumloanEligibility.getValue()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.disbursementloanamountexceedseligiableamount")], 2);
                    this.loanamount.reset();
                    this.loanfee.setValue(Number(""));
                        return false; 
                }
            }
        },this);
        this.maximumloanEligibility=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            fieldLabel: WtfGlobal.getLocaleText("acc.loan.disbursementtab.maximumloanEligibility"),
            maxLength: 10,
            value:0,
            cls:"clearStyle",
            readOnly:true,
            anchor:'75%',
            hiddenName:'maximumloanEligibility',
            name:'maximumloanEligibility'
        });
        
        
        this.numberOfPeriods = new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            hideLabel:false,
            allowDecimals:false,
            //            value:0,
            allowBlank:false,
            disabled:true,
            maxLength: 5,
            anchor:'75%',
            fieldLabel:WtfGlobal.getLocaleText("acc.contract.numberOfPeriods")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.contract.invoicenumberofperiodshelp")),//'Number of Periods',
            name:'numberOfPeriods',
            hiddenName:'numberOfPeriods',
            id:"numberOfPeriods"+this.id
        });
    },
    loanType:function(){
        addMasterItemWindow(32); //32 is master id of Loan Type
    },
    loanCategory:function(){
        addMasterItemWindow(58); //58 is master id of Loan Type
    },
    
    setNextNumber:function(config){
        if(this.sequenceFormatStore.getCount()>0){
            if(this.isEdit ){ //only edit case  && !this.copyInv
                var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
                if(index!=-1){
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                    this.sequenceFormatCombobox.disable();
                    this.loanrefno.disable();   
                } else {
                    this.sequenceFormatCombobox.setValue("NA"); 
                    this.sequenceFormatCombobox.disable();
                    this.loanrefno.enable();  
                }
            }
            if(!this.isEdit ){   //|| this.copyInv|| this.GENERATE_PO||this.GENERATE_SO){// create new,copy,generate so and po case
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                }
                if(this.sequenceFormatCombobox.getvalue()!=""){
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                } else{
                     this.loanrefno.setValue("");
                     this.loanrefno.disable();
                }
            }         
        }
    },
    getNextSequenceNumber:function(a,val){     
        if(!(a.getValue()=="NA")){
            var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag=rec!=null?rec.get('oldflag'):true;
            Wtf.Ajax.requestEx({
                url:"ACCCompanyPref/getNextAutoNumber.do",
                params:{
                    from:Wtf.LONE_MANAGEMENT_MODULEID,
                    sequenceformat:a.getValue(),
                    oldflag:oldflag
                }
            }, this,function(resp){
                if(resp.data=="NA"){
                    this.loanrefno.reset();
                    this.loanrefno.enable();
                }else {
                    this.loanrefno.setValue(resp.data);
                    this.loanrefno.disable();
                }
            
            });
        } else {
            this.loanrefno.reset();
            this.loanrefno.enable();
        }
    },
    
    createForm:function(){
        this.disbursementForm=new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,        
            id:"northForm"+this.id,
            disabledClass:"newtripcmbss",
            disabled:this.readOnly,
            // bodyStyle:"padding:10px",
            border:false,
            items:[{
                layout:'form',
                defaults:{
                    border:false
                },
                baseCls:'northFormFormat',
                cls:"visibleDisabled",
                labelWidth:160,
                items:[{
                    layout:'column',
                    defaults:{
                        border:false
                    },
                    items:[{
                        layout:'form',
                        columnWidth:0.49,
                        items:[
                        this.custmerCmb,
                        this.Currency,this.suretyCmb,
                        this.loancategoryCombo,this.loanTypeCombo,
                        this.sequenceFormatCombobox,this.loanrefno,
                        this.scheduleTypeCombo,
                        {
                            layout:'column',
                            border:false,
                            defaults:{
                                border:false
                            },
                            items:[{
                                layout:'form',
                                width:'50%',
                                items:this.termValue
                            },{
                                layout:'form',
                                width:'50%',
                                items:this.loanTermType
                            }]
                        },
//                        this.installmentInterval,
                        this.installmentTypeCombo                              //,this.securityDeposite,this.leaseAmount,this.email,this.attachment
                        ]
                    },{
                        layout:'form',
                        columnWidth:0.49,
                        items:[this.disburseAccount,this.applicationdate,this.approveddate,this.disbursementdate,this.firstpaymentdate,this.loanrate,this.loanamount,
                        {
                            layout:'column',
                            border:false,
                            defaults:{
                                border:false
                            },
                            items:[{
                                layout:'form',
                                width:'50%',
                                items:this.percentloanfee
                            },{
                                layout:'form',
                                width:'50%',
                                items:this.loanfee
                            }]
                        },this.maximumloanEligibility]
                        }]
            }]
            }]
        });
 
    },
    
   setInstallmentInterval :function(){
        if(this.termValue != "" && this.loanTermType != "" ){
            if(this.loanTermType.getValue()== 1){
                this.installmentInterval.setValue(this.termValue.getValue());
            }else{
                this.installmentInterval.setValue(this.termValue.getValue()*12);
            }       
        }
    },
    
    updateData:function(){
        var customer= this.custmerCmb.getValue();
        Wtf.Ajax.requestEx({
            url:"ACCCustomerCMN/getCurrencyInfo.do",
            params:{
                mode:4,
                customerid:customer,
                isBilling : false,
                getLoanEligibility : true
            }
        }, this,this.setCurrencyInfo);       
       
//        this.leaseAmount.enable();
//        this.leaseAmount.reset();
//        this.leaseAmount.disable();
     
    },
    setCurrencyInfo:function(response){
        if(response.success){
            this.externalcurrencyrate=0;
            this.custdatechange=true;
            this.Currency.setValue(response.currencyid);
            this.maximumloanEligibility.setValue(response.maximumloanEligibility);
            this.loanRuleType = response.loanRuleType;     
            this.loanRuleTypeValue = response.loanRuleTypeValue;     
            this.currencyid=response.currencyid;
            this.symbol = response.currencysymbol;       
            this.custChange=true;
            this.changeCurrencyStore();
            this.currencyStore.load();
        //            this.SOStore.load();
        }
    },
    changeCurrencyStore:function(){
        var currency=this.Currency.getValue();
        if(this.val=="")this.val=new Date();
        if(currency!=""||this.custChange)
            this.currencyStore.load({
                params:{
                    mode:201,
                    transactiondate:WtfGlobal.convertToGenericDate(this.val),
                    tocurrencyid:this.Currency.getValue()
                    }
                });
    else
        this.currencyStore.load({
            params:{
                mode:201,
                transactiondate:WtfGlobal.convertToGenericDate(this.val)
                }
            });
},
save:function(){
   
    if(!this.disbursementForm.getForm().isValid()){
        WtfComMsgBox(2, 2);
        return;
    }
    if(this.disbursementForm.getForm().isValid() ) {
        var rec=this.disbursementForm.getForm().getValues();
        WtfGlobal.onFormSumbitGetDisableFieldValues(this.disbursementForm.form.items, rec);
        rec.customer=this.custmerCmb.getValue();
        this.ajxurl = ""; 
        this.ajxurl = "ACCLoanCMN/saveDisbursement.do";
        this.showConfirmAndSave(rec,false); 
    }else{
        WtfComMsgBox(2, 2);
    }
},

showContractAttachWindow: function(){
    this.imageUploadURL=(this.savedFilesMappingId==undefined || this.savedFilesMappingId==null) ?'ACCLoanCMN/attachDocuments.do?type=doc' :'ACCLoanCMN/attachDocuments.do?type=doc&savedFilesMappingId='+this.savedFilesMappingId;
        var scopeObj = this;
        var comp = Wtf.getCmp('loanFileUploalWindow');
        if(!comp){
            this.uploadForm = new Wtf.MultiFlieUploadPanel({
                methodType : 'upload',
                id:'loanFileUploalWindow',
                layout : 'fit',
                closable:false,
                border : false,
                url : this.imageUploadURL,
                savedFilesMappingId:this.savedFilesMappingId,
                isDisbursement : true,
                fileStr:this.isFromSaveAndCreateNewButton ? undefined : scopeObj.attachedFilesStr,
                bbar : [new Wtf.FileBrowseButton({
                    text : WtfGlobal.getLocaleText("acc.activitydetailpanel.addfilesBTN"),//'Add Files',
                    tooltip : WtfGlobal.getLocaleText("acc.template.addfiles.ttip"),//'Click here to browse and add your files to upload',
                    handler : function(btn) {
                        this.uploadForm.addFiles(btn);
                    },
                    scope : this
                }),{
                    text : WtfGlobal.getLocaleText("acc.uploadbtn"),//'Upload',
                    tooltip : WtfGlobal.getLocaleText("acc.template.uploadbtn.ttip"),//'Click here to start uploading your files which are listed above',
                    handler : function() {
                        this.uploadForm.startUpload();
                    },
                    scope : this
                },{
                    text : WtfGlobal.getLocaleText("acc.template.stop"),//'Stop',
                    tooltip : WtfGlobal.getLocaleText("acc.template.canceluploading.ttip"),//'Cancel uploading of files which are not uploaded yet',
                    handler : function() {
                        this.uploadForm.cancelUpload();
                    },
                    scope : this
                },{
                    text : WtfGlobal.getLocaleText("acc.template.clear"),//'Clear',
                    tooltip : WtfGlobal.getLocaleText("acc.template.removefilesttip"),//'remove all files which are listed above',
                    handler : function() {
                        this.uploadForm.clearAll();
                    },
                    scope : this
                },'->',{
                    text : WtfGlobal.getLocaleText("acc.msgbox.ok"),//'Clear',
                    //            tooltip : WtfGlobal.getLocaleText("acc.template.removefilesttip"),//'remove all files which are listed above',
                    handler : function() {
                        this.attachWindow.close();
                    },
                    scope : this
                }]
            });
    
            this.uploadForm.on('uploadComplete',function(){
                this.savedFilesMappingId = this.uploadForm.savedFilesMappingId;
                this.attachedFilesStr = this.uploadForm.savedFilesId;
            },this)            ;
            this.attachWindow=new Wtf.Window({
                width:600,
                height: 300,
                modal :true,
                title : WtfGlobal.getLocaleText("acc.contract.uploadnewfile"),//'Upload File',
                layout : 'fit',
                items:[this.uploadForm]
            });
            this.attachWindow.show();
        }
        
},
showConfirmAndSave: function(rec,incash){ //detail,servicedetail
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),this.EditisAutoCreateDO ?  WtfGlobal.getLocaleText("acc.invoice.msg16"):WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
        if(btn!="yes") {
            return;
        }
            
        this.msg= WtfComMsgBox(27,4,true);
        rec.currencyid=this.Currency.getValue();
        rec.customer=this.custmerCmb.getValue();;
        rec.termValue=this.termValue.getValue();
        rec.savedFilesMappingId= this.savedFilesMappingId;
        rec.currencyid=this.Currency.getValue();
        rec.externalcurrencyrate=this.externalcurrencyrate;     
        rec.number=this.loanrefno.getValue();
        rec.installmentinterval=this.installmentInterval.getValue();
//        rec.file=this.attachment.getValue();
        rec.disbursementdate=WtfGlobal.convertToGenericDate(this.disbursementdate.getValue());
        rec.firstpaymentdate=WtfGlobal.convertToGenericDate(this.firstpaymentdate.getValue());
        rec.approveddate=WtfGlobal.convertToGenericDate(this.approveddate.getValue());
        rec.applicationdate=WtfGlobal.convertToGenericDate(this.applicationdate.getValue());


        if(this.isEdit){
            rec.loanrefno=this.loanrefno;
            rec.isEdit=this.isEdit;
            rec.disbursementid=this.disbursementid;
        }          
        rec.loanrefno=this.loanrefno.getValue();
        rec.mode=this.moduleid;
        rec.loanRuleTypeValue=this.loanRuleTypeValue;
        var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
        rec.sequenceformat=this.sequenceFormatCombobox.getValue();
        rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                  
        Wtf.Ajax.requestEx({
            url:this.ajxurl,
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);

    },this);
},
disableComponent : function(){
    if(this.disbursementForm){
        this.disbursementForm.disable();
    }
    if(this.saveBttn){
        this.saveBttn.disable();
    }
    if(this.savencreateBttn){
        this.savencreateBttn.disable();
    }
    if(this.attachFilesBttn){
        this.attachFilesBttn.disable();
    }
},
genSuccessResponse:function(response, request){
    if(response.success){
        WtfComMsgBox([this.title,response.msg],response.success*2+1); //WtfGlobal.getLocaleText("acc.loan.DisbursementsuccessfullygeneratedNumber") + '<b>'+this.loanrefno.getValue()+'</b>'
    } else {
        WtfComMsgBox([this.title,response.msg],response.success*2+1);
    }
        
    if(response.success) {
        if(this.saveOnlyFlag === 1){
            this.isFromSaveAndCreateNewButton=false;
            this.disableComponent();//use of this function - do not reset form
        }else  if(this.saveOnlyFlag === 2){
            
            this.isFromSaveAndCreateNewButton=true;
            this.attachedFilesStr=""//to clear attachment 
            this.attachment.setValue("");
            this.sequenceFormatStore.load();
            this.disbursementForm.getForm().reset()
            this.symbol = WtfGlobal.getCurrencySymbol();
            this.currencyid = WtfGlobal.getCurrencyID();
            this.termValue.setValue(1);
            this.loanTermType.setValue(2);
            this.installmentInterval.setValue(12);
            this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
        }
            
    }
},

genFailureResponse:function(response){
    Wtf.MessageBox.hide();
    var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
    if(response.msg)msg=response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
},
updateFormCurrency:function(){
//    this.onCurrencyChangeOnly();
    this.applyCurrencySymbol();
},

applyCurrencySymbol:function(){
        var index=this.getCurrencySymbol();
        if(index>=0){
           this.externalcurrencyrate=this.currencyStore.getAt(index).data.exchangerate;
           this.symbol=  this.currencyStore.getAt(index).data.symbol;
       }
       return this.symbol;
    },    
    
getCurrencySymbol:function(){
        var index=null;
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
    
    getloanfee: function () {
        var loanfeepercetage = this.percentloanfee.getValue();
        var amount = this.loanamount.getValue();
        if (amount !="" && loanfeepercetage != "") {
            this.loanfee.setValue((amount * loanfeepercetage) / 100);
        } else{
            this.percentloanfee.reset();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.loan.disbursementloanfee")], 2);
        }
    }
});
