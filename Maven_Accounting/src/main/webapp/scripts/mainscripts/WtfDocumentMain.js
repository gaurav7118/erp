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

Wtf.account.MainClosablePanel=function(config){
    Wtf.apply(this, config);
    Wtf.apply(this, {
        bbar: []
    });
    Wtf.account.MainClosablePanel.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.MainClosablePanel,Wtf.Panel,{
    closeMsg:WtfGlobal.getLocaleText("acc.msgbox.51"),  //"The data you filled is unsaved.Do you still want to close the panel?",
    autoScroll: true,// layout:'border',//Bug Fixed: 14871[SK]
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border:'false',
    externalcurrencyrate:0,
    exchangeratetype:"",
    revexternalcurrencyrate:0,
    isCurrencyLoad:false,
    currencyid:null,
    custdatechange:false,
    closable : true,
    cash:false,
    layout : 'border',
    isCustomer:false,
    cls : 'southcollapse',
    isCustBill:false,
    isOrder:false,
    fromOrder:false, 
    isClosable:true,
    initComponent:function(config){
        Wtf.account.MainClosablePanel.superclass.initComponent.call(this,config);
       
        //Create common stores required either for comboboxes, grid, or any other component.
        this.createCommonStore();
        //Add event to common stores
        this.addCommonStoreEvents();
        
        //Create common fields that will be used in all inheriting modules
        this.createCommonFields();
        //Append common fields in array
        this.appendCommonNorthFormFields();
        
        //Create common buttons
        this.createCommonButtons();
        //Append common buttons in an array
        this.appendCommonButtons()
        
        this.on('beforeclose', this.askToClose,this);
    },
    
    onRender:function(config){
        Wtf.account.MainClosablePanel.superclass.onRender.call(this, config);
        
        //Create center panel that will contain components like NorthForm, SouthForm, Grid etc.
        this.createCenterPanel();
        
        //Layout various components.
        this.doLayoutComponents();
        
        //Add components to center panel.
        this.addToCenterPanel();
        
        //Add center panel to main closable panel.
        this.addToMainClosablePanel();
    },

    initCommonValues: function(config){
        this.titlel = config.title!=undefined?config.title:"null";
        this.dataLoaded=false;
        this.isViewTemplate = (config.isViewTemplate!=undefined?config.isViewTemplate:false);
        this.isTemplate = (config.isTemplate!=undefined?config.isTemplate:false);
        this.createTransactionAlso = false;
        this.transactionType = 0;
        this.isCopyFromTemplate = (config.isCopyFromTemplate!=undefined?config.isCopyFromTemplate:false);
        this.isSelfBilledInvoice=(config.isSelfBilledInvoice!=undefined && config.isSelfBilledInvoice!=null)?config.isSelfBilledInvoice:false;
        this.sendMailFlag = false;
        this.saveOnlyFlag = false;
        this.mailFlag=false;
        this.isExpenseInv=false;
        this.isFromGrORDO = false;
        this.billid=null;
        this.custChange=false;
        this.termsincludegst = WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_termsincludegst);
        if (this.isEdit || this.copyInv){
            this.billid = this.record.data.billid;
        }
        this.productOptimizedFlag=WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_productOptimizedFlag);
        this.handleEmptyText=false; //To handle empty text after clicking on save button
        this.isMultiSelectFlag=false; // keep this flag for allow multiselection
        this.datechange=0;
        this.oldval="";
        this.val="";
        this.pronamearr=[];
        this.changeGridDetails=true;
        this.appendID = true;
        this.response = "";
        this.request = "";
        this.amountdue=0;
        this.gstCurrencyRate=0.0;
        this.CustomStore = "";
        this.termid="";
        this.currentAddressDetailrec="";
        this.productComboStore=this.isCustomer?Wtf.productStoreSales:Wtf.productStore;
        this.autoPopulateMappedProduct=WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autoPopulateMappedProduct)!=undefined? WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autoPopulateMappedProduct):false;
        this.custUPermType=config.isCustomer?WtfGlobal.getUPermObj(Wtf.UPerm_customer):WtfGlobal.getUPermObj(Wtf.UPerm_vendor);
        this.custPermType=config.isCustomer?WtfGlobal.getPermObj(Wtf.Perm_customer):WtfGlobal.getPermObj(Wtf.Perm_vendor);
        this.soUPermType=(config.isCustomer?WtfGlobal.getUPermObj(Wtf.UPerm_invoice):WtfGlobal.getUPermObj(Wtf.UPerm_vendorinvoice));
        this.soPermType=(config.isCustomer?WtfGlobal.getPermObj(Wtf.Perm_invoice_createso):WtfGlobal.getPermObj(Wtf.Perm_vendorinvoice_createpo));
        this.isWithInvUpdate = config.isWithInvUpdate;
        this.nameBeforeSelect="";
        this.createPaymentPermType=(config.isCustomer?WtfGlobal.getPermObj(Wtf.Perm_invoice_createreceipt):WtfGlobal.getPermObj(Wtf.Perm_vendorinvoice_createpayment));
        this.buttonArray = new Array();
        this.leftItemArr = new Array();
        this.rightItemArr = new Array();
        this.isDraft = false;
        this.tranType=null;
        this.custVenOptimizedFlag = WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_custvenloadtype);
        this.businessPerson=(this.isCustomer?'Customer':'Vendor');
        this.loadCurrFlag = true;
        if(!this.isCustBill){
            this.isCustBill = false;
        }
        this.transdate=(this.isEdit || this.copyInv ?WtfGlobal.convertToGenericDate(this.record.data.date):WtfGlobal.convertToGenericDate(new Date()));
        this.isEditORisCopy=(this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv!=undefined ?this.copyInv:false) ||(this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false);
    },
    
    createCommonStore: function(){
        this.termRec = new Wtf.data.Record.create([
            {name: 'termname'},
            {name: 'termdays'},
            {name: 'termid'}
        ]);
        this.termds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.termRec),
            url: "ACCTerm/getTerm.do",
            baseParams: {
                mode: 91
            }
        });
        
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'value'},
            {name: 'oldflag'}
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: this.modeName,
                isEdit: this.copyInv ? false : this.isEdit
            }
        });
        
        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid', mapping: 'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname', mapping: 'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.currencyRec),
            url: "ACCCurrency/getCurrencyExchange.do"
        });
        
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
            }, Wtf.personRec)
        });
        
        this.PORec = Wtf.data.Record.create([
            {name: 'billid'},
            {name: 'journalentryid'},
            {name: 'entryno'},
            {name: 'billto'},
            {name: 'discount'},
            {name: 'shipto'},
            {name: 'mode'},
            {name: 'billno'},
            {name: 'date', type: 'date'},
            {name: 'duedate', type: 'date'},
            {name: 'shipdate', type: 'date'},
            {name: 'personname'},
            {name: 'creditoraccount'},
            {name: 'personid'},
            {name: 'shipping'},
            {name: 'othercharges'},
            {name: 'taxid'},
            {name: 'discounttotal'},
            {name: 'isAppliedForTax'}, // in Malasian company if DO is applied for tax
            {name: 'discountispertotal', type: 'boolean'},
            {name: 'currencyid'},
            {name: 'currencysymbol'},
            {name: 'amount'},
            {name: 'amountinbase'},
            {name: 'amountdue'},
            {name: 'costcenterid'},
            {name: 'lasteditedby'},
            {name: 'costcenterName'},
            {name: 'memo'},
            {name: 'shipvia'},
            {name: 'fob'},
            {name: 'includeprotax', type: 'boolean'},
            {name: 'salesPerson'},
            {name: 'islockQuantityflag'},
            {name: 'agent'},
            {name: 'termdetails'},
            {name: 'gstIncluded'},
            {name: 'quotationtype'},
            {name: 'contract'},
            {name: 'termid'},
            {name: 'externalcurrencyrate'}, //    ERP-9886
            {name: 'customerporefno'},
            {name: 'billingAddressType'},
            {name: 'billingAddress'},
            {name: 'billingCountry'},
            {name: 'billingState'},
            {name: 'billingPostal'},
            {name: 'billingEmail'},
            {name: 'billingFax'},
            {name: 'billingMobile'},
            {name: 'billingPhone'},
            {name: 'billingContactPerson'},
            {name: 'billingRecipientName'},
            {name: 'billingContactPersonNumber'},
            {name: 'billingContactPersonDesignation'},
            {name: 'billingWebsite'},
            {name: 'billingCounty'},
            {name: 'billingCity'},
            {name: 'shippingAddressType'},
            {name: 'shippingAddress'},
            {name: 'shippingCountry'},
            {name: 'shippingState'},
            {name: 'shippingCounty'},
            {name: 'shippingCity'},
            {name: 'shippingEmail'},
            {name: 'shippingFax'},
            {name: 'shippingMobile'},
            {name: 'shippingPhone'},
            {name: 'shippingPostal'},
            {name: 'shippingContactPersonNumber'},
            {name: 'shippingContactPersonDesignation'},
            {name: 'shippingWebsite'},
            {name: 'shippingRecipientName'},
            {name: 'shippingContactPerson'},
            {name: 'shippingRoute'},
            {name: 'vendcustShippingAddress'},
            {name: 'vendcustShippingCountry'},
            {name: 'vendcustShippingState'},
            {name: 'vendcustShippingCounty'},
            {name: 'vendcustShippingCity'},
            {name: 'vendcustShippingEmail'},
            {name: 'vendcustShippingFax'},
            {name: 'vendcustShippingMobile'},
            {name: 'vendcustShippingPhone'},
            {name: 'vendcustShippingPostal'},
            {name: 'vendcustShippingContactPersonNumber'},
            {name: 'vendcustShippingContactPersonDesignation'},
            {name: 'vendcustShippingWebsite'},
            {name: 'vendcustShippingContactPerson'},
            {name: 'vendcustShippingRecipientName'},
            {name: 'vendcustShippingAddressType'}
        ]);
        
        this.includeTaxStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value', type: 'boolean'}],
            data: [['Yes', true], ['No', false]]
        });
        
        this.SelectedTemplateRec = Wtf.data.Record.create([
            {name: 'billid'},
            {name: 'journalentryid'},
            {name: 'entryno'},
            {name: 'billto'},
            {name: 'discount'},
            {name: 'currencysymbol'},
            {name: 'orderamount'},
            {name: 'isexpenseinv'},
            {name: 'currencyid'},
            {name: 'shipto'},
            {name: 'mode'},
            {name: 'billno'},
            {name: 'date', type: 'date'},
            {name: 'duedate', type: 'date'},
            {name: 'shipdate', type: 'date'},
            {name: 'personname'},
            {name: 'personemail'},
            {name: 'personid'},
            {name: 'shipping'},
            {name: 'othercharges'},
            {name: 'partialinv', type: 'boolean'},
            {name: 'includeprotax', type: 'boolean'},
            {name: 'amount'},
            {name: 'amountdue'},
            {name: 'termdays'},
            {name: 'termid'},
            {name: 'termname'},
            {name: 'incash'},
            {name: 'taxamount'},
            {name: 'taxid'},
            {name: 'orderamountwithTax'},
            {name: 'taxincluded', type: 'boolean'},
            {name: 'taxname'},
            {name: 'deleted'},
            {name: 'amountinbase'},
            {name: 'memo'},
            {name: 'externalcurrencyrate'},
            {name: 'ispercentdiscount'},
            {name: 'discountval'},
            {name: 'crdraccid'},
            {name: 'creditDays'},
            {name: 'isRepeated'},
            {name: 'porefno'},
            {name: 'costcenterid'},
            {name: 'costcenterName'},
            {name: 'interval'},
            {name: 'intervalType'},
            {name: 'startDate', type: 'date'},
            {name: 'nextDate', type: 'date'},
            {name: 'expireDate', type: 'date'},
            {name: 'repeateid'},
            {name: 'status'},
            {name: 'archieve', type: 'int'},
            {name: 'withoutinventory', type: 'boolean'},
            {name: 'rowproductname'},
            {name: 'rowquantity'},
            {name: 'rowrate'},
            {name: 'rowprdiscount'},
            {name: 'rowprtaxpercent'},
            {name: 'shipvia'},
            {name: 'fob'},
            {name: 'salesPerson'},
            {name: 'agent'},
            {name: 'termdetails'},
            {name: 'methodid'}

        ]);
    },
    
    addCommonStoreEvents: function(){
        //Add params and event to global stores.
        this.paramsForGlobalStore();
        
        this.currencyStore.on('load',this.changeTemplateSymbol,this);
        
        if (!this.isTemplate) {  // this check is added due to avoding issue of sequence number getting incremented in case of template not having create also transaction check (ERP-1518)
            this.sequenceFormatStore.on('load', this.setNextNumber, this);
        }
    },
    
    paramsForGlobalStore : function() {
        Wtf.customerAccRemoteStore.on('beforeload', function(s,o) {
            if(!o.params)o.params={};
            var currentBaseParams = Wtf.customerAccRemoteStore.baseParams;
            if(this.isEditORisCopy){
                currentBaseParams.isPermOrOnetime="";
            }else{
                if(this.ShowOnlyOneTime != undefined && this.ShowOnlyOneTime.getValue() == true){
                    currentBaseParams.isPermOrOnetime=true;
                }else{
                    currentBaseParams.isPermOrOnetime=false;
                }
            }
            Wtf.customerAccRemoteStore.baseParams=currentBaseParams;
        }, this);
        
        Wtf.customerAccRemoteStore.on("load", function() {
            var currentBaseParams = Wtf.customerAccRemoteStore.baseParams;
            currentBaseParams.isPermOrOnetime="";
            Wtf.customerAccRemoteStore.baseParams=currentBaseParams;
        }, this);
        
        //Global customer store load
        Wtf.customerAccStore.on('beforeload', function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = Wtf.customerAccStore.baseParams;
            if(this.isEditORisCopy){
                currentBaseParams.isPermOrOnetime="";
            }else{
                if(this.ShowOnlyOneTime != undefined && this.ShowOnlyOneTime.getValue() == true){
                    currentBaseParams.isPermOrOnetime=true;
                }else{
                    currentBaseParams.isPermOrOnetime=false;
                }
            }
            Wtf.customerAccStore.baseParams=currentBaseParams;
        }, this);
        
        Wtf.customerAccStore.on("load", function() {
            var currentBaseParams = Wtf.customerAccStore.baseParams;
            currentBaseParams.isPermOrOnetime="";
            Wtf.customerAccStore.baseParams=currentBaseParams;
        }, this);
    },
    
    createCommonFields: function(){
        this.CurrencyConfig = {
            fieldLabel: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.currency.tt")+"'>"+ WtfGlobal.getLocaleText("acc.currency.cur") +" *"  +"</span>",
            hiddenName:'currencyid',
            name:'currencyid',
            id:"currency"+this.heplmodeid+this.id,
            typeAhead: false, //ERP-11388
            allowBlank : false
        };
        this.Currency = WtfGlobal.createFnCombobox(this.CurrencyConfig, this.currencyStore, 'currencyid', 'currencyname', this);
        
        this.sequenceFormatComboboxConfig = {
            mode: 'local',
            id: 'sequenceFormatCombobox' + this.heplmodeid + this.id,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.Sequenceformat.tip") + "'>" + WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat") + "</span>",
            disabled: (this.isEdit && !this.copyInv && !(!this.isCustomer && this.ispurchaseReq) ? true : false),
            name: 'sequenceformat',
            hiddenName: 'sequenceformat',
            allowBlank: false,
            width: 240,
            listeners: {
                'select': {
                    fn: this.getNextSequenceNumber,
                    scope: this
                }
            }
        };
        this.sequenceFormatCombobox = WtfGlobal.createCombobox(this.sequenceFormatComboboxConfig, this.sequenceFormatStore, 'id', 'value', this);
        
        this.Memo = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_descriptionType), //'Memo/Note',
            name: 'memo',
            hiddenName: 'memo',
            id: "memo" + this.heplmodeid + this.id,
            height: 40,
            width: 240,
            readOnly: this.isViewTemplate,
            maxLength: 2048,
            qtip: (this.record == undefined) ? ' ' : this.record.data.memo,
            listeners: {
                render: function(c) {
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }
        });
        
        this.includingGST = new Wtf.form.Checkbox({
            name: 'includingGST',
            hiddenName: 'includingGST',
            id: "includingGST" + this.heplmodeid + this.id,
            hideLabel: (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137'), // hide if company is malaysian
            hidden: (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137'), // hide if company is malaysian
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.includeGST.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.cust.includingGST") + "</span>",
            cls: 'custcheckbox',
            width: 10
        });
        
        this.includeProTaxConfig = {
            mode: 'local',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.includeprodtax.tip") + "'>" + WtfGlobal.getLocaleText("acc.invoice.productTax") + "</span>", //"Include Product Tax",
            id: "includeprotax" + this.heplmodeid + this.id,
            value: (this.isEdit ? true : false),
            width: 240,
            hideLabel: (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && !WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_enableGST)), // hide if company is malaysian and GST is not enabled for it
            hidden: (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && !WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_enableGST)), // hide if company is malaysian and GST is not enabled for it
            name: 'includeprotax',
            hiddenName: 'includeprotax',
            listeners: {
                'change': {
                    fn: this.includeProTaxHandler,
                    scope: this
                }
            }
        };
        this.includeProTax = WtfGlobal.createCombobox(this.includeProTaxConfig, this.includeTaxStore, 'value', 'name', this);
        
        this.billDateConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.he.12") + "'>" + ((this.isEdit ? this.label : this.label)) + ' ' + WtfGlobal.getLocaleText("acc.invoice.date") + "</span>", //(this.isPOfromSO?WtfGlobal.getLocaleText("acc.accPref.autoPO"):(this.isSOfromPO)?WtfGlobal.getLocaleText("acc.accPref.autoSO"):(this.isEdit?this.label:this.label)) +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id: "invoiceDate" + this.heplmodeid + this.id,
            name: 'billdate',
            hiddenName: 'billdate',
            maxValue: null,
            listeners: {
                'change': {
                    fn: this.updateDueDate,
                    scope: this
                }
            }
        };
        this.billDate = WtfGlobal.createDatefield(this.billDateConfig, (this.isTemplate && !this.createTransactionAlso), this);
        
        this.shipviaConfig = {
            fieldLabel: (companyid == PacificTechCompanyId) ? WtfGlobal.getLocaleText("acc.field.LeadTime") : "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.ShipVia.tt") + "'>" + WtfGlobal.getLocaleText("acc.field.ShipVia") + "</span>",
            name: 'shipvia',
            hiddenName: 'shipvia',
            id: "shipvia" + this.heplmodeid + this.id
        };
        this.shipvia = WtfGlobal.createTextfield(this.shipviaConfig, false, true, 255, this);
        
        this.fobConfig = {
            fieldLabel: (companyid == PacificTechCompanyId) ? WtfGlobal.getLocaleText("acc.field.DeliveryTerm") : "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.fob.tip") + "'>" + WtfGlobal.getLocaleText("acc.field.FOB") + "</span>",
            name: 'fob',
            hiddenName: 'fob',
            id: "fob" + this.heplmodeid + this.id
        };
        this.fob = WtfGlobal.createTextfield(this.fobConfig, false, true, 255, this);
        
        this.editedByConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.field.LastEditedBy"),
            name: 'lasteditedby',
            hiddenName: 'lasteditedby',
            id: "lasteditedby" + this.heplmodeid + this.id,
            hidden: (this.isEdit && !this.templateId) ? false : true,
            hideLabel: (this.isEdit && !this.templateId) ? false : true
        };
        this.editedBy = WtfGlobal.createTextfield(this.editedByConfig, true, true, 255, this);
        
        this.NameConfig = {
            fieldLabel:this.nameFieldLabel,
            hiddenName: this.businessPerson.toLowerCase(),
            name: this.businessPerson.toLowerCase(),
            id: "customer" + this.heplmodeid + this.id,
            minChars: 1,
            extraFields: WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_accountsWithCode) ? ['acccode'] : [],
            listWidth: WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_accountsWithCode) ? 550 : 400,
            allowBlank: false,
            hirarchical: true,
            emptyText: this.isCustomer ? WtfGlobal.getLocaleText("acc.inv.cus") : WtfGlobal.getLocaleText("acc.inv.ven"),
            extraComparisionField: 'acccode'// type ahead search on acccode as well.
        };
        if (this.custVenOptimizedFlag) {
            this.NameConfig.mode = 'remote';
            this.NameConfig.hideTrigger = true;
            this.NameConfig.ctCls = 'optimizedclass';
            this.Name = WtfGlobal.createExtFnCombobox(this.NameConfig, ((this.isCustomer ? Wtf.customerAccRemoteStore : Wtf.vendorAccRemoteStore)), 'accid', 'accname', this);
        } else {
            this.NameConfig.mode = 'local';
            this.Name = WtfGlobal.createExtFnCombobox(this.NameConfig, ((this.isCustomer ? Wtf.customerAccStore : Wtf.vendorAccStore)), 'accid', 'accname', this);
        }
        if (!WtfGlobal.EnableDisable(this.custUPermType, this.custPermType.create)){
            this.Name.addNewFn = this.addPerson.createDelegate(this, [false, null, this.businessPerson + "window", this.isCustomer], true);
        }
        
        this.NumberConfig = {
            fieldLabel: ((this.isEdit ? this.label : this.label)) + " " + ((this.isTemplate) ? 'Number' : WtfGlobal.getLocaleText("acc.common.number")),
            name: 'number',
            hiddenName: 'number',
            id: "invoiceNo" + this.heplmodeid + this.id,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenternumber")
        };
        this.Number = WtfGlobal.createTextfield(this.NumberConfig, (this.isEdit && !this.copyInv ? true : false), (this.checkin || (this.isTemplate && !this.createTransactionAlso)), 50, this);
        
        this.usersConfig = {
            triggerAction: 'all',
            mode: 'local',
            id: "salesperson" + this.heplmodeid + this.id,
            addNoneRecord: true,
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.masterConfig.15") : WtfGlobal.getLocaleText("acc.masterConfig.20"),
            emptyText: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectSalesPerson") : WtfGlobal.getLocaleText("acc.field.SelectAgent"),
            name: this.isCustomer ? 'salesPerson' : 'agent',
            hiddenName: this.isCustomer ? 'salesPerson' : 'agent'
        };
        this.users = WtfGlobal.createFnCombobox(this.usersConfig, (this.isCustomer ? Wtf.salesPersonFilteredByCustomer : Wtf.agentStore), 'id', 'name', this);
        this.users.addNewFn = this.addSalesPerson.createDelegate(this);
        
        this.shipDateConfig = {
            fieldLabel: "<span wtf:qtip='" + this.shipDateFieldLabelToolTip + "'>" + this.shipDateFieldLabel + "</span>",
            id: "shipdate" + this.heplmodeid + this.id,
            name: 'shipdate',
            hiddenName: 'shipdate',
            listeners: {
                'change': {
                    fn: this.updateDueDate,
                    scope: this
                }
            }
        };
        this.shipDate = WtfGlobal.createDatefield(this.shipDateConfig, true, this);
    },
    
    appendCommonNorthFormFields: function(){
        this.leftItemArr.push(
            this.Name, 
            this.Currency,
            this.sequenceFormatCombobox,
            this.Number,
            this.billDate,
            this.editedBy
        );
            
        this.rightItemArr.push(
            this.shipDate,
            this.Memo,
            this.shipvia,
            this.fob,
            this.includingGST,
            this.includeProTax,
            this.users
        );
    },
    
    createCommonButtons: function(){
        this.saveBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            id: "save" + this.heplmodeid+ this.id,
            hidden:this.isViewTemplate,
            scope: this,
            handler: function(){
                this.isDraft = false;
                this.saveOnlyFlag = true;
                this.disableSaveButtons();
                if(this.isTemplate){
                    if(this.moduleTemplateName.getValue() == ''){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.PleaseEnterTemplateNameFirst")], 2);
                        this.enableSaveButtons();
                        return;
                    }                
                    this.saveTemplate();
                }else{
                    this.save();
                }
            },
            iconCls: 'pwnd save'
        });
        
        this.savencreateBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
            id: "savencreate" + this.heplmodeid + this.id,
            hidden : (this.isEdit&&!this.isCopyFromTemplate) || (this.copyInv&&!this.isCopyFromTemplate) || this.isTemplate || this.isViewTemplate,
            scope: this,
            handler: function(){
                this.isDraft = false;
                this.saveOnlyFlag = false;
                this.disableSaveButtons();
                this.save();
            },
            iconCls: 'pwnd save'
        });
        
        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
            this.singlePrint=new Wtf.exportButton({
                obj:this,
                id:"exportpdf" + this.id,
                iconCls: 'pwnd exportpdfsingle',
                text:WtfGlobal.getLocaleText("acc.field.ExportPDF"),// + " "+ singlePDFtext,
                tooltip :WtfGlobal.getLocaleText("acc.rem.39.single"),  //'Export Single Record details',
                disabled :true,
                isEntrylevel:true,
                exportRecord:this.exportRecord,
                hidden:this.isRequisition || this.isRFQ || this.isSalesCommissionStmt,
                menuItem:{rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ this.singlePDFtext},
                get:this.tranType,
                moduleid:this.moduleid
            });
        }
        
        this.postTextButtonConfig = {
            text:  WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
            cls: 'pwnd add',
            id: "posttext" + this.id,        
            //hidden:(config.moduleid!=Wtf.Acc_Invoice_ModuleId && config.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId),        
            tooltip : WtfGlobal.getLocaleText("acc.field.UsePostTextoptiontoinserttextafterSignature"),       
            style:" padding-left: 15px;",
            scope: this,
            hidden:this.isTemplate || this.isViewTemplate,
            handler: function() {
                this.getPostTextEditor(this.postText);
            }
        };
        
        this.showAddressButtonConfig = {
            text: WtfGlobal.getLocaleText("acc.field.ShowAddress"),  //WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
            cls: 'pwnd add',
            id: "showaddress" + this.id,                
            tooltip : WtfGlobal.getLocaleText("acc.field.UseShowAddressoptiontoinsertAddresses"),       
            style:" padding-left: 15px;",
            scope: this,
            disabled : true, 
            handler:this.getAddressWindow 
        };
    },
    
    appendCommonButtons: function(){
        this.buttonArray.push(this.saveBttn);
        this.buttonArray.push(this.savencreateBttn);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
            this.buttonArray.push(this.singlePrint);
        }
        this.buttonArray.push(this.postTextButtonConfig);
        this.buttonArray.push(this.showAddressButtonConfig);
    },
    
    addButtonsTobbar: function(){
        this.outerPanel.bottomToolbar = this.buttonArray;
    },
    
    createCommonSouthPanelFields: function(){
        this.tplSummary=new Wtf.XTemplate(
            '<div class="currency-view">',
            '<table width="100%">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td text-align=right>{subtotal}</td></tr>',
            '</table>',
            '<table width="100%">',
            '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.invoice.Tax")+': </b></td><td align=right>{tax}</td></tr>',
            '</table>',
            '<table width="100%">',
            '</table>',
            '<hr class="templineview">',
            '<table width="100%">',
            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmt")+' </b></td><td align=right>{aftertaxamt}</td></tr>',
            '</table>',
            '<table width="100%">',
            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmtInBase")+' </b></td><td align=right>{totalAmtInBase}</td></tr>',
            '</table>',
            '<hr class="templineview">',
            '<hr class="templineview">',
            '</div>'
        );
        if(this.IsInvoiceTerm) { //customer invoice
            this.tplSummary=new Wtf.XTemplate(
                '<div class="currency-view">',
                '<table width="100%">',
                '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td text-align=right>{subtotal}</td></tr>',
                '</table>',
                '<table width="100%">',
                '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.invoice.Tax")+': </b></td><td align=right>{tax}</td></tr>',
                '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.field.InvoiceTerm")+' </b></td><td align=right>{termtotal}</td></tr>',
                '</table>',
                '<table width="100%">',
                '</table>',
                '<hr class="templineview">',
                '<table width="100%">',
                '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmt")+' </b></td><td align=right>{aftertaxamt}</td></tr>',
                '</table>',
                '<table width="100%">',
                '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmtInBase")+' </b></td><td align=right>{totalAmtInBase}</td></tr>',
                '</table>',
                '<hr class="templineview">',
                 '</table>',
                '<table width="100%">',
                '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.inv.amountdue")+' </b></td><td align=right>{amountdue}</td></tr>',
                '</table>',
                '<hr class="templineview">',
                '<hr class="templineview">',
                '</div>'
            );
        }
        
        this.productDetailsTplSummary=new Wtf.XTemplate(
            '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            '<div><hr class="templineview"></div>',
            '<div>',
            '<table width="100%">'+
            '<tr>'+
            '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.field.ProductName")+'</b></td><td style="width:55%;"><span wtf:qtip="{productname}">'+Wtf.util.Format.ellipsis('{productname}',60)+'</span></td>'+                   
            '</tr>'+
            '<tr>'+
            '<td><b>'+WtfGlobal.getLocaleText("acc.field.InStock")+': </b></td><td style="width:10%;">{qty}</td>'+
            "<td><b>"+WtfGlobal.getLocaleText("acc.field.OpenPO")+": </b></td><td style='width:10%;'><a href='#' onclick='Showproductdetails(\"{productid}\",\"{productname}\",false)'>{poqty}</a></td>"+  
            "<td><b>"+WtfGlobal.getLocaleText("acc.field.OpenSO")+": </b></td><td style='width:30%;'><a href='#' onclick='Showproductdetails(\"{productid}\",\"{productname}\",true)'>{soqty}</a></td>"+         //provided link on wich we will get product quantity details
            '</tr>'+
            '</table>'+
            '</div>',            
            '<div><hr class="templineview"></div>',                        
            '</div>'
        );
            
        var blockSpotRateLink_first = "";
        var blockSpotRateLink_second = "";
        if(!WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateToBlockSpotRate)){ // If activateToBlockSpotRate is set then block the Spot Rate Links
            blockSpotRateLink_first = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div><div style='padding-left:30px;padding-top:5px;padding-bottom:10px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{foreigncurrency}\",\"{basecurrency}\",\"{revexchangerate}\",\"foreigntobase\")'wtf:qtip=''>{foreigncurrency} to {basecurrency}</a>";
            blockSpotRateLink_second = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div> <div style='padding-left:30px;padding-top:5px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\",\"basetoforeign\")'wtf:qtip=''>{basecurrency} to {foreigncurrency}</a></div>";
        }
        this.southCenterTplSummary=new Wtf.XTemplate(
            "<div> &nbsp;</div>",  //Currency:
            '<tpl if="editable==true">',
            "<b>"+WtfGlobal.getLocaleText("acc.invoice.msg8")+"</b>",  //Applied Exchange Rate for the current transaction:
            "<div style='line-height:18px;padding-left:30px;'>1 {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+" = {revexchangerate} {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+". "+
            blockSpotRateLink_first,
            "</div><div style='line-height:18px;padding-left:30px;'>1 {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+" = {exchangerate} {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+". "+    
            blockSpotRateLink_second,
            '</tpl>'
        );
            
        this.productDetailsTpl=new Wtf.Panel({
            id:'productDetailsTpl'+this.id,
            border:false,
            baseCls:'tempbackgroundview',
            width:'95%',
            hidden:(this.isCustBill)?true:false,
            html:this.productDetailsTplSummary.apply({productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0})
        });
        
        this.southCenterTpl=new Wtf.Panel({
            border:false,
            html:this.southCenterTplSummary.apply({basecurrency:WtfGlobal.getCurrencyName(),exchangerate:'x',foreigncurrency:"Foreign Currency", editable:false})
        });
        
        this.prodDetailSouthItems = [];
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
            this.prodDetailSouthItems = [this.productDetailsTpl,this.productProfitMarginTpl,this.southCenterTpl];
        }else{
            this.prodDetailSouthItems = [this.productDetailsTpl,this.southCenterTpl];
        }
        if(this.IsInvoiceTerm) {
            this.addInvoiceTermGrid(this.isEdit);
            this.prodDetailSouthItems.push(this.InvoiceTermGridPanel);
        }
        
        this.isTaxableConfig = {
            mode: 'local',
            id:"includetax"+this.heplmodeid+this.id,
            listWidth:50,
            fieldLabel:WtfGlobal.getLocaleText("acc.inv.totax"),  //"Include Total Tax",
            allowBlank:this.isOrder,
            value:false,
            hideLabel:(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && !WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_enableGST)),// hide if company is malaysian and GST is not enabled for it
            hidden:(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && !WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_enableGST)),// hide if company is malaysian and GST is not enabled for it
            width:50,
            name:'includetax',
            hiddenName:'includetax',
            listeners:{
                'select':{
                    fn:this.enabletax,
                    scope:this
                }
            }
        };
        this.isTaxable = WtfGlobal.createCombobox(this.isTaxableConfig, this.fromPOStore, 'value', 'name', this);
        
        this.TaxConfig = {
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //'Tax',
            id:"tax"+this.heplmodeid+this.id,
            disabled:!this.isEdit,
            hiddenName:'tax',
            anchor: '97%',
            hideLabel:(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && !WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_enableGST)),// hide if company is malaysian and GST is not enabled for it
            hidden:(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && !WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_enableGST)),// hide if company is malaysian and GST is not enabled for it
            listeners:{
                'select':{
                    fn:this.callGSTCurrencyRateandUpdateSubtotal,
                    scope:this
                }
            }
        };
        this.Tax = WtfGlobal.createFnCombobox(this.TaxConfig, this.Grid.taxStore, 'prtaxid', 'prtaxname', this);
        if(!WtfGlobal.EnableDisable(WtfGlobal.getUPermObj(Wtf.UPerm_tax), WtfGlobal.getPermObj(Wtf.Perm_tax_view))){
            this.Tax.addNewFn=this.addTax.createDelegate(this);
        }
        
        this.southCalTemp=new Wtf.Panel({  
            border:false,
            baseCls:'tempbackgroundview',
            html:this.tplSummary.apply({subtotal:WtfGlobal.currencyRenderer(0),discount:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),termtotal:WtfGlobal.currencyRenderer(0),amountdue:WtfGlobal.currencyRenderer(0)})
        });
    },
    
    createCenterPanel: function(){
        this.centerPanel = new Wtf.Panel({
            region : 'center',
            border : false,
            autoScroll : true
        });
        
        this.outerPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
                this.centerPanel,
                {
                    border: true,
                    id: 'south' + this.id,
                    region: 'south',
                    hidden : this.isCustBill,
                    layout: 'fit',
                    height:130 ,
                    plugins : new Wtf.ux.collapsedPanelTitlePlugin(),
                    collapsibletitle : WtfGlobal.getLocaleText("acc.common.recentrec") + " " +this.businessPerson + " "+WtfGlobal.getLocaleText("acc.field.fortheProduct"),
                    title : WtfGlobal.getLocaleText("acc.common.recentrec") + " " +this.businessPerson + " "+WtfGlobal.getLocaleText("acc.field.fortheProduct"),
                    collapsible: true,
                    collapsed: true,
                    items : [
                        this.lastTransPanel
                    ]
                }
            ],
            bbar: this.buttonArray
        });
    },
    
    addToCenterPanel: function(){
        this.centerPanel.add(this.NorthForm);
        if(this.isCustomer||this.isCustBill||this.isOrder||this.isEdit && !this.isExpenseInv||this.copyInv && !this.isExpenseInv || this.isTemplate) {
            this.centerPanel.add(this.SouthForm);
            this.centerPanel.add(this.toggleBtnPanel);
            this.centerPanel.add(this.Grid);
        } else if((this.isEdit && this.isExpenseInv) || (this.copyInv && this.isExpenseInv)) {
            if(this.ExpenseGrid){
                this.centerPanel.add(this.SouthForm);
                this.centerPanel.add(this.ExpenseGrid);
            }
        } else {
            if(this.GridPanel){
                this.centerPanel.add(this.SouthForm);
                this.centerPanel.add(this.toggleBtnPanel);
                this.centerPanel.add(this.GridPanel);
            }
        }
        this.centerPanel.add(this.southPanel);
    },
    
    appendSouthPanelFields: function(){
        this.southPanelItemArr = [];
        this.southPanelItemArr.push(
            {
                columnWidth: .45,// width: 570,//region:'center',
                border:false,
                items:this.prodDetailSouthItems
            },{
                id : this.id + 'southEastPanel',
                columnWidth: .55,//width:650,
                border:false,
                layout:'column',
                items:[{
                    layout:'form',
                    width:170,
                    labelWidth:100,
                    border:false,
                    items:this.isTaxable
                },{
                    layout:'form',
                    columnWidth:0.4,
                    labelWidth:30,
                    border:false,
                    items:this.Tax
                },{
                    columnWidth:0.6,
                    layout:'form',
                    cls:'bckgroundcolor',
                    bodyStyle:'padding:10px',
                    labelWidth:70,
                    items:this.southCalTemp
               }]
            }
        );
    },
    
    addToMainClosablePanel: function(){
        this.add(this.outerPanel);
    },
    
    doLayoutComponents: function(){
        this.NorthForm.doLayout();
        this.southPanel.doLayout();
    },
    
    updateStoreReaderForCustOrDim: function(){
        var DimensionCustomFielsArray = GlobalDimensionCustomFieldModel[this.moduleid];
        if( DimensionCustomFielsArray){
           for(var cnt = 0;cnt < DimensionCustomFielsArray.length;cnt++){
               if(DimensionCustomFielsArray[cnt] != undefined){
                    var fieldname = DimensionCustomFielsArray[cnt].fieldname;
                    var newField = new Wtf.data.Field({
                        name:fieldname.replace(".",""),
                        type:DimensionCustomFielsArray[cnt].fieldtype == 3 ?  'date' : (DimensionCustomFielsArray[cnt].fieldtype == 2?'float':'auto'),
                        format:DimensionCustomFielsArray[cnt].fieldtype == 3 ?  'y-m-d' : undefined
                    });
                    this.POStore.fields.items.push(newField);
                    this.POStore.fields.map[fieldname]=newField;
                    this.POStore.fields.keys.push(fieldname);
               }
           }
           this.POStore.reader = new Wtf.data.KwlJsonReader(this.POStore.reader.meta, this.POStore.fields.items);
        }
    },
    
    createCustOrDimFields: function(isViewMode, isEdit){
        var tagsFieldset = new Wtf.account.CreateCustomFields({
           border: false,
           compId: "northForm"+this.id,
           autoHeight: true,
           parentcompId:this.id,
           moduleid: this.moduleid,
           record: this.record,
           isViewMode: isViewMode,
           isEdit: isEdit
        });
        
        return tagsFieldset;
    },
    
    createNorthForm: function(labelWidth, leftcolumnwidth, rightcolumnwidth){
        this.NorthForm=new Wtf.form.FormPanel({
            region:'north',
            autoHeight:true,
            id:"northForm"+this.id,
            border:false,
            disabledClass:"newtripcmbss",
            items:[{
                layout:'form',
                baseCls:'northFormFormat',
                defaults:{labelWidth:labelWidth},
                cls:"visibleDisabled",
                items:[
                        {
                            xtype: 'panel',
                            id: this.id + 'requiredfieldmessagepanel',
                            hidden: true,
                            cls: 'invalidfieldinfomessage'
                        },
                        {
                        layout:'column',
                        border:false,
                        defaults:{border:false},
                        items:[{
                            layout:'form',
                            columnWidth:leftcolumnwidth,
                            border:false,
                            items:this.leftItemArr
                        },{
                            layout:'form',
                            columnWidth:rightcolumnwidth,
                            border:false,
                            items:this.rightItemArr
                        }]
                    }, 
                    this.tagsFieldset
                ]
            }]
        });
    },
    
    createSouthPanel: function(disabled, height){
        this.southPanel=new Wtf.Panel({
            region: 'center',
            border: false,
            disabled: disabled,
            disabledClass: "newtripcmbss",
            style: 'padding:0px 10px 10px 10px',
            layout: 'column',//layout:'border',//Bug Fixed: 14871[SK] Scrolling issue : changed layout from border to column
            height: height,
            items:this.southPanelItemArr
        });
    },
    
    askToClose:function(){
        if(this.mailFlag || this.isViewTemplate){ // mailFlag shows that at the time of creation of invoice if we press sav button then whole component will be disabled and at close action of that tab no msg will be displayed.
            this.ownerCt.remove(this);
            return;
        }
        if(this.isClosable!==true){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.msgbox.51"),  //this.closeMsg,
                width:500,
                buttons: Wtf.MessageBox.YESNO,
                animEl: 'mb9',
                fn:function(btn){
                    if(btn!="yes")return;
                    this.ownerCt.remove(this);
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
        }
        return this.isClosable;
    },
    
    populateDimensionData:function(record){
        WtfGlobal.resetCustomFields(this.tagsFieldset);
        var fieldArr = this.POStore.fields.items;
        for (var fieldCnt = 0; fieldCnt < fieldArr.length; fieldCnt++) {
            var fieldN = fieldArr[fieldCnt];
            if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id) && record.data[fieldN.name] != "") {
                if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id).getXType() == 'datefield') {
                    Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                } else if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id).getXType() == 'fncombo') {
                    var ComboValue = record.data[fieldN.name];
                    if (ComboValue) {
                        Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue(ComboValue);
                        var childid = Wtf.getCmp(fieldN.name + this.tagsFieldset.id).childid;
                        if (childid.length > 0) {
                            var childidArray = childid.split(",");
                            for (var i = 0; i < childidArray.length; i++) {
                                var currentBaseParams = Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.baseParams;
                                currentBaseParams.parentid = ComboValue;
                                Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.baseParams = currentBaseParams;
                                Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.load();
                            }
                        }
                    }
                } else {
                    Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                }
            }
            if (fieldN.name.indexOf("Custom_") == 0) {
                var fieldname = fieldN.name.substring(7, fieldN.name.length);
                if (Wtf.getCmp(fieldname + this.tagsFieldset.id) && record.data[fieldN.name] != "") {
                    if (Wtf.getCmp(fieldname + this.tagsFieldset.id).getXType() == 'fieldset') {
                        var ComboValue = record.json[fieldN.name];
                        var ComboValueArrya = ComboValue.split(',');
                        var ComboValueID = "";
                        var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;
                        for (var i = 0; i < ComboValueArrya.length; i++) {
                            for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                if (checkListCheckBoxesArray[checkitemcnt].id.indexOf(ComboValueArrya[i]) != -1)
                                    if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                        Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                    }
                            }
                        }
                    } else if (Wtf.getCmp(fieldname + this.tagsFieldset.id).getXType() == 'select') {
                        var ComboValue = record.json[fieldN.name];
                        if (ComboValue != "" && ComboValue != undefined)
                            Wtf.getCmp(fieldname + this.tagsFieldset.id).setValue(ComboValue);
                    }

                }
            }
        } 
    },
    
    addInvoiceTermGrid: function() {
        this.termcm = [{
                header: WtfGlobal.getLocaleText("acc.field.Term"),
                dataIndex: 'term'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.Percentage"),
                dataIndex: 'termpercentage',
                editor: new Wtf.form.NumberField({
                    xtype: "numberfield",
                    maxLength: 15,
                    allowNegative: false,
                    minValue: 0,
                    maxValue: 100,
                    regexText: Wtf.MaxLengthText + "15"
                })
            }, {
                header: WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
                dataIndex: 'termamount',
                renderer: function(val, meta, rec) {
                    if (typeof val == 'number' && val >= 0 && rec.data.sign == 0) {
                        rec.set('termamount', val * (-1));
                        return val * (-1)
                    } else
                        return val;
                },
                editor: new Wtf.form.NumberField({
                    xtype: "numberfield",
                    maxLength: 15,
                    allowNegative: true,
                    regexText: Wtf.MaxLengthText + "15"
                })
            }
        ];
        this.termRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'term'},
            {name: 'glaccount'},
            {name: 'sign'}, 
            {name: 'formula'}, 
            {name: 'formulaids'}, 
            {name: 'termamount'}, 
            {name: 'termpercentage'}
        ]);
        this.termStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.termRec),
            url: 'ACCAccount/getInvoiceTermsSales.do',
            baseParams: {
                isSalesOrPurchase: this.isCustomer ? true : false
            }
        });
        if (this.IsInvoiceTerm) {
            this.termStore.load();
        }
        this.termStore.on('load', this.closeTermGrid, this);
        this.termgrid = new Wtf.grid.EditorGridPanel({
            clicksToEdit: 1,
            store: this.termStore,
            height: 100,
            autoScroll: true,
            disabledClass: "newtripcmbss",
            cm: new Wtf.grid.ColumnModel(this.termcm),
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.InvoiceTermGridPanel = new Wtf.Panel({
            id: "invoicetermgridpanel" + this.id,
            height: 130,
            border: false,
            style: 'padding-top:10px;',
            items: [
                {
                    border: false,
                    layout: 'table',
                    /*
                     *  Due to new improvement for Invoice terms below setting is removed from system
                     *  i.e. from system control also.
                     */
                    items: [{
                            width: 150,
                            border: false,
                            bodyStyle: 'padding-top:10px;',
                            html: 'Terms Inclusive of GST:',
                            hidden: true
                        }, this.TermsIncludeGST_YES = new Wtf.form.Radio({
                            xtype: 'radio',
                            disabled: true,
                            checked: WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_termsincludegst) == true,
                            width: 50,
                            fieldLabel: 'Terms Inclusive of GST',
                            hidden: true
                        }), this.TermsIncludeGST_NO = new Wtf.form.Radio({
                            xtype: 'radio',
                            disabled: true,
                            checked: WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_termsincludegst) == false,
                            width: 50,
                            fieldLabel: 'Terms Inclusive of GST1',
                            hidden: true
                        })
                    ]
                },
                this.termgrid
            ]
        });

        this.termgrid.on('afteredit', function(obj) {
            if (obj.field == 'termamount') {
                obj.record.set('termpercentage', '');
            } else if (obj.field == 'termpercentage' && obj.value == 0) {
                obj.record.set('termpercentage', '');
            }
            this.updateSubtotalOnTermChange(true);
        }, this);
    },
    
    closeTermGrid: function(obj) {
        var store = this.termgrid.store;
        if (this.termStore.data.length == 0)
        {
            this.InvoiceTermGridPanel.hide();
        }
   },
   
   setTermValues: function(termDetails) {
        if (termDetails != "" && termDetails != null && termDetails != undefined) {
            var detailArr = eval(termDetails);
            for (var cnt = 0; cnt < detailArr.length; cnt++) {
                var jObj = detailArr[cnt];

                var record = this.termStore.queryBy(function(record) {
                    return (record.get('id') == jObj.id);
                }, this).items[0];
                if (record) {
                    record.set('termamount', jObj.termamount);
                    record.set('termpercentage', jObj.termpercentage == 0 ? "" : jObj.termpercentage);
                }
            }
        }
    },
    
    updateSubtotalOnTermChange: function(updateTotalSummary) {
        for (var i = 0; i < this.termStore.getCount(); i++) {
            var recdata = this.termStore.getAt(i).data; //  var recdata = obj.record.data;
            var store = this.termStore;
            var subtotal = this.calProdSubtotalWithoutDiscount();
            var formula = recdata.formulaids.split(",");
            var termtotal = 0;
            for (var cnt = 0; cnt < formula.length; cnt++) {
                if (formula[cnt] == 'Basic') {
                    termtotal += (subtotal);
                }
                var record = store.queryBy(function(record) {
                    return (record.get('id') == formula[cnt]);
                }, this).items[0];
                if (record && (typeof record.data.termamount == 'number')) {
                    termtotal += (record.data.termamount);
                }
            }
            if (typeof (recdata.termpercentage) != "string" && parseInt(recdata.termpercentage) >= 0) {
                var opmod = recdata.sign == 0 ? -1 : 1;
                var this_termTotal = ((Math.abs(termtotal) * recdata.termpercentage * 1) / 100) * opmod;
                this.termStore.getAt(i).set('termamount', getRoundedAmountValue(this_termTotal));
            }
        }
        if (updateTotalSummary) {
            this.updateSubtotal();
        }
    },
    
    findTermsTotal: function() {
        var termTotal = 0;
        if (this.termgrid) {
            var store = this.termgrid.store;
            var totalCnt = store.getCount();
            for (var cnt = 0; cnt < totalCnt; cnt++) {
                var lineAmt = store.getAt(cnt).data.termamount;
                if (typeof lineAmt == 'number')
                    termTotal += getRoundedAmountValue(lineAmt);
            }
        }
        return getRoundedAmountValue(termTotal);
    },
    
    findTermsTotalInBase: function() {
        var termTotal = 0;
        if (this.termgrid) {
            var store = this.termgrid.store;
            var totalCnt = store.getCount();
            for (var cnt = 0; cnt < totalCnt; cnt++) {
                var lineAmt = store.getAt(cnt).data.termamount;
                if (typeof lineAmt == 'number') {
                    var termVal = getRoundedAmountValue(lineAmt);
                    termTotal += this.calAmountInBase(termVal);
                }
            }
        }
        return getRoundedAmountValue(termTotal);
    },
    
    getInvoiceTermDetails: function() {
        var arr = [];
        if (this.termgrid) {
            var store = this.termgrid.store;
            store.each(function(rec) {
                var lineAmt = rec.data.termamount;
                if (typeof lineAmt == 'number' && lineAmt != 0) {
                    arr.push(store.indexOf(rec));
                }
            }, this);
            return WtfGlobal.getJSONArray(this.termgrid, true, arr)
        }
    },
    
    getJSONArray: function(store, includeLast, idxArr) {
        var indices = "";
        if (idxArr)
            indices = ":" + idxArr.join(":") + ":";
        var arr = [];
        var fields = store.fields;
        var len = store.getCount();
        for (var i = 0; i < len; i++) {
            if (idxArr && indices.indexOf(":" + i + ":") < 0)
                continue;
            var rec = store.getAt(i);
            var recarr = [];
            for (var j = 0; j < fields.length; j++) {
                var value = rec.data[fields.get(j).name];
                switch (fields.get(j).type) {
                    case "auto":
                        if (value != undefined) {
                            value = (value + "").trim();
                        }
                        value = encodeURI(value);
                        value = "\"" + value + "\"";
                        break;
                    case "date":
                        value = "'" + WtfGlobal.convertToGenericDate(value) + "'";
                        break;
                }
                recarr.push(fields.get(j).name + ":" + value);
            }
            recarr.push("modified:" + rec.dirty);
            arr.push("{" + recarr.join(",") + "}");
        }
        return "[" + arr.join(',') + "]";
    },

    updateFieldConfigs: function() {
        if (this.isViewTemplate) {
            this.Number.hideLabel = true;
            this.Number.hide();
            this.sequenceFormatCombobox.hideLabel = true;
            this.sequenceFormatCombobox.hide();
        }
        if (this.viewGoodReceipt) {
            if (this.Number) {
                this.Number.hideLabel = false;
                this.Number.show();
            }
            if (this.sequenceFormatCombobox) {
                this.sequenceFormatCombobox.hideLabel = false;
                this.sequenceFormatCombobox.show();
            }
            if (this.billDate) {
                this.billDate.hideLabel = false;
                this.billDate.show();
            }
        }
        if (this.isTemplate) {
            this.Number.setValue("");
            this.Number.disable();
            this.sequenceFormatCombobox.disable();
            this.billDate.disable();
        }
    },
    
    hideTransactionFormFields: function(array) {
        if (array) {
            for (var i = 0; i < array.length; i++) {
                var fieldArray = array[i];
                if (Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id)) {
                    if (fieldArray.fieldId == "ShowOnlyOneTime" && ((this.isEdit != undefined ? this.isEdit : false) || (this.copyInv != undefined ? this.copyInv : false) || (this.isCopyFromTemplate != undefined ? this.isCopyFromTemplate : false) || (this.isTemplate != undefined ? this.isTemplate : false))) {
                        continue;
                    }
                    if (fieldArray.isHidden) {
                        Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).hidden = fieldArray.isHidden;
                    }
                    if (fieldArray.isReadOnly) {
                        Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).disabled = fieldArray.isReadOnly;
                    }
                    if (fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).fieldLabel != undefined) {
                        Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).fieldLabel = Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).fieldLabel + " *"
                    }
                    if (fieldArray.fieldLabelText != null && fieldArray.fieldLabelText != undefined && fieldArray.fieldLabelText != "") {
                        if (fieldArray.isManadatoryField && fieldArray.isFormField)
                            Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).fieldLabel = fieldArray.fieldLabelText + "*";
                        else
                            Wtf.getCmp(fieldArray.fieldId + this.heplmodeid + this.id).fieldLabel = fieldArray.fieldLabelText;
                    }
                }
            }
        }
    },
    
    applyTemplate: function(store, index) {
        var editable = this.Currency.getValue() != WtfGlobal.getCurrencyID() && this.Currency.getValue() != ""//&&!this.isOrder;
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if (this.externalcurrencyrate > 0) {
            exchangeRate = this.externalcurrencyrate;
        } else if (this.isEdit && this.record.data.externalcurrencyrate && !(this.custdatechange || this.currencychanged)) {
            var externalCurrencyRate = this.record.data.externalcurrencyrate - 0;//??[PS]
            if (externalCurrencyRate > 0) {
                exchangeRate = externalCurrencyRate;
            }
        }
        this.externalcurrencyrate = exchangeRate;
        var revExchangeRate = 1 / (exchangeRate - 0);
        if (this.exchangeratetype != undefined && this.exchangeratetype == "foreigntobase" && this.revexternalcurrencyrate != undefined && this.revexternalcurrencyrate != 0)
        {
            revExchangeRate = this.revexternalcurrencyrate
            this.revexternalcurrencyrate = 0;
        }
        revExchangeRate = (Math.round(revExchangeRate * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
        this.southCenterTplSummary.overwrite(this.southCenterTpl.body, {foreigncurrency: store.getAt(index).data['currencyname'], exchangerate: exchangeRate, basecurrency: WtfGlobal.getCurrencyName(), editable: editable, revexchangerate: revExchangeRate
        });
    },
    
    changeCurrencyStore: function(pronamearr) {
        this.pronamearr = pronamearr;
        var currency = this.Currency.getValue();
        if (this.val == "")
            this.val = this.billDate.getValue();
        this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.val)}});
    },
    
    getCurrencySymbol: function() {
        var index = null;
        var FIND = this.Currency.getValue();
        if (FIND == "" || FIND == undefined || FIND == null) {
            FIND = WtfGlobal.getCurrencyID();
        }
        index = this.currencyStore.findBy(function(rec) {
            var parentname = rec.data['currencyid'];
            if (parentname == FIND)
                return true;
            else
                return false
        })
        this.currencyid = this.Currency.getValue();
        return index;
    },
    
    applyCurrencySymbol: function() {
        var index = this.getCurrencySymbol();
        var rate = this.externalcurrencyrate;
        if (index >= 0) {
            rate = (rate == "" ? this.currencyStore.getAt(index).data.exchangerate : rate);
            this.symbol = this.currencyStore.getAt(index).data.symbol;
            this.Grid.setCurrencyid(this.currencyid, rate, this.symbol, index);
            this.applyTemplate(this.currencyStore, index);
        }
        return this.symbol;
    },
    
    initForClose: function() {
        this.cascade(function(comp) {
            if (comp.isXType('field')) {
                comp.on('change', function() {
                    this.isClosable = false;
                }, this);
            }
        }, this);
    },
    
    onDateChange: function(a, val, oldval) {
        if (this.Currency.getValue() == WtfGlobal.getCurrencyID()) { //when tranaction in base currency for all cases (edit,copy, create new)
            this.doOnDateChanged(val, oldval);
        } else if ((this.isEdit && !this.copyInv) && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_retainExchangeRate)) { //edit case: when user want to retain exchange rate        
            this.exchangeRateInRetainCase = true;
            this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
        } else if (this.isEdit || this.copyInv) { //1.Edit case when user do not want to retain exchange rate 2.copy case
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.invoice.exchangeRateMsg"), function(btn) {
                if (btn == "yes") {
                    this.doOnDateChanged(val, oldval);
                } else {
                    this.billDate.setValue(oldval);
                    return;
                }
            }, this);
        } else { //Normal Create New Case           
            this.doOnDateChanged(val, oldval);
        }
    },
    
    doOnDateChanged:function(val,oldval){
        this.val=val;
        this.oldval=oldval;
//        this.loadTax(val);
        this.externalcurrencyrate=0;
        this.custdatechange=true;
        this.datechange=1;
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)){
            this.currencyStore.on('load',function(store){this.onDateChangeVendorCurrencyExchangeRate();},this);
        }
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
        if(!(this.isCustBill||this.isExpenseInv)) {
            var affecteduser = this.Name.getValue();
        }else{
            this.changeCurrencyStore();
            this.updateSubtotal();
            this.applyCurrencySymbol();
            var subtotal=0.00;
            var tax=0.00;
            var taxAndSubtotal=this.Grid.calLineLevelTax();
            if(this.includeProTax.getValue()){
                subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
            }else{
                subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            }
            var discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
            var totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
            var aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol);
            var totalAmtInBase=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:discount,totalamount:totalamount,tax:tax,aftertaxamt:aftertaxamt,totalAmtInBase:totalAmtInBase,amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
            if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)){
                this.calProfitMargin();
                this.productProfitMarginTplSummary.overwrite(this.productProfitMarginTpl.body,{
                    totalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                    totalproductcost:WtfGlobal.addCurrencySymbolOnly(this.totalproductcost,WtfGlobal.getCurrencySymbol()),
                    totalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                    totalprodcutprofitmarginpercent:this.totalproductsellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.totalprodcutprofitmarginpercent+'%</div>',
                    totalservicesellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalservicesellingprice,WtfGlobal.getCurrencySymbol()),
                    totalservicecost:WtfGlobal.addCurrencySymbolOnly(this.totalservicecost,WtfGlobal.getCurrencySymbol()),
                    totalserviceprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalserviceprofitmargin,WtfGlobal.getCurrencySymbol()),
                    totalserviceprofitmarginpercent:this.totalservicesellingprice==0 ? '<div class="currency">NA</div>' : '<div class="currency">'+this.totalserviceprofitmarginpercent+'%</div>',
                    finalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.finalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                    finalproductcost:WtfGlobal.addCurrencySymbolOnly(this.finalproductcost,WtfGlobal.getCurrencySymbol()),
                    finalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.finalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                    finalproductprofitmarginpercent:this.finalproductsellingprice==0 ? '<div class="currency">NA</div>' : '<div class="currency">'+this.finalproductprofitmarginpercent+'%</div>'              
                });
            }
        }
    },
    
    loadTax: function(val) {
        this.Grid.taxStore.load({params: {transactiondate: WtfGlobal.convertToGenericDate(val)}});
        this.Tax.setValue("");
        this.Grid.getStore().each(function(rec) {
            if (rec.data.prtaxid != "" && rec.data.prtaxid != null && rec.data.prtaxid != undefined) {
                var taxRec = WtfGlobal.searchRecord(this.Grid.taxStore, rec.data.prtaxid, "prtaxid");
                if (taxRec != null && taxRec.data.applydate > this.billDate.getValue()) {
                    rec.set('prtaxid', '')
                    rec.set('taxamount', 0)
                }
            }
        }, this);
    },
    
    changeTemplateSymbol:function(){
        // check wheather exchange rate is set for currency on selected date while retaining exchange rate.
        if (this.exchangeRateInRetainCase) {
            if (this.Currency.getValue() != "" && WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid") == null) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthecurrencyrate") + " " + WtfGlobal.getLocaleText("acc.field.fortheselecteddate") + "<b>" + WtfGlobal.convertToGenericDate(this.val) + "</b>"], 0);
                this.exchangeRateInRetainCase = false;
                this.billDate.setValue("");
            }
            return;
        }
        if(this.loadCurrFlag && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_currencyid)){
            this.Currency.setValue(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_currencyid));
            this.loadCurrFlag = false;
        }
        /*if date of without inventory changes. price store will not be loaded in this case.[PS]*/
        if(this.isCustBill||this.isExpenseInv){
             if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.billDate.setValue("");
            }
            else
                this.updateFormCurrency();
        }

        /*if date of withinventory changes. After price store load. [PS]   */
        if(this.datechange==1){
            var str=""
            if(this.pronamearr!=undefined&&this.pronamearr.length>0){
                str+=this.pronamearr[0];
                for(var i=1;i<this.pronamearr.length;i++){
                    str+="</b>, <b>"+ this.pronamearr[i]
                }
            }
            var recResult=WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid");
            if(this.Currency.getValue() !="" && recResult == null){
                this.Currency.setValue("");      
                callCurrencyExchangeWindow();
                str= WtfGlobal.getLocaleText("acc.field.andpriceof")+" <b>"+str+"</b>";
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthecurrencyrate")+" "+str+WtfGlobal.getLocaleText("acc.field.fortheselecteddate")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.billDate.setValue("");
            } else {
                this.updateFormCurrency();
                if(this.pronamearr!=undefined&&this.pronamearr.length>0){
                    str=WtfGlobal.getLocaleText("acc.field.priceof")+" <b>"+str+"</b>";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthe")+" "+str+WtfGlobal.getLocaleText("acc.field.fortheselecteddate")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                }
                this.Grid.pronamearr=[];
                this.updateFormCurrency();
            }
            this.datechange=0;
            this.updateSubtotal();
            this.applyCurrencySymbol();
            
            var subtotal=0.00;
            var tax=0.00;
            var taxAndSubtotal=this.Grid.calLineLevelTax();
            if(this.includeProTax.getValue()){
                subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
            }else{
                subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
            }
            var discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
            var totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
            var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);       
            var aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol);
            var totalAmtInBase=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:discount,totalamount:totalamount,tax:tax,termtotal:calTermTotal,aftertaxamt:aftertaxamt,totalAmtInBase:totalAmtInBase,amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
            if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)){
                this.calProfitMargin();
                this.productProfitMarginTplSummary.overwrite(this.productProfitMarginTpl.body,{
                    totalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                    totalproductcost:WtfGlobal.addCurrencySymbolOnly(this.totalproductcost,WtfGlobal.getCurrencySymbol()),
                    totalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                    totalprodcutprofitmarginpercent:this.totalproductsellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.totalprodcutprofitmarginpercent+'%</div>',

                    totalservicesellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalservicesellingprice,WtfGlobal.getCurrencySymbol()),
                    totalservicecost:WtfGlobal.addCurrencySymbolOnly(this.totalservicecost,WtfGlobal.getCurrencySymbol()),
                    totalserviceprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalserviceprofitmargin,WtfGlobal.getCurrencySymbol()),
                    totalserviceprofitmarginpercent:this.totalservicesellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.totalserviceprofitmarginpercent+'%</div>',

                    finalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.finalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                    finalproductcost:WtfGlobal.addCurrencySymbolOnly(this.finalproductcost,WtfGlobal.getCurrencySymbol()),
                    finalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.finalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                    finalproductprofitmarginpercent:this.finalproductsellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.finalproductprofitmarginpercent+'%</div>'              
                });
            }
        }
        if(this.currencychanged){
            if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.Currency.setValue("");   
            } else {
                this.updateFormCurrency();
            }
            this.currencychanged = false;
        }
        /*when customer/vendor name changes [PS]*/
        if(this.custChange){
            if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.Name.setValue("");
            } else{
                this.Currency.setValue(this.currencyid);
                if(this.isEdit && this.number){
                    this.Number.setValue(this.number);           
                }
                this.updateFormCurrency();
            }
            this.custChange=false;
        }
        this.Grid.pronamearr=[];
    },
    
    updateFormCurrency:function(){
        this.applyCurrencySymbol();
        var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
        var subtotal=0.00;
        var tax=0.00;
        var taxAndSubtotal=this.Grid.calLineLevelTax();
        if(this.includeProTax.getValue()){
            subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
            tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
        }else{
            subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
        }
        this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,termtotal:calTermTotal,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)){
            this.calProfitMargin();
            this.productProfitMarginTplSummary.overwrite(this.productProfitMarginTpl.body,{
                totalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                totalproductcost:WtfGlobal.addCurrencySymbolOnly(this.totalproductcost,WtfGlobal.getCurrencySymbol()),
                totalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                totalprodcutprofitmarginpercent:this.totalproductsellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.totalprodcutprofitmarginpercent+'%</div>',
                totalservicesellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalservicesellingprice,WtfGlobal.getCurrencySymbol()),
                totalservicecost:WtfGlobal.addCurrencySymbolOnly(this.totalservicecost,WtfGlobal.getCurrencySymbol()),
                totalserviceprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalserviceprofitmargin,WtfGlobal.getCurrencySymbol()),
                totalserviceprofitmarginpercent:this.totalservicesellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.totalserviceprofitmarginpercent+'%</div>',
                finalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.finalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                finalproductcost:WtfGlobal.addCurrencySymbolOnly(this.finalproductcost,WtfGlobal.getCurrencySymbol()),
                finalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.finalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                finalproductprofitmarginpercent:this.finalproductsellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.finalproductprofitmarginpercent+'%</div>'              
            });
        }
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && this.importService){// for malasyian company
            if (this.importService && this.Currency.getValue() == gcurrencyid){
                this.importService.setValue(false);
                this.importService.disable();
            } else{
                this.importService.enable();
            }
        }
    },
    
    getPostTextEditor: function(posttext){
    	var _tw=new Wtf.EditorWindowQuotation({
    		val:this.postText
    	});
        _tw.on("okClicked", function(obj){
            this.postText = obj.getEditorVal().textVal;
            var styleExpression  =  new RegExp("<style.*?</style>");
            this.postText=this.postText.replace(styleExpression,"");
        }, this);
        _tw.show();
        return this.postText;
    },
    
    caltax:function(){
        var totalamount=this.calTotalAmount();
        var rec= this.Grid.taxStore.getAt(this.Grid.taxStore.find('prtaxid',this.Tax.getValue()));
        var totalterm = 0;
        if (rec != null) {
            for (var i = 0; i < this.termStore.getCount(); i++) {
                var recdata = this.termStore.getAt(i).data; //  var recdata = obj.record.data;
                var store = this.termStore;
                var subtotal = this.calProdSubtotalWithoutDiscount();
                var formula = recdata.formulaids.split(",");
                var termtotal = 0;
                var terms = rec.data.termid;
                terms = terms.split(",");
                terms = "[" + terms.join(',') + "]";
                if (terms.indexOf(recdata.id)!=-1) {
                    for (var cnt = 0; cnt < formula.length; cnt++) {
                        if (formula[cnt] == 'Basic') {
                            termtotal += (subtotal);
                        }
                        var record = store.queryBy(function (record) {
                            return (record.get('id') == formula[cnt]);
                        }, this).items[0];
                        if (record && (typeof record.data.termamount == 'number')) {
                            //                termtotal +=(record.data.termamount*(record.data.sign==1 ? 1 : -1 ));
                            termtotal += (record.data.termamount);
                        }
                    }
                    var this_termTotal = 0;
                    if (typeof (recdata.termpercentage) != "string" && parseInt(recdata.termpercentage) >= 0) {
                        var opmod = recdata.sign == 0 ? -1 : 1;
                        this_termTotal = ((Math.abs(termtotal) * recdata.termpercentage * 1) / 100) * opmod;
                    }
                    else if (typeof (recdata.termamount) != "string") {     // for term charges 
                        parseInt(recdata.termamount)
                        this_termTotal = recdata.termamount;
                    }
                    totalterm = totalterm + this_termTotal;
                }
            }
        }
        var taxamount=0;
        if(rec!=null){
            totalamount=getRoundedAmountValue(this.calTotalAmount());
            
            taxamount=((totalamount+totalterm)*rec.data["percent"])/100;
        }
        return getRoundedAmountValue(taxamount);
    },
    
    caltaxInBase:function(){
        var totalamount=this.calTotalAmountWithDiscInBase();
        var rec= this.Grid.taxStore.getAt(this.Grid.taxStore.find('prtaxid',this.Tax.getValue()));
        var totalterm = 0;
        if (rec != null) {
            for (var i = 0; i < this.termStore.getCount(); i++) {
                var recdata = this.termStore.getAt(i).data; //  var recdata = obj.record.data;
                var store = this.termStore;
                var subtotal = this.calProdSubtotalWithoutDiscountInBase();
                var formula = recdata.formulaids.split(",");
                var termtotal = 0;
                var terms = rec.data.termid;
                terms = terms.split(",");
                terms = "[" + terms.join(',') + "]";
                if (terms.indexOf(recdata.id)!=-1) {
                    for (var cnt = 0; cnt < formula.length; cnt++) {
                        if (formula[cnt] == 'Basic') {
                            termtotal += (subtotal);
                        }
                        var record = store.queryBy(function (record) {
                            return (record.get('id') == formula[cnt]);
                        }, this).items[0];
                        if (record && (typeof record.data.termamount == 'number')) {
                            //                termtotal +=(record.data.termamount*(record.data.sign==1 ? 1 : -1 ));
                            termtotal += this.calAmountInBase(getRoundedAmountValue(record.data.termamount));
                        }
                    }
                    var this_termTotal = 0;
                    if (typeof (recdata.termpercentage) != "string" && parseInt(recdata.termpercentage) >= 0) {
                        var opmod = recdata.sign == 0 ? -1 : 1;
                        this_termTotal = ((Math.abs(termtotal) * recdata.termpercentage * 1) / 100) * opmod;
                    }
                    else if (typeof (recdata.termamount) != "string") {     // for term charges 
                        parseInt(recdata.termamount)
                        this_termTotal = recdata.termamount;
                    }
                    totalterm = totalterm + this.calAmountInBase(getRoundedAmountValue(this_termTotal));
                }
            }
        }
        var taxamount=0;
        if(rec!=null){
            totalamount=this.calTotalAmountWithDiscInBase();
            
            taxamount=((totalamount+totalterm)*rec.data["percent"])/100;
        }
        return getRoundedAmountValue(taxamount);
    },
    
    showGridTax:function(c,rec,val){
        var hide=(val==null||undefined?!rec.data['value']:val) ;
        var id=this.Grid.getId()
        var rowtaxindex=this.Grid.getColumnModel().getIndexById(id+"prtaxid");
        var rowtaxamountindex=this.Grid.getColumnModel().getIndexById(id+"taxamount");
            this.Grid.getColumnModel().setHidden( rowtaxindex,hide) ;
            this.Grid.getColumnModel().setHidden( rowtaxamountindex,hide) ;
        var rowRateIncludingGstAmountIndex=this.Grid.getColumnModel().getIndexById(id+"rateIncludingGst");
        var rowprDiscountIndex=this.Grid.getColumnModel().getIndexById(id+"prdiscount");
        var rowDiscountIsPercentIndex=this.Grid.getColumnModel().getIndexById(id+"discountispercent");
        var rowRateAmountIndex=this.Grid.getColumnModel().getIndexById(id+"rate");
        if(rowprDiscountIndex!=-1&&rowDiscountIsPercentIndex!=-1&&rowRateIncludingGstAmountIndex!=-1){
            if(this.includingGST.getValue()){
                this.Grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,!this.includingGST.getValue());
                this.Grid.getColumnModel().getColumnById(id+"rate").editable=false;
                this.Grid.getColumnModel().setHidden(rowprDiscountIndex,!hide);
                this.Grid.getColumnModel().setHidden(rowDiscountIsPercentIndex,!hide);
            }else if(!this.Grid.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden){
                this.Grid.getColumnModel().getColumnById(id+"rate").editable=true;
                this.Grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,!this.includingGST.getValue());
                this.Grid.getColumnModel().setHidden(rowprDiscountIndex,hide);
                this.Grid.getColumnModel().setHidden(rowDiscountIsPercentIndex,hide);
            }
        }
        
        this.Grid.getStore().each(function(rec){
            if(this.includeProTax && this.includeProTax.getValue() == true
                && (rec.data.prtaxid == "" || rec.data.prtaxid == undefined)) {//In Edit, values are resetting after selection Product level Tax value as No
                var taxid = "";
                var taxamount = 0;
                if(!(rec.data.productid == "" || rec.data.productid == undefined)){// for excluding last empty row
                    if(taxid == ""){// if tax is mapped to customer or vendor then it will come default populated
                        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
                        var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                        if(actualTaxId== undefined || actualTaxId == "" ||  actualTaxId == null){// if customer/vendor is not mapped with tax then check that is their mapping account is mapped with tax or not, if it is mapped take account tax
                            actualTaxId=currentTaxItem!=null?currentTaxItem.get('mappedAccountTaxId'):"";
                        }
                        if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null){
                            taxid = actualTaxId;
                            rec.set('prtaxid',taxid);
                            taxamount = this.Grid.setTaxAmountAfterSelection(rec);
                        }
                    }
                }
                if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null){
                    taxid = actualTaxId;
                    rec.set('prtaxid',taxid);
                    taxamount = this.Grid.setTaxAmountAfterSelection(rec);
                }
                rec.set('prtaxid',taxid);
                rec.set('taxamount',taxamount);
            } else if(this.includeProTax && this.includeProTax.getValue() != true){
                rec.set('prtaxid','');
                rec.set('taxamount',0);
            }
            if(this.includingGST&&this.includingGST.getValue()){
                rec.set('discountispercent',1);
                rec.set('prdiscount',0);
                rec.set('rateIncludingGst',rec.get('rate'));
                rec.set('rateIncludingGst',rec.get('taxamount'));
                var taxamount= 0;
                var unitAmount= 0;
                var unitTax= 0;
                var unitVal= 0;
                var amount=rec.get('rate')!=null?getRoundedAmountValue(rec.get('rate')):0;
                var quantity=rec.get('quantity')!=null?getRoundofValue(rec.get('quantity')):0;
                var tax=rec.get('taxamount')!=null?getRoundofValue(rec.get('taxamount')):0;
                if(quantity!=0){
                    unitAmount=getRoundedAmountValue(amount);
                    unitTax=getRoundedAmountValue(tax/quantity);
                }
                if(unitAmount+unitTax!=0){
                    rec.set('rateIncludingGst',unitAmount+unitTax);
                }else{
                    rec.set('rateIncludingGst',rec.get('rate'));
                }
            }else if(rowRateIncludingGstAmountIndex!=-1&&this.Grid.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden && rec.data.prdiscount==0){//if column unit price column is hidden. Works for all case except when include gst is checked.
                rec.set('discountispercent',1);
                rec.set('prdiscount',0);
                rec.set('rateIncludingGst',0);
            }
        },this);
        this.updateSubtotal();
    },
    
    includeProTaxHandler : function(c,rec,val){
        if(this.includeProTax.getValue() == true){
            this.isTaxable.setValue(false);
            this.isTaxable.disable();
            this.Tax.setValue("");
            this.Tax.disable();
        }else{
            this.isTaxable.reset();
            this.isTaxable.enable();
        }
        this.showGridTax(c,rec,val);
    },
    
    enabletax:function(c,rec){
        if(rec.data['value']==true)
            this.Tax.enable();   
        else{
            this.Tax.disable();
            this.Tax.setValue("");
        }
        this.updateSubtotal();
    },
    
    setTerm:function(c,rec,ind){
        this.Term.setValue(rec.data['termid']);
        this.updateDueDate();
    },
    
    callGSTCurrencyRateandUpdateSubtotal:function(a,val){
        if(WtfGlobal.singaporecountry()&&WtfGlobal.getCurrencyID()!=Wtf.Currency.SGD&&this.isInvoice && this.Grid.forCurrency!=Wtf.Currency.SGD){
            callGstCurrencyRateWin(this.id,"SGD ",undefined,this.gstCurrencyRate);
        }
        this.updateSubtotal(a,val);
    },
    
    updateSubtotal:function(a,val){
        if(this.calDiscount())return;
        this.isClosable=false; // Set Closable flag after updating grid data
        this.applyCurrencySymbol();
        var subtotal=0.00;
        var tax=0.00;
        var taxAndSubtotal=this.Grid.calLineLevelTax();
        if(this.includeProTax.getValue()){
            subtotal=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0]-taxAndSubtotal[1],this.symbol)
            tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
        }else{
            subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
            tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
        }
        this.updateSubtotalOnTermChange(false);
        var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
        this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:tax,termtotal:calTermTotal,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.findTermsTotal()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)){
            this.calProfitMargin();
            this.productProfitMarginTplSummary.overwrite(this.productProfitMarginTpl.body,{
                totalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                totalproductcost:WtfGlobal.addCurrencySymbolOnly(this.totalproductcost,WtfGlobal.getCurrencySymbol()),
                totalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                totalprodcutprofitmarginpercent:this.totalproductsellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.totalprodcutprofitmarginpercent+'%</div>',

                totalservicesellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalservicesellingprice,WtfGlobal.getCurrencySymbol()),
                totalservicecost:WtfGlobal.addCurrencySymbolOnly(this.totalservicecost,WtfGlobal.getCurrencySymbol()),
                totalserviceprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalserviceprofitmargin,WtfGlobal.getCurrencySymbol()),
                totalserviceprofitmarginpercent:this.totalservicesellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.totalserviceprofitmarginpercent+'%</div>',

                finalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.finalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                finalproductcost:WtfGlobal.addCurrencySymbolOnly(this.finalproductcost,WtfGlobal.getCurrencySymbol()),
                finalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.finalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                finalproductprofitmarginpercent:this.finalproductsellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.finalproductprofitmarginpercent+'%</div>'              
            });
        }
        if(this.isFromProjectStatusRep){
            if(this.SOLinkedArr.length>0){
                this.PO.setValue(this.SOLinkedArr);
            }
            this.isMultiSelectFlag = true;    
            this.loadDataForProjectStatusReport();
        }
    },
    
    getDiscountInBase:function(){
        var disc = 0;
        var per = 1;
        if(this.Discount)
        disc=this.Discount.getValue();
        if(this.perDiscount)
        per=this.perDiscount.getValue();
        var subtotalAfterTerm = this.Grid.calSubtotalInBase() + this.findTermsTotalInBase();
        if(isNaN(parseFloat(disc))){
            return 0;
        }else{
            disc=this.calAmountInBase(disc);
            subtotalAfterTerm=getRoundedAmountValue(subtotalAfterTerm);
            if(per){
                return (disc*subtotalAfterTerm)/100
            }else{
                return disc;
            }
        }
    },
    
    calTotalAmount:function(){
        var subtotal=this.Grid.calSubtotal();
        var discount=this.getDiscount();
        return subtotal-discount;
    },
    
    calTotalAmountWithDiscInBase:function(){
        var subtotal=this.Grid.calSubtotalInBase();
        var discount=this.getDiscountInBase();
        return subtotal-discount;
    },
    
    getExchangeRate:function(){
        var index=this.getCurrencySymbol();
        var rate=this.externalcurrencyrate;
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
    
    calAmountInBase:function(val){
        var returnVal = getRoundedAmountValue(val*this.getExchangeRate());
        return returnVal; 
    },
    
    calTotalAmountInBase:function(){
        var subtotal=this.Grid.calSubtotalInBase(); 
        var discount=this.getDiscountInBase();   
        var taxVal = this.caltaxInBase();
        var returnValInOriginalCurr = subtotal-discount + this.findTermsTotalInBase()+taxVal;
        returnValInOriginalCurr = getRoundedAmountValue(returnValInOriginalCurr);
        return returnValInOriginalCurr; 
    },
    
    checklastproduct:function(incash,count){
        if(this.Grid.getStore().getAt(count-1).data['pid']!="" && this.Grid.getStore().getAt(count-1).data['productid']==""){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.common.productWithSpecifiedId")+" "+this.Grid.getStore().getAt(count-1).data['pid']+" "+WtfGlobal.getLocaleText("acc.common.productDoesNotExistsOrInDormantState")+". "+WtfGlobal.getLocaleText("acc.accPref.productnotFoundonSave")+'</center>' ,function(btn){
                if(btn=="yes") {
                    this.createRecords(incash);
                }else{
                    this.enableSaveButtons();
                    return;
                } 
            },this);                
        }else{
            this.createRecords(incash);
        } 
    },
    
    getDiscount:function(){
        var disc = 0;
        var per = 1;
        if(this.Discount)
            disc=this.Discount.getValue();
        if(this.perDiscount)
            per=this.perDiscount.getValue();
        var subtotalAfterTerm = this.Grid.calSubtotal() + this.findTermsTotal();
        if(isNaN(parseFloat(disc))){
            return 0;
        }else{
            disc=getRoundedAmountValue(disc);
            subtotalAfterTerm=getRoundedAmountValue(subtotalAfterTerm);
            if(per){
                return (disc*subtotalAfterTerm)/100
            }else{
                return disc;
            }
        }
    },
    
    //TODO: Thismethod need to be removed as Ravesh has already created a common for this in WtfGlobal..js
    getAddressDetails: function(rec) {
        if (this.currentAddressDetailrec != "" && this.currentAddressDetailrec != undefined) {
            rec.billingAddress = this.currentAddressDetailrec.billingAddress;
            rec.billingCity = this.currentAddressDetailrec.billingCity;
            rec.billingState = this.currentAddressDetailrec.billingState;
            rec.billingCountry = this.currentAddressDetailrec.billingCountry;
            rec.billingPostal = this.currentAddressDetailrec.billingPostal;
            rec.billingPhone = this.currentAddressDetailrec.billingPhone;
            rec.billingMobile = this.currentAddressDetailrec.billingMobile;
            rec.billingFax = this.currentAddressDetailrec.billingFax;
            rec.billingEmail = this.currentAddressDetailrec.billingEmail;
            rec.billingRecipientName = this.currentAddressDetailrec.billingRecipientName;
            rec.billingContactPerson = this.currentAddressDetailrec.billingContactPerson;
            rec.billingContactPersonNumber = this.currentAddressDetailrec.billingContactPersonNumber;
            rec.billingContactPersonDesignation = this.currentAddressDetailrec.billingContactPersonDesignation;
            rec.shippingAddress = this.currentAddressDetailrec.shippingAddress;
            rec.shippingCity = this.currentAddressDetailrec.shippingCity;
            rec.shippingState = this.currentAddressDetailrec.shippingState;
            rec.shippingCountry = this.currentAddressDetailrec.shippingCountry;
            rec.shippingPostal = this.currentAddressDetailrec.shippingPostal;
            rec.shippingPhone = this.currentAddressDetailrec.shippingPhone;
            rec.shippingMobile = this.currentAddressDetailrec.shippingMobile;
            rec.shippingFax = this.currentAddressDetailrec.shippingFax;
            rec.shippingEmail = this.currentAddressDetailrec.shippingEmail;
            rec.shippingRecipientName = this.currentAddressDetailrec.shippingRecipientName;
            rec.shippingContactPerson = this.currentAddressDetailrec.shippingContactPerson;
            rec.shippingContactPersonNumber = this.currentAddressDetailrec.shippingContactPersonNumber;
            rec.shippingContactPersonDesignation = this.currentAddressDetailrec.shippingContactPersonDesignation;
            rec.shippingRoute = this.currentAddressDetailrec.shippingRoute;
            rec.billingAddressType = this.currentAddressDetailrec.billingAddrsCombo;
            rec.shippingAddressType = this.currentAddressDetailrec.shippingAddrsCombo;
            rec.isEdit = this.isEdit;
            rec.copyInv = this.copyInv;
        }else if((this.record!=null &&(this.isEdit || this.copyInv) && !(this.GENERATE_PO||this.GENERATE_SO)) || (this.linkRecord && this.singleLink)) {
            var addressrec = "";
            if (this.linkRecord && this.singleLink) {
                addressrec = this.linkRecord.data;
            } else {
                addressrec = this.record.data;
            }
            
            rec.billingAddress = addressrec.billingAddress;
            rec.billingCity = addressrec.billingCity;
            rec.billingState = addressrec.billingState;
            rec.billingCountry = addressrec.billingCountry;
            rec.billingPostal = addressrec.billingPostal;
            rec.billingPhone = addressrec.billingPhone;
            rec.billingMobile = addressrec.billingMobile;
            rec.billingFax = addressrec.billingFax;
            rec.billingEmail = addressrec.billingEmail;
            rec.billingRecipientName = addressrec.billingRecipientName;
            rec.billingContactPerson = addressrec.billingContactPerson;
            rec.billingContactPersonNumber = addressrec.billingContactPersonNumber;
            rec.billingContactPersonDesignation = addressrec.billingContactPersonDesignation;
            rec.shippingAddress = addressrec.shippingAddress;
            rec.shippingCity = addressrec.shippingCity;
            rec.shippingState = addressrec.shippingState;
            rec.shippingCountry = addressrec.shippingCountry;
            rec.shippingPostal = addressrec.shippingPostal;
            rec.shippingPhone = addressrec.shippingPhone;
            rec.shippingMobile = addressrec.shippingMobile;
            rec.shippingFax = addressrec.shippingFax;
            rec.shippingEmail = addressrec.shippingEmail;
            rec.shippingRecipientName = addressrec.shippingRecipientName;
            rec.shippingContactPerson = addressrec.shippingContactPerson;
            rec.shippingContactPersonNumber = addressrec.shippingContactPersonNumber;
            rec.shippingContactPersonDesignation = addressrec.shippingContactPersonDesignation;
            rec.shippingRoute = addressrec.shippingRoute;
            rec.billingAddressType = addressrec.billingAddressType;
            rec.shippingAddressType = addressrec.shippingAddressType;
            rec.isEdit = this.isEdit;
            rec.copyInv = this.copyInv;
        } else {//Used for saving default address of customer/vendor on java side
            rec.defaultAdress = true;
        }
    },
    
    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_fyfrom))
            monthDateStr=WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_fyfrom).format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    getFinancialYRStartDatesMinOne:function(start){
        var d=Wtf.serverDate;
        var monthDateStr=d.format('M d');
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_bbfrom))
            monthDateStr=WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_bbfrom).format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd.add(Date.YEAR, 0).add(Date.DAY, -1);
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
    setCurrencyInfo:function(response){
        if(response.success){                        
            this.custdatechange=true; 
            if (!(this.isEdit || this.copyInv ) && (WtfGlobal.getCurrencyID() == this.Currency.getValue())) { // WtfGlobal.getCurrencyID() == this.Currency.getValue() this condition added due to handle issue ERP-9271 
                this.externalcurrencyrate=0;
                this.Currency.setValue(response.currencyid);
                this.currencyid=response.currencyid;
                this.symbol = response.currencysymbol;   
            }            
            var taxid = response.taxid
            var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
            var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";

            if(actualTaxId== undefined || actualTaxId == "" ||  actualTaxId == null){// if customer/vendor is not mapped with tax then check that is their mapping account is mapped with tax or not, if it is mapped take account tax
                actualTaxId=currentTaxItem!=null?currentTaxItem.get('mappedAccountTaxId'):"";
            }
            if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            } else {
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
            if(this.isEdit){
                this.setProductAndTransactionTaxValues();   
            }                  
            this.custChange=true;
            if(!(this.isEdit || this.copyInv)){ 
                this.changeCurrencyStore();
            }
            this.amountdue=0;
            this.amountdue=response.amountdue;

            if(this.GENERATE_PO || this.GENERATE_SO){
                this.updateFormCurrency();
            }    
            else{
                this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.currencyRenderer(0),discount:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),termtotal:WtfGlobal.currencyRenderer(0),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,this.symbol)})
                if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && (this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)){
                    this.calProfitMargin();
                    this.productProfitMarginTplSummary.overwrite(this.productProfitMarginTpl.body,{
                        totalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                        totalproductcost:WtfGlobal.addCurrencySymbolOnly(this.totalproductcost,WtfGlobal.getCurrencySymbol()),
                        totalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                        totalprodcutprofitmarginpercent:this.totalproductsellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.totalprodcutprofitmarginpercent+'%</div>',

                        totalservicesellingprice:WtfGlobal.addCurrencySymbolOnly(this.totalservicesellingprice,WtfGlobal.getCurrencySymbol()),
                        totalservicecost:WtfGlobal.addCurrencySymbolOnly(this.totalservicecost,WtfGlobal.getCurrencySymbol()),
                        totalserviceprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.totalserviceprofitmargin,WtfGlobal.getCurrencySymbol()),
                        totalserviceprofitmarginpercent:this.totalservicesellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.totalserviceprofitmarginpercent+'%</div>',

                        finalproductsellingprice:WtfGlobal.addCurrencySymbolOnly(this.finalproductsellingprice,WtfGlobal.getCurrencySymbol()),
                        finalproductcost:WtfGlobal.addCurrencySymbolOnly(this.finalproductcost,WtfGlobal.getCurrencySymbol()),
                        finalproductprofitmargin:WtfGlobal.addCurrencySymbolOnly(this.finalproductprofitmargin,WtfGlobal.getCurrencySymbol()),
                        finalproductprofitmarginpercent:this.finalproductsellingprice==0 ? '<div class="currency">NA</div>' :'<div class="currency">'+this.finalproductprofitmarginpercent+'%</div>'              
                    });
                }  
            }
        }
    },
    
    loadStoreOnNameSelect:function(){
        this.PO.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);
        if(this.isTemplate){
            this.createTransactionAlsoOldVal = this.createTransactionAlso;
            this.oldTempNameVal = this.moduleTemplateName.getValue();
        } 
        if(this.isExpenseInv){
            this.autoGenerateDO.setValue(false);
            this.autoGenerateDO.disable();
            this.includingGST.setValue(false);
            this.includingGST.disable();
        }else{
            this.autoGenerateDO.reset();
            if(!this.isTemplate) {
                this.autoGenerateDO.enable();
            }
            this.includingGST.reset();
            this.includingGST.enable();
        }
        if(this.isTemplate){
            this.moduleTemplateName.setValue(this.oldTempNameVal);
            if(this.createTransactionAlsoOldVal){
                this.createAsTransactionChk.setValue(true);
                if(!this.isViewTemplate){                      // View mode- all fields should be disabled unconditionaly
                    this.Number.enable();
                }    
                this.sequenceFormatCombobox.enable();
            }
        }
        if(this.fromPO){         
            this.fromPO.enable();
        }
        if(this.fromLinkCombo){
            this.fromLinkCombo.setDisabled(true);
            this.fromLinkCombo.clearValue();
        }
        this.fromPO.setValue(false); 
        
        if(this.partialInvoiceCmb){
            this.partialInvoiceCmb.disable();
            var id=this.Grid.getId();
            var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");
            if(rowindex != -1){
                this.Grid.getColumnModel().setHidden( rowindex,true);
            }            
        }
        this.showGridTax(null,null,true);
        this.Grid.symbol=undefined; 
        this.Grid.updateRow(null);
        this.resetForm = true;
        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
        if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
            this.isTaxable.setValue(true);
            this.Tax.enable();
            this.Tax.setValue(actualTaxId);
        }else{
            this.Tax.setValue("");
            this.Tax.setDisabled(true);				
            this.isTaxable.setValue(false);
        }
    },
    
    addTax:function(){
        var p= callTax("taxwin");
        Wtf.getCmp("taxwin").on('update', function(){this.Grid.taxStore.reload();}, this);
    },
    
    setProductAndTransactionTaxValues:function(){
        if(this.record.data.includeprotax){
            this.includeProTax.setValue(true);
            this.showGridTax(null,null,false);  
            this.isTaxable.setValue(false);
            this.Tax.setValue('');
            this.Tax.disable();
        }else{   
            this.includeProTax.setValue(false);
            this.showGridTax(null,null,true);
        }
    },
    
    calProdSubtotalWithoutDiscount:function(){
        var subtotal=0;
        var count=this.Grid.store.getCount();
        var store = this.Grid.store;
        for(var i=0;i<count;i++){
          if(this.isExpenseInv)
              {
                  var total=store.getAt(i).data.amount;
              }else{
                  var total=store.getAt(i).data.amountwithouttax;
              }
            subtotal+=total;
        }
        /*
         *  If preference set to include GST amount in base then in subtotal add GST tax amount
         */
        if(this.termsincludegst) {
            subtotal +=this.caltax();
        }
        return subtotal;
    },
    
    calProdSubtotalWithoutDiscountInBase:function(){
        var subtotal=0;
        var count=this.Grid.store.getCount();
        var store = this.Grid.store;
        for(var i=0;i<count;i++){
          if(this.isExpenseInv)
              {
                  var total=store.getAt(i).data.amount;
              }else{
                  var total=store.getAt(i).data.amountwithouttax;
              }
            subtotal+=this.calAmountInBase(getRoundedAmountValue(total));
        }
        
        /*
         *  If preference set to include GST amount in base then in subtotal add GST tax amount
         */
        if(this.termsincludegst) {
            subtotal =getRoundedAmountValue(subtotal) + this.caltaxInBase();
        }
        return subtotal;
    },
    
    checkBeforeProceed: function(vals){
          var doSubmit = true;       
            if (vals.indexOf(this.Number.emptyText)!=-1) {
                doSubmit = false;        
        }
        return doSubmit;
    },
    
    showGridBatch:function(newval){//written to hide & show Serial window Column-Neeraj D
        var hide=(newval)?0:1 ;
        var id=this.Grid.getId();
        var rowtaxindex=this.Grid.getColumnModel().getIndexById(id+'serialwindow');
        this.Grid.getColumnModel().setHidden( rowtaxindex,hide) ;
        this.Grid.getColumnModel().setHidden( rowtaxindex,hide) ;
    },
    
    showPartialDiscount : function(c,rec,val) {
        var hide=val;
        var id=this.Grid.getId();
        var rowindex=this.Grid.getColumnModel().getIndexById(id+"partdisc");

        this.Grid.getColumnModel().setHidden( rowindex,hide) ;
        this.Grid.getStore().each(function(rec){
            rec.set('partamount',0)
        },this);
        this.updateSubtotal();
    },  
    
    onCurrencySelect: function() {
        this.externalcurrencyrate = 0;
        this.currencychanged = true;
        if (this.includingGST.getValue() != false) {
            this.includingGST.setValue(false);
        }
        var customer = "", currency = "";
        if (this.Name.getValue() != undefined && this.Name.getValue() != "") {
            customer = this.Name.getValue();
        }
        if (this.Currency.getValue() != undefined && this.Currency.getValue() != "") {
            currency = this.Currency.getValue();
        }
        if (!this.GENERATE_PO && !this.GENERATE_SO) {
            this.onCurrencyChangeOnly();
            this.Name.setValue(customer);
            this.Currency.setValue(currency);
        }
        this.updateFormCurrency();
        if (this.Grid) {
            this.Grid.forCurrency = this.Currency.getValue();
        }
    },
    
    onNameBeforeSelect: function(combo) {
        this.nameBeforeSelect = combo.getValue();
    },
    
    onIncludingGSTfocus: function(o, newval, oldval) {
        var includeGstCount = 0;
        var excludeGstCount = 0;
        var selectedids = this.PO.getValue();
        var selectedValuesArr = selectedids.split(',');
        for (var cntGst = 0; cntGst < selectedValuesArr.length; cntGst++) {
            var rec = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[cntGst]));
            if (rec != undefined) {
                if (rec.data["gstIncluded"]) {
                    includeGstCount++;
                } else if (!rec.data["gstIncluded"]) {
                    excludeGstCount++;
                }
            }
        }
        if (this.fromPO.getValue() != undefined && this.fromPO.getValue() == true && this.PO.getValue() != "") {
            var message = ""
            if (selectedValuesArr.length == includeGstCount && this.includingGST.getValue()) {
                message = WtfGlobal.getLocaleText("acc.inclidingGST");
            } else if (selectedValuesArr.length == excludeGstCount && !this.includingGST.getValue()) {
                message = WtfGlobal.getLocaleText("acc.excludingGST");
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), message, function(btn) {
                if (btn != "yes") {
                    return;
                }
                if (!((selectedValuesArr.length == includeGstCount && !this.includingGST.getValue()) || (selectedValuesArr.length == excludeGstCount && this.includingGST.getValue()))) {
                    this.PO.clearValue();
                }
                var value = this.includingGST.getValue()
                this.includingGST.setValue(!value);
            }, this);
        }
    },
    
    onIncludingGSTCheck: function(o, newval, oldval) {
        if (this.includingGST.getValue()) {
            this.includeProTax.setValue(true);
            this.includeProTax.disable();
        } else {
            this.isViewTemplate == true ? this.includeProTax.disable() : this.includeProTax.enable();
        }
        var rec = WtfGlobal.searchRecord(this.includeProTax.store, true, 'value');
        if (rec != null) {
            this.includeProTaxHandler(this.includeProTax, rec, !this.includeProTax.getValue());
        }
    },
    
    onPOClearVal: function() {
        if (this.PO.getValue() == "" && !this.isEdit && !this.handleEmptyText) {
            this.Grid.getStore().removeAll();
            this.Grid.addBlankRow();
            var fieldArr = this.POStore.fields.items;
            for (var fieldCnt = 0; fieldCnt < fieldArr.length; fieldCnt++) {
                var fieldN = fieldArr[fieldCnt];
                if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id)) {
                    Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue('');
                }
            }
        }
        this.handleEmptyText = false;
    },
    
    onShowOnlyOneTimeCheck: function(obj, isChecked) {
        this.Name.reset();
        this.Name.store.load();
    },
    
    getInvalidFields: function() {
        var invalidFields = []
        this.NorthForm.getForm().items.filterBy(function(field) {
            if (field.validate())
                return;
            invalidFields.push(field);
        });
        if (this.SouthForm) {
            this.SouthForm.getForm().items.filterBy(function(field) {
                if (field.validate())
                    return;
                invalidFields.push(field);
            });
        }
        var invalidCustomFieldsArray = this.tagsFieldset.getInvalidCustomFields();// Function for getting invalid custom fields and dimensions 
        for (var i = 0; i < invalidCustomFieldsArray.length; i++) {
            invalidFields.push(invalidCustomFieldsArray[i]);
        }
        return invalidFields;
    }
});
