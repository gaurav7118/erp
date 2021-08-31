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
function openPersonWin(isCustomer){
    if(isCustomer=='true')
        callBusinessContactWindow(false,null,null,true);
    else
        callBusinessContactWindow(false,null,null,false);
}
Wtf.account.BusinessContactPanel=function(config){
    this.withinventory=config.withinventory||false;
    this.summary = new Wtf.ux.grid.GridSummary();
    this.personlinkid=config.personlinkid;
    this.perAccID=null;
    this.openperson=config.openperson;
    this.isEdit=false;
    this.ispropagatetochildcompanyflag=false;//this flag is used to delete propagated record in child companies. 
    this.recArr=[];
    this.isAdd=false;
    this.nondeleted=false;
    this.deleted=false;
    this.custAmountDueMoreThanLimit=config.custAmountDueMoreThanLimit!=undefined?config.custAmountDueMoreThanLimit:false;
    this.id=config.id;
    this.typeEditor = new Wtf.form.ComboBox({
        store: Wtf.delTypeStore,
        name:'typeid',
        displayField:'name',
        valueField:'typeid',
        mode: 'local',
        value:0,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    this.isCustomer=config.isCustomer;
    this.accid="";
    this.getDetailPanel();
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.uPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.moduleId=config.moduleId;
    this.GridRec = Wtf.data.Record.create ([
        {name:'accid'},
        {name:'openbalance'},
        {name:'id'},
        {name:'title'},
        {name:'accname'},
        {name:'aliasname'},
        {name:'acccode'},
        {name:'accnamecode'},
        {name:'address'},
        {name:'baddress2'},
        {name:'baddress3'},
        {name:'personname',mapping:'accname'},
        {name:'personemail',mapping:'email'},
        {name:'personid',mapping:'id'},
        {name:'taxeligible',type:'boolean'},
        {name:'overseas',type:'boolean'},
        {name:'mapcustomervendor',type:'boolean'},
        {name:'taxidnumber'},
        {name:'taxcode'},
        {name:'company'},        
        {name:'uenno'},        
        {name:'vattinno'},        
        {name:'csttinno'},        
        {name:'gstin'},        
        {name:'GSTINRegistrationTypeId'},        
        {name:'CustomerVendorTypeId'},
        {name:'GSTINRegistrationTypeName'},        
        {name:'CustomerVendorTypeName'},
        {name:'panno'},        
        {name:'vendorbranch'},        
        {name:'npwp'},        
        {name:'servicetaxno'},        
        {name:'tanno'},        
        {name:'eccno'},        
        {name:'pdm'},
        {name:'isPermOrOnetime'},
        {name:'pdmname'},
        {name:'parentid'},
        {name:'parentname'},
        {name:'bankaccountno'},
        {name:'termid'},
        {name:'termname'},
        {name: 'isavailableonlytosalespersons'},
        {name: 'isvendoravailabletoagent'},
        {name: 'agentsmappedwithvendor'},
        {name: 'salesPersonAgent'},
        {name: 'mappedSalesPersonId'},
        {name: 'mappedReceivedFromId'},
        {name: 'mappedPaidToId'},
        {name: 'mappedMultiSalesPersonId'},//For multi-select sales person combobox
        {name: 'defaultagentmappingid'},
        {name:'other'},
        {name: 'leaf'},
//        {name: 'currencysymbol'}, //ERP-2865 SagarM - commented it as all amounts in column are showing in base currency. No need to show symbol of customer/vendor currency 
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'istaxeligible'},
        {name: 'deleted'},
        {name: 'creationDate' ,type:'date'},
      //  {name: 'creationDate' },//,type:'date'//This date is handled on JAVA side & sent as String only
        {name: 'categoryid'},
        {name:'intercompany',type:'boolean'},
        {name:'isTDSapplicableoncust',type:'boolean'},
        {name:'isTDSapplicableonvendor',type:'boolean'},
        {name:'isVendorUsedInTDSTransactions',type:'boolean'},
        {name: 'intercompanytypeid'},
        {name: 'taxno'},
        {name: 'taxId'},
        {name: 'level'},
        {name: 'contactperson'},
        {name: 'amountdue'},
        {name: 'mappingaccid'},
        {name: 'mappingvenaccid'},
        {name: 'mappingcusaccid'},
        {name: 'country'},
        {name: 'limit'},
        {name: 'sequenceformat'},   
        {name: 'addressDetails'},
        {name: 'billingAddress'},
        {name: 'billingContactPerson'},
        {name: 'billingContactPersonNumber'},
        {name: 'billingContactPersonDesignation'},
        {name: 'billingState'},        
        {name: 'billingMobileNumber'},
        {name: 'billingEmailID'},
        {name: 'shippingAddress'},
        {name: 'shippingState'},
        {name: 'productname'},
        {name: 'prodname'},
        {name: 'productid'},
        {name: 'isIBGActivated'},
        {name: 'isactivate'},
        {name: 'employmentStatus'},
        {name: 'employerName'},
        {name: 'companyAddress'},
        {name: 'occupationAndYears'},
        {name: 'monthlyIncome'},
        {name: 'noofActiveCreditLoans'},
        {name: 'exceededamount'},
        {name: 'companyRegistrationNumber'},
        {name: 'gstRegistrationNumber'},
        {name: 'rmcdApprovalNumber'},
        {name: 'paymentmethod'},
        {name: 'paymentCriteria'},
        {name: 'pricingBandID'},
        {name: 'mappingcusaccid'},
        {name: 'mappingvenaccid'}, 
        {name: 'selfBilledFromDate',type:'date'},
        {name: 'gstVerifiedDate',type:'date'},
        {name: 'seztodate',type:'date'},
        {name: 'sezfromdate',type:'date'},
        {name: 'selfBilledToDate',type:'date'},
        {name: 'synchedfromotherapp'} ,                    //set to non-editable customer code i.e. synced from CRM
        {name: "ibgReceivingDetails"},    
        {name: "isPropagatedPersonalDetails"} ,
        {name :'DBSbank'},
        {name :'CIMBbank'},
        {name :'itno'},
        {name :'panStatusId'},
        {name :'natureOfPayment'},
        {name :'tdsInterestPayableAccount'},
        {name :'istdsInterestPayableAccountisUsed'},
        {name :'deducteeTypeId'},
        {name :'deducteeCode'},
        {name :'residentialstatus'},
        {name: 'dealertype'},
        {name: 'interstateparty'},
        {name: 'gtaapplicable'}, // GTA Applicable  ERP-25539
        {name: 'isInterstatepartyEditable'},
        {name: 'isUsedInTransactions'},
        {name: 'isVendorUsedInTDSTransactions'},
        {name: 'cformapplicable'},
        {name: 'vatregdate',type:'date'},
        {name: 'cstregdate',type:'date'},
        {name: 'defaultnatureofpurchase'},
        {name: 'manufacturertype'},
        {name: 'importereccno'},
        {name: 'iecno'},
        {name: 'range'},
        {name: 'division'},
        {name: 'defaultnatureofpurchase'},
        {name: 'commissionerate'},
        {name: 'deliveryDate'},
        {name: 'deliveryTime'},
        {name: 'vehicleNo'},
        {name: 'vehicleNoID'},
        {name: 'driver'},
        {name: 'driverID'},
        {name: 'currencysymbol'},
        {name: 'dtaaApplicable'},
        {name: 'dtaaFromDate'},
        {name: 'dtaaToDate'},
        {name: 'dtaaSpecialRate'},
        {name: 'higherTDSRate'},
        {name: 'lowerRate'},
        {name: 'nonLowerDedutionApplicable'},
        {name: 'deductionReason'},
        {name: 'certificateNo'},
        {name: 'deductionFromDate', type: 'date'},
        {name: 'deductionToDate', type: 'date'},
        {name: 'referenceNumberNo'},
        {name: 'minPriceValueForVendor'},
        {name: 'considerExemptLimit'}
]);
    this.msgLmt = 30;
    this.jReader = new Wtf.data.KwlJsonReader({
        totalProperty: 'totalCount',
        root: "data"
    }, Wtf.personRec);
    if(this.custAmountDueMoreThanLimit){
        this.mainURL = "ACC"+this.businessPerson+"CMN/get"+this.businessPerson+"sForDefaultCustomerList.do"
    }else{
        this.mainURL="ACC"+this.businessPerson+"CMN/get"+this.businessPerson+"s.do";
    }
    this.Store = new Wtf.data.Store({
        title:this.businessPerson+" Information",
//        url:Wtf.req.account+this.businessPerson+'Manager.jsp',
        url:this.mainURL,
        baseParams:{
            mode:2,
            group:(config.isCustomer?[10]:[13]),
            custAmountDueMoreThanLimit:this.custAmountDueMoreThanLimit
        },
        reader: this.jReader,
        remoteSort:true
    });
   // Wtf.Ajax.timeout = 1800000;
    this.Store.on('beforeload',function(s,o){
        WtfGlobal.setAjaxTimeOut();
        //ERP-20056--To get Configs of showing columns and hiding columns
//        if(this.moduleId==Wtf.Acc_Customer_ModuleId||this.moduleId==Wtf.Acc_Vendor_ModuleId ){
//           
//            Wtf.Ajax.requestEx({
//                url: "ACCAccountCMN/getCustomizedReportFields.do",
//                params: {
//                    flag: 34,
//                    moduleid:this.moduleId,
//                    reportId:1,
//                    isFormField:false,
//                    isLineField:false
//                }
//            }, this, function(action, response){
//                if(action.success && action.data!=undefined){
//                    this.customizeData=action.data;
//                    var cm=this.grid.getColumnModel();
//                    for(var i=0;i<action.data.length;i++){
//                        for(var j=0;j<cm.config.length;j++){
//                            if((cm.config[j].dataIndex==action.data[i].fieldDataIndex)||(cm.config[j].header==action.data[i].fieldDataIndex)){
//                                cm.setHidden(j,action.data[i].hidecol);
//                            }
//                        }
//                    }
//                    this.grid.reconfigure( s, cm);
//                } else {
//                }
//            },function() {
//                //                Wtf.updateProgress();
//                //                WtfGlobal.resetAjaxReqTimeout();
//                });
//        }
        
        if(!o.params)o.params={};
        o.params.deleted=this.deleted;
        o.params.nondeleted=this.nondeleted;
        o.params.getSundryCustomer=config.isCustomer?true:false;
        o.params.getSundryVendor=config.isCustomer?false:true;
        if(this.showOnetimeCustomer != undefined && this.showOnetimeCustomer.getValue() != "1"){ // Filter Customers 1-ALL,2-Permanent,3-One Time
            o.params.isPermOrOnetime=this.showOnetimeCustomer.getValue() =="3"?true : false;
        }
        if(this.activateDormantFilterCombo!=undefined && this.activateDormantFilterCombo.getValue() != ""){
         o.params.activeDormantFlag=this.activateDormantFilterCombo.getValue();
        }
    },this);

     var CustomerDetails=[
        ['1',WtfGlobal.getLocaleText("acc.cust.OneTimeFilterVal1")], // 1-ALL, 2-Permanent, 3-One Time Customers
        ['2',WtfGlobal.getLocaleText("acc.cust.OneTimeFilterVal2")],
        ['3',WtfGlobal.getLocaleText("acc.cust.OneTimeFilterVal3")],
       ]
  
  
    this.CustomerTypeStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'id'
        },{           
            name:'value'
        }],
        data :CustomerDetails
        
    });
   this.activateDormantFilterCombo = new Wtf.form.ComboBox({
        store: this.isCustomer? Wtf.activeDormantCustomerStore:Wtf.activeDormantStore,
        name:'typeid',
        displayField:'name',
        valueField:'typeid',
        mode: 'local',
        value:-1,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    
    this.customerReports = [
        [Wtf.autoNum.customerRegistryReport, "Customer Registry Report"],
    ];

    this.vendorReoports = [
        [Wtf.autoNum.vendorRegistryReport, "Vendor Registry Report"],
    ];
   
    this.moduleStore = new Wtf.data.SimpleStore({
        fields: ["id", "name"],
        data: (this.isCustomer)?this.customerReports:this.vendorReoports
    });
    this.reportsCombo = new Wtf.form.ComboBox({
        store: this.moduleStore,
        name: 'id',
        displayField: 'name',
        valueField: 'id',
        mode: 'local',
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });
  
    this.showOnetimeCustomer = new Wtf.form.ComboBox({
        store: this.CustomerTypeStore,
        mode: 'local',
        triggerAction: 'all',
        hidden:!config.isCustomer,
        editable: false,
        fieldLabel: WtfGlobal.getLocaleText("acc.customerdetails.CustomerType"),
        emptyText:WtfGlobal.getLocaleText("acc.cust.EmptyTextSelectOneTime"),
        allowBlank: false,
        width: 160,
        valueField: 'id',
        displayField: 'value'
    });
    this.showOnetimeCustomer.setValue("2"); // Default to Permanent Customers
    this.showOnetimeCustomer.on('select', this.LoadOneTimeCustomer, this);
    this.activateDormantFilterCombo.on('select', this.loadactiveDormatVendor, this);
    this.reportsCombo.on('select', this.loadRegistryReport, this);

    var glblStore=(config.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore);
    glblStore.on('beforeload',function(){this.Store.reload();},this);    
    this.Store.on('load',this.hideMsg,this);
    this.Store.on('loadexception',this.hideMsg,this);
    this.colModelArray = GlobalColumnModelForReports[this.moduleId];
    WtfGlobal.updateStoreConfig(this.colModelArray, this.Store);
    this.LineLevelcolModelArray=GlobalColumnModel[this.moduleId];
    WtfGlobal.updateStoreConfig(this.LineLevelcolModelArray, this.Store);
    this.Store.load({
        params: {
            start: 0,
            limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
        }
    });
    WtfComMsgBox(29,4,true);
    this.btnArr=[];
    this.secondBtnArr=[];
    
    this.bbarBtnArr=[];
    this.bbarBtnArrEDSingleS=[]; // Enable/Disable button's indexes on single select
    this.bbarBtnArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
    this.btnArrEDSingleS=[]; // Enable/Disable button's indexes on single select
    this.btnArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
    var chart=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.chart"),  //'Chart',
        tooltip :config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.chartTT"):WtfGlobal.getLocaleText("acc.vendorList.chartTT"),  //'Get the graphical view of your key '+this.businessPerson+'(s).',
       // id: 'chartRec' + this.businessPerson,
        scope: this,
        handler:this.getChart,
        id:'chart'+(config.isCustomer?6:7),
       iconCls :(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart')

    });
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    this.addPerson= new Wtf.Action({
            text:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.addNewCustomer"):WtfGlobal.getLocaleText("acc.vendorList.addNewvendor"),  //'Add New '+this.businessPerson,
            id:(config.isCustomer?"addNewCustomer6":"vendorsManagement7"),
            scope:this,
            tooltip:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.addTT"):WtfGlobal.getLocaleText("acc.vendorList.addTT")), //"Add new "+this.businessPerson+" details. You may also add a sub-"+this.businessPerson+" to an existing "+this.businessPerson+".",
            iconCls:getButtonIconCls(Wtf.etype.menuadd),
            handler:this.showForm.createDelegate(this,[false,false])
        })
        this.editPerson=new Wtf.Action({
            text:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.editCustomer"):WtfGlobal.getLocaleText("acc.vendorList.editvendor"),  //'Edit '+this.businessPerson,
            id:(config.isCustomer?'editCustomer6':'editVendor7'),
            scope:this,
            tooltip:config.isCustomer?WtfGlobal.getLocaleText("acc.rem.51"):WtfGlobal.getLocaleText("acc.rem.52"),  //{text:"Select a "+this.businessPerson+" to edit.",dtext:"Select a "+this.businessPerson+" to edit.", etext:"Edit selected "+this.businessPerson+" details."},
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.menuedit),
            handler:this.showForm.createDelegate(this,[true,false])
        })
        this.copyPerson=new Wtf.Action({
            text:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.copyCustomer"):WtfGlobal.getLocaleText("acc.vendorList.copyvendor"),  //'Copy '+this.businessPerson,
            id:(config.isCustomer?'copyCustomer6':'copyVendor7'),
            scope:this,
            tooltip:config.isCustomer?WtfGlobal.getLocaleText("acc.rem.51.1"):WtfGlobal.getLocaleText("acc.rem.52.1"),  //{text:"Select a "+this.businessPerson+" to edit.",dtext:"Select a "+this.businessPerson+" to edit.", etext:"Edit selected "+this.businessPerson+" details."},
            disabled:true,
            hidden:false,
            iconCls:getButtonIconCls(Wtf.etype.copy),
            handler:this.showForm.createDelegate(this,[false,true])
        });
       this.deletePerson=new Wtf.Action({
            text:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.deleteCustomer"):WtfGlobal.getLocaleText("acc.vendorList.deletevendor"),  // 'Delete '+this.businessPerson,
            id:(config.isCustomer?'deleteCustomer6':'deleteVendor7'),
            scope: this,
            tooltip:config.isCustomer?WtfGlobal.getLocaleText("acc.rem.49"):WtfGlobal.getLocaleText("acc.rem.50"),  //{text:"Select a "+this.businessPerson+" to delete.",dtext:"Select a "+this.businessPerson+" to delete.", etext:"Delete selected "+this.businessPerson+" details."},
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            handler:this.confirmBeforeDeleteCustomerVendor.createDelegate(this)
        })
        /*Customize report view*/
