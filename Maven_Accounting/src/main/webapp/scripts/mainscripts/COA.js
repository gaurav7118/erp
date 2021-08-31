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

Wtf.account.COA = function(config){
    this.cashbank=false;
    this.incomenature=config.incomenature;
    this.ispurchase=config.ispurchase;
    this.islibility=config.islibility;
    this.isotherexpense=config.isotherexpense;
    this.isexpense=config.isexpense;
    this.issales=config.issales;
    this.isCOA = config.isCOA;
    this.responsedAccountId = "";
    this.currencyExchangeWinId = "SetCurrencyExchangeWin";
    this.ibgBankDetailRec = {};
    this.ibgbankdetailid = "";
    this.cimbbankdetailid = "";
    this.uobbankdetailid = "";
    this.ibgbanktype="";
    this.ispropagatetochildcompanyflag=false;
    this.fromPaymentMethod=config.fromPaymentMethod;
    this.isParentAccountBeforeSelect = "";
//    this.fieldwidth = Wtf.account.companyAccountPref.splitOpeningBalanceAmount ? 300 : 185;
    this.fieldwidth = 350;
    
    Wtf.apply(this,{
        constrainHeader :true,		// 19991
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            scope: this,
            handler:this.confirmBeforeSave.createDelegate(this)
        }, {
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeForm.createDelegate(this)
        }]
    },config);
    Wtf.account.COA.superclass.constructor.call(this, config);
    this.groupId = config.groupId!=undefined?config.groupId:"";
    this.accountType = config.accountType!=undefined?config.accountType:0;
    this.addEvents({
        'update':true,
        'cancel':true,
        'loadingcomplete':true
    });
}

