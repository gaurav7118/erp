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

function callAccountPrefOnScriptLoad(loadingMask) {

    var panel = Wtf.getCmp("companyAccPrefWin");
            if(panel==null){
        panel = new Wtf.account.CompanyAccountPreferences({
            id : 'companyAccPrefWin',
            border : false,
            layout: 'fit',
            compPrefLoadmask:loadingMask,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.dashboard.accountPreferences"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.accPref.tabTitleTT"),  //'You can set your Account Preferences here related to Financial Year Settings, Close Books, Account Settings, Inventory Settings and Automatic Number Generation',
            helpmodeid:32,
            closable: true,
            iconCls:'accountingbase'
        });
        Wtf.getCmp('as').add(panel);
        panel.on("resize", function(){
            panel.doLayout();
        },this);
        panel.on("activate", function() {

            panel.doLayout();
        }, this);
        Wtf.getCmp('as').doLayout(); //editior grid panel height issue.
    }
    if (loadingMask != undefined) {
        loadingMask.hide();
    }
    Wtf.getCmp('as').setActiveTab(panel);
}
Wtf.account.CompanyAccountPreferences=function(config){
    Wtf.account.CompanyAccountPreferences.superclass.constructor.call(this, config);
    this.on('beforedestroy', function () {
        if(this.GroupCompanyTab!=undefined && this.GroupCompanyTab!="undefined"){
            this.newPanel.setActiveTab(this.GroupCompanyTab);
        }
//        if(this.mainPOSPanelTab!=undefined && this.mainPOSPanelTab!="undefined"){
//            this.newPanel.setActiveTab(this.mainPOSPanelTab);
//        }
        if(this.posPanelTab!=undefined && this.posPanelTab!="undefined"){
            this.newPanel.setActiveTab(this.posPanelTab);
        }
        return true;
    });
};

Wtf.extend(Wtf.account.CompanyAccountPreferences, Wtf.account.ClosablePanel,{
    val:false,
    currentyear:-1,
    ffyear:null,
    onRender:function(config){
        Wtf.account.CompanyAccountPreferences.superclass.onRender.call(this, config);
        WtfComMsgBox(29,4,true);
        this.setCurrentYear();
        WtfGlobal.setAjaxTimeOut();//by default set to 15min as discussed with Sagar M Sir
        if(this.compPrefLoadmask!=undefined){
            this.compPrefLoadmask.hide();
        }
        if(Wtf.account.companyAccountPref.activateInventoryTab){
//            this.getAllInventorySequFormat();
        }
        this.val=Wtf.account.companyAccountPref.withoutinventory;
        this.accRec = Wtf.data.Record.create ([
        {
            name:'accountname',
            mapping:'accname'
        },

        {
            name:'accountid',
            mapping:'accid'
        }
        ,
        {name:'acccode',
         mapping:'acccode'
        },
        {
         name:'groupid',
         mapping:'groupid'
        },
        {
         name:'accounttype'
        }
        //            {name:'level', type:'int'}
        ]);

        this.expenseStore = new Wtf.data.Store({
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                //              nature:[2],
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });

        this.liabilityStore = new Wtf.data.Store({
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                //              nature:[0],
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });

        this.dgStore = new Wtf.data.Store({
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                ignore:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });


        this.exStore = new Wtf.data.Store({
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
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
        {name: 'autopopulate'},
    ]);

        this.customDutyStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getAccountsForCombo.do",
            baseParams: {
                mode: 2,
                nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.accRec)
        });
        this.customDutyStore.on("load",this.setCustomDutyAccountValue,this);

          this.crStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.pmtRec),
            url : "ACCPaymentMethods/getPaymentMethods.do",
            baseParams:{
                populateincpcs:true
            }
        });

        this.localUOMRec = Wtf.data.Record.create ([
            {
                name:'uomid'
            },
            {
                name:'uomname'
            },
            {
                name: 'precision'
            }
            ]);
        this.localUOMStore=new Wtf.data.Store({
            url: "ACCUoM/getUnitOfMeasure.do",
            baseParams:{
                mode:31,
                common:'1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.localUOMRec)
        });


        this.salesAccRec = new Wtf.data.Record.create ([
        {
            name: 'accid'
        },

        {
            name: 'acccode'
        },

        {
            name: 'accname'
        }
        ]);
        this.salesAccStore=new Wtf.data.Store({
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[3]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });
        this.salesAccStoreForRevenueRecognitionMannualJE=new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams: {
                mode: 2,
                ignoreCashAccounts: true,
                ignoreBankAccounts: true,
                ignoreGSTAccounts: true,
                ignorecustomers: true,
                ignorevendors: true,
                common:'1',
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });
        this.liabilityAccStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getAccountsForCombo.do",
            baseParams: {
                mode: 2,
                ignoreCashAccounts: true,
                ignoreBankAccounts: true,
                ignoreGSTAccounts: true,
                ignorecustomers: true,
                ignorevendors: true,
                nature: [0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.salesAccRec)
        });

        this.custRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }
        ]);
        this.selectedCustomerStore = new Wtf.data.Store({

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
            }, this.custRec)
        });

        this.vendorRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }
        ]);

        this.selectedVendorStore = new Wtf.data.Store({

            url: "ACCVendor/getVendorsForCombo.do",
            baseParams: {
                mode: 2,
                group: 13,
                deleted: false,
                nondeleted: true,
                common: '1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad: false
            }, this.vendorRec)
        });

        this.gstAccountForBadDebtsStore=new Wtf.data.Store({
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });
        this.gstBadDebtsReleifAccountStore=new Wtf.data.Store({ // For Sales
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });
        this.gstBadDebtsRecoverAccountStore=new Wtf.data.Store({ //For Sales
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });

        this.gstBadDebtsReleifPurchaseAccountStore=new Wtf.data.Store({     //ERP-10400 , For Purchase
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });
        this.gstBadDebtsRecoverPurchaseAccountStore=new Wtf.data.Store({    //ERP-10400 , For Purchase
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });
        this.gstBadDebtsSuspenseAccountStore=new Wtf.data.Store({
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });

        this.inputTaxAdjustmentAccountStore=new Wtf.data.Store({
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });


        this.outputTaxAdjustmentAccountStore=new Wtf.data.Store({
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });

        this.freeGiftJEAccountStore=new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nature:[0]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });
        this.stateRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.stateStore = new Wtf.data.Store({
//    url:Wtf.req.base+"UserManager.jsp",
            url: "kwlCommonTables/getAllStates.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.stateRec),
            baseParams: {
                mode: 20,
                common: '1',
                countryid: Wtf.account.companyAccountPref.countryid
            }
        });

            this.addMultiEntityGrid();
        this.createForm();
    },
    setCustomDutyAccountValue:function(){
        this.customDutyAccount.setValue(Wtf.CustomDutyAccount);
        this.IGSTAccount.setValue(Wtf.IGSTCustomDutyAccount);
    },
    loadStores : function(){
        this.exStore.on('load',function(){
            Wtf.MessageBox.hide();
            this.setAccounts();
        },this);
        this.dgStore.on('load',function(){
            this.exStore.load();
        },this);

        this.expenseStore.load();
        this.expenseStore.on('load',function(){
            this.liabilityStore.add(this.expenseStore.getRange());
        },this);
        //        this.liabilityStore.load();
        this.dgStore.load();
        this.setSalesFlag=true;
        // this store loaded here because on load of dgstore exStore is loaded and on load of exStore we have seted account
        this.isDeferredRevenueRecognition.on('focus',function(){
            this.salesAcc.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.product.salesAcc"));
            this.salesAccStoreForRevenueRecognitionMannualJE.clearFilter(true);
            // this.salesAcc.clearValue();
            this.setSalesFlag=false;
            this.salesAccStoreForRevenueRecognitionMannualJE.load({
                params:{
                    nature: [3]
                }
            });
           this.salesAcc.enable();
            this.salesAcc.clearValue();
            this.salesRevenueRecognitionAccount.enable();
            this.salesRevenueRecognitionAccount.allowBlank=false;
            this.salesAcc.allowBlank=false;
        },this);
        this.recurringDeferredRevenueRecognition.on('focus',function(){
            this.salesAcc.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.product.advanceSalesAcc"));
           this.salesAccStoreForRevenueRecognitionMannualJE.clearFilter(true);
            //this.salesAcc.clearValue();
            this.setSalesFlag=false;
            this.salesAccStoreForRevenueRecognitionMannualJE.load();
            this.salesAcc.enable();
            this.salesAcc.clearValue();
            this.salesRevenueRecognitionAccount.enable();
            this.salesRevenueRecognitionAccount.allowBlank=false;
            this.salesAcc.allowBlank=false;
        },this);
        this.noRevenueRecognition.on('focus',function(){
            this.salesAcc.el.up('.x-form-item', 10, true).child('.x-form-item-label').update(WtfGlobal.getLocaleText("acc.product.salesAcc"));
            this.salesRevenueRecognitionAccount.allowBlank=true;
            this.salesAcc.allowBlank=true;
            this.salesAcc.setValue("");
            this.salesRevenueRecognitionAccount.setValue("");
            this.salesAcc.disable();
            this.salesRevenueRecognitionAccount.disable();
            if(Wtf.account.companyAccountPref.isLMSIntegration){
                this.liabilityAccountForLMS.setValue("");
                this.liabilityAccountForLMS.disable();
            }
        },this);
        if(Wtf.Countryid == Wtf.Country.INDIA){
            if(this.customDutyStore){
                this.customDutyStore.load();
            }
        }
        this.DashBoardImage.on('check',function(obj,ischeck){
            if(ischeck){
                this.UploadImageButton.enable();
            }else{
                this.UploadImageButton.disable();
            }
        },this);
    },

    addNewAccountBlock:function(){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa)){
            //            this.Cash.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true);
            this.DiscountGiven.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.discountgiven ,this.DiscountGiven.store],true);
            this.DiscountReceived.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.discountreceived,this.DiscountReceived.store],true);
            this.Depreciation.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"depcoaWin",true,false, Wtf.account.companyAccountPref.depreciationaccount,this.Depreciation.store],true);
            this.ForeignExchange.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"fxcoaWin",true,false, Wtf.account.companyAccountPref.foreignexchange,this.ForeignExchange.store],true);
            this.LoanDisbursement.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",true,false,Wtf.account.companyAccountPref.loandisbursementaccount,this.LoanDisbursement.store],true);
            this.LoanInterestAccount.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",true,false, Wtf.account.companyAccountPref.loaninterestaccount,this.LoanInterestAccount.store],true);
            this.UnrealisedGainLoss.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"fxcoaWin",true,false, Wtf.account.companyAccountPref.unrealisedgainloss,this.UnrealisedGainLoss.store],true);
//            this.OtherCharges.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false],true);
            this.expenseAccount.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",true,false,Wtf.account.companyAccountPref.expenseaccount,this.expenseAccount.store],true);
            this.liabilityAccount.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.liabilityaccount,this.liabilityAccount.store],true);
            this.customerDefaultAccount.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.customerdefaultaccount,this.customerDefaultAccount.store],true);
            this.vendorDefaultAccount.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.vendordefaultaccount,this.vendorDefaultAccount.store],true);
            this.wastageDefaultAccount.addNewFn = this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.wastageDefaultAccount,this.wastageDefaultAccount.store],true);
            this.invoiceWriteOffAccount.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.writeOffAccount,this.invoiceWriteOffAccount.store],true);

            this.receiptWriteOffAccount.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.receiptWriteOffAccount,this.receiptWriteOffAccount.store],true);
            this.roundingDifferenceAccount.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.roundingDifferenceAccount,this.roundingDifferenceAccount.store],true);
            this.adjustmentAccountPayment.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.adjustmentAccountPayment,this.adjustmentAccountPayment.store],true);
            this.adjustmentAccountReceipt.addNewFn=this.addNewAccount.createDelegate(this,[false,null,"coaWin",false,false, Wtf.account.companyAccountPref.adjustmentAccountReceipt,this.adjustmentAccountReceipt.store],true);
            //Replacing 5th argument with false as it shows GST as default Master Type instead of General Ledger. ERP-38658
        }
    },

    addNewFormatFormat : function(mode,module, component){
        var p = Wtf.getCmp("sequenceformatlinkforaccounting");
        if(!p){
            this.showSquenceFormatWindow = new Wtf.SequenceFormatWindow({
                mode:mode,
                module:module,
                component:component,
                scope:this
            })
            this.showSquenceFormatWindow.show();
        }
        this.showSquenceFormatWindow.on('setAutoNumbers',this.setAutoNumbers,this);
    },
    addNewFormatFormatForInv : function(mode,module, component){
        var moduleName=module;
        if(mode=="autoSRequest"){
            module="0";
        }else if(mode=="autoIssueNote"){
            module="1";
        }else if(mode=="autoInstStore"){
            module="2";
        }else if(mode=="autoSA"){
            module="3";
        }else if(mode=="autoCycleCount"){
            module="4";
        }else if(mode=="autoInstLocation"){
            module="5";
        }else if(mode=="assetid"){
            module="6";
        }
        var p = Wtf.getCmp("inventorySequnceFormId");
        if(!p){
            p = new Wtf.SequenceFormatForm({
                id:"inventorySequnceFormId",
                title : "Add Sequence Format",
                layout : 'fit',
                closable: true,
                width:650,
                height:480,
                modal:true,
                action:"ADD",
                resizable:false,
                compPref:true,
                moduleId:module,
                moduleName:moduleName,
                parentCmp:this
            })
        }
        p.show();
    },
    addChequeNumber : function(){
        var p = Wtf.getCmp("chequesequenceformatlinkforaccounting");
        if(!p){
            this.showChequeSquenceFormatWindow = new Wtf.ChequeSequenceFormat({
                scope:this
            })
            this.showChequeSquenceFormatWindow.show();
        }
        this.showChequeSquenceFormatWindow.on('setAutoNumbers',this.setChequeNumber,this);
    },

    setChequeNumber:function(sequenceformat, isAllowToAdd){
        var chequeVal = this.autocheque.getValue();
        var updatedValue = "";
        //        chequeVal = sequenceformat;
        if(!isAllowToAdd){
            this.autocheque.setValue(sequenceformat);
            Wtf.account.companyAccountPref.autocheque = sequenceformat;
            updatedValue = sequenceformat;
        }else{
            //            if(chequeVal!=""){
            //                chequeVal = chequeVal+","+sequenceformat
            //            } else {
            //                chequeVal = sequenceformat
            //            }
            this.autocheque.setValue(sequenceformat);
            Wtf.account.companyAccountPref.autocheque = sequenceformat;
            updatedValue = chequeVal;
        }
        this.autocheque.toolTip = updatedValue;
        this.autocheque.fireEvent('render', this.autocheque);
    },

    setAutoNumbers : function(mode, sequenceformat,isAllowToAdd){
        var updatedValue = "";
        switch(mode){
            case 'autojournalentry':
                if(!isAllowToAdd){
                    this.autojournalentry.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autojournalentry = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autojournalentry.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autojournalentry.setValue(value);
                    Wtf.account.companyAccountPref.autojournalentry=value
                    updatedValue = value;
                }
                this.autojournalentry.toolTip = updatedValue;
                this.autojournalentry.fireEvent('render', this.autojournalentry);
                break;
            case 'autoinvoice':
                if(!isAllowToAdd){
                    this.autoinvoice.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoinvoice = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoinvoice.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoinvoice.setValue(value);
                    Wtf.account.companyAccountPref.autoinvoice = value
                    updatedValue = value;
                }
                this.autoinvoice.toolTip = updatedValue;
                this.autoinvoice.fireEvent('render', this.autoinvoice);
                break;
            case 'autocreditmemo':
                if(!isAllowToAdd){
                    this.autocreditmemo.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autocreditmemo = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autocreditmemo.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autocreditmemo.setValue(value);
                    Wtf.account.companyAccountPref.autocreditmemo = value;
                    updatedValue = value;
                }
                this.autocreditmemo.toolTip = updatedValue;
                this.autocreditmemo.fireEvent('render', this.autocreditmemo);
                break;
            case 'autoreceipt':
                if(!isAllowToAdd){
                    this.autoreceipt.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoreceipt = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoreceipt.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoreceipt.setValue(value);
                    Wtf.account.companyAccountPref.autoreceipt = value;
                    updatedValue = value;
                }
                this.autoreceipt.toolTip = updatedValue;
                this.autoreceipt.fireEvent('render', this.autoreceipt);
                break;
            case 'autogoodsreceipt':
                if(!isAllowToAdd){
                    this.autogoodsreceipt.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autogoodsreceipt = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autogoodsreceipt.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autogoodsreceipt.setValue(value);
                    Wtf.account.companyAccountPref.autogoodsreceipt = value;
                    updatedValue = value;
                }
                this.autogoodsreceipt.toolTip = updatedValue;
                this.autogoodsreceipt.fireEvent('render', this.autogoodsreceipt);
                break;
            case 'autodebitnote':
                if(!isAllowToAdd){
                    this.autodebitnote.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autodebitnote = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autodebitnote.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autodebitnote.setValue(value);
                    Wtf.account.companyAccountPref.autodebitnote=value;
                    updatedValue = value;
                }
                this.autodebitnote.toolTip = updatedValue;
                this.autodebitnote.fireEvent('render', this.autodebitnote);
                break;
            case 'autopayment':
                if(!isAllowToAdd){
                    this.autopayment.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autopayment = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autopayment.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autopayment.setValue(value);
                    Wtf.account.companyAccountPref.autopayment = value;
                    updatedValue = value;
                }
                this.autopayment.toolTip = updatedValue;
                this.autopayment.fireEvent('render', this.autopayment);
                break;
            case 'autoso':
                if(!isAllowToAdd){
                    this.autoso.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoso = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoso.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoso.setValue(value);
                    Wtf.account.companyAccountPref.autoso = value;
                    updatedValue = value;
                }
                this.autoso.toolTip = updatedValue;
                this.autoso.fireEvent('render', this.autoso);
                break;
            case 'autocontract':
                if(!isAllowToAdd){
                    this.autocontract.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autocontract = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autocontract.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autocontract.setValue(value);
                    Wtf.account.companyAccountPref.autocontract = value;
                    updatedValue = value;
                }
                this.autocontract.toolTip = updatedValue;
                this.autocontract.fireEvent('render', this.autocontract);
                break;
            case 'autopo':
                if(!isAllowToAdd){
                    this.autopo.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autopo = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autopo.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autopo.setValue(value);
                    Wtf.account.companyAccountPref.autopo = value;
                    updatedValue = value;
                }
                this.autopo.toolTip = updatedValue;
                this.autopo.fireEvent('render', this.autopo);
                break;
            case 'autocashsales':
                if(!isAllowToAdd){
                    this.autocashsales.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autocashsales = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autocashsales.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autocashsales.setValue(value);
                    Wtf.account.companyAccountPref.autocashsales = value;
                    updatedValue = value;
                }
                this.autocashsales.toolTip = updatedValue;
                this.autocashsales.fireEvent('render', this.autocashsales);
                break;
            case 'autocashpurchase':
                if(!isAllowToAdd){
                    this.autocashpurchase.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autocashpurchase = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autocashpurchase.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autocashpurchase.setValue(value);
                    Wtf.account.companyAccountPref.autocashpurchase = value;
                    updatedValue = value;
                }
                this.autocashpurchase.toolTip = updatedValue;
                this.autocashpurchase.fireEvent('render', this.autocashpurchase);
                break;
            case 'autobillinginvoice':
                if(!isAllowToAdd){
                    this.autobillinginvoice.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillinginvoice = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillinginvoice.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillinginvoice.setValue(value);
                    Wtf.account.companyAccountPref.autobillinginvoice = value;
                    updatedValue = value;
                }
                this.autobillinginvoice.toolTip = updatedValue;
                this.autobillinginvoice.fireEvent('render', this.autobillinginvoice);
                break;
            case 'autobillingreceipt':
                if(!isAllowToAdd){
                    this.autobillingreceipt.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillingreceipt = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillingreceipt.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillingreceipt.setValue(value);
                    Wtf.account.companyAccountPref.autobillingreceipt = value;
                    updatedValue = value;
                }
                this.autobillingreceipt.toolTip = updatedValue;
                this.autobillingreceipt.fireEvent('render', this.autobillingreceipt);
                break;
            case 'autobillingcashsales':
                if(!isAllowToAdd){
                    this.autobillingcashsales.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillingcashsales = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillingcashsales.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillingcashsales.setValue(value);
                    Wtf.account.companyAccountPref.autobillingcashsales = value;
                    updatedValue = value;
                }
                this.autobillingcashsales.toolTip = updatedValue;
                this.autobillingcashsales.fireEvent('render', this.autobillingcashsales);
                break;
            case 'autobillinggoodsreceipt':
                if(!isAllowToAdd){
                    this.autobillinggoodsreceipt.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillinggoodsreceipt = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillinggoodsreceipt.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillinggoodsreceipt.setValue(value);
                    Wtf.account.companyAccountPref.autobillinggoodsreceipt = value;
                    updatedValue = value;
                }
                this.autobillinggoodsreceipt.toolTip = updatedValue;
                this.autobillinggoodsreceipt.fireEvent('render', this.autobillinggoodsreceipt);
                break;
            case 'autobillingdebitnote':
                if(!isAllowToAdd){
                    this.autobillingdebitnote.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillingdebitnote = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillingdebitnote.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillingdebitnote.setValue(value);
                    Wtf.account.companyAccountPref.autobillingdebitnote = value;
                    updatedValue = value;
                }
                this.autobillingdebitnote.toolTip = updatedValue;
                this.autobillingdebitnote.fireEvent('render', this.autobillingdebitnote);
                break;
            case 'autobillingcreditmemo':
                if(!isAllowToAdd){
                    this.autobillingcreditmemo.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillingcreditmemo = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillingcreditmemo.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillingcreditmemo.setValue(value);
                    Wtf.account.companyAccountPref.autobillingcreditmemo = value;
                    updatedValue = value;
                }
                this.autobillingcreditmemo.toolTip = updatedValue;
                this.autobillingcreditmemo.fireEvent('render', this.autobillingcreditmemo);
                break;
            case 'autobillingpayment':
                if(!isAllowToAdd){
                    this.autobillingpayment.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillingpayment = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillingpayment.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillingpayment.setValue(value);
                    Wtf.account.companyAccountPref.autobillingpayment = value;
                    updatedValue = value;
                }
                this.autobillingpayment.toolTip = updatedValue;
                this.autobillingpayment.fireEvent('render', this.autobillingpayment);
                break;
            case 'autobillingso':
                if(!isAllowToAdd){
                    this.autobillingso.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillingso = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillingso.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillingso.setValue(value);
                    Wtf.account.companyAccountPref.autobillingso = value;
                    updatedValue = value;
                }
                this.autobillingso.toolTip = updatedValue;
                this.autobillingso.fireEvent('render', this.autobillingso);
                break;
            case 'autobillingpo':
                if(!isAllowToAdd){
                    this.autobillingpo.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillingpo = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillingpo.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillingpo.setValue(value);
                    Wtf.account.companyAccountPref.autobillingpo = value;
                    updatedValue = value;
                }
                this.autobillingpo.toolTip = updatedValue;
                this.autobillingpo.fireEvent('render', this.autobillingpo);
                break;
            case 'autobillingcashpurchase':
                if(!isAllowToAdd){
                    this.autobillingcashpurchase.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobillingcashpurchase = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobillingcashpurchase.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobillingcashpurchase.setValue(value);
                    Wtf.account.companyAccountPref.autobillingcashpurchase = value;
                    updatedValue = value;
                }
                this.autobillingcashpurchase.toolTip = updatedValue;
                this.autobillingcashpurchase.fireEvent('render', this.autobillingcashpurchase);
                break;
            case 'autorequisition':
                if(!isAllowToAdd){
                    this.autopurchaserequisition.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autorequisition = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autopurchaserequisition.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autopurchaserequisition.setValue(value);
                    Wtf.account.companyAccountPref.autorequisition = value;
                    updatedValue = value;
                }
                this.autopurchaserequisition.toolTip = updatedValue;
                this.autopurchaserequisition.fireEvent('render', this.autopurchaserequisition);
                break;
            case 'autorequestforquotation':
                if(!isAllowToAdd){
                    this.autorequestforquotation.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autorequestforquotation = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autorequestforquotation.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autorequestforquotation.setValue(value);
                    Wtf.account.companyAccountPref.autorequestforquotation = value;
                    updatedValue = value;
                }
                this.autorequestforquotation.toolTip = updatedValue;
                this.autorequestforquotation.fireEvent('render', this.autorequestforquotation);
                break;
            case 'autovenquotation':
                if(!isAllowToAdd){
                    this.autovenquotation.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autovenquotation = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autovenquotation.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autovenquotation.setValue(value);
                    Wtf.account.companyAccountPref.autovenquotation = value;
                    updatedValue = value;
                }
                this.autovenquotation.toolTip = updatedValue;
                this.autovenquotation.fireEvent('render', this.autovenquotation);
                break;
            case 'autoquotation':
                if(!isAllowToAdd){
                    this.autoquotation.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoquotation = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoquotation.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoquotation.setValue(value);
                    Wtf.account.companyAccountPref.autoquotation = value;
                    updatedValue = value;
                }
                this.autoquotation.toolTip = updatedValue;
                this.autoquotation.fireEvent('render', this.autoquotation);
                break;
            case 'autodo':
                if(!isAllowToAdd){
                    this.autodo.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autodo = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autodo.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autodo.setValue(value);
                    Wtf.account.companyAccountPref.autodo = value;
                    updatedValue = value;
                }
                this.autodo.toolTip = updatedValue;
                this.autodo.fireEvent('render', this.autodo);
                break;
            case 'autogro':
                if(!isAllowToAdd){
                    this.autogro.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autogro = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autogro.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autogro.setValue(value);
                    Wtf.account.companyAccountPref.autogro = value;
                    updatedValue = value;
                }
                this.autogro.toolTip = updatedValue;
                this.autogro.fireEvent('render', this.autogro);
                break;
            case 'autosr':
                if(!isAllowToAdd){
                    this.autosr.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autosr = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autosr.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autosr.setValue(value);
                    Wtf.account.companyAccountPref.autosr = value;
                    updatedValue = value;
                }
                this.autosr.toolTip = updatedValue;
                this.autosr.fireEvent('render', this.autosr);
                break;
            case 'autopr':
                if(!isAllowToAdd){
                    this.autopr.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autopr = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autopr.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autopr.setValue(value);
                    Wtf.account.companyAccountPref.autopr = value;
                    updatedValue = value;
                }
                this.autopr.toolTip = updatedValue;
                this.autopr.fireEvent('render', this.autopr);
                break;
            case 'autoproductid':
                if(!isAllowToAdd){
                    this.autoproductid.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoproductid = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoproductid.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoproductid.setValue(value);
                    Wtf.account.companyAccountPref.autoproductid = value;
                    updatedValue = value;
                }
                this.autoproductid.toolTip = updatedValue;
                this.autoproductid.fireEvent('render', this.autoproductid);
                break;
            case 'autocustomerid':
                if(!isAllowToAdd){
                    this.autocustomerid.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autocustomerid = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autocustomerid.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autocustomerid.setValue(value);
                    Wtf.account.companyAccountPref.autocustomerid = value;
                    updatedValue = value;
                }
                this.autocustomerid.toolTip = updatedValue;
                this.autocustomerid.fireEvent('render', this.autocustomerid);
                break;

            case 'autovendorid':
                if(!isAllowToAdd){
                    this.autovendorid.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autovendorid = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autovendorid.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autovendorid.setValue(value);
                    Wtf.account.companyAccountPref.autovendorid = value;
                    updatedValue = value;
                }
                this.autovendorid.toolTip = updatedValue;
                this.autovendorid.fireEvent('render', this.autovendorid);
                break;
            case 'autosalesbaddebtclaimid':
                if(!isAllowToAdd){
                    this.autosalesbaddebtclaimid.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autosalesbaddebtclaimid = sequenceformat;
                    updatedValue = sequenceformat;
                }else {
                    var value = this.autosalesbaddebtclaimid.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autosalesbaddebtclaimid.setValue(value);
                    Wtf.account.companyAccountPref.autosalesbaddebtclaimid = value;
                    updatedValue = value;
                }
                this.autosalesbaddebtclaimid.toolTip = updatedValue;
                this.autosalesbaddebtclaimid.fireEvent('render', this.autosalesbaddebtclaimid);
                break;
            case 'autosalesbaddebtrecoverid':
                if(!isAllowToAdd){
                    this.autosalesbaddebtrecoverid.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autosalesbaddebtrecoverid = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autosalesbaddebtrecoverid.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autosalesbaddebtrecoverid.setValue(value);
                    Wtf.account.companyAccountPref.autosalesbaddebtrecoverid = value;
                    updatedValue = value;
                }
                this.autosalesbaddebtrecoverid.toolTip = updatedValue;
                this.autosalesbaddebtrecoverid.fireEvent('render', this.autosalesbaddebtrecoverid);
                break;
            case 'autopurchasebaddebtclaimid':
                if(!isAllowToAdd){
                    this.autopurchasebaddebtclaimid.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autopurchasebaddebtclaimid = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autopurchasebaddebtclaimid.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autopurchasebaddebtclaimid.setValue(value);
                    Wtf.account.companyAccountPref.autopurchasebaddebtclaimid = value;
                    updatedValue = value;
                }
                this.autopurchasebaddebtclaimid.toolTip = updatedValue;
                this.autopurchasebaddebtclaimid.fireEvent('render', this.autopurchasebaddebtclaimid);
                break;
            case 'autopurchasebaddebtrecoverid':
                if(!isAllowToAdd){
                    this.autopurchasebaddebtrecoverid.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autopurchasebaddebtrecoverid = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autopurchasebaddebtrecoverid.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autopurchasebaddebtrecoverid.setValue(value);
                    Wtf.account.companyAccountPref.autopurchasebaddebtrecoverid = value;
                    updatedValue = value;
                }
                this.autopurchasebaddebtrecoverid.toolTip = updatedValue;
                this.autopurchasebaddebtrecoverid.fireEvent('render', this.autopurchasebaddebtrecoverid);
                break;
            case 'autobuildassembly':
                if(!isAllowToAdd){
                    this.autobuildassembly.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autobuildassembly = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autobuildassembly.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autobuildassembly.setValue(value);
                    Wtf.account.companyAccountPref.autobuildassembly = value;
                    updatedValue = value;
                }
                this.autobuildassembly.toolTip = updatedValue;
                this.autobuildassembly.fireEvent('render', this.autobuildassembly);
                break;

            case 'autounbuildassembly':
                if(!isAllowToAdd){
                    this.autounbuildassembly.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autounbuildassembly = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autounbuildassembly.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autounbuildassembly.setValue(value);
                    Wtf.account.companyAccountPref.autounbuildassembly = value;
                    updatedValue = value;
                }
                this.autounbuildassembly.toolTip = updatedValue;
                this.autounbuildassembly.fireEvent('render', this.autounbuildassembly);
                break;

            case 'autoreconcilenumber':
                if(!isAllowToAdd){
                    this.reconcilenumber.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoreconcilenumber = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.reconcilenumber.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.reconcilenumber.setValue(value);
                    Wtf.account.companyAccountPref.autoreconcilenumber = value;
                    updatedValue = value;
                }
                this.reconcilenumber.toolTip = updatedValue;
                this.reconcilenumber.fireEvent('render', this.reconcilenumber);
                break;

            case 'autounreconcilenumber':
                if(!isAllowToAdd){
                    this.unreconcilenumber.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autounreconcilenumber = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.unreconcilenumber.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.unreconcilenumber.setValue(value);
                    Wtf.account.companyAccountPref.autounreconcilenumber = value;
                    updatedValue = value;
                }
                this.unreconcilenumber.toolTip = updatedValue;
                this.unreconcilenumber.fireEvent('render', this.unreconcilenumber);
                break;
                /*
                 * To Genarete Sequence Format To Security gate entry form.
                 */
                case 'autoSecurityNo':
                if(!isAllowToAdd){
                    this.securityNo.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoSecurityNo = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.securityNo.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.securityNo.setValue(value);
                    Wtf.account.companyAccountPref.autoSecurityNo = value;
                    updatedValue = value;
                }
                this.securityNo.toolTip = updatedValue;
                this.securityNo.fireEvent('render', this.securityNo);
                break;

            case 'autoassetgroup':
                if(!isAllowToAdd){
                    this.autoassetgroup.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoassetgroup = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoassetgroup.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoassetgroup.setValue(value);
                    Wtf.account.companyAccountPref.autoassetgroup = value;
                    updatedValue = value;
                }
                this.autoassetgroup.toolTip = updatedValue;
                this.autoassetgroup.fireEvent('render', this.autoassetgroup);
                break;
            case 'autoRG23EntryNumber':
                if(!isAllowToAdd){
                    this.autoRG23EntryNumber.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoRG23EntryNumber = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoRG23EntryNumber.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoRG23EntryNumber.setValue(value);
                    Wtf.account.companyAccountPref.autoRG23EntryNumber = value;
                    updatedValue = value;
                }
                this.autoRG23EntryNumber.toolTip = updatedValue;
                this.autoRG23EntryNumber.fireEvent('render', this.autoRG23EntryNumber);
                break;
             case 'autoloanrefnumber':
                if(!isAllowToAdd){
                    this.autoloanrefnumber.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoloanrefnumber = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoloanrefnumber.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoloanrefnumber.setValue(value);
                    Wtf.account.companyAccountPref.autoloanrefnumber = value;
                    updatedValue = value;
                }
                this.autoloanrefnumber.toolTip = updatedValue;
                this.autoloanrefnumber.fireEvent('render', this.autoloanrefnumber);
                break;
                case 'automachineid':
                if(!isAllowToAdd){
                    this.automachineid.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.automachineid = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.automachineid.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.automachineid.setValue(value);
                    Wtf.account.companyAccountPref.autoloanrefnumber = value;
                    updatedValue = value;
                }
                this.automachineid.toolTip = updatedValue;
                this.automachineid.fireEvent('render', this.automachineid);
                break;
           case 'autolabour':
                if (!isAllowToAdd) {
                    this.autolabour.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autolabour = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autolabour.getValue();
                    if (value != "") {
                        value = value + "," + sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autolabour.setValue(value);
                    Wtf.account.companyAccountPref.autolabour = value;
                    updatedValue = value;
                }
                this.autolabour.toolTip = updatedValue;
                this.autolabour.fireEvent('render', this.autolabour);
                break;
            case 'automrpcontract':
                if (!isAllowToAdd) {
                    this.automrpcontract.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.automrpcontract = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.automrpcontract.getValue();
                    if (value != "") {
                        value = value + "," + sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.automrpcontract.setValue(value);
                    Wtf.account.companyAccountPref.automrpcontract = value;
                    updatedValue = value;
                }
                this.automrpcontract.toolTip = updatedValue;
                this.automrpcontract.fireEvent('render', this.automrpcontract);
                break;
              case 'autojobwork':
                if (!isAllowToAdd) {
                    this.autojobwork.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autojobwork = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autojobwork.getValue();
                    if (value != "") {
                        value = value + "," + sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autojobwork.setValue(value);
                    Wtf.account.companyAccountPref.autojobwork = value;
                    updatedValue = value;
                }
                this.autojobwork.toolTip = updatedValue;
                this.autojobwork.fireEvent('render', this.autojobwork);
                break;
              case 'autoworkcentre':
                if (!isAllowToAdd) {
                    this.autoworkcentre.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoworkcentre = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoworkcentre.getValue();
                    if (value != "") {
                        value = value + "," + sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoworkcentre.setValue(value);
                    Wtf.account.companyAccountPref.autoworkcentre = value;
                    updatedValue = value;
                }
                this.autoworkcentre.toolTip = updatedValue;
                this.autoworkcentre.fireEvent('render', this.autoworkcentre);
                break;
              case 'autoworkorder':
                if (!isAllowToAdd) {
                    this.autoworkorder.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoworkorder = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoworkorder.getValue();
                    if (value != "") {
                        value = value + "," + sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autoworkorder.setValue(value);
                    Wtf.account.companyAccountPref.autoworkorder = value;
                    updatedValue = value;
                }
                this.autoworkorder.toolTip = updatedValue;
                this.autoworkorder.fireEvent('render', this.autoworkorder);
                break;
            case 'autoroutecode':
                if (!isAllowToAdd) {
                    this.autoroutecode.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoroutecode = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoroutecode.getValue();
                    if (value != "") {
                        value = value + "," + sequenceformat;
                    } else {
                        value = sequenceformat;
                    }
                    this.autoroutecode.setValue(value);
                    Wtf.account.companyAccountPref.autoroutecode = value;
                    updatedValue = value;
                }
                this.autoroutecode.toolTip = updatedValue;
                this.autoroutecode.fireEvent('render', this.autoroutecode);
                break;
            case 'autopackingdo':
                if (!isAllowToAdd) {
                    this.autopackingdo.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autopackingdo = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autopackingdo.getValue();
                    if (value != "") {
                        value = value + "," + sequenceformat;
                    } else {
                        value = sequenceformat;
                    }
                    this.autopackingdo.setValue(value);
                    Wtf.account.companyAccountPref.autopackingdo = value;
                    updatedValue = value;
                }
                this.autopackingdo.toolTip = updatedValue;
                this.autopackingdo.fireEvent('render', this.autopackingdo);
                break;
            case 'autoshippingdo':
                if (!isAllowToAdd) {
                    this.autoshippingdo.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autoshippingdo = sequenceformat;
                    updatedValue = sequenceformat;
                } else {
                    var value = this.autoshippingdo.getValue();
                    if (value != "") {
                        value = value + "," + sequenceformat;
                    } else {
                        value = sequenceformat;
                    }
                    this.autoshippingdo.setValue(value);
                    Wtf.account.companyAccountPref.autoshippingdo = value;
                    updatedValue = value;
                }
                this.autoshippingdo.toolTip = updatedValue;
                this.autoshippingdo.fireEvent('render', this.autoshippingdo);
                break;

               /*
                * Adding new Sequence format for job work out order
                */
               case 'autojwo':
                if(!isAllowToAdd){                    /* Deleting sequence format*/
                    this.autojwo.setValue(sequenceformat);
                    Wtf.account.companyAccountPref.autojwo = sequenceformat;
                    updatedValue = sequenceformat;
                } else {                              /* Adding new sequence format*/
                    var value = this.autojwo.getValue();
                    if(value!=""){
                        value = value+","+sequenceformat
                    } else {
                        value = sequenceformat
                    }
                    this.autojwo.setValue(value);
                    Wtf.account.companyAccountPref.autojwo = value;
                    updatedValue = value;
                }
                this.autojwo.toolTip = updatedValue;
                this.autojwo.fireEvent('render', this.autojwo);
                break;
        }
    },
    createForm:function(){

        this.barcodeTypeStore=new Wtf.data.SimpleStore({
            fields:[{
                name:'code'
            },{
                name:'codename'
            }],
            //            autoLoad:true,

            data: [  [Wtf.BarcodeType_Code_CODE128,Wtf.BarcodeType_Value_CODE128],  //Default Type
                     [Wtf.BarcodeType_Code_CODE39,Wtf.BarcodeType_Value_CODE39],
                     [Wtf.BarcodeType_Code_EAN8,Wtf.BarcodeType_Value_EAN8],
                     [Wtf.BarcodeType_Code_EAN13,Wtf.BarcodeType_Value_EAN13],
                     [Wtf.BarcodeType_Code_DATAMATRICS,Wtf.BarcodeType_Value_DATAMATRICS]
                     //[Wtf.BarcodeType_Code_EAN128,Wtf.BarcodeType_Value_EAN128],
                     //[Wtf.BarcodeType_Code_CODEBAR,Wtf.BarcodeType_Value_CODEBAR],
                     //[Wtf.BarcodeType_Code_UPCA,Wtf.BarcodeType_Value_UPCA],
                     //[Wtf.BarcodeType_Code_UPCE,Wtf.BarcodeType_Value_UPCE],
                     //[Wtf.BarcodeType_Code_POSTNET,Wtf.BarcodeType_Value_POSTNET],
                     //[Wtf.BarcodeType_Code_INTERLEAVED2OF5,Wtf.BarcodeType_Value_INTERLEAVED2OF5],
                     //[Wtf.BarcodeType_Code_ROYALMAILCUST,Wtf.BarcodeType_Value_ROYALMAILCUST],
                     //[Wtf.BarcodeType_Code_USPSINTGNTMAIL,Wtf.BarcodeType_Value_USPSINTGNTMAIL]
                 ]
        });
         this.printTypeStore=new Wtf.data.SimpleStore({
            fields:[{
                name:'code'
            },{
                name:'codename'
            }],

            data: [
                    [Wtf.PrintType_Value_Right, Wtf.PrintType_Direction_Right],
                    [Wtf.PrintType_Value_Left, Wtf.PrintType_Direction_Left],
                    [Wtf.PrintType_Value_Bottom, Wtf.PrintType_Direction_Bottom],
                    [Wtf.PrintType_Value_Top, Wtf.PrintType_Direction_Top],
                  ]
        });
        this.autojournalentry= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autojournalentry,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoJE"),  //'Journal Entry*',
            name:'autojournalentry',
            anchor:'80%',
            allowBlank:false,
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autojournalentry",WtfGlobal.getLocaleText("acc.accPref.autoJE")]),
            value:Wtf.account.companyAccountPref.autojournalentry
        });
        this.autoinvoice= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoinvoice,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //'Invoice',
            name:'autoinvoice',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoinvoice",WtfGlobal.getLocaleText("acc.accPref.autoInvoice")]),
            value:Wtf.account.companyAccountPref.autoinvoice
        });
        this.autocreditmemo= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autocreditmemo,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCN"),  //'Credit Note',
            name:'autocreditmemo',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autocreditmemo",WtfGlobal.getLocaleText("acc.accPref.autoCN")]),
            value:Wtf.account.companyAccountPref.autocreditmemo
        });
        this.autoreceipt= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoreceipt,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoRP"),  //'Receive Payment',
            name:'autoreceipt',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoreceipt",WtfGlobal.getLocaleText("acc.accPref.autoRP")]),
            value:Wtf.account.companyAccountPref.autoreceipt
        });
        this.autogoodsreceipt= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autogoodsreceipt,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoVI"),  //'Vendor Invoice',
            name:'autogoodsreceipt',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autogoodsreceipt",WtfGlobal.getLocaleText("acc.accPref.autoVI")]),
            value:Wtf.account.companyAccountPref.autogoodsreceipt
        });
        this.autodebitnote= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autodebitnote,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoDN"),  //'Debit Note',
            name:'autodebitnote',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autodebitnote",WtfGlobal.getLocaleText("acc.accPref.autoDN")]),
            value:Wtf.account.companyAccountPref.autodebitnote
        });
        this.autopayment= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autopayment,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Make Payment',
            name:'autopayment',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autopayment",WtfGlobal.getLocaleText("acc.accPref.autoMP")]),
            value:Wtf.account.companyAccountPref.autopayment
        });
        this.autoso= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoso,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSO"),  //'Sales Order',
            name:'autoso',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoso",WtfGlobal.getLocaleText("acc.accPref.autoSO")]),
            value:Wtf.account.companyAccountPref.autoso
        });
        this.autolabour = new Wtf.form.TextFieldAddNewBtn({
            toolTip: Wtf.account.companyAccountPref.autolabour,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.labour"), //'Sales Order',
            name: 'autolabour',
            readOnly: true,
            addNewFn: this.addNewFormatFormat.createDelegate(this, ["autolabour", WtfGlobal.getLocaleText("acc.field.labour")]),
            value: Wtf.account.companyAccountPref.autolabour
        });
        this.automrpcontract = new Wtf.form.TextFieldAddNewBtn({
            toolTip: Wtf.account.companyAccountPref.automrpcontract,
            fieldLabel: WtfGlobal.getLocaleText("acc.dimension.module.22"), //'Sales Order',
            name: 'automrpcontract',
            readOnly: true,
            addNewFn: this.addNewFormatFormat.createDelegate(this, ["automrpcontract", WtfGlobal.getLocaleText("acc.dimension.module.22")]),
            value: Wtf.account.companyAccountPref.automrpcontract
        });
         this.autojobwork= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autojobwork,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.jobworkentryform.title"),  //'Journal Entry*',
            name:'autojobwork',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autojobwork",WtfGlobal.getLocaleText("acc.field.jobworkentryform.title")]),
            value:Wtf.account.companyAccountPref.autojobwork
        });
         this.autoworkcentre= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoworkcentre,
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.workcentre"),  //'Journal Entry*',
            name:'autoworkcentre',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoworkcentre",WtfGlobal.getLocaleText("mrp.workorder.entry.workcentre")]),
            value:Wtf.account.companyAccountPref.autoworkcentre
        });
         this.autoworkorder= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoworkorder,
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.title"),  //'Journal Entry*',
            name:'autoworkorder',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoworkorder",WtfGlobal.getLocaleText("mrp.workorder.entry.title")]),
            value:Wtf.account.companyAccountPref.autoworkorder
        });
        this.autoroutecode = new Wtf.form.TextFieldAddNewBtn({
            toolTip: Wtf.account.companyAccountPref.autoroutecode,
            fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.routecode"), //'Sales Order',
            name: 'autoroutecode',
            readOnly: true,
            addNewFn: this.addNewFormatFormat.createDelegate(this, ["autoroutecode", WtfGlobal.getLocaleText("mrp.workorder.entry.routecode")]),
            value: Wtf.account.companyAccountPref.autoroutecode
        });
        this.autocontract= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autocontract,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoContract"),  //'Sales Order',
            name:'autocontract',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autocontract",WtfGlobal.getLocaleText("acc.accPref.autoContract")]),
            value:Wtf.account.companyAccountPref.autocontract
        });
        this.autopo= new Wtf.form.TextFieldAddNewBtn({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoPO"),  //'Purchase Order',
            toolTip : Wtf.account.companyAccountPref.autopo,
            name:'autopo',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autopo",WtfGlobal.getLocaleText("acc.accPref.autoPO")]),
            value:Wtf.account.companyAccountPref.autopo
        });
        this.autocashsales= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autocashsales,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCS"),  //'Cash Sales',
            name:'autocashsales',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autocashsales",WtfGlobal.getLocaleText("acc.accPref.autoCS")]),
            value:Wtf.account.companyAccountPref.autocashsales
        });
        this.autocashpurchase= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autocashpurchase,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCP"),  //'Cash Purchase',
            name:'autocashpurchase',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autocashpurchase",WtfGlobal.getLocaleText("acc.accPref.autoCP")]),
            value:Wtf.account.companyAccountPref.autocashpurchase
        });
        this.autobillinginvoice= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillinginvoice,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //'Invoice',
            name:'autobillinginvoice',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillinginvoice",WtfGlobal.getLocaleText("acc.accPref.autoInvoice")]),
            value:Wtf.account.companyAccountPref.autobillinginvoice
        });
        this.autobillingreceipt= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillingreceipt,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoRP"),  //'Receive Payment',
            name:'autobillingreceipt',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillingreceipt",WtfGlobal.getLocaleText("acc.accPref.autoRP")]),
            value:Wtf.account.companyAccountPref.autobillingreceipt
        });
        this.autobillingcashsales= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillingcashsales,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCS"),  //'Cash Sales',
            name:'autobillingcashsales',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillingcashsales",WtfGlobal.getLocaleText("acc.accPref.autoCS")]),
            value:Wtf.account.companyAccountPref.autobillingcashsales
        });
        this.autobillinggoodsreceipt= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillinggoodsreceipt,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoVI"),  //'Vendor Invoice',
            name:'autobillinggoodsreceipt',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillinggoodsreceipt",WtfGlobal.getLocaleText("acc.accPref.autoVI")]),
            value:Wtf.account.companyAccountPref.autobillinggoodsreceipt
        });
        this.autobillingdebitnote= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillingdebitnote,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoDN"),  //'Debit Note',
            name:'autobillingdebitnote',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillingdebitnote",WtfGlobal.getLocaleText("acc.accPref.autoDN")]),
            value:Wtf.account.companyAccountPref.autobillingdebitnote
        });
        this.autobillingcreditmemo= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillingcreditmemo,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCN"),  //'Credit Note',
            name:'autobillingcreditmemo',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillingcreditmemo",WtfGlobal.getLocaleText("acc.accPref.autoCN")]),
            value:Wtf.account.companyAccountPref.autobillingcreditmemo
        });
        this.autobillingpayment= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillingpayment,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Make Payment',
            name:'autobillingpayment',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillingpayment",WtfGlobal.getLocaleText("acc.accPref.autoMP")]),
            value:Wtf.account.companyAccountPref.autobillingpayment
        });
        this.autobillingso= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillingso,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSO"),  //'Sales Order',
            name:'autobillingso',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillingso",WtfGlobal.getLocaleText("acc.accPref.autoSO")]),
            value:Wtf.account.companyAccountPref.autobillingso
        });
        this.autobillingpo= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillingpo,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoPO"),  //'Purchase Order',
            name:'autobillingpo',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillingpo",WtfGlobal.getLocaleText("acc.accPref.autoPO")]),
            value:Wtf.account.companyAccountPref.autobillingpo
        });
        this.autobillingcashpurchase= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobillingcashpurchase,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCP"),  //'Cash Purchase',
            name:'autobillingcashpurchase',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobillingcashpurchase",WtfGlobal.getLocaleText("acc.accPref.autoCP")]),
            value:Wtf.account.companyAccountPref.autobillingcashpurchase
        });
        this.autopurchaserequisition= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autorequisition,
            fieldLabel:WtfGlobal.getLocaleText("acc.preqList.tabTitle"),  //'Quotation',
            name:'autorequisition',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autorequisition",WtfGlobal.getLocaleText("acc.preqList.tabTitle")]),
            value:Wtf.account.companyAccountPref.autorequisition
        });
        this.autorequestforquotation= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autorequestforquotation,
            fieldLabel:WtfGlobal.getLocaleText("acc.requestQuotationList.tabTitle"),  //'Quotation',
            name:'autorequestforquotation',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autorequestforquotation",WtfGlobal.getLocaleText("acc.requestQuotationList.tabTitle")]),
            value:Wtf.account.companyAccountPref.autorequestforquotation
        });
        this.autovenquotation= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autovenquotation,
            fieldLabel: WtfGlobal.getLocaleText("acc.fxexposure.vendor")+" "+ WtfGlobal.getLocaleText("acc.accPref.autoQN"),  //'Quotation',
            name:'autovenquotation',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autovenquotation",WtfGlobal.getLocaleText("acc.accPref.autoQN")]),
            value:Wtf.account.companyAccountPref.autovenquotation
        });
        this.autoquotation = new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoquotation,
            fieldLabel:WtfGlobal.getLocaleText("acc.fxexposure.customer")+" " + WtfGlobal.getLocaleText("acc.accPref.autoQN"),  //'Quotation',
            name:'autoquotation',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoquotation",WtfGlobal.getLocaleText("acc.accPref.autoQN")]),
            value:Wtf.account.companyAccountPref.autoquotation
        });
        this.autodo= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autodo,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoDO"),  //'Delivery Order',
            name:'autodo',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autodo",WtfGlobal.getLocaleText("acc.accPref.autoDO")]),
            value:Wtf.account.companyAccountPref.autodo
        });
        this.autogro= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autogro,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoGRO"),  //'Delivery Order',
            name:'autogro',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autogro",WtfGlobal.getLocaleText("acc.accPref.autoGRO")]),
            value:Wtf.account.companyAccountPref.autogro
        });
        this.autosr= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autosr,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSR"),  //'Delivery Order',
            name:'autosr',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autosr",WtfGlobal.getLocaleText("acc.accPref.autoSR")]),
            value:Wtf.account.companyAccountPref.autosr
        });
        this.autopr= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autopr,
            fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.18"),  //'Delivery Order',
            name:'autopr',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autopr",WtfGlobal.getLocaleText("acc.dimension.module.18")]),
            value:Wtf.account.companyAccountPref.autopr
        });
        this.autocheque= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autocheque,
//            fieldLabel:WtfGlobal.getLocaleText("acc.field.ChequeNumber"),  //'Cheque Number',
            fieldLabel:"Payment/Cheque No.",  //'Cheque Number',
            name:'autocheque',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addChequeNumber.createDelegate(this,["autocheque"]),
            value:Wtf.account.companyAccountPref.autocheque
        });
        this.autoproductid= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoproductid,
            fieldLabel:WtfGlobal.getLocaleText("acc.product.gridProductID"),  //'Product ID',
            name:'autoproductid',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoproductid",WtfGlobal.getLocaleText("acc.product.gridProductID")]),
            value:Wtf.account.companyAccountPref.autoproductid
        });
        this.autocustomerid= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autocustomerid,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCustomerid"),  //'Customer ID',
            name:'autocustomerid',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autocustomerid",WtfGlobal.getLocaleText("acc.accPref.autoCustomerid")]),
            value:Wtf.account.companyAccountPref.autocustomerid
        });
        this.autovendorid= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autovendorid,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoVendorid"),  //'Vendor ID',
            name:'autovendorid',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autovendorid",WtfGlobal.getLocaleText("acc.accPref.autoVendorid")]),
            value:Wtf.account.companyAccountPref.autovendorid
        });
        this.autosalesbaddebtclaimid= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autosalesbaddebtclaimid,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSalesBadDebtClaim"),  //'Sales Bad Debt Claim',
            name:'autosalesbaddebtclaimid',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autosalesbaddebtclaimid",WtfGlobal.getLocaleText("acc.accPref.autoSalesBadDebtClaim")]),
            value:Wtf.account.companyAccountPref.autosalesbaddebtclaimid,
            hideLabel:Wtf.account.companyAccountPref.countryid!='137',// only for malasian company
            hidden:Wtf.account.companyAccountPref.countryid!='137',// only for malasian company
            hideParent:Wtf.account.companyAccountPref.countryid!='137'

        });
        this.autosalesbaddebtrecoverid= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autosalesbaddebtrecoverid,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSalesBadDebtRecover"),  //'Sales Bad Debt Recover',
            name:'autosalesbaddebtrecoverid',
            readOnly:true,
            anchor:'80%',
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autosalesbaddebtrecoverid",WtfGlobal.getLocaleText("acc.accPref.autoSalesBadDebtRecover")]),
            value:Wtf.account.companyAccountPref.autosalesbaddebtrecoverid,
            hideLabel:Wtf.account.companyAccountPref.countryid!='137',// only for malasian company
            hidden: Wtf.account.companyAccountPref.countryid!='137',
            hideParent:Wtf.account.companyAccountPref.countryid!='137'// only for malasian company

        });
        this.autopurchasebaddebtclaimid= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autopurchasebaddebtclaimid,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoPurchaseBadDebtClaim"),  //'Sales Bad Debt Recover',
            name:'autopurchasebaddebtclaimid',
            readOnly:true,
            anchor:'80%',
            addNewFn:this.addNewFormatFormat.createDelegate(this,["autopurchasebaddebtclaimid",WtfGlobal.getLocaleText("acc.accPref.autoPurchaseBadClaim")]),
            value:Wtf.account.companyAccountPref.autopurchasebaddebtclaimid,
            hideLabel:(Wtf.account.companyAccountPref.countryid!='137'),// only for malasian company
            hidden:(Wtf.account.companyAccountPref.countryid!='137'),// only for malasian company
            hideParent:(Wtf.account.companyAccountPref.countryid!='137')

        });
        this.autopurchasebaddebtrecoverid= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autopurchasebaddebtrecoverid,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoPurchaseBadDebtRecover"),  //'Sales Bad Debt Recover',
            name:'autopurchasebaddebtRecoverid',
            readOnly:true,
            anchor:'80%',
            addNewFn:this.addNewFormatFormat.createDelegate(this,["autopurchasebaddebtrecoverid",WtfGlobal.getLocaleText("acc.accPref.autoPurchaseBadRecover")]),
            value:Wtf.account.companyAccountPref.autopurchasebaddebtrecoverid,
            hideLabel: (Wtf.account.companyAccountPref.countryid!='137'),// only for malasian company
            hidden:Wtf.account.companyAccountPref.countryid!='137',// only for malasian company
            hideParent:Wtf.account.companyAccountPref.countryid!='137'

        });
        this.autobuildassembly= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autobuildassembly,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoBuildAssembly"),  //'Vendor ID',
            name:'autobuildassembly',
            readOnly:true,
            anchor:'80%',
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autobuildassembly",WtfGlobal.getLocaleText("acc.accPref.autoBuildAssembly")]),
            value:Wtf.account.companyAccountPref.autobuildassembly
        });
        this.autounbuildassembly= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autounbuildassembly,
            fieldLabel:WtfGlobal.getLocaleText("acc.productList.unBuildAssembly"),  //'Unbuild Assembly',
            name:'autounbuildassembly',
            readOnly:true,
            anchor:'80%',
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autounbuildassembly",WtfGlobal.getLocaleText("acc.productList.unBuildAssembly")]),
            value:Wtf.account.companyAccountPref.autounbuildassembly
        });
        this.reconcilenumber= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autounbuildassembly,
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg6"),  //'Reconcile No.',
            name:'autoreconcilenumber',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoreconcilenumber",WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg6")]),
            value:Wtf.account.companyAccountPref.autoreconcilenumber
        });
        this.unreconcilenumber= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autounbuildassembly,
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg8"),  //'Reconcile No.',
            name:'autounreconcilenumber',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autounreconcilenumber",WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg6")]),
            value:Wtf.account.companyAccountPref.autounreconcilenumber
        });
        this.autoassetgroup= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoassetgroup,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoAssetGroup"),
            name:'autoassetgroup',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoassetgroup",WtfGlobal.getLocaleText("acc.accPref.autoAssetGroup")]),
            value:Wtf.account.companyAccountPref.autoassetgroup
        });
        /*
         * Job Work Out Module
         */
        this.autojwo= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autojwo,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoJWO"),
            name:'autojwo',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autojwo",WtfGlobal.getLocaleText("acc.accPref.autoJWO")]),
            value:Wtf.account.companyAccountPref.autojwo

        });
        /*
         * Company Prefrences have setting to provide security gate entry link on
         * Navigation panel
         */

        this.securityNo= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoSecurityNo,
            fieldLabel:WtfGlobal.getLocaleText("acc.securitygate.title"),
            name:'autoSecurityNo',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoSecurityNo",WtfGlobal.getLocaleText("acc.securitygate.title")]),
            value:Wtf.account.companyAccountPref.autoSecurityNo
        });

        this.autoRG23EntryNumber= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoassetgroup,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.RG23DEntryNumber"),
            name:'autoRG23EntryNumber',
            anchor:'80%',
            readOnly:true,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hideParent:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.account.companyAccountPref.registrationType==Wtf.registrationTypeValues.DEALER)?false:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoRG23EntryNumber",WtfGlobal.getLocaleText("acc.accPref.autoRG23EntryNumber")]),
            value:Wtf.account.companyAccountPref.autoRG23EntryNumber
        });
        /**
         * Sequence format for Packing Delivery Order
         */
        this.autopackingdo= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autopackingdo,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autopackingdo"),
            name:'autopackingdo',
            anchor:'80%',
            readOnly:true,
            hidden:!Wtf.account.companyAccountPref.pickpackship,
            hideLabel:!Wtf.account.companyAccountPref.pickpackship,
            hideParent:!Wtf.account.companyAccountPref.pickpackship,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autopackingdo",WtfGlobal.getLocaleText("acc.accPref.autopackingdo")]),
            value:Wtf.account.companyAccountPref.autopackingdo
        });
        /**
         * Sequence format for Shipping Delivery Order
         */
        this.autoshippingdo= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoshippingdo,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoshippingdo"),  //'Vendor ID',
            name:'autoshippingdo',
            anchor:'80%',
            readOnly:true,
            hidden:!Wtf.account.companyAccountPref.pickpackship,
            hideLabel:!Wtf.account.companyAccountPref.pickpackship,
            hideParent:!Wtf.account.companyAccountPref.pickpackship,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoshippingdo",WtfGlobal.getLocaleText("acc.accPref.autoshippingdo")]),
            value:Wtf.account.companyAccountPref.autoshippingdo
        });
        this.autoloanrefnumber= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.autoloanrefnumber,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoloanrefnumber"),  //'Vendor ID',
            name:'autoloanrefnumber',
            anchor:'80%',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["autoloanrefnumber",WtfGlobal.getLocaleText("acc.accPref.autoloanrefnumber")]),
            value:Wtf.account.companyAccountPref.autoloanrefnumber
        });
        this.dimensionnumber = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.accPref.autodimensionnumber"),
            tooltip: WtfGlobal.getLocaleText("acc.accPref.autodimensionnumber"),
            disabled: false,
            scope: this,
            handler: this.addNewFormatFormat.createDelegate(this,["autodimensionnumber",WtfGlobal.getLocaleText("acc.accPref.autodimensionnumber")])
        });
        this.automachineid= new Wtf.form.TextFieldAddNewBtn({
            toolTip : Wtf.account.companyAccountPref.automachineid,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoMachineId"), //'Machine ID',
            name:'automachineid',
            readOnly:true,
            addNewFn: this.addNewFormatFormat.createDelegate(this,["automachineid",WtfGlobal.getLocaleText("acc.accPref.autoMachineId")]),
            value:Wtf.account.companyAccountPref.automachineid
        });

        this.instLocation= new Wtf.form.TextFieldAddNewBtn({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoInstLocation"),
            name:'autoInstLocation',
            allowBlank:true,
            readOnly:true,
            addNewFn: this.addNewFormatFormatForInv.createDelegate(this,["autoInstLocation","Inter Location Transfer"])
        });

        this.instStore= new Wtf.form.TextFieldAddNewBtn({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoInstStore"),
            name:'autoInstStore',
            allowBlank:true,
            readOnly:true,
            addNewFn: this.addNewFormatFormatForInv.createDelegate(this,["autoInstStore","Inter Store Transfer"])
        });
        this.instStockIssue= new Wtf.form.TextFieldAddNewBtn({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoIssueNote"),
            name:'autoIssueNote',
            allowBlank:true,
            readOnly:true,
            addNewFn: this.addNewFormatFormatForInv.createDelegate(this,["autoIssueNote","Issue Note"])
        });
        this.autoStockAdj= new Wtf.form.TextFieldAddNewBtn({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSA"),
            name:'autoSA',
            allowBlank:true,
            readOnly:true,
            addNewFn: this.addNewFormatFormatForInv.createDelegate(this,["autoSA","Stock Adjustment"])
        });

        this.autoStockRequest= new Wtf.form.TextFieldAddNewBtn({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSRequest"),
            name:'autoSRequest',
            allowBlank:true,
            readOnly:true,
            addNewFn: this.addNewFormatFormatForInv.createDelegate(this,["autoSRequest","Stock Request"]),
            renderer:this.getAllInventorySequFormat()
        });
        this.autoCycleCount= new Wtf.form.TextFieldAddNewBtn({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCycleCount"),
            name:'autoCycleCount',
            allowBlank:true,
            readOnly:true,
            addNewFn: this.addNewFormatFormatForInv.createDelegate(this,["autoCycleCount","Cycle Count"]),
            renderer:this.getAllInventorySequFormat()
        });
        this.skuSequence= new Wtf.form.TextFieldAddNewBtn({
            fieldLabel:"Asset module",
            name:'assetid',
            allowBlank:true,
            readOnly:true,
            addNewFn: this.addNewFormatFormatForInv.createDelegate(this,["assetid","Asset Id"]),
            renderer:this.getAllInventorySequFormat()
        });

        this.daysStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'daysid',
                type:'int'
            }, 'name'],
            data :[[1,'1'],[2,'2'],[3,'3'],[4,'4'],[5,'5'],[6,'6'],[7,'7'],[8,'8'],[9,'9'],[10,'10'],
            [11,'11'],[12,'12'],[13,'13'],[14,'14'],[15,'15'],[16,'16'],[17,'17'],[18,'18'],[19,'19'],[20,'20'],
            [21,'21'],[22,'22'],[23,'23'],[24,'24'],[25,'25'],[26,'26'],[27,'27'],[28,'28'],[29,'29'],[30,'30'],[31,'31']]
        });
        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'monthid',
                type:'int'
            }, 'name'],
            data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
            [10,'November'],[11,'December']]
        });

        this.lockEditor= new Wtf.form.Checkbox({
            boxLabel:" ",
            name:'rectype',
            inputValue:'2'
        });
        this.fdays = new Wtf.form.ComboBox({
            store:  this.daysStore,
            name:'daysid',
            displayField:'name',
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.firstfinancialYearDate"),  //'Financial Year Date',
            forceSelection: true,
            valueField:'daysid',
            mode: 'local',
            anchor:'95%',
            triggerAction: 'all',
            selectOnFocus:true,
            listeners:{
                scope:this,
                select: function(combo, record, index) {
                    if (!Wtf.account.companyAccountPref.openingDepreciationPosted) {
                        var y1 = combo.getValue();
                        this.byear.setValue(this.finanyear.getValue());
                        this.bmonth.setValue(this.fmonth.getValue());
                        this.bdays.setValue(y1);

                        var fdate = new Date(this.finanyear.getValue(), this.fmonth.getValue(), this.fdays.getValue());
                        var bdate = new Date(this.byear.getValue(), this.bmonth.getValue(), this.bdays.getValue());
                        var originalbdate = new Date(this.byear.originalValue, this.bmonth.originalValue, this.bdays.originalValue);
                        var originalfdate = new Date(this.finanyear.originalValue, this.fmonth.originalValue, this.fdays.originalValue);
                        if (new Date(fdate) > new Date(originalbdate)) {
                            this.checkanytransaction();
                            this.checkpreviousyearlock();
                        } else if (new Date(fdate) < new Date(originalbdate)) {
                            this.checkpreviousyearlock();
                            this.checkOpeningTransactions();
                            this.gridsetvalue();
                        } else {
                            this.checkpreviousyearlock();
                            this.gridsetvalue();
                        }
                        this.fdays.collapse();
                    } else {
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.information"),
                            msg: WtfGlobal.getLocaleText("acc.messages.cannotchangepreferences"),
                            buttons: Wtf.MessageBox.OK,
                            icon: Wtf.MessageBox.INFO
                        });
                        this.fdays.setValue(new Date(Wtf.account.companyAccountPref.firstfyfrom).getDate());
                    }
                }
            }
        });
        this.fmonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'monthid',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            listeners:{
                scope:this,
                select: function(combo, record, index) {
                    if (!Wtf.account.companyAccountPref.openingDepreciationPosted) {
                        var y1 = combo.getValue();
                        this.byear.setValue(this.finanyear.getValue());
                        this.bmonth.setValue(y1);
                        this.bdays.setValue(this.fdays.getValue());

                        var fdate = new Date(this.finanyear.getValue(), this.fmonth.getValue(), this.fdays.getValue());
                        var bdate = new Date(this.byear.getValue(), this.bmonth.getValue(), this.bdays.getValue());
                        var originalbdate = new Date(this.byear.originalValue, this.bmonth.originalValue, this.bdays.originalValue);
                        var originalfdate = new Date(this.finanyear.originalValue, this.fmonth.originalValue, this.fdays.originalValue);
                        if (new Date(fdate) > new Date(originalbdate)) {
                            this.checkanytransaction();
                            this.checkpreviousyearlock();
                        } else if (new Date(fdate) < new Date(originalbdate)) {
                            this.checkpreviousyearlock();
                            this.checkOpeningTransactions();
                            this.gridsetvalue();
                        } else {
                            this.checkpreviousyearlock();
                            this.gridsetvalue();
                        }
                        this.fmonth.collapse();
                    } else {
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.information"),
                            msg: WtfGlobal.getLocaleText("acc.messages.cannotchangepreferences"),
                            buttons: Wtf.MessageBox.OK,
                            icon: Wtf.MessageBox.INFO
                        });
                        this.fmonth.setValue(new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth());
                    }
                }
            }
        });
        this.bdays = new Wtf.form.ComboBox({
            store:  this.daysStore,
            name:'daysid',
            displayField:'name',
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.bookBeginingDate"),  //'Book Beginning Date',
            valueField:'daysid',
            mode: 'local',
            forceSelection: true,
            anchor:'95%',
            triggerAction: 'all',
            selectOnFocus:true,
            listeners:{
                scope:this,
                select: function(combo, record, index) {
                    var fdate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
                    var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
                    var originalbdate=new Date(this.byear.originalValue,this.bmonth.originalValue,this.bdays.originalValue);

                    if(new Date(fdate)>new Date(bdate)){
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.information"),
                            msg: WtfGlobal.getLocaleText("acc.common.FinanBookAlert"),
                            buttons: Wtf.MessageBox.OK,
                            icon: Wtf.MessageBox.INFO
                        });
                        this.bdays.setValue(this.fdays.getValue());
                        this.bmonth.setValue(this.fmonth.getValue());
                        this.byear.setValue(this.finanyear.getValue());
                    }else{
                        if(new Date(bdate)>new Date(originalbdate)){
                            this.checkanytransaction();
                            this.checkpreviousyearlock();
                            this.gridsetvalue();
                        }else if (new Date(bdate) < new Date(originalbdate)) {
                            this.checkpreviousyearlock();
                            this.checkOpeningTransactions();
                            this.gridsetvalue();
                        }else{
                            this.checkpreviousyearlock();
                            this.gridsetvalue();
                        }
                    }
                    this.bdays.collapse();
                }
            }
        });

        this.bmonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'monthid',
            displayField:'name',
            anchor:'95%',
            forceSelection: true,
            valueField:'monthid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            listeners:{
                scope:this,
                select: function(combo, record, index) {
                    var fdate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
                    var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
                    var originalbdate=new Date(this.byear.originalValue,this.bmonth.originalValue,this.bdays.originalValue);
                    var originalfdate=new Date(this.finanyear.originalValue,this.fmonth.originalValue,this.fdays.originalValue);

                    if(new Date(fdate)>new Date(bdate)){
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.information"),
                            msg: WtfGlobal.getLocaleText("acc.common.FinanBookAlert"),
                            buttons: Wtf.MessageBox.OK,
                            icon: Wtf.MessageBox.INFO
                        });
                        this.bdays.setValue(this.fdays.getValue());
                        this.bmonth.setValue(this.fmonth.getValue());
                        this.byear.setValue(this.finanyear.getValue());
                    }else{
                        if(new Date(bdate)>new Date(originalbdate)){
                            this.checkanytransaction();
                            this.checkpreviousyearlock();
                            this.gridsetvalue();
                        }else if (new Date(bdate) < new Date(originalbdate)) {
                            this.checkpreviousyearlock();
                            this.checkOpeningTransactions();
                            this.gridsetvalue();
                        }else{
                            this.checkpreviousyearlock();
                            this.gridsetvalue();
                        }
                    }
                    this.bmonth.collapse();
                }
            }
        });
        var data=this.getBookBeginningYear(true);
        this.yearStore= new Wtf.data.SimpleStore({
            fields: [{
                name:'id',
                type:'int'
            }, 'yearid'],
            data :data
        });
        //for financial year date-Neeraj D
        var currentTime = new Date();
        var now = currentTime.getFullYear()+1;
        var years = [];
        var y = 2000;
        while(y<=now+2){
            years.push([y]);
            y++;
        }
        this.storeThn = new Wtf.data.SimpleStore({
            fields: [ 'financialyears' ],
            data: years
        });
        this.finanyear = new Wtf.form.ComboBox({
            store: this.storeThn,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'yearid',
            xtype: 'combo',
            displayField:'financialyears',
            anchor:'95%',
            valueField:'financialyears',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            listClass: 'x-combo-list-small',
            typeAhead: false,
            allowBlank: false,
            listeners:{
                scope:this,
                beforequery: function() {
                    this.finanyear.store.loadData(years);
                },
                select: function(combo, record, index) {
                    if (!Wtf.account.companyAccountPref.openingDepreciationPosted) {
                        var y1 = combo.getValue();
                        this.byear.setValue(y1);
                        var fdate = new Date(this.finanyear.getValue(), this.fmonth.getValue(), this.fdays.getValue());
                        var bdate = new Date(this.byear.getValue(), this.bmonth.getValue(), this.bdays.getValue());
                        var originalbdate = new Date(this.byear.originalValue, this.bmonth.originalValue, this.bdays.originalValue);
                        var originalfdate = new Date(this.finanyear.originalValue, this.fmonth.originalValue, this.fdays.originalValue);
                        if (new Date(fdate) > new Date(originalfdate)) {
                            this.checkanytransaction();//if transaction is made after old financial year date.
                            this.checkpreviousyearlock();
                        } else if (new Date(fdate) < new Date(originalbdate)) {
                            this.checkpreviousyearlock();
                            this.checkOpeningTransactions();
                            this.gridsetvalue();
                        } else {
                            this.gridsetvalue();
                        }
                        this.finanyear.collapse();
                    } else {
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.information"),
                            msg: WtfGlobal.getLocaleText("acc.messages.cannotchangepreferences"),
                            buttons: Wtf.MessageBox.OK,
                            icon: Wtf.MessageBox.INFO
                        });
                        this.finanyear.setValue(new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear());
                    }
                }
            }
        });

        this.byear = new Wtf.form.ComboBox({
            store: this.storeThn,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'yearid',
            displayField:'financialyears',
            anchor:'95%',
            valueField:'financialyears',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            listClass: 'x-combo-list-small',
            typeAhead: false,
            allowBlank: false,
            listeners:{
                scope:this,
                beforequery: function() {
                    var y1=this.finanyear.getValue();
                    var currentTime1 = new Date();
                    var now1 = currentTime1.getFullYear()+1;
                    var years1 = [];
                    while(y1<=now1){
                        years1.push([y1]);
                        y1++;
                    }
                    this.byear.store.loadData(years1);
                },
                select: function(combo, record, index) {
                    this.byear.collapse();
                    var fdate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
                    var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
                    var originalbdate=new Date(this.byear.originalValue,this.bmonth.originalValue,this.bdays.originalValue);

                    if(new Date(fdate)>new Date(bdate)){
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.information"),
                            msg: WtfGlobal.getLocaleText("acc.common.FinanBookAlert"),
                            buttons: Wtf.MessageBox.OK,
                            icon: Wtf.MessageBox.INFO
                        });
                        this.bdays.setValue(this.fdays.getValue());
                        this.bmonth.setValue(this.fmonth.getValue());
                        this.byear.setValue(this.finanyear.getValue());
                    }else{
                        if(new Date(bdate)>new Date(originalbdate)){
                            this.checkanytransaction();//checking for transaction
                            this.checkpreviousyearlock();//checking previous year lock
                        }else if (new Date(bdate) < new Date(originalbdate)) {
                            this.checkpreviousyearlock();
                            this.checkOpeningTransactions();
                            this.gridsetvalue();
                        }else{
                            this.checkpreviousyearlock();
                            this.gridsetvalue();
                        }
                    }
                }
            }
        });
        this.companyAddressButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.accPref.accManageComapanyAddress"),
            tooltip: WtfGlobal.getLocaleText("acc.accPref.accManageComapanyAddresstt"),
            disabled: false,
            scope: this,
            handler: function(){
               showAddressWindowForCompany();
            }
        });
//        this.remitPaymentTo1 = new Wtf.form.TextArea({
//            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.accRemitPaymentto"),
//            name:'remitpaymentto1',
//            height:40,
//            maxLength: 1024,
//            width : 240,
//            emptyText :WtfGlobal.getLocaleText("acc.field.EnterRemitPaymentAddress")
//        });
        this.remitPaymentTo = new Wtf.form.HtmlEditor({
            name: 'remitpaymentto',
            width:520,
            maxLength: 4096,
            height:150,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.accRemitPaymentto"),
            hideLabel:true,
            anchor:'90%',
            enableFont:false,
            plugins: [
//                new Wtf.ux.form.HtmlEditor.IndentOutdent({})
                new Wtf.ux.form.HtmlEditor.Table({}),
                new Wtf.ux.form.HtmlEditor.TableCell({})
            ]
        });
        this.remitTextPanel =new Wtf.Panel({
            html: WtfGlobal.getLocaleText("acc.accPref.accRemitPaymentto")+':',
            border :false
        });

         this.isAddressFromVendorMaster= new Wtf.form.Checkbox({                               //Show address in Document form company master.
            name:'isaddressfromvendormaster',
            id:this.id+'isaddressfromvendormaster',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.addFromCompnymaster"),
            checked :Wtf.account.companyAccountPref.isAddressFromVendorMaster,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true,
            anchor:'70%'
        });
        /**
         * For IDNIA country if transaction created then "Show vendors address in Document" check restrict ON/OFF
         */
        if(WtfGlobal.isIndiaCountryAndGSTApplied()){
            this.isAddressFromVendorMaster.on('change',this.isPurchaseTransactionCreated,this);
        }
        this.sendMailTo=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Sendacopyto"),
            name:'sendmailto',
            id:this.id+'sendMailTo',
            maxLength:100,
            scope:this,
            anchor:'95%',
            validator:WtfGlobal.validateMultipleEmail
        });

        this.sendImportMailTo=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Sendacopyto"),
            name:'sendimportmailto',
            id:this.id+'sendImportMailTo',
            maxLength:100,
            scope:this,
            anchor:'95%',
            validator:WtfGlobal.validateMultipleEmail,
            value:Wtf.account.companyAccountPref.sendimportmailto
        });

        this.userRec = new Wtf.data.Record.create([
        {
            name: 'userid'
        },
        {
            name: 'fullname'
        }
        ,
        {
            name: 'emailid'
        }

        ]);

        this.userReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.userRec);

        this.userStore = new Wtf.data.Store({
            url: 'ProfileHandler/getAllUserDetails.do',
            reader:this.userReader
        });
        this.userEmailComboList = {
            emptyText:WtfGlobal.getLocaleText("acc.field.importMailUsers"),
            name: 'userEmailComboList',
            store:this.userStore,
            typeAhead: true,
            selectOnFocus:true,
            valueField:'emailid',
            displayField: 'fullname',
            lastQuery:'',
            scope:this,
            hirarchical:true,
            triggerAction: 'all',
            name:'useremails',
            hiddenName:'useremails'
        };

        this.userEmailCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.importMailUsers"),
            forceSelection:true,
            listWidth:300,
            width:180
        },this.userEmailComboList));

        this.userStore.load({
            params:{
                isActive : "true"
            }
        });
        this.userStore.on("load", function(){
            this.userEmailCombo.setValue(Wtf.account.companyAccountPref.useremails);
        }, this);

        this.approvalMail= new Wtf.form.Checkbox({
            name:'approvalMail',
            id:this.id+'approvalMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.AllowSendingApprovalMail"),
            checked :Wtf.account.companyAccountPref.approvalMail,
            scope:this,
            cls : 'custcheckbox',
            width: 10
        });

        this.approvalMail.on('check',this.setMailIds,this);

        this.createDocumentEmailSettingFields();



        this.salesAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:Wtf.account.companyAccountPref.recurringDeferredRevenueRecognition ? WtfGlobal.getLocaleText("acc.product.advanceSalesAcc") : WtfGlobal.getLocaleText("acc.product.salesAcc"),//'Sales Account*',
            store:this.salesAccStoreForRevenueRecognitionMannualJE,
            name:'salesAccount',
            anchor:'85%',
            hiddenName:'salesAccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });
        this.salesRevenueRecognitionAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.product.salesRevenueRecognitionAccount"),
            store:this.salesAccStore,
            name:'salesRevenueRecognitionAccount',
            anchor:'85%',
            hiddenName:'salesRevenueRecognitionAccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
            forceSelection: true,
            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });
        this.liabilityAccountForLMS = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.liabilityAccountForLMS"),
            store: this.liabilityAccStore,
            name: 'liabilityAccountForLMS',
            anchor: '85%',
            hiddenName: 'liabilityAccountForLMS',
            extraFields: [],
            extraComparisionField: 'acccode',
            valueField: 'accid',
            displayField: 'accname',
            forceSelection: true,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
//            allowBlank: false,
            hidden:!Wtf.account.companyAccountPref.isLMSIntegration,
            hideLabel:!Wtf.account.companyAccountPref.isLMSIntegration
                    //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });

        this.storeRec = new Wtf.data.Record.create([
        {
            name:"store_id"
        },

        {
            name:"abbr"
        },

        {
            name:"fullname"
        },
        {
            name:"defaultlocationid"
        },
        {
            name:"defaultlocation"
        }
        ]);

        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);

        this.storeList = new Wtf.data.Store({
            url :"INVStore/getStoreList.do",
            reader:this.storeReader
        });
        //ERM-691 Display specific Scrap Store in the combo
        this.ScrapStoreList = new Wtf.data.Store({
            url :"INVStore/getStoreList.do",
            reader:this.storeReader
        });

       this.ScrapStoreList .load({  //store used for displaying scrap stores in the scrap combo
            params:{
                isActive : "true",
                isScrapstoreonly:true
            }
        });
        //ERM-691 Display specific Repair Store in the combo
        this.RepairStoreList = new Wtf.data.Store({
            url :"INVStore/getStoreList.do",
            reader:this.storeReader
        });

       this.RepairStoreList.load({  //store used for displaying repair type stores in the repair combo
            params:{
                isActive : "true",
                isRepairStoreOnly:true,
                includePickandPackStore:false,
                includeQAAndRepairStore:true
            }
        });

       this.storeList.load({
            params:{
                isActive : "true",
                includeQAAndRepairStore:true,
                includePickandPackStore:true,
                isFromCompanyPreferences:true
            }
        });
        this.locationRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"isactive"
        },

        {
            name:"name"
        }
        ]);

        this.locationReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.locationRec);

        this.locationList = new Wtf.data.Store({
            url :"INVLocation/getLocations.do",
            reader:this.locationReader
        });

       this.locationList.load({
            params:{
                isActive : "true"
            }
        });

        this.locationList.on('load', function() {
            this.packinglocation.setValue(Wtf.account.companyAccountPref.packinglocation);
        }, this);

        this.inspectionStoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.storeList,
            displayField:"fullname",
            valueField:"store_id",
            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.storeForQAInspection"),
            hiddenName:"inspectionStore",
            width:200,
            listWidth:200,
            parent:this,
            id: 'inspectionStore' + this.id,
            name: 'inspectionStore',
            emptyText:"Inspection Warehouse",
//            value: Wtf.account.companyAccountPref.inspectionStore,
            disabled : !(Wtf.account.companyAccountPref.activateQAApprovalFlow),
            allowBlank: !(Wtf.account.companyAccountPref.activateQAApprovalFlow)
        });
        this.inspectionStoreCombo.on('change',function(){
            this.checkbeforeQAStoreChange(false,Wtf.account.companyAccountPref.inspectionStore,false,true);
            this.checkStoreUsedInTransaction("fromQA");

        },this);

        this.inspectionStoreCombo.on('select', function(combo, record, index) {
            this.validateStoreSelection(combo, record, index, "fromQA");
        }, this);

        this.repairStoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.RepairStoreList,
            displayField:"fullname",
            valueField:"store_id",
            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.storeForStockRepair"),
            hiddenName:"repairStore",
            width:200,
            listWidth:200,
            parent:this,
            id: 'repairStore' + this.id,
            name: 'repairStore',
            emptyText:"Repair Warehouse",
//            value: Wtf.account.companyAccountPref.repairStore,
            disabled : !(Wtf.account.companyAccountPref.activateQAApprovalFlow),
            allowBlank:!(Wtf.account.companyAccountPref.activateQAApprovalFlow)
        });
        this.scrapStoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.ScrapStoreList,
            displayField:"fullname",
            valueField:"store_id",
            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.scrapStore"),
            hiddenName:"scrapStore",
            width:200,
            listWidth:200,
            parent:this,
            id: 'scrapStore' + this.id,
            name: 'scrapStore',
            emptyText:"Scrap Warehouse",
            disabled : !(Wtf.account.companyAccountPref.activateQAApprovalFlow),
            allowBlank:!(Wtf.account.companyAccountPref.activateQAApprovalFlow)
        });

         this.repairStoreCombo.on('change',function(){
            this.checkbeforeQAStoreChange(false,Wtf.account.companyAccountPref.repairStore,true);
        },this)
         this.scrapStoreCombo.on('change',function(){
            this.checkdefaultlocationmappedforStore(this.ScrapStoreList,this.scrapStoreCombo.getValue());
        },this)

        this.storeList.on('load', function() {
            this.inspectionStoreCombo.setValue(Wtf.account.companyAccountPref.inspectionStore);
            this.packingstore.setValue(Wtf.account.companyAccountPref.packingstore);
            this.vendorJobOrderStore.setValue(Wtf.account.companyAccountPref.vendorjoborderstore);
        }, this);
        this.ScrapStoreList.on('load', function() {
            this.scrapStoreCombo.setValue(Wtf.account.companyAccountPref.columnPref.scrapStore); //ERM-691 Scrap Store feature for GRN QA flow
        }, this);
        this.RepairStoreList.on('load', function() {
            this.repairStoreCombo.setValue(Wtf.account.companyAccountPref.repairStore); //ERM-691 multiple Repair Store feature for GRN QA flow
        }, this);

        this.defaultWarehouseRecord = new Wtf.data.Record.create([
            {name: 'warehouse'}, // Warehouse ID
            {name: 'name'},
            {name: 'customer'},
            {name: 'company'},
            {name: 'doids'}
        ]);

        this.defaultWarehouseStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            },this.defaultWarehouseRecord),
            url: "ACCCustomerCMN/getAllCustomerWarehouse.do",
            baseParams: {
                isForCustomer: true,
                groupbywarehouse:true
            }
        });

        this.defaultWarehouse = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.defaultCustomerWarehouse"),
            mode: "local",
            triggerAction: 'all',
            typeAhead: true,
            hidden:Wtf.defaultReferralKeyflag,
            hideLabel:Wtf.defaultReferralKeyflag,
            id: 'defaultWarehouse' + this.id,
            name: 'defaultWarehouse',
            store: this.defaultWarehouseStore,
            displayField: "name",
            valueField: 'warehouse',
            emptyText: WtfGlobal.getLocaleText("acc.field.selectAWarehouse"),
            width: 200,
            value: Wtf.account.companyAccountPref.defaultWarehouse
        });

        this.defaultWarehouseStore.load();

        this.defaultWarehouseStore.on('load', function() {
            this.defaultWarehouse.setValue(Wtf.account.companyAccountPref.defaultWarehouse);
        }, this);


        this.stockINQAApproval= new Wtf.form.Checkbox({                               //Show address in Document form company master.
            name:'stockInQAApproval',
            id:this.id+'stockInQAApproval',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.stockInQAApproval"),
            checked : Wtf.account.companyAccountPref.activateQAApprovalFlow,
            scope:this,
            disabled:true,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true,
            anchor:'70%'
        });

        this.interStoreReturnQAApproval= new Wtf.form.Checkbox({                               //Show address in Document form company master.
            name:'interStoreQAApproval',
            id:this.id+'interstoreQAApproval',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.interStoreReturnQAApproval"),
            checked : Wtf.account.companyAccountPref.activateQAApprovalFlow,
            scope:this,
            disabled:true,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true,
            anchor:'70%'
        });

        this.isQaApprovalFlow = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.QAApprovalFlowinGoodsReceiptNote"),
            labelStyle: 'width: 340px;',
            name: 'isQaApprovalFlow',
            autoWidth: true,
            anchor: '70%',
            disabled: (Wtf.account.companyAccountPref.invAccIntegration) ? true : false,
            checked: Wtf.account.companyAccountPref.isQaApprovalFlow
        });
        this.isQaApprovalFlowInDO = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.QAApprovalFlowinDeliveryOrder"),
            labelStyle: 'width: 340px;',
            name: 'isQaApprovalFlowInDO',
            autoWidth: true,
            anchor: '70%',
            disabled: (Wtf.account.companyAccountPref.invAccIntegration) ? true : false,
            checked: Wtf.account.companyAccountPref.isQaApprovalFlowInDO
        });
        this.stockRequestReturnQAApproval= new Wtf.form.Checkbox({                               //Show address in Document form company master.
            name:'stockRequestReturnQAApproval',
            id:this.id+'stockRequestReturnQAApproval',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.stockRequestReturnQAApproval"),
            checked : Wtf.account.companyAccountPref.activateQAApprovalFlow,
            scope:this,
            disabled:true,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true,
            anchor:'70%'
        });
        /**
         * flag for activation of MRP QA flow
         */
        this.isQaApprovalFlowInMRP = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("QA Approval Flow in MRP"),
            labelStyle: 'width: 340px;',
            name: 'isQaApprovalFlowInMRP',
            autoWidth: true,
            hidden: false,
            anchor: '70%',
            disabled: (Wtf.account.companyAccountPref.invAccIntegration) ? true : false,
            checked: Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP
        });

        Wtf.Ajax.requestEx({
            url: "INVConfig/getConfig.do",
            params: {
                cid: companyid
            }
        },
        this,
        function (request, response) {
            if (request && request.data) {
                var stockInQA=(request.data.enableStockOutApprovalFlow != undefined && request.data.enableStockOutApprovalFlow !="") ? request.data.enableStockOutApprovalFlow : false;
                var interStoreQA=(request.data.enableInterStoreApprovalFlow != undefined && request.data.enableInterStoreApprovalFlow !="") ? request.data.enableInterStoreApprovalFlow : false;
                var stockRequestQA=(request.data.enableStockRequestReturnApprovalFlow != undefined && request.data.enableStockRequestReturnApprovalFlow !="") ? request.data.enableStockRequestReturnApprovalFlow : false;

                this.stockINQAApproval.setValue(stockInQA);
                this.interStoreReturnQAApproval.setValue(interStoreQA);
                this.stockRequestReturnQAApproval.setValue(stockRequestQA);
            }
        },
        function () {

        }
        );

        this.gstAccountForBadDebts=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.gst.bad.debt.accounts"),//GST Account For Bad Debts
            store:this.gstAccountForBadDebtsStore,
            name:'gstaccountforbaddebt',
            anchor:'85%',
            hiddenName:'gstaccountforbaddebt',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true
        //            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });


        this.gstBadDebtsReleifAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.gst.bad.debt.releif.accounts"),//GST Bad Debt Relief Sales Account
            store:this.gstBadDebtsReleifAccountStore,
            name:'gstbaddebtreleifaccount',
            anchor:'85%',
            hiddenName:'gstbaddebtreleifaccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true
        //            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });


        this.gstBadDebtsRecoverAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.gst.bad.debt.recover.accounts"),//GST Bad Debt Recover Sales Account
            store:this.gstBadDebtsRecoverAccountStore,
            name:'gstbaddebtrecoveraccount',
            anchor:'85%',
            hiddenName:'gstbaddebtrecoveraccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true
        //            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });

        this.gstBadDebtsReleifPurchaseAccount=new Wtf.form.ExtFnComboBox({     //ERP-10400 ,For Purchase
            fieldLabel:WtfGlobal.getLocaleText("acc.gst.bad.debt.releif.purchaseaccounts"),//GST Bad Debt Relief Purchase Account
            store:this.gstBadDebtsReleifPurchaseAccountStore,
            name:'gstbaddebtreleifpurchaseaccount',
            anchor:'85%',
            hiddenName:'gstbaddebtreleifpurchaseaccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true
        //            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });

        this.taxRec = Wtf.data.Record.create([
        {
            name: 'prtaxid',
            mapping:'taxid'
        },

        {
            name: 'prtaxname',
            mapping:'taxname'
        },

        {
            name: 'taxdescription'
        },

        {
            name: 'percent',
            type:'float'
        },

        {
            name: 'taxcode'
        },

        {
            name: 'accountid'
        },

        {
            name: 'accountname'
        },

        {
            name: 'applydate',
            type:'date'
        },
        {
            name:'hasAccess'
        }

        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad:false
            },this.taxRec),
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33,
                moduleid:Wtf.Acc_Vendor_Invoice_ModuleId
            }
        });
        this.gstBadDebtsRecoverPurchaseAccount=new Wtf.form.ExtFnComboBox({    //ERP-10400, For Purchase
            fieldLabel:WtfGlobal.getLocaleText("acc.gst.bad.debt.recover.purchaseaccounts"),//GST Bad Debt Recover Purchase Account
            store:this.gstBadDebtsRecoverPurchaseAccountStore,
            name:'gstbaddebtrecoverpurchaseaccount',
            anchor:'85%',
            hiddenName:'gstbaddebtrecoverpurchaseaccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true
        //            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });


        this.gstBadDebtsSuspenseAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.gst.bad.debt.suspense.accounts"),//GST Bad Debt Suspense Account
            store:this.gstBadDebtsSuspenseAccountStore,
            name:'gstbaddebtsuspenseaccount',
            anchor:'85%',
            hiddenName:'gstbaddebtsuspenseaccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true
        //            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });

        this.inputTaxAdjustmentAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:"Input Tax Adjustment Account",
            store:this.inputTaxAdjustmentAccountStore,
            name:'inputtaxadjustmentaccount',
            anchor:'85%',
            hiddenName:'inputtaxadjustmentaccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true
        //            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });
        /*
         *CGA Tax for Malaysian Company.
         */
        this.taxCgaMalaysian= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //'Tax',
            id:"tax"+this.heplmodeid+this.id,
            hiddenName:'tax',
            anchor: '97%',
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
            scope:this,
            selectOnFocus:true,
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
        this.outputTaxAdjustmentAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:"Output Tax Adjustment Account",
            store:this.outputTaxAdjustmentAccountStore,
            name:'outputtaxadjustmentaccount',
            anchor:'85%',
            hiddenName:'outputtaxadjustmentaccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true
        //            allowBlank: false
        //            disabled:(Wtf.account.companyAccountPref.isDeferredRevenueRecognition)?false:true
        });

        this.freeGiftJEAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.account.freeGiftJEAccount"),
            store:this.freeGiftJEAccountStore,
            name:'freeGiftJEAccount',
            anchor:'85%',
            hiddenName:'freeGiftJEAccount',
            extraFields:[],
            extraComparisionField:'acccode',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true
        });

        this.noRevenueRecognition=new Wtf.form.Radio({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.NoRevenueRecognition"),
            name:'revenueRecognitionOnDeliveryOrderOrJE',
            checked :!Wtf.account.companyAccountPref.isDeferredRevenueRecognition&&!Wtf.account.companyAccountPref.recurringDeferredRevenueRecognition,
            id:'noRevenueRecognition'
        });
        this.isDeferredRevenueRecognition=new Wtf.form.Radio({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.RevenuerecognitiononDeliveryOrder"),
            name:'revenueRecognitionOnDeliveryOrderOrJE',
            checked :Wtf.account.companyAccountPref.isDeferredRevenueRecognition,
            id:'isDeferredRevenueRecognition'
        });
        this.recurringDeferredRevenueRecognition=new Wtf.form.Radio({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.RevenuerecognitiononmanualJE"),
            name:'revenueRecognitionOnDeliveryOrderOrJE',
            checked :Wtf.account.companyAccountPref.recurringDeferredRevenueRecognition,
            id:'recurringDeferredRevenueRecognition'
        });
        if(Wtf.account.companyAccountPref.countryid == '137'){// Only for Malasian Company
            //
            //            this.gstAccountForBadDebtsStore.on('load',function(){
            //                this.gstAccountForBadDebts.setValue(Wtf.account.companyAccountPref.gstaccountforbaddebt);
            //            },this);
            //            this.gstAccountForBadDebtsStore.load();
            //
            //            //
            //            this.gstBadDebtsReleifAccountStore.on('load',function(){
            //                this.gstBadDebtsReleifAccount.setValue(Wtf.account.companyAccountPref.gstbaddebtreleifaccount);
            //            },this);
            //            this.gstBadDebtsReleifAccountStore.load();
            //
            //            //
            //            this.gstBadDebtsRecoverAccountStore.on('load',function(){
            //                this.gstBadDebtsRecoverAccount.setValue(Wtf.account.companyAccountPref.gstbaddebtrecoveraccount);
            //            },this);
            //            this.gstBadDebtsRecoverAccountStore.load();
            //
            //            //
            //            this.gstBadDebtsSuspenseAccountStore.on('load',function(){
            //                this.gstBadDebtsSuspenseAccount.setValue(Wtf.account.companyAccountPref.gstbaddebtsuspenseaccount);
            //            },this);
            //            this.gstBadDebtsSuspenseAccountStore.load();
            //
            //            //
            //            this.inputTaxAdjustmentAccountStore.on('load',function(){
            //                this.inputTaxAdjustmentAccount.setValue(Wtf.account.companyAccountPref.inputtaxadjustmentaccount);
            //            },this);
            //            this.inputTaxAdjustmentAccountStore.load();
            //
            //            //
            //            this.outputTaxAdjustmentAccountStore.on('load',function(){
            //                this.outputTaxAdjustmentAccount.setValue(Wtf.account.companyAccountPref.outputtaxadjustmentaccount);
            //            },this);
            //
            //            this.outputTaxAdjustmentAccountStore.load();


            this.gstAccountForBadDebtsStore.on('load',function(){

                this.gstBadDebtsReleifAccountStore.add(this.gstAccountForBadDebtsStore.getRange());
                this.gstBadDebtsRecoverAccountStore.add(this.gstAccountForBadDebtsStore.getRange());
                this.gstBadDebtsReleifPurchaseAccountStore.add(this.gstAccountForBadDebtsStore.getRange());    //For Purchase
                this.gstBadDebtsRecoverPurchaseAccountStore.add(this.gstAccountForBadDebtsStore.getRange());    //For Purchase
                this.gstBadDebtsSuspenseAccountStore.add(this.gstAccountForBadDebtsStore.getRange());
                this.inputTaxAdjustmentAccountStore.add(this.gstAccountForBadDebtsStore.getRange());
                this.outputTaxAdjustmentAccountStore.add(this.gstAccountForBadDebtsStore.getRange());
                this.freeGiftJEAccountStore.add(this.gstAccountForBadDebtsStore.getRange());

                this.gstAccountForBadDebts.setValue(Wtf.account.companyAccountPref.gstaccountforbaddebt);
                this.gstBadDebtsReleifAccount.setValue(Wtf.account.companyAccountPref.gstbaddebtreleifaccount);
                this.gstBadDebtsRecoverAccount.setValue(Wtf.account.companyAccountPref.gstbaddebtrecoveraccount);
                this.gstBadDebtsReleifPurchaseAccount.setValue(Wtf.account.companyAccountPref.gstbaddebtreleifpurchaseaccount);    //ERP-10400,For Purchase
                this.gstBadDebtsRecoverPurchaseAccount.setValue(Wtf.account.companyAccountPref.gstbaddebtrecoverpurchaseaccount);    //For Purchase
                this.gstBadDebtsSuspenseAccount.setValue(Wtf.account.companyAccountPref.gstbaddebtsuspenseaccount);
                this.inputTaxAdjustmentAccount.setValue(Wtf.account.companyAccountPref.inputtaxadjustmentaccount);
                this.outputTaxAdjustmentAccount.setValue(Wtf.account.companyAccountPref.outputtaxadjustmentaccount);
                this.freeGiftJEAccount.setValue(Wtf.account.companyAccountPref.freeGiftJEAccount);


            },this);
            this.taxStore.on('load',function(){
                var rowIndex = this.taxStore.find("prtaxname", Wtf.CapitalGoodsAcquired_TaxNameForMalaysia);
                if(Wtf.account.companyAccountPref.taxCgaMalaysian!=""){
                    this.taxCgaMalaysian.setValue(Wtf.account.companyAccountPref.taxCgaMalaysian);
                }else{
                    this.taxCgaMalaysian.setValue(this.taxStore.data.items[rowIndex].data.prtaxid);
                }
            },this);
            this.gstAccountForBadDebtsStore.load();
            this.taxStore.load();
        }
        this.salesAccStore.load();
        //        this.salesAccStore.on('load',this.setRevenueRecognitionAccounts(),this);
        this.salesAccStore.on('load',function(){
            if(Wtf.account.companyAccountPref.isDeferredRevenueRecognition||Wtf.account.companyAccountPref.recurringDeferredRevenueRecognition){
                //if(Wtf.account.companyAccountPref.salesAccount!=undefined&&Wtf.account.companyAccountPref.salesAccount!=""&&Wtf.account.companyAccountPref.salesRevenueRecognitionAccount!=undefined&&Wtf.account.companyAccountPref.salesRevenueRecognitionAccount!=""){
                if(Wtf.account.companyAccountPref.salesAccount!=undefined/*&&Wtf.account.companyAccountPref.salesAccount!=""*/&&Wtf.account.companyAccountPref.salesRevenueRecognitionAccount!=undefined/*&&Wtf.account.companyAccountPref.salesRevenueRecognitionAccount!=""*/){
                    this.salesAcc.enable();
                    this.salesRevenueRecognitionAccount.enable();
                    this.salesAcc.setValue(Wtf.account.companyAccountPref.salesAccount);
                    this.salesRevenueRecognitionAccount.setValue(Wtf.account.companyAccountPref.salesRevenueRecognitionAccount);
            }else{
                this.salesAcc.disable();
                this.salesRevenueRecognitionAccount.disable();
            }
            }else{
                this.salesAcc.disable();
                this.salesRevenueRecognitionAccount.disable();
            }
        },this);
         this.salesAccStoreForRevenueRecognitionMannualJE.load();
        this.salesAccStoreForRevenueRecognitionMannualJE.on('load',function(){
            if(Wtf.account.companyAccountPref.isDeferredRevenueRecognition||Wtf.account.companyAccountPref.recurringDeferredRevenueRecognition){
                if(Wtf.account.companyAccountPref.salesAccount!=undefined && this.setSalesFlag){
                    this.salesAcc.enable();
                    this.salesAcc.setValue(Wtf.account.companyAccountPref.salesAccount);
                }else if(this.setSalesFlag){
                    this.salesAcc.disable();
                }else{
                    this.salesAcc.enable();
                }
            }else if(this.setSalesFlag){
                this.salesAcc.disable();
            }
        },this);

        if (Wtf.account.companyAccountPref.isLMSIntegration) {
            this.liabilityAccStore.load();
            this.liabilityAccStore.on('load', function () {
                if (Wtf.account.companyAccountPref.recurringDeferredRevenueRecognition || Wtf.account.companyAccountPref.isLMSIntegration) {
                    if (Wtf.account.companyAccountPref.lmsliabilityAccount != undefined && Wtf.account.companyAccountPref.lmsliabilityAccount != "") {
                        this.liabilityAccountForLMS.enable();
                        this.liabilityAccountForLMS.setValue(Wtf.account.companyAccountPref.lmsliabilityAccount);
                    }
                }else {
                    this.liabilityAccountForLMS.disable();
                }
            }, this);
        }
        this.invoiceFormFieldLink = this.getFormFieldLink(Wtf.Acc_Invoice_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowCustomerInvoiceFormFields"));
        this.vendorInvoiceFormFieldLink = this.getFormFieldLink(Wtf.Acc_Vendor_Invoice_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowVendorInvoiceFormFields"));

        this.CSFormFieldLink = this.getFormFieldLink(Wtf.Acc_Cash_Sales_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowCashSalesFormFields"));
        this.CPFormFieldLink = this.getFormFieldLink(Wtf.Acc_Cash_Purchase_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowCashPurchaseFormFields"));

        this.POFormFieldLink = this.getFormFieldLink(Wtf.Acc_Purchase_Order_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowPurchaseOrderFormFields"));
        this.SOFormFieldLink = this.getFormFieldLink(Wtf.Acc_Sales_Order_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowSalesOrderFormFields"));
        this.VQFormFieldLink = this.getFormFieldLink(Wtf.Acc_Vendor_Quotation_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowVendorQuotationFormFields"));
        this.CQFormFieldLink = this.getFormFieldLink(Wtf.Acc_Customer_Quotation_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowCustomerQuotationFormFields"));
        this.PRFormFieldLink = this.getFormFieldLink(Wtf.Acc_Purchase_Return_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowPurchaseReturnFormFields"));
        this.SRFormFieldLink = this.getFormFieldLink(Wtf.Acc_Sales_Return_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowSalesReturnFormFields"));
        this.GRFormFieldLink = this.getFormFieldLink(Wtf.Acc_Goods_Receipt_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowGoodsReceiptFormFields"));
        this.DOFormFieldLink = this.getFormFieldLink(Wtf.Acc_Delivery_Order_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowDeliveryOrderFormFields"));
        this.PROFormFieldLink = this.getFormFieldLink(Wtf.Acc_PRO_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowProductFormFields"));
        this.StockReqFormFieldLink = this.getFormFieldLink(Wtf.Acc_Stock_Request_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowStockReqFormFields"));
        this.SalesContractFieldLink = this.getFormFieldLink(Wtf.Acc_Contract_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/ShowSalesContractFields"));
        this.LeaseContractFieldLink = this.getFormFieldLink(Wtf.Acc_Lease_Contract, WtfGlobal.getLocaleText("acc.field.Hide/ShowLeaseContractFields"));
        this.MakePaymentFieldLink = this.getFormFieldLink(Wtf.Acc_Make_Payment_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/MakePaymentFields"));
        this.ReceivePaymentFieldLink = this.getFormFieldLink(Wtf.Acc_Receive_Payment_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/showReceivePaymentFields"));
        this.CreditNoteFieldLink = this.getFormFieldLink(Wtf.Acc_Credit_Note_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/showCreditNoteFields"));
        this.DeditNoteFieldLink = this.getFormFieldLink(Wtf.Acc_Debit_Note_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/showDebitNoteFields"));
        /*
         *a link to hide/show Customer form fields
         **/
        this.CustomerLink = this.getFormFieldLink(Wtf.Acc_Customer_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/showCustomerFields"));
        /*
         *a link to hide/show Vendor form fields
         **/
        this.VendorLink = this.getFormFieldLink(Wtf.Acc_Vendor_ModuleId, WtfGlobal.getLocaleText("acc.field.Hide/showVendorFields"));
        /*
         *a link to hide/show Purchase Requisition Report/form fields
         **/
        this.PurchaseRequisitionLink = this.getFormFieldLink(Wtf.Acc_Purchase_Requisition_ModuleId, WtfGlobal.getLocaleText("Hide/Show Purchase Requisition Form/Reports Fields"));
          /*
         *a link to hide/show RFQ Report/form fields
         **/
         this.RFQLink = this.getFormFieldLink(Wtf.Acc_RFQ_ModuleId, WtfGlobal.getLocaleText("Hide/Show RFQ Form/Reports Fields"));
       /*
         *a link to hide/show pack  form fields
         **/

       this.PackLink = this.getFormFieldLink(Wtf.Acc_Packing_ModuleId, WtfGlobal.getLocaleText("acc.hideshow.pickform"));
       /*
         *a link to hide/show Ship Report/form fields
         **/
      this.ShipLink = this.getFormFieldLink(Wtf.Acc_Shipping_ModuleId , WtfGlobal.getLocaleText("acc.hideshow.shipform"));

      /*
       * a link to hide/show Lease Quotation Report/form fields
       */
      this.LeaseQuotationLink = this.getFormFieldLink(Wtf.Acc_Lease_Quotation, WtfGlobal.getLocaleText("acc.field.Hide/ShowLeaseQuotationFields"));

      /*
       * a link to hide/show Lease Order Report/form fields
       */
      this.LeaseOrderLink = this.getFormFieldLink(Wtf.Acc_Lease_Order, WtfGlobal.getLocaleText("acc.field.Hide/ShowLeaseOrderFields"));

      /*
       * a link to hide/show Lease Delivery Order Report/form fields
       */
      this.LeaseDeliveryOrderLink = this.getFormFieldLink(Wtf.Acc_Lease_DO, WtfGlobal.getLocaleText("acc.field.Hide/ShowLeaseDeliveryOrderFields"));

      /*
       * a link to hide/show Lease Invoice Report/form fields
       */
      this.LeaseInvoiceLink = this.getFormFieldLink(Wtf.LEASE_INVOICE_MODULEID, WtfGlobal.getLocaleText("acc.field.Hide/ShowLeaseInvoiceFields"));

      /*
       * a link to hide/show Lease Return Report/form fields
       */
      this.LeaseReturnLink = this.getFormFieldLink(Wtf.Acc_Lease_Return, WtfGlobal.getLocaleText("Hide/Show Lease Return Form/Reports Fields"));

      this.hideshowlinkArr = [this.invoiceFormFieldLink,this.vendorInvoiceFormFieldLink,this.CSFormFieldLink,this.CPFormFieldLink,this.POFormFieldLink,this.SOFormFieldLink,this.VQFormFieldLink,this.CQFormFieldLink,this.PRFormFieldLink,this.SRFormFieldLink,this.GRFormFieldLink,this.DOFormFieldLink,this.PROFormFieldLink,this.StockReqFormFieldLink,this.SalesContractFieldLink, this.LeaseContractFieldLink,this.MakePaymentFieldLink,this.ReceivePaymentFieldLink,this.CreditNoteFieldLink,this.DeditNoteFieldLink, this.CustomerLink, this.VendorLink, this.PurchaseRequisitionLink, this.RFQLink, this.LeaseQuotationLink, this.LeaseOrderLink, this.LeaseDeliveryOrderLink, this.LeaseInvoiceLink, this.LeaseReturnLink]

        if (Wtf.account.companyAccountPref.pickpackship) {
            /**
             * Add link only if Pick Pack Functionlity enable
             */
            this.hideshowlinkArr.push(this.PackLink);
            this.hideshowlinkArr.push(this.ShipLink);
        }
        this.downloadCurrencyExchangeratesLink=this.getdownloadCurrencyExchange(WtfGlobal.getLocaleText("acc.common.downloadexchangerates"));     //Download Exchange Rates
        this.fmonth.on('select',this.getBeginningYear,this);
        this.fdays.on('select',this.getBeginningYear,this);
        this.fmonth.on('select',this.getBookBeginningYear.createDelegate(this,[false]),this);
        this.fdays.on('select',this.getBookBeginningYear.createDelegate(this,[false]),this);
        this.customizeCurrencySymbolCodeLink=this.getCustomizeCurrencySymbolCode(WtfGlobal.getLocaleText("acc.common.customizecurrencysymbolcode"));
        this.limitedAccounts=this.getLimitedAccounts(WtfGlobal.getLocaleText("acc.common.LimitedAccountsLink"));
        this.JournalEntryRevaluationDimensionLink=this.getjournalEnteryDimensionforRevaluation(WtfGlobal.getLocaleText("acc.field.JournalEntryRevaluationDimension"));
        var btnArr=[];
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.accpref, Wtf.Perm.accpref.edit))
            btnArr.push(new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                handler:this.savePreferences,
                iconCls :getButtonIconCls(Wtf.etype.save),
                scope:this
            }));
        var ExportImport1=[];
        this.ExportSettings = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.accPref.ExportSettings"),
            tooltip:WtfGlobal.getLocaleText("acc.accPref.ExportSettings"),
            handler:this.ExportAccountPreferencesSettings.createDelegate(this,this.ExportSettingsOrSequence=["setting"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportSequenceFormat = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.accPref.ExportSequenceFormat"),
            tooltip:WtfGlobal.getLocaleText("acc.accPref.ExportSequenceFormat"),
            handler:this.ExportAccountPreferencesSettings.createDelegate(this,this.ExportSettingsOrSequence=["sequence"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportTransactionFields = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.accPref.ExportFormfields"),
            tooltip:WtfGlobal.getLocaleText("acc.accPref.ExportFormfields"),
            handler:this.ExportAccountPreferencesSettings.createDelegate(this,this.ExportSettingsOrSequence=["hideshow"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
         this.ExportxlsSettings = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.accPref.ExportXLSSettings"),
            tooltip:WtfGlobal.getLocaleText("acc.accPref.ExportXLSSettings"),
            handler:this.ExportAccountPreferencesSettings.createDelegate(this,[this.ExportSettingsOrSequence=["setting"],this.type=["xls"]]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportxlsSequenceFormat = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.accPref.ExportXLSSequenceFormat"),
            tooltip:WtfGlobal.getLocaleText("acc.accPref.ExportXLSSequenceFormat"),
            handler:this.ExportAccountPreferencesSettings.createDelegate(this,[this.ExportSettingsOrSequence=["sequence"],this.type=["xls"]]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportxlsTransactionFields = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.accPref.ExportXLSFormfields"),
            tooltip:WtfGlobal.getLocaleText("acc.accPref.ExportXLSFormfields"),
            handler:this.ExportAccountPreferencesSettings.createDelegate(this,[this.ExportSettingsOrSequence=["hideshow"],this.type=["xls"]]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        ExportImport1.push(this.ExportSettings);
        ExportImport1.push(this.ExportxlsSettings);
        ExportImport1.push(this.ExportSequenceFormat);
        ExportImport1.push(this.ExportxlsSequenceFormat);
        ExportImport1.push(this.ExportTransactionFields);
        ExportImport1.push(this.ExportxlsTransactionFields);
        this.ExportButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip:WtfGlobal.getLocaleText("acc.common.export"),
            scope:this,
            hidden:false,
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            menu:ExportImport1
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.accpref, Wtf.Perm.accpref.accprefexport)){
            btnArr.push(this.ExportButton);
        }

        var ExportImport2=[];

        this.ImportSettings = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.accPref.ImportSettings"),
            tooltip:WtfGlobal.getLocaleText("Master Item Menu"),
            handler:callSettingsImportWin.createDelegate(this,['download_sample_Accounts Preferences Settings.csv', 'sample_Accounts_Preferences_Settings',WtfGlobal.getLocaleText("acc.accPref.ImportSettings"),"ACCCompanyPrefCMN/ImportAccountPrefSettings.do?",true,'Accounts Preferences Settings']),  //Sample Filename must be without Extention
            scope:this,
            iconCls: 'pwnd importcsv'
        });
        this.ImportSequenceFormat = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.accPref.ImportSequenceFormat"),
            tooltip:WtfGlobal.getLocaleText("acc.accPref.ImportSequenceFormat"),
            handler:callSettingsImportWin.createDelegate(this,['download_sample_Sequence_Format.csv','sample_Sequence_Format',WtfGlobal.getLocaleText("acc.accPref.ImportSequenceFormats"),"ACCCompanyPrefCMN/ImportSequenceFormat.do?",false,'Sequence Format']),   //Sample Filename must be without Extention
            scope:this,
            iconCls: 'pwnd importcsv'
        });
        this.ImportTransactionFields = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.accPref.ImportFields"),
            tooltip:WtfGlobal.getLocaleText("acc.accPref.ImportFields"),
            handler:callSettingsImportWin.createDelegate(this,['download_sample_Hide_Show Transaction Forms Fields.csv', 'sample_Hide_Show_Transaction_Forms_Fields',WtfGlobal.getLocaleText("acc.accPref.ImporthideshowTransactional"),"ACCCompanyPrefCMN/ImporthideshowTransactionalFields.do?",false,'Hide Show Transaction Forms Fields']),  //Sample Filename must be without Extention
            scope:this,
            iconCls: 'pwnd importcsv'
        });
        ExportImport2.push(this.ImportSettings);
        ExportImport2.push(this.ImportSequenceFormat);
        ExportImport2.push(this.ImportTransactionFields);
        this.ImportButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.import"),
            tooltip:WtfGlobal.getLocaleText("acc.common.import"),
            scope:this,
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            hidden:false,
            menu:ExportImport2
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.accpref, Wtf.Perm.accpref.accprefimport)) {
            btnArr.push(this.ImportButton);
        }
        btnArr.push("->");
        //When Mrp tab is also there at that time help button not working properly , so removed it.
//        btnArr.push(getHelpButton(this,this.helpmodeid));
        this.badDebtPeriodTypeStore = new Wtf.data.SimpleStore({
            fields: [{
                name: 'id'
            }, {
                name: 'type'
            }],
            data: [[0,'Months'],
            [1,'Days']]
        });
        this.gstSubmissionPeriodStore = new Wtf.data.SimpleStore({
            fields: [{
                name: 'id'
            }, {
                name: 'type'
            }],
            data: [[0,'Monthly'],
            [1,'Quarterly']]
        });
        this.gafVersionStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'type'}],
            data: [['1.0', '1.0'], ['2.0', '2.0']]
        });
        var re = new Wtf.data.Record({
            id: "-1",
            name: "None"
        });
        this.masterItemGroupRec = Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        }
        ]);
        this.masterItemTempStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 59
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.masterItemGroupRec)
        });
        if(Wtf.account.companyAccountPref.countryid == '137'){
            this.masterItemTempStore.load();
        }
        this.masterItemTempStore.on('load',function(){
            this.masterItemTempStore.insert(0, re);
            if(Wtf.account.companyAccountPref.industryCode!=undefined){
                this.industryCodeCmb.setValue(Wtf.account.companyAccountPref.industryCode);
            }else{
                this.industryCodeCmb.setValue("-1");
            }

        },this);
        // Store for 'Negative value in' combo
        this.negativeValueInStore = new Wtf.data.SimpleStore({
            fields: [{
                name: 'id'
            }, {
                name: 'value'
            }],
            data: [[1,'Minus Symbol'],
            [2,'Brackets']]
        });
        this.addGrid();
        var typeofdealerRec=
        [
//        ['1','Composition'],
//        ['2','Composition to Regular'],
        ['3','Regular']
        ]
        this.typeofdealerStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'dealerType'
            },{
                name:'dealerName'
            }],
            data:typeofdealerRec
        });
        /* ---------------------- TDS Applicable For INDIA Company ----------------------- */
        if (Wtf.isTDSApplicable) {
            this.stateStore.load();
        }

        this.deductorTypeStore = new Wtf.data.SimpleStore({
            fields: [{
                name: 'id'
            }, {
                name: 'type'
            }],
            data:
           [[0,WtfGlobal.getLocaleText("acc.companypref.CG")],                //Central Government
            [1,WtfGlobal.getLocaleText("acc.companypref.SG")],                //State Government
            [2,WtfGlobal.getLocaleText("acc.companypref.SB(CG)")],            //Statutory Body (Central Govt)
            [3,WtfGlobal.getLocaleText("acc.companypref.SB(SG)")],            //Statutory Body (State Govt)
            [4,WtfGlobal.getLocaleText("acc.companypref.AB(CG)")],            //Autonomous Body (Central Govt)
            [5,WtfGlobal.getLocaleText("acc.companypref.AB(SG)")],            //Autonomous Body (State Govt)
            [6,WtfGlobal.getLocaleText("acc.companypref.LA(CG)")],            //Local Authority (Central Govt)
            [7,WtfGlobal.getLocaleText("acc.companypref.LA(SG)")],            //Local Authority (State Govt)
            [8,WtfGlobal.getLocaleText("acc.companypref.company")],            //Local Authority (State Govt)
            [9,WtfGlobal.getLocaleText("acc.companypref.B/DOC")],             //Branch/Division of Company
            [10,WtfGlobal.getLocaleText("acc.companypref.AOP")],               //Association of Person (AOP)
            [11,WtfGlobal.getLocaleText("acc.companypref.AOP.T")],            //Association of Person (Trust)
            [12,WtfGlobal.getLocaleText("acc.companypref.AJP")],              //Artificial Juridical Person
            [13,WtfGlobal.getLocaleText("acc.companypref.IndividualOrHUF")],  //Individual / HUF
            [14,WtfGlobal.getLocaleText("acc.companypref.BOI")],              //Body of Individuals
            [15,WtfGlobal.getLocaleText("acc.companypref.Firm")]              //Firm
            ]// No Need to provide list in master configuration - ERP-27737
        });

        this.deductorTypeCompanyLevel=new Wtf.form.ComboBox({
            fieldLabel:"Deductor Type",
            store:this.deductorTypeStore,
            name:'deductortype',
            id:'deductorType'+this.id,
            width:183,
            listWidth:183,
            hiddenName:'deductortypehidden',
            valueField:'id',
            mode:'local',
            displayField:'type',
            forceSelection: true,
            triggerAction: 'all',
            value:Wtf.deductortype!=""?Wtf.deductortype:0,
            selectOnFocus:true
        });
        this.AssessmentYear = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='<b>Pattern:</b> YYYY-YYYY'>Assessment Year</span>",
            name: 'AssessmentYear',
            hidden: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            hideLabel: (Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable),
            maxLength: 9,
            width:200,
            invalidText: WtfGlobal.getLocaleText("acc.companyPreferences.assessmentYearFormat"),
            regex:/[0-9]{4}-[0-9]{4}/,
            value:Wtf.assessmentYear
        });

        this.TDS= new Wtf.form.TextField({
            fieldLabel: "Income Tax Circle/Ward(TDS)",
            name:"tdsincometaxcircle",
            width:200,
            maxLength:11,
            value:Wtf.TDSincometaxcircle
        });
        this.PANNo = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.CompanyPANNO"), // "Customer PAN"
            value: Wtf.CompanyPANNumber,
            width: 200,
            maxLength: 10,
            invalidText: 'Alphabets and numbers only',
            vtype: "alphanum",
            regex: /[A-Z]{5}\d{4}[A-Z]/, //[a-z]{3}[cphfatblj][a-z]\d{4}[a-z]/i,
            regexText: 'Invalid PAN eg."AAAAA1234A"',
            anchor: '89%'//,
                    //hidden : true,
                    //hideLabel : true
        });
        this.headofficetanno =new Wtf.form.TextField({
            fieldLabel: "Head Office TAN",// "Customer TAN"
            name:"headofficetanno",
            value:Wtf.headofficetanno,
            width:200,
            maxLength:10,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum"
        });
        this.responsiblePerson= new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='Name of the Person responsible for Deduction of Tax'>Responsible Person</span>",
            name:"tdsrespperson",
            value:Wtf.TDSrespperson,
            width:200
        });

        this.fathersName= new Wtf.form.TextField({
            fieldLabel: "Son / Daughter of(Father's Name)",
            name:"tdsresppersonfathersNamse",
            value:Wtf.TDSresppersonfathersname,
            width:200
        });
        this.designation= new Wtf.form.TextField({
            fieldLabel: "Designation",
            name:"tdsresppersonDesignation",
            value:Wtf.TDSresppersondesignation,
            width:200
        });
        this.isAddressChanged = new Wtf.form.Checkbox({//Send mail notfication when RFQ Generation
            name: 'isAddressChanged',
            id: this.id + 'isAddressChanged',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.tds.hasCompChangeAdd"),//"Has company changed address since last return ?",
            checked: Wtf.isAddressChanged,
            scope: this,
            labelStyle: 'width: 340px;',
            cls: 'checkboxtopPosition',
            autoWidth: true
        });
        this.resposiblePersonAddress = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.add"),//"Address",
            name: "resposiblePersonAddress",
            value: Wtf.resposiblePersonAddress,
            width: 200,
            maxLength: 1000
        });
        this.resposiblePersonstate = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.State"),
            store: this.stateStore,
            width: 180,
            name: 'resposiblePersonstate',
            listWidth: 200,
            labelWidth: 80,
            displayField: 'name',
            valueField: 'id',
            value: Wtf.resposiblePersonstate
        });

//        this.resposiblePersonstate.setValue(Wtf.resposiblePersonstate);
        this.resposiblePersonTeleNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.teleNumber"),//"Telephone Number",
            name: 'resposiblePersonTeleNumber',
            id: this.id + 'resposiblePersonTeleNumber',
            maxLength: 15,
            width:200,
            value:Wtf.resposiblePersonTeleNumber
        });
        this.resposiblePersonMobNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.vendor.tds.MobNumber"),//"Mobile Number ",
            name: 'resposiblePersonMobNumber',
            id: this.id + 'resposiblePersonMobNumber',
            maxLength: 15,
            width:200,
            value:Wtf.resposiblePersonMobNumber
        });
        this.resposiblePersonEmail = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Email"),
            name: 'resposiblePersonEmail',
            id: this.id + 'resposiblePersonEmail',
            maxLength: 50,
            width:200,
            validator: WtfGlobal.validateEmail,
            value: Wtf.resposiblePersonEmail
        });
        this.resposiblePersonPostal = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name: "resposiblePersonPostal",
            id: this.id + 'resposiblePersonPostal',
            maxLength: 6,
            width:200,
            allowNegative: false,
            value: Wtf.resposiblePersonPostal
        });
        this.resposiblePersonPAN = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.setupwizard.pan"),//"PAN",
            name: 'resposiblePersonPAN',
            id: this.id + 'resposiblePersonPAN',
            maxLength:10,
            width:200,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum",
            regex:/[A-Z]{5}\d{4}[A-Z]/, //[a-z]{3}[cphfatblj][a-z]\d{4}[a-z]/i,
            regexText:'Invalid PAN eg."AAAAA1234A"',
            value:Wtf.resposiblePersonPAN
        });
        this.resposiblePersonHasAddressChanged = new Wtf.form.Checkbox({
            name: 'resposiblePersonHasAddressChanged',
            id: this.id + 'resposiblePersonHasAddressChanged',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.tds.hasPersonChangeAdd"),//"Has person changed address since last return ?",
            checked: Wtf.resposiblePersonHasAddressChanged,
            scope: this,
            labelStyle: 'width: 340px;',
            cls: 'checkboxtopPosition',
            autoWidth: true
        });
        this.particularsResposiblePerson = new Wtf.form.FieldSet({
            id: 'particularsResposiblePerson',
            xtype: 'particularsResposiblePerson',
            title: WtfGlobal.getLocaleText("acc.vendor.tds.responsibleForTaxDeduction"),//"Particulars of the person responsible for deduction of tax",
            hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            autoWidth: true,
            scope:this,
            items: [this.resposiblePersonHasAddressChanged,this.responsiblePerson,this.fathersName,this.designation,this.resposiblePersonAddress,this.resposiblePersonstate,this.resposiblePersonTeleNumber,this.resposiblePersonMobNumber,this.resposiblePersonEmail,this.resposiblePersonPostal,this.resposiblePersonPAN]
        });

        this.TANNo =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Companytan"),// "Customer TAN"
                            value: Wtf.CompanyTANNumber,
                            width:200,
                            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            maxLength:10,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum"
                        });
        this.TDSInterestRate = new Wtf.form.NumberField({
            id: 'tdsinterestrate' + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.CompanyPreferences.TDSInterestRate"),
            value: Wtf.CompanyTDSInterestRate,
            width: 200,
            maxValue:100,
            hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA
        });

        this.purchaseAccRec = Wtf.data.Record.create ([
        {
            name: 'accid'
        },

        {
            name: 'accname'
        },

        {
            name: 'acccode'
        },

        {
            name: 'hasAccess'
        }
        ]);
        this.purchaseAccStore=new Wtf.data.Store({
            //           url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.purchaseAccRec)
        });
        this.AccsRecord = Wtf.data.Record.create ([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'acccode'},
            {name: 'nature'},
            {name: 'hasAccess'}

        ]);
        this.AccsStore=new Wtf.data.Store({
            //           url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
//                ignoreCashAccounts:true,
//                ignoreBankAccounts:true,
//                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nondeleted:true,
                controlAccounts:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.AccsRecord)
        });
        this.AccsStore.on('load', function () {
            this.AccsStore.insert(0,new this.AccsRecord({
                accid: '',
                accname: 'None',
                nature: '',
                hasAccess: '',
                acccode:''
            }))
        },this);
        this.purchaseAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.purchaseAccount")+"'>"+ WtfGlobal.getLocaleText("acc.product.purchaseAccount")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'65%',
            name:'purchaseaccountidcompany',
            id:'purchaseaccountidcompany'+this.id,
            hiddenName:'purchaseaccountidcompany',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true,
            //addNewFn:this.isInventory.createDelegate(this),
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.purchaseReturnAcc.getValue())
                            this.purchaseReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
        });

        this.purchaseReturnAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.purchaseReturnAccount")+"'>"+ WtfGlobal.getLocaleText("acc.product.purchaseReturnAccount")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseReturnAcc"),//'Purchase Return Account*',
            store:this.purchaseAccStore,
            anchor:'65%',
            name:'purchaseretaccountidcompany',
            id:'purchaseretaccountidcompany'+this.id,
            hiddenName:'purchaseretaccountidcompany',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true,
            //addNewFn:this.isInventory.createDelegate(this),
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.purchaseReturnAcc.getValue())
                            this.purchaseReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
        });
        this.interStatePurAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.interStatePurchaseAcc")+"'>"+ WtfGlobal.getLocaleText("acc.product.interStatePurchaseAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'65%',
            name:'interStatePurAccID',
            id:'interStatePurAccID'+this.id,
            hiddenName:'interStatePurAccID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true,
             listeners:{
                select:{
                    fn:function(c){
                        if(!this.interStatePurReturnAcc.getValue())
                            this.interStatePurReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
        });

        this.interStatePurReturnAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.interStatePurchaseReturnAcc")+"'>"+ WtfGlobal.getLocaleText("acc.product.interStatePurchaseReturnAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'65%',
            name:'interStatePurReturnAccID',
            id:'interStatePurReturnAccID'+this.id,
            hiddenName:'interStatePurReturnAccID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });

        this.interStatePurAccCform=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.interStatePurchaseAccCForm")+"'>"+ WtfGlobal.getLocaleText("acc.product.interStatePurchaseAccCForm")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'65%',
            name:'interStatePurAccCformID',
            id:'interStatePurAccCformID'+this.id,
            hiddenName:'interStatePurAccCformID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,    // k
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true,
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.interStatePurReturnAccCform.getValue())
                            this.interStatePurReturnAccCform.setValue(c.getValue());
                    },
                    scope:this
                }
            }
        });

        this.interStatePurReturnAccCform=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.interStatePurchaseReturnAccCForm")+"'>"+ WtfGlobal.getLocaleText("acc.product.interStatePurchaseReturnAccCForm")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.purchaseAccStore,
            anchor:'65%',
            name:'interStatePurReturnAccCformID',
            id:'interStatePurAccReturnCformID'+this.id,
            hiddenName:'interStatePurAccReturnCformID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,    // k
            hidden: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });

        this.defaultPurchaseAccountSettings=new Wtf.form.FieldSet({
            id: 'defaultPurchaseAccountSettings',
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText("acc.companypreferences.productPurchaseAccDefaultSettings"),
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            items:[this.purchaseAcc,this.purchaseReturnAcc,this.interStatePurAcc,this.interStatePurReturnAcc,this.interStatePurAccCform,this.interStatePurReturnAccCform]
        });
        this.salesAccRec = new Wtf.data.Record.create ([
        {
            name: 'accid'
        },

        {
            name: 'accname'
        },

        {
            name: 'acccode'
        },

        {
            name: 'hasAccess'
        }
        ]);
        this.salesAccStoreProduct=new Wtf.data.Store({
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.salesAccRec)
        });
        this.salesAccStoreProduct.load();
        this.salesAccProduct=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.salesAccount")+"'>"+ WtfGlobal.getLocaleText("acc.product.salesAccount")+"</span>",//WtfGlobal.getLocaleText("acc.product.salesAcc"),//'Sales Account*',
            store:this.salesAccStoreProduct,
            name:'salesaccountidcompany',
            id:'salesaccountidcompany'+this.id,
            anchor:'70%',
            hiddenName:'salesaccountidcompany',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true,
            //  addNewFn: this.addAccount.createDelegate(this,[Wtf.salesAccStore,false,false,true],true),
            listeners:{
                select:{
                    fn:function(c){
                        if(!this.salesReturnAcc.getValue())
                            this.salesReturnAcc.setValue(c.getValue());
                    },
                    scope:this
                }
            }
        });

        this.salesReturnAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.salesReturnAccount")+"'>"+ WtfGlobal.getLocaleText("acc.product.salesReturnAccount")+"</span>",//WtfGlobal.getLocaleText("acc.product.salesReturnAcc"),//'Sales Return Account*',
            store:this.salesAccStoreProduct,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            name:'salesretaccountidcompany',
            id:'salesretaccountidcompany'+this.id,
            anchor:'70%',
            hiddenName:'salesretaccountidcompany',
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true//,
        // addNewFn: this.addAccount.createDelegate(this,[Wtf.salesAccStore,true,false],true)
        });

        this.interStateSalesAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.interStateSalesAcc")+"'>"+ WtfGlobal.getLocaleText("acc.product.interStateSalesAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.salesAccStoreProduct,
            anchor:'70%',
            name:'interStateSalesAccID',
            id:'interStateSalesAccID'+this.id,
            hiddenName:'interStateSalesAccID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });

        this.interStateSalesReturnAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.interStateSalesReturnAcc")+"'>"+ WtfGlobal.getLocaleText("acc.product.interStateSalesReturnAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.salesAccStoreProduct,
            anchor:'70%',
            name:'interStateSalesReturnAccID',
            id:'interStateSalesReturnAccID'+this.id,
            hiddenName:'interStateSalesReturnAccID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true
        });

        this.interStateSalesAccCform=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.interStateSalesAccCForm")+"'>"+ WtfGlobal.getLocaleText("acc.product.interStateSalesAccCForm")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.salesAccStoreProduct,
            anchor:'70%',
            name:'interStateSalesAccCformID',
            id:'interStateSalesAccCformID'+this.id,
            hiddenName:'interStateSalesAccCformID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            value:Wtf.interstatesalesacccformid,
            allowBlank: true
        });

        this.interStateSalesAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.interStateSalesAcc")+"'>"+ WtfGlobal.getLocaleText("acc.product.interStateSalesAcc")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.salesAccStoreProduct,
            anchor:'70%',
            name:'interStateSalesAccID',
            id:'interStateSalesAccID'+this.id,
            hiddenName:'interStateSalesAccID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            value:Wtf.interstatesalesaccid,
            allowBlank: true
        });

        this.interStateSalesReturnAccCform=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.interStateSalesReturnAccCForm")+"'>"+ WtfGlobal.getLocaleText("acc.product.interStateSalesReturnAccCForm")+"</span>",//WtfGlobal.getLocaleText("acc.product.purchaseAcc"),//'Purchase Account*',
            store:this.salesAccStoreProduct,
            anchor:'70%',
            name:'interStateSalesReturnAccCformID',
            id:'interStateSalesAccReturnCformID'+this.id,
            hiddenName:'interStateSalesAccReturnCformID',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            typeAhead: true,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            value:Wtf.interstatesalesreturnacccformid,
            allowBlank: true
        });

        this.defaultSalesAccountSettings=new Wtf.form.FieldSet({
            id: 'defaultSalesAccountSettings',
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText("acc.companypreferences.productSalesAccDefaultSettings"),
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            items:[this.salesAccProduct,this.salesReturnAcc,this.interStateSalesAcc,this.interStateSalesReturnAcc,this.interStateSalesAccCform,this.interStateSalesReturnAccCform]
        });
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            this.purchaseAccStore.on('load',function(){

                this.purchaseReturnAcc.setValue(Wtf.purchaseretaccountidcompany);
                this.purchaseAcc.setValue(Wtf.purchaseaccountidcompany);
                this.interStatePurAcc.setValue(Wtf.interstatepuraccid);
                this.interStatePurReturnAcc.setValue(Wtf.interstatepurreturnaccid);    //ERP-10400,For Purchase
                this.interStatePurAccCform.setValue(Wtf.interstatepuracccformid);    //For Purchase
                this.interStatePurReturnAccCform.setValue(Wtf.interstatepuraccreturncformid);
            },this);
            this.purchaseAccStore.load({
                params:{
                    group:[6]
                    }
                });
        this.salesAccStoreProduct.on('load',function(){

            this.salesAccProduct.setValue(Wtf.salesaccountidcompany);
            this.salesReturnAcc.setValue(Wtf.salesretaccountidcompany);
            this.interStateSalesAcc.setValue(Wtf.interstatesalesaccid);
            this.interStateSalesReturnAcc.setValue(Wtf.interstatesalesreturnaccid);    //ERP-10400,For Purchase
            this.interStateSalesAccCform.setValue(Wtf.interstatesalesacccformid);    //For Purchase
            this.interStateSalesReturnAccCform.setValue(Wtf.interstatesalesaccreturncformid);
        },this);
        this.salesAccStoreProduct.load({
            params:{
                group:[6]
                }
            });
}
        this.tdsFieldset=new Wtf.form.FieldSet({
            id: 'fieldsetNuovaTecnologia',
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText("acc.companypreferences.isTDSapplicable"),
            checkboxToggle: true,
            collapsed: !Wtf.isTDSApplicable,// mantiene di default chiuso il fildset
            checkboxName: 'isTDSapplicable',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            items:[this.PANNo,this.headofficetanno,this.TDS,this.TANNo,this.TDSInterestRate,this.deductorTypeCompanyLevel,this.particularsResposiblePerson,this.AssessmentYear,this.isAddressChanged]
        });
        this.tdsFieldset.on('collapse',function(){
            if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
                this.IsLineLevelTaxUsedInTransaction("",'TDS',this.tdsFieldset);
            }

        },this);
        this.stateStore.on('load',function(){
            this.resposiblePersonstate.setValue(Wtf.resposiblePersonstate)
        },this);
            //Excise Accounts
    this.excisePayableAccRec = new Wtf.data.Record.create ([
        {
            name: 'accid'
        },

        {
            name: 'accname'
        },

        {
            name: 'acccode'
        },

        {
            name: 'hasAccess'
        }
        ]);
        this.excisePayableAccStore=new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
//                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.excisePayableAccRec)
        });

        this.STPayableAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CompanySTPayableAcc"),
            store:this.excisePayableAccStore,
            name:'STPayableAcc',
            id:'STPayableAcc'+this.id,
            hiddenName:'STPayableAcc',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:true,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
//            allowBlank: Wtf.isSTApplicable?false:true,
            width:185
        });
        this.STAdvancePaymentAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CompanySTAdvancePaymentAcc"),
            store:this.excisePayableAccStore,
            name:'STAdvancePaymentaccount',
            id:'STAdvancePaymentaccount'+this.id,
            hiddenName:'STAdvancePaymentaccount',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            width:185
        });

        this.commissionerateCode =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.commisionercode"),
            name:"commissioneratecode",
            value:Wtf.commissioneratecode,
            width:200,
//            maxLength:2,
            invalidText :'Alphabets and numbers only'
//            vtype : "alphanum",
//            allowBlank:Wtf.isSTApplicable?false:true
        });
        this.commissionerateName =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.commisionername"),
            name:"commissioneratename",
            value:Wtf.commissioneratename,
            width:200,
            maxLength:100
//            allowBlank:Wtf.isSTApplicable?false:true
        });
        this.serviceTaxRegNo =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.serviceTaxRegno"),
            name:"servicetaxregno",
            value: Wtf.CompanyServiceTaxRegNumber,
            width:200,
            vtype : "alphanum",
            maxLength:15
//            allowBlank:Wtf.isSTApplicable?false:true
        });
        this.divisionCode =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.divisioncode"),
            name:"divisioncode",
            value:Wtf.divisioncode,
            width:200
//            allowBlank:Wtf.isSTApplicable?false:true
        });
        this.rangeCode =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.rangecode"),
            name:"rangecode",
            value:Wtf.rangecode,
            width:200
//            allowBlank:Wtf.isSTApplicable?false:true
        });

       this.GTAKKCPaybleAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.compref.india.rcm.KKC.payable"),
            store:this.excisePayableAccStore,
            name:'GTAKKCPaybleAccount',
            id:'GTAKKCPaybleAccount'+this.id,
            hiddenName:'GTAKKCPaybleAccount',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            width:185,
            value:Wtf.GTAKKCPaybleAccount
        });
       this.GTASBCPaybleAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.compref.india.rcm.SBC.payable"),
            store:this.excisePayableAccStore,
            name:'GTASBCPaybleAccount',
            id:'GTASBCPaybleAccount'+this.id,
            hiddenName:'GTASBCPaybleAccount',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            width:185,
            value:Wtf.GTASBCPaybleAccount
        });
        this.cstFieldset=new Wtf.form.FieldSet({
            id: 'cstApplicableFieldSet',
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText("acc.field.india.isstavailable"),
            checkboxToggle: true,
            collapsed: !Wtf.isSTApplicable,
            checkboxName: 'isSTApplicable',
            hidden: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,  // k
            items:[this.STPayableAcc,this.GTAKKCPaybleAccount,this.GTASBCPaybleAccount,this.STAdvancePaymentAccount,this.commissionerateName,this.commissionerateCode,this.divisionCode,this.rangeCode,this.serviceTaxRegNo]
        });
        this.cstFieldset.on('collapse',function(){
            if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
                this.IsLineLevelTaxUsedInTransaction("4",'Service Tax',this.cstFieldset);
            }
        },this);
//        this.cstFieldset.on('expand',function(){
//            this.rangeCode.allowBlank=false;
//            this.divisionCode.allowBlank=false;
//            this.commissionerateName.allowBlank=false;
//            this.serviceTaxRegNo.allowBlank=false;
//            this.commissionerateCode.allowBlank=false;
//            this.STPayableAcc.allowBlank=false;
//        },this);

//        this.cstFieldset.on('beforeexpand',function(){
//            if(!this.exciseFieldset.collapsed){
//                this.exciseFieldset.collapse(true);
//                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), "Excise Duty check is ON. Either Excise Duty or Service Tax will be applicable.</br></br>Are you sure want apply Service Tax?",function(btn){
//                if(btn=="yes") {
//                    this.exciseFieldset.collapse(true);
//                }else{
//                    this.cstFieldset.collapse(true);
//                    this.exciseFieldset.expand();
//                }
//            }, this)
//            }
//        },this);

        this.exciseCommissionerateCode =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.commisionercode")+"*",
            name:"excisecommissioneratecode",
            value:Wtf.excisecommissioneratecode,
            width:200,
//            maxLength:2,
            allowBlank:Wtf.isExciseApplicable?false:true
//            vtype : "alphanum"
        });
        this.exciseCommissionerateName =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.commisionername")+"*",
            name:"excisecommissioneratename",
            value:Wtf.excisecommissioneratename,
            width:200,
            maxLength:100,
            allowBlank:Wtf.isExciseApplicable?false:true
        });
        this.UnitName =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.exciseUnitName"),//"Excise Unit Name",
            name:"unitname",
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            value:Wtf.unitname,
            width:200,
            maxLength:100
//            allowBlank:Wtf.isExciseApplicable?false:true
        });
        this.registrationTypeCombo=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.india.registrationtype"),
            name:'registrationType',
            id:'registrationType'+this.id,
//            width:200,
            anchor:'89%',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            value:Wtf.account.companyAccountPref.registrationType,
            readOnly:true
        });
        this.manufacturerTypeCombo=new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.india.typeofmanufacturer"),//'Type of Manufacturer',
            store:Wtf.manufactureTypeStore,
            name:'manufacturerType',
            id:'manufacturerType'+this.id,
            width:183,
            listWidth:183,
            hiddenName:'manufacturerType',
            valueField:'id',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            mode:'local',
            displayField:'name',
            forceSelection: true,
            triggerAction: 'all',
            value:Wtf.manufacturerType,
            selectOnFocus:true
        });
        this.ECCNo =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Companyecc") ,// "Customer ECC"
            value: Wtf.CompanyECCNumber,
            width:200,
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            maxLength:15,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum"
        });
        this.exciseDivisionCode =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.divisioncode")+"*",
            name:"excisedivisioncode",
            value:Wtf.excisedivisioncode,
            width:200,
            allowBlank:Wtf.isExciseApplicable?false:true
        });
        this.exciseRangeCode =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.india.rangecode")+"*",
            name:"exciserangecode",
            value:Wtf.exciserangecode,
            width:200,
            allowBlank:Wtf.isExciseApplicable?false:true
        });
        if(Wtf.excisedivisioncode!="" && Wtf.excisedivisioncode!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            this.exciseDivisionCode.setValue(Wtf.excisedivisioncode);
        }
        if(Wtf.exciserangecode!="" && Wtf.exciserangecode!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            this.exciseRangeCode.setValue(Wtf.exciserangecode);
        }
        if(Wtf.divisioncode!="" && Wtf.divisioncode!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            this.divisionCode.setValue(Wtf.divisioncode);
        }
        if(Wtf.rangecode!="" && Wtf.rangecode!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            this.rangeCode.setValue(Wtf.rangecode);
        }

    //VAT Accounts
    this.vatPayableAccRec = new Wtf.data.Record.create ([
        {
            name: 'accid'
        },

        {
            name: 'accname'
        },

        {
            name: 'acccode'
        },

        {
            name: 'hasAccess'
        }
        ]);
        this.vatPayableAccStore=new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.vatPayableAccRec)
        });
        this.vatPayableAccStore.load();
        this.vatPayableAccStore.on("load",function(){
            if(Wtf.vatPayableAcc!="" && Wtf.vatPayableAcc!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.vatPayableAcc.setValue(Wtf.vatPayableAcc);
            }
        },this);
        this.vatPayableAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CompanyVATPayableAcc"),
            store:this.vatPayableAccStore,
            name:'vatPayableAcc',
            id:'vatPayableAcc'+this.id,
            hiddenName:'vatPayableAcc',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true,
            width:185
        });

    this.vatInCreditAvailAccRec = new Wtf.data.Record.create ([
        {
            name: 'accid'
        },

        {
            name: 'accname'
        },

        {
            name: 'acccode'
        },

        {
            name: 'hasAccess'
        }
        ]);
        this.vatInCreditAvailAccStore=new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.vatInCreditAvailAccRec)
        });
        this.vatInCreditAvailAccStore.load();
        this.vatInCreditAvailAccStore.on("load",function(){
            if(Wtf.vatInCreditAvailAcc!="" && Wtf.vatInCreditAvailAcc!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.vatInCreditAvailAcc.setValue(Wtf.vatInCreditAvailAcc);
            }
        },this);
        this.vatInCreditAvailAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CompanyInputCreditAvailableAcc"),
            store:this.vatInCreditAvailAccStore,
            name:'vatInCreditAvailAcc',
            id:'vatInCreditAvailAcc'+this.id,
            hiddenName:'vatInCreditAvailAcc',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true,
            width:185
        });

    //CST Accounts
    this.CSTPayableAccRec = new Wtf.data.Record.create ([
        {
            name: 'accid'
        },

        {
            name: 'accname'
        },

        {
            name: 'acccode'
        },

        {
            name: 'hasAccess'
        }
        ]);
        this.CSTPayableAccStore=new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.CSTPayableAccRec)
        });
        this.CSTPayableAccStore.load();
        this.CSTPayableAccStore.on("load",function(){
            if(Wtf.CSTPayableAcc!="" && Wtf.CSTPayableAcc!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.CSTPayableAcc.setValue(Wtf.CSTPayableAcc);
            }
        },this);
        this.CSTPayableAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CompanyCSTPayableAcc"),
            store:this.CSTPayableAccStore,
            name:'CSTPayableAcc',
            id:'CSTPayableAcc'+this.id,
            hiddenName:'CSTPayableAcc',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: true,
            width:185
        });

        // Payment Method
        this.pmtRec = new Wtf.data.Record.create([
        {
            name: 'methodid'
        },

        {
            name: 'methodname'
        },

        {
            name: 'accountid'
        },

        {
            name: 'acccurrency'
        },

        {
            name: 'accountname'
        },

        {
            name: 'isIBGBankAccount',
            type: 'boolean'
        },

        {
            name: 'isdefault'
        },

        {
            name: 'detailtype',
            type: 'int'
        },

        {
            name: 'acccustminbudget'
        },

        {
            name: 'autopopulate'
        },

        {
            name:'bankType'
        }
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
        this.bankStore=new Wtf.data.Store({
                url:"ACCAccountCMN/getAccountsForCombo.do",
                baseParams:{
                    mode:2,
                    ignoreGLAccounts:true,
                    ignoreCashAccounts:true,
                    ignoreGSTAccounts:true,
                    ignorecustomers:true,
                    ignorevendors:true,
                    nondeleted:true
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.salesAccRec)
            });
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            this.bankStore.load();
            this.bankStore.on("load",function( store, records){
                if(!Wtf.bankid){
                    if(records && records[0] && records[0].data && records[0].data.accid && this.bankCombo){
                        this.bankCombo.setValue(records[0].data.accid);
                    }
                } else{
                    this.bankCombo.setValue(Wtf.bankid);
                }
            },this);
        }

        this.pmtStore.load();
        this.pmtStore.on("load",function(){
            if(Wtf.pmtMethod!="" && Wtf.pmtMethod!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.pmtMethod.setValue(Wtf.pmtMethod);
            }
        },this);

        this.pmtMethod = new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.payMethod"),// 'Payment Method',
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            name: "pmtmethod",
            hiddenName:'pmtmethod',
            store: this.pmtStore,
            valueField: 'methodid',
            displayField: 'methodname',
            allowBlank: true,
            disabled:   this.readOnly,
            width:185,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true//,
        });
        this.bankCombo = new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.system.controls.Bank"),// 'Payment Method',
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            name: "bankcombo",
            hiddenName:'bankcombo',
            store: this.bankStore,
            valueField:'accid',
            displayField:'accname',
            allowBlank: true,
            disabled:   this.readOnly,
            width:185,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true//,
        });


        this.excisePayableAccStore.load();
        this.excisePayableAccStore.on("load",function(){
            if(Wtf.excisePayableAcc!="" && Wtf.excisePayableAcc!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.excisePayableAcc.setValue(Wtf.excisePayableAcc);
                }
            if(Wtf.exciseDutyAdvancePaymentaccount!="" && Wtf.exciseDutyAdvancePaymentaccount!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.exciseDutyAdvancePaymentAccount.setValue(Wtf.exciseDutyAdvancePaymentaccount);
            }
            if(Wtf.STPayableAcc!="" && Wtf.STPayableAcc!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.STPayableAcc.setValue(Wtf.STPayableAcc);
            }
            if(Wtf.STAdvancePaymentaccount!="" && Wtf.STAdvancePaymentaccount!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.STAdvancePaymentAccount.setValue(Wtf.STAdvancePaymentaccount);
            }
            if(Wtf.GTAKKCPaybleAccount!="" && Wtf.GTAKKCPaybleAccount!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.GTAKKCPaybleAccount.setValue(Wtf.GTAKKCPaybleAccount);
            }
            if(Wtf.GTASBCPaybleAccount!="" && Wtf.GTASBCPaybleAccount!=undefined && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                this.GTASBCPaybleAccount.setValue(Wtf.GTASBCPaybleAccount);
            }
        },this);
        this.excisePayableAcc=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CompanyExcisePayableAcc")+ "*",
            store:this.excisePayableAccStore,
            name:'excisePayableAcc',
            id:'excisePayableAcc'+this.id,
            hiddenName:'excisePayableAcc',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank:  Wtf.isExciseApplicable?false:true,
            width:185
        });
        this.exciseDutyAdvancePaymentAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.preferences.exciseDutyAdvancePaymentaccount"),
            store:this.excisePayableAccStore,
            name:'exciseDutyAdvancePaymentaccount',
            id:'exciseDutyAdvancePaymentaccount'+this.id,
            hiddenName:'exciseDutyAdvancePaymentaccount',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',
            typeAhead: true,
            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            isAccountCombo:true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            width:185
        });

        this.exciseTariffdetails=new Wtf.form.FieldSet({
            id: this.id+'exciseTariffdetails',
            title: WtfGlobal.getLocaleText("acc.field.india.setAlterexisetariff"),//"Set/Alter excise tarrif details",
            name: 'exciseTariffdetails',
            scope:this,
            checkboxToggle: true,
            collapsed:!Wtf.exciseTariffdetails,
            checkboxName: 'exciseTariffdetails',
            autoWidth: true,
            checked :Wtf.exciseTariffdetails,
            cls : 'custcheckbox',
            hidden: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            items:[
                    this.tariffName = new Wtf.form.TextField({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.tariffname") + "'>" + WtfGlobal.getLocaleText("acc.product.tariffname")+"*"+ "</span>", //WtfGlobal.getLocaleText("acc.product.parentProduct"),//'Parent Product',
                            name: 'tariffName',
                            width:200,
                            value:Wtf.tariffName,
                            allowBlank:Wtf.exciseTariffdetails?false:true,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum"
                    }),
                    this.HSNCode = new Wtf.form.TextField({
                        fieldLabel: WtfGlobal.getLocaleText("acc.product.HSNcode")+"*",
                        name: 'HSNCode',
                        id: 'HSNCode' + this.id,
                        width:200,
                        allowBlank:Wtf.exciseTariffdetails?false:true,
                        maxLength:9,
                        validator: function(val) {
                            if (val.match(/[0-9 | \s]/)) {
                                return true;
                            }
                            else {
                                return "Only Numbers are allowed";
                            }
                        },
                        maskRe:/[0-9 | \s]/
                    }),
                    this.reportingUOM = new Wtf.form.ComboBox({
                        fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.Reportinguom") + "'>" + WtfGlobal.getLocaleText("acc.product.Reportinguom")+"*" + "</span>", //WtfGlobal.getLocaleText("acc.product.parentProduct"),//'Parent Product',
                        forceSelection: true,
                        name: 'reportingUOM',
                        store:this.localUOMStore,
                        hiddenName: 'reportingUOM',
                        valueField: 'uomid',
                        displayField: 'uomname',
                        listWidth:200,
                        selectOnFocus: true,
                        mode: 'local',
                        triggerAction: 'all',
                        typeAhead: true,
                        width:184,
//                        value:Wtf.reportingUOM
                        allowBlank:Wtf.exciseTariffdetails?false:true

                    }),
                    this.exciseMethodCombo = new Wtf.form.ComboBox({
                        fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.valuationType")+ "'>" + WtfGlobal.getLocaleText("acc.product.valuationType")+"*" + "</span>",
                        forceSelection: true,
                        hiddenName: 'excisemethod',
                        name: 'excisemethod',
                        store: Wtf.exciseMethodStore,
                        valueField: 'id',
                        displayField: 'name',
                        listWidth:200,
                        selectOnFocus: true,
                        mode: 'local',
                        triggerAction: 'all',
                        typeAhead: true,
                        width:184,
//                        value:Wtf.exciseMethod,
                        allowBlank:Wtf.exciseTariffdetails?false:true
                    })
//                    ,
//                    this.exciseRate=new Wtf.form.NumberField({
//                        fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.india.excise.rate")+"'>"+WtfGlobal.getLocaleText("acc.field.india.excise.rate")+"(%)"+"*"+"</span>",
//                        name: 'exciserate',
//                        id:'excisedutypercent'+this.id,
//                        minValue:0,
//                        maxValue:100,
//                        allowNegative:false,
//                        width:200,
//                        value:Wtf.exciseRate,
//                        allowBlank:Wtf.exciseTariffdetails?false:true
//                    })
            ]
        });
        this.exciseTariffdetails.on('collapse',function(){
            this.tariffName.allowBlank=true;
            this.tariffName.validate();
            this.HSNCode.allowBlank=true;
            this.HSNCode.validate();
            this.reportingUOM.allowBlank=true;
            this.reportingUOM.validate();
            this.exciseMethodCombo.allowBlank=true;
            this.exciseMethodCombo.validate();
//            this.exciseRate.allowBlank=true;
        },this);
        this.exciseTariffdetails.on('expand',function(){
            this.tariffName.allowBlank=false;
            this.tariffName.validate();
            this.HSNCode.allowBlank=false;
            this.HSNCode.validate();
            this.reportingUOM.allowBlank=false;
            this.reportingUOM.validate();
            this.exciseMethodCombo.allowBlank=false;
            this.exciseMethodCombo.validate();
//            this.exciseRate.allowBlank=false;
        },this);
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            if(Wtf.HSNCode!=""){
                this.HSNCode.setValue(Wtf.HSNCode);
            }
            if(Wtf.exciseMethod != ""){
                this.exciseMethodCombo.setValue(Wtf.exciseMethod);
            }
            this.localUOMStore.load();
            this.localUOMStore.on('load',function(){
                if(Wtf.reportingUOM!= undefined && Wtf.reportingUOM!== ''){
                    var rec = WtfGlobal.searchRecord(this.localUOMStore, Wtf.reportingUOM, "uomid");
                    if(rec && rec.data && rec.data.uomid){
                        this.reportingUOM.setValue(rec.data.uomid);
                    }
                }else{//Default Value
                    var rec = WtfGlobal.searchRecord(this.localUOMStore, "KG", "uomname");
                    if(rec && rec.data && rec.data.uomid){
                        this.reportingUOM.setValue(rec.data.uomid);
                    }
                }
            },this);
            /**
             *Following code is for setting erroricon component properly for combobox.
             **/
            this.exciseMethodCombo.on('invalid',function(comp,value){
                if(comp.errorIcon){
                    comp.errorIcon.setLeft(comp.errorIcon.getLeft() - (comp.getEl().getWidth()/2) + 3);
                }
            },this);
            this.reportingUOM.on('invalid',function(comp,value){
                if(comp.errorIcon){
                    comp.errorIcon.setLeft(comp.errorIcon.getLeft() - (comp.getEl().getWidth()/2) + 3);
                }
            },this);
            this.STPayableAcc.on('invalid',function(comp,value){
                if(comp.errorIcon){
                    comp.errorIcon.setLeft(comp.errorIcon.getLeft() - (comp.getEl().getWidth()/2) + 12);
                }
            },this);
        }

        this.exciseJurisdictiondetails=new Wtf.form.Checkbox({
            id: this.id+'excisejurisdictiondetails',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.india.setAlterjurisdiction"),//"Set/Alter jurisdiction details",
            name: 'excisejurisdictiondetails',
            scope:this,
            checked :Wtf.exciseJurisdictiondetails,
            cls : 'custcheckbox',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA
        });
        this.exciseMultipleUnit=new Wtf.form.Checkbox({
            id: this.id+'exciseMultipleUnit',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.india.enableMultipleUnit"),//"Enable Multiple Excise Units",
            name: 'exciseMultipleUnit',
            scope:this,
            checked :Wtf.exciseMultipleUnit,
            cls : 'custcheckbox',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA
        });
        this.exciseFieldset=new Wtf.form.FieldSet({
            id: 'exciseApplicableFieldSet',
            xtype: 'fieldset',
            title: WtfGlobal.getLocaleText("acc.field.india.isexciseavailable"),
            checkboxToggle: true,
            collapsed: !Wtf.isExciseApplicable,
            checkboxName: 'isExciseApplicable',
            hidden: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,  // k
            items:[this.excisePayableAcc,this.exciseDutyAdvancePaymentAccount,this.UnitName,this.manufacturerTypeCombo,this.ECCNo,this.exciseTariffdetails,this.exciseJurisdictiondetails,this.exciseMultipleUnit,this.exciseCommissionerateName,this.exciseCommissionerateCode,this.exciseDivisionCode,this.exciseRangeCode]
        });
        this.exciseFieldset.on('collapse',function(){
            if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
                this.IsLineLevelTaxUsedInTransaction("2","Excise Duty",this.exciseFieldset);
            }
        },this);
        this.exciseFieldset.on('expand',function(){
            if (Wtf.account.companyAccountPref.registrationType == Wtf.registrationTypeValues.NA) {
                this.exciseFieldset.collapse(true);//To Uncheck the Checkbox used in Fieldset.
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.companyPreferences.exciseFieldSetAlert")]);
                return false;
            }
            this.exciseCommissionerateCode.allowBlank=false;
            this.exciseCommissionerateName.allowBlank=false;
            this.exciseDivisionCode.allowBlank=false;
            this.exciseRangeCode.allowBlank=false;
            this.excisePayableAcc.allowBlank=false;
        },this);

//        this.exciseFieldset.on('beforeexpand',function(){
//            if(!this.cstFieldset.collapsed){
//                this.cstFieldset.collapse(true);
//                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), "Service Tax check is ON. Either Excise Duty or Service Tax will be applicable.</br></br>Are you sure want apply Excise Duty?",function(btn){
//                if(btn=="yes") {
//                    this.cstFieldset.collapse(true);
//                }else{
//                    this.exciseFieldset.collapse(true);
//                    this.cstFieldset.expand();
//                }
//            }, this)
//            }
//        },this);


        this.purchaseTermWindowLink = this.getTermLink('purchase',Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ? WtfGlobal.getLocaleText("acc.field.india.set.Term.purchase") : WtfGlobal.getLocaleText("acc.field.set.Term.purchase"), Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA);
        this.salesTermWindowLink = this.getTermLink('sales',Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ? WtfGlobal.getLocaleText("acc.field.india.set.Term.sales") : WtfGlobal.getLocaleText("acc.field.set.Term.sales"), Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA);
        this.termPanel = new Wtf.Panel({
            border:false,
            width : 600,
            height:50,
            layout:'column',
            hidden: !Wtf.account.companyAccountPref.isLineLevelTermFlag,
            items:[
                {
                    columnWidth: 0.4,
                    items:[
                        this.purchaseTermWindowLink
                    ]
                },{
                    columnWidth: 0.4,
                    items:[
                        this.salesTermWindowLink
                    ]
                }]
        })
        this.SMTPConfigLink = this.getSMTPConfigurationLink(WtfGlobal.getLocaleText("acc.field.smtpAuthForm"));
        
        this.integrationSettingsFieldSet = Wtf.integrationFunctions.createIntegrationSettingsFieldSet(this);
        
        this.form=new Wtf.form.FormPanel({
            style: 'background: white;',
            border:false,
            buttonAlign:'left',
            autoScroll:true,
            defaults:{
                labelWidth:200,
                border:false
            },
            items:[{
                layout:'column',
                defaults:{
                    border:false,
                    bodyStyle:'padding:10px'
                },
                items:[{
                    columnWidth:.49,
                    layout:'form',
                    items:[{
                        xtype:'fieldset',
                        autoHeight:true,
                        id:"financialYearSettings"+this.helpmodeid,
                        title:WtfGlobal.getLocaleText("acc.accPref.FYsettings"),  //'Financial Year Settings',
                        title:"<span wtf:qtip= '"+WtfGlobal.getLocaleText("acc.accPref.FYsettingsTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.FYsettings")+"</span>",
                        defaults:{
                            format:WtfGlobal.getOnlyDateFormat(),
                            allowBlank:false
                        },
                        items:[{
                            layout:'column',
                            border:false,
                            labelWidth:130,
                            defaults:{
                                border:false
                            },
                            items:[{
                                layout:'form',
                                columnWidth:0.42,
                                items:this.fdays
                            },{
                                columnWidth:0.30,
                                labelWidth:40,
                                layout:'form',
                                items:this.fmonth
                            },{ //Show Financial Start YEAR as read only[SK]
                                columnWidth:0.27,
                                labelWidth:35,
                                layout:'form',
                                items:this.finanyear
                            //                                    new Wtf.form.Field({
                            //                                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
                            //                                            cls:"clearStyle",
                            //                                            anchor:'95%',
                            //                                            readOnly:true,
                            //                                            value:this.currentyear
                            //                                        })
                            }]
                        },{
                            layout:'column',
                            border:false,
                            labelWidth:130,
                            defaults:{
                                border:false
                            },
                            items:[{
                                layout:'form',
                                columnWidth:0.42,
                                items:this.bdays
                            },{
                                columnWidth:0.30,
                                labelWidth:40,
                                layout:'form',
                                items:this.bmonth
                            },{
                                columnWidth:0.27,
                                labelWidth:35,
                                layout:'form',
                                items:this.byear
                            }]
                        }]
                    },{
                        columnWidth:.49,
                        layout:'fit',
                        items:this.grid
                    },
                    this.defaultPurchaseAccountSettings,
                    this.defaultSalesAccountSettings,
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,       // k
                        hideLabel: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.YoucansetCompanyVATTaxDetailNumbers")+"'>"+WtfGlobal.getLocaleText("acc.field.CompanyVATTaxDetailNumbers")+"</span>" ,
                        items:[
                        this.country =new Wtf.form.TextField({
                                fieldLabel:WtfGlobal.getLocaleText("acc.field.Country"),
                                id:"setupwiz"+"country",
                                width : 200,
                                name:'country',
                                hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                                hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                                readOnly:true,
                                value: Wtf.account.companyAccountPref.countryname
                            }),
                        this.state =new Wtf.form.TextField({
                                fieldLabel:WtfGlobal.getLocaleText("acc.field.State"),
                                id:"setupwiz"+"state",
                                width : 200,
                                name:'State',
                                hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                                hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                                readOnly:true,
                                value:Wtf.account.companyAccountPref.statename
                            }),
                        this.dateOfRegistration= new Wtf.form.DateField({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.dateofregistration"),
                            format:WtfGlobal.getOnlyDateFormat(),
                            maxValue : new Date(),
                            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            forceSelection: false,
                            width : 183,
                            value:Wtf.dateofregistration!=undefined?new Date(Wtf.dateofregistration):""
                        }),
                        this.VATTINNo= new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.CompanyVATTINNO"),// "Company VTN"
                            value: Wtf.CompanyVATNumber,
                            width:200,
                            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            maxLength:11,
                            regex:/\d{10}[a-z | A-Z | 0-9 ]{1}/,
                            invalidText :'The value in this field is invalid'
                        }),
                        this.CSTTINNo= new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.CompanyCSTTINNO"),// "Customer VTN"
                            value: Wtf.CompanyCSTNumber,
                            width:200,
                            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            maxLength:11,
                            invalidText :'Alphabets and numbers only',
                            regex:/\d{10}[a-z | A-Z | 0-9 ]{1}/,
                            invalidText :'The value in this field is invalid'
                        }),
                        this.CSTRegDate= new Wtf.form.DateField({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.CompanyCSTRegDate"),
                            format:WtfGlobal.getOnlyDateFormat(),
                            maxValue : new Date(),
                            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            forceSelection: false,
                            width : 183,
                            value:Wtf.cstregistrationdate!=undefined?new Date(Wtf.cstregistrationdate):""
                        }),
                        this.SERVICENo =new Wtf.form.TextField({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Companyservice"),// "Customer Service TAX"
                            value: Wtf.CompanyServiceTaxRegNumber,
                            width:200,
//                            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
//                            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            maxLength:15,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum",
                            hidden : true,
                            hideLabel : true
                        }),
                        this.returncode =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.returncode") ,// "Return Code"
                            value: Wtf.returncode,
                            width:200,
                            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                            maxLength:30,
                            invalidText :'Alphabets and numbers only',
                            //vtype : "alphanum"
                            listeners: {
                                change: function(obj){
                                   obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\,\'\"\;\:\<\>\!\~\`\_\=]/g, ""));
                                }
                            }
                        }),this.vatPayableAcc,this.vatInCreditAvailAcc,this.CSTPayableAcc,this.pmtMethod,this.bankCombo
                        ]
                    },
                    this.IndonesiaCountrySpecificNumbers = new Wtf.form.FieldSet({
                        autoHeight: true,
                        hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDONESIA,
                        title: WtfGlobal.getLocaleText("acc.companypreferences.companyOtherDetails"), //Company GST Details
                        items: [this.NPWPNo = new Wtf.form.TextField({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.CompanyNPWPNO"), // "Customer NPWP"
                                value: Wtf.CompanyNPWPNumber,
                                width: 200,
                                maxLength: 20,
                                invalidText: 'Numbers only',
                                regex: /\d{2}\.\d{3}\.\d{3}\.\d{1}[-.]\d{3}\.\d{3}/,
                                regexText: 'Invalid NPWP No. (eg."01.567.505.1-056.000")',
                                emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenter") + " " + WtfGlobal.getLocaleText("acc.setupwizard.npwp")
                            })]
                    }),
                    this.tdsFieldset,
                    this.cstFieldset,
                    this.exciseFieldset,
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden: !Wtf.account.companyAccountPref.isLineLevelTermFlag || Wtf.account.companyAccountPref.avalaraIntegration,//hide if Avalara Integration is activated
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.linelevelterms")+"'>"+WtfGlobal.getLocaleText("acc.field.linelevelterms")+"</span>" ,
                        items:[
                            this.termPanel
                        ]
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,  // k
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.statutorycomp")+"'>"+WtfGlobal.getLocaleText("acc.field.statutorycomp")+"</span>" ,
                        items:[
                        this.enableVATCST =new Wtf.form.Checkbox({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.enalbleVATCST") ,
                            width:200,
                            checked:Wtf.account.companyAccountPref.enablevatcst,
                            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA
                        }),
                        this.registrationTypeCombo,
                        this.CINnumber =new Wtf.form.TextField({// Corporate Identity Number
                            fieldLabel:"Corporate Identity Number"+WtfGlobal.addLabelHelp("<table style='border-collapse: collapse; border: 1px solid black;padding: 5px;'><tr><td width='150px' style=' border: 1px solid black;padding: 5px;'><b>Digit No.</b></td><td width='180px'style=' border: 1px solid black;padding: 5px;'><b>What it Shows?</b></td><td style=' border: 1px solid black;padding: 5px;' width='650px' ><b>Remarks</b></td></tr><tr><td style=' border: 1px solid black;padding: 5px;'>1st Digit</td><td style=' border: 1px solid black;padding: 5px;' width='350px'>Listing Status</td><td style=' border: 1px solid black;padding: 5px;'>If Company is listed it will start with 'L' and If Company is not listed it will start with 'U' </td></tr><tr><td style=' border: 1px solid black;padding: 5px;'>Next 5 digit</td><td style=' border: 1px solid black;padding: 5px;'>Industry code</td><td style=' border: 1px solid black;padding: 5px;'> </td></tr> <tr><td style=' border: 1px solid black;padding: 5px;'>Next 2 digit</td><td style=' border: 1px solid black;padding: 5px;'>State code</td><td style=' border: 1px solid black;padding: 5px;'>i.e MH for Maharastra. </td></tr><tr><td style=' border: 1px solid black;padding: 5px;'>Next 4 digit</td><td style=' border: 1px solid black;padding: 5px;'>Year of incorporation</td><td style=' border: 1px solid black;padding: 5px;'>i.e For company formed in calendar Year 2011 the same will be 2011</td></tr><tr><td style=' border: 1px solid black;padding: 5px;'>Next 3 digit</td><td style=' border: 1px solid black;padding: 5px;'>Ownership</td><td style=' border: 1px solid black;padding: 5px;'>PLC for Public Limited Company<br>PTC for Private Limited Company</td></tr><tr><td style=' border: 1px solid black;padding: 5px;'>Last 6 digit</td><td style=' border: 1px solid black;padding: 5px;'>ROC reg.</td><td style=' border: 1px solid black;padding: 5px;'>i.e 090868 for ROC-Mumbai<br>i.e 090633 for ROC-Kolkata</td></tr></table>") ,
                            anchor:'89%',
                            name:'CINnumber',
                            regex:/[L|U]{1}\d{5}[A-Z]{2}\d{4}(PLC|PTC)\d{6}$/, // L or U - 5 Digit number - 2 Char State code(MH for Maharastra) - 4 Digit Number(Year)- PLC(public ltd.) or PTC(Private ltd) - 6 Digit number(ROC reg)
                            value:Wtf.CINnumber,
                            maxLength:21
                        }),
                        this.typeOfDealer=new Wtf.form.ComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.typeofdealer"),
                            anchor:'89%',
//                            listWidth:140,
                            forceSelection: true,
                            triggerAction:'all',
                            store:this.typeofdealerStore,
                            id:'typeoddealer'+this.id,
                            displayField:'dealerName',
                            mode:'local',
                            valueField:'dealerType',
                            forceSelection: false
                        }),

                        this.dateOfApplicability= new Wtf.form.DateField({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.applicabilityofvat"),
                            format:WtfGlobal.getOnlyDateFormat(),
                            forceSelection: false,
                            anchor:'89%',
                            value:Wtf.account.companyAccountPref.applicabilityofvat?new Date(Wtf.account.companyAccountPref.applicabilityofvat):"",
                            hidden:true,
                            hideLabel:true
//                            name:'applicabilityofvat'
                        }),
                        this.assessmentcircle =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.assessmentcircle") ,
                            anchor:'89%',
                            maxLength:15,
                            value:Wtf.account.companyAccountPref.assessmentcircle,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum"
                        }),
                        this.division =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.division") ,
                            anchor:'89%',
                            value:Wtf.account.companyAccountPref.division,
                            maxLength:15,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum"
                        }),
                        this.areaCode =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.areacode") ,
                            anchor:'89%',
                            value:Wtf.account.companyAccountPref.areacode,
                            maxLength:15,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum"
                        }),
                        this.importexportcode =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.importexportcode") ,
                            anchor:'89%',
                            value:Wtf.account.companyAccountPref.importexportcode,
                            maxLength:10,
                            invalidText :'Numbers only',
                            //vtype : "alphanum",
                             validator : function (val){
                                   if (Wtf.isEmpty(val)) {
                                       return true;
                                   }else{
                                       var reg = new RegExp('^[0-9]+$');
                                       if(reg.test(val)){
                                           return true;
                                       }else{
                                           return false;
                                       }
                                   }
                            }
                        }),
                        this.authrizedby =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.authrizedby") ,
                            anchor:'89%',
                            value:Wtf.account.companyAccountPref.authorizedby,
                            maxLength:32,
                            invalidText :'Alphabets only',
                            listeners: {
                                change: function(obj){
                                   obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\,\'\"\;\:\<\>\!\~\`\_0-9]/g, ""));
                                }
                            }
                        }),
                        this.authrizedperson =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.authrizedperson") ,
                            anchor:'89%',
                            maxLength:32,
                            value:Wtf.account.companyAccountPref.authorizedperson,
                            invalidText :'Alphabets only',
                            listeners: {
                                change: function(obj){
                                    obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\,\'\"\;\:\<\>\!\~\`\_0-9]/g, ""));
                                }
                            }
                        }),
                        this.statudORdesignation =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.statusordesignation") ,
                            anchor:'89%',
                            maxLength:32,
                            value:Wtf.account.companyAccountPref.statusordesignation,
                            invalidText :'Alphabets only',
                            listeners: {
                                change: function(obj){
                                  obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\,\'\"\;\:\<\>\!\~\`\_0-9]/g, ""));
                                }
                            }
                        }),
                        this.place =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.place") ,
                            anchor:'89%',
                            maxLength:15,
                            value:Wtf.account.companyAccountPref.place,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alpha"
                        }),
                        this.VATTINcomposition =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.VATTINComposition") ,
                            anchor:'89%',
                            maxLength:11,
                            value:Wtf.account.companyAccountPref.vattincomposition,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum",
                            hidden : true,
                            hideLabel : true
                        }),
                        this.VATTINregular =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.VATTINregular") ,
                            anchor:'89%',
                            maxLength:11,
                            value:Wtf.account.companyAccountPref.vattinregular,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum",
                            hidden : true,
                            hideLabel : true
                        }),
                        this.LocalSalesTaxNumber= new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.LocalSalesTaxNumber"),
                            anchor:'89%',
                            value:Wtf.account.companyAccountPref.localsalestaxnumber,
                            maxLength:10,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum",
                            hidden : true,
                            hideLabel : true
                        }),
                        this.InterStateSalesTaxNumber =new Wtf.form.TextField({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.InterStateSalesTaxNumber") ,
                            anchor:'89%',
                            value:Wtf.account.companyAccountPref.interstatesalestaxnumber,
                            maxLength:15,
                            invalidText :'Alphabets and numbers only',
                            vtype : "alphanum",
                            hidden : true,
                            hideLabel : true
                        })

                        ]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Youcansetapprovalsettingsforsendingapproval")+"'>"+WtfGlobal.getLocaleText("acc.field.TransactionApprovalMailSetting")+"</span>" ,
                        items:[this.approvalMail,this.sendMailTo,{
                            xtype: 'panel',
                            border: false,
                            cls: 'emailfieldInfo',
                            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
                        }]
                    },{
                        xtype:'fieldset',
                        collapsible:true,
                        collapsed:true,
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.otherMailSettingTTip")+"'>"+WtfGlobal.getLocaleText("acc.field.transactionGenerationMailSetting")+"</span>" ,
                        items:[this.purchaseReqGenerationMail,this.purchaseReqUpdationMail,this.vendorQuotationGenerationMail,this.vendorQuotationUpdationMail,this.purchaseOrderGenerationMail,this.purchaseOrderUpdationMail,this.purchaseInvoiceGenerationMail,this.purchaseInvoiceUpdationMail,this.goodsReceiptGenerationMail,this.goodsReceiptUpdationMail,this.purchaseReturnGenerationMail,this.purchaseReturnUpdationMail,this.vendorPaymentGenerationMail,this.vendorPaymentUpdationMail,this.debitNoteGenerationMail,this.debitNoteUpdationMail,
                               this.customerQuotationGenerationMail,this.customerQuotationUpdationMail,this.salesOrderGenerationMail,this.salesOrderUpdationMail,this.salesInvoiceGenerationMail,this.salesInvoiceUpdationMail,this.deleveryOrderGenerationMail,this.deleveryOrderUpdationMail,this.salesReturnGenerationMail,this.salesReturnUpdationMail,this.receiptGenerationMail,this.receiptUpdationMail,this.creditNoteGenerationMail,this.creditNoteUpdationMail,
                               this.leaseQuotationGenerationMail,this.leaseQuotationUpdationMail,this.leaseOrderGenerationMail,this.leaseOrderUpdationMail,this.leaseDeliveryOrderGenerationMail,this.leaseDeliveryOrderUpdationMail,this.leaseReturnGenerationMail,this.leaseReturnUpdationMail,this.leaseInvoiceGenerationMail,this.leaseInvoiceUpdationMail,this.leaseContractGenerationMail,this.leaseContractUpdationMail,
                               this.consignmentReqGenerationMail,this.consignmentReqUpdationMail,this.consignmentDOGenerationMail,this.consignmentDOUpdationMail,this.consignmentInvoiceGenerationMail,this.consignmentInvoiceUpdationMail,this.consignmentReturnGenerationMail,this.consignmentReturnUpdationMail,
                               this.consignmentPReqGenerationMail,this.consignmentPReqUpdationMail,this.consignmentPDOGenerationMail,this.consignmentPDOUpdationMail,this.consignmentPInvoiceGenerationMail,this.consignmentPInvoiceUpdationMail,this.consignmentPReturnGenerationMail,this.consignmentPReturnUpdationMail,
                               this.assetPurchaseReqGenerationMail,this.assetPurchaseReqUpdationMail,this.assetVendorQuotationGenerationMail,this.assetVendorQuotationUpdationMail,this.assetPurchaseOrderGenerationMail,this.assetPurchaseOrderUpdationMail,this.assetPurchaseInvoiceGenerationMail,this.assetPurchaseInvoiceUpdationMail,this.assetDisposalInvoiceGenerationMail,
                               this.assetDisposalInvoiceUpdationMail,this.assetGoodsReceiptGenerationMail,this.assetGoodsReceiptUpdationMail,this.assetDeliveryOrderGenerationMail,this.assetDeliveryOrderUpdationMail,this.assetPurchaseReturnGenerationMail,this.assetPurchaseReturnUpdationMail,this.assetSalesReturnGenerationMail,this.assetSalesReturnUpdationMail,
                               this.consignmentRequestApproval,this.qtyBelowReorderLevelMail,this.recurringInvoiceMail,this.RFQGenerationMail,this.RFQUpdationMail]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.defaultMailsenderSettingTTip")+"'>"+WtfGlobal.getLocaleText("acc.field.defaultMailsenderSetting")+"</span>" ,
                        items:[this.companyMail=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.companymail"),  //companymail,
                            name:'defaultmailsenderFlag',
                            id:'companymail',
                            checked:Wtf.account.companyAccountPref.defaultmailsenderFlag==Wtf.companyMail
                        }), this.UserMail=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.usermail"),  //usermail
                            name:'defaultmailsenderFlag',
                            id:'usermail',
                            checked:Wtf.account.companyAccountPref.defaultmailsenderFlag==Wtf.UserMail
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                         defaults:{
                            anchor:'80%'
                        },
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.defaultMailsenderSettingTTip")+"'>"+WtfGlobal.getLocaleText("acc.field.importMailsenderSetting")+"</span>" ,
                        items:[this.userEmailCombo,this.sendImportMailTo]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.accSettingsTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.accSettings")+"</span>",
                        id:'accountsettings'+this.helpmodeid,
                        defaults:{
                            forceSelection: true,
                            allowBlank:false,
                            anchor:'80%'
                        },
                        items:[
                        //                        this.Cash= new Wtf.form.FnComboBox({
                        ////                            addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
                        //                            fieldLabel:'Cash goes to',
                        //                            name:'cashaccount',
                        //                            store:this.dgStore,
                        //                            hiddenName:'cashaccount',
                        //                            displayField:'accountname',
                        ////                            disabled:true,
                        //                            valueField:'accountid',
                        ////                            value:Wtf.account.companyAccountPref.cashaccount,
                        //                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        //
                        //                        }),
                        this.Cash= new Wtf.form.Field({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.cashGoesTo"),  //'Cash goes to',
                            name:'cashaccount',
                            hiddenName:'cashaccount',
                            cls:"clearStyle",
                            readOnly:true,
                            value:'Cash in hand'
                        }),
                        this.DiscountGiven= new Wtf.form.ExtFnComboBox({
                            //                            addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.disGivenGoesTo"),  //'Discount Given goes to',
                            name:'discountgiven',
                            store:this.dgStore,
                            hiddenName:'discountgiven',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode']:[],
                            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
                            //       value:Wtf.account.companyAccountPref.discountgiven,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.DiscountReceived= new Wtf.form.ExtFnComboBox({
                            //                            addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.disReceivedGoesTo"),  //'Discount Received goes to',
                            name:'discountreceived',
                            store:this.dgStore,
                            hiddenName:'discountreceived',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            //      value:Wtf.account.companyAccountPref.discountreceived,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.Depreciation= new Wtf.form.ExtFnComboBox({
                            //      addNewFn:this.addNewAccount.createDelegate(this,[false,null,"depcoaWin",true],true),
                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.assetDrepGoesTo"),  //'Asset Depreciation goes to',
                            name:'depreciationaccount',
                            store:this.exStore,//this.depStore,
                            hiddenName:'depreciationaccount',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            //      value:Wtf.account.companyAccountPref.depreciationaccount,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.ForeignExchange= new Wtf.form.ExtFnComboBox({
                            //      addNewFn:this.addNewAccount.createDelegate(this,[false,null,"fxcoaWin",true],true),
                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.foreignExchangeGoesTo"),  //'Foreign Exchange Gain/Loss goes to',
                            name:'foreginexchange',
                            store:this.exStore,
                            hiddenName:'foreignexchange',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            //    value:Wtf.account.companyAccountPref.foreignexchange,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.UnrealisedGainLoss= new Wtf.form.ExtFnComboBox({
                            //      addNewFn:this.addNewAccount.createDelegate(this,[false,null,"fxcoaWin",true],true),
                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.UnrealisedGoesTo"),  //'Unrealised Gain Loss goes to',
                            name:'unrealisedgainloss',
                            store:this.exStore,
                            hiddenName:'unrealisedgainloss',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            //    value:Wtf.account.companyAccountPref.foreignexchange,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        //                        }),
                        //                        this.ShippingCharges= new Wtf.form.FnComboBox({
                        //                            addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
                        //                            hirarchical:true,
                        //                            fieldLabel:'Shipping Charges go to',
                        //                            name:'shippingcharges',
                        //                            store:this.dgStore,
                        //                            hiddenName:'shippingcharges',
                        //                            displayField:'accountname',
                        //                            valueField:'accountid',
                        //                            value:Wtf.account.companyAccountPref.shippingcharges,
                        //                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText"),
                        //                            hidden:true,
                        //                            hideLabel:true
                        }),
//                        this.OtherCharges= new Wtf.form.FnComboBox({
//                            //     addNewFn:this.addNewAccount.createDelegate(this,[false,null,"coaWin",false],true),
//                            hirarchical:true,
//                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.otherCharges"),
//                            name:'othercharges',
//                            store:this.dgStore,
//                            hiddenName:'othercharges',
//                            displayField:'accountname',
//                            valueField:'accountid',
//                            //      value:Wtf.account.companyAccountPref.othercharges,
//                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText"),
//                            hidden:true,
//                            hideLabel:true
//
//                        }),
                        this.expenseAccount= new Wtf.form.ExtFnComboBox({
                            //                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.SalaryExpensegoesto"),
                            name:'expenseaccount',
                            store:this.expenseStore,
                            forceSelection: false,
                            allowBlank:true,
                            hiddenName:'expenseaccount',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.liabilityAccount= new Wtf.form.ExtFnComboBox({
                            //                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.SalaryPayable(Liability)goesto"),
                            name:'liabilityaccount',
                            store:this.liabilityStore,
                            forceSelection: false,
                            allowBlank:true,
                            hiddenName:'liabilityaccount',
                            displayField:'accountname',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            valueField:'accountid',
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.customerDefaultAccount= new Wtf.form.ExtFnComboBox({
                            //                            hirarchical:true,
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.CustomerDefaultAccount"),
                            name:'customerdefaultacc',
                            store:this.dgStore,
                            forceSelection: false,
                            allowBlank:true,
                            hiddenName:'customerdefaultacc',
                            displayField:'accountname',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            textraComparisionField:'acccode',// type ahead search on acccode as well.
                            valueField:'accountid',
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.vendorDefaultAccount= new Wtf.form.ExtFnComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.VendorDefaultAccount"),
                            name:'vendordefaultacc',
                            store:this.liabilityStore,
                            forceSelection: false,
                            allowBlank:true,
                            hiddenName:'vendordefaultacc',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        })
                    ]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.comperf.adjustmentaccountsetting")+"'>"+WtfGlobal.getLocaleText("acc.comperf.adjustmentaccountsetting")+"</span>",
                        defaults:{
                            anchor:'80%'
                        },
                        items:[this.roundingDifferenceAccount = new Wtf.form.ExtFnComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.filed.roundingdiffAccount"),
                            name:'roundingDifferenceAccount',
                            store:this.exStore,
                            forceSelection: false,
                            allowBlank:false,
                            hiddenName:'roundingDifferenceAccount',
                            displayField:'accountname',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            extraComparisionField:'acccode',
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            valueField:'accountid',
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.adjustmentAccountPayment = new Wtf.form.ExtFnComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.filed.adjustmentaccountMP"),
                            name:'adjustmentAccountPayment',
                            store:this.exStore,
                            forceSelection: true,
                            allowBlank:true,
                            hiddenName:'adjustmentAccountPayment',
                            displayField:'accountname',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            extraComparisionField:'acccode',
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            valueField:'accountid',
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.adjustmentAccountReceipt = new Wtf.form.ExtFnComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.filed.adjustmentaccountRP"),
                            name:'adjustmentAccountReceipt',
                            store:this.exStore,
                            forceSelection: true,
                            allowBlank:true,
                            hiddenName:'adjustmentAccountReceipt',
                            displayField:'accountname',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            extraComparisionField:'acccode',
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            valueField:'accountid',
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        })]
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.writeOff.AccountSetting")+"'>"+WtfGlobal.getLocaleText("acc.writeOff.AccountSetting")+"</span>",
                        defaults:{
                            anchor:'80%'
                        },
                        items:[this.invoiceWriteOffAccount= new Wtf.form.ExtFnComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.combo.invoicesWriteOffAccount"),
                            name:'invoicesWriteOffAccount',
                            store:this.dgStore,
                            forceSelection: true,
                            allowBlank:true,
                            hiddenName:'invoicesWriteOffAccount',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),
                        this.receiptWriteOffAccount= new Wtf.form.ExtFnComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.combo.receiptsWriteOffAccount"),
                            name:'receiptsWriteOffAccount',
                            store:this.dgStore,
                            forceSelection: true,
                            allowBlank:true,
                            hiddenName:'receiptsWriteOffAccount',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        })]
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:!WtfGlobal.isIndiaCountryAndGSTApplied(),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.IndianGSTSettings.gstmodule")+"'>"+WtfGlobal.getLocaleText("acc.IndianGSTSettings.gstmodule")+"</span>",
                        items:[new Wtf.Toolbar.Button({
                                text: WtfGlobal.getLocaleText("acc.IndianGSTSettings.gstmodule"),
                                tooltip:WtfGlobal.getLocaleText("acc.IndianGSTSettings.gstmodule"),
                                scope: this,
                                handler:this.IndianGSTSettings
                            })]
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.EditPermissionToonlyCreator")+"'>"+WtfGlobal.getLocaleText("acc.field.EditPermissionToonlyCreator")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.editSOTransaction=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoSO"),
                            name:'editso',
                            cls:'checkboxtopPosition',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.editso
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.accPref.updateloadSetting"),  //'Inventory Settings',
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.updateloadSettingTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.updateloadSetting")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[
                        this.showVendorUpdate=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.showvendorupdate"),  //show Vendor Update
                            name:'showVendorUpdate',
                            cls:'checkboxtopPosition',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showVendorUpdate
                        }),
                        this.showCustomerUpdate=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.showcustomerupdate"),  //Show Customer Update
                            name:'showCustomerUpdate',
                            cls:'checkboxtopPosition',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showCustomerUpdate
                        }),
                        this.showProductUpdate=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.showproductupdate"),  //Show Product Update
                            name:'showProductUpdate',
                            cls:'checkboxtopPosition',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showProductUpdate
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CustomReportBuilderSettings.ttip")+"'>"+WtfGlobal.getLocaleText("acc.field.CustomReportBuilderSettings")+"</span>",
                        items:[this.showPivotInCustomReports=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.CustomReport.pivotReports"), //Enable/Disable Pivot Reports in Custom Report Builder
                            labelStyle:'width: 250px;',
                            anchor:'80%',
                            name:'showPivotInCustomReports',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showPivotInCustomReports
                        })]
                    }, this.integrationSettingsFieldSet, {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+"Connect Bank Accounts"+"'>"+"Connect Bank Accounts"+"</span>",  
                        defaults:{
                            maxLength:50
                        },
                        items:[this.selectBankCombo = new Wtf.form.ComboBox({
                            fieldLabel: "<span wtf:qtip='" + "Select Bank" + "'>" + "Select Bank" + "* " + "</span>",
                            emptyText:"Select a bank",
                            selectOnFocus: true,
                            typeAhead: true,
                            triggerAction: 'all',
                            mode: 'local',
                            store: new Wtf.data.JsonStore({
                                fields: ['id', 'name'],
                                data: [
                                    {id: "5", name: "DBS Bank"}
                                ],
                                autoLoad: true
                            }),
                            forceSelection: true,
                            displayField: 'name',
                            valueField: 'id',
                            listWidth: 150,
                            width: 150
                        }), this.addBankAccountBttn = new Wtf.Button({
                            text: "Connect Bank Accounts      ",
                            tooltip: "Connect Bank Accounts",
                            disabled: false,
                            style: 'margin-top: 8px',
                            scope: this,
                                            handler: function (obj, value) {
                                                if (this.selectBankCombo && this.selectBankCombo.getValue()) {
                                                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), "<b>Login and Connect Accounts</b>", function (btn) {
                                                        if (btn == "yes") {
                                                            new Wtf.account.DbsAccountsMappingWindow({
                                                                title: "<span wtf:qtip='" + "Connect Accounts" + "'>" + "Connect Accounts" + "</span>",
                                                                width: 1000,
                                                                modal: true,
                                                                constrain: true,
                                                                resizable: false,
                                                                autoScroll: true,
                                                                isReadOnly: false,
                                                                bodyStyle: "background-color:#f1f1f1;",
                                                                height: 375,
                                                                layout: "border"
                                                            }).show();

                                                        }
                                                    }, this);
                                                } else {
                                                    this.selectBankCombo.markInvalid();
                                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("Please select Bank first.")], 0);
                                                    return;
                                                }
                                            }
                        }),this.viewBankAccountMappingButton = new Wtf.Button({
                                            text: "View Connected Bank Accounts",
                                            tooltip: "View Connected Bank Accounts",
                                            disabled: false,
                                            style: 'margin-top: 8px',
                                            scope: this,
                                            handler: function (obj, value) {
                                                new Wtf.account.DbsAccountsMappingWindow({
                                                    title: "<span wtf:qtip='" + "Connected Bank Accounts" + "'>" + "Connected Bank Accounts" + "</span>",
                                                    width: 1000,
                                                    modal: true,
                                                    constrain: true,
                                                    resizable: false,
                                                    autoScroll: true,
                                                    isReadOnly: true,
                                                    bodyStyle: "background-color:#f1f1f1;",
                                                    height: 375,
                                                    layout: "border"
                                                }).show();

                                            }
                                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.PickPackShipSettings.ttip")+"'>"+WtfGlobal.getLocaleText("acc.field.PickPackShipSettings")+"</span>",  //Added checkbox for memmo setting
                        defaults:{
                            maxLength:50
                        //  validator:this.validateFormat
                        },
                        items:[this.packingDoList=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.erp.activatepackingdolist"), // "Activate Pick And Pack Functionality in DO Report",
                            labelStyle:'width: 250px;',
                            anchor:'80%',
                            name:'packingdolist',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.packingdolist
                        }),this.pickPackShip=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.pickpackship.msg1"), // "Activate Pick,Pack and Ship Functionality in DO Report",
                            labelStyle:'width: 250px;',
                            anchor:'80%',
                            name:'pickpackship',
                            id:'pickpackship'+this.id,
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.pickpackship
                        }),this.interloconpick=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.pickpackship.msg2"), // "Activate Pick,Pack and Ship Functionality in DO Report",
                            labelStyle:'width: 250px;',
                            name:'interloconpick',
                            anchor:'80%',
                            autoWidth:true,
                            hidden:true,
                            hideLabel:true,
                            checked:Wtf.account.companyAccountPref.interloconpick
                        }),this.packinglocation = new Wtf.form.ComboBox({
                            anchor:'80%',
                            triggerAction:"all",
                            mode:"local",
                            typeAhead:true,
                            forceSelection:true,
                            store:this.locationList,
                            displayField:"name",
                            valueField:"id",
                            fieldLabel:WtfGlobal.getLocaleText("acc.pickpackship.msg3"),
                            hiddenName:"packinglocation",
                            width:150,
                            listWidth:150,
                            parent:this,
                            hidden:true,
                            hideLabel:true,
                            id: 'packinglocation' + this.id,
                            name: 'packinglocation',
                            emptyText:"Packing Location",
                            disabled : !(Wtf.account.companyAccountPref.pickpackship),
                            allowBlank: !(Wtf.account.companyAccountPref.pickpackship)
                        }),this.packingstore = new Wtf.form.ComboBox({
                            anchor:'80%',
                            triggerAction:"all",
                            mode:"local",
                            typeAhead:true,
                            forceSelection:true,
                            store:this.storeList,
                            displayField:"fullname",
                            valueField:"store_id",
                            fieldLabel:WtfGlobal.getLocaleText("acc.pickpackship.msg4"),
                            hiddenName:"packingstore",
                            width:150,
                            parent:this,
                            id: 'packingstore'+this.id,
                            name: 'packingstore',
                            emptyText:"Packing Store",
                            disabled : !(Wtf.account.companyAccountPref.pickpackship),
                            allowBlank: !(Wtf.account.companyAccountPref.pickpackship)
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.OtherSetting.ttip")+"'>"+WtfGlobal.getLocaleText("acc.field.OtherSetting")+"</span>",  //Added checkbox for memmo setting
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        //  validator:this.validateFormat
                        },
                        items:[
//                          this.systemManagementFlag=new Wtf.form.Checkbox({
//                            }
//                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.systemmanagement"),
//                            labelStyle:'width: 280px;',
//                            name:'systemManagementFlag',
//                            checked:Wtf.account.companyAccountPref.systemManagementFlag
//                        }),
                        this.masterManagementFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.mastersmanagement"),
                            labelStyle:'width:250px;',
                            name:'masterManagementFlag',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.masterManagementFlag
                        }),this.generalledgerManagementFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.GeneralLedger/Cash/Bankmanagement"),
                            labelStyle:'width: 250px;',
                            autoWidth:true,
                            name:'generalledgerManagementFlag',
                            checked:Wtf.account.companyAccountPref.generalledgerManagementFlag
                        }),this.accountsreceivablesalesFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.AccountsReceivableSalesmanagement"),
                            labelStyle:'width: 250px;',
                            name:'accountsreceivablesalesFlag',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.accountsreceivablesalesFlag
                        }),this.accountpayableManagementFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.AccountsPayablePurchasesmanagement"),
                            labelStyle:'width: 250px;',
                            autoWidth:true,
                            name:'accountpayableManagementFlag',
                            checked:Wtf.account.companyAccountPref.accountpayableManagementFlag
                        }),this.securityGateEntryFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.securitygate.flag"),
                            labelStyle:'width: 250px;',
                            autoWidth:true,
                            name:'securityGateEntryFlag',
                            checked:Wtf.account.companyAccountPref.securityGateEntryFlag
                        }),this.assetManagementFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.Assetsmanagement"),
                            labelStyle:'width: 250px;',
                            autoWidth:true,
                            name:'assetManagementFlag',
                            hidden:Wtf.defaultReferralKeyflag,
                            hideLabel:Wtf.defaultReferralKeyflag,
                            checked:Wtf.account.companyAccountPref.assetManagementFlag
                        }),this.leaseManagementFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.lease.management"),
                            labelStyle:'width: 250px;',
                            autoWidth:true,
                            name:'leaseManagementFlag',
                            hidden:Wtf.defaultReferralKeyflag,
                            hideLabel:Wtf.defaultReferralKeyflag,
                            checked:Wtf.account.companyAccountPref.leaseManagementFlag
//                        }),this.consignmentSalesManagementFlag=new Wtf.form.Checkbox({
//                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.ConsignmentStock.salesmanagement"),
//                            labelStyle:'width: 250px;',
//                            name:'consignmentSalesManagementFlag',
//                            checked:Wtf.account.companyAccountPref.consignmentSalesManagementFlag
//                        }),this.consignmentPurchaseManagementFlag=new Wtf.form.Checkbox({
//                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.ConsignmentStock.purchasemanagement"),
//                            labelStyle:'width: 250px;',
//                            name:'consignmentPurchaseManagementFlag',
//                            checked:Wtf.account.companyAccountPref.consignmentPurchaseManagementFlag
                        }),this.statutoryManagementFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.Statutorymanagement"),
                            labelStyle:'width: 250px;',
                            autoWidth:true,
                            name:'statutoryManagementFlag',
                            checked:Wtf.account.companyAccountPref.statutoryManagementFlag
                        }),this.miscellaneousManagementFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.Miscellaneousmanagement"),
                            labelStyle:'width: 250px;',
                            autoWidth:true,
                            name:'miscellaneousManagementFlag',
                            checked:Wtf.account.companyAccountPref.miscellaneousManagementFlag
                        }),this.ismemocomplsory=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowalertifMemoismissing"),
                            name:'memo',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.memo
                        }),this.DOSetting=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.SettoMakeDeliveryOrderinCustomerInvoicebydefault")+"'>"+WtfGlobal.getLocaleText("acc.field.CustomerInvoiceWithDeliveryOrder")+"</span>",
                            name:'DOSettings',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.DOSettings
                        }),this.GRSetting=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.SettoMakeGoodsReceiptinVendorInvoicebydefault")+"'>"+WtfGlobal.getLocaleText("acc.field.VendorInvoiceWithGoodReceipt")+"</span>",
                            name:'GRSettings',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.GRSettings
                        }),this.showprodserial=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Allowyoutoaddthelocationwarehousesbatchforproduct")+"'>"+WtfGlobal.getLocaleText("acc.field.AllowaddingBatchSerialNoforproduct")+"</span>",
                            name:'showprodserial',
                            hidden:true,
                            hideLabel:true,
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showprodserial
                        }),this.isAutoFillBatchDetails=new Wtf.form.Checkbox({ //option to check wether Batch Deatils are auto Fill or not
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isAutoFillBatchDetailsTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isAutoFillBatchDetails")+"</span>",
                            name:'isAutoFillBatchDetails',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isAutoFillBatchDetails
                        }),this.isLocationCompulsory=new Wtf.form.Checkbox({ //option to check wether Location is compulsory or not
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isLocationCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isLocationCompulsory")+"</span>",
                            name:'isLocationCompulsory',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isLocationCompulsory||Wtf.account.companyAccountPref.activateInventoryTab
                        }),this.isWarehouseCompulsory=new Wtf.form.Checkbox({ //option to check wether Warehouse is compulsory or not
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isWarehouseCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isWarehouseCompulsory")+"</span>",
                            name:'isWarehouseCompulsory',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isWarehouseCompulsory||Wtf.account.companyAccountPref.activateInventoryTab
                        }),this.isRowCompulsory=new Wtf.form.Checkbox({ //option to check wether Row is compulsory or not
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isRowCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isRowCompulsory")+"</span>",
                            name:'isRowCompulsory',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isRowCompulsory
                        }),this.isRackCompulsory=new Wtf.form.Checkbox({ //option to check wether Row is compulsory or not
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isRackCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isRackCompulsory")+"</span>",
                            name:'isRackCompulsory',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isRackCompulsory
                        }),this.isBinCompulsory=new Wtf.form.Checkbox({ //option to check wether Row is compulsory or not
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isBinCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isBinCompulsory")+"</span>",
                            name:'isBinCompulsory',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isBinCompulsory
                        }),this.isBatchCompulsory=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isBatchCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isBatchCompulsory")+"</span>",
                            name:'isBatchCompulsory',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isBatchCompulsory
                        }),this.restrictDuplicateBatch=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CompanyPref.RestrictDuplicateBatch")+"'>"+WtfGlobal.getLocaleText("acc.field.CompanyPref.RestrictDuplicateBatch")+"</span>",
                            name:'restrictDuplicateBatch',
                            autoWidth:true,
                            checked:CompanyPreferenceChecks.restrictDuplicateBatchCheck()
                        }),this.isSerialCompulsory=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isSerialCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isSerialCompulsory")+"</span>",
                            name:'isSerialCompulsory',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isSerialCompulsory
                        }),this.productComposition=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("erp.activateproductcomposition"), // "Activate Product Composition Functionality For Product",
                            labelStyle:'width: 250px;',
                            name:'activateProductComposition',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.activateProductComposition
                        }),this.productPriceinMultipleCurrency=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.ProductPriceInMultipleC"),
                            labelStyle:'width: 250px;',
                            name:'productPriceinMultipleCurrency',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.productPriceinMultipleCurrency
                        }),this.productPricingOnBands = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.activateProductPricingOnBandsToolTip") + "'>" + WtfGlobal.getLocaleText("acc.field.activateProductPricingOnBands") + "</span>", // "Activate Product Pricing On Bands",
                            labelStyle: 'width: 250px;',
                            name: 'productPricingOnBands',
                            autoWidth:true,
                            checked: Wtf.account.companyAccountPref.productPricingOnBands
                        }),this.bandsWithSpecialRateForPurchase = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.bandsWithSpecialRateForPurchase.label") + "'>" + WtfGlobal.getLocaleText("acc.bandsWithSpecialRateForPurchase.label") + "</span>", // "Activate Product Pricing List on Bands with Special Rate For Purchase",
                            labelStyle: 'width: 250px;',
                            name: Wtf.companyAccountPref_bandsWithSpecialRateForPurchase,
                            autoWidth: true,
                            checked: CompanyPreferenceChecks.bandsWithSpecialRateForPurchase(),
                            disabled: CompanyPreferenceChecks.bandsWithSpecialRateForPurchase() ? false : true
                        }),this.productPricingOnBandsForSales = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.activateProductPricingOnBandsForSalesToolTip") + "'>" + WtfGlobal.getLocaleText("acc.field.activateProductPricingOnBandsForSales") + "</span>", // "Activate Product Pricing On Bands",
                            labelStyle: 'width: 250px;',
                            name: 'productPricingOnBandsForSales',
                            autoWidth:true,
                            checked: Wtf.account.companyAccountPref.productPricingOnBandsForSales
                        }),this.bandsWithSpecialRateForSales = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.bandsWithSpecialRateForSales.label") + "'>" + WtfGlobal.getLocaleText("acc.bandsWithSpecialRateForSales.label") + "</span>", // "Activate Product Pricing List on Bands with Special Rate For Sales",
                            labelStyle: 'width: 250px;',
                            name: 'bandsWithSpecialRateForSales',
                            autoWidth: true,
                            checked: Wtf.account.companyAccountPref.bandsWithSpecialRateForSales,
                            disabled: Wtf.account.companyAccountPref.productPricingOnBandsForSales? false : true
                        }),this.isActivateIBG=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.activateIBG"),
                            hidden:Wtf.hide_ibg_company_preference_link || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA,
                            hideLabel:Wtf.hide_ibg_company_preference_link || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA,
                            name:'activateIBG',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.activateIBG
                        }),
                            /*
                             * ERP-29076
                             */
                            this.isActivateIBGCollection=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.activateIBGCollection"),
                            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.SINGAPORE,
                            hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.SINGAPORE,
                            name:'activateIBGCollection',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.activateIBGCollection,
                            listeners: {
                                'check': {
                                    fn: this.activateIBGCollectionHandler,
                                    scope: this
                                }
                            }
                        }),this.activateIBGCollectionDetails = new Wtf.form.FieldSet({
                            xtype: 'fieldset',
                            autoHeight: true,
                            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.activateIBGCollection.details") + "'>" + WtfGlobal.getLocaleText("acc.accPref.activateIBGCollection.details") + "</span>",
                            hidden: !Wtf.account.companyAccountPref.activateIBGCollection,
                            autoWidth: true,
                            items: [this.UOBEndToEndID = new Wtf.form.TextField({
                                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.endToEndId.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.endToEndId") + "* </span>",
                                    name: 'uobendtoendid',
                                    hiddenName: 'uobendtoendid',
                                    disabled: Wtf.account.companyAccountPref.countryid!=Wtf.Country.SINGAPORE,
                                    disabledClass: "newtripcmbss",
                                    id: "uobendtoendid" + this.id,
                                    maxLength: 35,
                                    scope: this,
                                    style:"width: 200px;"
                                }), this.UOBPurposeCode = new Wtf.form.TextField({
                                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.cimb.purposeCode.qtip") + "'>" + WtfGlobal.getLocaleText("acc.cimb.purposeCode") + "* </span>",
                                    name: 'uobpurposecode',
                                    hiddenName: 'uobpurposecode',
                                    disabled: Wtf.account.companyAccountPref.countryid!=Wtf.Country.SINGAPORE,
                                    disabledClass: "newtripcmbss",
                                    id: "uobpurposecode" + this.id,
                                    maxLength: 4,
                                    scope: this,
                                    style:"width: 200px;"
                                })]
                        }),this.isActivateSalesContractManagement=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.sales.contrcat.management.activate"),
                            //                            hidden:Wtf.hide_ibg_company_preference_link,
                            //                            hideLabel:Wtf.hide_ibg_company_preference_link,
                            name:'activateSalesContrcatManagement',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.activateSalesContrcatManagement
                        }),this.onlyBaseCurrency=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.showonlybasecurrency"),
                            labelStyle:'width: 250px;',
                            hidden:true,
                            hideLabel : true,
                            name:'onlyBaseCurrency',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.onlyBaseCurrency
                        }),this.versionsList=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("erp.field.ActivateVersonsList"), // "Activate Pick And Pack Functionality in DO Report",
                            labelStyle:'width: 250px;',
                            name:'versionslist',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.versionslist
                        }),this.versionsListPO=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("erp.field.ActivateVersoningInPO"),
                            labelStyle:'width: 250px;',
                            name:Wtf.companyAccountPref_activeVersioningInPurchaseOrder,
                            autoWidth:true,
                            checked:CompanyPreferenceChecks.activeVersioningInPurchaseOrder()
                        }),this.isSalesOrderCreatedForCustomer=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.SalesOrderIsCreatedForCustomerTooltip")+"'>"+WtfGlobal.getLocaleText("acc.field.SalesOrderIsCreatedForCustomer")+"</span>",
                            labelStyle:'width: 250px;',
                            name:'isSalesOrderCreatedForCustomer',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isSalesOrderCreatedForCustomer
                        }),this.isOutstandingInvoiceForCustomer=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.AlertifCustomerhasanyOutstandingInvoicewhilecreatingSalesOrder")+"'>"+WtfGlobal.getLocaleText("acc.field.AlertifCustomerhasanyOutstandingInvoice")+"</span>",
                            labelStyle:'width: 250px;',
                            name:'isOutstandingInvoiceForCustomer',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isOutstandingInvoiceForCustomer
                        }),this.isMinMaxOrdering=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.minMaxOrderingTooltip")+"'>"+WtfGlobal.getLocaleText("acc.field.minMaxOrdering")+"</span>",
                            labelStyle:'width: 250px;',
                            name:'isMinMaxOrdering',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.isMinMaxOrdering
                        }),this.blockPOcreationwithMinValue=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.BlockPOcreationwithminimumPOPricevalue")+"'>"+WtfGlobal.getLocaleText("acc.field.BlockPOcreationwithminimumPOPricevalue")+"</span>",
                            labelStyle:'width: 250px;',
                            name:'blockPOcreationwithMinValue',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.blockPOcreationwithMinValue
                        }),this.priceConfigurationAlert = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.activatePriceConfigurationAlertToolTip") + "'>" + WtfGlobal.getLocaleText("acc.field.activatePriceConfigurationAlert") + "</span>", // "Activate Price Configuration Alert",
                            labelStyle: 'width: 250px;',
                            name: 'priceConfigurationAlert',
                            autoWidth:true,
                            checked: Wtf.account.companyAccountPref.priceConfigurationAlert
                        }),this.retainExchangeRate = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.retainExchangeRateToolTip") + "'>" + WtfGlobal.getLocaleText("acc.field.retainExchangeRate") + "</span>", // "Retain Exchange Rate
                            labelStyle: 'width: 250px;',
                            name: 'retainExchangeRate',
                            autoWidth:true,
                            checked: Wtf.account.companyAccountPref.retainExchangeRate
                       }),this.isDuplicateItems = new Wtf.form.Checkbox({//Alert for duplicate product
                                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.Alertifduplicateitemintransaction.ttip") + "'>" + WtfGlobal.getLocaleText("acc.field.Alertifduplicateitemintransaction") + "</span>", // "Duplicate items in transaction
                                labelStyle: 'width: 250px;',
                                name: 'isDuplicateItems',
                                autoWidth:true,
                                checked:Wtf.account.companyAccountPref.isDuplicateItems
                        }),this.activateimportForJE=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.activateimportForJE"),
                            labelStyle: 'width: 250px;',
                            name:'activateimportForJE',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.activateimportForJE
                        }),this.activateToBlockSpotRate=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.ActivatetoblockspotrateTooltip") + "'>" + WtfGlobal.getLocaleText("acc.field.Activatetoblockspotrate") + "</span>",
                            labelStyle: 'width: 250px;',
                            name:'activateToBlockSpotRate',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.activateToBlockSpotRate
//                        }),this.activateCRblockingWithoutStock=new Wtf.form.Checkbox({
//                            fieldLabel:WtfGlobal.getLocaleText("acc.field.activateCRblockingWithoutStock"),
//                            labelStyle: 'width: 250px;',
//                            name:'activateCRblockingWithoutStock',
//                            checked:Wtf.account.companyAccountPref.activateCRblockingWithoutStock
                        }),this.hierarchicalDimensions=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.hierarchicalDimensionsTooltip") + "'>" + WtfGlobal.getLocaleText("acc.field.hierarchicalDimensions") + "</span>",
                            labelStyle: 'width: 250px;',
                            name:'hierarchicalDimensions',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.hierarchicalDimensions
                        }),this.autoPopulateDeliveredQuantity=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.autoPopulateDeliveredQuantitytooltip") + "'>" +WtfGlobal.getLocaleText("acc.field.autoPopulateDeliveredQuantity")+ "</span>",
                            labelStyle: 'width: 250px;',
                            name:'autoPopulateDeliveredQuantity',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.autoPopulateDeliveredQuantity
                        }),this.defaultTenplateLogo=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.compPref.DeafulttemplateLogoSettingTT") + "'>" +WtfGlobal.getLocaleText("acc.compPref.DeafulttemplateLogoSetting")+ "</span>",
                            labelStyle: 'width: 250px;',
                            name:'defaultTemplateLogoFlag',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.defaultTemplateLogoFlag
                        }),this.calculateProductWeightMeasurment = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.calculateproductWeightmeasurement") + "'>" +WtfGlobal.getLocaleText("acc.accPref.calculateproductWeightmeasurement")+ "</span>",
                            labelStyle: 'width: 250px;',
                            name: 'calculateproductweightmeasurment',
                            autoWidth: true,
                            checked: Wtf.account.companyAccountPref.calculateproductweightmeasurment
                        }),this.activateWastageCalculation = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.activateWastageCalculation") + "'>" +WtfGlobal.getLocaleText("acc.field.activateWastageCalculation")+ "</span>",
                            labelStyle: 'width: 250px;',
                            name: 'activateWastageCalculation',
                            autoWidth: true,
                            checked: Wtf.account.companyAccountPref.activateWastageCalculation
                        }),this.wastageDefaultAccount = new Wtf.form.ExtFnComboBox({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.wastageDefaultAccount"),
                            labelStyle: 'width: 300px;',
                            name: 'wastageDefaultAccount',
                            store: this.liabilityStore,
                            forceSelection: false,
                            allowBlank: true,
                            hiddenName: 'wastageDefaultAccount',
                            displayField: 'accountname',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            extraComparisionField:'acccode',
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            valueField: 'accountid',
                            emptyText: WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.noOfDaysforValidTillField=new Wtf.form.TextField({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.noofdaysforvalidtillfield"),
                            labelStyle:'width: 300px;',
                            name:'noOfDaysforValidTillField',
                            maxLength:10,
                            value:Wtf.account.companyAccountPref.noOfDaysforValidTillField==-1?"":Wtf.account.companyAccountPref.noOfDaysforValidTillField,
                            maskRe:/[0-9]/
                        }),this.isActivateLoanManagement=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.loan.management.activate"),
                            hidden:false,
                            hideLabel:false,
                            labelStyle: 'width: 250px;',
                            autoWidth: true,
                            name:'activateLoanManagementFlag',
                            checked:Wtf.account.companyAccountPref.activateLoanManagementFlag
                        }),this.LoanDisbursement= new Wtf.form.FnComboBox({
                            //      addNewFn:this.addNewAccount.createDelegate(this,[false,null,"fxcoaWin",true],true),
//                            hirarchical:true,
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.loanDisbursementTo"),  //'Loan Disbursement',
                            labelStyle: 'width: 300px;',
                            name:'loandisbursementaccount',
                            store:this.exStore,
                            hiddenName:'loandisbursementaccount',
                            displayField:'accountname',
                            valueField:'accountid',
                            forceSelection: false,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.LoanInterestAccount= new Wtf.form.FnComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.rp.loneInterestGoesTo"),  //'Loan Interest',
                            labelStyle: 'width: 300px;',
                            name:'loaninterestaccount',
                            store:this.exStore,
                            hiddenName:'loaninterestaccount',
                            displayField:'accountname',
                            valueField:'accountid',
                            forceSelection: false,
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),this.gstIncomeGroupCheck=new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypreferanceGstIncomeGroup.tooltip") + "'>" +WtfGlobal.getLocaleText("acc.companypreferanceGstIncomeGroup.lable")+ "</span>",
                            labelStyle: 'width: 300px;',
                            name:'gstIncomeGroup',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.gstIncomeGroup
//                        }),this.paymentMethodAsCard=new Wtf.form.Checkbox({   SDP-14125
//                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypreferancePaymentAsCard.tooltip") + "'>" +WtfGlobal.getLocaleText("acc.companypreferancePaymentAsCard.lable")+ "</span>",
//                            labelStyle: 'width: 300px;',
//                            name:'paymentMethodAsCard',
//                            autoWidth:true,
//                            checked:Wtf.account.companyAccountPref.paymentMethodAsCard
                        }),this.jobOrderItemFlow=new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypreferancejoborderitem.lable") + "'>" +WtfGlobal.getLocaleText("acc.companypreferancejoborderitem.lable")+ "</span>",
                            labelStyle: 'width: 300px;',
                            name:'jobOrderItemFlow',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.jobOrderItemFlow
                        }),this.usersVisibilityFlow=new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.user.usersVisibilityFlow") + "'>" +WtfGlobal.getLocaleText("acc.user.usersVisibilityFlow")+ "</span>",
                            labelStyle: 'width: 300px;',
                            name:'usersVisibilityFlow',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.usersVisibilityFlow
                        }),this.usersspecificinfoFlow=new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.user.dimensionDatavisibilitybasedonusergroup") + "'>" +WtfGlobal.getLocaleText("acc.user.dimensionDatavisibilitybasedonusergroup")+ "</span>",
                            labelStyle: 'width: 300px;',
                            name:'usersspecificinfoFlow',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.usersspecificinfoFlow

                        }),this.jobWorkOutFlow=new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.user.jobWorkOutFlow") + "'>" +WtfGlobal.getLocaleText("acc.user.jobWorkOutFlow")+ "</span>",
                            labelStyle: 'width: 300px;',
                            name:'jobWorkOutFlow',
                            id:'jobWorkOutFlow'+this.id,
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.jobWorkOutFlow
                        }),this.vendorJobOrderStore = new Wtf.form.ComboBox({
                            triggerAction:"all",
                            mode:"local",
                            typeAhead:true,
                            forceSelection:true,
                            store:this.storeList,
                            displayField:"fullname",
                            valueField:"store_id",
                            fieldLabel:WtfGlobal.getLocaleText("acc.JobWorkOut.vendorjobworkstore"),
                            hiddenName:"vendorjoborderstore",
                            width:150,
                            parent:this,
                            id: 'vendorjoborderstore' + this.id,
                            name: 'vendorjoborderstore',
                            emptyText:WtfGlobal.getLocaleText("acc.JobWorkOut.vendorjobworkstore"),
                            disabled : !(Wtf.account.companyAccountPref.jobWorkOutFlow),
                            allowBlank: !(Wtf.account.companyAccountPref.jobWorkOutFlow)
                        }),
                        //CheckBox for Enabling Cash Received and Returned fields in Sales Invoice
                        this.enableCashReceiveReturn=new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypref.enableCashReceiveReturn.ttip") + "'>" +WtfGlobal.getLocaleText("acc.companypref.enableCashReceiveReturn")+ "</span>",
                            labelStyle: 'width: 300px;',
                            name:'enableCashReceiveReturn',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.enableCashReceiveReturn
                        }),
                        //CheckBox for Enabling to apply discount on payment terms
                        this.discountOnPaymentTerms = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypref.enablediscountonpaymenttermqtip") + "'>" + WtfGlobal.getLocaleText("acc.companypref.enablediscountonpaymentterm") + "</span>",
                            labelStyle: 'width: 300px;',
                            name: Wtf.companyAccountPref_discountOnPaymentTerms,
                            autoWidth: true,
                            checked: CompanyPreferenceChecks.discountOnPaymentTerms(),
                            scope: this,
                            listeners: {
                                scope: this,
                                'change': function (obj, newval, oldval) {
                                            if (!newval) {
                                                Wtf.Ajax.requestEx({
                                                    url: "ACCReceiptNew/checkTransactionsForDiscountOnPaymentTerms.do"
                                                }, this, function (response) {
                                                    if (response.msg != "" && response.msg != null && response.msg != undefined) {
                                                        Wtf.MessageBox.show({
                                                            title: WtfGlobal.getLocaleText("acc.common.information"),
                                                            width: 500,
                                                            msg: response.msg,
                                                            buttons: Wtf.MessageBox.OK,
                                                            scope: this,
                                                            icon: Wtf.MessageBox.WARNING
                                                        });
                                                        obj.setValue(oldval);
                                                    } else {
                                                        obj.setValue(newval);
                                                    }
                                                }, function (response) {

                                                });
                                            } else if (newval && !oldval) {
                                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.companypref.enablediscountonpaymenttermerrormsg"), function (btn) {
                                                    if (btn == "yes") {
                                                        obj.setValue(newval);
                                                    } else {
                                                        obj.setValue(oldval);
                                                    }
                                                }, this);
                                            } else {
                                                obj.setValue(newval);
                                            }
                                }
                            }
                        }),this.duplicateCustomerPoReferenceNo=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.customPoReferenceNoToolTip") + "'>" +WtfGlobal.getLocaleText("acc.field.customPoReferenceNo")+ "</span>",
                            labelStyle:'width: 250px;',
                            name:Wtf.companyAccountPref_customerPoReferenceNo,
                            autoWidth:true,
                             checked: CompanyPreferenceChecks.duplicateCustomerPoReferenceNo()
                        }),  
                        //ERP-41133
                        this.undeliveredServiceSO=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companyPreferences.undeliveredServiceSOOPen") + "'>" +WtfGlobal.getLocaleText("acc.companyPreferences.undeliveredServiceSOOPen")+ "</span>",
                            labelStyle: 'width: 300px;',
                            name:'undeliveredServiceSOOpen',
                            autoWidth:true,
                            checked:CompanyPreferenceChecks.undeliveredServiceSOOpen()
                        })
//                        //CheckBox for Enabling discount in bulk payment
//                        this.discountInBulkPayment = new Wtf.form.Checkbox({
//                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypref.enablediscountinbulkpaymentqtip") + "'>" + WtfGlobal.getLocaleText("acc.companypref.enablediscountinbulkpayment") + "</span>",
//                            labelStyle: 'width: 300px;',
//                            name: Wtf.companyAccountPref_discountInBulkPayment,
//                            autoWidth: true,
//                            checked: CompanyPreferenceChecks.discountInBulkPayment(),
//                            scope: this
//                        })
                        /*
                         * Below code is commented temporary as per discussion with paritosh sir after the clearances it will be uncommented.
                         */
//                        ,this.postingDate=new Wtf.form.Checkbox({
//                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypref.enablepostingDatetooltip") + "'>" +WtfGlobal.getLocaleText("acc.companypref.enablepostingDate")+ "</span>",
//                            labelStyle: 'width: 300px;',
//                            name:Wtf.companyAccountPref_isPostingDateCheck,
//                            autoWidth:true,
//                            checked:CompanyPreferenceChecks.isPostingDateCheck()
//                        })
                    ]
                    },
                    this.activateDD={
                        id:this.id+'activateMailSettingsforDD',
                        xtype: 'fieldset',
                        title: WtfGlobal.getLocaleText("acc.field.sendDDtemplate.email"),
                        checkboxToggle: true,
                        scope:this,
                        collapsed: !(Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink),
                        checkboxName: 'activateMailSettingsforDD',
                        listeners:{
                            scope:this,
                            beforeexpand: function(){
                                if( Wtf.account.companyAccountPref.activateDDInsertTemplateLink ||Wtf.account.companyAccountPref.activateDDTemplateFlow){
                                   if(Wtf.account.companyAccountPref.activateDDTemplateFlow){
                                        this.activateDDTemplateFlow.setValue(true);
                                    }
                                    if(Wtf.account.companyAccountPref.activateDDInsertTemplateLink){
                                        this.activateDDInsertTemplateLink.setValue(true);
                                    }
                                }else{
                                    this.activateDDInsertTemplateLink.setValue(true);
                                }
                            },
                             beforecollapse: function(){
                                    this.activateDDInsertTemplateLink.setValue(false);
                                    this.activateDDTemplateFlow.setValue(false);
                            }
                        },
                        items:[this.activateDDTemplateFlow= new Wtf.form.Checkbox({//Change mail Template
                            name:'activateDDTemplateFlow',
                            id:this.id+'activateDDTemplateFlow',
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendDDtemplate.inlineemail")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.sendDDtemplate.email.help")),
                            checked :Wtf.account.companyAccountPref.activateDDTemplateFlow,
                            scope:this,
                            labelStyle:'width: 340px;',
                            cls:'checkboxtopPosition',
                            autoWidth:true
                        }),
                        this.activateDDInsertTemplateLink= new Wtf.form.Checkbox({                               //Change mail Template
                            name:'activateDDInsertTemplateLink',
                            id:this.id+'activateDDInsertTemplateLink',
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendDDtemplate.hyperlinkemail"),
                            checked :Wtf.account.companyAccountPref.activateDDInsertTemplateLink,
                            scope:this,
                            labelStyle:'width: 340px;',
                            cls:'checkboxtopPosition',
                            autoWidth:true
                        })]
                    },
                      {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.FinancialReportSettins")+"'>"+WtfGlobal.getLocaleText("acc.field.FinancialReportSettins")+"</span>",  //Financial Report Settings
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        },
                        items:[this.stockValuationFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Showstockvaluationinfinancialreport"),
                            labelStyle:'width: 250px;',
                            name:'stockValuationFlag',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.stockValuationFlag
                        }),this.showAllAccountInGL=new Wtf.form.Checkbox({
                                fieldLabel:WtfGlobal.getLocaleText("acc.field.showAllAccountInGL"),
                                labelStyle:'width: 280px;',
                                name:'showAllAccountInGl',
                                autoWidth:true,
                                checked:Wtf.account.companyAccountPref.showAllAccountInGl
                        }),this.showAllAccountsInPnl=new Wtf.form.Checkbox({
                                fieldLabel:WtfGlobal.getLocaleText("acc.field.showallaccountsinpnl"),
                                labelStyle:'width: 280px;',
                                name:'showAllAccountsInPnl',
                                autoWidth:true,
                                checked:Wtf.account.companyAccountPref.showAllAccountsInPnl
                        }),this.showAllAccount=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowallaccountsinTrailBalances"),
                            labelStyle:'width: 250px;',
                            name:'showAllAccount',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showAllAccount
                        }),this.showallaccountsinbs=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowallaccountsinBalanceSheet"),
                            labelStyle:'width: 250px;',
                            name:'showallaccountsinbs',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showallaccountsinbs
                        }),this.showChildAccountsInGl=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowChildAccountsinGl"),
                            labelStyle:'width: 250px;',
                            name:'showChildAccountsInGl',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showChildAccountsInGl
                        }),this.showChildAccountsInPnl=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowChildAccountsinPnl"),
                            labelStyle:'width: 250px;',
                            name:'showChildAccountsInPnl',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showChildAccountsInPnl
                        }),this.showAllAccount=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowChildAccountsinTB"),
                            labelStyle:'width: 250px;',
                            name:'showChildAccountsInTb',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showChildAccountsInTb
                        }),this.showChildAccountsInBS=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowChildAccountsinBS"),
                            labelStyle:'width: 250px;',
                            name:'showChildAccountsInBS',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showChildAccountsInBS
                        }),this.downloadglprocessflag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.downloadGLprocess.chboxtitle"),
                            labelStyle:'width: 250px;',
                            name:'downloadGLprocess',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.downloadglprocessflag
                        }),this.downloadDimPLprocessflag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.downloadDimPLprocess.chboxtitle"),
                            labelStyle:'width: 250px;',
                            name:'downloadDimPLprocess',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.downloadDimPLprocess
                        }),this.downloadSOAprocessflag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.downloadSOAprocess.chboxtitle"),
                            labelStyle:'width: 250px;',
                            name:'downloadSOAprocess',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.downloadSOAprocess
                        }),this.showZeroAmountAsBlank=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.ShowzeroamountasblankToolTip") + "'>" +WtfGlobal.getLocaleText("acc.field.Showzeroamountasblank")+ "</span>",
                            labelStyle:'width: 250px;',
                            name:'showzeroamountasblank',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showZeroAmountAsBlank
                        }),this.showAccountCodeInFinancialReport=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.showaccountcodeinfinancialreportToolTip") + "'>" +WtfGlobal.getLocaleText("acc.field.showaccountcodeinfinancialreport")+ "</span>",
                            labelStyle:'width: 250px;',
                            name:'showaccountcodeinfinancialreport',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.showaccountcodeinfinancialreport
                        }),this.activateBankReconcilitaionDraft=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.activatebankdraft") + "'>" +WtfGlobal.getLocaleText("acc.field.activatebankdraft")+ "</span>",
                            labelStyle : 'width: 250px;',
                            name : 'activateBankReconcilitaionDraft',
                            autoWidth : true,
                            checked : Wtf.account.companyAccountPref.columnPref.activateBankReconcilitaionDraft
                        })
                    ]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.accConsignmentStockSetting")+"'>"+WtfGlobal.getLocaleText("acc.accPref.accConsignmentStockSetting")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                         //   validator:this.validateFormat
                            },
                        items:[this.consignmentSalesManagementFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.ConsignmentStock.salesmanagement"),
                            labelStyle:'width: 250px;',
                            name:'consignmentSalesManagementFlag',
                            autoWidth:true,
                            hidden:Wtf.defaultReferralKeyflag,
                            hideLabel:Wtf.defaultReferralKeyflag,
                            checked:Wtf.account.companyAccountPref.consignmentSalesManagementFlag
                        }),this.consignmentPurchaseManagementFlag=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.on.ConsignmentStock.purchasemanagement"),
                            labelStyle:'width: 250px;',
                            name:'consignmentPurchaseManagementFlag',
                            hidden:Wtf.defaultReferralKeyflag,
                            hideLabel:Wtf.defaultReferralKeyflag,
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.consignmentPurchaseManagementFlag
                        }),this.activateCRblockingWithoutStock=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.activateCRblockingWithoutStock"),
                            labelStyle: 'width: 250px;',
                            name:'activateCRblockingWithoutStock',
                            autoWidth:true,
                            hidden:Wtf.defaultReferralKeyflag,
                            hideLabel:Wtf.defaultReferralKeyflag,
                            checked:Wtf.account.companyAccountPref.activateCRblockingWithoutStock
                        }),this.activatefromdateToDate=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.activatefromdateToDate"),
                            labelStyle: 'width: 250px;',
                            name:'activatefromdateToDate',
                            hidden:Wtf.defaultReferralKeyflag,
                            hideLabel:Wtf.defaultReferralKeyflag,
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.activatefromdateToDate
                         }),
                            this.requestApprovalFlow=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.activateRequestApprovalFlow"),  //Activate QA Approval Flow
                            labelStyle: 'width: 250px;',
                            autoWidth:true,
                            hidden:Wtf.defaultReferralKeyflag,
                            hideLabel:Wtf.defaultReferralKeyflag,
                            name:'requestApprovalFlow',
                            checked:Wtf.account.companyAccountPref.requestApprovalFlow
                         }),
                            this.closedStatus=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.closeconsignmentdeliveryorder"),//"Close Consignment Delivery Order",  //Activate QA Approval Flow
                            name:'closedStatusforDo',
                            autoWidth:true,
                            labelStyle: 'width: 250px;',
                            hidden:Wtf.defaultReferralKeyflag,
                            hideLabel:Wtf.defaultReferralKeyflag,
                            checked:Wtf.account.companyAccountPref.closedStatusforDo
                        }),
                            this.isMovementWarehouseFlow=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.MovementWarehouseMapping"),
                            labelStyle: 'width: 250px;',
                            name:'isMovementWarehouseFlow',
                            disabled:(!Wtf.account.companyAccountPref.activateInventoryTab)?true:false,
                            autoWidth:true,
                            hidden:Wtf.defaultReferralKeyflag,
                            hideLabel:Wtf.defaultReferralKeyflag,
                            checked:Wtf.account.companyAccountPref.isMovementWarehouseMapping
                            }),
                            this.defaultWarehouse
                          ]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.QAApprovalFlow")+"'>"+WtfGlobal.getLocaleText("acc.field.QAApprovalFlow")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50
//                            validator:this.validateFormat
                            },
                                    items: [
                                        this.qaApprovalFlow = new Wtf.form.Checkbox({
                                            fieldLabel: WtfGlobal.getLocaleText("acc.companypreferences.activateQAInspectionFlow"), //Activate QA Approval Flow
                                            name: 'activateQAApprovalFlow',
                                            labelStyle:'width: 340px;',
                                            autoWidth: true,
                                            anchor: '70%',
                                            checked: Wtf.account.companyAccountPref.activateQAApprovalFlow,
                                            listeners: {
                                                scope: this,
                                                check: function() {
                                                    if (this.qaApprovalFlow.getValue()) {
                                                        if (this.inspectionStoreCombo != undefined && this.repairStoreCombo != undefined) {
                                                            this.inspectionStoreCombo.setDisabled(false);
                                                            this.repairStoreCombo.setDisabled(false);
                                                            this.scrapStoreCombo.setDisabled(false);
                                                            this.inspectionStoreCombo.allowBlank = false;
                                                            this.repairStoreCombo.allowBlank = false;
                                                            this.scrapStoreCombo.allowBlank = false;
                                                            if (this.integrationWithInventory.checked || Wtf.account.companyAccountPref.activateInventoryTab) {
                                                                this.stockINQAApproval.enable();
                                                                this.interStoreReturnQAApproval.enable();
                                                                this.stockRequestReturnQAApproval.enable();
                                                                this.isQaApprovalFlowInDO.enable();
                                                                this.isQaApprovalFlowInMRP.enable();
                                                                this.isQaApprovalFlow.enable();
                                                            }
                                                        }
                                                    } else {
                                                        this.checkbeforeQAStoreChange(Wtf.account.companyAccountPref.activateQAApprovalFlow, Wtf.account.companyAccountPref.inspectionStore);
                                                        if (this.inspectionStoreCombo != undefined && this.repairStoreCombo != undefined) {
                                                            this.inspectionStoreCombo.clearValue();
                                                            this.repairStoreCombo.clearValue();
                                                            this.scrapStoreCombo.clearValue();
                                                            this.inspectionStoreCombo.setDisabled(true);
                                                            this.repairStoreCombo.setDisabled(true);
                                                            this.scrapStoreCombo.setDisabled(true);
                                                            this.inspectionStoreCombo.allowBlank = true;
                                                            this.repairStoreCombo.allowBlank = true;
                                                            this.scrapStoreCombo.allowBlank = true;

                                                            this.stockINQAApproval.setValue(false);
                                                            this.stockINQAApproval.disable();
                                                            this.interStoreReturnQAApproval.setValue(false);
                                                            this.interStoreReturnQAApproval.disable();
                                                            this.stockRequestReturnQAApproval.setValue(false);
                                                            this.stockRequestReturnQAApproval.disable();
                                                            this.isQaApprovalFlowInDO.setValue(false);
                                                            this.isQaApprovalFlowInDO.disable();
                                                            this.isQaApprovalFlowInMRP.setValue(false);
                                                            this.isQaApprovalFlowInMRP.disable();
                                                            this.isQaApprovalFlow.setValue(false);
                                                            this.isQaApprovalFlow.disable();
                                                        }
                                                    }
                                                }
                                            }
                                        }),
                                         this.buildAssemblyQACheckbox = new Wtf.form.Checkbox({
                                         fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypref.settings.qaindeliveryorderactivation") + "'>" + WtfGlobal.getLocaleText("acc.companypref.settings.qaindeliveryorderactivation") + "</span>", // "Activate Delivery Planner",
                                         labelStyle: 'width: 250px;',
                                         name: 'BuildAssemblyApprovalFlow',
                                         autoWidth: true,
                                         checked: Wtf.account.companyAccountPref.BuildAssemblyApprovalFlow
                                     }),
                                        this.stockINQAApproval,
                                        this.interStoreReturnQAApproval,
                                        this.stockRequestReturnQAApproval, this.isQaApprovalFlow, this.isQaApprovalFlowInDO, this.isQaApprovalFlowInMRP,
                                        this.inspectionStoreCombo,
                                        this.repairStoreCombo,
                                        this.scrapStoreCombo]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.accShiptoBilltoTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.accShiptoBillto")+"</span>",
                        id:'addrSettings',
                        items:[this.isAddressFromVendorMaster,this.remitTextPanel ,this.remitPaymentTo,this.companyAddressButton]
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.UserwiseAmendpricepermissions")+"'>"+WtfGlobal.getLocaleText("acc.field.UserwiseAmendpricepermissions")+"</span>",
                        items:[this.AmendPriceBtn = new Wtf.Toolbar.Button({
                            text: WtfGlobal.getLocaleText("acc.field.SetPermissions"),
                            tooltip: WtfGlobal.getLocaleText("acc.field.SetPermissions"),
                            disabled: false,
                            scope: this,
                            handler: function() {
                                loadAdminPage(1);

                            }
                        })
                        ]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.controllAccountsSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.controllAccountsSettings")+"</span>",
                        items:[this.controlAccountSetting = new Wtf.Toolbar.Button({
                            text: WtfGlobal.getLocaleText("acc.field.MapAccountstoControl"),
                            tooltip: WtfGlobal.getLocaleText("acc.field.SelectAccountstomap"),
                            scope: this,
                            handler:this.controlAccountSettingHandler
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ManualJEPostSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.ManualJEPostSettings")+"</span>",
                        items:[this.manualJePostSetting = new Wtf.Toolbar.Button({
                            text: WtfGlobal.getLocaleText("acc.field.selectcatopostJE"),
                            tooltip: WtfGlobal.getLocaleText("acc.field.selectcatopostJEOrOtherTransactions"),
                            scope: this,
                            handler:this.manualJePostSettingHandler
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ClicktobellowlinkstoHide/ShowTransactionFormsFields")+"'>"+WtfGlobal.getLocaleText("acc.field.Hide/ShowTransactionFormsFields")+"</span>",
                        items:this.hideshowlinkArr
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CurrencyRates")+"'>"+WtfGlobal.getLocaleText("acc.field.CurrencyRates")+"</span>",
                        items:[this.downloadCurrencyExchangeratesLink]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.import.currencysetting")+"'>"+WtfGlobal.getLocaleText("acc.import.currencysetting")+"</span>" ,
                        items:[this.currencycode=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.cimb.currencyCode"),  //currency code
                            name:'isCurrencyCode',
                            id:'currencycode',
                            checked:Wtf.account.companyAccountPref.isCurrencyCode
                        }),this.currencyname=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.import.currencyname"),  //currencyname,
                            name:'isCurrencyCode',
                            id:'currencyname',
                            checked:!Wtf.account.companyAccountPref.isCurrencyCode
                        })]
                    },{
                        xtype: 'fieldset',
                        autoHeight: true,
                        title: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.deliveryPlannerSetting")+"'>"+WtfGlobal.getLocaleText("acc.field.deliveryPlannerSetting")+"</span>" ,
                        items: [
                            this.deliveryPlanner = new Wtf.form.Checkbox({
                                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.activateDeliveryPlannerTooltip") + "'>" + WtfGlobal.getLocaleText("acc.field.activateDeliveryPlanner") + "</span>", // "Activate Delivery Planner",
                                labelStyle: 'width: 250px;',
                                name: 'deliveryPlanner',
                                autoWidth: true,
                                checked: Wtf.account.companyAccountPref.deliveryPlanner,
                                listeners: {
                                    change: function(fieldset, deliveryPlanner) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.dashboard.change.deliveryPlanner")]);
                                    }
                                }
                            }), this.autoPopulateFieldsForDeliveryPlanner = new Wtf.form.Checkbox({
                                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.autoPopulateFieldsForDeliveryPlannerMsg") + "'>" + WtfGlobal.getLocaleText("acc.field.autoPopulateFieldsForDeliveryPlannerMsg") + "</span>",
                                labelStyle: 'width: 250px;',
                                name: 'autoPopulateFieldsForDeliveryPlanner',
                                autoWidth: true,
                                checked: Wtf.account.companyAccountPref.autoPopulateFieldsForDeliveryPlanner
                            })
                        ]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CustomizeCurrencySymbolCodeSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.CustomizeCurrencySymbolCodeSettings")+"</span>",
                        items:[this.customizeCurrencySymbolCodeLink]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.JournalEntryRevaluationDimensionSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.JournalEntryRevaluationDimensionSettings")+"</span>",
                        items:[this.JournalEntryRevaluationDimensionLink]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.MobileFieldSetupSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.MobileFieldSetupSettings")+"</span>",
                        items:[this.mobileFieldSetupGrid,
                            this.allowCustomerCheckInCheckOut = new Wtf.form.Checkbox({
                                fieldLabel: WtfGlobal.getLocaleText("acc.field.CheckInOut"),
                                labelStyle: 'width: 250px;',
                                name: 'allowCustomerCheckInCheckOut',
                                autoWidth: true,
                                checked: Wtf.account.companyAccountPref.allowCustomerCheckInCheckOut
                            })]
                    },{
                        /**
                         * Fieldset for Negative value setting
                         */
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.NegativeValueSetting")+"'>"+WtfGlobal.getLocaleText("acc.field.NegativeValueSetting")+"</span>",
                        items:[{
                                border:false,
                                xtype:'panel',
                                id:'negativeValueSettingTipId',
                                html:'<font color="#555555">'+ WtfGlobal.getLocaleText("acc.field.NegativeValueSetting.tip") +'</font>'
                            },
                            // Combo for options to show negative value
                            this.shownegativevaluein=new Wtf.form.ComboBox({
                                fieldLabel:WtfGlobal.getLocaleText("acc.common.shownegativevaluein"),
                                store:this.negativeValueInStore,
                                name:'negativeValueIn',
                                id:'negativeValueIn'+this.id,
                                anchor:'85%',
                                hiddenName:'negativeValueIn',
                                valueField:'id',
                                mode:'local',
                                value:Wtf.account.companyAccountPref.negativeValueIn?Wtf.account.companyAccountPref.negativeValueIn:2,
                                displayField:'value',
                                forceSelection: true,
                                triggerAction: 'all',
                                selectOnFocus:true,
                                listeners:{
                                    select: function(component, record, newVal){
                                        if(record.data.id === 1){
                                            Wtf.getCmp('negativeValueBracketsTipId').hide();
                                            Wtf.getCmp('negativeValueMinusSymbolTipId').show();
                                        } else if(record.data.id === 2){
                                            Wtf.getCmp('negativeValueMinusSymbolTipId').hide();
                                            Wtf.getCmp('negativeValueBracketsTipId').show();
                                        }
                                    }
                                }
                            }),
                            // Example tip for minus symbol option selected
                            {
                                border:false,
                                xtype:'panel',
                                id:'negativeValueMinusSymbolTipId',
                                bodyStyle:'padding: 0 0 0 205px',
                                hidden:Wtf.account.companyAccountPref.negativeValueIn === 1 ? false : true,
                                html:'<font color="#555555">'+ WtfGlobal.getLocaleText("acc.field.shownegativevaluein.minusSymbol.exampleMsg") +'</font>'
                            },
                            // Example tip for brackets option selected
                            {
                                border:false,
                                xtype:'panel',
                                id:'negativeValueBracketsTipId',
                                bodyStyle:'padding: 0 0 0 205px',
                                hidden:Wtf.account.companyAccountPref.negativeValueIn === 2 ? false : true,
                                html:'<font color="#555555">'+ WtfGlobal.getLocaleText("acc.field.shownegativevaluein.brackets.exampleMsg") +'</font>'
                            }]
                        }
//                        ,{
//                        xtype: 'fieldset',
//                        autoHeight: true,
//                        id:'buildassemblyqasettingsid',
//                        title: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypref.settings.qaofbuildassemblyproduct.jobqork")+"'>"+WtfGlobal.getLocaleText("acc.companypref.settings.qaofbuildassemblyproduct.jobqork")+"</span>" ,
//                        items: [
//                            this.buildAssemblyQACheckbox = new Wtf.form.Checkbox({
//                                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypref.settings.qaindeliveryorderactivation") + "'>" + WtfGlobal.getLocaleText("acc.companypref.settings.qaindeliveryorderactivation") + "</span>", // "Activate Delivery Planner",
//                                labelStyle: 'width: 250px;',
//                                name: 'BuildAssemblyApprovalFlow',
//                                autoWidth: true,
//                                checked: Wtf.account.companyAccountPref.BuildAssemblyApprovalFlow
//                            })
//
//                        ]
//                      }
                      ,{
                        xtype: 'fieldset',
                        autoHeight: true,
                        title: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ReportLoadSettingonDocumentSave")+"'>"+WtfGlobal.getLocaleText("acc.field.ReportLoadSettingonDocumentSave")+"</span>" ,
                        items:[this.isAutoRefershReportonSave = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.AutoLoadReportonDocumentSave") + "'>" + WtfGlobal.getLocaleText("acc.field.AutoLoadReportonDocumentSave") + "</span>", // "Auto Load Report on Document Save",
                            labelStyle: 'width: 250px;',
                            name: 'autorefrshreportonsave',
                            checked: Wtf.isAutoRefershReportonDocumentSave,
                            autoWidth: true
                        })]
                    },{
                        xtype: 'fieldset',
                        autoHeight: true,
                        title: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.comp.preferences.stockadjustment.price.settings")+"'>"+WtfGlobal.getLocaleText("acc.comp.preferences.stockadjustment.price.settings")+"</span>" ,
                        items: [this.updateStockAdjustmentPrice = new Wtf.form.Checkbox({
                                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.comp.preferences.update.stockadjustment.out.price.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.comp.preferences.update.stockadjustment.out.price") + "</span>",
                                labelStyle: 'width: 250px;',
                                name: 'updateStockAdjustmentPrice',
                                checked: Wtf.account.companyAccountPref.updateStockAdjustmentPrice,
                                autoWidth: true
                            })]
                    },{
                        xtype: 'fieldset',
                        autoHeight: true,
                        title: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.accPref.fieldset.marginButtonSettings")+"'>"+WtfGlobal.getLocaleText("acc.accPref.fieldset.marginButtonSettings")+"</span>" ,
                        items: [
                            this.activateProfitMarginField=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.TT.activateProfitMargin") + "'>" + WtfGlobal.getLocaleText("acc.field.activateProfitMargin") + "</span>",
                            labelStyle: 'width: 250px;',
//                            hidden:true,
//                            hideLabel:true,
                            name:'activateProfitMargin',
                            autoWidth:true,
                            checked:Wtf.account.companyAccountPref.activateProfitMargin
                        }), this.isShowMarginButton = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.TT.isShowMarginButton") + "'>" + WtfGlobal.getLocaleText("acc.accPref.isShowMarginButton") + "</span>", // Show or Hide Margin Button in Invoice/Sales/Quotation create form //ERM-76
                            name: 'isShowMarginButton',
                            labelStyle: 'width: 250px;',
                            autoWidth:true,
                            checked: Wtf.account.companyAccountPref.isShowMarginButton
                        })]
                    },{
                          /*---Company preferences check to map tax at product level----  */
                                    xtype: 'fieldset',
                                    autoHeight: true,
                                    title: 'To Map Taxes At Product Level',
                                    hidden: Wtf.account.companyAccountPref.countryid != Wtf.CountryID.MALAYSIA,
                                    defaults: {
                                        anchor: '80%'
                                    },
                                    items: [this.mapTaxesAtProductLevel = new Wtf.form.Checkbox({
                                            boxLabel: "<span wtf:qtip='" + 'To Map Taxes At Product Level' + "'>" + 'Map taxes at product level' + "</span>",
                                            hideLabel: true,
                                            name: 'mapTaxesAtProductLevel',
                                            checked: CompanyPreferenceChecks.mapTaxesAtProductLevel(),
                                            id: 'mapTaxesAtProductLevel',
                                               listeners:{
                                                scope:this,
                                                'change': {
                                                    fn: this.validateCheckUncheckMapTaxesAtProductLevel,
                                                    scope: this
                                                }
                                            }
                                        })]
                    },{

                                     /*---Company preferences check to "Send documents to next level while Editing"----  */
                                    xtype: 'fieldset',
                                    autoHeight: true,
                                    title: WtfGlobal.getLocaleText("acc.accPref.sendApprovalDocumentsToNextLevel"),
                                    defaults: {
                                        anchor: '80%'
                                    },
                                    items: [this.sendPendingDocumentsToNextLevel = new Wtf.form.Checkbox({
                                            boxLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.TT.sendApprovalDocumentsToNextLevel") + "'>" + WtfGlobal.getLocaleText("acc.accPref.sendApprovalDocumentsToNextLevel") + "</span>",
                                            hideLabel: true,
                                            name: 'sendPendingDocumentsToNextLevel',
                                            checked: CompanyPreferenceChecks.sendPendingDocumentsToNextLevel(),
                                            id: 'sendPendingDocumentsToNextLevel'

                                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.LimitedAccounts.ttip")+"'>"+WtfGlobal.getLocaleText("acc.field.LimitedAccounts")+"</span>",
                        items:[this.limitedAccounts]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.invoiceterm.autoLoadInvoiceTermTax.ttip")+"'>"+WtfGlobal.getLocaleText("acc.invoiceterm.forInvoiceTermTaxes")+"</span>",
                        items:[this.autoLoadInvoiceTermTaxes = new Wtf.form.Checkbox({
                            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceterm.autoLoadInvoiceTermTax"),
                            labelStyle: 'width: 250px;',
                            name: 'autoLoadInvoiceTermTax',
                            id: 'autoLoadInvoiceTermTax',
                            checked: CompanyPreferenceChecks.autoLoadInvoiceTermTaxes(),
                            autoWidth:true
                        })]
                    },{
                        /*
                         * ERM -735- Map Default method to customer check in company prference
                         */
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.defaultPaymentSelection.ttip")+"'>"+WtfGlobal.getLocaleText("acc.field.defaultPaymentSelection")+"</span>",
                        items:[this.mapDefaultPaymentToCustomer=new Wtf.form.Checkbox({
                           fieldLabel: WtfGlobal.getLocaleText("acc.field.mapDefaultPaymentToCustomer"),
                           labelStyle: 'width: 250px;',
                           autoWidth:true,
                           name:'mapDefaultPaymentToCustomer',
                           checked: CompanyPreferenceChecks.mapDefaultPaymentMethod(),
                           id: 'mapDefaultPaymentToCustomer'

                        })]
                    }, {
                                    xtype: 'fieldset',
                                    autoHeight: true,
                                    defaults: {
                                        anchor: '80%'
                                    },
                                    hidden: Wtf.account.companyAccountPref.countryid != Wtf.CountryID.INDIA,
                                    title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.landedcost.customduty.configuration.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.landedcost.customduty.configuration") + "</span>",
                                    items: [this.customDutyAccount = new Wtf.form.ExtFnComboBox({
//                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.disGivenGoesTo"),  //'Discount Given goes to',
                                            fieldLabel: WtfGlobal.getLocaleText("acc.landedcost.customduty.account"), // Custom Duty Account
                                            name: 'customdutyaccount',
                                            store: this.customDutyStore,
                                            hiddenName: 'customdutyaccount',
                                            displayField: 'accountname',
                                            valueField: 'accountid',
                                            extraComparisionField: 'acccode', // type ahead search on acccode as well.
                                            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
                                            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
                                            //       value:Wtf.account.companyAccountPref.discountgiven,
                                            emptyText: WtfGlobal.getLocaleText("acc.accPref.emptyText")
                                        }), this.IGSTAccount = new Wtf.form.ExtFnComboBox({
                                            hirarchical: true,
//                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.disReceivedGoesTo"),  //'Discount Received goes to',
                                            fieldLabel: WtfGlobal.getLocaleText("acc.landedcost.customduty.igstaccount"), // IGST Account
                                            name: 'igstaccount',
                                            store: this.customDutyStore,
                                            hiddenName: 'igstaccount',
                                            displayField: 'accountname',
                                            valueField: 'accountid',
                                            extraComparisionField: 'acccode', // type ahead search on acccode as well.
                                            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
                                            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
                                            emptyText: WtfGlobal.getLocaleText("acc.accPref.emptyText")
                                        })]
                                }]
                },{
                    columnWidth:.49,
                    layout:'form',
                    items:[{
                        //                        xtype:'fieldset',
                        //                        autoHeight:true,
                        //                        title:'Email Settings',
                        //                        id:'emailSettings'+this.helpmodeid,
                        //                        defaults:{xtype:'checkbox',anchor:'80%',maxLength:50,validator:this.validateFormat},
                        //                        items:[this.sendInvMail=new Wtf.form.Checkbox({
                        //                            fieldLabel:'Send email notification on invoice creation',
                        //                            name:'emailinvoice',
                        //                            checked:Wtf.account.companyAccountPref.emailinvoice
                        //                        })]
                        //                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.accPref.invSettings"),  //'Inventory Settings',
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.invSettingsTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.invSettings")+"</span>",
                        defaults:{
//                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[
                        this.integrationWithInventory=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.inventory.activateInventoryIntegration"),  //Inventory Integartion
                            name:'withInventory',
                            anchor:'80%',
                            checked:Wtf.account.companyAccountPref.activateInventoryTab
                        }),
                                        this.activatemrpmodule = new Wtf.form.Checkbox({
                                            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.activatemrpmodule"), //"Activate MRP Module",
                                            checked: Wtf.account.companyAccountPref.activateMRPManagementFlag,
                                            name: 'activatemrpmodule',
                                            hidden:true,
                                            hideLabel:true,
                                            anchor:'80%',
                                            id: 'activatemrpmodule',
                                            listeners:{
                                                scope:this,
                                                'change': {
                                                    fn: this.checkTransactionsForManufacturing,
                                                    scope: this
                                                }
                                            }
                                        }),
                        this.inventoryCycleCount=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.inventory.activateCycleCount"),  //Cycle Count
                            name:'withcyclecount',
                            anchor:'80%',
                            disabled: !Wtf.account.companyAccountPref.activateInventoryTab,
                            checked:Wtf.account.companyAccountPref.activateCycleCount
                        }),
                        this.withInv=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.withoutInv"),  //'Transactions without inventory',
                            name:'withoutinventory',
                            disabled : true,
                            anchor:'80%',
                            checked:this.val
                        }), this.withInvUpdate=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.IncludeTradingFlow"),  //Update inventory on DO
                            //hidden : this.val,
                            //hideLabel : this.val,
                            anchor:'80%',
                            disabled : true,
                            name:'withinvupdate',
                            checked:this.val? false : Wtf.account.companyAccountPref.withinvupdate
                        }), {
                                xtype: 'fieldset',
                                autoHeight: true,
                                title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypreferences.inventory.valuation.method") + "'>" + WtfGlobal.getLocaleText("acc.companypreferences.inventory.valuation.method") + "</span>",
                                defaults: {
                                    anchor: '80%',
                                    maxLength: 50,
                                    validator: this.validateFormat
                                },
                                items: [this.periodicInventory = new Wtf.form.Radio({
                                    fieldLabel: WtfGlobal.getLocaleText("acc.companypreferences.periodic.inventory"),
                                    name: 'inventoryvaluationtype',
                                    disabled:Wtf.account.companyAccountPref.activateMRPManagementFlag,
                                    checked: Wtf.account.companyAccountPref.inventoryValuationType == "0",
                                    id: 'periodicInventory'
                                }),
                                this.perpetualInventory = new Wtf.form.Radio({
                                    fieldLabel: WtfGlobal.getLocaleText("acc.companypreferences.perpetual.inventory"),
                                    name: 'inventoryvaluationtype',
                                    disabled:Wtf.account.companyAccountPref.activateMRPManagementFlag,
                                    checked: Wtf.account.companyAccountPref.inventoryValuationType == "1",
                                    id: 'perpetualInventory'
                                }),
                                this.PeriodicJE = new Wtf.form.Checkbox({
                                    fieldLabel: WtfGlobal.getLocaleText("acc.companypreferences.periodic.postJE"),
                                    name: 'jeforperiodic',
                                    disabled:Wtf.account.companyAccountPref.inventoryValuationType == "1",
                                    checked: CompanyPreferenceChecks.PeriodicJE(),
                                    id: 'periodicje'
                                }),
                                this.deductSOBlockedQtyFromValuation = new Wtf.form.Checkbox({
                                    fieldLabel: WtfGlobal.getLocaleText("Deduct Valuation for Block Sales Order for Periodic Inventory"),
                                    name: 'deductsoblockedqtyfromvaluation',
                                    disabled:Wtf.account.companyAccountPref.inventoryValuationType == "1",
                                    checked: CompanyPreferenceChecks.deductSOBlockedQtyFromValuation(),
                                    id: 'deductsoblockedqtyfromstockvaluation'
                                }),
                                {xtype: 'fieldset',
                                                    autoHeight: true,
                                                    title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypreferences.inventory.valuation.perpetualsetting") + "'>" + WtfGlobal.getLocaleText("acc.companypreferences.inventory.valuation.perpetualsetting") + "</span>",
                                                    autoWidth: true,
                                                    id:'perpetualsettingfieldset',
                                                    defaults: {
                                                        anchor: '80%',
                                                        maxLength: 50
                                                    },
                                                    hidden: !this.perpetualInventory.checked,
                                                    items: [this.cogsAcc = new Wtf.form.ExtFnComboBox({
                                                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.cogs.account") + "'>" + WtfGlobal.getLocaleText("acc.field.cogs.account") + "</span>", // Cost of Goods Sold Account
                                                            store: this.AccsStore,
                                                            anchor: '70%',
                                                            name: 'cogsaccountid',
                                                            id: 'cogsaccountid' + this.id,
                                                            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
                                                            extraComparisionField: 'acccode', // type ahead search on acccode as well.
                                                            typeAheadDelay: 30000,
                                                            typeAhead: true,
                                                            isAccountCombo: true,
//                                                            hideLabel: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == Wtf.PERPETUAL_VALUATION_METHOD),
//                                                            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == Wtf.PERPETUAL_VALUATION_METHOD),
                                                            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 240,
                                                            hiddenName: 'cogsaccountid',
                                                            valueField: 'accid',
                                                            forceSelection: true,
                                                            displayField: 'accname',
                                                            allowBlank: true
                                                        }), this.stockAdjustmentAcc = new Wtf.form.ExtFnComboBox({
                                                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.stock.account") + "\b'>" + WtfGlobal.getLocaleText("acc.field.stock.account") + "\b</span>", // Stock Adjustment Account
                                                            store: this.AccsStore,
                                                            anchor: '70%',
                                                            name: 'stockadjustmentaccountid',
                                                            id: 'stockadjustmentaccountid' + this.id,
                                                            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
                                                            extraComparisionField: 'acccode', // type ahead search on acccode as well.
                                                            typeAheadDelay: 30000,
                                                            typeAhead: true,
                                                            isAccountCombo: true,
//                                                            hideLabel: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == Wtf.PERPETUAL_VALUATION_METHOD),
//                                                            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == Wtf.PERPETUAL_VALUATION_METHOD),
                                                            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 240,
                                                            hiddenName: 'stockadjustmentaccountid',
                                                            valueField: 'accid',
                                                            forceSelection: true,
                                                            displayField: 'accname',
                                                            allowBlank: true
                                                        }),this.inventoryAcc = new Wtf.form.ExtFnComboBox({
                                                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.inventory.account") + "'>" + WtfGlobal.getLocaleText("acc.field.inventory.account") + "</span>", // Inventory Account
                                                            store: this.AccsStore,
                                                            anchor: '70%',
                                                            name: 'inventoryaccountid',
                                                            id: 'inventoryaccountid' + this.id,
                                                            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
                                                            extraComparisionField: 'acccode', // type ahead search on acccode as well.
                                                            typeAheadDelay: 30000,
                                                            typeAhead: true,
                                                            isAccountCombo: true,
//                                                            hideLabel: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == Wtf.PERPETUAL_VALUATION_METHOD),
//                                                            hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == Wtf.PERPETUAL_VALUATION_METHOD),
                                                            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 240,
                                                            hiddenName: 'inventoryaccountid',
                                                            valueField: 'accid',
                                                            forceSelection: true,
                                                            displayField: 'accname',
                                                            allowBlank: true
                                                        })]
                                                }]
                            }]
//                    },{
//                        xtype:'fieldset',
//                        autoHeight:true,
//                        title:"QA Setting",  //'QA Settings',
////                        title:"<span wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.accPref.invSettings")+"</span>",
//                        defaults:{
//                            anchor:'80%',
//                            maxLength:50,
//                            validator:this.validateFormat
//                            },
//                        items:[
//                        this.qaApprovalFlow=new Wtf.form.Checkbox({
//                            fieldLabel:"Activate QA Inspection Flow",  //Activate QA Approval Flow
//                            name:'activateQAApprovalFlow',
//                            checked:Wtf.account.companyAccountPref.activateQAApprovalFlow
//                        })]
//                    },
//                    {
//                        xtype:'fieldset',
//                        autoHeight:true,
//                        title:"Consignment Setting",  //'QA Settings',
////                        title:"<span wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.accPref.invSettings")+"</span>",
//                        defaults:{
//                            anchor:'80%',
//                            maxLength:50,
//                            validator:this.validateFormat
//                            },
//                        items:[
//                        this.closedStatus=new Wtf.form.Checkbox({
//                            fieldLabel:WtfGlobal.getLocaleText("acc.field.closeconsignmentdeliveryorder"),//"Close Consignment Delivery Order",  //Activate QA Approval Flow
//                            name:'closedStatusforDo',
//                            checked:Wtf.account.companyAccountPref.closedStatusforDo
//                        })]
                   },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.integrationSettings.ttip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.integrationSettings")+"</span>",//'Integration Settings',
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        },
                        items:[this.activateCRMIntegration=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.activateCRMIntegration"),  //'Activate CRM Integration',
                            name:'activateCRMIntegration',
                            disabled:Wtf.account.companyAccountPref.standalone,
                            checked:Wtf.account.companyAccountPref.activateCRMIntegration
                        }),this.activateLMSIntegration=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.activateLMSIntegration"),  //'Activate LMS Integration',
                            name:'activateLMSIntegration',
                            disabled:Wtf.account.companyAccountPref.standalone,
                            checked:Wtf.account.companyAccountPref.isLMSIntegration
                        }),this.activateGroupCompanyIntegration=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.activateGroupCompanyIntegration"),  //'Multi Group Activation
                            name:'activateGroupCompanies',
                            checked:Wtf.account.companyAccountPref.activateGroupCompaniesFlag
                        }),this.integrationWithPOS=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.isPOSIntegration"),  //'Transactions without inventory',
                            name:'withPOS',
                            checked:Wtf.account.companyAccountPref.integrationWithPOS
                        }),
                        this.customerForPOS=new Wtf.form.ComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.walk-inCustomer"),  //Update inventory on DO
                            store:this.selectedCustomerStore,
                            anchor:'85%',
                            valueField:'accid',
                            displayField: 'accname',//Wtf.account.companyAccountPref.customerForPOS,
                            disabled : true,
                            emptyText:WtfGlobal.getLocaleText("acc.inv.cus"), //'Select a '+this.businessPerson+'...',
                            forceSelection: true,
                            triggerAction:'all' ,
                             hidden:true,
                            hideLabel:true
                        }),
                        this.vendorForPOS=new Wtf.form.ComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.cashoutVendor"),  //Update inventory on DO
                            store:this.selectedVendorStore,
                            anchor:'85%',
                            valueField:'accid',
                            displayField: 'accname',//Wtf.account.companyAccountPref.vendoForPOS,
                            disabled : true,
                            emptyText:WtfGlobal.getLocaleText("acc.inv.cus"), //'Select a '+this.businessPerson+'...',
                            forceSelection: true,
                            triggerAction:'all'                       ,
                            hidden:true,
                            hideLabel:true
                        }),
                        this.isCloseRegisterMultipleTimes=new Wtf.form.Checkbox({                   // Allow to user open register multiple times in POS APP
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.isCloseRegisterMultipleTimes"),
                            name:'isCloseRegisterMultipleTimes',
                            checked:Wtf.account.companyAccountPref.isCloseRegisterMultipleTimes,
                             hidden:true,
                            hideLabel:true
                       })
//                       this.CreditAccountforPOS= new Wtf.form.ComboBox({                          //payment Method for Cashout from POS
//                            fieldLabel:WtfGlobal.getLocaleText("acc.mp.payMethod"),
//                            name:'cashoutaccountforpos',
//                            store:this.crStore,
//                            hiddenName:'cashoutaccount',
//                            valueField:'methodid',
//                            displayField:'methodname',
//                            mode: 'local',
////                            extraComparisionField:'methodname',// type ahead search on acccode as well.
////                            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode']:[],
////                            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
//                            //       value:Wtf.account.companyAccountPref.discountgiven,
//                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
//                        })
                    ]
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
//                        title:WtfGlobal.getLocaleText("acc.accPref.invtypeSettings"),  //'Inventory Settings',
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.invtypeSettingsTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.invtypeSettings")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.inventoryschema=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.inventoryschema"),  //'Transactions without inventory',
                            name:'UomSchemaType',
                            id:'uomschema',
                            //                            disabled : true,
                            checked:Wtf.account.companyAccountPref.UomSchemaType=='0'
                        }),this.uomConversionEditCheck=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.edituomconversion"),
                            name:'edituomconversion',
                            id:'edituomconversion',
                            disabled:Wtf.account.companyAccountPref.UomSchemaType=='0' ? false : true,
                            checked:Wtf.account.companyAccountPref.UomSchemaType=='0' ? Wtf.account.companyAccountPref.isBaseUOMRateEdit : false
                         }), this.differentUOM = new Wtf.form.Checkbox({            //added company preference check to enable Variable conversion rate ERM-319
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypref.variableConversionRateQtip") + "'>" + WtfGlobal.getLocaleText("acc.companypref.variableConversionRate") + "</span>",
                            name: Wtf.companyAccountPref_differentUOM,
                            checked: CompanyPreferenceChecks.differentUOM(),
                            scope: this
                        }),
                        this.inventorypackaging=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.inventorypackaging"),  //Update inventory on DO
                            //                            disabled : true,
                            //                            hidden:true,
                            //                            hideLabel:true,
                            name:'UomSchemaType',
                            id:'uompackaging',
                            checked:Wtf.account.companyAccountPref.UomSchemaType=='1'
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypreferences.salespersonagentflow.enabledisable")+"'>"+WtfGlobal.getLocaleText("acc.companypreferences.salespersonagentflow.enabledisable")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.enablesalespersonAgentFlow=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.salespersonagentflow.enabledisable"),
                            name:'salesperosnagent',
                            id:'salesperosnagent',
                            checked:Wtf.account.companyAccountPref.enablesalespersonAgentFlow
                        }),this.viewallexcludecustomerwithoutsalesperson=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypref.salesperson.viewallorexcludecustomerwithoutsalesperson")+WtfGlobal.addLabelHelp("<ul style='list-style-type:disc; margin-left:10px;'><li>When this option is checked , those customer who have salesperson mapping with current user are displayed in customer dropdown while creating document on document creation form(sales side) e.g Sales Invoice, Sales Order.</li><li> Basically, When this option is checked we are excluding customers who have no salesperson mapping with current user.</li> <li>When this option is checked and user has view all permission , all the customer who have salesperson mapping with any user will be displayed to user in customer dropdown. </li><li> This option do affect only on customers to be displayed in customer dropdown while creating documents.</li></ul>"),
                            name:'viewallexcludecustomerwithoutsalesperson',
                            id:'viewallexcludecustomerwithoutsalesperson',
                            checked:Wtf.account.companyAccountPref.viewallexcludecustomerwithoutsalesperson
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypreferences.MakePRmandatory")+"'>"+WtfGlobal.getLocaleText("acc.companypreferences.MakePRmandatory")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.mandatoryPR=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.MakePRmandatory"),
                            name:'isprmandatory',
                            id:'isprmandatory',
                            checked:Wtf.account.companyAccountPref.isPRmandatory
                         })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypreferences.enabledefaultsqlenceformatforrecurringinvoice")+"'>"+WtfGlobal.getLocaleText("acc.companypreferences.enabledefaultsqlenceformatforrecurringinvoice")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                        },
                        items:[this.enableDefaultsqlformatForRecurInvoice=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.enabledefaultsqlenceformatforrecurringinvoice.checkboxname"),
                            name:'usedefaultseqformat',
                            id:'usedefaultseqformat',
                            checked:Wtf.account.companyAccountPref.defaultsequenceformatforrecinv
                        }),
                        this.pickAddressFromMaster=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.recurringdocumentsetting.pickAddressFromMaster"),
                            name:'pickaddressfrommaster',
                            id:'pickaddressfrommaster',
                            checked:Wtf.account.companyAccountPref.pickaddressfrommaster
                        }),{
                            xtype:'fieldset',
                            autoHeight:true,
                            title:WtfGlobal.getLocaleText("acc.accPref.recurringSalesInvoiceMemoSetting"),
                            defaults:{
                                anchor:'80%',
                                maxLength:50,
                                validator:this.validateFormat
                                },
                            items:[this.recuringSalesInvoiceOrignalMemo=new Wtf.form.Radio({
                                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.asofMemoqtip") + "'>" +WtfGlobal.getLocaleText("acc.accPref.asofMemo")+ "</span>",
                                name:Wtf.companyAccountPref_recuringSalesInvoiceMemo,
                                checked:CompanyPreferenceChecks.recuringSalesInvoiceMemo()==Wtf.RECURRINGINVOICESMEMO.DATEDON
                            }),this.recuringSalesInvoiceAsOfMemo = new Wtf.form.Radio({
                                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.orignalSalesInvoiceMemoQtip") + "'>" +WtfGlobal.getLocaleText("acc.accPref.orignalSalesInvoiceMemo")+ "</span>",
                                name: Wtf.companyAccountPref_recuringSalesInvoiceMemo,
                                checked: CompanyPreferenceChecks.recuringSalesInvoiceMemo()==Wtf.RECURRINGINVOICESMEMO.ORIGANALINVOICE
                            }),this.recuringSalesInvoiceOrignalAsOfMemo=new Wtf.form.Radio({
                                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.orignalSalesInvoiceMemoAsOfQtip") + "'>" +WtfGlobal.getLocaleText("acc.accPref.orignalSalesInvoiceMemoAsOf")+ "</span>",
                                name: Wtf.companyAccountPref_recuringSalesInvoiceMemo,
                                checked : CompanyPreferenceChecks.recuringSalesInvoiceMemo()==Wtf.RECURRINGINVOICESMEMO.DATEDONORIGANALINVOICE
                            })]
                        }]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.dashboardviewsetting"),//Dashboard View Setting,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.dashboardviewsetting.tip")+"'>"+WtfGlobal.getLocaleText("acc.dashboardviewsetting")+"</span>",// You can choose a Dashboard view as Flow Diagram View or Widget View
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.viewFlowDiagram=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.flowdiagramview"),  //Flow Diagram View
                            name:'dashboardView',
                            checked :Wtf.account.companyAccountPref.viewDashboard=='0',
                            id:'flowDiagramView',
                            listeners: {
                                change: function(fieldset,flowDiagramView){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.dashboard.change")]);
                                }
                            }
                        }),this.viewWidget=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.WidgetView"),  //Widget View
                            name:'dashboardView',
                            checked :Wtf.account.companyAccountPref.viewDashboard=='1',
                            id:'widgetView',
                            listeners: {
                                change: function(fieldset,flowDiagramView){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.dashboard.change")]);
                                }
                            }
//                        }),this.viewGraphicalDashboard = new Wtf.form.Radio({
//                            fieldLabel:"Graphical View",  //Widget View
//                            name:'dashboardView',
//                            checked :Wtf.account.companyAccountPref.viewDashboard=='2',
//                            id:'graphView',
//                            listeners: {
//                                change: function(fieldset,flowDiagramView){
//                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.dashboard.change")]);
//                                }
//                            }
                        })]
                       },{
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden : true,
                        title: "Theme Settings",//Dashboard View Setting,
                        defaults:{
                            anchor:'80%',
                            maxLength:50
//                            validator:this.validateFormat
                            },
                        items:[this.themeSelector = new Wtf.ux.ThemeCombo({
                            width : 150,
                            value : Wtf.theme
                        })]
                       },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.TransactionSettings"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CompanyTransactionSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.TransactionSettings")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.editTransaction=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.AllowEditingTransactions"),
                            name:'editTransaction',
                            checked:Wtf.account.companyAccountPref.editTransaction
                        }),this.deleteTransaction=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.AllowDeletingTransactions"),
                            name:'deleteTransaction',
                            checked:Wtf.account.companyAccountPref.deleteTransaction
                        }),this.editLinkedTransactionQuantity=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.LockEditingQuantity"),
                            name:'editLinkedTransactionQuantity',
                            checked:Wtf.account.companyAccountPref.editLinkedTransactionQuantity
                        }),this.editLinkedTransactionPrice=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.LockEditingPrice"),
                            name:'editLinkedTransactionPrice',
                            checked:Wtf.account.companyAccountPref.editLinkedTransactionPrice
                        }),this.shipDateConfiguration=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.shipDateConfiguration"), // "Due Date in VI depends upon Ship Date",
                            name:'shipDateConfiguration',
                            checked:Wtf.account.companyAccountPref.shipDateConfiguration
                        }),this.unitPriceInDO=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.unitPriceInDO"), // "Display 'Unit Price' in DO
                            name:'unitPriceInDO',
                            checked:Wtf.account.companyAccountPref.unitPriceInDO
                        }),this.unitPriceInGR=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.unitPriceInGR"), // "Display 'Unit Price' in GR
                            name:'unitPriceInGR',
                            checked:Wtf.account.companyAccountPref.unitPriceInGR
                        }),this.unitPriceInSR=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.unitPriceInSR"), // "Display 'Unit Price' in SR
                            name:'unitPriceInSR',
                            checked:Wtf.account.companyAccountPref.unitPriceInSR
                        }),this.unitPriceInPR=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.unitPriceInPR"), // "Display 'Unit Price' in PR
                            name:'unitPriceInPR',
                            checked:Wtf.account.companyAccountPref.unitPriceInPR
                        }),this.manyCreditDebit=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.manyCreditdebit"), // "Display 'Unit Price' in GR/DO/PR/SR",
                            name:'manyCreditDebit',
                            checked:Wtf.account.companyAccountPref.manyCreditDebit
                        }),this.openPOandSO=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.openPOandSO"), // "Show open PO and SO information",
                            name:'openPOandSO',
                            checked:Wtf.account.companyAccountPref.openPOandSO
                        }),this.showAddressonPOSOSave=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.showAddressonPOSOSave"), // "Show open PO and SO information",
                            name:'showAddressonPOSOSave',
                            checked:Wtf.account.companyAccountPref.showAddressonPOSOSave
                        }),this.isCustShipAddressInPurchase = new Wtf.form.Checkbox({
                             fieldLabel: WtfGlobal.getLocaleText("acc.field.showCustShippingAddreInPurchase"), // Show customer shipping address in cross link case
                              name: 'isCustShipAddressInPurchase',
                             checked: Wtf.account.companyAccountPref.isCustShipAddressInPurchase
                         }),this.isAutoSaveAndPrintChkBox=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.isAutoSaveAndPrintChkBox"), // Auto Save and Print for Cash Invoice/Credit Invoice //SDP-6826
                            name:'isAutoSaveAndPrintChkBox',
                            checked:Wtf.account.companyAccountPref.isAutoSaveAndPrintChkBox
                        }),this.isDisplayUOM=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.showDisplayUOM"), // Auto Save and Print for Cash Invoice/Credit Invoice //SDP-6826
                            name: Wtf.companyAccountPref_displayuom,
                            checked:CompanyPreferenceChecks.displayUOMCheck()
                        }),this.advanceSearchInDocumentlinking=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.advanceSearchInDocumentlinking"), // "Allow advance search for document linking",
                            name: Wtf.companyAccountPref_advanceSearchInDocumentlinking,
                            checked:CompanyPreferenceChecks.advanceSearchInDocumentlinking()
                        })]
                        }, {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.accPref.productSetting"),  //'Product  Setting',
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.productSetting")+"'>"+WtfGlobal.getLocaleText("acc.accPref.productSetting")+"</span>",
                        defaults:{
//                            anchor:'80%',
//                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.autoPopulateMappedProduct=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.autoPopulateMappedProduct"),
                            name:'autoPopulateMappedProduct',
                            anchor: '80%',
                            maxLength: 50,
                            checked:Wtf.account.companyAccountPref.autoPopulateMappedProduct
                        }),/*
                            *Checkbox for Enable Import Assembly Product Without BOM
                            */
                            this.withoutBOM=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.enableImportAssemblyProductWithoutBOM"),
                            name: Wtf.companyAccountPref_withoutBOM,
                            anchor: '80%',
                            maxLength: 50,
                            checked : CompanyPreferenceChecks.withoutBOMCheck()
                        }),
                        this.isFilterProductByCustomerCategory=new Wtf.form.Checkbox({ //option to filter product by customer category
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isFilterProductByCustomerCategoryTTip")+"'>"+WtfGlobal.getLocaleText("acc.field.isFilterProductByCustomerCategory")+"</span>",
                            name:'isFilterProductByCustomerCategory',
                            anchor: '80%',
                            maxLength: 50,
                            checked:Wtf.account.companyAccountPref.isFilterProductByCustomerCategory
                        }),{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.zerountipriceforproductqtip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.zerountipriceforproduct")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.AllowZeroUnitPrice=new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.accPref.allowzerountipriceforsalesandpurchasetransactions") + "'>" +WtfGlobal.getLocaleText("acc.accPref.allowzerountipriceforproduct")+ "</span>",
                            name:'aloowzeroprice',
                            id:'allowzeroprice',
                            checked:Wtf.account.companyAccountPref.allowZeroUntiPriceForProduct
                        }),this.AllowZeroUnitPriceInLeaseModule=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzerountipriceforproductinleasemodule"),
                            name:Wtf.companyAccountPref_allowZeroUntiPriceInLeaseModule, 
                            checked: CompanyPreferenceChecks.allowZeroUntiPriceInLeaseModule()
                        }),this.carryForwardPriceForCrossLinking = new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.compPref.allowCarryForwardUnitPriceForCrossLinking.label") + "'>" +WtfGlobal.getLocaleText("acc.compPref.allowCarryForwardUnitPriceForCrossLinking.label")+ "</span>",
                            name: 'carryForwardPriceForCrossLinking',
                            checked: Wtf.account.companyAccountPref.carryForwardPriceForCrossLinking
                        }),this.discountMaster=new Wtf.form.Checkbox({
                            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.discountMasterqtip") + "'>" + WtfGlobal.getLocaleText("acc.field.discountMaster") + "</span>",
                            name: Wtf.companyAccountPref_discountMaster,
                            checked : CompanyPreferenceChecks.discountMaster()
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.zeroqtyforproductqtip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproduct")+"</span>",
                        items:[new Wtf.Toolbar.Button({
                                text: WtfGlobal.getLocaleText("acc.accPref.allowzeroqty"),
                                tooltip:WtfGlobal.getLocaleText("acc.invoicelist.youcanmaketransactionwithzeroquantity"),
                                scope: this,
                                handler:this.AllowZeroQuantityWindow
                            })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.accPref.productselection"),  //'Product Selection Setting',
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.productselectiontoolTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.productselection")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.allautoload=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.productselectionAll"),  //'Show all Products',
                            name:'ProductSelectionType',
                            id:'allautoload',
                            //                            disabled : true,
                            checked:Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Show_all_Products,
                            listeners: {
                                    scope:this,
                                    change: function(){
                                        this.enableDisablePagingCheckbox();
                                    }
                                }
                            }), this.ontypeahead=new Wtf.form.Radio({
                                fieldLabel:WtfGlobal.getLocaleText("acc.accPref.productselectionOnType"),  //Show Products on type ahed
                                //                            disabled : true,
                                //                            hidden:true,
                                //                            hideLabel:true,
                                name:'ProductSelectionType',
                                id:'ontypeahead',
                                checked:Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Products_on_type_ahead,
                                listeners: {
                                    scope:this,
                                    change: function(){
                                        this.enableDisablePagingCheckbox();
                                    }
                                }
                            }),
                            this.onSubmit=new Wtf.form.Radio({
                                fieldLabel:WtfGlobal.getLocaleText("acc.accPref.productselectionOnsubmit"),  //Show on submit
                                name:'ProductSelectionType',
                                id:'onsubmit',
                                checked:Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Products_on_Submit,
                                listeners: {
                                    scope:this,
                                    change: function(){
                                        this.enableDisablePagingCheckbox();
                                    }
                                }
                            }),
                            this.allowProductPaging=new Wtf.form.Checkbox({
                                fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.extracompanypref.reports")+"'>"+WtfGlobal.getLocaleText("acc.common.extracompanypref.productpaging")+"</span>",
                                name:'allowProductPaging',
                                id:'allowProductPagingEditing',
                                disabled:Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Products_on_type_ahead?false:true,
                                checked:CompanyPreferenceChecks.productPagingCheck()
                            })
                    ]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title: WtfGlobal.getLocaleText("acc.accPref.productSortingSettingLabel"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.productSortingSettingLabel")+"'>"+WtfGlobal.getLocaleText("acc.accPref.productSortingSettingLabel")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                        },
                        items:[this.productSortByName=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.productSortByName"),
                            name:'productSortingSettingControl',
                            checked :Wtf.account.companyAccountPref.productsortingflag==Wtf.ProductSortByName,
                            id:'productSortByNameCase'
                        }),this.productSortById=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.productSortById"),
                            name:'productSortingSettingControl',
                            checked :Wtf.account.companyAccountPref.productsortingflag==Wtf.ProductSortById,
                            id:'productSortByIdCase'
                        })]

                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title: WtfGlobal.getLocaleText("acc.accPref.productSearchingSettingLabel"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.productSearchingSettingLabel")+"'>"+WtfGlobal.getLocaleText("acc.accPref.productSearchingSettingLabel")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                        },
                        items:[this.productSearchByStartWith=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.productSearchByStartWith"),
                            name:'productSearchingSettingControl',
                            checked :Wtf.account.companyAccountPref.productsearchingflag==Wtf.productSearchByStartWith,
                            id:'productSearchByStartWithCase'
                        }),this.productSearchByAnywhere=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.productSearchByAnywhere"),
                            name:'productSearchingSettingControl',
                            checked :Wtf.account.companyAccountPref.productsearchingflag==Wtf.productSearchByAnywhere,
                            id:'productSearchByAnywhereCase'
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title: WtfGlobal.getLocaleText("acc.accPref.richtextboxlabel"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.richtextboxlabel")+"'>"+WtfGlobal.getLocaleText("acc.accPref.richtextboxlabel")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                        },
                        items:[this.productDescInTextAreaCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.prodDescriptionInTextArea"),
                            name:'productDescSettingControl',
                            checked :Wtf.account.companyAccountPref.proddiscripritchtextboxflag==Wtf.ProductDescInTextArea,
                            id:'productDescInTextAreaCase'
                        }),this.productDescInTextBoxCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.proddiscipritchtextbox"),
                            name:'productDescSettingControl',
                            checked :Wtf.account.companyAccountPref.proddiscripritchtextboxflag==Wtf.ProductDescInTextBox,
                            id:'productDescInTextBoxCase'
                        }),this.productDescInHtmlEditorCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.prodDescriptionInHtmlEditor"),
                            name:'productDescSettingControl',
                            checked :Wtf.account.companyAccountPref.proddiscripritchtextboxflag==Wtf.ProductDescInHtmlEditor,
                            id:'productDescInHtmlEditorCase'
                        })]

                    }]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypreferences.customerVendorSetting.ttip")+"'>"+WtfGlobal.getLocaleText("acc.companypreferences.customerVendorSetting")+"</span>",
                        defaults:{anchor:'80%',maxLength:50,validator:this.validateFormat},
                        items:[ this.ontypeahead_CustVen=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.custvenloadtype"),
                            name:'custvenloadtype',
                            id:'custvenloadtype',
                            checked:Wtf.account.companyAccountPref.custvenloadtype
                        }),
                            this.allowCustVenCodeEditing=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowcustvenedit"),
                            name:'allowCustVenCodeEditing',
                            id:'allowcustvenCodeediting',
                            checked:Wtf.account.companyAccountPref.allowCustVenCodeEditing
                        })
//                        , this.allowCustomerVendorPaging=new Wtf.form.Checkbox({
//                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.extracompanypref.reports")+"'>"+WtfGlobal.getLocaleText("acc.common.extracompanypref.customervendorpaging")+"</span>",
//                            name:'allowCustomerVendorPaging',
//                            id:'allowCustomerVendorPagingEditing',
////                            hidden:false,
//                            checked:CompanyPreferenceChecks.customerVendorPagingCheck()
//                        })
                    ]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.CustomerVendorSortingSettingLabel")+"'>"+WtfGlobal.getLocaleText("acc.accPref.CustomerVendorSortingSettingLabel")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                        },
                        items:[this.CustomerVendorSortByName=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.CustomerVendorSortByName"),
                            name:'CustomerVendorSortingSettingControl',
                            checked :Wtf.account.companyAccountPref.customervendorsortingflag==Wtf.CustomerVendorSortByName,
                            id:'CustomerVendorSortByNameCase'
                        }),this.CustomerVendorSortByCode=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.CustomerVendorSortByCode"),
                            name:'CustomerVendorSortingSettingControl',
                            checked :Wtf.account.companyAccountPref.customervendorsortingflag==Wtf.CustomerVendorSortByCode,
                            id:'CustomerVendorSortByCodeCase'
                        })]

                    },
                      /*this code is for Productcombo description
                       * assgin 0 whhen  type is added inthe combo
                       * assign 1 when description is add in the combo 
                       * Defualt value is 0 */
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                            title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.ProductComboDropdownSettingLabel")+"'>"+WtfGlobal.getLocaleText("acc.accPref.ProductComboDropdownSettingLabel")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                        },
                        items:[this.Producttype=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.ProductIncludeType"),
                            name:'ProductComboDropdownSettingLabel',
                            checked :CompanyPreferenceChecks. productComboDisplay()==Wtf.AccountProducttype ,
                            id:'productcomboincludetype'
                        }),this.Productdescription=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.ProductIncludeDescription"),
                            name:'ProductComboDropdownSettingLabel',
                            checked :CompanyPreferenceChecks. productComboDisplay()==Wtf.AccountProcutdescription,
                            id:'productcomboincludedescription'
                        })]
                   
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.AccountSortingSettingLabel")+"'>"+WtfGlobal.getLocaleText("acc.accPref.AccountSortingSettingLabel")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                        },
                        items:[this.AccountSortByName=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.AccountSortByName"),
                            name:'AccountSortingSettingControl',
                            checked :Wtf.account.companyAccountPref.accountsortingflag==Wtf.CustomerVendorSortByName,
                            id:'AccountSortByNameCase'
                        }),this.AccountSortByCode=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.AccountSortByCode"),
                            name:'AccountSortingSettingControl',
                            checked :Wtf.account.companyAccountPref.accountsortingflag==Wtf.CustomerVendorSortByCode,
                            id:'AccountSortByCodeCase'
                        })]

                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.accPref.salestypeselection"),  //'Sales Type Selection Setting',
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.salestypeselectionToolTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.salestypeselection")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.cashSales=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCS"),  //'Cash Sales',
                            name:'SalesSelectionType',
                            id:'cashsales',
                            checked:Wtf.account.companyAccountPref.salesTypeFlag==true
                        }), this.creditSales=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.CreditSales"),  //Credit Sales
                            name:'SalesSelectionType',
                            id:'creditsales',
                            checked:Wtf.account.companyAccountPref.salesTypeFlag==false
                        })]
                    }, {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.accPref.purchasetypeselection"),  //'Purchase Type Selection Setting',
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.purchasetypeselectionToolTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.purchasetypeselection")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.cashPurchase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoCP"),  //'Cash Purchase',
                            name:'PurchaseSelectionType',
                            id:'cashpurchase',
                            checked:Wtf.account.companyAccountPref.purchaseTypeFlag==true
                        }), this.creditPurchase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.CreditPurchase"),  //Credit Purchase
                            name:'PurchaseSelectionType',
                            id:'creditpurchase',
                            checked:Wtf.account.companyAccountPref.purchaseTypeFlag==false
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,

                        hidden:true,
                        title:WtfGlobal.getLocaleText("acc.accPref.invoicetermssetting"),  //'Invoice Terms Setting - Inclusive of GST',
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.invoicetermssettingtoolTip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.invoicetermssetting")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.termsincludegst=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.msgbox.yes"),
                            name:'InvoiceTermsSetting',
                            id:'termsincludegst',
                            checked:Wtf.account.companyAccountPref.termsincludegst==true
                        }), this.termsexcludegst=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.msgbox.no"),
                            name:'InvoiceTermsSetting',
                            id:'termsexcludegst',
                            checked:Wtf.account.companyAccountPref.termsincludegst==false
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CompanyImage")+"'>"+WtfGlobal.getLocaleText("acc.field.CompanyImage")+"</span>",
                        items:[this.DashBoardImage=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.UpaloadCompanyImage"),
                            labelStyle:'width: 280px;',
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat,
                            name:'DashBoardImage',
                            checked: Wtf.account.companyAccountPref.DashBoardImageFlag
                        }),this.UploadImageButton = new Wtf.Toolbar.Button({
                            text: WtfGlobal.getLocaleText("acc.field.DashBackImage"),
                            tooltip: WtfGlobal.getLocaleText("acc.field.DashBackImage.msg"),
                            disabled: ! Wtf.account.companyAccountPref.DashBoardImageFlag,
                            scope: this,
                            handler: function() {
                                addImageBackgroundToDash();

                            }
                        })]

                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:Wtf.defaultReferralKeyflag,
                        hideLabel:Wtf.defaultReferralKeyflag,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CompanyFixedAssetSetting")+"'>"+WtfGlobal.getLocaleText("acc.field.CompanyFixedAssetSetting")+"</span>",
                        items:[this.assetSetting = new Wtf.Toolbar.Button({
                            text: WtfGlobal.getLocaleText("acc.field.activatefixedasset"),
                            tooltip: WtfGlobal.getLocaleText("acc.field.activatefixedasset"),
                            //                            disabled: Wtf.account.companyAccountPref.ActivateFixedAssetModule,
                            scope: this,
                            handler:this.assetSettingsHandler
                        })]
                    },{

                        xtype:'fieldset',
                        autoHeight:true,
                        // hidden:true,
                        title:WtfGlobal.getLocaleText("acc.field.NegativeStock"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.NegativeStockSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.NegativeStock")+"</span>",
                        defaults:{
//                            anchor:'80%',
//                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.isNegativeStockForLocationWarehouse=new Wtf.form.Checkbox({ //option to checknegative stockfor location/warehouse
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isNegativeStockForLocationWarehouseTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isNegativeStockForLocationWarehouse")+"</span>",
                            name:'isnegativestockforlocwar',
                            anchor: '80%',
                            maxLength: 50,
                            checked:Wtf.account.companyAccountPref.isnegativestockforlocwar
                        }),this.isAllowQtyMoreThanLinkedDoc=new Wtf.form.Checkbox({ //option to Allow Qantity Normal Linking (eg. SO-SI, PO-PI, etc)
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isAllowQtyMoreThanLinkedDoc")+"'>"+WtfGlobal.getLocaleText("acc.field.isAllowQtyMoreThanLinkedDoc")+"</span>",
                            name:'isAllowQtyMoreThanLinkedDoc',
                            anchor: '80%',
                            maxLength: 50,
                            checked:Wtf.account.companyAccountPref.isAllowQtyMoreThanLinkedDoc}),

                            this.isAllowQtyMoreThanLinkedDocCross=new Wtf.form.Checkbox({ //option to Allow Qantity (eg. SO-PO, or vice versa)
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isAllowQtyMoreThanLinkedDocCross")+"'>"+WtfGlobal.getLocaleText("acc.field.isAllowQtyMoreThanLinkedDocCross")+"</span>",
                            name:'isAllowQtyMoreThanLinkedDocCross',
                            anchor: '80%',
                            maxLength: 50,
                            checked:Wtf.account.companyAccountPref.isAllowQtyMoreThanLinkedDocCross}),{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsDO"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsDO")+"'>"+WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsDO")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.negativeignoreCase=new Wtf.form.Radio({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'negativeStock',
                            checked :Wtf.account.companyAccountPref.negativestock=='0',
                            disabled:!Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'ignoreCase'
                        //                            checked:Wtf.account.companyAccountPref.ignoreCase
                        }),this.negativeblockCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'negativeStock',
                            checked :Wtf.account.companyAccountPref.negativestock=='1'&& !Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            disabled:Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'blockCase'
                        //                            checked:Wtf.account.companyAccountPref.blockCase
                        }),this.negativewarnCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'negativeStock',
                            checked :Wtf.account.companyAccountPref.negativestock=='2',
                            disabled:!Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'warnCase'
                        //                            checked:Wtf.account.companyAccountPref.warnCase
                        })]

                    },{

                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.FormulaNegativeStockSettingsSO"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.FormulaNegativeStockSettingsSO")+"'>"+WtfGlobal.getLocaleText("acc.field.FormulaNegativeStockSettingsSO")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.defaultnegativeFormulaSO=new Wtf.form.Radio({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.DefaultFormulainso"),
                            name:'negativeStockFormulaSO',
                            checked :Wtf.account.companyAccountPref.negativeStockFormulaSO=='0',
                            id:'defaultnegativeFormulaSO'
                        }),this.negativeFormulaSO=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.negativeFormulaSO"),
                            name:'negativeStockFormulaSO',
                            checked :Wtf.account.companyAccountPref.negativeStockFormulaSO=='1',
                            id:'negativeFormulaSO'
                        })]

                    },{

                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsSO"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsSO")+"'>"+WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsSO")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.negativeignoreCaseSO=new Wtf.form.Radio({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'negativeStockSO',
                            checked :Wtf.account.companyAccountPref.negativeStockSO=='0',
                            disabled:!Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'ignoreCaseSO'
                        }),this.negativeblockCaseSO=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'negativeStockSO',
                            checked :Wtf.account.companyAccountPref.negativeStockSO=='1' && !Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            disabled:Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'blockCaseSO'
                        }),this.negativewarnCaseSO=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'negativeStockSO',
                            checked :Wtf.account.companyAccountPref.negativeStockSO=='2',
                            disabled:!Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'warnCaseSO'
                        })]

                    },{

                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.FormulaNegativeStockSettingsSICS"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.FormulaNegativeStockSettingsSICS")+"'>"+WtfGlobal.getLocaleText("acc.field.FormulaNegativeStockSettingsSICS")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.defaultnegativeFormulaSI=new Wtf.form.Radio({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.DefaultFormulainsi"),
                            name:'negativeStockFormulaSI',
                            checked :Wtf.account.companyAccountPref.negativeStockFormulaSI=='0',
                            id:'defaultnegativeFormulaSI'
                        }),this.negativeFormulaSI=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.negativeFormulaSI"),
                            name:'negativeStockFormulaSI',
                            checked :Wtf.account.companyAccountPref.negativeStockFormulaSI=='1',
                            id:'negativeFormulaSI'
                        })]

                    },{

                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsSICS"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsSICS")+"'>"+WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsSICS")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.negativeignoreCaseSICS=new Wtf.form.Radio({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'negativeStockSICS',
                            checked :Wtf.account.companyAccountPref.negativeStockSICS=='0',
                            disabled:!Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'ignoreCaseSICS'
                        }),this.negativeblockCaseSICS=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'negativeStockSICS',
                            checked :Wtf.account.companyAccountPref.negativeStockSICS=='1' && !Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            disabled:Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'blockCaseSICS'
                        }),this.negativewarnCaseSICS=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'negativeStockSICS',
                            checked :Wtf.account.companyAccountPref.negativeStockSICS=='2',
                            disabled:!Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'warnCaseSICS'
                        })]

                    },{

                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsPR"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsPR")+"'>"+WtfGlobal.getLocaleText("acc.field.NegativeStockSettingsPR")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.negativeignoreCasePR=new Wtf.form.Radio({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'negativeStockPR',
                            checked :Wtf.account.companyAccountPref.negativeStockPR=='0',
                            disabled:!Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'ignoreCasePR'
                        }),this.negativeblockCasePR=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'negativeStockPR',
                            checked :Wtf.account.companyAccountPref.negativeStockPR=='1' && !Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            disabled:Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'blockCasePR'
                        }),this.negativewarnCasePR=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'negativeStockPR',
                            checked :Wtf.account.companyAccountPref.negativeStockPR=='2',
                            disabled:!Wtf.account.companyAccountPref.isnegativestockforlocwar,
                            id:'warnCasePR'
                        })]

                    }]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        //hidden:true,
                        title: WtfGlobal.getLocaleText("acc.field.CustomerCreditControl"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CustomerCreditSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.CustomerCreditControl")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.includeAmountInLimitSI=new Wtf.form.Checkbox({
                                fieldLabel:WtfGlobal.getLocaleText("acc.creditlimit.conflict"),
                                name:'includeAmountInLimitSI',
                                id:'includeAmountInLimitSI',
                                checked:Wtf.account.companyAccountPref.includeAmountInLimitSI
                         }),this.ignoreCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'creditControl',
                            checked :Wtf.account.companyAccountPref.custcreditlimit==Wtf.controlCases.IGNORE,
                            id:'ignoreCaseControl'
                        //                            checked:Wtf.account.companyAccountPref.ignoreCase
                        }),this.blockCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'creditControl',
                            checked :Wtf.account.companyAccountPref.custcreditlimit==Wtf.controlCases.BLOCK,
                            id:'blockCaseControl'
                        //                            checked:Wtf.account.companyAccountPref.blockCase
                        }),this.warnCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'creditControl',
                            checked :Wtf.account.companyAccountPref.custcreditlimit==Wtf.controlCases.WARN,
                            id:'warnCaseControl'
                        //                            checked:Wtf.account.companyAccountPref.warnCase
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        //hidden:true,
                        title: WtfGlobal.getLocaleText("acc.field.CustomerCreditControlOrder"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CustomerCreditSettingsOrder")+"'>"+WtfGlobal.getLocaleText("acc.field.CustomerCreditControlOrder")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.includeAmountInLimitSO=new Wtf.form.Checkbox({
                                fieldLabel:WtfGlobal.getLocaleText("acc.creditlimit.conflict"),
                                name:'includeAmountInLimitSO',
                                id:'includeAmountInLimitSO',
                                checked:Wtf.account.companyAccountPref.includeAmountInLimitSO
                         }),this.ignoreCaseorder=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'creditControlOrder',
                            checked :Wtf.account.companyAccountPref.custcreditlimitorder==Wtf.controlCases.IGNORE,
                            id:'ignoreCaseControlOrder'
                        //                            checked:Wtf.account.companyAccountPref.ignoreCase
                        }),this.blockCaseorder=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'creditControlOrder',
                            checked :Wtf.account.companyAccountPref.custcreditlimitorder==Wtf.controlCases.BLOCK,
                            id:'blockCaseControlOrder'
                        //                            checked:Wtf.account.companyAccountPref.blockCase
                        }),this.warnCaseorder=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'creditControlOrder',
                            checked :Wtf.account.companyAccountPref.custcreditlimitorder==Wtf.controlCases.WARN,
                            id:'warnCaseControlOrder'
                        //                            checked:Wtf.account.companyAccountPref.warnCase
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        //hidden:true,
                        title: WtfGlobal.getLocaleText("acc.field.VendorCreditControlOrder"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.VendorCreditSettingsOrder")+"'>"+WtfGlobal.getLocaleText("acc.field.VendorCreditControlOrder")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.includeAmountInLimitPO=new Wtf.form.Checkbox({
                                fieldLabel:WtfGlobal.getLocaleText("acc.creditlimit.conflict"),
                                name:'includeAmountInLimitPO',
                                id:'includeAmountInLimitPO',
                                checked:Wtf.account.companyAccountPref.includeAmountInLimitPO
                         }),this.ignoreCasevendororder=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'creditControlVendorOrder',
                            checked :Wtf.account.companyAccountPref.vendorcreditlimitorder==Wtf.controlCases.IGNORE,
                            id:'ignoreCaseControlVendorOrder'
                        //                            checked:Wtf.account.companyAccountPref.ignoreCase
                        }),this.blockCasevendororder=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'creditControlVendorOrder',
                            checked :Wtf.account.companyAccountPref.vendorcreditlimitorder==Wtf.controlCases.BLOCK,
                            id:'blockCaseControlVendorOrder'
                        //                            checked:Wtf.account.companyAccountPref.blockCase
                        }),this.warnCasevendororder=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'creditControlVendorOrder',
                            checked :Wtf.account.companyAccountPref.vendorcreditlimitorder==Wtf.controlCases.WARN,
                            id:'warnCaseControlVendorOrder'
                        //                            checked:Wtf.account.companyAccountPref.warnCase
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.VendorDebitSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.VendorDebitControl")+"</span>",
                        defaults:{
                            anchor:'80%'
                        },
                        items:[this.includeAmountInLimitPI=new Wtf.form.Checkbox({
                                fieldLabel:WtfGlobal.getLocaleText("acc.creditlimit.conflict"),
                                name:'includeAmountInLimitPI',
                                id:'includeAmountInLimitPI',
                                checked:Wtf.account.companyAccountPref.includeAmountInLimitPI
                         }),this.vendorCreditIgnoreCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'vendorCreditLimit',
                            checked :Wtf.account.companyAccountPref.vendorcreditcontrol==Wtf.controlCases.IGNORE,
                            id:'ignoreVendorCreditControl'
                        }),this.vendorCreditBlockCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'vendorCreditLimit',
                            checked :Wtf.account.companyAccountPref.vendorcreditcontrol==Wtf.controlCases.BLOCK,
                            id:'blockVendorCreditControl'

                        }),this.vendorCreditWarneCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'vendorCreditLimit',
                            checked :Wtf.account.companyAccountPref.vendorcreditcontrol==Wtf.controlCases.WARN,
                            id:'warnVendorCreditControl'
                        })]

                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.CustomerBudgetControl"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.MinimumBudgetSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.MinimumBudgetSettings")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.budgetIgnoreCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'minBudgetControl',
                            checked :Wtf.account.companyAccountPref.custMinBudget=='0',
                            id:'ignoreMinBudget'
                        }),this.budgetBlockCase=new Wtf.form.Radio({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'minBudgetControl',
                            checked :Wtf.account.companyAccountPref.custMinBudget=='1',
                            id:'blockMinBudget'
                        }),this.budgetWarnCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'minBudgetControl',
                            checked :Wtf.account.companyAccountPref.custMinBudget=='2',
                            id:'warnMinBudget'
                        })]

                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.AccountsWithCode.ttip")+"'>"+WtfGlobal.getLocaleText("acc.field.AccountsWithCode")+"</span>",
                        defaults:{
                            anchor:'80%'
                        },
                        items:[this.withCode=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.WithCode"),
                            name:'accountWithOrWithoutCode',
                            checked :Wtf.account.companyAccountPref.accountsWithCode,
                            id:'accountWithCodeId'
                        }),this.withoutCode=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.WithoutCode"),
                            name:'accountWithOrWithoutCode',
                            checked :!Wtf.account.companyAccountPref.accountsWithCode,
                            id:'accountWithoutCodeId'
                        })]

                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.PartNumbersSettings"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.PartNumbersSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.PartNumbersSettings")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.ShowPartNumber=new Wtf.form.Checkbox({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShowPartNumber"),
                            name:'partNumber',
                            checked:Wtf.account.companyAccountPref.partNumber
                        })]
                    },
//                    {
//                        xtype:'fieldset',
//                        autoHeight:true,
//                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.autogeneratedchequeno.ttip")+"'>"+WtfGlobal.getLocaleText("acc.field.autogeneratedchequeno")+"</span>",
//                        //                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.autogeneratedchequeno")+"'>"+WtfGlobal.getLocaleText("acc.field.PartNumbersSettings")+"</span>",
//                        defaults:{
//                            anchor:'80%',
//                            maxLength:50,
//                            validator:this.validateFormat
//                            },
//                        items:[this.isAutoGeneratedChequeNumber=new Wtf.form.Checkbox({
//                            fieldLabel: WtfGlobal.getLocaleText("acc.field.autogeneratedchequenosetting"),//Should Cheque Number Auto Generate
//                            name:'showAutoGeneratedChequeNumber',
//                            checked:Wtf.account.companyAccountPref.showAutoGeneratedChequeNumber
//                        })]
//                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        title: WtfGlobal.getLocaleText("acc.field.checkForDuplicateChecksSettings"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.checkForDuplicateChecksSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.checkForDuplicateChecks")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                        },
                        items:[this.chequeNoIgnoreCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Ignore"),
                            name:'chequeNoDuplicatsControl',
                            checked :Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoIngore,
                            id:'chequeNoignoreCaseControl'
                        }),this.chequeNoBlockCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Block"),
                            name:'chequeNoDuplicatsControl',
                            checked :Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoBlock,
                            id:'chequeNoblockCaseControl'
                        }),this.chequeNoWarnCase=new Wtf.form.Radio({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.Warn"),
                            name:'chequeNoDuplicatsControl',
                            checked :Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoWarn,
                            id:'chequeNowarnCaseControl'
                        })]

                    }, {
                        //                        xtype:'fieldset',
                        //                        autoHeight:true,
                        //                        title:'Automatic Number Generation With Leading Zero',
                        //                        defaults:{anchor:'80%'},
                        //                        items:[this.showLeadingZero=new Wtf.form.Checkbox({
                        //                            fieldLabel:"Show Leading Zero",
                        //                            name:'showleadingzero',
                        //                            checked:Wtf.account.companyAccountPref.showLeadingZero
                        //                        })]
                        //                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:!Wtf.account.companyAccountPref.invAccIntegration,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.InventoryUpdateSettings")+"'>"+WtfGlobal.getLocaleText("acc.field.InventoryUpdateSettings")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.updateInvLevelCheck=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.UpdateInventoryLevelthroghAccountingSystem"),
                            labelStyle:'width: 280px;',
                            name:'updateInvLevelCheck',
                            checked:Wtf.account.companyAccountPref.isUpdateInvLevel
                        })]
                    }
//                    ,{
//                        xtype:'fieldset',
//                        autoHeight:true,
//                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.MovementWarehouseFlow")+"'>"+WtfGlobal.getLocaleText("acc.field.MovementWarehouseFlow")+"</span>",
//                        defaults:{
//                            anchor:'80%',
//                            maxLength:50,
//                            validator:this.validateFormat
//                            },
//                        items:[this.isMovementWarehouseFlow=new Wtf.form.Checkbox({
//                            fieldLabel:WtfGlobal.getLocaleText("acc.field.MovementWarehouseMapping"),
//                            labelStyle:'width: 280px;',
//                            name:'isMovementWarehouseFlow',
//                            disabled:(!Wtf.account.companyAccountPref.activateInventoryTab)?true:false,
//                            checked:Wtf.account.companyAccountPref.isMovementWarehouseMapping
//                        })]
//                    },
                     ,{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypreferences.customizeProductFunctionality.ttip")+"'>"+WtfGlobal.getLocaleText("acc.companypreferences.customizeProductFunctionality")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50,
                            validator:this.validateFormat
                            },
                        items:[this.ShowDependentField=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.companypreferences.showTariffParametersField"),
                            name:'dependentField',
                            checked:Wtf.account.companyAccountPref.dependentField
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip="+WtfGlobal.getLocaleText("acc.field.RevenueRecognitionAccount*")+"':'>"+WtfGlobal.getLocaleText("acc.field.RevenueRecognitionAccount")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        //                            validator:this.validateFormat
                        },
                        items:[this.noRevenueRecognition,this.isDeferredRevenueRecognition,this.recurringDeferredRevenueRecognition,
                        this.salesAcc,this.salesRevenueRecognitionAccount,this.liabilityAccountForLMS]
//                    },{
//                        xtype: 'fieldset',
//                        autoHeight: true,
//                        title: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.defaultCustomerWarehouseSetting")+"'>"+WtfGlobal.getLocaleText("acc.field.defaultCustomerWarehouseSetting")+"</span>",
//                        defaults: {
//                            anchor: '80%',
//                            maxLength: 50
//                        },
//                        items: [this.defaultWarehouse]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:(Wtf.account.companyAccountPref.countryid != '137'),//only for Malasian Company
                        title:"<span wtf:qtip="+WtfGlobal.getLocaleText("Set Bad Debt Accounts")+"':'>"+WtfGlobal.getLocaleText("Bad Debt Accounts")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        //                            validator:this.validateFormat
                        },
                        items:[this.gstAccountForBadDebts,this.gstBadDebtsReleifAccount,this.gstBadDebtsRecoverAccount,this.gstBadDebtsReleifPurchaseAccount,this.gstBadDebtsRecoverPurchaseAccount,this.gstBadDebtsSuspenseAccount]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:(Wtf.account.companyAccountPref.countryid != '137'),//only for Malasian Company
                        title:"<span wtf:qtip="+WtfGlobal.getLocaleText("Set Tax Adjustment Accounts")+"':'>"+WtfGlobal.getLocaleText("Tax Adjustment Accounts")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        //                            validator:this.validateFormat
                        },
                        items:[this.inputTaxAdjustmentAccount, this.outputTaxAdjustmentAccount]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:(Wtf.account.companyAccountPref.countryid != '137'),//only for Malasian Company
                        title:"<span wtf:qtip="+WtfGlobal.getLocaleText("acc.cga.setting")+"':'>"+WtfGlobal.getLocaleText("acc.cga.setting")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        },
                        items:[this.taxCgaMalaysian]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:(Wtf.account.companyAccountPref.countryid != Wtf.Country.MALAYSIA),//only for Malasian Company
                        title:"<span wtf:qtip="+WtfGlobal.getLocaleText("acc.account.freeGiftJEAccountSetting")+"':'>"+WtfGlobal.getLocaleText("acc.account.freeGiftJEAccountSetting")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        },
                        items:[this.freeGiftJEAccount]
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.accPref.autoNoDescription") +"'>"+WtfGlobal.getLocaleText("acc.accPref.autoNoGeneration")+" </span>",
                        id:"automaticNumberGeneration"+this.helpmodeid,
                        defaults:{
                            xtype:'textfield',
                            maxLength:2000
                        },
                        items:[{
                            border:false,
                            xtype:'panel',
                            width:350,
                            bodyStyle:'padding:0px 0px 20px 40px;',
                            html:'<font color="#555555"><ul><li> '+ WtfGlobal.getLocaleText("acc.accPref.msg1")+
                            '<li>'+ WtfGlobal.getLocaleText("acc.accPref.msg2") +'</ul></font>'
                        },this.autojournalentry,this.autoso,this.autocontract,this.autoinvoice,this.autocreditmemo,this.autoreceipt,this.autogoodsreceipt,this.autodebitnote,this.autopayment,this.autopo,this.autocashsales,this.autocashpurchase,this.autobillingso,this.autobillinginvoice,this.autobillingcreditmemo,this.autobillingreceipt,this.autobillinggoodsreceipt,this.autobillingdebitnote,this.autobillingpayment,this.autobillingpo,this.autobillingcashsales,this.autobillingcashpurchase,this.autopurchaserequisition,this.autorequestforquotation,this.autovenquotation,this.autoquotation,this.autogro,this.autodo,this.autosr,this.autopr,this.autoproductid,this.autocustomerid,this.autovendorid,this.autocheque,this.autobuildassembly,this.autounbuildassembly,this.autoassetgroup,this.autoloanrefnumber,this.reconcilenumber,this.unreconcilenumber,this.securityNo,this.autopackingdo,this.autoshippingdo,this.autojwo,this.dimensionnumber,this.autosalesbaddebtclaimid,this.autopurchasebaddebtclaimid,this.autoRG23EntryNumber]
                    }, {
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:(Wtf.account.companyAccountPref.activateInventoryTab)?false:true,
                        title:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.accPref.autoNoGenerationINv") +"'>"+WtfGlobal.getLocaleText("acc.accPref.autoNoGenerationINv")+" </span>",
                        id:"automaticNumberGenerationInv"+this.helpmodeid,
                        defaults:{
                            xtype:'textfield',
                            anchor:'80%',
                            maxLength:500
                        },
                        items:[{
                            border:false,
                            xtype:'panel',
                            width:350,
                            bodyStyle:'padding:0px 0px 20px 40px;',
                            html:'<font color="#555555"><ul><li> '+ WtfGlobal.getLocaleText("acc.accPref.msg1")+
                            '<li>'+ WtfGlobal.getLocaleText("acc.accPref.msg2") +'</ul></font>'
                        },this.instLocation,this.instStore,this.instStockIssue,this.autoStockAdj,this.autoStockRequest,this.autoCycleCount,this.skuSequence]
                    },{
                        xtype: 'fieldset',
                        autoHeight: true,
                        title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.gst.entity.cp.qtip") + "'>" + WtfGlobal.getLocaleText("acc.gst.multiEntity") + " </span>",
                        id: "multiEntityGST" + this.helpmodeid,
                        items: [{
                                border:false,
                                xtype:'panel',
                                html:'<font color="#555555">'+ WtfGlobal.getLocaleText("acc.gst.entity.cp.panel.qtiptxt") +'</font>'
                            }, this.isMultiEntity = new Wtf.form.Checkbox({
                                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.gst.entity.cp.qtip") + "'>" + WtfGlobal.getLocaleText("acc.gst.activate.multiEntity") + " </span>",
                                cls: 'custcheckbox',
                                width: 15,
                                name: 'isMultiEntity',
                                disabled: true,
                                style:'left: 100px;',
                                listeners: {
                                    'check': {
                                        fn: this.multiEntityHandler,
                                        scope: this
                                    }
                                },
                                checked: Wtf.account.companyAccountPref.isMultiEntity
                            })]
                    },{
                        columnWidth: .49,
                        layout: 'fit',
                        items: this.multiEntityGrid     //GST Details Grid
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:(Wtf.account.companyAccountPref.countryid == '203' ||Wtf.account.companyAccountPref.countryid == '137')?false:true,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.GoodsandServicesTaxCodefortheCompany")+"'>"+WtfGlobal.getLocaleText("acc.field.CompanyGSTDetail")+" </span>",
                        id:"gstNumber"+this.helpmodeid,
                        defaults:{
                            xtype:'textfield',
                            anchor:'80%',
                            maxLength:30
                        },
                        items:[this.enableGST=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.enable.gst"),
                            cls : 'custcheckbox',
                            width : 10,
                            hidden:(Wtf.account.companyAccountPref.countryid != '137'),// hide if company is not a Malasian
                            hideLabel:(Wtf.account.companyAccountPref.countryid != '137'),// hide if company is not a Malasian
                            name:'enableGST',
                            listeners:{
                                'check':{
                                    fn:this.GSTEnableHandler,
                                    scope:this
                                }
                            },
                            checked:Wtf.account.companyAccountPref.enableGST
                        }),this.gstnumber=new Wtf.form.TextField({
                            fieldLabel: ((Wtf.account.companyAccountPref.countryid == '137'))?WtfGlobal.getLocaleText("acc.trade.reg.no"):WtfGlobal.getLocaleText("acc.field.GSTNumber"),  //'Transactions without inventory',
                            name:'gstNumber',
                            disabled:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// for malasian company
                            emptyText: ((Wtf.account.companyAccountPref.countryid == '137'))?WtfGlobal.getLocaleText("acc.trade.reg.no.empty.text"):WtfGlobal.getLocaleText("acc.field.EnterGSTNumber"),
                            value:Wtf.account.companyAccountPref.gstnumber,
                            hideLabel: Wtf.account.companyAccountPref.isMultiEntity,//hidden when multi entity is activated
                            hidden: Wtf.account.companyAccountPref.isMultiEntity//hidden when multi entity is activated
                        }),this.taxNumber=new Wtf.form.TextField({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.TaxNumber"),  //'Transactions without inventory',
                            name:'taxNumber',
                            disabled:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// for malasian company
                            emptyText:WtfGlobal.getLocaleText("acc.field.EnterTaxNumber"),
                            value:Wtf.account.companyAccountPref.taxNumber,
                            hideLabel: Wtf.account.companyAccountPref.isMultiEntity,//hidden when multi entity is activated
                            hidden: Wtf.account.companyAccountPref.isMultiEntity//hidden when multi entity is activated
                        }),this.gstEffectiveDate= new Wtf.form.DateField({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.malaysiangst.activationdate"),
                            format:WtfGlobal.getOnlyDateFormat(),
                            name: 'gstEffectiveDate',
                            hidden:(Wtf.account.companyAccountPref.countryid != '137'),
                            hideLabel:(Wtf.account.companyAccountPref.countryid != '137'),
                            disabled:this.isDisabledGstEffectiveDate(),//(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// for malasian company
                            //                            minValue:Wtf.account.companyAccountPref.firstfyfrom,
                            value:this.getGstEffectiveDate(),//Wtf.account.companyAccountPref.gstEffectiveDate
                            listeners: {
                                scope:this,
                                change:function(obj,newValue,oldValue){

                                    /*
                                        ERP-26012 - User will be allowed to set GST activation date to past date only when deactivation date is not set. i.e. at initial company set up
                                    */
                                    if((this.gstEffectiveDate.getValue() < new Date().clearTime())  &&  (Wtf.account.companyAccountPref.gstDeactivationDate != '' && Wtf.account.companyAccountPref.gstDeactivationDate != null && Wtf.account.companyAccountPref.gstDeactivationDate != undefined)){
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Please select future date."], 2);
                                        this.gstEffectiveDate.setValue(oldValue);
                                        return;
                                    }
                                }
                            }
                        }),this.gstDeactivationDate= new Wtf.form.DateField({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.malaysiangst.deactivationdate"),
                            format:WtfGlobal.getOnlyDateFormat(),
                            name: 'gstDeactivationDate',
                            hidden:(Wtf.account.companyAccountPref.countryid != '137'),
                            hideLabel:(Wtf.account.companyAccountPref.countryid != '137'),
                            disabled:this.isDisabledGstDeactivationDate(),//(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// for malasian company
//                            minValue:new Date(),
                            value:this.getGstDeactivationDate(),
                            listeners: {
                                scope:this,
                                change:function(obj,newValue,oldValue){
                                    if (this.gstEffectiveDate.getValue() == "") {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"Please select activation date first."],2);
                                        this.gstDeactivationDate.reset();
                                        return;
                                    }
                                    if (this.gstDeactivationDate.getValue() <= new Date().clearTime()) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Please select future date."], 2);
                                        this.gstDeactivationDate.setValue(oldValue);
                                        return;
                                    }
                                    if(this.gstDeactivationDate.getValue() < this.gstEffectiveDate.getValue()){
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"Deactivation Date cannot be less than the Activation Date."],2);
                                        this.gstDeactivationDate.reset();
                                        return;
                                    }
                                }
                            }
                        }),this.companyuen=new Wtf.form.TextField({
                            fieldLabel:(Wtf.account.companyAccountPref.countryid != '137')?WtfGlobal.getLocaleText("acc.field.CompanyUEN"):WtfGlobal.getLocaleText("acc.field.CompanyBRN"),
                            name:'companyUEN',
                            emptyText:(Wtf.account.companyAccountPref.countryid != '137')?WtfGlobal.getLocaleText("acc.field.EnterCompanyUEN"):WtfGlobal.getLocaleText("acc.field.EnterCompanyBRN"),
                            disabled:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// for malasian company
                            value:Wtf.account.companyAccountPref.companyuen,
                            hideLabel: Wtf.account.companyAccountPref.isMultiEntity,//hidden when multi entity is activated
                            hidden: Wtf.account.companyAccountPref.isMultiEntity//hidden when multi entity is activated
                        }),
                            this.industryCodeCmb = new Wtf.form.FnComboBox({
                             triggerAction: 'all',
                             mode: 'local',
                             name: 'industryCodeId',
                             hiddenName: 'industryCodeId',
                             labelWidth : '130',
                             valueField: 'id',
                             disabled: (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST), // for malasian company
                             hideLabel: Wtf.account.companyAccountPref.isMultiEntity || Wtf.account.companyAccountPref.countryid != '137',//hidden when multi entity is activated
                             hidden: Wtf.account.companyAccountPref.isMultiEntity || Wtf.account.companyAccountPref.countryid != '137',//hidden when multi entity is activated
                             displayField: 'name',
                             store: this.masterItemTempStore,
                             fieldLabel:WtfGlobal.getLocaleText("acc.PrimaryMsic.code"),
                             width: 240,
                             typeAhead: true,
                             forceSelection: true
                        })
                        ,this.iafversion=new Wtf.form.TextField({
                            fieldLabel:(Wtf.account.companyAccountPref.countryid != '137')? WtfGlobal.getLocaleText("acc.field.IAFFileVersion"):WtfGlobal.getLocaleText("acc.field.GAFileVersion"),
                            name:'iafVersion',
                            emptyText:(Wtf.account.companyAccountPref.countryid != '137')?WtfGlobal.getLocaleText("acc.field.EnterIAFFileVersion"):WtfGlobal.getLocaleText("acc.field.EnterGAFFileVersion"),
                            disabled:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// for malasian company
                            value:Wtf.account.companyAccountPref.countryid == '137' ? Wtf.MasterConfig_GAFFileVersion : Wtf.account.companyAccountPref.iafversion,
                            readOnly:Wtf.account.companyAccountPref.countryid == '137',
                            hideLabel: Wtf.account.companyAccountPref.countryid == '137',
                            hidden: Wtf.account.companyAccountPref.countryid == '137'
                        }),this.gafVersion=new Wtf.form.ComboBox({
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.GAFileVersion"),
                            store: this.gafVersionStore,
                            name: 'gafversion',
                            id: 'gafversion' + this.id,
                            anchor: '85%',
                            hiddenName: 'gafversion',
                            valueField: 'id',
                            disabled: (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST), // for malasian company
                            mode: 'local',
                            value: Wtf.account.companyAccountPref.iafversion,
                            displayField: 'type',
                            forceSelection: true,
                            triggerAction: 'all',
                            selectOnFocus: true,
                            hideLabel: !(Wtf.account.companyAccountPref.countryid == '137'),
                            hidden: !(Wtf.account.companyAccountPref.countryid == '137')
                        }),this.badDebtProcessingType=new Wtf.form.ComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.malaysiangst.badDebtProcessingPeriodType"),
                            store:this.badDebtPeriodTypeStore,
                            name:'badDebtProcessingPeriodType',
                            id:'badDebtProcessingPeriodType'+this.id,
                            anchor:'85%',
                            hiddenName:'badDebtProcessingPeriodType',
                            valueField:'id',
                            disabled:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// for malasian company
                            mode:'local',
                            value:Wtf.account.companyAccountPref.badDebtProcessingPeriodType?Wtf.account.companyAccountPref.badDebtProcessingPeriodType:0,
                            displayField:'type',
                            forceSelection: true,
                            triggerAction: 'all',
                            selectOnFocus: true,
                            hideLabel: !(Wtf.account.companyAccountPref.countryid == '137'),
                            hidden: !(Wtf.account.companyAccountPref.countryid == '137')
                        }),this.badDebtProcessingPeriod=new Wtf.form.NumberField({
                            name:'badDebtProcessingPeriod',
                            hiddenName:'badDebtProcessingPeriod',
                            id:'badDebtProcessingPeriod'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.malaysiangst.badDebtProcessingPeriod"),
                            allowNegative: false,
                            minValue:1,
                            disabled:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// for malasian company
                            value:Wtf.account.companyAccountPref.badDebtProcessingPeriod?Wtf.account.companyAccountPref.badDebtProcessingPeriod:6,
                            allowDecimals:false,
                            hideLabel: !(Wtf.account.companyAccountPref.countryid == '137'),
                            hidden: !(Wtf.account.companyAccountPref.countryid == '137')
                        }),this.gstSubmissionPeriod=new Wtf.form.ComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.malaysiangst.gstSubmissionPeriod"),
                            store:this.gstSubmissionPeriodStore,
                            name:'gstSubmissionPeriod',
                            id:'gstSubmissionPeriod'+this.id,
                            anchor:'85%',
                            hiddenName:'gstSubmissionPeriod',
                            valueField:'id',
                            disabled:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// for malasian company
                            mode:'local',
                            value:Wtf.account.companyAccountPref.gstSubmissionPeriod?Wtf.account.companyAccountPref.gstSubmissionPeriod:0,
                            displayField:'type',
                            forceSelection: true,
                            triggerAction: 'all',
                            selectOnFocus:true,
                            hideLabel: !(Wtf.account.companyAccountPref.countryid == '137') || Wtf.account.companyAccountPref.isMultiEntity,//ERM-776
                            hidden: !(Wtf.account.companyAccountPref.countryid == '137') || Wtf.account.companyAccountPref.isMultiEntity//ERM-776
                        })]
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:!Wtf.account.companyAccountPref.standalone,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ManageSMTPAuthentication")+"'>"+WtfGlobal.getLocaleText("acc.field.ManageSMTPAuthentication")+" </span>",
                        id:"smtpauthId",
                        defaults:{
                           // anchor:'80%',
                            maxLength:30
                        },
                       items: [
                           this.SMTPConfigLink,
                           this.testEmail = new Wtf.Toolbar.Button({
                               text: WtfGlobal.getLocaleText("acc.field.testEmail"),
                               tooltip: WtfGlobal.getLocaleText("acc.field.testEmail"),
                               scope: this,
                               handler: this.testEmailHandler
                           })
                       ]
                    },
                    {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.otherdetails.compprefpanel.title"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.otherdetails.compprefpanel.ttip")+"'>"+WtfGlobal.getLocaleText("acc.otherdetails.compprefpanel.title")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        },
                        items:[this.viewDetailsField=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.otherdetails.compprefpanel.flabel"),
                            name:'viewDetailsParm',
                            checked:Wtf.account.companyAccountPref.viewDetailsPerm
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        hidden:Wtf.defaultReferralKeyflag,
                        hideLabel:Wtf.defaultReferralKeyflag,
                        title:WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.title"),
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.ttip")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.title")+"</span>",
                        defaults:{
                            anchor:'80%',
                            maxLength:50
                        },
                        items:[this.SKUField=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.SKUEnable"),
                            name:'SKUFieldParm',
                            checked:Wtf.account.companyAccountPref.SKUFieldParm,
                           listeners:{
                                scope:this,
                                check :function(){
                                    if(this.SKUField.getValue()){
                                        Wtf.getCmp('SKUFieldRename'+this.id).enable();
                                    } else{
                                        Wtf.getCmp('SKUFieldRename'+this.id).disable();
                                    }
                                }
                        }
                        }),
                        this.SKUFieldLabel=new Wtf.form.TextField({//to rename sku in entire project
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.SKULabel")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.SKULabel") +"</span>",
                            name:'SKUFieldRename',
                            id:'SKUFieldRename'+this.id,
                            disabled:true,
                            allowBlank: true,
                            maxLength:20,
                            value:Wtf.account.companyAccountPref.SKUFieldRename== undefined ? "" : Wtf.account.companyAccountPref.SKUFieldRename
                        }),
                        this.barcodeGenField=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.flabel"),
                            name:'generateBarcodeParm',
                            checked:Wtf.account.companyAccountPref.generateBarcodeParm,
                            listeners:{
                                scope:this,
                                check :function(){
                                    if(this.barcodeGenField.getValue()){
                                        Wtf.getCmp('barcodetype'+this.id).enable();
                                        Wtf.getCmp('barcdDpi'+this.id).enable();
                                        Wtf.getCmp('barcdHeight'+this.id).enable();
                                        Wtf.getCmp('barcdTopMargin'+this.id).enable();
                                        Wtf.getCmp('barcdLeftMargin'+this.id).enable();
                                        Wtf.getCmp('barcdLabelHeight'+this.id).enable();
                                        Wtf.getCmp('generateBarcodeWithPriceParm'+this.id).enable();
                                        Wtf.getCmp('generateBarcodeWithPnameParm'+this.id).enable();
                                        Wtf.getCmp('generateBarcodeWithPidParm'+this.id).enable();
                                        Wtf.getCmp('generateBarcodeWithMRP'+this.id).enable();
                                    }else{
                                        Wtf.getCmp('barcodetype'+this.id).setValue(Wtf.BarcodeType_Code_CODE128)
                                        Wtf.getCmp('barcdDpi'+this.id).disable();
                                        Wtf.getCmp('barcdHeight'+this.id).disable();
                                        Wtf.getCmp('barcodetype'+this.id).disable();
                                        Wtf.getCmp('barcdTopMargin'+this.id).disable();
                                        Wtf.getCmp('barcdLeftMargin'+this.id).disable();
                                        Wtf.getCmp('barcdLabelHeight'+this.id).disable();
                                        Wtf.getCmp('generateBarcodeWithPriceParm'+this.id).disable();
                                        Wtf.getCmp('generateBarcodeWithPnameParm'+this.id).disable();
                                        Wtf.getCmp('generateBarcodeWithPidParm'+this.id).disable();
                                        Wtf.getCmp('generateBarcodeWithMRP'+this.id).disable();

                                    }

                                }
                            }
                        }),
                        this.barcodeType=new Wtf.form.ComboBox({
                            mode:"local",
                            triggerAction:'all',
                            typeAhead: true,
                            id:'barcodetype'+this.id,
                            name:'barcodetype',
                            store:this.barcodeTypeStore,
                            displayField:"codename",
                            disabled:true,
                            width:200,
                            fieldLabel:WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcodetype"),
                            valueField:'code',
                            value:Wtf.account.companyAccountPref.barcodetype=='' ? Wtf.BarcodeType_Code_CODE128 : Wtf.account.companyAccountPref.barcodetype
                        }),this.dpi=new Wtf.form.NumberField({
                            name:'barcdDpi',
                            id:'barcdDpi'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcddpi") +"(in mm)",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: false,
                            maxLength:3,
                            value:Wtf.account.companyAccountPref.barcodeDpi==undefined ? 99 : Wtf.account.companyAccountPref.barcodeDpi,
                            allowDecimals:false
                        }),this.barcodeHeight=new Wtf.form.NumberField({
                            name:'barcdHeight',
                            id:'barcdHeight'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdheight") +"(in mm)",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: false,
                            value:Wtf.account.companyAccountPref.barcodeHeight==undefined ? 12 : Wtf.account.companyAccountPref.barcodeHeight,
                            maxLength:2,
                            allowDecimals:false
                        }),this.barcodeTopMargin=new Wtf.form.NumberField({
                            name:'barcdTopMargin',
                            id:'barcdTopMargin'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdtopmargin"),
                            disabled:true,
                            allowBlank: false,
                            allowNegative: false,
                            value:Wtf.account.companyAccountPref.barcdTopMargin==undefined ? 110 : Wtf.account.companyAccountPref.barcdTopMargin,
                            maxLength:5,
                            allowDecimals:false
                        }),this.barcodeLeftMargin=new Wtf.form.NumberField({
                            name:'barcdLeftMargin',
                            id:'barcdLeftMargin'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdleftmargin"),
                            disabled:true,
                            allowBlank: false,
                            allowNegative: false,
                            value:Wtf.account.companyAccountPref.barcdLeftMargin==undefined ? 50 : Wtf.account.companyAccountPref.barcdLeftMargin,
                            maxLength:5,
                            maxValue:200,//ERP-11556
                            allowDecimals:false
                        }),this.barcodeLabelHeight=new Wtf.form.NumberField({
                            name:'barcdLabelHeight',
                            id:'barcdLabelHeight'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdLabelHeight") +"(in mm)",
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdLabelHeightttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcdLabelHeight") +"(in mm)"+"</span>",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: false,
                            value:Wtf.account.companyAccountPref.barcdLabelHeight==undefined ? 03 : Wtf.account.companyAccountPref.barcdLabelHeight,
                            maxLength:5,
                            allowDecimals:true
                        }),this.barcodeWithPrice=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcodewithpricettp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcodewithprice")+"</span>",
                            name:'generateBarcodeWithPriceParm',
                            id:'generateBarcodeWithPriceParm'+this.id,
                            tooltip: WtfGlobal.getLocaleText("acc.field.BudgetingSetting"),
                            checked:Wtf.account.companyAccountPref.generateBarcodeWithPriceParm,
                            listeners:{
                                scope:this,
                                check :function(){
                                    if(this.barcodeWithPrice.getValue()){
                                        Wtf.getCmp('pricePrintType'+this.id).enable();
                                        Wtf.getCmp('priceTranslateX'+this.id).enable();
                                        Wtf.getCmp('priceTranslateY'+this.id).enable();
                                        Wtf.getCmp('priceFontSize'+this.id).enable();
                                        Wtf.getCmp('pricePrefix'+this.id).enable();
                                    }else{
                                        Wtf.getCmp('pricePrintType'+this.id).disable();
                                        Wtf.getCmp('priceTranslateX'+this.id).disable();
                                        Wtf.getCmp('priceTranslateY'+this.id).disable();
                                        Wtf.getCmp('priceFontSize'+this.id).disable();
                                        Wtf.getCmp('pricePrefix'+this.id).disable();
                                    }

                                }
                            }
                        }),this.pricePrintType=new Wtf.form.ComboBox({
                            mode:"local",
                            triggerAction:'all',
                            typeAhead: true,
                            id:'pricePrintType'+this.id,
                            name:'pricePrintType',
                            store:this.printTypeStore,
                            displayField:"codename",
                            disabled:true,
                            width:200,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pricePrintTypettp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pricePrintType")+"</span>",
                            valueField:'code',
                            value:Wtf.account.companyAccountPref.pricePrintType=='' ? '90' : Wtf.account.companyAccountPref.pricePrintType
                        }),this.priceTranslateY=new Wtf.form.NumberField({
                            name:'priceTranslateY',
                            id:'priceTranslateY'+this.id,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.priceTranslateYttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.priceTranslateY") +"</span>",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.priceTranslateY==undefined ? -40 : Wtf.account.companyAccountPref.priceTranslateY,
                            maxLength:5,
                            allowDecimals:false
                        }),this.priceTranslateX=new Wtf.form.NumberField({
                            name:'priceTranslateX',
                            id:'priceTranslateX'+this.id,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.priceTranslateXttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.priceTranslateX") +"</span>",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.priceTranslateX==undefined ? -15 : Wtf.account.companyAccountPref.priceTranslateX,
                            maxLength:5,
                            allowDecimals:false
                        }),this.priceFontSize=new Wtf.form.NumberField({
                            name:'priceFontSize',
                            id:'priceFontSize'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.priceFontSize"),
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.priceFontSize==undefined ? 10 : Wtf.account.companyAccountPref.priceFontSize,
                            maxLength:2,
                            allowDecimals:false
                        }),this.pricePrefix = new Wtf.form.TextField({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pricePrefixTT")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pricePrefix") +"</span>",
                            name:'pricePrefix',
                            id:'pricePrefix'+this.id,
                            disabled:true,
                            allowBlank: true,
                            maxLength:20,
                            value:Wtf.account.companyAccountPref.pricePrefix== undefined ? "" : Wtf.account.companyAccountPref.pricePrefix
                        }),
                        this.barcodeWithProdName=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcodewithprodnamettp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcodewithprodname")+"</span>",
                            name:'generateBarcodeWithPnameParm',
                            id:'generateBarcodeWithPnameParm'+this.id,
                            tooltip: WtfGlobal.getLocaleText("acc.field.BudgetingSetting"),
                            checked:Wtf.account.companyAccountPref.generateBarcodeWithPnameParm,
                            listeners:{
                                scope:this,
                                check :function(){
                                    if(this.barcodeWithProdName.getValue()){
                                        Wtf.getCmp('pnamePrintType'+this.id).enable();
                                        Wtf.getCmp('pnameTranslateX'+this.id).enable();
                                        Wtf.getCmp('pnameTranslateY'+this.id).enable();
                                        Wtf.getCmp('pnameFontSize'+this.id).enable();
                                        Wtf.getCmp('pnamePrefix'+this.id).enable();
                                    }else{
                                        Wtf.getCmp('pnamePrintType'+this.id).disable();
                                        Wtf.getCmp('pnameTranslateX'+this.id).disable();
                                        Wtf.getCmp('pnameTranslateY'+this.id).disable();
                                        Wtf.getCmp('pnameFontSize'+this.id).disable();
                                        Wtf.getCmp('pnamePrefix'+this.id).disable();
                                    }

                                }
                            }
                        }),this.pnamePrintType=new Wtf.form.ComboBox({
                            mode:"local",
                            triggerAction:'all',
                            typeAhead: true,
                            id:'pnamePrintType'+this.id,
                            name:'pnamePrintType',
                            store:this.printTypeStore,
                            displayField:"codename",
                            disabled:true,
                            width:200,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodNamePrintTypettp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodNamePrintType")+"</span>",
                            valueField:'code',
                            value:Wtf.account.companyAccountPref.pnamePrintType=='' ? '0' : Wtf.account.companyAccountPref.pnamePrintType
                        }),this.pnameTranslateX=new Wtf.form.NumberField({
                            name:'pnameTranslateX',
                            id:'pnameTranslateX'+this.id,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodNameTranslateXttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodNameTranslateX") +"</span>",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.pnameTranslateX==undefined ? 0 : Wtf.account.companyAccountPref.pnameTranslateX,
                            maxLength:3,
                            maxValue:999,
                            allowDecimals:false
                        }),this.pnameTranslateY=new Wtf.form.NumberField({
                            name:'pnameTranslateY',
                            id:'pnameTranslateY'+this.id,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodNameTranslateYttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodNameTranslateY") +"</span>",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.pnameTranslateY==undefined ? 0 : Wtf.account.companyAccountPref.pnameTranslateY,
                            maxLength:3,
                            maxValue:999,
                            allowDecimals:false
                        }),this.pnameFontSize=new Wtf.form.NumberField({
                            name:'pnameFontSize',
                            id:'pnameFontSize'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodNameFontSize"),
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.pnameFontSize==undefined ? 10 : Wtf.account.companyAccountPref.pnameFontSize,
                            maxLength:2,
                            maxValue:99,
                            allowDecimals:false
                        }),this.pnamePrefix = new Wtf.form.TextField({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodNamePrefixTT")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodNamePrefix") +"</span>",
                            name:'pnamePrefix',
                            id:'pnamePrefix'+this.id,
                            disabled:true,
                            allowBlank: true,
                            maxLength:20,
                            value:Wtf.account.companyAccountPref.pnamePrefix== undefined ? "" : Wtf.account.companyAccountPref.pnamePrefix
                        }),this.barcodeWithPid=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcodewithpidttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcodewithpid")+"</span>",
                            name:'generateBarcodeWithPidParm',
                            id:'generateBarcodeWithPidParm'+this.id,
                            tooltip: WtfGlobal.getLocaleText("acc.field.BudgetingSetting"),
                            checked:Wtf.account.companyAccountPref.generateBarcodeWithPidParm,
                            listeners:{
                                scope:this,
                                check :function(){
                                    if(this.barcodeWithPid.getValue()){
                                        Wtf.getCmp('pidPrintType'+this.id).enable();
                                        Wtf.getCmp('pidTranslateX'+this.id).enable();
                                        Wtf.getCmp('pidTranslateY'+this.id).enable();
                                        Wtf.getCmp('pidFontSize'+this.id).enable();
                                        Wtf.getCmp('pidPrefix'+this.id).enable();
                                    }else{
                                        Wtf.getCmp('pidPrintType'+this.id).disable();
                                        Wtf.getCmp('pidTranslateX'+this.id).disable();
                                        Wtf.getCmp('pidTranslateY'+this.id).disable();
                                        Wtf.getCmp('pidFontSize'+this.id).disable();
                                        Wtf.getCmp('pidPrefix'+this.id).disable();
                                    }

                                }
                            }
                        }),this.pidPrintType=new Wtf.form.ComboBox({
                            mode:"local",
                            triggerAction:'all',
                            typeAhead: true,
                            id:'pidPrintType'+this.id,
                            name:'pidPrintType',
                            store:this.printTypeStore,
                            displayField:"codename",
                            disabled:true,
                            width:200,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pidPrintTypettp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pidPrintType")+"</span>",
                            valueField:'code',
                            value:Wtf.account.companyAccountPref.pnamePrintType=='' ? '0' : Wtf.account.companyAccountPref.pidPrintType
                        }),this.pidTranslateX=new Wtf.form.NumberField({
                            name:'pidTranslateX',
                            id:'pidTranslateX'+this.id,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pidTranslateXttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pidTranslateX") +"</span>",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.pidTranslateX==undefined ? 0 : Wtf.account.companyAccountPref.pidTranslateX,
                            maxLength:3,
                            maxValue:999,
                            allowDecimals:false
                        }),this.pidTranslateY=new Wtf.form.NumberField({
                            name:'pidTranslateY',
                            id:'pidTranslateY'+this.id,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pidTranslateYttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pidTranslateY") +"</span>",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.pidTranslateY==undefined ? 0 : Wtf.account.companyAccountPref.pidTranslateY,
                            maxLength:3,
                            maxValue:999,
                            allowDecimals:false
                        }),this.pidFontSize=new Wtf.form.NumberField({
                            name:'pidFontSize',
                            id:'pidFontSize'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pidFontSize"),
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.pidFontSize==undefined ? 10 : Wtf.account.companyAccountPref.pidFontSize,
                            maxLength:2,
                            maxValue:99,
                            allowDecimals:false
                        }),this.pidPrefix = new Wtf.form.TextField({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pidPrefixTT")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.pidPrefix") +"</span>",
                            name:'pidPrefix',
                            id:'pidPrefix'+this.id,
                            disabled:true,
                            allowBlank: true,
                            maxLength:20,
                            value:Wtf.account.companyAccountPref.pidPrefix== undefined ? "" : Wtf.account.companyAccountPref.pidPrefix
                        }),this.barcodeWithMRP=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcodewithprodmrpttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.barcodewithprodmrp")+"</span>",
                            name:'generateBarcodeWithMRP',
                            id:'generateBarcodeWithMRP'+this.id,
                            tooltip: WtfGlobal.getLocaleText("acc.field.BudgetingSetting"),
                            checked:Wtf.account.companyAccountPref.generateBarcodeWithmrpParm,
                            hidden:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
                            hideLabel:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
                            listeners:{
                                scope:this,
                                check :function(){
                                    if(this.barcodeWithMRP.getValue()){
                                        Wtf.getCmp('mrpPrintType'+this.id).enable();
                                        Wtf.getCmp('mrpTranslateX'+this.id).enable();
                                        Wtf.getCmp('mrpTranslateY'+this.id).enable();
                                        Wtf.getCmp('mrpFontSize'+this.id).enable();
                                        Wtf.getCmp('mrpPrefix'+this.id).enable();
                                    }else{
                                        Wtf.getCmp('mrpPrintType'+this.id).disable();
                                        Wtf.getCmp('mrpTranslateX'+this.id).disable();
                                        Wtf.getCmp('mrpTranslateY'+this.id).disable();
                                        Wtf.getCmp('mrpFontSize'+this.id).disable();
                                        Wtf.getCmp('mrpPrefix'+this.id).disable();
                                    }

                                }
                            }
                        }),this.mrpPrintType=new Wtf.form.ComboBox({
                            mode:"local",
                            triggerAction:'all',
                            typeAhead: true,
                            id:'mrpPrintType'+this.id,
                            name:'pnamePrintType',
                            store:this.printTypeStore,
                            displayField:"codename",
                            disabled:true,
                            width:200,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodMrpPrintTypettp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodMrpPrintType")+"</span>",
                            valueField:'code',
                            value:Wtf.account.companyAccountPref.mrpPrintType=='' ? '0' : Wtf.account.companyAccountPref.mrpPrintType,
                            hidden:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
                            hideLabel:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA)
                        }),this.mrpTranslateY=new Wtf.form.NumberField({
                            name:'mrpTranslateY',
                            id:'mrpTranslateY'+this.id,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodMrpTranslateYttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodMrpTranslateY") +"</span>",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.mrpTranslateY==undefined ? 0 : Wtf.account.companyAccountPref.mrpTranslateY,
                            maxLength:3,
                            maxValue:999,
                            allowDecimals:false,
                            hidden:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
                            hideLabel:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA)
                        }),this.mrpTranslateX=new Wtf.form.NumberField({
                            name:'mrpTranslateX',
                            id:'mrpTranslateX'+this.id,
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodMrpTranslateXttp")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodMrpTranslateX") +"</span>",
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.mrpTranslateX==undefined ? 0 : Wtf.account.companyAccountPref.mrpTranslateX,
                            maxLength:3,
                            maxValue:999,
                            allowDecimals:false,
                            hidden:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
                            hideLabel:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA)
                        }),this.mrpFontSize=new Wtf.form.NumberField({
                            name:'mrpFontSize',
                            id:'mrpFontSize'+this.id,
                            fieldLabel : WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodMrpFontSize"),
                            disabled:true,
                            allowBlank: false,
                            allowNegative: true,
                            value:Wtf.account.companyAccountPref.mrpFontSize==undefined ? 10 : Wtf.account.companyAccountPref.mrpFontSize,
                            maxLength:2,
                            maxValue:99,
                            allowDecimals:false,
                            hidden:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
                            hideLabel:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA)
                        }),this.mrpPrefix = new Wtf.form.TextField({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodMrpPrefixTT")+"'>"+WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.prodMrpPrefix") +"</span>",
                            name:'mrpPrefix',
                            id:'mrpPrefix'+this.id,
                            disabled:true,
                            allowBlank: true,
                            maxLength:20,
                            value:Wtf.account.companyAccountPref.mrpPrefix== undefined ? "" : Wtf.account.companyAccountPref.mrpPrefix,
                            hidden:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA),
                            hideLabel:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA)
                        })
                        ]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                         hidden:Wtf.defaultReferralKeyflag,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.activateBudgetingonDepartment")+"'>"+WtfGlobal.getLocaleText("acc.field.activateBudgetingonDepartment")+"</span>",
                        items:[this.assetSetting = new Wtf.Toolbar.Button({
                            text: WtfGlobal.getLocaleText("acc.field.BudgetingSetting"),
                            tooltip: WtfGlobal.getLocaleText("acc.field.BudgetingSetting"),
                            scope: this,
                            handler:this.budgetSettingsHandler

                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.companypreferences.linkToWindowSettings"),
                        defaults:{anchor:'80%',maxLength:50,validator:this.validateFormat},
                        items:[ this.enableLinkToWin=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypreferences.enableMoreOptionInLinkToDropdown.ttip")+"'>"+WtfGlobal.getLocaleText("acc.companypreferences.enableMoreOptionInLinkToDropdown")+"</span>",
                            name:'enableLinkToSelWin',
                            id:'enableLinkToSelWin',
                            checked:Wtf.account.companyAccountPref.enableLinkToSelWin
                        })]
                    },{
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.companypreferences.bulkinvoicesfromDOSOsettings"),
                        defaults:{anchor:'80%',maxLength:50,validator:this.validateFormat},
                        items:[ this.showBulkInvoices=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypreferences.enableBulkinvoicesgenerationfromDO.ttip")+"'>"+WtfGlobal.getLocaleText("acc.companypreferences.enableBulkinvoicesgenerationfromDO")+"</span>",
                            name:'showBulkInvoices',
                            id:'showBulkInvoices',
                            checked:Wtf.account.companyAccountPref.showBulkInvoices

                            /* Settings to show Bulk invoice creation button in SO report tab */
                        }),this.showBulkInvoicesFromSO=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypreferences.enableBulkinvoicesgenerationfromSO.ttip")+"'>"+WtfGlobal.getLocaleText("acc.companypreferences.enableBulkinvoicesgenerationfromSO")+"</span>",
                            name:'showBulkInvoicesFromSO',
                            id:'showBulkInvoicesFromSO',
                            checked:Wtf.account.companyAccountPref.showBulkInvoicesFromSO
                        })]
                    },{
                         /* Settings to show Bulk DO creation button in SO report tab */
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.companypreferences.bulkDOGRfromSOPOsettings"),
                        defaults:{anchor:'80%',maxLength:50,validator:this.validateFormat},
                        items:[ this.showBulkDOFromSO=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypreferences.enableBulkDOgenerationfromSO.ttip")+"'>"+WtfGlobal.getLocaleText("acc.companypreferences.enableBulkDOgenerationfromSO")+"</span>",
                            name:'showBulkDOFromSO',
                            id:'showBulkDOFromSO',
                            checked:Wtf.account.companyAccountPref.showBulkDOFromSO

                        }),
                    ]
                    }, {
                        xtype:'fieldset',
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.companypreferences.activateSplitofOpeningBalanceinCOA"),
                        defaults:{anchor:'80%',maxLength:50,validator:this.validateFormat},
                        items:[ this.showBulkInvoices=new Wtf.form.Checkbox({
                            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.splitOpeningBalanceAmount.tooltip")+"'>"+WtfGlobal.getLocaleText("acc.field.splitOpeningBalanceAmount")+"</span>",
                            name:'splitOpeningBalanceAmount',
                            id:'splitOpeningBalanceAmount',
                            checked:Wtf.account.companyAccountPref.splitOpeningBalanceAmount
                        })]
                    },{
                        xtype: 'fieldset',
                        autoHeight: true,
                        title: WtfGlobal.getLocaleText('acc.companypreferences.salesCommissionReportSettingForCommissionInvoiceAmount'),
                        defaults: {
                            anchor: '80%'
                        },
                        items:[this.salesCommissionReportMode0 = new Wtf.form.Radio({
                            boxLabel: "<span wtf:qtip='"+WtfGlobal.getLocaleText('acc.companypreferences.invoiceGrossAmount.ttip')+"'>"+WtfGlobal.getLocaleText('acc.companypreferences.invoiceGrossAmount')+"</span>",
                            hideLabel: true,
                            name: 'salesCommissionReportMode',
                            checked: Wtf.account.companyAccountPref.salesCommissionReportMode == '0',
                            id: 'salesCommissionReportMode_0'
                        }),this.salesCommissionReportMode1 = new Wtf.form.Radio({
                            boxLabel: "<span wtf:qtip='"+WtfGlobal.getLocaleText('acc.companypreferences.invoiceNetAmount.ttip')+"'>"+WtfGlobal.getLocaleText('acc.companypreferences.invoiceNetAmount')+"</span>",
                            hideLabel: true,
                            name: 'salesCommissionReportMode',
                            checked: Wtf.account.companyAccountPref.salesCommissionReportMode == '1',
                            id: 'salesCommissionReportMode_1'
                        }), this.salesCommissionReportMode2 = new Wtf.form.Radio({
                            boxLabel: "<span wtf:qtip='"+WtfGlobal.getLocaleText('acc.companypreferences.invoiceNetAmountExcludingTax.ttip')+"'>"+WtfGlobal.getLocaleText('acc.companypreferences.invoiceNetAmountExcludingTax')+"</span>",
                            hideLabel: true,
                            name: 'salesCommissionReportMode',
                            checked: Wtf.account.companyAccountPref.salesCommissionReportMode == '2',
                            id: 'salesCommissionReportMode_2'
                        })]
                    },{
                        xtype: 'fieldset',
                        autoHeight: true,
                        title: 'Change SO status after Sales Return',
                        defaults: {
                            anchor: '80%'
                        },
                        items:[this.soReopen = new Wtf.form.Checkbox({
                            boxLabel: "<span wtf:qtip='"+'Reopen SO After SR'+"'>"+'Reopen SO After SR'+"</span>",
                            hideLabel: true,
                            name: 'salesorderreopen',
                            checked: Wtf.account.companyAccountPref.salesorderreopen,
                            id: 'salesorderreopen'
                        })]
                    },{
                        xtype: 'fieldset',
                        autoHeight: true,
                        title: 'To Show Requisition Status Column',
                        defaults: {
                            anchor: '80%'
                        },
                        items:[this.statusOfRequisitionForPO = new Wtf.form.Checkbox({
                            boxLabel: "<span wtf:qtip='"+'Requisition status will be shown Open or Closed based on quantity used in PO'+"'>"+'Show Requisition Status For PO'+"</span>",
                            hideLabel: true,
                            name: 'statusOfRequisitionForPO',
                            checked: CompanyPreferenceChecks.statusOfRequisitionForPO(),
                            id: 'statusOfRequisitionForPO'
                        })]
                    },{

                        xtype: 'fieldset',
                        autoHeight: true,
                        title: 'Activate Dropship Functionality',
                        defaults: {
                            anchor: '80%'
                        },
                        items:[this.activateDropShip = new Wtf.form.Checkbox({
                            boxLabel: "<span wtf:qtip='"+'Requisition status will be shown Open or Closed based on quantity used in PO'+"'>"+'Activate Drop Ship'+"</span>",
                            hideLabel: true,
                            name: 'activatedropship',
                            checked: CompanyPreferenceChecks.activateDropShip(),
                            id: 'activatedropship'
                        })]

                    },{
                                    xtype: 'fieldset',
                                    autoHeight: true,
                                    title: 'Activate Landed Cost of Item',
                                    defaults: {
                                        anchor: '80%'
                                    },
                                    items: [this.isActiveLandingCostOfItem = new Wtf.form.Checkbox({
                                            boxLabel: "<span wtf:qtip='" + 'Landed Cost of Item' + "'>" + 'Landed Cost of Item' + "</span>",
                                            hideLabel: true,
                                            name: 'isActiveLandingCostOfItem',
                                            checked: Wtf.account.companyAccountPref.isActiveLandingCostOfItem,
                                            id: 'isActiveLandingCostOfItem',
                                            listeners: {
                                                scope: this,
                                                "check": function (obj, checked) {
                                                    if (!checked) {
                                                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("Do you want to deactivate the Landed Cost Item ?"), function (btn) {
                                                            if (btn != "yes") {
                                                                obj.setValue(true);
                                                            } else {
                                                                this.isLandedCostTermJE.setDisabled(true);
                                                                this.isLandedCostTermJE.setValue(false);
                                                            }
                                                        }, this);
                                                    } else {
                                                        this.isLandedCostTermJE.setDisabled(false);
                                                        this.isLandedCostTermJE.setValue(CompanyPreferenceChecks.isLandedCostTermJE());
                                                    }
                                                }
                                            }
                                        }), this.isLandedCostTermJE = new Wtf.form.Checkbox({
                                            boxLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.companypref.landingcosttermje") + "'>" + WtfGlobal.getLocaleText("acc.companypref.landingcosttermjeinfo") + "</span>",
                                            hideLabel: true,
                                            name: 'isLandedCostTermJE',
                                            disabled: !Wtf.account.companyAccountPref.isActiveLandingCostOfItem,
                                            scope: this,
                                            checked: CompanyPreferenceChecks.isLandedCostTermJE(),
                                            id: 'isLandedCostTermJE',
                                            listeners: {
                                                scope: this,
                                                "check": function (obj, checked) {
                                                    if (this.isActiveLandingCostOfItem.getValue() && checked!=Wtf.account.companyAccountPref.columnPref.isLandedCostTermJE) {
                                                        Wtf.Ajax.requestEx({
                                                            url: "ACCGoodsReceipt/isLandedCostWithTermTransactionsPresent.do"
                                                        }, this, function (response) {
                                                            /**
                                                             * response.success will be true if Landed Cost term transaction is present and
                                                             * false if not Present.
                                                             */
                                                            if (response.success) {
                                                                Wtf.MessageBox.show({
                                                                    title: WtfGlobal.getLocaleText("acc.common.alert"),
                                                                    width: 500,
                                                                    msg: response.msg,
                                                                    buttons: Wtf.MessageBox.OK,
                                                                    scope: this,
                                                                    icon: Wtf.MessageBox.WARNING
                                                                });
                                                                obj.setValue(!checked);
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        })]
                                }
                    ,{
                        xtype:'fieldset',
                        collapsible:true,
                        collapsed:true,
                        autoHeight:true,
                        hidden : !Wtf.account.companyAccountPref.childCompaniesPresent,
                        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.companypref.propagatemaster.ttip")+"'>"+WtfGlobal.getLocaleText("acc.companypref.propagatemaster.title")+"</span>" ,
                        items:[ this.propagateToChildCompanies=new Wtf.form.Checkbox({
                                fieldLabel:WtfGlobal.getLocaleText("acc.companypref.propagatecheckbox.title"),
                                name:'propagatemasters',
                                id:'propagatemasters',
                                checked:Wtf.account.companyAccountPref.propagateToChildCompanies
                         })]
                    }
                    ,{
                        xtype: 'fieldset',
                        autoHeight: true,
                        title: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.GST.eway.ewayQtip")+"'>"+WtfGlobal.getLocaleText("acc.GST.eway.activateEway")+"</span>",
                        defaults: {
                            anchor: '80%'
                        },
                        hidden : !WtfGlobal.isIndiaCountryAndGSTApplied() && !Wtf.account.companyAccountPref.childCompaniesPresent,
                        items:[  this.activateEWayBill = new Wtf.form.Checkbox({
                                            name: 'eWayBillPayment',
                                            id: 'eWayBillPayment',
                                            fieldLabel: WtfGlobal.getLocaleText("acc.GST.eway.activateEway"),
                                            checked: CompanyPreferenceChecks.activateEWayBill(),
                                            scope: this,
                                            width: 300,
                                            cls: 'custcheckbox'
        
                         })]
                    }
                ]
                }]
            }]
            //bbar:btnArr
        });

        if(this.perpetualInventory.checked){
            this.AccsStore.load();
        }

         this.purchaseOrder=new Wtf.form.Radio({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoPO"),
            name:'autoGenPurchaseType',
            checked :Wtf.account.companyAccountPref.autoGenPurchaseType==0,
            id:'purchaseOrder'
        });
        this.purchaseRequisition=new Wtf.form.Radio({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoPRequisition"),
            name:'autoGenPurchaseType',
            checked :Wtf.account.companyAccountPref.autoGenPurchaseType==1,
            id:'purchaseRequisition'
        });

           this.autoGenPurchaseTypeFieldset=new Wtf.form.FieldSet({
            id: 'autoGenPurchaseType',
            xtype: 'fieldset',
            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.autoGenPurchaseTypeFieldset.title") + "'>" + WtfGlobal.getLocaleText("acc.autoGenPurchaseTypeFieldset.title") + " </span>",
            checkboxName: 'autoGenPurchaseType',
            items:[this.purchaseOrder,this.purchaseRequisition]
        });
         this.onTaskCompletion=new Wtf.form.Radio({
            fieldLabel:WtfGlobal.getLocaleText("acc.mrpPref.onTaskCompletion"),
            name:'InventoryUpdateType',
            checked :Wtf.account.companyAccountPref.woInventoryUpdateType==0,
            id:'TaskCompletion'
        });
        this.onWorkOrderCompletion=new Wtf.form.Radio({
            fieldLabel:WtfGlobal.getLocaleText("acc.mrpPref.onworkorderCompletion"),
            name:'InventoryUpdateType',
            checked :Wtf.account.companyAccountPref.woInventoryUpdateType==1,
            id:'WorkOrderCompletion'
        });

           this.woInventoryUpdateTypeFieldset=new Wtf.form.FieldSet({
            id: 'woInventoryUpdateType',
            xtype: 'fieldset',
            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.updateinventoryforWO.title") + "'>" + WtfGlobal.getLocaleText("acc.updateinventoryforWO.title") + " </span>",
            checkboxName: 'woInventoryUpdateType',
            items:[this.onTaskCompletion,this.onWorkOrderCompletion]
        });

        this.mrpProductComponentType=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("Co-Product and Scrap"),
            name:'mrpProductComponentType',
            checked :Wtf.account.companyAccountPref.mrpProductComponentType==1,
            id:'mrpProduct'
        });

        this.mrpProductComponentTypeFieldset=new Wtf.form.FieldSet({
            id: 'mrpProductComponentType',
            xtype: 'fieldset',
            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("Product component type") + "'>" + WtfGlobal.getLocaleText("Product component type") + " </span>",
            checkboxName: 'mrpProductComponentType',
            items:[this.mrpProductComponentType]
        });

        this.MRPPrefForm=new Wtf.form.FormPanel({
            style: 'background: white;',
            autoHeight: true,
            disabledClass:"newtripcmbss",
            id:"MRP"+this.id,
            border:false,
            items:[{
                layout:'form',
                defaults:{
                    border:false
                },
               // baseCls:'northFormFormat',
                labelWidth:200,
                items:[{
                    layout:'column',
                   defaults:{
                    border:false,
                    bodyStyle:'padding:10px'
                },
                    items:[{
                        layout:'form',
                        columnWidth:0.49,

                                    items: [
                                        {
                                            xtype: 'fieldset',
                                            autoHeight: true,
                                            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrpPref.autoNoDescription") + "'>" + WtfGlobal.getLocaleText("acc.accPref.autoNoGeneration") + " </span>",
                                            id: "automaticNumberGenerationmrp" + this.helpmodeid,
                                            defaults: {
                                                xtype: 'textfield',
                                                anchor: '80%',
                                                maxLength: 500
                                            },
                                            items: [{
                                                    border: false,
                                                    xtype: 'panel',
                                                    width: 350,
                                                    bodyStyle: 'padding:0px 0px 20px 40px;',
                                                    html: '<font color="#555555"><ul><li> ' + WtfGlobal.getLocaleText("acc.accPref.msg1") +
                                                            '<li>' + WtfGlobal.getLocaleText("acc.accPref.msg2") + '</ul></font>'
                                                }, this.autolabour,this.automachineid,this.automrpcontract,this.autojobwork,this.autoworkcentre,this.autoworkorder,this.autoroutecode]
                                        }
                                    ]
                                },
                    {
                        layout:'form',
                        columnWidth:0.49,
                        items:[
                                this.autoGenPurchaseTypeFieldset,
                                //this.woInventoryUpdateTypeFieldset,
                                this.mrpProductComponentTypeFieldset
                              ]
                    }

                    ]
                }]
            }]
        });
        Wtf.integrationFunctions.toggleUpsIntegrationOnPickPackShipChange(Wtf.account.companyAccountPref.pickpackship);
        this.pickPackShip.on('check', function () {
            if (this.pickPackShip.getValue()) {
                this.interloconpick.enable();
                this.packinglocation.disable();
                this.packingstore.enable();
            } else {
                this.interloconpick.setValue(false);
                this.interloconpick.disable();
                this.packinglocation.reset();
                this.packinglocation.disable();
                this.packingstore.reset();
                this.packingstore.disable();
            }
            Wtf.integrationFunctions.toggleUpsIntegrationOnPickPackShipChange(this.pickPackShip.getValue());
        }, this);
        this.pickPackShip.on('change', function(obj, newValue, oldValue){
            Wtf.Ajax.requestEx({
                url : "ACCInvoice/isPickPackShipDOPresent.do"
            }, this, function(response){
                /**
                 * response.success will be true if DO is present and
                 * response.success will be false when DO is not Present.
                 */
                if (response.success){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.alert"),
                        width: 500,
                        msg: response.msg,
                        buttons: Wtf.MessageBox.OK,
                        scope: this,
                        icon: Wtf.MessageBox.WARNING
                    });
                    this.pickPackShip.setValue(oldValue);
                    Wtf.integrationFunctions.toggleUpsIntegrationOnPickPackShipChange(oldValue);
                }
            });
        }, this);
         this.packingstore.on('change',function(){
             Wtf.Ajax.requestEx({
                url : "ACCInvoice/isPackingStoreUsedBefore.do"  ,
                params :
                        {
                            store : this.packingstore.getValue()
                        }
             },this, function(response){
                if (response.success){   //response.success is true when pick pack warehouse is already used in any transaction
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.SystemControls.pickpackwarehouse")],2);
                    this.packingstore.setValue("");
                }
            });
        }, this);

        this.packingstore.on('select', function(combo, record, index) {
            this.validateStoreSelection(combo, record, index, "fromPack");
        }, this);

         this.vendorJobOrderStore.on('select', function(combo, record, index) {
            this.validateStoreSelection(combo, record, index, "fromJobOrder");
        }, this);
        this.vendorJobOrderStore.on('change',function(){
            this.checkStoreUsedInTransaction("fromJobOrder");
        },this);

        this.packingstore.on('change',function(){
            this.checkStoreUsedInTransaction("fromPack");
        },this);

        this.jobWorkOutFlow.on('check', function() {
            if (this.jobWorkOutFlow.getValue()) {
                this.vendorJobOrderStore.enable();
            } else {
                this.vendorJobOrderStore.reset();
                this.vendorJobOrderStore.disable();
            }
        }, this);

        this.interloconpick.on('check', function () {
            if (this.interloconpick.getValue()) {
//                this.interloconpick.enable();
                this.packinglocation.enable();
                this.packingstore.reset();
                this.packingstore.disable();
            } else {
//                this.interloconpick.disable();
                this.packinglocation.reset();
                this.packinglocation.disable();
                this.packingstore.enable();
            }
        }, this);

        /*
         * SDP-6506
         */
        this.UOBEndToEndID.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        this.UOBEndToEndID.on('change', function (obj) {
            obj.setValue(obj.getValue().replace(/[\~\`\!\@\#\$\%\^\&\*\_\=\<\>\[\]\{\}\\\|]/g, ""));
        }, this);
        this.UOBEndToEndID.on('render', function (obj) {
            obj.setValue(Wtf.account.companyAccountPref.uobendtoendid);
        }, this);
        this.UOBPurposeCode.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        this.UOBPurposeCode.on('render', function (obj) {
            obj.setValue(Wtf.account.companyAccountPref.uobpurposecode);
        }, this);
        this.exciseMultipleUnit.on('check', function () {
            if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && !this.exciseMultipleUnit.getValue()) {
                this.IsLineLevelTaxUsedInTransaction("", "Excise Unit", this.exciseMultipleUnit);
            }
            this.fireEvent('resize', this);
        }, this);
        this.enableVATCST.on('check', function () {
                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && !this.enableVATCST.getValue()) {
//                    this.IsLineLevelTaxUsedInTransaction("3","CST");
                    this.IsLineLevelTaxUsedInTransaction("1","VAT");
                }
                this.fireEvent('resize',this);
            }, this);
        this.typeOfDealer.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.typeOfDealer);
            }
            if(Wtf.account.companyAccountPref.typeofdealer!="0"){
                this.typeOfDealer.setValue(Wtf.account.companyAccountPref.typeofdealer);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.typeOfDealer);
                } else {
                    this.hideFormElement(this.typeOfDealer);
                }
                this.fireEvent('resize',this);
            }, this);
        }, this);


       this.dateOfApplicability.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.dateOfApplicability);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.dateOfApplicability);
                } else {
                    this.hideFormElement(this.dateOfApplicability);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);
       this.PANNo.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.PANNo);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.PANNo);
                } else {
                    this.hideFormElement(this.PANNo);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.assessmentcircle.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.assessmentcircle);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.assessmentcircle);
                } else {
                    this.hideFormElement(this.assessmentcircle);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.division.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.division);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.division);
                } else {
                    this.hideFormElement(this.division);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.areaCode.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.areaCode);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.areaCode);
                } else {
                    this.hideFormElement(this.areaCode);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.importexportcode.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.importexportcode);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.importexportcode);
                } else {
                    this.hideFormElement(this.importexportcode);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.authrizedby.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.authrizedby);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.authrizedby);
                } else {
                    this.hideFormElement(this.authrizedby);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.authrizedperson.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.authrizedperson);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.authrizedperson);
                } else {
                    this.hideFormElement(this.authrizedperson);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.statudORdesignation.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.statudORdesignation);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.statudORdesignation);
                } else {
                    this.hideFormElement(this.statudORdesignation);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.place.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.place);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.place);
                } else {
                    this.hideFormElement(this.place);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.VATTINcomposition.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.VATTINcomposition);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.VATTINcomposition);
                } else {
                    this.hideFormElement(this.VATTINcomposition);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);


       this.VATTINregular.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.VATTINregular);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.VATTINregular);
                } else {
                    this.hideFormElement(this.VATTINregular);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.LocalSalesTaxNumber.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.LocalSalesTaxNumber);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.LocalSalesTaxNumber);
                } else {
                    this.hideFormElement(this.LocalSalesTaxNumber);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.InterStateSalesTaxNumber.on("render", function() {
            if (!this.enableVATCST.getValue()) {
                this.hideFormElement(this.InterStateSalesTaxNumber);
            }
            this.enableVATCST.on('check', function () {
                if (this.enableVATCST.getValue()) {
                    this.showFormElement(this.InterStateSalesTaxNumber);
                } else {
                    this.hideFormElement(this.InterStateSalesTaxNumber);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);
       this.isLocationCompulsory.on("check",function(chk,checked){
            if(checked==false && Wtf.account.companyAccountPref.activateInventoryTab){
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.SystemControls.warehouselocation")],2);
               this.isLocationCompulsory.setValue(true);
            }
        },this);
        this.isWarehouseCompulsory.on("check",function(chk,checked){
            if(checked==false && Wtf.account.companyAccountPref.activateInventoryTab){
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.SystemControls.warehouselocation")],2);
               this.isWarehouseCompulsory.setValue(true);
            }
        },this);
        this.isLocationCompulsory.on("change",this.checkLocationUsedOrNot);
        this.securityGateEntryFlag.on("change",this.checkSecurityCreated);
        this.isWarehouseCompulsory.on("change",this.checkWarehouseUsedOrNot);
        this.isRowCompulsory.on("change",this.checkRowUsedOrNot);
        this.isRackCompulsory.on("change",this.checkRackUsedOrNot);
        this.isBinCompulsory.on("change",this.checkBinUsedOrNot);
        this.isBatchCompulsory.on("change",this.checkBatchUsedOrNot);
        this.isSerialCompulsory.on("change",this.checkSerialUsedOrNot);
        this.integrationWithInventory.on("change",this.checkInventoryModuleDependancies,this);
        this.isMinMaxOrdering.on("change",this.checkMinMaxOrdering);
        this.isFilterProductByCustomerCategory.on("change",this.isFilterProductByCustomerCategory);
        this.isNegativeStockForLocationWarehouse.on("change",this.checkBatchSerialUsedOrNot,this);
        //        this.noOfDaysforValidTillField.on('blur',this.setInputValue,this);
        //        this.noOfDaysforValidTillField.on('focus',this.resetInputValue,this);
        this.inventorypackaging.on('change', function () {
            if(this.inventorypackaging.getValue()){
                this.uomConversionEditCheck.disable();
                this.uomConversionEditCheck.setValue(false);
                /*
                 * disabling "Variable Purchase/Sales UOM conversion rate" when package schema is activated.ERM-319
                 */
                this.differentUOM.disable();
                this.differentUOM.setValue(false);
            }
        }, this);
        this.inventoryschema.on('change', function () {
            if(this.inventoryschema.getValue()){
                this.uomConversionEditCheck.enable();
                this.differentUOM.enable();         //enabling "Variable Purchase/Sales UOM conversion rate" when package schema is activated.ERM-319
            }

        }, this);
        this.activateCRMIntegration.on('change', this.checkCRMIntegration , this);
        this.activateLMSIntegration.on('change', this.checkLMSIntegration , this);
        this.integrationWithPOS.on('check', function () {
             if (this.integrationWithPOS.getValue()) {
                this.vendorForPOS.enable();
                this.customerForPOS.enable();
                this.selectedCustomerStore.load();
                this.selectedVendorStore.load();
//                this.CreditAccountforPOS.enable();
                this.isCloseRegisterMultipleTimes.enable();
                 this.crStore.load();
                this.isCloseRegisterMultipleTimes.setValue(Wtf.account.companyAccountPref.isCloseRegisterMultipleTimes);

            } else {
                this.customerForPOS.disable();
//                this.CreditAccountforPOS.disable();
                this.vendorForPOS.disable();
                this.isCloseRegisterMultipleTimes.reset()
                this.isCloseRegisterMultipleTimes.disable();
            }
        }, this);

        if (this.integrationWithPOS.checked) {
            this.selectedCustomerStore.on("load", function () {
                this.customerForPOS.setValue(Wtf.account.companyAccountPref.customerForPOS);
            }, this);
            this.selectedCustomerStore.load();
            this.selectedVendorStore.on("load", function () {
                this.vendorForPOS.setValue(Wtf.account.companyAccountPref.vendorForPOS);
            }, this);
            this.selectedVendorStore.load();
//            this.crStore.on("load", function () {
//                this.CreditAccountforPOS.setValue(Wtf.account.companyAccountPref.creditAccountforPOS);
//            }, this);
            this.crStore.load();

        }

          this.ERPPrefTab= new Wtf.Panel({
            width:450,
            frame:true,
            height:500,
            title:WtfGlobal.getLocaleText("acc.companypreferences.accounting"),
            autoScroll:true,
            items:this.form
        });

          this.MRPPrfTab= new Wtf.Panel({
            width:450,
            frame:true,
            height:500,
            title:WtfGlobal.getLocaleText("acc.mrp.management"),
            items:[this.MRPPrefForm]
        });

        var panelArr = new Array();
        panelArr.push(this.ERPPrefTab);
        if(Wtf.account.companyAccountPref.activateMRPManagementFlag){
            panelArr.push(this.MRPPrfTab);    //This code creating problem to existing system. So time being I am commenting this code.
                                               //Empty MRP tab leads problem while making changes in Company Pereferences. ERP-22091
        }

        if(Wtf.account.companyAccountPref.activateGroupCompaniesFlag){
            //Create Group Company Mapping Tab
        this.createGroupCompanyMappingTab();

        this.GroupCompanyTab= new Wtf.Panel({
            width:450,
            frame:true,
            height:500,
            title:WtfGlobal.getLocaleText("acc.groupcompany.mappingwizard"),
            items:[this.GroupCompanyMappingForm]
        });
            panelArr.push(this.GroupCompanyTab);

            this.GroupCompanyTab.on('activate',function(){//on activate tab hide export & show button. this.isGroupCompantFlag to save configuration
                if(this.ExportButton!=undefined && this.ExportButton!="undefined" && this.ExportButton!=null && this.ExportButton!="null"){
                    this.ExportButton.hide();
                }
                if(this.ImportButton!=undefined && this.ImportButton!="undefined" && this.ImportButton!=null && this.ImportButton!="null"){
                    this.ImportButton.hide();
                }
                this.isGroupCompanyTabFlag = true;
                this.GroupCompanyTab.doLayout();
            },this);

            this.GroupCompanyTab.on('deactivate',function(){
                this.isGroupCompanyTabFlag = false;
                if(this.ExportButton!=undefined && this.ExportButton!="undefined" && this.ExportButton!=null && this.ExportButton!="null"){
                    this.ExportButton.show();
                }
                if(this.ImportButton!=undefined && this.ImportButton!="undefined" && this.ImportButton!=null && this.ImportButton!="null"){
                    this.ImportButton.show();
                }

                this.GroupCompanyTab.doLayout();
            },this);
            //                    this.GroupCompanyTab.doLayout();
            this.GroupCompanyTab.on('render',function(){
                /* When GroupCompanyTab is activated intially activating mrp tab and then on render of GroupCompanyTab tab activating */
                this.newPanel.setActiveTab(this.ERPPrefTab);
            },this);
        }

        //For POS related code
        if(Wtf.account.companyAccountPref.integrationWithPOS){
            //Create Group Company Mapping Tab
            this.createPOSTab();

            this.posPanelTab= new Wtf.Panel({
                width:450,
                frame:true,
                height:500,
                title:"Point of Sale Mapping Wizard",
                items:[this.mainPOSPanel]
            });
            panelArr.push(this.posPanelTab);

            this.posPanelTab.on('activate',function(){//on activate tab hide export & show button. this.isGroupCompantFlag to save configuration
                if(this.ExportButton!=undefined && this.ExportButton!="undefined" && this.ExportButton!=null && this.ExportButton!="null"){
                    this.ExportButton.hide();
                }
                if(this.ImportButton!=undefined && this.ImportButton!="undefined" && this.ImportButton!=null && this.ImportButton!="null"){
                    this.ImportButton.hide();
                }
                this.isPOSTabFlag = true;
                this.posPanelTab.doLayout();
            },this);

            this.posPanelTab.on('deactivate',function(){
                this.isPOSTabFlag = false;
                if(this.ExportButton!=undefined && this.ExportButton!="undefined" && this.ExportButton!=null && this.ExportButton!="null"){
                    this.ExportButton.show();
                }
                if(this.ImportButton!=undefined && this.ImportButton!="undefined" && this.ImportButton!=null && this.ImportButton!="null"){
                    this.ImportButton.show();
                }

                this.posPanelTab.doLayout();
            },this);

            this.posPanelTab.on('render',function(){
                /* When GroupCompanyTab is activated intially activating mrp tab and then on render of GroupCompanyTab tab activating */
                this.newPanel.setActiveTab(this.ERPPrefTab);
            },this);
        }


        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.showIndiaCompanyPreferencesTab){
            this.indiaAccountPref_panel = new Wtf.account.IndiaCompanyPreferences({
                id : 'IndiaAccPrefWinhiddenLabel',
                border : false,
                layout: 'fit',
                style: 'background: white;',
                closable: true,
                scope:this
            });
            this.GSTPrfTab= new Wtf.Panel({
                width:450,
                frame:true,
                height:500,
                title:"India Accounting",
                items:[this.indiaAccountPref_panel]
            });

            panelArr.push(this.GSTPrfTab);
        }
        this.newPanel=new Wtf.TabPanel({
            autoScroll:true,
            region : 'center',
            frame:true,
            scope:this,
            closable:true,
            items:panelArr,
            bbar:btnArr
        });

        this.MRPPrfTab.doLayout();

        this.newPanel.doLayout();
        this.add(this.newPanel);
        if (Wtf.account.companyAccountPref.activateMRPManagementFlag) {
            /* When MRp is activated intially activating mrp tab and then on render of MRP tab activating ERP tab to resolve ERP-23872*/
            this.newPanel.setActiveTab(this.MRPPrfTab);
        } else if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.showIndiaCompanyPreferencesTab) {
            this.newPanel.setActiveTab(this.GSTPrfTab);
        }else {
            this.newPanel.setActiveTab(this.ERPPrefTab);
        }
        this.ERPPrefTab.on('activate',function(){
            this.ERPPrefTab.doLayout();
            this.MRPPrfTab.doLayout();
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.showIndiaCompanyPreferencesTab) {
                this.GSTPrfTab.doLayout();
            }
        },this);
         this.MRPPrfTab.on('activate',function(){
//            this.ERPPrefTab.doLayout();
            this.MRPPrfTab.doLayout();
        },this);
           this.MRPPrfTab.on('render',function(){
                /* When MRp is activated intially activating mrp tab and then on render of MRP tab activating ERP tab to resolve ERP-23872*/
            this.newPanel.setActiveTab(this.ERPPrefTab);
        },this);
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.showIndiaCompanyPreferencesTab) {
            this.GSTPrfTab.on('render',function(){
                this.newPanel.setActiveTab(this.ERPPrefTab);
            },this);
            this.GSTPrfTab.on('activate',function(){
                this.GSTPrfTab.doLayout();
            },this);
        }
        //        this.ownerCt.doLayout();
        this.setData();
        this.autoproductid.on("render", function(){//On rendering Last Auto_No_Field reArranged AutoNo fields[SK]
            this.reArrangeAutoNoFields(this.val);
            this.withInv.on("check", function(chk, checked){
                this.reArrangeAutoNoFields(checked);
            },this);

            this.reArrangeAutoNoFieldsTrading(Wtf.account.companyAccountPref.withinvupdate);
            this.withInvUpdate.on("check", function(chk, checked){
                this.reArrangeAutoNoFieldsTrading(checked);
            },this);
        },this);

        this.wastageDefaultAccount.on("render", function() {
            if (!Wtf.account.companyAccountPref.activateWastageCalculation) {
                this.hideFormElement(this.wastageDefaultAccount);
                this.isWastageAccountSyncSize = true;
            } else {
                this.isWastageAccountSyncSize = false;
            }
            this.activateWastageCalculation.on('check', function () {
                if (this.activateWastageCalculation.getValue()) {
                    this.showFormElement(this.wastageDefaultAccount);
                    if (this.isWastageAccountSyncSize) {
                        this.wastageDefaultAccount.syncSize();
                        this.isWastageAccountSyncSize = false;
                    }
                } else {
                    this.wastageDefaultAccount.reset();
                    this.hideFormElement(this.wastageDefaultAccount);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.LoanDisbursement.on("render", function() {
            if (!Wtf.account.companyAccountPref.activateLoanManagementFlag) {
                this.hideFormElement(this.LoanDisbursement);
                this.isLoanDisbursementSyncSize = true;
            } else {
                this.isLoanDisbursementSyncSize = false;
            }
            this.isActivateLoanManagement.on('check', function () {
                if (this.isActivateLoanManagement.getValue()) {
                    this.showFormElement(this.LoanDisbursement);
                    this.LoanDisbursement.allowBlank=false;
                    this.LoanInterestAccount.validate();
                    if (this.isLoanDisbursementSyncSize) {
                        this.LoanDisbursement.syncSize();
                        this.isLoanDisbursementSyncSize = false;
                    }
                } else {
                    this.LoanDisbursement.reset();
                    this.LoanDisbursement.allowBlank=true;
                    this.hideFormElement(this.LoanDisbursement);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.LoanInterestAccount.on("render", function() {
            if (!Wtf.account.companyAccountPref.activateLoanManagementFlag) {
                this.hideFormElement(this.LoanInterestAccount);
                this.isLoanInterestAccountSyncSize = true;
            } else {
                this.isLoanInterestAccountSyncSize = false;
            }
            this.isActivateLoanManagement.on('check', function () {
                if (this.isActivateLoanManagement.getValue()) {
                    this.showFormElement(this.LoanInterestAccount);
                    this.LoanInterestAccount.allowBlank=false;
                    this.LoanInterestAccount.validate();
                    if (this.isLoanInterestAccountSyncSize) {
                        this.LoanInterestAccount.syncSize();
                        this.isLoanInterestAccountSyncSize = false;
                    }
                } else {
                    this.LoanInterestAccount.reset();
                    this.LoanInterestAccount.allowBlank=true;
                    this.hideFormElement(this.LoanInterestAccount);
                }
                this.fireEvent('resize',this);
            }, this);
       }, this);

       this.productPricingOnBandsForSales.on('check', function () {
            if (this.productPricingOnBandsForSales.getValue()) {
                this.bandsWithSpecialRateForSales.enable();
            } else {
                this.bandsWithSpecialRateForSales.setValue(false);
                this.bandsWithSpecialRateForSales.disable();
            }
        }, this);
        this.productPricingOnBands.on('check', function () {
            if (this.productPricingOnBands.getValue()) {
                this.bandsWithSpecialRateForPurchase.enable();
            } else {
                this.bandsWithSpecialRateForPurchase.setValue(false);
                this.bandsWithSpecialRateForPurchase.disable();
            }
        }, this);

        this.productPricingOnBandsForSales.on('change', function (obj, newValue, oldValue) {
            this.checkSalesorPurchaseTransactionsPresent(newValue, oldValue, true,obj);
        }, this);

        this.bandsWithSpecialRateForSales.on('change', function (obj, newValue, oldValue) {
            this.checkSalesorPurchaseTransactionsPresent(newValue, oldValue, true,obj);
        }, this);

        this.productPricingOnBands.on('change', function (obj, newValue, oldValue) {
            this.checkSalesorPurchaseTransactionsPresent(newValue, oldValue, false,obj);
        }, this);

        this.bandsWithSpecialRateForPurchase.on('change', function (obj, newValue, oldValue) {
            this.checkSalesorPurchaseTransactionsPresent(newValue, oldValue, false,obj);
        }, this);

        this.periodicInventory.on('change',function(obj,newValue,oldValue) {
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/checkTransactionPresentForManufacturingModule.do"
            }, this, function(response) {
                if (response.msg != "" && response.msg != null && response.msg != undefined) {
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.information"),
                        width: 500,
                        msg: response.msg,
                        buttons: Wtf.MessageBox.OK,
                        scope: this,
                        icon: Wtf.MessageBox.WARNING
                    });

                    this.periodicInventory.setValue(!response.isPerpetualValuationMethod);
                    this.perpetualInventory.setValue(response.isPerpetualValuationMethod);
                    this.AccsStore.load();
                    if (Wtf.getCmp('perpetualsettingfieldset') != undefined && this.perpetualInventory.getValue()) {
                        Wtf.getCmp('perpetualsettingfieldset').setVisible(true);
                    }
                } else if(response.msg == ""){
                    if (Wtf.getCmp('perpetualsettingfieldset') != undefined && !this.perpetualInventory.getValue()) {
                        Wtf.getCmp('perpetualsettingfieldset').setVisible(false);
                    }
                }
            if(this.periodicInventory.getValue()){
                  this.PeriodicJE.enable();
                  this.deductSOBlockedQtyFromValuation.enable();
            }
            }, function(response) {
            });
        }, this);
        
        this.activateBankReconcilitaionDraft.on('change',function(obj,newValue,oldValue) {
            Wtf.Ajax.requestEx({
                url: "ACCReconciliation/getBankReconcilationDrafts.do"
            }, this, function(response) {
                if (!newValue && response.data && response.data.length > 0){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.information"),
                        width: 500,
                        msg: "Some of the Bank Reconciliation records are already saved as draft. You cannot change this setting.",
                        buttons: Wtf.MessageBox.OK,
                        scope: this,
                        icon: Wtf.MessageBox.WARNING
                    });
                    
                    this.activateBankReconcilitaionDraft.setValue(true);
                } 
            });
        }, this);
        
        this.perpetualInventory.on('change', function(obj,newValue,oldValue) {
             Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/checkTransactionPresentForManufacturingModule.do"
            }, this, function(response) {
                if (response.msg != "" && response.msg != null && response.msg != undefined) {
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.information"),
                        width: 500,
                        msg: response.msg,
                        buttons: Wtf.MessageBox.OK,
                        scope: this,
                        icon: Wtf.MessageBox.WARNING
                    });

                    this.perpetualInventory.setValue(response.isPerpetualValuationMethod);
                    this.periodicInventory.setValue(!response.isPerpetualValuationMethod);
                    if (Wtf.getCmp('perpetualsettingfieldset') != undefined && !this.perpetualInventory.getValue()) {
                        Wtf.getCmp('perpetualsettingfieldset').setVisible(false);
                    }

                } else if (response.msg == "") {
                    this.AccsStore.load();
                    if (Wtf.getCmp('perpetualsettingfieldset') != undefined && this.perpetualInventory.getValue()) {
                        Wtf.getCmp('perpetualsettingfieldset').setVisible(true);
                    }
                }
            if(this.perpetualInventory.getValue()){
                  this.PeriodicJE.disable();
                  this.deductSOBlockedQtyFromValuation.setValue(false);
                  this.deductSOBlockedQtyFromValuation.disable();
            }
            }, function(response) {

            });
        }, this);
        this.loadStores();
        this.addNewAccountBlock();
    },
createPOSTab:function(){
//        var htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.posinterfacetab.northpanel.title"), WtfGlobal.getLocaleText("acc.posinterfacetab.northpanel.maintabview.title"), '../../images/accounitngperiodgrid.jpg', true, '0px 0px 0px 0px');
        var htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.posinterfacetab.northpanel.title"),WtfGlobal.getLocaleText("acc.posinterfacetab.northpanel.bodyTitle"), '../../images/accounitngperiodgrid.jpg', true, '0px 0px 0px 0px');
        //When Multi Group Flag is activated
        this.posNorthPanel = new Wtf.Panel({
            region: "north",
            height: 100,
            border: false,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: htmlDesc
        });

        this.walkIncustmerCmb =CommonERPComponent.createCustomerPagingComboBox(240,450,Wtf.CustomerCombopageSize,this);
        this.walkIncustmerCmb.fieldLabel=WtfGlobal.getLocaleText("acc.accPref.walk-inCustomer") +"*";
        this.walkIncustmerCmb.labelStyle="width:243px";
        this.walkIncustmerCmb.disabled=true;
        this.walkIncustmerCmb.on('invalid',function(comp,value){
            if(comp.errorIcon){
                comp.errorIcon.setLeft((comp.errorIcon.getLeft()-22) - (comp.getEl().getWidth()/2));
            }
        },this);

        this.isAllowCloseRegisterMultipleTimes=new Wtf.form.Checkbox({                   // Allow to user open register multiple times in POS APP
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.isCloseRegisterMultipleTimes"),
            name:'isAllowCloseRegisterMultipleTimes',
            checked:true,
            labelStyle:'width: 243px;',
            disabled:true,
            hideLabel:true,
            hidden:true
        });

        //Store Configs
        var ajaxUrl="ACCPaymentMethods/getPaymentMethods.do";
        var pmParam = {
            populateincpcs:true
        };
        //Payment Method-SIngle select  component
//        this.POSpmtMethod=CommonERPComponent.createPaymentMethodPagingComboBox(this, 240, 450, 30,[], "pmtmethodpos",'methodname',ajaxUrl,Object.assign(new Object(), pmParam),'width: 243px;',WtfGlobal.getLocaleText("acc.mp.payMethod"),false);
        //Payment Method-Multi select  component
        this.POSpmtMethod = CommonERPComponent.createPaymentMethodMultiPagingComboBox(this, 240, 450, 30,WtfGlobal.getLocaleText("acc.mp.payMethod"),'',{},'methodname',ajaxUrl,Object.assign(new Object(), pmParam),'width: 243px;',false);
        this.POSpmtMethod.disabled=true;
        this.POSpmtMethod.emptyText=WtfGlobal.getLocaleText("acc.RestSerivce.selectPaymentMethodEmptyText");
        //Cash In and cash out accounts and params
//        ajaxUrl="ACCAccountCMN/getAccountsForCombo.do";
        ajaxUrl="ACCAccountCMN/getAccountsIdNameForCombo.do";
        //Show only expense type of accounts
        var cashOutBaseParam = {
            grouper	:'paymentTrans',
            ignorecustomers	:true,
            ignorevendors	:true,
            mode            :2,
            nondeleted	:true,
            nature:"2"
        };
        var extraFields = Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'];
        this.cashOutAccount = CommonERPComponent.createAccountPagingComboBox(this, 240, 450, 300, 30, extraFields, Object.assign(new Object(), cashOutBaseParam),"",ajaxUrl);
        this.cashOutAccount.fieldLabel="Cash Out Account*";
        this.cashOutAccount.labelStyle="width:243px";
        this.cashOutAccount.disabled=true;
        this.cashOutAccount.allowBlank=false;//mandatory to fill the details
        this.cashOutAccount.emptyText=WtfGlobal.getLocaleText("acc.RestSerivce.selectCashAccountMsg")+"*";
        //eRROR ICON ON THE SIDE OF COMBO FIELD
        this.cashOutAccount.on('invalid',function(comp,value){
            if(comp.errorIcon){
                comp.errorIcon.setLeft((comp.errorIcon.getLeft()-22) - (comp.getEl().getWidth()/2));
            }
        },this);

        cashOutBaseParam = {
            grouper	:'paymentTrans',
            ignorecustomers	:true,
            ignorevendors	:true,
            nondeleted	:true
        };

        //Deposit Account
        this.depositAccount = CommonERPComponent.createAccountPagingComboBox(this, 240, 450, 300, 30, extraFields, Object.assign(new Object(), cashOutBaseParam),"",ajaxUrl);
        this.depositAccount.fieldLabel=WtfGlobal.getLocaleText("acc.RestSerivce.depositAccountLabel")+"*";
        this.depositAccount.labelStyle="width:243px";
        this.depositAccount.disabled=true;
        this.depositAccount.allowBlank=false;//mandatory to fill the details
        this.depositAccount.emptyText=WtfGlobal.getLocaleText("acc.msgbox.0");
        //eRROR ICON ON THE SIDE OF COMBO FIELD
        this.depositAccount.on('invalid',function(comp,value){
            if(comp.errorIcon){
                comp.errorIcon.setLeft((comp.errorIcon.getLeft()-22) - (comp.getEl().getWidth()/2));
            }
        },this);

        ajaxUrl="INVStore/getStoreList.do";
        var invStoreParams={
            isActive : "true",
            includePickandPackStore:false,
            storeTypes:1
        }
        this.retailStoreComboBox = CommonERPComponent.createStorePagingComboBox(this, 240, 450, 30,'retailCombo','abbr',ajaxUrl, Object.assign(new Object(), invStoreParams),'width: 243px;',"Retail Store ",false);
        this.retailStoreComboBox.on("select",function(combo,record,index){
            if(combo){
                var recparams={}
                recparams.storeid=this.retailStoreComboBox.getValue();
                Wtf.Ajax.requestEx({
                    url: "AccPOSInterface/getERPPOSMappingDetails.do",
                    params: recparams
                }, this,this.genSuccessResPOS);

                this.isAllowCloseRegisterMultipleTimes.enable();
                this.cashOutAccount.enable();
                this.depositAccount.enable();
                this.invoiceSeqMethod.enable();
                this.doSeqMethod.enable();
                this.cnSeqMethod.enable();
                this.srSeqMethod.enable();
                this.POSpmtMethod.enable();
                this.walkIncustmerCmb.enable();
                this.mpSeqMethod.enable();
                this.rpSeqMethod.enable();
                this.salesOrderSeqMethod.enable();
            }
        },this);
//
//        this.retailStoreComboBox.disabled=true;
        //Invoice SeqFormat
        this.invoiceSeqMethod=CommonERPComponent.createSequenceFormatPagingComboBox(this, 240, 450, 30,[], "invoiceseq",'autoinvoice','width: 233px;margin-left: 10px;margin-top: 5px',"Invoice Sequence Format",true,true);
        this.invoiceSeqMethod.disabled=true;
        //DO SeqFormat
        this.doSeqMethod=CommonERPComponent.createSequenceFormatPagingComboBox(this, 240, 450, 30,[], "doseq",'autodo','width: 233px;margin-left: 10px;margin-top: 5px',"Delivery Order Sequence Format",true,true);
        this.doSeqMethod.disabled=true;

        //SalesOrder SEQFORMAT
        this.salesOrderSeqMethod=CommonERPComponent.createSequenceFormatPagingComboBox(this, 240, 450, 30,[], "salesorderseq",'autoso','width: 233px;margin-left: 10px;margin-top: 5px',"Sales Order Sequence Format",true,true);
        this.salesOrderSeqMethod.disabled=true;
        //CN SEQFORMAT
        this.cnSeqMethod=CommonERPComponent.createSequenceFormatPagingComboBox(this, 240, 450, 30,[], "cnseq",'autocreditmemo','width: 233px;margin-left: 10px;margin-top: 5px',"Credit Note Sequence Format",true,true);
        this.cnSeqMethod.disabled=true;
        //SALESRETURN SEQFORMAT
        this.srSeqMethod=CommonERPComponent.createSequenceFormatPagingComboBox(this, 240, 450, 30,[], "srseq",'autosr','width: 233px;margin-left: 10px;margin-top: 5px',"Sales Return Sequence Format",true,true);
        this.srSeqMethod.disabled=true;

        //MAKE PAYMENT SEQFORMAT
        this.mpSeqMethod=CommonERPComponent.createSequenceFormatPagingComboBox(this, 240, 450, 30,[], "mpseq",'autopayment','width: 233px;margin-left: 10px;margin-top: 5px',"Make Payment Sequence Format",true,true);
        this.mpSeqMethod.disabled=true;

        //RECEIVE PAYMENT SEQFORMAT
        this.rpSeqMethod=CommonERPComponent.createSequenceFormatPagingComboBox(this, 240, 450, 30,[], "rpseq",'autoreceipt','width: 233px;margin-left: 10px;margin-top: 5px',"Receive Payment Sequence Format",true,true);
        this.rpSeqMethod.disabled=true;

        this.mainPOSPanel = new Wtf.Panel({
            autoScroll: true,
            bodyStyle: 'background: none repeat scroll 0 0 #DFE8F6;',
            region: 'center',
            border:false,
            items: [this.posNorthPanel,{
                xtype: 'fieldset',
                autoHeight: true,
                title:"<span wtf:qtip='Store Mappings'>Store Mappings</span>",
                defaults: {
                    anchor: '80%',
                    maxLength: 50
                },
                items: [this.retailStoreComboBox,this.isAllowCloseRegisterMultipleTimes,this.walkIncustmerCmb,this.POSpmtMethod,
                this.cashOutAccount,this.depositAccount,
                {
                    layout:'column',
                    items:[
                    {
                        xtype: 'fieldset',
                        autoHeight: true,
                        columnWidth:.49,
                        title:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.accPref.autoNoDescription") +"'>"+WtfGlobal.getLocaleText("acc.accPref.autoNoGeneration")+" </span>",
                        defaults: {
                            anchor: '100%',
                            maxLength: 50
                        },
                        items: [this.invoiceSeqMethod,this.doSeqMethod,this.salesOrderSeqMethod,this.cnSeqMethod,this.srSeqMethod,this.mpSeqMethod,this.rpSeqMethod]
                    }]
                }]
            }]
        });
    },
    savePOSConfigurations:function(){
        //Does not allow if retailStore config is not given.
        if(this.retailStoreComboBox.getValue()!=""&& this.retailStoreComboBox.getValue()!=null && this.retailStoreComboBox.getValue()!="null"){

            if(this.walkIncustmerCmb.getValue()==="" || this.walkIncustmerCmb.getValue()==null || this.walkIncustmerCmb.getValue()=="null"||this.walkIncustmerCmb.getValue()==undefined){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.RestSerivce.selectWalkinMsg")], 2);
                return;
            }

            if(this.POSpmtMethod.getValue()===""|| this.POSpmtMethod.getValue()==null || this.POSpmtMethod.getValue()=="null"||this.POSpmtMethod.getValue()==undefined){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.RestSerivce.selectpaymentMethodMsg")], 2);
                return;
            }
            if(this.cashOutAccount.getValue()===""|| this.cashOutAccount.getValue()==null || this.cashOutAccount.getValue()=="null"||this.cashOutAccount.getValue()==undefined){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.RestSerivce.selectCashOutAccountMsg")], 2);
                return;
            }
            if(this.depositAccount.getValue()===""|| this.depositAccount.getValue()==null || this.depositAccount.getValue()=="null"||this.depositAccount.getValue()==undefined){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.RestSerivce.selectDepositOutAccountMsg")], 2);
                return;
            }

            var recparams={}
            recparams.walkinCustomer=this.walkIncustmerCmb.getValue();
            recparams.allowcloseregistermultipletimesFlag=this.isAllowCloseRegisterMultipleTimes.getValue();
            recparams.storeid=this.retailStoreComboBox.getValue();
            recparams.paymentMethodId=this.POSpmtMethod.getValue();
            recparams.cashOutAccountId=this.cashOutAccount.getValue();
            recparams.invoicesequenceformat=this.invoiceSeqMethod.getValue();
            recparams.dosequenceformat=this.doSeqMethod.getValue();
            recparams.srsequenceformat=this.srSeqMethod.getValue();
            recparams.cnsequenceformat=this.cnSeqMethod.getValue();
            recparams.mpsequenceformat=this.mpSeqMethod.getValue();
            recparams.rpsequenceformat=this.rpSeqMethod.getValue();
            recparams.depositAccountId=this.depositAccount.getValue();
            recparams.salesordersequenceformat=this.salesOrderSeqMethod.getValue();
            Wtf.Ajax.requestEx({
                url: "AccPOSInterface/savePOSCompanyWizardSettings.do",
                params: recparams
            }, this,this.genSuccessRes);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.pos.selectstoremsg")], 2);
        }
    },
    genSuccessResPOS: function (response, request) {
        if (response.success && response.totalCount>0 && response.data) {
            var dataArr = response.data || [];
            for(var i=0;i<dataArr.length;i++){
                var rec = dataArr[i];
                this.cashOutAccount.setValForRemoteStore(rec.cashOutAccountId, rec.cashOutAccountName);
                this.depositAccount.setValForRemoteStore(rec.depositAccountId, rec.depositAccountName);
                this.invoiceSeqMethod.setValForRemoteStore(rec.invoicesequenceformat, rec.invoicesequenceformatName);
                this.doSeqMethod.setValForRemoteStore(rec.dosequenceformat, rec.dosequenceformatName);
                this.cnSeqMethod.setValForRemoteStore(rec.cnsequenceformat, rec.cnsequenceformatName);
                this.srSeqMethod.setValForRemoteStore(rec.srsequenceformat, rec.srsequenceformatName);
                this.mpSeqMethod.setValForRemoteStore(rec.mpsequenceformat, rec.mpsequenceformatName);
                this.rpSeqMethod.setValForRemoteStore(rec.rpsequenceformat, rec.rpsequenceformatName);
                this.rpSeqMethod.setValForRemoteStore(rec.rpsequenceformat, rec.rpsequenceformatName);
                this.salesOrderSeqMethod.setValForRemoteStore(rec.salesordersequenceformat, rec.salesordersequenceformatName);
                this.POSpmtMethod.setValForRemoteStore(rec.paymentMethodId, rec.paymentMethodName);
                this.walkIncustmerCmb.setValForRemoteStore(rec.walkinCustomer, rec.walkinCustomerName);
                this.isAllowCloseRegisterMultipleTimes.setValue(rec.allowcloseregistermultipletimesFlag);
            }
            }else{
                this.cashOutAccount.setValue("");
                this.depositAccount.setValue("");
                this.invoiceSeqMethod.setValue("");
                this.doSeqMethod.setValue("");
                this.cnSeqMethod.setValue("");
                this.srSeqMethod.setValue("");
                this.POSpmtMethod.setValue("");
                this.walkIncustmerCmb.setValue("");
                this.rpSeqMethod.setValue("");
                this.mpSeqMethod.setValue("");
                this.salesOrderSeqMethod.setValue("");
                this.isAllowCloseRegisterMultipleTimes.setValue(false);
            }

    },
    checkdefaultlocationmappedforStore: function (store,currentStoreid) { //ERM-691 checking if a default location is set for the scrap store
        if (store !== undefined && store !== "") {
            var selectedrecord = WtfGlobal.searchRecord(store, currentStoreid, 'store_id');
            if (selectedrecord !== undefined && selectedrecord.data.defaultlocationid == "") {
                WtfComMsgBox(["Info", WtfGlobal.getLocaleText("acc.companypreferences.nodefaultlocation"), ], 0);
                this.scrapStoreCombo.reset();
            }
        }
    },
    checkbeforeQAStoreChange:function(isQainspectionFlow,store,isRepair,isQA){
        if(store != undefined && store!=""){
            Wtf.Ajax.requestEx({
                url:"INVStockLevel/isAnyStockLiesInStore.do",
                params: {
                    storeid: store,
                    isQainspectionFlow:isQainspectionFlow,
                    isRepair:isRepair,
                    isQA:isQA
                }
            },
            this,
            function(result, req){
                if(result.success){
                    var isStockPresent=result.data.isStockPresent;
                    if(isStockPresent == true){
                        if(!isRepair && !isQainspectionFlow){
                            this.inspectionStoreCombo.setValue(store);
//                            this.inspectionStoreCombo.disable();
                        }else if(isRepair){
                            this.repairStoreCombo.setValue(store);
//                            this.repairStoreCombo.disable();
                        }else{
                            this.qaApprovalFlow.setValue(isQainspectionFlow);
                        }
                        WtfComMsgBox(["Info",WtfGlobal.getLocaleText("acc.inventory.inspectionflow.warning"),],0);
                        return false;
                    }
                }
                else if(result.success==false){
                    this.stockINQAApproval.setValue(false);
                    this.stockINQAApproval.disable();
                    this.interStoreReturnQAApproval.setValue(false);
                    this.interStoreReturnQAApproval.disable();
                    this.stockRequestReturnQAApproval.setValue(false);
                    this.stockRequestReturnQAApproval.disable();
                    return true;
                }
            },
            function(result, req){
                return false;
            });
        }
    },
    checkStoreUsedInTransaction: function(sourceCombo) {
        if (this.inspectionStoreCombo.getValue() != "" || this.vendorJobOrderStore.getValue() != "" || this.packingstore.getValue() != "") {
            var storeID="";
            var isJobOrderCmb=false;
            var isQACmb=false;
            var isPackCmb=false;
            if(sourceCombo=="fromJobOrder"){
                storeID=this.vendorJobOrderStore.getValue();
                isJobOrderCmb=true;
            }else if(sourceCombo=="fromQA"){
                storeID=this.inspectionStoreCombo.getValue();
                isQACmb=true;
            }else if(sourceCombo=="fromPack"){
                storeID=this.packingstore.getValue();
                isPackCmb=true;
            }

            Wtf.Ajax.requestEx({
                url: "ACCInvoice/isStoreUsedInTransaction.do",
                params: {
                    storeid: storeID
                }
            },
            this,
            function(result, req) {
                if (result.success) {
                    var isUsedInTransaction = result.isUsedInTransaction;
                            if (isUsedInTransaction) {
                                WtfComMsgBox(["Info", WtfGlobal.getLocaleText("acc.field.SystemControls.pickpackwarehouse"), ], 0);
                                if(isQACmb){
                                 this.inspectionStoreCombo.setValue("");
                                }else if(isPackCmb){
                                 this.packingstore.setValue("");
                                }else if(isJobOrderCmb){
                                  this.vendorJobOrderStore.setValue("");
                                }
                                return false;
                            }
                 }
              }
            , function(result, req) {
                return false;
            });
        }

    },
    validateStoreSelection: function(combo, record, index, sourceCombo) {
        if (record != null && record != undefined && combo.getValue()!="") {
            var showWarning = false;
            if (sourceCombo == "fromQA") {
                if (combo.getValue() == this.vendorJobOrderStore.getValue() || combo.getValue() == this.packingstore.getValue()) {
                    showWarning = true;
                }
            } else if (sourceCombo == "fromJobOrder") {
                if (combo.getValue() == this.inspectionStoreCombo.getValue() || combo.getValue() == this.packingstore.getValue()) {
                    showWarning = true;
                }
            } else if (sourceCombo == "fromPack") {
                if (combo.getValue() == this.inspectionStoreCombo.getValue() || combo.getValue() == this.vendorJobOrderStore.getValue()) {
                    showWarning = true;
                }
            }
            if (showWarning) {
                WtfComMsgBox(["Info", WtfGlobal.getLocaleText("acc.field.SystemControls.pickpackwarehouse"), ], 0);
                combo.setValue("");
                return false;
            }
        }
    },
    getAllInventorySequFormat:function(){
        Wtf.Ajax.requestEx({
            url:"INVSeq/getSeqFormatsPref.do",
            params: {
                activeInv:Wtf.account.companyAccountPref.activateInventoryTab,
                isActive:"true"
            }
        },
        this,
        function(result, req){
            if(result.success){
                if(result.data!=undefined){
                    this.instLocation.setValue(result.data.instlocation.toString());
                    this.instStore.setValue(result.data.inststore.toString());
                    this.instStockIssue.setValue(result.data.stkissue);
                    this.autoStockAdj.setValue(result.data.stockadj);
                    this.autoStockRequest.setValue(result.data.stkrequest);
                    this.autoCycleCount.setValue(result.data.cyclecount);
                    this.skuSequence.setValue(result.data.skufiled);

                    this.instLocation.setTooltip(result.data.instlocation.toString());
                    this.instStore.setTooltip(result.data.inststore.toString());
                    this.instStockIssue.setTooltip(result.data.stkissue.toString());
                    this.autoStockAdj.setTooltip(result.data.stockadj.toString());
                    this.autoStockRequest.setTooltip(result.data.stkrequest.toString());
                    this.skuSequence.setValue(result.data.skufiled);
                }
            }
        },
        function(result, req){
            return false;
        });
    },
    createDocumentEmailSettingFields:function(){
         // Purchase Requisition
        this.purchaseReqGenerationMail= new Wtf.form.Checkbox({
            name:'purchaseReqGenerationMail',
            id:this.id+'purchaseReqGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendPRGenerationMail"),
            checked :Wtf.account.companyAccountPref.purchaseReqGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        this.purchaseReqUpdationMail= new Wtf.form.Checkbox({
            name:'purchaseReqUpdationMail',
            id:this.id+'purchaseReqUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendPRUpdationMail"),
            checked :Wtf.account.companyAccountPref.purchaseReqUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        //Vendor Quotation
        this.vendorQuotationGenerationMail= new Wtf.form.Checkbox({
            name:'vendorQuotationGenerationMail',
            id:this.id+'vendorQuotationGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendVQGenerationMail"),
            checked :Wtf.account.companyAccountPref.vendorQuotationGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        this.vendorQuotationUpdationMail= new Wtf.form.Checkbox({
            name:'vendorQuotationUpdationMail',
            id:this.id+'vendorQuotationUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendVQUpdationMail"),
            checked :Wtf.account.companyAccountPref.vendorQuotationUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        //Purchase Order
        this.purchaseOrderGenerationMail= new Wtf.form.Checkbox({
            name:'purchaseOrderGenerationMail',
            id:this.id+'purchaseOrderGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendPOGenerationMail"),
            checked :Wtf.account.companyAccountPref.purchaseOrderGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        this.purchaseOrderUpdationMail= new Wtf.form.Checkbox({
            name:'purchaseOrderUpdationMail',
            id:this.id+'purchaseOrderUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendPOUpdationMail"),
            checked :Wtf.account.companyAccountPref.purchaseOrderUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        //Goods Receipt Order
        this.goodsReceiptGenerationMail= new Wtf.form.Checkbox({
            name:'goodsReceiptGenerationMail',
            id:this.id+'goodsReceiptGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendGRGenerationMail"),
            checked :Wtf.account.companyAccountPref.goodsReceiptGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        this.goodsReceiptUpdationMail= new Wtf.form.Checkbox({
            name:'goodsReceiptUpdationMail',
            id:this.id+'goodsReceiptUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendGRUpdationMail"),
            checked :Wtf.account.companyAccountPref.goodsReceiptUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });
        //Purchase Return
        this.purchaseReturnGenerationMail= new Wtf.form.Checkbox({
            name:'purchaseReturnGenerationMail',
            id:this.id+'purchaseReturnGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendPR1GenerationMail"),
            checked :Wtf.account.companyAccountPref.purchaseReturnGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        this.purchaseReturnUpdationMail= new Wtf.form.Checkbox({
            name:'purchaseReturnUpdationMail',
            id:this.id+'purchaseReturnUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendPR1UpdationMail"),
            checked :Wtf.account.companyAccountPref.purchaseReturnUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });
        //Vendor Payment
        this.vendorPaymentGenerationMail= new Wtf.form.Checkbox({
            name:'vendorPaymentGenerationMail',
            id:this.id+'vendorPaymentGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.vendorPaymentGenerationMail"),
            checked :Wtf.account.companyAccountPref.vendorPaymentGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        this.vendorPaymentUpdationMail= new Wtf.form.Checkbox({
            name:'vendorPaymentUpdationMail',
            id:this.id+'vendorPaymentUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.vendorPaymentUpdationMail"),
            checked :Wtf.account.companyAccountPref.vendorPaymentUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });
        //Debit Note
        this.debitNoteGenerationMail= new Wtf.form.Checkbox({
            name:'debitNoteGenerationMail',
            id:this.id+'debitNoteGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.debitNoteGenerationMail"),
            checked :Wtf.account.companyAccountPref.debitNoteGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        this.debitNoteUpdationMail= new Wtf.form.Checkbox({
            name:'debitNoteUpdationMail',
            id:this.id+'debitNoteUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.debitNoteUpdationMail"),
            checked :Wtf.account.companyAccountPref.debitNoteUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });
    // ****************************************Sales side Email Notification Setting********************
        //Customer Quotation
        this.customerQuotationGenerationMail= new Wtf.form.Checkbox({
            name:'customerQuotationGenerationMail',
            id:this.id+'customerQuotationGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCQGenerationMail"),
            checked :Wtf.account.companyAccountPref.customerQuotationGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        this.customerQuotationUpdationMail= new Wtf.form.Checkbox({
            name:'customerQuotationUpdationMail',
            id:this.id+'customerQuotationUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCQUpdationMail"),
            checked :Wtf.account.companyAccountPref.customerQuotationUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        //Sales Order
        this.salesOrderGenerationMail= new Wtf.form.Checkbox({
            name:'salesOrderGenerationMail',
            id:this.id+'salesOrderGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendSOGenerationMail"),
            checked :Wtf.account.companyAccountPref.salesOrderGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        this.salesOrderUpdationMail= new Wtf.form.Checkbox({
            name:'salesOrderUpdationMail',
            id:this.id+'salesOrderUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendSOUpdationMail"),
            checked :Wtf.account.companyAccountPref.salesOrderUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        //Delevery Order
        this.deleveryOrderGenerationMail= new Wtf.form.Checkbox({
            name:'deleveryOrderGenerationMail',
            id:this.id+'deleveryOrderGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendDOGenerationMail"),
            checked :Wtf.account.companyAccountPref.deleveryOrderGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        this.deleveryOrderUpdationMail= new Wtf.form.Checkbox({
            name:'deleveryOrderUpdationMail',
            id:this.id+'deleveryOrderUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendDOUpdationMail"),
            checked :Wtf.account.companyAccountPref.deleveryOrderUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        //Sales Return
        this.salesReturnGenerationMail= new Wtf.form.Checkbox({
            name:'salesReturnGenerationMail',
            id:this.id+'salesReturnGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendSRGenerationMail"),
            checked :Wtf.account.companyAccountPref.salesReturnGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        this.salesReturnUpdationMail= new Wtf.form.Checkbox({
            name:'salesReturnUpdationMail',
            id:this.id+'salesReturnUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendSRUpdationMail"),
            checked :Wtf.account.companyAccountPref.salesReturnUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        //Receipt Generation
        this.receiptGenerationMail= new Wtf.form.Checkbox({
            name:'receiptGenerationMail',
            id:this.id+'receiptGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendcustomerReceiptGenerationMail"),
            checked :Wtf.account.companyAccountPref.receiptGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        this.receiptUpdationMail= new Wtf.form.Checkbox({
            name:'receiptUpdationMail',
            id:this.id+'receiptUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendcustomerReceiptUpdationMail"),
            checked :Wtf.account.companyAccountPref.receiptUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        //Receipt Generation
        this.creditNoteGenerationMail= new Wtf.form.Checkbox({
            name:'creditNoteGenerationMail',
            id:this.id+'creditNoteGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendcreditNoteGenerationMail"),
            checked :Wtf.account.companyAccountPref.creditNoteGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        this.creditNoteUpdationMail= new Wtf.form.Checkbox({
            name:'creditNoteUpdationMail',
            id:this.id+'creditNoteUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendcreditNoteUpdationMail"),
            checked :Wtf.account.companyAccountPref.creditNoteUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        //Lease Fixed Asset Module
        this.leaseQuotationGenerationMail= new Wtf.form.Checkbox({
            name:'leaseQuotationGenerationMail',
            id:this.id+'leaseQuotationGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLQGenerationMail"),
            checked :Wtf.account.companyAccountPref.leaseQuotationGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseQuotationUpdationMail= new Wtf.form.Checkbox({
            name:'leaseQuotationUpdationMail',
            id:this.id+'leaseQuotationUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLQUpdationMail"),
            checked :Wtf.account.companyAccountPref.leaseQuotationUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseOrderGenerationMail= new Wtf.form.Checkbox({
            name:'leaseOrderGenerationMail',
            id:this.id+'leaseOrderGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLOGenerationMail"),
            checked :Wtf.account.companyAccountPref.leaseOrderGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseOrderUpdationMail= new Wtf.form.Checkbox({
            name:'leaseOrderUpdationMail',
            id:this.id+'leaseOrderUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLOUpdationMail"),
            checked :Wtf.account.companyAccountPref.leaseOrderUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseDeliveryOrderGenerationMail= new Wtf.form.Checkbox({
            name:'leaseDeliveryOrderGenerationMail',
            id:this.id+'leaseDeliveryOrderGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLDOGenerationMail"),
            checked :Wtf.account.companyAccountPref.leaseDeliveryOrderGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseDeliveryOrderUpdationMail= new Wtf.form.Checkbox({
            name:'leaseDeliveryOrderUpdationMail',
            id:this.id+'leaseDeliveryOrderUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLDOUpdationMail"),
            checked :Wtf.account.companyAccountPref.leaseDeliveryOrderUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseReturnGenerationMail= new Wtf.form.Checkbox({
            name:'leaseReturnGenerationMail',
            id:this.id+'leaseReturnGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLSRGenerationMail"),
            checked :Wtf.account.companyAccountPref.leaseReturnGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseReturnUpdationMail= new Wtf.form.Checkbox({
            name:'leaseReturnUpdationMail',
            id:this.id+'leaseReturnUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLSRUpdationMail"),
            checked :Wtf.account.companyAccountPref.leaseReturnUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseInvoiceGenerationMail= new Wtf.form.Checkbox({
            name:'leaseInvoiceGenerationMail',
            id:this.id+'leaseInvoiceGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLeaseInvoiceGenerationMail"),
            checked :Wtf.account.companyAccountPref.leaseInvoiceGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseInvoiceUpdationMail= new Wtf.form.Checkbox({
            name:'leaseInvoiceUpdationMail',
            id:this.id+'leaseInvoiceUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLeaseInvoiceUpdationMail"),
            checked :Wtf.account.companyAccountPref.leaseInvoiceUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseContractGenerationMail= new Wtf.form.Checkbox({
            name:'leaseContractGenerationMail',
            id:this.id+'leaseContractGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLeaseContractGenerationMail"),
            checked :Wtf.account.companyAccountPref.leaseContractGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.leaseContractUpdationMail= new Wtf.form.Checkbox({
            name:'leaseContractUpdationMail',
            id:this.id+'leaseContractUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendLeaseContractUpdationMail"),
            checked :Wtf.account.companyAccountPref.leaseContractUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        //Consign Stock Sales Module

        this.consignmentReqGenerationMail= new Wtf.form.Checkbox({
            name:'consignmentReqGenerationMail',
            id:this.id+'consignmentReqGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCRGenerationMail"),
            checked :Wtf.account.companyAccountPref.consignmentReqGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentReqUpdationMail= new Wtf.form.Checkbox({
            name:'consignmentReqUpdationMail',
            id:this.id+'consignmentReqUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCRUpdationMail"),
            checked :Wtf.account.companyAccountPref.consignmentReqUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentDOGenerationMail= new Wtf.form.Checkbox({
            name:'consignmentDOGenerationMail',
            id:this.id+'consignmentDOGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCDOGenerationMail"),
            checked :Wtf.account.companyAccountPref.consignmentDOGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentDOUpdationMail= new Wtf.form.Checkbox({
            name:'consignmentDOUpdationMail',
            id:this.id+'consignmentDOUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCDOUpdationMail"),
            checked :Wtf.account.companyAccountPref.consignmentDOUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentInvoiceGenerationMail= new Wtf.form.Checkbox({
            name:'consignmentInvoiceGenerationMail',
            id:this.id+'consignmentInvoiceGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendConsignmentSIGenerationMail"),
            checked :Wtf.account.companyAccountPref.consignmentInvoiceGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentInvoiceUpdationMail= new Wtf.form.Checkbox({
            name:'consignmentInvoiceUpdationMail',
            id:this.id+'consignmentInvoiceUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendConsignmentSIUpdationMail"),
            checked :Wtf.account.companyAccountPref.consignmentInvoiceUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentReturnGenerationMail= new Wtf.form.Checkbox({
            name:'consignmentReturnGenerationMail',
            id:this.id+'consignmentReturnGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCSRGenerationMail"),
            checked :Wtf.account.companyAccountPref.consignmentReturnGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentReturnUpdationMail= new Wtf.form.Checkbox({
            name:'consignmentReturnUpdationMail',
            id:this.id+'consignmentReturnUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCSRUpdationMail"),
            checked :Wtf.account.companyAccountPref.consignmentReturnUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
         //Consign Stock Purchase  Module
         this.consignmentPReqGenerationMail= new Wtf.form.Checkbox({
            name:'consignmentPReqGenerationMail',
            id:this.id+'consignmentPReqGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCRGenerationMail1"),
            checked :Wtf.account.companyAccountPref.consignmentPReqGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentPReqUpdationMail= new Wtf.form.Checkbox({
            name:'consignmentPReqUpdationMail',
            id:this.id+'consignmentPReqUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCRUpdationMail1"),
            checked :Wtf.account.companyAccountPref.consignmentPReqUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentPDOGenerationMail= new Wtf.form.Checkbox({
            name:'consignmentPDOGenerationMail',
            id:this.id+'consignmentPDOGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCDOGenerationMail1"),
            checked :Wtf.account.companyAccountPref.consignmentPDOGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentPDOUpdationMail= new Wtf.form.Checkbox({
            name:'consignmentPDOUpdationMail',
            id:this.id+'consignmentPDOUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCDOUpdationMail1"),
            checked :Wtf.account.companyAccountPref.consignmentPDOUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentPInvoiceGenerationMail= new Wtf.form.Checkbox({
            name:'consignmentPInvoiceGenerationMail',
            id:this.id+'consignmentPInvoiceGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendConsignmentPIGenerationMail1"),
            checked :Wtf.account.companyAccountPref.consignmentPInvoiceGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentPInvoiceUpdationMail= new Wtf.form.Checkbox({
            name:'consignmentPInvoiceUpdationMail',
            id:this.id+'consignmentPInvoiceUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendConsignmentPIUpdationMail1"),
            checked :Wtf.account.companyAccountPref.consignmentPInvoiceUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentPReturnGenerationMail= new Wtf.form.Checkbox({
            name:'consignmentPReturnGenerationMail',
            id:this.id+'consignmentPReturnGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCSRGenerationMail1"),
            checked :Wtf.account.companyAccountPref.consignmentPReturnGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.consignmentPReturnUpdationMail= new Wtf.form.Checkbox({
            name:'consignmentPReturnUpdationMail',
            id:this.id+'consignmentPReturnUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCSRUpdationMail1"),
            checked :Wtf.account.companyAccountPref.consignmentPReturnUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        //Asset Module
        this.assetPurchaseReqGenerationMail= new Wtf.form.Checkbox({
            name:'assetPurchaseReqGenerationMail',
            id:this.id+'assetPurchaseReqGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAPRGenerationMail"),
            checked :Wtf.account.companyAccountPref.assetPurchaseReqGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetPurchaseReqUpdationMail= new Wtf.form.Checkbox({
            name:'assetPurchaseReqUpdationMail',
            id:this.id+'assetPurchaseReqUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAPRUpdationMail"),
            checked :Wtf.account.companyAccountPref.assetPurchaseReqUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetVendorQuotationGenerationMail= new Wtf.form.Checkbox({
            name:'assetVendorQuotationGenerationMail',
            id:this.id+'assetVendorQuotationGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAVQGenerationMail"),
            checked :Wtf.account.companyAccountPref.assetVendorQuotationGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetVendorQuotationUpdationMail= new Wtf.form.Checkbox({
            name:'assetVendorQuotationUpdationMail',
            id:this.id+'assetVendorQuotationUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAVQUpdationMail"),
            checked :Wtf.account.companyAccountPref.assetVendorQuotationUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetPurchaseOrderGenerationMail= new Wtf.form.Checkbox({
            name:'assetPurchaseOrderGenerationMail',
            id:this.id+'assetPurchaseOrderGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAPOGenerationMail"),
            checked :Wtf.account.companyAccountPref.assetPurchaseOrderGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetPurchaseOrderUpdationMail= new Wtf.form.Checkbox({
            name:'assetPurchaseOrderUpdationMail',
            id:this.id+'assetPurchaseOrderUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAPOUpdationMail"),
            checked :Wtf.account.companyAccountPref.assetPurchaseOrderUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetPurchaseInvoiceGenerationMail= new Wtf.form.Checkbox({
            name:'assetPurchaseInvoiceGenerationMail',
            id:this.id+'assetPurchaseInvoiceGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAPIGenerationMail"),
            checked :Wtf.account.companyAccountPref.assetPurchaseInvoiceGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetPurchaseInvoiceUpdationMail= new Wtf.form.Checkbox({
            name:'assetPurchaseInvoiceUpdationMail',
            id:this.id+'assetPurchaseInvoiceUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAPIUpdationMail"),
            checked :Wtf.account.companyAccountPref.assetPurchaseInvoiceUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetDisposalInvoiceGenerationMail= new Wtf.form.Checkbox({
            name:'assetDisposalInvoiceGenerationMail',
            id:this.id+'assetDisposalInvoiceGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendADIGenerationMail"),
            checked :Wtf.account.companyAccountPref.assetDisposalInvoiceGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetDisposalInvoiceUpdationMail= new Wtf.form.Checkbox({
            name:'assetDisposalInvoiceUpdationMail',
            id:this.id+'assetDisposalInvoiceUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendADIUpdationMail"),
            checked :Wtf.account.companyAccountPref.assetDisposalInvoiceUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetGoodsReceiptGenerationMail= new Wtf.form.Checkbox({
            name:'assetGoodsReceiptGenerationMail',
            id:this.id+'assetGoodsReceiptGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAGRGenerationMail"),
            checked :Wtf.account.companyAccountPref.assetGoodsReceiptGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetGoodsReceiptUpdationMail= new Wtf.form.Checkbox({
            name:'assetGoodsReceiptUpdationMail',
            id:this.id+'assetGoodsReceiptUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAGRUpdationMail"),
            checked :Wtf.account.companyAccountPref.assetGoodsReceiptUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetDeliveryOrderGenerationMail= new Wtf.form.Checkbox({
            name:'assetDeliveryOrderGenerationMail',
            id:this.id+'assetDeliveryOrderGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendADOGenerationMail"),
            checked :Wtf.account.companyAccountPref.assetDeliveryOrderGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetDeliveryOrderUpdationMail= new Wtf.form.Checkbox({
            name:'assetDeliveryOrderUpdationMail',
            id:this.id+'assetDeliveryOrderUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendADOUpdationMail"),
            checked :Wtf.account.companyAccountPref.assetDeliveryOrderUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetPurchaseReturnGenerationMail= new Wtf.form.Checkbox({
            name:'assetPurchaseReturnGenerationMail',
            id:this.id+'assetPurchaseReturnGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAPR1GenerationMail"),
            checked :Wtf.account.companyAccountPref.assetPurchaseReturnGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetPurchaseReturnUpdationMail= new Wtf.form.Checkbox({
            name:'assetPurchaseReturnUpdationMail',
            id:this.id+'assetPurchaseReturnUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendAPR1UpdationMail"),
            checked :Wtf.account.companyAccountPref.assetPurchaseReturnUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetSalesReturnGenerationMail= new Wtf.form.Checkbox({
            name:'assetSalesReturnGenerationMail',
            id:this.id+'assetSalesReturnGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendASRGenerationMail"),
            checked :Wtf.account.companyAccountPref.assetSalesReturnGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.assetSalesReturnUpdationMail= new Wtf.form.Checkbox({
            name:'assetSalesReturnUpdationMail',
            id:this.id+'assetSalesReturnUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendASRUpdationMail"),
            checked :Wtf.account.companyAccountPref.assetSalesReturnUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        //Migrated Fields

         this.salesInvoiceGenerationMail= new Wtf.form.Checkbox({
            name:'salesInvoiceGenerationMail',
            id:this.id+'salesInvoiceGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCIGenerationMail"),
            checked :Wtf.account.companyAccountPref.salesInvoiceGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        this.purchaseInvoiceGenerationMail= new Wtf.form.Checkbox({
            name:'purchaseInvoiceGenerationMail',
            id:this.id+'purchaseInvoiceGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendVIGenerationMail"),
            checked :Wtf.account.companyAccountPref.purchaseInvoiceGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });

        this.salesInvoiceUpdationMail= new Wtf.form.Checkbox({
            name:'salesInvoiceUpdationMail',
            id:this.id+'salesInvoiceUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCIUpdationMail"),
            checked :Wtf.account.companyAccountPref.salesInvoiceUpdationMail,
            scope:this,
            cls:'checkboxtopPosition',
            autoWidth:true,
            labelStyle:'width: 340px;'
            //anchor:'95%'
        });

        this.purchaseInvoiceUpdationMail= new Wtf.form.Checkbox({
            name:'purchaseInvoiceUpdationMail',
            id:this.id+'purchaseInvoiceUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendVIUpdationMail"),
            checked :Wtf.account.companyAccountPref.purchaseInvoiceUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });
        this.recurringInvoiceMail= new Wtf.form.Checkbox({
            name:'recurringInvoiceMail',
            id:this.id+'recurringInvoiceMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.recurringInvoiceMail"),
            checked :Wtf.account.companyAccountPref.recurringInvoiceMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

        this.consignmentRequestApproval= new Wtf.form.Checkbox({                               //Send mail notfication on CR Approved
            name:'consignmentRequestApproval',
            id:this.id+'consignmentRequestApproval',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendCRAMail"),
            checked :Wtf.account.companyAccountPref.consignmentRequestApproval,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });
        this.qtyBelowReorderLevelMail= new Wtf.form.Checkbox({                               //Send mail notfication when available qty less than Reorder Level
            name:'qtyBelowReorderLevelMail',
            id:this.id+'qtyBelowReorderLevelMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendRLMail"),
            checked :Wtf.account.companyAccountPref.qtyBelowReorderLevelMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
            //anchor:'95%'
        });
        this.RFQGenerationMail= new Wtf.form.Checkbox({                               //Send mail notfication when RFQ Generation
            name:'RFQGenerationMail',
            id:this.id+'RFQGenerationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendRFQGenerationMail"),
            checked :Wtf.account.companyAccountPref.RFQGenerationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });
        this.RFQUpdationMail= new Wtf.form.Checkbox({                               //Send mail notfication when RFQ Updation
            name:'RFQUpdationMail',
            id:this.id+'RFQUpdationMail',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.sendRFQUpdationMail"),
            checked :Wtf.account.companyAccountPref.RFQUpdationMail,
            scope:this,
            labelStyle:'width: 340px;',
            cls:'checkboxtopPosition',
            autoWidth:true
        });

    },
    checkCRMIntegration:function(checkBox,value){
        if(value){
            if(Wtf.isCRMSync){ //When CRM will be subscribed this flag will be t
                Wtf.Ajax.requestEx({
                    url:"ACCCompanyPrefCMN/getSubscribedAppInformation.do",
                    params: {
                        appid : Wtf.appID.CRM
                    }
                },this,function(response){
                   if(response.success && !response.iscurrencysame){
                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.differentcurrency")], 2);
                      this.activateCRMIntegration.setValue(false);
                   }
                },function(){});
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.crmnotsubscrobed")], 2);
                this.activateCRMIntegration.setValue(false);
            }
        }
    },

    checkLMSIntegration:function(checkBox,value){
        if(value){
            if(Wtf.isLMSSync){ //When LMS will be subscribed this flag will be t
                Wtf.Ajax.requestEx({
                    url:"ACCCompanyPrefCMN/getSubscribedAppInformation.do",
                    params: {
                        appid : Wtf.appID.eUnivercity //LMS id is 5
                    }
                },this,function(response){
                   if(response.success && !response.iscurrencysame){
                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.differentcurrencyLMS")], 2);
                      this.activateLMSIntegration.setValue(false);
                   }
                },function(){});
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.lmsnotsubscrobed")], 2);
                this.activateLMSIntegration.setValue(false);
            }
        }
    },

    checkMinMaxOrdering:function(comp,newValue,oldValue){
        if(newValue){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.PleasemakesurethatMinandMaxOrderingQuantityareassignedforProductsinProductMaster")], 2);
        }
    },

    checkSerialUsedOrNot:function(comp,newValue,oldValue){
        if(Wtf.account.companyAccountPref.isUsedSerial || Wtf.account.companyAccountPref.isnegativestockforlocwar){
            comp.setValue(oldValue);
            if(Wtf.account.companyAccountPref.isUsedSerial){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "You cannot change this values serial no functionality is used for some product(s)."], 2);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Activate Negative Stock For Location Warehouse setting is used for the company so cannot use Activate Batches/Activate Serial No or Both settings."], 2);
            }
        }
    },
     checkInventoryModuleDependancies:function(comp,newValue,oldValue){
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            this.openDeactivateInventoryWizard(comp, oldValue);
        }else{
            this.openActivateInventoryWizard(comp, oldValue);
        }
    },

    openActivateInventoryWizard: function(comp, oldValue){
        var wizard = Wtf.getCmp("activateInventoryWizardId");
        if(wizard == null){
            wizard = new Wtf.ActivateInventoryWizard({
                id:'activateInventoryWizardId',
                buttons:[{
                    text:'Confirm',
                    scope: this,
                    handler: function(){
                        wizard.activateInventoryHandler()
                    }
                }, {
                    text:'Cancel',
                    scope: this,
                    handler: function(){
                        wizard.close();
                        comp.setValue(oldValue)
                    }
                }]
            })
        }
        wizard.on('activationSuccess', function(){
            wizard.close();
            Wtf.account.companyAccountPref.activateInventoryTab = true;
            this.genSuccessResponseRule();
        }, this);
        wizard.show();
    },
    openDeactivateInventoryWizard: function(comp, oldValue){
        var wizard = Wtf.getCmp("deactivateInventoryWizardId");
        if(wizard == null){
            wizard = new Wtf.DeactivateInventoryWizard({
                id:'deactivateInventoryWizardId',
                buttons:[{
                        text:'Confirm',
                        scope: this,
                    handler: function(){
                        wizard.deactivateInventoryHandler()
                    }
                }, {
                    text:'Cancel',
                    scope: this,
                    handler: function(){
                        wizard.close();
                        comp.setValue(oldValue)
                    }
                }]
            })
        }
        wizard.on('deactivationSuccess', function(){
            wizard.close();
            Wtf.account.companyAccountPref.activateInventoryTab = false;
            Wtf.getCmp('inventory').setVisible(false);
        }, this);
        wizard.show();
    },
    checkBatchUsedOrNot:function(comp,newValue,oldValue){
        if(Wtf.account.companyAccountPref.isUsedBatch || Wtf.account.companyAccountPref.isnegativestockforlocwar){
            comp.setValue(oldValue);
            if(Wtf.account.companyAccountPref.isUsedBatch){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "You cannot change this values, add Batch functionality is used for some product(s)."], 2);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Activate Negative Stock For Location Warehouse setting is used for the company so cannot use Activate Batches/Activate Serial No or Both settings."], 2);
            }
        }
    },
    checkBatchSerialUsedOrNot:function(comp,newValue,oldValue){
//        if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory){
//            comp.setValue(oldValue);
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Activate Batches/Activate Serial No or Both settings are used for the company so cannot use Activate Negative Stock For Location Warehouse setting."], 2);
//        }else
            if(newValue==true){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"),
                msg:'If you <b>Activate Negative Stock For Location Warehouse</b> then Negative Stock Setting for Delivery Order,Purchase Return,Cash/Credit Sales and Sales Order cannot be set as Block.<div></br><b>Note:</b> Press <b>Yes</b> if you want to proceed futher. Press <b>No</b> if you don\'t want to continue. ',
                width:500,
                buttons: Wtf.MessageBox.YESNO,
                scope:{
                    scopeObject:this
                },
                icon: Wtf.MessageBox.INFO,
                fn: function(btn){
                    if(btn=="yes") {
                        if(this.scopeObject.negativeblockCase.getValue()){
                            this.scopeObject.negativeblockCase.setValue(false);
                            this.scopeObject.negativewarnCase.setValue(true);
                        }
                        if(this.scopeObject.negativeblockCasePR.getValue()){
                            this.scopeObject.negativeblockCasePR.setValue(false);
                            this.scopeObject.negativewarnCasePR.setValue(true);
                        }
                        if(this.scopeObject.negativeblockCaseSO.getValue()){
                            this.scopeObject.negativeblockCaseSO.setValue(false);
                            this.scopeObject.negativewarnCaseSO.setValue(true);
                        }
                        if(this.scopeObject.negativeblockCaseSICS.getValue()){
                            this.scopeObject.negativeblockCaseSICS.setValue(false);
                            this.scopeObject.negativewarnCaseSICS.setValue(true);
                        }
                        /*
                         *If we Activate Negative Stock For Location Warehouse in this case,
                         *we disable Block case for SO,PR,DO and SICS.
                         */
                        this.scopeObject.negativeblockCasePR.disable();
                        this.scopeObject.negativeblockCase.disable();
                        this.scopeObject.negativeblockCaseSO.disable();
                        this.scopeObject.negativeblockCaseSICS.disable();
                         /*
                         *If we Activate Negative Stock For Location Warehouse in this case,
                         *we enable Ignore and Warn case for SO,PR,DO and SICS.
                         */
                        this.scopeObject.negativeignoreCase.enable();
                        this.scopeObject.negativewarnCase.enable();

                        this.scopeObject.negativeignoreCasePR.enable();
                        this.scopeObject.negativewarnCasePR.enable();

                        this.scopeObject.negativeignoreCaseSICS.enable();
                        this.scopeObject.negativewarnCaseSICS.enable();

                        this.scopeObject.negativeignoreCaseSO.enable();
                        this.scopeObject.negativewarnCaseSO.enable();

                    }else{
                        if(this.scopeObject.negativeblockCase.disable()){
                            this.scopeObject.negativeblockCase.enable();
                        }
                        if(this.scopeObject.negativeblockCasePR.disable()){
                            this.scopeObject.negativeblockCasePR.enable();
                        }
                        comp.setValue(oldValue);
                    }
                }
            },this);
        } else if(newValue == false){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"),
                msg:'If you <b>De-Activate Negative Stock For Location Warehouse</b> then Negative Stock Setting for Delivery Order,Purchase Return,Cash/Credit Sales and Sales Order can not be set to Ignore and Warn.<div></br><b>Note:</b> Press <b>Yes</b> if you want to proceed futher. Press <b>No</b> if you don\'t want to continue. ',
                width:500,
                buttons: Wtf.MessageBox.YESNO,
                scope:{
                    scopeObject:this
                },
                icon: Wtf.MessageBox.INFO,
                fn: function(btn){
                    if(btn=="yes") {
                        /*
                         *If we De-Activate Negative Stock For Location Warehouse in this case,
                         *we disable Ignore and Warn and set value "True" to Block case for SO,PR,DO and SICS.
                         */
                        if(this.scopeObject.negativeblockCase.disable()){
                            this.scopeObject.negativeblockCase.enable();
                            this.scopeObject.negativeignoreCase.disable();
                            this.scopeObject.negativewarnCase.disable();
                            this.scopeObject.negativeblockCase.setValue(true);
                        }
                        if(this.scopeObject.negativeblockCasePR.disable()){
                            this.scopeObject.negativeblockCasePR.enable();
                            this.scopeObject.negativeignoreCasePR.disable();
                            this.scopeObject.negativewarnCasePR.disable();
                            this.scopeObject.negativeblockCasePR.setValue(true);
                        }
                        if(this.scopeObject.negativeblockCaseSO.disable()){
                            this.scopeObject.negativeblockCaseSO.enable();
                            this.scopeObject.negativeignoreCaseSO.disable();
                            this.scopeObject.negativewarnCaseSO.disable();
                            this.scopeObject.negativeblockCaseSO.setValue(true);
                        }
                        if(this.scopeObject.negativeblockCaseSICS.disable()){
                            this.scopeObject.negativeblockCaseSICS.enable();
                            this.scopeObject.negativeignoreCaseSICS.disable();
                            this.scopeObject.negativewarnCaseSICS.disable();
                            this.scopeObject.negativeblockCaseSICS.setValue(true);
                        }
                    }else{
                        comp.setValue(oldValue);
                    }
                }
            },this);

        }
    },
    checkLocationUsedOrNot:function(comp,newValue,oldValue){
        if(Wtf.account.companyAccountPref.isUsedLocation){
            comp.setValue(oldValue);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "You cannot change this values, add Location functionality is used for some product(s)."], 2);
        }
    },
    checkSecurityCreated:function(comp,newValue,oldValue){
        Wtf.Ajax.requestEx({
            url:"ACCCompanyPrefCMN/checkSecurityGateFunctionalityisusedornot.do"

        },this,function(response){
            if(response.success && response.isCheckSecurityGate){
                comp.setValue(oldValue);
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.securityGate.checkMsg")], 2);
            }
        },function(){});
    },
    checkWarehouseUsedOrNot:function(comp,newValue,oldValue){
        if(Wtf.account.companyAccountPref.isUsedWarehouse){
            comp.setValue(oldValue);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "You cannot change this values, add Warehouse serial no functionality is used for some product(s)."], 2);
        }
    },
    checkRowUsedOrNot:function(comp,newValue,oldValue){
        if(Wtf.account.companyAccountPref.isUsedRow){
            comp.setValue(oldValue);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "You cannot change this values, add Row serial no functionality is used for some product(s)."], 2);
        }
    },
    checkRackUsedOrNot:function(comp,newValue,oldValue){
        if(Wtf.account.companyAccountPref.isUsedRack){
            comp.setValue(oldValue);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "You cannot change this values, add Rack serial no functionality is used for some product(s)."], 2);
        }
    },
    checkBinUsedOrNot:function(comp,newValue,oldValue){
        if(Wtf.account.companyAccountPref.isUsedBin){
            comp.setValue(oldValue);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "You cannot change this values, add Bin serial no functionality is used for some product(s)."], 2);
        }
    },
    getFormFieldLink:function(moduleId,clickText){
        var linkTemplate = new Wtf.XTemplate(
            "<div> &nbsp;</div>",
            '<tpl>',
            "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: callCustomizeViewWindow(\""+moduleId+"\")'wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Clickto")+" \""+clickText+"\"'>"+clickText+"</a>"+"</div>",
            '</tpl>'
            );

        var tplPanel = new Wtf.Panel({
            border:false,
            html:linkTemplate.apply()
        })

        return tplPanel;
    },
    getdownloadCurrencyExchange:function(clickText){
        var linkTemplate = new Wtf.XTemplate(
            "<div> &nbsp;</div>",
            '<tpl>',
            "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: ExchangeRateswindows()'wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Clickto")+" \""+clickText+"\"'>"+clickText+"</a>"+"</div>",
            '</tpl>'
            );

        var tplPanel = new Wtf.Panel({
            border:false,
            html:linkTemplate.apply()
        })

        return tplPanel;
    },
    getCustomizeCurrencySymbolCode:function(clickText){
        var linkTemplate = new Wtf.XTemplate(
            "<div> &nbsp;</div>",
            '<tpl>',
            "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: CustomizeCurrencySymbolCodewindows()'wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Clickto")+" \""+clickText+"\"'>"+clickText+"</a>"+"</div>",
            '</tpl>'
            );

        var tplPanel = new Wtf.Panel({
            border:false,
            html:linkTemplate.apply()
        })

        return tplPanel;
    },
    getLimitedAccounts:function(clickText){
        var linkTemplate = new Wtf.XTemplate(
            "<div> &nbsp;</div>",
            '<tpl>',
            "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: LimitedAccountsWindow()'wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Clickto")+" \""+clickText+"\"'>"+clickText+"</a>"+"</div>",
            '</tpl>'
        );
        var tplPanel = new Wtf.Panel({
            border:false,
            html:linkTemplate.apply()
        });
        return tplPanel;
    },
    getjournalEnteryDimensionforRevaluation:function(clickText){
        var customfieldDimensionTemplate = new Wtf.XTemplate(
            "<div> &nbsp;</div>",
            '<tpl>',
            "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: RevaluationJournalEnteryDimensionWindow()'wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Clickto")+" \""+clickText+"\"'>"+clickText+"</a>"+"</div>",
            '</tpl>'
            );

        var tplPanel = new Wtf.Panel({
            border:false,
            html:customfieldDimensionTemplate.apply()
        })

        return tplPanel;
    },
    getSMTPConfigurationLink:function(clickText){
        var linkTemplate = new Wtf.XTemplate(
            "<div> &nbsp;</div>",
            '<tpl>',
            "<div style='padding-left:1px;'>"+"<ul><li> <a class='tbar-link-text' href='#' onClick='javascript: showAutheticationForm()'wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Clickto")+" \""+clickText+"\"'>"+clickText+"</a> </li></ul>"+"</div>",
            '</tpl>'
            );

        var tplPanel = new Wtf.Panel({
            border:false,
            html:linkTemplate.apply()
        })

        return tplPanel;
    },
    getTermLink:function(module,clickText, isIndiaCompany){
        if(WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.NONE){
            isIndiaCompany=true;
        }
        var linkTemplate = new Wtf.XTemplate(
            "<div> &nbsp;</div>",
            '<tpl>',
            "<div style='padding-left:30px;'>"+"<br><a class='tbar-link-text' href='#' onClick='javascript: callTermWindow(\""+module+"\",true,"+isIndiaCompany+")'wtf:qtip=' \""+clickText+"\"'>"+clickText+"</a>"+"</div>",
        '</tpl>'
            );

        var tplPanel = new Wtf.Panel({
            border:false,
            html:linkTemplate.apply()
        })

        return tplPanel;
    },
    reArrangeAutoNoFieldsTrading : function(checked){
        if(checked){
            this.showFormElement(this.autodo);
            this.showFormElement(this.autogro);

        } else {
            this.hideFormElement(this.autodo);
            this.hideFormElement(this.autogro);
        }
    },
    reArrangeAutoNoFields: function(checked){
        if(checked){
            this.hideFormElement(this.autoso);
            this.hideFormElement(this.autocontract);
            this.hideFormElement(this.autoinvoice);
            this.hideFormElement(this.autocreditmemo);
            this.hideFormElement(this.autoreceipt);
            this.hideFormElement(this.autogoodsreceipt);
            this.hideFormElement(this.autodebitnote);
            this.hideFormElement(this.autopayment);
            this.hideFormElement(this.autopo);
            this.hideFormElement(this.autocashsales);
            this.hideFormElement(this.autocashpurchase);
            this.hideFormElement(this.autovenquotation);
            this.hideFormElement(this.autopurchaserequisition);
            this.hideFormElement(this.autorequestforquotation);
            this.hideFormElement(this.autoquotation);
            this.hideFormElement(this.withInvUpdate);
            this.withInvUpdate.setValue(false);
            this.reArrangeAutoNoFieldsTrading(false);
            this.hideFormElement(this.autodo);
            this.hideFormElement(this.autosr);
            this.hideFormElement(this.autopr);
            this.hideFormElement(this.autogro);
            this.hideFormElement(this.autoproductid);

            this.showFormElement(this.autobillingso);
            this.showFormElement(this.autobillinginvoice);
            this.showFormElement(this.autobillingcreditmemo);
            this.showFormElement(this.autobillingreceipt);
            this.showFormElement(this.autobillingcashsales);
            this.showFormElement(this.autobillinggoodsreceipt);
            this.showFormElement(this.autobillingdebitnote);
            this.showFormElement(this.autobillingpayment);
            this.showFormElement(this.autobillingpo);
            this.showFormElement(this.autobillingcashpurchase);

        //            this.showFormElement(this.autoproductid);
        } else {
            this.showFormElement(this.autoso);
            this.showFormElement(this.autocontract);
            this.showFormElement(this.autoinvoice);
            this.showFormElement(this.autocreditmemo);
            this.showFormElement(this.autoreceipt);
            this.showFormElement(this.autogoodsreceipt);
            this.showFormElement(this.autodebitnote);
            this.showFormElement(this.autopayment);
            this.showFormElement(this.autopo);
            this.showFormElement(this.autocashsales);
            this.showFormElement(this.autocashpurchase);
            this.showFormElement(this.autovenquotation);
            this.showFormElement(this.autopurchaserequisition);
            this.showFormElement(this.autorequestforquotation);
            this.showFormElement(this.autoquotation);
            this.showFormElement(this.autodo);
            this.showFormElement(this.autosr);
            this.showFormElement(this.autopr);
            this.showFormElement(this.autogro);
            this.showFormElement(this.autoproductid);
            this.showFormElement(this.withInvUpdate);
            this.withInvUpdate.setValue(Wtf.account.companyAccountPref.withinvupdate);
            this.reArrangeAutoNoFieldsTrading(Wtf.account.companyAccountPref.withinvupdate);
            this.hideFormElement(this.autobillingso);
            this.hideFormElement(this.autobillinginvoice);
            this.hideFormElement(this.autobillingcreditmemo);
            this.hideFormElement(this.autobillingreceipt);
            this.hideFormElement(this.autobillingcashsales);
            this.hideFormElement(this.autobillinggoodsreceipt);
            this.hideFormElement(this.autobillingdebitnote);
            this.hideFormElement(this.autobillingpayment);
            this.hideFormElement(this.autobillingpo);
            this.hideFormElement(this.autobillingcashpurchase);
        }
    },

    showFormElement:function(obj){
        var cntDiv = obj.container.up('div.x-form-item');
        cntDiv.dom.style.display='block';
        cntDiv.dom.className = WtfGlobal.replaceAll(cntDiv.dom.className, "hidden-from-item", "");
    },
    hideFormElement:function(obj){
        var cntDiv = obj.container.up('div.x-form-item');
        cntDiv.dom.style.display='none';
        cntDiv.dom.className += ' hidden-from-item';
    },

    getBookBeginningYear:function(isfirst){
        var ffyear;
        if(isfirst){
            var cfYear=new Date(Wtf.account.companyAccountPref.fyfrom)
            ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom)
            ffyear=new Date( ffyear.getFullYear(),cfYear.getMonth(),cfYear.getDate()).clearTime()
        }
        else{
            var fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
            ffyear=new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime()
        }

        var data=[];
        var newrec;
        if(ffyear==null||ffyear=="NaN"){
            ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)
            }
        var year=ffyear.getFullYear();
        data.push([0,year])
        if(!(ffyear.getMonth()==0&&ffyear.getDate()==1)){
            data.push([1,year+1]);
            newrec = new Wtf.data.Record({
                id:1,
                yearid:year+1
                })
        }
        if(!isfirst&&this.yearStore.getCount()<2){
            this.yearStore.insert(1,newrec)
        }
        return data;
    },
    addGrid:function(){
        this.lockRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        },

        {
            name: 'islock'
        },
        {name: "endYearId"}, /* Financial Year - End Date (Year) ERP-29582*/
        {name: "adjustmentForTransactionCompleted", type: 'boolean'},
        {name: "documentRevaluationCompleted", type: 'boolean'},
        {name: "inventoryAdjustmentCompleted", type: 'boolean'},
        {name: "assetDepreciationPosted", type: 'boolean'}
        ]);
        this.lockds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.lockRec),
            //            url:Wtf.req.account+'CompanyManager.jsp',
            url : "ACCCompanyPref/getYearLock.do",
            baseParams:{
                mode:94,
                CurrentFinancialYear:WtfGlobal.convertToGenericDate(Wtf.account.companyAccountPref.firstfyfrom),
                CurrentBookingYear:WtfGlobal.convertToGenericDate(Wtf.account.companyAccountPref.bbfrom)
            }
        });
        this.lockds.on('load',this.getBeginningYear,this)
        this.lockds.load();
        this.gridcm= new Wtf.grid.ColumnModel([{
            header:WtfGlobal.getLocaleText("acc.accPref.gridFinancialYear"),  //"Financial Year",
            dataIndex:'endYearId',
            align:'center',
            autoWidth : true,
            renderer:function(v,m,rec) {
                if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                    return (v!=undefined && v!="")?(v+"-"+(v+1)):"";
                }
                return v;
            }
        },{
            header:WtfGlobal.getLocaleText("acc.accPref.gridYearBeginDate"),  //"Year Beginning Date",
            dataIndex:'sdate',
            align:'center',
            autoWidth : true
        },{
            header:WtfGlobal.getLocaleText("acc.accPref.gridYearEndDate"),  //"Year Ending Date",
            align:'center',
            dataIndex:'edate',
            autoWidth : true
        },{
            header:WtfGlobal.getLocaleText("acc.field.close.books.assessment.year"),  //" Assessment Year (AY)
            dataIndex:'assessmentyear',
            align:'center',
            hidden:Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?false:true,
            autoWidth : true
        },this.checkColumn = new Wtf.grid.CheckColumn({
            header: WtfGlobal.getLocaleText("acc.accPref.gridCloseBook"),  //"Close Book",
            align:'center',
            dataIndex: 'islock',
            width: 40
        })
        ]);
        this.grid = new Wtf.grid.EditorGridPanel({
            cls:'vline-on',
            layout:'fit',
            id:'closebooks'+this.helpmodeid,
            autoScroll:true,
            autoHeight: true,
//            height:270,
            width:580,//TODO : IE7 display problem for yearlock grid
            plugins:[this.checkColumn],
            title:WtfGlobal.getLocaleText("acc.rem.48"),  //"Close Book(s)",
            store: this.lockds,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.grid.on('afteredit', this.callYearEndClosingCheckList, this);
        //Mobile Field Setup Grid
        this.mobileFieldModuleStore = new Wtf.data.SimpleStore({
            fields: [{
                name: 'moduleid',
                type:'int'
            },
            {
                name: 'modulename'
            }],
            data :[[Wtf.Acc_Sales_Order_ModuleId,'SalesOrder'],[Wtf.Acc_Invoice_ModuleId ,'Sales Invoice'],[Wtf.Acc_Cash_Sales_ModuleId,'Cash Sales'],[Wtf.Acc_Sales_Return_ModuleId,'Sales Return']]
//            data :[[Wtf.Acc_Sales_Order_ModuleId,'SalesOrder'],[Wtf.Acc_Invoice_ModuleId ,'Sales Invoice'],[Wtf.Acc_Cash_Sales_ModuleId,'Cash Sales'],[Wtf.Acc_Sales_Return_ModuleId,'Sales Return'],[Wtf.Acc_Credit_Note_ModuleId,'Credit Note'],[Wtf.Acc_Product_Master_ModuleId,'Product']]
        });

        this.mobilefieldcolumnmodel=new Wtf.grid.ColumnModel([{
                header:WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName"),
                dataIndex:'modulename',
                align:'left',
                width:150
            },{
                header:WtfGlobal.getLocaleText("acc.field.SummaryView"),
                dataIndex:'summaryView',
                align:'center',
                width:150,
                renderer:function(v,m,rec) {
                return "<a class='tbar-link-text' href='#' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Clickto")+" \""+WtfGlobal.getLocaleText("acc.field.Hide/Show")+" "+WtfGlobal.getLocaleText("acc.field.SummaryView")+" Fields\"'>"+WtfGlobal.getLocaleText("acc.field.Hide/Show")+"</a>";
                }
            },{
                header:WtfGlobal.getLocaleText("acc.field.DetailView"),
                dataIndex:'detailView',
                align:'center',
                width:150,
                renderer:function(v,m,rec) {
                    return "<a class='tbar-link-text' href='#' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Clickto")+" \""+WtfGlobal.getLocaleText("acc.field.Hide/Show")+" "+WtfGlobal.getLocaleText("acc.field.DetailView")+" Fields\"'>"+WtfGlobal.getLocaleText("acc.field.Hide/Show")+"</a>";
                }
            },{
                header:WtfGlobal.getLocaleText("acc.field.EntryView"),
                dataIndex:'addEditView',
                align:'center',
                width:150,
                renderer:function(v,m,rec) {
                    return "<a class='tbar-link-text' href='#' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.Clickto")+" \""+WtfGlobal.getLocaleText("acc.field.Hide/Show")+" "+WtfGlobal.getLocaleText("acc.field.EntryView")+" Fields\"'>"+WtfGlobal.getLocaleText("acc.field.Hide/Show")+"</a>";
                }
            }]);

        this.mobileFieldSetupGrid = new Wtf.grid.EditorGridPanel({
            cls:'vline-on',
            layout:'fit',
            id:'mobilefieldsetup'+this.helpmodeid,
            autoScroll:true,
            autoHeight: true,
            width:700,//TODO : IE7 display problem for yearlock grid
            store:this.mobileFieldModuleStore,
            cm:this.mobilefieldcolumnmodel ,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.mobileFieldSetupGrid.on('cellclick',this.onCellClick, this);

        this.reqRuleRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'level'
        },

        {
            name: 'rule'
        },

        {
            name: 'users'
        },

        {
            name: 'userids'
        },
        ]);
        this.reqRuleds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.reqRuleRec),
            //            url:Wtf.req.account+'CompanyManager.jsp',
            url : "ACCPurchaseOrder/getRequisitionFlowData.do"
        });
        this.reqRuleds.load();
        this.requsitionrulegridcm= new Wtf.grid.ColumnModel([new Wtf.grid.CheckboxSelectionModel({
            singleSelect : true
        }),{
            header:WtfGlobal.getLocaleText("acc.field.Level"),
            dataIndex:'level',
            align:'center',
            autoWidth : true,
            renderer:function(value){
                var res = "Level "+value;
                return res;
            }
        },{
            header:WtfGlobal.getLocaleText("acc.field.Approvers"),  //"Year Ending Date",
            align:'left',
            dataIndex:'users',
            autoWidth : true
        },{
            header:WtfGlobal.getLocaleText("acc.field.Rule"),
            dataIndex:'rule',
            align:'center',
            autoWidth : true
        }]);
        this.addRequisitionRule=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.AddRule"),
            iconCls:getButtonIconCls(Wtf.etype.add),
            tooltip:WtfGlobal.getLocaleText("acc.field.Addruleinrequisitionapprovalflow"),
            handler:this.addRequisitionRule.createDelegate(this,[false])
        });
        this.deleteRequisitionRule=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.DeleteRule"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            tooltip:WtfGlobal.getLocaleText("acc.field.DeleteRulefromRequisitionApprovalFlow"),
            handler:this.deteleRequisitionRule.createDelegate(this,[false])
        });
        this.RequisitionRuleGrid = new Wtf.grid.GridPanel({
            //            cls:'vline-on',
            layout:'fit',
            id:'requisition'+this.helpmodeid,
            autoScroll:true,
//            height:270,
            autoHeight: true,
            width:580,
            title:WtfGlobal.getLocaleText("acc.field.RequisitionApprovalFlow"),
            store: this.reqRuleds,
            tbar : [this.addRequisitionRule,this.deleteRequisitionRule],
            cm: this.requsitionrulegridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

    },
    onCellClick: function(obj,row,column,event) {
        event.stopEvent();
        var el = event.getTarget("a");
        if (el == null) {
            return;
        } else {
            var formrec;
            var type;
            var dataindex = obj.getColumnModel().getDataIndex(column);
            formrec = obj.getStore().getAt(row);
            var moduleid=formrec.data.moduleid;
            if (dataindex == Wtf.mobilefield.detailView) {
                type=Wtf.mobilefield.detailView;
                callMobileFieldCustomizeViewWindow(type,moduleid);
            } else if (dataindex == Wtf.mobilefield.addEditView) {
                type=Wtf.mobilefield.addEditView;
                callMobileFieldCustomizeViewWindow(type,moduleid);
            } else if (dataindex == Wtf.mobilefield.summaryView) {
                type= Wtf.mobilefield.summaryView;
                callMobileFieldCustomizeViewWindow(type,moduleid);
            }
        }
    },
    deteleRequisitionRule : function() {
        var higherLevelRule =false;
        var selectedLevel=this.RequisitionRuleGrid.getSelectionModel().getSelected().data.level;
        for(var cnt=0;cnt<this.reqRuleds.getCount();cnt++)
        {
            if(this.reqRuleds.getAt(cnt).data.level>selectedLevel) {
                higherLevelRule=true;
                break;
            }
        }
        if(higherLevelRule){
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.higherLevelRuleSet"));
            return;
        }
        var rec = this.RequisitionRuleGrid.getSelectionModel().getSelected();
        if(rec) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedrule"),function(btn){
                if(btn=="yes") {
                    var formRecord = this.RequisitionRuleGrid.getSelectionModel().getSelected();
                    Wtf.Ajax.requestEx({
                        url: "ACCPurchaseOrder/deleterequisitionflowlevel.do",
                        params: {
                            id : formRecord.data.id
                        }
                    },this,this.genSuccessResponseRule,this.genFailureResponseRule);
                }
            }, this)
        } else {
            WtfComMsgBox(5,2);
        }
    },
    genSuccessResponseRule : function(response){
        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
            this.reqRuleds.load();
        }, this);
    },
    genFailureResponseRule : function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhiledeletingrule")],2);
    },
    addRequisitionRule : function() {
        var highestLevelExists=0;
        for(var cnt=0;cnt<this.reqRuleds.getCount();cnt++)
        {
            if(this.reqRuleds.getAt(cnt).data.level>highestLevelExists)
                highestLevelExists=this.reqRuleds.getAt(cnt).data.level;
        }
        this.usersRec = new Wtf.data.Record.create([
        {
            name: 'userid'
        },

        {
            name: 'username'
        },

        {
            name: 'fname'
        },

        {
            name: 'lname'
        },

        {
            name: 'image'
        },

        {
            name: 'emailid'
        },

        {
            name: 'lastlogin',
            type: 'date'
        },

        {
            name: 'aboutuser'
        },

        {
            name: 'address'
        },

        {
            name: 'contactno'
        },

        {
            name: 'rolename'
        },

        {
            name: 'roleid'
        }
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
        this.userCombo= new Wtf.common.Select({
            width:150,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Approvers"),
            name:'approver',
            store:this.userds,
            hiddenName:'approver',
            xtype:'select',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            displayField:'fname',
            valueField:'userid',
            mode: 'local',
            allowBlank:false,
            triggerAction:'all',
            typeAhead: true
        });
        this.userds.load();
        this.levelstore = new Wtf.data.SimpleStore({
            fields:['id','level'],
            data: [
            ['1','Level 1'],
            ['2','Level 2'],
            ['3','Level 3'],
            ['4','Level 4'],
            ['5','Level 5'],
            ['6','Level 6'],
            ['7','Level 7'],
            ['8','Level 8'],
            ['9','Level 9'],
            ['10','Level 10']
            ]
        });
        for(var count=highestLevelExists+1;count<=9;count++)
            this.levelstore.remove(this.levelstore.getAt(highestLevelExists+1));
        this.levelCombo=new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Level"), //WtfGlobal.getLocaleText("hrms.common.Gender")+"*",
            hiddenName:'level',
            store:this.levelstore,
            displayField:'level',
            valueField:'id',
            forceSelection: true,
            selectOnFocus:true,
            triggerAction: 'all',
            typeAhead:true,
            mode: 'local',
            width:220,
            allowBlank:false
        });

        this.rulestore = new Wtf.data.SimpleStore({
            fields:['id','rule'],
            data: [
            ['1','is greater than'],
            ['2','is less than'],
            ['3','is equal to'],
            ['4','is in the range']
            ]
        });
        this.ruleCombo=new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Rule"), //WtfGlobal.getLocaleText("hrms.common.Gender")+"*",
            hiddenName:'rule',
            store:this.rulestore,
            displayField:'rule',
            valueField:'id',
            forceSelection: true,
            selectOnFocus:true,
            triggerAction: 'all',
            typeAhead:true,
            allowBlank:true,
            mode: 'local',
            width:220
        });
        this.ruleCombo.on("select",function(combo,record,index){
            if(record.data.id==4)
                this.ulimit.enable();
            else
                this.ulimit.disable();
        },this)

        this.limit = new Wtf.form.NumberField ({
            fieldLabel:WtfGlobal.getLocaleText("acc.ra.value"),
            name:'limitvalue',
            allowBlank:true
        });
        this.ulimit = new Wtf.form.NumberField ({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.UpperLimit"),
            name:'ulimitvalue',
            allowBlank:true,
            disabled : true
        });

        this.RulePanel = new Wtf.Panel({
            autoWidth:true,
            hidden : true,
            border : false,
            layout:'form',
            labelWidth: 125,
            autoHeight:true,
            defaults: {
                anchor:'94%'
            },
            items : [this.ruleCombo,this.limit,this.ulimit]
        })

        this.isReqRule = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Doyouwanttoapplyconditionalrule?"),
            name: 'isrule'
        })

        this.isReqRule.on("check",function(obj,value){
            if(value) {
                this.RulePanel.show();
                this.RulePanel.doLayout();
                this.ruleCombo.allowBlank = false;
                this.limit.allowBlank = false;
                this.ulimit.allowBlank = false;
            } else {
                this.RulePanel.hide();
                this.RulePanel.doLayout();
                this.ruleCombo.allowBlank = true;
                this.limit.allowBlank = true;
                this.ulimit.allowBlank = true;
            }
        },this);

        this.requisitionRuleForm=new Wtf.form.FormPanel({
            //            frame:true,
            url:"ACCPurchaseOrder/addrequisitionflowlevel.do",
            labelWidth: 125,
            border : false,
            autoHeight:true,
            bodyStyle:'padding:5px 5px 0',
            autoWidth:true,
            defaults: {
                anchor:'94%'
            },
            defaultType: 'textfield',
            items:[this.levelCombo,this.userCombo,this.isReqRule,this.RulePanel],
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.field.Save"),
                handler:function(){
                    if(this.requisitionRuleForm.getForm().isValid()) {
                        var gridCount=this.RequisitionRuleGrid.getStore().getCount();
                        var isDuplicateRule=false;
                        for(var cnt=0;cnt<gridCount;cnt++)
                        {
                            if(!this.isReqRule.getValue()) //if NO conditional rule
                            {
                                if((this.levelCombo.getValue() ==this.RequisitionRuleGrid.getStore().getAt(cnt).data.level)&&(this.RequisitionRuleGrid.getStore().getAt(cnt).data.rule)==""){
                                    isDuplicateRule=true;
                                    break;
                                }
                            }else{                    //if conditional rule
                                var levelInGrid= this.RequisitionRuleGrid.getStore().getAt(cnt).data.level;
                                var ruleInGrid= this.RequisitionRuleGrid.getStore().getAt(cnt).data.rule;
                                var newLevel= this.levelCombo.getValue();
                                var newRuleText= this.ruleCombo.getRawValue();
                                var newRule;
                                if(newRuleText=='is greater than')
                                {
                                    newRule= '$$>'+this.limit.getValue();
                                }else if(newRuleText=='is less than'){
                                    newRule= '$$<'+this.limit.getValue();
                                }else if(newRuleText=='is equal to'){
                                    newRule= '$$=='+this.limit.getValue();
                                }else{
                                    newRule= this.limit.getValue()+'<=$$'+' && $$<='+this.ulimit.getValue();
                                }
                                if((newLevel == levelInGrid)&&(ruleInGrid==newRule))
                                {
                                    isDuplicateRule=true;
                                    break;
                                }
                            }
                        }
                        if(isDuplicateRule)
                        {
                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.cp.ruleWithSameConditionAlreadyExists"));
                        }else{
                            this.requisitionRuleForm.getForm().submit({
                                waitMsg:WtfGlobal.getLocaleText("acc.field.SavingFeature..."),
                                baseParams:{
                                    ulimitvalue : this.ulimit.getValue()
                                },
                                scope:this,
                                success:function(f,a){
                                    this.Rulewin.close();
                                    this.reqRuleds.load();
                                    var response = eval('('+a.response.responseText+')')
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.accPref.tabTitle"),response.data.msg],response.success*2+1);
                                },
                                failure:function(f,a){
                                    this.Rulewin.close();
                                    this.genFailureResponse(eval('('+a.response.responseText+')'))
                                    }
                            });
                        }
                    }
                },
                scope:this
            }]
        });
        this.requisitionRuleForm.add({
            xtype:'hidden',
            name:'featureid'
        })

        this.Rulewin=new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.AddRule"),
            closable:true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            //            layout: 'border',
            //            border : false,
            //            width:300,
            width: 455,
            autoHeight:true,
            //            plain:true,
            modal:true,
            buttonAlign : 'right',
            items:this.requisitionRuleForm
        });
        this.Rulewin.show();
    //        if(isEdit)this.form.requisitionRuleForm().loadRecord(rec);
    },

    getBeginningYear:function(){
        this.lockds.each(function(rec){
            var year=rec.data.name;
            rec.set('assessmentyear', (year+1) +"-"+ (year+2));
            var date=new Date(year,this.fmonth.getValue(),this.fdays.getValue());
            rec.set('sdateWithoutFormat',date);
            date=date.dateFormat(WtfGlobal.getOnlyDateFormat(date));
            rec.set('sdate',date);
            date=new Date(++year,this.fmonth.getValue(),this.fdays.getValue());
            date=date.add(Date.DAY, -1);
            rec.set('edateWithoutFormat',date);
            date=date.dateFormat(WtfGlobal.getOnlyDateFormat(date));
            rec.set('edate',date);
        },this)
    },

    getEndingYear:function(a,m,rec) {
        var year=rec.data.name;
        var date=new Date(++year,this.fmonth.getValue(),this.fdays.getValue());
        date=date.add(Date.DAY, -1);
        date=date.dateFormat(WtfGlobal.getOnlyDateFormat(date));
        return date;
    },
    setCurrentYear:function(){
        this.currentyear = new Date(Wtf.account.companyAccountPref.fyfrom).getFullYear();
    //      this.currentyear = Wtf.serverDate?Wtf.serverDate.getFullYear():new Date().getFullYear();
    },

    setData:function(){
        this.bdays.setValue(new Date(Wtf.account.companyAccountPref.bbfrom).getDate());
        this.bmonth.setValue(new Date(Wtf.account.companyAccountPref.bbfrom).getMonth());
        this.byear.setValue(new Date(Wtf.account.companyAccountPref.bbfrom).getFullYear());
        this.finanyear.setValue(new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear());
        this.fdays.setValue(new Date(Wtf.account.companyAccountPref.firstfyfrom).getDate());
        this.fmonth.setValue(new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth());
        this.remitPaymentTo.setValue(Wtf.account.companyAccountPref.remitpaymentto);
        if(Wtf.account.companyAccountPref.approvalMail){
            this.sendMailTo.setValue(Wtf.account.companyAccountPref.sendmailto);
        } else{
            this.sendMailTo.reset();
            this.sendMailTo.disable();
        }
        this.initForClose();
    },

    setMailIds:function(obj,value){
        if(value){
            this.sendMailTo.enable();
            this.sendMailTo.setValue(Wtf.account.companyAccountPref.sendmailto);
        }else{
            this.sendMailTo.setValue("");
            this.sendMailTo.disable();
        }
    },
    /**
     * For IDNIA country if transaction created then "Show vendors address in Document" check restrict ON/OFF
     */
    isPurchaseTransactionCreated: function (obj, newval, oldval) {
        Wtf.Ajax.requestEx({
            url: "ACCCompanyPrefCMN/checkSalesorPurchaseTransactionsPresent.do",
            params: {
                issaleside: false
            }
        }, this, function (response) {
            if (response.success && response.istransacionstpresent) {
                if (obj != undefined) {
                    obj.setValue(oldval);
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.pricebandfunctionality.purchase.systemPreference")], 2);
            }
        }, function () {
        });
    },
    setAccounts:function(){
//        this.OtherCharges.setValue(Wtf.account.companyAccountPref.othercharges)
        this.ForeignExchange.setValue(Wtf.account.companyAccountPref.foreignexchange)
        this.LoanDisbursement.setValue(Wtf.account.companyAccountPref.loandisbursementaccount)
        this.LoanInterestAccount.setValue(Wtf.account.companyAccountPref.loaninterestaccount)
        this.UnrealisedGainLoss.setValue(Wtf.account.companyAccountPref.unrealisedgainloss)
        this.Depreciation.setValue(Wtf.account.companyAccountPref.depreciationaccount)
        this.DiscountReceived.setValue(Wtf.account.companyAccountPref.discountreceived)
        this.DiscountGiven.setValue(Wtf.account.companyAccountPref.discountgiven)

        //        this.Cash.setValue(Wtf.account.companyAccountPref.cashaccount)

        var idx = this.dgStore.find("accountid", Wtf.account.companyAccountPref.cashaccount);
        if(idx != -1){
            this.Cash.setValue(this.dgStore.getAt(idx).get("accountname"));
        }
        this.expenseAccount.setValue(Wtf.account.companyAccountPref.expenseaccount);
        this.liabilityAccount.setValue(Wtf.account.companyAccountPref.liabilityaccount);
        this.customerDefaultAccount.setValue(Wtf.account.companyAccountPref.customerdefaultaccount);
        this.vendorDefaultAccount.setValue(Wtf.account.companyAccountPref.vendordefaultaccount);
        this.wastageDefaultAccount.setValue(Wtf.account.companyAccountPref.wastageDefaultAccount);
        this.invoiceWriteOffAccount.setValue(Wtf.account.companyAccountPref.writeOffAccount);
        this.receiptWriteOffAccount.setValue(Wtf.account.companyAccountPref.receiptWriteOffAccount);

        this.roundingDifferenceAccount.setValue(Wtf.account.companyAccountPref.roundingDifferenceAccount);
        this.adjustmentAccountPayment.setValue(Wtf.account.companyAccountPref.adjustmentAccountPayment);
        this.adjustmentAccountReceipt.setValue(Wtf.account.companyAccountPref.adjustmentAccountReceipt);
        if (Wtf.account.companyAccountPref.inventoryValuationType == Wtf.PERPETUAL_VALUATION_METHOD) {
            if (Wtf.account.companyAccountPref.cogsAcc) {
                this.cogsAcc.setValue(Wtf.account.companyAccountPref.cogsAcc);
            } else {
                this.cogsAcc.setValue('');
            }
            if (Wtf.account.companyAccountPref.stockAdjustmentAcc) {
                this.stockAdjustmentAcc.setValue(Wtf.account.companyAccountPref.stockAdjustmentAcc);
            } else {
                this.stockAdjustmentAcc.setValue('');
            }
            if (Wtf.account.companyAccountPref.inventoryAcc) {

                this.inventoryAcc.setValue(Wtf.account.companyAccountPref.inventoryAcc);
            }
            else {
                this.inventoryAcc.setValue('');
            }
        }
    },
    setRevenueRecognitionAccounts:function(){
        if(Wtf.account.companyAccountPref.isDeferredRevenueRecognition||Wtf.account.companyAccountPref.recurringDeferredRevenueRecognition){
            if(Wtf.account.companyAccountPref.salesAccount!=undefined&&Wtf.account.companyAccountPref.salesAccount!=""&&Wtf.account.companyAccountPref.salesRevenueRecognitionAccount!=undefined&&Wtf.account.companyAccountPref.salesRevenueRecognitionAccount!=""){
                this.salesAcc.enable();
                this.salesRevenueRecognitionAccount.enable();
                this.salesAcc.setValue(Wtf.account.companyAccountPref.salesAccount);
                this.salesRevenueRecognitionAccount.setValue(Wtf.account.companyAccountPref.salesRevenueRecognitionAccount);
            }
        }else{
            this.salesAcc.disable();
            this.salesRevenueRecognitionAccount.disable();
        }
    },
    calFinancialYear:function(){
        this.fyear=this.currentyear
        return true;
    },
    validateFinancialYear:function(){
        var isvaliddate= "";
        isvaliddate= this.checkdate(this.fdays.getValue(),this.fmonth.getValue(),this.finanyear.getValue());
        if(!isvaliddate){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.accPref.msg3") ],2);
            return false;
        }
        this.fdate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
        isvaliddate= this.checkdate(this.bdays.getValue(),this.bmonth.getValue(),this.byear.getValue());
        if(!isvaliddate){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.accPref.msg4") ],2);
            return false;
        }
        this.bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue())//.format(WtfGlobal.getOnlyDateFormat());
        var fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear();
        this.firstfyear=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue()).clearTime()//.format(WtfGlobal.getOnlyDateFormat());
        var nxtfyear=new Date(this.firstfyear).add(Date.YEAR,1).clearTime();
        return true;
    },
    checkOpeningTransactions:function(){
        var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
        var originalbdate=new Date(this.byear.originalValue,this.bmonth.originalValue,this.bdays.originalValue);
        var reqparams={
            transactiondate: WtfGlobal.convertToGenericDate(bdate)
        }
        Wtf.Ajax.requestEx({
            url: "ACCCompanyPref/checkOpeningTransactionsForFirstFinancialYearDate.do",
            params: reqparams
        }, this,this.gensuccessresp,this.genfailureresp);
    },
    gensuccessresp:function(response){
        if(response.msg!="" && response.msg!=null && response.msg!=undefined){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"), //'Alert',
                width: 500,
                msg: response.msg+"<b><br><br>"+WtfGlobal.getLocaleText("acc.msg.DoyouWantToUpdateOpeningTransDate")+WtfGlobal.convertToGenericDate(this.getOpeningTransactionDate())+"?",
                buttons: {
                    yes:WtfGlobal.getLocaleText("acc.msgbox.yes"),
                    no:WtfGlobal.getLocaleText("acc.msgbox.no")
                },
                fn:function(btn){
                    if(btn!="yes"){
                        this.fdays.setValue(this.fdays.originalValue)
                        this.fmonth.setValue(this.fmonth.originalValue)
                        this.finanyear.setValue(this.finanyear.originalValue);
                        this.bmonth.setValue(this.bmonth.originalValue);
                        this.bdays.setValue(this.bdays.originalValue);
                        this.byear.setValue(this.byear.originalValue);
                        this.gridsetvalue();
                        return;
                    }
                    this.updateOpeningTransactionDates();
                    this.gridsetvalue();
                },
                scope:this,
                icon: Wtf.MessageBox.WARNING
            });
        }
    },
    genfailureresp:function(response){
        this.fdays.setValue(this.fdays.originalValue)
        this.fmonth.setValue(this.fmonth.originalValue)
        this.finanyear.setValue(this.finanyear.originalValue);
        this.bmonth.setValue(this.bmonth.originalValue);
        this.bdays.setValue(this.bdays.originalValue);
        this.byear.setValue(this.byear.originalValue);
        this.gridsetvalue();
    },
    updateOpeningTransactionDates:function(){
        var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
        var reqparams={
            bbdate: WtfGlobal.convertToGenericStartDate(bdate),
            transactiondate: WtfGlobal.convertToGenericStartDate(this.getOpeningTransactionDate())
        }
        Wtf.Ajax.requestEx({
            url: "ACCCompanyPref/updateOpeningTransactionDates.do",
            params: reqparams
        }, this,this.generateSucRes,this.generateFailRes);
    },
    generateSucRes:function(response){
        if(response.msg){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.information"),
                msg: response.msg,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO
            });
        }
    },
    generateFailRes:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.accPref.tabTitle"),response.msg],response.success*2+1);
    },
    getOpeningTransactionDate:function(){
        var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
        var monthDateStr=bdate.format('M d');
        var openingDocDate=new Date(monthDateStr+', '+bdate.getFullYear()+' 12:00:00 AM');
        return openingDocDate;
    },
    checkanytransaction:function(){
        var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
        var originalbdate=new Date(this.byear.originalValue,this.bmonth.originalValue,this.bdays.originalValue);
        var currencyparams={
            transactiondate: WtfGlobal.convertToGenericDate(bdate),
            originalbookbdate:WtfGlobal.convertToGenericDate(originalbdate)
        }
        Wtf.Ajax.requestEx({
            //            url:Wtf.req.account+'CompanyManager.jsp',
            url: "ACCCompanyPref/checktransactionforbookbeginningdate.do",
            params: currencyparams
        }, this,this.genresuccess,this.genrefailure);

    },
    checkpreviousyearlock:function(){
        var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
        var fdate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
        var currencyparams={
            transactiondate: WtfGlobal.convertToGenericDate(bdate),
            CurrentFinancialYear:WtfGlobal.convertToGenericDate(fdate),
            CurrentBookingYear:WtfGlobal.convertToGenericDate(bdate),
            Previouslockcheck:true //passing the flag to check the lock period of previous five years
        }
        Wtf.Ajax.requestEx({
            //            url:Wtf.req.account+'CompanyManager.jsp',
            url: "ACCCompanyPref/checkpreviousyearlock.do",
            params: currencyparams
        }, this,this.genresuccess,this.genrefailure);

    },
    genresuccess:function(response){
        if(response.msg){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.information"),
                msg: response.msg,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO
            });
            this.bmonth.setValue(this.bmonth.originalValue);
            this.bdays.setValue(this.bdays.originalValue);
            this.byear.setValue(this.byear.originalValue);
            this.fdays.setValue(this.fdays.originalValue)
            this.fmonth.setValue(this.fmonth.originalValue)
            this.finanyear.setValue(this.finanyear.originalValue);
        }
        this.gridsetvalue();
    },
    genrefailure:function(){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.accPref.tabTitle"),response.msg],response.success*2+1);
        this.byear.setValue(this.byear.originalValue());
        this.finanyear.setValue(this.finanyear.originalValue());
        this.gridsetvalue();
    },
    savecurrencyexchange:function(){
        var currencyparams={
            applydate: WtfGlobal.convertToGenericDate(this.firstfyear)
        }
        this.savecurrencyexchange= Wtf.Ajax.requestEx({
            //            url:Wtf.req.account+'CompanyManager.jsp',
            url: "ACCCurrency/saveApplyDateforExchangeRate.do",
            params: currencyparams
        }, this);

    },

    updateaccounts:function(){//updating creationdate for all Accounts
        var currencyparams={
            applydate: WtfGlobal.convertToGenericDate(this.bdate)
        }
        this.updateaccounts=Wtf.Ajax.requestEx({
            url:"ACCAccount/updateCreationDateforAccounts.do",
            params: currencyparams
        },this);

    },
    updatetaxes:function(){//updating applydate for all Taxes
        var currencyparams={
            applydate: WtfGlobal.convertToGenericDate(this.firstfyear)
        }
        this.updatetaxes=Wtf.Ajax.requestEx({
            url:"ACCTax/updateApplyDateForTaxes.do",
            params: currencyparams
        },this);

    },
    gridsetvalue:function(){ //calling the same function to change the grid value in account preferences
        this.finandate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
        this.bookdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue())
        this.lockds.baseParams.CurrentBookingYear=WtfGlobal.convertToGenericDate(this.bookdate);
        this.lockds.baseParams.CurrentFinancialYear=WtfGlobal.convertToGenericDate(this.finandate);
        this.lockds.load();
    },
    checkdate:function(d,m,y){
        var yl=1900; // least year to consider
        var ym=2100; // most year to consider
        if (m<0 || m>11) return(false);
        if (d<1 || d>31) return(false);
        if (y<yl || y>ym) return(false);
        if (m==3 || m==5 || m==8 || m==10)
            if (d==31) return(false);
        if (m==1)
        {
            var b=parseInt(y/4);
            if (isNaN(b)) return(false);
            if (d>29) return(false);
            if (d==29 && ((y/4)!=parseInt(y/4))) return(false);
        }
        return(true);
    },

    getUpdatedLockDetails:function(){
        var arr=[];
        for(var i=0;i<this.lockds.getCount();i++){
            var rec=this.lockds.getAt(i)
//            if(rec.dirty)
//                rec.data.edate=WtfGlobal.convertToGenericDate(new Date(rec.data.edateWithoutFormat));
//            rec.data.sdate=WtfGlobal.convertToGenericDate(new Date(rec.data.sdateWithoutFormat));
            arr.push(rec.data);
        }
        return arr;
    //        return WtfGlobal.getJSONArray(this.grid,true);
    },

    savePreferences:function(){
        if(this.isGroupCompanyTabFlag){//save settings when group company mapping wizard is saved.
            this.saveGroupCompanyWizardButton();
        }else if(this.isPOSTabFlag){//save settings when pos interface is on
            this.savePOSConfigurations();
        }else{
            if((Wtf.account.companyAccountPref.countryid == '137') && this.enableGST && this.enableGST.getValue()){
                this.gstnumber.allowBlank=false;
                this.taxNumber.allowBlank=false;
                this.gstEffectiveDate.allowBlank=false;
                this.companyuen.allowBlank=false;
//                this.iafversion.allowBlank=false;
                this.gafVersion.allowBlank=false;
                this.badDebtProcessingPeriod.allowBlank=false;
                this.badDebtProcessingType.allowBlank=false;
                this.gstSubmissionPeriod.allowBlank=false;
            }else{
                this.gstnumber.allowBlank=true;
                this.taxNumber.allowBlank=true;
                this.gstEffectiveDate.allowBlank=true;
                this.companyuen.allowBlank=true;
                this.iafversion.allowBlank=true;
                this.badDebtProcessingPeriod.allowBlank=true;
                this.badDebtProcessingType.allowBlank=true;
                this.gstSubmissionPeriod.allowBlank=true;
            }
            /*
      * when multi entity is activated allowBLank true for gstnumber,taxNumber and companyuen
      * because this fields are hidden when multi entity is activated
      */
            if (Wtf.account.companyAccountPref.isMultiEntity) {
                this.gstnumber.allowBlank = true;
                this.taxNumber.allowBlank = true;
                this.companyuen.allowBlank = true;
            }
            if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
                if (this.gstDeactivationDate.getValue() !== "" && this.gstDeactivationDate.getValue() !== "") {
                    if (this.gstDeactivationDate.getValue() <= this.gstEffectiveDate.getValue()) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "GST Deactivation Date must be greater than the Activation Date."], 2);
                        return;
                    }
                }
            }
            //ERM-691 if user has not set a Scrap store but QA flow is enabled then do not allow to save
            if (this.qaApprovalFlow.checked) {
                if (Wtf.account.companyAccountPref.columnPref.scrapStore == undefined && this.scrapStoreCombo.getValue()=="") {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.store.scrapcreation")], 0);
                    return;
                }
            }
            if(this.form.getForm().isValid()===false){
                WtfComMsgBox(2, 2);
                return
            };
            if(this.MRPPrefForm.getForm().isValid()===false){
                WtfComMsgBox(2, 2);
                return
            };
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.showIndiaCompanyPreferencesTab){
                if(this.indiaAccountPref_panel.formIndiaPanel.getForm().isValid()===false){
                    var title=this.GSTPrfTab.title;
                    title=title.replace(/<(?:.|\n)*?>/gm, '');//regex used to remove unwanted HTML code
                    this.GSTPrfTab.setTitle('<font color="red">' + title + '</font>');
                    this.GSTPrfTab.doLayout();
                    WtfComMsgBox(2, 2);
                    return
                };
            }
            var isSingle=this.validateFinancialYear();
            if(this.barcodeGenField.getValue() && this.barcodeType.getValue()=='') {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.rem.243")],0);
                return;
            }
            if(this.dpi.getValue()=='0'){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.error.barcdheight")],0);
                return;
            }
            if(this.barcodeHeight.getValue()=='0'){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.product.barcode.compprefpanel.error.barcddpi")],0);
                return;
            }
            if(!isSingle)return;
            var closingFlag=false;
            this.grid.store.each(function(rec){
                if(rec.data.islock == true){
                    closingFlag=true;
                }
            },this);
            var msg=WtfGlobal.getLocaleText("acc.accPref.msg6");
            if(closingFlag){
                msg=WtfGlobal.getLocaleText("acc.accPref.msg6")+"<br/>"+WtfGlobal.getLocaleText("acc.accPref.closingBooks");
            }
            if(this.viewWidget!=undefined && this.viewWidget.getValue()==true){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.confirm"), //'Alert',
                    width: 500,
                    msg: WtfGlobal.getLocaleText("acc.widget.view.save.msg"),
                    buttons: {
                        yes:WtfGlobal.getLocaleText("acc.common.saveBtn"),
                        no:WtfGlobal.getLocaleText("acc.common.cancelBtn")
                    },
                    fn:function(btn){
                        if(btn!="yes")return;
                        this.makeRequest();
                    },
                    scope:this,
                    icon: Wtf.MessageBox.WARNING
                });
            }else{
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.confirm"), //'Confirm',
                    width: 600,
                    msg: "<div style='padding-left:50px;'>"+msg+"</div>",
                    buttons: {
                        yes:WtfGlobal.getLocaleText("acc.accPref.msg7"),
                        no:WtfGlobal.getLocaleText("acc.common.cancelBtn")
                    },
                    fn:function(btn){
                        if(btn!="yes")return;
                        this.makeRequest();
                    },
                    scope:this,
                    icon: Wtf.MessageBox.WARNING
                });
            }
        }
    },
    ExportAccountPreferencesSettings:function(ExportSettingsOrSequence,type){
        if(ExportSettingsOrSequence=="setting"){
            var align="none,none,none,none";
            var filename="Accounts Preferences Settings";
            var exportUrl="ACCCompanyPref/ExportAccountsPreferencesSettings.do";
            var type=(type=="xls")?"xls":"csv";
            var get=1;
            var  header="Setting_Name,Setting_Value,Customer,Account_Code";
            var nondeleted="true";
            var title="Setting Name,Setting Value,Customer,Account Code";
            var width="100,100,100,100";
            var url = exportUrl+"?filename="+encodeURIComponent(filename)+"+&filetype="+type+"&nondeleted="+nondeleted+"&header="+header+"&title="+encodeURIComponent(title)+"&width="+width+"&get="+get+"&align="+align;//+"&companyprefset="+JSON.stringify(Wtf.account.companyAccountPref);
            Wtf.get('downloadframe').dom.src  = url;
        }else if(ExportSettingsOrSequence=="sequence"){
          var Sequencealign="none,none,none,none,none,none,none,none,none,none,none";
            var Sequencefilename="Sequence Format";
            var SequenceexportUrl="ACCCompanyPref/ExportSequenceFormat.do";
            var Sequencetype=(type=="xls")?"xls":"csv";
            var Sequenceget=1;
            var Sequenceheader="name,prefix,suffix,numberofdigit,startfrom,showleadingzero,modulename,isdefaultformat,showdateinprefix,accountcode,bankname";
            var Sequencenondeleted="true";
            var Sequencetitle="Name,Prefix,Suffix,Number of digit,Start from,Show leading zero,Module name,Is Default format,Show Date in prefix,Account Code,Bank Name";
            var Sequencewidth="75,75,75,75,75,75,75,75,75,75";
            var Sequenceurl = SequenceexportUrl+"?filename="+encodeURIComponent(Sequencefilename)+"+&filetype="+Sequencetype+"&nondeleted="+Sequencenondeleted+"&header="+Sequenceheader+"&title="+encodeURIComponent(Sequencetitle)+"&width="+Sequencewidth+"&get="+Sequenceget+"&align="+Sequencealign;
            Wtf.get('downloadframe').dom.src  = Sequenceurl;
        }else{
            var hideshowalign="none,none,none,none,none,none,none,none";
            var hideshowfilename="Hide/Show Transaction Forms Fields";
            var hideshowexportUrl="ACCCompanyPref/ExportHideShow.do";
            var hideshowtype=(type=="xls")?"xls":"csv";
            var hideshowget=1;
            var hideshowheader="header,modulename,user,ishidden,isformfield,ismanadatory,isusermandatory,readonly";
            var hideshownondeleted="true";
            var hideshowtitle="Header,Module Name,User,Is Hidden,Is Form Field,System Manadatory, Is Manadatory,Read Only";
            var hideshowwidth="75,75,75,75,75,75,75,75,75";
            var hideshowurl = hideshowexportUrl+"?filename="+encodeURIComponent(hideshowfilename)+"+&filetype="+hideshowtype+"&nondeleted="+hideshownondeleted+"&header="+hideshowheader+"&title="+encodeURIComponent(hideshowtitle)+"&width="+hideshowwidth+"&get="+hideshowget+"&align="+hideshowalign;
            Wtf.get('downloadframe').dom.src  = hideshowurl;
        }

    },
    makeRequest:function(){
        var rec=this.requestParameters();
//        var closingFlag=false;
//        this.grid.store.each(function(rec){
//            if(rec.data.islock == true){
//                closingFlag=true;
//            }
//        },this);

//        if(closingFlag){
//            WtfGlobal.setAjaxTimeOutFor30Minutes();
//            var rec=this.requestParameters();
//            Wtf.Ajax.requestEx({
//                //            url: Wtf.req.account+'CompanyManager.jsp',
//                url : "ACCCompanyPrefCMN/getOpeningBalanceBalanceSheet.do",
//                params: rec
//            },this,this.genSuccessResponseOpeningBal,this.genFailureResponse);
//        }else{
            WtfGlobal.setAjaxTimeOutFor30Minutes();
            Wtf.Ajax.requestEx({
                url : "ACCCompanyPrefCMN/saveCompanyAccountPreferences.do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
            //Load all limited account stores
            loadMappedAccountsStore();
            //if the value is less than previous saved value in financial year then save the currency exchange
            this.originalfirstfyear=new Date(this.finanyear.originalValue,this.fmonth.originalValue,this.fdays.originalValue);//called when the current financial year is less than original value
            this.originalBBDate=new Date(this.byear.originalValue,this.bmonth.originalValue,this.bdays.originalValue);//called when the current financial year is less than original value
            if(new Date(this.firstfyear)< new Date(this.originalfirstfyear)){
                if(!Wtf.account.companyAccountPref.activateToDateforExchangeRates)
                    this.savecurrencyexchange();
                this.updatetaxes();//update tax
            }
            if(new Date(this.bdate)< new Date(this.originalBBDate)){
                this.updateaccounts();//update accounts
            }

            var groupcompany=false;
            //If Multigroup of companies check is on
            if(this.GroupCompanyTab!=undefined && this.GroupCompanyTab!="undefined"){
                if(this.items!=undefined && this.items!="undefined"){
                    if(this.items.items[0]!=undefined && this.items.items[0]!="undefined"){
                        if(this.items.items[0].items!=undefined && this.items.items[0].items!="undefined"){
                            groupcompany=true;
                            var subitem=this.items.items[0].items;
                            for(var i=1;i<=subitem.items.length;i++){
                                if(subitem.items[i]){
                                    //                    this.remove(this.items[i]);
                                    subitem.remove(subitem.items[i]);
                                }
                            }
                            Wtf.getCmp('as').remove(this);
                        }
                    }
                }
            }

            var isPosFlag=false;
            //If POS check is on
            if(this.posPanelTab!=undefined && this.posPanelTab!="undefined"){
                if(this.items!=undefined && this.items!="undefined"){
                    if(this.items.items[0]!=undefined && this.items.items[0]!="undefined"){
                        if(this.items.items[0].items!=undefined && this.items.items[0].items!="undefined"){
                            isPosFlag=true;
                            var subitem=this.items.items[0].items;
                            for(var i=1;i<=subitem.items.length;i++){
                                if(subitem.items[i]){
                                    //                    this.remove(this.items[i]);
                                    subitem.remove(subitem.items[i]);
                                }
                            }
                            Wtf.getCmp('as').remove(this);
                        }
                    }
                }
            }

            if(!groupcompany && !isPosFlag){
                this.ownerCt.items.each(function(item){
                    //                if(!(item===this||item.id=="tabdashboard"))
                    if(!(item.id=="tabdashboard"))
                        item.ownerCt.remove(item);
                }, this);
            }
        //}
    },

    requestParameters:function(){
        var columnPref = {};
        var rec = this.form.getForm().getValues();
        if (Wtf.getCmp('pickpackship' + this.id))
        {
            var checkboxEnabled = (Wtf.getCmp('pickpackship' + this.id).getValue())
            if (checkboxEnabled)
            {
                if (rec['pickpackship'] && rec['pickpackship'] == "on")

                {
                    rec.packingstore = Wtf.getCmp('packingstore' + this.id).getValue();
                }
            }
        }
           if (Wtf.getCmp('jobWorkOutFlow'+this.id))
        {
            var checkboxEnabled = (Wtf.getCmp('jobWorkOutFlow'+this.id).getValue())
            if (checkboxEnabled)
            {
                if (rec['jobWorkOutFlow'] && rec['jobWorkOutFlow'] == "on")

                {
                    rec.vendorjoborderstore = Wtf.getCmp('vendorjoborderstore' + this.id).getValue();
                }
            }
        }
        rec.remitpaymentto = this.remitPaymentTo.getValue();
        rec.emailinvoice=false;
        rec.dateOfRegistration= WtfGlobal.convertToGenericDate(this.dateOfRegistration.getValue());
        rec.CSTRegDate= WtfGlobal.convertToGenericDate(this.CSTRegDate.getValue());
        rec.returncode= this.returncode.getValue();
        rec.companyvattinno= this.VATTINNo.getValue();
        rec.companycsttinno=this.CSTTINNo.getValue();
        if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDONESIA)
        {
            rec.companynpwpno=this.NPWPNo.getValue();
            rec.companycountryid=Wtf.account.companyAccountPref.countryid;
        }else{
            rec.companypanno=this.PANNo.getValue();
        }
        rec.companyservicetaxno=this.SERVICENo.getValue();
        rec.companytanno=this.TANNo.getValue();
        rec.companyeccno=this.ECCNo.getValue();
        rec.companytdsinterestrate=this.TDSInterestRate.getValue();

        rec.deductortype=this.deductorTypeCompanyLevel.getValue();
        rec.headofficetanno=this.headofficetanno.getValue();
        rec.commissioneratecode=this.commissionerateCode.getValue();
        rec.commissioneratename=this.commissionerateName.getValue();
        rec.servicetaxregno=this.serviceTaxRegNo.getValue();
        rec.divisioncode=this.divisionCode.getValue();
        rec.rangecode=this.rangeCode.getValue();
        rec.tdsincometaxcircle=this.TDS.getValue();
        rec.tdsrespperson=this.responsiblePerson.getValue();
        rec.tdsresppersonfathersname=this.fathersName.getValue();
        rec.tdsresppersondesignation=this.designation.getValue();
        rec.mode=82;
        //rec.cashaccount=this.Cash.getValue();
        rec.cashaccount = Wtf.account.companyAccountPref.cashaccount;
        //rec.shippingcharges=this.ShippingCharges.getValue();
//        rec.othercharges=this.OtherCharges.getValue();
        if(this.withInv.getValue())
            rec.withoutinventory="on";
        if(this.withInvUpdate.getValue())
            rec.withinvupdate="on";
        if(this.editTransaction.getValue())
            rec.editTransaction="on";
        if(this.editLinkedTransactionQuantity.getValue())
            rec.editLinkedTransactionQuantity="on";
        if(this.editLinkedTransactionPrice.getValue())
            rec.editLinkedTransactionPrice="on";
        if(this.autoPopulateMappedProduct.getValue())
            rec.autoPopulateMappedProduct="on";
        /*
         *putting the value of checkbox(Enable import assembly product without BOM) in request parameter
         */
        if(this.withoutBOM.getValue()){
            columnPref.withoutBOM = true;
        }
        else{
            columnPref.withoutBOM = false;
        }
       if(this.duplicateCustomerPoReferenceNo.getValue()){
            columnPref.customerPoReferenceNo = true;
        }
        else{
            columnPref.customerPoReferenceNo = false;
        }
        /*
         *putting the value of checkbox(Allow Zero Unit Price In Lease Module) in request parameter
         */
        if(this.AllowZeroUnitPriceInLeaseModule.getValue()){
            columnPref.allowZeroUntiPriceInLeaseModule = true;
        }
        else{
            columnPref.allowZeroUntiPriceInLeaseModule = false;
        }
        if(this.mapDefaultPaymentToCustomer.getValue())
        {
            columnPref.mapDefaultPaymentToCustomer = true;
        }
        else{
             columnPref.mapDefaultPaymentToCustomer = false;
        }
        if(this.isDisplayUOM.getValue()){
            columnPref.isDisplayUOM = true;
        }
        else{
            columnPref.isDisplayUOM = false;
        }

        if (this.discountMaster.getValue()) {
            columnPref.discountMaster = true;
        } else {
            columnPref.discountMaster = false;
        }
        if (this.autoLoadInvoiceTermTaxes.getValue()) {
            columnPref.autoLoadInvoiceTermTaxes = true;
        } else {
            columnPref.autoLoadInvoiceTermTaxes = false;
        }

        columnPref = Wtf.integrationFunctions.addIntegrationSettingsIntoColumnPref(columnPref, this);

        if (this.discountOnPaymentTerms.getValue()) {
            columnPref.discountOnPaymentTerms = true;
        } else {
            columnPref.discountOnPaymentTerms = false;
        }
//        if (this.discountInBulkPayment.getValue()) {
//            columnPref.discountInBulkPayment = true;
//        } else {
//            columnPref.discountInBulkPayment = false;
//        }

//        if (this.postingDate.getValue()) {
//            columnPref.isPostingDateCheck = true;
//        } else {
//            columnPref.isPostingDateCheck = false;
//        }
         if (this.differentUOM.getValue()) {            //adding or updating value of differentUOM in columnPref Json column of extracompanypreference table if check is enabled in company preferences  ERM-319
            columnPref.differentUOM = true;
        } else {
            columnPref.differentUOM = false;
        }

        if(this.PeriodicJE.getValue()){
            columnPref.PeriodicJE=true;
        }else{
            columnPref.PeriodicJE=false;
        }
        
        if (this.deductSOBlockedQtyFromValuation.getValue()) {
            columnPref.deductSOBlockedQtyFromValuation = true;
        } else {
            columnPref.deductSOBlockedQtyFromValuation = false;
        }

        if (this.bandsWithSpecialRateForPurchase.getValue()) {            //adding or updating value of differentUOM in columnPref Json column of extracompanypreference table if check is enabled in company preferences  ERM-319
            columnPref.bandsWithSpecialRateForPurchase = true;
        } else {
            columnPref.bandsWithSpecialRateForPurchase = false;
        }

        if(this.versionsListPO.getValue()){
            columnPref.activeVersioningInPurchaseOrder = true;
        }else{
            columnPref.activeVersioningInPurchaseOrder = false;
        }

        /**
         * Recurring sales invoice Memo Settings
         */
        if (this.recuringSalesInvoiceOrignalMemo.getValue()) {
            columnPref.recuringSalesInvoiceMemo = 1;
        } else if (this.recuringSalesInvoiceAsOfMemo.getValue()) {
            columnPref.recuringSalesInvoiceMemo = 2;
        } else if (this.recuringSalesInvoiceOrignalAsOfMemo.getValue()) {
            columnPref.recuringSalesInvoiceMemo = 3;
        }

        if(this.statusOfRequisitionForPO.getValue()){
           columnPref.statusOfRequisitionForPO = true;
        }else{
           columnPref.statusOfRequisitionForPO = false;
        }

        if (this.activateDropShip.getValue()) {
            columnPref.activatedropship = true;
        } else {
            columnPref.activatedropship = false;
        }
         if (this.activateEWayBill.getValue()) {
            columnPref.activateEWayBill = true;
        } else {
            columnPref.activateEWayBill = false;
        }

        if (this.mapTaxesAtProductLevel.getValue()) {
            columnPref.mapTaxesAtProductLevel = true;
        } else {
            columnPref.mapTaxesAtProductLevel = false;
        }

        if (this.sendPendingDocumentsToNextLevel.getValue()) {
            columnPref.sendPendingDocumentsToNextLevel = true;
        } else {
            columnPref.sendPendingDocumentsToNextLevel = false;
        }

//        if (this.allowCustomerVendorPaging.getValue()) {
//            columnPref.allowCustomerVendorPagingEditing = true;
//        }else{
//            columnPref.allowCustomerVendorPagingEditing = false;
//        }
        if (this.allowProductPaging.getValue()) {
            columnPref.allowProductPagingEditing = true;
        }else{
            columnPref.allowProductPagingEditing = false;
        }
//        rec.restrictduplicatebatch = this.restrictDuplicateBatch.getValue();
        if (this.restrictDuplicateBatch.getValue()) {
            columnPref.restrictDuplicateBatch = true;
        }else{
            columnPref.restrictDuplicateBatch = false;
        }

        if(CompanyPreferenceChecks.highlightDepreciatedAssets()){
            columnPref.highlightDepreciatedAssets = true;
        }else{
            columnPref.highlightDepreciatedAssets = false;
        }

        if(this.advanceSearchInDocumentlinking.getValue()){
            columnPref.advanceSearchInDocumentlinking = true;
        }else{
            columnPref.advanceSearchInDocumentlinking = false;
        }
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            if (this.tdsAccount != undefined) {
                columnPref.tdsAccount = this.tdsAccount;
            }
            if (this.tcsAccount != undefined) {
                columnPref.tcsAccount = this.tcsAccount;
            }
        }
        
        if(this.activateBankReconcilitaionDraft){
            columnPref.activateBankReconcilitaionDraft = this.activateBankReconcilitaionDraft.getValue();
        }
        if(this.undeliveredServiceSO){
            columnPref.undeliveredServiceSOOpen = this.undeliveredServiceSO.getValue();
        }
        
        if(this.shipDateConfiguration.getValue())
            rec.shipDateConfiguration="on";
        if(this.unitPriceInDO.getValue())
            rec.unitPriceInDO="on";
        if(this.unitPriceInGR.getValue())
            rec.unitPriceInGR="on";
        if(this.unitPriceInSR.getValue())
            rec.unitPriceInSR="on";
        if(this.unitPriceInPR.getValue())
            rec.unitPriceInPR="on";
        if (this.manyCreditDebit.getValue())
            rec.manyCreditDebit = "on"
        if (this.openPOandSO.getValue())
            rec.openPOandSO = "on"
        if (this.showAddressonPOSOSave.getValue())
            rec.showAddressonPOSOSave = "on"
        if (this.isAutoSaveAndPrintChkBox.getValue())
            rec.isAutoSaveAndPrintChkBox = "on"
        if (this.isShowMarginButton.getValue())
            rec.isShowMarginButton = "on"
        if (this.isCustShipAddressInPurchase.getValue())
            rec.isCustShipAddressInPurchase = "on"
        if(this.deleteTransaction.getValue())
            rec.deleteTransaction="on";
        if(this.ShowPartNumber.getValue())
            rec.partNumber="on";
        if(this.ShowDependentField.getValue())
            rec.dependentField="on";
//        if(this.isAutoGeneratedChequeNumber.getValue())
//            rec.showAutoGeneratedChequeNumber="on";
        if(this.activateCRMIntegration.getValue()){
            rec.activateCRMIntegration="on";
        }
        if(this.activateLMSIntegration.getValue()){
            rec.activateLMSIntegration="on";
        }

         if(this.activateGroupCompanyIntegration.getValue()){
            rec.activateGroupCompanyIntegration="on";
        }

         if(this.integrationWithPOS.getValue()){
            rec.isPOSIntegration="on";
            rec.customerForPOS=this.customerForPOS.getValue();
            rec.vendorForPOS=this.vendorForPOS.getValue();
//            rec.cashoutaccountforpos=this.CreditAccountforPOS.getValue();
            rec.isCloseRegisterMultipleTimes=this.isCloseRegisterMultipleTimes.getValue();
        }
        if(this.integrationWithInventory.getValue()){
            rec.activateInventoryTab="on";
        }
        if(this.inventoryCycleCount.getValue()){
            rec.activateCycleCount="on";
        }
        if(this.isMovementWarehouseFlow.getValue()){
            rec.isMovementWarehouseMapping="on";
        }
        if(this.qaApprovalFlow.getValue()){
            rec.activateQAApprovalFlow=this.qaApprovalFlow.getValue();
            rec.inspectionStore=this.inspectionStoreCombo.getValue();
            rec.repairStore=this.repairStoreCombo.getValue();
            if (this.scrapStoreCombo !== undefined && this.scrapStoreCombo.getValue() !== "") {
                rec.scrapStore = this.scrapStoreCombo.getValue();
                columnPref.scrapStore = this.scrapStoreCombo.getValue();
            }
        }
        rec.sendimportmailto=this.sendImportMailTo.getValue();
        rec.useremails=this.userEmailCombo.getValue();
        rec.salesorderreopen=this.soReopen.getValue();
        rec.isActiveLandingCostOfItem=this.isActiveLandingCostOfItem.getValue();
        columnPref.isLandedCostTermJE=this.isLandedCostTermJE.getValue();
        rec.includeAmountInLimitSI=this.includeAmountInLimitSI.getValue();
        rec.includeAmountInLimitPI=this.includeAmountInLimitPI.getValue();
        rec.includeAmountInLimitSO=this.includeAmountInLimitSO.getValue();
        rec.includeAmountInLimitPO=this.includeAmountInLimitPO.getValue();

        if (Wtf.account.companyAccountPref.isLMSIntegration) {
            rec.liabilityAccountForLMS = this.liabilityAccountForLMS != undefined ? this.liabilityAccountForLMS.getValue() : "";
        }
        rec.fyfrom=WtfGlobal.convertToGenericDate(this.firstfyear);
        rec.firstfyfrom=WtfGlobal.convertToGenericDate(this.firstfyear);
        rec.bbfrom=WtfGlobal.convertToGenericDate(this.bdate);
        rec.data=JSON.stringify(this.getUpdatedLockDetails());
        rec.gstnumber = this.gstnumber.getValue();
        rec.companyuen = this.companyuen.getValue();
        rec.industryCode = this.industryCodeCmb.getValue();
        rec.iafversion = this.iafversion.getValue();
        rec.gafVersion= this.gafVersion.getValue();//ERM-315
        rec.taxNumber = this.taxNumber.getValue();
        rec.gstEffectiveDate = WtfGlobal.convertToGenericDate(this.gstEffectiveDate.getValue());
        rec.gstdeactivationdate = WtfGlobal.convertToGenericDate(this.gstDeactivationDate.getValue());
        rec.enableGST = this.enableGST.getValue();
        rec.isMultiEntity = this.isMultiEntity.getValue();
        rec.isDimensionCreated = Wtf.account.companyAccountPref.isDimensionCreated;
        rec.gstaccountforbaddebt=this.gstAccountForBadDebts!=undefined?this.gstAccountForBadDebts.getValue():"";
        rec.gstbaddebtreleifaccount=this.gstBadDebtsReleifAccount!=undefined?this.gstBadDebtsReleifAccount.getValue():"";
        rec.gstbaddebtrecoveraccount=this.gstBadDebtsRecoverAccount!=undefined?this.gstBadDebtsRecoverAccount.getValue():"";
        rec.gstbaddebtreleifpurchaseaccount=this.gstBadDebtsReleifPurchaseAccount!=undefined?this.gstBadDebtsReleifPurchaseAccount.getValue():"";    //ERP-10400,For Purchase
        rec.gstbaddebtrecoverpurchaseaccount=this.gstBadDebtsRecoverPurchaseAccount!=undefined?this.gstBadDebtsRecoverPurchaseAccount.getValue():"";    //For Purchase
        rec.gstbaddebtsuspenseaccount=this.gstBadDebtsSuspenseAccount!=undefined?this.gstBadDebtsSuspenseAccount.getValue():"";
        rec.inputtaxadjustmentaccount=this.inputTaxAdjustmentAccount!=undefined?this.inputTaxAdjustmentAccount.getValue():"";
        rec.taxCgaMalaysian=this.taxCgaMalaysian!=undefined?this.taxCgaMalaysian.getValue():"";
        rec.outputtaxadjustmentaccount=this.outputTaxAdjustmentAccount!=undefined?this.outputTaxAdjustmentAccount.getValue():"";
        rec.freeGiftJEAccount=this.freeGiftJEAccount!=undefined?this.freeGiftJEAccount.getValue():"";
        rec.badDebtProcessingPeriod=this.badDebtProcessingPeriod!=undefined?this.badDebtProcessingPeriod.getValue():"";
        rec.badDebtProcessingType=this.badDebtProcessingType!=undefined?this.badDebtProcessingType.getValue():"";
        rec.gstSubmissionPeriod=this.gstSubmissionPeriod!=undefined?this.gstSubmissionPeriod.getValue():"";

        rec.DOSettings=this.DOSetting.getValue();
        rec.GRSettings=this.GRSetting.getValue();
        rec.updateStockAdjustmentPrice=this.updateStockAdjustmentPrice.getValue();
        rec.allowCustomerCheckInCheckOut=this.allowCustomerCheckInCheckOut.getValue();
        rec.isDeferredRevenueRecognition=this.isDeferredRevenueRecognition.getValue();
        rec.DashBoardImage=this.DashBoardImage.getValue();
        rec.recurringDeferredRevenueRecognition=this.recurringDeferredRevenueRecognition.getValue();
        rec.salesAccount=this.salesAcc!=undefined?this.salesAcc.getValue():"";
        rec.loandisbursementaccount=this.LoanDisbursement!=undefined?this.LoanDisbursement.getValue():"";
        rec.loandinterestaccount=this.LoanInterestAccount!=undefined?this.LoanInterestAccount.getValue():"";
        rec.salesRevenueRecognitionAccount=this.salesRevenueRecognitionAccount!=undefined?this.salesRevenueRecognitionAccount.getValue():"";
        rec.defaultWarehouse = this.defaultWarehouse!=undefined? this.defaultWarehouse.getValue() : "";
        rec.sendmailto=this.sendMailTo.getValue();
        rec.activateProfitMargin=this.activateProfitMarginField.getValue();
        rec.activateimportForJE=this.activateimportForJE.getValue();
        rec.activateCRblockingWithoutStock=this.activateCRblockingWithoutStock.getValue();
        rec.activatefromdateToDate=this.activatefromdateToDate.getValue();
        rec.isBaseUOMRateEdit=this.uomConversionEditCheck.getValue();
        rec.allowZeroUntiPriceForProduct=this.AllowZeroUnitPrice.getValue();
        rec.enablesalespersonAgentFlow=this.enablesalespersonAgentFlow.getValue();
        rec.viewallexcludecustomerwithoutsalesperson=this.viewallexcludecustomerwithoutsalesperson.getValue();
        rec.BuildAssemblyApprovalFlow=this.buildAssemblyQACheckbox.getValue();
        rec.isAutoRefershReportonSave=this.isAutoRefershReportonSave.getValue();
        rec.isPRmandatory=this.mandatoryPR.getValue();
        rec.defaultsequenceformatforrecinv=this.enableDefaultsqlformatForRecurInvoice.getValue();
        rec.pickaddressfrommaster=this.pickAddressFromMaster.getValue();
        rec.gstIncomeGroup=this.gstIncomeGroupCheck.getValue();
        //rec.paymentMethodAsCard=this.paymentMethodAsCard.getValue();
        rec.jobOrderItemFlow=this.jobOrderItemFlow.getValue();
        rec.usersVisibilityFlow=this.usersVisibilityFlow.getValue();
        rec.usersspecificinfoFlow=this.usersspecificinfoFlow.getValue();
        rec.jobWorkOutFlow=this.jobWorkOutFlow.getValue();
        //set value of Enable Cash Receive Return field flag
        rec.enableCashReceiveReturn=this.enableCashReceiveReturn.getValue();
        rec.propagatetochildcompanies=this.propagateToChildCompanies.getValue();

        rec.invoicesWriteOffAccount = this.invoiceWriteOffAccount!=undefined ? this.invoiceWriteOffAccount.getValue():"";

        rec.receiptWriteOffAccount = this.receiptWriteOffAccount!=undefined ? this.receiptWriteOffAccount.getValue():"";
        if (this.inventoryschema.getValue()) {
            rec.UomSchemaType = 0;
        } else if (this.inventorypackaging.getValue()) {
            rec.UomSchemaType = 1;
        }
        if (this.allautoload.getValue()) {
            rec.ProductSelectionType = 0; //All load
        } else if (this.ontypeahead.getValue()) {
            rec.ProductSelectionType = 1;//type ahead
        } else if(this.onSubmit.getValue()){
            rec.ProductSelectionType =2;//free text search
        }
        //Auto Genrate Purchase Type
        if (this.purchaseOrder.getValue()) {
            rec.autoGenPurchaseType = 0; // Purchase order
        } else if (this.purchaseRequisition.getValue()) {
            rec.autoGenPurchaseType = 1;// Purchase Requisition
        }
        //Inventory Update for Work Order
        if (this.onTaskCompletion.getValue()) {
            rec.woInventoryUpdateType = 0; // On task Completion
        } else if (this.onWorkOrderCompletion.getValue()) {
            rec.woInventoryUpdateType = 1;// on work order completion
        }
        if(this.mrpProductComponentType.getValue())
        {
              rec.mrpProductComponentType =1;

        }
//        if(this.proddiscripritchtextboxflag.getValue()){
//            rec.proddiscripritchtextboxflag=1;
//        }
        rec.enablevatcst=this.enableVATCST.getValue();
        if(this.enableVATCST.getValue()){
            rec.typeofdealer=this.typeOfDealer.getRawValue()==""?"0":this.typeOfDealer.getValue();
            rec.applicabilityofvat=WtfGlobal.convertToGenericDate(this.dateOfApplicability.getValue());
            rec.assessmentcircle=this.assessmentcircle.getValue();
            rec.division=this.division.getValue();
            rec.areacode=this.areaCode.getValue();
            rec.importexportcode=this.importexportcode.getValue();
            rec.authorizedby=this.authrizedby.getValue();
            rec.authrizedperson=this.authrizedperson.getValue();
            rec.statusordesignation=this.statudORdesignation.getValue();
            rec.place=this.place.getValue();
            rec.vattincomposition=this.VATTINcomposition.getValue();
            rec.vattinregular=this.VATTINregular.getValue();
            rec.localsalestaxnumber=this.LocalSalesTaxNumber.getValue();
            rec.interstatesalestaxnumber=this.InterStateSalesTaxNumber.getValue();
            rec.vatPayableAcc=this.vatPayableAcc.getValue();
            rec.vatInCreditAvailAcc=this.vatInCreditAvailAcc.getValue();
            rec.CSTPayableAcc=this.CSTPayableAcc.getValue();
            rec.excisePayableAcc=this.excisePayableAcc.getValue();
            rec.pmtMethod=this.pmtMethod.getValue();
        }
        if(this.tdsFieldset.collapsed== false){
            rec.resposiblePersonstate=this.resposiblePersonstate.getValue();
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            rec.bankid=this.bankCombo.getValue();
            rec.STPayableAcc = this.STPayableAcc.getValue();
            rec.STAdvancePaymentaccount = this.STAdvancePaymentAccount.getValue();
        }
        if (this.productDescInTextAreaCase.getValue()) {
            rec.proddiscripritchtextboxflag = 0;
        } else if (this.productDescInTextBoxCase.getValue()) {
            rec.proddiscripritchtextboxflag = 1;
        } else if (this.productDescInHtmlEditorCase.getValue()) {
            rec.proddiscripritchtextboxflag = 2;
        }

        if (this.productSortByName.getValue()) {
            rec.productsortingflag = 0;
        } else if (this.productSortById.getValue()) {
            rec.productsortingflag = 1;
        }

        if (this.productSearchByStartWith.getValue()) {
            rec.productsearchingflag = 0;
        } else if (this.productSearchByAnywhere.getValue()) {
            rec.productsearchingflag = 1;
        }

        if (this.CustomerVendorSortByName.getValue()) {
            rec.customervendorsortingflag = 0;
        } else if (this.CustomerVendorSortByCode.getValue()) {
            rec.customervendorsortingflag = 1;
        }
        /*this code is for Productcombo description
         * assgin 0 whhen  type is added inthe combo
         * assign 1 when description is add in the combo */
        if (this.Producttype.getValue()) {
             columnPref.Productflag  = Wtf.AccountProducttype;
        } else if (this.Productdescription.getValue()) {
            columnPref.Productflag = Wtf.AccountProcutdescription;
        }

        if (this.AccountSortByName.getValue()) {
            rec.accountsortingflag = 0;
        } else if (this.AccountSortByCode.getValue()) {
            rec.accountsortingflag = 1;
        }

        if(this.companyMail.getValue()){
            rec.defaultmailsenderFlag=0;
        }else if(this.UserMail.getValue()){
            rec.defaultmailsenderFlag=1;
        }
         if(this.currencycode.getValue()){
            rec.isCurrencyCode=true;
        }else if(this.currencyname.getValue()){
            rec.isCurrencyCode=false;
        }
        if (this.ontypeahead_CustVen.getValue()) {
            rec.custvenloadtype = 1;//type ahead
        }

        if(this.downloadglprocessflag.getValue()){
            rec.downloadglprocessflag=1;
        }

        if(this.downloadDimPLprocessflag.getValue()){
            rec.downloadDimPLprocessflag=1;
        }
        if(this.downloadSOAprocessflag.getValue()){
            rec.downloadSOAprocessflag=1;
        }
        if (this.cashSales.getValue()) {
            rec.SalesSelectionType = true;
        } else if (this.creditSales.getValue()) {
            rec.SalesSelectionType = false;
        }
        if (this.cashPurchase.getValue()) {
            rec.PurchaseSelectionType = true;
        } else if (this.creditPurchase.getValue()) {
            rec.PurchaseSelectionType = false;
        }

        if (this.termsincludegst.getValue()) {
            rec.InvoiceTermsSetting = false;
        } else if (this.termsexcludegst.getValue()) {
            rec.InvoiceTermsSetting = false;
        }
        if (this.negativeignoreCase.getValue()) {
            rec.negativestock = 0;
        } else if (this.negativeblockCase.getValue()) {
            rec.negativestock = 1;
        } else if (this.negativewarnCase.getValue()) {
            rec.negativestock = 2;
        }
        if (this.defaultnegativeFormulaSO.getValue()) {
            rec.negativestockformulaso = 0;
        } else if (this.negativeFormulaSO.getValue()) {
            rec.negativestockformulaso = 1;
        }
        if (this.negativeignoreCaseSO.getValue()) {
            rec.negativestockso= 0;
        } else if (this.negativeblockCaseSO.getValue()) {
            rec.negativestockso= 1;
        } else if (this.negativewarnCaseSO.getValue()) {
            rec.negativestockso= 2;
        }
        if (this.defaultnegativeFormulaSI.getValue()) {
            rec.negativestockformulasi = 0;
        } else if (this.negativeFormulaSI.getValue()) {
            rec.negativestockformulasi = 1;
        }
        if (this.negativeignoreCaseSICS.getValue()) {
            rec.negativestocksics = 0;
        } else if (this.negativeblockCaseSICS.getValue()) {
            rec.negativestocksics = 1;
        } else if (this.negativewarnCaseSICS.getValue()) {
            rec.negativestocksics = 2;
        }
        if (this.negativeignoreCasePR.getValue()) {
            rec.negativestockpr = 0;
        } else if (this.negativeblockCasePR.getValue()) {
            rec.negativestockpr = 1;
        } else if (this.negativewarnCasePR.getValue()) {
            rec.negativestockpr = 2;
        }

        /*Customer Credit Control*/
        if (this.ignoreCase.getValue()) {
            rec.custcreditlimit = Wtf.controlCases.IGNORE;
        } else if (this.blockCase.getValue()) {
            rec.custcreditlimit = Wtf.controlCases.BLOCK;
        } else if (this.warnCase.getValue()) {
            rec.custcreditlimit = Wtf.controlCases.WARN;
        }

                /*Customer Order Credit Control*/
        if (this.ignoreCaseorder.getValue()) {
            rec.custcreditlimitorder = Wtf.controlCases.IGNORE;
        } else if (this.blockCaseorder.getValue()) {
            rec.custcreditlimitorder = Wtf.controlCases.BLOCK;
        } else if (this.warnCaseorder.getValue()) {
            rec.custcreditlimitorder = Wtf.controlCases.WARN;
        }

        /*Vendor Order Credit Control*/
        if (this.ignoreCasevendororder.getValue()) {
            rec.vendorcreditlimitorder = Wtf.controlCases.IGNORE;
        } else if (this.blockCasevendororder.getValue()) {
            rec.vendorcreditlimitorder = Wtf.controlCases.BLOCK;
        } else if (this.warnCasevendororder.getValue()) {
            rec.vendorcreditlimitorder = Wtf.controlCases.WARN;
        }
        /*Vendor Debit Control*/
        if (this.vendorCreditIgnoreCase.getValue()) {
            rec.vendorCreditControl = Wtf.controlCases.IGNORE;
        } else if (this.vendorCreditBlockCase.getValue()) {
            rec.vendorCreditControl = Wtf.controlCases.BLOCK;
        } else if (this.vendorCreditWarneCase.getValue()) {
            rec.vendorCreditControl = Wtf.controlCases.WARN;
        }

        if (this.chequeNoIgnoreCase.getValue()) {
            rec.chequeNoDuplicate = 0;
        } else if (this.chequeNoBlockCase.getValue()) {
            rec.chequeNoDuplicate = 1;
        } else if (this.chequeNoWarnCase.getValue()) {
            rec.chequeNoDuplicate = 2;
        }

        rec.accountWithOrWithoutCode = this.withCode.getValue();
        //        rec.showleadingzero=this.showLeadingZero.getValue();

        if (this.budgetIgnoreCase.getValue()) {
            rec.custMinBudget = 0;
        } else if (this.budgetBlockCase.getValue()) {
            rec.custMinBudget = 1;
        } else if (this.budgetWarnCase.getValue()) {
            rec.custMinBudget = 2;
        }

        if(this.viewFlowDiagram.getValue()){
            rec.viewDashboard=0;
        }
        if(this.viewWidget.getValue()){
            rec.viewDashboard=1;
        }
//        if(this.viewGraphicalDashboard.getValue()){
//            rec.viewDashboard=2;
//        }

        if(this.themeSelector.getValue()){
            Wtf.theme = rec.theme = this.themeSelector.getValue();
        }

        if(this.viewDetailsField.getValue()){
            rec.viewDetailsPerm="on";
        }
        if(this.barcodeGenField.getValue()){
            rec.generateBarcodeParm="on";
        }
        if(this.barcodeWithPrice.getValue()){
            rec.generateBarcodeWithPriceParm="on";
        }
        if(this.barcodeWithProdName.getValue()){
            rec.generateBarcodeWithPnameParm="on";
        }
        if (this.barcodeWithMRP.getValue()) {
            rec.generateBarcodeWithMrpParm = "on";
        }
        if(this.barcodeWithPid.getValue()){
            rec.generateBarcodeWithPidParm="on";
        }
        rec.barcodetype=this.barcodeType.getValue();
         rec.pricePrintType=this.pricePrintType.getValue();
        rec.pnamePrintType=this.pnamePrintType.getValue();
        rec.pidPrintType=this.pidPrintType.getValue();
        rec.mrpPrintType=this.mrpPrintType.getValue();
        if(this.SKUField.getValue()){
            rec.SKUFieldParm="on";
        }

        if(this.activatemrpmodule.getValue()){
            rec.activatemrpmodule="on";
        }
        Wtf.viewDashboard = rec.viewDashboard;

        if (this.salesCommissionReportMode0.getValue()) {
            rec.salesCommissionReportMode = 0;
        } else if (this.salesCommissionReportMode1.getValue()) {
            rec.salesCommissionReportMode = 1;
        } else if (this.salesCommissionReportMode2.getValue()) {
            rec.salesCommissionReportMode = 2;
        }
        rec.activateWastageCalculation = this.activateWastageCalculation.getValue();
        rec.calculateproductweightmeasurment = this.calculateProductWeightMeasurment.getValue();
        rec.carryForwardPriceForCrossLinking = this.carryForwardPriceForCrossLinking.getValue();
        rec.bandsWithSpecialRateForSales = this.bandsWithSpecialRateForSales.getValue();
        if (this.periodicInventory.getValue()) {
            rec.inventoryvaluationtype = 0;
        } else if (this.perpetualInventory.getValue()) {
            rec.inventoryvaluationtype = 1;
            rec.inventoryaccountid=this.inventoryAcc.getValue();
            rec.stockadjustmentaccountid=this.stockAdjustmentAcc.getValue();
            rec.cogsaccountid=this.cogsAcc.getValue();
        }
        rec.showIndiaCompanyPreferencesTab=Wtf.showIndiaCompanyPreferencesTab;
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.showIndiaCompanyPreferencesTab){
            if(this.indiaAccountPref_panel.formIndiaPanel!=undefined && this.indiaAccountPref_panel.formIndiaPanel.getForm()!=undefined && this.indiaAccountPref_panel.formIndiaPanel.getForm().getValues()!=undefined){
                var indian_GST_form=this.indiaAccountPref_panel.formIndiaPanel.getForm().getValues();
                try{
                    rec = Wtf.applyIf(rec,indian_GST_form); // marge both form
                 }catch(e){}
            }
        }
        /**
         * Set limited accounts mapping activation flag in columnpref array
         */
        if(Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref != null){
            if(LimitedAccountsWindow.isLimitedCustomerAccounts != undefined && LimitedAccountsWindow.isLimitedCustomerAccounts != null){
                columnPref.isLimitedCustomerAccounts = LimitedAccountsWindow.isLimitedCustomerAccounts;
            } else if(Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts != null){
                columnPref.isLimitedCustomerAccounts = Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts;
            }
            if(LimitedAccountsWindow.isLimitedVendorAccounts != undefined && LimitedAccountsWindow.isLimitedVendorAccounts != null){
                columnPref.isLimitedVendorAccounts = LimitedAccountsWindow.isLimitedVendorAccounts;
            } else if(Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts != null){
                columnPref.isLimitedVendorAccounts = Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts;
            }
            if(LimitedAccountsWindow.isLimitedProductPurchaseAccounts != undefined && LimitedAccountsWindow.isLimitedProductPurchaseAccounts != null){
                columnPref.isLimitedProductPurchaseAccounts = LimitedAccountsWindow.isLimitedProductPurchaseAccounts;
            } else if(Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts != null){
                columnPref.isLimitedProductPurchaseAccounts = Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts;
            }
            if(LimitedAccountsWindow.isLimitedProductSalesAccounts != undefined && LimitedAccountsWindow.isLimitedProductSalesAccounts != null){
                columnPref.isLimitedProductSalesAccounts = LimitedAccountsWindow.isLimitedProductSalesAccounts;
            } else if(Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts != null){
                columnPref.isLimitedProductSalesAccounts = Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts;
            }
        } else{
            /**
             * Set limited accounts mapping activation flag as flase in columnpref array if no mapping present
             */
            columnPref.isLimitedCustomerAccounts = false;
            columnPref.isLimitedVendorAccounts = false;
            columnPref.isLimitedProductPurchaseAccounts = false;
            columnPref.isLimitedProductSalesAccounts = false;
        }
        //set value for activation flag of MRP QA flow
        if(this.isQaApprovalFlowInMRP.getValue()){
            columnPref.isQaApprovalFlowInMRP = true;
        }else{
            columnPref.isQaApprovalFlowInMRP = false;
        }

        rec.columnPref = JSON.stringify(columnPref);
        return rec;
    },

    addNewAccount:function(isEdit,record,winid,isexpense,isliability,accId,store){
        var rec = WtfGlobal.searchRecord(store, accId, "accountid");
        var groupId = "";
        var accounttype = "";
        if (rec != null && rec != undefined && rec.data != undefined) {
            groupId = rec.data.groupid;
            accounttype = rec.data.accounttype;
        }
      callCOAWindow(isEdit, record, winid,false,false,isliability,isexpense,'','','','',groupId,accounttype);
        Wtf.getCmp(winid).on('update', function(){
            if(winid=="fxcoaWin")
                this.exStore.reload();
            else if (winid=="depcoaWin")
                this.exStore.reload();
            else
                this.dgStore.reload();
            this.expenseStore.reload();
            this.liabilityStore.reload();
        }, this);
    },
    genSuccessResponse:function(response){
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.accPref.tabTitle"), response.msg], response.success * 2 + 1);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.accPref.tabTitle"), response.msg], 2);
        }
        if(response.success){
            WtfGlobal.resetAjaxTimeOut();
            getCompanyAccPref();
            this.lockds.load();
            bHasChanged=true;
        }
    },
//    genSuccessResponseOpeningBal:function(response){
//        WtfGlobal.resetAjaxTimeOut();
//        var rec=this.requestParameters();
//        if(response.success){
//            if(response.data==0){
//                WtfGlobal.setAjaxTimeOutFor30Minutes();
//                Wtf.Ajax.requestEx({
//                    //            url: Wtf.req.account+'CompanyManager.jsp',
//                    url : "ACCCompanyPrefCMN/saveCompanyAccountPreferences.do",
//                    params: rec
//                },this,this.genSuccessResponse,this.genFailureResponse);
//               this.ownerCt.items.each(function(item){
//                    //                if(!(item===this||item.id=="tabdashboard"))
//                    if(!(item.id=="tabdashboard"))
//                        item.ownerCt.remove(item);
//                }, this);
//                //if the value is less than previous saved value in financial year then save the currency exchange
//                this.originalfirstfyear=new Date(this.finanyear.originalValue,this.fmonth.originalValue,this.fdays.originalValue);//called when the current financial year is less than original value
//                if(new Date(this.firstfyear)< new Date(this.originalfirstfyear) && !Wtf.account.companyAccountPref.activateToDateforExchangeRates){
//                    this.savecurrencyexchange();
//                }
//            }else{
//                //var msg="You can't close books with account(s) having opening balance(s)";  //"Failed to make connection with Web Server";
//                //WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//            }
//        }
//    },

    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
createGroupCompanyMappingTab:function(){
       //When Multi Group Flag is activated
//       if(Wtf.account.companyAccountPref.activateGroupCompaniesFlag){
            this.childSubdomainRecord = new Wtf.data.Record.create([
            {
                name: 'id'
            },

            {
                name: 'cdomain'
            },

            {
                name: 'companyid'
            },

            {
                name: 'contexturl'
            },
            {
                name: 'isparent',
                type:'boolean'
            }
            ]);

            this.childSubdomainReader = new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            }, this.childSubdomainRecord);

            this.subDomainStore = new Wtf.data.Store({
                reader:this.childSubdomainReader,
                url:"AccGroupCompany/getSubdomains.do"
            });

            this.isSalesorPurchaseCheckbox=new Wtf.form.Checkbox({
                fieldLabel:WtfGlobal.getLocaleText("acc.groupcompany.isSourceCustomer"),  //'Is Source Customer
                name:'isSalesFlag',
                checked:true,
                disabled:true
            });

            this.sourceComboList = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.sourceSubdomain"),
                name: 'sourceComboField',
                allowBlank:false,
                width: 150,
                readOnly:true,
                cls: 'clearStyle',
                value:Wtf.cdomain
            });

            this.destinationComboList  = new Wtf.form.ComboBox({
                displayField:'cdomain',
                valueField:'companyid',
                store:this.subDomainStore,
                name:'id',
                mode: 'local',
                fieldLabel:WtfGlobal.getLocaleText("acc.field.destinationSubdomain"),
                emptyText:WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain"),
                triggerAction: 'all',
                allowBlank: false,
                typeAhead:true,
                width: 150,
                listWidth:300,
                listeners:{
                    scope:this,
                    beforequery: function() {
                        this.subDomainStore.load({
                            params:{
                                cdomainValue:this.sourceComboList.getValue()
                            }
                        })
                    }
                }
            });

            this.GroupCompanyMappingForm=new Wtf.form.FormPanel({
                style: 'background: white;',
                autoHeight: true,
                disabledClass:"newtripcmbss",
                id:"groupCompany"+this.id,
                border:false,
                items:[{
                    layout:'form',
                    defaults:{
                        border:false
                    },
                    // baseCls:'northFormFormat',
                    labelWidth:200,
                    items:[{
                        layout:'column',
                        defaults:{
                            border:false,
                            bodyStyle:'padding:10px'
                        },
                        items:[{
                            layout:'form',
                            columnWidth:0.49,
                            items: [
                            {
                                xtype: 'fieldset',
                                autoHeight: true,
                                title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.CompanyMapping") + "'>" + WtfGlobal.getLocaleText("acc.field.CompanyMapping") + " </span>",
                                defaults: {
                                    xtype: 'textfield',
//                                    anchor: '80%',
                                    maxLength: 500
                                },
                                items: [this.sourceComboList,this.destinationComboList]
                            },
                            {
                                xtype:'fieldset',
                                autoHeight:true,
                                title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.groupCompany.purchasetoSalesMappingToolTip")+"'>"+WtfGlobal.getLocaleText("acc.groupCompany.purchasetoSalesMapping")+"</span>",
                                items:[
//                                    this.isSalesorPurchaseCheckbox,
                                this.purchasetoSalesmoduleMappingButton = new Wtf.Toolbar.Button({
                                    text: WtfGlobal.getLocaleText("acc.groupCompany.mapModuleMappingButton"),
                                    tooltip: WtfGlobal.getLocaleText("acc.field.moduleMappingToolTip"),
                                    style: 'margin-top:10px;',
                                    scope: this,
                                    handler: function() {
                                        if(this.destinationComboList.getValue()!=""&& this.destinationComboList.getValue()!=null && this.destinationComboList.getValue()!="null"){
                                            this.mappingInterface(true,false,false,false,true);
                                        }else{
                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain")], 2);
                                        }
                                    }
                                }),this.purchasetoSalesCustomerVendorMappingBtn = new Wtf.Toolbar.Button({
                                    text: WtfGlobal.getLocaleText("acc.groupCompany.mapVendorcustomerMappingButton"),
                                    tooltip: WtfGlobal.getLocaleText("acc.field.CustomerVendorMappingToolTip"),
                                    scope: this,
                                    style: 'margin-top:10px;',
                                    handler: function() {
                                        if(this.destinationComboList.getValue()!=""&& this.destinationComboList.getValue()!=null && this.destinationComboList.getValue()!="null"){
                                            this.mappingInterface(false,false,true,false,true);
                                        }else{
                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain")], 2);
                                        }
                                    }
                                }),this.purchasetoSalesTaxMappingBtn = new Wtf.Toolbar.Button({
                                    text: WtfGlobal.getLocaleText("acc.groupCompany.mapPurchaseSalesTaxMappingButton"),
                                    tooltip: WtfGlobal.getLocaleText("acc.field.taxMappingToolTip"),
                                    scope: this,
                                    style: 'margin-top:10px;',
                                    handler: function() {
                                        if(this.destinationComboList.getValue()!=""&& this.destinationComboList.getValue()!=null && this.destinationComboList.getValue()!="null"){
                                            this.mappingInterface(false,true,false,false,true);
                                        }else{
                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain")], 2);
                                        }
                                    }
                                }),
                                this.purchasetoSalesInvoiceTermsMappingBtn = new Wtf.Toolbar.Button({
                                    text: WtfGlobal.getLocaleText("acc.groupCompany.mapPurchaseSalesInvoiceTermsMappingButton"),
                                    tooltip: WtfGlobal.getLocaleText("acc.field.invoiceTermsMappingToolTip"),
                                    disabled: false,
                                    scope: this,
                                    style: 'margin-top:10px;',
                                    handler: function() {
                                        if(this.destinationComboList.getValue()!=""&& this.destinationComboList.getValue()!=null && this.destinationComboList.getValue()!="null"){
                                            this.mappingInterface(false,false,false,true,true);
                                        }else{
                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain")], 2);
                                        }
                                    }
                                })
                                ]
                            }
//                            ,{
//                                xtype:'fieldset',
//                                autoHeight:true,
//                                title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.groupCompany.salestoPurchaseMappingToolTip")+"'>"+WtfGlobal.getLocaleText("acc.groupCompany.salestoPurchaseMapping")+"</span>",
//                                items:[
//                                this.salesToPurchaseModuleMappingButton = new Wtf.Toolbar.Button({
//                                    text: WtfGlobal.getLocaleText("acc.groupCompany.smapModuleMappingButton"),
//                                    tooltip: WtfGlobal.getLocaleText("acc.field.moduleMappingToolTip"),
//                                    scope: this,
//                                    style: 'margin-top:10px;',
//                                    handler: function() {
//                                        if(this.destinationComboList.getValue()!=""&& this.destinationComboList.getValue()!=null && this.destinationComboList.getValue()!="null"){
//                                            this.mappingInterface(true,false,false,false,false);
//                                        }else{
//                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain")], 2);
//                                        }
//                                    }
//                                }),this.salesToPurchaseCustomerVendorMappingBtn = new Wtf.Toolbar.Button({
//                                    text: WtfGlobal.getLocaleText("acc.groupCompany.smapVendorcustomerMappingButton"),
//                                    tooltip: WtfGlobal.getLocaleText("acc.field.CustomerVendorMappingToolTip"),
//                                    scope: this,
//                                    style: 'margin-top:10px;',
//                                    handler: function() {
//                                        if(this.destinationComboList.getValue()!=""&& this.destinationComboList.getValue()!=null && this.destinationComboList.getValue()!="null"){
//                                            this.mappingInterface(false,false,true,false,false);
//                                        }else{
//                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain")], 2);
//                                        }
//                                    }
//                                }),
//                                this.salesToPurchaseInvoiceTermsMappingBtn = new Wtf.Toolbar.Button({
//                                    text: WtfGlobal.getLocaleText("acc.groupCompany.smapPurchaseSalesInvoiceTermsMappingButton"),
//                                    tooltip: WtfGlobal.getLocaleText("acc.field.invoiceTermsMappingToolTip"),
//                                    disabled: false,
//                                    scope: this,
//                                    style: 'margin-top:10px;',
//                                    handler: function() {
//                                        if(this.destinationComboList.getValue()!=""&& this.destinationComboList.getValue()!=null && this.destinationComboList.getValue()!="null"){
//                                            this.mappingInterface(false,false,false,true,false);
//                                        }else{
//                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain")], 2);
//                                        }
//                                    }
//                                }),this.salesToPurchaseTaxMappingBtn = new Wtf.Toolbar.Button({
//                                    text: WtfGlobal.getLocaleText("acc.groupCompany.smapPurchaseSalesTaxMappingButton"),
//                                    tooltip: WtfGlobal.getLocaleText("acc.field.taxMappingToolTip"),
//                                    scope: this,
//                                    style: 'margin-top:10px;',
//                                    handler: function() {
//                                        if(this.destinationComboList.getValue()!=""&& this.destinationComboList.getValue()!=null && this.destinationComboList.getValue()!="null"){
//                                            this.mappingInterface(false,true,false,false,false);
//                                        }else{
//                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain")], 2);
//                                        }
//                                    }
//                                })
//                                ]
//                            }
                        ]
                        }]
                    }]
                }]
            });
//        }
    },
    mappingInterface: function(ismodulemapping,istaxmapping,iscustomervendormapping,isinvoicetermsmapping,isSourcePurchaseFlag) {
        this.mappingConfigs = {};
        this.mappingConfigs.istaxmapping = istaxmapping;
        this.mappingConfigs.ismodulemapping = ismodulemapping;
        this.mappingConfigs.iscustomervendormapping = iscustomervendormapping;
        this.mappingConfigs.isinvoicetermsmapping = isinvoicetermsmapping;
        this.mappingConfigs.sourcecompanyid = this.sourceComboList.getValue();
        this.mappingConfigs.destinationcompanyid =this.destinationComboList.getValue();
        this.mappingConfigs.isSourceFlag =isSourcePurchaseFlag;
        Wtf.callgroupCompanyMappingInterface(this.mappingConfigs, this);
    },
  saveGroupCompanyWizardButton:function(){
        //Does not allow to save if destination subdomain is not given.
        if(this.destinationComboList.getValue()!=""&& this.destinationComboList.getValue()!=null && this.destinationComboList.getValue()!="null"){
            var recparams={}
            recparams.sourcecompanyid=companyid;
            recparams.destinationcompanyid=this.destinationComboList.getValue();
            if(this.purchaseSalesTaxMappingKey!=undefined && this.purchaseSalesTaxMappingKey!="undefined"){
              recparams.purchaseSalesTaxMappingKey=this.purchaseSalesTaxMappingKey;
          }

            if(this.purchaseSalesModuleMappingKey!=undefined &&this.purchaseSalesModuleMappingKey!="undefined"){
                recparams.purchaseSalesModuleMappingKey=this.purchaseSalesModuleMappingKey;
            }

            if(this.purchaseSalesInvoiceTermsMappingKey!=undefined &&this.purchaseSalesInvoiceTermsMappingKey!="undefined"){
                recparams.purchaseSalesInvoiceTermsMappingKey=this.purchaseSalesInvoiceTermsMappingKey;
            }

            if(this.vendorcustomerMappingKey!=undefined &&this.vendorcustomerMappingKey!="undefined"){
                recparams.vendorcustomerMappingKey=this.vendorcustomerMappingKey;
            }

            Wtf.Ajax.requestEx({
                url: "AccGroupCompany/saveGroupCompanyWizardSettings.do",
                params: recparams
            }, this,this.genSuccessRes);

        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.groupCompany.selectDestinationSubdomain")], 2);
        }

    },
    genSuccessRes: function (response, request) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
        }
        else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
        }
    },
    validateFormat:function(val){
        var temp=val;
        temp=temp.replace(/[0]/g, "");
        if(val.length-temp.length<6)
            return WtfGlobal.getLocaleText("acc.accPref.msg8");
        else
            return true;
    },

    initForClose:function(){
        this.form.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){
                    this.isClosable=false;
                },this);
            }
        },this);
        this.grid.on("afteredit", function(){
            this.isClosable=false;
        },this);
    },

    GSTEnableHandler:function(c,checked){
        if(Wtf.account.companyAccountPref.countryid == '137'){// only for malasian company
            if(checked){
                this.gstnumber.enable();
                this.taxNumber.enable();
                //                this.gstEffectiveDate.enable();
                this.companyuen.enable();
//                this.iafversion.enable();
                this.gafVersion.enable();
                this.badDebtProcessingPeriod.enable();
                this.badDebtProcessingType.enable();
                this.gstSubmissionPeriod.enable();
            }else{

                this.gstnumber.reset();
                this.taxNumber.reset();
                this.gstEffectiveDate.reset();
                this.companyuen.reset();
//                this.iafversion.reset();
                this.gafVersion.reset();
                this.badDebtProcessingPeriod.reset();
                this.badDebtProcessingType.reset();
                this.gstSubmissionPeriod.reset();

                this.gstnumber.disable();
                this.taxNumber.disable();
                //                this.gstEffectiveDate.disable();
                this.companyuen.disable();
//                this.iafversion.disable();
                this.gafVersion.disable();
                this.badDebtProcessingPeriod.disable();
                this.badDebtProcessingType.disable();
                this.gstSubmissionPeriod.disable();

                this.gstnumber.allowBlank=true;
                this.gstEffectiveDate.allowBlank=true;
                this.companyuen.allowBlank=true;
//                this.iafversion.allowBlank=true;
                this.gafVersion.allowBlank=true;
                this.badDebtProcessingPeriod.allowBlank=true;
                this.badDebtProcessingType.allowBlank=true;
                this.gstSubmissionPeriod.allowBlank=true;
            }
        }
    },
    addMultiEntityGrid: function () {
        this.multiEntitygridRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'multiEntityid'},
            {name: 'multiEntity'},
            {name: 'multiEntitygstno'},
            {name: 'multiEntitytaxNumber'},
            {name: 'multiEntitycompanybrn'},
            {name: 'industryCodeValue'},
            {name: 'industryCodeId'},
            {name: 'gstSubmissionPeriod'}
        ]);
        this.multiEntitygridStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.multiEntitygridRec),
            url: "AccGST/getEntityDetails.do"
        });

        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 5,
            id: "pagingtoolbar" + this.id,
            store: this.multiEntitygridStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
        });
        this.multiEntitygridStore.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 5
            }
        });

        this.multiEntitygridcm = new Wtf.grid.ColumnModel([new Wtf.grid.CheckboxSelectionModel({
                singleSelect: true
            }), {
                header: WtfGlobal.getLocaleText("acc.gst.header.entity"),
                dataIndex: 'multiEntity',
                align: 'center',
                autoWidth: true
            }, {
                header: (Wtf.account.companyAccountPref.countryid == '137')?WtfGlobal.getLocaleText("acc.gst.header.trn"):WtfGlobal.getLocaleText("acc.gst.header.GSTNo"),
                dataIndex: 'multiEntitygstno',
                align: 'center',
                autoWidth: true
            }, {
                header: WtfGlobal.getLocaleText("acc.gst.header.taxNumber"),
                dataIndex: 'multiEntitytaxNumber',
                align: 'center',
                autoWidth: true
            }, {
                header: (Wtf.account.companyAccountPref.countryid == '137')?WtfGlobal.getLocaleText("acc.gst.header.brn"):WtfGlobal.getLocaleText("acc.gst.header.UEN"),
                dataIndex: 'multiEntitycompanybrn',
                align: 'center',
                autoWidth: true
            }, {
                header: WtfGlobal.getLocaleText("acc.PrimaryMsic.code"),
                dataIndex: 'industryCodeValue',
                hidden: Wtf.account.companyAccountPref.countryid != '137',
                align: 'center',
                autoWidth: true
            },{
                header: WtfGlobal.getLocaleText("acc.malaysiangst.gstSubmissionPeriod"),
                dataIndex: 'gstSubmissionPeriod',
                hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.MALAYSIA,
                align: 'center',
                autoWidth: true,
                renderer: function (value) {
                    var value = (value == 0) ? "Monthly" : "Quarterly";
                    return value;
                }
            }]);
        this.addEntity = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.gst.activate.add.entity"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            tooltip: WtfGlobal.getLocaleText("acc.gst.activate.add.entity"),
            handler: this.addEditEntity.createDelegate(this, [false])
        });
        this.editEntity = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.gst.activate.Edit.entity"),
            iconCls: getButtonIconCls(Wtf.etype.edit),
            tooltip: WtfGlobal.getLocaleText("acc.gst.activate.Edit.entity"),
            handler: this.addEditEntity.createDelegate(this, [true])
        });
        this.deleteEntity = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.gst.activate.Delete.entity"),
            iconCls: getButtonIconCls(Wtf.etype.deletebutton),
            tooltip: WtfGlobal.getLocaleText("acc.gst.activate.Delete.entity"),
            handler: this.deleteEntityMap.createDelegate(this)
        });
        this.sm = new Wtf.grid.CheckboxSelectionModel({singleSelect: true});
        this.multiEntityGrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            id: 'multiEntityGrid',
            autoScroll: true,
            autoHeight: true,
            hidden: true,
            title: WtfGlobal.getLocaleText("acc.gst.gstDetails"),
            store: this.multiEntitygridStore,
            tbar: [this.addEntity, this.editEntity, this.deleteEntity],
            bbar: this.pagingToolbar,
            cm: this.multiEntitygridcm,
            sm: this.sm,
            border: true,
            loadMask: true,
            bodyStyle: 'border: solid #cccccc 1px',
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
    },
    multiEntityHandler: function (c, checked) {
        if (Wtf.account.companyAccountPref.isDimensionCreated && checked && Wtf.account.companyAccountPref.countryid=="137") {
            this.multiEntityGrid.show();    //GST Details Grid show only when Custom Dimension is not Created and Multi Entity is activated
        } else {
            this.multiEntityGrid.hide();    //GST Details Grid hide when Custom Dimension is not Created
        }
    },
    addEditEntity: function (isEdit) {
        this.multiEntiryGSTRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'fieldid'}
        ]);
        this.multiEntiryGSTStore = new Wtf.data.Store({
            url: "AccGST/getMultiEntityForCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.multiEntiryGSTRec)
        });

        this.multiEntitygstCombo = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.gst.activate.select.entity")+"*",
            emptyText: WtfGlobal.getLocaleText("acc.gst.activate.select.entity"),
            store: this.multiEntiryGSTStore,
            name: 'multiEntity',
//            id: 'multiEntity',
            valueField: 'id',
            displayField: 'name',
            mode: 'remote',
            forceSelection: true,
            triggerAction: 'all',
            selectOnFocus: true,
            width: 240,
            allowBlank:false
        });
        this.multiEntiryGSTStore.load();

        this.multiEntitygstno = new Wtf.form.TextField({
            fieldLabel: ((Wtf.account.companyAccountPref.countryid == '137')?WtfGlobal.getLocaleText("acc.trade.reg.no"):WtfGlobal.getLocaleText("acc.field.GSTNumber"))+"*",
            name: 'multiEntitygstno',
            emptyText: ((Wtf.account.companyAccountPref.countryid == '137'))?WtfGlobal.getLocaleText("acc.trade.reg.no.empty.text"):WtfGlobal.getLocaleText("acc.field.EnterGSTNumber"),
            allowBlank:false,
            width: 240
        });
        this.multiEntitytaxNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.TaxNumber")+"*",
            name: 'multiEntitytaxNumber',
            emptyText: WtfGlobal.getLocaleText("acc.field.EnterTaxNumber"),
            allowBlank:false,
            width: 240
        });
        this.masterItemGroupRec = Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        }
        ]);
        this.masterItemTempStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 59
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.masterItemGroupRec)
        });
        if(Wtf.account.companyAccountPref.countryid == '137'){
            this.masterItemTempStore.load();
        }
        var re = new Wtf.data.Record({
            id: "-1",
            name: "None"
        });
        this.industryCodeCmb = new Wtf.form.FnComboBox({
            triggerAction: 'all',
            mode: 'local',
            name: 'industryCodeId',
            hiddenName: 'industryCodeId',
            labelWidth : '130',
            valueField: 'id',
            hidden:Wtf.account.companyAccountPref.countryid != '137',
            hideLabel:Wtf.account.companyAccountPref.countryid != '137',
            displayField: 'name',
            store: this.masterItemTempStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.PrimaryMsic.code"),
            width: 240,
            value: "None",
            typeAhead: true,
            forceSelection: true
        });
        this.masterItemTempStore.on('load',function(){
            this.masterItemTempStore.insert(0, re);
            if(isEdit&&rec.data['industryCodeId']!=""){
                this.industryCodeCmb.setValue(rec.data['industryCodeId']);
            }else{
                this.industryCodeCmb.setValue("-1");
            }
        },this);

        this.multiEntitycompanybrn = new Wtf.form.TextField({
            fieldLabel:((Wtf.account.companyAccountPref.countryid != '137')?WtfGlobal.getLocaleText("acc.field.CompanyUEN"):WtfGlobal.getLocaleText("acc.field.CompanyBRN"))+"*",
            name: 'multiEntitycompanybrn',
            emptyText:(Wtf.account.companyAccountPref.countryid != '137')?WtfGlobal.getLocaleText("acc.field.EnterCompanyUEN"):WtfGlobal.getLocaleText("acc.field.EnterCompanyBRN"),
            allowBlank:false,
            width: 240
        });
        this.multiEntityGSTSubmissionPeriod = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.malaysiangst.gstSubmissionPeriod"),
            store: this.gstSubmissionPeriodStore,
            name: 'multiEntityGSTSubmissionPeriod',
            hiddenName: 'multiEntityGSTSubmissionPeriod',
            valueField: 'id',
            mode: 'local',
            displayField: 'type',
            forceSelection: true,
            triggerAction: 'all',
            value: 0,
            selectOnFocus: true,
            hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.MALAYSIA,
            hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.MALAYSIA,
            width: 240
        });
        if (isEdit && (this.multiEntityGrid.getSelectionModel().hasSelection() == false || this.multiEntityGrid.getSelectionModel().getCount() > 1)) {
            var msg = !this.multiEntityGrid.getSelectionModel().hasSelection() ? WtfGlobal.getLocaleText("acc.msgbox.129a") : WtfGlobal.getLocaleText("acc.account.OpeningBalanceType.editMulti");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            return;
        }
        var rec = this.multiEntityGrid.getSelectionModel().getSelected();

        this.addNewEntityForm = new Wtf.form.FormPanel({
            region: "center",
            border: false,
            bodyStyle: "background-color:#f1f1f1;padding:15px",
            labelWidth: 130,
            items: [this.multiEntitygstCombo,this.multiEntitygstno,this.multiEntitytaxNumber,this.multiEntitycompanybrn,this.industryCodeCmb,this.multiEntityGSTSubmissionPeriod],
            buttons: [{
                    text: WtfGlobal.getLocaleText("acc.field.Save"),
                    handler: function () {

                        this.multiEntitygstno.setValue(this.multiEntitygstno.getValue().trim());
                        this.multiEntitytaxNumber.setValue(this.multiEntitytaxNumber.getValue().trim());
                        this.multiEntitycompanybrn.setValue(this.multiEntitycompanybrn.getValue().trim());

                        if (this.addNewEntityForm.getForm().isValid()) {
                            var gridCount = this.multiEntityGrid.getStore().getCount();
                            var isDuplicateEntity = false;
                            for (var cnt = 0; cnt < gridCount; cnt++) {
                                if ((this.multiEntitygstCombo.getRawValue() == this.multiEntityGrid.getStore().getAt(cnt).data.multiEntity) && !((isEdit && this.multiEntitygstCombo.getRawValue() == rec.data.multiEntity))) {
                                    isDuplicateEntity = true;
                                    break;
                                }
                            }
                            if (isDuplicateEntity) {
//                                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gst.entity.add.duplicate"));
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.entity.add.duplicate")], 2);
                            } else {
                                Wtf.MessageBox.show({
                                    title: WtfGlobal.getLocaleText("acc.common.savdat"),
                                    msg: WtfGlobal.getLocaleText("acc.je.msg1"),
                                    scope: this,
                                    buttons: Wtf.MessageBox.YESNO,
                                    icon: Wtf.MessageBox.INFO,
                                    width: 300,
                                    fn: function (btn) {
                                        if (btn == "yes") {
                                            var jsonObject = this.addNewEntityForm.form.getValues();
                                            jsonObject.multiEntityId = this.multiEntitygstCombo.value;
                                            jsonObject.industryCodeId = this.industryCodeCmb.getValue();
                                            jsonObject.gstSubmissionPeriod = this.multiEntityGSTSubmissionPeriod.getValue();
                                            if (isEdit)
                                                jsonObject.id = rec.data.id;
                                            var array = [];
                                            array.push(jsonObject);
                                            var obj = {};
                                            obj.data = array;
                                            this.ajxUrl = "AccGST/saveEntityMapping.do";
                                            Wtf.Ajax.requestEx({
                                                url: this.ajxUrl,
                                                params: {
                                                    data: JSON.stringify(obj),
                                                    isEdit: isEdit
                                                }
                                            }, this, this.genSuccessRes);
                                        }
                                    }
                                }, this);
                            }
                        }
                    },
                    scope: this
                }, {
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    handler: function () {
                        this.addNewEntitywin.close();
                    },
                    scope: this
                }]
        });
        this.multiEntiryGSTStore.on('load', function () {
            if (isEdit) {
                this.multiEntitygstCombo.setValue(rec.data['multiEntityid']);
                this.multiEntitygstno.setValue(rec.data['multiEntitygstno']);
                this.multiEntitytaxNumber.setValue(rec.data['multiEntitytaxNumber']);
                this.multiEntitycompanybrn.setValue(rec.data['multiEntitycompanybrn']);
                this.multiEntityGSTSubmissionPeriod.setValue(rec.data['gstSubmissionPeriod']);
            }
        }, this);

        this.addNewEntitywin = new Wtf.Window({
            title: isEdit ? WtfGlobal.getLocaleText("acc.gst.activate.Edit.entity") :WtfGlobal.getLocaleText("acc.gst.activate.add.entity"),
            closable: true,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            width: 455,
            autoHeight: true,
            modal: true,
            buttonAlign: 'right',
            items: this.addNewEntityForm
        });
        this.addNewEntitywin.show();
    },
    deleteEntityMap:function (){

        if (this.multiEntityGrid.getSelectionModel().hasSelection()) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.gst.entity.confirmDelete"), function(btn) {
                if (btn == "yes") {
                    var arr = [];
                    this.recArr = this.multiEntityGrid.getSelectionModel().getSelections();
                    for (var i = 0; i < this.recArr.length; i++) {
                        arr.push(this.multiEntitygridStore.indexOf(this.recArr[i]));
                    }
                    var data = WtfGlobal.getJSONArray(this.multiEntityGrid, true, arr);
                    Wtf.Ajax.requestEx({
                        url: "AccGST/deleteEntityMapping.do",
                        params: {
                            data: data
                        }
                    }, this, this.genSuccessRes);
                } else {
                    return;
                }
            }, this);
        } else {
            WtfComMsgBox(129, 2);
            return;
        }


    },
    genSuccessRes: function (response, request) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
            this.multiEntitygridStore.load();
            this.addNewEntitywin.close();
        }else{
            var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if (response.msg !== undefined && response.msg !== "") {
                msg = response.msg;
            }
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
    },
    checkmrpmodule:function(obj, newval, oldval) {
        if (newval == true) {
            this.periodicInventory.setValue(false);
            this.PeriodicJE.setValue(false);
            this.perpetualInventory.setValue(true);
            this.periodicInventory.disable();
            this.perpetualInventory.disable();
            this.PeriodicJE.disable();
        } else {
            this.periodicInventory.setValue(true);
            this.perpetualInventory.setValue(false);
            this.periodicInventory.enable();
            this.perpetualInventory.enable();
            this.PeriodicJE.enable();
        }
    },
    checkTransactionsForManufacturing: function(obj, newval, oldval) {
        Wtf.Ajax.requestEx({
            url: "ACCCompanyPref/checkTransactionPresentForManufacturingModule.do"
        }, this, function(response) {
            if (response.msg != "" && response.msg != null && response.msg != undefined) {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.information"),
                    width: 500,
                    msg: response.msg,
                    buttons: Wtf.MessageBox.OK,
                    scope: this,
                    icon: Wtf.MessageBox.WARNING
                });
                this.activatemrpmodule.setValue(oldval);
                this.checkmrpmodule(obj, oldval);
            } else {
                this.checkmrpmodule(obj, newval);
            }
        }, function(response) {

        });
    },


    validateCheckUncheckMapTaxesAtProductLevel: function(obj, newval, oldval) {

        /*------Restricting to Uncheck this setting----------*/
        if (newval == false && CompanyPreferenceChecks.mapTaxesAtProductLevel()) {
            this.mapTaxesAtProductLevel.setValue(true);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.maptaxesatproductlevel.cannotUncheck")], 2);
            return;
        }

        /*-----Prompt to aware user , Once checked It cannot be unchecked again----  */
        if (newval == true) {

            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.maptaxesatproductlevel.checkUncheckValidation"),
                width: 370,
                buttons: Wtf.MessageBox.YESNO,
                fn: function(btn) {
                    if (btn === 'no') {
                        this.mapTaxesAtProductLevel.setValue(false);
                        return;
                    }
                },
                icon: Wtf.MessageBox.INFO,
                scope: this
            });
        }

    },

    enableDisablePagingCheckbox: function() {
        if(this.allowProductPaging!=undefined && this.allowProductPaging!=null){
            if(this.ontypeahead.getValue()){
                this.allowProductPaging.enable();
            }else{
                this.allowProductPaging.disable();
            }
            this.allowProductPaging.setValue(this.ontypeahead.getValue() || this.allautoload.getValue()); //  || this.allautoload.getValue() - Paging is enabled for combo type All product
        }
    },
    getGstEffectiveDate:function(){
        if(Wtf.account.companyAccountPref.gstEffectiveDate){
            //            return Date.parseDate(Wtf.account.companyAccountPref.gstEffectiveDate,WtfGlobal.getOnlyDateFormat());
            return new Date(Wtf.account.companyAccountPref.gstEffectiveDate);
        }
    },

    assetSettingsHandler:function(){
        this.fixedAssetSettings = new Wtf.account.AssetSetting({
            title:WtfGlobal.getLocaleText("acc.field.activatefixedasset"),
            layout:'border',
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            height:400,
            width:500
        });
        this.fixedAssetSettings.show();
    },

    /**
     * window to allow zero quantity for product in transactions
     */
    AllowZeroQuantityWindow:function(){
        this.ProductQuantitySettingWindow = new Wtf.account.AllowZeroQuantityForProduct({
            title:WtfGlobal.getLocaleText("acc.accPref.allowzeroqty"),
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            autoScroll:true,
            height:350,
            width:500
        });
        this.ProductQuantitySettingWindow.show();
    },
        IndianGSTSettings:function(){
        this.IndianGSTSettingsWindow = new Wtf.account.IndianGSTSettings({
            title:WtfGlobal.getLocaleText("acc.IndianGSTSettings.gstmodule"),
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            autoScroll:true,
            height:350,
            width:500,
            parentObj:this
        });
        this.IndianGSTSettingsWindow.show();
    },
    controlAccountSettingHandler: function(){
        this.controlAccountsSettings = new Wtf.account.ControlAccountsSettings({
            title:WtfGlobal.getLocaleText("acc.field.controllAccountsSettings"),
            layout:'border',
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            height:190,
            width:450
        });
        this.controlAccountsSettings.show();
    },

    manualJePostSettingHandler: function(){
        var  winid="ManualJEPostSettingWin";
        var p = Wtf.getCmp(winid);
        if(!p){
            var cm=[{
                header: WtfGlobal.getLocaleText("acc.field.Purpose"),  //"Purpose",
                dataIndex: 'purpose',
                align:'left',
                renderer:function(value,meta,rec){
                    meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='"+WtfGlobal.getLocaleText("acc.field.UsedIn")+"'";
                    if(!value) return value;
                    return value;
                }
            },{
                header: WtfGlobal.getLocaleText("acc.coa.accCode"),  //"Account Code",
                dataIndex: 'acccode',
                 align:'center',
                 sortable:true
            },{
                header: WtfGlobal.getLocaleText("acc.coa.gridAccountName"),  //"Account name",
                dataIndex: 'accname',
                 align:'left',
                 sortable:true
            },this.CheckBoxColumn = new Wtf.grid.CheckColumn ({
                header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.havetopostmanualJEOrOtherTrnsactions")+"'>"+WtfGlobal.getLocaleText("acc.field.havetopostmanualJEOrOtherTrnsactions")+"</span>",  //"Have to post Manual JE",
                align:'center',
                dataIndex: 'haveToPostJe'
            })
            ];
            cm.defaultSortable = false;
            this.dataRec = new Wtf.data.Record.create([
            {
                name: 'accountid'
            },

            {
                name: 'purpose'
            },

            {
                name: 'acccode'
            },

            {
                name: 'accname'
            },

            {
                name: 'haveToPostJe'
            }
            ]);

            this.dataStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty:"count"
                },this.dataRec),
                remoteSort:true,
                url :"ACCCompanyPref/getManualJePostSettingData.do"
            });
            this.dataStore.load({
                params:{
                    start:0,
                    limit:15
                }
            });

            new Wtf.account.GridUpdateWindow({
                mode:101,
                width:700,
                height:510,
                store:this.dataStore,
                record:this.dataRec,
                cm:cm,
                title:WtfGlobal.getLocaleText("acc.field.selectcatopostJE"),  //Manual JE's Post Settings
                id:winid,
                renderTo: document.body,
                gridPlugins:[this.CheckBoxColumn]
            }).show();
        }
    },

    budgetSettingsHandler:function(){
        this.bugetAssetSettings = new Wtf.account.BudgetSetting({
            title:WtfGlobal.getLocaleText("acc.field.BudgetingSetting"),
            layout:'border',
            scroll:true,
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            height:550,
            width:600
        });

        this.bugetAssetSettings.show();
    },
    isDisabledGstEffectiveDate: function() {
        var disabled=false;
        if(Wtf.account.companyAccountPref.gstEffectiveDate !=""){
            disabled=true;
        }
        return disabled;
    },
    isDisabledGstDeactivationDate: function() {
        var disabled=false;
        if(Wtf.account.companyAccountPref.gstDeactivationDate !=""){
            disabled=true;
        }
        if (Wtf.account.companyAccountPref.gstEffectiveDate == "") {
            disabled = false;
        }
        return disabled;
    },
    getGstDeactivationDate: function() {
        if (Wtf.account.companyAccountPref.gstDeactivationDate) {
            return new Date(Wtf.account.companyAccountPref.gstDeactivationDate);
        }
    },
    IsLineLevelTaxUsedInTransaction: function(taxtype,taxname,component) {
        var isUsed= false;
        if(taxtype != "" && taxtype != undefined){
            //[[1,'VAT'],[2,'Excise Duty'],[3,'CST'],[4,'Service Tax'],[5,'Swachh Bharat Cess'],[6,'Krishi Kalyan Cess'],[7,'Others']]
            Wtf.Ajax.requestEx({
                url:"ACCCompanyPrefCMN/IsLineLevelTaxUsedInTransaction.do",
                params: {
                    taxtype: taxtype,
                    taxname: taxname
                }
            },this,function(response){
                if(response.success && response.isUsed){
                    isUsed = true;
                    var msg = "";
                    var table="";
                    table="<table cellspacing=10>"+
                    "<tr>"+
                    "<td><b>Tax </b></td>"+
                    "<td><b>Used In</b></td>"+
                    "<td><b>Number</b></td>"+
                    "</tr>";
                    var limit = 0, j = 0;
                    var PreviousUsedIn="";
                    for(j=0;j < response.UsedInArray.length ; j++){
                        var json = response.UsedInArray[j];
                        if(limit == 0 || PreviousUsedIn != json.UsedIn){
                            PreviousUsedIn = json.UsedIn;
                            limit = 0;
                        }
                        if(limit < 2 && PreviousUsedIn == json.UsedIn){//"limit" of "2" is applied according to Used in Category.
                            //It Will Add "limit" Records of Each Used In Catogory.
                            table+="<tr> <td valign=top>"+json.TermName+"</td>";
                            table+="<td valign=top>"+json.UsedIn+"</td>";
                            table+="<td valign=top>"+json.UsedInNumber+"</td>";
                            table+="</tr>";
                            limit++;
                        }
                    }
                    table+="<tr><td valign=top></td>";
                    table+="<td valign=top><b>Total Used In Transaction: </b></td>";
                    table+="<td valign=top><b>"+j+"</b></td>";
                    table+="</tr>";
                    table+="</table>";

                    if( component != undefined){
                        component.expand();//As (cstFieldset, exciseFieldset)these fieldset needs to expand again.
                        if(isUsed && component == this.exciseFieldset){// If Excise Duty Tax is Used in Transaction then throws Msg.
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.deActivateExcise")+ "\n "+table], 2);
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.deActivateExcise")], 2);
                        }else if(!isUsed && component == this.exciseFieldset){//If Excise Duty Tax is not Used in Transaction then perform actions after collapse.
                            //Following Actions set to be done only after collapse.
                            this.exciseCommissionerateCode.allowBlank=true;
                            this.exciseCommissionerateName.allowBlank=true;
                            this.exciseDivisionCode.allowBlank=true;
                            this.exciseRangeCode.allowBlank=true;
                            this.excisePayableAcc.allowBlank=true;

                            this.exciseTariffdetails.collapse();
                            this.tariffName.allowBlank=true;
                            this.tariffName.validate();
                            this.HSNCode.allowBlank=true;
                            this.HSNCode.validate();
                            this.reportingUOM.allowBlank=true;
                            this.reportingUOM.validate();
                            this.exciseMethodCombo.allowBlank=true;
                            this.exciseMethodCombo.validate();
                        }else if(isUsed && component == this.cstFieldset){// If Service Tax is Used in Transaction then throws Msg.
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.deActivateService")+ "\n "+table], 2);
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.deActivateService")], 2);
                        } else if(!isUsed && component == this.cstFieldset){//If Service Tax is not Used in Transaction then perform actions after collapse.
                            //Following Actions set to be done only after collapse.
                            this.rangeCode.allowBlank=true;
                            this.divisionCode.allowBlank=true;
                            this.commissionerateName.allowBlank=true;
                            this.serviceTaxRegNo.allowBlank=true;
                            this.commissionerateCode.allowBlank=true;
                            this.STPayableAcc.allowBlank=true;
                        }
                    }else if(taxtype == "1" || taxtype == "3"){
                        this.enableVATCST.setValue(true);// this (this.enableVATCST) checkbox needs to "Checked" again.
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.deActivateVATCST")+ "\n "+table], 2);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.deActivateVATCST")], 2);
                    }
                }else if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && taxtype == "1"){//First verifying for VAT, if not then verify for CST.
                    this.IsLineLevelTaxUsedInTransaction("3","CST");
                }else if(!response.isUsed && taxtype === "4"){//Service Tax
                    this.rangeCode.allowBlank=true;
                    this.serviceTaxRegNo.allowBlank=true;
                    this.divisionCode.allowBlank=true;
                    this.commissionerateName.allowBlank=true;
                    this.commissionerateCode.allowBlank=true;
                    this.STPayableAcc.allowBlank=true;
                }else if(!response.isUsed && component == this.exciseFieldset){
                    this.exciseCommissionerateCode.allowBlank=true;
                    this.exciseCommissionerateName.allowBlank=true;
                    this.exciseDivisionCode.allowBlank=true;
                    this.exciseRangeCode.allowBlank=true;
                    this.excisePayableAcc.allowBlank=true;

                    this.exciseTariffdetails.collapse();
                    this.tariffName.allowBlank=true;
                    this.tariffName.validate();
                    this.HSNCode.allowBlank=true;
                    this.HSNCode.validate();
                    this.reportingUOM.allowBlank=true;
                    this.reportingUOM.validate();
                    this.exciseMethodCombo.allowBlank=true;
                    this.exciseMethodCombo.validate();
                }
            },function(){});
        }else if(taxname == "TDS"){//Flow for TDS
            Wtf.Ajax.requestEx({
                url:"ACCCompanyPrefCMN/IsTDSUsedInTransaction.do",
                params: {
                    taxtype: taxtype,
                    taxname: taxname
                }
            },this,function(response){
                if(response.success && response.isUsed){
                    isUsed = true;
                    var msg = "";
                    var table="";
                    table="<table cellspacing=10>"+
                    "<tr>"+
                    "<td><b>TDS Payable Account </b></td>"+
                    "<td><b>Used In</b></td>"+
                    "<td><b>Number</b></td>"+
                    "</tr>";
                    var limit = 0, j=0;
                    var PreviousUsedIn="";
                    for(j=0;j < response.UsedInArray.length ; j++){
                        var json = response.UsedInArray[j];
                        if(limit == 0 || PreviousUsedIn != json.UsedIn){
                            PreviousUsedIn = json.UsedIn;
                            limit = 0;
                        }
                        if(limit < 2 && PreviousUsedIn == json.UsedIn){//"limit" of "2" is applied according to Used in Category.
                            //It Will Add "limit" Records of Each Used In Catogory.
                            table+="<tr> <td valign=top>"+json.TDSPayableAccount+"</td>";
                            table+="<td valign=top>"+json.UsedIn+"</td>";
                            table+="<td valign=top>"+json.UsedInNumber+"</td>";
                            table+="</tr>";
                            limit++;
                        }
                    }
                    table+="<tr><td valign=top></td>";
                    table+="<td valign=top><b>Total Used In Transaction: </b></td>";
                    table+="<td valign=top><b>"+j+"</b></td>";
                    table+="</tr>";
                    table+="</table>";
                    if( component != undefined){
                        component.expand();//As (cstFieldset, exciseFieldset)these fieldset needs to expand again.
                        if(isUsed && component == this.tdsFieldset){// If Excise Duty Tax is Used in Transaction then throws Msg.
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.deActivateTDS")+ "\n "+table], 2);
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.deActivateTDS")], 2);
                        }
                    }
                }
            },function(){});
        } else if (taxname == "Excise Unit" && component != undefined && component == this.exciseMultipleUnit) {
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPrefCMN/IsExciseUnitUsedInTransaction.do",
                params: {
                    taxtype: taxtype,
                    CheckExciseUnit: true,
                    taxname: taxname
                }
            }, this, function (response) {
                if (response.success && response.isUsed) {
                    isUsed = true;
                    if (isUsed) {
                        this.exciseMultipleUnit.setValue(true);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.deActivateExciseUnit")], 2);
                    }
                }
            }, function () {
            });
        }
    },

    testEmailHandler: function() {
        callTestMailWindow();
    },
    callYearEndClosingCheckList: function (obj) {
        if (obj.record != undefined && obj.record.data.islock) {
            callYearEndCheckList(obj.record, this);
        }
    },
    activateIBGCollectionHandler: function (c, checked) {
        if (checked) {
            this.activateIBGCollectionDetails.show();
            this.UOBEndToEndID.allowBlank = false;
            this.UOBPurposeCode.allowBlank = false;
        } else {
            this.UOBEndToEndID.allowBlank = true;
            this.UOBPurposeCode.allowBlank = true;
            this.activateIBGCollectionDetails.hide();
        }
    },
    /**
     * function to check- Sales trasnactions or Purchase transaction are present or not.
     *
     */
    checkSalesorPurchaseTransactionsPresent: function (newValue,oldValue,issalesSide,obj) {
        Wtf.Ajax.requestEx({
            url: "ACCCompanyPrefCMN/checkSalesorPurchaseTransactionsPresent.do",
            params: {
                issaleside:issalesSide
            }
        }, this, function (response) {
            if (response.success && response.istransacionstpresent) {
                if (issalesSide) {
                    this.bandsWithSpecialRateForSales.reset();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.pricebandfunctionality.sales.systemPreference")], 2);
                } else {
                    this.bandsWithSpecialRateForPurchase.reset();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.pricebandfunctionality.purchase.systemPreference")], 2);
                }
                if (obj != undefined) {
                    obj.setValue(oldValue);
                }
            }
        }, function () {
        });
    }
});



Wtf.SequenceFormatWindow = function (config){
    this.availableAutogenSeqStartFromVal = 1;
    this.matchedTransactionswithsequenceformat=false;
    this.addEvents({
        'setAutoNumbers':true
    });
    Wtf.apply(this,config);
//    this.saveSeqBttn = new Wtf.Toolbar.Button({
//        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
//        scope: this,
//        handler:this.saveSequenceNumber
//    });
    Wtf.SequenceFormatWindow.superclass.constructor.call(this,{
        buttons:[{
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            scope: this,
            id : "savebtn" + this.id,
            handler:this.saveSequenceNumber
        },{
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //"Reset",
            scope:this,
            handler:this.resetFormat
        },{
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.SequenceFormatWindow,Wtf.Window,{
    layout:"border",
    modal:true,
    title:WtfGlobal.getLocaleText("acc.field.AddSequenceFormat"), //"Change Password",//WtfGlobal.getLocaleText("acc.changePass.tabTitle")
    id:'sequenceformatlinkforaccounting',
    width:650, //SDP-416
    height:585,
    resizable:false,
    iconCls: "pwnd deskeralogoposition",
    initComponent:function (){
        Wtf.SequenceFormatWindow.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);
        this.add(this.centerPanel);
        this.add(this.AddEditForm);
    },
    GetNorthPanel:function (){
        this.sequenceFormatRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'value'
        },

        {
            name: 'prefix'
        },

        {
            name: 'suffix'
        },

        {
            name: 'numberofdigit'
        },

        {
            name: 'startfrom'
        },

        {
            name: 'showleadingzero'
        },

        {
            name: 'isdefaultformat'
        },
        {
            name: 'showdateinprefix'
        },
        {
            name: 'selecteddateformat'
        },

        {
            name: 'showdateafterprefix'
        },
        {
            name: 'selecteddateformatafterprefix'
        },
        {
            name: 'oldflag'
        },

        {
            name: 'isChecked'
        },
        {
            name: 'showdateaftersuffix'
        },
        {
            name: 'dateformataftersuffix'
        },
        {
            name: 'resetcounter'
        },
        {
            name: 'custom'
        },
        {
            name: 'customid'
        }
        ]);
        this.isEdit=false;

        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"

            },this.sequenceFormatRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do"
        });
        this.sequenceFormatStore.load({
            params:{
                mode:this.mode,
                isAllowNA:false,
                isEdit:true
            }
        });
        //        Wtf.Ajax.requestEx({
        //            url : "ACCCompanyPref/getAutoGenNumberStartFromValue.do",
        //            params:{
        //                mode:this.mode
        //            }
        //        },this,
        //        function(res,req){
        //            if(res.success){
        //                this.availableAutogenSeqStartFromVal = res.autogenNumStartFrom;
        //            }
        //
        //        },
        //        function(req){
        //            });
        this.sequenceCM= new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            dataIndex:'value'
        },{
            header: WtfGlobal.getLocaleText("acc.inv.fieldset.title"),//Dimensions
            dataIndex:'custom',
            hidden:this.mode!='autodimensionnumber'
        },{
            header:WtfGlobal.getLocaleText("acc.field.showDateInPrefix"),
            dataIndex:'showdateinprefix'
        },{
            header:WtfGlobal.getLocaleText("acc.field.showDateAfterPrefix"),    //Show Date After Prefix
            dataIndex:'showdateafterprefix'
        },{
            header:WtfGlobal.getLocaleText("acc.field.showDateAfterSuffix"),
            dataIndex:'showdateaftersuffix'
        },{
            header:WtfGlobal.getLocaleText("acc.field.counterreset"),
            dataIndex:'resetcounter'
        },{
            header:WtfGlobal.getLocaleText("acc.field.Prefix"),
            dataIndex:'prefix'
        },{
            header:WtfGlobal.getLocaleText("acc.field.Suffix"),
            dataIndex:'suffix'
        },{
            header:WtfGlobal.getLocaleText("acc.field.NumberofDigit"),
            dataIndex:'numberofdigit'
        },{
            header:WtfGlobal.getLocaleText("acc.field.StartFrom"),
            dataIndex:'startfrom'
        },{
            header:WtfGlobal.getLocaleText("acc.field.ShowLeadingZero"),
            dataIndex:'showleadingzero'
        },{
            header:WtfGlobal.getLocaleText("acc.field.SetDefult"),
            dataIndex:'isdefaultformat'
        },{
            header:WtfGlobal.getLocaleText("acc.field.isActivated"),
            dataIndex:'isChecked',
            renderer:function(val){
                return val ? "Yes" : "No";
            }
        },{
            header:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            align:'center',
            renderer:function(){
                //                      return "<div class='pwnd deleteSequenceNo'> </div>";
                return "<div class='pwnd delete-gridrow' > </div>";
            }
        }
        ]);

        this.customDimensionRec = new Wtf.data.Record.create([
            {name:"id"},
            {name:"name"}
        ]);

        this.customDimensionReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.customDimensionRec);

        this.customDimensionStore = new Wtf.data.Store({
            url:"ACCMaster/getMasterGroups.do",
            reader:this.customDimensionReader,
            baseParams:{
                mode:111,
                isShowDimensiononly:true
            }
        });
        this.customDimensionStore.load();

        this.filterDimensionStore = new Wtf.data.Store({
            url:"ACCMaster/getMasterGroups.do",
            reader:this.customDimensionReader,
            baseParams:{
                mode:111,
                isShowDimensiononly:true
            }
        });
        this.filterDimensionStore.load();

        this.filterDimensionStore.on('load',function(){
            var rec=this.filterDimensionStore.getAt(0);
            rec.data.name="All"
            rec.data.id="all"
            this.filterDimensionStore.add(rec);
            this.filtercustomDimension.setValue('all');
        },this);

        this.filtercustomDimension = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.selectdimensioncustom"),
            name:'filtercustomdimension',
            store:this.filterDimensionStore,
            forceSelection: true,
            valueField:'id',
            displayField:'name',
            hidden:this.mode!='autodimensionnumber',
            hideLabel:this.mode!='autodimensionnumber',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });

        this.selectcustomDimension = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.selectdimensioncustom"),
            name:'customdimension',
            store:this.customDimensionStore,
            forceSelection: true,
            valueField:'id',
            displayField:'name',
            hidden:this.mode!='autodimensionnumber',
            hideLabel:this.mode!='autodimensionnumber',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });

        this.filtercustomDimension.on('select',function(){
            this.sequenceFormatStore.load({
                params:{
                    mode:this.mode,
                    isAllowNA:false,
                    isEdit:true,
                    masterid:this.filtercustomDimension.getValue()
                }
            });
        },this);

        this.sequencenoGrid = new Wtf.grid.GridPanel({
            store: this.sequenceFormatStore,
            cm:this.sequenceCM,
            height:280,
            autoScroll:true, //SDP-416
            viewConfig:{
                forceFit:false,
                emptyText:WtfGlobal.getLocaleText("acc.field.Norecordfound")
            }
        });
        this.sequencenoGrid.on("cellclick", this.deleteSequence, this);
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(WtfGlobal.getLocaleText("acc.field.AddSequenceFormat"),WtfGlobal.getLocaleText("acc.field.AddSequenceFormatfor")+"'"+this.module+"'",'../../images/createuser.png',false,'0px 0px 0px 0px')
        });
        this.centerPanel = new Wtf.Panel({
            region:"center",
            layout: 'fit',
            items:[this.sequencenoGrid],
            tbar:this.mode=='autodimensionnumber'?[WtfGlobal.getLocaleText("acc.accPref.selectdimensioncustom")+":",this.filtercustomDimension]:[]
        })
    },
    //    handleStartFromNumberField:function(obj, newVal, oldVal){
    //        if(newVal==0 || newVal<this.availableAutogenSeqStartFromVal){
    //            var msg = "";
    //            if(newVal==0){
    //                msg = "Please enter a number greater than zero."
    //            }else if(newVal<this.availableAutogenSeqStartFromVal){
    //                msg = 'Please enter Start From number greater than '+this.availableAutogenSeqStartFromVal+'. as you have already saved Start From Number '+this.availableAutogenSeqStartFromVal;
    //            }
    //            WtfComMsgBox(["Information",msg], 0);
    //            this.startfrom.reset();
    //        }
    //    },
    //    handleFields:function(obj, newVal, oldVal){
    //        var regExp = new RegExp(/^([0-9]*)$/);
    //        if(regExp.test(newVal)){
    //            this.startfrom.enable();
    //        }else{
    //            if(this.startfrom.getValue()!=null &&this.startfrom.getValue()!=undefined && this.startfrom.getValue() != ""){
    //                this.startfrom.reset();
    //            }
    //            this.startfrom.disable();
    ////            this.keepLeadingZero.reset();
    ////            this.keepLeadingZero.disable();
    //        }
    //    },

    GetAddEditForm:function (){
        this.formatStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'formatid',
                type:'string'
            }, 'name'],
            data :[['YYYY','YYYY'],['YYYY-','YYYY-'],['YYYYMM','YYYYMM'],['YYYYMM-','YYYYMM-'],['YYYYMMDD','YYYYMMDD'],['YYYYMMDD-','YYYYMMDD-'],['YY','YY'],['YY-','YY-'],
                   ['YYMM','YYMM'],['YYMM-','YYMM-'], ['YYMMDD','YYMMDD'],['YYMMDD-','YYMMDD-'],['empty','']]
        });
        this.showDateinPrefix = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.showDateInPrefix"),
            name:'showdateinprefix',
            checked:false
        });
        this.selectDateFormat = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.selectDateFormat"),
            name:'showdateinprefix',
            store:this.formatStore,
            forceSelection: true,
            valueField:'formatid',
            displayField:'name',
//            hidden:this.showDateinPrefix.getValue(),
//            hidelabel:this.showDateinPrefix.getValue(),
            disabled:true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true

        });
        //Show date after prefix
        this.showdateafterprefix = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.showDateAfterPrefix"),
            name:'showdateafterprefix',
            checked:false
        });
        this.selectDateFormatAfterPrefix = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.selectDateFormat"),
            name:'showdateafterprefix',
            store:this.formatStore,
            forceSelection: true,
            valueField:'formatid',
            displayField:'name',
            disabled:true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true

        });

        this.showDateAfterSuffix = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.showDateAfterSuffix"),
            name:'showdatefftersuffix',
            checked:false
        });

        this.selectDateFormatForSuffix = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.selectDateFormatSuffix"),
            name:'selectdateformatforsuffix',
            store:this.formatStore,
            forceSelection: true,
            valueField:'formatid',
            displayField:'name',
            disabled:true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.showDateinPrefix.on('check',function(obj,isChecked){
            if(isChecked){
                this.selectDateFormat.reset();
                this.selectDateFormat.enable();
                this.resetCounter.enable();
            }else{
                this.selectDateFormat.reset();
                this.selectDateFormat.clearValue();
                this.selectDateFormat.disable();
                if(!this.showDateAfterSuffix.getValue()){//when both prefix and suffix get delected then
                    this.resetCounter.setValue(false);
                    this.resetCounter.disable();
                }
            }

        },this);

        this.showDateAfterSuffix.on('check',function(obj,isChecked){
            if(isChecked){
                this.selectDateFormatForSuffix.reset();
                this.selectDateFormatForSuffix.enable();
                this.resetCounter.enable();
            }else{
                this.selectDateFormatForSuffix.reset();
                this.selectDateFormatForSuffix.clearValue();
                this.selectDateFormatForSuffix.disable();
                if(!this.showDateinPrefix.getValue()){//when both prefix and suffix get delected then
                    this.resetCounter.setValue(false);
                    this.resetCounter.disable();
                }
            }
        },this);

        this.showdateafterprefix.on('check',function(obj,isChecked){
            if(isChecked){
                this.selectDateFormatAfterPrefix.reset();
                this.selectDateFormatAfterPrefix.enable();
                this.resetCounter.enable();
            }else{
                this.selectDateFormatAfterPrefix.reset();
                this.selectDateFormatAfterPrefix.clearValue();
                this.selectDateFormatAfterPrefix.disable();
                if(!this.showDateAfterSuffix.getValue() && !this.showDateinPrefix.getValue()){//when both prefix and suffix get detected then
                    this.resetCounter.setValue(false);
                    this.resetCounter.disable();
                }
            }

        },this);

        this.resetCounter = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.counterreset") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.counterresethelpmessage")),
            name:'resetcounter',
            checked:false,
            disabled:true
        });

        this.prefix = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Prefix"),
            name:'prefix',
            maxLength:20,
            emptyText:WtfGlobal.getLocaleText("acc.field.PleaseenterPrefixvalue"),
            regex:WtfGlobal.isIndiaCountryAndGSTApplied()?/^[A-Za-z0-9\/-]*$/:null
        });
        this.suffix = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Suffix"),
            name:'suffix',
            maxLength:20,
            emptyText:WtfGlobal.getLocaleText("acc.field.PleaseenterSuffixvalue"),
            regex:WtfGlobal.isIndiaCountryAndGSTApplied()?/^[A-Za-z0-9\/-]*$/:null
        });
        this.numberofdigit = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.NumberofDigit*"),
            name:'numberofdigit',
            allowNegative:false,
            allowBlank:false,
            allowDecimals:false,
            minValue:1,
            maxValue:10,
            emptyText:WtfGlobal.getLocaleText("acc.field.Pleaseenternumberofdigit")
        //            listeners:{
        //                        'change':{
        //                            fn:this.handleStartFromNumberField,
        //                            scope:this
        //                        }
        //                    }
        });
        this.startfrom = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.StartFrom"),
            name:'startfrom',
            allowNegative:false,
            //            disabled:true,
            //            allowBlank:true,
            allowDecimals:false,
            emptyText:WtfGlobal.getLocaleText("acc.field.Pleaseenterstartvalue")
        //            listeners:{
        //                        'change':{
        //                            fn:this.handleStartFromNumberField,
        //                            scope:this
        //                        }
        //                    }
        });
        this.showleadingzero = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowLeadingZero"),
            name:'showleadingzero',
            //            disabled:true,
            checked:true
        });
        this.isdefaultformat = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.SetDefult"),
            name:'isdefaultformat',
            checked:false
        });
        this.isCheckedCheckBox = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.isActivated"),
            name:'isChecked',
            checked:false
        });
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"south",
            border:false,
            height:315,
            autoScroll:true,
            labelWidth: 160, //SDP-416
            defaults:{
                width:250 //SDP-416
            },
            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding:15px",
            items:[this.selectcustomDimension,this.showDateinPrefix,this.selectDateFormat,this.showdateafterprefix,this.selectDateFormatAfterPrefix,this.showDateAfterSuffix,this.selectDateFormatForSuffix,this.resetCounter,this.prefix,this.suffix,this.numberofdigit,this.startfrom,this.showleadingzero,this.isdefaultformat, this.isCheckedCheckBox]
        });
    },
    validateFormat:function(val){
        var temp=val;
        temp=temp.replace(/[0]/g, "");
        if(val.length-temp.length<6)
            return WtfGlobal.getLocaleText("acc.accPref.msg8");
        else
            return true;
    },
    saveSequenceNumber:function (){
        if(!this.AddEditForm.form.isValid())
        {
            return;
        } else {
            // Check squence format length for INDIA country
            var isChecked = this.isCheckedCheckBox.getValue();
            var finalSequenceFormat = this.selectDateFormat.getValue() + this.prefix.getValue() + this.selectDateFormatAfterPrefix.getValue() + this.suffix.getValue() + this.selectDateFormatForSuffix.getValue();
            var finalSequenceFormatLength = finalSequenceFormat.length + this.numberofdigit.getValue();
            if (this.isEdit && !isChecked) {
                    this.beforeSaveFormat();
            } else {

                if (finalSequenceFormatLength > Wtf.SeqenceFormatMaxLengthForIndianCompany && WtfGlobal.isIndiaCountryAndGSTApplied() ) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.companypreferences.sequenceFormatMaxLength1") + Wtf.SeqenceFormatMaxLengthForIndianCompany + WtfGlobal.getLocaleText("acc.companypreferences.sequenceFormatMaxLength2")], 2);
                } else {
                    this.beforeSaveFormat();
                }
            }
        }
    },
    beforeSaveFormat: function() {
        this.isChecked = this.isCheckedCheckBox.getValue();
        if(this.prefix.getValue()=="" && this.suffix.getValue()==""){
            var msg="<br> "+WtfGlobal.getLocaleText("acc.field.YouhavenotinputanyPrefixandPostfix")+"</br><br>"+WtfGlobal.getLocaleText("acc.field.Wouldyouliketoproceed")+"</br>";
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),msg, function(btn){
                if(btn!="yes"){
                    this.matchedTransactionswithsequenceformat=false;
                    return;
                }
                else{
                    if(this.isEdit){
                        var ri=this.sequencenoGrid.getSelectionModel().getSelections();
                        var id=ri[0].data.id;
                        this.saveFormat(true,id)
                    }else{
                        this.saveFormat(false);
                    }

                }
            },this);
        }else{
            if(this.isEdit){
                var ri=this.sequencenoGrid.getSelectionModel().getSelections();
                var id=ri[0].data.id;
                this.saveFormat(true,id);
            }
            else{
                this.saveFormat(false);
            }
        }
    },
    saveFormat:function(isEdit,id){
        var showDateInPrefix=this.showDateinPrefix.getValue();
        if(showDateInPrefix!=undefined && showDateInPrefix==true && this.selectDateFormat.getValue()==''){
            this.showDateinPrefix.markInvalid();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.selectanyDateFormat")],0);
            return;
        }

        var showdateafterprefix=this.showdateafterprefix.getValue();
        if(showdateafterprefix!=undefined && showdateafterprefix==true && this.selectDateFormatAfterPrefix.getValue()==''){
            this.showdateafterprefix.markInvalid();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.selectanyDateFormat")],0);
             return;
         }

        var showDateAfterSuffix=this.showDateAfterSuffix.getValue();
        if(showDateAfterSuffix!=undefined && showDateAfterSuffix==true && this.selectDateFormatForSuffix.getValue()==''){
            this.selectDateFormatForSuffix.markInvalid();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.selectanyDateFormatforsuffix")],0);
            return;
        }
        var param={
            mode:this.mode,
            prefix : this.prefix.getValue(),
            suffix : this.suffix.getValue(),
            numberofdigit : this.numberofdigit.getValue(),
            startfrom : this.startfrom.getValue(),
            showleadingzero : this.showleadingzero.getValue(),
            showdateinprefix : this.showDateinPrefix.getValue(),
            showdateafterprefix : this.showdateafterprefix.getValue(),
            selecteddateformat : this.selectDateFormat.getValue(),
            selecteddateformatafterprefix : this.selectDateFormatAfterPrefix.getValue(),
            showdateaftersuffix : this.showDateAfterSuffix.getValue(),
            selectedsuffixdateformat : this.selectDateFormatForSuffix.getValue(),
            resetcounter : this.resetCounter.getValue(),
            isdefaultformat:this.isdefaultformat.getValue(),
            module:this.module,
            isEdit:isEdit,
            id:id,
            isChecked: this.isChecked,
            custom:this.selectcustomDimension.getValue(),
            matchedTransactionswithsequenceformat : this.matchedTransactionswithsequenceformat
        }

       // if(Wtf.getCmp("savebtn" + this.id)) {
            Wtf.getCmp("savebtn" + this.id).disable(); //Disable save button on click
       // }
        Wtf.Ajax.requestEx({
            url : "ACCCompanyPref/saveSequenceFormat.do",
            params:param
        },this,
        function(req,res){
            //if(Wtf.getCmp("savebtn" + this.id)) {
                Wtf.getCmp("savebtn" + this.id).enable();  //Enable save button on success response
            //}
            var restext=req;
            if(restext.success){
                if(restext.name && restext.isduplicate &&  !(res.params.isEdit)){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ThisSequenceFormatalreadyexistsPlease")],0);
                    arguments[0].enable();
                }else{
                    if(restext.name && !(res.params.isEdit)){
                        this.fireEvent('setAutoNumbers',res.params.mode, restext.name,true);
                    }
                    this.close();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.sequence.format.save")],0);
                }
            } else if(restext.matchedTransactionswithsequenceformat!=undefined && restext.matchedTransactionswithsequenceformat){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.confirm"), //'Warning',
                    msg: WtfGlobal.getLocaleText("Some transactions are matched with the sequence format.Do you want to update them?"),
                    width:370,
                    buttons: Wtf.MessageBox.YESNO,
                    fn:function(btn){
                        if(btn==='no'){
                            this.matchedTransactionswithsequenceformat=false;
                            return;
                        }else{
                            this.matchedTransactionswithsequenceformat=true;
                            if(this.isEdit){
                                var ri=this.sequencenoGrid.getSelectionModel().getSelections();
                                var id=ri[0].data.id;
                                this.saveFormat(true,id)
                            }else{
                                this.saveFormat(false);
                            }
                        }
                    },
                    icon: Wtf.MessageBox.INFO,
                    scope: this
                });
                return;

                }else{

                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.companypreferences.cannotEditSequenceFormat")],2);
            }

        },
        function(req){
            var restext=req;
            Wtf.getCmp("savebtn" + this.id).enable();  //Enable save button on success response
            if(restext.msg !=""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1);
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.sequence.format.failure")],1);
            }
        });
    },

    deleteSequence:function(gd, ri, ci, e) {
        var event = e;
        if(event.target.className == "pwnd delete-gridrow") {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedsequenceformat"),function(btn){
                if(btn=="yes") {
                    var size=gd.getStore().getCount();
                    if(this.mode=="autojournalentry" && size==1){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.JournalEntryshouldhaveatleastonesequenceformatSo")],0);
                        return;
                    }
                    var isdefaultformat = gd.getStore().getAt(ri).data.isdefaultformat;
                    if(isdefaultformat=="Yes"){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.defaultField")],0);
                        return;
                    }
                    var sequenceformat = gd.getStore().getAt(ri).data.value;
                    var oldflag = gd.getStore().getAt(ri).data.oldflag;
                    var id = gd.getStore().getAt(ri).data.id;
                    Wtf.Ajax.requestEx({
                        //                        url:Wtf.req.base+'UserManager.jsp',
                        url : "ACCCompanyPref/deleteSequenceFormat.do",
                        params:{
                            mode:this.mode,
                            sequenceformat:sequenceformat,
                            oldflag:oldflag,
                            id:id,
                            module:this.module
                        }
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                            this.sequenceFormatStore.remove(this.sequenceFormatStore.getAt(ri));
                            var count=this.sequenceFormatStore.getCount();
                            var i=0;
                            var remainingFormats="";
                            while(i<count){
                                if(remainingFormats!=""){
                                    remainingFormats+=","+this.sequenceFormatStore.getAt(i).data.value;
                                }else{
                                    remainingFormats+=this.sequenceFormatStore.getAt(i).data.value;
                                }
                                i++ ;
                            }
                            this.fireEvent('setAutoNumbers',res.params.mode, remainingFormats,false);
                            //                            this.close();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.sequence.format.delete")],0);
                        } else if(restext.msg==undefined && restext.msg==""){

                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.sequence.format.deletefailure")],1);
                        }
                        else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.companypreferences.cannotDeleteSequenceFormat")],1);
                        }

                    },
                    function(req){
                        var restext=req;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.sequence.format.deletefailure")],1);
                    });
                }

            }, this)
            this.resetFormat();
        }else{
            this.editForm(gd, ri)
        }
    },
    editForm:function(gd, ri) {
        this.prefix.setValue(gd.getStore().getAt(ri).data.prefix);
        this.suffix.setValue(gd.getStore().getAt(ri).data.suffix);
        this.selectcustomDimension.setValue(gd.getStore().getAt(ri).data.customid);
        this.selectcustomDimension.disable();
        this.numberofdigit.setValue(gd.getStore().getAt(ri).data.numberofdigit);
        this.startfrom.setValue(gd.getStore().getAt(ri).data.startfrom);
        var showleadingzero = (gd.getStore().getAt(ri).data.showleadingzero == 	"Yes")?true:false;
        this.showleadingzero.setValue(showleadingzero);
        var isdefaultformat = (gd.getStore().getAt(ri).data.isdefaultformat == 	"Yes")?true:false;
        this.isdefaultformat.setValue(isdefaultformat);
        var isChecked = gd.getStore().getAt(ri).data.isChecked;
        this.isCheckedCheckBox.setValue(isChecked);
        var isshowdateinprefix = (gd.getStore().getAt(ri).data.showdateinprefix == "Yes")?true:false;
        this.showDateinPrefix.setValue(isshowdateinprefix);
        this.selectDateFormat.setValue(gd.getStore().getAt(ri).data.selecteddateformat);
        var isshowdateafterprefix = (gd.getStore().getAt(ri).data.showdateafterprefix == "Yes")?true:false;
        this.showdateafterprefix.setValue(isshowdateafterprefix);
        this.selectDateFormatAfterPrefix.setValue(gd.getStore().getAt(ri).data.selecteddateformatafterprefix);
        var showdateaftersuffix = (gd.getStore().getAt(ri).data.showdateaftersuffix == "Yes")?true:false;
        this.showDateAfterSuffix.setValue(showdateaftersuffix);
        this.selectDateFormatForSuffix.setValue(gd.getStore().getAt(ri).data.dateformataftersuffix);
        var resetCounter = (gd.getStore().getAt(ri).data.resetcounter == "Yes")?true:false;
        this.resetCounter.setValue(resetCounter);
        if(isshowdateinprefix || isshowdateafterprefix || showdateaftersuffix){//if date suffix or prefix availble in this case user can reset counter
            this.resetCounter.enable();
        } else {
            this.resetCounter.disable();
        }
        this.disabledComponent();
        this.isEdit=true;

    },
    disabledComponent:function(){

        this.prefix.disable();
        this.suffix.disable();
        this.numberofdigit.disable();
        this.startfrom.disable();
        this.showleadingzero.disable();
        this.showDateinPrefix.disable();
        this.showdateafterprefix.disable();
        this.selectDateFormat.disable();
        this.selectDateFormatAfterPrefix.disable();
        this.showDateAfterSuffix.disable();
        this.selectDateFormatForSuffix.disable();
    },
    resetFormat:function(){
        this.prefix.enable();
        this.suffix.enable();
        this.selectcustomDimension.reset();
        this.selectcustomDimension.enable();
        this.numberofdigit.enable();
        this.startfrom.enable();
        this.showleadingzero.enable();
        this.showDateinPrefix.enable();
        this.showdateafterprefix.enable();
        this.showDateAfterSuffix.enable();
        this.prefix.reset();
        this.suffix.reset();
        this.numberofdigit.reset();
        this.startfrom.reset();
        this.showleadingzero.reset();
        this.showDateinPrefix.reset();
        this.showdateafterprefix.reset();
        this.showDateAfterSuffix.reset();
        this.selectDateFormat.clearValue();//set to Empty Record
        this.selectDateFormatAfterPrefix.clearValue();
        this.selectDateFormatForSuffix.clearValue();
        this.isdefaultformat.reset();
        this.resetCounter.setValue(false);
        this.resetCounter.disable();

        this.isEdit=false;

    }
});

Wtf.callgroupCompanyMappingInterface = function(mappingConfigs, parentObj){
    var mappingWindow = Wtf.getCmp("grpCompanyMappingInterface"+this.id);
    if(!mappingWindow) {
        this.mapWindowInterface=new Wtf.groupCompanyMappingInterface({
            ismodulemapping:mappingConfigs.ismodulemapping,
            istaxmapping:mappingConfigs.istaxmapping,
            iscustomervendormapping:mappingConfigs.iscustomervendormapping,
            isinvoicetermsmapping:mappingConfigs.isinvoicetermsmapping,
            sourcecompanyid:mappingConfigs.sourcecompanyid,
            destinationcompanyid:mappingConfigs.destinationcompanyid,
            isSourceFlag:mappingConfigs.isSourceFlag,
            parentObj:parentObj
        }).show();
    } else {
        mappingWindow.ismodulemapping=mappingConfigs.ismodulemapping,
        mappingWindow.istaxmapping= mappingConfigs.istaxmapping,
        mappingWindow.iscustomervendormapping=mappingConfigs.iscustomervendormapping,
        mappingWindow.isinvoicetermsmapping=mappingConfigs.isinvoicetermsmapping,
        mappingWindow.sourcecompanyid=mappingConfigs.sourcecompanyid,
        mappingWindow.destinationcompanyid=mappingConfigs.destinationcompanyid,
        mappingWindow.isSourceFlag=mappingConfigs.isSourceFlag
        mappingWindow.parentObj=parentObj
        mappingWindow.show();
    }
}
/**********************************************************************************************************
 *                              Mapping Window
 **********************************************************************************************************/
Wtf.groupCompanyMappingInterface = function(config) {
    Wtf.apply(this, config);
    Wtf.groupCompanyMappingInterface.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.groupCompanyMappingInterface, Wtf.Window, {
    iconCls : 'importIcon',
    width:850,
    height:580,
    modal:true,
    layout:"fit",
    id:'grpCompanyMappingInterface'+this.id,
    closable:false,
    initComponent: function() {
        Wtf.groupCompanyMappingInterface.superclass.initComponent.call(this);
    },

    onRender: function(config){
        Wtf.groupCompanyMappingInterface.superclass.onRender.call(this, config);
        this.title="";
        this.dataIndexHeaderName="";
        this.displaySourceHeaderName="";
        this.displayDestinationHeaderName="";
        this.sourceEmptyMsg="";
        this.destinationEmptyMsg="";

        this.url="";
        var reqparameters={};
        var destinationBaseParams={};
        var sourceBaseParams={};
        this.note="";
        var height=80;

        destinationBaseParams.destinationcompanyid=this.destinationcompanyid;
        sourceBaseParams.sourcecompanyid=companyid;
        destinationBaseParams.isSourceFlag=!this.isSourceFlag;//will fetch Customer to Map at Destination because for destination, source is customer
        sourceBaseParams.isSourceFlag=this.isSourceFlag;//will fetch Vendor to Map at Source because for source,destination is vendor

        if(this.ismodulemapping){//Module Mapping
            this.title=WtfGlobal.getLocaleText("acc.groupCompany.moduleMapping");

            this.mapDsRec = new Wtf.data.Record.create([
            {
                name: 'moduleid',
                type:'int'
            },

            {
                name: 'modulename'
            }
            ]);

            this.url="AccGroupCompany/getPurchaseAndSalesModules.do";
            this.dataIndexHeaderName="modulename";
            this.displaySourceHeaderName=WtfGlobal.getLocaleText("acc.groupCompany.source") + " Module";
            this.displayDestinationHeaderName=WtfGlobal.getLocaleText("acc.groupCompany.destination") + " Module";
            this.sourceEmptyMsg=WtfGlobal.getLocaleText("acc.groupCompany.sourceEmptyMsgModule");
            this.destinationEmptyMsg=WtfGlobal.getLocaleText("acc.groupCompany.destinationEmptyMsgModule");

            this.note="<ul style='list-style-type:disc;padding-left:15px;'><li>"+WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesModuleNote1")+"<br><br>";
            this.note +=WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesModuleNote2")+"</li><li>";
            this.note +=WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesModuleNote3")+"</li><li>";
            this.note +=WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesModuleNote4")+"</li><li>";
            this.note +=WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesModuleNote5")+"</li><li>";
            this.note +=WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesModuleNote6")+"</li><li>";
            this.note +=WtfGlobal.getLocaleText("acc.groupCompany.saveMappingInterfaceNote1")+"</li></ul>";
            height=170;

        } else if(this.istaxmapping){//Tax Mapping
            this.title=WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesTaxMapping");
            reqparameters.sourcecompanyid=this.sourcecompanyid,
            reqparameters.destinationcompanyid=this.destinationcompanyid,

            this.mapDsRec = new Wtf.data.Record.create([
            {
                name: 'taxid'
            },
            {
                name: 'taxcode'
            }
            ]);

            this.url="AccGroupCompany/getTax.do";
            this.dataIndexHeaderName="taxcode";
            this.displaySourceHeaderName=WtfGlobal.getLocaleText("acc.groupCompany.source") + " Purchase Tax";
            this.displayDestinationHeaderName=WtfGlobal.getLocaleText("acc.groupCompany.destination") + " Sales Tax";

            this.sourceEmptyMsg=WtfGlobal.getLocaleText("acc.groupCompany.sourceEmptyMsgTax");
            this.destinationEmptyMsg=WtfGlobal.getLocaleText("acc.groupCompany.destinationEmptyMsgTax");

            this.note ="<ul style='list-style-type:disc;padding-left:15px;'><li>"+WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesTaxNote1")+"</li><li>";
            this.note +=WtfGlobal.getLocaleText("acc.groupCompany.saveMappingInterfaceNote1")+"</li></ul>";

        }else if(this.iscustomervendormapping){//Customer Vendor Mapping
            this.title=WtfGlobal.getLocaleText("acc.groupCompany.vendorcustomerMapping");

            this.mapDsRec = new Wtf.data.Record.create([
            {
                name: 'accid'
            },
            {
                name:'acccode'
            }
            ]);

            this.url="AccGroupCompany/getCustomerVendorMappingFields.do";

            this.dataIndexHeaderName="acccode";
            this.displaySourceHeaderName=WtfGlobal.getLocaleText("acc.groupCompany.source")+ " Vendor";
            this.displayDestinationHeaderName=WtfGlobal.getLocaleText("acc.groupCompany.destination")+" Customer";
            this.sourceEmptyMsg=WtfGlobal.getLocaleText("acc.groupCompany.sourceEmptyMsgVendCus");
            this.destinationEmptyMsg=WtfGlobal.getLocaleText("acc.groupCompany.destinationEmptyMsgVendCus");

            this.note ="<ul style='list-style-type:disc;padding-left:15px;'><li>"+WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesCustomerVendorNote1")+"</li><li>";
            this.note +=WtfGlobal.getLocaleText("acc.groupCompany.saveMappingInterfaceNote1")+"</li></ul>";

            destinationBaseParams.mode=2;
            destinationBaseParams.group=10;
            destinationBaseParams.deleted=false;
            destinationBaseParams.nondeleted=true;
            destinationBaseParams.common='1';
//            destinationBaseParams.start=0;
//            destinationBaseParams.limit=15;

            sourceBaseParams.mode=2;
            sourceBaseParams.group=13;
            sourceBaseParams.deleted=false;
            sourceBaseParams.nondeleted=true;
            sourceBaseParams.common='1';
//            sourceBaseParams.start=0;
//            sourceBaseParams.limit=15;

        }else if(this.isinvoicetermsmapping){//Invoice Terms Mapping
            this.title=WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesInvoiceTermsMapping");

            this.mapDsRec = new Wtf.data.Record.create([
            {
                name: 'id'
            }, {
                name: 'term'
            }
            ]);

            this.url="AccGroupCompany/getInvoiceTermsMappingFields.do";
            this.dataIndexHeaderName="term";
            this.displaySourceHeaderName=WtfGlobal.getLocaleText("acc.groupCompany.source")+ " Purchase Invoice Terms";
            this.displayDestinationHeaderName=WtfGlobal.getLocaleText("acc.groupCompany.destination")+ " Sales Invoice Terms";
            this.sourceEmptyMsg=WtfGlobal.getLocaleText("acc.groupCompany.sourceEmptyMsgTerms");
            this.destinationEmptyMsg=WtfGlobal.getLocaleText("acc.groupCompany.destinationEmptyMsgTerms")

            this.note ="<ul style='list-style-type:disc;padding-left:15px;'><li>"+WtfGlobal.getLocaleText("acc.groupCompany.purchaseSalesInvoiceTermsNote1")+"</li><li>";
            this.note +=WtfGlobal.getLocaleText("acc.groupCompany.saveMappingInterfaceNote1")+"</li></ul>";

        }

        this.sourceHeaderDs = new Wtf.data.Store({
            url : this.url,
            sortInfo: {field: this.dataIndexHeaderName,direction: "ASC"},
            baseParams:sourceBaseParams,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.mapDsRec)
        });

        var sourceHeaderCm = new Wtf.grid.ColumnModel([{
            header:this.displaySourceHeaderName,
            dataIndex: this.dataIndexHeaderName,
            sortable:true,
            hideable: false
        }]);

        this.quickPanelSearchSource = new Wtf.KWLTagSearch({
            width: 170,
            id:"quickSearchSource"+this.id,
            Store:this.sourceHeaderDs,
            field : this.displaySourceHeaderName,
            emptyText:WtfGlobal.getLocaleText("acc.import.selCol")  //"Search "+(this.typeXLSFile?"xls":"csv")+" Headers "
        });

        this.sourceHeaderGrid= new Wtf.grid.GridPanel({
            ddGroup:"sourceColumn",
            enableDragDrop : true,
            sm:new Wtf.grid.RowSelectionModel({
                singleSelect:true
            }),
            height:370,
            store: this.sourceHeaderDs,
            cm: sourceHeaderCm,
            border : false,
            loadMask : true,
            tbar:!this.ismodulemapping?[this.quickPanelSearchSource]:"",
            view:new Wtf.grid.GridView({
                forceFit:true,
                emptyText:this.sourceEmptyMsg
            })
        //            ,bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
        //                pageSize: 15,
        //                id: "pagingtoolbar" + this.id,
        //                store: this.sourceHeaderDs,
        ////                searchField: this.quickPanelSearch,
        ////                displayInfo: true,
        //                //            displayMsg: 'Displaying records {0} - {1} of {2}',
        //                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
        //                plugins: this.pP = new Wtf.common.pPageSize({
        //                    id: "pPageSize_" + this.id
        //                    })
        ////                items : bottombtnArr  //added utton of link info button
        //            })
        });

        this.destinationDs = new Wtf.data.Store({
            url : this.url,
            sortInfo: {
                field: this.dataIndexHeaderName,
                direction: "ASC"
            },
            baseParams:destinationBaseParams,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.mapDsRec)
        });
        var destinationCm = new Wtf.grid.ColumnModel([
        {
            header: this.displayDestinationHeaderName,
            dataIndex: this.dataIndexHeaderName,
            sortable:true,
            hideable: false
        }]);

        this.quickPanelSearchDestination = new Wtf.KWLTagSearch({
            width: 170,
            id:"quickSearchDestination"+this.id,
            Store:this.destinationDs,
            field : this.displaySourceHeaderName,
            emptyText:WtfGlobal.getLocaleText("acc.import.selCol")  //"Search "+(this.typeXLSFile?"xls":"csv")+" Headers "
        });
        this.destinationGrid= new Wtf.grid.GridPanel({
            ddGroup:"destinationColumn",
            enableDragDrop : true,
            sm:new Wtf.grid.RowSelectionModel({
                singleSelect:true
            }),
            height:370,
            store: this.destinationDs,
            cm: destinationCm,
            border : false,
            loadMask : true,
            tbar:!this.ismodulemapping?[this.quickPanelSearchDestination]:"",
            view:new Wtf.grid.GridView({
                forceFit:true,
                emptyText:this.destinationEmptyMsg
            })
        });

        //Mapped source headers
        this.mappedSourceHeaderDs = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.mapDsRec)
        });

        var mappedSourceHeaderCm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText("acc.groupCompany.mappedSourceColumn"),
            dataIndex:this.dataIndexHeaderName
        }]);

        //mapping source column grid
        this.mappedSourceHeaderGrid= new Wtf.grid.GridPanel({
            ddGroup:"restoreSourceMappedHeader",
            enableDragDrop : true,
            sm:new Wtf.grid.RowSelectionModel({
                singleSelect:true
            }),
            store: this.mappedSourceHeaderDs,
            cm: mappedSourceHeaderCm,
            height:370,
            border : false,
            loadMask : true,
            tbar:[{
                xtype:'panel',
                height:20,
                border:false,
                renderer:function(a,b,c){
                    var qtip="";
                    var style="";
                    var equalsTo="<==>"
                    return "<span wtf:qtip='mapped to' style='cursor:pointer;"+style+"'>"+equalsTo+"</span>";
                }
            }],
            view:new Wtf.grid.GridView({
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.import.drag")  //"Drag and Drop Header here"
            })
        });

        this.mappedDestinationHeaderDs = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.mapDsRec)
        });

        var mappedDestinationHeaderCm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText("acc.groupCompany.mappedDestinationColumn"),
            dataIndex:this.dataIndexHeaderName
        }]);
        this.mappedDestinationHeaderGrid= new Wtf.grid.GridPanel({
            ddGroup:"restoreDestinationMappedHeader",
            enableDragDrop : true,
            sm:new Wtf.grid.RowSelectionModel({
                singleSelect:true
            }),
            store: this.mappedDestinationHeaderDs,
            cm: mappedDestinationHeaderCm,
            height:370,
            border : false,
            loadMask : true,
            tbar:[{
                xtype:'panel',
                height:20,
                border:false
            }],
            view:new Wtf.grid.GridView({
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.import.drag")  //"Drag and Drop Header here"
            })
        });

        if(this.destinationcompanyid!="" && this.destinationcompanyid!=null && this.destinationcompanyid!="null"&& this.destinationcompanyid!=undefined && this.destinationcompanyid!="undefined"){
            var isEdit=false;
            //Load Data of information of  Mapping Source Column
            Wtf.Ajax.requestEx({
                url : this.url,
                method: 'POST',
                params: {
                    destinationcompanyid:this.destinationcompanyid,
                    sourcecompanyid:companyid,
                    isSourceFlag:this.isSourceFlag
                }
            }, this, function(response) {
                if (response.success) {
                    if(response.mappedData!=undefined &&response.mappedData!="undefined"){
                        var resData = response.mappedData;
                        isEdit=true;
                        for (var i = 0; i < resData.length; i++) {
                            if(resData[i]!=undefined && resData[i]!='undefined'){
                                var data = resData[i];

                                var newHeaderRecord = new Wtf.data.Record(data);
                                this.mappedSourceHeaderDs.add(newHeaderRecord);
                            }
                        }
                    }

                    if(response.modifiedData!=undefined &&response.modifiedData!="undefined"){
                        var resData = response.modifiedData;
                        isEdit=true;
                        for (var i = 0; i < resData.length; i++) {
                            if(resData[i]!=undefined && resData[i]!='undefined'){
                                var data = resData[i];
                                var newHeaderRecord = new Wtf.data.Record(data);
                                this.sourceHeaderDs.add(newHeaderRecord);
                            }
                        }
                    }
                    if(!isEdit){//load source array if both are jsonarray are missing
                        this.sourceHeaderDs.load();
                    }

                }else{
                    this.sourceHeaderDs.load();
                }
            }, function(response) {
                });

            //Load Data of information of  Mapping Destination Column
            Wtf.Ajax.requestEx({
                url : this.url,
                method: 'POST',
                params: {
                    destinationcompanyid:this.destinationcompanyid,
                    sourcecompanyid:companyid,
                    isSourceFlag:!this.isSourceFlag
                }
            }, this, function(response) {
                if (response.success) {
                    if(response.mappedData!=undefined &&response.mappedData!="undefined"){
                        var resData = response.mappedData;
                        isEdit=true;
                        for (var i = 0; i < resData.length; i++) {
                            if(resData[i]!=undefined && resData[i]!='undefined'){
                                var data = resData[i];
                                var newHeaderRecord = new Wtf.data.Record(data);
                                this.mappedDestinationHeaderDs.add(newHeaderRecord);
                            }
                        }
                    }

                    if(response.modifiedData!=undefined &&response.modifiedData!="undefined"){
                        var resData = response.modifiedData;
                        isEdit=true;
                        for (var i = 0; i < resData.length; i++) {
                            if(resData[i]!=undefined && resData[i]!='undefined'){
                                var data = resData[i];
                                var newHeaderRecord = new Wtf.data.Record(data);
                                this.destinationDs.add(newHeaderRecord);
                            }
                        }
                    }
                    if(!isEdit){//load destination array
                        this.destinationDs.load();
                    }

                }else{
                    this.destinationDs.load();
                }
            }, function(response) {
                });
        }

            this.add({
                border: false,
                layout : 'border',
                items :[
                {
                    region: 'north',
                    border:false,
                    height:height+20,
                    bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                    items:[{
                        xtype:"panel",
                        border:false,
                        height:height,
//                        html:getImportTopHtml(this.title,"<ul style='list-style-type:disc;padding-left:15px;'><li>"+WtfGlobal.getLocaleText("acc.groupCompany.note1")+"</li><li>"+ WtfGlobal.getLocaleText("acc.groupCompany.saveMappingInterfaceNote1")+"</li></ul>","../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                        html:getImportTopHtml(this.title, this.note ,"../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                    }]
                },{
                    region: 'center',
//                    autoScroll: true,
                    bodyStyle : 'background:white;font-size:10px;',
                    border:false,
                    layout:"column",
                    items: [
                    {
                        xtype:"panel",
                        columnWidth:.25,
                        border:false,
                        layout:"fit",
                        autoScroll:true,
                        // title:headerName,
                        items:this.sourceHeaderGrid
                    },{
                        xtype:"panel",
                        columnWidth:.24,
                        border:false,
                        layout:"fit",
                        autoScroll:true,
                        //title:"Mapped Columns",
                        items:this.mappedSourceHeaderGrid
                    },{
                        xtype:"panel",
                        columnWidth:.25,
                        border:false,
                        layout:"fit",
                        autoScroll:true,
                        //  title:"Table Columns",
                        items:this.mappedDestinationHeaderGrid
                    },{
                        xtype:"panel",
                        columnWidth:.24,
                        border:false,
                        layout:"fit",
                        autoScroll:true,
                        // title:"Mapped Headers",
                        items:this.destinationGrid
                    }]
                }
                ],
                buttonAlign: 'right',
                buttons:[{
                    text: WtfGlobal.getLocaleText("acc.het.108"),  //'Change Preferences',
                    minWidth: 80,
                    scope: this,
                    handler: function(){

                    if(this.ismodulemapping){//Module Mapping
                        this.savePurchaseModuletoSalesMapping(this.mappedSourceHeaderDs,this.mappedDestinationHeaderDs,this.parentObj);
                    } else if(this.istaxmapping){//Tax Mapping
                        this.savePurchaseTaxtoSalesTaxMapping(this.mappedSourceHeaderDs,this.mappedDestinationHeaderDs,this.parentObj);
                    }else if(this.iscustomervendormapping){//Customer Vendor Mapping
                        this.saveVendortoCustomerMapping(this.mappedSourceHeaderDs,this.mappedDestinationHeaderDs,this.parentObj);
                    }else if(this.isinvoicetermsmapping){//Invoice Terms Mapping
                        this.savePurchaseTermstoSalesTermsMapping(this.mappedSourceHeaderDs,this.mappedDestinationHeaderDs,this.parentObj);
                    }
                    this.close();
                },
                    scope:this
                },{
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
                    minWidth: 80,
                    handler: function(){
                        this.close();
                    },
                    scope: this
                }]
            });


            this.on("afterlayout",function(){
                function rowsDiff(store1,store2){
                    return diff=store1.getCount()-store2.getCount();
                }

                function unMapRec(atIndex,isheaderStoreFlag){
                if(isheaderStoreFlag){
                    var headerRec = mappedHeaderStore.getAt(atIndex);
                    if(headerRec!==undefined){
                        mappedHeaderStore.remove(headerRec);
                        //                        headerStore.add(headerRec);//Commented to allow Multiple header mapping{SK}
                        headerStore.insert(atIndex,headerRec);//Commented to allow Multiple header mapping{SK}
                        headerStore.sort(sortHeaderName,"ASC");
                        headerGrid.getView().refresh();
                    }
                }
                else{
                    var columnRec = mappedColumnStore.getAt(atIndex);
                    if(columnRec!==undefined){
                        mappedColumnStore.remove(columnRec);
                        columnStore.insert(atIndex,columnRec);//Commented to allow Multiple header mapping{SK}
                        //                        columnStore.add(columnRec);
                        //Rearrange table columns
                        columnStore.sort(sortHeaderName,"ASC");
                        columnGrid.getView().refresh();
                    }
                }
            }

                columnStore = this.destinationDs;
                columnGrid = this.destinationGrid;

                mappedColumnStore = this.mappedDestinationHeaderDs;
                mappedColumGrid = this.mappedDestinationHeaderGrid;

                headerStore = this.sourceHeaderDs;
                headerGrid = this.sourceHeaderGrid;

                mappedHeaderStore = this.mappedSourceHeaderDs;
                mappedHeaderGrid = this.mappedSourceHeaderGrid;

                sortHeaderName=this.dataIndexHeaderName;

                // Drag n drop [ Headers -> Mapped Headers ]
                DropTargetEl =  mappedHeaderGrid.getView().el.dom.childNodes[0].childNodes[1];
                DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
                    ddGroup    : 'sourceColumn',
                    //                copy       : true,
                    notifyDrop : function(ddSource, e, data){
                        function sourceColumn(record, index, allItems) {
                            if(rowsDiff(mappedHeaderStore,mappedColumnStore)==0){//if both are equal then you can insert the field
                                if(columnStore.getCount()!=0){
                                    var newHeaderRecord = new Wtf.data.Record(record.data);
                                    mappedHeaderStore.add(newHeaderRecord);
//                                      mappedHeaderStore.insert(mappedHeaderStore.getCount(),newHeaderRecord);
                                      ddSource.grid.store.remove(record);//Commented to allow Multiple header mapping{SK}
                                      headerGrid.getView().refresh();
                                } else {
                                    WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.headerMApping"), WtfGlobal.getLocaleText("acc.import.noCol")], 0);
                                }
                            }else{
                                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.headerMApping"), WtfGlobal.getLocaleText("acc.import.prevHead")], 0);
                            }
                        }
                        Wtf.each(ddSource.dragData.selections ,sourceColumn);
                        return(true);
                    }
                });

                // Drag n drop [ Mapped Headers -> Headers ]
                DropTargetEl =  headerGrid.getView().el.dom.childNodes[0].childNodes[1];
                DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
                    ddGroup    : 'restoreSourceMappedHeader',
                    //                copy       : true,
                    notifyDrop : function(ddSource, e, data){
                        function restoreSourceMappedHeader(record, index, allItems) {
                            unMapRec(ddSource.grid.store.indexOf(record),true);
                        }
                        Wtf.each(ddSource.dragData.selections ,restoreSourceMappedHeader);
                        return(true);
                    }
                });

                // Drag n drop [ Mapped columns -> columns ]
                DropTargetEl =  columnGrid.getView().el.dom.childNodes[0].childNodes[1];
                DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
                    ddGroup    : 'restoreDestinationMappedHeader',
//                    copy       : true,
                    notifyDrop : function(ddSource, e, data){
                        function restoreDestinationMappedHeader(record, index, allItems) {
                            unMapRec(ddSource.grid.store.indexOf(record),false);
                        }
                        Wtf.each(ddSource.dragData.selections ,restoreDestinationMappedHeader);
                        return(true);
                    }
                });

                // Drag n drop [ columns -> Mapped columns ]
                DropTargetEl =  mappedColumGrid.getView().el.dom.childNodes[0].childNodes[1];
                DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
                    ddGroup    : 'destinationColumn',
//                    copy       : true,
                    notifyDrop : function(ddSource, e, data){
                        function destinationColumn(record, index, allItems) {
                            if(rowsDiff(mappedHeaderStore,mappedColumnStore)==1){
                                mappedColumnStore.add(record);
                                ddSource.grid.store.remove(record);
                                columnGrid.getView().refresh();
                            }else{
                                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.headerMApping"), WtfGlobal.getLocaleText("acc.import.selHead")], 0);
                            }
                        }
                        Wtf.each(ddSource.dragData.selections ,destinationColumn);
                        return(true);
                    }
                });


            },this);
    },
    saveVendortoCustomerMapping:function(sourceMappedStore,destinationMappedStore,parentObj){

    if(this.calculaterowDiff(sourceMappedStore,destinationMappedStore)==0){
            var jArray=[];
            var totalStoreCount=destinationMappedStore.getCount();
            for(var count=0;count<totalStoreCount;count++){
                var jsonObj={};
                if(sourceMappedStore.data.items[count]!=undefined &&  destinationMappedStore.data.items[count]){
                    jsonObj['sourceMasterid']=sourceMappedStore.data.items[count].data.accid;
//                    jsonObj['sourceMastername']=sourceMappedStore.data.items[count].data.accname;
                    jsonObj['sourceMasterCode']=sourceMappedStore.data.items[count].data.acccode;
                    jsonObj['destinationMasterid']=destinationMappedStore.data.items[count].data.accid;
//                    jsonObj['destinationMastername']=destinationMappedStore.data.items[count].data.accname;
                    jsonObj['destinationMasterCode']=destinationMappedStore.data.items[count].data.acccode;
                    jsonObj['isSourceCustomer']=this.isSourceFlag;
                    jsonObj['sourcecompanyid']=companyid;
                    jsonObj['destinationcompanyid']=this.destinationcompanyid;
                    jArray.push(jsonObj);
                }
            }
            this.parentObj.vendorcustomerMappingKey=JSON.stringify(jArray);
        }
    },
    savePurchaseTaxtoSalesTaxMapping:function(sourceMappedStore,destinationMappedStore,parentObj){
        if(this.calculaterowDiff(sourceMappedStore,destinationMappedStore)==0){
            var jArray=[];
            var totalStoreCount=destinationMappedStore.getCount();
            for(var count=0;count<totalStoreCount;count++){
                var jsonObj={};
                if(sourceMappedStore.data.items[count]!=undefined &&  destinationMappedStore.data.items[count]){
                    jsonObj['sourceTaxid']=sourceMappedStore.data.items[count].data.taxid;
                    jsonObj['sourceTaxname']=sourceMappedStore.data.items[count].data.taxname;
                    jsonObj['sourceTaxCode']=sourceMappedStore.data.items[count].data.taxcode;
                    jsonObj['destinationTaxid']=destinationMappedStore.data.items[count].data.taxid;
                    jsonObj['destinationTaxname']=destinationMappedStore.data.items[count].data.taxname;
                    jsonObj['destinationTaxCode']=destinationMappedStore.data.items[count].data.taxcode;
                    jsonObj['sourcecompanyid']=companyid;
                    jsonObj['destinationcompanyid']=this.destinationcompanyid;
                    jArray.push(jsonObj);
                }
            }
            this.parentObj.purchaseSalesTaxMappingKey=JSON.stringify(jArray);
        }
    },
    savePurchaseTermstoSalesTermsMapping:function(sourceMappedStore,destinationMappedStore,parentObj){
        if(this.calculaterowDiff(sourceMappedStore,destinationMappedStore)==0){
            var jArray=[];
            var totalStoreCount=destinationMappedStore.getCount();
            for(var count=0;count<totalStoreCount;count++){
                var jsonObj={};
                if(sourceMappedStore.data.items[count]!=undefined &&  destinationMappedStore.data.items[count]){
                    jsonObj['sourceTermName']=sourceMappedStore.data.items[count].data.term;
                    jsonObj['sourceTermid']=sourceMappedStore.data.items[count].data.id;
                    jsonObj['destinationTermName']=destinationMappedStore.data.items[count].data.term;
                    jsonObj['destinationTermid']=destinationMappedStore.data.items[count].data.id;
                    jsonObj['sourcecompanyid']=companyid;
                    jsonObj['destinationcompanyid']=this.destinationcompanyid;
                    jArray.push(jsonObj);
                }
            }
            this.parentObj.purchaseSalesInvoiceTermsMappingKey=JSON.stringify(jArray);
        }
    },
    savePurchaseModuletoSalesMapping:function(sourceMappedStore,destinationMappedStore,parentObj){
        if(this.calculaterowDiff(sourceMappedStore,destinationMappedStore)==0){
            var jArray=[];
            var totalStoreCount=destinationMappedStore.getCount();
            for(var count=0;count<totalStoreCount;count++){
                var jsonObj={};
                if(sourceMappedStore.data.items[count]!=undefined &&  destinationMappedStore.data.items[count]){
                    jsonObj['sourcemodule']=sourceMappedStore.data.items[count].data.moduleid;
                    jsonObj['sourcemodulename']=sourceMappedStore.data.items[count].data.modulename;
                    jsonObj['sourcecompanyid']=companyid;
                    jsonObj['destinationmodule']=destinationMappedStore.data.items[count].data.moduleid;
                    jsonObj['destinationmodulename']=destinationMappedStore.data.items[count].data.modulename;
                    jsonObj['destinationcompanyid']=this.destinationcompanyid;
                    jArray.push(jsonObj);
                }
            }
            this.parentObj.purchaseSalesModuleMappingKey=JSON.stringify(jArray);
        }
    },
    calculaterowDiff:function(store1,store2){
        return (store1.getCount()-store2.getCount());
    },
    checkTransactionsForDiscountOnPaymentTerms: function (obj, newval, oldval) {
        Wtf.Ajax.requestEx({
            url: "ACCReceiptNew/checkTransactionsForDiscountOnPaymentTerms.do"
        }, this, function (response) {
            if (response.msg != "" && response.msg != null && response.msg != undefined) {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.information"),
                    width: 500,
                    msg: response.msg,
                    buttons: Wtf.MessageBox.OK,
                    scope: this,
                    icon: Wtf.MessageBox.WARNING
                });
            } else {

            }
        }, function (response) {

        });
    }
 });