//        this.customReportViewBtn = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"), 
//            scope: this,
//            //            hidden:Wtf.Acc_Customer_ModuleId?true,
//            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
//            handler: this.customizeView,
//            iconCls:'accountingbase fetch'
//        });
        // Show Vendor Activate De-activate 
        this.activateVendor=new Wtf.Action({
            text:this.isCustomer?WtfGlobal.getLocaleText("acc.customer.activateCustomer"): WtfGlobal.getLocaleText("acc.vendor.activateVendor"),
            scope: this,
            disabled:true,
            tooltip:this.isCustomer?WtfGlobal.getLocaleText("acc.customer.activateCustomer"):WtfGlobal.getLocaleText("acc.vendor.activateVendor"),  //Activate Vendor'
            iconCls:getButtonIconCls(Wtf.etype.activate),
            handler:this.activateDeactivateVendor.createDelegate(this,this.activateDeactivate=["activate"])
        });
        
        this.deactivateVendor=new Wtf.Action({
            text:this.isCustomer?WtfGlobal.getLocaleText("acc.customer.deactivateCustomer"):WtfGlobal.getLocaleText("acc.vendor.deactivateVendor"),
            scope: this,
            disabled:true,
            tooltip:this.isCustomer?WtfGlobal.getLocaleText("acc.customer.deactivateCustomer"):WtfGlobal.getLocaleText("acc.vendor.deactivateVendor"),
            iconCls:getButtonIconCls(Wtf.etype.deactivate),
           handler:this.activateDeactivateVendor.createDelegate(this,this.activateDeactivate=["deactivate"])
        });
        
        var extraParams = "";
        var importOpenTransactionsbtnArray=[];
        
        if((!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.importsalesinvoice)&&this.businessPerson=="Customer")||(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.importpurchaseinvoice)&&this.businessPerson!="Customer")){
            var importModuleName = this.businessPerson=="Customer"?Wtf.OpeningModuleName.openingSalesInvoice:Wtf.OpeningModuleName.openingPrchaseInvoice;
            var extraConfig = {};
            extraConfig.isExcludeXLS=true;
            extraConfig.url= this.businessPerson=="Customer"?"ACCInvoice/importOpeningBalanceInvoice.do":"ACCGoodsReceipt/importOpeningBalanceInvoice.do";
            var importProductbtnArray = Wtf.importMenuArray(this, importModuleName, this.Store, extraParams, extraConfig);
            this.importCustomerInvoice=new Wtf.Action({
                text: (this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.field.ImportCustomerInvoice"):WtfGlobal.getLocaleText("acc.field.ImportVendorInvoice"),
                scope: this,
                tooltip:this.isCustomer?WtfGlobal.getLocaleText("acc.field.ImportCustomerInvoice"):WtfGlobal.getLocaleText("acc.field.ImportVendorInvoice"), 
                iconCls: 'pwnd importcsv',
                menu: importProductbtnArray
            });
            importOpenTransactionsbtnArray.push(this.importCustomerInvoice);
        }

        if((!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.salesreceivepayment.importreceivepayments)&&this.businessPerson=="Customer")||(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.purchasemakepayment.importmakepayment)&&this.businessPerson!="Customer")){
            var importModuleName = (this.businessPerson=="Customer")?Wtf.OpeningModuleName.openingReceipt:Wtf.OpeningModuleName.openingPayment;
            var extraConfig = {};
            extraConfig.isExcludeXLS=true;
            extraConfig.url= (this.businessPerson=="Customer")?"ACCReceipt/importOpeningBalanceReceipts.do?":"ACCVendorPayment/importOpeningBalancePayments.do?";
            var importOpnTranBtnArray = Wtf.importMenuArray(this, importModuleName, this.Store, extraParams, extraConfig);
            this.importReceivePayment=new Wtf.Action({
                text: (this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.field.ImportReceivePayments"):WtfGlobal.getLocaleText("acc.field.ImportMakePayments"),
                scope: this,
                tooltip: this.isCustomer?WtfGlobal.getLocaleText("acc.field.ImportReceivePayments"):WtfGlobal.getLocaleText("acc.field.ImportMakePayments"), 
                iconCls: 'pwnd importcsv',
                menu: importOpnTranBtnArray
            })
            importOpenTransactionsbtnArray.push(this.importReceivePayment);
        }    
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditnote, Wtf.Perm.creditnote.importcreditnote)){
            var importModuleName = (this.businessPerson=="Customer")?Wtf.OpeningModuleName.openingCustomerCreditNote:Wtf.OpeningModuleName.openingVendorCreditNote;
            var extraConfig = {};
            extraConfig.isExcludeXLS=true;
            extraConfig.url= (this.businessPerson=="Customer")?"ACCCreditNote/importOpeningBalanceCustomerCNs.do?":"ACCCreditNote/importOpeningBalanceVendorCNs.do?";
            var importOpnTranBtnArray = Wtf.importMenuArray(this, importModuleName, this.Store, extraParams, extraConfig);
            this.importCreditNote=new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.field.ImportCreditNote"),
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.field.ImportCreditNote"), 
                iconCls: 'pwnd importcsv',
                menu: importOpnTranBtnArray
            })
            importOpenTransactionsbtnArray.push(this.importCreditNote);
        }

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.debitnote, Wtf.Perm.debitnote.importdebitnote)){
            var importModuleName = (this.businessPerson=="Customer")?Wtf.OpeningModuleName.openingCustomerDebitNote:Wtf.OpeningModuleName.openingVendorDebitNote;
            var extraConfig = {};
            extraConfig.isExcludeXLS=true;
            extraConfig.url= (this.businessPerson=="Customer")?"ACCDebitNote/importOpeningBalanceCustomerDNs.do?":"ACCDebitNote/importOpeningBalanceVendorDNs.do?";
            var importOpnTranBtnArray = Wtf.importMenuArray(this, importModuleName, this.Store, extraParams, extraConfig);
            this.importDebitNote=new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.field.ImportDebitNote"),
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.field.ImportDebitNote"), 
                iconCls: 'pwnd importcsv',
                menu: importOpnTranBtnArray
            })
            importOpenTransactionsbtnArray.push(this.importDebitNote);
        }
        
        /*
         * Below if block is used to put import Opening SO/PO Transaction button
        */
            var moduleName = (this.businessPerson=="Customer")?Wtf.OpeningModuleName.openingCustomerSalesOrder:Wtf.OpeningModuleName.openingVendorPurchaseOrder;
            var extraConfig = {};
            extraConfig.isOpeningOrder=true;
            extraConfig.isExcludeXLS=true;
            extraConfig.url= (this.businessPerson=="Customer")?"ACCSalesOrder/importSalesOrders.do":"ACCPurchaseOrder/importPurchaseOrders.do";
            var moduleId=(this.businessPerson=="Customer")?Wtf.Acc_Sales_Order_ModuleId:Wtf.Acc_Purchase_Order_ModuleId;
            var importOpnTranBtnArray = Wtf.importMenuArray(this, moduleName, this.Store, extraParams, extraConfig);
            this.importOpeningOrder=new Wtf.Action({
                text: (this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.field.ImportOpeningSalesOrder"):WtfGlobal.getLocaleText("acc.field.ImportOpeningPurchaseOrder"),
                scope: this,
                tooltip:(this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.field.ImportOpeningSalesOrder"):WtfGlobal.getLocaleText("acc.field.ImportOpeningPurchaseOrder"),
                iconCls: 'pwnd importcsv',
                menu: importOpnTranBtnArray
            });
            importOpenTransactionsbtnArray.push(this.importOpeningOrder);
        
        if(importOpenTransactionsbtnArray.length>0){
            this.importOpeningTransactionsBtn=new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.field.ImportOpeningTransactions"),
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.field.ImportOpeningTransactions"),
                iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
                menu:importOpenTransactionsbtnArray,
                hidden : this.custAmountDueMoreThanLimit
            })
        }

        // Creating import button for IBG Import ERP-23223
        var importIBGDetailsBtnArray=[];
        var extraConfig = {};
        //DBS Bank
        extraConfig.isExcludeXLS=true;
        extraConfig.url="ACCVendorCMN/importDBSBankDetails.do";
        var importDBSDetailsBtnArray = Wtf.importMenuArray(this, 'DBS Receiving Bank Details', this.Store, extraParams, extraConfig);
        this.importDBSDetailsBtn=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.import.dbsBankDetails"),
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.import.dbsBankDetails"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu:importDBSDetailsBtnArray
        })
        importIBGDetailsBtnArray.push(this.importDBSDetailsBtn); 

        //CIBM Bank TODO
        //    extraConfig = {}
        //    extraConfig.isExcludeXLS=true;
        //        extraConfig.url="ACCVendorCMN/importCIMBBankDetails.do";
        //        var importCIMBDetailsBtnArray = Wtf.importMenuArray(this, 'CIMB Receiving Bank Details', this.Store, extraParams, extraConfig);

        //        this.importCIMBDetailsBtn=new Wtf.Action({
        //            text: WtfGlobal.getLocaleText("acc.field.ImportOpeningTransactions"),
        //            scope: this,
        //            tooltip:WtfGlobal.getLocaleText("acc.field.ImportOpeningTransactions"),
        //            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
        //            menu:importCIMBDetailsBtnArray
        //        })



        this.importIBGDetails = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.ibg.bank.ibg.importibgdetails"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.ibg.bank.ibg.importibgdetails"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu: importIBGDetailsBtnArray,
            hidden:(this.businessPerson=="Customer") || !Wtf.account.companyAccountPref.activateIBG || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA //ERP-29466
        }); 


       this.syncCustomer=[];
       if(config.isCustomer && !(Wtf.account.companyAccountPref.standalone)){
            this.syncCustomer.push(new Wtf.Action({
                    text:WtfGlobal.getLocaleText("acc.productList.dataSync"),//'Data Sync',
                    tooltip: WtfGlobal.getLocaleText("acc.customerlist.dataSync.ttip"),
                    iconCls:getButtonIconCls(Wtf.etype.sync),
                    hidden:(config.datasync!=undefined),
                    menu : [{
                            text:WtfGlobal.getLocaleText("acc.field.CustomerSyncToCRM"),
                            scope : this,
                            tooltip:WtfGlobal.getLocaleText("acc.field.CustomerSyncToCRMTT"),  
                            iconCls:getButtonIconCls(Wtf.etype.syncmenuItem),
                            hidden:!Wtf.isCRMSync,//hidden when crm not subscribed
                            handler:function(){
                                if(!Wtf.account.companyAccountPref.activateCRMIntegration){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.crmnotacivatedalert")],2);
                                }else {
                                    this.syncCustomerCRM(this.sync=["CRM"]);                              
                                }                               
                            }                                
                    },{
                            text:WtfGlobal.getLocaleText("acc.field.CustomerSyncFromCRM"),
                            scope : this,
                            tooltip:WtfGlobal.getLocaleText("acc.field.CustomerSyncFromCRMTT"),  
                            iconCls:getButtonIconCls(Wtf.etype.syncmenuItem),
                            hidden:!Wtf.isCRMSync,//hidden when crm not subscribed
                            handler:function(){
                                if(!Wtf.account.companyAccountPref.activateCRMIntegration){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.crmnotacivatedalert")],2);
                                }else {
                                    this.syncCustomerCRM(this.synch=["ERP"]);                       
                                }                               
                            }                                                   
                    },{
                            text:WtfGlobal.getLocaleText("acc.field.CustomerSyncFromLMS"),
                            scope : this,
                            tooltip:WtfGlobal.getLocaleText("acc.field.CustomerSyncFromLMSTT"),  
                            iconCls:getButtonIconCls(Wtf.etype.syncmenuItem),
                            hidden:!Wtf.isLMSSync,//hidden when LMS not subscribed                            
                            handler:function(){
                                if(!Wtf.account.companyAccountPref.isLMSIntegration){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.lmsnotacivatedalert")],2);
                                }else {
                                    this.syncCustomerfromLMS();              
                                }                               
                            }   
                    }]            
                })); 
       } 
       this.openingBalance=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalance"),
            id:'openingBalance'+this.id,
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.field.ViewOpeningBalance"),
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.edit),
            handler:this.viewOpeningBalance.createDelegate(this)
        })
        
       this.ibgDetails=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.ibg.bank.ibg.details"),
            id:'ibgdetails'+this.id,
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.ibg.bank.ibg.details"),
            disabled:true,
            hidden:!this.shouldIBGDetailsButtonShown(),
            iconCls:getButtonIconCls(Wtf.etype.edit),
            handler:this.viewIBGDetails.createDelegate(this)
        });
        var searchCriteria = ", "+WtfGlobal.getLocaleText("acc.field.CustomerVendorUEN");
        this.quickSearchTF = new Wtf.KWLTagSearch({
            emptyText:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.search"):WtfGlobal.getLocaleText("acc.vendorList.search"))+searchCriteria,  //'Search by '+this.businessPerson+' Name',
            width: 150,
            id:'quickSearch'+config.id,
            field: 'accname',
            Store:this.Store
        });
        this.btnArr.push(this.quickSearchTF);
                 this.btnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            hidden:this.isSummary,
            tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search '+this.businessPerson+' name'+' by clearing existing search  '+this.businessPerson+' name'+'.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        }));
        this.resetBttn.on('click',this.handleResetClick,this);
        if(!this.custAmountDueMoreThanLimit){
            this.btnArr.push(this.AdvanceSearchBtn);
        }
        this.custArr=[];
        this.custArrEDSingleS=[]; // Enable/Disable button's indexes on single select
        this.custArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
    if(!WtfGlobal.EnableDisable(this.uPermType,this.permType.create)&&!this.custAmountDueMoreThanLimit) {
            //        this.btnArr.push(this.addPerson);
            this.custArr.push(this.addPerson);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)&&!this.custAmountDueMoreThanLimit) {
            //        this.btnArr.push(this.editPerson);
            //        this.btnArrEDSingleS.push(this.btnArr.length-1);
            this.custArr.push(this.editPerson); 
            this.custArrEDSingleS.push(this.custArr.length-1);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.copy)&&!this.custAmountDueMoreThanLimit) {
            //        this.btnArr.push(this.deletePerson);
//            this.btnArrEDMultiS.push(this.btnArr.length-1);
            this.custArr.push(this.copyPerson); 
            this.custArrEDSingleS.push(this.custArr.length-1);
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove)&&!this.custAmountDueMoreThanLimit) {
            this.custArr.push(this.deletePerson); 
            this.custArrEDMultiS.push(this.custArr.length-1);
    }
    this.custArr.push(this.activateVendor);
    this.custArr.push(this.deactivateVendor);
        if(this.custArr.length>0) {
            this.btnArr.push({
                 text:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.customerMenu"):WtfGlobal.getLocaleText("acc.vendorList.vendorMenu"), 
                tooltip:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.customerMenuTT"):WtfGlobal.getLocaleText("acc.vendorList.vendorMenuTT"),  //{text:"Click here to add, edit, clone or delete a Customer."},
                iconCls :getButtonIconCls(Wtf.etype.customer),
                menu:this.custArr
            });
    }
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.openingbalance)&&!this.custAmountDueMoreThanLimit) {
        this.btnArr.push(this.openingBalance);
        this.btnArrEDSingleS.push(this.btnArr.length-1);
    }
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.viewibgdetails) &&!this.custAmountDueMoreThanLimit) {
        this.btnArr.push(this.ibgDetails);
        this.btnArrEDSingleS.push(this.btnArr.length-1);
    }
    
    
       // this.btnArrEDMultiS.push(this.btnArr.length-1);

    this.viewContractDetailBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.cust.viewCustomerContracts"),  // 'View Customer Contracts',
        tooltip :WtfGlobal.getLocaleText("acc.cust.viewCustomerContracts"),  // 'View Customer Contracts',
        id: 'btnviewCustomerContractDetails' + this.id,
        scope: this,
        hidden:!config.isCustomer,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport),
        disabled :true
    });
    
    this.viewContractDetailBtn.on('click', function(){
        var rec = this.sm.getSelected();
        callCustomerContractDetailsTab(rec);
    }, this);
    this.btnArr.push(this.viewContractDetailBtn);
    this.btnArrEDSingleS.push(this.btnArr.length-1);