Wtf.extend(Wtf.account.COA, Wtf.Window, {
    loadRecord:function(){
        this.openingBal.setValue(0);
        if(this.record!=null){
            
            if(this.isEdit){
                if(this.Currency.getValue() != Wtf.account.companyAccountPref.currencyid){
                    Wtf.Ajax.requestEx({
                        url:"ACCCurrency/getCurrencyExchange.do",
                        params: {
                            transactiondate: WtfGlobal.convertToGenericDate(this.record.data.creationDate),
                            tocurrencyid: this.record.data.currencyid
                        }
                    },this,function(response){
                        if(response.count>0){
                        this.exchangeRate.setValue(response.data[0].exchangerate);
                        }else{
                            this.exchangeRate.setRawValue("");
                            this.exchangeRate.disable();
                            this.Currency.reset();
                            callCurrencyExchangeWindow();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.creationdate.getValue())+"</b>"], 0);
                        }
                    });
                }else{
                    this.exchangeRate.setValue(1);
                    this.exchangeRate.disable();
                }

                if(this.isEdit){
                    Wtf.Ajax.requestEx({
                        url:"ACCAccountCMN/editCurrency.do",
                        params: {accid: this.record.data['accid']}
                    },this,this.editCurrencySuccessResponse,this.editCurrencyFailureResponse);
                }
            }

            if(this.record.data['parentid'])
                this.subAccount.toggleCollapse();                            
            this.coaForm.getForm().loadRecord(this.record);
//            var accountHasOpeningTransactions = this.record.data.accountHasOpeningTransactions;
//            if(accountHasOpeningTransactions){
//                this.openingBal.disable();
//            }else{
//                this.openingBal.enable();
//            }
            var accountTypeTransaction = this.record.data.accountTypeTransaction;
            if(accountTypeTransaction){
                this.radioGL.disable();
                this.radioCash.disable();
                this.radioBank.disable();
                this.radioGST.disable();
                this.accountTypeCombo.disable();
                /*
                 * SDP-12497 - If account used in any of IBG, Tax, Payment Method or Product then allow to enter opening for COA.
                 */
//                this.openingBal.disable(); 
                this.balTypeEditor.disable();
            }else if(this.record.data.accounttype==0){
                this.radioGL.enable();
                this.radioCash.disable();
                this.radioBank.disable();
                this.radioGST.disable();
                this.accountTypeCombo.enable();
            }else{
                this.radioGL.enable();
                this.radioCash.enable();
                this.radioBank.enable();
                this.radioGST.enable();
                this.accountTypeCombo.enable();
                this.openingBal.enable();
                this.balTypeEditor.enable();
            }
            var accountHasJedTransaction = this.record.data.accountHasJedTransaction;
            if(!this.record.data.acccode=='' && accountHasJedTransaction){
                this.accCode.disable();
            }else{
                 this.accCode.enable();
            }
            var bal=this.record.data.openbalance;
            if (this.isCOA != undefined && this.isCOA) { // opening balance excluding child opening balance 
                bal = this.record.data.orignalopenbalance;
            }
            if(bal!=0){		// Neeraj Opening balance not used anymore for asset value
                this.openingBal.setValue(Math.abs(bal));
                (bal>0)?this.balTypeEditor.setValue(true):this.balTypeEditor.setValue(false);
            }else{
                var rec=this.accGroup.store.getAt(this.accGroup.store.find("groupid",this.accGroup.getValue()))
                if(rec) {
                    this.setOBType(rec.data["nature"]);
                    this.tagsFieldset.setOBType(rec.data["nature"]);
                }
            }
            if(this.record){
                if(this.record.data['mastertypevalue']==1){
                    this.radioGL.setValue(true);
                    this.Currency.setDisabled(true);
                }else if(this.record.data['mastertypevalue']==2){
                    this.radioCash.setValue(true);
                    this.Currency.setDisabled(false);
                }else if(this.record.data['mastertypevalue']==3){
                    this.radioBank.setValue(true);
                    this.Currency.setDisabled(false);
                    this.ibgBank.enable();
                    if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                        this.mailingDetailsSetup.show();
                    }
                }else if(this.record.data['mastertypevalue']==4){
                    this.radioGST.setValue(true);
                     this.Currency.setDisabled(true);
                }               
            }
            this.custMinBudget.setValue(this.record.data.custminbudget);
            if(this.ifsccode){
                this.ifsccode.setValue(this.record.data.ifsccode);
            }
            this.creationdate.setValue(this.record.data.creationDate);
            this.accCode.setValue(this.record.data.acccode);
            this.taxCombo.setValue(this.record.data.taxid);
            this.users.setValue(this.record.data.userid);
            if(this.record.data.isHeaderAccount){                                        
                this.accGroup.setDisabled(true);
                this.openingBal.setDisabled(true);
                this.exchangeRate.setDisabled(true);
                this.Currency.setDisabled(true);
                this.creationdate.setDisabled(true);
                this.balTypeEditor.setDisabled(true);
                this.taxCombo.setDisabled(true);                
            }
            if(this.record.data.isHeaderAccount||(this.record.data['parentid']!=undefined && this.record.data['parentid']!="")){       
                var groupID=this.record.data['groupid'];
                var grouprec =  this.accGroup.store.findBy(function(record) {
                    if(record.data.groupid==groupID)
                        return true;
                    else
                        return false;
                });
                grouprec=this.accGroup.store.getAt(grouprec);                        
                var mastergroupid=grouprec.data['mastergroupid'];
                this.parentAccountStore.filterBy(function(rec){
                    if(rec.data.mastergroupid==mastergroupid)
                        return true
                    else
                        return false
                },this);                                
                this.accGroup.store.filterBy(function(rec){
                    if(rec.data.mastergroupid==mastergroupid)
                        return true
                    else
                        return false
                },this);                
            }
            if(Wtf.account.companyAccountPref.bbfrom && this.creationdate){
                var creationDate=WtfGlobal.getDayMonthYearDate(this.creationdate.getValue());
                var bbDate=WtfGlobal.getDayMonthYearDate(Wtf.account.companyAccountPref.bbfrom);
                if(creationDate>bbDate){
                    this.openingBal.disable();
                }else if(!this.record.data.accountHasOpeningTransactions && !accountTypeTransaction){
                        this.openingBal.enable();
                        this.balTypeEditor.enable();
                }
            }
            //ERP-40205 - Disable opening balance field if account is used as Inventory account in product master
            if(this.record && this.record.data && this.record.data.accountUsedAsInventoryAccountInProduct){
                this.openingBal.disable();
            }
            if(this.record.data.isibgbank){     // Setting the saved data initially on loading
                // Data of CIMB Bank
                this.ibgBankDetailRec.settlementMode = this.record.data['settlementMode'];
                this.ibgBankDetailRec.serviceCode = this.record.data['serviceCode'];
                this.ibgBankDetailRec.bankAccountNumber = this.record.data['bankAccountNumber'];
                this.ibgBankDetailRec.postingIndicator = this.record.data['postingIndicator'];
                this.ibgBankDetailRec.ordererName = this.record.data['ordererName'];
                this.ibgBankDetailRec.currencyCode = this.record.data['currencyCode'];
                this.ibgBankDetailRec.cimbbankdetailid = this.record.data['cimbbankdetailid'];
                this.cimbbankdetailid = this.record.data['cimbbankdetailid'];
                
                // Data of DBS Bank
                this.ibgbankdetailid = this.record.data['ibgbankdetailid'];
                this.ibgBankDetailRec.ibgbankdetailid = this.record.data['ibgbankdetailid'];
                this.ibgBankDetailRec.bankCode = this.record.data['bankCode'];
                this.ibgBankDetailRec.branchCode = this.record.data['branchCode'];
                this.ibgBankDetailRec.accountNumber = this.record.data['accountNumber'];
                this.ibgBankDetailRec.accountName = this.record.data['accountName'];
                this.ibgBankDetailRec.sendersCompanyID = this.record.data['sendersCompanyID'];
                this.ibgBankDetailRec.bankDailyLimit = this.record.data['bankDailyLimit'];
                
                // Data of UOB Bank
                this.uobbankdetailid = this.record.data['uobbankdetailid'];
                this.ibgBankDetailRec.uobbankdetailid = this.record.data['uobbankdetailid'];
                this.ibgBankDetailRec.uobOriginatingBICCode = this.record.data['uobOriginatingBICCode'] 
                this.ibgBankDetailRec.uobCurrencyCode = this.record.data['uobCurrencyCode'] 
                this.ibgBankDetailRec.uobOriginatingAccountNumber = this.record.data['uobOriginatingAccountNumber'] 
                this.ibgBankDetailRec.uobOriginatingAccountName = this.record.data['uobOriginatingAccountName'] 
                this.ibgBankDetailRec.uobUltimateOriginatingCustomer = this.record.data['uobUltimateOriginatingCustomer']
                this.ibgBankDetailRec.uobCompanyId = this.record.data['uobCompanyId'];
                
                // Data of OCBC Bank
                this.ocbcbankdetailid = this.record.data["ocbcbankdetailid"];
                this.ibgBankDetailRec.ocbcbankdetailid = this.record.data["ocbcbankdetailid"];
                this.ibgBankDetailRec.ocbcOriginatingBankCode = this.record.data["ocbcOriginatingBankCode"];
                this.ibgBankDetailRec.ocbcAccountNumber = this.record.data["ocbcAccountNumber"];
                this.ibgBankDetailRec.ocbcReferenceNumber = this.record.data["ocbcReferenceNumber"];
                
                // Common data
                this.ibgBankDetailRec.ibgbanktype = this.record.data['ibgbanktype'];
                this.ibgBankDetailRec.ibgbank = this.record.data['ibgbanktype'];   
                this.ibgBankDetailRec.accountHasJedTransaction = this.record.data['accountHasJedTransaction'];   
            } 
            var purchasetype=this.record!=undefined?this.record.data['purchasetype']:"";
            this.typeOfPurchaseCombo.setValue(purchasetype)
            var salestype=this.record!=undefined?this.record.data['salestype']:"";
            this.typeOfSalesCombo.setValue(salestype)
        }
        else
         if(this.issales){
            this.accountTypeCombo.setValue(this.accountType);
            this.accGroup.setValue(this.groupId);
            this.balTypeEditor.setValue(false);
            this.accGroup.setDisabled(true);
        }
        else if(this.incomenature) {
            this.accountTypeCombo.setValue(this.accountType);
            this.accGroup.setValue(this.groupId);
            this.balTypeEditor.setValue(false);
        }
        else if(this.ispurchase){
            this.accountTypeCombo.setValue(this.accountType);
            this.accGroup.setValue(this.groupId);
            this.accGroup.setDisabled(true);
            this.balTypeEditor.setValue(true);
        }
        else if(this.islibility){
            this.accountTypeCombo.setValue(this.accountType);
            this.accGroup.setValue(this.groupId);
            this.balTypeEditor.setValue(false);
        }
         else if(this.isotherexpense){
            this.accountTypeCombo.setValue(this.accountType);
            this.accountTypeCombo.setValue(this.accountType);
            this.accGroup.setValue(this.groupId);
//            this.accGroup.setDisabled(true);
            this.balTypeEditor.setValue(true);
        }
        else if(this.isexpense){
            this.accountTypeCombo.setValue(this.accountType);
            this.accGroup.setValue(this.groupId);
//            this.accGroup.setDisabled(true);
            this.balTypeEditor.setValue(true);
        }
        // ERP-25558 - Setting default master type and account group
        if(this.fromPaymentMethod){
            this.radioCash.setValue(false);
            this.radioGL.setValue(false);
            this.radioGST.setValue(false);
            this.radioBank.setValue(true);
            var accgroupRec = WtfGlobal.searchRecord(this.groupStore, 'Bank', 'groupname');
            if(accgroupRec!=null && accgroupRec!=undefined){
                var accGroupId = accgroupRec.data.groupid;
                this.accGroup.setValue(accGroupId);
            }
        }
        if(this.record!=null){
            if(this.record.data.controlAccounts){
                this.enableDisableFields();
            }
        }
        this.sortAccountGroups();
        this.hideLoading(false);
        this.loadingMask.hide();
    },

    editCurrencySuccessResponse:function(response){
        this.editCurrency=response.currencyEdit;
        if(!this.editCurrency || this.record.data.isHeaderAccount){
            this.Currency.disable();
//            this.openingBal.disable();
        }else{
            this.Currency.enable();
//            this.openingBal.enable();
        }
        if(this.record){
                if(this.record.data['mastertypevalue']==1){
                    this.Currency.setDisabled(true);
                }else if(this.record.data['mastertypevalue']==2){
                    this.Currency.setDisabled(false);
                }else if(this.record.data['mastertypevalue']==3){
                    this.Currency.setDisabled(false);
                    if(this.record.data['isibgbank'] == true) {
                        this.Currency.disable();
                    }
                }else if(this.record.data['mastertypevalue']==4){
                    this.Currency.setDisabled(true);
                }               
            }
    },

    onRender: function(config) {
        Wtf.account.COA.superclass.onRender.call(this, config);
        this.createFields();
        this.createForm();
        this.createPanel();
        var msg = WtfGlobal.getLocaleText("acc.bankBook.gridAccount");  //(this.record==null? "Add New ":"Edit ")+(this.isFixedAsset?'Fixed Asset':'Account');
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.title,msg,"../../images/accounting_image/Chart-of-Accounts.gif")
        }, this.centerPanel=new Wtf.Panel({
            region: 'center',
            border: false,            
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'border',
            items:[this.coaForm,{
                    region: 'east',
                    border: false,
                    width:160,
                    layout: 'fit',
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    items:[this.pan]
                }]
        }));
    },

    hideLoading:function(val){
        if(!val){
            this.fireEvent("loadingcomplete",this);
            this.accCode.focus();      //To set focus on Name on form load.
        }
    },

    createFields:function(){
        //Record and store for tax combo
        this.taxRec = new Wtf.data.Record.create([
        {
            name: 'taxid'
        },{
            name: 'taxname'
        },{
            name: 'taxcode'
        },{
            name: 'percent',
            type:'float'
        },{
            name: 'applydate',
            type:'date'
        },{
            name: 'isEditing'
        },{
            name: 'hasAccess'
        }]);

        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
            url:"ACCTax/getTax.do",
            baseParams: {
                includeDeactivatedTax: this.isEdit != undefined ? this.isEdit : false
            }            
        });
        this.taxStore.on("load", function() {
            var record = new Wtf.data.Record({
                taxid: '',
                taxname: 'None'
            });
            this.taxStore.insert(this.taxStore.getCount()+1, record);
        }, this);

         var groupRec=new Wtf.data.Record.create([
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'nature'},
            {name: 'mastergroupid'},
            {name: 'naturename'},
            {name: 'leaf',type:'boolean'},
            {name: 'level', type:'int'}
        ]);
       
        this.groupStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },groupRec),
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccount/getGroups.do",
            baseParams:{
                mode:1,
                ignorevendors:false,
                ignorecustomers:false
            }
        });
        
        var MVATAnnexureCodesRec=new Wtf.data.Record.create([
            {name: 'mvatannexurecode'},
            {name: 'mvatdescription'}
        ]);
         this.MVATAnnexureCodesStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },MVATAnnexureCodesRec),
            url:"ACCAccount/getMVATAnnexureCodeForAccount.do"
        });   
        this.accRec = Wtf.data.Record.create ([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'}
//            {name:'level', type:'int'}
        ]);
 
        var parentRec=new Wtf.data.Record.create([
            {name: 'parentid',mapping:'accid'},
            {name: 'groupid'},
            {name: 'nature'},
            {name: 'naturename'},
            {name: 'mastergroupid'},
            {name: 'accounttype'},
            {name: 'mastertypevalue'},
            {name: 'parentname',mapping:'accname'},
            {name: 'deleted'},
            {name: 'code',mapping:'acccode'},
            {name: 'isOnlyAccount'},
            {name: 'alscode',mapping:'aliascode'}
//            {name: 'leaf',type:'boolean'},
//            {name: 'level',type:'int'}
        ]);

        
        this.parentStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },parentRec),
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                accountid:(this.record!=null?this.record.data['accid']:null)
            }
        });        
        
        this.parentAccountStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },parentRec),
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                accountid:(this.record!=null?this.record.data['accid']:null),
                ignorecustomers:true,
                ignorevendors:true,                                                
                COA: true,
                headerAdded:true,
                nondeleted:true
            }
        });
        
        this.groupStore.on('load',function(){
            if(this.issales)
                this.parentAccountStore.load({params:{group:[5]}});
            else if(this.ispurchase)
                this.parentAccountStore.load({params:{group:[6]}});
            else if(this.isotherexpense)
                this.parentAccountStore.load({params:{group:[8]}});
             else if(this.isexpense)
                this.parentAccountStore.load({params:{group:[7]}});
            else if(this.cashbank)
                this.parentAccountStore.load({params:{group:[2,9,18]}});
            else if(this.incomenature)
                this.parentAccountStore.load({params:{nature:[Wtf.account.nature.Income]}});
            else
                this.parentAccountStore.load({params:{group:[12],ignore:true}});
            this.hideLoading(false);
        },this);
        
        this.taxStore.load();
        if((Wtf.Countryid==Wtf.Country.INDIA && Wtf.Stateid==Wtf.StateName.MAHARASHTRA) &&  Wtf.account.companyAccountPref.enablevatcst){
            this.MVATAnnexureCodesStore.load();
        }
        if(this.isEdit && this.record && this.record.data && (this.record.data.parentid != null && this.record.data.parentid != '' && this.record.data.parentid != undefined)){
            this.groupStore.load({
                params:{
                    nature:this.record.data.nature
                }
            });
        } else {
            if(this.cashbank){
                this.groupStore.load({
                params:{
                    group:[2,9,18]
                    }
                });
            }else if(this.incomenature || this.isexpense){
                this.groupStore.load();
            }else{
                this.groupStore.load({
                params:{
                    group:[12],
                    ignore:true
                }});
            }
        }

    chkcurrencyload();

    this.currencyRec = new Wtf.data.Record.create([
    {
        name: 'currencyid',
        mapping:'tocurrencyid'
    },{
        name: 'symbol'
    },{
        name: 'currencyname',
        mapping:'tocurrency'
    },{
        name: 'exchangerate'
    },{
        name: 'htmlcode'
    }]);

    this.currencyStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.currencyRec),
        //        url:Wtf.req.account+'CompanyManager.jsp'
        url:"ACCCurrency/getCurrencyExchange.do"
    });
    
     this.defaultCurrencyRec = new Wtf.data.Record.create([
         {name: 'id'},
         {name: 'fromcurrencyid'},
         {name: 'tocurrencyid'}
     ]);
     
     this.defaultCurrencyStore = new Wtf.data.Store({
         reader: new Wtf.data.KwlJsonReader({
             root: "data",
             totalProperty:"count"
         },this.defaultCurrencyRec),
         url:"ACCCurrency/getDefaultCurrencyExchange.do"
     });

     this.currencyStore.on("load",function(store){
         if(!this.isEdit && Wtf.account.companyAccountPref.currencyid){
             this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
         }
         
         if(this.isEdit && this.record!=undefined && this.record !=null){//Some due to the slowness currecny does not get set. Hence need to set here ERP-31003
             this.Currency.setValue(this.record.data.currencyid);
         }         
         if(store.getCount()==0){
            callCurrencyExchangeWindow(this.currencyExchangeWinId);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please set Currency Exchange Rates"],2);
            Wtf.getCmp(this.currencyExchangeWinId).on("update", function(){
                this.currencyStore.reload();
                this.defaultCurrencyStore.reload();
            },this);
        }
     },this);
     this.defaultCurrencyStore.load();
     if(this.isEdit && this.record.data.creationDate!="" &&this.record.data.creationDate!=undefined){
         this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.record.data.creationDate)}});
     }else{
         this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(Wtf.account.companyAccountPref.bbfrom)}});
     }
     this.groupStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
     this.currencyStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
     this.parentStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
     this.parentStore.load();
     
     this.parentAccountStore.on('load',this.loadRecord,this);
     this.loadingMask = new Wtf.LoadMask(document.body,{
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
            });
     this.parentAccountStore.on('beforeload',function(){
         this.loadingMask.show();
     },this);
     
        //Combo for Tax
        this.taxCombo= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.coa.mapTax"),//Map Tax
            name:'taxid',
            width: this.fieldwidth,
            store:this.taxStore,
            hidden :  (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it,
            hideLabel : (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it,
            valueField:'taxid',
            emptyText:WtfGlobal.getLocaleText("acc.coa.mapTaxEmptyText"),//"Please select Tax",  //'Please select Tax...',
            forceSelection: true,
            displayField:'taxname',
            selectOnFocus:true,
            disabledClass:"newtripcmbss",
            typeAhead: true,
            mode: 'remote',
            minChars:0,
            addNoneRecord: false,         //For 'None' option in Tax Combo.
            triggerAction: 'all',
            extraFields: [],
            isTax: true,
            listeners: {
                'beforeselect': {
                    fn: function (combo, record, index) {
                        return validateSelection(combo, record, index);
                    },
                    scope: this
                }
            }
        });

        this.custMinBudget = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.MinimumBudgetLimit"),
            name:'custminbudget',
            disabledClass:"newtripcmbss",
            width: this.fieldwidth,
            maxLength:15,
            minvalue : 0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            value:this.isEdit ? 0 : 0
        });
        
        this.ifsccode = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.IFSCCode"),  //"IFSC Code",
            width: this.fieldwidth,
            maxLength:11,
            emptyText:WtfGlobal.getLocaleText("acc.setupWizard.enterIFSCCode"),  //"Enter IFSC Code here",
            name:"ifsccode",
            invalidText :'Alphabets and numbers only example- ANDB0001478 (4 letters + 7 digits)',
            vtype : "alphanum",
            regex:/[^\s]{4}\d{7}/
        });
        
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:this.id,
            autoHeight: true,
            autoWidth:true,
            parentcompId:this.id,
            disabledClass:"newtripcmbss",
            moduleid:this.moduleid,
            isEdit: this.isEdit,
            record: this.record,
            isWindow:true,
            widthVal:93,
            accountid:((Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.record!=null)?this.record.data['accid']:null),
            presentbalance:((Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.record!=null)?this.record.data['openbalance']:null),
            IsCOA: (Wtf.account.companyAccountPref.splitOpeningBalanceAmount ? true : false)
        });
        
        this.accCode = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.coa.addAccCode"), //"Account Code",
            name: 'acccode',
            id: 'acccode',
            allowBlank:false,
            width: this.fieldwidth,
            maxLength:50,
            listeners:{
                scope:this,
                focus:function(){
                    this.searchText(this.parentStore,this.pan,'acccode','code');
                }
            }
        });
        
        this.aliasCode = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.coa.aliasCode"),
            name: 'aliascode',
            id: 'aliascode',
            disabledClass:"newtripcmbss",
            width: this.fieldwidth,
            maxLength:50,
            listeners:{
                scope:this,
                focus:function(){
                    this.searchText(this.parentStore,this.pan,'aliascode','alscode');
                }
            }
        });

        this.exchangeRate = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.setupWizard.curEx"+"*"),
            name: 'exchangerate',
            disabledClass:"newtripcmbss",
            allowBlank:false,
            width: this.fieldwidth,
            maxLength:15,
            minvalue : 0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            value:1,
            validator:function(val){
                if(val>0)
                    return true;
                else
                    return WtfGlobal.getLocaleText("acc.field.Exchangeratemustbegreaterthanzero.");
            }
        });
        
        this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.coa.gridCurrency") + "*",  //'Currency*',
            hiddenName:'currencyid',
            disabled:this.isEdit ? true : false,