//    this.btnArr.push(this.customReportViewBtn);//Adding Customize report view button
    
    this.pricingBandGroupingDetails = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.pricingBandGrouping"), // "Pricing Band Grouping",
        id: 'pricingBandGroupingDetails' + this.id,
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.field.pricingBandGrouping"),
        hidden: (!this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && !Wtf.account.companyAccountPref.productPricingOnBandsForSales),
        iconCls: getButtonIconCls(Wtf.etype.reorderreport),
        handler: this.viewPricingBandGroupingDetails.createDelegate(this)
    });
    
    this.btnArr.push(this.pricingBandGroupingDetails);
        
    if (this.businessPerson == "Customer") {
        this.secondBtnArr.push(WtfGlobal.getLocaleText("acc.customerdetails.CustomerType"));
        this.secondBtnArr.push(this.showOnetimeCustomer);
    }
    this.secondBtnArr.push("&nbsp;"+WtfGlobal.getLocaleText("acc.cc.8"), this.activateDormantFilterCombo);
//    this.secondBtnArr.push("&nbsp;Report:", this.reportsCombo);


    var firsttbar = new Wtf.Toolbar(this.btnArr);
    var secondtbar = new Wtf.Toolbar(this.secondBtnArr);
    var moduleIdToGetReports=this.businessPerson == "Customer" ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
    WtfGlobal.getReportMenu(firsttbar, moduleIdToGetReports, WtfGlobal.getModuleName(moduleIdToGetReports));

    this.toolbarPanel = new Wtf.Panel({
        border: false,
        items: [firsttbar, secondtbar]
    });
    this.sm = new Wtf.grid.CheckboxSelectionModel({
    	header: (Wtf.isIE7)?"":'<div class="x-grid3-hd-checker"> </div>'    // For IE 7 the all select option not available
    });
    
    this.gridColumnModelArr=[];
    this.gridColumnModelArr.push(this.sm,{  
            header:"",
            hidden:true,
            dataIndex:'accid',
            hideable:false      //ERP-5269[SJ]      
        },{       
            header:(this.businessPerson == "Customer")? WtfGlobal.getLocaleText("acc.common.customer.code") : WtfGlobal.getLocaleText("acc.common.vendor.code"),
            dataIndex:'acccode',//dataIndex:'accnamecode',
            renderer:WtfGlobal.linkDeletedRenderer,
            sortable: true,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),  //"Name",
            dataIndex:'accname',//dataIndex:'accnamecode',
            renderer:WtfGlobal.deletedRenderer,
            sortable: true,
            pdfwidth:75
        },
        {
            header:config.isCustomer?WtfGlobal.getLocaleText("acc.cust.aliasname"): WtfGlobal.getLocaleText("acc.ven.aliasname"),   //"AliasName",
            dataIndex:'aliasname',
            renderer:WtfGlobal.deletedRenderer,
            sortable: true,
            pdfwidth:75
        },{
            header: WtfGlobal.getLocaleText("acc.customer.controlaccountcode"),   //"Control Account Code",
            dataIndex:'controlaccountcode',
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.customer.controlaccountname"),   //"Control Account Name",
            dataIndex:'controlaccountname',
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Address"),  //"Billing Address",
            dataIndex:'billingAddress',
            renderer:WtfGlobal.deletedRenderer,
//            sortable: true,
            pdfwidth:75
        }
        ,{
            header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.State"),  //"Billing State",
            dataIndex:'billingState',
            renderer:WtfGlobal.deletedRenderer,
//            sortable: true,
            pdfwidth:75
        }        
        ,{
            header:WtfGlobal.getLocaleText("acc.address.ContactPerson"),  //"Contact Person" ERP-11857 [SJ],
            dataIndex:'billingContactPerson',
            renderer:WtfGlobal.deletedRenderer,
//            sortable: true,
            pdfwidth:75
        }
        ,{
            header:WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),  //"Contact Person no." ERP-11857 [SJ],
            dataIndex:'billingContactPersonNumber',
            renderer:WtfGlobal.deletedRenderer,
//            sortable: true,
            pdfwidth:75
        }
        ,{
            header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Email"),  //"Billing Email",
            dataIndex:'billingEmailID',
//            sortable: true,
            pdfwidth:110,
           renderer:WtfGlobal.renderDeletedEmailsTo
        },{
            header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Mobile"),  //"Billing Mobile",
            dataIndex:'billingMobileNumber',
//            sortable: true,
            pdfwidth:75,
            renderer:WtfGlobal.renderDeletedContactToSkype
        },{
            header:config.isCustomer?WtfGlobal.getLocaleText("acc.field.CustomerUEN"): WtfGlobal.getLocaleText("acc.field.VendorUEN"),
            dataIndex:'uenno',
            sortable: true,
            hidden : (Wtf.account.companyAccountPref.countryid != '203'),
            pdfwidth:75
        },{
            header:this.isCustomer?WtfGlobal.getLocaleText("acc.masterConfig.15"):WtfGlobal.getLocaleText("acc.masterConfig.20"),  //"Sales Person"/"Agent"
            dataIndex:'salesPersonAgent',
            pdfwidth:125
        },{
            header: WtfGlobal.getLocaleText("acc.accreport.Status"), // "Status", Activate/Deactivate
            dataIndex: 'isactivate',
            pdfwidth:110,
            sortable: true
        //hidden: this.isCustomer
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalance"),  //"Opening Balance",
            dataIndex:'openbalance',
            align:'right',
            //            hidden:true,
//            renderer:this.opBalRenderer,
            renderer:WtfGlobal.currencyDeletedRenderer,//SDP-3679 Amount is always in Base currency 
            //            summaryType:'sum',
            //            summaryRenderer:this.opBalRenderer,
            pdfwidth:75,
            pdfrenderer:"rowcurrency"				// Opening Balance to be displayed in customer/vendor currency in CSV and pdf Export
        },{
            header: WtfGlobal.getLocaleText("acc.customerList.gridCreationDate"),  //"Creation Date",
            dataIndex: "creationDate",
            sortable: true,
            //This date is handled on JAVA side & sent as String only
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalanceType"),  //'Opening Balance Type',
            dataIndex: 'openbalance',
            pdfwidth:75,
            hidden:true,
//            summaryType:'sum',
//            summaryRenderer:this.balTypeRenderer,
            renderer:this.balTypeRenderer
         },{
                header :WtfGlobal.getLocaleText("acc.customerList.gridCurrency"),  //'Currency',
            pdfwidth:75,
            dataIndex: 'currencyname',
            hidden:true,            
            renderer:WtfGlobal.deletedRenderer
        },{
            header:(Wtf.account.companyAccountPref.countryid != '203')?WtfGlobal.getLocaleText("acc.customerList.gridOtherInfo"):WtfGlobal.getLocaleText("acc.field.GSTNumber"),  //"Other Information",
            dataIndex:'other',
            renderer:WtfGlobal.deletedRenderer,
            sortable: true,
            hidden: !Wtf.account.companyAccountPref.withouttax1099,
            pdfwidth:50
//            pdfrenderer:"rowcurrency"
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridShippingAddress"),  //"Shipping Address",
            dataIndex:'shippingAddress',
//            sortable: true,
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75,
            hidden: !config.isCustomer

        },{
            header:WtfGlobal.getLocaleText("acc.address.Shipping")+" "+WtfGlobal.getLocaleText("acc.address.State"),  //"Shipping State",
            dataIndex:'shippingState',
            renderer:WtfGlobal.deletedRenderer,
//            sortable: true,
            pdfwidth:75
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.gridCreditTerm"):WtfGlobal.getLocaleText("acc.customerList.gridDebitTerm")),  //"Credit":"Debit")+" Term",
            dataIndex:'termname',
            sortable: true,
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:125//,
//            hidden: true
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridAmountDue"),  //"Amount Due",
            dataIndex:'amountdue',
            align: 'right',
            pdfwidth:75,
            pdfrenderer:"rowcurrency",
            defaultselectionunchk:true,         // " column unselected by default when export 
            renderer:this.formatMoney
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.cust.creditLimit"):WtfGlobal.getLocaleText("acc.cust.debitLimit")),  //"Credit":"Debit")+" limit",
            dataIndex:'limit',
            sortable: true,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol, //SDP-3679 Will always be shown in Customer Currency
            align: 'right',
            pdfwidth:100//,
//            hidden: true
        },{
            header:WtfGlobal.getLocaleText("acc.field.ExceededAmount"),  //"Credit":"Debit")+" limit",
            dataIndex:'exceededamount',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,//SDP-3679 Will always be shown in Customer Currency
            pdfwidth:75,
            defaultselectionunchk:true,         // " column unselected by default when export 
            hidden:this.custAmountDueMoreThanLimit?false:true
        });
        if ((Wtf.account.companyAccountPref.productPricingOnBandsForSales || Wtf.account.companyAccountPref.productPricingOnBands)) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.field.priceBand"),
            dataIndex: 'pricingBandName',
            renderer:WtfGlobal.deletedRenderer,
//            sortable: true,
            align: 'left',
            pdfwidth: 75
        });
    }
    if (!(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST)) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.grid.DefaultTaxCode"), //"Default Tax Code",
            dataIndex: 'taxcode',
//            sortable: true,
            renderer: WtfGlobal.deletedRenderer,
            pdfwidth: 125
        });
    }
    if (this.moduleId == Wtf.Acc_Vendor_ModuleId && Wtf.account.companyAccountPref.countryid=='137') {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.vendor.gstverifiedDate"), //"GST Varified Date",
            dataIndex: "gstVerifiedDate",
            sortable: true,
            //This date is handled on JAVA side & sent as String only
            renderer: WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth: 150
        });
    }
    if (this.moduleId == Wtf.Acc_Vendor_ModuleId && Wtf.account.companyAccountPref.blockPOcreationwithMinValue) {
        this.gridColumnModelArr.push({
            header:WtfGlobal.getLocaleText("acc.field.BlockPOcreationfield"),  
            dataIndex:'minPriceValueForVendor',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:75
        });
    }
    if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstin"), //"GST Registration Number",
            dataIndex: "gstin",
            sortable: true,
            pdfwidth: 150
        });
        this.gridColumnModelArr.push({
            header: WtfGlobal.getLocaleText("acc.masterConfig.62"), //"GST Registration Type",
            dataIndex: "GSTINRegistrationTypeName",
            sortable: true,
            pdfwidth: 150
        });
//        this.gridColumnModelArr.push({
//            header: WtfGlobal.getLocaleText("acc.GST.sezFromDate"), //"GST Varified Date",
//            dataIndex: "sezfromdate",
//            sortable: true,
//            renderer: WtfGlobal.onlyDateDeletedRenderer,
//            pdfwidth: 150
//        });
//        this.gridColumnModelArr.push({
//            header: WtfGlobal.getLocaleText("acc.GST.sezToDate"), //"GST Varified Date",
//            dataIndex: "seztodate",
//            sortable: true,
//            renderer: WtfGlobal.onlyDateDeletedRenderer,
//            pdfwidth: 150
//        });
    }
    if (Wtf.Countryid == Wtf.Country.INDIA || Wtf.Countryid == Wtf.Country.INDONESIA) {
        /*
         * For Indonesia - Tax Type & For India -  Customer/ Vendor Type
         */
        this.gridColumnModelArr.push({
            header: Wtf.Countryid == Wtf.Country.INDONESIA ? WtfGlobal.getLocaleText("acc.common.taxType") : (config.isCustomer ? WtfGlobal.getLocaleText("acc.customer.GST.type") : WtfGlobal.getLocaleText("acc.vendor.GST.type")), //"Customer/Vendor Type",
            dataIndex: "CustomerVendorTypeName",
            sortable: true,
            pdfwidth: 150
        });
    }
    /*Appending Custom Column in Grid of Customer Details*/
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,this.colModelArray,true);
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,this.LineLevelcolModelArray,true,undefined,undefined,undefined,this.moduleId);
    
    this.grid = new Wtf.grid.HirarchicalGridPanel({
        store:this.Store,
        sm: this.sm,
        border:false,
        layout:'fit',
//        id:'cust_grid',
        hirarchyColNumber:3,
        //plugins:[this.summary],
        viewConfig:{
            forceFit:false,
            emptyText:WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openPersonWin(\""+config.isCustomer+"\")'>"+(config.isCustomer?WtfGlobal.getLocaleText("acc.cus.rem1"):WtfGlobal.getLocaleText("acc.ven.rem1"))+"</a>")
        },
        forceFit:false,
        loadMask:true,
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr)
//        columns:[]
    });
    
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    },this);
    this.pageLimit = new Wtf.forumpPageSize({
        ftree:this.grid,
        recordsLimit: Wtf.MaxPageSizeLimit
    });
     this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleId,
        advSearch: false,
        isAvoidRedundent:true
    });
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.pToolBar = new Wtf.PagingSearchToolbar({
        id: 'pgTbar' + this.id,
        pageSize: this.msgLmt,
        store: this.Store,
        searchField: this.quickSearchTF,
//        displayInfo: true,
//        displayMsg: 'Displaying records {0} - {1} of {2}',
//        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No records to display",
        plugins: this.pageLimit,
        items : this.bbarBtnArr
    });
    this.Store.on("load", this.setPageSize, this);
    this.Store.on('datachanged', function() {
        var p = this.pageLimit.combo.value;
        this.quickSearchTF.setPage(p);
    }, this);

    this.bbarBtnArr.push('-');
    if(importOpenTransactionsbtnArray.length>0){
        if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.openingbalance)) { // SDP-12403 - import opening transaction should be based on Opening Balance permission
            this.bbarBtnArr.push(this.importOpeningTransactionsBtn);
        }
    }
    if(importIBGDetailsBtnArray.length>0){
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.importdata)) {
            this.bbarBtnArr.push('-');
            this.bbarBtnArr.push(this.importIBGDetails);
        }
    }
    if(config.isCustomer && !(Wtf.account.companyAccountPref.standalone)){
        this.bbarBtnArr.push('-');
        this.bbarBtnArr.push(this.syncCustomer);
    }

    if(config.isCustomer) {
        this.bbarBtnArr.push(this.defaultwarehouseBtn= new Wtf.Toolbar.Button({
            obj:this,
            text: WtfGlobal.getLocaleText("acc.warehouse.set.default.warehouse"),
            tooltip: WtfGlobal.getLocaleText("acc.warehouse.set.default.tooltip"),
            disabled :true,
            id:'defaultwarehousebtn',
            //hidden:true,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            handler: function(){
                if(this.grid.getSelectionModel().getSelections().length==1){
                    var record = this.grid.getSelectionModel().getSelections();
                    this.setDefaultWarehouse(record);
                }
            },
            scope:this
        }))
          this.bbarBtnArrEDSingleS.push(this.bbarBtnArr.length-1);
    }
       if (!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.exportdata)) {
           this.exportselRec = new Wtf.exportButton({
                obj: this,
                id: (config.isCustomer?("exportselCustomerLists"+config.helpmodeid):("exportselVendorLists"+this.id)),
                iconCls: 'pwnd exportpdfsingle',
                get: (config.isCustomer?113:114),
                text: WtfGlobal.getLocaleText("acc.field.ExportSelRecord"), // + " "+ singlePDFtext,
                tooltip: WtfGlobal.getLocaleText("acc.field.ExportSelRecord"), //'Export selected record(s)'
                filename: config.isCustomer? (config.custAmountDueMoreThanLimit?config.title+"_v1":(WtfGlobal.getLocaleText("acc.customerList.customerExportFileName"))+"_v1"): (WtfGlobal.getLocaleText("acc.customerList.venmdorExportFileName"))+"_v1",
                disabled: true,
                menuItem: {csv: true, xls: true,pdf:true}
            });
           this.bbarBtnArr.push(this.exportselRec);    
           this.bbarBtnArr.push(this.exportButton=new Wtf.exportButton({
                obj:this,
                text:WtfGlobal.getLocaleText("acc.common.export"),
                tooltip:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.exportCustomer"):WtfGlobal.getLocaleText("acc.vendorList.exportVendor")),  //"Export "+this.businessPerson+" details",  //.toLowerCase()+" details",
                disabled :true,
                filename: config.isCustomer? (config.custAmountDueMoreThanLimit?config.title+"_v1":(WtfGlobal.getLocaleText("acc.customerList.customerExportFileName"))+"_v1"): (WtfGlobal.getLocaleText("acc.customerList.venmdorExportFileName"))+"_v1",//ERP-17331
                id:(config.isCustomer?("exportCustomerLists6"+config.helpmodeid):("exportVendorLists7"+this.id)),
                menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
                get:(config.isCustomer?113:114),
                label:this.businessPerson.toLowerCase(),
                isDefaultCustomerList : this.custAmountDueMoreThanLimit
            }));
    }
    
    var importBtnArr = [];
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.importdata)) {
        var extraConfig = {};
        extraConfig.url= config.isCustomer?"ACCCustomer/importCustomer.do":"ACCVendor/importVendor.do";
        var extraParams = "{\"DepreciationAccont\":\""+Wtf.account.companyAccountPref.depreciationaccount+"\"}";
        this.importBtnArray= Wtf.importMenuArray(this, this.businessPerson, this.Store, extraParams, extraConfig);
//        this.importButton= Wtf.importMenuButtonA(this.importBtnArray, this, this.businessPerson);
        this.importButton = new Wtf.Action({
            text: config.isCustomer ? WtfGlobal.getLocaleText("acc.field.importCustomer") : WtfGlobal.getLocaleText("acc.field.importVendor"),
            scope: this,
            tooltip: config.isCustomer ? WtfGlobal.getLocaleText("acc.field.importCustomer") : WtfGlobal.getLocaleText("acc.field.importVendor"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: this.importBtnArray
        });
        
        if(!this.custAmountDueMoreThanLimit){
            importBtnArr.push(this.importButton);
        }
    }
    
    // For importing Address Details
    var addressExtraConfig = {};
    addressExtraConfig.url= "ACCCustomerCMN/importCustomerAddressDetails.do";
    var addressExtraParams = "";
    var importAddressDetailsBtnArray = Wtf.importMenuArray(this, "Customer Address Details", this.Store, addressExtraParams, addressExtraConfig);
    
    this.importAddressDetailsBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.field.importCustomerAddressDetails"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.field.importCustomerAddressDetails"),
        iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
        menu: importAddressDetailsBtnArray
    });
    if (config.isCustomer) {
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.importdata)) {            
            importBtnArr.push(this.importAddressDetailsBtn);
        }
    }
    
    if (importBtnArr.length > 0) {
        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu: importBtnArr
        });
        this.bbarBtnArr.push(this.importBtn);
    }
    
    if (!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.print)) {
        this.bbarBtnArr.push(this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            id:(config.isCustomer?("printCustomer6"+this.id):("printVendor7"+this.id)),
            tooltip:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.printCustomer"):WtfGlobal.getLocaleText("acc.vendorList.printVendor")),  //"Print "+this.businessPerson+" details",   //.toLowerCase()+" details",
            disabled :true,
            filename: config.isCustomer?(config.custAmountDueMoreThanLimit?config.title:(WtfGlobal.getLocaleText("acc.customerList.tabTitle"))) : (WtfGlobal.getLocaleText("acc.vendorList.tabTitle")), //ERP-17331
            params:{name:config.isCustomer?WtfGlobal.getLocaleText("acc.rem.14"):WtfGlobal.getLocaleText("acc.vendorList.tab")},
            menuItem:{print:true},
            get:(config.isCustomer?113:114),
            label:this.businessPerson.toLowerCase()
        }));        
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.chart)&&!this.custAmountDueMoreThanLimit) {
        this.btnArr.push(chart);
    }
    
    this.sm.on('selectionchange', this.changeMsg, this);
    this.btnArr.push("->");
//    this.btnArr.push(this.typeEditor);
    this.btnArr.push(getHelpButton(this,(config.isCustomer?(config.helpmodeid!=undefined)?config.helpmodeid:6:7)));
//    this.tbar=this.btnArr;
//    this.bbar = this.pToolBar;
    this.typeEditor.on('select',this.loadTypeStore,this);
    Wtf.account.BusinessContactPanel.superclass.constructor.call(this,config);
    this.getMyConfig();
    this.grid.on('cellclick',this.onCellClick, this);
    /*
     * select customer or vendor ids to export selected records
     */
   if (this.exportselRec) {
    this.exportselRec.on('click',function(){
        var records=this.sm.getSelections();
        var exportcustvenids="";
        for(var i=0;i<records.length;i++){
            exportcustvenids=exportcustvenids+"'"+records[i].data.accid+"',"
        }
        this.exportselRec.setParams({
            exportcustvenids:exportcustvenids
        });
       }, this);
    }
//    this.grid.on('savemystate',this.saveMyStateHandler,this)
//    this.grid.on("columnmove", this.saveMyState, this);
//    this.grid.on("columnresize", this.saveMyState, this);
//    this.grid.colModel.on("hiddenchange", this.saveMyState, this);
//    this.grid.colModel.on("widthchange", this.saveMyState, this);
//    this.grid.colModel.on("configchanged", this.saveMyState, this);
}
Wtf.extend(Wtf.account.BusinessContactPanel,Wtf.Panel,{
    loadTypeStore:function(a,rec){
        this.deleted=false;
        this.nondeleted=false;
        var index=rec.data.typeid;
        if(index==1){
            this.deleted=true;
        }
        else if(index==2)
            this.nondeleted=true;
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt,
                ss : this.quickSearchTF.getValue()
            }
        });
        WtfComMsgBox(29,4,true);
    },
    customizeView : function() {
        this.customizeViewWin=new Wtf.customizeView({
            scope:this,
            moduleid:this.moduleId,
            parentHelpModeId:this.helpmodeid,
            parentId:this.id,
            isForFormFields : false
        });
        this.customizeViewWin.show();
    },
    handleResetClick:function(){
        if(this.quickSearchTF.getValue()){
            this.quickSearchTF.reset();
            this.Store.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
            }
        });
        }
    },
    setPageSize: function(store, rec, opt){
        var count = 0;
        for (var i = 0; i < store.getCount(); i++) {
            if (rec[i].data['level'] == 0 && (rec[i].data['parentid'] == "" || rec[i].data['parentid'] == undefined))
                count++;
        }
//        if(this.jReader.jsonData['totalCount']) {
//            this.getBottomToolbar().displayEl.update("Displaying records " + parseInt(opt.params["start"] + 1) + " - " + parseInt(count + opt.params["start"]) + " of " + this.jReader.jsonData['totalCount']);
//        }
//        this.pageLimit.totalSize = this.jReader.jsonData['totalCount'];
    },
    storeloaded:function(store){
        Wtf.MessageBox.hide();
    },
   calllinkRowColor:function(id){
        var index=this.Store.find('id',id );
         var rec=this.Store.getAt(index);
         if(index>=0)
            WtfGlobal.highLightRowColor(this.grid,rec,true,0,0);
   },
    changeMsg:function(sm,index,record){
        Wtf.uncheckSelAllCheckbox(this.sm);

        WtfGlobal.enableDisableBtnArr(this.btnArr, this.grid, this.btnArrEDSingleS, this.btnArrEDMultiS);
        WtfGlobal.enableDisableBtnArr(this.bbarBtnArr, this.grid, this.bbarBtnArrEDSingleS, this.bbarBtnArrEDMultiS);
        WtfGlobal.enableDisableBtnArr(this.custArr, this.grid, this.custArrEDSingleS, this.custArrEDMultiS);
        var arr=this.grid.getSelectionModel().getSelections();
        if(arr.length==1){
            this.recid=this.grid.getSelectionModel().getSelections()[0].data.accid;
            this.acccode=this.grid.getSelectionModel().getSelections()[0].data.acccode;
	    this.getDetailPanel();
        }
        if (this.exportselRec) {
          if (arr.length >= 1) {
              this.exportselRec.enable();
          } else {
              this.exportselRec.disable();
          }
        }
        var record=this.grid.getSelectionModel().getSelected();
        if (arr.length == 1) {
            if (record && record.data.isactivate=="Active") {
                this.activateVendor.disable();
                this.deactivateVendor.enable();
            } else {
                this.activateVendor.enable();
                this.deactivateVendor.disable();
            }
            this.ibgDetails.enable();
        } else {
            this.activateVendor.enable();
            this.deactivateVendor.enable();
            this.ibgDetails.disable();
        }
        
        for(var i=0;i<arr.length;arr++){
            if(arr[i]&&arr[i].data.deleted) {
                this.deletePerson.disable();
                this.editPerson.disable();
            }
        }
        var selRecArr = this.grid.getSelectionModel().getSelections();
        if (selRecArr != undefined && selRecArr.length > 0) {
            var isdormant = false;
            var isactive = false;
            for (var i = 0; i < selRecArr.length; i++) {

                //When multiple custoer are selected at that to handle enable/disable of customer/vendor added following check.
                if (selRecArr[i].data.isactivate === Wtf.Customer.Dormant) {
                    if (!isdormant) {
                        isdormant = true;
                    }

                }
                //When multiple custoer are selected at that to handle enable/disable of customer/vendor added following check.
                if (selRecArr[i].data.isactivate === Wtf.Customer.Active) {
                    if (!isactive) {
                        isactive = true;
                    }

                }
            }
            if (isdormant === true) {
                this.activateVendor.enable();

            } else {
                this.activateVendor.disable();
            }
            if (isactive === true) {
                this.deactivateVendor.enable();
            } else {
                this.deactivateVendor.disable();
            }
        } else {
            this.activateVendor.disable();
            this.deactivateVendor.disable();
        }
    },

    hideMsg: function(store){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.uncheckSelAllCheckbox(this.sm);
        this.quickSearchTF.StorageChanged(store);
        if(this.Store.getCount()==0){
             
//            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer("<a class='grid-link-text' href='#' onClick='javascript:openPersonWin(\""+config.isCustomer+"\")'>"+(config.isCustomer?WtfGlobal.getLocaleText("acc.cus.rem1"):WtfGlobal.getLocaleText("acc.ven.rem1"))+"</a>");
//            this.grid.getView().refresh();                 
                
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();            
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        Wtf.MessageBox.hide();
        if(this.personlinkid!=undefined)
            this.calllinkRowColor(this.personlinkid);
    },

    onRender: function(config){
     this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            listeners:{
                'afterlayout':function(p){
                    if(p.attachDetailTrigger){
                        p.layout.south.slideOut = p.layout.south.slideOut.createSequence(this.getDetails,this);
                        delete p.attachDetailTrigger;
                    }
                    },
                    scope:this
            },
            items: [this.objsearchComponent
            , {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.toolbarPanel,
                bbar: this.pToolBar
            },{
                region:'south',
                height:250,
                hidden:!Wtf.account.companyAccountPref.viewDetailsPerm ,
                title:WtfGlobal.getLocaleText("acc.editors.otherdetailregion"),//'Other Details',
                collapsible:true,
                collapsed : true,
                collapsibletitle : WtfGlobal.getLocaleText("acc.editors.otherdetailregion"),//'Other Details',
                plugins: new Wtf.ux.collapsedPanelTitlePlugin(),
                layout: "fit",
                split : true,
                items:[this.detailPanel],
                listeners:{
                    'expand':this.getDetails,
                    scope:this
                }
            }]
        }); 
       this.add(this.leadpan);  
        var lastTransPanelId = "";
        lastTransPanelId = this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        if(this.openperson){
            callBusinessContactWindow(false, null, 'bcwin', this.isCustomer);
            var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
            Wtf.getCmp(tabid).on('update',this.updateGrid,this);
        }
        Wtf.account.BusinessContactPanel.superclass.onRender.call(this,config);
    },

    showForm:function(isEdit,isCopy){
       this.recArr =[] ;
       this.isEdit=isEdit;
        if(isEdit){
            this.recArr =this.grid.getSelectionModel().getSelections();
            if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                if(this.isCustomer)
                    WtfComMsgBox(11,2);
                else
                    WtfComMsgBox(19,2);
                return;
                }
        }
        if(isEdit){
            var chkURL = "ACCVendorCMN/checkVendorTransactions.do";
            if(this.businessPerson=="Customer")
                chkURL = "ACCCustomerCMN/checkCustomerTransactions.do";

            var  rec=this.recArr[0];
            Wtf.Ajax.requestEx({
                url:chkURL,
                params:{
                    accid:rec.data.accid,
                    openbalance:rec.data.openbalance
                }
            },
            this,function(response,a){
                callBusinessContactWindow(isEdit, rec, 'bcwin', this.isCustomer,response.success);
                var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
                Wtf.getCmp(tabid).on('update',this.updateGrid,this);
            },function(response,a){
                callBusinessContactWindow(isEdit, rec, 'bcwin', this.isCustomer);
                var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
                Wtf.getCmp(tabid).on('update',this.updateGrid,this);
            });

        }else{
            if(isEdit || isCopy){
                this.recArr =this.grid.getSelectionModel().getSelections();
            }
            var  rec=this.recArr[0];
            callBusinessContactWindow(isEdit,rec, 'bcwin', this.isCustomer,null,isCopy);
            var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
            if(isEdit != undefined && isEdit){
                tabid = "edit-"+tabid;
            }else if(isCopy !=undefined && isCopy){
                tabid = "copy-"+tabid;
            }
            Wtf.getCmp(tabid).on('update',this.updateGrid,this);
        }
    },

     updateGrid: function(obj,perAccID){
        this.perAccID=perAccID;
        this.Store.reload();
        this.isAdd=true;
        this.Store.on('load',this.colorRow,this)
    },
    
    getDetailPanel:function(){
        this.detailPanel = new Wtf.DetailPanel({
            grid:this.grid,
            Store:this.Store,
            modulename:this.isCustomer?'Customer':'Vendor',
            keyid:this.isCustomer?'customerid':'vendorid',
            height:200,
            mapid:1,
            id2:this.id,
            moduleID:this.isCustomer?Wtf.Acc_Customer_ModuleId:Wtf.Acc_Vendor_ModuleId,
            moduleName:this.isCustomer?'Customer':'Vendor',
            mainTab:this.mainTab,
            leadDetailFlag:true,
            moduleScope:this,
            accid:this.recid,
            acccode:this.acccode
        });

    },
    getDetails:function(){
        var sm = this.grid.getSelectionModel();
        var errorcode="";
        if(sm.getCount()==0){
          errorcode="Error0";  
        }else if(sm.getCount()>1){
            errorcode="Error1";
        }
        var commentlist = getDocsAndCommentList(sm.getSelected(), this.moduleId,this.id,undefined,this.isCustomer?'Customer':'Vendor',undefined,"email",'leadownerid',this.contactsPermission,0,this.recid,errorcode);
    },
    colorRow: function(store){
        if(this.isAdd && (!this.isEdit)){
            this.recArr=[];
            if(store.find('accid',this.perAccID) != -1) {
                this.recArr.push(store.getAt(store.find('accid',this.perAccID)));
                WtfGlobal.highLightRowColor(this.grid,this.recArr[0],true,0,0);
            }
            this.isAdd=false;
        }
    },
     confirmBeforeDeleteCustomerVendor: function () {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText({key: "acc.customervendormaster.propagated.delete.confirm", params: [this.businessPerson == "Customer" ? "customer(s)" : "vendor(s)"]}),
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
                    this.scopeObject.deletePersonFunc();
                }
            }, this);
        } else {
            this.deletePersonFunc();
        }
    },

    deletePersonFunc: function(){
       var arr=[];
       var data=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
       this.grid.getSelectionModel().clearSelections();
      WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
       Wtf.MessageBox.show({
       title: WtfGlobal.getLocaleText("acc.common.warning"),  //"Warning",
//       msg: "Are you sure you want to delete the selected "+this.businessPerson+" and all associated sub "+this.businessPerson+"(s)?<div><b>Note: This data cannot be retrieved later</b></div>",
       msg: this.businessPerson=="Customer"?WtfGlobal.getLocaleText("acc.customerList.delTT"):WtfGlobal.getLocaleText("acc.vendorList.delTT")+"<div><b>"+WtfGlobal.getLocaleText("acc.customerList.delTT1")+"</b></div>",
       width: 560,
       buttons: Wtf.MessageBox.OKCANCEL,
       animEl: 'upbtn',
       icon: Wtf.MessageBox.QUESTION,
       scope:this,
       fn:function(btn){
            if(btn!="ok"){
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.Store.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
            for(i=0;i<this.recArr.length;i++){
                arr.push(this.Store.indexOf(this.recArr[i]));
                data += "{accid:"+this.recArr[i].data.accid+",openbalance:"+this.recArr[i].data.openbalance+"}";
                if(this.Store.find('parentid',this.recArr[i].data.accid) != -1){
                	data += ",";
                	var child = this.Store.getAt(this.Store.find('parentid',this.recArr[i].data.accid));
                	data += "{accid:"+child.data.accid+",openbalance:"+child.data.openbalance+"}";

                	while(this.Store.find('parentid',child.data.accid) != -1){
                		data += ",";
                		child = this.Store.getAt(this.Store.find('parentid',child.data.accid));
                    	data += "{accid:"+child.data.accid+",openbalance:"+child.data.openbalance+"}";
                	}
                }
                if(i<this.recArr.length-1)
                	data += ",";
            }
            data = "["+data+"]";
//        data= WtfGlobal.getJSONArray(this.grid,true,arr);     // [Issue 24363] New: [Delete Customer] Unable to Delete 300 Customers in one go.
        this.deleteUrl = "";
        if(this.businessPerson=="Customer") {
            this.deleteUrl = "ACCCustomerCMN/deleteCustomer.do";
        } else if(this.businessPerson=="Vendor") {
            this.deleteUrl = "ACCVendorCMN/deleteVendor.do";
        }
        WtfGlobal.setAjaxTimeOut(); //ERP-2145
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                url:this.deleteUrl,
                params:{
                   data:data,
                    mode:3,
                    ispropagatetochildcompanyflag:this.ispropagatetochildcompanyflag
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
            
            this.ispropagatetochildcompanyflag=false; //after deleting reocrd setting this flag to 'false'.
       }});
    },
    activateDeactivateVendor: function(activateDeactivate) {
        this.activateDeactivateUrl = "";
        if(this.businessPerson=="Customer") {
            this.activateDeactivateUrl = "ACCCustomerCMN/activateDeactivateCustomers.do";
        } else if(this.businessPerson=="Vendor") {
            this.activateDeactivateUrl = "ACCVendorCMN/activateDeactivateVendors.do";
        }
        var arr = [];
        var data = [];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        var activateDeactivateFlag = false;
        
        if (activateDeactivate == "activate") {
            activateDeactivateFlag = true;        //Send this flag as true whenever you want to activate or deactivate Vendors.  
        }
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"), //"Confirm",
            msg: activateDeactivateFlag ?(this.isCustomer ?WtfGlobal.getLocaleText("acc.customer.customerActivateConfirmMsg"):WtfGlobal.getLocaleText("acc.vendor.VendorActivateConfirmMsg")):(this.isCustomer ?WtfGlobal.getLocaleText("acc.customer.customerDeactivateConfirmMsg"):WtfGlobal.getLocaleText("acc.vendor.VendorDeactivateConfirmMsg")),
            width: 560,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function(btn) {
                if (btn != "ok") {
                    return;
                }
                for (i = 0; i < this.recArr.length; i++) {
                    arr.push(this.Store.indexOf(this.recArr[i]));
                }
                data = WtfGlobal.getJSONArray(this.grid, true, arr);

                Wtf.Ajax.requestEx({
                    url: this.activateDeactivateUrl,
                    params: {
                        data: data,
                        activateDeactivateFlag: activateDeactivateFlag, //Send this flag as true whenever you want to Activate or Deactivate Customers/Vendors.
                        mode: 1
                    }
                }, this, this.genActivateSuccessResponse, this.genActivateFailureResponse);
            }
        });
    },
    genActivateSuccessResponse: function(response) {
        var title="";
        if(this.businessPerson=="Customer") {
            title = WtfGlobal.getLocaleText("acc.customerList.tabTitle");
        } else if(this.businessPerson=="Vendor") {
            title = WtfGlobal.getLocaleText("acc.vendorList.tabTitle");
        }
        this.Store.on('load', function() {
            WtfComMsgBox([(title), response.msg], response.success * 2 + 1);
        }, this.Store, {
            single: true
        });
        this.Store.reload();
        
    },
    genActivateFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