//            anchor: '90%',
            disabledClass:"newtripcmbss",
            width: this.fieldwidth,
            allowBlank:false,
            store:this.currencyStore,
            valueField:'currencyid',
            emptyText:WtfGlobal.getLocaleText("acc.cust.currencyTT"),  //'Please select Currency...',
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true,
            maxHeight:200,
            listAlign:"bl-tl?"
        });
        
        this.Currency.on('select',function(combo,record,index){
            if (Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.tagsFieldset != undefined) {
                this.ApplyCurrencySymbol();
            }
             
            if(this.Currency.getValue()!=undefined && this.Currency.getValue()!=""){
                if(this.Currency.getValue() != Wtf.account.companyAccountPref.currencyid){
                    Wtf.Ajax.requestEx({
                        url:"ACCCurrency/getCurrencyExchange.do",
                        params: {
                            transactiondate: WtfGlobal.convertToGenericDate(this.creationdate.getValue()),
                            tocurrencyid: this.Currency.getValue()
                        }
                    },this,function(response){
                        if(response.count>0){
                            this.exchangeRate.setValue(response.data[0].exchangerate);
                        }else{
                            this.exchangeRate.setRawValue("");
                            this.exchangeRate.disable();
                            this.Currency.reset();
                            callCurrencyExchangeWindow();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.creationdate.getValue())+"</b>"], 0);
                        }
                    });
                }else{
                    this.exchangeRate.setValue(1);
                    this.exchangeRate.disable();
                }
            }else{
                this.exchangeRate.setRawValue("");
                this.exchangeRate.disable();
            }
        },this);
        
        this.cmbParent= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.20"),  //'Fixed Asset Parent Name*':'Parent Account',
            hiddenName:'parentid',
            name:'parentid',
            store:this.parentAccountStore,
            valueField:'parentid',
            displayField:'parentname',
            disableOnField: "deleted",
            disabledClass:"newtripcmbss",
            width: this.fieldwidth,
            mode: 'local',
            lastQuery : '',
            //disableKeyFilter:true,
            triggerAction:'all',
            typeAhead: true,
            hirarchical:true,
            forceSelection:true
        });
        
        this.AccName=new Wtf.form.ExtendedTextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.AccountName"),  // 'Account Name*',
            name: 'accname',
            disabledClass:"newtripcmbss",
            width: this.fieldwidth,
//            regex:Wtf.specialChar,
            maxLength:200,
            id:'accname',
            allowBlank:false,
            listeners:{
                scope:this,
                focus:function(){
                    this.searchText(this.parentStore,this.pan,'accname','parentname');
                }
            }
        });
        
        this.AccDesc=new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.AccountDescription"),  //'Account Description'
            name: 'accdesc',
            id:"accdesc"+this.heplmodeid+this.id,
            height:40,
            width : this.fieldwidth
            //maxLength:240
        });

        this.creationdate= new Wtf.form.FnDateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.rem.24"),  //'Creation Date':'As On',
            name: 'creationDate',
            disabledClass:"newtripcmbss",
            width: this.fieldwidth,
            format:WtfGlobal.getOnlyDateFormat(),
            disabled:this.isEdit ? true :false,
            value: Wtf.account.companyAccountPref.bbfrom,
            allowBlank:false
        });

        this.creationdate.on('change',this.checkForFirstFinancialYearDate,this);
        this.creationdate.on("dateselect", function(){
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.creationdate.getValue())}});
            if(this.Currency.getValue()!=undefined && this.Currency.getValue()!=""){
                if(this.Currency.getValue() != Wtf.account.companyAccountPref.currencyid){
                    Wtf.Ajax.requestEx({
                        url:"ACCCurrency/getCurrencyExchange.do",
                        params: {
                            transactiondate: WtfGlobal.convertToGenericDate(this.creationdate.getValue()),
                            tocurrencyid: this.Currency.getValue()
                        }
                    },this,function(response){
                        if(response.count>0){
                            this.exchangeRate.setValue(response.data[0].exchangerate);
                            this.exchangeRate.disable();
                        }else{
                            this.exchangeRate.setRawValue("");
                            this.exchangeRate.disable();
                            this.Currency.reset();
                            callCurrencyExchangeWindow();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.creationdate.getValue())+"</b>"], 0);
                        }
                    });
                }else{
                    this.exchangeRate.setValue(1);
                    this.exchangeRate.disable();
                }
            }else{
                this.exchangeRate.setRawValue("");
                this.exchangeRate.disable();
            }
        }, this);
                
        this.subAccount=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.rem.19"),  //'Is a Sub Asset':'Is a subaccount?',
            checkboxToggle: true,
            autoHeight: true,
       //     width:150,
            border:false,
            disabledClass:"newtripcmbss",
            checkboxName: 'subaccount',
            style: 'margin-left:-10px',
            collapsed: true,
            items:[this.cmbParent]
        });
                
        this.MVATAnnexureCodes = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText('acc.field.mvatannexurecode'),//MVAT Annexure Code ERP-25719
            autoHeight: true,
            border:false,
            hideLabel: Wtf.Countryid != Wtf.Country.INDIA ?  true :( Wtf.Stateid != Wtf.StateName.MAHARASHTRA ? true : (Wtf.account.companyAccountPref.enablevatcst ? false : true)),
            hidden: Wtf.Countryid != Wtf.Country.INDIA ?  true :( Wtf.Stateid != Wtf.StateName.MAHARASHTRA ? true : (Wtf.account.companyAccountPref.enablevatcst ? false : true)),
            style: 'margin-left:-10px',
            items:[this.MVATAnnexureCodeCombo = new Wtf.common.Select(Wtf.applyIf({
                multiSelect:false,
                forceSelection:true,
                extraFields:['mvatannexurecode'],
                extraComparisionField:'mvatannexurecode', 
                extraComparisionFieldArray:['mvatdescription','mvatannexurecode'],
                listWidth:400,
                width:350
            },{
                fieldLabel: WtfGlobal.getLocaleText('acc.field.mvatannexurecode'),//Select MVAT Annexure Code
                hiddenName:'mvatcode',
                name: 'mvatcode',
                store: this.MVATAnnexureCodesStore,
                valueField:'mvatannexurecode',
                displayField:'mvatdescription',
                mode: 'local',
                allowBlank:true,
                //emptyText:'Select MVAT Annexure Code',
                typeAhead: true,                  
                triggerAction:'all',
                scope:this
            }))]
        });
        
        this.accGroup=new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.rem.18.1")+'*',
            hiddenName:'groupid',
            name:'groupid',
            store:this.groupStore,
            valueField:'groupid',
            disabledClass:"newtripcmbss",
            displayField:'groupname',
            typeAhead: true,
            forceSelection: true,
            width: this.fieldwidth,
            allowBlank: false,
            mode: 'local',
            disableKeyFilter:true,
            lastQuery : '',
            hirarchical:true,
            triggerAction:'all',
            listeners: {
                select: function (combo,rec) {
                    if (this.accountTypeCombo.getValue() == 1) {
                        if (rec.data.naturename == "Liability" || rec.data.naturename == "Asset")
                            return true;
                        else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Group does not belongs to <b>Balance Sheet</b>. Please select another group."],2);
                            this.accGroup.reset();
                        }
                    } else {
                        if (rec.data.naturename == "Income" || rec.data.naturename == "Expences")
                            return true;
                        else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Group does not belongs to <b>Profit & Loss</b>. Please select another group."],2);
                            this.accGroup.reset();
                        }
                    }
                },
                scope: this
            }