viewOpeningBalance : function(){
   this.recArr =this.grid.getSelectionModel().getSelections();
   if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
       if(this.isCustomer)
           WtfComMsgBox(11,2);
       else
           WtfComMsgBox(19,2);
       return;
   }
//   this.grid.getSelectionModel().clearSelections();
//   WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,1);
   var rec=this.recArr[0];
   
   this.callOpeningBalaneWindow(rec, this.isCustomer);
},

viewIBGDetails:function(){
    this.recArr =this.grid.getSelectionModel().getSelections();
   if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
           WtfComMsgBox(19,2);
       return;
   }
   
   var rec=this.recArr[0];
   
   this.callIBGDetailsGrid(rec);
},

viewPricingBandGroupingDetails: function() {
    this.isCustomer? callCustomerByPricingBandReport() : callVendorByPricingBandReport();
},

callOpeningBalaneWindow:function(rec, isCustomer){
    var openingBalenceWindow = new Wtf.account.openingBalanceWindow({
        title:WtfGlobal.getLocaleText("acc.field.OpeningBalances"),
        layout:'border',
        accRec:rec,
        isActivateFlag : (rec.data.isactivate == Wtf.Customer.Active),
        id:'openBalWinId',
        resizable:false,
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        modal:true,
        isCustomer:isCustomer,
        height:600,
        width:700
    });
    openingBalenceWindow.show();
    openingBalenceWindow.on('updateBushinessPersonGrid', this.updateCustomerDetailsGrid,this);
},
callcustomerSeqFormatWindow:function(isCustomer){
    var customerSeqFormatWindow = new Wtf.account.customerSeqFormatWindow({
        title:WtfGlobal.getLocaleText("acc.field.SyncCustomer.Sequence"),
        layout:'border',
        id:'customerSequenceNumber',
        resizable:false,
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        modal:true,
        isCustomer:isCustomer,
        height:300,
        width:400,
        grid:this.grid
    });
    customerSeqFormatWindow.show();
//    customerSeqFormatWindow.on('updateBushinessPersonGrid', this.updateCustomerDetailsGrid,this);
},

callIBGDetailsGrid:function(rec){
    var ibgDetailsGrid = new Wtf.account.VendorIBGDetailsGrid({
        title:WtfGlobal.getLocaleText("acc.vendorIBG.ReceivingBankDetails"),
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        height:500,
        width:850,
        accRec:rec,
        modal:true,
        isCustomer:this.isCustomer,
        customerId : rec.data.accid,
        bankType : this.isCustomer?Wtf.IBGBanks.UOBBank:undefined,
        layout:'border'
    });
    
    ibgDetailsGrid.show();
},

updateCustomerDetailsGrid:function(){
    (function(){
        WtfGlobal.loadpersonacc(this.isCustomer);//this.Store.reload();
    }).defer(WtfGlobal.gridReloadDelay(),this);
},

    genSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.show({
        title: WtfGlobal.getLocaleText("acc.common.info"),
        msg: response.msg,
        buttons: Wtf.MessageBox.OK,
        animEl: 'upbtn',
        icon: Wtf.MessageBox.INFO,
        scope:this,
        fn:function(btn){
            for (var i = 0; i < this.recArr.length; i++) {
                    var ind = this.Store.indexOf(this.recArr[i])
                    var num = ind % 2;
                    WtfGlobal.highLightRowColor(this.grid, this.recArr[i], false, num, 2, true);
                }
                if (response.success) {
                    (function () {
                        WtfGlobal.loadpersonacc(this.isCustomer);//this.Store.reload();
                    }).defer(WtfGlobal.gridReloadDelay(), this);
                }
        }
    });

    },

    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    getChart:function(){
          var chartid=this.isCustomer?"customerprioritychartid":"vendorprioritychartid";
          var swf="../../scripts/graph/krwcolumn/krwcolumn/krwcolumn.swf";
          var id1=this.isCustomer?"customerpriorityid":"vendorpriorityid"
//          var dataflag=this.isCustomer?3:4;
          var dataflag=this.isCustomer?"ACCChart/getTopCustomerChart":"ACCChart/getTopVendorsChart";
          var mainid=this.isCustomer?"mainCustomerDetails":"mainVendorDetails";
          var xmlpath= this.isCustomer?'../../scripts/graph/krwcolumn/examples/CustomerPriority/customer_settings.xml':'../../scripts/graph/krwcolumn/examples/VendorPriority/vendor_settings.xml';
          globalChart(chartid,id1,swf,dataflag,mainid,xmlpath,true,this.withinventory);
    },
    
    formatMoney:function(val,m,rec,i,j,s){
//        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
        var v=parseFloat(val);
        if(isNaN(v)) return val;
        
        v= WtfGlobal.conventInDecimalWithLink(v,WtfGlobal.getCurrencySymbol())
        
        return v;     
        
    },
    onCellClick:function(g,i,j,e){
        var el=e.getTarget("a");
        if(el==null) {
            return;
        }
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="amountdue"){
            this.viewTransection(g,i,e)
        } else if(header=="acccode"){
            this.viewBussinessPersonDetail(g,i,e);
        }
    },
    viewTransection:function(grid, rowIndex, columnIndex){
        var formrec=null;
    if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
            WtfComMsgBox(15,2);
            return;
        }
    formrec = this.grid.getStore().getAt(rowIndex);
        var accid=formrec.get('accid');
        var withinventory=this.withinventory;
        if(this.businessPerson=='Customer'){
            callAgedRecievable({withinventory:withinventory,custVendorID:accid}); 
        }else{
            callAgedPayable({withinventory:withinventory,custVendorID:accid});
        }
    },
    viewBussinessPersonDetail: function(grid, rowIndex, columnIndex) {
        var rec = null;
        if (rowIndex < 0 && this.grid.getStore().getAt(rowIndex) == undefined || this.grid.getStore().getAt(rowIndex) == null) {
            WtfComMsgBox(15, 2);
            return;
        }
        rec = this.grid.getStore().getAt(rowIndex);
        callViewBusinessContactWindow(rec, this.isCustomer)
    },
    
    opBalRenderer:function(val,m,rec){
//        return WtfGlobal.currencyDeletedRenderer(Math.abs(val),m,rec);
    	return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },

    balTypeRenderer:function(val,m,rec){
        val=val==0?"N/A":val>0?"Debit":"Credit";
        return WtfGlobal.deletedRenderer(val,m,rec)
    },showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
        
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.Store.load({params: {ss: this.quickSearchTF.getValue(), start: 0, limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt}});
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({params: {ss: this.quickSearchTF.getValue(), start: 0, limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },
    
    LoadOneTimeCustomer: function() {
            this.Store.load(
            {
                params: {
                    ss: this.quickSearchTF.getValue(), 
                    start: 0, 
                    limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
                }
            });

    },
    loadactiveDormatVendor: function() {
            this.Store.load(
            {
                params: {
                    ss: this.quickSearchTF.getValue(), 
                    start: 0, 
                    limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
                }
            });

    },
    loadRegistryReport: function (combo, record, index) {
        if (this.sm.hasSelection() && this.sm.getCount()<2){
            switch (record.data.id) {
                case Wtf.autoNum.customerRegistryReport:
                    var panel = Wtf.getCmp("CustomerRegistryReport");
                    if (panel == null) {
                        panel = new Wtf.account.CustomerVendorRegisteryReport({
                            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.customerregistry.tabTitle")),
                            tabTip: WtfGlobal.getLocaleText("acc.customerregistry.tabTitle"), // "Account Forecast Report",
                            id: 'CustomerRegistryReport',
                            closable: true,
                            border: false,
                            isCustomer: true,
                            fromMaster: true,
                            personid:this.sm.getSelected().get('accid'),
                            iconCls: 'accountingbase vendor',
                            activeTab: 0
                        });
                        Wtf.getCmp('as').add(panel);
                    }
                    Wtf.getCmp('as').setActiveTab(panel);
                    Wtf.getCmp('as').doLayout();
                    break;
                case Wtf.autoNum.vendorRegistryReport:
                    var panel = Wtf.getCmp("VendorRegistryReport");
                    if (panel == null) {
                        panel = new Wtf.account.CustomerVendorRegisteryReport({
                            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.vendorregistry.tabTitle")),
                            tabTip: WtfGlobal.getLocaleText("acc.vendorregistry.tabTitle"), // "Account Forecast Report",
                            id: 'VendorRegistryReport',
                            closable: true,
                            border: false,
                            isCustomer: false,
                            fromMaster: true,
                            personid:this.sm.getSelected().get('accid'),
                            iconCls: 'accountingbase vendor',
                            activeTab: 0
                        });
                        Wtf.getCmp('as').add(panel);
                    }
                    Wtf.getCmp('as').setActiveTab(panel);
                    Wtf.getCmp('as').doLayout();
                    break;
                }
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(this.isCustomer)?WtfGlobal.getLocaleText("acc.customer.singleselect"):WtfGlobal.getLocaleText("acc.vendor.singleselect")],2);
        }
    },

    syncCustomerCRM:function(synchFrom){
        var synchfrom=synchFrom; 
        if( synchfrom=="CRM"){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttosyncsynccustomertocrm"),  //"Are you sure you want to sync products from Inventory?",           
                buttons: Wtf.MessageBox.OKCANCEL,
                animEl: 'upbtn',
                icon: Wtf.MessageBox.QUESTION,
                scope:this,
                fn:function(btn){
                    if(btn=="ok"){
                        Wtf.commonWaitMsgBox("Processing...");
                        WtfGlobal.setAjaxTimeOut();
                        var param={
                            deleted:this.deleted,
                            nondeleted:this.nondeleted,
                            getSundryCustomer:true,
                            getSundryVendor:false
                        }
                        Wtf.Ajax.requestEx({
                            url:"ACCCustomerCMN/createAccountForCustomerInCRM.do",
                            param:param
                        },this,this.genSuccessResponseforSynch,this.genFailureResponseforSynch);
                    }
                }
            });
        } else if(synchfrom=="ERP") {
//            this.callcustomerSeqFormatWindow();   //refer ticket ERP-12487
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttosyncsynccustomerfromcrm"),  //"Are you sure you want to sync products from Crm?"
                buttons: Wtf.MessageBox.OKCANCEL,
                animEl: 'upbtn',
                icon: Wtf.MessageBox.QUESTION,
                scope:this,
                fn:function(btn){
                    if(btn=="ok"){
                        Wtf.commonWaitMsgBox("Processing...");
                        WtfGlobal.setAjaxTimeOut();
                        Wtf.Ajax.requestEx({
                            url:"ACCCustomerCMN/getCustomerFromCRMAccounts.do"
                        },this,this.genSuccessResponseForSyncFromCRM,this.genFailureResponseForSyncFromCRM);
                    }
                }
            });
        }
    },
    
    genSuccessResponseForSyncFromCRM:function(response){
//        this.grid.store.reload();
//        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
    Wtf.updateProgress();
    WtfGlobal.resetAjaxTimeOut();
    Wtf.MessageBox.show({
        title: WtfGlobal.getLocaleText("acc.common.info"),
        msg: response.msg,
        buttons: Wtf.MessageBox.OK,
        animEl: 'upbtn',
        icon: Wtf.MessageBox.INFO,
        scope:this,
        fn:function(btn){
            if(btn=="ok"){
                this.grid.store.reload();
            }
        }
    });
    },
    
    genFailureResponseForSyncFromCRM:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        if(response.statusText == "transaction aborted"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.customer.sync.timeout.msg")],3);
        } else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        }
    },
    
    setDefaultWarehouse:function(record){
           new Wtf.DefaultWarehouseWin({
            id: "defaultlwarwin",
            border : false,
            title: WtfGlobal.getLocaleText("acc.warehouse.set.default.warehouse"),
            layout : 'fit',
            closable: true,
            width:450,
            height:300,
            modal:true,
            isdefaultwarehouse:true,
            record:record,
            resizable:false
        }).show();
   },
     syncCustomerfromLMS:function(){
        
        
           Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.confirm"),
           msg: WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosynccustomerfromLMS"),  //"Are you sure you want to sync products from Inventory?",           
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn=="ok"){
                   
                        WtfGlobal.setAjaxTimeOut();
                         var param={
                            deleted:this.deleted,
                            nondeleted:this.nondeleted,
                            getSundryCustomer:true,
                            getSundryVendor:false
                        }
                        Wtf.Ajax.requestEx({
                            url:"ACCCustomerCMN/getCustomerFromLMS.do",
                            param:param
                        },this,this.genSuccessResponseforSynch,this.genFailureResponseforSynch);
                }
               
           }
        }); 
         
        
    },
    genSuccessResponseforSynch:function(response){
        Wtf.updateProgress();
        WtfGlobal.resetAjaxTimeOut();
//        for(var i=0;i<this.arrRec.length;i++){
//             var ind=this.productStore.indexOf(this.arrRec[i])
//             var num= ind%2;
//             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
//        }
        if(response.success){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.customerList.gridCustomer"),
                msg: response.msg,
                width: 450,
                scope: {
                    scopeObj: this
                },
                fn: function(btn, text, option) {
                    this.scopeObj.Store.reload();
                },
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
            
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.customerList.gridCustomer"),response.msg],response.success*2+1);
             
//            if(response.success){
//            (function(){
//                      
//            }).defer(WtfGlobal.gridReloadDelay(),this);
//            }
        }else{
            if(response.isused="true")
          {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg]);
        }
        else
        {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.customerList.gridCustomer"),WtfGlobal.getLocaleText("acc.field.NoCustomerareavailableforsyncing")],response.success*2+1);
        }
    }        
   },
    genFailureResponseforSynch:function(response){
        WtfGlobal.resetAjaxTimeOut();
//        for(var i=0;i<this.arrRec.length;i++){
//             var ind=this.productStore.indexOf(this.arrRec[i])
//             var num= ind%2;
//             WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
//        }
        var msg=  WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        if(response.statusText == "transaction aborted"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.customer.sync.timeout.msg")],3);
        } else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        }
   },
   
   saveMyState: function(){
//        var state = this.grid.getState();
//        this.grid.fireEvent("savemystate", this, state);
    },
        
    getMyConfig : function(){
        WtfGlobal.getGridConfig (this.grid, this.moduleId, false, false);
    },
    
    saveMyStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleId, grid.gridConfigId, false);
    },
    /*
     * Logic for showing/hiding the IBG details button
     */
    shouldIBGDetailsButtonShown : function(){
        var returnValue=false;
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE){
            if(this.isCustomer){
                returnValue = Wtf.account.companyAccountPref.activateIBGCollection;
            } else {
                returnValue = Wtf.account.companyAccountPref.activateIBG;
            }
        }
        return returnValue;
    }
   
//,
//    importCSVRecords : function(obj, moduleName, store, extraParams, extraConfig){
//        this.impWin1 = Wtf.commonFileImportWindow(obj, moduleName, store, extraParams, extraConfig);
//        this.impWin1.show();
//    },
//    importXLSRecords :function(obj,moduleName,store,extraParams, extraConfig){
//        this.impWin1 = Wtf.xlsCommonFileImportWindow(obj,moduleName,store,extraParams, extraConfig);
//        this.impWin1.show();
//    },
//    mappingCSVInterface:function(Header, res, impWin1, delimiterType, extraParams, extraConfig) {
//        this.filename=res.FileName;
//
//        this.mapCSV=new Wtf.csvFileMappingInterface({
//            csvheaders:Header,
//            modName:this.businessPerson,
//            impWin1:impWin1,
//            delimiterType:delimiterType,
//            cm:this.gridcm,
//            extraParams: extraParams,
//            extraConfig: extraConfig
//        }).show();
//        Wtf.getCmp("csvMappingInterface").on('importfn',this.importCSVfunction, this);
//    },
//    importCSVfunction:function(response, delimiterType, extraParams, extraConfig) {
//        Wtf.importMappedRecords(this, response, this.businessPerson, this.filename, this.Store, delimiterType, extraParams, extraConfig);
//    }
});