//            addNewFn:this.showGroupWindow.createDelegate(this)
        });
        this.typeOfPurchaseStore = new Wtf.data.SimpleStore({
            fields : ['idpurchasetype', 'namepurchasetype','codepurchasetype'],
            data : [
            ['Blank1', 'Import from Outside India',''],
            ['Blank2', 'High Seas Purchase',''],
            ['Blank3', 'Own Goods Received Back after job work against F-Form',''],
            ['PUR', 'Purchase from Unregistered Dealer','PUR'],
            ['PCD', 'Purchase from Composition Dealer','PCD'],
            ['PCG', 'Purchase against Non Creditable Goods','PCG'],
            ['PRI', 'Purchase Against Retail Invoices','PRI'],
            ['PTF', 'Purchase against Tax free Goods (Local)','PTF'],
            ['PLSWC', 'Labour & Services related to work contract','PLSWC'],
            ['PATI', 'Purchase against Tax invoices not eligible for ITC','PATI'],
            ['PDDH', 'Purchase from Delhi dealers against Form-H','PDDH'],
            ['PCGM', 'Purchase of Capital Goods(Used for manufacturing of non-creditable goods)','PCGM'],
            ['Blank4', 'Other Dealer Received for job work against F-form',''],
            ['Blank5', 'Capital Goods Purchase Against C-Form',''],
            ['Blank6', 'Goods (Other than capital goods) purchased against C-Forms',''],
            ['Blank7', 'Inter state Purchase against H-Form (other than Delhi dealers)',''],
            ['Blank8', 'Inter state Purchase without Forms',''],
            ['Blank9', 'Inward Stock Transfer (Branch) against F-Form',''],
            ['Blank10', 'Inward Stock Transfer (Consignment) against F-Form',''],
            ['Blank11', 'Local purchase -Eligible Capital Goods -Purchase Amount',''],
            ['PPD', 'For Purchase of Diesel & Petrol from Oil Marketing Companies in Delhi','PPD'],
            ['GD', 'Local purchase -Eligible Others -Purchase Amount (For Goods Taxable)','CG'],
            ['WC', 'Local purchase -Eligible Others -Purchase Amount (For Works Contract Taxable)','WC']
            ]
        });
        this.DVATnnexure2ATypeOftTransaction = new Wtf.form.FieldSet({
            title: 'Type of Purchase/Sale as per Annexure 2A/2B',
            autoHeight: true,
            border:false,
            hideLabel: true,//Wtf.Countryid != Wtf.Country.INDIA ?  true :( Wtf.Stateid != Wtf.StateName.DELHI ? true : (Wtf.account.companyAccountPref.enablevatcst ? false : true)),
            hidden: true,//Wtf.Countryid != Wtf.Country.INDIA ?  true :( Wtf.Stateid != Wtf.StateName.DELHI ? true : (Wtf.account.companyAccountPref.enablevatcst ? false : true)),
            style: 'margin-left:-10px',
            items:[this.typeOfPurchaseCombo = new Wtf.common.Select(Wtf.applyIf({
                multiSelect:false,
                forceSelection:true,
                extraFields:['codepurchasetype'],
                extraComparisionField:'codepurchasetype', 
                extraComparisionFieldArray:['namepurchasetype','codepurchasetype'],
                listWidth:400,
                width:350
            },{
                fieldLabel: 'Select type of transaction',
                hiddenName:'purchasetype',
                name: 'purchasetype',
                store: this.typeOfPurchaseStore,
                displayField:'namepurchasetype',
                valueField:'idpurchasetype',
                mode: 'local',
                allowBlank:true,
                typeAhead: true,                  
                triggerAction:'all',
                scope:this
            }))]
        });
        
        /*This is combo of Transaction Description at the Account Creation Level for DVAT Form 31 */
        
        this.typeOfSalesStore = new Wtf.data.SimpleStore({
            fields : ['idsalessetype', 'namesalestype'],
            data : [
            ['1', 'Export'],
            ['2', 'High Sea Sale'],
            ['3', 'Own goods transferrd for Job Work against F-Form'],
            ['4', 'Other dealers goods returned after Job work against F-Form'],
            ['5', 'Stock transfer (Branch) against F- Form'],
            ['6', 'Stock transfer (Consignment) against F- Form'],
            ['7', 'Sale against H- Form'],
            ['8', 'Sale against I- Form'],
            ['9', 'Sale against J- Form'],
            ['10', 'Sale against C+E- I/E-II'],
            ['11', 'Sale of Exempted Goods [Sch. I]'],
            ['12', 'Sales covered under proviso to [Sec.9( 1)] Read with Sec.8(4)]'],
            ['13', 'Sales of Goods Outside Delhi (Sec. 4)'],
            ['14', 'Sale against C-Form excluding sale of capital assets'],
            ['15', 'Capital Goods sold against C- Forms'],
            ['16', 'Sale without forms'],
            ['17', 'Turnover (Goods) (excluding VAT)'],
            ['18', 'Turnover (WC) (excluding VAT)'],
            ['19', 'Charges towards labour, services and other like charges, in civil works contracts'],
            ['20', 'Charges towards cost of land, if any, in civil works contracts'],
            ['21', 'Sale against H-Form to Delhi dealers'],
            ['22', 'Sale of Petrol/Diesel suffered tax on full sale price at OMC level']
            ]
        });
        this.DVATForm31ypeOftTransaction = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.report.DVATForm31.TransactionDescDVAT31"),//'Transaction Description as per D VAT 31',
            autoHeight: true,
            border:false,
            hideLabel: true,//Wtf.Countryid != Wtf.Country.INDIA ?  true :( Wtf.Stateid != Wtf.StateName.DELHI ? true : (Wtf.account.companyAccountPref.enablevatcst ? false : true)),
            hidden: true,//Wtf.Countryid != Wtf.Country.INDIA ?  true :( Wtf.Stateid != Wtf.StateName.DELHI ? true : (Wtf.account.companyAccountPref.enablevatcst ? false : true)),
            style: 'margin-left:-10px',
            items:[this.typeOfSalesCombo = new Wtf.common.Select(Wtf.applyIf({
                multiSelect:false,
                forceSelection:true,
                listWidth:400,
                width:350
            },{
                fieldLabel: WtfGlobal.getLocaleText("acc.report.DVATForm31.Selecttypeoftransaction"),//'Select type of transaction',
                hiddenName:'salestype',
                name: 'salestype',
                store: this.typeOfSalesStore,
                displayField:'namesalestype',
                valueField:'idsalessetype',
                mode: 'local',
                allowBlank:true,
                typeAhead: true,                  
                triggerAction:'all',
                scope:this
            }))]
        });
        this.accountTypeStore = new Wtf.data.SimpleStore({
            fields : ['id', 'name'],
            data : [
            ['1', 'Balance Sheet'],
            ['0', 'Profit & Loss']
            ]
        });
        /**
         * If Country id = 182(PHILIPPINES)
         * this.radioGST box label will be TAX insted of GST ERP-41392.
         */
        this.radioGSTBoxLabel = WtfGlobal.getLocaleText("coa.masterType.GST");
        if (Wtf.Countryid == Wtf.Country.INDIA || Wtf.Countryid == Wtf.Country.PHILIPPINES) {
            this.radioGSTBoxLabel = Wtf.Countryid == Wtf.Country.INDIA ? WtfGlobal.getLocaleText("coa.masterType.dutiesntaxes") : WtfGlobal.getLocaleText("acc.het.381");//Tax
        }
        this.masterType= new Wtf.form.FieldSet({
                xtype:'fieldset',
                title: WtfGlobal.getLocaleText("coa.masterType.title"),  //'Page Border',
//                disabled:(this.isEdit),
                disabledClass:"newtripcmbss",
                autoHeight : true,
                items:[
                this.radioGL = new Wtf.form.Radio({
                 name:'mastertype',
                id:'gl'+this.id,
//                disabled:this.isEdit,
                height : 22,
                labelAlign : 'left',
                inputValue :'false',
                labelSeparator:'',
                checked:true,
                boxLabel:WtfGlobal.getLocaleText("coa.masterType.GL")  //'No Border'
            }),
            this.radioCash = new Wtf.form.Radio({
                id:'cash'+this.id,
                height : 22,
                name:'mastertype',
//                disabled:this.isEdit,
                labelAlign : 'left',
                inputValue :'true',
                boxLabel:WtfGlobal.getLocaleText("coa.masterType.cash"), 
                labelSeparator:''
                
            }),
            this.radioBank = new Wtf.form.Radio({
                name:'mastertype',
                id:'bank'+this.id,
                inputValue :'false',
//                disabled:this.isEdit,
                height : 22,
                labelAlign : 'left',
                labelSeparator:'',
                boxLabel:WtfGlobal.getLocaleText("coa.masterType.bank")  
            }), this.radioGST = new Wtf.form.Radio({
                name:'mastertype',
                height : 22,
                id:'gst'+this.id,
                inputValue :'false',
//                disabled:this.isEdit,
                labelAlign : 'left',
                labelSeparator:'',
                checked:this.islibility,
                boxLabel:this.radioGSTBoxLabel
            })  
            ]
        });
         if(this.radioGL && this.radioGST)
        {
            this.Currency.setDisabled(true);
        }else 
        {
            this.Currency.enabled();

        }
         this.radioGL.on('change',this.radioGlchange ,this);
         this.radioCash.on('change',this.radioCashchange,this); 
         this.radioBank.on('change',this.radioBankchange,this);   
         this.radioGST.on('change',this.radioGSTchange,this);   
        this.accountTypeCombo= new Wtf.form.ComboBox({
            fieldLabel:'Type',
            name: 'accounttype',
            forceSelection:true,
            triggerAction:'all',
            editable:false,
            displayField:'name',
            valueField:'id',
            disabledClass:"newtripcmbss",
            store:this.accountTypeStore,
            mode:'local',
            value:1,
            width: this.fieldwidth
        });
        
        this.accountTypeCombo.on("select",function(id,name,ii){
            if(this.cmbParent.getValue()!="" && this.cmbParent.getValue()!=undefined){ // When account has assigned parent account
                // Here checking If user selected Type other than Parent account then giving alert msg
                var parentRecord = this.getParentAccountRecord();
                if(parentRecord!=null){
                    if(parentRecord.data.accounttype!= this.accountTypeCombo.getValue()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("coa.type.wrongtypeselected")],2);
                        this.accountTypeCombo.setValue(parentRecord.data.accounttype);
                        return;
                    }
                }
            }
            
            this.radioGL.setValue(true);
            this.accGroup.reset();
            
            this.groupStore.filterBy(function(rec){
                if(ii==0){
                    if(rec.data.naturename=="Liability" || rec.data.naturename=="Asset")
                        return true
                    else
                        return false
                }else{
                    if(rec.data.naturename=="Income" || rec.data.naturename=="Expences")
                        return true
                    else
                        return false
                } 
            },this); 

            if(this.accountTypeCombo.getValue()==0){
                this.radioGL.setDisabled(false);
                this.radioCash.setDisabled(true);
                this.radioBank.setDisabled(true);
                this.radioGST.setDisabled(true);
            }else{
                this.radioGL.setDisabled(false);
                this.radioCash.setDisabled(false);
                this.radioBank.setDisabled(false);
                this.radioGST.setDisabled(false);  
            }
        },this);


        if(!WtfGlobal.EnableDisable(Wtf.UPerm.groups, Wtf.Perm.groups.create))
            this.accGroup.addNewFn= this.showGroupWindow.createDelegate(this)
        this.accCode.on('change',this.checkDuplicateAccountCode ,this);
        if(SATSCOMPANY_ID!=companyid){
            this.aliasCode.on('change',this.checkDuplicateAliasCode ,this);
        }
        this.accGroup.on('select',function(c,rec){this.setOBType(rec.data["nature"]);this.tagsFieldset.setOBType(rec.data["nature"]);},this);
        this.accGroup.on('select',function(field, newvalue, oldvalue){
            this.checkGroup(field, newvalue, oldvalue,false);
        },this);
        this.cmbParent.on('select',this.onParentAccoluntSelect,this);
        this.cmbParent.on('beforeselect',function(combo,record,index){
            this.isParentAccountBeforeSelect=combo.getValue();
        },this);
        this.subAccount.on('collapse',this.enableDisableCombo.createDelegate(this,[false]),this);
        this.subAccount.on('expand',this.enableDisableCombo.createDelegate(this,[false]),this);


        this.balTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'boolean'}, 'name'],
            data :[[true,'Debit'],[false,'Credit']]
        });
        this.balTypeEditor = new Wtf.form.ComboBox({
            store: this.balTypeStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.coa.gridOpeningBalanceType"),
            name:'debitType',
            displayField:'name',
            forceSelection: true,
            allowBlank:false,
            width: this.fieldwidth,
            valueField:'typeid',
            mode: 'local',
            value:true,
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true
        });

        this.openingBal= new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance"),       // in "+WtfGlobal.getCurrencySymbolForForm()),
            name: 'openbalance',
            width: this.fieldwidth,
            maxLength:15,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            allowBlank:false,
            allowNegative:false,
            xtype:'numberfield'
        });
       this.openingBal.setValue(0);
        
        if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount){
            this.openingBal.on('change', function(field, newvalue, oldvalue){
                this.tagsFieldset.presentbalance = this.openingBal.getValue();
            }, this);
        }
        
        this.ibgBank=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.coa.ibgBank"), // "IBG Bank"
            name:'isibgbank',
            checked:false,