//===== By Category Report ========
Wtf.account.BusinessPersonListByCategory=function(config){
    this.isPricingBandGrouping = (config.isPricingBandGrouping != null && config.isPricingBandGrouping != undefined)? config.isPricingBandGrouping : false;
    this.isBySalesPersonOrAgent = (config.isBySalesPersonOrAgent == null || config.isBySalesPersonOrAgent == undefined || config.isBySalesPersonOrAgent == "")? false : config.isBySalesPersonOrAgent;
     Wtf.apply(this, config);
     
     this.businessPerson=(config.isCustomer?'Customer':'Vendor');
     this.isCategoryUpdated = false;
    this.uPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;

     this.typeEditor =CommonERPComponent.createCustomerVendorCategoryPagingCombobox('',250,30,this,true);
     this.typeEditor.store.on('load',this.setValue,this);
     
     this.GridRec = Wtf.data.Record.create ([
        {name: 'accid'},
        {name: 'openbalance'},
        {name: 'id'},
        {name: 'title'},
        {name: 'accname'},
        {name:'aliasname'},
        {name: 'address'},
        {name: 'company'},
        {name: 'email'},
        {name: 'contactno'},
        {name: 'contactno2'},
        {name:'uenno'},
        {name:'vattinno'},
        {name:'csttinno'},
        {name:'gstin'},
        {name:'GSTINRegistrationTypeId'},        
        {name:'CustomerVendorTypeId'},
        {name:'GSTINRegistrationTypeName'},        
        {name:'CustomerVendorTypeName'},
        {name:'servicetaxno'},        
        {name:'tanno'},        
        {name:'eccno'},
        {name:'panno'},
        {name:'vendorbranch'}, 
        {name:'npwp'},
        {name: 'fax'},
        {name: 'shippingaddress'},
        {name: 'shippingState'},        
        {name: 'pdm'},
        {name: 'pdmname'},
        {name: 'parentid'},
        {name: 'parentname'},
        {name: 'bankaccountno'},
        {name: 'termid'},
        {name: 'termname'},
        {name: 'other'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'deleted'},
        {name: 'creationDate' ,type:'date'},
        {name: 'gstVerifiedDate',type:'date'},
//        {name: 'creationDate'},//,type:'date' //This date is handled on JAVA side & sent as String only
        {name: 'taxno'},
        {name: 'categoryid'},
        {name: 'acccode'},
        {name: 'category'},
        {name: 'salesPersonAgentId'},
        {name: 'salesPersonAgent'},
        {name: 'billingAddress'},
        {name: 'billingState'},
        {name: 'billingMobileNumber'},
        {name: 'billingCity'},          //add billing city for SDP-16040
        {name: 'billingEmailID'},
        {name: 'shippingAddress'},
        {name: 'pricingBandID'},
        {name: 'pricingBand'},
        {name: 'isactivate'},
        {name :'deducteeTypeId'},
        {name: 'dealertype'},
        {name: 'typeofdealer'},  // Ticket :ERP-26370   field use for MVAT Annexure Form  Indian Company only for Maharashtra
        {name: 'interstateparty'},
        {name: 'cformapplicable'},
        {name: 'vatregdate',type:'date'},
        {name: 'cstregdate',type:'date'},
        {name: 'defaultnatureofpurchase'},
        {name: 'manufacturertype'},
        {name: 'importereccno'},
        {name: 'iecno'},
        {name: 'range'},
        {name: 'division'},
        {name: 'defaultnatureofpurchase'},
        {name: 'commissionerate'}
        
    ]);
    var personByMethod="";
    if(this.isBySalesPersonOrAgent){
        if(this.isCustomer){
            personByMethod="sBySalesPerson.do";
        }else{
            personByMethod="sByAgent.do";
        }
    }else{
        personByMethod="sByCategory.do";
    }
    this.ByCategoryStore = new Wtf.data.GroupingStore({
        url:"ACC"+this.businessPerson+"CMN/get"+this.businessPerson+personByMethod,
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec),
        baseParams:{
            isPricingBandGrouping: this.isPricingBandGrouping,
            isBySalesPersonOrAgent : this.isBySalesPersonOrAgent
        },
        remoteSort:true,
        groupField: this.isBySalesPersonOrAgent?"salesPersonAgent":(this.isPricingBandGrouping? "pricingBand" : "category"),
        sortInfo: {field: 'accname',direction: "ASC"}
    });
    this.ByCategoryStore.on('beforeload',function(){
        WtfComMsgBox(29,4,true);
    },this);
    this.ByCategoryStore.on('load',this.storeloaded, this);
    this.ByCategoryStore.on('loadException',this.storeloaded, this);
    
    var editBtnLabel="",editBtnTooltip="";
    if(this.isBySalesPersonOrAgent){
        if(this.isCustomer){
            editBtnLabel=WtfGlobal.getLocaleText("acc.cust.editSalesPerson");
            editBtnTooltip=WtfGlobal.getLocaleText("acc.customerList.editCustSalesPersonTT");
        }else{
            editBtnLabel=WtfGlobal.getLocaleText("acc.vend.editAgent");
            editBtnTooltip=WtfGlobal.getLocaleText("acc.vendorList.editVendorAgentTT");
        }
    }else{
        if(this.isPricingBandGrouping){
            editBtnLabel=WtfGlobal.getLocaleText("acc.common.edit") + " " + WtfGlobal.getLocaleText("acc.field.pricingBandMaster");
        }else{
            if(this.isCustomer){
                editBtnLabel=WtfGlobal.getLocaleText("acc.cust.editCategoty");
            }else{
                editBtnLabel=WtfGlobal.getLocaleText("acc.vend.editCategory");
            }
        }
        if(this.isCustomer){
            editBtnTooltip=WtfGlobal.getLocaleText("acc.customerList.editCustCatTT");
        }else{
            editBtnTooltip=WtfGlobal.getLocaleText("acc.vendorList.editVendorCatTT");
        }
    }
    
    this.editPersonCategory=new Wtf.Toolbar.Button({
        text: editBtnLabel,  //'Edit '+this.businessPerson,
        id:this.isBySalesPersonOrAgent?(config.isCustomer?'editCustomerSalesPerson15':'editVendorAgent20'):(config.isCustomer?'editCustomerCategory6':'editVendorCategory7'),
        scope:this,
        tooltip:editBtnTooltip,  //{text:"Select a "+this.businessPerson+" to edit.",dtext:"Select a "+this.businessPerson+" to edit.", etext:"Edit selected "+this.businessPerson+" details."},
        iconCls:getButtonIconCls(Wtf.etype.edit)
    });
    this.editPersonCategory.on('click',this.editCustomerCategory.createDelegate(this,[config.isCustomer?true:false]),this);
     
    this.sm = new Wtf.grid.RowSelectionModel();
    this.grid = new Wtf.grid.GridPanel({    
        store:this.ByCategoryStore,
        border:false,
        layout:'fit',
        view: new Wtf.grid.GroupingView({
            forceFit:false
        }),
        loadMask:true,
        columns:[
            {
                header:this.isCustomer?WtfGlobal.getLocaleText("acc.cust.Customercategory"):WtfGlobal.getLocaleText("acc.cust.vendorcategory"),  //"Category",
                dataIndex:'category',
                hidden: (this.isPricingBandGrouping)?true:false,
                pdfwidth:75,
//                fixed: true,
                renderer:WtfGlobal.deletedRenderer
            },{
                header:this.isCustomer?WtfGlobal.getLocaleText("acc.masterConfig.15"):WtfGlobal.getLocaleText("acc.masterConfig.20"),  //"Sales Person"/"Agent"
                dataIndex:'salesPersonAgent',
                // hidden: true,
                // fixed: true,
                pdfwidth:75,
                renderer:WtfGlobal.deletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.field.pricingBandMaster"),  //"Pricing List-Band",
                dataIndex: 'pricingBand',
                hidden: true,
                fixed: true,
                renderer: WtfGlobal.deletedRenderer
            },{
                header:this.isCustomer?WtfGlobal.getLocaleText("acc.common.customer.code") : WtfGlobal.getLocaleText("acc.common.vendor.code"),
                dataIndex:'acccode',
                renderer:WtfGlobal.deletedRenderer,
                sortable: true,
                pdfwidth:75
            },{    
                header:WtfGlobal.getLocaleText("acc.customerList.gridName"),  //"Name",
                dataIndex:'accname',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridAliasName"),  //"Alias Name",
               dataIndex:'aliasname',
               sortable: true,
               renderer:WtfGlobal.deletedRenderer,        
               pdfwidth:75
           },{
                header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Address"),  //"Billing Address",
                dataIndex:'billingAddress',
                sortable: true,
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.State"),  //"Billing Address",
                dataIndex:'billingState',
                sortable: true,
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75
            },{
                header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Email"),  //"Billing Email",
                dataIndex:'billingEmailID',
                pdfwidth:110,
                sortable: true,
                renderer:WtfGlobal.renderDeletedEmailsTo
            },{
               header: WtfGlobal.getLocaleText("acc.accreport.Status"), // "Status", Activate/Deactivate
               dataIndex: 'isactivate',
               pdfwidth:110,
               sortable: true
               //hidden: this.isCustomer
            },{
                header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Mobile"),  //"Billing Mobile",
                dataIndex:'billingMobileNumber',
                pdfwidth:75,
                renderer:WtfGlobal.renderDeletedContactToSkype
            },{
                header: WtfGlobal.getLocaleText("acc.address.Billing") + " " + WtfGlobal.getLocaleText("acc.address.City"), //"Billing city" for SDP-16040,
                dataIndex: 'billingCity',
                pdfwidth: 75,
                renderer: WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalance"),  //"Opening Balance",
                dataIndex:'openbalance',
                align:'right',
                renderer:this.opBalRenderer,
                summaryType:'sum',
                summaryRenderer:this.opBalRenderer,
                pdfwidth:75,
                pdfrenderer:"currency"
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridCreationDate"),  // "Creation Date",
                dataIndex: "creationDate",
                //This date is handled on JAVA side & sent as String only
                renderer:WtfGlobal.onlyDateDeletedRenderer,
                pdfwidth:150
            },{
            header: WtfGlobal.getLocaleText("acc.vendor.gstverifiedDate"),  //"GST Varified Date",
            dataIndex: "gstVerifiedDate",
            sortable: true,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:150,
            hidden:Wtf.account.companyAccountPref.countryid!='137'
            },{
                header :WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalanceType"),  //'Opening Balance Type',
                dataIndex: 'openbalance',
                pdfwidth:75,
                summaryType:'sum',
                summaryRenderer:this.balTypeRenderer,
                renderer:this.balTypeRenderer
             },{
                header :WtfGlobal.getLocaleText("acc.customerList.gridCurrency"),  //'Currency',
                pdfwidth:75,
                dataIndex: 'currencyname',
                renderer:WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridOtherInfo"),  //"Other Information",
                dataIndex:'other',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:50
            },{
                header:WtfGlobal.getLocaleText("acc.customerList.gridShippingAddress"),  //"Shipping Address",
                dataIndex:'shippingAddress',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75,
                hidden: !config.isCustomer
            },{
                header:WtfGlobal.getLocaleText("acc.address.Shipping")+" "+WtfGlobal.getLocaleText("acc.address.State"),  //"Shipping State",
                dataIndex:'shippingState',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:75,                
            },{
                header:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.gridCreditTerm"):WtfGlobal.getLocaleText("acc.customerList.gridDebitTerm")),  //"Credit":"Debit")+" Term",
                dataIndex:'termname',
                renderer:WtfGlobal.deletedRenderer,
                pdfwidth:125
            }]
    });
    Wtf.account.BusinessPersonListByCategory.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.BusinessPersonListByCategory,Wtf.Panel,{
        onRender: function(config){
        this.ByCategoryStore.load({
            params:{
                start:0,
                limit:30,
                isPricingBandGrouping: this.isPricingBandGrouping,
                isBySalesPersonOrAgent : this.isBySalesPersonOrAgent
            }
        });
        var filterName="";
        if(this.isBySalesPersonOrAgent){
            if(this.isCustomer){
                filterName=WtfGlobal.getLocaleText("acc.masterConfig.15");
            }else{
                filterName=WtfGlobal.getLocaleText("acc.masterConfig.20");
            }
        }else{
            if(this.isPricingBandGrouping){
                filterName=WtfGlobal.getLocaleText("acc.field.pricingBandMaster") ;
            }else{
                if(this.isCustomer){
                    filterName=WtfGlobal.getLocaleText("acc.cust.Customercategory")
                }else{
                    filterName=WtfGlobal.getLocaleText("acc.cust.vendorcategory")
                }
            }
        }
        this.bbarBtnArr=new Array();       
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.exportdata)) {
            this.bbarBtnArr.push(this.exportButton=new Wtf.exportButton({ //ERP-19693
                obj:this,
                text:WtfGlobal.getLocaleText("acc.common.export"),
                tooltip:this.tabTip,            
                filename:this.tabTip, 
                menuItem:{
                    csv:true,
                    pdf:true,
                    rowPdf:false,
                    xls:true
                    },
                get:(this.isCustomer?1005:1006),
                hidden:!(this.id=='CustomerBySalesPersonDetails' || this.id=='VendorByAgentDetails' || this.id=="CustomerByCategoryDetails" || this.id=="VendorByCategoryDetails"),
                label:this.businessPerson.toLowerCase(),
                isDefaultCustomerList : this.custAmountDueMoreThanLimit
                }));
        }
        if (this.id == "CustomerByCategoryDetails" || this.id == "VendorByCategoryDetails") {
            var categoryConfig = {};
            categoryConfig.url = (this.id == "VendorByCategoryDetails") ? "ACCVendorCMN/importVendorCategory.do" : "ACCCustomerCMN/importCustomerCategory.do";
            var moduleName = (this.id == "VendorByCategoryDetails") ? "Vendor Category" : "Customer Category";
            var importBtnArr = Wtf.importMenuArray(this, moduleName, this.ByCategoryStore, "", categoryConfig);
            if (!WtfGlobal.EnableDisable(Wtf.UPerm.customer, Wtf.Perm.customer.importdata)) {
                this.importBtn = new Wtf.Action({
                    text: WtfGlobal.getLocaleText("acc.common.import"),
                    scope: this,
                    tooltip: WtfGlobal.getLocaleText("acc.common.import"),
                    iconCls: (Wtf.isChrome? 'pwnd importChrome' : 'pwnd import'),
                    menu: importBtnArr
                });

                this.bbarBtnArr.push(this.importBtn);
            }
        }
        var extraConfig = {};
        extraConfig.url= config.isCustomer?"ACCCustomer/importCustomer1.do":"ACCVendor/importVendor1.do";
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: [filterName, this.typeEditor,{
                        xtype:'button',
                        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                        tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),//'Fetch your preferences.',
                        iconCls:'accountingbase fetch',
                        scope:this,
                        handler:this.loadTypeStore
                        },'-',!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)?this.editPersonCategory:""],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.ByCategoryStore,
                    displayInfo: true,
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                }),
                items : this.bbarBtnArr //ERP-19693
                    })
            }]
        }); 
        this.add(this.leadpan);
        
        this.on("activate",function(){
            if(this.isCategoryUpdated){
                this.ByCategoryStore.reload();
                this.isCategoryUpdated=false;
            }
        },this);
        Wtf.account.BusinessPersonListByCategory.superclass.onRender.call(this,config);
    },
    setValue:function(){
        if(this.typeEditor.getRawValue() == "" || this.typeEditor.getRawValue() == undefined ||this.typeEditor.getRawValue() == null){
            var record = new Wtf.data.Record({
                name:'All',
                id:'All'
            });
            if(this.typeEditor.store!=undefined && this.typeEditor.store!=null){
                var index=WtfGlobal.searchRecordIndex(this.typeEditor.store, 'All', 'name')
                if(index==-1){
                    this.typeEditor.store.insert(0,record);    
                }
            }
        }
        
    },
    
    
    loadTypeStore:function(a,rec){
        var categoryid = "";
        var salesPersonAgentId = "";
        if(this.isBySalesPersonOrAgent){
            salesPersonAgentId=this.typeEditor.getValue();
        }else{
            categoryid=this.typeEditor.getValue();
        }
        var currentBaseParams = this.ByCategoryStore.baseParams;
        if(categoryid!='All' && salesPersonAgentId!="All"){
            if(this.isBySalesPersonOrAgent){
                currentBaseParams.salesPersonAgentId=this.typeEditor.getValue();
            }else{
                currentBaseParams.categoryid=this.typeEditor.getValue();
            }
        }else{
            currentBaseParams.salesPersonAgentId="";
            currentBaseParams.categoryid="";
        }
        this.ByCategoryStore.baseParams = currentBaseParams;
        if(categoryid=='All' || salesPersonAgentId=="All") {
            this.ByCategoryStore.load({params: {start:0, limit:this.pP.combo.value, isPricingBandGrouping : this.isPricingBandGrouping, isBySalesPersonOrAgent:this.isBySalesPersonOrAgent}});
        } else {
            this.ByCategoryStore.load({params: {start:0, limit:this.pP.combo.value, categoryid:categoryid, isPricingBandGrouping : this.isPricingBandGrouping,salesPersonAgentId:salesPersonAgentId, isBySalesPersonOrAgent:this.isBySalesPersonOrAgent}});
        }
    },
    
    storeloaded:function(store){
        Wtf.MessageBox.hide();
    },
    opBalRenderer:function(val,m,rec){
//        return WtfGlobal.currencyDeletedRenderer(Math.abs(val),m,rec);
        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },

    balTypeRenderer:function(val,m,rec){
        val=val==0?"N/A":val>0?"Debit":"Credit";
        return WtfGlobal.deletedRenderer(val,m,rec)
    },
    editCustomerCategory:function(isCustomerGrouping) {
        callPersonGrouping(isCustomerGrouping, this.ByCategoryStore, this.isPricingBandGrouping, this.isBySalesPersonOrAgent);
    }
});

//'Customer Grouping' / 'Vendor Grouping' window of left navigation panel
Wtf.persongroupingwin = function (config){
    this.isCustomerGrouping=config.isCustomerGrouping;
    this.ByCategoryStore=config.ByCategoryStore;
    this.isPricingBandGrouping = config.isPricingBandGrouping? config.isPricingBandGrouping : false;
    this.isBySalesPersonOrAgent = config.isBySalesPersonOrAgent? config.isBySalesPersonOrAgent : false;
    
    Wtf.apply(this,config);
    Wtf.persongroupingwin.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:function () {
                if(!this.PersonGroupingForm.form.isValid())
                {
                    return;
                } else {
                    var personList=this.personList.getValue();
                    var personCategory=this.personCategory.getValue();
                    var param={
                        mode:14,
                        personList:personList,
                        personCategory:personCategory
                    }
                    var ajaxURL="";
                    if(this.isBySalesPersonOrAgent){
                        if(this.isCustomerGrouping){
                            ajaxURL="ACCCustomerCMN/saveCustomerSalesPersonMapping.do";
                        }else{
                            ajaxURL="ACCVendorCMN/saveVendorAgentMapping.do";
                        }
                    }else{
                        if(this.isPricingBandGrouping){
                            if(this.isCustomerGrouping){
                                ajaxURL="ACCCustomerCMN/saveCustomerPricingBandMapping.do";
                            }else{
                                ajaxURL="ACCVendorCMN/saveVendorPricingBandMapping.do";
                            }
                        }else{
                            if(this.isCustomerGrouping){
                                ajaxURL="ACCCustomerCMN/saveCustomerCategoryMapping.do";
                            }else{
                                ajaxURL="ACCVendorCMN/saveVendorCategoryMapping.do";
                            }
                        }
                    }
                    Wtf.Ajax.requestEx({
                        url : ajaxURL,
                        params:param
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                            this.close();
                            if (this.isBySalesPersonOrAgent) {
                                if (this.isCustomerGrouping) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.cust.editSalesPerson"),WtfGlobal.getLocaleText("acc.rem.500")],0);
                                } else {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.vend.editAgent"),WtfGlobal.getLocaleText("acc.rem.501")],0);
                                }
                            }else{
                                if (this.isPricingBandGrouping) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.edit") + " " + WtfGlobal.getLocaleText("acc.field.pricingBandMaster"), WtfGlobal.getLocaleText("acc.rem.241")],0);
                                } else if (this.isCustomerGrouping) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.cust.editCategoty"),WtfGlobal.getLocaleText("acc.rem.230")],0);
                                } else {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.cust.editCategoty"),WtfGlobal.getLocaleText("acc.rem.232")],0);
                                }
                            }
                            this.ByCategoryStore.reload();
                        } else{
                            if (this.isBySalesPersonOrAgent) {
                                if (this.isCustomerGrouping) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.cust.editSalesPerson"),WtfGlobal.getLocaleText("acc.rem.502")],1);
                                } else {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.vend.editAgent"),WtfGlobal.getLocaleText("acc.rem.503")],1);
                                }
                            }else{
                                if (this.isPricingBandGrouping) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.edit") + " " + WtfGlobal.getLocaleText("acc.field.pricingBandMaster"), WtfGlobal.getLocaleText("acc.rem.242")],0);
                                } else if (this.isCustomerGrouping) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.cust.editCategoty"),WtfGlobal.getLocaleText("acc.rem.231")],1);
                                } else {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.cust.editCategoty"),WtfGlobal.getLocaleText("acc.rem.233")],1);
                                }
                            }
                        }
                            

                    },
                    function(req){
                        if (this.isBySalesPersonOrAgent) {
                            if (this.isCustomerGrouping) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.cust.editSalesPerson"),WtfGlobal.getLocaleText("acc.rem.502")],1);
                            } else {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.vend.editAgent"),WtfGlobal.getLocaleText("acc.rem.503")],1);
                            }
                        }else{
                            if (this.isPricingBandGrouping) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.edit") + " " + WtfGlobal.getLocaleText("acc.field.pricingBandMaster"), WtfGlobal.getLocaleText("acc.rem.242")],0);
                            } else if (this.isCustomerGrouping) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.cust.editCategoty"),WtfGlobal.getLocaleText("acc.rem.231")],1);
                            } else {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.cust.editCategoty"),WtfGlobal.getLocaleText("acc.rem.233")],1);
                            }
                        }
                    });
                }
            }
        }, {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.persongroupingwin,Wtf.Window,{
    layout:"border",
    modal:true,
    id:'personsgroupinglinkforaccounting',
    width:450,
    height:230,
    resizable:false,
    iconCls: "pwnd favwinIcon",
    initComponent:function (){
        Wtf.persongroupingwin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetPersonGroupingForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);
        this.add(this.PersonGroupingForm);
    },
    GetNorthPanel:function (){
        var htmlDesc = "";
        if(this.isBySalesPersonOrAgent){
            if (this.isCustomerGrouping) {
                htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.cust.editSalesPerson"), WtfGlobal.getLocaleText("acc.cust.editSalesPersonDesc"),'../../images/createuser.png',false,'0px 0px 0px 0px');
            } else {
                htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.vend.editAgent"), WtfGlobal.getLocaleText("acc.vendor.editAgentDesc"),'../../images/createuser.png',false,'0px 0px 0px 0px');
            }
        }else{
            if (this.isPricingBandGrouping) {
                if (this.isCustomerGrouping) {
                    htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.common.edit") + " " + WtfGlobal.getLocaleText("acc.field.pricingBandMaster"), WtfGlobal.getLocaleText("acc.cust.editPricingListBandDesc"),'../../images/createuser.png',false,'0px 0px 0px 0px');
                } else {
                    htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.common.edit") + " " + WtfGlobal.getLocaleText("acc.field.pricingBandMaster"), WtfGlobal.getLocaleText("acc.vendor.editPricingListBandDesc"),'../../images/createuser.png',false,'0px 0px 0px 0px');
                }

            } else if (this.isCustomerGrouping) {
                htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.cust.editCategoty"), WtfGlobal.getLocaleText("acc.cust.editCategoryDesc"),'../../images/createuser.png',false,'0px 0px 0px 0px');
            } else {
                htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.vend.editCategory"), WtfGlobal.getLocaleText("acc.vend.editCategoryDesc"),'../../images/createuser.png',false,'0px 0px 0px 0px');
            }
        }
        
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html: htmlDesc
        });
    },
    GetPersonGroupingForm:function (){
        this.isCustomerGrouping? Wtf.customerAccStore.load() : Wtf.vendorAccStore.load();
        if(this.isBySalesPersonOrAgent){
            this.isCustomerGrouping? Wtf.salesPersonStore.load() : Wtf.agentStore.load();
        }else{
            if (this.isPricingBandGrouping) {
                this.pricingBandMasterRec = Wtf.data.Record.create([
                    {name: 'id'},
                    {name: 'name'},
                    {name: 'currencyName'},
                    {name: 'currencyID'}
                ]);

                this.pricingBandMasterStore = new Wtf.data.Store({
                    url: "ACCMaster/getPricingBandItems.do",
                    reader: new Wtf.data.KwlJsonReader({
                        totalProperty: "totalCount",
                        root: "data"
                    }, this.pricingBandMasterRec)
                });
                this.pricingBandMasterStore.load();
            } else {
                this.isCustomerGrouping? Wtf.CustomerCategoryStore.load() : Wtf.VendorCategoryStore.load();
            }
        }
        
        this.personList = new Wtf.common.Select(Wtf.apply({
            multiSelect:true,
            fieldLabel:this.isCustomerGrouping? WtfGlobal.getLocaleText("acc.customer.Cname") : WtfGlobal.getLocaleText("acc.customer.Vname"), //"Customer Name*":"Vendor Name*" ,
            forceSelection:true
        },{
            name:this.isCustomerGrouping? "customerlist":"vendorlist",
            id:this.isCustomerGrouping? "customerlist":"vendorlist",
            store: this.isCustomerGrouping? Wtf.customerAccStore : Wtf.vendorAccStore,
            valueField:'accid',
            displayField:'accname',
            emptyText:this.isCustomerGrouping? WtfGlobal.getLocaleText("acc.mp.cust") : WtfGlobal.getLocaleText("acc.mp.vend"), //"Please Select Customer":"Please Select Vendor",
            anchor:'85%',
            mode: 'local',
            selectOnFocus:true,
            allowBlank:false,
            triggerAction:'all',
            typeAhead: true,
            scope:this
        }));
        
        if (this.isPricingBandGrouping) {
            this.personCategory = new Wtf.form.ComboBox({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.pricingBandMaster") + "*", // "Pricing List-Band*"
                name: this.isCustomerGrouping? "customercategory" : "vendorcategory",
                id: this.isCustomerGrouping? "customercategory" : "vendorcategory",
                store: this.pricingBandMasterStore,
                valueField: 'id',
                displayField: 'name',
                emptyText: WtfGlobal.getLocaleText("acc.cust.pricingListBandEmptyText"), // "Select Price List - Band."
                anchor: '85%',
                mode: 'local',
                selectOnFocus: true,
                allowBlank:false,
                triggerAction: 'all',
                typeAhead: true,
                scope: this
            });
        } else {
            if(this.isBySalesPersonOrAgent){
                this.personCategory = new Wtf.common.Select(Wtf.apply({
                    multiSelect:true,
                    fieldLabel:this.isCustomerGrouping?WtfGlobal.getLocaleText("acc.masterConfig.15"):WtfGlobal.getLocaleText("acc.masterConfig.20")+"*", //"Sales Person/Agent*" ,
                    forceSelection:true
                },{
                    name:this.isCustomerGrouping?"customersalesperson":"vendoragent",
                    id:this.isCustomerGrouping?"customersalesperson":"vendoragent",
                    store: this.isCustomerGrouping? Wtf.salesPersonStore : Wtf.agentStore,
                    valueField:'id',
                    displayField:'name',
                    emptyText:this.isCustomerGrouping? WtfGlobal.getLocaleText("acc.cust.salespersonEmptyText") : WtfGlobal.getLocaleText("acc.vend.agentEmptyText"), //"Select Sales Person for Customer":"Select Agent for Vendor",
                    anchor:'85%',
                    mode: 'local',
                    selectOnFocus:true,
                    allowBlank:false,
                    triggerAction:'all',
                    typeAhead: true,
                    scope:this
                }));
            }else{
                this.personCategory = new Wtf.common.Select(Wtf.apply({
                    multiSelect:true,
                    fieldLabel:this.isCustomerGrouping?WtfGlobal.getLocaleText("acc.cust.Customercategory"):WtfGlobal.getLocaleText("acc.cust.vendorcategory")+"*", //"Category*" ,
                    forceSelection:true
                },{
                    name:this.isCustomerGrouping?"customercategory":"vendorcategory",
                    id:this.isCustomerGrouping?"customercategory":"vendorcategory",
                    store: this.isCustomerGrouping? Wtf.CustomerCategoryStore : Wtf.VendorCategoryStore,
                    valueField:'id',
                    displayField:'name',
                    emptyText:this.isCustomerGrouping? WtfGlobal.getLocaleText("acc.cust.categoryEmptyText") : WtfGlobal.getLocaleText("acc.vend.categoryEmptyText"), //"Select Category for Customer":"Select Category for Vendor",
                    anchor:'85%',
                    mode: 'local',
                    selectOnFocus:true,
                    allowBlank:false,
                    triggerAction:'all',
                    typeAhead: true,
                    scope:this
                }));
            }
        }
        
        this.PersonGroupingForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:10px",
            items:[this.personList, this.personCategory]
        });
    }
});

//===== For Product Mapping with Vendors Tab & Customer Tab -NeerajD========
//Same function call for Vendor Management  & Customer Management
Wtf.account.BusinessPersonProductMapping=function(config){
     Wtf.apply(this, config);
     this.businessPerson=(config.isCustomer?'Customer':'Vendor');
     this.GridRec1 = Wtf.data.Record.create ([
        {name: 'accid'},
        {name: 'openbalance'},
        {name: 'id'},
        {name: 'title'},
        {name: 'accname'},
        {name: 'aliasname'},
        {name: 'address'},
        {name: 'company'},
        {name: 'email'},
        {name: 'contactno'},
        {name: 'contactno2'},
        {name:'uenno'},
        {name:'vattinno'},
        {name:'csttinno'},
        {name:'gstin'},
        {name:'GSTINRegistrationTypeId'},        
        {name:'CustomerVendorTypeId'},
        {name:'GSTINRegistrationTypeName'},        
        {name:'CustomerVendorTypeName'},
        {name:'panno'},
        {name:'vendorbranch'}, 
        {name:'npwp'},
        {name:'servicetaxno'},        
        {name:'tanno'},        
        {name:'eccno'},
        {name: 'fax'},
        {name: 'shippingaddress'},
        {name: 'pdm'},
        {name: 'pdmname'},
        {name: 'parentid'},
        {name: 'parentname'},
        {name: 'bankaccountno'},
        {name: 'termid'},
        {name: 'termname'},
        {name: 'other'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'deleted'},
        {name: 'creationDate' ,type:'date'},
//        {name: 'creationDate' },//,type:'date' /This date is handled on JAVA side & sent as String only
        {name: 'taxno'},
        {name: 'categoryid'},
        {name: 'category'},
        {name:'productid'},
        {name:'productname'},
        {name:'desc'},
        {name:'pid'},
        {name:'vendor'},
        {name:'producttype'},
        {name:'type'},
        {name:'initialsalesprice'},
        {name:'warrantyperiod'},
        {name:'warrantyperiodsal'},
        {name:'uomid'},
        {name:'uomname'},
        {name:'parentuuid'},
        {name:'parentid'},
        {name:'parentname'},
        {name:'purchaseaccountid'},
        {name:'salesaccountid'},
        {name:'purchaseretaccountid'},
        {name:'salesretaccountid'},
        {name:'shelfLocationId'},
        {name:'reorderquantity'},
        {name:'quantity'},
        {name:'reorderlevel'},
        {name:'leadtime'},
        {name:'purchaseprice'},
        {name:'saleprice'},
        {name: 'leaf'},
        {name: 'warranty'},
        {name: 'syncable'},
        {name: 'qaenable'},
        {name: 'multiuom'},
        {name: 'blockLooseSell'},
        {name: 'level'},
        {name: 'initialquantity',mapping:'initialquantity'},
        {name: 'initialprice'},
        {name: 'ccountinterval'},
        {name:'ccounttolerance'},
        {name:'productweight'},
        {name:'vendornameid'},
        {name :'deducteeTypeId'},
        {name: 'dealertype'},
        {name: 'typeofdealer'},   // Ticket :ERP-26370   field use for MVAT Annexure Form  Indian Company only for Maharashtra
        {name: 'interstateparty'},
        {name: 'cformapplicable'},
        {name: 'vatregdate',type:'date'},
        {name: 'cstregdate',type:'date'},
        {name: 'defaultnatureofpurchase'},
        {name: 'manufacturertype'},
        {name: 'importereccno'},
        {name: 'iecno'},
        {name: 'range'},
        {name: 'division'},
        {name: 'defaultnatureofpurchase'},
        {name: 'commissionerate'}
        ]);
        
//        this.pageLimitvendorproduct = new Wtf.forumpPageSize({
//        ftree:this.gridproduct
//    });
    this.VendorNamesStore = new Wtf.data.GroupingStore({
        url:"ACC"+this.businessPerson+"CMN/get"+this.businessPerson+"ProductsMappingFunction.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec1),
        groupField:"accname",
        sortInfo: {field: 'productname',direction: "ASC"},
          baseParams:{
            mode:[2,22],
                productid:(this.record!=null?this.record.data['productid']:null),
                group:(config.isCustomer?[10]:[13])
        }
    });

    this.VendorNamesStore.on('load',this.setPageSize, this);
    this.VendorNamesStore.on('loadException',this.storeloaded, this);
    this.quickSearchVendor = new Wtf.KWLTagSearch({
            emptyText:config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.search"):WtfGlobal.getLocaleText("acc.vendorList.search"),  //'Search by '+this.businessPerson+' Name',
            width: 150,
            field: 'accname',
            Store:this.VendorNamesStore
        });
        this.VendorNamesStore.on('datachanged', function() {
        var p =  this.pP.combo.value;
        this.quickSearchVendor.setPage(p);
    }, this);
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false,
        handler:this.handleResetClick
    });
    
     this.VendorNamesStore.on('beforeload',function(store){
         store.baseParams.ss=this.quickSearchVendor.getValue();
     },this );
    
    this.sm = new Wtf.grid.RowSelectionModel();
    this.gridproduct = new Wtf.grid.GridPanel({    
        store:  this.VendorNamesStore,
        border:false,
        layout:'fit',
        view: new Wtf.grid.GroupingView({
            forceFit:false
        }),
        loadMask:true,
        columns:[
            {
           header:WtfGlobal.getLocaleText("acc.product.gridProduct"),//"Product",acc.productList.gridProduct
           dataIndex:'productname',
           pdfwidth:75
         },{
        	 header:WtfGlobal.getLocaleText("acc.productList.gridProductID"),
             dataIndex:'pid',
             align:'left',
             pdfwidth:75
         },{
             header:WtfGlobal.getLocaleText("acc.coa.gridAccountName"),//WtfGlobal.getLocaleText("acc.productList.gridProductID"),
             dataIndex:'accname',
//             align:'left',
             pdfwidth:75
         },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridAliasName"),  //"AliasName",
            dataIndex:'aliasname',
            renderer:WtfGlobal.deletedRenderer,
            sortable: true,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridDescription"),//"Description",
            dataIndex:'desc',
            renderer : function(val) {
                 val = val.replace(/(<([^>]+)>)/ig,"");
                 return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridDescription")+"'>"+val+"</div>";
            },
            pdfwidth:75
         },{
            hidden:true,
            dataIndex:'productid'
         },{
            header:WtfGlobal.getLocaleText("acc.masterConfig.uom"),
            dataIndex:'uomname',
            hidden:true
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductType"),//"Product Type",
            dataIndex:'type',
            pdfwidth:75,
            renderer: function(val){
                return  val;
            }
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridReorderQuantity"),//"Reorder Quantity",
            dataIndex:'reorderquantity',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridReorderLevel"),//"Reorder Level",
            dataIndex:'reorderlevel',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridLeadTime"),//"Lead Time(in days)",
            dataIndex:'leadtime',
            align:'right',
            renderer:this.LeadTimeRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"),//"Available Quantity",
            dataIndex:"quantity",
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"),//"Purchase Price",
            dataIndex:'purchaseprice',
            align:'right',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"),//"Sales Price",
            align:'right',
            dataIndex:'saleprice',
            pdfwidth:75,
            renderer:function(v,metadata,record){
                  if(record.data['type'] == "Inventory Non-Sale"){
                     return "N/A";
                 }else{ 
                      return WtfGlobal.currencyRenderer(v,false);
                   }
        }
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountInterval"),//"Cycle count Interval",
            align:'right',
            dataIndex:'ccountinterval',
            width:80,
            pdfwidth:75,
            renderer:function(v,metadata,record){
		 if(record.data['type'] == "Service"){
		 return "N/A";
	        }else{ 
                      return (v+' days');}
	    }
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridCycleCountTolerance"),//"Cycle count Tolerance",
            align:'right',
            dataIndex:'ccounttolerance',
            width:80,
            pdfwidth:75,
            renderer:function(v,metadata,record){
            					 if(record.data['type'] == "Service"){
            						 return "N/A";
            					 }else{
            						 return v+' %';}
//            						 return'<div class="currency">'+v+'%</div>';}
            					 }
        },{
            header:WtfGlobal.getLocaleText("acc.field.Warranty(Days)"),//"Cycle count Tolerance",
            align:'center',
            dataIndex:'warranty',
            width:80,
            pdfwidth:75
            
        }]
    });
    Wtf.account.BusinessPersonProductMapping.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.BusinessPersonProductMapping,Wtf.Panel,{
        onRender: function(config){
        this.VendorNamesStore.load({
            params:{
                start:0,
                limit:30
            }
        });
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.gridproduct],
                tbar: [(this.isCustomer?WtfGlobal.getLocaleText("acc.agedReceive.gridCustomer"):WtfGlobal.getLocaleText("acc.het.180")),
                    this.quickSearchVendor,this.resetBttn
//                    ,{
//                        xtype:'button',
//                        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
////                        tooltip:WtfGlobal.getLocaleText("acc.trial.fetchTT"),  //"Select a time period to view corresponding trial balance records.",
//                        iconCls:'accountingbase fetch',
//                        scope:this,
//                        handler:this.loadProductStore
//                        }, '-'
                    ],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize:30,
                    id: "pagingtoolbar" + this.id,
                    store: this.VendorNamesStore,
                    displayInfo: true,
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    })
                })
            }]
        }); 
        this.add(this.leadpan);
        
        this.on("activate",function(){
            if(this.isCategoryUpdated){
                this.VendorNamesStore.reload();
                this.isCategoryUpdated=false;
            }
        },this);
        Wtf.account.BusinessPersonProductMapping.superclass.onRender.call(this,config);
    },
     setPageSize: function(store, rec, opt) {
        var count = 0;
        for (var i = 0; i < store.getCount(); i++) {
            if (rec[i].data['level'] == 0 && (rec[i].data['parentid'] == "" || rec[i].data['parentid'] == undefined))
                count++;
        }
        this.gridproduct.getView().refresh();
        this.storeloaded();
    },
//    parameterss:function(store, rec, opt){
//        
//    },

    
    loadProductStore:function(a,rec){
            this.VendorNamesStore.load();
    },
    storeloaded:function(store){
        Wtf.MessageBox.hide();
    },
    opBalRenderer:function(val,m,rec){
//        return WtfGlobal.currencyDeletedRenderer(Math.abs(val),m,rec);
        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },

    balTypeRenderer:function(val,m,rec){
        val=val==0?"N/A":val>0?"Debit":"Credit";
        return WtfGlobal.deletedRenderer(val,m,rec)
    },
    handleResetClick:function(){
        if(this.quickSearchVendor.getValue()){
            this.quickSearchVendor.reset();
            this.VendorNamesStore.reload();
        }
    }
});