//            hideLabel:!Wtf.account.companyAccountPref.activateIBG,
//            hidden:!Wtf.account.companyAccountPref.activateIBG,
            cls : 'custcheckbox',
            width: 10,
            disabled:true
        });
        
        this.ibgBank.on('change', this.ibgBankChecked, this);
        
        this.viewibgButton = new Wtf.Button({
            disabled:this.isEdit? ((this.record.data.isibgbank == true)? false:true) : true,
//            hidden:!Wtf.account.companyAccountPref.activateIBG && !Wtf.account.companyAccountPref.activateIBGCollection,
            text:WtfGlobal.getLocaleText("acc.lp.viewccd"), // 'View Details',
            scope:this,
            handler:this.viewIBGBankDetailHandler
        });
        
        this.fsIbgBankDetail= new Wtf.form.FieldSet({
                xtype:'fieldset',
                hidden:!Wtf.account.companyAccountPref.activateIBG && !Wtf.account.companyAccountPref.activateIBGCollection,
                title: WtfGlobal.getLocaleText("acc.field.ibgBankDetail"), // 'IBG Bank Detils'
                autoHeight : true,
                items:[this.ibgBank,this.viewibgButton]
        });
        
        this.usersRec = new Wtf.data.Record.create([
            {name: 'userid'},
            {name: 'username'},
            {name: 'fname'},
            {name: 'lname'},
            {name: 'image'},
            {name: 'emailid'},
            {name: 'lastlogin',type: 'date'},
            {name: 'aboutuser'},
            {name: 'address'},
            {name: 'contactno'},
            {name: 'rolename'},
            {name: 'roleid'}
        ]);

        this.userds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            },this.usersRec),
            url : "ProfileHandler/getAllUserDetails.do",
            baseParams:{
                mode:11
            }
        });

        this.userds.load();

        this.users= new Wtf.form.ComboBox({     
            fieldLabel : WtfGlobal.getLocaleText("acc.field.SelectUser"),
            triggerAction:'all',
            mode: 'local',
            selectOnFocus:true,
            valueField:'userid',
            displayField:'fname',
            store:this.userds,     
            width: this.fieldwidth,
            typeAhead: true,
            forceSelection: true,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectUser"),
            name:'username',
            hiddenName:'username'            
        });
        this.bankBranchName = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.bankBranchName"),  
            name:'bankbranchname',
            width:this.fieldwidth,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.setupWizard.bankBranchName")
            
        });
        this.bankAccountNo = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.AccountNo"),  //"Account No",
            width:this.fieldwidth,
            maxLength:30,
            emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterAccountNo"),  //"Enter Account No",
            //                    allowBlank: false,
            name:"accountno",
            regex:Wtf.specialChar
         })
        this.bankBranchAddress = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.bankBranchAddress"),  
            name:'bankbranchaddress',
            width:this.fieldwidth,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.setupWizard.bankBranchAddress")
        });
        this.bsrCode = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.bsrCode"),  
            name:'bsrcode',
            maskRe:/\d/,//To allow only number field.
            width:this.fieldwidth,
            allowNegative: false,
            maxLength:7,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.setupWizard.bsrCode")+" of Bank Branch"
        });
        Wtf.stateStore.load({
                    params:{
                        countryid: Wtf.account.companyAccountPref.countryid 
                    }
                });
        this.branchState =new Wtf.form.ComboBox({
            store: Wtf.stateStore,
            width:this.fieldwidth-17,
            id:'branchstate',
            name:'branchstate',
            listWidth:this.fieldwidth-17,
            labelWidth:80,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.State"),
            displayField:'name',
            valueField:'id',
            value:Wtf.account.companyAccountPref.state,
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.field.State"),
            selectOnFocus:true,
            forceSelection: true
        });
        this.branchPincode = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.pincode"),  
            name:'pincode',
            width:this.fieldwidth,
            allowNegative: false,
            maxLength:6,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.setupWizard.pincode")
        });
        this.mailingDetailsSetup=new Wtf.form.FieldSet({
            title: "Bank Mailing Details",  //'Do you want to track Inventory in Deskera Accounting?',
            autoHeight: true,
            cls: "wiz-card6-fieldset",
            border: false,
            items:[this.bankAccountNo, this.bankBranchName,this.bsrCode,this.ifsccode,this.bankBranchAddress,this.branchState,this.branchPincode]
        });
        this.mailingDetailsSetup.hide();
    },
    
    getParentAccountRecord : function(){
        var parentAccountID = this.cmbParent.getValue();
        var parentRecord=WtfGlobal.searchRecord(this.cmbParent.store, parentAccountID, 'parentid');
        return parentRecord;
    },
    
    checkParentMasterType:function(selectedMasterType){
        var masterAccountRecord=this.getParentAccountRecord();
        if(masterAccountRecord!=null && masterAccountRecord.data.mastertypevalue!=selectedMasterType){// when selected mastertype not same as parent master account then giving alert message to user and reset master aaccount master type
            var masteraccountMasterType=masterAccountRecord.data.mastertypevalue;
            if(masteraccountMasterType==1){
                this.radioGL.setValue(true);
            } else if(masteraccountMasterType==2){
                this.radioCash.setValue(true);
            } else if(masteraccountMasterType==3){
                this.radioBank.setValue(true);
            } else if(masteraccountMasterType==4){
                this.radioGST.setValue(true);
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("coa.masterType.wrongmastertypeselected")],2);
            return false;;
        }
        return true;
    },
    
    sortAccountGroups: function(){
            this.groupStore.filterBy(function(rec){
                if(this.accountTypeCombo.getValue()==1){
                    if(rec.data.naturename=="Liability" || rec.data.naturename=="Asset")
                        return true
                    else
                        return false
                }else{
                    if(rec.data.naturename=="Income" || rec.data.naturename=="Expences")
                        return true
                    else
                        return false
                } 
        },this);  
    },
    
     ApplyCurrencySymbol: function() {
        var index = this.getCurrencySymbol();
        if (index >= 0) {
            this.tagsFieldset.setCurrencyid(this.currencyid, this.symbol, index);
//            this.applyTemplate(this.currencyStore, index);
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
    
    enableDisableFields:function(){ 
        if(this.taxCombo){
            this.taxCombo.disable();
        }
        if(this.cmbParent){
            this.cmbParent.disable();
        }
        if( this.accountTypeCombo){
             this.accountTypeCombo.disable();
        }
        if(this.aliasCode){
            this.aliasCode.disable();
        }
        if(this.custMinBudget){
            this.custMinBudget.disable();
        }
        if(this.AccName){
            this.AccName.disable();
        }
        if(this.cmbParent){
            this.cmbParent.disable();
        }
        if(this.creationdate){
            this.creationdate.disable();
        }
        if(this.subAccount){
            this.subAccount.disable();
        }
        if(this.tagsFieldset){
            this.tagsFieldset.disable();
        }
        if(this.accGroup){
            this.accGroup.disable();
        }
        if(this.masterType){
            this.masterType.disable();
        }
    },
    
    genPurchaseSucess:function(response){
    	this.cmbPaymentAccount.setValue(response.purchaseAccount);
    },

    searchText:function(cAccTypeStore,pan,cmpName,cmpRecordField){
        document.getElementById(cmpName).onkeyup=function(){
            this.text=Wtf.getCmp(cmpName).getValue();
            if(this.text=="" && this.tpl != undefined){
            	this.tpl.overwrite(pan.body,"");	// 20140 fixed
//                pan.hide();
            }
            else{
                if(cmpRecordField=="parentname")
                {
                    pan.setTitle(WtfGlobal.getLocaleText("acc.field.ExistingAccountsName"));
                    cAccTypeStore.filterBy(function(rec){
                        var FIND = String(this.text);
                        FIND = new RegExp('^'+Wtf.escapeRe(FIND), 'i');
                        return rec && FIND.test(rec.data.parentname) && !rec.data.deleted;
                    },this);
                }else if(cmpRecordField=="code")
                {
                    pan.setTitle(WtfGlobal.getLocaleText("acc.field.ExistingAccountsCode"));
                    cAccTypeStore.filterBy(function(rec){
                        var FIND = String(this.text);
                        FIND = new RegExp('^'+Wtf.escapeRe(FIND), 'i');
                        return rec && FIND.test(rec.data.code) && !rec.data.deleted;
                    },this);
                }else if(cmpRecordField=="alscode"){
                    pan.setTitle(WtfGlobal.getLocaleText("acc.field.ExistingAliasCode"));
                    cAccTypeStore.filterBy(function(rec){
                        var FIND = String(this.text);
                        FIND = new RegExp('^'+Wtf.escapeRe(FIND), 'i');
                        return rec && FIND.test(rec.data.alscode) && !rec.data.deleted;
                    },this);
                }

                this.len=cAccTypeStore.getCount();
                if(this.len==0 && this.tpl != undefined){
                	this.tpl.overwrite(pan.body,"");	// 20140 fixed
//                    pan.hide();
                }
                for(var i=0;i<this.len;i++){
                    this.presentAcc=cAccTypeStore.getAt(i).get(cmpRecordField);
                    this.tdata={
                        paname:this.presentAcc
                    }
                    this.tpl=new Wtf.Template('<font size=2> <p>{paname}</p></font>');
                    if(i==0)
                        this.tpl.overwrite(pan.body,this.tdata);
                    else
                        this.tpl.append(pan.body,this.tdata);

                    pan.show();
                }
            }
        };
    },
    
    addMaster:function(id,store){
        addMasterItemWindow(id);
        Wtf.getCmp('masterconfiguration').on('update', function(){
            store.reload();
        }, this);
    },
    
    setOBType:function(nature){
        this.balTypeEditor.setValue(nature==Wtf.account.nature.Asset||nature==Wtf.account.nature.Expences);
    },

    ibgBankChecked : function() {
        if(this.ibgBank.getValue()) {
            this.Currency.setValue(6);
            this.Currency.disable();
            this.viewibgButton.enable();
            
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.msg.confirmationMsgforIBGDetails"),
                buttons: Wtf.MessageBox.YESNO,
                fn:function(btn){
                    if(btn!="yes") {
                        this.ibgBank.setValue(false);
                        this.viewibgButton.disable();
                        this.Currency.enable();
                        return;
                    }
                    
                    callIBGbankDetailsWin();
                    
                    Wtf.getCmp('ibgBankDetailsWin').on('update',function(config) {
                        this.ibgBankDetailRec=null;
                        this.ibgBankDetailRec = config.ibgBankDetail;
                        this.ibgbankdetailid = config.ibgbankdetailid;
                        this.cimbbankdetailid = config.cimbbankdetailid;
                        this.ibgbanktype = config.ibgbanktype;
                    },this);
                    
                    Wtf.getCmp('ibgBankDetailsWin').on('cancel',function(config) {
                        this.ibgBank.setValue(false);
                        this.viewibgButton.disable();
                    },this);
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
        } else {
            this.Currency.enable();
            this.viewibgButton.disable();
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), // 'Warning',
                msg: WtfGlobal.getLocaleText("acc.msg.confirmationMsgforIBGDetailsDataLoss"),
                buttons: Wtf.MessageBox.YESNO,
                fn:function(btn){
                    if(btn!="yes") {
                        this.ibgBank.setValue(true);
                        this.viewibgButton.enable();
                        this.Currency.setValue(6);
                        this.Currency.disable();
                        return;
                    }
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
        }
    },
    
    viewIBGBankDetailHandler : function() {
        if(this.ibgBankDetailRec != "") {
            callIBGbankDetailsWin("ibgBankDetailsWin",true,false,this.ibgBankDetailRec,this.isEdit);
        } else {
            callIBGbankDetailsWin("ibgBankDetailsWin",true,false,this.record,this.isEdit);
        }
        
        Wtf.getCmp('ibgBankDetailsWin').on('update',function(config) {
            this.ibgBankDetailRec=null;
            this.ibgBankDetailRec = config.ibgBankDetail;
            this.ibgbankdetailid = config.ibgbankdetailid;
            this.cimbbankdetailid = config.cimbbankdetailid;
            this.uobbankdetailid = config.uobbankdetailid;
            this.ocbcbankdetailid = config.ocbcbankdetailid;
            this.ibgbanktype = config.ibgbanktype;
                        
        },this);
    },
    
    checkForFirstFinancialYearDate : function(dF,newVal,oldVal){
        if(Wtf.account.companyAccountPref.bbfrom && this.creationdate){
            var creationDate=WtfGlobal.getDayMonthYearDate(this.creationdate.getValue());
            var bbDate=WtfGlobal.getDayMonthYearDate(Wtf.account.companyAccountPref.bbfrom);
            if(creationDate>bbDate){
                //If account having opening balance enter and changed date then check for Book Beginning Date.
                if(this.openingBal.getValue()!=0 ){
                    if(!(this.isEdit && this.record!=null && this.record.data.accountHasOpeningTransactions)){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Openingbalancecanneverbenonzero")],2);
                        this.creationdate.setValue(oldVal);
                    }
                }else{
                    this.openingBal.disable();
                }
            }else{
                if(this.isEdit){
                    if(this.record!=null && !this.record.data.accountHasOpeningTransactions && !this.record.data.accountUsedAsInventoryAccountInProduct){
                        this.openingBal.enable();
                    }else{
                        this.openingBal.disable();
                    }
                }else{
                    this.openingBal.enable();
                }
            }
        }
    },
    onParentAccoluntSelect:function(combo,record,index){
        if(this.isEdit){// below block ony execute for edit case
            var accountType = this.accountTypeCombo.getValue();
            var groupID=this.accGroup.getValue();
            var accountMasterTypeValue = 1;
            if(this.radioGL.getValue()){
                accountMasterTypeValue=1;
            } else if(this.radioCash.getValue()){
                accountMasterTypeValue=2
            } else if(this.radioBank.getValue()){
                accountMasterTypeValue=3
            } else if(this.radioGST.getValue()){
                accountMasterTypeValue=4
            }
            
            var parentAccountsAccountType= record.data.accounttype;
            var paentMasterTypeValue = record.data.mastertypevalue;
            var paentGroupID = record.data.mastergroupid;
            var parentGroupNature = record.data.nature;
            var groupRec = WtfGlobal.searchRecord(this.accGroup.store, groupID, 'groupid')
            var accountGroupNature = groupRec ? groupRec.data.nature : 0;     
            if(accountType != parentAccountsAccountType || accountMasterTypeValue != paentMasterTypeValue || accountGroupNature != parentGroupNature){
                var accountName=record.data.parentname;
                var msg=WtfGlobal.getLocaleText("coa.type.youcannotselect")+" <b>"+accountName+"</b> "+WtfGlobal.getLocaleText("coa.type.astypemastertypegroupdifferent");
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                this.cmbParent.setValue(this.isParentAccountBeforeSelect);
                return;
            }
            this.checkGroup(this.accGroup, record.data.mastergroupid, undefined,true);
        } else{
            if(record && record.data){
                this.checkGroup(this.accGroup, record.data.mastergroupid, undefined,true);
            } else {
                this.checkGroup(this.accGroup, undefined, undefined,true);
            }
        }
    },
    
    checkGroup:function(field, newvalue, oldvalue, isParentSelect){
        var temp=this.coaForm.getForm().getValues();
        if(!temp.subaccount){
          //  this.accGroup.store.clearFilter(true); //ERP-13162[SJ]
            return;   
        }
        var parentID=this.cmbParent.getValue();
        var rec =this.cmbParent.store.findBy(function(record) {
                    if(record.data.parentid==parentID)
                        return true;
                    else
                        return false;
                });        
        if(rec==-1)return;
        rec=this.cmbParent.store.getAt(rec);
        var mastergroupid=rec.data['mastergroupid'];  
        var parentAccGrpNature=rec.data.nature;           
        if((this.isEdit==false)||(this.isEdit==true && !this.record.data.isHeaderAccount)){ //if edit is true then check header account and fixed asset
            this.accGroup.store.filterBy(function(rec){
                if(rec.data.nature==parentAccGrpNature)
                    return true
                else
                    return false
            },this);
            if(temp.subaccount){
                this.accountTypeCombo.setValue(rec.data['accounttype']);
                if(this.accountTypeCombo.getValue()==0){
                    this.radioGL.setDisabled(false);
                    this.radioCash.setDisabled(true);
                    this.radioBank.setDisabled(true);
                    this.radioGST.setDisabled(true);
                }else{
                    this.radioGL.setDisabled(false);
                    this.radioCash.setDisabled(false);
                    this.radioBank.setDisabled(false);
                    this.radioGST.setDisabled(false);  
                }
                if(rec.data['mastertypevalue']==1){
                    this.radioGL.setValue(true);
                    this.Currency.setDisabled(true);
                }else if(rec.data['mastertypevalue']==2){
                    this.radioCash.setValue(true);
                    this.Currency.setDisabled(false);
                }else if(rec.data['mastertypevalue']==3){
                    this.radioBank.setValue(true);
                    this.Currency.setDisabled(false);
                    this.ibgBank.enable();
                }else if(rec.data['mastertypevalue']==4){
                    this.radioGST.setValue(true);
                    this.Currency.setDisabled(true);
                }
                var groupRec = WtfGlobal.searchRecord(this.accGroup.store, newvalue, 'groupid');
                if(groupRec && groupRec.data){
                    var selectGroupNature=rec.data.nature; 
                    if((selectGroupNature != parentAccGrpNature) || (isParentSelect && selectGroupNature == parentAccGrpNature)){
                        this.accGroup.setValue(mastergroupid);
                    } 
                }
            }
            this.setOBType(rec.data["nature"]);   
            this.tagsFieldset.setOBType(rec.data["nature"]);
            var groupID=this.accGroup.getValue();
            var grouprec =this.accGroup.store.findBy(function(record) {
                        if(record.data.groupid==groupID)
                            return true;
                        else
                            return false;
                    });        
            if(grouprec==-1){
                this.accGroup.setValue("");
            }
        }         
    },

    enableDisableCombo:function(disabled){        
        if(this.isEdit==true){     
            if(this.record!=null && !this.record.data.isHeaderAccount && !this.isotherexpense && !this.issales && !this.ispurchase){
                this.accGroup.setDisabled(disabled);
            }               
        }else{
            if(!this.isotherexpense && !this.issales && !this.ispurchase){			// issue 20495 fixed
                this.accGroup.setDisabled(disabled);
            }
        }                    
        this.cmbParent.setValue("");
        this.checkGroup();        
    },

    validateCard:function(){
        var flag=true;
        var val;
        var temp=this.coaForm.getForm().getValues();
        if(temp.subaccount){
            val=this.cmbParent.getValue();
            if(val.length<=0){
                this.cmbParent.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
                flag=false;
            }
        }
        return flag;
    },

    createPanel:function(){
        this.pan=new Wtf.Panel({
            height:100,
            width: this.fieldwidth,
            style:"padding:20px 13px 30px 0px;",
            autoScroll:true,
            title:WtfGlobal.getLocaleText("acc.nee.10")
        });
        this.pan.hide();
    },
    
    createForm:function(){
        this.coaForm=new Wtf.form.FormPanel({
            region:'center',
//            width: Wtf.account.companyAccountPref.splitOpeningBalanceAmount ? 700 : 530,
            width: 760,
            autoHeight:true,
            labelWidth:135,
            autoScroll : true,
            border:false,
            bodyStyle: "background:transparent; padding: 20px 10px 0px 10px",
            style: "background: transparent;padding-left:15px;",
            defaultType: 'textfield',
            items:[{
                xtype:'hidden',
                name:'accid'
            },this.accCode,this.aliasCode,this.AccName,this.AccDesc,this.subAccount,this.DVATnnexure2ATypeOftTransaction,this.DVATForm31ypeOftTransaction,this.MVATAnnexureCodes,this.accountTypeCombo,this.masterType,this.accGroup,this.openingBal, this.exchangeRate, this.Currency,this.creationdate,this.balTypeEditor,this.users,this.taxCombo,this.custMinBudget,
            this.tagsFieldset, this.fsIbgBankDetail,this.mailingDetailsSetup,]
        });
        this.exchangeRate.disable();
    },
    closeForm:function(){
        this.fireEvent('cancel',this);
         this.close();
    },
      confirmBeforeSave: function () {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {

            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText({key: "acc.savecustomer.propagate.confirmmessage", params: ["Account"]}),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.QUESTION,
                width: 300,
                scope: {
                    scopeObject: this
                },
                fn: function (btn) {
                    if (btn == "yes") {
                        this.scopeObject.ispropagatetochildcompanyflag = true;
                    }
                    this.scopeObject.saveForm();
                }
            }, this);

        } else {
            this.saveForm();
        }
    },   
    saveForm:function(){
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();        
        this.AccName.setValue(this.AccName.getValue().trim());
        this.accCode.setValue(this.accCode.getValue().trim());
        var flag1=this.coaForm.getForm().isValid();
        var flag2=this.validateCard();
 
        if(!flag1||!flag2 || !isValidCustomFields){
            WtfComMsgBox(2,2);
        }else{
            if (Wtf.isBookClosed) {
                //  ERP-40285  Show alert if book(s) are closed and using is trying to add/update opening balance for account.
                if (this.isEdit && this.openingBal.getValue() != this.record.data.orignalopenbalance) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.account.openingbalance.cannotbeupdated.if.fyclosed")], 2);
                    return;
                } else if (this.openingBal.getValue() != 0) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.account.openingbalance.cannotbeadded.if.fyclosed")], 2);
                    return;
                }
            }
            var financialYearDate;
            if(Wtf.account.companyAccountPref.fyfrom){
                financialYearDate = Wtf.account.companyAccountPref.fyfrom.format(WtfGlobal.getOnlyDateFormat());
            }
            var bookBeginningDate;
            if(Wtf.account.companyAccountPref.bbfrom){
                bookBeginningDate = Wtf.account.companyAccountPref.bbfrom.format(WtfGlobal.getOnlyDateFormat());
            }
            var accountNature = this.accGroup.store.getAt(this.accGroup.store.find("groupid",this.accGroup.getValue())).data["nature"];
            //accountNature ===> 0: Liability, 1: Asset
            if(financialYearDate == bookBeginningDate && accountNature != 0 && accountNature != 1 && this.openingBal.getValue() && this.openingBal.getValue() > 0){

                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.field.Theconversionmonthisthefirstmonth"),
                    buttons: Wtf.MessageBox.YESNO,
                    fn:function(btn){
                        if(btn!="yes")return;
                        this.saveFormData();
                    },
                    scope:this,
                    icon: Wtf.MessageBox.QUESTION
                });

            }else{
                this.saveFormData();
            }

        }
    },

    saveFormData: function(){
        this.loadMask1 = new Wtf.LoadMask(this.id, {
            msg: WtfGlobal.getLocaleText("acc.msgbox.49"), 
            msgCls: "x-mask-loading acc-customer-form-mask"
        });
        var custFieldArr=this.tagsFieldset.createFieldValuesArray();
        var custDistributedOpeningBalance = undefined;
        var custDistributedDeleteField = undefined;
        if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount){  
            var returnStr = '';
            for (var i = 0; i < this.tagsFieldset.distributeOpeningBalanceFields.length; i++) {
                var tAmt = 0;
                if(this.tagsFieldset.distributeOpeningBalanceValues.length>0){
                    for (var j = 0; j < this.tagsFieldset.distributeOpeningBalanceValues.length; j++) {
                        if(this.tagsFieldset.distributeOpeningBalanceFields[i][0] == this.tagsFieldset.distributeOpeningBalanceValues[j][3]  ){
                            if(this.tagsFieldset.distributeOpeningBalanceValues[j][2]){
                                tAmt += this.tagsFieldset.distributeOpeningBalanceValues[j][1];
                            }else{
                                tAmt -= this.tagsFieldset.distributeOpeningBalanceValues[j][1];
                            }
                        }
                    }
               }else{
                    for (var j = 0; j < this.tagsFieldset.tempdistributeOpeningBalanceValues.length; j++) {
                        if(this.tagsFieldset.distributeOpeningBalanceFields[i][0] == this.tagsFieldset.tempdistributeOpeningBalanceValues[j][3]){
                            if(this.tagsFieldset.tempdistributeOpeningBalanceValues[j][2]){
                                tAmt += this.tagsFieldset.tempdistributeOpeningBalanceValues[j][1];
                            }else{
                                tAmt -= this.tagsFieldset.tempdistributeOpeningBalanceValues[j][1];
                            }
                        }
                    }
               }

                var accountopeningbalance = this.openingBal.getValue();
                if(!this.balTypeEditor.getValue()){
                    accountopeningbalance = accountopeningbalance *(-1);
                }
                tAmt = getRoundedAmountValue(tAmt); //To rounds the sum of all dimensions.
                if(this.tagsFieldset.distributeOpeningBalanceFields[i][2] && tAmt!= accountopeningbalance ){
                    returnStr += this.tagsFieldset.distributeOpeningBalanceFields[i][1] + ',';
                }
            }

            if(returnStr!=''){
                returnStr = returnStr.substring(returnStr, returnStr.length-1);
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DistributeOpeningBalanceAlert.msg") + ' for <b>' + returnStr + '</b>'], 2);
            }
            if(returnStr!=''){
                return;
            }
            custDistributedOpeningBalance = this.tagsFieldset.getDistributedOpeningBalance();
            custDistributedDeleteField = this.tagsFieldset.getDistributedDeleteFieldIds();
        }
        this.loadMask1.show();
        Wtf.getCmp(this.id).on("loadingcomplete",function(){
            this.loadMask1.msg = WtfGlobal.getLocaleText("acc.field.Saved...") ;
            this.loadMask1.hide()
        },this);
        var rec=this.coaForm.getForm().getValues();
        rec.acccode=this.accCode.getValue();
        rec.subaccount=(rec.subaccount=="on"?true:false);
        rec.groupid=this.accGroup.getValue();
        rec.openbalance=this.openingBal.getValue();
        rec.custminbudget=this.custMinBudget.getValue();
        if(this.ifsccode){
            rec.ifsccode=this.ifsccode.getValue();
        }
        rec.currencyid=this.Currency.getValue();//WtfGlobal.getCurrencyID(),
        rec.creationDate=WtfGlobal.convertToGenericDate(this.creationdate.getValue());
        rec.mode=3;
        if(this.radioCash.getValue()){
            rec.mastertypevalue=2;
        }else if(this.radioBank.getValue()){
            rec.mastertypevalue=3;
        }else if(this.radioGL.getValue()){
            rec.mastertypevalue=1;
        }else if(this.radioGST.getValue()){
            rec.mastertypevalue=4;
        }
        if((Wtf.Countryid==Wtf.Country.INDIA && Wtf.Stateid==Wtf.StateName.MAHARASHTRA) &&  Wtf.account.companyAccountPref.enablevatcst){
            rec.mvatcode = this.MVATAnnexureCodeCombo.getValue();
        }
        rec.accounttype=this.accountTypeCombo.getValue();
        rec.purchasetype=this.typeOfPurchaseCombo.getValue();
        rec.salestype=this.typeOfSalesCombo.getValue();
        rec.parentid=this.cmbParent.getValue();
        rec.parentname=this.cmbParent.getRawValue();
        if(this.record != null && this.record != undefined){
            rec.oldUser = this.record.get('userid');
        }
        if (custFieldArr.length > 0){
            rec.customfield = JSON.stringify(custFieldArr);
        }
        if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount && custDistributedOpeningBalance.length > 0){
            rec.distributedopeningbalance = JSON.stringify(custDistributedOpeningBalance);
        }
        if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount && custDistributedDeleteField.length > 0){
            rec.distributeddeletefield = JSON.stringify(custDistributedDeleteField);
        }
        rec.userid=this.users.getValue();
        rec.debitType=this.balTypeEditor.getValue();
        rec.taxid =this.taxCombo.getValue();            
        rec.isibgbank = this.ibgBank.getValue();
            rec.bankbranchname = this.bankBranchName.getValue();
            rec.accountno = this.bankAccountNo.getValue();
            rec.bankbranchaddress = this.bankBranchAddress.getValue();
            rec.branchstate = this.branchState.getValue();
            rec.bsrcode = this.bsrCode.getValue();
            rec.pincode = this.branchPincode.getValue();
        if(this.ibgBank.getValue() && this.ibgBankDetailRec != "" && this.ibgBankDetailRec != undefined) {
            //DBS Bank
            rec.ibgbankdetailid = this.ibgbankdetailid;
            rec.cimbbankdetailid = this.cimbbankdetailid;
            rec.uobbankdetailid = this.uobbankdetailid;
            rec.ocbcbankdetailid = this.ocbcbankdetailid;
            
            rec.ibgbank = this.ibgBankDetailRec.ibgbank;
            rec.bankCode = this.ibgBankDetailRec.bankCode;
            rec.branchCode = this.ibgBankDetailRec.branchCode;
            rec.accountNumber = this.ibgBankDetailRec.accountNumber;
            rec.accountName = this.ibgBankDetailRec.accountName;
            rec.sendersCompanyID = this.ibgBankDetailRec.sendersCompanyID;
            rec.bankDailyLimit = this.ibgBankDetailRec.bankDailyLimit;
            rec.ibgbanktype = this.ibgBankDetailRec.ibgbanktype;
            
            //CIMB Bank
            rec.serviceCode = this.ibgBankDetailRec.serviceCode;
            rec.bankAccountNumber = this.ibgBankDetailRec.bankAccountNumber;
            rec.ordererName = this.ibgBankDetailRec.ordererName;
            rec.currencyCode = this.ibgBankDetailRec.currencyCode;
            rec.settlementMode = this.ibgBankDetailRec.settlementMode;
            rec.postingIndicator = this.ibgBankDetailRec.postingIndicator;
            
            //UOB Bank
            rec.uobOriginatingBICCode =  this.ibgBankDetailRec.uobOriginatingBICCode;
            rec.uobCurrencyCode =  this.ibgBankDetailRec.uobCurrencyCode;
            rec.uobOriginatingAccountNumber =  this.ibgBankDetailRec.uobOriginatingAccountNumber;
            rec.uobOriginatingAccountName =  this.ibgBankDetailRec.uobOriginatingAccountName;
            rec.uobUltimateOriginatingCustomer =  this.ibgBankDetailRec.uobUltimateOriginatingCustomer;
            rec.uobCompanyId =  this.ibgBankDetailRec.uobCompanyId;
            
            //OCBC Bank
            rec.ocbcOriginatingBankCode = this.ibgBankDetailRec.ocbcOriginatingBankCode;
            rec.ocbcAccountNumber = this.ibgBankDetailRec.ocbcAccountNumber;
            rec.ocbcReferenceNumber = this.ibgBankDetailRec.ocbcReferenceNumber;
        }

        if (this.ispropagatetochildcompanyflag) {
            rec.ispropagatetochildcompanyflag = this.ispropagatetochildcompanyflag;
            rec.name = this.AccName.getValue();
            rec.creationdate=WtfGlobal.convertToGenericDate(this.creationdate.getValue());
        }
        if(!this.exchangeRate.disabled && WtfGlobal.searchRecord(this.defaultCurrencyStore,this.Currency.getValue(),'tocurrencyid') != null){
            var erId = WtfGlobal.searchRecord(this.defaultCurrencyStore,this.Currency.getValue(),'tocurrencyid').data.id
            Wtf.Ajax.requestEx({
                url:"ACCCurrency/saveCurrencyExchangeDetail.do",
                params: {
                    applydate: WtfGlobal.convertToGenericDate(this.creationdate.getValue()),
                    exchangerate: this.exchangeRate.getValue(),
                    id:erId
                }
            },this);
        }

        Wtf.Ajax.requestEx({
            url:"ACCAccount/saveAccount.do",
            //                url: Wtf.req.account+'CompanyManager.jsp',
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
        },

    genSuccessResponse:function(response){
        // Msgbox for success of adding of account moved to last line of this method from here          Neeraj
        if(response.success){
            var title=this.title;
             Wtf.MessageBox.show({
                title: title,
                msg: response.msg,
                width:450,
                scope: {
                  scopeObj:this  
                },
                buttons: Wtf.MessageBox.OK,
                fn: function(btn, text, option) {
                    Wtf.salesAccStore.reload();
                    this.scopeObj.responsedAccountId = response.accID;
                    this.scopeObj.fireEvent('update', this, response.accID);

                    //Neeraj
                    var tabid = 'ledger';
                    if (Wtf.getCmp(tabid) != undefined) {
                        Wtf.getCmp(tabid).accStore.reload();
                    }
                    this.scopeObj.close();
                },
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            }); 
        } else {
            if(response.msg) {
                var msgArr = [];
                msgArr[0] = WtfGlobal.getLocaleText("acc.common.error");
                msgArr[1] = response.msg;
                WtfComMsgBox(msgArr, 1);
            }
        }
        this.fireEvent("loadingcomplete",this);
    },
    
    genFailureResponse:function(response){
    	this.fireEvent("loadingcomplete",this);
        var msg=WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },

    checkDuplicateAccountCode:function(o,newval,oldval){
        var FIND = this.accCode.getValue().trim();
        FIND =FIND.replace(/\s+/g, '');
        var index = this.parentStore.findBy(function(rec){
             var code=rec.data['code'].trim();
             code=code.replace(/\s+/g, '');
            if(code===FIND && !rec.data.deleted) // Add non-deleted record check
                return true;
             else
                return false
        });
        if(index>=0){ 
            WtfComMsgBox(48,2);
            this.accCode.setValue(oldval);
            return;
        }
    },
    
    checkDuplicateAliasCode:function(o,newval,oldval){
        var FIND = this.aliasCode.getValue().trim();
        FIND =FIND.replace(/\s+/g, '');
        var index = this.parentStore.find('alscode', FIND);
        if(index>=0){
            WtfComMsgBox(50,2);
            this.aliasCode.setValue(oldval);
            return;
        }
    },

    radioGlchange:function(obj,isCheck){   
        if(isCheck && this.cmbParent.getValue()!="" && this.cmbParent.getValue()!=undefined){ // When account has assigned parent account
            var valid=this.checkParentMasterType(1);
            if(!valid){
                return;
            }
        }
        
        if(isCheck){
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.mailingDetailsSetup.hide();
            }
            this.ibgBank.setValue(false);
            this.viewibgButton.disable();
            this.ibgBank.disable();
            this.Currency.setDisabled(true);
        }else{
            this.ibgBank.setDisabled(true);
            this.Currency.disable();
        }
    },

    radioCashchange:function(obj,isCheck){
        if(isCheck && this.cmbParent.getValue()!="" && this.cmbParent.getValue()!=undefined){ // When account has assigned parent account
            var valid=this.checkParentMasterType(2);
            if(!valid){
                return;
            }
        }
        if(isCheck){
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.mailingDetailsSetup.hide();
            }
            this.ibgBank.setValue(false);
            this.viewibgButton.disable();
            this.ibgBank.disable();
            this.Currency.setDisabled(false);
        }else{
            this.Currency.setDisabled(true);
            this.ibgBank.disable();
        }
    },

    radioBankchange:function(obj,isCheck){
        if(isCheck && this.cmbParent.getValue()!="" && this.cmbParent.getValue()!=undefined){ // When account has assigned parent account
            var valid=this.checkParentMasterType(3);
            if(!valid){
                return;
            }
        }
        if(isCheck){
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.mailingDetailsSetup.show();
            }
            this.ibgBank.enable();            
            this.Currency.setDisabled(false);
        }else{
            this.ibgBank.setDisabled(true);
            this.Currency.disable();
        }
    },

    radioGSTchange:function(obj,isCheck){
        if(isCheck && this.cmbParent.getValue()!="" && this.cmbParent.getValue()!=undefined){ // When account has assigned parent account
            var valid=this.checkParentMasterType(4);
            if(!valid){
                return;
            }
        }
        if(isCheck){
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.mailingDetailsSetup.hide();
            }
            this.ibgBank.setValue(false);
            this.viewibgButton.disable();
            this.ibgBank.disable();
            this.Currency.setDisabled(true);
        }else{
            this.ibgBank.setDisabled(true);
            this.Currency.disable();
        }
    },

    showGroupWindow:function(){
        callGroupWindow(false, null, "groupWin");
        Wtf.getCmp("groupWin").alignTo(this.getEl(),"other-tl",[50,50]);
        Wtf.getCmp("groupWin").on("update",function(obj,groupID){
            this.accGroup.store.reload();
            var groupReport=Wtf.getCmp("groupDetails");
            if(groupReport!=undefined){
                Wtf.getCmp("groupDetails").updateGrid(obj,groupID);      //updating Group report tab   
            }
        },this);
    }
});
